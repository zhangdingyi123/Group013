# TA Recruitment System · User Manual

**Group 013 · BUPT International School · EBU6304**

This manual is for end users (TA applicants, module organisers MO, administrators). Screenshots are **bilingual side-by-side** (left: Chinese, right: English; switch via header or `?lang=zh` / `?lang=en`).

---

## 0. Introduction

### 0.1 What the system does

Online posting and applications for **course TAs, invigilation, event support**, including:

- **Jobs**: MO posts jobs; TA browses, filters, and applies.
- **Résumé and matching**: rule-based scoring + keyword extraction; optional embedding semantic match (§3.2, §4.7).
- **English job text**: MO title, description, and skills must be English (client + server validation, §4.3).
- **Application lifecycle**: pending → interview → accepted / rejected / cancelled.
- **Communication**: forum, DMs, friend requests.
- **Assistant (optional)**: multi-model chat, résumé context, quota, scope guard (§3.4).
- **Résumé workspace**: upload/edit, match hints, in-page AI polish (§3.2).
- **Interview**: MO schedules; TA confirm / decline / reschedule (§3.3, §4.4).
- **Admin workload**: statistics, details, transfer / cancel assignments (§5.2).

### 0.2 Roles

| Role | User | Main tasks |
|------|------|------------|
| **TA (applicant)** | Student | Profile, résumé, browse/apply, interview response, DMs |
| **MO** | Teacher / module lead | Post/edit/close jobs, screen applicants, interview, hire/reject |
| **Admin** | School admin | Global workload view and assignment adjustments |

> Use **separate login sessions** per role. **Log out** before switching roles in the same browser.

### 0.3 Typical flow

```
MO posts job → TA applies → MO screens (match score)
    → interview / hire / reject → TA responds in “My applications”
    → accepted rows appear in admin workload
```

### 0.4 Screenshots

- Left = Chinese UI, right = English UI.
- **34** bilingual images (01–34) cover auth, dashboards, personal centre, forum, assistant, admin, and key dialogs.

### 0.5 Feature index

| Module | Features | Entry | Screenshots |
|--------|----------|-------|-------------|
| Auth | TA/MO/Admin login, register, logout, 30 min session | `/ta/auth`, `/mo/auth`, `/admin/auth` | 03–05, 16, 23–24 |
| TA profile | Name, ID, phone, skills, completeness | `/ta/profile` | 17 |
| TA résumé | Upload txt/pdf/doc/docx, edit, download, match hints, AI polish | Dashboard · Résumé | 07, 33 |
| TA jobs | List, filter, hide applied, match %, apply | Dashboard · Open jobs | 08, 26, 27 |
| TA applications | Status, cancel, interview actions | Dashboard · My applications | 13 |
| TA messages | DM MO, unread, friend requests | Dashboard · Messages | 14 |
| MO jobs | Post (English), edit, close, list | Dashboard · My jobs / Post | 10, 15, 19, 22 |
| MO screening | Match score, gaps, stats, hire/reject/interview | `/mo/job-applicants` | 11, 28 |
| MO messages | DM TA | Dashboard · Messages | 20 |
| Forum | List, search, post, thread detail | `/forum` | 06, 18, 31 |
| Assistant | Chat, résumé upload, quota | `/assistant` | 09 |
| Admin | Workload, transfer/cancel | `/admin/workload` | 12, 32 |
| i18n | zh/en | Header / `?lang=` | All |

---

## 1. Access

| Deployment | URL |
|------------|-----|
| Dev (`mvn tomcat7:run`) | `http://localhost:8082/` |
| Standalone Tomcat | `http://{host}:{port}/ta-recruitment/` |

**Language**: header **中文 / English**, or `?lang=zh` / `?lang=en` (sets cookie).

**Session**: ~**30 minutes** idle timeout. After logout, do not rely on browser Back for sensitive pages.

**Data**: JSON under deployment `data/`—do not delete arbitrarily.

**Demo accounts**

