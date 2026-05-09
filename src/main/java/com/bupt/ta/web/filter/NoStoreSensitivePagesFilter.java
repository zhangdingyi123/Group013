package com.bupt.ta.web.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录页与登录后页面禁止缓存，降低「退出后通过后退键看到旧页面」的风险。
 */
@WebFilter(urlPatterns = {
        "/ta/auth", "/mo/auth", "/admin/auth",
        "/ta/dashboard", "/ta/dashboard/*",
        "/mo/dashboard", "/mo/job-applicants",
        "/admin/workload", "/ta/resume",
        "/forum"
})
public class NoStoreSensitivePagesFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void destroy() { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse) {
            HttpServletResponse resp = (HttpServletResponse) response;
            resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, private");
            resp.setHeader("Pragma", "no-cache");
            resp.setDateHeader("Expires", 0);
        }
        chain.doFilter(request, response);
    }
}
