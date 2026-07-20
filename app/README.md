# Android prototype

This module is the native Android app (`applicationId`: `org.seniorconnect.app`).

Home screen buttons:

| Button | Behavior (current phase) |
|---|---|
| **CALL** | Trusted-contact dialing handoff |
| **YOUTUBE** | In-app TV-style YouTube screen |
| **SPEAK** | Local Gemma voice conversation (needs `gemma.task`) |
| **MAP** | Nearby places map + optional Google Maps directions handoff |

## Run from the terminal (recommended)

Full setup and emulator steps: [../docs/DEV_SETUP.md](../docs/DEV_SETUP.md)

From the **repository root**:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

emulator -avd senior -no-snapshot &
adb wait-for-device
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n org.seniorconnect.app/.MainActivity
```

Windows:

```powershell
.\scripts\run-android.ps1
```

## Android Studio (optional)

Open the repository root and run the `app` configuration.
