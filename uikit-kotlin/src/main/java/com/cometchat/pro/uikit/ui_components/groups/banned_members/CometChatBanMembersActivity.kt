package com.cometchat.pro.uikit.ui_components.groups.banned_members

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction
import com.cometchat.pro.uikit.ui_settings.UIKitSettings
import com.google.android.material.appbar.MaterialToolbar

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
        if (UIKitSettings.color != null) window.statusBarColor = Color.parseColor(UIKitSettings.color)
        val banFragment = CometChatBanMembers()
        val bundle = Bundle()
        bundle.putString(UIKitConstants.IntentStrings.GUID, guid)
        bundle.putString(UIKitConstants.IntentStrings.GROUP_NAME, gName)
        bundle.putString(UIKitConstants.IntentStrings.MEMBER_SCOPE, loggedInUserScope)
        banFragment.arguments = bundle
        supportFragmentManager.beginTransaction().add(R.id.ban_member_frame, banFragment).commit()
    }

    private fun handleIntent() {
        if (intent.hasExtra(UIKitConstants.IntentStrings.GUID)) {
            guid = intent.getStringExtra(UIKitConstants.IntentStrings.GUID)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.GROUP_NAME)) {
            gName = intent.getStringExtra(UIKitConstants.IntentStrings.GROUP_NAME)
//            banToolbar!!.title = String.format(resources.getString(R.string.ban_member_of_group), gName)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.MEMBER_SCOPE)) {
            loggedInUserScope = intent.getStringExtra(UIKitConstants.IntentStrings.MEMBER_SCOPE)
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