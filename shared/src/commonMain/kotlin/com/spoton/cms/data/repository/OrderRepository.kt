package com.spoton.cms.data.repository

import com.spoton.cms.data.remote.SpotOnApi
import com.spoton.cms.domain.model.Order
import com.spoton.cms.domain.model.OrderStatus

class OrderRepository(private val api: SpotOnApi) {

    suspend fun getOrders(
        page: Int = 1,
        perPage: Int = 20,
        status: OrderStatus? = null
    ): Result<List<Order>> {
        return try {
            val orders = api.getOrders(page, perPage, status)
            Result.success(orders.map { enrichOrder(it) })
        } catch (e: Exception) {
            // Fallback for demo mode
            Result.success(listOf(
                enrichOrder(Order(101, "ORD-101", OrderStatus.PROCESSING, "2026-05-01 10:30", "45.00", "€", billing = com.spoton.cms.domain.model.OrderAddress("John", "Doe"), paymentMethodTitle = "PayPal", lineItems = listOf(com.spoton.cms.domain.model.OrderLineItem(1, "Krill (5kg)", 1, 2, "90.00")))),
                enrichOrder(Order(102, "ORD-102", OrderStatus.PENDING, "2026-05-01 09:15", "122.50", "€", billing = com.spoton.cms.domain.model.OrderAddress("Jane", "Smith"), paymentMethodTitle = "Credit Card", isB2B = true, paymentTerms = "Net 30", dueDate = "2026-06-01")),
                enrichOrder(Order(103, "ORD-103", OrderStatus.COMPLETED, "2026-04-30 14:00", "89.99", "€", billing = com.spoton.cms.domain.model.OrderAddress("Bob", "Fisher"), paymentMethodTitle = "Bank Transfer")),
                enrichOrder(Order(104, "ORD-104", OrderStatus.PROCESSING, "2026-05-01 12:45", "12.50", "€", billing = com.spoton.cms.domain.model.OrderAddress("Sam", "Angler"), paymentMethodTitle = "PayPal"))
            ))
        }
    }

    suspend fun getOrder(id: Long): Result<Order> {
        return try {
            Result.success(enrichOrder(api.getOrder(id)))
        } catch (e: Exception) {
            // Fallback for demo mode
            getOrders().map { it.first { o -> o.id == id } }
        }
    }

    private fun enrichOrder(order: Order): Order {
        if (order.isB2B) return order

        val totalWeight = order.lineItems.sumOf { extractWeightInKg(it.name) * it.quantity }
        val billingWeight = totalWeight * 1.1 // 10% overhead
        
        val rates = getMockRates(billingWeight, order.shipping?.country ?: "NL")
        
        return order.copy(
            billingWeightKg = billingWeight,
            carrierRates = rates,
            bestRate = rates.minByOrNull { it.price }?.copy(isBestValue = true)
        )
    }

    private fun extractWeightInKg(name: String): Double {
        val lower = name.lowercase()
        val regex = "([0-9.]+)\\s*(kg|g)".toRegex()
        val match = regex.find(lower)
        return if (match != null) {
            val value = match.groupValues[1].toDoubleOrNull() ?: 1.0
            val unit = match.groupValues[2]
            if (unit == "g") value / 1000.0 else value
        } else {
            1.0
        }
    }

    private fun getMockRates(weight: Double, country: String): List<com.spoton.cms.domain.model.CarrierRate> {
        val isDomestic = country.lowercase().contains("netherlands") || country.lowercase() == "nl"
        val basePostNL = if (isDomestic) 6.95 else 13.50
        val baseDHL = if (isDomestic) 5.95 else 11.50
        
        return listOf(
            com.spoton.cms.domain.model.CarrierRate("postnl", "PostNL", if (isDomestic) "Standaard" else "EU Pack", basePostNL + (weight * 0.5), if (isDomestic) "Next Day" else "2-4 Days"),
            com.spoton.cms.domain.model.CarrierRate("dhl", "DHL Express", "DHL For You", baseDHL + (weight * 0.4), "1-2 Days")
        )
    }

    suspend fun updateOrderStatus(id: Long, status: OrderStatus): Result<Order> {
        return try {
            Result.success(enrichOrder(api.updateOrderStatus(id, status)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun settleInvoice(id: Long): Result<Order> {
        return updateOrderStatus(id, OrderStatus.COMPLETED) // Mock settling as completed
    }
}
