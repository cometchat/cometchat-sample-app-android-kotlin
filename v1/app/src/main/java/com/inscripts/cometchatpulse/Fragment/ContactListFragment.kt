package com.inscripts.cometchatpulse.Fragment


import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.media.AudioManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.databinding.DataBindingUtil
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.models.User
import com.inscripts.cometchatpulse.Activities.MainActivity
import com.inscripts.cometchatpulse.Adapter.ContactListAdapter
import com.inscripts.cometchatpulse.CometChatPro
import com.inscripts.cometchatpulse.Helpers.CardItemTouchHelper
import com.inscripts.cometchatpulse.Helpers.UnreadCountInterface
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.Appearance
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.ViewModel.UserViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_contact_list.view.*


class ContactListFragment : Fragment() {

    companion object {
        private val TAG = "ContactListFragment"
    }

    lateinit var userViewModel: UserViewModel

    lateinit var contactListAdapter: ContactListAdapter

    private lateinit var linearLayoutManager: androidx.recyclerview.widget.LinearLayoutManager

    private lateinit var user:User

    var searchBoxOpen : Boolean = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view: View = layoutInflater.inflate(R.layout.fragment_contact_list, container, false)
        val config: Configuration = activity?.resources?.configuration!!
        if (config.smallestScreenWidthDp < 600) {
            CommonUtil.setCardView(view.contact_cardview)
        }
        userViewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)
        linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        view.contact_recycler.layoutManager = linearLayoutManager
        view.contact_recycler.itemAnimator= androidx.recyclerview.widget.DefaultItemAnimator()
        contactListAdapter = ContactListAdapter(activity,false)
        view.contact_recycler.adapter = contactListAdapter
        if (StringContract.AppDetails.theme == Appearance.AppTheme.AZURE_RADIANCE) {
            view.etSearch.setHintTextColor(StringContract.Color.black)
            view.etSearch.setTextColor(StringContract.Color.black)

        } else {
            view.etSearch.setHintTextColor(StringContract.Color.white)
            view.etSearch.setTextColor(StringContract.Color.white)
        }

        view.etSearch.background=CommonUtil.setDrawable(StringContract.Color.primaryDarkColor,16f)
        view.etSearch.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                var searchString=p0.toString()
                if (searchString.isNotEmpty()) {
                    searchBoxOpen = true
                    searchUser(searchString)
                }
                else{
                    searchBoxOpen = false
                    userViewModel.fetchUser(LIMIT = 30,shimmer = view.contact_shimmer)
                }
            }
        })

        try {

            val icon = context?.let { ContextCompat.getDrawable(it, R.drawable.ic_outline_video_call_white_24px) }
            icon?.setColorFilter(StringContract.Color.white,PorterDuff.Mode.SRC_ATOP)
            val color = StringContract.Color.primaryDarkColor
            val helper = object : CardItemTouchHelper(context!!,icon!!, color) {
                override fun onSwiped(p0: androidx.recyclerview.widget.RecyclerView.ViewHolder, p1: Int) {
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

        userViewModel.fetchUnreadCountForUser()


        userViewModel.userList.observe(this, Observer { users ->
            users?.let { contactListAdapter.setUser(it) }
        })



        userViewModel.unReadCount.observe(this, Observer {unReadCount->
            unReadCount?.let {
                contactListAdapter.setUnreadCount(it)
            }

        })


        view.contact_recycler.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!searchBoxOpen) {
                    if (!recyclerView.canScrollVertically(1)) {
                        userViewModel.fetchUser(LIMIT = 30, shimmer = view.contact_shimmer)
                    }
                }
            }

        })

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

    }

     fun searchUser(userName:String){
         userViewModel.searchUser(userName)
    }

    override fun onResume() {
        super.onResume()
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
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle(CommonUtil.setTitle(title, context))

                .setMessage(message + contactName)
                .setCancelable(true)
                .setNegativeButton(CommonUtil.setTitle("Cancel", context)) {
                    dialogInterface, i -> dialogInterface.dismiss() }
                .setPositiveButton(CommonUtil.setTitle("Yes", context)) { dialogInterface, i ->  userViewModel.initCall(context,user)
                }.show()
    }
}
