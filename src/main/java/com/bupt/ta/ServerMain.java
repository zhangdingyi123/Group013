package com.bupt.ta;

import com.bupt.ta.api.ApiHandler;
import com.bupt.ta.service.ApplicantService;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.ModuleOrganiserService;
import com.bupt.ta.storage.Storage;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 前后端分离：启动 HTTP 服务，提供 REST API 与静态前端。
 * 仅使用 JDK HttpServer，不使用 Spring Boot。
 */
public class ServerMain {
    private static final int PORT = 8080;
    private static final String FRONTEND_DIR = "frontend";

    public static void main(String[] args) throws IOException {
        Storage storage = new Storage();
        ApplicantService applicantService = new ApplicantService(storage);
        JobService jobService = new JobService(storage);
        ApplicationService applicationService = new ApplicationService(storage);
        ModuleOrganiserService moService = new ModuleOrganiserService(storage);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api", new ApiHandler(applicantService, jobService, applicationService, moService));
        server.createContext("/", new StaticFileHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("TA 招聘系统 - 前后端分离");
        System.out.println("后端 API: http://localhost:" + PORT + "/api");
        System.out.println("前端页面: http://localhost:" + PORT + "/");
        System.out.println("数据目录: " + storage.getDataDir());
    }

    /** 静态文件：优先从项目根目录的 frontend 目录提供，便于开发。 */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCors(exchange);
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            Path base = Paths.get(FRONTEND_DIR).toAbsolutePath();
            Path file = base.resolve(path.replaceFirst("^/", "")).normalize();
            if (!file.startsWith(base) || !Files.isRegularFile(file)) {
                file = base.resolve("index.html");
            }
            if (!Files.exists(file)) {
                String msg = "Frontend not found. Put frontend files in ./" + FRONTEND_DIR + "/";
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                exchange.sendResponseHeaders(404, msg.getBytes().length);
                exchange.getResponseBody().write(msg.getBytes());
                return;
            }
            byte[] bytes = Files.readAllBytes(file);
            String contentType = contentType(path);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        }

        private void addCors(HttpExchange exchange) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        }

        private String contentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=UTF-8";
            if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (path.endsWith(".css")) return "text/css; charset=UTF-8";
            return "application/octet-stream";
        }
    }
}
