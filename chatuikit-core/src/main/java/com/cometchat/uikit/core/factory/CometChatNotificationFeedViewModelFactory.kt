package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.chat.core.NotificationCategoriesRequest
import com.cometchat.chat.core.NotificationFeedRequest
import com.cometchat.uikit.core.viewmodel.CometChatNotificationFeedViewModel

/**
 * Factory for creating CometChatNotificationFeedViewModel with dependencies.
 * Enables dependency injection of custom request builders.
 *
 * @param feedRequestBuilder Optional custom request builder for feed items.
 *                           Defaults to null (ViewModel creates default builder with limit=20).
 * @param categoriesRequestBuilder Optional custom request builder for categories.
 *                                  Defaults to null (ViewModel creates default builder with limit=50).
 * @param enableListeners Whether to enable WebSocket listeners. Set to false for previews/testing.
 *                        Defaults to true for production use.
 * @param pollingIntervalMs Interval for unread count polling in milliseconds.
 *                          Defaults to 30000ms (30 seconds).
 */
class CometChatNotificationFeedViewModelFactory(
    private val feedRequestBuilder: NotificationFeedRequest.NotificationFeedRequestBuilder? = null,
    private val categoriesRequestBuilder: NotificationCategoriesRequest.NotificationCategoriesRequestBuilder? = null,
    private val enableListeners: Boolean = true,
    private val pollingIntervalMs: Long = 30_000L
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatNotificationFeedViewModel::class.java)) {
            return CometChatNotificationFeedViewModel(
                feedRequestBuilder = feedRequestBuilder,
                categoriesRequestBuilder = categoriesRequestBuilder,
                enableListeners = enableListeners,
                pollingIntervalMs = pollingIntervalMs
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
