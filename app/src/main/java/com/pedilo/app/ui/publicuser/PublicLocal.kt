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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.pedilo.app.core.model.PublicProductSummary
import com.pedilo.app.core.model.PublicStoreSummary

enum class LocalCategory(val label: String) {
    Featured("Destacados"),
    Pizzas("Productos"),
    Starters("Entradas"),
    Drinks("Bebidas"),
}

data class LocalProduct(
    val name: String,
    val description: String,
    val price: Int,
    val category: LocalCategory,
    val badge: String = "",
)

data class LocalCartItem(
    val product: LocalProduct,
    val size: String,
    val extras: List<String>,
    val quantity: Int,
    val notes: String,
)

data class LocalOrderData(
    val fullName: String,
    val phone: String,
    val address: String,
    val payment: String,
    val notes: String,
)

private enum class LocalIconKind {
    Pizza,
    Star,
    Clock,
    Delivery,
    Cart,
    Plus,
    Minus,
    Check,
    Ticket,
    Tracking,
    Person,
    Location,
}

private val localProducts = listOf(
    LocalProduct("Pizza muzzarella", "Salsa de tomate, muzzarella y aceitunas", 6200, LocalCategory.Pizzas, "Más pedida"),
    LocalProduct("Pizza napolitana", "Tomate fresco, ajo y muzzarella", 6900, LocalCategory.Pizzas),
    LocalProduct("Fugazzeta rellena", "Cebolla, queso y masa al molde", 7600, LocalCategory.Pizzas, "Especial"),
    LocalProduct("Empanadas x 6", "Carne suave, jamón y queso o verdura", 4300, LocalCategory.Starters),
    LocalProduct("Papas rústicas", "Con salsa de la casa", 3100, LocalCategory.Starters),
    LocalProduct("Gaseosa 1.5L", "Línea Coca-Cola o similar", 2400, LocalCategory.Drinks),
    LocalProduct("Agua saborizada", "Pomelo o manzana", 1700, LocalCategory.Drinks),
)

private fun productsFor(category: LocalCategory): List<LocalProduct> =
    when (category) {
        LocalCategory.Featured -> localProducts.take(4)
        else -> localProducts.filter { it.category == category }
    }

private fun productsFor(category: LocalCategory, catalogState: PublicCatalogState): List<LocalProduct> {
    val realProducts = catalogState.productsByStore["pizzeria-roma"].orEmpty()
        .filter { it.visible && it.available && it.priceCents != null }
        .map { it.toLocalProduct() }
    if (realProducts.isEmpty()) return productsFor(category)
    return when (category) {
        LocalCategory.Featured -> realProducts.take(4)
        else -> realProducts.filter { it.category == category }
    }.ifEmpty { realProducts }
}

private fun PublicProductSummary.toLocalProduct(): LocalProduct =
    LocalProduct(
        name = name,
        description = description,
        price = ((priceCents ?: 0L) / 100L).toInt(),
        category = when {
            name.contains("gaseosa", ignoreCase = true) || name.contains("bebida", ignoreCase = true) -> LocalCategory.Drinks
            name.contains("empanada", ignoreCase = true) -> LocalCategory.Starters
            else -> LocalCategory.Pizzas
        },
        badge = if (name.contains("promo", ignoreCase = true)) "Especial" else "",
    )

private fun PublicCatalogState.romaStore(): PublicStoreSummary? =
    stores.firstOrNull { it.id == "pizzeria-roma" } ?: stores.firstOrNull()

fun localCartTotal(items: List<LocalCartItem>): Int = items.sumOf { item ->
    val extras = item.extras.size * 450
    val size = if (item.size == "Grande") 1200 else 0
    (item.product.price + extras + size) * item.quantity
}

fun localDeliveryCost(): Int = 1200

fun localGrandTotal(items: List<LocalCartItem>): Int = localCartTotal(items) + localDeliveryCost()

fun formatLocalMoney(value: Int): String = "$" + "%,d".format(value).replace(",", ".")

