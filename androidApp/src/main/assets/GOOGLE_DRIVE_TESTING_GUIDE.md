# Google Drive Backup - Manual Testing Guide

**Date**: 2026-08-21  
**Feature**: Automatic Backup with Google Drive Integration (Phase 2)  
**Status**: Ready for Testing

---

## 🎯 Testing Scope

This guide covers manual testing of the Google Drive automatic backup feature, including:
- Local backup (Phase 1 - Recap)
- Google Drive destination UI (Phase 2 - Focus)
- OAuth 2.0 sign-in flow
- Backup destination toggle
- Sign-in/Sign-out flow

---

## 📋 Pre-Testing Checklist

### ✅ Prerequisites
- [ ] Android 8+ device or emulator
- [ ] Google account for testing
- [ ] Stable internet connection
- [ ] AntCashManager v1.7.4 (or latest dev build)
- [ ] Google Cloud Console setup completed (GOOGLE_DRIVE_SETUP.md)

### ✅ Test Accounts
- **Primary**: Testing Google account (has Drive access)
- **Secondary**: Optional fallback account

### ✅ Device Settings
- Notifications enabled: Settings → Apps → AntCashManager → Notifications
- Storage access enabled: Device Settings → Apps → Permissions → Storage
- Internet connection stable

---

## 🧪 Test Scenarios

### Scenario 1: LOCAL Backup Toggle (Phase 1 Recap)

**Objective**: Verify Phase 1 local backup still works correctly

**Steps**:
1. Open Settings → Data Management
2. Locate "Automatic Backup" card
3. Toggle OFF → Verify toggle is OFF, no UI change
4. Toggle ON → SAF folder picker opens
5. Cancel folder picker → Verify toggle remains OFF
6. Toggle ON again → Select a folder (e.g., Downloads)
7. Verify:
   - ✅ Toggle remains ON
   - ✅ "Backup is weekly" label visible
   - ✅ Destination section shows (Local + Google Drive options)
   - ✅ "Local" is active (highlighted), "Google Drive" is disabled

**Expected Result**: ✅ PASS - Local backup toggle works, folder picker opens

---

### Scenario 2: Google Drive Destination UI (Phase 2 Core)

**Objective**: Verify Google Drive destination UI is visible and responsive

**Steps**:
1. Open Settings → Data Management
2. Enable Automatic Backup (toggle ON)
3. In Destination section, verify:
   - ✅ "Local" card visible with Backup icon (teal background)
   - ✅ "Google Drive" card visible with Cloud icon (disabled state = gray background)
   - ✅ "Google Drive" subtitle: "Coming soon"
4. Tap "Google Drive" card
5. Verify:
   - ✅ Google Sign-In dialog appears
   - ✅ Dialog title: "Enable Google Drive Backup"
   - ✅ Dialog message mentions encryption and Google account
   - ✅ "Sign In with Google" button is clickable

**Expected Result**: ✅ PASS - UI is visible and dialogs appear correctly

---

### Scenario 3: OAuth 2.0 Sign-In Flow

**Objective**: Test complete Google Sign-In flow

**Steps**:
1. Open Settings → Data Management
2. Enable Automatic Backup
3. Tap "Google Drive" card → Google Sign-In dialog appears
4. Tap "Sign In with Google" button
5. Verify:
   - ✅ Loading dialog: "Signing in to Google…"
   - ✅ After ~2-3 seconds, dialog closes
   - ✅ Destination section updates:
     - "Local" is now disabled (gray)
     - "Google Drive" is now active (blue background)
     - Subtitle now shows: `user@gmail.com` (email)
   - ✅ New "Sign Out from Google" card appears below
6. Verify state persistence:
   - Close Settings and reopen
   - ✅ Google Drive remains selected
   - ✅ Email still visible
   - ✅ Sign-out button still visible

**Expected Result**: ✅ PASS - Sign-in flow completes, state persists

---

### Scenario 4: Destination Toggle (LOCAL ↔ GOOGLE_DRIVE)

**Objective**: Verify seamless destination switching

**Steps** (starting from Scenario 3 - signed in):
1. Google Drive is currently selected
2. Tap "Local" card
3. Verify:
   - ✅ "Local" becomes active (teal background)
   - ✅ "Google Drive" becomes disabled (gray background)
   - ✅ Email subtitle disappears
   - ✅ "Sign Out from Google" button disappears
   - ✅ Backup schedule persists (still weekly)
4. Tap "Google Drive" again
5. Verify:
   - ✅ No dialog appears (already signed in)
   - ✅ "Google Drive" immediately becomes active
   - ✅ Email reappears
   - ✅ "Sign Out from Google" button reappears

**Expected Result**: ✅ PASS - Destination toggles instantly when signed in

---

### Scenario 5: Sign-Out Flow

**Objective**: Test logout and credential revocation

**Steps** (starting from Google Drive selected):
1. Locate "Sign Out from Google" card
2. Tap the card
3. Verify:
   - ✅ Loading dialog: "Signing out…" (brief)
   - ✅ After completion, destination reverts to LOCAL
   - ✅ Email disappears
   - ✅ "Sign Out from Google" button disappears
