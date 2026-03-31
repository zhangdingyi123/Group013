package com.bupt.ta.web;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.service.ApplicantService;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简历文件上传单独映射，避免在 {@link TADashboardServlet} 上使用 {@link MultipartConfig}
 * 导致部分 Tomcat 版本对同一路径的 GET 请求无法解析查询参数（如 tab=jobs）。
 */
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 5 * 1024 * 1024)
@WebServlet("/ta/dashboard/upload-resume")
public class TAResumeUploadServlet extends HttpServlet {
    private final ApplicantService applicantService = new ApplicantService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        Applicant user = (Applicant) req.getSession().getAttribute("taUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/ta/auth");
            return;
        }
        try {
            Part filePart = req.getPart("resumeFile");
            if (filePart != null && filePart.getSize() > 0) {
                String fn = getSubmittedFileName(filePart);
                if (fn != null && !fn.trim().isEmpty()) {
                    String ext = fn.contains(".") ? fn.substring(fn.lastIndexOf('.')).toLowerCase() : "";
                    if (ext.equals(".txt") || ext.equals(".pdf") || ext.equals(".doc") || ext.equals(".docx")) {
                        String saved = applicantService.saveResumeFile(user.getId(), filePart.getInputStream(), fn);
                        if (saved != null) {
                            user.setResumePath(saved);
                            applicantService.update(user);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        resp.sendRedirect(req.getContextPath() + "/ta/dashboard?tab=resume");
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
}
