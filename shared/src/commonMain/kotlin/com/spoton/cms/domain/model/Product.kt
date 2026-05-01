package com.spoton.cms.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: Long,
    val name: String,
    val slug: String,
    val sku: String = "",
    val status: ProductStatus = ProductStatus.PUBLISH,
    val description: String = "",
    @SerialName("short_description")
    val shortDescription: String = "",
    @SerialName("regular_price")
    val regularPrice: String = "",
    @SerialName("sale_price")
    val salePrice: String = "",
    val price: String = "",
    @SerialName("stock_quantity")
    val stockQuantity: Int? = null,
    @SerialName("stock_status")
    val stockStatus: String = "instock",
    @SerialName("manage_stock")
    val manageStock: Boolean = false,
    val categories: List<ProductCategory> = emptyList(),
    val images: List<ProductImage> = emptyList(),
    val weight: String = "",
    val variations: List<Long> = emptyList(), // List of variation IDs
    @SerialName("meta_data")
    val metaData: List<ProductMeta> = emptyList()
) {
    // Helper to get meta values
    fun getMetaValue(key: String): String? = metaData.find { it.key == key }?.value
    
    // Specific business accessors
    val series: String get() = getMetaValue("_spoton_series") ?: ""
    val unit: String get() = getMetaValue("_spoton_unit") ?: "kg"
    val threshold: Int get() = getMetaValue("_spoton_threshold")?.toIntOrNull() ?: 5
    val ingredients: List<String> get() = getMetaValue("_spoton_ingredients")?.split("\n")?.filter { it.isNotBlank() } ?: emptyList()
    val specifications: List<String> get() = getMetaValue("_spoton_specifications")?.split("\n")?.filter { it.isNotBlank() } ?: emptyList()
}

@Serializable
data class ProductMeta(
    val id: Long? = null,
    val key: String,
    val value: String
)

@Serializable
data class ProductCategory(
    val id: Long,
    val name: String,
    val slug: String = ""
)

@Serializable
data class ProductImage(
    val id: Long,
    val src: String,
    val name: String = "",
    val alt: String = ""
)

@Serializable
enum class ProductStatus {
    @SerialName("draft") DRAFT,
    @SerialName("publish") PUBLISH,
    @SerialName("pending") PENDING,
    @SerialName("private") PRIVATE
}
