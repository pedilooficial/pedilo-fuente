package com.pedilo.app.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedilo.app.core.model.AdminActiveOrdersBucket
import com.pedilo.app.core.model.AdminOrderAction
import com.pedilo.app.core.model.AdminOrderActionRequest
import com.pedilo.app.core.model.AdminOperationOrderClassification
import com.pedilo.app.core.model.AdminOperationOrderSignals
import com.pedilo.app.core.model.AdminOrderDetail
import com.pedilo.app.core.model.AdminOrderSummary
import com.pedilo.app.core.model.AdminProblemOrdersBucket
import com.pedilo.app.core.model.AdminTodayOrdersBucket
import com.pedilo.app.core.result.CoreResult
import com.pedilo.app.core.result.CoreError
import com.pedilo.app.core.runtime.adminOrdersUseCase
import com.pedilo.app.ui.admin.components.AdminBottomBar
import com.pedilo.app.ui.admin.components.AdminEntryCard
import com.pedilo.app.ui.admin.components.AdminHeader
import com.pedilo.app.ui.admin.components.AdminInfoPanel
import com.pedilo.app.ui.components.PediloTextField
import com.pedilo.app.ui.publicuser.PediloBg
import com.pedilo.app.ui.publicuser.PediloCardBrush
import com.pedilo.app.ui.publicuser.PediloGreen
import com.pedilo.app.ui.publicuser.PediloLine
import com.pedilo.app.ui.publicuser.PediloMuted
import com.pedilo.app.ui.publicuser.PediloOrange
import com.pedilo.app.ui.publicuser.PediloPanel
import com.pedilo.app.ui.publicuser.PediloPanelSoft
import com.pedilo.app.ui.publicuser.PediloText
import com.pedilo.app.ui.publicuser.PediloWarning
import com.pedilo.app.ui.publicuser.pediloCardDepth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

enum class AdminRoot(val label: String) {
    Operation("Operación"),
    Configuration("Configuración"),
    RoleAccess("Alta de roles"),
}

internal enum class OperationOrderVariant {
    Normal,
    NeedsAttention,
    WithProblem,
    ActionUnavailable,
}

private sealed interface AdminRoute {
    data object Operation : AdminRoute
    data object Configuration : AdminRoute
    data object RoleAccess : AdminRoute
    data class OperationUniverse(val universe: AdminOperationUniverse) : AdminRoute
    data class OperationView(val universe: AdminOperationUniverse, val view: AdminOperationView) : AdminRoute
    data class OperationList(
        val universe: AdminOperationUniverse,
        val view: AdminOperationView,
        val list: AdminOperationList,
    ) : AdminRoute
    data class OperationOrderDetail(
        val returnRoute: AdminRoute,
        val variant: OperationOrderVariant,
        val realOrderId: String? = null,
    ) : AdminRoute
    data class Section(val root: AdminRoot, val title: String) : AdminRoute
    data class ConfigurationSection(val section: AdminConfigurationSection) : AdminRoute
    data class ConfigurationSubsection(val section: AdminConfigurationSection, val title: String) : AdminRoute
    data class ConfigurationConvergence(
        val section: String,
        val subsection: String,
        val step: AdminConfigurationConvergenceStep,
    ) : AdminRoute
    data class RoleAccessSection(val section: AdminRoleAccessSection) : AdminRoute
    data class RoleAccessSubsection(val section: AdminRoleAccessSection, val title: String) : AdminRoute
    data class RoleAccessConvergence(
        val section: String,
        val subsection: String,
        val step: AdminRoleAccessConvergenceStep,
    ) : AdminRoute
}

private enum class AdminConfigurationConvergenceStep {
    Entity,
    Editor,
    Preview,
    Impact,
    SensitiveConfirmation,
    Result,
}

private enum class AdminRoleAccessConvergenceStep {
    Account,
    CreateAccount,
    AccessEditor,
    ChangeRole,
    ToggleAccess,
    LinkEntity,
    Impact,
    SensitiveConfirmation,
    Result,
}

data class AdminEntry(
    val title: String,
    val note: String,
)

private enum class AdminOperationMetricTone {
    Neutral,
    Healthy,
    Warning,
    Danger,
}

private data class AdminOperationalLiveCard(
    val icon: String,
    val title: String,
    val countLabel: String,
    val detail: String,
    val priority: String,
    val tension: String,
)

private data class AdminOperationSubcard(
    val icon: String,
    val title: String,
    val value: String,
    val detail: String,
    val tone: AdminOperationMetricTone,
    val onClick: () -> Unit,
)

internal data class AdminOrderDetailEntry(
    val label: String,
    val note: String,
    val variant: OperationOrderVariant,
    val realOrderId: String? = null,
)

private data class AdminConfigurationSection(
    val title: String,
    val summary: String,
    val contextTitle: String,
    val contextText: String,
    val entries: List<AdminEntry>,
)

private data class AdminRoleAccessSection(
    val title: String,
    val summary: String,
    val contextTitle: String,
    val contextText: String,
    val entries: List<AdminEntry>,
)

private val adminBottomBarReservedPadding = 112.dp
private val adminContentBottomPadding = 24.dp

private val configurationSections = listOf(
    AdminConfigurationSection(
        title = "Usuario público",
        summary = "Definiciones de experiencia visible para personas usuarias.",
        contextTitle = "Experiencia pública",
        contextText = "Ordena criterios de presentación sin cambiar flujos reales ni guardar cambios.",
        entries = listOf(
            AdminEntry("Presentación pública", "Lineamientos de portada y acceso"),
            AdminEntry("Banners y avisos", "Espacios de comunicación visible"),
            AdminEntry("Textos visibles", "Mensajes y etiquetas públicas"),
            AdminEntry("Seguimiento público", "Criterios de lectura de seguimiento"),
            AdminEntry("Orden y visibilidad de secciones", "Prioridad de bloques en pantalla"),
        ),
    ),
    AdminConfigurationSection(
        title = "Locales",
        summary = "Estructura de información de comercios.",
        contextTitle = "Estructura del local",
        contextText = "Prepara criterios de datos del local sin operar pedidos ni cuentas.",
        entries = listOf(
            AdminEntry("Datos del local", "Identidad y referencia comercial"),
            AdminEntry("Información pública", "Datos mostrables al usuario público"),
            AdminEntry("Horarios y descripción", "Disponibilidad declarativa"),
            AdminEntry("Estado de configuración", "Nivel de preparación del local"),
            AdminEntry("Revisión estructural", "Control de consistencia administrativa"),
        ),
    ),
    AdminConfigurationSection(
        title = "Catálogo y productos",
        summary = "Estructura de entidades vendibles.",
        contextTitle = "Oferta configurable",
        contextText = "Prepara categorías y criterios de oferta para pedidos futuros.",
        entries = listOf(
            AdminEntry("Categorías", "Agrupación principal de oferta"),
            AdminEntry("Subcategorías", "Detalle interno de categorías"),
            AdminEntry("Productos", "Entidades publicables"),
            AdminEntry("Precios", "Criterios de valor"),
            AdminEntry("Imágenes", "Recursos visuales de producto"),
            AdminEntry("Disponibilidad", "Condiciones de oferta"),
            AdminEntry("Visibilidad", "Reglas de exposición pública"),
        ),
    ),
    AdminConfigurationSection(
        title = "Pedidos",
        summary = "Criterios estructurales del flujo de pedido.",
        contextTitle = "Reglas del pedido",
        contextText = "Define parámetros generales sin abrir pedidos concretos ni estados vivos.",
        entries = listOf(
            AdminEntry("Reglas de creación", "Condiciones para iniciar pedidos"),
            AdminEntry("Estados visibles", "Etapas mostrables al público"),
            AdminEntry("Seguimiento futuro", "Criterios de lectura de avance"),
            AdminEntry("Reglas de tiempos extendidos", "Criterios de tiempos extendidos"),
            AdminEntry("Reglas de cancelación", "Criterios generales de cancelación"),
            AdminEntry("Comportamiento del pedido", "Normas de consistencia del flujo"),
        ),
    ),
    AdminConfigurationSection(
        title = "Comunicación",
        summary = "Estructura de mensajes y avisos.",
        contextTitle = "Mensajería administrativa",
        contextText = "Organiza plantillas y criterios de comunicación sin envío real.",
        entries = listOf(
            AdminEntry("Plantillas", "Base de mensajes reutilizables"),
            AdminEntry("Avisos", "Comunicaciones puntuales"),
            AdminEntry("Destinatario conceptual", "Segmentación administrativa"),
            AdminEntry("Canal previsto", "Canal definido para futuras etapas"),
            AdminEntry("Revisión de mensaje", "Control de claridad y tono"),
            AdminEntry("Impacto del cambio", "Efecto esperado del ajuste"),
        ),
    ),
    AdminConfigurationSection(
        title = "Operación",
        summary = "Criterios de lectura operativa.",
        contextTitle = "Marco de operación",
        contextText = "Define criterios de interpretación, no la operación viva del día.",
        entries = listOf(
            AdminEntry("Criterios de retraso", "Reglas para identificar retrasos"),
            AdminEntry("Criterios de problemas", "Reglas de clasificación de incidencias"),
            AdminEntry("Umbrales operativos", "Límites para alertas de seguimiento"),
            AdminEntry("Clasificaciones", "Ejes de agrupación operativa"),
            AdminEntry("Reglas de atención", "Prioridades de revisión"),
            AdminEntry("Condiciones para revisión", "Cuándo escalar un caso"),
        ),
    ),
    AdminConfigurationSection(
        title = "Reglas y validaciones",
        summary = "Condiciones generales de integridad.",
        contextTitle = "Base de validación",
        contextText = "Define mínimos de calidad sin tocar reglas técnicas de infraestructura.",
        entries = listOf(
            AdminEntry("Datos mínimos", "Campos requeridos por bloque"),
            AdminEntry("Reglas de publicación", "Condiciones para mostrar cambios"),
            AdminEntry("Bloqueos por incompleto", "Contenciones por faltantes"),
            AdminEntry("Validaciones de pedido", "Consistencia del flujo de pedido"),
            AdminEntry("Validaciones de local", "Consistencia de estructura comercial"),
            AdminEntry("Validaciones de producto", "Consistencia de oferta vendible"),
            AdminEntry("Condiciones generales", "Reglas comunes entre bloques"),
        ),
    ),
    AdminConfigurationSection(
        title = "Auditoría",
        summary = "Trazabilidad administrativa de cambios.",
        contextTitle = "Registro administrativo",
        contextText = "Representa seguimiento de cambios sin exponer detalles técnicos crudos.",
        entries = listOf(
            AdminEntry("Cambios de configuración", "Registro por bloque"),
            AdminEntry("Publicaciones", "Eventos de publicación"),
            AdminEntry("Desactivaciones", "Eventos de desactivación"),
            AdminEntry("Cambios sensibles", "Ajustes de mayor impacto"),
            AdminEntry("Intervenciones Admin registradas", "Acciones administrativas documentadas"),
            AdminEntry("Detalle de registro", "Resumen de contexto"),
            AdminEntry("Impacto registrado", "Efecto administrativo declarado"),
            AdminEntry("Resultado registrado", "Cierre del cambio representado"),
        ),
    ),
    AdminConfigurationSection(
        title = "Emergencias",
        summary = "Marco excepcional de configuración.",
        contextTitle = "Gestión excepcional",
        contextText = "Define criterios de contingencia sin activar acciones reales.",
        entries = listOf(
            AdminEntry("Modo seguro", "Perfil de contingencia"),
            AdminEntry("Restricciones temporales", "Alcance limitado por ventana"),
            AdminEntry("Avisos globales excepcionales", "Comunicación de contingencia"),
            AdminEntry("Estado de emergencia", "Lectura de situación"),
            AdminEntry("Alcance", "Bloques potencialmente impactados"),
            AdminEntry("Impacto", "Efecto esperado en la experiencia"),
            AdminEntry("Confirmación futura", "Paso de verificación previa"),
            AdminEntry("Registro posterior", "Trazabilidad luego de la contingencia"),
        ),
    ),
    AdminConfigurationSection(
        title = "General",
        summary = "Parámetros globales de administración.",
        contextTitle = "Marco general",
        contextText = "Agrupa criterios transversales y deriva cada tema a su bloque dueño.",
        entries = listOf(
            AdminEntry("Parámetros generales", "Ajustes de alcance amplio"),
            AdminEntry("Preferencias administrativas", "Preferencias de gestión"),
            AdminEntry("Estado general de configuración", "Lectura consolidada de preparación"),
            AdminEntry("Pendientes globales", "Tareas por asignar al bloque dueño"),
            AdminEntry("Derivación al bloque dueño", "Ruta al universo responsable"),
        ),
    ),
)

