# 🐜 AntCashManager

**AntCashManager** is a personal finance management app for Android, built with **Kotlin Multiplatform (KMP)** and **Jetpack Compose**. Track your income and expenses like a diligent ant — one step at a time!

---

## ✨ Features

### 💰 Transaction Management
- Add, edit, and delete income & expense transactions
- Categorise every transaction with custom or default categories
- Add optional notes, payee, location, and tags to each entry
- Mark transactions as recurring

### 📊 Charts & Reports
- Interactive **pie chart** for expense breakdown by category
- **Bar chart** with monthly income vs expense overview
- Flexible date-range presets (7 days, 1 / 3 / 6 / 12 months, all time) and custom date pickers
- Toggle chart visibility from Settings

### 🗂️ Categories
- Pre-seeded **10 expense** and **6 income** default categories with Material Icons
- Expense / Income tab-based view
- Add custom categories with name, icon, and colour
- Default categories are protected from deletion

### ⚙️ Settings
- **Appearance** — Light / Dark / System theme
- **Language** — English, Italiano, Français, Deutsch, Español (runtime switch, no restart)
- **Display** — Show or hide Charts tab in navigation
- **Accessibility** — High Contrast mode, Large Text, Reduce Motion
- **Data Management** — Delete all data, Backup & Restore (coming soon)
- **Support** — Send Feedback, Privacy Policy
- **About** — App version

### ♿ Accessibility
- High-contrast colour scheme for better readability
- Scalable typography (1.25× increase) for visually impaired users
- Reduce-motion toggle for animation-sensitive users

### 🐜 Ant Mascot
- Friendly vector ant mascot shown in empty states across all screens
- Custom adaptive launcher icon featuring the ant with a gold coin

---

## 🏗️ Architecture

```
AntCashManager/
├── shared/                    # Kotlin Multiplatform module
│   ├── commonMain/            # Domain models, repository interfaces, use cases
│   ├── androidMain/           # Room database, DAOs, entities, mappers, DataStore
│   └── test/                  # Unit tests for use cases
├── androidApp/                # Android application module
│   ├── navigation/            # NavGraph, BottomNavItem
│   ├── ui/                    # Compose screens (Home, Charts, Transactions, Categories, Settings)
│   │   ├── theme/             # Color, Typography, Theme (with accessibility support)
│   │   └── components/        # Reusable UI components (AppCard)
│   └── test/                  # ViewModel unit tests
└── gradle/                    # Version catalog (libs.versions.toml)
```

### Key Patterns
| Pattern | Implementation |
|---|---|
| **Clean Architecture** | Domain → Data → Presentation layers |
| **MVVM** | ViewModels + StateFlow for reactive UI |
| **Repository** | Interface in `commonMain`, implementation in `androidMain` |
| **Use Cases** | Single-responsibility interactors for each operation |
| **Room + AutoMigration** | Version 3 schema with `@ColumnInfo(defaultValue = ...)` |
| **DataStore** | Preferences for theme, language, and accessibility settings |
| **Dependency Injection** | Manual DI via `Application` class (Hilt-ready) |

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin 2.1+ |
| UI | Jetpack Compose + Material 3 |
| Navigation | Compose Navigation |
| Database | Room (KSP) with AutoMigration |
| Preferences | DataStore |
| Logging | Kermit (multiplatform) |
| Build | Gradle 8.x with Version Catalog |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |

---

## 🌍 Localisation

Full translation support for 5 languages:

| Language | Code | Status |
|---|---|---|
| 🇬🇧 English | `en` | ✅ Complete |
| 🇮🇹 Italiano | `it` | ✅ Complete |
| 🇫🇷 Français | `fr` | ✅ Complete |
| 🇩🇪 Deutsch | `de` | ✅ Complete |
| 🇪🇸 Español | `es` | ✅ Complete |

Language can be switched at runtime without restarting the app.

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or later
- JDK 17+
- Android SDK 35

### Build & Run

```bash
# Clone the repository
git clone https://github.com/your-username/AntCashManager.git
cd AntCashManager

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew :shared:test :androidApp:testDebugUnitTest

# Install on connected device
./gradlew installDebug
```

---

## 🧪 Testing

- **Unit tests** for ViewModels and Use Cases using JUnit + Coroutines Test
- **Fake repositories** for isolated testing without database
- Run all tests: `./gradlew test`

---

## 📁 Default Categories

### Expense
| Icon | Category |
|---|---|
| 🏠 | Casa (Home) |
| 🚗 | Trasporti (Transport) |
| 🍕 | Cibo (Food) |
| 📄 | Bollette (Bills) |
| 🍽️ | Pranzi/Cene fuori (Dining Out) |
| 🎭 | Divertimento (Entertainment) |
| 🏥 | Salute (Health) |
| 🛍️ | Shopping |
| 🎓 | Istruzione (Education) |
| ••• | Altro (Other) |

### Income
| Icon | Category |
|---|---|
| 💳 | Stipendio (Salary) |
| 💰 | Paghetta (Allowance) |
| 💱 | Rimborso (Refund) |
| 📈 | Investimenti (Investments) |
| 💼 | Freelance |
| ••• | Altro (Other) |

---

## 📄 License

This project is licensed under the terms specified in the [LICENSE](LICENSE) file.

---

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

<p align="center">
  Built with ❤️ and 🐜 diligence
</p>
