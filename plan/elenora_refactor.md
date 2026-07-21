# Nuove categorie di default da foglio spese personali — AntCashManager

## Contesto

L'utente ha condiviso un foglio Excel/CSV ("Spese mensili") usato per la gestione del budget di casa, con l'obiettivo di (1) aggiungere alle categorie di default dell'app quelle che mancano rispetto a quanto usato nel foglio, e (2) valutare ulteriori migliorie ispirate dalla struttura del foglio.

**Analisi del foglio** (sezioni macro con relative sotto-voci, ognuna con un totale mensile):
- PRODOTTI ALIMENTARI (spesa, cene fuori, pranzo lavoro, aperitivi/snack)
- INTRATTENIMENTO (cinema/teatro, concerti, eventi sportivi, svago)
- PER LA PERSONA (visite mediche/esami, farmacia, parrucchiera/ceretta, spa/trattamenti, + voci molto personali/informali non generalizzabili)
- SHOPPING (abbigliamento, accessori, scarpe, trucco/skincare/haircare, + voci informali)
- TRASPORTI (carburante, manutenzione, assicurazione/bollo, pedaggi/parcheggi)
- CASA (cellulare, Netflix/Prime/Disney+, manutenzione/riparazione, bollette, affitto)
- REGALI
- Più in alto: riepilogo "Entrate mensili" (Stipendio + Entrate extra) e un **Budget mensile con relativo saldo scoperto** (budget €500 vs speso, saldo negativo mostrato).

**Confronto con le categorie di default attuali dell'app** (seed in `androidApp/src/main/kotlin/com/antcashmanager/android/AntCashManagerApp.kt`, righe 45-78, eseguito da `seedDefaultCategories()`):
- EXPENSE: Non categorizzato, Casa, Trasporti, Cibo, Bollette, Pranzi/Cene fuori, Divertimento, Salute, Shopping, Istruzione, Altro
- INCOME: Non categorizzato, Stipendio, Paghetta, Rimborso, Investimenti, Freelance, Altro

La maggior parte delle macro-sezioni del foglio è già coperta (Cibo/Pranzi-Cene fuori, Divertimento, Shopping, Trasporti, Casa/Bollette). Le sotto-voci più granulari (benzina, parcheggi, farmacia, abbonamenti streaming...) non richiedono categorie dedicate: l'app usa categorie piatte (non gerarchiche) e queste rientrano già nelle macro-categorie esistenti tramite titolo/note della transazione.

**Gap reali identificati** (categorie mancanti, generalizzabili, non presenti né come macro né come icona dedicata già pronta):
1. **Regali** (REGALI nel foglio) — assente, EXPENSE. Icona `redeem` già esistente nella mappa icone (`CategoriesScreen.kt` riga 122) e già con descrizione "Gifts" in inglese — semplicemente non ancora usata da nessuna categoria di default.
2. **Abbonamenti** (Netflix/Prime/Disney+ nel foglio, oggi annegati genericamente in "Casa") — assente come categoria propria pur avendo già l'icona `subscriptions` pronta e inutilizzata (riga 129).
3. **Cura personale** (spa/trattamenti, parrucchiera/ceretta nel foglio — distinta dalle visite mediche, già coperte da "Salute") — assente, richiede una nuova icona `spa` (Material Icons Extended, già disponibile come dipendenza).

Le voci più informali/personali del foglio (es. "cose che non mi servivano ma son fighissime", "cazzate", "sciamani") non vengono generalizzate: troppo idiosincratiche per un set di default universale.

**Problema di retrocompatibilità**: `seedDefaultCategories()` gira **solo se `getDefaultCategoryCount() == 0`** (riga 47-48), cioè solo al primissimo avvio in assoluto. Gli utenti che hanno già l'app installata non riceverebbero mai le 3 nuove categorie di default con un semplice aggiornamento. Vanno quindi retro-inserite anche per chi ha già categorie esistenti, senza duplicare nulla.

## Modifiche da implementare

### 1. `androidApp/src/main/kotlin/com/antcashmanager/android/AntCashManagerApp.kt`
- Aggiungere alla lista `expenseCategories` (righe 52-64) le 3 nuove `Category`:
  ```kotlin
  Category(name = "Regali", icon = "redeem", color = 0xFFFF8A65, type = "EXPENSE", isDefault = true),
  Category(name = "Abbonamenti", icon = "subscriptions", color = 0xFFFFD54F, type = "EXPENSE", isDefault = true),
  Category(name = "Cura personale", icon = "spa", color = 0xFFA1887F, type = "EXPENSE", isDefault = true),
  ```
  (colori scelti dalla palette già esistente `categoryColors` in `CategoriesScreen.kt`, riutilizzando valori oggi usati solo lato INCOME — nessun clash visivo perché le due liste non sono mai mostrate mescolate).
