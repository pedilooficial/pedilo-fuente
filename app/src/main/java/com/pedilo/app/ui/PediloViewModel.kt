package com.pedilo.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pedilo.app.data.PediloRepository
import com.pedilo.app.domain.Order
import com.pedilo.app.domain.OrderDraft
import com.pedilo.app.domain.OrderEvent
import com.pedilo.app.domain.UserProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PediloUiState(
    val isLoadingOperator: Boolean = true,
    val isSubmittingOrder: Boolean = false,
    val isSigningIn: Boolean = false,
    val operatorProfile: UserProfile? = null,
    val orders: List<Order> = emptyList(),
    val selectedOrder: Order? = null,
    val selectedEvents: List<OrderEvent> = emptyList(),
    val publicOrderId: String? = null,
    val error: String? = null
)

enum class PublicOrderUiPhase {
    Idle,
    Editing,
    Submitting,
    Success,
    Error
}

class PediloViewModel(
    private val repository: PediloRepository
) : ViewModel() {
    private val _state = MutableStateFlow(PediloUiState())
    val state: StateFlow<PediloUiState> = _state

    private var profileJob: Job? = null
    private var ordersJob: Job? = null
    private var eventsJob: Job? = null

    init {
        attachOperatorSession()
    }

    fun createPublicOrder(draft: OrderDraft) = viewModelScope.launch {
        if (_state.value.isSubmittingOrder) return@launch
        _state.update { it.copy(isSubmittingOrder = true, error = null) }
        runCatching { repository.createPublicOrder(draft) }
            .onSuccess { orderId ->
                _state.update {
                    it.copy(
                        isSubmittingOrder = false,
                        publicOrderId = orderId,
                        error = null
                    )
                }
            }
            .onFailure { showError(it) }
    }

    fun signInOperator(email: String, password: String) = viewModelScope.launch {
        if (_state.value.isSigningIn) return@launch
        _state.update { it.copy(isSigningIn = true, error = null) }
        runCatching { repository.signInOperator(email, password) }
            .onSuccess {
                _state.update { it.copy(isSigningIn = false) }
                attachOperatorSession()
            }
            .onFailure { showError(it) }
    }

    fun signOutOperator() = viewModelScope.launch {
        repository.signOutOperator()
        profileJob?.cancel()
        ordersJob?.cancel()
        eventsJob?.cancel()
        _state.value = PediloUiState(isLoadingOperator = false)
    }

    fun runAction(orderId: String, action: String, note: String? = null) = viewModelScope.launch {
        runCatching { repository.runOrderAction(orderId, action, note) }
            .onFailure { showError(it) }
    }

    fun assignDriver(orderId: String, driverId: String) = viewModelScope.launch {
        runCatching { repository.assignDriver(orderId, driverId) }
            .onFailure { showError(it) }
    }

    fun adminSetStatus(orderId: String, toStatus: String, note: String? = null) = viewModelScope.launch {
        runCatching { repository.adminSetStatus(orderId, toStatus, note) }
            .onFailure { showError(it) }
    }

    fun selectOrder(order: Order?) {
        eventsJob?.cancel()
        _state.update { it.copy(selectedOrder = order, selectedEvents = emptyList()) }
        if (order == null) return
        eventsJob = viewModelScope.launch {
            repository.observeEvents(order.id)
                .catch { showError(it) }
                .collect { events ->
                    _state.update { it.copy(selectedEvents = events) }
                }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun attachOperatorSession() {
        val uid = repository.currentOperatorId
        if (uid == null) {
            _state.update { it.copy(isLoadingOperator = false, operatorProfile = null, orders = emptyList()) }
            return
        }
        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            repository.observeOperatorProfile(uid)
                .catch { showError(it) }
                .collect { profile ->
                    _state.update {
                        it.copy(
                            isLoadingOperator = false,
                            operatorProfile = profile,
                            orders = if (profile == null) emptyList() else it.orders
                        )
                    }
                    if (profile != null) attachOrders(profile)
                }
        }
    }

    private fun attachOrders(profile: UserProfile) {
        ordersJob?.cancel()
        ordersJob = viewModelScope.launch {
            repository.observeLiveOrders(profile)
                .catch { showError(it) }
                .collect { orders ->
                    _state.update { it.copy(orders = orders) }
                }
        }
    }

    private fun showError(error: Throwable) {
        _state.update {
            it.copy(
                isLoadingOperator = false,
                isSubmittingOrder = false,
                isSigningIn = false,
                error = error.message ?: "No se pudo completar la acción."
            )
        }
    }
}

class PediloViewModelFactory(
    private val repository: PediloRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PediloViewModel(repository) as T
    }
}
