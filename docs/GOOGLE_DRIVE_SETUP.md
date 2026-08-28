# Google Drive Integration Setup Guide

## 🎯 Obiettivo
Questa guida ti aiuta a configurare l'autenticazione OAuth 2.0 per integrazione Google Drive nel backup automatico di AntCashManager.

**Tempo stimato**: 15-20 minuti

---

## 📋 Prerequisiti

- ✅ Account Google (gmail.com o Google Workspace)
- ✅ Accesso a Google Cloud Console (console.cloud.google.com)
- ✅ AntCashManager source code (per generare SHA-1 fingerprints)
- ✅ Ambiente locale: Android SDK + Gradle configurato

---

## 🔑 STEP 1: Generare SHA-1 Fingerprints

### Cos'è?
SHA-1 fingerprint è un'impronta digitale unica del tuo keystore di firma Android. Google utilizza questo per validare che l'app che effettua il login è veramente AntCashManager.

### Come generare SHA-1

#### **Opzione A: Via Gradle (Consigliato)**

```bash
# Da dentro la directory del progetto AntCashManager
cd /opt/src/GIT/app/AntCashManager

# Genera fingerprint per il debug keystore
./gradlew signingReport | grep -A 5 "SHA-1"
```

**Output atteso**:
```
Variant: debug
Config: debug
Store: /Users/username/.android/debug.keystore
Alias: androiddebugkey
MD5: AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90
SHA1: AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12
SHA-256: ...
```

**👉 Copia il valore SHA1 (la stringa lunga con i due punti)**

#### **Opzione B: Via keytool (Manuale)**

```bash
# Debug keystore (di default)
keytool -list -v -keystore ~/.android/debug.keystore \
  -alias androiddebugkey \
  -storepass android \
  -keypass android | grep "SHA1"
```
keytool -keystore /home/simone/Documents/Lavoro/AntCashManager/keystore_antcashmanager.jks -list -v



**Output**:
```
SHA1: AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12
```

### Per il Release Keystore

Se hai un keystore di firma per le release (solitamente in `/path/to/release.keystore`):

```bash
keytool -list -v -keystore /path/to/release.keystore \
  -alias release \
  -storepass <your-password> \
  -keypass <your-password> | grep "SHA1"
```

### 📌 Salva questi valori
```
DEBUG SHA-1:   AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12
RELEASE SHA-1: [se applicabile]
```

---

## ☁️ STEP 2: Creare Progetto in Google Cloud Console

### 2.1: Accedere a Google Cloud Console

1. Visita: **https://console.cloud.google.com**
2. Accedi con il tuo account Google
3. Se è la prima volta, accetta i termini di servizio

### 2.2: Creare un Nuovo Progetto

1. In alto a sinistra, vedi un dropdown con il progetto corrente (o "Select a Project")
2. Clicca su **"Select a Project"**
3. Clicca su **"NEW PROJECT"**
4. Compila il modulo:
   - **Project name**: `AntCashManager` (o `AntCashManager - Development`)
   - **Organization**: (opzionale, lascia vuoto se non applicabile)
   - **Billing account**: Seleziona l'account billing
5. Clicca **"CREATE"**

**Attesa**: Ci vorranno 10-30 secondi per creare il progetto. Il dashboard si aggiornerà automaticamente.

### 2.3: Verificare il Progetto

Una volta creato, dovresti vedere:
- Dashboard con il nome del progetto in alto a sinistra
- Un ID progetto unico (es: `antcashmanager-2024-1234567890`)

---

## 🔌 STEP 3: Abilitare Google APIs Necessarie

### 3.1: Accedere all'API Library

1. Nel menu a sinistra (tre linee ☰), clicca **"APIs & Services"**
2. Clicca **"Library"**

### 3.2: Abilitare Google Drive API

**Search**: Digita `Google Drive API` nella casella di ricerca

**Risultato**: Seleziona "Google Drive API" (con il logo Drive blu)

**Clicca**: **"ENABLE"**

**Attesa**: Ci vorranno 1-2 minuti. Una volta abilitata, vedrai un pulsante "DISABLE" e dettagli API.

### 3.3: Abilitare Google+ API (per Sign-In)

**Search**: Digita `Google+ API` nella casella di ricerca

**Risultato**: Seleziona "Google+ API" (con il logo G+ colorato)

**Clicca**: **"ENABLE"**

### ✅ Verificare

Nella dashboard, sotto "APIs & Services", dovresti vedere:
- ✅ Google Drive API — Enabled
- ✅ Google+ API — Enabled

---

## 🔐 STEP 4: Creare OAuth 2.0 Credentials

