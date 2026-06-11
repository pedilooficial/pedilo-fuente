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
    val nextAllowedActions: List<LiveOrderAction>,
    val source: String,
    val requestType: String,
    val storeName: String,
    val createdAtMillis: Long?,
    val total: String,
    val paymentMethod: String = "",
    val amountToCollect: String = "",
    val collectedAmount: String = "",
    val collectionRequired: Boolean = false,
    val cashResponsibleRole: String = "",
    val orderType: String = "",
    val financialStatus: String = "",
    val communicationStatus: String = "",
    val aiRiskLevel: String = "",
    val aiClassification: String = "",
    val aiSuggestedAction: String = "",
    val aiRequiresHumanReview: Boolean = false,
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
    val nextAllowedActions: List<LiveOrderAction>,
    val source: String,
    val requestType: String,
    val storeName: String,
    val customerName: String,
    val createdAtMillis: Long?,
    val updatedAtMillis: Long?,
    val total: String,
    val paymentMethod: String = "",
    val amountToCollect: String = "",
    val collectedAmount: String = "",
    val collectionRequired: Boolean = false,
    val cashResponsibleRole: String = "",
    val financialNotes: String = "",
    val itemsSummary: List<String>,
    val lastEventSummary: String,
    val orderType: String = "",
    val financialStatus: String = "",
    val communicationStatus: String = "",
    val aiRiskLevel: String = "",
    val aiClassification: String = "",
    val aiSuggestedAction: String = "",
    val aiRequiresHumanReview: Boolean = false,
    val incidentStatus: String = "",
    val archiveStatus: String = "",
    val currentResponsibleRole: String = "",
    val assignedActorId: String = "",
    val assignedActorRole: String = "",
    val version: Int = 0,
    val idempotencyKey: String = "",
    val events: List<AdminOrderEvent> = emptyList(),
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

data class AdminOperationalHealthReport(
    val healthStatus: String = "unknown",
    val severity: String = "unknown",
    val generatedAt: String = "",
    val metrics: AdminOperationalHealthMetrics = AdminOperationalHealthMetrics(),
    val modules: List<AdminModuleHealth> = emptyList(),
    val alerts: List<AdminHealthAlert> = emptyList(),
    val criticalEvents: List<AdminCriticalEvent> = emptyList(),
    val auditSummary: AdminAuditSummary = AdminAuditSummary(),
    val securitySignals: List<AdminHealthAlert> = emptyList(),
)

data class AdminOperationalHealthMetrics(
    val liveOrders: Int = 0,
    val pendingReviewOrders: Int = 0,
    val openIncidentOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val closedOrders: Int = 0,
    val failedCommunicationOrders: Int = 0,
    val preparedCommunicationOrders: Int = 0,
    val disabledCommunicationOrders: Int = 0,
    val financialReviewOrders: Int = 0,
    val pendingAiSuggestionOrders: Int = 0,
    val publicClaimsReceived: Int = 0,
    val linkedPublicClaims: Int = 0,
    val unlinkedPublicClaims: Int = 0,
    val requiresAttention: Int = 0,
    val collectOnDeliveryOrders: Int = 0,
    val transferDeclaredPending: Int = 0,
    val paidDeclaredUnconfirmed: Int = 0,
    val collectionPendingOrders: Int = 0,
    val openIncidents: Int = 0,
    val resolvedIncidents: Int = 0,
    val unresolvedIncidents: Int = 0,
    val aiSuggested: Int = 0,
    val aiAccepted: Int = 0,
    val aiRejected: Int = 0,
    val aiNotApplicable: Int = 0,
    val highRiskAi: Int = 0,
    val whatsappDisabled: Boolean = true,
    val pushDisabled: Boolean = true,
    val externalAiDisabled: Boolean = true,
    val engineVersion: String = "",
    val providerStatus: String = "disabled",
)

data class AdminModuleHealth(
    val key: String,
    val label: String,
    val moduleStatus: String,
    val healthStatus: String,
    val severity: String,
    val warningCode: String,
    val warningMessage: String,
)

data class AdminHealthAlert(
    val healthStatus: String,
    val severity: String,
    val scope: String,
    val source: String,
    val metricKey: String,
    val metricValue: String,
    val warningCode: String,
    val warningMessage: String,
    val requiresAdminReview: Boolean,
    val relatedOrderId: String = "",
)

data class AdminCriticalEvent(
    val relatedOrderId: String,
    val source: String,
    val type: String,
    val summary: String,
    val actorRole: String,
    val previousStatus: String,
    val nextStatus: String,
    val severity: String,
)

data class AdminAuditSummary(
    val ordersWithEvents: Int = 0,
    val orderEventRecords: Int = 0,
    val incidentRecords: Int = 0,
    val claimRecords: Int = 0,
    val communicationRecords: Int = 0,
    val aiDecisionRecords: Int = 0,
    val publicClaimRecords: Int = 0,
    val exposesPublicAudit: Boolean = false,
    val correctiveActionsExecuted: Boolean = false,
)

data class AdminTeamUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val role: String,
    val active: Boolean,
    val storeId: String = "",
    val driverId: String = "",
    val updatedAtMillis: Long? = null,
)

data class AdminConfigState(
    val id: String = "real_use",
    val maintenanceMode: Boolean = false,
    val rainMode: Boolean = false,
    val saturationMode: Boolean = false,
    val emergencyMode: Boolean = false,
    val publicOrderingEnabled: Boolean = true,
    val lastUpdatedBy: String = "",
    val updatedAtMillis: Long? = null,
)

data class AdminConfigUpdateRequest(
    val field: String,
    val enabled: Boolean,
)

data class AdminRoleUpdateRequest(
    val uid: String,
    val role: String? = null,
    val active: Boolean? = null,
)

data class AdminMutationResult(
    val message: String,
)
