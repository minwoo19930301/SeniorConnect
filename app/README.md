# Android UI Prototype

This module contains one native Android screen with four large buttons:

- CALL
- YOUTUBE
- SPEAK
- MAP

The Speak button opens a local Gemma voice conversation. The app requests only
microphone access; the other three buttons remain visual placeholders.

Open the repository root in Android Studio and run the `app` configuration. To
build from a terminal with Android SDK Platform 36 installed, run:

```bash
./gradlew :app:assembleDebug
```
