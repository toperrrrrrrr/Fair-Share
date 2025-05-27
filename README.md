# FairShare - Android App

FairShare is a group expense management application that helps users track shared expenses and automatically calculate balances between group members. This Android application is built using modern Android development practices and Jetpack Compose.

## Features

- 👤 User Authentication
  - Email/Password login with verification
  - Biometric authentication (Fingerprint/Face ID)
  - Google Sign-in integration
  - Profile management
  - Secure session handling

- 👥 Group Management
  - Create and join groups
  - Real-time member management
  - Multiple currency support
  - Role-based permissions
  - Activity logging

- 💰 Expense Tracking
  - Add/edit/delete expenses
  - Multiple split options (Equal, Percentage, Custom)
  - Receipt management (Coming Soon)
  - Expense categorization
  - Real-time updates

- 📊 Balance Management
  - Automatic balance computation
  - Multi-currency support
  - Simplified debt resolution
  - Settlement suggestions
  - Transaction history
  - Balance visualization

- 🔐 Security Features
  - End-to-end encryption
  - Secure data storage
  - Biometric authentication
  - API key protection
  - Input validation

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture
- **Backend**: Firebase (Authentication, Firestore, Storage)
- **Dependencies**:
  - Material3 Design Components
  - Navigation Compose
  - ViewModel Compose
  - Firebase BoM
  - Kotlin Coroutines
  - Biometric Authentication
  - Room Database (offline support)
  - WorkManager (background tasks)

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
   - Configure Storage for receipt uploads

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
├── data/           # Data layer
│   ├── local/     # Local storage
│   ├── remote/    # Remote data sources
│   └── repos/     # Repositories
├── domain/         # Business logic
│   ├── models/    # Domain models
│   ├── usecases/  # Use cases
│   └── repos/     # Repository interfaces
└── presentation/   # UI layer
    ├── auth/      # Authentication
    ├── groups/    # Group management
    ├── expenses/  # Expense tracking
    ├── profile/   # User profile
    └── common/    # Shared components
```

## Current Status

- Version: 0.2.0
- Status: Beta
- Next Release: Receipt Management Update

### Upcoming Features
- Receipt upload and OCR
- Enhanced user profiles
- Advanced group features
- Offline support improvements
- Performance optimizations

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

Please read our [Contributing Guidelines](CONTRIBUTING.md) for details.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support, please:
- Check our [Documentation](docs/README.md)
- Open an [Issue](../../issues)
- Join our [Discord Community](discord-link)

## Acknowledgments

- Firebase team for the excellent backend services
- Jetpack Compose team for the modern UI toolkit
- Our contributors and early adopters 