package com.pedilo.app.domain

data class PublicOrderForm(
    val requesterName: String = "",
    val contactPhone: String = "",
    val deliveryAddress: String = "",
    val itemsText: String = "",
    val note: String = ""
) {
    fun toDraft(): OrderDraft = OrderDraft(
        requesterName = requesterName.trim(),
        contactPhone = contactPhone.trim(),
        deliveryAddress = deliveryAddress.trim(),
        itemsText = itemsText.trim(),
        note = note.trim()
    )
}

data class PublicOrderValidationResult(
    val requesterNameError: String? = null,
    val contactPhoneError: String? = null,
    val deliveryAddressError: String? = null,
    val itemsTextError: String? = null,
    val noteError: String? = null
) {
    val isValid: Boolean =
        requesterNameError == null &&
            contactPhoneError == null &&
            deliveryAddressError == null &&
            itemsTextError == null &&
            noteError == null
}

object PublicOrderValidator {
    const val NAME_MAX = 80
    const val PHONE_MIN = 8
    const val PHONE_MAX = 15
    const val ADDRESS_MIN = 5
    const val ADDRESS_MAX = 180
    const val ITEMS_MIN = 3
    const val ITEMS_MAX = 1200
    const val NOTE_MAX = 300

    fun validate(form: PublicOrderForm): PublicOrderValidationResult {
        val name = form.requesterName.trim()
        val phone = form.contactPhone.trim()
        val address = form.deliveryAddress.trim()
        val items = form.itemsText.trim()
        val note = form.note.trim()

        return PublicOrderValidationResult(
            requesterNameError = when {
                name.isBlank() -> "Nombre obligatorio"
                name.length > NAME_MAX -> "Máximo $NAME_MAX caracteres"
                else -> null
            },
            contactPhoneError = when {
                phone.isBlank() -> "Teléfono obligatorio"
                phone.any { !it.isDigit() } -> "Solo números, sin espacios"
                phone.length !in PHONE_MIN..PHONE_MAX -> "Entre $PHONE_MIN y $PHONE_MAX dígitos"
                else -> null
            },
            deliveryAddressError = when {
                address.isBlank() -> "Dirección obligatoria"
                address.length < ADDRESS_MIN -> "Dirección demasiado corta"
                address.length > ADDRESS_MAX -> "Máximo $ADDRESS_MAX caracteres"
                else -> null
            },
            itemsTextError = when {
                items.isBlank() -> "Pedido obligatorio"
                items.length < ITEMS_MIN -> "Pedido demasiado corto"
                items.length > ITEMS_MAX -> "Máximo $ITEMS_MAX caracteres"
                else -> null
            },
            noteError = when {
                note.length > NOTE_MAX -> "Máximo $NOTE_MAX caracteres"
                else -> null
            }
        )
    }
}
