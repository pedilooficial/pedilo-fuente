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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
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
import com.pedilo.app.ui.publicuser.PediloPrimaryBrush
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

private enum class OperationSolveStage {
    Start,
    Options,
    SensitiveAction,
    Result,
}

private sealed interface AdminRoute {
    data object Operation : AdminRoute
    data object Configuration : AdminRoute
    data object RoleAccess : AdminRoute
    data class OperationSection(val section: AdminOperationSection) : AdminRoute
    data class OperationSubsection(val section: AdminOperationSection, val title: String) : AdminRoute
    data class OperationOrderDetail(
        val returnRoute: AdminRoute,
        val variant: OperationOrderVariant,
        val realOrderId: String? = null,
    ) : AdminRoute
    data class OperationOrderSolve(val returnRoute: AdminRoute, val stage: OperationSolveStage) : AdminRoute
    data class OperationOperationalProfile(val kind: AdminOperationalProfileKind, val state: String) : AdminRoute
    data class TodayOrdersCategory(val category: AdminTodayOrdersCategory) : AdminRoute
    data class TodayOrdersSubsection(val category: AdminTodayOrdersCategory, val title: String) : AdminRoute
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

private enum class AdminOperationalProfileKind {
    Store,
    Driver,
}

data class AdminEntry(
    val title: String,
    val note: String,
)

private data class AdminOperationHomeMetric(
    val title: String,
    val note: String,
    val value: String,
    val tone: AdminOperationMetricTone,
    val targetSection: String? = null,
    val targetSubsection: String? = null,
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

internal data class AdminOperationSection(
    val title: String,
    val summary: String,
    val contextTitle: String,
    val contextText: String,
    val entries: List<AdminEntry>,
)

internal data class AdminTodayOrdersCategory(
    val title: String,
    val summary: String,
    val contextText: String,
    val entries: List<AdminEntry>,
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

private fun operationLiveCardFor(entry: AdminEntry): AdminOperationalLiveCard {
    return when (entry.title) {
        "Pedidos del día" -> AdminOperationalLiveCard("Hoy", entry.title, "Cobertura diaria", entry.note, "Prioridad alta", "Entrada principal")
        "Pedidos activos" -> AdminOperationalLiveCard("Run", entry.title, "Flujo en curso", entry.note, "Prioridad alta", "Seguimiento continuo")
        "Pedidos con problemas" -> AdminOperationalLiveCard("!", entry.title, "Casos sensibles", entry.note, "Prioridad alta", "Requiere foco")
        "Repartidores activos" -> AdminOperationalLiveCard("Drv", entry.title, "Estado de reparto", entry.note, "Prioridad media", "Cobertura logística")
        "Locales activos" -> AdminOperationalLiveCard("Loc", entry.title, "Estado comercial", entry.note, "Prioridad media", "Disponibilidad de locales")
        "Activos" -> AdminOperationalLiveCard("Run", entry.title, "Pedidos abiertos", entry.note, "Prioridad alta", "En movimiento")
        "Finalizados" -> AdminOperationalLiveCard("Ok", entry.title, "Pedidos cerrados", entry.note, "Prioridad baja", "Cierre correcto")
        "Cancelados" -> AdminOperationalLiveCard("X", entry.title, "Pedidos cancelados", entry.note, "Prioridad media", "Requiere lectura")
        "Demorados" -> AdminOperationalLiveCard("R", entry.title, "Pedidos demorados", entry.note, "Prioridad alta", "Ritmo afectado")
        "Con problemas" -> AdminOperationalLiveCard("!", entry.title, "Pedidos con incidencia", entry.note, "Prioridad alta", "Casos críticos")
        else -> AdminOperationalLiveCard("•", entry.title, "Lectura operativa", entry.note, "Prioridad operativa", "Seguimiento")
    }
}

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

    val todayCount = readOnlyOrders.todayOrders().values.sumOf { it.size }
    val activeCount = readOnlyOrders.activeOrders().values.sumOf { it.size }
    val problemCount = readOnlyOrders.problemOrders().values.sumOf { it.size }
    val delayedCount = readOnlyOrders.todayOrders()[AdminTodayOrdersBucket.DELAYED]?.size ?: 0

    BackHandler(enabled = route !is AdminRoute.Operation && route !is AdminRoute.Configuration && route !is AdminRoute.RoleAccess) {
        route = when (val current = route) {
            is AdminRoute.OperationOrderSolve -> when (current.stage) {
                OperationSolveStage.Start -> current.returnRoute
                OperationSolveStage.Options -> AdminRoute.OperationOrderSolve(current.returnRoute, OperationSolveStage.Start)
                OperationSolveStage.SensitiveAction -> AdminRoute.OperationOrderSolve(current.returnRoute, OperationSolveStage.Options)
                OperationSolveStage.Result -> AdminRoute.OperationOrderSolve(current.returnRoute, OperationSolveStage.SensitiveAction)
            }
            is AdminRoute.OperationOperationalProfile -> AdminRoute.Operation
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
            is AdminRoute.TodayOrdersSubsection -> AdminRoute.TodayOrdersCategory(current.category)
            is AdminRoute.TodayOrdersCategory -> operationSections.first { it.title == "Pedidos del día" }.let {
                AdminRoute.OperationSection(it)
            }
            is AdminRoute.OperationSubsection -> AdminRoute.OperationSection(current.section)
            is AdminRoute.OperationSection -> AdminRoute.Operation
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
                totalOrdersToday = todayCount,
                activeOrders = activeCount,
                problemOrders = problemCount,
                delayedOrders = delayedCount,
                onOpenSection = { title ->
                    operationSections.firstOrNull { it.title == title }?.let {
                        route = AdminRoute.OperationSection(it)
                    }
                },
                onOpenMetric = { metric ->
                    val section = operationSections.firstOrNull { it.title == metric.targetSection }
                    if (section != null && metric.targetSubsection != null) {
                        route = AdminRoute.OperationSubsection(section, metric.targetSubsection)
                    } else if (section != null) {
                        route = AdminRoute.OperationSection(section)
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
            is AdminRoute.OperationSection -> AdminOperationSectionScreen(
                section = current.section,
                orders = readOnlyOrders,
                onEntry = { entry ->
                    if (current.section.title == "Pedidos del día") {
                        todayOrdersCategories.firstOrNull { it.title == entry.title }?.let {
                            route = AdminRoute.TodayOrdersCategory(it)
                        }
                    } else {
                        route = AdminRoute.OperationSubsection(current.section, entry.title)
                    }
                },
            )
            is AdminRoute.OperationSubsection -> {
                val scopedOrders = readOnlyOrders.forOperationSubsection(current.section.title, current.title)
                val orderEntries = orderDetailEntriesFor(current.section.title, current.title, scopedOrders)
                AdminSectionScreen(
                    root = AdminRoot.Operation,
                    title = current.title,
                    summary = "Lectura operativa por estado.",
                    panelTitle = current.section.title,
                    panelText = if (orderEntries.isEmpty()) {
                        "No hay pedidos para mostrar en esta sección."
                    } else {
                        "Pedidos detectados en esta sección: ${orderEntries.size}."
                    },
                    orderDetailEntries = orderEntries,
                    onOrderDetail = { selected ->
                        route = AdminRoute.OperationOrderDetail(
                            returnRoute = AdminRoute.OperationSubsection(current.section, current.title),
                            variant = selected.variant,
                            realOrderId = selected.realOrderId,
                        )
                    },
                    onOperationalProfile = { kind ->
                        route = AdminRoute.OperationOperationalProfile(
                            kind = kind,
                            state = current.title,
                        )
                    },
                )
            }
            is AdminRoute.OperationOrderDetail -> AdminOrderDetailScreen(
                variant = current.variant,
                orderId = current.realOrderId,
                summary = current.realOrderId?.let { id -> readOnlyOrders.firstOrNull { it.id == id } },
                detail = current.realOrderId?.let { readOnlyOrderDetails[it] },
                operationMessage = operationMessage,
                operationError = operationError,
                onOpenStore = {
                    route = AdminRoute.OperationOperationalProfile(
                        kind = AdminOperationalProfileKind.Store,
                        state = "Necesita atención",
                    )
                },
                onOpenDriver = {
                    route = AdminRoute.OperationOperationalProfile(
                        kind = AdminOperationalProfileKind.Driver,
                        state = "En seguimiento",
                    )
                },
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
            is AdminRoute.OperationOrderSolve -> AdminOrderSolveScreen(
                stage = current.stage,
                onNext = { next ->
                    route = AdminRoute.OperationOrderSolve(current.returnRoute, next)
                },
            )
            is AdminRoute.OperationOperationalProfile -> AdminOperationalProfileScreen(
                kind = current.kind,
                state = current.state,
            )
            is AdminRoute.TodayOrdersCategory -> AdminTodayOrdersCategoryScreen(
                category = current.category,
                onEntry = { route = AdminRoute.TodayOrdersSubsection(current.category, it.title) },
            )
            is AdminRoute.TodayOrdersSubsection -> AdminSectionScreen(
                root = AdminRoot.Operation,
                title = current.title,
                summary = "Lectura del día por estado.",
                panelTitle = current.category.title,
                panelText = "Lectura del día por categoría con datos disponibles.",
                orderDetailEntries = orderDetailEntriesFor(
                    sectionTitle = "Pedidos del día",
                    subsectionTitle = current.title,
                    orders = readOnlyOrders.forTodaySubsection(current.category.title, current.title),
                ),
                onOrderDetail = { selected ->
                    route = AdminRoute.OperationOrderDetail(
                        returnRoute = AdminRoute.TodayOrdersSubsection(current.category, current.title),
                        variant = selected.variant,
                        realOrderId = selected.realOrderId,
                    )
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
private fun AdminOperationSectionScreen(
    section: AdminOperationSection,
    orders: List<AdminOrderSummary>,
    onEntry: (AdminEntry) -> Unit,
) {
    val scopedSignals = orders.map { it to AdminOperationOrderSignals.from(it) }
    val totalInSection = when (section.title) {
        "Pedidos del día" -> scopedSignals.count { (_, s) -> AdminOperationOrderClassification.todayBucket(s) != null }
        "Pedidos activos" -> scopedSignals.count { (_, s) -> AdminOperationOrderClassification.activeBucket(s) != null }
        "Pedidos con problemas" -> scopedSignals.count { (_, s) -> AdminOperationOrderClassification.problemBucket(s) != null }
        else -> 0
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
                title = section.title,
                eyebrow = "Operación",
                summary = section.summary,
                onSignOut = {},
                showSignOut = false,
            )
        }
        item {
            AdminInfoPanel(title = section.contextTitle, text = section.contextText)
        }
        item {
            AdminInfoPanel(
                title = "Estado del grupo",
                text = if (totalInSection == 0) "No hay pedidos para mostrar en este grupo ahora." else "$totalInSection pedidos en este grupo operativo.",
            )
        }
        items(section.entries) { entry ->
            AdminOperationLiveCardView(
                card = operationLiveCardFor(entry).copy(
                    countLabel = operationCountLabel(section.title, entry.title, orders),
                    detail = operationDetailLabel(section.title, entry.title, orders, entry.note),
                    tension = operationTensionLabel(section.title, entry.title, orders),
                ),
                onClick = { onEntry(entry) },
            )
        }
    }
}

@Composable
private fun AdminOperationDeskScreen(
    totalOrdersToday: Int,
    activeOrders: Int,
    problemOrders: Int,
    delayedOrders: Int,
    onOpenSection: (String) -> Unit,
    onOpenMetric: (AdminOperationHomeMetric) -> Unit,
    onSignOut: () -> Unit,
) {
    val orderMetrics = listOf(
        AdminOperationHomeMetric("Pedidos del día", "Total recibido hoy", totalOrdersToday.toOperationalCount("pedidos"), AdminOperationMetricTone.Neutral, "Pedidos del día"),
        AdminOperationHomeMetric("Pedidos activos", "Pedidos en curso", activeOrders.toOperationalCount("pedidos"), AdminOperationMetricTone.Healthy, "Pedidos activos"),
        AdminOperationHomeMetric("Pedidos con problemas", "Requieren atención", problemOrders.toOperationalCount("casos"), if (problemOrders > 0) AdminOperationMetricTone.Danger else AdminOperationMetricTone.Neutral, "Pedidos con problemas"),
    )
    val driverMetrics = listOf(
        AdminOperationHomeMetric("En servicio", "Repartidores conectados", "Dato pendiente", AdminOperationMetricTone.Neutral, "Repartidores activos"),
        AdminOperationHomeMetric("Disponibles", "Listos para tomar pedidos", "Dato pendiente", AdminOperationMetricTone.Healthy, "Repartidores activos", "Libres"),
        AdminOperationHomeMetric("Con incidencias", "Requieren revisión", "Dato pendiente", AdminOperationMetricTone.Danger, "Repartidores activos", "Con incidencia"),
    )
    val storeMetrics = listOf(
        AdminOperationHomeMetric("Operando", "Locales recibiendo pedidos", "Dato pendiente", AdminOperationMetricTone.Healthy, "Locales activos", "Vendiendo ahora"),
        AdminOperationHomeMetric("Pausados", "Operación detenida", "Dato pendiente", AdminOperationMetricTone.Warning, "Locales activos", "Pausados"),
        AdminOperationHomeMetric("Con demoras", "Ritmo afectado", delayedOrders.toOperationalCount("casos"), if (delayedOrders > 0) AdminOperationMetricTone.Warning else AdminOperationMetricTone.Neutral, "Pedidos del día", "Demorados"),
    )

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
                summary = "Home operativo para leer el día, pedidos, repartidores y locales.",
                onSignOut = onSignOut,
                showSignOut = true,
            )
        }
        item {
            AdminTodaySummaryCard(
                title = "Pedidos del día",
                subtitle = "Resumen operativo de hoy",
                count = totalOrdersToday,
                onClick = { onOpenSection("Pedidos del día") },
            )
        }
        item {
            AdminOperationUniverseCard("Universo de pedidos", orderMetrics, onOpenMetric)
        }
        item {
            AdminOperationUniverseCard("Repartidores", driverMetrics, onOpenMetric)
        }
        item {
            AdminOperationUniverseCard("Locales activos", storeMetrics, onOpenMetric)
        }
    }
}

@Composable
private fun AdminTodaySummaryCard(
    title: String,
    subtitle: String,
    count: Int,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(listOf(PediloPanel, PediloPanel.copy(alpha = 0.9f))), RoundedCornerShape(18.dp))
            .border(1.dp, PediloOrange.copy(alpha = 0.42f), RoundedCornerShape(18.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(17.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = PediloText, fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text(subtitle, color = PediloMuted, fontSize = 13.sp, lineHeight = 18.sp)
            }
            AdminStatusPill("En vivo", AdminOperationMetricTone.Healthy)
        }
        Text(
            text = if (count == 0) "Sin pedidos ahora" else "$count pedidos",
            color = PediloText,
            fontSize = 30.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = "Lectura preparada para datos operativos reales.",
            color = PediloMuted,
            fontSize = 12.sp,
            lineHeight = 17.sp,
        )
    }
}

@Composable
private fun AdminOperationUniverseCard(
    title: String,
    metrics: List<AdminOperationHomeMetric>,
    onMetric: (AdminOperationHomeMetric) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(PediloCardBrush, RoundedCornerShape(18.dp))
            .border(1.dp, PediloLine.copy(alpha = 0.62f), RoundedCornerShape(18.dp))
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, color = PediloText, fontSize = 19.sp, lineHeight = 23.sp, fontWeight = FontWeight.ExtraBold)
        metrics.forEach { metric ->
            AdminOperationMetricRow(metric = metric, onClick = { onMetric(metric) })
        }
    }
}

@Composable
private fun AdminOperationMetricRow(
    metric: AdminOperationHomeMetric,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PediloPanelSoft.copy(alpha = 0.82f), RoundedCornerShape(14.dp))
            .border(1.dp, metric.tone.operationToneColor().copy(alpha = 0.34f), RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(metric.title, color = PediloText, fontSize = 15.sp, lineHeight = 19.sp, fontWeight = FontWeight.ExtraBold)
            Text(metric.note, color = PediloMuted, fontSize = 12.sp, lineHeight = 16.sp)
        }
        AdminStatusPill(metric.value, metric.tone)
    }
}

