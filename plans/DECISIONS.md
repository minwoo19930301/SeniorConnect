# Product Decisions

## D-001: keep the home screen to four clear actions

**Decision:** the four primary actions are Call, YouTube, Speak, and Map.

**Reason:** these labels match the tasks the user asked for and remain readable on a simple four-button screen.

## D-002: combine chat and search

**Decision:** Speak performs web search only when the question needs current information.

**Reason:** requiring the user to choose “chat” versus “search” exposes a technical distinction that does not help them.

## D-003: explicit family pairing

**Decision:** one trusted person is paired and confirmed during MVP setup.

**Reason:** automatic relationship inference is inaccurate and unnecessarily invasive.

## D-004: no automated WhatsApp calling

**Decision:** MVP supports trusted-chat opening. A callback request is future work requiring a separately authenticated family client; do not promise a direct personal WhatsApp voice/video API.

**Reason:** WhatsApp does not document a public consumer API for silently starting a one-to-one personal call.

## D-005: explain ads; never block or skip automatically

**Decision:** YouTube guidance announces an advertisement and points out Skip only when exposed.

**Reason:** advertisement detection is heuristic, and interfering with another app’s ads is not a compliant product path.

## D-006: soft installation guard

**Decision:** explain the install screen and offer Home or family assistance.

**Reason:** ordinary consumer apps cannot reliably enforce installation restrictions; managed-device control is a future path.

## D-007: model proposes, policy decides

**Decision:** GPT produces a structured proposal. Deterministic code validates targets, confirmation, and tool policy.

**Reason:** natural-language understanding benefits from a model; external actions require predictable safety behavior.

## D-010: live nearby places require explicit location permission

**Decision:** opening Map requests Android location permission. If granted, the
app uses device coordinates only for the active lookup, shows the locality
through Android Geocoder, and finds nearby hospitals, bus stops, and
supermarkets through OpenStreetMap's public Overpass service.

**Reason:** the person explicitly opens Map. The screen works with approximate
location, shows an honest unavailable state and retry option, and does not
retain location history. Directions remain future work.

## D-008: plan and harness before application code

**Decision:** this repository begins with plans, contracts, fixtures, validation, and CI only.

**Reason:** four teammates can implement in parallel once interfaces and non-negotiable safety behavior are shared.

## D-009: begin with a UI-only Android shell

**Decision:** the first implementation is one native Android screen with Call,
YouTube, Speak, and Camera. The buttons have no behavior and the app requests
no permissions.

**Reason:** the team can validate the most important accessibility choice—the
simple home screen—before adding integrations or agent behavior.
