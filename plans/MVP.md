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

## Flow A: Call a trusted contact

1. User taps **Call** or says “Call my trusted contact.”
2. The model may resolve only a paired trusted-contact ID.
3. The app shows a generic trusted-contact card and asks: “Call this trusted contact?”
4. The user confirms or cancels.
5. Deterministic Android code opens the normal dialer with the paired number.
6. The app may say only that the dialer opened. It cannot claim the call started or connected.

Failure: an unknown or ambiguous contact produces a clear question, never a guessed number.

## Flow B: YouTube guidance

1. User taps **YouTube**.
2. The app opens YouTube through a supported Android intent.
3. When the user asks for guidance, deterministic code maps visible signals to a sanitized state.
4. The agent gives one plain-language explanation such as “This is an ad. Just wait.”
5. Install, permission, and purchase prompts receive a warning; no button is pressed automatically.

Failure: an unknown screen is described as unknown. The agent does not invent a button or timer.

## Flow C: Speak with optional search

1. User taps **Speak**.
2. The app shows a visible listening state.
3. The agent classifies the request as conversational, current-information, action, high-stakes, or unclear.
4. Current-information requests may invoke web search; ordinary chat does not.
5. The app speaks one short answer and shows its text.
6. If searched, it shows source links and when the information was checked.
7. User can `Repeat Slowly`, `Explain More`, `Take Me Home`, or `Stop`.

Failure: no network produces a clear offline message, not an invented current answer.

## Flow D: Camera and explain

1. User taps **Camera**.
2. The visible camera opens with one large shutter control.
3. The user takes a photo.
4. The app asks: “May I send this photo to explain it?”
5. After confirmation, the model returns a short summary, important visible details, uncertainty, and a suggested next step.
6. The user can `Take Again`, `Repeat Slowly`, `Take Me Home`, or delete the photo.

Failure: blurry, cropped, sensitive, or ambiguous content triggers an honest limitation.

## Definition of done

- All four happy paths work on the demo device.
- Every permission denial has a usable fallback.
- Golden and safety fixtures pass.
- No arbitrary phone number, URL, app package, or screen text can become an external action.
- English copy passes the accessibility checklist; additional languages have fluent-speaker review.
- The team can demonstrate offline and uncertainty behavior.
