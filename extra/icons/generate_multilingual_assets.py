#!/usr/bin/env python3
"""Generate localized store and social graphics for AntCashManager."""

from __future__ import annotations

import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parent

FEATURE_BASE = ROOT / "store-assets/feature-graphics/feature-graphic.svg"
PROMO_BASE = ROOT / "store-assets/promo-graphics/promo-180x120.svg"
TWITTER_BASE = ROOT / "social-assets/twitter-banner/twitter-banner-1500x500.svg"

LANGS = {
    "it": {
        "feature": {
            "Gestione Finanze": "Gestione Finanze",
            "Intelligente": "Intelligente",
            "Controlla le tue spese con stile": "Controlla le tue spese con stile",
            "Backup sicuro • Grafici intuitivi • Sempre con te": "Backup sicuro \u2022 Grafici intuitivi \u2022 Sempre con te",
            "Backup Sicuro": "Backup Sicuro",
            "Grafici Intuitivi": "Grafici Intuitivi",
        },
        "promo": {
            "Gestione Finanze": "Gestione Finanze",
            "Intelligente": "Intelligente",
            "v1.4.6 • Gratis": "v1.4.6 \u2022 Gratis",
        },
        "twitter": {
            "Finanze intelligenti": "Finanze intelligenti",
            "Scarica ora su Google Play": "Scarica ora su Google Play",
            "Funzionalità": "Funzionalita",
            "Transazioni": "Transazioni",
            "Gestisci entrate e uscite": "Gestisci entrate e uscite",
            "Grafici": "Grafici",
            "Analizza le tue spese": "Analizza le tue spese",
            "Backup": "Backup",
            "Dati sempre al sicuro": "Dati sempre al sicuro",
        },
    },
    "en": {
        "feature": {
            "Gestione Finanze": "Finance Management",
            "Intelligente": "Made Smart",
            "Controlla le tue spese con stile": "Track your spending with style",
            "Backup sicuro • Grafici intuitivi • Sempre con te": "Secure backup \u2022 Intuitive charts \u2022 Always with you",
            "Backup Sicuro": "Secure Backup",
            "Grafici Intuitivi": "Intuitive Charts",
        },
        "promo": {
            "Gestione Finanze": "Finance Management",
            "Intelligente": "Made Smart",
            "v1.4.6 • Gratis": "v1.4.6 \u2022 Free",
        },
        "twitter": {
            "Finanze intelligenti": "Smart finance",
            "Scarica ora su Google Play": "Download now on Google Play",
            "Funzionalità": "Features",
            "Transazioni": "Transactions",
            "Gestisci entrate e uscite": "Manage income and expenses",
            "Grafici": "Charts",
            "Analizza le tue spese": "Analyze your spending",
            "Backup": "Backup",
            "Dati sempre al sicuro": "Your data always safe",
        },
    },
    "de": {
        "feature": {
            "Gestione Finanze": "Finanzverwaltung",
            "Intelligente": "Intelligent",
            "Controlla le tue spese con stile": "Verfolge Ausgaben mit Stil",
            "Backup sicuro • Grafici intuitivi • Sempre con te": "Sicheres Backup \u2022 Intuitive Charts \u2022 Immer dabei",
            "Backup Sicuro": "Sicheres Backup",
            "Grafici Intuitivi": "Intuitive Charts",
        },
        "promo": {
            "Gestione Finanze": "Finanzverwaltung",
            "Intelligente": "Intelligent",
            "v1.4.6 • Gratis": "v1.4.6 \u2022 Kostenlos",
        },
        "twitter": {
            "Finanze intelligenti": "Intelligente Finanzen",
            "Scarica ora su Google Play": "Jetzt bei Google Play laden",
            "Funzionalità": "Funktionen",
            "Transazioni": "Transaktionen",
            "Gestisci entrate e uscite": "Einnahmen und Ausgaben verwalten",
            "Grafici": "Diagramme",
            "Analizza le tue spese": "Analysiere deine Ausgaben",
            "Backup": "Backup",
            "Dati sempre al sicuro": "Daten immer sicher",
        },
    },
    "fr": {
        "feature": {
            "Gestione Finanze": "Gestion Financiere",
            "Intelligente": "Intelligente",
            "Controlla le tue spese con stile": "Suivez vos depenses avec style",
            "Backup sicuro • Grafici intuitivi • Sempre con te": "Sauvegarde securisee \u2022 Graphiques intuitifs \u2022 Toujours avec vous",
            "Backup Sicuro": "Sauvegarde securisee",
            "Grafici Intuitivi": "Graphiques intuitifs",
        },
        "promo": {
            "Gestione Finanze": "Gestion Financiere",
            "Intelligente": "Intelligente",
            "v1.4.6 • Gratis": "v1.4.6 \u2022 Gratuit",
        },
        "twitter": {
            "Finanze intelligenti": "Finance intelligente",
            "Scarica ora su Google Play": "Telechargez sur Google Play",
            "Funzionalità": "Fonctionnalites",
            "Transazioni": "Transactions",
            "Gestisci entrate e uscite": "Gerez revenus et depenses",
            "Grafici": "Graphiques",
            "Analizza le tue spese": "Analysez vos depenses",
            "Backup": "Sauvegarde",
            "Dati sempre al sicuro": "Donnees toujours securisees",
        },
    },
    "es": {
        "feature": {
            "Gestione Finanze": "Gestion Financiera",
            "Intelligente": "Inteligente",
            "Controlla le tue spese con stile": "Controla tus gastos con estilo",
            "Backup sicuro • Grafici intuitivi • Sempre con te": "Copia segura \u2022 Graficos intuitivos \u2022 Siempre contigo",
            "Backup Sicuro": "Copia segura",
            "Grafici Intuitivi": "Graficos intuitivos",
        },
        "promo": {
            "Gestione Finanze": "Gestion Financiera",
            "Intelligente": "Inteligente",
            "v1.4.6 • Gratis": "v1.4.6 \u2022 Gratis",
        },
        "twitter": {
            "Finanze intelligenti": "Finanzas inteligentes",
            "Scarica ora su Google Play": "Descarga ahora en Google Play",
            "Funzionalità": "Funciones",
            "Transazioni": "Transacciones",
            "Gestisci entrate e uscite": "Gestiona ingresos y gastos",
            "Grafici": "Graficos",
            "Analizza le tue spese": "Analiza tus gastos",
            "Backup": "Copia",
            "Dati sempre al sicuro": "Datos siempre seguros",
        },
    },
}


