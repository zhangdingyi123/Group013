# Teaching Assistant Recruitment System · Beijing University of Posts and Telecommunications, International School

A lightweight Web application based on **Java Servlet / JSP** for TA application, job posting, and admission management. Data is persisted as **JSON and text files** in the `data/` directory under the application root, **no database dependency**, making it easy for course demonstration and local deployment.

---

## Feature Overview

### Applicant (TA)

- Register, login, and personal profile (name, student ID, skill tags, etc.)
- Resume: upload plain text, or use the assistant to extract content from PDF/Word and save it
- Browse jobs, submit applications, check application status
- Dashboard: tabs for profile, applications, messages, etc.
- **Discussion Forum**: browse, post, reply (login required)
- **Internal Messages** and **Friends**: establish friend relationships with other users and send private messages (with read status)

### Module Organiser (MO)

- Register, login
- Post and manage jobs (course TA, proctoring, event support, etc., with required skills)
- View applicant list per job: **skill match percentage**, **skill gaps**, admit/reject, close job
- Forum, internal messages, and friends (same data model as TA side)

### Administrator

- Login via `/admin/auth` (session validation)
- **`/admin/workload`**: view overall TA workload (number of admitted, average and extreme values, relative high/low indications), expand each TA's admitted jobs, **transfer admissions** to balance workload (requires prior communication in practice)
- Accessing the workload page without login redirects to admin login page

### Cross‑role & Home

- **`/personal-center`**: personal center entry; if logged in, redirect to the corresponding page for TA/MO/admin, otherwise go to role selection page
- **Smart Assistant** (`/assistant`): optional integration with LLM API (see below), supports conversation and resume-related capabilities; REST: `/api/assistant/chat`, `/api/assistant/extract-resume`

### Business Rules (built‑in logic, not external AI)

(No content originally – left empty as in source)

---

## Technology Stack

| Category | Description |
|----------|-------------|
| Runtime | Java 11, Maven |
| Web | Servlet 4.0, JSP, JSTL |
| JSON | Gson |
| Resume text extraction | Apache PDFBox, Apache POI (PDF/Word text extraction) |
| Packaging | `war`, default artifact name `ta-recruitment.war` |

---

## Data Storage

On application startup, `AppListener` sets the data directory to **`{Web application root}/data`** (consistent with the deployment context).

| File / Directory | Purpose |
|------------------|---------|
| `applicants.json` | Applicants |
| `module_organisers.json` | Module organisers |
| `admins.json` | Administrator accounts |
| `jobs.json` | Jobs |
| `applications.json` | Applications and admission status |
| `messages.json` | Internal messages |
| `dm_read_states.json` | Private message read states |
| `forum_threads.json` / `forum_replies.json` | Forum threads and replies |
| `friend_links.json` / `friend_requests.json` | Friend relationships and friend requests |
| `data/resumes/` | Resume text files |

Passwords are stored after **SHA-256** hashing into JSON; do not commit the `data/` directory containing real secrets or production data to public repositories.

---

## Smart Assistant (Optional)

Assistant‑related configuration is read by `AssistantConfig`, supporting **`assistant.properties` in the classpath**, or an external configuration file via environment variable **`ASSISTANT_PROPERTIES_PATH`** / JVM parameter **`-Dassistant.properties.path=`**; **environment variables can override** key‑type settings from the file.

It can integrate with **Moonshot Kimi**, **Alibaba Tongyi (OpenAI compatible)**, **OpenAI**, etc. (the specific model and Base URL depend on the configuration). When no API key is configured, the page shows a "not ready" message, and other recruitment functions are unaffected.

---

## Build & Run

### 1. Package WAR

```bash
mvn clean package
```

Artifact: `target/ta-recruitment.war`. Put the WAR into Tomcat's `webapps/`. The default access path is:

- **`http://{host}:{port}/ta-recruitment/`** (port depends on Tomcat's `server.xml`, typically `8080`)

### 2. Maven Embedded Tomcat (common for development)

```bash
mvn tomcat7:run
```

The current `pom.xml` sets the plugin context to **`/`** and default port **`8082`**, so the local root address is:

- **`http://localhost:8082/`**

If the port is occupied, you can specify a different port, e.g.:

```bash
mvn tomcat7:run -Dmaven.tomcat.port=8083
```

**Note**: The context path differs between embedded run and standalone Tomcat deployment (`/` vs. `/ta-recruitment/`). Bookmarks and links in the documentation must be adapted to the actual environment.

---

## Main URL Quick Reference

| Path | Description |
|------|-------------|
| `/` | Home |
| `/personal-center` | Personal center entry |
| `/ta/auth`, `/ta/dashboard`, `/ta/profile` | Applicant authentication and dashboard |
| `/mo/auth`, `/mo/dashboard`, `/mo/profile` | Module organiser authentication and dashboard |
| `/mo/job-applicants` | Job applicant management |
| `/admin/auth`, `/admin/workload` | Admin login and workload |
| `/forum` | Discussion forum |
| `/assistant` | Smart assistant page |

Remaining static pages and tab JSPs are forwarded by corresponding Servlets, as per the project's `src/main/webapp`.

---

## Source Code Structure (Summary)

```
src/main/java/com/bupt/ta/
  model/          # Domain models
  storage/        # Storage: JSON read/write and path
  service/        # Business services (including assistant subpackage)
  util/           # Password, session utilities
  web/            # Servlets, Listener, Filter
src/main/webapp/
  index.jsp, personal_center_gate.jsp, assistant.jsp, …
  ta/, mo/, admin/, forum/, css/
  data/           # Runtime data (located under application root after deployment)
  WEB-INF/web.xml
```

---

## Full Update Recommendation for Standalone Tomcat

When upgrading or troubleshooting JSP 500 errors, it is recommended to **replace the whole package** and **clean the JSP compilation cache** for this application under `work/` to avoid mixed versions caused by copying only a single class or a single JSP. Steps summary:

1. `mvn clean package -DskipTests`
2. Stop Tomcat
3. Delete `webapps/ta-recruitment/` and `webapps/ta-recruitment.war` (depending on your actual deployment)
4. Delete `work/Catalina/localhost/ta-recruitment/` (or the directory corresponding to the actual context name)
5. Copy the new WAR to `webapps/` and start Tomcat

---

## Notes & Limitations

- This system is intended for teaching and prototype demonstration; for production, additional auditing, HTTPS, backup, and finer‑grained permissions are required.
- Administrator and other role permissions are based on session and page logic; data files are plain JSON. Keep the deployment directory secure.