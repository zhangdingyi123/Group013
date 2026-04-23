# 助教招聘系统 · 北京邮电大学国际学院

基于 **Java Servlet / JSP** 的轻量级 Web 应用，用于助教申请、岗位发布与录用管理。数据以 **JSON 与文本文件** 持久化在应用目录下的 `data/`，**不依赖数据库**，便于课程演示与本地部署。

---

## 功能概览

### 应聘者（TA）

- 注册、登录与个人资料（姓名、学号、技能标签等）
- 简历：上传纯文本，或借助小助手从 PDF / Word 抽取正文后保存
- 浏览岗位、提交申请、查看申请状态
- 工作台：资料、申请、站内信等分栏入口
- **交流论坛**：浏览、发帖、回复（需登录）
- **站内信** 与 **好友**：与其他用户建立好友关系并私信（含已读状态）

### 课程组织者（MO）

- 注册、登录
- 发布与管理岗位（课程助教 / 监考 / 活动支持等，可填写所需技能）
- 按岗位查看应聘者列表：**技能匹配度**、**技能短板**、录用 / 拒绝、关闭岗位
- 论坛与站内信、好友（与 TA 侧一致的数据模型）

### 管理员

- 通过 **`/admin/auth`** 登录（会话校验）
- **`/admin/workload`**：查看助教整体工作负荷（录用数、人均与极值、相对偏高 / 偏低提示），展开每位助教的具体录用岗位，**转移录用**以均衡分工（业务上需事先沟通）
- 未登录访问工作负荷页会重定向到管理员登录页

### 跨角色与首页

- **`/personal-center`**：个人中心入口；已登录则按角色跳转到 TA / MO / 管理员对应页面，否则进入身份选择页
- **智能小助手**（`/assistant`）：可选接入大模型 API（见下文），支持对话与简历相关能力；REST：`/api/assistant/chat`、`/api/assistant/extract-resume`

### 业务规则（内置逻辑，非外部 AI）


---

## 技术栈

| 类别 | 说明 |
|------|------|
| 运行环境 | Java 11、Maven |
| Web | Servlet 4.0、JSP、JSTL |
| JSON | Gson |
| 简历正文 | Apache PDFBox、Apache POI（PDF / Word 文本抽取） |
| 打包 | `war`，默认产物名 `ta-recruitment.war` |

---

## 数据存储

应用启动时通过 `AppListener` 将数据目录设为 **`{Web 应用根目录}/data`**（与部署上下文一致）。

| 文件 / 目录 | 用途 |
|-------------|------|
| `applicants.json` | 应聘者 |
| `module_organisers.json` | 课程组织者 |
| `admins.json` | 管理员账号 |
| `jobs.json` | 岗位 |
| `applications.json` | 申请与录用状态 |
| `messages.json` | 站内信 |
| `dm_read_states.json` | 私信已读状态 |
| `forum_threads.json` / `forum_replies.json` | 论坛帖子与回复 |
| `friend_links.json` / `friend_requests.json` | 好友关系与好友请求 |
| `data/resumes/` | 简历文本文件 |

密码经 **SHA-256** 哈希后存入 JSON；请勿将含真实密钥或生产数据的 `data/` 提交到公开仓库。

---

## 智能小助手（可选）

小助手相关配置由 `AssistantConfig` 读取，支持 **classpath 内的 `assistant.properties`**，或通过环境变量 **`ASSISTANT_PROPERTIES_PATH`** / JVM 参数 **`-Dassistant.properties.path=`** 指向外部配置文件；**环境变量可覆盖** 文件中的密钥类配置。

可对接 **Moonshot Kimi**、**阿里云通义（OpenAI 兼容）**、**OpenAI** 等（具体模型与 Base URL 以配置为准）。未配置任何 API Key 时，页面会提示未就绪，不影响其余招聘功能。

为避免跑题，小助手默认开启“提问范围严格限制”：服务端会在调用上游大模型前先判定主题，若明显与助教招聘系统/应聘无关则直接返回固定拒绝话术。可通过环境变量 `ASSISTANT_STRICT_SCOPE=0` 或配置 `assistant.strict.scope=false` 关闭。

### 额度（可选）

小助手支持“按用户每月免费次数 + 付费点数”的简单额度机制：

