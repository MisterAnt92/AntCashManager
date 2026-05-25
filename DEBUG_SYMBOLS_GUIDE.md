# Debug Symbols Configuration Guide

**Data:** May 25, 2026  
**App:** AntCashManager  
**Native Code:** ML Kit Text Recognition v16.0.1  

## Problema

Google Play Console mostra l'avvertimento:
> "Questo App Bundle contiene codice nativo e non hai caricato simboli di debug. Ti consigliamo di caricare un file di simboli per poter eseguire più facilmente l'analisi e il debug degli arresti anomali e degli errori ANR."

## Causa

L'app utilizza **ML Kit Text Recognition** che include librerie native (`.so` files). Play Console richiede i debug symbols per:
- Decodificare stack trace di crash dalle app in production
- Analizzare ANR (Application Not Responding)
- Correlate automaticamente errori simili

## Soluzione

I debug symbols per ML Kit sono distribuiti automaticamente via Maven e impacchettati nel bundle Android App (.aab). Quando carichi il bundle su Google Play Console, estrae e gestisce automaticamente i debug symbols senza richiedere azioni manuali.

La configurazione Firebase Crashlytics nel build.gradle.kts è già corretta:
```kotlin
firebaseCrashlytics {
    // Upload automatico del mapping file per deobfuscation stacktrace release.
    mappingFileUploadEnabled = true
}
```

Questo consente:
- Upload automatico di ProGuard mapping file (per Java/Kotlin code)
- Decodifica automatica di native crash stack trace da Play Console

## Come caricare i Debug Symbols

### Step 1: Build the Release Bundle

```bash
cd /opt/src/GIT/app/AntCashManager
./gradlew bundleRelease
```

Output: `androidApp/build/outputs/bundle/release/app-release.aab`

### Step 2: Carica su Google Play Console

1. Accedi a [Google Play Console](https://play.google.com/console)
2. Seleziona **AntCashManager**
3. Vai a **Release** → **Create New Release** (oppure modifica una release esistente)
4. Upload `app-release.aab`
5. **IMPORTANTE**: La sezione "Debug Symbols and Mapping" dovrebbe mostrarsi automaticamente
   - Se non vedi l'opzione, significa che il sistema ha già rilevato i simboli automaticamente
   - Google Play Console estrae i simboli dal `.aab` in background

### Step 3 (Manuale - se necessario): Upload esplicito di Debug Symbols

Se Play Console non carica i simboli automaticamente:

1. Estrai i simboli localmente:
   ```bash
   # Localizza il file unzip del bundle
   cd androidApp/build/outputs/bundle/release
   unzip app-release.aab
   
   # I simboli sono in: base/lib/{arch}/
   # Es: base/lib/arm64-v8a/libtflite_jni.so
   ```

2. Prepara un ZIP contenente la struttura corretta:
   ```
   symbols.zip/
   ├── lib/
   │   ├── arm64-v8a/
   │   │   ├── libtflite_jni.so
   │   │   ├── libflatbuffers_jni.so
   │   │   └── ...
   │   ├── armeabi-v7a/
   │   └── x86_64/
   └── mapping.txt (ProGuard mapping)
   ```

3. In Play Console → **Release** → **Debug Symbols**:
   - Upload il file `symbols.zip`
   - Seleziona App Bundle corrispondente
   - Submit

## Verifica

### Come verificare che i simboli siano caricati

1. In **Play Console** → **Quality** → **Crashes and ANRs**
2. Seleziona un crash correlato a ML Kit
3. **Stack trace dovrebbe contenere simboli decodificati**, non indirizzi raw (`0x12ab34cd`)

Esempio di stack trace corretto (con simboli):
```
java.lang.ExceptionInInitializerError
    at com.google.mlkit.vision.text.TextRecognition.getClient(...)
    at com.antcashmanager.android.data.receipt.MlKitReceiptOcrService.recognizeText(...)
```

Esempio di stack trace NON decodificato (senza simboli):
```
at 0x12ab34cd
at 0x45ef67ab
```

### Verificare il bundle localmente

```bash
# Usa bundletool (scaricato dal Play Console o da Maven)
bundletool inspect-bundle \
  --bundle=androidApp/build/outputs/bundle/release/app-release.aab \
  --mode=summary

# Controlla la presente di native libraries nella sezione "Native Libraries"
```

## Dipendenze Native Correnti

| Libreria | Versione | Origine | Note |
|---|---|---|---|
| `com.google.mlkit:text-recognition` | 16.0.1 | Maven | Contiene `libtflite_jni.so` e altri `.so` per OCR |

## Automazione (Build.gradle.kts)

La configurazione necessaria nel `androidApp/build.gradle.kts` è già presente:

```kotlin
firebaseCrashlytics {
    // Upload automatico del mapping file per deobfuscation stacktrace release.
    mappingFileUploadEnabled = true
}
```

Questo assicura che:
- I debug symbols Java/Kotlin siano caricati automaticamente tramite Firebase Crashlytics
- I debug symbols nativi (`.so`) siano inclusi nel bundle ed estratti da Play Console

## Troubleshooting

| Problema | Soluzione |
|---|---|
| "Debug symbols not found" in Play Console | Verifica che l'AAB sia stato uploadato correttamente, attendi 5-10 minuti per l'elaborazione |
| Stack trace ancora con indirizzi raw | Ripeti l'upload dei simboli, verifica il mapping.txt sia presente |
| Simboli non appaiono per una release antica | Google Play Console conserva simboli solo per ultimi 90 giorni |

## Riferimenti

- [Google Play Console - Debug Symbols Setup](https://developer.android.com/topic/performance/app-optimization/enable-app-optimization#native-crash-support)
- [ML Kit Documentation](https://developers.google.com/ml-kit)
- [Android NDK Debug Symbols](https://developer.android.com/ndk/guides/debugging)

---

**Prossimi step:**
- Build e upload `bundleRelease` a Play Console (internal testing track)
- Verifica che l'avvertimento scompaia
- Monitora crash su Play Console → **Quality** → **Crashes and ANRs** per confermare decodifica corretta

