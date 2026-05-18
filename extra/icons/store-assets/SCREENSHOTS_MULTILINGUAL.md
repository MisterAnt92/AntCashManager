# Screenshot Management - Multilingua

Guida per gestire gli screenshot dell'app in diverse lingue per Google Play Store.

## Panoramica

Google Play Store permette di caricare screenshot diversi per ogni lingua. Questo è cruciale per:
- Mantenere coerenza linguistica con l'app
- Massimizzare il conversion rate nelle diverse lingue
- Mostrare l'app nel contesto culturale dell'utente

AntCashManager supporta **5 lingue**:
1. 🇮🇹 **Italiano** (it)
2. 🇬🇧 **Inglese** (en)
3. 🇩🇪 **Tedesco** (de)
4. 🇫🇷 **Francese** (fr)
5. 🇪🇸 **Spagnolo** (es)

---

## Struttura Cartelle per Screenshot

```
extra/icons/store-assets/screenshots/
├── it/                              # Italiano (lingua principale)
│   ├── phone/
│   │   ├── 01_dashboard.png        # Dashboard - 1080x1920 px
│   │   ├── 02_add_transaction.png  # Aggiungi transazione
│   │   ├── 03_categories.png       # Categorie e statistiche
│   │   ├── 04_charts.png           # Grafici e analisi
│   │   ├── 05_scan_receipt.png     # Scansione ricevute (OCR)
│   │   ├── 06_settings.png         # Impostazioni
│   │   ├── 07_backup_restore.png   # Backup e restore
│   │   └── 08_dark_mode.png        # Dark mode
│   └── tablet/
│       ├── 01_dashboard_tablet.png # Tablet layout - 1280x720 px
│       └── ...
│
├── en/
│   ├── phone/
│   │   ├── 01_dashboard.png
│   │   ├── 02_add_transaction.png
│   │   └── ...
│   └── tablet/
│
├── de/
│   ├── phone/
│   │   └── ...
│   └── tablet/
│
├── fr/
│   ├── phone/
│   │   └── ...
│   └── tablet/
│
└── es/
    ├── phone/
    │   └── ...
    └── tablet/
```

---

## Configurazione Lingua nell'Emulatore/Device

### Cambiare lingua di sistema

Per catturare screenshot in diverse lingue:

#### Opzione 1: Android Emulator (Consigliato)

```bash
# Avviare l'emulatore
emulator -avd Pixel_6_API_30

# Nel device:
# Settings → System → Languages and input → Languages → Add a language
```

#### Opzione 2: Device Fisico

```bash
# Connetti device
adb devices

# Cambia lingua da terminal
adb shell pm grant com.android.settings android.permission.CHANGE_CONFIGURATION

# Settings → System → Languages and input → Languages → Add language
```

#### Opzione 3: Script Automatico (Avanzato)

Usa lo script `set_locale.sh` che fornirò per cambiare linguaggio da terminal.

---

## Come Catturare gli Screenshot

### Step 1: Configura la Lingua

```bash
# Italiano
adb shell am broadcast -a com.android.intent.action.LOCALE -e com.android.intent.extra.LOCALE it

# Oppure manualmente:
# Settings → System → Languages & input → Languages → Italiano
```

### Step 2: Apri l'App e Naviga alla Schermata

```bash
# Avvia l'app
adb shell am start -n com.sformica.ant_cashmanager/.MainActivity

# Attendi il caricamento (5-10 secondi)
```

### Step 3: Cattura Screenshot

#### Con Android Studio
1. **Device Manager** → seleziona device
2. **Tools** → **Device Manager**
3. **Screenshot button** dalla toolbar
4. Salva in: `extra/icons/store-assets/screenshots/{lingua}/phone/`

#### Con ADB
```bash
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png ./screenshot_it_01.png
adb shell rm /sdcard/screenshot.png
```

#### Script Automatico
```bash
bash capture_screenshots.sh it
```

---

## Nomenclatura dei File

Usa questa convenzione per mantenere ordine:

```
{LINGUA}/phone/{NUM:2d}_{FEATURE}_{VARIANT}.png

Esempi:
- it/phone/01_dashboard.png
- it/phone/02_add_transaction_light.png
- it/phone/03_categories_dark.png
- en/phone/01_dashboard.png
- de/tablet/01_dashboard_landscape.png
```

