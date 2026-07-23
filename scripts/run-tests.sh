#!/bin/bash

set -e

echo "🧪 Running all tests..."
echo ""

JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :shared:allTests :androidApp:testDebugUnitTest

echo ""
echo "✅ All tests passed!"
