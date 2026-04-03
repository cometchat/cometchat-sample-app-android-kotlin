package com.cometchat.uikit.kotlin.presentation.reactionlist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.ReactionCount
import com.cometchat.uikit.kotlin.databinding.CometchatReactionHeaderItemBinding

/**
 * Adapter class for displaying reaction header tabs in a horizontal RecyclerView.
 * 
 * This adapter handles the display and interaction of reaction tabs, showing:
 * - "All" tab with total count as the first item
 * - Individual emoji tabs with their respective counts
 * - Active tab indicator (underline) for the selected tab
 * 
 * The adapter follows the pattern from the Java implementation in
 * chatuikit/src/main/java/com/cometchat/chatuikit/reactionlist/adapter/ReactionsHeaderAdapter.java
 */
class ReactionsHeaderAdapter : RecyclerView.Adapter<ReactionsHeaderAdapter.ReactionHeaderViewHolder>() {

    /**
     * Callback interface for tab selection events.
     */
    fun interface OnTabSelectedListener {
        /**
         * Called when a tab is selected.
         * 
         * @param index The index of the selected tab (0 = "All", 1+ = emoji tabs)
         * @param reaction The reaction string ("All" or emoji)
         */
        fun onTabSelected(index: Int, reaction: String)
    }

    // Data
    private val reactionCounts = mutableListOf<ReactionCount>()
    private var activeTab = 0

    // Callback
    private var onTabSelectedListener: OnTabSelectedListener? = null

    // Styling
    @ColorInt
    private var textColor: Int = 0

    @ColorInt
    private var textActiveColor: Int = 0

    @StyleRes
    private var textAppearance: Int = 0

    @ColorInt
    private var tabActiveIndicatorColor: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionHeaderViewHolder {
        val binding = CometchatReactionHeaderItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReactionHeaderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReactionHeaderViewHolder, position: Int) {
        if (position == RecyclerView.NO_POSITION || position >= reactionCounts.size) return
        
        val reactionCount = reactionCounts[position]
        
        // Build display text: "emoji count" (e.g., "All 8" or "👍 5")
        val displayText = "${reactionCount.reaction} ${reactionCount.count}"
        
        holder.binding.tvText.apply {
            text = displayText
            if (textAppearance != 0) {
                setTextAppearance(textAppearance)
            }
            setTextColor(if (position == activeTab) textActiveColor else textColor)
        }
        
        // Show/hide active tab indicator
        holder.binding.tabIndicator.apply {
            setBackgroundColor(tabActiveIndicatorColor)
            visibility = if (position == activeTab) View.VISIBLE else View.GONE
        }
        
        // Handle tab click
        holder.binding.layoutReactionTab.setOnClickListener {
            @Suppress("DEPRECATION")
            val clickedIndex = holder.adapterPosition
            if (clickedIndex != RecyclerView.NO_POSITION && clickedIndex != activeTab && clickedIndex < reactionCounts.size) {
                val previousActiveTab = activeTab
                activeTab = clickedIndex
                
                // Notify listener with the reaction string
                onTabSelectedListener?.onTabSelected(
                    clickedIndex,
                    reactionCounts[clickedIndex].reaction
                )
                
                // Update UI for previous and new active tabs
                notifyItemChanged(previousActiveTab)
                notifyItemChanged(activeTab)
            }
        }
    }

    override fun getItemCount(): Int = reactionCounts.size

    /**
     * Updates the list of reaction counts to display.
     * 
     * The list should include "All" as the first item with total count,
     * followed by individual emoji tabs.
     * 
     * @param counts The list of ReactionCount objects to display
     */
    fun updateReactionCounts(counts: List<ReactionCount>) {
        reactionCounts.clear()
        reactionCounts.addAll(counts)
        notifyDataSetChanged()
    }

    /**
     * Sets the active tab index.
     * 
     * @param index The index of the tab to set as active (0 = "All", 1+ = emoji tabs)
     */
    fun setActiveTab(index: Int) {
        if (index in 0 until reactionCounts.size && index != activeTab) {
            val previousActiveTab = activeTab
            activeTab = index
            notifyItemChanged(previousActiveTab)
            notifyItemChanged(activeTab)
        } else if (index in 0 until reactionCounts.size) {
            activeTab = index
        }
    }

    /**
     * Gets the current active tab index.
     * 
     * @return The index of the currently active tab
     */
    fun getActiveTab(): Int = activeTab

    /**
     * Sets the callback for tab selection events.
     * 
     * @param listener The listener to invoke when a tab is selected
     */
    fun setOnTabSelected(listener: OnTabSelectedListener?) {
        this.onTabSelectedListener = listener
    }

    /**
     * Sets the text color for inactive tabs.
     * 
     * @param color The color to use for inactive tab text
     */
    fun setTextColor(@ColorInt color: Int) {
        this.textColor = color
        notifyDataSetChanged()
    }

    /**
     * Gets the text color for inactive tabs.
     * 
     * @return The color used for inactive tab text
     */
    @ColorInt
    fun getTextColor(): Int = textColor

    /**
     * Sets the text color for the active tab.
     * 
     * @param color The color to use for active tab text
     */
    fun setTextActiveColor(@ColorInt color: Int) {
        this.textActiveColor = color
        notifyDataSetChanged()
    }

    /**
     * Gets the text color for the active tab.
     * 
     * @return The color used for active tab text
     */
    @ColorInt
    fun getTextActiveColor(): Int = textActiveColor

    /**
     * Sets the text appearance for tab labels.
     * 
     * @param appearance The text appearance resource ID
     */
    fun setTextAppearance(@StyleRes appearance: Int) {
        this.textAppearance = appearance
        notifyDataSetChanged()
    }

    /**
     * Gets the text appearance for tab labels.
     * 
     * @return The text appearance resource ID
     */
    @StyleRes
    fun getTextAppearance(): Int = textAppearance

    /**
     * Sets the color for the active tab indicator (underline).
     * 
     * @param color The color to use for the active tab indicator
     */
    fun setTabActiveIndicatorColor(@ColorInt color: Int) {
        this.tabActiveIndicatorColor = color
        notifyDataSetChanged()
    }

    /**
     * Gets the color for the active tab indicator.
     * 
     * @return The color used for the active tab indicator
     */
    @ColorInt
    fun getTabActiveIndicatorColor(): Int = tabActiveIndicatorColor

    /**
     * Gets the list of reaction counts.
     * 
     * @return The current list of ReactionCount objects
     */
    fun getReactionCounts(): List<ReactionCount> = reactionCounts.toList()

    /**
     * ViewHolder for reaction header tab items.
     * 
     * Uses ViewBinding for the cometchat_reaction_header_item layout.
     */
    class ReactionHeaderViewHolder(
        val binding: CometchatReactionHeaderItemBinding
    ) : RecyclerView.ViewHolder(binding.root)
}
