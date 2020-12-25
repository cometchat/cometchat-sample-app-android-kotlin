package com.cometchat.pro.androiduikit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class ComponentListActivity : AppCompatActivity() {
    var cometchatAvatar: MaterialCardView? = null
    var cometchatStatusIndicator: MaterialCardView? = null
    var cometchatBadgeCount: MaterialCardView? = null
    var cometchatUserList: MaterialCardView? = null
    var cometchatGroupList: MaterialCardView? = null
    var cometchatConversationList: MaterialCardView? = null
    var cometchatCallList: MaterialCardView? = null
    var backIcon: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_component_list)
        backIcon = findViewById(R.id.backIcon)
        backIcon!!.setOnClickListener(View.OnClickListener { onBackPressed() })
        cometchatAvatar = findViewById(R.id.cometchat_avatar)
        cometchatAvatar!!.setOnClickListener(View.OnClickListener { view: View? ->
            val intent = Intent(this@ComponentListActivity, ComponentLoadActivity::class.java)
            intent.putExtra("screen", R.id.cometchat_avatar)
            startActivity(intent)
        })
        cometchatStatusIndicator = findViewById(R.id.cometchat_status_indicator)
        cometchatStatusIndicator!!.setOnClickListener(View.OnClickListener { view: View? ->
            val intent = Intent(this@ComponentListActivity, ComponentLoadActivity::class.java)
            intent.putExtra("screen", R.id.cometchat_status_indicator)
            startActivity(intent)
        })
        cometchatBadgeCount = findViewById(R.id.cometchat_badge_count)
        cometchatBadgeCount!!.setOnClickListener(View.OnClickListener { view: View? ->
            val intent = Intent(this@ComponentListActivity, ComponentLoadActivity::class.java)
            intent.putExtra("screen", R.id.cometchat_badge_count)
            startActivity(intent)
        })
        cometchatUserList = findViewById(R.id.cometchat_user_view)
        cometchatUserList!!.setOnClickListener(View.OnClickListener { view: View? ->
            val intent = Intent(this@ComponentListActivity, ComponentLoadActivity::class.java)
            intent.putExtra("screen", R.id.cometchat_user_view)
            startActivity(intent)
        })
        cometchatGroupList = findViewById(R.id.cometchat_group_view)
        cometchatGroupList!!.setOnClickListener(View.OnClickListener { view: View? ->
            val intent = Intent(this@ComponentListActivity, ComponentLoadActivity::class.java)
            intent.putExtra("screen", R.id.cometchat_group_view)
            startActivity(intent)
        })
        cometchatConversationList = findViewById(R.id.cometchat_conversation_view)
        cometchatConversationList!!.setOnClickListener(View.OnClickListener { view: View? ->
            val intent = Intent(this@ComponentListActivity, ComponentLoadActivity::class.java)
            intent.putExtra("screen", R.id.cometchat_conversation_view)
            startActivity(intent)
        })
        cometchatCallList = findViewById(R.id.cometchat_callList)
        cometchatCallList!!.setOnClickListener(View.OnClickListener { view: View? ->
            val intent = Intent(this@ComponentListActivity, ComponentLoadActivity::class.java)
            intent.putExtra("screen", R.id.cometchat_callList)
            startActivity(intent)
        })
    }
}