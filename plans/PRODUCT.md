# Product Definition

## One-sentence promise

**An Agent for Elders gives four clear choices—Call, YouTube, Speak, and Camera—so a crowded phone is easier to use.**

## Primary users

- Older adults who find common smartphone interfaces difficult to read or navigate.
- People with low literacy who benefit from speech, photographs, symbols, and short explanations.
- A trusted family member or helper who completes the initial setup with the user.

The user remains the decision-maker. Family pairing supports agency; it does not create remote surveillance or control.

## Four primary actions

### 1. Call — `MVP`

During one-time assisted setup, the user confirms one trusted contact with a
name, relationship label, and preferred phone number. The Call action:

- shows the trusted contact before any action;
- asks for clear confirmation;
- opens the regular phone dialer only after confirmation.

No address-book scanning, automatic family discovery, arbitrary number calling,
or silent calls are allowed. Opening the dialer does not prove a call connected.

### 2. YouTube — `MVP, LIMITED`

YouTube opens through a supported Android intent. User-triggered guidance may
explain only a small allowlist of confusing states:

- `VIDEO_AD_WAIT`
- `VIDEO_SKIP_AVAILABLE`
- `INSTALL_PROMPT`
- `PERMISSION_PROMPT`
- `PURCHASE_PROMPT`
- `SUSPICIOUS_WARNING`
- `UNKNOWN`

It explains and points. It never clicks automatically, blocks an advertisement,
approves a permission, completes a purchase, or installs an application. If
cross-app guidance is too brittle, the fallback is **Explain a Screenshot**.

### 3. Speak — `MVP`

Tap once and speak naturally. Speak supports:

- friendly conversation;
- simple explanations;
- current factual questions through web search;
- spoken answers with large matching text.

Search is conditional. “Tell me a story” needs no search; “Is the bank open
today?” does. A searched answer gives one short response, the time context, and
visible source links.

### 4. Camera — `MVP`

The user opens a visible camera and deliberately takes or selects a photo. The agent may:

- read a letter or sign aloud;
- simplify printed instructions;
- translate short visible text;
- explain a menu, appliance control, or product label;
- say that the photo is blurry and request another.

The agent must state uncertainty. It must not identify people, guarantee physical safety from a photo, provide medicine dosage, extract sensitive identity/payment numbers, or open a photographed link automatically.

## Global escape actions

- **Repeat Slowly** — replay the current guidance more slowly.
- **Take Me Home** — return to the agent’s home screen.
- **Stop** — cancel listening, speaking, capture, or an unfinished action.

## Product success criteria

The MVP succeeds when a first-time user can, without reading a manual:

1. request a call to one paired contact without selecting a number;
2. understand a YouTube advertisement or install prompt without an accidental action;
3. ask a question and hear a short answer;
4. photograph a synthetic letter and hear its main point;
5. stop or ask a human at every stage.

## Non-goals

- Replacing a caregiver, clinician, emergency service, or financial adviser.
- Autonomous phone control.
- Continuous microphone, camera, or screen monitoring.
- Medication management, payments, banking, or emergency dispatch.
- Universal support for every app and screen.
- iOS support during the hackathon.
