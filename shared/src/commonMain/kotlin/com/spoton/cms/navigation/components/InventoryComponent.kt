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

/**
 * Inventory management component with barcode scanner integration.
 * When a barcode/QR code is scanned, it looks up the product by SKU
 * and opens a quick-edit stock panel.
 */
class InventoryComponent(
    componentContext: ComponentContext,
    val onBack: () -> Unit,
    private val onProductSelected: (Long) -> Unit
) : ComponentContext by componentContext, KoinComponent {

    private val productRepository: ProductRepository by inject()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    data class State(
        val products: List<Product> = emptyList(),
        val scannedProduct: Product? = null,
        val isScannerOpen: Boolean = false,
        val isLoading: Boolean = true,
        val isSaving: Boolean = false,
        val searchQuery: String = "",
        val selectedCategory: String = "All",
        val error: String? = null,
        val saveSuccess: Boolean = false
    ) {
        val filteredProducts: List<Product> get() = products.filter { product ->
            val matchesSearch = searchQuery.isBlank() || 
                product.name.contains(searchQuery, true) || 
                product.sku.contains(searchQuery, true)
            val matchesCategory = selectedCategory == "All" || 
                product.categories.any { it.name == selectedCategory }
            matchesSearch && matchesCategory
        }

        val categories: List<String> get() = listOf("All") + products.flatMap { p -> p.categories.map { it.name } }.distinct().sorted()

        val totalValue: Double get() = products.sumOf { (it.stockQuantity ?: 0) * (it.price.toDoubleOrNull() ?: 0.0) }
        val lowStockCount: Int get() = products.count { (it.stockQuantity ?: 0) <= it.threshold && (it.stockQuantity ?: 0) > 0 }
        val outOfStockCount: Int get() = products.count { (it.stockQuantity ?: 0) <= 0 }
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = productRepository.getProducts(perPage = 50)
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

    fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun onCategorySelected(category: String) {
        _state.value = _state.value.copy(selectedCategory = category)
    }

    fun toggleScanner() {
        _state.value = _state.value.copy(
            isScannerOpen = !_state.value.isScannerOpen,
            scannedProduct = null
        )
    }

    fun onBarcodeScanned(barcode: String) {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, isScannerOpen = false)
            val result = productRepository.getProductBySku(barcode)
            result.fold(
                onSuccess = { product ->
                    _state.value = _state.value.copy(
                        scannedProduct = product,
                        isLoading = false,
                        error = if (product == null) "No product found for SKU: $barcode" else null
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

    fun updateStock(productId: Long, quantity: Int) {
        scope.launch {
            _state.value = _state.value.copy(isSaving = true, saveSuccess = false)
            val result = productRepository.updateStock(productId, quantity)
            result.fold(
                onSuccess = { updated ->
                    _state.value = _state.value.copy(
                        scannedProduct = updated,
                        products = _state.value.products.map {
                            if (it.id == productId) updated else it
                        },
                        isSaving = false,
                        saveSuccess = true,
                        error = null
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        error = e.message
                    )
                }
            )
        }
    }

    fun clearScannedProduct() {
        _state.value = _state.value.copy(scannedProduct = null, saveSuccess = false)
    }

    fun onProductClicked(id: Long) {
        onProductSelected(id)
    }
}
