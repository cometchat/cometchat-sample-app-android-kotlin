package com.cometchat.pro.uikit.ui_components.groups.group_detail

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.BannedGroupMembersRequest
import com.cometchat.pro.core.BannedGroupMembersRequest.BannedGroupMembersRequestBuilder
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.*
import com.cometchat.pro.core.GroupMembersRequest
import com.cometchat.pro.core.GroupMembersRequest.GroupMembersRequestBuilder
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.Action
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.GroupMember
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.cometchat_ui.CometChatUI
import com.cometchat.pro.uikit.ui_components.groups.add_members.CometChatAddMembersActivity
import com.cometchat.pro.uikit.ui_components.groups.admin_moderator_list.CometChatAdminModeratorListActivity
import com.cometchat.pro.uikit.ui_components.groups.banned_members.CometChatBanMembersActivity
import com.cometchat.pro.uikit.ui_components.groups.group_members.GroupMemberAdapter
import com.cometchat.pro.uikit.ui_components.shared.cometchatAvatar.CometChatAvatar
import com.cometchat.pro.uikit.ui_components.shared.cometchatSharedMedia.CometChatSharedMedia
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.ClickListener
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.RecyclerTouchListener
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.internal.Util
import java.util.*

class CometChatGroupDetailActivity() : AppCompatActivity() {
    private var kickingGroupMembersEnabled: Boolean = false
    private var banningGroupMembersEnabled: Boolean = false
    private val TAG = "CometChatGroupDetail"
    private var userPresenceEnabled: Boolean = false
    private lateinit var groupIcon: CometChatAvatar
    private var groupType: String? = null
    private var ownerId: String? = null
    private var tvGroupName: TextView? = null
    private var tvAdminCount: TextView? = null
    private var tvModeratorCount: TextView? = null
    private var tvBanMemberCount: TextView? = null
    private val groupMemberUids = ArrayList<String>()
    private var rvMemberList: RecyclerView? = null
    private var guid: String? = null
    private var gName: String? = null
    private var gDesc: String? = null
    private var gPassword: String? = null
    private var groupMembersRequest: GroupMembersRequest? = null
    private var groupMemberAdapter: GroupMemberAdapter? = null
    private var adminCount = 0
    private var moderatorCount = 0
    var s = arrayOfNulls<String>(0)
    private var rlAddMemberView: RelativeLayout? = null
    private var rlAdminListView: RelativeLayout? = null
    private var rlModeratorView: RelativeLayout? = null
    private var rlBanMembers: RelativeLayout? = null
    private var loggedInUserScope: String? = null
    private var groupMember: GroupMember? = null
    private var tvDelete: TextView? = null
    private var tvLoadMore: TextView? = null
    private var groupMemberList: List<GroupMember>? = null

    private var dialog: AlertDialog.Builder? = null
    private var tvMemberCount: TextView? = null
    private var groupMemberCount = 0
    private val loggedInUser = CometChat.getLoggedInUser()
    private var fontUtils: FontUtils? = null
    private var sharedMediaLayout: LinearLayout? = null
    private var sharedMediaView: CometChatSharedMedia? = null

