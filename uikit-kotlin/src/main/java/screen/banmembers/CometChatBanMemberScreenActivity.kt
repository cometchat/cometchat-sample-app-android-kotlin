package screen.banmembers

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.pro.uikit.R
import com.google.android.material.appbar.MaterialToolbar
import constant.StringContract

class CometChatBanMemberScreenActivity : AppCompatActivity() {
    private var guid: String? = null
    private var gName: String? = null
    private var loggedInUserScope: String? = null
    private var banToolbar: MaterialToolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comet_chat_ban_member_screen)
        banToolbar = findViewById(R.id.banToolbar)
        banToolbar!!.setNavigationOnClickListener(View.OnClickListener { onBackPressed() })
        handleIntent()
        val banFragment = CometChatBanMemberScreen()
        val bundle = Bundle()
        bundle.putString(StringContract.IntentStrings.GUID, guid)
        bundle.putString(StringContract.IntentStrings.GROUP_NAME, gName)
        bundle.putString(StringContract.IntentStrings.MEMBER_SCOPE, loggedInUserScope)
        banFragment.arguments = bundle
        supportFragmentManager.beginTransaction().add(R.id.ban_member_frame, banFragment).commit()
    }

    private fun handleIntent() {
        if (intent.hasExtra(StringContract.IntentStrings.GUID)) {
            guid = intent.getStringExtra(StringContract.IntentStrings.GUID)
        }
        if (intent.hasExtra(StringContract.IntentStrings.GROUP_NAME)) {
            gName = intent.getStringExtra(StringContract.IntentStrings.GROUP_NAME)
            banToolbar!!.title = String.format(resources.getString(R.string.ban_member_of_group), gName)
        }
        if (intent.hasExtra(StringContract.IntentStrings.MEMBER_SCOPE)) {
            loggedInUserScope = intent.getStringExtra(StringContract.IntentStrings.MEMBER_SCOPE)
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