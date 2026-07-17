# Feasibility Gates

Run these go/no-go tests before building beyond the vertical skeleton.

## Gate 1: Talk and search

Deadline: first half of Day 1.

Pass when:

- GPT-5.6 answers a casual prompt without search;
- a current prompt invokes web search and returns visible sources;
- a search-result prompt injection cannot propose an external action;
- network failure produces no invented current answer.

Fallback: Talk remains conversational and current questions clearly state that search is unavailable.

## Gate 2: See

Deadline: end of Day 1.

Pass when:

- CameraX capture and selected-photo preview work on the demo device;
- upload confirmation occurs before network transfer;
- a synthetic letter receives a short structured explanation;
- blurry, identity-document, medicine-dose, face-identification, and QR-injection cases behave safely.

Fallback: ship See with gallery selection only; retain the same consent and safety contract.

## Gate 3: Family

Deadline: end of Day 1.

Pass when:

- one contact is selected through the system picker;
- the model can resolve only its opaque ID;
- policy rejects a hallucinated or screen-sourced target;
- the named confirmation opens `ACTION_DIAL`;
- acknowledgement says only that the dialer opened.

Fallback: one fixed synthetic demo contact stored locally. Do not add a callback backend.

## Gate 4: Help

Deadline: first half of Day 2.

Cross-app mode passes only when:

- the AccessibilityService has a clear disclosure and manual enablement flow;
- supported packages and events are allowlisted;
- processing begins only after the user invokes Help and stops after one state snapshot or a short bounded window;
- no raw accessibility content is uploaded;
- ad, install, permission, purchase, and unknown states are reliably distinguished on the demo device;
- no action auto-clicks, blocks, approves, buys, or installs.

Fallback trigger: any policy ambiguity, unreliable state detection, or onboarding delay by the deadline.

Fallback: **Explain a Screenshot** through the Android Share sheet or an explicitly labeled Scenario Lab. Do not claim general cross-app understanding.

## Gate 5: evaluation evidence

Deadline: Day 2.

Pass when:

- fixture lint is green;
- a recorded/live runner captures model proposal, policy decision, confirmation state, tool invocation, and result;
- safety-critical fixtures are compared with actual outputs;
- results distinguish “not run,” “proposed,” “confirmed,” “invoked,” “failed,” and “succeeded.”

Fallback: show fixture lint only as engineering preparation, never as proof the product passed behavioral evaluation.
