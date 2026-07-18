# Android UI Prototype

This module contains one native Android screen with four large buttons:

- CALL
- YOUTUBE
- SPEAK
- CAMERA

The buttons intentionally have no click handlers. The app requests no Android
permissions and does not connect to contacts, YouTube, a microphone, a camera,
the internet, or an AI service.

Open the repository root in Android Studio and run the `app` configuration. To
build from a terminal with Android SDK Platform 36 installed, run:

```bash
./gradlew :app:assembleDebug
```
