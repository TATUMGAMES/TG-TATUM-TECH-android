package com.tatumgames.tatumtech.android.ui.components.screens.models

data class Event(
    val id: Long,
    val name: String,
    val host: String,
    val date: String,
    val durationHours: Int,
    val location: String,
    val isRegistrationOpen: Boolean,
    val attendees: List<Attendee>,
    val featuredImage: String
)
