package com.pedilo.app.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedilo.app.core.model.AdminActiveOrdersBucket
import com.pedilo.app.core.model.AdminOrderPrimaryPlacement
import com.pedilo.app.core.model.AdminOperationOrderClassification
import com.pedilo.app.core.model.AdminOperationOrderSignals
import com.pedilo.app.core.model.AdminOrderDetail
import com.pedilo.app.core.model.AdminOrderSummary
import com.pedilo.app.core.model.AdminProblemOrdersBucket
import com.pedilo.app.core.result.CoreResult
import com.pedilo.app.core.runtime.adminOrdersUseCase
import com.pedilo.app.ui.admin.components.AdminBottomBar
import com.pedilo.app.ui.admin.components.AdminEntryCard
import com.pedilo.app.ui.admin.components.AdminHeader
import com.pedilo.app.ui.admin.components.AdminInfoPanel
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
import java.util.Calendar
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

private enum class AdminOrderSection {
    Summary,
    Operation,
    Delivery,
    Payment,
    Problems,
    History,
    Options,
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
    data class OperationOrderSection(
        val detailRoute: OperationOrderDetail,
        val section: AdminOrderSection,
    ) : AdminRoute
    data class Section(val root: AdminRoot, val title: String) : AdminRoute
    data class ConfigurationSection(val section: AdminConfigurationSection) : AdminRoute
    data class ConfigurationSubsection(val section: AdminConfigurationSection, val title: String) : AdminRoute
    data class ConfigurationPublicWorld(val world: String) : AdminRoute
    data class ConfigurationPublicWorldPart(val world: String, val part: String) : AdminRoute
    data class ConfigurationPublicWorldEditor(
        val world: String,
        val part: String,
        val item: String,
        val step: AdminPublicHomeEditorStep,
    ) : AdminRoute
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
    Audit,
}

private enum class AdminPublicHomeEditorStep {
    Detail,
    Edit,
    Preview,
    Impact,
    Confirmation,
    Result,
    Audit,
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
    Audit,
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
    val icon: ImageVector,
    val title: String,
    val countLabel: String,
    val detail: String,
    val priority: String,
    val tension: String,
)

private data class AdminOrderNavigationEntry(
    val section: AdminOrderSection,
    val icon: ImageVector,
    val title: String,
    val note: String,
)

