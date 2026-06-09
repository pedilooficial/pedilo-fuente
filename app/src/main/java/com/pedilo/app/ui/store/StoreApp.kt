package com.pedilo.app.ui.store

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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedilo.app.core.model.AdminLiveOrderActionRequest
import com.pedilo.app.core.model.LiveOrderAction
import com.pedilo.app.core.model.StoreOrderDetail
import com.pedilo.app.core.model.StoreOrderSummary
import com.pedilo.app.core.result.CoreError
import com.pedilo.app.core.result.CoreResult
import com.pedilo.app.core.runtime.storeOrdersUseCase
import com.pedilo.app.ui.components.PediloTextField
import com.pedilo.app.ui.publicuser.PediloBg
import com.pedilo.app.ui.publicuser.PediloGreen
import com.pedilo.app.ui.publicuser.PediloLine
import com.pedilo.app.ui.publicuser.PediloMuted
import com.pedilo.app.ui.publicuser.PediloOrange
import com.pedilo.app.ui.publicuser.PediloPanel
import com.pedilo.app.ui.publicuser.PediloPanelSoft
import com.pedilo.app.ui.publicuser.PediloText
import com.pedilo.app.ui.publicuser.PediloWarning
import kotlinx.coroutines.launch

private data class PendingStoreAction(
    val orderId: String,
    val action: LiveOrderAction,
    val expectedVersion: Int,
)

