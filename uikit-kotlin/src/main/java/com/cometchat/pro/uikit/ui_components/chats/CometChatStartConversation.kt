package com.cometchat.pro.uikit.ui_components.chats

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.groups.group_list.CometChatGroupList
import com.cometchat.pro.uikit.ui_components.shared.cometchatSharedMedia.adapter.TabAdapter
import com.cometchat.pro.uikit.ui_components.users.user_list.CometChatUserList
import com.cometchat.pro.uikit.ui_settings.UIKitSettings
import com.cometchat.pro.uikit.ui_settings.enum.ConversationMode
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout

class CometChatStartConversation : AppCompatActivity() {

    private var viewPager: ViewPager? = null

    private var tabLayout: TabLayout? = null

    private var adapter: TabAdapter? = null
    private lateinit var toolbar : MaterialToolbar
    private val conversationType: String = UIKitSettings.conversationInMode.toString()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_conversation_from_chat)

        toolbar = findViewById(R.id.start_chat_toolbar)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        adapter = TabAdapter(supportFragmentManager)

        val cometChatUserList = CometChatUserList()
        cometChatUserList.setTitleVisible(false)
        val cometChatGroupList = CometChatGroupList()
        cometChatGroupList.setTitleVisible(false)
        cometChatGroupList.setGroupCreateVisible(false)

        when (conversationType) {
            ConversationMode.ALL_CHATS.toString() -> {
                adapter?.addFragment(cometChatUserList, getString(R.string.users))
                adapter?.addFragment(cometChatGroupList, getString(R.string.groups))
            }
            ConversationMode.GROUP.toString() -> {
                toolbar.title = getString(R.string.select_group)
                tabLayout?.visibility = View.GONE;
                adapter?.addFragment(cometChatGroupList, getString(R.string.groups));
            }
            else -> {
                toolbar.title = getString(R.string.select_user)
                tabLayout?.visibility = View.GONE;
                adapter?.addFragment(cometChatUserList, getString(R.string.users));
            }
        }

        viewPager?.adapter = adapter
        viewPager?.offscreenPageLimit = 3
        tabLayout?.setupWithViewPager(viewPager)

        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    toolbar.title = getString(R.string.select_user)
                } else {
                    toolbar.title = getString(R.string.select_group)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}