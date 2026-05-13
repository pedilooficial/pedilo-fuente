const assert = require("node:assert/strict");
const {test} = require("node:test");
const fs = require("node:fs");

test("operator roles are explicit and protected", () => {
  const roles = fs.readFileSync("functions/src/roles.ts", "utf8");
  const userRole = fs.readFileSync("app/src/main/java/com/pedilo/app/domain/UserRole.kt", "utf8");
  assert.match(roles, /role !== "store" && role !== "driver" && role !== "admin"/);
  assert.match(userRole, /Store\("store"\)/);
  assert.match(userRole, /Driver\("driver"\)/);
  assert.match(userRole, /Admin\("admin"\)/);
});
