# Lightweight developer setup (no Android Studio)

This project builds and runs with the Android **command-line** toolchain only.
Android Studio works too, but is optional and much heavier. Expo does not apply
— this is a native Android app, not React Native.

You need three things: **JDK 17**, the **Android command-line tools**, and either
a **USB phone** or the **standalone emulator**.

## macOS (Apple Silicon or Intel)

```bash
brew install openjdk@17 android-commandlinetools

# add to ~/.zshrc (or run each session)
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

yes | sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-36" "build-tools;35.0.0"
```

Emulator (skip if you will use a real phone — a phone is the lightest option):

```bash
# Apple Silicon
sdkmanager "emulator" "system-images;android-35;google_apis;arm64-v8a"
avdmanager create avd -n senior -k "system-images;android-35;google_apis;arm64-v8a" -d pixel_6

# Intel Mac: use x86_64 instead of arm64-v8a
```

## Windows

1. Install [Temurin JDK 17](https://adoptium.net).
2. Download **"Command line tools only"** from the bottom of
   <https://developer.android.com/studio> and unzip so the tools live at
   `C:\Android\cmdline-tools\latest\`.
3. In PowerShell:

```powershell
setx ANDROID_HOME C:\Android
setx PATH "%PATH%;C:\Android\platform-tools;C:\Android\emulator"
C:\Android\cmdline-tools\latest\bin\sdkmanager --licenses
C:\Android\cmdline-tools\latest\bin\sdkmanager "platform-tools" "platforms;android-36" "build-tools;35.0.0"
```

Emulator (optional; enable the "Windows Hypervisor Platform" Windows feature for speed):

```powershell
C:\Android\cmdline-tools\latest\bin\sdkmanager "emulator" "system-images;android-35;google_apis;x86_64"
C:\Android\cmdline-tools\latest\bin\avdmanager create avd -n senior -k "system-images;android-35;google_apis;x86_64" -d pixel_6
```

## Real phone instead of an emulator

Enable **Developer Options → USB debugging** on any Android phone (Android 6+),
plug it in, accept the prompt, and confirm it appears in `adb devices`. All
install/launch commands below work identically.

## Build, install, launch

### macOS / Linux

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

# Boot the shared AVD (skip when a phone is already connected)
emulator -avd senior -no-snapshot &
adb wait-for-device
adb shell 'while [ "$(getprop sys.boot_completed)" != "1" ]; do sleep 2; done'
adb devices

# Build + install + open home screen
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n org.seniorconnect.app/.MainActivity
```

You should see **"What would you like to do?"** with four large buttons:
**CALL**, **YOUTUBE**, **SPEAK**, **MAP**.

### Windows one-shot script

From the repo root:

```powershell
.\scripts\run-android.ps1
```

This starts AVD `senior` if needed, sets a simulated Islamabad location,
builds, installs, and launches `MainActivity`.

Manual Windows flow:

```powershell
.\gradlew.bat :app:assembleDebug
emulator -avd senior
adb wait-for-device
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n org.seniorconnect.app/.MainActivity
```

## Optional helpers

```bash
# Media volume (YouTube sound)
adb shell media volume --stream 3 --set 15

# Simulated GPS (lon lat) — useful for Map nearby places
adb emu geo fix 73.05756 33.71921

# Screenshot
adb exec-out screencap -p > screen.png

# App logs
adb logcat | grep -i seniorconnect

# Stop emulator
adb emu kill
```

## Speak (local Gemma)

Model weights are not in git. After the app is installed, push a
`gemma.task` file into private app storage (see README **Install the local
Gemma model**), then open **SPEAK** on the home screen.

## Debugging tips

| Symptom | Check |
|---|---|
| `adb: command not found` | `ANDROID_HOME` / `PATH` exports |
| `Unable to locate a Java Runtime` | `JAVA_HOME` points at JDK 17 |
| Emulator stuck on boot | `adb devices`, wait for `sys.boot_completed=1` |
| Map shows no location | `adb emu geo fix <lon> <lat>` or grant location |
| No YouTube audio | media volume; avoid `emulator -no-audio` |
| Package / activity not found | use `org.seniorconnect.app/.MainActivity` |
