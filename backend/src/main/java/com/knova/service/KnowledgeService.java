package com.knova.service;

import com.knova.domain.KnowledgeDocument;
import com.knova.repository.KnowledgeDocumentMapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.*;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** 文档向量化与 RAG 问答业务。 */
@Service
@RequiredArgsConstructor
public class KnowledgeService {
    private final KnowledgeDocumentMapper documentMapper;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final VectorStorePersistence vectorStorePersistence;
    private final ChatModel chatModel;
    private final Tika tika = new Tika();

    /**
     * 解析上传文件并写入 Milvus。
     * 元数据同时携带知识库 ID、文档 ID 和文件名，便于隔离检索及精确删除。
     */
    public KnowledgeDocument ingest(Long knowledgeBaseId, MultipartFile file) {
        KnowledgeDocument documentRecord = new KnowledgeDocument();
        documentRecord.setKnowledgeBaseId(knowledgeBaseId);
        documentRecord.setFileName(file.getOriginalFilename());
        documentRecord.setContentType(file.getContentType());
        documentRecord.setSize(file.getSize());
        documentMapper.insert(documentRecord);

        try {
            String text = tika.parseToString(file.getInputStream());
            Metadata metadata = new Metadata()
                    .put("knowledgeBaseId", knowledgeBaseId.toString())
                    .put("documentId", documentRecord.getId().toString())
                    .put("fileName", documentRecord.getFileName());
            Document document = Document.from(text, metadata);

            // 800 字符切片并保留 120 字符重叠，减少跨片段语义丢失。
            var segments = DocumentSplitters.recursive(800, 120).split(document);
            embeddingStore.addAll(embeddingModel.embedAll(segments).content(), segments);
            vectorStorePersistence.persist();
            documentRecord.setSegmentCount(segments.size());
            documentRecord.setStatus("READY");
        } catch (Exception exception) {
            documentRecord.setStatus("FAILED");
            documentRecord.setErrorMessage(limitError(exception.getMessage()));
        }
        documentMapper.updateById(documentRecord);
        return documentRecord;
    }

    /** 对问题向量化，仅召回当前知识库的前 6 个高相关片段，再交给大模型生成答案。 */
    public String ask(Long knowledgeBaseId, String question) {
        var queryEmbedding = embeddingModel.embed(question).content();
        var filter = new MetadataFilterBuilder("knowledgeBaseId").isEqualTo(knowledgeBaseId.toString());
        var matches = embeddingStore.search(EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding).maxResults(6).minScore(0.55).filter(filter).build()).matches();
        String context = matches.stream()
                .map(match -> "[来源: " + match.embedded().metadata().getString("fileName") + "]\n" + match.embedded().text())
                .reduce("", (left, right) -> left + "\n\n" + right);
        if (context.isBlank()) return "当前知识库中没有找到足够相关的内容。";
        return chatModel.chat("你是企业知识库助手。只基于以下资料回答；不知道就明确说明。"
                + "使用清晰的中文，并在结尾列出引用文件。\n\n资料：" + context + "\n\n问题：" + question);
    }

    /** 同时清理向量和关系型数据库记录。先删向量可降低产生孤儿向量的概率。 */
    public void delete(KnowledgeDocument document) {
        embeddingStore.removeAll(new MetadataFilterBuilder("documentId").isEqualTo(document.getId().toString()));
        vectorStorePersistence.persist();
        documentMapper.deleteById(document.getId());
    }

    private String limitError(String message) {
        if (message == null) return "未知解析错误";
        return message.substring(0, Math.min(message.length(), 1000));
    }
}
