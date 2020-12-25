package screen.addmember

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cometchat.pro.uikit.R
import constant.StringContract

class CometChatAddMemberScreenActivity : AppCompatActivity() {
    private var fragment: Fragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_screen)
        handleIntent()
    }

    private fun handleIntent() {
        if (intent != null) {
            val bundle = Bundle()
            fragment = CometChatAddMemberScreen()
            bundle.putString(StringContract.IntentStrings.GUID, intent.getStringExtra(StringContract.IntentStrings.GUID))
            bundle.putString(StringContract.IntentStrings.GROUP_NAME, intent.getStringExtra(StringContract.IntentStrings.GROUP_NAME))
            bundle.putStringArrayList(StringContract.IntentStrings.GROUP_MEMBER, intent.getStringArrayListExtra(StringContract.IntentStrings.GROUP_MEMBER))
            fragment!!.setArguments(bundle)
            supportFragmentManager.beginTransaction().replace(R.id.frame_fragment, fragment!!).commit()
        }
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