# AntCashManager - App Store Resources

Questo folder contiene tutte le risorse grafiche necessarie per la pubblicazione di AntCashManager su Google Play Store e altri marketplace.

## Struttura Cartelle

```
icons/
├── app-icons/           # Icone dell'app in vari formati
│   ├── svg/            # Versioni scalabili (192px, 512px)
│   ├── png/            # Versioni rasterizzate (per Android)
│   └── ico/            # Formato Windows/Web
├── store-assets/       # Risorse per Google Play Store
│   ├── feature-graphics/  # Feature graphic (1024x500 px)
│   ├── promo-graphics/    # Promo graphics (180x120 px)
│   ├── screenshots/       # Schermate dell'app
│   └── preview/        # Anteprime per testing
└── social-assets/      # Risorse social media
    ├── twitter-banner/    # Banner Twitter (1500x500 px)
    ├── facebook-cover/    # Copertina Facebook (820x312 px)
    └── instagram-story/   # Story Instagram (1080x1920 px)
```

## Google Play Store Requirements

### Obbligatori

- **App Icon**: 512x512 px (PNG, JPEG)
  - File: `app-icons/png/app-icon-512.png`
  - Usato come icona principale nel Play Store
  
- **Feature Graphic**: 1024x500 px (PNG, JPEG)
  - File: `store-assets/feature-graphics/feature-graphic-1024x500.png`
  - Immagine di presentazione principale nella pagina dell'app

- **Screenshots**: Min 2, Max 8 **per lingua**
  - Phone: 1080x1920 px (Portrait)
  - Tablet: 1280x720 px (Landscape) - opzionale
  - File: `store-assets/screenshots/{lingua}/phone/*.png`
  - Lingue supportate: it, en, de, fr, es
  - Vedi: [SCREENSHOTS_MULTILINGUAL.md](store-assets/SCREENSHOTS_MULTILINGUAL.md)

### Facoltativi (ma consigliati)

- **Promo Graphic**: 180x120 px
  - Per promozioni in store
  - File: `store-assets/promo-graphics/promo-180x120.png`

- **Icon (Legacy)**: 192x192 px
  - Usato in alcuni device Android precedenti
  - File: `app-icons/png/app-icon-192.png`

## Formati Disponibili

### SVG (Scalabili, raccomandati per editing)

- `app-icons/svg/app-icon-scalable.svg` - Versione 192x192 px base
- `app-icons/svg/app-icon-large.svg` - Versione 512x512 px
- `store-assets/feature-graphics/feature-graphic.svg` - Feature graphic scalabile

**Vantaggi**: Modificabili vettorialmente, senza perdita di qualità

### PNG (Per deployment)

- `app-icons/png/app-icon-192.png` - 192x192 px
- `app-icons/png/app-icon-512.png` - 512x512 px
- `store-assets/feature-graphics/feature-graphic-1024x500.png` - Feature graphic
- `store-assets/promo-graphics/promo-180x120.png` - Promo graphic

**Vantaggi**: Pronti per caricare su Play Store

## Come Generare PNG da SVG

### Opzione 1: Usando ImageMagick (raccomandato)

```bash
# Installare ImageMagick se non disponibile
sudo apt-get install imagemagick

# Convertire SVG a PNG con densità alta (per qualità)
convert -density 150 source.svg -resize 512x512 output-512.png
convert -density 150 source.svg -resize 192x192 output-192.png
convert -density 150 source.svg -resize 1024x500 output-1024x500.png
```

### Opzione 2: Usando Inkscape

```bash
inkscape --export-filename=output.png --export-width=512 --export-height=512 source.svg
```

### Opzione 3: Script di Conversione Automatica

Esegui lo script fornito:
```bash
bash convert_icons.sh
```

Questo script genera automaticamente tutti i PNG dai file SVG.

## Integrazione in Prodotto

### Android (androidApp/)

1. **Icona app in manifest**:
   - Copiare `app-icons/png/app-icon-192.png` in `androidApp/src/main/res/mipmap-xxxhdpi/`
   - Copiare in più risoluzioni: `mipmap-mdpi/`, `mipmap-hdpi/`, `mipmap-xhdpi/`, `mipmap-xxhdpi/`, `mipmap-xxxhdpi/`

