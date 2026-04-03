package com.cometchat.uikit.compose.presentation.shared.baseelements.date

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.compose.theme.CometChatTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * CometChatDate is a composable that displays formatted date/time information.
 * It provides smart date formatting with support for relative dates like "Today" and "Yesterday".
 *
 * Features:
 * - Multiple date display patterns (TIME, DAY_DATE, DAY_DATE_TIME)
 * - Smart relative date formatting ("Today", "Yesterday", weekday names)
 * - Customizable date/time/day format patterns
 * - Custom date formatter callbacks for localization
 * - Fully customizable styling through DateStyle
 * - Optional transparent background mode
 * - Custom date string override
 *
 * @param modifier Modifier for the date container
 * @param timestamp The timestamp in seconds to display. If 0, no date is shown unless customDateString is provided
 * @param pattern The display pattern to use (TIME, DAY_DATE, or DAY_DATE_TIME)
 * @param datePattern SimpleDateFormat pattern for full dates (default: "dd MMM yyyy")
 * @param dayPattern SimpleDateFormat pattern for day names (default: "EEE")
 * @param timePattern SimpleDateFormat pattern for time (default: "h:mm a")
 * @param customDateString Optional custom string to display instead of formatted timestamp
 * @param transparentBackground If true, background will be transparent with no border/padding
 * @param style Styling configuration for the date. Use DateStyle.default() for theme-based defaults
 * @param dateTimeFormatterCallback Optional callback for custom date/time formatting and localization
 *
 * @sample
 * ```
 * // Time only display with default styling
 * CometChatDate(
 *     timestamp = 1702345678,
 *     pattern = Pattern.TIME,
 *     style = DateStyle.default()
 * )
 *
 * // Smart day/date display with custom styling
 * CometChatDate(
 *     timestamp = 1702345678,
 *     pattern = Pattern.DAY_DATE,
 *     style = DateStyle.default(
 *         backgroundColor = Color.LightGray,
 *         cornerRadius = 8.dp,
 *         textColor = Color.Black
 *     )
 * )
 *
 * // Custom date string
 * CometChatDate(
 *     customDateString = "Last seen recently",
 *     style = DateStyle.default(textColor = Color.Gray)
 * )
 *
 * // With custom formatting callback
 * CometChatDate(
 *     timestamp = 1702345678,
 *     pattern = Pattern.DAY_DATE_TIME,
 *     style = DateStyle.default(),
 *     dateTimeFormatterCallback = object : DateTimeFormatterCallback {
 *         override fun today(timestamp: Long) = "Hoy"
 *         override fun yesterday(timestamp: Long) = "Ayer"
 *     }
 * )
 *
 * // Using Date object
 * CometChatDate(
 *     date = Date(),
 *     pattern = Pattern.TIME,
 *     style = DateStyle.default()
 * )
 *
 * // Custom text alignment
 * CometChatDate(
 *     timestamp = 1702345678,
 *     pattern = Pattern.TIME,
 *     style = DateStyle.default(textAlign = TextAlign.Start)
 * )
 * ```
 */
