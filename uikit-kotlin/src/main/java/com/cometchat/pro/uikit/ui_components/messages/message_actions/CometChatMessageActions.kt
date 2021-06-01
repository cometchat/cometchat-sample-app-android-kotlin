package com.cometchat.pro.uikit.ui_components.messages.message_actions

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.model.Reaction
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.cometchat.pro.uikit.ui_components.messages.threaded_message_list.CometChatThreadMessageListActivity
import com.cometchat.pro.uikit.ui_components.messages.extensions.Extensions
import com.cometchat.pro.uikit.ui_components.messages.message_list.CometChatMessageListActivity
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import org.json.JSONObject

class CometChatMessageActions : BottomSheetDialogFragment() {

    private val INITIAL_REACTION_COUNT = 5
    private lateinit var threadMessage: TextView
    private lateinit var editMessage: TextView
    private lateinit var replyMessage: TextView
    private lateinit var forwardMessage: TextView
    private lateinit var deleteMessage: TextView
    private lateinit var copyMessage: TextView
    private lateinit var messageInfo: TextView
    private lateinit var shareMessage: TextView
    private lateinit var initialReactionLayout: LinearLayout
    private lateinit var sendMessagePrivately: TextView

    private var isShareVisible = false
    private var isThreadVisible = false
    private var isCopyVisible = false
    private var isEditVisible = false
    private var isDeleteVisible = false
    private var isForwardVisible = false
    private var isReplyVisible = false
    private var isMessageInfoVisible = false
    private var isReactionVisible = false
    private var isSendMessagePrivatelyVisible = false
    private var metadata : JSONObject? =null

    private var messageActionListener: MessageActionListener? = null

