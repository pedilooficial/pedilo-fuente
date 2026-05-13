package com.pedilo.app.domain

data class OrderDraft(
    val requesterName: String,
    val itemsText: String,
    val deliveryAddress: String,
    val contactPhone: String,
    val note: String = ""
)