@Composable
fun PublicLocalScreen(
    selectedCategory: LocalCategory,
    cartItems: List<LocalCartItem>,
    catalogState: PublicCatalogState = PublicCatalogState(isLoading = false),
    onCategory: (LocalCategory) -> Unit,
    onProduct: (LocalProduct) -> Unit,
    onCart: () -> Unit,
    onHome: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
) {
    PublicShell(current = PublicBottomDestination.Shop, onHome = onHome, onPlus = onPlus, onShop = onShop) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(PediloBg),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { LocalHero(catalogState.romaStore()) }
            item { LocalStatsRow(catalogState.romaStore()) }
            item { CategoryTabs(selected = selectedCategory, onSelected = onCategory) }
            item { LocalPromo() }
            items(productsFor(selectedCategory, catalogState)) { product ->
                LocalProductCard(product = product, onClick = { onProduct(product) })
            }
        }
    }
}

@Composable
fun PublicLocalProductScreen(
    product: LocalProduct,
    onAddToCart: (LocalCartItem) -> Unit,
    onHome: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
) {
    var size by remember { mutableStateOf("Grande") }
    var quantity by remember { mutableIntStateOf(1) }
    var extraCheese by remember { mutableStateOf(true) }
    var extraSauce by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    val extras = buildList {
        if (extraCheese) add("Extra queso")
        if (extraSauce) add("Salsa aparte")
    }
    val unit = product.price + (if (size == "Grande") 1200 else 0) + extras.size * 450

    PublicShell(current = PublicBottomDestination.Shop, onHome = onHome, onPlus = onPlus, onShop = onShop) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(PediloBg),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { ProductVisual(product) }
            item { LocalSectionTitle(product.name, product.description) }
            item { OptionSelector("Tamaño", listOf("Individual", "Grande"), size, onSelected = { size = it }) }
            item {
                ToggleOption("Extra queso", "Suma muzzarella al producto", extraCheese) { extraCheese = !extraCheese }
            }
            item {
                ToggleOption("Salsa aparte", "Ideal para compartir", extraSauce) { extraSauce = !extraSauce }
            }
            item { QuantitySelector(quantity = quantity, onMinus = { if (quantity > 1) quantity-- }, onPlus = { quantity++ }) }
            item { LocalInput("Observaciones para el local", notes, "Sin cebolla, poco picante", minHeight = 88.dp, singleLine = false, onValueChange = { notes = it }) }
            item {
                LocalPrimaryButton(
                    label = "Agregar al carrito · ${formatLocalMoney(unit * quantity)}",
                    icon = LocalIconKind.Cart,
                    onClick = {
                        onAddToCart(LocalCartItem(product, size, extras, quantity, notes.ifBlank { "Sin observaciones" }))
                    },
                )
            }
        }
    }
}

@Composable
fun PublicLocalCartScreen(
    cartItems: List<LocalCartItem>,
    onMoreProducts: () -> Unit,
    onContinue: () -> Unit,
    onHome: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
) {
    PublicShell(current = PublicBottomDestination.Shop, onHome = onHome, onPlus = onPlus, onShop = onShop) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(PediloBg),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { LocalSectionTitle("Carrito del local", "Este pedido pertenece a Pizzería Roma") }
            items(cartItems) { item -> CartItemCard(item) }
            item { TotalsCard(cartItems) }
            item { LocalSecondaryButton("Agregar más productos", LocalIconKind.Plus, onMoreProducts) }
            item { LocalPrimaryButton("Continuar", LocalIconKind.Check, onClick = onContinue) }
        }
    }
}

