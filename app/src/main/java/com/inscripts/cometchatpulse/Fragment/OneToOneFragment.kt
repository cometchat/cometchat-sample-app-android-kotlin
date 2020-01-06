package com.inscripts.cometchatpulse.Fragment


import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Application
import android.app.SearchManager
import androidx.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import androidx.databinding.DataBindingUtil
import android.graphics.PorterDuff
import android.media.MediaRecorder
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.SimpleItemAnimator
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import com.bumptech.glide.Glide
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.helpers.Logger
import com.cometchat.pro.models.*
import com.inscripts.cometchatpulse.Activities.ChatActivity
import com.inscripts.cometchatpulse.Activities.GroupMessageInfoActivity
import com.inscripts.cometchatpulse.Activities.LocationActivity
import com.inscripts.cometchatpulse.Activities.UserProfileViewActivity
import com.inscripts.cometchatpulse.Adapter.OneToOneAdapter
import com.inscripts.cometchatpulse.CometChatPro
import com.inscripts.cometchatpulse.CustomView.AttachmentTypeSelector
import com.inscripts.cometchatpulse.CustomView.StickyHeaderDecoration
import com.inscripts.cometchatpulse.Helpers.*
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.StringContract.IntentString.Companion.EXTRA_MIME_DOC
import com.inscripts.cometchatpulse.StringContract.ListenerName.Companion.MESSAGE_LISTENER
import com.inscripts.cometchatpulse.Utils.Appearance
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.Utils.FileUtil
import com.inscripts.cometchatpulse.ViewModel.OnetoOneViewModel
import com.inscripts.cometchatpulse.databinding.FragmentContactDetailBinding
import kotlinx.android.synthetic.main.fragment_contact_detail.*
import kotlinx.android.synthetic.main.fragment_contact_detail.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.coroutines.CoroutineContext