@Composable
fun CometChatDate(
    modifier: Modifier = Modifier,
    timestamp: Long = 0,
    pattern: Pattern? = null,
    datePattern: String = "dd MMM yyyy",
    dayPattern: String = "EEE",
    timePattern: String = "h:mm a",
    customDateString: String? = null,
    transparentBackground: Boolean = true,
    style: DateStyle = DateStyle.default(),
    dateTimeFormatterCallback: DateTimeFormatterCallback? = null
) {
    val context = LocalContext.current
    val typography = style.typography ?: CometChatTheme.typography
    
    // Create SimpleDateFormat instances with remembered locale
    val locale = remember { Locale.getDefault() }
    val simpleDateFormat = remember(datePattern, locale) {
        SimpleDateFormat(datePattern, locale)
    }
    val simpleDayFormat = remember(dayPattern, locale) {
        SimpleDateFormat(dayPattern, locale)
    }
    val simpleTimeFormat = remember(timePattern, locale) {
        SimpleDateFormat(timePattern, locale)
    }
    
    // Use style values directly - defaults are now in DateStyle.default()
    val bgColor = if (transparentBackground) {
        Color.Transparent
    } else {
        style.backgroundColor ?: Color.Transparent
    }
    val txtColor = style.textColor ?: CometChatTheme.colorScheme.textColorSecondary
    val txtStyle = style.textStyle ?: typography.caption1Regular
    
    // Calculate the date text to display
    val dateText = remember(timestamp, pattern, customDateString, datePattern, dayPattern, timePattern) {
        when {
            customDateString != null && customDateString.isNotEmpty() -> customDateString
            timestamp != 0L && pattern != null -> {
                when (pattern) {
                    Pattern.TIME -> getTime(
                        timestamp, 
                        simpleTimeFormat, 
                        dateTimeFormatterCallback
                    )
                    Pattern.DAY_DATE -> getDayDate(
                        timestamp, 
                        simpleDateFormat, 
                        dateTimeFormatterCallback,
                        context
                    )
                    Pattern.DAY_DATE_TIME -> getDayDateTime(
                        timestamp, 
                        simpleDateFormat, 
                        simpleDayFormat, 
                        simpleTimeFormat, 
                        dateTimeFormatterCallback,
                        context
                    )
                }
            }
            else -> ""
        }
    }
    
    // Determine shape
    val shape = RoundedCornerShape(style.cornerRadius)
    
    // Determine padding based on transparent background
    val contentPadding = if (transparentBackground) 0.dp else 6.dp
    
    if (dateText.isNotEmpty()) {
        Box(
            modifier = modifier
                .clip(shape)
                .then(
                    if (!transparentBackground && style.borderWidth > 0.dp && style.borderColor != null) {
                        Modifier.border(style.borderWidth, style.borderColor, shape)
                    } else {
                        Modifier
                    }
                )
                .background(bgColor)
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dateText,
                style = txtStyle,
                color = txtColor,
                textAlign = style.textAlign
            )
        }
    }
}

/**
 * Formats the given timestamp to a string representing the time.
 *
 * @param timestamp The timestamp to format, in seconds
 * @param timeFormat The SimpleDateFormat to use for formatting
 * @param callback Optional callback for custom time formatting
 * @return The formatted time string
 */
private fun getTime(
    timestamp: Long,
    timeFormat: SimpleDateFormat,
    callback: DateTimeFormatterCallback?
): String {
    val timeInMillis = timestamp * 1000
    
    // Check if callback provides custom formatting
    callback?.time(timeInMillis)?.let { return it }
    
    return timeFormat.format(Date(timeInMillis))
}

/**
 * Formats the given timestamp to a string representing the day or date.
 * Returns "Today" if the date is the current day, "Yesterday" if the date is
 * one day before the current day, or the full date otherwise.
 *
 * @param timestamp The timestamp to format, in seconds
 * @param dateFormat The SimpleDateFormat to use for formatting
 * @param callback Optional callback for custom date formatting
 * @param context Android context for accessing string resources
 * @return A string indicating if the date is today, yesterday, or the formatted date
 */
private fun getDayDate(
    timestamp: Long,
    dateFormat: SimpleDateFormat,
    callback: DateTimeFormatterCallback?,
    context: android.content.Context
): String {
    val now = Calendar.getInstance()
    val timeStampInMillis = timestamp * 1000
    val timeToCheck = Calendar.getInstance(Locale.getDefault()).apply {
        timeInMillis = timeStampInMillis
    }
    
    return when {
        now.get(Calendar.DAY_OF_YEAR) == timeToCheck.get(Calendar.DAY_OF_YEAR) &&
        now.get(Calendar.YEAR) == timeToCheck.get(Calendar.YEAR) -> {
            callback?.today(timeStampInMillis) ?: "Today"
        }
        (now.get(Calendar.DAY_OF_YEAR) - 1) == timeToCheck.get(Calendar.DAY_OF_YEAR) &&
        now.get(Calendar.YEAR) == timeToCheck.get(Calendar.YEAR) -> {
            callback?.yesterday(timeStampInMillis) ?: "Yesterday"
        }
        else -> {
            callback?.otherDays(timeStampInMillis) ?: dateFormat.format(Date(timeStampInMillis))
        }
    }
}

