package com.cometchat.sampleapp.kotlin.ui.chats

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.databinding.FragmentChatsBinding
import com.cometchat.sampleapp.kotlin.ui.messages.MessagesActivity
import com.cometchat.sampleapp.kotlin.ui.newchat.NewChatActivity
import com.cometchat.sampleapp.kotlin.ui.splash.SplashActivity
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatar

/**
 * Fragment displaying the list of conversations (Chats tab).
 *
 * This fragment uses the CometChatConversations component from chatuikit-kotlin
 * to display all active conversations. It handles:
 * - Displaying conversation list with last message preview
 * - Showing unread message count badges
 * - Navigation to message screen on conversation tap
 * - Pull-to-refresh functionality
 * - Loading and empty states
 * - Overflow menu with user avatar for logout and create conversation
 *
 * ## Architecture:
 * - Uses ViewBinding for type-safe view access
 * - Delegates business logic to CometChatConversations component
 * - The component internally uses CometChatConversationsViewModel from chatuikit-core
 *
 * @see com.cometchat.uikit.kotlin.presentation.conversations.ui.CometChatConversations
 *
 * Validates: Requirements 8.1, 8.2, 8.4, 8.5, 10.1
 */
class ChatsFragment : Fragment() {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!
    
    private val TAG = "ChatsFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupConversationList()
    }

    /**
     * Sets up the CometChatConversations component.
     *
     * Configures click handlers and customizations for the conversation list.
     * 
     * Validates: Requirement 8.1 (Display CometChatConversations component)
     */
    private fun setupConversationList() {
        binding.conversationList.apply {
            // Set click handler for conversation items
            // Validates: Requirement 8.2 (Navigate to MessagesActivity on conversation click)
            setOnItemClick { conversation ->
                navigateToMessages(conversation)
            }
            
            // Set up overflow menu with user avatar
            // Validates: Requirement 8.4
            setOverflowMenu(createOverflowMenuView())
        }
    }

    /**
     * Creates the overflow menu view containing user avatar.
     * 
     * Validates: Requirement 8.4
     */
    private fun createOverflowMenuView(): View? {
        if (!CometChatUIKit.isSDKInitialized()) return null
        val loggedInUser = CometChatUIKit.getLoggedInUser() ?: return null
        
        val avatar = CometChatAvatar(requireContext()).apply {
            setAvatar(loggedInUser.name, loggedInUser.avatar)
            val size = resources.getDimensionPixelSize(com.cometchat.uikit.kotlin.R.dimen.cometchat_40dp)
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
            setOnClickListener { view ->
                showUserMenu(view)
            }
        }
        return avatar
    }

    /**
     * Shows the user popup menu with options matching the reference app styling.
     * 
     * Validates: Requirements 8.4, 8.5 (Profile menu includes logout functionality)
     */
    private fun showUserMenu(anchorView: View) {
        val loggedInUser = CometChatUIKit.getLoggedInUser() ?: return
        
        val popupView = LayoutInflater.from(requireContext())
            .inflate(R.layout.popup_user_menu, null)
        
        val popupWindow = PopupWindow(
            popupView,
            resources.getDimensionPixelSize(com.cometchat.uikit.kotlin.R.dimen.cometchat_200dp),
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        
        // Set up user name
        popupView.findViewById<TextView>(R.id.tv_user_name)?.apply {
            text = loggedInUser.name
        }
        
        // Set up Create Conversation click - navigate to Users tab
        popupView.findViewById<TextView>(R.id.tv_create_conversation)?.setOnClickListener {
            popupWindow.dismiss()
            val intent = Intent(requireContext(), NewChatActivity::class.java)
            startActivity(intent)
        }
        
        // Set up Logout click
        // Validates: Requirement 8.5 (Profile menu includes logout functionality)
        popupView.findViewById<TextView>(R.id.tv_logout)?.setOnClickListener {
            popupWindow.dismiss()
            performLogout()
        }
        
        // Set elevation for shadow effect
        popupWindow.elevation = resources.getDimension(com.cometchat.uikit.kotlin.R.dimen.cometchat_elevation_1)
        
        // Position the popup below the anchor with proper offset
        val endMargin = resources.getDimensionPixelSize(com.cometchat.uikit.kotlin.R.dimen.cometchat_margin_2)
        val anchorWidth = anchorView.width
        val offsetX = anchorWidth - popupWindow.width - endMargin
        popupWindow.showAsDropDown(anchorView, offsetX, 0)
    }

    /**
     * Navigates to the Users tab in the bottom navigation.
     */
    private fun navigateToUsersTab() {
        // Navigate to Users tab using bottom navigation
        activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
            R.id.bottomNavigationView
        )?.selectedItemId = R.id.nav_users
    }

    /**
     * Performs logout and navigates to SplashActivity.
     * 
     * Validates: Requirements 8.5 (Profile menu includes logout functionality), 11.5 (Logout navigates to SplashActivity)
     */
    private fun performLogout() {
        CometChat.logout(object : CometChat.CallbackListener<String>() {
            override fun onSuccess(message: String) {
                Log.d(TAG, "Logout successful")
                // Navigate to SplashActivity and clear back stack
                // Validates: Requirement 11.5 (Logout navigates to SplashActivity and clears session)
                val intent = Intent(requireContext(), SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                activity?.finish()
            }

            override fun onError(e: CometChatException) {
                Log.e(TAG, "Logout failed: ${e.message}")
                // Remain on current screen - no action needed
            }
        })
    }

    /**
     * Navigates to the Messages screen for the selected conversation.
     *
     * @param conversation The selected conversation
     * 
     * Validates: Requirement 8.2 (Navigate to MessagesActivity on conversation click)
     */
    private fun navigateToMessages(conversation: Conversation) {
        val conversationType = conversation.conversationType
        when (conversationType) {
            CometChatConstants.CONVERSATION_TYPE_USER -> {
                val user = conversation.conversationWith as? User
                user?.let {
                    // Navigate to one-on-one chat
                    MessagesActivity.start(requireContext(), user = it)
                }
            }
            CometChatConstants.CONVERSATION_TYPE_GROUP -> {
                val group = conversation.conversationWith as? Group
                group?.let {
                    // Navigate to group chat
                    MessagesActivity.start(requireContext(), group = it)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
