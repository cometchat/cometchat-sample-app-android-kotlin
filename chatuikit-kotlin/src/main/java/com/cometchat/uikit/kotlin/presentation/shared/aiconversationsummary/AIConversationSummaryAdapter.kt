package com.cometchat.uikit.kotlin.presentation.shared.aiconversationsummary

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatAiConversationSummaryRowBinding

/**
 * AIConversationSummaryAdapter is a RecyclerView adapter for displaying
 * AI-generated conversation summary text.
 *
 * Unlike the ConversationStarterAdapter which displays multiple clickable options,
 * this adapter typically displays a single summary text item that provides
 * an overview of the conversation history.
 *
 * The adapter supports customization of item appearance including background color,
 * corner radius, stroke, and text styling.
 */
class AIConversationSummaryAdapter : RecyclerView.Adapter<AIConversationSummaryAdapter.ViewHolder>() {

    companion object {
        private val TAG = AIConversationSummaryAdapter::class.java.simpleName
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
        val binding = CometchatAiConversationSummaryRowBinding.inflate(
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
     * Updates the list of summary items.
     *
     * @param newList The new list of summary strings (typically a single item).
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
     * ViewHolder for conversation summary items.
     */
    inner class ViewHolder(
        private val binding: CometchatAiConversationSummaryRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds the summary text to the view and applies styling.
         *
         * @param summary The conversation summary text to display.
         */
        fun bindView(summary: String) {
            binding.tvMessage.text = summary
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
            itemView.setTag(R.string.cometchat_reply_lowercase, summary)
        }
    }
}
