# Changelog

All notable changes to the FairShare project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Authentication system enhancements
  - Improved Firebase Auth integration
  - Robust error handling
  - Anonymous authentication for development
  - Proper type safety for FirebaseUser
- Group management improvements
  - Enhanced member role system (ADMIN/MEMBER)
  - Proper group repository implementation
  - Firebase Firestore integration
  - Real-time group updates
- Data model refinements
  - Consolidated FirebaseUser model
  - Enhanced Group and GroupMember models
  - Added Settlement model
  - Improved currency handling
- Repository layer improvements
  - Implemented GroupRepository interface
  - Added FirebaseRepository with Firestore
  - Enhanced error handling
  - Added proper null safety
- Navigation enhancements
  - Implemented Screen sealed class
  - Added type-safe route parameters
  - Improved navigation flow
- Biometric authentication support
  - Fingerprint and Face ID integration
  - Secure key storage using Android Keystore
  - Error handling and fallback mechanisms
- Email verification system
  - Automatic verification on signup
  - Email verification check before login
  - Resend verification functionality
  - User-friendly verification UI
- Balance summary features
  - Overall balance view
  - Individual balance cards
  - "Who Owes Who" breakdown
  - Color-coded amounts
  - Settlement suggestions
- Firebase integration
  - Real-time data synchronization
  - Automatic balance calculations
  - Push notification system
  - Offline data persistence
- Settlement and payment features
  - Settlement suggestions screen
  - Payment recording UI
  - Payment history screen
  - Debt simplification visualization
- Enhanced UI/UX
  - Material 3 design implementation
  - Loading states for all operations
  - Error boundaries and states
  - Form validation feedback
  - Proper content descriptions
  - Error logging and reporting

### Changed
- Improved authentication flow
  - Enhanced error handling
  - Better loading states
  - Proper Firebase integration
  - Type-safe auth results
- Updated navigation structure
  - Cleaner navigation setup
  - Better state preservation
  - Enhanced error recovery
- Enhanced group management
  - Improved member handling
  - Better role management
  - Enhanced currency support

### Fixed
- Type mismatch in GoogleAuthManager
- Duplicate GroupMember definition
- AuthViewModel exhaustive when expressions
- Proper suspend function implementation
- Firebase import conflicts
- Currency formatting issues

## [0.1.0] - 2024-03-XX
### Added
- Initial release
- Basic authentication (Login, Register)
- Google Sign-in integration
- Basic group management
- Expense creation and editing
- Basic expense listing
- Material 3 design
- Basic navigation structure

## Notes
- Dates follow YYYY-MM-DD format
- Unreleased changes are collected at the top
- Changes are grouped by type (Added, Changed, Fixed, etc.)
- Breaking changes are marked with "BREAKING CHANGE:" 