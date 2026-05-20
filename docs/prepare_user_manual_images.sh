#!/usr/bin/env bash
# 生成中英并排截图并复制到用户手册目录
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
ZH="$ROOT/report_ui/zh"
EN="$ROOT/report_ui/en"
COMBINED="$ROOT/report_ui/combined"
DST="$ROOT/user_manual/bilingual"

if [[ ! -d "$ZH" ]] || [[ -z "$(ls -A "$ZH" 2>/dev/null || true)" ]]; then
  echo "未找到 $ZH，请先启动应用并运行: python3 docs/capture_ui_screenshots.py" >&2
  exit 1
fi
if [[ ! -d "$EN" ]] || [[ -z "$(ls -A "$EN" 2>/dev/null || true)" ]]; then
  echo "未找到 $EN，请先运行: python3 docs/capture_ui_screenshots.py" >&2
  exit 1
fi

python3 "$ROOT/build_ui_composites.py"
mkdir -p "$DST"
cp -f "$COMBINED"/*.png "$DST"/
echo "已生成并复制中英并排截图到 $DST"
