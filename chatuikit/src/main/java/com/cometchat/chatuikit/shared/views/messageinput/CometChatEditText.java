package com.cometchat.chatuikit.shared.views.messageinput;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Patterns;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;

import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.spans.MarkdownConverter;
import com.cometchat.chatuikit.shared.spans.NonEditableSpan;
import com.cometchat.chatuikit.shared.spans.RichTextFormatSpan;

/**
 * CometChatEditText class is a subclass of AppCompatEditText which is a widget
 * for user to enter text. It is a customizable EditText which can be used in
 * CometChatMessageInput view. It has some additional features like adding
 * media, selecting media, removing media, etc. Created at: 11 September 2024
 * Modified at:
 */
@SuppressWarnings("unused")
public class CometChatEditText extends AppCompatEditText {
    private static final String TAG = CometChatEditText.class.getSimpleName();

    public OnEditTextMediaListener onEditTextMediaListener;
    public CometChatTextWatcher textWatcher;
    public Context context;
    private OnPasteLinkListener onPasteLinkListener;
    private OnRichTextPasteListener onRichTextPasteListener;

    /**
     * Constructor with parameter
     *
     * @param context the context
     */
    public CometChatEditText(Context context) {
        super(context);
        this.context = context;
        init();
    }

    /**
     * This method is used to initialize the views and listeners.
     */
    private void init() {
        // Force software rendering so that LineBackgroundSpan backgrounds
        // and the cursor are drawn in a deterministic order.  Without this,
        // hardware-accelerated partial invalidations can cause the code-block
        // background to paint over the blinking cursor (AOSP bug b/37044606).
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        addTextChangedListener(new TagWatcher());
    }