**Naming Convention Dettagliato**:

| Campo | Valori | Esempio |
|-------|--------|---------|
| `{LINGUA}` | it, en, de, fr, es | `it` |
| `{DEVICE}` | phone, tablet | `phone` |
| `{NUM}` | 01-08 (ordine flow) | `01` |
| `{FEATURE}` | dashboard, add_transaction, categories, charts, scan_receipt, settings, backup_restore, dark_mode | `dashboard` |
| `{VARIANT}` | light, dark (opzionale) | `light` |

---

## Screenshot da Catturare (Per Lingua)

### Sequenza Consigliata (8 screenshot)

Per ogni lingua, cattura in questo ordine:

1. **Dashboard Principale** (Light Mode)
   - Mostra riepilogo transazioni
   - Saldo totale
   - Ultimi movimenti
   - Icone categorie
   - Nomina: `01_dashboard.png`

2. **Aggiungi Transazione**
   - Form per nuova transazione
   - Selezione categoria
   - Input importo
   - Nomina: `02_add_transaction.png`

3. **Categorie e Statistiche**
   - Lista categorie con importi
   - Breakdown percentuale
   - Nomina: `03_categories.png`

4. **Grafici e Analisi**
   - Grafico mensilità
   - Trend spesa
   - Filtri temporali
   - Nomina: `04_charts.png`

5. **Scansione Ricevute (OCR)**
   - Camera screen
   - Receipt scanning preview
   - Extracted data
   - Nomina: `05_scan_receipt.png`

6. **Impostazioni**
   - Preferenze tema
   - Lingua
   - Notifiche
   - Nomina: `06_settings.png`

7. **Backup e Restore**
   - Data Management section
   - Backup/Restore actions
   - Nomina: `07_backup_restore.png`

8. **Dark Mode**
   - App in dark mode
   - Mostra diversità di design
   - Nomina: `08_dark_mode.png`

---

## Script di Automazione

### Script 1: Cattura Screenshot per Lingua

Creerò uno script `capture_screenshots.sh`:

```bash
bash capture_screenshots.sh it     # Cattura screenshot in italiano
bash capture_screenshots.sh en     # Cattura screenshot in inglese
bash capture_screenshots.sh de     # Cattura screenshot in tedesco
```

### Script 2: Ottimizzazione Screenshot

```bash
bash optimize_screenshots.sh       # Comprimi/ridimensiona screenshot
```

### Script 3: Upload su Play Store

```bash
bash upload_screenshots_to_play_store.sh it  # Upload versione italiana
```

---

## Checklist per Ogni Lingua

Prima di finire con una lingua, verifica:

- [ ] 8 screenshot catturati (phone)
- [ ] 2-3 screenshot tablet (opzionale)
- [ ] Tutti i file nella cartella corretta: `{lingua}/phone/`
- [ ] Nomenclatura coerente: `01_dashboard.png`, `02_add_transaction.png`, etc.
- [ ] Risoluzione corretta:
  - Phone: 1080x1920 px (portrait)
  - Tablet: 1280x720 px (landscape)
- [ ] Nessun elemento non localizzato (button, text, etc. devono essere nella lingua)
- [ ] App in stabile (nessun loading indicator visibile)
- [ ] Screenshot leggibile su smartphone (non troppo piccoli)

---

## Processo Completo per Tutte le Lingue

### Setup Iniziale (Una volta)

```bash
# Crea struttura cartelle
mkdir -p extra/icons/store-assets/screenshots/{it,en,de,fr,es}/{phone,tablet}

# Preparazione emulatore
emulator -avd Pixel_6_API_30
```

### Per Ogni Lingua (Ripeti 5 volte)

```bash
# 1. Cambia lingua sistema
adb shell settings put system locale it-IT

# 2. Riavvia app
adb shell am force-stop com.sformica.ant_cashmanager
adb shell am start -n com.sformica.ant_cashmanager/.MainActivity

# 3. Cattura screenshot per ogni feature
bash capture_screenshots.sh it

# 4. Verifica qualità
# Apri: extra/icons/store-assets/screenshots/it/phone/
# Verifica che il testo sia in italiano
```

### Finale

