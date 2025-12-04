package com.cometchat.chatuikit.report;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.FlagReason;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.shared.interfaces.OnClick;

import java.util.List;
import java.util.Map;

public class CometChatFlagMessageDialog extends Dialog {
    private static final String TAG = CometChatFlagMessageDialog.class.getSimpleName();

    private final CometChatFlagMessage flagMessageView;
    private final BaseMessage message;

    /**
     * Constructor for CometChatFlagMessageDialog
     *
     * @param context The context in which the dialog should run
     * @param message The message to be flagged
     */
    public CometChatFlagMessageDialog(@NonNull Context context, @NonNull BaseMessage message) {
        super(context);
        this.message = message;
        this.flagMessageView = new CometChatFlagMessage(context);
    }

    /**
     * Called when the dialog is created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(flagMessageView);
        configureDialogWindow();
    }

    /**
     * Configures the dialog window's layout and appearance
     */
    private void configureDialogWindow() {
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.getDecorView().setPadding(
                    getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_margin_4),
                    0,
                    getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_margin_4),
                    0);
            window.setGravity(Gravity.CENTER);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    // ==================== STATIC FACTORY METHODS ====================

    public void setFlagMessageStyle(@StyleRes int styleResId) {
        flagMessageView.setFlagMessageStyle(styleResId);
    }

    public void setFlagReasons(List<FlagReason> flagReasons) {
        flagMessageView.setFlagReasons(flagReasons);
    }

    // ==================== GETTERS ====================

    /**
     * Gets the base message being flagged
     *
     * @return The BaseMessage instance
     */
    public BaseMessage getMessage() {
        return message;
    }

    /**
     * Gets the flag message view
     *
     * @return The CometChatFlagMessage view instance
     */
    public CometChatFlagMessage getFlagMessageView() {
        return flagMessageView;
    }

    public void onFlagMessageError() {
        flagMessageView.onFlagMessageError();
    }

    public void hidePositiveButtonProgressBar(boolean b) {
        flagMessageView.hidePositiveButtonProgressBar(b);
    }

    public void setOnPositiveButtonClickListener(CometChatFlagMessage.OnReportClickListener clickListener) {
        flagMessageView.setOnReportClickListener(clickListener);
    }

    public void setOnCancelButtonClickListener(OnClick clickListener) {
        flagMessageView.setOnCancelClickListener(clickListener);
    }

    public void setOnCloseButtonClickListener(OnClick clickListener) {
        flagMessageView.setOnCloseButtonClickListener(clickListener);
    }

    public void setLocalizationIdMap(Map<String, Integer> localizationIdMap) {
        flagMessageView.setLocalizationIdMap(localizationIdMap);
    }

    public void setFlagRemarkInputFieldVisibility(int flagRemarkInputFieldVisibility) {
        flagMessageView.setFlagRemarkInputFieldVisibility(flagRemarkInputFieldVisibility);
    }
}