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

class OrdersComponent(
    componentContext: ComponentContext,
    val onBack: () -> Unit,
    val onOrderSelected: (Long) -> Unit
) : ComponentContext by componentContext, KoinComponent {

    private val orderRepository: OrderRepository by inject()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    data class State(
        val orders: List<Order> = emptyList(),
        val selectedFilter: OrderStatus? = null,
        val isLoading: Boolean = true,
        val error: String? = null
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        loadOrders()
    }

    fun onFilterChanged(status: OrderStatus?) {
        _state.value = _state.value.copy(selectedFilter = status)
        loadOrders(status)
    }

    fun loadOrders(status: OrderStatus? = _state.value.selectedFilter) {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = orderRepository.getOrders(status = status)
            result.fold(
                onSuccess = { orders ->
                    _state.value = _state.value.copy(
                        orders = orders,
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
}
