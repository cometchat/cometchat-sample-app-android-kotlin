package com.cometchat.uikit.kotlin.presentation.pinnedmessages

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.utils.MessageOptionsUtils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.BubbleStyles
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.CometChatMessageBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.InternalContentRenderer
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.CometChatMessageBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.audiobubble.CometChatAudioBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.collaborativebubble.CometChatCollaborativeBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.filebubble.CometChatFileBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.imagebubble.CometChatImageBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.meetcallbubble.CometChatMeetCallBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.pollbubble.CometChatPollBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.stickerbubble.CometChatStickerBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.stickerbubble.CometChatStickerBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.textbubble.CometChatTextBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.videobubble.CometChatVideoBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.kotlin.shared.formatters.CometChatMentionsFormatter
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter for the Pinned Messages screen. Each row renders the real message bubble
 * ([CometChatMessageBubble]) — avatar, sender·date header, content and the pinned footer — exactly
 * as it appears in chat. Bubbles are rendered LEFT-aligned so the sender and date header show for
 * every pinned message (including your own), since the list is viewed out of conversation context.
 * Tapping a row jumps to the message; long-pressing offers an unpin option.
 */
internal class PinnedMessagesAdapter(
    private val onRowClick: (BaseMessage) -> Unit,
    private val onOptionClick: (BaseMessage, String) -> Unit
) : RecyclerView.Adapter<PinnedMessagesAdapter.PinnedViewHolder>() {

    private val items = mutableListOf<BaseMessage>()

    // Conversation context, needed so MessageOptionsUtils can apply per-message/permission rules
    // (e.g. delete only for own/admin) when building the long-press options.
    private var user: User? = null
    private var group: Group? = null

    fun setConversationContext(user: User?, group: Group?) {
        this.user = user
        this.group = group
    }

    /** The options shown on long-press of a pinned message, in display order. */
    private val allowedOptionOrder = listOf(
        UIKitConstants.MessageOption.MESSAGE_INFORMATION,
        UIKitConstants.MessageOption.COPY,
        // Text-only (the option map offers it for text messages); translates in place — the
        // bubble re-renders with the translation, exactly like the message list.
        UIKitConstants.MessageOption.TRANSLATE,
        UIKitConstants.MessageOption.PIN,
        UIKitConstants.MessageOption.UNPIN,
        // No THREAD_SUBSCRIPTION here, deliberately: a pinned thread message can be appended in
        // realtime over the websocket, and a socket-delivered message carries no threadSubscribed
        // flag — so this panel cannot know the thread's state and must not offer a toggle that
        // would render with a wrong label.
        UIKitConstants.MessageOption.DELETE
    )

    // Same default text formatters the message list uses (mentions), created once. Applied to every
    // bubble so mentions/links render identically to the message list — markdown itself is rendered
    // by the text bubble regardless, but this keeps the formatting pipeline in parity.
    private var textFormatters: List<CometChatTextFormatter>? = null

    private fun textFormatters(context: Context): List<CometChatTextFormatter> =
        textFormatters ?: listOf(CometChatMentionsFormatter(context)).also { textFormatters = it }

    // Own rows render LEFT-aligned but with the outgoing (purple) bubble, and the formatter picks
    // the mention style by alignment — LEFT gets the incoming look (primary-on-20%-primary), which
    // is illegible on purple. This variant remaps LEFT to the OUTGOING mention style (white on
    // primary-700). Note the setter names are alignment-swapped (RTL heritage): the "incoming"
    // setter feeds the styles that LEFT-aligned bubbles read.
    private var ownTextFormatters: List<CometChatTextFormatter>? = null

    private fun ownTextFormatters(context: Context): List<CometChatTextFormatter> =
        ownTextFormatters ?: listOf(
            CometChatMentionsFormatter(context).apply {
                setIncomingBubbleMentionTextStyle(context, R.style.CometChatOutgoingBubbleMentionsStyle)
            }
        ).also { ownTextFormatters = it }

    /**
     * Replaces the formatters used for every pinned bubble, matching
     * [com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageList.setTextFormatters].
     *
     * A supplied list is used for own and other rows alike, so the own-row mention remap above
     * applies only to the built-in default — a caller styling mentions themselves owns both looks.
     */
    fun setTextFormatters(formatters: List<CometChatTextFormatter>) {
        textFormatters = formatters
        ownTextFormatters = formatters
        notifyDataSetChanged()
    }

    fun submit(messages: List<BaseMessage>) {
        items.clear()
        items.addAll(messages)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PinnedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cometchat_pinned_message_item, parent, false)
        return PinnedViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: PinnedViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class PinnedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bubble: CometChatMessageBubble = itemView.findViewById(R.id.pinned_item_bubble)
        private val popupMenu = CometChatPopupMenu(itemView.context, 0)
        private val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        private var headerName: TextView? = null
        private var headerDate: TextView? = null

        fun bind(message: BaseMessage) {
            val context = itemView.context
            // Everything is LEFT-aligned so the avatar + sender·date header renders for every pinned
            // message, whoever sent it. For the logged-in user's own messages we still want the
            // outgoing (purple) look — so we push outgoing content styles into the LEFT layout and
            // label the sender "You".
            val isOwn = message.sender?.uid == CometChat.getLoggedInUser()?.uid

            bubble.setBubbleStyles(
                if (isOwn) BubbleStyles(
                    textBubbleStyle = CometChatTextBubbleStyle.outgoing(context),
                    imageBubbleStyle = CometChatImageBubbleStyle.outgoing(context),
                    videoBubbleStyle = CometChatVideoBubbleStyle.outgoing(context),
                    audioBubbleStyle = CometChatAudioBubbleStyle.outgoing(context),
                    fileBubbleStyle = CometChatFileBubbleStyle.outgoing(context),
                    // Extension bubbles (poll/sticker/whiteboard-document/meeting) also resolve
                    // their style from alignment (LEFT → incoming), so own messages need the
                    // outgoing variants — otherwise their content renders in incoming (dark)
                    // colours on the purple bubble.
                    pollBubbleStyle = CometChatPollBubbleStyle.outgoing(context),
                    stickerBubbleStyle = CometChatStickerBubbleStyle.outgoing(context),
                    collaborativeBubbleStyle = CometChatCollaborativeBubbleStyle.outgoing(context),
                    meetCallBubbleStyle = CometChatMeetCallBubbleStyle.outgoing(context)
                ) else BubbleStyles()
            )

            // Render media (image/video/audio/file) with the same multi-attachment "grid" bubbles
            // the message list uses, instead of the deprecated single-attachment bubbles. Must be set
            // before bindViews, which builds the content view. The grid bubbles wire their own
            // tap-to-open/preview listeners internally (via InternalContentRenderer), so no extra
            // media click wiring is needed here.
            bubble.setEnableMultipleAttachments(true)

            // Apply the same text formatters as the message list (before bindViews, which binds the
            // content view) so mentions/links render identically here. Own rows get the variant
            // whose LEFT-alignment mention style is the outgoing look (see ownTextFormatters).
            bubble.setTextFormatters(
                if (isOwn) ownTextFormatters(context) else textFormatters(context)
            )

            bubble.bindViews(message, UIKitConstants.MessageBubbleAlignment.LEFT)

            // Read-only list: no thread "N Replies" row under the bubble and no quoted-parent
            // reply preview above the content — the pinned row shows just the message itself.
            // Both must be re-hidden after every bindViews (it re-shows them per message).
            // Reactions still show via the default footer, but view-only — no reaction callbacks
            // are wired on this screen.
            bubble.setThreadViewVisibility(View.GONE)
            bubble.setReplyViewVisibility(View.GONE)

            // Outer bubble card colour: outgoing (purple) for own, incoming otherwise. Set explicitly
            // each bind so recycled rows never keep a previous message's colour. Stickers are the
            // exception — bindViews resolved their outer card to TRANSPARENT (same as the message
            // list) and overriding it here would wrap the sticker in a coloured card.
            val isSticker = message.category == CometChatConstants.CATEGORY_CUSTOM &&
                message.type == InternalContentRenderer.EXTENSION_STICKER
            if (!isSticker) {
                bubble.applyStyle(
                    if (isOwn) CometChatMessageBubbleStyle.outgoing(context)
                    else CometChatMessageBubbleStyle.incoming(context)
                )
            }

            // Custom "name • date" header (the stock bubble header can't render them adjacent).
            ensureCustomHeader()
            headerName?.text =
                if (isOwn) context.getString(R.string.cometchat_you)
                else message.sender?.name ?: message.sender?.uid ?: ""
            headerDate?.text = if (message.sentAt > 0) dateFormat.format(Date(message.sentAt * 1000)) else ""

            // Own messages: footer (pin/time) tinted white to sit on the purple outgoing bubble
            // (the shared status-info view tints it dark for LEFT). Not for stickers — their card
            // is transparent, so a white footer would vanish on the light screen background.
            if (isOwn && !isSticker) tintFooterWhite()

            // Tap (jump) acts on the bubble card only — R.id.message_bubble is wrap_content, so the
            // empty space beside the bubble doesn't react to taps. The card's Material ripple is
            // disabled (per design — no highlight).
            val bubbleCard = bubble.findViewById<View?>(R.id.message_bubble)
            (bubbleCard as? MaterialCardView)?.rippleColor =
                ColorStateList.valueOf(Color.TRANSPARENT)
            bubbleCard?.setOnClickListener { onRowClick(message) }
            bubbleCard?.setOnLongClickListener {
                showOptionsMenu(message)
                true
            }
            itemView.setOnClickListener(null)
            itemView.isClickable = false
            // Long-press must ALSO live on the row: every content bubble (text/media/sticker/poll/
            // collaborative/…) consumes its own long-press and re-routes it to the RecyclerView row
            // via Utils.performAdapterClick → itemView.performLongClick(). With the row inert, that
            // routing dead-ended and long-pressing the bubble CONTENT never opened the options menu
            // (only the card's uncovered padding/header did). The card listener above still handles
            // those edge areas.
            itemView.setOnLongClickListener {
                showOptionsMenu(message)
                true
            }

            // Stickers consume plain taps too (their own internal click listener), which would
            // otherwise swallow the row's tap-to-jump — wire it explicitly.
            if (isSticker) {
                findStickerBubble(bubble)?.setOnStickerClickListener { onRowClick(message) }
            }
        }

        /** Depth-first search for the sticker content view inside the bound bubble. */
        private fun findStickerBubble(view: View): CometChatStickerBubble? {
            if (view is CometChatStickerBubble) return view
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    findStickerBubble(view.getChildAt(i))?.let { return it }
                }
            }
            return null
        }

        /**
         * Installs the custom "name • date" header on the bubble once (replacing the stock header),
         * and enlarges the sender name by 2sp. Called after the first bindViews so the bubble's
         * header container exists; the custom header then persists across rebinds.
         */
        private fun ensureCustomHeader() {
            if (headerName != null) return
            val header = LayoutInflater.from(itemView.context)
                .inflate(R.layout.cometchat_pinned_message_header, bubble.getHeaderView(), false)
            bubble.setHeaderView(header)
            headerName = header.findViewById(R.id.pinned_header_name)
            headerDate = header.findViewById(R.id.pinned_header_date)
            headerName?.let {
                val twoSpPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP, 2f, itemView.resources.displayMetrics
                )
                it.setTextSize(TypedValue.COMPLEX_UNIT_PX, it.textSize + twoSpPx)
            }
        }

        /** Recolors the footer (pin/saved indicators, dot, time) white for the outgoing own-bubble. */
        private fun tintFooterWhite() {
            bubble.findViewById<ImageView?>(R.id.status_pin_indicator)?.setColorFilter(Color.WHITE)
            bubble.findViewById<ImageView?>(R.id.status_saved_indicator)?.setColorFilter(Color.WHITE)
            bubble.findViewById<ImageView?>(R.id.status_pin_dot)?.setColorFilter(Color.WHITE)
            bubble.findViewById<CometChatDate?>(R.id.date_time)?.setDateTextColor(Color.WHITE)
        }

        /**
         * Shows the long-press options menu, restricted to Info / Copy / Pin / Unpin /
         * Stop notifications / Delete. Options are built via [MessageOptionsUtils] (so per-message
         * and permission rules still apply — e.g. Copy only for text, Delete only for own/admin,
         * Unpin for the already-pinned rows) and then filtered to the allowed set. Each selection is
         * routed back to the screen via [onOptionClick].
         */
        private fun showOptionsMenu(message: BaseMessage) {
            val context = itemView.context
            val errorColor = CometChatTheme.getErrorColor(context)

            val options = MessageOptionsUtils.getDefaultMessageOptions(context, message, user, group)
                .filter { allowedOptionOrder.contains(it.id) }
                .sortedBy { allowedOptionOrder.indexOf(it.id) }
            if (options.isEmpty()) return

            val menuItems = options.map { option ->
                val isDelete = option.id == UIKitConstants.MessageOption.DELETE
                CometChatPopupMenu.MenuItem(
                    id = option.id,
                    name = option.title,
                    startIcon = if (option.icon != 0)
                        ResourcesCompat.getDrawable(itemView.resources, option.icon, context.theme) else null,
                    startIconTint = if (isDelete) errorColor else option.iconTintColor,
                    textColor = if (isDelete) errorColor else option.titleColor
                ) {
                    popupMenu.dismiss()
                    onOptionClick(message, option.id)
                }
            }
            popupMenu.setMenuItems(menuItems)
            popupMenu.show(itemView)
        }
    }
}
