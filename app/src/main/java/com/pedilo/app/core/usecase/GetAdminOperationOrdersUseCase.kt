package com.pedilo.app.core.usecase

import com.pedilo.app.core.model.AdminOrderActionRequest
import com.pedilo.app.core.model.AdminLiveOrderActionRequest
import com.pedilo.app.core.port.AdminOrdersPort

class GetAdminOperationOrdersUseCase(
    private val port: AdminOrdersPort,
) {
    suspend operator fun invoke() = port.getOrdersReadOnly()
    fun observe() = port.observeOrdersReadOnly()
    suspend fun getDetail(orderId: String) = port.getOrderDetailReadOnly(orderId)
    suspend fun execute(request: AdminOrderActionRequest) = port.executeAdminOrderAction(request)
    suspend fun executeLive(request: AdminLiveOrderActionRequest) = port.executeLiveOrderAction(request)
    suspend fun getEvents(orderId: String) = port.getOrderEventsReadOnly(orderId)
    suspend fun getHealth() = port.getOperationalHealth()
}
