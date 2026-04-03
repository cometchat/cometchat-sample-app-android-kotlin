package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.pollbubble

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView

/**
 * A custom view that displays voter avatars and count for poll options.
 *
 * Shows up to 3 voter avatars overlapping and a count of total votes.
 */
class ImageAndCountView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val adapter: ImageAndCountAdapter
    private val countView: TextView

    init {
        Utils.initMaterialCard(this)
        val view = View.inflate(context, R.layout.cometchat_image_and_count_layout, null)
        val recyclerView: RecyclerView = view.findViewById(R.id.image_res_view)
        adapter = ImageAndCountAdapter(context)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true)
        recyclerView.adapter = adapter
        countView = view.findViewById(R.id.count_view)
        addView(view)
    }

    /**
     * Sets the voter data and count to display.
     *
     * @param list List of voter information (up to 3 shown)
     * @param count Total vote count for this option
     */
    fun setData(list: List<ImageTextPoJo>, count: Int) {
        adapter.setList(list)
        countView.text = count.toString()
    }

    /**
     * Sets the style for voter avatars.
     */
    fun setAvatarStyle(@StyleRes style: Int) {
        adapter.setAvatarStyle(style)
    }

    /**
     * Sets the text appearance for the count display.
     */
    fun setCountTextAppearance(@StyleRes style: Int) {
        if (style != 0) {
            countView.setTextAppearance(style)
        }
    }

    /**
     * Sets the text color for the count display.
     */
    fun setCountTextColor(@ColorInt color: Int) {
        countView.setTextColor(color)
    }
}
