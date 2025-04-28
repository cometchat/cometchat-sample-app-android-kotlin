package com.cometchat.chatuikit.shared.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.StyleRes;
import androidx.core.content.res.ResourcesCompat;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.models.Action;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Conversation;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.InteractiveMessage;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chat.models.TypingIndicator;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.calls.utils.CallUtils;
import com.cometchat.chatuikit.extensions.ExtensionConstants;
import com.cometchat.chatuikit.extensions.Extensions;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.formatters.CometChatTextFormatter;
import com.cometchat.chatuikit.shared.formatters.FormatterUtils;
import com.cometchat.chatuikit.shared.interfaces.DateTimeFormatterCallback;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.views.date.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ConversationsUtils {
    private static final String TAG = ConversationsUtils.class.getSimpleName();
    private static final HashMap<MessageType, Integer> lastMessageTypeAndIconHashMap = new HashMap<MessageType, Integer>() {
        {
            put(MessageType.PHOTO, R.drawable.cometchat_ic_conversations_photo);
            put(MessageType.VIDEO, R.drawable.cometchat_ic_conversations_video);
            put(MessageType.AUDIO, R.drawable.cometchat_ic_conversations_audio);
            put(MessageType.DOCUMENT, R.drawable.cometchat_ic_conversations_document);
            put(MessageType.DELETED_MESSAGE, R.drawable.cometchat_ic_conversations_deleted_message);
            put(MessageType.NOT_SUPPORTED, R.drawable.cometchat_ic_conversations_deleted_message);
            put(MessageType.INCOMING_VOICE_CALL, R.drawable.cometchat_ic_conversations_incoming_voice_call);
            put(MessageType.OUTGOING_VOICE_CALL, R.drawable.cometchat_ic_conversations_outgoing_voice_call);
            put(MessageType.INCOMING_VIDEO_CALL, R.drawable.cometchat_ic_conversations_incoming_video_call);
            put(MessageType.OUTGOING_VIDEO_CALL, R.drawable.cometchat_ic_conversations_outgoing_video_call);
            put(MessageType.MISSED_VIDEO_CALL, R.drawable.cometchat_ic_conversations_missed_video_call);
            put(MessageType.MISSED_VOICE_CALL, R.drawable.cometchat_ic_conversations_missed_voice_call);
            put(MessageType.STICKER, R.drawable.cometchat_ic_conversations_stricker);
            put(MessageType.GIF, R.drawable.cometchat_ic_conversations_gif);
            put(MessageType.LINK, R.drawable.cometchat_ic_conversations_link);
            put(MessageType.POLL, R.drawable.cometchat_ic_conversations_poll);
            put(MessageType.LOCATION, R.drawable.cometchat_ic_conversations_location);
            put(MessageType.THREAD, R.drawable.cometchat_ic_conversations_thread);
            put(MessageType.COLLABORATIVE_DOCUMENT, R.drawable.cometchat_ic_conversations_collabrative_document);
            put(MessageType.COLLABORATIVE_WHITEBOARD, R.drawable.cometchat_ic_conversations_collaborative_whiteboard);
        }
    };

    public static String getConversationTitle(Conversation conversation) {
        if (UIKitConstants.ConversationType.USERS.equals(conversation.getConversationType())) {
            return ((User) conversation.getConversationWith()).getName();
        } else {
            return ((Group) conversation.getConversationWith()).getName();
        }
    }

    public static String getConversationAvatar(Conversation conversation) {
        if (UIKitConstants.ConversationType.USERS.equals(conversation.getConversationType())) {
            return ((User) conversation.getConversationWith()).getAvatar();
        } else {
            return ((Group) conversation.getConversationWith()).getIcon();
        }
    }

    public static ConversationTailView getConversationTailViewContainer(Context context) {
        return new ConversationTailView(context);
    }

    public static void bindConversationTailView(ConversationTailView conversationTailView,
                                                SimpleDateFormat dateFormat,
                                                DateTimeFormatterCallback dateTimeFormatterCallback,
                                                Conversation conversation,
                                                @StyleRes int badgeStyle,
                                                @StyleRes int dateStyle) {
        conversationTailView.getBadge().setCount(conversation.getUnreadMessageCount());
        conversationTailView.getBadge().setVisibility(conversation.getUnreadMessageCount() != 0 ? View.VISIBLE : View.GONE);
        conversationTailView.getDate().setDateFormat(dateFormat);
        conversationTailView.getDate().setDateTimeFormatterCallback(dateTimeFormatterCallback == null ? CometChatUIKit.getAuthSettings()
                                                                                                                      .getDateTimeFormatterCallback() : dateTimeFormatterCallback);
        conversationTailView.getDate().setDate(conversation.getUpdatedAt(), Pattern.DAY_DATE_TIME);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                                                               LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.END;
        conversationTailView.getBadge().setStyle(badgeStyle);
        conversationTailView.getDate().setStyle(dateStyle);
        conversationTailView.setLayoutParams(layoutParams);
    }

    public static SubtitleView getSubtitleViewContainer(Context context) {
        SubtitleView subtitleView = new SubtitleView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                               LinearLayout.LayoutParams.WRAP_CONTENT);
        subtitleView.setLayoutParams(layoutParams);
        return subtitleView;
    }

    public static void bindSubtitleView(Context context,
                                        SubtitleView subtitleView,
                                        Conversation conversation,
                                        HashMap<Conversation, TypingIndicator> typingIndicatorHashMap,
                                        boolean disableReadReceipt,
                                        List<CometChatTextFormatter> formatters,
                                        @StyleRes int conversationsItemSubtitleTextAppearance,
                                        @ColorInt int conversationsItemSubtitleTextColor,
                                        @ColorInt int conversationsItemMessageTypeIconTint,
                                        @StyleRes int conversationsReceiptStyle,
                                        @StyleRes int conversationsTypingIndicatorStyle) {
        // Handle Typing Indicator
        if (!typingIndicatorHashMap.isEmpty() && typingIndicatorHashMap.containsKey(conversation)) {
            TypingIndicator typingIndicator = typingIndicatorHashMap.get(conversation);
            if (typingIndicator != null) {
                try (TypedArray typedArray = context
                    .getTheme()
                    .obtainStyledAttributes(conversationsTypingIndicatorStyle, R.styleable.CometChatTypingIndicator)) {
                    subtitleView.setTypingIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatTypingIndicator_cometchatTypingIndicatorTextAppearance,
                                                                                           0));
                    subtitleView.setTypingIndicatorTextColor(typedArray.getColor(R.styleable.CometChatTypingIndicator_cometchatTypingIndicatorTextColor,
                                                                                 0));
                }
                if (typingIndicator.getReceiverType().equalsIgnoreCase(CometChatConstants.RECEIVER_TYPE_USER)) {
                    subtitleView.setTypingIndicatorText(context.getString(R.string.cometchat_typing));
                } else {
                    subtitleView.setTypingIndicatorText(typingIndicator
                                                            .getSender()
                                                            .getName() + " " + context.getString(R.string.cometchat_is_typing));
                }
                subtitleView.showTypingIndicator(true);
                subtitleView.showSubtitleViewContainer(false);
            } else {
                subtitleView.showTypingIndicator(false);
                subtitleView.showSubtitleViewContainer(true);
            }
        } else {
            subtitleView.showTypingIndicator(false);
            subtitleView.showSubtitleViewContainer(true);
        }
        // Sets the message receipt icons
        subtitleView.getCometChatMessageReceipt().setStyle(conversationsReceiptStyle);
        // Update the message receipt icons as per the message status
        subtitleView.setMessageReceiptIcon(MessageReceiptUtils.MessageReceipt(conversation.getLastMessage()));
        // Hide the message receipt icons if the feature is disabled
        if (!disableReadReceipt) {
            // Hide the message receipt icons unless the last message was sent by me
            subtitleView.hideMessageReceiptIcon(MessageReceiptUtils.hideReceipt(conversation.getLastMessage()));
        } else {
            // Hide the message receipt icons if the feature is disabled
            subtitleView.hideMessageReceiptIcon(true);
        }
        // Handle Last Message Type Icon
        LastMessageData lastMessageData = getLastMessageData(context, conversation);
        if (conversation.getLastMessage() != null && conversation.getLastMessage().getParentMessageId() != 0) {
            lastMessageData.setMessageType(MessageType.THREAD);
        }
        subtitleView.setMessageTypeIcon(getIconDrawableRes(context, lastMessageData.getMessageType()));

        if (lastMessageData.getSender() != null && !lastMessageData.getSender().isEmpty()) {
            subtitleView.setSenderNameText(lastMessageData.getSender());
            subtitleView.setSenderNameTextAppearance(conversationsItemSubtitleTextAppearance);
            subtitleView.setSenderNameTextColor(conversationsItemSubtitleTextColor);
            subtitleView.hideSenderName(false);
        } else {
            subtitleView.hideSenderName(true);
        }

        subtitleView.setMessageTypeIconTint(conversationsItemMessageTypeIconTint);
        subtitleView.showMessageTypeIconView(lastMessageData.getMessageType() != MessageType.DEFAULT);

        // Set the last message text
        subtitleView.setLastMessageTextAppearance(conversationsItemSubtitleTextAppearance);
        subtitleView.setLastMessageTextColor(conversationsItemSubtitleTextColor);
        if (lastMessageData.messageType == MessageType.NOT_SUPPORTED || lastMessageData.messageType == MessageType.DELETED_MESSAGE) {
            subtitleView.setLastMessageText(lastMessageData.lastMessage);
        } else {
            SpannableString spannableString = SpannableString.valueOf(FormatterUtils.getFormattedText(context,
                                                                                                      conversation.getLastMessage(),
                                                                                                      UIKitConstants.FormattingType.CONVERSATIONS,
                                                                                                      null,
                                                                                                      lastMessageData.getLastMessage(),
                                                                                                      formatters != null && !formatters.isEmpty() ? formatters : new ArrayList<>()));
            subtitleView.setLastMessageText(spannableString);
        }
    }

    private static LastMessageData getLastMessageData(Context context, Conversation conversation) {
        BaseMessage lastMessage = conversation.getLastMessage();
        if (lastMessage == null) {
            return new LastMessageData(MessageType.DEFAULT, "", context.getString(R.string.cometchat_start_conv_hint));
        } else if (lastMessage instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) lastMessage;
            if (textMessage.getText() != null && !textMessage.getText().isEmpty() && textMessage.getDeletedAt() == 0) {
                JSONObject metadata = textMessage.getMetadata();
                if (metadata != null && checkForLinks(metadata.toString())) {
                    return new LastMessageData(MessageType.LINK,
                                               Utils.getMessagePrefix(lastMessage, context),
                                               checkProfanityMessageAndDataMasking(context, textMessage));
                }
                return new LastMessageData(MessageType.DEFAULT,
                                           Utils.getMessagePrefix(lastMessage, context),
                                           checkProfanityMessageAndDataMasking(context, textMessage));
            } else {
                return new LastMessageData(MessageType.DELETED_MESSAGE,
                                           Utils.getMessagePrefix(lastMessage, context),
                                           context.getString(R.string.cometchat_this_message_deleted));
            }
        } else if (lastMessage instanceof MediaMessage) {
            if (lastMessage.getDeletedAt() == 0) {
                if (lastMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_IMAGE)) {
                    MediaMessage mediaMessage = (MediaMessage) lastMessage;
                    if (mediaMessage.getAttachment() != null && mediaMessage.getAttachment().toString().contains(".gif")) {
                        return new LastMessageData(MessageType.GIF,
                                                   Utils.getMessagePrefix(lastMessage, context),
                                                   context.getString(R.string.cometchat_message_gif));
                    } else {
                        return new LastMessageData(MessageType.PHOTO,
                                                   Utils.getMessagePrefix(lastMessage, context),
                                                   context.getString(R.string.cometchat_message_image));
                    }
                } else if (lastMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_VIDEO)) {
                    return new LastMessageData(MessageType.VIDEO,
                                               Utils.getMessagePrefix(lastMessage, context),
                                               context.getString(R.string.cometchat_message_video));
                } else if (lastMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_FILE)) {
                    return new LastMessageData(MessageType.DOCUMENT,
                                               Utils.getMessagePrefix(lastMessage, context),
                                               context.getString(R.string.cometchat_message_document));
                } else if (lastMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_AUDIO)) {
                    return new LastMessageData(MessageType.AUDIO,
                                               Utils.getMessagePrefix(lastMessage, context),
                                               context.getString(R.string.cometchat_message_audio));
                }
            } else {
                return new LastMessageData(MessageType.DELETED_MESSAGE, "", context.getString(R.string.cometchat_this_message_deleted));
            }
        } else if (lastMessage instanceof Call) {
            Call call = (Call) lastMessage;
            String callMessageText = CallUtils.getCallStatus(context, call);
            if (CallUtils.isVideoCall(call)) {
                if (CallUtils.isCallInitiatedByMe(call)) {
                    return new LastMessageData(MessageType.OUTGOING_VIDEO_CALL, CallUtils.getCallerName(context, call, true), callMessageText);
                } else {
                    if (call.getCallStatus().equals(UIKitConstants.CallStatusConstants.UNANSWERED) || call
                        .getCallStatus()
                        .equals(UIKitConstants.CallStatusConstants.CANCELLED)) {
                        return new LastMessageData(MessageType.MISSED_VIDEO_CALL, CallUtils.getCallerName(context, call, true), callMessageText);
                    } else {
                        return new LastMessageData(MessageType.INCOMING_VIDEO_CALL, CallUtils.getCallerName(context, call, true), callMessageText);
                    }
                }
            } else {
                if (CallUtils.isCallInitiatedByMe(call)) {
                    return new LastMessageData(MessageType.OUTGOING_VOICE_CALL, CallUtils.getCallerName(context, call, true), callMessageText);
                } else {
                    if (call.getCallStatus().equals(UIKitConstants.CallStatusConstants.UNANSWERED) || call
                        .getCallStatus()
                        .equals(UIKitConstants.CallStatusConstants.CANCELLED)) {
                        return new LastMessageData(MessageType.MISSED_VOICE_CALL, CallUtils.getCallerName(context, call, true), callMessageText);
                    } else {
                        return new LastMessageData(MessageType.INCOMING_VOICE_CALL, CallUtils.getCallerName(context, call, true), callMessageText);
                    }
                }
            }
        } else if (lastMessage instanceof CustomMessage) {
            if (lastMessage.getDeletedAt() == 0) {
                // Use switch-case for message type
                switch (lastMessage.getType()) {
                    case ExtensionConstants.ExtensionType.EXTENSION_POLL:
                        return new LastMessageData(MessageType.POLL,
                                                   Utils.getMessagePrefix(lastMessage, context),
                                                   context.getString(R.string.cometchat_message_poll));
                    case ExtensionConstants.ExtensionType.STICKER:
                        return new LastMessageData(MessageType.STICKER,
                                                   Utils.getMessagePrefix(lastMessage, context),
                                                   context.getString(R.string.cometchat_message_sticker));
                    case ExtensionConstants.ExtensionType.LOCATION:
                        return new LastMessageData(MessageType.LOCATION,
                                                   Utils.getMessagePrefix(lastMessage, context),
                                                   context.getString(R.string.cometchat_message_location));
                    case ExtensionConstants.ExtensionType.DOCUMENT:
                        return new LastMessageData(MessageType.COLLABORATIVE_DOCUMENT,
                                                   Utils.getMessagePrefix(lastMessage, context),
                                                   context.getString(R.string.cometchat_message_collaborative_document));
                    case ExtensionConstants.ExtensionType.WHITEBOARD:
                        return new LastMessageData(MessageType.COLLABORATIVE_WHITEBOARD,
                                                   Utils.getMessagePrefix(lastMessage, context),
                                                   context.getString(R.string.cometchat_message_collaborative_whiteboard));
                    case ExtensionConstants.ExtensionType.MEETING:
                        String senderName = Utils.getMessagePrefix(lastMessage, context);
                        senderName = senderName.substring(0, senderName.indexOf(":"));
                        if (senderName.equals(context.getString(R.string.cometchat_you))) {
                            return new LastMessageData(MessageType.DEFAULT,
                                                       "",
                                                       String.format(Locale.US,
                                                                     context.getString(R.string.cometchat_meeting_initiated_by_you),
                                                                     senderName));
                        } else {
                            return new LastMessageData(MessageType.DEFAULT,
                                                       "",
                                                       String.format(Locale.US,
                                                                     context.getString(R.string.cometchat_meeting_initiated_by_others),
                                                                     senderName));
                        }
                    default:
                        if (lastMessage.getMetadata() != null && lastMessage.getMetadata().has("pushNotification")) {
                            try {
                                return new LastMessageData(MessageType.DEFAULT,
                                                           Utils.getMessagePrefix(lastMessage, context),
                                                           lastMessage.getMetadata().getString("pushNotification"));
                            } catch (Exception ignored) {
                                return new LastMessageData(MessageType.DELETED_MESSAGE,
                                                           Utils.getMessagePrefix(lastMessage, context),
                                                           context.getString(R.string.cometchat_this_message_deleted));
                            }
                        } else {
                            return new LastMessageData(MessageType.DEFAULT, Utils.getMessagePrefix(lastMessage, context), lastMessage.getType());
                        }
                }
            } else {
                return new LastMessageData(MessageType.DELETED_MESSAGE,
                                           Utils.getMessagePrefix(lastMessage, context),
                                           context.getString(R.string.cometchat_this_message_deleted));
            }
        } else if (lastMessage instanceof InteractiveMessage) {
            return new LastMessageData(MessageType.NOT_SUPPORTED,
                                       Utils.getMessagePrefix(lastMessage, context),
                                       context.getString(R.string.cometchat_this_message_type_is_not_supported));
        } else if (lastMessage instanceof Action) {
            return getActionMessage(context, lastMessage);
        }
        return new LastMessageData(MessageType.DEFAULT, "", context.getString(R.string.cometchat_start_conv_hint));
    }

    private static Drawable getIconDrawableRes(Context context, MessageType key) {
        try {
            Integer iconResId = lastMessageTypeAndIconHashMap.get(key);
            if (iconResId != null) {
                return ResourcesCompat.getDrawable(context.getResources(), iconResId, null);
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
        return null;
    }

    private static boolean checkForLinks(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject injected = jsonObject.optJSONObject("@injected");
            if (injected != null) {
                JSONObject extensions = injected.optJSONObject("extensions");
                if (extensions != null) {
                    JSONObject linkPreview = extensions.optJSONObject("link-preview");
                    if (linkPreview != null) {
                        JSONArray links = linkPreview.optJSONArray("links");
                        return links != null && links.length() > 0;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static String checkProfanityMessageAndDataMasking(Context context, TextMessage textMessage) {
        String text = Extensions.checkProfanityMessage(context, textMessage);
        if (text.equalsIgnoreCase(textMessage.getText())) text = Extensions.checkDataMasking(context, textMessage);
        return text;
    }

    private static LastMessageData getActionMessage(Context context, BaseMessage baseMessage) {
        Action action = (Action) baseMessage;
        LastMessageData lastMessageData = null;
        if (action.getType().equalsIgnoreCase(CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER)) {
            switch (action.getAction()) {
                case CometChatConstants.ActionKeys.ACTION_JOINED: {
                    User actionBy = (User) action.getActionBy();
                    lastMessageData = new LastMessageData(MessageType.DEFAULT,
                                                          "",
                                                          actionBy.getName() + " " + context.getString(R.string.cometchat_joined));
                    break;
                }
                case CometChatConstants.ActionKeys.ACTION_LEFT: {
                    User actionBy = (User) action.getActionBy();
                    lastMessageData = new LastMessageData(MessageType.DEFAULT,
                                                          "",
                                                          actionBy.getName() + " " + context.getString(R.string.cometchat_left));
                    break;
                }
                case CometChatConstants.ActionKeys.ACTION_KICKED: {
                    User actionBy = (User) action.getActionBy();
                    User actionOn = (User) action.getActionOn();
                    lastMessageData = new LastMessageData(MessageType.DEFAULT,
                                                          "",
                                                          actionBy.getName() + " " + context.getString(R.string.cometchat_kicked_by) + " " + actionOn.getName());
                    break;
                }
                case CometChatConstants.ActionKeys.ACTION_BANNED: {
                    User actionBy = (User) action.getActionBy();
                    User actionOn = (User) action.getActionOn();
                    lastMessageData = new LastMessageData(MessageType.DEFAULT,
                                                          "",
                                                          actionBy.getName() + " " + context.getString(R.string.cometchat_banned) + " " + actionOn.getName());
                    break;
                }
                case CometChatConstants.ActionKeys.ACTION_UNBANNED: {
                    User actionBy = (User) action.getActionBy();
                    User actionOn = (User) action.getActionOn();
                    lastMessageData = new LastMessageData(MessageType.DEFAULT,
                                                          "",
                                                          actionBy.getName() + " " + context.getString(R.string.cometchat_unban) + " " + actionOn.getName());
                    break;
                }
                case CometChatConstants.ActionKeys.ACTION_MEMBER_ADDED: {
                    User actionBy = (User) action.getActionBy();
                    User actionOn = (User) action.getActionOn();
                    lastMessageData = new LastMessageData(MessageType.DEFAULT,
                                                          "",
                                                          actionBy.getName() + " " + context.getString(R.string.cometchat_added) + " " + actionOn.getName());
                    break;
                }
                case CometChatConstants.ActionKeys.ACTION_SCOPE_CHANGED: {
                    User actionBy = (User) action.getActionBy();
                    User actionOn = (User) action.getActionOn();
                    String newScope = action.getNewScope();
                    String actionMessage;
                    switch (newScope.toLowerCase()) {
                        case CometChatConstants.SCOPE_MODERATOR:
                            actionMessage = actionBy.getName() + " " + context.getString(R.string.cometchat_made) + " " + actionOn.getName() + " " + context.getString(
                                R.string.cometchat_moderator);
                            break;
                        case CometChatConstants.SCOPE_ADMIN:
                            actionMessage = actionBy.getName() + " " + context.getString(R.string.cometchat_made) + " " + actionOn.getName() + " " + context.getString(
                                R.string.cometchat_admin);
                            break;
                        case CometChatConstants.SCOPE_PARTICIPANT:
                            actionMessage = actionBy.getName() + " " + context.getString(R.string.cometchat_made) + " " + actionOn.getName() + " " + context.getString(
                                R.string.cometchat_participant);
                            break;
                        default:
                            actionMessage = action.getMessage();
                            break;
                    }
                    lastMessageData = new LastMessageData(MessageType.DEFAULT,
                                                          "",
                                                          actionMessage);
                    break;
                }
                default: {
                    lastMessageData = new LastMessageData(MessageType.DEFAULT, "", context.getString(R.string.cometchat_this_message_deleted));
                    break;
                }
            }
        } else if (action.getType().equalsIgnoreCase(CometChatConstants.ActionKeys.ACTION_TYPE_MESSAGE)) {
            switch (action.getAction()) {
                case CometChatConstants.ActionKeys.ACTION_MESSAGE_EDITED:
                    lastMessageData = new LastMessageData(MessageType.DEFAULT, "", CometChatConstants.ActionMessages.ACTION_MESSAGE_EDITED_MESSAGE);
                    break;
                case CometChatConstants.ActionKeys.ACTION_MESSAGE_DELETED:
                    lastMessageData = new LastMessageData(MessageType.DELETED_MESSAGE,
                                                          "",
                                                          CometChatConstants.ActionMessages.ACTION_MESSAGE_DELETED_MESSAGE);
                    break;
            }
        }
        return lastMessageData;
    }

    private static void setMessageTypeIcon(Context context, SubtitleView view, Drawable icon, @DrawableRes int defaultIcon) {
        view.setMessageTypeIcon(icon == null ? ResourcesCompat.getDrawable(context.getResources(), defaultIcon, null) : icon);
    }

    private enum MessageType {
        DEFAULT, PHOTO, VIDEO, AUDIO, INCOMING_VOICE_CALL, OUTGOING_VOICE_CALL, INCOMING_VIDEO_CALL, OUTGOING_VIDEO_CALL, MISSED_VIDEO_CALL, MISSED_VOICE_CALL, EMOJI, STICKER, GIF, LINK, POLL, LOCATION, THREAD, DOCUMENT, COLLABORATIVE_DOCUMENT, COLLABORATIVE_WHITEBOARD, MEETING, DELETED_MESSAGE, NOT_SUPPORTED
    }

    private static class LastMessageData {
        private String lastMessage;
        private String sender;
        private MessageType messageType;

        public LastMessageData(MessageType messageType, String sender, String lastMessage) {
            this.lastMessage = lastMessage;
            this.sender = sender;
            this.messageType = messageType;
        }

        public String getLastMessage() {
            return lastMessage;
        }

        public void setLastMessage(String lastMessage) {
            this.lastMessage = lastMessage;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public MessageType getMessageType() {
            return messageType;
        }

        public void setMessageType(MessageType messageType) {
            this.messageType = messageType;
        }
    }
}