2. **AndroidManifest.xml**:
   ```xml
   <application
       android:icon="@mipmap/ic_launcher"
       android:label="@string/app_name"
       ...>
   </application>
   ```

### Google Play Store

1. Accedi a [Google Play Console](https://play.google.com/console)
2. Seleziona l'app AntCashManager
3. Vai a **Store Listing** → **Graphic Assets**
4. Carica:
   - **App Icon**: `app-icon-512.png`
   - **Feature Graphic**: `feature-graphic-1024x500.png`
   - **Screenshots**: i file da `store-assets/screenshots/`
   - **Promo Graphic** (opzionale): `promo-180x120.png`

## Linee Guida di Design

### App Icon

- **Colori principali**: Rosa (#FFB3BA), Rosa scuro (#FF8A95), Oro (#FFD54F)
- **Stile**: Simpatico, amichevole, finanziario (porcellino salvadanaio)
- **Trasparenza**: Consentita
- **Versioni**: Light (default) e Dark (adattive, se supportate)

### Feature Graphic

- **Dimensioni**: 1024x500 px (aspect ratio 2.048:1)
- **Contenuto**: Showcase principale dell'app
  - Titolo app in alto
  - Icona o screenshot principale al centro
  - Claim/tagline in basso
  - Colori coerenti con l'app

### Screenshot

- **Contenuti consigliati**:
  1. Dashboard principale con riepilogo transazioni
  2. Aggiungi transazione
  3. Categorie e statistiche
  4. Grafici e analisi
  5. Impostazioni e backup
  6. Dark mode (se supportato)
  7. Funzionalità scanning ricevute (OCR)
  8. Export e condivisione dati

**Font consigliato**: Font sans-serif leggibile a piccole dimensioni (es. Roboto, Inter)

## Checklist Pre-Pubblicazione

- [ ] App Icon 512x512 px creata e testata su Play Store
- [ ] Feature Graphic 1024x500 px pronta
- [ ] Almeno 2 screenshot (up to 8) preparati
  - [ ] Phone: 1080x1920 px
  - [ ] Tablet: 1280x720 px
- [ ] Promo Graphic 180x120 px (opzionale ma consigliato)
- [ ] Tutti i PNG in formato JPEG o PNG senza trasparenze problematiche
- [ ] Nessun logo di terze parti senza autorizzazione
- [ ] Testo leggibile anche su schermi piccoli
- [ ] Brand consistency rispetto all'app
- [ ] Test su Play Console preview prima di pubblicare

## Aggiornamento Iconografia

Quando aggiorni l'icona:

1. Modifica il file SVG base (`app-icons/svg/app-icon-scalable.svg`)
2. Runna `bash convert_icons.sh` per generare tutti i PNG
3. Testa le icone su dispositivi reali
4. Carica nuove versioni su Play Store

## Note Importanti

- **Non usare Google Play logo**: Caricare su Play Store non autorizza a usarne il logo in marketing
- **Trasparenza in Android**: Assicurati che la trasparenza sia supportata nella versione minima target
- **Accessibilità**: Assicurati che il testo negli screenshot sia leggibile (contrast ratio ≥ 4.5:1)
- **Localizzazione**: Se usi testo negli screenshot, considera versioni localizzate in IT, EN, DE, FR, ES

## Riferimenti

- [Google Play Console Help](https://support.google.com/googleplay/android-developer)
- [Material Design Icon Guidelines](https://m3.material.io/foundations/iconography)
- [Android App Icon Specifications](https://developer.android.com/guide/practices/ui_guidelines/icon_design_launcher)
- [Play Store Asset Specifications](https://support.google.com/googleplay/android-developer/answer/1078870)

---

**Last Updated**: May 2026
**App Version**: 1.4.6
**Package**: com.sformica.ant_cashmanager

## Gestione Multilingue degli Screenshot

AntCashManager è disponibile in **5 lingue** e gli screenshot devono rispecchiare questa localizzazione.

### Lingue Supportate

| Lingua | Codice | Cartella |
|--------|--------|----------|
| 🇮🇹 Italiano | it | `screenshots/it/` |
| 🇬🇧 English | en | `screenshots/en/` |
| 🇩🇪 Deutsch | de | `screenshots/de/` |
| 🇫🇷 Français | fr | `screenshots/fr/` |
| 🇪🇸 Español | es | `screenshots/es/` |

### Workflow Rapido - Cattura Screenshot Multilingue

```bash
# 1. Verifica struttura cartelle
bash store-assets/verify_screenshots.sh

# 2. Cattura screenshot per UNA lingua
bash store-assets/capture_screenshots.sh it

# 3. Cattura screenshot per TUTTE le lingue
bash store-assets/capture_screenshots.sh --all

# 4. Ottimizza screenshot (ridimensiona/comprimi)
bash store-assets/optimize_screenshots.sh

# 5. Verifica completamento
bash store-assets/verify_screenshots.sh
```

### Dettagli - Screenshot per Lingua

Per ogni lingua, cattura **8 screenshot** nella sequenza:

1. **Dashboard** - Riepilogo transazioni e saldo
2. **Aggiungi Transazione** - Form di inserimento
3. **Categorie** - Breakdown per categoria
4. **Grafici** - Analisi e trend
5. **Scansione Ricevute** - OCR receipt scanning
6. **Impostazioni** - Settings e preferenze
7. **Backup/Restore** - Data management
8. **Dark Mode** - Theme alternativo

**Importante**: Il testo negli screenshot deve essere nella lingua corretta.

### Struttura Cartelle Screenshot

```
store-assets/
└── screenshots/
    ├── it/              # Italiano
    │   ├── phone/       # 8 screenshot 1080x1920 px
    │   │   ├── 01_dashboard.png
    │   │   ├── 02_add_transaction.png
    │   │   ├── 03_categories.png
    │   │   ├── 04_charts.png
    │   │   ├── 05_scan_receipt.png
    │   │   ├── 06_settings.png
    │   │   ├── 07_backup_restore.png
    │   │   └── 08_dark_mode.png
    │   └── tablet/      # Opzionale - landscape 1280x720 px
    │
    ├── en/              # English
    │   ├── phone/
    │   └── tablet/
    │
    ├── de/              # Deutsch
    │   ├── phone/
    │   └── tablet/
    │
    ├── fr/              # Français
    │   ├── phone/
    │   └── tablet/
    │
    └── es/              # Español
        ├── phone/
        └── tablet/
```

### Come Catturare Screenshot

**Prerequisiti:**
- Android emulator o device connesso con `adb`
- App compilata e installata

**Steps:**

```bash
# 1. Verifica device connesso
adb devices

# 2. Cattura per italiano (guided mode)
bash store-assets/capture_screenshots.sh it

# 3. Lo script guiderà per ogni screenshot:
#    - Cambia lingua automaticamente
#    - Cattura screenshot quando pronto
#    - Attendi prompt per navigare alla prossima schermata
#    - Premi ENTER per continuare

# 4. Reperti per altri linguaggi
bash store-assets/capture_screenshots.sh en
bash store-assets/capture_screenshots.sh de
bash store-assets/capture_screenshots.sh fr
bash store-assets/capture_screenshots.sh es
```

### Strumenti Forniti

#### 1. `capture_screenshots.sh` - Cattura screenshot
```bash
bash store-assets/capture_screenshots.sh it        # Una lingua
bash store-assets/capture_screenshots.sh --all     # Tutte le lingue
```

#### 2. `optimize_screenshots.sh` - Ottimizza file
```bash
bash store-assets/optimize_screenshots.sh
# Ridimensiona, comprime, crea versioni thumbnail
```

#### 3. `verify_screenshots.sh` - Verifica completamento
```bash
bash store-assets/verify_screenshots.sh
# Verifica che tutti gli screenshot siano presenti
# Mostra statistiche e suggerimenti
```

### Upload su Google Play Console

Per ogni lingua:

1. Accedi a [Google Play Console](https://play.google.com/console)
2. **Store Listing** → seleziona **Italiano**
3. Scorri a **Graphic Assets** → **Phone screenshots**
4. Upload files da: `store-assets/screenshots/it/phone/`
5. Ripeti per: **English** → **Deutsch** → **Français** → **Español**

---

## Riferimento Completo - Screenshot Multilingue

Per la documentazione completa sulla gestione multilingue degli screenshot, incluso:
- Come configurare l'emulatore per diverse lingue
- Best practices di design per screenshot
- Troubleshooting
- Localizzazione di testo negli screenshot

Vedi: **[SCREENSHOTS_MULTILINGUAL.md](store-assets/SCREENSHOTS_MULTILINGUAL.md)**

---
