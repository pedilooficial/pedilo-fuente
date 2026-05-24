package com.pedilo.app.ui.publicuser

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class PublicBottomDestination {
    Home,
    Plus,
    Shop,
}

private enum class PediloIconKind {
    Home,
    Plus,
    Shop,
    Search,
    Cart,
    Bottle,
    Pill,
    Paw,
    Tag,
    Star,
    Clock,
    Megaphone,
    Info,
}

private data class QuickAccess(
    val title: String,
    val icon: PediloIconKind,
    val query: String,
)

private data class OfferItem(
    val title: String,
    val store: String,
    val price: String,
    val oldPrice: String,
    val discount: String,
    val icon: PediloIconKind,
)

private data class LocalItem(
    val name: String,
    val category: String,
    val rating: String,
    val eta: String,
    val icon: PediloIconKind,
)

private val quickAccessItems = listOf(
    QuickAccess("Supermercado", PediloIconKind.Cart, "Supermercado"),
    QuickAccess("Bebidas", PediloIconKind.Bottle, "Bebidas"),
    QuickAccess("Farmacia", PediloIconKind.Pill, "Farmacia"),
    QuickAccess("Mascotas", PediloIconKind.Paw, "Mascotas"),
)

private val offerItems = listOf(
    OfferItem("Hamburguesas Clásicas", "Burger House", "$4.990", "$6.290", "-20%", PediloIconKind.Cart),
    OfferItem("Pizzas Seleccionadas", "Pizzería Roma", "$5.990", "$7.990", "-15%", PediloIconKind.Tag),
    OfferItem("Empanadas x 6", "La Criolla", "$3.590", "$3.990", "-10%", PediloIconKind.Tag),
    OfferItem("Jugos Naturales", "Fruta Viva", "$2.240", "$2.990", "-25%", PediloIconKind.Bottle),
)

private val localItems = listOf(
    LocalItem("Café Central", "Cafetería", "4,8", "20-30 min", PediloIconKind.Shop),
    LocalItem("Sushi Zen", "Sushi", "4,7", "30-40 min", PediloIconKind.Tag),
    LocalItem("Verde Vivo", "Saludable", "4,6", "20-30 min", PediloIconKind.Paw),
    LocalItem("Dulce Hogar", "Panadería", "4,9", "20-30 min", PediloIconKind.Shop),
)

@Composable
fun PublicHomeScreen(
    onHome: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
    onSearch: () -> Unit,
    onConventions: () -> Unit,
    onCategory: (String) -> Unit,
    onOffer: (String) -> Unit,
    onAllOffers: () -> Unit,
    onLocal: (String) -> Unit,
    onAllLocals: () -> Unit,
) {
    PublicShell(
        current = PublicBottomDestination.Home,
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
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            item { PublicHeader() }
            item { SearchBlock(onSearch = onSearch) }
            item { QuickAccessSection(onCategory = onCategory) }
            item { OffersSection(onOffer = onOffer, onAllOffers = onAllOffers) }
            item { LocalsSection(onLocal = onLocal, onAllLocals = onAllLocals) }
            item { HomeBanner() }
            item { ConventionsSection(onConventions = onConventions) }
        }
    }
}

