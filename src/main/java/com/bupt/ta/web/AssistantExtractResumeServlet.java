package com.bupt.ta.web;

import com.bupt.ta.service.ResumeTextExtractor;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import com.bupt.ta.model.Applicant;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 上传简历文件，服务端抽取纯文本，供小助手「粘贴或上传」使用。
 */
@MultipartConfig(maxFileSize = 6 * 1024 * 1024, maxRequestSize = 7 * 1024 * 1024)
@WebServlet("/api/assistant/extract-resume")
public class AssistantExtractResumeServlet extends HttpServlet {

    private static final int MAX_RETURN_CHARS = 10000;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession(false);
        Applicant user = session != null ? (Applicant) session.getAttribute("taUser") : null;
        if (user == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "login required");
            return;
        }

        Part part;
        try {
            part = req.getPart("file");
        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "no file");
            return;
        }
        if (part == null || part.getSize() <= 0) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "empty file");
            return;
        }

        String filename = getSubmittedFileName(part);
        if (filename == null || filename.trim().isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "missing filename");
            return;
        }
        if (!ResumeTextExtractor.isSupportedFilename(filename)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "unsupported format");
            return;
        }

        byte[] data;
        try (InputStream in = part.getInputStream()) {
            data = in.readAllBytes();
        }
        if (data.length == 0) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "empty file");
            return;
        }

        String text;
        try {
            text = ResumeTextExtractor.extractFromBytes(data, filename);
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "unsupported format");
            return;
        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "extract failed");
            return;
        }

        if (text == null) {
            text = "";
        }
        text = text.trim();
        if (text.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "no text extracted");
            return;
        }
        if (text.length() > MAX_RETURN_CHARS) {
            text = text.substring(0, MAX_RETURN_CHARS);
        }

        JsonObject out = new JsonObject();
        out.addProperty("ok", true);
        out.addProperty("text", text);
        writeJson(resp, out);
    }

    private static String getSubmittedFileName(Part part) {
        if (part == null) {
            return null;
        }
        String cd = part.getHeader("Content-Disposition");
        if (cd == null) {
            return null;
        }
        Matcher m = Pattern.compile("filename\\*?=(?:UTF-8'')?[\"']?([^\"';\\r\\n]+)[\"']?").matcher(cd);
        if (m.find()) {
            String name = m.group(1).trim();
            if (!name.isEmpty()) {
                return name;
            }
        }
        return null;
    }

    private void writeJson(HttpServletResponse resp, JsonObject obj) throws IOException {
        PrintWriter w = resp.getWriter();
        w.write(obj.toString());
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        JsonObject o = new JsonObject();
        o.addProperty("ok", false);
        o.addProperty("error", message);
        writeJson(resp, o);
    }
}
