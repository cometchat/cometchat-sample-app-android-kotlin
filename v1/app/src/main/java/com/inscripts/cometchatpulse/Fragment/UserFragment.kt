package com.inscripts.cometchatpulse.Fragment


import android.app.Activity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.*
import android.widget.Toast
import com.cometchat.pro.models.User
import com.inscripts.cometchatpulse.Adapter.ContactListAdapter
import com.inscripts.cometchatpulse.Extensions.setTitleTypeface
import com.inscripts.cometchatpulse.Helpers.OnClickEvent

import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.Appearance
import com.inscripts.cometchatpulse.ViewModel.UserViewModel
import kotlinx.android.synthetic.main.fragment_ban_member.view.*
import kotlinx.android.synthetic.main.fragment_contact_list.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.fragment_user.view.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class UserFragment : Fragment() {

    private lateinit var userScope: String

    private lateinit var ownerId: String

    private lateinit var guid: String

    lateinit var contactListAdapter:ContactListAdapter

    lateinit var userViewModel: UserViewModel
    var selectedUserView:MutableMap<String,View> = mutableMapOf()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
       val mainView = inflater.inflate(R.layout.fragment_user, container, false)

        setHasOptionsMenu(true)
        guid = arguments?.getString(StringContract.IntentString.GROUP_ID).toString()


        ownerId = arguments?.getString(StringContract.IntentString.USER_ID).toString()

        try {
            userScope= arguments?.getString(StringContract.IntentString.USER_SCOPE)!!
        }catch (e:NullPointerException){
            e.printStackTrace()
        }
        contactListAdapter=ContactListAdapter(context,true,object :OnClickEvent{
            override fun onClickRl(item: View, any: Any) {

                if(any is User){

                    if(selectedUserView.containsKey(any.uid)){
                        selectedUserView[any.uid]?.setBackgroundColor(resources.getColor(android.R.color.transparent))
                        selectedUserView.remove(any.uid)
                    }
                    else {
                        selectedUserView[any.uid] = item
                        selectedUserView.get(any.uid)?.setBackgroundColor(resources.getColor(R.color.shimmer_background))

                    }
                }

            }

        })
        (activity as AppCompatActivity).setSupportActionBar(mainView.userList_Toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mainView.userList_Toolbar.title = getString(R.string.add_members)
        mainView.userList_Toolbar.setTitleTypeface(StringContract.Font.title)

        mainView.userList_Toolbar.navigationIcon?.setColorFilter(StringContract.Color.iconTint,
                PorterDuff.Mode.SRC_ATOP)

        if (StringContract.AppDetails.theme == Appearance.AppTheme.AZURE_RADIANCE) {
            mainView.userList_Toolbar.setTitleTextColor(StringContract.Color.black)
        } else {
            mainView.userList_Toolbar.setTitleTextColor(StringContract.Color.white)
        }

        mainView.userList_Toolbar.setBackgroundColor(StringContract.Color.primaryColor)

        userViewModel=ViewModelProviders.of(this).get(UserViewModel::class.java)

        userViewModel.fetchUser(LIMIT = 30,shimmer = null)


         mainView.rvAddMember.apply {
             layoutManager= androidx.recyclerview.widget.LinearLayoutManager(context)
             adapter=contactListAdapter
         }



        userViewModel.userList.observe(this,Observer { users ->
            users?.let { contactListAdapter.setUser(it) }
        })


        mainView.rvAddMember.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(1)) {
                    userViewModel.fetchUser(LIMIT = 30,shimmer = null)
                }
            }

        })

        return mainView
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {

        inflater?.inflate(R.menu.add_member,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

         when(item?.itemId){

             android.R.id.home->{
                 activity?.onBackPressed()
             }

             R.id.check->{

                 if (selectedUserView.isNotEmpty()){

                    val guidList=selectedUserView.keys

                    userViewModel.addMembertoGroup(activity as Activity,guidList,guid)
                 }
                 else{
                     Toast.makeText(context,"Select user",Toast.LENGTH_SHORT).show()
                 }
             }
         }

        return true
    }


}
