package com.inscripts.cometchatpulse.Fragment


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cometchat.pro.models.User
import com.inscripts.cometchatpulse.Adapter.ContactListAdapter
import com.inscripts.cometchatpulse.Helpers.CardItemTouchHelper
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.ViewModel.UserViewModel
import kotlinx.android.synthetic.main.fragment_contact_list.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ContactListFragment : Fragment() {

    companion object {
        private val TAG = this::class.java.simpleName
    }

    lateinit var userViewModel: UserViewModel

    lateinit var contactListAdapter: ContactListAdapter

    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var user:User


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view: View = layoutInflater.inflate(R.layout.fragment_contact_list, container, false)
        val config: Configuration = activity?.resources?.configuration!!
        if (config.smallestScreenWidthDp < 600) {
            CommonUtil.setCardView(view.contact_cardview)
        }

        userViewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)
        linearLayoutManager = LinearLayoutManager(activity)
        view.contact_recycler.layoutManager = linearLayoutManager
        view.contact_recycler.itemAnimator=DefaultItemAnimator()
        contactListAdapter = ContactListAdapter(activity,false)
        view.contact_recycler.adapter = contactListAdapter

        try {

            val icon = context?.let { ContextCompat.getDrawable(it, R.drawable.ic_outline_video_call_white_24px) }
            icon?.setColorFilter(StringContract.Color.white,PorterDuff.Mode.SRC_ATOP)
            val color = StringContract.Color.primaryDarkColor
            val helper = object : CardItemTouchHelper(context!!,icon!!, color) {
                override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {
                    contactListAdapter.notifyDataSetChanged()
                    user = (p0.itemView.getTag(  R.string.user) as User)
                    showDialog("Are you sure you want to call ","Please Confirm",user.name,context)
                }
            }

            val itemTouchHelper = ItemTouchHelper(helper)

            itemTouchHelper.attachToRecyclerView(view.contact_recycler)

        } catch (e: Exception) {
            e.printStackTrace()
        }


        userViewModel.fetchUser(LIMIT = 30,shimmer = view.contact_shimmer)


        userViewModel.userList.observe(this, Observer { users ->
            users?.let { contactListAdapter.setUser(it) }
        })


        view.contact_recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(1)) {
                    userViewModel.fetchUser(LIMIT = 30,shimmer = view.contact_shimmer)
                }
            }

        })

        return view
    }

    override fun onStart() {
        super.onStart()
        userViewModel.addPresenceListener(StringContract.ListenerName.USER_LISTENER)
    }

    override fun onDestroy() {
        super.onDestroy()
        userViewModel.removeUserListener(StringContract.ListenerName.USER_LISTENER)
    }

    fun showDialog(message: String, title: String, contactName: String, context: Context) {
        val builder = android.support.v7.app.AlertDialog.Builder(context)
        builder.setTitle(CommonUtil.setTitle(title, context))

                .setMessage(message + contactName)
                .setCancelable(true)
                .setNegativeButton(CommonUtil.setTitle("Cancel", context)) {
                    dialogInterface, i -> dialogInterface.dismiss() }
                .setPositiveButton(CommonUtil.setTitle("Yes", context), {
                    dialogInterface, i ->  userViewModel.initCall(context,user)
                }).show()
    }

}
