package com.cometchat.pro.uikit.ui_components.messages.message_list

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.inputmethod.InputContentInfoCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.*
import com.cometchat.pro.core.GroupMembersRequest
import com.cometchat.pro.core.GroupMembersRequest.GroupMembersRequestBuilder
import com.cometchat.pro.core.MessagesRequest
import com.cometchat.pro.core.MessagesRequest.MessagesRequestBuilder
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.helpers.CometChatHelper
import com.cometchat.pro.models.*
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.groups.group_detail.CometChatGroupDetailActivity
import com.cometchat.pro.uikit.ui_components.messages.extensions.ExtensionResponseListener
import com.cometchat.pro.uikit.ui_components.messages.extensions.Extensions
import com.cometchat.pro.uikit.ui_components.messages.forward_message.CometChatForwardMessageActivity
import com.cometchat.pro.uikit.ui_components.messages.live_reaction.LiveReactionListener
import com.cometchat.pro.uikit.ui_components.messages.live_reaction.ReactionClickListener
import com.cometchat.pro.uikit.ui_components.messages.message_actions.CometChatMessageActions
import com.cometchat.pro.uikit.ui_components.messages.message_actions.listener.MessageActionCloseListener
import com.cometchat.pro.uikit.ui_components.messages.message_actions.listener.OnMessageLongClick
import com.cometchat.pro.uikit.ui_components.messages.message_information.CometChatMessageInfoScreenActivity
import com.cometchat.pro.uikit.ui_components.messages.threaded_message_list.CometChatThreadMessageListActivity
import com.cometchat.pro.uikit.ui_components.shared.cometchatAvatar.CometChatAvatar
import com.cometchat.pro.uikit.ui_components.shared.cometchatComposeBox.CometChatComposeBox
import com.cometchat.pro.uikit.ui_components.shared.cometchatComposeBox.listener.ComposeActionListener
import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.CometChatReactionDialog
import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.listener.OnReactionClickListener
import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.model.Reaction
import com.cometchat.pro.uikit.ui_components.shared.cometchatSmartReplies.CometChatSmartReply
import com.cometchat.pro.uikit.ui_components.shared.cometchatStickers.StickerView
import com.cometchat.pro.uikit.ui_components.shared.cometchatStickers.listener.StickerClickListener
import com.cometchat.pro.uikit.ui_components.shared.cometchatStickers.model.Sticker
import com.cometchat.pro.uikit.ui_components.users.user_details.CometChatUserDetailScreenActivity
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.*
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener
import com.cometchat.pro.uikit.ui_resources.utils.keyboard_utils.KeyBoardUtils
import com.cometchat.pro.uikit.ui_resources.utils.keyboard_utils.KeyboardVisibilityListener
import com.cometchat.pro.uikit.ui_resources.utils.sticker_header.StickyHeaderDecoration
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction
import com.cometchat.pro.uikit.ui_settings.UIKitSettings
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * Purpose - CometChatMessageScreen class is a fragment used to display list of messages and perform certain action on click of message.
 * It also provide search bar to perform search operation on the list of messages. User can send text,images,video and file as messages
 * to each other and in groups. User can also perform actions like edit message,delete message and forward messages to other user and groups.
 *
 * @see CometChat
 *
 * @see User
 *
 * @see Group
 *
 * @see TextMessage
 *
 * @see MediaMessage
 *
 *
 * Created on - 20th December 2019
 *
 *
 * Modified on  - 16th January 2020
 */
class CometChatMessageList : Fragment(), View.OnClickListener, OnMessageLongClick, MessageActionCloseListener {
    private var isReplyPrivately: Boolean = false
    private var replyMessageJson: JSONObject = JSONObject()
    private var isImageNotSafe: Boolean = false
    private var isDeleteMemberMessageVisible: Boolean = false
    private var isReactionsVisible: Boolean = false
    private var isReplyVisible: Boolean = false
    private var isThreadVisible : Boolean = false
    private var intentRequestCode: Int? = null
    private var isOngoingCall: Boolean = false
    private var ivUserInfo: ImageView? = null
    private var imageToFly: ImageView? = null
    private var liveReactionLayout: FrameLayout? = null
    private var name: String? = ""
    private var status: String? = ""
    private var messagesRequest //Used to fetch messages.
            : MessagesRequest? = null
    private var composeBox: CometChatComposeBox? = null
    private val mediaRecorder: MediaRecorder? = null
    private val mediaPlayer: MediaPlayer? = null
    private val audioFileNameWithPath: String? = null
    private var rvChatListView //Used to display list of messages.
            : RecyclerView? = null
    private var messageAdapter: MessageAdapter? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var rvSmartReply: CometChatSmartReply? = null
    private var messageShimmer: ShimmerFrameLayout? = null

    /**
     * **Avatar** is a UI Kit Component which is used to display user and group avatars.
     */
    private var userAvatar: CometChatAvatar? = null
    private var tvName: TextView? = null
    private var tvStatus: TextView? = null
    private lateinit var Id: String
    private var c: Context? = null
    private var blockUserLayout: LinearLayout? = null
    private var blockedUserName: TextView? = null
    private var stickyHeaderDecoration: StickyHeaderDecoration? = null
    private var avatarUrl: String? = null
    private var toolbar: Toolbar? = null
    private lateinit var type: String
    private var link: String? = null
    private var groupType: String? = null
    private var isBlockedByMe = false
    private var loggedInUserScope: String? = null
    //    private var rlMessageAction: RelativeLayout? = null
    private var ivCloseMessageAction: ImageView? = null
    //    private var ivCopyMessageAction: ImageView? = null
//    private var tvEditMessage: TextView? = null
//    private var tvDeleteMessage: TextView? = null
//    private var tvForwardMessage: TextView? = null
    private var editMessageLayout: RelativeLayout? = null
    private var tvMessageTitle: TextView? = null
    private var tvMessageSubTitle: TextView? = null
    private var baseMessage: BaseMessage? = null
    private var baseMessages: List<BaseMessage>? = ArrayList()
    private val messageList: MutableList<BaseMessage> = ArrayList()
    private var isEdit = false
    private var groupOwnerId: String? = null
    private var memberCount = 0
    private var memberNames: String? = null
    private var groupDesc: String? = null
    private var groupPassword: String? = null
    private val timer = Timer()
    private var typingTimer: Timer? = Timer()
    private var vw: View? = null
    private var isNoMoreMessages = false
    private var fontUtils: FontUtils? = null
    private val loggedInUser = getLoggedInUser()
    var CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var isInProgress = false
    private var isSmartReplyClicked = false
    private var onGoingCallView: RelativeLayout? = null
    private var onGoingCallTxt: TextView? = null
    private var onGoingCallClose: ImageView? = null
    var count = 0

    private var replyMessageLayout: RelativeLayout? = null
    private var replyTitle: TextView? = null
    private var replyMessage: TextView? = null
    private var replyMedia: ImageView? = null
    private var replyClose: ImageView? = null
    private var isReply = false

    private var cometChatMessageActions : CometChatMessageActions? = null

    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var location: Location? = null
    private val MIN_TIME: Long = 1000
    private val MIN_DIST: Long = 5
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var LATITUDE = 0.0
    var LONGITUDE = 0.0

    private var stickersView: StickerView? = null
    private var stickerLayout: RelativeLayout? = null
    private var closeStickerView: ImageView? = null
    private var pollOptionsArrayList = ArrayList<View>()

    private var ivAudioCallBtn: ImageView? = null
    private var ivVideoCallBtn: ImageView? = null

    private var ivBackArrow: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleArguments()
        if (activity != null) fontUtils = FontUtils.getInstance(activity)
        FeatureRestriction.isThreadedMessagesEnabled(object : FeatureRestriction.OnSuccessListener {
            override fun onSuccess(p0: Boolean) {
                isThreadVisible = p0
            }
        })
        FeatureRestriction.isMessageRepliesEnabled(object : FeatureRestriction.OnSuccessListener {
            override fun onSuccess(p0: Boolean) {
                isReplyVisible = p0
            }
        })
        FeatureRestriction.isReactionsEnabled(object : FeatureRestriction.OnSuccessListener {
            override fun onSuccess(p0: Boolean) {
                isReactionsVisible = p0
            }
        })
        FeatureRestriction.isDeleteMemberMessageEnabled(object : FeatureRestriction.OnSuccessListener {
            override fun onSuccess(p0: Boolean) {
                isDeleteMemberMessageVisible = p0
            }
        })
    }

    /**
     * This method is used to handle arguments passed to this fragment.
     */
    private fun handleArguments() {
        if (arguments != null) {
            Id = arguments?.getString(UIKitConstants.IntentStrings.UID).toString()
            avatarUrl = arguments?.getString(UIKitConstants.IntentStrings.AVATAR)
            status = arguments?.getString(UIKitConstants.IntentStrings.STATUS)
            name = arguments?.getString(UIKitConstants.IntentStrings.NAME)
            type = arguments?.getString(UIKitConstants.IntentStrings.TYPE).toString()
            link = arguments?.getString(UIKitConstants.IntentStrings.LINK)
            isReplyPrivately = arguments!!.getBoolean("isReply", false)
            if ((arguments!!.getString("baseMessageMetadata") != null)) {
                val json = arguments!!.getString("baseMessageMetadata")
                replyMessageJson = JSONObject(json!!)
            }

            Log.e(TAG, "handleArguments: isReply "+isReply)
//            if (isReply) replyMessage()
            if (type != null && type == CometChatConstants.RECEIVER_TYPE_GROUP) {
                Id = arguments?.getString(UIKitConstants.IntentStrings.GUID).toString()
                memberCount = arguments!!.getInt(UIKitConstants.IntentStrings.MEMBER_COUNT)
                groupDesc = arguments?.getString(UIKitConstants.IntentStrings.GROUP_DESC)
                groupPassword = arguments?.getString(UIKitConstants.IntentStrings.GROUP_PASSWORD)
                groupType = arguments?.getString(UIKitConstants.IntentStrings.GROUP_TYPE)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        vw = inflater.inflate(R.layout.fragment_cometchat_messagelist, container, false)
        initViewComponent(vw)
        return vw
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isReplyPrivately && replyMessageJson != null) {
            if (replyMessageJson.has("category")) {
                baseMessage = CometChatHelper.processMessage(replyMessageJson)
                Log.e(TAG, "onCreate: base"+baseMessage )
                replyMessage()
            }

        }
    }

    /**
     * This is a main method which is used to initialize the view for this fragment.
     *
     * @param view
     */
    private fun initViewComponent(view: View?) {
        setHasOptionsMenu(true)
        if (cometChatMessageActions != null) cometChatMessageActions?.dismiss()

        composeBox = view!!.findViewById(R.id.message_box)
//        if (type == CometChatConstants.RECEIVER_TYPE_USER)
//            composeBox?.isStartVideoCall = false
        liveReactionLayout = view.findViewById(R.id.live_reactions_layout)
        composeBox!!.btnLiveReaction?.setOnTouchListener(object : LiveReactionListener(object : ReactionClickListener() {
            override fun onClick(var1: View?) {
                liveReactionLayout?.alpha = 0.1f
                sendLiveReaction()
            }

            override fun onCancel(var1: View?) {
                Handler().postDelayed(object : Runnable {
                    override fun run() {
                        if (imageToFly != null) {
                            val animator = ObjectAnimator.ofFloat(liveReactionLayout!!, "alpha", 1f, 0.5f)
                            animator.duration = 2000
                            animator.start()
                            animator.addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator?) {
                                    super.onAnimationEnd(animation)
                                    if (imageToFly != null)
                                        imageToFly?.clearAnimation()
                                    liveReactionLayout?.clearAnimation()
                                    if (typingTimer != null)
                                        typingTimer?.schedule(object : TimerTask() {
                                            override fun run() {
                                                val metaData = JSONObject()
                                                try {
                                                    metaData.put("reaction", "heart")
                                                } catch (e: JSONException) {
                                                    e.printStackTrace()
                                                }
                                                val typingIndicator = TypingIndicator(Id!!, type, metaData)
                                                endTyping(typingIndicator)
                                            }
                                        }, 1000)
                                }
                            })
                        }
                    }
                }, 1000)
            }
        }) {})
        messageShimmer = view?.findViewById(R.id.shimmer_layout)
//        composeBox = view.findViewById(R.id.message_box)
        setComposeBoxListener()

        FeatureRestriction.isOneOnOneChatEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                if (type == CometChatConstants.RECEIVER_TYPE_USER) {
                    if (p0)
                        composeBox?.visibility = View.VISIBLE
                    else
                        composeBox?.visibility = View.GONE
                }
            }
        })
        FeatureRestriction.isGroupChatEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                if (type == CometChatConstants.RECEIVER_TYPE_GROUP) {
                    if (p0)
                        composeBox?.visibility = View.VISIBLE
                    else
                        composeBox?.visibility = View.GONE
                }
            }
        })

        rvSmartReply = view?.findViewById(R.id.rv_smartReply)
        FeatureRestriction.isSmartRepliesEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                if (!p0)
                    rvSmartReply?.visibility = View.GONE
            }
        })
//        rlMessageAction = view.findViewById(R.id.message_actions)
        ivCloseMessageAction = view?.findViewById(R.id.iv_close_message_action)
        ivCloseMessageAction?.setOnClickListener(this)
