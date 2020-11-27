package com.cometchat.pro.androiduikit.ComponentFragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.cometchat.pro.androiduikit.R
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.MessagesRequest
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.CometChatCallList
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.snackbar.Snackbar

import java.util.Collections

import constant.StringContract
import listeners.OnItemClickListener
import screen.CometChatGroupDetailScreenActivity
import screen.CometChatUserDetailScreenActivity
import screen.messagelist.CometChatMessageListActivity
import utils.CallUtils
import utils.Utils

class CallListViewFragment : Fragment() {

    private var rvCallList: CometChatCallList? = null

    private var noCallView: LinearLayout? = null

    private var shimmerFrameLayout: ShimmerFrameLayout? = null

    private var messagesRequest: MessagesRequest? = null

    private var linearLayoutManager: LinearLayoutManager? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_call_list, container, false)
        rvCallList = view.findViewById(com.cometchat.pro.uikit.R.id.callList_rv)
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false
        )
        rvCallList!!.layoutManager = linearLayoutManager
        shimmerFrameLayout = view.findViewById(R.id.contact_shimmer)
        noCallView = view.findViewById(com.cometchat.pro.uikit.R.id.no_call_vw)
        rvCallList!!.setItemClickListener(object : OnItemClickListener<Call>() {
            override fun OnItemClick(`var`: Call, position: Int) {
                if (`var`.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                    val user: User
                    if (`var`.sender.uid == CometChat.getLoggedInUser().uid) {
                        user = `var`.callReceiver as User
                    } else {
                        user = `var`.sender
                    }
                    val intent = Intent(context, CometChatUserDetailScreenActivity::class.java)
                    intent.putExtra(StringContract.IntentStrings.UID, user.uid)
                    intent.putExtra(StringContract.IntentStrings.NAME, user.name)
                    intent.putExtra(StringContract.IntentStrings.AVATAR, user.avatar)
                    intent.putExtra(StringContract.IntentStrings.STATUS, user.status)
                    intent.putExtra(StringContract.IntentStrings.IS_BLOCKED_BY_ME, user.isBlockedByMe)
                    intent.putExtra(StringContract.IntentStrings.FROM_CALL_LIST, true)
                    startActivity(intent)
                } else {
                    val group: Group
                    group = `var`.callReceiver as Group
                    val intent = Intent(context, CometChatGroupDetailScreenActivity::class.java)
                    intent.putExtra(StringContract.IntentStrings.GUID, group.guid)
                    intent.putExtra(StringContract.IntentStrings.NAME, group.name)
                    intent.putExtra(StringContract.IntentStrings.AVATAR, group.icon)
                    intent.putExtra(StringContract.IntentStrings.MEMBER_SCOPE, group.scope)
                    intent.putExtra(StringContract.IntentStrings.GROUP_OWNER, group.owner)
                    startActivity(intent)
                }
            }
        })
        rvCallList!!.setItemCallClickListener(object : OnItemClickListener<Call>() {
            override fun OnItemClick(`var`: Call, position: Int) {
                CometChat.initiateCall(`var`, object : CometChat.CallbackListener<Call>() {
                    override fun onSuccess(call: Call) {
                        Log.e("onSuccess: ", call.toString())
                        if (`var`.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                            val user: User
                            if (`var`.sender.uid == CometChat.getLoggedInUser().uid) {
                                user = `var`.callReceiver as User
                            } else {
                                user = `var`.sender
                            }
                            CallUtils.startCallIntent(context, user, CometChatConstants.CALL_TYPE_AUDIO, true, call.sessionId)
                        } else
                            CallUtils.startGroupCallIntent(context, call.callReceiver as Group, CometChatConstants.CALL_TYPE_AUDIO, true, call.sessionId)
                    }

                    override fun onError(e: CometChatException) {

                    }
                })
            }
        })
        rvCallList!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!rvCallList!!.canScrollVertically(1)) {
                    getCallList()
                }
            }
        })
        getCallList()
        return view
    }

    /**
     * This method is used to get the fetch the call List.
     */
    private fun getCallList() {
        if (messagesRequest == null) {
            messagesRequest = MessagesRequest.MessagesRequestBuilder().setCategory(CometChatConstants.CATEGORY_CALL).setLimit(30).build()
        }

        messagesRequest!!.fetchPrevious(object : CometChat.CallbackListener<List<BaseMessage>>() {
            override fun onSuccess(baseMessages: List<BaseMessage>) {
                Collections.reverse(baseMessages)
                rvCallList!!.setCallList(baseMessages)
                if (rvCallList!!.size() != 0) {
                    hideShimmer()
                    noCallView!!.visibility = View.GONE
                } else
                    noCallView!!.visibility = View.VISIBLE
            }

            override fun onError(e: CometChatException) {
                Log.e("onError: ", e.message.toString())
                if (rvCallList != null)
                    Snackbar.make(rvCallList!!, com.cometchat.pro.uikit.R.string.call_list_error, Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun hideShimmer() {
        shimmerFrameLayout!!.stopShimmer()
        shimmerFrameLayout!!.visibility = View.GONE
    }

}
