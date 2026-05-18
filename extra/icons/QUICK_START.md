# Quick Start - Icon Resources

Guida rapida per generare e usare le risorse grafiche di AntCashManager.

## In 3 Step

### 1️⃣ Genera i PNG dai SVG

```bash
cd extra/icons/
bash convert_icons.sh
```

Questo genererà automaticamente:
- `app-icons/png/app-icon-512.png` (per Google Play Store)
- `app-icons/png/app-icon-192.png` (per Android)
- `store-assets/feature-graphics/feature-graphic-1024x500.png` (Feature graphic)
- `store-assets/promo-graphics/promo-180x120.png` (Promo graphic)
- Versioni JPEG compresse

### 2️⃣ Installa negli Asset Android (Opzionale)

Se vuoi usare le icone nel tuo build Android:

```bash
bash install_android_icons.sh
```

Questo copia automaticamente le icone nelle cartelle `mipmap-*` di Android.

### 3️⃣ Pubblica su Google Play Store

Leggi: [GOOGLE_PLAY_PUBLICATION_GUIDE.md](GOOGLE_PLAY_PUBLICATION_GUIDE.md)

Riassunto:
1. Accedi a [Google Play Console](https://play.google.com/console)
2. Vai a **Store Listing** → **Graphic Assets**
3. Carica:
   - **App Icon**: `app-icons/png/app-icon-512.png`
   - **Feature Graphic**: `store-assets/feature-graphics/feature-graphic-1024x500.png`
   - **Screenshots**: I tuoi screenshot (min 2, max 8)

---

## Struttura Cartelle

```
extra/icons/
├── README.md                          ← Questa guida
├── GOOGLE_PLAY_PUBLICATION_GUIDE.md   ← Guida completa pubblicazione
├── convert_icons.sh                   ← Script conversione SVG→PNG
├── install_android_icons.sh           ← Script installazione Android
│
├── app-icons/
│   ├── svg/                          (Modifica questi!)
│   │   ├── app-icon-192.svg
│   │   └── app-icon-512.svg
│   └── png/                          (Generati da script)
│       ├── app-icon-192.png
│       ├── app-icon-512.png
│       └── android-res/ (versioni per mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
│
├── store-assets/
│   ├── feature-graphics/
│   │   ├── feature-graphic.svg       (Modifica)
│   │   ├── feature-graphic-1024x500.png
│   │   └── jpeg/
│   ├── promo-graphics/
│   │   ├── promo-180x120.svg         (Modifica)
│   │   ├── promo-180x120.png
│   │   └── jpeg/
│   └── screenshots/                  (Aggiungi i tuoi screenshot)
│
└── social-assets/
    ├── twitter-banner/
    │   └── twitter-banner-1500x500.svg
    ├── facebook-cover/
    └── instagram-story/
```

---

## Cosa Modificare

### Se vuoi cambiare il design dell'icona:

1. Apri con Inkscape (gratuito):
   ```bash
   sudo apt-get install inkscape
   inkscape extra/icons/app-icons/svg/app-icon-512.svg
   ```

2. Modifica il design (colori, forme, ecc.)

3. Salva il file SVG

4. Genera i PNG:
   ```bash
   bash extra/icons/convert_icons.sh
   ```

### Se vuoi aggiornare il feature graphic:

1. Apri: `extra/icons/store-assets/feature-graphics/feature-graphic.svg`
2. Modifica in Inkscape
3. Salva
4. Runna lo script di conversione

---

## File Importanti per la Pubblicazione

| File | Uso | Dimensioni | Formato |
|------|-----|-----------|---------|
| `app-icons/png/app-icon-512.png` | Google Play Store - App Icon | 512x512 px | PNG |
| `store-assets/feature-graphics/feature-graphic-1024x500.png` | Google Play Store - Feature Graphic | 1024x500 px | PNG |
| `store-assets/promo-graphics/promo-180x120.png` | Google Play Store - Promo (opzionale) | 180x120 px | PNG |
| `social-assets/twitter-banner-1500x500.svg` | Twitter Banner | 1500x500 px | SVG/PNG |

---

## Troubleshooting

### "Errore: ImageMagick not found"

Installa:
```bash
sudo apt-get install imagemagick
```

### "Errore: convert command not working"

Aggiorna ImageMagick:
```bash
sudo apt-get update
sudo apt-get upgrade imagemagick
```

### "PNG generation failed"

Prova manualmente:
```bash
convert -density 150 app-icons/svg/app-icon-512.svg -resize 512x512 app-icons/png/app-icon-512.png
```

### I PNG non hanno background trasparente

Modifica il comando `convert` nel script per aggiungere `-background none`.

---

## Colori Brand

- **Rosa primario**: #FFB3BA
- **Rosa scuro**: #FF8A95
- **Magenta accent**: #E91E63
- **Oro (coin)**: #FFD54F
- **Background light**: #FFE0E6

---

## File da Non Toccare

- `androidApp/google-services.json` - Generato da Firebase, non modificare
- `androidApp/src/main/res/values/strings.xml` - Localizzazioni

---

## Prossimi Step

1. ✅ Genera PNG: `bash convert_icons.sh`
2. ✅ Verifica i file generati
3. ⏭️ Leggi: [GOOGLE_PLAY_PUBLICATION_GUIDE.md](GOOGLE_PLAY_PUBLICATION_GUIDE.md)
4. ⏭️ Carica gli asset su Google Play Console
5. ⏭️ Pubblica la tua app!

---

**Domande?** Consulta il README principale della cartella `icons/`.

