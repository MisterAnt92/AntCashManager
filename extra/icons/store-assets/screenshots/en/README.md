# English Screenshot Pack

This folder contains the localized screenshot set for the English store listing of AntCashManager.

## Status

- **Language**: English (`en`)
- **Target**: Google Play Store listing screenshots
- **Required device format**: Phone portrait `1080x1920 px`
- **Optional device format**: Tablet landscape `1280x720 px`
- **Current state**: Pack initialized, capture pending on a connected Android device/emulator

## Required Files

Create the following screenshots in `phone/`:

1. `01_dashboard.png`
2. `02_add_transaction.png`
3. `03_categories.png`
4. `04_charts.png`
5. `05_scan_receipt.png`
6. `06_settings.png`
7. `07_backup_restore.png`
8. `08_dark_mode.png`

Optional tablet equivalents can be stored in `tablet/` with the same naming pattern.

## Capture Command

When a device or emulator is connected, launch the English capture flow from the project root:

```bash
cd /opt/src/GIT/app/AntCashManager/extra/icons
bash store-assets/capture_screenshots.sh en
```

## Notes

- Use localized English UI strings in the app before capture.
- Avoid exposing personal data, real transactions, or sensitive information.
- Review the final images before uploading them to Google Play Console.

