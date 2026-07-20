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

## 2026-07-19 - Agentic conversational call flow integrated

### User request
- When the elder taps the CALL button, activate a conversational agent.
- Agent talks to the elder, asks who they want to call.
- If elder says "call my son", agent searches contacts by relation keyword.
- If not understood, agent asks "What is the name of the person you want to call?"
- If the elder gives a name, agent does a fuzzy name search across all saved contacts.
- If multiple contacts match, show all as tappable buttons so elder picks one.
- If elder rejects ("that's not my son"), agent searches again.
- After identifying the right contact, show confirmation then open the dialer.
- All changes documented here so the next coding agent knows where to continue.

### Files changed

#### `VoiceContactMatcher.kt` — extended with name-based fuzzy search and rejection detection
- `match()` now tries relation keywords first, then falls back to `matchByName()`.
- New `MatchResult.Ambiguous(candidates)` returned when multiple contacts match a name.
- New `matchByName(transcript)`: splits transcript into words, returns every contact whose
  `displayName` contains any of those words (case-insensitive, min 2 chars per word).
- New `isRejection(transcript)`: detects "no", "not", "wrong", "different", "nope", "nah",
  "that's not" — used to route the conversation back to the search step.
- Expanded `emergencyWords` list to include "911".
- Expanded `relationAliases` map — each relation has multiple natural-language aliases
  (e.g. son → "son", "boy", "my boy", "my son").

#### `TrustedCallAgent.kt` — added multi-turn conversation support
- `AgentDecision` sealed interface gains `MultipleFound(candidates)` and `RejectedContact`.
- `resolveSpokenRequest()` now returns `MultipleFound` when matcher returns `Ambiguous`.
- New `refineByName(transcript)`: used on follow-up turns when elder provides a name.
  Checks for rejection first, then re-runs name search.

#### `DialingActivity.kt` — full conversational state machine
New `ConvState` enum drives screen routing:
- `PICKING`    — home grid "Who do you want to call?"
- `CLARIFYING` — agent didn't understand; "What is the name of the person you want to call?"
- `CANDIDATES` — multiple contacts found; all shown as tappable buttons
- `CONFIRMING` — single contact confirmed; "I found X. Do you want to call X?"
- `SETUP`      — contact slot has no phone number yet

New screens:
- `showClarifying(message)` — prompts for a name with a Speak button and Take Me Home.
- `showCandidates(candidates, message)` — one full-width button per candidate; "None of these"
  routes back to clarifying.

Conversation routing in `handleVoiceTranscript()`:
- `PICKING` / `SETUP` → `handleFirstTurn()` — full match (relation + name)
- `CLARIFYING`        → `handleRefineTurn()` — name-only search via `refineByName()`
- `CANDIDATES`        → `handleCandidatesTurn()` — narrows the already-shown candidates list;
  falls back to `handleRefineTurn()` if no narrowing works
- `CONFIRMING`        → `handleConfirmationTurn()` — detects "yes"/"no"/"call"; if something
  else was said, treats it as a new search

### Conversation flow example (from user spec)
1. Elder taps CALL → "Who do you want to call?"
2. Elder says "call my son" → relation match → "I found Rahul. Do you want to call Rahul?"
3. Elder taps Yes, Call → dialer opens.

OR (name not in relation keywords):
1. Elder says "call John" → no relation match, name search finds "John Smith (son)"
2. One result → "I found John Smith. Do you want to call John Smith?"
3. Elder taps Yes, Call → dialer opens.

OR (ambiguous):
1. Elder says "call Mary" → name search finds "Mary (daughter)" and "Mary K (caregiver)"
2. → "I found 2 possible contacts. Which one did you mean?"
   Buttons: [Mary (Daughter)] [Mary K (Caregiver)] [None of these — try again]
3. Elder taps "Mary (Daughter)" → confirmation screen → dialer.

OR (rejection):
1. Confirmation shown with wrong contact
2. Elder taps "No" → "Okay. What is the name of the person you want to call?"
3. Elder speaks the name → name search → confirmation.

### Safety invariants unchanged
- No `CALL_PHONE` permission added.
- Still uses `Intent.ACTION_DIAL` exclusively.
- Elder must tap "Yes, Call" before the dialer opens.
- No names or phone numbers sent to any external service.
- Emergency words still show a warning and do not auto-call.

### Validation
- `npm test` passed (41 fixture cases, Android shell lint OK).
- Gradle APK build still requires ANDROID_HOME to be set (no change from before).

