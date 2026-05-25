package com.pedilo.app.core.result

data class ValidationError(
    val field: Field,
    val reason: Reason,
) {
    enum class Field {
        NAME,
        PHONE,
        ADDRESS,
        ITEMS,
        STORE,
        PAYMENT,
        TRACKING_NUMBER,
    }

    enum class Reason {
        REQUIRED,
        INVALID,
    }
}
