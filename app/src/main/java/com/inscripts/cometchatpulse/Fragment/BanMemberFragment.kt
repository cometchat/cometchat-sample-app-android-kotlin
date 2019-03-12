package com.inscripts.cometchatpulse.Fragment


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.*
import com.cometchat.pro.models.User
import com.inscripts.cometchatpulse.Adapter.MemberListAdapter
import com.inscripts.cometchatpulse.Extensions.setTitleTypeface
import com.inscripts.cometchatpulse.Helpers.OnClickEvent
import com.inscripts.cometchatpulse.Helpers.RecyclerviewTouchListener

import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.Appearance
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.ViewModel.GroupChatViewModel
import kotlinx.android.synthetic.main.fragment_ban_member.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.coroutines.*
import java.lang.Exception


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class BanMemberFragment : Fragment() {


    private lateinit var guid: String

    private lateinit var ownerId: String

    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var groupChatViewModel: GroupChatViewModel

    private lateinit var memberListAdapter: MemberListAdapter

    private lateinit var member: User

    lateinit var my: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        my = inflater.inflate(R.layout.fragment_ban_member, container, false)

        groupChatViewModel = ViewModelProviders.of(this).get(GroupChatViewModel::class.java)

        guid = arguments?.getString(StringContract.IntentString.GROUP_ID).toString()

        ownerId = arguments?.getString(StringContract.IntentString.USER_ID).toString()

        (activity as AppCompatActivity).setSupportActionBar(my.banmember_toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        my.banmember_toolbar.title = "Banned Members"
        my.banmember_toolbar.setTitleTypeface(StringContract.Font.title)

        my.banmember_toolbar.navigationIcon?.setColorFilter(StringContract.Color.iconTint,
                PorterDuff.Mode.SRC_ATOP)

        if (StringContract.AppDetails.theme== Appearance.AppTheme.AZURE_RADIANCE){
            my.banmember_toolbar.setTitleTextColor(StringContract.Color.black)
        }
        else{
            my.banmember_toolbar.setTitleTextColor(StringContract.Color.white)
        }

        my.banmember_toolbar.setBackgroundColor(StringContract.Color.primaryColor)

        linearLayoutManager = LinearLayoutManager(context)
        my.rv_ban_member.setLayoutManager(linearLayoutManager)
        try {
            memberListAdapter = MemberListAdapter(ownerId,R.layout.group_member_item, object : OnClickEvent {
                override fun onClickRl(item: View, user: Any) {

                    if (user is User) {
                        member = user
                        if (ownerId != user.getUid()) {
                            val popup = context?.let { PopupMenu(it, item) }
                            //Inflating the Popup using xml file
                            popup?.getMenuInflater()?.inflate(R.menu.menu_group_action, popup.getMenu())

                            popup?.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                                override fun onMenuItemClick(p0: MenuItem?): Boolean {

                                    when (p0!!.itemId) {

                                        R.id.menu_item_Reinstate -> {
                                            groupChatViewModel.unbanMember(member.uid, guid)
                                        }

                                    }

                                    return true
                                }

                            })

                            popup?.show()
                        }
                    }
                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        my.rv_ban_member.adapter = memberListAdapter


        Thread {
            groupChatViewModel.getBanedMember(guid, LIMIT = 10)
        }.start()



        groupChatViewModel.banMemberList.observe(this, Observer { groupMember ->
            groupMember?.let {
                memberListAdapter.setMemberList(it)
            }
        })


        my.rv_ban_member.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(1)) {
                    groupChatViewModel.getBanedMember(guid, LIMIT = 10)
                }
            }

        })

        return my
    }
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId){

            android.R.id.home->{
                activity?.onBackPressed()
            }
        }

        return true
    }
}
