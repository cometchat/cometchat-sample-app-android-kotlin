package com.cometchat.pro.uikit.ui_components.cometchat_ui

import android.Manifest
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.*
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.databinding.ActivityCometchatUnifiedBinding
import com.cometchat.pro.uikit.ui_components.calls.call_list.CometChatCallList
import com.cometchat.pro.uikit.ui_components.chats.CometChatConversationList
import com.cometchat.pro.uikit.ui_components.groups.group_list.CometChatGroupList
import com.cometchat.pro.uikit.ui_components.messages.message_list.CometChatMessageListActivity
import com.cometchat.pro.uikit.ui_components.userProfile.CometChatUserProfile
import com.cometchat.pro.uikit.ui_components.users.user_list.CometChatUserList
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import com.cometchat.pro.uikit.ui_resources.utils.custom_alertDialog.CustomAlertDialogHelper
import com.cometchat.pro.uikit.ui_resources.utils.custom_alertDialog.OnAlertDialogButtonClickListener
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction
import com.cometchat.pro.uikit.ui_settings.UIKitSettings
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.util.*

/**
 * Purpose - CometChatUnified class is main class used to launch the fully working chat application.
 * It consist of BottomNavigationBar which helps to navigate between different screens like
 * ConversationListScreen, UserListScreen, GroupListScreen, MoreInfoScreen.
 * @link= "https://prodocs.cometchat.com/docs/android-ui-unified"
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 16th January 2020
 */
class CometChatUI : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener, OnAlertDialogButtonClickListener {
    private var userSettingsEnabled: Boolean = false
    private var recentChatListEnabled: Boolean = false
    private var callListEnabled: Boolean = false
    private var groupListEnabled: Boolean = false
    private var userListEnabled: Boolean = false

    //Used to bind the layout with class
    private var activityCometChatUnifiedBinding: ActivityCometchatUnifiedBinding? = null

    //Stores the count of user whose messages are unread.
    private val unreadCount: MutableList<String> = ArrayList()
    private var badgeDrawable: BadgeDrawable? = null
    private var fragment: Fragment? = null
    private var progressDialog: ProgressDialog? = null
    private var groupPassword: String? = null
    private var group: Group? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityCometChatUnifiedBinding = DataBindingUtil.setContentView(this, R.layout.activity_cometchat_unified)
        initViewComponent()
        // It performs action on click of user item in CometChatUserListScreen.
        setUserClickListener()


        //It performs action on click of group item in CometChatGroupListScreen.
        //It checks whether the logged-In user is already a joined a group or not and based on it perform actions.
        setGroupClickListener()

