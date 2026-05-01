package com.spoton.cms.navigation.components

import com.arkivanov.decompose.ComponentContext
import com.spoton.cms.data.repository.OrderRepository
import com.spoton.cms.data.repository.ProductRepository
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

class DashboardComponent(
    componentContext: ComponentContext,
    val onNavigateToProducts: () -> Unit,
    val onNavigateToOrders: () -> Unit,
    val onNavigateToInventory: () -> Unit,
    val onNavigateToArticles: () -> Unit,
    val onNavigateToStyles: () -> Unit,
    val onLogout: () -> Unit
) : ComponentContext by componentContext, KoinComponent {

    private val orderRepository: OrderRepository by inject()
    private val productRepository: ProductRepository by inject()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    data class State(
        val recentOrders: List<Order> = emptyList(),
        val pendingOrderCount: Int = 0,
        val totalProducts: Int = 0,
        val isLoading: Boolean = true,
        val error: String? = null
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val ordersResult = orderRepository.getOrders(perPage = 5)
                val pendingResult = orderRepository.getOrders(
                    perPage = 1,
                    status = OrderStatus.PROCESSING
                )
                val productsResult = productRepository.getProducts(perPage = 1)

                _state.value = _state.value.copy(
                    recentOrders = ordersResult.getOrDefault(emptyList()),
                    pendingOrderCount = pendingResult.getOrDefault(emptyList()).size,
                    totalProducts = productsResult.getOrDefault(emptyList()).size,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
