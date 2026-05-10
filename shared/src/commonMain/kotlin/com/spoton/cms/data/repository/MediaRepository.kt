package com.spoton.cms.data.repository

import com.spoton.cms.data.remote.SpotOnApi
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class MediaRepository(
    private val api: SpotOnApi,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    /**
     * Uploads an image to the WordPress Media Library.
     * Returns the attachment ID on success.
     */
    suspend fun uploadImage(fileName: String, byteArray: ByteArray): Result<Int> {
        return try {
            val response = api.httpClient.post("${api.baseUrl}/wp-json/wp/v2/media") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("file", byteArray, Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                                append(HttpHeaders.ContentType, "image/jpeg")
                            })
                            append("title", fileName)
                        }
                    )
                )
            }

            if (response.status == HttpStatusCode.Created) {
                val body = response.bodyAsText()
                val jsonObject = json.parseToJsonElement(body).jsonObject
                val id = jsonObject["id"]?.jsonPrimitive?.int ?: throw Exception("ID not found in response")
                Result.success(id)
            } else {
                Result.failure(Exception("Upload failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
