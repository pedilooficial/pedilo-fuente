package com.pedilo.app.core.model

data class AdminOrderSummary(
    val id: String,
    val trackingNumber: String,
    val publicOrderNumber: String,
    val status: String,
    val publicStatus: String,
    val operationalStatus: String,
    val responsibleRole: String,
    val priority: String,
    val needsAttention: Boolean,
    val activeIncident: Boolean,
    val nextAllowedActions: List<AdminOrderAction>,
    val source: String,
    val requestType: String,
    val storeName: String,
    val createdAtMillis: Long?,
    val total: String,
    val orderType: String = "",
    val financialStatus: String = "",
    val communicationStatus: String = "",
    val incidentStatus: String = "",
    val archiveStatus: String = "",
    val currentResponsibleRole: String = "",
    val assignedActorId: String = "",
    val assignedActorRole: String = "",
    val version: Int = 0,
    val idempotencyKey: String = "",
)

data class AdminOrderDetail(
    val id: String,
    val trackingNumber: String,
    val publicOrderNumber: String,
    val status: String,
    val publicStatus: String,
    val operationalStatus: String,
    val responsibleRole: String,
    val priority: String,
    val needsAttention: Boolean,
    val activeIncident: Boolean,
    val nextAllowedActions: List<AdminOrderAction>,
    val source: String,
    val requestType: String,
    val storeName: String,
    val customerName: String,
    val createdAtMillis: Long?,
    val updatedAtMillis: Long?,
    val total: String,
    val itemsSummary: List<String>,
    val lastEventSummary: String,
    val orderType: String = "",
    val financialStatus: String = "",
    val communicationStatus: String = "",
    val incidentStatus: String = "",
    val archiveStatus: String = "",
    val currentResponsibleRole: String = "",
    val assignedActorId: String = "",
    val assignedActorRole: String = "",
    val version: Int = 0,
    val idempotencyKey: String = "",
)

enum class AdminOrderAction(
    val wireName: String,
    val label: String,
    val impact: String,
    val requiresReason: Boolean = false,
) {
    MarkAdminReviewed(
        wireName = "mark_admin_reviewed",
        label = "Marcar revisado",
        impact = "Registra que Admin vio el pedido y baja la atención pendiente si no hay incidencia activa.",
    ),
    ConfirmIntervention(
        wireName = "confirm_intervention",
        label = "Confirmar intervención",
        impact = "Deja constancia de intervención operativa y asigna seguimiento a Admin.",
    ),
    MarkIncident(
        wireName = "mark_incident",
        label = "Marcar incidencia",
        impact = "Abre incidencia operativa simple, sube prioridad y mantiene el pedido bajo atención.",
        requiresReason = true,
    ),
    ResolveIncident(
        wireName = "resolve_incident",
        label = "Resolver incidencia",
        impact = "Cierra la incidencia activa y devuelve el pedido a seguimiento operativo.",
        requiresReason = true,
    ),
    CancelByAdmin(
        wireName = "cancel_by_admin",
        label = "Cancelar pedido",
        impact = "Cierra el pedido como cancelado por Admin y lo quita del flujo activo.",
        requiresReason = true,
    ),
    ForceStatus(
        wireName = "force_status",
        label = "Forzar estado permitido",
        impact = "Aplica un cambio de estado sólo dentro del contrato operativo permitido.",
        requiresReason = true,
    ),
    AssignResponsible(
        wireName = "assign_responsible",
        label = "Asignar responsable Admin",
        impact = "Asigna el seguimiento operativo del pedido al rol Admin.",
    ),
    ClearResponsible(
        wireName = "clear_responsible",
        label = "Limpiar responsable",
        impact = "Quita responsable operativo cuando el contrato lo permite.",
    );

    companion object {
        fun fromWire(value: String): AdminOrderAction? =
            entries.firstOrNull { it.wireName == value.trim() }
    }
}

data class AdminOrderActionRequest(
    val orderId: String,
    val action: AdminOrderAction,
    val reason: String = "",
    val forcedStatus: String = "",
    val responsibleRole: String = "",
)

data class AdminOrderActionResult(
    val orderId: String,
    val status: String,
    val publicStatus: String,
    val operationalStatus: String,
    val responsibleRole: String,
    val priority: String,
    val needsAttention: Boolean,
    val activeIncident: Boolean,
    val nextAllowedActions: List<AdminOrderAction>,
    val eventSummary: String,
    val humanMessage: String,
)
