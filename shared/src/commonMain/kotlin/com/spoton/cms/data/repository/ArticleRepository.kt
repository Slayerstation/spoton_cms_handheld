package com.spoton.cms.data.repository

import com.spoton.cms.db.SpotOnDatabaseQueries
import com.spoton.cms.data.remote.SpotOnApi
import com.spoton.cms.domain.model.Article
import com.spoton.cms.domain.model.ArticleDraft
import com.spoton.cms.domain.model.ArticleStatus
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.spoton.cms.util.getCurrentTimeMillis

class ArticleRepository(
    private val api: SpotOnApi,
    private val queries: SpotOnDatabaseQueries
) {
    fun observeArticles(): Flow<List<Article>> {
        val articlesFlow = queries.getArticles()
            .asFlow()
            .mapToList(Dispatchers.Default)
        
        val draftsFlow = queries.getDrafts()
            .asFlow()
            .mapToList(Dispatchers.Default)

        return combine(articlesFlow, draftsFlow) { articles, drafts ->
            val domainArticles = articles.map { it.toDomain() }
            val domainDrafts = drafts.map { it.toDomain() }
            
            // 1. De-duplicate drafts: only keep the newest draft per wpId (or newest orphan)
            // This fixes the crash where multiple drafts for the same article caused duplicate keys.
            val latestDrafts = domainDrafts
                .sortedByDescending { it.lastModified }
                .distinctBy { it.wpId ?: -(it.id ?: 0L) }

            // 2. Start with articles, but override with draft content if it exists
            val merged = domainArticles.map { article ->
                val draft = latestDrafts.find { it.wpId == article.id }
                if (draft != null) {
                    article.copy(
                        title = draft.title,
                        content = draft.content,
                        status = ArticleStatus.DRAFT
                    )
                } else {
                    article
                }
            }.toMutableList()
            
            // 3. Add "New Post" drafts (where wpId is null or not in cached articles)
            val newDrafts = latestDrafts.filter { draft -> 
                draft.wpId == null || domainArticles.none { it.id == draft.wpId }
            }.map { draft ->
                Article(
                    id = draft.wpId ?: -(draft.id ?: 0L),
                    title = draft.title,
                    content = draft.content,
                    status = ArticleStatus.DRAFT,
                    authorId = 0,
                    dateCreated = draft.lastModified.toString(),
                    featuredMediaUrl = null
                )
            }
            
            merged.addAll(newDrafts)
            merged.sortedByDescending { it.id }
        }
    }

    suspend fun getArticles(page: Int = 1, perPage: Int = 20, forceRefresh: Boolean = false): Result<List<Article>> {
        return try {
            val cachedArticles = queries.getArticles().executeAsList().map { it.toDomain() }
            val localDrafts = queries.getDrafts().executeAsList().map { it.toDomain() }
            
            // Merge logic:
            // 1. New drafts (wpId == null) become new entries
            // 2. Drafts for existing articles (wpId != null) should ideally be merged or flagged.
            // For now, let's just combine them and filter duplicates if needed, 
            // but mark new drafts clearly.
            
            val merged = mutableListOf<Article>()
            
            // Add existing cached articles
            merged.addAll(cachedArticles)
            
            // Add "New Post" drafts that haven't been published yet
            val newDrafts = localDrafts.filter { draft -> 
                draft.wpId == null || cachedArticles.none { it.id == draft.wpId }
            }.map { draft ->
                Article(
                    id = draft.wpId ?: -(draft.id ?: 0L), // Use negative ID for local-only items
                    title = draft.title,
                    content = draft.content,
                    status = ArticleStatus.DRAFT,
                    authorId = 0,
                    dateCreated = draft.lastModified.toString(),
                    featuredMediaUrl = null
                )
            }
            
            merged.addAll(newDrafts)
            
            // Sort by ID (newest/local first)
            Result.success(merged.sortedByDescending { it.id })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getArticle(id: Long): Result<Article> {
        return try {
            val cached = queries.getArticleById(id).executeAsOneOrNull()?.toDomain()
            if (cached != null) Result.success(cached) else Result.failure(Exception("Article not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveArticleLocally(title: String, content: String, id: Long? = null, status: ArticleStatus = ArticleStatus.DRAFT): Long = withContext(Dispatchers.IO) {
        if (id == null) {
            queries.insertArticle(
                id = null, // Auto-increment
                title = title,
                content = content,
                status = status.name.lowercase(),
                author_id = 0,
                date_created = getCurrentTimeMillis().toString(),
                featured_media_url = null,
                last_synced = 0
            )
            queries.getArticles().executeAsList().first().id
        } else {
            queries.updateArticle(
                title = title,
                content = content,
                status = status.name.lowercase(),
                id = id
            )
            id
        }
    }

    suspend fun deleteArticleLocally(id: Long) = withContext(Dispatchers.IO) {
        queries.deleteArticle(id)
        queries.deleteDraftByWpId(id) // Also cleanup draft for this article
    }

    suspend fun deleteArticleAndDraft(id: Long) = withContext(Dispatchers.IO) {
        // Wrap in transaction to ensure single notification and atomicity
        queries.transaction {
            if (id <= 0) {
                // Local-only item (negative or zero ID)
                val localId = if (id < 0) -id else id
                queries.deleteDraft(localId)
                queries.deleteArticle(id) 
            } else {
                // Potential WordPress article or draft with wp_id
                queries.deleteArticle(id)
                queries.deleteDraftByWpId(id)
                queries.deleteDraft(id) 
            }
        }
    }

    suspend fun clearAllArticlesAndDrafts() = withContext(Dispatchers.IO) {
        queries.transaction {
            queries.deleteAllArticles()
            queries.deleteAllDrafts()
        }
    }

    // ── Drafts (Autosave) ───────────────────────────────────────────
    // Keeping drafts separate for high-frequency autosaves

    fun getDrafts(): List<ArticleDraft> {
        return queries.getDrafts().executeAsList().map { it.toDomain() }
    }

    fun getDraftById(id: Long): ArticleDraft? {
        return queries.getDraftById(id).executeAsOneOrNull()?.toDomain()
    }

    fun getDraftByWpId(wpId: Long): ArticleDraft? {
        return queries.getDraftByWpId(wpId).executeAsOneOrNull()?.toDomain()
    }

    suspend fun saveDraft(draft: ArticleDraft): Long = withContext(Dispatchers.IO) {
        queries.insertDraft(
            id = draft.id,
            wp_id = draft.wpId,
            title = draft.title,
            content = draft.content,
            last_modified = getCurrentTimeMillis()
        )
        draft.id ?: queries.getDrafts().executeAsList().first().id
    }

    suspend fun deleteDraft(id: Long) = withContext(Dispatchers.IO) {
        queries.deleteDraft(id)
    }

    suspend fun deleteDraftByWpId(wpId: Long) = withContext(Dispatchers.IO) {
        queries.deleteDraftByWpId(wpId)
    }
}

// ── Extensions ──────────────────────────────────────────────────────

private fun com.spoton.cms.db.ArticleCache.toDomain(): Article = Article(
    id = id,
    title = title,
    content = content,
    status = try { ArticleStatus.valueOf(status.uppercase()) } catch (e: Exception) { ArticleStatus.DRAFT },
    authorId = author_id,
    dateCreated = date_created,
    featuredMediaUrl = featured_media_url
)

private fun com.spoton.cms.db.ArticleDraft.toDomain(): ArticleDraft = ArticleDraft(
    id = id,
    wpId = wp_id,
    title = title,
    content = content,
    lastModified = last_modified
)
