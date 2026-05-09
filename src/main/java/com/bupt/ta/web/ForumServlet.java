package com.bupt.ta.web;

import com.bupt.ta.model.Admin;
import com.bupt.ta.model.Applicant;
import com.bupt.ta.model.ForumReply;
import com.bupt.ta.model.ForumThread;
import com.bupt.ta.model.ModuleOrganiser;
import com.bupt.ta.service.ForumService;
import com.bupt.ta.util.I18n;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * 交流论坛：公开浏览，登录用户（应聘者 / 课程组织者 / 管理员）可发帖与回复。
 */
@WebServlet("/forum")
public class ForumServlet extends HttpServlet {
    private final ForumService forumService = new ForumService();

    static final class ForumAuthor {
        final String userId;
        final String role;
        final String displayName;

        ForumAuthor(String userId, String role, String displayName) {
            this.userId = userId;
            this.role = role;
            this.displayName = displayName;
        }
    }

    static ForumAuthor resolveAuthor(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) {
            return null;
        }
        Object ta = s.getAttribute("taUser");
        if (ta instanceof Applicant) {
            Applicant a = (Applicant) ta;
            String n = a.getName();
            return new ForumAuthor(a.getId(), "ta", (n != null && !n.isEmpty()) ? n : I18n.msg(req, "role.default.ta"));
        }
        Object mo = s.getAttribute("moUser");
        if (mo instanceof ModuleOrganiser) {
            ModuleOrganiser m = (ModuleOrganiser) mo;
            String n = m.getName();
            return new ForumAuthor(m.getId(), "mo", (n != null && !n.isEmpty()) ? n : I18n.msg(req, "role.default.mo"));
        }
        Object ad = s.getAttribute("adminUser");
        if (ad instanceof Admin) {
            Admin a = (Admin) ad;
            String n = a.getName();
            return new ForumAuthor(a.getId(), "admin", (n != null && !n.isEmpty()) ? n : I18n.msg(req, "role.default.admin"));
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String threadId = req.getParameter("threadId");
        if (threadId != null) {
            threadId = threadId.trim();
        }
        try {
            if (threadId != null && !threadId.isEmpty()) {
                Optional<ForumThread> t = forumService.findThreadById(threadId);
                if (t.isEmpty()) {
                    resp.sendRedirect(req.getContextPath() + "/forum");
                    return;
                }
                req.setAttribute("forumThread", t.get());
                req.setAttribute("forumReplies", forumService.findRepliesForThread(threadId));
                req.setAttribute("forumAuthor", resolveAuthor(req));
                String notice = consumeForumNotice(req);
                if (notice != null) {
                    req.setAttribute("forumNotice", notice);
                }
                req.getRequestDispatcher("/forum/thread.jsp").forward(req, resp);
                return;
            }
            String q = req.getParameter("q");
            String sort = req.getParameter("sort");
            int page = 1;
            try {
                page = Integer.parseInt(req.getParameter("page"));
            } catch (NumberFormatException ignored) {}
            ForumService.ForumIndexPage indexPage = forumService.listThreadsForIndex(q, sort, page, ForumService.DEFAULT_PAGE_SIZE);
            req.setAttribute("forumThreads", indexPage.threads);
            req.setAttribute("forumIndexTotal", indexPage.total);
            req.setAttribute("forumIndexPage", indexPage.page);
            req.setAttribute("forumIndexPageSize", indexPage.pageSize);
            req.setAttribute("forumIndexTotalPages", indexPage.totalPages);
            req.setAttribute("forumQuery", q != null ? q : "");
            req.setAttribute("forumSort", sort != null ? sort : "");
            req.setAttribute("forumAuthor", resolveAuthor(req));
            String notice = consumeForumNotice(req);
            if (notice != null) {
                req.setAttribute("forumNotice", notice);
            }
            req.getRequestDispatcher("/forum/index.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("forumError", e.getMessage());
            req.setAttribute("forumThreads", java.util.Collections.emptyList());
            req.setAttribute("forumIndexTotal", 0);
            req.setAttribute("forumIndexPage", 1);
            req.setAttribute("forumIndexPageSize", ForumService.DEFAULT_PAGE_SIZE);
            req.setAttribute("forumIndexTotalPages", 1);
            req.setAttribute("forumQuery", "");
            req.setAttribute("forumSort", "");
            req.setAttribute("forumAuthor", resolveAuthor(req));
            req.getRequestDispatcher("/forum/index.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        ForumAuthor author = resolveAuthor(req);
        if (author == null) {
            req.getSession(true).setAttribute("forumNotice", I18n.msg(req, "forum.need.login"));
            resp.sendRedirect(req.getContextPath() + "/forum");
            return;
        }
        String action = req.getParameter("action");
        try {
            if ("newThread".equals(action)) {
                String title = req.getParameter("title");
                String body = req.getParameter("body");
                ForumThread created = forumService.createThread(author.userId, author.role, author.displayName, title, body);
                if (created == null) {
                    req.getSession(true).setAttribute("forumNotice", I18n.msg(req, "forum.new.fail"));
                    resp.sendRedirect(req.getContextPath() + "/forum");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/forum?threadId="
                            + URLEncoder.encode(created.getId(), StandardCharsets.UTF_8));
                }
                return;
            }
            if ("newReply".equals(action)) {
                String threadId = req.getParameter("threadId");
                String body = req.getParameter("body");
                if (threadId != null) {
                    threadId = threadId.trim();
                }
                ForumReply reply = forumService.addReply(threadId, author.userId, author.role, author.displayName, body);
                if (reply == null) {
                    req.getSession(true).setAttribute("forumNotice", I18n.msg(req, "forum.reply.fail"));
                    resp.sendRedirect(req.getContextPath() + "/forum");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/forum?threadId="
                            + URLEncoder.encode(threadId, StandardCharsets.UTF_8) + "#reply-form");
                }
                return;
            }
        } catch (Exception e) {
            req.getSession(true).setAttribute("forumNotice", I18n.msg(req, "forum.op.fail", e.getMessage()));
        }
        resp.sendRedirect(req.getContextPath() + "/forum");
    }

    private static String consumeForumNotice(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) {
            return null;
        }
        Object o = s.getAttribute("forumNotice");
        if (o instanceof String) {
            s.removeAttribute("forumNotice");
            return (String) o;
        }
        return null;
    }
}
