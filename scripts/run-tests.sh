#!/bin/bash

set -e

# Navigate to project root (parent of scripts directory)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "🧪 Running all tests from $PROJECT_ROOT..."
echo ""

cd "$PROJECT_ROOT"
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :shared:allTests :androidApp:testDebugUnitTest

echo ""
echo "✅ All tests passed!"
