package com.pedilo.app.ui.publicuser

import android.app.Activity
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
import androidx.compose.ui.platform.LocalContext
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
}

@Composable
fun PublicApp() {
    PublicTheme {
        val activity = LocalContext.current as? Activity
        var route by remember { mutableStateOf<PublicRoute>(PublicRoute.Home) }
        val history = remember { mutableStateListOf<PublicRoute>() }
        var showExitConfirmation by remember { mutableStateOf(false) }

        fun navigateTo(next: PublicRoute) {
            if (route != next) {
                history.add(route)
                route = next
            }
        }

        fun goHome() {
            history.clear()
            route = PublicRoute.Home
            showExitConfirmation = false
        }

        fun goShop() {
            history.clear()
            route = PublicRoute.Shop
            showExitConfirmation = false
        }

        fun handleNativeBack() {
            if (history.isNotEmpty()) {
                route = history.removeAt(history.lastIndex)
                showExitConfirmation = false
            } else if (route != PublicRoute.Home) {
                route = PublicRoute.Home
                showExitConfirmation = false
            } else {
                showExitConfirmation = true
            }
        }

        BackHandler {
            handleNativeBack()
        }

        when (route) {
            PublicRoute.Home -> PublicHomeScreen(
                onHome = { goHome() },
                onPlus = { navigateTo(PublicRoute.Plus) },
                onShop = { goShop() },
                onSearch = { navigateTo(PublicRoute.ShopSearch("", PublicBottomDestination.Home)) },
                onConventions = { navigateTo(PublicRoute.Conventions) },
            )
            PublicRoute.Plus -> PublicPlusChoiceScreen(
                onHome = { goHome() },
                onPlus = { navigateTo(PublicRoute.Plus) },
                onShop = { goShop() },
                onBuy = { navigateTo(PublicRoute.PlusBuy) },
                onPickupShipping = { navigateTo(PublicRoute.PlusPickupShipping) },
            )
            PublicRoute.PlusBuy -> PublicPlusBuyScreen(
                onHome = { goHome() },
                onPlus = { navigateTo(PublicRoute.Plus) },
                onShop = { goShop() },
                onContinue = { navigateTo(PublicRoute.PlusConfirmation(it)) },
            )
            PublicRoute.PlusPickupShipping -> PublicPlusPickupShippingScreen(
                onHome = { goHome() },
                onPlus = { navigateTo(PublicRoute.Plus) },
                onShop = { goShop() },
                onContinue = { navigateTo(PublicRoute.PlusConfirmation(it)) },
            )
            is PublicRoute.PlusConfirmation -> PublicPlusConfirmationScreen(
                request = (route as PublicRoute.PlusConfirmation).request,
                onHome = { goHome() },
                onPlus = { navigateTo(PublicRoute.Plus) },
                onShop = { goShop() },
                onConfirm = { navigateTo(PublicRoute.PlusTicket(it)) },
            )
            is PublicRoute.PlusTicket -> PublicPlusTicketScreen(
                request = (route as PublicRoute.PlusTicket).request,
                onHome = { goHome() },
                onPlus = { navigateTo(PublicRoute.Plus) },
                onShop = { goShop() },
                onTracking = { navigateTo(PublicRoute.PublicTracking(it, PublicBottomDestination.Plus)) },
            )
            PublicRoute.Shop -> PublicShopScreen(
                onHome = { goHome() },
                onPlus = { navigateTo(PublicRoute.Plus) },
                onShop = { goShop() },
                onSearch = { navigateTo(PublicRoute.ShopSearch(it, PublicBottomDestination.Shop)) },
                onTracking = { navigateTo(PublicRoute.ShopTracking(it)) },
                onSubcategory = { navigateTo(PublicRoute.ShopSubcategory(it)) },
            )
            is PublicRoute.ShopSearch -> PublicShopSearchScreen(
                query = (route as PublicRoute.ShopSearch).query,
                current = (route as PublicRoute.ShopSearch).origin,
                onHome = { goHome() },
                onPlus = { navigateTo(PublicRoute.Plus) },
                onShop = { goShop() },
            )
            is PublicRoute.ShopSubcategory -> PublicShopSubcategoryScreen(
                title = (route as PublicRoute.ShopSubcategory).name,
                onHome = { goHome() },
                onPlus = { navigateTo(PublicRoute.Plus) },
                onShop = { goShop() },
            )
            is PublicRoute.ShopTracking -> PublicShopTrackingScreen(
                orderNumber = (route as PublicRoute.ShopTracking).orderNumber,
                current = PublicBottomDestination.Shop,
                onHome = { goHome() },
                onPlus = { navigateTo(PublicRoute.Plus) },
                onShop = { goShop() },
            )
            PublicRoute.Conventions -> PublicConventionsScreen(
                onHome = { goHome() },
                onPlus = { navigateTo(PublicRoute.Plus) },
                onShop = { goShop() },
                onInfo = { navigateTo(PublicRoute.ConventionsInfo) },
                onClaim = { navigateTo(PublicRoute.ConventionsClaim) },
                onTracking = { navigateTo(PublicRoute.ConventionsTrackingEntry) },
            )
            PublicRoute.ConventionsInfo -> PublicConventionsInfoScreen(
                onHome = { goHome() },
                onPlus = { navigateTo(PublicRoute.Plus) },
                onShop = { goShop() },
            )
            PublicRoute.ConventionsClaim -> PublicConventionsClaimScreen(
                onHome = { goHome() },
                onPlus = { navigateTo(PublicRoute.Plus) },
                onShop = { goShop() },
            )
            PublicRoute.ConventionsTrackingEntry -> PublicConventionsTrackingEntryScreen(
                onHome = { goHome() },
                onPlus = { navigateTo(PublicRoute.Plus) },
                onShop = { goShop() },
                onSubmit = { navigateTo(PublicRoute.PublicTracking(it, PublicBottomDestination.Home)) },
            )
            is PublicRoute.PublicTracking -> PublicShopTrackingScreen(
                orderNumber = (route as PublicRoute.PublicTracking).orderNumber,
                current = (route as PublicRoute.PublicTracking).current,
                onHome = { goHome() },
                onPlus = { navigateTo(PublicRoute.Plus) },
                onShop = { goShop() },
            )
        }

        if (showExitConfirmation) {
            AlertDialog(
                onDismissRequest = { showExitConfirmation = false },
                title = {
                    Text("Salir de Pedilo")
                },
                text = {
                    Text("Queres cerrar la app?")
                },
                confirmButton = {
                    TextButton(onClick = { activity?.finish() }) {
                        Text("Salir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitConfirmation = false }) {
                        Text("Seguir")
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
