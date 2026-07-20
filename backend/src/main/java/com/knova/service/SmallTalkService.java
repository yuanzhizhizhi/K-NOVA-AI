package com.knova.service;

import org.springframework.stereotype.Service;

/** 使用稳定模板处理高频闲聊，不调用 Embedding、Milvus 或聊天模型。 */
@Service
public class SmallTalkService {
    /**
     * 根据明确的闲聊关键词返回本地模板。
     * 本方法不访问向量数据库，也不调用大模型，可降低简单问候的延迟和调用成本。
     */
    public String answer(String question) {
        // 1. 规范化用户消息，兼容英文大小写和首尾空格。
        String text = question == null ? "" : question.trim().toLowerCase();
        // 2. 按致谢、告别和时段问候匹配稳定模板。
        if (text.contains("谢谢") || text.contains("感谢") || text.contains("多谢")) {
            return "不客气。如果还有知识库相关问题，可以继续问我。";
        }
        if (text.contains("再见") || text.contains("拜拜") || text.contains("bye")) {
            return "再见，随时欢迎回来继续查询资料。";
        }
        if (text.contains("早上好")) {
            return "早上好！我是 GGBOND AI，已经准备好帮你查询知识库了。";
        }
        if (text.contains("下午好")) {
            return "下午好！有什么知识库问题需要我协助吗？";
        }
        if (text.contains("晚上好")) {
            return "晚上好！你可以直接告诉我想查询的内容。";
        }
        // 3. 未命中特定模板时返回通用问候和能力引导。
        return "你好，我是 GGBOND AI。你可以直接询问已接入知识库中的内容。";
    }
}
