package screen.messagelist

import adapter.MessageAdapter
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.fragment.app.Fragment
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.uikit.R
import constant.StringContract
import listeners.MessageActionCloseListener
import listeners.OnMessageLongClick

/**
 *
 * Purpose - CometChatMessageListActivity.class is a Activity used to display messages using CometChatMessageScreen.class. It takes
 * parameter like TYPE to differentiate between User MessageScreen & Group MessageScreen.
 *
 * It passes parameters like UID (userID) ,AVATAR (userAvatar) ,NAME (userName) ,STATUS (userStatus) to CometChatMessageScreen.class
 * if TYPE is CometChatConstant.RECEIVER_TYPE_USER
 *
 * It passes parameters like GUID (groupID) ,AVATAR (groupIcon) ,NAME (groupName) ,GROUP_OWNER (groupOwner) to CometChatMessageScreen.class
 * if TYPE is CometChatConstant.RECEIVER_TYPE_GROUP
 *
 * @see CometChatConstants
 * @see CometChatMessageScreen
 */
class CometChatMessageListActivity : AppCompatActivity(), MessageAdapter.OnMessageLongClick {
    private val messageLongClick: OnMessageLongClick? = null
    var fragment: Fragment? = CometChatMessageScreen()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cometchat_message_list)

        val config: EmojiCompat.Config = BundledEmojiCompatConfig(this)
        EmojiCompat.init(config)

        if (intent != null) {
            val bundle = Bundle()
            bundle.putString(StringContract.IntentStrings.AVATAR, intent.getStringExtra(StringContract.IntentStrings.AVATAR))
            bundle.putString(StringContract.IntentStrings.NAME, intent.getStringExtra(StringContract.IntentStrings.NAME))
            bundle.putString(StringContract.IntentStrings.TYPE, intent.getStringExtra(StringContract.IntentStrings.TYPE))
            if (intent.hasExtra(StringContract.IntentStrings.TYPE) && intent.getStringExtra(StringContract.IntentStrings.TYPE) == CometChatConstants.RECEIVER_TYPE_USER) {
                bundle.putString(StringContract.IntentStrings.UID, intent.getStringExtra(StringContract.IntentStrings.UID))
                bundle.putString(StringContract.IntentStrings.STATUS, intent.getStringExtra(StringContract.IntentStrings.STATUS))
            } else {
                bundle.putString(StringContract.IntentStrings.GUID, intent.getStringExtra(StringContract.IntentStrings.GUID))
                bundle.putString(StringContract.IntentStrings.GROUP_OWNER, intent.getStringExtra(StringContract.IntentStrings.GROUP_OWNER))
                bundle.putInt(StringContract.IntentStrings.MEMBER_COUNT, intent.getIntExtra(StringContract.IntentStrings.MEMBER_COUNT, 0))
                bundle.putString(StringContract.IntentStrings.GROUP_TYPE, intent.getStringExtra(StringContract.IntentStrings.GROUP_TYPE))
                bundle.putString(StringContract.IntentStrings.GROUP_DESC, intent.getStringExtra(StringContract.IntentStrings.GROUP_DESC))
                bundle.putString(StringContract.IntentStrings.GROUP_PASSWORD, intent.getStringExtra(StringContract.IntentStrings.GROUP_PASSWORD))
            }
            fragment!!.arguments = bundle
            supportFragmentManager.beginTransaction().replace(R.id.ChatFragment, fragment!!).commit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: ")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult: ")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun setLongMessageClick(baseMessage: List<BaseMessage>?) {
        if (fragment != null) (fragment as OnMessageLongClick).setLongMessageClick(baseMessage)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    public fun handleDialogClose(dialog: DialogInterface) {
        if (fragment != null)
            (fragment as MessageActionCloseListener).handleDialogClose(dialog)
        dialog.dismiss()
    }

    companion object {
        private const val TAG = "CometChatMessageListAct"
    }
}