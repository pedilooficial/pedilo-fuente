package com.pedilo.app.core.usecase

import com.pedilo.app.core.model.PublicTrackingState
import com.pedilo.app.core.port.PublicTrackingPort
import com.pedilo.app.core.result.CoreError
import com.pedilo.app.core.result.CoreResult
import com.pedilo.app.core.result.ValidationError

class GetPublicTrackingUseCase(
    private val trackingPort: PublicTrackingPort,
) {
    suspend operator fun invoke(trackingNumber: String): CoreResult<PublicTrackingState> {
        val cleanTrackingNumber = trackingNumber.trim().uppercase()
        if (cleanTrackingNumber.isBlank() || cleanTrackingNumber.isPlaceholder()) {
            return CoreResult.Failure(
                CoreError.Validation(
                    listOf(
                        ValidationError(
                            field = ValidationError.Field.TRACKING_NUMBER,
                            reason = ValidationError.Reason.REQUIRED,
                        )
                    )
                )
            )
        }
        return trackingPort.getPublicTracking(cleanTrackingNumber)
    }
}
