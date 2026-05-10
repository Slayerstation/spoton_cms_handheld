package com.spoton.cms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoton.cms.db.MessageCache
import com.spoton.cms.navigation.components.ChatComponent
import com.spoton.cms.ui.theme.SpotOnOrange
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(component: ChatComponent) {
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (state.selectedThreadId == null) "Unified Inbox" else "Chat") 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.selectedThreadId != null) {
                            component.clearSelection()
                        } else {
                            component.onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.selectedThreadId == null) {
                InboxView(state.messages, component::selectThread)
            } else {
                ThreadView(
                    messages = state.threadMessages, 
                    input = state.messageInput,
                    onInputChanged = component::updateMessageInput,
                    onSend = component::sendMessage
                )
            }
        }
    }
}

@Composable
private fun InboxView(messages: List<MessageCache>, onSelect: (String) -> Unit) {
    if (messages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Geen berichten gevonden", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(messages) { msg ->
                MessageItem(msg = msg, onClick = { onSelect(msg.threadId) })
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun MessageItem(msg: MessageCache, onClick: () -> Unit) {
    val isRead = msg.isRead == 1L
    val bgColor = if (isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar / Channel Icon
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(getChannelColor(msg.channel)),
            contentAlignment = Alignment.Center
        ) {
            Text(msg.senderName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = msg.senderName, 
                    fontWeight = if (isRead) FontWeight.Normal else FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = formatTime(msg.timestamp), 
                    fontSize = 12.sp, 
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = msg.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isRead) Color.Gray else MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ThreadView(
    messages: List<MessageCache>,
    input: String,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(16.dp),
            reverseLayout = false // In a real app, you might want this to be true and provide items reversed
        ) {
            items(messages) { msg ->
                val isOutgoing = msg.isOutgoing == 1L
                ChatBubble(msg, isOutgoing)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChanged,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Typ een bericht...") },
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                modifier = Modifier.clip(CircleShape).background(SpotOnOrange)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: MessageCache, isOutgoing: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(
                    topStart = 16.dp, 
                    topEnd = 16.dp,
                    bottomStart = if (isOutgoing) 16.dp else 4.dp,
                    bottomEnd = if (isOutgoing) 4.dp else 16.dp
                ))
                .background(if (isOutgoing) SpotOnOrange else MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
        ) {
            Text(
                text = msg.content,
                color = if (isOutgoing) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getChannelColor(channel: String): Color {
    return when (channel.lowercase()) {
        "whatsapp" -> Color(0xFF25D366)
        "instagram" -> Color(0xFFE1306C)
        "messenger" -> Color(0xFF0084FF)
        "email" -> SpotOnOrange
        else -> Color.Gray
    }
}

private fun formatTime(millis: Long): String {
    val instant = Instant.fromEpochMilliseconds(millis)
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
}
