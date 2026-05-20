# 助教招聘系统 · 北京邮电大学国际学院

**Group 013 · EBU6304 Software Engineering**

基于 **Java Servlet / JSP** 的轻量级 Web 应用，用于助教申请、岗位发布与录用管理。数据以 **JSON 与文本文件** 持久化在应用目录下的 `data/`，**不依赖数据库**，便于课程演示与本地部署。

| 交付物 | 位置 |
|--------|------|
| **自述文件**（软件搭建、配置、运行） | [`自述文件.md`](自述文件.md) |
| 项目总览（本文件） | `README.md` |
| 用户手册（含界面截图） | `docs/UserManual.md` |
| 测试程序 | `test/java/`（JUnit）；说明见 `test/测试文档说明.md`、`test/测试用例说明.md` |
| 代码文档（JavaDoc） | 源码注释；生成：`mvn javadoc:javadoc` → `target/site/apidocs/` |

---

## 一、软件搭建

### 1.1 环境要求

| 组件 | 版本建议 | 说明 |
|------|----------|------|
| **JDK** | **11** 或以上 | 与 `pom.xml` 中 `maven.compiler.source/target` 一致 |
| **Apache Maven** | 3.6+ | 用于编译、打包、内嵌运行与单元测试 |
| **浏览器** | Chrome / Edge / Firefox | 推荐 Chrome |
| **Tomcat**（可选） | 9.x / 10.x | 独立部署 WAR 时使用，详见 [`docs/安装Tomcat.md`](docs/安装Tomcat.md) |

验证环境：

```bash
java -version    # 应显示 11 或更高
mvn -version
```

若 `java` 不可用，请安装 JDK 11 并配置 `JAVA_HOME`（macOS 可用 `brew install openjdk@11`）。

### 1.2 获取源码

```bash
# 克隆仓库后进入项目根目录（目录名以实际为准，例如「软工」）
cd /path/to/软工
```

项目为 Maven 标准结构：源码在 `src/main/java`、`src/main/webapp`，测试在 `test/java/`。

### 1.3 编译与打包

```bash
# 编译并生成 WAR（跳过测试可加 -DskipTests）
mvn clean package

# 运行单元测试（可选）
mvn test
```

| 产物 | 路径 |
|------|------|
| WAR 包 | `target/ta-recruitment.war` |
| 编译后的 class | `target/classes/`、`target/ta-recruitment/WEB-INF/classes/` |

**技术栈摘要**：Java 11、Servlet 4.0、JSP、JSTL、Gson（JSON）、Apache PDFBox / POI（简历 PDF/Word 正文抽取）。

---

## 二、配置说明

### 2.1 数据目录（必选，自动初始化）

应用启动时由 `AppListener` 将数据目录设为 **`{Web 应用根目录}/data`**：

| 场景 | `data/` 实际路径 |
|------|------------------|
| Maven 内嵌 Tomcat 开发 | `src/main/webapp/data/`（运行时会读写该目录） |
| 独立 Tomcat 部署 WAR | `webapps/ta-recruitment/data/` |

主要数据文件：

| 文件 / 目录 | 用途 |
|-------------|------|
| `applicants.json` | 应聘者 |
| `module_organisers.json` | 课程组织者 |
| `jobs.json` / `applications.json` | 岗位与申请 |
| `admins.json` | 管理员 |
| `messages.json`、`dm_read_states.json` | 站内信与已读状态 |
| `forum_threads.json`、`forum_replies.json` | 论坛 |
| `friend_links.json`、`friend_requests.json` | 好友 |
| `data/resumes/` | 简历文本 |
| `embeddings.json`（可选） | 语义匹配向量缓存 |

密码经 **SHA-256** 哈希后存入 JSON。**请勿将含真实密钥或生产数据的 `data/` 提交到公开仓库。**

### 2.2 智能小助手（可选）

配置文件默认位于 **`src/main/resources/assistant.properties`**（打包后位于 `WEB-INF/classes/`）。

| 配置方式 | 说明 |
|----------|------|
| 环境变量 | `KIMI_API_KEY`、`QWEN_API_KEY`、`OPENAI_API_KEY` 等（**优先于文件**） |
| 外部文件 | `ASSISTANT_PROPERTIES_PATH` 或 JVM 参数 `-Dassistant.properties.path=/绝对路径/assistant.properties` |

未配置任何 API Key 时，小助手页面会提示未就绪，**不影响**注册、岗位、申请等核心招聘功能。

常用可选项（文件内键名或对应环境变量）：

| 功能 | 配置要点 |
|------|----------|
| 默认模型提供方 | `assistant.default.provider=kimi\|qwen\|openai` |
| 提问范围限制 | 默认开启；关闭：`ASSISTANT_STRICT_SCOPE=0` 或 `assistant.strict.scope=false` |
| 每月免费次数 | `assistant.monthly.free.quota`（默认 30） |
| 兑换码充值 | `assistant.topup.code` / `ASSISTANT_TOPUP_CODE` |
| 微信 Native 扫码 | `assistant.pay.wechat.*` 系列（需公网 HTTPS 回调），详见 `assistant.properties` 内注释 |
| **语义匹配** | `match.semantic.enabled=true`（默认 **关闭**）；开启后会将文本发往第三方 embeddings API |

### 2.3 界面语言

由 `LocaleFilter` 与 Cookie `ui_lang` 控制；页眉切换语言，或访问 `?lang=zh` / `?lang=en`。

### 2.4 端口与上下文路径（部署相关）

