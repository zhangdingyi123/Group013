package com.bupt.ta.web;

import com.bupt.ta.model.Admin;
import com.bupt.ta.model.Applicant;
import com.bupt.ta.model.Application;
import com.bupt.ta.service.ApplicantService;
import com.bupt.ta.service.ApplicationService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员：查看助教整体工作负荷（每人被录用的岗位数）。需登录后访问。
 */
@WebServlet("/admin/workload")
public class AdminServlet extends HttpServlet {
    private final ApplicantService applicantService = new ApplicantService();
    private final ApplicationService applicationService = new ApplicationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Admin admin = (Admin) req.getSession().getAttribute("adminUser");
        if (admin == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/auth");
            return;
        }
        req.setAttribute("admin", admin);
        try {
            List<Application> all = applicationService.findAll();
            Map<String, Integer> workload = new HashMap<>();
            for (Application a : all) {
                if (Application.STATUS_ACCEPTED.equals(a.getStatus())) {
                    workload.merge(a.getApplicantId(), 1, Integer::sum);
                }
            }
            List<WorkloadEntry> entries = new ArrayList<>();
            for (Map.Entry<String, Integer> e : workload.entrySet()) {
                Applicant app = applicantService.findById(e.getKey()).orElse(null);
                entries.add(new WorkloadEntry(app != null ? app.getName() : e.getKey(), app != null ? app.getEmail() : "", e.getValue()));
            }
            entries.sort((a, b) -> Integer.compare(b.count, a.count));
            req.setAttribute("workload", entries);
            int total = entries.stream().mapToInt(e -> e.count).sum();
            req.setAttribute("totalAssignments", total);
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
        }
        req.getRequestDispatcher("/admin/workload.jsp").forward(req, resp);
    }

    public static class WorkloadEntry {
        public final String name;
        public final String email;
        public final int count;
        public WorkloadEntry(String name, String email, int count) {
            this.name = name;
            this.email = email;
            this.count = count;
        }
    }
}
