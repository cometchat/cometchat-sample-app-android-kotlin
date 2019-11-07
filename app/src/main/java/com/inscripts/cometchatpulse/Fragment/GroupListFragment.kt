package com.inscripts.cometchatpulse.Fragment


import android.app.ProgressDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.inscripts.cometchatpulse.Utils.Appearance
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.ViewModel.GroupViewModel
import kotlinx.android.synthetic.main.fragment_contact_list.view.*
import kotlinx.android.synthetic.main.fragment_group_list.view.*
import kotlinx.android.synthetic.main.fragment_group_list.view.contact_shimmer
import kotlinx.android.synthetic.main.fragment_group_list.view.etSearch
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


/**
 * A simple [Fragment] subclass.
 *
 */
class GroupListFragment : Fragment() {

    private lateinit var linearLayoutManager: androidx.recyclerview.widget.LinearLayoutManager

    private lateinit var groupListAdapter: GroupListAdapter

    private lateinit var groupViewModel: GroupViewModel

    private var twoPane: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_group_list, container, false)

        val config: Configuration = activity?.resources?.configuration!!
        if (config.smallestScreenWidthDp < 600) {
            CommonUtil.setCardView(view.group_Card)
        }
        //recycler view setup
        linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        view.group_recycler.layoutManager = linearLayoutManager
        groupListAdapter = GroupListAdapter(context)
        view.group_recycler.adapter = groupListAdapter

        groupViewModel = ViewModelProviders.of(this).get(GroupViewModel::class.java)


        groupViewModel.fetchGroups(LIMIT = 30, shimmerFrameLayout = view.contact_shimmer)

        groupViewModel.fetchGroupUnreadCount()

        if (StringContract.AppDetails.theme == Appearance.AppTheme.AZURE_RADIANCE) {
            view.etSearch.setHintTextColor(StringContract.Color.black)
            view.etSearch.setTextColor(StringContract.Color.black)

        } else {
            view.etSearch.setHintTextColor(StringContract.Color.white)
            view.etSearch.setTextColor(StringContract.Color.white)
        }

        view.etSearch.background = CommonUtil.setDrawable(StringContract.Color.primaryDarkColor, 16f)

        view.etSearch.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                Log.d("GroupListFragment","AfterTextChanges")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("GroupListFragment","beforeTextChanges")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchString = s.toString()
                if (searchString.isNotEmpty()) {
                    Log.d("","onSuccessSEARCH")
                    searchGroup(searchString)
                } else {
                    Log.d("","SEARCHFAILED")
                    groupViewModel.fetchGroups(LIMIT = 30, shimmerFrameLayout = view.contact_shimmer)
                }
            }

        })



        groupViewModel.groupList.observe(this, Observer { groups ->
            groups?.let {
                groupListAdapter.setGroup(it)
            }
        })



        view.group_recycler.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: androidx.recyclerview.widget.RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(1)) {

                    groupViewModel.fetchGroups(LIMIT = 30, shimmerFrameLayout = view.contact_shimmer)

                }
            }
        })


        return view
    }

    fun searchGroup(s: String) {
        groupViewModel.searchGroup(s)
    }

}
