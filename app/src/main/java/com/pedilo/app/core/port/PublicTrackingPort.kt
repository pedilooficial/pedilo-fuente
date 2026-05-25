package com.pedilo.app.core.port

import com.pedilo.app.core.model.PublicTrackingState
import com.pedilo.app.core.result.CoreResult

interface PublicTrackingPort {
    suspend fun getPublicTracking(trackingNumber: String): CoreResult<PublicTrackingState>
}
