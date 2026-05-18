# 📂 Struttura Documentazione - AntCashManager Wiki

Visualizzazione completa della struttura e dei contenuti della wiki.

---

## 🌳 Albero dei Documenti

```
wiki/
│
├── 📖 README.md ⭐ INIZIA DA QUI
│   └─ Descrizione wiki, accesso rapido, info progetto
│   └─ Link a: INDEX.md, QUICK_START.md, NAVIGATION.md
│
├── 📖 INDEX.md (INDICE CENTRALE)
│   └─ Elenco completo documenti con tabelle per ruolo
│   └─ FAQ rapida e flusso consigliato
│   └─ Link a: QUICK_START.md, NAVIGATION.md
│
├── ⚡ QUICK_START.md (5 MINUTI)
│   └─ Shortcut per profilo e percorsi comuni
│   └─ Le domande più frequenti
│   └─ Link diretti ai documenti essenziali
│
├── 🗺️ NAVIGATION.md (MAPPA VISUALE)
│   ├─ Griglia navigazione per ruolo
│   ├─ Ricerca rapida per argomento
│   ├─ Documento per feature
│   ├─ Tempo medio lettura
│   └─ Workflow per nuovi sviluppatori
│
├── 🏗️ ARCHITECTURE_GUIDELINES.md ⭐⭐⭐ UFFICIALE
│   ├─ Clean Architecture (3 layer)
│   ├─ Pattern UseCase/ViewModel/State/Screen
│   ├─ Result<T> pattern e custom exceptions
│   ├─ Dispatcher injection e coroutines
│   ├─ Testing (UseCase, ViewModel, Fake Repository)
│   ├─ Componenti UI disponibili
│   ├─ Internazionalizzazione (5 lingue)
│   ├─ Logging con Kermit
│   ├─ Anti-pattern da evitare
│   └─ Pre-Commit Checklist
│
├── 📝 IMPLEMENTATION_GUIDE.md
│   ├─ Milestone: skeleton loading + form transazioni
│   ├─ Dettagli implementazione UI
│   ├─ Flusso dati
│   ├─ UI Layout pattern
│   ├─ Test checklist
│   └─ Design decisions
│
├── 🔄 CONVERSION_GUIDE.md
│   ├─ Flusso rapido conversione dati
│   ├─ Uso script Python
│   ├─ Mapping dati (Transactions, Categories)
│   └─ Prerequisiti
│
├── 🔧 SCRIPT_CONVERSION_README.md
│   ├─ Dettagli tecnici script
│   ├─ Schema input/output JSON
│   ├─ Validazioni e regole conversione
│   ├─ Esempi log
│   └─ Codici errore
│
├── 🔐 privacy-policy.html (EN)
│   └─ Policy privacy ufficiale (lingua inglese)
│
├── 🔐 privacy-policy-de.html (DE)
│   └─ Policy privacy localizzata (lingua tedesca)
│
├── 🔐 privacy-policy-es.html (ES)
│   └─ Policy privacy localizzata (lingua spagnola)
│
├── 🔐 privacy-policy-fr.html (FR)
│   └─ Policy privacy localizzata (lingua francese)
│
└── 📂 STRUTTURA_DOCUMENTAZIONE.md (questo file)
    └─ Visualizzazione completa di contenuti e link
```

---

## 📊 Metadati File

| File | Tipo | Linee | Versione | Scopo |
|------|------|-------|---------|--------|
| README.md | Markdown | ~180 | 1.0 | Entry point con overview |
| INDEX.md | Markdown | ~180 | 1.0 | Indice centrale strutturato |
| QUICK_START.md | Markdown | ~200 | 1.0 | Accesso veloce (5 min) |
| NAVIGATION.md | Markdown | ~350 | 1.0 | Mappa visuale per ruolo |
| ARCHITECTURE_GUIDELINES.md | Markdown | 1574 | 1.4.6 | Guida architettura ufficiale |
| IMPLEMENTATION_GUIDE.md | Markdown | 419 | 1.4.6 | Guide implementative feature |
| CONVERSION_GUIDE.md | Markdown | 83 | 1.4.6 | Conversione dati rapida |
| SCRIPT_CONVERSION_README.md | Markdown | 214 | 1.4.6 | Dettagli script Python |
| privacy-policy.html | HTML | ~100 | 1.4.6 | Privacy policy (EN) |
| privacy-policy-de.html | HTML | ~100 | 1.4.6 | Privacy policy (DE) |
| privacy-policy-es.html | HTML | ~100 | 1.4.6 | Privacy policy (ES) |
| privacy-policy-fr.html | HTML | ~100 | 1.4.6 | Privacy policy (FR) |

