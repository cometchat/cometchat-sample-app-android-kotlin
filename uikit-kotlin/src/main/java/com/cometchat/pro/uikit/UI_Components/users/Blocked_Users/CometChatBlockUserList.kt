package com.cometchat.pro.uikit.ui_components.users.blocked_users

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.core.BlockedUsersRequest
import com.cometchat.pro.core.BlockedUsersRequest.BlockedUsersRequestBuilder
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.ClickListener
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.RecyclerTouchListener
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import java.util.*

/**
 * Purpose - CometChatBlockUserListScreen.class is a screen used to display List of blocked users.
 * It also helps to perform action like unblock user.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 16th January 2020
 *
 */
class CometChatBlockUserList : Fragment() {
    private val LIMIT = 100
    private var blockedUserAdapter: BlockedListAdapter? = null
    private var blockedUserRequest: BlockedUsersRequest? = null
    private var rvUserList: RecyclerView? = null
    private var fontUtils: FontUtils? = null
    private var noBlockUserLayout: TextView? = null
    private val userList: MutableList<User> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fontUtils = FontUtils.getInstance(activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cometchat_block_user, container, false)
        setHasOptionsMenu(true)
        rvUserList = view.findViewById(R.id.rv_blocked_user_list)
        noBlockUserLayout = view.findViewById(R.id.no_block_user)
        val toolbar: MaterialToolbar = view.findViewById(R.id.toolbar_blocked_user)
        setToolbar(toolbar)
        rvUserList!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    fetchBlockedUser()
                }
            }
        })

        // It unblock users when click on item in rvUserList
        rvUserList!!.addOnItemTouchListener(RecyclerTouchListener(context, rvUserList!!, object : ClickListener() {
            override fun onClick(var1: View, var2: Int) {
                val user = var1.getTag(R.string.user) as User
                if (activity != null) {
                    val alert = MaterialAlertDialogBuilder(activity)
                    alert.setTitle(resources.getString(R.string.unblock))
                    alert.setMessage(String.format(resources.getString(R.string.unblock_user_question), user.name))
                    alert.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, i -> unBlockUser(user, var1) }
                    alert.setNegativeButton(resources.getString(R.string.cancel)) { dialogInterface, i -> dialogInterface.dismiss() }
                    alert.create()
                    alert.show()
                }
            }
        }))
        fetchBlockedUser()
        return view
    }

    private fun setToolbar(toolbar: MaterialToolbar) {
        if (activity != null) {
            (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
            if ((activity as AppCompatActivity?)!!.supportActionBar != null) (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        if (Utils.changeToolbarFont(toolbar) != null) {
            Utils.changeToolbarFont(toolbar)!!.typeface = fontUtils!!.getTypeFace(FontUtils.robotoMedium)
        }
    }

    private fun unBlockUser(user: User, var1: View) {
        val uids = ArrayList<String>()
        uids.add(user.uid)
        CometChat.unblockUsers(uids, object : CallbackListener<HashMap<String?, String?>?>() {
            override fun onSuccess(stringStringHashMap: HashMap<String?, String?>?) {
                if (userList.contains(user)) userList.remove(user)
                blockedUserAdapter!!.removeUser(user)
                Snackbar.make(var1, String.format(resources.getString(R.string.user_unblocked), user.name), Snackbar.LENGTH_SHORT).show()
                checkIfNoUserVisible()
            }

            override fun onError(e: CometChatException) {
                Snackbar.make(var1, resources.getString(R.string.unblock_user_error), Snackbar.LENGTH_SHORT).show()
                Log.e(TAG, "onError: " + e.message)
            }
        })
    }

    /**
     * This method is used to fetch list of blocked users.
     *
     * @see BlockedUsersRequest
     */
    private fun fetchBlockedUser() {
        if (blockedUserRequest == null) {
            blockedUserRequest = BlockedUsersRequestBuilder().setDirection(BlockedUsersRequest.DIRECTION_BLOCKED_BY_ME).setLimit(LIMIT).build()
        }
        blockedUserRequest!!.fetchNext(object : CallbackListener<List<User>>() {
            override fun onSuccess(users: List<User>) {
                userList.addAll(users)
                if (users.size > 0) {
                    setAdapter(users)
                }
                checkIfNoUserVisible()
            }

            override fun onError(e: CometChatException) {
                Log.e(TAG, "onError: " + e.message)
                Snackbar.make(rvUserList!!, resources.getString(R.string.block_user_list_error), Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkIfNoUserVisible() {
        if (userList.size == 0) {
            noBlockUserLayout!!.visibility = View.VISIBLE
            rvUserList!!.visibility = View.GONE
        } else {
            noBlockUserLayout!!.visibility = View.GONE
            rvUserList!!.visibility = View.VISIBLE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (activity != null) activity!!.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * This method is used to set Adapter for rvUserlist to display blocked users.
     *
     * @param users
     */
    private fun setAdapter(users: List<User>) {
        if (blockedUserAdapter == null) {
            blockedUserAdapter = BlockedListAdapter(context!!, users)
            rvUserList!!.adapter = blockedUserAdapter
        } else {
            blockedUserAdapter!!.updateList(Utils.userSort(users))
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
    }

    override fun onPause() {
        super.onPause()
    }

    companion object {
        private const val TAG = "CometChatGroupMember"
    }
}