#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import base64
import hashlib
import json
import os
import random
import uuid
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from pathlib import Path


TZ_SHANGHAI = timezone(timedelta(hours=8))


def sha256_base64(plain: str) -> str:
    # Must match com.bupt.ta.util.PasswordUtil (SHA-256 + Base64)
    digest = hashlib.sha256(plain.encode("utf-8")).digest()
    return base64.b64encode(digest).decode("ascii")


def ms(dt: datetime) -> int:
    return int(dt.timestamp() * 1000)


@dataclass(frozen=True)
class PasswordBundle:
    admin_plain: str
    admin_hash: str
    mo_plain: str
    mo_hash: str
    ta_plain: str
    ta_hash: str


def ids(seed: str, prefix: str, name: str) -> str:
    return str(uuid.uuid5(uuid.NAMESPACE_DNS, f"{seed}:{prefix}:{name}"))


def write_json(path: Path, data) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")


def write_text(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate mock data for ta-recruitment (JSON storage).")
    parser.add_argument(
        "--out",
        default="src/main/webapp/data",
        help="Output data directory (default: src/main/webapp/data)",
    )
    parser.add_argument("--seed", default="group013-mock-v1", help="Deterministic seed string")
    parser.add_argument(
        "--lang",
        default="en",
        choices=["en", "zh"],
        help="Language for generated text fields (default: en)",
    )
    parser.add_argument("--applicants", type=int, default=12, help="Number of applicants (TA)")
    parser.add_argument("--mos", type=int, default=4, help="Number of module organisers (MO)")
    parser.add_argument("--jobs", type=int, default=10, help="Number of jobs")
    parser.add_argument("--threads", type=int, default=4, help="Forum thread count")
    parser.add_argument(
        "--clean-resumes",
        action="store_true",
        help="Remove existing files in out/resumes that are not generated in this run",
    )
    args = parser.parse_args()

    out_dir = Path(args.out)
    rng = random.Random(args.seed)

    pw = PasswordBundle(
        admin_plain="admin123",
        admin_hash=sha256_base64("admin123"),
        mo_plain="mo123456",
        mo_hash=sha256_base64("mo123456"),
        ta_plain="123456",
        ta_hash=sha256_base64("123456"),
    )

    t0 = datetime(2026, 4, 1, 9, 0, tzinfo=TZ_SHANGHAI)
    is_en = args.lang == "en"

    # ---------- Admins ----------
    admins = [
        {
            "id": ids(args.seed, "admin", "sys"),
            "name": "System Admin" if is_en else "系统管理员",
            "email": "admin@demo.com",
            "passwordHash": pw.admin_hash,
            "createdAt": ms(t0),
        },
        {
            "id": ids(args.seed, "admin", "ops"),
            "name": "Ops Admin" if is_en else "运营管理员",
            "email": "ops@demo.com",
            "passwordHash": pw.admin_hash,
            "createdAt": ms(t0 + timedelta(minutes=20)),
        },
    ]

    # ---------- MOs ----------
    mo_names = (
        [
            ("Prof. Li", "li.laoshi@demo.com", "School of Computer Science"),
            ("Prof. Wang", "wang.laoshi@demo.com", "School of Cyber Security"),
            ("Prof. Zhang", "zhang.laoshi@demo.com", "International School"),
            ("Prof. Chen", "chen.laoshi@demo.com", "School of AI"),
            ("Prof. Zhao", "zhao.laoshi@demo.com", "Software Engineering Dept."),
            ("Prof. Sun", "sun.laoshi@demo.com", "School of Information & Communication"),
        ]
        if is_en
        else [
            ("李老师", "li.laoshi@demo.com", "计算机学院"),
            ("王老师", "wang.laoshi@demo.com", "网络空间安全学院"),
            ("张老师", "zhang.laoshi@demo.com", "国际学院"),
            ("陈老师", "chen.laoshi@demo.com", "人工智能学院"),
            ("赵老师", "zhao.laoshi@demo.com", "软件工程系"),
            ("孙老师", "sun.laoshi@demo.com", "信息与通信工程学院"),
        ]
    )
    rng.shuffle(mo_names)
    mos = []
    for i in range(max(1, args.mos)):
        name, email, dept = mo_names[i % len(mo_names)]
        mos.append(
            {
                "id": ids(args.seed, "mo", email),
                "name": name,
                "email": email,
                "passwordHash": pw.mo_hash,
                "department": dept,
                "createdAt": ms(t0 + timedelta(hours=1 + i)),
            }
        )

    # ---------- Applicants (TA) ----------
    skill_pool = (
        [
            "Java",
            "Python",
            "C++",
            "Data Structures",
            "Operating Systems",
            "Databases",
            "Computer Networks",
            "Machine Learning",
            "Frontend",
            "Invigilation",
            "TA Experience",
            "Communication",
            "Technical Writing",
            "JSP/Servlet",
        ]
        if is_en
        else [
            "Java",
            "Python",
            "C++",
            "数据结构",
            "操作系统",
            "数据库",
            "网络",
            "机器学习",
            "前端",
            "监考",
            "助教经验",
            "沟通",
            "文档写作",
            "JSP/Servlet",
        ]
    )
    ta_names = (
        [
            ("Alex Chen", "alexchen"),
            ("Bella Li", "bellali"),
            ("Chris Wang", "chriswang"),
            ("Daisy Zhang", "daisyzhang"),
            ("Ethan Zhao", "ethanzhao"),
            ("Fiona Sun", "fionasun"),
            ("George Zhou", "georgezhou"),
            ("Hannah Wu", "hannahwu"),
            ("Ian Zheng", "ianzheng"),
            ("Joy Feng", "joyfeng"),
            ("Kevin Jiang", "kevinjiang"),
            ("Lily Tang", "lilytang"),
            ("Miles He", "mileshe"),
            ("Nina Gao", "ninagao"),
            ("Owen Lin", "owenlin"),
            ("Paula Xu", "paulaxu"),
        ]
        if is_en
        else [
            ("张晨", "zhangchen"),
            ("李雪", "lixue"),
            ("王浩", "wanghao"),
            ("陈雨", "chenyu"),
            ("赵磊", "zhaolei"),
            ("孙婷", "sunting"),
            ("周杰", "zhoujie"),
            ("吴敏", "wumin"),
            ("郑凯", "zhengkai"),
            ("冯悦", "fengyue"),
            ("蒋宁", "jiangning"),
            ("唐可", "tangke"),
            ("何欣", "hexin"),
            ("高远", "gaoyuan"),
            ("林然", "linran"),
            ("许诺", "xunuo"),
        ]
    )
    rng.shuffle(ta_names)

    applicants = []
    resumes_dir = out_dir / "resumes"
    expected_resume_files = set()
    for i in range(max(1, args.applicants)):
        name, user = ta_names[i % len(ta_names)]
        applicant_id = ids(args.seed, "ta", user)
        email = f"{user}@demo.com"
        student_id = f"2024{(1000 + i):04d}"
        phone = f"13{rng.randint(0,9)}-{rng.randint(1000,9999)}-{rng.randint(1000,9999)}"
        skills = rng.sample(skill_pool, k=rng.randint(3, 6))
        resume_filename = f"{applicant_id}.txt"
        resume_path = f"{resume_filename}"
        expected_resume_files.add(resume_filename)
        applicants.append(
            {
                "id": applicant_id,
                "name": name,
                "email": email,
                "passwordHash": pw.ta_hash,
                "studentId": student_id,
                "phone": phone,
                "skills": skills,
                "resumePath": resume_path,
                "createdAt": ms(t0 + timedelta(days=1, minutes=10 * i)),
            }
        )
        if is_en:
            resume_body = "\n".join(
                [
                    f"Name: {name}",
                    f"Student ID: {student_id}",
                    f"Email: {email}",
                    f"Phone: {phone}",
                    "",
                    "Skills:",
                    " - " + "\n - ".join(skills),
                    "",
                    "Experience:",
                    " - Course project: Web app development with Java Servlet/JSP",
                    " - Coursework: Data Structures, Operating Systems, Databases, Computer Networks",
                    "",
                    "Notes: Available for interview/teaching demo; proactive communicator.",
                ]
            )
        else:
            resume_body = "\n".join(
                [
                    f"姓名：{name}",
                    f"学号：{student_id}",
                    f"邮箱：{email}",
                    f"电话：{phone}",
                    "",
                    "技能：",
                    " - " + "\n - ".join(skills),
                    "",
                    "经历：",
                    " - 课程项目：基于 Java Servlet/JSP 的 Web 应用开发",
                    " - 课程：数据结构、操作系统、数据库、计算机网络",
                    "",
                    "备注：可配合面试/试讲时间，沟通顺畅。",
                ]
            )
        write_text(resumes_dir / resume_filename, resume_body)

    if args.clean_resumes and resumes_dir.is_dir():
        for p in resumes_dir.iterdir():
            if not p.is_file():
                continue
            if p.name in expected_resume_files:
                continue
            try:
                p.unlink()
            except OSError:
                pass

    # ---------- Jobs ----------
    job_templates = (
        [
            (
                "Data Structures TA",
                "Help with grading, Q&A sessions, and maintaining practice problems.",
                "course_ta",
                ["Data Structures", "Java", "Communication"],
            ),
            (
                "Operating Systems TA",
                "Support lab sessions, grade assignments, and assist in-class.",
                "course_ta",
                ["Operating Systems", "C++", "Technical Writing"],
            ),
            (
                "Database Systems TA",
                "Review SQL exercises and provide help in course channels.",
                "course_ta",
                ["Databases", "Java", "Communication"],
            ),
            (
                "Computer Networks TA",
                "Support lab exercises and walk through common issues.",
                "course_ta",
                ["Computer Networks", "Python", "Communication"],
            ),
            (
                "Final Exam Invigilator",
                "Assist with invigilation and on-site order during exams.",
                "invigilation",
                ["Invigilation", "Communication"],
            ),
            (
                "Orientation Event Support",
                "On-site check-in, guidance, and material coordination.",
                "activity",
                ["Communication", "Technical Writing"],
            ),
            (
                "JSP/Servlet Project TA",
                "Help students debug common Servlet/JSP issues.",
                "course_ta",
                ["JSP/Servlet", "Java", "Communication"],
            ),
            (
                "Machine Learning TA",
                "Homework Q&A and code walkthroughs.",
                "course_ta",
                ["Machine Learning", "Python", "Technical Writing"],
            ),
            (
                "Frontend Lab TA",
                "Review HTML/CSS/JS practice and provide feedback.",
                "course_ta",
                ["Frontend", "Communication"],
            ),
            (
                "Lab Open Day Volunteer",
                "Reception and guided tour support.",
                "activity",
                ["Communication"],
            ),
        ]
        if is_en
        else [
            ("数据结构课程助教", "协助批改作业、答疑、维护OJ题目。", "course_ta", ["数据结构", "Java", "沟通"]),
            ("操作系统课程助教", "实验课辅导、作业批改与课堂支持。", "course_ta", ["操作系统", "C++", "文档写作"]),
            ("数据库课程助教", "SQL 练习点评，课程群答疑。", "course_ta", ["数据库", "Java", "沟通"]),
            ("网络课程助教", "上机实验支持与作业讲评。", "course_ta", ["网络", "Python", "沟通"]),
            ("期末考试监考", "协助监考与现场秩序维护。", "invigilation", ["监考", "沟通"]),
            ("迎新活动支持", "现场签到、引导与物料管理。", "activity", ["沟通", "文档写作"]),
            ("JSP/Servlet 项目助教", "帮助同学排查 Servlet/JSP 常见问题。", "course_ta", ["JSP/Servlet", "Java", "沟通"]),
            ("机器学习课程助教", "作业答疑与代码讲解。", "course_ta", ["机器学习", "Python", "文档写作"]),
            ("前端小班助教", "HTML/CSS/JS 练习点评与答疑。", "course_ta", ["前端", "沟通"]),
            ("实验室开放日志愿者", "接待参观、讲解路线。", "activity", ["沟通"]),
        ]
    )
    rng.shuffle(job_templates)
    jobs = []
    for i in range(max(1, args.jobs)):
        title, desc, jtype, req_skills = job_templates[i % len(job_templates)]
        mo = mos[i % len(mos)]
        job_id = ids(args.seed, "job", f"{title}:{mo['email']}:{i}")
        jobs.append(
            {
                "id": job_id,
                "title": title,
                "moduleOrganiserId": mo["id"],
                "description": desc,
                "requiredSkills": req_skills,
                "type": jtype,
                "status": "open",
                "createdAt": ms(t0 + timedelta(days=2, hours=i)),
            }
        )

    # ---------- Applications ----------
    applications = []
    app_idx = 0

    # Helper: pick distinct applicants per job
    applicant_ids = [a["id"] for a in applicants]
    for j_i, job in enumerate(jobs):
        picks = rng.sample(applicant_ids, k=min(len(applicant_ids), rng.randint(2, 4)))
        for a_i, aid in enumerate(picks):
            app_id = ids(args.seed, "application", f"{job['id']}:{aid}:{a_i}")
            status_roll = (j_i + a_i) % 10
            status = "pending"
            interview_at = 0
            interview_detail = None
            interview_confirmed = False
            interview_ta_status = None
            if status_roll in (0, 1, 2):
                status = "accepted"
            elif status_roll in (3, 4):
                status = "interview"
                iv_time = t0 + timedelta(days=8 + j_i, hours=10 + a_i)
                interview_at = ms(iv_time)
                interview_detail = (
                    f"Online meeting (Tencent Meeting), ID {rng.randint(100000000,999999999)}"
                    if is_en
                    else f"线上会议：Tencent Meeting，会议号 {rng.randint(100000000,999999999)}"
                )
                interview_ta_status = "pending"
                interview_confirmed = False
            elif status_roll == 5:
                status = "rejected"
            elif status_roll == 6:
                status = "cancelled"
            else:
                status = "pending"

            applications.append(
                {
                    "id": app_id,
                    "applicantId": aid,
                    "jobId": job["id"],
                    "status": status,
                    "note": "I’m interested in this role and available for interviews."
                    if is_en
                    else "希望参与岗位，时间较灵活。",
                    "appliedAt": ms(t0 + timedelta(days=4, minutes=15 * app_idx)),
                    "interviewAt": interview_at,
                    "interviewDetail": interview_detail,
                    "interviewConfirmed": interview_confirmed,
                    "interviewTaStatus": interview_ta_status,
                }
            )
            app_idx += 1

    # Ensure at least one cancelled application for FriendService demo.
    if applications:
        applications[0]["status"] = "cancelled"
        applications[0]["interviewAt"] = 0
        applications[0]["interviewDetail"] = None
        applications[0]["interviewConfirmed"] = False
        applications[0]["interviewTaStatus"] = None

    # Close some jobs that have accepted applications (so admin cancel can reopen).
    accepted_by_job = {}
    for a in applications:
        if a["status"] == "accepted":
            accepted_by_job.setdefault(a["jobId"], 0)
            accepted_by_job[a["jobId"]] += 1
    for job in jobs:
        if accepted_by_job.get(job["id"], 0) > 0 and rng.random() < 0.6:
            job["status"] = "closed"

    # ---------- Friend links & requests ----------
    friend_links = []
    friend_requests = []

    # Create one accepted friendship (no strict coupling with friend_requests in storage).
    if applicants and mos:
        friend_links.append(
            {
                "applicantId": applicants[-1]["id"],
                "moduleOrganiserId": mos[0]["id"],
                "createdAt": ms(t0 + timedelta(days=6, hours=2)),
            }
        )

    # TA -> MO pending request: choose an applicant who has no non-cancelled app to that MO.
    def has_non_cancelled_app(applicant_id: str, mo_id: str) -> bool:
        mo_job_ids = {j["id"] for j in jobs if j["moduleOrganiserId"] == mo_id}
        for a in applications:
            if a["applicantId"] == applicant_id and a["jobId"] in mo_job_ids and a["status"] != "cancelled":
                return True
        return False

    if len(applicants) >= 2 and len(mos) >= 2:
        ta_for_request = applicants[0]["id"]
        mo_for_request = mos[1]["id"]
        if not has_non_cancelled_app(ta_for_request, mo_for_request):
            friend_requests.append(
                {
                    "id": ids(args.seed, "friend_request", f"ta->{mo_for_request}:{ta_for_request}"),
                    "applicantId": ta_for_request,
                    "moduleOrganiserId": mo_for_request,
                    "fromRole": "ta",
                    "status": "pending",
                    "createdAt": ms(t0 + timedelta(days=7)),
                }
            )

    # MO -> TA pending request: require TA had any application to that MO but currently no non-cancelled.
    # We made applications[0] cancelled; use its MO.
    if applications and mos:
        cancelled_app = applications[0]
        aid = cancelled_app["applicantId"]
        job_id = cancelled_app["jobId"]
        mo_id = next((j["moduleOrganiserId"] for j in jobs if j["id"] == job_id), None)
        if mo_id and not has_non_cancelled_app(aid, mo_id):
            friend_requests.append(
                {
                    "id": ids(args.seed, "friend_request", f"mo->{aid}:{mo_id}"),
                    "applicantId": aid,
                    "moduleOrganiserId": mo_id,
                    "fromRole": "mo",
                    "status": "pending",
                    "createdAt": ms(t0 + timedelta(days=7, hours=1)),
                }
            )

    # ---------- Direct messages + read states ----------
    messages = []
    dm_read_states = []

    def add_state(applicant_id: str, mo_id: str, ta_last: int, mo_last: int):
        dm_read_states.append(
            {
                "applicantId": applicant_id,
                "moduleOrganiserId": mo_id,
                "taLastReadAt": ta_last,
                "moLastReadAt": mo_last,
            }
        )

    # Create a few conversations based on non-cancelled applications.
    conv_pairs = []
    for a in applications:
        if a["status"] == "cancelled":
            continue
        job = next((j for j in jobs if j["id"] == a["jobId"]), None)
        if not job:
            continue
        pair = (a["applicantId"], job["moduleOrganiserId"], a["jobId"])
        if pair not in conv_pairs:
            conv_pairs.append(pair)
        if len(conv_pairs) >= 6:
            break

    for c_i, (aid, moid, jobid) in enumerate(conv_pairs):
        base = t0 + timedelta(days=5 + c_i, hours=9)
        m1 = {
            "id": ids(args.seed, "dm", f"{aid}:{moid}:1"),
            "applicantId": aid,
            "moduleOrganiserId": moid,
            "senderRole": "ta",
            "body": "Hello! I’ve submitted my application. Could you share the next steps?"
            if is_en
            else "老师您好，我已投递申请，想了解一下后续流程～",
            "sentAt": ms(base),
            "jobId": jobid,
        }
        m2 = {
            "id": ids(args.seed, "dm", f"{aid}:{moid}:2"),
            "applicantId": aid,
            "moduleOrganiserId": moid,
            "senderRole": "mo",
            "body": "Got it. We’ll complete the initial screening this week and send interview details."
            if is_en
            else "收到，我们会在本周内完成初筛并通知面试安排。",
            "sentAt": ms(base + timedelta(minutes=12)),
            "jobId": jobid,
        }
        m3 = {
            "id": ids(args.seed, "dm", f"{aid}:{moid}:3"),
            "applicantId": aid,
            "moduleOrganiserId": moid,
            "senderRole": "ta",
            "body": "Thanks! I’m flexible with scheduling this week."
            if is_en
            else "好的，谢谢老师！我这周时间比较灵活。",
            "sentAt": ms(base + timedelta(minutes=18)),
            "jobId": jobid,
        }
        messages.extend([m1, m2, m3])
        add_state(aid, moid, ms(base + timedelta(minutes=18)), ms(base + timedelta(minutes=12)))

    # ---------- Forum ----------
    threads = []
    replies = []
    forum_titles = (
        [
            "[Help] Resume upload failed — what to do?",
            "How long does it take to get feedback after applying?",
            "Sharing: tips for interview/teaching demo preparation",
            "This week’s invigilation checklist and reminders",
            "How to troubleshoot JSP 500 errors?",
            "How to balance TA workload effectively?",
        ]
        if is_en
        else [
            "【求助】简历上传失败怎么办？",
            "岗位申请后多久会有反馈？",
            "分享：面试/试讲准备经验",
            "本周监考安排与注意事项",
            "JSP 页面 500 如何排查？",
            "助教工作量太大怎么平衡？",
        ]
    )
    rng.shuffle(forum_titles)

    def pick_author():
        roll = rng.random()
        if roll < 0.6:
            a = rng.choice(applicants)
            return a["id"], "ta", a["name"]
        if roll < 0.9:
            m = rng.choice(mos)
            return m["id"], "mo", m["name"]
        ad = rng.choice(admins)
        return ad["id"], "admin", ad["name"]

    for i in range(max(1, args.threads)):
        author_id, author_role, author_name = pick_author()
        created = t0 + timedelta(days=3 + i, hours=8)
        tid = ids(args.seed, "thread", f"{forum_titles[i % len(forum_titles)]}:{i}")
        threads.append(
            {
                "id": tid,
                "title": forum_titles[i % len(forum_titles)],
                "body": "This is a sample thread used to test forum index and detail pages."
                if is_en
                else "这里是一个示例帖子内容，用于测试论坛列表与详情页展示。",
                "authorId": author_id,
                "authorRole": author_role,
                "authorName": author_name,
                "createdAt": ms(created),
                "lastReplyAt": ms(created),
                "replyCount": 0,
            }
        )

        # 0~3 replies
        rc = rng.randint(0, 3)
        last = created
        for r_i in range(rc):
            rid = ids(args.seed, "reply", f"{tid}:{r_i}")
            au_id, au_role, au_name = pick_author()
            last = last + timedelta(minutes=30 + 10 * r_i)
            replies.append(
                {
                    "id": rid,
                    "threadId": tid,
                    "authorId": au_id,
                    "authorRole": au_role,
                    "authorName": au_name,
                    "body": "Sample reply: check your network and file format, then try uploading again."
                    if is_en
                    else "回复示例：可以先检查网络和文件格式，再尝试重新上传。",
                    "createdAt": ms(last),
                }
            )
        if rc > 0:
            threads[-1]["lastReplyAt"] = ms(last)
            threads[-1]["replyCount"] = rc

    # ---------- Write all ----------
    write_json(out_dir / "admins.json", admins)
    write_json(out_dir / "module_organisers.json", mos)
    write_json(out_dir / "applicants.json", applicants)
    write_json(out_dir / "jobs.json", jobs)
    write_json(out_dir / "applications.json", applications)
    write_json(out_dir / "messages.json", messages)
    write_json(out_dir / "dm_read_states.json", dm_read_states)
    write_json(out_dir / "friend_links.json", friend_links)
    write_json(out_dir / "friend_requests.json", friend_requests)
    write_json(out_dir / "forum_threads.json", threads)
    write_json(out_dir / "forum_replies.json", replies)

    print("Mock data generated in:", out_dir)
    print("Accounts:")
    print(f"  Admin: admin@demo.com / {pw.admin_plain}")
    print(f"  MO:    {mos[0]['email']} / {pw.mo_plain}")
    print(f"  TA:    {applicants[0]['email']} / {pw.ta_plain}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
