package com.bupt.ta.web;

import com.bupt.ta.model.Job;
import com.bupt.ta.model.ModuleOrganiser;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.ModuleOrganiserService;
import com.bupt.ta.util.I18n;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * 课程组织者个人中心（独立于工作台子导航，右上角入口）。
 */
@WebServlet("/mo/profile")
public class MOProfileServlet extends HttpServlet {
    private final JobService jobService = new JobService();
    private final ModuleOrganiserService moduleOrganiserService = new ModuleOrganiserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ModuleOrganiser user = (ModuleOrganiser) req.getSession().getAttribute("moUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/mo/auth");
            return;
        }
        req.setAttribute("mo", user);
        try {
            List<Job> myJobs = jobService.findByModuleOrganiserId(user.getId());
            req.setAttribute("myJobs", myJobs);
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
        }
        HttpSession session = req.getSession();
        String moNotice = (String) session.getAttribute("moNotice");
        if (moNotice != null) {
            session.removeAttribute("moNotice");
            req.setAttribute("moNotice", moNotice);
        }
        req.getRequestDispatcher("/mo/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        ModuleOrganiser user = (ModuleOrganiser) session.getAttribute("moUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/mo/auth");
            return;
        }
        String action = req.getParameter("action");
        if ("updateProfile".equals(action)) {
            String name = req.getParameter("name");
            String department = req.getParameter("department");
            if (name != null) {
                user.setName(name.trim());
            }
            if (department != null) {
                user.setDepartment(department.trim());
            }
            try {
                moduleOrganiserService.update(user);
                session.setAttribute("moUser", user);
                session.setAttribute("moNotice", I18n.msg(req, "profile.saved.mo"));
            } catch (Exception ignored) {}
        }
        resp.sendRedirect(req.getContextPath() + "/mo/profile");
    }
}