| 运行方式 | 默认访问地址 | 上下文路径 |
|----------|--------------|------------|
| **`mvn tomcat7:run`**（开发推荐） | `http://localhost:8082/` | `/`（根路径） |
| **独立 Tomcat + WAR** | `http://localhost:8080/ta-recruitment/` | `/ta-recruitment/` |

内嵌 Tomcat 默认端口在 `pom.xml` 的 `maven.tomcat.port`（当前 **8082**）。端口占用时可指定：

```bash
mvn tomcat7:run -Dmaven.tomcat.port=8083
```

---

## 三、运行说明

### 3.1 方式 A：Maven 内嵌 Tomcat（推荐，无需安装 Tomcat）

```bash
cd /path/to/软工

# 若尚未打包，先执行一次
mvn clean package

# 启动 Web 应用（保持终端运行，Ctrl+C 停止）
mvn tomcat7:run
```

浏览器打开：**http://localhost:8082/**

### 3.2 方式 B：独立 Tomcat 部署 WAR

1. 安装 Tomcat（见 [`docs/安装Tomcat.md`](docs/安装Tomcat.md)）。
2. 将 `target/ta-recruitment.war` 复制到 Tomcat 的 `webapps/`。
3. 启动 Tomcat（`bin/startup.sh` 或 Windows 的 `startup.bat`）。
4. 访问：**http://localhost:8080/ta-recruitment/**（端口以 `server.xml` 为准）。

**升级部署建议**：停止 Tomcat → 删除旧 `webapps/ta-recruitment*` 及 `work/Catalina/localhost/ta-recruitment/` 缓存 → 放入新 WAR → 再启动，避免 JSP/class 版本混用。

### 3.3 演示账号

| 角色 | 邮箱 | 密码 |
|------|------|------|
| 应聘者 (TA) | `liuchen@bupt-demo.edu.cn` | `demo123` |
| 课程组织者 (MO) | `li.prof@bupt-demo.edu.cn` | `demo123` |
| 管理员 | `admin@bupt-demo.edu.cn` | `admin123` |

登录入口：首页 → **个人中心**（`/personal-center`），或各角色认证页 `/ta/auth`、`/mo/auth`、`/admin/auth`。

### 3.4 主要 URL

| 路径 | 说明 |
|------|------|
| `/` | 首页 |
| `/personal-center` | 个人中心（按角色跳转） |
| `/ta/dashboard`、`/mo/dashboard` | TA / MO 工作台 |
| `/mo/job-applicants` | 岗位应聘者与匹配度 |
| `/admin/workload` | 管理员工作负荷 |
| `/forum` | 交流论坛 |
| `/assistant` | 智能小助手 |

### 3.5 常见问题

| 现象 | 处理 |
|------|------|
| 端口 8082 被占用 | `mvn tomcat7:run -Dmaven.tomcat.port=8083` |
| `mvn` 找不到 | 安装 Maven 并确保在 `PATH` 中 |
| 独立 Tomcat 404 | 确认 WAR 已解压、URL 含 `/ta-recruitment/` |
| JSP 500 / 行为异常 | 按上文清理 `work/` 后整包重新部署 |
| 小助手无响应 | 检查 API Key 与网络；未配置时属预期，核心功能仍可用 |

### 3.6 测试与用户手册截图（可选）

```bash
# 终端 1
mvn tomcat7:run

# 终端 2（需 Python 3）
python3 docs/capture_ui_screenshots.py
bash docs/prepare_user_manual_images.sh
```

自动化单元测试：`mvn test`（不依赖 Tomcat 与外部 AI）。手工用例见 `test/测试用例说明.md`。

---

## 功能概览

### 应聘者（TA）

- 注册、登录与个人资料（姓名、学号、技能标签等）
- 简历：上传纯文本，或从小助手 / 页面上传 PDF、Word 抽取正文
- 浏览岗位、提交申请、查看状态；工作台分栏（资料、申请、站内信等）
- **交流论坛**、**站内信**、**好友**与私信（含已读状态）

### 课程组织者（MO）

- 发布与管理岗位（课程助教 / 监考 / 活动支持等）
- 按岗位查看应聘者：**技能匹配度**、**技能短板**、面试 / 录用 / 拒绝、关闭岗位

### 管理员

- `/admin/auth` 登录后访问 `/admin/workload`：助教录用负荷统计、展开明细、**转移录用**以均衡分工

### 跨角色

- **智能小助手**（`/assistant`）：对话、简历抽取；REST：`/api/assistant/chat`、`/api/assistant/extract-resume`
- 岗位匹配默认基于 **技能关键词规则**；可选 **embeddings 语义相似度**（见配置 2.2）

---

## 源码结构（摘要）

```
src/main/java/com/bupt/ta/
  model/          # 领域模型
  storage/        # JSON 读写
  service/        # 业务逻辑（含 assistant 子包）
  util/           # 密码、会话、向量等
  web/            # Servlet、Filter、Listener
src/main/webapp/
  index.jsp, ta/, mo/, admin/, forum/, css/
  data/           # 运行时数据
  WEB-INF/web.xml
test/java/com/bupt/ta/   # JUnit 5 单元测试
```

---

## 生成 JavaDoc

```bash
mvn javadoc:javadoc
```

浏览器打开 `target/site/apidocs/index.html`。

---

## 说明与限制

- 本系统面向教学与原型演示；生产环境需补充 HTTPS、审计、备份与细粒度权限等。
- 管理员与各角色权限以会话与页面逻辑为准；数据为明文 JSON，请妥善保管部署目录。
