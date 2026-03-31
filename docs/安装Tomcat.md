# 安装 Tomcat（用于部署 ta-recruitment.war）

任选一种方式即可。

---

## 方式一：用 Homebrew 安装（推荐）

若之前安装失败，先修复 Homebrew 目录权限（在终端执行，需输入密码）：

```bash
sudo chown -R $(whoami) /usr/local/Cellar /usr/local/Frameworks /usr/local/Homebrew /usr/local/bin /usr/local/etc /usr/local/include /usr/local/lib /usr/local/opt /usr/local/sbin /usr/local/share /usr/local/var
```

然后安装 Tomcat：

```bash
brew install tomcat
```

安装完成后：

- **Tomcat 目录**：`/usr/local/opt/tomcat`（或 `$(brew --prefix)/opt/tomcat`）
- **webapps 目录**：`/usr/local/opt/tomcat/libexec/webapps/`
- **启动**：`/usr/local/opt/tomcat/bin/startup.sh`
- **停止**：`/usr/local/opt/tomcat/bin/shutdown.sh`

部署本项目的 WAR：

```bash
cp /Users/dingyi.zhang/Desktop/软工/target/ta-recruitment.war /usr/local/opt/tomcat/libexec/webapps/
```

再执行 `startup.sh`，浏览器访问：**http://localhost:8080/ta-recruitment/**

---

## 方式二：手动下载安装（无需 Homebrew）

1. **下载 Tomcat**
   - 打开：https://tomcat.apache.org/download-10.cgi（或 9.x）
   - 在 “Binary Distributions” 里下载 **Core** 的 **zip**（如 `apache-tomcat-10.1.x.zip`）

2. **解压**
   - 解压到任意目录，例如：`~/Applications/apache-tomcat-10.1.24`
   - 记下这个路径，下面叫 `TOMCAT_HOME`

3. **部署 WAR**
   - 把项目的 `target/ta-recruitment.war` 复制到 `TOMCAT_HOME/webapps/`

4. **启动 Tomcat**
   - **Mac/Linux**：在终端执行  
     `TOMCAT_HOME/bin/startup.sh`  
     或  
     `sh /Users/你的用户名/Applications/apache-tomcat-10.1.24/bin/startup.sh`
   - **Windows**：双击 `TOMCAT_HOME\bin\startup.bat`

5. **访问应用**  
   浏览器打开：**http://localhost:8080/ta-recruitment/**

6. **停止 Tomcat**
   - **Mac/Linux**：`TOMCAT_HOME/bin/shutdown.sh`
   - **Windows**：双击 `shutdown.bat`

---

## 不装 Tomcat 也可以运行

本项目已配置 Maven Tomcat 插件，不安装 Tomcat 也能直接运行：

```bash
cd /Users/dingyi.zhang/Desktop/软工
mvn tomcat7:run
```

然后访问：**http://localhost:8080/ta-recruitment/**
