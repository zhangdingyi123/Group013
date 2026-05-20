#!/usr/bin/env python3
"""Capture bilingual UI screenshots (zh / en) for report appendix."""
import json
import os
import subprocess
import sys
import time
import urllib.parse
import urllib.request
import http.cookiejar

BASE = os.environ.get("APP_BASE", "http://localhost:8082").rstrip("/")
OUT_ROOT = os.path.join(os.path.dirname(__file__), "report_ui")
CHROME = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"

TA_EMAIL = "liuchen@bupt-demo.edu.cn"
TA_PASSWORD = "demo123"
MO_EMAIL = "li.prof@bupt-demo.edu.cn"
MO_PASSWORD = "demo123"
ADMIN_EMAIL = "admin@bupt-demo.edu.cn"
ADMIN_PASSWORD = "admin123"

LOCALES = ("zh", "en")


def out_dir(locale: str) -> str:
    d = os.path.join(OUT_ROOT, locale)
    os.makedirs(d, exist_ok=True)
    return d


def url_with_lang(path: str, lang: str) -> str:
    """First hit with ?lang= sets ui_lang cookie via LocaleFilter redirect."""
    sep = "&" if "?" in path else "?"
    return f"{BASE}{path}{sep}lang={lang}"


def wait_server(timeout=90):
    for _ in range(timeout):
        try:
            with urllib.request.urlopen(BASE + "/", timeout=2) as r:
                if r.status == 200:
                    return True
        except Exception:
            pass
        time.sleep(1)
    return False


def cookie_jar_after_lang(lang: str) -> http.cookiejar.CookieJar:
    cj = http.cookiejar.CookieJar()
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))
    urllib.request.install_opener(opener)
    urllib.request.urlopen(url_with_lang("/", lang), timeout=10)
    return cj


def cookies_to_header(cj: http.cookiejar.CookieJar) -> str:
    return "; ".join(f"{c.name}={c.value}" for c in cj)


def login(role_path: str, email: str, password: str, lang: str) -> str:
    cj = cookie_jar_after_lang(lang)
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))
    data = urllib.parse.urlencode(
        {"action": "login", "email": email, "password": password}
    ).encode()
    req = urllib.request.Request(
        BASE + role_path,
        data=data,
        method="POST",
        headers={"Content-Type": "application/x-www-form-urlencoded"},
    )
    opener.open(req, timeout=10)
    return cookies_to_header(cj)


def data_path(name: str) -> str:
    return os.path.join(os.path.dirname(__file__), "..", "src", "main", "webapp", "data", name)


def first_job_id():
    with open(data_path("jobs.json"), encoding="utf-8") as f:
        jobs = json.load(f)
    for j in jobs:
        if j.get("status", "open") == "open":
            return j["id"]
    return jobs[0]["id"] if jobs else ""


def first_forum_thread_id():
    with open(data_path("forum_threads.json"), encoding="utf-8") as f:
        threads = json.load(f)
    if threads:
        return threads[0]["id"]
    return ""


def chrome_screenshot_url(url: str, out_path: str, width=1280, height=800):
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    subprocess.run(
        [
            CHROME,
            "--headless=new",
            "--disable-gpu",
            "--hide-scrollbars",
            f"--window-size={width},{height}",
            "--screenshot=" + os.path.abspath(out_path),
            url,
        ],
        check=True,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
        timeout=30,
    )


def rewrite_html_paths(html_str: str) -> str:
    for a, b in (
        ('href="/', f'href="{BASE}/'),
        ("href='/", f"href='{BASE}/"),
        ('src="/', f'src="{BASE}/'),
        ("src='/", f"src='{BASE}/"),
        ('action="/', f'action="{BASE}/'),
        ("action='/", f"action='{BASE}/"),
    ):
        html_str = html_str.replace(a, b)
    return html_str


