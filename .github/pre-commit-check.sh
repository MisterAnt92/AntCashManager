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

check_file ".github/ai-assistant.yml" || FAILED=1
check_file ".github/agent-android-clean-architecture.md" || FAILED=1

if [ $FAILED -ne 0 ]; then
  echo "Pre-commit checks failed. Fix the errors above or copy the files into the repository." >&2
  exit 1
fi

echo "Pre-commit checks passed."
exit 0

