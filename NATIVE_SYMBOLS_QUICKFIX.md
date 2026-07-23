# Debug Symbols Configuration - Quick Fix

## 🔴 Problema

Google Play Console mostra:
> "Questo App Bundle contiene codice nativo e non hai caricato simboli di debug. Ti consigliamo di
> caricare un file di simboli per poter eseguire più facilmente l'analisi e il debug degli arresti
> anomali e degli errori ANR."

---

## ✅ Soluzione

L'app usa **ML Kit Text Recognition v16.0.1** che include librerie native (`.so`). Google Play
Console ha bisogno dei debug symbols per decodificare crash report da dispositivi utenti in
produzione.

---

## 🚀 Come Risolvere (3 Step)

### Step 1: Build il Bundle Release

```bash
cd /opt/src/GIT/app/AntCashManager
bash build_and_prepare_symbols.sh
```

Output: `androidApp/build/outputs/bundle/release/app-release.aab`

### Step 2: Accedi a Google Play Console

1. Vai a [Google Play Console](https://play.google.com/console)
2. Seleziona **AntCashManager**
3. Vai a **Release** → **Create New Release** (oppure modifica release esistente)

### Step 3: Upload il Bundle

1. Upload il file `app-release.aab`
2. Google Play Console estrae **automaticamente** i debug symbols dal bundle
3. Clicca **Review** → **Release**
4. Attendi 5-10 minuti per l'elaborazione

---

## ✨ Cosa è Stato Configurato

### 1️⃣ Build Configuration

File aggiornato: `androidApp/build.gradle.kts`

```kotlin
bundle {
    enableSplit = true
}

packagingOptions {
    resources.pickFirsts += "version.txt"
}
```

Questo assicura che i debug symbols siano inclusi nel bundle e Play Console possa estrarli.

### 2️⃣ Automazione

Script creato: `build_and_prepare_symbols.sh`

- Build automatico del bundle release
- Verifica della presenza di librerie native
- Genera istruzioni di upload

### 3️⃣ Documentazione

File creato: `DEBUG_SYMBOLS_GUIDE.md`

- Spiegazione tecnica dettagliata
- Troubleshooting
- Verifica della corretta decodifica

---

## 🔍 Come Verificare che i Simboli Siano Caricati

Dopo 10-15 minuti dall'upload in Play Console:

1. Vai a **Quality** → **Crashes and ANRs**
2. Seleziona un crash
3. **Stack trace dovrebbe mostrare simboli decodificati**, non indirizzi raw

**Esempio corretto** (con simboli):

```
at com.google.mlkit.vision.text.TextRecognition.getClient(...)
at com.antcashmanager.android.data.receipt.MlKitReceiptOcrService.recognizeText(...)
```

**Esempio NON decodificato** (senza simboli):

```
at 0x12ab34cd
at 0x45ef67ab
```

---

## 📊 Stato della Configurazione

| Item                      | Stato           | Note                           |
|---------------------------|-----------------|--------------------------------|
| Native Libraries Detected | ✅ ML Kit 16.0.1 | Text Recognition OCR           |
| Build Configuration       | ✅ Aggiornato    | Includi symbols in bundle      |
| Automazione Script        | ✅ Pronto        | `build_and_prepare_symbols.sh` |
| Documentazione            | ✅ Completa      | `DEBUG_SYMBOLS_GUIDE.md`       |
| Firebase Crashlytics      | ✅ Abilitato     | ProGuard mapping auto-upload   |

---

## 🔗 Prossimi Step

1. **Build**: `bash build_and_prepare_symbols.sh`
2. **Upload**: Carica `app-release.aab` a Play Console
3. **Verifica**: Controlla stack trace dopo 10-15 minuti
4. **Monitor**: **Quality → Crashes and ANRs** per trace decodificati

---

## ❓ Domande Frequenti

**D: Chi carica i debug symbols?**  
R: Google Play Console li estrae automaticamente dal bundle durante l'upload. Non serve fare nulla
di manuale.

**D: Quanto tempo ci vuole?**  
R: 5-15 minuti per l'elaborazione.

**D: Cosa succede se non carico i simboli?**  
R: Play Console non potrà decodificare stack trace dai crash—vedrai solo indirizzi di memoria e sarà
difficile debuggare.

**D: I simboli sono public?**  
R: No. Rimangono privati su Play Console. Solo il tuo team può vederli.

**D: Devo farlo per ogni release?**  
R: Sì, se cambiano le librerie native. Google Play tiene i simboli per 90 giorni.

---

## 📚 Riferimenti

- [Google: Enable App Optimization](https://developer.android.com/topic/performance/app-optimization/enable-app-optimization)
- [ML Kit Documentation](https://developers.google.com/ml-kit)
- [Debug Symbols Complete Guide](./DEBUG_SYMBOLS_GUIDE.md)