```bash
# Verifica struttura completa
find extra/icons/store-assets/screenshots -type f -name "*.png" | wc -l
# Dovrebbe essere: 5 lingue × 8 screenshot = 40 file (phone)

# Upload a Play Console (vedi sezione successiva)
```

---

## Upload su Google Play Console

### Step 1: Accedi a Google Play Console

```
https://play.google.com/console
```

### Step 2: Vai a Store Listing

**App** → **Store Listing** → seleziona lingua

### Step 3: Upload Screenshots per Lingua

1. Seleziona lingua (es. **Italiano - it**)
2. Scorri a **Graphic Assets**
3. **Upload phone screenshots**:
   - Carica files da: `extra/icons/store-assets/screenshots/it/phone/`
   - Max 8 file
   - Ordine: 01, 02, 03, ... 08
4. (Opzionale) **Upload tablet screenshots**:
   - Carica files da: `extra/icons/store-assets/screenshots/it/tablet/`

### Step 4: Ripeti per Ogni Lingua

1. Cambia lingua in alto a sinistra: **Italiano** → **English** → **Deutsch** → **Français** → **Español**
2. Ripeti upload per ciascuna lingua

### Step 5: Verifica Anteprima

1. Vai a **Preview** (in alto a destra)
2. Seleziona device (Pixel 6a, Pixel Tablet, etc.)
3. Verifica che gli screenshot:
   - Mostrino il testo nella lingua corretta
   - Siano allineati
   - Siano leggibili

---

## Linee Guida di Design per Screenshots

### Testo negli Screenshot

- **Font**: Roboto (default Android)
- **Dimension**: Almeno 16sp per leggibilità
- **Colore**: Contrasto alto (4.5:1 minimo)
- **Lingua**: Sempre nella lingua dello screenshot

### Elementi da Includeere

✅ **Includi**:
- UI dell'app
- Dati di esempio (transazioni fittizie)
- Indicazioni visuali (freccie, highlight)
- Tagline/descrizione feature (se spazio)

❌ **NON includere**:
- Logo Google Play
- Loghi di terze parti senza autorizzazione
- Dati sensibili reali (es. numeri veri)
- Watermark di device manufacturer

### Template Annotazioni

Se vuoi aggiungere testo/freccie agli screenshot:

```bash
# Usando ImageMagick
convert input.png \
  -fill white -pointsize 40 -gravity South -annotate +0+50 "Salva le tue spese" \
  -stroke yellow -strokewidth 3 -draw "line 100,500 200,300" \
  output_annotated.png
```

---

## Troubleshooting

### "Screenshot non mostra testo in lingua corretta"

```bash
# Verifica lingua attuale
adb shell getprop persist.sys.locale

# Reimposta lingua
adb shell setprop persist.sys.locale it-IT
adb reboot
```

### "Screenshot distorto/non nitido"

```bash
# Assicurati di usare la giusta risoluzione
adb shell wm size 1080x1920  # Phone standard
adb shell wm size 1280x720   # Tablet landscape

# Verifica con screenshot
adb shell screencap -p /sdcard/test.png
```

### "Troppo tempo per catturare tutti gli screenshot"

Usa lo script batch:
```bash
for lang in it en de fr es; do
  echo "Catturando screenshot in $lang..."
  bash capture_screenshots.sh $lang
done
```

---

## Best Practices

1. **Coerenza**: Stessa sequenza di screenshot per ogni lingua
2. **Localizzazione**: Tutto il testo deve essere nella lingua target
3. **Qualità**: Screenshot nitidi, ben illuminate, senza glitch UI
4. **Ordine**: Segui sempre la sequenza suggerita (01-08)
5. **Testing**: Verifica anteprima su Play Console prima di pubblicare
6. **Aggiornamenti**: Quando aggiorni l'app, aggiorna anche gli screenshot

---

## Riferimenti

- [Google Play Screenshot Guidelines](https://support.google.com/googleplay/android-developer/answer/1078870)
- [Localization Checklist](https://developer.android.com/guide/playcore/in-app-updates/localization)
- [Material Design - Localization](https://m3.material.io/)

---

**Nota**: Gli screenshot dovrebbero essere aggiornati ogni volta che rilasci una nuova versione dell'app con significative modifiche UI.

