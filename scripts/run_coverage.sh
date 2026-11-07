#!/usr/bin/env bash
# run_coverage.sh – Esegui unit test e genera report HTML Kover (Full Debug)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"

echo "==> JAVA_HOME: $JAVA_HOME"
echo "==> Java: $(java -version 2>&1 | head -1)"
echo ""

cd "$SCRIPT_DIR"

echo "==> Unit test: androidApp (FullDebug) + shared..."
./gradlew \
    :androidApp:testFullDebugUnitTest \
    :shared:testDebugUnitTest \
    --continue

echo ""
echo "==> Generazione report HTML Kover..."
./gradlew :koverHtmlReport

REPORT_DIR="$SCRIPT_DIR/build/reports/kover/html"

echo ""
echo "==> Applicazione stile personalizzato al report..."
python3 - "$REPORT_DIR" <<'PYEOF'
import sys, os, re

REPORT_DIR = sys.argv[1]

CUSTOM_CSS = """
<style id="ant-custom">
  /* ── Reset & base ── */
  *, *::before, *::after { box-sizing: border-box; }

  body {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
                 "Helvetica Neue", Arial, sans-serif;
    font-size: 14px;
    line-height: 1.6;
    color: #1a1a2e;
    background: #f5f7fa;
    margin: 0;
    padding: 0;
  }

  /* ── Top header bar ── */
  .header, #header, [id*="header"], .coverageReport > div:first-child {
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
    color: #fff;
    padding: 20px 32px;
    box-shadow: 0 2px 8px rgba(0,0,0,.25);
  }
  .header h1, #header h1 { font-size: 1.4rem; font-weight: 600; margin: 0; }

  /* ── Page wrapper ── */
  .coverageReport, #content, .container, main {
    max-width: 1200px;
    margin: 0 auto;
    padding: 24px 32px;
  }

  /* ── Coverage summary cards ── */
  .coverageStat, .stat, [class*="Stat"] {
    background: #fff;
    border-radius: 10px;
    padding: 16px 20px;
    margin: 8px 0;
    box-shadow: 0 1px 4px rgba(0,0,0,.08);
    border-left: 4px solid #4361ee;
  }

  /* ── Tables ── */
  table {
    width: 100%;
    border-collapse: collapse;
    background: #fff;
    border-radius: 10px;
    overflow: hidden;
    box-shadow: 0 1px 4px rgba(0,0,0,.08);
    margin: 16px 0;
  }
  thead tr { background: #4361ee; color: #fff; }
  th {
    padding: 12px 16px;
    font-weight: 600;
    font-size: 0.82rem;
    letter-spacing: .04em;
    text-transform: uppercase;
    text-align: left;
  }
  td { padding: 10px 16px; border-bottom: 1px solid #eef0f5; }
  tbody tr:last-child td { border-bottom: none; }
  tbody tr:hover { background: #f0f4ff; }

  /* ── Coverage percentage colours ── */
  .coverageHigh, [class*="high"], [class*="covered"] { color: #2d9e63; font-weight: 600; }
  .coverageMedium, [class*="medium"], [class*="partial"] { color: #e07b2a; font-weight: 600; }
  .coverageLow, [class*="low"], [class*="missed"] { color: #d64045; font-weight: 600; }

  /* ── Coverage bars ── */
  .coverageBar, [class*="Bar"], progress {
    height: 8px;
    border-radius: 4px;
    background: #e8ecf4;
    overflow: hidden;
    display: block;
    min-width: 80px;
  }

  /* ── Links ── */
  a { color: #4361ee; text-decoration: none; }
  a:hover { text-decoration: underline; }

  /* ── Breadcrumb ── */
  .breadcrumb, [class*="breadcrumb"], [class*="path"] {
    font-size: 0.82rem;
    color: #666;
    margin-bottom: 12px;
    padding: 8px 0;
    border-bottom: 1px solid #eef0f5;
  }

  /* ── Source view (file detail pages) ── */
  pre, code, .sourceCode, [class*="source"] {
    font-family: "JetBrains Mono", "Fira Code", "Cascadia Code",
                 "SF Mono", Consolas, "Liberation Mono", monospace;
    font-size: 13px;
    line-height: 1.7;
    background: #1e1e2e;
    color: #cdd6f4;
    border-radius: 8px;
    padding: 16px 20px;
    overflow-x: auto;
    margin: 16px 0;
    box-shadow: 0 2px 8px rgba(0,0,0,.2);
  }
  .covered, [class*="covered"]   { background: rgba(45,158,99,.20); }
  .missed,  [class*="missed"]    { background: rgba(214,64,69,.20); }
  .partial, [class*="partial"]   { background: rgba(224,123,42,.15); }

  /* ── Footer ── */
  .footer, #footer { font-size: 0.75rem; color: #999; padding: 24px 32px; text-align: center; }
</style>
"""

def inject(path):
    with open(path, "r", encoding="utf-8", errors="replace") as f:
        html = f.read()
    if "ant-custom" in html:
        return  # already injected
    if "</head>" in html:
        html = html.replace("</head>", CUSTOM_CSS + "\n</head>", 1)
    elif "<body" in html:
        html = re.sub(r"(<body[^>]*>)", r"\1\n" + CUSTOM_CSS, html, count=1)
    else:
        html = CUSTOM_CSS + html
    with open(path, "w", encoding="utf-8") as f:
        f.write(html)

count = 0
for root, _, files in os.walk(REPORT_DIR):
    for name in files:
        if name.endswith(".html"):
            inject(os.path.join(root, name))
            count += 1

print(f"  Stile applicato a {count} file HTML.")
PYEOF

REPORT="$REPORT_DIR/index.html"
echo ""
echo "✅  Report pronto: $REPORT"
