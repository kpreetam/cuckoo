# 🐦 Cuckoo Clock — Build Instructions

## Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| Android Studio | Hedgehog 2023.1+ | https://developer.android.com/studio |
| JDK | 17 or 21 | bundled with Android Studio |
| Android SDK | API 34 | via SDK Manager in Android Studio |
| Gradle | 8.4 (auto-downloaded) | via wrapper |

---

## Option A — Android Studio (Recommended)

1. Open Android Studio → **File → Open** → select the `CuckooClock` folder
2. Wait for Gradle sync to finish (downloads dependencies automatically)
3. Connect your Android device (USB debugging on) **or** start an emulator (API 26+)
4. Click the green ▶ **Run** button

The APK installs directly on the device.

---

## Option B — Command Line (Linux / macOS)

```bash
# 1. Set JAVA_HOME (Android Studio bundles a JDK)
export JAVA_HOME=/path/to/jdk17   # or use Android Studio's bundled JDK

# 2. Install SDK via sdkmanager (or use Android Studio's SDK Manager)
sdkmanager "platforms;android-34" "build-tools;34.0.0"

# 3. Set ANDROID_HOME
export ANDROID_HOME=$HOME/Android/Sdk   # typical Linux path
# macOS: export ANDROID_HOME=$HOME/Library/Android/sdk

# 4. Build debug APK
cd CuckooClock
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Install to device:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Option C — Build Release APK

```bash
# Generate a keystore (one-time):
keytool -genkey -v -keystore cuckoo.keystore -alias cuckoo -keyalg RSA -keysize 2048 -validity 10000

# Build signed release:
./gradlew assembleRelease \
  -Pandroid.injected.signing.store.file=cuckoo.keystore \
  -Pandroid.injected.signing.store.password=YOUR_STORE_PASS \
  -Pandroid.injected.signing.key.alias=cuckoo \
  -Pandroid.injected.signing.key.password=YOUR_KEY_PASS

# Output: app/build/outputs/apk/release/app-release.apk
```

---

## Permissions Required

| Permission | Purpose |
|-----------|---------|
| `SCHEDULE_EXACT_ALARM` | Precise hourly/half-hour chimes |
| `USE_EXACT_ALARM` | Android 13+ exact alarms |
| `RECEIVE_BOOT_COMPLETED` | Reschedule chimes after reboot |
| `POST_NOTIFICATIONS` | Foreground service notification |
| `FOREGROUND_SERVICE` | Keep chime service alive |
| `ACCESS_NOTIFICATION_POLICY` | Override DND (optional, user grants) |
| `VIBRATE` | Vibration support |

On **Android 12+** you may need to manually grant "Alarms & Reminders" in  
Settings → Apps → Cuckoo Clock → Permissions.

---

## Project Structure

```
CuckooClock/
├── app/src/main/
│   ├── java/com/example/cuckooclock/
│   │   ├── MainActivity.kt          — tab host for 3 clock modes
│   │   ├── SettingsActivity.kt      — preference screen
│   │   ├── PrefsKeys.kt             — preference key constants
│   │   ├── ChimeScheduler.kt        — schedules exact alarms
│   │   ├── ChimeReceiver.kt         — BroadcastReceiver; checks bedtime/silent
│   │   ├── ChimeService.kt          — ForegroundService; plays tones
│   │   ├── BootReceiver.kt          — reschedules after reboot
│   │   ├── fragments/
│   │   │   ├── DigitalClockFragment.kt
│   │   │   ├── AnalogClockFragment.kt
│   │   │   └── BitByteClockFragment.kt
│   │   └── views/
│   │       ├── AnalogClockView.kt   — custom drawn analogue face
│   │       └── BitByteClockView.kt  — 6-column binary clock
│   └── res/
│       ├── layout/                  — XML layouts
│       ├── values/                  — colors, strings, themes
│       └── xml/preferences.xml      — settings screen definition
```

---

## Features

### Clock Modes
| Tab | Description |
|-----|-------------|
| **Digital** | Large 12h display with seconds and date |
| **Analogue** | Custom-drawn dark-gold clock face with smooth hands |
| **Bit/Byte** | 6-column binary clock (H H : M M : S S), glowing dots |

### Chimes
- Hourly chime: plays N cuckoo sounds for the current hour (1–12)
- Half-hour chime: plays 1 sound at :30
- Both chimes independently configurable:
  - Enable/disable toggle
  - Sound: Cuckoo, Bell, Chime, Whistle (all synthesized, no audio files needed)
  - Volume: 0–100 slider

### Quiet Hours
- Bedtime start/end times (24h format, e.g. `22:00` / `07:00`)
- Handles overnight spans correctly (e.g. 22:00 → 07:00)

### Silent Mode Override
- When enabled, chimes play even in Ringer Silent or Vibrate mode
- Requires Android "Do Not Disturb access" permission (prompted automatically)

### Boot Persistence
- Chimes resume automatically after device reboot
