package com.spoton.cms.navigation.components

import com.arkivanov.decompose.ComponentContext
import com.spoton.cms.data.repository.OrderRepository
import com.spoton.cms.domain.model.Order
import com.spoton.cms.domain.model.OrderStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OrderDetailComponent(
    componentContext: ComponentContext,
    private val orderId: Long,
    val onBack: () -> Unit
) : ComponentContext by componentContext, KoinComponent {

    private val orderRepository: OrderRepository by inject()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    data class State(
        val order: Order? = null,
        val isLoading: Boolean = true,
        val isUpdating: Boolean = false,
        val error: String? = null,
        val updateSuccess: Boolean = false
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        loadOrder()
    }

    private fun loadOrder() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = orderRepository.getOrder(orderId)
            result.fold(
                onSuccess = { order ->
                    _state.value = _state.value.copy(
                        order = order,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            )
        }
    }

    fun updateStatus(newStatus: OrderStatus) {
        scope.launch {
            _state.value = _state.value.copy(isUpdating = true, updateSuccess = false)
            val result = orderRepository.updateOrderStatus(orderId, newStatus)
            result.fold(
                onSuccess = { order ->
                    _state.value = _state.value.copy(
                        order = order,
                        isUpdating = false,
                        updateSuccess = true,
                        error = null
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isUpdating = false,
                        error = e.message
                    )
                }
            )
        }
    }

    fun settleInvoice() {
        scope.launch {
            _state.value = _state.value.copy(isUpdating = true, updateSuccess = false)
            val result = orderRepository.settleInvoice(orderId)
            result.fold(
                onSuccess = { order ->
                    _state.value = _state.value.copy(
                        order = order,
                        isUpdating = false,
                        updateSuccess = true,
                        error = null
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isUpdating = false,
                        error = e.message
                    )
                }
            )
        }
    }
}
