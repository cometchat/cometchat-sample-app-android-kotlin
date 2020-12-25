package screen.adminAndModeratorList

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cometchat.pro.uikit.R
import constant.StringContract

class CometChatAdminModeratorListScreenActivity : AppCompatActivity() {
    private var guid: String? = null
    private var ownerId: String? = null
    private var showModerator = false
    private var loggedInUserScope: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_screen)
        handleIntent()
    }

    private fun handleIntent() {
        if (intent.hasExtra(StringContract.IntentStrings.MEMBER_SCOPE)) {
            loggedInUserScope = intent.getStringExtra(StringContract.IntentStrings.MEMBER_SCOPE)
        }
        if (intent.hasExtra(StringContract.IntentStrings.GUID)) {
            guid = intent.getStringExtra(StringContract.IntentStrings.GUID)
        }
        if (intent.hasExtra(StringContract.IntentStrings.GROUP_OWNER)) {
            ownerId = intent.getStringExtra(StringContract.IntentStrings.GROUP_OWNER)
        }
        if (intent.hasExtra(StringContract.IntentStrings.SHOW_MODERATORLIST)) {
            showModerator = intent.getBooleanExtra(StringContract.IntentStrings.SHOW_MODERATORLIST, false)
        }
        val fragment: Fragment = CometChatAdminModeratorListScreen()
        val bundle = Bundle()
        bundle.putString(StringContract.IntentStrings.GUID, guid)
        bundle.putString(StringContract.IntentStrings.GROUP_OWNER, ownerId)
        bundle.putString(StringContract.IntentStrings.MEMBER_SCOPE, loggedInUserScope)
        bundle.putBoolean(StringContract.IntentStrings.SHOW_MODERATORLIST, showModerator)
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