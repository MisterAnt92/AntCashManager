# 🗺️ Mappa di Navigazione - Wiki AntCashManager

Una guida visuale per navigare velocemente tra i documenti della wiki.

---

## 🎯 Dove Iniziare?

```
┌─────────────────────────────────────────┐
│   🏠 PRIMO ACCESSO ALLA WIKI?            │
│   Leggi: INDEX.md o README.md            │
└────────────┬────────────────────────────┘
             │
    ┌────────┴────────┐
    │                 │
    ▼                 ▼
┌─────────┐      ┌──────────┐
│ SVILUP- │      │   DATA   │
│ PATORE? │      │ ENGINEER │
└────┬────┘      └────┬─────┘
     │                │
     ▼                ▼
 ARCH →      CONVERSION →
GUIDE        GUIDE
```

---

## 📊 Griglia di Navigazione per Ruolo

### 👨‍💻 **Sviluppatore Backend/Android**

| Task | Documento | Azione |
|------|-----------|--------|
| Nuova feature | ARCHITECTURE_GUIDELINES.md | Leggi "Clean Architecture" e pattern UseCase/ViewModel |
| Modifiche UI | IMPLEMENTATION_GUIDE.md | Vedi layout e flusso dati transazioni |
| Prima di commit | ARCHITECTURE_GUIDELINES.md | Checklist "Pre-Commit" |
| Domanda architettura | ARCHITECTURE_GUIDELINES.md | Cerca nella tabella indici (TOC) |

**Percorso consigliato:**
```
1. INDEX.md o README.md (orientamento 5 min)
   ↓
2. ARCHITECTURE_GUIDELINES.md (studio approfondito)
   ↓
3. Implementa feature
   ↓
4. Pre-Commit Checklist (verifica)
```

### 🎨 **UI/UX Developer**

| Task | Documento | Azione |
|------|-----------|--------|
| Modificare form transazioni | IMPLEMENTATION_GUIDE.md | Sezione "UI Layout" |
| Skeleton loading | IMPLEMENTATION_GUIDE.md | Sezione "AnimatedComponents" |
| Componenti Compose | ARCHITECTURE_GUIDELINES.md | Sezione "Android & Compose Best Practices" |
| Stringhe localizzate | ARCHITECTURE_GUIDELINES.md | Sezione "Internazionalizzazione" |

**Percorso consigliato:**
```
1. README.md (quick access)
   ↓
2. IMPLEMENTATION_GUIDE.md (UI pattern)
   ↓
3. ARCHITECTURE_GUIDELINES.md (best practices Compose)
   ↓
4. Implementa componenti
```

### 🗄️ **Data Engineer / DevOps**

| Task | Documento | Azione |
|------|-----------|--------|
| Conversione dati rapida | CONVERSION_GUIDE.md | Sezione "Uso Rapido" |
| Dettagli tecnici script | SCRIPT_CONVERSION_README.md | Schema input/output |
| Errori conversione | SCRIPT_CONVERSION_README.md | Sezione "Codici Errore" |
| Estendere script | SCRIPT_CONVERSION_README.md | Dettagli implementazione |

**Percorso consigliato:**
```
1. README.md (accesso rapido)
   ↓
2. CONVERSION_GUIDE.md (uso script)
   ↓
3. SCRIPT_CONVERSION_README.md (se manutenzione/estensione)
   ↓
4. Script: /opt/src/GIT/app/AntCashManager/scripts/convert_to_debug_data.py
```

### ⚖️ **Legal / Compliance**

| Task | Documento | Azione |
|------|-----------|--------|
| Privacy policy | privacy-policy.html | Documento ufficiale EN |
| Privacy localizzata | privacy-policy-XX.html | DE, ES, FR |
| Analytics consentiti | README.md o INDEX.md | Sezione "Privacy & Analytics" |
| Audit compliance | ARCHITECTURE_GUIDELINES.md | Sezione "Error Handling & Logging" |

**Percorso consigliato:**
```
1. README.md (vista privacy quick)
   ↓
2. privacy-policy.html (policy ufficiale)
```

### 👔 **Tech Lead / Architect**

