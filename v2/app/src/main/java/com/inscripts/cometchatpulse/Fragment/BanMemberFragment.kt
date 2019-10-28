package com.inscripts.cometchatpulse.Fragment


import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import android.view.*
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.models.User
import com.inscripts.cometchatpulse.Adapter.MemberListAdapter
import com.inscripts.cometchatpulse.Extensions.setTitleTypeface
import com.inscripts.cometchatpulse.Helpers.OnClickEvent
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.Appearance
import com.inscripts.cometchatpulse.ViewModel.GroupChatViewModel
import kotlinx.android.synthetic.main.fragment_ban_member.view.*


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

    private lateinit var linearLayoutManager: androidx.recyclerview.widget.LinearLayoutManager

    private lateinit var groupChatViewModel: GroupChatViewModel

    private lateinit var memberListAdapter: MemberListAdapter

    private lateinit var member: User

    private var userScope: String? = null

    lateinit var my: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        my = inflater.inflate(R.layout.fragment_ban_member, container, false)

        groupChatViewModel = ViewModelProviders.of(this).get(GroupChatViewModel::class.java)

        guid = arguments?.getString(StringContract.IntentString.GROUP_ID).toString()

        ownerId = arguments?.getString(StringContract.IntentString.USER_ID).toString()

        try {
            userScope = arguments?.getString(StringContract.IntentString.USER_SCOPE)!!
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }

        (activity as AppCompatActivity).setSupportActionBar(my.banmember_toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        my.banmember_toolbar.title = getString(R.string.banned_members)
        my.banmember_toolbar.setTitleTypeface(StringContract.Font.title)

        my.banmember_toolbar.navigationIcon?.setColorFilter(StringContract.Color.iconTint,
                PorterDuff.Mode.SRC_ATOP)

        if (StringContract.AppDetails.theme == Appearance.AppTheme.AZURE_RADIANCE) {
            my.banmember_toolbar.setTitleTextColor(StringContract.Color.black)
        } else {
            my.banmember_toolbar.setTitleTextColor(StringContract.Color.white)
        }

        my.banmember_toolbar.setBackgroundColor(StringContract.Color.primaryColor)

        linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        my.rv_ban_member.setLayoutManager(linearLayoutManager)
        try {
            memberListAdapter = MemberListAdapter(context, ownerId, R.layout.group_member_item, object : OnClickEvent {
                override fun onClickRl(item: View, user: Any) {

                    if (user is User) {
                        member = user
                        if (ownerId != user.uid) {

                            if (userScope == CometChatConstants.SCOPE_ADMIN ||
                                    userScope == CometChatConstants.SCOPE_MODERATOR) {


                                val popup = context?.let { PopupMenu(it, item) }
                                //Inflating the Popup using xml file
                                popup?.menuInflater?.inflate(R.menu.menu_group_action, popup.getMenu())

                                popup?.setOnMenuItemClickListener { p0 ->
                                    when (p0!!.itemId) {

                                        R.id.menu_item_Reinstate -> {
                                            groupChatViewModel.unbanMember(member.uid, guid)
                                        }

                                    }

                                    true
                                }

                                popup?.show()
                            }
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
            groupMember?.let {memberListAdapter.setMemberList(it)}
        })


        my.rv_ban_member.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
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

        when (item?.itemId) {

            android.R.id.home -> {
                activity?.onBackPressed()
            }
        }

        return true
    }


}
