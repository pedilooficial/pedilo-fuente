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
    data object Shop : PublicRoute
    data class ShopSubcategory(val name: String) : PublicRoute
    data class ShopSearch(val query: String) : PublicRoute
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
            )
            PublicRoute.Plus -> PublicPhasePlaceholder(
                title = "Boton +",
                body = "Este acceso rapido se construira en una fase posterior.",
                onHome = { goHome() },
                onPlus = { navigateTo(PublicRoute.Plus) },
                onShop = { goShop() },
            )
            PublicRoute.Shop -> PublicShopScreen(
                onHome = { goHome() },
                onPlus = { navigateTo(PublicRoute.Plus) },
                onShop = { goShop() },
                onSearch = { navigateTo(PublicRoute.ShopSearch(it)) },
                onSubcategory = { navigateTo(PublicRoute.ShopSubcategory(it)) },
            )
            is PublicRoute.ShopSearch -> PublicShopSearchScreen(
                query = (route as PublicRoute.ShopSearch).query,
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