@Composable
private fun AdminStatusPill(text: String, tone: AdminOperationMetricTone) {
    Text(
        text = text,
        color = tone.operationToneColor(),
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(tone.operationToneColor().copy(alpha = 0.13f), RoundedCornerShape(999.dp))
            .border(1.dp, tone.operationToneColor().copy(alpha = 0.36f), RoundedCornerShape(999.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp),
    )
}

private fun Int.toOperationalCount(unit: String): String =
    if (this == 0) "Sin $unit" else "$this $unit"

private fun AdminOperationMetricTone.operationToneColor(): Color =
    when (this) {
        AdminOperationMetricTone.Neutral -> PediloOrange
        AdminOperationMetricTone.Healthy -> PediloGreen
        AdminOperationMetricTone.Warning -> PediloWarning
        AdminOperationMetricTone.Danger -> PediloOrange
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

private fun String?.adminDisplayValue(fallback: String = "Sin dato disponible"): String =
    this?.trim()?.takeIf { it.isNotBlank() } ?: fallback

private fun List<String>?.adminItemsSummary(): String =
    this?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }?.joinToString(separator = "\n") ?: "Sin dato disponible"

private fun AdminOrderDetail?.adminPersonName(): String =
    this?.component14().adminDisplayValue()

private fun Long?.adminMillisValue(): String =
    this?.let { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "AR")).format(Date(it)) } ?: "Aún no registrado"