class OneToOneFragment : Fragment(), View.OnClickListener, RecordListener, ActionMode.Callback, OnClickEvent,
        TextWatcher {

    companion object {
        const val ARG_PARAM1 = "param1"
        const val ARG_PARAM2 = "param2"
        private val TAG = "OneToOneFragment"
        var isReply: Boolean = false
        var metaData: JSONObject? = JSONObject()
        var currentId: String? = null
        var scrollFlag: Boolean = true
    }

    private lateinit var searchView: SearchView

    private var isEditMessage: Boolean=false

    private lateinit var linearLayoutManager: androidx.recyclerview.widget.LinearLayoutManager

    private lateinit var oneToOneAdapter: OneToOneAdapter

    private var ownerId: String

    lateinit var binding: FragmentContactDetailBinding

    private lateinit var userId: String

    private lateinit var config: Configuration

    private var status: String? = null

    private var name: String? = null

    private var avatar: String? = null

    private var uid: String? = null

    private lateinit var onetoOneViewModel: OnetoOneViewModel

    private var audioFileNamewithPath: String? = null

    private var parentJob = Job()

    private lateinit var clickListener: OnBackArrowClickListener

    var mediaRecorder: MediaRecorder? = null

    var count: Int = 0

    private var attachmentTypeSelector: AttachmentTypeSelector? = null

    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    private val scope = CoroutineScope(coroutineContext)

    var currentScrollPosition = 0

    private var mode: ActionMode? = null

    private var menuResId: Int? = null

    private lateinit var any: Any

    private  var user: User?=null

    private var lastActive:Long?=null

    private var timer: Timer? =Timer()

    init {
        ownerId = CometChat.getLoggedInUser().uid
        scrollFlag=true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_contact_detail, container, false)

        setHasOptionsMenu(true)

        name = arguments?.getString(StringContract.IntentString.USER_NAME)

        status = arguments?.getString(StringContract.IntentString.USER_STATUS)

        avatar = arguments?.getString(StringContract.IntentString.USER_AVATAR)

        uid = arguments?.getString(StringContract.IntentString.USER_ID)

        binding.name = name

        binding.status = status

        binding.avatar = avatar

        binding.lastActive = arguments?.getLong(StringContract.IntentString.LAST_ACTIVE)

        lastActive = arguments?.getLong(StringContract.IntentString.LAST_ACTIVE)

        userId = arguments?.getString(StringContract.IntentString.USER_ID)!!

        currentId = userId

        onetoOneViewModel = OnetoOneViewModel(CometChatPro.applicationContext() as Application)

        binding.messageBox?.recordButton?.setListenForRecord(true)

        binding.messageBox?.recordAudioView?.setCancelOffset(16.toFloat())

        binding.messageBox?.recordAudioView?.setLessThanSecondAllowed(false)

        binding.messageBox?.recordAudioView?.setSlideToCancelText(getString(R.string.slide_to_cancel))

        binding.messageBox?.recordAudioView?.setCustomSounds(R.raw.record_start, R.raw.record_finished, R.raw.record_error)

        binding.messageBox?.recordButton?.setRecordAudio(binding.messageBox?.recordAudioView)

        binding.messageBox?.recordAudioView?.setOnRecordListener(this)

        config = activity?.resources?.configuration!!

        clickListener = context as OnBackArrowClickListener

        linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        binding.recycler.layoutManager = linearLayoutManager

        binding.title.typeface = StringContract.Font.name
        binding.subTitle.typeface = StringContract.Font.status

        (binding.recycler.itemAnimator as androidx.recyclerview.widget.SimpleItemAnimator).supportsChangeAnimations=false
        oneToOneAdapter = OneToOneAdapter(context!!, CometChat.getLoggedInUser().uid, this)
        binding.recycler.addItemDecoration(StickyHeaderDecoration(oneToOneAdapter))
        binding.recycler.setRecyclerListener(RecycleListenerHelper())
        oneToOneAdapter.setHasStableIds(true)
        binding.recycler.adapter = oneToOneAdapter

        (activity as AppCompatActivity).setSupportActionBar(binding.cometchatToolbar)

        binding.cometchatToolbar.title = ""

        binding.cometchatToolbar.setBackgroundColor(StringContract.Color.primaryColor)

        binding.cometchatToolbar.navigationIcon?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)


        binding.subTitle.isSelected = true

        binding.contactPic.borderColor = StringContract.Color.white

        binding.contactPic.borderWidth = 2

        binding.messageBox?.buttonSendMessage?.setOnClickListener(this)
        binding.messageBox?.ivAttchment?.setOnClickListener(this)
        binding.messageBox?.editTextChatMessage?.addTextChangedListener(this)
        binding.rlTitlecontainer.setOnClickListener(this)

        binding.rlMain.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = binding.rlMain.rootView.height - binding.rlMain.height
            try {

                if (heightDiff > CommonUtil.dpToPx(CometChatPro.applicationContext(), 200f)) {
                    binding.recycler.scrollToPosition(oneToOneAdapter.itemCount - 1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        binding.messageBox?.replyLayout?.ivClose?.setOnClickListener {
            hideReplyContainer()
        }


        if (StringContract.AppDetails.theme == Appearance.AppTheme.AZURE_RADIANCE) {
            binding.title.setTextColor(StringContract.Color.black)
            binding.subTitle.setTextColor(StringContract.Color.black)
            binding.messageBox?.buttonSendMessage?.drawable?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)
            binding.messageBox?.ivAttchment?.drawable?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)
            binding.messageBox?.recordButton?.drawable?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)
        } else {
            binding.messageBox?.buttonSendMessage?.drawable?.setColorFilter(StringContract.Color.primaryColor, PorterDuff.Mode.SRC_ATOP)
            binding.messageBox?.ivAttchment?.drawable?.setColorFilter(StringContract.Color.primaryColor, PorterDuff.Mode.SRC_ATOP)
            binding.messageBox?.recordButton?.drawable?.setColorFilter(StringContract.Color.primaryColor, PorterDuff.Mode.SRC_ATOP)
            binding.title.setTextColor(StringContract.Color.white)
            binding.subTitle.setTextColor(StringContract.Color.white)
            binding.messageBox?.buttonSendMessage?.drawable?.setColorFilter(StringContract.Color.white, PorterDuff.Mode.SRC_ATOP)
        }

        binding.cometchatToolbar.overflowIcon?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)

        binding.messageBox?.buttonSendMessage?.backgroundTintList = ColorStateList.valueOf(StringContract.Color.primaryColor)


        scope.launch(Dispatchers.IO) {
            onetoOneViewModel.fetchMessage(LIMIT = 30, userId = userId)

        }

        onetoOneViewModel.messageList.observe(this, Observer { messages ->
            messages?.let {
                oneToOneAdapter.setMessageList(it)
                if (scrollFlag) {
                    scrollBottom()
                    scrollFlag = false
                }

            }
        })

        onetoOneViewModel.livefilter.observe(this, Observer { filter->
            filter?.let {
                oneToOneAdapter.setFilterList(it)
            }
        })

        onetoOneViewModel.liveDeletedMessage.observe(this, Observer { deletedMessage->
              deletedMessage?.let { oneToOneAdapter.setDeletedMessage(deletedMessage) }
        })

        onetoOneViewModel.liveEditMessage.observe(this, Observer { editMessage->
            editMessage?.let { oneToOneAdapter.setEditMessage(editMessage) }
        })


        onetoOneViewModel.liveStartTypingIndicator.observe(this, Observer { typingIndicator ->
            typingIndicator?.let {
                binding.subTitle.text = "Typing..."
            }
        })


        onetoOneViewModel.liveDeliveryReceipts.observe(this, Observer {
            messageReceipts->
            messageReceipts?.let {
               oneToOneAdapter.setDeliveryReceipts(it)
            }
        })

        onetoOneViewModel.liveReadReceipts.observe(this, Observer {
            messageReceipts->
            messageReceipts?.let {
                oneToOneAdapter.setRead(it)
            }
        })


        onetoOneViewModel.liveEndTypingIndicator.observe(this, Observer { typingIndicator ->
                binding.status = "online"

        })

        onetoOneViewModel.user.observe(this, Observer { user ->
            user?.let {
                if (it.uid.equals(userId)) {
                    binding.lastActive = it.lastActiveAt
                    binding.status = it.status
                }
            }
        })

        binding.recycler.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: androidx.recyclerview.widget.RecyclerView, newState: Int) {

                binding.cometchatToolbar.isSelected = binding.recycler.canScrollVertically(-1)

                if (!recyclerView.canScrollVertically(-1)) {
                    onetoOneViewModel.fetchMessage(LIMIT = 30, userId = userId)
                    scrollFlag = false
                }
            }

            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {

            }

        })

        return binding.root
    }


    fun hideReplyContainer() {
        isReply = false
        metaData = null
        metaData = JSONObject()
        binding.messageBox?.replyLayout?.rlMain?.visibility = View.GONE
    }


    override fun afterTextChanged(s: Editable?) {
        if (timer != null) {
            timer()
        } else {
            timer = Timer()
            timer()
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        val length = binding.messageBox?.editTextChatMessage?.text.toString().length

        if (length > 0) {
            onetoOneViewModel.sendTypingIndicator(userId)
        }
    }


    private fun timer() {

        timer?.schedule(object : TimerTask() {
            override fun run() {
            onetoOneViewModel.sendTypingIndicator(userId, true)
          }
        }, 2000)
    }

    override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {

        p0?.finish()

        when (p1?.itemId) {

            R.id.reply -> {

                isReply = true
                metaData = null
                metaData = JSONObject()
                metaData?.put("reply", "reply")

                binding.messageBox?.replyLayout?.rlMain?.visibility = View.VISIBLE

                if (any is TextMessage) {
                    binding.messageBox?.replyLayout?.tvNameReply?.text = (any as TextMessage).sender.name
                    binding.messageBox?.replyLayout?.tvTextMessage?.text = (any as TextMessage).text
                    metaData?.put("senderName", (any as TextMessage).sender.name)
                    metaData?.put("senderUid", (any as TextMessage).sender.uid)
                    metaData?.put("type", (any as TextMessage).type)
                    metaData?.put("id", (any as TextMessage).id)
                    metaData?.put("text", (any as TextMessage).text)

                    Log.d(TAG, "onActionItemClicked: " + metaData?.toString())

                }

                if (any is MediaMessage) {

                    metaData?.put("senderUid", (any as MediaMessage).sender.uid)
                    binding.messageBox?.replyLayout?.tvNameReply?.text = (any as MediaMessage).sender.name
                    metaData?.put("senderName", (any as MediaMessage).sender.name)
                    metaData?.put("url", (any as MediaMessage).attachment.fileUrl)
                    metaData?.put("id", (any as MediaMessage).id)
                    val type = (any as MediaMessage).type
                    metaData?.put("type", type)

                    if (type == CometChatConstants.MESSAGE_TYPE_IMAGE
                            || type == CometChatConstants.MESSAGE_TYPE_VIDEO) {

                        binding.messageBox?.replyLayout?.ivReplyImage?.visibility = View.VISIBLE

                        binding.messageBox?.replyLayout?.ivReplyImage?.let { Glide.with(this).load((any as MediaMessage).attachment.fileUrl).into(it) }

                    } else if (type.equals(CometChatConstants.MESSAGE_TYPE_AUDIO)) {

                        binding.messageBox?.replyLayout?.tvTextMessage?.text = "Audio Message"


                    } else if (type == CometChatConstants.MESSAGE_TYPE_FILE) {

                        binding.messageBox?.replyLayout?.tvTextMessage?.text = "File Message"
                    }

                }
            }

            R.id.delete->{
                 if (any is TextMessage) {
                     onetoOneViewModel.deleteMessage(any as TextMessage)
                 }
            }

            R.id.edit->{
                var textMessage: TextMessage
                isEditMessage = true
                if (any is TextMessage) {
                    textMessage = any as TextMessage
                    binding.messageBox?.editTextChatMessage?.setText(textMessage.text)
                }
            }

        }

        return true
    }

    override fun onCreateActionMode(p0: ActionMode?, p1: Menu?): Boolean {

        p0?.menuInflater?.inflate(R.menu.action_bar, p1)
        mode = p0

        mode?.title = name
        mode?.subtitle = status

        return true
    }

    override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
        return false
    }

    override fun onDestroyActionMode(p0: ActionMode?) {
        mode = null
    }

    private fun scrollBottom() {
        binding.recycler.scrollToPosition(oneToOneAdapter.itemCount - 1)
    }

    override fun onClickRl(item: View, any: Any) {
        binding.cometchatToolbar.startActionMode(this)
        this.any = any
    }


    override fun onRecordCancel() {
        binding.messageBox?.editTextChatMessage?.hint = getString(R.string.type_your_message)
        stopRecording(true)
    }

    override fun onRecordFinish(time: Long) {
        binding.messageBox?.editTextChatMessage?.hint = getString(R.string.type_your_message)
        stopRecording(false)

        if (audioFileNamewithPath != null) {
            Logger.error("audioFileNamewithPath", audioFileNamewithPath)
            onetoOneViewModel.sendMediaMessage(audioFileNamewithPath, CometChatConstants.MESSAGE_TYPE_AUDIO, userId, this)

        }
    }

    override fun onRecordLessTime() {
        binding.messageBox?.editTextChatMessage?.hint = getString(R.string.type_your_message)
        stopRecording(true)
    }

    override fun onRecordStart() {
        binding.messageBox?.editTextChatMessage?.hint = ""
        startRecording()
    }


    private fun startRecording() {
        try {
            mediaRecorder = MediaRecorder()
            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            audioFileNamewithPath = FileUtil.getOutputMediaFile(context).toString()
            mediaRecorder?.setOutputFile(audioFileNamewithPath)

            try {
                mediaRecorder?.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            mediaRecorder?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun stopRecording(isCancel: Boolean) {
        try {
            if (mediaRecorder != null) {
                mediaRecorder?.stop()
                mediaRecorder?.reset()
                mediaRecorder?.release()
                mediaRecorder = null
                if (isCancel) {
                    File(audioFileNamewithPath).delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = ""
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {

        menu?.clear()
        inflater?.inflate(R.menu.option_menu, menu)

        val audioCall = menu?.findItem(R.id.voice_call)
        val videoCall = menu?.findItem(R.id.video_call)


        menu?.findItem(R.id.menu_leave)?.isVisible = false

        audioCall?.icon?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)
        videoCall?.icon?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)

        var searchItem = menu?.findItem(R.id.app_bar_search)
        searchItem?.icon?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)
        var editText : EditText? = menu?.findItem(R.id.app_bar_search)?.actionView?.findViewById(androidx.appcompat.R.id.search_src_text)
        editText?.setTextColor(StringContract.Color.white)

        if (searchItem != null) {

            searchView = searchItem.getActionView() as SearchView

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(s: String): Boolean {
                    return false
                }
                override fun onQueryTextChange(s: String): Boolean {
                    onetoOneViewModel.searchMessage(s, userId)
                    return false
                }
            })

            searchView.setOnCloseListener({
                onetoOneViewModel.fetchMessage(30, userId)
                false
            })
        }


        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {

            android.R.id.home -> {

                clickListener.onBackClick()

            }
            R.id.voice_call -> {
                if (CCPermissionHelper.hasPermissions(activity, *arrayOf(CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO))) {

                    onetoOneViewModel.initCall(context!!, userId, CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.CALL_TYPE_AUDIO)
                } else {
                    CCPermissionHelper.requestPermissions(activity as Activity, arrayOf(CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO),
                            StringContract.RequestCode.VOICE_CALL)
                }

            }

            R.id.video_call -> {
                if (CCPermissionHelper.hasPermissions(activity, *arrayOf(CCPermissionHelper.REQUEST_PERMISSION_CAMERA, CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO))) {

                    onetoOneViewModel.initCall(context!!, userId, CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.CALL_TYPE_VIDEO)
                } else {
                    CCPermissionHelper.requestPermissions(activity as Activity, arrayOf(CCPermissionHelper.REQUEST_PERMISSION_CAMERA, CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO),
                            StringContract.RequestCode.VIDEO_CALL)
                }

            }

            R.id.menu_block->{
                onetoOneViewModel.blockUser(userId)
            }

        }
        return super.onOptionsItemSelected(item)
    }



    override fun onClick(p0: View?) {

        when (p0?.id) {


            R.id.buttonSendMessage -> {

                val messageText: String? = binding.messageBox?.editTextChatMessage?.text.toString().trim()

                if (messageText != null && !messageText.isEmpty()) {
                    val textMessage = TextMessage(userId, messageText, CometChatConstants.RECEIVER_TYPE_USER)

                    binding.messageBox?.editTextChatMessage?.setText("")

                    if (!isEditMessage) {
                        if (isReply) {
                            binding.messageBox?.replyLayout?.rlMain?.visibility = View.GONE
                            textMessage.metadata = metaData
                            metaData = null
                            metaData = JSONObject()
                        }
                        onetoOneViewModel.sendTextMessage(textMessage)
                    }
                    else{
                        isEditMessage=false
                        binding.messageBox?.editTextChatMessage?.setText("")
                        onetoOneViewModel.sendEditMessage(any as TextMessage,messageText)

                    }


                    onetoOneViewModel.sendTypingIndicator(userId, true)

                }

                scrollFlag = true

            }

            R.id.rl_titlecontainer -> {

                val profilViewIntent = Intent(context, UserProfileViewActivity::class.java)
                profilViewIntent.putExtra(StringContract.IntentString.USER_NAME, name)
                profilViewIntent.putExtra(StringContract.IntentString.USER_ID, uid)
                profilViewIntent.putExtra(StringContract.IntentString.USER_STATUS, status)
                profilViewIntent.putExtra(StringContract.IntentString.USER_AVATAR, avatar)
                startActivity(profilViewIntent)

            }

            R.id.iv_attchment -> {
                showPopUp()
            }
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            StringContract.RequestCode.ADD_DOCUMENT ->

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AttachmentHelper.selectMedia(activity, "", EXTRA_MIME_DOC)
                } else {
                    showToast()
                }

            StringContract.RequestCode.ADD_GALLERY ->

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    val intent = AttachmentHelper.selectMedia(activity, "", StringContract.IntentString.EXTRA_MIME_TYPE)

                    startActivityForResult(intent, StringContract.RequestCode.ADD_GALLERY)

                } else {
                    showToast()
                }

            StringContract.RequestCode.TAKE_PHOTO ->
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    val intent = AttachmentHelper.captureImage()
                    startActivityForResult(intent, StringContract.RequestCode.TAKE_PHOTO)

                } else {
                    showToast()
                }

            StringContract.RequestCode.TAKE_VIDEO ->

                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    val intent = AttachmentHelper.captureVideo()

                    startActivityForResult(intent, StringContract.RequestCode.TAKE_VIDEO)

                } else {
                    showToast()
                }

            StringContract.RequestCode.RECORD_CODE ->

                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    showToast()
                }

            StringContract.RequestCode.ADD_SOUND -> if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {

                AttachmentHelper.selectMedia(activity, StringContract.IntentString.AUDIO_TYPE, null)
            } else {
                showToast()
            }
            StringContract.RequestCode.VOICE_CALL -> if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {

                context?.let { onetoOneViewModel.initCall(it, userId, CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.CALL_TYPE_AUDIO) }

            } else {
                showToast()
            }
            StringContract.RequestCode.VIDEO_CALL -> if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                context?.let { onetoOneViewModel.initCall(it, userId, CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.CALL_TYPE_VIDEO) }
            } else {
                showToast()
            }

            StringContract.RequestCode.FILE_WRITE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                FileUtil.makeDirectory(context, CometChatConstants.MESSAGE_TYPE_AUDIO)
            }


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {

                StringContract.RequestCode.ADD_GALLERY -> {
                    val filePath = AttachmentHelper.handleFile(context, data)
                    onetoOneViewModel.sendMediaMessage(filePath[0], filePath[1], userId, this)
                    scrollFlag = true
                }
                StringContract.RequestCode.TAKE_PHOTO -> {
                    val filePath = AttachmentHelper.handleCameraImage(context, data)
                    scrollFlag = true
                    onetoOneViewModel.sendMediaMessage(filePath, CometChatConstants.MESSAGE_TYPE_IMAGE, userId, this)

                }

                StringContract.RequestCode.TAKE_VIDEO -> {
                    val filePath = AttachmentHelper.handleCameraVideo(context, data)
                    onetoOneViewModel.sendMediaMessage(filePath, CometChatConstants.MESSAGE_TYPE_VIDEO, userId, this)
                    scrollFlag = true

                }

                StringContract.RequestCode.ADD_SOUND -> {
                    val filePath = AttachmentHelper.handleFile(context, data)
                    onetoOneViewModel.sendMediaMessage(filePath[0], CometChatConstants.MESSAGE_TYPE_AUDIO, userId, this)
                    scrollFlag = true
                }

                StringContract.RequestCode.ADD_DOCUMENT -> {
                    val filePath = AttachmentHelper.handleFile(context, data)
                    onetoOneViewModel.sendMediaMessage(filePath[0], filePath[1], userId, this)
                    scrollFlag = true

                }
            }
        }

    }


    private fun showToast() {
        Toast.makeText(context, "PERMISSION NOT GRANTED", Toast.LENGTH_SHORT).show()
    }

    private fun showPopUp() {

        try {
            if (attachmentTypeSelector == null) {
                attachmentTypeSelector = AttachmentTypeSelector(context!!, AttachmentTypeListener())
            }
            attachmentTypeSelector!!.show(activity as Activity, binding.messageBox?.ivAttchment as View)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private inner class AttachmentTypeListener : AttachmentTypeSelector.AttachmentClickedListener {
        override fun onClick(type: Int) {
            addAttachment(type, activity)
        }
    }


    fun addAttachment(type: Int, activity: FragmentActivity?) {
        when (type) {
            StringContract.RequestCode.ADD_GALLERY ->

                if (CCPermissionHelper.hasPermissions(activity, *StringContract.RequestPermission.STORAGE_PERMISSION)) {

                    val intent = AttachmentHelper.selectMedia(activity, "*/*", StringContract.IntentString.EXTRA_MIME_TYPE)

                    startActivityForResult(intent, StringContract.RequestCode.ADD_GALLERY)

                } else {
                    CCPermissionHelper.requestPermissions(activity as Activity, StringContract.RequestPermission.STORAGE_PERMISSION, StringContract.RequestCode.ADD_GALLERY)
                }
            StringContract.RequestCode.ADD_DOCUMENT ->

                if (CCPermissionHelper.hasPermissions(activity, *StringContract.RequestPermission.STORAGE_PERMISSION)) {

                    val intent = AttachmentHelper.selectMedia(activity, "*/*", StringContract.IntentString.DOCUMENT_TYPE)

                    startActivityForResult(intent, StringContract.RequestCode.ADD_DOCUMENT)

                } else {
                    CCPermissionHelper.requestPermissions(activity as Activity, StringContract.RequestPermission.STORAGE_PERMISSION, StringContract.RequestCode.ADD_DOCUMENT)
                }
            StringContract.RequestCode.ADD_SOUND ->

                if (CCPermissionHelper.hasPermissions(activity, *StringContract.RequestPermission.STORAGE_PERMISSION)) {

                    val intent = AttachmentHelper.selectMedia(activity, StringContract.IntentString.AUDIO_TYPE,
                            null)

                    startActivityForResult(intent, StringContract.RequestCode.ADD_SOUND)

                } else {
                    CCPermissionHelper.requestPermissions(activity as Activity, StringContract.RequestPermission.STORAGE_PERMISSION, StringContract.RequestCode.ADD_SOUND)
                }

            StringContract.RequestCode.TAKE_PHOTO ->

                if (CCPermissionHelper.hasPermissions(activity, *StringContract.RequestPermission.CAMERA_PERMISSION)) {

                    val intent = AttachmentHelper.captureImage()

                    startActivityForResult(intent, StringContract.RequestCode.TAKE_PHOTO)
                } else {
                    CCPermissionHelper.requestPermissions(activity as Activity, StringContract.RequestPermission.CAMERA_PERMISSION, StringContract.RequestCode.TAKE_PHOTO)
                }
            StringContract.RequestCode.TAKE_VIDEO ->
                if (CCPermissionHelper.hasPermissions(activity, *StringContract.RequestPermission.CAMERA_PERMISSION)) {
                    val intent = AttachmentHelper.captureVideo()
                    startActivityForResult(intent, StringContract.RequestCode.TAKE_VIDEO)
                } else {
                    CCPermissionHelper.requestPermissions(activity as Activity, StringContract.RequestPermission.CAMERA_PERMISSION, StringContract.RequestCode.TAKE_VIDEO)

                }

            StringContract.RequestCode.LOCATION -> {

                try {

                    if (CommonUtil.checkPermission(context!!)) {
                        val locationIntent = Intent(context, LocationActivity::class.java)
                        locationIntent.putExtra(StringContract.IntentString.ID, userId)
                        locationIntent.putExtra(StringContract.IntentString.RECIVER_TYPE,
                                CometChatConstants.RECEIVER_TYPE_USER)
                        startActivity(locationIntent)
                    } else {
                        showDialog("App requires to access your location", "Enable Location")
                    }

                } catch (e: Exception) {

                }
            }
        }
    }


    private fun showDialog(message: String, title: String) {
        val builder = context?.let { androidx.appcompat.app.AlertDialog.Builder(it) }
        builder?.setTitle(context?.let { CommonUtil.setTitle(title, it) })?.setMessage(message)
                ?.setCancelable(true)
                ?.setNegativeButton(context?.let { CommonUtil.setTitle("Cancel", it) }) { dialogInterface, i -> dialogInterface.dismiss() }
                ?.setPositiveButton(context?.let { CommonUtil.setTitle("Go to settings to enable", it) }) { dialogInterface, i ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }?.show()
    }

    override fun onStart() {
        super.onStart()
        currentId = userId
    }


    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        currentId = userId
        onetoOneViewModel.fetchMessage(LIMIT = 30,userId = userId);
        onetoOneViewModel.receiveMessageListener(MESSAGE_LISTENER, userId)
        onetoOneViewModel.addPresenceListener(StringContract.ListenerName.USER_LISTENER)




    }

    override fun onDetach() {
        super.onDetach()
        currentId = null
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        currentId = null
    }

    override fun onDestroy() {
        super.onDestroy()
        currentId = null
        timer = null
    }


    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
        parentJob.cancel()
        onetoOneViewModel.removeMessageListener(MESSAGE_LISTENER)
        onetoOneViewModel.removePresenceListener(StringContract.ListenerName.USER_LISTENER)

        stopRecording(false)
        oneToOneAdapter?.stopPlayer()
        timer()
        currentId = null
    }

}
