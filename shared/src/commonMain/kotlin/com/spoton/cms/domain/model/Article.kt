package com.spoton.cms.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Article(
    val id: Long,
    val title: String,
    val content: String,
    val status: ArticleStatus = ArticleStatus.DRAFT,
    @SerialName("author_id")
    val authorId: Long = 0,
    @SerialName("date_created")
    val dateCreated: String = "",
    @SerialName("featured_media_url")
    val featuredMediaUrl: String? = null
)

@Serializable
data class ArticleDraft(
    val id: Long? = null,
    @SerialName("wp_id")
    val wpId: Long? = null,
    val title: String = "",
    val content: String = "",
    @SerialName("last_modified")
    val lastModified: Long = 0
)

@Serializable
enum class ArticleStatus {
    @SerialName("draft") DRAFT,
    @SerialName("publish") PUBLISH,
    @SerialName("future") FUTURE,
    @SerialName("pending") PENDING,
    @SerialName("private") PRIVATE
}
