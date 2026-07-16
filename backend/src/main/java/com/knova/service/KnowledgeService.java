package com.knova.service;

import com.knova.domain.KnowledgeDocument;
import com.knova.repository.KnowledgeDocumentMapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 负责文档切片、向量化与 RAG 检索问答。
 */
@Service
@RequiredArgsConstructor
public class KnowledgeService {
    private final KnowledgeDocumentMapper documentMapper;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final VectorStorePersistence vectorStorePersistence;
    private final ChatModel chatModel;
    private final Tika tika = new Tika();

    public KnowledgeDocument ingest(Long baseId, MultipartFile file) {
        KnowledgeDocument record = new KnowledgeDocument();
        record.setKnowledgeBaseId(baseId);
        record.setFileName(file.getOriginalFilename());
        record.setContentType(file.getContentType());
        record.setSize(file.getSize());
        documentMapper.insert(record);
        try {
            String text = tika.parseToString(file.getInputStream());
            Metadata metadata = new Metadata().put("knowledgeBaseId", baseId.toString())
                    .put("documentId", record.getId().toString()).put("fileName", record.getFileName());
            List<TextSegment> segments = DocumentSplitters.recursive(800, 120).split(Document.from(text, metadata));
            embeddingStore.addAll(embeddingModel.embedAll(segments).content(), segments);
            vectorStorePersistence.persist();
            record.setSegmentCount(segments.size());
            record.setStatus("READY");
        } catch (Exception exception) {
            record.setStatus("FAILED");
            record.setErrorMessage(limit(exception.getMessage()));
        }
        documentMapper.updateById(record);
        return record;
    }

    /**
     * 对全部可访问知识库分别过滤召回，再按相似度合并 Top 8。
     */
    public String ask(List<Long> baseIds, String question) {
        if (baseIds == null || baseIds.isEmpty()) return "请至少选择一个知识空间。";
        var query = embeddingModel.embed(question).content();
        List<EmbeddingMatch<TextSegment>> all = new ArrayList<>();
        for (Long id : new LinkedHashSet<>(baseIds)) {
            var filter = new MetadataFilterBuilder("knowledgeBaseId").isEqualTo(id.toString());
            all.addAll(embeddingStore.search(EmbeddingSearchRequest.builder().queryEmbedding(query)
                    .maxResults(6).minScore(0.55).filter(filter).build()).matches());
        }
        List<EmbeddingMatch<TextSegment>> matches = all.stream()
                .sorted(Comparator.comparingDouble((EmbeddingMatch<TextSegment> match) -> match.score()).reversed())
                .limit(8).toList();
        String context = matches.stream().map(match -> "[来源: "
                        + match.embedded().metadata().getString("fileName") + "]\n" + match.embedded().text())
                .reduce("", (left, right) -> left + "\n\n" + right);
        if (context.isBlank()) return "所选知识空间中没有找到足够相关的内容。";
        return chatModel.chat("你是企业知识助手。只基于资料回答；不知道时明确说明。"
                + "使用清晰中文，并在结尾列出引用文件。\n\n资料：" + context + "\n\n问题：" + question);
    }

    public void delete(KnowledgeDocument document) {
        embeddingStore.removeAll(new MetadataFilterBuilder("documentId").isEqualTo(document.getId().toString()));
        vectorStorePersistence.persist();
        documentMapper.deleteById(document.getId());
    }

    private String limit(String value) {
        if (value == null) return "未知解析错误";
        return value.substring(0, Math.min(value.length(), 1000));
    }
}
