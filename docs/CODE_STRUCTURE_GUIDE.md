# K-NOVA-AI 当前代码职责说明

## 1. 请求处理主链路

```text
Vue 前端
  → Controller 接收请求和转换 DTO
  → Service 执行业务规则与用例编排
  → Mapper 使用 MyBatis-Plus 读写关系数据库
  → LangChain4j 调用 Embedding、Milvus 和聊天模型
```

Controller 不直接编写数据库和 RAG 逻辑；Mapper 不处理业务规则；对话回答方式通过策略接口扩展。

## 2. config：基础设施配置

- `AiConfig`：创建 Embedding 模型、RAG 回答模型和意图分类模型。
- `DevVectorStoreConfig`：开发环境加载本地 JSON 向量库。
- `ProdVectorStoreConfig`：生产环境连接 Milvus 向量库。
- `SecurityConfig`：配置无状态 Spring Security、CORS、JWT 认证过滤器和接口访问规则。

## 3. controller：HTTP 接口

- `AuthController`：用户登录及首次启动管理员初始化。
- `KnowledgeController`：知识库、文档、成员、会话和 AI 问答接口。
- `ProfileController`：当前用户头像上传、头像读取和密码修改。
- `UserController`：管理员维护系统用户。

Controller 只负责读取当前登录用户、接收参数、调用 Service 和转换响应，不承载核心业务规则。

## 4. service：业务与应用层

### 4.1 对话编排

- `ChatService`：一次聊天用例的事务编排，负责校验、会话准备、消息保存、意图识别和回答路由。
- `ChatRequestValidator`：校验用户名、空问题、纯标点问题及最大长度。
- `ChatConversationManager`：创建全局对话、验证会话类型并保存消息。
- `ConversationService`：使用 MyBatis-Plus 维护会话与消息记录。

### 4.2 意图识别

- `ChatIntentService`：先调用本地规则，无法确定时调用 LLM 分类器。
- `LocalChatIntentClassifier`：识别高置信闲聊、系统帮助和明确知识问题。
- `LlmChatIntentClassifier`：使用独立短超时模型处理模糊消息，异常时降级为知识问答。
- `ChatIntent`：定义 `SMALL_TALK`、`HELP`、`KNOWLEDGE` 三种意图。

### 4.3 回答策略

- `ChatAnswerStrategy`：回答策略统一接口。
- `ChatAnswerRouter`：根据意图查找对应策略，避免在 ChatService 中维护 switch 分支。
- `SmallTalkAnswerStrategy`：返回本地闲聊模板，不访问向量库。
- `HelpAnswerStrategy`：返回系统能力和使用说明。
- `KnowledgeAnswerStrategy`：调用知识检索接口生成 RAG 回答。
- `ChatAnswerResult`：统一携带回答正文和引用来源。

### 4.4 RAG 与知识库

- `KnowledgeService`：文档解析、切片、向量化、向量召回、来源整理和模型回答。
- `KnowledgeRetriever`：知识检索抽象接口，隔离回答策略与具体 RAG 实现。
- `AccessibleKnowledgeRetriever`：查询当前用户全部可访问且未归档的知识库。
- `KnowledgeBaseService`：创建、查询和删除知识库，聚合文档数量和用户展示配置。
- `KnowledgeSpaceService`：维护知识空间成员、角色权限、收藏、归档、图标和排序。
- `VectorStorePersistence`：本地向量库持久化抽象；Milvus 环境不需要文件写回。

### 4.5 用户

- `UserService`：用户登录、JWT 签发、用户维护、头像地址和密码修改。

## 5. repository：数据访问层

所有 Mapper 继承 MyBatis-Plus `BaseMapper`，提供对应领域对象的通用 CRUD：

- `UserMapper`：系统用户。
- `KnowledgeBaseMapper`：知识库主记录。
- `KnowledgeDocumentMapper`：知识文档处理状态。
- `KnowledgeSpaceProfileMapper`：知识空间展示配置。
- `KnowledgeSpaceMemberMapper`：知识空间成员权限。
- `ChatConversationMapper`：对话主记录。
- `ChatMessageMapper`：对话消息。

## 6. domain：数据库实体

- `User`：账号、密码摘要、角色、启用状态和头像。
- `KnowledgeBase`：知识库名称、描述和主题颜色。
- `KnowledgeDocument`：上传文件、解析状态、切片数量和错误信息。
- `KnowledgeSpaceProfile`：所有者、收藏、归档、图标和排序。
- `KnowledgeSpaceMember`：用户在知识空间内的角色。
- `ChatConversation`：会话标题、所属用户和最后更新时间。
- `ChatMessage`：消息角色、内容和创建时间。

## 7. exception 与 log：横切能力

- `BusinessException`：携带稳定错误码、用户提示和 HTTP 状态。
- `GlobalExceptionHandler`：把业务异常转换为统一 JSON 错误响应。
- `ApiLog`：声明接口日志模块、操作名称和入参出参记录策略。
- `ApiLogAspect`：统一记录接口中文入参、出参、耗时和异常，并脱敏密码、Token 等字段。
- `RequestTraceFilter`：为每个请求生成或透传 traceId，串联一次请求的全部日志。

## 8. resources：运行配置

- `application.yml`：通用服务、模型、意图路由、RAG 和 MyBatis-Plus 配置。
- `application-dev.yml`：H2 和本地文件向量库配置。
- `application-prod.yml`：MySQL 和 Milvus 配置。
- `schema.sql`：关系型数据库表结构初始化。
- `logback-spring.xml`：中文日志格式、滚动保存和 traceId 输出。
