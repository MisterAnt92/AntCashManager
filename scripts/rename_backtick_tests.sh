#!/bin/bash
# =============================================================================
# rename_backtick_tests.sh
# =============================================================================
# Converte i nomi delle funzioni di test da notazione backtick a underscore/
# camelCase seguendo la convenzione: method_shouldExpectedBehavior_whenCondition
#
# Uso:
#   ./scripts/rename_backtick_tests.sh [--dry-run] [percorso_progetto]
#
# Opzioni:
#   --dry-run   Mostra solo le modifiche senza applicarle
#   percorso    Radice del progetto (default: directory dello script/..)
#
# Esempi:
#   ./scripts/rename_backtick_tests.sh
#   ./scripts/rename_backtick_tests.sh --dry-run
#   ./scripts/rename_backtick_tests.sh --dry-run /path/to/project
# =============================================================================

set -euo pipefail

# ---------------------------------------------------------------------------
# Argomenti
# ---------------------------------------------------------------------------
DRY_RUN=false
PROJECT_ROOT=""

for arg in "$@"; do
    case "$arg" in
        --dry-run) DRY_RUN=true ;;
        *)         PROJECT_ROOT="$arg" ;;
    esac
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="${PROJECT_ROOT:-$(cd "$SCRIPT_DIR/.." && pwd)}"

echo "============================================================"
echo " Rinomina unit test: backtick → underscore/camelCase"
echo "============================================================"
echo " Progetto : $PROJECT_ROOT"
echo " Dry-run  : $DRY_RUN"
echo "============================================================"
echo ""

# ---------------------------------------------------------------------------
# Logica di conversione (Python inline)
# ---------------------------------------------------------------------------
python3 - "$PROJECT_ROOT" "$DRY_RUN" << 'PYTHON_EOF'
import re
import os
import sys

project_root = sys.argv[1]
dry_run      = sys.argv[2].lower() == "true"

# ── helpers ────────────────────────────────────────────────────────────────

def to_camel_case(words: list[str]) -> str:
    """Converte una lista di parole in camelCase, ignorando caratteri non validi."""
    cleaned = [re.sub(r"[^a-zA-Z0-9]", "", w) for w in words]
    cleaned = [w for w in cleaned if w]
    if not cleaned:
        return ""
    first = cleaned[0][0].lower() + cleaned[0][1:] if len(cleaned[0]) > 1 else cleaned[0].lower()
    rest  = "".join(w[0].upper() + w[1:] for w in cleaned[1:] if w)
    return first + rest


def convert_backtick_name(raw: str) -> str:
    """
    Converte il contenuto di un nome backtick nella convenzione:
        method_shouldExpectedBehavior_whenCondition

    Regole di split:
      1) Le parole precedenti a "should" (o "returns"/"verifies") formano il
         segmento metodo.
      2) Le parole da "should" (inclusa) fino a "when" (esclusa) formano il
         segmento comportamento.
      3) Le parole da "when" (inclusa) in poi formano il segmento condizione.
      4) Se manca "should", si usa camelCase semplice su tutte le parole.
    """
    name  = raw.strip()
    words = re.split(r"[\s\-]+", name)
    words = [w for w in words if w]

    if not words:
        return name

    # Ricerca delle parole chiave di split
    should_idx: int | None = None
    when_idx:   int | None = None

    for i, w in enumerate(words):
        wl = w.lower()
        if wl == "should" and should_idx is None:
            should_idx = i
        elif wl == "when" and when_idx is None and should_idx is not None:
            when_idx = i

    if should_idx is not None and should_idx > 0:
        method_part = to_camel_case(words[:should_idx])

        if when_idx is not None:
            should_part = to_camel_case(words[should_idx:when_idx])
            when_part   = to_camel_case(words[when_idx:])
            return f"{method_part}_{should_part}_{when_part}"
        else:
            should_part = to_camel_case(words[should_idx:])
            return f"{method_part}_{should_part}"
    else:
        # Nessun "should": camelCase flat di tutte le parole
        return to_camel_case(words)


# Pattern per identificare: fun `testo qualsiasi`
# Cattura l'intero match (gruppo 0) e il solo nome interno (gruppo 1)
BACKTICK_FUN_RE = re.compile(r"fun\s+`([^`]+)`")

# ── scansione file ──────────────────────────────────────────────────────────

total_files   = 0
total_renamed = 0
errors        = []

# Cerca ricorsivamente tutti i file .kt nel progetto
for dirpath, dirnames, filenames in os.walk(project_root):
    # Esclude directory generate/di build (rispetta .gitignore)
    dirnames[:] = [
        d for d in dirnames
        if d not in {"build", ".gradle", ".idea", "node_modules", ".git"}
    ]

    for filename in filenames:
        if not filename.endswith(".kt"):
            continue

        filepath = os.path.join(dirpath, filename)

        # Opera solo sui source set di test
        rel_path = os.path.relpath(filepath, project_root)
        is_test = any(seg in rel_path.split(os.sep) for seg in (
            "test", "androidTest", "commonTest", "androidHostTest",
        ))
        if not is_test:
            continue

        try:
            with open(filepath, "r", encoding="utf-8") as f:
                original = f.read()
        except Exception as e:
            errors.append(f"  ⚠ Lettura fallita: {filepath} → {e}")
            continue

        # Trova tutti i match nel file
        matches = list(BACKTICK_FUN_RE.finditer(original))
        if not matches:
            continue

        file_renames = []
        new_content  = original

        for m in matches:
            old_name = m.group(1)
            new_name = convert_backtick_name(old_name)

            if new_name == old_name:
                continue  # nessuna trasformazione necessaria

            # Sostituzione puntuale: rimpiazza `fun \`old\`` con `fun new`
            old_token = f"fun `{old_name}`"
            new_token = f"fun {new_name}"

            if old_token in new_content:
                new_content = new_content.replace(old_token, new_token, 1)
                file_renames.append((old_name, new_name))

        if not file_renames:
            continue

        total_files   += 1
        total_renamed += len(file_renames)

        print(f"📄 {rel_path}")
        for old, new in file_renames:
            print(f"    - `{old}`")
            print(f"    + {new}")
        print()

        if not dry_run:
            try:
                with open(filepath, "w", encoding="utf-8") as f:
                    f.write(new_content)
            except Exception as e:
                errors.append(f"  ⚠ Scrittura fallita: {filepath} → {e}")

# ── riepilogo ───────────────────────────────────────────────────────────────

print("============================================================")
if dry_run:
    print(f" DRY-RUN: nessun file modificato")
else:
    print(f" Modifiche applicate")
print(f" File toccati  : {total_files}")
print(f" Test rinominati: {total_renamed}")

if errors:
    print()
    print(" ERRORI:")
    for e in errors:
        print(e)

print("============================================================")

if dry_run and total_renamed > 0:
    print()
    print(" Riesegui senza --dry-run per applicare le modifiche.")

PYTHON_EOF

