package com.pedilo.app.ui.publicuser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    data object Shop : PublicRoute
    data class ShopSubcategory(val name: String) : PublicRoute
}

@Composable
fun PublicApp() {
    PublicTheme {
        var route by remember { mutableStateOf<PublicRoute>(PublicRoute.Home) }

        when (route) {
            PublicRoute.Home -> PublicHomeScreen(
                onHome = { route = PublicRoute.Home },
                onPlus = { route = PublicRoute.Plus },
                onShop = { route = PublicRoute.Shop },
            )
            PublicRoute.Plus -> PublicPhasePlaceholder(
                title = "Boton +",
                body = "Este acceso rapido se construira en una fase posterior.",
                onHome = { route = PublicRoute.Home },
                onPlus = { route = PublicRoute.Plus },
                onShop = { route = PublicRoute.Shop },
            )
            PublicRoute.Shop -> PublicShopScreen(
                onHome = { route = PublicRoute.Home },
                onPlus = { route = PublicRoute.Plus },
                onShop = { route = PublicRoute.Shop },
                onSubcategory = { route = PublicRoute.ShopSubcategory(it) },
            )
            is PublicRoute.ShopSubcategory -> PublicShopSubcategoryScreen(
                title = (route as PublicRoute.ShopSubcategory).name,
                onBack = { route = PublicRoute.Shop },
                onHome = { route = PublicRoute.Home },
                onPlus = { route = PublicRoute.Plus },
                onShop = { route = PublicRoute.Shop },
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
