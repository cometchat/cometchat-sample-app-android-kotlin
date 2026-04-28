package com.cometchat.uikit.kotlin.presentation.search.ui.viewholders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.uikit.kotlin.databinding.CometchatSearchStickyHeaderBinding
import com.cometchat.uikit.kotlin.presentation.search.style.CometChatSearchStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDateStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.DatePattern
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback

/**
 * ViewHolder for date separator items in search message results.
 *
 * This ViewHolder is used for sticky date headers that group messages by date.
 * It displays a date pill showing the date of the messages below it.
 *
 * @param binding The ViewBinding for the date separator layout
 */
class DateItemHolder(
    val binding: CometchatSearchStickyHeaderBinding
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        /**
         * Creates a new DateItemHolder.
         *
         * @param parent The parent ViewGroup
         * @return A new DateItemHolder instance
         */
        fun create(parent: ViewGroup): DateItemHolder {
            val binding = CometchatSearchStickyHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return DateItemHolder(binding)
        }
    }

    /**
     * Binds the date to the view.
     *
     * @param timestamp The timestamp in seconds (CometChatDate.setDate() expects seconds)
     * @param style The style configuration
     * @param dateTimeFormatter Optional custom date/time formatter
     */
    fun bind(
        timestamp: Long,
        style: CometChatSearchStyle?,
        dateTimeFormatter: DateTimeFormatterCallback?
    ) {
        // Apply custom formatter BEFORE setDate (matching Java reference order)
        dateTimeFormatter?.let { formatter ->
            binding.txtMessageDate.setDateTimeFormatterCallback(formatter)
        }

        // Set the date
        binding.txtMessageDate.setDate(timestamp, DatePattern.DAY_DATE)

        // Apply style
        style?.let { applyStyle(it) }
    }

    /**
     * Binds the date using a pre-formatted string.
     *
     * @param formattedDate The pre-formatted date string
     * @param style The style configuration
     */
    fun bind(
        formattedDate: String,
        style: CometChatSearchStyle?
    ) {
        binding.txtMessageDate.setCustomDateString(formattedDate)

        // Apply style
        style?.let { applyStyle(it) }
    }

    /**
     * Applies style configuration to the date separator.
     */
    private fun applyStyle(style: CometChatSearchStyle) {
        // Apply date style if available
        style.dateSeparatorStyle?.let {
            binding.txtMessageDate.setStyle(it)
        }

        // Apply individual style properties
        style.dateSeparatorBackgroundColor?.let { color ->
            binding.txtMessageDate.setDateBackgroundColor(color)
        }

        style.dateSeparatorTextColor?.let { color ->
            binding.txtMessageDate.setDateTextColor(color)
        }

        style.dateSeparatorTextAppearance?.let { appearance ->
            binding.txtMessageDate.setDateTextAppearance(appearance)
        }
    }
}
