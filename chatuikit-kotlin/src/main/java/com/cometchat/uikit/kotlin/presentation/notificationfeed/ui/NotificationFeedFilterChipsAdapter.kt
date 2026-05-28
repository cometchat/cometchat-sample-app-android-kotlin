package com.cometchat.uikit.kotlin.presentation.notificationfeed.ui

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.uikit.core.state.FilterChipState
import com.cometchat.uikit.kotlin.presentation.notificationfeed.style.CometChatNotificationFeedStyle
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * RecyclerView adapter for the horizontal filter chips row.
 * Displays "All" chip first, followed by server-provided categories.
 * Shows unread badge count inside the chip when count > 0.
 * Uses CometChatTheme for all colors — fully theme-aware.
 */
class NotificationFeedFilterChipsAdapter(
    private var style: CometChatNotificationFeedStyle,
    private val onChipClick: (String) -> Unit
) : RecyclerView.Adapter<NotificationFeedFilterChipsAdapter.ChipViewHolder>() {

    private val chips = mutableListOf<FilterChipState>()

    fun submitList(newChips: List<FilterChipState>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = chips.size
            override fun getNewListSize() = newChips.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                chips[oldPos].id == newChips[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                chips[oldPos] == newChips[newPos]
        })
        chips.clear()
        chips.addAll(newChips)
        diffResult.dispatchUpdatesTo(this)
    }

    fun updateStyle(newStyle: CometChatNotificationFeedStyle) {
        this.style = newStyle
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val ctx = parent.context
        val density = ctx.resources.displayMetrics.density

        // Chip row: [label] [badge]
        val chipRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                (34 * density).toInt()
            ).apply {
                marginEnd = (8 * density).toInt()
            }
            gravity = Gravity.CENTER_VERTICAL
            setPadding((12 * density).toInt(), (6 * density).toInt(), (12 * density).toInt(), (6 * density).toInt())
        }

        val chipText = TextView(ctx).apply {
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        chipRow.addView(chipText)

        // Badge (inside chip, to the right of label)
        val badge = TextView(ctx).apply {
            textSize = 10f
            gravity = Gravity.CENTER
            setPadding((6 * density).toInt(), (2 * density).toInt(), (6 * density).toInt(), (2 * density).toInt())
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = (6 * density).toInt()
            }
        }
        chipRow.addView(badge)

        return ChipViewHolder(chipRow, chipText, badge)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        holder.bind(chips[position])
    }

    override fun getItemCount(): Int = chips.size

    inner class ChipViewHolder(
        itemView: View,
        private val chipText: TextView,
        private val badge: TextView
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(chip: FilterChipState) {
            val ctx = itemView.context
            val density = ctx.resources.displayMetrics.density

            chipText.text = chip.label

            // Colors from theme
            val primaryColor = CometChatTheme.getPrimaryColor(ctx)
            val backgroundColor1 = CometChatTheme.getBackgroundColor1(ctx)
            val strokeDefault = CometChatTheme.getStrokeColorDefault(ctx)
            val textWhite = CometChatTheme.getTextColorWhite(ctx)
            val textTertiary = CometChatTheme.getTextColorTertiary(ctx)

            // Chip background
            val bgDrawable = GradientDrawable().apply {
                cornerRadius = 20 * density
                if (chip.isActive) {
                    setColor(primaryColor)
                    setStroke(0, 0)
                } else {
                    setColor(backgroundColor1)
                    setStroke((1 * density).toInt(), strokeDefault)
                }
            }
            itemView.background = bgDrawable
            chipText.setTextColor(
                when {
                    chip.isActive -> textWhite
                    chip.unreadCount > 0 -> CometChatTheme.getTextColorSecondary(ctx)  // Has unreads: medium-dark
                    else -> textTertiary  // No unreads: light gray
                }
            )
            chipText.setTypeface(null, if (chip.isActive) Typeface.BOLD else Typeface.NORMAL)

            // Badge
            if (chip.unreadCount > 0) {
                badge.visibility = View.VISIBLE
                badge.text = if (chip.unreadCount > 99) "99+" else chip.unreadCount.toString()

                val badgeBgDrawable = GradientDrawable().apply {
                    cornerRadius = 10 * density
                    if (chip.isActive) {
                        // Active chip badge: ep50 bg, ep200 border, textHighlight text
                        setColor(CometChatTheme.getExtendedPrimaryColor50(ctx))
                        setStroke((1 * density).toInt(), CometChatTheme.getExtendedPrimaryColor200(ctx))
                    } else {
                        // Inactive chip badge: neutral600 bg, white text
                        setColor(CometChatTheme.getNeutralColor600(ctx))
                    }
                }
                badge.background = badgeBgDrawable
                badge.setTextColor(
                    if (chip.isActive) CometChatTheme.getTextColorHighlight(ctx)
                    else textWhite
                )
            } else {
                badge.visibility = View.GONE
            }

            // Click
            itemView.setOnClickListener {
                onChipClick(chip.id)
            }
        }
    }
}
