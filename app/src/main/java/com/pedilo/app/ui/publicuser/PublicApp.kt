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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PublicApp() {
    PublicTheme {
        PublicRecoveryPlaceholder()
    }
}

@Composable
private fun PublicRecoveryPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PediloBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Pedilo",
            color = PediloOrange,
            fontSize = 44.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Recuperacion tecnica en curso",
            color = PediloText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "La UI publica se reconstruira con componentes reales.",
            color = PediloMuted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}
