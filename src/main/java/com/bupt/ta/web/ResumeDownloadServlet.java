package com.bupt.ta.web;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.storage.Storage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 应聘者下载本人简历（.txt / .pdf / .doc / .docx）
 */
@WebServlet("/ta/resume")
public class ResumeDownloadServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Applicant user = (Applicant) req.getSession().getAttribute("taUser");
        if (user == null || user.getResumePath() == null || user.getResumePath().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Path path = Storage.getResumeFilePath(user.getResumePath());
        if (path == null || !Files.exists(path)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String filename = path.getFileName().toString();
        String contentType = getServletContext().getMimeType(filename);
        if (contentType == null) contentType = "application/octet-stream";
        resp.setContentType(contentType);
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        resp.setContentLengthLong(Files.size(path));
        try (OutputStream out = resp.getOutputStream()) {
            Files.copy(path, out);
        }
    }
}
