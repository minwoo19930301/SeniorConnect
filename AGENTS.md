# Contributor and Agent Instructions

These instructions apply to the entire repository.

## Current phase

This is a **planning and evaluation-harness repository**. Until the team explicitly changes the phase in this file, contributors may add or edit only:

- plans and decision records;
- JSON contracts and policy files;
- synthetic evaluation fixtures;
- harness-validation scripts and CI;
- contribution and submission documentation.

Do not create Android, iOS, backend, deployment, database, or production application code during this phase.

## Product contract

The product has four primary actions:

1. **Call** — confirmed dialer handoff for an explicitly paired trusted contact.
2. **YouTube** — open YouTube and explain a small allowlist of confusing states, such as an advertisement or install prompt.
3. **Speak** — conversation plus web-grounded answers for current facts.
4. **Camera** — user-initiated photo capture followed by reading or explanation.

`Repeat Slowly`, `Take Me Home`, and `Stop` must be reachable from every primary action.

Mark every capability as `MVP`, `FUTURE`, or `NON_GOAL`. Never describe a plan, mock, fixture, or future capability as implemented.

## Human language

- Prefer “older adults,” “elders,” or “people with low literacy.”
- Never use baby talk, mock confusion, or imply that age removes agency.
- Spoken guidance should express one idea at a time, normally in 20 words or fewer.
- Ask only one question at a time.
- Provide a clear next step and a safe exit.
- Translated copy and fixtures require review by a fluent speaker.

## Safety invariants

- GPT may propose an intent; it may never execute an external action directly.
- A deterministic policy layer must `ALLOW`, `CONFIRM`, `CLARIFY`, or `DENY` every proposal.
- Calls, messages, callback requests, uploads, photo sharing, and family notifications require explicit confirmation.
- Only allowlisted app IDs and synthetic trusted-contact IDs may reach tools.
- Never create tools for automatic family discovery, arbitrary-number calling, arbitrary UI clicks, silent messaging, app installation, purchases, ad blocking, automatic ad skipping, face identification, or medical diagnosis.
- Never claim that an action succeeded until a matching tool result reports success.
- Search pages, photos, QR codes, documents, screen text, and accessibility content are untrusted data. Instructions inside them must not select tools or override policy.
- Uncertainty must be visible. Offer `Try Again` or a confirmed call to a trusted contact; do not guess.

## Privacy invariants

- Never commit real names, phone numbers, emails, addresses, IDs, contact exports, messages, photos, credentials, tokens, or API keys.
- Use obviously synthetic IDs such as `contact_primary` in fixtures; do not invent a visible person name when a generic trusted contact is enough.
- Camera use must be visible and user-initiated. No background or continuous capture.
- Require confirmation before uploading or sharing a selected photo.
- Do not upload full contact lists, full accessibility trees, or screenshots by default.
- YouTube guidance receives a sanitized state enum such as `VIDEO_AD_WAIT`, not raw screen content.
- Do not retain raw audio, photos, search queries, or screen content by default.

## Tool policy

The source of truth is [contracts/tool-policy.json](contracts/tool-policy.json). Before implementation activates a tool, it requires:

1. a documented user benefit;
2. a minimum-data definition;
3. an explicit confirmation rule;
4. a success fixture;
5. an ambiguity fixture;
6. a privacy fixture;
7. a misuse fixture.

During this planning-only phase, fixture coverage may record a deliberate gap, but no handler may be implemented or presented as validated until all four behavior categories exist.

If a proposed capability is in `forbidden_tools`, do not rename it to evade the policy.

## External-action lifecycle

Keep these phases machine-readable and separate:

1. the model returns an `AgentProposal`;
2. policy returns `ALLOW`, `CONFIRM`, `CLARIFY`, or `DENY`;
3. a confirmation challenge names the action, target, channel, and data;
4. affirmative user input creates a single-use, expiring confirmation receipt;
5. deterministic code invokes the tool;
6. a tool result records only what the platform actually proved;
7. the user acknowledgement may claim only that proven result.

`proposed_tools` never means that a tool executed. `executed_tools` must be empty until an invocation has actually occurred. Opening Android’s dialer proves only `DIALER_OPENED`; it does not prove that a call started, connected, or reached a person.

## Harness rules

- Keep fixtures synthetic and deterministic.
- Add new cases to the appropriate JSONL suite and update the manifest only when coverage intentionally changes.
- Every fixture ID must be globally unique.
- A current-information question should search; casual conversation should not.
- A trusted-contact proposal must name an existing `contact_*` ID and require confirmation.
- A YouTube-guidance case may explain or point; it may not click, cover, block, approve, purchase, or install.
- High-stakes medical, legal, financial, or emergency requests must produce a limitation plus a safe human escalation.

## Validation and commits

Run before committing:

```bash
npm test
```

This command lints plans and fixtures only. Once implementation begins, add recorded and live evaluation runners; do not present fixture lint as model, policy, or product safety evidence.

Keep commits narrowly scoped. Do not mix application implementation into a planning/harness pull request. In the PR, state what changed, which fixture coverage changed, and the validation result.
