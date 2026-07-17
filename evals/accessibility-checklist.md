# Accessibility Review Checklist

Run this checklist with an older adult or low-literacy participant whenever possible. Do not claim user validation without real participation and consent.

## Language and speech

- [ ] One idea per spoken message.
- [ ] Normally 20 spoken words or fewer before a pause or user choice.
- [ ] One question at a time.
- [ ] No jargon such as API, intent, model, authentication, or permission scope in user copy.
- [ ] `Repeat Slowly`, `Stop`, `Ask Family`, and `Take Me Home` are consistently named.
- [ ] Speech does not claim an uncompleted action succeeded.
- [ ] Uncertainty is stated plainly.

## Visual interface

- [ ] Four primary buttons are distinguishable by text, icon, and color—not color alone.
- [ ] Touch targets are at least 48 dp and separated.
- [ ] Text remains usable at Android’s largest supported font size.
- [ ] Contrast meets WCAG AA guidance.
- [ ] Focus order and accessibility labels are logical.
- [ ] Loading, listening, camera, upload, and cancellation states are visible.
- [ ] Confirmations name the person, channel, app, or data being shared.

## Control and recovery

- [ ] Back and Home paths work without model access.
- [ ] Permission denial does not trap or repeatedly pressure the user.
- [ ] Every external action can be cancelled before execution.
- [ ] A failed tool has a clear retry or human-help path.
- [ ] Unknown screens produce uncertainty, not invented instructions.

## Privacy

- [ ] Camera and microphone activity is visible.
- [ ] A selected photo is previewed before upload or sharing.
- [ ] No full contact list, raw screen tree, or message history is collected.
- [ ] Logs exclude raw speech, photos, messages, and contact data.
- [ ] Pairing and revocation are understandable to both people.

## Language expansion

- [ ] Copy is reviewed by a fluent speaker.
- [ ] The translated speech remains short and respectful.
- [ ] Dates, numbers, names, and relationship terms are tested aloud.
- [ ] Right-to-left layout is tested when applicable.
