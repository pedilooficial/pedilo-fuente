package com.pedilo.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PediloButton(
    text: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Text(text)
    }
}
