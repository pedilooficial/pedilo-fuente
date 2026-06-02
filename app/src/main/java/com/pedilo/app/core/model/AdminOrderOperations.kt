package com.pedilo.app.core.model

object AdminOrderOperations {
    private val terminalStatuses = setOf("cancelled", "canceled", "delivered", "closed", "archived")

    fun allowedActions(
        status: String,
        adminReviewed: Boolean,
        activeIncident: Boolean,
        responsibleRole: String,
    ): List<AdminOrderAction> {
        val normalizedStatus = status.trim().lowercase()
        if (normalizedStatus in terminalStatuses) return emptyList()

        return buildList {
            if (!adminReviewed) add(AdminOrderAction.MarkAdminReviewed)
            add(AdminOrderAction.ConfirmIntervention)
            if (activeIncident) add(AdminOrderAction.ResolveIncident) else add(AdminOrderAction.MarkIncident)
            add(AdminOrderAction.CancelByAdmin)
            add(AdminOrderAction.ForceStatus)
            if (responsibleRole.isBlank()) add(AdminOrderAction.AssignResponsible) else add(AdminOrderAction.ClearResponsible)
        }
    }
}