private val roleAccessSections = listOf(
    AdminRoleAccessSection(
        title = "Usuarios del equipo",
        summary = "Vista general de cuentas vinculadas al sistema.",
        contextTitle = "Cuentas del equipo",
        contextText = "Organiza estado de acceso y roles sin consultar cuentas reales.",
        entries = listOf(
            AdminEntry("Cuentas activas", "Acceso habilitado en revisión"),
            AdminEntry("Cuentas en revisión", "Pendientes de validación"),
            AdminEntry("Roles asignados", "Distribución de Admin, Local y Repartidor"),
            AdminEntry("Estado de acceso", "Lectura de habilitación"),
            AdminEntry("Vínculos operativos", "Relación con entidad operativa"),
        ),
    ),
    AdminRoleAccessSection(
        title = "Administradores",
        summary = "Cuentas con alcance administrativo.",
        contextTitle = "Acceso administrativo",
        contextText = "Revisión de cuentas Admin sin modificar permisos reales.",
        entries = listOf(
            AdminEntry("Cuentas Admin", "Listado conceptual de acceso"),
            AdminEntry("Estado de revisión", "Control de vigencia administrativa"),
            AdminEntry("Acceso administrativo", "Alcance de acciones permitido"),
            AdminEntry("Sensibilidad del rol", "Nivel de impacto del acceso"),
        ),
    ),
    AdminRoleAccessSection(
        title = "Locales store",
        summary = "Cuentas con rol Local.",
        contextTitle = "Cuentas store",
        contextText = "Organiza relación de cuenta y local sin editar la entidad comercial.",
        entries = listOf(
            AdminEntry("Cuentas Local", "Estado de cuentas store"),
            AdminEntry("Local vinculado", "Relación con local asignado"),
            AdminEntry("Estado de acceso", "Lectura de habilitación de ingreso"),
            AdminEntry("Vinculación pendiente", "Cuenta sin relación completa"),
            AdminEntry("Revisión de cuenta", "Control administrativo de consistencia"),
        ),
    ),
    AdminRoleAccessSection(
        title = "Repartidores driver",
        summary = "Cuentas con rol Repartidor.",
        contextTitle = "Cuentas driver",
        contextText = "Organiza relación de cuenta y repartidor sin operar entregas.",
        entries = listOf(
            AdminEntry("Cuentas Repartidor", "Estado de cuentas driver"),
            AdminEntry("Estado de acceso", "Lectura de habilitación de ingreso"),
            AdminEntry("Repartidor vinculado", "Relación con entidad de reparto"),
            AdminEntry("Vinculación pendiente", "Cuenta con vínculo incompleto"),
            AdminEntry("Revisión de cuenta", "Control administrativo de consistencia"),
        ),
    ),
    AdminRoleAccessSection(
        title = "Altas pendientes",
        summary = "Cuentas en proceso de alta.",
        contextTitle = "Pendientes de alta",
        contextText = "Ordena estados previos a habilitación sin crear cuentas reales.",
        entries = listOf(
            AdminEntry("Cuentas por revisar", "Pendientes de validación administrativa"),
            AdminEntry("Rol previsto", "Perfil objetivo de la cuenta"),
            AdminEntry("Datos faltantes", "Información pendiente para completar"),
            AdminEntry("Estado pendiente", "Situación actual de la alta"),
            AdminEntry("Revisión antes de activar", "Chequeo previo a habilitación"),
        ),
    ),
    AdminRoleAccessSection(
        title = "Usuarios inactivos",
        summary = "Cuentas con acceso detenido.",
        contextTitle = "Acceso inactivo",
        contextText = "Representa inactividad sin borrar historial ni reactivar cuentas.",
        entries = listOf(
            AdminEntry("Cuentas inactivas", "Acceso actualmente detenido"),
            AdminEntry("Acceso detenido", "Estado de bloqueo de ingreso"),
            AdminEntry("Motivo visible", "Causa administrativa declarada"),
            AdminEntry("Revisión pendiente", "Control previo a cambio de estado"),
            AdminEntry("Posible reactivación futura", "Ruta de revisión posterior"),
        ),
    ),
    AdminRoleAccessSection(
        title = "Vinculaciones pendientes",
        summary = "Cuentas con rol asignado y vínculo incompleto.",
        contextTitle = "Relaciones pendientes",
        contextText = "Ordena relaciones faltantes sin crear entidades ni aplicar vínculos reales.",
        entries = listOf(
            AdminEntry("Cuenta store sin local vinculado", "Relación comercial incompleta"),
            AdminEntry("Cuenta driver sin repartidor vinculado", "Relación operativa incompleta"),
            AdminEntry("Relación incompleta", "Pendiente de asociación final"),
            AdminEntry("Entidad pendiente", "Entidad destino por definir"),
            AdminEntry("Revisión de vínculo", "Control de consistencia de asociación"),
        ),
    ),
)

