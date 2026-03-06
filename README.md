# TA 招聘系统

面向北邮国际学院助教（TA）招聘的简易招聘系统原型，供课程模块负责人及学校各类活动招募助教使用。

---

## 技术栈与约束

- **运行形态**：同时提供 **Java 桌面应用（Swing）** 与 **轻量级 Servlet/JSP Web 应用**（嵌入式 Tomcat），以及前后端分离（HttpServer + 静态页）、控制台版。
- **数据存储**：仅使用 **JSON 文本文件**（`data/` 目录），**不使用数据库**。
- **依赖**：JDK 11 + Gson + 嵌入式 Tomcat 9（无 Spring Boot）。

以上设计符合课程要求：聚焦软件工程核心原理、文本文件存储、禁止数据库与 Spring Boot。

---

## 快速开始

**环境**：JDK 11、Maven

```bash
# 编译
mvn compile
```

**推荐：Web 版（Servlet/JSP）**

```bash
mvn exec:java -Dexec.mainClass=com.bupt.ta.TomcatMain
```

浏览器访问 **http://localhost:8080/**，使用登录/注册页进入工作台。

---

## 运行方式

| 方式 | 入口类 | 说明 |
|------|--------|------|
| **Web（Servlet/JSP）** | `com.bupt.ta.TomcatMain` | 浏览器访问，登录/注册与工作台 |
| 桌面应用 | `com.bupt.ta.DesktopMain` | Swing 窗口，无需浏览器 |
| 前后端分离 | `com.bupt.ta.ServerMain` | HttpServer + 静态前端，API：`/api` |
| 控制台 | `com.bupt.ta.Main` | 纯命令行交互 |

- 前后端分离默认启动：`mvn exec:java -q`（主类为 `ServerMain`）。
- 若 8080 端口被占用，请先关闭占用该端口的进程再启动 Web 或 ServerMain。

---

## 数据存储

数据存放在项目根目录下的 `data/` 中（首次运行可自动创建）：

| 文件 | 说明 |
|------|------|
| `applicants.json` | 助教申请人资料 |
| `jobs.json` | 课程负责人发布的职位 |
| `applications.json` | 申请记录及状态（待处理/已录用/已拒绝） |
| `module_organisers.json` | 课程负责人账号 |

---

## 功能说明

- **助教（TA）**：注册/登录 → 维护个人资料、简历路径、技能 → 浏览职位、提交申请、查看申请状态。
- **课程负责人（MO）**：注册/登录 → 发布职位、管理我的职位、查看申请人并录用。
- **管理员（Admin）**：查看 TA 工作量（每人已分配职位数量）。

可选扩展：技能匹配、缺失技能提示、工作量均衡等（按课程规格说明）。

---

## Web 架构简述（Servlet/JSP）

- 嵌入式 Tomcat 9，Servlet 处理登录/注册与工作台请求，JSP 负责页面渲染，Session 维持登录状态。
- 登录/注册为独立页面，Tab 切换，居中卡片式布局。
- 与桌面版、控制台版共用同一套 `data/` 下 JSON 数据。
