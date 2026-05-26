package com.pedilo.app.ui.publicuser

import android.content.Context
import com.pedilo.app.core.model.TeamRole
import com.pedilo.app.core.model.TeamSession

private const val PREFS_NAME = "pedilo_team_session"
private const val KEY_ROLE = "team_role"

class TeamSessionStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun readPersistedSession(): TeamSession? {
        val roleName = prefs.getString(KEY_ROLE, null) ?: return null
        val role = TeamRole.entries.firstOrNull { it.wireName == roleName } ?: return null
        return TeamSession(role = role, keepSignedIn = true)
    }

    fun save(session: TeamSession) {
        if (session.keepSignedIn) {
            prefs.edit().putString(KEY_ROLE, session.role.wireName).apply()
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
