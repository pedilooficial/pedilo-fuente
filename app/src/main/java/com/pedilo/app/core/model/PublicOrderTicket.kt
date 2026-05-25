package com.pedilo.app.core.model

data class PublicOrderTicket(
    val trackingNumber: String,
    val status: PublicOrderStatus,
)
