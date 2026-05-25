package com.pedilo.app.core.model

data class PublicProductSummary(
    val id: String,
    val storeId: String,
    val name: String,
    val description: String = "",
)
