package com.spoton.cms.navigation.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.mohamedrejeb.richeditor.model.RichTextState
import com.spoton.cms.data.repository.ArticleRepository
import com.spoton.cms.domain.model.Article
import com.spoton.cms.domain.model.ArticleDraft
import com.spoton.cms.domain.model.ArticleStatus
import com.spoton.cms.ui.components.editor.HtmlRichTextConverter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.spoton.cms.util.getCurrentTimeMillis
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ArticleDetailComponent(
    componentContext: ComponentContext,
    private val articleId: Long?, // Null if creating new
    val onBack: () -> Unit
) : ComponentContext by componentContext, KoinComponent {

    private val articleRepository: ArticleRepository by inject()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val htmlConverter = HtmlRichTextConverter()

    val contentState = RichTextState()
    var titleState = MutableStateFlow("")

    data class State(
        val article: Article? = null,
        val isLoading: Boolean = true,
        val isSaving: Boolean = false,
        val isAutosaving: Boolean = false,
        val lastSaved: Long? = null,
        val error: String? = null
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private var localDraftId: Long? = null
    private var autosaveJob: Job? = null

    init {
        loadArticle()
        startAutosaveLoop()
        
        lifecycle.doOnDestroy {
            autosaveJob?.cancel()
            // Final attempt to save draft before component is destroyed
            scope.launch {
                saveDraftLocally()
            }
        }
    }

    private fun loadArticle() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            if (articleId == null) {
                // New post (from scratch)
                val existingDraft = articleRepository.getDrafts().find { it.wpId == null }
                existingDraft?.let { draft ->
                    localDraftId = draft.id
                    titleState.value = draft.title
                    htmlConverter.fromInput(draft.content, contentState)
                }
                _state.value = _state.value.copy(isLoading = false)
            } else if (articleId < 0) {
                // Local-only draft (from list view)
                val draftId = -articleId
                val draft = articleRepository.getDraftById(draftId)
                draft?.let {
                    localDraftId = it.id
                    titleState.value = it.title
                    htmlConverter.fromInput(it.content, contentState)
                }
                _state.value = _state.value.copy(isLoading = false)
            } else {
                // Existing WordPress article (with potential local draft)
                val draft = articleRepository.getDraftByWpId(articleId)
                draft?.let { localDraftId = it.id }
                
                val result = articleRepository.getArticle(articleId)
                
                result.fold(
                    onSuccess = { article ->
                        _state.value = _state.value.copy(article = article, isLoading = false)
                        
                        // Use draft content if it exists, otherwise use article
                        if (draft != null) {
                             titleState.value = draft.title
                             htmlConverter.fromInput(draft.content, contentState)
                        } else {
                            titleState.value = article.title
                            htmlConverter.fromInput(article.content, contentState)
                        }
                    },
                    onFailure = { e ->
                        _state.value = _state.value.copy(isLoading = false, error = e.message)
                    }
                )
            }
        }
    }

    private fun startAutosaveLoop() {
        autosaveJob?.cancel()
        autosaveJob = scope.launch {
            while (isActive) {
                delay(30000) // Autosave every 30 seconds
                saveDraftLocally()
            }
        }
    }

    fun saveDraftManual() {
        scope.launch {
            saveDraftLocally()
        }
    }

    private suspend fun saveDraftLocally() {
        if (_state.value.isAutosaving) return // Prevent concurrent autosaves

        _state.value = _state.value.copy(isAutosaving = true)
        try {
            withContext(Dispatchers.Default) {
                val html = htmlConverter.toOutput(contentState)
                val draft = ArticleDraft(
                    id = localDraftId,
                    wpId = articleId,
                    title = titleState.value,
                    content = html,
                    lastModified = getCurrentTimeMillis()
                )
                localDraftId = articleRepository.saveDraft(draft)
                _state.value = _state.value.copy(lastSaved = draft.lastModified)
            }
        } catch (e: Throwable) {
            // Log or handle error if needed
        } finally {
            _state.value = _state.value.copy(isAutosaving = false)
        }
    }

    fun publish() {
        scope.launch {
            _state.value = _state.value.copy(isSaving = true)
            val html = htmlConverter.toOutput(contentState)
            
            try {
                val savedId = articleRepository.saveArticleLocally(
                    title = titleState.value,
                    content = html,
                    id = articleId,
                    status = ArticleStatus.PUBLISH
                )
                
                _state.value = _state.value.copy(isSaving = false)
                
                // Clear draft after successful local save
                if (articleId == null) {
                     val draftToDelete = articleRepository.getDrafts().find { it.wpId == null }
                     draftToDelete?.id?.let { id ->
                         articleRepository.deleteDraft(id)
                     }
                } else {
                    articleRepository.deleteDraftByWpId(articleId)
                }
                
                onBack()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, error = e.message)
            }
        }
    }

}
