package com.pedilo.app.ui.admin

import com.pedilo.app.core.model.AdminOperationOrderClassification
import com.pedilo.app.core.model.AdminOrderSummary

internal val operationEntries = listOf(
    AdminEntry("Hoy", "Resumen temporal"),
    AdminEntry("Pedidos", "En curso, cerrados o con revisión"),
    AdminEntry("Repartidores", "Estado del equipo de reparto"),
    AdminEntry("Locales", "Estado de los locales activos"),
)

internal val operationUniverses = listOf(
    AdminOperationUniverse(
        key = AdminOperationUniverseKey.Orders,
        title = "Pedidos",
        summary = "Movimiento operativo.",
        contextTitle = "Pedidos",
        contextText = "Estado actual de pedidos.",
        views = listOf(
            AdminOperationView(
                title = "Hoy",
                summary = "Movimiento de pedidos ingresados hoy.",
                contextTitle = "Hoy",
                contextText = "Movimiento de pedidos ingresados hoy.",
                lists = listOf(
                    AdminOperationList("Ingresaron hoy", "Todos los pedidos ingresados hoy.", "Sin pedidos", AdminOperationListKind.TodayAll),
                    AdminOperationList("Activos de hoy", "Pedidos ingresados hoy que siguen activos.", "Sin pedidos", AdminOperationListKind.TodayActive),
                    AdminOperationList("Problemas de hoy", "Pedidos ingresados hoy que requieren revisión.", "Sin casos", AdminOperationListKind.TodayProblems),
                    AdminOperationList("Cerrados de hoy", "Pedidos ingresados hoy que ya cerraron.", "Sin pedidos", AdminOperationListKind.TodayClosed),
                ),
            ),
            AdminOperationView(
                title = "Activos",
                summary = "Pedidos que todavía siguen en curso.",
                contextTitle = "Activos",
                contextText = "Pedidos que todavía siguen en curso.",
                lists = listOf(
                    AdminOperationList("Esperando local", "Necesitan respuesta del local.", "Sin pedidos", AdminOperationListKind.ActiveWaitingStore),
                    AdminOperationList("Preparando", "El local está preparando.", "Sin pedidos", AdminOperationListKind.ActivePreparing),
                    AdminOperationList("Buscando repartidor", "Listos para asignar o retirar.", "Sin pedidos", AdminOperationListKind.ActiveWaitingDriver),
                    AdminOperationList("En camino", "Ya están camino al destino.", "Sin pedidos", AdminOperationListKind.ActiveInDelivery),
                ),
            ),
            AdminOperationView(
                title = "Problemas",
                summary = "Casos que necesitan revisión.",
                contextTitle = "Problemas",
                contextText = "Casos que necesitan revisión.",
                lists = listOf(
                    AdminOperationList("Local no responde", "Pedidos detenidos por falta de respuesta.", "Sin casos", AdminOperationListKind.ProblemStoreNotResponding),
                    AdminOperationList("Reclamo de cliente", "Casos avisados por el cliente.", "Sin casos", AdminOperationListKind.ProblemUserClaim),
                    AdminOperationList("Demorados", "Pedidos que superaron el tiempo esperado.", "Sin casos", AdminOperationListKind.ProblemDelayed),
                    AdminOperationList("Sin responsable", "Pedidos que necesitan responsable.", "Sin casos", AdminOperationListKind.ProblemWithoutResponsible),
                ),
            ),
            AdminOperationView(
                title = "Cerrados",
                summary = "Pedidos que ya terminaron.",
                contextTitle = "Cerrados",
                contextText = "Pedidos que ya terminaron.",
                lists = listOf(
                    AdminOperationList("Finalizados", "Pedidos cerrados correctamente.", "Sin pedidos", AdminOperationListKind.ClosedFinished),
                    AdminOperationList("Cancelados", "Pedidos cerrados sin completar.", "Sin pedidos", AdminOperationListKind.ClosedCancelled),
                ),
            ),
        ),
    ),
    AdminOperationUniverse(
        key = AdminOperationUniverseKey.Drivers,
        title = "Repartidores",
        summary = "Estado del equipo de reparto.",
        contextTitle = "Repartidores",
        contextText = "Estado del equipo de reparto.",
        views = listOf(
            AdminOperationView(
                title = "Repartidores",
                summary = "Estado del equipo de reparto.",
                contextTitle = "Estado de reparto",
                contextText = "Sin datos.",
                lists = listOf(
                    AdminOperationList("En servicio", "Repartidores conectados.", "Sin datos.", AdminOperationListKind.DriverInService),
                    AdminOperationList("Disponibles", "Listos para tomar pedidos.", "Sin datos.", AdminOperationListKind.DriverAvailable),
                    AdminOperationList("Con incidencias", "Necesitan revisión.", "Sin datos.", AdminOperationListKind.DriverWithIncidents),
                ),
            ),
        ),
    ),
    AdminOperationUniverse(
        key = AdminOperationUniverseKey.Stores,
        title = "Locales",
        summary = "Estado de los locales activos.",
        contextTitle = "Locales",
        contextText = "Estado de los locales activos.",
        views = listOf(
            AdminOperationView(
                title = "Locales",
                summary = "Estado de los locales activos.",
                contextTitle = "Estado de locales",
                contextText = "Sin datos.",
                lists = listOf(
                    AdminOperationList("Operando", "Locales recibiendo pedidos.", "Sin datos.", AdminOperationListKind.StoreOperating),
                    AdminOperationList("Pausados", "Operación detenida.", "Sin datos.", AdminOperationListKind.StorePaused),
                    AdminOperationList("Con demoras", "Ritmo afectado.", "Sin datos.", AdminOperationListKind.StoreDelayed),
                ),
            ),
        ),
    ),
)

