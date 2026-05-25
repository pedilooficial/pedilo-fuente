package com.pedilo.app.core.model

data class PublicTrackingState(
    val trackingNumber: String,
    val status: PublicOrderStatus,
)
