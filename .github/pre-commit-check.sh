#!/usr/bin/env zsh
# Simple repository-level checks for AI agent configuration
# This script is intended to be copied into .git/hooks/pre-commit locally

REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || echo ".")

check_file() {
  local f=$1
  if [ ! -f "$REPO_ROOT/$f" ]; then
    echo "ERROR: required file missing: $f"
    return 1
  fi
  if [ ! -s "$REPO_ROOT/$f" ]; then
    echo "ERROR: required file is empty: $f"
    return 1
  fi
  return 0
}

FAILED=0

# ── AI agent configuration files ──
check_file ".github/ai-assistant.yml" || FAILED=1
check_file ".github/agents/agent-compose-ui.agent.md" || FAILED=1
check_file ".github/agents/agent-unit-tests-mockk.agent.md" || FAILED=1
check_file ".github/agents/agent-usecase-pattern.agent.md" || FAILED=1
check_file ".github/agents/agent-viewmodel-stateflow.agent.md" || FAILED=1
check_file ".github/agents/agent-code-cleanup.agent.md" || FAILED=1

# ── ktlint check on staged Kotlin files ──
STAGED_KT=$(git diff --cached --name-only --diff-filter=ACM 2>/dev/null | grep -E '\.kts?$' || true)
if [ -n "$STAGED_KT" ]; then
  export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
  cd "$REPO_ROOT"
  ./gradlew ktlintCheck --quiet 2>&1 | head -50 || {
    echo "ktlint violations found. Run ./scripts/format.sh to auto-fix, then re-stage." >&2
    FAILED=1
  }
fi

if [ $FAILED -ne 0 ]; then
  echo "Pre-commit checks failed. Fix the errors above or copy the files into the repository." >&2
  exit 1
fi

echo "Pre-commit checks passed."
exit 0