| Role | Email | Password |
|------|-------|----------|
| TA | `liuchen@bupt-demo.edu.cn` | `demo123` |
| MO | `li.prof@bupt-demo.edu.cn` | `demo123` |
| Admin | `admin@bupt-demo.edu.cn` | `admin123` |

---

## 2. Public pages

### 2.1 Home

Visitor entry to TA/MO flows, forum, assistant, personal centre. Apply, post, and DM require login.

![Home](user_manual/bilingual/01_home.png)

### 2.2 Personal centre

`/personal-center`: logged-in users route to TA/MO/Admin workspace; guests choose role and go to login.

![Personal centre](user_manual/bilingual/02_personal_center.png)

### 2.3 Forum (list)

`/forum`: browse threads; post/reply need login. Search, sort, pagination.

![Forum list](user_manual/bilingual/06_forum.png)

New thread (logged in):

![Forum new post](user_manual/bilingual/31_forum_new_post.png)

### 2.4 Thread detail

`/forum?threadId=...`: body, replies, post reply when logged in.

![Forum thread](user_manual/bilingual/18_forum_thread.png)

---

## 3. Applicant (TA)

### 3.1 Login and register

| Page | Path |
|------|------|
| Login | `/ta/auth` |
| Register | `/ta/register.jsp` |
| Confirmation | After successful registration |

Register: name, student ID, email, password, **skill tags** (used in matching).

![TA login](user_manual/bilingual/03_ta_login.png)

![TA register](user_manual/bilingual/16_ta_register.png)

![TA register confirm](user_manual/bilingual/34_ta_register_confirm.png)

### 3.2 Dashboard — résumé and skills

`/ta/dashboard` tabs: **Résumé**, **Open jobs**, **My applications**, **Messages**.

| Action | Description |
|--------|-------------|
| Upload | `.txt`, `.pdf`, `.doc`, `.docx`; text used for MO matching |
| Edit/paste | Edit saved txt or paste as text résumé |
| Download | `/ta/resume` |
| Match strengths/gaps | vs all open jobs’ required skills |
| AI polish | Kimi/Qwen/OpenAI on saved résumé; copy back manually |
| Skill tags | Maintain in personal centre (§3.6) |

**Match score (0–100)**

| Source | Use |
|--------|-----|
| Skill tags | vs job `requiredSkills` |
| Résumé text | keyword/synonym rules |
| Rule score | (matched ÷ required) × 100; empty required → 100 |
| Semantic (optional) | embeddings + cosine similarity; cache in `embeddings.json` |

![TA dashboard · résumé](user_manual/bilingual/07_ta_dashboard.png)

![Résumé AI polish](user_manual/bilingual/33_ta_resume_ai.png)

### 3.3 Open jobs and applications

**Open jobs**: filter by keyword, type, skill; sort; match %; one application per job.

Guests can browse; apply requires login:

![Guest open jobs](user_manual/bilingual/27_ta_guest_jobs.png)

![Open jobs · match sort](user_manual/bilingual/26_ta_jobs_match.png)

**My applications**:

| Status | Meaning | TA actions |
|--------|---------|------------|
| Pending | Submitted | Cancel |
| Interview | MO scheduled | Confirm / decline / reschedule |
| Accepted / Rejected | Final | View |
| Cancelled | Withdrawn | — |

![Open jobs](user_manual/bilingual/08_ta_jobs.png)

![My applications](user_manual/bilingual/13_ta_applications.png)

### 3.4 Intelligent assistant

`/assistant`: multi-turn chat, résumé context, scope guard, monthly quota, optional WeChat top-up. Chat does not affect default rule match score (embeddings use a separate API if enabled).

![Assistant](user_manual/bilingual/09_assistant.png)

### 3.5 Messages and friends

DM MO; unread badges; accept MO friend requests in Messages tab.

![TA messages](user_manual/bilingual/14_ta_messages.png)

### 3.6 TA personal centre

