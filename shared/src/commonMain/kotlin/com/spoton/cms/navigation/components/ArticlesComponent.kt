package com.spoton.cms.navigation.components

import com.arkivanov.decompose.ComponentContext
import com.spoton.cms.data.repository.ArticleRepository
import com.spoton.cms.domain.model.Article
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ArticlesComponent(
    componentContext: ComponentContext,
    val onBack: () -> Unit,
    private val onArticleSelected: (Long) -> Unit,
    private val onCreateNew: () -> Unit
) : ComponentContext by componentContext, KoinComponent {

    private val articleRepository: ArticleRepository by inject()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    data class State(
        val articles: List<Article> = emptyList(),
        val isLoading: Boolean = true,
        val searchQuery: String = "",
        val error: String? = null
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        observeArticles()
    }

    private fun observeArticles() {
        scope.launch {
            articleRepository.observeArticles().collect { articles ->
                _state.value = _state.value.copy(
                    articles = articles,
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    fun loadArticles(forceRefresh: Boolean = false) {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = articleRepository.getArticles(forceRefresh = forceRefresh)
            result.fold(
                onSuccess = { articles ->
                    _state.value = _state.value.copy(
                        articles = articles,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun onArticleClicked(id: Long) {
        onArticleSelected(id)
    }

    fun onNewArticleClicked() {
        onCreateNew()
    }

    fun deleteArticle(id: Long) {
        scope.launch {
            articleRepository.deleteArticleAndDraft(id)
            // Manual refresh as a fallback if Flow is delayed
            loadArticles()
        }
    }

    fun clearAll() {
        scope.launch {
            articleRepository.clearAllArticlesAndDrafts()
            loadArticles()
        }
    }
}
