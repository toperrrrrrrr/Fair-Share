# FairShare - Android App

FairShare is a group expense management application that helps users track shared expenses and automatically calculate balances between group members. This Android application is built using modern Android development practices and Jetpack Compose.

## Features

- 👤 User Authentication
  - Email/Password login
  - User registration
  - Password recovery
  - Profile management

- 👥 Group Management
  - Create and join groups
  - Manage group members
  - Multiple currency support
  - Real-time updates

- 💰 Expense Tracking
  - Add/edit/delete expenses
  - Split expenses equally or custom
  - Receipt upload support
  - Expense categorization

- 📊 Balance Calculation
  - Automatic balance computation
  - Simplified debt resolution
  - Transaction history
  - Settlement tracking

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture
- **Backend**: Firebase (Authentication, Firestore, Storage)
- **Dependencies**:
  - Material3 Design Components
  - Navigation Compose
  - ViewModel Compose
  - Firebase BoM
  - Kotlin Coroutines

## Project Setup

1. Clone the repository:
```bash
git clone https://github.com/yourusername/fairshare-android.git
```

2. Open the project in Android Studio

3. Configure Firebase:
   - Create a new Firebase project
   - Add your `google-services.json` to the app directory
   - Enable Authentication and Firestore

4. Build and run the project

## Development Environment

- Android Studio Hedgehog | 2023.1.1
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Kotlin version: 1.9.22
- Compose version: 1.5.8

## Architecture

The project follows Clean Architecture principles with the following layers:

```
app/
├── data/           # Data layer (repositories, data sources)
├── domain/         # Business logic and entities
└── presentation/   # UI layer (screens, viewmodels)
    ├── auth/       # Authentication screens
    ├── groups/     # Group management
    ├── expenses/   # Expense tracking
    └── common/     # Shared components
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details. 