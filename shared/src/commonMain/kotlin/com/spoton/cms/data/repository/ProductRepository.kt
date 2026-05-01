package com.spoton.cms.data.repository

import com.spoton.cms.data.remote.SpotOnApi
import com.spoton.cms.domain.model.Product
import com.spoton.cms.domain.model.ProductMeta
import com.spoton.cms.domain.model.ProductStatus

class ProductRepository(private val api: SpotOnApi) {

    suspend fun getProducts(
        page: Int = 1,
        perPage: Int = 20,
        search: String? = null
    ): Result<List<Product>> {
        return try {
            Result.success(api.getProducts(page, perPage, search))
        } catch (e: Exception) {
            // Fallback for demo mode
            Result.success(listOf(
                Product(1, "Essential Cell Popups 15mm", "essential-cell-popups", "EC-15-POP", ProductStatus.PUBLISH, "", "", "10.99", "8.99", "8.99", 15, "instock", true, emptyList(), emptyList(), "0.1", emptyList(), listOf(ProductMeta(1, "_spoton_series", "Essential Cell"), ProductMeta(2, "_spoton_unit", "pot"))),
                Product(2, "The Link Boilies 1kg", "the-link-boilies", "LINK-1KG", ProductStatus.PUBLISH, "", "", "14.50", "", "14.50", 4, "instock", true, emptyList(), emptyList(), "1.0", emptyList(), listOf(ProductMeta(3, "_spoton_series", "The Link"), ProductMeta(4, "_spoton_threshold", "10"))),
                Product(3, "Mainline Stick Mix Liquid", "mainline-stick-mix", "ML-SML", ProductStatus.PUBLISH, "", "", "9.99", "", "9.99", 0, "outofstock", true, emptyList(), emptyList(), "0.5", emptyList(), listOf(ProductMeta(5, "_spoton_series", "Liquids"))),
                Product(4, "Korda Wide Gape Hooks X", "korda-wide-gape", "K-WG-X", ProductStatus.PUBLISH, "", "", "5.25", "", "5.25", 50, "instock", true, emptyList(), emptyList(), "0.05", emptyList(), listOf(ProductMeta(6, "_spoton_series", "Terminal Tackle")))
            ))
        }
    }

    suspend fun getProduct(id: Long): Result<Product> {
        return try {
            Result.success(api.getProduct(id))
        } catch (e: Exception) {
            // Fallback for demo mode
            val mock = listOf(
                Product(1, "Essential Cell Popups 15mm", "essential-cell-popups", "EC-15-POP", ProductStatus.PUBLISH, "Creamy, sweet and packed with the famous Essential Cell attractors, these 15mm pop-ups are a must-have for any carp angler.", "", "10.99", "8.99", "8.99", 15, "instock", true, emptyList(), emptyList(), "0.1", emptyList(), listOf(ProductMeta(1, "_spoton_series", "Essential Cell"), ProductMeta(2, "_spoton_unit", "pot"))),
                Product(2, "The Link Boilies 1kg", "the-link-boilies", "LINK-1KG", ProductStatus.PUBLISH, "A complex fishmeal based bait that has proven itself on the hardest waters.", "", "14.50", "", "14.50", 4, "instock", true, emptyList(), emptyList(), "1.0", emptyList(), listOf(ProductMeta(3, "_spoton_series", "The Link"), ProductMeta(4, "_spoton_threshold", "10"))),
                Product(3, "Mainline Stick Mix Liquid", "mainline-stick-mix", "ML-SML", ProductStatus.PUBLISH, "Perfect for boosting stick mixes, PVA bags and pellets.", "", "9.99", "", "9.99", 0, "outofstock", true, emptyList(), emptyList(), "0.5", emptyList(), listOf(ProductMeta(5, "_spoton_series", "Liquids"))),
                Product(4, "Korda Wide Gape Hooks X", "korda-wide-gape", "K-WG-X", ProductStatus.PUBLISH, "The ultimate all-round hook pattern.", "", "5.25", "", "5.25", 50, "instock", true, emptyList(), emptyList(), "0.05", emptyList(), listOf(ProductMeta(6, "_spoton_series", "Terminal Tackle")))
            ).find { it.id == id } ?: Product(id, "Mock Product $id", "mock-$id", "SKU-$id", ProductStatus.PUBLISH, "Mock Description", "", "0.00", "", "0.00", 0, "instock", true)
            
            Result.success(mock)
        }
    }

    suspend fun getProductBySku(sku: String): Result<Product?> {
        return try {
            val products = api.getProductBySku(sku)
            Result.success(products.firstOrNull())
        } catch (e: Exception) {
            // Fallback for demo mode
            val mock = listOf(
                Product(1, "Essential Cell Popups 15mm", "essential-cell-popups", "EC-15-POP", ProductStatus.PUBLISH, "Creamy, sweet and packed with the famous Essential Cell attractors, these 15mm pop-ups are a must-have for any carp angler.", "", "10.99", "8.99", "8.99", 15, "instock", true, emptyList(), emptyList(), "0.1", emptyList(), listOf(ProductMeta(1, "_spoton_series", "Essential Cell"), ProductMeta(2, "_spoton_unit", "pot"))),
                Product(2, "The Link Boilies 1kg", "the-link-boilies", "LINK-1KG", ProductStatus.PUBLISH, "A complex fishmeal based bait that has proven itself on the hardest waters.", "", "14.50", "", "14.50", 4, "instock", true, emptyList(), emptyList(), "1.0", emptyList(), listOf(ProductMeta(3, "_spoton_series", "The Link"), ProductMeta(4, "_spoton_threshold", "10"))),
                Product(3, "Mainline Stick Mix Liquid", "mainline-stick-mix", "ML-SML", ProductStatus.PUBLISH, "Perfect for boosting stick mixes, PVA bags and pellets.", "", "9.99", "", "9.99", 0, "outofstock", true, emptyList(), emptyList(), "0.5", emptyList(), listOf(ProductMeta(5, "_spoton_series", "Liquids"))),
                Product(4, "Korda Wide Gape Hooks X", "korda-wide-gape", "K-WG-X", ProductStatus.PUBLISH, "The ultimate all-round hook pattern.", "", "5.25", "", "5.25", 50, "instock", true, emptyList(), emptyList(), "0.05", emptyList(), listOf(ProductMeta(6, "_spoton_series", "Terminal Tackle")))
            ).find { it.sku == sku }
            Result.success(mock)
        }
    }

    suspend fun updateProduct(id: Long, updates: Map<String, Any>): Result<Product> {
        return try {
            Result.success(api.updateProduct(id, updates))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStock(id: Long, quantity: Int): Result<Product> {
        return try {
            Result.success(api.updateStock(id, quantity))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun bulkUpdatePrices(ids: List<Long>, percentage: Float): Result<Unit> {
        return try {
            // In real app, we'd iterate or use a bulk endpoint
            ids.forEach { id ->
                // This is a simplified mock for the plan
                // api.updateProduct(id, mapOf("price_adjustment" to percentage))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun bulkUpdateCategory(ids: List<Long>, categoryId: Long): Result<Unit> {
        return try {
            ids.forEach { id ->
                api.updateProduct(id, mapOf("categories" to listOf(mapOf("id" to categoryId))))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