    //    private var videoCallBtn: ImageView? = null
//    private var callBtn: ImageView? = null
    private var dividerAdmin: TextView? = null
    private var dividerBan: TextView? = null
    private var dividerModerator: TextView? = null
    private var divider2: TextView? = null
    private var banMemberRequest: BannedGroupMembersRequest? = null
    private var toolbar: MaterialToolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cometchat_group_detail)
        fontUtils = FontUtils.getInstance(this)
        initComponent()
    }

    private fun initComponent() {
        dividerAdmin = findViewById(R.id.tv_seperator_admin)
        dividerModerator = findViewById(R.id.tv_seperator_moderator)
        dividerBan = findViewById(R.id.tv_seperator_ban)
        divider2 = findViewById(R.id.tv_seperator_1)
        groupIcon = findViewById(R.id.iv_group)
        tvGroupName = findViewById(R.id.tv_group_name)
        tvGroupName!!.setOnClickListener(View.OnClickListener { updateGroupDialog() })
        tvMemberCount = findViewById(R.id.tv_members)
        tvAdminCount = findViewById(R.id.tv_admin_count)
        tvModeratorCount = findViewById(R.id.tv_moderator_count)
        tvBanMemberCount = findViewById(R.id.tv_ban_count)
        rvMemberList = findViewById(R.id.member_list)
        if (!FeatureRestriction.isViewingGroupMembersEnabled())
            rvMemberList?.visibility = View.GONE
        tvLoadMore = findViewById(R.id.tv_load_more)
        tvLoadMore!!.text = String.format(resources.getString(R.string.load_more_members), LIMIT)
        val tvAddMember = findViewById<TextView>(R.id.tv_add_member)
//        callBtn = findViewById(R.id.callBtn_iv)
//        videoCallBtn = findViewById(R.id.video_callBtn_iv)
        rlBanMembers = findViewById(R.id.rlBanView)
        rlBanMembers!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                openBanMemberListScreen()
            }
        })
        rlAddMemberView = findViewById(R.id.rl_add_member)
        rlAddMemberView!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                addMembers()
            }
        })
        rlAdminListView = findViewById(R.id.rlAdminView)
        rlAdminListView!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                openAdminListScreen(false)
            }
        })
        rlModeratorView = findViewById(R.id.rlModeratorView)
        rlModeratorView!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                openAdminListScreen(true)
            }
        })
        tvDelete = findViewById(R.id.tv_delete)
        val tvExit = findViewById<TextView>(R.id.tv_exit)
        toolbar = findViewById(R.id.groupDetailToolbar)
        tvDelete!!.typeface = fontUtils!!.getTypeFace(FontUtils.robotoMedium)
        tvExit.typeface = fontUtils!!.getTypeFace(FontUtils.robotoMedium)
        tvAddMember.typeface = fontUtils!!.getTypeFace(FontUtils.robotoRegular)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val linearLayoutManager = LinearLayoutManager(this)
        rvMemberList!!.layoutManager = linearLayoutManager
        //        rvMemberList.setNestedScrollingEnabled(false);
        handleIntent()
        checkDarkMode()
        sharedMediaLayout = findViewById<LinearLayout>(R.id.shared_media_layout)
        sharedMediaView = findViewById(R.id.shared_media_view)
        sharedMediaView!!.setRecieverId(guid)
        sharedMediaView!!.setRecieverType(CometChatConstants.RECEIVER_TYPE_GROUP)
        sharedMediaView!!.reload()
        rvMemberList!!.addOnItemTouchListener(RecyclerTouchListener(this, rvMemberList!!, object : ClickListener() {
            override fun onClick(var1: View, var2: Int) {
                val user = var1.getTag(R.string.user) as GroupMember
                if (loggedInUserScope != null && ((loggedInUserScope == CometChatConstants.SCOPE_ADMIN) || (loggedInUserScope == CometChatConstants.SCOPE_MODERATOR))) {
                    groupMember = user
                    val isAdmin = (user.scope == CometChatConstants.SCOPE_ADMIN)
                    val isSelf = (loggedInUser.uid == user.uid)
                    val isOwner = (loggedInUser.uid == ownerId)
                    if (!isSelf) {
                        if (!isAdmin || isOwner) {
                            if (banningGroupMembersEnabled || kickingGroupMembersEnabled) {
                                registerForContextMenu(rvMemberList)
                                openContextMenu(var1)
                            }
                        }
                    }
                }
            }

        }))
        tvLoadMore!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                groupMembers
            }
        })
        tvExit.setOnClickListener { view: View? ->
            createDialog(resources.getString(R.string.leave_group), resources.getString(R.string.leave_group_message),
                    resources.getString(R.string.leave_group), resources.getString(R.string.cancel), R.drawable.ic_exit_to_app)
        }
//        callBtn!!.setOnClickListener(View.OnClickListener { view: View? -> checkOnGoingCall(CometChatConstants.CALL_TYPE_AUDIO) })
//        videoCallBtn!!.setOnClickListener(View.OnClickListener { view: View? -> checkOnGoingCall(CometChatConstants.CALL_TYPE_VIDEO) })
        tvDelete!!.setOnClickListener(View.OnClickListener { view: View? ->
            createDialog(resources.getString(R.string.delete_group), resources.getString(R.string.delete_group_message),
                    resources.getString(R.string.delete_group), resources.getString(R.string.cancel), R.drawable.ic_delete_24dp)
        })


        //
        if (!FeatureRestriction.isJoinLeaveGroupsEnabled()) tvExit.visibility = View.GONE

        if (!FeatureRestriction.isGroupDeletionEnabled()) tvDelete!!.visibility = View.GONE

        if (!FeatureRestriction.isSharedMediaEnabled()) {
            sharedMediaLayout?.visibility = View.GONE
        }

        if (!FeatureRestriction.isViewGroupMember()) {
            rvMemberList!!.visibility = View.GONE
            tvLoadMore!!.visibility = View.GONE
            rlAddMemberView!!.visibility = View.GONE
            tvMemberCount?.visibility = View.GONE
            divider2?.visibility = View.GONE
        }
        if (!FeatureRestriction.isChangingGroupMemberScopeEnabled()) {
            rlModeratorView!!.visibility = View.GONE
            rlAdminListView!!.visibility = View.GONE
            dividerAdmin?.visibility = View.GONE
            dividerModerator?.visibility = View.GONE
        }

