package com.spoton.cms.navigation.components

import com.arkivanov.decompose.ComponentContext
import com.mohamedrejeb.richeditor.model.RichTextState
import com.spoton.cms.data.repository.ProductRepository
import com.spoton.cms.domain.model.Product
import com.spoton.cms.ui.components.editor.HtmlRichTextConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ProductDetailComponent(
    componentContext: ComponentContext,
    private val productId: Long,
    val onBack: () -> Unit
) : ComponentContext by componentContext, KoinComponent {

    private val productRepository: ProductRepository by inject()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val htmlConverter = HtmlRichTextConverter()

    val descriptionState = RichTextState()

    data class State(
        val product: Product? = null,
        val isLoading: Boolean = true,
        val isSaving: Boolean = false,
        val error: String? = null,
        val saveSuccess: Boolean = false
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        loadProduct()
    }

    private fun loadProduct() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = productRepository.getProduct(productId)
            result.fold(
                onSuccess = { product ->
                    _state.value = _state.value.copy(
                        product = product,
                        isLoading = false,
                        error = null
                    )
                    // Load description into editor
                    htmlConverter.fromInput(product.description, descriptionState)
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

    fun updateStock(quantity: Int) {
        scope.launch {
            _state.value = _state.value.copy(isSaving = true, saveSuccess = false)
            val result = productRepository.updateStock(productId, quantity)
            result.fold(
                onSuccess = { product ->
                    _state.value = _state.value.copy(
                        product = product,
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

    fun updateProduct(updates: Map<String, Any>) {
        scope.launch {
            _state.value = _state.value.copy(isSaving = true, saveSuccess = false)
            val result = productRepository.updateProduct(productId, updates)
            result.fold(
                onSuccess = { product ->
                    _state.value = _state.value.copy(
                        product = product,
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

    fun saveDescription() {
        val html = htmlConverter.toOutput(descriptionState)
        updateProduct(mapOf("description" to html))
    }

    fun updateMetadata(key: String, value: String) {
        val product = _state.value.product ?: return
        val currentMeta = product.metaData.toMutableList()
        val index = currentMeta.indexOfFirst { it.key == key }
        if (index >= 0) {
            currentMeta[index] = currentMeta[index].copy(value = value)
        } else {
            currentMeta.add(com.spoton.cms.domain.model.ProductMeta(key = key, value = value))
        }
        updateProduct(mapOf("meta_data" to currentMeta))
    }
}
