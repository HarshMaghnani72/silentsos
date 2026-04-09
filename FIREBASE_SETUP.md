# Firebase CLI Setup Guide (Windows)

## Option 1: Install via npm (Recommended)

### Step 1: Install Node.js
1. Download Node.js from: https://nodejs.org/
2. Install the LTS version (includes npm)
3. Verify installation:
   ```bash
   node --version
   npm --version
   ```

### Step 2: Install Firebase CLI
```bash
npm install -g firebase-tools
```

### Step 3: Verify Installation
```bash
firebase --version
```

### Step 4: Login to Firebase
```bash
firebase login
```
This will open a browser window for authentication.

---

## Option 2: Install via Standalone Binary

### Download and Install
1. Download from: https://firebase.tools/bin/win/instant/latest
2. Run the installer
3. Restart your terminal/command prompt
4. Verify: `firebase --version`

---

## Option 3: Manual Deployment (Without CLI)

If you prefer not to install Firebase CLI, you can deploy rules manually:

### For Firestore Rules:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Go to **Firestore Database** → **Rules**
4. Copy the content from `firestore.rules` file
5. Paste into the Firebase Console editor
6. Click **Publish**

### For Storage Rules:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Go to **Storage** → **Rules**
4. Copy the content from `storage.rules` file
5. Paste into the Firebase Console editor
6. Click **Publish**

---

## After Installation

### Initialize Firebase in Your Project

```bash
# Navigate to your project directory
cd D:\Projects\MAD harsh\silentsos

# Initialize Firebase
firebase init

# Select:
# - Firestore
# - Storage
# - Use existing project
# - Select your Firebase project
# - Accept default file names (firestore.rules, storage.rules)
```

### Deploy Rules

```bash
# Deploy both Firestore and Storage rules
firebase deploy --only firestore:rules,storage

# Or deploy individually
firebase deploy --only firestore:rules
firebase deploy --only storage
```

### Common Commands

```bash
# Login
firebase login

# Logout
firebase logout

# List projects
firebase projects:list

# Use specific project
firebase use <project-id>

# Deploy all
firebase deploy

# Deploy specific services
firebase deploy --only firestore:rules
firebase deploy --only storage
firebase deploy --only hosting
```

---

## Troubleshooting

### "firebase is not recognized"
- Restart your terminal/command prompt after installation
- Check if Node.js and npm are installed: `node --version`
- Reinstall Firebase CLI: `npm install -g firebase-tools`
- Add npm global bin to PATH (usually: `C:\Users\<YourName>\AppData\Roaming\npm`)

### "Permission denied"
- Run terminal as Administrator
- Or use: `npm install -g firebase-tools --force`

### "Cannot find module"
- Clear npm cache: `npm cache clean --force`
- Reinstall: `npm install -g firebase-tools`

### "Login failed"
- Try: `firebase login --reauth`
- Or: `firebase login --no-localhost`

---

## Quick Start (After Installation)

```bash
# 1. Login
firebase login

# 2. Initialize (one-time setup)
firebase init

# 3. Deploy rules
firebase deploy --only firestore:rules,storage

# Done! ✅
```

---

## Alternative: Use Firebase Console (No CLI Required)

If you don't want to install Firebase CLI, you can manage everything through the web console:

1. **Firestore Rules**: Console → Firestore → Rules → Edit → Publish
2. **Storage Rules**: Console → Storage → Rules → Edit → Publish
3. **Authentication**: Console → Authentication → Sign-in method
4. **Database**: Console → Firestore → Data

This is perfectly fine for development and small projects!

---

## Need Help?

- Firebase CLI Docs: https://firebase.google.com/docs/cli
- Firebase Console: https://console.firebase.google.com/
- Node.js Download: https://nodejs.org/
