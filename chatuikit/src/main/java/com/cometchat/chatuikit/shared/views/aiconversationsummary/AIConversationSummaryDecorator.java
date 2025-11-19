package com.cometchat.chatuikit.shared.views.aiconversationsummary;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.ai.AIOptionsStyle;
import com.cometchat.chatuikit.shared.views.aiconversationsummary.view.CometChatAIConversationSummaryView;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.events.CometChatMessageEvents;
import com.cometchat.chatuikit.shared.events.CometChatUIEvents;
import com.cometchat.chatuikit.shared.framework.DataSource;
import com.cometchat.chatuikit.shared.framework.DataSourceDecorator;
import com.cometchat.chatuikit.shared.models.AdditionParameter;
import com.cometchat.chatuikit.shared.models.CometChatMessageComposerAction;
import com.cometchat.chatuikit.shared.models.interactivemessage.CardMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.CustomInteractiveMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.FormMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.SchedulerMessage;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class AIConversationSummaryDecorator extends DataSourceDecorator {
    private static final String TAG = AIConversationSummaryDecorator.class.getSimpleName();
    private final AIConversationSummaryConfiguration configuration;
    private int unreadMessageThreshold = 30;
    private HashMap<String, String> id;
    private User userTemp;
    private Group groupTemp;

    public AIConversationSummaryDecorator(DataSource dataSource) {
        super(dataSource);
        configuration = null;
        addListeners();
    }

    public void addListeners() {
        String LISTENER_ID = System.currentTimeMillis() + AIConversationSummaryDecorator.class.getSimpleName();
        CometChatUIEvents.addListener(LISTENER_ID, new CometChatUIEvents() {
            /**
             * @param hashId
             *            The ID of the active chat.
             * @param message
             *            The last message in the chat.
             * @param user
             *            The user associated with the chat (if it's a user chat).
             * @param group
             *            The group associated with the chat (if it's a group chat).
             * @param unreadCount
             *            The unread count of the chat.
             */
            @Override
            public void ccActiveChatChanged(HashMap<String, String> hashId, BaseMessage message, User user, Group group, int unreadCount) {
                id = hashId;
                userTemp = user;
                groupTemp = group;
                if (configuration != null) {
                    unreadMessageThreshold = configuration.getUnreadMessageThreshold();
                }
                if (unreadCount != 0 && unreadCount >= unreadMessageThreshold) {
                    showSummaryPanel(
                        id,
                        UIKitConstants.CustomUIPosition.MESSAGE_LIST_BOTTOM,
                        configuration != null ? configuration.getStyle() : 0,
                        user,
                        group
                    );
                }
            }
        });
        CometChatMessageEvents.addListener(LISTENER_ID, new CometChatMessageEvents() {
            @Override
            public void ccMessageSent(BaseMessage baseMessage, int status) {
                hideSummaryPanel();
            }

            @Override
            public void onTextMessageReceived(TextMessage textMessage) {
                hidePanelRealTime(textMessage);
            }

            @Override
            public void onMediaMessageReceived(MediaMessage mediaMessage) {
                hidePanelRealTime(mediaMessage);
            }

            @Override
            public void onCustomMessageReceived(CustomMessage customMessage) {
                hidePanelRealTime(customMessage);
            }

            @Override
            public void onFormMessageReceived(FormMessage formMessage) {
                hidePanelRealTime(formMessage);
            }

            @Override
            public void onSchedulerMessageReceived(SchedulerMessage schedulerMessage) {
                hidePanelRealTime(schedulerMessage);
            }

            @Override
            public void onCardMessageReceived(CardMessage cardMessage) {
                hidePanelRealTime(cardMessage);
            }

            @Override
            public void onCustomInteractiveMessageReceived(CustomInteractiveMessage customInteractiveMessage) {
                hidePanelRealTime(customInteractiveMessage);
            }
        });
    }

    public void showSummaryPanel(
        HashMap<String, String> id, UIKitConstants.CustomUIPosition alignment, @StyleRes int aiConversationSummaryStyle, User user, Group group
    ) {
        for (CometChatUIEvents events : CometChatUIEvents.uiEvents.values()) {
            events.showPanel(id, alignment, context -> getSummaryPanel(id, context, aiConversationSummaryStyle, user, group));
            events.hidePanel(id, UIKitConstants.CustomUIPosition.COMPOSER_BOTTOM);
        }
    }

    public void hideSummaryPanel() {
        CometChatUIKitHelper.hidePanel(id, UIKitConstants.CustomUIPosition.MESSAGE_LIST_BOTTOM);
    }

    private void hidePanelRealTime(BaseMessage baseMessage) {
        if (baseMessage != null) {
            if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER) && userTemp != null) {
                if (baseMessage.getSender() != null && baseMessage.getSender().getUid() != null && userTemp.getUid().equalsIgnoreCase(baseMessage
                                                                                                                                          .getSender()
                                                                                                                                          .getUid())) {
                    hideSummaryPanel();
                }
            } else if (baseMessage.getReceiverType().equals(UIKitConstants.ReceiverType.GROUP) && groupTemp != null) {
                if (groupTemp.getGuid() != null && groupTemp.getGuid().equalsIgnoreCase(baseMessage.getReceiverUid())) {
                    hideSummaryPanel();
                }
            }
        }
    }

    public View getSummaryPanel(
        HashMap<String, String> idMap, Context context, @StyleRes int aiConversationSummaryStyle, User userTemp, Group groupTemp
    ) {

        CometChatAIConversationSummaryView aiConversationsSummaryView = new CometChatAIConversationSummaryView(context);
        aiConversationsSummaryView.setStyle(aiConversationSummaryStyle);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(
            context.getResources().getDimensionPixelSize(R.dimen.cometchat_10dp),
            0,
            context.getResources().getDimensionPixelSize(R.dimen.cometchat_10dp),
            context.getResources().getDimensionPixelSize(R.dimen.cometchat_10dp)
        );
        aiConversationsSummaryView.setLayoutParams(layoutParams);

        aiConversationsSummaryView.setMaxHeight(250);

        JSONObject configurationJsonObject = null;
        if (configuration != null) {
            aiConversationsSummaryView.setErrorViewLayout(configuration.getErrorStateView());
            aiConversationsSummaryView.setLoadingViewLayout(configuration.getLoadingStateView());
            aiConversationsSummaryView.setErrorStateText(configuration.getErrorText());
            aiConversationsSummaryView.setCloseIcon(configuration.getCloseIcon());
            if (configuration.getApiConfiguration() != null) {
                configurationJsonObject = configuration.getApiConfiguration().apply(idMap, userTemp, groupTemp);
            }
        }
        aiConversationsSummaryView.showLoadingView();
        CometChat.getConversationSummary(
            userTemp != null ? userTemp.getUid() : groupTemp.getGuid(),
            userTemp != null ? UIKitConstants.ReceiverType.USER : UIKitConstants.ReceiverType.GROUP,
            configurationJsonObject,
            new CometChat.CallbackListener<String>() {
                @Override
                public void onSuccess(String s) {
                    if (s != null && !s.isEmpty()) {
                        if (configuration != null && configuration.getSummaryView() != null) {
                            View view = configuration.getSummaryView().apply(context, s);
                            aiConversationsSummaryView.setCustomView(view);
                        } else {
                            aiConversationsSummaryView.setSummary(s);
                        }
                    } else {
                        hidePanel(idMap);
                    }
                }

                @Override
                public void onError(CometChatException e) {
                    aiConversationsSummaryView.showErrorView();
                }
            }
        );

        aiConversationsSummaryView.setOnCloseClick(view -> hidePanel(idMap));
        return aiConversationsSummaryView;
    }

    public void hidePanel(HashMap<String, String> id) {
        CometChatUIKitHelper.hidePanel(id, UIKitConstants.CustomUIPosition.MESSAGE_LIST_BOTTOM);
    }

    public AIConversationSummaryDecorator(DataSource dataSource, AIConversationSummaryConfiguration configuration) {
        super(dataSource);
        this.configuration = configuration;
        addListeners();
    }

    @Override
    public List<CometChatMessageComposerAction> getAIOptions(
        Context context, User user, Group group, HashMap<String, String> idMap, AIOptionsStyle aiOptionsStyle, AdditionParameter additionParameter
    ) {
        List<CometChatMessageComposerAction> actions = super.getAIOptions(context, user, group, idMap, aiOptionsStyle, additionParameter);
        id = idMap;
        if (!idMap.containsKey(UIKitConstants.MapId.PARENT_MESSAGE_ID)) {
            String title = context.getString(R.string.cometchat_generate_summary);
            CometChatMessageComposerAction action = new CometChatMessageComposerAction()
                .setId(getId())
                .setTitle(title)
                .setTitleColor(CometChatTheme.getTextColorPrimary(context))
                .setTitleAppearance(CometChatTheme.getTextAppearanceBodyRegular(context))
                .setBackground(CometChatTheme.getBackgroundColor1(context))
                .setIconTintColor(CometChatTheme.getIconTintHighlight(context))
                .setIcon(configuration != null ? configuration.getButtonIcon() : R.drawable.cometchat_ic_ai_conversation_summary)
                .setOnClick(() -> showSummaryPanel(
                    idMap,
                    UIKitConstants.CustomUIPosition.MESSAGE_LIST_BOTTOM,
                    configuration != null ? configuration.getStyle() : 0,
                    user,
                    group
                ));

            if (aiOptionsStyle != null) {
                if (aiOptionsStyle.getListItemBackgroundColor() != 0) action.setBackground(aiOptionsStyle.getListItemBackgroundColor());
                if (aiOptionsStyle.getListItemTextAppearance() != 0) action.setTitleAppearance(aiOptionsStyle.getListItemTextAppearance());
                if (aiOptionsStyle.getCornerRadius() >= 0) action.setCornerRadius((int) aiOptionsStyle.getCornerRadius());
                if (aiOptionsStyle.getListItemTextColor() != 0) action.setTitleColor(aiOptionsStyle.getListItemTextColor());
            }
            actions.add(action);
        }
        return actions;
    }

    @NonNull
    @Override
    public String getId() {
        return AIConversationSummaryDecorator.class.getSimpleName();
    }
}
