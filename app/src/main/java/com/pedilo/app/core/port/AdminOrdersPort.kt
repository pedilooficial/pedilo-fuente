package com.pedilo.app.core.port

import com.pedilo.app.core.model.AdminOrderDetail
import com.pedilo.app.core.model.AdminLiveOrderActionRequest
import com.pedilo.app.core.model.AdminLiveOrderActionResult
import com.pedilo.app.core.model.AdminOrderEvent
import com.pedilo.app.core.model.AdminOperationalHealthReport
import com.pedilo.app.core.model.AdminOrderActionRequest
import com.pedilo.app.core.model.AdminOrderActionResult
import com.pedilo.app.core.model.AdminOrderSummary
import com.pedilo.app.core.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface AdminOrdersPort {
    suspend fun getOrdersReadOnly(): CoreResult<List<AdminOrderSummary>>
    fun observeOrdersReadOnly(): Flow<CoreResult<List<AdminOrderSummary>>>
    suspend fun getOrderDetailReadOnly(orderId: String): CoreResult<AdminOrderDetail>
    suspend fun executeAdminOrderAction(request: AdminOrderActionRequest): CoreResult<AdminOrderActionResult>
    suspend fun executeLiveOrderAction(request: AdminLiveOrderActionRequest): CoreResult<AdminLiveOrderActionResult>
    suspend fun getOrderEventsReadOnly(orderId: String): CoreResult<List<AdminOrderEvent>>
    suspend fun getOperationalHealth(): CoreResult<AdminOperationalHealthReport>
}
