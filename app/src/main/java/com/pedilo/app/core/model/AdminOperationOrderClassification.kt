package com.pedilo.app.core.model

/**
 * Clasificación operativa read-only para Admin → Operación.
 * No escribe pedidos ni inventa estados sin señal en los datos.
 */
enum class AdminTodayOrdersBucket {
    ACTIVE,
    FINISHED,
    CANCELLED,
    DELAYED,
    WITH_PROBLEMS,
}

enum class AdminActiveOrdersBucket {
    WAITING_STORE,
    PREPARING,
    WAITING_DRIVER,
    IN_DELIVERY,
}

enum class AdminProblemOrdersBucket {
    STORE_NOT_RESPONDING,
    CUSTOMER_CLAIM,
}

data class AdminOperationOrderSignals(
    val status: String,
    val publicStatus: String,
    val source: String,
    val requestType: String,
) {
    companion object {
        fun from(summary: AdminOrderSummary): AdminOperationOrderSignals =
            AdminOperationOrderSignals(
                status = summary.status,
                publicStatus = summary.publicStatus,
                source = summary.source,
                requestType = summary.requestType,
            )
    }
}

object AdminOperationOrderClassification {
    const val STATUS_CREATED = "created"
    const val STATUS_CANCELLED = "cancelled"
    const val STATUS_CANCELED = "canceled"
    const val PUBLIC_STATUS_RECEIVED = "Pedido recibido"

    const val SOURCE_PUBLIC_LOCAL = "public_local"
    const val SOURCE_PUBLIC_PLUS_BUY = "public_plus_buy"
    const val SOURCE_PUBLIC_PLUS_PICKUP_SHIPPING = "public_plus_pickup_shipping"
    const val SOURCE_PUBLIC_APP = "public_app"

    fun todayBucket(signals: AdminOperationOrderSignals): AdminTodayOrdersBucket? {
        if (hasRealCancellationSignal(signals)) return AdminTodayOrdersBucket.CANCELLED
        if (hasRealFinishedSignal(signals)) return AdminTodayOrdersBucket.FINISHED
        if (hasRealDelaySignal(signals)) return AdminTodayOrdersBucket.DELAYED
        if (hasRealProblemSignal(signals)) return AdminTodayOrdersBucket.WITH_PROBLEMS
        if (hasRealActiveSignal(signals)) return AdminTodayOrdersBucket.ACTIVE
        return null
    }

    fun activeBucket(signals: AdminOperationOrderSignals): AdminActiveOrdersBucket? {
        if (!hasRealActiveSignal(signals)) return null
        if (hasRealPreparingSignal(signals)) return AdminActiveOrdersBucket.PREPARING
        if (hasRealWaitingDriverSignal(signals)) return AdminActiveOrdersBucket.WAITING_DRIVER
        if (hasRealInDeliverySignal(signals)) return AdminActiveOrdersBucket.IN_DELIVERY
        if (hasRealWaitingStoreSignal(signals)) return AdminActiveOrdersBucket.WAITING_STORE
        return null
    }

    fun problemBucket(signals: AdminOperationOrderSignals): AdminProblemOrdersBucket? {
        if (hasRealStoreNotRespondingSignal(signals)) return AdminProblemOrdersBucket.STORE_NOT_RESPONDING
        if (hasRealCustomerClaimSignal(signals)) return AdminProblemOrdersBucket.CUSTOMER_CLAIM
        return null
    }

    fun sourceLabel(source: String, requestType: String = ""): String =
        when (source.trim()) {
            SOURCE_PUBLIC_LOCAL -> "Pedido de local"
            SOURCE_PUBLIC_PLUS_BUY -> "Botón + Comprar"
            SOURCE_PUBLIC_PLUS_PICKUP_SHIPPING -> "Botón + Retiro / Envío"
            SOURCE_PUBLIC_APP -> "App pública (legado)"
            else -> if (source.isBlank()) "Origen no informado" else source
        }

    fun hasRealActiveSignal(signals: AdminOperationOrderSignals): Boolean =
        normalizedStatus(signals.status) == STATUS_CREATED &&
            signals.publicStatus.trim() == PUBLIC_STATUS_RECEIVED

    fun hasRealCancellationSignal(signals: AdminOperationOrderSignals): Boolean =
        normalizedStatus(signals.status) in setOf(STATUS_CANCELLED, STATUS_CANCELED)

    fun hasRealFinishedSignal(signals: AdminOperationOrderSignals): Boolean =
        normalizedStatus(signals.status) in setOf("delivered", "closed", "archived")

    fun hasRealDelaySignal(signals: AdminOperationOrderSignals): Boolean = false

    fun hasRealProblemSignal(signals: AdminOperationOrderSignals): Boolean =
        hasRealStoreNotRespondingSignal(signals) || hasRealCustomerClaimSignal(signals)

    fun hasRealWaitingStoreSignal(signals: AdminOperationOrderSignals): Boolean =
        hasRealActiveSignal(signals)

    fun hasRealPreparingSignal(signals: AdminOperationOrderSignals): Boolean = false

    fun hasRealWaitingDriverSignal(signals: AdminOperationOrderSignals): Boolean = false

    fun hasRealInDeliverySignal(signals: AdminOperationOrderSignals): Boolean = false

    fun hasRealStoreNotRespondingSignal(signals: AdminOperationOrderSignals): Boolean = false

    fun hasRealCustomerClaimSignal(signals: AdminOperationOrderSignals): Boolean =
        signals.publicStatus.contains("reclamo", ignoreCase = true) ||
            signals.publicStatus.contains("problema", ignoreCase = true)

    private fun normalizedStatus(status: String): String = status.trim().lowercase()
}