@Composable
fun StoreApp(onSignOutConfirmed: () -> Unit) {
    val storeOrders = remember { storeOrdersUseCase() }
    val scope = rememberCoroutineScope()
    var orders by remember { mutableStateOf<List<StoreOrderSummary>>(emptyList()) }
    var selectedOrderId by remember { mutableStateOf<String?>(null) }
    var detail by remember { mutableStateOf<StoreOrderDetail?>(null) }
    var message by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var pendingAction by remember { mutableStateOf<PendingStoreAction?>(null) }
    var pendingReason by remember { mutableStateOf("") }

    fun refreshDetail(orderId: String) {
        scope.launch {
            when (val result = storeOrders.getDetail(orderId)) {
                is CoreResult.Success -> detail = result.value
                is CoreResult.Failure -> error = result.error.storeErrorMessage()
            }
        }
    }

    fun executeAction(pending: PendingStoreAction, reason: String) {
        scope.launch {
            message = ""
            error = ""
            when (val result = storeOrders.execute(
                AdminLiveOrderActionRequest(
                    orderId = pending.orderId,
                    action = pending.action,
                    expectedVersion = pending.expectedVersion,
                    reason = reason,
                ),
            )) {
                is CoreResult.Success -> {
                    message = result.value.humanMessage.ifBlank { result.value.eventSummary }
                    pendingAction = null
                    pendingReason = ""
                    refreshDetail(pending.orderId)
                }
                is CoreResult.Failure -> {
                    error = result.error.storeErrorMessage()
                    pendingAction = null
                    pendingReason = ""
                    refreshDetail(pending.orderId)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        storeOrders.observe().collect { result ->
            when (result) {
                is CoreResult.Success -> orders = result.value
                is CoreResult.Failure -> {
                    orders = emptyList()
                    error = result.error.storeErrorMessage()
                }
            }
        }
    }

    selectedOrderId?.let { LaunchedEffect(it) { refreshDetail(it) } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PediloBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            StoreHeader(
                title = "Local",
                subtitle = "Pedidos propios asignados a tu cuenta",
                action = "Cerrar sesión",
                onAction = onSignOutConfirmed,
            )
        }
        if (message.isNotBlank()) item { StoreInfoCard("Resultado", message, PediloGreen) }
        if (error.isNotBlank()) item { StoreInfoCard("Error", error, PediloWarning) }
        if (selectedOrderId == null) {
            if (orders.isEmpty()) {
                item { StoreInfoCard("Pedidos", "No hay pedidos propios para operar.", PediloMuted) }
            } else {
                items(orders) { order ->
                    StoreOrderCard(order = order, onClick = {
                        selectedOrderId = order.id
                        message = ""
                        error = ""
                    })
                }
                item { StoreInfoCard("Productos y stock", "Gestión visual no disponible en este bloque. No se guardan cambios de catálogo ni disponibilidad.", PediloMuted) }
                item { StoreInfoCard("Solicitud de repartidor", "La asignación real queda a cargo del flujo operativo seguro. El local no solicita repartidor desde esta pantalla.", PediloMuted) }
                item { StoreInfoCard("Finanzas", "Sin caja, deuda, liquidaciones ni pagos reales en este bloque.", PediloMuted) }
            }
        } else {
            item {
                TextButton(onClick = {
                    selectedOrderId = null
                    detail = null
                    message = ""
                    error = ""
                }) {
                    Text("Volver")
                }
            }
            val current = detail
            if (current == null) {
                item { StoreInfoCard("Pedido", "Cargando pedido.", PediloMuted) }
            } else {
                item { StoreOrderDetailCard(current) }
                if (current.activeIncident) {
                    item { StoreInfoCard("Incidencia activa", "El pedido está bajo revisión operativa.", PediloWarning) }
                }
                if (current.nextAllowedActions.isEmpty()) {
                    item { StoreInfoCard("Sin acciones disponibles", "El backend no habilita acciones para este pedido o versión. Si el pedido está cerrado, no hay acciones normales.", PediloMuted) }
                } else {
                    item { StoreInfoCard("Acciones", "Permitidas por backend para la versión ${current.version}.", PediloOrange) }
                    items(current.nextAllowedActions) { action ->
                        StoreActionCard(action = action, onClick = {
                            pendingAction = PendingStoreAction(current.id, action, current.version)
                            pendingReason = ""
                        })
                    }
                }
            }
        }
    }

    pendingAction?.let { pending ->
        val requiresReason = pending.action.requiresStoreReason()
        AlertDialog(
            onDismissRequest = {
                pendingAction = null
                pendingReason = ""
            },
            title = { Text("Confirmar acción") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(pending.action.storeLabel())
                    Text(pending.action.storeImpact())
                    if (requiresReason) {
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
                    enabled = !requiresReason || pendingReason.trim().length >= 4,
                    onClick = { executeAction(pending, pendingReason.trim()) },
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingAction = null
                    pendingReason = ""
                }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun StoreHeader(title: String, subtitle: String, action: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloPanel, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            Text(title, color = PediloText, fontSize = 26.sp, lineHeight = 30.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = PediloMuted, fontSize = 13.sp, lineHeight = 17.sp)
        }
        TextButton(onClick = onAction) { Text(action) }
    }
}

@Composable
private fun StoreOrderCard(order: StoreOrderSummary, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PediloPanelSoft, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Pedido #${order.visibleNumber}", color = PediloText, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text(order.publicStatus.ifBlank { order.operationalStatus }, color = PediloOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(order.itemsSummary.joinToString(" · ").ifBlank { "Productos no informados" }, color = PediloMuted, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text("${order.nextAllowedActions.size} acciones disponibles", color = PediloMuted, fontSize = 12.sp)
    }
}

@Composable
private fun StoreOrderDetailCard(order: StoreOrderDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloPanelSoft, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Pedido #${order.visibleNumber}", color = PediloText, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text(order.publicStatus.ifBlank { order.operationalStatus }, color = PediloOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("Estado operativo: ${order.operationalStatus.ifBlank { "No informado" }}", color = PediloMuted, fontSize = 13.sp)
        Text("Persona: ${order.contactName.ifBlank { "No informado" }}", color = PediloMuted, fontSize = 13.sp)
        order.itemsSummary.forEach {
            Text(it, color = PediloText, fontSize = 14.sp, lineHeight = 18.sp)
        }
        Text("Total: ${order.total.ifBlank { "No informado" }}", color = PediloMuted, fontSize = 13.sp)
    }
}

@Composable
private fun StoreActionCard(action: LiveOrderAction, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PediloPanel, RoundedCornerShape(14.dp))
            .border(1.dp, PediloOrange.copy(alpha = 0.42f), RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(action.storeLabel(), color = PediloText, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        Text(action.storeImpact(), color = PediloMuted, fontSize = 13.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun StoreInfoCard(title: String, message: String, tone: androidx.compose.ui.graphics.Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(tone.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
            .border(1.dp, tone.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(title, color = tone, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        Text(message, color = PediloText, fontSize = 14.sp, lineHeight = 18.sp)
    }
}

private fun LiveOrderAction.storeLabel(): String =
    when (this) {
        LiveOrderAction.LocalAccept -> "Aceptar pedido"
        LiveOrderAction.LocalReject -> "Rechazar pedido"
        LiveOrderAction.LocalMarkPreparing -> "Marcar en preparación"
        LiveOrderAction.LocalMarkReady -> "Marcar listo"
        LiveOrderAction.CancelOrder -> "Cancelar pedido"
        LiveOrderAction.OpenIncident -> "Reportar problema"
        else -> "Acción no disponible"
    }

private fun LiveOrderAction.storeImpact(): String =
    when (this) {
        LiveOrderAction.LocalAccept -> "Confirma que el local toma el pedido."
        LiveOrderAction.LocalReject -> "Cierra el pedido con motivo auditado."
        LiveOrderAction.LocalMarkPreparing -> "Informa que el pedido está en preparación."
        LiveOrderAction.LocalMarkReady -> "Deja el pedido listo para retiro."
        LiveOrderAction.CancelOrder -> "Cancela el pedido con motivo auditado si el backend lo permite."
        LiveOrderAction.OpenIncident -> "Usalo para producto no disponible, demora o problema operativo con motivo claro."
        else -> "Backend no habilitó esta acción para el local."
    }

private fun LiveOrderAction.requiresStoreReason(): Boolean =
    this in setOf(LiveOrderAction.LocalReject, LiveOrderAction.CancelOrder, LiveOrderAction.OpenIncident)

private fun CoreError.storeErrorMessage(): String =
    when (this) {
        is CoreError.Operational -> humanMessage
        CoreError.NotAvailable -> "No pudimos cargar tus pedidos."
        CoreError.IncompleteData -> "Faltan datos para operar el pedido."
        is CoreError.Validation -> "Revisá los datos antes de confirmar."
        CoreError.Unknown -> "No pudimos completar la operación."
    }
