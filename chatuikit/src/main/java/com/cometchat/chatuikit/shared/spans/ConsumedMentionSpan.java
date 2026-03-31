package com.cometchat.chatuikit.shared.spans;

import androidx.annotation.Nullable;

import com.cometchat.chatuikit.shared.formatters.style.PromptTextStyle;
import com.cometchat.chatuikit.shared.views.suggestionlist.SuggestionItem;

/**
 * Marker span applied to plain-text mentions inserted inside code formatting
 * (inline code or code block). This span has no visual effect — it simply marks
 * the mention text so the scanning loop in the composer knows to skip the
 * tracking character ('@') and not re-trigger the suggestion list.
 * <p>
 * Unlike {@link NonEditableSpan}, this span does not make the text non-editable
 * or change its appearance. It is only used as a signal for mention detection logic.
 * </p>
 * <p>
 * When a code block is applied over existing mentions, the original
 * {@link NonEditableSpan} data (id, text, suggestionItem, textAppearance) is
 * stored in this span so it can be restored when the code block is removed.
 * </p>
 */
public class ConsumedMentionSpan {

    private final char id;
    @Nullable
    private final String text;
    @Nullable
    private final SuggestionItem suggestionItem;
    @Nullable
    private final PromptTextStyle textAppearance;

    /**
     * Creates a plain marker with no restoration data.
     * Used when a mention is inserted as plain text inside inline code.
     */
    public ConsumedMentionSpan() {
        this.id = 0;
        this.text = null;
        this.suggestionItem = null;
        this.textAppearance = null;
    }

    /**
     * Creates a marker that stores the original {@link NonEditableSpan} data
     * so the mention can be restored when the code block is removed.
     *
     * @param id             The tracking character (e.g. '@').
     * @param text           The mention display text.
     * @param suggestionItem The original suggestion item (may be null).
     * @param textAppearance The original text appearance (may be null).
     */
    public ConsumedMentionSpan(char id, @Nullable String text,
                               @Nullable SuggestionItem suggestionItem,
                               @Nullable PromptTextStyle textAppearance) {
        this.id = id;
        this.text = text;
        this.suggestionItem = suggestionItem;
        this.textAppearance = textAppearance;
    }

    /**
     * Returns {@code true} if this span stores enough data to restore a
     * {@link NonEditableSpan}.
     */
    public boolean canRestore() {
        return text != null;
    }

    public char getId() {
        return id;
    }

    @Nullable
    public String getText() {
        return text;
    }

    @Nullable
    public SuggestionItem getSuggestionItem() {
        return suggestionItem;
    }

    @Nullable
    public PromptTextStyle getTextAppearance() {
        return textAppearance;
    }
}
