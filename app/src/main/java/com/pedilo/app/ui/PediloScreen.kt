package com.pedilo.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pedilo.app.R
import com.pedilo.app.domain.Order
import com.pedilo.app.domain.OrderDraft
import com.pedilo.app.domain.OrderEvent
import com.pedilo.app.domain.PublicOrderForm
import com.pedilo.app.domain.PublicOrderValidator
import com.pedilo.app.domain.UserProfile
import com.pedilo.app.domain.UserRole
import com.pedilo.app.ui.components.PediloBackground
import com.pedilo.app.ui.components.PediloButton
import com.pedilo.app.ui.components.PediloCard
import com.pedilo.app.ui.components.PediloLoader
import com.pedilo.app.ui.components.PediloTextField

@Composable
fun PediloScreen(viewModel: PediloViewModel) {
    val state by viewModel.state.collectAsState()
    var showOperatorLogin by remember { mutableStateOf(false) }
    var actionWithNote by remember { mutableStateOf<Pair<Order, String>?>(null) }
    var assignOrder by remember { mutableStateOf<Order?>(null) }
    var adminStatusOrder by remember { mutableStateOf<Order?>(null) }

    BackHandler(enabled = showOperatorLogin) {
        showOperatorLogin = false
    }

    Scaffold(
        topBar = {
            PediloTopBar(
                profile = state.operatorProfile,
                onOperatorAccess = { showOperatorLogin = true },
                onSignOut = viewModel::signOutOperator
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                showOperatorLogin && state.operatorProfile == null -> OperatorLoginScreen(
                    isLoading = state.isSigningIn,
                    onBack = { showOperatorLogin = false },
                    onSignIn = viewModel::signInOperator
                )
                state.operatorProfile != null -> OperatorHome(
                    state = state,
                    onSelectOrder = viewModel::selectOrder,
                    onAdminStatus = { adminStatusOrder = it },
                    onAction = { order, action ->
                        when (action) {
                            "assign_driver" -> assignOrder = order
                            "cancel_order", "report_problem", "resolve_problem" -> {
                                actionWithNote = order to action
                            }
                            else -> viewModel.runAction(order.id, action)
                        }
                    }
                )
                else -> PublicOrderScreen(
                    orderId = state.publicOrderId,
                    isSubmitting = state.isSubmittingOrder,
                    hasError = state.error != null,
                    onOperatorAccess = { showOperatorLogin = true },
                    onCreate = viewModel::createPublicOrder
                )
            }
        }
    }

    actionWithNote?.let { (order, action) ->
        NoteDialog(
            title = actionLabel(action),
            onDismiss = { actionWithNote = null },
            onConfirm = { note ->
                actionWithNote = null
                viewModel.runAction(order.id, action, note)
            }
        )
    }

    assignOrder?.let { order ->
        AssignDriverDialog(
            onDismiss = { assignOrder = null },
            onConfirm = { driverId ->
                assignOrder = null
                viewModel.assignDriver(order.id, driverId)
            }
        )
    }

    adminStatusOrder?.let { order ->
        AdminStatusDialog(
            order = order,
            onDismiss = { adminStatusOrder = null },
            onConfirm = { status, note ->
                adminStatusOrder = null
                viewModel.adminSetStatus(order.id, status, note)
            }
        )
    }

    state.selectedOrder?.let { order ->
        AuditDialog(
            order = order,
            events = state.selectedEvents,
            onDismiss = { viewModel.selectOrder(null) }
        )
    }

    state.error?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            confirmButton = { TextButton(onClick = viewModel::clearError) { Text("Entendido") } },
            title = { Text("Acción no realizada") },
            text = { Text(message) }
        )
    }
}

@Composable
private fun PediloTopBar(
    profile: UserProfile?,
    onOperatorAccess: () -> Unit,
    onSignOut: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_512),
                    contentDescription = "Pédilo",
                    modifier = Modifier.size(44.dp)
                )
                Spacer(Modifier.size(12.dp))
                Column {
                    Text("Pédilo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    profile?.let {
                        Text(
                            "${it.displayName} · ${roleLabel(it.role)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (profile == null) {
                TextButton(onClick = onOperatorAccess) { Text("Operadores") }
            } else {
                TextButton(onClick = onSignOut) { Text("Salir") }
            }
        }
    }
}

