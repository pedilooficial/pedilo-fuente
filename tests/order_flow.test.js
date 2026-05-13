const assert = require("node:assert/strict");
const {test} = require("node:test");
const fs = require("node:fs");

test("state transitions are centralized and evented", () => {
  const flow = fs.readFileSync("functions/src/orderFlow.ts", "utf8");
  assert.match(flow, /const transitions/);
  assert.match(flow, /throw new HttpsError\("failed-precondition", "Transición inválida/);
  assert.match(flow, /writeEvent\(tx, orderRef/);
});
