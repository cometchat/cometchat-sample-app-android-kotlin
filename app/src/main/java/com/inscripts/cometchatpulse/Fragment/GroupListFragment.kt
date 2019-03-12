package com.inscripts.cometchatpulse.Fragment


import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.models.Group
import com.inscripts.cometchatpulse.Adapter.GroupListAdapter
import com.inscripts.cometchatpulse.Helpers.RecyclerviewTouchListener
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.ViewModel.GroupViewModel
import kotlinx.android.synthetic.main.fragment_group_list.view.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class GroupListFragment : Fragment() {

    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var groupListAdapter: GroupListAdapter

    private lateinit var groupViewModel:GroupViewModel

    private var twoPane: Boolean=false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view:View= inflater.inflate(R.layout.fragment_group_list, container, false)

        val config: Configuration = activity?.resources?.configuration!!
        if(config.smallestScreenWidthDp <600 ) {
            CommonUtil.setCardView(view.group_Card)
        }
        //recycler view setup
        linearLayoutManager= LinearLayoutManager(context)
        view.group_recycler.layoutManager=linearLayoutManager
        groupListAdapter= GroupListAdapter(context)
        view.group_recycler.adapter=groupListAdapter

        groupViewModel=ViewModelProviders.of(this).get(GroupViewModel::class.java)


       groupViewModel.fetchGroups(LIMIT = 30,shimmerFrameLayout= view.contact_shimmer)


        groupViewModel.groupList.observe(this, Observer { groups ->
            groups?.let { groupListAdapter.setGroup(it) }
        })

        view.group_recycler.addOnScrollListener(object :RecyclerView.OnScrollListener(){

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(1)) {

                    groupViewModel.fetchGroups(LIMIT = 30,shimmerFrameLayout= view.contact_shimmer)

                }
            }
        })


        return view
    }

}
