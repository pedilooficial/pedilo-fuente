package com.pedilo.app.ui.admin

internal enum class AdminOperationUniverseKey {
    Orders,
    Drivers,
    Stores,
}

internal enum class AdminOperationListKind {
    TodayActive,
    TodayFinished,
    TodayCancelled,
    TodayWithProblems,
    ActiveWaitingStore,
    ActivePreparing,
    ActiveWaitingDriver,
    ActiveInDelivery,
    ProblemStoreNotResponding,
    ProblemUserClaim,
    ProblemDelayed,
    ProblemWithoutResponsible,
    DriverInService,
    DriverAvailable,
    DriverWithIncidents,
    StoreOperating,
    StorePaused,
    StoreDelayed,
}

internal data class AdminOperationUniverse(
    val key: AdminOperationUniverseKey,
    val title: String,
    val summary: String,
    val contextTitle: String,
    val contextText: String,
    val views: List<AdminOperationView>,
)

internal data class AdminOperationView(
    val title: String,
    val summary: String,
    val contextTitle: String,
    val contextText: String,
    val lists: List<AdminOperationList>,
)

internal data class AdminOperationList(
    val title: String,
    val summary: String,
    val emptyText: String,
    val kind: AdminOperationListKind,
)
