# Product Decisions

## D-001: organize by human need, not app brand

**Decision:** use Talk, See, Family, and Help rather than WhatsApp, YouTube, Camera, and Search.

**Reason:** apps change; the user’s goal remains stable. This also distinguishes the product from a generic senior launcher.

## D-002: combine chat and search

**Decision:** Talk performs web search only when the question needs current information.

**Reason:** requiring the user to choose “chat” versus “search” exposes a technical distinction that does not help them.

## D-003: explicit family pairing

**Decision:** one trusted person is paired and confirmed during MVP setup.

**Reason:** automatic relationship inference is inaccurate and unnecessarily invasive.

## D-004: no automated WhatsApp calling

**Decision:** MVP supports trusted-chat opening. A callback request is future work requiring a separately authenticated family client; do not promise a direct personal WhatsApp voice/video API.

**Reason:** WhatsApp does not document a public consumer API for silently starting a one-to-one personal call.

## D-005: explain ads; never block or skip automatically

**Decision:** Help announces an advertisement and points out Skip only when exposed.

**Reason:** advertisement detection is heuristic, and interfering with another app’s ads is not a compliant product path.

## D-006: soft installation guard

**Decision:** explain the install screen and offer Home or family assistance.

**Reason:** ordinary consumer apps cannot reliably enforce installation restrictions; managed-device control is a future path.

## D-007: model proposes, policy decides

**Decision:** GPT produces a structured proposal. Deterministic code validates targets, confirmation, and tool policy.

**Reason:** natural-language understanding benefits from a model; external actions require predictable safety behavior.

## D-008: plan and harness before application code

**Decision:** this repository begins with plans, contracts, fixtures, validation, and CI only.

**Reason:** four teammates can implement in parallel once interfaces and non-negotiable safety behavior are shared.
