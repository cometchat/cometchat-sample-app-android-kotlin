package com.cometchat.pro.uikit.ui_components.shared.cometchatBadgeCount

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.shared.cometchatAvatar.CometChatAvatar

/**
 * Purpose - This class is a subclass of LinearLayout, It is a component which is been used by developer
 * to display the Badgecount for unread message count or unread conversations. It can also
 * be used for other purpose. This class contains various methods which
 * are used to update the count, set count background color, set count color, set count size
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 16th January 2020
 *
 */
@BindingMethods(value = [BindingMethod(type = CometChatAvatar::class, attribute = "app:count", method = "setCount")])
class CometChatBadgeCount : LinearLayout {
    private var tvCount: TextView? = null //Used to display count
    private var count: Int? = null//Used to store value of count = 0
    private var countSize: Float? = null //Used to store size of count = 0f
    private var countColor: Int?= null //Used to store color of count = 0
    private var countBackgroundColor: Int? = null //Used to store background color of count. = 0

    constructor(context: Context) : super(context) {
        initViewComponent(context, null, -1)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initViewComponent(context, attrs, -1)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initViewComponent(context, attrs, defStyleAttr)
    }

    /**
     * This method is used to initialize the view present in component and set the value if it is
     * available.
     *
     * @param context
     * @param attributeSet
     * @param defStyleAttr
     */
    private fun initViewComponent(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) {
        val view = View.inflate(context, R.layout.cometchat_badge_count, null)
        val a = getContext().theme.obtainStyledAttributes(
                attributeSet,
                R.styleable.BadgeCount,
                0, 0)
        count = a.getInt(R.styleable.BadgeCount_count, 0)
        countSize = a.getDimension(R.styleable.BadgeCount_count_size, 12f)
        countColor = a.getColor(R.styleable.BadgeCount_count_color, Color.WHITE)
        countBackgroundColor = a.getColor(R.styleable.BadgeCount_count_background_color, resources.getColor(R.color.colorPrimary))
        addView(view)
        visibility = if (count == 0) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
        tvCount = view.findViewById(R.id.tvSetCount)
        tvCount!!.background = resources.getDrawable(R.drawable.count_background)
        tvCount!!.textSize = countSize!!
        tvCount!!.setTextColor(countColor!!)
        tvCount!!.text = count.toString()
        setCountBackground(countBackgroundColor!!)
    }

    /**
     * This method is used to set background color of count.
     * @param color is an object of Color.class . It is used as color for background.
     */
    fun setCountBackground(@ColorInt color: Int) {
        val unwrappedDrawable = tvCount!!.background
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable)
        DrawableCompat.setTint(wrappedDrawable, color)
    }

    /**
     * This method is used to set color of count i.e integer.
     * @param color is an object of Color.class. It is used as color of text in tvCount (TextView)
     */
    fun setCountColor(@ColorInt color: Int) {
        tvCount!!.setTextColor(color)
    }

    /**
     * This method is used to set size of text present in tvCount(TextView) i.e count size;
     * @param size
     */
    fun setCountSize(size: Float) {
        tvCount!!.textSize = size
    }

    /**
     * This method is used to set count in tvCount(TextView). If count is 0 then tvCount is invisible
     * If count is more than 1, then it will display the value. The limit of the count shown will be 999.
     *
     * @param count is an Integer which is set in tvCount(TextView).
     */
    fun setCount(count: Int) {
        this.count = count
        visibility = if (count == 0) View.GONE else View.VISIBLE
        if (count < 999) tvCount!!.text = count.toString() else tvCount!!.text = "999+"
    }
}