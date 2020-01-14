package com.inscripts.cometchatpulse.Fragment


import android.app.SearchManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.ConversationsRequest
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.helpers.CometChatHelper
import com.cometchat.pro.models.*
import com.facebook.shimmer.ShimmerFrameLayout
import com.inscripts.cometchatpulse.Adapter.RecentListAdapter
import com.inscripts.cometchatpulse.CometChatPro
import com.inscripts.cometchatpulse.Helpers.*
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.Appearance
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.ViewModel.RecentViewModel
import com.inscripts.cometchatpulse.ViewModel.UserViewModel
import kotlinx.android.synthetic.main.fragment_contact_list.view.*
import kotlinx.android.synthetic.main.fragment_recent_list.*
import kotlinx.android.synthetic.main.fragment_recent_list.view.*
//import kotlinx.android.synthetic.main.fragment_recent_list.view.etSearch


class RecentListFragment : Fragment() {

    companion object {
        private val TAG = "RecentListFragment"
    }

    var recentListAdapter: RecentListAdapter? = null

    private lateinit var linearLayoutManager: androidx.recyclerview.widget.LinearLayoutManager

    val conversationList : ArrayList<Conversation> = ArrayList()

    lateinit var recentViewModel : RecentViewModel

    private lateinit var childClickListener: ChildClickListener

    private lateinit var shimmerFrameLayout: ShimmerFrameLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view: View = layoutInflater.inflate(R.layout.fragment_recent_list, container, false)
        val config: Configuration = activity?.resources?.configuration!!
        if (config.smallestScreenWidthDp < 600) {
            CommonUtil.setCardView(view.recent_cardview)
        }
        childClickListener=activity as ChildClickListener
        recentViewModel = ViewModelProviders.of(this).get(RecentViewModel::class.java)
//
        linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        view.recent_recycler.layoutManager = linearLayoutManager
        view.recent_recycler.itemAnimator= androidx.recyclerview.widget.DefaultItemAnimator()!!
        recentListAdapter = RecentListAdapter(context!!,conversationList,object : OnUserClick{
            override fun onItemClick(item: View, any: Any) {
                childClickListener.OnChildClick(any)
            }
        })
        view.recent_recycler.adapter = recentListAdapter

        recentViewModel.conversationList.observe(this, Observer {

            list-> recentListAdapter?.setConversation(list)
        })

        recentViewModel.filterList.observe(this, Observer {

            list-> recentListAdapter?.setConversationFilter(list)
        })

        recentViewModel.conversation.observe(this, Observer {
            conversation -> recentListAdapter?.updateConversation(conversation)
        })
        view.recent_recycler.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                        recentViewModel.fetchConversation(LIMIT = 2, shimmer = recent_shimmer)
                }
            }

        })
        return view
    }

    override fun onStart() {
        super.onStart()
        recentViewModel.addMessageListener(TAG);
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume called")
        recentViewModel.addMessageListener(TAG)
        recentViewModel.fetchConversation(LIMIT = 2,shimmer = recent_shimmer,isRefresh = true)

    }

    override fun onDestroy() {
        super.onDestroy()
        removeMessageListener(TAG)
    }


    fun removeMessageListener(tag: String) {
        CometChat.removeMessageListener(tag)
    }

}