//        callBtn!!.visibility = View.GONE
//        videoCallBtn!!.visibility = View.GONE

        FeatureRestriction.isBanningGroupMembersEnabled(object : FeatureRestriction.OnSuccessListener {
            override fun onSuccess(p0: Boolean) {
                if (!p0) rlBanMembers!!.visibility = View.GONE
            }
        })
        FeatureRestriction.isUserPresenceEnabled(object : FeatureRestriction.OnSuccessListener {
            override fun onSuccess(p0: Boolean) {
                userPresenceEnabled = p0
            }

        })
        FeatureRestriction.isBanningGroupMembersEnabled(object : FeatureRestriction.OnSuccessListener {
            override fun onSuccess(p0: Boolean) {
                banningGroupMembersEnabled = p0
            }
        })

        FeatureRestriction.isKickingGroupMembersEnabled(object : FeatureRestriction.OnSuccessListener {
            override fun onSuccess(p0: Boolean) {
                kickingGroupMembersEnabled = p0
            }
        })

//        if (UIKitSettings.color != null) {
//            window.statusBarColor = Color.parseColor(UIKitSettings.color)
//            callBtn!!.imageTintList = ColorStateList.valueOf(
//                    Color.parseColor(UIKitSettings.color))
//            videoCallBtn!!.imageTintList = ColorStateList.valueOf(
//                    Color.parseColor(UIKitSettings.color))
//        }
    }

    private fun checkDarkMode() {
        if (Utils.isDarkMode(this)) {
            toolbar!!.setTitleTextColor(resources.getColor(R.color.textColorWhite))
            tvGroupName!!.setTextColor(resources.getColor(R.color.textColorWhite))
            dividerAdmin!!.setBackgroundColor(resources.getColor(R.color.grey))
            dividerModerator!!.setBackgroundColor(resources.getColor(R.color.grey))
            dividerBan!!.setBackgroundColor(resources.getColor(R.color.grey))
            divider2!!.setBackgroundColor(resources.getColor(R.color.grey))
        } else {
            toolbar!!.setTitleTextColor(resources.getColor(R.color.primaryTextColor))
            tvGroupName!!.setTextColor(resources.getColor(R.color.primaryTextColor))
            dividerAdmin!!.setBackgroundColor(resources.getColor(R.color.light_grey))
            dividerModerator!!.setBackgroundColor(resources.getColor(R.color.light_grey))
            dividerBan!!.setBackgroundColor(resources.getColor(R.color.light_grey))
            divider2!!.setBackgroundColor(resources.getColor(R.color.light_grey))
        }
    }

