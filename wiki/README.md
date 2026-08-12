# Wiki - AntCashManager

Documentazione tecnica e operativa del progetto AntCashManager.

> 👋 **[→ GUIDA LETTURA - Nuovo qui? Inizia da qui](./GUIDA_LETTURA.md)**  
> ⚡ **[→ QUICK START - 5 minuti per iniziare](./QUICK_START.md)**  
> 📖 **[→ INDICE CENTRALE - Clicca qui per navigare tutta la documentazione](./INDEX.md)**  
> 🗺️ **[→ MAPPA DI NAVIGAZIONE - Preferisci una guida visuale per ruolo?](./NAVIGATION.md)**

---

## 🎯 Accesso Rapido

Scegli il tuo profilo per accedere subito alla documentazione rilevante:

## Accesso rapido come risorse esterne

La wiki è pubblicata anche come insieme di risorse statiche tramite GitHub Pages, così i file possono essere aperti direttamente da browser senza passare dalla vista repository.

### Indice esterno
- `docs/wiki/index.html`
- URL pubblico atteso: `https://misterant92.github.io/AntCashManager/wiki/`

### Privacy Policy pubbliche
- Inglese: `https://misterant92.github.io/AntCashManager/wiki/privacy-policy.html`
- Tedesco: `https://misterant92.github.io/AntCashManager/wiki/privacy-policy-de.html`
- Francese: `https://misterant92.github.io/AntCashManager/wiki/privacy-policy-fr.html`
- Spagnolo: `https://misterant92.github.io/AntCashManager/wiki/privacy-policy-es.html`

> Nota: per rendere i file effettivamente raggiungibili dall'esterno, GitHub Pages deve essere abilitato sul branch `develop` con cartella `/docs`.

## Informazioni Progetto
| Profilo | Task | Documento |
|---------|------|-----------|
| 👨‍💻 **Sviluppatore** | Nuova feature / Modifiche architettura | [ARCHITECTURE_GUIDELINES.md](./ARCHITECTURE_GUIDELINES.md) |
| 🎨 **UI/UX Developer** | Modifiche transazioni / Form | [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) |
| 🗄️ **Data Engineer** | Conversione dati PiggyBank (uso) | [CONVERSION_GUIDE.md](./CONVERSION_GUIDE.md) |
| 🔧 **Script Maintainer** | Manutenzione/estensione script | [SCRIPT_CONVERSION_README.md](./SCRIPT_CONVERSION_README.md) |
| ⚖️ **Legal** | Privacy policy e compliance | [privacy-policy.html](./privacy-policy.html) |

---

## 📊 Informazioni Progetto

| Campo | Valore |
|---|---|
| **App name** | `AntCashManager` |
| **Versione corrente** | `1.7.0` |
| **Package name** (`applicationId`) | `com.sformica.ant_cashmanager` |
| **Namespace Android** | `com.antcashmanager.android` |
| **Moduli principali** | `androidApp`, `shared` (Kotlin Multiplatform) |
| **Tech Stack** | Jetpack Compose, Kotlin, Room, Koin |

---

## 📚 Documenti Disponibili

### 🏗️ Architettura & Sviluppo
**[ARCHITECTURE_GUIDELINES.md](./ARCHITECTURE_GUIDELINES.md)**
- ✅ Definisce Clean Architecture a 3 layer (Presentation/Domain/Data)
- ✅ Pattern UseCase/ViewModel/State/Screen con esempi completi
- ✅ Rules per testing, dispatcher injection, Result pattern
- ✅ Anti-pattern da evitare e Pre-Commit Checklist
- ✅ **Fonte ufficiale** per decisioni architetturali

**Quando usarlo**: Sempre prima di scrivere codice, per verifiche strutturali, PR review.

### 📝 Guide Implementative
**[IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md)**
- ✅ Milestone: skeleton loading + form transazioni esteso
- ✅ Dettagli implementazione UI (note, payee, location, tags, ricorrenza)
- ✅ Flusso dati e pattern layout
- ✅ Test checklist e design decisions

**Quando usarlo**: Quando modifichi transazioni, form o skeleton loading.

### 🔄 Conversione Dati
**[CONVERSION_GUIDE.md](./CONVERSION_GUIDE.md)** *(Guida esecutiva)*
- ✅ Flusso rapido conversione PiggyBank Pro → AntCashManager
- ✅ Mapping campi e prerequisiti
- ✅ Uso script e parametri default
- ✅ Per **chi usa lo script**

