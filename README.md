# AntCashManager

AntCashManager e una app Android open source per la gestione delle finanze personali.

L'obiettivo del progetto e aiutare a registrare entrate/uscite, analizzare l'andamento delle spese
e mantenere il controllo dei dati in locale, con un approccio privacy-first e senza raccolta di dati sensibili.

## App Info

| Campo | Valore                         |
|---|--------------------------------|
| App name | `AntCashManager`               |
| Versione | `1.5.9`                        |
| Application ID | `com.sformica.ant_cashmanager` |
| Android namespace | `com.antcashmanager.android`   |
| Min SDK | `26`                           |
| Target SDK | `36`                           |

## Cosa Fa l'App

- Gestione transazioni di entrata e uscita
- Categorie predefinite e personalizzabili
- Grafici e insight per analisi spese/entrate
- Backup e restore dei dati
- Supporto multilingua (EN, IT, FR, DE, ES)
- UI moderna con Jetpack Compose

## Perche e Open Source

- Trasparenza sulle scelte tecniche e sulla gestione dei dati
- Facilita di audit su privacy, analytics e sicurezza
- Possibilita di contributi dalla community (bugfix, feature, documentazione)

## Privacy, Sicurezza e Dati Sensibili

Questa repository non deve contenere dati personali utente, token o credenziali private.

- I dati utente applicativi sono gestiti localmente sul dispositivo
- Gli analytics custom sono usage-only (no contenuti testuali utente)
- Non vengono inviati in analytics: note, query, email, payee, location, tags, stacktrace raw
- La policy privacy ufficiale e in:
  - `wiki/privacy-policy.html` (riferimento inglese)
  - `wiki/privacy-policy-de.html`
  - `wiki/privacy-policy-fr.html`
  - `wiki/privacy-policy-es.html`

Nota: e presente anche una copia statica della privacy policy in `docs/wiki/` per pubblicazione esterna.

## Architettura

Il progetto segue Clean Architecture con organizzazione per feature:

- Presentation (`androidApp`) -> Compose + ViewModel
- Domain (`shared/commonMain`) -> modelli, use case, interfacce
- Data (`shared/androidMain`) -> repository implementation, persistenza

Pattern principali:

- MVVM + `StateFlow`
- UseCase per feature
- Repository pattern
- Dependency Injection con Koin

## Tech Stack

| Area | Tecnologie |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Storage | Room + DataStore |
| Async | Coroutines + Flow |
| DI | Koin |
| Logging | Kermit |
| Analytics/Crash | Firebase Analytics + Crashlytics |
| Build | Gradle + Version Catalog |

## Struttura Progetto

```text
AntCashManager/
|- androidApp/                 # App Android (UI, navigazione, DI)
|- shared/                     # Modulo KMP (domain + data)
|  |- src/commonMain/          # Domain
|  |- src/androidMain/         # Data Android
|- wiki/                       # Documentazione progetto (Markdown + privacy HTML)
|- docs/wiki/                  # Copia statica wiki per pubblicazione web
|- gradle/
```

## Wiki e Documentazione (Link Verificati)

Documentazione locale (repository):

- Indice wiki: [`wiki/README.md`](wiki/README.md)
- Indice centrale: [`wiki/INDEX.md`](wiki/INDEX.md)
- Guida lettura: [`wiki/GUIDA_LETTURA.md`](wiki/GUIDA_LETTURA.md)
- Quick start wiki: [`wiki/QUICK_START.md`](wiki/QUICK_START.md)
- Linee architetturali: [`wiki/ARCHITECTURE_GUIDELINES.md`](wiki/ARCHITECTURE_GUIDELINES.md)
- Guida implementazione: [`wiki/IMPLEMENTATION_GUIDE.md`](wiki/IMPLEMENTATION_GUIDE.md)
- Guida conversione: [`wiki/CONVERSION_GUIDE.md`](wiki/CONVERSION_GUIDE.md)
- Privacy policy (EN): [`wiki/privacy-policy.html`](wiki/privacy-policy.html)

Versione statica per consultazione web:

- `docs/wiki/index.html`
- `docs/wiki/privacy-policy.html`
- `docs/wiki/privacy-policy-de.html`
- `docs/wiki/privacy-policy-fr.html`
- `docs/wiki/privacy-policy-es.html`

## Setup e Avvio

Prerequisiti minimi:

- Android Studio (stable recente)
- JDK 17+
- Android SDK 36

Build debug:

```bash
git clone https://github.com/your-username/AntCashManager.git
cd AntCashManager
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug
```

## Test

Unit test:

```bash
./gradlew :shared:test
./gradlew :androidApp:testDebugUnitTest
```

Instrumentation test (device/emulatore connesso):

```bash
./gradlew :androidApp:connectedDebugAndroidTest
```

## Checklist Rilascio Android (Open Source Safe)

- [ ] Versione (`versionCode`/`versionName`) aggiornata in `androidApp/build.gradle.kts`
- [ ] Build release ok: `./gradlew :androidApp:assembleRelease`
- [ ] Test principali eseguiti (unit + instrumentation se disponibili)
- [ ] Nessun secret nel codice/repository (`API key`, token, credenziali)
- [ ] Privacy policy aggiornata e linkata in store listing
- [ ] Screenshot/store assets aggiornati (vedi `extra/icons/`)
- [ ] Crashlytics mapping upload configurato per release
- [ ] Changelog/release notes preparate

## Analytics Usage-Only

Eventi custom consentiti (uso app):

- `transactions_filter_applied`
- `transactions_filter_cleared`
- `transaction_add_opened`
- `receipt_scan_opened`
- `transaction_form_opened`
- `transaction_form_cancelled`
- `transaction_submit_success`
- `backup_create_requested`
- `backup_file_saved`
- `backup_file_save_error`
- `restore_open_requested`
- `restore_file_selected`
- `delete_all_data_confirmed`
- `reset_preferences_confirmed`

Evento Firebase standard usato: `screen_view`.

## Come Contribuire

1. Fork del repository
2. Crea branch feature/fix
3. Mantieni allineamento con linee guida in `wiki/ARCHITECTURE_GUIDELINES.md`
4. Apri Pull Request con descrizione chiara e test eseguiti

## Licenza

Il progetto e distribuito secondo i termini del file [`LICENSE`](LICENSE).
