package com.pedilo.app.ui.publicuser

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class PublicPlusRequestType(val label: String) {
    Buy("Compra"),
    PickupShipping("Retiro / Envío"),
}

data class PublicPlusItem(
    val name: String,
    val detail: String,
)

data class PublicPlusRequest(
    val type: PublicPlusRequestType,
    val items: List<PublicPlusItem>,
    val source: String,
    val destination: String,
    val contactName: String,
    val phone: String,
    val schedule: String,
    val alreadyPaid: Boolean,
    val payment: String,
    val amount: String,
    val notes: String,
    val orderNumber: String = "PDL-123456",
)

private enum class PlusIconKind {
    Buy,
    Delivery,
    Plus,
    Check,
    Ticket,
    Tracking,
    Wallet,
    Location,
    Note,
}

@Composable
fun PublicPlusChoiceScreen(
    onHome: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
    onBuy: () -> Unit,
    onPickupShipping: () -> Unit,
) {
    PublicShell(
        current = PublicBottomDestination.Plus,
        onHome = onHome,
        onPlus = onPlus,
        onShop = onShop,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(PediloBg),
            contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { PlusHeader(title = "¿Qué necesitás?", subtitle = "Elegí una solicitud directa de Pédilo") }
            item {
                ChoiceHeroCard()
            }
            item {
                PlusChoiceCard(
                    title = "Quiero comprar",
                    subtitle = "Cargá productos uno por uno y decinos dónde comprarlos.",
                    badge = "Compra",
                    icon = PlusIconKind.Buy,
                    onClick = onBuy,
                )
            }
            item {
                PlusChoiceCard(
                    title = "Hacer un retiro / envío",
                    subtitle = "Pedí que retiremos algo y lo llevemos a destino.",
                    badge = "Logística",
                    icon = PlusIconKind.Delivery,
                    onClick = onPickupShipping,
                )
            }
            item { PlusNotice("No abre locales, no crea carrito de local y no genera pedidos reales.") }
        }
    }
}

@Composable
fun PublicPlusBuyScreen(
    onHome: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
    onContinue: (PublicPlusRequest) -> Unit,
) {
    var productName by remember { mutableStateOf("Coca-Cola 1.5L") }
    var productDetail by remember { mutableStateOf("2 unidades") }
    val products = remember {
        mutableStateListOf(
            PublicPlusItem("Pan francés 1kg", "Para entregar hoy"),
        )
    }
    var source by remember { mutableStateOf("Supermercado o almacén cercano") }
    var notes by remember { mutableStateOf("Si no hay marca, aceptar similar.") }
    var contactName by remember { mutableStateOf("Persona solicitante") }
    var phone by remember { mutableStateOf("11 5555 5555") }
    var address by remember { mutableStateOf("Av. Siempre Viva 1234") }

    PublicShell(current = PublicBottomDestination.Plus, onHome = onHome, onPlus = onPlus, onShop = onShop) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(PediloBg),
            contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { PlusHeader(title = "Quiero comprar", subtitle = "Cargá productos para una compra directa") }
            item {
                PlusInput("Producto", productName, "Coca-Cola 1.5L", onValueChange = { productName = it })
            }
            item {
                PlusInput("Cantidad / detalle", productDetail, "2 unidades", onValueChange = { productDetail = it })
            }
            item {
                PlusActionButton(
                    label = "Agregar producto",
                    icon = PlusIconKind.Plus,
                    onClick = {
                        val cleanName = productName.ifBlank { "Producto sin nombre" }
                        val cleanDetail = productDetail.ifBlank { "Sin detalle" }
                        products.add(PublicPlusItem(cleanName, cleanDetail))
                        productName = ""
                        productDetail = ""
                    },
                )
            }
            item {
                ProductListCard(products)
            }
            item { PlusInput("Dónde comprar", source, "Kiosco, farmacia, supermercado", onValueChange = { source = it }) }
            item { PlusInput("Nombre", contactName, "Tu nombre", onValueChange = { contactName = it }) }
            item { PlusInput("WhatsApp", phone, "11 5555 5555", onValueChange = { phone = it }) }
            item { PlusInput("Dirección de entrega", address, "Calle y altura", onValueChange = { address = it }) }
            item { PlusInput("Observaciones", notes, "Detalle opcional", minHeight = 92.dp, singleLine = false, onValueChange = { notes = it }) }
            item {
                PlusActionButton(
                    label = "Continuar a confirmación",
                    icon = PlusIconKind.Check,
                    onClick = {
                        val requestProducts = products.ifEmpty {
                            listOf(PublicPlusItem("Coca-Cola 1.5L", "1 unidad"))
                        }
                        onContinue(
                            PublicPlusRequest(
                                type = PublicPlusRequestType.Buy,
                                items = requestProducts.toList(),
                                source = source.ifBlank { "Comercio cercano" },
                                destination = address.ifBlank { "Dirección pendiente" },
                                contactName = contactName.ifBlank { "Persona solicitante" },
                                phone = phone.ifBlank { "WhatsApp pendiente" },
                                schedule = "Lo antes posible",
                                alreadyPaid = false,
                                payment = "Efectivo o transferencia al recibir",
                                amount = "$18.500",
                                notes = notes.ifBlank { "Sin observaciones" },
                            ),
                        )
                    },
                )
            }
        }
    }
}

