package com.spoton.cms.navigation.components

import com.arkivanov.decompose.ComponentContext
import com.spoton.cms.db.SpotOnDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ChatComponent(
    componentContext: ComponentContext,
    val onBack: () -> Unit
) : ComponentContext by componentContext, KoinComponent {

    private val database: SpotOnDatabase by inject()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    data class State(
        val messages: List<com.spoton.cms.db.MessageCache> = emptyList(),
        val selectedThreadId: String? = null,
        val threadMessages: List<com.spoton.cms.db.MessageCache> = emptyList(),
        val messageInput: String = ""
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        loadMessages()
    }

    private fun loadMessages() {
        scope.launch {
            try {
                // Group messages by threadId to show the latest message for each thread in the inbox view
                val allMessages = database.spotOnDatabaseQueries.getMessages().executeAsList()
                val latestPerThread = allMessages.groupBy { it.threadId }.map { it.value.first() }
                _state.value = _state.value.copy(messages = latestPerThread)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun selectThread(threadId: String) {
        scope.launch {
            val messages = database.spotOnDatabaseQueries.getMessagesByThread(threadId).executeAsList()
            // Mark as read
            database.spotOnDatabaseQueries.markAsRead(threadId)
            
            _state.value = _state.value.copy(
                selectedThreadId = threadId,
                threadMessages = messages
            )
            // Reload inbox to update read status
            loadMessages()
        }
    }

    fun clearSelection() {
        _state.value = _state.value.copy(selectedThreadId = null, threadMessages = emptyList())
    }

    fun updateMessageInput(input: String) {
        _state.value = _state.value.copy(messageInput = input)
    }

    fun sendMessage() {
        val currentThread = _state.value.selectedThreadId ?: return
        val currentInput = _state.value.messageInput
        if (currentInput.isBlank()) return

        // For now, just save locally as outgoing message (until bridge is implemented)
        scope.launch {
            val newMessage = com.spoton.cms.db.MessageCache(
                id = kotlinx.datetime.Clock.System.now().toEpochMilliseconds().toString(),
                channel = _state.value.threadMessages.firstOrNull()?.channel ?: "unknown",
                senderName = "Me",
                senderId = "me",
                content = currentInput,
                timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                isRead = 1,
                isOutgoing = 1,
                threadId = currentThread
            )
            database.spotOnDatabaseQueries.insertMessage(
                id = newMessage.id,
                channel = newMessage.channel,
                senderName = newMessage.senderName,
                senderId = newMessage.senderId,
                content = newMessage.content,
                timestamp = newMessage.timestamp,
                isRead = 1,
                isOutgoing = 1,
                threadId = newMessage.threadId
            )
            
            _state.value = _state.value.copy(messageInput = "")
            selectThread(currentThread)
        }
    }
}
