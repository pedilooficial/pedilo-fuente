package com.pedilo.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun PediloTextField(
    value: String,
    label: String,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    minLines: Int = 1,
    maxLines: Int = if (minLines == 1) 1 else Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        isError = isError,
        supportingText = supportingText?.let { message -> { Text(message) } },
        minLines = minLines,
        maxLines = maxLines,
        visualTransformation = visualTransformation,
        modifier = Modifier.fillMaxWidth()
    )
}
