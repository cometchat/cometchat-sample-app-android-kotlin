package com.cometchat.pro.uikit.ui_components.messages.threaded_message_list

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_resources.constants.UIKitContracts
import com.cometchat.pro.uikit.ui_components.messages.message_actions.listener.MessageActionCloseListener
import com.cometchat.pro.uikit.ui_components.messages.message_actions.listener.OnMessageLongClick
import java.util.*

class CometChatThreadMessageListActivity : AppCompatActivity(), ThreadAdapter.OnMessageLongClick {
    var fragment: Fragment = CometChatThreadMessageList()
    private var avatar: String? = null
    private var name: String? = null
    private var uid: String? = null
    private var messageType: String? = null
    private var message: String? = null
    private var messagefileName: String? = null
    private var mediaUrl: String? = null
    private var mediaExtension: String? = null
    private var messageId = 0
    private var mediaSize = 0
    private var mediaMime: String? = null
    private var type: String? = null
    private var Id: String? = null
    private var sentAt: Long = 0
    private var messageCategory: String? = null
    private var latitude = 0.0
    private var longitude = 0.0
    private var replyCount = 0
    private var conversationName: String? = null
    private val baseMessage: String? = null
    private lateinit var reactionInfo: HashMap<String, String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cometchat_message_list)

        if (intent != null) {
            val bundle = Bundle()
            if (intent.hasExtra(UIKitContracts.IntentStrings.MESSAGE_CATEGORY)) messageCategory = intent.getStringExtra(UIKitContracts.IntentStrings.MESSAGE_CATEGORY)
            if (intent.hasExtra(UIKitContracts.IntentStrings.LOCATION_LONGITUDE)) longitude = intent.getDoubleExtra(UIKitContracts.IntentStrings.LOCATION_LONGITUDE, 0.0)
            if (intent.hasExtra(UIKitContracts.IntentStrings.LOCATION_LATITUDE)) latitude = intent.getDoubleExtra(UIKitContracts.IntentStrings.LOCATION_LATITUDE, 0.0)
            if (intent.hasExtra(UIKitContracts.IntentStrings.CONVERSATION_NAME)) conversationName = intent.getStringExtra(UIKitContracts.IntentStrings.CONVERSATION_NAME)
            if (intent.hasExtra(UIKitContracts.IntentStrings.PARENT_ID)) messageId = intent.getIntExtra(UIKitContracts.IntentStrings.PARENT_ID, 0)
            if (intent.hasExtra(UIKitContracts.IntentStrings.REPLY_COUNT)) replyCount = intent.getIntExtra(UIKitContracts.IntentStrings.REPLY_COUNT, 0)
            if (intent.hasExtra(UIKitContracts.IntentStrings.AVATAR)) avatar = intent.getStringExtra(UIKitContracts.IntentStrings.AVATAR)
            if (intent.hasExtra(UIKitContracts.IntentStrings.NAME)) name = intent.getStringExtra(UIKitContracts.IntentStrings.NAME)
            if (intent.hasExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE)) messageType = intent.getStringExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE)
            if (intent.hasExtra(UIKitContracts.IntentStrings.UID)) uid = intent.getStringExtra(UIKitContracts.IntentStrings.UID)
            if (intent.hasExtra(UIKitContracts.IntentStrings.SENTAT)) sentAt = intent.getLongExtra(UIKitContracts.IntentStrings.SENTAT, 0)
            if (intent.hasExtra(UIKitContracts.IntentStrings.TEXTMESSAGE)) message = intent.getStringExtra(UIKitContracts.IntentStrings.TEXTMESSAGE)
            if (intent.hasExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_NAME)) messagefileName = intent.getStringExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_NAME)
            if (intent.hasExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE)) mediaSize = intent.getIntExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, 0)
            if (intent.hasExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_URL)) mediaUrl = intent.getStringExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_URL)
            if (intent.hasExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION)) mediaExtension = intent.getStringExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION)
            if (intent.hasExtra(UIKitContracts.IntentStrings.TYPE)) type = intent.getStringExtra(UIKitContracts.IntentStrings.TYPE)
            if (intent.hasExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE)) mediaMime = intent.getStringExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE)

            if (type == CometChatConstants.RECEIVER_TYPE_GROUP) {
                if (intent.hasExtra(UIKitContracts.IntentStrings.GUID)) Id = intent.getStringExtra(UIKitContracts.IntentStrings.GUID)
            } else {
                if (intent.hasExtra(UIKitContracts.IntentStrings.UID)) Id = intent.getStringExtra(UIKitContracts.IntentStrings.UID)
            }
            bundle.putString(UIKitContracts.IntentStrings.MESSAGE_CATEGORY, messageCategory)
            bundle.putString(UIKitContracts.IntentStrings.ID, Id)
            bundle.putString(UIKitContracts.IntentStrings.CONVERSATION_NAME, conversationName)
            bundle.putString(UIKitContracts.IntentStrings.TYPE, type)
            bundle.putString(UIKitContracts.IntentStrings.AVATAR, avatar)
            bundle.putString(UIKitContracts.IntentStrings.NAME, name)
            bundle.putInt(UIKitContracts.IntentStrings.PARENT_ID, messageId)
            bundle.putInt(UIKitContracts.IntentStrings.REPLY_COUNT, replyCount)
            bundle.putString(UIKitContracts.IntentStrings.MESSAGE_TYPE, messageType)
            bundle.putString(UIKitContracts.IntentStrings.UID, uid)
            bundle.putLong(UIKitContracts.IntentStrings.SENTAT, sentAt)
            if (intent.hasExtra(UIKitContracts.IntentStrings.REACTION_INFO)) {
                reactionInfo = intent.getSerializableExtra(UIKitContracts.IntentStrings.REACTION_INFO) as HashMap<String, String>
                bundle.putSerializable(UIKitContracts.IntentStrings.REACTION_INFO, reactionInfo)
            }
            if (messageType == CometChatConstants.MESSAGE_TYPE_TEXT)
                bundle.putString(UIKitContracts.IntentStrings.TEXTMESSAGE, message)
            else if (messageType == UIKitContracts.IntentStrings.LOCATION) {
                bundle.putDouble(UIKitContracts.IntentStrings.LOCATION_LATITUDE, latitude)
                bundle.putDouble(UIKitContracts.IntentStrings.LOCATION_LONGITUDE, longitude)
            } else if (messageType == UIKitContracts.IntentStrings.STICKERS) {
                bundle.putString(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_URL, mediaUrl)
                bundle.putString(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, messagefileName)
            } else if (messageType == UIKitContracts.IntentStrings.WHITEBOARD || messageType == UIKitContracts.IntentStrings.WRITEBOARD) {
                bundle.putString(UIKitContracts.IntentStrings.TEXTMESSAGE, message)
            }
            else {
                bundle.putString(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_URL, mediaUrl)
                bundle.putString(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, messagefileName)
                bundle.putString(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, mediaExtension)
                bundle.putInt(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, mediaSize)
                bundle.putString(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE, mediaMime)
            }
            fragment.arguments = bundle
            supportFragmentManager.beginTransaction().replace(R.id.ChatFragment, fragment).commit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: ")
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult: ")
    }

    override fun setLongMessageClick(baseMessage: List<BaseMessage>?) {
        if (fragment != null) (fragment as OnMessageLongClick).setLongMessageClick(baseMessage!!)
    }

    fun handleDialogClose(dialog: DialogInterface) {
        (fragment as MessageActionCloseListener).handleDialogClose(dialog)
        dialog.dismiss()
    }

    companion object{
        private const val TAG = "CometChatMessageListAct"
    }

}