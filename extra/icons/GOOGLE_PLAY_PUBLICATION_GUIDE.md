# Google Play Store Publication Guide - AntCashManager

Questa guida ti accompagna passo dopo passo nel processo di pubblicazione di AntCashManager su Google Play Store, utilizzando le risorse grafiche fornite nella cartella `extra/icons/`.

## Indice

1. [Preparazione delle Risorse](#preparazione-delle-risorse)
2. [Configurazione Google Play Console](#configurazione-google-play-console)
3. [Upload degli Asset](#upload-degli-asset)
4. [Store Listing Completo](#store-listing-completo)
5. [Testing e Verifica](#testing-e-verifica)
6. [Pubblicazione](#pubblicazione)
7. [Manutenzione Post-Pubblicazione](#manutenzione-post-pubblicazione)

---

## Preparazione delle Risorse

### Step 1: Generare i PNG dai SVG

Le icone sono fornite in formato SVG (vettoriale, modificabile). Per caricarle su Google Play Store, devi convertirle in PNG.

#### Opzione A: Script Automatico (Consigliato)

```bash
cd extra/icons/
bash convert_icons.sh
```

Questo script:
- Converte tutti gli SVG in PNG alle dimensioni corrette
- Crea versioni per diverse risoluzioni Android
- Genera anche versioni JPEG compresse
- Crea la struttura di cartelle necessaria

#### Opzione B: Conversione Manuale con ImageMagick

```bash
# Installare ImageMagick (se non disponibile)
sudo apt-get install imagemagick

# Convertire singoli file
convert -density 150 app-icons/svg/app-icon-512.svg -resize 512x512 app-icons/png/app-icon-512.png
convert -density 150 store-assets/feature-graphics/feature-graphic.svg -resize 1024x500 store-assets/feature-graphics/feature-graphic-1024x500.png
```

#### Opzione C: Usare Inkscape

```bash
# Installare Inkscape
sudo apt-get install inkscape

# Convertire
inkscape --export-filename=app-icon-512.png --export-width=512 --export-height=512 app-icons/svg/app-icon-512.svg
```

### Step 2: Verificare i File Generati

Dopo la conversione, dovresti avere:

```
extra/icons/
├── app-icons/
│   ├── png/
│   │   ├── app-icon-192.png        (✓ Obbligatorio)
│   │   ├── app-icon-512.png        (✓ Obbligatorio)
│   │   └── android-res/
│   │       ├── mdpi-app-icon-192.png
│   │       ├── hdpi-app-icon-288.png
│   │       ├── xhdpi-app-icon-384.png
│   │       ├── xxhdpi-app-icon-512.png
│   │       └── xxxhdpi-app-icon-768.png
│   └── svg/
│       ├── app-icon-192.svg
│       └── app-icon-512.svg
├── store-assets/
│   ├── feature-graphics/
│   │   ├── feature-graphic-1024x500.png    (✓ Obbligatorio)
│   │   └── jpeg/
│   │       └── feature-graphic-1024x500.jpg
│   ├── promo-graphics/
│   │   ├── promo-180x120.png               (Consigliato)
│   │   └── jpeg/
│   │       └── promo-180x120.jpg
│   ├── screenshots/                        (Aggiungere i tuoi screenshot)
│   └── preview/
└── social-assets/
    ├── twitter-banner/
    ├── facebook-cover/
    └── instagram-story/
```

---

## Configurazione Google Play Console

### Step 1: Accedere a Google Play Console

1. Vai su [Google Play Console](https://play.google.com/console)
2. Accedi con il tuo account Google Developer
3. Seleziona l'app **AntCashManager** dal dashboard

### Step 2: Preparare il Build Release

Prima di caricare gli asset, devi avere un APK/Bundle di release compilato.

```bash
# Compilare il build release
cd /opt/src/GIT/app/AntCashManager
./gradlew :androidApp:bundleRelease --no-daemon

# L'APK/Bundle si trova in:
# androidApp/build/outputs/bundle/release/app-release.aab
```

### Step 3: Creare un Release (Draft)

1. Vai a **Release** → **Internal Testing**
2. Clicca **Create new release**
3. Upload il file `app-release.aab`
4. Compila:
   - **Release name**: `1.4.6` (versione attuale)
   - **Release notes**: Note della versione (vedi sezione "Release Notes")
5. Salva come **Draft** (NON pubblicare ancora)

---

## Upload degli Asset

### Step 1: Navigare a Store Listing

1. Dalla console, vai a **Store Listing**
2. Seleziona la lingua principale: **Italiano**
3. Scorri fino a **Graphic Assets**

### Step 2: Upload Icone e Graphic Assets

#### App Icon (Obbligatorio)

1. Clicca su **App Icon** (o trascina)
2. Seleziona il file: `extra/icons/app-icons/png/app-icon-512.png`
3. Dimensioni richieste: 512x512 px ✓
4. Formato: PNG ✓
5. Upload completato

#### Feature Graphic (Obbligatorio)

1. Clicca su **Feature Graphic** (o trascina)
2. Seleziona il file: `extra/icons/store-assets/feature-graphics/feature-graphic-1024x500.png`
3. Dimensioni richieste: 1024x500 px (aspect ratio 2.048:1) ✓
4. Formato: PNG ✓
5. Upload completato

#### Screenshots (Minimo 2, Massimo 8)

Aggiungi screenshot della tua app in azione:

1. **Phone Screenshots** (1080x1920 px, Portrait)
   - Dashboard con transazioni
   - Aggiungi transazione
   - Categorie e statistiche
   - Grafici e analisi
   - Dark mode (se supportato)
   - Impostazioni
   - Backup/Restore

2. **Tablet Screenshots** (1280x720 px, Landscape) - Opzionale
   - Screenshot tablet della tua app

**Come catturare gli screenshot**:

```bash
# Usando Android emulator o device connesso
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png ./screenshot.png
```

Oppure usa lo strumento built-in Android Studio:
- **Device Manager** → seleziona device
- **Snapshot** dal Virtual Device Configuration

**Upload su Play Console**:
1. Clicca **Add screenshots**
2. Upload fino a 8 screenshot per device type (phone, tablet, wear, etc.)
3. Organizza in ordine logico (workflow utente)

#### Promo Graphic (Consigliato)

1. Clicca su **Promo Graphic** (se presente)
2. Seleziona il file: `extra/icons/store-assets/promo-graphics/promo-180x120.png`
3. Dimensioni: 180x120 px ✓
4. Upload completato

---

## Store Listing Completo

### Titolo App

```
AntCashManager
```

**Nota**: Max 50 caratteri

### Descrizione Breve

```
Gestisci le tue finanze con stile
```

**Nota**: Max 80 caratteri

### Descrizione Completa

```
AntCashManager è l'app intelligente per gestire le tue finanze personali.

✓ DASHBOARD INTUITIVO
Visualizza il riepilogo delle tue transazioni, saldo disponibile e trend di spesa con grafici chiari e facili da leggere.

✓ GESTIONE TRANSAZIONI
Aggiungi, modifica e categorizza le tue entrate e uscite. Filtri potenti per trovare facilmente ciò che cerchi.

✓ SCANSIONE RICEVUTE (OCR)
Fotografa le ricevute e l'app estrae automaticamente i dati (importo, data, categoria) grazie alla tecnologia OCR.

✓ STATISTICHE E GRAFICI
Analizza le tue abitudini di spesa con grafici dettagliati per categoria, periodo e trend.

✓ BACKUP SICURO
Esporta i tuoi dati in formato JSON con crittografia end-to-end. Ripristina in qualsiasi momento.

✓ DARK MODE
Supporto completo per il tema scuro per una migliore leggibilità in ambienti poco illuminati.

✓ MULTILINGUE
Disponibile in: Italiano, Inglese, Tedesco, Francese, Spagnolo.

✓ PRIVACY-FOCUSED
Nessun dato personale raccolto. Solo dati di utilizzo anonimizzati per migliorare l'app.

CARATTERISTICHE TECNICHE:
- Offline-first: funziona senza connessione internet
- Database locale criptato su device
- Nessun server remoto necessario
- Nessuna registrazione richiesta
- Nessuna pubblicità

REQUISITI:
- Android 7.0+
- 50 MB spazio disponibile
- Connessione internet opzionale (solo per aggiornamenti)

Feedback e suggerimenti? Contattaci tramite l'app o visita il nostro repository GitHub.

Privacy Policy: Leggi la nostra politica sulla privacy in-app.
```

### Categoria

```
Finanza
```

### Valutazione Contenuti

Seleziona:
- **Violenza**: No content
- **Contenuto sessuale**: No content
- **Uso di alcol/tabacco**: No content
- **Non-medical treatment of illness**: No content
- **Contenuto politico**: No content
- **Religione**: No content
- **Altre restrizioni**: No content

### Email Contatti

```
support@antcashmanager.example.com  (se disponibile)
o il tuo email personale
```

### URL Sito Web

Lascia vuoto se non disponibile, oppure:
```
https://github.com/tuo-username/AntCashManager
```

### URL Privacy Policy

Se disponibile online:
```
https://github.com/tuo-username/AntCashManager/wiki/privacy-policy.html
```

---

## Testing e Verifica

### Step 1: Test Pre-Pubblicazione

#### Test su Emulator

```bash
./gradlew :androidApp:installDebug --no-daemon
```

Verifica:
- ✓ Icona visibile nella home screen
- ✓ Icona nitida alle diverse risoluzioni
- ✓ App si avvia senza crash
- ✓ Dark mode funziona
- ✓ OCR funziona
- ✓ Backup/Restore funzionano

#### Test su Device Reale

1. Connetti device Android (API 24+)
2. Esegui: `./gradlew :androidApp:installRelease --no-daemon`
3. Verifica lo stesso di sopra

#### Test Crashlytics

1. Click su versione app in Settings → genera crash
2. Verifica crash arrivato su Firebase Console
3. Stack trace deobfuscato correttamente

### Step 2: Anteprima su Play Console

1. Vai a **Store Listing** → **Preview**
2. Seleziona device (Pixel 6a, Tablet, etc.)
3. Verifica:
   - ✓ Icona visibile e nitida
   - ✓ Screenshot ben allineati
   - ✓ Testo descrizioni leggibile
   - ✓ Feature graphic proporzionato
   - ✓ Nessun overflow di testo

### Step 3: Compliance Check

Nella console, vai a **Release** → **Internal Testing** e controlla:

- ✓ **Content rating**: Completa il questionario IARC
- ✓ **Privacy Policy**: Caricata e accessibile
- ✓ **Permissions**: Revisa i permessi richiesti
- ✓ **Sensitive ads**: Nessun contenuto sensibile negli ads

---

## Pubblicazione

### Step 1: Completare il Modulo di Conformità

1. Vai a **App content** → **Content rating**
2. Completa il questionario IARC
   - Seleziona categoria principale: **Finanza**
   - Rispondi alle domande di conformità
   - Submit

### Step 2: Preparare il Release

1. Vai a **Release** → **Production** (o **Staged rollout** per graduale)
2. Clicca **Create new release**
3. Seleziona il bundle di release: `app-release.aab`
4. Compila:
   - **Release name**: `v1.4.6`
   - **Release notes**: 
     ```
     Versione 1.4.6 - Maggio 2026
     
     Nuove Funzionalità:
     - Miglioramento OCR per scansione ricevute
     - Dark mode completo
     - Backup e restore dati
     
     Bugfix:
     - Correzione crash in navigazione
     - Miglioramento stabilità
     
     Miglioramenti:
     - Ottimizzazione performance
     - UI migliorata
     - Support per 5 lingue (IT, EN, DE, FR, ES)
     ```

### Step 3: Rivedi e Pubblica

1. Verifica tutte le informazioni:
   - ✓ Store Listing completata
   - ✓ Content Rating presente
   - ✓ Privacy Policy linkato
   - ✓ Release notes compilate
   - ✓ App signing OK
   - ✓ Nessun warning critico

2. Clicca **Review release**
3. Se tutto OK, clicca **Start rollout to Production**

### Step 4: Attendi Revisione Google

- Tempo medio: 2-4 ore
- In rari casi: fino a 24 ore
- Controlla email per aggiornamenti

### Step 5: Live!

Una volta approvato, l'app sarà disponibile su Google Play Store a:
```
https://play.google.com/store/apps/details?id=com.sformica.ant_cashmanager
```

---

## Manutenzione Post-Pubblicazione

### Monitorare l'App

1. **Analytics**: Vai a **Statsitcs** per visualizzare:
   - Download e uninstall
   - Rating e review
   - Crash e ANR (Application Not Responding)
   - Device breakdown

2. **Firebase Console**: 
   - Monitora Crashlytics
   - Performance monitoring
   - Analytics events

3. **Review**: Leggi regolarmente le review per feedback utente

### Aggiornamenti Futuri

Per aggiornare l'app a versione 1.5.0:

1. Aggiorna `versionCode` e `versionName` in `build.gradle.kts`
2. Compila nuovo bundle: `./gradlew :androidApp:bundleRelease`
3. Ripeti i step di testing
4. Crea nuovo release nella console
5. Compila release notes e pubblica

### Gestione degli Asset

Se necessario aggiornare icone/screenshot:

1. Modifica i file SVG in `extra/icons/`
2. Runna lo script di conversione: `bash convert_icons.sh`
3. Carica i nuovi PNG sulla console
4. Verifica preview prima di publish

---

## Checklist Pre-Pubblicazione

- [ ] Tutti i file SVG convertiti a PNG
- [ ] App Icon 512x512 px caricato
- [ ] Feature Graphic 1024x500 px caricato
- [ ] Almeno 2 screenshot caricati (max 8)
- [ ] Titolo app (max 50 caratteri)
- [ ] Descrizione breve (max 80 caratteri)
- [ ] Descrizione completa scritta e compilata
- [ ] Categoria selezionata (Finanza)
- [ ] Email contatti inserita
- [ ] Privacy Policy linkato
- [ ] Funzionalità OCR testata
- [ ] Backup/Restore testati
- [ ] Dark mode testato
- [ ] Crashlytics test crash generato e verificato
- [ ] Content rating IARC completo
- [ ] Release notes preparato
- [ ] Build release compilato e testato
- [ ] Nessun warning nella console
- [ ] Screenshot nitidi e leggibili
- [ ] Testo non contiene errori ortografici
- [ ] Licenza (LICENSE) inclusa nel repository
- [ ] README.md aggiornato

---

## Supporto e Riferimenti

- [Google Play Console Help](https://support.google.com/googleplay/android-developer)
- [App Design Guidelines](https://developer.android.com/design)
- [Publishing Overview](https://developer.android.com/studio/publish)
- [Asset Specifications](https://support.google.com/googleplay/android-developer/answer/1078870)

---

**Versione Documento**: 1.0  
**Data**: Maggio 2026  
**App**: AntCashManager v1.4.6  
**Pacchetto**: com.sformica.ant_cashmanager

