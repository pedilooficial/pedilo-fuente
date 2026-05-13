const assert = require("node:assert/strict");
const {test} = require("node:test");
const fs = require("node:fs");

function numberConst(source, name) {
  const match = source.match(new RegExp(`const val ${name} = (\\d+)`));
  assert.ok(match, `Missing ${name}`);
  return Number(match[1]);
}

test("Android public form validation covers required stress cases", () => {
  const source = fs.readFileSync("app/src/main/java/com/pedilo/app/domain/PublicOrderValidation.kt", "utf8");
  assert.match(source, /requesterName\.trim\(\)/);
  assert.match(source, /contactPhone\.trim\(\)/);
  assert.match(source, /deliveryAddress\.trim\(\)/);
  assert.match(source, /itemsText\.trim\(\)/);
  assert.match(source, /note\.trim\(\)/);
  assert.match(source, /Nombre obligatorio/);
  assert.match(source, /Teléfono obligatorio/);
  assert.match(source, /Dirección obligatoria/);
  assert.match(source, /Pedido obligatorio/);
  assert.match(source, /phone\.any \{ !it\.isDigit\(\) \}/);
});

test("Android and Functions public validation limits stay aligned", () => {
  const kotlin = fs.readFileSync("app/src/main/java/com/pedilo/app/domain/PublicOrderValidation.kt", "utf8");
  const ts = fs.readFileSync("functions/src/validators.ts", "utf8");
  const pairs = [
    ["NAME_MAX", "nameMax"],
    ["PHONE_MIN", "phoneMin"],
    ["PHONE_MAX", "phoneMax"],
    ["ADDRESS_MIN", "addressMin"],
    ["ADDRESS_MAX", "addressMax"],
    ["ITEMS_MIN", "itemsMin"],
    ["ITEMS_MAX", "itemsMax"],
    ["NOTE_MAX", "noteMax"],
  ];

  for (const [kotlinName, tsName] of pairs) {
    const kotlinValue = numberConst(kotlin, kotlinName);
    assert.match(ts, new RegExp(`${tsName}: ${kotlinValue}`));
  }
});