---

## 🔗 Grafo di Cross-References

```
README.md
├─→ INDEX.md (indice principale)
├─→ QUICK_START.md (accesso 5 min)
└─→ NAVIGATION.md (mappa ruoli)

INDEX.md
├─→ QUICK_START.md
├─→ NAVIGATION.md
├─→ ARCHITECTURE_GUIDELINES.md
├─→ IMPLEMENTATION_GUIDE.md
├─→ CONVERSION_GUIDE.md
├─→ SCRIPT_CONVERSION_README.md
└─→ privacy-policy.html

QUICK_START.md
├─→ ARCHITECTURE_GUIDELINES.md
├─→ IMPLEMENTATION_GUIDE.md
├─→ CONVERSION_GUIDE.md
├─→ SCRIPT_CONVERSION_README.md
├─→ NAVIGATION.md
└─→ INDEX.md

NAVIGATION.md
├─→ ARCHITECTURE_GUIDELINES.md
├─→ IMPLEMENTATION_GUIDE.md
├─→ CONVERSION_GUIDE.md
├─→ SCRIPT_CONVERSION_README.md
└─→ privacy-policy.html

ARCHITECTURE_GUIDELINES.md
├─→ CONVERSION_GUIDE.md (riferimento)
├─→ SCRIPT_CONVERSION_README.md (riferimento)
└─→ IMPLEMENTATION_GUIDE.md (riferimento)

IMPLEMENTATION_GUIDE.md
└─→ ARCHITECTURE_GUIDELINES.md (riferimento)

CONVERSION_GUIDE.md
└─→ SCRIPT_CONVERSION_README.md (dettagli avanzati)

SCRIPT_CONVERSION_README.md
└─→ CONVERSION_GUIDE.md (guida rapida)
```

---

## 🎯 Come Navigare per Necessità

### Scenario 1: Sono nuovo, dove inizio?
```
1️⃣  README.md (leggi header)
2️⃣  QUICK_START.md (scegli il tuo profilo)
3️⃣  Documento principale per il tuo ruolo
```

### Scenario 2: Devo scrivere una nuova feature
```
1️⃣  QUICK_START.md sezione "Sviluppatore"
2️⃣  ARCHITECTURE_GUIDELINES.md sezione "Clean Architecture"
3️⃣  Copia l'esempio per il tuo tipo di feature
4️⃣  Scrivi test usando pattern in "Testing Requirements"
5️⃣  Verifica Pre-Commit Checklist
```

### Scenario 3: Devo modificare il form transazioni
```
1️⃣  QUICK_START.md sezione "UI Developer"
2️⃣  IMPLEMENTATION_GUIDE.md sezione "UI Layout"
3️⃣  Copia pattern e aggiungi campi
4️⃣  Aggiungi stringhe in strings.xml (5 lingue)
5️⃣  Verifica ARCHITECTURE_GUIDELINES.md "Componenti UI"
```

### Scenario 4: Devo convertire dati
```
1️⃣  QUICK_START.md sezione "Data Engineer"
2️⃣  CONVERSION_GUIDE.md sezione "Uso Rapido"
3️⃣  Run: python3 scripts/convert_to_debug_data.py
4️⃣  Se errore → SCRIPT_CONVERSION_README.md "Codici Errore"
```

### Scenario 5: Non so da dove iniziare
```
1️⃣  README.md (overview)
2️⃣  NAVIGATION.md (mappa per ruolo)
3️⃣  Scegli il tuo percorso dalla tabella
```

---

## 💾 Versioning & Manutenzione

### Quando Aggiornare

| Evento | File da aggiornare | Priorità |
|--------|-------------------|----------|
| Cambio pattern architetturale | ARCHITECTURE_GUIDELINES.md | 🔴 CRITICA |
| Nuova feature UI storica | IMPLEMENTATION_GUIDE.md | 🟡 Alta |
| Modifica script conversione | CONVERSION_GUIDE.md + SCRIPT_CONVERSION_README.md | 🟡 Alta |
| Aggiornamento versione app | Tutti (near top di ogni file) | 🟡 Alta |
| Nuova localizzazione privacy | privacy-policy-XX.html | 🟡 Alta |
| Aggiunta documento nuovo | INDEX.md, NAVIGATION.md, QUICK_START.md | 🟡 Alta |
| Miglioramento navigazione | INDEX.md, NAVIGATION.md, QUICK_START.md | 🟢 Bassa |

