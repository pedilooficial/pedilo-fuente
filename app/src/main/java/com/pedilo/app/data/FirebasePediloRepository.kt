package com.pedilo.app.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.pedilo.app.domain.Order
import com.pedilo.app.domain.OrderDraft
import com.pedilo.app.domain.OrderEvent
import com.pedilo.app.domain.OrderItem
import com.pedilo.app.domain.OrderStatus
import com.pedilo.app.domain.UserProfile
import com.pedilo.app.domain.UserRole
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebasePediloRepository : PediloRepository {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val functions = Firebase.functions

    override val currentOperatorId: String?
        get() = auth.currentUser?.uid

    override suspend fun createPublicOrder(draft: OrderDraft): String {
        val payload = mapOf(
            "requesterName" to draft.requesterName.trim(),
            "itemsText" to draft.itemsText.trim(),
            "deliveryAddress" to draft.deliveryAddress.trim(),
            "contactPhone" to draft.contactPhone.trim(),
            "note" to draft.note.trim()
        )
        val result = functions.getHttpsCallable("createOrder").call(payload).await()
        val data = result.getData() as? Map<*, *>
        return data?.get("orderId") as? String
            ?: error("El pedido fue procesado, pero no se recibió el número de seguimiento.")
    }

    override suspend fun signInOperator(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    override suspend fun signOutOperator() {
        auth.signOut()
    }

    override fun observeOperatorProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val registration = db.collection(USERS).document(userId).addSnapshotListener { snap, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val data = snap?.data
            if (data == null) {
                trySend(null)
                return@addSnapshotListener
            }
            val role = UserRole.fromWire(data["role"] as? String)
            if (role == null) {
                close(IllegalStateException("Perfil operativo inválido. Falta un rol válido."))
                return@addSnapshotListener
            }
            trySend(
                UserProfile(
                    id = userId,
                    displayName = data["displayName"] as? String ?: "Operador",
                    role = role
                )
            )
        }
        awaitClose { registration.remove() }
    }

    override fun observeLiveOrders(profile: UserProfile): Flow<List<Order>> = callbackFlow {
        val query = when (profile.role) {
            UserRole.Store -> db.collection(ORDERS)
                .whereEqualTo("storeId", profile.id)
                .whereIn("status", LIVE_STATUSES)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
            UserRole.Driver -> db.collection(ORDERS)
                .whereEqualTo("driverId", profile.id)
                .whereIn("status", LIVE_STATUSES)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
            UserRole.Admin -> db.collection(ORDERS)
                .whereIn("status", LIVE_STATUSES)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
        }
        val registration = query.addSnapshotListener { snap, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            runCatching { snap.toOrders() }
                .onSuccess { trySend(it) }
                .onFailure { close(it) }
        }
        awaitClose { registration.remove() }
    }

    override fun observeEvents(orderId: String): Flow<List<OrderEvent>> = callbackFlow {
        val registration = db.document("$ORDERS/$orderId").collection(EVENTS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                runCatching { snap.toEvents() }
                    .onSuccess { trySend(it) }
                    .onFailure { close(it) }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun runOrderAction(orderId: String, action: String, note: String?) {
        functions.getHttpsCallable("transitionOrder").call(
            mapOf(
                "orderId" to orderId,
                "action" to action,
                "note" to note.orEmpty()
            )
        ).await()
    }

    override suspend fun assignDriver(orderId: String, driverId: String) {
        functions.getHttpsCallable("assignDriver").call(
            mapOf("orderId" to orderId, "driverId" to driverId.trim())
        ).await()
    }

    override suspend fun adminSetStatus(orderId: String, toStatus: String, note: String?) {
        functions.getHttpsCallable("adminSetStatus").call(
            mapOf(
                "orderId" to orderId,
                "toStatus" to toStatus,
                "note" to note.orEmpty()
            )
        ).await()
    }

    private fun QuerySnapshot?.toOrders(): List<Order> =
        this?.documents.orEmpty().map { it.toOrder() }

    private fun DocumentSnapshot.toOrder(): Order {
        val data = data.orEmpty()
        @Suppress("UNCHECKED_CAST")
        val actionMap = (data["availableActionsByRole"] as? Map<String, Any>).orEmpty()
            .mapValues { (_, value) -> (value as? List<*>)?.filterIsInstance<String>().orEmpty() }
        @Suppress("UNCHECKED_CAST")
        val rawItems = data["items"] as? List<Map<String, Any>>
        val status = OrderStatus.fromWire(data["status"] as? String)
            ?: error("Pedido $id tiene un estado inválido.")
        return Order(
            id = id,
            status = status,
            requesterName = data["requesterName"] as? String ?: "",
            items = rawItems.orEmpty().map {
                OrderItem(
                    name = it["name"] as? String ?: "",
                    quantity = (it["quantity"] as? Number)?.toInt() ?: 1,
                    note = it["note"] as? String ?: ""
                )
            },
            deliveryAddress = data["deliveryAddress"] as? String ?: "",
            contactPhone = data["contactPhone"] as? String ?: "",
            note = data["note"] as? String ?: "",
            driverId = data["driverId"] as? String,
            problemNote = data["problemNote"] as? String,
            availableActionsByRole = actionMap,
            adminAllowedStatuses = (data["adminAllowedStatuses"] as? List<*>)
                ?.filterIsInstance<String>()
                .orEmpty(),
            createdAt = data["createdAt"] as? Timestamp,
            updatedAt = data["updatedAt"] as? Timestamp
        )
    }

    private fun QuerySnapshot?.toEvents(): List<OrderEvent> = this?.documents.orEmpty().map { doc ->
        val data = doc.data.orEmpty()
        OrderEvent(
            id = doc.id,
            actorId = data["actorId"] as? String ?: "",
            actorRole = data["actorRole"] as? String ?: "",
            type = data["type"] as? String ?: "",
            fromStatus = OrderStatus.fromWire(data["fromStatus"] as? String),
            toStatus = OrderStatus.fromWire(data["toStatus"] as? String),
            note = data["note"] as? String,
            createdAt = data["createdAt"] as? Timestamp
        )
    }

    private companion object {
        const val USERS = "users"
        const val ORDERS = "orders"
        const val EVENTS = "events"
        val LIVE_STATUSES = listOf(
            "created",
            "assigned_to_driver",
            "picked_up",
            "on_the_way",
            "problem"
        )
    }
}
