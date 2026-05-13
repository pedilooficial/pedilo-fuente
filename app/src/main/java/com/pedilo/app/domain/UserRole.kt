package com.pedilo.app.domain

enum class UserRole(val wireName: String) {
    Store("store"),
    Driver("driver"),
    Admin("admin");

    companion object {
        fun fromWire(value: String?): UserRole? =
            entries.firstOrNull { it.wireName == value }
    }
}
