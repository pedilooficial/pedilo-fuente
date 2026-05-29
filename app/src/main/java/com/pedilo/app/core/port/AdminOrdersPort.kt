package com.pedilo.app.core.port

import com.pedilo.app.core.model.AdminOrderDetail
import com.pedilo.app.core.model.AdminOrderSummary
import com.pedilo.app.core.result.CoreResult

interface AdminOrdersPort {
    suspend fun getOrdersReadOnly(): CoreResult<List<AdminOrderSummary>>
    suspend fun getOrderDetailReadOnly(orderId: String): CoreResult<AdminOrderDetail>
}
