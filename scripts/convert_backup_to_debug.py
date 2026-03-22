#!/usr/bin/env python3
import json
from pathlib import Path
import time

ROOT = Path('/opt/src/GIT/app/AntCashManager')
INPUT = ROOT / 'androidApp' / 'src' / 'main' / 'assets' / '1.0.73_2026-03-20T08-40-08_backup.json'
OUTPUT = ROOT / 'androidApp' / 'src' / 'main' / 'assets' / 'debug_initial_data.json'

def color_to_hex(color_str):
    # expected format: "A:R:G:B" with integers 0-255
    if not color_str:
        return '#FFCCCCCC'
    parts = color_str.split(':')
    try:
        a, r, g, b = [int(p) for p in parts]
        return '#{0:02X}{1:02X}{2:02X}{3:02X}'.format(a, r, g, b)
    except Exception:
        return '#FFCCCCCC'


def main():
    if not INPUT.exists():
        print('Input backup not found:', INPUT)
        return 1
    data = json.loads(INPUT.read_text())
    records = data.get('records', [])
    categories = data.get('categories', [])

    transactions = []
    for rec in records:
        tid = rec.get('id') or 0
        value = rec.get('value')
        # if value is None skip
        if value is None:
            continue
        # determine type
        ctype = rec.get('category_type')
        if ctype is not None:
            tx_type = 'INCOME' if int(ctype) == 1 else 'EXPENSE'
        else:
            tx_type = 'INCOME' if float(value) > 0 else 'EXPENSE'
        title = rec.get('title') or 'Senza titolo'
        category = (rec.get('category_name') or 'Uncategorized').strip()
        timestamp = rec.get('datetime') or int(time.time() * 1000)
        notes = rec.get('description')

        transactions.append({
            'id': int(tid),
            'title': title,
            'amount': float(value),
            'category': category,
            'type': tx_type,
            'timestamp': int(timestamp),
            'notes': notes,
            'isRecurring': False,
            'recurrenceRule': None,
            'tags': []
        })

    normalized_categories = []
    for cat in categories:
        name = cat.get('name') or 'Unknown'
        ctype = cat.get('category_type')
        cat_type = 'INCOME' if int(ctype) == 1 else 'EXPENSE'
        color = color_to_hex(cat.get('color'))
        icon = cat.get('icon')
        normalized_categories.append({
            'name': name,
            'type': cat_type,
            'color': color,
            'icon': icon,
            'isArchived': bool(int(cat.get('is_archived', 0)))
        })

    out = {
        'version': 'debug_initial_data',
        'package_name': data.get('package_name', ''),
        'created_at': int(time.time() * 1000),
        'transactions': transactions,
        'categories': normalized_categories
    }

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(json.dumps(out, indent=2, ensure_ascii=False))
    print('Wrote', OUTPUT)
    return 0

if __name__ == '__main__':
    raise SystemExit(main())