@Composable
fun AdminApp(onSignOutConfirmed: () -> Unit) {
    var route by remember { mutableStateOf<AdminRoute>(AdminRoute.Operation) }
    var showSignOut by remember { mutableStateOf(false) }
    var readOnlyOrders by remember { mutableStateOf<List<AdminOrderSummary>>(emptyList()) }
    var readOnlyOrderDetails by remember { mutableStateOf<Map<String, AdminOrderDetail>>(emptyMap()) }
    var pendingAction by remember { mutableStateOf<Pair<String, AdminOrderAction>?>(null) }
    var pendingReason by remember { mutableStateOf("") }
    var operationMessage by remember { mutableStateOf("") }
    var operationError by remember { mutableStateOf("") }
    val adminOrders = remember { adminOrdersUseCase() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        adminOrders.observe().collect { result ->
            when (result) {
                is CoreResult.Success -> readOnlyOrders = result.value
                is CoreResult.Failure -> readOnlyOrders = emptyList()
            }
        }
    }

    BackHandler(enabled = route !is AdminRoute.Operation && route !is AdminRoute.Configuration && route !is AdminRoute.RoleAccess) {
        route = when (val current = route) {
            is AdminRoute.ConfigurationConvergence -> when (current.step) {
                AdminConfigurationConvergenceStep.Entity -> AdminRoute.ConfigurationSubsection(
                    section = configurationSections.first { it.title == current.section },
                    title = current.subsection,
                )
                AdminConfigurationConvergenceStep.Editor -> current.copy(step = AdminConfigurationConvergenceStep.Entity)
                AdminConfigurationConvergenceStep.Preview -> current.copy(step = AdminConfigurationConvergenceStep.Editor)
                AdminConfigurationConvergenceStep.Impact -> current.copy(step = AdminConfigurationConvergenceStep.Preview)
                AdminConfigurationConvergenceStep.SensitiveConfirmation -> current.copy(step = AdminConfigurationConvergenceStep.Impact)
                AdminConfigurationConvergenceStep.Result -> current.copy(step = AdminConfigurationConvergenceStep.SensitiveConfirmation)
            }
            is AdminRoute.RoleAccessSubsection -> AdminRoute.RoleAccessSection(current.section)
            is AdminRoute.RoleAccessConvergence -> when (current.step) {
                AdminRoleAccessConvergenceStep.Account -> AdminRoute.RoleAccessSubsection(
                    section = roleAccessSections.first { it.title == current.section },
                    title = current.subsection,
                )
                AdminRoleAccessConvergenceStep.CreateAccount -> current.copy(step = AdminRoleAccessConvergenceStep.Account)
                AdminRoleAccessConvergenceStep.AccessEditor -> current.copy(step = AdminRoleAccessConvergenceStep.Account)
                AdminRoleAccessConvergenceStep.ChangeRole -> current.copy(step = AdminRoleAccessConvergenceStep.Account)
                AdminRoleAccessConvergenceStep.ToggleAccess -> current.copy(step = AdminRoleAccessConvergenceStep.Account)
                AdminRoleAccessConvergenceStep.LinkEntity -> current.copy(step = AdminRoleAccessConvergenceStep.Account)
                AdminRoleAccessConvergenceStep.Impact -> current.copy(step = AdminRoleAccessConvergenceStep.Account)
                AdminRoleAccessConvergenceStep.SensitiveConfirmation -> current.copy(step = AdminRoleAccessConvergenceStep.Impact)
                AdminRoleAccessConvergenceStep.Result -> current.copy(step = AdminRoleAccessConvergenceStep.SensitiveConfirmation)
            }
            is AdminRoute.RoleAccessSection -> AdminRoute.RoleAccess
            is AdminRoute.ConfigurationSubsection -> AdminRoute.ConfigurationSection(current.section)
            is AdminRoute.ConfigurationSection -> AdminRoute.Configuration
            is AdminRoute.OperationOrderDetail -> current.returnRoute
            is AdminRoute.OperationList -> AdminRoute.OperationView(current.universe, current.view)
            is AdminRoute.OperationView -> AdminRoute.Operation
            is AdminRoute.OperationUniverse -> AdminRoute.Operation
            is AdminRoute.Section -> when (current.root) {
                AdminRoot.Operation -> AdminRoute.Operation
                AdminRoot.Configuration -> AdminRoute.Configuration
                AdminRoot.RoleAccess -> AdminRoute.RoleAccess
            }
            AdminRoute.Operation -> AdminRoute.Operation
            AdminRoute.Configuration -> AdminRoute.Configuration
            AdminRoute.RoleAccess -> AdminRoute.RoleAccess
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PediloBg),
    ) {
        when (val current = route) {
            AdminRoute.Operation -> AdminOperationDeskScreen(
                orders = readOnlyOrders,
                onOpenView = { viewTitle ->
                    operationUniverses.firstOrNull { it.key == AdminOperationUniverseKey.Orders }?.let { universe ->
                        universe.views.firstOrNull { it.title == viewTitle }?.let { view ->
                            route = AdminRoute.OperationView(universe, view)
                        }
                    }
                },
                onOpenList = { universeKey, viewTitle, listTitle ->
                    operationUniverses.firstOrNull { it.key == universeKey }?.let { universe ->
                        universe.views.firstOrNull { it.title == viewTitle }?.let { view ->
                            view.lists.firstOrNull { it.title == listTitle }?.let { list ->
                                route = AdminRoute.OperationList(universe, view, list)
                            }
                        }
                    }
                },
                onSignOut = { showSignOut = true },
            )
            AdminRoute.Configuration -> AdminRootScreen(
                title = "Configuración",
                eyebrow = "Secciones de configuración",
                summary = "Organizá cómo funciona Pédilo.",
                entries = configurationEntries,
                onEntry = { entry ->
                    configurationSections.firstOrNull { it.title == entry.title }?.let {
                        route = AdminRoute.ConfigurationSection(it)
                    }
                },
                onSignOut = { showSignOut = true },
                showSignOut = false,
            )
            AdminRoute.RoleAccess -> AdminRootScreen(
                title = "Alta de roles",
                eyebrow = "Usuarios y accesos",
                summary = "Cuentas, roles y vinculaciones.",
                entries = roleEntries,
                onEntry = { entry ->
                    roleAccessSections.firstOrNull { it.title == entry.title }?.let {
                        route = AdminRoute.RoleAccessSection(it)
                    }
                },
                onSignOut = { showSignOut = true },
                showSignOut = false,
            )
            is AdminRoute.OperationUniverse -> AdminOperationUniverseScreen(
                universe = current.universe,
                orders = readOnlyOrders,
                onView = { view ->
                    route = AdminRoute.OperationView(current.universe, view)
                },
            )
            is AdminRoute.OperationView -> AdminOperationViewScreen(
                universe = current.universe,
                view = current.view,
                orders = readOnlyOrders,
                onList = { list ->
                    route = AdminRoute.OperationList(current.universe, current.view, list)
                },
            )
            is AdminRoute.OperationList -> AdminOperationListScreen(
                universe = current.universe,
                view = current.view,
                list = current.list,
                orders = readOnlyOrders,
                onOrderDetail = { selected ->
                    route = AdminRoute.OperationOrderDetail(
                        returnRoute = AdminRoute.OperationList(current.universe, current.view, current.list),
                        variant = selected.variant,
                        realOrderId = selected.realOrderId,
                    )
                },
            )
            is AdminRoute.OperationOrderDetail -> AdminOrderDetailScreen(
                variant = current.variant,
                orderId = current.realOrderId,
                summary = current.realOrderId?.let { id -> readOnlyOrders.firstOrNull { it.id == id } },
                detail = current.realOrderId?.let { readOnlyOrderDetails[it] },
                operationMessage = operationMessage,
                operationError = operationError,
                onLoadDetail = { orderId ->
                    if (readOnlyOrderDetails.containsKey(orderId)) return@AdminOrderDetailScreen
                    scope.launch {
                        when (val result = adminOrders.getDetail(orderId)) {
                            is CoreResult.Success -> readOnlyOrderDetails = readOnlyOrderDetails + (orderId to result.value)
                            is CoreResult.Failure -> Unit
                        }
                    }
                },
                onAction = { orderId, action ->
                    operationMessage = ""
                    operationError = ""
                    pendingReason = ""
                    pendingAction = orderId to action
                },
            )
            is AdminRoute.ConfigurationSection -> AdminConfigurationSectionScreen(
                section = current.section,
                onEntry = { route = AdminRoute.ConfigurationSubsection(current.section, it.title) },
            )
            is AdminRoute.ConfigurationSubsection -> AdminSectionScreen(
                root = AdminRoot.Configuration,
                title = current.title,
                summary = "Subsección lista para revisión administrativa.",
                panelTitle = current.section.title,
                panelText = "Este espacio organiza criterios sin editar datos, publicar cambios ni ejecutar acciones reales.",
                onConfigurationConvergence = {
                    route = AdminRoute.ConfigurationConvergence(
                        section = current.section.title,
                        subsection = current.title,
                        step = AdminConfigurationConvergenceStep.Entity,
                    )
                },
            )
            is AdminRoute.ConfigurationConvergence -> AdminConfigurationConvergenceScreen(
                section = current.section,
                subsection = current.subsection,
                step = current.step,
                onNext = { next -> route = current.copy(step = next) },
            )
            is AdminRoute.RoleAccessSection -> AdminRoleAccessSectionScreen(
                section = current.section,
                onEntry = { route = AdminRoute.RoleAccessSubsection(current.section, it.title) },
            )
            is AdminRoute.RoleAccessSubsection -> AdminSectionScreen(
                root = AdminRoot.RoleAccess,
                title = current.title,
                summary = "Subsección de acceso lista para revisión administrativa.",
                panelTitle = current.section.title,
                panelText = "Este espacio ordena cuentas y vínculos de forma visual sin modificar usuarios ni roles.",
                onRoleAccessConvergence = { initial ->
                    route = AdminRoute.RoleAccessConvergence(
                        section = current.section.title,
                        subsection = current.title,
                        step = initial,
                    )
                },
            )
            is AdminRoute.RoleAccessConvergence -> AdminRoleAccessConvergenceScreen(
                section = current.section,
                subsection = current.subsection,
                step = current.step,
                onNext = { next -> route = current.copy(step = next) },
            )
            is AdminRoute.Section -> AdminSectionScreen(
                root = current.root,
                title = current.title,
                summary = "Sección lista para organizar el próximo paso.",
                panelTitle = "Sección preparada",
                panelText = "Este espacio ordena el bloque sin leer información, guardar cambios ni operar pedidos.",
            )
        }

        AdminBottomBar(
            current = route.root(),
            onOperation = { route = AdminRoute.Operation },
            onConfiguration = { route = AdminRoute.Configuration },
            onRoleAccess = { route = AdminRoute.RoleAccess },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
        )
    }

    if (showSignOut) {
        AlertDialog(
            onDismissRequest = { showSignOut = false },
            title = { Text("¿Querés cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = onSignOutConfirmed) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOut = false }) {
                    Text("No")
                }
            },
        )
    }

    pendingAction?.let { (orderId, action) ->
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text(action.label) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(action.impact)
                    if (action.requiresReason) {
                        PediloTextField(
                            value = pendingReason,
                            onValueChange = { pendingReason = it },
                            label = "Motivo operativo",
                            singleLine = false,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val reason = pendingReason
                        pendingAction = null
                        scope.launch {
                            val request = AdminOrderActionRequest(
                                orderId = orderId,
                                action = action,
                                reason = reason,
                                forcedStatus = "under_review",
                                responsibleRole = "admin",
                            )
                            when (val result = adminOrders.execute(request)) {
                                is CoreResult.Success -> {
                                    operationMessage = result.value.humanMessage.ifBlank { result.value.eventSummary }
                                    operationError = ""
                                    when (val detail = adminOrders.getDetail(orderId)) {
                                        is CoreResult.Success -> readOnlyOrderDetails = readOnlyOrderDetails + (orderId to detail.value)
                                        is CoreResult.Failure -> Unit
                                    }
                                }
                                is CoreResult.Failure -> {
                                    operationMessage = ""
                                    operationError = (result.error as? CoreError.Operational)?.humanMessage
                                        ?: "No pudimos ejecutar la acción."
                                }
                            }
                        }
                    },
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun AdminRootScreen(
    title: String,
    eyebrow: String,
    summary: String,
    entries: List<AdminEntry>,
    onEntry: (AdminEntry) -> Unit,
    onSignOut: () -> Unit,
    showSignOut: Boolean,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = adminBottomBarReservedPadding),
        contentPadding = PaddingValues(top = 18.dp, bottom = adminContentBottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AdminHeader(
                title = title,
                eyebrow = eyebrow,
                summary = summary,
                onSignOut = onSignOut,
                showSignOut = showSignOut,
            )
        }
        items(entries) {
            AdminEntryCard(entry = it, onClick = { onEntry(it) })
        }
    }
}

@Composable
private fun AdminOperationUniverseScreen(
    universe: AdminOperationUniverse,
    orders: List<AdminOrderSummary>,
    onView: (AdminOperationView) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = adminBottomBarReservedPadding),
        contentPadding = PaddingValues(top = 18.dp, bottom = adminContentBottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AdminHeader(
                title = universe.title,
                eyebrow = "Operación",
                summary = universe.summary,
                onSignOut = {},
                showSignOut = false,
            )
        }
        item {
            AdminInfoPanel(title = universe.contextTitle, text = universe.contextText)
        }
        item {
            AdminOperationMotherCard(
                title = universe.title,
                summary = operationUniverseSummary(universe, orders),
                subcards = universe.views.map { view ->
                    AdminOperationSubcard(
                        icon = operationIconFor(view.title),
                        title = view.title,
                        value = operationViewCountLabel(view, orders),
                        detail = operationViewStateLabel(view, orders),
                        tone = operationToneFor(view, orders),
                        onClick = { onView(view) },
                    )
                },
            )
        }
    }
}

