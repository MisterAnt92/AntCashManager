# Architettura Multi-Step per Aggiunta Transazioni

## Panoramica
Il nuovo sistema di aggiunta transazioni è stato completamente refactorizzato in un flusso multi-step dedicato nel package `add_transaction`.

## Struttura dei File

### 1. `AddTransactionState.kt`
Contiene le definizioni dello stato per il flusso multi-step:
- **`AddTransactionStep`**: Enum che rappresenta i 4 step del wizard:
  - `CATEGORY_SELECTION`: Selezione della categoria con icone
  - `TYPE_SELECTION`: Scelta tra Entrata/Uscita
  - `DETAILS`: Inserimento dei dettagli (titolo, importo, date, note, etc)
  - `CONFIRMATION`: Riepilogo e conferma prima del salvataggio
  
- **`AddTransactionState`**: Data class che gestisce lo stato dell'intera form
  - Traccia il step corrente
  - Contiene i dati selezionati (categoria, tipo)
  - Contiene i campi della transazione
  - Contiene la lista delle categorie disponibili e stati di caricamento

### 2. `AddTransactionViewModel.kt`
ViewModel che gestisce la logica del flusso:
- **Event-driven**: Riceve `AddTransactionEvent` per ogni azione dell'utente
- **Validazione**: Valida ogni step prima di permettere il passaggio al successivo
- **Persistenza**: Salva la transazione nel database quando completata
- **Errori**: Gestisce e comunica errori all'UI

#### Event disponibili:
- `SelectCategory(category)`: Selezione categoria
- `SelectType(type)`: Selezione tipo transazione
- `UpdateTitle(title)`: Aggiornamento titolo
- `UpdateAmount(amount)`: Aggiornamento importo
- `UpdateNotes(notes)`: Aggiornamento note
- `UpdatePayee(payee)`: Aggiornamento beneficiario
- `UpdateLocation(location)`: Aggiornamento luogo
- `UpdateTags(tags)`: Aggiornamento tag
- `UpdateTimestamp(timestamp)`: Aggiornamento data
- `SetRecurring(isRecurring)`: Impostazione ricorrenza
- `UpdateRecurrenceInterval(interval)`: Intervallo ricorrenza
- `NextStep`: Procedi al prossimo step
- `PreviousStep`: Torna al step precedente
- `Submit`: Salva la transazione
- `Cancel`: Cancella e torna indietro

### 3. `AddTransactionScreen.kt`
Screen principale che gestisce la navigazione tra gli step:
- Dirige al composable step corretto in base a `state.currentStep`
- Passa gli event handler appropriati ad ogni step

#### Step 1: CategorySelectionStep
- Grid 2x2 di categorie con icone
- Pulsanti "Annulla" e "Avanti"
- Validazione: Categoria obbligatoria

#### Step 2: TypeSelectionStep
- Radio button per Entrata/Uscita
- Mostra la categoria selezionata nello step precedente
- Pulsanti "Indietro" e "Avanti"
- Validazione: Tipo obbligatorio

#### Step 3: DetailsStep
- Campi di input: Titolo, Importo, Data, Note, Beneficiario, Luogo
- DatePickerDialog per selezione data
- Mostra categoria e tipo dei step precedenti
- Pulsanti "Indietro" e "Avanti"
- Validazione: Titolo e Importo obbligatori

#### Step 4: ConfirmationStep
- Riepilogo di tutti i dati inseriti
- Mostra solo i campi valorizzati (nascondi null)
- Pulsanti "Indietro" e "Salva"
- Al click "Salva" viene chiamato il submit

## Flusso di Navigazione

```
TransactionsScreen
         |
         v (click FAB "Aggiungi")
  add_transaction route
         |
         v
   AddTransactionScreen
         |
    +----+----+----+----+
    |    |    |    |    |
    v    v    v    v    v
  Cat  Type  Det  Conf  (navigazione tra step)
  Sel  Sel   ails irm
```

## Integrazione con la Navigazione

Nel `NavGraph.kt`:
```kotlin
composable("add_transaction") {
    AddTransactionScreen(
        transactionRepository = transactionRepository,
        categoryRepository = categoryRepository,
        onNavigateBack = { navController.popBackStack() },
        onTransactionAdded = { navController.popBackStack() },
    )
}
```

## Vantaggi dell'Architettura

1. **Separazione dei Concern**: Ogni step è isolato in un composable separato
2. **Riutilizzabilità**: Il ViewModel può essere riusato in altri contesti
3. **Testabilità**: Logic separata da UI, facile da testare
4. **UX Migliorata**: Flusso guidato passo passo, meno confusione
5. **Manutenibilità**: Codice organizzato e ben strutturato
6. **Scalabilità**: Facile aggiungere nuovi step o validazioni

## Estensioni Future

Per aggiungere nuovi campi:
1. Aggiungi il campo a `AddTransactionState`
2. Aggiungi un event in `AddTransactionEvent`
3. Aggiungi l'handler nel ViewModel
4. Aggiorna il composable step appropriato con il nuovo campo
5. Aggiorna la validazione se necessario

## Note Implementative

- **Validazione Progressiva**: Ogni step viene validato prima di procedere
- **State Reset**: Lo stato si resetta quando la transazione è completata
- **Error Handling**: Gli errori vengono gestiti e comunicati all'utente
- **Coroutines**: Tutte le operazioni async usano coroutine del viewModelScope

