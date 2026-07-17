# MVP and User Flows

## Scope rule

The hackathon MVP demonstrates one reliable path per primary button. Breadth is less valuable than clear failure behavior.

## First-run setup

1. Choose spoken and display language.
2. Hear the privacy explanation.
3. Pair one trusted family member through a short-lived invitation or select one contact with the system picker.
4. Confirm the name, photo, relationship, and preferred channel.
5. Grant camera, microphone, Home-role, or accessibility permissions only when the relevant feature needs them.
6. Practice the four buttons in a guided tour that can be skipped.

No permission is bundled into a single “accept everything” screen.

## Flow A: Talk with optional search

1. User taps **Talk**.
2. The app shows a visible listening state.
3. The agent classifies the request as conversational, current-information, action, high-stakes, or unclear.
4. Current-information requests may invoke web search; ordinary chat does not.
5. The app speaks one short answer and shows its text.
6. If searched, it shows source links and when the information was checked.
7. User can `Repeat Slowly`, `Explain More`, `Ask Family`, or `Stop`.

Failure: no network produces a clear offline message, not an invented current answer.

## Flow B: See and explain

1. User taps **See**.
2. The visible camera opens with one large shutter control.
3. The user takes a photo.
4. The app asks: “May I send this photo to explain it?”
5. After confirmation, the model returns a short summary, important visible details, uncertainty, and a suggested next step.
6. The user can `Take Again`, `Repeat Slowly`, `Ask Family`, or delete the photo.

Failure: blurry, cropped, sensitive, or ambiguous content triggers an honest limitation.

## Flow C: Call family

1. User taps **Family** or says “Call my granddaughter.”
2. The model may resolve only the paired trusted-contact ID.
3. The app shows the trusted person’s photo and says: “Call Mina by phone?”
4. The user confirms or cancels.
5. Deterministic Android code opens the dialer with Mina’s trusted number.
6. The app may say only that the dialer opened. It cannot claim the call started, connected, or reached Mina.

WhatsApp callback requests require a separate authenticated family client and are `FUTURE`, not part of this MVP.

## Flow D: Help with a screen

1. User taps **Help** while on a supported screen.
2. On-device deterministic code maps visible accessibility signals to a sanitized state.
3. GPT receives only the state and language preferences.
4. The app says one plain-language explanation.
5. It offers safe choices such as `Wait`, `Take Me Home`, or `Ask Family`.

Unknown means unknown. The agent must not infer a button or timer that is not exposed.

## Definition of done

- All four happy paths work on the demo device.
- Every permission denial has a usable fallback.
- Golden and safety fixtures pass.
- No arbitrary phone number, URL, app package, or screen text can become an external action.
- English copy passes the accessibility checklist; additional languages have fluent-speaker review.
- The team can demonstrate offline and uncertainty behavior.
