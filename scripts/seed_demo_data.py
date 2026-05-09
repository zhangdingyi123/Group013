#!/usr/bin/env python3
"""Regenerate webapp/data JSON + resume stubs (BUPT TA demo). Run from repo root."""
from __future__ import annotations

import base64
import hashlib
import json
import shutil
from datetime import datetime, timedelta, timezone
from pathlib import Path
from uuid import NAMESPACE_DNS, uuid5

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src" / "main" / "webapp" / "data"
RESUMES = DATA / "resumes"


def sha256_b64(s: str) -> str:
    d = hashlib.sha256(s.encode("utf-8")).digest()
    return base64.b64encode(d).decode("ascii")


def uid(key: str) -> str:
    return str(uuid5(NAMESPACE_DNS, f"bupt-ta-recruitment:{key}"))


def ts(dt: datetime) -> int:
    return int(dt.replace(tzinfo=timezone.utc).timestamp() * 1000)


def main() -> None:
    demo_pw = "demo123"
    pw_hash = sha256_b64(demo_pw)
    admin_pw_hash = sha256_b64("admin123")

    t0 = datetime(2026, 4, 1, 8, 0, 0)

    mo_ids = [uid(f"mo-{i}") for i in range(3)]
    admin_id = uid("admin-1")
    ta_keys = [
        ("刘晨", "2023210101", "liuchen@bupt-demo.edu.cn", "13800001001"),
        ("王雨桐", "2023210102", "wangyutong@bupt-demo.edu.cn", "13800001002"),
        ("张昊", "2023210103", "zhanghao@bupt-demo.edu.cn", "13800001003"),
        ("陈一宁", "2023210104", "chenyining@bupt-demo.edu.cn", "13800001005"),
        ("赵思远", "2023210201", "zhaosiyuan@bupt-demo.edu.cn", "13800001006"),
        ("孙嘉禾", "2023210202", "sunjiahe@bupt-demo.edu.cn", "13800001007"),
        ("周若曦", "2023210203", "zhouruoxi@bupt-demo.edu.cn", "13800001008"),
        ("吴子涵", "2023210204", "wuzihan@bupt-demo.edu.cn", "13800001009"),
    ]
    applicants = []
    for i, (name, sid, email, phone) in enumerate(ta_keys):
        aid = uid(f"ta-{sid}")
        skills_sets = [
            ["Java", "JSP/Servlet", "Communication", "Databases"],
            ["Python", "Machine Learning", "Technical Writing"],
            ["C++", "Operating Systems", "Data Structures"],
            ["Frontend", "JavaScript", "Communication"],
            ["Java", "Data Structures", "TA Experience"],
            ["Databases", "SQL", "Communication"],
            ["Machine Learning", "Python", "English"],
            ["Computer Networks", "Linux", "Technical Writing"],
        ]
        applicants.append(
            {
                "id": aid,
                "name": name,
                "email": email,
                "passwordHash": pw_hash,
                "studentId": sid,
                "phone": phone,
                "skills": skills_sets[i % len(skills_sets)],
                "resumePath": f"{aid}.txt",
                "createdAt": ts(t0 + timedelta(hours=i * 3)),
            }
        )

    module_organisers = [
        {
            "id": mo_ids[0],
            "name": "李教授",
            "email": "li.prof@bupt-demo.edu.cn",
            "passwordHash": pw_hash,
            "department": "计算机学院（软件工程系）",
            "createdAt": ts(t0 - timedelta(days=2)),
        },
        {
            "id": mo_ids[1],
            "name": "王副教授",
            "email": "wang.prof@bupt-demo.edu.cn",
            "passwordHash": pw_hash,
            "department": "人工智能学院",
            "createdAt": ts(t0 - timedelta(days=1)),
        },
        {
            "id": mo_ids[2],
            "name": "刘老师",
            "email": "liu.laoshi@bupt-demo.edu.cn",
            "passwordHash": pw_hash,
            "department": "信息与通信工程学院",
            "createdAt": ts(t0 - timedelta(hours=12)),
        },
    ]

    admins = [
        {
            "id": admin_id,
            "name": "系统管理员",
            "email": "admin@bupt-demo.edu.cn",
            "passwordHash": admin_pw_hash,
            "createdAt": ts(t0 - timedelta(days=3)),
        }
    ]

    job_defs = [
        ("软件工程课程助教", mo_ids[0], "批改作业、答疑与实验课辅导。", ["Java", "Communication"], "course_ta", "open", 0),
        ("Web 开发实践助教", mo_ids[0], "协助 Servlet/JSP 项目周与代码走查。", ["JSP/Servlet", "Java", "Communication"], "course_ta", "open", 1),
        ("数据结构实验助教", mo_ids[1], "上机课答疑、实验报告初评。", ["Data Structures", "C++", "Communication"], "course_ta", "open", 2),
        ("机器学习研讨课助教", mo_ids[1], "作业批改与论文阅读小组组织。", ["Machine Learning", "Python", "Technical Writing"], "course_ta", "open", 3),
        ("操作系统课程助教", mo_ids[2], "实验环境维护与课堂演示支持。", ["Operating Systems", "Linux", "C++"], "course_ta", "closed", 4),
        ("数据库系统助教", mo_ids[2], "SQL 练习课与在线题库维护。", ["Databases", "SQL", "Communication"], "course_ta", "open", 5),
        ("期中考试监考", mo_ids[0], "机房监考、签到与试卷分发。", ["Communication"], "invigilation", "open", 6),
        ("开放日志愿者", mo_ids[1], "展台引导与物料整理。", ["Communication", "Technical Writing"], "activity", "open", 7),
        ("毕业设计档案整理", mo_ids[2], "材料核对与系统录入。", ["Technical Writing", "Communication"], "activity", "open", 8),
    ]
    jobs = []
    job_ids: list[str] = []
    for title, moid, desc, skills, jtype, status, idx in job_defs:
        jid = uid(f"job-{idx}-{title}")
        job_ids.append(jid)
        jobs.append(
            {
                "id": jid,
                "title": title,
                "moduleOrganiserId": moid,
                "description": desc,
                "requiredSkills": skills,
                "type": jtype,
                "status": status,
                "createdAt": ts(t0 + timedelta(days=1, hours=idx * 2)),
            }
        )

    a = {r["studentId"]: r for r in applicants}

    def aid(sid: str) -> str:
        return a[sid]["id"]

    app_rows = [
        (aid("2023210101"), job_ids[0], "pending", "希望参与软件工程助教工作。", None, None, False, None),
        (aid("2023210102"), job_ids[2], "interview", "有数据结构课程高分经历。", ts(t0 + timedelta(days=20, hours=14)), "教三楼 401，试讲 20 分钟", False, "pending"),
        (aid("2023210103"), job_ids[2], "accepted", "熟悉 Linux 与 OS 实验。", None, None, False, None),
        (aid("2023210104"), job_ids[3], "rejected", "申请 ML 助教。", None, None, False, None),
        (aid("2023210201"), job_ids[1], "accepted", "可做 Web 全栈辅导。", None, None, False, None),
        (aid("2023210202"), job_ids[5], "pending", "擅长 SQL 与性能优化。", None, None, False, None),
        (aid("2023210203"), job_ids[6], "interview", "可参加周末监考。", ts(t0 + timedelta(days=22, hours=9)), "主校区教学楼 A101", True, "confirmed"),
        (aid("2023210204"), job_ids[7], "cancelled", "时间冲突，已撤销。", None, None, False, None),
        (aid("2023210101"), job_ids[6], "accepted", "可配合排班。", None, None, False, None),
        (aid("2023210102"), job_ids[7], "pending", "希望做活动志愿。", None, None, False, None),
    ]
    applications = []
    for i, (applicant_id, job_id, status, note, iv_at, iv_detail, iv_conf, iv_ta) in enumerate(app_rows):
        row = {
            "id": uid(f"application-{i}"),
            "applicantId": applicant_id,
            "jobId": job_id,
            "status": status,
            "note": note,
            "appliedAt": ts(t0 + timedelta(days=5, minutes=i * 25)),
            "interviewAt": iv_at or 0,
            "interviewDetail": iv_detail,
            "interviewConfirmed": iv_conf,
            "interviewTaStatus": iv_ta,
        }
        applications.append(row)

    messages = [
        {
            "id": uid("dm-1"),
            "applicantId": aid("2023210101"),
            "moduleOrganiserId": mo_ids[0],
            "senderRole": "ta",
            "body": "老师您好，我已投递软件工程助教岗位，请问本周是否有答疑安排？",
            "sentAt": ts(t0 + timedelta(days=6)),
            "jobId": job_ids[0],
        },
        {
            "id": uid("dm-2"),
            "applicantId": aid("2023210101"),
            "moduleOrganiserId": mo_ids[0],
            "senderRole": "mo",
            "body": "收到。本周四下午 office hour，欢迎带上简历中的项目说明来聊。",
            "sentAt": ts(t0 + timedelta(days=6, hours=2)),
            "jobId": job_ids[0],
        },
        {
            "id": uid("dm-3"),
            "applicantId": aid("2023210201"),
            "moduleOrganiserId": mo_ids[0],
            "senderRole": "ta",
            "body": "Web 实践助教这边我可以承担晚间在线答疑，请问排班一般怎么定？",
            "sentAt": ts(t0 + timedelta(days=7)),
            "jobId": job_ids[1],
        },
    ]

    dm_read_states = [
        {
            "applicantId": aid("2023210101"),
            "moduleOrganiserId": mo_ids[0],
            "taLastReadAt": ts(t0 + timedelta(days=6, hours=3)),
            "moLastReadAt": ts(t0 + timedelta(days=6, hours=3)),
        }
    ]

    thread1 = uid("forum-thread-1")
    thread2 = uid("forum-thread-2")
    thread3 = uid("forum-thread-3")

    forum_threads = [
        {
            "id": thread1,
            "title": "Servlet 部署后 404，大家怎么排查？",
            "body": "war 已部署但访问路径总 404，想确认 context path 与 web.xml 的 servlet-mapping。",
            "authorId": aid("2023210101"),
            "authorRole": "ta",
            "authorName": "刘晨",
            "createdAt": ts(t0 + timedelta(days=8)),
            "lastReplyAt": ts(t0 + timedelta(days=8, hours=5)),
            "replyCount": 2,
        },
        {
            "id": thread2,
            "title": "监考排班是否可以申请调换？",
            "body": "期中监考与另一门考试冲突，想问问调换流程。",
            "authorId": aid("2023210203"),
            "authorRole": "ta",
            "authorName": "周若曦",
            "createdAt": ts(t0 + timedelta(days=9)),
            "lastReplyAt": ts(t0 + timedelta(days=9)),
            "replyCount": 0,
        },
        {
            "id": thread3,
            "title": "本学年助教评优时间节点提醒",
            "body": "请各位课程组织者关注学院教务通知，材料提交截止 6 月 15 日。",
            "authorId": mo_ids[1],
            "authorRole": "mo",
            "authorName": "王副教授",
            "createdAt": ts(t0 + timedelta(days=10)),
            "lastReplyAt": ts(t0 + timedelta(days=10, hours=2)),
            "replyCount": 1,
        },
    ]

    forum_replies = [
        {
            "id": uid("forum-reply-1"),
            "threadId": thread1,
            "authorId": aid("2023210201"),
            "authorRole": "ta",
            "authorName": "赵思远",
            "body": "先看 Tomcat manager 里应用是否 running，再看 URL 是否带了正确的 context。",
            "createdAt": ts(t0 + timedelta(days=8, hours=2)),
        },
        {
            "id": uid("forum-reply-2"),
            "threadId": thread1,
            "authorId": mo_ids[0],
            "authorRole": "mo",
            "authorName": "李教授",
            "body": "把 server log 里第一条 Caused by 贴出来会更快定位。",
            "createdAt": ts(t0 + timedelta(days=8, hours=5)),
        },
        {
            "id": uid("forum-reply-3"),
            "threadId": thread3,
            "authorId": admin_id,
            "authorRole": "admin",
            "authorName": "系统管理员",
            "body": "系统内申请记录可作为附件之一导出。",
            "createdAt": ts(t0 + timedelta(days=10, hours=2)),
        },
    ]

    friend_links = [
        {
            "applicantId": aid("2023210201"),
            "moduleOrganiserId": mo_ids[0],
            "createdAt": ts(t0 + timedelta(days=4)),
        }
    ]

    friend_requests = [
        {
            "id": uid("fr-1"),
            "applicantId": aid("2023210103"),
            "moduleOrganiserId": mo_ids[2],
            "fromRole": "ta",
            "status": "pending",
            "createdAt": ts(t0 + timedelta(days=3)),
        },
        {
            "id": uid("fr-2"),
            "applicantId": aid("2023210102"),
            "moduleOrganiserId": mo_ids[1],
            "fromRole": "mo",
            "status": "pending",
            "createdAt": ts(t0 + timedelta(days=2)),
        },
    ]

    assistant_usage: list[dict] = []

    # --- write ---
    if RESUMES.exists():
        for p in RESUMES.iterdir():
            if p.is_file():
                p.unlink()
    else:
        RESUMES.mkdir(parents=True, exist_ok=True)

    def dump(name: str, obj) -> None:
        path = DATA / name
        path.write_text(json.dumps(obj, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    dump("admins.json", admins)
    dump("module_organisers.json", module_organisers)
    dump("applicants.json", applicants)
    dump("jobs.json", jobs)
    dump("applications.json", applications)
    dump("messages.json", messages)
    dump("dm_read_states.json", dm_read_states)
    dump("forum_threads.json", forum_threads)
    dump("forum_replies.json", forum_replies)
    dump("friend_links.json", friend_links)
    dump("friend_requests.json", friend_requests)
    dump("assistant_usage.json", assistant_usage)

    emb_path = DATA / "embeddings.json"
    if emb_path.exists():
        emb_path.write_text("[]\n", encoding="utf-8")

    resume_template = (
        "姓名：{name}\n学号：{sid}\n邮箱：{email}\n\n教育背景：北京邮电大学 本科在读\n\n"
        "技能：{skills}\n\n项目经历：课程设计「小型招聘系统」—— Servlet/JSP + JSON 存储；"
        "熟悉 Git 协作与单元测试基础。\n\n可工作时间：工作日晚上与周末部分时段。\n"
    )
    for r in applicants:
        text = resume_template.format(
            name=r["name"],
            sid=r["studentId"],
            email=r["email"],
            skills="、".join(r["skills"]),
        )
        (RESUMES / r["resumePath"]).write_text(text, encoding="utf-8")

    print("OK: regenerated data under", DATA)
    print("Demo passwords: TA/MO = demo123 | Admin = admin123")


if __name__ == "__main__":
    main()
