# SilentSOS - Personal Safety App

<div align="center">

![SilentSOS Logo](docs/logo.png)

**A discreet emergency alert system designed for personal safety**

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

</div>

---

## 📱 Overview

SilentSOS is a production-ready Android application that provides discreet emergency alerting capabilities. The app disguises itself as a calculator while offering powerful safety features including:

- 🔐 **Discreet Access**: Calculator disguise with secret PIN
- 🚨 **Emergency Alerts**: One-tap SOS with countdown
- 📍 **Live Location Tracking**: Real-time GPS updates to emergency contacts
- 🎙️ **Audio Recording**: Automatic evidence collection
- 📱 **SMS Notifications**: Instant alerts to trusted contacts
- 🔄 **Offline Support**: Works even without internet connection
- 🛡️ **Duress Mode**: Silent alert when under threat

---

## 🏗️ Architecture

### Clean Architecture + MVVM

```
app/
├── data/                    # Data Layer
│   ├── local/              # Local data sources (DataStore)
│   ├── remote/             # Remote data sources (Firebase)
│   └── repository/         # Repository implementations
├── domain/                  # Domain Layer
│   ├── model/              # Business models
│   ├── repository/         # Repository interfaces
│   └── usecase/            # Business logic use cases
├── presentation/            # Presentation Layer
│   ├── ui/                 # Compose UI screens
│   └── viewmodel/          # ViewModels
├── di/                      # Dependency Injection (Hilt)
├── service/                 # Background services
├── worker/                  # WorkManager workers
└── utils/                   # Utilities and helpers
```

### Key Technologies

- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt (Dagger)
- **Backend**: Firebase (Auth, Firestore, Storage, FCM)
- **Async**: Kotlin Coroutines + Flow
- **Location**: FusedLocationProviderClient
- **Background**: Foreground Services + WorkManager
- **Local Storage**: DataStore (Preferences)

---

## 🚀 Features

### 1. Authentication
- **Phone OTP**: Secure Firebase phone authentication
- **Session Persistence**: Stay logged in across app restarts
- **Auto-logout**: Configurable session timeout

### 2. Disguise Mode
- **Calculator Interface**: Fully functional calculator
- **Secret PIN**: Access hidden dashboard (default: 1234)
- **Duress PIN**: Trigger silent SOS (default: 0000)
- **Decoy Screens**: Fake "system deactivated" screens

### 3. SOS System
- **Manual Trigger**: Big red button on dashboard
- **Countdown Buffer**: 10-second cancellation window
- **Multiple Triggers**: Power button, shake, voice (configurable)
- **Automatic Actions**:
  - Send SMS to all emergency contacts
  - Start live location tracking
  - Begin audio recording
  - Create Firestore event log
  - Send FCM push notifications

### 4. Location Tracking
- **Real-time Updates**: Every 10-15 seconds
- **High Accuracy**: GPS + Network positioning
- **Battery Optimized**: Balanced power consumption
- **Offline Queue**: Syncs when connection restored
- **Foreground Service**: Continues even when app closed

### 5. Emergency Contacts
- **Unlimited Contacts**: Add family, friends, authorities
- **Priority Levels**: High, Medium, Low
- **Verification**: Confirm contact phone numbers
- **SMS Alerts**: Automatic emergency messages with location

### 6. Audio Recording
- **Automatic Start**: Begins with SOS trigger
- **High Quality**: AAC encoding, 44.1kHz
- **Cloud Upload**: Stored in Firebase Storage
- **Auto-delete**: Configurable retention period
- **Encrypted**: Secure storage and transmission

### 7. Incident History
- **Complete Log**: All SOS events with timestamps
- **Location History**: Track movement during incident
- **Audio Playback**: Review recorded evidence
- **Export Data**: Download for authorities

### 8. Settings
- **Trigger Configuration**: Customize SOS triggers
- **PIN Management**: Change secret and duress PINs
- **Delay Settings**: Adjust countdown duration
- **Privacy Controls**: Auto-delete recordings
- **Disguise Selection**: Choose app appearance

---

## 📋 Requirements

### Minimum Requirements
- Android 8.0 (API 26) or higher
- 100 MB free storage
- GPS capability
- Internet connection (for initial setup)
- Phone number for authentication

### Recommended
- Android 12.0 (API 31) or higher
- 500 MB free storage
- 4G/5G or WiFi connection
- Biometric authentication support

### Permissions Required
- **Location**: GPS tracking during SOS
- **SMS**: Send emergency alerts
- **Audio**: Record evidence
- **Phone State**: Device identification
- **Foreground Service**: Background operation
- **Notifications**: Alert display
- **Boot Completed**: Auto-start after reboot

---

## 🛠️ Setup & Installation

### For Users

