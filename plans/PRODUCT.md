# Product Definition

## One-sentence promise

**An Agent for Elders turns spoken questions, printed information, and confusing phone screens into one clear next step—with family always one tap away.**

## Primary users

- Older adults who find common smartphone interfaces difficult to read or navigate.
- People with low literacy who benefit from speech, photographs, symbols, and short explanations.
- A trusted family member or helper who completes the initial setup with the user.

The user remains the decision-maker. Family pairing supports agency; it does not create remote surveillance or control.

## Four primary actions

### 1. Talk — `MVP`

Tap once and speak naturally. Talk supports:

- friendly conversation;
- simple explanations;
- current factual questions through web search;
- spoken answers with large matching text.

Search should be conditional. “Tell me a story” needs no search; “Is the bank open today?” does. A searched answer should provide one short spoken answer, the date or time context, and visible source links.

Permanent follow-ups: `Repeat Slowly`, `Explain More`, and `Ask Family`.

### 2. See — `MVP`

The user opens a visible camera and deliberately takes or selects a photo. The agent may:

- read a letter or sign aloud;
- simplify printed instructions;
- translate short visible text;
- explain a menu, appliance control, or product label;
- say that the photo is blurry and request another.

The agent must state uncertainty. It must not identify people, guarantee physical safety from a photo, provide medicine dosage, extract sensitive identity/payment numbers, or open a photographed link automatically.

### 3. Family — `MVP`

During one-time assisted setup, the user confirms one trusted person with a name, photo, relationship label, and preferred contact method.

MVP actions:

- confirm and open the regular phone dialer for the trusted person;
- open the trusted WhatsApp chat;

Future action: a separately authenticated family client may receive a callback request. It is not part of the first MVP and must not be simulated as delivered infrastructure.

No address-book scanning or automatic relationship inference is allowed. Personal WhatsApp provides chat links but no public consumer API for silently starting a one-to-one voice or video call. Opening the Android dialer does not prove that a call connected.

### 4. Help — `MVP, LIMITED`

A user-triggered help control explains only these sanitized states:

- `VIDEO_AD_WAIT`
- `VIDEO_SKIP_AVAILABLE`
- `INSTALL_PROMPT`
- `PERMISSION_PROMPT`
- `PURCHASE_PROMPT`
- `SUSPICIOUS_WARNING`
- `UNKNOWN`

It explains and points. It never clicks automatically, blocks an advertisement, approves a permission, completes a purchase, or installs an application.

If Android cross-app Help proves too brittle, the fallback MVP is **Explain a Screenshot** through the Android Share sheet.

## Global escape actions

- **Ask Family** — request assistance after naming what will be shared.
- **Repeat Slowly** — replay the current guidance more slowly.
- **Take Me Home** — return to the agent’s home screen.
- **Stop** — cancel listening, speaking, capture, or an unfinished action.

## Product success criteria

The MVP succeeds when a first-time user can, without reading a manual:

1. ask a question and hear a short answer;
2. photograph a synthetic letter and hear its main point;
3. request a call to one paired person without selecting a number;
4. understand an advertisement or install prompt without an accidental action;
5. stop or ask a human at every stage.

## Non-goals

- Replacing a caregiver, clinician, emergency service, or financial adviser.
- Autonomous phone control.
- Continuous microphone, camera, or screen monitoring.
- Medication management, payments, banking, or emergency dispatch.
- Universal support for every app and screen.
- iOS support during the hackathon.
