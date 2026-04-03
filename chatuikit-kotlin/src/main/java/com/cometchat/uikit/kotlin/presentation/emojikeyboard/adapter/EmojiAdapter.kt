package com.cometchat.uikit.kotlin.presentation.emojikeyboard.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.emojikeyboard.model.EmojiCategory

/**
 * RecyclerView adapter for the outer emoji category list.
 *
 * Each item represents one [EmojiCategory], inflating `cometchat_emoji_container.xml`
 * which contains a category name [TextView] and an inner [RecyclerView] displaying
 * emojis in an 8-column grid via [EmojiItemAdapter].
 *
 * @param context The context used for layout inflation and layout manager creation.
 * @param emojiCategories The list of emoji categories to display.
 */
class EmojiAdapter(
    private val context: Context,
    emojiCategories: List<EmojiCategory>?
) : RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>() {

    private var emojiCategories: List<EmojiCategory> = emojiCategories ?: emptyList()
    private val handler: Handler = Handler(Looper.getMainLooper())
    private var onClick: EmojiItemOnClick? = null
    @StyleRes
    private var categoryTextAppearance: Int = 0
    @ColorInt
    private var categoryTextColor: Int = 0

    /**
     * Updates the emoji categories and refreshes the adapter.
     */
    fun updateCategories(categories: List<EmojiCategory>) {
        this.emojiCategories = categories
        notifyDataSetChanged()
    }

    fun setOnClick(onClick: EmojiItemOnClick?) {
        if (onClick != null) {
            this.onClick = onClick
            notifyDataSetChanged()
        }
    }

    fun setCategoryTextAppearance(@StyleRes categoryTextAppearance: Int) {
        this.categoryTextAppearance = categoryTextAppearance
        notifyDataSetChanged()
    }

    fun setCategoryTextColor(@ColorInt categoryTextColor: Int) {
        this.categoryTextColor = categoryTextColor
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        return EmojiViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.cometchat_emoji_container, parent, false)
        )
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        holder.bind(emojiCategories[position])
    }

    override fun getItemCount(): Int = emojiCategories.size

    inner class EmojiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.category_name)
        private val emojiItemAdapter: EmojiItemAdapter = EmojiItemAdapter(context, onClick)

        init {
            val emojiListRecyclerView: RecyclerView = itemView.findViewById(R.id.emoji_list_view)
            val gridLayoutManager = GridLayoutManager(context, 8)
            emojiListRecyclerView.layoutManager = gridLayoutManager
            emojiListRecyclerView.adapter = emojiItemAdapter
        }

        fun bind(emojiCategory: EmojiCategory) {
            if (categoryTextAppearance != 0) {
                categoryName.setTextAppearance(categoryTextAppearance)
            }
            if (categoryTextColor != 0) {
                categoryName.setTextColor(categoryTextColor)
            }
            categoryName.text = emojiCategory.name
            Thread {
                handler.post {
                    emojiItemAdapter.setEmojiList(emojiCategory.emojis)
                    emojiItemAdapter.notifyDataSetChanged()
                }
            }.start()
        }
    }
}
