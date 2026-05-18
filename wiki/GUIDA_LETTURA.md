# 👋 Benvenuto nella Wiki AntCashManager

Questa guida ti aiuta a navigare la documentazione del progetto **AntCashManager** in base al tuo profilo.

> **Se non hai tempo**: [QUICK_START.md](./QUICK_START.md) (5 minuti)

---

## 🎯 Scegli il Tuo Profilo

### 1️⃣ 👨‍💻 Sono uno **Sviluppatore** (prima feature o modifiche)
**Hai bisogno di imparare l'architettura e i pattern del progetto.**

**Percorso suggerito** (90 minuti):
1. Leggi questo file fino alla fine (5 min)
2. Apri [QUICK_START.md](./QUICK_START.md) sezione "Sviluppatore" (5 min)
3. Leggi [ARCHITECTURE_GUIDELINES.md](./ARCHITECTURE_GUIDELINES.md) completo (45 min)
4. Leggi [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) (20 min)
5. Fai un esempio: scegli una feature semplice e sviluppala (120 min)
6. Prima del commit: usa Pre-Commit Checklist in ARCHITECTURE_GUIDELINES.md (5 min)

**Output**: Sarai pronto a sviluppare feature seguendo i pattern.

---

### 2️⃣ 🎨 Sono un **UI/UX Developer** (form, componenti, layout)
**Hai bisogno di capire come costruire interfacce in Compose.**

**Percorso suggerito** (60 minuti):
1. Leggi questo file fino alla fine (5 min)
2. Apri [QUICK_START.md](./QUICK_START.md) sezione "UI Developer" (5 min)
3. Leggi [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) (20 min)
4. Consulta [ARCHITECTURE_GUIDELINES.md](./ARCHITECTURE_GUIDELINES.md) sezioni:
   - "Componenti UI - USO OBBLIGATORIO" (10 min)
   - "Internazionalizzazione" (5 min)
   - "Tema & Styling" (5 min)
5. Fai un esempio: modifica un form aggiungendo un campo (60 min)

**Output**: Sarai pronto a creare interfacce consistenti e localizzate.

---

### 3️⃣ 🗄️ Sono un **Data Engineer/DevOps** (conversione dati, backup)
**Hai bisogno di convertire dati da PiggyBank Pro a AntCashManager.**

**Percorso suggerito** (30 minuti):
1. Leggi questo file fino alla fine (5 min)
2. Apri [QUICK_START.md](./QUICK_START.md) sezione "Data Engineer" (5 min)
3. Se usi lo script:
   - Leggi [CONVERSION_GUIDE.md](./CONVERSION_GUIDE.md) (5 min)
   - Esegui lo script: `python3 scripts/convert_to_debug_data.py` (5 min)
4. Se mantieni/estendi lo script:
   - Leggi [SCRIPT_CONVERSION_README.md](./SCRIPT_CONVERSION_README.md) (15 min)

**Output**: Sarai pronto a convertire dati o mantenere lo script.

---

### 4️⃣ ⚖️ Sono **Legal/Compliance** (privacy, policy)
**Hai bisogno di accedere alle policy privacy.**

**Percorso suggerito** (15 minuti):
1. Leggi questo file fino alla fine (5 min)
2. Apri [privacy-policy.html](./privacy-policy.html) per policy ufficiale (10 min)
3. Se localizzato:
   - [privacy-policy-de.html](./privacy-policy-de.html) (Tedesco)
   - [privacy-policy-es.html](./privacy-policy-es.html) (Spagnolo)
   - [privacy-policy-fr.html](./privacy-policy-fr.html) (Francese)
4. Per analytics consentiti: vedi [README.md](./README.md) sezione "Privacy & Analytics"

**Output**: Hai tutto ciò che serve per compliance e audit.

---

### 5️⃣ 👔 Sono un **Tech Lead/Architect** (review, decisioni, aggiornamenti)
**Hai bisogno di mantenere la documentazione e gli standard.**

**Percorso suggerito** (120 minuti):
1. Leggi questo file fino alla fine (5 min)
2. Leggi [INDEX.md](./INDEX.md) (10 min)
3. Leggi [ARCHITECTURE_GUIDELINES.md](./ARCHITECTURE_GUIDELINES.md) completo (60 min)
4. Leggi [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) (15 min)
5. Consulta [NAVIGATION.md](./NAVIGATION.md) per PR review process (10 min)
6. Usa Pre-Commit Checklist per review (5 min)

**Output**: Sarai autorità sugli standard del progetto.

