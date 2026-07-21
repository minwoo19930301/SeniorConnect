# SeniorConnect

**SeniorConnect** is a simple Android app concept that helps older people use a phone with less confusion.

The app gives one clear answer or one clear next step. When it is unsure, it helps the person contact someone they trust.

The repository includes a small Android UI prototype with four large buttons.
The Speak button starts a simple conversation for company. Android speech
recognition transcribes the user's words and text-to-speech reads the answer.
If a local Gemma model is installed, it gives richer answers; without Gemma,
SeniorConnect uses a small offline small-talk fallback.

## What the planned home screen looks like

![Concept home screen with four large buttons: Call, YouTube, Speak, and Map](assets/planned-home-screen.png)

This is a concept image. The Android prototype follows the same simple
two-by-two home-screen layout.

## Watch the concept trailer

This 9:16 video shows how the app could help an older person. It is a concept demonstration, not footage of a finished app.

[Watch the vertical concept trailer](assets/seniorconnect-trailer.mp4)

## Why we want to build it

Phone screens can have small buttons, unclear messages, advertisements, and choices that are easy to press by mistake.

Our goal is not to take control of someone’s phone. Our goal is to make the phone easier to understand while the person stays in control.

## The four main buttons

### Call

Choose a trusted family contact during setup. Tap **Call**, choose the contact,
and confirm before the normal phone dialer opens.

The app does not search for relatives, invent phone numbers, or place a call in
the background.

### YouTube

Tap **YouTube** to open YouTube with simple guidance.

If an advertisement appears, the app can explain: “An ad may be playing. Please wait.” If an install screen appears, it warns the person
before they continue. It does not block ads or press buttons by itself.

### Speak

Tap **Speak** and talk normally.

The agent can have a simple conversation, answer a question, or search for
current information and show where the answer came from.

### Map

Tap **Map** to open a simple map and find a nearby places without sorting through
a crowded phone. Tapping a place marker can open Google Maps directions after a
clear confirmation.

The map opens only when the person asks. The app does not provide in-app
directions; it shares only the chosen-place coordinate with Google Maps after
confirmation. Google Maps determines the current origin itself.

## Controls are always close

Parts of the app include these simple controls:

- **Repeat Slowly**
- **Take Me Home**
- **Stop**

## Our safety promise

- The person using the phone makes the final decision.
- Calls need clear permission.
- The full contact list is not sent to the AI.
- The app does not make purchases or medical decisions.
- If the app is unsure, it says so instead of guessing.

## What is in this repository

This repository contains:

- a native Android UI prototype;
- what the app should do;
- how the four main buttons should work;
- privacy and safety rules;
- a three-day hackathon plan;
- 41 example situations we can use to test the future app.

The Android prototype includes Call, YouTube, Speak, and Map. Network is used
for YouTube playback and Map place/tile lookups. Speak works with the Android
microphone and text-to-speech; Gemma is optional.

## Install the local Gemma model

The model weights are intentionally not committed to GitHub. Download a
compatible Gemma 3 `.task` model, name it `gemma.task`, and copy it into the
app's private storage:

```powershell
$adb = "C:\Users\ACB\AppData\Local\Android\Sdk\platform-tools\adb.exe"
& $adb push .\gemma.task /data/local/tmp/gemma.task
& $adb shell run-as org.seniorconnect.app mkdir -p files
& $adb shell run-as org.seniorconnect.app cp /data/local/tmp/gemma.task files/gemma.task
```

Open **SPEAK**. Gemma starts listening automatically, reads its answer aloud,
and listens for the next question. Press **STOP** to end the conversation.

## Run the Android prototype

Android Studio is **not** required. The supported lightweight path uses JDK 17
plus the Android command-line tools, with either a USB-connected phone (the
lightest option) or the standalone emulator.

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

You should see **"What would you like to do?"** with **CALL / YOUTUBE / SPEAK / MAP**.

### Windows PowerShell shortcut

```powershell
.\scripts\run-android.ps1
```

That script boots AVD `senior` if needed, sets a simulated Islamabad location,
builds, installs, and launches the app.

### Useful adb helpers

```bash
adb devices
adb exec-out screencap -p > screen.png
adb shell media volume --stream 3 --set 15          # media volume
adb emu geo fix 73.05756 33.71921                   # lon lat (Islamabad)
adb emu kill                                        # stop emulator
```

The debug APK will be created at
`app/build/outputs/apk/debug/app-debug.apk`. Note this is a native Kotlin app,
so Expo and other React Native tooling do not apply.

If you prefer Android Studio, opening the repository folder and running the
`app` configuration also works.

## For teammates

Start here:

1. Read [the product plan](plans/PRODUCT.md).
2. Read [the small first version](plans/MVP.md).
3. Follow [the contributor rules](AGENTS.md).
4. Check [the three-day team plan](plans/THREE_DAY_PLAN.md).
5. Install local git hooks so **everyone** (including admins) uses PRs, not direct pushes to `main`:

   ```bash
   ./scripts/install-dev-hooks.sh
   ```

6. Read [PR workflow](docs/PR_WORKFLOW.md) (Mac/Windows daily flow + optional Mac hourly Grok PR checker via crontab).

Other useful documents:

- [Safety and privacy](plans/SAFETY_AND_PRIVACY.md)
- [PR workflow](docs/PR_WORKFLOW.md)
- [How family pairing works](plans/PAIRING_PROTOCOL.md)
- [Ideas for later](plans/IDEA_BACKLOG.md)
- [Demo plan](plans/DEMO_PLAN.md)
- [Hackathon checklist](plans/SUBMISSION_CHECKLIST.md)

## Check the project files

If Node.js 20 or newer is installed, run:

```bash
npm test
```

This checks the planning fixtures and confirms that the Android screen has
exactly four primary buttons and that the Speak voice flow is present. It does
not test a real microphone, device speech recognizer, or Gemma inference.

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
