package com.cometchat.chatuikit.shared.spans;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;

import com.cometchat.chatuikit.shared.formatters.style.PromptTextStyle;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.views.suggestionlist.SuggestionItem;

import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class NonEditableSpan extends ClickableSpan {
    private static final String TAG = NonEditableSpan.class.getSimpleName();


    private String mText;
    private char id;
    private PromptTextStyle textAppearance;
    private SuggestionItem suggestionItem;

    public NonEditableSpan(char id, @Nonnull String text, @Nonnull SuggestionItem suggestionItem) {
        this.mText = text;
        this.id = id;
        this.suggestionItem = suggestionItem;
        this.textAppearance = suggestionItem.getPromptTextAppearance();
    }

    public NonEditableSpan(char id, @Nonnull String text, @Nullable PromptTextStyle textAppearance) {
        this.mText = text;
        this.id = id;
        this.textAppearance = textAppearance;
    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
    }

    public char getId() {
        return id;
    }

    public void setId(char id) {
        this.id = id;
    }

    public PromptTextStyle getTextAppearance() {
        return textAppearance;
    }

    public void setTextAppearance(PromptTextStyle textAppearance) {
        this.textAppearance = textAppearance;
    }

    public SuggestionItem getSuggestionItem() {
        return suggestionItem;
    }

    public void setSuggestionItem(SuggestionItem suggestionItem) {
        this.suggestionItem = suggestionItem;
    }

    @Override
    public void onClick(@NonNull View widget) {
        widget.setEnabled(false);
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        if (textAppearance == null) return;

        if (textAppearance.getTextAppearance() != null)
            ds.setTypeface(textAppearance.getTextAppearance());

        if (textAppearance.getColor() != 0) ds.setColor(textAppearance.getColor());

        if (textAppearance.getTextSize() > -1) ds.setTextSize(textAppearance.getTextSize());

        if (textAppearance.getBackgroundColor() != 0)
            ds.bgColor = (Utils.applyColorWithAlphaValue(textAppearance.getBackgroundColor(), 51));

        // Explicitly clear text decorations that may bleed through from
        // overlapping RichTextFormatSpan instances (e.g., UnderlineFormatSpan).
        // Even when removeFormatsFromMentions() splits format spans around the
        // mention, Android may still apply the decoration if the span boundaries
        // overlap by even one position. Clearing here acts as a defensive measure.
        ds.setUnderlineText(false);
        ds.setStrikeThruText(false);
    }
}
