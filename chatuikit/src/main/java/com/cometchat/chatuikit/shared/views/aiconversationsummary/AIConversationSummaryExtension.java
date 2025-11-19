package com.cometchat.chatuikit.shared.views.aiconversationsummary;

import com.cometchat.chatuikit.ai.AIExtensionDataSource;
import com.cometchat.chatuikit.shared.framework.ChatConfigurator;

public class AIConversationSummaryExtension extends AIExtensionDataSource {
    private static final String TAG = AIConversationSummaryExtension.class.getSimpleName();
    private AIConversationSummaryConfiguration configuration;
    public static final String ID = "conversation-summary";

    public AIConversationSummaryExtension(AIConversationSummaryConfiguration configuration) {
        this.configuration = configuration;
    }

    public AIConversationSummaryExtension() {
    }

    @Override
    public void addExtension() {
        ChatConfigurator.enable(var1 -> new AIConversationSummaryDecorator(var1, configuration));
    }

    @Override
    public String getExtensionId() {
        return ID;
    }
}
