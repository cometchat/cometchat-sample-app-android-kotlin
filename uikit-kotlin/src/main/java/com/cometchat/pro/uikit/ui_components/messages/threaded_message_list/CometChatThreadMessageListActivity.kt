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
import com.cometchat.pro.uikit.ui_components.messages.message_actions.listener.MessageActionCloseListener
import com.cometchat.pro.uikit.ui_components.messages.message_actions.listener.OnMessageLongClick
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
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
    private var pollQuestion: String? = null
    private var pollOptions: String? = null
    private var pollResult: ArrayList<String>? = null
    private var voteCount = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cometchat_message_list)

        if (intent != null) {
            val bundle = Bundle()
            if (intent.hasExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY)) messageCategory = intent.getStringExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY)
            if (intent.hasExtra(UIKitConstants.IntentStrings.LOCATION_LONGITUDE)) longitude = intent.getDoubleExtra(UIKitConstants.IntentStrings.LOCATION_LONGITUDE, 0.0)
            if (intent.hasExtra(UIKitConstants.IntentStrings.LOCATION_LATITUDE)) latitude = intent.getDoubleExtra(UIKitConstants.IntentStrings.LOCATION_LATITUDE, 0.0)
            if (intent.hasExtra(UIKitConstants.IntentStrings.CONVERSATION_NAME)) conversationName = intent.getStringExtra(UIKitConstants.IntentStrings.CONVERSATION_NAME)
            if (intent.hasExtra(UIKitConstants.IntentStrings.PARENT_ID)) messageId = intent.getIntExtra(UIKitConstants.IntentStrings.PARENT_ID, 0)
            if (intent.hasExtra(UIKitConstants.IntentStrings.REPLY_COUNT)) replyCount = intent.getIntExtra(UIKitConstants.IntentStrings.REPLY_COUNT, 0)
            if (intent.hasExtra(UIKitConstants.IntentStrings.AVATAR)) avatar = intent.getStringExtra(UIKitConstants.IntentStrings.AVATAR)
            if (intent.hasExtra(UIKitConstants.IntentStrings.NAME)) name = intent.getStringExtra(UIKitConstants.IntentStrings.NAME)
            if (intent.hasExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE)) messageType = intent.getStringExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE)
            if (intent.hasExtra(UIKitConstants.IntentStrings.UID)) uid = intent.getStringExtra(UIKitConstants.IntentStrings.UID)
            if (intent.hasExtra(UIKitConstants.IntentStrings.SENTAT)) sentAt = intent.getLongExtra(UIKitConstants.IntentStrings.SENTAT, 0)
            if (intent.hasExtra(UIKitConstants.IntentStrings.TEXTMESSAGE)) message = intent.getStringExtra(UIKitConstants.IntentStrings.TEXTMESSAGE)
            if (intent.hasExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME)) messagefileName = intent.getStringExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME)
            if (intent.hasExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE)) mediaSize = intent.getIntExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, 0)
            if (intent.hasExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL)) mediaUrl = intent.getStringExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL)
            if (intent.hasExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION)) mediaExtension = intent.getStringExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION)
            if (intent.hasExtra(UIKitConstants.IntentStrings.TYPE)) type = intent.getStringExtra(UIKitConstants.IntentStrings.TYPE)
            if (intent.hasExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE)) mediaMime = intent.getStringExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE)
            if (intent.hasExtra(UIKitConstants.IntentStrings.POLL_QUESTION)) pollQuestion = intent.getStringExtra(UIKitConstants.IntentStrings.POLL_QUESTION)
            if (intent.hasExtra(UIKitConstants.IntentStrings.POLL_OPTION)) pollOptions = intent.getStringExtra(UIKitConstants.IntentStrings.POLL_OPTION)
            if (intent.hasExtra(UIKitConstants.IntentStrings.POLL_RESULT)) pollResult = intent.getStringArrayListExtra(UIKitConstants.IntentStrings.POLL_RESULT)
            if (intent.hasExtra(UIKitConstants.IntentStrings.POLL_VOTE_COUNT)) voteCount = intent.getIntExtra(UIKitConstants.IntentStrings.POLL_VOTE_COUNT, 0)

            if (type == CometChatConstants.RECEIVER_TYPE_GROUP) {
                if (intent.hasExtra(UIKitConstants.IntentStrings.GUID)) Id = intent.getStringExtra(UIKitConstants.IntentStrings.GUID)
            } else {
                if (intent.hasExtra(UIKitConstants.IntentStrings.UID)) Id = intent.getStringExtra(UIKitConstants.IntentStrings.UID)
            }
            bundle.putString(UIKitConstants.IntentStrings.MESSAGE_CATEGORY, messageCategory)
            bundle.putString(UIKitConstants.IntentStrings.ID, Id)
            bundle.putString(UIKitConstants.IntentStrings.CONVERSATION_NAME, conversationName)
            bundle.putString(UIKitConstants.IntentStrings.TYPE, type)
            bundle.putString(UIKitConstants.IntentStrings.AVATAR, avatar)
            bundle.putString(UIKitConstants.IntentStrings.NAME, name)
            bundle.putInt(UIKitConstants.IntentStrings.PARENT_ID, messageId)
            bundle.putInt(UIKitConstants.IntentStrings.REPLY_COUNT, replyCount)
            bundle.putString(UIKitConstants.IntentStrings.MESSAGE_TYPE, messageType)
            bundle.putString(UIKitConstants.IntentStrings.UID, uid)
            bundle.putLong(UIKitConstants.IntentStrings.SENTAT, sentAt)
            if (intent.hasExtra(UIKitConstants.IntentStrings.REACTION_INFO)) {
                reactionInfo = intent.getSerializableExtra(UIKitConstants.IntentStrings.REACTION_INFO) as HashMap<String, String>
                bundle.putSerializable(UIKitConstants.IntentStrings.REACTION_INFO, reactionInfo)
            }
            if (messageType == CometChatConstants.MESSAGE_TYPE_TEXT)
                bundle.putString(UIKitConstants.IntentStrings.TEXTMESSAGE, message)
            else if (messageType == UIKitConstants.IntentStrings.LOCATION) {
                bundle.putDouble(UIKitConstants.IntentStrings.LOCATION_LATITUDE, latitude)
                bundle.putDouble(UIKitConstants.IntentStrings.LOCATION_LONGITUDE, longitude)
            } else if (messageType == UIKitConstants.IntentStrings.STICKERS) {
                bundle.putString(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL, mediaUrl)
                bundle.putString(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, messagefileName)
            } else if (messageType == UIKitConstants.IntentStrings.WHITEBOARD || messageType == UIKitConstants.IntentStrings.WRITEBOARD) {
                bundle.putString(UIKitConstants.IntentStrings.TEXTMESSAGE, message)
            } else if (messageType == UIKitConstants.IntentStrings.POLLS) {
                bundle.putStringArrayList(UIKitConstants.IntentStrings.POLL_RESULT, pollResult)
                bundle.putString(UIKitConstants.IntentStrings.POLL_QUESTION, pollQuestion)
                bundle.putString(UIKitConstants.IntentStrings.POLL_OPTION, pollOptions)
                bundle.putInt(UIKitConstants.IntentStrings.POLL_VOTE_COUNT, voteCount)
            }
            else {
                bundle.putString(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL, mediaUrl)
                bundle.putString(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, messagefileName)
                bundle.putString(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, mediaExtension)
                bundle.putInt(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, mediaSize)
                bundle.putString(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE, mediaMime)
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