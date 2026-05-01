package com.spoton.cms.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductVariation(
    val id: Long,
    val name: String = "",
    val sku: String = "",
    val price: String = "",
    @SerialName("regular_price")
    val regularPrice: String = "",
    @SerialName("sale_price")
    val salePrice: String = "",
    @SerialName("stock_quantity")
    val stockQuantity: Int? = null,
    @SerialName("stock_status")
    val stockStatus: String = "instock",
    @SerialName("manage_stock")
    val manageStock: Boolean = false,
    val attributes: List<VariationAttribute> = emptyList(),
    val image: ProductImage? = null
)

@Serializable
data class VariationAttribute(
    val id: Int,
    val name: String,
    val option: String
)
