<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<%
    String ctx = request.getContextPath();
    request.setAttribute("moPostNotEnglishMsg", I18n.msg(request, "mo.post.notEnglish"));
%>
<div class="section">
    <h2><%= I18n.msg(request, "mo.post.title") %></h2>
    <p class="section-desc" style="margin-top:-0.25rem"><%= I18n.msg(request, "mo.post.englishOnlyHint") %></p>
    <form method="post" action="<%= ctx %>/mo/dashboard" id="mo-create-job-form">
        <input type="hidden" name="action" value="createJob">
        <div class="form-group">
            <label><%= I18n.msg(request, "mo.post.label.title") %></label>
            <input type="text" name="title" required placeholder="<%= I18n.msg(request, "mo.post.ph.title") %>">
        </div>
        <div class="form-group">
            <label><%= I18n.msg(request, "mo.post.label.desc") %></label>
            <textarea name="description" placeholder="<%= I18n.msg(request, "mo.post.ph.desc") %>"></textarea>
        </div>
        <div class="form-group">
            <label><%= I18n.msg(request, "mo.post.label.type") %></label>
            <select name="type">
                <option value="course_ta"><%= I18n.msg(request, "jobs.type.course_ta") %></option>
                <option value="invigilation"><%= I18n.msg(request, "jobs.type.invigilation") %></option>
                <option value="activity"><%= I18n.msg(request, "jobs.type.activity") %></option>
            </select>
        </div>
        <div class="form-group">
            <label><%= I18n.msg(request, "mo.post.label.skills") %></label>
            <input type="text" name="requiredSkills" placeholder="<%= I18n.msg(request, "mo.post.ph.skills") %>">
        </div>
        <button type="button" class="btn btn-primary" id="mo-post-open-confirm"><%= I18n.msg(request, "mo.post.submit") %></button>
        <div class="mo-job-en-modal mo-post-confirm-modal" id="moPostConfirmModal" role="dialog" aria-modal="true" aria-hidden="true" aria-labelledby="moPostConfirmTitle">
            <div class="mo-job-en-modal__backdrop" aria-hidden="true"></div>
            <div class="mo-job-en-modal__panel">
                <h2 id="moPostConfirmTitle"><%= I18n.msg(request, "mo.post.confirm.title") %></h2>
                <p class="mo-job-en-modal__lead" style="margin-top:0"><%= I18n.msg(request, "mo.post.confirm.lead") %></p>
                <dl class="mo-post-confirm-summary">
                    <dt><%= I18n.msg(request, "mo.post.label.title") %></dt><dd id="moPostSumTitle"></dd>
                    <dt><%= I18n.msg(request, "mo.post.label.desc") %></dt><dd id="moPostSumDesc"></dd>
                    <dt><%= I18n.msg(request, "mo.post.label.type") %></dt><dd id="moPostSumType"></dd>
                    <dt><%= I18n.msg(request, "mo.post.label.skills") %></dt><dd id="moPostSumSkills"></dd>
                </dl>
                <div class="mo-job-en-modal__actions">
                    <button type="button" class="btn btn-secondary" id="mo-post-confirm-cancel"><%= I18n.msg(request, "mo.post.confirm.back") %></button>
                    <button type="submit" class="btn btn-primary" id="mo-post-confirm-submit"><%= I18n.msg(request, "mo.post.confirm.submit") %></button>
                </div>
            </div>
        </div>
    </form>
    <div class="mo-job-en-modal mo-job-en-modal--stack mo-post-lang-error-modal" id="moPostLangErrorModal" role="dialog" aria-modal="true" aria-hidden="true" aria-labelledby="moPostLangErrorTitle">
        <div class="mo-job-en-modal__backdrop" aria-hidden="true"></div>
        <div class="mo-job-en-modal__panel">
            <h2 id="moPostLangErrorTitle"><%= I18n.msg(request, "mo.post.langError.title") %></h2>
            <p class="mo-post-lang-error-body" id="moPostLangErrorBody"><c:out value="${moPostNotEnglishMsg}"/></p>
            <div class="mo-job-en-modal__actions">
                <button type="button" class="btn btn-primary" id="mo-post-lang-error-ok"><%= I18n.msg(request, "mo.post.langError.ok") %></button>
            </div>
        </div>
    </div>
    <script>
    (function () {
      var form = document.getElementById('mo-create-job-form');
      var modal = document.getElementById('moPostConfirmModal');
      var openBtn = document.getElementById('mo-post-open-confirm');
      var cancelBtn = document.getElementById('mo-post-confirm-cancel');
      var errModal = document.getElementById('moPostLangErrorModal');
      var errOk = document.getElementById('mo-post-lang-error-ok');
      var dash = '\u2014';
      if (!form || !modal || !openBtn) return;
      function fillSummary() {
        var title = form.querySelector('[name="title"]');
        var desc = form.querySelector('[name="description"]');
        var skills = form.querySelector('[name="requiredSkills"]');
        var sel = form.querySelector('[name="type"]');
        document.getElementById('moPostSumTitle').textContent = (title && title.value.trim()) ? title.value.trim() : dash;
        document.getElementById('moPostSumDesc').textContent = (desc && desc.value.trim()) ? desc.value.trim() : dash;
        document.getElementById('moPostSumSkills').textContent = (skills && skills.value.trim()) ? skills.value.trim() : dash;
        var typeDd = document.getElementById('moPostSumType');
        if (sel && typeDd) {
          typeDd.textContent = sel.options[sel.selectedIndex] ? sel.options[sel.selectedIndex].text : dash;
        }
      }
      function openM() {
        if (!form.checkValidity()) { form.reportValidity(); return; }
        fillSummary();
        modal.classList.add('mo-job-en-modal--open');
        modal.setAttribute('aria-hidden', 'false');
        document.body.classList.add('mo-job-en-modal-open');
      }
      function closeM() {
        modal.classList.remove('mo-job-en-modal--open');
        modal.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('mo-job-en-modal-open');
      }
      function openLangErr() {
        if (!errModal) return;
        errModal.classList.add('mo-job-en-modal--open');
        errModal.setAttribute('aria-hidden', 'false');
        document.body.classList.add('mo-job-en-modal-open');
      }
      function closeLangErr() {
        if (!errModal) return;
        errModal.classList.remove('mo-job-en-modal--open');
        errModal.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('mo-job-en-modal-open');
      }
      if (errModal && errOk) {
        errOk.addEventListener('click', closeLangErr);
        errModal.querySelector('.mo-job-en-modal__backdrop').addEventListener('click', closeLangErr);
      }
      openBtn.addEventListener('click', openM);
      cancelBtn.addEventListener('click', closeM);
      modal.querySelector('.mo-job-en-modal__backdrop').addEventListener('click', closeM);
      document.addEventListener('keydown', function (e) {
        if (e.key !== 'Escape') return;
        if (errModal && errModal.classList.contains('mo-job-en-modal--open')) {
          closeLangErr();
          return;
        }
        if (modal.classList.contains('mo-job-en-modal--open')) closeM();
      });
      function moNonEnglishCp(cp) {
        if (cp >= 0x4E00 && cp <= 0x9FFF) return true;
        if (cp >= 0x3400 && cp <= 0x4DBF) return true;
        if (cp >= 0x20000 && cp <= 0x2A6DF) return true;
        if (cp >= 0xAC00 && cp <= 0xD7AF) return true;
        if (cp >= 0x3040 && cp <= 0x30FF) return true;
        if (cp >= 0x0400 && cp <= 0x052F) return true;
        if (cp >= 0x0600 && cp <= 0x06FF) return true;
        return false;
      }
      function moJobTextLooksNonEnglish(s) {
        if (!s) return false;
        for (var i = 0; i < s.length; ) {
          var cp = s.codePointAt(i);
          if (moNonEnglishCp(cp)) return true;
          i += cp > 0xFFFF ? 2 : 1;
        }
        return false;
      }
      form.addEventListener('submit', function (e) {
        var titleEl = form.querySelector('[name="title"]');
        var descEl = form.querySelector('[name="description"]');
        var skillsEl = form.querySelector('[name="requiredSkills"]');
        var title = titleEl && titleEl.value ? titleEl.value.trim() : '';
        var desc = descEl && descEl.value ? descEl.value.trim() : '';
        var skills = skillsEl && skillsEl.value ? skillsEl.value : '';
        var combined = title + '\n' + desc + '\n' + skills;
        if (moJobTextLooksNonEnglish(combined)) {
          e.preventDefault();
          closeM();
          openLangErr();
        }
      });
    })();
    </script>
</div>
