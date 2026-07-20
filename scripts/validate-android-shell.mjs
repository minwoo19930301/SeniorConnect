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

if (manifest.includes("<uses-permission")) {
  // INTERNET is allowed for the Gemini AI integration.
  // All other permissions (CALL_PHONE, READ_CONTACTS, RECORD_AUDIO, etc.) remain forbidden.
  const permissionMatches = manifest.match(/<uses-permission[^>]*android:name="([^"]+)"[^>]*>/g) ?? [];
  const allowedPermissions = ["android.permission.INTERNET"];
  for (const perm of permissionMatches) {
    const nameMatch = perm.match(/android:name="([^"]+)"/);
    const name = nameMatch ? nameMatch[1] : "";
    if (!allowedPermissions.includes(name)) {
      throw new Error(`Forbidden Android permission: ${name}`);
    }
  }
}

if (layout.includes("android:onClick")) {
  throw new Error("Buttons must not use XML onClick handlers.");
}

const handlerCount = (activity.match(/setOnClickListener/g) ?? []).length;
if (handlerCount !== 1 || !activity.includes("R.id.action_call")) {
  throw new Error("Only the Call button may launch the dialing feature.");
}

for (const forbiddenId of ["action_youtube", "action_speak", "action_map"]) {
  if (activity.includes(`R.id.${forbiddenId}`)) {
    throw new Error(`The ${forbiddenId} button must remain behavior-free.`);
  }
}

if (!dialingSources.includes("Intent.ACTION_DIAL")) {
  throw new Error("The dialing feature must use ACTION_DIAL, not direct calling.");
}

if (dialingSources.includes("Intent.ACTION_CALL")) {
  throw new Error("The dialing feature must not use ACTION_CALL.");
}

console.log("Android shell lint OK: 4 home buttons, 0 permissions, Call-only dialing entry.");
