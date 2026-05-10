package com.spoton.cms.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: Long,
    val number: String = "",
    val status: OrderStatus = OrderStatus.PENDING,
    @SerialName("date_created")
    val dateCreated: String = "",
    val total: String = "0.00",
    @SerialName("total_tax")
    val totalTax: String = "0.00",
    @SerialName("shipping_total")
    val shippingTotal: String = "0.00",
    @SerialName("currency_symbol")
    val currencySymbol: String = "€",
    val billing: OrderAddress? = null,
    val shipping: OrderAddress? = null,
    @SerialName("line_items")
    val lineItems: List<OrderLineItem> = emptyList(),
    @SerialName("payment_method_title")
    val paymentMethodTitle: String = "",
    @SerialName("customer_note")
    val customerNote: String = "",
    
    // Advanced fields (B2B/Logistics)
    val isB2B: Boolean = false,
    val paymentTerms: String = "",
    val dueDate: String = "",
    val billingWeightKg: Double = 0.0,
    val carrierRates: List<CarrierRate> = emptyList(),
    val bestRate: CarrierRate? = null
) {
    val totalItems: Int get() = lineItems.sumOf { it.quantity }
}

@Serializable
data class CarrierRate(
    val carrierId: String,
    val carrierName: String,
    val service: String,
    val price: Double,
    val deliveryTime: String,
    val isBestValue: Boolean = false
)

@Serializable
data class OrderAddress(
    @SerialName("first_name")
    val firstName: String = "",
    @SerialName("last_name")
    val lastName: String = "",
    val company: String = "",
    @SerialName("address_1")
    val address1: String = "",
    @SerialName("address_2")
    val address2: String = "",
    val city: String = "",
    val state: String = "",
    val postcode: String = "",
    val country: String = "",
    val email: String = "",
    val phone: String = ""
) {
    val fullName: String get() = "$firstName $lastName".trim()
    val fullAddress: String
        get() = listOf(address1, address2, "$postcode $city", country)
            .filter { it.isNotBlank() }
            .joinToString(", ")
}

@Serializable
data class OrderLineItem(
    val id: Long,
    val name: String,
    @SerialName("product_id")
    val productId: Long,
    val quantity: Int,
    val total: String = "0.00",
    val sku: String = "",
    val price: Double = 0.0
)

@Serializable
enum class OrderStatus {
    @SerialName("pending") PENDING,
    @SerialName("processing") PROCESSING,
    @SerialName("on-hold") ON_HOLD,
    @SerialName("completed") COMPLETED,
    @SerialName("cancelled") CANCELLED,
    @SerialName("refunded") REFUNDED,
    @SerialName("failed") FAILED,
    @SerialName("trash") TRASH
}
