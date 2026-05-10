package com.spoton.cms.data.repository

import com.spoton.cms.data.remote.SpotOnApi
import com.spoton.cms.db.SpotOnDatabase
import com.spoton.cms.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.*

class ContentRepository(
    private val api: SpotOnApi,
    private val database: SpotOnDatabase,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    /**
     * Fetches a content group from WordPress and merges it with any local drafts.
     */
    suspend fun getContentGroup(group: ContentGroup): Result<ContentGroup> {
        return try {
            // 1. Fetch from WP (simulated REST endpoint for now)
            // In a real ACF setup, you'd fetch from /wp-json/acf/v3/options/options 
            // or /wp-json/wp/v2/pages/{id}?acf_format=standard
            
            // For now, we assume the data is fetched and parsed.
            // 2. Fetch local drafts
            val drafts = database.spotOnDatabaseQueries.getContentDraftsByGroup(group.key).executeAsList()
            val draftMap = drafts.associateBy { it.fieldKey }

            // 3. Merge
            val mergedFields = group.fields.map { field ->
                val draft = draftMap[field.key]
                if (draft != null) {
                    applyDraftToField(field, draft.fieldValue)
                } else {
                    field
                }
            }

            Result.success(group.copy(fields = mergedFields))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveDraft(groupKey: String, fieldKey: String, value: String): Result<Unit> {
        return try {
            database.spotOnDatabaseQueries.insertContentDraft(
                groupKey = groupKey,
                fieldKey = fieldKey,
                fieldValue = value,
                lastModified = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pushLive(group: ContentGroup): Result<Unit> {
        return try {
            val drafts = database.spotOnDatabaseQueries.getContentDraftsByGroup(group.key).executeAsList()
            if (drafts.isEmpty()) return Result.success(Unit)

            // Construct JSON for WP update
            val acfData = buildJsonObject {
                drafts.forEach { draft ->
                    put(draft.fieldKey, draft.fieldValue) // In reality, might need to parse JSON if complex
                }
            }

            // Push to WP (simulated)
            // api.updateAcfFields(group.source, group.targetId, acfData)

            // Clear drafts upon success
            database.spotOnDatabaseQueries.deleteContentDraftsByGroup(group.key)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun applyDraftToField(field: ContentField, draftValue: String): ContentField {
        return when (field) {
            is ContentField.Text -> field.copy(value = draftValue)
            is ContentField.Toggle -> field.copy(value = draftValue.toBoolean())
            is ContentField.Color -> field.copy(hex = draftValue)
            is ContentField.Image -> field.copy(attachmentId = draftValue.toIntOrNull())
            is ContentField.Repeater -> field // Repeater drafts need complex JSON parsing
        }
    }
}
