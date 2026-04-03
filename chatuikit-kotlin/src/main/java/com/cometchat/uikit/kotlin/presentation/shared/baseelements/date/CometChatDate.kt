package com.cometchat.uikit.kotlin.presentation.shared.baseelements.date

import android.content.Context
import android.graphics.Color
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatDateBinding
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.kotlin.shared.resources.localise.CometChatLocalize
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * CometChatDate is a custom view that displays formatted timestamps.
 * 
 * This component supports:
 * - Multiple date patterns (TIME, DAY_DATE, DAY_DATE_TIME)
 * - Relative time display (Today, Yesterday)
 * - Custom date formatting
 * - Full styling customization
 * 
 * Usage in XML:
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDate
 *     android:id="@+id/date"
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content" />
 * ```
 */
@Suppress("unused")
class CometChatDate @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatDateStyle
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatDate::class.java.simpleName
    }

    private lateinit var binding: CometchatDateBinding

    // Date patterns
    private var datePatternString = "dd MMM yyyy"
    private var dayPatternString = "EEE"
    private var timePatternString = "h:mm a"

    // State
    private var dateText: String = ""
    private var timestamp: Long = 0
    private var pattern: DatePattern? = null
    private var customPattern: ((Long) -> String)? = null

    // Formatters
    private lateinit var simpleDateFormat: SimpleDateFormat
    private lateinit var simpleDayFormat: SimpleDateFormat
    private lateinit var simpleTimeFormat: SimpleDateFormat

    // Single style object - NO individual style properties
    private var style: CometChatDateStyle = CometChatDateStyle()

    // Custom formatter callback
    private var dateTimeFormatterCallback: DateTimeFormatterCallback? = null

    init {
        if (!isInEditMode) {
            inflateAndInitializeView(attrs, defStyleAttr)
        }
    }

    private fun inflateAndInitializeView(attrs: AttributeSet?, defStyleAttr: Int) {
        binding = CometchatDateBinding.inflate(LayoutInflater.from(context), this, true)
        setDefaultValues()
        applyStyleAttributes(attrs, defStyleAttr)
    }

    private fun setDefaultValues() {
        val locale = CometChatLocalize.getDefault()
        simpleDateFormat = SimpleDateFormat(datePatternString, locale)
        simpleDayFormat = SimpleDateFormat(dayPatternString, locale)
        simpleTimeFormat = SimpleDateFormat(timePatternString, locale)
        setTransparentBackground(true)
    }

    /**
     * Applies style attributes from XML using the style class factory method.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatDate, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatDate_cometchatDateStyle, 0
        )
        typedArray.recycle()
        
        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatDate, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatDateStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies all style properties from the style object to views.
     */
    private fun applyStyle() {
        if (style.strokeWidth != 0) applyStrokeWidth(style.strokeWidth)
        if (style.cornerRadius != 0) applyCornerRadius(style.cornerRadius)
        if (style.backgroundColor != 0) applyBackgroundColor(style.backgroundColor)
        if (style.textAppearance != 0) applyTextAppearance(style.textAppearance)
        // Always apply text color - use theme default if style value is 0
        applyTextColor(if (style.textColor != 0) style.textColor else CometChatTheme.getTextColorSecondary(context))
        if (style.strokeColor != 0) applyStrokeColor(style.strokeColor)
    }

    // ========================================
    // Private Apply Methods
    // ========================================

    private fun applyTextAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) {
            binding.tvDate.setTextAppearance(appearance)
        }
    }

    private fun applyTextColor(@ColorInt color: Int) {
        binding.tvDate.setTextColor(color)
    }

    private fun applyStrokeColor(@ColorInt color: Int) {
        binding.layoutDate.strokeColor = color
    }

    private fun applyStrokeWidth(@Dimension width: Int) {
        binding.layoutDate.strokeWidth = width
    }

    private fun applyCornerRadius(@Dimension radius: Int) {
        binding.layoutDate.radius = radius.toFloat()
    }

    private fun applyBackgroundColor(@ColorInt color: Int) {
        binding.layoutDate.setCardBackgroundColor(color)
    }

    /**
     * Configures whether the background should be transparent.
     */
    fun setTransparentBackground(isTransparent: Boolean) {
        if (isTransparent) {
            binding.layoutDate.strokeColor = Color.TRANSPARENT
            binding.layoutDate.strokeWidth = 0
            binding.layoutDate.cardElevation = 0f
            binding.layoutDate.setPadding(0, 0, 0, 0)
        } else {
            binding.layoutDate.setPadding(6, 6, 6, 6)
        }
    }

    // ========================================
    // Public Style Methods
    // ========================================

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(styleRes, R.styleable.CometChatDate)
            // fromTypedArray handles recycling internally
            setStyle(CometChatDateStyle.fromTypedArray(context, typedArray))
        }
    }

    /**
     * Sets the style from a CometChatDateStyle object.
     */
    fun setStyle(style: CometChatDateStyle) {
        this.style = style
        applyStyle()
    }

    // ========================================
    // Getters (read from style object)
    // ========================================

    fun getDateTextAppearance(): Int = style.textAppearance

    fun getStrokeColor(): Int = style.strokeColor

    fun getDateTextColor(): Int = style.textColor

    fun getDateStrokeWidth(): Int = style.strokeWidth

    fun getDateCornerRadius(): Int = style.cornerRadius

    fun getDateBackgroundColor(): Int = style.backgroundColor

    // ========================================
    // Setters (update style object + apply)
    // ========================================

    fun getDateText(): String = dateText

    fun setDateText(text: String) {
        dateText = text
        binding.tvDate.text = text
    }

    fun setDateTextAppearance(@StyleRes textAppearance: Int) {
        style = style.copy(textAppearance = textAppearance)
        applyTextAppearance(textAppearance)
    }

    fun setStrokeColor(@ColorInt color: Int) {
        style = style.copy(strokeColor = color)
        applyStrokeColor(color)
    }

    fun setDateTextColor(@ColorInt color: Int) {
        style = style.copy(textColor = color)
        applyTextColor(color)
    }

    fun setDateStrokeWidth(@Dimension width: Int) {
        style = style.copy(strokeWidth = width)
        applyStrokeWidth(width)
    }

    fun setDateCornerRadius(@Dimension radius: Int) {
        style = style.copy(cornerRadius = radius)
        applyCornerRadius(radius)
    }

    fun setDateBackgroundColor(@ColorInt color: Int) {
        style = style.copy(backgroundColor = color)
        applyBackgroundColor(color)
    }

    fun setDateTextAlignment(alignment: Int) {
        binding.tvDate.textAlignment = alignment
    }

    /**
     * Sets the date using a timestamp and format string.
     */
    fun setDate(timestamp: Long, format: String) {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = timestamp * 1000L
        val date = DateFormat.format(format, cal).toString()
        binding.tvDate.text = date
    }

    /**
     * Sets the timestamp.
     */
    fun setTimestamp(timestamp: Long) {
        if (timestamp != 0L) {
            this.timestamp = timestamp
            pattern?.let { setDate(timestamp, it) }
        }
    }

    /**
     * Sets the date using a timestamp and DatePattern.
     */
    fun setDate(timestamp: Long, pattern: DatePattern) {
        binding.tvDate.visibility = View.VISIBLE
        val customText = getCustomPattern(timestamp)
        if (customText != null) {
            binding.tvDate.text = customText
        } else if (timestamp != 0L) {
            binding.tvDate.text = when (pattern) {
                DatePattern.TIME -> getTime(timestamp)
                DatePattern.DAY_DATE -> getDayDate(timestamp)
                DatePattern.DAY_DATE_TIME -> getDayDateTime(timestamp)
            }
        }
    }

    /**
     * Sets the date using a timestamp with default DAY_DATE_TIME pattern.
     */
    fun setDate(timestamp: Long) {
        setDate(timestamp, DatePattern.DAY_DATE_TIME)
    }

    /**
     * Sets the date using a timestamp and custom formatter function.
     */
    fun setDate(timestamp: Long, formatter: (Long) -> String) {
        binding.tvDate.visibility = View.VISIBLE
        binding.tvDate.text = formatter(timestamp)
    }

    /**
     * Sets the date from a Date object.
     */
    fun setDate(date: Date) {
        setTimestamp(date.time / 1000)
    }

    private fun getCustomPattern(timestamp: Long): String? {
        return customPattern?.invoke(timestamp)
    }

    private fun getTime(timestamp: Long): String {
        val timeInMillis = timestamp * 1000
        if (isDateTimeFormatterCallbackSet()) {
            dateTimeFormatterCallback?.time(timeInMillis)?.let { return it }
        }
        return simpleTimeFormat.format(Date(timeInMillis))
    }

    private fun getDayDate(timestamp: Long): String {
        val now = Calendar.getInstance()
        val timeInMillis = timestamp * 1000
        val timeToCheck = Calendar.getInstance(Locale.ENGLISH).apply {
            this.timeInMillis = timeInMillis
        }

        return when {
            now.get(Calendar.DAY_OF_YEAR) == timeToCheck.get(Calendar.DAY_OF_YEAR) -> {
                if (isDateTimeFormatterCallbackSet()) {
                    dateTimeFormatterCallback?.today(timeInMillis)?.let { return it }
                }
                context.getString(R.string.cometchat_today)
            }
            (now.get(Calendar.DAY_OF_YEAR) - 1) == timeToCheck.get(Calendar.DAY_OF_YEAR) -> {
                if (isDateTimeFormatterCallbackSet()) {
                    dateTimeFormatterCallback?.yesterday(timeInMillis)?.let { return it }
                }
                context.getString(R.string.cometchat_yesterday)
            }
            else -> {
                if (isDateTimeFormatterCallbackSet()) {
                    dateTimeFormatterCallback?.otherDays(timeInMillis)?.let { return it }
                }
                simpleDateFormat.format(Date(timeInMillis))
            }
        }
    }

    private fun getDayDateTime(timestamp: Long): String {
        val timeInMillis = timestamp * 1000
        val lastMessageDate = simpleDateFormat.format(Date(timeInMillis))
        val lastMessageWeek = simpleDayFormat.format(Date(timeInMillis))
        val now = Calendar.getInstance()
        val timeToCheck = Calendar.getInstance(Locale.ENGLISH).apply {
            this.timeInMillis = timeInMillis
        }

        return when {
            // Today - show time
            now.get(Calendar.DAY_OF_YEAR) == timeToCheck.get(Calendar.DAY_OF_YEAR) -> {
                getTime(timestamp)
            }
            // Yesterday
            (now.get(Calendar.DAY_OF_YEAR) - 1) == timeToCheck.get(Calendar.DAY_OF_YEAR) -> {
                if (isDateTimeFormatterCallbackSet()) {
                    dateTimeFormatterCallback?.yesterday(timeInMillis)?.let { return it }
                }
                context.getString(R.string.cometchat_yesterday)
            }
            // Last 7 days - show day name
            (now.get(Calendar.DAY_OF_YEAR) - 7) <= timeToCheck.get(Calendar.DAY_OF_YEAR) -> {
                if (isDateTimeFormatterCallbackSet()) {
                    dateTimeFormatterCallback?.lastWeek(timeInMillis)?.let { return it }
                }
                lastMessageWeek
            }
            // Older - show full date
            else -> {
                if (isDateTimeFormatterCallbackSet()) {
                    dateTimeFormatterCallback?.otherDays(timeInMillis)?.let { return it }
                }
                lastMessageDate
            }
        }
    }

    private fun isDateTimeFormatterCallbackSet(): Boolean {
        return dateTimeFormatterCallback != null
    }

    /**
     * Sets the pattern for displaying the date.
     */
    fun setPattern(pattern: DatePattern) {
        this.pattern = pattern
        setDate(timestamp, pattern)
    }

    /**
     * Sets a custom date string.
     */
    fun setCustomDateString(string: String) {
        if (string.isNotEmpty()) {
            binding.tvDate.text = string
        }
    }

    /**
     * Sets a custom pattern for the date.
     */
    private fun setCustomPattern(customPattern: ((Long) -> String)?) {
        if (customPattern != null) {
            this.customPattern = customPattern
            pattern?.let { setDate(timestamp, it) }
        }
    }

    /**
     * Sets a custom date format function.
     */
    fun setCustomDateFormat(formatter: (Long) -> String) {
        customPattern = formatter
        pattern?.let { setDate(timestamp, it) }
    }

    // Format getters and setters (matching Java implementation)

    fun getTimeFormat(): SimpleDateFormat = simpleTimeFormat

    fun setTimeFormat(timeFormat: SimpleDateFormat?) {
        if (timeFormat != null) {
            this.simpleTimeFormat = timeFormat
        }
    }

    fun getDateFormat(): SimpleDateFormat = simpleDateFormat

    fun setDateFormat(dateFormat: SimpleDateFormat?) {
        if (dateFormat != null) {
            this.simpleDateFormat = dateFormat
        }
    }

    fun getDayFormat(): SimpleDateFormat = simpleDayFormat

    fun setDayFormat(dayFormat: SimpleDateFormat?) {
        if (dayFormat != null) {
            this.simpleDayFormat = dayFormat
        }
    }

    fun getDatePattern(): String = datePatternString

    fun setDatePattern(pattern: String?) {
        if (!pattern.isNullOrEmpty()) {
            this.datePatternString = pattern
            this.simpleDateFormat = SimpleDateFormat(pattern, CometChatLocalize.getDefault())
        }
    }

    fun getDayPattern(): String = dayPatternString

    fun setDayPattern(pattern: String?) {
        if (!pattern.isNullOrEmpty()) {
            this.dayPatternString = pattern
            this.simpleDayFormat = SimpleDateFormat(pattern, CometChatLocalize.getDefault())
        }
    }

    fun getTimePattern(): String = timePatternString

    fun setTimePattern(pattern: String?) {
        if (!pattern.isNullOrEmpty()) {
            this.timePatternString = pattern
            this.simpleTimeFormat = SimpleDateFormat(pattern, CometChatLocalize.getDefault())
        }
    }

    /**
     * Sets padding/margins for the date text view.
     * This overrides the default View.setPadding to apply margins to the internal TextView
     * instead of padding on the outer container, matching the Java implementation behavior.
     */
    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        if (!::binding.isInitialized) {
            super.setPadding(left, top, right, bottom)
            return
        }
        val layoutParams = binding.tvDate.layoutParams
        if (layoutParams is MarginLayoutParams) {
            layoutParams.setMargins(
                if (left > -1) left else 0,
                if (top > -1) top else 0,
                if (right > -1) right else 0,
                if (bottom > -1) bottom else 0
            )
            binding.tvDate.layoutParams = layoutParams
        }
    }

    /**
     * Sets padding/margins for the date text view.
     * @deprecated Use setPadding instead for consistency with Java implementation.
     */
    @Deprecated("Use setPadding instead", ReplaceWith("setPadding(left, top, right, bottom)"))
    fun setDatePadding(left: Int, top: Int, right: Int, bottom: Int) {
        setPadding(left, top, right, bottom)
    }

    fun setDateTimeFormatterCallback(callback: DateTimeFormatterCallback?) {
        if (callback != null) {
            dateTimeFormatterCallback = callback
        }
    }
}
