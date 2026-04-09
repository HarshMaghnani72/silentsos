# SilentSOS - Production Setup Guide

## Overview
This guide covers the complete setup process for deploying SilentSOS to production.

---

## 1. Firebase Setup

### 1.1 Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add Project"
3. Enter project name: `silentsos-prod`
4. Enable Google Analytics (recommended)
5. Create project

### 1.2 Add Android App
1. In Firebase Console, click "Add app" → Android
2. Enter package name: `com.silentsos.app`
3. Download `google-services.json`
4. Place it in `app/` directory
5. Follow Firebase SDK setup instructions (already configured in this project)

### 1.3 Enable Authentication
1. Go to **Authentication** → **Sign-in method**
2. Enable **Phone** authentication
3. Configure phone number verification settings
4. Add test phone numbers if needed (for development)

### 1.4 Setup Firestore Database
1. Go to **Firestore Database** → **Create database**
2. Choose **Production mode** (we'll add rules next)
3. Select region closest to your users
4. Deploy security rules:
   ```bash
   firebase deploy --only firestore:rules
   ```
5. Or manually copy rules from `firestore.rules` to Firebase Console

### 1.5 Setup Cloud Storage
1. Go to **Storage** → **Get started**
2. Choose **Production mode**
3. Select same region as Firestore
4. Deploy storage rules:
   ```bash
   firebase deploy --only storage
   ```
5. Or manually copy rules from `storage.rules` to Firebase Console

### 1.6 Setup Cloud Messaging (FCM)
1. Go to **Cloud Messaging**
2. Note your **Server Key** (for backend notifications)
3. FCM is automatically configured in the app

---

## 2. Security Rules Deployment

### Firestore Rules
The `firestore.rules` file contains comprehensive security rules:
- Users can only access their own data
- Contacts are private to each user
- SOS events have audit trail protection
- Location updates are validated and immutable

### Storage Rules
The `storage.rules` file protects audio recordings:
- Users can only upload/access their own recordings
- File size limits enforced (50MB for audio)
- Content type validation (audio files only)

### Deploy Rules
```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login to Firebase
firebase login

# Initialize Firebase in project
firebase init

# Deploy rules
firebase deploy --only firestore:rules,storage
```

---

## 3. App Configuration

### 3.1 Update Build Configuration
In `app/build.gradle.kts`:
```kotlin
android {
    defaultConfig {
        applicationId = "com.silentsos.app"
        versionCode = 1
        versionName = "1.0.0"
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Add signing config
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### 3.2 Generate Signing Key
```bash
keytool -genkey -v -keystore silentsos-release.keystore \
  -alias silentsos -keyalg RSA -keysize 2048 -validity 10000
```

### 3.3 Configure Signing
Create `keystore.properties`:
```properties
storePassword=YOUR_STORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=silentsos
storeFile=../silentsos-release.keystore
```

Add to `app/build.gradle.kts`:
```kotlin
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

android {
    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }
}
```

---

## 4. Permissions & Privacy

### 4.1 Required Permissions
All permissions are declared in `AndroidManifest.xml`:
- **Location**: For SOS location tracking
- **SMS**: For emergency contact notifications
- **Audio**: For evidence recording
- **Phone State**: For device identification
- **Foreground Service**: For background SOS tracking
- **Notifications**: For SOS alerts

### 4.2 Runtime Permission Handling
The app requests permissions at appropriate times:
- Location: When user first accesses dashboard
- SMS: When adding first emergency contact
- Audio: When SOS is triggered
- Notifications: On app first launch

### 4.3 Privacy Policy
Create a privacy policy covering:
- Data collection (location, audio, contacts)
- Data storage (Firebase, encrypted)
- Data sharing (emergency contacts only)
- Data retention (configurable auto-delete)
- User rights (access, deletion, export)

Host privacy policy at: `https://yourwebsite.com/privacy`

Update in Google Play Console.

---

## 5. Testing Checklist

### 5.1 Authentication Testing
- [ ] Phone OTP sends successfully
- [ ] OTP verification works
- [ ] Session persists after app restart
- [ ] Sign out works correctly
- [ ] Invalid OTP shows error

