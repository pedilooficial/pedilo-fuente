const assert = require("node:assert/strict");
const {test} = require("node:test");
const fs = require("node:fs");
const {spawnSync} = require("node:child_process");

const adapterPath = "app/src/main/java/com/pedilo/app/core/firebase/FirebasePublicCatalogAdapter.kt";
const mapperPath = "app/src/main/java/com/pedilo/app/core/firebase/FirestoreCatalogMappers.kt";
const seedPath = "tools/seed_public_catalog.js";
const verifyPath = "tools/verify_public_catalog.js";
const publicAppPath = "app/src/main/java/com/pedilo/app/ui/publicuser/PublicApp.kt";
const publicCatalogStatePath = "app/src/main/java/com/pedilo/app/ui/publicuser/PublicCatalogState.kt";
const publicShopSearchPath = "app/src/main/java/com/pedilo/app/ui/publicuser/PublicShopSearch.kt";
const publicLocalPath = "app/src/main/java/com/pedilo/app/ui/publicuser/PublicLocal.kt";

function read(path) {
  return fs.readFileSync(path, "utf8");
}

test("Firebase catalog adapter is read-only and targets catalog collections", () => {
  const source = read(adapterPath);
  assert.match(source, /class FirebasePublicCatalogAdapter/);
  assert.match(source, /PublicCatalogPort/);
  assert.match(source, /collection\(STORES\)[\s\S]*?whereEqualTo\(VISIBLE, true\)[\s\S]*?\.get\(\)[\s\S]*?\.await\(\)/);
  assert.match(source, /collection\(PRODUCTS\)[\s\S]*?whereEqualTo\(VISIBLE, true\)[\s\S]*?whereEqualTo\(AVAILABLE, true\)[\s\S]*?\.get\(\)[\s\S]*?\.await\(\)/);
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

test("read-only catalog verifier only reads the initial catalog scope", () => {
  const source = read(verifyPath);
  assert.match(source, /STORE_ID = "pizzeria-roma"/);
  assert.match(source, /PRODUCT_IDS = \["muzzarella", "napolitana", "empanadas", "gaseosa", "promo-dia"\]/);
  assert.match(source, /collection\("stores"\)\.doc\(STORE_ID\)/);
  assert.match(source, /storeRef\.collection\("products"\)\.get\(\)/);
  assert.doesNotMatch(source, /\.(set|update|delete|add)\(/);
  assert.doesNotMatch(source, /collection\("orders"\)|order_tracking|users|roles/);
});

test("public UI loads real catalog once and keeps failure as a non-crashing state", () => {
  const stateSource = read(publicCatalogStatePath);
  const appSource = read(publicAppPath);

  assert.match(stateSource, /FirebasePublicCatalogAdapter/);
  assert.match(stateSource, /adapter\.getVisibleStores\(\)/);
  assert.match(stateSource, /adapter\.getProductsForStore\(store\.id\)/);
  assert.match(stateSource, /return PublicCatalogState\(isLoading = false\)/);
  assert.match(stateSource, /hasRealCatalog = stores\.isNotEmpty\(\)/);
  assert.doesNotMatch(stateSource, /\.(set|update|delete|add)\(/);

  assert.match(appSource, /LaunchedEffect\(Unit\)/);
  assert.match(appSource, /loadPublicCatalogState\(\)/);
  assert.match(appSource, /catalogState = catalogState/);
});

test("shop search and local screens prefer real catalog before fallback", () => {
  const searchSource = read(publicShopSearchPath);
  const localSource = read(publicLocalPath);

  assert.match(searchSource, /realStoresForQuery\(activeQuery, catalogState\)\.ifEmpty \{ storesForQuery\(activeQuery\) \}/);
  assert.match(searchSource, /if \(!catalogState\.hasRealCatalog\) return emptyList\(\)/);
  assert.match(localSource, /catalogState\.productsByStore\["pizzeria-roma"\]/);
  assert.match(localSource, /if \(realProducts\.isEmpty\(\)\) return productsFor\(category\)/);
  assert.match(localSource, /LocalHero\(catalogState\.romaStore\(\)\)/);
});
