#!/usr/bin/env python3
"""Build side-by-side zh|en composite screenshots for report layout."""
import os
from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.join(os.path.dirname(__file__), "report_ui")
OUT = os.path.join(ROOT, "combined")
ZH = os.path.join(ROOT, "zh")
EN = os.path.join(ROOT, "en")

# (filename, Chinese title, English title)
FIGURES = [
    ("01_home.png", "A.1 首页", "A.1 Home"),
    ("02_personal_center.png", "A.2 个人中心", "A.2 Personal centre"),
    ("03_ta_login.png", "A.3 应聘者登录", "A.3 TA sign-in"),
    ("04_mo_login.png", "A.4 课程组织者登录", "A.4 MO sign-in"),
    ("05_admin_login.png", "A.5 管理员登录", "A.5 Admin sign-in"),
    ("06_forum.png", "A.6 交流论坛", "A.6 Forum"),
    ("07_ta_dashboard.png", "A.7 应聘者工作台", "A.7 Applicant dashboard"),
    ("08_ta_jobs.png", "A.8 开放岗位", "A.8 Open jobs"),
    ("09_assistant.png", "A.9 智能小助手", "A.9 AI assistant"),
    ("10_mo_dashboard.png", "A.10 MO 工作台", "A.10 MO dashboard"),
    ("11_mo_applicants.png", "A.11 应聘者列表", "A.11 Applicant list"),
    ("12_admin_workload.png", "A.12 工作负荷", "A.12 Workload"),
    ("13_ta_applications.png", "A.13 我的申请", "A.13 My applications"),
    ("14_ta_messages.png", "A.14 站内信", "A.14 Messages"),
    ("15_mo_post_job.png", "A.15 发布岗位", "A.15 Post job"),
    ("16_ta_register.png", "A.16 TA 注册", "A.16 TA register"),
    ("17_ta_profile.png", "A.17 TA 个人中心", "A.17 TA profile"),
    ("18_forum_thread.png", "A.18 论坛帖子", "A.18 Forum thread"),
    ("19_mo_positions.png", "A.19 MO 我的岗位", "A.19 MO positions"),
    ("20_mo_messages.png", "A.20 MO 站内信", "A.20 MO messages"),
    ("21_mo_profile.png", "A.21 MO 个人中心", "A.21 MO profile"),
    ("22_mo_edit_job.png", "A.22 编辑岗位", "A.22 Edit job"),
    ("23_mo_register.png", "A.23 MO 注册", "A.23 MO register"),
    ("24_admin_register.png", "A.24 管理员注册", "A.24 Admin register"),
    ("25_ta_profile_edit.png", "A.25 TA 编辑资料", "A.25 TA edit profile"),
    ("26_ta_jobs_match.png", "A.26 岗位匹配度排序", "A.26 Jobs by match"),
    ("27_ta_guest_jobs.png", "A.27 访客浏览岗位", "A.27 Guest job browse"),
    ("28_mo_schedule_interview.png", "A.28 安排面试", "A.28 Schedule interview"),
    ("29_mo_post_english_error.png", "A.29 英文校验提示", "A.29 English-only error"),
    ("30_mo_post_confirm.png", "A.30 发布确认", "A.30 Post confirm"),
    ("31_forum_new_post.png", "A.31 论坛发帖", "A.31 New forum post"),
    ("32_admin_workload_detail.png", "A.32 录用明细", "A.32 Assignment detail"),
    ("33_ta_resume_ai.png", "A.33 简历 AI 润色", "A.33 Resume AI"),
    ("34_ta_register_confirm.png", "A.34 注册成功", "A.34 TA register OK"),
]

PANEL_W = 620
HEADER_H = 44
GAP = 12
PAD = 16
BG = (248, 250, 252)
LABEL_BG_ZH = (219, 234, 254)
LABEL_BG_EN = (237, 233, 254)
TEXT = (30, 41, 59)


def load_font(size: int):
    for path in (
        "/System/Library/Fonts/PingFang.ttc",
        "/System/Library/Fonts/STHeiti Light.ttc",
        "/Library/Fonts/Arial Unicode.ttf",
        "/System/Library/Fonts/Supplemental/Arial.ttf",
    ):
        if os.path.isfile(path):
            try:
                return ImageFont.truetype(path, size)
            except OSError:
                continue
    return ImageFont.load_default()


def fit_width(img: Image.Image, target_w: int) -> Image.Image:
    w, h = img.size
    nh = int(h * target_w / w)
    return img.resize((target_w, nh), Image.Resampling.LANCZOS)


def composite(name: str, zh_title: str, en_title: str) -> str:
    zh_path = os.path.join(ZH, name)
    en_path = os.path.join(EN, name)
    if not os.path.isfile(zh_path) or not os.path.isfile(en_path):
        raise FileNotFoundError(f"Missing {name}")

    zh = fit_width(Image.open(zh_path).convert("RGB"), PANEL_W)
    en = fit_width(Image.open(en_path).convert("RGB"), PANEL_W)
    panel_h = max(zh.height, en.height)

    def pad_panel(img):
        canvas = Image.new("RGB", (PANEL_W, panel_h), (255, 255, 255))
        canvas.paste(img, (0, 0))
        return canvas

    zh, en = pad_panel(zh), pad_panel(en)

    total_w = PAD * 2 + PANEL_W * 2 + GAP
    total_h = PAD * 2 + HEADER_H + panel_h
    out = Image.new("RGB", (total_w, total_h), BG)
    draw = ImageDraw.Draw(out)
    font = load_font(18)
    font_sm = load_font(14)

    # headers
    zh_box = (PAD, PAD, PAD + PANEL_W, PAD + HEADER_H)
    en_box = (PAD + PANEL_W + GAP, PAD, total_w - PAD, PAD + HEADER_H)
    draw.rectangle(zh_box, fill=LABEL_BG_ZH)
    draw.rectangle(en_box, fill=LABEL_BG_EN)
    draw.text((zh_box[0] + 12, PAD + 10), f"中文 · {zh_title}", fill=TEXT, font=font)
    draw.text((en_box[0] + 12, PAD + 10), f"English · {en_title}", fill=TEXT, font=font)

    y0 = PAD + HEADER_H
    out.paste(zh, (PAD, y0))
    out.paste(en, (PAD + PANEL_W + GAP, y0))

    note = "Left: Chinese UI  |  Right: English UI  (locale via ?lang=zh / ?lang=en)"
    draw.text((PAD, total_h - 22), note, fill=(100, 116, 139), font=font_sm)

    os.makedirs(OUT, exist_ok=True)
    out_path = os.path.join(OUT, name)
    out.save(out_path, "PNG", optimize=True)
    return out_path


def main():
    for item in FIGURES:
        path = composite(*item)
        print("Built", path)


if __name__ == "__main__":
    main()
