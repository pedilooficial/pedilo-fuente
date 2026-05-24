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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class PublicOrderStep(val label: String) {
    Received("Recibido"),
    Preparing("En preparación"),
    OnTheWay("En camino"),
    Delivered("Entregado"),
}

private data class PublicTrackingData(
    val orderNumber: String,
    val mainStatus: String,
    val subtitle: String,
    val activeStep: PublicOrderStep,
    val eta: String,
    val address: String,
    val city: String,
)

@Composable
fun PublicShopTrackingScreen(
    orderNumber: String,
    current: PublicBottomDestination = PublicBottomDestination.Shop,
    onHome: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
) {
    val tracking = remember(orderNumber) {
        PublicTrackingData(
            orderNumber = orderNumber.ifBlank { "PDL-123456" },
            mainStatus = "Tu pedido va en camino",
            subtitle = "Estamos por llegar a tu dirección",
            activeStep = PublicOrderStep.OnTheWay,
            eta = "10-15 min",
            address = "Av. Siempre Viva 1234",
            city = "CABA, Argentina",
        )
    }

    PublicShell(
        current = current,
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
            item { OrderNumberCard(tracking.orderNumber) }
            item { TrackingProgressCard(tracking) }
            item {
                TrackingInfoCard(
                    icon = TrackingIconKind.Clock,
                    label = "Tiempo estimado de llegada",
                    value = tracking.eta,
                )
            }
            item {
                TrackingInfoCard(
                    icon = TrackingIconKind.Pin,
                    label = "Dirección de entrega",
                    value = tracking.address,
                    detail = tracking.city,
                )
            }
            item {
                TrackingActionButton(
                    label = "Cancelar pedido",
                    icon = TrackingIconKind.Cancel,
                    filled = true,
                    onClick = { },
                )
            }
            item {
                TrackingActionButton(
                    label = "Reportar problema",
                    icon = TrackingIconKind.Warning,
                    filled = false,
                    onClick = { },
                )
            }
        }
    }
}

@Composable
private fun TrackingHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Seguimiento del pedido",
            color = PediloOrange,
            fontSize = 28.sp,
            lineHeight = 31.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "Consulta el estado actual de tu pedido",
            color = PediloMuted,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun OrderNumberCard(orderNumber: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloPanel, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Número de pedido", color = PediloMuted, fontSize = 13.sp)
            Text(orderNumber, color = PediloText, fontSize = 25.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold)
        }
        TrackingIcon(TrackingIconKind.Copy, tint = PediloOrange, modifier = Modifier.size(30.dp))
    }
}

@Composable
private fun TrackingProgressCard(tracking: PublicTrackingData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(PediloOrangeDark.copy(alpha = 0.32f), PediloPanel)), RoundedCornerShape(15.dp))
            .border(1.dp, PediloOrangeDark, RoundedCornerShape(15.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tracking.mainStatus,
                    color = PediloOrange,
                    fontSize = 28.sp,
                    lineHeight = 31.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(Modifier.height(7.dp))
                Text(tracking.subtitle, color = PediloMuted, fontSize = 14.sp)
            }
            TrackingIcon(TrackingIconKind.Scooter, tint = PediloOrange, modifier = Modifier.size(74.dp))
        }
        Spacer(Modifier.height(22.dp))
        TrackingSteps(activeStep = tracking.activeStep)
    }
}

