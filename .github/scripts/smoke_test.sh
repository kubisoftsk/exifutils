#!/usr/bin/env bash
# Smoke test of a built application with a real exiftool: set a date, rename, read it back.
# usage: smoke_test.sh COMMAND...   e.g. smoke_test.sh java -jar exifutils.jar
set -euo pipefail

dir=$(mktemp -d)
trap 'rm -rf "$dir"' EXIT

# 8x8 px JPEG without metadata
base64 -d > "$dir/photo.jpg" <<'JPEG'
/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDABALDA4MChAODQ4SERATGCgaGBYWGDEjJR0oOjM9PDkzODdASFxOQERXRTc4UG1RV19i
Z2hnPk1xeXBkeFxlZ2P/wAALCAAIAAgBAREA/8QAFAABAAAAAAAAAAAAAAAAAAAAAf/EABQQAQAAAAAAAAAAAAAAAAAAAAD/2gAI
AQEAAD8AH//Z
JPEG

printf 'y\ny\n' | "$@" set-date -r -d "2020-07-02 03:04:05" "$dir"
test -f "$dir/IMG_20200702_030405.jpg" || { echo "File was not renamed:"; ls "$dir"; exit 1; }
"$@" info "$dir" | tee "$dir/info.txt"
grep -q "2020-07-02 03:04:05" "$dir/info.txt" || { echo "Date was not written"; exit 1; }
echo "Smoke test passed"
