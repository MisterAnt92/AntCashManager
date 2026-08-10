#!/usr/bin/env python3
"""
Script per convertire tutti gli Spacer nel progetto AntCashManager
al nuovo sistema AppSpacer con SpacingSize enumerato.

Uso:
    python3 scripts/convert_spacers.py

Caratteristiche:
    - Sostituisce Spacer(modifier = Modifier.height(Xdp)) con VerticalSpacer(SpacingSize.*)
    - Sostituisce Spacer(modifier = Modifier.width(Xdp)) con HorizontalSpacer(SpacingSize.*)
    - Aggiorna gli import automaticamente
    - Preserva spacer con valori custom non mappati
    - Genera report delle modifiche
"""

import os
import re
import sys
from pathlib import Path
from typing import Dict, List, Tuple

# Mapping delle dimensioni Spacer ai nuovi SpacingSize
SIZE_MAPPING = {
    "4.dp": "SpacingSize.XXXS",
    "6.dp": "SpacingSize.XXS",
    "8.dp": "SpacingSize.XS",
    "12.dp": "SpacingSize.SM",
    "16.dp": "SpacingSize.MD",
    "20.dp": "SpacingSize.ML",
    "24.dp": "SpacingSize.LG",
    "32.dp": "SpacingSize.XL",
    "48.dp": "SpacingSize.XXL",
    "72.dp": "SpacingSize.XXXL",  # Fallback per 72dp
    "80.dp": "SpacingSize.XXXL",
}

ANDROID_APP_PATH = Path(__file__).parent.parent / "androidApp"
EXCLUDE_DIRS = {".gradle", "build", ".idea", "generated"}


def should_skip_file(file_path: Path) -> bool:
    """Determina se un file dovrebbe essere saltato."""
    # Skip file in directory escluse
    for exclude in EXCLUDE_DIRS:
        if exclude in file_path.parts:
            return True
    return False


def extract_spacer_calls(content: str) -> List[Tuple[str, str]]:
    """
    Estrae tutte le chiamate a Spacer dal contenuto.
    Ritorna lista di tuple (match_completo, tipo_spacer).
    """
    matches = []

    # Pattern per Spacer(modifier = Modifier.height(Xdp))
    pattern_height = r'Spacer\s*\(\s*modifier\s*=\s*Modifier\.height\s*\(\s*(\d+\.dp)\s*\)\s*\)'
    for match in re.finditer(pattern_height, content):
        size = match.group(1)
        full_match = match.group(0)
        matches.append((full_match, size, "height"))

    # Pattern per Spacer(modifier = Modifier.width(Xdp))
    pattern_width = r'Spacer\s*\(\s*modifier\s*=\s*Modifier\.width\s*\(\s*(\d+\.dp)\s*\)\s*\)'
    for match in re.finditer(pattern_width, content):
        size = match.group(1)
        full_match = match.group(0)
        matches.append((full_match, size, "width"))

    return matches


def needs_app_spacer_imports(content: str, has_app_spacer: bool) -> bool:
    """Determina se il file necessita degli import di AppSpacer."""
    return has_app_spacer and "VerticalSpacer" in content or "HorizontalSpacer" in content


def remove_old_spacer_import(content: str) -> str:
    """Rimuove l'import di Spacer se non più necessario."""
    lines = content.split("\n")
    new_lines = []
    for line in lines:
        # Skip l'import di Spacer da androidx.compose.foundation.layout
        if "import androidx.compose.foundation.layout.Spacer" in line:
            continue
        new_lines.append(line)

    return "\n".join(new_lines)


def add_app_spacer_imports(content: str) -> str:
    """Aggiunge gli import di AppSpacer se non presenti."""
    if "import com.antcashmanager.android.ui.components.layout.VerticalSpacer" in content:
        return content  # Già presente

    lines = content.split("\n")
    new_lines = []
    import_section_ended = False

    for i, line in enumerate(lines):
        new_lines.append(line)

        # Aggiungi i nuovi import dopo l'ultimo import androidx.compose
        if (
            not import_section_ended
            and line.startswith("import androidx.compose")
            and (
                i + 1 >= len(lines)
                or not lines[i + 1].startswith("import androidx.compose")
            )
        ):
            new_lines.append(
                "import com.antcashmanager.android.ui.components.layout.SpacingSize"
            )
            new_lines.append(
                "import com.antcashmanager.android.ui.components.layout.VerticalSpacer"
            )
            new_lines.append(
                "import com.antcashmanager.android.ui.components.layout.HorizontalSpacer"
            )
            import_section_ended = True

    return "\n".join(new_lines)


