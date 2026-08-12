#!/usr/bin/env python3
"""
Script di conversione unificato per importare dati in AntCashManager.

Supporta:
- Backup file (.json)
- PiggyBank Pro export (.json)

Esporta nel formato debug_initial_data.json compatibile con l'ultima versione della entity Transaction:
- id: Long
- title: String
- amount: Double
- category: String
- type: TransactionType (INCOME|EXPENSE)
- timestamp: Long
- notes: String (opzionale)
- payee: String (opzionale)
- location: String (opzionale)
- isRecurring: Boolean
- tags: String (comma-separated, opzionale)
- recurrenceInterval: String (opzionale)
- paymentType: PaymentType (ELECTRONIC|CASH|MEAL_VOUCHERS)
- categoryIcon: String (opzionale)
- categoryColor: Long (hex color)

Uso:
    python3 convert_to_debug_data.py [input_file]
"""
import json
import sys
from pathlib import Path
import time
import random
from typing import Optional, Any, Dict, List

# Set seed for reproducible random distribution
random.seed(42)

def color_to_hex_long(color_str: Optional[str]) -> int:
    """
    Converte il formato colore "255:129:199:132" a Long hex (0xFFRRGGBB).
    Se il colore è vuoto/null, restituisce il grigio di default (0xFF90A4AE).
    """
    if not color_str or not color_str.strip():
        return 0xFF90A4AE

    parts = color_str.split(':')
    try:
        a, r, g, b = [int(p.strip()) for p in parts]
        # Formato: 0xAARRGGBB
        hex_value = (a << 24) | (r << 16) | (g << 8) | b
        return hex_value
    except (ValueError, IndexError):
        return 0xFF90A4AE


def assign_payment_type() -> str:
    """
    Assegna il tipo di pagamento casualmente:
    50% ELECTRONIC, 30% CASH, 20% MEAL_VOUCHERS
    """
    rand = random.random()
    if rand < 0.5:
        return 'ELECTRONIC'
    elif rand < 0.8:
        return 'CASH'
    else:
        return 'MEAL_VOUCHERS'


def get_optional_str(value: Any, default: str = '') -> str:
    """
    Restituisce una stringa pulita o una stringa vuota.
    Non restituisce mai la stringa letterale 'null'.
    Rimuove spazi e accoda rimozione di 'null' letterale.
    """
    if value is None:
        return default

    value_str = str(value).strip()

    if not value_str or value_str.lower() == 'null':
        return default

    return value_str


def parse_tags(tags_input: Any) -> str:
    """
    Converte i tag in una stringa comma-separated.
    Supporta: lista, string comma-separated, o None.
    """
    if not tags_input:
        return ''

    if isinstance(tags_input, list):
        # Filtra tag vuoti e unisci con virgola
        clean_tags = [str(t).strip() for t in tags_input if str(t).strip()]
        return ','.join(clean_tags) if clean_tags else ''
    elif isinstance(tags_input, str):
        # Se è già una stringa, puliscila
        tags = [t.strip() for t in tags_input.split(',') if t.strip()]
        return ','.join(tags) if tags else ''

    return ''


def get_category_icon(category_name: str, categories_map: Dict[str, Dict]) -> str:
    """Ottiene l'icona della categoria se disponibile."""
    if category_name in categories_map:
        cat = categories_map[category_name]
        icon = cat.get('icon', '')
        if icon and str(icon).strip().lower() != 'null':
            return str(icon).strip()
    return ''


def get_category_color(category_name: str, categories_map: Dict[str, Dict]) -> int:
    """Ottiene il colore della categoria se disponibile."""
    if category_name in categories_map:
        cat = categories_map[category_name]
        color_str = cat.get('color', '')
        if color_str:
            return color_to_hex_long(color_str)
    return 0xFF90A4AE


def convert_transactions(
    records: list,
    categories_map: Dict[str, Dict],
) -> list:
    """Converte la lista di transazioni nel formato della nuova entity."""
    transactions = []

    for rec in records:
        # Salta record senza valore
        value = rec.get('value')
        if value is None:
            continue

        tid = rec.get('id') or 0

        # Determina il tipo di transazione
        ctype = rec.get('category_type')
        if ctype is not None:
            tx_type = 'INCOME' if int(ctype) == 1 else 'EXPENSE'
        else:
            tx_type = 'INCOME' if float(value) > 0 else 'EXPENSE'

        # Titolo (fallback a "Senza titolo")
        title = get_optional_str(rec.get('title'), 'Senza titolo')

        # Categoria
        category = get_optional_str(rec.get('category_name'), 'Uncategorized').strip()

        # Timestamp
        timestamp = rec.get('datetime') or int(time.time() * 1000)

        # Note (opzionale)
        notes = get_optional_str(rec.get('description'), '')

        # Beneficiario (opzionale)
        payee = get_optional_str(rec.get('payee'), '')

        # Luogo (opzionale)
        location = get_optional_str(rec.get('location'), '')

        # Tag (opzionale - gestito come stringa comma-separated)
        tags = parse_tags(rec.get('tags'))

        # Ricorrenza
        is_recurring = bool(rec.get('isRecurring', False))
        recurrence_interval = get_optional_str(rec.get('recurrenceInterval'), '')

        # Tipo di pagamento - assegna random se non presente
        payment_type = get_optional_str(rec.get('paymentType'), '')
        if not payment_type or payment_type not in ['ELECTRONIC', 'CASH', 'MEAL_VOUCHERS']:
            payment_type = assign_payment_type()

        # Icona e colore categoria
        category_icon = get_category_icon(category, categories_map)
        category_color = get_category_color(category, categories_map)

        # Costruisci la transazione con TUTTI i campi della entity
        tx = {
            'id': int(tid),
            'title': title,
            'amount': float(value),
            'category': category,
            'type': tx_type,
            'timestamp': int(timestamp),
            'notes': notes,
            'payee': payee,
            'location': location,
            'isRecurring': is_recurring,
            'tags': tags,
            'recurrenceInterval': recurrence_interval,
            'paymentType': payment_type,
            'categoryIcon': category_icon,
            'categoryColor': category_color,
        }

        transactions.append(tx)

    return transactions


