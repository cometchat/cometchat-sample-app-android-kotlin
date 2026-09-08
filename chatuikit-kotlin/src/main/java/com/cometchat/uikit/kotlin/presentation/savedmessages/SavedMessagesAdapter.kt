package com.cometchat.uikit.kotlin.presentation.savedmessages

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.conversations.utils.ConversationUtils
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.DatePattern
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Adapter for the user-level Saved Messages screen. Each row is a dedicated saved-message item
 * ([R.layout.cometchat_saved_message_item]) that mirrors the Conversations row visuals — avatar,
 * title, subtitle (sender prefix + message-type icon + preview) and timestamp — but is self-contained
 * to the saved feature: no unread badge, receipt, or selection chrome. It composes the shared atoms
 * (avatar, date) and the shared preview helpers directly rather than reusing the Conversations item.
 * Tapping a row opens its conversation at the message; long-pressing offers an unsave option.
 */
internal class SavedMessagesAdapter(
    private val onRowClick: (BaseMessage) -> Unit,
    private val onUnsaveClick: (BaseMessage) -> Unit
) : RecyclerView.Adapter<SavedMessagesAdapter.SavedViewHolder>() {

    private val items = mutableListOf<BaseMessage>()
    private var textFormatters: List<CometChatTextFormatter> = emptyList()

    fun setTextFormatters(formatters: List<CometChatTextFormatter>) {
        textFormatters = formatters
        notifyDataSetChanged()
    }

    fun submit(messages: List<BaseMessage>) {
        items.clear()
        items.addAll(messages)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cometchat_saved_message_item, parent, false)
        return SavedViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SavedViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class SavedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: CometChatAvatar = itemView.findViewById(R.id.saved_item_avatar)
        private val title: TextView = itemView.findViewById(R.id.saved_item_title)
        private val senderPrefix: TextView = itemView.findViewById(R.id.saved_item_sender_prefix)
        private val typeIcon: ImageView = itemView.findViewById(R.id.saved_item_type_icon)
        private val preview: TextView = itemView.findViewById(R.id.saved_item_preview)
        private val date: CometChatDate = itemView.findViewById(R.id.saved_item_date)
        private val popupMenu = CometChatPopupMenu(itemView.context, 0)

        init {
            date.setDateTextAlignment(View.TEXT_ALIGNMENT_VIEW_END)
            date.setDateTextColor(CometChatTheme.getTextColorSecondary(itemView.context))
            date.setTransparentBackground(true)
        }

        fun bind(message: BaseMessage) {
            val context = itemView.context

            bindHeader(message)

            // Subtitle: sender prefix ("You: " / "John: "), message-type icon, and preview text —
            // reusing the same shared helpers the Conversations subtitle uses, so previews stay
            // consistent across the app.
            val prefix = getSavedMessagePrefix(context, message)
            senderPrefix.visibility = if (prefix.isNotEmpty()) View.VISIBLE else View.GONE
            senderPrefix.text = prefix

            val icon = ConversationUtils.getLastMessageIcon(message)
            if (icon != null) {
                typeIcon.visibility = View.VISIBLE
                typeIcon.setImageResource(icon)
            } else {
                typeIcon.visibility = View.GONE
            }

            preview.text = ConversationUtils.getFormattedLastMessageText(context, message, textFormatters)

            if (message.sentAt > 0) {
                date.visibility = View.VISIBLE
                date.setDate(message.sentAt, DatePattern.DAY_DATE_TIME)
            } else {
                date.visibility = View.GONE
            }

            itemView.setOnClickListener { onRowClick(message) }
            itemView.setOnLongClickListener {
                showUnsaveMenu(message)
                true
            }
        }

        /**
         * Sender prefix for a saved row. Unlike the Conversations prefix (group-only), the saved
         * list also prefixes 1-1 messages the logged-in user sent with "You: " — out of their home
         * conversation, ownership isn't obvious from the row title alone.
         */
        private fun getSavedMessagePrefix(context: Context, message: BaseMessage): String {
            val prefix = ConversationUtils.getMessagePrefix(context, message)
            if (prefix.isNotEmpty()) return prefix
            if (message is Action) return ""
            val myUid = try {
                CometChatUIKit.getLoggedInUser()?.uid
            } catch (e: Exception) {
                null
            }
            return if (myUid != null && message.sender?.uid == myUid) {
                "${context.getString(R.string.cometchat_you)}: "
            } else {
                ""
            }
        }

        /** Resolves the row's avatar + title from the message's source conversation (group or peer). */
        private fun bindHeader(message: BaseMessage) {
            if (message.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                val group = message.receiver as? Group
                val name = group?.name ?: message.receiverUid
                title.text = name
                if (group != null) avatar.setAvatar(group) else avatar.setAvatar(name, null as String?)
            } else {
                val myUid = try {
                    CometChatUIKit.getLoggedInUser()?.uid
                } catch (e: Exception) {
                    null
                }
                // The other party: if I sent it, the receiver; otherwise the sender.
                val peer: User? = if (message.sender?.uid == myUid) {
                    message.receiver as? User
                } else {
                    message.sender
                }
                val name = peer?.name ?: peer?.uid ?: message.receiverUid
                title.text = name
                if (peer != null) avatar.setAvatar(peer) else avatar.setAvatar(name, null as String?)
            }
        }

        private fun showUnsaveMenu(message: BaseMessage) {
            val unsaveIcon = ResourcesCompat.getDrawable(
                itemView.resources,
                com.cometchat.uikit.core.R.drawable.cometchat_ic_bookmark_filled,
                null
            )
            val item = CometChatPopupMenu.MenuItem.withIcons(
                id = UIKitConstants.MessageOption.UNSAVE,
                name = itemView.context.getString(R.string.cometchat_unsave),
                startIcon = unsaveIcon
            ) {
                popupMenu.dismiss()
                onUnsaveClick(message)
            }
            popupMenu.setMenuItems(listOf(item))
            popupMenu.show(itemView)
        }
    }
}
