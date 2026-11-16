#!/usr/bin/env bash
# format.sh – Auto-formatta tutto il codice Kotlin con ktlintFormat
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"

cd "$PROJECT_ROOT"

echo "==> ktlintFormat su androidApp e shared..."
./gradlew ktlintFormat 2>&1 | tail -20

echo ""
echo "✅  Formattazione completata."
echo "    Report HTML: androidApp/build/reports/ktlint/"
echo "    Verifica le modifiche con: git diff"
