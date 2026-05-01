package com.spoton.cms.navigation.components

import com.arkivanov.decompose.ComponentContext
import com.spoton.cms.data.repository.ProductRepository
import com.spoton.cms.domain.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ProductsComponent(
    componentContext: ComponentContext,
    val onBack: () -> Unit,
    val onProductSelected: (Long) -> Unit
) : ComponentContext by componentContext, KoinComponent {

    private val productRepository: ProductRepository by inject()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    data class State(
        val products: List<Product> = emptyList(),
        val searchQuery: String = "",
        val selectedIds: Set<Long> = emptySet(),
        val isLoading: Boolean = true,
        val isProcessing: Boolean = false,
        val error: String? = null
    ) {
        val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        loadProducts()
    }

    fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        loadProducts(search = query.ifBlank { null })
    }

    fun loadProducts(search: String? = null) {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = productRepository.getProducts(search = search)
            result.fold(
                onSuccess = { products ->
                    _state.value = _state.value.copy(
                        products = products,
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
    fun toggleSelection(id: Long) {
        val current = _state.value.selectedIds
        _state.value = _state.value.copy(
            selectedIds = if (current.contains(id)) current - id else current + id
        )
    }

    fun clearSelection() {
        _state.value = _state.value.copy(selectedIds = emptySet())
    }

    fun bulkUpdatePrices(percentage: Float) {
        scope.launch {
            _state.value = _state.value.copy(isProcessing = true)
            val result = productRepository.bulkUpdatePrices(_state.value.selectedIds.toList(), percentage)
            result.fold(
                onSuccess = {
                    clearSelection()
                    loadProducts()
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(isProcessing = false, error = e.message)
                }
            )
        }
    }

    fun bulkUpdateCategory(categoryId: Long) {
        scope.launch {
            _state.value = _state.value.copy(isProcessing = true)
            val result = productRepository.bulkUpdateCategory(_state.value.selectedIds.toList(), categoryId)
            result.fold(
                onSuccess = {
                    clearSelection()
                    loadProducts()
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(isProcessing = false, error = e.message)
                }
            )
        }
    }
}
