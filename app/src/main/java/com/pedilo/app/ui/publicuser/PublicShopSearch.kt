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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class SearchFilter(val label: String) {
    Nearby("Más cercanos"),
    Rated("Mejor puntuados"),
    Fast("Entrega rápida"),
}

private data class SearchStore(
    val name: String,
    val description: String,
    val rating: String,
    val reviews: String,
    val distance: String,
    val eta: String,
    val delivery: String,
    val badge: String? = null,
    val icon: SearchIconKind = SearchIconKind.Shop,
)

private val pizzaSearchStores = listOf(
    SearchStore("Pizzería Roma", "Pizza a la piedra", "4.6", "2.451", "0.8 km", "20-30 min", "$1200", "10% OFF", SearchIconKind.Pizza),
    SearchStore("La Esquina Pizzería", "Pizza al molde y a la piedra", "4.6", "820", "1.2 km", "25-35 min", "$1300", icon = SearchIconKind.Pizza),
    SearchStore("Pizza & Co.", "Pizzas artesanales", "4.7", "1.050", "1.5 km", "30-40 min", "$1200", icon = SearchIconKind.Pizza),
    SearchStore("Don Pietro Pizzería", "Tradición italiana desde 1990", "4.5", "743", "1.0 km", "20-30 min", "$1.100", icon = SearchIconKind.Pizza),
    SearchStore("Napoli Pizza", "Estilo napolitano auténtico", "4.7", "591", "1.3 km", "25-35 min", "$1200", "TOP", SearchIconKind.Pizza),
    SearchStore("La Nonna Pizzas", "Recetas caseras desde 1985", "4.4", "612", "1.8 km", "30-45 min", "$1.350", icon = SearchIconKind.Pizza),
)

private val coherentStores = mapOf(
    "hamburguesas" to listOf(
        SearchStore("Burger House", "Hamburguesas clásicas", "4.7", "1.208", "0.9 km", "20-30 min", "$1.100", "20% OFF"),
        SearchStore("Big Burger", "Combos y papas", "4.5", "684", "1.4 km", "25-35 min", "$1.250"),
    ),
    "farmacia" to listOf(
        SearchStore("Farmacia Central", "Farmacia y perfumería", "4.8", "932", "0.7 km", "15-25 min", "$900", icon = SearchIconKind.Pharmacy),
        SearchStore("Salud Norte", "Medicamentos y cuidado personal", "4.6", "518", "1.6 km", "20-35 min", "$1.100", icon = SearchIconKind.Pharmacy),
    ),
    "mascotas" to listOf(
        SearchStore("Pet Shop Norte", "Alimentos y accesorios", "4.8", "721", "1.0 km", "20-30 min", "$1.000", icon = SearchIconKind.Paw),
        SearchStore("Veterinaria Patitas", "Cuidado y alimentos", "4.7", "466", "1.8 km", "25-40 min", "$1.200", icon = SearchIconKind.Paw),
    ),
    "bebidas" to listOf(
        SearchStore("Bebidas Express", "Gaseosas, aguas y jugos", "4.6", "690", "0.8 km", "15-25 min", "$900", icon = SearchIconKind.Drink),
        SearchStore("Kiosco La Esquina", "Bebidas frías y snacks", "4.4", "385", "1.1 km", "20-30 min", "$850", icon = SearchIconKind.Drink),
    ),
    "supermercado" to listOf(
        SearchStore("Supermercado Sol", "Almacén y frescos", "4.5", "1.020", "1.2 km", "25-40 min", "$1.300"),
        SearchStore("Almacén Don Luis", "Despensa de barrio", "4.6", "544", "0.6 km", "20-30 min", "$950"),
    ),
    "sushi" to listOf(
        SearchStore("Sushi Zen", "Rolls y piezas combinadas", "4.7", "812", "1.5 km", "30-40 min", "$1.500"),
        SearchStore("Nikkei Club", "Sushi y cocina japonesa", "4.6", "430", "2.1 km", "35-45 min", "$1.700"),
    ),
    "panaderia" to listOf(
        SearchStore("Dulce Hogar", "Panadería y facturas", "4.9", "755", "0.9 km", "20-30 min", "$850"),
        SearchStore("La Espiga", "Panificados artesanales", "4.6", "389", "1.4 km", "25-35 min", "$950"),
    ),
    "panadería" to listOf(
        SearchStore("Dulce Hogar", "Panadería y facturas", "4.9", "755", "0.9 km", "20-30 min", "$850"),
        SearchStore("La Espiga", "Panificados artesanales", "4.6", "389", "1.4 km", "25-35 min", "$950"),
    ),
    "cafeteria" to listOf(
        SearchStore("Café Central", "Café y pastelería", "4.8", "903", "0.7 km", "20-30 min", "$900"),
        SearchStore("Barista Club", "Café de especialidad", "4.7", "612", "1.3 km", "25-35 min", "$1.000"),
    ),
    "cafetería" to listOf(
        SearchStore("Café Central", "Café y pastelería", "4.8", "903", "0.7 km", "20-30 min", "$900"),
        SearchStore("Barista Club", "Café de especialidad", "4.7", "612", "1.3 km", "25-35 min", "$1.000"),
    ),
    "ofertas" to listOf(
        SearchStore("Burger House", "Hamburguesas clásicas", "4.7", "1.208", "0.9 km", "20-30 min", "$1.100", "20% OFF"),
        SearchStore("Pizzería Roma", "Pizzas seleccionadas", "4.6", "2.451", "0.8 km", "20-30 min", "$1200", "15% OFF"),
        SearchStore("Fruta Viva", "Jugos naturales", "4.5", "408", "1.2 km", "20-30 min", "$900", "25% OFF"),
    ),
    "nuevos locales" to listOf(
        SearchStore("Café Central", "Cafetería", "4.8", "903", "0.7 km", "20-30 min", "$900", "Nuevo"),
        SearchStore("Sushi Zen", "Sushi", "4.7", "812", "1.5 km", "30-40 min", "$1.500", "Nuevo"),
        SearchStore("Verde Vivo", "Comida saludable", "4.6", "377", "1.1 km", "20-30 min", "$1.100", "Nuevo"),
    ),
)

