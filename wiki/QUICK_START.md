# ⚡ Quick Start - AntCashManager Wiki

Hai 5 minuti? Inizia da qui.

---

## 🎯 Scegli il Tuo Percorso

### 👨‍💻 Sviluppatore (Prima Feature)
```
1. Leggi → ARCHITECTURE_GUIDELINES.md sezione "Clean Architecture"
2. Cerca → il tuo tipo di feature (UseCase, ViewModel, Screen)
3. Copia → l'esempio fornito
4. Implementa → seguendo il pattern
5. Test → usando gli esempi di test
6. Commit → dopo Pre-Commit Checklist
```
⏱️ **30-45 minuti**

### 🎨 UI Developer (Modifiche Transazioni)
```
1. Leggi → IMPLEMENTATION_GUIDE.md sezione "UI Layout"
2. Copia → pattern di form/layout già implementato
3. Aggiungi → nuovi campi seguendo lo stesso pattern
4. Verifica → con ARCHITECTURE_GUIDELINES.md "Componenti UI"
5. Localizza → tutte le stringhe in strings.xml (5 lingue)
6. Commit → dopo pre-checklist
```
⏱️ **20-30 minuti**

### 🗄️ Data Engineer (Conversione Dati)
```
1. Leggi → CONVERSION_GUIDE.md "Uso Rapido"
2. Esegui → python3 scripts/convert_to_debug_data.py
3. Se errore → Leggi SCRIPT_CONVERSION_README.md "Codici Errore"
4. Se manutenzione → Leggi SCRIPT_CONVERSION_README.md completo
```
⏱️ **5-10 minuti (uso)** | **20-30 minuti (manutenzione)**

### ⚖️ Legal (Privacy Policy)
```
1. Leggi → privacy-policy.html
2. Localizzazioni → privacy-policy-{de,es,fr}.html
3. Analytics consentiti → vedi README.md sezione "Privacy & Analytics"
```
⏱️ **10-15 minuti**

---

## 📚 Documenti Essenziali

| Hai bisogno di... | Vai a | Tempo |
|------------------|--------|-------|
| Capire l'architettura | [ARCHITECTURE_GUIDELINES.md](./ARCHITECTURE_GUIDELINES.md) | 45 min |
| Modificare UI transazioni | [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) | 20 min |
| Convertire dati | [CONVERSION_GUIDE.md](./CONVERSION_GUIDE.md) | 5 min |
| Mantenere lo script | [SCRIPT_CONVERSION_README.md](./SCRIPT_CONVERSION_README.md) | 20 min |
| Controllare privacy | [privacy-policy.html](./privacy-policy.html) | 10 min |
| Navigazione completa | [NAVIGATION.md](./NAVIGATION.md) | 10 min |

---

## 🔥 Shortcut Comuni

### Aggiungo una nuova feature?
```
→ ARCHITECTURE_GUIDELINES.md
  → "UseCase (Domain Layer)" per la logica
  → "ViewModel" per lo stato
  → "Screen (Composables)" per l'UI
  → "Testing Requirements" per i test
```

### Modifico il form transazioni?
```
→ IMPLEMENTATION_GUIDE.md
  → "UI Layout" sezione transazioni
  → Copia il pattern per nuovi campi
  → Aggiungi stringhe in strings.xml (5 lingue)
```

### Converto dati da PiggyBank?
```
→ CONVERSION_GUIDE.md
  → Run: python3 scripts/convert_to_debug_data.py
  → Se errore → SCRIPT_CONVERSION_README.md
```

### Creo un componente UI?
```
→ ARCHITECTURE_GUIDELINES.md sezione "Componenti UI"
  → Verifica se esiste già
  → Se SÌ: usa quello
  → Se NO: crea nuovo seguendo pattern, aggiungi @Preview
```

### Scrivo test?
```
→ ARCHITECTURE_GUIDELINES.md sezione "Testing Requirements"
  → UseCase Test: vedi "UseCase Test con Dispatcher"
  → ViewModel Test: vedi "ViewModel Test"
  → Naming: `method_shouldBehavior_whenCondition`
```

### Sto facendo una PR?
```
→ ARCHITECTURE_GUIDELINES.md sezione "Pre-Commit Checklist"
  → Verifica tutti i punti
  → Rimuovi import non usati
  → Package name corretto
  → State immutabile
```