@Composable
fun PublicPlusPickupShippingScreen(
    onHome: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
    onContinue: (PublicPlusRequest) -> Unit,
) {
    var pickupAddress by remember { mutableStateOf("Av. Corrientes 123") }
    var destination by remember { mutableStateOf("Av. Siempre Viva 1234") }
    var schedule by remember { mutableStateOf("Ahora") }
    var contactName by remember { mutableStateOf("Persona solicitante") }
    var phone by remember { mutableStateOf("11 5555 5555") }
    var alreadyPaid by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("$7.800") }
    var description by remember { mutableStateOf("Sobre cerrado para retirar en recepción") }

    PublicShell(current = PublicBottomDestination.Plus, onHome = onHome, onPlus = onPlus, onShop = onShop) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(PediloBg),
            contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { PlusHeader(title = "Retiro / Envío", subtitle = "Cargá los datos operativos del traslado") }
            item { PlusInput("Dirección de retiro", pickupAddress, "Dónde retiramos", onValueChange = { pickupAddress = it }) }
            item { PlusInput("Dirección de entrega", destination, "Dónde entregamos", onValueChange = { destination = it }) }
            item { ScheduleSelector(selected = schedule, onSelected = { schedule = it }) }
            item { PlusInput("A nombre de quién está", contactName, "Nombre de referencia", onValueChange = { contactName = it }) }
            item { PlusInput("WhatsApp", phone, "11 5555 5555", onValueChange = { phone = it }) }
            item {
                PaymentStateCard(
                    alreadyPaid = alreadyPaid,
                    onToggle = { alreadyPaid = !alreadyPaid },
                )
            }
            if (!alreadyPaid) {
                item { PlusInput("Monto a pagar", amount, "$7.800", onValueChange = { amount = it }) }
            }
            item {
                PlusInput(
                    label = "Descripción de lo que hay que retirar/enviar",
                    value = description,
                    placeholder = "Paquete, sobre, documentación, compra ya paga",
                    minHeight = 108.dp,
                    singleLine = false,
                    onValueChange = { description = it },
                )
            }
            item {
                PlusActionButton(
                    label = "Continuar a confirmación",
                    icon = PlusIconKind.Check,
                    onClick = {
                        onContinue(
                            PublicPlusRequest(
                                type = PublicPlusRequestType.PickupShipping,
                                items = listOf(PublicPlusItem(description.ifBlank { "Objeto a retirar" }, "Operación logística")),
                                source = pickupAddress.ifBlank { "Retiro pendiente" },
                                destination = destination.ifBlank { "Entrega pendiente" },
                                contactName = contactName.ifBlank { "Persona solicitante" },
                                phone = phone.ifBlank { "WhatsApp pendiente" },
                                schedule = schedule,
                                alreadyPaid = alreadyPaid,
                                payment = if (alreadyPaid) "Ya está pago" else "Pagar al retirar",
                                amount = if (alreadyPaid) "$0" else amount.ifBlank { "Monto a confirmar" },
                                notes = description.ifBlank { "Sin descripción" },
                            ),
                        )
                    },
                )
            }
        }
    }
}