private data class AdminOperationSubcard(
    val icon: ImageVector,
    val title: String,
    val value: String,
    val detail: String,
    val preview: List<String> = emptyList(),
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

private val adminBottomBarReservedPadding = 128.dp
private val adminContentBottomPadding = 24.dp

private fun configurationSection(
    title: String,
    summary: String,
    context: String,
    vararg entries: Pair<String, String>,
) = AdminConfigurationSection(
    title = title,
    summary = summary,
    contextTitle = "Tablero de $title",
    contextText = context,
    entries = entries.map { AdminEntry(it.first, it.second) },
)

private val configurationSections = listOf(
    configurationSection(
        "Público", "Contenido visible para personas usuarias.", "Información humana, segura y preparada para publicar.",
        "Home público" to "Orden, avisos y convenciones", "Compra / Retiro" to "Flujo público central",
        "Tienda" to "Presentación de oferta", "Seguimiento / Reclamos" to "Consulta y ayuda preparada",
    ),
    configurationSection(
        "Locales", "Entidades comerciales, catálogo y capacidad.", "Un local incompleto no se publica ni opera.",
        "Listado de locales" to "Buscar, filtrar y ver detalle", "Datos del local" to "Identidad, contacto y dirección",
        "Horarios y capacidad" to "Apertura, pausas y saturación", "Publicación y visibilidad" to "Estado público y administrativo",
        "Catálogo del local" to "Productos ligados al local", "Productos y variantes" to "Oferta, extras e imágenes",
        "Disponibilidad y promos" to "Oferta para pedidos futuros", "Pendientes de revisión" to "Faltantes e impacto",
    ),
    configurationSection(
        "Reparto", "Condiciones administrativas de repartidores.", "Define habilitación y finanzas; no opera entregas vivas.",
        "Listado de repartidores" to "Buscar y ver detalle", "Habilitación y bloqueos" to "Condiciones administrativas",
        "Documentación" to "Datos requeridos y pendientes", "Perfil operativo" to "Disponibilidad y condiciones",
        "Cierre financiero" to "Reglas, deuda y comprobantes", "Sonidos y avisos" to "Preferencias del repartidor",
    ),
    configurationSection(
        "Marketplace", "Orden y exposición de la oferta pública.", "Muestra oferta publicable de locales reales.",
        "Categorías" to "Crear, editar y ordenar", "Subcategorías" to "Organización visible",
        "Destacados y nuevos" to "Criterios de exposición", "Ofertas visibles" to "Promociones publicables",
        "Ranking público" to "Orden configurable", "Previsualización" to "Revisión antes de publicar",
    ),
    configurationSection(
        "Pedidos", "Reglas generales para pedidos futuros.", "Configura integridad sin abrir ni modificar pedidos vivos.",
        "Reglas de creación" to "Datos y condiciones obligatorias", "Estados públicos" to "Lectura visible del avance",
        "Estados internos" to "Etapas y estados ocultos", "Tracking y fallbacks" to "Seguimiento y contingencias",
        "Tiempos y cancelaciones" to "Timeouts y criterios futuros", "Cambios sensibles" to "Impacto y confirmación",
    ),
    configurationSection(
        "Precios", "Valores comerciales y operativos.", "Los cambios aplican a pedidos nuevos y conservan snapshots confirmados.",
        "Precios comerciales" to "Producto, variante, extra y promo", "Tarifas operativas" to "Envío, distancia y recargos",
        "Modo lluvia" to "Activación e impacto visible", "Partes de reparto" to "Repartidor y Pédilo",
        "Promociones" to "Vigencia y pausas", "Historial de precios" to "Consulta y auditoría",
    ),
    configurationSection(
        "Cobros", "Cómo, cuándo y quién paga o cobra.", "Define reglas futuras; no confirma pagos vivos.",
        "Formas de pago" to "Opciones permitidas", "Quién paga y cobra" to "Responsabilidades de cobro",
        "Pago al retirar" to "Reglas y montos requeridos", "Pago al entregar" to "Condiciones de cierre",
        "Comprobantes" to "Validaciones y pendientes", "Cancelación y devolución" to "Reglas e impacto",
    ),
    configurationSection(
        "Mensajes", "Comunicación preparada por contexto.", "Comunica sin gobernar estados ni enviar por canales reales.",
        "Plantillas" to "Crear, editar y buscar", "Mensajes por estado" to "Persona usuaria, local y repartidor",
        "Mensajes por problema" to "Demora, cancelación y pago", "Avisos públicos" to "Publicar y despublicar",
        "Avisos internos" to "Comunicación Admin preparada", "Tono y canales" to "Previsualización y alcance",
    ),
    configurationSection(
        "Reglas", "Condiciones de integridad del sistema.", "Define condiciones; la autoridad real queda fuera de esta herramienta.",
        "Publicación de locales" to "Datos mínimos y bloqueos", "Publicación de productos" to "Completitud y revisión",
        "Creación de pedidos" to "Validaciones y borradores", "Solicitud de repartidor" to "Límites y datos requeridos",
        "Pagos y roles activos" to "Validaciones previas", "Confirmaciones sensibles" to "Impacto y auditoría",
    ),
    configurationSection(
        "Notificaciones", "Señales visuales y sonoras por rol.", "Cada señal tiene origen, destinatario y prioridad.",
        "Eventos notificables" to "Origen y agrupación", "Canales por rol" to "Destinatarios permitidos",
        "Badges y prioridad" to "Lectura visual", "Sonidos y silencios" to "Reglas anti-saturación",
        "Alertas críticas" to "Alcance y restricciones", "Prueba visual" to "Previsualización sin push real",
    ),
    configurationSection(
        "Métricas", "Criterios de lectura, ranking y visibilidad.", "Las métricas nacen de pedidos y eventos; no se inventan.",
        "Pedidos y productos" to "Indicadores y tendencias", "Locales" to "Rendimiento, demoras y rechazos",
        "Repartidores" to "Criterios operativos y financieros", "Marketplace" to "Exposición y ranking",
        "Visibilidad por rol" to "Indicadores habilitados", "Detalle y tendencia" to "Consulta preparada",
    ),
    configurationSection(
        "Auditoría", "Consulta de trazabilidad administrativa.", "La auditoría se consulta; no se edita ni se borra.",
        "Registro de cambios" to "Buscar y filtrar", "Publicaciones" to "Historial de contenido",
        "Cambios sensibles" to "Antes, después e impacto", "Precios y finanzas" to "Trazabilidad preparada",
        "Emergencias" to "Alcance y resultado", "Detalle de registro" to "Quién, cuándo y resultado",
    ),
    configurationSection(
        "Emergencias", "Contingencias controladas y auditables.", "Toda emergencia requiere impacto, confirmación y registro.",
        "Modo seguro" to "Alcance y restricciones", "Modo lluvia" to "Contingencia y recargo visible",
        "Pausa general" to "Impacto y confirmación", "Pausa de locales" to "Alcance comercial",
        "Pausa de reparto" to "Alcance operativo", "Avisos globales" to "Comunicación excepcional",
        "Registro posterior" to "Resultado y auditoría",
    ),
    configurationSection(
        "General", "Parámetros transversales mínimos.", "General no absorbe: deriva cada pendiente al bloque dueño.",
        "Estado global" to "Lectura consolidada", "Preferencias administrativas" to "Parámetros sin dueño",
        "Pendientes globales" to "Revisión transversal", "Derivación al bloque dueño" to "Ruta responsable",
    ),
)

private val configurationEntries = configurationSections.map {
    AdminEntry(it.title, "Abrir tablero")
}

private val publicWorldEntries = listOf(
    AdminEntry("Home público", "Contenido principal"),
    AdminEntry("Compra / Retiro", "Compra, retiro y ticket"),
    AdminEntry("Tienda", "Oferta pública"),
    AdminEntry("Seguimiento / Reclamos", "Información y ayuda"),
)

private val publicHomeEntries = listOf(
    AdminEntry("Encabezado", "Título, subtítulo e imagen"),
    AdminEntry("Accesos rápidos", "Nombre, orden y destino"),
    AdminEntry("Banner destacado", "Texto, imagen y botón"),
    AdminEntry("Ver más / Convenciones", "Información ampliada"),
    AdminEntry("Ofertas", "Orden y visibilidad"),
    AdminEntry("Nuevos locales", "Locales publicables"),
    AdminEntry("Buscador / tags", "Sugerencias y criterios"),
    AdminEntry("Vista previa del Home", "Revisión visual completa"),
)

private val publicPurchaseEntries = listOf(
    AdminEntry("Pantalla inicial", "Opciones y avisos visibles"),
    AdminEntry("Compra", "Textos e imágenes del flujo"),
    AdminEntry("Retiro / Envío", "Dirección, horario y pago visible"),
    AdminEntry("Confirmación", "Revisión antes de confirmar"),
    AdminEntry("Ticket recibido", "Mensaje y seguimiento visible"),
    AdminEntry("Vista previa", "Revisión del flujo completo"),
)

private val publicStoreEntries = listOf(
    AdminEntry("Portada Tienda", "Título, buscador y seguimiento"),
    AdminEntry("Categorías", "Nombre, ícono, orden y destino"),
    AdminEntry("Subcategorías", "Padre, estado y destino"),
    AdminEntry("Locales visibles", "Orden, destacados y estado visible"),
    AdminEntry("Buscador Tienda", "Sugerencias y mensaje sin resultados"),
    AdminEntry("Seguimiento desde Tienda", "Consulta visible desde Tienda"),
    AdminEntry("Vista previa", "Revisión de portada y listados"),
    AdminEntry("Orden / visibilidad", "Exposición pública preparada"),
)

private val publicTrackingEntries = listOf(
    AdminEntry("Consulta de pedido", "Texto, número y mensajes"),
    AdminEntry("Motivos de reclamo", "Motivos visibles y avisos"),
    AdminEntry("Vista previa", "Consulta preparada"),
    AdminEntry("Auditoría visual", "Registro preparado"),
)

private fun publicWorldEntriesFor(world: String): List<AdminEntry> =
    when (world) {
        "Home público" -> publicHomeEntries
        "Compra / Retiro" -> publicPurchaseEntries
        "Tienda" -> publicStoreEntries
        else -> publicTrackingEntries
    }

private fun publicWorldPartEntries(world: String, part: String): List<AdminEntry> =
    when {
        world == "Home público" -> publicHomePartEntries(part)
        world == "Compra / Retiro" -> publicPurchasePartEntries(part)
        world == "Tienda" -> publicStorePartEntries(part)
        else -> publicTrackingPartEntries(part)
    }

private fun publicHomePartEntries(part: String): List<AdminEntry> =
    when (part) {
        "Encabezado" -> listOf(
            AdminEntry("Título", "Valor actual: Pédilo"),
            AdminEntry("Subtítulo", "Todos tus pedidos en un solo lugar"),
            AdminEntry("Imagen / marca", "Imagen visible y alternativa"),
            AdminEntry("Vista previa", "Revisar encabezado"),
        )
        "Accesos rápidos" -> listOf("Mascotas", "Farmacia", "Bebidas", "Mercado").map {
            AdminEntry(it, "Nombre, ícono, orden, estado y destino")
        }
        "Banner destacado" -> listOf(
            AdminEntry("Texto principal", "Título visible del banner"),
            AdminEntry("Texto secundario", "Información complementaria"),
            AdminEntry("Imagen", "Imagen visible y alternativa"),
            AdminEntry("Botón ver más", "Texto y destino del botón"),
            AdminEntry("Activo / inactivo", "Visibilidad del banner"),
            AdminEntry("Vista previa", "Revisar banner completo"),
        )
        "Ver más / Convenciones" -> listOf(
            AdminEntry("Día activo", "Título, texto, imagen y estado"),
            AdminEntry("Información del día", "Título, texto, imagen y estado"),
            AdminEntry("Reclamos", "Contenido visible y orden"),
            AdminEntry("Seguimiento", "Contenido visible y orden"),
        )
        "Ofertas" -> listOf(
            AdminEntry("Título de sección", "Texto visible"),
            AdminEntry("Cards visibles", "Oferta publicable"),
            AdminEntry("Orden", "Posición en Home"),
            AdminEntry("Activo / inactivo", "Visibilidad de la sección"),
            AdminEntry("Destino", "Ruta pública preparada"),
            AdminEntry("Vista previa", "Revisar ofertas"),
        )
        "Nuevos locales" -> listOf(
            AdminEntry("Título de sección", "Texto visible"),
            AdminEntry("Cards visibles", "Locales publicables"),
            AdminEntry("Orden", "Posición en Home"),
            AdminEntry("Activo / inactivo", "Visibilidad de la sección"),
            AdminEntry("Destino", "Ruta pública preparada"),
            AdminEntry("Vista previa", "Revisar nuevos locales"),
        )
        "Buscador / tags" -> listOf(
            AdminEntry("Tags visibles", "Nombre y criterio"),
            AdminEntry("Orden", "Posición de sugerencias"),
            AdminEntry("Activo / inactivo", "Visibilidad de tags"),
            AdminEntry("Destino / criterio", "Categoría existente"),
            AdminEntry("Vista previa", "Revisar buscador y tags"),
        )
        else -> listOf(
            AdminEntry("Vista completa", "Encabezado, accesos y banner"),
            AdminEntry("Contenido ampliado", "Convenciones, ofertas y locales"),
            AdminEntry("Búsqueda y tags", "Sugerencias visibles"),
        )
    }

private fun publicPurchasePartEntries(part: String): List<AdminEntry> =
    when (part) {
        "Pantalla inicial" -> listOf(
            AdminEntry("Título", "Valor visible principal"),
            AdminEntry("Subtítulo", "Guía inicial"),
            AdminEntry("Opción Compra", "Texto, ícono y orden"),
            AdminEntry("Opción Retiro / Envío", "Texto, ícono y orden"),
            AdminEntry("Avisos visibles", "Mensajes activos"),
        )
        "Compra" -> listOf(
            AdminEntry("Título del flujo", "Texto visible"),
            AdminEntry("Texto guía", "Ayuda para completar"),
            AdminEntry("Campo producto", "Texto del campo"),
            AdminEntry("Campo cantidad / detalle", "Texto del campo"),
            AdminEntry("Agregar otro producto", "Texto del botón"),
            AdminEntry("Continuar", "Texto del botón"),
            AdminEntry("Imagen / ícono", "Selector visual preparado"),
            AdminEntry("Avisos", "Mensajes visibles"),
        )
        "Retiro / Envío" -> listOf(
            AdminEntry("Título del flujo", "Texto visible"),
            AdminEntry("Texto guía", "Ayuda para completar"),
            AdminEntry("Dirección de retiro", "Texto del campo"),
            AdminEntry("Horario", "Texto del campo"),
            AdminEntry("Nombre del paquete", "Texto del campo"),
            AdminEntry("Pago en retiro", "Texto visible"),
            AdminEntry("Monto a pagar", "Texto visible"),
            AdminEntry("Imagen / ícono", "Selector visual preparado"),
        )
        "Confirmación" -> listOf(
            AdminEntry("Título", "Texto visible"),
            AdminEntry("Textos de revisión", "Labels visibles"),
            AdminEntry("Confirmar", "Texto del botón"),
            AdminEntry("Corregir", "Texto de regreso"),
            AdminEntry("Avisos visibles", "Mensajes activos"),
        )
        "Ticket recibido" -> listOf(
            AdminEntry("Título", "Texto visible"),
            AdminEntry("Pedido recibido", "Mensaje principal"),
            AdminEntry("Número de seguimiento", "Texto visible"),
            AdminEntry("Seguir comprando", "Texto del botón"),
            AdminEntry("Ver seguimiento", "Texto del botón"),
            AdminEntry("Imagen / ícono", "Selector visual preparado"),
        )
        else -> listOf(
            AdminEntry("Pantalla inicial", "Compra y Retiro / Envío"),
            AdminEntry("Flujo Compra", "Campos y avisos visibles"),
            AdminEntry("Flujo Retiro / Envío", "Campos y avisos visibles"),
            AdminEntry("Confirmación", "Revisión final"),
            AdminEntry("Ticket recibido", "Cierre visible"),
        )
    }

private fun publicStorePartEntries(part: String): List<AdminEntry> =
    when (part) {
        "Portada Tienda" -> listOf(
            AdminEntry("Título", "Texto visible"),
            AdminEntry("Subtítulo", "Texto de apoyo"),
            AdminEntry("Texto de buscador", "Campo visible"),
            AdminEntry("Imagen / ícono", "Selector visual preparado"),
            AdminEntry("Bloque seguimiento", "Consulta visible"),
            AdminEntry("Orden visual", "Posición preparada"),
        )
        "Categorías" -> listOf(
            AdminEntry("Nombre visible", "Texto de card"),
            AdminEntry("Ícono / imagen", "Selector visual preparado"),
            AdminEntry("Orden", "Posición pública"),
            AdminEntry("Activo / inactivo", "Estado visible"),
            AdminEntry("Destino", "Ruta pública preparada"),
        )
        "Subcategorías" -> listOf(
            AdminEntry("Nombre visible", "Texto de card"),
            AdminEntry("Ícono / imagen", "Selector visual preparado"),
            AdminEntry("Categoría padre", "Relación requerida"),
            AdminEntry("Orden", "Posición pública"),
            AdminEntry("Activo / inactivo", "Estado visible"),
            AdminEntry("Destino", "Ruta pública preparada"),
        )
        "Locales visibles" -> listOf(
            AdminEntry("Orden de exposición", "Posición pública"),
            AdminEntry("Destacados", "Locales publicables"),
            AdminEntry("Nuevos", "Locales publicables"),
            AdminEntry("Estado visible", "Activo u oculto"),
            AdminEntry("Imagen", "Imagen de card"),
            AdminEntry("Texto de card", "Nombre y descripción"),
        )
        "Buscador Tienda" -> listOf(
            AdminEntry("Texto del buscador", "Campo visible"),
            AdminEntry("Tags / sugerencias", "Sugerencias visibles"),
            AdminEntry("Sin resultados", "Mensaje visible"),
            AdminEntry("Orden de sugerencias", "Prioridad pública"),
            AdminEntry("Activo / inactivo", "Estado visible"),
        )
        "Seguimiento desde Tienda" -> listOf(
            AdminEntry("Título", "Texto visible"),
            AdminEntry("Texto guía", "Ayuda de consulta"),
            AdminEntry("Número de pedido", "Texto del campo"),
            AdminEntry("Consultar", "Texto del botón"),
            AdminEntry("Sin resultado", "Mensaje visible"),
        )
        else -> listOf(
            AdminEntry("Portada", "Título, buscador y seguimiento"),
            AdminEntry("Categorías", "Orden y destino"),
            AdminEntry("Subcategorías", "Padre y destino"),
            AdminEntry("Locales visibles", "Exposición pública"),
            AdminEntry("Buscador", "Sugerencias y mensajes"),
        )
    }

private fun publicTrackingPartEntries(part: String): List<AdminEntry> =
    when (part) {
        "Consulta de pedido" -> listOf(
            AdminEntry("Texto para consultar", "Título visible"),
            AdminEntry("Número de pedido", "Texto del campo"),
            AdminEntry("Sin resultado", "Mensaje visible"),
            AdminEntry("Pedido cerrado", "Mensaje visible"),
        )
        "Motivos de reclamo" -> listOf(
            AdminEntry("Texto de reclamo", "Título visible"),
            AdminEntry("Motivos visibles", "Lista preparada"),
            AdminEntry("Avisos", "Mensajes activos"),
            AdminEntry("Imagen / ícono", "Selector visual preparado"),
        )
        else -> listOf(
            AdminEntry("Consulta preparada", "Texto, número y mensajes"),
            AdminEntry("Ayuda visible", "Motivos y avisos"),
            AdminEntry("Registro preparado", "Auditoría visual"),
        )
    }

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
            AdminEntry("Auditoría", "Historial visual consultivo"),
        ),
    ),
    AdminRoleAccessSection(
        title = "Administradores",
        summary = "Cuentas con alcance administrativo.",
        contextTitle = "Acceso administrativo",
        contextText = "Revisión de cuentas Admin sin modificar permisos reales.",
        entries = listOf(
            AdminEntry("Cuentas Admin", "Listado conceptual de acceso"),
            AdminEntry("Crear Admin", "Alta visual sensible"),
            AdminEntry("Estado de acceso", "Control de vigencia administrativa"),
            AdminEntry("Nivel de sensibilidad", "Impacto del acceso"),
            AdminEntry("Permisos visibles", "Alcance de acciones permitido"),
            AdminEntry("Auditoría", "Historial visual consultivo"),
        ),
    ),
    AdminRoleAccessSection(
        title = "Locales store",
        summary = "Cuentas con rol Local.",
        contextTitle = "Cuentas store",
        contextText = "Organiza relación de cuenta y local sin editar la entidad comercial.",
        entries = listOf(
            AdminEntry("Cuentas Local", "Estado de cuentas store"),
            AdminEntry("Crear cuenta Local", "Alta visual de acceso"),
            AdminEntry("Local vinculado", "Relación con local asignado"),
            AdminEntry("Vinculación pendiente", "Cuenta sin relación completa"),
            AdminEntry("Estado de acceso", "Lectura de habilitación de ingreso"),
            AdminEntry("Auditoría", "Historial visual consultivo"),
        ),
    ),
    AdminRoleAccessSection(
        title = "Repartidores driver",
        summary = "Cuentas con rol Repartidor.",
        contextTitle = "Cuentas driver",
        contextText = "Organiza relación de cuenta y repartidor sin operar entregas.",
        entries = listOf(
            AdminEntry("Cuentas Repartidor", "Estado de cuentas driver"),
            AdminEntry("Crear cuenta Repartidor", "Alta visual de acceso"),
            AdminEntry("Repartidor vinculado", "Relación con entidad de reparto"),
            AdminEntry("Vinculación pendiente", "Cuenta con vínculo incompleto"),
            AdminEntry("Estado de acceso", "Lectura de habilitación de ingreso"),
            AdminEntry("Auditoría", "Historial visual consultivo"),
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
            AdminEntry("Resultado", "Cierre visual preparado"),
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
            AdminEntry("Posible reactivación", "Ruta de revisión posterior"),
            AdminEntry("Auditoría", "Historial visual consultivo"),
        ),
    ),
    AdminRoleAccessSection(
        title = "Vinculaciones pendientes",
        summary = "Cuentas con rol asignado y vínculo incompleto.",
        contextTitle = "Relaciones pendientes",
        contextText = "Ordena relaciones faltantes sin crear entidades ni aplicar vínculos reales.",
        entries = listOf(
            AdminEntry("Store sin local", "Relación comercial incompleta"),
            AdminEntry("Driver sin repartidor", "Relación operativa incompleta"),
            AdminEntry("Relación incompleta", "Pendiente de asociación final"),
            AdminEntry("Entidad pendiente", "Entidad destino por definir"),
            AdminEntry("Revisión de vínculo", "Control de consistencia de asociación"),
            AdminEntry("Resultado", "Cierre visual preparado"),
        ),
    ),
    AdminRoleAccessSection(
        title = "Auditoría de accesos",
        summary = "Consulta de roles, accesos y vínculos.",
        contextTitle = "Trazabilidad de accesos",
        contextText = "Consulta cambios de cuenta y rol sin editar ni borrar registros.",
        entries = listOf(
            AdminEntry("Cambios de rol", "Antes, después y motivo"),
            AdminEntry("Activaciones", "Accesos habilitados"),
            AdminEntry("Bloqueos", "Accesos detenidos"),
            AdminEntry("Desbloqueos", "Accesos restaurados"),
            AdminEntry("Vinculaciones", "Relaciones cuenta y entidad"),
            AdminEntry("Detalle de registro", "Quién, cuándo y resultado"),
        ),
    ),
)

