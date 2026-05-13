package com.pedilo.app.domain

enum class OrderStatus(val wireName: String) {
    Created("created"),
    AssignedToDriver("assigned_to_driver"),
    PickedUp("picked_up"),
    OnTheWay("on_the_way"),
    Delivered("delivered"),
    Cancelled("cancelled"),
    Problem("problem");

    companion object {
        fun fromWire(value: String?): OrderStatus? =
            entries.firstOrNull { it.wireName == value }
    }
}
