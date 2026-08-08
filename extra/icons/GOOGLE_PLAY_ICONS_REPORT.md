# Google Play Icon Report

**Data generazione:** 2026-05-25  
**Progetto:** AntCashManager  
**Stato:** Icone principali generate e verificate

## Asset generati

| Asset                       | Percorso                                                                      | Dimensioni | Formato | Uso su Play Console       | Stato |
|-----------------------------|-------------------------------------------------------------------------------|-----------:|---------|---------------------------|-------|
| App icon principale         | `extra/icons/app-icons/png/app-icon-512.png`                                  |    512x512 | PNG     | Obbligatorio              | Ready |
| App icon legacy             | `extra/icons/app-icons/png/app-icon-192.png`                                  |    192x192 | PNG     | Facoltativo/compatibilita | Ready |
| Feature graphic             | `extra/icons/store-assets/feature-graphics/feature-graphic-1024x500.png`      |   1024x500 | PNG     | Obbligatorio              | Ready |
| Promo graphic               | `extra/icons/store-assets/promo-graphics/promo-180x120.png`                   |    180x120 | PNG     | Facoltativo               | Ready |
| Feature graphic (compressa) | `extra/icons/store-assets/feature-graphics/jpeg/feature-graphic-1024x500.jpg` |   1024x500 | JPG     | Alternativa upload        | Ready |
| Promo graphic (compressa)   | `extra/icons/store-assets/promo-graphics/jpeg/promo-180x120.jpg`              |    180x120 | JPG     | Alternativa upload        | Ready |

## Derivati Android (supporto interno)

| Asset   | Percorso                                                         | Dimensioni | Stato |
|---------|------------------------------------------------------------------|-----------:|-------|
| mdpi    | `extra/icons/app-icons/png/android-res/mdpi-app-icon-192.png`    |    192x192 | Ready |
| hdpi    | `extra/icons/app-icons/png/android-res/hdpi-app-icon-288.png`    |    288x288 | Ready |
| xhdpi   | `extra/icons/app-icons/png/android-res/xhdpi-app-icon-384.png`   |    384x384 | Ready |
| xxhdpi  | `extra/icons/app-icons/png/android-res/xxhdpi-app-icon-512.png`  |    512x512 | Ready |
| xxxhdpi | `extra/icons/app-icons/png/android-res/xxxhdpi-app-icon-768.png` |    768x768 | Ready |

## Note Google Play Store

1. Caricare obbligatoriamente:
    - `extra/icons/app-icons/png/app-icon-512.png`
    - `extra/icons/store-assets/feature-graphics/feature-graphic-1024x500.png`
2. Caricare facoltativamente:
    - `extra/icons/store-assets/promo-graphics/promo-180x120.png`
3. Screenshot: al momento non risultano screenshot PNG/JPG in
   `extra/icons/store-assets/screenshots/`.
   Vanno aggiunti prima della pubblicazione finale.

## Esito verifica

- Dimensioni critiche verificate via `identify`: OK.
- Conversione SVG -> PNG/JPG completata.
- Asset pronti per upload su Google Play Console.

