package com.cometchat.pro.uikit.ui_components.groups.group_members

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction
import com.cometchat.pro.uikit.ui_settings.UIKitSettings

class CometChatGroupMemberListActivity : AppCompatActivity() {
    private var transferOwnership = false
    private var guid: String? = null
    private var showModerators = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen)
        guid = intent.getStringExtra(UIKitConstants.IntentStrings.GUID)
        showModerators = intent.getBooleanExtra(UIKitConstants.IntentStrings.SHOW_MODERATORLIST, showModerators)
        transferOwnership = intent.getBooleanExtra(UIKitConstants.IntentStrings.TRANSFER_OWNERSHIP, transferOwnership)
        val fragment: Fragment = CometChatGroupMemberList()
        val bundle = Bundle()
        bundle.putString(UIKitConstants.IntentStrings.GUID, guid)
        bundle.putBoolean(UIKitConstants.IntentStrings.SHOW_MODERATORLIST, showModerators)
        bundle.putBoolean(UIKitConstants.IntentStrings.TRANSFER_OWNERSHIP, transferOwnership)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.frame_fragment, fragment).commit()
        if (UIKitSettings.color != null) window.statusBarColor = Color.parseColor(UIKitSettings.color)
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