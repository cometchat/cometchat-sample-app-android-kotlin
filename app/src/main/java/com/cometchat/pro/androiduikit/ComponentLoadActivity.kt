package com.cometchat.pro.androiduikit

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cometchat.pro.androiduikit.ComponentFragments.*
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.Conversation
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.ui_components.calls.call_list.CometChatCallList
import com.cometchat.pro.uikit.ui_components.chats.CometChatConversationList
import com.cometchat.pro.uikit.ui_components.messages.message_list.CometChatMessageListActivity
import com.cometchat.pro.uikit.ui_components.users.user_list.CometChatUserList
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.custom_alertDialog.CustomAlertDialogHelper
import com.cometchat.pro.uikit.ui_resources.utils.custom_alertDialog.OnAlertDialogButtonClickListener
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener
import com.cometchat.pro.uikit.ui_components.groups.group_list.CometChatGroupList
import com.cometchat.pro.uikit.ui_components.userProfile.CometChatUserProfile
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils

class ComponentLoadActivity : AppCompatActivity(), OnAlertDialogButtonClickListener {
    private var progressDialog: ProgressDialog? = null
    private var groupPassword: String? = null
    private var group: Group? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_fragment)
        val id = intent.getIntExtra("screen", 0)
        if (id == R.id.users) {
            loadFragment(CometChatUserList())
        } else if (id == R.id.calls) {
            loadFragment(CometChatCallList())
        } else if (id == R.id.groups) {
            loadFragment(CometChatGroupList())
        } else if (id == R.id.conversations) {
            loadFragment(CometChatConversationList())
        } else if (id == R.id.moreinfo) {
            loadFragment(CometChatUserProfile())
        } else if (id == R.id.cometchat_avatar) {
            loadFragment(AvatarFragment())
        } else if (id == R.id.cometchat_status_indicator) {
            loadFragment(StatusIndicatorFragment())
        } else if (id == R.id.cometchat_badge_count) {
            loadFragment(BadgeCountFragment())
        } else if (id == R.id.cometchat_user_view) {
            loadFragment(UserListViewFragment())
        } else if (id == R.id.cometchat_group_view) {
            loadFragment(GroupListViewFragment())
        } else if (id == R.id.cometchat_conversation_view) {
            loadFragment(ConversationListViewFragment())
        } else if (id == R.id.cometchat_callList) {
            loadFragment(CallListViewFragment())
        }
        CometChatUserList.setItemClickListener(object : OnItemClickListener<Any>() {
            override fun OnItemClick(t: Any, position: Int) {
                userIntent(t as User)
            }
        })
        CometChatGroupList.setItemClickListener(object : OnItemClickListener<Any>() {
            override fun OnItemClick(t: Any, position: Int) {
                group = t as Group
                if (group!!.isJoined) {
                    startGroupIntent(group)
                } else {
                    if (group!!.groupType == CometChatConstants.GROUP_TYPE_PASSWORD) {
                        val dialogview = layoutInflater.inflate(R.layout.cc_dialog, null)
                        val tvTitle = dialogview.findViewById<TextView>(R.id.textViewDialogueTitle)
                        tvTitle.text = ""
                        CustomAlertDialogHelper(this@ComponentLoadActivity, "Password", dialogview, "Join",
                                "", "Cancel", this@ComponentLoadActivity, 1, false)
                    } else if (group!!.groupType == CometChatConstants.GROUP_TYPE_PUBLIC) {
                        joinGroup(group)
                    }
                }
            }
        })
        CometChatConversationList.setItemClickListener(object : OnItemClickListener<Any>() {
            override fun OnItemClick(t: Any, position: Int) {
                val conversation = t as Conversation
                if (conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_GROUP) {
                    startGroupIntent(conversation.conversationWith as Group)
                } else {
                    val user = conversation.conversationWith as User
                    userIntent(user)
                }
            }
        })
    }

    private fun loadFragment(fragment: Fragment?) {
        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.frame, fragment).commit()
        }
    }

    fun userIntent(user: User) {
        val intent = Intent(this@ComponentLoadActivity, CometChatMessageListActivity::class.java)
        intent.putExtra(UIKitConstants.IntentStrings.UID, user.uid)
        intent.putExtra(UIKitConstants.IntentStrings.AVATAR, user.avatar)
        intent.putExtra(UIKitConstants.IntentStrings.STATUS, user.status)
        intent.putExtra(UIKitConstants.IntentStrings.NAME, user.name)
        intent.putExtra(UIKitConstants.IntentStrings.TYPE, CometChatConstants.RECEIVER_TYPE_USER)
        startActivity(intent)
    }

    private fun startGroupIntent(group: Group?) {
        val intent = Intent(this@ComponentLoadActivity, CometChatMessageListActivity::class.java)
        intent.putExtra(UIKitConstants.IntentStrings.GUID, group!!.guid)
        intent.putExtra(UIKitConstants.IntentStrings.GROUP_OWNER, group.owner)
        intent.putExtra(UIKitConstants.IntentStrings.AVATAR, group.icon)
        intent.putExtra(UIKitConstants.IntentStrings.NAME, group.name)
        intent.putExtra(UIKitConstants.IntentStrings.GROUP_TYPE, group.groupType)
        intent.putExtra(UIKitConstants.IntentStrings.TYPE, CometChatConstants.RECEIVER_TYPE_GROUP)
        intent.putExtra(UIKitConstants.IntentStrings.MEMBER_COUNT, group.membersCount)
        intent.putExtra(UIKitConstants.IntentStrings.GROUP_DESC, group.description)
        intent.putExtra(UIKitConstants.IntentStrings.GROUP_PASSWORD, group.password)
        startActivity(intent)
    }

    private fun joinGroup(group: Group?) {
        progressDialog = ProgressDialog.show(this, "", "Joining")
        progressDialog!!.setCancelable(false)
        CometChat.joinGroup(group!!.guid, group.groupType, groupPassword, object : CallbackListener<Group?>() {
            override fun onSuccess(group: Group?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                startGroupIntent(group)
            }

            override fun onError(e: CometChatException) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                ErrorMessagesUtils.cometChatErrorMessage(this@ComponentLoadActivity, e.code)
            }
        })
    }

    override fun onButtonClick(alertDialog: AlertDialog?, v: View?, which: Int, popupId: Int) {
        val groupPasswordInput = v!!.findViewById<View>(R.id.edittextDialogueInput) as EditText
        if (which == DialogInterface.BUTTON_NEGATIVE) { // Cancel
            alertDialog!!.dismiss()
        } else if (which == DialogInterface.BUTTON_POSITIVE) { // Join
            try {
                progressDialog = ProgressDialog.show(this, "", "Joining")
                progressDialog!!.setCancelable(false)
                groupPassword = groupPasswordInput.text.toString()
                if (groupPassword!!.isEmpty()) {
                    groupPasswordInput.setText("")
                    groupPasswordInput.error = "Incorrect"
                } else {
                    try {
                        alertDialog!!.dismiss()
                        joinGroup(group)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}