@Composable
private fun AdminOperationViewScreen(
    universe: AdminOperationUniverse,
    view: AdminOperationView,
    orders: List<AdminOrderSummary>,
    onList: (AdminOperationList) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = adminBottomBarReservedPadding),
        contentPadding = PaddingValues(top = 18.dp, bottom = adminContentBottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AdminHeader(
                title = view.title,
                eyebrow = universe.title,
                summary = view.summary,
                onSignOut = {},
                showSignOut = false,
            )
        }
        item {
            AdminOperationMotherCard(
                title = view.contextTitle,
                summary = operationViewSummary(view, orders),
                subcards = view.lists.map { list ->
                    AdminOperationSubcard(
                        icon = operationIconFor(list.title),
                        title = operationCompactTitle(list.title),
                        value = operationListCountLabel(list, orders),
                        detail = operationListTensionLabel(list, orders),
                        tone = operationToneFor(list, orders),
                        onClick = { onList(list) },
                    )
                },
            )
        }
    }
}

@Composable
private fun AdminOperationListScreen(
    universe: AdminOperationUniverse,
    view: AdminOperationView,
    list: AdminOperationList,
    orders: List<AdminOrderSummary>,
    onOrderDetail: (AdminOrderDetailEntry) -> Unit,
) {
    val scopedOrders = orders.forOperationList(list.kind)
    val orderEntries = orderDetailEntriesFor(list.kind, scopedOrders)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = adminBottomBarReservedPadding),
        contentPadding = PaddingValues(top = 18.dp, bottom = adminContentBottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AdminHeader(
                title = list.title,
                eyebrow = view.title,
                summary = list.summary,
                onSignOut = {},
                showSignOut = false,
            )
        }
        item {
            AdminInfoPanel(
                title = universe.title,
                text = if (orderEntries.isEmpty()) list.emptyText else "${orderEntries.size} pedidos.",
            )
        }
        items(orderEntries) { entry ->
            AdminOperationOrderCard(
                card = AdminOperationalLiveCard(
                    icon = "#",
                    title = entry.label,
                    countLabel = entry.note.substringBefore(" · "),
                    detail = entry.note,
                    priority = "",
                    tension = "",
                ),
                onClick = { onOrderDetail(entry) },
            )
        }
    }
}

@Composable
private fun AdminOperationDeskScreen(
    orders: List<AdminOrderSummary>,
    onOpenView: (String) -> Unit,
    onOpenList: (AdminOperationUniverseKey, String, String) -> Unit,
    onSignOut: () -> Unit,
) {
    val orderUniverse = operationUniverses.first { it.key == AdminOperationUniverseKey.Orders }
    val driverUniverse = operationUniverses.first { it.key == AdminOperationUniverseKey.Drivers }
    val storeUniverse = operationUniverses.first { it.key == AdminOperationUniverseKey.Stores }
    val orderViews = orderUniverse.views
    val driverView = driverUniverse.views.first()
    val storeView = storeUniverse.views.first()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = adminBottomBarReservedPadding),
        contentPadding = PaddingValues(top = 18.dp, bottom = adminContentBottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AdminHeader(
                title = "Pédilo Admin",
                eyebrow = "Operación",
                summary = "Lo importante para controlar el día.",
                onSignOut = onSignOut,
                showSignOut = true,
            )
        }
        item {
            AdminOperationMotherCard(
                title = "Pedidos",
                summary = operationUniverseSummary(orderUniverse, orders),
                subcards = orderViews.map { view ->
                    AdminOperationSubcard(
                        icon = operationIconFor(view.title),
                        title = view.title,
                        value = operationViewCountLabel(view, orders),
                        detail = operationViewStateLabel(view, orders),
                        tone = operationToneFor(view, orders),
                        onClick = { onOpenView(view.title) },
                    )
                },
            )
        }
        item {
            AdminOperationMotherCard(
                title = "Repartidores",
                summary = "Aún no hay información real",
                subcards = driverView.lists.map { list ->
                    AdminOperationSubcard(
                        icon = operationIconFor(list.title),
                        title = operationCompactTitle(list.title),
                        value = operationListCountLabel(list, orders),
                        detail = operationListTensionLabel(list, orders),
                        tone = AdminOperationMetricTone.Neutral,
                        onClick = { onOpenList(AdminOperationUniverseKey.Drivers, driverView.title, list.title) },
                    )
                },
            )
        }
        item {
            AdminOperationMotherCard(
                title = "Locales",
                summary = "Aún no hay información real",
                subcards = storeView.lists.map { list ->
                    AdminOperationSubcard(
                        icon = operationIconFor(list.title),
                        title = operationCompactTitle(list.title),
                        value = operationListCountLabel(list, orders),
                        detail = operationListTensionLabel(list, orders),
                        tone = AdminOperationMetricTone.Neutral,
                        onClick = { onOpenList(AdminOperationUniverseKey.Stores, storeView.title, list.title) },
                    )
                },
            )
        }
    }
}

