package com.cometchat.uikit.kotlin.presentation.pinnedmessages

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.utils.PinSaveUtils
import com.cometchat.uikit.core.events.onMessagePinned
import com.cometchat.uikit.core.events.onMessageUnpinned
import com.cometchat.uikit.core.factory.CometChatPinnedMessagesViewModelFactory
import com.cometchat.uikit.core.state.PinnedSavedListUIState
import com.cometchat.uikit.core.viewmodel.CometChatPinnedMessagesViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.messageinformation.ui.CometChatMessageInformationBottomSheet
import com.cometchat.uikit.kotlin.presentation.shared.dialog.CometChatConfirmDialog
import com.cometchat.uikit.kotlin.presentation.shared.toolbar.CometChatToolbar
import com.cometchat.uikit.kotlin.presentation.shared.toolbar.CometChatToolbarStyle
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import kotlinx.coroutines.launch

/**
 * A full-screen list of a conversation's pinned messages (conversation-wide, newest pin first).
 * Read-only: it never marks messages read or changes unread counts. Set the conversation with
 * [setUser] or [setGroup]; wire navigation via [setOnMessageClickListener] (jump to the message)
 * and [setOnBackClickListener].
 */
class CometChatPinnedMessages @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val toolbar: CometChatToolbar
    private val emptyView: View
    private val errorView: View
    private val recyclerView: RecyclerView

    private val viewModel: CometChatPinnedMessagesViewModel =
        CometChatPinnedMessagesViewModelFactory().create(CometChatPinnedMessagesViewModel::class.java)

    private val adapter = PinnedMessagesAdapter(
        onRowClick = { onMessageClickListener?.invoke(it) },
        onOptionClick = { message, optionId -> handleOption(message, optionId) }
    )

    private var onMessageClickListener: ((BaseMessage) -> Unit)? = null
    private var onBackClickListener: (() -> Unit)? = null

    private var user: User? = null
    private var group: Group? = null

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.cometchat_pinned_messages, this, true)
        toolbar = findViewById(R.id.pinned_messages_toolbar)
        emptyView = findViewById(R.id.pinned_messages_empty)
        errorView = findViewById(R.id.pinned_messages_error)
        recyclerView = findViewById(R.id.pinned_messages_recycler)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        // Title styled exactly like the CometChatUsers toolbar (Heading1Bold, same as its "Users" title).
        toolbar.setStyle(
            CometChatToolbarStyle.default(context).copy(
                titleTextAppearance = CometChatTheme.getTextAppearanceHeading1Bold(context)
            )
        )
        toolbar.setTitle(context.getString(R.string.cometchat_pinned_messages))
        toolbar.setOnBackPress { onBackClickListener?.invoke() }
    }

    /** Configures the panel for a 1-1 conversation. */
    fun setUser(user: User) {
        this.user = user
        this.group = null
        adapter.setConversationContext(user, null)
        viewModel.configure(uid = user.uid, guid = null)
    }

    /** Configures the panel for a group conversation. */
    fun setGroup(group: Group) {
        this.user = null
        this.group = group
        adapter.setConversationContext(null, group)
        viewModel.configure(uid = null, guid = group.guid)
    }

    /** Called when a row is tapped — wire this to jump to the message in its conversation. */
    fun setOnMessageClickListener(listener: (BaseMessage) -> Unit) {
        onMessageClickListener = listener
    }

    /** Called when the toolbar back icon is tapped. */
    fun setOnBackClickListener(listener: () -> Unit) {
        onBackClickListener = listener
    }

    /**
     * Replaces the text formatters used to render pinned bubbles (defaults to
     * [com.cometchat.uikit.kotlin.shared.formatters.CometChatMentionsFormatter], matching the
     * message list).
     */
    fun setTextFormatters(formatters: List<CometChatTextFormatter>) {
        adapter.setTextFormatters(formatters)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val owner = findViewTreeLifecycleOwner() ?: return
        owner.lifecycleScope.launch {
            viewModel.messages.collect { adapter.submit(it) }
        }
        owner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Load failure (e.g. RBAC 403 on listPinnedMessages): full-screen "Oops!" + retry,
                // same as the conversations component. The ViewModel reserves Error for load
                // failures, so a healthy list is never replaced by this state.
                emptyView.visibility = if (state is PinnedSavedListUIState.Empty) View.VISIBLE else View.GONE
                errorView.visibility = if (state is PinnedSavedListUIState.Error) View.VISIBLE else View.GONE
                recyclerView.visibility =
                    if (state is PinnedSavedListUIState.Empty || state is PinnedSavedListUIState.Error) View.GONE
                    else View.VISIBLE
            }
        }
        owner.lifecycleScope.launch {
            viewModel.actionResult.collect { showActionToast(it) }
        }
        // Live upkeep from the UIKit bus (acting device + realtime once wired).
        owner.onMessagePinned { viewModel.onMessagePinnedExternally(it) }
        owner.onMessageUnpinned { viewModel.onMessageUnpinnedExternally(it) }
    }

    /**
     * Routes a long-press option selection to its action. Data-mutating actions live on the
     * ViewModel (shared with Compose); UI actions (info sheet, clipboard, delete confirm) are here.
     */
    private fun handleOption(message: BaseMessage, optionId: String) {
        when (optionId) {
            UIKitConstants.MessageOption.MESSAGE_INFORMATION -> showMessageInformation(message)
            UIKitConstants.MessageOption.COPY -> copyMessage(message)
            UIKitConstants.MessageOption.TRANSLATE -> viewModel.translate(message)
            UIKitConstants.MessageOption.PIN -> viewModel.pin(message)
            UIKitConstants.MessageOption.UNPIN -> confirmUnpin(message)
            UIKitConstants.MessageOption.DELETE -> confirmDelete(message)
        }
    }

    private fun showMessageInformation(message: BaseMessage) {
        val activity = Utils.getActivity(context) as? FragmentActivity ?: return
        CometChatMessageInformationBottomSheet.newInstance(message)
            .show(activity.supportFragmentManager, "pinned_message_information")
    }

    private fun copyMessage(message: BaseMessage) {
        val text = (message as? TextMessage)?.text ?: return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("message", text))
    }

    private fun confirmUnpin(message: BaseMessage) {
        val dialog = CometChatConfirmDialog(context, com.cometchat.uikit.kotlin.R.style.CometChatConfirmDialogStyle)
        dialog.hideDialogIcon(true)
        dialog.setTitleText(context.getString(R.string.cometchat_unpin_message_confirm_title))
        dialog.setSubtitleText(context.getString(R.string.cometchat_unpin_message_confirm_body))
        dialog.setPositiveButtonText(context.getString(R.string.cometchat_unpin))
        dialog.setNegativeButtonText(context.getString(R.string.cometchat_cancel))
        dialog.setOnPositiveButtonClick {
            viewModel.unpin(message)
            dialog.dismiss()
        }
        dialog.setOnNegativeButtonClick { dialog.dismiss() }
        dialog.setConfirmDialogElevation(0)
        dialog.setCancelable(false)
        dialog.show()
        // Non-destructive: primary (purple) positive button. Must be set AFTER show() (onCreate resets it).
        dialog.setPositiveButtonBackgroundColor(CometChatTheme.getPrimaryColor(context))
    }

    private fun confirmDelete(message: BaseMessage) {
        val dialog = CometChatConfirmDialog(context)
        dialog.setConfirmDialogIcon(
            ResourcesCompat.getDrawable(resources, R.drawable.cometchat_ic_delete, null)
        )
        dialog.setConfirmDialogIconTint(CometChatTheme.getErrorColor(context))
        dialog.setTitleText(context.getString(R.string.cometchat_delete_message_title))
        dialog.setSubtitleText(context.getString(R.string.cometchat_delete_message_subtitle))
        dialog.setPositiveButtonText(context.getString(R.string.cometchat_delete))
        dialog.setNegativeButtonText(context.getString(R.string.cometchat_cancel))
        dialog.setOnPositiveButtonClick {
            viewModel.delete(message)
            dialog.dismiss()
        }
        dialog.setOnNegativeButtonClick { dialog.dismiss() }
        dialog.setConfirmDialogElevation(0)
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun showActionToast(result: CometChatPinnedMessagesViewModel.PinnedActionResult) {
        val message = when (result) {
            CometChatPinnedMessagesViewModel.PinnedActionResult.DELETE_FAILED,
            CometChatPinnedMessagesViewModel.PinnedActionResult.PIN_FAILED,
            CometChatPinnedMessagesViewModel.PinnedActionResult.TRANSLATE_FAILED ->
                context.getString(R.string.cometchat_something_went_wrong)
            // The cap was hit: name it (from app settings) rather than a generic failure.
            CometChatPinnedMessagesViewModel.PinnedActionResult.PIN_LIMIT_REACHED ->
                PinSaveUtils.pinnedMessagesLimit()
                    ?.let { context.getString(R.string.cometchat_pin_limit_reached, it) }
                    ?: context.getString(R.string.cometchat_pin_limit_reached_unknown)
            // Pin/unpin is offered to every member; the server enforces RBAC — a denial
            // surfaces as the shared permission toast.
            CometChatPinnedMessagesViewModel.PinnedActionResult.PERMISSION_DENIED ->
                context.getString(R.string.cometchat_action_permission_denied)
            // Successful pin/delete update the row directly — no toast needed.
            CometChatPinnedMessagesViewModel.PinnedActionResult.DELETED,
            CometChatPinnedMessagesViewModel.PinnedActionResult.PINNED -> return
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
