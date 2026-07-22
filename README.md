# SeniorConnect

**SeniorConnect** is a native Android MVP that helps older adults use a phone
with less confusion. It gives one clear next step and keeps the person in
control.

The app currently has four large actions: trusted-contact dialer handoff, an
in-app YouTube TV screen, local voice conversation, and live nearby places.

**Submitted on Devpost:**
[https://devpost.com/software/seniorconnect](https://devpost.com/software/seniorconnect?ref_content=contribution-prompt&ref_feature=engagement&ref_medium=email&utm_campaign=contribution-prompt&utm_content=contribution_reminder&utm_medium=email&utm_source=transactional#app-team)

## Home screen

![Concept home screen with four large buttons: Call, YouTube, Speak, and Map](assets/planned-home-screen.png)

This is a concept image. The implemented Android home screen follows the same
simple two-by-two layout.

## Watch the concept trailer

This 9:16 video is a concept demonstration, not footage of a finished app.

[Watch the vertical concept trailer](assets/seniorconnect-trailer.mp4)

## Implemented MVP

### Call

Tap **Call** to add or choose one of four trusted-contact slots: Son, Daughter,
Doctor, or Caregiver. The app can also recognize one of those saved relations
from a spoken request. It asks for confirmation before opening the normal phone
dialer with `ACTION_DIAL`.

The app does not search for relatives, invent phone numbers, or call in the
background. Opening the dialer does not prove a call started or connected.

### YouTube

Tap **YouTube** to start a full-screen, in-app TV-style player using the
official embedded YouTube player. It starts with a fresh random video, plays
automatically, and advances when a video ends. A reviewed per-country playlist
can be configured; the bundled reviewed video list is the fallback.

There are no on-screen playback controls. A touch shield absorbs every touch,
including on ads and install prompts. The app never blocks, mutes, skips, or
clicks ads. Leave with the system back gesture; locking the screen, going home,
or switching tasks ends the session. If every video fails to play, the screen
shows that videos are unavailable.

### Speak

Tap **Speak** and talk normally. Android speech recognition transcribes the
words and text-to-speech reads the reply. When a compatible `gemma.task` model
is installed on the device, Speak uses it for a richer local response;
otherwise, it uses a short offline small-talk fallback.

This MVP is for local conversation only. GPT-5.6-backed answers and web search
for current facts are planned work, not part of the current APK.

### Map

Tap **Map** to request location permission only when it is needed, show the
current reverse-geocoded address, and find the nearest hospital, bus stop, and
supermarket within five kilometres. The map uses MapLibre with OpenStreetMap
tiles and nearby-place data from OpenStreetMap’s public Overpass service.

Tapping a nearby-place marker shows its distance and asks before opening the
installed Google Maps app for directions. If Google Maps is unavailable, the
app says so. The app does not provide in-app directions or retain location data.

## Current controls

The Call flow includes **Repeat Slowly**, **Take Me Home**, and **Stop**.
Speak includes **Stop** and **Take me home**. Map includes **Try again** and
**Back to home**. The YouTube screen intentionally has no in-app controls;
leave it with the system back gesture. Making the same controls available from
every primary action is future work.

## Safety and privacy

- The person using the phone makes the final decision.
- Calls and directions handoff need clear confirmation.
- The app does not read a device contact list or invent phone numbers.
- The app does not make purchases or medical decisions.
- Microphone and location permission are requested only when Speak or Map is
  opened.
- The active Map session does not retain location or address data.

## What is in this repository

- the native Android MVP;
- product, architecture, privacy, and safety documentation;
- policy contracts and 50 synthetic evaluation fixtures; and
- lightweight validation for both fixtures and the Android shell.

Network is used only for YouTube playback and Map tile/place lookups. Speak
uses the Android microphone, speech recognition, and text-to-speech; Gemma is
optional.

## Install the local Gemma model

Model weights are intentionally not committed to GitHub. Download a compatible
Gemma 3 `.task` model, name it `gemma.task`, and copy it into the app's private
storage:

```powershell
$adb = "C:\Users\ACB\AppData\Local\Android\Sdk\platform-tools\adb.exe"
& $adb push .\gemma.task /data/local/tmp/gemma.task
& $adb shell run-as org.seniorconnect.app mkdir -p files
& $adb shell run-as org.seniorconnect.app cp /data/local/tmp/gemma.task files/gemma.task
```

Open **SPEAK**. It requests microphone permission when needed, then begins
listening. It reads the reply aloud and continues listening. Press **STOP** to
end the conversation.

## Run the Android MVP

Android Studio is optional. The supported lightweight path uses JDK 17 and the
Android command-line tools with either a USB-connected phone or an emulator.

**One-time setup (macOS / Windows):** [docs/DEV_SETUP.md](docs/DEV_SETUP.md)

### Quick run (macOS / Linux)

```bash
# env (Homebrew paths on Apple Silicon)
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

# boot the shared AVD (create once via docs/DEV_SETUP.md)
emulator -avd senior -no-snapshot &
adb wait-for-device
adb shell 'while [ "$(getprop sys.boot_completed)" != "1" ]; do sleep 2; done'

# build, install, launch
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n org.seniorconnect.app/.MainActivity
```

You should see **“What would you like to do?”** with **CALL / YOUTUBE / SPEAK / MAP**.

### Windows PowerShell shortcut

```powershell
.\scripts\run-android.ps1
```

That script boots AVD `senior` if needed, sets a simulated Islamabad location,
builds, installs, and launches the app.

The debug APK is created at `app/build/outputs/apk/debug/app-debug.apk`.

## Validate the project

With Node.js 20 or newer installed, run:

```bash
npm test
./gradlew :app:assembleDebug
```

`npm test` validates the policy contracts and synthetic evaluation fixtures. It
also checks that the Android shell has four primary buttons, the required
handlers and permissions, `ACTION_DIAL` only for calls, and no YouTube
picture-in-picture. The Gradle command compiles a debug APK. These checks do
not test real microphone input, speech recognition, Gemma inference, network
playback, location accuracy, or real dialer/Google Maps handoff.

## For teammates

Start with [AGENTS.md](AGENTS.md), then read the [product plan](plans/PRODUCT.md),
[current MVP scope](plans/MVP.md), [safety and privacy guidance](plans/SAFETY_AND_PRIVACY.md),
and [development setup](docs/DEV_SETUP.md).

## GPT-5.6 and Codex

### GPT-5.6: product reasoning, with a bounded runtime role

We used GPT-5.6 during development to turn the broad problem—making a phone
less confusing—into small, testable flows and plain-language copy. It helped
us identify the decisions that must remain deterministic: a person confirms a
call or directions handoff, the app never acts on an arbitrary phone number,
and uncertainty gets a safe next step instead of a guess.

The intended production boundary is deliberately narrow: GPT-5.6 may provide
a conversation response or propose a constrained intent, but it cannot invoke
Android actions. A deterministic policy and confirmation step must decide
whether an action is allowed before Android opens the dialer or Google Maps.
This is the key safety decision behind the architecture.

**Current implementation status:** GPT-5.6 is not connected to the Android
APK yet. The implemented Speak experience uses Android speech recognition and
text-to-speech, an optional on-device Gemma model, and a short offline fallback.
We state this distinction so the demo does not imply that a planned cloud-model
integration already exists.

### Codex: accelerating the build and verification loop

We used Codex as an engineering collaborator to explore the codebase, turn the
product constraints into Android implementation tasks, and quickly iterate on
the native shell, documentation, and checks. In particular, Codex accelerated
the workflow by helping us:

- keep the home screen focused on exactly four large actions;
- implement and review the safety boundaries around `ACTION_DIAL`, location
  permission, confirmed Google Maps handoff, and the touch-protected YouTube
  WebView;
- add and maintain lightweight validation that catches regressions such as a
  missing home-screen handler, direct-call permission, or an unsupported
  integration; and
- use the command-line Android build/emulator workflow to surface integration
  issues without requiring Android Studio.

Codex also made the important trade-offs visible in the repository: no direct
calling, no in-app directions, no ad interaction, and no retained location.
That shortened the path from an idea to a reviewable MVP while leaving final
product and safety decisions with the team.

## License

[MIT](LICENSE)
