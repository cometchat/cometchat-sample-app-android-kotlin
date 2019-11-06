package com.inscripts.cometchatpulse.Activities

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingUtil
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.PopupMenu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.constants.CometChatConstants.Params.LIMIT
import com.cometchat.pro.models.User
import com.inscripts.cometchatpulse.Adapter.ContactListAdapter
import com.inscripts.cometchatpulse.Helpers.OnClickEvent
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.ViewModel.UserViewModel
import com.inscripts.cometchatpulse.databinding.ActivityBlockUserListBinding
import kotlinx.android.synthetic.main.activity_block_user_list.*

class BlockUserListActivity : AppCompatActivity() {

    private lateinit var binding:ActivityBlockUserListBinding

    private lateinit var userViewModel: UserViewModel

    private lateinit var contactListAdapter: ContactListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=DataBindingUtil.setContentView(this,R.layout.activity_block_user_list)

        setSupportActionBar(binding.toolbarBlockedUser)
        binding.toolbarBlockedUser.setBackgroundColor(StringContract.Color.primaryColor)
        binding.toolbarBlockedUser.setTitleTextColor(StringContract.Color.white)



        try {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title="Blocked Users"
            binding.toolbarBlockedUser.navigationIcon?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)
        } catch (e: Exception) {
        }

        CommonUtil.setStatusBarColor(this)

        userViewModel=ViewModelProviders.of(this).get(UserViewModel::class.java)

        userViewModel.getBlockedUserList(LIMIT = 100)


        contactListAdapter= ContactListAdapter(this,true,object :OnClickEvent{

            override fun onClickRl(item: View, user: Any) {

                if (user is User) {

                    val popup =  PopupMenu(this@BlockUserListActivity, item)
                            //Inflating the Popup using xml file
                            popup.menuInflater.inflate(R.menu.menu_unblock, popup.getMenu())

                            popup.setOnMenuItemClickListener { p0 ->

                                when (p0!!.itemId) {

                                    R.id.unblock_menu -> {
                                       userViewModel.unBlockUser(user)
                                    }

                                }

                                true
                            }

                            popup.show()
                        }

            }

        })

        binding.rvBlockedUser.apply {
            layoutManager= androidx.recyclerview.widget.LinearLayoutManager(this@BlockUserListActivity)
            adapter=contactListAdapter
        }

        userViewModel.blockedUserList.observe(this, Observer {
            it?.let { it1 -> contactListAdapter.setUser(it1) }
        })

        userViewModel.blockedUser.observe(this, Observer {
            contactListAdapter.removeUser(it)
        })


    }
}