| Task | Documento | Azione |
|------|-----------|--------|
| Review architettura | ARCHITECTURE_GUIDELINES.md | Intera guida + Anti-patterns |
| Decisioni design | IMPLEMENTATION_GUIDE.md | "Design Decisions" section |
| PR review checklist | ARCHITECTURE_GUIDELINES.md | "Pre-Commit Checklist" |
| Update guidelines | ARCHITECTURE_GUIDELINES.md | Modifica prioritaria |

**Percorso consigliato:**
```
1. INDEX.md (strategic overview)
   ↓
2. ARCHITECTURE_GUIDELINES.md (standard completo)
   ↓
3. IMPLEMENTATION_GUIDE.md (decisioni storiche)
   ↓
4. Review PR usando checklist
```

---

## 🔍 Ricerca Rapida per Argomento

### 🏗️ Architettura

| Argomento | Documento | Sezione |
|-----------|-----------|---------|
| Clean Architecture (3 layer) | ARCHITECTURE_GUIDELINES.md | "Clean Architecture - Struttura a Layer" |
| UseCase pattern | ARCHITECTURE_GUIDELINES.md | "UseCase (Domain Layer)" |
| ViewModel pattern | ARCHITECTURE_GUIDELINES.md | "ViewModel (Presentation Layer)" |
| State pattern | ARCHITECTURE_GUIDELINES.md | "State (Presentation Layer)" |
| Screen Composables | ARCHITECTURE_GUIDELINES.md | "Screen (Composables)" |
| Result<T> pattern | ARCHITECTURE_GUIDELINES.md | "Pattern Result negli UseCase" |
| Custom exceptions | ARCHITECTURE_GUIDELINES.md | "Custom Domain Exceptions" |
| Dispatcher injection | ARCHITECTURE_GUIDELINES.md | "Coroutines & Threading negli UseCase" |

### 🧪 Testing

| Argomento | Documento | Sezione |
|-----------|-----------|---------|
| UseCase testing | ARCHITECTURE_GUIDELINES.md | "UseCase Test con Dispatcher" |
| ViewModel testing | ARCHITECTURE_GUIDELINES.md | "ViewModel Test" |
| Fake repository | ARCHITECTURE_GUIDELINES.md | "Fake Repository" |
| StandardTestDispatcher | ARCHITECTURE_GUIDELINES.md | "TestDispatcher — quando usare quale" |
| Test naming | ARCHITECTURE_GUIDELINES.md | "Testing Requirements" |
| Cancellazione test | ARCHITECTURE_GUIDELINES.md | "Cancellazione Cooperativa" |

### 🎨 UI/Compose

| Argomento | Documento | Sezione |
|-----------|-----------|---------|
| Componenti disponibili | ARCHITECTURE_GUIDELINES.md | "Componenti UI - USO OBBLIGATORIO" |
| Stringhe localizzate | ARCHITECTURE_GUIDELINES.md | "Internazionalizzazione" |
| MaterialTheme | ARCHITECTURE_GUIDELINES.md | "Tema & Styling" |
| Skeleton loading | IMPLEMENTATION_GUIDE.md | "AnimatedComponents" |
| Form transazioni | IMPLEMENTATION_GUIDE.md | "UI Layout" |

### 🔄 Data Conversion

| Argomento | Documento | Sezione |
|-----------|-----------|---------|
| Uso rapido script | CONVERSION_GUIDE.md | "Uso Rapido" |
| Mapping dati | CONVERSION_GUIDE.md | "Mapping Dati" |
| Schema JSON | SCRIPT_CONVERSION_README.md | "Input/Output Schema" |
| Validazioni | SCRIPT_CONVERSION_README.md | "Validazioni" |
| Codici errore | SCRIPT_CONVERSION_README.md | "Codici Errore" |

### 🔐 Privacy/Security

| Argomento | Documento | Sezione |
|-----------|-----------|---------|
| Policy privacy | privacy-policy.html | Intero documento |
| Analytics consentiti | README.md o INDEX.md | "Privacy & Analytics" |
| Dati vietati | README.md o INDEX.md | "Dati NON Consentiti" |
| Logging securo | ARCHITECTURE_GUIDELINES.md | "Error Handling & Logging" |
| Secure data | ARCHITECTURE_GUIDELINES.md | "Performance and Security" |

---

## 📱 Documento per Feature

### Feature: Transazioni

