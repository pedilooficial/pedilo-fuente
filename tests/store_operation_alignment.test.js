"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const test = require("node:test");

function read(path) {
  return fs.readFileSync(path, "utf8");
}

const app = "app/src/main/java/com/pedilo/app/ui/publicuser/PublicApp.kt";
const teamAdapter = "app/src/main/java/com/pedilo/app/core/firebase/FirebaseTeamAccessAdapter.kt";
const storeUi = "app/src/main/java/com/pedilo/app/ui/store/StoreApp.kt";
const storeAdapter = "app/src/main/java/com/pedilo/app/core/firebase/FirebaseStoreOrdersAdapter.kt";
const rules = "firestore.rules";
const adminUi = "app/src/main/java/com/pedilo/app/ui/admin/AdminApp.kt";
const driverAdapter = "app/src/main/java/com/pedilo/app/core/firebase/FirebaseDriverOrdersAdapter.kt";
const publicTracking = "app/src/main/java/com/pedilo/app/core/model/PublicTrackingState.kt";

test("store access depends on active store role and does not route other roles into store", () => {
  const appSource = read(app);
  const adapterSource = read(teamAdapter);

  assert.match(adapterSource, /profile\.getBoolean\(ACTIVE\) != true/);
  assert.match(adapterSource, /TeamRole\.fromWire\(profile\.getString\(ROLE\)\.orEmpty\(\)\)/);
  assert.match(appSource, /TeamRole\.Local -> StoreApp\(onSignOutConfirmed = onSignOutConfirmed\)/);
  assert.match(appSource, /TeamRole\.Admin -> AdminApp\(onSignOutConfirmed = onSignOutConfirmed\)/);
  assert.match(appSource, /TeamRole\.Driver -> DriverApp\(onSignOutConfirmed = onSignOutConfirmed\)/);
});

test("store reads only own orders and rules prevent foreign order visibility", () => {
  const adapter = read(storeAdapter);
  const ruleSource = read(rules);

  assert.match(adapter, /whereEqualTo\(STORE_ID, uid\)/);
  assert.match(adapter, /doc\.getString\(STORE_ID\)\.orEmpty\(\) != uid/);
  assert.match(ruleSource, /order\.storeId == request\.auth\.uid/);
  assert.match(ruleSource, /operatorRole\(\) in \["store", "driver", "admin"\] && operatorActive\(\)/);
});

test("store actions are backend-provided and include safe local lifecycle actions", () => {
  const ui = read(storeUi);
  const adapter = read(storeAdapter);

  assert.match(ui, /current\.nextAllowedActions\.isEmpty\(\)/);
  assert.match(ui, /items\(current\.nextAllowedActions\)/);
  assert.match(ui, /PendingStoreAction\(current\.id, action, current\.version\)/);
  assert.match(ui, /expectedVersion = pending\.expectedVersion/);
  assert.match(adapter, /getHttpsCallable\(OPERATE_LIVE_ORDER\)/);
  assert.match(adapter, /"expectedVersion" to expectedVersion/);
  assert.match(adapter, /LiveOrderAction\.LocalAccept/);
  assert.match(adapter, /LiveOrderAction\.LocalReject/);
  assert.match(adapter, /LiveOrderAction\.LocalMarkPreparing/);
  assert.match(adapter, /LiveOrderAction\.LocalMarkReady/);
  assert.match(adapter, /LiveOrderAction\.CancelOrder/);
  assert.match(adapter, /LiveOrderAction\.OpenIncident/);
  assert.doesNotMatch(`${ui}\n${adapter}`, /collection\("orders"\).*\.set|collection\("orders"\).*\.update|writeBatch|runTransaction/);
});

test("store detail handles terminal or unavailable actions without false buttons", () => {
  const ui = read(storeUi);

  assert.match(ui, /Sin acciones disponibles/);
  assert.match(ui, /El backend no habilita acciones para este pedido o versión/);
  assert.match(ui, /Si el pedido está cerrado, no hay acciones normales/);
  assert.match(ui, /Estado operativo:/);
  assert.match(ui, /Incidencia activa/);
  assert.doesNotMatch(ui, /driver_take|driver_mark_picked_up|driver_mark_delivered|force_status/);
});

test("store non persistent areas are explicit for stock delivery request and finances", () => {
  const ui = read(storeUi);

  assert.match(ui, /Productos y stock/);
  assert.match(ui, /Gestión visual no disponible en este bloque/);
  assert.match(ui, /No se guardan cambios de catálogo ni disponibilidad/);
  assert.match(ui, /Solicitud de repartidor/);
  assert.match(ui, /El local no solicita repartidor desde esta pantalla/);
  assert.match(ui, /Finanzas/);
  assert.match(ui, /estado financiero mínimo/);
  assert.match(ui, /Caja, deuda y liquidaciones siguen fuera/);
  assert.match(ui, /producto no disponible, demora o problema operativo/);
});

test("store alignment preserves public admin and driver boundaries", () => {
  const ui = read(storeUi);
  const admin = read(adminUi);
  const driver = read(driverAdapter);
  const tracking = read(publicTracking);

  assert.match(admin, /adminOrders\.executeLive/);
  assert.match(driver, /whereEqualTo\(RESPONSIBLE_ROLE, DRIVER_ROLE\)/);
  assert.match(driver, /whereEqualTo\(ASSIGNED_ACTOR_ID, ""\)/);
  assert.doesNotMatch(tracking, /responsibleRole|currentResponsibleRole|assignedActorId|driverId|events|incidents|audit|payload/i);
  assert.doesNotMatch(ui, /AdminApp|DriverApp|FirebaseFirestore|collection\("orders"\)/);
});