private val roleAccessRootEntries = roleAccessSections.map {
    AdminEntry(it.title, "Abrir mundo")
}

@Composable
fun AdminApp(onSignOutConfirmed: () -> Unit) {
    var route by remember { mutableStateOf<AdminRoute>(AdminRoute.Operation) }
    var showSignOut by remember { mutableStateOf(false) }
    var readOnlyOrders by remember { mutableStateOf<List<AdminOrderSummary>>(emptyList()) }
    var readOnlyOrderDetails by remember { mutableStateOf<Map<String, AdminOrderDetail>>(emptyMap()) }
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
            is AdminRoute.ConfigurationPublicWorldEditor -> when (current.step) {
                AdminPublicHomeEditorStep.Detail -> AdminRoute.ConfigurationPublicWorldPart(current.world, current.part)
                AdminPublicHomeEditorStep.Edit -> current.copy(step = AdminPublicHomeEditorStep.Detail)
                AdminPublicHomeEditorStep.Preview -> current.copy(step = AdminPublicHomeEditorStep.Edit)
                AdminPublicHomeEditorStep.Impact -> current.copy(step = AdminPublicHomeEditorStep.Preview)
                AdminPublicHomeEditorStep.Confirmation -> current.copy(step = AdminPublicHomeEditorStep.Impact)
                AdminPublicHomeEditorStep.Result -> current.copy(step = AdminPublicHomeEditorStep.Confirmation)
                AdminPublicHomeEditorStep.Audit -> current.copy(step = AdminPublicHomeEditorStep.Result)
            }
            is AdminRoute.ConfigurationPublicWorldPart -> AdminRoute.ConfigurationPublicWorld(current.world)
            is AdminRoute.ConfigurationPublicWorld -> AdminRoute.ConfigurationSection(
                configurationSections.first { it.title == "Público" },
            )
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
                AdminConfigurationConvergenceStep.Audit -> AdminRoute.ConfigurationSubsection(
                    section = configurationSections.first { it.title == current.section },
                    title = current.subsection,
                )
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
                AdminRoleAccessConvergenceStep.Audit -> current.copy(step = AdminRoleAccessConvergenceStep.Result)
            }
            is AdminRoute.RoleAccessSection -> AdminRoute.RoleAccess
            is AdminRoute.ConfigurationSubsection -> AdminRoute.ConfigurationSection(current.section)
            is AdminRoute.ConfigurationSection -> AdminRoute.Configuration
            is AdminRoute.OperationOrderSection -> current.detailRoute
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
            AdminRoute.Configuration -> AdminConfigurationHomeScreen(
                entries = configurationEntries,
                onEntry = { entry ->
                    route = AdminRoute.ConfigurationSection(configurationSections.first { it.title == entry.title })
                },
            )
            AdminRoute.RoleAccess -> AdminRootScreen(
                title = "Alta de roles",
                eyebrow = "Usuarios y accesos",
                summary = "Cuentas, roles y vinculaciones.",
                entries = roleAccessRootEntries,
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
                onSection = { section ->
                    route = AdminRoute.OperationOrderSection(current, section)
                },
            )
            is AdminRoute.OperationOrderSection -> AdminOrderSectionScreen(
                section = current.section,
                variant = current.detailRoute.variant,
                orderId = current.detailRoute.realOrderId,
                summary = current.detailRoute.realOrderId?.let { id -> readOnlyOrders.firstOrNull { it.id == id } },
                detail = current.detailRoute.realOrderId?.let { readOnlyOrderDetails[it] },
            )
            is AdminRoute.ConfigurationSection -> AdminConfigurationSectionScreen(
                section = current.section,
                entries = if (current.section.title == "Público") publicWorldEntries else current.section.entries,
                useGrid = current.section.title == "Público",
                onEntry = {
                    route = if (current.section.title == "Público") {
                        AdminRoute.ConfigurationPublicWorld(it.title)
                    } else {
                        AdminRoute.ConfigurationSubsection(current.section, it.title)
                    }
                },
            )
            is AdminRoute.ConfigurationPublicWorld -> AdminPublicWorldScreen(
                world = current.world,
                onPart = { route = AdminRoute.ConfigurationPublicWorldPart(current.world, it.title) },
            )
            is AdminRoute.ConfigurationPublicWorldPart -> AdminPublicWorldPartScreen(
                world = current.world,
                part = current.part,
                onItem = {
                    route = AdminRoute.ConfigurationPublicWorldEditor(
                        world = current.world,
                        part = current.part,
                        item = it.title,
                        step = AdminPublicHomeEditorStep.Detail,
                    )
                },
            )
            is AdminRoute.ConfigurationPublicWorldEditor -> AdminPublicHomeEditorScreen(
                world = current.world,
                part = current.part,
                item = current.item,
                step = current.step,
                onNext = { route = current.copy(step = it) },
            )
            is AdminRoute.ConfigurationSubsection -> AdminSectionScreen(
                root = AdminRoot.Configuration,
                title = current.title,
                summary = current.section.entries.firstOrNull { it.title == current.title }?.note
                    ?: publicWorldEntries.firstOrNull { it.title == current.title }?.note
                    ?: "Revisión visual preparada.",
                panelTitle = current.section.title,
                panelText = "Revisá el detalle, prepará cambios y evaluá su impacto antes de confirmar.",
                onConfigurationConvergence = {
                    route = AdminRoute.ConfigurationConvergence(
                        section = current.section.title,
                        subsection = current.title,
                        step = if (current.section.title == "Auditoría") {
                            AdminConfigurationConvergenceStep.Audit
                        } else {
                            AdminConfigurationConvergenceStep.Entity
                        },
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
private fun AdminConfigurationHomeScreen(
    entries: List<AdminEntry>,
    onEntry: (AdminEntry) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = adminBottomBarReservedPadding),
        contentPadding = PaddingValues(top = 18.dp, bottom = adminContentBottomPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            AdminHeader(
                title = "Configuración",
                eyebrow = "Datos y reglas",
                summary = "Prepará cómo funciona Pédilo.",
                onSignOut = {},
                showSignOut = false,
            )
        }
        gridItems(entries, key = { it.title }) { entry ->
            AdminConfigurationRootCard(
                entry = entry,
                icon = configurationIconFor(entry.title),
                onClick = { onEntry(entry) },
            )
        }
    }
}

@Composable
private fun AdminConfigurationGridScreen(
    title: String,
    eyebrow: String,
    summary: String,
    entries: List<AdminEntry>,
    onEntry: (AdminEntry) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = adminBottomBarReservedPadding),
        contentPadding = PaddingValues(top = 18.dp, bottom = adminContentBottomPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            AdminHeader(title = title, eyebrow = eyebrow, summary = summary, onSignOut = {}, showSignOut = false)
        }
        gridItems(entries, key = { it.title }) { entry ->
            AdminConfigurationRootCard(
                entry = entry,
                icon = publicHomeIconFor(entry.title),
                onClick = { onEntry(entry) },
            )
        }
    }
}

