const assert = require("node:assert/strict");
const {test} = require("node:test");
const fs = require("node:fs");
const {spawnSync} = require("node:child_process");

const adapterPath = "app/src/main/java/com/pedilo/app/core/firebase/FirebasePublicCatalogAdapter.kt";
const mapperPath = "app/src/main/java/com/pedilo/app/core/firebase/FirestoreCatalogMappers.kt";
const seedPath = "tools/seed_public_catalog.js";

function read(path) {
  return fs.readFileSync(path, "utf8");
}

test("Firebase catalog adapter is read-only and targets catalog collections", () => {
  const source = read(adapterPath);
  assert.match(source, /class FirebasePublicCatalogAdapter/);
  assert.match(source, /PublicCatalogPort/);
  assert.match(source, /collection\(STORES\)\.get\(\)\.await\(\)/);
  assert.match(source, /collection\(PRODUCTS\)[\s\S]*?\.get\(\)[\s\S]*?\.await\(\)/);
  assert.doesNotMatch(source, /\.(set|update|delete|add)\(/);
  assert.doesNotMatch(source, /collection\("orders"\)|collection\(ORDERS\)/);
});

test("Firestore catalog mappers filter visibility and optional availability", () => {
  const source = read(mapperPath);
  assert.match(source, /toPublicStoreSummaryOrNull/);
  assert.match(source, /toPublicProductSummaryOrNull/);
  assert.match(source, /optionalBoolean\("visible"\) != true/);
  assert.match(source, /optionalBoolean\("operational"\) == false/);
  assert.match(source, /optionalBoolean\("acceptsOrders"\) == false/);
  assert.match(source, /optionalBoolean\("available"\) == false/);
  assert.match(source, /priceCents/);
  assert.doesNotMatch(source, /FirebasePublicCatalogAdapter/);
});

test("seed script uses stable catalog ids and requires explicit confirmation", () => {
  const source = read(seedPath);
  assert.match(source, /PEDILO_CONFIRM_SEED/);
  assert.match(source, /REQUIRED_CONFIRMATION = "YES"/);
  assert.match(source, /STORE_ID = "pizzeria-roma"/);
  for (const id of ["muzzarella", "napolitana", "empanadas", "gaseosa", "promo-dia"]) {
    assert.match(source, new RegExp(`id: "${id}"`));
  }
  assert.doesNotMatch(source, /collection\("orders"\)/);
  assert.doesNotMatch(source, /order_tracking|users|roles/);
});

test("seed script stays in safe mode before loading Firebase Admin", () => {
  const source = read(seedPath);
  assert.match(source, /process\.env\[CONFIRM_ENV\] !== REQUIRED_CONFIRMATION/);
  assert.ok(
    source.indexOf("process.env[CONFIRM_ENV] !== REQUIRED_CONFIRMATION") <
      source.indexOf('require("firebase-admin")'),
  );
  assert.match(source, /no se escribe catálogo/);
});

test("public UI does not expose removed sample labels", () => {
  const forbidden = ["de" + "mo", "mo" + "ck"];
  for (const term of forbidden) {
    const result = spawnSync("rg", ["-n", "-i", `\\b${term}\\b`, "app/src/main/java/com/pedilo/app/ui/publicuser"], {
      encoding: "utf8",
    });
    assert.notEqual(result.status, 0, result.stdout);
  }
});
