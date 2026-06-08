package com.pedilo.app.core.model

enum class LiveOrderType(val wireName: String) {
    LocalOrder("local_order"),
    DirectPurchase("direct_purchase"),
    PickupShipping("pickup_shipping"),
}

enum class LiveOrderOperationalStatus(val wireName: String) {
    WaitingAdminReview("waiting_admin_review"),
    LocalAccepted("local_accepted"),
    Preparing("preparing"),
    ReadyForPickup("ready_for_pickup"),
    DriverAssigned("driver_assigned"),
    PickedUp("picked_up"),
    Delivered("delivered"),
    AdminReviewed("admin_reviewed"),
    AdminIntervention("admin_intervention"),
    IncidentOpen("incident_open"),
    IncidentResolved("incident_resolved"),
}

enum class LiveOrderFinancialStatus(val wireName: String) {
    PendingReview("pending_review"),
}

enum class LiveOrderCommunicationStatus(val wireName: String) {
    Received("received"),
}

enum class LiveOrderIncidentStatus(val wireName: String) {
    None("none"),
}

enum class LiveOrderArchiveStatus(val wireName: String) {
    Live("live"),
    Archived("archived"),
}

enum class LiveOrderAction(val wireName: String) {
    LocalAccept("local_accept"),
    LocalReject("local_reject"),
    LocalMarkPreparing("local_mark_preparing"),
    LocalMarkReady("local_mark_ready"),
    DriverTake("driver_take"),
    DriverMarkPickedUp("driver_mark_picked_up"),
    DriverMarkDelivered("driver_mark_delivered"),
    CancelOrder("cancel_order"),
    OpenIncident("open_incident"),
    ResolveIncident("resolve_incident"),
    AdminIntervene("admin_intervene"),
}

data class LiveOrderBirthState(
    val orderType: String,
    val operationalStatus: String,
    val financialStatus: String,
    val communicationStatus: String,
    val incidentStatus: String,
    val archiveStatus: String,
    val currentResponsibleRole: String,
    val assignedActorId: String,
    val version: Int,
    val idempotencyKey: String,
)
