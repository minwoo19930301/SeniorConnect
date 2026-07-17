import { readFile } from "node:fs/promises";
import path from "node:path";

const root = process.cwd();
const errors = [];
const seenIds = new Set();
const coveredTools = new Set();
let totalCases = 0;

const readJson = async (relativePath) => {
  try {
    return JSON.parse(await readFile(path.join(root, relativePath), "utf8"));
  } catch (error) {
    errors.push(`${relativePath}: invalid JSON (${error.message})`);
    return null;
  }
};

const manifest = await readJson("evals/manifest.json");
const toolPolicy = await readJson("contracts/tool-policy.json");

for (const schemaPath of [
  "contracts/agent-proposal.schema.json",
  "contracts/confirmation-receipt.schema.json",
  "contracts/eval-case.schema.json",
  "contracts/image-analysis.schema.json",
  "contracts/openai-proposal-transport.schema.json",
  "contracts/screen-state.schema.json",
  "contracts/tool-result.schema.json",
  "contracts/web-answer.schema.json"
]) {
  await readJson(schemaPath);
}

if (!manifest || !toolPolicy) finish();

const allowedTools = new Set(Object.keys(toolPolicy.allowed_tools ?? {}));
const forbiddenTools = new Set(toolPolicy.forbidden_tools ?? []);
const validSuites = new Set(["voice_intent", "camera_assist", "screen_state", "safety_confirmation"]);
const validPolicies = new Set(["ALLOW", "CONFIRM", "CLARIFY", "DENY"]);
const validConfirmations = new Set(["NONE", "ELDER", "EXPLICIT_REQUEST", "BEFORE_UPLOAD"]);
const validIntents = new Set([
  "RESPOND",
  "SEARCH_WEB",
  "EXPLAIN_SELECTED_PHOTO",
  "OPEN_ALLOWED_APP",
  "OPEN_DIALER_FOR_TRUSTED_CONTACT",
  "OPEN_TRUSTED_CHAT",
  "REQUEST_CALLBACK",
  "EXPLAIN_SCREEN",
  "ASK_FAMILY",
  "CLARIFY",
  "REFUSE"
]);

const manifestedSuites = (manifest.suites ?? []).map((suite) => suite.id);
for (const suite of validSuites) {
  const count = manifestedSuites.filter((candidate) => candidate === suite).length;
  if (count !== 1) errors.push(`evals/manifest.json: suite ${suite} must appear exactly once, found ${count}`);
}

for (const tool of allowedTools) {
  const rule = toolPolicy.allowed_tools?.[tool];
  if (forbiddenTools.has(tool)) errors.push(`contracts/tool-policy.json: ${tool} cannot be both allowed and forbidden`);
  if (!new Set(["MVP", "FUTURE"]).has(rule?.status)) errors.push(`contracts/tool-policy.json: ${tool} has invalid status`);
  if (!validConfirmations.has(rule?.confirmation)) errors.push(`contracts/tool-policy.json: ${tool} has invalid confirmation`);
  if (typeof rule?.external_action !== "boolean") errors.push(`contracts/tool-policy.json: ${tool} needs external_action boolean`);
}

for (const suite of manifest.suites ?? []) {
  if (!validSuites.has(suite.id)) {
    errors.push(`evals/manifest.json: unknown suite ${suite.id}`);
    continue;
  }

  let raw;
  try {
    raw = await readFile(path.join(root, suite.path), "utf8");
  } catch (error) {
    errors.push(`${suite.path}: cannot read (${error.message})`);
    continue;
  }

  const cases = [];
  const lines = raw.split(/\r?\n/);

  for (let index = 0; index < lines.length; index += 1) {
    const line = lines[index].trim();
    if (!line) continue;

    const location = `${suite.path}:${index + 1}`;
    let item;
    try {
      item = JSON.parse(line);
    } catch (error) {
      errors.push(`${location}: invalid JSON (${error.message})`);
      continue;
    }

    cases.push(item);
    totalCases += 1;
    validateCase(item, location, suite.id);
  }

  if (cases.length < suite.min_cases) {
    errors.push(`${suite.path}: expected at least ${suite.min_cases} cases, found ${cases.length}`);
  }

  const tags = new Set(cases.flatMap((item) => item.tags ?? []));
  for (const tag of suite.required_tags ?? []) {
    if (!tags.has(tag)) errors.push(`${suite.path}: missing required tag ${tag}`);
  }
}

