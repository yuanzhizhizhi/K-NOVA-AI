package com.knova.service;

import com.knova.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 返回稳定的系统能力说明，避免从企业知识库中检索产品自身的使用帮助。 */
@Service
@RequiredArgsConstructor
public class HelpService {
    private static final String ASSISTANT_IDENTITY_MESSAGE =
            "我是 GGBOND AI 企业知识助手，可以根据你有权限的知识库文档回答问题。";
    private static final String CAPABILITY_MESSAGE =
            "我是 GGBOND AI 企业知识助手。我会自动检索你有权限的全部未归档知识库，"
                    + "根据相关文档片段回答问题。你可以在“知识库管理”中创建知识库并批量上传 "
                    + "PDF、Word、PPT、TXT 或 Markdown 文件。当前未接入实时天气、新闻和互联网搜索能力。";

    private final UserService userService;

    /**
     * 返回当前系统的固定能力边界。
     * 参数暂时保留，方便后续根据不同帮助意图拆分上传说明、文件类型说明等回答。
     */
    public String answer(String username, String question) {
        // 1. 规范化问题，区分用户身份、AI 身份和系统能力问题。
        String normalized = LocalChatIntentClassifier.normalize(question);
        // 2. “我是谁”读取当前登录账号，禁止模型猜测用户真实身份。
        if ("我是谁".equals(normalized)) {
            return currentUserIdentity(username);
        }
        // 3. “你是谁”只返回 AI 身份，避免混入冗长的全部能力说明。
        if ("你是谁".equals(normalized) || "你叫什么".equals(normalized)) {
            return ASSISTANT_IDENTITY_MESSAGE;
        }
        // 4. 知识库管理问题返回对应页面和操作路径，不进入 RAG 检索。
        String managementAnswer = knowledgeManagementAnswer(normalized);
        if (managementAnswer != null) {
            return managementAnswer;
        }
        // 5. 其他帮助问题返回稳定的系统能力和边界说明，不访问向量库。
        return CAPABILITY_MESSAGE;
    }

    private String knowledgeManagementAnswer(String question) {
        // 1. 删除操作说明入口、权限和不可恢复风险。
        if (question.contains("删除") || question.contains("移除")) {
            return "请进入“知识库管理”，找到你拥有的目标知识库，点击卡片上的“删除”按钮并确认。"
                    + "只有知识库所有者可以删除；删除会同时清理文档、向量和相关历史数据，请谨慎操作。";
        }
        // 2. 创建操作说明知识库管理页面的新建入口。
        if (question.contains("创建") || question.contains("新建")) {
            return "请进入“知识库管理”，点击“新建知识库”，填写名称、描述和主题颜色后保存。";
        }
        // 3. 上传操作说明必须先选中具体知识库。
        if (question.contains("上传") || question.contains("导入")) {
            return "请进入“知识库管理”，打开目标知识库后上传文档。系统会自动解析、切片并生成向量。";
        }
        // 4. 成员和授权操作仅允许知识库所有者执行。
        if (question.contains("成员") || question.contains("授权")) {
            return "请进入“知识库管理”，打开目标知识库的成员管理。只有所有者可以添加成员或调整角色。";
        }
        // 5. 收藏和归档属于知识库展示配置。
        if (question.contains("收藏") || question.contains("归档")) {
            return "请进入“知识库管理”，在目标知识库卡片上使用收藏或归档操作；归档后不会参与默认问答检索。";
        }
        // 6. 没有匹配到具体动作时交给通用能力说明。
        return null;
    }

    private String currentUserIdentity(String username) {
        // 1. 根据安全上下文中的用户名查询当前用户资料。
        User user = userService.current(username);
        // 2. 展示名称为空时只返回登录账号，避免输出无意义空字段。
        if (user.getDisplayName() == null || user.getDisplayName().isBlank()) {
            return "你当前登录的账号是“" + user.getUsername() + "”。除此之外，我不会猜测你的真实身份。";
        }
        // 3. 返回登录账号和展示名称，并声明系统不会推断用户真实身份。
        return "你当前登录的账号是“" + user.getUsername() + "”，展示名称是“"
                + user.getDisplayName() + "”。除此之外，我不会猜测你的真实身份。";
    }
}
