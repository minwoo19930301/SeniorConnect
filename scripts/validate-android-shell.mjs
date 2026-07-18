import { readFileSync } from "node:fs";

const layoutPath = "app/src/main/res/layout/activity_main.xml";
const stringsPath = "app/src/main/res/values/strings.xml";
const manifestPath = "app/src/main/AndroidManifest.xml";
const activityPath =
  "app/src/main/java/org/seniorconnect/app/MainActivity.java";

const layout = readFileSync(layoutPath, "utf8");
const strings = readFileSync(stringsPath, "utf8");
const manifest = readFileSync(manifestPath, "utf8");
const activity = readFileSync(activityPath, "utf8");

const expected = [
  ["action_call", "action_call", "CALL"],
  ["action_youtube", "action_youtube", "YOUTUBE"],
  ["action_speak", "action_speak", "SPEAK"],
  ["action_camera", "action_camera", "CAMERA"],
];

const buttonBlocks = layout.match(/<Button\b[\s\S]*?\/>/g) ?? [];

if (buttonBlocks.length !== expected.length) {
  throw new Error(
    `Expected exactly ${expected.length} buttons, found ${buttonBlocks.length}.`,
  );
}

for (const [id, stringName, label] of expected) {
  const block = buttonBlocks.find((value) =>
    value.includes(`android:id="@+id/${id}"`),
  );

  if (!block) {
    throw new Error(`Missing button @+id/${id}.`);
  }

  if (!block.includes(`android:text="@string/${stringName}"`)) {
    throw new Error(`Button ${id} must use @string/${stringName}.`);
  }

  const stringPattern = new RegExp(
    `<string\\s+name="${stringName}">${label}<\\/string>`,
  );
  if (!stringPattern.test(strings)) {
    throw new Error(`String ${stringName} must be exactly ${label}.`);
  }
}

if (manifest.includes("<uses-permission")) {
  throw new Error("The UI-only prototype must not request Android permissions.");
}

if (layout.includes("android:onClick") || activity.includes("setOnClickListener")) {
  throw new Error("The four buttons must remain behavior-free in this phase.");
}

console.log("Android shell lint OK: exactly 4 buttons, 0 permissions, 0 handlers.");
