(function () {
  const API = '/api';
  let currentTA = null;
  let currentMO = null;

  function showView(id) {
    document.querySelectorAll('.view').forEach(v => v.classList.add('hidden'));
    const el = document.getElementById(id);
    if (el) el.classList.remove('hidden');
  }

  function showHome() {
    currentTA = null;
    currentMO = null;
    showView('view-home');
  }

  async function api(method, path, body) {
    const opt = { method, headers: { 'Content-Type': 'application/json' } };
    if (body) opt.body = JSON.stringify(body);
    const r = await fetch(API + path, opt);
    const data = r.ok ? await r.json().catch(() => ({})) : { error: (await r.text()) || 'Request failed' };
    if (!r.ok) throw new Error(data.error || 'Request failed');
    return data;
  }

  // ---------- 首页 ----------
  document.querySelectorAll('.card[data-role]').forEach(btn => {
    btn.addEventListener('click', () => {
      const role = btn.getAttribute('data-role');
      if (role === 'ta') showView('view-ta-auth');
      else if (role === 'mo') showView('view-mo-auth');
      else if (role === 'admin') loadAdminWorkload();
    });
  });

  document.querySelectorAll('.link.back').forEach(btn => {
    btn.addEventListener('click', showHome);
  });

  // ---------- TA 登录/注册 ----------
  document.querySelectorAll('#view-ta-auth .tab').forEach(tab => {
    tab.addEventListener('click', () => {
      document.querySelectorAll('#view-ta-auth .tab').forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      document.getElementById('ta-login').classList.toggle('hidden', tab.dataset.tab !== 'login');
      document.getElementById('ta-register').classList.toggle('hidden', tab.dataset.tab !== 'register');
    });
  });

  document.getElementById('ta-login').addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = e.target.email.value.trim();
    try {
      currentTA = await api('POST', '/applicants/login', { email });
      showTAMain();
    } catch (err) {
      alert(err.message || '登录失败');
    }
  });

  document.getElementById('ta-register').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = e.target.name.value.trim();
    const email = e.target.email.value.trim();
    try {
      currentTA = await api('POST', '/applicants/register', { name, email });
      showTAMain();
    } catch (err) {
      alert(err.message || '注册失败');
    }
  });

  function showTAMain() {
    showView('view-ta-main');
    document.getElementById('ta-name').textContent = currentTA.name;
    document.getElementById('ta-cv-path').value = currentTA.cvPath || '';
    document.getElementById('ta-skills').value = (currentTA.skills || []).join(', ');
    loadTAJobs();
    loadTAMyApps();
  }

  document.getElementById('ta-logout').addEventListener('click', showHome);

  document.querySelectorAll('#view-ta-main .tab').forEach(tab => {
    tab.addEventListener('click', () => {
      document.querySelectorAll('#view-ta-main .tab').forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      ['ta-profile', 'ta-jobs', 'ta-my-apps'].forEach((id, i) => {
        document.getElementById(id).classList.toggle('hidden', tab.dataset.tab !== ['profile', 'jobs', 'my-apps'][i]);
      });
    });
  });

  document.getElementById('ta-save-cv').addEventListener('click', async () => {
    const cvPath = document.getElementById('ta-cv-path').value.trim();
    try {
      await api('PATCH', '/applicants/' + currentTA.id, { cvPath });
      currentTA.cvPath = cvPath;
      alert('已保存');
    } catch (err) { alert(err.message); }
  });

  document.getElementById('ta-save-skills').addEventListener('click', async () => {
    const skills = document.getElementById('ta-skills').value.split(',').map(s => s.trim()).filter(Boolean);
    try {
      await api('PATCH', '/applicants/' + currentTA.id, { skills });
      currentTA.skills = skills;
      alert('已保存');
    } catch (err) { alert(err.message); }
  });

  async function loadTAJobs() {
    const list = document.getElementById('ta-job-list');
    try {
      const jobs = await api('GET', '/jobs/open');
      list.innerHTML = jobs.length ? jobs.map(j =>
        `<li><strong>${j.title}</strong> (${j.moduleCode}) · 技能: ${(j.requiredSkills || []).join(', ')} · ID: ${j.id}</li>`
      ).join('') : '<li>暂无开放职位</li>';
    } catch (err) {
      list.innerHTML = '<li class="error">加载失败</li>';
    }
  }

  document.getElementById('ta-apply-btn').addEventListener('click', async () => {
    const jobId = document.getElementById('ta-apply-job-id').value.trim();
    if (!jobId) { alert('请输入职位 ID'); return; }
    try {
      await api('POST', '/applications', { applicantId: currentTA.id, jobId });
      alert('申请已提交');
      loadTAJobs();
      loadTAMyApps();
    } catch (err) { alert(err.message); }
  });

  async function loadTAMyApps() {
    const list = document.getElementById('ta-app-list');
    try {
      const apps = await api('GET', '/applications?applicantId=' + encodeURIComponent(currentTA.id));
      list.innerHTML = apps.length ? apps.map(a => `<li>申请 ${a.jobId} → ${a.status}</li>`).join('') : '<li>暂无申请</li>';
    } catch (err) {
      list.innerHTML = '<li class="error">加载失败</li>';
    }
  }

  // ---------- MO 登录/注册 ----------
  document.querySelectorAll('#view-mo-auth .tab').forEach(tab => {
    tab.addEventListener('click', () => {
      document.querySelectorAll('#view-mo-auth .tab').forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      document.getElementById('mo-login').classList.toggle('hidden', tab.dataset.tab !== 'login');
      document.getElementById('mo-register').classList.toggle('hidden', tab.dataset.tab !== 'register');
    });
  });

  document.getElementById('mo-login').addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = e.target.email.value.trim();
    try {
      currentMO = await api('POST', '/mo/login', { email });
      showMOMain();
    } catch (err) {
      alert(err.message || '登录失败');
    }
  });

  document.getElementById('mo-register').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = e.target.name.value.trim();
    const email = e.target.email.value.trim();
    try {
      currentMO = await api('POST', '/mo/register', { name, email });
      showMOMain();
    } catch (err) {
      alert(err.message || '注册失败');
    }
  });

  function showMOMain() {
    showView('view-mo-main');
    document.getElementById('mo-name').textContent = currentMO.name;
    loadMOJobs();
  }

  document.getElementById('mo-logout').addEventListener('click', showHome);

  document.querySelectorAll('#view-mo-main .tab').forEach(tab => {
    tab.addEventListener('click', () => {
      document.querySelectorAll('#view-mo-main .tab').forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      ['mo-post', 'mo-my-jobs', 'mo-select'].forEach((id, i) => {
        document.getElementById(id).classList.toggle('hidden', tab.dataset.tab !== ['post', 'my-jobs', 'select'][i]);
      });
    });
  });

  document.getElementById('mo-post-job').addEventListener('submit', async (e) => {
    e.preventDefault();
    const title = e.target.title.value.trim();
    const moduleCode = e.target.moduleCode.value.trim();
    const skillsStr = e.target.requiredSkills.value.trim();
    const requiredSkills = skillsStr ? skillsStr.split(',').map(s => s.trim()) : [];
    try {
      await api('POST', '/jobs', { title, moduleCode, moId: currentMO.id, requiredSkills });
      alert('职位已发布');
      loadMOJobs();
    } catch (err) { alert(err.message); }
  });

  async function loadMOJobs() {
    const list = document.getElementById('mo-job-list');
    try {
      const jobs = await api('GET', '/jobs?moId=' + encodeURIComponent(currentMO.id));
      list.innerHTML = jobs.length ? jobs.map(j =>
        `<li><strong>${j.title}</strong> (${j.moduleCode}) · ${j.status} · ID: ${j.id}</li>`
      ).join('') : '<li>暂无职位</li>';
    } catch (err) {
      list.innerHTML = '<li class="error">加载失败</li>';
    }
  }

  document.getElementById('mo-load-apps').addEventListener('click', async () => {
    const jobId = document.getElementById('mo-select-job-id').value.trim();
    if (!jobId) { alert('请输入职位 ID'); return; }
    const list = document.getElementById('mo-app-list');
    try {
      const apps = await api('GET', '/applications?jobId=' + encodeURIComponent(jobId));
      list.innerHTML = apps.length ? apps.map(a =>
        `<li>申请 ID: ${a.id} · 申请人: ${a.applicantId} · ${a.status}</li>`
      ).join('') : '<li>该职位暂无申请</li>';
    } catch (err) {
      list.innerHTML = '<li class="error">加载失败</li>';
    }
  });

  document.getElementById('mo-select-btn').addEventListener('click', async () => {
    const appId = document.getElementById('mo-select-app-id').value.trim();
    if (!appId) { alert('请输入申请 ID'); return; }
    try {
      await api('POST', '/applications/' + appId + '/select');
      alert('已录用');
      document.getElementById('mo-load-apps').click();
    } catch (err) { alert(err.message); }
  });

  // ---------- Admin ----------
  async function loadAdminWorkload() {
    showView('view-admin');
    const tbody = document.querySelector('#admin-workload tbody');
    try {
      const rows = await api('GET', '/admin/workload');
      tbody.innerHTML = rows.length ? rows.map(r =>
        `<tr><td>${r.name}</td><td>${r.email}</td><td>${r.workload}</td></tr>`
      ).join('') : '<tr><td colspan="3">暂无数据</td></tr>';
    } catch (err) {
      tbody.innerHTML = '<tr><td colspan="3" class="error">加载失败</td></tr>';
    }
  }
})();