`/ta/profile`: profile overview, completeness %, edit name/ID/phone/skills, shortcuts to dashboard tabs.

![TA profile](user_manual/bilingual/17_ta_profile.png)

![TA edit profile](user_manual/bilingual/25_ta_profile_edit.png)

---

## 4. Module organiser (MO)

### 4.1 Login and register

| Page | Path |
|------|------|
| Login | `/mo/auth` |
| Register | `/mo/register.jsp` |

MO, TA, and Admin sessions are independent—log out before switching roles.

![MO login](user_manual/bilingual/04_mo_login.png)

![MO register](user_manual/bilingual/23_mo_register.png)

### 4.2 My jobs

`/mo/dashboard` → **My jobs**: list, edit open jobs, screen applicants, close job.

![MO dashboard](user_manual/bilingual/10_mo_dashboard.png)

![MO job list](user_manual/bilingual/19_mo_positions.png)

### 4.3 Post job

**English only** for title, description, and required skills. Confirm dialog before submit. CJK/Cyrillic/Arabic etc. blocked (browser + server). No auto-translation.

![Post confirm dialog](user_manual/bilingual/30_mo_post_confirm.png)

![English validation error](user_manual/bilingual/29_mo_post_english_error.png)

![Post job](user_manual/bilingual/15_mo_post_job.png)

### 4.4 Edit job

`/mo/dashboard?tab=edit&jobId=...` — open jobs only; same English rules.

![Edit job](user_manual/bilingual/22_mo_edit_job.png)

### 4.5 MO messages

![MO messages](user_manual/bilingual/20_mo_messages.png)

### 4.6 MO profile

![MO profile](user_manual/bilingual/21_mo_profile.png)

### 4.7 Applicants and matching

`/mo/job-applicants?jobId=...`: match score, matched skills, gaps, cohort stats, sort (match desc; tie-break by lower current workload).

![Schedule interview](user_manual/bilingual/28_mo_schedule_interview.png)

![Applicant list](user_manual/bilingual/11_mo_applicants.png)

---

## 5. Administrator

### 5.1 Login and register

![Admin login](user_manual/bilingual/05_admin_login.png)

![Admin register](user_manual/bilingual/24_admin_register.png)

### 5.2 Workload

`/admin/workload`: per-TA accepted count, avg/min/max, high/low hints, expand details, transfer/cancel (demo environment—add approval in production).

![Workload](user_manual/bilingual/12_admin_workload.png)

![Workload details](user_manual/bilingual/32_admin_workload_detail.png)

---

## 6. FAQ

| Issue | Notes |
|-------|-------|
| Wrong role after login | Log out; do not mix TA/MO/Admin in one browser session |
| Apply disabled | Already applied, job closed, or not logged in |
| Score 100 but gaps shown | Job may have no required skills |
| “Not English” on post | Remove non-Latin scripts from title/description/skills |
| Low match after upload | Align skill tags and résumé keywords with job skills (English) |
| Assistant silent | Check API keys in `assistant.properties` |
| Data missing | Do not delete deployment `data/` |
| Cannot post on forum | Login required; check `data/` writable |

---

## 7. Related documents

| Document | Path |
|----------|------|
| Setup and run | `ReadMe.md`, `README.md` |
| Test cases | `test/TestCases.md` |
| PRD | `docs/PRD.md` |
| Unit tests | `test/java/` |
| JavaDoc | `mvn javadoc:javadoc` → `target/site/apidocs/` |

---

## Appendix: Regenerate screenshots

| Step | Command |
|------|---------|
| 1. Start app | `mvn tomcat7:run` |
| 2. Capture UI | `python3 docs/capture_ui_screenshots.py` |
| 3. Bilingual composites | `bash docs/prepare_user_manual_images.sh` |
| 4. Export Word | `pandoc docs/UserManual.md -o docs/submission_docx/UserManual.docx --resource-path="docs:docs/user_manual"` |

*34 screenshots (`01`–`34`). Regenerate after UI changes.*
