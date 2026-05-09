package com.bupt.ta.web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 首页「个人中心」入口：已登录则进入对应角色页面，否则进入身份选择页。
 */
@WebServlet("/personal-center")
public class PersonalCenterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ctx = req.getContextPath();
        if (req.getSession().getAttribute("taUser") != null) {
            resp.sendRedirect(ctx + "/ta/profile");
            return;
        }
        if (req.getSession().getAttribute("moUser") != null) {
            resp.sendRedirect(ctx + "/mo/profile");
            return;
        }
        if (req.getSession().getAttribute("adminUser") != null) {
            resp.sendRedirect(ctx + "/admin/workload");
            return;
        }
        req.getRequestDispatcher("/personal_center_gate.jsp").forward(req, resp);
    }
}
