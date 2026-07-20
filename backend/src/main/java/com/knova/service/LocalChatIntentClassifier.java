package com.knova.service;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/** 仅处理高置信度本地规则；无法确定时返回空值，由模型分类器兜底。 */
@Component
class LocalChatIntentClassifier {
    private static final Set<String> SMALL_TALK_PHRASES = Set.of(
            "你好", "您好", "嗨", "哈喽", "hello", "hi", "谢谢", "多谢", "感谢", "辛苦了", "不客气",
            "再见", "拜拜", "bye", "早上好", "中午好", "下午好", "晚上好"
    );
    private static final Set<String> HELP_PHRASES = Set.of(
            "你是谁", "你叫什么", "我是谁", "你能做什么", "你会什么", "怎么使用", "如何使用", "支持什么文件",
            "支持上传什么文件", "怎么上传文档", "如何上传知识库", "回答来自哪里", "答案是从哪里来的"
    );
    private static final Set<String> KNOWLEDGE_KEYWORDS = Set.of(
            "如何", "怎么", "为什么", "哪些", "流程", "规定", "配置", "文档", "制度", "公司", "项目",
            "产品", "合同", "手册", "审批", "退款", "milvus"
    );
    private static final Set<String> KNOWLEDGE_MANAGEMENT_ACTIONS = Set.of(
            "创建", "新建", "删除", "移除", "上传", "导入", "管理", "维护", "成员", "授权", "收藏", "归档"
    );

    /**
     * 按“精确闲聊、系统帮助、明确知识特征”的顺序识别意图。
     * 返回 Optional.empty() 表示规则置信度不足，必须交由 LLM 继续分类。
     */
    Optional<ChatIntent> classify(String question) {
        // 1. 统一大小写、空白和句尾标点。
        String normalized = normalize(question);
        // 2. 精确匹配纯闲聊短句，防止业务问题因包含问候词而误判。
        if (SMALL_TALK_PHRASES.contains(normalized)) {
            return Optional.of(ChatIntent.SMALL_TALK);
        }
        // 3. 匹配系统身份、能力和使用方法问题。
        if (HELP_PHRASES.contains(normalized) || isHelpQuestion(normalized)) {
            return Optional.of(ChatIntent.HELP);
        }
        // 4. 知识库创建、删除、上传等产品操作直接进入帮助策略。
        if (isKnowledgeManagementQuestion(normalized)) {
            return Optional.of(ChatIntent.HELP);
        }
        // 5. 情绪寒暄保留为不确定状态，交给模型理解完整语义。
        if (isConversationalQuestion(normalized)) {
            return Optional.empty();
        }
        // 6. 出现明确企业知识特征时直接进入知识问答。
        if (hasKnowledgeFeature(normalized)) {
            return Optional.of(ChatIntent.KNOWLEDGE);
        }
        // 7. 没有足够证据时返回空值，禁止本地规则强行分类。
        return Optional.empty();
    }

    private boolean isKnowledgeManagementQuestion(String text) {
        // 1. 操作帮助必须明确提到知识库或知识空间，避免宽泛动作词误判业务问题。
        boolean knowledgeTarget = text.contains("知识库") || text.contains("知识空间");
        // 2. 同时包含任一产品管理动作时识别为 HELP。
        return knowledgeTarget && KNOWLEDGE_MANAGEMENT_ACTIONS.stream().anyMatch(text::contains);
    }

    private boolean isHelpQuestion(String text) {
        // 1. 判断消息是否包含系统能力或使用方式特征。
        boolean helpFeature = text.contains("你能做什么") || text.contains("支持哪些文件")
                || text.contains("怎么用这个系统") || text.contains("如何使用这个系统");
        // 2. 同时存在企业知识关键词时不按帮助处理，避免复合业务问题误判。
        return helpFeature && !containsKnowledgeKeyword(text);
    }

    /** “你今天怎么样”包含“怎么”但属于寒暄，必须留给模型结合完整语义判断。 */
    private boolean isConversationalQuestion(String text) {
        // 1. 匹配包含“怎么样”但实际表达情绪寒暄的完整短句。
        return text.matches("^(你|你今天|你最近|最近)(过得)?怎么样$")
                || text.matches("^(你|你今天|你最近)状态(怎么样|好吗)$");
    }

    private boolean hasKnowledgeFeature(String text) {
        // 1. 命中企业知识关键词时直接认定为知识问题。
        if (containsKnowledgeKeyword(text)) {
            return true;
        }
        // 2. 带问号且不是系统身份问题时按知识问题处理。
        return (text.contains("?") || text.contains("？")) && !isSystemIdentityQuestion(text);
    }

    private boolean containsKnowledgeKeyword(String text) {
        // 1. 判断文本是否包含任一明确知识问答特征词。
        return KNOWLEDGE_KEYWORDS.stream().anyMatch(text::contains);
    }

    private boolean isSystemIdentityQuestion(String text) {
        // 1. 识别询问当前 AI、当前用户身份或系统能力的问题。
        return text.contains("你是谁") || text.contains("你叫什么") || text.contains("我是谁")
                || text.contains("你能做什么");
    }

    static String normalize(String question) {
        // 1. null 输入转换为空串，避免后续规则发生空指针异常。
        if (question == null) {
            return "";
        }
        // 2. 去除首尾空格、统一英文大小写、清理句尾标点并压缩连续空白。
        return question.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[。！!？?～~]+$", "")
                .replaceAll("\\s+", " ");
    }
}
