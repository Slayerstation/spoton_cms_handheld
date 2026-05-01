package com.spoton.cms

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

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
            // The system will automatically show the notification
            // when the app is in the background.
            // For foreground, we would create a notification channel here.
        }

        // Handle data payload
        val orderData = message.data
        if (orderData.containsKey("order_id")) {
            Log.d(TAG, "New order received: ${orderData["order_id"]}")
            // TODO: Show a local notification or update the UI
        }
    }
}
