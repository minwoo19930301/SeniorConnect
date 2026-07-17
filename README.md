# An Agent for Elders

**An Agent for Elders** is an idea for a simple Android app that helps older people use a phone with less confusion.

The app gives one clear answer or one clear next step. When it is unsure, it helps the person contact someone they trust.

This project is still in the planning stage. There is no working app yet.

## Why we want to build it

Phone screens can have small buttons, unclear messages, advertisements, and choices that are easy to press by mistake.

Our goal is not to take control of someone’s phone. Our goal is to make the phone easier to understand while the person stays in control.

## The four main buttons

### Talk

Press the button and speak normally.

The app can have a simple conversation or answer a question. If the question needs new information, such as today’s weather or opening hours, it can search the web and show where the answer came from.

### See

Take a photo of a letter, sign, menu, label, or appliance button.

The app reads it aloud and explains it in simple words. It says when the photo is unclear. It does not identify people or give medical advice.

### Family

Choose one trusted family member during setup.

The app can open the phone dialer or that person’s WhatsApp chat after asking for confirmation. It never searches for family members automatically, invents a phone number, or makes a silent call.

### Help

Ask what a confusing phone screen means.

For example, the app may say:

> This is an advertisement. Your video has not ended. Please wait.

Or:

> This button will install a new app. Did you mean to do that?

The app explains the screen. It does not block advertisements, press buttons, approve payments, or install apps by itself.

## Help is always close

Every part of the app should include these simple choices:

- **Repeat Slowly**
- **Ask Family**
- **Take Me Home**
- **Stop**

## Our safety promise

- The person using the phone makes the final decision.
- Calls, messages, and photo sharing need clear permission.
- The camera only opens when the person asks for it.
- The full contact list is not sent to the AI.
- The app does not make purchases or medical decisions.
- If the app is unsure, it says so instead of guessing.

## What is in this repository

This repository contains plans for the team:

- what the app should do;
- how the four main buttons should work;
- privacy and safety rules;
- a three-day hackathon plan;
- 41 example situations we can use to test the future app.

It contains plans and automated checks only. It does not contain Android or server code yet.

## For teammates

Start here:

1. Read [the product plan](plans/PRODUCT.md).
2. Read [the small first version](plans/MVP.md).
3. Follow [the contributor rules](AGENTS.md).
4. Check [the three-day team plan](plans/THREE_DAY_PLAN.md).

Other useful documents:

- [Safety and privacy](plans/SAFETY_AND_PRIVACY.md)
- [How family pairing works](plans/PAIRING_PROTOCOL.md)
- [Ideas for later](plans/IDEA_BACKLOG.md)
- [Demo plan](plans/DEMO_PLAN.md)
- [Hackathon checklist](plans/SUBMISSION_CHECKLIST.md)

## Check the planning files

If Node.js 20 or newer is installed, run:

```bash
npm test
```

This checks that our example situations and safety rules agree with each other. It does **not** test a real app or call an AI model.

## Project name

Use `an-agent-for-elders` as the name inside code, packages, test files, and documentation. The GitHub repository itself can keep its current name.

## License

[MIT](LICENSE)
