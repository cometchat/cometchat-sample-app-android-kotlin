package com.cometchat.pro.uikit.ui_components.messages.message_list

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
import com.cometchat.pro.uikit.ui_resources.constants.UIKitContracts
import com.cometchat.pro.uikit.ui_components.messages.message_actions.listener.MessageActionCloseListener
import com.cometchat.pro.uikit.ui_components.messages.message_actions.listener.OnMessageLongClick

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
 * @see CometChatMessageList
 */
class CometChatMessageListActivity : AppCompatActivity(), MessageAdapter.OnMessageLongClick {
    private val messageLongClick: OnMessageLongClick? = null
    var fragment: Fragment? = CometChatMessageList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cometchat_message_list)

        val config: EmojiCompat.Config = BundledEmojiCompatConfig(this)
        EmojiCompat.init(config)

        if (intent != null) {
            val bundle = Bundle()
            bundle.putString(UIKitContracts.IntentStrings.AVATAR, intent.getStringExtra(UIKitContracts.IntentStrings.AVATAR))
            bundle.putString(UIKitContracts.IntentStrings.NAME, intent.getStringExtra(UIKitContracts.IntentStrings.NAME))
            bundle.putString(UIKitContracts.IntentStrings.TYPE, intent.getStringExtra(UIKitContracts.IntentStrings.TYPE))
            if (intent.hasExtra(UIKitContracts.IntentStrings.TYPE) && intent.getStringExtra(UIKitContracts.IntentStrings.TYPE) == CometChatConstants.RECEIVER_TYPE_USER) {
                bundle.putString(UIKitContracts.IntentStrings.UID, intent.getStringExtra(UIKitContracts.IntentStrings.UID))
                bundle.putString(UIKitContracts.IntentStrings.STATUS, intent.getStringExtra(UIKitContracts.IntentStrings.STATUS))
            } else {
                bundle.putString(UIKitContracts.IntentStrings.GUID, intent.getStringExtra(UIKitContracts.IntentStrings.GUID))
                bundle.putString(UIKitContracts.IntentStrings.GROUP_OWNER, intent.getStringExtra(UIKitContracts.IntentStrings.GROUP_OWNER))
                bundle.putInt(UIKitContracts.IntentStrings.MEMBER_COUNT, intent.getIntExtra(UIKitContracts.IntentStrings.MEMBER_COUNT, 0))
                bundle.putString(UIKitContracts.IntentStrings.GROUP_TYPE, intent.getStringExtra(UIKitContracts.IntentStrings.GROUP_TYPE))
                bundle.putString(UIKitContracts.IntentStrings.GROUP_DESC, intent.getStringExtra(UIKitContracts.IntentStrings.GROUP_DESC))
                bundle.putString(UIKitContracts.IntentStrings.GROUP_PASSWORD, intent.getStringExtra(UIKitContracts.IntentStrings.GROUP_PASSWORD))
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