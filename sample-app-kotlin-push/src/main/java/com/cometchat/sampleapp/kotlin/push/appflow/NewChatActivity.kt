package com.cometchat.sampleapp.kotlin.push.appflow

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.sampleapp.kotlin.push.R
import com.cometchat.sampleapp.kotlin.push.databinding.ActivityNewChatBinding
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson

/**
 * Activity for starting a new conversation.
 * Displays tabs for Users and Groups to select a conversation partner.
 */
class NewChatActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityNewChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        applyWindowInsets()
        adjustWindowSettings()
        
        setupToolbar()
        setupTabs()
        setupUsers()
        setupGroups()
    }
    
    private fun setupToolbar() {
        binding.ivBack.setOnClickListener { finish() }
        binding.tvTitle.setTextColor(CometChatTheme.getTextColorPrimary(this))
    }
    
    private fun setupTabs() {
        // Hide toolbar for Users and Groups since this activity has its own toolbar
        binding.users.setToolbarVisibility(View.GONE)
        binding.groups.setToolbarVisibility(View.GONE)
        
        // Add tabs
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.app_bottom_nav_users)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.app_bottom_nav_groups)))
        binding.tabLayout.getTabAt(0)?.select()
        
        // Tab selection listener
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    binding.users.visibility = View.VISIBLE
                    binding.groups.visibility = View.GONE
                } else {
                    binding.users.visibility = View.GONE
                    binding.groups.visibility = View.VISIBLE
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        
        // Apply tab colors
        binding.tabLayout.setTabTextColors(
            CometChatTheme.getTextColorSecondary(this),
            CometChatTheme.getPrimaryColor(this)
        )
    }
    
    private fun setupUsers() {
        // TODO: setUsersRequestBuilder not available in chatuikit-kotlin CometChatUsers
        // Configure users request builder - hide blocked users
        // binding.users.setUsersRequestBuilder(
        //     UsersRequestBuilder()
        //         .hideBlockedUsers(true)
        //         .setLimit(30)
        // )
        
        // TODO: setOnItemClick callback signature changed in chatuikit-kotlin
        // Old: (View, Int, User) -> Unit
        // New: (User) -> Unit
        binding.users.setOnItemClick { user ->
            val intent = Intent(this@NewChatActivity, MessagesActivity::class.java)
            intent.putExtra(getString(R.string.app_user), Gson().toJson(user))
            startActivity(intent)
            finish()
        }
    }
    
    private fun setupGroups() {
        // TODO: setGroupsRequestBuilder not available in chatuikit-kotlin CometChatGroups
        // Configure groups request builder - joined groups only
        // binding.groups.setGroupsRequestBuilder(
        //     GroupsRequestBuilder()
        //         .joinedOnly(true)
        //         .setLimit(30)
        // )
        
        binding.groups.setOnItemClick { group ->
            val intent = Intent(this@NewChatActivity, MessagesActivity::class.java)
            intent.putExtra(getString(R.string.app_group), Gson().toJson(group))
            startActivity(intent)
            finish()
        }
    }
    
    private fun adjustWindowSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
        } else {
            @Suppress("DEPRECATION")
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.newChatMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