//        ivCopyMessageAction = view.findViewById(R.id.iv_copy_message_action)
//        ivCopyMessageAction!!.setOnClickListener(this)
//        tvEditMessage = view.findViewById(R.id.edit_message)
//        tvEditMessage!!.setOnClickListener(this)
//        tvDeleteMessage = view.findViewById(R.id.delete_message)
//        tvDeleteMessage!!.setOnClickListener(this)
//        tvForwardMessage = view.findViewById(R.id.forward_message)
//        tvForwardMessage!!.setOnClickListener(this)
        editMessageLayout = view.findViewById(R.id.editMessageLayout)
        tvMessageTitle = view.findViewById(R.id.tv_message_layout_title)
        tvMessageSubTitle = view.findViewById(R.id.tv_message_layout_subtitle)
        val ivMessageClose = view.findViewById<ImageView>(R.id.iv_message_close)
        ivMessageClose?.setOnClickListener(this)

        stickersView = view.findViewById(R.id.stickersView)
        stickerLayout = view.findViewById(R.id.sticker_layout)
        closeStickerView = view.findViewById(R.id.close_sticker_layout)

        closeStickerView?.setOnClickListener(View.OnClickListener { stickerLayout?.visibility = View.GONE })

        stickersView?.setStickerClickListener(object : StickerClickListener {
            override fun onClickListener(sticker: Sticker?) {
                val stickerData = JSONObject()
                try {
                    stickerData.put("sticker_url", sticker?.url)
                    stickerData.put("sticker_name", sticker?.name)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                sendCustomMessage(UIKitConstants.IntentStrings.STICKERS, stickerData)
                stickerLayout?.visibility = View.GONE
            }
        })

        replyMessageLayout = view.findViewById(R.id.replyMessageLayout)
        replyTitle = view.findViewById(R.id.tv_reply_layout_title)
        replyMessage = view.findViewById(R.id.tv_reply_layout_subtitle)
        replyMedia = view.findViewById(R.id.iv_reply_media)
        replyClose = view.findViewById(R.id.iv_reply_close)
        replyClose?.setOnClickListener(this)

        ivAudioCallBtn = view.findViewById(R.id.iv_audio_call_btn)
        ivVideoCallBtn = view.findViewById(R.id.iv_video_call_btn)

        FeatureRestriction.isOneOnOneAudioCallEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                if (p0 && type == CometChatConstants.RECEIVER_TYPE_USER) {
                    ivAudioCallBtn?.visibility = View.VISIBLE
                } else ivAudioCallBtn?.visibility = View.GONE
            }
        })
        FeatureRestriction.isOneOnOneVideoCallEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                if (p0) ivVideoCallBtn?.visibility = View.VISIBLE else ivVideoCallBtn?.visibility = View.GONE
            }
        })


        rvChatListView = view.findViewById(R.id.rv_message_list)
        val unblockUserBtn: MaterialButton = view.findViewById(R.id.btn_unblock_user)
        unblockUserBtn.setOnClickListener(this)
        blockedUserName = view.findViewById(R.id.tv_blocked_user_name)
        blockUserLayout = view.findViewById(R.id.blocked_user_layout)
        tvName = view.findViewById(R.id.tv_name)
        tvStatus = view.findViewById(R.id.tv_status)
        FeatureRestriction.isUserPresenceEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                if (!p0)
                    if (type == CometChatConstants.RECEIVER_TYPE_USER)
                        tvStatus?.visibility = View.GONE
            }
        })
        userAvatar = view.findViewById(R.id.iv_chat_avatar)
        toolbar = view.findViewById(R.id.chatList_toolbar)
        toolbar?.setOnClickListener(this)
        ivUserInfo = view.findViewById(R.id.iv_user_info)
        ivUserInfo?.setOnClickListener(this)
        ivBackArrow = view.findViewById(R.id.iv_back_arrow)
        ivBackArrow?.setOnClickListener(this)


        userAvatar = view.findViewById(R.id.iv_chat_avatar)
        toolbar = view.findViewById(R.id.chatList_toolbar)
        toolbar?.setOnClickListener(this)
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        tvName?.typeface = fontUtils?.getTypeFace(FontUtils.robotoMedium)
        tvName?.text = name
        setAvatar()

//        barVisualizer = view.findViewById(R.id.barVisualizer);
        rvChatListView!!.layoutManager = linearLayoutManager
//        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
//        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        if (Utils.isDarkMode(context!!)) {
            toolbar?.setBackgroundColor(resources.getColor(R.color.grey))
            editMessageLayout?.background = resources.getDrawable(R.drawable.left_border_dark)
            composeBox?.setBackgroundColor(resources.getColor(R.color.darkModeBackground))
            rvChatListView?.setBackgroundColor(resources.getColor(R.color.darkModeBackground))
            replyMessageLayout?.background = resources.getDrawable(R.drawable.left_border_dark)
//            rlMessageAction?.setBackgroundColor(resources.getColor(R.color.darkModeBackground))
            tvName?.setTextColor(resources.getColor(R.color.textColorWhite))
        } else {
            toolbar?.setBackgroundColor(resources.getColor(R.color.textColorWhite))
            editMessageLayout?.background = resources.getDrawable(R.drawable.left_border)
            composeBox?.setBackgroundColor(resources.getColor(R.color.textColorWhite))
            rvChatListView?.setBackgroundColor(resources.getColor(R.color.textColorWhite))
            replyMessageLayout?.background = resources.getDrawable(R.drawable.left_border)
//            rlMessageAction!!.setBackgroundColor(resources.getColor(R.color.textColorWhite))
            tvName?.setTextColor(resources.getColor(R.color.primaryTextColor))
        }
        KeyBoardUtils.setKeyboardVisibilityListener(activity!!, rvChatListView?.parent as View, object : KeyboardVisibilityListener {
            override fun onKeyboardVisibilityChanged(keyboardVisible: Boolean) {
                if (keyboardVisible) {
                    scrollToBottom()
                    composeBox?.ivMic?.visibility = View.GONE
                    composeBox?.btnLiveReaction?.visibility = View.GONE
                    composeBox?.ivSend?.visibility = View.VISIBLE
                } else {
                    composeBox?.ivSend?.visibility = View.GONE
                    composeBox?.btnLiveReaction?.visibility = View.VISIBLE
                    composeBox?.ivMic?.visibility = View.VISIBLE
                    FeatureRestriction.isVoiceNotesEnabled(object : FeatureRestriction.OnSuccessListener{
                        override fun onSuccess(p0: Boolean) {
                            if (p0) {
                                composeBox!!.ivMic!!.visibility = View.VISIBLE
                            } else {
                                composeBox!!.ivMic!!.visibility = View.GONE
                                composeBox!!.ivSend!!.visibility = View.VISIBLE
                            }
                        }

                    })
                }
            }
        })


        // Uses to fetch next list of messages if rvChatListView (RecyclerView) is scrolled in downward direction.
        rvChatListView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

                //for toolbar elevation animation i.e stateListAnimator
                toolbar?.isSelected = rvChatListView!!.canScrollVertically(-1)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!isNoMoreMessages && !isInProgress) {
                    if (linearLayoutManager?.findFirstVisibleItemPosition() == 10 || !rvChatListView!!.canScrollVertically(-1)) {
                        isInProgress = true
                        fetchMessage()
                    }
                }
            }
        })
        rvSmartReply?.setItemClickListener(object : OnItemClickListener<String?>() {
            override fun OnItemClick(t: Any, position: Int) {
                if (!isSmartReplyClicked) {
                    isSmartReplyClicked = true
                    rvSmartReply?.visibility = View.GONE
                    sendMessage(t as String)
                }
            }
        })

        //Check Ongoing Call
        onGoingCallView = view.findViewById(R.id.ongoing_call_view)
        onGoingCallClose = view.findViewById(R.id.close_ongoing_view)
        onGoingCallTxt = view.findViewById(R.id.ongoing_call)
        checkOnGoingCall()
        if (isOngoingCall) {
            ivAudioCallBtn?.isEnabled = false
            ivVideoCallBtn?.isEnabled = false
        } else {
            ivAudioCallBtn?.isEnabled = true
            ivVideoCallBtn?.isEnabled = true
        }
        if (type == CometChatConstants.RECEIVER_TYPE_GROUP) {
            ivAudioCallBtn?.visibility = View.GONE
        } else ivAudioCallBtn?.visibility = View.VISIBLE
        ivAudioCallBtn?.setOnClickListener {
            if (type == CometChatConstants.RECEIVER_TYPE_USER) {
                Utils.initiatecall(context!!, Id, CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.CALL_TYPE_AUDIO)
            }
//            else Utils.initiatecall(context!!, Id, CometChatConstants.RECEIVER_TYPE_GROUP, CometChatConstants.CALL_TYPE_AUDIO)
        }
        ivVideoCallBtn?.setOnClickListener {
            if (type == CometChatConstants.RECEIVER_TYPE_USER) {
                Utils.initiatecall(context!!, Id, CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.CALL_TYPE_VIDEO)
            }
            else {
                val body = JSONObject()
                body.put("sessionID", Id)
                sendCustomMessage(UIKitConstants.IntentStrings.MEETING, body)
            }
        }
    }

    private fun sendLiveReaction() {
        var metadata : JSONObject = JSONObject()
        metadata.put("reaction", "heart")
        var typingIndicator: TypingIndicator = TypingIndicator(Id, type, metadata)
        CometChat.startTyping(typingIndicator)
        setLiveReaction()
    }

    private fun setLiveReaction() {
        liveReactionLayout?.alpha = 0.1f
        imageToFly = ImageView(context)
        flyImage(imageToFly!!, R.drawable.heart_reaction)
    }

    private fun flyImage(imageToFly: ImageView, resId: Int) {
        var layoutParam: FrameLayout.LayoutParams  = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParam.gravity = Gravity.BOTTOM or Gravity.END
        layoutParam.rightMargin = 16

        liveReactionLayout?.alpha = 1.0f
        imageToFly.layoutParams = layoutParam
        liveReactionLayout?.addView(imageToFly)

        val bitmap = BitmapFactory.decodeResource(context?.resources, resId)
        if (bitmap != null) {
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, (bitmap.width * 0.2f).toInt(), (bitmap.height * 0.2f).toInt(), false)
            imageToFly.setImageBitmap(scaledBitmap)
        }

        fadeOutAnimation(imageToFly)
    }

    private fun fadeOutAnimation(viewToAnimate: ImageView) {
        var transition: ObjectAnimator = ObjectAnimator.ofFloat(viewToAnimate, "translationY", -400f)
        var fadeOut: ObjectAnimator = ObjectAnimator.ofFloat(viewToAnimate, "alpha", 1f, 0f)
        transition.repeatCount = 3
        fadeOut.repeatCount = 3
        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                viewToAnimate.visibility = View.GONE
            }
        })
        val animatorSet= AnimatorSet()
        animatorSet.playTogether(transition, fadeOut)
        animatorSet.duration = 1000L
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()

    }

    private fun checkOnGoingCall() {
        if (getActiveCall() != null && getActiveCall().callStatus == CometChatConstants.CALL_STATUS_ONGOING && getActiveCall().sessionId != null) {
            isOngoingCall = true
            ivAudioCallBtn?.isEnabled = false
            ivVideoCallBtn?.isEnabled = false
            if (onGoingCallView != null) onGoingCallView!!.visibility = View.VISIBLE
            if (onGoingCallTxt != null) {
                onGoingCallTxt?.setOnClickListener {
                    onGoingCallView?.visibility = View.GONE
                    Utils.joinOnGoingCall(context!!)
                }
            }
            if (onGoingCallClose != null) {
                onGoingCallClose!!.setOnClickListener {
                    onGoingCallView!!.visibility = View.GONE
                    ivAudioCallBtn?.isEnabled = true
                    ivVideoCallBtn?.isEnabled = true
                }
            }
        } else if (getActiveCall() != null) {
            if (onGoingCallView != null) onGoingCallView?.visibility = View.GONE
            Log.e(TAG, "checkOnGoingCall: " + getActiveCall().toString())
        }
    }

    private fun setComposeBoxListener() {
        composeBox?.setComposeBoxListener(object : ComposeActionListener() {
            override fun onEditTextMediaSelected(inputContentInfo: InputContentInfoCompat?) {
                Log.e(TAG, """
     onEditTextMediaSelected: Path=${inputContentInfo?.linkUri?.path}
     Host=${inputContentInfo?.linkUri?.fragment}
     """.trimIndent())
                val messageType = inputContentInfo?.linkUri.toString().substring(inputContentInfo?.linkUri.toString().lastIndexOf('.'))
                val mediaMessage = MediaMessage(Id, null, CometChatConstants.MESSAGE_TYPE_IMAGE, type)
                val attachment = Attachment()
                attachment.fileUrl = inputContentInfo?.linkUri.toString()
                attachment.fileMimeType = inputContentInfo?.description?.getMimeType(0)
                attachment.fileExtension = messageType
                attachment.fileName = inputContentInfo?.description?.label.toString()
                mediaMessage.attachment = attachment
                Log.e(TAG, "onClick: $attachment")
                sendMediaMessage(mediaMessage, object : CallbackListener<MediaMessage?>() {
                    override fun onSuccess(mediaMessage: MediaMessage?) {
                        if (messageAdapter != null) {
                            messageAdapter?.addMessage(mediaMessage!!)
                            scrollToBottom()
                        }
                    }

                    override fun onError(e: CometChatException) {
                        if (activity != null) {
                            ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                        }
                    }
                })
            }

            override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
                if (charSequence!!.isNotEmpty()) {
                    sendTypingIndicator(false)
                } else {
                    sendTypingIndicator(true)
                }
            }


            override fun afterTextChanged(editable: Editable?) {
                if (typingTimer == null) {
                    typingTimer = Timer()
                }
                endTypingTimer()
            }

            override fun onAudioActionClicked() {
                if (Utils.hasPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    startActivityForResult(MediaUtils.openAudio(activity!!), UIKitConstants.RequestCode.AUDIO)
                } else {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), UIKitConstants.RequestCode.AUDIO)
                }
            }

            override fun onCameraActionClicked() {
                if (Utils.hasPermissions(context, *CAMERA_PERMISSION)) {
                    startActivityForResult(MediaUtils.openCamera(context!!), UIKitConstants.RequestCode.CAMERA)
                } else {
                    requestPermissions(CAMERA_PERMISSION, UIKitConstants.RequestCode.CAMERA)
                }
            }

            override fun onGalleryActionClicked() {
                if (Utils.hasPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    startActivityForResult(MediaUtils.openGallery(activity!!), UIKitConstants.RequestCode.GALLERY)
                } else {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), UIKitConstants.RequestCode.GALLERY)
                }
            }

            override fun onFileActionClicked() {
                if (Utils.hasPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    startActivityForResult(MediaUtils.getFileIntent(UIKitConstants.IntentStrings.EXTRA_MIME_DOC), UIKitConstants.RequestCode.FILE)
                } else {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), UIKitConstants.RequestCode.FILE)
                }
            }

            override fun onSendActionClicked(editText: EditText?) {
                val message = editText?.text.toString().trim { it <= ' ' }
                editText?.setText("")
                editText?.hint = getString(R.string.message)
                if (isEdit) {
                    editMessage(baseMessage, message)
                    editMessageLayout?.visibility = View.GONE
                } else if (isReply) {
                    replyMessage(baseMessage, message)
                    replyMessageLayout?.visibility = View.GONE
                } else if (message.isNotEmpty()) sendMessage(message)
            }

            override fun onVoiceNoteComplete(string: String?) {
                if (string != null) {
                    val audioFile = File(string)
                    sendMediaMessage(audioFile, CometChatConstants.MESSAGE_TYPE_AUDIO)
                }
            }

            override fun onLocationActionClicked() {
                if (Utils.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    initLocation()
                    //locationManager = (LocationManager) Objects.requireNonNull(getContext()).getSystemService(Context.LOCATION_SERVICE);
                    val provider: Boolean = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    if (!provider) {
                        turnOnLocation()
                    } else {
                        getLocation()
                    }
                } else {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), UIKitConstants.RequestCode.LOCATION)
                }
            }

            override fun onStickerActionClicked() {
                stickerLayout?.visibility = View.VISIBLE
                isExtensionEnabled("stickers", object : CallbackListener<Boolean>() {
                    override fun onSuccess(p0: Boolean) {
                        if (p0) {
                            Extensions.fetchStickers(object : ExtensionResponseListener<Any>() {
                                override fun onResponseSuccess(vararg: Any?) {
                                    val stickersJSON = vararg as JSONObject
                                    stickersView?.setData(Id, type, Extensions.extractStickersFromJSON(stickersJSON))
                                }

                                override fun onResponseFailed(e: CometChatException?) {
                                    ErrorMessagesUtils.cometChatErrorMessage(context, e?.code)
                                }

                            })
                        }
                    }

                    override fun onError(p0: CometChatException?) {
                        ErrorMessagesUtils.cometChatErrorMessage(context, p0?.code)
                    }

                })

            }

            override fun onWhiteBoardClicked() {
                Extensions.callExtensions("whiteboard", Id, type, object : ExtensionResponseListener<Any>() {
                    override fun onResponseSuccess(vararg: Any?) {
                        var jasonObject: JSONObject = vararg as JSONObject
                    }

                    override fun onResponseFailed(e: CometChatException?) {
                        ErrorMessagesUtils.cometChatErrorMessage(context, e?.code)
                    }
                })

            }

            override fun onWriteBoardClicked() {
                Extensions.callExtensions("document", Id, type, object : ExtensionResponseListener<Any>() {
                    override fun onResponseSuccess(vararg: Any?) {
                        var jasonObject: JSONObject = vararg as JSONObject
                    }

                    override fun onResponseFailed(e: CometChatException?) {
                        ErrorMessagesUtils.cometChatErrorMessage(context, e?.code)
                    }
                })
            }

