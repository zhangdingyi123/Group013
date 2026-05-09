<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.model.ModuleOrganiser" %>
<%@ page import="java.util.List" %>
<%
    ModuleOrganiser mo = (ModuleOrganiser) request.getAttribute("mo");
    if (mo == null) {
        String c = request.getContextPath();
        if (session.getAttribute("moUser") != null) {
            response.sendRedirect(c + "/mo/profile");
        } else {
            response.sendRedirect(c + "/mo/auth");
        }
        return;
    }
    @SuppressWarnings("unchecked")
    List<Job> myJobs = (List<Job>) request.getAttribute("myJobs");
    if (myJobs == null) myJobs = java.util.Collections.emptyList();
    int jobCount = myJobs.size();
    int openCount = 0;
    for (Job j : myJobs) {
        if (j != null && Job.STATUS_OPEN.equals(j.getStatus())) openCount++;
    }
    String ctx = request.getContextPath();
    String dispName = mo.getName() != null && !mo.getName().trim().isEmpty()
            ? mo.getName().trim() : I18n.msg(request, "pc.mo.organizer.default");
    String avLetter = dispName.substring(0, 1);
    String emailDisp = mo.getEmail() != null && !mo.getEmail().isEmpty() ? mo.getEmail() : "—";
    String deptDisp = mo.getDepartment() != null && !mo.getDepartment().trim().isEmpty()
            ? mo.getDepartment().trim() : I18n.msg(request, "pc.field.empty");
    boolean nameOk = mo.getName() != null && !mo.getName().trim().isEmpty();
    boolean deptOk = mo.getDepartment() != null && !mo.getDepartment().trim().isEmpty();
    int pct = 40;
    if (nameOk) pct += 30;
    if (deptOk) pct += 30;
%>
<div id="pc-overview" class="mo-pc-stack">
    <section class="pc-card mo-hero-card" aria-label="<%= I18n.msg(request, "pc.mo.hero.aria") %>">
        <div class="mo-hero-strip"></div>
        <div class="mo-hero-inner">
            <div class="mo-avatar-xl" aria-hidden="true"><%= avLetter %></div>
            <div class="mo-hero-info">
                <p class="mo-badge"><%= I18n.msg(request, "pc.mo.badge") %></p>
                <h2 class="mo-name"><%= dispName %></h2>
                <p class="mo-subline"><span><%= emailDisp %></span><span class="mo-dot">·</span><span><%= deptDisp %></span></p>
                <div class="mo-stat-row">
                    <div class="mo-stat-pill"><strong><%= jobCount %></strong><span><%= I18n.msg(request, "pc.mo.stat.jobs") %></span></div>
                    <div class="mo-stat-pill mo-stat-open"><strong><%= openCount %></strong><span><%= I18n.msg(request, "pc.mo.stat.open") %></span></div>
                </div>
                <div class="mo-progress-block">
                    <div class="mo-progress-meta">
                        <span><%= I18n.msg(request, "pc.mo.profile.complete") %></span>
                        <strong><%= pct %>%</strong>
                    </div>
                    <div class="mo-progress-track" role="progressbar" aria-valuenow="<%= pct %>" aria-valuemin="0" aria-valuemax="100">
                        <div class="mo-progress-fill" style="width:<%= pct %>%"></div>
                    </div>
                    <p class="mo-progress-tip"><%= I18n.msg(request, "pc.mo.profile.tip") %></p>
                </div>
            </div>
        </div>
    </section>

    <section id="pc-quick" class="pc-card" aria-label="<%= I18n.msg(request, "pc.mo.nav.aria") %>">
        <div class="pc-card-hd" style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:.5rem">
            <h2 style="margin:0;font-size:1rem;font-weight:600"><%= I18n.msg(request, "pc.mo.quick.title") %></h2>
            <span style="font-size:.82rem;color:#8f959e"><%= I18n.msg(request, "pc.mo.quick.sub") %></span>
        </div>
        <div class="pc-card-bd mo-quick-wrap">
            <a href="<%= ctx %>/mo/dashboard?tab=positions" class="mo-tile">
                <span class="mo-tile-ic" aria-hidden="true">📌</span>
                <span class="mo-tile-t"><%= I18n.msg(request, "pc.mo.tile.jobs") %></span>
                <span class="mo-tile-d"><%= I18n.msg(request, "pc.mo.tile.jobs.d") %></span>
            </a>
            <a href="<%= ctx %>/mo/dashboard?tab=post" class="mo-tile">
                <span class="mo-tile-ic" aria-hidden="true">➕</span>
                <span class="mo-tile-t"><%= I18n.msg(request, "pc.mo.tile.post") %></span>
                <span class="mo-tile-d"><%= I18n.msg(request, "pc.mo.tile.post.d") %></span>
            </a>
            <a href="<%= ctx %>/mo/dashboard?tab=messages" class="mo-tile">
                <span class="mo-tile-ic" aria-hidden="true">💬</span>
                <span class="mo-tile-t"><%= I18n.msg(request, "pc.mo.tile.dm") %></span>
                <span class="mo-tile-d"><%= I18n.msg(request, "pc.mo.tile.dm.d") %></span>
            </a>
        </div>
    </section>
