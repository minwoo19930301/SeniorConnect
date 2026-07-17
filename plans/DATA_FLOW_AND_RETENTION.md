# Data Flow and Retention Plan

This plan separates device storage, the team’s backend, OpenAI processing, web-search sources, and future third parties. “Delete locally” does not imply a processor retained nothing.

## Processor inventory

| Flow | Data sent | Processor | Planned storage choice | User control |
| --- | --- | --- | --- | --- |
| Talk without search | transcript and minimal preferences | Team backend → OpenAI Responses API | Request with `store: false`; no app conversation log by default | Stop, clear local session |
| Talk with search | query, time/place context | Team backend → OpenAI and retrieved web sources | No app query history by default; source links shown | Search disclosure, clear session |
| See | one user-selected image and request | Team backend → OpenAI image input | Upload only after confirmation; no app image retention by default | Preview, cancel, delete |
| Family | opaque trusted-contact ID in model context | Team backend → OpenAI | Raw number stays on device | Revoke trusted contact |
| Dialer | trusted number | Android dialer | Governed by device/dialer behavior | Cancel before opening |
| Help | sanitized state enum and preferences | Team backend → OpenAI | No raw accessibility tree or screenshot | Disable Help/accessibility permission |
| Future callback | target ID and request metadata | Future backend/push provider | Undefined until protocol review | Out of MVP |

## OpenAI configuration

- Set `store: false` for Responses API requests.
- Do not use background mode for the MVP.
- Do not treat `store: false` as a promise of zero processing or zero abuse-monitoring retention.
- Review the live data-controls documentation and organization settings before launch.
- Do not add a remote MCP or another processor without updating this inventory.

OpenAI documents that Responses application state is stored by default and that `store: false` disables stored state; other controls and retention categories depend on account configuration and feature use. See [OpenAI data controls](https://developers.openai.com/api/docs/guides/your-data#v1responses).

## Device storage

Store only:

- language, text size, and speech rate;
- one encrypted trusted-contact record;
- approved channel and app IDs;
- consent settings;
- non-content error counters if needed.

Do not store by default:

- raw audio or transcript history;
- selected photos after a response;
- search history;
- accessibility trees or screenshots;
- message contents;
- full contact lists.

## Team backend

- Keep the OpenAI key server-side and out of the Android bundle.
- Avoid request/response body logging.
- Redact authorization headers and uploaded media from error reporting.
- Use short request IDs that cannot reveal a contact or device identity.
- Define an explicit deletion path before retaining any user content.

## Claims checklist

Before public copy says “not stored,” “private,” “deleted,” or “not used for training,” verify the statement separately for:

1. the Android device;
2. team backend and logging vendors;
3. OpenAI account/data-control configuration;
4. web-search or third-party providers;
5. crash analytics and push services.

If verification is incomplete, use precise limited wording rather than an absolute claim.
