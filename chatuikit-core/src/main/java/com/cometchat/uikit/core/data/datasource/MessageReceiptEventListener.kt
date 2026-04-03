package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.MessageReceipt
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Helper class that wraps CometChat SDK message receipt listeners.
 * Exposes real-time receipt events as Kotlin Flows for reactive consumption.
 * 
 * Per design doc: Wraps CometChatMessageEvents.addListener() and removeListener()
 * for onMessagesDelivered and onMessagesRead events.
 */
class MessageReceiptEventListener {

    /**
     * Creates a Flow that emits MessageReceipt events for both delivered and read receipts.
     * The listener is automatically added when collection starts and removed when cancelled.
     * 
     * @return Flow emitting MessageReceipt for delivered and read events
     */
    fun receiptEvents(): Flow<MessageReceipt> = callbackFlow {
        val listenerTag = "MessageInformation_${System.currentTimeMillis()}"
        
        CometChat.addMessageListener(listenerTag, object : CometChat.MessageListener() {
            override fun onMessagesDelivered(messageReceipt: MessageReceipt) {
                trySend(messageReceipt)
            }

            override fun onMessagesRead(messageReceipt: MessageReceipt) {
                trySend(messageReceipt)
            }
        })

        awaitClose {
            CometChat.removeMessageListener(listenerTag)
        }
    }

    /**
     * Creates a Flow that emits only delivered receipt events.
     * 
     * @return Flow emitting MessageReceipt for delivered events only
     */
    fun deliveredEvents(): Flow<MessageReceipt> = callbackFlow {
        val listenerTag = "MessageInformation_Delivered_${System.currentTimeMillis()}"
        
        CometChat.addMessageListener(listenerTag, object : CometChat.MessageListener() {
            override fun onMessagesDelivered(messageReceipt: MessageReceipt) {
                trySend(messageReceipt)
            }
        })

        awaitClose {
            CometChat.removeMessageListener(listenerTag)
        }
    }

    /**
     * Creates a Flow that emits only read receipt events.
     * 
     * @return Flow emitting MessageReceipt for read events only
     */
    fun readEvents(): Flow<MessageReceipt> = callbackFlow {
        val listenerTag = "MessageInformation_Read_${System.currentTimeMillis()}"
        
        CometChat.addMessageListener(listenerTag, object : CometChat.MessageListener() {
            override fun onMessagesRead(messageReceipt: MessageReceipt) {
                trySend(messageReceipt)
            }
        })

        awaitClose {
            CometChat.removeMessageListener(listenerTag)
        }
    }
}