//            override fun onStartCallClicked() {
//                val body = JSONObject()
//                body.put("sessionID", Id)
//                sendCustomMessage(UIKitConstants.IntentStrings.MEETING, body)
//            }

            override fun onPollActionClicked() {
                createPollDialog()
            }
        })
    }

    private fun createPollDialog() {
        var dialog = AlertDialog.Builder(context!!, R.style.dialogTheme)
        val view: View = LayoutInflater.from(context).inflate(R.layout.create_poll_layout, null)
        dialog.setView(view)
        val alertDialog: Dialog = dialog.create()
        pollOptionsArrayList.clear()
        val etQuestion = view.findViewById<EditText>(R.id.et_question)
        val option1 = view.findViewById<View>(R.id.option_1)
        val option2 = view.findViewById<View>(R.id.option_2)
        option1.findViewById<View>(R.id.delete_option).visibility = View.GONE
        option2.findViewById<View>(R.id.delete_option).visibility = View.GONE
        val addOption = view.findViewById<MaterialCardView>(R.id.add_options)
        val optionLayout = view.findViewById<LinearLayout>(R.id.options_layout)
        val createPoll: MaterialButton = view.findViewById(R.id.btn_create_poll)
        createPoll.setBackgroundColor(Color.parseColor(UIKitSettings.color))
        val cancelPoll = view.findViewById<ImageView>(R.id.cancel_poll)
        addOption.setOnClickListener(View.OnClickListener {
            val optionView: View = LayoutInflater.from(context).inflate(R.layout.poll_option, null)
            pollOptionsArrayList.add(optionView)
            optionLayout.addView(optionView)
            optionView.findViewById<View>(R.id.delete_option).setOnClickListener {
                pollOptionsArrayList.remove(optionView)
                optionLayout.removeView(optionView)
            }
        })
        createPoll.isEnabled = true
        createPoll.setOnClickListener(View.OnClickListener {
            etQuestion.requestFocus()
            val option1Text = option1.findViewById<EditText>(R.id.et_option)
            val option2Text = option2.findViewById<EditText>(R.id.et_option)
            if (etQuestion.text.toString().trim { it <= ' ' }.isEmpty()) etQuestion.error = getString(R.string.fill_this_field) else if (option1Text.text.toString().trim { it <= ' ' }.isEmpty()) option1Text.error = getString(R.string.fill_this_field) else if (option2Text.text.toString().trim { it <= ' ' }.isEmpty()) option2Text.error = getString(R.string.fill_this_field) else {
                val progressDialog = ProgressDialog.show(context, "", resources.getString(R.string.create_poll))
                createPoll.isEnabled = false
                try {
                    val optionJson = JSONArray()
                    optionJson.put(option1Text.text.toString())
                    optionJson.put(option2Text.text.toString())
                    for (views in pollOptionsArrayList) {
                        val optionsText = views.findViewById<EditText>(R.id.et_option)
                        if (optionsText.text.toString().trim { it <= ' ' }.isNotEmpty()) optionJson.put(optionsText.text.toString())
                    }
                    val jsonObject = JSONObject()
                    jsonObject.put("question", etQuestion.text.toString())
                    jsonObject.put("options", optionJson)
                    jsonObject.put("receiver", Id)
                    jsonObject.put("receiverType", type)
                    callExtension("polls", "POST", "/v2/create",
                            jsonObject, object : CallbackListener<JSONObject?>() {
                        override fun onSuccess(jsonObject: JSONObject?) {
                            progressDialog.dismiss()
                            alertDialog.dismiss()
                        }

                        override fun onError(e: CometChatException) {
                            progressDialog.dismiss()
                            createPoll.isEnabled = true
                            Log.e(TAG, "onErrorCallExtension: " + e.message)
                            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                        }
                    })
                } catch (e: java.lang.Exception) {
                    createPoll.isEnabled = true
                    Log.e(TAG, "onErrorJSON: " + e.message)
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }
            }
        })
        cancelPoll.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun getLocation() {
        fusedLocationProviderClient?.lastLocation?.addOnSuccessListener { location ->
            if (location != null) {
                val lon = location.longitude
                val lat = location.latitude
                val customData = JSONObject()
                try {
                    customData.put("latitude", lat)
                    customData.put("longitude", lon)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                initAlert(customData)
            } else {
                Toast.makeText(context, getString(R.string.unable_to_get_location), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initAlert(customData: JSONObject) {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(getString(R.string.share_location_alert))
        builder.setPositiveButton(getString(R.string.share)) { dialog, which -> sendCustomMessage(UIKitConstants.IntentStrings.LOCATION, customData) }.setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }
        builder.create()
        builder.show()
    }

    private fun sendCustomMessage(customType: String, customData: JSONObject) {
        var progressDialog = ProgressDialog.show(context, "", resources.getString(R.string.sending))
        val customMessage: CustomMessage
        customMessage = if (type.equals(CometChatConstants.RECEIVER_TYPE_USER, ignoreCase = true)) CustomMessage(Id, CometChatConstants.RECEIVER_TYPE_USER, customType, customData) else CustomMessage(Id, CometChatConstants.RECEIVER_TYPE_GROUP, customType, customData)
        val incrementCountObject = JSONObject()
        val pushNotificationObject = JSONObject()
        if (customType == UIKitConstants.IntentStrings.LOCATION) {
            pushNotificationObject.put("pushNotification", loggedInUser.name+" has shared his location")
            customMessage.metadata = pushNotificationObject
        }
        if (customType == UIKitConstants.IntentStrings.STICKERS || customType == UIKitConstants.IntentStrings.LOCATION) {
            incrementCountObject.put("incrementUnreadCount", true)
            customMessage.metadata = incrementCountObject
        }
        sendCustomMessage(customMessage, object : CallbackListener<CustomMessage>() {
            override fun onSuccess(customMessage: CustomMessage) {
                progressDialog.dismiss()
                if (messageAdapter != null) {
                    messageAdapter?.addMessage(customMessage)
                    scrollToBottom()
                    if (customMessage.customData.has("sessionID"))
                        Utils.startVideoCallIntent(context!!, customMessage.customData.getString("sessionID"))
                }
            }

            override fun onError(e: CometChatException) {
                if (activity != null) {
                    progressDialog.dismiss()
                    ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                }
            }
        })
    }

    private fun turnOnLocation() {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(getString(R.string.turn_on_gps))
        builder.setPositiveButton(getString(R.string.on)) { dialog, which -> startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), UIKitConstants.RequestCode.LOCATION) }.setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }
        builder.create()
        builder.show()
    }

    private fun initLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {}
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST.toFloat(), locationListener)
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST.toFloat(), locationListener)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionsResult: ")
        when (requestCode) {
            UIKitConstants.RequestCode.CAMERA -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) startActivityForResult(MediaUtils.openCamera(activity!!), UIKitConstants.RequestCode.CAMERA) else showSnackBar(view?.findViewById(R.id.message_box), resources.getString(R.string.grant_camera_permission))
            UIKitConstants.RequestCode.GALLERY -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) startActivityForResult(MediaUtils.openGallery(activity!!), UIKitConstants.RequestCode.GALLERY) else showSnackBar(view?.findViewById(R.id.message_box), resources.getString(R.string.grant_storage_permission))
            UIKitConstants.RequestCode.FILE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) startActivityForResult(MediaUtils.getFileIntent(UIKitConstants.IntentStrings.EXTRA_MIME_DOC), UIKitConstants.RequestCode.FILE) else showSnackBar(view?.findViewById(R.id.message_box), resources.getString(R.string.grant_storage_permission))
            UIKitConstants.RequestCode.LOCATION -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initLocation()
                //locationManager = (LocationManager) Objects.requireNonNull(getContext()).getSystemService(Context.LOCATION_SERVICE);
                val provider = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                if (!provider) {
                    turnOnLocation()
                } else {
                    getLocation()
                }
            } else
                showSnackBar(view?.findViewById(R.id.message_box), resources.getString(R.string.grant_location_permission))
        }
    }

    private fun showSnackBar(view: View?, message: String) {
        ErrorMessagesUtils.showCometChatErrorDialog(context, message)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (activity != null) {
                activity?.onBackPressed()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * This method is used to get Group Members and display names of group member.
     *
     * @see GroupMember
     *
     * @see GroupMembersRequest
     */
    private val member: Unit
        private get() {
            val groupMembersRequest = GroupMembersRequestBuilder(Id).setLimit(100).build()
            groupMembersRequest.fetchNext(object : CallbackListener<List<GroupMember>?>() {
                override fun onSuccess(list: List<GroupMember>?) {
                    var s = arrayOfNulls<String>(0)
                    if (list != null && list.size != 0) {
                        s = arrayOfNulls(list.size)
                        for (j in list.indices) {
                            s[j] = list[j].name
                        }
                    }

                    setSubTitle(s)
                }

                override fun onError(e: CometChatException) {
                    Log.d(TAG, "Group Member list fetching failed with exception: " + e.message)
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
            })
        }

    /**
     * Incase if user is blocked already, then this method is used to unblock the user .
     *
     * @see CometChat.unblockUsers
     */
    private fun unblockUser() {
        val uids = ArrayList<String?>()
        uids.add(Id)
        unblockUsers(uids, object : CallbackListener<HashMap<String?, String?>?>() {
            override fun onSuccess(stringStringHashMap: HashMap<String?, String?>?) {
//                Snackbar.make(rvChatListView!!, String.format(resources.getString(R.string.unblocked_successfully), name), Snackbar.LENGTH_LONG).show()
                blockUserLayout?.visibility = View.GONE
                isBlockedByMe = false
                messagesRequest = null
            }

            override fun onError(e: CometChatException) {
                ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
            }
        })
    }

    /**
     * This method is used to set GroupMember names as subtitle in toolbar.
     *
     * @param users
     */
    private fun setSubTitle(users: Array<String?>) {
        if (users != null && users.size != 0) {

            memberNames = users.joinToString(", ")
            tvStatus?.text = memberNames
        }
    }

    /**
     * This method is used to fetch message of users & groups. For user it fetches previous 100 messages at
     * a time and for groups it fetches previous 30 messages. You can change limit of messages by modifying
     * number in `setLimit()`
     * This method also mark last message as read using markMessageAsRead() present in this class.
     * So all the above messages get marked as read.
     *
     * @see MessagesRequest.fetchPrevious
     */
    private fun fetchMessage() {
        if (messagesRequest == null) {
            if (type != null) {
                if (type == CometChatConstants.RECEIVER_TYPE_USER) {
                    messagesRequest = if (!FeatureRestriction.isHideDeletedMessagesEnabled()) {
                        MessagesRequestBuilder().setLimit(LIMIT).setUID(Id)
                                .hideReplies(true)
                                .setTypes(UIKitConstants.MessageRequest.messageTypesForUser)
                                .setCategories(UIKitConstants.MessageRequest.messageCategoriesForUser)
                                .build()
                    } else {
                        MessagesRequestBuilder().setLimit(LIMIT).setUID(Id)
                                .hideReplies(true)
                                .hideDeletedMessages(true)
                                .setTypes(UIKitConstants.MessageRequest.messageTypesForUser)
                                .setCategories(UIKitConstants.MessageRequest.messageCategoriesForUser)
                                .build()
                    }
                }
                else {
                    messagesRequest = if (!FeatureRestriction.isHideDeletedMessagesEnabled()) {
                        MessagesRequestBuilder().setLimit(LIMIT).setGUID(Id)
                                .hideReplies(true)
                                .setTypes(UIKitConstants.MessageRequest.messageTypesForGroup)
                                .setCategories(UIKitConstants.MessageRequest.messageCategoriesForGroup)
                                .build()
                    } else {
                        MessagesRequestBuilder().setLimit(LIMIT).setGUID(Id)
                                .hideReplies(true)
                                .hideDeletedMessages(true)
                                .hideMessagesFromBlockedUsers(true)
                                .setTypes(UIKitConstants.MessageRequest.messageTypesForGroup)
                                .setCategories(UIKitConstants.MessageRequest.messageCategoriesForGroup)
                                .build()
                    }
                }
            }
        }
        messagesRequest?.fetchPrevious(object : CallbackListener<List<BaseMessage>>() {
            override fun onSuccess(baseMessages: List<BaseMessage>) {
                for (i in baseMessages) {
                    Log.d(TAG, "onSuccess: basemsgtype " + i.type)
                }
                isInProgress = false
                initMessageAdapter(baseMessages)
                if (baseMessages.isNotEmpty()) {
                    stopHideShimmer()
                    val baseMessage = baseMessages[baseMessages.size - 1]
                    markMessageAsRead(baseMessage)
                }
                if (baseMessages.isEmpty()) {
                    stopHideShimmer()
                    isNoMoreMessages = true
                }
            }

            override fun onError(e: CometChatException) {
                Log.d(TAG, "onError: " + e.message)
            }
        })
    }

    private fun stopHideShimmer() {
        messageShimmer?.stopShimmer()
        messageShimmer?.visibility = View.GONE
    }

    private fun getSmartReplyList(baseMessage: BaseMessage) {
        val extensionList = Extensions.extensionCheck(baseMessage)
        if (extensionList != null && extensionList.containsKey("smartReply")) {
            rvSmartReply!!.visibility = View.VISIBLE
            val replyObject = extensionList["smartReply"]
            val replyList: MutableList<String> = ArrayList()
            try {
                replyList.add(replyObject!!.getString("reply_positive"))
                replyList.add(replyObject.getString("reply_neutral"))
                replyList.add(replyObject.getString("reply_negative"))
            } catch (e: Exception) {
                Log.e(TAG, "onSuccess: " + e.message)
            }
            setSmartReplyAdapter(replyList)
        } else {
            rvSmartReply?.visibility = View.GONE
        }
    }

    private fun setSmartReplyAdapter(replyList: List<String>?) {
        rvSmartReply?.setSmartReplyList(replyList)
        scrollToBottom()
    }

    /**
     * This method is used to initialize the message adapter if it is empty else it helps
     * to update the messagelist in adapter.
     *
     * @param messageList is a list of messages which will be added.
     */
    private fun initMessageAdapter(messageList: List<BaseMessage>) {
        if (messageAdapter == null) {
            messageAdapter = activity?.let { MessageAdapter(it, messageList, CometChatMessageList::class.java.name) }
            rvChatListView?.adapter = messageAdapter
            stickyHeaderDecoration = StickyHeaderDecoration(messageAdapter!!)
            rvChatListView?.addItemDecoration(stickyHeaderDecoration!!, 0)
            scrollToBottom()
            messageAdapter?.notifyDataSetChanged()
        } else {
            messageAdapter?.updateList(messageList)
        }
        if (!isBlockedByMe && rvSmartReply?.adapter?.itemCount == 0) {
            val lastMessage = messageAdapter?.lastMessage
            checkSmartReply(lastMessage)
        }
    }

    /**
     * This method is used to send typing indicator to other users and groups.
     *
     * @param isEnd is boolean which is used to differentiate between startTyping & endTyping Indicators.
     * @see CometChat.startTyping
     * @see CometChat.endTyping
     */
    private fun sendTypingIndicator(isEnd: Boolean) {
        if (isEnd) {
            if (type == CometChatConstants.RECEIVER_TYPE_USER) {
                endTyping(TypingIndicator(Id, CometChatConstants.RECEIVER_TYPE_USER))
            } else {
                endTyping(TypingIndicator(Id, CometChatConstants.RECEIVER_TYPE_GROUP))
            }
        } else {
            if (type == CometChatConstants.RECEIVER_TYPE_USER) {
                startTyping(TypingIndicator(Id, CometChatConstants.RECEIVER_TYPE_USER))
            } else {
                startTyping(TypingIndicator(Id, CometChatConstants.RECEIVER_TYPE_GROUP))
            }
        }
    }

    private fun endTypingTimer() {
        if (typingTimer != null) {
            typingTimer?.schedule(object : TimerTask() {
                override fun run() {
                    sendTypingIndicator(true)
                }
            }, 2000)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: " + data?.data)
        when (requestCode) {
            UIKitConstants.RequestCode.AUDIO -> if (data != null) {
                intentRequestCode = UIKitConstants.RequestCode.AUDIO
                val file = MediaUtils.getRealPath(getContext(), data.data)
                val cr = activity?.contentResolver
                sendMediaMessage(file, CometChatConstants.MESSAGE_TYPE_AUDIO)
            }
            UIKitConstants.RequestCode.GALLERY -> if (data != null) {
                intentRequestCode = UIKitConstants.RequestCode.GALLERY
                Log.e(TAG, "onActivityResult:intent $data")
                val file = MediaUtils.getRealPath(context, data.data)
                val cr = activity?.contentResolver
                val mimeType = cr?.getType(data.data!!)
                Log.e(TAG, "onActivityResult:file $file")
                if (mimeType != null && mimeType.contains("image")) {
                    if (file.exists()) sendMediaMessage(file, CometChatConstants.MESSAGE_TYPE_IMAGE) else Snackbar.make(rvChatListView!!, R.string.file_not_exist, Snackbar.LENGTH_LONG).show()
                } else {
                    if (file.exists()) sendMediaMessage(file, CometChatConstants.MESSAGE_TYPE_VIDEO) else Snackbar.make(rvChatListView!!, R.string.file_not_exist, Snackbar.LENGTH_LONG).show()
                }
            }
            UIKitConstants.RequestCode.CAMERA -> {
                intentRequestCode = UIKitConstants.RequestCode.CAMERA
                val file: File
                file = if (Build.VERSION.SDK_INT >= 29) {
                    MediaUtils.getRealPath(context, MediaUtils.uri)
                } else {
                    File(MediaUtils.pictureImagePath)
                }
                if (file.exists()) sendMediaMessage(file, CometChatConstants.MESSAGE_TYPE_IMAGE) else Snackbar.make(rvChatListView!!, R.string.file_not_exist, Snackbar.LENGTH_LONG).show()
            }
            UIKitConstants.RequestCode.FILE -> {
                intentRequestCode = UIKitConstants.RequestCode.FILE
                if (data != null) sendMediaMessage(MediaUtils.getRealPath(activity, data.data), CometChatConstants.MESSAGE_TYPE_FILE)
            }
            UIKitConstants.RequestCode.BLOCK_USER -> name = data?.getStringExtra("")
            UIKitConstants.RequestCode.LOCATION -> {
                locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(context, getString(R.string.gps_enabled), Toast.LENGTH_SHORT).show()
                    getLocation()
                } else {
                    Toast.makeText(context, getString(R.string.gps_disabled), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * This method is used to send media messages to other users and group.
     *
     * @param file     is an object of File which is been sent within the message.
     * @param filetype is a string which indicate a type of file been sent within the message.
     * @see CometChat.sendMediaMessage
     * @see MediaMessage
     */
    private fun sendMediaMessage(file: File, filetype: String) {
//        val progressDialog: ProgressDialog
//        progressDialog = ProgressDialog.show(context, "", "Sending Media Message")
        val mediaMessage: MediaMessage
        mediaMessage = if (type.equals(CometChatConstants.RECEIVER_TYPE_USER, ignoreCase = true)) MediaMessage(Id, file, filetype, CometChatConstants.RECEIVER_TYPE_USER) else MediaMessage(Id, file, filetype, CometChatConstants.RECEIVER_TYPE_GROUP)
        val jsonObject = JSONObject()
        try {
            jsonObject.put("path", file.absolutePath)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        mediaMessage.metadata = jsonObject
        mediaMessage.sender = loggedInUser
        mediaMessage.muid = System.currentTimeMillis().toString()
        mediaMessage.category = CometChatConstants.CATEGORY_MESSAGE
        if (messageAdapter != null) {
            messageAdapter?.addMessage(mediaMessage)
            scrollToBottom()
        }
        sendMediaMessage(mediaMessage, object : CallbackListener<MediaMessage>() {
            override fun onSuccess(mediaMessage: MediaMessage) {
//                progressDialog.dismiss()
                Log.d(TAG, "sendMediaMessage onSuccess: $mediaMessage")
                messageAdapter?.updateSentMessage(mediaMessage)
            }

            override fun onError(e: CometChatException) {
//                progressDialog.dismiss()
                if (activity != null) {
//                    Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                    ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                }
            }
        })
    }

    /**
     * This method is used to get details of reciever.
     *
     * @see CometChat.getUser
     */
    private val user: Unit
        private get() {
            getUser(Id, object : CallbackListener<User>() {
                override fun onSuccess(user: User) {
                    if (activity != null) {
                        if (user.isBlockedByMe) {
                            isBlockedByMe = true
                            rvSmartReply?.visibility = View.GONE
                            toolbar?.isSelected = false
                            blockedUserName?.text = "You've blocked " + user.name
                            blockUserLayout?.visibility = View.VISIBLE
                        } else {
                            isBlockedByMe = false
                            blockUserLayout?.visibility = View.GONE
                            avatarUrl = user.avatar
                            FeatureRestriction.isUserPresenceEnabled(object : FeatureRestriction.OnSuccessListener {
                                override fun onSuccess(p0: Boolean) {
                                    if (p0) {
                                        if (user.status == CometChatConstants.USER_STATUS_ONLINE)
                                            tvStatus!!.setTextColor(activity!!.resources.getColor(R.color.colorPrimary))
                                        status = user.status.toString()
                                        tvStatus!!.text = status
                                    }
                                }

                            })
                            setAvatar()

                        }
                        name = user.name
                        tvName?.text = name
                        Log.d(TAG, "onSuccess: $user")
                    }
                }

                override fun onError(e: CometChatException) {
//                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                }
            })
        }

    private fun setAvatar() {
        if (avatarUrl != null && !avatarUrl!!.isEmpty()) userAvatar!!.setAvatar(avatarUrl!!) else {
            userAvatar?.setInitials(name!!)
        }
    }

    /**
     * This method is used to get Group Details.
     *
     * @see CometChat.getGroup
     */
    private val group: Unit
        private get() {
            getGroup(Id!!, object : CallbackListener<Group>() {
                override fun onSuccess(group: Group) {
                    Log.e(TAG, "getGroup: "+group.toString() )
                    if (activity != null) {
                        name = group.name
                        avatarUrl = group.icon
                        loggedInUserScope = group.scope
                        groupOwnerId = group.owner
                        tvName?.text = name
                        setAvatar()
                    }
                }

                override fun onError(e: CometChatException) {
                    ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                }
            })
        }

    /**
     * This method is used to send Text Message to other users and groups.
     *
     * @param message is a String which is been sent as message.
     * @see TextMessage
     *
     * @see CometChat.sendMessage
     */
    private fun sendMessage(message: String) {
        val textMessage: TextMessage
        textMessage = if (type.equals(CometChatConstants.RECEIVER_TYPE_USER, ignoreCase = true)) TextMessage(Id, message, CometChatConstants.RECEIVER_TYPE_USER) else TextMessage(Id, message, CometChatConstants.RECEIVER_TYPE_GROUP)
        sendTypingIndicator(true)
        textMessage.category = CometChatConstants.CATEGORY_MESSAGE
        textMessage.sender = loggedInUser
        textMessage.muid = System.currentTimeMillis().toString()
        if (messageAdapter != null) {
            MediaUtils.playSendSound(context, R.raw.outgoing_message)
            messageAdapter?.addMessage(textMessage)
            scrollToBottom()
        }
        sendMessage(textMessage, object : CallbackListener<TextMessage?>() {
            override fun onSuccess(textMessage: TextMessage?) {
                isSmartReplyClicked = false
                rvSmartReply?.visibility = View.GONE
                Log.e(TAG, "onSuccess: " + textMessage.toString())
                if (messageAdapter != null) {
                    messageAdapter?.updateSentMessage(textMessage)
                }
            }

            override fun onError(e: CometChatException) {
//                Log.d(TAG, "onError: " + e.message)
//                ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                textMessage.sentAt = -1
                if (e.code.equals("ERROR_INTERNET_UNAVAILABLE", ignoreCase = true)) {
                    ErrorMessagesUtils.showCometChatErrorDialog(context,
                            getString(R.string.please_check_your_internet_connection))
                } else if (!e.code.equals("ERR_BLOCKED_BY_EXTENSION", ignoreCase = true)) {
                    if (messageAdapter == null) {
                        Log.e(TAG, "onError: MessageAdapter is null")
                    } else {
                        textMessage.sentAt = -1
                        messageAdapter?.updateSentMessage(textMessage)
                    }
                } else if (messageAdapter != null) {
                    messageAdapter?.removeMessage(textMessage)
                }
            }
        })
    }

    /**
     * This method is used to delete the message.
     *
     * @param baseMessage is an object of BaseMessage which is being used to delete the message.
     * @see BaseMessage
     *
     * @see CometChat.deleteMessage
     */
    private fun deleteMessage(baseMessage: BaseMessage?) {
        deleteMessage(baseMessage!!.id, object : CallbackListener<BaseMessage?>() {
            override fun onSuccess(baseMessage: BaseMessage?) {
//                if (messageAdapter != null) messageAdapter?.setUpdatedMessage(baseMessage!!)
                if (messageAdapter != null) messageAdapter?.removeMessage(baseMessage!!)
            }

            override fun onError(e: CometChatException) {
                Log.d(TAG, "onError: " + e.message)
                ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
            }
        })
    }

    /**
     * This method is used to edit the message. This methods takes old message and change text of old
     * message with new message i.e String and update it.
     *
     * @param baseMessage is an object of BaseMessage, It is a old message which is going to be edited.
     * @param message     is String, It is a new message which will be replaced with text of old message.
     * @see TextMessage
     *
     * @see BaseMessage
     *
     * @see CometChat.editMessage
     */
    private fun editMessage(baseMessage: BaseMessage?, message: String) {
        isEdit = false
        val textMessage: TextMessage
        textMessage = if (baseMessage?.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER, ignoreCase = true)) TextMessage(baseMessage!!.receiverUid, message, CometChatConstants.RECEIVER_TYPE_USER) else TextMessage(baseMessage!!.receiverUid, message, CometChatConstants.RECEIVER_TYPE_GROUP)
        sendTypingIndicator(true)
        textMessage.id = baseMessage.id
        editMessage(textMessage, object : CallbackListener<BaseMessage>() {
            override fun onSuccess(message: BaseMessage) {
                if (messageAdapter != null) {
                    Log.e(TAG, "onSuccess: $message")
                    messageAdapter?.setUpdatedMessage(message)
                }
            }

            override fun onError(e: CometChatException) {
                Log.d(TAG, "onError: " + e.message)
            }
        })
    }

    /**
     * This method is used to send reply message by link previous message with new message.
     * @param baseMessage is a linked message
     * @param message is a String. It will be new message sent as reply.
     */
    private fun replyMessage(baseMessage: BaseMessage?, message: String) {
        isReply = false
        try {
            var textMessage: TextMessage
            textMessage = if (type.equals(CometChatConstants.RECEIVER_TYPE_USER, ignoreCase = true)) TextMessage(Id, message, CometChatConstants.RECEIVER_TYPE_USER) else TextMessage(Id!!, message, CometChatConstants.RECEIVER_TYPE_GROUP)
            textMessage.category = CometChatConstants.CATEGORY_MESSAGE
            textMessage.sender = loggedInUser
            textMessage.muid = System.currentTimeMillis().toString()
            var jsonObject = JSONObject()
            var replyObject = JSONObject()

            if (isReplyPrivately)
                jsonObject.put("replyToMessage", baseMessage?.rawMessage)
            else
                jsonObject.put("reply-message", baseMessage?.rawMessage)

            textMessage.metadata = jsonObject
            sendTypingIndicator(true)
            if (messageAdapter != null) {
                if (UIKitConstants.Sounds.enableMessageSounds) MediaUtils.playSendSound(context, R.raw.outgoing_message)
                messageAdapter?.addMessage(textMessage)
                scrollToBottom()
            }
            sendMessage(textMessage, object : CallbackListener<TextMessage?>() {
                override fun onSuccess(textMessage: TextMessage?) {
                    Log.d(TAG, "onSuccess: reply message" + textMessage.toString())
                    if (messageAdapter != null) {
//                        if (UIKitConstants.Sounds.enableMessageSounds) MediaUtils.playSendSound(context, R.raw.outgoing_message)
//                        messageAdapter?.addMessage(textMessage!!)
//                        scrollToBottom()
                        messageAdapter?.updateSentMessage(textMessage)
                    }
                }

                override fun onError(e: CometChatException) {
                    Log.e(TAG, "onError: " + e.message)
                    textMessage.sentAt = -1
                    if (e.code.equals("ERROR_INTERNET_UNAVAILABLE", ignoreCase = true)) {
                        ErrorMessagesUtils.showCometChatErrorDialog(context,
                                getString(R.string.please_check_your_internet_connection))
                    } else if (!e.code.equals("ERR_BLOCKED_BY_EXTENSION", ignoreCase = true)) {
                        if (messageAdapter == null) {
                            Log.e(TAG, "onError: MessageAdapter is null")
                        } else {
                            textMessage.sentAt = -1
                            messageAdapter?.updateSentMessage(textMessage)
                        }
                    } else if (messageAdapter != null) {
                        messageAdapter?.removeMessage(textMessage)
                    }
                }
            })
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "replyMessage: " + e.message)
        }
    }

    private fun scrollToBottom() {
        if (messageAdapter != null && messageAdapter!!.itemCount > 0) {
            rvChatListView?.scrollToPosition(messageAdapter!!.itemCount - 1)
        }
    }

    /**
     * This method is used to recieve real time group events like onMemberAddedToGroup, onGroupMemberJoined,
     * onGroupMemberKicked, onGroupMemberLeft, onGroupMemberBanned, onGroupMemberUnbanned,
     * onGroupMemberScopeChanged.
     *
     * @see CometChat.addGroupListener
     */
    private fun addGroupListener() {
        addGroupListener(TAG, object : GroupListener() {
            override fun onGroupMemberJoined(action: Action, joinedUser: User, joinedGroup: Group) {
                super.onGroupMemberJoined(action, joinedUser, joinedGroup)
                if (joinedGroup.guid == Id) tvStatus?.text = memberNames + "," + joinedUser.name
                onMessageReceived(action)
            }

            override fun onGroupMemberLeft(action: Action, leftUser: User, leftGroup: Group) {
                super.onGroupMemberLeft(action, leftUser, leftGroup)
                Log.d(TAG, "onGroupMemberLeft: " + leftUser.name)
                if (leftGroup.guid == Id) {
                    if (memberNames != null) tvStatus?.text = memberNames?.replace("," + leftUser.name, "")
                }
                onMessageReceived(action)
            }

            override fun onGroupMemberKicked(action: Action, kickedUser: User, kickedBy: User, kickedFrom: Group) {
                super.onGroupMemberKicked(action, kickedUser, kickedBy, kickedFrom)
                Log.d(TAG, "onGroupMemberKicked: " + kickedUser.name)
                if (kickedUser.uid == getLoggedInUser().uid) {
                    if (activity != null) activity?.finish()
                }
                if (kickedFrom.guid == Id) tvStatus?.text = memberNames?.replace("," + kickedUser.name, "")
                onMessageReceived(action)
            }

            override fun onGroupMemberBanned(action: Action, bannedUser: User, bannedBy: User, bannedFrom: Group) {
                if (bannedUser.uid == getLoggedInUser().uid) {
                    if (activity != null) {
                        activity?.onBackPressed()
                        Toast.makeText(activity, "You have been banned", Toast.LENGTH_SHORT).show()
                    }
                }
                onMessageReceived(action)
            }

            override fun onGroupMemberUnbanned(action: Action, unbannedUser: User, unbannedBy: User, unbannedFrom: Group) {
                onMessageReceived(action)
            }

            override fun onGroupMemberScopeChanged(action: Action, updatedBy: User, updatedUser: User, scopeChangedTo: String, scopeChangedFrom: String, group: Group) {
                onMessageReceived(action)
            }

            override fun onMemberAddedToGroup(action: Action, addedby: User, userAdded: User, addedTo: Group) {
                if (addedTo.guid == Id) tvStatus?.text = memberNames + "," + userAdded.name
                onMessageReceived(action)
            }
        })
    }

    /**
     * This method is used to get real time user status i.e user is online or offline.
     *
     * @see CometChat.addUserListener
     */
    private fun addUserListener() {
        if (type == CometChatConstants.RECEIVER_TYPE_USER) {
            addUserListener(TAG, object : UserListener() {
                override fun onUserOnline(user: User) {
                    Log.d(TAG, "onUserOnline: $user")
                    if (user.uid == Id) {
                        tvStatus?.text = user.status
                        tvStatus?.setTextColor(resources.getColor(R.color.colorPrimary))
                    }
                }

                override fun onUserOffline(user: User) {
                    Log.d(TAG, "onUserOffline: $user")
                    if (user.uid == Id) {
                        if (Utils.isDarkMode(context!!)) tvStatus?.setTextColor(resources.getColor(R.color.textColorWhite)) else tvStatus?.setTextColor(resources.getColor(android.R.color.black))
                        tvStatus?.text = user.status
                    }
                }
            })
        }
    }

    /**
     * This method is used to mark users & group message as read.
     *
     * @param baseMessage is object of BaseMessage.class. It is message which is been marked as read.
     */
    private fun markMessageAsRead(baseMessage: BaseMessage) {
        if (type == CometChatConstants.RECEIVER_TYPE_USER) markAsRead(baseMessage.id, baseMessage.sender.uid, baseMessage.receiverType) else markAsRead(baseMessage.id, baseMessage.receiverUid, baseMessage.receiverType)
    }

    /**
     * This method is used to add message listener to recieve real time messages between users &
     * groups. It also give real time events for typing indicators, edit message, delete message,
     * message being read & delivered.
     *
     * @see CometChat.addMessageListener
     */
    private fun addMessageListener() {
        addMessageListener(TAG, object : MessageListener() {
            override fun onTextMessageReceived(message: TextMessage) {
                Log.d(TAG, "onTextMessageReceived: $message")
                onMessageReceived(message)
            }

            override fun onMediaMessageReceived(message: MediaMessage) {
                Log.d(TAG, "onMediaMessageReceived: $message")
                onMessageReceived(message)
            }

            override fun onCustomMessageReceived(message: CustomMessage?) {
                Log.d(TAG, "onCustomMessageReceived: " + message.toString())
                onMessageReceived(message!!)
            }

            override fun onTypingStarted(typingIndicator: TypingIndicator) {
                Log.e(TAG, "onTypingStarted: $typingIndicator")
                setTypingIndicator(typingIndicator, true)
            }

            override fun onTypingEnded(typingIndicator: TypingIndicator) {
                Log.d(TAG, "onTypingEnded: $typingIndicator")
                setTypingIndicator(typingIndicator, false)
            }

            override fun onMessagesDelivered(messageReceipt: MessageReceipt) {
                Log.d(TAG, "onMessagesDelivered: $messageReceipt")
                setMessageReciept(messageReceipt)
            }

            override fun onMessagesRead(messageReceipt: MessageReceipt) {
                Log.e(TAG, "onMessagesRead: $messageReceipt")
                setMessageReciept(messageReceipt)
            }

            override fun onMessageEdited(message: BaseMessage) {
                Log.d(TAG, "onMessageEdited: $message")
                updateMessage(message)
            }

            override fun onMessageDeleted(message: BaseMessage) {
                Log.d(TAG, "onMessageDeleted: ")
                updateMessage(message)
            }
        })
    }

    private fun setMessageReciept(messageReceipt: MessageReceipt) {
        if (messageAdapter != null) {
            if (messageReceipt.receivertype == CometChatConstants.RECEIVER_TYPE_USER) {
                if (Id != null && messageReceipt.sender.uid == Id) {
                    if (messageReceipt.receiptType == MessageReceipt.RECEIPT_TYPE_DELIVERED) messageAdapter?.setDeliveryReceipts(messageReceipt) else messageAdapter?.setReadReceipts(messageReceipt)
                }
            }
        }
    }

    private fun setTypingIndicator(typingIndicator: TypingIndicator, isShow: Boolean) {
        if (typingIndicator.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER, ignoreCase = true)) {
            Log.e(TAG, "onTypingStarted: $typingIndicator")
            if (Id != null && Id.equals(typingIndicator.sender.uid, ignoreCase = true)) typingIndicator(typingIndicator, isShow)
        } else {
            if (Id != null && Id.equals(typingIndicator.receiverId, ignoreCase = true)) typingIndicator(typingIndicator, isShow)
        }
    }

    private fun onMessageReceived(message: BaseMessage) {
        MediaUtils.playSendSound(context, R.raw.incoming_message)
        if (message.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
            if (Id != null && Id.equals(message.sender.uid, ignoreCase = true)) {
                setMessage(message)
            } else if (Id != null && Id.equals(message.receiverUid, ignoreCase = true) && message.sender.uid.equals(loggedInUser.uid, ignoreCase = true)) {
                setMessage(message)
            }
        } else {
            if (Id != null && Id.equals(message.receiverUid, ignoreCase = true)) {
                setMessage(message)
            }
        }
    }

    /**
     * This method is used to update edited message by calling `setEditMessage()` of adapter
     *
     * @param message is an object of BaseMessage and it will replace with old message.
     * @see BaseMessage
     */
    private fun updateMessage(message: BaseMessage) {
        messageAdapter?.setUpdatedMessage(message)
    }

    /**
     * This method is used to mark message as read before adding them to list. This method helps to
     * add real time message in list.
     *
     * @param message is an object of BaseMessage, It is recieved from message listener.
     * @see BaseMessage
     */
    private fun setMessage(message: BaseMessage) {
        if (message.parentMessageId == 0) {
            if (messageAdapter != null) {
                messageAdapter?.addMessage(message)
                checkSmartReply(message)
                markMessageAsRead(message)
                if (messageAdapter!!.itemCount - 1 - (rvChatListView?.layoutManager as LinearLayoutManager?)!!.findLastVisibleItemPosition() < 5) scrollToBottom()
            } else {
                messageList.add(message)
                initMessageAdapter(messageList)
            }
        }
    }

    private fun checkSmartReply(lastMessage: BaseMessage?) {
        if (lastMessage != null && lastMessage.sender.uid != loggedInUser.uid) {
            if (lastMessage.metadata != null) {
                getSmartReplyList(lastMessage)
            }
        }
    }

    /**
     * This method is used to display typing status to user.
     *
     * @param show is boolean, If it is true then **is Typing** will be shown to user
     * If it is false then it will show user status i.e online or offline.
     */
    private fun typingIndicator(typingIndicator: TypingIndicator, show: Boolean) {
        if (messageAdapter != null) {
            if (show) {
                if (typingIndicator.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                    if (typingIndicator.metadata == null) {
                        FeatureRestriction.isTypingIndicatorsEnabled(object : FeatureRestriction.OnSuccessListener{
                            override fun onSuccess(p0: Boolean) {
                                if (p0) {
                                    tvStatus?.visibility = View.VISIBLE
                                    tvStatus?.text = "is Typing..."
                                }
                            }

                        })
                    }
                    else
                        setLiveReaction()
                } else {
                    if (typingIndicator.metadata == null) {
                        FeatureRestriction.isTypingIndicatorsEnabled(object : FeatureRestriction.OnSuccessListener{
                            override fun onSuccess(p0: Boolean) {
                                if (p0) {
                                    tvStatus?.visibility = View.VISIBLE
                                    tvStatus?.text = typingIndicator.sender.name + " is Typing..."
                                }
                            }

                        })
                    }
                    else
                        setLiveReaction()
                }
            } else {
                if (typingIndicator.receiverType == CometChatConstants.RECEIVER_TYPE_USER){
                    if (typingIndicator.metadata == null) {
                        FeatureRestriction.isUserPresenceEnabled(object : FeatureRestriction.OnSuccessListener {
                            override fun onSuccess(p0: Boolean) {
                                if (p0) tvStatus!!.text = status else tvStatus?.visibility = View.GONE
                            }
                        })
                    }
                    else{
                        val animator = ObjectAnimator.ofFloat(liveReactionLayout!!, "alpha", 0.2f)
                        animator.duration = 700
                        animator.repeatCount = 3
                        animator.start()
                        animator.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                super.onAnimationEnd(animation)
                                if (imageToFly != null) imageToFly?.clearAnimation()
                                liveReactionLayout?.removeAllViews()
                            }
                        })
                    }
                }
                else{
                    if (typingIndicator.metadata == null)
                        tvStatus?.text = memberNames
                    else{
                        val animator = ObjectAnimator.ofFloat(liveReactionLayout!!, "alpha", 0.2f)
                        animator.duration = 700
                        animator.start()
                        animator.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                super.onAnimationEnd(animation)
                                if (imageToFly != null) imageToFly?.clearAnimation()
                                liveReactionLayout?.removeAllViews()
                            }
                        })
                    }
                }
            }
        }
    }

    /**
     * This method is used to remove message listener
     *
     * @see CometChat.removeMessageListener
     */
    private fun removeMessageListener() {
        removeMessageListener(TAG)
    }

    /**
     * This method is used to remove user presence listener
     *
     * @see CometChat.removeUserListener
     */
    private fun removeUserListener() {
        removeUserListener(TAG)
    }

    override fun onPause() {
        Log.d(TAG, "onPause: ")
        super.onPause()
        if (messageAdapter != null) messageAdapter?.stopPlayingAudio()
        removeMessageListener()
        removeUserListener()
        removeGroupListener()
        sendTypingIndicator(true)

        if (cometChatMessageActions != null) cometChatMessageActions?.dismiss()
    }

    private fun removeGroupListener() {
        removeGroupListener(TAG)
    }

    override fun onResume() {
//        onCloseAction()
        super.onResume()
        Log.d(TAG, "onResume: ")
        Log.e(TAG, "onResume: " + intentRequestCode)
        checkOnGoingCall()
        stickyHeaderDecoration?.let { this.rvChatListView?.removeItemDecoration(it) }
        if (!(intentRequestCode == UIKitConstants.RequestCode.AUDIO || intentRequestCode == UIKitConstants.RequestCode.GALLERY ||
                intentRequestCode == UIKitConstants.RequestCode.FILE || intentRequestCode == UIKitConstants.RequestCode.CAMERA)) {
            messageAdapter = null
            messagesRequest = null
            fetchMessage()
        }
        addMessageListener()

        if (cometChatMessageActions != null && cometChatMessageActions?.fragmentManager != null) cometChatMessageActions?.dismiss()

        if (type != null) {
            if (type == CometChatConstants.RECEIVER_TYPE_USER) {
                FeatureRestriction.isUserPresenceEnabled(object : FeatureRestriction.OnSuccessListener {
                    override fun onSuccess(p0: Boolean) {
                        if (p0) {
                            addUserListener()
                            tvStatus?.text = status
                        }
                    }
                })
                Thread(Runnable { user }).start()
            } else {
                if (!FeatureRestriction.isGroupActionMessagesEnabled())
                    addGroupListener()
                Thread(Runnable { group }).start()
                Thread(Runnable { member }).start()
            }
        }
    }

    fun onCloseAction() {
        if (messageAdapter != null) messageAdapter?.clearLongClickSelectedItem()
        composeBox?.visibility = View.VISIBLE
//        rlMessageAction?.visibility = View.GONE
        userAvatar?.visibility = View.VISIBLE
        ivCloseMessageAction?.visibility = View.GONE
//        ivCopyMessageAction?.visibility = View.GONE
        if (activity != null && (activity as AppCompatActivity?)?.supportActionBar != null) (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.c = context
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onClick(view: View) {
        val id = view.id
//        if (id == R.id.delete_message) {
//            deleteMessage(baseMessage)
//            if (messageAdapter != null) {
//                messageAdapter!!.clearLongClickSelectedItem()
//                messageAdapter!!.notifyDataSetChanged()
//            }
//            onCloseAction()
//        }
//        else if (id == R.id.forward_message) {
//            val intent = Intent(getContext(), CometChatForwardMessageScreenActivity::class.java)
//            if (baseMessage!!.type == CometChatConstants.MESSAGE_TYPE_TEXT) {
//                intent.putExtra(CometChatConstants.MESSAGE_TYPE_TEXT, (baseMessage as TextMessage?)!!.text)
//                intent.putExtra(StringContract.IntentStrings.TYPE, CometChatConstants.MESSAGE_TYPE_TEXT)
//            } else if (baseMessage!!.type == CometChatConstants.MESSAGE_TYPE_IMAGE) {
//                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, (baseMessage as MediaMessage?)!!.attachment.fileName)
//                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL, (baseMessage as MediaMessage?)!!.attachment.fileUrl)
//                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE, (baseMessage as MediaMessage?)!!.attachment.fileMimeType)
//                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, (baseMessage as MediaMessage?)!!.attachment.fileExtension)
//                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, (baseMessage as MediaMessage?)!!.attachment.fileSize)
//                intent.putExtra(StringContract.IntentStrings.TYPE, CometChatConstants.MESSAGE_TYPE_IMAGE)
//            }
//            startActivity(intent)
//        } else if (id == R.id.edit_message) {
//            if (baseMessage != null && baseMessage!!.type == CometChatConstants.MESSAGE_TYPE_TEXT) {
//                isEdit = true
//                tvMessageTitle!!.text = resources.getString(R.string.edit_message)
//                tvMessageSubTitle!!.text = (baseMessage as TextMessage).text
//                editMessageLayout!!.visibility = View.VISIBLE
//                if (messageAdapter != null) {
//                    messageAdapter!!.setSelectedMessage(baseMessage!!.getId())
//                    messageAdapter!!.notifyDataSetChanged()
//                }
//            }
//            onCloseAction()
//        }
//        else if (id == R.id.iv_copy_message_action) {
//            var message = ""
//            for (bMessage in baseMessages!!) {
//                if (bMessage.deletedAt == 0L && bMessage is TextMessage) {
//                    message = message+" [ "+Utils.getLastMessageDate(bMessage.getSentAt())+" ] "+
//                            bMessage.getSender().name +": "+bMessage.text+"\n\n";
//                }
//            }
//            Log.e(TAG, "onCopy: $message")
//            val clipboardManager = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//            val clipData = ClipData.newPlainText("MessageAdapter", message)
//            clipboardManager.setPrimaryClip(clipData)
//            Toast.makeText(context, resources.getString(R.string.text_copied_clipboard), Toast.LENGTH_LONG).show()
//            if (messageAdapter != null) {
//                messageAdapter!!.clearLongClickSelectedItem()
//                messageAdapter!!.notifyDataSetChanged()
//            }
//            onCloseAction()
//        }
//        else if (id == R.id.iv_close_message_action) {
//            if (messageAdapter != null) {
//                messageAdapter!!.clearLongClickSelectedItem()
//                messageAdapter!!.notifyDataSetChanged()
//            }
//            onCloseAction()
//        }
        if (id == R.id.iv_message_close) {
            if (messageAdapter != null) {
                messageAdapter?.clearLongClickSelectedItem()
                messageAdapter?.notifyDataSetChanged()
            }
            isEdit = false
            baseMessage = null
            editMessageLayout?.visibility = View.GONE
        } else if (id == R.id.iv_reply_close) {
            if (messageAdapter != null) {
                messageAdapter?.clearLongClickSelectedItem()
                messageAdapter?.notifyDataSetChanged()
            }
            isReply = false
            baseMessage = null
            replyMessageLayout?.visibility = View.GONE
        }
        else if (id == R.id.btn_unblock_user) {
            unblockUser()
        } else if (id == R.id.iv_user_info) {
            if (type == CometChatConstants.RECEIVER_TYPE_USER) {
                val intent = Intent(context, CometChatUserDetailScreenActivity::class.java)
                intent.putExtra(UIKitConstants.IntentStrings.UID, Id)
                intent.putExtra(UIKitConstants.IntentStrings.NAME, name)
                intent.putExtra(UIKitConstants.IntentStrings.AVATAR, avatarUrl)
                intent.putExtra(UIKitConstants.IntentStrings.IS_BLOCKED_BY_ME, isBlockedByMe)
                intent.putExtra(UIKitConstants.IntentStrings.STATUS, status)
                intent.putExtra(UIKitConstants.IntentStrings.TYPE, type)
                intent.putExtra(UIKitConstants.IntentStrings.LINK, link)
                startActivity(intent)
            } else {
                val intent = Intent(context, CometChatGroupDetailActivity::class.java)
                intent.putExtra(UIKitConstants.IntentStrings.GUID, Id)
                intent.putExtra(UIKitConstants.IntentStrings.NAME, name)
                intent.putExtra(UIKitConstants.IntentStrings.AVATAR, avatarUrl)
                intent.putExtra(UIKitConstants.IntentStrings.TYPE, type)
                intent.putExtra(UIKitConstants.IntentStrings.GROUP_TYPE, groupType)
                intent.putExtra(UIKitConstants.IntentStrings.MEMBER_SCOPE, loggedInUserScope)
                intent.putExtra(UIKitConstants.IntentStrings.GROUP_OWNER, groupOwnerId)
                intent.putExtra(UIKitConstants.IntentStrings.MEMBER_COUNT, memberCount)
                intent.putExtra(UIKitConstants.IntentStrings.GROUP_DESC, groupDesc)
                intent.putExtra(UIKitConstants.IntentStrings.GROUP_PASSWORD, groupPassword)
                startActivity(intent)
            }
        } else if (id == R.id.iv_back_arrow) {
            activity?.onBackPressed()
        }
    }

    override fun setLongMessageClick(baseMessagesList: List<BaseMessage>?) {
        Log.e(TAG, "setLongMessageClick: $baseMessagesList")

        isReply = false
        isEdit = false
        cometChatMessageActions = CometChatMessageActions()
        replyMessageLayout!!.visibility = View.GONE
        editMessageLayout!!.visibility = View.GONE

        var threadVisible = isThreadVisible
        var shareVisible = FeatureRestriction.isShareCopyForwardMessageEnabled()
        var copyVisible = FeatureRestriction.isShareCopyForwardMessageEnabled()
        var replyVisible = isReplyVisible
        var editVisible = FeatureRestriction.isEditMessageEnabled()
        var deleteVisible = FeatureRestriction.isDeleteMessageEnabled()
        var forwardVisible = FeatureRestriction.isShareCopyForwardMessageEnabled()
        var reactionVisible = isReactionsVisible
        var sendMessagePrivatelyVisible = false
        var replyPrivatelyVisible = false
        var messageInfoVisible = false

        var metadata = JSONObject()
        val textMessageList: MutableList<BaseMessage> = ArrayList()
        val mediaMessageList: MutableList<BaseMessage> = ArrayList()
        val locationMessageList: MutableList<BaseMessage> = ArrayList()
        val stickerMessageList: MutableList<BaseMessage> = ArrayList()
        val whiteBoardMessageList: MutableList<BaseMessage> = ArrayList()
        val writeBoardMessageList: MutableList<BaseMessage> = ArrayList()
        val directCallMessageList: MutableList<BaseMessage> = ArrayList()
        val pollsMessageList: MutableList<BaseMessage> = ArrayList()

        for (baseMessage in baseMessagesList!!) {
            if (baseMessage.type == CometChatConstants.MESSAGE_TYPE_TEXT) {
                textMessageList.add(baseMessage)
            } else if (baseMessage.type == CometChatConstants.MESSAGE_TYPE_IMAGE || baseMessage.type == CometChatConstants.MESSAGE_TYPE_VIDEO || baseMessage.type == CometChatConstants.MESSAGE_TYPE_FILE || baseMessage.type == CometChatConstants.MESSAGE_TYPE_AUDIO) {
                mediaMessageList.add(baseMessage)
            } else if (baseMessage.type == UIKitConstants.IntentStrings.LOCATION) {
                locationMessageList.add(baseMessage)
            } else if (baseMessage.type == UIKitConstants.IntentStrings.STICKERS) {
                stickerMessageList.add(baseMessage)
            } else if (baseMessage.type == UIKitConstants.IntentStrings.WHITEBOARD) {
                whiteBoardMessageList.add(baseMessage)
            } else if (baseMessage.type == UIKitConstants.IntentStrings.WRITEBOARD) {
                writeBoardMessageList.add(baseMessage)
            } else if (baseMessage.type == UIKitConstants.IntentStrings.MEETING) {
                directCallMessageList.add(baseMessage)
            } else if (baseMessage.type == UIKitConstants.IntentStrings.POLLS) {
                pollsMessageList.add(baseMessage)
            }
        }
        if (textMessageList.size == 1) {
            val basemessage = textMessageList[0]
            if (basemessage != null && basemessage.sender != null) {
                if (basemessage !is Action && basemessage.deletedAt == 0L) {
                    baseMessage = basemessage
                    if (basemessage.replyCount > 0)
                        threadVisible = false
                    else {
                        threadVisible
                    }
                    if (basemessage.sender.uid == getLoggedInUser().uid) {
                        deleteVisible = FeatureRestriction.isDeleteMessageEnabled()
                        editVisible = FeatureRestriction.isEditMessageEnabled()
//                        forwardVisible = true
                    } else {
                        editVisible = false
//                        forwardVisible = true
                        deleteVisible = if (loggedInUserScope != null && (loggedInUserScope == CometChatConstants.SCOPE_ADMIN || loggedInUserScope == CometChatConstants.SCOPE_MODERATOR)) (FeatureRestriction.isDeleteMessageEnabled() || isDeleteMemberMessageVisible) else false
                    }
                }
            }
        }

        if (mediaMessageList.size == 1) {
            val basemessage = mediaMessageList[0]
            if (basemessage != null && basemessage.sender != null) {
                if (basemessage !is Action && basemessage.deletedAt == 0L) {
                    baseMessage = basemessage
                    if (basemessage.replyCount > 0)
                        threadVisible = false
                    else {
                        threadVisible
                    }
                    copyVisible = false
                    if (basemessage.sender.uid == getLoggedInUser().uid) {
                        deleteVisible = FeatureRestriction.isDeleteMessageEnabled()
                        editVisible = false
//                        forwardVisible = true
                    } else {
                        deleteVisible = if (loggedInUserScope != null && (loggedInUserScope == CometChatConstants.SCOPE_ADMIN || loggedInUserScope == CometChatConstants.SCOPE_MODERATOR)) (FeatureRestriction.isDeleteMessageEnabled() || isDeleteMemberMessageVisible) else false
//                        forwardVisible = true
                        editVisible = false
                    }
                }
            }
        }
        if (locationMessageList.size == 1) {
            val basemessage = locationMessageList[0]
            if (basemessage != null && basemessage.sender != null) {
                if (basemessage !is Action && basemessage.deletedAt == 0L) {
                    baseMessage = basemessage
                    if (basemessage.replyCount > 0)
                        threadVisible = false
                    else {
                        threadVisible
                    }
                    copyVisible = false
                    replyVisible
                    shareVisible = false
                    forwardVisible = FeatureRestriction.isShareCopyForwardMessageEnabled()
                    if (basemessage.sender.uid == getLoggedInUser().uid) {
                        deleteVisible = FeatureRestriction.isDeleteMessageEnabled()
                        editVisible = false
                    } else {
                        deleteVisible = if (loggedInUserScope != null && (loggedInUserScope == CometChatConstants.SCOPE_ADMIN || loggedInUserScope == CometChatConstants.SCOPE_MODERATOR)) (FeatureRestriction.isDeleteMessageEnabled() || isDeleteMemberMessageVisible) else false
                        editVisible = false
                    }
                }
            }
        }
        if (stickerMessageList.size == 1){
            shareVisible = false
            forwardVisible = false
            editVisible = false
            copyVisible = false
            replyVisible
            val basemessage = stickerMessageList[0]
            if (basemessage != null && basemessage.sender != null){
                if (basemessage !is Action && basemessage.deletedAt == 0L){
                    baseMessage = basemessage
                    if (basemessage.replyCount > 0)
                        threadVisible = false
                    else {
                        threadVisible
                    }
                    if (basemessage.sender.uid == loggedInUser.uid){
                        deleteVisible = FeatureRestriction.isDeleteMessageEnabled()

                    } else{
                        deleteVisible = if (loggedInUserScope != null && (loggedInUserScope == CometChatConstants.SCOPE_ADMIN || loggedInUserScope == CometChatConstants.SCOPE_MODERATOR)) (FeatureRestriction.isDeleteMessageEnabled() || isDeleteMemberMessageVisible) else false

                    }

                }
            }

        }

        if (whiteBoardMessageList.size == 1) {
            forwardVisible = false
            copyVisible = false
            editVisible = false
            shareVisible = false
            val basemessage = whiteBoardMessageList[0]
            if (basemessage != null && basemessage.sender != null) {
                if (basemessage.deletedAt == 0L) {
                    baseMessage = basemessage
                    if (basemessage.replyCount > 0)
                        threadVisible = false
                    else {
                        threadVisible
                    }
                    if (basemessage.sender.uid == getLoggedInUser().uid)
                        deleteVisible = FeatureRestriction.isDeleteMessageEnabled()
                    else {
                        if (loggedInUserScope != null && (loggedInUserScope == CometChatConstants.SCOPE_ADMIN|| loggedInUserScope == CometChatConstants.SCOPE_MODERATOR)) {
                            deleteVisible = (FeatureRestriction.isDeleteMessageEnabled() || isDeleteMemberMessageVisible)
                        } else {
                            deleteVisible = false
                        }
                    }
                }
            }
        }

        if (writeBoardMessageList.size == 1) {
            forwardVisible = false
            copyVisible = false
            editVisible = false
            shareVisible = false
            val basemessage = writeBoardMessageList[0]
            if (basemessage != null && basemessage.sender != null) {
                if (basemessage.deletedAt == 0L) {
                    baseMessage = basemessage
                    if (basemessage.replyCount > 0)
                        threadVisible = false
                    else {
                        threadVisible
                    }
                    if (basemessage.sender.uid == getLoggedInUser().uid)
                        deleteVisible = FeatureRestriction.isDeleteMessageEnabled()
                    else {
                        if (loggedInUserScope != null && (loggedInUserScope == CometChatConstants.SCOPE_ADMIN || loggedInUserScope == CometChatConstants.SCOPE_MODERATOR)) {
                            deleteVisible = (FeatureRestriction.isDeleteMessageEnabled() || isDeleteMemberMessageVisible)
                        } else {
                            deleteVisible = false
                        }
                    }
                }
            }
        }

        if (directCallMessageList.size == 1) {
            forwardVisible = false
            copyVisible = false
            editVisible = false
            shareVisible = false
            replyVisible = false
            threadVisible = false
            reactionVisible = false
            val basemessage = directCallMessageList[0]
            if (basemessage != null && basemessage.sender != null) {
                if (basemessage.deletedAt == 0L) {
                    baseMessage = basemessage
                    if (basemessage.sender.uid == getLoggedInUser().uid)
                        deleteVisible = FeatureRestriction.isDeleteMessageEnabled()
                    else {
                        if (loggedInUserScope != null && (loggedInUserScope == CometChatConstants.SCOPE_ADMIN || loggedInUserScope == CometChatConstants.SCOPE_MODERATOR)) {
                            deleteVisible = (FeatureRestriction.isDeleteMessageEnabled() || isDeleteMemberMessageVisible)
                        } else {
                            deleteVisible = false
                        }
                    }
                }
            }
        }
        if (pollsMessageList.size == 1) {
            forwardVisible = false
            copyVisible = false
            editVisible = false
            shareVisible = false
            replyVisible
            val basemessage = pollsMessageList[0]
            if (basemessage.sender != null) {
                if (basemessage !is Action && basemessage.deletedAt == 0L) {
                    baseMessage = basemessage
                    if (basemessage.replyCount > 0)
                        threadVisible = false
                    else {
                        threadVisible
                    }
//                    threadVisible = if (basemessage.replyCount > 0) false else FeatureRestriction.isThreadedMessagesEnabled()
                    deleteVisible = if (basemessage.sender.uid == getLoggedInUser().uid) FeatureRestriction.isDeleteMessageEnabled() else {
                        if (loggedInUserScope != null && (loggedInUserScope == CometChatConstants.SCOPE_ADMIN || loggedInUserScope == CometChatConstants.SCOPE_MODERATOR)) {
                            (FeatureRestriction.isDeleteMessageEnabled() || isDeleteMemberMessageVisible)
                        } else {
                            false
                        }
                    }
                }
            }
        }


        baseMessages = baseMessagesList
        val bundle = Bundle()

        bundle.putBoolean("threadVisible", threadVisible) //done
        bundle.putBoolean("copyVisible", copyVisible)
        bundle.putBoolean("shareVisible", shareVisible)
        bundle.putBoolean("editVisible", editVisible)
        bundle.putBoolean("deleteVisible", deleteVisible)
        bundle.putBoolean("replyVisible", replyVisible) //done
        bundle.putBoolean("forwardVisible", forwardVisible)
        bundle.putBoolean("reactionVisible", reactionVisible) //done
//        if (isExtensionEnabled("reactions"))
//            bundle.putBoolean("reactionVisible", reactionVisible)

//        isExtensionEnabled("reactions", object : CallbackListener<Boolean>() {
//            override fun onSuccess(p0: Boolean?) {
//                if (p0 as Boolean) bundle.putBoolean("reactionVisible", reactionVisible)
//            }
//
//            override fun onError(p0: CometChatException?) {
//                Toast.makeText(context, "Error:" + p0?.message, Toast.LENGTH_SHORT).show()
//            }
//
//        })

//        if (baseMessage?.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP && baseMessage?.sender?.uid == loggedInUser.uid) bundle.putBoolean("messageInfoVisible", true)
//        })

        if (baseMessage?.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
            if (baseMessage?.sender?.uid == loggedInUser.uid) {
                messageInfoVisible = true
                bundle.putBoolean("messageInfoVisible", messageInfoVisible)
            }
            if (baseMessage?.sender?.uid != loggedInUser.uid) {
                sendMessagePrivatelyVisible = true
                replyPrivatelyVisible = true
                bundle.putBoolean("sendMessagePrivately", sendMessagePrivatelyVisible)
                bundle.putBoolean("replyPrivately", replyPrivatelyVisible)
            }
        }

        bundle.putString("type", CometChatMessageListActivity::class.java.name)

        cometChatMessageActions?.arguments = bundle
        if (editVisible || copyVisible || threadVisible || shareVisible || deleteVisible
                || replyVisible || forwardVisible || reactionVisible || messageInfoVisible || sendMessagePrivatelyVisible || replyPrivatelyVisible)
            cometChatMessageActions?.show(fragmentManager!!, cometChatMessageActions?.tag)
        cometChatMessageActions?.setMessageActionListener(object : CometChatMessageActions.MessageActionListener {

            override fun onEditMessageClick() {
                if (baseMessage != null && baseMessage?.type == CometChatConstants.MESSAGE_TYPE_TEXT) {
                    isEdit = true
                    isReply = false
                    tvMessageTitle?.text = resources.getString(R.string.edit_message)
                    tvMessageSubTitle?.text = (baseMessage as TextMessage).text
                    composeBox?.ivMic?.visibility = View.GONE
                    composeBox?.ivSend?.visibility = View.VISIBLE
                    editMessageLayout?.visibility = View.VISIBLE
                    composeBox?.etComposeBox?.setText((baseMessage as TextMessage).text)
                    if (messageAdapter != null) {
                        baseMessage?.id?.let { messageAdapter?.setSelectedMessage(it) }
                        messageAdapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun onThreadMessageClick() {
                startThreadActivity()
            }

            override fun onReplyMessageClick() {
                replyMessage()
            }

            override fun onForwardMessageClick() {
                startForwardMessageActivity()
            }

            override fun onDeleteMessageClick() {
                deleteMessage(baseMessage)
                if (messageAdapter != null) {
                    messageAdapter?.clearLongClickSelectedItem()
                    messageAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCopyMessageClick() {
                var message = ""
                for (bMessage in baseMessages!!) {
                    if (bMessage.deletedAt == 0L && bMessage is TextMessage) {
                        message = message + "[" + Utils.getLastMessageDate(bMessage.getSentAt()) + "] " + bMessage.getSender().name + ": " + bMessage.text
                    }
                }
                Log.e(TAG, "onCopy: $message")
                val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("MessageAdapter", message)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(context, resources.getString(R.string.text_copied), Toast.LENGTH_LONG).show()
                if (messageAdapter != null) {
                    messageAdapter?.clearLongClickSelectedItem()
                    messageAdapter?.notifyDataSetChanged()
                }
            }

            override fun onShareMessageClick() {
                shareMessage()
            }

            override fun onMessageInfoClick() {
                val intent = Intent(context, CometChatMessageInfoScreenActivity::class.java)
                intent.putExtra(UIKitConstants.IntentStrings.ID, baseMessage?.id)
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE, baseMessage?.type)
                intent.putExtra(UIKitConstants.IntentStrings.SENTAT, baseMessage?.sentAt)
                if (baseMessage?.type == CometChatConstants.MESSAGE_TYPE_TEXT) {
                    intent.putExtra(UIKitConstants.IntentStrings.TEXTMESSAGE, Extensions.getProfanityFilter(baseMessage!!))
                } else if (baseMessage?.category == CometChatConstants.CATEGORY_CUSTOM) {
                    if ((baseMessage as CustomMessage).customData != null)
                        intent.putExtra(UIKitConstants.IntentStrings.CUSTOM_MESSAGE,
                                (baseMessage as CustomMessage).customData.toString())
                    if (baseMessage?.type == UIKitConstants.IntentStrings.LOCATION) {
                        intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE,
                                UIKitConstants.IntentStrings.LOCATION)
                    } else if (baseMessage?.type == UIKitConstants.IntentStrings.STICKERS) {
                        intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE, UIKitConstants.IntentStrings.STICKERS)
                    } else if (baseMessage?.type == UIKitConstants.IntentStrings.WHITEBOARD) {
                        intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE,
                                UIKitConstants.IntentStrings.WHITEBOARD)
                        intent.putExtra(UIKitConstants.IntentStrings.TEXTMESSAGE, Extensions.getWhiteBoardUrl(baseMessage!!))
                    } else if (baseMessage?.type == UIKitConstants.IntentStrings.WRITEBOARD) {
                        intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE,
                                UIKitConstants.IntentStrings.WRITEBOARD)
                        intent.putExtra(UIKitConstants.IntentStrings.TEXTMESSAGE, Extensions.getWriteBoardUrl(baseMessage!!))
                    } else if (baseMessage?.type == UIKitConstants.IntentStrings.MEETING) {
                        intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE,
                                UIKitConstants.IntentStrings.MEETING)
                    } else {
                        intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE,
                                UIKitConstants.IntentStrings.POLLS)
                    }
                } else {
                    FeatureRestriction.isImageModerationEnabled(object : FeatureRestriction.OnSuccessListener{
                        override fun onSuccess(p0: Boolean) {
                            isImageNotSafe = Extensions.getImageModeration(context, baseMessage)
                        }
                    })
                    intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL,
                            (baseMessage as MediaMessage).attachment.fileUrl)
                    intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME,
                            (baseMessage as MediaMessage).attachment.fileName)
                    intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE,
                            (baseMessage as MediaMessage).attachment.fileSize)
                    intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION,
                            (baseMessage as MediaMessage).attachment.fileExtension)
                    intent.putExtra(UIKitConstants.IntentStrings.SENTAT, baseMessage?.sentAt)
                    intent.putExtra(UIKitConstants.IntentStrings.IMAGE_MODERATION, isImageNotSafe)
                }
                context?.startActivity(intent)
            }

            override fun onReactionClick(reaction: Reaction) {
                if (reaction.name == "add_reaction") {
                    val reactionDialog = CometChatReactionDialog()
                    reactionDialog.setOnEmojiClick(object : OnReactionClickListener {
                        override fun onEmojiClicked(emojicon: Reaction) {
                            sendReaction(emojicon)
                            reactionDialog.dismiss()
                        }
                    })
                    reactionDialog.show(fragmentManager!!, "ReactionDialog")
                } else {
                    sendReaction(reaction)
                }
            }

            override fun onSendMessagePrivatelyClick() {
                val intent = Intent(context, CometChatMessageListActivity::class.java)
                intent.putExtra(UIKitConstants.IntentStrings.UID, baseMessage?.sender?.uid)
                intent.putExtra(UIKitConstants.IntentStrings.AVATAR, baseMessage?.sender?.avatar)
                intent.putExtra(UIKitConstants.IntentStrings.STATUS, baseMessage?.sender?.status)
                intent.putExtra(UIKitConstants.IntentStrings.NAME, baseMessage?.sender?.name)
                intent.putExtra(UIKitConstants.IntentStrings.TYPE, CometChatConstants.RECEIVER_TYPE_USER)
                startActivity(intent)
                activity?.finish()
            }

            override fun onReplyPrivatelyClick() {
                val intent = Intent(context, CometChatMessageListActivity::class.java)
                intent.putExtra(UIKitConstants.IntentStrings.UID, baseMessage?.sender?.uid)
                intent.putExtra(UIKitConstants.IntentStrings.AVATAR, baseMessage?.sender?.avatar)
                intent.putExtra(UIKitConstants.IntentStrings.STATUS,  baseMessage?.sender?.status)
                intent.putExtra(UIKitConstants.IntentStrings.NAME, baseMessage?.sender?.name)
                intent.putExtra("isReply", true)
                intent.putExtra("baseMessageMetadata", baseMessage?.rawMessage.toString())

                intent.putExtra(UIKitConstants.IntentStrings.TYPE, CometChatConstants.RECEIVER_TYPE_USER)
                startActivity(intent)
                activity?.finish()
            }
        })




