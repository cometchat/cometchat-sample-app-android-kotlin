package com.cometchat.pro.uikit.ui_components.users.user_details

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.core.CometChat.GroupListener
import com.cometchat.pro.core.MessagesRequest
import com.cometchat.pro.core.MessagesRequest.MessagesRequestBuilder
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.*
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.messages.extensions.collaborative.CometChatCollaborativeActivity
import com.cometchat.pro.uikit.ui_components.shared.cometchatSharedMedia.CometChatSharedMedia
import com.cometchat.pro.uikit.ui_components.messages.message_list.CometChatMessageListActivity
import com.cometchat.pro.uikit.ui_components.shared.cometchatAvatar.CometChatAvatar
import com.cometchat.pro.uikit.ui_components.users.user_details.callHistory.CallHistoryAdapter
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction
import com.google.android.material.appbar.MaterialToolbar
import java.util.*

class CometChatUserDetailScreenActivity constructor() : AppCompatActivity() {
    private var tvViewProfile: TextView?= null
    private var tvAction: TextView?= null
    private var userAvatar: CometChatAvatar? = null
    private var userStatus: TextView? = null
    private var userName: TextView? = null
    private var addBtn: TextView? = null
    private var name: String? = null
    private val TAG: String = "CometChatUserDetailScreenActivity"
    private var avatar: String? = null
    private var link: String? = null
    private var uid: String? = null
    private var guid: String? = null
    private var groupName: String? = null
    private var isAddMember: Boolean = false
    private var isAlreadyAdded: Boolean = false
    private var tvSendMessage: TextView? = null
    private var blockUserLayout: LinearLayout? = null
    private var tvBlockUser: TextView? = null
    private var toolbar: MaterialToolbar? = null
    private var isBlocked: Boolean = false
    private var fontUtils: FontUtils? = null
//    private var callBtn: ImageView? = null
//    private var vidoeCallBtn: ImageView? = null
    private var historyView: LinearLayout? = null
    private var historyRv: RecyclerView? = null
    private var callHistoryAdapter: CallHistoryAdapter? = null
    private var messageRequest: MessagesRequest? = null
    private var sharedMediaView: CometChatSharedMedia? = null
    private var sharedMediaLayout: LinearLayout? = null
    private val inProgress: Boolean = false
    private var fromCallList: Boolean = false
    private var divider1: View? = null
    private var divider2: View? = null
    private var divider3: View? = null
    private val callList: MutableList<BaseMessage> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cometchat_user_detail)
        fontUtils = FontUtils.getInstance(this)
        initComponent()
    }

    private fun initComponent() {
        historyView = findViewById(R.id.history_view)
        historyRv = findViewById(R.id.history_rv)
        userAvatar = findViewById(R.id.iv_user)
        userName = findViewById(R.id.tv_name)
        userStatus = findViewById(R.id.tv_status)
//        callBtn = findViewById(R.id.callBtn_iv)
//        vidoeCallBtn = findViewById(R.id.video_callBtn_iv)
        addBtn = findViewById(R.id.btn_add)
        tvViewProfile = findViewById(R.id.tv_view_profile)
        tvAction = findViewById(R.id.tv_action)
        tvSendMessage = findViewById(R.id.tv_send_message)
        toolbar = findViewById(R.id.user_detail_toolbar)
        divider1 = findViewById(R.id.divider_1)
        divider2 = findViewById(R.id.divider_2)
        divider3 = findViewById(R.id.divider_3)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) supportActionBar?.setDisplayHomeAsUpEnabled(true)
        addBtn!!.typeface = fontUtils!!.getTypeFace(FontUtils.robotoRegular)
        blockUserLayout = findViewById(R.id.block_user_layout)
        tvBlockUser = findViewById(R.id.tv_blockUser)
        tvBlockUser!!.typeface = fontUtils!!.getTypeFace(FontUtils.robotoMedium)
        userName!!.typeface = fontUtils!!.getTypeFace(FontUtils.robotoMedium)
        handleIntent()
        sharedMediaLayout = findViewById(R.id.shared_media_layout)
        sharedMediaView = findViewById(R.id.shared_media_view)
        sharedMediaView!!.setRecieverId(uid)
        sharedMediaView!!.setRecieverType(CometChatConstants.RECEIVER_TYPE_USER)
        sharedMediaView!!.reload()

        if (!FeatureRestriction.isSharedMediaEnabled()) sharedMediaLayout?.visibility = View.GONE
        checkDarkMode()
        Log.e(TAG, "initComponent: "+link.toString() )
        if (FeatureRestriction.isViewProfileEnabled()) {
            if (link == null) {
                tvAction?.visibility = View.GONE
                tvViewProfile?.visibility = View.GONE
            } else {
                tvAction?.visibility = View.VISIBLE
                tvViewProfile?.visibility = View.VISIBLE
            }
        }

        tvViewProfile?.setOnClickListener {
            var intent = Intent(this, CometChatCollaborativeActivity::class.java)
            intent.putExtra(UIKitConstants.IntentStrings.URL, link)
            startActivity(intent)
        }
        addBtn?.setOnClickListener(View.OnClickListener { view: View? ->
            if (guid != null) {
                if (isAddMember) {
                    if (isAlreadyAdded) kickGroupMember() else addMember()
                }
            }
        })
        tvSendMessage!!.setOnClickListener(View.OnClickListener {
            if (isAddMember || fromCallList) {
                val intent: Intent = Intent(this@CometChatUserDetailScreenActivity, CometChatMessageListActivity::class.java)
                intent.putExtra(UIKitConstants.IntentStrings.TYPE, CometChatConstants.RECEIVER_TYPE_USER)
                intent.putExtra(UIKitConstants.IntentStrings.UID, uid)
                intent.putExtra(UIKitConstants.IntentStrings.NAME, name)
                intent.putExtra(UIKitConstants.IntentStrings.AVATAR, avatar)
                intent.putExtra(UIKitConstants.IntentStrings.STATUS, CometChatConstants.USER_STATUS_OFFLINE)
                startActivity(intent)
            } else onBackPressed()
        })
        tvBlockUser!!.setOnClickListener(View.OnClickListener({ view: View? -> if (isBlocked) unblockUser() else blockUser() }))
