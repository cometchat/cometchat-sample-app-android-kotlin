package com.cometchat.uikit.kotlin.presentation.messagelist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.uikit.kotlin.R

/**
 * RecyclerView adapter for the message popup menu option list.
 * Follows the same pattern as the shared PopupMenuAdapter but is accessible
 * from the messagelist package.
 */
internal class MessagePopupMenuAdapter(
    private val context: Context,
    private val items: List<MenuItem>,
    private val onItemClick: (String, String) -> Unit
) : RecyclerView.Adapter<MessagePopupMenuAdapter.ViewHolder>() {

    @ColorInt private var textColor: Int = 0
    @StyleRes private var textAppearance: Int = 0
    @ColorInt private var startIconTint: Int = 0
    @ColorInt private var endIconTint: Int = 0

    fun setTextColor(@ColorInt color: Int) { this.textColor = color }
    fun setTextAppearance(@StyleRes appearance: Int) { this.textAppearance = appearance }
    fun setStartIconTint(@ColorInt tint: Int) { this.startIconTint = tint }
    fun setEndIconTint(@ColorInt tint: Int) { this.endIconTint = tint }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cometchat_popup_menu_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.menu_item)
        private val ivStartIcon: ImageView = itemView.findViewById(R.id.start_icon)
        private val ivEndIcon: ImageView = itemView.findViewById(R.id.end_icon)

        fun bind(item: MenuItem) {
            tvTitle.text = item.name

            // Text styling: item-level overrides adapter-level defaults
            val itemTextColor = if (item.textColor != 0) item.textColor else textColor
            if (itemTextColor != 0) tvTitle.setTextColor(itemTextColor)

            val itemTextAppearance = if (item.textAppearance != 0) item.textAppearance else textAppearance
            if (itemTextAppearance != 0) tvTitle.setTextAppearance(itemTextAppearance)

            // Start icon
            if (item.startIcon != null) {
                ivStartIcon.visibility = View.VISIBLE
                ivStartIcon.setImageDrawable(item.startIcon)
                val tint = if (item.startIconTint != 0) item.startIconTint else startIconTint
                if (tint != 0) ivStartIcon.setColorFilter(tint)
            } else {
                ivStartIcon.visibility = View.GONE
            }

            // End icon
            if (item.endIcon != null) {
                ivEndIcon.visibility = View.VISIBLE
                ivEndIcon.setImageDrawable(item.endIcon)
                val tint = if (item.endIconTint != 0) item.endIconTint else endIconTint
                if (tint != 0) ivEndIcon.setColorFilter(tint)
            } else {
                ivEndIcon.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onItemClick(item.id, item.name)
            }
        }
    }
}
