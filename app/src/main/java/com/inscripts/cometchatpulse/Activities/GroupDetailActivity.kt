package com.inscripts.cometchatpulse.Activities

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.graphics.PorterDuff
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.cometchat.pro.core.CometChat
import com.inscripts.cometchatpulse.Adapter.GroupOptionAdapter
import com.inscripts.cometchatpulse.Extensions.setTitleTypeface
import com.inscripts.cometchatpulse.Fragment.BanMemberFragment
import com.inscripts.cometchatpulse.Fragment.MemberFragment
import com.inscripts.cometchatpulse.Helpers.OnOptionClickListener
import com.inscripts.cometchatpulse.Pojo.GroupOption
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.Appearance
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.ViewModel.GroupViewModel
import com.inscripts.cometchatpulse.databinding.ActivityGroupDetailBinding

class GroupDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupDetailBinding

    private lateinit var groupId: String

    private lateinit var ownerUid: String

    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var groupOptionAdapter: GroupOptionAdapter

    private var groupOptionList: MutableList<GroupOption> = mutableListOf()

    private lateinit var groupViewModel: GroupViewModel

    private  var groupOwner:String?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = DataBindingUtil.setContentView(this, R.layout.activity_group_detail)

        CommonUtil.setStatusBarColor(this)

        groupOwner = intent?.getStringExtra(StringContract.IntentString.GROUP_OWNER)

        binding.groupName = intent?.getStringExtra(StringContract.IntentString.GROUP_NAME)

        binding.groupIcon = intent?.getStringExtra(StringContract.IntentString.GROUP_ICON)

        binding.description = intent?.getStringExtra(StringContract.IntentString.GROUP_DESCRIPTION)

        ownerUid = CometChat.getLoggedInUser().uid

        groupId = intent?.getStringExtra(StringContract.IntentString.GROUP_ID).toString()

        binding.tvDescription.typeface=StringContract.Font.name
        binding.tvName.typeface=StringContract.Font.name



        groupViewModel = ViewModelProviders.of(this).get(GroupViewModel::class.java)

        layoutManager = LinearLayoutManager(this)
        binding.rvGroupOptions.layoutManager = layoutManager
        groupOptionAdapter = GroupOptionAdapter(this, GroupOptionItemList(), ownerUid, groupId,object :OnOptionClickListener(){

            override fun OnOptionClick( position:Int){

                when (position) {

                    0 -> {
                        val memberListFragment = MemberFragment().apply {
                            arguments = Bundle().apply {
                                putString(StringContract.IntentString.GROUP_ID, groupId)
                                putString(StringContract.IntentString.USER_ID, ownerUid)
                            }
                        }
                        supportFragmentManager.beginTransaction()
                                .replace(R.id.group_frame, memberListFragment).addToBackStack(null).commit()
                    }

                    1 -> {
                        val banMemberListFragment = BanMemberFragment().apply {
                            arguments = Bundle().apply {
                                putString(StringContract.IntentString.GROUP_ID, groupId)
                                putString(StringContract.IntentString.USER_ID, ownerUid)
                            }
                        }
                        supportFragmentManager.beginTransaction()
                                .replace(R.id.group_frame, banMemberListFragment).addToBackStack(null).commit()
                    }
                    2 -> {
                        showDialog("Are you sure want to leave ? "+intent?.getStringExtra(StringContract.IntentString.GROUP_NAME),
                                "Leave Group",position)
                    }

                    3 -> {
                        showDialog("Are you sure want to delete ? "+intent?.getStringExtra(StringContract.IntentString.GROUP_NAME),
                                "Delete Group",position)
                    }

                }
            }

        })
        binding.rvGroupOptions.adapter = groupOptionAdapter


        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title="Group Details"
        supportActionBar!!.elevation = 10f
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        binding.toolbar.setTitleTypeface(StringContract.Font.title)
        binding.toolbar.navigationIcon?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)

        if (StringContract.AppDetails.theme == Appearance.AppTheme.AZURE_RADIANCE) {
            binding.tv30.setBackgroundColor(StringContract.Color.primaryDarkColor)
           binding.toolbar.setTitleTextColor(StringContract.Color.black)

        } else {
            binding.tv30.setBackgroundColor(StringContract.Color.primaryColor)
            binding.toolbar.setTitleTextColor(StringContract.Color.white)
        }

    }


    private fun GroupOptionItemList(): MutableList<GroupOption> {

        groupOptionList.add(GroupOption("View Members", ContextCompat.getDrawable(this, R.drawable.ic_person_black_24dp)))
        groupOptionList.add(GroupOption("Unban Members", ContextCompat.getDrawable(this, R.drawable.ic_supervisor_account_black_24dp)))
        groupOptionList.add(GroupOption("Leave Group", ContextCompat.getDrawable(this, R.drawable.ic_exit_to_app_black_24dp)))
         if (ownerUid.equals(groupOwner)) {
             groupOptionList.add(GroupOption("Delete Group", ContextCompat.getDrawable(this, R.drawable.ic_delete_black_24dp)))
         }
        return groupOptionList
    }


    fun showDialog(message: String, title: String, position: Int) {
        val builder = android.support.v7.app.AlertDialog.Builder(this)
        builder.setTitle(CommonUtil.setTitle(title, this))

                .setMessage(message)
                .setCancelable(true)
                .setNegativeButton(CommonUtil.setTitle("Cancel", this)) {
                    dialogInterface, i -> dialogInterface.dismiss() }
                .setPositiveButton(CommonUtil.setTitle("Yes", this)) {
                    dialogInterface, i ->  when(position){

                    2->{
                        groupViewModel.leaveGroup(groupId, this@GroupDetailActivity)
                    }

                    3->{
                        groupViewModel.deleteGroup(groupId,this@GroupDetailActivity)
                    }
                }
                }.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

}