        //It performs action on click of conversation item in CometChatConversationListScreen
        //Based on conversation item type it will perform the actions like open message screen for user and groups..
        setConversationClickListener()
    }

    private fun setConversationClickListener() {
        CometChatConversationList.Companion.setItemClickListener(object : OnItemClickListener<Any>() {
            override fun OnItemClick(t: Any, position: Int) {
                var conversation = t as Conversation;
                if (conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_GROUP)
                    startGroupIntent(conversation.conversationWith as Group) else startUserIntent(conversation.conversationWith as User)
            }
        })
    }

    private fun setGroupClickListener() {
        CometChatGroupList.Companion.setItemClickListener(object : OnItemClickListener<Any>() {
            override fun OnItemClick(t: Any, position: Int) {
                group = t as Group
                if (group!!.isJoined) {
                    startGroupIntent(group)
                } else {
                    if (group!!.groupType == CometChatConstants.GROUP_TYPE_PASSWORD) {
                        val dialogview = layoutInflater.inflate(R.layout.cc_dialog, null)
                        val tvTitle = dialogview.findViewById<TextView>(R.id.textViewDialogueTitle)
                        tvTitle.text = String.format(resources.getString(R.string.enter_password_to_join), group!!.name)
                        CustomAlertDialogHelper(this@CometChatUI, resources.getString(R.string.password), dialogview, resources.getString(R.string.join),
                                "", resources.getString(R.string.cancel), this@CometChatUI, 1, false)
                    } else if (group!!.groupType == CometChatConstants.GROUP_TYPE_PUBLIC) {
                        joinGroup(group)
                    }
                }
            }
        })
    }

    private fun setUserClickListener() {
        CometChatUserList.Companion.setItemClickListener(object : OnItemClickListener<Any>() {
            override fun OnItemClick(t: Any, position: Int) {
                startUserIntent(t as User)
            }
        })
    }

    /**
     * This method initialize the BadgeDrawable which is used on conversation menu of BottomNavigationBar to display unread conversations.
     * It Loads **CometChatConversationScreen** at initial phase.
     * @see CometChatConversationList
     */
    private fun initViewComponent() {
        if (!Utils.hasPermissions(this, Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        UIKitConstants.RequestCode.RECORD)
            }
        }
        badgeDrawable = activityCometChatUnifiedBinding!!.bottomNavigation.getOrCreateBadge(R.id.menu_conversation)
        activityCometChatUnifiedBinding!!.bottomNavigation.setOnNavigationItemSelectedListener(this)

        if (UIKitSettings.color != null && UIKitSettings.color.isNotEmpty()) {
            window.statusBarColor = Color.parseColor(UIKitSettings.color)
            val widgetColor = Color.parseColor(UIKitSettings.color)
            val colorStateList = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_selected), intArrayOf()), intArrayOf(Color.GRAY, widgetColor))
            activityCometChatUnifiedBinding?.bottomNavigation?.itemIconTintList = colorStateList
        }

        //        activityCometChatUnifiedBinding.bottomNavigation.getMenu().add(Menu.NONE,12,Menu.NONE,"Test").setIcon(R.drawable.ic_security_24dp);

        FeatureRestriction.isRecentChatListEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                recentChatListEnabled = p0
                activityCometChatUnifiedBinding?.bottomNavigation?.menu?.findItem(R.id.menu_conversation)?.isVisible = p0
            }
        })
        FeatureRestriction.isUserListEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                userListEnabled = p0
                activityCometChatUnifiedBinding?.bottomNavigation?.menu?.findItem(R.id.menu_users)?.isVisible = p0
            }
        })
        FeatureRestriction.isCallListEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                callListEnabled = p0
                activityCometChatUnifiedBinding?.bottomNavigation?.menu?.findItem(R.id.menu_call)?.isVisible = p0
            }
        })
        FeatureRestriction.isGroupListEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                groupListEnabled = p0
                activityCometChatUnifiedBinding?.bottomNavigation?.menu?.findItem(R.id.menu_group)?.isVisible = p0
            }

        })
        FeatureRestriction.isUserSettingsEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                userSettingsEnabled = p0
                activityCometChatUnifiedBinding?.bottomNavigation?.menu?.findItem(R.id.menu_more)?.isVisible =  p0
            }

        })


        badgeDrawable!!.isVisible = false
        activityCometChatUnifiedBinding!!.bottomNavigation.id = R.id.menu_conversation
        when {
            recentChatListEnabled -> loadFragment(CometChatConversationList())
            callListEnabled -> loadFragment(CometChatCallList())
            groupListEnabled -> loadFragment(CometChatGroupList())
            userSettingsEnabled -> loadFragment(CometChatUserProfile())
            userListEnabled -> loadFragment(CometChatUserList())
        }
    }

    /**
     * This methods joins the logged-In user in a group.
     *
     * @param group  The Group user will join.
     * @see Group
     *
     * @see CometChat.joinGroup
     */
    private fun joinGroup(group: Group?) {
        if (FeatureRestriction.isJoinLeaveGroupsEnabled()) {
            progressDialog = ProgressDialog.show(this, "", resources.getString(R.string.joining))
            progressDialog!!.setCancelable(false)
            CometChat.joinGroup(group!!.guid, group.groupType, groupPassword, object : CallbackListener<Group?>() {
                override fun onSuccess(group: Group?) {
                    if (progressDialog != null) progressDialog!!.dismiss()
                    group?.let { startGroupIntent(it) }
                }

                override fun onError(e: CometChatException) {
                    if (progressDialog != null) progressDialog!!.dismiss()
//                    ErrorMessagesUtils.cometChatErrorMessage(this@CometChatUI, e.code)
                    ErrorMessagesUtils.showCometChatErrorDialog(this@CometChatUI, resources.getString(R.string.enter_the_correct_password))
//                Snackbar.make(activityCometChatUnifiedBinding!!.bottomNavigation, resources.getString(R.string.unable_to_join_message) + e.message,
//                        Snackbar.LENGTH_SHORT).show()
                }
            })
        }
    }

    /**
     * Loads the fragment get from parameter.
     * @param fragment
     * @return true if fragment is not null
     */
    private fun loadFragment(fragment: Fragment?): Boolean {
        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.frame, fragment).commit()
            return true
        }
        return false
    }//Logs the error if the error occurs.//add total count of users and groups whose messages are unread in BadgeDrawable//Add users whose messages are unread.
    //Add groups whose messages are unread.

    /**
     * Get Unread Count of conversation using `CometChat.getUnreadMessageCount()`.
     * @see CometChat.getUnreadMessageCount
     */
    val unreadConversationCount: Unit
        get() {
            CometChat.getUnreadMessageCount(object : CallbackListener<HashMap<String?, HashMap<String, Int?>>>() {
                override fun onSuccess(stringHashMapHashMap: HashMap<String?, HashMap<String, Int?>>) {
                    Log.e(TAG, "onSuccess: unread $stringHashMapHashMap")
                    unreadCount.addAll(stringHashMapHashMap["user"]!!.keys) //Add users whose messages are unread.
                    unreadCount.addAll(stringHashMapHashMap["group"]!!.keys) //Add groups whose messages are unread.
                    badgeDrawable!!.isVisible = unreadCount.size != 0
                    if (unreadCount.size != 0) {
                        badgeDrawable!!.number = unreadCount.size //add total count of users and groups whose messages are unread in BadgeDrawable
                    }
                }

                override fun onError(e: CometChatException) {
                    Log.e("onError: ", e.message) //Logs the error if the error occurs.
                }
            })
        }

    /**
     * Set unread message count
     * @param message An object of **BaseMessage** class that is been used to set unread count in BadgeDrawable.
     * @see BaseMessage
     */
    private fun setUnreadCount(message: BaseMessage) {
//        if (message.editedAt == 0L && message.deletedAt == 0L) {
            if (message.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                if (!unreadCount.contains(message.receiverUid)) {
                    unreadCount.add(message.receiverUid)
                    setBadge()
                }
            } else {
                if (!unreadCount.contains(message.sender.uid)) {
                    unreadCount.add(message.sender.uid)
                    setBadge()
                }
            }
//        }
    }

    /**
     * Updating BadgeDrawable set on conversation menu in BottomNavigationBar
     */
    private fun setBadge() {
        badgeDrawable!!.isVisible = badgeDrawable!!.number != 0
        badgeDrawable!!.number = badgeDrawable!!.number + 1
    }

    /**
     * MessageListener to update unread count of conversations
     * @see CometChat.addMessageListener
     */
    fun addConversationListener() {
        CometChat.addMessageListener(TAG, object : CometChat.MessageListener() {
            override fun onTextMessageReceived(message: TextMessage) {
                setUnreadCount(message)
            }

            override fun onMediaMessageReceived(message: MediaMessage) {
                setUnreadCount(message)
            }

            override fun onCustomMessageReceived(message: CustomMessage) {
                setUnreadCount(message)
            }


        })
    }

    /**
     * Open Message Screen for user using **CometChatMessageListActivity.class**
     *
     * @param user
     * @see CometChatMessageListActivity
     */
    private fun startUserIntent(user: User) {
        Log.e(TAG, "startUserIntent: "+user.link )
        val intent = Intent(this@CometChatUI, CometChatMessageListActivity::class.java)
        intent.putExtra(UIKitConstants.IntentStrings.UID, user.uid)
        intent.putExtra(UIKitConstants.IntentStrings.AVATAR, user.avatar)
        intent.putExtra(UIKitConstants.IntentStrings.STATUS, user.status)
        intent.putExtra(UIKitConstants.IntentStrings.NAME, user.name)
        intent.putExtra(UIKitConstants.IntentStrings.TYPE, CometChatConstants.RECEIVER_TYPE_USER)
        intent.putExtra(UIKitConstants.IntentStrings.LINK, user.link)
        startActivity(intent)
    }

    /**
     * Open Message Screen for group using **CometChatMessageListActivity.class**
     *
     * @param group
     * @see CometChatMessageListActivity
     */
    private fun startGroupIntent(group: Group?) {
        val intent = Intent(this@CometChatUI, CometChatMessageListActivity::class.java)
        intent.putExtra(UIKitConstants.IntentStrings.GUID, group!!.guid)
        intent.putExtra(UIKitConstants.IntentStrings.AVATAR, group.icon)
        intent.putExtra(UIKitConstants.IntentStrings.GROUP_OWNER, group.owner)
        intent.putExtra(UIKitConstants.IntentStrings.NAME, group.name)
        intent.putExtra(UIKitConstants.IntentStrings.GROUP_TYPE, group.groupType)
        intent.putExtra(UIKitConstants.IntentStrings.TYPE, CometChatConstants.RECEIVER_TYPE_GROUP)
        intent.putExtra(UIKitConstants.IntentStrings.MEMBER_COUNT, group.membersCount)
        intent.putExtra(UIKitConstants.IntentStrings.GROUP_DESC, group.description)
        intent.putExtra(UIKitConstants.IntentStrings.GROUP_PASSWORD, group.password)
        startActivity(intent)
    }

    /**
     * Open various screen on fragment based on item selected from BottomNavigationBar
     * @param item
     * @return true if fragment is not null.
     * @see CometChatUserList
     *
     * @see CometChatGroupList
     *
     * @see CometChatConversationList
     *
     * @see CometChatUserProfile
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.menu_users) {
            fragment = CometChatUserList()
        } else if (itemId == R.id.menu_group) {
            fragment = CometChatGroupList()
        } else if (itemId == R.id.menu_conversation) {
            fragment = CometChatConversationList()
        } else if (itemId == R.id.menu_more) {
            fragment = CometChatUserProfile()
        } else if (itemId == R.id.menu_call) {
            fragment = CometChatCallList()
        }
        return loadFragment(fragment)
    }

    override fun onButtonClick(alertDialog: AlertDialog?, v: View?, which: Int, popupId: Int) {
        val groupPasswordInput = v!!.findViewById<View>(R.id.edittextDialogueInput) as EditText
        if (which == DialogInterface.BUTTON_NEGATIVE) { // Cancel
            alertDialog!!.dismiss()
        } else if (which == DialogInterface.BUTTON_POSITIVE) { // Join
            try {
                groupPassword = groupPasswordInput.text.toString()
                if (groupPassword!!.length == 0) {
                    groupPasswordInput.setText("")
                    groupPasswordInput.error = resources.getString(R.string.incorrect)
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

    override fun onStart() {
        super.onStart()
        addConversationListener() //Enable Listener when app starts
    }

    override fun onResume() {
        super.onResume()
        unreadConversationCount // To get unread conversations count
    }

    override fun onPause() {
        super.onPause()
        unreadCount.clear() //Clear conversation count when app pauses or goes background.
    }

    companion object {
        //Used to identify class in Log's
        private val TAG = CometChatUI::class.java.simpleName
    }
}