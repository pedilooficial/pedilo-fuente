package com.pedilo.app.ui.admin

internal enum class AdminOperationUniverseKey {
    Orders,
    Drivers,
    Stores,
}

internal enum class AdminOperationListKind {
    TodayAll,
    TodayActive,
    TodayProblems,
    TodayClosed,
    TodayReview,
    Unclassified,
    ClosedFinished,
    ClosedCancelled,
    ActiveWaitingStore,
    ActivePreparing,
    ActiveWaitingDriver,
    ActiveInDelivery,
    ActiveReviewState,
    ProblemStoreNotResponding,
    ProblemUserClaim,
    ProblemDelayed,
    ProblemWithoutResponsible,
    ProblemOperationalReview,
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