- **Retrocompatibilità per utenti esistenti**: restrutturare `seedDefaultCategories()` cosicché, quando `count > 0` (utente già esistente), invece di fare solo `return`, verifichi individualmente via `categoryRepository.getCategoryByName(name)` se "Regali", "Abbonamenti", "Cura personale" esistono già e inserisca solo quelle mancanti. Nessun nuovo flag persistito necessario: `getCategoryByName` è già nell'interfaccia `CategoryRepository` (`shared/.../domain/repository/CategoryRepository.kt` riga 9). Il primo avvio in assoluto continua a inserire tutto in blocco come oggi.

### 2. `androidApp/src/main/kotlin/com/antcashmanager/android/ui/screen/categories/CategoriesScreen.kt`
- Aggiungere `"spa" to Icons.Default.Spa` a `categoryIconMap` (riga ~106-136).
- Aggiungere `"spa" -> R.string.icon_spa` in `getIconContentDescription` (riga ~141-176).

### 3. `androidApp/src/main/kotlin/com/antcashmanager/android/util/CategoryTranslation.kt`
- Aggiungere in `categoryResId()` (righe 11-63) sia le chiavi-risorsa sia i nomi italiani grezzi salvati in DB, stesso pattern delle voci esistenti:
  ```kotlin
  "category_gifts" -> R.string.category_gifts
  "category_subscriptions" -> R.string.category_subscriptions
  "category_personal_care" -> R.string.category_personal_care
  ...
  "Regali" -> R.string.category_gifts
  "Abbonamenti" -> R.string.category_subscriptions
  "Cura personale" -> R.string.category_personal_care
  ```

### 4. Stringhe in tutte e 5 le lingue (`values`, `values-de`, `values-es`, `values-fr`, `values-it`)
Vicino ai blocchi `icon_*` (`strings.xml` righe ~85-113) e `category_*` (righe ~467-483):
- `icon_spa` (es. IT: "Cura personale/Spa")
- `category_gifts` (es. IT: "Regali")
- `category_subscriptions` (es. IT: "Abbonamenti")
- `category_personal_care` (es. IT: "Cura personale")

## Verifica

1. `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :androidApp:compileDebugKotlin :androidApp:compileDebugUnitTestKotlin` — zero errori.
2. Suite completa `:androidApp:testDebugUnitTest --rerun` e `:shared:testAndroidHostTest --rerun` — zero regressioni rispetto alla baseline nota (184 shared / 242 androidApp).
3. Non essendo disponibile un emulatore in questo ambiente, non è possibile verificare a runtime che le 3 categorie compaiano davvero su un DB con categorie già esistenti (percorso di retro-seeding) — da controllare manualmente lato utente installando un aggiornamento su un dispositivo con dati esistenti, oppure aggiungendo (se richiesto in un secondo momento) un test dedicato per `seedDefaultCategories()`.

## Ulteriori migliorie ispirate dal foglio (analisi, non implementate in questo giro)

Il foglio rivela pattern che l'app oggi non copre affatto — da valutare come feature separate, su richiesta esplicita:

1. **Budget mensile (totale e per categoria) con scostamento speso/rimanente.** È la parte più corposa e ricorrente del foglio: colonna "Budget mensile" (es. €500) confrontata con il totale speso e un "SALDO" che segnala lo sforamento. Nell'app oggi non esiste alcun concetto di budget/limite (confermato: nessun modello, use case o schermata relativa) — solo saldo entrate/uscite generico in `BalanceCard.kt`. Sarebbe la feature col maggior valore percepito preso spunto da questo foglio.
2. **Vista aggregata "Abbonamenti"/spese ricorrenti.** L'app ha già `isRecurring`/`recurrenceInterval` sul modello `Transaction` (usati solo per rigenerare automaticamente la transazione successiva e per un badge nelle liste), ma non esiste una schermata che sommi "quanto spendo al mese in abbonamenti" — la nuova categoria "Abbonamenti" appena aggiunta la rende filtrabile manualmente nei Grafici, ma un vero riepilogo dedicato (con eventuale prossima data di rinnovo) sarebbe un passo oltre.
3. **Categorie con icone mancanti per sotto-voci comuni** (assicurazioni, tasse/fisco, manutenzione casa, farmacia distinta dalla salute generica) — non richiedono nuove categorie di default (rientrano già nelle macro-categorie esistenti), ma se in futuro si vuole arricchire la scelta icone per categorie personalizzate dall'utente, sono candidati naturali.

Non incluse in questo piano: andranno proposte all'utente come possibile lavoro successivo, non implementate ora.