### Versionamento

- Wiki version segue versione app (es. app v1.4.6 → wiki v1.4.6)
- Ogni file ha sezione "Ultima Modifica" a fine documento
- Mantenere tutte le 5 localizzazioni sincronizzate

---

## 🔧 File di Configurazione & Script

File correlati (fuori da wiki):

```
/opt/src/GIT/app/AntCashManager/
├── androidApp/
│   ├── build.gradle.kts (versione app qui)
│   └── src/main/res/
│       ├── values/strings.xml (EN)
│       ├── values-it/strings.xml (IT)
│       ├── values-fr/strings.xml (FR)
│       ├── values-de/strings.xml (DE)
│       └── values-es/strings.xml (ES)
├── shared/ (Domain + Data layer)
├── scripts/
│   └── convert_to_debug_data.py (script conversione)
└── wiki/ (QUESTA CARTELLA)
    └── (tutti i .md e .html qui)
```

---

## 📈 Statistiche Wiki

| Metrica | Valore |
|---------|--------|
| **Documenti totali** | 12 (8 Markdown + 4 HTML privacy) |
| **Linee total** | ~2,500+ |
| **Lingue supportate** | 5 (EN, IT, FR, DE, ES) |
| **Sezioni principali** | 4 (Architettura, Implementazione, Conversione, Privacy) |
| **Versione wiki** | 1.0 (allineata a app v1.4.6) |
| **Ultimo update** | Maggio 2026 |
| **Tempo lettura completo** | ~2-3 ore |
| **Accesso rapido (QUICK_START)** | 5 minuti |

---

## ✅ Quality Checklist

Per mantenere la wiki di qualità:

- [ ] Tutti i documenti linkati correttamente
- [ ] Nessun link rotto
- [ ] Versione allineata a build.gradle.kts
- [ ] Tutte le 5 lingue (stringhe) sincronizzate
- [ ] Privacy policy aggiornata
- [ ] ARCHITECTURE_GUIDELINES.md è la fonte ufficiale
- [ ] Pre-Commit Checklist è completo
- [ ] Esempi di codice sono corretti
- [ ] Cross-reference sono coerenti
- [ ] Indice (INDEX.md) è aggiornato

---

## 🚀 Come Contribuire alla Wiki

Se scopri qualcosa di nuovo o vedi spazi da migliorare:

1. **Identifica il documento** da aggiornare
2. **Proponi il cambiamento** con chiara descrizione
3. **Aggiorna il file** seguendo lo stile e formato esistente
4. **Aggiorna cross-reference** se agiungi sezione nuova
5. **Verifica link** non siano rotti
6. **Aggiorna "Ultima Modifica"** a fine documento
7. **Commit** con messaggio chiaro

**Linee guida per contributi:**
- Mantieni tone professionale ma accessibile
- Usa markdown coerente con style del progetto
- Incluди esempi di codice quando pertinente
- Aggiungi link cross-document quando appropriato
- Mantieni sezioni in ordine logico

---

## 🔍 Ricerca Veloce (Ctrl+F)

Usa il browser Ctrl+F (Cmd+F su Mac) per cercare:

**Pattern**
- "UseCase" → ARCHITECTURE_GUIDELINES.md
- "ViewModel" → ARCHITECTURE_GUIDELINES.md
- "Screen" → ARCHITECTURE_GUIDELINES.md
- "Test" → ARCHITECTURE_GUIDELINES.md
- "Transaction" → IMPLEMENTATION_GUIDE.md
- "Form" → IMPLEMENTATION_GUIDE.md
- "Convert" → CONVERSION_GUIDE.md
- "Privacy" → README.md o privacy-policy.html

---

## 📞 Supporto

Se hai domande sulla wiki:

1. Consulta [QUICK_START.md](./QUICK_START.md) - FAQ rapida
2. Naviga con [NAVIGATION.md](./NAVIGATION.md) - mappa per ruolo
3. Cerca in [ARCHITECTURE_GUIDELINES.md](./ARCHITECTURE_GUIDELINES.md) - fonte ufficiale
4. Contatta Team AntCashManager per chiarimenti

---

**Ultima Modifica**: Maggio 2026  
**Versione**: 1.0  
**Scopo**: Visualizzazione struttura completa della wiki AntCashManager  
**Maintainer**: Team AntCashManager Documentation

