# K·NOVA AI 知识库

完整的项目结构、架构设计、技术选型理由和演进路线见 [技术架构说明](docs/TECHNICAL_ARCHITECTURE.md)。

RAG 框架、向量数据库、检索方式和模型方案的对比见 [RAG 主流技术与方案差异](docs/RAG_TECHNOLOGY_COMPARISON.md)。

基于 Spring Boot、MyBatis-Plus、LangChain4j、Milvus、Embedding 与 Vue 3 的企业知识库。支持 JWT 登录、知识库管理、PDF/Office/TXT/Markdown 上传解析、文档切片向量化、语义检索、RAG 问答与来源引用。

后端采用 `Controller → Service → Mapper` 分层：Controller 只处理 HTTP 协议，Service 承担认证、校验、级联删除与 RAG 业务，Mapper 通过 MyBatis-Plus 访问 MySQL/H2。关系表由 `backend/src/main/resources/schema.sql` 初始化。

所有 REST 接口通过 `@ApiLog` 和统一 AOP 切面记录请求、响应、耗时及异常。日志默认输出到控制台和 `backend/logs/knova.log`，按日期及 50MB 大小滚动，保存 30 天；密码、Token 等字段会自动脱敏。

## 快速开始

1. 复制 `.env.example` 为 `.env`，填写模型服务的 API Key。
2. 确认已安装并启动 Ollama，然后执行 `ollama pull bge-m3`（默认 1024 维）。
3. 执行 `docker compose up --build`。
4. 浏览器访问 `http://localhost`，使用 `admin / admin123` 登录。

也可以分别开发：后端进入 `backend` 执行 `mvn spring-boot:run`（默认使用本地 H2 和文件型向量库），前端进入 `frontend` 执行 `npm install && npm run dev`。

## 环境配置

- 本地开发默认启用 `dev` Profile：使用 H2 和 `./data/local-vector-bge-m3.json` 文件型向量库，不需要启动 MySQL、etcd、MinIO 或 Milvus。
- 正式环境启用 `prod` Profile：使用 MySQL 和远程 Milvus。Docker Compose 已自动设置 `SPRING_PROFILES_ACTIVE=prod`。
- 手动启动正式配置可设置环境变量 `SPRING_PROFILES_ACTIVE=prod`，并配置 `DB_*`、`MILVUS_HOST`、`MILVUS_PORT`。

聊天与向量模型相互独立：DeepSeek `deepseek-v4-pro` 负责问答，本地 Ollama `bge-m3` 负责 Embedding（1024 维）。开发前先执行 `ollama pull bge-m3`；Docker 中通过 `host.docker.internal:11434` 访问宿主机 Ollama。

## 生产前必须调整

- 修改默认管理员密码、数据库密码和 `JWT_SECRET`。
- 将文档处理迁移到异步任务队列，避免大文件阻塞请求。
- 配置对象存储保存原始文件，并配置 HTTPS、限流、审计日志和数据备份。
- 若改用通义千问/vLLM 等 OpenAI 兼容服务，同时调整模型名、Base URL 与向量维度。
