package com.tatumgames.tatumtech.android.ui.components.screens.events.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tatumgames.tatumtech.android.database.interfaces.EventRegistrationInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpcomingEventsViewModel(
    private val repository: EventRegistrationInterface
) : ViewModel() {

    private val _registeredEvents = MutableStateFlow<List<Long>>(emptyList())
    val registeredEvents: StateFlow<List<Long>> = _registeredEvents.asStateFlow()

    init {
        viewModelScope.launch {
            _registeredEvents.value = repository.getRegisteredEvents()
        }
    }

    fun onRegister(id: Long) {
        viewModelScope.launch {
            repository.registerEvent(id)
            _registeredEvents.value = repository.getRegisteredEvents()
        }
    }

    fun onUnregister(id: Long) {
        viewModelScope.launch {
            repository.unregisterEvent(id)
            _registeredEvents.value = repository.getRegisteredEvents()
        }
    }
}
