package com.spoton.cms

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Firebase Cloud Messaging service for handling push notifications.
 * Receives notifications from the WordPress backend when new orders arrive.
 */
class SpotOnFirebaseService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "SpotOnFCM"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        
        // Subscribe to the global orders topic
        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("spoton_cms_orders")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Subscribed to spoton_cms_orders topic")
                }
            }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received: ${message.data}")

        // Handle notification payload
        message.notification?.let { notification ->
            Log.d(TAG, "Title: ${notification.title}")
            Log.d(TAG, "Body: ${notification.body}")
        }

        // Handle data payload (Orders vs Chat)
        val data = message.data
        
        if (data.containsKey("order_id")) {
            Log.d(TAG, "New order received: ${data["order_id"]}")
            // Handle order notification
        } else if (data.containsKey("channel")) {
            Log.d(TAG, "New chat message received")
            handleChatMessage(data)
        }
    }
    
    private fun handleChatMessage(data: Map<String, String>) {
        val channel = data["channel"] ?: "unknown"
        val senderName = data["senderName"] ?: "Unknown"
        val senderId = data["senderId"] ?: "unknown_id"
        val content = data["content"] ?: "No content"
        val timestampStr = data["timestamp"]
        val threadId = data["threadId"] ?: senderId
        
        val timestamp = timestampStr?.toLongOrNull() ?: kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        
        // Use Koin to get the database (since Services aren't automatically injected without KoinComponent or custom scoping)
        val database: com.spoton.cms.db.SpotOnDatabase by org.koin.java.KoinJavaComponent.inject(com.spoton.cms.db.SpotOnDatabase::class.java)
        
        GlobalScope.launch(Dispatchers.IO) {
            database.spotOnDatabaseQueries.insertMessage(
                id = kotlinx.datetime.Clock.System.now().toEpochMilliseconds().toString(),
                channel = channel,
                senderName = senderName,
                senderId = senderId,
                content = content,
                timestamp = timestamp,
                isRead = 0,
                isOutgoing = 0,
                threadId = threadId
            )
        }
    }
}