@Composable
private fun AdminOperationMotherCard(
    title: String,
    summary: String,
    subcards: List<AdminOperationSubcard>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pediloCardDepth(RoundedCornerShape(16.dp))
            .background(PediloCardBrush, RoundedCornerShape(16.dp))
            .border(1.dp, PediloLine.copy(alpha = 0.62f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, color = PediloText, fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                summary,
                color = PediloMuted,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            subcards.forEach { subcard ->
                AdminOperationSubcardView(subcard)
            }
        }
    }
}

@Composable
private fun AdminOperationSubcardView(subcard: AdminOperationSubcard) {
    val toneColor = subcard.tone.operationToneColor()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PediloPanel.copy(alpha = 0.88f), RoundedCornerShape(12.dp))
            .border(1.dp, toneColor.copy(alpha = 0.34f), RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = subcard.onClick)
            .defaultMinSize(minHeight = 58.dp)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(toneColor.copy(alpha = 0.16f), RoundedCornerShape(9.dp))
                .border(1.dp, toneColor.copy(alpha = 0.38f), RoundedCornerShape(9.dp))
                .size(30.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(subcard.icon, color = toneColor, fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.ExtraBold)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(subcard.title, color = PediloText, fontSize = 14.sp, lineHeight = 17.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                subcard.detail,
                color = PediloMuted,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            subcard.value,
            color = toneColor,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(74.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun AdminOperationMetricTone.operationToneColor(): Color =
    when (this) {
        AdminOperationMetricTone.Neutral -> PediloMuted
        AdminOperationMetricTone.Healthy -> PediloGreen
        AdminOperationMetricTone.Warning -> PediloWarning
        AdminOperationMetricTone.Danger -> PediloWarning
    }

private fun adminOrderVisibleNumber(
    summary: AdminOrderSummary?,
    detail: AdminOrderDetail?,
    orderId: String?,
): String =
    listOf(
        detail?.trackingNumber,
        summary?.trackingNumber,
        detail?.publicOrderNumber,
        summary?.publicOrderNumber,
        orderId?.take(8),
    ).firstOrNull { !it.isNullOrBlank() }?.let { "#$it" } ?: "#____"

private fun String?.adminDisplayValue(fallback: String = "—"): String =
    this?.trim()?.takeIf { it.isNotBlank() } ?: fallback

private fun String.adminHumanStatusValue(fallback: String = "—"): String =
    when (trim().lowercase()) {
        "" -> fallback
        "created" -> "Pedido recibido"
        "cancelled", "canceled" -> "Cancelado"
        "delivered" -> "Entregado"
        "closed" -> "Cerrado"
        "archived" -> "Archivado"
        else -> trim()
    }

private fun String.adminPriorityValue(): String =
    when (trim().lowercase()) {
        "" -> "Normal"
        "normal" -> "Normal"
        "high" -> "Alta"
        "medium" -> "Media"
        "low" -> "Baja"
        else -> trim().replaceFirstChar { it.uppercase() }
    }

private fun List<String>?.adminItemsSummary(): String =
    this?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }?.joinToString(separator = "\n") ?: "Sin dato"

private fun AdminOrderDetail?.adminPersonName(): String =
    this?.component14().adminDisplayValue()

private fun Long?.adminMillisValue(): String =
    this?.let { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "AR")).format(Date(it)) } ?: "Aún no registrado"

private fun operationIconFor(title: String): String =
    when (title) {
        "Pedidos del día" -> "#"
        "Pedidos activos", "Activos" -> ">"
        "Pedidos con problemas", "Con problemas", "Con incidencias" -> "!"
        "Finalizados" -> "+"
        "Cancelados" -> "x"
        "Demorados", "Con demoras" -> "!"
        "Esperando local" -> "L"
        "Preparando" -> "P"
        "Esperando repartidor", "En servicio", "Disponibles", "Repartidores" -> "R"
        "En entrega" -> ">"
        "Local no responde", "Locales" -> "L"
        "Reclamo de cliente" -> "C"
        "Sin responsable" -> "?"
        "Operando" -> "+"
        "Pausados" -> "x"
        else -> "•"
    }

private fun operationCompactTitle(title: String): String =
    when (title) {
        "Con incidencias" -> "Incidencias"
        "Con demoras" -> "Demoras"
        "Reclamo de cliente" -> "Reclamos"
        "Local no responde" -> "Local sin respuesta"
        else -> title
    }

private fun operationUniverseSummary(universe: AdminOperationUniverse, orders: List<AdminOrderSummary>): String {
    val count = universe.views.flatMap { it.lists }.sumOf { orders.forOperationList(it.kind).size }
    if (universe.key != AdminOperationUniverseKey.Orders) return "Aún no hay información real"
    return if (count == 0) "Sin pedidos por ahora" else "$count pedidos"
}

private fun operationViewSummary(view: AdminOperationView, orders: List<AdminOrderSummary>): String {
    val count = view.lists.sumOf { orders.forOperationList(it.kind).size }
    return if (count == 0) {
        if (view.title == "Pedidos con problemas") "Sin casos por ahora" else "Sin pedidos por ahora"
    } else {
        "$count pedidos"
    }
}

private fun operationViewStateLabel(view: AdminOperationView, orders: List<AdminOrderSummary>): String {
    val count = view.lists.sumOf { orders.forOperationList(it.kind).size }
    return when {
        count == 0 && view.title == "Pedidos con problemas" -> "Sin casos por ahora"
        count == 0 -> "Sin pedidos por ahora"
        view.title == "Pedidos con problemas" -> "Prioridad"
        view.title == "Pedidos activos" -> "En curso"
        else -> "Del día"
    }
}

private fun operationViewCountLabel(view: AdminOperationView, orders: List<AdminOrderSummary>): String {
    if (view.lists.none { it.kind.isOrderList() }) return "Aún no hay información real"
    val count = view.lists.sumOf { orders.forOperationList(it.kind).size }
    return if (count == 0) "0" else count.toString()
}

private fun operationListCountLabel(list: AdminOperationList, orders: List<AdminOrderSummary>): String {
    val count = orders.forOperationList(list.kind).size
    if (!list.kind.isOrderList()) return "—"
    return if (count == 0) "0" else count.toString()
}

private fun operationListTensionLabel(list: AdminOperationList, orders: List<AdminOrderSummary>): String {
    val count = orders.forOperationList(list.kind).size
    return when {
        !list.kind.isOrderList() -> "Sin datos"
        count == 0 -> if (list.emptyText == "Sin casos por ahora.") "Sin casos" else "Sin pedidos"
        list.kind in setOf(
            AdminOperationListKind.TodayWithProblems,
            AdminOperationListKind.ProblemStoreNotResponding,
            AdminOperationListKind.ProblemUserClaim,
            AdminOperationListKind.ProblemDelayed,
            AdminOperationListKind.ProblemWithoutResponsible,
        ) -> "Con casos"
        else -> "En curso"
    }
}

private fun operationToneFor(view: AdminOperationView, orders: List<AdminOrderSummary>): AdminOperationMetricTone {
    val count = view.lists.sumOf { orders.forOperationList(it.kind).size }
    return when {
        count == 0 -> AdminOperationMetricTone.Neutral
        view.title == "Pedidos con problemas" -> AdminOperationMetricTone.Danger
        view.title == "Pedidos activos" -> AdminOperationMetricTone.Healthy
        else -> AdminOperationMetricTone.Neutral
    }
}

private fun operationToneFor(list: AdminOperationList, orders: List<AdminOrderSummary>): AdminOperationMetricTone {
    val count = orders.forOperationList(list.kind).size
    return when {
        count == 0 -> AdminOperationMetricTone.Neutral
        list.kind in setOf(
            AdminOperationListKind.TodayWithProblems,
            AdminOperationListKind.ProblemStoreNotResponding,
            AdminOperationListKind.ProblemUserClaim,
            AdminOperationListKind.ProblemDelayed,
            AdminOperationListKind.ProblemWithoutResponsible,
        ) -> AdminOperationMetricTone.Danger
        list.kind in setOf(
            AdminOperationListKind.TodayFinished,
            AdminOperationListKind.StoreOperating,
            AdminOperationListKind.DriverAvailable,
        ) -> AdminOperationMetricTone.Healthy
        list.kind in setOf(
            AdminOperationListKind.TodayCancelled,
            AdminOperationListKind.ActiveWaitingStore,
            AdminOperationListKind.ActiveWaitingDriver,
            AdminOperationListKind.StorePaused,
            AdminOperationListKind.StoreDelayed,
            AdminOperationListKind.DriverWithIncidents,
        ) -> AdminOperationMetricTone.Warning
        else -> AdminOperationMetricTone.Neutral
    }
}

