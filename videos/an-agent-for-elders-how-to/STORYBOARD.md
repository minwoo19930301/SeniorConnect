---
format: 1080x1920
message: "A planned elder-friendly home screen replaces phone clutter with four clear actions—Call, YouTube, Speak, and Camera—while the older person stays in control."
arc: cinematic problem-solution trailer
audience: "Shorts viewers, hackathon judges, older adults, and their families"
mode: autonomous
music: none
captions: yes
---

## Video direction

- **Portrait frame:** Compose natively for 1080×1920. Stack information vertically, anchor primary reveals around the upper-middle, and reserve the bottom 17% for captions. Never crop a landscape composition into portrait.
- **Palette and type:** Keep the Blue Professional system: warm cream ground, near-black display text, cobalt as the only high-energy accent, muted gray supporting copy, and softly tinted UI cards. Space Grotesk-style display roles carry trailer titles; the body role carries explanations. No invented colors, logo mark, or fake brand partnership.
- **Home screen:** Use the actual raster PNG at `assets/ui/planned-home-screen.png`, not an HTML/SVG reconstruction, abstract app icon, or screenshot from a finished app. Its only four main action labels are **Call**, **YouTube**, **Speak**, and **Camera**.
- **Trailer grammar:** Use hard typographic cuts, vertical pushes, motion-blur arrivals, and two decisive bass-impact moments. Every reveal follows its spoken phrase into the back half of the shot. Entrances settle on smooth long-tail motion; no bounce, elastic motion, or simultaneous card dump.
- **Rhythm:** Frame 1 escalates into crowding; Frame 2 creates relief with one clean raster home screen; Frame 3 gives the four actions one beat each; Frame 4 chases the Call flow and holds on confirmation; Frame 5 explains a YouTube ad and an install warning; Frame 6 closes on the same four labels in a fully still grid.
- **Continuity:** A persistent small “CONCEPT TRAILER” rail and the same reconstructed phone shell connect the scenes. The phone moves from overwhelmed, to simple, to useful. All important content stays above the caption keep-out band.
- **Never show:** a fifth home-screen action, an invented person name, a project logo mark, a real YouTube logo or partnership, automatic family discovery, password access, silent calls, ad blocking, cross-app tapping, a completed install, childish elder stereotypes, slideshow front-loading, or screensaver-like independent floating.

## Frame 1 — The maze

- scene: A tall phone fills the portrait frame as app tiles, notification badges, tiny prompts, and a game-install dialog rapidly crowd around one simple request: “CALL.” A persistent “Concept trailer” label remains visible.
- voiceover: "Too many apps. Tiny buttons. One wrong tap—and a simple phone becomes a maze."
- duration: 5.312s
- poster: 4.2s
- transition_in: cut
- status: animated
- src: compositions/frames/01-the-maze.html
- type: pain_point
- persuasion: Pain validation + escalating accumulation
- beat: recognition and tension
- blueprint: overwhelm-surround (Adapt)
- focal: a reconstructed phone that reveals a dignified older-adult silhouette beneath the clutter
- roles: phone and silhouette = foreground subject · app tiles and prompts = surrounding demands · giant “MAZE” letterforms = background structure · “CALL” request = supporting human goal
- sfx: notification, glitch-1, impact-bass-1

narrativeRole: Opens like a trailer with the older adult's real problem, letting phone clutter physically overwhelm the frame before the simple home screen appears.

keyMessage: Ordinary phone tasks become difficult when every app and prompt competes for attention.

Adapt: Keep the blueprint's subject-reveal and radial close-in signature. Change generic product mockups into one portrait phone, and reveal a simple older-adult silhouette beneath it so the person—not the technology—becomes the center of the problem.

Scene 1 (0.0–1.1s): “TOO MANY APPS” arrives in a per-word staggered reveal (`dynamic-content-sequencing`) above a tall phone; three recognizable home-screen panels assemble below it. Layered-depth portrait framing fills the top 70% while the caption band remains clear.

Scene 2 (1.1–2.3s): As the voice says “Tiny buttons,” app tiles and notification badges stagger into place via short-path assemble (`center-outward-expansion`); selective blur (`depth-of-field-blur`) keeps the central “CALL” request sharp while density grows around it.

