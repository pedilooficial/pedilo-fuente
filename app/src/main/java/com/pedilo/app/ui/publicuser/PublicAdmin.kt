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
    data class Section(val root: AdminRoot, val title: String) : AdminRoute
}

private data class AdminEntry(
    val title: String,
    val note: String,
)

private val operationEntries = listOf(
    AdminEntry("Pedidos del día", "Sin datos conectados"),
    AdminEntry("Pedidos activos", "Sin datos conectados"),
    AdminEntry("Pedidos con problemas", "Visual"),
    AdminEntry("Repartidores activos", "Sin datos conectados"),
    AdminEntry("Locales activos", "Sin datos conectados"),
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
).map { AdminEntry(it, "Estructura visual futura") }

private val roleEntries = listOf(
    "Usuarios del equipo",
    "Administradores",
    "Locales store",
    "Repartidores driver",
    "Altas pendientes",
    "Usuarios inactivos",
    "Vinculaciones pendientes",
).map { AdminEntry(it, "Acceso visual futuro") }

@Composable
fun AdminApp(onSignOutConfirmed: () -> Unit) {
    var route by remember { mutableStateOf<AdminRoute>(AdminRoute.Operation) }
    var showSignOut by remember { mutableStateOf(false) }

    BackHandler(enabled = route is AdminRoute.Section) {
        route = when ((route as AdminRoute.Section).root) {
            AdminRoot.Operation -> AdminRoute.Operation
            AdminRoot.Configuration -> AdminRoute.Configuration
            AdminRoot.RoleAccess -> AdminRoute.RoleAccess
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
                summary = "Base visual para monitorear lo vivo cuando se conecten datos reales.",
                entries = operationEntries,
                onEntry = { route = AdminRoute.Section(AdminRoot.Operation, it.title) },
                onSignOut = { showSignOut = true },
            )
            AdminRoute.Configuration -> AdminRootScreen(
                title = "Configuración",
                eyebrow = "Estructura editable futura",
                summary = "Raíz visual para organizar parámetros sin editar nada todavía.",
                entries = configurationEntries,
                onEntry = { route = AdminRoute.Section(AdminRoot.Configuration, it.title) },
                onSignOut = { showSignOut = true },
            )
            AdminRoute.RoleAccess -> AdminRootScreen(
                title = "Alta de roles",
                eyebrow = "Usuarios y accesos",
                summary = "Raíz visual para futuras cuentas, roles y vinculaciones.",
                entries = roleEntries,
                onEntry = { route = AdminRoute.Section(AdminRoot.RoleAccess, it.title) },
                onSignOut = { showSignOut = true },
            )
            is AdminRoute.Section -> AdminSectionScreen(
                root = current.root,
                title = current.title,
                onBack = {
                    route = when (current.root) {
                        AdminRoot.Operation -> AdminRoute.Operation
                        AdminRoot.Configuration -> AdminRoute.Configuration
                        AdminRoot.RoleAccess -> AdminRoute.RoleAccess
                    }
                },
                onSignOut = { showSignOut = true },
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
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AdminHeader(title = title, eyebrow = eyebrow, summary = summary, onSignOut = onSignOut)
        }
        items(entries) {
            AdminEntryCard(entry = it, onClick = { onEntry(it) })
        }
    }
}

@Composable
private fun AdminSectionScreen(
    root: AdminRoot,
    title: String,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AdminHeader(
                title = title,
                eyebrow = root.label,
                summary = "Pantalla visual sin datos conectados ni acciones reales.",
                onSignOut = onSignOut,
            )
        }
        item {
            AdminInfoPanel(
                title = "Base visual",
                text = "Esta sección queda preparada para el siguiente bloque. No lee datos, no guarda cambios y no opera pedidos.",
            )
        }
        item {
            AdminActionButton(text = "Volver", onClick = onBack)
        }
    }
}

@Composable
private fun AdminHeader(
    title: String,
    eyebrow: String,
    summary: String,
    onSignOut: () -> Unit,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    eyebrow,
                    color = PediloWarning,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    title,
                    color = PediloOrange,
                    fontSize = 31.sp,
                    lineHeight = 35.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = TextStyle(brush = PediloPrimaryBrush),
                )
            }
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
private fun AdminActionButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .pediloButtonDepth(RoundedCornerShape(15.dp))
            .background(PediloPrimaryBrush, RoundedCornerShape(15.dp))
            .border(1.dp, PediloWarning.copy(alpha = 0.4f), RoundedCornerShape(15.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
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
            .height(88.dp)
            .background(Brush.verticalGradient(listOf(PediloPanelSoft.copy(alpha = 0.96f), PediloPanel)), RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            .height(62.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (selected) PediloOrange.copy(alpha = 0.18f) else Color.Transparent, RoundedCornerShape(15.dp))
            .border(1.dp, if (selected) PediloOrange.copy(alpha = 0.62f) else PediloLine.copy(alpha = 0.45f), RoundedCornerShape(15.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            label,
            color = if (selected) PediloOrange else PediloMuted,
            fontSize = 12.sp,
            lineHeight = 15.sp,
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
    is AdminRoute.Section -> root
}
