package com.bupt.ta.api;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.ModuleOrganiser;
import com.bupt.ta.service.ApplicantService;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.ModuleOrganiserService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * REST API 处理器，前后端分离：仅返回 JSON，不使用 Spring Boot。
 */
public class ApiHandler implements HttpHandler {
    private static final Gson GSON = new Gson();
    private final ApplicantService applicantService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final ModuleOrganiserService moService;

    public ApiHandler(ApplicantService applicantService, JobService jobService,
                      ApplicationService applicationService, ModuleOrganiserService moService) {
        this.applicantService = applicantService;
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.moService = moService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCors(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.startsWith("/api/applicants")) {
                handleApplicants(exchange, path, method);
            } else if (path.startsWith("/api/jobs")) {
                handleJobs(exchange, path, method);
            } else if (path.startsWith("/api/applications")) {
                handleApplications(exchange, path, method);
            } else if (path.startsWith("/api/mo")) {
                handleMo(exchange, path, method);
            } else if (path.startsWith("/api/admin/workload")) {
                handleAdminWorkload(exchange);
            } else {
                sendJson(exchange, 404, Map.of("error", "Not Found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, Map.of("error", e.getMessage() != null ? e.getMessage() : "Internal Error"));
        }
    }

    private void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PATCH, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private void handleApplicants(HttpExchange exchange, String path, String method) throws IOException {
        if (path.equals("/api/applicants/register") && "POST".equals(method)) {
            Map<String, String> body = readJson(exchange, new TypeToken<Map<String, String>>() {}.getType());
            String name = body.get("name");
            String email = body.get("email");
            if (name == null || email == null) {
                sendJson(exchange, 400, Map.of("error", "name and email required"));
                return;
            }
            if (applicantService.findByEmail(email).isPresent()) {
                sendJson(exchange, 400, Map.of("error", "Email already registered"));
                return;
            }
            Applicant a = applicantService.create(name, email);
            sendJson(exchange, 200, a);
            return;
        }
        if (path.equals("/api/applicants/login") && "POST".equals(method)) {
            Map<String, String> body = readJson(exchange, new TypeToken<Map<String, String>>() {}.getType());
            String email = body.get("email");
            Applicant a = applicantService.findByEmail(email != null ? email : "").orElse(null);
            if (a == null) {
                sendJson(exchange, 404, Map.of("error", "Applicant not found"));
                return;
            }
            sendJson(exchange, 200, a);
            return;
        }
        if (path.matches("/api/applicants/[^/]+") && "GET".equals(method)) {
            String id = path.substring("/api/applicants/".length());
            var opt = applicantService.findById(id);
            if (opt.isPresent()) {
                sendJson(exchange, 200, opt.get());
            } else {
                sendJson(exchange, 404, Map.of("error", "Not found"));
            }
            return;
        }
        if (path.matches("/api/applicants/[^/]+") && "PATCH".equals(method)) {
            String id = path.substring("/api/applicants/".length());
            Map<String, Object> body = readJson(exchange, new TypeToken<Map<String, Object>>() {}.getType());
            Applicant a = applicantService.findById(id).orElse(null);
            if (a == null) {
                sendJson(exchange, 404, Map.of("error", "Not found"));
                return;
            }
            if (body.containsKey("cvPath")) a.setCvPath((String) body.get("cvPath"));
            if (body.containsKey("skills")) {
                List<String> skills = (List<String>) body.get("skills");
                a.setSkills(skills != null ? skills : new ArrayList<>());
            }
            applicantService.save(a);
            sendJson(exchange, 200, a);
            return;
        }
        sendJson(exchange, 404, Map.of("error", "Not Found"));
    }

    private void handleJobs(HttpExchange exchange, String path, String method) throws IOException {
        if (path.equals("/api/jobs/open") && "GET".equals(method)) {
            sendJson(exchange, 200, jobService.findOpen());
            return;
        }
        if (path.equals("/api/jobs") && "GET".equals(method)) {
            String moId = queryParam(exchange, "moId");
            if (moId != null && !moId.isEmpty()) {
                List<Job> my = new ArrayList<>();
                for (Job j : jobService.findAll())
                    if (moId.equals(j.getMoId())) my.add(j);
                sendJson(exchange, 200, my);
            } else {
                sendJson(exchange, 200, jobService.findAll());
            }
            return;
        }
        if (path.equals("/api/jobs") && "POST".equals(method)) {
            Map<String, Object> body = readJson(exchange, new TypeToken<Map<String, Object>>() {}.getType());
            String title = (String) body.get("title");
            String moduleCode = (String) body.get("moduleCode");
            String moId = (String) body.get("moId");
            if (title == null || moduleCode == null || moId == null) {
                sendJson(exchange, 400, Map.of("error", "title, moduleCode, moId required"));
                return;
            }
            List<String> skills = body.containsKey("requiredSkills") ? (List<String>) body.get("requiredSkills") : null;
            Job j = jobService.create(title, moduleCode, moId, skills);
            sendJson(exchange, 200, j);
            return;
        }
        if (path.matches("/api/jobs/[^/]+") && "GET".equals(method)) {
            String id = path.substring("/api/jobs/".length());
            var opt = jobService.findById(id);
            if (opt.isPresent()) {
                sendJson(exchange, 200, opt.get());
            } else {
                sendJson(exchange, 404, Map.of("error", "Not found"));
            }
            return;
        }
        sendJson(exchange, 404, Map.of("error", "Not Found"));
    }

    private void handleApplications(HttpExchange exchange, String path, String method) throws IOException {
        if (path.equals("/api/applications") && "GET".equals(method)) {
            String applicantId = queryParam(exchange, "applicantId");
            String jobId = queryParam(exchange, "jobId");
            if (applicantId != null) {
                sendJson(exchange, 200, applicationService.findByApplicant(applicantId));
                return;
            }
            if (jobId != null) {
                sendJson(exchange, 200, applicationService.findByJob(jobId));
                return;
            }
            sendJson(exchange, 200, applicationService.findAll());
            return;
        }
        if (path.equals("/api/applications") && "POST".equals(method)) {
            Map<String, String> body = readJson(exchange, new TypeToken<Map<String, String>>() {}.getType());
            String applicantId = body.get("applicantId");
            String jobId = body.get("jobId");
            if (applicantId == null || jobId == null) {
                sendJson(exchange, 400, Map.of("error", "applicantId and jobId required"));
                return;
            }
            Application app = applicationService.apply(applicantId, jobId);
            sendJson(exchange, 200, app);
            return;
        }
        if (path.matches("/api/applications/[^/]+/select") && "POST".equals(method)) {
            String id = path.replace("/api/applications/", "").replace("/select", "");
            applicationService.selectApplicant(id);
            sendJson(exchange, 200, Map.of("ok", true));
            return;
        }
        if (path.matches("/api/applications/[^/]+/reject") && "POST".equals(method)) {
            String id = path.replace("/api/applications/", "").replace("/reject", "");
            applicationService.rejectApplicant(id);
            sendJson(exchange, 200, Map.of("ok", true));
            return;
        }
        sendJson(exchange, 404, Map.of("error", "Not Found"));
    }

    private void handleMo(HttpExchange exchange, String path, String method) throws IOException {
        if (path.equals("/api/mo/register") && "POST".equals(method)) {
            Map<String, String> body = readJson(exchange, new TypeToken<Map<String, String>>() {}.getType());
            String name = body.get("name");
            String email = body.get("email");
            if (name == null || email == null) {
                sendJson(exchange, 400, Map.of("error", "name and email required"));
                return;
            }
            ModuleOrganiser mo = moService.create(name, email);
            sendJson(exchange, 200, mo);
            return;
        }
        if (path.equals("/api/mo/login") && "POST".equals(method)) {
            Map<String, String> body = readJson(exchange, new TypeToken<Map<String, String>>() {}.getType());
            String email = body.get("email");
            ModuleOrganiser mo = moService.findAll().stream()
                    .filter(m -> email != null && email.equalsIgnoreCase(m.getEmail()))
                    .findFirst().orElse(null);
            if (mo == null) {
                sendJson(exchange, 404, Map.of("error", "MO not found"));
                return;
            }
            sendJson(exchange, 200, mo);
            return;
        }
        sendJson(exchange, 404, Map.of("error", "Not Found"));
    }

    private void handleAdminWorkload(HttpExchange exchange) throws IOException {
        Map<String, Integer> workload = new HashMap<>();
        for (Application a : applicationService.findAll()) {
            if ("SELECTED".equalsIgnoreCase(a.getStatus())) {
                workload.merge(a.getApplicantId(), 1, Integer::sum);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Applicant ta : applicantService.findAll()) {
            Map<String, Object> row = new HashMap<>();
            row.put("applicantId", ta.getId());
            row.put("name", ta.getName());
            row.put("email", ta.getEmail());
            row.put("workload", workload.getOrDefault(ta.getId(), 0));
            result.add(row);
        }
        sendJson(exchange, 200, result);
    }

    private String queryParam(HttpExchange exchange, String name) {
        String q = exchange.getRequestURI().getQuery();
        if (q == null) return null;
        for (String pair : q.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && name.equals(kv[0])) return kv[1];
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T readJson(HttpExchange exchange, Type type) throws IOException {
        InputStream in = exchange.getRequestBody();
        String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        return (T) GSON.fromJson(json, type);
    }

    private void sendJson(HttpExchange exchange, int code, Object body) throws IOException {
        byte[] bytes = GSON.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
