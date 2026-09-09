---
description: "Pulizia sicura di codice e risorse: import/classi/variabili non usati, directory vuote, stringhe/drawable/xml non referenziati. Nessun cambio di comportamento."
---

# Agent: Code Cleanup

Regole generali in [AGENTS.md §2, §3](../../AGENTS.md). Workflow: analizza → pianifica → un gruppo logico alla volta → compila → conferma.

## Scope

1. Directory vuote nel sorgente
2. Import non usati
3. Classi / funzioni / variabili non referenziate
4. Risorse `string` / `drawable` / `xml` non referenziate

## Regole

- Non toccare file in `.gitignore`, `google-services.json`, file generati.
- Nessun refactor funzionale: solo rimozioni provate.
- Prima di eliminare un simbolo: cerca usi via reflection, serialization (`@Serializable`), Koin (`get<X>()`, `::X`), `AndroidManifest`, `proguard-rules.pro`. Se c'è un dubbio, non rimuovere.
- Prima di eliminare una risorsa: cerca in `R.string.x`, `@string/x`, `R.drawable.x`, `@drawable/x`, manifest, `themes.xml`, widget `xml/`, e nome costruito dinamicamente (`getIdentifier`).
- Le stringhe vanno rimosse da **tutte le 13 locale** (`values*/strings.xml`) o da `values/untranslable.xml` se non traducibili, mai da un file solo.
- Non rimuovere `@Preview` in `ui/components/` e `screen/*/view/` (richieste); i `*Screen` root non ne hanno.
- Non rimuovere classi `Fake*`/`TestDataBuilder` in `testutil/` anche se poco usate.
- Modifiche atomiche e revisionabili; import puliti e package corretto in ogni file toccato.

## Comandi

```bash
git --no-pager status --short
rg -n "NomeClasse|nomeVariabile" androidApp shared
rg -n "R\.string\.chiave|@string/chiave" androidApp
rg -n "R\.drawable\.nome|@drawable/nome|R\.xml\.nome|@xml/nome" androidApp
find androidApp/src shared/src -type d -empty
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
./gradlew :androidApp:compileFullDebugKotlin 2>&1 | tail -30
```

## Completamento

- Nessuna directory vuota inutile; import puliti nei file toccati
- Simboli e risorse rimossi solo con prova di non-uso; 13 locale coerenti
- `compileFullDebugKotlin` verde; test mirati dei file toccati verdi