private fun operationCountLabel(sectionTitle: String, entryTitle: String, orders: List<AdminOrderSummary>): String {
    val count = when (sectionTitle) {
        "Pedidos activos", "Pedidos con problemas" -> orders.forOperationSubsection(sectionTitle, entryTitle).size
        "Pedidos del día" -> orders.forTodaySubsection(entryTitle, entryTitle).size
        else -> 0
    }
    return if (count == 0) "Sin pedidos ahora" else "$count pedidos"
}

private fun operationDetailLabel(
    sectionTitle: String,
    entryTitle: String,
    orders: List<AdminOrderSummary>,
    fallback: String,
): String {
    val first = when (sectionTitle) {
        "Pedidos activos", "Pedidos con problemas" -> orders.forOperationSubsection(sectionTitle, entryTitle).firstOrNull()
        "Pedidos del día" -> orders.forTodaySubsection(entryTitle, entryTitle).firstOrNull()
        else -> null
    }
    if (first == null) return "No hay pedidos para mostrar en esta sección."
    val source = AdminOperationOrderClassification.sourceLabel(first.source, first.requestType)
    val store = first.storeName.ifBlank { "Local sin dato disponible" }
    return "$store · ${first.publicStatus.ifBlank { "Estado sin dato disponible" }} · $source"
}

