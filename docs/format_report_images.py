#!/usr/bin/env python3
"""Post-process Report_group013.docx: centre appendix images, uniform width."""
import re
import sys
import zipfile
import os

# Word EMUs per inch
EMU_PER_INCH = 914400
# Full text width ~6.5in for A4 with margins; composites ~1280px — use 6.2in
TARGET_WIDTH_EMU = int(6.2 * EMU_PER_INCH)


def process_docx(path: str):
    tmp = path + ".tmp"
    with zipfile.ZipFile(path, "r") as zin:
        names = zin.namelist()
        files = {n: zin.read(n) for n in names}
    xml = files["word/document.xml"].decode("utf-8")

    def repl_extent(m):
        cx, cy = int(m.group(1)), int(m.group(2))
        if cx <= TARGET_WIDTH_EMU:
            return m.group(0)
        new_cx = TARGET_WIDTH_EMU
        new_cy = int(cy * new_cx / cx) if cx else cy
        return f'<wp:extent cx="{new_cx}" cy="{new_cy}"'

    xml = re.sub(r'<wp:extent cx="(\d+)" cy="(\d+)"', repl_extent, xml)

    # Centre image paragraphs
    def fix_p(match):
        p = match.group(0)
        if "wp:inline" not in p:
            return p
        if "<w:jc" in p:
            return p
        if "<w:pPr>" in p:
            return p.replace("<w:pPr>", "<w:pPr><w:jc w:val=\"center\"/>", 1)
        return p.replace("<w:p>", "<w:p><w:pPr><w:jc w:val=\"center\"/></w:pPr>", 1)

    xml = re.sub(r"<w:p[\s>][\s\S]*?</w:p>", fix_p, xml)
    files["word/document.xml"] = xml.encode("utf-8")

    with zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as zout:
        for n in names:
            zout.writestr(n, files[n])
    os.replace(tmp, path)
    print("Formatted:", path)


if __name__ == "__main__":
    p = sys.argv[1] if len(sys.argv) > 1 else os.path.join(
        os.path.dirname(__file__), "submission_docx", "Report_group013.docx"
    )
    process_docx(os.path.abspath(p))
