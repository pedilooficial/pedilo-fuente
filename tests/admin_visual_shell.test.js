const fs = require("node:fs");
const test = require("node:test");
const assert = require("node:assert/strict");

const admin = "app/src/main/java/com/pedilo/app/ui/publicuser/PublicAdmin.kt";
const app = "app/src/main/java/com/pedilo/app/ui/publicuser/PublicApp.kt";

test("admin role opens visual admin shell instead of empty placeholder", () => {
  const appSource = fs.readFileSync(app, "utf8");
  const adminSource = fs.readFileSync(admin, "utf8");

  assert.match(appSource, /role == TeamRole\.Admin/);
  assert.match(appSource, /AdminApp\(onSignOutConfirmed = onSignOutConfirmed\)/);
  assert.match(adminSource, /fun AdminApp/);
  assert.match(adminSource, /"Pédilo Admin"/);
});

test("admin has separated root navigation and no public bottom labels", () => {
  const source = fs.readFileSync(admin, "utf8");

  assert.match(source, /Operation\("Operación"\)/);
  assert.match(source, /Configuration\("Configuración"\)/);
  assert.match(source, /RoleAccess\("Alta de roles"\)/);
  assert.match(source, /AdminBottomBar/);
  assert.doesNotMatch(source, /"Inicio"|"\\+"|"Tienda"|"Casa"|"Salir de Pedilo"/);
});

test("admin operation root exposes visual operation cards only", () => {
  const source = fs.readFileSync(admin, "utf8");

  [
    "Pedidos del día",
    "Pedidos activos",
    "Pedidos con problemas",
    "Repartidores activos",
    "Locales activos",
  ].forEach((label) => assert.match(source, new RegExp(label)));
  assert.match(source, /Movimiento completo de hoy/);
  assert.match(source, /Pedidos que siguen en curso/);
  assert.match(source, /Casos que necesitan revisión/);
});

test("admin operation internal screens expose planned operation subworlds", () => {
  const source = fs.readFileSync(admin, "utf8");

  [
    "Activos",
    "Finalizados",
    "Cancelados",
    "Demorados",
    "Con problemas",
    "Esperando local",
    "Preparando",
    "Esperando repartidor",
    "En entrega",
    "Local no responde",
    "Reclamo del cliente",
    "Libres",
    "Ocupados",
    "Pendientes de respuesta",
    "Con incidencia",
    "Vendiendo ahora",
    "Sin respuesta",
    "Pausados",
    "Con configuración pendiente",
    "Sin productos vendibles",
  ].forEach((label) => assert.match(source, new RegExp(label)));
});

test("admin remaining operation roots use safe visual copy", () => {
  const source = fs.readFileSync(admin, "utf8");

  [
    "Pedidos esperando respuesta del local",
    "Pedidos en preparación",
    "Pedidos listos para asignación",
    "Pedidos en camino",
    "Pedidos detenidos por falta de respuesta",
    "Casos iniciados por aviso del cliente",
    "Repartidores disponibles",
    "Repartidores con pedido asignado",
    "Casos esperando confirmación",
    "Situaciones que requieren revisión",
    "Locales disponibles para recibir pedidos",
    "Locales que no respondieron a tiempo",
    "Locales temporalmente detenidos",
    "Locales con datos por revisar",
    "Locales sin oferta disponible",
  ].forEach((label) => assert.match(source, new RegExp(label)));
});

test("admin today orders flow exposes category screens and subworlds", () => {
  const source = fs.readFileSync(admin, "utf8");

  assert.match(source, /TodayOrdersCategory/);
  assert.match(source, /TodayOrdersSubsection/);
  [
    "Pedidos del día que siguen en curso",
    "Pedidos del día cerrados correctamente",
    "Pedidos del día cerrados sin completar",
    "Pedidos del día con tiempo excedido",
    "Pedidos del día marcados con incidencia",
    "Entregados",
    "Retirados",
    "Enviados",
    "Cancelados por cliente",
    "Cancelados por local",
    "Cancelados por operación",
  ].forEach((label) => assert.match(source, new RegExp(label)));
});

test("admin shell reserves safe area for system navigation", () => {
  const source = fs.readFileSync(admin, "utf8");

  assert.match(source, /navigationBarsPadding\(\)/);
  assert.match(source, /adminBottomBarReservedPadding = 112\.dp/);
  assert.match(source, /adminContentBottomPadding = 24\.dp/);
  assert.match(source, /padding\(bottom = adminBottomBarReservedPadding\)/);
  assert.match(source, /contentPadding = PaddingValues\(top = 18\.dp, bottom = adminContentBottomPadding\)/);
});