def convert_categories(categories: list) -> list:
    """Converte la lista di categorie."""
    normalized_categories = []

    for cat in categories:
        name = get_optional_str(cat.get('name'), 'Unknown')
        ctype = cat.get('category_type')

        # Tipo categoria: 0 = EXPENSE, 1 = INCOME
        cat_type = 'INCOME' if ctype and int(ctype) == 1 else 'EXPENSE'

        # Colore
        color = color_to_hex_long(cat.get('color'))

        # Icona (opzionale)
        icon = get_optional_str(cat.get('icon'), '')

        # Archiviata
        is_archived = bool(int(cat.get('is_archived', 0)))

        cat_obj = {
            'name': name,
            'type': cat_type,
            'color': f'0x{color:08X}',  # Formato esadecimale leggibile
            'isArchived': is_archived,
        }

        # Aggiungi icona solo se presente
        if icon:
            cat_obj['icon'] = icon

        normalized_categories.append(cat_obj)

    return normalized_categories


def build_categories_map(categories: list) -> Dict[str, Dict]:
    """Crea una mappa categoria_name -> categoria per lookup veloce."""
    categories_map = {}
    for cat in categories:
        name = cat.get('name', '').strip()
        if name:
            categories_map[name] = cat
    return categories_map


def convert_data(input_path: Path, output_path: Path) -> int:
    """Converte il file di input nel formato debug_initial_data.json."""
    if not input_path.exists():
        print(f'❌ File di input non trovato: {input_path}')
        return 1

    print(f'📖 Lettura dati da: {input_path}')

    try:
        data = json.loads(input_path.read_text(encoding='utf-8'))
    except json.JSONDecodeError as e:
        print(f'❌ Errore nella lettura del JSON: {e}')
        return 1
    except Exception as e:
        print(f'❌ Errore inaspettato: {e}')
        return 1

    records = data.get('records', [])
    categories = data.get('categories', [])

    print(f'📊 Trovate {len(records)} transazioni e {len(categories)} categorie')

    # Crea mappa categorie per lookup
    categories_map = build_categories_map(categories)

    # Converti transazioni e categorie
    transactions = convert_transactions(records, categories_map)
    normalized_categories = convert_categories(categories)

    print(f'✅ Convertite {len(transactions)} transazioni valide')
    print(f'✅ Convertite {len(normalized_categories)} categorie')

    # Crea struttura di output
    out = {
        'version': 'debug_initial_data',
        'package_name': data.get('package_name', 'com.antcashmanager'),
        'created_at': int(time.time() * 1000),
        'transactions': transactions,
        'categories': normalized_categories,
    }

    # Scrivi file di output
    try:
        output_path.parent.mkdir(parents=True, exist_ok=True)
        output_path.write_text(json.dumps(out, indent=2, ensure_ascii=False), encoding='utf-8')
        print(f'✅ Output scritto in: {output_path}')
        print(f'📋 Schema Transaction supportato:')
        print(f'   - id, title, amount, category, type')
        print(f'   - timestamp, notes, payee, location')
        print(f'   - isRecurring, tags (comma-separated), recurrenceInterval')
        print(f'   - paymentType (ELECTRONIC|CASH|MEAL_VOUCHERS)')
        print(f'   - categoryIcon, categoryColor')
        return 0
    except Exception as e:
        print(f'❌ Errore nella scrittura del file: {e}')
        return 1


def main():
    """Entry point dello script."""
    root = Path('/opt/src/GIT/app/AntCashManager')
    assets_dir = root / 'androidApp' / 'src' / 'main' / 'assets'

    # Accetta argomenti da linea di comando
    if len(sys.argv) == 1:
        # Nessun argomento: usa il PiggyBank Pro come default
        input_file = assets_dir / 'piggybankpro_data.json'
        output_file = assets_dir / 'debug_initial_data.json'
        print('ℹ️  Uso: python3 convert_to_debug_data.py [input_file]')
        print(f'ℹ️  Input di default: {input_file}')
        print(f'ℹ️  Output di default: {output_file}')
    elif len(sys.argv) == 2:
        input_file = Path(sys.argv[1])
        output_file = assets_dir / 'debug_initial_data.json'
    else:
        print('❌ Uso: python3 convert_to_debug_data.py [input_file]')
        return 1

    return convert_data(input_file, output_file)


if __name__ == '__main__':
    raise SystemExit(main())

