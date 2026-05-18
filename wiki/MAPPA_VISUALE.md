# 🗺️ Mappa Visuale Rapida - Wiki AntCashManager

Una mappa ASCII art per capire velocemente la struttura della wiki.

---

## 🏗️ Architettura della Wiki

```
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│              📖 README.md (ENTRY POINT)                     │
│         ← Leggi questo primo per sapere dove andare          │
│                                                              │
└──────────────────────────────────────────────────────────────┘
                              │
                    ┌─────────┼─────────┐
                    │         │         │
                    ▼         ▼         ▼
        ┌──────────────┐ ┌────────────┐ ┌──────────────┐
        │ 👋 GUIDA_    │ │ ⚡ QUICK_ │ │ 📖 INDEX.md  │
        │ LETTURA.md   │ │ START.md   │ │ (Indice)     │
        │ (Onboarding) │ │ (5 min)    │ └──────────────┘
        └──────────────┘ └────────────┘      │
               │              │               │
         [5 percorsi]    [categorie]  [tabelle per ruolo]
         per profilo     comuni        e documenti
                │              │               │
                └──────────────┼───────────────┘
                               │
                    ┌──────────┴──────────┐
                    │                     │
                    ▼                     ▼
        ┌────────────────────┐  ┌──────────────────┐
        │ 🗺️ NAVIGATION.md    │  │ 📂 STRUTTURA_    │
        │ (Mappa per ruolo)   │  │ DOCUMENTAZIONE   │
        │ + Ricerca rapida    │  │ (Albero + Meta)  │
        └────────────────────┘  └──────────────────┘
                    │                     │
         [Griglia per ruolo]     [Visualizzazione
          [Argomenti]             completa file]
          [FAQ]                   [Quality check]

                    │                     │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │                     │
                    ▼                     ▼
        ┌──────────────────────────┐ ┌──────────────┐
        │ 🏗️ ARCHITECTURE_         │ │ 📝 IMPL.     │
        │ GUIDELINES.md ⭐⭐⭐     │ │ GUIDE.md     │
        │ (UFFICIALE)              │ │ (Feature UI) │
        │                          │ │              │
        │ • Clean Arch             │ │ • Skeleton   │
        │ • Pattern UseCase        │ │ • Form       │
        │ • ViewModel/State        │ │ • Layout     │
        │ • Testing               │ │ • Design     │
        │ • Pre-Commit Checklist  │ │              │
        └──────────────────────────┘ └──────────────┘
                    │                     │
                    │          ┌──────────┘
                    │          │
                    ▼          ▼
        ┌──────────────────────────┐
        │ 🔄 CONVERSION_GUIDE.md    │
        │ (Data conversion)         │
        │                          │
        │ • Uso rapido script      │
        │ • Mapping dati           │
        │ • Prerequisiti           │
        └──────────────────────────┘
                    │
                    ▼
        ┌──────────────────────────┐
        │ 🔧 SCRIPT_CONVERSION_    │
        │ README.md                │
        │ (Dettagli tecnici)       │
        │                          │
        │ • Schema JSON            │
        │ • Validazioni            │
        │ • Codici errore          │
        └──────────────────────────┘

        ┌──────────────────────────┐
        │ 🔐 privacy-policy.html   │
        │ (+ localizzazioni)       │
        │                          │
        │ • EN, DE, ES, FR         │
        └──────────────────────────┘
```

---

## 🎯 Scegli il Tuo Percorso

```
START: README.md
  │
  ├─ 👤 "Sono nuovo"
  │   └─→ GUIDA_LETTURA.md
  │       └─→ [Scegli profilo]
  │           └─→ Documento principale
  │
  ├─ ⚡ "Ho fretta"
  │   └─→ QUICK_START.md
  │       └─→ [Categoria]
  │           └─→ Documento principale
  │
  ├─ 🔍 "Cerco un argomento"
  │   └─→ NAVIGATION.md
  │       └─→ [Ricerca rapida]
  │           └─→ Documento principale
  │
  ├─ 📋 "Voglio il completo"
  │   └─→ INDEX.md
  │       └─→ [Tabella per ruolo]
  │           └─→ Documento principale
  │
  └─ 📂 "Voglio capire la struttura"
      └─→ STRUTTURA_DOCUMENTAZIONE.md
          └─→ [Albero documenti]
              └─→ Documento principale
```