@Composable
private fun TrackingSteps(activeStep: PublicOrderStep) {
    val steps = PublicOrderStep.entries
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        steps.forEachIndexed { index, step ->
            val passed = step.ordinal < activeStep.ordinal
            val active = step == activeStep
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(if (passed || active) PediloOrange else PediloLine),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(if (active) 54.dp else 44.dp)
                            .clip(CircleShape)
                            .background(if (passed) PediloOrange else if (active) PediloPanel else PediloLine)
                            .border(2.dp, if (active) PediloOrange else Color.Transparent, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        TrackingIcon(
                            kind = if (step == PublicOrderStep.Delivered) TrackingIconKind.Box else if (active) TrackingIconKind.Scooter else TrackingIconKind.Check,
                            tint = if (passed) Color.White else if (active) PediloOrange else PediloMuted,
                            modifier = Modifier.size(if (active) 28.dp else 22.dp),
                        )
                    }
                    if (index < steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(if (passed) PediloOrange else PediloLine),
                        )
                    }
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    text = step.label,
                    color = if (active) PediloOrange else PediloMuted,
                    fontSize = 10.sp,
                    lineHeight = 11.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun TrackingInfoCard(
    icon: TrackingIconKind,
    label: String,
    value: String,
    detail: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloOverlay, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .background(PediloPanelSoft, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            TrackingIcon(icon, tint = PediloOrange, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = PediloMuted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, color = PediloText, fontSize = 22.sp, lineHeight = 25.sp, fontWeight = FontWeight.Bold)
            detail?.let {
                Text(it, color = PediloMuted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun TrackingActionButton(
    label: String,
    icon: TrackingIconKind,
    filled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                if (filled) Brush.verticalGradient(listOf(PediloOrangeSoft, PediloOrange)) else Brush.verticalGradient(listOf(PediloBg, PediloBg)),
                RoundedCornerShape(13.dp),
            )
            .border(1.dp, PediloOrange, RoundedCornerShape(13.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        TrackingIcon(icon, tint = if (filled) Color.White else PediloOrange, modifier = Modifier.size(25.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = if (filled) Color.White else PediloOrange, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

private enum class TrackingIconKind {
    Check,
    Scooter,
    Box,
    Clock,
    Pin,
    Cancel,
    Warning,
    Copy,
}

@Composable
private fun TrackingIcon(kind: TrackingIconKind, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = size.minDimension * 0.1f, cap = StrokeCap.Round)
        when (kind) {
            TrackingIconKind.Check -> {
                drawLine(tint, Offset(size.width * 0.24f, size.height * 0.52f), Offset(size.width * 0.42f, size.height * 0.7f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.42f, size.height * 0.7f), Offset(size.width * 0.78f, size.height * 0.28f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            TrackingIconKind.Scooter -> {
                drawCircle(tint, size.minDimension * 0.12f, Offset(size.width * 0.27f, size.height * 0.72f), style = stroke)
                drawCircle(tint, size.minDimension * 0.12f, Offset(size.width * 0.72f, size.height * 0.72f), style = stroke)
                drawLine(tint, Offset(size.width * 0.3f, size.height * 0.7f), Offset(size.width * 0.55f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.55f, size.height * 0.5f), Offset(size.width * 0.72f, size.height * 0.7f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.52f, size.height * 0.36f), Offset(size.width * 0.74f, size.height * 0.36f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.58f, size.height * 0.36f), Offset(size.width * 0.5f, size.height * 0.2f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            TrackingIconKind.Box -> {
                val box = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.16f)
                    lineTo(size.width * 0.82f, size.height * 0.34f)
                    lineTo(size.width * 0.82f, size.height * 0.68f)
                    lineTo(size.width * 0.5f, size.height * 0.86f)
                    lineTo(size.width * 0.18f, size.height * 0.68f)
                    lineTo(size.width * 0.18f, size.height * 0.34f)
                    close()
                }
                drawPath(box, tint, style = stroke)
                drawLine(tint, Offset(size.width * 0.18f, size.height * 0.34f), Offset(size.width * 0.5f, size.height * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.82f, size.height * 0.34f), Offset(size.width * 0.5f, size.height * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.52f), Offset(size.width * 0.5f, size.height * 0.86f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            TrackingIconKind.Clock -> {
                drawCircle(tint, size.minDimension * 0.36f, Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.28f), Offset(size.width * 0.5f, size.height * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.52f), Offset(size.width * 0.66f, size.height * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            TrackingIconKind.Pin -> {
                drawCircle(tint, size.minDimension * 0.34f, Offset(size.width * 0.5f, size.height * 0.4f), style = stroke)
                drawCircle(tint, size.minDimension * 0.08f, Offset(size.width * 0.5f, size.height * 0.4f))
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.72f), Offset(size.width * 0.5f, size.height * 0.9f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            TrackingIconKind.Cancel -> {
                drawCircle(tint, size.minDimension * 0.36f, Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
                drawLine(tint, Offset(size.width * 0.34f, size.height * 0.34f), Offset(size.width * 0.66f, size.height * 0.66f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.66f, size.height * 0.34f), Offset(size.width * 0.34f, size.height * 0.66f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            TrackingIconKind.Warning -> {
                val triangle = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.16f)
                    lineTo(size.width * 0.85f, size.height * 0.78f)
                    lineTo(size.width * 0.15f, size.height * 0.78f)
                    close()
                }
                drawPath(triangle, tint, style = stroke)
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.36f), Offset(size.width * 0.5f, size.height * 0.58f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawCircle(tint, size.minDimension * 0.04f, Offset(size.width * 0.5f, size.height * 0.68f))
            }
            TrackingIconKind.Copy -> {
                drawRoundRect(tint, Offset(size.width * 0.28f, size.height * 0.24f), Size(size.width * 0.44f, size.height * 0.5f), CornerRadius(size.width * 0.08f), style = stroke)
                drawRoundRect(tint, Offset(size.width * 0.14f, size.height * 0.12f), Size(size.width * 0.44f, size.height * 0.5f), CornerRadius(size.width * 0.08f), style = stroke)
            }
        }
    }
}
