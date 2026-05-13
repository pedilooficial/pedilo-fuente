package com.pedilo.app.data

import com.pedilo.app.domain.Order
import com.pedilo.app.domain.OrderDraft
import com.pedilo.app.domain.OrderEvent
import com.pedilo.app.domain.UserProfile
import kotlinx.coroutines.flow.Flow

interface PediloRepository {
    val currentOperatorId: String?

    suspend fun createPublicOrder(draft: OrderDraft): String
    suspend fun signInOperator(email: String, password: String)
    suspend fun signOutOperator()
    fun observeOperatorProfile(userId: String): Flow<UserProfile?>
    fun observeLiveOrders(profile: UserProfile): Flow<List<Order>>
    fun observeEvents(orderId: String): Flow<List<OrderEvent>>
    suspend fun runOrderAction(orderId: String, action: String, note: String? = null)
    suspend fun assignDriver(orderId: String, driverId: String)
    suspend fun adminSetStatus(orderId: String, toStatus: String, note: String? = null)
}
