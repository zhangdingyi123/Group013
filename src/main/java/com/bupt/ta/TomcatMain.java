package com.bupt.ta;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 轻量级 Servlet/JSP Web 应用：嵌入式 Tomcat 启动入口。
 * 数据仍存于 JSON 文本文件，无数据库、无 Spring Boot。
 */
public class TomcatMain {
    private static final int PORT = 8080;

    public static void main(String[] args) throws LifecycleException {
        Path webappDir = new File("src/main/webapp").toPath();
        if (!Files.isDirectory(webappDir)) {
            webappDir = new File("target/classes").toPath().getParent().resolve("src/main/webapp");
            if (!Files.isDirectory(webappDir)) {
                System.err.println("未找到 webapp 目录，请在项目根目录运行。");
                return;
            }
        }
        String docBase = webappDir.toAbsolutePath().toString();

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);
        tomcat.getConnector();
        tomcat.addWebapp("", docBase);

        tomcat.start();
        System.out.println("TA 招聘系统 - 轻量级 Servlet/JSP 应用");
        System.out.println("浏览器访问: http://localhost:" + PORT + "/");
        tomcat.getServer().await();
    }
}