Scene 3 (2.3–3.7s): On “One wrong tap,” a generic game-install prompt slams over the phone with a motion-blur streak (`motion-blur-streak`) and a bass hit; the screen content then morphs away through the blueprint's card-morph anchor (`card-morph-anchor`) to reveal the older-adult silhouette holding the phone.

Scene 4 (3.7–5.312s): On “becomes a maze,” demand bubbles close in from every compass direction via radial smooth-settle entries (`spring-pop-entrance` in restrained register) while the silhouette stays fixed. The giant word “MAZE” resolves behind the person and the crowded state holds still—no camera push and no breathing.

## Frame 2 — One clear home screen

- scene: The clutter clears to warm cream. A full portrait phone resolves into a simple flattened raster-style home screen with four large tiles labeled Call, YouTube, Speak, and Camera, plus a small “CONCEPT SCREEN” note.
- voiceover: "Now imagine one simple home screen, built for clarity instead of clutter."
- duration: 4.949s
- poster: 5s
- transition_in: zoom-through
- status: animated
- src: compositions/frames/02-simple-home-screen.html
- type: product_intro
- persuasion: Before-and-after + visual relief
- beat: relief and recognition
- blueprint: grid-card-assemble (Adapt)
- focal: the actual local raster PNG `assets/ui/planned-home-screen.png` with exactly four large action tiles
- roles: `assets/ui/planned-home-screen.png` = foreground hero image · Call / YouTube / Speak / Camera tiles inside the image = primary controls · “CONCEPT SCREEN” note = supporting truthfulness · cream field and cobalt rule = background
- sfx: whoosh-cinematic, click-soft, chime

narrativeRole: Replaces the maze with one immediately readable screen and makes the proposed interface concrete without pretending the app is already built.

keyMessage: The planned experience starts from one simple screen instead of a crowded phone launcher.

Adapt: Keep the blueprint's accumulating-card structure, but assemble a single flattened raster-style phone surface. The four large tiles are the complete home screen; no logo mark, fifth action, dock, or hidden menu appears.

Scene 1 (0.0–1.1s): The actual raster PNG `assets/ui/planned-home-screen.png` rises into the upper-middle with a small “CONCEPT SCREEN” label (`motion-blur-streak`, restrained). Do not recreate the interface in HTML or SVG.

Scene 2 (1.1–2.4s): A slow crop move across the already-rendered PNG reveals its four large buttons in reading order. Do not add extra placeholders or HTML tiles.

Scene 3 (2.4–3.8s): Four short callouts—“CALL,” “YOUTUBE,” “SPEAK,” and “CAMERA”—resolve around the single PNG in reading order (`dynamic-content-sequencing`). The image remains unchanged.

Scene 4 (3.8–4.949s): “ONE SIMPLE HOME SCREEN” lands above the PNG as the image holds fully still. The four buttons remain the only available main actions.

## Frame 3 — Four actions

- scene: Call, YouTube, Speak, and Camera take one cinematic beat each, then assemble into the same two-by-two home-screen grid under the line “ONLY FOUR MAIN ACTIONS.”
- voiceover: "Call. YouTube. Speak. Camera. Four clear choices, and nothing more."
- duration: 4.352s
- poster: 4.5s
- transition_in: push-slide UP
- status: animated
- src: compositions/frames/03-four-actions.html
- type: feature_showcase
- persuasion: Progressive disclosure + simplicity proof
- beat: clarity and control
- blueprint: kinetic-type-beats (Adapt)
- focal: the four exact action labels resolving into four large accessible tiles
- roles: Call / YouTube / Speak / Camera words = foreground kinetic beats · matching large tiles = midground payoff · one cobalt progress rule = background structure
- sfx: click-soft, pop, impact-bass-2

narrativeRole: Proves that the home screen is intentionally limited to four understandable choices rather than another dense launcher.

keyMessage: The elder sees four main actions—Call, YouTube, Speak, and Camera—and no extra app clutter.