@Composable
fun PublicLocalDataScreen(
    cartItems: List<LocalCartItem>,
    onContinue: (LocalOrderData) -> Unit,
    onHome: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var payment by remember { mutableStateOf("Efectivo al recibir") }
    var notes by remember { mutableStateOf("") }
    val canContinue = cartItems.isNotEmpty() && name.isNotBlank() && phone.isNotBlank() && address.isNotBlank() && payment.isNotBlank()

    PublicShell(current = PublicBottomDestination.Shop, onHome = onHome, onPlus = onPlus, onShop = onShop) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(PediloBg),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { LocalSectionTitle("Completá tus datos.", "") }
            item { LocalInput("Nombre completo", name, "Tu nombre", onValueChange = { name = it }) }
            item { LocalInput("WhatsApp", phone, "11 5555 5555", onValueChange = { phone = it }) }
            item { LocalInput("Dirección de entrega", address, "Calle, altura y piso", onValueChange = { address = it }) }
            item { OptionSelector("Forma de pago", listOf("Efectivo al recibir", "Transferencia"), payment, onSelected = { payment = it }) }
            item { LocalInput("Observaciones finales", notes, "Referencia de entrega", minHeight = 90.dp, singleLine = false, onValueChange = { notes = it }) }
            item { CompactOrderCard(cartItems) }
            item {
                LocalPrimaryButton("Continuar", LocalIconKind.Check, enabled = canContinue) {
                    onContinue(
                        LocalOrderData(
                            fullName = name,
                            phone = phone,
                            address = address,
                            payment = payment,
                            notes = notes,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
fun PublicLocalConfirmationScreen(
    cartItems: List<LocalCartItem>,
    orderData: LocalOrderData,
    onEditData: () -> Unit,
    onConfirm: () -> Unit,
    onHome: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
) {
    PublicShell(current = PublicBottomDestination.Shop, onHome = onHome, onPlus = onPlus, onShop = onShop) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(PediloBg),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { LocalSectionTitle("Confirmación", "") }
            item { CompactOrderCard(cartItems) }
            item { LocalInfoCard("Entrega", listOf(orderData.fullName, orderData.phone, orderData.address, orderData.notes), LocalIconKind.Location) }
            item { LocalInfoCard("Pago", listOf(orderData.payment, "Total ${formatLocalMoney(localGrandTotal(cartItems))}"), LocalIconKind.Cart) }
            item { LocalSecondaryButton("Editar datos", LocalIconKind.Person, onEditData) }
            item { LocalPrimaryButton("Confirmar pedido", LocalIconKind.Check, onClick = onConfirm) }
        }
    }
}

@Composable
fun PublicLocalTicketScreen(
    cartItems: List<LocalCartItem>,
    orderData: LocalOrderData,
    onTracking: (String) -> Unit,
    onHome: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
) {
    val orderNumber = "PDL-240518"
    PublicShell(current = PublicBottomDestination.Shop, onHome = onHome, onPlus = onPlus, onShop = onShop) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(PediloBg),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(PediloOrangeDark.copy(alpha = 0.45f), PediloPanel)), RoundedCornerShape(18.dp))
                        .border(1.dp, PediloOrangeDark, RoundedCornerShape(18.dp))
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(Modifier.size(72.dp).clip(CircleShape).background(PediloOrange), contentAlignment = Alignment.Center) {
                        LocalIcon(LocalIconKind.Check, Color.White, Modifier.size(38.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Pedido recibido", color = PediloText, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                    Text(orderNumber, color = PediloOrange, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Guardá este número para consultar el estado del pedido.", color = PediloMuted, fontSize = 13.sp, lineHeight = 17.sp, textAlign = TextAlign.Center)
                    Text("Estado inicial: Recibido", color = PediloMuted, fontSize = 14.sp)
                }
            }
            item { LocalInfoCard("Pedido de local", listOf("Pizzería Roma", "${cartItems.sumOf { it.quantity }} productos", "Total ${formatLocalMoney(localGrandTotal(cartItems))}"), LocalIconKind.Ticket) }
            item { LocalInfoCard("Entrega", listOf(orderData.address, orderData.payment), LocalIconKind.Location) }
            item { LocalPrimaryButton("Ver seguimiento", LocalIconKind.Tracking) { onTracking(orderNumber) } }
            item { LocalSecondaryButton("Volver al inicio", LocalIconKind.Check, onHome) }
        }
    }
}

