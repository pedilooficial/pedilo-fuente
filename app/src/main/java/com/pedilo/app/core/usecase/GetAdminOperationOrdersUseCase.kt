package com.pedilo.app.core.usecase

import com.pedilo.app.core.port.AdminOrdersPort

class GetAdminOperationOrdersUseCase(
    private val port: AdminOrdersPort,
) {
    suspend operator fun invoke() = port.getOrdersReadOnly()
    suspend fun getDetail(orderId: String) = port.getOrderDetailReadOnly(orderId)
}
