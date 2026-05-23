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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class SubcategoryFilter(val label: String) {
    Nearby("Más cercanos"),
    Rated("Mejor puntuados"),
    Fast("Entrega rápida"),
}

private data class RelatedStore(
    val name: String,
    val description: String,
    val rating: String,
    val reviews: String,
    val distance: String,
    val eta: String,
    val delivery: String,
    val verified: Boolean = false,
)

private val pizzaStores = listOf(
    RelatedStore("Pizzería Roma", "Pizza a la piedra", "4.8", "1.245", "0,8 km", "20-30 min", "$1.200", verified = true),
    RelatedStore("La Esquina Pizzería", "Pizza al molde y a la piedra", "4.6", "829", "1,2 km", "25-35 min", "$1.300"),
    RelatedStore("Pizza & Co.", "Pizzas artesanales", "4.7", "1.003", "1,5 km", "30-40 min", "$1.200"),
    RelatedStore("Don Pietro Pizzería", "Tradición italiana desde 1990", "4.6", "743", "1,0 km", "20-30 min", "$1.100"),
    RelatedStore("Napoli Pizza", "Estilo napolitano auténtico", "4.7", "954", "1,3 km", "25-35 min", "$1.200"),
    RelatedStore("Masa Madre", "Pizzas de horno y focaccias", "4.5", "612", "1,7 km", "30-45 min", "$1.450"),
)

@Composable
fun PublicShopSubcategoryScreen(
    title: String,
    onHome: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
) {
    var selectedFilter by remember { mutableStateOf(SubcategoryFilter.Nearby) }
    var statusMessage by remember { mutableStateOf("Elegir un local se habilitara en una fase posterior.") }

    PublicShell(
        current = PublicBottomDestination.Shop,
        onHome = onHome,
        onPlus = onPlus,
        onShop = onShop,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(PediloBg),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SubcategoryHeader(
                    title = title,
                )
            }
            item {
                FilterRow(
                    selected = selectedFilter,
                    onSelected = {
                        selectedFilter = it
                        statusMessage = "Filtro ${it.label} aplicado localmente."
                    },
                )
            }
            item {
                Text(
                    text = "${pizzaStores.size} locales encontrados",
                    color = PediloMuted,
                    fontSize = 16.sp,
                )
            }
            items(pizzaStores) { store ->
                RelatedStoreCard(
                    store = store,
                    onView = { statusMessage = "Ver local se construirá en la fase de Local público." },
                )
            }
            item {
                Text(
                    text = statusMessage,
                    color = PediloMuted,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PediloPanel, RoundedCornerShape(10.dp))
                        .border(1.dp, PediloLine, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                )
            }
        }
    }
}

