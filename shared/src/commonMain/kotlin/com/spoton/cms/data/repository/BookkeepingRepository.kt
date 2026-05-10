package com.spoton.cms.data.repository

import com.spoton.cms.data.remote.SpotOnApi
import com.spoton.cms.db.Expense
import com.spoton.cms.db.SpotOnDatabase
import com.spoton.cms.domain.model.BookkeepingSettings
import com.spoton.cms.domain.model.Order
import com.spoton.cms.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

data class BookkeepingSummary(
    val grossRevenue: Double,
    val totalVat: Double,
    val totalShippingCollected: Double,
    val costOfGoodsSold: Double,
    val mollieFees: Double,
    val myParcelCosts: Double,
    val manualExpenses: Double,
    val netProfit: Double
)

class BookkeepingRepository(
    private val api: SpotOnApi,
    private val database: SpotOnDatabase,
    private val settingsRepository: StoreSettingsRepository
) {
    suspend fun getSummary(timeRangeMillis: Long? = null): Result<BookkeepingSummary> {
        return try {
            val settings = settingsRepository.getSettings().bookkeeping
            val allOrders = api.getOrders(perPage = 100, status = OrderStatus.COMPLETED)
            
            // Filter by time if needed (simplified for now)
            // In a real scenario, you'd query WC API with after/before dates.
            
            val expenses = if (timeRangeMillis != null) {
                val now = Clock.System.now().toEpochMilliseconds()
                database.spotOnDatabaseQueries.getExpensesByDateRange(now - timeRangeMillis, now).executeAsList()
            } else {
                database.spotOnDatabaseQueries.getAllExpenses().executeAsList()
            }

            var grossRevenue = 0.0
            var totalVat = 0.0
            var totalShippingCollected = 0.0
            var cogs = 0.0
            var mollieFees = 0.0
            var myParcelCosts = 0.0

            allOrders.forEach { order ->
                val total = order.total.toDoubleOrNull() ?: 0.0
                grossRevenue += total
                totalVat += order.totalTax.toDoubleOrNull() ?: 0.0
                totalShippingCollected += order.shippingTotal.toDoubleOrNull() ?: 0.0

                // Mollie fee per order
                mollieFees += settings.mollieFixedFee + (total * settings.molliePercentageFee)
                
                // MyParcel label cost (assume 1 label per order for now)
                myParcelCosts += settings.myParcelLabelCost

                // COGS (Mocked fetching product cost price here. In reality, requires fetching product details or having it in order metadata)
                // For this iteration, we'll estimate COGS as 30% of subtotal if not available.
                // A complete implementation would map order.lineItems to product.costPrice.
                val subtotal = (order.total.toDoubleOrNull() ?: 0.0) - (order.shippingTotal.toDoubleOrNull() ?: 0.0) - (order.totalTax.toDoubleOrNull() ?: 0.0)
                cogs += subtotal * 0.30 
            }

            val manualExpenses = expenses.sumOf { it.amount }

            val netProfit = grossRevenue - totalVat - cogs - mollieFees - myParcelCosts - manualExpenses

            Result.success(
                BookkeepingSummary(
                    grossRevenue = grossRevenue,
                    totalVat = totalVat,
                    totalShippingCollected = totalShippingCollected,
                    costOfGoodsSold = cogs,
                    mollieFees = mollieFees,
                    myParcelCosts = myParcelCosts,
                    manualExpenses = manualExpenses,
                    netProfit = netProfit
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addExpense(amount: Double, category: String, description: String, receiptImageUri: String?): Result<Unit> {
        return try {
            database.spotOnDatabaseQueries.insertExpense(
                amount = amount,
                category = category,
                description = description,
                date = Clock.System.now().toEpochMilliseconds(),
                receiptImageUri = receiptImageUri
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExpenses(): Result<List<Expense>> {
        return try {
            Result.success(database.spotOnDatabaseQueries.getAllExpenses().executeAsList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteExpense(id: Long) {
        database.spotOnDatabaseQueries.deleteExpense(id)
    }
}