Adapt: Keep the blueprint's fixed-anchor word replacement and final assembled state. Each beat uses a distinct entrance, but all four settle into one restrained two-by-two grid with no extra buttons.

Scene 1 (0.0–0.9s): “CALL” lands first with a generic phone pictogram and the short supporting label “TRUSTED FAMILY” (`kinetic-beat-slam`). The word remains the clear focal point.

Scene 2 (0.9–2.1s): “YOUTUBE” replaces it through a velocity-matched cut, paired with a generic video rectangle and “EXPLAIN ADS”; “SPEAK” follows with a simple waveform and “ASK OUT LOUD” (`kinetic-beat-slam`, distinct axes).

Scene 3 (2.1–3.3s): “CAMERA” arrives with a plain viewfinder and “READ WHAT I SEE.” The four words then scale-swap into the actual PNG at `assets/ui/planned-home-screen.png` (`scale-swap-transition`).

Scene 4 (3.3–4.352s): The actual raster PNG pulls back just enough to reveal all four buttons at once. “ONLY FOUR MAIN ACTIONS” lands above and the image holds still.

## Frame 4 — Call a trusted family contact

- scene: A full-screen “CALL” command compresses into the Call tile, reveals a generic verified trusted-family-contact card, pauses at “Call this trusted family contact?”, then hands off to a normal phone dialer.
- voiceover: "Choose Call. It shows your trusted family contact, asks you to confirm, then opens the phone dialer."
- duration: 7.083s
- poster: 4.7s
- transition_in: push-slide UP
- status: animated
- src: compositions/frames/04-call-family.html
- type: feature_showcase
- persuasion: Demonstration + visible confirmation
- beat: confidence and connection
- blueprint: cursor-ui-demo (Adapt)
- focal: the Call action becoming a confirmed phone-dialer handoff for a generic trusted contact
- roles: “CALL” phrase and tap halo = initiating actor · Call tile and generic trusted-contact card = midground workflow · confirmation sheet = foreground decision · dialer handoff = payoff surface
- sfx: whoosh-short, click, chime

narrativeRole: Demonstrates the emotional family-contact use case while keeping the contact generic and the identity and confirmation steps visible.

keyMessage: The Call action reaches a previously approved family contact without guessing a name or starting a silent call.

Adapt: Keep the blueprint's camera-chase and responsive UI signature, replacing the tiny desktop cursor with a large accessible tap halo. The chase follows Call to a generic “TRUSTED FAMILY CONTACT” card, then to confirmation, then to the normal dialer.

Scene 1 (0.0–1.3s): “CALL” slams into the upper-middle (`kinetic-beat-slam`) while a simple phone pulse draws below it. Full-width strip framing makes the one chosen action unmistakable.

Scene 2 (1.3–3.1s): The word scales into the home screen's Call tile through a center scale-swap (`scale-swap-transition`); a large tap halo presses the tile with a ripple (`cursor-click-ripple`) and the camera chases downward to a card labeled “TRUSTED FAMILY CONTACT” (`viewport-change`). No name or portrait is shown.

Scene 3 (3.1–5.2s): The camera focus-locks on the verified generic contact, then follows the tap halo to the question “Call this trusted family contact?” (`camera-cursor-tracking`). The relationship label and “APPROVED DURING SETUP” appear before the Call button compresses and releases (`press-release-spring`).

Scene 4 (5.2–7.083s): The confirmation card morphs into a normal dialer surface (`card-morph-anchor`), the camera settles, and “OPENING PHONE DIALER” holds with “NO CALL HAS STARTED.” No dialing or call animation begins.

## Frame 5 — YouTube, explained

- scene: A generic YouTube screen freezes under “THIS IS AN AD. JUST WAIT.” It whips away to a game-install prompt and the warning “THIS MAY INSTALL A GAME.” No button is pressed.
- voiceover: "On YouTube, it explains, ‘This is an ad. Just wait.’ Before an accidental game install, it warns you."
- duration: 7.616s
- poster: 6.2s
- transition_in: blur-crossfade
- status: animated
- src: compositions/frames/05-youtube-explained.html
- type: feature_showcase
- persuasion: Worked scenario + prevention through explanation
- beat: foresight and safety
- blueprint: kinetic-type-beats (Adapt)
- focal: two plain-language safety statements replacing confusing YouTube and install surfaces
- roles: generic YouTube player and install prompt = reconstructed UI subjects · “THIS IS AN AD” and “THIS MAY INSTALL A GAME” = foreground hero phrases · wait counter and untouched buttons = supporting proof
- sfx: whoosh, error, impact-bass-1

