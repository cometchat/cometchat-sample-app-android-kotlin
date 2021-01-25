package screen

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.MessageReceipt
import com.cometchat.pro.uikit.CometChatReceiptsList
import com.cometchat.pro.uikit.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import constant.StringContract
import org.json.JSONException
import org.json.JSONObject
import utils.Utils

class CometChatMessageInfoScreenActivity : AppCompatActivity() {

    private var textMessage: View? = null
    private var imageMessage: View? = null
    private var audioMessage: View? = null
    private var fileMessage: View? = null
    private val videoMessage: View? = null
    private var stickerMessage: View? = null
    private var locationMessage: View? = null
    private lateinit var whiteBoardMessage: View
    private lateinit var writeBoardMessage: View

    private var ivMap: ImageView? = null
    private var tvPlaceName: TextView? = null

    private var messageText: TextView? = null
    private var messageImage: ImageView? = null
    private var messageVideo: ImageView? = null
    private var messageSticker: ImageView? = null
    private var txtTime: TextView? = null
//    private val sensitiveLayout: RelativeLayout? = null
    private lateinit var whiteBoardText: TextView
    private lateinit var writeBoardText: TextView
    private lateinit var joinWhiteBoard: MaterialButton
    private lateinit var joinWriteBoard: MaterialButton

    private var audioFileSize: TextView? = null

    private var fileName: TextView? = null
    private var fileExtension: TextView? = null
    private var fileSize: TextView? = null
    private lateinit var sensitiveLayout: RelativeLayout

    private var id = 0
    private var message: String? = null
    private var messageType: String? = null
    private var messageSize = 0
    private var messageExtension: String? = null
    private var percentage = 0
    private val TAG = "CometChatMessageInfo"

    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var cometChatReceiptsList: CometChatReceiptsList? = null

