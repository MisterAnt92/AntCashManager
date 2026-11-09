#!/usr/bin/env bash
# run_coverage.sh – Esegui unit test e genera report HTML Kover (Full Debug)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"

echo "==> JAVA_HOME: $JAVA_HOME"
echo "==> Java: $(java -version 2>&1 | head -1)"
echo ""

cd "$PROJECT_ROOT"

echo "==> Unit test: androidApp (FullDebug) + shared..."
./gradlew \
    :androidApp:testFullDebugUnitTest \
    :shared:testAndroidHostTest \
    --continue

echo ""
echo "==> Generazione report HTML Kover..."
./gradlew :koverHtmlReport

REPORT_DIR="$PROJECT_ROOT/build/reports/kover/html"
REPORT="$REPORT_DIR/index.html"

echo ""
echo "==> Applicazione stile Material Design al report Kover..."
python3 - "$REPORT_DIR" <<'PYEOF'
import os
import re
import sys

REPORT_DIR = sys.argv[1]
INDEX = os.path.join(REPORT_DIR, "index.html")

if not os.path.isfile(INDEX):
    raise SystemExit(f"Kover HTML report index not found: {INDEX}")

MATERIAL_CSS = r"""<style id="material-coverage">
:root {
  color-scheme: light;
  --md-primary: #6750a4;
  --md-on-primary: #fff;
  --md-primary-container: #eaddff;
  --md-on-primary-container: #21005d;
  --md-secondary: #625b71;
  --md-surface: #fef7ff;
  --md-surface-container: #f3edf7;
  --md-surface-high: #ece6f0;
  --md-on-surface: #1d1b20;
  --md-on-surface-variant: #49454f;
  --md-outline: #79747e;
  --md-outline-variant: #cac4d0;
  --md-error: #b3261e;
  --md-success: #386a20;
  --md-warning: #7d5700;
}
*, *::before, *::after { box-sizing: border-box; }
body {
  margin: 0;
  color: var(--md-on-surface);
  background: var(--md-surface);
  font: 400 14px/1.55 Roboto, "Segoe UI", Arial, sans-serif;
}
a { color: var(--md-primary); text-decoration: none; }
a:hover { text-decoration: underline; }
a:focus-visible, button:focus-visible { outline: 3px solid var(--md-primary); outline-offset: 2px; }
h1, h2, h3 { color: var(--md-on-surface); font-weight: 500; line-height: 1.25; }
h1 { font-size: clamp(1.6rem, 3vw, 2.25rem); }
h2 { font-size: 1.5rem; }
h3 { font-size: 1.2rem; }
body > header, #header, .header {
  padding: 24px clamp(16px, 4vw, 48px);
  color: var(--md-on-primary-container);
  background: var(--md-primary-container);
  border-bottom: 1px solid var(--md-outline-variant);
}
body > main, #content, #coverage, .coverageReport, body > table {
  width: min(100% - 32px, 1440px);
  margin: 24px auto;
}
table {
  width: 100%;
  margin: 16px 0 24px;
  border: 1px solid var(--md-outline-variant);
  border-spacing: 0;
  border-radius: 12px;
  background: var(--md-surface);
  overflow: hidden;
}
thead { color: var(--md-on-primary); background: var(--md-primary); }
th {
  padding: 12px 16px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: .04em;
  text-align: left;
}
td { padding: 10px 16px; border-top: 1px solid var(--md-outline-variant); }
tbody tr:hover { background: var(--md-surface-container); }
tr:hover a { text-decoration: underline; }
pre, code, tt {
  font-family: "Roboto Mono", "SFMono-Regular", Consolas, monospace;
  font-size: .92em;
}
pre {
  max-width: 100%;
  padding: 16px;
  overflow: auto;
  color: var(--md-on-surface);
  background: var(--md-surface-container);
  border: 1px solid var(--md-outline-variant);
  border-radius: 12px;
}
.green, .covered, .coverageHigh { color: var(--md-success); }
.yellow, .partial, .coverageMedium { color: var(--md-warning); }
.red, .missed, .coverageLow { color: var(--md-error); }
.covered { background: #e8f5e1; }
.partial { background: #fff3cd; }
.missed { background: #f9dedc; }
hr { border: 0; border-top: 1px solid var(--md-outline-variant); }
@media (max-width: 700px) {
  body > main, #content, #coverage, .coverageReport, body > table { width: min(100% - 16px, 1440px); margin: 12px auto; }
  table { display: block; overflow-x: auto; white-space: nowrap; }
  th, td { padding: 8px 12px; }
}
</style>"""

STYLE_RE = re.compile(r'<style\b[^>]*\bid=["\'](?:ant-custom|material-coverage)["\'][^>]*>.*?</style>', re.I | re.S)

html_files = []
for root, _, files in os.walk(REPORT_DIR):
    html_files.extend(os.path.join(root, name) for name in files if name.endswith(".html"))

for path in html_files:
    with open(path, encoding="utf-8") as file:
        html = file.read()
    html = STYLE_RE.sub("", html)
    if re.search(r"</head\s*>", html, re.I):
        html = re.sub(r"</head\s*>", MATERIAL_CSS + "\n</head>", html, count=1, flags=re.I)
    else:
        raise SystemExit(f"Cannot apply Material styling: HTML head not found in {path}")
    with open(path, "w", encoding="utf-8") as file:
        file.write(html)

print(f"  Stile Material applicato a {len(html_files)} pagine del report Kover.")
PYEOF

if [ ! -f "$REPORT" ]; then
    echo "❌ Report Kover non generato: $REPORT" >&2
    exit 1
fi

echo ""
echo "✅  Report pronto: $REPORT"
