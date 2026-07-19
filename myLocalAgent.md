# SeniorConnect Local Agent Log

## 2026-07-19 - Call feature planning pass

### User request
- Maintain this file as the working handoff log for future agents.
- Read the README and understand the app.
- Focus on the Call feature for older adults.
- Do not write implementation code yet.
- First visualize the Call feature and explain the proposed build approach.
- Wait for user confirmation before building.

### Repository state observed
- Repository: `SeniorConnect`
- App type: native Android prototype.
- Current implementation is intentionally narrow:
  - one Android activity;
  - one home screen;
  - four large buttons;
  - no click behavior yet;
  - no Android permissions yet.
- Current UI files checked:
  - `app/src/main/java/org/seniorconnect/app/MainActivity.java`
  - `app/src/main/res/layout/activity_main.xml`
  - `app/src/main/res/values/strings.xml`
  - `app/src/main/AndroidManifest.xml`
- Current home screen labels are `CALL`, `YOUTUBE`, `SPEAK`, and `MAP`.
- Note: repo docs also mention a later `Camera` primary action in some places, so future agents should be careful about the product naming mismatch before broad UI edits.

### Documents read
- `README.md`
- `AGENTS.md`
- `plans/PRODUCT.md`
- `plans/MVP.md`
- `plans/SAFETY_AND_PRIVACY.md`
- `plans/ARCHITECTURE.md`
- `plans/STATE_MACHINE.md`
- `plans/PAIRING_PROTOCOL.md`
- `plans/THREE_DAY_PLAN.md`
- `contracts/tool-policy.json`
- `evals/TOOL_TRACEABILITY.md`

### Call feature understanding
- Call is an MVP feature.
- The app must make calling easier without taking control away from the elder.
- The model may only propose a paired trusted-contact ID such as `contact_primary`.
- The phone number must stay local and must not be sent to GPT.
- The app must show the trusted contact and ask for explicit elder confirmation.
- After confirmation, deterministic Android code opens the normal phone dialer with `ACTION_DIAL`.
- The app can only claim `DIALER_OPENED`; it cannot claim the call started, connected, or reached the person.
- Unknown or ambiguous contact requests must clarify or fail safely. The app must never guess a number.

### Safety constraints to preserve
- No address-book scanning.
- No automatic family discovery.
- No arbitrary-number calling.
- No silent calls.
- No background call placement.
- No external action without confirmation.
- No real names, numbers, or personal data in fixtures.

### Proposed next work after user confirmation
1. Add a small Call flow UI state after tapping `CALL`.
2. Add synthetic trusted contact storage or mock data for the MVP, using an opaque ID.
3. Add a confirmation screen or dialog: "Open the phone dialer for this trusted contact?"
4. Add deterministic policy checks before any dialer intent.
5. Use `ACTION_DIAL`, not `ACTION_CALL`, so Android opens the dialer and the elder remains in control.
6. Add fixture coverage for success, ambiguity, privacy, and misuse before claiming the handler is validated.
7. Run `npm test` and, if the Android toolchain is available, `gradlew.bat :app:assembleDebug`.

### Current status
- Planning and visualization phase in progress.
- No application implementation code has been changed.
- Waiting for user confirmation before building the Call feature.

## 2026-07-19 - User refined Call feature into a call-focused agent

### Refined concept
- The user wants to build an agent specifically for: "Help an elder call a trusted person easily and safely."
- Possible product names listed by user:
  - `CallMate`
  - `EasyCall`
  - `OneTap Call`
  - `FamilyCall`
  - `TrustedCall`
  - `CareCall`
  - `CallGuide`
- User's best pick is `TrustedCall`.
- Rationale: the app does not call random people. It helps the elder call people they already trust.

### Refined MVP screen
- Main prompt: "Who do you want to call?"
- Large contact buttons:
  - Son
  - Daughter
  - Doctor
  - Caregiver
- Global controls:
  - Speak Name
  - Take Me Home
  - Stop

### Refined agent behavior
1. Elder opens the app.
2. Elder taps a large trusted-contact button or says a phrase such as "Call my son."
3. Agent checks saved trusted contacts only.
4. Agent clearly confirms the matched person, for example: "I found Rahul. Do you want to call Rahul?"
5. Elder confirms by tapping `Yes, Call` or saying "Yes."
6. Android opens the normal phone dialer with the number.
7. Elder makes the final call manually.

