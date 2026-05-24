package com.pedilo.app.ui.publicuser

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private sealed interface PublicRoute {
    data object Home : PublicRoute
    data object Plus : PublicRoute
    data object PlusBuy : PublicRoute
    data object PlusPickupShipping : PublicRoute
    data class PlusConfirmation(val request: PublicPlusRequest) : PublicRoute
    data class PlusTicket(val request: PublicPlusRequest) : PublicRoute
    data object Shop : PublicRoute
    data object Conventions : PublicRoute
    data object ConventionsInfo : PublicRoute
    data object ConventionsClaim : PublicRoute
    data object ConventionsTrackingEntry : PublicRoute
    data class PublicTracking(val orderNumber: String, val current: PublicBottomDestination) : PublicRoute
    data class ShopSubcategory(val name: String) : PublicRoute
    data class ShopSearch(val query: String, val origin: PublicBottomDestination) : PublicRoute
    data class ShopTracking(val orderNumber: String) : PublicRoute
    data object Local : PublicRoute
    data class LocalProductDetail(val product: LocalProduct) : PublicRoute
    data object LocalCart : PublicRoute
    data object LocalData : PublicRoute
    data class LocalConfirmation(val orderData: LocalOrderData) : PublicRoute
    data class LocalTicket(val orderData: LocalOrderData) : PublicRoute
}

