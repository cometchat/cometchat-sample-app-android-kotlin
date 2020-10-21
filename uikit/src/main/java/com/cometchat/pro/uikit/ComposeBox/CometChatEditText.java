package com.cometchat.pro.uikit.ComposeBox;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputContentInfo;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.os.BuildCompat;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.MediaMessage;

import java.io.File;

import utils.MediaUtils;
import utils.Utils;

public class CometChatEditText extends AppCompatEditText {

    private static final String TAG = "CometChatEditText";

    public OnEditTextMediaListener onEditTextMediaListener;

    public CometChatEditText(Context context) {
        super(context);
    }

    public CometChatEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CometChatEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        final InputConnection ic = super.onCreateInputConnection(outAttrs);
        EditorInfoCompat.setContentMimeTypes(outAttrs,
                new String [] {"image/png","image/gif"});


        final InputConnectionCompat.OnCommitContentListener callback =
                new InputConnectionCompat.OnCommitContentListener() {
                    @Override
                    public boolean onCommitContent(InputContentInfoCompat inputContentInfo,
                                                   int flags, Bundle opts) {
                        // read and display inputContentInfo asynchronously
                        if (BuildCompat.isAtLeastNMR1() && (flags &
                                InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
                            try {
                                inputContentInfo.requestPermission();
                            }
                            catch (Exception e) {
                                return false; // return false if failed
                            }
                        }
                        ContentResolver cr = getContext().getContentResolver();
                        String mimeType = cr.getType(inputContentInfo.getLinkUri());

                        Log.e(TAG, "onCommitContent: "+inputContentInfo.getLinkUri().getPath()
                                +"\n"+inputContentInfo.getContentUri()+"\n"+
                                mimeType);
                        onEditTextMediaListener.OnMediaSelected(inputContentInfo);
                        // read and display inputContentInfo asynchronously.
                        // call inputContentInfo.releasePermission() as needed.

                        return true;  // return true if succeeded
                    }
                };
        return InputConnectionCompat.createWrapper(ic, outAttrs, callback);
    }

    public void setMediaSelected(OnEditTextMediaListener onEditTextMediaListener) {
        this.onEditTextMediaListener = onEditTextMediaListener;
    }

    public interface OnEditTextMediaListener {
        void OnMediaSelected(InputContentInfoCompat i);
    }
}