//        val textMessageList: MutableList<BaseMessage> = ArrayList()
//        val mediaMessageList: MutableList<BaseMessage> = ArrayList()
//        for (baseMessage in baseMessagesList!!) {
//            if (baseMessage.type == CometChatConstants.MESSAGE_TYPE_TEXT) {
//                textMessageList.add(baseMessage)
//            } else {
//                mediaMessageList.add(baseMessage)
//            }
//        }
//        if (textMessageList.size == 1) {
//            val basemessage = textMessageList[0]
//            if (basemessage != null && basemessage.sender != null) {
//                if (basemessage !is Action && basemessage.deletedAt == 0L) {
//                    baseMessage = basemessage
//                    (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(false)
//                    ivCloseMessageAction!!.visibility = View.VISIBLE
//                    ivCopyMessageAction!!.visibility = View.VISIBLE
//                    userAvatar!!.visibility = View.GONE
//                    if (basemessage.sender.uid == getLoggedInUser().uid) {
//                        tvDeleteMessage!!.visibility = View.VISIBLE
//                        tvEditMessage!!.visibility = View.VISIBLE
//                        tvEditMessage!!.text = resources.getString(R.string.edit)
//                        tvForwardMessage!!.visibility = View.VISIBLE
//                        rlMessageAction!!.visibility = View.VISIBLE
//                    } else {
//                        tvForwardMessage!!.visibility = View.VISIBLE
//                        if (loggedInUserScope == CometChatConstants.SCOPE_ADMIN) {
//                            tvDeleteMessage!!.visibility = View.VISIBLE
//                            tvEditMessage!!.visibility = View.VISIBLE
//                        } else {
//                            tvDeleteMessage!!.visibility = View.GONE
//                            tvEditMessage!!.visibility = View.GONE
//                        }
//                        rlMessageAction!!.visibility = View.VISIBLE
//                    }
//                }
//            }
//        } else if (textMessageList.size == 0) {
//            onCloseAction()
//            if (messageAdapter != null) messageAdapter!!.clearLongClickSelectedItem()
//        } else {
//            ivCopyMessageAction!!.visibility = View.VISIBLE
//            tvEditMessage!!.visibility = View.GONE
//            tvDeleteMessage!!.visibility = View.GONE
//            tvForwardMessage!!.visibility = View.GONE
//        }
//        if (mediaMessageList.size == 1) {
//            val basemessage = mediaMessageList[0]
//            if (basemessage != null && basemessage.sender != null) {
//                if (basemessage !is Action && basemessage.deletedAt == 0L) {
//                    baseMessage = basemessage
//                    (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(false)
//                    ivCloseMessageAction!!.visibility = View.VISIBLE
//                    ivCopyMessageAction!!.visibility = View.GONE
//                    userAvatar!!.visibility = View.GONE
//                    if (basemessage.sender.uid == getLoggedInUser().uid) {
//                        tvDeleteMessage!!.visibility = View.VISIBLE
//                        tvEditMessage!!.visibility = View.GONE
//                        tvForwardMessage!!.visibility = View.VISIBLE
//                        rlMessageAction!!.visibility = View.VISIBLE
//                    } else {
//                        tvForwardMessage!!.visibility = View.VISIBLE
//                        tvEditMessage!!.visibility = View.GONE
//                        tvDeleteMessage!!.visibility = View.GONE
//                        rlMessageAction!!.visibility = View.GONE
//                        ivCloseMessageAction!!.visibility = View.GONE
//                    }
//                }
//            }
//        }
//        baseMessages = baseMessagesList
    }

    private fun sendReaction(reaction: Reaction) {
        val body = JSONObject()
        try {
            body.put("msgId", baseMessage?.id)
            body.put("emoji", reaction.name)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        callExtension("reactions", "POST", "/v1/react", body,
                object : CallbackListener<JSONObject>() {
                    override fun onSuccess(responseObject: JSONObject) {
                        Log.e(TAG, "onSuccess: $responseObject")
                        // ReactionModel added successfully.
                    }

                    override fun onError(e: CometChatException) {
                        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                        Log.e(TAG, "onError: " + e.code + e.message + e.details)
                    }
                })
//        var body = JSONObject()
//        try {
//            body.put("msgId", baseMessage!!.id)
//            body.put("emoji", reaction.name)
//        } catch (e: Exception){
//            e.printStackTrace()
//        }
//
//        callExtension("reactions", "POST", "/v1/react", body, object : CallbackListener<JSONObject>() {
//            override fun onSuccess(p0: JSONObject?) {
//                Log.e(TAG, "onSuccess: " + p0.toString())
//            }
//
//            override fun onError(e: CometChatException?) {
//                Toast.makeText(context, e!!.message, Toast.LENGTH_LONG).show()
//                Log.e(TAG, "onError: " + e.code + e.message + e.details)
//            }
//
//        })
    }

    private fun startThreadActivity() {
        val intent = Intent(context, CometChatThreadMessageListActivity::class.java)
        intent.putExtra(UIKitConstants.IntentStrings.CONVERSATION_NAME, name)
        intent.putExtra(UIKitConstants.IntentStrings.NAME, baseMessage?.sender?.name)
        intent.putExtra(UIKitConstants.IntentStrings.UID, baseMessage?.sender?.name)
        intent.putExtra(UIKitConstants.IntentStrings.AVATAR, baseMessage?.sender?.avatar)
        intent.putExtra(UIKitConstants.IntentStrings.PARENT_ID, baseMessage?.id)
        intent.putExtra(UIKitConstants.IntentStrings.REPLY_COUNT, baseMessage?.replyCount)
        intent.putExtra(UIKitConstants.IntentStrings.SENTAT, baseMessage?.sentAt)
        intent.putExtra(UIKitConstants.IntentStrings.REACTION_INFO, Extensions.getReactionsOnMessage(baseMessage!!))
        if (baseMessage?.category.equals(CometChatConstants.CATEGORY_MESSAGE, ignoreCase = true)) {
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE, baseMessage?.type)
            if (baseMessage?.type == CometChatConstants.MESSAGE_TYPE_TEXT) intent.putExtra(UIKitConstants.IntentStrings.TEXTMESSAGE, Extensions.getProfanityFilter(baseMessage!!)) else {
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, (baseMessage as MediaMessage).attachment.fileName)
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, (baseMessage as MediaMessage).attachment.fileExtension)
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL, (baseMessage as MediaMessage).attachment.fileUrl)
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, (baseMessage as MediaMessage).attachment.fileSize)
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE, (baseMessage as MediaMessage).attachment.fileMimeType)
            }
        } else {
            try {
                if (baseMessage?.type == UIKitConstants.IntentStrings.LOCATION) {
                    intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE, UIKitConstants.IntentStrings.LOCATION)
                    intent.putExtra(UIKitConstants.IntentStrings.LOCATION_LATITUDE,
                            (baseMessage as CustomMessage).customData.getDouble("latitude"))
                    intent.putExtra(UIKitConstants.IntentStrings.LOCATION_LONGITUDE,
                            (baseMessage as CustomMessage).customData.getDouble("longitude"))
                } else if (baseMessage?.type == UIKitConstants.IntentStrings.STICKERS) {
                    intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, (baseMessage as CustomMessage).customData.getString("name"))
                    intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL, (baseMessage as CustomMessage).customData.getString("url"))
                    intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE, UIKitConstants.IntentStrings.STICKERS)
                } else if (baseMessage?.type == UIKitConstants.IntentStrings.WHITEBOARD) {
                    intent.putExtra(UIKitConstants.IntentStrings.TEXTMESSAGE, Extensions.getWhiteBoardUrl(baseMessage!!))
                    intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE, UIKitConstants.IntentStrings.WHITEBOARD)
                } else if (baseMessage?.type == UIKitConstants.IntentStrings.WRITEBOARD) {
                    intent.putExtra(UIKitConstants.IntentStrings.TEXTMESSAGE, Extensions.getWriteBoardUrl(baseMessage!!))
                    intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE, UIKitConstants.IntentStrings.WRITEBOARD)
                } else if (baseMessage?.type == UIKitConstants.IntentStrings.POLLS) {
                    val options = (baseMessage as CustomMessage).customData.getJSONObject("options")
                    intent.putExtra(UIKitConstants.IntentStrings.POLL_QUESTION, (baseMessage as CustomMessage).customData.getString("question"))
                    intent.putExtra(UIKitConstants.IntentStrings.POLL_OPTION, options.toString())
                    intent.putExtra(UIKitConstants.IntentStrings.POLL_VOTE_COUNT, Extensions.getVoteCount(baseMessage!!))
                    intent.putExtra(UIKitConstants.IntentStrings.POLL_RESULT, Extensions.getVoterInfo(baseMessage!!, options.length()))
                    intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE, UIKitConstants.IntentStrings.POLLS)
                }
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "startThreadActivityError: " + e.message)
            }
        }
        intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY, baseMessage?.category)
        intent.putExtra(UIKitConstants.IntentStrings.TYPE, type)
        if (type == CometChatConstants.CONVERSATION_TYPE_GROUP) {
            intent.putExtra(UIKitConstants.IntentStrings.GUID, Id)
        } else {
            intent.putExtra(UIKitConstants.IntentStrings.UID, Id)
        }
        startActivity(intent)
    }

    private fun startForwardMessageActivity() {
        val intent = Intent(context, CometChatForwardMessageActivity::class.java)
        if (baseMessage?.category == CometChatConstants.CATEGORY_MESSAGE) {
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY, CometChatConstants.CATEGORY_MESSAGE)
            if (baseMessage?.type == CometChatConstants.MESSAGE_TYPE_TEXT) {
                intent.putExtra(CometChatConstants.MESSAGE_TYPE_TEXT, (baseMessage as TextMessage).text)
                intent.putExtra(UIKitConstants.IntentStrings.TYPE, CometChatConstants.MESSAGE_TYPE_TEXT)
            } else if (baseMessage?.type == CometChatConstants.MESSAGE_TYPE_IMAGE ||
                    baseMessage?.type == CometChatConstants.MESSAGE_TYPE_AUDIO ||
                    baseMessage?.type == CometChatConstants.MESSAGE_TYPE_VIDEO ||
                    baseMessage?.type == CometChatConstants.MESSAGE_TYPE_FILE) {
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, (baseMessage as MediaMessage).attachment.fileName)
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL, (baseMessage as MediaMessage).attachment.fileUrl)
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE, (baseMessage as MediaMessage).attachment.fileMimeType)
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, (baseMessage as MediaMessage).attachment.fileExtension)
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, (baseMessage as MediaMessage).attachment.fileSize)
                intent.putExtra(UIKitConstants.IntentStrings.TYPE, baseMessage?.type)
            }
        } else if (baseMessage?.category == CometChatConstants.CATEGORY_CUSTOM) {
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY, CometChatConstants.CATEGORY_CUSTOM)
            intent.putExtra(UIKitConstants.IntentStrings.TYPE, UIKitConstants.IntentStrings.LOCATION)
            try {
                intent.putExtra(UIKitConstants.IntentStrings.LOCATION_LATITUDE,
                        (baseMessage as CustomMessage).customData.getDouble("latitude"))
                intent.putExtra(UIKitConstants.IntentStrings.LOCATION_LONGITUDE,
                        (baseMessage as CustomMessage).customData.getDouble("longitude"))
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "startForwardMessageActivityError: " + e.message)
            }
        }

        startActivity(intent)
    }

    companion object {
        private const val TAG = "CometChatMessageScreen"
        private const val LIMIT = 30
    }

    private fun shareMessage() {
        if (baseMessage != null && baseMessage?.type == CometChatConstants.MESSAGE_TYPE_TEXT) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_TITLE, resources.getString(R.string.app_name))
            shareIntent.putExtra(Intent.EXTRA_TEXT, (baseMessage as TextMessage).text)
            shareIntent.type = "text/plain"
            val intent = Intent.createChooser(shareIntent, resources.getString(R.string.share_message))
            startActivity(intent)
        } else if (baseMessage != null && baseMessage?.type == CometChatConstants.MESSAGE_TYPE_IMAGE) {
            val mediaName = (baseMessage as MediaMessage).attachment.fileName
            Glide.with(context!!).asBitmap().load((baseMessage as MediaMessage).attachment.fileUrl).into(object : SimpleTarget<Bitmap?>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                    val path = MediaStore.Images.Media.insertImage(context?.contentResolver, resource, mediaName, null)
                    val shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
                    shareIntent.type = (baseMessage as MediaMessage).attachment.fileMimeType
                    val intent = Intent.createChooser(shareIntent, resources.getString(R.string.share_message))
                    startActivity(intent)
                }
            })
        }
    }

    private fun replyMessage() {
        if (baseMessage != null) {
            isReply = true
            replyTitle?.text = baseMessage?.sender?.name
            replyMedia?.visibility = View.VISIBLE
            if (baseMessage?.type == CometChatConstants.MESSAGE_TYPE_TEXT) {
                replyMessage?.text = (baseMessage as TextMessage).text
                replyMedia?.visibility = View.GONE
                replyMessage?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            } else if (baseMessage?.type == CometChatConstants.MESSAGE_TYPE_IMAGE) {
                replyMessage?.text = resources.getString(R.string.shared_a_image)
                Glide.with(context!!).load((baseMessage as MediaMessage).attachment.fileUrl).into(replyMedia!!)
                replyMessage?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            } else if (baseMessage?.type == CometChatConstants.MESSAGE_TYPE_AUDIO) {
                val messageStr = String.format(resources.getString(R.string.shared_a_audio),
                        Utils.getFileSize((baseMessage as MediaMessage).attachment.fileSize))
                replyMessage?.text = messageStr
                replyMessage?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_library_music_24dp, 0, 0, 0)
                replyMedia?.visibility = View.GONE
            } else if (baseMessage?.type == CometChatConstants.MESSAGE_TYPE_VIDEO) {
                replyMessage?.text = resources.getString(R.string.shared_a_video)
                Glide.with(context!!).load((baseMessage as MediaMessage).attachment.fileUrl).into(replyMedia!!)
            } else if (baseMessage?.type == CometChatConstants.MESSAGE_TYPE_FILE) {
                val messageStr = String.format(resources.getString(R.string.shared_a_file),
                        Utils.getFileSize((baseMessage as MediaMessage).attachment.fileSize))
                replyMessage?.text = messageStr
                replyMessage?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_insert_drive_file_black_24dp, 0, 0, 0)
            }
            else if (baseMessage?.type == UIKitConstants.IntentStrings.LOCATION) {
                try {
                    val jsonObject = (baseMessage as CustomMessage).customData
                    val messageStr = java.lang.String.format(getString(R.string.shared_location),
                            Utils.getAddress(context, jsonObject.getDouble("latitude"),
                                    jsonObject.getDouble("longitude")))
                    replyMessage?.text = messageStr
                    replyMedia?.visibility = View.GONE
                    replyMessage?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                } catch (e: Exception) {
                    Log.e(TAG, "replyMessageError: " + e.message)
                }
            } else if (baseMessage?.type == UIKitConstants.IntentStrings.POLLS) {
                try {
                    val jsonObject = (baseMessage as CustomMessage).customData
                    val messageStr = String.format(getString(R.string.shared_a_polls), jsonObject.getString("question"))
                    replyMessage?.text = messageStr
                } catch (e: Exception) {
                    Log.e(TAG, "replyMessageError: " + e.message)
                }
            }
            else if (baseMessage?.type == UIKitConstants.IntentStrings.STICKERS) {
                replyMessage?.text = resources.getString(R.string.shared_a_sticker)
                try {
                    Glide.with(context!!).load((baseMessage as CustomMessage).customData.getString("url")).into(replyMedia!!)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else if (baseMessage?.type == UIKitConstants.IntentStrings.WHITEBOARD) {
                replyMessage?.text = getString(R.string.shared_a_whiteboard)
                replyMessage?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_whiteboard_24dp, 0, 0, 0)
            } else if (baseMessage?.type == UIKitConstants.IntentStrings.WRITEBOARD) {
                replyMessage?.text = getString(R.string.shared_a_writeboard)
                replyMessage?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_writeboard_24dp, 0, 0, 0)
            }
            composeBox?.ivMic?.visibility = View.GONE
            composeBox?.ivSend?.visibility = View.VISIBLE
            replyMessageLayout?.visibility = View.VISIBLE
//            if (messageAdapter != null) {
//                baseMessage?.id?.let { messageAdapter?.setSelectedMessage(it) }
//                messageAdapter?.notifyDataSetChanged()
//            }
        }
    }

    override fun handleDialogClose(dialog: DialogInterface?) {
        if (messageAdapter != null) messageAdapter?.clearLongClickSelectedItem()
        dialog?.dismiss()
    }
}