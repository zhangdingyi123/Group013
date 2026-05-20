# TA Recruitment System — ReadMe

**BUPT International School · EBU6304 Software Engineering · Group 013**

| Item | Description |
|------|-------------|
| System name | TA Recruitment System |
| Technology | Java 11 + Servlet/JSP + Maven; JSON file storage (no database) |
| This document | **Setup, configuration, and run instructions** (course submission ReadMe) |
| Related docs | User manual `docs/UserManual.md`; overview `README.md` |

---

## 1. Build

### 1.1 Requirements

| Component | Version | Notes |
|-----------|---------|-------|
| **JDK** | **11+** | Matches `pom.xml` compiler target |
| **Apache Maven** | 3.6+ | Build, run, test |
| **Browser** | Chrome / Edge / Firefox | Web UI |
| **Tomcat** (optional) | 9.x / 10.x | Only for standalone WAR |

Check environment:

```bash
java -version
mvn -version
```

Install JDK 11 and set `JAVA_HOME` if needed (macOS: `brew install openjdk@11`).

### 1.2 Source layout

After clone or unzip, enter the project root:

```bash
cd /path/to/project-root
```

| Path | Contents |
|------|----------|
| `src/main/java` | Java business and web layer |
| `src/main/webapp` | JSP, static assets, runtime `data/` |
| `src/main/resources` | i18n, assistant config |
| `test/java` | JUnit 5 unit tests |
| `pom.xml` | Dependencies and build |

### 1.3 Compile and package

```bash
mvn clean package
mvn test    # optional
```

| Output | Path |
|--------|------|
| Deployable WAR | `target/ta-recruitment.war` |
| Compiled classes | `target/classes/` |

**Dependencies**: Servlet 4.0, JSP, JSTL, Gson, Apache PDFBox, Apache POI.

---

## 2. Configuration

### 2.1 Data directory (required)

`AppListener` sets **`{webapp root}/data`** automatically—no manual database setup.

| Deployment | `data/` location |
|------------|------------------|
| `mvn tomcat7:run` | `src/main/webapp/data/` |
| Tomcat + `ta-recruitment.war` | `webapps/ta-recruitment/data/` |

| File / directory | Purpose |
|------------------|---------|
| `applicants.json` | Applicant accounts and profiles |
| `module_organisers.json` | Module organisers |
| `admins.json` | Administrators |
| `jobs.json` | Job postings |
| `applications.json` | Applications and hiring status |
| `messages.json`, `dm_read_states.json` | DMs and read state |
| `forum_threads.json`, `forum_replies.json` | Forum |
| `friend_links.json`, `friend_requests.json` | Friends |
| `data/resumes/` | Résumé text files |
| `embeddings.json` (optional) | Semantic match cache |

Passwords use **SHA-256** hashing. Demo data is included; do not commit real private production data to public repositories.

### 2.2 Intelligent assistant (optional)

Default: `src/main/resources/assistant.properties`.

| Method | Description |
|--------|-------------|
| Environment variables | `KIMI_API_KEY`, `QWEN_API_KEY`, `OPENAI_API_KEY`, etc. (**preferred**) |
| External file | `ASSISTANT_PROPERTIES_PATH` or `-Dassistant.properties.path=absolute/path` |

**Without API keys**: hiring, applications, forum, etc. work; only the assistant page is disabled.

| Setting | Purpose | Default |
|---------|---------|---------|
| `assistant.default.provider` | kimi / qwen / openai | Auto from configured keys |
| `assistant.strict.scope` | Block off-topic questions | On |
| `assistant.monthly.free.quota` | Free chats per user per month | 30 |
| `assistant.topup.code` | Redeem code top-up | Empty = disabled |
| `assistant.pay.wechat.*` | WeChat Native pay | See properties comments |
| `match.semantic.enabled` | Embedding semantic match | **false** |

### 2.3 UI language

Cookie `ui_lang` and `LocaleFilter`; header button or `?lang=zh` / `?lang=en`.

### 2.4 Port and URL

| Run mode | URL | Context |
|----------|-----|---------|
| Embedded Tomcat | `http://localhost:8082/` | `/` |
| Standalone Tomcat | `http://localhost:8080/ta-recruitment/` | `/ta-recruitment/` |

If port 8082 is busy:

```bash
mvn tomcat7:run -Dmaven.tomcat.port=8083
```

---

## 3. Run

### 3.1 Recommended: Maven embedded Tomcat

No separate Tomcat install; good for development and viva demos.

```bash
cd /path/to/project-root
mvn clean package
mvn tomcat7:run    # Ctrl+C to stop
```

Browser: **http://localhost:8082/**

### 3.2 Alternative: standalone Tomcat

1. Install Tomcat (`docs/InstallTomcat.md`).
2. Copy `target/ta-recruitment.war` to Tomcat `webapps/`.
3. Run `bin/startup.sh` (Windows: `startup.bat`).
4. Open: **http://localhost:8080/ta-recruitment/**.

To update: stop service → remove old `webapps/ta-recruitment*` and `work/Catalina/localhost/ta-recruitment/` → deploy new WAR → restart.

### 3.3 Demo accounts

| Role | Email | Password |
|------|-------|----------|
| TA (applicant) | liuchen@bupt-demo.edu.cn | demo123 |
| MO | li.prof@bupt-demo.edu.cn | demo123 |
| Admin | admin@bupt-demo.edu.cn | admin123 |

Entry: home → **Personal centre** (`/personal-center`), or `/ta/auth`, `/mo/auth`, `/admin/auth`.

### 3.4 Main entry points

| URL | Function |
|-----|----------|
| `/` | Home |
| `/personal-center` | Personal centre (role routing) |
| `/ta/dashboard` | TA dashboard |
| `/mo/dashboard` | MO dashboard |
| `/mo/job-applicants` | Applicants and skill matching |
| `/admin/workload` | Admin workload |
| `/forum` | Forum |
| `/assistant` | Intelligent assistant |

### 3.5 Testing

```bash
mvn test    # JUnit; no Tomcat or external AI required
```

Manual cases: `test/TestCases.md`.

### 3.6 FAQ

| Symptom | Solution |
|---------|----------|
| Port 8082 in use | `mvn tomcat7:run -Dmaven.tomcat.port=8083` |
| `mvn` not found | Install Maven and add to PATH |
| Tomcat 404 | Confirm WAR deployed; URL includes `/ta-recruitment/` |
| Page 500 or odd UI | Clear Tomcat `work/` cache and redeploy |
| Assistant unavailable | Check API keys; expected if unset; core hiring unaffected |

---

## 4. Deliverables index

| Deliverable | Path |
|-------------|------|
| ReadMe (this file) | `ReadMe.md` |
| Project overview | `README.md` |
| User manual | `docs/UserManual.md` |
| Test programs | `test/java/` |
| Test documentation | `test/TestDocumentation.md` |
| Test cases | `test/TestCases.md` |
| JavaDoc | `mvn javadoc:javadoc` → `target/site/apidocs/` |

---

*Synced with Group 013 TA Recruitment System source. For teaching demos only; production requires stronger security and operations.*
