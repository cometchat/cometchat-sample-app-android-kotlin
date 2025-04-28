package com.cometchat.chatuikit.shared.views.date;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatDateBinding;
import com.cometchat.chatuikit.shared.interfaces.DateTimeFormatterCallback;
import com.cometchat.chatuikit.shared.interfaces.Function1;
import com.cometchat.chatuikit.shared.resources.localise.CometChatLocalize;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * CometChatDate is a custom LinearLayout that represents the date.
 *
 * <p>
 * It provides methods to customize the appearance of the date
 *
 * <p>
 * such as setting the text, text style, text color, stroke width, corner
 * radius, and background color. Created on: 10 September 2024 Modified on:
 */
@SuppressWarnings("unused")
public class CometChatDate extends LinearLayout {
    private static final String TAG = CometChatDate.class.getSimpleName();
    private String datePattern = "dd MMM yyyy";
    private String dayPattern = "EEE";
    private String timePattern = "h:mm a";
    private CometchatDateBinding binding;
    private String dateText;
    private long timestamp;
    private Pattern pattern;
    private Function1<Long, String> customPattern;
    private SimpleDateFormat simpleDateFormat;
    private SimpleDateFormat simpleDayFormat;
    private SimpleDateFormat simpleTimeFormat;
    private @StyleRes int dateTextAppearance;
    private @ColorInt int dateTextColor;
    private @ColorInt int strokeColor;
    private @Dimension int dateStrokeWidth;
    private @Dimension int dateCornerRadius;
    private @ColorInt int dateBackgroundColor;
    private DateTimeFormatterCallback dateTimeFormatterCallback;

    /**
     * Constructs a new CometChatDate object.
     *
     * @param context the context in which the CometChatDate is created
     */
    public CometChatDate(Context context) {
        this(context, null);
    }

