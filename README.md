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
- 查看助教整体工作负荷：**录用岗位数、人均/极值、相对人均偏高/偏低提示**
- 展开查看每位助教的**具体录用岗位**（名称、类型、课程组织者）
- **转移录用**：将某条已录用申请改挂到其他已注册助教（用于均衡分工，需业务上已沟通）

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

- **http://localhost:8082/ta-recruitment/**

（端口以实际 Tomcat 配置为准。）

#### Tomcat 全量部署流程（避免 JSP 与 class 版本不一致）

以下流程适用于**任意一次**需要可靠更新的场景（例如改过 `AdminServlet`、新增 `com.bupt.ta.web.view` 下的类、或修改 `admin/workload.jsp`）。原则是：**整包替换 + 清 JSP 编译缓存**，不要只拷单个 `.class` 或单个 `.jsp`。

**环境约定**

- `CATALINA_HOME`：Tomcat 安装目录（例如 macOS 上 `/usr/local/apache-tomcat-7.0.47` 或你解压的路径）。
- 应用上下文名：默认与 WAR 文件名一致，即 **`ta-recruitment`**（访问路径 `/ta-recruitment/`）。

**步骤 1：本地完整构建**

在项目根目录执行（需已安装 **JDK 11** 与 **Maven**）：

```bash
cd /path/to/软工   # 换成你的项目根目录
mvn clean package -DskipTests
```

- `clean` 会删掉 `target/`，避免旧 class 混进新包。
- 成功后得到 **`target/ta-recruitment.war`**。

**步骤 2：确认 WAR 内关键内容（可选但推荐）**

解压或 `jar tf target/ta-recruitment.war | grep` 检查是否存在例如：

- `WEB-INF/classes/com/bupt/ta/web/AdminServlet.class`
- `WEB-INF/classes/com/bupt/ta/web/view/WorkloadEntryView.class`
- `WEB-INF/classes/com/bupt/ta/web/view/WorkloadAssignmentView.class`
- `WEB-INF/classes/com/bupt/ta/web/view/ApplicantOptionView.class`
- `admin/workload.jsp`

快速列出 view 包：

```bash
jar tf target/ta-recruitment.war | grep 'web/view/'
```

**步骤 3：停止 Tomcat**

```bash
$CATALINA_HOME/bin/shutdown.sh
```

（Windows 使用 `shutdown.bat`。）确认进程已退出后再继续。

**步骤 4：删除旧应用与 JSP 编译缓存**

仍假设应用名为 `ta-recruitment`：

1. 删除已部署应用（二选一或都做，保证没有旧文件残留）  
   - 删除 **`$CATALINA_HOME/webapps/ta-recruitment/`** 整个目录（exploded 部署时）。  
   - 删除 **`$CATALINA_HOME/webapps/ta-recruitment.war`**（若使用 WAR 部署；Tomcat 启动时会再解压，删 WAR 可避免旧包被再次展开）。

2. **清空本应用在 Tomcat 的 work 缓存**（让 JSP 重新编译，避免沿用旧的 `workload_jsp.class`）：  
   - 删除目录 **`$CATALINA_HOME/work/Catalina/localhost/ta-recruitment/`**  
   - 若 `localhost` 下没有该名，可在 **`$CATALINA_HOME/work/`** 下搜索 `ta-recruitment` 相关子目录并整目录删除。

**步骤 5：部署新 WAR**

将 **`target/ta-recruitment.war`** 拷贝到 **`$CATALINA_HOME/webapps/`**。

（若你使用 **exploded** 部署：将 `mvn clean package` 生成的 **`target/ta-recruitment/`** 整个目录拷到 `webapps/ta-recruitment/`，并同样保证步骤 4 已删掉旧目录与 work 缓存。）

**步骤 6：启动 Tomcat**

```bash
$CATALINA_HOME/bin/startup.sh
```

**步骤 7：验证**

浏览器访问（端口以 `server.xml` 中 `Connector` 为准，默认常为 **8080**）：

- `http://localhost:8080/ta-recruitment/admin/workload`（需先以管理员登录）

若仍出现与 JSP 相关的 500，再次确认：**work 目录下是否已无旧的 `ta-recruitment` 缓存**，并已使用 **`mvn clean package`** 生成的 WAR。

**IDEA / Eclipse 提示**：若用「Exploded」热部署，请在重大类结构变更后执行一次 **Rebuild + 重新部署整个 artifact**，或改用手动 **WAR 全量替换** 流程 above，避免只同步了部分 class。

### 方式二：使用 Maven Tomcat 插件直接运行（无需单独安装 Tomcat）

```bash
mvn tomcat7:run
```

启动后浏览器访问：

- **http://localhost:8082/ta-recruitment/**（若改了端口，请把 URL 里的端口一并改掉）

**若启动失败：**

- **`Address already in use … 8082`**：本机已有进程占用 8082（例如上一次 `tomcat7:run` 未退出）。结束该进程，或换端口：`mvn tomcat7:run -Dmaven.tomcat.port=8083`。
- **`Exception starting filter … NoStoreSensitivePagesFilter` / `AbstractMethodError` … `init(FilterConfig)`**：请拉取最新代码；过滤器需实现 `init` / `destroy` 空方法以兼容内嵌 Tomcat 7。

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
  ta/             # 应聘者登录、注册；工作台分 profile / applications / my-resume 三页（均由 TADashboardServlet 按 tab 转发）
  mo/             # 课程组织者登录、注册、工作台
  admin/          # 工作负荷页
  css/style.css
  WEB-INF/web.xml
```

## 说明

- 管理员工作负荷页未做登录校验，仅作原型演示；生产环境需增加权限控制。
- 密码以 SHA-256 哈希后存储，简历与业务数据均为文本/JSON，便于在无数据库环境下运行与演示。
