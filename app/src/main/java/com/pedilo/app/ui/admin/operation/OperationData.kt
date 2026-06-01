package com.pedilo.app.ui.admin

import com.pedilo.app.core.model.AdminOperationOrderClassification
import com.pedilo.app.core.model.AdminOrderSummary

internal val operationEntries = listOf(
    AdminEntry("Pedidos del día", "Movimiento completo de hoy"),
    AdminEntry("Pedidos activos", "Pedidos que siguen en curso"),
    AdminEntry("Pedidos con problemas", "Casos que necesitan revisión"),
    AdminEntry("Repartidores activos", "Estado operativo de repartidores"),
    AdminEntry("Locales activos", "Estado operativo de locales"),
)

internal val todayOrdersCategories = listOf(
    AdminTodayOrdersCategory(
        title = "Activos",
        summary = "Pedidos del día que siguen en curso",
        contextText = "Pedidos recibidos que siguen abiertos dentro del día.",
        entries = listOf(AdminEntry("Esperando local", "Pedidos creados esperando respuesta del local")),
    ),
    AdminTodayOrdersCategory(
        title = "Finalizados",
        summary = "Pedidos del día cerrados correctamente",
        contextText = "Pedidos entregados, retirados o enviados dentro del día.",
        entries = listOf(
            AdminEntry("Entregados", "Pedidos completados exitosamente"),
            AdminEntry("Retirados", "Retirados en local"),
            AdminEntry("Enviados", "Completados por envío"),
        ),
    ),
    AdminTodayOrdersCategory(
        title = "Cancelados",
        summary = "Pedidos del día cerrados sin completar",
        contextText = "Pedidos cancelados antes de completarse.",
        entries = listOf(
            AdminEntry("Cancelados por cliente", "Iniciados por la persona usuaria"),
            AdminEntry("Cancelados por local", "Iniciados por el comercio"),
            AdminEntry("Cancelados por operación", "Iniciados por el equipo"),
        ),
    ),
    AdminTodayOrdersCategory(
        title = "Demorados",
        summary = "Pedidos del día con tiempo excedido",
        contextText = "Pedidos con tiempo de espera excedido en alguna etapa.",
        entries = listOf(
            AdminEntry("Esperando local", "Espera de aceptación"),
            AdminEntry("Preparando", "Espera de preparación"),
            AdminEntry("En entrega", "Espera de reparto"),
        ),
    ),
    AdminTodayOrdersCategory(
        title = "Con problemas",
        summary = "Pedidos del día marcados con incidencia",
        contextText = "Pedidos con incidencias que requieren revisión operativa.",
        entries = listOf(
            AdminEntry("Local no responde", "Requiere seguimiento"),
            AdminEntry("Reclamo del cliente", "Requiere revisión"),
        ),
    ),
)

internal val operationSections = listOf(
    AdminOperationSection("Pedidos del día", "Movimiento completo de pedidos del día.", "Vista del día", "Agrupa los estados principales sin abrir pedidos ni resolver casos.", listOf(
        AdminEntry("Activos", "Pedidos del día que siguen en curso"),
        AdminEntry("Finalizados", "Pedidos cerrados correctamente"),
        AdminEntry("Cancelados", "Pedidos cerrados sin completar"),
        AdminEntry("Demorados", "Pedidos con tiempo excedido"),
        AdminEntry("Con problemas", "Pedidos marcados con incidencia"),
    )),
    AdminOperationSection("Pedidos activos", "Pedidos vivos dentro de la operación actual.", "Operación en curso", "Ordena los momentos del pedido sin abrir acciones finales.", listOf(
        AdminEntry("Esperando local", "Pedidos esperando respuesta del local"),
        AdminEntry("Preparando", "Pedidos en preparación"),
        AdminEntry("Esperando repartidor", "Pedidos listos para asignación"),
        AdminEntry("En entrega", "Pedidos en camino"),
    )),
    AdminOperationSection("Pedidos con problemas", "Clasificación inicial de casos que requieren atención.", "Casos a revisar", "Separa motivos para lectura operativa sin cerrar incidencias.", listOf(
        AdminEntry("Local no responde", "Pedidos detenidos por falta de respuesta"),
        AdminEntry("Reclamo del cliente", "Casos iniciados por aviso del cliente"),
    )),
    AdminOperationSection("Repartidores activos", "Estado operativo de repartidores.", "Equipo en movimiento", "Agrupa disponibilidad sin tocar perfiles, permisos ni asignaciones.", listOf(
        AdminEntry("Libres", "Repartidores disponibles"),
        AdminEntry("Ocupados", "Repartidores con pedido asignado"),
        AdminEntry("Pendientes de respuesta", "Casos esperando confirmación"),
        AdminEntry("Con incidencia", "Situaciones que requieren revisión"),
    )),
    AdminOperationSection("Locales activos", "Estado operativo de locales.", "Locales en operación", "Ordena señales operativas sin tocar locales, productos ni visibilidad.", listOf(
        AdminEntry("Vendiendo ahora", "Locales disponibles para recibir pedidos"),
        AdminEntry("Sin respuesta", "Locales que no respondieron a tiempo"),
        AdminEntry("Pausados", "Locales temporalmente detenidos"),
        AdminEntry("Con configuración pendiente", "Locales con datos por revisar"),
        AdminEntry("Sin productos vendibles", "Locales sin oferta disponible"),
    )),
)

internal fun orderDetailEntriesFor(
    sectionTitle: String,
    subsectionTitle: String,
    orders: List<AdminOrderSummary>,
): List<AdminOrderDetailEntry> {
    val real = orders.firstOrNull()
    val variant = when {
        sectionTitle == "Pedidos con problemas" && subsectionTitle == "Local no responde" -> OperationOrderVariant.WithProblem
        sectionTitle == "Pedidos activos" && subsectionTitle == "Esperando repartidor" -> OperationOrderVariant.ActionUnavailable
        sectionTitle == "Pedidos activos" && subsectionTitle == "Esperando local" -> OperationOrderVariant.NeedsAttention
        else -> OperationOrderVariant.Normal
    }
    return real?.let {
        val sourceLabel = AdminOperationOrderClassification.sourceLabel(it.source, it.requestType)
        listOf(
            AdminOrderDetailEntry(
                label = if (it.trackingNumber.isNotBlank()) it.trackingNumber else "Pedido #____",
                note = "${it.publicStatus.ifBlank { "Pedido recibido" }} · $sourceLabel",
                variant = variant,
                realOrderId = it.id,
            ),
        )
    } ?: emptyList()
}