for (const tool of allowedTools) {
  if (!coveredTools.has(tool)) errors.push(`contracts/tool-policy.json: ${tool} has no proposed or executed fixture coverage`);
}

finish();

function validateCase(item, location, expectedSuite) {
  const required = ["schema_version", "id", "suite", "locale", "context", "input", "expected", "tags"];
  for (const key of required) {
    if (!(key in item)) errors.push(`${location}: missing ${key}`);
  }

  if (item.schema_version !== "1.0") errors.push(`${location}: schema_version must be 1.0`);
  if (item.suite !== expectedSuite) errors.push(`${location}: suite must be ${expectedSuite}`);
  if (!/^[a-z0-9_.-]+$/.test(item.id ?? "")) errors.push(`${location}: invalid id`);
  if (seenIds.has(item.id)) errors.push(`${location}: duplicate id ${item.id}`);
  seenIds.add(item.id);
  if (!/^[a-z]{2}-[A-Z]{2}$/.test(item.locale ?? "")) errors.push(`${location}: invalid locale`);
  if (!item.context || typeof item.context !== "object" || Array.isArray(item.context)) errors.push(`${location}: context must be an object`);
  if (!item.input || typeof item.input !== "object" || Array.isArray(item.input)) errors.push(`${location}: input must be an object`);
  if (!Array.isArray(item.tags) || item.tags.length === 0) errors.push(`${location}: tags must be a non-empty array`);

  if (expectedSuite === "camera_assist") {
    const validCameraPhases = new Set(["PRE_CAPTURE", "PRE_UPLOAD", "POST_ANALYSIS"]);
    if (!validCameraPhases.has(item.input?.phase)) errors.push(`${location}: camera fixture needs a valid input.phase`);
  }

  const expected = item.expected ?? {};
  for (const key of ["intent", "policy", "confirmation", "proposed_tools", "executed_tools", "max_spoken_words", "must_not_claim_success"]) {
    if (!(key in expected)) errors.push(`${location}: expected.${key} is required`);
  }

  if (!validIntents.has(expected.intent)) errors.push(`${location}: invalid expected.intent`);
  if (!validPolicies.has(expected.policy)) errors.push(`${location}: invalid expected.policy`);
  if (!validConfirmations.has(expected.confirmation)) errors.push(`${location}: invalid expected.confirmation`);
  if (!Array.isArray(expected.proposed_tools)) errors.push(`${location}: expected.proposed_tools must be an array`);
  if (!Array.isArray(expected.executed_tools)) errors.push(`${location}: expected.executed_tools must be an array`);
  if (typeof expected.must_not_claim_success !== "boolean") errors.push(`${location}: must_not_claim_success must be boolean`);
  if (!Number.isInteger(expected.max_spoken_words) || expected.max_spoken_words < 1 || expected.max_spoken_words > 60) {
    errors.push(`${location}: max_spoken_words must be an integer from 1 to 60`);
  }

  const proposedTools = expected.proposed_tools ?? [];
  const executedTools = expected.executed_tools ?? [];
  const allExpectedTools = [...proposedTools, ...executedTools];

  for (const tool of allExpectedTools) {
    coveredTools.add(tool);
    if (!allowedTools.has(tool)) errors.push(`${location}: tool ${tool} is not allowlisted`);
    if (forbiddenTools.has(tool)) errors.push(`${location}: forbidden tool ${tool} cannot be expected`);
  }

  for (const tool of proposedTools) {
    const rule = toolPolicy.allowed_tools?.[tool];
    if (rule && rule.confirmation !== expected.confirmation) {
      errors.push(`${location}: tool ${tool} requires exactly ${rule.confirmation} confirmation, found ${expected.confirmation}`);
    }
    if (rule?.confirmation !== "NONE" && expected.policy !== "CONFIRM") {
      errors.push(`${location}: tool ${tool} requiring confirmation must use CONFIRM policy`);
    }
  }

  if (expectedSuite === "camera_assist" && item.input?.phase === "PRE_CAPTURE" && !proposedTools.includes("capture_photo")) {
    errors.push(`${location}: PRE_CAPTURE must propose capture_photo`);
  }
  if (expectedSuite === "camera_assist" && item.input?.phase === "PRE_UPLOAD") {
    if (!proposedTools.includes("analyze_selected_photo")) errors.push(`${location}: PRE_UPLOAD must propose analyze_selected_photo`);
    if (expected.confirmation !== "BEFORE_UPLOAD") errors.push(`${location}: PRE_UPLOAD requires BEFORE_UPLOAD confirmation`);
  }

  for (const tool of expected.forbidden_tools ?? []) {
    if (!forbiddenTools.has(tool) && !allowedTools.has(tool)) {
      errors.push(`${location}: unknown forbidden_tools entry ${tool}`);
    }
  }

  const contactTools = new Set(["open_dialer_for_trusted_contact", "open_trusted_chat", "request_callback", "ask_family", "share_selected_photo"]);
  if (allExpectedTools.some((tool) => contactTools.has(tool))) {
    const targetId = expected.target_id ?? item.input?.tool_result?.target_id;
    const trustedIds = new Set((item.context?.trusted_contacts ?? []).map((contact) => contact.id));
    if (!/^contact_[a-z0-9_]+$/.test(targetId ?? "")) errors.push(`${location}: trusted-contact action requires a contact_* target_id`);
    if (!trustedIds.has(targetId)) errors.push(`${location}: target ${targetId} is not in context.trusted_contacts`);
  }

  const appTools = new Set(["open_allowed_app", "capture_photo", "go_home"]);
  if (allExpectedTools.some((tool) => appTools.has(tool))) {
    const targetId = expected.target_id;
    const allowedAppIds = new Set(item.context?.allowed_apps ?? []);
    if (!/^app_[a-z0-9_]+$/.test(targetId ?? "")) errors.push(`${location}: app action requires an app_* target_id`);
    if (!allowedAppIds.has(targetId)) errors.push(`${location}: target ${targetId} is not in context.allowed_apps`);
  }

  const serialized = JSON.stringify(item);
  const secretPatterns = [
    [/\bsk-[A-Za-z0-9_-]{12,}\b/, "API key"],
    [/[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}/i, "email address"],
    [/(?:\+?\d[ .()-]*){8,}/, "phone-like number"],
    [/data:image\/[a-z]+;base64,/i, "base64 image"]
  ];
  for (const [pattern, label] of secretPatterns) {
    if (pattern.test(serialized)) errors.push(`${location}: fixture contains a ${label}`);
  }

  if ((item.tags ?? []).includes("prompt_injection")) {
    const externalTools = allExpectedTools.filter((tool) => toolPolicy.allowed_tools?.[tool]?.external_action);
    if (externalTools.length > 0) errors.push(`${location}: prompt-injection case cannot expect an external-action tool`);
  }

  if ((item.tags ?? []).includes("high_stakes")) {
    const externalTools = allExpectedTools.filter((tool) => toolPolicy.allowed_tools?.[tool]?.external_action);
    if (externalTools.length > 0) errors.push(`${location}: high-stakes case cannot expect an external-action tool`);
  }

  const toolResult = item.input?.tool_result;
  if (toolResult) {
    if (!executedTools.includes(toolResult.tool)) errors.push(`${location}: tool_result.tool must appear in executed_tools`);
    if (toolResult.status === "success" && expected.must_not_claim_success !== false) {
      errors.push(`${location}: successful tool-result case should permit a narrow proven success acknowledgement`);
    }
    if (toolResult.status !== "success" && expected.must_not_claim_success !== true) {
      errors.push(`${location}: non-success tool result must forbid success claims`);
    }
  } else {
    if (executedTools.length > 0) errors.push(`${location}: executed_tools requires an input.tool_result fixture`);
    if (expected.must_not_claim_success !== true) errors.push(`${location}: success claims require a matching successful tool result`);
  }
}

function finish() {
  if (errors.length > 0) {
    console.error(`Fixture lint failed with ${errors.length} error(s):`);
    for (const error of errors) console.error(`- ${error}`);
    process.exit(1);
  }

  console.log(`Fixture lint OK: ${totalCases} cases across ${manifest.suites.length} suites.`);
}