@Composable
private fun PublicOrderScreen(
    orderId: String?,
    isSubmitting: Boolean,
    hasError: Boolean,
    onOperatorAccess: () -> Unit,
    onCreate: (OrderDraft) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var detail by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }
    val isSent = orderId != null
    val form = PublicOrderForm(
        requesterName = name,
        contactPhone = phone,
        deliveryAddress = address,
        itemsText = detail,
        note = note
    )
    val validation = PublicOrderValidator.validate(form)
    val canSubmit = validation.isValid
    val hasInput = name.isNotBlank() || phone.isNotBlank() || address.isNotBlank() || detail.isNotBlank() || note.isNotBlank()
    val showErrors = attemptedSubmit || hasInput
    val phase = when {
        isSubmitting -> PublicOrderUiPhase.Submitting
        isSent -> PublicOrderUiPhase.Success
        hasError -> PublicOrderUiPhase.Error
        hasInput -> PublicOrderUiPhase.Editing
        else -> PublicOrderUiPhase.Idle
    }

    LaunchedEffect(orderId) {
        if (orderId != null) {
            name = ""
            phone = ""
            address = ""
            detail = ""
            note = ""
            attemptedSubmit = false
        }
    }

    PediloBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Pedido público", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Enviá tu pedido al sistema. No necesitás iniciar sesión.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Estado: ${publicPhaseLabel(phase)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(18.dp))
            PediloTextField(
                value = name,
                label = "Nombre",
                enabled = !isSubmitting && !isSent,
                isError = showErrors && validation.requesterNameError != null,
                supportingText = if (showErrors) validation.requesterNameError else null,
                onValueChange = { name = it }
            )
            Spacer(Modifier.height(10.dp))
            PediloTextField(
                value = phone,
                label = "Teléfono",
                enabled = !isSubmitting && !isSent,
                isError = showErrors && validation.contactPhoneError != null,
                supportingText = if (showErrors) validation.contactPhoneError else null,
                onValueChange = { phone = it }
            )
            Spacer(Modifier.height(10.dp))
            PediloTextField(
                value = address,
                label = "Dirección de entrega",
                enabled = !isSubmitting && !isSent,
                isError = showErrors && validation.deliveryAddressError != null,
                supportingText = if (showErrors) validation.deliveryAddressError else null,
                onValueChange = { address = it }
            )
            Spacer(Modifier.height(10.dp))
            PediloTextField(
                value = detail,
                label = "Detalle del pedido",
                enabled = !isSubmitting && !isSent,
                isError = showErrors && validation.itemsTextError != null,
                supportingText = if (showErrors) validation.itemsTextError else null,
                minLines = 4,
                maxLines = 8,
                onValueChange = { detail = it }
            )
            Spacer(Modifier.height(10.dp))
            PediloTextField(
                value = note,
                label = "Nota opcional",
                enabled = !isSubmitting && !isSent,
                isError = showErrors && validation.noteError != null,
                supportingText = if (showErrors) validation.noteError else null,
                minLines = 2,
                maxLines = 4,
                onValueChange = { note = it }
            )
            Spacer(Modifier.height(16.dp))
            PediloButton(
                text = if (isSubmitting) "Enviando pedido..." else "Enviar pedido",
                enabled = canSubmit && !isSubmitting && !isSent,
                onClick = {
                    attemptedSubmit = true
                    if (validation.isValid) onCreate(form.toDraft())
                }
            )
            if (!canSubmit && attemptedSubmit && !isSent) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Revisá los campos marcados antes de enviar.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (isSubmitting) {
                Spacer(Modifier.height(14.dp))
                PediloLoader("Enviando pedido")
            }
            orderId?.let {
                Spacer(Modifier.height(14.dp))
                PublicConfirmation(orderId = it)
            }
            Spacer(Modifier.height(18.dp))
            OutlinedButton(
                onClick = onOperatorAccess,
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrar como operador")
            }
        }
    }
}

@Composable
private fun PublicConfirmation(orderId: String) {
    PediloCard {
        Text("Pedido enviado correctamente", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text("El pedido fue recibido por el sistema.")
        Spacer(Modifier.height(6.dp))
        Text(
            "Seguimiento: ${orderId.take(8)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Estado: enviado",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OperatorLoginScreen(
    isLoading: Boolean,
    onBack: () -> Unit,
    onSignIn: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    PediloBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Acceso operadores", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            PediloTextField(
                value = email,
                label = "Email",
                enabled = !isLoading,
                onValueChange = { email = it }
            )
            Spacer(Modifier.height(10.dp))
            PediloTextField(
                value = password,
                label = "Contraseña",
                enabled = !isLoading,
                visualTransformation = PasswordVisualTransformation(),
                onValueChange = { password = it }
            )
            Spacer(Modifier.height(16.dp))
            PediloButton(
                text = if (isLoading) "Entrando..." else "Entrar",
                enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                onClick = { onSignIn(email, password) }
            )
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onBack,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }
    }
}

@Composable
private fun OperatorHome(
    state: PediloUiState,
    onSelectOrder: (Order?) -> Unit,
    onAdminStatus: (Order) -> Unit,
    onAction: (Order, String) -> Unit
) {
    val profile = state.operatorProfile ?: return
    PediloBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(mainTitle(profile.role), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            if (state.isLoadingOperator) {
                item { PediloLoader("Cargando operación") }
            }
            if (state.orders.isEmpty() && !state.isLoadingOperator) {
                item { EmptyOrders(profile.role) }
            }
            items(state.orders, key = { it.id }) { order ->
                OrderCard(
                    order = order,
                    role = profile.role,
                    onAudit = { onSelectOrder(order) },
                    onAdminStatus = { onAdminStatus(order) },
                    onAction = { action -> onAction(order, action) }
                )
            }
        }
    }
}

