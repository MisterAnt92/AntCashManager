# PiggyBank Pro to AntCashManager Data Conversion

## Overview
This script converts PiggyBank Pro JSON data format to AntCashManager's debug_initial_data format.

## Usage
```bash
cd /opt/src/GIT/app/AntCashManager
python3 scripts/convert_piggybankpro_to_debug.py
```

## Input Format
- **File**: `androidApp/src/main/assets/piggybankpro_data.json`
- **Structure**: PiggyBank Pro export format with `records` and `categories` arrays

## Output Format  
- **File**: `androidApp/src/main/assets/debug_initial_data.json`
- **Structure**: AntCashManager debug format with `transactions` and `categories` arrays

## Conversion Details

### Transactions
- **Title**: Null titles converted to "Senza titolo"
- **Amount**: Preserves original values (negative for expenses, positive for income)
- **Type**: Determined by `category_type` field (0=EXPENSE, 1=INCOME)
- **Category**: Uses `category_name` field
- **Notes**: Maps from `description` field

### Categories
- **Type**: 0=EXPENSE, 1=INCOME
- **Color**: Converts from "255:129:199:132" format to "#FF81C784" hex format
- **Icon**: Preserves icon ID numbers
- **Archive Status**: Converts `is_archived` field to boolean

## Features
- ✅ Handles null values gracefully
- ✅ Preserves transaction amounts with correct signs
- ✅ Converts color format from ARGB to hex
- ✅ Maps all essential transaction fields
- ✅ Maintains data integrity

## Conversion Results
- **357 transactions** converted successfully
- **8 categories** with proper color conversion
- **79 income** and **286 expense** transactions
- All data validated and app compiles successfully

## Script Location
- **Main script**: `scripts/convert_piggybankpro_to_debug.py`
- **Backup script**: `scripts/convert_backup_to_debug.py` (for different format)

## Notes
- The script can be run multiple times safely (overwrites output file)
- Original PiggyBank Pro data is preserved
- Output format is compatible with AntCashManager app database structure