### 4.1: Accedere a Credentials

1. Nel menu "APIs & Services" (sinistra), clicca **"Credentials"**
2. Oppure vai direttamente a: **https://console.cloud.google.com/apis/credentials**

### 4.2: Creare una Nuova Credential

1. Clicca **"+ CREATE CREDENTIALS"** (in alto)
2. Seleziona **"OAuth client ID"**

**Nota**: Se Google ti chiede di prima creare una schermata di consenso, segui Step 4.3 prima.

### 4.3: Configurare OAuth Consent Screen (se necessario)

Se vedi il messaggio "To create an OAuth client ID, you must first configure the OAuth consent screen":

1. Clicca **"CONFIGURE CONSENT SCREEN"**
2. Seleziona **"External"** (User type)
3. Clicca **"CREATE"**
4. Compila il form:
   - **App name**: `AntCashManager`
   - **User support email**: Tua email (es: tuo-email@gmail.com)
   - **Developer contact information**: Tua email
5. Clicca **"SAVE AND CONTINUE"**
6. Scorri fino alla fine e clicca **"SAVE AND CONTINUE"** (per gli Scopes — non aggiungere scopi custom)
7. Di nuovo **"SAVE AND CONTINUE"** (test users)
8. Clicca **"BACK TO DASHBOARD"**

### 4.4: Creare OAuth Client ID (Riprendere da 4.2)

1. Clicca **"+ CREATE CREDENTIALS"** (in alto)
2. Seleziona **"OAuth client ID"**
3. Nel dropdown **"Application type"**, seleziona **"Android"**
4. Compila il form:

   | Campo | Valore |
   |-------|--------|
   | Package name | `com.sformica.ant_cashmanager` |
   | SHA-1 certificate fingerprint | [Il SHA-1 copiato da Step 1 — formato: `AB:CD:EF:...`] |

5. Se vuoi supportare sia debug che release:
   - Aggiungi un'altra riga con **"ADD FINGERPRINT"**
   - Incolla il RELEASE SHA-1

6. Clicca **"CREATE"**

### ✅ Risultato

Google genererà automaticamente un **Client ID** (non lo userai direttamente nel codice — la libreria Android lo ha già embedded).

Vedrai un popup con:
- **Client ID**: `123456789-abcdefg.apps.googleusercontent.com`
- **Client Secret**: (se applicabile)

**👉 Puoi chiudere questo popup** — il Client ID è memorizzato nel tuo progetto Google Cloud.

---

## 🔍 STEP 5: Verificare la Configurazione

### 5.1: Controllare le credenziali create

1. Vai a **APIs & Services → Credentials** (Google Cloud Console)
2. Nella sezione **"OAuth 2.0 Client IDs"**, dovresti vedere:
   - **Name**: `AntCashManager` (o simile)
   - **Type**: Android
   - **Package**: `com.sformica.ant_cashmanager`
   - **Fingerprints**: I SHA-1 che hai aggiunto

### 5.2: Verificare le API abilitate

1. Vai a **APIs & Services → Enabled APIs & services**
2. Dovresti vedere:
   - ✅ Google Drive API
   - ✅ Google+ API

### 5.3: Controllare Quota e Usage (Opzionale)

1. Vai a **APIs & Services → Quotas**
2. Cerca "Google Drive API"
3. Verifica che non ci siano limiti di quota configurati (di solito non ce ne sono per i nuovi progetti)

---

## 💻 STEP 6: Configurare l'App Android

### 6.1: La libreria Android ha il Client ID embedded

**Buone notizie**: Google Play Services per il Sign-In ha il Client ID di debug già embedded nella libreria stessa. 

Quando aggiungi la dipendenza:
```kotlin
implementation("com.google.android.gms:play-services-auth:21.1.1")
```

La libreria sa automaticamente come connettersi a Google usando i tuoi SHA-1 fingerprint registrati.

### 6.2: Non occorre configurazione aggiuntiva in AndroidManifest.xml

A differenza di Firebase, Google Sign-In non richiede configurazione nel manifest per il Client ID — tutto è gestito via SHA-1 matching.

### 6.3: Configurazione Gradle (già completa)

Il file `build.gradle.kts` ha già le dipendenze:
```kotlin
implementation(libs.google.play.services.auth)
implementation(libs.google.drive.api)
implementation(libs.google.http.client)
implementation(libs.google.oauth.client)
```

✅ **Niente altro da fare qui** — andrà tutto al momento dell'implementazione del codice.

---

## 🧪 STEP 7: Testare le Credenziali (Opzionale, consigliato)

### Opzione 1: Via Google Developer Console