private fun storesForQuery(query: String): List<SearchStore> {
    val normalized = query.trim().lowercase()
    if (normalized.isBlank()) return emptyList()
    if (listOf("p", "pi", "piz", "pizz", "pizza", "pizzas").any { normalized == it || "pizzas".startsWith(normalized) }) return pizzaSearchStores
    coherentStores.forEach { (key, stores) ->
        if (key.startsWith(normalized) || normalized.contains(key) || key.contains(normalized)) return stores
    }
    return emptyList()
}

@Composable
fun PublicShopSearchScreen(
    query: String,
    current: PublicBottomDestination = PublicBottomDestination.Shop,
    onHome: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
    onViewLocal: () -> Unit,
    titleOverride: String? = null,
) {
    var selectedFilter by remember { mutableStateOf(SearchFilter.Nearby) }
    var activeQuery by remember(query) { mutableStateOf(query) }
    val hasQuery = activeQuery.isNotBlank()
    val relatedStores = storesForQuery(activeQuery)
    val hasRelatedStores = relatedStores.isNotEmpty()
    val listingMode = titleOverride != null

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
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                SearchHeader(
                    query = activeQuery,
                    titleOverride = titleOverride,
                )
            }
            if (!listingMode) {
                item {
                    ActiveSearchBox(
                        query = activeQuery,
                        onQueryChange = { activeQuery = it },
                        onClear = { activeQuery = "" },
                    )
                }
            }
            if (listingMode || hasQuery) {
                if (hasRelatedStores) {
                    items(relatedStores) { store ->
                        SearchResultCard(
                            store = store,
                            onView = onViewLocal,
                        )
                    }
                } else {
                    item {
                        SearchNoResults()
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHeader(query: String, titleOverride: String?) {
    val hasQuery = query.isNotBlank()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titleOverride ?: if (hasQuery) "Resultado: ${query.trim()}" else "Pédilo!",
                color = PediloText,
                fontSize = 24.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (hasQuery && titleOverride == null) {
                Text(
                    text = "Locales relacionados",
                    color = PediloMuted,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ActiveSearchBox(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(PediloPanel, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .semantics { contentDescription = "Búsqueda activa" }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchIcon(SearchIconKind.Search, tint = PediloText, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            textStyle = TextStyle(color = PediloText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
            singleLine = true,
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "¿Qué estás buscando?",
                        color = PediloMuted,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                innerTextField()
            },
        )
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable(role = Role.Button, onClick = onClear)
                .semantics { contentDescription = "Limpiar búsqueda" },
            contentAlignment = Alignment.Center,
        ) {
            SearchIcon(SearchIconKind.Close, tint = PediloText, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun SearchNoResults() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloOverlay, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .padding(16.dp),
    ) {
        Text(
            text = "No encontramos resultados.",
            color = PediloText,
            fontSize = 19.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SearchFilterRow(
    selected: SearchFilter,
    onSelected: (SearchFilter) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SearchFilter.entries.forEach { filter ->
            val active = selected == filter
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .background(
                        if (active) Brush.verticalGradient(listOf(PediloOrangeSoft, PediloOrange)) else Brush.verticalGradient(listOf(PediloPanel, PediloPanel)),
                        RoundedCornerShape(21.dp),
                    )
                    .border(1.dp, if (active) PediloOrange else PediloLine, RoundedCornerShape(21.dp))
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
private fun SearchResultCard(
    store: SearchStore,
    onView: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(136.dp)
            .background(PediloOverlay, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchStoreThumbnail(store = store)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = store.name,
                color = PediloText,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = store.description,
                color = PediloMuted,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SearchIcon(SearchIconKind.Star, tint = PediloOrange, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text(store.rating, color = PediloOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(5.dp))
                Text("(${store.reviews})", color = PediloMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(" · ${store.eta}", color = PediloMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SearchIcon(SearchIconKind.Delivery, tint = PediloOrange, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(5.dp))
                Text("${store.distance} · Envío ${store.delivery}", color = PediloMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .width(68.dp)
                .height(34.dp)
                .background(Brush.verticalGradient(listOf(PediloOrangeSoft, PediloOrange)), RoundedCornerShape(10.dp))
                .clickable(role = Role.Button, onClick = onView),
            contentAlignment = Alignment.Center,
        ) {
            Text("Ver local", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun SearchStoreThumbnail(store: SearchStore) {
    Box(
        modifier = Modifier
            .size(width = 78.dp, height = 108.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(
                Brush.radialGradient(
                    listOf(PediloWarning, PediloOrangeDark, PediloPanelSoft),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        SearchIcon(store.icon, tint = Color.White, modifier = Modifier.size(62.dp))
        store.badge?.let { badge ->
            Text(
                text = badge,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .background(PediloOrange, RoundedCornerShape(10.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            )
        }
    }
}

private enum class SearchIconKind {
    Search,
    Close,
    Star,
    Delivery,
    Pizza,
    Shop,
    Paw,
    Pharmacy,
    Drink,
}

@Composable
private fun SearchIcon(
    kind: SearchIconKind,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = size.minDimension * 0.1f, cap = StrokeCap.Round)
        when (kind) {
            SearchIconKind.Search -> {
                drawCircle(tint, size.minDimension * 0.28f, Offset(size.width * 0.42f, size.height * 0.42f), style = stroke)
                drawLine(tint, Offset(size.width * 0.62f, size.height * 0.62f), Offset(size.width * 0.82f, size.height * 0.82f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            SearchIconKind.Close -> {
                drawLine(tint, Offset(size.width * 0.25f, size.height * 0.25f), Offset(size.width * 0.75f, size.height * 0.75f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.75f, size.height * 0.25f), Offset(size.width * 0.25f, size.height * 0.75f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            SearchIconKind.Star -> {
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
            SearchIconKind.Delivery -> {
                drawRoundRect(tint, Offset(size.width * 0.18f, size.height * 0.42f), Size(size.width * 0.45f, size.height * 0.26f), CornerRadius(size.width * 0.05f), style = stroke)
                drawLine(tint, Offset(size.width * 0.62f, size.height * 0.5f), Offset(size.width * 0.78f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawCircle(tint, size.minDimension * 0.06f, Offset(size.width * 0.34f, size.height * 0.78f))
                drawCircle(tint, size.minDimension * 0.06f, Offset(size.width * 0.7f, size.height * 0.78f))
            }
            SearchIconKind.Pizza -> {
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
            SearchIconKind.Shop -> {
                drawRoundRect(tint, Offset(size.width * 0.18f, size.height * 0.36f), Size(size.width * 0.64f, size.height * 0.46f), CornerRadius(size.width * 0.06f), style = stroke)
                drawLine(tint, Offset(size.width * 0.22f, size.height * 0.36f), Offset(size.width * 0.32f, size.height * 0.18f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.78f, size.height * 0.36f), Offset(size.width * 0.68f, size.height * 0.18f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.32f, size.height * 0.18f), Offset(size.width * 0.68f, size.height * 0.18f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            SearchIconKind.Paw -> {
                drawCircle(tint, size.minDimension * 0.11f, Offset(size.width * 0.5f, size.height * 0.62f))
                drawCircle(tint, size.minDimension * 0.07f, Offset(size.width * 0.32f, size.height * 0.42f))
                drawCircle(tint, size.minDimension * 0.07f, Offset(size.width * 0.46f, size.height * 0.32f))
                drawCircle(tint, size.minDimension * 0.07f, Offset(size.width * 0.6f, size.height * 0.32f))
                drawCircle(tint, size.minDimension * 0.07f, Offset(size.width * 0.74f, size.height * 0.42f))
            }
            SearchIconKind.Pharmacy -> {
                drawRoundRect(tint, Offset(size.width * 0.2f, size.height * 0.24f), Size(size.width * 0.6f, size.height * 0.56f), CornerRadius(size.width * 0.08f), style = stroke)
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.36f), Offset(size.width * 0.5f, size.height * 0.68f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.34f, size.height * 0.52f), Offset(size.width * 0.66f, size.height * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            SearchIconKind.Drink -> {
                drawRoundRect(tint, Offset(size.width * 0.34f, size.height * 0.18f), Size(size.width * 0.32f, size.height * 0.66f), CornerRadius(size.width * 0.08f), style = stroke)
                drawLine(tint, Offset(size.width * 0.42f, size.height * 0.18f), Offset(size.width * 0.42f, size.height * 0.08f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.42f, size.height * 0.08f), Offset(size.width * 0.66f, size.height * 0.08f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.38f, size.height * 0.52f), Offset(size.width * 0.62f, size.height * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
        }
    }
}