### Refined V1 core features
- Add 3-5 trusted contacts during setup.
- Big contact buttons.
- Voice command such as "Call my daughter."
- Confirmation before opening the dialer.
- Repeat Slowly button.
- Emergency warning, but no automatic emergency calling.
- No full contact-list upload to AI.

### Technical direction from user
- Android app.
- Prefer Kotlin for implementation direction, though current repo shell is Java/XML.
- Local trusted contacts stored on device.
- Speech-to-text for commands such as "Call my son."
- Simple deterministic matching first, AI later.
- Use `Intent.ACTION_DIAL` to open the phone dialer.
- The first agent should be clear, safe, and hard to misuse rather than overly smart.

### Updated implementation recommendation
- Build `TrustedCall` as a focused call flow inside SeniorConnect first, not as a broad AI agent.
- Start with deterministic relation matching:
  - transcript contains "son" -> `contact_son`
  - transcript contains "daughter" -> `contact_daughter`
  - transcript contains "doctor" -> `contact_doctor`
  - transcript contains "caregiver" -> `contact_caregiver`
- Add AI only after deterministic safety, confirmation, fixtures, and policy checks are working.

## 2026-07-19 - Merge and ownership constraints

### User clarification
- User is building only the call/dialing feature.
- Three other teammates are building the other features separately.
- Team decided to use Kotlin.
- Implementation should be isolated so it does not cause merge issues.
- Create a separate folder/module area for the dialing feature when implementation starts.
- Do not write app code yet; explain the build approach first.

### Updated merge-safe approach
- Keep the dialing feature in a dedicated package/folder, for example:
  - `app/src/main/java/org/seniorconnect/app/dialing/`
- Keep feature-specific classes inside that package so teammates can work on YouTube, Speak, and Camera/Map separately.
- Touch shared files only when necessary:
  - `MainActivity` or navigation entry point;
  - resource files for call-specific strings/layouts;
  - manifest only if a permission or activity registration is truly needed.
- Prefer no calling permission by using `Intent.ACTION_DIAL`; this avoids adding risky manifest permissions.
- Use Kotlin for new dialing feature files. If the current entry point remains Java temporarily, bridge from Java to Kotlin with a small, minimal call only after confirmation.

## 2026-07-19 - Testing concern: Expo vs native Android

### User concern
- User asked whether Expo can be used to run the Android app because the native command-line Android setup feels difficult.

### Important note
- This repository is a native Android app, not Expo or React Native.
- Expo cannot run this app directly unless the project is rewritten or wrapped as a React Native/Expo project, which would create merge conflicts and conflict with the team's Kotlin direction.
- Recommended simpler testing path is Android Studio or a generated APK install, not Expo.

## 2026-07-19 - TrustedCall implementation pass

### Implemented
- Added isolated Kotlin dialing feature package:
  - `app/src/main/java/org/seniorconnect/app/dialing/TrustedContact.kt`
  - `app/src/main/java/org/seniorconnect/app/dialing/TrustedContactRepository.kt`
  - `app/src/main/java/org/seniorconnect/app/dialing/VoiceContactMatcher.kt`
  - `app/src/main/java/org/seniorconnect/app/dialing/TrustedCallAgent.kt`
  - `app/src/main/java/org/seniorconnect/app/dialing/DialingPolicy.kt`
  - `app/src/main/java/org/seniorconnect/app/dialing/DialIntentLauncher.kt`
  - `app/src/main/java/org/seniorconnect/app/dialing/DialingActivity.kt`
- Wired only the existing home `CALL` button to launch `DialingActivity`.
- Kept `YOUTUBE`, `SPEAK`, and `MAP` buttons behavior-free to reduce merge conflicts with teammates.
- Registered `DialingActivity` in `AndroidManifest.xml`.
- Added call-specific strings for `TrustedCall` controls.
- Updated the Android shell validator so it allows only the Call button entry and checks that the dialing package uses `Intent.ACTION_DIAL`, not `Intent.ACTION_CALL`.

