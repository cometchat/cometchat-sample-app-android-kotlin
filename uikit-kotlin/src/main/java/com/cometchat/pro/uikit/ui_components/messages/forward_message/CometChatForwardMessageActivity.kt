package com.cometchat.pro.uikit.ui_components.messages.forward_message

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
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
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.cometchat_ui.CometChatUI
import com.cometchat.pro.uikit.ui_components.shared.cometchatConversations.CometChatConversation
import com.cometchat.pro.uikit.ui_components.shared.cometchatConversations.CometChatConversationsAdapter
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.MediaUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.json.JSONException
import org.json.JSONObject
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

    private var uri: Uri? = null
    private var sendIntent : String? = null
    private var sendIntentType: String? = null
    var progressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cometchat_forward_message)
        fontUtils = FontUtils.getInstance(this)
        checkPermissions()
        handleIntent()
        init()
    }

    private fun checkPermissions() {
        if (!Utils.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        UIKitConstants.RequestCode.RECORD)
            }
        }
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
            sendIntent = UIKitConstants.IntentStrings.INTENT_MEDIA_MESSAGE // can be boolean
            sendIntentType = MediaUtils.getExtensionType(intent.type!!)
            messageType = CometChatConstants.MESSAGE_TYPE_IMAGE
            uri = imageUri
            Log.e(TAG, "handleSendImage: $uri")
        }
    }
    fun handleSendVideo(intent: Intent) {
        val imageUri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri
        if (imageUri != null) {
            sendIntent = UIKitConstants.IntentStrings.INTENT_MEDIA_MESSAGE
            sendIntentType = MediaUtils.getExtensionType(intent.type!!)
            messageType = CometChatConstants.MESSAGE_TYPE_VIDEO
            uri = imageUri
//            Log.e(TAG, "handleSendVideo: $mediaMessageUrl")
        }
    }

    fun handleSendAudio(intent: Intent) {
        val imageUri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri
        if (imageUri != null) {
            sendIntent = UIKitConstants.IntentStrings.INTENT_MEDIA_MESSAGE
            sendIntentType = MediaUtils.getExtensionType(intent.type!!)
            messageType = CometChatConstants.MESSAGE_TYPE_AUDIO
            uri = imageUri
//            Log.e(TAG, "handleSendAudio: $mediaMessageUrl")
        }
    }

    fun handleFileIntent(intent: Intent) {
        val imageUri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri
        if (imageUri != null) {
            sendIntent = UIKitConstants.IntentStrings.INTENT_MEDIA_MESSAGE
            sendIntentType = MediaUtils.getExtensionType(intent.type!!)
            messageType = CometChatConstants.MESSAGE_TYPE_FILE
            uri = imageUri
//            Log.e(TAG, "handleSendAudio: $mediaMessageUrl")
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


        Log.e(TAG, "handleIntent: " + intent.type)

        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                handleSendText(intent) // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent) // Handle single image being sent
            } else if (type.startsWith("video/")) {
                handleSendVideo(intent)
            } else if (type.startsWith("audio/")) {
                handleSendAudio(intent)
            } else if (type.startsWith("application/")) {
                handleFileIntent(intent)
            }
        }

        if (intent.hasExtra(UIKitConstants.IntentStrings.TYPE)) {
            messageType = intent.getStringExtra(UIKitConstants.IntentStrings.TYPE)
        }
        if (intent.hasExtra(CometChatConstants.MESSAGE_TYPE_TEXT)) {
            textMessage = intent.getStringExtra(CometChatConstants.MESSAGE_TYPE_TEXT)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL)) {
            mediaMessageUrl = intent.getStringExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE)) {
            mediaMessageSize = intent.getIntExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, 0)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION)) {
            mediaMessageExtension = intent.getStringExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME)) {
            mediaMessageName = intent.getStringExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE)) {
            mediaMessageMime = intent.getStringExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.ID)) {
            id = intent.getIntExtra(UIKitConstants.IntentStrings.ID, 0)
        }
        if (getIntent().hasExtra(UIKitConstants.IntentStrings.LOCATION_LATITUDE)) {
            lat = getIntent().getDoubleExtra(UIKitConstants.IntentStrings.LOCATION_LATITUDE, 0.0)
        }
        if (getIntent().hasExtra(UIKitConstants.IntentStrings.LOCATION_LONGITUDE)) {
            lon = getIntent().getDoubleExtra(UIKitConstants.IntentStrings.LOCATION_LONGITUDE, 0.0)
        }
        if (getIntent().hasExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY)) {
            messageCategory = getIntent().getStringExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY)
        }
    }

    /**
     * This method is used to initialize the views
     */
    fun init() {
        // Inflate the layout
        val toolbar = findViewById<MaterialToolbar>(R.id.forward_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (Utils.changeToolbarFont(toolbar) != null) {
            Utils.changeToolbarFont(toolbar)?.typeface = fontUtils?.getTypeFace(FontUtils.robotoMedium)
        }
        selectedUsers = findViewById(R.id.selected_user)
        forwardBtn = findViewById(R.id.btn_forward)
        rvConversation = findViewById(R.id.rv_conversation_list)
        etSearch = findViewById(R.id.search_bar)
        clearSearch = findViewById(R.id.clear_search)

        makeConversationList()

        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.length > 1) clearSearch?.visibility = View.VISIBLE
            }

            override fun afterTextChanged(editable: Editable) {
                if (editable.toString().isNotEmpty()) {
                    if (cometChatConversationsAdapter != null) cometChatConversationsAdapter?.filter?.filter(editable.toString())
                }
            }
        })
        etSearch?.setOnEditorActionListener(OnEditorActionListener { textView: TextView, i: Int, keyEvent: KeyEvent? ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                if (cometChatConversationsAdapter != null) cometChatConversationsAdapter?.filter?.filter(textView.text.toString())
                clearSearch?.visibility = View.VISIBLE
                return@OnEditorActionListener true
            }
            false
        })
        clearSearch?.setOnClickListener(View.OnClickListener { view1: View? ->
            etSearch?.setText("")
            clearSearch?.visibility = View.GONE
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // Hide the soft keyboard
            inputMethodManager.hideSoftInputFromWindow(etSearch?.windowToken, 0)
        })
        rvConversation?.setItemClickListener(object : OnItemClickListener<Conversation>() {
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
                            selectedUsers?.removeView(vw)
                            checkUserList()
                        }
                        selectedUsers?.addView(chip, 0)
                    }
                    checkUserList()
                } else {
                    ErrorMessagesUtils.showCometChatErrorDialog(this@CometChatForwardMessageActivity, resources.getString(R.string.something_went_wrong_please_try_again))
                }
            }
        })

        //It sends message to selected users present in userList using thread. So UI thread doesn't get heavy.
        forwardBtn?.setOnClickListener(View.OnClickListener {
            progressDialog = ProgressDialog.show(this, "", "Sending Media Message")
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
                                val intent = Intent(this@CometChatForwardMessageActivity, CometChatUI::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }).start()
                } else if (messageType != null && sendIntent != UIKitConstants.IntentStrings.INTENT_MEDIA_MESSAGE) {
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
                                val intent = Intent(this@CometChatForwardMessageActivity, CometChatUI::class.java)
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
                            var message: MediaMessage? = null
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
//                            var file = MediaUtils.getRealPath(this@CometChatForwardMessageActivity, Uri.parse(mediaMessageUrl))
                            val file = uri?.let { it1 -> messageType?.let { it2 -> sendIntentType?.let { it3 -> MediaUtils.saveFile(this, it1, it2, it3) } } }
                            Log.e(TAG, "init: " + file.toString())
                            if (file != null && messageType == CometChatConstants.MESSAGE_TYPE_IMAGE)
                                message = MediaMessage(uid, file, messageType, type)
                            else if (file != null && messageType == CometChatConstants.MESSAGE_TYPE_VIDEO)
                                message = MediaMessage(uid, file, messageType, type)
                            else if (file != null && messageType == CometChatConstants.MESSAGE_TYPE_AUDIO)
                                message = MediaMessage(uid, file, messageType, type)
                            else if (file != null && messageType == CometChatConstants.MESSAGE_TYPE_FILE)
                                message = MediaMessage(uid, file, messageType, type)
                            message?.let { it1 -> sendMediaMessage(it1) }
//                            try {
//                                val jsonObject = JSONObject()
//                                jsonObject.put("path", mediaMessageUrl)
////                                message.metadata = jsonObject
//                            } catch (e: Exception) {
//                                Log.e(TAG, "onError: " + e.message)
//                            }

//                            if (i == userList!!.size - 1) {
//                                val intent = Intent(this@CometChatForwardMessageActivity, CometChatUI::class.java)
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                                startActivity(intent)
//                                finish()
//                            }

                        }
                    }.start()
                }
            } else {
                if (messageType != null && messageType.equals(UIKitConstants.IntentStrings.LOCATION, ignoreCase = true)) {
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
                            message = CustomMessage(uid, type, UIKitConstants.IntentStrings.LOCATION, customData)
                            sendLocationMessage(message)
                            if (i == userList.size - 1) {
                                val intent = Intent(this@CometChatForwardMessageActivity, CometChatUI::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }.start()
                }
            }

        })

        rvConversation?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                progressDialog?.dismiss()
                val intent = Intent(this@CometChatForwardMessageActivity, CometChatUI::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
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
            forwardBtn?.visibility = View.VISIBLE
        } else {
            forwardBtn?.visibility = View.GONE
        }
    }

    /**
     * This method is used to fetch conversations
     */
    private fun makeConversationList() {
        if (conversationsRequest == null) {
            conversationsRequest = ConversationsRequestBuilder().setLimit(50).build()
        }
        conversationsRequest?.fetchNext(object : CallbackListener<List<Conversation>>() {
            override fun onSuccess(conversationsList: List<Conversation>) {
                if (conversationsList.isNotEmpty()) {
                    setAdapter(conversationsList)
                }
            }

            override fun onError(e: CometChatException) {
                ErrorMessagesUtils.cometChatErrorMessage(baseContext, e.code)
            }
        })
    }

    private fun setAdapter(conversations: List<Conversation>) {
        if (cometChatConversationsAdapter == null) {
            cometChatConversationsAdapter = CometChatConversationsAdapter(this)
            rvConversation?.adapter = cometChatConversationsAdapter
        } else {
            cometChatConversationsAdapter?.updateList(conversations)
        }
    }

    public override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        conversationsRequest = null
        rvConversation?.clearList()
        userList?.clear()
        selectedUsers?.removeAllViews()
        checkUserList()
        makeConversationList()
    }

    override fun onPause() {
        Log.d(TAG, "onPause: ")
        super.onPause()
        CometChat.removeMessageListener(TAG)
        userList?.clear()
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