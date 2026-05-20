#!/usr/bin/env python3
"""Generate system architecture diagram for Group 013 final report."""
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch
import os

OUT = os.path.join(os.path.dirname(__file__), "architecture_diagram.png")

fig, ax = plt.subplots(1, 1, figsize=(10, 6.2), dpi=150)
ax.set_xlim(0, 10)
ax.set_ylim(0, 6.2)
ax.axis("off")
ax.set_title(
    "TA Recruitment System — Layered Architecture (Group 013)",
    fontsize=13,
    fontweight="bold",
    pad=12,
)

def box(x, y, w, h, text, fc, ec="#333333", fontsize=9):
    p = FancyBboxPatch(
        (x, y),
        w,
        h,
        boxstyle="round,pad=0.02,rounding_size=0.08",
        linewidth=1.2,
        edgecolor=ec,
        facecolor=fc,
    )
    ax.add_patch(p)
    ax.text(x + w / 2, y + h / 2, text, ha="center", va="center", fontsize=fontsize, wrap=True)

# Client
box(3.6, 5.0, 2.8, 0.75, "Browser\n(Chrome)", "#E8F4FC", fontsize=10)

# Presentation
box(0.4, 3.55, 9.2, 1.15,
     "Presentation Layer\nJSP + Servlet  (@WebServlet / web.xml)\n"
     "TA · MO · Admin · Forum · Assistant · Filters / Listeners",
     "#D6EAF8", fontsize=8.5)

# Business
box(0.4, 2.05, 9.2, 1.15,
     "Business Layer (service/)\n"
     "ApplicantService · JobService · ApplicationService · MatchHelper\n"
     "ForumService · MessageService · assistant/* · SemanticMatchService",
     "#D5F5E3", fontsize=8.5)

# Data
box(0.4, 0.55, 5.5, 1.15,
     "Data Layer\n"
     "Storage (JSON / text)  —  data/*.json, data/resumes/\n"
     "Gson · ReentrantReadWriteLock",
     "#FCF3CF", fontsize=8.5)

# External optional
box(6.2, 0.55, 3.4, 1.15,
     "Optional External APIs\n"
     "LLM Chat · Embeddings\n"
     "(Kimi / Qwen / OpenAI)",
     "#FADBD8", fontsize=8.5)

def arrow(x1, y1, x2, y2, label=None, style="-|>", color="#444"):
    a = FancyArrowPatch(
        (x1, y1), (x2, y2),
        arrowstyle=style, mutation_scale=12,
        linewidth=1.1, color=color,
        connectionstyle="arc3,rad=0",
    )
    ax.add_patch(a)
    if label:
        mx, my = (x1 + x2) / 2, (y1 + y2) / 2
        ax.text(mx + 0.15, my, label, fontsize=7, color="#555", style="italic")

# Main vertical flow
arrow(5.0, 5.0, 5.0, 4.72)
arrow(5.0, 3.55, 5.0, 3.22)
arrow(5.0, 2.05, 3.15, 1.72)
arrow(7.5, 2.35, 7.9, 1.72, label="optional")

# Side note
ax.text(
    0.45, 0.12,
    "Actors: TA (applicant) · MO (recruiter) · Admin (workload)   |   "
    "No database — file-based persistence per coursework",
    fontsize=7.5, color="#666",
)

plt.tight_layout()
plt.savefig(OUT, bbox_inches="tight", facecolor="white", edgecolor="none")
print("Saved:", OUT)
