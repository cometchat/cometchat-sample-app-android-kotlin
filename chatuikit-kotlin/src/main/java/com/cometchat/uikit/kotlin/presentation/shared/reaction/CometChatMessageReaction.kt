package com.cometchat.uikit.kotlin.presentation.shared.reaction

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.StyleRes
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.ReactionCount
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.card.MaterialCardView

/**
 * Callback invoked when a reaction chip is clicked.
 */
fun interface OnReactionClick {
    fun onClick(reaction: String, message: BaseMessage)
}

/**
 * Callback invoked when a reaction chip is long-pressed.
 */
fun interface OnReactionLongClick {
    fun onReactionLongClick(reaction: String, message: BaseMessage)
}

/**
 * Callback invoked when the "view more" / "add more reactions" chip is clicked.
 */
fun interface OnAddMoreReactionsClick {
    fun onAddMoreReactionsClick(message: BaseMessage)
}

/**
 * Container view that manages a FlexboxLayout of [CometChatReaction] chips.
 *
 * Direct 1:1 port of `CometChatMessageReaction.java` from the Java chatuikit module.
 */
class CometChatMessageReaction @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val parentView: FlexboxLayout
    @StyleRes private var reactionStyle: Int = 0
    private var onReactionClick: OnReactionClick? = null
    private var onReactionLongClick: OnReactionLongClick? = null
    private var onAddMoreReactionsClick: OnAddMoreReactionsClick? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.cometchat_message_reactions, this, true)
        parentView = view.findViewById(R.id.parent_view)
    }

    /**
     * Binds reaction data from a message to the FlexboxLayout, creating chips for each reaction.
     *
     * @param baseMessage The message containing reactions.
     * @param reactionLimit Maximum number of individual reaction chips to show before collapsing.
     */
    fun bindReactionsToMessage(baseMessage: BaseMessage, reactionLimit: Int) {
        val reactionsList: List<ReactionCount> = baseMessage.reactions ?: emptyList()
        parentView.removeAllViews()
        if (reactionsList.isEmpty()) {
            parentView.visibility = GONE
            return
        }
        parentView.visibility = VISIBLE
        val maxLimit = minOf(reactionsList.size, reactionLimit)
        for (i in 0 until maxLimit) {
            val reactionCount = reactionsList[i]
            parentView.addView(
                getReactionChip(
                    reactionCount.reaction,
                    reactionCount.count,
                    reactionCount.reactedByMe,
                    { handleReactionClick(baseMessage, reactionCount) },
                    { handleReactionLongClick(baseMessage, reactionCount) }
                )
            )
        }
        if (reactionsList.size > reactionLimit) {
            val viewMoreCount = reactionsList.size - reactionLimit
            val flag = isReactedByMeBeyondLimit(reactionsList, reactionLimit)
            parentView.addView(
                getReactionChip("+", viewMoreCount, flag, { handleViewMoreClick(baseMessage) }, null)
            )
        }
    }

    private fun getReactionChip(
        emoji: String,
        count: Int,
        reactedByMe: Boolean,
        clickEvent: OnClickListener?,
        longClickEvent: OnLongClickListener?
    ): View {
        val cometchatReaction = CometChatReaction(context)
        if (reactionStyle != 0) cometchatReaction.setStyle(reactionStyle)
        return cometchatReaction.buildReactionView(emoji, count, reactedByMe, clickEvent, longClickEvent)
    }

    private fun handleReactionClick(baseMessage: BaseMessage, reactionCount: ReactionCount) {
        onReactionClick?.onClick(reactionCount.reaction, baseMessage)
    }

    private fun handleReactionLongClick(baseMessage: BaseMessage, reactionCount: ReactionCount): Boolean {
        onReactionLongClick?.onReactionLongClick(reactionCount.reaction, baseMessage)
        return false
    }

    private fun isReactedByMeBeyondLimit(reactionsList: List<ReactionCount>, limit: Int): Boolean {
        for (i in limit until reactionsList.size) {
            if (reactionsList[i].reactedByMe) return true
        }
        return false
    }

    private fun handleViewMoreClick(baseMessage: BaseMessage) {
        onAddMoreReactionsClick?.onAddMoreReactionsClick(baseMessage)
    }

    fun setStyle(@StyleRes reactionStyle: Int) {
        this.reactionStyle = reactionStyle
    }

    fun setOnReactionClick(onReactionClick: OnReactionClick?) {
        this.onReactionClick = onReactionClick
    }

    fun setOnReactionLongClick(onReactionLongClick: OnReactionLongClick?) {
        this.onReactionLongClick = onReactionLongClick
    }

    fun setOnAddMoreReactionsClick(onAddMoreReactionsClick: OnAddMoreReactionsClick?) {
        this.onAddMoreReactionsClick = onAddMoreReactionsClick
    }
}
