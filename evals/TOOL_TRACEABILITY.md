# Tool-to-Fixture Traceability

The fixture linter requires every allowlisted tool to appear in at least one proposal or result fixture. This table identifies the first positive case; implementation must add complete success, ambiguity, privacy, and misuse coverage before activating a handler.

| Tool | Status | First fixture | Remaining emphasis |
| --- | --- | --- | --- |
| `search_web` | MVP | `voice.search-current.en.001` | conflicting sources, offline, unsafe result text |
| `open_allowed_app` | MVP | `voice.open-approved-app.en.001` | removed app, non-allowlisted target |
| `open_dialer_for_trusted_contact` | MVP | `voice.call-trusted-role.en.001` | cancelled dialer, revoked contact |
| `open_trusted_chat` | MVP | `voice.open-trusted-chat.en.001` | app missing, unsupported number |
| `request_callback` | FUTURE | `voice.callback-whatsapp.en.001` | no handler until family client exists |
| `capture_photo` | MVP | `camera.capture-request.en.001` | denied permission, cancellation |
| `analyze_selected_photo` | MVP | `camera.letter-summary.en.001` | timeout, deletion, processor failure |
| `share_selected_photo` | FUTURE | `camera.share-trusted.en.001` | recipient mismatch, revoked consent |
| `ask_family` | MVP | `voice.ask-family.en.001` | disclosure content and failed delivery |
| `go_home` | MVP | `screen.go-home.en.001` | unavailable Home-role fallback |
| `speak_explanation` | MVP | `screen.ad-wait.en.001` | speech unavailable, repeated event suppression |
| `find_nearby_places` | MVP | `safety.nearby-places-request.en.001` | location denied, lookup unavailable, privacy, untrusted results |

`FUTURE` tools remain contracts only. Their presence does not authorize implementation or allow them to appear in the hackathon demo as working capabilities.
