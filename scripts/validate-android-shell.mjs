import { readFileSync } from "node:fs";
import { readdirSync } from "node:fs";

const layoutPath = "app/src/main/res/layout/activity_main.xml";
const stringsPath = "app/src/main/res/values/strings.xml";
const manifestPath = "app/src/main/AndroidManifest.xml";
const activityPath =
  "app/src/main/java/org/seniorconnect/app/MainActivity.java";
const dialingActivityPath =
  "app/src/main/java/org/seniorconnect/app/dialing/DialingActivity.kt";
const dialingPackagePath = "app/src/main/java/org/seniorconnect/app/dialing";

const layout = readFileSync(layoutPath, "utf8");
const strings = readFileSync(stringsPath, "utf8");
const manifest = readFileSync(manifestPath, "utf8");
const activity = readFileSync(activityPath, "utf8");
const dialingActivity = readFileSync(dialingActivityPath, "utf8");
const dialingSources = readdirSync(dialingPackagePath)
  .filter((fileName) => fileName.endsWith(".kt"))
  .map((fileName) => readFileSync(`${dialingPackagePath}/${fileName}`, "utf8"))
  .join("\n");

const expected = [
  ["action_call", "action_call", "CALL"],
  ["action_youtube", "action_youtube", "YOUTUBE"],
  ["action_speak", "action_speak", "SPEAK"],
  ["action_map", "action_map", "MAP"],
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

if (layout.includes("android:onClick")) {
  throw new Error("Wire handlers in code, not with android:onClick in the layout.");
}

const handlerCount = (activity.match(/setOnClickListener/g) ?? []).length;
if (handlerCount !== 4) {
  throw new Error(
    "MainActivity must wire exactly four handlers: call, YouTube, Speak, and map.",
  );
}
if (!activity.includes("R.id.action_call") || !activity.includes("DialingActivity")) {
  throw new Error("The Call tile must open DialingActivity.");
}
if (!activity.includes("R.id.action_youtube") || !activity.includes("YouTubeActivity")) {
  throw new Error("The YouTube tile must open YouTubeActivity.");
}
if (!activity.includes("R.id.action_map") || !activity.includes("MapsActivity")) {
  throw new Error("The Map tile must open the Maps UI screen.");
}
if (!activity.includes("R.id.action_speak") || !activity.includes("SpeechRecognizer")) {
  throw new Error("The Speak tile must open the local voice conversation.");
}

const permissions = manifest.match(/<uses-permission[^>]*android:name="([^"]+)"/g) ?? [];
const allowedPermissions = new Set([
  "android.permission.RECORD_AUDIO",
  "android.permission.INTERNET",
  "android.permission.ACCESS_FINE_LOCATION",
  "android.permission.ACCESS_COARSE_LOCATION",
]);
const found = new Set();
for (const entry of permissions) {
  const name = entry.match(/android:name="([^"]+)"/)[1];
  if (!allowedPermissions.has(name)) {
    throw new Error(`Permission ${name} is not allowed in this phase.`);
  }
  found.add(name);
}
for (const required of allowedPermissions) {
  if (!found.has(required)) {
    throw new Error(`Missing required permission ${required}.`);
  }
}

// PiP must stay unavailable on the video screen.
if (/android:supportsPictureInPicture\s*=\s*"true"/.test(manifest)) {
  throw new Error("Picture-in-picture must not be enabled.");
}

if (!dialingSources.includes("Intent.ACTION_DIAL")) {
  throw new Error("The dialing feature must use ACTION_DIAL, not direct calling.");
}

if (dialingSources.includes("Intent.ACTION_CALL")) {
  throw new Error("The dialing feature must not use ACTION_CALL.");
}

if (!manifest.includes(".dialing.DialingActivity")) {
  throw new Error("AndroidManifest must declare DialingActivity.");
}

if (!manifest.includes(".MapsActivity")) {
  throw new Error("AndroidManifest must declare MapsActivity.");
}

if (!manifest.includes(".YouTubeActivity")) {
  throw new Error("AndroidManifest must declare YouTubeActivity.");
}

// dialingActivity is loaded to ensure the source file exists.
if (!dialingActivity.includes("class DialingActivity")) {
  throw new Error("DialingActivity.kt must define DialingActivity.");
}

console.log(
  "Android shell lint OK: 4 buttons, Call+YouTube+Speak+Map handlers, required permissions, ACTION_DIAL only, no PiP.",
);
