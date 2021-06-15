package com.cometchat.pro.uikit.ui_components.messages.message_list

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
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
import com.cometchat.pro.uikit.ui_components.cometchat_ui.CometChatUI
import com.cometchat.pro.uikit.ui_components.messages.message_actions.listener.MessageActionCloseListener
import com.cometchat.pro.uikit.ui_components.messages.message_actions.listener.OnMessageLongClick
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_settings.UIKitSettings

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

        if (UIKitSettings.color != null)
            window.statusBarColor = Color.parseColor(UIKitSettings.color)


        val config: EmojiCompat.Config = BundledEmojiCompatConfig(this)
        EmojiCompat.init(config)

        if (intent != null) {
            val bundle = Bundle()
            bundle.putString(UIKitConstants.IntentStrings.AVATAR, intent.getStringExtra(UIKitConstants.IntentStrings.AVATAR))
            bundle.putString(UIKitConstants.IntentStrings.NAME, intent.getStringExtra(UIKitConstants.IntentStrings.NAME))
            bundle.putString(UIKitConstants.IntentStrings.TYPE, intent.getStringExtra(UIKitConstants.IntentStrings.TYPE))
            if (intent.hasExtra(UIKitConstants.IntentStrings.TYPE) && intent.getStringExtra(UIKitConstants.IntentStrings.TYPE) == CometChatConstants.RECEIVER_TYPE_USER) {
                bundle.putString(UIKitConstants.IntentStrings.UID, intent.getStringExtra(UIKitConstants.IntentStrings.UID))
                bundle.putString(UIKitConstants.IntentStrings.STATUS, intent.getStringExtra(UIKitConstants.IntentStrings.STATUS))
                bundle.putString(UIKitConstants.IntentStrings.LINK, intent.getStringExtra(UIKitConstants.IntentStrings.LINK))
                bundle.putBoolean("isReply", intent.getBooleanExtra("isReply", false))
                bundle.putString("baseMessageMetadata", intent.getStringExtra("baseMessageMetadata"))
            } else {
                bundle.putString(UIKitConstants.IntentStrings.GUID, intent.getStringExtra(UIKitConstants.IntentStrings.GUID))
                bundle.putString(UIKitConstants.IntentStrings.GROUP_OWNER, intent.getStringExtra(UIKitConstants.IntentStrings.GROUP_OWNER))
                bundle.putInt(UIKitConstants.IntentStrings.MEMBER_COUNT, intent.getIntExtra(UIKitConstants.IntentStrings.MEMBER_COUNT, 0))
                bundle.putString(UIKitConstants.IntentStrings.GROUP_TYPE, intent.getStringExtra(UIKitConstants.IntentStrings.GROUP_TYPE))
                bundle.putString(UIKitConstants.IntentStrings.GROUP_DESC, intent.getStringExtra(UIKitConstants.IntentStrings.GROUP_DESC))
                bundle.putString(UIKitConstants.IntentStrings.GROUP_PASSWORD, intent.getStringExtra(UIKitConstants.IntentStrings.GROUP_PASSWORD))
            }
            fragment!!.arguments = bundle
            supportFragmentManager.beginTransaction().replace(R.id.ChatFragment, fragment!!).commit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: " + requestCode + " " + resultCode + " " + data + " " + data?.data)
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
        startActivity(Intent(this, CometChatUI::class.java))
        finish()
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