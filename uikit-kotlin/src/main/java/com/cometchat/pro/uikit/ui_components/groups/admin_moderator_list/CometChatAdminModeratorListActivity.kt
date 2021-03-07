package com.cometchat.pro.uikit.ui_components.groups.admin_moderator_list

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants

class CometChatAdminModeratorListActivity : AppCompatActivity() {
    private var guid: String? = null
    private var ownerId: String? = null
    private var showModerator = false
    private var loggedInUserScope: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen)
        handleIntent()
    }

    private fun handleIntent() {
        if (intent.hasExtra(UIKitConstants.IntentStrings.MEMBER_SCOPE)) {
            loggedInUserScope = intent.getStringExtra(UIKitConstants.IntentStrings.MEMBER_SCOPE)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.GUID)) {
            guid = intent.getStringExtra(UIKitConstants.IntentStrings.GUID)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.GROUP_OWNER)) {
            ownerId = intent.getStringExtra(UIKitConstants.IntentStrings.GROUP_OWNER)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.SHOW_MODERATORLIST)) {
            showModerator = intent.getBooleanExtra(UIKitConstants.IntentStrings.SHOW_MODERATORLIST, false)
        }
        val fragment: Fragment = CometChatAdminModeratorList()
        val bundle = Bundle()
        bundle.putString(UIKitConstants.IntentStrings.GUID, guid)
        bundle.putString(UIKitConstants.IntentStrings.GROUP_OWNER, ownerId)
        bundle.putString(UIKitConstants.IntentStrings.MEMBER_SCOPE, loggedInUserScope)
        bundle.putBoolean(UIKitConstants.IntentStrings.SHOW_MODERATORLIST, showModerator)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.frame_fragment, fragment).commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
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