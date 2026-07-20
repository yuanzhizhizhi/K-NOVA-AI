package com.knova.service;

import com.knova.domain.KnowledgeDocument;
import com.knova.repository.KnowledgeDocumentMapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * 负责文档切片、向量化与 RAG 检索问答。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeService {
    private final KnowledgeDocumentMapper documentMapper;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final VectorStorePersistence vectorStorePersistence;
    private final ChatModel chatModel;
    private final Tika tika = new Tika();

    @Value("${app.chat-routing.rag-min-score:0.55}")
    private double ragMinScore;
    @Value("${app.chat-routing.rag-max-results-per-base:6}")
    private int ragMaxResultsPerBase;
    @Value("${app.chat-routing.rag-final-top-k:8}")
    private int ragFinalTopK;

    /**
     * 文档入库主流程：创建处理记录 -> Tika 提取正文 -> 递归切片 ->
     * Embedding 向量化 -> 写入向量库 -> 更新处理状态与切片数量。
     */
    public KnowledgeDocument ingest(Long baseId, MultipartFile file) {
        // 1. 创建文档处理记录，确保失败时也能查询处理状态和错误原因。
        KnowledgeDocument record = new KnowledgeDocument();
        record.setKnowledgeBaseId(baseId);
        record.setFileName(file.getOriginalFilename());
        record.setContentType(file.getContentType());
        record.setSize(file.getSize());
        documentMapper.insert(record);
        try {
            // 2. 使用 Tika 把 PDF、Office、TXT 等格式统一提取为纯文本。
            String text = tika.parseToString(file.getInputStream());
            // 3. 为每个片段写入知识库、文档和文件名元数据。
            Metadata metadata = new Metadata().put("knowledgeBaseId", baseId.toString())
                    .put("documentId", record.getId().toString()).put("fileName", record.getFileName());
            // 4. 递归切片并保留重叠内容，降低语义在切片边界丢失的概率。
            List<TextSegment> segments = DocumentSplitters.recursive(800, 120).split(Document.from(text, metadata));
            // 5. 批量生成向量、写入向量库并触发本地持久化。
            embeddingStore.addAll(embeddingModel.embedAll(segments).content(), segments);
            vectorStorePersistence.persist();
            // 6. 更新文档切片数量和成功状态。
            record.setSegmentCount(segments.size());
            record.setStatus("READY");
        } catch (Exception exception) {
            // 7. 捕获解析或向量化异常，记录堆栈并保存失败状态。
            log.error("文档入库失败，文档ID={}，文件名={}，异常信息={}",
                    record.getId(), record.getFileName(), exception.getMessage(), exception);
            record.setStatus("FAILED");
            record.setErrorMessage(limit(exception.getMessage()));
        }
        // 8. 无论成功失败都更新处理结果，供管理页面展示。
        documentMapper.updateById(record);
        return record;
    }

    /**
     * 对全部可访问知识库分别过滤召回，再按相似度合并 Top 8。
     */
    public RagResult ask(List<Long> baseIds, String question) {
        // 1. 校验至少存在一个可访问知识库。
        if (baseIds == null || baseIds.isEmpty()) {
            return RagResult.unmatched("请至少选择一个知识空间。");
        }
        long embeddingStartTime = System.currentTimeMillis();
        // 2. 使用与文档相同的 Embedding 模型生成问题向量。
        Embedding query;
        try {
            query = embeddingModel.embed(question).content();
        } catch (Exception exception) {
            log.error("问题向量化失败，知识库数量={}，异常信息={}",
                    baseIds.size(), exception.getMessage(), exception);
            return RagResult.unmatched("知识检索服务暂时不可用，请稍后重试。");
        }
        long embeddingDuration = System.currentTimeMillis() - embeddingStartTime;
        long searchStartTime = System.currentTimeMillis();
        List<EmbeddingMatch<TextSegment>> all = new ArrayList<>();
        // 3. 对每个知识库单独使用元数据过滤执行向量召回。
        try {
            for (Long id : new LinkedHashSet<>(baseIds)) {
                var filter = new MetadataFilterBuilder("knowledgeBaseId").isEqualTo(id.toString());
                all.addAll(embeddingStore.search(EmbeddingSearchRequest.builder().queryEmbedding(query)
                        .maxResults(ragMaxResultsPerBase).minScore(ragMinScore).filter(filter).build()).matches());
            }
        } catch (Exception exception) {
            log.error("向量检索失败，知识库数量={}，异常信息={}",
                    baseIds.size(), exception.getMessage(), exception);
            return RagResult.unmatched("向量检索服务暂时不可用，请稍后重试。");
        }
        // 4. 合并结果并按相似度排序，只保留配置数量的最相关片段。
        List<EmbeddingMatch<TextSegment>> matches = all.stream()
                .sorted(Comparator.comparingDouble((EmbeddingMatch<TextSegment> match) -> match.score()).reversed())
                .limit(ragFinalTopK).toList();
        long searchDuration = System.currentTimeMillis() - searchStartTime;
        // 5. 组装带文件来源标记的上下文，未召回时返回明确降级提示。
        String context = matches.stream().map(match -> "[来源: "
                        + match.embedded().metadata().getString("fileName") + "]\n" + match.embedded().text())
                .reduce("", (left, right) -> left + "\n\n" + right);
        if (context.isBlank()) {
            log.info("RAG 检索完成但未召回相关片段，知识库数量={}，向量化耗时毫秒={}，向量检索耗时毫秒={}",
                    baseIds.size(), embeddingDuration, searchDuration);
            return RagResult.unmatched("所选知识空间中没有找到足够相关的内容。可以换一种问法后重试。");
        }
        // 6. 整理并去重引用文件名。
        List<String> sources = matches.stream()
                .map(match -> match.embedded().metadata().getString("fileName"))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        // 7. 调用聊天模型基于召回资料生成最终回答。
        long chatStartTime = System.currentTimeMillis();
        String answer;
        try {
            answer = chatModel.chat("你是企业知识助手。只基于资料回答；不知道时明确说明。"
                    + "使用清晰中文，并在结尾列出引用文件。\n\n资料：" + context + "\n\n问题：" + question);
        } catch (Exception exception) {
            log.error("RAG 回答模型调用失败，召回片段数={}，异常信息={}",
                    matches.size(), exception.getMessage(), exception);
            return RagResult.unmatched("回答生成服务暂时不可用，请稍后重试。");
        }
        // 8. 记录各阶段耗时并返回回答、命中状态、来源和片段数量。
        log.info("RAG 回答完成，知识库数量={}，召回片段数={}，向量化耗时毫秒={}，"
                        + "向量检索耗时毫秒={}，回答模型耗时毫秒={}",
                baseIds.size(), matches.size(), embeddingDuration, searchDuration,
                System.currentTimeMillis() - chatStartTime);
        return new RagResult(answer, true, sources, matches.size());
    }

    /** 删除文档对应的全部向量，持久化向量库状态后再删除数据库记录。 */
    public void delete(KnowledgeDocument document) {
        // 1. 按 documentId 元数据删除文档全部向量。
        embeddingStore.removeAll(new MetadataFilterBuilder("documentId").isEqualTo(document.getId().toString()));
        // 2. 持久化向量库变化后删除关系数据库文档记录。
        vectorStorePersistence.persist();
        documentMapper.deleteById(document.getId());
    }

    /** 限制解析异常长度，避免数据库错误字段和接口日志被超长堆栈占满。 */
    private String limit(String value) {
        // 1. null 异常信息转换为稳定提示。
        if (value == null) {
            return "未知解析错误";
        }
        // 2. 截断超长错误信息，避免占满数据库字段和接口日志。
        return value.substring(0, Math.min(value.length(), 1000));
    }

    /** RAG 结构化结果，供接口展示来源并区分无召回降级回答。 */
    public record RagResult(String answer, boolean matched, List<String> sources, int retrievedSegments) {
        public RagResult {
            // 1. 把 null 来源转换为空集合并创建不可变副本。
            sources = sources == null ? List.of() : List.copyOf(sources);
        }

        static RagResult unmatched(String answer) {
            // 1. 创建没有召回片段和引用来源的降级结果。
            return new RagResult(answer, false, List.of(), 0);
        }
    }
}
