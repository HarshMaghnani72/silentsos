# Quick Start Guide - Get Your App Running Now!

Follow these steps to get SilentSOS running on your device.

---

## ⚡ 5-Minute Setup

### Step 1: Firebase Console Setup (2 minutes)

1. **Go to Firebase Console**
   - Open: https://console.firebase.google.com/
   - Sign in with Google account

2. **Create/Select Project**
   - Click "Add project" or select existing project
   - Name it: `SilentSOS` (or any name you prefer)

3. **Add Android App**
   - Click Android icon
   - Package name: `com.silentsos.app`
   - Download `google-services.json`
   - Save it to: `D:\Projects\MAD harsh\silentsos\app\google-services.json`

4. **Enable Phone Authentication**
   - Go to **Authentication** → **Get started**
   - Click **Sign-in method** tab
   - Enable **Phone**
   - Click **Save**

5. **Create Firestore Database**
   - Go to **Firestore Database** → **Create database**
   - Choose **Production mode**
   - Select your region
   - Click **Enable**

6. **Setup Storage**
   - Go to **Storage** → **Get started**
   - Choose **Production mode**
   - Same region as Firestore
   - Click **Done**

---

### Step 2: Deploy Security Rules (1 minute)

**Copy and paste rules manually:**

#### Firestore Rules:
1. Go to **Firestore Database** → **Rules** tab
2. Open file: `firestore.rules` in your project
3. Copy ALL content
4. Paste in Firebase Console
5. Click **Publish**

#### Storage Rules:
1. Go to **Storage** → **Rules** tab
2. Open file: `storage.rules` in your project
3. Copy ALL content
4. Paste in Firebase Console
5. Click **Publish**

---

### Step 3: Build and Run (2 minutes)

1. **Open Android Studio**
   ```
   Open project: D:\Projects\MAD harsh\silentsos
   ```

2. **Sync Gradle**
   - Click **File** → **Sync Project with Gradle Files**
   - Wait for sync to complete

3. **Connect Device**
   - Connect Android phone via USB
   - Enable USB debugging on phone
   - Or use Android Emulator

4. **Run App**
   - Click green **Run** button (▶️)
   - Select your device
   - Wait for app to install and launch

---

## ✅ Test Your App

### Test 1: Authentication
1. App opens → Phone Auth screen
2. Enter your phone number (with country code: +1234567890)
3. Click "Send Code"
4. Check your phone for SMS
5. Enter 6-digit code
6. Click "Verify"
7. ✅ Should navigate to Calculator screen

### Test 2: Calculator Disguise
1. Calculator should work normally
2. Try: 2 + 2 = (should show 4)
3. Enter secret PIN: 1234
4. Press = button
5. ✅ Should navigate to Hidden Dashboard

### Test 3: Add Emergency Contact
1. On dashboard, click "Safety Network"
2. Click "Add Contact"
3. Enter name and phone number
4. Select priority level
5. Click "Save"
6. ✅ Contact should appear in list

### Test 4: SOS Trigger (CAREFUL!)
1. On dashboard, click big red "TRIGGER SOS" button
2. 10-second countdown starts
3. Click "Cancel" to stop it
4. ✅ Countdown should stop

**⚠️ Don't let countdown reach 0 unless you want to send real SMS to contacts!**

---

## 🐛 Common Issues

### Issue: "Firebase not initialized"
**Solution:**
- Make sure `google-services.json` is in `app/` folder
- Sync Gradle: **File** → **Sync Project with Gradle Files**
- Clean project: **Build** → **Clean Project**
- Rebuild: **Build** → **Rebuild Project**

### Issue: "SMS not received"
**Solution:**
- Check phone number format (include country code)
- Add test number in Firebase Console:
  - Go to **Authentication** → **Sign-in method** → **Phone**
  - Scroll to "Phone numbers for testing"
  - Add: +1234567890 → Code: 123456
  - Use this for testing without SMS charges

### Issue: "Permission denied" errors
**Solution:**
- Grant permissions when app asks
- Or go to phone Settings → Apps → SilentSOS → Permissions
- Enable: Location, SMS, Microphone, Phone

### Issue: App crashes on launch
**Solution:**
- Check Logcat in Android Studio for error
- Make sure `google-services.json` is present
- Make sure you're using Android 8.0+ device/emulator
- Try: **Build** → **Clean Project** → **Rebuild Project**

---

## 📱 Device Requirements

- **Minimum**: Android 8.0 (API 26)
- **Recommended**: Android 12.0+ (API 31+)
- **Storage**: 100 MB free space
- **Internet**: WiFi or mobile data
- **GPS**: Required for location tracking
- **Phone**: Required for SMS (or use emulator)

---

## 🎯 Next Steps

Once your app is running:

1. **Change Default PINs**
   - Go to Settings → Trigger Configuration
   - Change secret PIN (default: 1234)
   - Change duress PIN (default: 0000)

2. **Add Real Emergency Contacts**
   - Add family members
   - Add friends
   - Add local emergency services (optional)

3. **Test SOS Flow** (with caution)
   - Make sure contacts know it's a test
   - Trigger SOS
   - Verify SMS received
   - Verify location tracking works
   - Cancel SOS

4. **Customize Settings**
   - Adjust SOS delay (default: 10 seconds)
   - Configure trigger methods
   - Set auto-delete period for recordings

---

## 📚 Documentation

- **Full Setup Guide**: [PRODUCTION_SETUP.md](PRODUCTION_SETUP.md)
- **Manual Deployment**: [MANUAL_DEPLOYMENT_GUIDE.md](MANUAL_DEPLOYMENT_GUIDE.md)
- **Firebase CLI Setup**: [FIREBASE_SETUP.md](FIREBASE_SETUP.md)
- **Deployment Checklist**: [DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md)
- **Architecture Details**: [README.md](README.md)

---

## 💡 Tips

- **Test Mode**: Use test phone numbers in Firebase Console to avoid SMS charges
- **Emulator**: Use Android Emulator for development (no real SMS needed)
- **Debugging**: Check Logcat in Android Studio for detailed error messages
- **Firebase Console**: Monitor data in real-time at https://console.firebase.google.com/

---

## 🆘 Need Help?

1. **Check Logcat** in Android Studio for errors
2. **Check Firebase Console** for authentication/database issues
3. **Review Documentation** in the files listed above
4. **Check GitHub Issues** (if applicable)

---

## ✨ You're Ready!

Your SilentSOS app should now be running. Enjoy building and testing! 🚀

**Remember**: This is a safety app. Test thoroughly before relying on it in real emergencies.

---

**Made with ❤️ for personal safety**
