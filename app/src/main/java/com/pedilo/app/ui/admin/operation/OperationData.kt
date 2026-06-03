package com.pedilo.app.ui.admin

import com.pedilo.app.core.model.AdminOperationOrderClassification
import com.pedilo.app.core.model.AdminOrderSummary

internal val operationEntries = listOf(
    AdminEntry("Pedidos del día", "Movimiento completo de hoy"),
    AdminEntry("Universo de pedidos", "Todas las lecturas operativas de pedidos"),
    AdminEntry("Repartidores", "Estado operativo de repartidores"),
    AdminEntry("Locales activos", "Estado operativo de locales"),
)

internal val operationUniverses = listOf(
    AdminOperationUniverse(
        key = AdminOperationUniverseKey.Orders,
        title = "Universo de pedidos",
        summary = "Lecturas dinámicas del pedido como entidad central.",
        contextTitle = "Pedidos",
        contextText = "El pedido no vive en una carpeta fija: aparece en cada vista según los datos operativos disponibles.",
        views = listOf(
            AdminOperationView(
                title = "Pedidos del día",
                summary = "Vista dinámica de pedidos recibidos hoy.",
                contextTitle = "Lectura diaria",
                contextText = "Agrupa pedidos por estado operativo actual sin moverlos manualmente.",
                lists = listOf(
                    AdminOperationList("Activos", "Pedidos del día que siguen abiertos.", "No hay pedidos activos para mostrar ahora.", AdminOperationListKind.TodayActive),
                    AdminOperationList("Finalizados", "Pedidos del día cerrados correctamente.", "No hay pedidos finalizados para mostrar ahora.", AdminOperationListKind.TodayFinished),
                    AdminOperationList("Cancelados", "Pedidos del día cerrados sin completar.", "No hay pedidos cancelados para mostrar ahora.", AdminOperationListKind.TodayCancelled),
                    AdminOperationList("Con problemas", "Pedidos del día con señal operativa de problema.", "No hay pedidos con problemas para mostrar ahora.", AdminOperationListKind.TodayWithProblems),
                ),
            ),
            AdminOperationView(
                title = "Pedidos activos",
                summary = "Vista dinámica de pedidos vivos dentro de la operación.",
                contextTitle = "Operación en curso",
                contextText = "Separa momentos del pedido según señales reales disponibles.",
                lists = listOf(
                    AdminOperationList("Esperando local", "Pedidos esperando respuesta del local.", "No hay pedidos esperando local ahora.", AdminOperationListKind.ActiveWaitingStore),
                    AdminOperationList("Preparando", "Pedidos en preparación.", "No hay pedidos en preparación ahora.", AdminOperationListKind.ActivePreparing),
                    AdminOperationList("Esperando repartidor", "Pedidos listos para reparto.", "No hay pedidos esperando repartidor ahora.", AdminOperationListKind.ActiveWaitingDriver),
                    AdminOperationList("En entrega", "Pedidos en camino.", "No hay pedidos en entrega ahora.", AdminOperationListKind.ActiveInDelivery),
                ),
            ),
            AdminOperationView(
                title = "Pedidos con problemas",
                summary = "Vista dinámica de pedidos que requieren revisión.",
                contextTitle = "Casos a revisar",
                contextText = "Clasifica señales operativas sin resolver, cancelar ni cambiar responsables.",
                lists = listOf(
                    AdminOperationList("Local no responde", "Pedidos detenidos por falta de respuesta.", "No hay pedidos con local sin respuesta ahora.", AdminOperationListKind.ProblemStoreNotResponding),
                    AdminOperationList("Reclamo de la persona usuaria", "Casos iniciados por aviso de la persona usuaria.", "No hay reclamos para mostrar ahora.", AdminOperationListKind.ProblemUserClaim),
                    AdminOperationList("Demorados", "Pedidos con señal real de demora.", "No hay pedidos demorados para mostrar ahora.", AdminOperationListKind.ProblemDelayed),
                    AdminOperationList("Sin responsable", "Pedidos sin responsable operativo informado.", "No hay pedidos sin responsable para mostrar ahora.", AdminOperationListKind.ProblemWithoutResponsible),
                ),
            ),
        ),
    ),
    AdminOperationUniverse(
        key = AdminOperationUniverseKey.Drivers,
        title = "Repartidores",
        summary = "Lectura neutra del universo de reparto.",
        contextTitle = "Repartidores",
        contextText = "Aún no hay núcleo real de repartidores conectado en esta capa.",
        views = listOf(
            AdminOperationView(
                title = "Repartidores",
                summary = "Vista dinámica de disponibilidad logística.",
                contextTitle = "Estado de reparto",
                contextText = "Muestra estados neutros hasta contar con datos reales del núcleo de repartidores.",
                lists = listOf(
                    AdminOperationList("En servicio", "Repartidores conectados.", "Dato pendiente: no hay listado real de repartidores conectado.", AdminOperationListKind.DriverInService),
                    AdminOperationList("Disponibles", "Repartidores listos para tomar pedidos.", "Dato pendiente: no hay disponibilidad real conectada.", AdminOperationListKind.DriverAvailable),
                    AdminOperationList("Con incidencias", "Repartidores con señal de revisión.", "Dato pendiente: no hay incidencias reales conectadas.", AdminOperationListKind.DriverWithIncidents),
                ),
            ),
        ),
    ),
    AdminOperationUniverse(
        key = AdminOperationUniverseKey.Stores,
        title = "Locales activos",
        summary = "Lectura neutra del universo de locales.",
        contextTitle = "Locales",
        contextText = "Aún no hay núcleo real de locales conectado en esta capa.",
        views = listOf(
            AdminOperationView(
                title = "Locales activos",
                summary = "Vista dinámica de disponibilidad comercial.",
                contextTitle = "Estado de locales",
                contextText = "Muestra estados neutros hasta contar con datos reales del núcleo de locales.",
                lists = listOf(
                    AdminOperationList("Operando", "Locales recibiendo pedidos.", "Dato pendiente: no hay listado real de locales conectado.", AdminOperationListKind.StoreOperating),
                    AdminOperationList("Pausados", "Locales con operación detenida.", "Dato pendiente: no hay pausas reales conectadas.", AdminOperationListKind.StorePaused),
                    AdminOperationList("Con demoras", "Locales con ritmo afectado.", "Dato pendiente: no hay demoras reales de locales conectadas.", AdminOperationListKind.StoreDelayed),
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
        val sourceLabel = AdminOperationOrderClassification.sourceLabel(it.source, it.requestType)
        AdminOrderDetailEntry(
            label = if (it.trackingNumber.isNotBlank()) it.trackingNumber else "Pedido #____",
            note = "${it.publicStatus.ifBlank { "Pedido recibido" }} · $sourceLabel",
            variant = variant,
            realOrderId = it.id,
        )
    }
}
