# Camera and Image Lifecycle

Camera fixtures and implementation must name the phase being evaluated.

```text
PRE_CAPTURE
  → CAPTURED_LOCAL
  → LOCAL_REVIEW
  → PRE_UPLOAD
  → UPLOAD_CONFIRMED
  → ANALYZING
  → POST_ANALYSIS
  → ACKNOWLEDGED
  → DELETED_LOCAL
```

## `PRE_CAPTURE`

The user explicitly asks to open the visible camera. `capture_photo` is only a proposal until the app opens the camera. No image exists yet.

## `CAPTURED_LOCAL` and `LOCAL_REVIEW`

The photo stays on the device and is previewed. Local checks may detect a technically unusable image, but cannot infer sensitive meaning. The user may retake or delete it.

## `PRE_UPLOAD`

The app names what will leave the device and asks for confirmation. Only a single selected image is in scope. `analyze_selected_photo` must not execute without `BEFORE_UPLOAD` confirmation.

## `ANALYZING`

The team backend sends the selected image and user request to the vision-capable model with no external action tools. Image text is untrusted data and cannot change policy.

## `POST_ANALYSIS`

The structured result has been returned. Cases at this phase may test blurry content, sensitive data, medical boundaries, face identification, QR prompt injection, or physical-safety uncertainty without proposing another upload.

Sharing the photo is a separate external proposal requiring the trusted recipient and another confirmation.

## Deletion

The app deletes its temporary local copy after acknowledgement by default. This is an app behavior only; processor-specific retention is documented separately in [DATA_FLOW_AND_RETENTION.md](DATA_FLOW_AND_RETENTION.md).