| Operazione | Documento |
|-----------|-----------|
| Aggiungere transazione | ARCHITECTURE_GUIDELINES.md (UseCase pattern) |
| Mostrare lista transazioni | ARCHITECTURE_GUIDELINES.md (Screen/ViewModel) |
| Form di inserimento | IMPLEMENTATION_GUIDE.md |
| Test transazioni | ARCHITECTURE_GUIDELINES.md (Testing) |
| Errori transazioni | ARCHITECTURE_GUIDELINES.md (Custom Exceptions) |

### Feature: Categorie

| Operazione | Documento |
|-----------|-----------|
| CRUD categorie | ARCHITECTURE_GUIDELINES.md (UseCase/ViewModel) |
| Validazione categoria | ARCHITECTURE_GUIDELINES.md (Domain Exceptions) |
| Test categorie | ARCHITECTURE_GUIDELINES.md (Testing) |

### Feature: Backup/Restore

| Operazione | Documento |
|-----------|-----------|
| Convertire dati backup | CONVERSION_GUIDE.md |
| Dettagli conversione | SCRIPT_CONVERSION_README.md |
| Analytics backup | README.md (Privacy & Analytics) |

---

## 🔗 Link Diretti ai Documenti

### Principal Documents

- 📖 [INDEX.md](./INDEX.md) - **Indice centrale (INIZIA QUI)**
- 📖 [README.md](./README.md) - Descrizione wiki e accesso rapido
- 📖 [ARCHITECTURE_GUIDELINES.md](./ARCHITECTURE_GUIDELINES.md) - Architettura ufficiale
- 📖 [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) - Guide implementative feature

### Data Conversion

- 📖 [CONVERSION_GUIDE.md](./CONVERSION_GUIDE.md) - Guida rapida conversione dati
- 📖 [SCRIPT_CONVERSION_README.md](./SCRIPT_CONVERSION_README.md) - Dettagli tecnici script

### Privacy

- 📄 [privacy-policy.html](./privacy-policy.html) - Policy privacy (Inglese)
- 📄 [privacy-policy-de.html](./privacy-policy-de.html) - Policy privacy (Tedesco)
- 📄 [privacy-policy-es.html](./privacy-policy-es.html) - Policy privacy (Spagnolo)
- 📄 [privacy-policy-fr.html](./privacy-policy-fr.html) - Policy privacy (Francese)

---

## ⏱️ Tempo Medio di Lettura per Documento

| Documento | Tempo | Argomenti |
|-----------|-------|-----------|
| INDEX.md | 5 min | Overview + navigazione |
| README.md | 10 min | Intro + accesso rapido |
| ARCHITECTURE_GUIDELINES.md | 30-45 min | Studio completo della guida |
| IMPLEMENTATION_GUIDE.md | 15-20 min | Feature-specific (transazioni) |
| CONVERSION_GUIDE.md | 5 min | Uso rapido script |
| SCRIPT_CONVERSION_README.md | 15-20 min | Dettagli tecnici e troubleshooting |
| privacy-policy.html | 10 min | Policy completa |

---

## 🚀 Workflow Consigliato per Nuovi Sviluppatori

```
DAY 1: Orientamento
├─ Leggi INDEX.md (5 min)
├─ Leggi README.md (10 min)
└─ Sfoglia ARCHITECTURE_GUIDELINES.md TOC (5 min)

DAY 2: Studio Architettura
├─ Leggi ARCHITECTURE_GUIDELINES.md completo (45 min)
├─ Leggi IMPLEMENTATION_GUIDE.md (20 min)
└─ Leggi esempi di codice nel repo (30 min)

DAY 3+: Sviluppo
├─ Consulta checklist dal documento per feature
├─ Scrivi codice seguendo pattern
├─ Leggi sezione testing prima di scrivere test
└─ Pre-Commit Checklist (5 min)
```

---

## 💡 Tips per Navigazione Efficace

1. **Usa INDEX.md come home** - È il punto di partenza migliore
2. **Segui il tuo ruolo** - Ogni sezione ha una guida specifica
3. **Ricerca per argomento** - Usa la tabella "Ricerca Rapida per Argomento"
4. **Bookmarks** - Salva ARCHITECTURE_GUIDELINES.md, è la fonte ufficiale
5. **Ctrl+F** - Usa la ricerca nel browser per trovare argomenti specifici

---

**Ultima Modifica**: Maggio 2026  
**Versione**: 1.0  
**Scopo**: Facilitare navigazione e ricerca nella wiki AntCashManager