### Current TrustedCall behavior
- Shows "Who do you want to call?"
- Provides large buttons for `Son`, `Daughter`, `Doctor`, and `Caregiver`.
- Provides `Speak Name`, `Repeat Slowly`, `Take Me Home`, and `Stop`.
- Uses local demo trusted contacts with synthetic numbers:
  - `contact_son`
  - `contact_daughter`
  - `contact_doctor`
  - `contact_caregiver`
- Voice recognition uses Android's speech recognizer intent when available.
- Spoken requests are matched deterministically first.
- Emergency-like words show a warning and do not call automatically.
- Confirmation is required before opening the dialer.
- Dialing uses `Intent.ACTION_DIAL`; the elder must press the final call button in the phone dialer.
- No Android permissions were added.

### GPT/OpenAI note
- No OpenAI API call or API key was added to the Android client.
- This is intentional for safety: phone numbers and secrets should not be placed in a mobile client or sent to GPT.
- `TrustedCallAgent` is now the isolated decision point where a future server-side GPT classifier can be added after the deterministic MVP is working.

### Validation
- `npm test` passed.
- `gradlew.bat :app:assembleDebug` could not complete because this machine has no Android SDK configured:
  - `ANDROID_HOME` is not set.
  - `ANDROID_SDK_ROOT` is not set.
  - `C:\Android` does not exist.
- Gradle wrapper/network access worked far enough to reach the SDK-location error after removing the Kotlin plugin conflict.

### Next recommended steps
1. Install Android Studio or Android command-line tools.
2. Make sure the SDK path is configured through Android Studio or `ANDROID_HOME`.
3. Run `gradlew.bat :app:assembleDebug`.
4. Install the APK on a phone and test:
   - Home -> CALL -> Son -> Yes, Call.
   - Home -> CALL -> Speak Name -> say "call my son" -> Yes, Call.
   - Try saying "call emergency" and confirm it does not auto-call.

## 2026-07-19 - User test note: tapping Son does not call

### Explanation
- Current behavior is intentionally two-step for safety:
  - tap `Son`;
  - app shows confirmation;
  - tap `Yes, Call`;
  - Android dialer opens.
- Tapping `Son` alone should not place or open a call.
- Current repository contacts use placeholder demo numbers such as `5550101`; they are not real trusted-person numbers.
- To test a real phone call, a setup flow or local-only test contact update is needed. Do not commit real phone numbers to the repository.

### UI issue found from screenshot
- Screenshot showed duplicate bottom controls after cancelling a call:
  - `Speak Name`
  - `Repeat Slowly`
  - `Take Me Home`
  - `Stop`
  - repeated `Repeat Slowly`
  - repeated `Take Me Home`
  - repeated `Stop`
- Fixed `DialingActivity.kt` by:
  - splitting contact-grid buttons from full-width confirmation buttons;
  - tagging the controls container;
  - removing any existing controls container before adding a new one.
- `npm test` passed after the fix.

## 2026-07-19 - Local trusted-contact setup added

### User concern
- User wanted the feature to call actual trusted people and asked why the app was not using agentic AI to do the calling.

### Implemented
- Removed hardcoded fake demo numbers from `TrustedContactRepository.kt`.
- Added local on-device storage using `SharedPreferences`.
- Trusted slots now start without phone numbers:
  - Son
  - Daughter
  - Doctor
  - Caregiver
- If a slot has no number, tapping it opens a setup screen.
- Setup screen lets the user enter:
  - trusted contact display name;
  - phone number.
- The saved number stays local to the phone.
- After saving, the app shows the normal confirmation step.
- After confirmation, the app opens Android's dialer using `Intent.ACTION_DIAL`.

### Safety stance
- Still no automatic background calls.
- Still no `CALL_PHONE` permission.
- Still no full contact-list upload.
- Still no phone numbers sent to GPT.
- The "agent" role is local intent resolution and safe flow control first. A future server-side GPT classifier can be added behind `TrustedCallAgent`, but Android should not contain an API key.

### Validation
- `npm test` passed after the local setup change.
- Source scan found no `555010` fake numbers and no `Intent.ACTION_CALL` usage in app code.
