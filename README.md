# AntCashManager

AntCashManager e una app Android open source per la gestione delle finanze personali.

L'obiettivo del progetto e aiutare a registrare entrate/uscite, analizzare l'andamento delle spese
e mantenere il controllo dei dati in locale, con un approccio privacy-first e senza raccolta di dati sensibili.

## App Info

| Campo | Valore |
|---|---|
| App name | `AntCashManager` |
| Versione | `1.7.0` (versionCode: 19) |
| Application ID | `com.sformica.ant_cashmanager` |
| Android namespace | `com.antcashmanager.android` |
| Min SDK | `26` |
| Target SDK | `37` |

## Cosa Fa l'App

- Gestione transazioni di entrata e uscita
- Categorie predefinite e personalizzabili
- Grafici e insight per analisi spese/entrate
- Scansione scontrini con OCR (ML Kit) per creazione rapida transazioni
- Widget home screen (Glance API) per transazioni recenti e breakdown categorie
- Backup e restore dei dati
- Crittografia opzionale dei dati sensibili
- Supporto multilingua (EN, IT, FR, DE, ES)
- UI moderna con Jetpack Compose e Material 3
- Navigazione adattiva (bottom bar per phone, navigation rail per tablet/foldable)

## Perche è Open Source

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

- **Presentation** (`androidApp`) → Compose + ViewModel + Widget Glance + Navigation adattiva
- **Domain** (`shared/commonMain`) → modelli, use case, interfacce repository e servizi
- **Data** (`shared/androidMain`) → repository implementation, Room DB, DataStore, cifratura

Pattern principali:

- MVVM + `StateFlow`
- UseCase per feature (con `Result<T>` pattern)
- Repository pattern
- Dependency Injection con Koin
- Service layer (es. `ReceiptOcrService` per ML Kit)
- **Manager pattern** per business logic extraction

Moduli chiave:
- `ui/screen/` → feature screen (home, transactions, charts, categories, settings, receipt scan)
- `ui/widget/` → Glance widgets (recent transactions, category breakdown)
- `data/receipt/` → implementazione OCR con ML Kit
- `data/backup/` → backup/restore service

## Tech Stack

| Area | Tecnologie |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Widgets | Glance API |
| Navigation | Navigation Compose |
| Storage | Room + DataStore |
| Security | EncryptedSharedPreferences |
| Async | Coroutines + Flow |
| DI | Koin |
| Logging | Kermit |
| OCR | Google ML Kit Text Recognition v2 |
| Analytics/Crash | Firebase Analytics + Crashlytics |
| Build | Gradle + Version Catalog |

## Librerie

### Produzione

| Libreria | Versione | Scopo |
|---|---|---|
| Kotlin | 2.3.21 | Linguaggio principale |
| Compose BOM | 2026.06.01 | UI dichiarativa (Material 3, UI, Graphics) |
| Navigation Compose | 2.9.8 | Navigazione tra schermate |
| Lifecycle / ViewModel | 2.11.0 | Gestione stato e lifecycle awareness |
| Activity Compose | 1.13.0 | Entry point Compose |
| Glance AppWidget | 1.1.1 | Widget home screen (Glance API) |
| Room | 2.8.4 | Database locale (ORM) |
| DataStore Preferences | 1.2.1 | Preferenze reattive |
| Koin | 4.2.2 | Dependency Injection (Android + Compose) |
| Kermit | 2.1.0 | Logging multiplatform (KMP) |
| kotlinx-coroutines | 1.11.0 | Async e concorrenza |
| kotlinx-serialization-json | 1.11.0 | Serializzazione/deserializzazione JSON |
| kotlinx-datetime | 0.6.0 | Date e orari (KMP) |
| Firebase BOM | 34.17.0 | Analytics + Crashlytics |
| ML Kit Text Recognition | 16.0.1 | OCR per scansione scontrini |
| Google Fonts (Compose) | 1.11.4 | Tipografia con font Google |

### Test

| Libreria | Versione | Scopo |
|---|---|---|
| JUnit 4 | 4.13.2 | Unit test base |
| AndroidX Test JUnit | 1.1.5 | Test strumentati Android |
| kotlinx-coroutines-test | 1.11.0 | Test di coroutine e Flow |
| MockK | 1.14.11 | Mock/stub/verify (sostituisce Mockito) |
| Compose UI Test JUnit4 | 1.11.4 | Test UI Compose |

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

- **Guida AI Agents**: [`AGENTS.md`](AGENTS.md) – guida tecnica per AI coding agents
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

- Android Studio (stable recente, consigliato Ladybug o superiore)
- JDK 17+
- Android SDK 37

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
- `transaction_deleted`
- `transaction_shared`
- `backup_create_requested`
- `backup_file_saved`
- `backup_file_save_error`
- `restore_open_requested`
- `restore_file_selected`
- `delete_all_data_confirmed`
- `reset_preferences_confirmed`
- `delete_suggestions_confirmed`
- `category_created`
- `category_deleted`
- `chart_date_filter_changed`
- `chart_custom_date_range_set`
- `chart_shared`
- `chart_help_opened`
- `home_top_cards_reordered`
- `home_date_filter_changed`
- `home_search_opened`
- `home_help_opened`
- `receipt_scan_captured`
- `receipt_scan_saved`
- `data_encryption_toggled`
- `suggestions_toggled`
- `home_quick_insights_toggled`
- `date_format_changed`
- `transaction_display_type_changed`
- `theme_changed`
- `language_changed`
- `currency_format_changed`
- `feedback_email_sent`
- `tutorial_replay_requested`
- `decimal_digits_changed`
- `decimal_separator_changed`
- `thousands_separator_changed`
- `meal_voucher_value_changed`
- `show_charts_section_toggled`
- `charts_zoom_toggled`
- `show_payment_breakdown_toggled`
- `show_transaction_notes_toggled`
- `widget_background_color_changed`
- `widget_opacity_changed`
- `widget_recent_transactions_opened`
- `widget_category_breakdown_opened`
- `settings_help_opened`
- `settings_privacy_policy_opened`
- `settings_third_party_libraries_opened`
- `settings_high_contrast_toggled`
- `settings_large_text_toggled`
- `settings_reduce_motion_toggled`
- `transactions_filter_opened`
- `transactions_help_opened`
- `categories_help_opened`
- `receipt_scan_retry`
- `transaction_recurring_toggled`
- `home_transaction_detail_opened`

Evento Firebase standard usato: `screen_view`.

## Come Contribuire

1. Fork del repository
2. Crea branch feature/fix
3. Mantieni allineamento con linee guida in `wiki/ARCHITECTURE_GUIDELINES.md`
4. Apri Pull Request con descrizione chiara e test eseguiti

## Licenza

Il progetto e distribuito secondo i termini del file [`LICENSE`](LICENSE).
