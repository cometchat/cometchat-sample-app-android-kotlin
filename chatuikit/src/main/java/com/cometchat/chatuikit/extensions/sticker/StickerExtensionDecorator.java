package com.cometchat.chatuikit.extensions.sticker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Conversation;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.extensions.ExtensionConstants;
import com.cometchat.chatuikit.extensions.Extensions;
import com.cometchat.chatuikit.extensions.sticker.bubble.CometChatStickerBubble;
import com.cometchat.chatuikit.extensions.sticker.keyboard.CometChatStickerKeyboard;
import com.cometchat.chatuikit.extensions.sticker.keyboard.StickerKeyboardConfiguration;
import com.cometchat.chatuikit.extensions.sticker.keyboard.model.Sticker;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.framework.ChatConfigurator;
import com.cometchat.chatuikit.shared.framework.DataSource;
import com.cometchat.chatuikit.shared.framework.DataSourceDecorator;
import com.cometchat.chatuikit.shared.models.AdditionParameter;
import com.cometchat.chatuikit.shared.models.CometChatMessageTemplate;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.utils.MessageBubbleUtils;
import com.cometchat.chatuikit.shared.viewholders.MessagesViewHolderListener;
import com.cometchat.chatuikit.shared.views.deletebubble.CometChatDeleteBubble;
import com.cometchat.chatuikit.shared.views.messagebubble.CometChatMessageBubble;
import com.cometchat.chatuikit.shared.views.reaction.ExtensionResponseListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class StickerExtensionDecorator extends DataSourceDecorator {
    private static final String TAG = StickerExtensionDecorator.class.getSimpleName();


    private final String stickerTypeConstant = ExtensionConstants.ExtensionType.STICKER;
    private StickerKeyboardConfiguration configuration;
    private boolean isKeyboardVisible = false;

    public StickerExtensionDecorator(DataSource dataSource) {
        super(dataSource);
    }

    public StickerExtensionDecorator(DataSource dataSource, StickerKeyboardConfiguration configuration) {
        super(dataSource);
        this.configuration = configuration;
    }

    @Override
    public List<CometChatMessageTemplate> getMessageTemplates(AdditionParameter additionParameter) {
        List<CometChatMessageTemplate> templates = super.getMessageTemplates(additionParameter);
        if (templates != null && !templates.contains(getStickerTemplate(additionParameter)))
            templates.add(getStickerTemplate(additionParameter));
        return templates;
    }

    @Override
    public View getAuxiliaryOption(Context context, User user, Group group, HashMap<String, String> id, AdditionParameter additionParameter) {
        if (additionParameter != null && additionParameter.getStickersButtonVisibility() == View.VISIBLE) {
            LinearLayout layout = new LinearLayout(context);
            View view = super.getAuxiliaryOption(context, user, group, id, additionParameter);
            Utils.handleView(layout, view, false);
            layout.addView(getStickerIcon(context, id, user, group, additionParameter));
            return layout;
        } else {
            return super.getAuxiliaryOption(context, user, group, id, additionParameter);
        }
    }

    @Override
    public List<String> getDefaultMessageTypes(AdditionParameter additionParameter) {
        List<String> types = super.getDefaultMessageTypes(additionParameter);
        if (!types.contains(stickerTypeConstant)) {
            types.add(stickerTypeConstant);
        }
        return types;
    }

    @Override
    public List<String> getDefaultMessageCategories(AdditionParameter additionParameter) {
        List<String> categories = super.getDefaultMessageCategories(additionParameter);
        if (!categories.contains(UIKitConstants.MessageCategory.CUSTOM))
            categories.add(UIKitConstants.MessageCategory.CUSTOM);
        return categories;
    }

    @Override
    public SpannableString getLastConversationMessage(Context context, @Nullable Conversation conversation, AdditionParameter additionParameter) {
        if (conversation != null && conversation.getLastMessage() != null && (UIKitConstants.MessageCategory.CUSTOM.equals(conversation
                                                                                                                               .getLastMessage()
                                                                                                                               .getCategory()) && ExtensionConstants.ExtensionType.STICKER.equalsIgnoreCase(
            conversation.getLastMessage().getType())))
            return SpannableString.valueOf(getLastConversationMessage_(context, conversation, additionParameter));
        else return super.getLastConversationMessage(context, conversation, additionParameter);
    }

    public String getLastConversationMessage_(Context context, Conversation conversation, AdditionParameter additionParameter) {
        String lastMessageText;
        BaseMessage baseMessage = conversation.getLastMessage();
        if (baseMessage != null) {
            String message = getLastMessage(context, baseMessage);
            if (message != null) {
                lastMessageText = message;
            } else
                lastMessageText = String.valueOf(super.getLastConversationMessage(context, conversation, additionParameter));
            if (baseMessage.getDeletedAt() > 0) {
                lastMessageText = context.getString(R.string.cometchat_this_message_deleted);
            }
        } else {
            lastMessageText = context.getResources().getString(R.string.cometchat_start_conv_hint);
        }
        return lastMessageText;
    }

    public String getLastMessage(Context context, BaseMessage lastMessage) {
        String message = null;
        if (UIKitConstants.MessageCategory.CUSTOM.equals(lastMessage.getCategory()) && ExtensionConstants.ExtensionType.STICKER.equalsIgnoreCase(
            lastMessage.getType()))
            message = Utils.getMessagePrefix(lastMessage, context) + context.getString(R.string.cometchat_sticker_uppercase);
        return message;
    }

    public View getStickerIcon(Context context, HashMap<String, String> mapId, User user, Group group, AdditionParameter additionParameter) {
        View view = View.inflate(context, R.layout.cometchat_sticker_extension_button, null);
        ImageView stickerImage = view.findViewById(R.id.iv_sticker);
        ImageView activeStickerImage = view.findViewById(R.id.iv_filled_sticker);
        activeStickerImage.setImageDrawable(additionParameter.getActiveStickerIcon());
        activeStickerImage.setImageTintList(ColorStateList.valueOf(additionParameter.getActiveAuxiliaryIconTint()));
        if (configuration != null && configuration.getStickerIcon() != 0)
            stickerImage.setImageResource(configuration.getStickerIcon());
        else {
            stickerImage.setImageDrawable(additionParameter.getInactiveStickerIcon());
            stickerImage.setImageTintList(ColorStateList.valueOf(additionParameter.getInactiveAuxiliaryIconTint()));
        }

        boolean[] delayedKeyboardShow = {false};

        ViewTreeObserver.OnGlobalFocusChangeListener focusChangeListener = (oldFocus, newFocus) -> {
            // This is called when the focus changes
            // Check if the new focus is not null and is CometChatEditText
            if (newFocus != null && newFocus.getId() == R.id.cometchat_compose_box) {
                if (!delayedKeyboardShow[0]) {
                    delayedKeyboardShow[0] = true;

                    newFocus.clearFocus(); // Prevent the keyboard from showing immediately
                    CometChatUIKitHelper.hidePanel(mapId, UIKitConstants.CustomUIPosition.COMPOSER_BOTTOM); // Hide the sticker keyboard
                    activeStickerImage.setVisibility(View.GONE);
                    stickerImage.setVisibility(View.VISIBLE);

                    // Request focus and show the keyboard after a delay
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        newFocus.requestFocus();
                        Utils.showKeyBoard(context, newFocus);
                        delayedKeyboardShow[0] = false;
                    }, 350);
                }
            }
        };

        ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalFocusChangeListener(focusChangeListener);

        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                // No-op
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
                // Remove the focus change listener when the view is detached
                if (v.getViewTreeObserver().isAlive()) {
                    v.getViewTreeObserver().removeOnGlobalFocusChangeListener(focusChangeListener);
                }
            }
        });

        stickerImage.setOnClickListener(view1 -> {
            Utils.hideKeyBoard(context, view1);
            CometChatStickerKeyboard stickerKeyboard = getStickerKeyboard(context,
                                                                          activeStickerImage,
                                                                          stickerImage,
                                                                          user,
                                                                          group,
                                                                          mapId,
                                                                          configuration);
            CometChatUIKitHelper.showPanel(mapId, UIKitConstants.CustomUIPosition.COMPOSER_BOTTOM, var1 -> stickerKeyboard);
        });

        activeStickerImage.setOnClickListener(v -> CometChatUIKitHelper.hidePanel(mapId, UIKitConstants.CustomUIPosition.COMPOSER_BOTTOM));
        return view;
    }

    public CometChatStickerKeyboard getStickerKeyboard(Context context,
                                                       ImageView activeStickerImage,
                                                       ImageView stickerImage,
                                                       User user,
                                                       Group group,
                                                       HashMap<String, String> idMap,
                                                       StickerKeyboardConfiguration configuration) {
        CometChatStickerKeyboard cometchatStickerKeyboard = new CometChatStickerKeyboard(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                         context.getResources().getDimensionPixelSize(R.dimen.cometchat_296dp));
        cometchatStickerKeyboard.setLayoutParams(layoutParams);
        if (configuration != null) {
            cometchatStickerKeyboard.setStyle(configuration.getStyle());
            cometchatStickerKeyboard.setEmptyStateView(configuration.getEmptyStateView());
            cometchatStickerKeyboard.setErrorStateView(configuration.getErrorStateView());
            cometchatStickerKeyboard.setLoadingStateView(configuration.getLoadingStateView());
            cometchatStickerKeyboard.setErrorStateText(configuration.getErrorStateText());
        }
        cometchatStickerKeyboard.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                activeStickerImage.setVisibility(View.VISIBLE);
                stickerImage.setVisibility(View.GONE);
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
                activeStickerImage.setVisibility(View.GONE);
                stickerImage.setVisibility(View.VISIBLE);
            }
        });

        cometchatStickerKeyboard.setState(UIKitConstants.States.LOADING);
        Extensions.fetchStickers(new ExtensionResponseListener() {
            @Override
            public void OnResponseSuccess(Object var) {
                cometchatStickerKeyboard.setState(UIKitConstants.States.LOADED);
                JSONObject stickersJSON = (JSONObject) var;
                HashMap<String, List<Sticker>> stringListHashMap = Extensions.extractStickersFromJSON(stickersJSON);
                if (!stringListHashMap.isEmpty()) {
                    cometchatStickerKeyboard.setState(UIKitConstants.States.NON_EMPTY);
                    cometchatStickerKeyboard.setData(Extensions.extractStickersFromJSON(stickersJSON));
                } else cometchatStickerKeyboard.setState(UIKitConstants.States.EMPTY);
            }

            @Override
            public void OnResponseFailed(CometChatException e) {
                cometchatStickerKeyboard.setState(UIKitConstants.States.ERROR);
            }
        });
        cometchatStickerKeyboard.setStickerClickListener(sticker -> {
            JSONObject stickerData = new JSONObject();
            JSONObject jsonObject = new JSONObject();
            String pushNotificationMessage = context.getString(R.string.cometchat_shared_sticker);
            try {
                stickerData.put("sticker_url", sticker.getUrl());
                stickerData.put("sticker_name", sticker.getName());
                jsonObject.put("incrementUnreadCount", true);
                jsonObject.put("pushNotification", pushNotificationMessage);
            } catch (Exception e) {
                CometChatLogger.e(TAG, e.toString());
            }
            String id = "";
            String receiverType = "";
            if (user != null) {
                id = user.getUid();
                receiverType = UIKitConstants.ReceiverType.USER;
            } else if (group != null) {
                id = group.getGuid();
                receiverType = UIKitConstants.ReceiverType.GROUP;
            }
            CustomMessage customMessage = new CustomMessage(id, receiverType, stickerTypeConstant, stickerData);
            customMessage.shouldUpdateConversation(true);
            if (idMap.containsKey(UIKitConstants.MapId.PARENT_MESSAGE_ID)) {
                customMessage.setParentMessageId(Integer.parseInt(idMap.get(UIKitConstants.MapId.PARENT_MESSAGE_ID)));
            }
            customMessage.setMetadata(jsonObject);
            CometChatUIKit.sendCustomMessage(customMessage, null);
        });

        return cometchatStickerKeyboard;
    }

    public CometChatMessageTemplate getStickerTemplate(AdditionParameter additionParameter) {
        return new CometChatMessageTemplate()
            .setCategory(UIKitConstants.MessageCategory.CUSTOM)
            .setType(stickerTypeConstant)
            .setOptions((context, baseMessage, isLeftAlign) -> ChatConfigurator
                .getDataSource()
                .getCommonOptions(context, baseMessage, isLeftAlign, additionParameter))
            .setContentView(new MessagesViewHolderListener() {
                @Override
                public View createView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
                    View view = View.inflate(context, R.layout.cometchat_sticker_bubble_layout_container, null);
                    MessageBubbleUtils.setDeletedMessageBubble(context, view);
                    return view;
                }

                @Override
                public void bindView(Context context,
                                     @NonNull View createdView,
                                     BaseMessage message,
                                     UIKitConstants.MessageBubbleAlignment alignment,
                                     RecyclerView.ViewHolder holder,
                                     List<BaseMessage> messageList,
                                     int position) {
                    CometChatStickerBubble stickerBubble = createdView.findViewById(R.id.cometchat_sticker_bubble);
                    CometChatDeleteBubble deletedBubble = createdView.findViewById(R.id.cometchat_delete_text_bubble);

                    if (message.getDeletedAt() == 0) {
                        deletedBubble.setVisibility(View.GONE);
                        stickerBubble.setVisibility(View.VISIBLE);
                        stickerBubble.setMessage((CustomMessage) message);
                    } else {
                        stickerBubble.setVisibility(View.GONE);
                        deletedBubble.setVisibility(View.VISIBLE);
                        deletedBubble.setStyle(CometChatUIKit
                                                   .getLoggedInUser()
                                                   .getUid()
                                                   .equals(message
                                                               .getSender()
                                                               .getUid()) ? additionParameter.getOutgoingDeleteBubbleStyle() : additionParameter.getIncomingDeleteBubbleStyle());
                    }
                }
            })
            .setBottomView(new MessagesViewHolderListener() {
                @Override
                public View createView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
                    return CometChatUIKit.getDataSource().getBottomView(context, messageBubble, alignment);
                }

                @Override
                public void bindView(Context context,
                                     View createdView,
                                     BaseMessage message,
                                     UIKitConstants.MessageBubbleAlignment alignment,
                                     RecyclerView.ViewHolder holder,
                                     List<BaseMessage> messageList,
                                     int position) {
                    CometChatUIKit
                        .getDataSource()
                        .bindBottomView(context, createdView, message, alignment, holder, messageList, position, additionParameter);
                }
            });
    }

    @Override
    public String getId() {
        return StickerExtensionDecorator.class.getSimpleName();
    }
}
