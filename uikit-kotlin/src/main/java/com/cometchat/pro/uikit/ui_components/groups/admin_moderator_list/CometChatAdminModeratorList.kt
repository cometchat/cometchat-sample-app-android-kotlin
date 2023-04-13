package com.cometchat.pro.uikit.ui_components.groups.admin_moderator_list

import com.cometchat.pro.uikit.ui_components.groups.group_members.GroupMemberAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.core.CometChat.GroupListener
import com.cometchat.pro.core.GroupMembersRequest
import com.cometchat.pro.core.GroupMembersRequest.GroupMembersRequestBuilder
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.Action
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.GroupMember
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.groups.group_members.CometChatGroupMemberListActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.ClickListener
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.RecyclerTouchListener
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import java.util.*

/**
 * Purpose - CometChatAdminListScreen.class is a screen used to display List of admin's of a particular
 * group. It also helps to perform action like remove as admin, add as admin on group members.
 *
 *
 * Created on - 20th December 2019
 *
 *
 * Modified on  - 16th January 2020
 */
class CometChatAdminModeratorList : Fragment() {
    private var adminList: RecyclerView? = null
    private var ownerId: String? = null
    private var showModerators = false
    private var guid //It is guid of group whose members are been fetched.
            : String? = null
    private var groupMembersRequest //Used to fetch group members list.
            : GroupMembersRequest? = null
    private val members = ArrayList<GroupMember>()
    private var adapter: GroupMemberAdapter? = null
    private val TAG = "CometChatAdminListScreen"
    private var loggedInUserScope: String? = null
    private val loggedInUser = CometChat.getLoggedInUser()
    private var fontUtils: FontUtils? = null
    private var addAs: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fontUtils = FontUtils.getInstance(activity)
        handleArguments()
    }

    private fun handleArguments() {
        if (arguments != null) {
            guid = arguments!!.getString(UIKitConstants.IntentStrings.GUID)
            loggedInUserScope = arguments!!.getString(UIKitConstants.IntentStrings.MEMBER_SCOPE)
            ownerId = arguments!!.getString(UIKitConstants.IntentStrings.GROUP_OWNER)
            showModerators = arguments!!.getBoolean(UIKitConstants.IntentStrings.SHOW_MODERATORLIST)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cometchat_admin_moderator_list, container, false)
        adminList = view.findViewById(R.id.adminList)
        setHasOptionsMenu(true)
        val rlAddMember = view.findViewById<RelativeLayout>(R.id.rl_add_Admin)
        addAs = view.findViewById(R.id.add_as_tv)
        val toolbar: MaterialToolbar = view.findViewById(R.id.admin_toolbar)
        setToolbar(toolbar)
        if (showModerators) {
            toolbar.title = resources.getString(R.string.moderators)
            addAs!!.text = resources.getString(R.string.assign_as_moderator)
//            addAs!!.text = resources.getString(R.string.add_as_moderator)
        } else {
            toolbar.title = resources.getString(R.string.administrators)
            addAs!!.text = resources.getString(R.string.assign_as_admin)
        }
        adapter = GroupMemberAdapter(context!!, members, null)
        adminList!!.adapter = adapter
        if (loggedInUserScope != null && loggedInUserScope == CometChatConstants.SCOPE_ADMIN) {
            rlAddMember.visibility = View.VISIBLE
        }
        if (showModerators) {
            getModeratorList(guid)
        } else {
            getAdminList(guid)
        }
        rlAddMember.setOnClickListener { view1: View? ->
            val intent = Intent(context, CometChatGroupMemberListActivity::class.java)
            intent.putExtra(UIKitConstants.IntentStrings.GUID, guid)
            intent.putExtra(UIKitConstants.IntentStrings.SHOW_MODERATORLIST, showModerators)
            startActivity(intent)
        }
        adminList!!.addOnItemTouchListener(RecyclerTouchListener(context, adminList!!, object : ClickListener() {
            override fun onClick(var1: View, var2: Int) {
                val groupMember = var1.getTag(R.string.user) as GroupMember
                if (showModerators) {
                    if (loggedInUserScope == CometChatConstants.SCOPE_ADMIN && groupMember.uid != loggedInUser.uid) {
                        if (activity != null) {
                            val alertDialog = MaterialAlertDialogBuilder(activity)
                            alertDialog.setTitle(resources.getString(R.string.remove))
                            alertDialog.setMessage(String.format(resources.getString(R.string.remove_as_moderator), groupMember.name))
                            alertDialog.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, i -> updateMemberScope(groupMember, var1) }
                            alertDialog.setNegativeButton(resources.getString(R.string.cancel)) { dialogInterface, i -> dialogInterface.dismiss() }
                            alertDialog.create()
                            alertDialog.show()
                        }
                    } else {
                        val message: String
                        message = if (groupMember.uid == loggedInUser.uid) resources.getString(R.string.you_cannot_perform_action) else resources.getString(R.string.only_admin_removes_moderator)
                        Log.e(TAG, "onClick:admin "+message )
                        ErrorMessagesUtils.showCometChatErrorDialog(context, resources.getString(R.string.something_went_wrong_please_try_again))
                    }
                } else {
                    if (ownerId != null && loggedInUser.uid == ownerId && loggedInUserScope == CometChatConstants.SCOPE_ADMIN && groupMember.uid != loggedInUser.uid) {
                        if (activity != null) {
                            val alertDialog = MaterialAlertDialogBuilder(activity)
                            alertDialog.setTitle(resources.getString(R.string.remove))
                            alertDialog.setMessage(String.format(resources.getString(R.string.remove_as_admin), groupMember.name))
                            alertDialog.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, i -> updateMemberScope(groupMember, var1) }
                            alertDialog.setNegativeButton(resources.getString(R.string.cancel)) { dialogInterface, i -> dialogInterface.dismiss() }
                            alertDialog.create()
                            alertDialog.show()
                        }
                    } else {
                        val message: String
                        message = if (groupMember.uid == loggedInUser.uid) resources.getString(R.string.you_cannot_perform_action) else resources.getString(R.string.only_group_owner_removes_admin)
                        Log.e(TAG, "onClick:admin "+message )
                        ErrorMessagesUtils.showCometChatErrorDialog(context, resources.getString(R.string.something_went_wrong_please_try_again))
                    }
                }
            }
        }))
        return view
    }

    private fun setToolbar(toolbar: MaterialToolbar) {
        if (Utils.changeToolbarFont(toolbar) != null) {
            Utils.changeToolbarFont(toolbar)!!.typeface = fontUtils!!.getTypeFace(FontUtils.robotoMedium)
        }
        if (activity != null) {
            (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
            (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (activity != null) activity!!.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateMemberScope(groupMember: GroupMember, view: View) {
        CometChat.updateGroupMemberScope(groupMember.uid, guid!!, CometChatConstants.SCOPE_PARTICIPANT,
                object : CallbackListener<String?>() {
                    override fun onSuccess(s: String?) {
                        if (adapter != null) adapter?.removeGroupMember(groupMember)
//                        if (showModerators) {
//                            ErrorMessagesUtils.showCometChatErrorDialog(context, String.format(resources.getString(R.string.remove_from_moderator_privilege), groupMember.name),UIKitConstants.ErrorTypes.SUCCESS)
//                        } else {
//                            ErrorMessagesUtils.showCometChatErrorDialog(context, String.format(resources.getString(R.string.removed_from_admin), groupMember.name),UIKitConstants.ErrorTypes.SUCCESS)
//                        }
                    }

                    override fun onError(e: CometChatException) {
                        if (activity != null) {
                            ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                            Log.e(TAG, "onError: " + e.message)
                        }
                    }
                })
    }

    /**
     * This method is used to fetch Admin List.
     *
     * @param groupId is a unique id of group. It is used to fetch admin list of particular group.
     */
    private fun getAdminList(groupId: String?) {
        if (groupMembersRequest == null) {
            groupMembersRequest = GroupMembersRequestBuilder(groupId)
                    .setScopes(listOf(CometChatConstants.SCOPE_ADMIN)).setLimit(100).build()
        }
        groupMembersRequest!!.fetchNext(object : CallbackListener<List<GroupMember>>() {
            override fun onSuccess(groupMembers: List<GroupMember>) {
                adapter!!.addAll(groupMembers)
            }

            override fun onError(e: CometChatException) {
                ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                Log.e(TAG, "onError: " + e.message)
            }
        })
    }

    /**
     * This method is used to fetch Moderator List.
     *
     * @param groupId is a unique id of group. It is used to fetch moderator list of particular group.
     */
    private fun getModeratorList(groupId: String?) {
        if (groupMembersRequest == null) {
            groupMembersRequest = GroupMembersRequestBuilder(groupId)
                    .setScopes(listOf(CometChatConstants.SCOPE_MODERATOR)).setLimit(100).build()
        }
        groupMembersRequest!!.fetchNext(object : CallbackListener<List<GroupMember>>() {
            override fun onSuccess(groupMembers: List<GroupMember>) {
                adapter!!.addAll(groupMembers)
            }

            override fun onError(e: CometChatException) {
                ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                Log.e(TAG, "onError: " + e.message)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        groupMembersRequest = null
        if (guid != null) {
            if (showModerators) {
                getModeratorList(guid)
            } else {
                getAdminList(guid)
            }
        }
        addGroupListener()
    }

    override fun onPause() {
        super.onPause()
        CometChat.removeGroupListener(TAG)
    }

    private fun addGroupListener() {
        CometChat.addGroupListener(TAG, object : GroupListener() {
            override fun onGroupMemberLeft(action: Action, leftUser: User, leftGroup: Group) {
                updateGroupMember(leftUser, true, null)
            }

            override fun onGroupMemberKicked(action: Action, kickedUser: User, kickedBy: User, kickedFrom: Group) {
                updateGroupMember(kickedUser, true, null)
            }

            override fun onGroupMemberScopeChanged(action: Action, updatedBy: User, updatedUser: User, scopeChangedTo: String, scopeChangedFrom: String, group: Group) {
                if (action.newScope == CometChatConstants.SCOPE_ADMIN) updateGroupMember(updatedUser, false, action) else if (action.oldScope == CometChatConstants.SCOPE_ADMIN) updateGroupMember(updatedUser, true, null)
            }
        })
    }

    private fun updateGroupMember(user: User, isRemove: Boolean, action: Action?) {
        if (adapter != null) {
            if (isRemove) adapter!!.removeGroupMember(Utils.UserToGroupMember(user, false, CometChatConstants.SCOPE_PARTICIPANT)) else adapter!!.addGroupMember(Utils.UserToGroupMember(user, true, action!!.newScope)!!)
        }
    }
}