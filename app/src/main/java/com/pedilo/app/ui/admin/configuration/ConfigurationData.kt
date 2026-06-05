package com.pedilo.app.ui.admin

internal val configurationEntries = listOf(
    "Público",
    "Locales",
    "Reparto",
    "Marketplace",
    "Pedidos",
    "Precios",
    "Cobros",
    "Mensajes",
    "Reglas",
    "Notificaciones",
    "Métricas",
    "Auditoría",
    "Emergencias",
    "General",
).map { AdminEntry(it, "Abrir tablero") }
