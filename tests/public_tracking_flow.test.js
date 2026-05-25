const assert = require("node:assert/strict");
const {test} = require("node:test");
const fs = require("node:fs");
const {spawnSync} = require("node:child_process");

const publicApp = "app/src/main/java/com/pedilo/app/ui/publicuser/PublicApp.kt";
const trackingScreen = "app/src/main/java/com/pedilo/app/ui/publicuser/PublicShopTracking.kt";
const adapter = "app/src/main/java/com/pedilo/app/core/firebase/FirebasePublicTrackingAdapter.kt";
const useCase = "app/src/main/java/com/pedilo/app/core/usecase/GetPublicTrackingUseCase.kt";
const model = "app/src/main/java/com/pedilo/app/core/model/PublicTrackingState.kt";
const fn = "functions/index.js";
const rules = "firestore.rules";

function read(path) {
  return fs.readFileSync(path, "utf8");
}

test("tracking use case rejects empty input and normalizes the number", () => {
  const source = read(useCase);

  assert.match(source, /trackingNumber\.trim\(\)\.uppercase\(\)/);
  assert.match(source, /cleanTrackingNumber\.isBlank\(\)/);
  assert.match(source, /TRACKING_NUMBER/);
  assert.match(source, /trackingPort\.getPublicTracking\(cleanTrackingNumber\)/);
});

test("public tracking model exposes only public fields", () => {
  const source = read(model);

  for (const field of ["found", "trackingNumber", "publicStatus", "humanMessage", "orderType", "storeName", "summary", "isClosed"]) {
    assert.match(source, new RegExp(`val ${field}`));
  }
  assert.doesNotMatch(source, /phone|address|driverId|role|payment/i);
});

test("tracking adapter uses callable and never reads orders directly", () => {
  const source = read(adapter);

  assert.match(source, /getHttpsCallable\(GET_PUBLIC_ORDER_TRACKING\)/);
  assert.match(source, /GET_PUBLIC_ORDER_TRACKING = "getPublicOrderTracking"/);
  assert.doesNotMatch(source, /FirebaseFirestore|collection\("orders"\)|\.set\(|\.add\(|\.update\(|\.delete\(/);
});

test("Home, Tienda and tickets converge on the same tracking screen and use case", () => {
  const app = read(publicApp);

  assert.match(app, /publicTrackingUseCase\(\)/);
  assert.match(app, /PublicRoute\.PublicTracking\(it, PublicBottomDestination\.Home\)/);
  assert.match(app, /PublicRoute\.PublicTracking\(it, PublicBottomDestination\.Shop\)/);
  assert.match(app, /PublicRoute\.PublicTracking\(it, PublicBottomDestination\.Plus\)/);
  assert.match(app, /PublicShopTrackingScreen\(/);
  assert.match(app, /getTracking = getPublicTracking/);
  assert.doesNotMatch(app, /data class ShopTracking|PublicRoute\.ShopTracking/);
});

test("tracking UI has loading, not found and error states without direct order access", () => {
  const source = read(trackingScreen);

  assert.match(source, /Ingresá el número de pedido/);
  assert.match(source, /Consultando/);
  assert.match(source, /found/);
  assert.match(source, /No pudimos consultar/);
  assert.doesNotMatch(source, /FirebaseFirestore|collection\("orders"\)|Cancelar pedido|Reportar problema|PDL-123456/);
});

test("getPublicOrderTracking reads and filters public tracking data only", () => {
  const source = read(fn);
  const trackingFunction = source.slice(
    source.indexOf("exports.getPublicOrderTracking"),
    source.indexOf("function cleanOrderPayload"),
  );

  assert.match(trackingFunction, /exports\.getPublicOrderTracking/);
  assert.match(trackingFunction, /normalizeTrackingNumber/);
  assert.match(trackingFunction, /where\("trackingNumber", "==", trackingNumber\)/);
  assert.match(trackingFunction, /where\("publicOrderNumber", "==", trackingNumber\)/);
  assert.match(trackingFunction, /publicTrackingResponse/);
  assert.doesNotMatch(trackingFunction, /\.set\(|\.add\(|\.update\(|\.delete\(/);
  assert.doesNotMatch(source, /collection\("(users|roles|payments|order_tracking)"\)|driverId|WhatsApp|whatsapp/);
});

test("orders remain closed to direct public writes and raw public reads", () => {
  const source = read(rules);
  const ordersBlock = source.match(/match \/orders\/\{orderId\} \{[\s\S]*?match \/events/)[0];

  assert.match(ordersBlock, /allow read: if canReadOrder\(resource\.data\)/);
  assert.match(ordersBlock, /allow create, update, delete: if false/);
  assert.doesNotMatch(ordersBlock, /allow read: if true|allow write: if true/);
});

test("public UI does not read or write orders directly", () => {
  const result = spawnSync(
    "grep",
    [
      "-R",
      "collection(\"orders\"\\|collection('orders'",
      "-n",
      "app/src/main/java/com/pedilo/app/ui/publicuser",
    ],
    {encoding: "utf8"},
  );
  assert.notEqual(result.status, 0, result.stdout);
});