//        callBtn!!.setOnClickListener(object : View.OnClickListener {
//            public override fun onClick(v: View) {
//                checkOnGoingCall(CometChatConstants.CALL_TYPE_AUDIO)
//            }
//        })
//        vidoeCallBtn!!.setOnClickListener(object : View.OnClickListener {
//            public override fun onClick(v: View) {
//                checkOnGoingCall(CometChatConstants.CALL_TYPE_VIDEO)
//            }
//        })

        FeatureRestriction.isBlockUserEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                if (!p0)
                    blockUserLayout?.visibility = View.GONE
            }
        })
        tvBlockUser!!.setOnClickListener(View.OnClickListener { view: View? -> if (isBlocked) unblockUser() else blockUser() })

//        FeatureRestriction.isOneOnOneAudioCallEnabled(object : FeatureRestriction.OnSuccessListener{
//            override fun onSuccess(p0: Boolean) {
//                if (p0) callBtn?.visibility = View.VISIBLE else callBtn?.visibility = View.GONE
//            }
//        })
//        FeatureRestriction.isOneOnOneVideoCallEnabled(object : FeatureRestriction.OnSuccessListener{
//            override fun onSuccess(p0: Boolean) {
//                if (p0) videoCallBtn?.visibility = View.VISIBLE else videoCallBtn?.visibility = View.GONE
//            }
//
//        })

//        if (UIKitSettings.color != null) {
//            window.statusBarColor = Color.parseColor(UIKitSettings.color)
//            callBtn?.imageTintList = ColorStateList.valueOf(
//                    Color.parseColor(UIKitSettings.color))
//            videoCallBtn?.setImageTintList(ColorStateList.valueOf(
//                    Color.parseColor(UIKitSettings.color)))
//        }
//        callBtn!!.setOnClickListener(object : View.OnClickListener {
//            public override fun onClick(v: View) {
//                checkOnGoingCall(CometChatConstants.CALL_TYPE_AUDIO)
//            }
//        })
//        videoCallBtn!!.setOnClickListener(object : View.OnClickListener {
//            public override fun onClick(v: View) {
//                checkOnGoingCall(CometChatConstants.CALL_TYPE_VIDEO)
//            }
//        })
    }

    private fun checkDarkMode() {
        if (Utils.isDarkMode(this)) {
            userName!!.setTextColor(resources.getColor(R.color.textColorWhite))
            divider1!!.setBackgroundColor(resources.getColor(R.color.grey))
            divider2!!.setBackgroundColor(resources.getColor(R.color.grey))
            divider3!!.setBackgroundColor(resources.getColor(R.color.grey))
        } else {
            userName!!.setTextColor(resources.getColor(R.color.primaryTextColor))
            divider1!!.setBackgroundColor(resources.getColor(R.color.light_grey))
            divider2!!.setBackgroundColor(resources.getColor(R.color.light_grey))
            divider3!!.setBackgroundColor(resources.getColor(R.color.light_grey))
        }
    }

