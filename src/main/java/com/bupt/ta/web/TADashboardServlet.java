package com.bupt.ta.web;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.service.MatchHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TADashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Applicant ta = session != null ? (Applicant) session.getAttribute("ta") : null;
        if (ta == null) {
            resp.sendRedirect(req.getContextPath() + "/ta/auth");
            return;
        }
        ta = WebApp.getApplicantService().findById(ta.getId()).orElse(ta);
        req.setAttribute("ta", ta);
        List<Map<String, Object>> jobsWithMatch = MatchHelper.jobsWithMatch(ta.getId(), WebApp.getApplicantService(), WebApp.getJobService());
        req.setAttribute("jobsWithMatch", jobsWithMatch);
        List<Application> myApps = WebApp.getApplicationService().findByApplicant(ta.getId());
        req.setAttribute("myApplications", myApps);
        req.getRequestDispatcher("/ta/dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Applicant ta = session != null ? (Applicant) session.getAttribute("ta") : null;
        if (ta == null) {
            resp.sendRedirect(req.getContextPath() + "/ta/auth");
            return;
        }
        String action = req.getParameter("action");
        if ("saveCv".equals(action)) {
            String cvPath = req.getParameter("cvPath");
            WebApp.getApplicantService().updateCv(ta.getId(), cvPath != null ? cvPath.trim() : "");
        } else if ("saveSkills".equals(action)) {
            String skillsStr = req.getParameter("skills");
            List<String> skills = new java.util.ArrayList<>();
            if (skillsStr != null && !skillsStr.trim().isEmpty()) {
                for (String s : skillsStr.split(",")) if (!s.trim().isEmpty()) skills.add(s.trim());
            }
            WebApp.getApplicantService().updateSkills(ta.getId(), skills);
        } else if ("apply".equals(action)) {
            String jobId = req.getParameter("jobId");
            if (jobId != null && !jobId.trim().isEmpty())
                WebApp.getApplicationService().apply(ta.getId(), jobId.trim());
        }
        resp.sendRedirect(req.getContextPath() + "/ta/dashboard");
    }
}
