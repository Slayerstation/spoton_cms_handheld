package com.spoton.cms.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface ContentField {
    val key: String
    val label: String

    @Serializable
    data class Text(
        override val key: String,
        override val label: String,
        val value: String = "",
        val isMultiline: Boolean = false
    ) : ContentField

    @Serializable
    data class Toggle(
        override val key: String,
        override val label: String,
        val value: kotlin.Boolean = false
    ) : ContentField

    @Serializable
    data class Image(
        override val key: String,
        override val label: String,
        val attachmentId: Int? = null,
        val url: String? = null
    ) : ContentField

    @Serializable
    data class Color(
        override val key: String,
        override val label: String,
        val hex: String = "#000000"
    ) : ContentField

    @Serializable
    data class Repeater(
        override val key: String,
        override val label: String,
        val items: List<Map<String, ContentField>> = emptyList(),
        val subFieldSchemas: List<ContentFieldSchema> = emptyList()
    ) : ContentField
}

@Serializable
data class ContentFieldSchema(
    val key: String,
    val label: String,
    val type: String // "text", "boolean", "image", "color", "repeater"
)

@Serializable
data class ContentGroup(
    val key: String,
    val label: String,
    val fields: List<ContentField> = emptyList(),
    val source: ContentSource = ContentSource.OPTIONS,
    val targetId: String? = null // ID of page or CPT if not options
)

@Serializable
enum class ContentSource {
    OPTIONS, PAGE, POST, CUSTOM_POST_TYPE
}
