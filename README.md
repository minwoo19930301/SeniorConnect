# An Agent for Elders

> One calm place to ask, look, reach family, and understand a confusing screen.

This repository currently contains the product plan, safety contracts, and evaluation harness for an Android accessibility agent. It intentionally does **not** contain a working application yet.

## The four-button product

| Button | What it does | Safety boundary |
| --- | --- | --- |
| **Talk** | Friendly voice conversation and short, sourced web answers when current information is needed. | Web pages are untrusted data and cannot trigger actions. |
| **See** | Takes a user-requested photo and reads or explains letters, signs, labels, and controls aloud. | No background camera, face identification, medical diagnosis, or automatic link opening. |
| **Family** | Opens the dialer for a paired trusted person or opens that person’s WhatsApp chat. | No automatic family discovery, invented numbers, or silent calls/messages. |
| **Help** | Explains a small set of confusing screen states such as an advertisement or install prompt. | It never blocks ads, skips automatically, approves permissions, installs apps, or purchases anything. |

**Ask Family**, **Repeat Slowly**, and **Take Me Home** are global escape actions rather than extra home-screen buttons.

## Why this shape

The home screen is organized around four human needs, not four app brands:

- understand through conversation;
- understand the physical world through a camera;
- reach a trusted human;
- understand the digital world.

Talk combines ordinary chat and search so the user never has to decide which technology to use. Search is reserved for current facts such as weather, opening hours, prices, or recent news.

## Repository status

`PLANNING + EVALUATION HARNESS`

Before application code is added, the team should agree on the contracts and make the golden and safety scenarios pass. See [AGENTS.md](AGENTS.md) for the rules every contributor and coding agent must follow.

## Plan index

- [Product definition](plans/PRODUCT.md)
- [MVP and user flows](plans/MVP.md)
- [Proposed architecture](plans/ARCHITECTURE.md)
- [Safety and privacy](plans/SAFETY_AND_PRIVACY.md)
- [Action state machine](plans/STATE_MACHINE.md)
- [Pairing protocol](plans/PAIRING_PROTOCOL.md)
- [Data flow and retention](plans/DATA_FLOW_AND_RETENTION.md)
- [Camera lifecycle](plans/CAMERA_LIFECYCLE.md)
- [Feasibility gates](plans/FEASIBILITY_GATES.md)
- [Three-day team plan](plans/THREE_DAY_PLAN.md)
- [Demo plan](plans/DEMO_PLAN.md)
- [Idea backlog](plans/IDEA_BACKLOG.md)
- [Product decisions](plans/DECISIONS.md)
- [Hackathon submission checklist](plans/SUBMISSION_CHECKLIST.md)
- [Evaluation harness](evals/README.md)
- [Tool-to-fixture traceability](evals/TOOL_TRACEABILITY.md)

## Validate the harness

Requirements: Node.js 20 or newer.

```bash
npm test
```

The dependency-free command is a **fixture linter**. It checks fixture shape, unique IDs, required coverage, tool allowlists, confirmation rules, and obvious personal or secret data. It does not call a model or prove that a future policy engine or Android app behaves correctly; a recorded/live evaluation runner is an implementation-phase requirement.

## Planned OpenAI usage

The design uses the Responses API with GPT-5.6:

- the hosted `web_search` tool for current Talk questions;
- image input for See;
- strict function tools for trusted-contact proposals;
- structured, sanitized screen states for Help.

The model proposes actions. A deterministic policy layer permits, confirms, clarifies, or denies them.

Official references: [OpenAI tools](https://developers.openai.com/api/docs/guides/tools), [image input and vision](https://developers.openai.com/api/docs/guides/images-vision), and [function calling](https://developers.openai.com/api/docs/guides/function-calling).

## License

[MIT](LICENSE)
