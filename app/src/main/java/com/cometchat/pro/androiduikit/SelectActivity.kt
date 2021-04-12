package com.cometchat.pro.androiduikit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.uikit.ui_components.cometchat_ui.CometChatUI
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.cometchat.pro.uikit.ui_components.calls.call_manager.listener.CometChatCallListener.makeCall
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils

class SelectActivity : AppCompatActivity() {
    private var screenGroup: RadioGroup? = null
    private var callGroup: RadioGroup? = null
    private var userRb: RadioButton? = null
    private var conversationRb: RadioButton? = null
    private var groupRb: RadioButton? = null
    private var moreInfoRb: RadioButton? = null
    private var audioCallRb: RadioButton? = null
    private var videoCallRb: RadioButton? = null
    private var callsRb: RadioButton? = null
    private var logout: MaterialButton? = null
    private var unifiedLaunch: MaterialButton? = null
    private var screenLaunch: MaterialButton? = null
    private var componentLaunch: MaterialButton? = null
    private val directIntentFront: CardView? = null
    private val directIntentBack: CardView? = null
    private val usingScreenFront: CardView? = null
    private val usingScreenBack: CardView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)
        logout = findViewById(R.id.logout)
        unifiedLaunch = findViewById(R.id.directLaunch)
        screenLaunch = findViewById(R.id.fragmentlaunch)
        componentLaunch = findViewById(R.id.componentLaunch)
        logout!!.setOnClickListener(View.OnClickListener { v -> logoutUser(v) })
        if (Utils.isDarkMode(this)) {
            logout!!.setBackgroundColor(resources.getColor(R.color.darkModeBackground))
        } else {
            logout!!.setBackgroundColor(resources.getColor(R.color.textColorWhite))
        }
        unifiedLaunch!!.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@SelectActivity, CometChatUI::class.java))
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        })
        componentLaunch!!.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@SelectActivity, ComponentListActivity::class.java))
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        })
        screenGroup = findViewById<View>(R.id.screen_selector) as RadioGroup
        callGroup = findViewById(R.id.call_selector)
        audioCallRb = findViewById(R.id.audioCall)
        videoCallRb = findViewById(R.id.videoCall)
        callsRb = findViewById(R.id.calls)
        userRb = findViewById<View>(R.id.users) as RadioButton
        groupRb = findViewById<View>(R.id.groups) as RadioButton
        conversationRb = findViewById<View>(R.id.conversations) as RadioButton
        moreInfoRb = findViewById<View>(R.id.moreinfo) as RadioButton
        screenGroup!!.setOnCheckedChangeListener(screenListner)
        callGroup!!.setOnCheckedChangeListener(callListener)
        screenLaunch!!.setOnClickListener(View.OnClickListener { view ->
            val id1 = screenGroup!!.checkedRadioButtonId
            val id2 = callGroup!!.checkedRadioButtonId
            if (id1 < 0 && id2 < 0) {
                Snackbar.make(view, "Select any one screen.", Snackbar.LENGTH_LONG).show()
            } else if (id2 < 0) {
                val intent = Intent(this@SelectActivity, ComponentLoadActivity::class.java)
                intent.putExtra("screen", id1)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
            } else if (id1 < 0) {
                val type: String
                type = if (audioCallRb!!.isChecked) {
                    CometChatConstants.CALL_TYPE_AUDIO
                } else {
                    CometChatConstants.CALL_TYPE_VIDEO
                }
                makeCall(this@SelectActivity, "superhero5", CometChatConstants.RECEIVER_TYPE_USER, type)
            }
        })
    }

    private val callListener : RadioGroup.OnCheckedChangeListener? = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        if (checkedId != -1) {
            screenGroup!!.setOnCheckedChangeListener(null)
            screenGroup!!.clearCheck()
            screenGroup!!.setOnCheckedChangeListener(screenListner)
            screenLaunch!!.text = "Make Call"
        }
        if (audioCallRb!!.isChecked) {
            if (Utils.isDarkMode(this@SelectActivity)) audioCallRb!!.background = resources.getDrawable(R.drawable.darkmode_radiobuttonbackground) else audioCallRb!!.background = resources.getDrawable(R.drawable.radiobuttonbackground)
            conversationRb!!.background = null
            videoCallRb!!.background = null
            userRb!!.background = null
            callsRb!!.background = null
            groupRb!!.background = null
            moreInfoRb!!.background = null
        } else if (videoCallRb!!.isChecked) {
            if (Utils.isDarkMode(this@SelectActivity)) videoCallRb!!.background = resources.getDrawable(R.drawable.darkmode_radiobuttonbackground) else videoCallRb!!.background = resources.getDrawable(R.drawable.radiobuttonbackground)
            conversationRb!!.background = null
            audioCallRb!!.background = null
            userRb!!.background = null
            callsRb!!.background = null
            groupRb!!.background = null
            moreInfoRb!!.background = null
        }
    }
    private val screenListner :RadioGroup.OnCheckedChangeListener? = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        if (checkedId != -1) {
            callGroup!!.setOnCheckedChangeListener(null)
            callGroup!!.clearCheck()
            callGroup!!.setOnCheckedChangeListener(callListener)
            screenLaunch!!.text = "Navigate"
        }
        if (userRb!!.isChecked) {
            if (Utils.isDarkMode(this@SelectActivity)) userRb!!.background = resources.getDrawable(R.drawable.darkmode_radiobuttonbackground) else userRb!!.background = resources.getDrawable(R.drawable.radiobuttonbackground)
            groupRb!!.background = null
            callsRb!!.background = null
            conversationRb!!.background = null
            moreInfoRb!!.background = null
            audioCallRb!!.background = null
            videoCallRb!!.background = null
        } else if (callsRb!!.isChecked) {
            if (Utils.isDarkMode(this@SelectActivity)) callsRb!!.background = resources.getDrawable(R.drawable.darkmode_radiobuttonbackground) else callsRb!!.background = resources.getDrawable(R.drawable.radiobuttonbackground)
            userRb!!.background = null
            groupRb!!.background = null
            conversationRb!!.background = null
            moreInfoRb!!.background = null
            audioCallRb!!.background = null
            videoCallRb!!.background = null
        } else if (conversationRb!!.isChecked) {
            if (Utils.isDarkMode(this@SelectActivity)) conversationRb!!.background = resources.getDrawable(R.drawable.darkmode_radiobuttonbackground) else conversationRb!!.background = resources.getDrawable(R.drawable.radiobuttonbackground)
            userRb!!.background = null
            callsRb!!.background = null
            groupRb!!.background = null
            moreInfoRb!!.background = null
            audioCallRb!!.background = null
            videoCallRb!!.background = null
        } else if (groupRb!!.isChecked) {
            if (Utils.isDarkMode(this@SelectActivity)) groupRb!!.background = resources.getDrawable(R.drawable.darkmode_radiobuttonbackground) else groupRb!!.background = resources.getDrawable(R.drawable.radiobuttonbackground)
            userRb!!.background = null
            conversationRb!!.background = null
            moreInfoRb!!.background = null
            audioCallRb!!.background = null
            videoCallRb!!.background = null
            callsRb!!.background = null
        } else {
            if (Utils.isDarkMode(this@SelectActivity)) moreInfoRb!!.background = resources.getDrawable(R.drawable.darkmode_radiobuttonbackground) else moreInfoRb!!.background = resources.getDrawable(R.drawable.radiobuttonbackground)
            userRb!!.background = null
            groupRb!!.background = null
            conversationRb!!.background = null
            audioCallRb!!.background = null
            videoCallRb!!.background = null
            callsRb!!.background = null
        }
    }

    private fun logoutUser(view: View) {
        CometChat.logout(object : CallbackListener<String?>() {
            override fun onSuccess(s: String?) {
                startActivity(Intent(this@SelectActivity, MainActivity::class.java))
            }

            override fun onError(e: CometChatException) {
                ErrorMessagesUtils.cometChatErrorMessage(this@SelectActivity, e.code)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (CometChat.getLoggedInUser() == null) {
            startActivity(Intent(this@SelectActivity, MainActivity::class.java))
        }
    }
}