### 5.2 SOS Functionality Testing
- [ ] Manual SOS trigger works
- [ ] Countdown can be cancelled
- [ ] Location tracking starts
- [ ] Audio recording starts
- [ ] SMS sent to all contacts
- [ ] Firestore event created
- [ ] Location updates saved
- [ ] Audio uploaded to Storage
- [ ] SOS cancellation works
- [ ] Contacts notified of cancellation

### 5.3 Background Service Testing
- [ ] Service survives app closure
- [ ] Service survives device restart
- [ ] Location updates continue in background
- [ ] Audio recording continues
- [ ] Low battery doesn't stop service
- [ ] Network loss queues updates
- [ ] Network restore syncs data

### 5.4 Error Handling Testing
- [ ] No internet shows error
- [ ] GPS disabled shows error
- [ ] Permission denied shows error
- [ ] Retry mechanism works
- [ ] WorkManager retries failed operations

### 5.5 Security Testing
- [ ] Users can't access other users' data
- [ ] Firestore rules block unauthorized access
- [ ] Storage rules block unauthorized uploads
- [ ] PINs are stored securely
- [ ] Session tokens expire correctly

---

## 6. Performance Optimization

### 6.1 Battery Optimization
- Location updates: 10-15 second intervals
- Use `PRIORITY_BALANCED_POWER_ACCURACY` when possible
- Stop services when SOS cancelled
- Use WorkManager for background tasks

### 6.2 Network Optimization
- Firestore offline persistence enabled
- Batch location updates when possible
- Compress audio recordings
- Use FCM for push notifications (low bandwidth)

### 6.3 Memory Optimization
- Use Flow for reactive data
- Cancel coroutines properly
- Release MediaRecorder resources
- Clear notification listeners

---

## 7. Deployment

### 7.1 Build Release APK
```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

### 7.2 Build App Bundle (Recommended)
```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

### 7.3 Google Play Console Setup
1. Create app in Play Console
2. Upload app bundle
3. Fill out store listing:
   - Title: "SilentSOS - Personal Safety"
   - Short description: "Discreet emergency alert system"
   - Full description: (see PLAY_STORE_LISTING.md)
   - Screenshots: (prepare 8 screenshots)
   - Feature graphic: 1024x500px
4. Set content rating
5. Set target audience
6. Add privacy policy URL
7. Submit for review

---

## 8. Monitoring & Analytics

### 8.1 Firebase Crashlytics
Add to `app/build.gradle.kts`:
```kotlin
plugins {
    id("com.google.firebase.crashlytics")
}

dependencies {
    implementation("com.google.firebase:firebase-crashlytics-ktx")
}
```

### 8.2 Firebase Analytics
Already included via Firebase BOM. Track key events:
- SOS triggered
- SOS cancelled
- Contact added
- Authentication completed

### 8.3 Performance Monitoring
```kotlin
dependencies {
    implementation("com.google.firebase:firebase-perf-ktx")
}
```

---

## 9. Maintenance

### 9.1 Regular Updates
- Monitor crash reports weekly
- Update dependencies monthly
- Review security rules quarterly
- Test on new Android versions

### 9.2 User Support
- Monitor Play Store reviews
- Respond to user feedback
- Maintain FAQ/Help documentation
- Provide in-app support contact

### 9.3 Data Management
- Monitor Firestore usage
- Monitor Storage usage
- Implement data retention policies
- Provide data export for users

---

## 10. Legal Compliance

### 10.1 GDPR Compliance (EU)
- Obtain explicit consent
- Provide data access
- Implement data deletion
- Maintain data processing records

### 10.2 CCPA Compliance (California)
- Disclose data collection
- Provide opt-out mechanism
- Honor deletion requests

### 10.3 Emergency Services Disclaimer
Add to app and store listing:
> "SilentSOS is not a replacement for emergency services (911/112). 
> Always call emergency services directly when possible."

---

## Support

For issues or questions:
- Email: support@silentsos.app
- Documentation: https://docs.silentsos.app
- GitHub Issues: https://github.com/yourorg/silentsos/issues

---

## License

[Your License Here]