---

## 👥 Routing per Profilo

```
┌─────────────────────────────────────────────────────┐
│                  5 PROFILI                          │
├─────────────────────────────────────────────────────┤
│                                                     │
│  👨‍💻 SVILUPPATORE      → ARCHITECTURE_GUIDELINES.md  │
│  ├─ Clean Arch                                      │
│  ├─ Pattern UseCase/ViewModel/State                 │
│  ├─ Testing                                         │
│  └─ Pre-Commit Checklist                            │
│                                                     │
│  🎨 UI DEVELOPER     → IMPLEMENTATION_GUIDE.md      │
│  ├─ Form transazioni                                │
│  ├─ Skeleton loading                                │
│  ├─ Layout pattern                                  │
│  └─ Stringhe localizzate                            │
│                                                     │
│  🗄️ DATA ENGINEER    → CONVERSION_GUIDE.md          │
│  ├─ Uso rapido script                               │
│  ├─ Mapping dati                                    │
│  └─ Se dettagli → SCRIPT_CONVERSION_README.md       │
│                                                     │
│  ⚖️ LEGAL           → privacy-policy.html           │
│  ├─ Policy completa                                 │
│  ├─ 4 localizzazioni                                │
│  └─ Analytics consentiti                            │
│                                                     │
│  👔 TECH LEAD       → ARCHITECTURE_GUIDELINES.md    │
│  ├─ Decisioni architetturali                        │
│  ├─ Review PR checklist                             │
│  ├─ Aggiornamento guidelines                        │
│  └─ Design decisions                                │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## ⏱️ Tempo Lettura per Documento

```
QUICK_START.md ⚡
├─ ⏱️ 5 minuti
├─ Per: Chi ha fretta
└─ Contiene: Shortcut e percorsi veloci

GUIDA_LETTURA.md 👋
├─ ⏱️ 20 minuti (lettura intera)
├─ Per: Primo accesso
└─ Contiene: Percorsi personalizzati per profilo

INDEX.md 📖
├─ ⏱️ 15 minuti
├─ Per: Capire la struttura
└─ Contiene: Indice tabellare per ruolo

NAVIGATION.md 🗺️
├─ ⏱️ 10-15 minuti
├─ Per: Ricerca per argomento
└─ Contiene: Griglie e argomenti

ARCHITECTURE_GUIDELINES.md 🏗️
├─ ⏱️ 45-60 minuti (primo studio)
├─ Per: Developer che scrive feature
└─ Contiene: Pattern completi con esempi

IMPLEMENTATION_GUIDE.md 📝
├─ ⏱️ 20 minuti
├─ Per: Chi modifica UI transazioni
└─ Contiene: Feature-specific implementation

CONVERSION_GUIDE.md 🔄
├─ ⏱️ 5-10 minuti
├─ Per: Chi converte dati (uso rapido)
└─ Contiene: Flusso script

SCRIPT_CONVERSION_README.md 🔧
├─ ⏱️ 20-30 minuti
├─ Per: Chi mantiene lo script
└─ Contiene: Dettagli tecnici

privacy-policy.html 🔐
├─ ⏱️ 10-15 minuti
├─ Per: Legal/Compliance
└─ Contiene: Policy ufficiale
```

---

## 🔗 Link Graph

```
README.md
    ├─→ GUIDA_LETTURA.md
    ├─→ QUICK_START.md
    ├─→ INDEX.md
    └─→ NAVIGATION.md
        
GUIDA_LETTURA.md
    ├─→ QUICK_START.md
    ├─→ ARCHITECTURE_GUIDELINES.md
    └─→ NAVIGATION.md