- 免费额度：环境变量 `ASSISTANT_MONTHLY_FREE_QUOTA` 或配置 `assistant.monthly.free.quota`（默认 30 次/月）
- 付费点数：当免费额度用尽后，若该用户仍有付费点数则继续扣点；否则接口返回 **402**（Payment Required）
- 查询额度：`GET /api/assistant/quota`（未登录时按 session 粗略统计）
- **微信 Native 扫码充值（推荐）**：配置齐全时助手页展示「生成微信收款码」，无需兑换码。需在微信商户平台开通 Native，并配置公网 HTTPS 回调地址，与 `WECHAT_PAY_NOTIFY_URL` / `assistant.pay.wechat.notify.url` 一致；详见 `assistant.properties` 内注释。接口：`POST /api/assistant/pay/wechat/native`（下单）、`GET /api/assistant/pay/wechat/order?outTradeNo=`（轮询状态）、`POST /api/assistant/pay/wechat/notify`（微信服务器回调，勿浏览器访问）。
- 充值点数（兑换码模式，需登录，在未配置微信时可用）：`POST /api/assistant/quota/topup`，并配置 `ASSISTANT_TOPUP_CODE` 或 `assistant.topup.code`
- 可选提示：`ASSISTANT_PAY_HINT` 或 `assistant.pay.hint`（用于前端展示“付费/充值引导”文案）

### 语义匹配（可选）

默认的岗位/简历匹配是基于技能关键词的规则打分；也可开启 **embeddings 向量相似度** 作为匹配分来源：

- 在 `assistant.properties` 设置 `match.semantic.enabled=true`（或环境变量 `MATCH_SEMANTIC_ENABLED=1`）
- 可选指定 `match.semantic.provider=kimi|qwen|openai`（或 `MATCH_SEMANTIC_PROVIDER`），否则跟随 `assistant.default.provider`
- embeddings 接口与模型可用 `*.embeddings.url` / `*.embedding.model` 配置（不填会尝试由 `*.chat.completions.url` 推断 `/embeddings`）
- 首次计算会调用外部 embeddings API，并把向量缓存到 `data/embeddings.json`（避免每次请求都调用外部接口）

注意：开启后会将简历/岗位文本发送给第三方大模型服务以生成向量，请按隐私合规要求使用。

---

## 构建与运行

### 1. 打包 WAR

```bash
mvn clean package
```

产物：`target/ta-recruitment.war`。将 WAR 放入 Tomcat 的 `webapps/`，默认访问路径为：

- **`http://{主机}:{端口}/ta-recruitment/`**（端口以 Tomcat `server.xml` 为准，常见为 `8080`）

### 2. Maven 内嵌 Tomcat（开发常用）

```bash
mvn tomcat7:run
```

当前 `pom.xml` 将插件上下文设为 **`/`**、默认端口 **`8082`**，因此本地根地址为：

- **`http://localhost:8082/`**

若端口被占用，可指定例如：

```bash
mvn tomcat7:run -Dmaven.tomcat.port=8083
```

**注意**：内嵌运行与独立 Tomcat 部署的 **上下文路径不同**（`/` 与 `/ta-recruitment/`），书签与说明中的链接需按实际环境替换。



## 主要 URL 速查

| 路径 | 说明 |
|------|------|
| `/` | 首页 |
| `/personal-center` | 个人中心入口 |
| `/ta/auth`、`/ta/dashboard`、`/ta/profile` | 应聘者认证与工作台 |
| `/mo/auth`、`/mo/dashboard`、`/mo/profile` | 课程组织者认证与工作台 |
| `/mo/job-applicants` | 岗位应聘者管理 |
| `/admin/auth`、`/admin/workload` | 管理员登录与工作负荷 |
| `/forum` | 交流论坛 |
| `/assistant` | 智能小助手页面 |

其余静态页面与分栏 JSP 由对应 Servlet 转发，以项目 `src/main/webapp` 为准。

---

## 源码结构（摘要）

```
src/main/java/com/bupt/ta/
  model/          # 领域模型
  storage/        # Storage：JSON 读写与路径
  service/        # 业务服务（含 assistant 子包）
  util/           # 密码、会话等工具
  web/            # Servlet、Listener、Filter
src/main/webapp/
  index.jsp, personal_center_gate.jsp, assistant.jsp, …
  ta/, mo/, admin/, forum/, css/
  data/           # 运行时数据（部署后位于应用根下）
  WEB-INF/web.xml
```

---

## 独立 Tomcat 全量更新建议

升级或排查 JSP 500 时，建议 **整包替换** 并 **清理本应用在 `work/` 下的 JSP 编译缓存**，避免只拷贝单个 class 或单个 JSP 导致版本混用。步骤概要：

1. `mvn clean package -DskipTests`
2. 停止 Tomcat
3. 删除 `webapps/ta-recruitment/`、`webapps/ta-recruitment.war`（按你实际部署方式）
4. 删除 `work/Catalina/localhost/ta-recruitment/`（或实际上下文名对应目录）
5. 拷贝新 WAR 到 `webapps/`，启动 Tomcat

---

## 说明与限制

- 本系统面向教学与原型演示；生产环境需补充审计、HTTPS、备份与更细粒度权限等。
- 管理员与各角色权限以会话与页面逻辑为准；数据文件为明文 JSON，请妥善保管部署目录。