@Composable
private fun AdminPublicWorldScreen(
    world: String,
    onPart: (AdminEntry) -> Unit,
) {
    AdminConfigurationGridScreen(
        title = world,
        eyebrow = "Público",
        summary = publicWorldSummary(world),
        entries = publicWorldEntriesFor(world),
        onEntry = onPart,
    )
}

@Composable
private fun AdminPublicWorldPartScreen(
    world: String,
    part: String,
    onItem: (AdminEntry) -> Unit,
) {
    AdminConfigurationGridScreen(
        title = part,
        eyebrow = world,
        summary = publicWorldPartSummary(world, part),
        entries = publicWorldPartEntries(world, part),
        onEntry = onItem,
    )
}

private fun publicWorldSummary(world: String): String =
    when (world) {
        "Home público" -> "Elegí qué parte visible del Home querés gestionar."
        "Compra / Retiro" -> "Prepará el flujo público central sin crear pedidos reales."
        "Tienda" -> "Organizá exposición pública sin crear locales ni productos."
        else -> "Mundo preparado para consulta y reclamos visibles."
    }

private fun publicWorldPartSummary(world: String, part: String): String =
    when (world) {
        "Home público" -> publicHomePartSummary(part)
        "Compra / Retiro" -> publicPurchasePartSummary(part)
        "Tienda" -> publicStorePartSummary(part)
        else -> "Prepará textos, avisos, vista previa y registro visual."
    }

private fun publicHomePartSummary(part: String): String =
    when (part) {
        "Encabezado" -> "Título, subtítulo, marca y vista previa."
        "Accesos rápidos" -> "Elegí un acceso para revisar nombre, ícono, orden y destino."
        "Banner destacado" -> "Gestioná el contenido y el botón visible del banner."
        "Ver más / Convenciones" -> "Mantené alineada la información resumida y ampliada."
        "Ofertas" -> "Prepará oferta publicable sin crear productos ni editar precios."
        "Nuevos locales" -> "Mostrá únicamente locales completos y publicables."
        "Buscador / tags" -> "Prepará sugerencias con destino o criterio válido."
        else -> "Revisá cómo quedaría el Home antes de confirmar."
    }

private fun publicPurchasePartSummary(part: String): String =
    when (part) {
        "Pantalla inicial" -> "Opciones Compra y Retiro / Envío con avisos visibles."
        "Compra" -> "Textos, campos e imágenes del flujo Compra."
        "Retiro / Envío" -> "Textos, dirección, horario y pago visible."
        "Confirmación" -> "Labels y botones visibles antes del ticket."
        "Ticket recibido" -> "Mensaje final y seguimiento visible."
        else -> "Vista previa del flujo completo."
    }

private fun publicStorePartSummary(part: String): String =
    when (part) {
        "Portada Tienda" -> "Título, buscador, seguimiento y orden visual."
        "Categorías" -> "Navegación pública por categoría sin crear oferta."
        "Subcategorías" -> "Navegación pública con padre y destino válido."
        "Locales visibles" -> "Exposición de locales publicables."
        "Buscador Tienda" -> "Textos, sugerencias y mensajes sin resultados."
        "Seguimiento desde Tienda" -> "Consulta visible que converge al seguimiento común."
        else -> "Vista previa y orden de exposición pública."
    }

private fun publicHomeIconFor(title: String): ImageVector =
    when (title) {
        "Home público", "Encabezado", "Título", "Subtítulo" -> Icons.Outlined.Language
        "Compra / Retiro", "Pantalla inicial", "Accesos rápidos", "Mascotas", "Farmacia", "Bebidas", "Mercado" -> Icons.Outlined.Dashboard
        "Tienda", "Ofertas", "Cards visibles", "Portada Tienda", "Categorías", "Subcategorías" -> Icons.Outlined.ShoppingCart
        "Retiro / Envío", "Ticket recibido" -> Icons.AutoMirrored.Outlined.ReceiptLong
        "Locales visibles" -> Icons.Outlined.Storefront
        "Seguimiento / Reclamos", "Reclamos", "Seguimiento" -> Icons.Outlined.Feedback
        "Banner destacado", "Texto principal", "Texto secundario", "Botón ver más" -> Icons.Outlined.MoreHoriz
        "Ver más / Convenciones", "Día activo", "Información del día" -> Icons.Outlined.CalendarToday
        "Nuevos locales" -> Icons.Outlined.Storefront
        "Buscador / tags", "Tags visibles", "Destino / criterio" -> Icons.Outlined.Search
        "Vista previa del Home", "Vista previa", "Vista completa", "Contenido ampliado", "Búsqueda y tags" -> Icons.Outlined.CheckCircle
        "Imagen / marca", "Imagen" -> Icons.Outlined.Restaurant
        "Orden" -> Icons.Outlined.Tune
        "Activo / inactivo" -> Icons.Outlined.Bolt
        "Destino" -> Icons.Outlined.ChevronRight
        else -> Icons.Outlined.Tune
    }

@Composable
private fun AdminPublicHomeEditorScreen(
    world: String,
    part: String,
    item: String,
    step: AdminPublicHomeEditorStep,
    onNext: (AdminPublicHomeEditorStep) -> Unit,
) {
    val title = when (step) {
        AdminPublicHomeEditorStep.Detail -> item
        AdminPublicHomeEditorStep.Edit -> "Editar $item"
        AdminPublicHomeEditorStep.Preview -> "Vista previa"
        AdminPublicHomeEditorStep.Impact -> "Impacto"
        AdminPublicHomeEditorStep.Confirmation -> "Confirmar cambio"
        AdminPublicHomeEditorStep.Result -> "Resultado"
        AdminPublicHomeEditorStep.Audit -> "Auditoría visual"
    }
    val action = when (step) {
        AdminPublicHomeEditorStep.Detail -> "Editar contenido" to AdminPublicHomeEditorStep.Edit
        AdminPublicHomeEditorStep.Edit -> "Ver vista previa" to AdminPublicHomeEditorStep.Preview
        AdminPublicHomeEditorStep.Preview -> "Ver impacto" to AdminPublicHomeEditorStep.Impact
        AdminPublicHomeEditorStep.Impact -> "Continuar a confirmación" to AdminPublicHomeEditorStep.Confirmation
        AdminPublicHomeEditorStep.Confirmation -> "Confirmar visualmente" to AdminPublicHomeEditorStep.Result
        AdminPublicHomeEditorStep.Result -> "Consultar auditoría visual" to AdminPublicHomeEditorStep.Audit
        AdminPublicHomeEditorStep.Audit -> "Revisar registro" to AdminPublicHomeEditorStep.Audit
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
                eyebrow = "$part · $world",
                summary = publicHomeEditorSummary(world, step),
                onSignOut = {},
                showSignOut = false,
            )
        }
        item { AdminInfoPanel(title = "Valor actual", text = publicHomeCurrentValue(world, part, item)) }
        if (step == AdminPublicHomeEditorStep.Edit) {
            item { AdminInfoPanel(title = "Nuevo valor", text = publicHomeEditableFields(world, part, item)) }
            item {
                AdminActionCard(
                    title = "Guardar borrador visual",
                    note = "Conserva la revisión dentro de esta pantalla, sin guardar datos reales.",
                    onClick = {},
                )
            }
        }
        if (step == AdminPublicHomeEditorStep.Preview) {
            item { AdminInfoPanel(title = "Vista previa", text = publicHomePreview(world, part, item)) }
        }
        if (step == AdminPublicHomeEditorStep.Impact || step == AdminPublicHomeEditorStep.Confirmation) {
            item { AdminInfoPanel(title = "Impacto visible", text = publicHomeImpact(world, part, item)) }
        }
        if (step == AdminPublicHomeEditorStep.Result) {
            item { AdminInfoPanel(title = "Cambio preparado", text = "La revisión visual quedó lista. El usuario público real permanece sin cambios.") }
        }
        if (step == AdminPublicHomeEditorStep.Audit) {
            item {
                AdminInfoPanel(
                    title = "Registro preparado",
                    text = "Qué cambió · Responsable · Momento · Valor anterior · Valor nuevo · Dónde impacta · Resultado.",
                )
            }
        }
        item {
            AdminActionCard(
                title = action.first,
                note = "Continuar dentro de la herramienta visual.",
                onClick = { onNext(action.second) },
            )
        }
    }
}

private fun publicHomeEditorSummary(world: String, step: AdminPublicHomeEditorStep): String =
    when (step) {
        AdminPublicHomeEditorStep.Detail -> "Revisá el dato visible antes de editar."
        AdminPublicHomeEditorStep.Edit -> "Prepará un nuevo valor sin modificar $world real."
        AdminPublicHomeEditorStep.Preview -> "Compará cómo se vería el cambio."
        AdminPublicHomeEditorStep.Impact -> "Revisá qué parte visible cambia y qué permanece igual."
        AdminPublicHomeEditorStep.Confirmation -> "Confirmación visual previa al resultado."
        AdminPublicHomeEditorStep.Result -> "Cierre visual sin publicación real."
        AdminPublicHomeEditorStep.Audit -> "Consulta del registro visual preparado."
    }

private fun publicHomeCurrentValue(world: String, part: String, item: String): String =
    when {
        world == "Compra / Retiro" -> "$item visible en $part · Activo · Orden preparado"
        world == "Tienda" -> "$item visible en $part · Activo · Destino preparado"
        world == "Seguimiento / Reclamos" -> "$item preparado para consulta pública"
        part == "Encabezado" && item == "Título" -> "Pédilo"
        part == "Encabezado" && item == "Subtítulo" -> "Todos tus pedidos en un solo lugar"
        part == "Accesos rápidos" -> "$item · Activo · Orden visible · Destino configurado"
        part == "Banner destacado" -> "¡Envíos más rápidos! · Botón ver más"
        item.contains("Activo") -> "Activo"
        else -> "$item visible en $part"
    }

