package com.spoton.cms.navigation.components

import com.arkivanov.decompose.ComponentContext
import com.spoton.cms.data.repository.BookkeepingRepository
import com.spoton.cms.data.repository.BookkeepingSummary
import com.spoton.cms.db.Expense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BookkeepingComponent(
    componentContext: ComponentContext,
    val onBack: () -> Unit
) : ComponentContext by componentContext, KoinComponent {

    private val repository: BookkeepingRepository by inject()
    private val imagePicker: com.spoton.cms.util.ImagePicker by inject()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    data class State(
        val summary: BookkeepingSummary? = null,
        val expenses: List<Expense> = emptyList(),
        val isLoading: Boolean = false,
        val message: String? = null,
        val showAddExpenseDialog: Boolean = false,
        val newExpenseAmount: String = "",
        val newExpenseCategory: String = "Algemeen",
        val newExpenseDescription: String = "",
        val newExpenseReceiptUri: String? = null
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        loadData()
        observeImagePicker()
    }

    private fun loadData() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val expensesResult = repository.getExpenses()
            val expenses = expensesResult.getOrDefault(emptyList())
            _state.value = _state.value.copy(expenses = expenses)

            if (expensesResult.isFailure) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    message = "Fout bij laden van uitgaven: ${expensesResult.exceptionOrNull()?.message}"
                )
                return@launch
            }

            val result = repository.getSummary()
            if (result.isSuccess) {
                _state.value = _state.value.copy(
                    summary = result.getOrNull(),
                    isLoading = false
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    message = "Fout bij laden van data: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    private fun observeImagePicker() {
        scope.launch {
            imagePicker.imageFlow.collect { picked ->
                if (picked != null) {
                    _state.value = _state.value.copy(newExpenseReceiptUri = picked.fileName)
                }
            }
        }
    }

    fun setShowAddExpenseDialog(show: Boolean) {
        _state.value = _state.value.copy(showAddExpenseDialog = show)
    }

    fun updateNewExpenseAmount(amount: String) {
        _state.value = _state.value.copy(newExpenseAmount = amount)
    }

    fun updateNewExpenseCategory(category: String) {
        _state.value = _state.value.copy(newExpenseCategory = category)
    }

    fun updateNewExpenseDescription(desc: String) {
        _state.value = _state.value.copy(newExpenseDescription = desc)
    }

    fun pickReceipt() {
        imagePicker.pickImage()
    }

    fun takeReceiptPhoto() {
        imagePicker.takePhoto()
    }

    fun saveExpense() {
        scope.launch {
            val amount = _state.value.newExpenseAmount.toDoubleOrNull()
            if (amount == null) {
                _state.value = _state.value.copy(message = "Ongeldig bedrag.")
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true, showAddExpenseDialog = false)
            
            val result = repository.addExpense(
                amount = amount,
                category = _state.value.newExpenseCategory,
                description = _state.value.newExpenseDescription,
                receiptImageUri = _state.value.newExpenseReceiptUri
            )

            if (result.isSuccess) {
                _state.value = _state.value.copy(
                    newExpenseAmount = "",
                    newExpenseCategory = "Algemeen",
                    newExpenseDescription = "",
                    newExpenseReceiptUri = null,
                    message = "Uitgave succesvol toegevoegd."
                )
                loadData() // Refresh
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    message = "Fout bij toevoegen uitgave: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun deleteExpense(id: Long) {
        scope.launch {
            repository.deleteExpense(id)
            loadData()
        }
    }
    
    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    fun exportToCSV() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val summary = _state.value.summary
            val expenses = _state.value.expenses

            if (summary == null) {
                _state.value = _state.value.copy(isLoading = false, message = "Geen data om te exporteren.")
                return@launch
            }

            val csvBuilder = StringBuilder()
            csvBuilder.appendLine("SpotOn Baits - Boekhouding Export")
            csvBuilder.appendLine("Datum,${kotlinx.datetime.Clock.System.now()}")
            csvBuilder.appendLine()
            
            csvBuilder.appendLine("--- SAMENVATTING ---")
            csvBuilder.appendLine("Bruto Omzet,€${summary.grossRevenue}")
            csvBuilder.appendLine("BTW Afdracht,€${summary.totalVat}")
            csvBuilder.appendLine("Verzendkosten Ontvangen,€${summary.totalShippingCollected}")
            csvBuilder.appendLine("Inkoopkosten (COGS),€${summary.costOfGoodsSold}")
            csvBuilder.appendLine("Mollie Transactiekosten,€${summary.mollieFees}")
            csvBuilder.appendLine("MyParcel Label Kosten,€${summary.myParcelCosts}")
            csvBuilder.appendLine("Handmatige Uitgaven,€${summary.manualExpenses}")
            csvBuilder.appendLine("NETTO WINST,€${summary.netProfit}")
            csvBuilder.appendLine()

            csvBuilder.appendLine("--- UITGAVEN ---")
            csvBuilder.appendLine("Datum,Categorie,Bedrag,Omschrijving")
            expenses.forEach { exp ->
                val dateStr = kotlinx.datetime.Instant.fromEpochMilliseconds(exp.date).toString()
                csvBuilder.appendLine("$dateStr,${exp.category},€${exp.amount},${exp.description ?: ""}")
            }

            // In a real app, you'd use expect/actual to trigger Android's Intent.ACTION_SEND with a FileProvider.
            // For now, we'll display it in the message dialog or a dedicated state.
            _state.value = _state.value.copy(
                isLoading = false,
                message = "CSV gegenereerd (Kopieer dit):\n\n${csvBuilder.toString()}"
            )
        }
    }
}