def replace_text(content: str, mapping: dict[str, str]) -> str:
    updated = content
    for source, target in mapping.items():
        updated = updated.replace(source, target)
    return updated


def convert_image(svg_path: Path, png_path: Path, jpg_path: Path, size: str) -> None:
    png_path.parent.mkdir(parents=True, exist_ok=True)
    jpg_path.parent.mkdir(parents=True, exist_ok=True)

    subprocess.run(
        [
            "convert",
            "-density",
            "150",
            str(svg_path),
            "-resize",
            f"{size}!",
            "-background",
            "none",
            str(png_path),
        ],
        check=True,
    )
    subprocess.run(
        ["convert", str(png_path), "-quality", "90", str(jpg_path)],
        check=True,
    )


def generate_assets() -> None:
    feature_source = FEATURE_BASE.read_text(encoding="utf-8")
    promo_source = PROMO_BASE.read_text(encoding="utf-8")
    twitter_source = TWITTER_BASE.read_text(encoding="utf-8")

    for lang, values in LANGS.items():
        feature_svg = ROOT / f"store-assets/feature-graphics/localized/{lang}/feature-graphic-1024x500.svg"
        feature_png = ROOT / f"store-assets/feature-graphics/localized/{lang}/feature-graphic-1024x500.png"
        feature_jpg = ROOT / f"store-assets/feature-graphics/localized/{lang}/feature-graphic-1024x500.jpg"

        promo_svg = ROOT / f"store-assets/promo-graphics/localized/{lang}/promo-180x120.svg"
        promo_png = ROOT / f"store-assets/promo-graphics/localized/{lang}/promo-180x120.png"
        promo_jpg = ROOT / f"store-assets/promo-graphics/localized/{lang}/promo-180x120.jpg"

        twitter_svg = ROOT / f"social-assets/twitter-banner/localized/{lang}/twitter-banner-1500x500.svg"
        twitter_png = ROOT / f"social-assets/twitter-banner/localized/{lang}/twitter-banner-1500x500.png"
        twitter_jpg = ROOT / f"social-assets/twitter-banner/localized/{lang}/twitter-banner-1500x500.jpg"

        feature_svg.parent.mkdir(parents=True, exist_ok=True)
        promo_svg.parent.mkdir(parents=True, exist_ok=True)
        twitter_svg.parent.mkdir(parents=True, exist_ok=True)

        feature_svg.write_text(replace_text(feature_source, values["feature"]), encoding="utf-8")
        promo_svg.write_text(replace_text(promo_source, values["promo"]), encoding="utf-8")
        twitter_svg.write_text(replace_text(twitter_source, values["twitter"]), encoding="utf-8")

        convert_image(feature_svg, feature_png, feature_jpg, "1024x500")
        convert_image(promo_svg, promo_png, promo_jpg, "180x120")
        convert_image(twitter_svg, twitter_png, twitter_jpg, "1500x500")

        print(f"Generated localized assets for {lang}")


if __name__ == "__main__":
    generate_assets()

