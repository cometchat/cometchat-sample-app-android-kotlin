package com.cometchat.sampleapp.kotlin.push.appflow.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.cometchat.uikit.kotlin.presentation.notificationfeed.ui.CometChatNotificationFeed

/**
 * Fragment displaying the CometChatNotificationFeed component.
 * Shows the notification feed with category filtering, timestamp grouping,
 * and engagement reporting.
 */
class NotificationsFragment : Fragment() {

    private var notificationFeed: CometChatNotificationFeed? = null

    companion object {
        private const val TAG = "NotificationsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Create the CometChatNotificationFeed view programmatically
        val frameLayout = FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        notificationFeed = CometChatNotificationFeed(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        frameLayout.addView(notificationFeed)
        return frameLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationFeed?.apply {
            setTitle("Notifications")
            setShowBackButton(false)
            setShowFilterChips(true)

            onItemClick = { feedItem ->
                Log.d(TAG, "Item clicked: ${feedItem.id}")
            }

            onActionClick = { feedItem, action ->
                Log.d(TAG, "Action clicked on item: ${feedItem.id}, action: $action")
                android.widget.Toast.makeText(
                    requireContext(),
                    "Action: ${action["type"]} | Element: ${action["elementId"]}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }

            onError = { exception ->
                Log.e(TAG, "Error: ${exception.message}")
            }

            // Initialize with the fragment as ViewModelStoreOwner
            init(this@NotificationsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        notificationFeed = null
    }
}
