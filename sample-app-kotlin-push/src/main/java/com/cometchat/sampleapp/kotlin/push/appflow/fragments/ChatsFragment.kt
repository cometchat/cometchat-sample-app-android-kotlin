package com.cometchat.sampleapp.kotlin.push.appflow.fragments

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
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.sampleapp.kotlin.push.LoginActivity
import com.cometchat.sampleapp.kotlin.push.R
import com.cometchat.sampleapp.kotlin.push.appflow.MessagesActivity
import com.cometchat.sampleapp.kotlin.push.appflow.NewChatActivity
import com.cometchat.sampleapp.kotlin.push.appflow.SearchActivity
import com.cometchat.sampleapp.kotlin.push.databinding.FragmentChatsBinding
import com.cometchat.pushnotification.CometChatPushNotifications
import com.google.gson.Gson

/**
 * Fragment displaying the CometChatConversations component.
 * Handles conversation clicks to navigate to MessagesActivity.
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

    private fun setupConversationList() {
        binding.cometchatConversations.apply {
            // Set up item click listener for conversations using lambda callback pattern
            setOnItemClick { conversation ->
                if (conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_GROUP) {
                    val group = conversation.conversationWith as Group
                    navigateToMessages(group = group)
                } else {
                    val user = conversation.conversationWith as User
                    navigateToMessages(user = user)
                }
            }
            
            // Set up search click listener
            setOnSearchClick {
                val intent = Intent(requireContext(), SearchActivity::class.java)
                startActivity(intent)
            }
            
            // Set up overflow menu with user avatar
            setOverflowMenu(createUserAvatarView())
        }
    }

    /**
     * Creates a user avatar view for the overflow menu.
     */
    private fun createUserAvatarView(): View? {
        if (!CometChatUIKit.isSDKInitialized()) return null
        val loggedInUser = CometChatUIKit.getLoggedInUser() ?: return null
        
        return CometChatAvatar(requireContext()).apply {
            setAvatar(loggedInUser.name, loggedInUser.avatar)
            val size = resources.getDimensionPixelSize(com.cometchat.uikit.kotlin.R.dimen.cometchat_40dp)
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
            setOnClickListener { view ->
                showUserMenu(view)
            }
        }
    }

    /**
     * Shows the user popup menu with options matching the reference app styling.
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
        
        // Set up user name - uses theme colors from XML
        popupView.findViewById<TextView>(R.id.tv_user_name)?.apply {
            text = loggedInUser.name
        }
        
        // Set up Create Conversation click
        popupView.findViewById<TextView>(R.id.tv_create_conversation)?.setOnClickListener {
            popupWindow.dismiss()
            val intent = Intent(requireContext(), NewChatActivity::class.java)
            startActivity(intent)
        }
        
        // Set up Logout click - uses error color from XML
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
     * Performs logout and navigates to LoginActivity.
     * On success: Navigate to LoginActivity with FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK
     * On failure: Log error and remain on current screen
     * 
     * Validates: Requirements 11.1, 11.2, 11.3, 11.4, 11.5
     */
    private fun performLogout() {
        // Always unregister the FCM token BEFORE logging out to prevent ghost notifications.
        CometChatPushNotifications.unregisterToken(
            onSuccess = {
                Log.d(TAG, "FCM token unregistered")
                doLogout()
            },
            onError = { e ->
                Log.e(TAG, "Token unregister failed (proceeding with logout): ${e.message}")
                doLogout()
            }
        )
    }

    private fun doLogout() {
        CometChat.logout(object : CometChat.CallbackListener<String>() {
            override fun onSuccess(message: String) {
                Log.d(TAG, "Logout successful")
                // Navigate to LoginActivity and clear back stack
                val intent = Intent(requireContext(), LoginActivity::class.java)
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
     * Navigates to MessagesActivity with the given user or group.
     */
    private fun navigateToMessages(user: User? = null, group: Group? = null) {
        Log.d(TAG, "Navigate to messages - User: ${user?.name}, Group: ${group?.name}")
        
        val intent = Intent(requireContext(), MessagesActivity::class.java)
        user?.let { intent.putExtra(getString(R.string.app_user), it.toJson().toString()) }
        group?.let { intent.putExtra(getString(R.string.app_group), Gson().toJson(it)) }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