@Composable
fun PublicPlusConfirmationScreen(
    request: PublicPlusRequest,
    onHome: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
    onConfirm: (PublicPlusRequest) -> Unit,
) {
    PublicShell(current = PublicBottomDestination.Plus, onHome = onHome, onPlus = onPlus, onShop = onShop) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(PediloBg),
            contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { PlusHeader(title = "Confirmación", subtitle = "Revisá la solicitud antes de enviarla") }
            item { RequestSummaryCard(request = request, expanded = true) }
            item {
                DetailLineCard(
                    title = "Contacto",
                    lines = listOf(request.contactName, request.phone, request.destination),
                    icon = PlusIconKind.Location,
                )
            }
            item {
                DetailLineCard(
                    title = "Pago",
                    lines = listOf(request.payment, "Importe de muestra ${request.amount}", "Horario: ${request.schedule}"),
                    icon = PlusIconKind.Wallet,
                )
            }
            item {
                PlusActionButton(
                    label = "Confirmar pedido",
                    icon = PlusIconKind.Check,
                    onClick = { onConfirm(request) },
                )
            }
            item { PlusNotice("Esta confirmación no crea pedido real ni escribe datos en Firebase.") }
        }
    }
}

@Composable
fun PublicPlusTicketScreen(
    request: PublicPlusRequest,
    onHome: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
    onTracking: (String) -> Unit,
) {
    PublicShell(current = PublicBottomDestination.Plus, onHome = onHome, onPlus = onPlus, onShop = onShop) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(PediloBg),
            contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { TicketHero(request) }
            item { RequestSummaryCard(request = request, expanded = false) }
            item {
                DetailLineCard(
                    title = "Estado inicial",
                    lines = listOf("Recibido", request.payment, "Total de muestra ${request.amount}"),
                    icon = PlusIconKind.Ticket,
                )
            }
            item {
                PlusActionButton(
                    label = "Ver seguimiento",
                    icon = PlusIconKind.Tracking,
                    onClick = { onTracking(request.orderNumber) },
                )
            }
            item {
                SecondaryPlusAction(
                    label = "Volver al inicio",
                    icon = PlusIconKind.Check,
                    onClick = onHome,
                )
            }
            item { PlusNotice("Ticket visual de muestra. No hay pago real, WhatsApp real ni tracking persistente.") }
        }
    }
}

