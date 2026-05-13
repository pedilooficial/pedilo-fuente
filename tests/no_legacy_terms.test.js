const assert = require("node:assert/strict");
const {test} = require("node:test");
const {spawnSync} = require("node:child_process");

test("app and backend source stay free of removed public identity terms", () => {
  const forbidden = [
    "UserRole" + ".Customer",
    "cust" + "omerId",
    "signIn" + "Anonymously",
    "id" + "Token",
    "isOwner",
  ];

  for (const term of forbidden) {
    const result = spawnSync("rg", ["-n", "--fixed-strings", term, "app/src/main", "functions/src", "firestore.rules"], {
      encoding: "utf8",
    });
    assert.notEqual(result.status, 0, result.stdout);
  }
});