---

## 🔗 Link Rapidi

| Documento | Link |
|-----------|------|
| Indice Principale | [INDEX.md](./INDEX.md) |
| Architettura (UFFICIALE) | [ARCHITECTURE_GUIDELINES.md](./ARCHITECTURE_GUIDELINES.md) |
| Implementazione UI | [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) |
| Conversione Dati | [CONVERSION_GUIDE.md](./CONVERSION_GUIDE.md) |
| Dettagli Script | [SCRIPT_CONVERSION_README.md](./SCRIPT_CONVERSION_README.md) |
| Privacy Policy | [privacy-policy.html](./privacy-policy.html) |
| Navigazione per Ruolo | [NAVIGATION.md](./NAVIGATION.md) |

---

## ❓ Le Domande Più Comuni

**P: Come inizio se è la mia prima feature?**  
R: ARCHITECTURE_GUIDELINES.md → sezione "Clean Architecture" + cerca il tipo di feature → copia l'esempio.

**P: Come aggiungo un campo al form transazioni?**  
R: IMPLEMENTATION_GUIDE.md → "UI Layout" → copia pattern, aggiungi stringhe in strings.xml (5 lingue).

**P: Posso usare hardcoded string?**  
R: NO. SEMPRE `stringResource(R.string.*)`. Vedi ARCHITECTURE_GUIDELINES.md "Internazionalizzazione".

**P: Devo scrivere test?**  
R: SÌ. Vedi ARCHITECTURE_GUIDELINES.md "Testing Requirements" per pattern.

**P: Come converto dati da PiggyBank?**  
R: `python3 scripts/convert_to_debug_data.py`. Vedi CONVERSION_GUIDE.md.

**P: Che cosa è Result<T>?**  
R: Pattern per gestire successo/errore. ARCHITECTURE_GUIDELINES.md → "Pattern Result negli UseCase".

**P: Come gestisco gli errori?**  
R: Custom domain exceptions nel Domain layer. ARCHITECTURE_GUIDELINES.md → "Custom Domain Exceptions".

**P: Pre-Commit Checklist è obbligatorio?**  
R: SÌ. ARCHITECTURE_GUIDELINES.md → ultima sezione. Controlla prima di PR.

---

## 🎓 Livelli di Apprendimento

### 🟢 Principiante (Primi 3 giorni)
1. ✅ Leggi INDEX.md (5 min)
2. ✅ Leggi README.md (10 min)
3. ✅ Leggi ARCHITECTURE_GUIDELINES.md sezione "Clean Architecture" (15 min)
4. ✅ Leggi un esempio UseCase/ViewModel/Screen nel documento (20 min)
5. ✅ Leggi sezione Testing (15 min)
**Total: ~60 minuti**

### 🟡 Intermedio (Settimana 1)
1. ✅ Leggi ARCHITECTURE_GUIDELINES.md completo (45 min)
2. ✅ Leggi IMPLEMENTATION_GUIDE.md (20 min)
3. ✅ Leggi codice esistente nel repo (30 min)
4. ✅ Scrivi prima feature con test (120 min)
**Total: ~4 ore**

### 🔴 Avanzato (Settimana 2+)
1. ✅ Mantieni ARCHITECTURE_GUIDELINES.md aggiornato
2. ✅ Crea pattern nuovi se necessario
3. ✅ Review PR usando Pre-Commit Checklist
4. ✅ Update wiki quando scopri miglioramenti
**Total: Ongoing**

---

## 🚀 Next Steps

- **Leggi il documento appropriato per il tuo ruolo** (vedi tabella sopra)
- **Usa NAVIGATION.md se hai bisogno di una mappa completa**
- **Consulta ARCHITECTURE_GUIDELINES.md per decisioni architetturali**
- **Verifica Pre-Commit Checklist prima di ogni commit**

---

**Non sai da dove iniziare?**  
→ Leggi [INDEX.md](./INDEX.md) per una panoramica strutturata per ruolo.

**Preferisci una mappa visuale?**  
→ Leggi [NAVIGATION.md](./NAVIGATION.md) per griglie per argomento e ricerca rapida.

---

**Ultima Modifica**: Maggio 2026  
**Versione**: 1.0  
**Scopo**: Accesso veloce ai documenti essenziali della wiki