### Next recommended steps
1. Build the APK in Android Studio and test the conversation loop on a real phone.
2. Add "Speak Name" button to `showCandidates()` screen so the elder can say a name
   instead of tapping, if the list is long.
3. Add a max-retry guard: after 3 failed clarifications, offer "Take Me Home".
4. Consider adding a contact name search that tolerates phonetic/pronunciation differences
   (e.g. Soundex or Jaro-Winkler) for elders with accent variation.
5. Future: route `TrustedCallAgent.resolveSpokenRequest()` through a server-side
   GPT classifier for intent understanding — the agent class is already the isolated
   decision point for this upgrade.

## 2026-07-19 - Gemini AI integrated into calling agent

### User request
- Use a real AI model (free tier) to power the calling agent.
- AI chosen: **Gemini 2.0 Flash** via Google AI Studio REST API (free, no credit card).
- Integrate directly in the Android app (hackathon convenience).
- API key stored in `GeminiClient.kt` — do NOT commit to a public repo in production.

### New file: `GeminiClient.kt`
- Thin OkHttp wrapper around the Gemini `generateContent` REST endpoint.
- Model: `gemini-2.0-flash`
- `maxOutputTokens: 100`, `temperature: 0.2` — keeps replies short and deterministic.
- All calls are **synchronous** (must be run on a background thread).
- Returns `null` on any network/parse error — caller falls back gracefully.

### Changes to `TrustedCallAgent.kt`
- `resolveSpokenRequest()` now takes `contacts: List<TrustedContact>` as a parameter.
- `refineByName()` now takes `contacts: List<TrustedContact>` as a parameter.
- Flow:
  1. Try local keyword match (instant, no network).
  2. If no local match → build a structured prompt and call `GeminiClient.ask()`.
  3. Parse Gemini reply:
     - Single relation word → `AgentDecision.Contact`
     - `MULTIPLE: son, doctor` → `AgentDecision.MultipleFound`
     - A question → `AgentDecision.NeedsMoreInfo(question)`
  4. If Gemini fails (null) → local name fuzzy search fallback → `NeedsMoreInfo` if still nothing.
- New `AgentDecision.NeedsMoreInfo(question)` replaces old `NoMatch` for unclear cases.
  The `question` string comes directly from Gemini and is shown to the elder.

### Changes to `DialingActivity.kt`
- New `ConvState.THINKING` — shown while Gemini is processing ("Let me find that for you…").
- Gemini calls run on a `SingleThreadExecutor`; result posted back via `mainHandler.post`.
- `handleVoiceTranscript()` now ignores input while in `THINKING` state (prevents double-calls).
- `resolveAsync(transcript, isRefinement)` is the single entry point for all AI calls.
- `applyDecision()` handles the full `AgentDecision` sealed interface including `NeedsMoreInfo`.
- Executor shut down cleanly in `onDestroy()`.

### Changes to `app/build.gradle.kts`
- Added dependencies:
  - `com.squareup.okhttp3:okhttp:4.12.0` — HTTP client for Gemini REST calls
  - `org.json:json:20240303` — JSON parsing

### Changes to `AndroidManifest.xml`
- Added `<uses-permission android:name="android.permission.INTERNET" />`
- Required for OkHttp to reach the Gemini API endpoint.

### Changes to `scripts/validate-android-shell.mjs`
- Updated permission lint rule: `INTERNET` is now explicitly allowed.
- All other permissions (CALL_PHONE, READ_CONTACTS, RECORD_AUDIO, etc.) remain forbidden.
- `npm test` still passes.

### Full conversation flow with Gemini
1. Elder taps CALL → "Who do you want to call?"
2. Elder taps Speak Name, says "call my grandson"
3. No local keyword match → Gemini called with contact list + transcript
4. Gemini returns: "What is the name of your grandson?" (NeedsMoreInfo)
5. Screen shows: "What is the name of your grandson?" + Speak button
6. Elder says "John" → refineByName("John") → local name search finds "John (son)"
7. Confirmation: "I found John. Do you want to call John?" → Yes, Call → dialer opens

### Safety unchanged
- Phone numbers are NOT sent to Gemini — only relation labels and display names.
- Still uses `Intent.ACTION_DIAL` — elder presses call in the dialer.
- Emergency words still show a warning, never auto-call.
- Local keyword match runs first — Gemini only called when local match fails.

### Next recommended steps
1. Build APK in Android Studio and test on a real phone.
2. Move API key to `local.properties` / `BuildConfig` before any public repo push.
3. Add a timeout indicator on the THINKING screen (spinner or progress bar).
4. Consider caching the last successful Gemini response for offline replay.