//    private fun checkOnGoingCall(callType: String) {
//        if ((CometChat.getActiveCall() != null) && (CometChat.getActiveCall().callStatus == CometChatConstants.CALL_STATUS_ONGOING) && (CometChat.getActiveCall().sessionId != null)) {
//            val alert = AlertDialog.Builder(this)
//            alert.setTitle(resources.getString(R.string.ongoing_call))
//                    .setMessage(resources.getString(R.string.ongoing_call_message))
//                    .setPositiveButton(resources.getString(R.string.join), object : DialogInterface.OnClickListener {
//                        override fun onClick(dialog: DialogInterface, which: Int) {
//                            Utils.joinOnGoingCall(this@CometChatGroupDetailActivity)
//                        }
//                    }).setNegativeButton(resources.getString(R.string.cancel), object : DialogInterface.OnClickListener {
//                        override fun onClick(dialog: DialogInterface, which: Int) {
//                            dialog.dismiss()
//                        }
//                    }).create().show()
//        } else {
//            initiateGroupCall(guid, CometChatConstants.RECEIVER_TYPE_GROUP, callType)
//        }
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.group_action_menu, menu)
        if (!banningGroupMembersEnabled) menu.findItem(R.id.item_ban).isVisible = false
        if (!kickingGroupMembersEnabled) menu.findItem(R.id.item_remove).isVisible = false
        menu.findItem(R.id.item_make_admin).isVisible = false
        menu.setHeaderTitle(getString(R.string.group_alert))
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.item_remove) {
            kickMember()
        } else if (item.itemId == R.id.item_ban) {
            banMember()
        }
        return super.onContextItemSelected(item)
    }

    fun initiateGroupCall(recieverID: String?, receiverType: String?, callType: String?) {
        val call = Call((recieverID)!!, receiverType, callType)
        CometChat.initiateCall(call, object : CallbackListener<Call>() {
            override fun onSuccess(call: Call) {
                Utils.startGroupCallIntent(this@CometChatGroupDetailActivity, (call.callReceiver as Group), call.type, true, call.sessionId)
            }

            override fun onError(e: CometChatException) {
                Log.e(TAG, "onError: " + e.message)
                if (rvMemberList != null) Snackbar.make(rvMemberList!!, resources.getString(R.string.call_initiate_error) + ":" + e.message, Snackbar.LENGTH_LONG).show()
            }
        })
    }

    /**
     * This method is used to create dialog box on click of events like **Delete Group** and **Exit Group**
     * @param title
     * @param message
     * @param positiveText
     * @param negativeText
     * @param drawableRes
     */
    private fun createDialog(title: String, message: String, positiveText: String, negativeText: String, drawableRes: Int) {
        val alert_dialog = MaterialAlertDialogBuilder(this@CometChatGroupDetailActivity,
                R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered)
        alert_dialog.setTitle(title)
        alert_dialog.setMessage(message)
        alert_dialog.setPositiveButton(positiveText) { dialogInterface: DialogInterface?, i: Int ->
            if (positiveText.equals(resources.getString(R.string.leave_group), ignoreCase = true)) leaveGroup() else if ((positiveText.equals(resources.getString(R.string.delete_group), ignoreCase = true)
                            && loggedInUserScope.equals(CometChatConstants.SCOPE_ADMIN, ignoreCase = true))) deleteGroup()
        }
        alert_dialog.setNegativeButton(negativeText, object : DialogInterface.OnClickListener {
            override fun onClick(dialogInterface: DialogInterface, i: Int) {
                dialogInterface.dismiss()
            }
        })
        alert_dialog.create()
        alert_dialog.show()
    }

    /**
     * This method is used to handle the intent passed to this activity.
     */
    private fun handleIntent() {
        if (intent.hasExtra(UIKitConstants.IntentStrings.GUID)) {
            guid = intent.getStringExtra(UIKitConstants.IntentStrings.GUID)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.MEMBER_SCOPE)) {
            loggedInUserScope = intent.getStringExtra(UIKitConstants.IntentStrings.MEMBER_SCOPE)
            if (loggedInUserScope != null && (loggedInUserScope == CometChatConstants.SCOPE_ADMIN)) {
                rlAddMemberView!!.visibility = View.VISIBLE
                rlBanMembers!!.visibility = View.VISIBLE
                tvDelete!!.visibility = View.VISIBLE
            } else if (loggedInUserScope != null && (loggedInUserScope == CometChatConstants.SCOPE_MODERATOR)) {
                rlBanMembers!!.visibility = View.VISIBLE
            }
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.NAME)) {
            gName = intent.getStringExtra(UIKitConstants.IntentStrings.NAME)
            tvGroupName!!.text = gName
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.AVATAR)) {
            val avatar = intent.getStringExtra(UIKitConstants.IntentStrings.AVATAR)
            if (avatar != null && avatar.isNotEmpty()) groupIcon.setAvatar(avatar) else groupIcon.setInitials((gName)!!)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.GROUP_DESC)) {
            gDesc = intent.getStringExtra(UIKitConstants.IntentStrings.GROUP_DESC)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.GROUP_PASSWORD)) {
            gPassword = intent.getStringExtra(UIKitConstants.IntentStrings.GROUP_PASSWORD)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.GROUP_OWNER)) {
            ownerId = intent.getStringExtra(UIKitConstants.IntentStrings.GROUP_OWNER)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.MEMBER_COUNT)) {
            tvMemberCount!!.visibility = View.VISIBLE
            groupMemberCount = intent.getIntExtra(UIKitConstants.IntentStrings.MEMBER_COUNT, 0)
            tvMemberCount!!.text = (groupMemberCount).toString() + " Members"
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.GROUP_TYPE)) {
            groupType = intent.getStringExtra(UIKitConstants.IntentStrings.GROUP_TYPE)
        }
    }

    /**
     * This method is used whenever user click **Banned Members**. It takes user to
     * `CometChatBanMemberScreenActivity.class`
     *
     * @see CometChatBanMembersActivity
     */
    private fun openBanMemberListScreen() {
        val intent = Intent(this, CometChatBanMembersActivity::class.java)
        intent.putExtra(UIKitConstants.IntentStrings.GUID, guid)
        intent.putExtra(UIKitConstants.IntentStrings.GROUP_NAME, gName)
        intent.putExtra(UIKitConstants.IntentStrings.MEMBER_SCOPE, loggedInUserScope)
        startActivity(intent)
    }

    /**
     * This method is used whenever user click **Administrator**. It takes user to
     * `CometChatAdminListScreenActivity.class`
     *
     * @see CometChatAdminModeratorListActivity
     */
    fun openAdminListScreen(showModerators: Boolean) {
        val intent = Intent(this, CometChatAdminModeratorListActivity::class.java)
        intent.putExtra(UIKitConstants.IntentStrings.GUID, guid)
        intent.putExtra(UIKitConstants.IntentStrings.SHOW_MODERATORLIST, showModerators)
        intent.putExtra(UIKitConstants.IntentStrings.GROUP_OWNER, ownerId)
        intent.putExtra(UIKitConstants.IntentStrings.MEMBER_SCOPE, loggedInUserScope)
        startActivity(intent)
    }

    /**
     * This method is used whenever user click **Add Member**. It takes user to
     * `CometChatAddMemberScreenActivity.class`
     *
     * @see CometChatAddMembersActivity
     */
    fun addMembers() {
        if (FeatureRestriction.isAddingGroupMembersEnabled()) {
            val intent = Intent(this, CometChatAddMembersActivity::class.java)
            intent.putExtra(UIKitConstants.IntentStrings.GUID, guid)
            intent.putExtra(UIKitConstants.IntentStrings.GROUP_MEMBER, groupMemberUids)
            intent.putExtra(UIKitConstants.IntentStrings.GROUP_NAME, gName)
            intent.putExtra(UIKitConstants.IntentStrings.MEMBER_SCOPE, loggedInUserScope)
            intent.putExtra(UIKitConstants.IntentStrings.IS_ADD_MEMBER, true)
            startActivity(intent)
        }
    }

    /**
     * This method is used to delete Group. It is used only if loggedIn user is admin.
     */
    private fun deleteGroup() {
        if (FeatureRestriction.isGroupDeletionEnabled()) {
            CometChat.deleteGroup((guid)!!, object : CallbackListener<String?>() {
                override fun onSuccess(s: String?) {
                    launchUnified()
                }

                override fun onError(e: CometChatException) {
                    ErrorMessagesUtils.cometChatErrorMessage(this@CometChatGroupDetailActivity, e.code)
                    Log.e(TAG, "onError: " + e.message)
                }
            })
        }
    }

    private fun launchUnified() {
        val intent = Intent(this@CometChatGroupDetailActivity, CometChatUI::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    /**
     * This method is used to kick group member from the group. It is used only if loggedIn user is admin.
     *
     * @see CometChat.kickGroupMember
     */
    private fun kickMember() {
        CometChat.kickGroupMember(groupMember!!.uid, (guid)!!, object : CallbackListener<String>() {
            override fun onSuccess(s: String) {
                Log.e(TAG, "onSuccess: kickmember $s")
                tvMemberCount!!.text = (groupMemberCount - 1).toString() + " Members"
                groupMemberUids.remove(groupMember!!.uid)
                groupMemberAdapter!!.removeGroupMember(groupMember)
//                ErrorMessagesUtils.showCometChatErrorDialog(this@CometChatGroupDetailActivity, resources.getString(R.string.group_member_kicked_successfully), UIKitConstants.ErrorTypes.SUCCESS)
            }

            override fun onError(e: CometChatException) {
                ErrorMessagesUtils.cometChatErrorMessage(this@CometChatGroupDetailActivity, e.code)
                Log.e(TAG, "onError: " + e.code + ": " + e.message)
            }
        })
    }

    /**
     * This method is used to ban group member from the group. It is used only if loggedIn user is admin.
     *
     * @see CometChat.banGroupMember
     */
    private fun banMember() {
        CometChat.banGroupMember(groupMember!!.uid, (guid)!!, object : CallbackListener<String>() {
            override fun onSuccess(s: String) {
                Log.e(TAG, "onSuccess: $s")
//                tvMemberCount!!.text = (groupMemberCount - 1).toString() + " Members"
//                var count = tvBanMemberCount!!.text.toString().toInt()
//                tvBanMemberCount!!.setText(++count)
                groupMemberUids.remove(groupMember!!.uid)
                groupMemberAdapter!!.removeGroupMember(groupMember)
//                ErrorMessagesUtils.showCometChatErrorDialog(this@CometChatGroupDetailActivity, resources.getString(R.string.banned_successfully), UIKitConstants.ErrorTypes.SUCCESS)
            }

            override fun onError(e: CometChatException) {
                ErrorMessagesUtils.cometChatErrorMessage(this@CometChatGroupDetailActivity, e.code)
                Log.e(TAG, "onError: " + e.message)
            }
        })
    }

    private val bannedMemberCount: Unit
    private get () {
        banMemberRequest = BannedGroupMembersRequestBuilder(guid).setLimit(100).build()
        banMemberRequest!!.fetchNext(object : CallbackListener<List<GroupMember?>>() {
            override fun onSuccess(groupMembers: List<GroupMember?>) {
                if (groupMembers.size >= 99) {
                    tvBanMemberCount!!.text = "99+"
                } else {
                    tvBanMemberCount!!.text = groupMembers.size.toString() + ""
                }
            }

            override fun onError(e: CometChatException) {
                Log.e(TAG, "onError: " + e.message + "\n" + e.code)
            }
        })
    }

    /**
     * This method is used to get list of group members. It also helps to update other things like
     * Admin count.
     *
     * @see GroupMembersRequest.fetchNext
     * @see GroupMember
     */
    private val groupMembers: List<GroupMember>?
    private get () {
        if (groupMembersRequest == null) {
            groupMembersRequest = GroupMembersRequestBuilder(guid).setLimit(LIMIT).build()
        }
        groupMembersRequest!!.fetchNext(object : CallbackListener<List<GroupMember>?>() {
            override fun onSuccess(groupMembers: List<GroupMember>?) {
                Log.e(TAG, "onSuccess: groupMem " + groupMembers!!.size)
                if (groupMembers != null && groupMembers.isNotEmpty()) {
                    adminCount = 0
                    moderatorCount = 0
                    groupMemberUids.clear()
                    s = arrayOfNulls(groupMembers.size)
                    for (j in groupMembers.indices) {
                        groupMemberUids.add(groupMembers[j].uid)
                        if ((groupMembers[j].scope == CometChatConstants.SCOPE_ADMIN)) {
                            adminCount++
                        }
                        if ((groupMembers[j].scope == CometChatConstants.SCOPE_MODERATOR)) {
                            moderatorCount++
                        }
                        s[j] = groupMembers[j].name
                    }
//                        tvAdminCount!!.text = adminCount.toString() + ""
//                        tvModeratorCount!!.text = moderatorCount.toString() + ""
                    if (groupMemberAdapter == null) {
                        groupMemberAdapter = GroupMemberAdapter(this@CometChatGroupDetailActivity, groupMembers, ownerId)
                        rvMemberList!!.adapter = groupMemberAdapter
                    } else {
                        groupMemberAdapter!!.addAll(groupMembers)
                    }
                    if (groupMembers.size < LIMIT) {
                        tvLoadMore!!.visibility = View.GONE
                    }
                } else {
                    tvLoadMore!!.visibility = View.GONE
                }
                groupMemberList = groupMembers
            }

            override fun onError(e: CometChatException) {
                ErrorMessagesUtils.cometChatErrorMessage(this@CometChatGroupDetailActivity, e.code)
                Log.e(TAG, "onError: " + e.message)
                groupMemberList = emptyList();
            }
        })
        return groupMemberList
    }

    /**
     * This method is used to leave the loggedIn User from respective group.
     *
     * @see CometChat.leaveGroup
     */
    private fun leaveGroup() {
        if (FeatureRestriction.isJoinLeaveGroupsEnabled()) {
            CometChat.leaveGroup((guid)!!, object : CallbackListener<String?>() {
                override fun onSuccess(s: String?) {
                    launchUnified()
                }

                override fun onError(e: CometChatException) {
                    ErrorMessagesUtils.cometChatErrorMessage(this@CometChatGroupDetailActivity, e.code)
                    Log.e(TAG, "onError: " + e.message)
                }
            })
        }
    }

    /**
     * This method is used to add group listener in this screen to receive real-time events.
     *
     * @see CometChat.addGroupListener
     */
    fun addGroupListener() {
        CometChat.addGroupListener(TAG, object : GroupListener() {
            override fun onGroupMemberJoined(action: Action, joinedUser: User, joinedGroup: Group) {
                Log.e(TAG, "onGroupMemberJoined: " + joinedUser.uid)
                if ((joinedGroup.guid == guid)) updateGroupMember(joinedUser, false, false, action)
            }

            override fun onGroupMemberLeft(action: Action, leftUser: User, leftGroup: Group) {
                Log.d(TAG, "onGroupMemberLeft: ")
                if ((leftGroup.guid == guid)) updateGroupMember(leftUser, true, false, action)
            }

            override fun onGroupMemberKicked(action: Action, kickedUser: User, kickedBy: User, kickedFrom: Group) {
                Log.d(TAG, "onGroupMemberKicked: ")
                if ((kickedFrom.guid == guid)) updateGroupMember(kickedUser, true, false, action)
            }

            override fun onGroupMemberScopeChanged(action: Action, updatedBy: User, updatedUser: User, scopeChangedTo: String, scopeChangedFrom: String, group: Group) {
                Log.d(TAG, "onGroupMemberScopeChanged: ")
                if ((group.guid == guid)) updateGroupMember(updatedUser, false, true, action)
            }

            override fun onMemberAddedToGroup(action: Action, addedby: User, userAdded: User, addedTo: Group) {
                if ((addedTo.guid == guid)) updateGroupMember(userAdded, false, false, action)
            }

            override fun onGroupMemberBanned(action: Action, bannedUser: User, bannedBy: User, bannedFrom: Group) {
                if ((bannedFrom.guid == guid)) {
//                    var count = tvBanMemberCount!!.text.toString().toInt()
//                    tvBanMemberCount!!.setText(++count)
                    updateGroupMember(bannedUser, true, false, action)
                }
            }

            override fun onGroupMemberUnbanned(action: Action, unbannedUser: User, unbannedBy: User, unbannedFrom: Group) {
                if ((unbannedFrom.guid == guid)) {
                    var count = tvBanMemberCount!!.text.toString().toInt()
                    tvBanMemberCount!!.setText(--count)
                }
            }
        })
    }

    /**
     * This method is used to update group members from events recieved in real time. It updates or removes
     * group member from list based on parameters passed.
     *
     * @param user is a object of User.
     * @param isRemoved is a boolean which helps to know whether group member needs to be removed from list or not.
     * @param isScopeUpdate is a boolean which helps to know whether group member scope is updated or not.
     * @param action is object of Action.
     *
     * @see Action
     *
     * @see GroupMember
     *
     * @see User
     *
     * @see utils.Utils.UserToGroupMember
     */
    private fun updateGroupMember(user: User, isRemoved: Boolean, isScopeUpdate: Boolean, action: Action) {
        if (groupMemberAdapter != null) {
            if (!isRemoved && !isScopeUpdate) {
                groupMemberAdapter!!.addGroupMember(Utils.UserToGroupMember(user, false, action.oldScope)!!)
                val count = ++groupMemberCount
                tvMemberCount!!.text = "$count Members"
            } else if (isRemoved && !isScopeUpdate) {
                groupMemberAdapter!!.removeGroupMember(Utils.UserToGroupMember(user, false, action.oldScope))
                val count = --groupMemberCount
                tvMemberCount!!.text = "$count Members"
                if (action.newScope != null) {
                    if ((action.newScope == CometChatConstants.SCOPE_ADMIN)) {
                        adminCount = adminCount - 1
                        tvAdminCount!!.text = adminCount.toString()
                    } else if ((action.newScope == CometChatConstants.SCOPE_MODERATOR)) {
                        moderatorCount = moderatorCount - 1
                        tvModeratorCount!!.text = moderatorCount.toString()
                    }
                }
            } else if (!isRemoved) {
                groupMemberAdapter!!.updateMember(Utils.UserToGroupMember(user, true, action.newScope)!!)
                if ((action.newScope == CometChatConstants.SCOPE_ADMIN)) {
                    adminCount = adminCount + 1
                    tvAdminCount!!.text = adminCount.toString()
                    if ((user.uid == loggedInUser.uid)) {
                        rlAddMemberView!!.visibility = View.VISIBLE
                        loggedInUserScope = CometChatConstants.SCOPE_ADMIN
                        tvDelete!!.visibility = View.VISIBLE
                    }
                } else if ((action.newScope == CometChatConstants.SCOPE_MODERATOR)) {
                    moderatorCount = moderatorCount + 1
                    tvModeratorCount!!.text = moderatorCount.toString()
                    if ((user.uid == loggedInUser.uid)) {
                        rlBanMembers!!.visibility = View.VISIBLE
                        loggedInUserScope = CometChatConstants.SCOPE_MODERATOR
                    }
                } else if ((action.oldScope == CometChatConstants.SCOPE_ADMIN)) {
                    adminCount = adminCount - 1
                    tvAdminCount!!.text = adminCount.toString()
                } else if ((action.oldScope == CometChatConstants.SCOPE_MODERATOR)) {
                    moderatorCount = moderatorCount - 1
                    tvModeratorCount!!.text = moderatorCount.toString()
                }
            }
        }
    }

    private fun updateGroupDialog() {
        dialog = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.cometchat_update_group_dialog, null)
        val avatar: CometChatAvatar = view.findViewById(R.id.group_icon)
        val avatar_url: TextInputEditText = view.findViewById(R.id.icon_url_edt)
        if (groupIcon.avatarUrl != null) {
            avatar.visibility = View.VISIBLE
            avatar.setAvatar(groupIcon.avatarUrl!!)
            avatar_url.setText(groupIcon.avatarUrl)
        } else {
            avatar.visibility = View.GONE
        }
        val groupName: TextInputEditText = view.findViewById(R.id.groupname_edt)
        val groupDesc: TextInputEditText = view.findViewById(R.id.groupdesc_edt)
        val groupOldPwd: TextInputEditText = view.findViewById(R.id.group_old_pwd)
        val groupNewPwd: TextInputEditText = view.findViewById(R.id.group_new_pwd)
        val groupOldPwdLayout: TextInputLayout = view.findViewById(R.id.input_group_old_pwd)
        val groupNewPwdLayout: TextInputLayout = view.findViewById(R.id.input_group_new_pwd)
        val groupTypeSp = view.findViewById<Spinner>(R.id.groupTypes)
        val updateGroupBtn: MaterialButton = view.findViewById(R.id.updateGroupBtn)
        val cancelBtn: MaterialButton = view.findViewById(R.id.cancelBtn)
        groupName.setText(gName)
        groupDesc.setText(gDesc)
        if (groupType != null && (groupType == CometChatConstants.GROUP_TYPE_PUBLIC)) {
            groupTypeSp.setSelection(0)
            groupOldPwdLayout.visibility = View.GONE
            groupNewPwdLayout.visibility = View.GONE
        } else if (groupType != null && (groupType == CometChatConstants.GROUP_TYPE_PRIVATE)) {
            groupTypeSp.setSelection(1)
            groupOldPwdLayout.visibility = View.GONE
            groupNewPwdLayout.visibility = View.GONE
        } else {
            groupTypeSp.setSelection(2)
            groupOldPwdLayout.visibility = View.VISIBLE
            groupNewPwdLayout.visibility = View.VISIBLE
        }
        groupTypeSp.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (parent.selectedItemPosition == 2) {
                    if (gPassword == null) {
                        groupOldPwdLayout.visibility = View.GONE
                    } else groupOldPwdLayout.visibility = View.VISIBLE
                    groupNewPwdLayout.visibility = View.VISIBLE
                } else {
                    groupOldPwdLayout.visibility = View.GONE
                    groupNewPwdLayout.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        avatar_url.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (!s.toString().isEmpty()) {
                    avatar.visibility = View.VISIBLE
                    Glide.with(this@CometChatGroupDetailActivity).load(s.toString()).into(avatar)
                } else avatar.visibility = View.GONE
            }
        })
        val alertDialog = dialog!!.create()
        alertDialog.setView(view)
        updateGroupBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val group = Group()
                if (groupName.text.toString().isEmpty()) {
                    groupName.error = getString(R.string.fill_this_field)
                } else if (groupTypeSp.selectedItemPosition == 2) {
                    if (gPassword != null && groupOldPwd.text.toString().trim { it <= ' ' }.isEmpty()) {
                        groupOldPwd.error = resources.getString(R.string.fill_this_field)
                    } else if (gPassword != null && groupOldPwd.text.toString().trim { it <= ' ' } != gPassword!!.trim { it <= ' ' }) {
                        groupOldPwd.error = resources.getString(R.string.password_not_matched)
                    } else if (groupNewPwd.text.toString().trim { it <= ' ' }.isEmpty()) {
                        groupNewPwd.error = resources.getString(R.string.fill_this_field)
                    } else {
                        group.name = groupName.text.toString()
                        group.guid = guid
                        group.groupType = CometChatConstants.GROUP_TYPE_PASSWORD
                        group.password = groupNewPwd.text.toString()
                        group.icon = avatar_url.text.toString()
                        updateGroup(group, alertDialog)
                    }
                } else if (groupTypeSp.selectedItemPosition == 1) {
                    group.name = groupName.text.toString()
                    group.guid = guid
                    group.groupType = CometChatConstants.GROUP_TYPE_PRIVATE
                    group.icon = avatar_url.text.toString()
                } else {
                    group.name = groupName.text.toString()
                    group.groupType = CometChatConstants.GROUP_TYPE_PUBLIC
                    group.icon = avatar_url.text.toString()
                }
                group.guid = guid
                updateGroup(group, alertDialog)
            }
        })
        cancelBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                alertDialog.dismiss()
            }
        })
        alertDialog.show()
    }

    private fun updateGroup(group: Group, dialog: AlertDialog) {
        CometChat.updateGroup(group, object : CallbackListener<Group?>() {
            override fun onSuccess(group: Group?) {
                if (rvMemberList != null) {
//                    ErrorMessagesUtils.showCometChatErrorDialog(this@CometChatGroupDetailActivity, resources.getString(R.string.group_updated), UIKitConstants.ErrorTypes.SUCCESS)
//                    Snackbar.make(rvMemberList!!, resources.getString(R.string.group_updated), Snackbar.LENGTH_LONG).show()
                    group
                }
                dialog.dismiss()
            }

            override fun onError(e: CometChatException) {
                if (rvMemberList != null) {
                    ErrorMessagesUtils.cometChatErrorMessage(this@CometChatGroupDetailActivity, e.code)
                }
                dialog.dismiss()
            }
        })
    }

    /**
     * This method is used to remove group listener.
     */
    fun removeGroupListener() {
        CometChat.removeGroupListener(TAG)
    }

    /**
     * This method is used to get Group Details.
     *
     * @see CometChat.getGroup
     */
    private val group: Unit
    private get () {
        CometChat.getGroup((guid)!!, object : CallbackListener<Group>() {
            override fun onSuccess(group: Group) {
                gName = group.name
                tvGroupName!!.text = gName
                groupIcon.setAvatar(group.icon)
                loggedInUserScope = group.scope
                groupMemberCount = group.membersCount
                groupType = group.groupType
                gDesc = group.description
                tvMemberCount!!.text = "$groupMemberCount Members"
            }

            override fun onError(e: CometChatException) {
//                    Toast.makeText(this@CometChatGroupDetailActivity, "Error:" + e.message, Toast.LENGTH_LONG).show()
                ErrorMessagesUtils.cometChatErrorMessage(this@CometChatGroupDetailActivity, e.code)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        group
        groupMembersRequest = null
        if (groupMemberAdapter != null) {
            groupMemberAdapter!!.resetAdapter()
            groupMemberAdapter = null
        }
        bannedMemberCount
        if (userPresenceEnabled)
            getUserStatus()
        if (FeatureRestriction.isViewGroupMember())
            groupMembers
        addGroupListener()
    }

    private fun getUserStatus() {
        addUserListener(TAG, object : UserListener() {
            override fun onUserOnline(user: User) {
                groupMemberAdapter?.updateMemberByStatus(user)
            }

            override fun onUserOffline(user: User) {
                groupMemberAdapter?.updateMemberByStatus(user)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
        removeGroupListener()
    }

    override fun onStop() {
        super.onStop()
        removeGroupListener()
    }

    companion object {
        private val LIMIT = 30
    }
}