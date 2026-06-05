package com.pedilo.app.ui.admin

import com.pedilo.app.core.model.AdminOperationOrderClassification
import com.pedilo.app.core.model.AdminOrderSummary

internal val operationEntries = listOf(
    AdminEntry("Pedidos del día", "Resumen de hoy"),
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
                title = "Pedidos del día",
                summary = "Todo lo que ingresó hoy.",
                contextTitle = "Pedidos del día",
                contextText = "Todo lo que ingresó hoy.",
                lists = listOf(
                    AdminOperationList("Activos", "Pedidos que siguen abiertos.", "Sin pedidos por ahora.", AdminOperationListKind.TodayActive),
                    AdminOperationList("Finalizados", "Pedidos cerrados correctamente.", "Sin pedidos por ahora.", AdminOperationListKind.TodayFinished),
                    AdminOperationList("Cancelados", "Pedidos cerrados sin completar.", "Sin pedidos por ahora.", AdminOperationListKind.TodayCancelled),
                    AdminOperationList("Con problemas", "Pedidos del día que requieren revisión.", "Sin casos por ahora.", AdminOperationListKind.TodayWithProblems),
                ),
            ),
            AdminOperationView(
                title = "Pedidos activos",
                summary = "Pedidos que todavía siguen en curso.",
                contextTitle = "Pedidos activos",
                contextText = "Pedidos que todavía siguen en curso.",
                lists = listOf(
                    AdminOperationList("Esperando local", "Necesitan respuesta del local.", "Sin pedidos por ahora.", AdminOperationListKind.ActiveWaitingStore),
                    AdminOperationList("Preparando", "El local está preparando.", "Sin pedidos por ahora.", AdminOperationListKind.ActivePreparing),
                    AdminOperationList("Esperando repartidor", "Listos para asignar o retirar.", "Sin pedidos por ahora.", AdminOperationListKind.ActiveWaitingDriver),
                    AdminOperationList("En entrega", "Ya están camino al destino.", "Sin pedidos por ahora.", AdminOperationListKind.ActiveInDelivery),
                ),
            ),
            AdminOperationView(
                title = "Pedidos con problemas",
                summary = "Casos que necesitan revisión.",
                contextTitle = "Pedidos con problemas",
                contextText = "Casos que necesitan revisión.",
                lists = listOf(
                    AdminOperationList("Local no responde", "Pedidos detenidos por falta de respuesta.", "Sin casos por ahora.", AdminOperationListKind.ProblemStoreNotResponding),
                    AdminOperationList("Reclamo de cliente", "Casos avisados por el cliente.", "Sin casos por ahora.", AdminOperationListKind.ProblemUserClaim),
                    AdminOperationList("Demorados", "Pedidos que superaron el tiempo esperado.", "Sin casos por ahora.", AdminOperationListKind.ProblemDelayed),
                    AdminOperationList("Sin responsable", "Pedidos que necesitan responsable.", "Sin casos por ahora.", AdminOperationListKind.ProblemWithoutResponsible),
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
            AdminOperationListKind.TodayWithProblems,
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
        val secondary = it.storeName.ifBlank { function }
        AdminOrderDetailEntry(
            label = if (it.trackingNumber.isNotBlank()) "Pedido #${it.trackingNumber}" else "Pedido #____",
            note = listOf(identity, function, secondary)
                .distinct()
                .joinToString(" · "),
            variant = variant,
            realOrderId = it.id,
        )
    }
}
