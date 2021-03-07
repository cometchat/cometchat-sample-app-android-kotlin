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
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.core.MessagesRequest
import com.cometchat.pro.core.MessagesRequest.MessagesRequestBuilder
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.ui_components.shared.cometchatCalls.CometChatCalls
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.snackbar.Snackbar
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener
import com.cometchat.pro.uikit.ui_components.groups.group_detail.CometChatGroupDetailActivity
import com.cometchat.pro.uikit.ui_components.users.user_details.CometChatUserDetailScreenActivity
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import java.util.*

class CallListViewFragment : Fragment() {
    private var rvCallList: CometChatCalls? = null
    private var noCallView: LinearLayout? = null
    private var shimmerFrameLayout: ShimmerFrameLayout? = null
    private var messagesRequest: MessagesRequest? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_call_list, container, false)
        rvCallList = view.findViewById(com.cometchat.pro.uikit.R.id.callList_rv)
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false
        )
        rvCallList!!.setLayoutManager(linearLayoutManager)
        shimmerFrameLayout = view.findViewById(R.id.contact_shimmer)
        noCallView = view.findViewById(com.cometchat.pro.uikit.R.id.no_call_vw)
        rvCallList!!.setItemClickListener(object : OnItemClickListener<Call?>() {
            override fun OnItemClick(t: Any, position: Int) {
                val `var` = t as Call
                if (`var`.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                    val user: User
                    user = if (`var`.sender.uid == CometChat.getLoggedInUser().uid) {
                        `var`.callReceiver as User
                    } else {
                        `var`.sender
                    }
                    val intent = Intent(context, CometChatUserDetailScreenActivity::class.java)
                    intent.putExtra(UIKitConstants.IntentStrings.UID, user.uid)
                    intent.putExtra(UIKitConstants.IntentStrings.NAME, user.name)
                    intent.putExtra(UIKitConstants.IntentStrings.AVATAR, user.avatar)
                    intent.putExtra(UIKitConstants.IntentStrings.STATUS, user.status)
                    intent.putExtra(UIKitConstants.IntentStrings.IS_BLOCKED_BY_ME, user.isBlockedByMe)
                    intent.putExtra(UIKitConstants.IntentStrings.FROM_CALL_LIST, true)
                    startActivity(intent)
                } else {
                    val group: Group
                    group = `var`.callReceiver as Group
                    val intent = Intent(context, CometChatGroupDetailActivity::class.java)
                    intent.putExtra(UIKitConstants.IntentStrings.GUID, group.guid)
                    intent.putExtra(UIKitConstants.IntentStrings.NAME, group.name)
                    intent.putExtra(UIKitConstants.IntentStrings.AVATAR, group.icon)
                    intent.putExtra(UIKitConstants.IntentStrings.MEMBER_SCOPE, group.scope)
                    intent.putExtra(UIKitConstants.IntentStrings.MEMBER_COUNT, group.membersCount)
                    intent.putExtra(UIKitConstants.IntentStrings.GROUP_OWNER, group.owner)
                    intent.putExtra(UIKitConstants.IntentStrings.GROUP_DESC, group.description)
                    intent.putExtra(UIKitConstants.IntentStrings.GROUP_PASSWORD, group.password)
                    intent.putExtra(UIKitConstants.IntentStrings.GROUP_TYPE, group.groupType)
                    startActivity(intent)
                }
            }
        })
        rvCallList!!.setItemCallClickListener(object : OnItemClickListener<Call?>() {
            override fun OnItemClick(t: Any, position: Int) {
                val `var` = t as Call
                CometChat.initiateCall(`var`, object : CallbackListener<Call>() {
                    override fun onSuccess(call: Call) {
                        Log.e("onSuccess: ", call.toString())
                        if (`var`.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                            val user: User
                            user = if (`var`.sender.uid == CometChat.getLoggedInUser().uid) {
                                `var`.callReceiver as User
                            } else {
                                `var`.sender
                            }
                            Utils.startCallIntent(context!!, user, CometChatConstants.CALL_TYPE_AUDIO, true, call.sessionId)
                        } else Utils.startGroupCallIntent(context!!, call.callReceiver as Group, CometChatConstants.CALL_TYPE_AUDIO, true, call.sessionId)
                    }

                    override fun onError(e: CometChatException) {}
                })
            }
        })
        rvCallList!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!rvCallList!!.canScrollVertically(1)) {
                    callList
                }
            }
        })
        callList
        return view
    }

    /**
     * This method is used to get the fetch the call List.
     */
    private val callList: Unit
        private get() {
            if (messagesRequest == null) {
                messagesRequest = MessagesRequestBuilder().setCategory(CometChatConstants.CATEGORY_CALL).setLimit(30).build()
            }
            messagesRequest!!.fetchPrevious(object : CallbackListener<List<BaseMessage?>?>() {
                override fun onSuccess(baseMessages: List<BaseMessage?>?) {
                    Collections.reverse(baseMessages)
                    rvCallList!!.setCallList(baseMessages)
                    if (rvCallList!!.size() != 0) {
                        hideShimmer()
                        noCallView!!.visibility = View.GONE
                    } else noCallView!!.visibility = View.VISIBLE
                }

                override fun onError(e: CometChatException) {
                    Log.e("onError: ", e.message)
                    if (rvCallList != null) Snackbar.make(rvCallList!!, com.cometchat.pro.uikit.R.string.call_list_error, Snackbar.LENGTH_LONG).show()
                }
            })
        }

    private fun hideShimmer() {
        shimmerFrameLayout!!.stopShimmer()
        shimmerFrameLayout!!.visibility = View.GONE
    }
}