"use strict";

const assert = require("node:assert/strict");
const {test} = require("node:test");
const fs = require("node:fs");

const functionsPath = "functions/index.js";
const modelPath = "app/src/main/java/com/pedilo/app/core/model/AdminOrderReadModels.kt";
const operationsPath = "app/src/main/java/com/pedilo/app/core/model/AdminOrderOperations.kt";
const adapterPath = "app/src/main/java/com/pedilo/app/core/firebase/FirebaseAdminOrdersAdapter.kt";
const adminUiPath = "app/src/main/java/com/pedilo/app/ui/admin/AdminApp.kt";
const rulesPath = "firestore.rules";

function read(path) {
  return fs.readFileSync(path, "utf8");
}

test("admin operational core exposes responsible role and allowed actions", () => {
  const model = read(modelPath);
  const operations = read(operationsPath);

  for (const field of ["responsibleRole", "nextAllowedActions", "needsAttention", "activeIncident", "priority"]) {
    assert.match(model, new RegExp(`val ${field}`));
  }
  for (const action of [
    "mark_admin_reviewed",
    "confirm_intervention",
    "mark_incident",
    "resolve_incident",
    "cancel_by_admin",
    "force_status",
    "assign_responsible",
    "clear_responsible",
  ]) {
    assert.match(model, new RegExp(action));
  }
  assert.match(operations, /allowedActions/);
  assert.doesNotMatch(`${model}\n${operations}`, /Firebase|Firestore|androidx\.compose/);
});

test("admin callable validates auth role state action and writes audit events", () => {
  const source = read(functionsPath);
  const callable = source.slice(
    source.indexOf("exports.adminOrderAction"),
    source.indexOf("function cleanAdminActionPayload"),
  );

  assert.match(callable, /exports\.adminOrderAction/);
  assert.match(callable, /request\.auth/);
  assert.match(callable, /collection\("users"\)\.doc\(uid\)/);
  assert.match(callable, /requireAdminActor\(request\)/);
  assert.match(callable, /role !== "admin"/);
  assert.match(callable, /userSnap\.get\("active"\) !== true/);
  assert.match(callable, /db\.runTransaction/);
  assert.match(callable, /allowedAdminActions\(current\)/);
  assert.match(callable, /orderRef\.collection\("events"\)/);
  assert.match(callable, /orderRef\.collection\("incidents"\)/);
  assert.match(callable, /nextAllowedActions/);
});

test("admin adapter observes orders and calls backend for mutations only", () => {
  const source = read(adapterPath);

  assert.match(source, /addSnapshotListener/);
  assert.match(source, /getHttpsCallable\(OPERATE_LIVE_ORDER\)/);
  assert.match(source, /OPERATE_LIVE_ORDER = "operateLiveOrder"/);
  assert.match(source, /getOrderEventsReadOnly/);
  assert.match(source, /collection\(EVENTS\)/);
  assert.match(source, /LiveOrderAction\.fromWire/);
  assert.doesNotMatch(source, /AdminOrderOperations\.allowedActions/);
  assert.match(source, /ADMIN_ORDER_ACTION = "adminOrderAction"/);
  assert.doesNotMatch(source, /\.set\(|\.update\(|\.delete\(|writeBatch|runTransaction/);
});

test("admin UI executes only backend-provided operational actions with confirmation", () => {
  const source = read(adminUiPath);
  const detailScreen = source.slice(
    source.indexOf("private fun AdminOrderDetailScreen"),
    source.indexOf("private fun AdminOrderSectionScreen"),
  );

  assert.match(source, /AdminLiveOrderActionRequest/);
  assert.match(source, /adminOrders\.executeLive/);
  assert.match(source, /pendingLiveAction/);
  assert.match(source, /Confirmar acción/);
  assert.match(source, /Motivo operativo/);
  assert.match(source, /loadOrderDetail\(pending\.orderId, force = true\)/);
  assert.match(detailScreen, /AdminOperationOrderClassification\.operationalIdentity/);
  assert.match(detailScreen, /AdminOperationOrderClassification\.operationalFunction/);
  assert.match(detailScreen, /allowedActions/);
  assert.match(detailScreen, /nextAllowedActions/);
  assert.match(detailScreen, /Acciones disponibles/);
  assert.match(detailScreen, /onLiveAction\(action, expectedVersion\)/);
  assert.match(detailScreen, /AdminOrderNavigationCard/);
  assert.match(source, /AdminOrderSectionScreen/);
  assert.match(source, /AdminOrderSection\.History -> listOf/);
  assert.match(source, /detail\?\.events\.orEmpty\(\)/);
  assert.doesNotMatch(detailScreen, /local_accept|driver_take|driver_mark_delivered/);
  assert.doesNotMatch(source, /collection\("orders"\)|\.set\(|\.add\(|\.update\(|\.delete\(|runTransaction|writeBatch/);
});

test("rules keep orders and audit subcollections protected from client writes", () => {
  const source = read(rulesPath);
  const ordersBlock = source.match(/match \/orders\/\{orderId\} \{[\s\S]*?\n    \}/)[0];

  assert.match(ordersBlock, /allow create, update, delete: if false/);
  assert.match(ordersBlock, /match \/events\/\{eventId\}/);
  assert.match(ordersBlock, /match \/incidents\/\{incidentId\}/);
  assert.match(ordersBlock, /allow write: if false/);
});
