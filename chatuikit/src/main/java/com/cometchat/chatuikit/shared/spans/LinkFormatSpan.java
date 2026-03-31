package com.cometchat.chatuikit.shared.spans;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

/**
 * Span for links with URL metadata in WYSIWYG rich text editing.
 * <p>
 * This span extends {@link ClickableSpan} to provide clickable link functionality
 * with underline and link color styling. It implements {@link RichTextFormatSpan}
 * to enable format detection and markdown conversion.
 * </p>
 * <p>
 * When applied to text, this span displays the link with:
 * <ul>
 *   <li>Underline styling to indicate it's a link</li>
 *   <li>Link color (from CometChatTheme primary color)</li>
 *   <li>Clickable behavior that opens the URL in a browser</li>
 * </ul>
 * No markdown syntax markers (like [text](url)) are shown. The markdown markers are only
 * generated when converting to markdown for sending messages.
 * </p>
 * <p>
 * The URL is stored as metadata within the span, enabling proper markdown conversion
 * when the message is sent.
 * </p>
 * <p>
 * Validates: Requirements 5.1, 5.4
 * </p>
 *
 * @see RichTextFormatSpan
 * @see FormatType#LINK
 */
public class LinkFormatSpan extends ClickableSpan implements RichTextFormatSpan {

    /**
     * The URL that this link points to.
     */
    @NonNull
    private String url;

    /**
     * The color to use for the link text.
     */
    @ColorInt
    private int linkColor;

    /**
     * Whether to show underline on the link text.
     */
    private boolean underlineEnabled;

    /**
     * Context for accessing theme colors. May be null if colors are set directly.
     */
    @Nullable
    private Context context;

    /**
     * Creates a new LinkFormatSpan with the specified URL.
     * <p>
     * Uses default styling with underline enabled and link color set to 0
     * (will use theme primary color when context is available).
     * </p>
     *
     * @param url The URL that this link points to.
     */
    public LinkFormatSpan(@NonNull String url) {
        this.url = url != null ? url : "";
        this.linkColor = 0;
        this.underlineEnabled = true;
        this.context = null;
    }

    /**
     * Creates a new LinkFormatSpan with the specified URL and context for theme colors.
     * <p>
     * Uses CometChatTheme to obtain the primary color for link styling.
     * </p>
     *
     * @param url     The URL that this link points to.
     * @param context The context used to access theme colors.
     */
    public LinkFormatSpan(@NonNull String url, @NonNull Context context) {
        this.url = url != null ? url : "";
        this.context = context;
        this.linkColor = CometChatTheme.getPrimaryColor(context);
        this.underlineEnabled = true;
    }

    /**
     * Creates a new LinkFormatSpan with custom styling.
     *
     * @param url              The URL that this link points to.
     * @param linkColor        The color to use for the link text.
     * @param underlineEnabled Whether to show underline on the link text.
     */
    public LinkFormatSpan(@NonNull String url, @ColorInt int linkColor, boolean underlineEnabled) {
        this.url = url != null ? url : "";
        this.linkColor = linkColor;
        this.underlineEnabled = underlineEnabled;
        this.context = null;
    }

    /**
     * Returns the URL that this link points to.
     *
     * @return The URL string.
     */
    @NonNull
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL that this link points to.
     *
     * @param url The URL string to set.
     */
    public void setUrl(@NonNull String url) {
        this.url = url != null ? url : "";
    }

    /**
     * Returns the format type associated with this span.
     *
     * @return {@link FormatType#LINK} indicating this is a link format span.
     */
    @Override
    public FormatType getFormatType() {
        return FormatType.LINK;
    }

    /**
     * Called when the link is clicked.
     * <p>
     * Opens the URL in the default browser application. If the URL is empty
     * or invalid, no action is taken.
     * </p>
     *
     * @param widget The view that was clicked.
     */
    @Override
    public void onClick(@NonNull View widget) {
        if (url.isEmpty()) {
            return;
        }

        try {
            String urlToOpen = url;
            // Add http:// prefix if no scheme is present
            if (!urlToOpen.startsWith("http://") && !urlToOpen.startsWith("https://")) {
                urlToOpen = "https://" + urlToOpen;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            widget.getContext().startActivity(intent);
        } catch (Exception e) {
            // Silently fail if URL cannot be opened
            // This can happen if no browser is installed or URL is malformed
        }
    }

    /**
     * Updates the draw state of the text paint for rendering the link.
     * <p>
     * This method applies underline styling if enabled.
     * The link color behavior:
     * <ul>
     *   <li>If linkColor is explicitly set (non-zero), use that color</li>
     *   <li>If context is provided, use theme primary color</li>
     *   <li>If neither is set, use default blue color (for composer compatibility)</li>
     * </ul>
     * For message bubbles, the bubble's setTextLinkColor() should be called after
     * setting text to override the default color with the theme-appropriate color.
     * </p>
     *
     * @param ds The TextPaint to update.
     */
    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        // Apply link color based on priority:
        // 1. Explicitly set linkColor
        // 2. Theme color from context
        // 3. Default blue for composer
        int color = getEffectiveLinkColor();
        if (color != 0) {
            ds.setColor(color);
        }
        
        // Apply underline styling
        ds.setUnderlineText(underlineEnabled);
    }

    /**
     * Gets the effective link color, using theme color if available.
     *
     * @return The link color to use.
     */
    @ColorInt
    private int getEffectiveLinkColor() {
        if (linkColor != 0) {
            return linkColor;
        }
        if (context != null) {
            return CometChatTheme.getPrimaryColor(context);
        }
        // Default to blue color for links
        return 0xFF3D88F5;
    }

    // ==================== Getters and Setters ====================

    /**
     * Gets the link color.
     *
     * @return The link color.
     */
    @ColorInt
    public int getLinkColor() {
        return linkColor;
    }

    /**
     * Sets the link color.
     *
     * @param linkColor The link color to set.
     */
    public void setLinkColor(@ColorInt int linkColor) {
        this.linkColor = linkColor;
    }

    /**
     * Checks if underline is enabled for this link.
     *
     * @return true if underline is enabled, false otherwise.
     */
    public boolean isUnderlineEnabled() {
        return underlineEnabled;
    }

    /**
     * Sets whether underline should be shown for this link.
     *
     * @param underlineEnabled true to enable underline, false to disable.
     */
    public void setUnderlineEnabled(boolean underlineEnabled) {
        this.underlineEnabled = underlineEnabled;
    }
}
