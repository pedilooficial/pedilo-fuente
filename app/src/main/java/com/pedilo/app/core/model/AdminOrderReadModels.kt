package com.pedilo.app.core.model

data class AdminOrderSummary(
    val id: String,
    val trackingNumber: String,
    val publicOrderNumber: String,
    val status: String,
    val publicStatus: String,
    val source: String,
    val requestType: String,
    val storeName: String,
    val createdAtMillis: Long?,
    val total: String,
)

data class AdminOrderDetail(
    val id: String,
    val trackingNumber: String,
    val publicOrderNumber: String,
    val status: String,
    val publicStatus: String,
    val source: String,
    val requestType: String,
    val storeName: String,
    val customerName: String,
    val createdAtMillis: Long?,
    val updatedAtMillis: Long?,
    val total: String,
    val itemsSummary: List<String>,
)