private fun operationTensionLabel(sectionTitle: String, entryTitle: String, orders: List<AdminOrderSummary>): String {
    val count = when (sectionTitle) {
        "Pedidos activos", "Pedidos con problemas" -> orders.forOperationSubsection(sectionTitle, entryTitle).size
        "Pedidos del día" -> orders.forTodaySubsection(entryTitle, entryTitle).size
        else -> 0
    }
    return when {
        count == 0 -> "Sin tensión operativa"
        entryTitle in listOf("Con problemas", "Demorados", "Local no responde", "Reclamo del cliente") -> "Prioridad alta"
        else -> "Seguimiento activo"
    }
}

@Composable
private fun AdminTodayOrdersCategoryScreen(
    category: AdminTodayOrdersCategory,
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
                title = category.title,
                eyebrow = "Pedidos del día",
                summary = category.summary,
                onSignOut = {},
                showSignOut = false,
            )
        }
        item {
            AdminInfoPanel(title = category.title, text = category.contextText)
        }
        items(category.entries) { entry ->
            AdminOperationLiveCardView(
                card = operationLiveCardFor(entry),
                onClick = { onEntry(entry) },
            )
        }
    }
}

@Composable
private fun AdminOperationLiveCardView(
    card: AdminOperationalLiveCard,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(listOf(PediloPanel, PediloPanel.copy(alpha = 0.86f))),
                RoundedCornerShape(16.dp),
            )
            .border(1.dp, PediloLine.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(PediloOrange.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .border(1.dp, PediloOrange.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(card.icon, color = PediloOrange, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
                Text(card.title, color = PediloText, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
            }
            Text(card.priority, color = PediloOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Text(card.countLabel, color = PediloText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(card.detail, color = PediloMuted, fontSize = 13.sp, lineHeight = 17.sp)
        Text(card.tension, color = PediloOrange.copy(alpha = 0.88f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
    orderDetailEntries: List<AdminOrderDetailEntry> = emptyList(),
    onOrderDetail: (AdminOrderDetailEntry) -> Unit = {},
    onOperationalProfile: (AdminOperationalProfileKind) -> Unit = {},
    onConfigurationConvergence: () -> Unit = {},
    onRoleAccessConvergence: (AdminRoleAccessConvergenceStep) -> Unit = {},
) {
    val allowStoreProfile = title in listOf(
        "Vendiendo ahora",
        "Sin respuesta",
        "Pausados",
        "Con configuración pendiente",
        "Sin productos vendibles",
        "Local no responde",
    )
    val allowDriverProfile = title in listOf(
        "Libres",
        "Ocupados",
        "Pendientes de respuesta",
        "Con incidencia",
        "Esperando repartidor",
        "En entrega",
    )
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
        items(orderDetailEntries) { entry ->
            AdminOperationLiveCardView(
                card = AdminOperationalLiveCard(
                    icon = "Ord",
                    title = entry.label,
                    countLabel = "Pedido detectado",
                    detail = entry.note,
                    priority = "Prioridad operativa",
                    tension = "Abrir lectura completa",
                ),
                onClick = { onOrderDetail(entry) },
            )
        }
        if (allowStoreProfile) {
            item {
                AdminOperationLiveCardView(
                    card = AdminOperationalLiveCard(
                        icon = "Loc",
                        title = "Local operativo",
                        countLabel = "Lectura de local",
                        detail = "Estado operativo del local relacionado",
                        priority = "Prioridad media",
                        tension = "Contexto comercial",
                    ),
                    onClick = { onOperationalProfile(AdminOperationalProfileKind.Store) },
                )
            }
        }
        if (allowDriverProfile) {
            item {
                AdminOperationLiveCardView(
                    card = AdminOperationalLiveCard(
                        icon = "Drv",
                        title = "Repartidor operativo",
                        countLabel = "Lectura de repartidor",
                        detail = "Estado operativo del repartidor relacionado",
                        priority = "Prioridad media",
                        tension = "Contexto de entrega",
                    ),
                    onClick = { onOperationalProfile(AdminOperationalProfileKind.Driver) },
                )
            }
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
    onOpenStore: () -> Unit,
    onOpenDriver: () -> Unit,
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
                summary = "Ficha operativa read-only del pedido.",
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
                    "Número visible" to visibleNumber,
                    "Tracking" to trackingNumber.adminDisplayValue(),
                    "ID público" to publicOrderNumber.adminDisplayValue(),
                    "ID interno" to orderId.adminDisplayValue(),
                    "Origen" to AdminOperationOrderClassification.sourceLabel(source, requestType),
                    "Tipo de pedido" to requestType.adminDisplayValue(),
                ),
            )
        }
        item {
            AdminOrderFactPanel(
                facts = listOf(
                    "Estado actual" to status.adminDisplayValue(),
                    "Estado público" to publicStatus.adminDisplayValue(),
                    "Estado operativo" to operationalStatus.adminDisplayValue(),
                    "Activo" to if (status.lowercase() in listOf("done", "completed", "cancelled", "canceled")) "No" else "Sí",
                    "Demorado" to if (delaySignal) "Con señal operativa" else "Sin demora registrada",
                    "Problema" to if (activeIncident || needsAttention) "Con señal operativa" else "Sin problemas registrados",
                    "Prioridad" to priority.adminDisplayValue("Normal"),
                    "Responsable" to responsible.adminDisplayValue("Sin asignar"),
                ),
            )
        }
        item {
            AdminOrderFactPanel(
                facts = listOf(
                    "Persona usuaria" to detail.adminPersonName(),
                    "Teléfono/contacto" to "Sin dato disponible",
                    "Dirección" to "Sin dato disponible",
                ),
            )
        }
        item {
            AdminOrderFactPanel(
                facts = listOf(
                    "Resumen" to detail?.itemsSummary.adminItemsSummary(),
                    "Observaciones" to "Sin dato disponible",
                    "Operación" to requestType.adminDisplayValue(),
                ),
            )
        }
        item {
            AdminOrderFactPanel(
                facts = listOf(
                    "Local / origen" to storeName.adminDisplayValue(),
                    "Fuente" to source.adminDisplayValue(),
                ),
            )
        }
        item {
            AdminOrderFactPanel(
                facts = listOf(
                    "Total" to total.adminDisplayValue(),
                    "Forma de pago" to "Sin dato disponible",
                    "Estado de pago" to "Sin dato disponible",
                ),
            )
        }
        item {
            AdminOrderFactPanel(
                facts = listOf(
                    "Creado" to createdAt.adminMillisValue(),
                    "Última actualización" to detail?.updatedAtMillis.adminMillisValue(),
                    "Referencia temporal" to if (createdAt == null && detail?.updatedAtMillis == null) "Aún no registrado" else "Registrada",
                ),
            )
        }
        item {
            AdminInfoPanel(
                title = "Problemas / demoras",
                text = when {
                    activeIncident -> "Incidencia operativa registrada."
                    needsAttention -> "El pedido requiere atención operativa."
                    delaySignal -> "Con señal de demora en el grupo operativo actual."
                    else -> "Sin problemas registrados."
                },
            )
        }
        item {
            AdminInfoPanel(
                title = "Historial operativo",
                text = detail?.lastEventSummary.adminDisplayValue("Historial operativo aún no disponible"),
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
    val (status, detail) = when {
        activeIncident -> "Con incidencia" to "Hay una incidencia operativa activa."
        needsAttention -> "Necesita atención" to "El pedido requiere seguimiento operativo."
        else -> when (variant) {
            OperationOrderVariant.WithProblem -> "Con señal operativa" to "El grupo actual indica revisión operativa."
            OperationOrderVariant.NeedsAttention -> "Con señal operativa" to "El grupo actual indica seguimiento operativo."
            else -> "Lectura operativa" to "Estado leído desde los datos disponibles."
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
            "Operativo: ${operationalStatus.ifBlank { "Sin dato" }} · Responsable: ${responsibleRole.ifBlank { "Sin asignar" }} · Prioridad: ${priority.ifBlank { "normal" }}",
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
                Text(value, color = PediloText, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
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

@Composable
private fun AdminOrderSolveScreen(stage: OperationSolveStage, onNext: (OperationSolveStage) -> Unit) {
    val content = when (stage) {
        OperationSolveStage.Start -> SolveStageContent(
            title = "Solucionar",
            summary = "Inicio de revisión para este pedido.",
            panelTitle = "Contexto",
            panelText = "El pedido requiere intervención y revisión de impacto antes de cualquier acción operativa.",
            actionTitle = "Ver opciones",
            actionNote = "Abrí las alternativas de resolución disponibles en esta etapa.",
            next = OperationSolveStage.Options,
        )
        OperationSolveStage.Options -> SolveStageContent(
            title = "Opciones",
            summary = "Elegí una línea de trabajo para este caso.",
            panelTitle = "Opciones de resolución",
            panelText = "Acción recomendada: Revisar respuesta pendiente. Acciones secundarias: Marcar para seguimiento. Excepción Admin: Intervención excepcional.",
            actionTitle = "Continuar",
            actionNote = "Revisá la acción sensible antes del resultado.",
            next = OperationSolveStage.SensitiveAction,
        )
        OperationSolveStage.SensitiveAction -> SolveStageContent(
            title = "Acción sensible",
            summary = "Revisión previa de impacto.",
            panelTitle = "Impacto",
            panelText = "Esta representación muestra qué cambiaría en operación y qué quedaría sin cambios. No ejecuta modificaciones reales.",
            actionTitle = "Confirmar visualmente",
            actionNote = "Pasá al resultado representado.",
            next = OperationSolveStage.Result,
        )
        OperationSolveStage.Result -> SolveStageContent(
            title = "Resultado",
            summary = "Cierre de la secuencia guiada.",
            panelTitle = "Resultado preparado",
            panelText = "La revisión quedó lista para volver al pedido y continuar el seguimiento operativo.",
            actionTitle = "Reiniciar recorrido",
            actionNote = "Revisar nuevamente el flujo de solución.",
            next = OperationSolveStage.Start,
        )
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
            AdminHeader(title = content.title, eyebrow = "Operación", summary = content.summary, onSignOut = {}, showSignOut = false)
        }
        item {
            AdminInfoPanel(title = content.panelTitle, text = content.panelText)
        }
        item {
            AdminActionCard(title = content.actionTitle, note = content.actionNote, onClick = { onNext(content.next) })
        }
    }
}
private data class SolveStageContent(
    val title: String,
    val summary: String,
    val panelTitle: String,
    val panelText: String,
    val actionTitle: String,
    val actionNote: String,
    val next: OperationSolveStage,
)

@Composable
private fun AdminOperationalProfileScreen(kind: AdminOperationalProfileKind, state: String) {
    val title = if (kind == AdminOperationalProfileKind.Store) "Local operativo" else "Repartidor operativo"
    val context = if (kind == AdminOperationalProfileKind.Store) {
        "Este estado se usa para seguimiento operativo del local sin cambiar su configuración ni productos."
    } else {
        "Este estado se usa para seguimiento operativo del repartidor."
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
            AdminHeader(title = title, eyebrow = "Operación", summary = "Seguimiento operativo concreto.", onSignOut = {}, showSignOut = false)
        }
        item { AdminInfoPanel(title = "Estado", text = state) }
        item { AdminInfoPanel(title = "Contexto", text = context) }
        item {
            AdminDisabledActionCard(
                title = "Acción no disponible desde esta vista",
                note = "Esta vista es de lectura y seguimiento.",
            )
        }
    }
}

private fun AdminRoute.root(): AdminRoot = when (this) {
    AdminRoute.Operation -> AdminRoot.Operation
    AdminRoute.Configuration -> AdminRoot.Configuration
    AdminRoute.RoleAccess -> AdminRoot.RoleAccess
    is AdminRoute.OperationSection -> AdminRoot.Operation
    is AdminRoute.OperationSubsection -> AdminRoot.Operation
    is AdminRoute.OperationOrderDetail -> AdminRoot.Operation
    is AdminRoute.OperationOrderSolve -> AdminRoot.Operation
    is AdminRoute.OperationOperationalProfile -> AdminRoot.Operation
    is AdminRoute.ConfigurationSection -> AdminRoot.Configuration
    is AdminRoute.ConfigurationSubsection -> AdminRoot.Configuration
    is AdminRoute.ConfigurationConvergence -> AdminRoot.Configuration
    is AdminRoute.RoleAccessSection -> AdminRoot.RoleAccess
    is AdminRoute.RoleAccessSubsection -> AdminRoot.RoleAccess
    is AdminRoute.RoleAccessConvergence -> AdminRoot.RoleAccess
    is AdminRoute.TodayOrdersCategory -> AdminRoot.Operation
    is AdminRoute.TodayOrdersSubsection -> AdminRoot.Operation
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

private fun List<AdminOrderSummary>.forOperationSubsection(
    sectionTitle: String,
    subsectionTitle: String,
): List<AdminOrderSummary> {
    val signals = this.map { it to AdminOperationOrderSignals.from(it) }
    return when (sectionTitle) {
        "Pedidos activos" -> signals.filter { (_, s) ->
            when (subsectionTitle) {
                "Esperando local" -> AdminOperationOrderClassification.activeBucket(s) == AdminActiveOrdersBucket.WAITING_STORE
                "Preparando" -> AdminOperationOrderClassification.activeBucket(s) == AdminActiveOrdersBucket.PREPARING
                "Esperando repartidor" -> AdminOperationOrderClassification.activeBucket(s) == AdminActiveOrdersBucket.WAITING_DRIVER
                "En entrega" -> AdminOperationOrderClassification.activeBucket(s) == AdminActiveOrdersBucket.IN_DELIVERY
                else -> false
            }
        }.map { it.first }
        "Pedidos con problemas" -> signals.filter { (_, s) ->
            when (subsectionTitle) {
                "Local no responde" -> AdminOperationOrderClassification.problemBucket(s) == AdminProblemOrdersBucket.STORE_NOT_RESPONDING
                "Reclamo del cliente" -> AdminOperationOrderClassification.problemBucket(s) == AdminProblemOrdersBucket.CUSTOMER_CLAIM
                else -> false
            }
        }.map { it.first }
        else -> emptyList()
    }
}

private fun List<AdminOrderSummary>.forTodaySubsection(
    categoryTitle: String,
    subsectionTitle: String,
): List<AdminOrderSummary> {
    val signals = this.map { it to AdminOperationOrderSignals.from(it) }
    return when (categoryTitle) {
        "Activos" -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.todayBucket(s) == AdminTodayOrdersBucket.ACTIVE &&
                subsectionTitle == "Esperando local" &&
                AdminOperationOrderClassification.activeBucket(s) == AdminActiveOrdersBucket.WAITING_STORE
        }.map { it.first }
        "Finalizados" -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.todayBucket(s) == AdminTodayOrdersBucket.FINISHED
        }.map { it.first }
        "Cancelados" -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.todayBucket(s) == AdminTodayOrdersBucket.CANCELLED
        }.map { it.first }
        "Demorados" -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.todayBucket(s) == AdminTodayOrdersBucket.DELAYED
        }.map { it.first }
        "Con problemas" -> signals.filter { (_, s) ->
            when (subsectionTitle) {
                "Local no responde" -> AdminOperationOrderClassification.problemBucket(s) == AdminProblemOrdersBucket.STORE_NOT_RESPONDING
                "Reclamo del cliente" -> AdminOperationOrderClassification.problemBucket(s) == AdminProblemOrdersBucket.CUSTOMER_CLAIM
                else -> false
            }
        }.map { it.first }
        else -> emptyList()
    }
}
