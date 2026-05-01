package com.spoton.cms.navigation.components

import com.arkivanov.decompose.ComponentContext
import com.spoton.cms.data.repository.StyleRepository
import com.spoton.cms.domain.model.StyleConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class StylesComponent(
    componentContext: ComponentContext,
    val onBack: () -> Unit
) : ComponentContext by componentContext, KoinComponent {

    private val styleRepository: StyleRepository by inject()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    data class State(
        val config: StyleConfig = StyleConfig(),
        val isLoading: Boolean = true,
        val isSaving: Boolean = false,
        val error: String? = null,
        val saveSuccess: Boolean = false
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        loadStyles()
    }

    private fun loadStyles() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = styleRepository.getStyleConfig()
            result.fold(
                onSuccess = { config ->
                    _state.value = _state.value.copy(
                        config = config,
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

    fun updateConfig(config: StyleConfig) {
        _state.value = _state.value.copy(config = config)
    }

    fun saveStyles() {
        scope.launch {
            _state.value = _state.value.copy(isSaving = true, saveSuccess = false)
            val result = styleRepository.updateStyleConfig(_state.value.config)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
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
}
