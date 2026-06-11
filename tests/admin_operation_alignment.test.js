"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const test = require("node:test");

function read(path) {
  return fs.readFileSync(path, "utf8");
}

const app = "app/src/main/java/com/pedilo/app/ui/publicuser/PublicApp.kt";
const teamAdapter = "app/src/main/java/com/pedilo/app/core/firebase/FirebaseTeamAccessAdapter.kt";
const adminUi = "app/src/main/java/com/pedilo/app/ui/admin/AdminApp.kt";
const adminAdapter = "app/src/main/java/com/pedilo/app/core/firebase/FirebaseAdminOrdersAdapter.kt";
const publicTrackingModel = "app/src/main/java/com/pedilo/app/core/model/PublicTrackingState.kt";
const storeAdapter = "app/src/main/java/com/pedilo/app/core/firebase/FirebaseStoreOrdersAdapter.kt";
const driverAdapter = "app/src/main/java/com/pedilo/app/core/firebase/FirebaseDriverOrdersAdapter.kt";

test("admin access depends on active admin role and does not route other roles into admin", () => {
  const appSource = read(app);
  const adapterSource = read(teamAdapter);

  assert.match(adapterSource, /profile\.getBoolean\(ACTIVE\) != true/);
  assert.match(adapterSource, /TeamRole\.fromWire\(profile\.getString\(ROLE\)\.orEmpty\(\)\)/);
  assert.match(appSource, /role == TeamRole\.Admin/);
  assert.match(appSource, /TeamRole\.Admin -> AdminApp\(onSignOutConfirmed = onSignOutConfirmed\)/);
  assert.match(appSource, /TeamRole\.Local -> StoreApp\(onSignOutConfirmed = onSignOutConfirmed\)/);
  assert.match(appSource, /TeamRole\.Driver -> DriverApp\(onSignOutConfirmed = onSignOutConfirmed\)/);
});

test("admin operation reads real order models and mutates only through callable backend", () => {
  const ui = read(adminUi);
  const adapter = read(adminAdapter);

  assert.match(adapter, /db\.collection\(ORDERS\)\.addSnapshotListener/);
  assert.match(adapter, /getOrderDetailReadOnly/);
  assert.match(adapter, /getOrderEventsReadOnly/);
  assert.match(adapter, /getHttpsCallable\(OPERATE_LIVE_ORDER\)/);
  assert.match(adapter, /"expectedVersion" to expectedVersion/);
  assert.match(ui, /adminOrders\.executeLive/);
  assert.match(ui, /AdminLiveOrderActionRequest/);
  assert.match(ui, /expectedVersion = pending\.expectedVersion/);
  assert.match(ui, /loadOrderDetail\(pending\.orderId, force = true\)/);
  assert.doesNotMatch(ui, /collection\("orders"\)|\.set\(|\.add\(|\.update\(|\.delete\(|writeBatch|runTransaction/);
});

test("admin order detail shows backend allowed actions and safe empty action state", () => {
  const ui = read(adminUi);
  const detail = ui.slice(
    ui.indexOf("private fun AdminOrderDetailScreen"),
    ui.indexOf("@Composable\nprivate fun AdminOrderSectionScreen"),
  );

  assert.match(detail, /allowedActions = detail\?\.nextAllowedActions \?: summary\?\.nextAllowedActions\.orEmpty\(\)/);
  assert.match(detail, /Estas acciones están habilitadas para la versión \$expectedVersion del pedido/);
  assert.match(detail, /Sin acciones disponibles/);
  assert.match(detail, /No hay acciones disponibles ahora/);
  assert.doesNotMatch(detail, /local_accept|driver_take|driver_mark_delivered|force_status/);
});

test("admin configuration and role access are real persisted admin surfaces", () => {
  const ui = read(adminUi);
  const adapter = read(adminAdapter);

  assert.match(ui, /AdminRealConfigurationScreen/);
  assert.match(ui, /AdminRealRoleAccessScreen/);
  assert.match(ui, /Controles de operación/);
  assert.match(ui, /Gestiona cuentas existentes del equipo/);
  assert.match(ui, /Crear cuentas nuevas no está disponible/);
  assert.match(adapter, /observeAdminConfig/);
  assert.match(adapter, /observeTeamUsers/);
  assert.match(adapter, /updateAdminConfig/);
  assert.match(adapter, /updateTeamUser/);
  assert.match(adapter, /db\.collection\(USERS\)/);
  assert.match(adapter, /db\.collection\(ADMIN_CONFIG\)/);
  assert.doesNotMatch(ui, /sin guardar datos reales|No se aplicaron cambios reales|Confirmar de forma visual|guardar borrador visual|confirmar visualmente|maqueta|prototipo|Alta de roles|ruta heredada|bloque/);
  assert.doesNotMatch(adapter, /collection\(ORDERS\)\.document\(.+\.update|collection\(ORDERS\)\.document\(.+\.set/);
});

test("admin errors are human controlled and public store driver compatibility remains protected", () => {
  const ui = read(adminUi);
  const publicModel = read(publicTrackingModel);
  const store = read(storeAdapter);
  const driver = read(driverAdapter);

  assert.match(ui, /Error operativo/);
  assert.match(ui, /adminHumanError\(\)/);
  assert.match(ui, /No pudimos actualizar el pedido/);
  assert.doesNotMatch(publicModel, /responsibleRole|currentResponsibleRole|assignedActorId|driverId|events|incidents|audit|payload/i);
  assert.match(store, /whereEqualTo\(STORE_ID, uid\)/);
  assert.match(driver, /whereEqualTo\(RESPONSIBLE_ROLE, DRIVER_ROLE\)/);
  assert.match(driver, /whereEqualTo\(ASSIGNED_ACTOR_ID, ""\)/);
});
