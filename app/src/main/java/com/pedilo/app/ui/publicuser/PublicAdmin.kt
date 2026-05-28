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

private sealed interface AdminRoute {
    data object Operation : AdminRoute
    data object Configuration : AdminRoute
    data object RoleAccess : AdminRoute
    data class OperationSection(val section: AdminOperationSection) : AdminRoute
    data class OperationSubsection(val section: AdminOperationSection, val title: String) : AdminRoute
    data class TodayOrdersCategory(val category: AdminTodayOrdersCategory) : AdminRoute
    data class TodayOrdersSubsection(val category: AdminTodayOrdersCategory, val title: String) : AdminRoute
    data class Section(val root: AdminRoot, val title: String) : AdminRoute
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

@Composable
fun AdminApp(onSignOutConfirmed: () -> Unit) {
    var route by remember { mutableStateOf<AdminRoute>(AdminRoute.Operation) }
    var showSignOut by remember { mutableStateOf(false) }

    BackHandler(enabled = route !is AdminRoute.Operation && route !is AdminRoute.Configuration && route !is AdminRoute.RoleAccess) {
        route = when (val current = route) {
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
            is AdminRoute.OperationSubsection -> AdminSectionScreen(
                root = AdminRoot.Operation,
                title = current.title,
                summary = "Submundo operativo preparado para organizar la siguiente capa.",
                panelTitle = current.section.title,
                panelText = "Este espacio mantiene la separación operativa sin listados reales ni acciones disponibles.",
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
    is AdminRoute.TodayOrdersCategory -> AdminRoot.Operation
    is AdminRoute.TodayOrdersSubsection -> AdminRoot.Operation
    is AdminRoute.Section -> root
}
