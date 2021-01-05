package screen.threadconversation

import adapter.ThreadAdapter
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.uikit.R
import constant.StringContract
import listeners.MessageActionCloseListener
import listeners.OnMessageLongClick
import java.util.*

class CometChatThreadMessageActivity : AppCompatActivity(), ThreadAdapter.OnMessageLongClick {
    var fragment: Fragment = CometChatThreadMessageScreen()
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
            if (intent.hasExtra(StringContract.IntentStrings.MESSAGE_CATEGORY)) messageCategory = intent.getStringExtra(StringContract.IntentStrings.MESSAGE_CATEGORY)
            if (intent.hasExtra(StringContract.IntentStrings.LOCATION_LONGITUDE)) longitude = intent.getDoubleExtra(StringContract.IntentStrings.LOCATION_LONGITUDE, 0.0)
            if (intent.hasExtra(StringContract.IntentStrings.LOCATION_LATITUDE)) latitude = intent.getDoubleExtra(StringContract.IntentStrings.LOCATION_LATITUDE, 0.0)
            if (intent.hasExtra(StringContract.IntentStrings.CONVERSATION_NAME)) conversationName = intent.getStringExtra(StringContract.IntentStrings.CONVERSATION_NAME)
            if (intent.hasExtra(StringContract.IntentStrings.PARENT_ID)) messageId = intent.getIntExtra(StringContract.IntentStrings.PARENT_ID, 0)
            if (intent.hasExtra(StringContract.IntentStrings.REPLY_COUNT)) replyCount = intent.getIntExtra(StringContract.IntentStrings.REPLY_COUNT, 0)
            if (intent.hasExtra(StringContract.IntentStrings.AVATAR)) avatar = intent.getStringExtra(StringContract.IntentStrings.AVATAR)
            if (intent.hasExtra(StringContract.IntentStrings.NAME)) name = intent.getStringExtra(StringContract.IntentStrings.NAME)
            if (intent.hasExtra(StringContract.IntentStrings.MESSAGE_TYPE)) messageType = intent.getStringExtra(StringContract.IntentStrings.MESSAGE_TYPE)
            if (intent.hasExtra(StringContract.IntentStrings.UID)) uid = intent.getStringExtra(StringContract.IntentStrings.UID)
            if (intent.hasExtra(StringContract.IntentStrings.SENTAT)) sentAt = intent.getLongExtra(StringContract.IntentStrings.SENTAT, 0)
            if (intent.hasExtra(StringContract.IntentStrings.TEXTMESSAGE)) message = intent.getStringExtra(StringContract.IntentStrings.TEXTMESSAGE)
            if (intent.hasExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME)) messagefileName = intent.getStringExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME)
            if (intent.hasExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE)) mediaSize = intent.getIntExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, 0)
            if (intent.hasExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL)) mediaUrl = intent.getStringExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL)
            if (intent.hasExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION)) mediaExtension = intent.getStringExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION)
            if (intent.hasExtra(StringContract.IntentStrings.TYPE)) type = intent.getStringExtra(StringContract.IntentStrings.TYPE)
            if (intent.hasExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE)) mediaMime = intent.getStringExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE)

            if (type == CometChatConstants.RECEIVER_TYPE_GROUP) {
                if (intent.hasExtra(StringContract.IntentStrings.GUID)) Id = intent.getStringExtra(StringContract.IntentStrings.GUID)
            } else {
                if (intent.hasExtra(StringContract.IntentStrings.UID)) Id = intent.getStringExtra(StringContract.IntentStrings.UID)
            }
            bundle.putString(StringContract.IntentStrings.MESSAGE_CATEGORY, messageCategory)
            bundle.putString(StringContract.IntentStrings.ID, Id)
            bundle.putString(StringContract.IntentStrings.CONVERSATION_NAME, conversationName)
            bundle.putString(StringContract.IntentStrings.TYPE, type)
            bundle.putString(StringContract.IntentStrings.AVATAR, avatar)
            bundle.putString(StringContract.IntentStrings.NAME, name)
            bundle.putInt(StringContract.IntentStrings.PARENT_ID, messageId)
            bundle.putInt(StringContract.IntentStrings.REPLY_COUNT, replyCount)
            bundle.putString(StringContract.IntentStrings.MESSAGE_TYPE, messageType)
            bundle.putString(StringContract.IntentStrings.UID, uid)
            bundle.putLong(StringContract.IntentStrings.SENTAT, sentAt)
            if (intent.hasExtra(StringContract.IntentStrings.REACTION_INFO)) {
                reactionInfo = intent.getSerializableExtra(StringContract.IntentStrings.REACTION_INFO) as HashMap<String, String>
                bundle.putSerializable(StringContract.IntentStrings.REACTION_INFO, reactionInfo)
            }
            if (messageType == CometChatConstants.MESSAGE_TYPE_TEXT)
                bundle.putString(StringContract.IntentStrings.TEXTMESSAGE, message)
            else if (messageType == StringContract.IntentStrings.LOCATION) {
                bundle.putDouble(StringContract.IntentStrings.LOCATION_LATITUDE, latitude)
                bundle.putDouble(StringContract.IntentStrings.LOCATION_LONGITUDE, longitude)
            } else if (messageType == StringContract.IntentStrings.STICKERS) {
                bundle.putString(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL, mediaUrl)
                bundle.putString(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, messagefileName)
            }
            else {
                bundle.putString(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL, mediaUrl)
                bundle.putString(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, messagefileName)
                bundle.putString(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, mediaExtension)
                bundle.putInt(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, mediaSize)
                bundle.putString(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE, mediaMime)
            }
            fragment.setArguments(bundle)
            supportFragmentManager.beginTransaction().replace(R.id.ChatFragment, fragment).commit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(CometChatThreadMessageActivity.TAG, "onActivityResult: ")
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(CometChatThreadMessageActivity.TAG, "onRequestPermissionsResult: ")
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