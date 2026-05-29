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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    data class OperationOrderDetail(val returnRoute: AdminRoute, val variant: OperationOrderVariant) : AdminRoute
    data class OperationOrderSolve(val returnRoute: AdminRoute, val stage: OperationSolveStage) : AdminRoute
    data class OperationOperationalProfile(val kind: AdminOperationalProfileKind, val state: String) : AdminRoute
    data class TodayOrdersCategory(val category: AdminTodayOrdersCategory) : AdminRoute
    data class TodayOrdersSubsection(val category: AdminTodayOrdersCategory, val title: String) : AdminRoute
    data class Section(val root: AdminRoot, val title: String) : AdminRoute
}

private enum class AdminOperationalProfileKind {
    Store,
    Driver,
}

private data class AdminEntry(
    val title: String,
    val note: String,
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
        contextText = "Clasifica los pedidos abiertos del día sin abrir pedidos concretos ni ejecutar acciones.",
        entries = listOf(
            AdminEntry("Esperando local", "Aguardan aceptación"),
            AdminEntry("Preparando", "En preparación"),
            AdminEntry("Esperando repartidor", "Aguardan asignación"),
            AdminEntry("En entrega", "Camino al cliente"),
        ),
    ),
    AdminTodayOrdersCategory(
        title = "Finalizados",
        summary = "Pedidos del día cerrados correctamente",
        contextText = "Ordena cierres correctos del día sin mostrar listados reales.",
        entries = listOf(
            AdminEntry("Entregados", "Llegaron al cliente"),
            AdminEntry("Retirados", "Retirados en local"),
            AdminEntry("Enviados", "Completados por envío"),
        ),
    ),
    AdminTodayOrdersCategory(
        title = "Cancelados",
        summary = "Pedidos del día cerrados sin completar",
        contextText = "Separa motivos de cancelación sin abrir gestiones ni modificar estados.",
        entries = listOf(
            AdminEntry("Cancelados por cliente", "Cierre solicitado por cliente"),
            AdminEntry("Cancelados por local", "Cierre iniciado por local"),
            AdminEntry("Cancelados por operación", "Cierre desde operación"),
        ),
    ),
    AdminTodayOrdersCategory(
        title = "Demorados",
        summary = "Pedidos del día con tiempo excedido",
        contextText = "Agrupa retrasos por momento operativo sin resolver casos todavía.",
        entries = listOf(
            AdminEntry("Esperando local", "Demora de aceptación"),
            AdminEntry("Preparando", "Demora de preparación"),
            AdminEntry("En entrega", "Demora de reparto"),
        ),
    ),
    AdminTodayOrdersCategory(
        title = "Con problemas",
        summary = "Pedidos del día marcados con incidencia",
        contextText = "Clasifica incidencias del día sin abrir gestiones ni acciones finales.",
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

private val roleEntries = listOf(
    "Usuarios del equipo",
    "Administradores",
    "Locales store",
    "Repartidores driver",
    "Altas pendientes",
    "Usuarios inactivos",
    "Vinculaciones pendientes",
).map { AdminEntry(it, "Organización de accesos") }

private fun orderDetailEntriesFor(sectionTitle: String, subsectionTitle: String): List<AdminOrderDetailEntry> {
    val primary = when {
        sectionTitle == "Pedidos activos" && subsectionTitle == "En entrega" ->
            AdminOrderDetailEntry("Pedido #____", "Estado normal", OperationOrderVariant.Normal)
        sectionTitle == "Pedidos activos" && subsectionTitle == "Esperando local" ->
            AdminOrderDetailEntry("Pedido #____", "Necesita atención", OperationOrderVariant.NeedsAttention)
        sectionTitle == "Pedidos con problemas" && subsectionTitle == "Local no responde" ->
            AdminOrderDetailEntry("Pedido #____", "Con problema", OperationOrderVariant.WithProblem)
        sectionTitle == "Pedidos activos" && subsectionTitle == "Esperando repartidor" ->
            AdminOrderDetailEntry("Pedido #____", "Acción no disponible", OperationOrderVariant.ActionUnavailable)
        else -> null
    }
    return primary?.let { listOf(it) } ?: emptyList()
}

@Composable
fun AdminApp(onSignOutConfirmed: () -> Unit) {
    var route by remember { mutableStateOf<AdminRoute>(AdminRoute.Operation) }
    var showSignOut by remember { mutableStateOf(false) }

    BackHandler(enabled = route !is AdminRoute.Operation && route !is AdminRoute.Configuration && route !is AdminRoute.RoleAccess) {
        route = when (val current = route) {
            is AdminRoute.OperationOrderSolve -> when (current.stage) {
                OperationSolveStage.Start -> current.returnRoute
                OperationSolveStage.Options -> AdminRoute.OperationOrderSolve(current.returnRoute, OperationSolveStage.Start)
                OperationSolveStage.SensitiveAction -> AdminRoute.OperationOrderSolve(current.returnRoute, OperationSolveStage.Options)
                OperationSolveStage.Result -> AdminRoute.OperationOrderSolve(current.returnRoute, OperationSolveStage.SensitiveAction)
            }
            is AdminRoute.OperationOperationalProfile -> AdminRoute.Operation
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
            AdminRoute.Operation -> AdminRootScreen(
                title = "Pédilo Admin",
                eyebrow = "Operación",
                summary = "Vista inicial para seguir la operación.",
                entries = operationEntries,
                onEntry = { entry ->
                    operationSections.firstOrNull { it.title == entry.title }?.let {
                        route = AdminRoute.OperationSection(it)
                    }
                },
                onSignOut = { showSignOut = true },
                showSignOut = true,
            )
            AdminRoute.Configuration -> AdminRootScreen(
                title = "Configuración",
                eyebrow = "Secciones de configuración",
                summary = "Organizá cómo funciona Pédilo.",
                entries = configurationEntries,
                onEntry = { route = AdminRoute.Section(AdminRoot.Configuration, it.title) },
                onSignOut = { showSignOut = true },
                showSignOut = false,
            )
            AdminRoute.RoleAccess -> AdminRootScreen(
                title = "Alta de roles",
                eyebrow = "Usuarios y accesos",
                summary = "Cuentas, roles y vinculaciones.",
                entries = roleEntries,
                onEntry = { route = AdminRoute.Section(AdminRoot.RoleAccess, it.title) },
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
                val orderEntries = orderDetailEntriesFor(current.section.title, current.title)
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
                onSolve = {
                    route = AdminRoute.OperationOrderSolve(
                        returnRoute = AdminRoute.OperationOrderDetail(current.returnRoute, current.variant),
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
private fun AdminSectionScreen(
    root: AdminRoot,
    title: String,
    summary: String,
    panelTitle: String,
    panelText: String,
    orderDetailEntries: List<AdminOrderDetailEntry> = emptyList(),
    onOrderDetail: (OperationOrderVariant) -> Unit = {},
    onOperationalProfile: (AdminOperationalProfileKind) -> Unit = {},
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
    }
}

@Composable
private fun AdminOrderDetailScreen(
    variant: OperationOrderVariant,
    onSolve: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenDriver: () -> Unit,
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
                title = "Pedido #____",
                eyebrow = "Operación",
                summary = "Qué está pasando con este pedido ahora.",
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
                        text = "Referencia ·----\nLocal asignado ·----\nReparto ·----",
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
    is AdminRoute.TodayOrdersCategory -> AdminRoot.Operation
    is AdminRoute.TodayOrdersSubsection -> AdminRoot.Operation
    is AdminRoute.Section -> root
}