    /**
     * Constructs a new CometChatDate object with the specified attributes.
     *
     * @param context the context in which the CometChatDate is created
     * @param attrs   the attributes of the XML tag that is inflating the view
     */
    public CometChatDate(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatDateStyle);
    }

    /**
     * Constructs a new CometChatDate object with the specified attributes and
     * style.
     *
     * @param context      the context in which the CometChatDate is created
     * @param attrs        the attributes of the XML tag that is inflating the view
     * @param defStyleAttr an attribute in the current theme that contains a reference to a
     *                     style resource
     */
    public CometChatDate(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            inflateAndInitializeView(attrs, defStyleAttr);
        }
    }

    /**
     * Inflates and initializes the view by setting up the layout, retrieving the
     * attributes, and applying styles.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        // Inflate the layout using ViewBinding
        binding = CometchatDateBinding.inflate(LayoutInflater.from(getContext()), this, true);
        // Set the default values for the CometChatDate
        setDefaultValues();
        // Apply the style attributes from XML
        applyStyleAttributes(attrs, defStyleAttr);
    }

    /**
     * Sets the default values for the CometChatDate.
     */
    private void setDefaultValues() {
        simpleDateFormat = new SimpleDateFormat(datePattern, CometChatLocalize.getDefault());
        simpleDayFormat = new SimpleDateFormat(dayPattern, CometChatLocalize.getDefault());
        simpleTimeFormat = new SimpleDateFormat(timePattern, CometChatLocalize.getDefault());
        dateTextColor = CometChatTheme.getTextColorSecondary(getContext());
        setTransparentBackground(true);
    }

    /**
     * Applies the style attributes from XML, allowing direct attribute overrides.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatDate, defStyleAttr, 0);
        @StyleRes int styleResId = directAttributes.getResourceId(R.styleable.CometChatDate_cometchatDateStyle, 0);
        directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatDate, defStyleAttr, styleResId);
        extractAttributesAndApplyDefaults(directAttributes);
    }

    /**
     * Configures whether the background should be transparent.
     *
     * @param isTrue If {@code true}, the background will be set to transparent. If
     *               {@code false}, default padding will be applied.
     */
    public void setTransparentBackground(boolean isTrue) {
        if (isTrue) {
            binding.layoutDate.setStrokeColor(Color.TRANSPARENT);
            binding.layoutDate.setStrokeWidth(0);
            binding.layoutDate.setCardElevation(0f);
            binding.layoutDate.setPadding(0, 0, 0, 0);
        } else {
            binding.layoutDate.setPadding(6, 6, 6, 6);
        }
    }

    /**
     * Extracts the attributes and applies the default values if they are not set in
     * the XML.
     *
     * @param typedArray The TypedArray containing the attributes to be extracted.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        if (typedArray == null) return;
        try {
            // Apply attributes if they are not already set
            dateBackgroundColor = typedArray.getColor(R.styleable.CometChatDate_cometchatDateBackgroundColor, dateBackgroundColor);
            dateTextAppearance = typedArray.getResourceId(R.styleable.CometChatDate_cometchatDateTextAppearance, dateTextAppearance);
            dateTextColor = typedArray.getColor(R.styleable.CometChatDate_cometchatDateTextColor, dateTextColor);
            dateCornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatDate_cometchatDateCornerRadius, dateCornerRadius);
            dateStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatDate_cometchatDateStrokeWidth, dateStrokeWidth);
            strokeColor = typedArray.getColor(R.styleable.CometChatDate_cometchatDateStrokeColor, strokeColor);
            // Apply default styles
            applyDefault();
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Applies the extracted or default values to the CometChatDate.
     */
    private void applyDefault() {
        setDateStrokeWidth(dateStrokeWidth);
        setDateCornerRadius(dateCornerRadius);
        setDateBackgroundColor(dateBackgroundColor);
        setDateTextAppearance(dateTextAppearance);
        setDateTextColor(dateTextColor);
        setStrokeColor(strokeColor);
    }

    /**
     * Sets the style for the CometChatDate view by applying a style resource.
     *
     * @param style The style resource to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatDate);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Gets the current date text displayed.
     *
     * @return The date text as a {@link String}.
     */
    public String getDateText() {
        return dateText;
    }

    /**
     * Sets the date text to be displayed.
     *
     * @param dateText The date text to set.
     */
    public void setDateText(String dateText) {
        this.dateText = dateText;
        binding.tvDate.setText(dateText);
    }

    /**
     * Gets the style resource ID for the date text.
     *
     * @return The style resource ID for the date text.
     */
    public @StyleRes int getDateTextAppearance() {
        return dateTextAppearance;
    }

    /**
     * Sets the style for the date text and updates the UI element accordingly.
     *
     * @param dateTextAppearance The style resource ID to be applied to the date text.
     */
    public void setDateTextAppearance(@StyleRes int dateTextAppearance) {
        this.dateTextAppearance = dateTextAppearance;
        binding.tvDate.setTextAppearance(dateTextAppearance);
    }

    /**
     * Gets the color of the stroke around the date background.
     *
     * @return The color of the stroke around the date background as an integer.
     */
    public @ColorInt int getStrokeColor() {
        return strokeColor;
    }

    /**
     * Sets the color of the stroke around the date background.
     */
    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        binding.layoutDate.setStrokeColor(strokeColor);
    }

    /**
     * Gets the color of the date text.
     *
     * @return The color of the date text as an integer.
     */
    public @ColorInt int getDateTextColor() {
        return dateTextColor;
    }

    /**
     * Sets the color of the date text and updates the UI element accordingly.
     *
     * @param dateTextColor The color to be applied to the date text.
     */
    public void setDateTextColor(@ColorInt int dateTextColor) {
        this.dateTextColor = dateTextColor;
        binding.tvDate.setTextColor(dateTextColor);
    }

    /**
     * Gets the stroke width of the date background.
     *
     * @return The stroke width of the date background in pixels.
     */
    public @Dimension int getDateStrokeWidth() {
        return dateStrokeWidth;
    }

    /**
     * Sets the stroke width for the date background and updates the UI element
     * accordingly.
     *
     * @param dateStrokeWidth The stroke width to be applied to the date background in pixels.
     */
    public void setDateStrokeWidth(@Dimension int dateStrokeWidth) {
        this.dateStrokeWidth = dateStrokeWidth;
        binding.layoutDate.setStrokeWidth(dateStrokeWidth);
    }

    /**
     * Gets the corner radius of the date background.
     *
     * @return The corner radius of the date background in pixels.
     */
    public @Dimension int getDateCornerRadius() {
        return dateCornerRadius;
    }

    /**
     * Sets the corner radius for the date background and updates the UI element
     * accordingly.
     *
     * @param dateCornerRadius The corner radius to be applied to the date background in pixels.
     */
    public void setDateCornerRadius(@Dimension int dateCornerRadius) {
        this.dateCornerRadius = dateCornerRadius;
        binding.layoutDate.setRadius(dateCornerRadius);
    }

    /**
     * Gets the background color of the date.
     *
     * @return The background color of the date as an integer.
     */
    public @ColorInt int getDateBackgroundColor() {
        return dateBackgroundColor;
    }

    /**
     * Sets the background color for the date and updates the UI element
     * accordingly.
     *
     * @param dateBackgroundColor The background color to be applied to the date.
     */
    public void setDateBackgroundColor(@ColorInt int dateBackgroundColor) {
        this.dateBackgroundColor = dateBackgroundColor;
        binding.layoutDate.setCardBackgroundColor(dateBackgroundColor);
    }

    /**
     * Sets the text alignment for the date text.
     *
     * @param alignment The text alignment to be applied. Should be one of the
     *                  {@link android.view.View} alignment constants (e.g.,
     *                  {@link android.view.View#TEXT_ALIGNMENT_CENTER}).
     */
    public void setDateTextAlignment(int alignment) {
        binding.tvDate.setTextAlignment(alignment);
    }

    /**
     * Sets the date using the specified timestamp and format.
     *
     * @param timestamp the timestamp of the date
     * @param format    the format of the date
     */
    public void setDate(long timestamp, String format) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp * 1000L);
        String date = DateFormat.format(format, cal).toString();
        binding.tvDate.setText(date);
    }

    /**
     * Sets the timestamp of the date.
     *
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        if (timestamp != 0) {
            this.timestamp = timestamp;
            setDate(timestamp, pattern);
        }
    }

    /**
     * Sets the date using the specified timestamp and pattern.
     *
     * @param timestamp the timestamp of the date
     * @param pattern   the pattern for displaying the date
     */
    public void setDate(long timestamp, Pattern pattern) {
        binding.tvDate.setVisibility(VISIBLE);
        if (getCustomPattern(timestamp) != null) {
            binding.tvDate.setText(getCustomPattern(timestamp));
        } else {
            if (timestamp != 0 && pattern != null) {
                if (Pattern.TIME.equals(pattern)) {
                    binding.tvDate.setText(getTime(timestamp));
                } else if (Pattern.DAY_DATE.equals(pattern)) {
                    binding.tvDate.setText(getDayDate(timestamp));
                } else if (Pattern.DAY_DATE_TIME.equals(pattern)) {
                    binding.tvDate.setText(getDayDateTime(timestamp));
                }
            }
        }
    }

    /**
     * Formats the given timestamp according to a custom pattern, if provided. If a
     * custom pattern function is set, it applies the function to the timestamp and
     * returns the result. Otherwise, it returns {@code null}.
     *
     * @param timeStamp The timestamp to format, in seconds.
     * @return The formatted string based on the custom pattern function, or
     * {@code null} if no pattern function is set.
     */
    @Nullable
    private String getCustomPattern(long timeStamp) {
        if (customPattern != null) {
            return customPattern.apply(timeStamp);
        }
        return null;
    }

    /**
     * Formats the given timestamp to a string representing the time in "h:mm a"
     * format.
     *
     * @param timestamp The timestamp to format, in seconds.
     * @return The formatted time string.
     */
    private String getTime(long timestamp) {
        long timeInMillis = timestamp * 1000;
        if (isDateTimeFormatterCallbackSet()) {
            String time = dateTimeFormatterCallback.time(timeInMillis);
            if (time != null) {
                return time;
            }
        }
        return simpleTimeFormat.format(new java.util.Date(timeInMillis));
    }

    /**
     * Formats the given timestamp to a string representing the day or date. It
     * returns "Today" if the date is the current day, "Yesterday" if the date is
     * one day before the current day, or the full date in "dd MMM yyyy" format
     * otherwise.
     *
     * @param timestamp The timestamp to format, in seconds.
     * @return A string indicating if the date is today, yesterday, or the formatted
     * date.
     */
    private String getDayDate(long timestamp) {
        Calendar now = Calendar.getInstance();
        long timeStampInMillis = timestamp * 1000;
        Calendar timeToCheck = Calendar.getInstance(Locale.ENGLISH);
        timeToCheck.setTimeInMillis(timeStampInMillis);
        if (now.get(Calendar.DAY_OF_YEAR) == timeToCheck.get(Calendar.DAY_OF_YEAR)) {
            if (isDateTimeFormatterCallbackSet()) {
                String today = dateTimeFormatterCallback.today(timeStampInMillis);
                if (today != null) {
                    return today;
                }
            }
            return getContext().getString(R.string.cometchat_today);
        } else if ((now.get(Calendar.DAY_OF_YEAR) - 1) == timeToCheck.get(Calendar.DAY_OF_YEAR)) {
            if (isDateTimeFormatterCallbackSet()) {
                String yesterday = dateTimeFormatterCallback.yesterday(timeStampInMillis);
                if (yesterday != null) {
                    return yesterday;
                }
            }
            return getContext().getString(R.string.cometchat_yesterday);
        } else {
            if (isDateTimeFormatterCallbackSet()) {
                String otherDays = dateTimeFormatterCallback.otherDays(timeStampInMillis);
                if (otherDays != null) {
                    return otherDays;
                }
            }
            return simpleDateFormat.format(new java.util.Date(timeStampInMillis));
        }
    }

    /**
     * Formats the given timestamp to a string representing the date and time. It
     * returns the time if the date is today, "Yesterday" if the date is one day
     * before the current day, the weekday if the date is within the last 7 days, or
     * the full date otherwise.
     *
     * @param timestamp The timestamp to format, in seconds.
     * @return A string representing the date and/or time.
     */
    private String getDayDateTime(long timestamp) {
        long timeInMillis = timestamp * 1000;
        String lastMessageDate = simpleDateFormat.format(new java.util.Date(timeInMillis));
        String lastMessageWeek = simpleDayFormat.format(new java.util.Date(timeInMillis));
        Calendar now = Calendar.getInstance();
        Calendar timeToCheck = Calendar.getInstance(Locale.ENGLISH);
        timeToCheck.setTimeInMillis(timeInMillis);
        if (now.get(Calendar.DAY_OF_YEAR) == timeToCheck.get(Calendar.DAY_OF_YEAR)) {
            return getTime(timestamp);
        } else if ((now.get(Calendar.DAY_OF_YEAR) - 1) == timeToCheck.get(Calendar.DAY_OF_YEAR)) {
            if (isDateTimeFormatterCallbackSet()) {
                String yesterday = dateTimeFormatterCallback.yesterday(timeInMillis);
                if (yesterday != null) {
                    return yesterday;
                }
            }
            return getContext().getString(R.string.cometchat_yesterday);
        } else if ((now.get(Calendar.DAY_OF_YEAR) - 7) <= timeToCheck.get(Calendar.DAY_OF_YEAR)) {
            if (isDateTimeFormatterCallbackSet()) {
                String lastWeek = dateTimeFormatterCallback.lastWeek(timeInMillis);
                if (lastWeek != null) {
                    return lastWeek;
                }
            }
            return lastMessageWeek;
        } else {
            if (isDateTimeFormatterCallbackSet()) {
                String otherDays = dateTimeFormatterCallback.otherDays(timeInMillis);
                if (otherDays != null) {
                    return otherDays;
                }
            }
            return lastMessageDate;
        }
    }

    private boolean isDateTimeFormatterCallbackSet() {
        return dateTimeFormatterCallback != null;
    }

    /**
     * Sets the pattern for displaying the date.
     *
     * @param pattern the pattern to set
     * @see Pattern
     */
    public void setPattern(Pattern pattern) {
        if (pattern != null) {
            this.pattern = pattern;
            setDate(timestamp, pattern);
        }
    }

    /**
     * Sets a custom string for the date.
     *
     * @param string the custom string to set
     */
    public void setCustomDateString(String string) {
        if (string != null && !string.isEmpty()) binding.tvDate.setText(string);
    }

    /**
     * Sets a custom pattern for the date.
     *
     * @param customPattern the custom pattern to set
     */
    private void setCustomPattern(Function1<Long, String> customPattern) {
        if (customPattern != null) {
            this.customPattern = customPattern;
            setDate(timestamp, pattern);
        }
    }

    public SimpleDateFormat getTimeFormat() {
        return simpleTimeFormat;
    }

    public void setTimeFormat(SimpleDateFormat timeFormat) {
        if (timeFormat != null) {
            this.simpleTimeFormat = timeFormat;
        }
    }

    public SimpleDateFormat getDateFormat() {
        return simpleDateFormat;
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        if (dateFormat != null) {
            this.simpleDateFormat = dateFormat;
        }
    }

    public SimpleDateFormat getDayFormat() {
        return simpleDayFormat;
    }

    public void setDayFormat(SimpleDateFormat dayFormat) {
        if (dayFormat != null) {
            this.simpleDayFormat = dayFormat;
        }
    }

    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String pattern) {
        if (pattern != null && !pattern.isEmpty()) {
            this.datePattern = pattern;
            this.simpleDateFormat = new SimpleDateFormat(pattern, CometChatLocalize.getDefault());
        }
    }

    public String getDayPattern() {
        return dayPattern;
    }

    public void setDayPattern(String pattern) {
        if (pattern != null && !pattern.isEmpty()) {
            this.dayPattern = pattern;
            this.simpleDayFormat = new SimpleDateFormat(pattern, CometChatLocalize.getDefault());
        }
    }

    public String getTimePattern() {
        return timePattern;
    }

    public void setTimePattern(String pattern) {
        if (pattern != null && !pattern.isEmpty()) {
            this.timePattern = pattern;
            this.simpleTimeFormat = new SimpleDateFormat(pattern, CometChatLocalize.getDefault());
        }
    }

    public void setPadding(int left, int top, int right, int bottom) {
        ViewGroup.LayoutParams layoutParams = binding.tvDate.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
            marginLayoutParams.setMargins(left > -1 ? left : 0, top > -1 ? top : 0, right > -1 ? right : 0, bottom > -1 ? bottom : 0);
            binding.tvDate.setLayoutParams(marginLayoutParams);
        }
    }

    public void setDateTimeFormatterCallback(DateTimeFormatterCallback dateTimeFormatterCallback) {
        if (dateTimeFormatterCallback != null) {
            this.dateTimeFormatterCallback = dateTimeFormatterCallback;
        }
    }
}
