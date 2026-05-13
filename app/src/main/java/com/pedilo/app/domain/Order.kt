package com.pedilo.app.domain

import com.google.firebase.Timestamp

data class OrderItem(
    val name: String,
    val quantity: Int,
    val note: String = ""
)

data class Order(
    val id: String,
    val status: OrderStatus,
    val requesterName: String,
    val items: List<OrderItem>,
    val deliveryAddress: String,
    val contactPhone: String,
    val note: String,
    val driverId: String?,
    val problemNote: String?,
    val availableActionsByRole: Map<String, List<String>>,
    val adminAllowedStatuses: List<String>,
    val createdAt: Timestamp?,
    val updatedAt: Timestamp?
) {
    fun actionsFor(role: UserRole): List<String> =
        availableActionsByRole[role.wireName].orEmpty()
}
