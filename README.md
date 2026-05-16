# AntCashManager

AntCashManager is an Android personal finance app built with Kotlin Multiplatform and Jetpack Compose.
It helps you track income and expenses with a clean UI, multi-language support, and privacy-first local storage.

## App Info

| Field | Value |
|---|---|
| App name | `AntCashManager` |
| Version | `1.4.6` |
| Package name (`applicationId`) | `com.sformica.ant_cashmanager` |
| Android namespace | `com.antcashmanager.android` |
| Min SDK | `26` |
| Target SDK | `36` |

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Localization](#localization)
- [Getting Started](#getting-started)
- [Testing](#testing)
- [Analytics](#analytics)
- [License](#license)
- [Contributing](#contributing)

## Features

### Transactions
- Add, edit, and delete income/expense transactions
- Optional details: notes, payee, location, tags, recurring flag
- Fast filtering and date-range selection

### Charts and Insights
- Expense breakdown by category (pie chart)
- Income vs expense trend (bar chart)
- Configurable chart visibility from Settings

### Categories
- Built-in default categories for expense and income
- Custom categories with icon and color
- Protection against deletion of default categories

### Settings
- Appearance: Light, Dark, System
- Language: English, Italian, French, German, Spanish
- Accessibility: high contrast, larger text, reduced motion
- Data management: backup, restore, delete all data, reset preferences
- Support: feedback and privacy policy

## Architecture

The app follows Clean Architecture with a feature-oriented structure.

- Presentation layer (`androidApp`): Compose screens + ViewModels
- Domain layer (`shared/commonMain`): use cases, models, interfaces
- Data layer (`shared/androidMain`): repository implementations, Room, DataStore

### Patterns in use

| Pattern | Implementation |
|---|---|
| Clean Architecture | Presentation -> Domain -> Data |
| MVVM | ViewModel + immutable UI State (`StateFlow`) |
| Use Case per feature | Business logic isolated in dedicated use cases |
| Repository pattern | Domain interfaces + data implementations |
| Dependency Injection | Koin modules (`dataModule`, `useCaseModule`, `presentationModule`) |

## Tech Stack

| Area | Tools |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Local DB | Room |
| Preferences | DataStore |
| Async | Coroutines + Flow |
| Logging | Kermit |
| DI | Koin |
| Build | Gradle + Version Catalog |

## Project Structure

```text
AntCashManager/
|- androidApp/                 # Android app module (UI, navigation, DI)
|- shared/                     # KMP shared module (domain + data)
|  |- src/commonMain/          # Domain layer
|  |- src/androidMain/         # Android data layer
|- wiki/                       # Project guidelines and docs
|- gradle/                     # Version catalog and wrapper config
```

## Localization

Supported languages:

- English (`en`)
- Italian (`it`)
- French (`fr`)
- German (`de`)
- Spanish (`es`)

Language switching is available at runtime.

## Getting Started

### Prerequisites
- Android Studio (recent stable version)
- JDK 17+
- Android SDK 36

### Build and Run

```bash
git clone https://github.com/your-username/AntCashManager.git
cd AntCashManager
./gradlew assembleDebug
./gradlew installDebug
```

## Testing

```bash
./gradlew :shared:test
./gradlew :androidApp:testDebugUnitTest
```

For instrumentation tests (connected device/emulator):

```bash
./gradlew :androidApp:connectedDebugAndroidTest
```

## Analytics

Firebase Analytics is integrated for core navigation and feature events.

Examples:
- `screen_view`
- `transaction_submit_success`
- `transactions_filter_applied`
- `backup_create_requested`
- `backup_file_saved`

## License

This project is distributed under the terms in [LICENSE](LICENSE).

## Contributing

1. Fork the repository
2. Create a branch (`git checkout -b feature/your-feature`)
3. Commit your changes
4. Push and open a Pull Request
