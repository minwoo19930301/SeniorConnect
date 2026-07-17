# Three-Day Team Plan

This plan starts only after the planning/harness phase is approved.

## Team ownership

| Person | Primary ownership | Deliverable |
| --- | --- | --- |
| 1 | Android shell | Four-button Compose UI, permissions, local speech, navigation |
| 2 | Agent and policy | Responses API, structured proposals, web search, confirmation gate |
| 3 | Camera and Help | CameraX, image flow, sanitized screen-state fixtures/service |
| 4 | Family, QA, submission | Pairing, dialer/chat flow, harness, README, deployment, video |

Each person owns tests and documentation for their component. One person reviews every policy/tool change before merge.

## Day 1 — vertical skeleton

- Freeze contracts and tool policy.
- Create four-button home with mocked result cards.
- Connect press-to-talk, local text-to-speech, and cancellation.
- Implement the server-side GPT-5.6 request boundary.
- Create one synthetic trusted contact.
- Make all four buttons complete a mocked flow on one device.

Exit condition: a judge can understand the complete experience even though external integrations are mocked.

## Day 2 — real features

- Talk: conditional web search with visible sources.
- See: visible capture, upload confirmation, image explanation, blurry-photo failure.
- Family: trusted-ID resolution, named confirmation, `ACTION_DIAL`, and trusted-chat opening.
- Help: only the documented state enum, plus unknown-state handling.
- Run the golden and safety suites against recorded outputs.

Exit condition: one reliable live path per button and no arbitrary external target.

## Day 3 — reliability and submission

- Add offline phrases and permission-denied fallbacks.
- Test large text, contrast, touch target size, screen reader labels, and slow speech.
- Red-team search and image prompt injection.
- Verify denial for arbitrary numbers, URLs, installs, purchases, medical dosing, and face recognition.
- Build the testable APK/demo.
- Finish English submission text, public/private repository requirements, license, `/feedback` ID, and three-minute narrated video.

Exit condition: fresh-device install, five clean demo rehearsals, harness green, and submission artifacts uploaded.

## Scope protection

If time slips, cut in this order:

1. WhatsApp chat launch; retain the regular dialer flow.
2. Cross-app AccessibilityService; retain Explain a Screenshot fixture.
3. Multiple languages; retain English plus translation-ready copy.
4. More than one trusted contact.

Never cut confirmation, Stop, unknown-state handling, privacy disclosure, or the harness.
