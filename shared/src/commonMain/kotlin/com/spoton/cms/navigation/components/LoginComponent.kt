package com.spoton.cms.navigation.components

import com.arkivanov.decompose.ComponentContext
import com.spoton.cms.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LoginComponent(
    componentContext: ComponentContext,
    private val onLoginSuccess: () -> Unit
) : ComponentContext by componentContext, KoinComponent {

    private val authRepository: AuthRepository by inject()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    data class State(
        val serverUrl: String = "",
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _state = MutableStateFlow(
        State(
            serverUrl = when {
                authRepository.currentServerUrl.contains(".test") -> "https://backend.spotonbaits.nl"
                authRepository.currentServerUrl.isBlank() -> "https://backend.spotonbaits.nl"
                else -> authRepository.currentServerUrl
            },
            username = "",
            password = ""
        )
    )
    val state: StateFlow<State> = _state.asStateFlow()

    fun onServerUrlChanged(url: String) {
        _state.value = _state.value.copy(serverUrl = url)
    }

    fun onUsernameChanged(username: String) {
        _state.value = _state.value.copy(username = username)
    }

    fun onPasswordChanged(password: String) {
        _state.value = _state.value.copy(password = password)
    }

    fun onLoginClicked() {
        val current = _state.value
        if (current.serverUrl.isBlank() || current.username.isBlank() || current.password.isBlank()) {
            _state.value = current.copy(error = "All fields are required")
            return
        }

        _state.value = current.copy(isLoading = true, error = null)

        scope.launch {
            val result = authRepository.login(
                serverUrl = current.serverUrl,
                username = current.username,
                password = current.password
            )
            result.fold(
                onSuccess = { onLoginSuccess() },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Login failed"
                    )
                }
            )
        }
    }
}
