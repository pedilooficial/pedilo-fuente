package com.pedilo.app.core.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pedilo.app.core.model.AdminOrderDetail
import com.pedilo.app.core.model.AdminOrderSummary
import com.pedilo.app.core.port.AdminOrdersPort
import com.pedilo.app.core.result.CoreError
import com.pedilo.app.core.result.CoreResult
import kotlinx.coroutines.tasks.await

class FirebaseAdminOrdersAdapter(
    private val db: FirebaseFirestore = Firebase.firestore,
) : AdminOrdersPort {
    override suspend fun getOrdersReadOnly(): CoreResult<List<AdminOrderSummary>> =
        runCatching {
            db.collection(ORDERS).get().await().documents.map { doc ->
                AdminOrderSummary(
                    id = doc.id,
                    trackingNumber = doc.getString(TRACKING).orEmpty(),
                    publicOrderNumber = doc.getString(PUBLIC_NUMBER).orEmpty(),
                    status = doc.getString(STATUS).orEmpty(),
                    publicStatus = doc.getString(PUBLIC_STATUS).orEmpty(),
                    source = doc.getString(SOURCE).orEmpty(),
                    requestType = doc.getString(REQUEST_TYPE).orEmpty(),
                    storeName = doc.getString(STORE_NAME).orEmpty(),
                    createdAtMillis = (doc.get(CREATED_AT) as? Timestamp)?.toDate()?.time,
                    total = doc.get(TOTAL)?.toString().orEmpty(),
                )
            }
        }.fold(
            onSuccess = { CoreResult.Success(it) },
            onFailure = { CoreResult.Failure(CoreError.NotAvailable) },
        )

    override suspend fun getOrderDetailReadOnly(orderId: String): CoreResult<AdminOrderDetail> =
        runCatching {
            val doc = db.collection(ORDERS).document(orderId).get().await()
            val itemsSummary = (doc.get(ITEMS) as? List<Map<String, Any?>>).orEmpty().map {
                "${it[ITEM_NAME].orEmptyText()} x${it[ITEM_QTY].orEmptyText()}"
            }
            val customer = doc.get(CUSTOMER) as? Map<String, Any?>
            AdminOrderDetail(
                id = doc.id,
                trackingNumber = doc.getString(TRACKING).orEmpty(),
                publicOrderNumber = doc.getString(PUBLIC_NUMBER).orEmpty(),
                status = doc.getString(STATUS).orEmpty(),
                publicStatus = doc.getString(PUBLIC_STATUS).orEmpty(),
                source = doc.getString(SOURCE).orEmpty(),
                requestType = doc.getString(REQUEST_TYPE).orEmpty(),
                storeName = doc.getString(STORE_NAME).orEmpty(),
                customerName = customer?.get(NAME).orEmptyText(),
                createdAtMillis = (doc.get(CREATED_AT) as? Timestamp)?.toDate()?.time,
                updatedAtMillis = (doc.get(UPDATED_AT) as? Timestamp)?.toDate()?.time,
                total = doc.get(TOTAL)?.toString().orEmpty(),
                itemsSummary = itemsSummary,
            )
        }.fold(
            onSuccess = { CoreResult.Success(it) },
            onFailure = { CoreResult.Failure(CoreError.NotAvailable) },
        )

    private fun Any?.orEmptyText(): String = this?.toString().orEmpty()

    private companion object {
        const val ORDERS = "orders"
        const val TRACKING = "trackingNumber"
        const val PUBLIC_NUMBER = "publicOrderNumber"
        const val STATUS = "status"
        const val PUBLIC_STATUS = "publicStatus"
        const val SOURCE = "source"
        const val REQUEST_TYPE = "requestType"
        const val STORE_NAME = "storeName"
        const val CREATED_AT = "createdAt"
        const val UPDATED_AT = "updatedAt"
        const val TOTAL = "total"
        const val ITEMS = "items"
        const val ITEM_NAME = "name"
        const val ITEM_QTY = "quantity"
        const val CUSTOMER = "customer"
        const val NAME = "name"
    }
}