/**
 * Formats the given timestamp to a string representing the date and time.
 * Returns the time if the date is today, "Yesterday" if the date is one day
 * before the current day, the weekday if the date is within the last 7 days,
 * or the full date otherwise.
 *
 * @param timestamp The timestamp to format, in seconds
 * @param dateFormat The SimpleDateFormat to use for date formatting
 * @param dayFormat The SimpleDateFormat to use for day name formatting
 * @param timeFormat The SimpleDateFormat to use for time formatting
 * @param callback Optional callback for custom date/time formatting
 * @param context Android context for accessing string resources
 * @return A string representing the date and/or time
 */
private fun getDayDateTime(
    timestamp: Long,
    dateFormat: SimpleDateFormat,
    dayFormat: SimpleDateFormat,
    timeFormat: SimpleDateFormat,
    callback: DateTimeFormatterCallback?,
    context: android.content.Context
): String {
    val timeInMillis = timestamp * 1000
    val now = Calendar.getInstance()
    val timeToCheck = Calendar.getInstance(Locale.getDefault()).apply {
        this.timeInMillis = timeInMillis
    }
    
    return when {
        // Today - show time
        now.get(Calendar.DAY_OF_YEAR) == timeToCheck.get(Calendar.DAY_OF_YEAR) &&
        now.get(Calendar.YEAR) == timeToCheck.get(Calendar.YEAR) -> {
            getTime(timestamp, timeFormat, callback)
        }
        // Yesterday
        (now.get(Calendar.DAY_OF_YEAR) - 1) == timeToCheck.get(Calendar.DAY_OF_YEAR) &&
        now.get(Calendar.YEAR) == timeToCheck.get(Calendar.YEAR) -> {
            callback?.yesterday(timeInMillis) ?: "Yesterday"
        }
        // Last 7 days - show day name
        (now.get(Calendar.DAY_OF_YEAR) - 7) <= timeToCheck.get(Calendar.DAY_OF_YEAR) &&
        now.get(Calendar.YEAR) == timeToCheck.get(Calendar.YEAR) -> {
            callback?.lastWeek(timeInMillis) ?: dayFormat.format(Date(timeInMillis))
        }
        // Older - show full date
        else -> {
            callback?.otherDays(timeInMillis) ?: dateFormat.format(Date(timeInMillis))
        }
    }
}

/**
 * CometChatDate overload that accepts a Date object instead of a timestamp.
 * Converts the Date to a timestamp in seconds and delegates to the main composable.
 *
 * @param modifier Modifier for the date container
 * @param date The Date object to display
 * @param pattern The display pattern to use (TIME, DAY_DATE, or DAY_DATE_TIME)
 * @param datePattern SimpleDateFormat pattern for full dates (default: "dd MMM yyyy")
 * @param dayPattern SimpleDateFormat pattern for day names (default: "EEE")
 * @param timePattern SimpleDateFormat pattern for time (default: "h:mm a")
 * @param transparentBackground If true, background will be transparent with no border/padding
 * @param style Styling configuration for the date
 * @param dateTimeFormatterCallback Optional callback for custom date/time formatting
 *
 * @sample
 * ```
 * CometChatDate(
 *     date = Date(),
 *     pattern = Pattern.TIME,
 *     style = DateStyle.default()
 * )
 * ```
 */
@Composable
fun CometChatDate(
    modifier: Modifier = Modifier,
    date: Date,
    pattern: Pattern? = null,
    datePattern: String = "dd MMM yyyy",
    dayPattern: String = "EEE",
    timePattern: String = "h:mm a",
    transparentBackground: Boolean = true,
    style: DateStyle = DateStyle.default(),
    dateTimeFormatterCallback: DateTimeFormatterCallback? = null
) {
    CometChatDate(
        modifier = modifier,
        timestamp = date.time / 1000,
        pattern = pattern,
        datePattern = datePattern,
        dayPattern = dayPattern,
        timePattern = timePattern,
        customDateString = null,
        transparentBackground = transparentBackground,
        style = style,
        dateTimeFormatterCallback = dateTimeFormatterCallback
    )
}
