# 助教招聘系统 · 北京邮电大学国际学院

轻量级 Java Servlet/JSP Web 应用，用于简化助教申请与招聘流程。**所有数据存储为 JSON 与文本文件，不使用数据库。**

## 功能概览

### 应聘者（TA）
- 注册/登录、创建个人申请档案（姓名、学号、技能标签）
- 上传简历（纯文本，保存为 `.txt`）
- 查询可申请岗位、提交岗位申请、查询申请状态

### 课程组织者（MO）
- 注册/登录
- 发布招聘岗位（课程助教 / 监考 / 活动支持，可填所需技能）
- 按岗位筛选应聘者，查看**技能匹配度**与**技能短板**
- 录用/拒绝申请；关闭岗位

### 管理员
- 查看助教整体工作负荷（每人已被录用的岗位数）

### 辅助逻辑（规则实现，非外部 AI）
- **岗位与应聘者技能匹配**：按岗位所需技能与应聘者技能计算匹配分
- **技能短板**：列出岗位需要但应聘者未填写的技能
- **负荷均衡**：推荐应聘者时在同等匹配度下优先推荐当前录用数较少者

## 技术栈

- Java 11、Maven
- Servlet 4.0、JSP、JSTL
- Gson（JSON 读写）
- 数据目录：`{应用根}/data/`，内含 `applicants.json`、`jobs.json`、`applications.json`、`module_organisers.json`，简历存放在 `data/resumes/*.txt`

## 构建与运行

### 方式一：生成 WAR 并部署到 Tomcat

```bash
mvn clean package
```

在项目目录下会生成 **`target/ta-recruitment.war`**。将其拷贝到 Tomcat 的 `webapps/` 目录，启动 Tomcat 后访问：

- **http://localhost:8081/ta-recruitment/**

（端口以实际 Tomcat 配置为准。）

### 方式二：使用 Maven Tomcat 插件直接运行（无需单独安装 Tomcat）

```bash
mvn tomcat7:run
```

启动后浏览器访问：

- **http://localhost:8081/ta-recruitment/**

（插件默认端口为 8081，上下文路径为 `/ta-recruitment`，可在 `pom.xml` 中修改。）

### 方式三：在 IDE 中运行

- **IDEA**：可配置 Tomcat 运行配置，将部署目标设为 `ta-recruitment:war exploded` 或已打包的 `target/ta-recruitment.war`。
- **Eclipse**：可安装 Tomcat 插件并将项目以 Dynamic Web Project 方式部署并运行。

进入首页后，可选择：应聘者入口、课程组织者入口、管理员工作负荷。

## 项目结构

```
src/main/java/com/bupt/ta/
  model/          # Applicant, Job, Application, ModuleOrganiser
  storage/        # Storage（JSON 文件读写）
  service/        # ApplicantService, JobService, ApplicationService, ModuleOrganiserService, MatchHelper
  util/           # PasswordUtil（SHA-256）
  web/            # HomeServlet, TAAuthServlet, TADashboardServlet, MOAuthServlet, MODashboardServlet, AdminServlet, AppListener
src/main/webapp/
  index.jsp       # 首页
  ta/             # 应聘者登录、注册、工作台
  mo/             # 课程组织者登录、注册、工作台
  admin/          # 工作负荷页
  css/style.css
  WEB-INF/web.xml
```

## 说明

- 管理员工作负荷页未做登录校验，仅作原型演示；生产环境需增加权限控制。
- 密码以 SHA-256 哈希后存储，简历与业务数据均为文本/JSON，便于在无数据库环境下运行与演示。
