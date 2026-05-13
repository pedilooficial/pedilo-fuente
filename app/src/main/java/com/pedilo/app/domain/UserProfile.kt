package com.pedilo.app.domain

data class UserProfile(
    val id: String,
    val displayName: String,
    val role: UserRole
)
