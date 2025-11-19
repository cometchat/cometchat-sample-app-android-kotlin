package com.cometchat.chatuikit.ai;

import com.cometchat.chatuikit.shared.views.aiconversationsummary.AIConversationSummaryExtension;

import java.util.ArrayList;
import java.util.List;

public class DefaultAIFeature {
    private static final String TAG = DefaultAIFeature.class.getSimpleName();

    public static List<AIExtensionDataSource> get() {
        List<AIExtensionDataSource> list = new ArrayList<>();
        list.add(new AIConversationSummaryExtension());
        return list;
    }
}