---

## 🗺️ Mappa Documenti Principali

```
┌─────────────────────────────────────────────────────┐
│ 🏠 README.md ← LEGGI QUESTO PRIMA                   │
├─────────────────────────────────────────────────────┤
│ ⚡ QUICK_START.md (5 min) ← Accesso rapido          │
├─────────────────────────────────────────────────────┤
│ 📖 INDEX.md ← Indice organizzato                     │
├─────────────────────────────────────────────────────┤
│ 🗺️  NAVIGATION.md ← Mappa per ruolo                  │
├─────────────────────────────────────────────────────┤
│ 🏗️  ARCHITECTURE_GUIDELINES.md ⭐⭐⭐ UFFICIALE     │
│     (Clean Architecture, pattern, testing, checklist)│
├─────────────────────────────────────────────────────┤
│ 📝 IMPLEMENTATION_GUIDE.md                           │
│     (Feature storica: skeleton + form transazioni)   │
├─────────────────────────────────────────────────────┤
│ 🔄 CONVERSION_GUIDE.md + SCRIPT_CONVERSION_README.md│
│     (Conversione dati PiggyBank)                     │
├─────────────────────────────────────────────────────┤
│ 🔐 privacy-policy.html (+ localizzazioni)          │
│     (Policy privacy ufficiale)                       │
└─────────────────────────────────────────────────────┘
```

---

## ❓ Come Usare la Wiki

### Se Hai Fretta (5-10 minuti)
```
→ Apri QUICK_START.md
→ Scegli la tua categoria
→ Segui il percorso rapido
→ Accedi al documento principale
```

### Se Vuoi Capire la Struttura (20 minuti)
```
→ Leggi questo file (GUIDA_LETTURA.md)
→ Apri INDEX.md
→ Apri NAVIGATION.md per mappa visuale
→ Scegli il documento per il tuo task
```

### Se Devi Trovare Qualcosa Specifico (1-5 minuti)
```
→ Apri NAVIGATION.md sezione "Ricerca Rapida per Argomento"
→ Cerca il tuo argomento nella tabella
→ Clicca il documento suggerito
→ Usa Ctrl+F nel browser per cercare nella pagina
```

### Se È la Tua Prima Feature (90 minuti)
```
→ Leggi questa guida (5 min)
→ Leggi ARCHITECTURE_GUIDELINES.md sezione "Clean Architecture" (20 min)
→ Leggi un esempio di UseCase/ViewModel/Screen (15 min)
→ Leggi sezione Testing (15 min)
→ Scegli una feature semplice e sviluppala (60 min)
→ Pre-Commit Checklist prima di commit (5 min)
```

---

## 🔑 Concetti Chiave (per prepararti)

Prima di leggere la documentazione, conosci questi termini:

| Termine | Significato | Dove Imparare |
|---------|------------|----------------|
| **Clean Architecture** | Separazione in 3 layer: Presentation/Domain/Data | ARCHITECTURE_GUIDELINES.md |
| **UseCase** | Logica di business isolata e testabile | ARCHITECTURE_GUIDELINES.md sezione "UseCase" |
| **ViewModel** | Gestione stato UI in Kotlin | ARCHITECTURE_GUIDELINES.md sezione "ViewModel" |
| **State** | Immutable data class che rappresenta l'UI | ARCHITECTURE_GUIDELINES.md sezione "State" |
| **Screen** | Composable principale che rappresenta una schermata | ARCHITECTURE_GUIDELINES.md sezione "Screen" |
| **Result<T>** | Pattern per gestire successo/errore | ARCHITECTURE_GUIDELINES.md sezione "Pattern Result" |
| **Dispatcher** | Specifica quale thread eseguire il codice | ARCHITECTURE_GUIDELINES.md sezione "Coroutines" |
| **KMP** | Kotlin Multiplatform - shared code tra piattaforme | Introduzione documenti |

---

## 💡 Consigli per Navigazione Efficace

### 1️⃣ Usa INDEX.md come Punto di Partenza
È organizzato per ruolo e contiene link a tutto.

### 2️⃣ Quando Leggi ARCHITECTURE_GUIDELINES.md
Ha una tabella all'inizio con i capitoli principali. Usa quella per saltare direttamente quello che ti serve.

### 3️⃣ Usa Ctrl+F (Cmd+F su Mac) nel Browser
Quando cerchi un argomento specifico, Ctrl+F è più veloce di scorrere.