def chrome_screenshot_auth(path: str, out_path: str, cookie_header: str, width=1280, height=800, inject_js: str = ""):
    req = urllib.request.Request(BASE + path, headers={"Cookie": cookie_header})
    with urllib.request.urlopen(req, timeout=15) as resp:
        html = resp.read()
    tmp = os.path.join(OUT_ROOT, "_tmp_page.html")
    html_str = rewrite_html_paths(html.decode("utf-8", errors="replace"))
    if inject_js:
        html_str = html_str.replace("</body>", f"<script>{inject_js}</script></body>")
    with open(tmp, "w", encoding="utf-8") as f:
        f.write(html_str)
    chrome_screenshot_url("file://" + os.path.abspath(tmp), out_path, width, height)
    try:
        os.remove(tmp)
    except OSError:
        pass


def mo_post_english_error_via_server(mo_cookie: str, od: str) -> bool:
    """Trigger server-side English validation error, then capture post tab."""
    data = urllib.parse.urlencode(
        {
            "action": "createJob",
            "title": "数据结构助教",
            "description": "Help with labs",
            "type": "course_ta",
            "requiredSkills": "Java",
        }
    ).encode()
    req = urllib.request.Request(
        BASE + "/mo/dashboard",
        data=data,
        method="POST",
        headers={
            "Cookie": mo_cookie,
            "Content-Type": "application/x-www-form-urlencoded",
        },
    )
    try:
        urllib.request.urlopen(req, timeout=15)
    except urllib.error.HTTPError:
        pass
    chrome_screenshot_auth(
        "/mo/dashboard?tab=post",
        os.path.join(od, "29_mo_post_english_error.png"),
        mo_cookie,
        height=950,
    )
    return True


