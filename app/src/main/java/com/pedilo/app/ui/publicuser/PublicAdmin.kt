package com.pedilo.app.ui.publicuser

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
import com.pedilo.app.core.model.AdminOperationOrderClassification
import com.pedilo.app.core.model.AdminOperationOrderSignals
import com.pedilo.app.core.model.AdminOrderDetail
import com.pedilo.app.core.model.AdminOrderSummary
import com.pedilo.app.core.model.AdminProblemOrdersBucket
import com.pedilo.app.core.model.AdminTodayOrdersBucket
import com.pedilo.app.core.result.CoreResult
import com.pedilo.app.core.runtime.adminOrdersUseCase
import kotlinx.coroutines.launch

private enum class AdminRoot(val label: String) {
    Operation("Operación"),
    Configuration("Configuración"),
    RoleAccess("Alta de roles"),
}

private enum class OperationOrderVariant {
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

private data class AdminEntry(
    val title: String,
    val note: String,
)

private data class AdminPriorityCard(
    val icon: String,
    val title: String,
    val count: Int,
    val note: String,
    val priority: String,
    val tension: String,
    val targetSection: String? = null,
)

private data class AdminOperationSection(
    val title: String,
    val summary: String,
    val contextTitle: String,
    val contextText: String,
    val entries: List<AdminEntry>,
)

private data class AdminTodayOrdersCategory(
    val title: String,
    val summary: String,
    val contextText: String,
    val entries: List<AdminEntry>,
)

private data class AdminOrderDetailEntry(
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

private val operationEntries = listOf(
    AdminEntry("Pedidos del día", "Movimiento completo de hoy"),
    AdminEntry("Pedidos activos", "Pedidos que siguen en curso"),
    AdminEntry("Pedidos con problemas", "Casos que necesitan revisión"),
    AdminEntry("Repartidores activos", "Estado operativo de repartidores"),
    AdminEntry("Locales activos", "Estado operativo de locales"),
)

private val todayOrdersCategories = listOf(
    AdminTodayOrdersCategory(
        title = "Activos",
        summary = "Pedidos del día que siguen en curso",
        contextText = "Pedidos recibidos que siguen abiertos dentro del día.",
        entries = listOf(
            AdminEntry("Esperando local", "Pedidos creados esperando respuesta del local"),
        ),
    ),
    AdminTodayOrdersCategory(
        title = "Finalizados",
        summary = "Pedidos del día cerrados correctamente",
        contextText = "Pedidos entregados, retirados o enviados dentro del día.",
        entries = listOf(
            AdminEntry("Entregados", "Pedidos completados exitosamente"),
            AdminEntry("Retirados", "Retirados en local"),
            AdminEntry("Enviados", "Completados por envío"),
        ),
    ),
    AdminTodayOrdersCategory(
        title = "Cancelados",
        summary = "Pedidos del día cerrados sin completar",
        contextText = "Pedidos cancelados antes de completarse.",
        entries = listOf(
            AdminEntry("Cancelados por cliente", "Iniciados por la persona usuaria"),
            AdminEntry("Cancelados por local", "Iniciados por el comercio"),
            AdminEntry("Cancelados por operación", "Iniciados por el equipo"),
        ),
    ),
    AdminTodayOrdersCategory(
        title = "Demorados",
        summary = "Pedidos del día con tiempo excedido",
        contextText = "Pedidos con tiempo de espera excedido en alguna etapa.",
        entries = listOf(
            AdminEntry("Esperando local", "Espera de aceptación"),
            AdminEntry("Preparando", "Espera de preparación"),
            AdminEntry("En entrega", "Espera de reparto"),
        ),
    ),
    AdminTodayOrdersCategory(
        title = "Con problemas",
        summary = "Pedidos del día marcados con incidencia",
        contextText = "Pedidos con incidencias que requieren revisión operativa.",
        entries = listOf(
            AdminEntry("Local no responde", "Requiere seguimiento"),
            AdminEntry("Reclamo del cliente", "Requiere revisión"),
        ),
    ),
)

private val operationSections = listOf(
    AdminOperationSection(
        title = "Pedidos del día",
        summary = "Movimiento completo de pedidos del día.",
        contextTitle = "Vista del día",
        contextText = "Agrupa los estados principales sin abrir pedidos ni resolver casos.",
        entries = listOf(
            AdminEntry("Activos", "Pedidos del día que siguen en curso"),
            AdminEntry("Finalizados", "Pedidos cerrados correctamente"),
            AdminEntry("Cancelados", "Pedidos cerrados sin completar"),
            AdminEntry("Demorados", "Pedidos con tiempo excedido"),
            AdminEntry("Con problemas", "Pedidos marcados con incidencia"),
        ),
    ),
    AdminOperationSection(
        title = "Pedidos activos",
        summary = "Pedidos vivos dentro de la operación actual.",
        contextTitle = "Operación en curso",
        contextText = "Ordena los momentos del pedido sin abrir acciones finales.",
        entries = listOf(
            AdminEntry("Esperando local", "Pedidos esperando respuesta del local"),
            AdminEntry("Preparando", "Pedidos en preparación"),
            AdminEntry("Esperando repartidor", "Pedidos listos para asignación"),
            AdminEntry("En entrega", "Pedidos en camino"),
        ),
    ),
    AdminOperationSection(
        title = "Pedidos con problemas",
        summary = "Clasificación inicial de casos que requieren atención.",
        contextTitle = "Casos a revisar",
        contextText = "Separa motivos para lectura operativa sin cerrar incidencias.",
        entries = listOf(
            AdminEntry("Local no responde", "Pedidos detenidos por falta de respuesta"),
            AdminEntry("Reclamo del cliente", "Casos iniciados por aviso del cliente"),
        ),
    ),
    AdminOperationSection(
        title = "Repartidores activos",
        summary = "Estado operativo de repartidores.",
        contextTitle = "Equipo en movimiento",
        contextText = "Agrupa disponibilidad sin tocar perfiles, permisos ni asignaciones.",
        entries = listOf(
            AdminEntry("Libres", "Repartidores disponibles"),
            AdminEntry("Ocupados", "Repartidores con pedido asignado"),
            AdminEntry("Pendientes de respuesta", "Casos esperando confirmación"),
            AdminEntry("Con incidencia", "Situaciones que requieren revisión"),
        ),
    ),
    AdminOperationSection(
        title = "Locales activos",
        summary = "Estado operativo de locales.",
        contextTitle = "Locales en operación",
        contextText = "Ordena señales operativas sin tocar locales, productos ni visibilidad.",
        entries = listOf(
            AdminEntry("Vendiendo ahora", "Locales disponibles para recibir pedidos"),
            AdminEntry("Sin respuesta", "Locales que no respondieron a tiempo"),
            AdminEntry("Pausados", "Locales temporalmente detenidos"),
            AdminEntry("Con configuración pendiente", "Locales con datos por revisar"),
            AdminEntry("Sin productos vendibles", "Locales sin oferta disponible"),
        ),
    ),
)

private val configurationEntries = listOf(
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
).map { AdminEntry(it, "Ajustes preparados para la app") }

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

private val roleEntries = listOf(
    "Usuarios del equipo",
    "Administradores",
    "Locales store",
    "Repartidores driver",
    "Altas pendientes",
    "Usuarios inactivos",
    "Vinculaciones pendientes",
).map { AdminEntry(it, "Organización de accesos") }

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

private fun orderDetailEntriesFor(
    sectionTitle: String,
    subsectionTitle: String,
    orders: List<AdminOrderSummary>,
): List<AdminOrderDetailEntry> {
    val real = orders.firstOrNull()
    val variant = when {
        sectionTitle == "Pedidos con problemas" && subsectionTitle == "Local no responde" -> OperationOrderVariant.WithProblem
        sectionTitle == "Pedidos activos" && subsectionTitle == "Esperando repartidor" -> OperationOrderVariant.ActionUnavailable
        sectionTitle == "Pedidos activos" && subsectionTitle == "Esperando local" -> OperationOrderVariant.NeedsAttention
        else -> OperationOrderVariant.Normal
    }
    return real?.let {
        val sourceLabel = AdminOperationOrderClassification.sourceLabel(it.source, it.requestType)
        listOf(
            AdminOrderDetailEntry(
                label = if (it.trackingNumber.isNotBlank()) it.trackingNumber else "Pedido #____",
                note = "${it.publicStatus.ifBlank { "Pedido recibido" }} · $sourceLabel",
                variant = variant,
                realOrderId = it.id,
            ),
        )
    } ?: emptyList()
}

@Composable
fun AdminApp(onSignOutConfirmed: () -> Unit) {
    var route by remember { mutableStateOf<AdminRoute>(AdminRoute.Operation) }
    var showSignOut by remember { mutableStateOf(false) }
    var readOnlyOrders by remember { mutableStateOf<List<AdminOrderSummary>>(emptyList()) }
    var readOnlyOrderDetails by remember { mutableStateOf<Map<String, AdminOrderDetail>>(emptyMap()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        when (val result = adminOrdersUseCase().invoke()) {
            is CoreResult.Success -> readOnlyOrders = result.value
            is CoreResult.Failure -> readOnlyOrders = emptyList()
        }
    }

    val todayCount = readOnlyOrders.todayOrders().values.sumOf { it.size }
    val activeCount = readOnlyOrders.activeOrders().values.sumOf { it.size }
    val problemCount = readOnlyOrders.problemOrders().values.sumOf { it.size }
    val inDeliveryCount = readOnlyOrders.activeOrders()[AdminActiveOrdersBucket.IN_DELIVERY]?.size ?: 0
    val finishedRecentCount = readOnlyOrders.todayOrders()[AdminTodayOrdersBucket.FINISHED]?.size ?: 0
    val delayedCount = readOnlyOrders.todayOrders()[AdminTodayOrdersBucket.DELAYED]?.size ?: 0

    val operationDeskCards = listOf(
        AdminPriorityCard(
            icon = "!",
            title = "Necesitan atención",
            count = problemCount,
            note = if (problemCount == 0) "No hay pedidos con revisión urgente." else "Pedidos que requieren revisión operativa.",
            priority = "Prioridad alta",
            tension = "Casos sensibles",
            targetSection = "Pedidos con problemas",
        ),
        AdminPriorityCard(
            icon = "R",
            title = "Demorados",
            count = delayedCount,
            note = if (delayedCount == 0) "Sin pedidos Demorados por ahora." else "Pedidos con tiempo excedido o espera prolongada.",
            priority = "Prioridad media",
            tension = "Ritmo afectado",
            targetSection = "Pedidos del día",
        ),
        AdminPriorityCard(
            icon = "?",
            title = "Sin responsable",
            count = 0,
            note = "Sin información disponible todavía.",
            priority = "Prioridad operativa",
            tension = "Cobertura parcial",
            targetSection = "Pedidos activos",
        ),
        AdminPriorityCard(
            icon = "OK",
            title = "En curso normal",
            count = activeCount,
            note = if (activeCount == 0) "No hay pedidos activos ahora." else "Pedidos avanzando sin alertas visibles.",
            priority = "Seguimiento",
            tension = "Sin alertas",
            targetSection = "Pedidos activos",
        ),
        AdminPriorityCard(
            icon = ">",
            title = "En entrega",
            count = inDeliveryCount,
            note = if (inDeliveryCount == 0) "No hay pedidos en entrega ahora." else "Pedidos actualmente en etapa de entrega.",
            priority = "Seguimiento",
            tension = "Tránsito activo",
            targetSection = "Pedidos activos",
        ),
        AdminPriorityCard(
            icon = "✓",
            title = "Finalizados recientes",
            count = finishedRecentCount,
            note = if (finishedRecentCount == 0) "No hay cierres recientes para mostrar." else "Pedidos cerrados recientemente.",
            priority = "Cierre del ciclo",
            tension = "Cierre estable",
            targetSection = "Pedidos del día",
        ),
    )

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
                deskCards = operationDeskCards,
                totalOrdersToday = todayCount,
                onOpenSection = { title ->
                    operationSections.firstOrNull { it.title == title }?.let {
                        route = AdminRoute.OperationSection(it)
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
                val orderEntries = orderDetailEntriesFor(current.section.title, current.title, readOnlyOrders)
                AdminSectionScreen(
                    root = AdminRoot.Operation,
                    title = current.title,
                    summary = "Submundo operativo preparado para organizar la siguiente capa.",
                    panelTitle = current.section.title,
                    panelText = "Este espacio mantiene la separación operativa sin listados reales ni acciones disponibles.",
                    orderDetailEntries = orderEntries,
                    onOrderDetail = { variant ->
                        route = AdminRoute.OperationOrderDetail(
                            returnRoute = AdminRoute.OperationSubsection(current.section, current.title),
                            variant = variant,
                            realOrderId = orderEntries.firstOrNull { it.variant == variant }?.realOrderId,
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
                detail = current.realOrderId?.let { readOnlyOrderDetails[it] },
                onSolve = {
                    route = AdminRoute.OperationOrderSolve(
                        returnRoute = AdminRoute.OperationOrderDetail(current.returnRoute, current.variant, current.realOrderId),
                        stage = OperationSolveStage.Start,
                    )
                },
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
                        when (val result = adminOrdersUseCase().getDetail(orderId)) {
                            is CoreResult.Success -> readOnlyOrderDetails = readOnlyOrderDetails + (orderId to result.value)
                            is CoreResult.Failure -> Unit
                        }
                    }
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
                summary = "Submundo de pedidos del día preparado para organizar la siguiente capa.",
                panelTitle = current.category.title,
                panelText = "Este espacio clasifica el movimiento del día sin mostrar listados reales ni acciones disponibles.",
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
            modifier = Modifier.align(Alignment.BottomCenter),
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
                eyebrow = "Operación",
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
private fun AdminOperationDeskScreen(
    deskCards: List<AdminPriorityCard>,
    totalOrdersToday: Int,
    onOpenSection: (String) -> Unit,
    onSignOut: () -> Unit,
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
                title = "Pédilo Admin",
                eyebrow = "Mesa Operativa Viva",
                summary = "Lectura en tiempo real sin ejecutar acciones operativas.",
                onSignOut = onSignOut,
                showSignOut = true,
            )
        }
        item {
            AdminInfoPanel(
                title = "Resumen operativo",
                text = if (totalOrdersToday == 0) {
                    "No hay pedidos para mostrar en esta sección."
                } else {
                    "Pedidos del día registrados: $totalOrdersToday"
                },
            )
        }
        items(deskCards) { card ->
            AdminPriorityCardView(
                card = card,
                onClick = { card.targetSection?.let(onOpenSection) },
            )
        }
        item {
            AdminInfoPanel(
                title = "Capas de lectura",
                text = "Entrá por prioridad para abrir detalle por estado y después cada pedido.",
            )
        }
    }
}

@Composable
private fun AdminPriorityCardView(
    card: AdminPriorityCard,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(PediloPanel, PediloPanel.copy(alpha = 0.9f)),
                ),
                RoundedCornerShape(16.dp),
            )
            .border(1.dp, PediloLine.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(PediloOrange.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .border(1.dp, PediloOrange.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(card.icon, color = PediloOrange, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
                Text(
                    text = card.title,
                    style = TextStyle(
                        color = PediloText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.sp,
                    ),
                )
            }
            Text(
                text = card.priority,
                style = TextStyle(
                    color = PediloOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp,
                ),
            )
        }
        Text(
            text = "${card.count} pedidos",
            style = TextStyle(
                color = PediloText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp,
            ),
        )
        Text(
            text = card.note,
            style = TextStyle(
                color = PediloMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp,
            ),
        )
        Text(
            text = card.tension,
            style = TextStyle(
                color = PediloOrange.copy(alpha = 0.9f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp,
            ),
        )
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
        items(category.entries) {
            AdminEntryCard(entry = it, onClick = { onEntry(it) })
        }
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
    onOrderDetail: (OperationOrderVariant) -> Unit = {},
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
            AdminEntryCard(entry = AdminEntry(entry.label, entry.note), onClick = { onOrderDetail(entry.variant) })
        }
        if (allowStoreProfile) {
            item {
                AdminEntryCard(
                    entry = AdminEntry("Local operativo", "Estado operativo del local relacionado"),
                    onClick = { onOperationalProfile(AdminOperationalProfileKind.Store) },
                )
            }
        }
        if (allowDriverProfile) {
            item {
                AdminEntryCard(
                    entry = AdminEntry("Repartidor operativo", "Estado operativo del repartidor relacionado"),
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
    detail: AdminOrderDetail?,
    onSolve: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenDriver: () -> Unit,
    onLoadDetail: (String) -> Unit,
) {
    orderId?.let { LaunchedEffect(it) { onLoadDetail(it) } }
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
                title = detail?.trackingNumber?.ifBlank { "Pedido #____" } ?: "Pedido #____",
                eyebrow = "Operación",
                summary = detail?.publicStatus?.ifBlank { "Seguimiento del pedido." } ?: "Qué está pasando con este pedido ahora.",
                onSignOut = {},
                showSignOut = false,
            )
        }
        item {
            AdminOrderStatusPanel(variant = variant)
        }
        when (variant) {
            OperationOrderVariant.Normal -> {
                item {
                    AdminInfoPanel(
                        title = "Contexto",
                        text = "Persona esperando entrega · Local confirmó preparación · Reparto en curso.",
                    )
                }
                item {
                    AdminInfoPanel(
                        title = "Datos del pedido",
                        text = buildString {
                            appendLine("Estado: ${detail?.status.orEmpty().ifBlank { "Sin dato" }}")
                            appendLine("Origen: ${detail?.source.orEmpty().ifBlank { "Sin dato" }}")
                            appendLine("Tipo: ${detail?.requestType.orEmpty().ifBlank { "Sin dato" }}")
                            append("Local: ${detail?.storeName.orEmpty().ifBlank { "Sin dato" }}")
                        },
                    )
                }
            }
            OperationOrderVariant.NeedsAttention -> {
                item {
                    AdminInfoPanel(
                        title = "Atención requerida",
                        text = "El local aún no confirmó este pedido.",
                    )
                }
                item {
                    AdminOrderFactPanel(
                        facts = listOf(
                            "Qué necesita" to "Confirmación del local",
                            "Quién debería actuar" to "Equipo de operación",
                            "Desde cuándo" to "Hace unos minutos",
                            "Impacto" to "El cliente sigue esperando respuesta",
                        ),
                    )
                }
                item {
                    AdminDisabledActionCard(
                        title = "Revisión pendiente",
                        note = "Acción guiada en próximo bloque",
                    )
                }
            }
            OperationOrderVariant.WithProblem -> {
                item {
                    AdminOrderFactPanel(
                        facts = listOf(
                            "Problema" to "El local no responde",
                            "Qué se esperaba" to "Aceptación del pedido",
                            "Qué no ocurrió" to "Sin confirmación del local",
                            "Responsable actual" to "Local asignado",
                            "Tiempo detenido" to "Desde hace unos minutos",
                            "Impacto" to "Pedido detenido sin avanzar",
                        ),
                    )
                }
                item {
                    AdminActionCard(
                        title = "Solucionar",
                        note = "Revisá opciones de resolución sin ejecutar cambios reales.",
                        onClick = onSolve,
                    )
                }
                item {
                    AdminEntryCard(
                        entry = AdminEntry("Local relacionado", "Ver estado operativo del local"),
                        onClick = onOpenStore,
                    )
                }
            }
            OperationOrderVariant.ActionUnavailable -> {
                item {
                    AdminInfoPanel(
                        title = "Motivo",
                        text = "Este pedido no admite cambios desde aquí en este momento.",
                    )
                }
                item {
                    AdminInfoPanel(
                        title = "Qué podés hacer ahora",
                        text = "Revisá el estado y volvé cuando haya una acción habilitada.",
                    )
                }
                item {
                    AdminEntryCard(
                        entry = AdminEntry("Repartidor relacionado", "Ver estado operativo del repartidor"),
                        onClick = onOpenDriver,
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminOrderStatusPanel(variant: OperationOrderVariant) {
    val (status, detail) = when (variant) {
        OperationOrderVariant.Normal -> "Estado normal" to "El pedido avanza dentro de lo previsto."
        OperationOrderVariant.NeedsAttention -> "Necesita atención" to "Hay un punto que requiere seguimiento operativo."
        OperationOrderVariant.WithProblem -> "Con problema" to "El pedido quedó detenido por una incidencia."
        OperationOrderVariant.ActionUnavailable -> "Acción no disponible" to "No hay gestiones habilitadas en este momento."
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
        "Este estado se usa para seguimiento operativo del repartidor sin cambios de cuenta, permisos ni accesos."
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
                note = "Las acciones operativas finales se habilitan en etapas posteriores.",
            )
        }
    }
}

@Composable
private fun AdminHeader(
    title: String,
    eyebrow: String,
    summary: String,
    onSignOut: () -> Unit,
    showSignOut: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pediloCardDepth(RoundedCornerShape(18.dp))
            .background(PediloWarmPanelBrush, RoundedCornerShape(18.dp))
            .border(1.dp, PediloGoldLine, RoundedCornerShape(18.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                eyebrow,
                color = PediloWarning,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            if (showSignOut) {
                Text(
                    "Cerrar sesión",
                    color = PediloText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(PediloPanel.copy(alpha = 0.82f), RoundedCornerShape(14.dp))
                        .border(1.dp, PediloOrange.copy(alpha = 0.42f), RoundedCornerShape(14.dp))
                        .clickable(role = Role.Button, onClick = onSignOut)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                )
            }
        }
        Text(
            title,
            color = PediloOrange,
            fontSize = 30.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            style = TextStyle(brush = PediloPrimaryBrush),
        )
        Text(summary, color = PediloMuted, fontSize = 14.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun AdminEntryCard(entry: AdminEntry, onClick: () -> Unit) {
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
        Text(entry.title, color = PediloText, fontSize = 19.sp, lineHeight = 23.sp, fontWeight = FontWeight.ExtraBold)
        Text(entry.note, color = PediloMuted, fontSize = 13.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun AdminInfoPanel(title: String, text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloPanelSoft, RoundedCornerShape(15.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(15.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, color = PediloText, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text(text, color = PediloMuted, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun AdminBottomBar(
    current: AdminRoot,
    onOperation: () -> Unit,
    onConfiguration: () -> Unit,
    onRoleAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(94.dp)
            .navigationBarsPadding()
            .background(Brush.verticalGradient(listOf(PediloPanelSoft.copy(alpha = 0.96f), PediloPanel)), RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AdminBottomItem("Operación", current == AdminRoot.Operation, onOperation, Modifier.weight(1f))
        AdminBottomItem("Configuración", current == AdminRoot.Configuration, onConfiguration, Modifier.weight(1f))
        AdminBottomItem("Alta de roles", current == AdminRoot.RoleAccess, onRoleAccess, Modifier.weight(1f))
    }
}

@Composable
private fun AdminBottomItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(68.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (selected) PediloOrange.copy(alpha = 0.18f) else Color.Transparent, RoundedCornerShape(15.dp))
            .border(1.dp, if (selected) PediloOrange.copy(alpha = 0.62f) else PediloLine.copy(alpha = 0.45f), RoundedCornerShape(15.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            label,
            color = if (selected) PediloOrange else PediloMuted,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
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
