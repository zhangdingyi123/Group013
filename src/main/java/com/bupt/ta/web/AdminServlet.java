package com.bupt.ta.web;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.model.Application;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Integer> workload = new HashMap<>();
        for (Application a : WebApp.getApplicationService().findAll()) {
            if ("SELECTED".equalsIgnoreCase(a.getStatus()))
                workload.merge(a.getApplicantId(), 1, Integer::sum);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Applicant ta : WebApp.getApplicantService().findAll()) {
            Map<String, Object> row = new HashMap<>();
            row.put("name", ta.getName());
            row.put("email", ta.getEmail());
            row.put("workload", workload.getOrDefault(ta.getId(), 0));
            rows.add(row);
        }
        req.setAttribute("workloadRows", rows);
        req.getRequestDispatcher("/admin/workload.jsp").forward(req, resp);
    }
}
