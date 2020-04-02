package com.cometchat.pro.androiduikit

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.cometchat.pro.androiduikit.ComponentFragments.AvatarFragment
import com.cometchat.pro.androiduikit.ComponentFragments.BadgeCountFragment
import com.cometchat.pro.androiduikit.ComponentFragments.CallListViewFragment
import com.cometchat.pro.androiduikit.ComponentFragments.ConversationListViewFragment
import com.cometchat.pro.androiduikit.ComponentFragments.GroupListViewFragment
import com.cometchat.pro.androiduikit.ComponentFragments.StatusIndicatorFragment
import com.cometchat.pro.androiduikit.ComponentFragments.UserListViewFragment
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.Conversation
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.User

import constant.StringContract
import listeners.CustomAlertDialogHelper
import listeners.OnAlertDialogButtonClickListener
import listeners.OnItemClickListener
import screen.CometChatConversationListScreen
import screen.call.CometChatCallListScreen
import screen.messagelist.CometChatMessageListActivity
import screen.CometChatGroupListScreen
import screen.CometChatUserInfoScreen
import screen.CometChatUserListScreen

class ComponentLoadActivity : AppCompatActivity(), OnAlertDialogButtonClickListener {

    private var progressDialog: ProgressDialog? = null
    private var groupPassword: String? = null
    private val group: Group? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_fragment)
        val id = intent.getIntExtra("screen", 0)
        if (id == R.id.users) {
            loadFragment(CometChatUserListScreen())

        } else if (id == R.id.calls) {
            loadFragment(CometChatCallListScreen())
        } else if (id == R.id.groups) {
            loadFragment(CometChatGroupListScreen())
        } else if (id == R.id.conversations) {
            loadFragment(CometChatConversationListScreen())
        } else if (id == R.id.moreinfo) {
            loadFragment(CometChatUserInfoScreen())
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
        CometChatUserListScreen.setItemClickListener(object : OnItemClickListener<User>() {
            override fun OnItemClick(`var`: User, position: Int) {
                userIntent(`var`)
            }
        })


        CometChatGroupListScreen.setItemClickListener(object : OnItemClickListener<Group>() {
            override fun OnItemClick(`var`: Group, position: Int) {
                if (group!!.isJoined) {
                    startGroupIntent(group)
                } else {
                    if (group.groupType == CometChatConstants.GROUP_TYPE_PASSWORD) {
                        val dialogview = layoutInflater.inflate(R.layout.cc_dialog, null)
                        val tvTitle = dialogview.findViewById<TextView>(R.id.textViewDialogueTitle)
                        tvTitle.text = ""
                        CustomAlertDialogHelper(this@ComponentLoadActivity, "Password", dialogview, "Join",
                                "", "Cancel", this@ComponentLoadActivity, 1, false)
                    } else if (group.groupType == CometChatConstants.GROUP_TYPE_PUBLIC) {
                        joinGroup(group)
                    }
                }
            }
        })


        CometChatConversationListScreen.setItemClickListener(object : OnItemClickListener<Conversation>() {
            override fun OnItemClick(conversation: Conversation, position: Int) {
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
        intent.putExtra(StringContract.IntentStrings.UID, user.uid)
        intent.putExtra(StringContract.IntentStrings.AVATAR, user.avatar)
        intent.putExtra(StringContract.IntentStrings.STATUS, user.status)
        intent.putExtra(StringContract.IntentStrings.NAME, user.name)
        intent.putExtra(StringContract.IntentStrings.TYPE, CometChatConstants.RECEIVER_TYPE_USER)
        startActivity(intent)
    }

    private fun startGroupIntent(group: Group) {

        val intent = Intent(this@ComponentLoadActivity, CometChatMessageListActivity::class.java)
        intent.putExtra(StringContract.IntentStrings.GUID, group.guid)
        intent.putExtra(StringContract.IntentStrings.GROUP_OWNER, group.owner)
        intent.putExtra(StringContract.IntentStrings.AVATAR, group.icon)
        intent.putExtra(StringContract.IntentStrings.NAME, group.name)
        intent.putExtra(StringContract.IntentStrings.TYPE, CometChatConstants.RECEIVER_TYPE_GROUP)
        startActivity(intent)
    }

    private fun joinGroup(group: Group) {
        progressDialog = ProgressDialog.show(this, "", "Joining")
        progressDialog!!.setCancelable(false)
        CometChat.joinGroup(group.guid, group.groupType, groupPassword, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(group: Group) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                startGroupIntent(group)
            }

            override fun onError(e: CometChatException) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                Toast.makeText(this@ComponentLoadActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onButtonClick(alertDialog: AlertDialog, v: View, which: Int, popupId: Int) {
        val groupPasswordInput = v.findViewById<View>(R.id.edittextDialogueInput) as EditText
        if (which == DialogInterface.BUTTON_NEGATIVE) { // Cancel

            alertDialog.dismiss()
        } else if (which == DialogInterface.BUTTON_POSITIVE) { // Join
            try {
                progressDialog = ProgressDialog.show(this, "", "Joining")
                progressDialog!!.setCancelable(false)
                groupPassword = groupPasswordInput.text.toString()
                if (groupPassword!!.length == 0) {
                    groupPasswordInput.setText("")
                    groupPasswordInput.error = "Incorrect"

                } else {
                    try {
                        alertDialog.dismiss()
                        joinGroup(group!!)

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