</div>

<section id="pc-edit" class="pc-card mo-edit-card">
    <div class="pc-card-hd" style="border-bottom:1px solid #e8eaed">
        <div>
            <h2 style="margin:0 0 .25rem;font-size:1rem;font-weight:600"><%= I18n.msg(request, "pc.mo.edit.title") %></h2>
            <span style="font-size:.82rem;color:#8f959e"><%= I18n.msg(request, "pc.mo.edit.sub") %></span>
        </div>
    </div>
    <div class="pc-card-bd">
        <form method="post" action="<%= ctx %>/mo/profile">
            <input type="hidden" name="action" value="updateProfile">
            <div class="form-group">
                <label for="mo-pf-name"><%= I18n.msg(request, "pc.mo.label.name") %></label>
                <input id="mo-pf-name" type="text" name="name" required value="<%= mo.getName() != null ? mo.getName() : "" %>">
            </div>
            <div class="form-group">
                <label for="mo-pf-email"><%= I18n.msg(request, "profile.edit.email") %></label>
                <input id="mo-pf-email" type="email" class="field-ro" value="<%= mo.getEmail() != null ? mo.getEmail() : "" %>" readonly aria-readonly="true">
            </div>
            <div class="form-group">
                <label for="mo-pf-dept"><%= I18n.msg(request, "pc.mo.label.dept") %></label>
                <input id="mo-pf-dept" type="text" name="department" placeholder="<%= I18n.msg(request, "profile.edit.dept.ph") %>" value="<%= mo.getDepartment() != null ? mo.getDepartment() : "" %>">
            </div>
            <button type="submit" class="btn btn-primary"><%= I18n.msg(request, "common.save") %></button>
        </form>
    </div>
</section>

<style>
  .mo-pc-stack{display:flex;flex-direction:column;gap:1.25rem}
  .mo-hero-card{overflow:hidden;padding:0}
  .mo-hero-strip{height:4px;background:linear-gradient(90deg,#0f172a,#1e3a5f,#2563eb)}
  .mo-hero-inner{display:flex;gap:1.25rem;padding:1.35rem 1.35rem 1.5rem;flex-wrap:wrap;align-items:flex-start}
  .mo-avatar-xl{width:88px;height:88px;border-radius:50%;background:linear-gradient(145deg,#1e293b,#2563eb);color:#fff;font-size:2rem;font-weight:700;display:flex;align-items:center;justify-content:center;flex-shrink:0;box-shadow:0 8px 24px rgba(15,23,42,.25)}
  .mo-hero-info{flex:1;min-width:0}
  .mo-badge{display:inline-block;margin:0 0 .5rem;font-size:.72rem;font-weight:600;letter-spacing:.04em;color:#64748b;text-transform:uppercase}
  .mo-name{margin:0 0 .35rem;font-size:1.35rem;font-weight:600;letter-spacing:-.02em;color:#1f2329}
  .mo-subline{margin:0 0 .85rem;font-size:.88rem;color:#646a73}
  .mo-dot{opacity:.45;margin:0 .35rem}
  .mo-stat-row{display:flex;flex-wrap:wrap;gap:.5rem;margin-bottom:1rem}
  .mo-stat-pill{display:inline-flex;align-items:center;gap:.5rem;padding:.45rem .85rem;background:#f7f8fa;border-radius:999px;font-size:.82rem;color:#646a73}
  .mo-stat-pill strong{font-size:1.05rem;color:#1f2329}
  .mo-stat-open{background:#eff6ff;color:#1d4ed8}
  .mo-stat-open strong{color:#1d4ed8}
  .mo-progress-meta{display:flex;justify-content:space-between;align-items:center;font-size:.82rem;color:#646a73;margin-bottom:.35rem}
  .mo-progress-meta strong{color:#2563eb;font-size:.95rem}
  .mo-progress-track{height:8px;background:#eef0f3;border-radius:999px;overflow:hidden}
  .mo-progress-fill{height:100%;background:linear-gradient(90deg,#1e3a5f,#2563eb);border-radius:999px}
  .mo-progress-tip{margin:.5rem 0 0;font-size:.78rem;color:#8f959e;line-height:1.45}
  .mo-quick-wrap{display:grid;grid-template-columns:repeat(auto-fill,minmax(200px,1fr));gap:1rem}
  .mo-tile{display:flex;flex-direction:column;align-items:flex-start;gap:.25rem;padding:1rem 1.1rem;border:1px solid #e8eaed;border-radius:10px;text-decoration:none;color:inherit;transition:border-color .15s,box-shadow .15s,transform .12s;background:#fafbfc}
  .mo-tile:hover{border-color:#bfdbfe;box-shadow:0 4px 14px rgba(37,99,235,.1);transform:translateY(-2px);background:#fff}
  .mo-tile-ic{font-size:1.35rem;line-height:1;margin-bottom:.15rem}
  .mo-tile-t{font-size:.95rem;font-weight:600;color:#1f2329}
  .mo-tile-d{font-size:.8rem;color:#8f959e;line-height:1.4}
  .mo-edit-card{margin:0}
</style>