    /**
     * Constructor with parameter
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public CometChatEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    /**
     * Constructor with parameter
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    public CometChatEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    /**
     * This method is used to create InputConnection for the CometChatEditText.
     *
     * @param outAttrs the out attrs
     * @return the input connection
     */
    @Override
    public InputConnection onCreateInputConnection(@NonNull EditorInfo outAttrs) {
        final InputConnection ic = super.onCreateInputConnection(outAttrs);
        EditorInfoCompat.setContentMimeTypes(outAttrs, new String[]{"image/png", "image/gif"});
        final InputConnectionCompat.OnCommitContentListener callback = (inputContentInfo, flags, opts) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
                try {
                    inputContentInfo.requestPermission();
                } catch (Exception e) {
                    return false;
                }
            }
            try {
                onEditTextMediaListener.OnMediaSelected(inputContentInfo);
            } catch (Exception e) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    inputContentInfo.releasePermission();
                }
            }
            return true;
        };
        if (ic == null) {
            return null;
        }
        InputConnection wrapped = InputConnectionCompat.createWrapper(ic, outAttrs, callback);
        return wrapped;
    }

    /**
     * This method is used to set the media selected listener.
     *
     * @param onEditTextMediaListener the listener
     */
    public void setMediaSelected(OnEditTextMediaListener onEditTextMediaListener) {
        this.onEditTextMediaListener = onEditTextMediaListener;
    }

    /**
     * This method is used to remove the media selected listener.
     */
    public void removeMediaSelected() {
        this.onEditTextMediaListener = null;
    }

    /**
     * This method is used to remove the media selected listener.
     */
    public void setTextWatcher(CometChatTextWatcher textWatcher) {
        this.textWatcher = textWatcher;
    }

    /**
     * This method is used to remove the media selected listener.
     */
    public void removeTextWatcher() {
        this.textWatcher = null;
    }

    /**
     * Guard against AOSP bug where {@code Editor$InsertionPointCursorController}
     * is null when software rendering is enabled (LAYER_TYPE_SOFTWARE).
     * This manifests as a NullPointerException on certain devices (e.g. Pixel Fold)
     * when the user long-presses the EditText.
     */
    @Override
    public boolean performLongClick() {
        try {
            return super.performLongClick();
        } catch (NullPointerException e) {
            // Swallow the NPE from Editor.performLongClick() when
            // InsertionPointCursorController is null due to software rendering.
            return true;
        }
    }

    /**
     * This method is used to handle the selection change event.
     *
     * @param selStart The start position of the selection.
     * @param selEnd   The end position of the selection.
     */
    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if (textWatcher != null) {
            textWatcher.onSelectionChanged(selStart, selEnd);
        }
        try {
            if (getText() == null) return;
            NonEditableSpan[] spans = getText().getSpans(0, getText().length(), NonEditableSpan.class);
            for (NonEditableSpan span : spans) {
                int spanStart = getText().getSpanStart(span);
                int spanEnd = getText().getSpanEnd(span);
                if (selStart > spanStart && selStart < spanEnd) {
                    if (Math.abs(selStart - spanStart) < Math.abs(selStart - spanEnd)) {
                        setSelection(spanStart);
                    } else {
                        setSelection(spanEnd + 1);
                    }
                    return;
                }
                // Check if the selection end is within a span
                if (selEnd > spanStart && selEnd < spanEnd) {
                    setSelection(selStart, spanStart); // End the selection at the start of the span
                    return;
                }
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
        super.onSelectionChanged(selStart, selEnd);
    }

    /**
     * This method is used to delete the span if needed.
     */
    private void deleteSpanIfNeeded() {
        int selectionStart = getSelectionStart();
        if (getText() == null) return;
        NonEditableSpan[] spans = getText().getSpans(0, getText().length(), NonEditableSpan.class);
        for (NonEditableSpan span : spans) {
            int spanStart = getText().getSpanStart(span);
            int spanEnd = getText().getSpanEnd(span);
            String spanText = getText().toString().substring(spanStart, spanEnd);
            // Check if the cursor is right after a span.
            if (selectionStart == spanEnd && !spanText.equals(span.getText())) {
                // Delete the entire span.
                getText().delete(spanStart, spanEnd);
                if (textWatcher != null) textWatcher.onSpanDeleted(span);
                return; // We found and deleted the span, no need to check further.
            }
        }
    }

    /**
     * Sets the listener for paste-link-on-selection events.
     *
     * @param listener The listener to set, or null to remove.
     */
    public void setOnPasteLinkListener(@Nullable OnPasteLinkListener listener) {
        this.onPasteLinkListener = listener;
    }

    /**
     * Sets the listener for rich text paste events.
     * When set, pasted text containing markdown formatting will be parsed
     * and inserted with formatting spans instead of plain text.
     *
     * @param listener The listener to set, or null to remove.
     */
    public void setOnRichTextPasteListener(@Nullable OnRichTextPasteListener listener) {
        this.onRichTextPasteListener = listener;
    }

    /**
     * Intercepts copy, cut, and paste actions to preserve rich text formatting.
     * <p>
     * For copy/cut: Converts formatting spans in the selected text to markdown
     * syntax before placing it on the clipboard. This ensures that when the text
     * is pasted back, the markdown can be parsed to restore formatting.
     * </p>
     * <p>
     * For paste: Parses markdown from clipboard content and inserts it with
     * the appropriate formatting spans. Also handles paste-link-on-selection.
     * </p>
     *
     * @param id The identifier of the context menu item.
     * @return true if the action was handled, false to let the default behavior proceed.
     */
    @Override
    public boolean onTextContextMenuItem(int id) {
        // Handle copy and cut: convert spans to markdown for clipboard
        if (id == android.R.id.copy || id == android.R.id.cut) {
            if (onRichTextPasteListener != null) {
                Editable editable = getText();
                if (editable != null) {
                    int selStart = getSelectionStart();
                    int selEnd = getSelectionEnd();
                    if (selStart != selEnd && selStart >= 0 && selEnd <= editable.length()) {
                        // Check if the selected text has any formatting spans
                        RichTextFormatSpan[] spans = editable.getSpans(selStart, selEnd, RichTextFormatSpan.class);
                        if (spans != null && spans.length > 0) {
                            // Extract the selected portion as a new Spannable with spans preserved
                            SpannableStringBuilder selectedSpannable = new SpannableStringBuilder(editable, selStart, selEnd);
                            // Convert to markdown
                            String markdown = MarkdownConverter.toMarkdown(selectedSpannable);
                            if (markdown != null && !markdown.isEmpty()) {
                                // Place markdown on clipboard
                                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                if (clipboard != null) {
                                    ClipData clipData = ClipData.newPlainText("Formatted Text", markdown);
                                    clipboard.setPrimaryClip(clipData);
                                    // For cut, delete the selected text after copying
                                    if (id == android.R.id.cut) {
                                        editable.delete(selStart, selEnd);
                                    }
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            // Fall through to default behavior if no formatting spans found
        }

        if (id == android.R.id.paste || id == android.R.id.pasteAsPlainText) {
            int selStart = getSelectionStart();
            int selEnd = getSelectionEnd();

            // First, check for paste-link-on-selection (existing behavior)
            if (selStart != selEnd && onPasteLinkListener != null) {
                String clipboardUrl = getClipboardUrl();
                if (clipboardUrl != null) {
                    String selectedText = getText() != null
                            ? getText().subSequence(selStart, selEnd).toString()
                            : "";
                    if (onPasteLinkListener.onPasteLink(selectedText, clipboardUrl, selStart, selEnd)) {
                        return true;
                    }
                }
            }

            // Then, check for rich text paste (markdown in clipboard)
            if (onRichTextPasteListener != null) {
                String clipboardText = getClipboardText();
                if (clipboardText != null && !clipboardText.isEmpty()) {
                    SpannableString parsed = MarkdownConverter.fromMarkdown(clipboardText, getContext(), false);
                    // Check if fromMarkdown actually produced any formatting spans
                    boolean hasFormatting = parsed.getSpans(0, parsed.length(), RichTextFormatSpan.class).length > 0;
                    if (hasFormatting) {
                        Editable editable = getText();
                        if (editable != null) {
                            // Delete any selected text first
                            if (selStart != selEnd) {
                                editable.delete(selStart, selEnd);
                            }
                            // Insert the parsed spannable at the cursor position
                            editable.insert(selStart, parsed);
                            // Place cursor at the end of the inserted text
                            setSelection(selStart + parsed.length());
                            // Notify the listener so the composer can update toolbar state
                            onRichTextPasteListener.onRichTextPaste(selStart, selStart + parsed.length());
                            return true;
                        }
                    }
                }
            }
        }
        return super.onTextContextMenuItem(id);
    }

    /**
     * Extracts a URL from the clipboard if the clipboard contains a single URL string.
     *
     * @return The URL string if the clipboard contains a valid URL, null otherwise.
     */
    @Nullable
    private String getClipboardUrl() {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null || !clipboard.hasPrimaryClip()) {
            return null;
        }
        ClipData clipData = clipboard.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) {
            return null;
        }
        CharSequence clipText = clipData.getItemAt(0).getText();
        if (clipText == null) {
            return null;
        }
        String text = clipText.toString().trim();
        if (Patterns.WEB_URL.matcher(text).matches()) {
            return text;
        }
        return null;
    }

    /**
     * Extracts the text content from the clipboard.
     *
     * @return The clipboard text, or null if the clipboard is empty or unavailable.
     */
    @Nullable
    private String getClipboardText() {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null || !clipboard.hasPrimaryClip()) {
            return null;
        }
        ClipData clipData = clipboard.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) {
            return null;
        }
        CharSequence clipText = clipData.getItemAt(0).getText();
        return clipText != null ? clipText.toString() : null;
    }

    /**
     * Listener interface for when a URL is pasted over selected text.
     */
    public interface OnPasteLinkListener {
        /**
         * Called when a URL is pasted while text is selected.
         *
         * @param selectedText The currently selected text.
         * @param url          The URL from the clipboard.
         * @param selStart     The start position of the selection.
         * @param selEnd       The end position of the selection.
         * @return true if the event was handled (link was embedded), false to proceed with default paste.
         */
        boolean onPasteLink(String selectedText, String url, int selStart, int selEnd);
    }

    /**
     * Listener interface for when text with rich formatting (markdown) is pasted.
     */
    public interface OnRichTextPasteListener {
        /**
         * Called after markdown-formatted text has been parsed and inserted with
         * formatting spans. The composer should update its toolbar state to reflect
         * the formats present in the pasted content.
         *
         * @param pasteStart The start position of the pasted content.
         * @param pasteEnd   The end position of the pasted content.
         */
        void onRichTextPaste(int pasteStart, int pasteEnd);
    }

    /**
     * This interface is used to handle the media selection event.
     */
    public interface OnEditTextMediaListener {
        void OnMediaSelected(InputContentInfoCompat i);
    }

    /**
     * This class is used to watch the text changes in the CometChatEditText.
     */
    private class TagWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            if (textWatcher != null) {
                textWatcher.beforeTextChanged(charSequence, start, count, after);
            }
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            if (textWatcher != null) {
                textWatcher.onTextChanged(charSequence, start, before, count);
            }
            if (before == 1 && count == 0) {
                deleteSpanIfNeeded();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (textWatcher != null) {
                textWatcher.afterTextChanged(s);
            }
        }
    }
}
