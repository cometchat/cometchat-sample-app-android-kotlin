package com.cometchat.sampleapp.kotlin.fcm.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.fragment.app.Fragment

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.chatuikit.CometChatTheme
import com.cometchat.chatuikit.logger.CometChatLogger
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit
import com.cometchat.chatuikit.shared.interfaces.OnItemClick
import com.cometchat.chatuikit.shared.resources.utils.Utils
import com.cometchat.chatuikit.shared.views.avatar.CometChatAvatar
import com.cometchat.sampleapp.kotlin.fcm.BuildConfig
import com.cometchat.sampleapp.kotlin.fcm.R
import com.cometchat.sampleapp.kotlin.fcm.data.interfaces.OnItemClickListener
import com.cometchat.sampleapp.kotlin.fcm.data.repository.Repository
import com.cometchat.sampleapp.kotlin.fcm.databinding.FragmentChatsBinding
import com.cometchat.sampleapp.kotlin.fcm.databinding.UserProfilePopupMenuLayoutBinding
import com.cometchat.sampleapp.kotlin.fcm.fcm.FCMMessageDTO
import com.cometchat.sampleapp.kotlin.fcm.ui.activity.MessagesActivity
import com.cometchat.sampleapp.kotlin.fcm.ui.activity.SplashActivity
import com.cometchat.sampleapp.kotlin.fcm.utils.AppConstants
import com.cometchat.sampleapp.kotlin.fcm.utils.MyApplication
import com.google.gson.Gson

/**
 * A fragment representing the chat interface where users can see their
 * conversations and interact with them.
 */
class ChatsFragment : Fragment() {

