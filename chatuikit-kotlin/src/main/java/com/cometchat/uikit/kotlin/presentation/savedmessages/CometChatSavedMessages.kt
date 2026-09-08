package com.cometchat.uikit.kotlin.presentation.savedmessages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.events.onMessageSaved
import com.cometchat.uikit.core.events.onMessageUnsaved
import com.cometchat.uikit.core.factory.CometChatSavedMessagesViewModelFactory
import com.cometchat.uikit.core.state.PinnedSavedListUIState
import com.cometchat.uikit.core.viewmodel.CometChatSavedMessagesViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.dialog.CometChatConfirmDialog
import com.cometchat.uikit.kotlin.presentation.shared.toolbar.CometChatToolbar
import com.cometchat.uikit.kotlin.presentation.shared.toolbar.CometChatToolbarStyle
import com.cometchat.uikit.kotlin.shared.formatters.CometChatMentionsFormatter
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import kotlinx.coroutines.launch

/**
 * A full-screen, user-level list of the current user's saved (bookmarked) messages across all
 * conversations, newest save first. Private to the user; read-only. Each row shows its source
 * conversation so the cross-conversation list stays legible. Wire navigation via
 * [setOnMessageClickListener] (open the conversation at that message) and [setOnBackClickListener].
 */
class CometChatSavedMessages @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val toolbar: CometChatToolbar
    private val emptyView: View
    private val errorView: View
    private val recyclerView: RecyclerView

    private val viewModel: CometChatSavedMessagesViewModel =
        CometChatSavedMessagesViewModelFactory().create(CometChatSavedMessagesViewModel::class.java)

    private val adapter = SavedMessagesAdapter(
        onRowClick = { onMessageClickListener?.invoke(it) },
        // The row's long-press "Unsave" option routes here; unsave itself still needs a confirm
        // (mirrors the Compose screen and the pinned screen's unpin flow).
        onUnsaveClick = { confirmUnsave(it) }
    )

    private var onMessageClickListener: ((BaseMessage) -> Unit)? = null
    private var onBackClickListener: (() -> Unit)? = null
    private var loaded = false

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.cometchat_saved_messages, this, true)
        toolbar = findViewById(R.id.saved_messages_toolbar)
        emptyView = findViewById(R.id.saved_messages_empty)
        errorView = findViewById(R.id.saved_messages_error)
        recyclerView = findViewById(R.id.saved_messages_recycler)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        // Default mentions formatter so subtitle previews style mentions exactly like the
        // Conversations list item (same helper, same formatter).
        adapter.setTextFormatters(listOf(CometChatMentionsFormatter(context)))
        // Title styled exactly like the CometChatUsers toolbar (Heading1Bold, same as its "Users" title).
        toolbar.setStyle(
            CometChatToolbarStyle.default(context).copy(
                titleTextAppearance = CometChatTheme.getTextAppearanceHeading1Bold(context)
            )
        )
        toolbar.setTitle(context.getString(R.string.cometchat_saved_messages))
        toolbar.setOnBackPress { onBackClickListener?.invoke() }
    }

    /**
     * Confirmation dialog before unsaving (long-press → "Unsave" → confirm). Mirrors the pinned
     * screen's unpin dialog: no icon, primary (non-destructive) positive button.
     */
    private fun confirmUnsave(message: BaseMessage) {
        val dialog = CometChatConfirmDialog(context, R.style.CometChatConfirmDialogStyle)
        dialog.hideDialogIcon(true)
        dialog.setTitleText(context.getString(R.string.cometchat_unsave_message_confirm_title))
        dialog.setSubtitleText(context.getString(R.string.cometchat_unsave_message_confirm_body))
        dialog.setPositiveButtonText(context.getString(R.string.cometchat_unsave))
        dialog.setNegativeButtonText(context.getString(R.string.cometchat_cancel))
        dialog.setOnPositiveButtonClick {
            viewModel.unsave(message)
            dialog.dismiss()
        }
        dialog.setOnNegativeButtonClick { dialog.dismiss() }
        dialog.setConfirmDialogElevation(0)
        dialog.setCancelable(false)
        dialog.show()
        // Non-destructive: primary (purple) positive button. Must be set AFTER show() (onCreate resets it).
        dialog.setPositiveButtonBackgroundColor(CometChatTheme.getPrimaryColor(context))
    }

    /** Called when a row is tapped — wire this to open the conversation at the message. */
    fun setOnMessageClickListener(listener: (BaseMessage) -> Unit) {
        onMessageClickListener = listener
    }

    /** Called when the toolbar back icon is tapped. */
    fun setOnBackClickListener(listener: () -> Unit) {
        onBackClickListener = listener
    }

    /**
     * Replaces the text formatters used to style subtitle previews (defaults to
     * [CometChatMentionsFormatter], matching the Conversations list).
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
            viewModel.unsaveSuccess.collect {
                Toast.makeText(
                    context,
                    context.getString(R.string.cometchat_message_unsaved),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        owner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Load failure: full-screen "Oops!" + retry, same as the conversations component.
                // The ViewModel reserves Error for load failures, so a healthy list is never
                // replaced by this state.
                emptyView.visibility = if (state is PinnedSavedListUIState.Empty) View.VISIBLE else View.GONE
                errorView.visibility = if (state is PinnedSavedListUIState.Error) View.VISIBLE else View.GONE
                recyclerView.visibility =
                    if (state is PinnedSavedListUIState.Empty || state is PinnedSavedListUIState.Error) View.GONE
                    else View.VISIBLE
            }
        }
        owner.onMessageSaved { viewModel.onMessageSavedExternally(it) }
        owner.onMessageUnsaved { viewModel.onMessageUnsavedExternally(it) }
        if (!loaded) {
            loaded = true
            viewModel.reload()
        }
    }
}
