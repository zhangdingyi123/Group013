package com.bupt.ta.web;

import com.bupt.ta.model.*;
import com.bupt.ta.service.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebServlet("/mo/dashboard")
public class MODashboardServlet extends HttpServlet {
    private final ModuleOrganiserService moService = new ModuleOrganiserService();
    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();

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
        req.getRequestDispatcher("/mo/dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        ModuleOrganiser user = (ModuleOrganiser) req.getSession().getAttribute("moUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/mo/auth");
            return;
        }
        String action = req.getParameter("action");
        if ("createJob".equals(action)) {
            String title = req.getParameter("title");
            String description = req.getParameter("description");
            String type = req.getParameter("type");
            String skillsStr = req.getParameter("requiredSkills");
            if (title != null && !title.trim().isEmpty()) {
                List<String> skills = new ArrayList<>();
                if (skillsStr != null) {
                    for (String s : skillsStr.split("[,，\\s]+")) {
                        if (!s.trim().isEmpty()) skills.add(s.trim());
                    }
                }
                try {
                    jobService.create(title.trim(), user.getId(), description != null ? description.trim() : "",
                            type != null ? type : "course_ta", skills);
                } catch (Exception ignored) {}
            }
            resp.sendRedirect(req.getContextPath() + "/mo/dashboard");
            return;
        }
        if ("closeJob".equals(action)) {
            String jobId = req.getParameter("jobId");
            if (jobId != null) {
                try {
                    Optional<Job> j = jobService.findById(jobId);
                    if (j.isPresent() && user.getId().equals(j.get().getModuleOrganiserId())) {
                        j.get().setStatus(Job.STATUS_CLOSED);
                        jobService.update(j.get());
                    }
                } catch (Exception ignored) {}
            }
            resp.sendRedirect(req.getContextPath() + "/mo/dashboard");
            return;
        }
        if ("applicationStatus".equals(action)) {
            String appId = req.getParameter("applicationId");
            String status = req.getParameter("status");
            if (appId != null && status != null
                    && (Application.STATUS_ACCEPTED.equals(status) || Application.STATUS_REJECTED.equals(status))) {
                try {
                    applicationService.updateStatus(appId, status);
                } catch (Exception ignored) {}
            }
            resp.sendRedirect(req.getContextPath() + "/mo/dashboard");
            return;
        }
        doGet(req, resp);
    }
}
