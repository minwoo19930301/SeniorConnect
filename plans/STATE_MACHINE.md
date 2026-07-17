# Action State Machine

External actions must follow this lifecycle. A model proposal and a tool execution are different events.

```text
IDLE
  → INPUT_READY
  → PROPOSAL_READY
  → POLICY_ALLOW ───────────────→ TOOL_INVOKING
  → POLICY_CONFIRM → AWAITING_CONFIRMATION
  → POLICY_CLARIFY → IDLE
  → POLICY_DENY ────────────────→ IDLE

AWAITING_CONFIRMATION
  → CONFIRMED → RECEIPT_ISSUED → TOOL_INVOKING
  → CANCELLED / EXPIRED ────────→ IDLE

TOOL_INVOKING
  → TOOL_SUCCEEDED → ACKNOWLEDGED → IDLE
  → TOOL_FAILED ───→ ACKNOWLEDGED → IDLE
```

## Proposal

The model may return an intent, opaque target ID, channel, short spoken challenge, confidence, and uncertainty. It cannot supply a raw phone number, arbitrary URL, package name, or confirmation receipt.

## Policy decision

Deterministic code validates:

- intent and proposed tool are allowlisted;
- `contact_*` exists in the local trusted-contact set;
- `app_*` exists in the local approved-app set;
- channel is approved for that contact;
- the required confirmation mode exactly matches tool policy;
- untrusted web, photo, or screen content did not supply the target;
- the feature is enabled and the proposal is not stale.

## Confirmation challenge

The challenge names:

- the exact action;
- the person or app;
- the channel;
- any data that will leave the device.

Example: “Open the phone dialer for this trusted contact?” not “Continue?”

## Confirmation receipt

A receipt is created only after an affirmative tap or clearly matched voice response. It is:

- single-use;
- bound to proposal hash, target, tool, and channel;
- short-lived;
- invalid after a changed target or action;
- consumed atomically before invocation.

The planned shape is [confirmation-receipt.schema.json](../contracts/confirmation-receipt.schema.json).

## Tool result

A result reports the narrow outcome the platform actually proved. Examples:

- `DIALER_OPENED`
- `APP_OPENED`
- `PHOTO_CAPTURED`
- `REQUEST_DELIVERED`
- `FAILED`
- `CANCELLED`

`DIALER_OPENED` does not mean a call started or connected. `REQUEST_DELIVERED` does not mean a person read or acted on it.

## User acknowledgement

- Before tool success: never use past tense that implies execution.
- After success: state only the result code’s proven outcome.
- After failure: state what did not happen and offer one retry or safe exit.
- After expiry or cancellation: say that no action was taken.

## Harness representation

- `proposed_tools` lists tools that policy may present for confirmation.
- `executed_tools` is empty before invocation.
- A case with `input.tool_result` lists the attempted tool in `executed_tools`.
- `must_not_claim_success` may be false only when the input contains a successful matching tool result.