@Composable
private fun SubcategoryHeader(
    title: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = PediloText,
                fontSize = 30.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Locales relacionados con esta categoría",
                color = PediloMuted,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun FilterRow(
    selected: SubcategoryFilter,
    onSelected: (SubcategoryFilter) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SubcategoryFilter.entries.forEach { filter ->
            val active = selected == filter
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(
                        if (active) Brush.verticalGradient(listOf(PediloOrangeSoft, PediloOrange)) else Brush.verticalGradient(listOf(PediloPanel, PediloPanel)),
                        RoundedCornerShape(22.dp),
                    )
                    .border(1.dp, if (active) PediloOrange else PediloLine, RoundedCornerShape(22.dp))
                    .clickable(role = Role.Button) { onSelected(filter) }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = filter.label,
                    color = if (active) Color.White else PediloMuted,
                    fontSize = 11.sp,
                    lineHeight = 12.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RelatedStoreCard(
    store: RelatedStore,
    onView: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(174.dp)
            .background(PediloOverlay, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StoreThumbnail(store.name)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (store.verified) {
                    VerifiedBadge()
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = store.name,
                    color = PediloText,
                    fontSize = 16.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = store.description,
                color = PediloMuted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SubcategoryIcon(SubcategoryIconKind.Star, tint = PediloOrange, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text(store.rating, color = PediloOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Text("(${store.reviews})", color = PediloMuted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SubcategoryIcon(SubcategoryIconKind.Pin, tint = PediloMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(store.distance, color = PediloMuted, fontSize = 12.sp)
                Text("  ·  ", color = PediloMuted, fontSize = 12.sp)
                SubcategoryIcon(SubcategoryIconKind.Clock, tint = PediloMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(store.eta, color = PediloMuted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SubcategoryIcon(SubcategoryIconKind.Delivery, tint = PediloOrange, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(5.dp))
                Text("Envio ${store.delivery}", color = PediloMuted, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .width(100.dp)
                        .background(Brush.verticalGradient(listOf(PediloOrangeSoft, PediloOrange)), RoundedCornerShape(10.dp))
                        .clickable(role = Role.Button, onClick = onView),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Ver local", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun StoreThumbnail(seed: String) {
    val accent = if (seed.length % 2 == 0) PediloOrangeDark else PediloOrange
    Box(
        modifier = Modifier
            .size(width = 92.dp, height = 112.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.radialGradient(listOf(PediloWarning, accent, PediloPanelSoft))),
        contentAlignment = Alignment.Center,
    ) {
        SubcategoryIcon(SubcategoryIconKind.Pizza, tint = Color.White, modifier = Modifier.size(58.dp))
    }
}

@Composable
private fun VerifiedBadge() {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(PediloOrange, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        SubcategoryIcon(SubcategoryIconKind.Check, tint = Color.White, modifier = Modifier.size(15.dp))
    }
}

private enum class SubcategoryIconKind {
    Star,
    Pin,
    Clock,
    Delivery,
    Chevron,
    Pizza,
    Check,
}

@Composable
private fun SubcategoryIcon(
    kind: SubcategoryIconKind,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = size.minDimension * 0.1f, cap = StrokeCap.Round)
        when (kind) {
            SubcategoryIconKind.Star -> {
                val star = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.16f)
                    lineTo(size.width * 0.6f, size.height * 0.42f)
                    lineTo(size.width * 0.86f, size.height * 0.44f)
                    lineTo(size.width * 0.66f, size.height * 0.6f)
                    lineTo(size.width * 0.72f, size.height * 0.86f)
                    lineTo(size.width * 0.5f, size.height * 0.72f)
                    lineTo(size.width * 0.28f, size.height * 0.86f)
                    lineTo(size.width * 0.34f, size.height * 0.6f)
                    lineTo(size.width * 0.14f, size.height * 0.44f)
                    lineTo(size.width * 0.4f, size.height * 0.42f)
                    close()
                }
                drawPath(star, tint)
            }
            SubcategoryIconKind.Pin -> {
                drawCircle(tint, size.minDimension * 0.24f, Offset(size.width * 0.5f, size.height * 0.38f), style = stroke)
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.64f), Offset(size.width * 0.5f, size.height * 0.88f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            SubcategoryIconKind.Clock -> {
                drawCircle(tint, size.minDimension * 0.36f, Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.3f), Offset(size.width * 0.5f, size.height * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.52f), Offset(size.width * 0.66f, size.height * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            SubcategoryIconKind.Delivery -> {
                drawRoundRect(tint, Offset(size.width * 0.18f, size.height * 0.42f), Size(size.width * 0.45f, size.height * 0.26f), CornerRadius(size.width * 0.05f), style = stroke)
                drawLine(tint, Offset(size.width * 0.62f, size.height * 0.5f), Offset(size.width * 0.78f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawCircle(tint, size.minDimension * 0.06f, Offset(size.width * 0.34f, size.height * 0.78f))
                drawCircle(tint, size.minDimension * 0.06f, Offset(size.width * 0.7f, size.height * 0.78f))
            }
            SubcategoryIconKind.Chevron -> {
                drawLine(tint, Offset(size.width * 0.35f, size.height * 0.2f), Offset(size.width * 0.65f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.65f, size.height * 0.5f), Offset(size.width * 0.35f, size.height * 0.8f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            SubcategoryIconKind.Pizza -> {
                val slice = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.18f)
                    lineTo(size.width * 0.82f, size.height * 0.34f)
                    lineTo(size.width * 0.42f, size.height * 0.86f)
                    close()
                }
                drawPath(slice, tint, style = stroke)
                drawCircle(tint, size.minDimension * 0.05f, Offset(size.width * 0.48f, size.height * 0.44f))
                drawCircle(tint, size.minDimension * 0.05f, Offset(size.width * 0.58f, size.height * 0.58f))
            }
            SubcategoryIconKind.Check -> {
                drawLine(tint, Offset(size.width * 0.25f, size.height * 0.52f), Offset(size.width * 0.43f, size.height * 0.7f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.43f, size.height * 0.7f), Offset(size.width * 0.78f, size.height * 0.3f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
        }
    }
}