    private var toolbar: Toolbar? = null
    private var messageLayout: RelativeLayout? = null
    private var backIcon: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comet_chat_message_info_screen)

        toolbar = findViewById(R.id.detail_toolbar)
        messageLayout = findViewById(R.id.message_layout)
        backIcon = findViewById(R.id.backIcon)
        textMessage = findViewById(R.id.vwTextMessage)
        imageMessage = findViewById(R.id.vwImageMessage)
        audioMessage = findViewById(R.id.vwAudioMessage)
        fileMessage = findViewById(R.id.vwFileMessage)
        stickerMessage = findViewById(R.id.vw_sticker_message)
        messageSticker = findViewById(R.id.sticker_view)
        locationMessage = findViewById(R.id.vwLocationMessage)
        whiteBoardMessage = findViewById(R.id.vw_whiteboard_message)
        writeBoardMessage = findViewById(R.id.vw_writeboard_message)
        whiteBoardText = findViewById(R.id.whiteboard_message)
        writeBoardText = findViewById(R.id.writeboard_message)
        joinWhiteBoard = findViewById(R.id.join_whiteboard)
        joinWriteBoard = findViewById(R.id.join_writeboard)
        sensitiveLayout = findViewById(R.id.sensitive_layout)

        messageText = findViewById(R.id.go_txt_message)
        txtTime = findViewById(R.id.txt_time)
        txtTime!!.setVisibility(View.VISIBLE)
        messageImage = findViewById(R.id.go_img_message)
        messageVideo = findViewById(R.id.go_video_message)

        audioFileSize = findViewById(R.id.audiolength_tv)
        fileName = findViewById(R.id.tvFileName)
        fileSize = findViewById(R.id.tvFileSize)
        fileExtension = findViewById(R.id.tvFileExtension)
        swipeRefreshLayout = findViewById(R.id.swipe_refresh)
        cometChatReceiptsList = findViewById(R.id.rvReceipts)
        swipeRefreshLayout!!.setColorSchemeColors(
                resources.getColor(R.color.colorPrimary),
                resources.getColor(R.color.red),
                resources.getColor(R.color.grey))
        swipeRefreshLayout!!.setOnRefreshListener { fetchReceipts() }

        ivMap = findViewById(R.id.iv_map)
        tvPlaceName = findViewById(R.id.tv_place_name)
        handleIntent()
        fetchReceipts()
        backIcon!!.setOnClickListener(View.OnClickListener { view: View? -> onBackPressed() })
        if (Utils.isDarkMode(this)) {
            toolbar!!.setBackgroundColor(resources.getColor(R.color.darkModeBackground))
            messageLayout!!.setBackgroundColor(resources.getColor(R.color.darkModeBackground))
        } else {
            toolbar!!.setBackgroundColor(resources.getColor(R.color.textColorWhite))
            messageLayout!!.setBackgroundColor(resources.getColor(R.color.light_grey))
        }
    }

    private fun handleIntent() {
        if (intent.hasExtra(StringContract.IntentStrings.ID)) {
            id = intent.getIntExtra(StringContract.IntentStrings.ID, 0)
        }
        if (intent.hasExtra(StringContract.IntentStrings.TEXTMESSAGE)) {
            message = intent.getStringExtra(StringContract.IntentStrings.TEXTMESSAGE)
        }
        if (intent.hasExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL)) {
            message = intent.getStringExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL)
        }
        if (intent.hasExtra(StringContract.IntentStrings.MESSAGE_TYPE)) {
            messageType = intent.getStringExtra(StringContract.IntentStrings.MESSAGE_TYPE)
        }
        if (intent.hasExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION)) {
            messageExtension = intent.getStringExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION)
        }
        if (intent.hasExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE)) {
            messageSize = intent.getIntExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, 0)
        }
        if (intent.hasExtra(StringContract.IntentStrings.SENTAT)) {
            txtTime!!.text = Utils.getHeaderDate(intent
                    .getLongExtra(StringContract.IntentStrings.SENTAT, 0) * 1000)
        }
        if (intent.hasExtra(StringContract.IntentStrings.IMAGE_MODERATION)) {
            val isImageNotSafe = intent
                    .getBooleanExtra(StringContract.IntentStrings.IMAGE_MODERATION, true)
            if (isImageNotSafe) sensitiveLayout.setVisibility(View.VISIBLE) else sensitiveLayout.setVisibility(View.GONE)
        }
        if (intent.hasExtra(StringContract.IntentStrings.CUSTOM_MESSAGE)) {
            message = intent.getStringExtra(StringContract.IntentStrings.CUSTOM_MESSAGE)
        }
        if (messageType != null) {
            if (messageType == CometChatConstants.MESSAGE_TYPE_TEXT) {
                textMessage!!.visibility = View.VISIBLE
                messageText!!.text = message
            } else if (messageType == CometChatConstants.MESSAGE_TYPE_IMAGE) {
                imageMessage!!.visibility = View.VISIBLE
                Glide.with(this).load(message).into(messageImage!!)
            } else if (messageType == CometChatConstants.MESSAGE_TYPE_VIDEO) {
                videoMessage!!.visibility = View.VISIBLE
                Glide.with(this).load(message).into(messageVideo!!)
            } else if (messageType == CometChatConstants.MESSAGE_TYPE_FILE) {
                fileMessage!!.visibility = View.VISIBLE
                fileName!!.text = message
                fileSize!!.text = Utils.getFileSize(messageSize)
                fileExtension!!.text = messageExtension
            } else if (messageType == CometChatConstants.MESSAGE_TYPE_AUDIO) {
                audioMessage!!.visibility = View.VISIBLE
                audioFileSize!!.text = Utils.getFileSize(messageSize)
            } else if (messageType == StringContract.IntentStrings.STICKERS) {
                stickerMessage?.visibility = View.VISIBLE
                try {
                    val jsonObject = JSONObject(message)
                    Glide.with(this).load(jsonObject.getString("url")).into(messageSticker!!)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else if (messageType == StringContract.IntentStrings.WHITEBOARD) {
                whiteBoardMessage.visibility = View.VISIBLE
                whiteBoardText.text = getString(R.string.you_created_whiteboard)
                joinWhiteBoard.setOnClickListener(View.OnClickListener {
                    val boardUrl = intent.getStringExtra(StringContract.IntentStrings.TEXTMESSAGE)
                    val intent = Intent(this@CometChatMessageInfoScreenActivity, CometChatWebViewActivity::class.java)
                    intent.putExtra(StringContract.IntentStrings.URL, boardUrl)
                    startActivity(intent)
                })
            } else if (messageType == StringContract.IntentStrings.WRITEBOARD) {
                writeBoardMessage.visibility = View.VISIBLE
                writeBoardText.text = getString(R.string.you_created_document)
                joinWriteBoard.setOnClickListener(View.OnClickListener {
                    val boardUrl = intent.getStringExtra(StringContract.IntentStrings.TEXTMESSAGE)
                    val intent = Intent(this@CometChatMessageInfoScreenActivity, CometChatWebViewActivity::class.java)
                    intent.putExtra(StringContract.IntentStrings.URL, boardUrl)
                    startActivity(intent)
                })
            }
            else if (messageType == StringContract.IntentStrings.LOCATION) {
                try {
                    locationMessage!!.visibility = View.VISIBLE
                    val jsonObject = JSONObject(message)
                    val LATITUDE = jsonObject.getDouble("latitude")
                    val LONGITUDE = jsonObject.getDouble("longitude")
                    tvPlaceName!!.setVisibility(View.GONE)
                    val mapUrl: String = StringContract.MapUrl.MAPS_URL + LATITUDE + "," + LONGITUDE + "&key=" + StringContract.MapUrl.MAP_ACCESS_KEY
                    Glide.with(this)
                            .load(mapUrl)
                            .into(ivMap!!)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun fetchReceipts() {
        CometChat.getMessageReceipts(id, object : CometChat.CallbackListener<List<MessageReceipt>>() {
            override fun onSuccess(messageReceipts: List<MessageReceipt>) {
                cometChatReceiptsList!!.clear()
                cometChatReceiptsList!!.setMessageReceiptList(messageReceipts)
                if (swipeRefreshLayout!!.isRefreshing) swipeRefreshLayout!!.isRefreshing = false
            }

            override fun onError(e: CometChatException) {
                Snackbar.make(cometChatReceiptsList!!, e.message!!, Snackbar.LENGTH_LONG).show()
            }
        })
    }
}