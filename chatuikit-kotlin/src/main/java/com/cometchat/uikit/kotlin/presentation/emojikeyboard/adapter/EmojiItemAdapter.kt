package com.cometchat.uikit.kotlin.presentation.emojikeyboard.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.emojikeyboard.model.Emoji

/**
 * Callback interface for emoji click and long-click events.
 *
 * This interface mirrors the `EmojiKeyBoardView.OnClick` interface from the Java `chatuikit` module.
 * It will be replaced by `EmojiKeyBoardView.OnClick` once `EmojiKeyBoardView` is implemented.
 */
interface EmojiItemOnClick {
    fun onClick(emoji: String)
    fun onLongClick(emoji: String)
}

/**
 * RecyclerView adapter for rendering individual emoji items in an 8-column grid.
 *
 * Each item inflates `cometchat_emoji_item.xml` and binds the emoji unicode string
 * to the `cometchat_emoji_item_text` TextView. Click and long-click events are
 * forwarded to the provided [EmojiItemOnClick] callback.
 *
 * @param context The context used for layout inflation.
 * @param onClick Optional callback for emoji click and long-click events.
 */
class EmojiItemAdapter(
    private val context: Context,
    private val onClick: EmojiItemOnClick?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var emojiList: List<Emoji> = emptyList()

    fun setEmojiList(emojiList: List<Emoji>) {
        this.emojiList = emojiList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return EmojiItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.cometchat_emoji_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as EmojiItemViewHolder).bind(emojiList[position])
    }

    override fun getItemCount(): Int = emojiList.size

    inner class EmojiItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cometchatEmojiItemText: TextView =
            itemView.findViewById(R.id.cometchat_emoji_item_text)

        fun bind(emoji: Emoji) {
            cometchatEmojiItemText.text = emoji.emoji
            cometchatEmojiItemText.setOnClickListener {
                onClick?.onClick(emoji.emoji)
            }
            cometchatEmojiItemText.setOnLongClickListener {
                onClick?.onLongClick(emoji.emoji)
                true
            }
        }
    }
}
