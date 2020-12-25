package screen

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.pro.core.BlockedUsersRequest
import com.cometchat.pro.core.BlockedUsersRequest.BlockedUsersRequestBuilder
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import screen.blockuserlist.CometChatBlockUserListScreenActivity
import utils.FontUtils
import utils.Utils

class CometChatMorePrivacyScreenActivity constructor() : AppCompatActivity() {
    private var tvBlockUserCount: TextView? = null
    private var blockedUsersRequest: BlockedUsersRequest? = null
    private var blockUserTv: TextView? = null
    private var divider: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comet_chat_more_privacy_screen)
        blockUserTv = findViewById(R.id.blocked_user_tv)
        tvBlockUserCount = findViewById(R.id.tv_blocked_user_count)
        val toolbar: MaterialToolbar = findViewById(R.id.privacy_toolbar)
        divider = findViewById(R.id.divider)
        setSupportActionBar(toolbar)
        if (getSupportActionBar() != null) getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        if (Utils.changeToolbarFont(toolbar) != null) {
            Utils.changeToolbarFont(toolbar)!!.setTypeface(FontUtils.getInstance(this).getTypeFace(FontUtils.robotoMedium))
        }
        if (Utils.isDarkMode(this)) {
            divider!!.setBackgroundColor(getResources().getColor(R.color.grey))
            blockUserTv!!.setTextColor(getResources().getColor(R.color.textColorWhite))
        } else {
            divider!!.setBackgroundColor(getResources().getColor(R.color.light_grey))
            blockUserTv!!.setTextColor(getResources().getColor(R.color.primaryTextColor))
        }
        blockCount
    }

    fun blockUserList(view: View?) {
        startActivity(Intent(this, CometChatBlockUserListScreenActivity::class.java))
    }

    val blockCount: Unit
        get() {
            blockedUsersRequest = BlockedUsersRequestBuilder().setDirection(BlockedUsersRequest.DIRECTION_BLOCKED_BY_ME).setLimit(100).build()
            blockedUsersRequest!!.fetchNext(object : CallbackListener<List<User?>>() {
                public override fun onSuccess(users: List<User?>) {
                    if (users.size == 0) {
                        tvBlockUserCount!!.setText("")
                    } else if (users.size < 2) {
                        tvBlockUserCount!!.setText(users.size.toString() + " " + getResources().getString(R.string.user))
                    } else {
                        tvBlockUserCount!!.setText(users.size.toString() + " " + getResources().getString(R.string.users))
                    }
                }

                public override fun onError(e: CometChatException) {
                    Snackbar.make((tvBlockUserCount)!!, getResources().getString(R.string.blocked_list_error), Snackbar.LENGTH_SHORT).show()
                    Toast.makeText(this@CometChatMorePrivacyScreenActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            })
        }

    override fun onResume() {
        super.onResume()
        blockedUsersRequest = null
        blockCount
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() == android.R.id.home) onBackPressed()
        return super.onOptionsItemSelected(item)
    }
}