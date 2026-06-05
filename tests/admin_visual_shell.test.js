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
  const bottomBar = fs.readFileSync("app/src/main/java/com/pedilo/app/ui/admin/components/AdminComponents.kt", "utf8");

  assert.match(source, /Operation\("Operación"\)/);
  assert.match(source, /Configuration\("Configuración"\)/);
  assert.match(source, /RoleAccess\("Alta de roles"\)/);
  assert.match(source, /AdminBottomBar/);
  assert.doesNotMatch(bottomBar, /"Inicio"|"\\+"|"Tienda"|"Casa"|"Salir de Pedilo"/);
});

test("admin operation root exposes visual operation cards only", () => {
  const source = readAdminSourceTree();

  [
    "Hoy",
    "Pedidos",
    "Repartidores",
    "Locales",
  ].forEach((label) => assert.match(source, new RegExp(label)));
  assert.match(source, /Resumen temporal/);
  assert.match(source, /Activos/);
  assert.match(source, /Problemas/);
  assert.match(source, /Cerrados/);
  assert.doesNotMatch(source, /Pedidos del día|Pedidos activos|Pedidos con problemas/);
  assert.match(source, /En servicio/);
  assert.match(source, /Operando/);
  assert.match(source, /AdminOperationMotherCard/);
  assert.match(source, /AdminOperationSubcardView/);
  assert.match(source, /AdminOperationOrderCard/);
  assert.doesNotMatch(source, /AdminOperationUniverseCard|AdminStatusPill|AdminOperationLiveCardView/);
  assert.match(source, /onOpenView/);
  assert.match(source, /onOpenList/);
  assert.match(source, /Sin datos/);
  assert.doesNotMatch(source, /Mesa Operativa Viva|Necesitan atención|Finalizados recientes|Capas de lectura/);
  assert.match(source, /AdminBottomItem\(Icons\.Outlined\.Dashboard, "Operación"/);
  assert.doesNotMatch(source, /"Moto"|"Cfg"|"Rol"/);
});

test("admin operation routes expose explicit hierarchy levels", () => {
  const source = readAdminSourceTree();

  assert.match(source, /data object Operation : AdminRoute/);
  assert.match(source, /data class OperationUniverse/);
  assert.match(source, /data class OperationView/);
  assert.match(source, /data class OperationList/);
  assert.match(source, /data class OperationOrderDetail/);
  assert.doesNotMatch(source, /OperationOrderSolve|OperationOperationalProfile|AdminOperationalProfileScreen|AdminOrderSolveScreen/);
  assert.match(source, /AdminOperationUniverseScreen/);
  assert.match(source, /AdminOperationViewScreen/);
  assert.match(source, /AdminOperationListScreen/);
});

test("admin operation internal screens expose planned operation subworlds", () => {
  const source = readAdminSourceTree();

  [
    "Activos",
    "Activos de hoy",
    "Problemas de hoy",
    "Cerrados de hoy",
    "Finalizados",
    "Cancelados",
    "Con problemas",
    "Esperando local",
    "Preparando",
    "Buscando repartidor",
    "En camino",
    "Local no responde",
    "Reclamo de cliente",
    "Demorados",
    "Sin responsable",
    "Revisar pedido",
    "Revisar estado",
    "Revisión operativa",
    "Revisión de hoy",
    "En servicio",
    "Disponibles",
    "Con incidencias",
    "Incidencias",
    "Operando",
    "Pausados",
    "Con demoras",
    "Demoras",
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

test("admin operation home opens concrete screens and lists directly", () => {
  const source = readAdminSourceTree();

  assert.match(source, /AdminOperationUniverseKey\.Orders/);
  assert.match(source, /AdminOperationListKind\.TodayActive/);
  assert.match(source, /AdminOperationListKind\.ActiveWaitingStore/);
  assert.match(source, /AdminOperationListKind\.ProblemWithoutResponsible/);
  assert.match(source, /forOperationList/);
  assert.match(source, /returnRoute = AdminRoute\.OperationList/);
  assert.match(source, /route = AdminRoute\.OperationView\(universe, view\)/);
  assert.match(source, /route = AdminRoute\.OperationList\(universe, view, list\)/);
  assert.doesNotMatch(source, /TodayOrdersCategory|TodayOrdersSubsection/);
});

test("admin operation visible strings avoid internal architecture copy", () => {
  const visible = readVisibleAdminStrings();

  [
    "Vista dinámica",
    "Listado filtrado",
    "Entidad central",
    "Ramas",
    "Elegí la rama",
    "A / B / C",
    "Universo de pedidos",
    "Lecturas dinámicas",
    "El pedido no vive en una carpeta fija",
    "Datos operativos reales",
    "Lectura preparada para datos operativos reales",
    "Abrir listados filtrados",
    "Sin tensión operativa",
    "Dato pendiente",
    "No hay núcleo real conectado",
    "Abrir pedido",
    "Ver estado",
    "Actualizado hace 1 min",
    "Pedido Vivo Universal",
    "Pedido read-only",
    "Seguimiento activo",
    "Prioridad alta",
  ].forEach((text) => assert.doesNotMatch(visible, new RegExp(text)));

  assert.doesNotMatch(visible, /\bmock\b|\bdemo\b|\bsample\b|\bplaceholder\b/i);
});

test("admin shell reserves safe area for system navigation", () => {
  const source = readAdminSourceTree();

  assert.match(source, /navigationBarsPadding\(\)/);
  assert.match(source, /adminBottomBarReservedPadding = 128\.dp/);
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
  assert.match(source, /is AdminRoute\.OperationView -> AdminRoute\.Operation/);
  assert.match(source, /is AdminRoute\.OperationUniverse -> AdminRoute\.Operation/);
  forbiddenReturnLabels.forEach((label) => assert.doesNotMatch(source, new RegExp(`"${label}"`)));
  assert.match(source, /showSignOut = true/);
  assert.match(source, /showSignOut = false/);
  assert.match(source, /if \(showSignOut\)/);
});

test("admin configuration and role access roots expose their planned entries", () => {
  const source = readAdminSourceTree();
  const adminSource = fs.readFileSync(admin, "utf8");
  const configuration = adminSource.slice(
    adminSource.indexOf("private val configurationSections"),
    adminSource.indexOf("private val roleAccessSections"),
  );

  [
    "Público",
    "Locales",
    "Reparto",
    "Marketplace",
    "Pedidos",
    "Precios",
    "Cobros",
    "Mensajes",
    "Reglas",
    "Notificaciones",
    "Métricas",
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

  [
    "Usuario público",
    "Catálogo y productos",
    "Comunicación",
    "Operación",
    "Reglas y validaciones",
  ].forEach((label) => assert.doesNotMatch(configuration, new RegExp(`"${label}"`)));

  assert.deepEqual(
    [...configuration.matchAll(/configurationSection\(\s*\n\s*"([^"]+)"/g)].map((match) => match[1]),
    ["Público", "Locales", "Reparto", "Marketplace", "Pedidos", "Precios", "Cobros", "Mensajes", "Reglas", "Notificaciones", "Métricas", "Auditoría", "Emergencias", "General"],
  );
  assert.match(adminSource, /private val configurationEntries = configurationSections\.map/);
  assert.match(adminSource, /AdminRoute\.ConfigurationSection\(configurationSections\.first \{ it\.title == entry\.title \}\)/);
});

test("admin configuration exposes internal visual sections without operational mixing", () => {
  const source = fs.readFileSync(admin, "utf8");

  [
    "Home público",
    "Tienda pública",
    "Botón +",
    "Seguimiento público",
    "Avisos y textos",
    "Pantallas vacías",
    "Listado de locales",
    "Datos del local",
    "Horarios y capacidad",
    "Catálogo del local",
    "Productos y variantes",
    "Listado de repartidores",
    "Habilitación y bloqueos",
    "Cierre financiero",
    "Categorías",
    "Subcategorías",
    "Destacados y nuevos",
    "Ranking público",
    "Reglas de creación",
    "Estados públicos",
    "Estados internos",
    "Tracking y fallbacks",
    "Precios comerciales",
    "Tarifas operativas",
    "Modo lluvia",
    "Formas de pago",
    "Quién paga y cobra",
    "Comprobantes",
    "Plantillas",
    "Mensajes por estado",
    "Mensajes por problema",
    "Publicación de locales",
    "Publicación de productos",
    "Solicitud de repartidor",
    "Eventos notificables",
    "Canales por rol",
    "Alertas críticas",
    "Pedidos y productos",
    "Visibilidad por rol",
    "Registro de cambios",
    "Publicaciones",
    "Cambios sensibles",
    "Detalle de registro",
    "Modo seguro",
    "Pausa general",
    "Pausa de locales",
    "Pausa de reparto",
    "Registro posterior",
    "Estado global",
    "Preferencias administrativas",
    "Pendientes globales",
    "Derivación al bloque dueño",
  ].forEach((label) => assert.match(source, new RegExp(label)));

  assert.match(source, /AdminConfigurationHomeScreen/);
  assert.match(source, /LazyVerticalGrid/);
  assert.match(source, /GridCells\.Fixed\(2\)/);
  assert.match(source, /\.aspectRatio\(1f\)/);
  assert.match(source, /AdminConfigurationRootCard/);
  assert.match(source, /configurationIconFor/);
  assert.match(source, /textAlign = TextAlign\.Center/);
  assert.match(source, /collectIsPressedAsState/);
  assert.match(source, /AdminConfigurationSectionScreen/);
  assert.match(source, /AdminRoute\.ConfigurationSection/);
  assert.match(source, /AdminRoute\.ConfigurationSubsection/);
  assert.match(source, /AdminRoute\.ConfigurationConvergence/);
  assert.match(
    source,
    /AdminRoute\.Configuration -> AdminConfigurationHomeScreen/,
  );
});

test("admin configuration convergence flow is available and remains visual only", () => {
  const source = fs.readFileSync(admin, "utf8");
  const configurationFlow = source.slice(
    source.indexOf("private fun AdminConfigurationConvergenceScreen"),
    source.indexOf("private fun AdminOrderDetailScreen"),
  );
  [
    "Detalle de configuración",
    "Preparar cambio",
    "Previsualización",
    "Impacto",
    "Confirmación sensible",
    "Resultado",
    "Auditoría visual",
    "El cambio permanece sin confirmar",
  ].forEach((label) => assert.match(source, new RegExp(label)));

  assert.match(source, /AdminConfigurationConvergenceScreen/);
  assert.match(source, /AdminConfigurationConvergenceStep/);
  assert.match(source, /onConfigurationConvergence/);
  assert.match(source, /current\.section\.title == "Auditoría"[\s\S]*AdminConfigurationConvergenceStep\.Audit/);
  assert.match(configurationFlow, /AdminConfigurationConvergenceStep\.Audit -> "Consulta de trazabilidad sin edición\."/);
  assert.doesNotMatch(configurationFlow, /No se aplicaron cambios reales|Entidad configurable|Preview y revisión/);
});

test("admin configuration keeps specialized roots within their final responsibilities", () => {
  const source = fs.readFileSync(admin, "utf8");
  const configuration = source.slice(
    source.indexOf("private val configurationSections"),
    source.indexOf("private val roleAccessSections"),
  );
  const block = (title, nextTitle) => configuration.slice(
    configuration.indexOf(`"${title}"`),
    configuration.indexOf(`"${nextTitle}"`),
  );
  const locales = block("Locales", "Reparto");
  const metrics = block("Métricas", "Auditoría");
  const audit = block("Auditoría", "Emergencias");
  const emergencies = block("Emergencias", "General");
  const general = configuration.slice(configuration.indexOf('"General"'));

  assert.match(locales, /Catálogo del local/);
  assert.match(locales, /Productos y variantes/);
  assert.match(metrics, /Criterios de lectura, ranking y visibilidad/);
  assert.doesNotMatch(metrics, /inventar|editar resultado|cargar resultado/i);
  assert.match(audit, /consulta|trazabilidad/i);
  assert.match(audit, /no se edita ni se borra/i);
  assert.match(emergencies, /Contingencias controladas y auditables/);
  assert.match(emergencies, /Registro posterior/);
  assert.doesNotMatch(general, /Catálogo del local|Precios comerciales|Formas de pago|Mensajes por estado|Eventos notificables|Registro de cambios|Modo seguro/);
});

test("admin public home uses specific layered cards and editor instead of generic configuration flow", () => {
  const source = fs.readFileSync(admin, "utf8");
  const publicHome = source.slice(
    source.indexOf("private val publicWorldEntries"),
    source.indexOf("private val roleAccessSections"),
  );
  const publicScreens = source.slice(
    source.indexOf("private fun AdminPublicHomeScreen"),
    source.indexOf("private fun AdminConfigurationRootCard"),
  );

  ["Home público", "Botón +", "Tienda", "Seguimiento / Reclamos"].forEach((label) => {
    assert.match(publicHome, new RegExp(label.replace("+", "\\+")));
  });
  [
    "Encabezado",
    "Accesos rápidos",
    "Banner destacado",
    "Ver más / Convenciones",
    "Ofertas",
    "Nuevos locales",
    "Buscador / tags",
    "Vista previa del Home",
  ].forEach((label) => assert.match(publicHome, new RegExp(label)));

  assert.match(source, /data object ConfigurationPublicHome/);
  assert.match(source, /data class ConfigurationPublicHomePart/);
  assert.match(source, /data class ConfigurationPublicHomeEditor/);
  assert.match(source, /AdminRoute\.ConfigurationPublicHome -> AdminPublicHomeScreen/);
  assert.match(source, /is AdminRoute\.ConfigurationPublicHomePart -> AdminPublicHomePartScreen/);
  assert.match(source, /is AdminRoute\.ConfigurationPublicHomeEditor -> AdminPublicHomeEditorScreen/);
  assert.match(source, /AdminPublicHomeEditorStep\.Detail -> AdminRoute\.ConfigurationPublicHomePart/);
  assert.match(source, /is AdminRoute\.ConfigurationPublicHomePart -> AdminRoute\.ConfigurationPublicHome/);
  assert.match(publicScreens, /AdminConfigurationGridScreen/);
  assert.match(publicScreens, /Guardar borrador visual/);
  assert.match(publicScreens, /Valor actual/);
  assert.match(publicScreens, /Nuevo valor/);
  assert.match(publicScreens, /Vista previa/);
  assert.match(publicScreens, /Impacto visible/);
  assert.match(publicScreens, /Confirmar visualmente/);
  assert.match(publicScreens, /Auditoría visual/);
  assert.match(publicScreens, /No crea productos ni modifica precios/);
  assert.match(publicScreens, /Home público real permanece sin cambios/);
  assert.doesNotMatch(publicScreens, /Firebase|Functions|Rules|backend|mock|demo|placeholder/i);
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
  const source =
    readAdminSourceTree() +
    fs.readFileSync("app/src/main/java/com/pedilo/app/core/model/AdminOperationOrderClassification.kt", "utf8");
  const adminSource = fs.readFileSync(admin, "utf8");
  const detailScreen = adminSource.slice(
    adminSource.indexOf("private fun AdminOrderDetailScreen"),
    adminSource.indexOf("private fun AdminOrderSectionScreen"),
  );
  const forbiddenTitles = ["Pedido vivo", "Detalle del pedido", "Resolución del pedido"];
  const forbiddenOrderCopy = [
    "Qué pasa con este pedido",
    "Estado general",
    "Estado actual",
    "Situación",
    "Local / origen",
    "Origen técnico",
    "Pedido de local",
    "Retirar en local",
    "Qué hay que hacer",
    "Qué necesita este pedido",
  ];

  assert.match(source, /OperationOrderDetail/);
  assert.match(source, /OperationOrderSection/);
  assert.match(source, /enum class AdminOrderSection/);
  assert.match(source, /OperationOrderVariant/);
  assert.match(source, /"Pedido #____"/);
  assert.match(source, /AdminOrderDetailScreen/);
  [
    "Compra solicitada",
    "Retiro solicitado",
    "Retiro de local",
    "Comprar y entregar",
    "Retirar y entregar",
    "Persona",
    "Pedido",
    "Local",
    "Retiro",
    "Entrega",
    "Total",
    "Pago",
    "Problemas",
    "Historial",
    "Opciones",
    "Sin acciones",
    "Sin datos",
    "Sin dato",
    "—",
  ].forEach((label) => assert.match(source, new RegExp(label)));
  forbiddenOrderCopy.forEach((label) => assert.doesNotMatch(detailScreen, new RegExp(label)));
  assert.match(detailScreen, /AdminOrderNavigationCard/);
  assert.match(detailScreen, /"Ubicación actual"/);
  assert.match(detailScreen, /adminPlacementLabel/);
  assert.match(detailScreen, /"Ingresó hoy"/);
  assert.match(source, /AdminOrderSectionScreen/);
  assert.match(source, /AdminOrderSection\.Summary/);
  assert.match(source, /AdminOrderSection\.Operation/);
  assert.match(source, /AdminOrderSection\.Delivery/);
  assert.match(source, /AdminOrderSection\.Payment/);
  assert.match(source, /AdminOrderSection\.Problems/);
  assert.match(source, /AdminOrderSection\.History/);
  assert.match(source, /AdminOrderSection\.Options/);
  assert.doesNotMatch(detailScreen, /AdminOrderFactPanel/);
  forbiddenTitles.forEach((title) => assert.doesNotMatch(source, new RegExp(`"${title}"`)));
});

test("admin operation uses real icons chips and tactile feedback", () => {
  const source = readAdminSourceTree();
  const adminSource = fs.readFileSync(admin, "utf8");
  const operationIcons = adminSource.slice(
    adminSource.indexOf("private fun operationIconFor"),
    adminSource.indexOf("private fun operationCompactTitle"),
  );

  assert.match(source, /material\.icons\.outlined/);
  assert.match(source, /Icon\(/);
  assert.match(source, /ImageVector/);
  assert.match(source, /MutableInteractionSource/);
  assert.match(source, /collectIsPressedAsState/);
  assert.match(source, /AdminStatusChip/);
  assert.match(source, /Buscando repartidor/);
  assert.match(source, /En camino/);
  assert.match(source, /Entregado/);
  assert.doesNotMatch(operationIcons, /-> "[#>!+xLPRC?•]"/);
  assert.doesNotMatch(source, /Text\("[#>!+xLPRC?•]"/);
  assert.doesNotMatch(source, /Aún no hay información real|Sin acciones disponibles por ahora|Pedido read-only/);
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
  assert.match(source, /is AdminRoute\.OperationOrderSection -> current\.detailRoute/);
  assert.match(source, /is AdminRoute\.OperationOrderDetail -> AdminOrderDetailScreen/);
  assert.match(source, /is AdminRoute\.OperationOrderSection -> AdminOrderSectionScreen/);
  assert.match(source, /adminOrderVisibleNumber/);
  assert.match(source, /adminDisplayValue/);
  assert.match(source, /adminItemsSummary/);
  assert.match(source, /component15\(\)\.adminDisplayValue/);
  assert.doesNotMatch(source, /Acciones disponibles/);
  forbiddenReturnLabels.forEach((label) => assert.doesNotMatch(source, new RegExp(`"${label}"`)));
  assert.match(source, /private fun AdminOrderDetailScreen[\s\S]*showSignOut = false/);
});

test("admin orders board removes repeated summaries and exposes useful hierarchy", () => {
  const source = fs.readFileSync(admin, "utf8");
  const viewScreen = source.slice(
    source.indexOf("private fun AdminOperationViewScreen"),
    source.indexOf("private fun AdminOperationListScreen"),
  );
  const listScreen = source.slice(
    source.indexOf("private fun AdminOperationListScreen"),
    source.indexOf("private fun AdminOperationDeskScreen"),
  );

  assert.match(source, /prominentValue = orders\.size\.toString\(\)/);
  assert.match(source, /summary = "Movimiento operativo"/);
  assert.match(source, /preview = orderDetailEntriesFor/);
  assert.match(source, /operationViewOrders\(view, orders\)\.size/);
  assert.match(source, /\.distinctBy \{ it\.id \}/);
  assert.match(source, /createdAtMillis\.isAdminToday\(\)/);
  assert.match(source, /val todaySignals = signals\.filter/);
  assert.match(source, /forPrimaryPlacement\(AdminOrderPrimaryPlacement\.ACTIVE\)/);
  assert.match(source, /forPrimaryPlacement\(AdminOrderPrimaryPlacement\.PROBLEM\)/);
  assert.match(source, /AdminOperationListKind\.TodayClosed/);
  assert.match(source, /AdminOperationListKind\.ClosedFinished/);
  assert.match(source, /AdminOperationListKind\.ClosedCancelled/);
  assert.match(source, /AdminOperationListKind\.TodayReview/);
  assert.match(source, /AdminOperationListKind\.Unclassified/);
  assert.match(source, /AdminOperationListKind\.ActiveReviewState/);
  assert.match(source, /AdminOperationListKind\.ProblemOperationalReview/);
  assert.match(source, /forPrimaryPlacement\(AdminOrderPrimaryPlacement\.UNCLASSIFIED\)/);
  assert.match(source, /activeBucket\(s\) == AdminActiveOrdersBucket\.REVIEW_STATE/);
  assert.match(source, /problemBucket\(s\) == AdminProblemOrdersBucket\.OPERATIONAL_REVIEW/);
  assert.doesNotMatch(viewScreen, /AdminOperationMotherCard/);
  assert.doesNotMatch(listScreen, /AdminInfoPanel/);
  assert.match(listScreen, /operationCompactTitle\(list\.title\)/);
  assert.match(listScreen, /entry\.note\.substringAfter/);
});

test("admin operation fallback cards keep counts previews and human detail aligned", () => {
  const source = readAdminSourceTree();
  const adminSource = fs.readFileSync(admin, "utf8");
  const detailScreen = adminSource.slice(
    adminSource.indexOf("private fun AdminOrderDetailScreen"),
    adminSource.indexOf("private fun AdminOrderSectionScreen"),
  );
  const placementLabels = adminSource.slice(
    adminSource.indexOf("private fun AdminOrderPrimaryPlacement.adminPlacementLabel"),
  );

  assert.match(source, /AdminOperationList\("Revisar pedido"[\s\S]*AdminOperationListKind\.Unclassified\)/);
  assert.match(source, /AdminOperationList\("Revisar estado"[\s\S]*AdminOperationListKind\.ActiveReviewState\)/);
  assert.match(source, /AdminOperationList\("Revisión operativa"[\s\S]*AdminOperationListKind\.ProblemOperationalReview\)/);
  assert.match(source, /AdminOperationList\("Revisión de hoy"[\s\S]*AdminOperationListKind\.TodayReview\)/);
  assert.match(adminSource, /operationListCountLabel[\s\S]*orders\.forOperationList\(list\.kind\)\.size/);
  assert.match(adminSource, /preview = orderDetailEntriesFor\(list\.kind, orders\.forOperationList\(list\.kind\)\)/);
  assert.match(adminSource, /val scopedOrders = orders\.forOperationList\(list\.kind\)/);
  assert.match(detailScreen, /AdminOrderPrimaryPlacement\.UNCLASSIFIED -> "Sin datos"/);
  assert.match(detailScreen, /AdminActiveOrdersBucket\.REVIEW_STATE -> "Revisar estado"/);
  assert.match(placementLabels, /AdminOrderPrimaryPlacement\.UNCLASSIFIED -> "Revisar pedido"/);
  const visibleLines = new Set(readVisibleAdminStrings().split("\n").map((line) => line.trim()));
  for (const technicalCopy of ["UNCLASSIFIED", "operationalStatus", "requestType", "source", "mock", "demo", "placeholder"]) {
    assert.equal(visibleLines.has(technicalCopy), false);
  }
  assert.doesNotMatch(placementLabels, /Sin clasificar/);
});

test("admin operation universe avoids explanatory mockup panels", () => {
  const source = fs.readFileSync(admin, "utf8");
  const universeScreen = source.slice(
    source.indexOf("private fun AdminOperationUniverseScreen"),
    source.indexOf("private fun AdminOperationViewScreen"),
  );

  assert.doesNotMatch(universeScreen, /AdminInfoPanel/);
  assert.doesNotMatch(source, /Elegí qué parte de los pedidos/);
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
