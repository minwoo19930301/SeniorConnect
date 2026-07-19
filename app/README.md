# Android Prototype

This module contains a home screen with four large buttons and a live Maps
screen opened by the Map button:

- CALL
- YOUTUBE
- SPEAK
- MAP

Call, YouTube, and Speak remain UI-only. Map asks for Android location
permission only after the person opens it. With permission granted, it uses the
device location to show the current city, state, country, and the nearest
hospital, bus stop, and supermarket.

The Maps screen uses Android Geocoder for the locality and OpenStreetMap's
public Overpass service for nearby places. Coordinates are used only for the
active lookup and are not stored by the app. The feature needs internet access;
it does not provide directions, contact access, or AI integration.

Open the repository root in Android Studio and run the `app` configuration. To
build from a terminal with Android SDK Platform 36 installed, run:

```bash
./gradlew :app:assembleDebug
```