test("admin relies on native back and only shows sign out on operation root", () => {
  const source = fs.readFileSync(admin, "utf8");
  const forbiddenReturnLabels = [`Volv${"er"}`, `Atr${"ás"}`];

  assert.match(source, /BackHandler\(enabled = route !is AdminRoute\.Operation/);
  forbiddenReturnLabels.forEach((label) => assert.doesNotMatch(source, new RegExp(`"${label}"`)));
  assert.match(source, /showSignOut = true/);
  assert.match(source, /showSignOut = false/);
  assert.match(source, /if \(showSignOut\)/);
});

test("admin configuration and role access roots expose their planned entries", () => {
  const source = fs.readFileSync(admin, "utf8");

  [
    "Usuario público",
    "Locales",
    "Catálogo y productos",
    "Pedidos",
    "Comunicación",
    "Operación",
    "Reglas y validaciones",
    "Auditoría",
    "Emergencias",
    "General",
    "Usuarios del equipo",
    "Administradores",
    "Locales store",
    "Repartidores driver",
    "Altas pendientes",
    "Usuarios inactivos",
    "Vinculaciones pendientes",
  ].forEach((label) => assert.match(source, new RegExp(label)));
});

test("admin order detail convergence exposes Pedido #____ variants without real data", () => {
  const source = fs.readFileSync(admin, "utf8");
  const forbiddenTitles = ["Pedido vivo", "Detalle del pedido", "Resolución del pedido"];

  assert.match(source, /OperationOrderDetail/);
  assert.match(source, /OperationOrderVariant/);
  assert.match(source, /"Pedido #____"/);
  assert.match(source, /AdminOrderDetailScreen/);
  [
    "Estado normal",
    "Necesita atención",
    "Con problema",
    "Acción no disponible",
  ].forEach((label) => assert.match(source, new RegExp(label)));
  forbiddenTitles.forEach((title) => assert.doesNotMatch(source, new RegExp(`"${title}"`)));
  assert.doesNotMatch(source, /OperationOrderSolve|AdminSolveScreen/);
});

test("admin order detail keeps shell rules and visual entry paths", () => {
  const source = fs.readFileSync(admin, "utf8");
  const forbiddenReturnLabels = [`Volv${"er"}`, `Atr${"ás"}`];

  assert.match(source, /orderDetailEntriesFor/);
  assert.match(source, /sectionTitle == "Pedidos activos" && subsectionTitle == "En entrega"/);
  assert.match(source, /sectionTitle == "Pedidos activos" && subsectionTitle == "Esperando local"/);
  assert.match(source, /sectionTitle == "Pedidos con problemas" && subsectionTitle == "Local no responde"/);
  assert.match(source, /is AdminRoute\.OperationOrderDetail -> current\.returnRoute/);
  assert.match(source, /is AdminRoute\.OperationOrderDetail -> AdminOrderDetailScreen/);
  assert.match(source, /AdminDisabledActionCard/);
  assert.match(source, /Disponible en el próximo bloque de operación/);
  forbiddenReturnLabels.forEach((label) => assert.doesNotMatch(source, new RegExp(`"${label}"`)));
  assert.match(source, /private fun AdminOrderDetailScreen[\s\S]*showSignOut = false/);
});

test("admin visual shell does not touch real data or operational systems", () => {
  const source = fs.readFileSync(admin, "utf8");
  const oldCopy = [
    `Sin datos conect${"ados"}`,
    `Estructura visual fut${"ura"}`,
    `Acceso visual fut${"uro"}`,
  ];

  assert.doesNotMatch(source, /Firebase|Firestore|collection\(|orders|createLocalOrder|createPlusOrder|getPublicOrderTracking|payments|WhatsApp|whatsapp|driverId/);
  assert.doesNotMatch(source, /reasignar|desactivar usuario|editar perfil|editar local|cargar catálogo/);
  oldCopy.forEach((text) => assert.doesNotMatch(source, new RegExp(text)));
  assert.match(source, /"¿Querés cerrar sesión\?"/);
  assert.match(source, /Text\("No"\)/);
  assert.match(source, /Text\("Sí"\)/);
});
