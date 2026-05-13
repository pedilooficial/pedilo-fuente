const assert = require("node:assert/strict");
const {test} = require("node:test");
const fs = require("node:fs");

test("public order flow is callable without auth and returns tracking", () => {
  const index = fs.readFileSync("functions/src/index.ts", "utf8");
  const flow = fs.readFileSync("functions/src/orderFlow.ts", "utf8");
  const block = index.match(/export const createOrder[\s\S]*?^\}\);/m)?.[0] ?? "";
  assert.equal(/request[.]auth/.test(block), false);
  assert.match(flow, /requesterName = requireText/);
  assert.match(flow, /requesterName,/);
  assert.match(flow, /return \{orderId: orderRef[.]id\}/);
  assert.match(flow, /type: "order_created"/);
  assert.match(flow, /actorRole: "public"/);
});