//    private fun checkOnGoingCall(callType: String) {
//        if ((CometChat.getActiveCall() != null) && (CometChat.getActiveCall().callStatus == CometChatConstants.CALL_STATUS_ONGOING) && (CometChat.getActiveCall().sessionId != null)) {
//            val alert: AlertDialog.Builder = AlertDialog.Builder(this)
//            alert.setTitle(resources.getString(R.string.ongoing_call))
//                    .setMessage(resources.getString(R.string.ongoing_call_message))
//                    .setPositiveButton(resources.getString(R.string.join), object : DialogInterface.OnClickListener {
//                        public override fun onClick(dialog: DialogInterface, which: Int) {
//                            Utils.joinOnGoingCall(this@CometChatUserDetailScreenActivity)
//                        }
//                    }).setNegativeButton(resources.getString(R.string.cancel), object : DialogInterface.OnClickListener {
//                        public override fun onClick(dialog: DialogInterface, which: Int) {
//                            callBtn!!.isEnabled = true
//                            vidoeCallBtn!!.isEnabled = true
//                            dialog.dismiss()
//                        }
//                    }).create().show()
//        } else {
//            Utils.initiatecall(this@CometChatUserDetailScreenActivity, uid, CometChatConstants.RECEIVER_TYPE_USER, callType)
//        }
//    }

    private fun handleIntent() {
        if (intent.hasExtra(UIKitConstants.IntentStrings.IS_ADD_MEMBER)) {
            isAddMember = intent.getBooleanExtra(UIKitConstants.IntentStrings.IS_ADD_MEMBER, false)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.FROM_CALL_LIST)) {
            fromCallList = intent.getBooleanExtra(UIKitConstants.IntentStrings.FROM_CALL_LIST, false)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.IS_BLOCKED_BY_ME)) {
            isBlocked = intent.getBooleanExtra(UIKitConstants.IntentStrings.IS_BLOCKED_BY_ME, false)
            setBlockUnblock()
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.GUID)) {
            guid = intent.getStringExtra(UIKitConstants.IntentStrings.GUID)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.UID)) {
            uid = intent.getStringExtra(UIKitConstants.IntentStrings.UID)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.GROUP_NAME)) {
            groupName = intent.getStringExtra(UIKitConstants.IntentStrings.GROUP_NAME)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.NAME)) {
            name = intent.getStringExtra(UIKitConstants.IntentStrings.NAME)
            userName!!.text = name
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.AVATAR)) {
            avatar = intent.getStringExtra(UIKitConstants.IntentStrings.AVATAR)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.STATUS)) {
            val status: String? = intent.getStringExtra(UIKitConstants.IntentStrings.STATUS)
            if (status != null && (status == CometChatConstants.USER_STATUS_ONLINE)) userStatus!!.setTextColor(resources.getColor(R.color.colorPrimary))
            userStatus!!.text = status
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.LINK)) {
            link = intent.getStringExtra(UIKitConstants.IntentStrings.LINK)
        }
        if (avatar != null && avatar!!.isNotEmpty()) userAvatar!!.setAvatar(avatar!!) else {
            if (name != null && name!!.isNotEmpty()) userAvatar!!.setInitials(name!!) else userAvatar!!.setInitials("Unknown")
        }
        if (isAddMember) {
            addBtn!!.text = String.format(resources.getString(R.string.add_user_to_group), name, groupName)
            historyView!!.visibility = View.GONE
        } else {
            fetchCallHistory()
            addBtn!!.visibility = View.GONE
        }
    }

    private fun fetchCallHistory() {
        if (messageRequest == null) {
            messageRequest = MessagesRequestBuilder().setUID((uid)!!).setCategories(listOf(CometChatConstants.CATEGORY_CALL)).setLimit(30).build()
        }
        messageRequest!!.fetchPrevious(object : CallbackListener<List<BaseMessage>>() {
            public override fun onSuccess(messageList: List<BaseMessage>) {
                if (messageList.isNotEmpty()) {
                    callList.addAll(messageList)
                    setCallHistoryAdapter(messageList)
                }
//                if (callList.size != 0) historyView!!.visibility = View.VISIBLE else historyView!!.visibility = View.GONE
            }

            public override fun onError(e: CometChatException) {}
        })
    }

    private fun setCallHistoryAdapter(messageList: List<BaseMessage>) {
        if (callHistoryAdapter == null) {
            callHistoryAdapter = CallHistoryAdapter(this@CometChatUserDetailScreenActivity, messageList)
            val linearLayoutManager: LinearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
            historyRv!!.layoutManager = linearLayoutManager
            historyRv!!.adapter = callHistoryAdapter
        } else callHistoryAdapter!!.updateList(messageList)
    }

    private fun setBlockUnblock() {
        if (isBlocked) {
            tvBlockUser!!.setTextColor(resources.getColor(R.color.online_green))
            tvBlockUser!!.text = resources.getString(R.string.unblock_user)
        } else {
            tvBlockUser!!.text = resources.getString(R.string.block_user)
            tvBlockUser!!.setTextColor(resources.getColor(R.color.red))
        }
    }

    public override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addMember() {
        val userList: MutableList<GroupMember> = ArrayList()
        userList.add(GroupMember(uid, CometChatConstants.SCOPE_PARTICIPANT))
        CometChat.addMembersToGroup((guid)!!, userList, null, object : CallbackListener<HashMap<String?, String?>?>() {
            public override fun onSuccess(stringStringHashMap: HashMap<String?, String?>?) {
                Log.e(TAG, "onSuccess: " + uid + "Group" + guid)
                if (tvBlockUser != null) 
//                    ErrorMessagesUtils.showCometChatErrorDialog(this@CometChatUserDetailScreenActivity, String.format(resources.getString(R.string.user_added_to_group), userName!!.text.toString(), groupName), UIKitConstants.ErrorTypes.SUCCESS)
                addBtn!!.text = String.format(resources.getString(R.string.remove_from_group), groupName)
                isAlreadyAdded = true
            }

            public override fun onError(e: CometChatException) {
                ErrorMessagesUtils.cometChatErrorMessage(this@CometChatUserDetailScreenActivity, e.code)
            }
        })
    }

    private fun kickGroupMember() {
        CometChat.kickGroupMember((uid)!!, (guid)!!, object : CallbackListener<String?>() {
            public override fun onSuccess(s: String?) {
                if (tvBlockUser != null)
//                    ErrorMessagesUtils.showCometChatErrorDialog(this@CometChatUserDetailScreenActivity, String.format(resources.getString(R.string.user_removed_from_group), userName!!.text.toString(), groupName), UIKitConstants.ErrorTypes.SUCCESS)
                addBtn?.text = String.format(resources.getString(R.string.add_in), groupName)
                addBtn?.visibility = View.VISIBLE
                tvAction?.visibility = View.VISIBLE
                isAlreadyAdded = false
            }

            public override fun onError(e: CometChatException) {
                if (tvBlockUser != null)
                    ErrorMessagesUtils.cometChatErrorMessage(this@CometChatUserDetailScreenActivity, e.code)
            }
        })
    }

    private fun unblockUser() {
        val uids: ArrayList<String?> = ArrayList()
        uids.add(uid)
        CometChat.unblockUsers(uids, object : CallbackListener<HashMap<String?, String?>?>() {
            public override fun onSuccess(stringStringHashMap: HashMap<String?, String?>?) {
                if (tvBlockUser != null)
//                    ErrorMessagesUtils.showCometChatErrorDialog(this@CometChatUserDetailScreenActivity, String.format(resources.getString(R.string.unblocked_successfully), userName!!.text.toString()),UIKitConstants.ErrorTypes.SUCCESS)
                isBlocked = false
                setBlockUnblock()
            }

            public override fun onError(e: CometChatException) {
                Log.d(TAG, "onError: " + e.message)
                if (tvBlockUser != null) ErrorMessagesUtils.cometChatErrorMessage(this@CometChatUserDetailScreenActivity, e.code)
            }
        })
    }

    private fun blockUser() {
        val uids: ArrayList<String?> = ArrayList()
        uids.add(uid)
        CometChat.blockUsers(uids, object : CallbackListener<HashMap<String?, String?>?>() {
            public override fun onSuccess(stringStringHashMap: HashMap<String?, String?>?) {
                if (tvBlockUser != null)
//                    ErrorMessagesUtils.showCometChatErrorDialog(this@CometChatUserDetailScreenActivity, String.format(resources.getString(R.string.user_is_blocked), userName?.text.toString()),UIKitConstants.ErrorTypes.SUCCESS)
                isBlocked = true
                setBlockUnblock()
            }

            public override fun onError(e: CometChatException) {
                if (tvBlockUser != null)
                    ErrorMessagesUtils.cometChatErrorMessage(this@CometChatUserDetailScreenActivity, e.code)
                Log.d(TAG, "onError: " + e.message)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        groupListener()
    }

    override fun onStop() {
        super.onStop()
        CometChat.removeGroupListener(TAG)
    }

    private fun groupListener() {
        CometChat.addGroupListener(TAG, object : GroupListener() {
            public override fun onGroupMemberJoined(action: Action, joinedUser: User, joinedGroup: Group) {
                updateBtn(joinedUser, R.string.remove_from_group)
            }

            public override fun onGroupMemberLeft(action: Action, leftUser: User, leftGroup: Group) {
                updateBtn(leftUser, R.string.add_in)
            }

            public override fun onGroupMemberKicked(action: Action, kickedUser: User, kickedBy: User, kickedFrom: Group) {
                updateBtn(kickedUser, R.string.add_in)
            }

            public override fun onMemberAddedToGroup(action: Action, addedby: User, userAdded: User, addedTo: Group) {
                updateBtn(userAdded, R.string.remove_from_group)
            }
        })
    }

    private fun updateBtn(user: User, resource_string: Int) {
        if ((user.uid == uid)) addBtn!!.text = String.format(resources.getString(resource_string), groupName)
    }
}