4. Close Settings and reopen
5. Verify:
   - ✅ LOCAL is still selected
   - ✅ No email visible

**Expected Result**: ✅ PASS - Sign-out clears credentials and reverts to LOCAL

---

### Scenario 6: Automatic Backup Execution (LOCAL)

**Objective**: Verify automatic backup creates file locally

**Prerequisites**: 
- Local backup is selected
- Folder is chosen
- Device has at least one transaction

**Steps**:
1. Open Settings → Data Management
2. Verify "Automatic Backup" is ON
3. Go to file manager, navigate to chosen backup folder
4. Wait 7 days OR manually trigger backup (debug mode):
   - In Android Studio terminal: `adb shell cmd jobscheduler run -f com.sformica.ant_cashmanager <job_id>`
   - Or: Reduce work interval temporarily in debug build
5. Verify:
   - ✅ New backup file appears: `AntCashManager-backup-YYYYMMDD-HHMMSS.json`
   - ✅ File size > 1 KB (contains data)
   - ✅ "Last Backup" timestamp in Settings updated

**Expected Result**: ✅ PASS - Backup file created successfully

---

### Scenario 7: Dark Mode Compatibility

**Objective**: Verify UI looks correct in dark theme

**Steps**:
1. Device Settings → Display → Dark Theme (or system default)
2. Open Settings → Data Management
3. Verify:
   - ✅ Cards are readable (text contrast OK)
   - ✅ Icons are visible (correct tint colors)
   - ✅ Disabled cards show disabled state (gray tint)
   - ✅ Selected destination is clearly highlighted
4. Disable Automatic Backup
5. Verify:
   - ✅ Destination section disappears
   - ✅ "Weekly" label disappears
   - ✅ No layout shift

**Expected Result**: ✅ PASS - Dark mode renders correctly

---

### Scenario 8: Error Handling (Negative Test)

**Objective**: Verify app handles errors gracefully

**Steps**:
1. **Network Error**: Airplane mode ON, attempt sign-in
   - Verify: Error is caught, dialog closes, state reverts to LOCAL
2. **User Cancels**: Tap "Cancel" in Sign-In dialog (if available)
   - Verify: Dialog closes, destination remains LOCAL
3. **Invalid Credentials**: Sign in with test account, then revoke permissions in Google account settings
   - Tap "Google Drive" on next app open
   - Verify: Error is handled, option to retry

**Expected Result**: ✅ PASS - All error cases handled gracefully

---

### Scenario 9: Localization (Multi-Language)

**Objective**: Verify strings are translated correctly

**Steps**:
1. Device Settings → Language → Change to German (Deutsch)
2. Open AntCashManager Settings → Data Management
3. Verify:
   - ✅ "Automatische Sicherung" (German title)
   - ✅ All dialog texts in German
   - ✅ "Mit Google anmelden" (German sign-in button)
4. Switch language to Italian
   - ✅ "Backup Automatico"
   - ✅ All texts in Italian
5. Switch back to English
   - ✅ "Automatic Backup"

**Expected Result**: ✅ PASS - All strings translated correctly

---

## ✅ Test Checklist

| Scenario | Status | Notes |
|----------|--------|-------|
| 1. LOCAL Toggle | ⬜ | |
| 2. Google Drive UI | ⬜ | |
| 3. OAuth Sign-In | ⬜ | |
| 4. Destination Toggle | ⬜ | |
| 5. Sign-Out Flow | ⬜ | |
| 6. Auto Backup Execution | ⬜ | |
| 7. Dark Mode | ⬜ | |
| 8. Error Handling | ⬜ | |
| 9. Localization | ⬜ | |

**Overall Status**: ⬜ UNTESTED / 🟡 IN PROGRESS / 🟢 COMPLETE

---

## 📝 Issue Template

If you encounter issues, log them using this template:

```
**Title**: [Brief description]
**Scenario**: [Which scenario from above]
**Steps to Reproduce**:
1. 
2. 
3. 

**Expected**: 
**Actual**: 
**Device/OS**: [e.g., Pixel 6 / Android 12]
**Build**: [e.g., v1.7.4-dev-build-123]
**Screenshots**: [If applicable]
```

---

## 🚀 Performance Notes

- **Sign-In Time**: Expected ~2-3 seconds (network dependent)
- **Destination Switch**: Should be instant (<100ms)
- **UI Responsiveness**: No janks or freezes
- **Memory**: No leaks after repeated sign-in/out cycles

---

## 🔍 Post-Testing Checklist

After all scenarios pass:
- [ ] No crashes observed
- [ ] All UI elements render correctly
- [ ] All strings localized properly
- [ ] Performance acceptable
- [ ] Error handling works
- [ ] State persistence verified
- [ ] Dark mode looks good
- [ ] Ready for Alpha/Beta release

---

## 📞 Support

For questions or issues:
1. Review GOOGLE_DRIVE_SETUP.md for OAuth setup
2. Check logs: `adb logcat | grep "GoogleSignInManager\|DriveUploadManager"`
3. Verify Google Cloud Console settings
4. Check app permissions: Settings → Apps → AntCashManager

---

**Version**: 1.0  
**Last Updated**: 2026-08-21  
**Author**: Development Team