@Composable
private fun AdminOperationOrderCard(
    card: AdminOperationalLiveCard,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PediloPanel, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = 82.dp)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(card.title, color = PediloText, fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.ExtraBold)
            Text(card.icon, color = PediloOrange, fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.ExtraBold)
        }
        Text(card.countLabel, color = PediloText, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
        Text(card.detail, color = PediloMuted, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AdminConfigurationSectionScreen(
    section: AdminConfigurationSection,
    onEntry: (AdminEntry) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = adminBottomBarReservedPadding),
        contentPadding = PaddingValues(top = 18.dp, bottom = adminContentBottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AdminHeader(
                title = section.title,
                eyebrow = "Configuración",
                summary = section.summary,
                onSignOut = {},
                showSignOut = false,
            )
        }
        item {
            AdminInfoPanel(title = section.contextTitle, text = section.contextText)
        }
        items(section.entries) {
            AdminEntryCard(entry = it, onClick = { onEntry(it) })
        }
    }
}

@Composable
private fun AdminRoleAccessSectionScreen(
    section: AdminRoleAccessSection,
    onEntry: (AdminEntry) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = adminBottomBarReservedPadding),
        contentPadding = PaddingValues(top = 18.dp, bottom = adminContentBottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AdminHeader(
                title = section.title,
                eyebrow = "Alta de roles",
                summary = section.summary,
                onSignOut = {},
                showSignOut = false,
            )
        }
        item {
            AdminInfoPanel(title = section.contextTitle, text = section.contextText)
        }
        items(section.entries) {
            AdminEntryCard(entry = it, onClick = { onEntry(it) })
        }
    }
}

@Composable
private fun AdminSectionScreen(
    root: AdminRoot,
    title: String,
    summary: String,
    panelTitle: String,
    panelText: String,
    onConfigurationConvergence: () -> Unit = {},
    onRoleAccessConvergence: (AdminRoleAccessConvergenceStep) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = adminBottomBarReservedPadding),
        contentPadding = PaddingValues(top = 18.dp, bottom = adminContentBottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AdminHeader(
                title = title,
                eyebrow = root.label,
                summary = summary,
                onSignOut = {},
                showSignOut = false,
            )
        }
        item {
            AdminInfoPanel(
                title = panelTitle,
                text = panelText,
            )
        }
        if (root == AdminRoot.Configuration) {
            item {
                AdminEntryCard(
                    entry = AdminEntry("Entidad configurable", "Abrir flujo de revisión del cambio"),
                    onClick = onConfigurationConvergence,
                )
            }
        }
        if (root == AdminRoot.RoleAccess) {
            val initialStep = when (title) {
                "Altas pendientes" -> AdminRoleAccessConvergenceStep.CreateAccount
                "Usuarios inactivos" -> AdminRoleAccessConvergenceStep.ToggleAccess
                "Vinculaciones pendientes" -> AdminRoleAccessConvergenceStep.LinkEntity
                else -> AdminRoleAccessConvergenceStep.Account
            }
            item {
                AdminEntryCard(
                    entry = AdminEntry("Cuenta concreta", "Abrir detalle de cuenta y acciones disponibles"),
                    onClick = { onRoleAccessConvergence(initialStep) },
                )
            }
        }
    }
}

@Composable
private fun AdminRoleAccessConvergenceScreen(
    section: String,
    subsection: String,
    step: AdminRoleAccessConvergenceStep,
    onNext: (AdminRoleAccessConvergenceStep) -> Unit,
) {
    val title = when (step) {
        AdminRoleAccessConvergenceStep.Account -> "Cuenta concreta"
        AdminRoleAccessConvergenceStep.CreateAccount -> "Alta de cuenta"
        AdminRoleAccessConvergenceStep.AccessEditor -> "Editor de acceso"
        AdminRoleAccessConvergenceStep.ChangeRole -> "Cambio de rol"
        AdminRoleAccessConvergenceStep.ToggleAccess -> "Activar o desactivar"
        AdminRoleAccessConvergenceStep.LinkEntity -> "Vincular entidad"
        AdminRoleAccessConvergenceStep.Impact -> "Impacto"
        AdminRoleAccessConvergenceStep.SensitiveConfirmation -> "Confirmación sensible"
        AdminRoleAccessConvergenceStep.Result -> "Resultado"
    }
    val summary = when (step) {
        AdminRoleAccessConvergenceStep.Account -> "Detalle de cuenta y acciones de acceso."
        AdminRoleAccessConvergenceStep.CreateAccount -> "Preparación de alta sin crear usuario real."
        AdminRoleAccessConvergenceStep.AccessEditor -> "Revisión de datos permitidos de acceso."
        AdminRoleAccessConvergenceStep.ChangeRole -> "Revisión de cambio entre Admin, Local y Repartidor."
        AdminRoleAccessConvergenceStep.ToggleAccess -> "Revisión de habilitación o detención de ingreso."
        AdminRoleAccessConvergenceStep.LinkEntity -> "Revisión de vínculo operativo de la cuenta."
        AdminRoleAccessConvergenceStep.Impact -> "Evaluación de alcance antes de confirmar."
        AdminRoleAccessConvergenceStep.SensitiveConfirmation -> "Validación final previa al resultado."
        AdminRoleAccessConvergenceStep.Result -> "Cierre de revisión sin aplicación real."
    }
    val context = when (step) {
        AdminRoleAccessConvergenceStep.Account -> "Cuenta: persona@equipo.com · Nombre visible: Referencia de cuenta · Rol: Local · Estado: En revisión · Puede ingresar: Pendiente."
        AdminRoleAccessConvergenceStep.CreateAccount -> "Email, nombre visible, rol previsto y vínculo requerido.\nNo se crean cuentas reales en este bloque."
        AdminRoleAccessConvergenceStep.AccessEditor -> "Campos permitidos: nombre visible, observación administrativa y estado de revisión.\nNo se editan datos sensibles de autenticación."
        AdminRoleAccessConvergenceStep.ChangeRole -> "Rol actual y rol nuevo dentro de Admin, Local y Repartidor.\nNo modifica pedidos, tickets ni catálogo."
        AdminRoleAccessConvergenceStep.ToggleAccess -> "Estado actual y nuevo estado de ingreso.\nDesactivar corta ingreso pero no borra historial ni pedidos."
        AdminRoleAccessConvergenceStep.LinkEntity -> "Cuenta y vínculo operativo pendiente.\nLa entidad se gestiona en su bloque dueño."
        AdminRoleAccessConvergenceStep.Impact -> "Qué cambia en acceso y vínculo, y qué no cambia.\nNo afecta pedidos existentes, tracking, catálogo ni operación viva."
        AdminRoleAccessConvergenceStep.SensitiveConfirmation -> "Acción sensible en revisión final para cuenta del equipo.\nNo aplica cambios reales en esta etapa."
        AdminRoleAccessConvergenceStep.Result -> "La revisión quedó preparada.\nNo se aplicaron cambios reales."
    }
    val next = when (step) {
        AdminRoleAccessConvergenceStep.Account -> AdminRoleAccessConvergenceStep.AccessEditor
        AdminRoleAccessConvergenceStep.CreateAccount -> AdminRoleAccessConvergenceStep.Impact
        AdminRoleAccessConvergenceStep.AccessEditor -> AdminRoleAccessConvergenceStep.ChangeRole
        AdminRoleAccessConvergenceStep.ChangeRole -> AdminRoleAccessConvergenceStep.Impact
        AdminRoleAccessConvergenceStep.ToggleAccess -> AdminRoleAccessConvergenceStep.Impact
        AdminRoleAccessConvergenceStep.LinkEntity -> AdminRoleAccessConvergenceStep.Impact
        AdminRoleAccessConvergenceStep.Impact -> AdminRoleAccessConvergenceStep.SensitiveConfirmation
        AdminRoleAccessConvergenceStep.SensitiveConfirmation -> AdminRoleAccessConvergenceStep.Result
        AdminRoleAccessConvergenceStep.Result -> AdminRoleAccessConvergenceStep.Account
    }
    val actionLabel = when (step) {
        AdminRoleAccessConvergenceStep.Account -> "Abrir editor de acceso"
        AdminRoleAccessConvergenceStep.CreateAccount -> "Revisar impacto"
        AdminRoleAccessConvergenceStep.AccessEditor -> "Revisar cambio de rol"
        AdminRoleAccessConvergenceStep.ChangeRole -> "Evaluar impacto"
        AdminRoleAccessConvergenceStep.ToggleAccess -> "Evaluar impacto"
        AdminRoleAccessConvergenceStep.LinkEntity -> "Evaluar impacto"
        AdminRoleAccessConvergenceStep.Impact -> "Ir a confirmación"
        AdminRoleAccessConvergenceStep.SensitiveConfirmation -> "Confirmar de forma visual"
        AdminRoleAccessConvergenceStep.Result -> "Reiniciar revisión"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = adminBottomBarReservedPadding),
        contentPadding = PaddingValues(top = 18.dp, bottom = adminContentBottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { AdminHeader(title = title, eyebrow = "Alta de roles", summary = summary, onSignOut = {}, showSignOut = false) }
        item { AdminInfoPanel(title = "Contexto", text = "Sección: $section · Subsección: $subsection\n$context") }
        item {
            AdminInfoPanel(
                title = "Roles permitidos",
                text = "Admin · Local · Repartidor",
            )
        }
        item {
            AdminActionCard(
                title = actionLabel,
                note = "Continuar sin aplicar cambios reales.",
                onClick = { onNext(next) },
            )
        }
    }
}

