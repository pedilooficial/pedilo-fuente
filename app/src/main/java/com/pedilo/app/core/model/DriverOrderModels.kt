package com.pedilo.app.core.model

data class DriverOrderSummary(
    val id: String,
    val visibleNumber: String,
    val publicStatus: String,
    val operationalStatus: String,
    val contactName: String,
    val storeLabel: String,
    val itemsSummary: List<String>,
    val nextAllowedActions: List<LiveOrderAction>,
    val version: Int,
    val activeIncident: Boolean,
    val isAssignedToCurrentDriver: Boolean,
)

data class DriverOrderDetail(
    val id: String,
    val visibleNumber: String,
    val publicStatus: String,
    val operationalStatus: String,
    val contactName: String,
    val contactPhone: String,
    val deliveryAddress: String,
    val storeLabel: String,
    val itemsSummary: List<String>,
    val total: String,
    val nextAllowedActions: List<LiveOrderAction>,
    val version: Int,
    val activeIncident: Boolean,
    val isAssignedToCurrentDriver: Boolean,
)
