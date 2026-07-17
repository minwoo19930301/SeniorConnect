# Proposed Architecture

This document is a plan, not an implementation claim.

## System shape

```text
Android UI
  ├─ Call: trusted-contact ID + confirmed dialer handoff
  ├─ YouTube: app intent + deterministic screen-state guidance
  ├─ Speak: press-to-talk + local text-to-speech
  └─ Camera: visible capture or selected image
          │
          ▼
Sanitizer and policy layer
  ├─ removes unneeded raw content
  ├─ validates allowlisted IDs and apps
  ├─ requires confirmation for external actions
  └─ denies forbidden capabilities
          │
          ▼
Server-side OpenAI Responses API
  ├─ GPT-5.6 conversation and plain-language explanation
  ├─ hosted web_search for current questions
  ├─ image input for a confirmed selected photo
  └─ strict function proposals
          │
          ▼
Deterministic Android tools after policy + confirmation
```

## Why one agent, not four

The buttons are four contexts for one agent. One shared conversation state preserves language, speech speed, paired-contact IDs, and the user’s current goal. Each context exposes only the minimum tools it needs.

## OpenAI plan

Use the Responses API with the hackathon-required GPT-5.6 model family.

### Speak

- Default to a normal text response.
- Enable the hosted `web_search` tool for current or externally verifiable questions.
- Require citations in the visible UI for searched answers.
- Treat retrieved page content as untrusted evidence, never as instructions.

### Camera

- Send only the selected photo after an upload confirmation.
- Request a structured response: `summary`, `visible_details`, `uncertainty`, `next_step`, and `risk_flag`.
- Do not include action tools in the image-explanation request.

### Call

- Provide only synthetic trusted-contact IDs, display labels, and allowed channels.
- Use strict function schemas.
- Set `parallel_tool_calls` to false for user-facing external actions.
- Return a proposal to the policy layer; the model never performs the Android action.

### YouTube

- Build the state enum on-device.
- Send a small object such as:

```json
{
  "state": "VIDEO_AD_WAIT",
  "skip_available": false,
  "visible_countdown_seconds": null,
  "language": "en",
  "reading_level": "very_simple"
}
```

- Do not send the full accessibility tree, notifications, passwords, or messages.

References: [using tools](https://developers.openai.com/api/docs/guides/tools), [image input](https://developers.openai.com/api/docs/guides/images-vision), [function calling](https://developers.openai.com/api/docs/guides/function-calling), and [agent safety](https://developers.openai.com/api/docs/guides/agent-builder-safety).

The broad `AgentProposal` schema is an application-domain contract. The planned all-required transport shape is [openai-proposal-transport.schema.json](../contracts/openai-proposal-transport.schema.json); implementation must smoke-test the exact schema against the API rather than assuming every JSON Schema keyword is supported.

## Android plan

- Kotlin and Jetpack Compose for the proposed app.
- Android Home role for the optional four-button home screen; selection remains user-controlled.
- System contact picker or short-lived family invitation for minimum contact access.
- `ACTION_DIAL` as the reliable baseline. Its only success result is `DIALER_OPENED`.
- CameraX for visible user-triggered capture.
- Package-scoped AccessibilityService only if YouTube guidance is implemented cross-app.
- Android local text-to-speech for cached safety phrases and offline fallback.

If the AccessibilityService gate passes, the enabled service ignores content by default. A user tap starts one short YouTube-guidance session, captures only the minimum accessible signals needed for one state, sanitizes them locally, discards raw nodes, and returns to inactive processing. If that cannot be demonstrated clearly, use Explain a Screenshot instead.

## Data classes

- `UserPreferences`: language, text size, speech rate; stored locally.
- `TrustedContact`: opaque ID, display name, relationship, photo reference, approved channels; encrypted locally.
- `AgentProposal`: validated against [agent-proposal.schema.json](../contracts/agent-proposal.schema.json).
- `ScreenState`: validated against [screen-state.schema.json](../contracts/screen-state.schema.json).
- `ConsentReceipt`: action category, timestamp, result; contains no raw conversation or image.
- `ToolResult`: a platform-proven result such as `DIALER_OPENED`, never an inferred real-world outcome.

## Trust boundaries

| Data | Default location | Cloud rule |
| --- | --- | --- |
| Full contact list | Never collected | Never uploaded |
| Selected trusted contact | Device | Opaque ID only when needed |
| Raw audio | Ephemeral | Do not retain by default |
| Selected photo | Device | Upload only after confirmation; delete after response by default |
| Accessibility tree | Device | Never upload |
| Sanitized state enum | Device/server | May be sent for explanation |
| Search query | Server | Send only when search is needed |

These rows describe planned app/backend behavior, not every processor’s retention. See [DATA_FLOW_AND_RETENTION.md](DATA_FLOW_AND_RETENTION.md).

## Failure strategy

Safety phrases for ads, install prompts, permissions, purchases, cancellation, and uncertainty should be cached locally. If the network or model fails, the user still receives a clear explanation and can go Home or ask family.