@Composable
private fun PlusHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = PediloOrange,
            fontSize = 30.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = subtitle,
            color = PediloMuted,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ChoiceHeroCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(PediloOrangeDark.copy(alpha = 0.45f), PediloPanel)), RoundedCornerShape(16.dp))
            .border(1.dp, PediloOrangeDark, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(PediloOrange),
            contentAlignment = Alignment.Center,
        ) {
            PlusIcon(PlusIconKind.Plus, tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Solicitud directa", color = PediloText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Elegí si necesitás comprar algo o mover un paquete.", color = PediloMuted, fontSize = 13.sp, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun PlusChoiceCard(
    title: String,
    subtitle: String,
    badge: String,
    icon: PlusIconKind,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloOverlay, RoundedCornerShape(15.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(15.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = title }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(PediloPanelSoft, RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center,
        ) {
            PlusIcon(icon, tint = PediloOrange, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = PediloText, fontSize = 18.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = PediloMuted, fontSize = 12.sp, lineHeight = 15.sp)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            badge,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(PediloOrange, RoundedCornerShape(18.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun ProductListCard(products: List<PublicPlusItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloOverlay, RoundedCornerShape(15.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(15.dp))
            .padding(14.dp),
    ) {
        Text("Productos agregados", color = PediloText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        products.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${index + 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(PediloOrange, CircleShape).padding(horizontal = 8.dp, vertical = 4.dp))
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, color = PediloText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(item.detail, color = PediloMuted, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ScheduleSelector(selected: String, onSelected: (String) -> Unit) {
    val options = listOf("Ahora", "Programar")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloPanel, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .padding(13.dp),
    ) {
        Text("Horario de retiro", color = PediloMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val active = selected == option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(if (active) PediloOrange else PediloPanelSoft, RoundedCornerShape(22.dp))
                        .border(1.dp, if (active) PediloOrange else PediloLine, RoundedCornerShape(22.dp))
                        .clickable(role = Role.Button, onClick = { onSelected(option) }),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(option, color = if (active) Color.White else PediloMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PaymentStateCard(alreadyPaid: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloOverlay, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onToggle)
            .semantics { contentDescription = "Cambiar estado de pago" }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlusIcon(PlusIconKind.Wallet, tint = PediloOrange, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("¿Ya está pago?", color = PediloText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(if (alreadyPaid) "Sí, solo retirar y entregar" else "No, hay monto a pagar", color = PediloMuted, fontSize = 12.sp)
        }
        Text(if (alreadyPaid) "Sí" else "No", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(PediloOrange, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 6.dp))
    }
}

@Composable
private fun RequestSummaryCard(request: PublicPlusRequest, expanded: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloOverlay, RoundedCornerShape(15.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(15.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlusIcon(if (request.type == PublicPlusRequestType.Buy) PlusIconKind.Buy else PlusIconKind.Delivery, tint = PediloOrange, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(request.type.label, color = PediloText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Número de muestra ${request.orderNumber}", color = PediloMuted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        request.items.take(if (expanded) 10 else 2).forEach {
            Text("• ${it.name}", color = PediloText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(it.detail, color = PediloMuted, fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp, bottom = 5.dp))
        }
        Text("Origen: ${request.source}", color = PediloMuted, fontSize = 12.sp)
        Text("Destino: ${request.destination}", color = PediloMuted, fontSize = 12.sp)
        if (expanded) {
            Text("Observaciones: ${request.notes}", color = PediloMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun DetailLineCard(title: String, lines: List<String>, icon: PlusIconKind) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloPanel, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(Modifier.size(44.dp).background(PediloPanelSoft, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            PlusIcon(icon, tint = PediloOrange, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = PediloText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            lines.forEach { Text(it, color = PediloMuted, fontSize = 12.sp, lineHeight = 16.sp) }
        }
    }
}

@Composable
private fun TicketHero(request: PublicPlusRequest) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(PediloOrangeDark.copy(alpha = 0.42f), PediloPanel)), RoundedCornerShape(18.dp))
            .border(1.dp, PediloOrangeDark, RoundedCornerShape(18.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(72.dp).clip(CircleShape).background(PediloOrange), contentAlignment = Alignment.Center) {
            PlusIcon(PlusIconKind.Check, tint = Color.White, modifier = Modifier.size(38.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text("Pedido recibido", color = PediloText, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        Text(request.orderNumber, color = PediloOrange, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Estado inicial: Recibido", color = PediloMuted, fontSize = 14.sp)
    }
}

@Composable
private fun PlusInput(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    minHeight: Dp = 58.dp,
    singleLine: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloPanel, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .padding(13.dp),
    ) {
        Text(label, color = PediloMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(7.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = PediloText, fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
            singleLine = singleLine,
            modifier = Modifier
                .fillMaxWidth()
                .height(minHeight),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PediloPanelSoft, RoundedCornerShape(11.dp))
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                ) {
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            color = PediloMuted,
                            fontSize = 16.sp,
                            maxLines = if (singleLine) 1 else 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun PlusActionButton(label: String, icon: PlusIconKind, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(Brush.verticalGradient(listOf(PediloOrangeSoft, PediloOrange)), RoundedCornerShape(14.dp))
            .border(1.dp, PediloOrange, RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlusIcon(icon, tint = Color.White, modifier = Modifier.size(25.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SecondaryPlusAction(label: String, icon: PlusIconKind, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(PediloBg, RoundedCornerShape(14.dp))
            .border(1.dp, PediloOrange, RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlusIcon(icon, tint = PediloOrange, modifier = Modifier.size(23.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = PediloOrange, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PlusNotice(text: String) {
    Text(
        text = text,
        color = PediloMuted,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloPanel, RoundedCornerShape(12.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(12.dp))
            .padding(12.dp),
    )
}

@Composable
private fun PlusIcon(kind: PlusIconKind, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = size.minDimension * 0.09f, cap = StrokeCap.Round)
        when (kind) {
            PlusIconKind.Buy -> {
                drawLine(tint, Offset(size.width * 0.18f, size.height * 0.25f), Offset(size.width * 0.3f, size.height * 0.25f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawRoundRect(tint, Offset(size.width * 0.32f, size.height * 0.34f), Size(size.width * 0.48f, size.height * 0.28f), CornerRadius(size.width * 0.04f), style = stroke)
                drawCircle(tint, size.minDimension * 0.06f, Offset(size.width * 0.42f, size.height * 0.78f))
                drawCircle(tint, size.minDimension * 0.06f, Offset(size.width * 0.72f, size.height * 0.78f))
            }
            PlusIconKind.Delivery, PlusIconKind.Tracking -> {
                drawRoundRect(tint, Offset(size.width * 0.16f, size.height * 0.28f), Size(size.width * 0.52f, size.height * 0.35f), CornerRadius(size.width * 0.07f), style = stroke)
                drawLine(tint, Offset(size.width * 0.68f, size.height * 0.39f), Offset(size.width * 0.82f, size.height * 0.39f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.72f, size.height * 0.55f), Offset(size.width * 0.86f, size.height * 0.55f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawCircle(tint, size.minDimension * 0.07f, Offset(size.width * 0.28f, size.height * 0.76f), style = stroke)
                drawCircle(tint, size.minDimension * 0.07f, Offset(size.width * 0.66f, size.height * 0.76f), style = stroke)
            }
            PlusIconKind.Plus -> {
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.2f), Offset(size.width * 0.5f, size.height * 0.8f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.2f, size.height * 0.5f), Offset(size.width * 0.8f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            PlusIconKind.Check -> {
                drawLine(tint, Offset(size.width * 0.24f, size.height * 0.52f), Offset(size.width * 0.42f, size.height * 0.7f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.42f, size.height * 0.7f), Offset(size.width * 0.78f, size.height * 0.28f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            PlusIconKind.Ticket -> {
                drawRoundRect(tint, Offset(size.width * 0.18f, size.height * 0.25f), Size(size.width * 0.64f, size.height * 0.5f), CornerRadius(size.width * 0.08f), style = stroke)
                drawLine(tint, Offset(size.width * 0.32f, size.height * 0.42f), Offset(size.width * 0.68f, size.height * 0.42f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.32f, size.height * 0.58f), Offset(size.width * 0.58f, size.height * 0.58f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            PlusIconKind.Wallet -> {
                drawRoundRect(tint, Offset(size.width * 0.16f, size.height * 0.32f), Size(size.width * 0.68f, size.height * 0.42f), CornerRadius(size.width * 0.08f), style = stroke)
                drawCircle(tint, size.minDimension * 0.04f, Offset(size.width * 0.68f, size.height * 0.53f))
            }
            PlusIconKind.Location -> {
                drawCircle(tint, size.minDimension * 0.24f, Offset(size.width * 0.5f, size.height * 0.4f), style = stroke)
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.64f), Offset(size.width * 0.5f, size.height * 0.88f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawCircle(tint, size.minDimension * 0.04f, Offset(size.width * 0.5f, size.height * 0.4f))
            }
            PlusIconKind.Note -> {
                val note = Path().apply {
                    moveTo(size.width * 0.24f, size.height * 0.18f)
                    lineTo(size.width * 0.68f, size.height * 0.18f)
                    lineTo(size.width * 0.8f, size.height * 0.32f)
                    lineTo(size.width * 0.8f, size.height * 0.82f)
                    lineTo(size.width * 0.24f, size.height * 0.82f)
                    close()
                }
                drawPath(note, tint, style = stroke)
                drawLine(tint, Offset(size.width * 0.34f, size.height * 0.48f), Offset(size.width * 0.66f, size.height * 0.48f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.34f, size.height * 0.62f), Offset(size.width * 0.58f, size.height * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
        }
    }
}
