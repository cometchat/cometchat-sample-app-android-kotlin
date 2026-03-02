package com.cometchat.chatuikit.shared.views.messageinput;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;

import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.spans.NonEditableSpan;

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
        return InputConnectionCompat.createWrapper(ic, outAttrs, callback);
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
