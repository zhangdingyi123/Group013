package com.bupt.ta.web;

import com.bupt.ta.service.assistant.AssistantConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 智能小助手页面。
 */
@WebServlet("/assistant")
public class AssistantServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        req.setAttribute("assistantKimiConfigured", !AssistantConfig.kimiApiKey().isEmpty());
        req.setAttribute("assistantQwenConfigured", !AssistantConfig.qwenApiKey().isEmpty());
        req.setAttribute("assistantDefaultProvider", AssistantConfig.defaultProvider());
        req.getRequestDispatcher("/assistant.jsp").forward(req, resp);
    }
}
