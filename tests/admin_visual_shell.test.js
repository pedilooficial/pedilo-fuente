const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const assert = require("node:assert/strict");

const admin = "app/src/main/java/com/pedilo/app/ui/admin/AdminApp.kt";
const adminDir = "app/src/main/java/com/pedilo/app/ui/admin";
const app = "app/src/main/java/com/pedilo/app/ui/publicuser/PublicApp.kt";

function readAdminSourceTree() {
  const stack = [adminDir];
  const files = [];
  while (stack.length > 0) {
    const dir = stack.pop();
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) stack.push(full);
      else if (entry.isFile() && entry.name.endsWith(".kt")) files.push(full);
    }
  }
  files.sort();
  return files.map((file) => fs.readFileSync(file, "utf8")).join("\n");
}

function readVisibleAdminStrings() {
  return [...readAdminSourceTree().matchAll(/"([^"\\]*(?:\\.[^"\\]*)*)"/g)]
    .map((match) => match[1])
    .join("\n");
}

test("admin role opens visual admin shell instead of empty placeholder", () => {
  const appSource = fs.readFileSync(app, "utf8");
  const adminSource = fs.readFileSync(admin, "utf8");

  assert.match(appSource, /role == TeamRole\.Admin/);
  assert.match(appSource, /AdminApp\(onSignOutConfirmed = onSignOutConfirmed\)/);
  assert.match(adminSource, /fun AdminApp/);
  assert.match(adminSource, /"Pédilo Admin"/);
});

test("admin has separated root navigation and no public bottom labels", () => {
  const source = readAdminSourceTree();

  assert.match(source, /Operation\("Operación"\)/);
  assert.match(source, /Configuration\("Configuración"\)/);
  assert.match(source, /RoleAccess\("Alta de roles"\)/);
  assert.match(source, /AdminBottomBar/);
  assert.doesNotMatch(source, /"Inicio"|"\\+"|"Tienda"|"Casa"|"Salir de Pedilo"/);
});

test("admin operation root exposes visual operation cards only", () => {
  const source = readAdminSourceTree();

  [
    "Pedidos del día",
    "Pedidos",
    "Repartidores",
    "Locales",
  ].forEach((label) => assert.match(source, new RegExp(label)));
  assert.match(source, /Resumen de hoy/);
  assert.match(source, /En vivo/);
  assert.match(source, /AdminOperationUniverseCard/);
  assert.match(source, /onOpenUniverse/);
  assert.match(source, /Aún no hay información real/);
  assert.match(source, /AdminTodaySummaryCard/);
  assert.doesNotMatch(source, /Mesa Operativa Viva|Necesitan atención|Finalizados recientes|Capas de lectura/);
  assert.match(source, /AdminBottomItem\("Operación"/);
  assert.doesNotMatch(source, /"Moto"|"Cfg"|"Rol"/);
});

test("admin operation routes expose explicit hierarchy levels", () => {
  const source = readAdminSourceTree();

  assert.match(source, /data object Operation : AdminRoute/);
  assert.match(source, /data class OperationUniverse/);
  assert.match(source, /data class OperationView/);
  assert.match(source, /data class OperationList/);
  assert.match(source, /data class OperationOrderDetail/);
  assert.match(source, /AdminOperationUniverseScreen/);
  assert.match(source, /AdminOperationViewScreen/);
  assert.match(source, /AdminOperationListScreen/);
});

test("admin operation internal screens expose planned operation subworlds", () => {
  const source = readAdminSourceTree();

  [
    "Activos",
    "Finalizados",
    "Cancelados",
    "Con problemas",
    "Esperando local",
    "Preparando",
    "Esperando repartidor",
    "En entrega",
    "Local no responde",
    "Reclamo de cliente",
    "Demorados",
    "Sin responsable",
    "En servicio",
    "Disponibles",
    "Con incidencias",
    "Operando",
    "Pausados",
    "Con demoras",
  ].forEach((label) => assert.match(source, new RegExp(label)));
});

test("admin remaining operation roots use safe visual copy", () => {
  const source = readAdminSourceTree();

  [
    "Necesitan respuesta del local.",
    "El local está preparando.",
    "Listos para asignar o retirar.",
    "Ya están camino al destino.",
    "Pedidos detenidos por falta de respuesta",
    "Casos avisados por el cliente",
    "Repartidores conectados.",
    "Listos para tomar pedidos.",
    "Necesitan revisión.",
    "Locales recibiendo pedidos.",
    "Operación detenida.",
    "Ritmo afectado.",
  ].forEach((label) => assert.match(source, new RegExp(label)));
});

test("admin order universe exposes dynamic views and filtered lists", () => {
  const source = readAdminSourceTree();

  assert.match(source, /AdminOperationUniverseKey\.Orders/);
  assert.match(source, /AdminOperationListKind\.TodayActive/);
  assert.match(source, /AdminOperationListKind\.ActiveWaitingStore/);
  assert.match(source, /AdminOperationListKind\.ProblemWithoutResponsible/);
  assert.match(source, /forOperationList/);
  assert.match(source, /returnRoute = AdminRoute\.OperationList/);
  assert.doesNotMatch(source, /TodayOrdersCategory|TodayOrdersSubsection/);
});

test("admin operation visible strings avoid internal architecture copy", () => {
  const visible = readVisibleAdminStrings();

  [
    "Vista dinámica",
    "Listado filtrado",
    "Entidad central",
    "Lecturas dinámicas",
    "El pedido no vive en una carpeta fija",
    "Datos operativos reales",
    "Lectura preparada para datos operativos reales",
    "Abrir listados filtrados",
    "Sin tensión operativa",
    "Dato pendiente",
    "No hay núcleo real conectado",
    "Pedido Vivo Universal",
  ].forEach((text) => assert.doesNotMatch(visible, new RegExp(text)));

  assert.doesNotMatch(visible, /\bmock\b|\bdemo\b|\bsample\b|\bplaceholder\b/i);
});

test("admin shell reserves safe area for system navigation", () => {
  const source = readAdminSourceTree();

  assert.match(source, /navigationBarsPadding\(\)/);
  assert.match(source, /adminBottomBarReservedPadding = 112\.dp/);
  assert.match(source, /adminContentBottomPadding = 24\.dp/);
  assert.match(source, /padding\(bottom = adminBottomBarReservedPadding\)/);
  assert.match(source, /contentPadding = PaddingValues\(top = 18\.dp, bottom = adminContentBottomPadding\)/);
});

test("admin relies on native back and only shows sign out on operation root", () => {
  const source = readAdminSourceTree();
  const forbiddenReturnLabels = [`Volv${"er"}`, `Atr${"ás"}`];

  assert.match(source, /BackHandler\(enabled = route !is AdminRoute\.Operation/);
  assert.match(source, /is AdminRoute\.OperationOrderDetail -> current\.returnRoute/);
  assert.match(source, /is AdminRoute\.OperationList -> AdminRoute\.OperationView/);
  assert.match(source, /is AdminRoute\.OperationView -> AdminRoute\.OperationUniverse/);
  assert.match(source, /is AdminRoute\.OperationUniverse -> AdminRoute\.Operation/);
  forbiddenReturnLabels.forEach((label) => assert.doesNotMatch(source, new RegExp(`"${label}"`)));
  assert.match(source, /showSignOut = true/);
  assert.match(source, /showSignOut = false/);
  assert.match(source, /if \(showSignOut\)/);
});

test("admin configuration and role access roots expose their planned entries", () => {
  const source = readAdminSourceTree();

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

test("admin configuration exposes internal visual sections without operational mixing", () => {
  const source = fs.readFileSync(admin, "utf8");

  [
    "Presentación pública",
    "Banners y avisos",
    "Textos visibles",
    "Seguimiento público",
    "Orden y visibilidad de secciones",
    "Datos del local",
    "Información pública",
    "Horarios y descripción",
    "Estado de configuración",
    "Revisión estructural",
    "Categorías",
    "Subcategorías",
    "Productos",
    "Precios",
    "Imágenes",
    "Disponibilidad",
    "Visibilidad",
    "Reglas de creación",
    "Estados visibles",
    "Seguimiento futuro",
    "Reglas de tiempos extendidos",
    "Reglas de cancelación",
    "Comportamiento del pedido",
    "Plantillas",
    "Avisos",
    "Destinatario conceptual",
    "Canal previsto",
    "Revisión de mensaje",
    "Impacto del cambio",
    "Criterios de retraso",
    "Criterios de problemas",
    "Umbrales operativos",
    "Clasificaciones",
    "Reglas de atención",
    "Condiciones para revisión",
    "Datos mínimos",
    "Reglas de publicación",
    "Bloqueos por incompleto",
    "Validaciones de pedido",
    "Validaciones de local",
    "Validaciones de producto",
    "Condiciones generales",
    "Cambios de configuración",
    "Publicaciones",
    "Desactivaciones",
    "Cambios sensibles",
    "Intervenciones Admin registradas",
    "Detalle de registro",
    "Impacto registrado",
    "Resultado registrado",
    "Modo seguro",
    "Restricciones temporales",
    "Avisos globales excepcionales",
    "Estado de emergencia",
    "Alcance",
    "Confirmación futura",
    "Registro posterior",
    "Parámetros generales",
    "Preferencias administrativas",
    "Estado general de configuración",
    "Pendientes globales",
    "Derivación al bloque dueño",
  ].forEach((label) => assert.match(source, new RegExp(label)));

  assert.match(source, /AdminConfigurationSectionScreen/);
  assert.match(source, /AdminRoute\.ConfigurationSection/);
  assert.match(source, /AdminRoute\.ConfigurationSubsection/);
  assert.match(source, /AdminRoute\.ConfigurationConvergence/);
  assert.match(
    source,
    /AdminRoute\.Configuration -> AdminRootScreen[\s\S]*showSignOut = false/,
  );
});

test("admin configuration convergence flow is available and remains visual only", () => {
  const source = fs.readFileSync(admin, "utf8");
  [
    "Entidad configurable",
    "Editor",
    "Preview y revisión",
    "Impacto",
    "Confirmación sensible",
    "Resultado",
    "No se aplicaron cambios reales",
  ].forEach((label) => assert.match(source, new RegExp(label)));

  assert.match(source, /AdminConfigurationConvergenceScreen/);
  assert.match(source, /AdminConfigurationConvergenceStep/);
  assert.match(source, /onConfigurationConvergence/);
});

test("admin role access exposes internal visual sections without touching real users", () => {
  const source = fs.readFileSync(admin, "utf8");
  [
    "Usuarios del equipo",
    "Administradores",
    "Locales store",
    "Repartidores driver",
    "Altas pendientes",
    "Usuarios inactivos",
    "Vinculaciones pendientes",
    "Cuentas activas",
    "Cuentas en revisión",
    "Roles asignados",
    "Estado de acceso",
    "Vínculos operativos",
    "Cuentas Admin",
    "Estado de revisión",
    "Acceso administrativo",
    "Sensibilidad del rol",
    "Cuentas Local",
    "Local vinculado",
    "Vinculación pendiente",
    "Revisión de cuenta",
    "Cuentas Repartidor",
    "Repartidor vinculado",
    "Cuentas por revisar",
    "Rol previsto",
    "Datos faltantes",
    "Estado pendiente",
    "Revisión antes de activar",
    "Cuentas inactivas",
    "Acceso detenido",
    "Motivo visible",
    "Revisión pendiente",
    "Posible reactivación futura",
    "Cuenta store sin local vinculado",
    "Cuenta driver sin repartidor vinculado",
    "Relación incompleta",
    "Entidad pendiente",
    "Revisión de vínculo",
  ].forEach((label) => assert.match(source, new RegExp(label)));

  assert.match(source, /AdminRoleAccessSectionScreen/);
  assert.match(source, /AdminRoute\.RoleAccessSection/);
  assert.match(source, /AdminRoute\.RoleAccessSubsection/);
  assert.match(source, /AdminRoute\.RoleAccessConvergence/);
  assert.match(source, /AdminRoute\.RoleAccess -> AdminRootScreen[\s\S]*showSignOut = false/);
});

test("admin role access convergence flow is available and restricted to visual mode", () => {
  const source = fs.readFileSync(admin, "utf8");
  [
    "Cuenta concreta",
    "Alta de cuenta",
    "Editor de acceso",
    "Cambio de rol",
    "Activar o desactivar",
    "Vincular entidad",
    "Impacto",
    "Confirmación sensible",
    "Resultado",
    "No se aplicaron cambios reales",
    "Admin · Local · Repartidor",
  ].forEach((label) => assert.match(source, new RegExp(label)));

  assert.match(source, /AdminRoleAccessConvergenceScreen/);
  assert.match(source, /AdminRoleAccessConvergenceStep/);
  assert.match(source, /onRoleAccessConvergence/);
  assert.doesNotMatch(source, /supervisor|soporte|cajero|operador|owner|manager/);
});

test("admin order detail exposes Pedido #____ read-only ficha with real-data fallbacks", () => {
  const source = readAdminSourceTree();
  const forbiddenTitles = ["Pedido vivo", "Detalle del pedido", "Resolución del pedido"];

  assert.match(source, /OperationOrderDetail/);
  assert.match(source, /OperationOrderVariant/);
  assert.match(source, /"Pedido #____"/);
  assert.match(source, /AdminOrderDetailScreen/);
  [
    "Qué pasa con este pedido",
    "Estado general",
    "Estado actual",
    "Situación",
    "Persona / cliente",
    "Teléfono",
    "Dirección",
    "Pedido",
    "Local / origen",
    "Total",
    "Forma de pago",
    "Creado",
    "Última actualización",
    "Problemas / demoras",
    "Historial operativo",
    "Opciones",
    "Sin acciones disponibles por ahora",
    "Aún no hay información real",
    "Aún no registrado",
    "Historial operativo aún no disponible",
  ].forEach((label) => assert.match(source, new RegExp(label)));
  forbiddenTitles.forEach((title) => assert.doesNotMatch(source, new RegExp(`"${title}"`)));
});

test("admin order detail keeps shell rules and visual entry paths", () => {
  const source = readAdminSourceTree();
  const forbiddenReturnLabels = [`Volv${"er"}`, `Atr${"ás"}`];

  assert.match(source, /orderDetailEntriesFor/);
  assert.match(source, /AdminOperationListKind\.ProblemStoreNotResponding/);
  assert.match(source, /AdminOperationListKind\.ActiveWaitingDriver/);
  assert.match(source, /AdminOperationOrderClassification\.problemBucket/);
  assert.match(source, /AdminOperationOrderClassification\.activeBucket/);
  assert.match(source, /is AdminRoute\.OperationOrderDetail -> current\.returnRoute/);
  assert.match(source, /is AdminRoute\.OperationOrderDetail -> AdminOrderDetailScreen/);
  assert.match(source, /adminOrderVisibleNumber/);
  assert.match(source, /adminDisplayValue/);
  assert.match(source, /adminItemsSummary/);
  assert.match(source, /adminMillisValue/);
  assert.doesNotMatch(source, /Acciones disponibles/);
  forbiddenReturnLabels.forEach((label) => assert.doesNotMatch(source, new RegExp(`"${label}"`)));
  assert.match(source, /private fun AdminOrderDetailScreen[\s\S]*showSignOut = false/);
});

test("admin visual shell keeps non-operational actions and avoids writes", () => {
  const source = readAdminSourceTree();
  const oldCopy = [
    `Sin datos conect${"ados"}`,
    `Estructura visual fut${"ura"}`,
    `Acceso visual fut${"uro"}`,
  ];

  assert.doesNotMatch(source, /set\(|add\(|update\(|delete\(|runTransaction|writeBatch|createLocalOrder|createPlusOrder|getPublicOrderTracking|payments|WhatsApp|whatsapp|driverId/);
  assert.doesNotMatch(source, /reasignar|desactivar usuario|editar perfil|editar local|cargar catálogo/);
  oldCopy.forEach((text) => assert.doesNotMatch(source, new RegExp(text)));
  assert.match(source, /"¿Querés cerrar sesión\?"/);
  assert.match(source, /Text\("No"\)/);
  assert.match(source, /Text\("Sí"\)/);
});
