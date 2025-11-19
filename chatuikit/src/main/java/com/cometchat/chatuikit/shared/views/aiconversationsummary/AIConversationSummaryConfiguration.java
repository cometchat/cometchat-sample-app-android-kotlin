package com.cometchat.chatuikit.shared.views.aiconversationsummary;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StyleRes;

import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.shared.interfaces.Function2;
import com.cometchat.chatuikit.shared.interfaces.Function3;

import org.json.JSONObject;

import java.util.HashMap;

public class AIConversationSummaryConfiguration {
    private static final String TAG = AIConversationSummaryConfiguration.class.getSimpleName();
    private Function3<HashMap<String, String>, User, Group, JSONObject> apiConfiguration;
    private Function2<Context, String, View> summaryView;
    private int unreadMessageThreshold = 30;
    private @LayoutRes int ErrorStateView;
    private @LayoutRes int loadingStateView;
    private @DrawableRes int buttonIcon;
    private Drawable closeIcon;
    private String loadingText;
    private String errorText;
    private String emptyText;
    private @StyleRes int aiConversationSummaryStyle;

    public AIConversationSummaryConfiguration setButtonIcon(@DrawableRes int buttonIcon) {
        this.buttonIcon = buttonIcon;
        return this;
    }

    public AIConversationSummaryConfiguration setCloseIcon(Drawable closeIcon) {
        this.closeIcon = closeIcon;
        return this;
    }

    public AIConversationSummaryConfiguration setStyle(@StyleRes int aiConversationSummaryStyle) {
        this.aiConversationSummaryStyle = aiConversationSummaryStyle;
        return this;
    }

    public AIConversationSummaryConfiguration setLoadingStateView(int loadingStateView) {
        this.loadingStateView = loadingStateView;
        return this;
    }

    public AIConversationSummaryConfiguration setErrorStateView(int errorStateView) {
        ErrorStateView = errorStateView;
        return this;
    }

    public AIConversationSummaryConfiguration setLoadingText(String loadingText) {
        this.loadingText = loadingText;
        return this;
    }

    public AIConversationSummaryConfiguration setErrorText(String errorText) {
        this.errorText = errorText;
        return this;
    }

    public AIConversationSummaryConfiguration setEmptyText(String emptyText) {
        this.emptyText = emptyText;
        return this;
    }

    public int getErrorStateView() {
        return ErrorStateView;
    }

    public int getLoadingStateView() {
        return loadingStateView;
    }

    public String getLoadingText() {
        return loadingText;
    }

    public String getErrorText() {
        return errorText;
    }

    public String getEmptyText() {
        return emptyText;
    }

    public @StyleRes int getStyle() {
        return aiConversationSummaryStyle;
    }

    public AIConversationSummaryConfiguration setApiConfiguration(Function3<HashMap<String, String>, User, Group, JSONObject> apiConfiguration) {
        this.apiConfiguration = apiConfiguration;
        return this;
    }

    public AIConversationSummaryConfiguration setSummaryView(Function2<Context, String, View> summaryView) {
        this.summaryView = summaryView;
        return this;
    }

    public AIConversationSummaryConfiguration setUnreadMessageThreshold(int unreadMessageThreshold) {
        this.unreadMessageThreshold = unreadMessageThreshold;
        return this;
    }

    public Function3<HashMap<String, String>, User, Group, JSONObject> getApiConfiguration() {
        return apiConfiguration;
    }

    public Function2<Context, String, View> getSummaryView() {
        return summaryView;
    }

    public int getUnreadMessageThreshold() {
        return unreadMessageThreshold;
    }

    public int getButtonIcon() {
        return buttonIcon;
    }

    public Drawable getCloseIcon() {
        return closeIcon;
    }
}
