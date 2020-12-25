package screen.blockuserlist

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cometchat.pro.uikit.R

class CometChatBlockUserListScreenActivity : AppCompatActivity() {
    private val fragment: Fragment? = null
    private val guid: String? = null
    private val loggedInUserScope: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_screen)
        val fragment: Fragment = CometChatBlockUserListScreen()
        supportFragmentManager.beginTransaction().replace(R.id.frame_fragment, fragment).commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
}