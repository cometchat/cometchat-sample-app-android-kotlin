package com.cometchat.pro.androiduikit

import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import android.os.Bundle
import android.view.View

import com.google.android.material.card.MaterialCardView

class ComponentListActivity : AppCompatActivity() {

    lateinit var cometchatAvatar: MaterialCardView
    lateinit var cometchatStatusIndicator: MaterialCardView
    lateinit var cometchatBadgeCount: MaterialCardView
    lateinit var cometchatUserList: MaterialCardView
    lateinit var cometchatGroupList: MaterialCardView
    lateinit var cometchatConversationList: MaterialCardView
    lateinit var cometchatCallList: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_component_list)
        cometchatAvatar = findViewById(R.id.cometchat_avatar)
        cometchatAvatar.setOnClickListener { view ->
            val intent = Intent(this@ComponentListActivity, ComponentLoadActivity::class.java)
            intent.putExtra("screen", R.id.cometchat_avatar)
            startActivity(intent)
        }
        cometchatStatusIndicator = findViewById(R.id.cometchat_status_indicator)
        cometchatStatusIndicator.setOnClickListener { view ->
            val intent = Intent(this@ComponentListActivity, ComponentLoadActivity::class.java)
            intent.putExtra("screen", R.id.cometchat_status_indicator)
            startActivity(intent)
        }
        cometchatBadgeCount = findViewById(R.id.cometchat_badge_count)
        cometchatBadgeCount.setOnClickListener { view ->
            val intent = Intent(this@ComponentListActivity, ComponentLoadActivity::class.java)
            intent.putExtra("screen", R.id.cometchat_badge_count)
            startActivity(intent)
        }
        cometchatUserList = findViewById(R.id.cometchat_user_view)
        cometchatUserList.setOnClickListener { view ->
            val intent = Intent(this@ComponentListActivity, ComponentLoadActivity::class.java)
            intent.putExtra("screen", R.id.cometchat_user_view)
            startActivity(intent)
        }
        cometchatGroupList = findViewById(R.id.cometchat_group_view)
        cometchatGroupList.setOnClickListener { view ->
            val intent = Intent(this@ComponentListActivity, ComponentLoadActivity::class.java)
            intent.putExtra("screen", R.id.cometchat_group_view)
            startActivity(intent)
        }
        cometchatConversationList = findViewById(R.id.cometchat_conversation_view)
        cometchatConversationList.setOnClickListener { view ->
            val intent = Intent(this@ComponentListActivity, ComponentLoadActivity::class.java)
            intent.putExtra("screen", R.id.cometchat_conversation_view)
            startActivity(intent)
        }
        cometchatCallList = findViewById(R.id.cometchat_callList)
        cometchatCallList.setOnClickListener { view ->
            val intent = Intent(this@ComponentListActivity, ComponentLoadActivity::class.java)
            intent.putExtra("screen", R.id.cometchat_callList)
            startActivity(intent)
        }
    }

    fun backClick(view: View) {
        super.onBackPressed()
    }
}
