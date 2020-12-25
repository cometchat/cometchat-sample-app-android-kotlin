package screen.messagelist

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.cometchat.pro.uikit.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import screen.threadconversation.CometChatThreadMessageActivity

class MessageActionFragment : BottomSheetDialogFragment() {

    private var threadMessage: TextView? = null
    private var editMessage: TextView? = null
    private var replyMessage: TextView? = null
    private var forwardMessage: TextView? = null
    private var deleteMessage: TextView? = null
    private var copyMessage: TextView? = null
    private var messageInfo: TextView? = null
    private var shareMessage: TextView? = null

    private var isShareVisible = false
    private var isThreadVisible = false
    private var isCopyVisible = false
    private var isEditVisible = false
    private var isDeleteVisible = false
    private var isForwardVisible = false
    private var isReplyVisible = false
    private var isMessageInfoVisible = false

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
            type = arguments!!.getString("type")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        var views : View? = inflater.inflate(R.layout.fragment_message_action, container, false)
        views!!.viewTreeObserver.addOnGlobalLayoutListener {
            var dialog = dialog as BottomSheetDialog?
            // androidx should use: com.google.android.material.R.id.design_bottom_sheet
            var bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            var behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = 0
        }
        threadMessage = views!!.findViewById(R.id.start_thread)
        editMessage = views!!.findViewById(R.id.edit_message)
        replyMessage = views!!.findViewById(R.id.reply_message)
        forwardMessage = views!!.findViewById(R.id.forward_message)
        deleteMessage = views!!.findViewById(R.id.delete_message)
        copyMessage = views!!.findViewById(R.id.copy_message)
        shareMessage = views!!.findViewById(R.id.share_message)
        messageInfo = views!!.findViewById(R.id.message_info)

        if (isThreadVisible) threadMessage!!.setVisibility(View.VISIBLE) else threadMessage!!.setVisibility(View.GONE)
        if (isCopyVisible) copyMessage!!.setVisibility(View.VISIBLE) else copyMessage!!.setVisibility(View.GONE)
        if (isEditVisible) editMessage!!.setVisibility(View.VISIBLE) else editMessage!!.setVisibility(View.GONE)
        if (isDeleteVisible) deleteMessage!!.setVisibility(View.VISIBLE) else deleteMessage!!.setVisibility(View.GONE)
        if (isReplyVisible) replyMessage!!.setVisibility(View.VISIBLE) else replyMessage!!.setVisibility(View.GONE)
        if (isForwardVisible) forwardMessage!!.setVisibility(View.VISIBLE) else forwardMessage!!.setVisibility(View.GONE)
        if (isShareVisible) shareMessage!!.setVisibility(View.VISIBLE) else shareMessage!!.setVisibility(View.GONE)
        if (isMessageInfoVisible) messageInfo!!.setVisibility(View.VISIBLE) else messageInfo!!.setVisibility(View.GONE)

        threadMessage!!.setOnClickListener {
            if (messageActionListener != null) messageActionListener!!.onThreadMessageClick()
            dismiss()
        }
        copyMessage!!.setOnClickListener {
            if (messageActionListener != null) messageActionListener!!.onCopyMessageClick()
            dismiss()
        }
        editMessage!!.setOnClickListener {
            if (messageActionListener != null) messageActionListener!!.onEditMessageClick()
            dismiss()
        }
        deleteMessage!!.setOnClickListener {
            if (messageActionListener != null) messageActionListener!!.onDeleteMessageClick()
            dismiss()
        }
        forwardMessage!!.setOnClickListener {
            if (messageActionListener != null) messageActionListener!!.onForwardMessageClick()
            dismiss()
        }
        replyMessage!!.setOnClickListener {
            if (messageActionListener != null) messageActionListener!!.onReplyMessageClick()
            dismiss()
        }
        shareMessage!!.setOnClickListener {
            if (messageActionListener != null) messageActionListener!!.onShareMessageClick()
            dismiss()
        }
        messageInfo!!.setOnClickListener {
            if (messageActionListener != null) messageActionListener!!.onMessageInfoClick()
            dismiss()
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
    }

    override fun onDismiss(dialog: DialogInterface) {
        var activity: Activity? = activity
        if (activity != null)
            if (type != null && type == CometChatMessageListActivity::class.java.name)
                (activity as CometChatMessageListActivity).handleDialogClose(dialog)
            else
                (activity as CometChatThreadMessageActivity).handleDialogClose(dialog)
    }

    override fun onCancel(dialog: DialogInterface) {
        var activity: Activity? = activity
        if (activity != null)
            if (type != null && type == CometChatMessageListActivity::class.java.name)
                (activity as CometChatMessageListActivity).handleDialogClose(dialog)
            else
                (activity as CometChatThreadMessageActivity).handleDialogClose(dialog)
    }
}