@Composable
fun PublicShell(
    current: PublicBottomDestination,
    onHome: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PediloBg),
    ) {
        content()
        PublicBottomBar(
            current = current,
            onHome = onHome,
            onPlus = onPlus,
            onShop = onShop,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun PublicHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Pédilo!",
                color = PediloOrange,
                fontSize = 44.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "todos tus pedidos en un solo lugar",
                color = PediloText,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            modifier = Modifier.padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PediloLineIcon(PediloIconKind.Info, tint = PediloOrange, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(4.dp))
            Text("Ayuda", color = PediloOrange, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SearchBlock(onSearch: () -> Unit) {
    SurfacePanel {
        Text(
            text = "¿Qué necesitás?",
            color = PediloText,
            fontSize = 20.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        SearchInput(onSearch = onSearch)
    }
}

@Composable
private fun SearchInput(onSearch: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(PediloPanelSoft, RoundedCornerShape(12.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onSearch)
            .semantics { contentDescription = "Buscar desde Home" }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PediloLineIcon(PediloIconKind.Search, tint = PediloMuted, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f)) {
            Text(
                text = "Buscar productos, locales o categorías...",
                color = PediloMuted,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun QuickAccessSection(onCategory: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        quickAccessItems.forEach { item ->
            QuickAccessCard(item, onClick = { onCategory(item.query) }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickAccessCard(item: QuickAccess, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(78.dp)
            .background(PediloPanel, RoundedCornerShape(10.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(10.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = item.title }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PediloLineIcon(item.icon, tint = PediloOrange, modifier = Modifier.size(25.dp))
        Spacer(Modifier.height(5.dp))
        Text(
            text = item.title,
            color = PediloText,
            fontSize = 10.sp,
            lineHeight = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Clip,
        )
    }
}

@Composable
private fun OffersSection(onOffer: (String) -> Unit, onAllOffers: () -> Unit) {
    SectionHeader(icon = PediloIconKind.Tag, title = "Ofertas", action = "Ver todas", onAction = onAllOffers)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(offerItems) { offer ->
            OfferCard(offer, onClick = { onOffer(offer.store) })
        }
    }
}

@Composable
private fun OfferCard(offer: OfferItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(118.dp)
            .height(160.dp)
            .background(PediloPanel, RoundedCornerShape(10.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(10.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(7.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.radialGradient(listOf(PediloOrangeDark, PediloPanelSoft))),
        ) {
            PediloLineIcon(
                offer.icon,
                tint = PediloWarning,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(34.dp),
            )
            Text(
                text = offer.discount,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(PediloOrange, RoundedCornerShape(5.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(offer.title, color = PediloText, fontSize = 11.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(offer.store, color = PediloMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(offer.price, color = PediloOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(4.dp))
            Text(offer.oldPrice, color = PediloMuted, fontSize = 9.sp)
        }
    }
}

@Composable
private fun LocalsSection(onLocal: (String) -> Unit, onAllLocals: () -> Unit) {
    SectionHeader(icon = PediloIconKind.Shop, title = "Nuevos locales", action = "Ver todos", onAction = onAllLocals)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(localItems) { local ->
            LocalCard(local, onClick = { onLocal(local.name) })
        }
    }
}

@Composable
private fun LocalCard(local: LocalItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(132.dp)
            .height(142.dp)
            .background(PediloPanel, RoundedCornerShape(10.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(10.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(7.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.linearGradient(listOf(PediloPanelSoft, PediloOrangeDark))),
        ) {
            PediloLineIcon(local.icon, tint = PediloWarning, modifier = Modifier.align(Alignment.Center).size(34.dp))
            Text(
                text = "Nuevo",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(PediloOrange, RoundedCornerShape(5.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(local.name, color = PediloText, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(local.category, color = PediloMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            PediloLineIcon(PediloIconKind.Star, tint = PediloOrange, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(3.dp))
            Text("${local.rating} · ${local.eta}", color = PediloMuted, fontSize = 9.sp, maxLines = 1)
        }
    }
}

@Composable
private fun HomeBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .background(Brush.horizontalGradient(listOf(PediloOrangeDark, PediloPanel)), RoundedCornerShape(12.dp))
            .border(1.dp, PediloOrange, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Column(modifier = Modifier.align(Alignment.CenterStart).fillMaxWidth().padding(end = 82.dp)) {
            Text("¡Envíos más rápidos!", color = Color.White, fontSize = 18.sp, lineHeight = 20.sp, fontWeight = FontWeight.ExtraBold)
            Text("Tus locales favoritos, ahora más cerca que nunca.", color = Color.White, fontSize = 11.sp, lineHeight = 14.sp)
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Ver más",
                color = PediloOrangeDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(18.dp))
                    .padding(horizontal = 18.dp, vertical = 7.dp),
            )
        }
        PediloLineIcon(PediloIconKind.Megaphone, tint = PediloWarning, modifier = Modifier.align(Alignment.CenterEnd).size(54.dp))
    }
}

@Composable
private fun ConventionsSection(onConventions: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloOverlay, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onConventions)
            .semantics { contentDescription = "Abrir Convenciones" }
            .padding(14.dp),
    ) {
        Text("Convenciones", color = PediloText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        ConventionRow(label = "Indicador de novedad", badge = "Nuevo")
        ConventionRow(label = "Descuento / Promoción", badge = "-20%")
        ConventionIconRow(label = "Calificación del local", icon = PediloIconKind.Star)
        ConventionIconRow(label = "Tiempo estimado", icon = PediloIconKind.Clock)
    }
}

@Composable
private fun ConventionRow(label: String, badge: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = badge,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .width(58.dp)
                .background(PediloOrange, RoundedCornerShape(5.dp))
                .padding(vertical = 4.dp),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.width(12.dp))
        Text(label, color = PediloMuted, fontSize = 13.sp)
    }
}

@Composable
private fun ConventionIconRow(label: String, icon: PediloIconKind) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(58.dp), contentAlignment = Alignment.Center) {
            PediloLineIcon(icon, tint = PediloOrange, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(label, color = PediloMuted, fontSize = 13.sp)
    }
}

@Composable
private fun SectionHeader(icon: PediloIconKind, title: String, action: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PediloLineIcon(icon, tint = PediloOrange, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, color = PediloText, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.clickable(role = Role.Button, onClick = onAction),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(action, color = PediloOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(" >", color = PediloOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SurfacePanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloOverlay, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .padding(14.dp),
        content = content,
    )
}

@Composable
private fun PublicBottomBar(
    current: PublicBottomDestination,
    onHome: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .background(PediloPanel, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .border(1.dp, PediloLine, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .padding(horizontal = 42.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomItem(
                icon = PediloIconKind.Home,
                label = "Inicio",
                selected = current == PublicBottomDestination.Home,
                onClick = onHome,
            )
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(PediloOrangeSoft, PediloOrange)))
                    .clickable(role = Role.Button, onClick = onPlus)
                    .semantics { contentDescription = "Abrir boton mas" },
                contentAlignment = Alignment.Center,
            ) {
                PediloLineIcon(PediloIconKind.Plus, tint = Color.White, modifier = Modifier.size(31.dp))
            }
            BottomItem(
                icon = PediloIconKind.Shop,
                label = "Tienda",
                selected = current == PublicBottomDestination.Shop,
                onClick = onShop,
            )
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .background(PediloBg),
        )
    }
}

@Composable
private fun BottomItem(
    icon: PediloIconKind,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PediloLineIcon(icon, tint = if (selected) PediloOrange else PediloMuted, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            color = if (selected) PediloOrange else PediloMuted,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}

@Composable
private fun PediloLineIcon(
    kind: PediloIconKind,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = size.minDimension * 0.09f, cap = StrokeCap.Round)
        when (kind) {
            PediloIconKind.Home -> {
                val roof = Path().apply {
                    moveTo(size.width * 0.15f, size.height * 0.48f)
                    lineTo(size.width * 0.5f, size.height * 0.18f)
                    lineTo(size.width * 0.85f, size.height * 0.48f)
                }
                drawPath(roof, tint, style = stroke)
                drawRoundRect(tint, Offset(size.width * 0.25f, size.height * 0.46f), Size(size.width * 0.5f, size.height * 0.38f), CornerRadius(size.width * 0.05f), style = stroke)
            }
            PediloIconKind.Plus -> {
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.2f), Offset(size.width * 0.5f, size.height * 0.8f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.2f, size.height * 0.5f), Offset(size.width * 0.8f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            PediloIconKind.Shop -> {
                drawRoundRect(tint, Offset(size.width * 0.2f, size.height * 0.42f), Size(size.width * 0.6f, size.height * 0.42f), CornerRadius(size.width * 0.05f), style = stroke)
                drawLine(tint, Offset(size.width * 0.18f, size.height * 0.38f), Offset(size.width * 0.82f, size.height * 0.38f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.28f, size.height * 0.18f), Offset(size.width * 0.18f, size.height * 0.38f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.72f, size.height * 0.18f), Offset(size.width * 0.82f, size.height * 0.38f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.18f), Offset(size.width * 0.5f, size.height * 0.38f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            PediloIconKind.Search -> {
                drawCircle(tint, radius = size.minDimension * 0.27f, center = Offset(size.width * 0.43f, size.height * 0.43f), style = stroke)
                drawLine(tint, Offset(size.width * 0.62f, size.height * 0.62f), Offset(size.width * 0.84f, size.height * 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            PediloIconKind.Cart -> {
                drawLine(tint, Offset(size.width * 0.18f, size.height * 0.25f), Offset(size.width * 0.3f, size.height * 0.25f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawRoundRect(tint, Offset(size.width * 0.32f, size.height * 0.34f), Size(size.width * 0.48f, size.height * 0.28f), CornerRadius(size.width * 0.04f), style = stroke)
                drawCircle(tint, size.minDimension * 0.06f, Offset(size.width * 0.42f, size.height * 0.78f))
                drawCircle(tint, size.minDimension * 0.06f, Offset(size.width * 0.72f, size.height * 0.78f))
            }
            PediloIconKind.Bottle -> {
                drawRoundRect(tint, Offset(size.width * 0.36f, size.height * 0.28f), Size(size.width * 0.28f, size.height * 0.56f), CornerRadius(size.width * 0.09f), style = stroke)
                drawLine(tint, Offset(size.width * 0.42f, size.height * 0.18f), Offset(size.width * 0.58f, size.height * 0.18f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.45f, size.height * 0.18f), Offset(size.width * 0.45f, size.height * 0.28f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.55f, size.height * 0.18f), Offset(size.width * 0.55f, size.height * 0.28f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            PediloIconKind.Pill -> {
                drawRoundRect(tint, Offset(size.width * 0.22f, size.height * 0.35f), Size(size.width * 0.56f, size.height * 0.3f), CornerRadius(size.width * 0.16f), style = stroke)
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.35f), Offset(size.width * 0.5f, size.height * 0.65f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            PediloIconKind.Paw -> {
                drawCircle(tint, size.minDimension * 0.12f, Offset(size.width * 0.5f, size.height * 0.58f))
                drawCircle(tint, size.minDimension * 0.07f, Offset(size.width * 0.32f, size.height * 0.38f))
                drawCircle(tint, size.minDimension * 0.07f, Offset(size.width * 0.45f, size.height * 0.28f))
                drawCircle(tint, size.minDimension * 0.07f, Offset(size.width * 0.58f, size.height * 0.28f))
                drawCircle(tint, size.minDimension * 0.07f, Offset(size.width * 0.7f, size.height * 0.38f))
            }
            PediloIconKind.Tag -> {
                val tag = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.32f)
                    lineTo(size.width * 0.5f, size.height * 0.18f)
                    lineTo(size.width * 0.82f, size.height * 0.5f)
                    lineTo(size.width * 0.5f, size.height * 0.82f)
                    close()
                }
                drawPath(tag, tint, style = stroke)
                drawCircle(tint, size.minDimension * 0.04f, Offset(size.width * 0.47f, size.height * 0.37f))
            }
            PediloIconKind.Star -> {
                val star = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.18f)
                    lineTo(size.width * 0.6f, size.height * 0.42f)
                    lineTo(size.width * 0.85f, size.height * 0.44f)
                    lineTo(size.width * 0.66f, size.height * 0.6f)
                    lineTo(size.width * 0.72f, size.height * 0.84f)
                    lineTo(size.width * 0.5f, size.height * 0.7f)
                    lineTo(size.width * 0.28f, size.height * 0.84f)
                    lineTo(size.width * 0.34f, size.height * 0.6f)
                    lineTo(size.width * 0.15f, size.height * 0.44f)
                    lineTo(size.width * 0.4f, size.height * 0.42f)
                    close()
                }
                drawPath(star, tint)
            }
            PediloIconKind.Clock -> {
                drawCircle(tint, size.minDimension * 0.34f, Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.32f), Offset(size.width * 0.5f, size.height * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.52f), Offset(size.width * 0.64f, size.height * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            PediloIconKind.Megaphone -> {
                val horn = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.45f)
                    lineTo(size.width * 0.68f, size.height * 0.22f)
                    lineTo(size.width * 0.68f, size.height * 0.78f)
                    lineTo(size.width * 0.2f, size.height * 0.55f)
                    close()
                }
                drawPath(horn, tint, style = stroke)
                drawLine(tint, Offset(size.width * 0.32f, size.height * 0.58f), Offset(size.width * 0.42f, size.height * 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.78f, size.height * 0.36f), Offset(size.width * 0.9f, size.height * 0.28f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.8f, size.height * 0.5f), Offset(size.width * 0.94f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.78f, size.height * 0.64f), Offset(size.width * 0.9f, size.height * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            PediloIconKind.Info -> {
                drawCircle(tint, size.minDimension * 0.38f, Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
                drawCircle(tint, size.minDimension * 0.04f, Offset(size.width * 0.5f, size.height * 0.34f))
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.48f), Offset(size.width * 0.5f, size.height * 0.68f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
        }
    }
}
