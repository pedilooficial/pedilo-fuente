package com.pedilo.app.core.model

data class PublicOrderItem(
    val name: String,
    val quantity: Int = 1,
    val notes: String = "",
)
