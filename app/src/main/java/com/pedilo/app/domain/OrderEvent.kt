package com.pedilo.app.domain

import com.google.firebase.Timestamp

data class OrderEvent(
    val id: String,
    val actorId: String,
    val actorRole: String,
    val type: String,
    val fromStatus: OrderStatus?,
    val toStatus: OrderStatus?,
    val note: String?,
    val createdAt: Timestamp?
)