1. Vai a **APIs & Services → OAuth 2.0 Client IDs**
2. Clicca sul tuo **Android client ID**
3. Scorri fino a **"Authorized JavaScript origins"** (anche se è per Android, puoi testare il flusso)
4. Non è necessaria configurazione aggiuntiva per testare

### Opzione 2: Test durante l'implementazione

Quando implementerai `GoogleSignInManager.kt`, potrai testare il flusso OAuth direttamente:
1. Compila l'app
2. Attiva il toggle "Google Drive" in Settings
3. Clicca "Sign In"
4. Autorizza l'app ad accedere a Google Drive
5. Verifica che il sign-in riesca

---

## ⚠️ Troubleshooting

### Errore: "Invalid Client ID"

**Possibili cause**:
1. ❌ SHA-1 fingerprint non registrato correttamente
2. ❌ Package name non corrisponde (`com.sformica.ant_cashmanager`)
3. ❌ SHA-1 copiato con spazi extra

**Soluzione**:
```bash
# Regenera il SHA-1 e verifica il formato
./gradlew signingReport | grep "SHA1" | tr -d ' '
```

Copia **esattamente** quello che vedi (incluso i due punti).

### Errore: "10: DEVELOPER_ERROR"

**Causa**: Credenziali Android non configurate correttamente in Google Cloud Console.

**Soluzione**:
1. Torna a **Credentials → OAuth 2.0 Client IDs**
2. Clicca sul tuo client Android
3. Verifica che il **Package name** sia esatto: `com.sformica.ant_cashmanager`
4. Verifica che gli **SHA-1 fingerprints** siano corretti (formato: `XX:XX:XX:...`)

### Errore: "NETWORK_ERROR"

**Causa**: Timeout di rete, Google Drive API non abilitata, o nessun accesso a Internet.

**Soluzione**:
1. Verifica che Google Drive API sia **Enabled** in Google Cloud Console
2. Verifica la connessione Internet del device/emulatore
3. Attendi 1-2 minuti dopo aver abilitato l'API (ci vuole tempo per propagarsi)

### Errore: "USER_CANCELLED"

**Causa**: L'utente ha cancellato il flusso di login.

**Azione**: Niente — è il comportamento atteso. L'utente dovrà cliccare "Sign In" di nuovo.

---

## 📚 Prossimi Step

Una volta completato il setup di Google Cloud Console:

1. ✅ Notifica al team che il setup è completato
2. ✅ Condividi il **Client ID** e **Project ID** (se altri devono lavorare sul progetto)
3. ✅ Procedi con l'implementazione del codice (Step 3 onwards):
   - Creare `GoogleSignInManager.kt`
   - Creare `DriveUploadManager.kt`
   - Modificare `AutoBackupWorker.kt`
   - Aggiornare UI

---

## 🔒 Sicurezza & Best Practices

### ✅ Do's

- ✅ **Tieni i SHA-1 fingerprint privati** — sono collegati al tuo keystore
- ✅ **Usa scopes minimi** — `drive.file` (accesso solo ai file creati da AntCashManager)
- ✅ **Refresh token periodicamente** — la libreria lo fa automaticamente
- ✅ **Gestisci revoche** — se utente revoca, ritorna a Local backup

### ❌ Don'ts

- ❌ Non esporre il Client ID in versione control (ma è OK — è pubblico)
- ❌ Non usare scopes completi di Drive (`drive`) — è una security risk
- ❌ Non salvare token in plain text (il codice li cripta con AndroidKeyStore)

---

## 📞 Supporto

Se incontri problemi:

1. **Verificare il progetto Google Cloud**:
   - Vai a https://console.cloud.google.com
   - Seleziona il progetto "AntCashManager"
   - Controlla che le API siano abilitate

2. **Controllare i logs**:
   ```bash
   ./gradlew testDebugUnitTest --info 2>&1 | grep -i "oauth\|drive\|auth"
   ```

3. **Documentazione ufficiale**:
   - [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android)
   - [Google Drive API](https://developers.google.com/drive/api)

---

## ✨ Prossima fase

Una volta che questo setup è completo, procederemo con:

**Step 3+: Implementazione del Codice**
- Creare `GoogleSignInManager.kt` (OAuth orchestration)
- Creare `DriveUploadManager.kt` (Google Drive API calls)
- Modificare `AutoBackupWorker.kt` (dual destination support)
- Aggiornare UI per il toggle Google Drive

**Tempo stimato**: 14 giorni di sviluppo (seguendo il piano definito)

---

**Data creazione**: 2026-08-21  
**Versione**: 1.0  
**Ultimo update**: 2026-08-21
