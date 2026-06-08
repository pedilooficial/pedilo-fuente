package com.pedilo.app.core.model

data class StoreOrderSummary(
    val id: String,
    val visibleNumber: String,
    val publicStatus: String,
    val operationalStatus: String,
    val contactName: String,
    val itemsSummary: List<String>,
    val nextAllowedActions: List<LiveOrderAction>,
    val version: Int,
    val activeIncident: Boolean,
)

data class StoreOrderDetail(
    val id: String,
    val visibleNumber: String,
    val publicStatus: String,
    val operationalStatus: String,
    val contactName: String,
    val itemsSummary: List<String>,
    val total: String,
    val nextAllowedActions: List<LiveOrderAction>,
    val version: Int,
    val activeIncident: Boolean,
)