QUICK_START.md
    ├─→ ARCHITECTURE_GUIDELINES.md
    ├─→ IMPLEMENTATION_GUIDE.md
    ├─→ CONVERSION_GUIDE.md
    └─→ INDEX.md

INDEX.md
    ├─→ ARCHITECTURE_GUIDELINES.md
    ├─→ IMPLEMENTATION_GUIDE.md
    ├─→ CONVERSION_GUIDE.md
    ├─→ SCRIPT_CONVERSION_README.md
    └─→ privacy-policy.html

NAVIGATION.md
    ├─→ ARCHITECTURE_GUIDELINES.md
    ├─→ IMPLEMENTATION_GUIDE.md
    ├─→ CONVERSION_GUIDE.md
    ├─→ SCRIPT_CONVERSION_README.md
    └─→ privacy-policy.html

ARCHITECTURE_GUIDELINES.md
    ├─→ CONVERSION_GUIDE.md (riferimento)
    └─→ IMPLEMENTATION_GUIDE.md (riferimento)

CONVERSION_GUIDE.md
    └─→ SCRIPT_CONVERSION_README.md (dettagli)

SCRIPT_CONVERSION_README.md
    └─→ CONVERSION_GUIDE.md (guida rapida)
```

---

## 📊 Statistiche a Colpo d'Occhio

```
┌──────────────────────────────────┐
│ DOCUMENTI: 15                    │
├──────────────────────────────────┤
│ Markdown:     11                 │
│ HTML (Privacy): 4                │
│ TOTALE LINEE: ~4,000+            │
│ LINGUE:       5 (EN, IT, DE, ES, FR) │
│ VERSIONE:     1.0 (allineata a app 1.4.6) │
└──────────────────────────────────┘

┌──────────────────────────────────┐
│ NAVIGAZIONE: 5 MODI              │
├──────────────────────────────────┤
│ • GUIDA_LETTURA (Profilo)        │
│ • QUICK_START (5 minuti)         │
│ • INDEX (Tabelle)                │
│ • NAVIGATION (Argomenti)         │
│ • STRUTTURA (Albero)             │
└──────────────────────────────────┘

┌──────────────────────────────────┐
│ PROFILI: 5                       │
├──────────────────────────────────┤
│ • Sviluppatore                   │
│ • UI Developer                   │
│ • Data Engineer                  │
│ • Legal/Compliance               │
│ • Tech Lead/Architect            │
└──────────────────────────────────┘
```

---

## ✨ Feature della Wiki

```
✅ Accesso Centralizzato
   └─ README.md come entry point
   └─ Link prominenti ai 5 percorsi

✅ Navigazione Personalizzata
   └─ GUIDA_LETTURA per profilo
   └─ QUICK_START per task comune
   └─ INDEX per indice tabellare
   └─ NAVIGATION per ricerca

✅ Onboarding Strutturato
   └─ Percorsi per Giorno 1, Settimana 1, Settimana 2+
   └─ Checklist di verifica

✅ Ricerca Veloce
   └─ Tabella argomenti in NAVIGATION.md
   └─ Uso di Ctrl+F nel browser

✅ Cross-Reference Coerenti
   └─ Header con link a INDEX in ogni documento
   └─ Link back to README

✅ Quality Assurance
   └─ VERIFICA_COMPLETAMENTO.md
   └─ STRUTTURA_DOCUMENTAZIONE.md
   └─ Checklist manutenzione
```

---

## 🚀 Primo Accesso

```
1️⃣ README.md
   ├─ Leggi intro
   └─ Scegli link (3 opzioni)
   
2️⃣ GUIDA_LETTURA.md (consigliato)
   ├─ Scegli profilo
   └─ Segui percorso
   
3️⃣ Documento Principale
   ├─ Leggi il documento
   └─ Consolida apprendimento
   
4️⃣ Sei Pronto!
   ✅ Inizia a sviluppare/contribuire
```

---

**Wiki Version**: 1.0 con Indice Centralizzato  
**Ultimo Update**: Maggio 2026  
**Status**: ✅ Struttura Completa e Navigabile  
**Prossimo Step**: Usare la wiki! 🎉