private fun publicHomeEditableFields(world: String, part: String, item: String): String =
    when {
        world == "Compra / Retiro" -> "Valor actual · Nuevo valor · Imagen / ícono · Estado activo / inactivo · Orden · Avisos · Vista previa"
        world == "Tienda" -> "Valor actual · Nuevo valor · Selector visual · Estado activo / inactivo · Orden · Destino · Vista previa"
        world == "Seguimiento / Reclamos" -> "Texto visible · Mensaje sin resultado · Motivos · Avisos · Imagen / ícono · Vista previa"
        part == "Accesos rápidos" -> "Nombre visible · Imagen / ícono · Orden · Activo / inactivo · Destino"
        part == "Banner destacado" -> "Texto principal · Texto secundario · Imagen · Botón visible · Texto del botón · Destino · Activo / inactivo"
        part == "Ver más / Convenciones" -> "Título · Texto · Imagen · Orden · Activo / inactivo"
        part == "Ofertas" || part == "Nuevos locales" -> "Título · Cards visibles · Orden · Activo / inactivo · Destino"
        part == "Buscador / tags" -> "Nombre · Orden · Activo / inactivo · Destino / criterio"
        part == "Vista previa del Home" -> "Encabezado · Accesos rápidos · Banner · Convenciones · Ofertas · Nuevos locales · Tags"
        else -> "Nuevo valor · Imagen / marca · Estado visible"
    }

private fun publicHomePreview(world: String, part: String, item: String): String =
    when (world) {
        "Compra / Retiro" -> "$item se revisa dentro de $part con Compra, Retiro / Envío, confirmación y ticket preparado."
        "Tienda" -> "$item se revisa dentro de $part con portada, categorías, locales visibles, buscador y seguimiento preparado."
        "Seguimiento / Reclamos" -> "$item queda preparado como card de consulta, sin desarrollo profundo todavía."
        else -> "$item se muestra dentro de $part junto al resto del Home, con tamaño, orden y destino preparados."
    }

private fun publicHomeImpact(world: String, part: String, item: String): String =
    when {
        world == "Compra / Retiro" -> "Cambia la presentación del flujo público. No crea pedidos reales ni cambia pagos."
        world == "Tienda" && part == "Locales visibles" -> "Cambia exposición de locales publicables. No crea locales ni edita productos."
        world == "Tienda" -> "Cambia navegación o presentación pública. No crea oferta ni edita precios."
        world == "Seguimiento / Reclamos" -> "Prepara consulta y ayuda visible. No redefine seguimiento real."
        part == "Ofertas" -> "Cambia la presentación de ofertas publicables. No crea productos ni modifica precios."
        part == "Nuevos locales" -> "Cambia la presentación de locales publicables. No crea ni habilita locales."
        part == "Banner destacado" -> "Cambia el banner visible. Solo el botón ver más conserva navegación."
        part == "Accesos rápidos" -> "Cambia accesos visibles, orden y destino preparado."
        part == "Buscador / tags" -> "Cambia sugerencias visibles y criterio preparado."
        else -> "Cambia $item dentro de $part. No modifica datos reales ni pedidos vivos."
    }

