package com.pedilo.app.core.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.pedilo.app.core.model.AdminOrderAction
import com.pedilo.app.core.model.AdminOrderActionRequest
import com.pedilo.app.core.model.AdminOrderActionResult
import com.pedilo.app.core.model.AdminLiveOrderActionRequest
import com.pedilo.app.core.model.AdminLiveOrderActionResult
import com.pedilo.app.core.model.AdminOrderEvent
import com.pedilo.app.core.model.AdminOrderDetail
import com.pedilo.app.core.model.AdminOrderSummary
import com.pedilo.app.core.model.LiveOrderAction
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
            val events = getOrderEventsReadOnly(orderId).let {
                when (it) {
                    is CoreResult.Success -> it.value
                    is CoreResult.Failure -> emptyList()
                }
            }
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
                paymentMethod = doc.getString(PAYMENT_METHOD).orEmpty(),
                amountToCollect = doc.get(AMOUNT_TO_COLLECT)?.toString().orEmpty(),
                collectedAmount = doc.get(COLLECTED_AMOUNT)?.toString().orEmpty(),
                collectionRequired = doc.getBoolean(COLLECTION_REQUIRED) ?: false,
                cashResponsibleRole = doc.getString(CASH_RESPONSIBLE_ROLE).orEmpty(),
                financialNotes = doc.getString(FINANCIAL_NOTES).orEmpty(),
                itemsSummary = itemsSummary,
                lastEventSummary = lastEvent[EVENT_SUMMARY].orEmptyText(),
                orderType = doc.getString(ORDER_TYPE).orEmpty(),
                financialStatus = doc.getString(FINANCIAL_STATUS).orEmpty(),
                communicationStatus = doc.getString(COMMUNICATION_STATUS).orEmpty(),
                aiRiskLevel = doc.getString(AI_RISK_LEVEL).orEmpty(),
                aiClassification = doc.getString(AI_CLASSIFICATION).orEmpty(),
                aiSuggestedAction = doc.getString(AI_SUGGESTED_ACTION).orEmpty(),
                aiRequiresHumanReview = doc.getBoolean(AI_REQUIRES_HUMAN_REVIEW) ?: false,
                incidentStatus = doc.getString(INCIDENT_STATUS).orEmpty(),
                archiveStatus = doc.getString(ARCHIVE_STATUS).orEmpty(),
                currentResponsibleRole = doc.getString(CURRENT_RESPONSIBLE_ROLE).orEmpty(),
                assignedActorId = doc.getString(ASSIGNED_ACTOR_ID).orEmpty(),
                assignedActorRole = doc.getString(ASSIGNED_ACTOR_ROLE).orEmpty(),
                version = doc.version(),
                idempotencyKey = doc.getString(IDEMPOTENCY_KEY).orEmpty(),
                events = events,
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

    override suspend fun executeLiveOrderAction(request: AdminLiveOrderActionRequest): CoreResult<AdminLiveOrderActionResult> =
        runCatching {
            val result = functions
                .getHttpsCallable(OPERATE_LIVE_ORDER)
                .call(request.toCallablePayload())
                .await()

            @Suppress("UNCHECKED_CAST")
            val data = result.getData() as? Map<String, Any?>
                ?: error("Unexpected operateLiveOrder response")

            AdminLiveOrderActionResult(
                orderId = data["orderId"].asText(),
                action = data["action"].asText(),
                publicStatus = data["publicStatus"].asText(),
                operationalStatus = data["operationalStatus"].asText(),
                version = (data["version"] as? Number)?.toInt() ?: 0,
                eventSummary = data["eventSummary"].asText(),
                humanMessage = data["humanMessage"].asText(),
            )
        }.fold(
            onSuccess = { CoreResult.Success(it) },
            onFailure = { CoreResult.Failure(CoreError.Operational((it as? FirebaseFunctionsException)?.message ?: "No pudimos ejecutar la acción.")) },
        )

    override suspend fun getOrderEventsReadOnly(orderId: String): CoreResult<List<AdminOrderEvent>> =
        runCatching {
            db.collection(ORDERS)
                .document(orderId)
                .collection(EVENTS)
                .orderBy(CREATED_AT, Query.Direction.DESCENDING)
                .limit(8)
                .get()
                .await()
                .documents
                .map { doc ->
                    AdminOrderEvent(
                        id = doc.id,
                        type = doc.getString(EVENT_TYPE).orEmpty(),
                        summary = doc.getString(EVENT_SUMMARY).orEmpty(),
                        actorRole = doc.getString(ACTOR_ROLE).orEmpty(),
                        reason = doc.getString(REASON).orEmpty(),
                        createdAtMillis = (doc.get(CREATED_AT) as? Timestamp)?.toDate()?.time,
                    )
                }
        }.fold(
            onSuccess = { CoreResult.Success(it) },
            onFailure = { CoreResult.Failure(CoreError.NotAvailable) },
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
            paymentMethod = getString(PAYMENT_METHOD).orEmpty(),
            amountToCollect = get(AMOUNT_TO_COLLECT)?.toString().orEmpty(),
            collectedAmount = get(COLLECTED_AMOUNT)?.toString().orEmpty(),
            collectionRequired = getBoolean(COLLECTION_REQUIRED) ?: false,
            cashResponsibleRole = getString(CASH_RESPONSIBLE_ROLE).orEmpty(),
            orderType = getString(ORDER_TYPE).orEmpty(),
            financialStatus = getString(FINANCIAL_STATUS).orEmpty(),
            communicationStatus = getString(COMMUNICATION_STATUS).orEmpty(),
            aiRiskLevel = getString(AI_RISK_LEVEL).orEmpty(),
            aiClassification = getString(AI_CLASSIFICATION).orEmpty(),
            aiSuggestedAction = getString(AI_SUGGESTED_ACTION).orEmpty(),
            aiRequiresHumanReview = getBoolean(AI_REQUIRES_HUMAN_REVIEW) ?: false,
            incidentStatus = getString(INCIDENT_STATUS).orEmpty(),
            archiveStatus = getString(ARCHIVE_STATUS).orEmpty(),
            currentResponsibleRole = getString(CURRENT_RESPONSIBLE_ROLE).orEmpty(),
            assignedActorId = getString(ASSIGNED_ACTOR_ID).orEmpty(),
            assignedActorRole = getString(ASSIGNED_ACTOR_ROLE).orEmpty(),
            version = version(),
            idempotencyKey = getString(IDEMPOTENCY_KEY).orEmpty(),
        )

    private fun com.google.firebase.firestore.DocumentSnapshot.operationalStatus(): String =
        getString(OPERATIONAL_STATUS).orEmpty().ifBlank { getString(STATUS).orEmpty() }

    private fun com.google.firebase.firestore.DocumentSnapshot.priority(): String =
        getString(PRIORITY).orEmpty().ifBlank {
            if (getBoolean(ACTIVE_INCIDENT) == true || getBoolean(NEEDS_ATTENTION) == true) "high" else "normal"
        }

    private fun com.google.firebase.firestore.DocumentSnapshot.version(): Int =
        (get(VERSION) as? Number)?.toInt() ?: 0

    private fun com.google.firebase.firestore.DocumentSnapshot.nextAllowedActions(): List<LiveOrderAction> =
        (get(NEXT_ALLOWED_ACTIONS) as? List<*>)
            .orEmpty()
            .mapNotNull { LiveOrderAction.fromWire(it.orEmptyText()) }
            .ifEmpty {
                emptyList()
            }

    private fun AdminOrderActionRequest.toCallablePayload(): Map<String, Any?> =
        mapOf(
            "orderId" to orderId,
            "action" to action.wireName,
            "reason" to reason,
            "forcedStatus" to forcedStatus,
            "responsibleRole" to responsibleRole,
        )

    private fun AdminLiveOrderActionRequest.toCallablePayload(): Map<String, Any?> =
        mapOf(
            "orderId" to orderId,
            "action" to action.wireName,
            "expectedVersion" to expectedVersion,
            "reason" to reason,
        )

    private fun Any?.asText(): String = this as? String ?: ""

    private fun Any?.asActionList(): List<AdminOrderAction> =
        (this as? List<*>).orEmpty().mapNotNull { AdminOrderAction.fromWire(it.orEmptyText()) }

    private fun Any?.orEmptyText(): String = this?.toString().orEmpty()

    private companion object {
        const val REGION = "southamerica-east1"
        const val ORDERS = "orders"
        const val ADMIN_ORDER_ACTION = "adminOrderAction"
        const val OPERATE_LIVE_ORDER = "operateLiveOrder"
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
        const val EVENTS = "events"
        const val EVENT_TYPE = "type"
        const val EVENT_SUMMARY = "summary"
        const val ACTOR_ROLE = "actorRole"
        const val REASON = "reason"
        const val SOURCE = "source"
        const val REQUEST_TYPE = "requestType"
        const val STORE_NAME = "storeName"
        const val CREATED_AT = "createdAt"
        const val UPDATED_AT = "updatedAt"
        const val TOTAL = "total"
        const val PAYMENT_METHOD = "paymentMethod"
        const val AMOUNT_TO_COLLECT = "amountToCollect"
        const val COLLECTED_AMOUNT = "collectedAmount"
        const val COLLECTION_REQUIRED = "collectionRequired"
        const val CASH_RESPONSIBLE_ROLE = "cashResponsibleRole"
        const val FINANCIAL_NOTES = "financialNotes"
        const val ITEMS = "items"
        const val ITEM_NAME = "name"
        const val ITEM_QTY = "quantity"
        const val CUSTOMER = "customer"
        const val NAME = "name"
        const val ORDER_TYPE = "orderType"
        const val FINANCIAL_STATUS = "financialStatus"
        const val COMMUNICATION_STATUS = "communicationStatus"
        const val AI_RISK_LEVEL = "aiRiskLevel"
        const val AI_CLASSIFICATION = "aiClassification"
        const val AI_SUGGESTED_ACTION = "aiSuggestedAction"
        const val AI_REQUIRES_HUMAN_REVIEW = "aiRequiresHumanReview"
        const val INCIDENT_STATUS = "incidentStatus"
        const val ARCHIVE_STATUS = "archiveStatus"
        const val CURRENT_RESPONSIBLE_ROLE = "currentResponsibleRole"
        const val ASSIGNED_ACTOR_ID = "assignedActorId"
        const val ASSIGNED_ACTOR_ROLE = "assignedActorRole"
        const val VERSION = "version"
        const val IDEMPOTENCY_KEY = "idempotencyKey"
    }
}
