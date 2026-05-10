package com.spoton.cms.data.remote

import com.spoton.cms.domain.model.AuthToken
import com.spoton.cms.domain.model.Order
import com.spoton.cms.domain.model.OrderStatus
import com.spoton.cms.domain.model.Product
import com.spoton.cms.domain.model.StyleConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * API client for all backend communication.
 * Uses WooCommerce REST API v3 for products/orders,
 * WPGraphQL JWT for authentication,
 * and the custom cms/v1 bridge for options/styles.
 */
class SpotOnApi(
    internal val httpClient: HttpClient,
    private val baseUrlProvider: () -> String
) {
    internal val baseUrl: String get() = baseUrlProvider()
    private val wcApi: String get() = "$baseUrl/wp-json/wc/v3"
    private val wpApi: String get() = "$baseUrl/wp-json/wp/v2"
    private val cmsApi: String get() = "$baseUrl/wp-json/cms/v1"
    private val jwtApi: String get() = "$baseUrl/wp-json/jwt-auth/v1"

    // ── Authentication ──────────────────────────────────────────────

    suspend fun login(username: String, password: String): AuthToken {
        val response = httpClient.post("$jwtApi/token") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "username" to username,
                "password" to password
            ))
        }

        if (response.status != HttpStatusCode.OK) {
            // Attempt to parse the WP error message
            val errorBody = response.body<Map<String, kotlinx.serialization.json.JsonElement>>()
            val message = errorBody["message"]?.toString()?.removeSurrounding("\"") 
                ?: "Login failed (${response.status.value})"
            throw Exception(message)
        }

        return response.body()
    }

    // ── Products ────────────────────────────────────────────────────

    suspend fun getProducts(page: Int = 1, perPage: Int = 20, search: String? = null): List<Product> {
        return httpClient.get("$wcApi/products") {
            parameter("page", page)
            parameter("per_page", perPage)
            search?.let { parameter("search", it) }
        }.body()
    }

    suspend fun getProduct(id: Long): Product {
        return httpClient.get("$wcApi/products/$id").body()
    }

    suspend fun getProductBySku(sku: String): List<Product> {
        return httpClient.get("$wcApi/products") {
            parameter("sku", sku)
        }.body()
    }

    suspend fun updateProduct(id: Long, updates: Map<String, Any>): Product {
        return httpClient.put("$wcApi/products/$id") {
            contentType(ContentType.Application.Json)
            setBody(updates)
        }.body()
    }

    suspend fun updateStock(id: Long, quantity: Int): Product {
        return updateProduct(id, mapOf(
            "stock_quantity" to quantity,
            "manage_stock" to true
        ))
    }

    // ── Orders ──────────────────────────────────────────────────────

    suspend fun getOrders(
        page: Int = 1,
        perPage: Int = 20,
        status: OrderStatus? = null
    ): List<Order> {
        return httpClient.get("$wcApi/orders") {
            parameter("page", page)
            parameter("per_page", perPage)
            status?.let { parameter("status", it.name.lowercase().replace("_", "-")) }
        }.body()
    }

    suspend fun getOrder(id: Long): Order {
        return httpClient.get("$wcApi/orders/$id").body()
    }

    suspend fun updateOrderStatus(id: Long, status: OrderStatus): Order {
        return httpClient.put("$wcApi/orders/$id") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("status" to status.name.lowercase().replace("_", "-")))
        }.body()
    }

    // ── Articles ────────────────────────────────────────────────────

    suspend fun getArticles(page: Int = 1, perPage: Int = 20, search: String? = null): List<com.spoton.cms.domain.model.Article> {
        return httpClient.get("$wpApi/posts") {
            parameter("page", page)
            parameter("per_page", perPage)
            search?.let { parameter("search", it) }
        }.body()
    }

    suspend fun getArticle(id: Long): com.spoton.cms.domain.model.Article {
        return httpClient.get("$wpApi/posts/$id").body()
    }

    suspend fun createArticle(article: Map<String, Any>): com.spoton.cms.domain.model.Article {
        return httpClient.post("$wpApi/posts") {
            contentType(ContentType.Application.Json)
            setBody(article)
        }.body()
    }

    suspend fun updateArticle(id: Long, updates: Map<String, Any>): com.spoton.cms.domain.model.Article {
        return httpClient.put("$wpApi/posts/$id") {
            contentType(ContentType.Application.Json)
            setBody(updates)
        }.body()
    }

    // ── CMS Bridge (Options / Styles) ───────────────────────────────

    suspend fun getOption(name: String): OptionResponse {
        return httpClient.get("$cmsApi/option/$name").body()
    }

    suspend fun updateOption(name: String, value: Any): OptionResponse {
        return httpClient.post("$cmsApi/option/$name") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("value" to value))
        }.body()
    }

    suspend fun getStyleConfig(): StyleConfig {
        return try {
            val response = getOption("spoton_style_config")
            kotlinx.serialization.json.Json.decodeFromString<StyleConfig>(
                response.value.toString()
            )
        } catch (e: Exception) {
            StyleConfig() // Return defaults if no config stored yet
        }
    }

    suspend fun updateStyleConfig(config: StyleConfig) {
        updateOption("spoton_style_config", config)
    }

    // ── System ──────────────────────────────────────────────────────
    
    suspend fun getSystemInfo(): com.spoton.cms.domain.model.BackendSystemInfo {
        return httpClient.get("$cmsApi/system").body()
    }
}

@Serializable
data class OptionResponse(
    val name: String,
    val value: kotlinx.serialization.json.JsonElement? = null
)