@Composable
private fun AdminConfigurationRootCard(
    entry: AdminEntry,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(if (pressed) 0.98f else 1f)
            .pediloCardDepth(RoundedCornerShape(16.dp))
            .background(PediloCardBrush, RoundedCornerShape(16.dp))
            .border(1.dp, if (pressed) PediloOrange else PediloLine, RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = entry.title,
            tint = PediloOrange,
            modifier = Modifier.size(34.dp),
        )
        Text(
            text = entry.title,
            color = PediloText,
            fontSize = 16.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun configurationIconFor(title: String): ImageVector =
    when (title) {
        "Público" -> Icons.Outlined.Language
        "Locales" -> Icons.Outlined.Storefront
        "Reparto" -> Icons.Outlined.TwoWheeler
        "Marketplace" -> Icons.Outlined.ShoppingCart
        "Pedidos" -> Icons.AutoMirrored.Outlined.ReceiptLong
        "Precios" -> Icons.Outlined.Payments
        "Cobros" -> Icons.Outlined.CreditCard
        "Mensajes" -> Icons.Outlined.Feedback
        "Reglas" -> Icons.Outlined.TaskAlt
        "Notificaciones" -> Icons.Outlined.Notifications
        "Métricas" -> Icons.Outlined.BarChart
        "Auditoría" -> Icons.Outlined.History
        "Emergencias" -> Icons.Outlined.ReportProblem
        else -> Icons.Outlined.Tune
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
            AdminOperationMotherCard(
                title = universe.title,
                summary = operationUniverseSummary(universe, orders),
                prominentValue = if (universe.key == AdminOperationUniverseKey.Orders) orders.size.toString() else null,
                subcards = universe.views.map { view ->
                    AdminOperationSubcard(
                        icon = operationIconFor(view.title),
                        title = operationHomeViewTitle(view.title),
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
    val viewCount = operationViewOrderCount(view, orders)
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
                title = "${view.title} · $viewCount",
                eyebrow = universe.title,
                summary = operationViewStateLabel(view, orders),
                onSignOut = {},
                showSignOut = false,
            )
        }
        items(view.lists) { list ->
            AdminOperationSubcardView(
                AdminOperationSubcard(
                    icon = operationIconFor(list.title),
                    title = operationCompactTitle(list.title),
                    value = operationListCountLabel(list, orders),
                    detail = list.summary,
                    preview = orderDetailEntriesFor(list.kind, orders.forOperationList(list.kind))
                        .take(2)
                        .map { "${it.label} · ${it.note.substringBefore(" · ")}" },
                    tone = operationToneFor(list, orders),
                    onClick = { onList(list) },
                ),
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
                title = "${operationCompactTitle(list.title)} · ${orderEntries.size}",
                eyebrow = view.title,
                summary = if (orderEntries.isEmpty()) list.emptyText else list.summary,
                onSignOut = {},
                showSignOut = false,
            )
        }
        items(orderEntries) { entry ->
            AdminOperationOrderCard(
                card = AdminOperationalLiveCard(
                    icon = Icons.Outlined.ChevronRight,
                    title = entry.label,
                    countLabel = entry.note.substringBefore(" · "),
                    detail = entry.note.substringAfter(" · ", entry.note),
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
                summary = "Movimiento operativo",
                prominentValue = orders.size.toString(),
                subcards = orderViews.map { view ->
                    AdminOperationSubcard(
                        icon = operationIconFor(view.title),
                        title = operationHomeViewTitle(view.title),
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
                summary = "Sin datos",
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
                summary = "Sin datos",
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
    prominentValue: String? = null,
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = operationIconFor(title),
                    contentDescription = title,
                    tint = PediloOrange,
                    modifier = Modifier.size(24.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, color = PediloText, fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text(summary, color = PediloMuted, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            prominentValue?.let {
                Text(it, color = PediloOrange, fontSize = 28.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold)
            }
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
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (pressed) 0.988f else 1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (pressed) PediloPanelSoft else PediloPanel.copy(alpha = 0.88f), RoundedCornerShape(12.dp))
            .border(1.dp, toneColor.copy(alpha = if (pressed) 0.82f else 0.34f), RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = subcard.onClick)
            .defaultMinSize(minHeight = if (subcard.preview.isEmpty()) 58.dp else 92.dp)
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
            Icon(
                imageVector = subcard.icon,
                contentDescription = subcard.title,
                tint = toneColor,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(subcard.title, color = PediloText, fontSize = 14.sp, lineHeight = 17.sp, fontWeight = FontWeight.ExtraBold)
            if (subcard.detail.length <= 24) {
                AdminStatusChip(label = subcard.detail, toneColor = toneColor)
            } else {
                Text(
                    subcard.detail,
                    color = PediloMuted,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            subcard.preview.forEach { preview ->
                Text(
                    preview,
                    color = PediloMuted,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
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

private fun List<String>?.adminItemsSummary(): String =
    this?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }?.joinToString(separator = "\n") ?: "Sin dato"

private fun AdminOrderDetail?.adminPersonName(fallback: String = "Sin datos"): String =
    this?.component15().adminDisplayValue(fallback)

private fun adminHumanOperationStatus(
    publicStatus: String,
    operationalStatus: String,
    rawStatus: String,
    hasProblem: Boolean,
): String {
    val visible = "$publicStatus $operationalStatus"
    return when {
        visible.contains("local no responde", ignoreCase = true) ||
            visible.contains("sin respuesta", ignoreCase = true) -> "Local sin respuesta"
        visible.contains("demora", ignoreCase = true) ||
            visible.contains("retras", ignoreCase = true) -> "Demorado"
        visible.contains("preparando", ignoreCase = true) -> "Preparando"
        visible.contains("esperando repartidor", ignoreCase = true) -> "Buscando repartidor"
        visible.contains("en entrega", ignoreCase = true) -> "En camino"
        hasProblem -> "Con problema"
        publicStatus.isNotBlank() -> publicStatus
        else -> rawStatus.adminHumanStatusValue("Sin datos")
    }
}

private fun adminOrderProblemFocus(
    variant: OperationOrderVariant,
    publicStatus: String,
    operationalStatus: String,
    needsAttention: Boolean,
    activeIncident: Boolean,
): Pair<String, String>? {
    val statusText = "$publicStatus $operationalStatus"
    return when {
        statusText.contains("local no responde", ignoreCase = true) ||
            statusText.contains("sin respuesta", ignoreCase = true) -> "Local sin respuesta" to "Requiere revisión"
        statusText.contains("demora", ignoreCase = true) ||
            statusText.contains("retras", ignoreCase = true) -> "Demorado" to "Requiere revisión"
        activeIncident -> "Incidencia registrada" to "Requiere revisión"
        needsAttention || variant == OperationOrderVariant.WithProblem -> "Requiere revisión" to "Con problema"
        variant == OperationOrderVariant.NeedsAttention -> "Esperando respuesta" to "Requiere seguimiento"
        else -> null
    }
}

private fun operationIconFor(title: String): ImageVector =
    when (title) {
        "Hoy", "Ingresaron hoy" -> Icons.Outlined.CalendarToday
        "Pedidos" -> Icons.AutoMirrored.Outlined.ReceiptLong
        "Activos", "Activos de hoy" -> Icons.Outlined.Bolt
        "Problemas", "Problemas de hoy", "Con problemas", "Con incidencias", "Revisión operativa" -> Icons.Outlined.ReportProblem
        "Cerrados", "Cerrados de hoy", "Finalizados" -> Icons.Outlined.TaskAlt
        "Cancelados", "Pausados" -> Icons.Outlined.Cancel
        "Demorados", "Con demoras" -> Icons.Outlined.Schedule
        "Esperando local", "Local no responde", "Locales", "Local / Retiro", "Retiro" -> Icons.Outlined.Storefront
        "Preparando", "Compra" -> Icons.Outlined.Restaurant
        "Esperando repartidor", "Buscando repartidor", "En servicio", "Disponibles", "Repartidores" -> Icons.Outlined.TwoWheeler
        "En entrega", "En camino", "Entrega" -> Icons.Outlined.LocalShipping
        "Reclamo de cliente" -> Icons.Outlined.Feedback
        "Sin responsable" -> Icons.Outlined.PersonOff
        "Revisar pedido", "Revisar estado", "Revisión de hoy" -> Icons.Outlined.Search
        "Operando" -> Icons.Outlined.CheckCircle
        "Pago" -> Icons.Outlined.CreditCard
        "Historial" -> Icons.Outlined.History
        "Opciones" -> Icons.Outlined.MoreHoriz
        "Resumen" -> Icons.Outlined.Dashboard
        else -> Icons.Outlined.ChevronRight
    }

private fun operationCompactTitle(title: String): String =
    when (title) {
        "Con incidencias" -> "Incidencias"
        "Con demoras" -> "Demoras"
        "Reclamo de cliente" -> "Reclamos"
        "Local no responde" -> "Local sin respuesta"
        "Esperando repartidor", "Buscando repartidor" -> "Buscando repartidor"
        "En entrega" -> "En camino"
        else -> title
    }

private fun operationHomeViewTitle(title: String): String =
    title

private fun operationUniverseSummary(universe: AdminOperationUniverse, orders: List<AdminOrderSummary>): String {
    if (universe.key != AdminOperationUniverseKey.Orders) return "Sin datos"
    return if (orders.isEmpty()) "Sin pedidos" else "Movimiento operativo"
}

private fun operationViewOrderCount(view: AdminOperationView, orders: List<AdminOrderSummary>): Int =
    operationViewOrders(view, orders).size

private fun operationViewOrders(view: AdminOperationView, orders: List<AdminOrderSummary>): List<AdminOrderSummary> =
    when (view.title) {
        "Hoy" -> orders.forOperationList(AdminOperationListKind.TodayAll)
        "Activos" -> orders.forPrimaryPlacement(AdminOrderPrimaryPlacement.ACTIVE)
        "Problemas" -> orders.forPrimaryPlacement(AdminOrderPrimaryPlacement.PROBLEM)
        "Revisar pedido" -> orders.forPrimaryPlacement(AdminOrderPrimaryPlacement.UNCLASSIFIED)
        "Cerrados" -> orders.forPrimaryPlacements(
            AdminOrderPrimaryPlacement.FINISHED,
            AdminOrderPrimaryPlacement.CANCELLED,
        )
        else -> view.lists.flatMap { orders.forOperationList(it.kind) }.distinctBy { it.id }
    }

private fun operationViewStateLabel(view: AdminOperationView, orders: List<AdminOrderSummary>): String {
    val count = operationViewOrderCount(view, orders)
    return when {
        count == 0 && view.title == "Problemas" -> "Sin casos"
        count == 0 -> "Sin pedidos"
        view.title == "Problemas" -> "Prioridad"
        view.title == "Activos" -> "En curso"
        view.title == "Cerrados" -> "Completados"
        view.title == "Revisar pedido" -> "Requiere revisión"
        else -> "Ingresados hoy"
    }
}

private fun operationViewCountLabel(view: AdminOperationView, orders: List<AdminOrderSummary>): String {
    if (view.lists.none { it.kind.isOrderList() }) return "—"
    val count = operationViewOrderCount(view, orders)
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
        count == 0 -> if (list.emptyText == "Sin casos") "Sin casos" else "Sin pedidos"
        list.kind in setOf(
            AdminOperationListKind.TodayProblems,
            AdminOperationListKind.ProblemStoreNotResponding,
            AdminOperationListKind.ProblemUserClaim,
            AdminOperationListKind.ProblemDelayed,
            AdminOperationListKind.ProblemWithoutResponsible,
            AdminOperationListKind.ProblemOperationalReview,
        ) -> "Con casos"
        else -> "En curso"
    }
}

private fun operationToneFor(view: AdminOperationView, orders: List<AdminOrderSummary>): AdminOperationMetricTone {
    val count = operationViewOrderCount(view, orders)
    return when {
        count == 0 -> AdminOperationMetricTone.Neutral
        view.title == "Problemas" -> AdminOperationMetricTone.Danger
        view.title == "Activos" -> AdminOperationMetricTone.Healthy
        view.title == "Revisar pedido" -> AdminOperationMetricTone.Warning
        else -> AdminOperationMetricTone.Neutral
    }
}

private fun operationToneFor(list: AdminOperationList, orders: List<AdminOrderSummary>): AdminOperationMetricTone {
    val count = orders.forOperationList(list.kind).size
    return when {
        count == 0 -> AdminOperationMetricTone.Neutral
        list.kind in setOf(
            AdminOperationListKind.TodayProblems,
            AdminOperationListKind.ProblemStoreNotResponding,
            AdminOperationListKind.ProblemUserClaim,
            AdminOperationListKind.ProblemDelayed,
            AdminOperationListKind.ProblemWithoutResponsible,
            AdminOperationListKind.ProblemOperationalReview,
        ) -> AdminOperationMetricTone.Danger
        list.kind in setOf(
            AdminOperationListKind.ClosedFinished,
            AdminOperationListKind.StoreOperating,
            AdminOperationListKind.DriverAvailable,
        ) -> AdminOperationMetricTone.Healthy
        list.kind in setOf(
            AdminOperationListKind.TodayClosed,
            AdminOperationListKind.ClosedCancelled,
            AdminOperationListKind.ActiveWaitingStore,
            AdminOperationListKind.ActiveWaitingDriver,
            AdminOperationListKind.ActiveReviewState,
            AdminOperationListKind.TodayReview,
            AdminOperationListKind.Unclassified,
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
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (pressed) 0.988f else 1f)
            .clip(RoundedCornerShape(14.dp))
            .background(if (pressed) PediloPanelSoft else PediloPanel, RoundedCornerShape(14.dp))
            .border(1.dp, if (pressed) PediloOrange.copy(alpha = 0.72f) else PediloLine.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
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
            Icon(
                imageVector = card.icon,
                contentDescription = "Pedido",
                tint = PediloOrange,
                modifier = Modifier.size(20.dp),
            )
        }
        AdminStatusChip(label = card.countLabel, toneColor = PediloOrange)
        Text(card.detail, color = PediloMuted, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AdminConfigurationSectionScreen(
    section: AdminConfigurationSection,
    entries: List<AdminEntry> = section.entries,
    useGrid: Boolean = false,
    onEntry: (AdminEntry) -> Unit,
) {
    if (useGrid) {
        AdminConfigurationGridScreen(
            title = section.title,
            eyebrow = "Configuración",
            summary = "Mundos visibles para personas usuarias.",
            entries = entries,
            onEntry = onEntry,
        )
        return
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
                eyebrow = "Configuración",
                summary = section.summary,
                onSignOut = {},
                showSignOut = false,
            )
        }
        item {
            AdminInfoPanel(title = section.contextTitle, text = section.contextText)
        }
        items(entries) {
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
                    entry = AdminEntry("Abrir revisión", "Ver detalle y preparar el cambio"),
                    onClick = onConfigurationConvergence,
                )
            }
        }
        if (root == AdminRoot.RoleAccess) {
            val isAccessAudit = panelTitle == "Auditoría de accesos"
            val initialStep = when (title) {
                "Altas pendientes" -> AdminRoleAccessConvergenceStep.CreateAccount
                "Usuarios inactivos" -> AdminRoleAccessConvergenceStep.ToggleAccess
                "Vinculaciones pendientes" -> AdminRoleAccessConvergenceStep.LinkEntity
                else -> if (isAccessAudit) {
                    AdminRoleAccessConvergenceStep.Audit
                } else {
                    AdminRoleAccessConvergenceStep.Account
                }
            }
            item {
                AdminEntryCard(
                    entry = if (isAccessAudit) {
                        AdminEntry("Detalle de registro", "Consulta de accesos sin edición")
                    } else {
                        AdminEntry("Cuenta concreta", "Abrir detalle de cuenta y acciones disponibles")
                    },
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
        AdminRoleAccessConvergenceStep.Audit -> "Auditoría de accesos"
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
        AdminRoleAccessConvergenceStep.Audit -> "Consulta visual de cuenta, rol, vínculo y resultado."
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
        AdminRoleAccessConvergenceStep.Audit -> "Qué cambió, quién lo revisó, cuándo, valor anterior, valor nuevo, rol afectado, cuenta afectada, entidad vinculada, motivo y resultado."
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
        AdminRoleAccessConvergenceStep.Result -> AdminRoleAccessConvergenceStep.Audit
        AdminRoleAccessConvergenceStep.Audit -> AdminRoleAccessConvergenceStep.Audit
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
        AdminRoleAccessConvergenceStep.Result -> "Consultar auditoría de accesos"
        AdminRoleAccessConvergenceStep.Audit -> "Revisar registro"
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
        AdminConfigurationConvergenceStep.Entity -> "Detalle de configuración"
        AdminConfigurationConvergenceStep.Editor -> "Preparar cambio"
        AdminConfigurationConvergenceStep.Preview -> "Previsualización"
        AdminConfigurationConvergenceStep.Impact -> "Impacto"
        AdminConfigurationConvergenceStep.SensitiveConfirmation -> "Confirmación sensible"
        AdminConfigurationConvergenceStep.Result -> "Resultado"
        AdminConfigurationConvergenceStep.Audit -> "Auditoría visual"
    }
    val summary = when (step) {
        AdminConfigurationConvergenceStep.Entity -> "Lectura base de lo que se quiere ajustar."
        AdminConfigurationConvergenceStep.Editor -> "Preparación del cambio sin aplicarlo."
        AdminConfigurationConvergenceStep.Preview -> "Revisión conceptual antes de continuar."
        AdminConfigurationConvergenceStep.Impact -> "Evaluación de alcance y efectos esperados."
        AdminConfigurationConvergenceStep.SensitiveConfirmation -> "Validación previa para cambios sensibles."
        AdminConfigurationConvergenceStep.Result -> "Cierre de la secuencia de revisión."
        AdminConfigurationConvergenceStep.Audit -> "Consulta de trazabilidad sin edición."
    }
    val context = when (step) {
        AdminConfigurationConvergenceStep.Entity -> "Sección: $section · Subsección: $subsection.\nControla alcance, estado actual y restricciones sin ejecutar acciones."
        AdminConfigurationConvergenceStep.Editor -> "Valor actual y nuevo valor se muestran para revisar.\nNo se guardan cambios reales ni se publica contenido."
        AdminConfigurationConvergenceStep.Preview -> "Comparación conceptual del cambio.\nTodavía no está aplicado en la app real."
        AdminConfigurationConvergenceStep.Impact -> "Qué cambia, qué afecta y qué no cambia.\nSolo lectura de impacto, sin aplicación."
        AdminConfigurationConvergenceStep.SensitiveConfirmation -> "Confirmación humana del alcance y advertencias.\nEl botón de confirmar es solo visual en este bloque."
        AdminConfigurationConvergenceStep.Result -> "La revisión quedó preparada.\nEl cambio permanece sin confirmar."
        AdminConfigurationConvergenceStep.Audit -> "Registro preparado para buscar, filtrar y consultar.\nNo se edita ni se borra."
    }
    val action = when (step) {
        AdminConfigurationConvergenceStep.Entity -> "Ir al editor" to AdminConfigurationConvergenceStep.Editor
        AdminConfigurationConvergenceStep.Editor -> "Ir a preview" to AdminConfigurationConvergenceStep.Preview
        AdminConfigurationConvergenceStep.Preview -> "Revisar impacto" to AdminConfigurationConvergenceStep.Impact
        AdminConfigurationConvergenceStep.Impact -> "Continuar a confirmación" to AdminConfigurationConvergenceStep.SensitiveConfirmation
        AdminConfigurationConvergenceStep.SensitiveConfirmation -> "Confirmar de forma visual" to AdminConfigurationConvergenceStep.Result
        AdminConfigurationConvergenceStep.Result -> "Consultar auditoría visual" to AdminConfigurationConvergenceStep.Audit
        AdminConfigurationConvergenceStep.Audit -> "Revisar otro registro" to AdminConfigurationConvergenceStep.Audit
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
    onSection: (AdminOrderSection) -> Unit,
) {
    orderId?.let { LaunchedEffect(it) { onLoadDetail(it) } }
    val visibleNumber = adminOrderVisibleNumber(summary, detail, orderId)
    val source = detail?.source ?: summary?.source.orEmpty()
    val requestType = detail?.requestType ?: summary?.requestType.orEmpty()
    val publicStatus = detail?.publicStatus ?: summary?.publicStatus.orEmpty()
    val operationalStatus = detail?.operationalStatus ?: summary?.operationalStatus.orEmpty()
    val needsAttention = detail?.needsAttention ?: summary?.needsAttention ?: false
    val activeIncident = detail?.activeIncident ?: summary?.activeIncident ?: false
    val storeName = detail?.storeName ?: summary?.storeName.orEmpty()
    val identity = AdminOperationOrderClassification.operationalIdentity(source, requestType)
    val operationFunction = AdminOperationOrderClassification.operationalFunction(source, requestType)
    val signals = AdminOperationOrderSignals(
        status = detail?.status ?: summary?.status.orEmpty(),
        publicStatus = publicStatus,
        operationalStatus = operationalStatus,
        responsibleRole = detail?.responsibleRole ?: summary?.responsibleRole.orEmpty(),
        needsAttention = needsAttention,
        activeIncident = activeIncident,
        source = source,
        requestType = requestType,
    )
    val placement = AdminOperationOrderClassification.primaryPlacement(signals)
    val enteredToday = (detail?.createdAtMillis ?: summary?.createdAtMillis).isAdminToday()
    val problemFocus = adminOrderProblemFocus(
        variant = variant,
        publicStatus = publicStatus,
        operationalStatus = operationalStatus,
        needsAttention = needsAttention,
        activeIncident = activeIncident,
    )
    val statusText = when {
        placement == AdminOrderPrimaryPlacement.UNCLASSIFIED -> "Sin datos"
        placement == AdminOrderPrimaryPlacement.ACTIVE &&
            AdminOperationOrderClassification.activeBucket(signals) == AdminActiveOrdersBucket.REVIEW_STATE -> "Revisar estado"
        else -> adminHumanOperationStatus(
            publicStatus = publicStatus,
            operationalStatus = operationalStatus,
            rawStatus = detail?.status ?: summary?.status.orEmpty(),
            hasProblem = problemFocus != null,
        )
    }
    val operationTitle = adminOrderOperationSectionTitle(identity)
    val operationNote = when (identity) {
        AdminOperationOrderClassification.IDENTITY_LOCAL_PICKUP -> storeName.adminDisplayValue("Información de retiro")
        AdminOperationOrderClassification.IDENTITY_PLUS_BUY -> detail?.itemsSummary.adminItemsSummary()
        else -> "Información de retiro"
    }
    val navigationEntries = listOf(
        AdminOrderNavigationEntry(AdminOrderSection.Summary, Icons.Outlined.Dashboard, "Resumen", statusText),
        AdminOrderNavigationEntry(AdminOrderSection.Operation, operationIconFor(operationTitle), operationTitle, operationNote),
        AdminOrderNavigationEntry(AdminOrderSection.Delivery, Icons.Outlined.LocalShipping, "Entrega", detail.adminPersonName("Sin datos")),
        AdminOrderNavigationEntry(AdminOrderSection.Payment, Icons.Outlined.Payments, "Pago", (detail?.total ?: summary?.total).adminDisplayValue("Sin datos")),
        AdminOrderNavigationEntry(AdminOrderSection.Problems, Icons.Outlined.ReportProblem, "Problemas", problemFocus?.first ?: "Sin problemas"),
        AdminOrderNavigationEntry(AdminOrderSection.History, Icons.Outlined.History, "Historial", detail?.lastEventSummary.adminDisplayValue("Sin datos")),
        AdminOrderNavigationEntry(AdminOrderSection.Options, Icons.Outlined.Tune, "Opciones", "Sin acciones"),
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
                title = "Pedido $visibleNumber",
                eyebrow = "Operación",
                summary = identity,
                onSignOut = {},
                showSignOut = false,
            )
        }
        item { AdminOrderMomentPanel(title = statusText, detail = operationFunction, highlighted = problemFocus != null) }
        item {
            AdminOrderMomentPanel(
                title = placement.adminPlacementLabel(),
                detail = if (enteredToday) "Ingresó hoy" else "Ubicación principal",
                highlighted = placement == AdminOrderPrimaryPlacement.PROBLEM,
                eyebrow = "Ubicación actual",
            )
        }
        if (operationMessage.isNotBlank()) {
            item { AdminInfoPanel(title = "Resultado", text = operationMessage) }
        }
        if (operationError.isNotBlank()) {
            item { AdminInfoPanel(title = "Error operativo", text = operationError) }
        }
        items(navigationEntries) { entry ->
            AdminOrderNavigationCard(entry = entry, onClick = { onSection(entry.section) })
        }
    }
}

@Composable
private fun AdminOrderSectionScreen(
    section: AdminOrderSection,
    variant: OperationOrderVariant,
    orderId: String?,
    summary: AdminOrderSummary?,
    detail: AdminOrderDetail?,
) {
    val visibleNumber = adminOrderVisibleNumber(summary, detail, orderId)
    val source = detail?.source ?: summary?.source.orEmpty()
    val requestType = detail?.requestType ?: summary?.requestType.orEmpty()
    val identity = AdminOperationOrderClassification.operationalIdentity(source, requestType)
    val operationFunction = AdminOperationOrderClassification.operationalFunction(source, requestType)
    val publicStatus = detail?.publicStatus ?: summary?.publicStatus.orEmpty()
    val operationalStatus = detail?.operationalStatus ?: summary?.operationalStatus.orEmpty()
    val needsAttention = detail?.needsAttention ?: summary?.needsAttention ?: false
    val activeIncident = detail?.activeIncident ?: summary?.activeIncident ?: false
    val signals = AdminOperationOrderSignals(
        status = detail?.status ?: summary?.status.orEmpty(),
        publicStatus = publicStatus,
        operationalStatus = operationalStatus,
        responsibleRole = detail?.responsibleRole ?: summary?.responsibleRole.orEmpty(),
        needsAttention = needsAttention,
        activeIncident = activeIncident,
        source = source,
        requestType = requestType,
    )
    val placement = AdminOperationOrderClassification.primaryPlacement(signals)
    val storeName = detail?.storeName ?: summary?.storeName.orEmpty()
    val problem = adminOrderProblemFocus(
        variant = variant,
        publicStatus = publicStatus,
        operationalStatus = operationalStatus,
        needsAttention = needsAttention,
        activeIncident = activeIncident,
    )
    val status = when {
        placement == AdminOrderPrimaryPlacement.UNCLASSIFIED -> "Sin datos"
        placement == AdminOrderPrimaryPlacement.ACTIVE &&
            AdminOperationOrderClassification.activeBucket(signals) == AdminActiveOrdersBucket.REVIEW_STATE -> "Revisar estado"
        else -> adminHumanOperationStatus(
            publicStatus = publicStatus,
            operationalStatus = operationalStatus,
            rawStatus = detail?.status ?: summary?.status.orEmpty(),
            hasProblem = problem != null,
        )
    }
    val title = when (section) {
        AdminOrderSection.Summary -> "Resumen"
        AdminOrderSection.Operation -> adminOrderOperationSectionTitle(identity)
        AdminOrderSection.Delivery -> "Entrega"
        AdminOrderSection.Payment -> "Pago"
        AdminOrderSection.Problems -> "Problemas"
        AdminOrderSection.History -> "Historial"
        AdminOrderSection.Options -> "Opciones"
    }
    val facts = when (section) {
        AdminOrderSection.Summary -> listOf(
            "Tipo" to identity,
            "Estado" to status,
            "Función" to operationFunction,
            "Referencia" to (problem?.first ?: storeName.adminDisplayValue(detail?.itemsSummary.adminItemsSummary())),
        )
        AdminOrderSection.Operation -> adminOrderOperationFacts(identity, storeName, detail)
        AdminOrderSection.Delivery -> listOf("Persona" to detail.adminPersonName("Sin datos"))
        AdminOrderSection.Payment -> listOf("Total" to (detail?.total ?: summary?.total).adminDisplayValue("Sin datos"))
        AdminOrderSection.Problems -> listOf(
            "Estado" to (problem?.first ?: "Sin problemas"),
            "Seguimiento" to (problem?.second ?: "Sin problemas"),
        )
        AdminOrderSection.History -> listOf(
            "Último movimiento" to detail?.lastEventSummary.adminDisplayValue("Sin datos"),
        )
        AdminOrderSection.Options -> listOf("Opciones" to "Sin acciones")
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
                eyebrow = "Pedido $visibleNumber",
                summary = identity,
                onSignOut = {},
                showSignOut = false,
            )
        }
        item { AdminOrderFactPanel(title = title, facts = facts) }
    }
}

private fun adminOrderOperationSectionTitle(identity: String): String =
    when (identity) {
        AdminOperationOrderClassification.IDENTITY_PLUS_BUY -> "Compra"
        AdminOperationOrderClassification.IDENTITY_LOCAL_PICKUP -> "Local / Retiro"
        else -> "Retiro"
    }

private fun adminOrderOperationFacts(
    identity: String,
    storeName: String,
    detail: AdminOrderDetail?,
): List<Pair<String, String>> =
    when (identity) {
        AdminOperationOrderClassification.IDENTITY_PLUS_BUY -> listOf(
            "Detalle de compra" to detail?.itemsSummary.adminItemsSummary(),
        )
        AdminOperationOrderClassification.IDENTITY_LOCAL_PICKUP -> listOf(
            "Local" to storeName.adminDisplayValue("Sin datos"),
            "Retiro" to "Sin datos",
        )
        else -> listOf("Lugar de retiro" to storeName.adminDisplayValue("Sin datos"))
    }

@Composable
private fun AdminOrderNavigationCard(entry: AdminOrderNavigationEntry, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (pressed) 0.988f else 1f)
            .clip(RoundedCornerShape(14.dp))
            .background(if (pressed) PediloPanel else PediloPanelSoft, RoundedCornerShape(14.dp))
            .border(1.dp, if (pressed) PediloOrange.copy(alpha = 0.72f) else PediloLine, RoundedCornerShape(14.dp))
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = 72.dp)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(entry.icon, contentDescription = entry.title, tint = PediloOrange, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(entry.title, color = PediloText, fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.ExtraBold)
            AdminStatusChip(label = entry.note, toneColor = PediloMuted)
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = PediloMuted, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun AdminOrderMomentPanel(
    title: String,
    detail: String,
    highlighted: Boolean,
    eyebrow: String? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloPanelSoft, RoundedCornerShape(15.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(15.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        eyebrow?.let {
            Text(it, color = PediloMuted, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
        }
        AdminStatusChip(label = title, toneColor = if (highlighted) PediloWarning else PediloGreen)
        Text(detail, color = PediloText, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun AdminStatusChip(label: String, toneColor: Color) {
    Text(
        text = label,
        color = toneColor,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(toneColor.copy(alpha = 0.12f), RoundedCornerShape(50))
            .border(1.dp, toneColor.copy(alpha = 0.34f), RoundedCornerShape(50))
            .padding(horizontal = 9.dp, vertical = 4.dp),
    )
}

@Composable
private fun AdminOrderFactPanel(title: String, facts: List<Pair<String, String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloPanelSoft, RoundedCornerShape(15.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(15.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, color = PediloText, fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.ExtraBold)
        facts.forEach { (label, value) ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, color = PediloMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(value, color = PediloText, fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold)
            }
        }
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
    is AdminRoute.OperationOrderSection -> AdminRoot.Operation
    is AdminRoute.ConfigurationSection -> AdminRoot.Configuration
    is AdminRoute.ConfigurationSubsection -> AdminRoot.Configuration
    is AdminRoute.ConfigurationConvergence -> AdminRoot.Configuration
    is AdminRoute.ConfigurationPublicWorld -> AdminRoot.Configuration
    is AdminRoute.ConfigurationPublicWorldPart -> AdminRoot.Configuration
    is AdminRoute.ConfigurationPublicWorldEditor -> AdminRoot.Configuration
    is AdminRoute.RoleAccessSection -> AdminRoot.RoleAccess
    is AdminRoute.RoleAccessSubsection -> AdminRoot.RoleAccess
    is AdminRoute.RoleAccessConvergence -> AdminRoot.RoleAccess
    is AdminRoute.Section -> root
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
    val todaySignals = signals.filter { (order, _) -> order.createdAtMillis.isAdminToday() }
    return when (kind) {
        AdminOperationListKind.TodayAll -> todaySignals.map { it.first }
        AdminOperationListKind.TodayActive -> todaySignals.filter { (_, s) ->
            AdminOperationOrderClassification.primaryPlacement(s) == AdminOrderPrimaryPlacement.ACTIVE
        }.map { it.first }
        AdminOperationListKind.TodayProblems -> todaySignals.filter { (_, s) ->
            AdminOperationOrderClassification.primaryPlacement(s) == AdminOrderPrimaryPlacement.PROBLEM
        }.map { it.first }
        AdminOperationListKind.TodayClosed -> todaySignals.filter { (_, s) ->
            AdminOperationOrderClassification.primaryPlacement(s) in setOf(
                AdminOrderPrimaryPlacement.FINISHED,
                AdminOrderPrimaryPlacement.CANCELLED,
            )
        }.map { it.first }
        AdminOperationListKind.TodayReview -> todaySignals.filter { (_, s) ->
            AdminOperationOrderClassification.primaryPlacement(s) == AdminOrderPrimaryPlacement.UNCLASSIFIED
        }.map { it.first }
        AdminOperationListKind.Unclassified -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.primaryPlacement(s) == AdminOrderPrimaryPlacement.UNCLASSIFIED
        }.map { it.first }
        AdminOperationListKind.ClosedFinished -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.primaryPlacement(s) == AdminOrderPrimaryPlacement.FINISHED
        }.map { it.first }
        AdminOperationListKind.ClosedCancelled -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.primaryPlacement(s) == AdminOrderPrimaryPlacement.CANCELLED
        }.map { it.first }
        AdminOperationListKind.ActiveWaitingStore -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.primaryPlacement(s) == AdminOrderPrimaryPlacement.ACTIVE &&
                AdminOperationOrderClassification.activeBucket(s) == AdminActiveOrdersBucket.WAITING_STORE
        }.map { it.first }
        AdminOperationListKind.ActivePreparing -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.primaryPlacement(s) == AdminOrderPrimaryPlacement.ACTIVE &&
                AdminOperationOrderClassification.activeBucket(s) == AdminActiveOrdersBucket.PREPARING
        }.map { it.first }
        AdminOperationListKind.ActiveWaitingDriver -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.primaryPlacement(s) == AdminOrderPrimaryPlacement.ACTIVE &&
                AdminOperationOrderClassification.activeBucket(s) == AdminActiveOrdersBucket.WAITING_DRIVER
        }.map { it.first }
        AdminOperationListKind.ActiveInDelivery -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.primaryPlacement(s) == AdminOrderPrimaryPlacement.ACTIVE &&
                AdminOperationOrderClassification.activeBucket(s) == AdminActiveOrdersBucket.IN_DELIVERY
        }.map { it.first }
        AdminOperationListKind.ActiveReviewState -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.primaryPlacement(s) == AdminOrderPrimaryPlacement.ACTIVE &&
                AdminOperationOrderClassification.activeBucket(s) == AdminActiveOrdersBucket.REVIEW_STATE
        }.map { it.first }
        AdminOperationListKind.ProblemStoreNotResponding -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.primaryPlacement(s) == AdminOrderPrimaryPlacement.PROBLEM &&
                AdminOperationOrderClassification.problemBucket(s) == AdminProblemOrdersBucket.STORE_NOT_RESPONDING
        }.map { it.first }
        AdminOperationListKind.ProblemUserClaim -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.primaryPlacement(s) == AdminOrderPrimaryPlacement.PROBLEM &&
                AdminOperationOrderClassification.problemBucket(s) == AdminProblemOrdersBucket.CUSTOMER_CLAIM
        }.map { it.first }
        AdminOperationListKind.ProblemDelayed -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.primaryPlacement(s) == AdminOrderPrimaryPlacement.PROBLEM &&
                AdminOperationOrderClassification.problemBucket(s) == AdminProblemOrdersBucket.DELAYED
        }.map { it.first }
        AdminOperationListKind.ProblemWithoutResponsible -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.primaryPlacement(s) == AdminOrderPrimaryPlacement.PROBLEM &&
                AdminOperationOrderClassification.problemBucket(s) == AdminProblemOrdersBucket.WITHOUT_RESPONSIBLE
        }.map { it.first }
        AdminOperationListKind.ProblemOperationalReview -> signals.filter { (_, s) ->
            AdminOperationOrderClassification.primaryPlacement(s) == AdminOrderPrimaryPlacement.PROBLEM &&
                AdminOperationOrderClassification.problemBucket(s) == AdminProblemOrdersBucket.OPERATIONAL_REVIEW
        }.map { it.first }
        else -> emptyList()
    }.distinctBy { it.id }
}

private fun List<AdminOrderSummary>.forPrimaryPlacement(
    placement: AdminOrderPrimaryPlacement,
): List<AdminOrderSummary> =
    filter {
        AdminOperationOrderClassification.primaryPlacement(AdminOperationOrderSignals.from(it)) == placement
    }.distinctBy { it.id }

private fun List<AdminOrderSummary>.forPrimaryPlacements(
    vararg placements: AdminOrderPrimaryPlacement,
): List<AdminOrderSummary> =
    filter {
        AdminOperationOrderClassification.primaryPlacement(AdminOperationOrderSignals.from(it)) in placements
    }.distinctBy { it.id }

private fun Long?.isAdminToday(): Boolean {
    if (this == null) return false
    val now = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = this@isAdminToday }
    return now.get(Calendar.ERA) == date.get(Calendar.ERA) &&
        now.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
}

private fun AdminOperationListKind.isOrderList(): Boolean =
    this in setOf(
        AdminOperationListKind.TodayAll,
        AdminOperationListKind.TodayActive,
        AdminOperationListKind.TodayProblems,
        AdminOperationListKind.TodayClosed,
        AdminOperationListKind.TodayReview,
        AdminOperationListKind.Unclassified,
        AdminOperationListKind.ClosedFinished,
        AdminOperationListKind.ClosedCancelled,
        AdminOperationListKind.ActiveWaitingStore,
        AdminOperationListKind.ActivePreparing,
        AdminOperationListKind.ActiveWaitingDriver,
        AdminOperationListKind.ActiveInDelivery,
        AdminOperationListKind.ActiveReviewState,
        AdminOperationListKind.ProblemStoreNotResponding,
        AdminOperationListKind.ProblemUserClaim,
        AdminOperationListKind.ProblemDelayed,
        AdminOperationListKind.ProblemWithoutResponsible,
        AdminOperationListKind.ProblemOperationalReview,
    )

private fun AdminOrderPrimaryPlacement.adminPlacementLabel(): String =
    when (this) {
        AdminOrderPrimaryPlacement.PROBLEM -> "Con problemas"
        AdminOrderPrimaryPlacement.ACTIVE -> "Activo"
        AdminOrderPrimaryPlacement.FINISHED -> "Finalizado"
        AdminOrderPrimaryPlacement.CANCELLED -> "Cancelado"
        AdminOrderPrimaryPlacement.UNCLASSIFIED -> "Revisar pedido"
    }
