# Trusted-Contact Pairing Protocol

## MVP choice

Use an assisted, same-device setup with Android’s system contact picker. Do not build a second family application or QR account-linking flow for the first MVP.

This minimizes infrastructure and avoids making a short-lived demo protocol look production-secure.

## Setup flow

1. The elder deliberately enters **Family setup**.
2. The app says what will be selected and that it will not read or upload the full address book.
3. Android’s system picker returns one user-selected contact.
4. The elder or present helper assigns a relationship label and selects allowed channels.
5. The app shows the photo/name and asks: “Is Mina your granddaughter?”
6. Affirmative confirmation creates an opaque local ID such as `contact_mina`.
7. The phone number remains encrypted in app-local storage and is never provided to GPT.
8. A practice action opens the dialer, then returns to setup.

## Use-time validation

- The model receives only opaque contact IDs, display labels, relationships, and approved channels.
- Policy rejects any target absent from the current local trusted-contact set.
- Relationship ambiguity produces a clarification, not a tool proposal.
- A changed or deleted system contact requires re-confirmation before use.
- Revocation removes the trusted mapping and invalidates outstanding confirmations.

## Revocation and device loss

- **Remove family member** is reachable without the model.
- Removal requires a local confirmation but no family approval.
- App data removal or device reset removes the mapping.
- The MVP has no remote family access, so there is no remote session to revoke.

## Future remote pairing

A future callback-request feature would require a separate family client and a reviewed protocol with:

- authenticated accounts on both devices;
- a signed, single-use, short-expiry invitation;
- human verification of relationship and device;
- replay protection and rate limits;
- encrypted transport and secure local key storage;
- mutual revocation and device-loss recovery;
- delivery, read, and failure states;
- tests for stolen QR codes, screenshots, replays, renamed contacts, duplicate roles, and account takeover.

Until that exists, callback delivery must be labeled `FUTURE` or a mock—not a working product capability.
