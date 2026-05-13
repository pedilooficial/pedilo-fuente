const assert = require("node:assert/strict");
const {test} = require("node:test");
const fs = require("node:fs");

test("incidents are separate from normal events", () => {
  const events = fs.readFileSync("functions/src/events.ts", "utf8");
  const flow = fs.readFileSync("functions/src/orderFlow.ts", "utf8");
  assert.match(events, /collection\("incidents"\)/);
  assert.match(flow, /writeIncident\(tx, orderRef/);
  assert.match(flow, /writeEvent\(tx, orderRef/);
});
