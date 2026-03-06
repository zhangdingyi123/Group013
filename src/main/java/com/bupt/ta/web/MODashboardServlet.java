package com.bupt.ta.web;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.ModuleOrganiser;
import com.bupt.ta.service.MatchHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MODashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        ModuleOrganiser mo = session != null ? (ModuleOrganiser) session.getAttribute("mo") : null;
        if (mo == null) {
            resp.sendRedirect(req.getContextPath() + "/mo/auth");
            return;
        }
        mo = WebApp.getMoService().findById(mo.getId()).orElse(mo);
        req.setAttribute("mo", mo);
        List<Job> myJobs = new ArrayList<>();
        for (Job j : WebApp.getJobService().findAll())
            if (mo.getId().equals(j.getMoId())) myJobs.add(j);
        req.setAttribute("myJobs", myJobs);
        String jobId = req.getParameter("jobId");
        if (jobId != null && !jobId.trim().isEmpty()) {
            List<Map<String, Object>> appsWithStats = MatchHelper.applicationsWithStats(jobId.trim(),
                    WebApp.getApplicantService(), WebApp.getJobService(), WebApp.getApplicationService());
            req.setAttribute("applicationsWithStats", appsWithStats);
            req.setAttribute("selectedJobId", jobId.trim());
        }
        req.getRequestDispatcher("/mo/dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        ModuleOrganiser mo = session != null ? (ModuleOrganiser) session.getAttribute("mo") : null;
        if (mo == null) {
            resp.sendRedirect(req.getContextPath() + "/mo/auth");
            return;
        }
        String action = req.getParameter("action");
        if ("postJob".equals(action)) {
            String title = req.getParameter("title");
            String moduleCode = req.getParameter("moduleCode");
            String skillsStr = req.getParameter("requiredSkills");
            List<String> skills = new ArrayList<>();
            if (skillsStr != null && !skillsStr.trim().isEmpty())
                for (String s : skillsStr.split(",")) if (!s.trim().isEmpty()) skills.add(s.trim());
            if (title != null && !title.trim().isEmpty() && moduleCode != null && !moduleCode.trim().isEmpty())
                WebApp.getJobService().create(title.trim(), moduleCode.trim(), mo.getId(), skills);
        } else if ("select".equals(action)) {
            String appId = req.getParameter("applicationId");
            if (appId != null && !appId.trim().isEmpty())
                WebApp.getApplicationService().selectApplicant(appId.trim());
        }
        String jobId = req.getParameter("jobId");
        if (jobId != null && !jobId.isEmpty())
            resp.sendRedirect(req.getContextPath() + "/mo/dashboard?jobId=" + java.net.URLEncoder.encode(jobId, "UTF-8"));
        else
            resp.sendRedirect(req.getContextPath() + "/mo/dashboard");
    }
}