    private var type: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchArguments()
    }

    private fun fetchArguments() {
        if (arguments != null) {
            isCopyVisible = arguments!!.getBoolean("copyVisible")
            isThreadVisible = arguments!!.getBoolean("threadVisible")
            isEditVisible = arguments!!.getBoolean("editVisible")
            isDeleteVisible = arguments!!.getBoolean("deleteVisible")
            isReplyVisible = arguments!!.getBoolean("replyVisible")
            isForwardVisible = arguments!!.getBoolean("forwardVisible")
            isShareVisible = arguments!!.getBoolean("shareVisible")
            isMessageInfoVisible = arguments!!.getBoolean("messageInfoVisible")
            isReactionVisible = arguments!!.getBoolean("reactionVisible")
            isSendMessagePrivatelyVisible = arguments!!.getBoolean("sendMessagePrivately")
            var string = arguments!!.getString("metadata")
            if (string != null)
                metadata = JSONObject(string)

            type = arguments!!.getString("type")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        var views : View? = inflater.inflate(R.layout.fragment_cometchat_message_actions, container, false)
        views!!.viewTreeObserver.addOnGlobalLayoutListener {
            var dialog = dialog as BottomSheetDialog?
            // androidx should use: com.google.android.material.R.id.design_bottom_sheet
            var bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            var behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = 0
        }
        threadMessage = views.findViewById(R.id.start_thread)
        editMessage = views.findViewById(R.id.edit_message)
        replyMessage = views.findViewById(R.id.reply_message)
        forwardMessage = views.findViewById(R.id.forward_message)
        deleteMessage = views.findViewById(R.id.delete_message)
        copyMessage = views.findViewById(R.id.copy_message)
        shareMessage = views.findViewById(R.id.share_message)
        messageInfo = views.findViewById(R.id.message_info)
        initialReactionLayout = views.findViewById(R.id.initial_reactions)
        sendMessagePrivately = views.findViewById(R.id.send_message_privately)

        if (isThreadVisible) threadMessage.visibility = View.VISIBLE else threadMessage.visibility = View.GONE
        if (isCopyVisible) copyMessage.visibility = View.VISIBLE else copyMessage.visibility = View.GONE
        if (isEditVisible) editMessage.visibility = View.VISIBLE else editMessage.visibility = View.GONE
        if (isDeleteVisible) deleteMessage.visibility = View.VISIBLE else deleteMessage.visibility = View.GONE
        if (isReplyVisible) replyMessage.visibility = View.VISIBLE else replyMessage.visibility = View.GONE
        if (isForwardVisible) forwardMessage.visibility = View.VISIBLE else forwardMessage.visibility = View.GONE
        if (isShareVisible) shareMessage.visibility = View.VISIBLE else shareMessage.visibility = View.GONE
        if (isMessageInfoVisible) messageInfo.visibility = View.VISIBLE else messageInfo.visibility = View.GONE
        if (isReactionVisible) initialReactionLayout.visibility = View.VISIBLE else initialReactionLayout.visibility = View.GONE
        if (isSendMessagePrivatelyVisible) sendMessagePrivately.visibility = View.VISIBLE else sendMessagePrivately.visibility = View.GONE

        val initialReactionList = Extensions.getInitialReactions(INITIAL_REACTION_COUNT)
        for (reaction in initialReactionList){
            val vw: View = LayoutInflater.from(context).inflate(R.layout.reaction_list_row, null)
            val tvReaction: TextView = vw.findViewById(R.id.tv_reaction)
            val layoutParam: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParam.rightMargin = 16
            layoutParam.leftMargin = 16
            layoutParam.topMargin = 8
            layoutParam.bottomMargin = 8
            tvReaction.layoutParams = layoutParam
            tvReaction.text = reaction.name
            initialReactionLayout.addView(vw)
            tvReaction.setOnClickListener(View.OnClickListener {
                if (messageActionListener != null) messageActionListener!!.onReactionClick(reaction)
                dismiss()
            })
        }

        val imageView = ImageView(context)
        imageView.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.add_reaction))
        imageView.setColorFilter(R.color.primaryTextColor)
        val layoutParam: LinearLayout.LayoutParams = LinearLayout.LayoutParams(Utils.dpToPx(context!!,34f).toInt(), Utils.dpToPx(context!!,34f).toInt())
        layoutParam.topMargin = 8
        layoutParam.leftMargin = 16
        imageView.layoutParams = layoutParam
        initialReactionLayout.addView(imageView)
        imageView.setOnClickListener(View.OnClickListener {
//            Toast.makeText(context,"clicked",Toast.LENGTH_LONG).show()
            if (messageActionListener != null) messageActionListener!!.onReactionClick(Reaction("add_reaction", 0))
            dismiss()
        })



        threadMessage.setOnClickListener {
            if (messageActionListener != null) messageActionListener!!.onThreadMessageClick()
            dismiss()
        }
        copyMessage.setOnClickListener {
            if (messageActionListener != null) messageActionListener!!.onCopyMessageClick()
            dismiss()
        }
        editMessage.setOnClickListener {
            if (messageActionListener != null) messageActionListener!!.onEditMessageClick()
            dismiss()
        }
        deleteMessage.setOnClickListener {
            if (messageActionListener != null) messageActionListener!!.onDeleteMessageClick()
            dismiss()
        }
        forwardMessage.setOnClickListener {
            if (messageActionListener != null) messageActionListener!!.onForwardMessageClick()
            dismiss()
        }
        replyMessage.setOnClickListener {
            if (messageActionListener != null) messageActionListener!!.onReplyMessageClick()
            dismiss()
        }
        shareMessage.setOnClickListener {
            if (messageActionListener != null) messageActionListener!!.onShareMessageClick()
            dismiss()
        }
        messageInfo.setOnClickListener {
            if (messageActionListener != null) messageActionListener!!.onMessageInfoClick()
            dismiss()
        }
        sendMessagePrivately.setOnClickListener {
            if (messageActionListener != null) messageActionListener?.onSendMessagePrivatelyClick(metadata)
        }
        return views
    }

    fun setMessageActionListener(messageActionListener: MessageActionListener) {
        this.messageActionListener = messageActionListener
    }

    interface MessageActionListener {
        fun onThreadMessageClick()
        fun onEditMessageClick()
        fun onReplyMessageClick()
        fun onForwardMessageClick()
        fun onDeleteMessageClick()
        fun onCopyMessageClick()
        fun onShareMessageClick()
        fun onMessageInfoClick()
        fun onReactionClick(reaction: Reaction)
        fun onSendMessagePrivatelyClick(metadata : JSONObject?)
    }

    override fun onDismiss(dialog: DialogInterface) {
        var activity: Activity? = activity
        if (activity != null)
            if (type != null && type == CometChatMessageListActivity::class.java.name)
                (activity as CometChatMessageListActivity).handleDialogClose(dialog)
            else
                (activity as CometChatThreadMessageListActivity).handleDialogClose(dialog)
    }

    override fun onCancel(dialog: DialogInterface) {
        var activity: Activity? = activity
        if (activity != null)
            if (type != null && type == CometChatMessageListActivity::class.java.name)
                (activity as CometChatMessageListActivity).handleDialogClose(dialog)
            else
                (activity as CometChatThreadMessageListActivity).handleDialogClose(dialog)
    }
}