# Safety and Privacy

## Core rule

The agent reduces confusion without taking control. It explains, proposes, confirms, and escalates.

## Threat model

The plan assumes these inputs can be incorrect or hostile:

- speech recognition transcripts;
- web search results;
- text inside a photographed document, QR code, or sign;
- visible app and accessibility text;
- contact labels;
- model-generated tool arguments.

No untrusted input may bypass the deterministic policy layer.

## Action levels

| Level | Examples | Required behavior |
| --- | --- | --- |
| Read-only | conversation, cached explanation | Allow, with Stop available |
| External information | web search, confirmed image analysis | Disclose network use and minimize data |
| Reversible navigation | open an approved app, go Home | User-triggered only |
| Human contact | call, open chat, request callback, share photo | Name recipient/channel and confirm |
| High stakes | payment, installation, permission approval, medication, emergency | Do not execute; explain and escalate |

## Camera safety

- Capture is visible and user-triggered.
- Show the selected photo before upload.
- Ask before uploading or sharing.
- Do not identify faces.
- Do not extract or repeat full payment-card, bank-account, identity-document, or authentication values.
- A medicine label may be read aloud, but the agent cannot recommend dosage, treatment, or interactions.
- A photograph cannot prove that a stove, road, medication, or other physical situation is safe.
- A QR code may be described; it is never opened automatically.

## Family safety

- Pair family explicitly; never infer relationships from call frequency or names.
- Invitation links expire and are single-use.
- Both people can revoke pairing.
- The family member cannot view the elder’s microphone, screen, messages, photos, or history remotely.
- Use only a trusted-contact ID supplied by the application.
- State the person and channel during confirmation.
- WhatsApp chat opening leaves the final send action to the user. A callback request is preferable to brittle UI automation.

## Screen Help safety

- Accessibility access is opt-in, explained immediately before system permission, and package-scoped where possible.
- Detection is deterministic and best-effort.
- Guidance may describe or point but cannot automatically click another app.
- Never block or cover an advertisement, auto-skip it, approve a permission, confirm a purchase, or install an app.
- A normal consumer application provides a soft guard only. Real installation restriction is a future managed-device path.

References: [Google Play AccessibilityService policy](https://support.google.com/googleplay/android-developer/answer/10964491), [Android AccessibilityService](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService), and [YouTube API policies](https://developers.google.com/youtube/terms/developer-policies).

## High-stakes requests

- **Medical:** provide no diagnosis or dosing recommendation; suggest a clinician, pharmacist, or trusted person.
- **Financial:** do not move money, enter payment data, or pronounce an offer safe.
- **Legal:** explain that the app cannot provide legal advice and offer human help.
- **Emergency:** encourage contacting local emergency services or a nearby person; the hackathon MVP does not dispatch automatically.
- **Scam:** describe suspicious signals and recommend not proceeding; do not guarantee that a message is safe or fraudulent.

## Data minimization

- No continuous microphone, camera, or screen capture.
- No full address book.
- No raw screen tree sent to the model.
- The app and team backend do not retain raw audio, photos, or searches by default.
- Do not opt user content into provider training. Verify the actual organization and processor controls before making any public “not used for training” claim.
- Logs contain action categories and error codes, not messages or images.
- Repository fixtures contain only synthetic data.

See [DATA_FLOW_AND_RETENTION.md](DATA_FLOW_AND_RETENTION.md) for processor-specific limits; local deletion is not a claim about third-party retention.

## Incident behavior

If the system is uncertain, a tool fails, or the policy rejects a proposal:

1. state what did not happen;
2. do not claim success;
3. offer one safe retry;
4. offer `Ask Family` or `Take Me Home`.
