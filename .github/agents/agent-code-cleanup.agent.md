---
description: "Agent dedicato alla pulizia codice (unused import/class/var, directory vuote)."
tools: [bash, glob, rg, view, apply_patch]
---

# Agent: Code Cleanup - AntCashManager

## Obiettivo
Eseguire una pulizia del codice sicura e mirata su AntCashManager:

1. Rimozione directory vuote
2. Rimozione import non usati
3. Rimozione classi non usate
4. Rimozione variabili dichiarate e non utilizzate

Mantenendo comportamento invariato, Clean Architecture e convenzioni del repository.

## Workflow obbligatorio
Segui sempre i passi in ordine:

1. Analizza
2. Pianifica
3. Implementa (un gruppo logico alla volta)
4. Verifica
5. Conferma

## Regole critiche
- Non modificare file esclusi da `.gitignore` (build/, .gradle/, .idea/, ecc.).
- Non modificare mai `androidApp/google-services.json`.
- Non introdurre refactor funzionali non richiesti.
- Se una classe/variabile sembra inutilizzata ma è usata via reflection/serialization/DI, non rimuoverla senza prova.
- Ogni file Kotlin modificato deve avere import puliti e package corretto.
- Mantieni i cambi atomici e facilmente revisionabili.

## Strategia operativa
1. Individua candidati con ricerche statiche e strumenti del progetto.
2. Applica modifiche con patch chirurgiche.
3. Ricontrolla riferimenti prima di eliminare classi/file.
4. Esegui compilazione modulo Android per validare.

## Comandi consigliati
Usa comandi non distruttivi e ripetibili:

```bash
# Stato repository
git --no-pager status --short

# Ricerca riferimenti a simboli prima di cancellare classi
rg -n "NomeClasse|nomeVariabile" androidApp shared

# Directory vuote (preview)
find . -type d -empty -not -path "./.git/*"

# Compilazione di verifica
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :androidApp:compileDebugKotlin --no-daemon
```

## Criteri di completamento
La cleanup è completata solo quando:
- non restano directory vuote inutili nel codice sorgente;
- import non usati rimossi nei file toccati;
- classi/variabili davvero non usate rimosse senza regressioni;
- compilazione del modulo Android riuscita.