@Composable
private fun AdminConfigurationConvergenceScreen(
    section: String,
    subsection: String,
    step: AdminConfigurationConvergenceStep,
    onNext: (AdminConfigurationConvergenceStep) -> Unit,
) {
    val title = when (step) {
        AdminConfigurationConvergenceStep.Entity -> "Entidad configurable"
        AdminConfigurationConvergenceStep.Editor -> "Editor"
        AdminConfigurationConvergenceStep.Preview -> "Preview y revisión"
        AdminConfigurationConvergenceStep.Impact -> "Impacto"
        AdminConfigurationConvergenceStep.SensitiveConfirmation -> "Confirmación sensible"
        AdminConfigurationConvergenceStep.Result -> "Resultado"
    }
    val summary = when (step) {
        AdminConfigurationConvergenceStep.Entity -> "Lectura base de lo que se quiere ajustar."
        AdminConfigurationConvergenceStep.Editor -> "Preparación del cambio sin aplicarlo."
        AdminConfigurationConvergenceStep.Preview -> "Revisión conceptual antes de continuar."
        AdminConfigurationConvergenceStep.Impact -> "Evaluación de alcance y efectos esperados."
        AdminConfigurationConvergenceStep.SensitiveConfirmation -> "Validación previa para cambios sensibles."
        AdminConfigurationConvergenceStep.Result -> "Cierre de la secuencia de revisión."
    }
    val context = when (step) {
        AdminConfigurationConvergenceStep.Entity -> "Sección: $section · Subsección: $subsection.\nControla alcance, estado actual y restricciones sin ejecutar acciones."
        AdminConfigurationConvergenceStep.Editor -> "Valor actual y nuevo valor se muestran para revisar.\nNo se guardan cambios reales ni se publica contenido."
        AdminConfigurationConvergenceStep.Preview -> "Comparación conceptual del cambio.\nTodavía no está aplicado en la app real."
        AdminConfigurationConvergenceStep.Impact -> "Qué cambia, qué afecta y qué no cambia.\nSolo lectura de impacto, sin aplicación."
        AdminConfigurationConvergenceStep.SensitiveConfirmation -> "Confirmación humana del alcance y advertencias.\nEl botón de confirmar es solo visual en este bloque."
        AdminConfigurationConvergenceStep.Result -> "La revisión quedó preparada para una etapa posterior.\nNo se aplicaron cambios reales."
    }
    val action = when (step) {
        AdminConfigurationConvergenceStep.Entity -> "Ir al editor" to AdminConfigurationConvergenceStep.Editor
        AdminConfigurationConvergenceStep.Editor -> "Ir a preview" to AdminConfigurationConvergenceStep.Preview
        AdminConfigurationConvergenceStep.Preview -> "Revisar impacto" to AdminConfigurationConvergenceStep.Impact
        AdminConfigurationConvergenceStep.Impact -> "Continuar a confirmación" to AdminConfigurationConvergenceStep.SensitiveConfirmation
        AdminConfigurationConvergenceStep.SensitiveConfirmation -> "Confirmar de forma visual" to AdminConfigurationConvergenceStep.Result
        AdminConfigurationConvergenceStep.Result -> "Reiniciar revisión" to AdminConfigurationConvergenceStep.Entity
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = adminBottomBarReservedPadding),
        contentPadding = PaddingValues(top = 18.dp, bottom = adminContentBottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AdminHeader(
                title = title,
                eyebrow = "Configuración",
                summary = summary,
                onSignOut = {},
                showSignOut = false,
            )
        }
        item { AdminInfoPanel(title = "Contexto", text = context) }
        if (step == AdminConfigurationConvergenceStep.Editor) {
            item {
                AdminInfoPanel(
                    title = "Campos del editor",
                    text = "Valor actual · Nuevo valor · Campo requerido · Campo bloqueado · Guardado como borrador (solo visual).",
                )
            }
        }
        if (step == AdminConfigurationConvergenceStep.Impact || step == AdminConfigurationConvergenceStep.SensitiveConfirmation) {
            item {
                AdminInfoPanel(
                    title = "Impacto esperado",
                    text = "Qué cambia · Qué afecta · Qué no afecta · Requisitos previos.",
                )
            }
        }
        item {
            AdminActionCard(
                title = action.first,
                note = "Continuar sin aplicar cambios reales.",
                onClick = { onNext(action.second) },
            )
        }
    }
}

@Composable
private fun AdminOrderDetailScreen(
    variant: OperationOrderVariant,
    orderId: String?,
    summary: AdminOrderSummary?,
    detail: AdminOrderDetail?,
    operationMessage: String,
    operationError: String,
    onLoadDetail: (String) -> Unit,
    onAction: (String, AdminOrderAction) -> Unit,
) {
    orderId?.let { LaunchedEffect(it) { onLoadDetail(it) } }
    val visibleNumber = adminOrderVisibleNumber(summary, detail, orderId)
    val source = detail?.source ?: summary?.source.orEmpty()
    val requestType = detail?.requestType ?: summary?.requestType.orEmpty()
    val status = detail?.status ?: summary?.status.orEmpty()
    val publicStatus = detail?.publicStatus ?: summary?.publicStatus.orEmpty()
    val responsible = detail?.responsibleRole ?: summary?.responsibleRole.orEmpty()
    val priority = detail?.priority ?: summary?.priority.orEmpty()
    val operationalStatus = detail?.operationalStatus ?: summary?.operationalStatus.orEmpty()
    val needsAttention = detail?.needsAttention ?: summary?.needsAttention ?: false
    val activeIncident = detail?.activeIncident ?: summary?.activeIncident ?: false
    val createdAt = detail?.createdAtMillis ?: summary?.createdAtMillis
    val total = detail?.total ?: summary?.total.orEmpty()
    val storeName = detail?.storeName ?: summary?.storeName.orEmpty()
    val trackingNumber = detail?.trackingNumber ?: summary?.trackingNumber.orEmpty()
    val publicOrderNumber = detail?.publicOrderNumber ?: summary?.publicOrderNumber.orEmpty()
    val statusDisplay = publicStatus.ifBlank { status.adminHumanStatusValue() }
    val situationDisplay = operationalStatus.adminHumanStatusValue("Esperando respuesta del local")
    val priorityDisplay = priority.adminPriorityValue()
    val delaySignal = variant == OperationOrderVariant.NeedsAttention || variant == OperationOrderVariant.WithProblem
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = adminBottomBarReservedPadding),
        contentPadding = PaddingValues(top = 18.dp, bottom = adminContentBottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AdminHeader(
                title = "Pedido $visibleNumber",
                eyebrow = "Operación",
                summary = "Qué pasa con este pedido.",
                onSignOut = {},
                showSignOut = false,
            )
        }
        item {
            AdminOrderStatusPanel(
                variant = variant,
                operationalStatus = operationalStatus,
                responsibleRole = responsible,
                priority = priority,
                needsAttention = needsAttention,
                activeIncident = activeIncident,
            )
        }
        if (operationMessage.isNotBlank()) {
            item { AdminInfoPanel(title = "Resultado", text = operationMessage) }
        }
        if (operationError.isNotBlank()) {
            item { AdminInfoPanel(title = "Error operativo", text = operationError) }
        }
        item {
            AdminOrderFactPanel(
                facts = listOf(
                    "Estado actual" to statusDisplay,
                    "Situación" to situationDisplay,
                    "Responsable" to responsible.adminDisplayValue("Sin responsable"),
                    "Prioridad" to priorityDisplay,
                ),
            )
        }
        item {
            AdminOrderFactPanel(
                facts = listOf(
                    "Persona / cliente" to detail.adminPersonName(),
                    "Teléfono" to "—",
                    "Dirección" to "—",
                ),
            )
        }
        item {
            AdminOrderFactPanel(
                facts = listOf(
                    "Pedido" to detail?.itemsSummary.adminItemsSummary(),
                    "Observaciones" to "—",
                ),
            )
        }
        item {
            AdminOrderFactPanel(
                facts = listOf(
                    "Local / origen" to storeName.adminDisplayValue(),
                    "Origen" to AdminOperationOrderClassification.sourceLabel(source, requestType),
                ),
            )
        }
        item {
            AdminOrderFactPanel(
                facts = listOf(
                    "Total" to total.adminDisplayValue(),
                    "Forma de pago" to "—",
                    "Estado de pago" to "—",
                ),
            )
        }
        item {
            AdminOrderFactPanel(
                facts = listOf(
                    "Creado" to createdAt.adminMillisValue(),
                    "Última actualización" to detail?.updatedAtMillis.adminMillisValue(),
                ),
            )
        }
        item {
            AdminInfoPanel(
                title = "Problemas / demoras",
                text = when {
                    activeIncident -> "Incidencia operativa registrada."
                    needsAttention -> "El pedido requiere atención operativa."
                    delaySignal -> "Requiere revisión."
                    else -> "Sin problemas registrados."
                },
            )
        }
        item {
            AdminInfoPanel(
                title = "Historial operativo",
                text = detail?.lastEventSummary.adminDisplayValue("Aún no registrado"),
            )
        }
        item {
            AdminInfoPanel(
                title = "Opciones",
                text = "Sin acciones disponibles por ahora.",
            )
        }
    }
}

