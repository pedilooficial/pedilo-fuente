package com.pedilo.app.ui.admin

internal val configurationEntries = listOf(
    "Usuario público",
    "Locales",
    "Catálogo y productos",
    "Pedidos",
    "Comunicación",
    "Operación",
    "Reglas y validaciones",
    "Auditoría",
    "Emergencias",
    "General",
).map { AdminEntry(it, "Ajustes preparados para la app") }
