package com.pedilo.app.core.model

data class PublicTrackingState(
    val trackingNumber: String,
    val status: PublicOrderStatus,
    val publicStatus: String,
    val humanMessage: String,
    val found: Boolean = true,
    val orderType: String = "",
    val storeName: String = "",
    val summary: String = "",
    val isClosed: Boolean = false,
)