def convert_spacers_in_file(file_path: Path) -> Tuple[bool, Dict]:
    """
    Converte gli spacer in un file.
    Ritorna (file_modificato, dettagli_modifiche).
    """
    try:
        content = file_path.read_text(encoding="utf-8")
    except Exception as e:
        return False, {"error": str(e)}

    original_content = content
    replacements = []

    # Estrai tutte le chiamate a Spacer
    spacer_calls = extract_spacer_calls(content)

    if not spacer_calls:
        return False, {"replacements": []}

    # Sostituisci gli spacer
    for full_match, size, direction in spacer_calls:
        if size not in SIZE_MAPPING:
            replacements.append(
                {
                    "original": full_match,
                    "status": "skipped",
                    "reason": f"Size {size} not in mapping",
                }
            )
            continue

        spacing_size = SIZE_MAPPING[size]

        if direction == "height":
            replacement = f"VerticalSpacer({spacing_size})"
        else:  # width
            replacement = f"HorizontalSpacer({spacing_size})"

        content = content.replace(full_match, replacement)
        replacements.append(
            {
                "original": full_match,
                "replacement": replacement,
                "status": "replaced",
                "direction": direction,
                "size": size,
            }
        )

    # Aggiungi import se necessario
    if replacements:
        content = add_app_spacer_imports(content)
        # Rimuovi Spacer import solo se non ci sono altri Spacer non convertiti
        if "Spacer(modifier = Modifier" not in content:
            content = remove_old_spacer_import(content)

    # Scrivi il file se modificato
    if content != original_content:
        try:
            file_path.write_text(content, encoding="utf-8")
            return True, {"replacements": replacements, "file": str(file_path)}
        except Exception as e:
            return False, {"error": str(e), "file": str(file_path)}

    return False, {"replacements": replacements}


def main():
    """Funzione principale."""
    print("🚀 Avvio conversione Spacer → AppSpacer")
    print(f"📁 Target: {ANDROID_APP_PATH}")
    print()

    if not ANDROID_APP_PATH.exists():
        print(f"❌ Errore: {ANDROID_APP_PATH} non trovato!")
        sys.exit(1)

    # Trova tutti i file .kt
    kt_files = list(ANDROID_APP_PATH.rglob("*.kt"))
    kt_files = [f for f in kt_files if not should_skip_file(f)]

    print(f"📊 File Kotlin trovati: {len(kt_files)}")
    print()

    modified_count = 0
    skipped_count = 0
    total_replacements = 0
    files_with_errors = []

    for file_path in kt_files:
        modified, details = convert_spacers_in_file(file_path)

        if "error" in details:
            files_with_errors.append((file_path, details["error"]))
            skipped_count += 1
            continue

        replacements = details.get("replacements", [])

        if modified:
            replaced = sum(1 for r in replacements if r["status"] == "replaced")
            total_replacements += replaced
            modified_count += 1

            # Mostra il resoconto per il file
            rel_path = file_path.relative_to(ANDROID_APP_PATH)
            print(f"✅ {rel_path}")
            for replacement in replacements:
                if replacement["status"] == "replaced":
                    print(
                        f"   • {replacement['direction']}: {replacement['size']} → {replacement['replacement']}"
                    )

    print()
    print("=" * 70)
    print("📈 RIEPILOGO")
    print("=" * 70)
    print(f"File modificati: {modified_count}")
    print(f"Sostituzioni totali: {total_replacements}")
    print(f"File saltati: {skipped_count}")

    if files_with_errors:
        print()
        print("⚠️  File con errori:")
        for file_path, error in files_with_errors:
            rel_path = file_path.relative_to(ANDROID_APP_PATH)
            print(f"   • {rel_path}: {error}")

    print()
    print("✨ Conversione completata!")
    print()
    print("📝 Prossimi passi:")
    print("   1. Verifica le modifiche: git diff")
    print("   2. Esegui il build: ./gradlew build")
    print("   3. Esegui i test: ./gradlew test")
    print("   4. Commit: git add -A && git commit -m 'refactor: standardize spacers with AppSpacer'")


if __name__ == "__main__":
    main()