### 4️⃣ Salva i Bookmark
- ARCHITECTURE_GUIDELINES.md (fonte ufficiale)
- QUICK_START.md (accesso veloce)
- NAVIGATION.md (mappa)

### 5️⃣ Se Trovi un Errore o Spazio da Migliorare
Parla con il team e aggiorna la documentazione. La wiki è viva!

---

## 🎓 Percorsi di Apprendimento Completi

### 🟢 Primo Giorno (2 ore)
**Obbiettivo**: Capire la struttura di base

1. Leggi GUIDA_LETTURA.md (questo file) - 10 min
2. Leggi README.md - 10 min
3. Leggi QUICK_START.md - 10 min
4. Leggi NAVIGATION.md sezione "Dove Iniziare" - 10 min
5. Leggi ARCHITECTURE_GUIDELINES.md sezione "Clean Architecture" - 20 min
6. Leggi un esempio di UseCase/ViewModel nel documento - 20 min
7. Domande? Consulta FAQ in QUICK_START.md - 10 min

**Totale**: ~90 minuti

### 🟡 Prima Settimana (6 ore)
**Obbiettivo**: Pronto a scrivere la prima feature

Giorno 1 (✅ sopra) + :
- Leggi ARCHITECTURE_GUIDELINES.md completo - 60 min
- Leggi IMPLEMENTATION_GUIDE.md - 20 min
- Leggi sezione Testing in ARCHITECTURE_GUIDELINES.md - 20 min
- Esamina codice esistente nel repo - 30 min
- Scrivi prima feature semplice - 90 min

**Totale**: ~6 ore

### 🔴 Settimana 2+ (Ongoing)
**Obbiettivo**: Mastery

- Mantieni ARCHITECTURE_GUIDELINES.md come riferimento
- Consulta per ogni nuova feature
- Proponi miglioramenti
- Aiuta altri sviluppatori
- Aggiorna wiki quando scopri nuovi pattern

---

## 🔗 Link Rapidi

| Cosa Cerchi | Clicca |
|------------|--------|
| Indice principale | [INDEX.md](./INDEX.md) |
| Accesso veloce (5 min) | [QUICK_START.md](./QUICK_START.md) |
| Mappa navigazione | [NAVIGATION.md](./NAVIGATION.md) |
| Architettura (UFFICIALE) | [ARCHITECTURE_GUIDELINES.md](./ARCHITECTURE_GUIDELINES.md) |
| Feature storica: UI transazioni | [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) |
| Conversione dati | [CONVERSION_GUIDE.md](./CONVERSION_GUIDE.md) |
| Dettagli script | [SCRIPT_CONVERSION_README.md](./SCRIPT_CONVERSION_README.md) |
| Privacy policy | [privacy-policy.html](./privacy-policy.html) |
| Struttura completa | [STRUTTURA_DOCUMENTAZIONE.md](./STRUTTURA_DOCUMENTAZIONE.md) |

---

## ✅ Checklist per Iniziare

- [ ] Ho letto questo file (GUIDA_LETTURA.md)
- [ ] Ho identificato il mio profilo (Sviluppatore, UI, Data, Legal, Tech Lead)
- [ ] Ho salvato i documenti principali nei bookmark
- [ ] Ho aperto il primo documento del mio percorso
- [ ] Se ho domande, ho consultato QUICK_START.md FAQ
- [ ] Sono pronto a iniziare!

---

## 📞 Non Sai Da Dove Iniziare?

1. **Hai fretta?** → [QUICK_START.md](./QUICK_START.md) (5 min)
2. **Vuoi una mappa?** → [NAVIGATION.md](./NAVIGATION.md) (10 min)
3. **Vuoi l'indice?** → [INDEX.md](./INDEX.md) (15 min)
4. **Vuoi capire tutto?** → Continua a leggere questa guida (20 min)

---

## 🎯 Il Tuo Prossimo Step

Basato su dove sei ora:

- ✅ **Ho finito di leggere questa guida** → Apri [QUICK_START.md](./QUICK_START.md)
- ✅ **Ho identificato il mio profilo** → Segui il percorso suggerito per il tuo profilo (sopra)
- ✅ **Sono pronto a iniziare** → Apri il primo documento del tuo percorso

---

**Benvenuto in AntCashManager! 🎉**

La documentazione è qui per supportarti. Se trovi spazi da migliorare, aiuta il team a renderla ancora migliore.

---

**Versione**: 1.0  
**Ultimo Aggiornamento**: Maggio 2026  
**Scopo**: Guidare il primo accesso alla wiki AntCashManager

