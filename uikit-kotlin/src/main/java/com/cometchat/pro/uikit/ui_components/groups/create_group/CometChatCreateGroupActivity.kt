package com.cometchat.pro.uikit.ui_components.groups.create_group

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction
import com.cometchat.pro.uikit.ui_settings.UIKitSettings

class CometChatCreateGroupActivity : AppCompatActivity() {
    private val fragment: Fragment? = null
    private val guid: String? = null
    private val loggedInUserScope: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen)
        val fragment: Fragment = CometChatCreateGroup()
        supportFragmentManager.beginTransaction().replace(R.id.frame_fragment, fragment).commit()
        if (UIKitSettings.color != null) window.statusBarColor = Color.parseColor(UIKitSettings.color)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
}