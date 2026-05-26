package com.pedilo.app.core.model

enum class TeamRole(val wireName: String, val screenTitle: String) {
    Admin("admin", "Pantalla Admin"),
    Local("local", "Pantalla Local"),
    Driver("repartidor", "Pantalla Repartidor"),
}

data class TeamSession(
    val role: TeamRole,
    val keepSignedIn: Boolean,
)

data class TeamLoginRequest(
    val user: String,
    val secret: String,
    val keepSignedIn: Boolean,
)

sealed interface TeamLoginResult {
    data class Success(val session: TeamSession) : TeamLoginResult
    data object NoAccess : TeamLoginResult
    data object MissingSecureProvider : TeamLoginResult
}
