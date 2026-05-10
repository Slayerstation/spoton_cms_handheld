package com.spoton.cms.navigation.components

import com.arkivanov.decompose.ComponentContext
import com.spoton.cms.data.repository.ContentRepository
import com.spoton.cms.data.repository.MediaRepository
import com.spoton.cms.domain.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ContentComponent(
    componentContext: ComponentContext,
    val onBack: () -> Unit
) : ComponentContext by componentContext, KoinComponent {

    private val repository: ContentRepository by inject()
    private val mediaRepository: MediaRepository by inject()
    private val imagePicker: com.spoton.cms.util.ImagePicker by inject()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var currentPickingFieldKey: String? = null

    data class State(
        val curatedGroups: List<ContentGroup> = emptyList(),
        val selectedGroup: ContentGroup? = null,
        val isLoading: Boolean = false,
        val message: String? = null
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        loadCuratedGroups()
        observeImagePicker()
    }

    private fun observeImagePicker() {
        scope.launch {
            imagePicker.imageFlow.collect { picked ->
                val fieldKey = currentPickingFieldKey ?: return@collect
                val image = picked ?: return@collect
                
                _state.value = _state.value.copy(isLoading = true, message = "Uploading image...")
                val result = mediaRepository.uploadImage(image.fileName, image.byteArray)
                
                if (result.isSuccess) {
                    val attachmentId = result.getOrNull()
                    updateField(fieldKey, attachmentId.toString())
                    _state.value = _state.value.copy(isLoading = false, message = "Image uploaded successfully!")
                } else {
                    _state.value = _state.value.copy(isLoading = false, message = "Upload failed: ${result.exceptionOrNull()?.message}")
                }
                currentPickingFieldKey = null
            }
        }
    }

    private fun loadCuratedGroups() {
        // Mock curated groups for initial implementation
        val groups = listOf(
            ContentGroup(
                key = "homepage_hero",
                label = "Homepage Hero",
                fields = listOf(
                    ContentField.Text("hero_title", "Hero Title", "Welcome to SpotOn Baits"),
                    ContentField.Text("hero_subtitle", "Hero Subtitle", "Premium Baits for Serious Anglers", isMultiline = true),
                    ContentField.Image("hero_image", "Hero Banner Image")
                )
            ),
            ContentGroup(
                key = "announcement_bar",
                label = "Announcement Bar",
                fields = listOf(
                    ContentField.Toggle("show_announcement", "Show Bar", true),
                    ContentField.Text("announcement_text", "Text", "Free shipping on orders over €50!"),
                    ContentField.Color("announcement_bg", "Background Color", "#FF5722")
                )
            ),
            ContentGroup(
                key = "usps",
                label = "Value Propositions (USPs)",
                fields = listOf(
                    ContentField.Repeater(
                        key = "usp_list",
                        label = "USP List",
                        items = listOf(
                            mapOf(
                                "icon" to ContentField.Text("icon", "Icon (Lucide Name)", "truck"),
                                "text" to ContentField.Text("text", "Text", "Fast Delivery")
                            ),
                            mapOf(
                                "icon" to ContentField.Text("icon", "Icon (Lucide Name)", "shield"),
                                "text" to ContentField.Text("text", "Text", "Secure Payment")
                            )
                        )
                    )
                )
            )
        )
        
        _state.value = _state.value.copy(curatedGroups = groups)
        
        // Refresh with drafts
        scope.launch {
            val refreshed = groups.map { repository.getContentGroup(it).getOrDefault(it) }
            _state.value = _state.value.copy(curatedGroups = refreshed)
        }
    }

    fun selectGroup(group: ContentGroup) {
        _state.value = _state.value.copy(selectedGroup = group)
    }

    fun pickImage(fieldKey: String) {
        currentPickingFieldKey = fieldKey
        imagePicker.pickImage()
    }

    fun takePhoto(fieldKey: String) {
        currentPickingFieldKey = fieldKey
        imagePicker.takePhoto()
    }

    fun clearSelection() {
        _state.value = _state.value.copy(selectedGroup = null)
    }

    fun updateField(fieldKey: String, newValue: String) {
        val currentGroup = _state.value.selectedGroup ?: return
        
        scope.launch {
            repository.saveDraft(currentGroup.key, fieldKey, newValue)
            // Reload group
            val refreshed = repository.getContentGroup(currentGroup).getOrDefault(currentGroup)
            _state.value = _state.value.copy(selectedGroup = refreshed)
            
            // Also update the list view
            val newList = _state.value.curatedGroups.map { 
                if (it.key == currentGroup.key) refreshed else it 
            }
            _state.value = _state.value.copy(curatedGroups = newList)
        }
    }

    fun pushLive() {
        val currentGroup = _state.value.selectedGroup ?: return
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = repository.pushLive(currentGroup)
            _state.value = _state.value.copy(
                isLoading = false,
                message = if (result.isSuccess) "Content pushed live!" else "Error: ${result.exceptionOrNull()?.message}"
            )
            if (result.isSuccess) {
                // Refresh data
                val refreshed = repository.getContentGroup(currentGroup).getOrDefault(currentGroup)
                _state.value = _state.value.copy(selectedGroup = refreshed)
                loadCuratedGroups()
            }
        }
    }

    fun dismissMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