@Composable
private fun AdminOrderStatusPanel(
    variant: OperationOrderVariant,
    operationalStatus: String,
    responsibleRole: String,
    priority: String,
    needsAttention: Boolean,
    activeIncident: Boolean,
) {
    val situation = operationalStatus.adminHumanStatusValue()
    val priorityText = priority.adminPriorityValue()
    val (status, detail) = when {
        activeIncident -> "Con incidencia" to "Hay una incidencia operativa activa."
        needsAttention -> "Necesita atención" to "El pedido requiere seguimiento operativo."
        else -> when (variant) {
            OperationOrderVariant.WithProblem -> "Requiere revisión" to "Este pedido necesita atención."
            OperationOrderVariant.NeedsAttention -> "Esperando respuesta" to "Hace falta respuesta para seguir."
            else -> "En curso" to "Estado disponible para revisar."
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloPanelSoft, RoundedCornerShape(15.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(15.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Estado general", color = PediloMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(status, color = PediloOrange, fontSize = 22.sp, lineHeight = 26.sp, fontWeight = FontWeight.ExtraBold)
        Text(detail, color = PediloText, fontSize = 14.sp, lineHeight = 20.sp)
        Text(
            "Situación: $situation · Responsable: ${responsibleRole.ifBlank { "Sin responsable" }} · Prioridad: $priorityText",
            color = PediloMuted,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun AdminOrderFactPanel(facts: List<Pair<String, String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloPanelSoft, RoundedCornerShape(15.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(15.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        facts.forEach { (label, value) ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, color = PediloMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(value, color = PediloText, fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun AdminDisabledActionCard(title: String, note: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloPanel.copy(alpha = 0.55f), RoundedCornerShape(15.dp))
            .border(1.dp, PediloLine.copy(alpha = 0.7f), RoundedCornerShape(15.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Text(title, color = PediloMuted, fontSize = 19.sp, lineHeight = 23.sp, fontWeight = FontWeight.ExtraBold)
        Text(note, color = PediloMuted, fontSize = 13.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun AdminActionCard(title: String, note: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pediloCardDepth(RoundedCornerShape(15.dp))
            .background(PediloCardBrush, RoundedCornerShape(15.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(15.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Text(title, color = PediloText, fontSize = 19.sp, lineHeight = 23.sp, fontWeight = FontWeight.ExtraBold)
        Text(note, color = PediloMuted, fontSize = 13.sp, lineHeight = 17.sp)
    }
}

private fun AdminRoute.root(): AdminRoot = when (this) {
    AdminRoute.Operation -> AdminRoot.Operation
    AdminRoute.Configuration -> AdminRoot.Configuration
    AdminRoute.RoleAccess -> AdminRoot.RoleAccess
    is AdminRoute.OperationUniverse -> AdminRoot.Operation
    is AdminRoute.OperationView -> AdminRoot.Operation
    is AdminRoute.OperationList -> AdminRoot.Operation
    is AdminRoute.OperationOrderDetail -> AdminRoot.Operation
    is AdminRoute.ConfigurationSection -> AdminRoot.Configuration
    is AdminRoute.ConfigurationSubsection -> AdminRoot.Configuration
    is AdminRoute.ConfigurationConvergence -> AdminRoot.Configuration
    is AdminRoute.RoleAccessSection -> AdminRoot.RoleAccess
    is AdminRoute.RoleAccessSubsection -> AdminRoot.RoleAccess
    is AdminRoute.RoleAccessConvergence -> AdminRoot.RoleAccess
    is AdminRoute.Section -> root
}

private fun List<AdminOrderSummary>.todayOrders(): Map<AdminTodayOrdersBucket, List<AdminOrderSummary>> {
    val ordersWithSignals = map { order -> order to AdminOperationOrderSignals.from(order) }
    return ordersWithSignals
        .mapNotNull { (order, signals) ->
            AdminOperationOrderClassification.todayBucket(signals)?.let { it to order }
        }
        .groupBy({ it.first }, { it.second })
}

private fun List<AdminOrderSummary>.activeOrders(): Map<AdminActiveOrdersBucket, List<AdminOrderSummary>> {
    val ordersWithSignals = map { order -> order to AdminOperationOrderSignals.from(order) }
    return ordersWithSignals
        .mapNotNull { (order, signals) ->
            AdminOperationOrderClassification.activeBucket(signals)?.let { it to order }
        }
        .groupBy({ it.first }, { it.second })
}

private fun List<AdminOrderSummary>.problemOrders(): Map<AdminProblemOrdersBucket, List<AdminOrderSummary>> {
    val ordersWithSignals = map { order -> order to AdminOperationOrderSignals.from(order) }
    return ordersWithSignals
        .mapNotNull { (order, signals) ->
            AdminOperationOrderClassification.problemBucket(signals)?.let { it to order }
        }
        .groupBy({ it.first }, { it.second })
}

private fun List<AdminOrderSummary>.forOperationList(kind: AdminOperationListKind): List<AdminOrderSummary> {
    val signals = this.map { it to AdminOperationOrderSignals.from(it) }
    return when (kind) {
        AdminOperationListKind.TodayActive -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.todayBucket(s) == AdminTodayOrdersBucket.ACTIVE
        }.map { it.first }
        AdminOperationListKind.TodayFinished -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.todayBucket(s) == AdminTodayOrdersBucket.FINISHED
        }.map { it.first }
        AdminOperationListKind.TodayCancelled -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.todayBucket(s) == AdminTodayOrdersBucket.CANCELLED
        }.map { it.first }
        AdminOperationListKind.TodayWithProblems -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.todayBucket(s) == AdminTodayOrdersBucket.WITH_PROBLEMS
        }.map { it.first }
        AdminOperationListKind.ActiveWaitingStore -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.activeBucket(s) == AdminActiveOrdersBucket.WAITING_STORE
        }.map { it.first }
        AdminOperationListKind.ActivePreparing -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.activeBucket(s) == AdminActiveOrdersBucket.PREPARING
        }.map { it.first }
        AdminOperationListKind.ActiveWaitingDriver -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.activeBucket(s) == AdminActiveOrdersBucket.WAITING_DRIVER
        }.map { it.first }
        AdminOperationListKind.ActiveInDelivery -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.activeBucket(s) == AdminActiveOrdersBucket.IN_DELIVERY
        }.map { it.first }
        AdminOperationListKind.ProblemStoreNotResponding -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.problemBucket(s) == AdminProblemOrdersBucket.STORE_NOT_RESPONDING
        }.map { it.first }
        AdminOperationListKind.ProblemUserClaim -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.problemBucket(s) == AdminProblemOrdersBucket.CUSTOMER_CLAIM
        }.map { it.first }
        AdminOperationListKind.ProblemDelayed -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.problemBucket(s) == AdminProblemOrdersBucket.DELAYED
        }.map { it.first }
        AdminOperationListKind.ProblemWithoutResponsible -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.problemBucket(s) == AdminProblemOrdersBucket.WITHOUT_RESPONSIBLE
        }.map { it.first }
        else -> emptyList()
    }
}

private fun AdminOperationListKind.isOrderList(): Boolean =
    this in setOf(
        AdminOperationListKind.TodayActive,
        AdminOperationListKind.TodayFinished,
        AdminOperationListKind.TodayCancelled,
        AdminOperationListKind.TodayWithProblems,
        AdminOperationListKind.ActiveWaitingStore,
        AdminOperationListKind.ActivePreparing,
        AdminOperationListKind.ActiveWaitingDriver,
        AdminOperationListKind.ActiveInDelivery,
        AdminOperationListKind.ProblemStoreNotResponding,
        AdminOperationListKind.ProblemUserClaim,
        AdminOperationListKind.ProblemDelayed,
        AdminOperationListKind.ProblemWithoutResponsible,
    )
