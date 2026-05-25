package com.pedilo.app.core.model

data class PublicOrderDraft(
    val contact: CustomerContact,
    val deliveryLocation: DeliveryLocation?,
    val items: List<PublicOrderItem>,
    val paymentMethod: PaymentMethod = PaymentMethod.NotSpecified,
    val notes: String = "",
)
