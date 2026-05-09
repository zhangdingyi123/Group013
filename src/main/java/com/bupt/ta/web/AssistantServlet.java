package com.bupt.ta.web;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.service.ApplicantService;
import com.bupt.ta.service.assistant.AssistantConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 智能小助手页面。
 */
@WebServlet("/assistant")
public class AssistantServlet extends HttpServlet {

    private final ApplicantService applicantService = new ApplicantService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);
        Applicant ta = session != null ? (Applicant) session.getAttribute("taUser") : null;
        if (ta == null) {
            String q = URLEncoder.encode("/assistant", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/ta/auth?returnUrl=" + q);
            return;
        }
        req.setAttribute("assistantKimiConfigured", !AssistantConfig.kimiApiKey().isEmpty());
        req.setAttribute("assistantQwenConfigured", !AssistantConfig.qwenApiKey().isEmpty());
        req.setAttribute("assistantOpenaiConfigured", !AssistantConfig.openaiApiKey().isEmpty());
        req.setAttribute("assistantDefaultProvider", AssistantConfig.defaultProvider());
        req.setAttribute("assistantTaLoggedIn", Boolean.TRUE);
        String rp = ta.getResumePath();
        boolean okResume = rp != null && !rp.trim().isEmpty() && applicantService.canAssistantReadResume(rp);
        req.setAttribute("assistantSavedResumeTxt", okResume);
        req.getRequestDispatcher("/assistant.jsp").forward(req, resp);
    }
}
