package com.cometchat.pro.uikit.ui_components.userProfile.privacy_and_security

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.pro.core.BlockedUsersRequest
import com.cometchat.pro.core.BlockedUsersRequest.BlockedUsersRequestBuilder
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.users.blocked_users.CometChatBlockUserListActivity
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction
import com.cometchat.pro.uikit.ui_settings.UIKitSettings
import com.google.android.material.appbar.MaterialToolbar

class CometChatMorePrivacyActivity constructor() : AppCompatActivity() {
    private var tvBlockUserCount: TextView? = null
    private var blockedUsersRequest: BlockedUsersRequest? = null
    private var blockUserTv: TextView? = null
    private var divider: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cometchat_more_privacy)
        blockUserTv = findViewById(R.id.blocked_user_tv)
        tvBlockUserCount = findViewById(R.id.tv_blocked_user_count)
        val toolbar: MaterialToolbar = findViewById(R.id.privacy_toolbar)
        divider = findViewById(R.id.divider)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        if (UIKitSettings.color != null) window.statusBarColor = Color.parseColor(UIKitSettings.color)
        if (Utils.changeToolbarFont(toolbar) != null) {
            Utils.changeToolbarFont(toolbar)!!.typeface = FontUtils.getInstance(this).getTypeFace(FontUtils.robotoMedium)
        }
        if (Utils.isDarkMode(this)) {
            divider!!.setBackgroundColor(resources.getColor(R.color.grey))
            blockUserTv!!.setTextColor(resources.getColor(R.color.textColorWhite))
        } else {
            divider!!.setBackgroundColor(resources.getColor(R.color.light_grey))
            blockUserTv!!.setTextColor(resources.getColor(R.color.primaryTextColor))
        }
        blockCount
    }

    fun blockUserList(view: View?) {
        startActivity(Intent(this, CometChatBlockUserListActivity::class.java))
    }

    val blockCount: Unit
        get() {
            blockedUsersRequest = BlockedUsersRequestBuilder().setDirection(BlockedUsersRequest.DIRECTION_BLOCKED_BY_ME).setLimit(100).build()
            blockedUsersRequest!!.fetchNext(object : CallbackListener<List<User?>>() {
                public override fun onSuccess(users: List<User?>) {
                    if (users.isEmpty()) {
                        tvBlockUserCount!!.text = ""
                    } else if (users.size < 2) {
                        tvBlockUserCount!!.text = users.size.toString() + " " + getResources().getString(R.string.user)
                    } else {
                        tvBlockUserCount!!.text = users.size.toString() + " " + getResources().getString(R.string.users)
                    }
                }

                public override fun onError(e: CometChatException) {
                    ErrorMessagesUtils.cometChatErrorMessage(this@CometChatMorePrivacyActivity, e.code)
                }
            })
        }

    override fun onResume() {
        super.onResume()
        blockedUsersRequest = null
        blockCount
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) onBackPressed()
        return super.onOptionsItemSelected(item)
    }
}