package com.cometchat.pro.uikit.ui_components.groups.banned_members

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.pro.uikit.R
import com.google.android.material.appbar.MaterialToolbar
import com.cometchat.pro.uikit.ui_resources.constants.UIKitContracts

class CometChatBanMembersActivity : AppCompatActivity() {
    private var guid: String? = null
    private var gName: String? = null
    private var loggedInUserScope: String? = null
    private var banToolbar: MaterialToolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cometchat_ban_members)
        banToolbar = findViewById(R.id.banToolbar)
        banToolbar!!.setNavigationOnClickListener(View.OnClickListener { onBackPressed() })
        handleIntent()
        val banFragment = CometChatBanMembers()
        val bundle = Bundle()
        bundle.putString(UIKitContracts.IntentStrings.GUID, guid)
        bundle.putString(UIKitContracts.IntentStrings.GROUP_NAME, gName)
        bundle.putString(UIKitContracts.IntentStrings.MEMBER_SCOPE, loggedInUserScope)
        banFragment.arguments = bundle
        supportFragmentManager.beginTransaction().add(R.id.ban_member_frame, banFragment).commit()
    }

    private fun handleIntent() {
        if (intent.hasExtra(UIKitContracts.IntentStrings.GUID)) {
            guid = intent.getStringExtra(UIKitContracts.IntentStrings.GUID)
        }
        if (intent.hasExtra(UIKitContracts.IntentStrings.GROUP_NAME)) {
            gName = intent.getStringExtra(UIKitContracts.IntentStrings.GROUP_NAME)
            banToolbar!!.title = String.format(resources.getString(R.string.ban_member_of_group), gName)
        }
        if (intent.hasExtra(UIKitContracts.IntentStrings.MEMBER_SCOPE)) {
            loggedInUserScope = intent.getStringExtra(UIKitContracts.IntentStrings.MEMBER_SCOPE)
        }
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