@Composable
fun PublicApp() {
    PublicTheme {
        var route by remember { mutableStateOf<PublicRoute>(PublicRoute.Home) }
        val history = remember { mutableStateListOf<PublicRoute>() }
        val localCart = remember { mutableStateListOf<LocalCartItem>() }
        var localCategory by remember { mutableStateOf(LocalCategory.Featured) }
        var localOrderPlaced by remember { mutableStateOf(false) }
        var pendingLocalExit by remember { mutableStateOf<PublicRoute?>(null) }

        fun isLocalRoute(target: PublicRoute): Boolean = when (target) {
            PublicRoute.Local,
            is PublicRoute.LocalProductDetail,
            PublicRoute.LocalCart,
            PublicRoute.LocalData,
            is PublicRoute.LocalConfirmation,
            is PublicRoute.LocalTicket -> true
            else -> false
        }

        fun hasActiveLocalCart(): Boolean = localCart.isNotEmpty() && !localOrderPlaced

        fun navigateTo(next: PublicRoute) {
            if (route != next) {
                history.add(route)
                route = next
            }
        }

        fun goHome() {
            if (isLocalRoute(route) && hasActiveLocalCart()) {
                pendingLocalExit = PublicRoute.Home
                return
            }
            history.clear()
            route = PublicRoute.Home
        }

        fun goShop() {
            if (isLocalRoute(route) && hasActiveLocalCart()) {
                pendingLocalExit = PublicRoute.Shop
                return
            }
            history.clear()
            route = PublicRoute.Shop
        }

        fun goPlus() {
            if (isLocalRoute(route) && hasActiveLocalCart()) {
                pendingLocalExit = PublicRoute.Plus
                return
            }
            navigateTo(PublicRoute.Plus)
        }

        fun confirmLocalExit() {
            val target = pendingLocalExit ?: PublicRoute.Home
            localCart.clear()
            localOrderPlaced = false
            pendingLocalExit = null
            history.clear()
            route = target
        }

        fun handleNativeBack() {
            val previous = history.lastOrNull()
            if (isLocalRoute(route) && hasActiveLocalCart() && (previous == null || !isLocalRoute(previous))) {
                pendingLocalExit = previous ?: PublicRoute.Home
            } else if (history.isNotEmpty()) {
                route = history.removeAt(history.lastIndex)
            } else if (route != PublicRoute.Home) {
                route = PublicRoute.Home
            }
        }

        BackHandler(enabled = route != PublicRoute.Home || history.isNotEmpty()) {
            handleNativeBack()
        }

        when (route) {
            PublicRoute.Home -> PublicHomeScreen(
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
                onSearch = { navigateTo(PublicRoute.ShopSearch("", PublicBottomDestination.Home)) },
                onConventions = { navigateTo(PublicRoute.Conventions) },
            )
            PublicRoute.Plus -> PublicPlusChoiceScreen(
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
                onBuy = { navigateTo(PublicRoute.PlusBuy) },
                onPickupShipping = { navigateTo(PublicRoute.PlusPickupShipping) },
            )
            PublicRoute.PlusBuy -> PublicPlusBuyScreen(
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
                onContinue = { navigateTo(PublicRoute.PlusConfirmation(it)) },
            )
            PublicRoute.PlusPickupShipping -> PublicPlusPickupShippingScreen(
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
                onContinue = { navigateTo(PublicRoute.PlusConfirmation(it)) },
            )
            is PublicRoute.PlusConfirmation -> PublicPlusConfirmationScreen(
                request = (route as PublicRoute.PlusConfirmation).request,
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
                onConfirm = { navigateTo(PublicRoute.PlusTicket(it)) },
            )
            is PublicRoute.PlusTicket -> PublicPlusTicketScreen(
                request = (route as PublicRoute.PlusTicket).request,
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
                onTracking = { navigateTo(PublicRoute.PublicTracking(it, PublicBottomDestination.Plus)) },
            )
            PublicRoute.Shop -> PublicShopScreen(
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
                onSearch = { navigateTo(PublicRoute.ShopSearch(it, PublicBottomDestination.Shop)) },
                onTracking = { navigateTo(PublicRoute.ShopTracking(it)) },
                onSubcategory = { navigateTo(PublicRoute.ShopSubcategory(it)) },
            )
            is PublicRoute.ShopSearch -> PublicShopSearchScreen(
                query = (route as PublicRoute.ShopSearch).query,
                current = (route as PublicRoute.ShopSearch).origin,
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
                onViewLocal = {
                    localOrderPlaced = false
                    navigateTo(PublicRoute.Local)
                },
            )
            is PublicRoute.ShopSubcategory -> PublicShopSubcategoryScreen(
                title = (route as PublicRoute.ShopSubcategory).name,
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
                onViewLocal = {
                    localOrderPlaced = false
                    navigateTo(PublicRoute.Local)
                },
            )
            is PublicRoute.ShopTracking -> PublicShopTrackingScreen(
                orderNumber = (route as PublicRoute.ShopTracking).orderNumber,
                current = PublicBottomDestination.Shop,
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
            )
            PublicRoute.Conventions -> PublicConventionsScreen(
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
                onInfo = { navigateTo(PublicRoute.ConventionsInfo) },
                onClaim = { navigateTo(PublicRoute.ConventionsClaim) },
                onTracking = { navigateTo(PublicRoute.ConventionsTrackingEntry) },
            )
            PublicRoute.ConventionsInfo -> PublicConventionsInfoScreen(
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
            )
            PublicRoute.ConventionsClaim -> PublicConventionsClaimScreen(
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
            )
            PublicRoute.ConventionsTrackingEntry -> PublicConventionsTrackingEntryScreen(
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
                onSubmit = { navigateTo(PublicRoute.PublicTracking(it, PublicBottomDestination.Home)) },
            )
            is PublicRoute.PublicTracking -> PublicShopTrackingScreen(
                orderNumber = (route as PublicRoute.PublicTracking).orderNumber,
                current = (route as PublicRoute.PublicTracking).current,
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
            )
            PublicRoute.Local -> PublicLocalScreen(
                selectedCategory = localCategory,
                cartItems = localCart,
                onCategory = { localCategory = it },
                onProduct = { navigateTo(PublicRoute.LocalProductDetail(it)) },
                onCart = { navigateTo(PublicRoute.LocalCart) },
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
            )
            is PublicRoute.LocalProductDetail -> PublicLocalProductScreen(
                product = (route as PublicRoute.LocalProductDetail).product,
                onAddToCart = {
                    localOrderPlaced = false
                    localCart.add(it)
                    navigateTo(PublicRoute.LocalCart)
                },
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
            )
            PublicRoute.LocalCart -> PublicLocalCartScreen(
                cartItems = localCart,
                onMoreProducts = { navigateTo(PublicRoute.Local) },
                onContinue = { navigateTo(PublicRoute.LocalData) },
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
            )
            PublicRoute.LocalData -> PublicLocalDataScreen(
                cartItems = localCart,
                onContinue = { navigateTo(PublicRoute.LocalConfirmation(it)) },
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
            )
            is PublicRoute.LocalConfirmation -> PublicLocalConfirmationScreen(
                cartItems = localCart,
                orderData = (route as PublicRoute.LocalConfirmation).orderData,
                onEditData = { navigateTo(PublicRoute.LocalData) },
                onConfirm = {
                    localOrderPlaced = true
                    navigateTo(PublicRoute.LocalTicket((route as PublicRoute.LocalConfirmation).orderData))
                },
                onHome = { goHome() },
                onPlus = { goPlus() },
                onShop = { goShop() },
            )
            is PublicRoute.LocalTicket -> PublicLocalTicketScreen(
                cartItems = localCart,
                orderData = (route as PublicRoute.LocalTicket).orderData,
                onTracking = { navigateTo(PublicRoute.PublicTracking(it, PublicBottomDestination.Shop)) },
                onHome = {
                    localCart.clear()
                    localOrderPlaced = false
                    goHome()
                },
                onPlus = { goPlus() },
                onShop = { goShop() },
            )
        }

        pendingLocalExit?.let {
            AlertDialog(
                onDismissRequest = { pendingLocalExit = null },
                title = {
                    Text("Salir del local")
                },
                text = {
                    Text("Tenés productos de Pizzería Roma en el carrito. Si salís, se vacía este pedido del local.")
                },
                confirmButton = {
                    TextButton(onClick = { confirmLocalExit() }) {
                        Text("Salir y vaciar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingLocalExit = null }) {
                        Text("Seguir en el local")
                    }
                },
            )
        }
    }
}

@Composable
private fun PublicPhasePlaceholder(
    title: String,
    body: String,
    onHome: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
) {
    PublicShell(
        current = if (title == "Tienda") PublicBottomDestination.Shop else PublicBottomDestination.Plus,
        onHome = onHome,
        onPlus = onPlus,
        onShop = onShop,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PediloBg)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Pedilo",
                color = PediloOrange,
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = title,
                color = PediloText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = body,
                color = PediloMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