narrativeRole: Turns the requested YouTube-ad and accidental-install examples into memorable trailer lines while showing that the app explains what is happening before the elder acts.

keyMessage: A clear explanation can prevent an accidental install while the older person keeps the final choice.

Adapt: Keep the multi-beat type replacement engine, but let each hero phrase emerge directly from the reconstructed surface it explains. Use the plain word “YouTube” only; do not reproduce its logo, visual identity, or suggest a partnership.

Scene 1 (0.0–1.8s): A generic vertical video player labeled “YOUTUBE” establishes in the upper two-thirds; an “Ad · 5 seconds” badge and untouched skip area appear. The camera holds and only the countdown ticks (`discrete-text-sequence`).

Scene 2 (1.8–3.6s): “THIS IS AN AD” flies in with directional blur and resolves sharp (`motion-blur-streak`), then “JUST WAIT” hard-cuts beneath it (`kinetic-beat-slam`). The player dims behind the statement and the read holds briefly.

Scene 3 (3.6–5.2s): A vertical waterfall cut (`cut-catalog.md`) carries the ad explanation upward while a generic game-install card enters from below at matched velocity. Cancel and Install remain visually untouched.

Scene 4 (5.2–7.616s): “THIS MAY INSTALL A GAME” assembles in three strong chunks (`dynamic-content-sequencing`); a cobalt outline circles Cancel (`css-marker-patterns`) without clicking it. “YOU CHOOSE” lands last and holds still, making the prevention mechanism an explanation rather than autonomous control.

## Frame 6 — Four clear actions

- scene: Call, YouTube, Speak, and Camera land one by one as giant vertical title beats, assemble into the simple raster home screen, and finish as a still two-by-two grid with a clear concept-only note. No logo mark appears.
- voiceover: "Call. YouTube. Speak. Camera. Four clear actions, with the older person still in control."
- duration: 5.931s
- poster: 3.9s
- transition_in: zoom-through
- status: animated
- src: compositions/frames/06-four-clear-actions.html
- type: closing
- persuasion: Callback + distillation
- beat: relief and resolve
- blueprint: kinetic-type-beats (Reproduce)
- focal: the same four action labels resolving into the actual raster PNG `assets/ui/planned-home-screen.png`
- roles: Call / YouTube / Speak / Camera = foreground kinetic words · `assets/ui/planned-home-screen.png` = midground payoff image · concept-only note and cobalt progress rule = supporting structure
- sfx: pop, whoosh-cinematic, impact-bass-2, chime

narrativeRole: Closes like a movie trailer by turning the four-button interface into a memorable promise without adding a logo or another feature.

keyMessage: Four clear actions can make a phone easier to use while the older person stays in control.

Scene 1 (0.0–1.7s): “CALL,” “YOUTUBE,” “SPEAK,” and “CAMERA” hard-cut one at a time at a fixed upper-middle anchor (`kinetic-beat-slam`); each word uses a distinct entrance direction and the cobalt background rule advances once per label.

Scene 2 (1.7–3.2s): The four words scale-swap into the actual raster PNG `assets/ui/planned-home-screen.png` (`scale-swap-transition`). Do not recreate its buttons in HTML or SVG. No dock, helper row, or fifth action appears.

Scene 3 (3.2–4.6s): “FOUR CLEAR ACTIONS” reveals in measured chunks above the PNG (`dynamic-content-sequencing`); the four labeled buttons remain visible and stable below it.

Scene 4 (4.6–5.931s): The final PNG holds on Call, YouTube, Speak, and Camera with “OLDER PERSON IN CONTROL” and “CONCEPT DEMO · APP NOT BUILT YET.” There is no project name, app icon, or logo mark.
