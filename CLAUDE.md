@AGENTS.md

# Claude Code — operational notes

- Before any Gradle command: `export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64`.
- Never `git add/commit/push` without explicit approval in the current message (see AGENTS.md §1).
- Multi-step tasks: one file at a time, compile once per logical group, confirm before the next step (AGENTS.md §2).
- Follow the token discipline in AGENTS.md §3: grep before Read, `| tail -30`, targeted tests, no unrequested docs.
- Session memory lives in `~/.claude/projects/-opt-src-GIT-app-AntCashManager/memory/`; do not duplicate AGENTS.md content there.
