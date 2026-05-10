package com.spoton.cms.navigation.components

import com.arkivanov.decompose.ComponentContext
import com.spoton.cms.data.repository.StoreSettingsRepository
import com.spoton.cms.domain.model.LabeledValue
import com.spoton.cms.domain.model.StoreSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsComponent(
    componentContext: ComponentContext,
    val onBack: () -> Unit
) : ComponentContext by componentContext, KoinComponent {

    private val repository: StoreSettingsRepository by inject()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    enum class Tab {
        COMPANY, CONTACT, LEGAL, SHIPPING, INTEGRATIONS, BOOKKEEPING, SYSTEM
    }

    enum class ShippingSubTab {
        CARRIERS, DIMENSIONS, RULES
    }

    data class State(
        val settings: StoreSettings = StoreSettings(),
        val selectedTab: Tab = Tab.COMPANY,
        val selectedShippingSubTab: ShippingSubTab = ShippingSubTab.CARRIERS,
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val message: String? = null,
        val generatedEnv: String? = null
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val settings = repository.getSettings()
                _state.value = _state.value.copy(
                    settings = settings,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    message = "Error loading settings: ${e.message}"
                )
            }
        }
    }

    fun selectTab(tab: Tab) {
        _state.value = _state.value.copy(selectedTab = tab)
    }

    fun selectShippingSubTab(subTab: ShippingSubTab) {
        _state.value = _state.value.copy(selectedShippingSubTab = subTab)
    }

    // Shipping Helpers
    fun addDimension() {
        val newDim = com.spoton.cms.domain.model.PackageDimension(
            id = kotlinx.datetime.Clock.System.now().toEpochMilliseconds().toString(),
            name = "Nieuw Pakket",
            length = 0.0,
            width = 0.0,
            height = 0.0,
            weight = 0.0
        )
        updateSettings { it.copy(shipping = it.shipping.copy(dimensions = it.shipping.dimensions + newDim)) }
    }

    fun removeDimension(id: String) {
        updateSettings { it.copy(shipping = it.shipping.copy(dimensions = it.shipping.dimensions.filter { d -> d.id != id })) }
    }

    fun addShippingRule() {
        val newRule = com.spoton.cms.domain.model.ShippingRule(
            id = kotlinx.datetime.Clock.System.now().toEpochMilliseconds().toString(),
            destination = "NL",
            carrierId = "postnl"
        )
        updateSettings { it.copy(shipping = it.shipping.copy(rules = it.shipping.rules + newRule)) }
    }

    fun removeShippingRule(id: String) {
        updateSettings { it.copy(shipping = it.shipping.copy(rules = it.shipping.rules.filter { r -> r.id != id })) }
    }

    fun moveRuleUp(index: Int) {
        if (index <= 0) return
        val rules = _state.value.settings.shipping.rules.toMutableList()
        val item = rules.removeAt(index)
        rules.add(index - 1, item)
        updateSettings { it.copy(shipping = it.shipping.copy(rules = rules)) }
    }

    fun moveRuleDown(index: Int) {
        val rules = _state.value.settings.shipping.rules.toMutableList()
        if (index >= rules.size - 1) return
        val item = rules.removeAt(index)
        rules.add(index + 1, item)
        updateSettings { it.copy(shipping = it.shipping.copy(rules = rules)) }
    }

    fun updateSettings(update: (StoreSettings) -> StoreSettings) {
        _state.value = _state.value.copy(
            settings = update(_state.value.settings)
        )
    }

    fun saveSettings() {
        scope.launch {
            _state.value = _state.value.copy(isSaving = true)
            try {
                repository.saveSettings(_state.value.settings)
                _state.value = _state.value.copy(
                    isSaving = false,
                    message = "Settings saved successfully!"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    message = "Error saving settings: ${e.message}"
                )
            }
        }
    }

    fun clearHostingerCache() {
        scope.launch {
            _state.value = _state.value.copy(isSaving = true)
            try {
                val hostingerClient: com.spoton.cms.data.remote.HostingerApiClient by inject()
                val result = hostingerClient.clearCache()
                if (result.isSuccess) {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        message = "Hostinger server cache cleared successfully!"
                    )
                } else {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        message = result.exceptionOrNull()?.message ?: "Failed to clear cache"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    message = "Error: ${e.message}"
                )
            }
        }
    }

    fun generateEnv() {
        val s = _state.value.settings
        val env = """
            # ─── Mollie ──────────────────────────────────────────────────────────
            MOLLIE_API_KEY=${s.integrations.mollieApiKey}
            
            # ─── WooCommerce ─────────────────────────────────────────────────────
            NEXT_PUBLIC_WC_SITE_URL=${s.integrations.wcUrl}
            WC_CONSUMER_KEY=${s.integrations.wcConsumerKey}
            WC_CONSUMER_SECRET=${s.integrations.wcConsumerSecret}
            
            # ─── MyParcel ────────────────────────────────────────────────────────
            MYPARCEL_API_KEY=${s.integrations.myParcelApiKey}
            
            # ─── System ──────────────────────────────────────────────────────────
            NEXT_PUBLIC_MAINTENANCE_MODE=${s.system.maintenanceMode}
            NEXT_PUBLIC_PRODUCTS_DISABLED=${s.system.productsDisabled}
            
            # Frontend FTP
            FTP_HOST=${s.system.ftpHost}
            FTP_USERNAME=${s.system.ftpUsername}
            FTP_PASSWORD=${s.system.ftpPassword}
            FTP_PORT=${s.system.ftpPort}
            
            # Backend FTP
            FTP_BACKEND_HOST=${s.system.ftpBackendHost}
            FTP_BACKEND_USERNAME=${s.system.ftpBackendUsername}
            FTP_BACKEND_PASSWORD=${s.system.ftpBackendPassword}
            FTP_BACKEND_PORT=${s.system.ftpBackendPort}
        """.trimIndent()
        
        _state.value = _state.value.copy(generatedEnv = env)
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
    
    fun clearEnv() {
        _state.value = _state.value.copy(generatedEnv = null)
    }
}
