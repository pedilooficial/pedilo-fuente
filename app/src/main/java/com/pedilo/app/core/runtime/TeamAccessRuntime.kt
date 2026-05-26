package com.pedilo.app.core.runtime

import com.pedilo.app.core.model.TeamLoginRequest
import com.pedilo.app.core.model.TeamLoginResult
import com.pedilo.app.core.port.TeamAccessPort

fun teamAccessPort(): TeamAccessPort = ClosedTeamAccessPort()

private class ClosedTeamAccessPort : TeamAccessPort {
    override suspend fun login(request: TeamLoginRequest): TeamLoginResult =
        TeamLoginResult.MissingSecureProvider
}