    private val TAG: String = ChatsFragment::class.java.simpleName
    private lateinit var binding: FragmentChatsBinding
    private var listener: OnItemClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnItemClickListener) {
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set up item click listener for the conversations view
        binding.cometchatConversations.onItemClick = OnItemClick { view, position, conversation ->
            if (conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_GROUP) {
                val group = conversation.conversationWith as Group
                val intent = Intent(context, MessagesActivity::class.java)
                intent.putExtra(getString(R.string.app_group), Gson().toJson(group))
                startActivity(intent)
            } else {
                val user = conversation.conversationWith as User
                val intent = Intent(context, MessagesActivity::class.java)
                intent.putExtra(getString(R.string.app_user), Gson().toJson(user))
                startActivity(intent)
            }
        } // Set the overflow menu (Logout button) in the Conversations view
        binding.cometchatConversations.setOverflowMenu(logoutView)
        handleDeepLinking()
    }

    private val logoutView: View?
        /**
         * Creates a logout view that displays a logout icon and handles logout clicks.
         *
         * @return A View representing the logout option.
         */
        get() {
            if (!CometChatUIKit.isSDKInitialized()) return null
            val user: User? = CometChatUIKit.getLoggedInUser()
            if (user != null) {
                val cometchatAvatar = CometChatAvatar(requireContext())
                cometchatAvatar.setAvatar(user.name, user.avatar)
                val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(resources.getDimensionPixelSize(
                        com.cometchat.chatuikit.R.dimen.cometchat_40dp
                    ), resources.getDimensionPixelSize(com.cometchat.chatuikit.R.dimen.cometchat_40dp))
                layoutParams.layoutDirection = Gravity.CENTER_VERTICAL
                cometchatAvatar.setLayoutParams(layoutParams)
                cometchatAvatar.setOnClickListener { v: View ->
                    showCustomMenu(binding.cometchatConversations.binding.toolbar)
                }
                return cometchatAvatar
            }
            return null
        }

    // Inside your Activity or Fragment
    private fun showCustomMenu(anchorView: View) {
        val popupMenuBinding = UserProfilePopupMenuLayoutBinding.inflate(LayoutInflater.from(requireContext()))
        val popupWindow = PopupWindow(
            popupMenuBinding.root, resources.getDimensionPixelSize(
                com.cometchat.chatuikit.R.dimen.cometchat_200dp
            ), LinearLayout.LayoutParams.WRAP_CONTENT, true
        )
        MyApplication.popupWindows.add(popupWindow)
        popupMenuBinding.tvUserName.text = CometChatUIKit.getLoggedInUser().name
        val version = (("V" + BuildConfig.VERSION_NAME) + "(" + BuildConfig.VERSION_CODE) + ")"
        popupMenuBinding.tvVersion.text = version

        popupMenuBinding.tvCreateConversation.setOnClickListener { _: View ->
            popupWindow.dismiss()
            listener?.onItemClick()
        }

        popupMenuBinding.tvUserName.setOnClickListener { _: View ->
            popupWindow.dismiss()
        }

        popupMenuBinding.tvLogout.setOnClickListener { _: View ->
            Repository.unregisterFCMToken(object : CometChat.CallbackListener<String>() {
                override fun onSuccess(s: String) {
                    Repository.logout(object : CometChat.CallbackListener<String>() {
                        override fun onSuccess(s: String) {
                            startActivity(
                                Intent(
                                    context, SplashActivity::class.java
                                )
                            )
                            requireActivity().finish()
                        }

                        override fun onError(e: CometChatException) {
                            binding.cometchatConversations.setOverflowMenu(logoutView)
                        }
                    })
                }

                override fun onError(e: CometChatException) {
                    binding.cometchatConversations.setOverflowMenu(logoutView)
                }
            })
            popupWindow.dismiss()
        }

        popupMenuBinding.tvUserName.setTextColor(CometChatTheme.getTextColorPrimary(requireContext()))
        popupMenuBinding.tvCreateConversation.setTextColor(CometChatTheme.getTextColorPrimary(requireContext()))
        popupMenuBinding.tvUserName.setTextColor(CometChatTheme.getTextColorPrimary(requireContext()))

        popupWindow.elevation = 5f
        val endMargin = resources.getDimensionPixelSize(com.cometchat.chatuikit.R.dimen.cometchat_margin_2)
        val anchorWidth = anchorView.width
        val offsetX: Int = anchorWidth - popupWindow.width - endMargin
        val offsetY = 0
        popupWindow.showAsDropDown(anchorView, offsetX, offsetY)
    }

    private fun handleDeepLinking() {
        val args: Bundle? = arguments
        if (args != null) {
            val notificationType: String? = args.getString(AppConstants.FCMConstants.NOTIFICATION_TYPE)
            val notificationPayload: String? = args.getString(AppConstants.FCMConstants.NOTIFICATION_PAYLOAD)
            if (AppConstants.FCMConstants.NOTIFICATION_TYPE_MESSAGE == notificationType) {
                val fcmMessageDTO: FCMMessageDTO = Gson().fromJson(
                    notificationPayload, FCMMessageDTO::class.java
                )
                val isUser = fcmMessageDTO.receiverType == CometChatConstants.RECEIVER_TYPE_USER
                val uid = if (isUser) fcmMessageDTO.sender else fcmMessageDTO.receiver
                if (isUser) {
                    Repository.getUser(uid!!, object : CometChat.CallbackListener<User>() {
                        override fun onSuccess(user: User?) {
                            val intent = Intent(context, MessagesActivity::class.java)
                            intent.putExtra(getString(R.string.app_user), Gson().toJson(user))
                            startActivity(intent)
                        }

                        override fun onError(e: CometChatException) {
                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                            CometChatLogger.e(TAG, e.toString())
                        }
                    })
                } else {
                    Repository.getGroup(uid!!, object : CometChat.CallbackListener<Group>() {
                        override fun onSuccess(group: Group) {
                            val intent = Intent(context, MessagesActivity::class.java)
                            intent.putExtra(getString(R.string.app_group), Gson().toJson(group))
                            startActivity(intent)
                        }

                        override fun onError(e: CometChatException) {
                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                            CometChatLogger.e(TAG, e.toString())
                        }
                    })
                }
            }
        }
    }
}