@Composable
private fun EmptyOrders(role: UserRole) {
    PediloCard {
        Text(
            text = when (role) {
                UserRole.Store -> "No hay pedidos asignados al local."
                UserRole.Driver -> "No hay entregas asignadas."
                UserRole.Admin -> "No hay pedidos vivos."
            }
        )
    }
}

@Composable
private fun OrderCard(
    order: Order,
    role: UserRole,
    onAudit: () -> Unit,
    onAdminStatus: () -> Unit,
    onAction: (String) -> Unit
) {
    PediloCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Pedido ${order.id.take(8)}", fontWeight = FontWeight.SemiBold)
                if (order.requesterName.isNotBlank()) {
                    Text(
                        order.requesterName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    order.deliveryAddress,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AssistChip(onClick = {}, label = { Text(statusLabel(order.status.wireName)) })
        }
        Spacer(Modifier.height(10.dp))
        Text(order.items.joinToString { "${it.quantity} x ${it.name}" })
        if (order.problemNote?.isNotBlank() == true) {
            Spacer(Modifier.height(8.dp))
            Text("Problema: ${order.problemNote}", color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onAudit) { Text("Historial") }
            if (role == UserRole.Admin && order.adminAllowedStatuses.isNotEmpty()) {
                OutlinedButton(onClick = onAdminStatus) { Text("Cambiar") }
            }
        }
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            order.actionsFor(role).forEach { action ->
                PediloButton(
                    text = actionLabel(action),
                    onClick = { onAction(action) }
                )
            }
        }
    }
}

@Composable
private fun AdminStatusDialog(
    order: Order,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var selected by remember { mutableStateOf(order.adminAllowedStatuses.firstOrNull().orEmpty()) }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar estado") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                order.adminAllowedStatuses.forEach { status ->
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selected = status }
                    ) {
                        Text(if (selected == status) "* ${statusLabel(status)}" else statusLabel(status))
                    }
                }
                PediloTextField(note, "Motivo", onValueChange = { note = it })
            }
        },
        confirmButton = {
            PediloButton(
                text = "Confirmar",
                enabled = selected.isNotBlank(),
                modifier = Modifier,
                onClick = { onConfirm(selected, note) }
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun NoteDialog(title: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { PediloTextField(note, "Motivo o detalle", minLines = 2, onValueChange = { note = it }) },
        confirmButton = { PediloButton("Confirmar", modifier = Modifier, onClick = { onConfirm(note) }) },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun AssignDriverDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var driverId by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Asignar repartidor") },
        text = { PediloTextField(driverId, "UID del repartidor", onValueChange = { driverId = it }) },
        confirmButton = {
            PediloButton(
                text = "Asignar",
                enabled = driverId.isNotBlank(),
                modifier = Modifier,
                onClick = { onConfirm(driverId) }
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun AuditDialog(order: Order, events: List<OrderEvent>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Historial ${order.id.take(8)}") },
        text = {
            LazyColumn(
                modifier = Modifier.height(360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events, key = { it.id }) { event ->
                    Column {
                        Text(event.type, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${event.actorRole}: ${event.fromStatus?.wireName ?: "-"} -> ${event.toStatus?.wireName ?: "-"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (!event.note.isNullOrBlank()) {
                            Text(event.note, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

private fun mainTitle(role: UserRole): String = when (role) {
    UserRole.Store -> "Pedidos del local"
    UserRole.Driver -> "Mis entregas"
    UserRole.Admin -> "Pedidos vivos"
}

private fun roleLabel(role: UserRole): String = when (role) {
    UserRole.Store -> "Local"
    UserRole.Driver -> "Repartidor"
    UserRole.Admin -> "Admin"
}

private fun actionLabel(action: String): String = when (action) {
    "assign_driver" -> "Asignar repartidor"
    "mark_picked_up" -> "Retirado"
    "mark_on_the_way" -> "En camino"
    "mark_delivered" -> "Entregado"
    "cancel_order" -> "Cancelar"
    "report_problem" -> "Problema"
    "resolve_problem" -> "Resolver"
    else -> action
}

private fun statusLabel(status: String): String = status.replace('_', ' ')

private fun publicPhaseLabel(phase: PublicOrderUiPhase): String = when (phase) {
    PublicOrderUiPhase.Idle -> "completando pedido"
    PublicOrderUiPhase.Editing -> "editando"
    PublicOrderUiPhase.Submitting -> "enviando"
    PublicOrderUiPhase.Success -> "enviado"
    PublicOrderUiPhase.Error -> "requiere revisión"
}