1. **Download**: Get from Google Play Store (coming soon)
2. **Install**: Follow standard Android installation
3. **Setup**:
   - Enter phone number
   - Verify OTP code
   - Add emergency contacts
   - Configure PINs
   - Grant permissions
4. **Test**: Trigger test SOS to verify setup

### For Developers

#### Prerequisites
```bash
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 35
- Firebase account
```

#### Clone Repository
```bash
git clone https://github.com/yourorg/silentsos.git
cd silentsos
```

#### Firebase Setup
1. Create Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add Android app with package name: `com.silentsos.app`
3. Download `google-services.json`
4. Place in `app/` directory
5. Enable Authentication (Phone), Firestore, Storage, FCM

#### Deploy Security Rules

**Option 1: Using Firebase CLI** (if installed)
```bash
firebase deploy --only firestore:rules,storage
```

**Option 2: Manual Deployment** (no CLI required)
See [MANUAL_DEPLOYMENT_GUIDE.md](MANUAL_DEPLOYMENT_GUIDE.md) for step-by-step instructions to deploy rules through Firebase Console.

#### Build & Run
```bash
./gradlew assembleDebug
./gradlew installDebug
```

Or use Android Studio:
- Open project
- Sync Gradle
- Run on device/emulator

---

## 🔒 Security

### Data Protection
- **Encryption**: All data encrypted in transit (TLS) and at rest
- **Firebase Rules**: Strict access control (see `firestore.rules`)
- **User Isolation**: Users can only access their own data
- **Audit Trail**: Immutable SOS event logs
- **Secure Storage**: PINs stored in encrypted DataStore

### Privacy
- **Minimal Data**: Only collect what's necessary
- **User Control**: Delete data anytime
- **No Tracking**: No analytics without consent
- **Local First**: Settings stored locally
- **Transparent**: Open source code

### Best Practices
- Change default PINs immediately
- Use strong, unique PINs
- Verify emergency contacts
- Test SOS regularly
- Keep app updated
- Review permissions periodically

---

## 🧪 Testing

### Manual Testing
See `PRODUCTION_SETUP.md` for comprehensive testing checklist.

### Automated Testing
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# UI tests
./gradlew connectedCheck
```

### Test Coverage
- Repository layer: Unit tests
- Use cases: Unit tests
- ViewModels: Unit tests
- UI: Instrumented tests
- Services: Integration tests

---

## 📊 Performance

### Benchmarks
- **App Size**: ~15 MB (release APK)
- **Memory**: ~50 MB average usage
- **Battery**: <5% per hour during active SOS
- **Network**: ~1 KB per location update
- **Storage**: ~1 MB per hour of audio

### Optimization
- Firestore offline persistence
- Efficient location updates
- Compressed audio encoding
- Lazy loading UI
- ProGuard/R8 optimization

---

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Development Workflow
1. Fork repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

### Code Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable names
- Add comments for complex logic
- Write unit tests for new features

---

## 📝 License

This project is licensed under the MIT License - see [LICENSE](LICENSE) file for details.

---

## ⚠️ Disclaimer

**SilentSOS is not a replacement for emergency services (911/112/999).**

- Always call emergency services directly when possible
- This app is a supplementary safety tool
- Functionality depends on device, network, and GPS availability
- No guarantee of message delivery or location accuracy
- Use at your own risk

---

## 📞 Support

### Documentation
- [Production Setup Guide](PRODUCTION_SETUP.md)
- [API Documentation](docs/API.md)
- [FAQ](docs/FAQ.md)

### Contact
- **Email**: support@silentsos.app
- **Website**: https://silentsos.app
- **Issues**: [GitHub Issues](https://github.com/yourorg/silentsos/issues)

### Community
- **Discord**: [Join our server](https://discord.gg/silentsos)
- **Twitter**: [@SilentSOSApp](https://twitter.com/silentsosapp)

---

## 🙏 Acknowledgments

- Firebase team for excellent backend services
- Jetpack Compose team for modern UI toolkit
- Android community for libraries and support
- Beta testers for valuable feedback

---

## 🗺️ Roadmap

### v1.1 (Q2 2024)
- [ ] Biometric authentication
- [ ] Video recording option
- [ ] Multi-language support
- [ ] Wear OS companion app

### v1.2 (Q3 2024)
- [ ] Group safety features
- [ ] Check-in reminders
- [ ] Safe zone alerts
- [ ] Emergency services integration

### v2.0 (Q4 2024)
- [ ] AI threat detection
- [ ] Smart home integration
- [ ] Vehicle integration
- [ ] Professional monitoring service

---

<div align="center">

**Made with ❤️ for personal safety**

[Website](https://silentsos.app) • [Documentation](docs/) • [Support](mailto:support@silentsos.app)

</div>
