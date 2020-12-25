package screen.groupmemberlist

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cometchat.pro.uikit.R
import constant.StringContract

class CometChatGroupMemberListScreenActivity : AppCompatActivity() {
    private var guid: String? = null
    private var showModerators = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_screen)
        guid = intent.getStringExtra(StringContract.IntentStrings.GUID)
        showModerators = intent.getBooleanExtra(StringContract.IntentStrings.SHOW_MODERATORLIST, showModerators)
        val fragment: Fragment = CometChatGroupMemberListScreen()
        val bundle = Bundle()
        bundle.putString(StringContract.IntentStrings.GUID, guid)
        bundle.putBoolean(StringContract.IntentStrings.SHOW_MODERATORLIST, showModerators)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.frame_fragment, fragment).commit()
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