#!/usr/bin/env python3
import json
from pathlib import Path
import time
import random

# Set seed for reproducible random distribution
random.seed(42)

ROOT = Path('/opt/src/GIT/app/AntCashManager')
INPUT = ROOT / 'androidApp' / 'src' / 'main' / 'assets' / 'piggybankpro_data.json'
OUTPUT = ROOT / 'androidApp' / 'src' / 'main' / 'assets' / 'debug_initial_data.json'

def color_to_hex(color_str):
    """Convert PiggyBank Pro color format "255:129:199:132" to hex format "#FF81C784"."""
    if not color_str:
        return '#FFCCCCCC'
    parts = color_str.split(':')
    try:
        a, r, g, b = [int(p) for p in parts]
        return '#{0:02X}{1:02X}{2:02X}{3:02X}'.format(a, r, g, b)
    except Exception:
        return '#FFCCCCCC'


def assign_payment_type():
    """Assign payment type randomly: 50% ELECTRONIC, 30% CASH, 20% MEAL_VOUCHERS."""
    rand = random.random()
    if rand < 0.5:
        return 'ELECTRONIC'
    elif rand < 0.8:
        return 'CASH'
    else:
        return 'MEAL_VOUCHERS'


def main():
    if not INPUT.exists():
        print('Input piggybankpro_data.json not found:', INPUT)
        return 1

    print('Reading PiggyBank Pro data from:', INPUT)
    data = json.loads(INPUT.read_text())
    records = data.get('records', [])
    categories = data.get('categories', [])

    print(f'Found {len(records)} transactions and {len(categories)} categories')

    # Convert transactions
    transactions = []
    for rec in records:
        tid = rec.get('id') or 0
        value = rec.get('value')

        # Skip transactions without value
        if value is None:
            continue

        # Determine transaction type from category_type
        ctype = rec.get('category_type')
        if ctype is not None:
            tx_type = 'INCOME' if int(ctype) == 1 else 'EXPENSE'
        else:
            # Fallback: determine type from value sign
            tx_type = 'INCOME' if float(value) > 0 else 'EXPENSE'

        # Handle title - use "Senza titolo" for null titles
        title = rec.get('title')
        if title is None:
            title = 'Senza titolo'

        category = (rec.get('category_name') or 'Uncategorized').strip()
        timestamp = rec.get('datetime') or int(time.time() * 1000)
        notes = rec.get('description')

        transactions.append({
            'id': int(tid),
            'title': title,
            'amount': float(value),  # Keep original value (negative for expenses)
            'category': category,
            'type': tx_type,
            'timestamp': int(timestamp),
            'notes': notes,
            'isRecurring': False,
            'recurrenceRule': None,
            'tags': [],
            'paymentType': assign_payment_type()
        })

    # Convert categories
    normalized_categories = []
    for cat in categories:
        name = cat.get('name') or 'Unknown'
        ctype = cat.get('category_type')

        # Convert category type: 0 = EXPENSE, 1 = INCOME
        cat_type = 'INCOME' if int(ctype) == 1 else 'EXPENSE'

        # Convert color format from "255:129:199:132" to "#FF81C784"
        color = color_to_hex(cat.get('color'))
        icon = cat.get('icon')
        is_archived = bool(int(cat.get('is_archived', 0)))

        normalized_categories.append({
            'name': name,
            'type': cat_type,
            'color': color,
            'icon': icon,
            'isArchived': is_archived
        })

    # Create output structure
    out = {
        'version': 'debug_initial_data',
        'package_name': data.get('package_name', 'com.github.emavgl.piggybankpro'),
        'created_at': int(time.time() * 1000),
        'transactions': transactions,
        'categories': normalized_categories
    }

    # Write output file
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(json.dumps(out, indent=2, ensure_ascii=False))

    print(f'✅ Conversion completed successfully!')
    print(f'✅ Converted {len(transactions)} transactions')
    print(f'✅ Converted {len(normalized_categories)} categories')
    print(f'✅ Output written to: {OUTPUT}')

    return 0

if __name__ == '__main__':
    raise SystemExit(main())