**[SCRIPT_CONVERSION_README.md](./SCRIPT_CONVERSION_README.md)** *(Dettagli tecnici)*
- ✅ Schema input/output JSON
- ✅ Validazioni e regole di conversione
- ✅ Codici errore e esempi log
- ✅ Per **chi mantiene il script Python**

**Quando usarlo**: Conversione dati esportati da PiggyBank Pro.

### 🔐 Privacy & Policy
**[privacy-policy.html](./privacy-policy.html)** - Inglese  
**[privacy-policy-de.html](./privacy-policy-de.html)** - Tedesco  
**[privacy-policy-es.html](./privacy-policy-es.html)** - Spagnolo  
**[privacy-policy-fr.html](./privacy-policy-fr.html)** - Francese

- ✅ Policy privacy ufficiale
- ✅ Focus analytics usage-only (no dati personali)
- ✅ Eventi consentiti e proibiti

**Quando usarlo**: Riferimento legale, app store, documentazione esterna.

---

## ⚡ Flusso Consigliato

```
1. NON SEI ANCORA QUI? → Leggi INDEX.md per capire struttura
   ↓
2. NUOVA FEATURE? → ARCHITECTURE_GUIDELINES.md
   ↓
3. MODIFICHE TRANSAZIONI? → IMPLEMENTATION_GUIDE.md
   ↓
4. CONVERSIONE DATI? → CONVERSION_GUIDE.md (uso) o SCRIPT_CONVERSION_README.md (mantenimet)
   ↓
5. PRIMA DI COMMIT → Pre-Commit Checklist (in ARCHITECTURE_GUIDELINES.md)
```

---

## 🛠️ Note di Manutenzione Wiki

**Allineamento con il codice:**
- Mantieni versione e package allineati con `androidApp/build.gradle.kts`
- Aggiorna `ARCHITECTURE_GUIDELINES.md` se cambi i pattern architetturali
- Se modifichi il script di conversione, aggiorna sia `CONVERSION_GUIDE.md` sia `SCRIPT_CONVERSION_README.md`
- Se aggiungi una feature storica, documenta in `IMPLEMENTATION_GUIDE.md`

**Localizzazioni:**
- Se aggiungi stringhe privacy in altre lingue, crea `privacy-policy-XX.html` corrispondente
- Mantieni le 5 lingue standard: EN, IT, FR, DE, ES

**Versioning:**
- Wiki version segue versione app (es. v1.4.6 app → wiki v1.4.6)

---

## 🔐 Privacy & Analytics - Usage-Only Policy

La policy di analytics segue il principio **usage-only**: tracciamo SOLO l'utilizzo dell'app, NON i dati personali dell'utente.

### ✅ Eventi Consentiti

**Utilizzo funzionalità:**
- `transactions_filter_applied` - filtro applicato
- `transactions_filter_cleared` - filtro rimosso
- `transaction_add_opened` - apertura form aggiunta
- `receipt_scan_opened` - apertura scanner ricevute
- `transaction_form_opened` - apertura form transazione
- `transaction_form_cancelled` - cancellazione form
- `transaction_submit_success` - invio transazione riuscito
- `backup_create_requested` - richiesta backup
- `backup_file_saved` - backup salvato
- `backup_file_save_error` - errore salvataggio backup
- `restore_open_requested` - richiesta restore
- `restore_file_selected` - file restore selezionato
- `delete_all_data_confirmed` - cancellazione dati confermata
- `reset_preferences_confirmed` - reset preferenze confermato

**Firebase Standard (consentito):**
- `screen_view` - visualizzazione screen

### ❌ Dati NON Consentiti

**Mai inviare in analytics:**
- ❌ Testo libero utente (query, note, titoli, descrizioni)
- ❌ Dati transazione dettagliati (importo descrittivo, payee, location, tags, categoria specifica)
- ❌ Email, telefono o identificatori personali
- ❌ Messaggi errore raw o stacktrace
- ❌ Coordinate geografiche o dati sensibili

**Linea guida:**  
Se contiene info che identificherebbe o riguarderebbe direttamente un utente → **VIETATO**.

---

## 📞 Supporto & Contatti

Per domande sulla documentazione:
- 📧 Wiki Maintainer: Team AntCashManager
- 🐛 Issue/Bug: Vedi repository GitHub
- 📋 Aggiornamenti: Verifica "Note di Manutenzione Wiki" sopra

---

**Ultima Modifica**: Agosto 2026  
**Versione Wiki**: 1.7.0  
**Status**: ✅ Documentazione Completa