def capture_locale(lang: str):
    od = out_dir(lang)
    label = "中文" if lang == "zh" else "English"
    print(f"\n=== Locale: {lang} ({label}) ===", flush=True)

    public = [
        ("01_home.png", "/"),
        ("02_personal_center.png", "/personal-center"),
        ("03_ta_login.png", "/ta/auth"),
        ("04_mo_login.png", "/mo/auth"),
        ("05_admin_login.png", "/admin/auth"),
        ("06_forum.png", "/forum"),
    ]
    for name, path in public:
        out = os.path.join(od, name)
        print(f"  {name}", flush=True)
        chrome_screenshot_url(url_with_lang(path, lang), out)

    print("  TA login + pages", flush=True)
    ta_cookie = login("/ta/auth", TA_EMAIL, TA_PASSWORD, lang)
    ta_pages = [
        ("07_ta_dashboard.png", "/ta/dashboard?tab=resume", 1100),
        ("08_ta_jobs.png", "/ta/dashboard?tab=jobs", 900),
        ("13_ta_applications.png", "/ta/dashboard?tab=applications", 800),
        ("14_ta_messages.png", "/ta/dashboard?tab=messages", 800),
        ("09_assistant.png", "/assistant", 1000),
        ("16_ta_register.png", "/ta/register.jsp", 800),
        ("17_ta_profile.png", "/ta/profile", 1000),
    ]
    for name, path, height in ta_pages:
        if path.endswith(".jsp"):
            chrome_screenshot_url(url_with_lang(path, lang), os.path.join(od, name), height=height)
        else:
            chrome_screenshot_auth(path, os.path.join(od, name), ta_cookie, height=height)

    print("  MO login + pages", flush=True)
    mo_cookie = login("/mo/auth", MO_EMAIL, MO_PASSWORD, lang)
    mo_pages = [
        ("10_mo_dashboard.png", "/mo/dashboard?tab=positions", 800),
        ("15_mo_post_job.png", "/mo/dashboard?tab=post", 900),
        ("19_mo_positions.png", "/mo/dashboard?tab=positions", 800),
        ("20_mo_messages.png", "/mo/dashboard?tab=messages", 800),
        ("21_mo_profile.png", "/mo/profile", 900),
        ("23_mo_register.png", "/mo/register.jsp", 800),
    ]
    for name, path, height in mo_pages:
        if path.endswith(".jsp"):
            chrome_screenshot_url(url_with_lang(path, lang), os.path.join(od, name), height=height)
        else:
            chrome_screenshot_auth(path, os.path.join(od, name), mo_cookie, height=height)
    jid = first_job_id()
    if jid:
        chrome_screenshot_auth(
            f"/mo/job-applicants?jobId={jid}",
            os.path.join(od, "11_mo_applicants.png"),
            mo_cookie,
            height=1200,
        )
        chrome_screenshot_auth(
            f"/mo/dashboard?tab=edit&jobId={jid}",
            os.path.join(od, "22_mo_edit_job.png"),
            mo_cookie,
            height=900,
        )
    tid = first_forum_thread_id()
    if tid:
        chrome_screenshot_url(
            url_with_lang(f"/forum?threadId={tid}", lang),
            os.path.join(od, "18_forum_thread.png"),
            height=900,
        )

    print("  TA feature extras", flush=True)
    chrome_screenshot_auth(
        "/ta/profile", os.path.join(od, "25_ta_profile_edit.png"), ta_cookie, height=2000
    )
    chrome_screenshot_url(
        url_with_lang("/ta/dashboard?tab=jobs", lang),
        os.path.join(od, "27_ta_guest_jobs.png"),
        height=900,
    )
    chrome_screenshot_auth(
        "/ta/dashboard?tab=jobs&jobSort=match_desc",
        os.path.join(od, "26_ta_jobs_match.png"),
        ta_cookie,
        height=1000,
    )
    chrome_screenshot_auth(
        "/ta/dashboard?tab=resume",
        os.path.join(od, "33_ta_resume_ai.png"),
        ta_cookie,
        height=1500,
    )
    chrome_screenshot_url(
        url_with_lang("/ta/register_confirm.jsp", lang),
        os.path.join(od, "34_ta_register_confirm.png"),
        height=700,
    )
    chrome_screenshot_auth("/forum", os.path.join(od, "31_forum_new_post.png"), ta_cookie, height=1600)

    print("  MO feature extras", flush=True)
    jid = first_job_id()
    if jid:
        chrome_screenshot_auth(
            f"/mo/job-applicants?jobId={jid}&filter=pending",
            os.path.join(od, "28_mo_schedule_interview.png"),
            mo_cookie,
            height=1400,
        )
    confirm_js = (
        "document.addEventListener('DOMContentLoaded',function(){"
        "var f=document.getElementById('mo-create-job-form');"
        "if(f){var t=f.querySelector('[name=title]');if(t)t.value='Data Structures TA';}"
        "var m=document.getElementById('moPostConfirmModal');"
        "if(m){m.classList.add('mo-job-en-modal--open');m.setAttribute('aria-hidden','false');"
        "document.body.classList.add('mo-job-en-modal-open');}});"
    )
    chrome_screenshot_auth(
        "/mo/dashboard?tab=post",
        os.path.join(od, "30_mo_post_confirm.png"),
        mo_cookie,
        height=1000,
        inject_js=confirm_js,
    )
    print("  29_mo_post_english_error.png", flush=True)
    mo_post_english_error_via_server(mo_cookie, od)

    print("  Admin login + workload", flush=True)
    admin_cookie = login("/admin/auth", ADMIN_EMAIL, ADMIN_PASSWORD, lang)
    chrome_screenshot_auth("/admin/workload", os.path.join(od, "12_admin_workload.png"), admin_cookie, height=1000)
    detail_js = (
        "document.addEventListener('DOMContentLoaded',function(){"
        "var d=document.getElementById('wtab-detail');if(d)d.checked=true;});"
    )
    chrome_screenshot_auth(
        "/admin/workload",
        os.path.join(od, "32_admin_workload_detail.png"),
        admin_cookie,
        height=1100,
        inject_js=detail_js,
    )
    chrome_screenshot_url(
        url_with_lang("/admin/register.jsp", lang),
        os.path.join(od, "24_admin_register.png"),
        height=800,
    )


def main():
    if not wait_server():
        print("Server not ready at", BASE, file=sys.stderr)
        sys.exit(1)
    for lang in LOCALES:
        capture_locale(lang)
    print("\nDone:", OUT_ROOT)


if __name__ == "__main__":
    main()
