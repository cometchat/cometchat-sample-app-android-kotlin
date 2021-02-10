package com.cometchat.pro.uikit.ui_components.messages.forward_message

import com.cometchat.pro.uikit.ui_components.shared.cometchatConversations.CometChatConversationsAdapter
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.core.ConversationsRequest
import com.cometchat.pro.core.ConversationsRequest.ConversationsRequestBuilder
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.*
import com.cometchat.pro.uikit.ui_components.shared.cometchatConversations.CometChatConversation
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.cometchat_ui.CometChatUnified
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.cometchat.pro.uikit.ui_resources.constants.UIKitContracts
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener
import org.json.JSONException
import org.json.JSONObject
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.MediaUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import java.util.*

/**
 * Purpose - CometChatForwardMessageScreenActivity class is a fragment used to display list of users to which
 * we will forward the message.
 * Created on - 20th December 2019
 *
 * Modified on  - 16th January 2020
 */
class CometChatForwardMessageActivity : AppCompatActivity() {
    private var rvConversation: CometChatConversation? = null
    private val userList: HashMap<String, Conversation>? = HashMap()
    private var cometChatConversationsAdapter: CometChatConversationsAdapter? = null
    private var conversationsRequest: ConversationsRequest? = null
    private var etSearch: EditText? = null
    private var clearSearch: ImageView? = null
    private var name: String? = null
    private var avatar: String? = null
    private var forwardBtn: MaterialButton? = null
    private var selectedUsers: ChipGroup? = null
    private var textMessage = ""
    private var fontUtils: FontUtils? = null
    private var messageType: String? = null
    private var mediaMessageUrl: String? = null
    private var mediaMessageExtension: String? = null
    private var mediaMessageName: String? = null
    private var mediaMessageMime: String? = null
    private var mediaMessageSize = 0
    private var id = 0
    private var lat = 0.0
    private var lon = 0.0
    private var messageCategory = CometChatConstants.CATEGORY_MESSAGE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cometchat_forward_message)
        fontUtils = FontUtils.getInstance(this)
        handleIntent()
        init()
    }

    fun handleSendText(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText != null) {
            messageType = CometChatConstants.MESSAGE_TYPE_TEXT
            textMessage = sharedText
        }
    }

    fun handleSendImage(intent: Intent) {
        val imageUri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri
        if (imageUri != null) {
            messageType = UIKitContracts.IntentStrings.INTENT_MEDIA_MESSAGE
            mediaMessageUrl = imageUri.toString()
            Log.e(TAG, "handleSendImage: $mediaMessageUrl")
        }
    }

    /**
     * This method is used to handle parameter passed to this class.
     */
    private fun handleIntent() {

        // Get intent, action and MIME type
        val intent = intent
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                handleSendText(intent) // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent) // Handle single image being sent
            }
        }

        if (intent.hasExtra(UIKitContracts.IntentStrings.TYPE)) {
            messageType = intent.getStringExtra(UIKitContracts.IntentStrings.TYPE)
        }
        if (intent.hasExtra(CometChatConstants.MESSAGE_TYPE_TEXT)) {
            textMessage = intent.getStringExtra(CometChatConstants.MESSAGE_TYPE_TEXT)
        }
        if (intent.hasExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_URL)) {
            mediaMessageUrl = intent.getStringExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_URL)
        }
        if (intent.hasExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE)) {
            mediaMessageSize = intent.getIntExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, 0)
        }
        if (intent.hasExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION)) {
            mediaMessageExtension = intent.getStringExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION)
        }
        if (intent.hasExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_NAME)) {
            mediaMessageName = intent.getStringExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_NAME)
        }
        if (intent.hasExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE)) {
            mediaMessageMime = intent.getStringExtra(UIKitContracts.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE)
        }
        if (intent.hasExtra(UIKitContracts.IntentStrings.ID)) {
            id = intent.getIntExtra(UIKitContracts.IntentStrings.ID, 0)
        }
        if (getIntent().hasExtra(UIKitContracts.IntentStrings.LOCATION_LATITUDE)) {
            lat = getIntent().getDoubleExtra(UIKitContracts.IntentStrings.LOCATION_LATITUDE, 0.0)
        }
        if (getIntent().hasExtra(UIKitContracts.IntentStrings.LOCATION_LONGITUDE)) {
            lon = getIntent().getDoubleExtra(UIKitContracts.IntentStrings.LOCATION_LONGITUDE, 0.0)
        }
        if (getIntent().hasExtra(UIKitContracts.IntentStrings.MESSAGE_CATEGORY)) {
            messageCategory = getIntent().getStringExtra(UIKitContracts.IntentStrings.MESSAGE_CATEGORY)
        }
    }

    /**
     * This method is used to initialize the views
     */
    fun init() {
        // Inflate the layout
        val toolbar = findViewById<MaterialToolbar>(R.id.forward_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        if (Utils.changeToolbarFont(toolbar) != null) {
            Utils.changeToolbarFont(toolbar)!!.setTypeface(fontUtils!!.getTypeFace(FontUtils.robotoMedium))
        }
        selectedUsers = findViewById(R.id.selected_user)
        forwardBtn = findViewById(R.id.btn_forward)
        rvConversation = findViewById(R.id.rv_conversation_list)
        etSearch = findViewById(R.id.search_bar)
        clearSearch = findViewById(R.id.clear_search)

        makeConversationList()

        etSearch!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.length > 1) clearSearch!!.setVisibility(View.VISIBLE)
            }

            override fun afterTextChanged(editable: Editable) {
                if (editable.toString().length != 0) {
                    if (cometChatConversationsAdapter != null) cometChatConversationsAdapter!!.filter.filter(editable.toString())
                }
            }
        })
        etSearch!!.setOnEditorActionListener(OnEditorActionListener { textView: TextView, i: Int, keyEvent: KeyEvent? ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                if (cometChatConversationsAdapter != null) cometChatConversationsAdapter!!.filter.filter(textView.text.toString())
                clearSearch!!.setVisibility(View.VISIBLE)
                return@OnEditorActionListener true
            }
            false
        })
        clearSearch!!.setOnClickListener(View.OnClickListener { view1: View? ->
            etSearch!!.setText("")
            clearSearch!!.setVisibility(View.GONE)
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // Hide the soft keyboard
            inputMethodManager.hideSoftInputFromWindow(etSearch!!.getWindowToken(), 0)
        })
        rvConversation!!.setItemClickListener(object : OnItemClickListener<Conversation>() {
//            override fun OnItemClick(conversation: Conversation, position: Int) {
//
//            }

            override fun OnItemClick(t: Any, position: Int) {
                if (userList != null && userList.size < 5) {
                    var conversation: Conversation = t as Conversation
                    if (!userList.containsKey(conversation.conversationId)) {
                        userList[conversation.conversationId] = conversation
                        val chip = Chip(this@CometChatForwardMessageActivity)
                        if (conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_USER) {
                            name = (conversation.conversationWith as User).name
                            avatar = (conversation.conversationWith as User).avatar
                        } else {
                            name = (conversation.conversationWith as Group).name
                            avatar = (conversation.conversationWith as Group).icon
                        }
                        chip.text = name
                        Glide.with(this@CometChatForwardMessageActivity).load(avatar).placeholder(R.drawable.ic_contacts).transform(CircleCrop()).into(object : SimpleTarget<Drawable?>() {
                            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable?>?) {
                                chip.chipIcon = resource
                            }
                        })
                        chip.isCloseIconVisible = true
                        chip.setOnCloseIconClickListener { vw ->
                            userList.remove(conversation.conversationId)
                            selectedUsers!!.removeView(vw)
                            checkUserList()
                        }
                        selectedUsers!!.addView(chip, 0)
                    }
                    checkUserList()
                } else {
                    Toast.makeText(this@CometChatForwardMessageActivity, "You cannot forward message to more than 5 members", Toast.LENGTH_LONG).show()
                }
            }
        })

        //It sends message to selected users present in userList using thread. So UI thread doesn't get heavy.
        forwardBtn!!.setOnClickListener(View.OnClickListener {
            if (messageCategory == CometChatConstants.CATEGORY_MESSAGE) {
                if (messageType != null && messageType == CometChatConstants.MESSAGE_TYPE_TEXT) {
                    Thread(Runnable {
                        for (i in 0 until userList!!.size) {
                            val conversation = ArrayList(userList.values)[i]
                            var message: TextMessage
                            var uid: String?
                            var type: String
                            Log.e(TAG, "run: " + conversation.conversationId)
                            if (conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_USER) {
                                uid = (conversation.conversationWith as User).uid
                                type = CometChatConstants.RECEIVER_TYPE_USER
                            } else {
                                uid = (conversation.conversationWith as Group).guid
                                type = CometChatConstants.RECEIVER_TYPE_GROUP
                            }
                            message = TextMessage(uid, textMessage, type)
                            sendMessage(message)
                            if (i == userList.size - 1) {
                                val intent = Intent(this@CometChatForwardMessageActivity, CometChatUnified::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }).start()
                } else if (messageType != null && messageType != UIKitContracts.IntentStrings.INTENT_MEDIA_MESSAGE) {
                    Thread(Runnable {
                        for (i in 0 until userList!!.size) {
                            val conversation = ArrayList(userList.values)[i]
                            var message: MediaMessage
                            var uid: String?
                            var type: String
                            Log.e(TAG, "run: " + conversation.conversationId)
                            if (conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_USER) {
                                uid = (conversation.conversationWith as User).uid
                                type = CometChatConstants.RECEIVER_TYPE_USER
                            } else {
                                uid = (conversation.conversationWith as Group).guid
                                type = CometChatConstants.RECEIVER_TYPE_GROUP
                            }
                            message = MediaMessage(uid, null, messageType, type)
                            val attachment = Attachment()
                            attachment.fileUrl = mediaMessageUrl
                            attachment.fileMimeType = mediaMessageMime
                            attachment.fileSize = mediaMessageSize
                            attachment.fileExtension = mediaMessageExtension
                            attachment.fileName = mediaMessageName
                            message.attachment = attachment
                            Log.e(TAG, "onClick: $attachment")
                            sendMediaMessage(message)
                            if (i == userList.size - 1) {
                                val intent = Intent(this@CometChatForwardMessageActivity, CometChatUnified::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }).start()
                } else {
                    Thread {
                        for (i in 0 until userList!!.size) {
                            val conversation = ArrayList(userList.values)[i]
                            var message: MediaMessage
                            var uid: String?
                            var type: String
                            Log.e(TAG, "run: " + conversation.conversationId)
                            if (conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_USER) {
                                uid = (conversation.conversationWith as User).uid
                                type = CometChatConstants.RECEIVER_TYPE_USER
                            } else {
                                uid = (conversation.conversationWith as Group).guid
                                type = CometChatConstants.RECEIVER_TYPE_GROUP
                            }
                            val file = MediaUtils.getRealPath(this@CometChatForwardMessageActivity, Uri.parse(mediaMessageUrl))
                            message = MediaMessage(uid, file, CometChatConstants.MESSAGE_TYPE_IMAGE, type)
                            try {
                                val jsonObject = JSONObject()
                                jsonObject.put("path", mediaMessageUrl)
                                message.metadata = jsonObject
                            } catch (e: Exception) {
                                Log.e(TAG, "onError: " + e.message)
                            }
                            sendMediaMessage(message)
                            if (i == userList!!.size - 1) {
                                val intent = Intent(this@CometChatForwardMessageActivity, CometChatUnified::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }.start()
                }
            } else {
                if (messageType != null && messageType.equals(UIKitContracts.IntentStrings.LOCATION, ignoreCase = true)) {
                    Thread {
                        for (i in 0 until userList!!.size) {
                            val conversation = ArrayList(userList!!.values)[i]
                            var message: CustomMessage
                            var uid: String?
                            var customData = JSONObject()
                            var type: String
                            Log.e(TAG, "run: " + conversation.conversationId)
                            if (conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_USER) {
                                uid = (conversation.conversationWith as User).uid
                                type = CometChatConstants.RECEIVER_TYPE_USER
                            } else {
                                uid = (conversation.conversationWith as Group).guid
                                type = CometChatConstants.RECEIVER_TYPE_GROUP
                            }
                            try {
                                customData = JSONObject()
                                customData.put("latitude", lat)
                                customData.put("longitude", lon)
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                            message = CustomMessage(uid, type, UIKitContracts.IntentStrings.LOCATION, customData)
                            sendLocationMessage(message)
                            if (i == userList!!.size - 1) {
                                val intent = Intent(this@CometChatForwardMessageActivity, CometChatUnified::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }.start()
                }
            }

        })

        rvConversation!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    makeConversationList()
                }
            }
        })
    }

    private fun sendLocationMessage(message: CustomMessage) {
        CometChat.sendCustomMessage(message, object : CallbackListener<CustomMessage>() {
            override fun onSuccess(customMessage: CustomMessage) {
                Log.e(TAG, "onSuccess: " + customMessage.receiverUid)
            }

            override fun onError(e: CometChatException) {
                Log.e(TAG, "onErrorCustom: " + e.message)
            }
        })
    }

    private fun sendMessage(message: TextMessage) {
        CometChat.sendMessage(message, object : CallbackListener<TextMessage>() {
            override fun onSuccess(textMessage: TextMessage) {
                Log.e(TAG, "onSuccess: " + textMessage.receiverUid)
            }

            override fun onError(e: CometChatException) {
                Log.e(TAG, "onError: " + e.message)
            }
        })
    }

    private fun sendMediaMessage(mediaMessage: MediaMessage) {
        CometChat.sendMediaMessage(mediaMessage, object : CallbackListener<MediaMessage>() {
            override fun onSuccess(mediaMessage: MediaMessage) {
                Log.d(TAG, "sendMediaMessage onSuccess: $mediaMessage")
            }

            override fun onError(e: CometChatException) {
                Log.e(TAG, "onError: " + e.message)
            }
        })
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

    private fun checkUserList() {
        Log.e(TAG, "checkUserList: " + userList!!.size)
        if (userList.size > 0) {
            forwardBtn!!.visibility = View.VISIBLE
        } else {
            forwardBtn!!.visibility = View.GONE
        }
    }

    /**
     * This method is used to fetch conversations
     */
    private fun makeConversationList() {
        if (conversationsRequest == null) {
            conversationsRequest = ConversationsRequestBuilder().setLimit(50).build()
        }
        conversationsRequest!!.fetchNext(object : CallbackListener<List<Conversation>>() {
            override fun onSuccess(conversationsList: List<Conversation>) {
                if (conversationsList.isNotEmpty()) {
                    setAdapter(conversationsList)
                }
            }

            override fun onError(e: CometChatException) {
                Toast.makeText(baseContext, e.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setAdapter(conversations: List<Conversation>) {
        if (cometChatConversationsAdapter == null) {
            cometChatConversationsAdapter = CometChatConversationsAdapter(this)
            rvConversation!!.adapter = cometChatConversationsAdapter
        } else {
            cometChatConversationsAdapter!!.updateList(conversations)
        }
    }

    public override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        conversationsRequest = null
        cometChatConversationsAdapter = null
        makeConversationList()
    }

    override fun onPause() {
        Log.d(TAG, "onPause: ")
        super.onPause()
        CometChat.removeMessageListener(TAG)
        userList!!.clear()
    }

    public override fun onStart() {
        super.onStart()
    }

    public override fun onStop() {
        super.onStop()
    }

    companion object {
        private const val TAG = "CometChatForward"
    }
}