@Composable
private fun LocalHero(store: PublicStoreSummary?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloPanel, RoundedCornerShape(18.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(18.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .background(Brush.radialGradient(listOf(PediloWarning, PediloOrangeDark, PediloPanelSoft))),
            contentAlignment = Alignment.Center,
        ) {
            LocalIcon(LocalIconKind.Pizza, Color.White, Modifier.size(90.dp))
        }
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(store?.name ?: "Pizzería Roma", color = PediloText, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                Text(if (store?.isOpen != false) "Abierto" else "Cerrado", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(if (store?.isOpen != false) PediloGreen else PediloMuted, RoundedCornerShape(16.dp)).padding(horizontal = 10.dp, vertical = 5.dp))
            }
            Text(store?.description?.ifBlank { store.category } ?: "Pizza a la piedra · Cocina italiana", color = PediloMuted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun LocalStatsRow(store: PublicStoreSummary?) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        StatPill("Horario", store?.openingHours ?: "hasta 00:30", LocalIconKind.Clock, Modifier.weight(1f))
        StatPill("20-30", "min", LocalIconKind.Clock, Modifier.weight(1f))
        StatPill("$1.200", "envío", LocalIconKind.Delivery, Modifier.weight(1f))
    }
}

@Composable
private fun StatPill(value: String, label: String, icon: LocalIconKind, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(88.dp)
            .background(PediloOverlay, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LocalIcon(icon, PediloOrange, Modifier.size(20.dp))
        Text(value, color = PediloText, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(label, color = PediloMuted, fontSize = 10.sp, lineHeight = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}

@Composable
private fun CategoryTabs(selected: LocalCategory, onSelected: (LocalCategory) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(LocalCategory.entries.toList()) { category ->
            val active = selected == category
            Box(
                modifier = Modifier
                    .height(42.dp)
                    .background(if (active) PediloOrange else PediloPanel, RoundedCornerShape(21.dp))
                    .border(1.dp, if (active) PediloOrange else PediloLine, RoundedCornerShape(21.dp))
                    .clickable(role = Role.Button) { onSelected(category) }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(category.label, color = if (active) Color.White else PediloMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LocalPromo() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloOverlay, RoundedCornerShape(12.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Text("Promo del día", color = PediloText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("Pizza grande con bebida con precio especial.", color = PediloMuted, fontSize = 13.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun LocalProductCard(product: LocalProduct, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(124.dp)
            .background(PediloOverlay, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProductThumb(product)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(product.name, color = PediloText, fontSize = 16.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (product.badge.isNotBlank()) {
                    Text(product.badge, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(PediloOrange, RoundedCornerShape(10.dp)).padding(horizontal = 7.dp, vertical = 4.dp))
                }
            }
            Text(product.description, color = PediloMuted, fontSize = 12.sp, lineHeight = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Text(formatLocalMoney(product.price), color = PediloOrange, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun ProductVisual(product: LocalProduct) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.radialGradient(listOf(PediloWarning, PediloOrangeDark, PediloPanelSoft))),
        contentAlignment = Alignment.Center,
    ) {
        LocalIcon(LocalIconKind.Pizza, Color.White, Modifier.size(118.dp))
        Text(formatLocalMoney(product.price), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomEnd).padding(14.dp).background(PediloBg.copy(alpha = 0.82f), RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 7.dp))
    }
}

@Composable
private fun ProductThumb(product: LocalProduct) {
    val accent = if (product.category == LocalCategory.Drinks) PediloCyan else PediloOrangeDark
    Box(
        modifier = Modifier
            .size(width = 86.dp, height = 102.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.radialGradient(listOf(PediloWarning, accent, PediloPanelSoft))),
        contentAlignment = Alignment.Center,
    ) {
        LocalIcon(if (product.category == LocalCategory.Drinks) LocalIconKind.Delivery else LocalIconKind.Pizza, Color.White, Modifier.size(56.dp))
    }
}

@Composable
private fun LocalSectionTitle(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, color = PediloOrange, fontSize = 30.sp, lineHeight = 33.sp, fontWeight = FontWeight.ExtraBold)
        if (subtitle.isNotBlank()) {
            Text(subtitle, color = PediloMuted, fontSize = 13.sp, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun OptionSelector(title: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloPanel, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .padding(13.dp),
    ) {
        Text(title, color = PediloMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val active = selected == option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .background(if (active) PediloOrange else PediloPanelSoft, RoundedCornerShape(21.dp))
                        .border(1.dp, if (active) PediloOrange else PediloLine, RoundedCornerShape(21.dp))
                        .clickable(role = Role.Button) { onSelected(option) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(option, color = if (active) Color.White else PediloMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ToggleOption(title: String, subtitle: String, checked: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloOverlay, RoundedCornerShape(14.dp))
            .border(1.dp, if (checked) PediloOrange else PediloLine, RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(28.dp).background(if (checked) PediloOrange else PediloPanelSoft, CircleShape), contentAlignment = Alignment.Center) {
            if (checked) LocalIcon(LocalIconKind.Check, Color.White, Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = PediloText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = PediloMuted, fontSize = 12.sp)
        }
        Text("+$450", color = PediloOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun QuantitySelector(quantity: Int, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloPanel, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Cantidad", color = PediloText, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        QuantityButton(LocalIconKind.Minus, onMinus)
        Text("$quantity", color = PediloText, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 18.dp))
        QuantityButton(LocalIconKind.Plus, onPlus)
    }
}

@Composable
private fun QuantityButton(icon: LocalIconKind, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(PediloOrange, CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        LocalIcon(icon, Color.White, Modifier.size(20.dp))
    }
}

@Composable
private fun CartItemCard(item: LocalCartItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloOverlay, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        ProductThumb(item.product)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("${item.quantity} x ${item.product.name}", color = PediloText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("${item.size} · ${item.extras.joinToString().ifBlank { "Sin extras" }}", color = PediloMuted, fontSize = 12.sp)
            Text(item.notes, color = PediloMuted, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Text(formatLocalMoney((item.product.price + (if (item.size == "Grande") 1200 else 0) + item.extras.size * 450) * item.quantity), color = PediloOrange, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TotalsCard(cartItems: List<LocalCartItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloPanel, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        TotalLine("Subtotal", formatLocalMoney(localCartTotal(cartItems)))
        TotalLine("Envío", formatLocalMoney(localDeliveryCost()))
        TotalLine("Total", formatLocalMoney(localGrandTotal(cartItems)), strong = true)
    }
}

@Composable
private fun TotalLine(label: String, value: String, strong: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, color = if (strong) PediloText else PediloMuted, fontSize = if (strong) 17.sp else 13.sp, fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
        Text(value, color = if (strong) PediloOrange else PediloText, fontSize = if (strong) 18.sp else 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CompactOrderCard(cartItems: List<LocalCartItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloOverlay, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Text("Resumen de Pizzería Roma", color = PediloText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        cartItems.forEach {
            Text("${it.quantity} x ${it.product.name}", color = PediloText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("${it.size} · ${it.extras.joinToString().ifBlank { "Sin extras" }}", color = PediloMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        }
        Spacer(Modifier.height(6.dp))
        TotalLine("Total", formatLocalMoney(localGrandTotal(cartItems)), strong = true)
    }
}

@Composable
private fun LocalInfoCard(title: String, lines: List<String>, icon: LocalIconKind) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PediloPanel, RoundedCornerShape(14.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(Modifier.size(44.dp).background(PediloPanelSoft, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            LocalIcon(icon, PediloOrange, Modifier.size(25.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = PediloText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            lines.forEach { Text(it, color = PediloMuted, fontSize = 12.sp, lineHeight = 16.sp) }
        }
    }
}

@Composable
private fun LocalInput(label: String, value: String, placeholder: String, onValueChange: (String) -> Unit, minHeight: androidx.compose.ui.unit.Dp = 56.dp, singleLine: Boolean = true) {
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
            textStyle = TextStyle(color = PediloText, fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold),
            singleLine = singleLine,
            modifier = Modifier.fillMaxWidth().height(minHeight),
            decorationBox = { inner ->
                Box(Modifier.fillMaxSize().background(PediloPanelSoft, RoundedCornerShape(11.dp)).padding(horizontal = 12.dp, vertical = 12.dp)) {
                    if (value.isBlank()) Text(placeholder, color = PediloMuted, fontSize = 15.sp, maxLines = if (singleLine) 1 else 3, overflow = TextOverflow.Ellipsis)
                    inner()
                }
            },
        )
    }
}

@Composable
private fun LocalPrimaryButton(label: String, icon: LocalIconKind, enabled: Boolean = true, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                if (enabled) Brush.verticalGradient(listOf(PediloOrangeSoft, PediloOrange)) else Brush.verticalGradient(listOf(PediloLine, PediloLine)),
                RoundedCornerShape(14.dp),
            )
            .border(1.dp, if (enabled) PediloOrange else PediloLine, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LocalIcon(icon, Color.White, Modifier.size(24.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun LocalSecondaryButton(label: String, icon: LocalIconKind, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(PediloBg, RoundedCornerShape(14.dp))
            .border(1.dp, PediloOrange, RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LocalIcon(icon, PediloOrange, Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = PediloOrange, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LocalNotice(text: String) {
    Text(text, color = PediloMuted, fontSize = 12.sp, lineHeight = 16.sp, modifier = Modifier.fillMaxWidth().background(PediloPanel, RoundedCornerShape(12.dp)).border(1.dp, PediloLine, RoundedCornerShape(12.dp)).padding(12.dp))
}

@Composable
private fun LocalIcon(kind: LocalIconKind, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = size.minDimension * 0.1f, cap = StrokeCap.Round)
        when (kind) {
            LocalIconKind.Pizza -> {
                val slice = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.16f)
                    lineTo(size.width * 0.84f, size.height * 0.34f)
                    lineTo(size.width * 0.42f, size.height * 0.88f)
                    close()
                }
                drawPath(slice, tint, style = stroke)
                drawCircle(tint, size.minDimension * 0.05f, Offset(size.width * 0.48f, size.height * 0.45f))
                drawCircle(tint, size.minDimension * 0.05f, Offset(size.width * 0.58f, size.height * 0.6f))
            }
            LocalIconKind.Star -> {
                val star = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.14f)
                    lineTo(size.width * 0.62f, size.height * 0.42f)
                    lineTo(size.width * 0.88f, size.height * 0.44f)
                    lineTo(size.width * 0.68f, size.height * 0.6f)
                    lineTo(size.width * 0.72f, size.height * 0.86f)
                    lineTo(size.width * 0.5f, size.height * 0.72f)
                    lineTo(size.width * 0.28f, size.height * 0.86f)
                    lineTo(size.width * 0.32f, size.height * 0.6f)
                    lineTo(size.width * 0.12f, size.height * 0.44f)
                    lineTo(size.width * 0.38f, size.height * 0.42f)
                    close()
                }
                drawPath(star, tint)
            }
            LocalIconKind.Clock -> {
                drawCircle(tint, size.minDimension * 0.36f, Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.28f), Offset(size.width * 0.5f, size.height * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.52f), Offset(size.width * 0.66f, size.height * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            LocalIconKind.Delivery, LocalIconKind.Tracking -> {
                drawRoundRect(tint, Offset(size.width * 0.16f, size.height * 0.36f), Size(size.width * 0.52f, size.height * 0.28f), CornerRadius(size.width * 0.05f), style = stroke)
                drawLine(tint, Offset(size.width * 0.68f, size.height * 0.45f), Offset(size.width * 0.84f, size.height * 0.45f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawCircle(tint, size.minDimension * 0.06f, Offset(size.width * 0.3f, size.height * 0.76f))
                drawCircle(tint, size.minDimension * 0.06f, Offset(size.width * 0.68f, size.height * 0.76f))
            }
            LocalIconKind.Cart -> {
                drawLine(tint, Offset(size.width * 0.18f, size.height * 0.26f), Offset(size.width * 0.3f, size.height * 0.26f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawRoundRect(tint, Offset(size.width * 0.32f, size.height * 0.35f), Size(size.width * 0.48f, size.height * 0.28f), CornerRadius(size.width * 0.04f), style = stroke)
                drawCircle(tint, size.minDimension * 0.06f, Offset(size.width * 0.42f, size.height * 0.78f))
                drawCircle(tint, size.minDimension * 0.06f, Offset(size.width * 0.72f, size.height * 0.78f))
            }
            LocalIconKind.Plus -> {
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.2f), Offset(size.width * 0.5f, size.height * 0.8f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.2f, size.height * 0.5f), Offset(size.width * 0.8f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            LocalIconKind.Minus -> drawLine(tint, Offset(size.width * 0.22f, size.height * 0.5f), Offset(size.width * 0.78f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            LocalIconKind.Check -> {
                drawLine(tint, Offset(size.width * 0.24f, size.height * 0.52f), Offset(size.width * 0.42f, size.height * 0.7f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.42f, size.height * 0.7f), Offset(size.width * 0.78f, size.height * 0.28f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            LocalIconKind.Ticket -> {
                drawRoundRect(tint, Offset(size.width * 0.18f, size.height * 0.25f), Size(size.width * 0.64f, size.height * 0.5f), CornerRadius(size.width * 0.08f), style = stroke)
                drawLine(tint, Offset(size.width * 0.32f, size.height * 0.44f), Offset(size.width * 0.68f, size.height * 0.44f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.32f, size.height * 0.6f), Offset(size.width * 0.58f, size.height * 0.6f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            LocalIconKind.Person -> {
                drawCircle(tint, size.minDimension * 0.18f, Offset(size.width * 0.5f, size.height * 0.34f), style = stroke)
                drawRoundRect(tint, Offset(size.width * 0.26f, size.height * 0.58f), Size(size.width * 0.48f, size.height * 0.22f), CornerRadius(size.width * 0.12f), style = stroke)
            }
            LocalIconKind.Location -> {
                drawCircle(tint, size.minDimension * 0.23f, Offset(size.width * 0.5f, size.height * 0.38f), style = stroke)
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.62f), Offset(size.width * 0.5f, size.height * 0.86f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
        }
    }
}