internal fun orderDetailEntriesFor(
    listKind: AdminOperationListKind,
    orders: List<AdminOrderSummary>,
): List<AdminOrderDetailEntry> {
    val variant = when {
        listKind in setOf(
            AdminOperationListKind.TodayProblems,
            AdminOperationListKind.ProblemStoreNotResponding,
            AdminOperationListKind.ProblemUserClaim,
            AdminOperationListKind.ProblemDelayed,
            AdminOperationListKind.ProblemWithoutResponsible,
        ) -> OperationOrderVariant.WithProblem
        listKind == AdminOperationListKind.ActiveWaitingStore -> OperationOrderVariant.NeedsAttention
        else -> OperationOrderVariant.Normal
    }
    return orders.map {
        val identity = AdminOperationOrderClassification.operationalIdentity(it.source, it.requestType)
        val function = AdminOperationOrderClassification.operationalFunction(it.source, it.requestType)
        val status = it.adminHumanOperationStatus()
        val secondary = it.storeName.ifBlank { function }
        AdminOrderDetailEntry(
            label = if (it.trackingNumber.isNotBlank()) "Pedido #${it.trackingNumber}" else "Pedido #____",
            note = listOf(identity, status, function, secondary)
                .distinct()
                .joinToString(" · "),
            variant = variant,
            realOrderId = it.id,
        )
    }
}

private fun AdminOrderSummary.adminHumanOperationStatus(): String {
    val visible = "$publicStatus $operationalStatus"
    return when {
        visible.contains("local no responde", ignoreCase = true) ||
            visible.contains("sin respuesta", ignoreCase = true) -> "Local sin respuesta"
        visible.contains("demora", ignoreCase = true) ||
            visible.contains("retras", ignoreCase = true) -> "Demorado"
        visible.contains("preparando", ignoreCase = true) -> "Preparando"
        visible.contains("esperando repartidor", ignoreCase = true) -> "Buscando repartidor"
        visible.contains("en entrega", ignoreCase = true) -> "En camino"
        status.equals("delivered", ignoreCase = true) -> "Entregado"
        status.equals("cancelled", ignoreCase = true) || status.equals("canceled", ignoreCase = true) -> "Cancelado"
        needsAttention || activeIncident -> "Con problema"
        publicStatus.isNotBlank() -> publicStatus
        else -> "Sin datos"
    }
}
