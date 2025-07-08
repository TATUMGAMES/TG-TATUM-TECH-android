package com.tatumgames.tatumtech.android.ui.components.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tatumgames.tatumtech.android.database.interfaces.EventRegistrationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpcomingEventsViewModel(
    private val repository: EventRegistrationRepository
) : ViewModel() {

    private val _registeredEvents = MutableStateFlow<List<String>>(emptyList())
    val registeredEvents: StateFlow<List<String>> = _registeredEvents.asStateFlow()

    init {
        viewModelScope.launch {
            _registeredEvents.value = repository.getRegisteredEvents()
        }
    }

    fun onRegister(eventId: String) {
        viewModelScope.launch {
            repository.registerEvent(eventId)
            _registeredEvents.value = repository.getRegisteredEvents()
        }
    }

    fun onUnregister(eventId: String) {
        viewModelScope.launch {
            repository.unregisterEvent(eventId)
            _registeredEvents.value = repository.getRegisteredEvents()
        }
    }
} 
