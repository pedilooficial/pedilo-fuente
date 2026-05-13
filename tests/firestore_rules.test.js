const assert = require("node:assert/strict");
const {test} = require("node:test");
const fs = require("node:fs");

test("rules protect operator data and reject direct order writes", () => {
  const rules = fs.readFileSync("firestore.rules", "utf8");
  assert.match(rules, /match \/users\/\{userId\}/);
  assert.match(rules, /operatorRole\(\) in \["store", "driver", "admin"\]/);
  assert.match(rules, /match \/orders\/\{orderId\}/);
  assert.match(rules, /allow create, update, delete: if false/);
  assert.doesNotMatch(rules, /isOwner/);
  assert.doesNotMatch(rules, new RegExp("cust" + "omer", "i"));
});
