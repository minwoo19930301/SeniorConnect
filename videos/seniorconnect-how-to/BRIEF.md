---
workflow: faceless-explainer
flow: automation
storyboard: yes
message: "A crowded phone becomes four clear choices: Call, YouTube, Speak, and Camera, while the older adult stays in control."
destination: youtube-shorts
aspect: 1080x1920
language: en
audience: "hackathon judges, new teammates, older adults, and their families"
length: 35-45s
angle: cinematic problem-solution trailer
narration: yes
captions: yes
---

## Intent

Create a short, cinematic problem-to-solution trailer for the planned
`seniorconnect` app. Begin with an older adult facing a phone crowded by
apps, small controls, advertisements, and unexpected install prompts. Then show
one simple raster home screen with exactly four actions: Call, YouTube, Speak,
and Camera. Demonstrate a confirmed family call and clear YouTube ad guidance.

## Assets

- `assets/ui/planned-home-screen.png` — the raster README concept image and the
  visual source of truth for the planned home screen.

## Customizations

- Use clear English narration, readable on-screen captions, fast trailer-style
  cuts, strong title beats, phone close-ups, and restrained impact sound design.
- Use the same labels and safety boundaries as the README.
- Keep sound effects below the narration so every instruction remains easy to hear.
- Show a complete story: crowded phone, one simple home screen, the four clear
  actions, a confirmed family call, and help with a confusing advertisement.
- Do not show a product logo, app logo, profile name, UI placeholder slot, or
  video placeholder slot.

## Notes

- This repository contains plans and checks only. The app is not implemented.
- Label the still and video as a concept. Never imply that a feature was tested
  in a working Android app.
- Do not show automatic family discovery, silent calls, ad blocking, purchases,
  medical advice, or cross-app button pressing.
- A trusted family contact is selected during setup and every call requires
  confirmation. Do not invent a person's name.
- “Connect apps” means the user or helper chooses which installed apps the
  agent may open through supported links or Android intents. It does not mean
  autonomous control, password access, or silent cross-app actions.
- Deliver a real vertical MP4 suitable for Shorts. HyperFrames is the production
  and rendering engine, not the user-facing deliverable.
