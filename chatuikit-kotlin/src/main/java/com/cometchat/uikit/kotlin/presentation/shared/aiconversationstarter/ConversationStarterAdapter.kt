package com.cometchat.uikit.kotlin.presentation.shared.aiconversationstarter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatAiConversationStarterRowBinding

/**
 * ConversationStarterAdapter is a RecyclerView adapter for displaying
 * AI-generated conversation starter suggestions.
 *
 * Each item displays a text suggestion that users can tap to start a conversation.
 * The adapter supports customization of item appearance including background color,
 * corner radius, stroke, and text styling.
 */
class ConversationStarterAdapter : RecyclerView.Adapter<ConversationStarterAdapter.ViewHolder>() {

    companion object {
        private val TAG = ConversationStarterAdapter::class.java.simpleName
    }

    private val list: MutableList<String> = mutableListOf()

    // Item style properties
    private var itemBackgroundDrawable: Drawable? = null
    @ColorInt private var itemBackgroundColor: Int = 0
    @ColorInt private var itemStrokeColor: Int = 0
    @Dimension private var itemStrokeWidth: Int = 0
    @Dimension private var itemCornerRadius: Int = 0
    @ColorInt private var itemTextColor: Int = 0
    @StyleRes private var itemTextAppearance: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CometchatAiConversationStarterRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(list[position])
    }

    override fun getItemCount(): Int = list.size

    /**
     * Updates the list of conversation starters.
     *
     * @param newList The new list of conversation starter strings.
     */
    fun setList(newList: List<String>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    /**
     * Sets the background color for each item.
     */
    fun setItemBackgroundColor(@ColorInt itemBackgroundColor: Int) {
        this.itemBackgroundColor = itemBackgroundColor
    }

    /**
     * Sets the stroke color for each item.
     */
    fun setItemStrokeColor(@ColorInt itemStrokeColor: Int) {
        this.itemStrokeColor = itemStrokeColor
    }

    /**
     * Sets the stroke width for each item.
     */
    fun setItemStrokeWidth(@Dimension itemStrokeWidth: Int) {
        this.itemStrokeWidth = itemStrokeWidth
    }

    /**
     * Sets the corner radius for each item.
     */
    fun setItemCornerRadius(@Dimension itemCornerRadius: Int) {
        this.itemCornerRadius = itemCornerRadius
    }

    /**
     * Sets the text color for each item.
     */
    fun setItemTextColor(@ColorInt itemTextColor: Int) {
        this.itemTextColor = itemTextColor
    }

    /**
     * Sets the text appearance for each item.
     */
    fun setItemTextAppearance(@StyleRes itemTextAppearance: Int) {
        this.itemTextAppearance = itemTextAppearance
    }

    /**
     * Sets the background drawable for each item.
     */
    fun setItemBackgroundDrawable(itemBackgroundDrawable: Drawable?) {
        this.itemBackgroundDrawable = itemBackgroundDrawable
    }

    /**
     * ViewHolder for conversation starter items.
     */
    inner class ViewHolder(
        private val binding: CometchatAiConversationStarterRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds the reply text to the view and applies styling.
         *
         * @param reply The conversation starter text to display.
         */
        fun bindView(reply: String) {
            binding.tvMessage.text = reply
            binding.messageCard.setCardBackgroundColor(itemBackgroundColor)
            binding.messageCard.strokeColor = itemStrokeColor
            binding.messageCard.strokeWidth = itemStrokeWidth
            binding.messageCard.radius = itemCornerRadius.toFloat()
            binding.tvMessage.setTextColor(itemTextColor)
            if (itemTextAppearance != 0) {
                binding.tvMessage.setTextAppearance(itemTextAppearance)
            }
            itemBackgroundDrawable?.let {
                binding.messageCard.background = it
            }
            itemView.setTag(R.string.cometchat_reply_lowercase, reply)
        }
    }
}
