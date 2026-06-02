package com.pedilo.app.core.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.pedilo.app.core.model.AdminOrderAction
import com.pedilo.app.core.model.AdminOrderActionRequest
import com.pedilo.app.core.model.AdminOrderActionResult
import com.pedilo.app.core.model.AdminOrderOperations
import com.pedilo.app.core.model.AdminOrderDetail
import com.pedilo.app.core.model.AdminOrderSummary
import com.pedilo.app.core.port.AdminOrdersPort
import com.pedilo.app.core.result.CoreError
import com.pedilo.app.core.result.CoreResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAdminOrdersAdapter(
    private val db: FirebaseFirestore = Firebase.firestore,
    private val functions: FirebaseFunctions = Firebase.functions(REGION),
) : AdminOrdersPort {
    override suspend fun getOrdersReadOnly(): CoreResult<List<AdminOrderSummary>> =
        runCatching {
            db.collection(ORDERS).get().await().documents.map { doc -> doc.toSummary() }
        }.fold(
            onSuccess = { CoreResult.Success(it) },
            onFailure = { CoreResult.Failure(CoreError.NotAvailable) },
        )

    override fun observeOrdersReadOnly(): Flow<CoreResult<List<AdminOrderSummary>>> =
        callbackFlow {
            val registration = db.collection(ORDERS).addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(CoreResult.Failure(CoreError.NotAvailable))
                    return@addSnapshotListener
                }
                trySend(CoreResult.Success(snapshot.documents.map { it.toSummary() }))
            }
            awaitClose { registration.remove() }
        }

    override suspend fun getOrderDetailReadOnly(orderId: String): CoreResult<AdminOrderDetail> =
        runCatching {
            val doc = db.collection(ORDERS).document(orderId).get().await()
            val itemsSummary = (doc.get(ITEMS) as? List<Map<String, Any?>>).orEmpty().map {
                "${it[ITEM_NAME].orEmptyText()} x${it[ITEM_QTY].orEmptyText()}"
            }
            val customer = doc.get(CUSTOMER) as? Map<String, Any?>
            val lastEvent = (doc.get(LAST_OPERATION_EVENT) as? Map<String, Any?>).orEmpty()
            AdminOrderDetail(
                id = doc.id,
                trackingNumber = doc.getString(TRACKING).orEmpty(),
                publicOrderNumber = doc.getString(PUBLIC_NUMBER).orEmpty(),
                status = doc.getString(STATUS).orEmpty(),
                publicStatus = doc.getString(PUBLIC_STATUS).orEmpty(),
                operationalStatus = doc.operationalStatus(),
                responsibleRole = doc.getString(RESPONSIBLE_ROLE).orEmpty(),
                priority = doc.priority(),
                needsAttention = doc.getBoolean(NEEDS_ATTENTION) ?: false,
                activeIncident = doc.getBoolean(ACTIVE_INCIDENT) ?: false,
                nextAllowedActions = doc.nextAllowedActions(),
                source = doc.getString(SOURCE).orEmpty(),
                requestType = doc.getString(REQUEST_TYPE).orEmpty(),
                storeName = doc.getString(STORE_NAME).orEmpty(),
                customerName = customer?.get(NAME).orEmptyText(),
                createdAtMillis = (doc.get(CREATED_AT) as? Timestamp)?.toDate()?.time,
                updatedAtMillis = (doc.get(UPDATED_AT) as? Timestamp)?.toDate()?.time,
                total = doc.get(TOTAL)?.toString().orEmpty(),
                itemsSummary = itemsSummary,
                lastEventSummary = lastEvent[EVENT_SUMMARY].orEmptyText(),
            )
        }.fold(
            onSuccess = { CoreResult.Success(it) },
            onFailure = { CoreResult.Failure(CoreError.NotAvailable) },
        )

    override suspend fun executeAdminOrderAction(request: AdminOrderActionRequest): CoreResult<AdminOrderActionResult> =
        runCatching {
            val result = functions
                .getHttpsCallable(ADMIN_ORDER_ACTION)
                .call(request.toCallablePayload())
                .await()

            @Suppress("UNCHECKED_CAST")
            val data = result.getData() as? Map<String, Any?>
                ?: error("Unexpected adminOrderAction response")

            AdminOrderActionResult(
                orderId = data["orderId"].asText(),
                status = data["status"].asText(),
                publicStatus = data["publicStatus"].asText(),
                operationalStatus = data["operationalStatus"].asText(),
                responsibleRole = data["responsibleRole"].asText(),
                priority = data["priority"].asText(),
                needsAttention = data["needsAttention"] as? Boolean ?: false,
                activeIncident = data["activeIncident"] as? Boolean ?: false,
                nextAllowedActions = data["nextAllowedActions"].asActionList(),
                eventSummary = data["eventSummary"].asText(),
                humanMessage = data["humanMessage"].asText(),
            )
        }.fold(
            onSuccess = { CoreResult.Success(it) },
            onFailure = { CoreResult.Failure(CoreError.Operational((it as? FirebaseFunctionsException)?.message ?: "No pudimos ejecutar la acción.")) },
        )

    private fun com.google.firebase.firestore.DocumentSnapshot.toSummary(): AdminOrderSummary =
        AdminOrderSummary(
            id = id,
            trackingNumber = getString(TRACKING).orEmpty(),
            publicOrderNumber = getString(PUBLIC_NUMBER).orEmpty(),
            status = getString(STATUS).orEmpty(),
            publicStatus = getString(PUBLIC_STATUS).orEmpty(),
            operationalStatus = operationalStatus(),
            responsibleRole = getString(RESPONSIBLE_ROLE).orEmpty(),
            priority = priority(),
            needsAttention = getBoolean(NEEDS_ATTENTION) ?: false,
            activeIncident = getBoolean(ACTIVE_INCIDENT) ?: false,
            nextAllowedActions = nextAllowedActions(),
            source = getString(SOURCE).orEmpty(),
            requestType = getString(REQUEST_TYPE).orEmpty(),
            storeName = getString(STORE_NAME).orEmpty(),
            createdAtMillis = (get(CREATED_AT) as? Timestamp)?.toDate()?.time,
            total = get(TOTAL)?.toString().orEmpty(),
        )

    private fun com.google.firebase.firestore.DocumentSnapshot.operationalStatus(): String =
        getString(OPERATIONAL_STATUS).orEmpty().ifBlank { getString(STATUS).orEmpty() }

    private fun com.google.firebase.firestore.DocumentSnapshot.priority(): String =
        getString(PRIORITY).orEmpty().ifBlank {
            if (getBoolean(ACTIVE_INCIDENT) == true || getBoolean(NEEDS_ATTENTION) == true) "high" else "normal"
        }

    private fun com.google.firebase.firestore.DocumentSnapshot.nextAllowedActions(): List<AdminOrderAction> =
        (get(NEXT_ALLOWED_ACTIONS) as? List<*>)
            .orEmpty()
            .mapNotNull { AdminOrderAction.fromWire(it.orEmptyText()) }
            .ifEmpty {
                AdminOrderOperations.allowedActions(
                    status = getString(STATUS).orEmpty(),
                    adminReviewed = getBoolean(ADMIN_REVIEWED) ?: false,
                    activeIncident = getBoolean(ACTIVE_INCIDENT) ?: false,
                    responsibleRole = getString(RESPONSIBLE_ROLE).orEmpty(),
                )
            }

    private fun AdminOrderActionRequest.toCallablePayload(): Map<String, Any?> =
        mapOf(
            "orderId" to orderId,
            "action" to action.wireName,
            "reason" to reason,
            "forcedStatus" to forcedStatus,
            "responsibleRole" to responsibleRole,
        )

    private fun Any?.asText(): String = this as? String ?: ""

    private fun Any?.asActionList(): List<AdminOrderAction> =
        (this as? List<*>).orEmpty().mapNotNull { AdminOrderAction.fromWire(it.orEmptyText()) }

    private fun Any?.orEmptyText(): String = this?.toString().orEmpty()

    private companion object {
        const val REGION = "southamerica-east1"
        const val ORDERS = "orders"
        const val ADMIN_ORDER_ACTION = "adminOrderAction"
        const val TRACKING = "trackingNumber"
        const val PUBLIC_NUMBER = "publicOrderNumber"
        const val STATUS = "status"
        const val PUBLIC_STATUS = "publicStatus"
        const val OPERATIONAL_STATUS = "operationalStatus"
        const val RESPONSIBLE_ROLE = "responsibleRole"
        const val PRIORITY = "priority"
        const val NEEDS_ATTENTION = "needsAttention"
        const val ACTIVE_INCIDENT = "activeIncident"
        const val ADMIN_REVIEWED = "adminReviewed"
        const val NEXT_ALLOWED_ACTIONS = "nextAllowedActions"
        const val LAST_OPERATION_EVENT = "lastOperationEvent"
        const val EVENT_SUMMARY = "summary"
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
