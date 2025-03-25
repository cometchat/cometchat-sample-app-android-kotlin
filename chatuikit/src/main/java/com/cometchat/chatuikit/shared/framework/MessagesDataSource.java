package com.cometchat.chatuikit.shared.framework;

import android.content.Context;
import android.text.SpannableString;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.models.Action;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Conversation;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.InteractiveMessage;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.ai.AIOptionsStyle;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.formatters.CometChatMentionsFormatter;
import com.cometchat.chatuikit.shared.formatters.CometChatTextFormatter;
import com.cometchat.chatuikit.shared.formatters.FormatterUtils;
import com.cometchat.chatuikit.shared.models.AdditionParameter;
import com.cometchat.chatuikit.shared.models.CometChatMessageComposerAction;
import com.cometchat.chatuikit.shared.models.CometChatMessageOption;
import com.cometchat.chatuikit.shared.models.CometChatMessageTemplate;
import com.cometchat.chatuikit.shared.models.interactivemessage.CardMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.FormMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.SchedulerMessage;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.utils.MessageBubbleUtils;
import com.cometchat.chatuikit.shared.viewholders.MessagesViewHolderListener;
import com.cometchat.chatuikit.shared.views.audiobubble.CometChatAudioBubble;
import com.cometchat.chatuikit.shared.views.messagebubble.CometChatMessageBubble;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MessagesDataSource implements DataSource {
    private static final String TAG = MessagesDataSource.class.getSimpleName();


    private final HashMap<String, CometChatMessageTemplate> cometchatMessageTemplateHashMap = new HashMap<>();
    private CometChatAudioBubble currentlyPlayingBubble = null;
    private int currentlyPlayingPosition = -1;

    @Override
    public List<CometChatMessageOption> getTextMessageOptions(Context context,
                                                              BaseMessage baseMessage,
                                                              Group group,
                                                              AdditionParameter additionParameter) {
        return _getTextTemplateOptions(context, baseMessage, group, additionParameter);
    }

    private List<CometChatMessageOption> _getTextTemplateOptions(Context context,
                                                                 BaseMessage baseMessage,
                                                                 Group group,
                                                                 AdditionParameter additionParameter) {
        List<CometChatMessageOption> cometchatOptions = new ArrayList<>();
        if (baseMessage.getDeletedAt() == 0) {
            if (baseMessage.getParentMessageId() == 0) {
                if (additionParameter.getReplyInThreadOptionVisibility() == View.VISIBLE) cometchatOptions.add(_getReplyInThreadOption(context));
            }
            if (additionParameter.getShareMessageOptionVisibility() == View.VISIBLE) cometchatOptions.add(_getShareOption(context));
            if (additionParameter.getCopyMessageOptionVisibility() == View.VISIBLE) cometchatOptions.add(_getCopyOption(context));
            if (_isCommon(baseMessage, group)) {
                if (isMyMessage(baseMessage)) {
                    if (additionParameter.getEditMessageOptionVisibility() == View.VISIBLE) {
                        cometchatOptions.add(_getEditOption(context));
                    }
                    if (additionParameter.getMessageInfoOptionVisibility() == View.VISIBLE) {
                        cometchatOptions.add(_getMessageInformation(context));
                    }
                }
                if (additionParameter.getDeleteMessageOptionVisibility() == View.VISIBLE) {
                    cometchatOptions.add(_getDeleteOption(context));
                }
            }
            if (baseMessage.getReceiverType().equalsIgnoreCase(UIKitConstants.ReceiverType.GROUP) && !baseMessage
                .getSender()
                .getUid()
                .equalsIgnoreCase(CometChatUIKit.getLoggedInUser().getUid())) cometchatOptions.add(_getMessagePrivatelyOption(context));
        }
        return cometchatOptions;
    }

    private CometChatMessageOption _getReplyInThreadOption(Context context) {
        return new CometChatMessageOption(UIKitConstants.MessageOption.REPLY_IN_THREAD,
                                          context.getString(R.string.cometchat_reply_uppercase),
                                          R.drawable.cometchat_ic_threaded_message,
                                          null);
    }

    private CometChatMessageOption _getShareOption(Context context) {
        return new CometChatMessageOption(UIKitConstants.MessageOption.SHARE,
                                          context.getString(R.string.cometchat_share),
                                          R.drawable.cometchat_ic_share,
                                          null);
    }

    private CometChatMessageOption _getCopyOption(Context context) {
        return new CometChatMessageOption(UIKitConstants.MessageOption.COPY,
                                          context.getString(R.string.cometchat_copy),
                                          R.drawable.cometchat_ic_copy_paste,
                                          null);
    }

    private boolean _isCommon(BaseMessage baseMessage, Group group) {
        if (baseMessage.getSender().getUid().equals(CometChatUIKit.getLoggedInUser().getUid())) {
            return true;
        } else if (baseMessage.getReceiverType().equalsIgnoreCase(UIKitConstants.ReceiverType.GROUP)) {
            return isNotParticipant(group);
        } else return false;
    }

    private boolean isMyMessage(BaseMessage baseMessage) {
        return baseMessage.getSender().getUid().equals(CometChatUIKit.getLoggedInUser().getUid());
    }

    private CometChatMessageOption _getEditOption(Context context) {
        return new CometChatMessageOption(UIKitConstants.MessageOption.EDIT,
                                          context.getString(R.string.cometchat_edit),
                                          R.drawable.cometchat_ic_edit,
                                          null);
    }

    private CometChatMessageOption _getMessageInformation(Context context) {
        return new CometChatMessageOption(UIKitConstants.MessageOption.MESSAGE_INFORMATION,
                                          context.getString(R.string.cometchat_info),
                                          R.drawable.cometchat_ic_info,
                                          null);
    }

    private CometChatMessageOption _getDeleteOption(@NonNull Context context) {
        return new CometChatMessageOption(
            UIKitConstants.MessageOption.DELETE,
            context.getString(R.string.cometchat_delete),
            CometChatTheme.getErrorColor(context),
            R.drawable.cometchat_ic_delete,
            CometChatTheme.getErrorColor(context),
            0,
            null);
    }

    private CometChatMessageOption _getMessagePrivatelyOption(Context context) {
        return new CometChatMessageOption(UIKitConstants.MessageOption.MESSAGE_PRIVATELY,
                                          context.getString(R.string.cometchat_message_privately),
                                          R.drawable.cometchat_ic_send_message_privately,
                                          null);
    }

    private boolean isNotParticipant(Group group) {
        return group != null && group.getScope() != null && !CometChatConstants.SCOPE_PARTICIPANT.equalsIgnoreCase(group.getScope());
    }

    @Override
    public List<CometChatMessageOption> getImageMessageOptions(Context context,
                                                               BaseMessage baseMessage,
                                                               Group group,
                                                               AdditionParameter additionParameter) {
        return _getImageTemplateOptions(context, baseMessage, group, additionParameter);
    }

    private List<CometChatMessageOption> _getImageTemplateOptions(Context context,
                                                                  BaseMessage baseMessage,
                                                                  Group group,
                                                                  AdditionParameter additionParameter) {
        return ChatConfigurator.getDataSource().getCommonOptions(context, baseMessage, group, additionParameter);
    }

    @Override
    public List<CometChatMessageOption> getVideoMessageOptions(Context context,
                                                               BaseMessage baseMessage,
                                                               Group group,
                                                               AdditionParameter additionParameter) {
        return _getVideoTemplateOptions(context, baseMessage, group, additionParameter);
    }

    private List<CometChatMessageOption> _getVideoTemplateOptions(Context context,
                                                                  BaseMessage baseMessage,
                                                                  Group group,
                                                                  AdditionParameter additionParameter) {
        return ChatConfigurator.getDataSource().getCommonOptions(context, baseMessage, group, additionParameter);
    }

    @Override
    public List<CometChatMessageOption> getAudioMessageOptions(Context context,
                                                               BaseMessage baseMessage,
                                                               Group group,
                                                               AdditionParameter additionParameter) {
        return _getAudioTemplateOptions(context, baseMessage, group, additionParameter);
    }

    private List<CometChatMessageOption> _getAudioTemplateOptions(Context context,
                                                                  BaseMessage baseMessage,
                                                                  Group group,
                                                                  AdditionParameter additionParameter) {
        return ChatConfigurator.getDataSource().getCommonOptions(context, baseMessage, group, additionParameter);
    }

    @Override
    public List<CometChatMessageOption> getFileMessageOptions(Context context,
                                                              BaseMessage baseMessage,
                                                              Group group,
                                                              AdditionParameter additionParameter) {
        return _getFileTemplateOptions(context, baseMessage, group, additionParameter);
    }

    private List<CometChatMessageOption> _getFileTemplateOptions(Context context,
                                                                 BaseMessage baseMessage,
                                                                 Group group,
                                                                 AdditionParameter additionParameter) {
        return ChatConfigurator.getDataSource().getCommonOptions(context, baseMessage, group, additionParameter);
    }

    @Override
    public View getBottomView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
        return null;
    }

    @Override
    public void bindBottomView(Context context,
                               View createdView,
                               BaseMessage message,
                               UIKitConstants.MessageBubbleAlignment alignment,
                               RecyclerView.ViewHolder holder,
                               List<BaseMessage> messageList,
                               int position,
                               AdditionParameter additionParameter) {
    }

    @Override
    public View getTextBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
        return MessageBubbleUtils.getTextContentViewContainer(context);
    }

    @Override
    public void bindTextBubbleContentView(Context context,
                                          View createdView,
                                          TextMessage message,
                                          @StyleRes int textBubbleStyle,
                                          UIKitConstants.MessageBubbleAlignment alignment,
                                          RecyclerView.ViewHolder holder,
                                          List<BaseMessage> messageList,
                                          int position,
                                          AdditionParameter additionParameter) {
        MessageBubbleUtils.bindTextContentView(createdView,
                                               message,
                                               alignment,
                                               textBubbleStyle,
                                               CometChatUIKit
                                                   .getLoggedInUser()
                                                   .getUid()
                                                   .equals(message
                                                               .getSender()
                                                               .getUid()) ? additionParameter.getOutgoingDeleteBubbleStyle() : additionParameter.getIncomingDeleteBubbleStyle(),
                                               additionParameter);
    }

    @Override
    public View getFormBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
        return MessageBubbleUtils.getFormBubbleContainer(context, alignment);
    }

    @Override
    public void bindFormBubbleContentView(Context context,
                                          View createdView,
                                          FormMessage message,
                                          @StyleRes int formBubbleStyle,
                                          UIKitConstants.MessageBubbleAlignment alignment,
                                          RecyclerView.ViewHolder holder,
                                          List<BaseMessage> messageList,
                                          int position,
                                          AdditionParameter additionParameter) {
        MessageBubbleUtils.bindFormBubble(createdView,
                                          message,
                                          formBubbleStyle,
                                          CometChatUIKit
                                              .getLoggedInUser()
                                              .getUid()
                                              .equals(message
                                                          .getSender()
                                                          .getUid()) ? additionParameter.getOutgoingDeleteBubbleStyle() : additionParameter.getIncomingDeleteBubbleStyle(),
                                          additionParameter);
    }

    @Override
    public View getSchedulerBubbleContentView(Context context,
                                              CometChatMessageBubble messageBubble,
                                              UIKitConstants.MessageBubbleAlignment alignment) {
        return MessageBubbleUtils.getSchedulerBubbleContainer(context, alignment);
    }

    @Override
    public void bindSchedulerBubbleContentView(Context context,
                                               View createdView,
                                               SchedulerMessage message,
                                               @StyleRes int schedulerBubbleStyle,
                                               UIKitConstants.MessageBubbleAlignment alignment,
                                               RecyclerView.ViewHolder holder,
                                               List<BaseMessage> messageList,
                                               int position,
                                               AdditionParameter additionParameter) {
        MessageBubbleUtils.bindSchedulerBubble(createdView,
                                               message,
                                               schedulerBubbleStyle,
                                               CometChatUIKit
                                                   .getLoggedInUser()
                                                   .getUid()
                                                   .equals(message
                                                               .getSender()
                                                               .getUid()) ? additionParameter.getOutgoingDeleteBubbleStyle() : additionParameter.getIncomingDeleteBubbleStyle(),
                                               additionParameter);
    }

    @Override
    public View getCardBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
        return MessageBubbleUtils.getCardBubbleContainer(context, alignment);
    }

    @Override
    public void bindCardBubbleContentView(Context context,
                                          View createdView,
                                          CardMessage message,
                                          @StyleRes int cardBubbleStyle,
                                          UIKitConstants.MessageBubbleAlignment alignment,
                                          RecyclerView.ViewHolder holder,
                                          List<BaseMessage> messageList,
                                          int position,
                                          AdditionParameter additionParameter) {
        MessageBubbleUtils.bindCardBubble(createdView,
                                          message,
                                          cardBubbleStyle,
                                          CometChatUIKit
                                              .getLoggedInUser()
                                              .getUid()
                                              .equals(message
                                                          .getSender()
                                                          .getUid()) ? additionParameter.getOutgoingDeleteBubbleStyle() : additionParameter.getIncomingDeleteBubbleStyle(),
                                          additionParameter);
    }

    @Override
    public View getImageBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
        return MessageBubbleUtils.getImageContentViewContainer(context);
    }

    @Override
    public void bindImageBubbleContentView(Context context,
                                           View createdView,
                                           String thumbnailUrl,
                                           MediaMessage message,
                                           @StyleRes int imageBubbleStyle,
                                           UIKitConstants.MessageBubbleAlignment alignment,
                                           RecyclerView.ViewHolder holder,
                                           List<BaseMessage> messageList,
                                           int position,
                                           AdditionParameter additionParameter) {
        MessageBubbleUtils.bindImageContentView(createdView,
                                                thumbnailUrl,
                                                alignment,
                                                message,
                                                imageBubbleStyle,
                                                CometChatUIKit
                                                    .getLoggedInUser()
                                                    .getUid()
                                                    .equals(message
                                                                .getSender()
                                                                .getUid()) ? additionParameter.getOutgoingDeleteBubbleStyle() : additionParameter.getIncomingDeleteBubbleStyle(),
                                                additionParameter);
    }

    @Override
    public View getVideoBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
        return MessageBubbleUtils.getVideoContentViewContainer(context);
    }

    @Override
    public void bindVideoBubbleContentView(Context context,
                                           View createdView,
                                           String thumbnailUrl,
                                           MediaMessage message,
                                           @StyleRes int videoBubbleStyle,
                                           UIKitConstants.MessageBubbleAlignment alignment,
                                           RecyclerView.ViewHolder holder,
                                           List<BaseMessage> messageList,
                                           int position,
                                           AdditionParameter additionParameter) {
        MessageBubbleUtils.bindVideoContentView(createdView,
                                                thumbnailUrl,
                                                message,
                                                videoBubbleStyle,
                                                CometChatUIKit
                                                    .getLoggedInUser()
                                                    .getUid()
                                                    .equals(message
                                                                .getSender()
                                                                .getUid()) ? additionParameter.getOutgoingDeleteBubbleStyle() : additionParameter.getIncomingDeleteBubbleStyle(),
                                                additionParameter);
    }

    @Override
    public View getFileBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
        return MessageBubbleUtils.getFileContentViewContainer(context);
    }

    @Override
    public void bindFileBubbleContentView(Context context,
                                          View createdView,
                                          MediaMessage message,
                                          @StyleRes int fileBubbleStyle,
                                          UIKitConstants.MessageBubbleAlignment alignment,
                                          RecyclerView.ViewHolder holder,
                                          List<BaseMessage> messageList,
                                          int position,
                                          AdditionParameter additionParameter) {
        MessageBubbleUtils.bindFileContentView(context,
                                               createdView,
                                               message,
                                               fileBubbleStyle,
                                               CometChatUIKit
                                                   .getLoggedInUser()
                                                   .getUid()
                                                   .equals(message
                                                               .getSender()
                                                               .getUid()) ? additionParameter.getOutgoingDeleteBubbleStyle() : additionParameter.getIncomingDeleteBubbleStyle(),
                                               additionParameter);
    }

    @Override
    public View getAudioBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
        return MessageBubbleUtils.getAudioContentViewContainer(context);
    }

    @Override
    public void bindAudioBubbleContentView(Context context,
                                           View createdView,
                                           @NonNull MediaMessage message,
                                           @StyleRes int audioBubbleStyle,
                                           UIKitConstants.MessageBubbleAlignment alignment,
                                           RecyclerView.ViewHolder holder,
                                           List<BaseMessage> messageList,
                                           int position,
                                           AdditionParameter additionParameter) {
        MessageBubbleUtils.bindAudioContentView(context,
                                                createdView,
                                                message,
                                                audioBubbleStyle,
                                                CometChatUIKit
                                                    .getLoggedInUser()
                                                    .getUid()
                                                    .equals(message
                                                                .getSender()
                                                                .getUid()) ? additionParameter.getOutgoingDeleteBubbleStyle() : additionParameter.getIncomingDeleteBubbleStyle(),
                                                additionParameter);
        CometChatAudioBubble audioBubble = createdView.findViewById(R.id.audio_bubble);
        if (audioBubble != null) {
            audioBubble.setOnClick(() -> {
                if (currentlyPlayingPosition == position) {
                    if (audioBubble.isPlaying()) {
                        audioBubble.stopPlaying();
                    } else {
                        audioBubble.startPlaying();
                    }
                } else {
                    // Stop the currently playing audio
                    if (currentlyPlayingBubble != null) {
                        if (currentlyPlayingBubble.isPlaying()) {
                            currentlyPlayingBubble.stopPlaying();
                        }
                    }
                    audioBubble.startPlaying();

                    // Update the currently playing state
                    currentlyPlayingPosition = position;
                    currentlyPlayingBubble = audioBubble;
                }
            });
        }

        createdView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                // No action needed when attached
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                if (audioBubble != null && currentlyPlayingPosition == position && currentlyPlayingBubble == audioBubble) {
                    if (audioBubble.isPlaying()) {
                        audioBubble.stopPlaying();
                    }
                    currentlyPlayingBubble = null;
                    currentlyPlayingPosition = -1;
                }
            }
        });
    }

    @Override
    public CometChatMessageTemplate getAudioTemplate(AdditionParameter additionParameter) {
        return _getDefaultAudioTemplate(additionParameter);
    }

    private CometChatMessageTemplate _getDefaultAudioTemplate(AdditionParameter additionParameter) {
        return new CometChatMessageTemplate()
            .setCategory(CometChatConstants.CATEGORY_MESSAGE)
            .setType(CometChatConstants.MESSAGE_TYPE_AUDIO)
            .setOptions((context, baseMessage, group) -> ChatConfigurator
                .getDataSource()
                .getAudioMessageOptions(context, baseMessage, group, additionParameter))
            .setContentView(new MessagesViewHolderListener() {
                @Override
                public View createView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
                    return CometChatUIKit.getDataSource().getAudioBubbleContentView(context, messageBubble, alignment);
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
                        .bindAudioBubbleContentView(context,
                                                    createdView,
                                                    (MediaMessage) message,
                                                    CometChatUIKit
                                                        .getLoggedInUser()
                                                        .getUid()
                                                        .equals(message
                                                                    .getSender()
                                                                    .getUid()) ? additionParameter.getOutgoingAudioBubbleStyle() : additionParameter.getIncomingAudioBubbleStyle(),
                                                    alignment,
                                                    holder,
                                                    messageList,
                                                    position,
                                                    additionParameter);
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
                    CometChatUIKit.getDataSource().bindBottomView(context, createdView, message, alignment, holder, messageList, position, null);
                }
            });
    }

    @Override
    public CometChatMessageTemplate getVideoTemplate(AdditionParameter additionParameter) {
        return _getDefaultVideoTemplate(additionParameter);
    }

    private CometChatMessageTemplate _getDefaultVideoTemplate(AdditionParameter additionParameter) {
        return new CometChatMessageTemplate()
            .setCategory(CometChatConstants.CATEGORY_MESSAGE)
            .setType(CometChatConstants.MESSAGE_TYPE_VIDEO)
            .setOptions((context, baseMessage, group) -> ChatConfigurator
                .getDataSource()
                .getVideoMessageOptions(context, baseMessage, group, additionParameter))
            .setContentView(new MessagesViewHolderListener() {
                @Override
                public View createView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
                    return CometChatUIKit.getDataSource().getVideoBubbleContentView(context, messageBubble, alignment);
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
                        .bindVideoBubbleContentView(context,
                                                    createdView,
                                                    null,
                                                    (MediaMessage) message,
                                                    CometChatUIKit
                                                        .getLoggedInUser()
                                                        .getUid()
                                                        .equals(message
                                                                    .getSender()
                                                                    .getUid()) ? additionParameter.getOutgoingVideoBubbleStyle() : additionParameter.getIncomingVideoBubbleStyle(),
                                                    alignment,
                                                    holder,
                                                    messageList,
                                                    position,
                                                    additionParameter);
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
    public CometChatMessageTemplate getImageTemplate(AdditionParameter additionParameter) {
        return _getDefaultImageTemplate(additionParameter);
    }

    private CometChatMessageTemplate _getDefaultImageTemplate(@NonNull AdditionParameter additionParameter) {
        return new CometChatMessageTemplate()
            .setCategory(CometChatConstants.CATEGORY_MESSAGE)
            .setType(CometChatConstants.MESSAGE_TYPE_IMAGE)
            .setOptions((context, baseMessage, group) -> ChatConfigurator
                .getDataSource()
                .getImageMessageOptions(context, baseMessage, group, additionParameter))
            .setContentView(new MessagesViewHolderListener() {
                @Override
                public View createView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
                    return CometChatUIKit.getDataSource().getImageBubbleContentView(context, messageBubble, alignment);
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
                        .bindImageBubbleContentView(context,
                                                    createdView,
                                                    null,
                                                    (MediaMessage) message,
                                                    CometChatUIKit
                                                        .getLoggedInUser()
                                                        .getUid()
                                                        .equals(message
                                                                    .getSender()
                                                                    .getUid()) ? additionParameter.getOutgoingImageBubbleStyle() : additionParameter.getIncomingImageBubbleStyle(),
                                                    alignment,
                                                    holder,
                                                    messageList,
                                                    position,
                                                    additionParameter);
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
    public CometChatMessageTemplate getGroupActionsTemplate(AdditionParameter additionParameter) {
        return _getDefaultGroupActionTemplate(additionParameter);
    }

    private CometChatMessageTemplate _getDefaultGroupActionTemplate(AdditionParameter additionParameter) {
        return new CometChatMessageTemplate()
            .setCategory(CometChatConstants.CATEGORY_ACTION)
            .setType(CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER)
            .setContentView(new MessagesViewHolderListener() {
                @Override
                public View createView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
                    return MessageBubbleUtils.getActionContentViewContainer(context);
                }

                @Override
                public void bindView(Context context,
                                     View createdView,
                                     BaseMessage message,
                                     UIKitConstants.MessageBubbleAlignment alignment,
                                     RecyclerView.ViewHolder holder,
                                     List<BaseMessage> messageList,
                                     int position) {
                    MessageBubbleUtils.bindActionContentView(context, createdView, null, (Action) message, additionParameter.getActionBubbleStyle());
                }
            });
    }

    @Override
    public CometChatMessageTemplate getFileTemplate(AdditionParameter additionParameter) {
        return _getDefaultFileTemplate(additionParameter);
    }

    private CometChatMessageTemplate _getDefaultFileTemplate(AdditionParameter additionParameter) {
        return new CometChatMessageTemplate()
            .setCategory(CometChatConstants.CATEGORY_MESSAGE)
            .setType(CometChatConstants.MESSAGE_TYPE_FILE)
            .setOptions((context, baseMessage, group) -> ChatConfigurator
                .getDataSource()
                .getFileMessageOptions(context, baseMessage, group, additionParameter))
            .setContentView(new MessagesViewHolderListener() {
                @Override
                public View createView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
                    return CometChatUIKit.getDataSource().getFileBubbleContentView(context, messageBubble, alignment);
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
                        .bindFileBubbleContentView(context,
                                                   createdView,
                                                   (MediaMessage) message,
                                                   CometChatUIKit
                                                       .getLoggedInUser()
                                                       .getUid()
                                                       .equals(message
                                                                   .getSender()
                                                                   .getUid()) ? additionParameter.getOutgoingFileBubbleStyle() : additionParameter.getIncomingFileBubbleStyle(),
                                                   alignment,
                                                   holder,
                                                   messageList,
                                                   position,
                                                   additionParameter);
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
    public CometChatMessageTemplate getTextTemplate(@NonNull AdditionParameter additionParameter) {
        return _getDefaultTextTemplate(additionParameter);
    }

    private CometChatMessageTemplate _getDefaultTextTemplate(AdditionParameter additionParameter) {
        return new CometChatMessageTemplate()
            .setCategory(CometChatConstants.CATEGORY_MESSAGE)
            .setType(CometChatConstants.MESSAGE_TYPE_TEXT)
            .setOptions((context, baseMessage, group) -> ChatConfigurator
                .getDataSource()
                .getTextMessageOptions(context, baseMessage, group, additionParameter))
            .setContentView(new MessagesViewHolderListener() {
                @Override
                public View createView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
                    return CometChatUIKit.getDataSource().getTextBubbleContentView(context, messageBubble, alignment);
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
                        .bindTextBubbleContentView(context,
                                                   createdView,
                                                   (TextMessage) message,
                                                   message
                                                       .getSender()
                                                       .getUid()
                                                       .equals(CometChatUIKit
                                                                   .getLoggedInUser()
                                                                   .getUid()) ? additionParameter.getOutgoingTextBubbleStyle() : additionParameter.getIncomingTextBubbleStyle(),
                                                   alignment,
                                                   holder,
                                                   messageList,
                                                   position,
                                                   additionParameter);
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
    public CometChatMessageTemplate getFormTemplate(AdditionParameter additionParameter) {
        return _getDefaultFormTemplate(additionParameter);
    }

    private CometChatMessageTemplate _getDefaultFormTemplate(AdditionParameter additionParameter) {
        return new CometChatMessageTemplate()
            .setCategory(CometChatConstants.CATEGORY_INTERACTIVE)
            .setType(UIKitConstants.MessageType.FORM)
            .setOptions((context, baseMessage, group) -> ChatConfigurator
                .getDataSource()
                .getCommonOptions(context, baseMessage, group, additionParameter))
            .setContentView(new MessagesViewHolderListener() {
                @Override
                public View createView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
                    return CometChatUIKit.getDataSource().getFormBubbleContentView(context, messageBubble, alignment);
                }

                @Override
                public void bindView(Context context,
                                     View createdView,
                                     BaseMessage message,
                                     UIKitConstants.MessageBubbleAlignment alignment,
                                     RecyclerView.ViewHolder holder,
                                     List<BaseMessage> messageList,
                                     int position) {
                    /*
                     * FormBubbleStyle formBubbleStyle = new
                     * FormBubbleStyle().setTitleAppearance(theme.getTypography().getHeading()).
                     * setTitleColor(theme.getPalette().getAccent(context)).setSeparatorColor(theme.
                     * getPalette().getAccent100(context))
                     * .setLabelAppearance(theme.getTypography().getSubtitle1()).setLabelColor(theme
                     * .getPalette().getAccent(context))
                     * .setInputTextAppearance(theme.getTypography().getSubtitle1()).
                     * setInputTextColor(theme.getPalette().getAccent(context)).setInputHintColor(
                     * theme.getPalette().getAccent500(context)).setErrorColor(theme.getPalette().
                     * getError(context)).setInputStrokeColor(theme.getPalette().getAccent600(
                     * context)).setActiveInputStrokeColor(theme.getPalette().getAccent(context))
                     * .setDefaultCheckboxButtonTint(theme.getPalette().getAccent500(context)).
                     * setSelectedCheckboxButtonTint(theme.getPalette().getPrimary(context)).
                     * setErrorCheckboxButtonTint(theme.getPalette().getError(context)).
                     * setCheckboxTextColor(theme.getPalette().getAccent(context)).
                     * setCheckboxTextAppearance(theme.getTypography().getSubtitle1())
                     * .setButtonBackgroundColor(theme.getPalette().getPrimary(context)).
                     * setButtonTextColor(theme.getPalette().getAccent900(context)).
                     * setButtonTextAppearance(theme.getTypography().getSubtitle1()).
                     * setProgressBarTintColor(theme.getPalette().getAccent900(context)).
                     * setRadioButtonTint(theme.getPalette().getAccent500(context)).
                     * setRadioButtonTextColor(theme.getPalette().getAccent(context)).
                     * setRadioButtonTextAppearance(theme.getTypography().getSubtitle1()).
                     * setSelectedRadioButtonTint(theme.getPalette().getPrimary(context))
                     * .setSpinnerTextColor(theme.getPalette().getAccent(context)).
                     * setSpinnerTextAppearance(theme.getTypography().getSubtitle1()).
                     * setSpinnerBackgroundColor(theme.getPalette().getAccent500(context))
                     * .setBackgroundColor(theme.getPalette().getBackground(context)).setBackground(
                     * theme.getPalette().getGradientBackground()) .setSingleSelectStyle(new
                     * SingleSelectStyle().setOptionTextAppearance(theme.getTypography().
                     * getSubtitle1()).setOptionTextColor(theme.getPalette().getAccent500(context)).
                     * setSelectedOptionTextAppearance(theme.getTypography().getSubtitle1()).
                     * setSelectedOptionTextColor(theme.getPalette().getAccent(context)).
                     * setButtonStrokeColor(theme.getPalette().getAccent600(context)).setTitleColor(
                     * theme.getPalette().getAccent(context)).setTitleAppearance(theme.getTypography
                     * ().getSubtitle1())) .setQuickViewStyle(new
                     * QuickViewStyle().setCornerRadius(16).setBackgroundColor(theme.getPalette().
                     * getBackground(context)).setLeadingBarTint(theme.getPalette().getPrimary(
                     * context)).setTitleColor(theme.getPalette().getPrimary(context)).
                     * setTitleAppearance(theme.getTypography().getText1()).setSubtitleColor(theme.
                     * getPalette().getAccent500(context)).setSubtitleAppearance(theme.getTypography
                     * ().getSubtitle1()));
                     */
                    CometChatUIKit
                        .getDataSource()
                        .bindFormBubbleContentView(context,
                                                   createdView,
                                                   (FormMessage) message,
                                                   CometChatUIKit
                                                       .getLoggedInUser()
                                                       .getUid()
                                                       .equals(message
                                                                   .getSender()
                                                                   .getUid()) ? additionParameter.getOutgoingFormBubbleStyle() : additionParameter.getIncomingFormBubbleStyle(),
                                                   alignment,
                                                   holder,
                                                   messageList,
                                                   position,
                                                   additionParameter);
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
    public CometChatMessageTemplate getSchedulerTemplate(AdditionParameter additionParameter) {
        return _getDefaultSchedulerTemplate(additionParameter);
    }

    private CometChatMessageTemplate _getDefaultSchedulerTemplate(AdditionParameter additionParameter) {
        return new CometChatMessageTemplate()
            .setCategory(CometChatConstants.CATEGORY_INTERACTIVE)
            .setType(UIKitConstants.MessageType.SCHEDULER)
            .setOptions((context, baseMessage, group) -> ChatConfigurator
                .getDataSource()
                .getCommonOptions(context, baseMessage, group, additionParameter))
            .setContentView(new MessagesViewHolderListener() {
                @Override
                public View createView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
                    return CometChatUIKit.getDataSource().getSchedulerBubbleContentView(context, messageBubble, alignment);
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
                        .bindSchedulerBubbleContentView(context,
                                                        createdView,
                                                        (SchedulerMessage) message,
                                                        CometChatUIKit
                                                            .getLoggedInUser()
                                                            .getUid()
                                                            .equals(message
                                                                        .getSender()
                                                                        .getUid()) ? additionParameter.getOutgoingSchedulerBubbleStyle() : additionParameter.getIncomingSchedulerBubbleStyle(),
                                                        alignment,
                                                        holder,
                                                        messageList,
                                                        position,
                                                        additionParameter);
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
    public CometChatMessageTemplate getCardTemplate(AdditionParameter additionParameter) {
        return _getDefaultCardTemplate(additionParameter);
    }

    private CometChatMessageTemplate _getDefaultCardTemplate(AdditionParameter additionParameter) {
        return new CometChatMessageTemplate()
            .setCategory(CometChatConstants.CATEGORY_INTERACTIVE)
            .setType(UIKitConstants.MessageType.CARD)
            .setOptions((context, baseMessage, group) -> ChatConfigurator
                .getDataSource()
                .getCommonOptions(context, baseMessage, group, additionParameter))
            .setContentView(new MessagesViewHolderListener() {
                @Override
                public View createView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
                    return CometChatUIKit.getDataSource().getCardBubbleContentView(context, messageBubble, alignment);
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
                        .bindCardBubbleContentView(context,
                                                   createdView,
                                                   (CardMessage) message,
                                                   CometChatUIKit
                                                       .getLoggedInUser()
                                                       .getUid()
                                                       .equals(message
                                                                   .getSender()
                                                                   .getUid()) ? additionParameter.getOutgoingCardBubbleStyle() : additionParameter.getIncomingCardBubbleStyle(),
                                                   alignment,
                                                   holder,
                                                   messageList,
                                                   position,
                                                   additionParameter);
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
    public List<CometChatMessageTemplate> getMessageTemplates(AdditionParameter additionParameter) {
        return _getDefaultMessageTemplates(additionParameter);
    }

    private List<CometChatMessageTemplate> _getDefaultMessageTemplates(AdditionParameter additionParameter) {
        return new ArrayList<>(getDefaultMessageTemplatesHashMap(additionParameter).values());
    }

    public HashMap<String, CometChatMessageTemplate> getDefaultMessageTemplatesHashMap(AdditionParameter additionParameter) {
        cometchatMessageTemplateHashMap.put(UIKitConstants.MessageTemplateId.TEXT,
                                            ChatConfigurator.getDataSource().getTextTemplate(additionParameter));
        cometchatMessageTemplateHashMap.put(UIKitConstants.MessageTemplateId.IMAGE,
                                            ChatConfigurator.getDataSource().getImageTemplate(additionParameter));
        cometchatMessageTemplateHashMap.put(UIKitConstants.MessageTemplateId.VIDEO,
                                            ChatConfigurator.getDataSource().getVideoTemplate(additionParameter));
        cometchatMessageTemplateHashMap.put(UIKitConstants.MessageTemplateId.AUDIO,
                                            ChatConfigurator.getDataSource().getAudioTemplate(additionParameter));
        cometchatMessageTemplateHashMap.put(UIKitConstants.MessageTemplateId.FILE,
                                            ChatConfigurator.getDataSource().getFileTemplate(additionParameter));
        if (additionParameter.getGroupActionMessageVisibility() == View.VISIBLE)
            cometchatMessageTemplateHashMap.put(UIKitConstants.MessageTemplateId.GROUP_ACTION,
                                                ChatConfigurator
                                                    .getDataSource()
                                                    .getGroupActionsTemplate(additionParameter));
        cometchatMessageTemplateHashMap.put(UIKitConstants.MessageTemplateId.FORM,
                                            ChatConfigurator.getDataSource().getFormTemplate(additionParameter));
        cometchatMessageTemplateHashMap.put(UIKitConstants.MessageTemplateId.SCHEDULER,
                                            ChatConfigurator.getDataSource().getSchedulerTemplate(additionParameter));
        cometchatMessageTemplateHashMap.put(UIKitConstants.MessageTemplateId.CARD,
                                            ChatConfigurator.getDataSource().getCardTemplate(additionParameter));
        return cometchatMessageTemplateHashMap;
    }

    @Override
    public CometChatMessageTemplate getMessageTemplate(String category, String type, AdditionParameter additionParameter) {
        return _getMessageTemplate(category, type, additionParameter);
    }

    private CometChatMessageTemplate _getMessageTemplate(String category, String type, AdditionParameter additionParameter) {
        getDefaultMessageTemplatesHashMap(null);
        return cometchatMessageTemplateHashMap.get(category + "_" + type);
    }

    @Override
    public List<CometChatMessageOption> getMessageOptions(Context context,
                                                          BaseMessage baseMessage,
                                                          Group group,
                                                          AdditionParameter additionParameter) {
        return _getMessageOptions(context, baseMessage, group, additionParameter);
    }

    private List<CometChatMessageOption> _getMessageOptions(Context context,
                                                            BaseMessage baseMessage,
                                                            Group group,
                                                            AdditionParameter additionParameter) {
        List<CometChatMessageOption> cometchatOptions = new ArrayList<>();
        if (baseMessage.getType().equalsIgnoreCase(UIKitConstants.MessageType.TEXT)) {
            cometchatOptions.addAll(_getTextTemplateOptions(context, baseMessage, group, additionParameter));
        } else if (baseMessage.getType().equalsIgnoreCase(UIKitConstants.MessageType.IMAGE)) {
            cometchatOptions.addAll(_getImageTemplateOptions(context, baseMessage, group, additionParameter));
        } else if (baseMessage.getType().equalsIgnoreCase(UIKitConstants.MessageType.VIDEO)) {
            cometchatOptions.addAll(_getVideoTemplateOptions(context, baseMessage, group, additionParameter));
        } else if (baseMessage.getType().equalsIgnoreCase(UIKitConstants.MessageType.AUDIO)) {
            cometchatOptions.addAll(_getAudioTemplateOptions(context, baseMessage, group, additionParameter));
        } else if (baseMessage.getType().equalsIgnoreCase(UIKitConstants.MessageType.FILE)) {
            cometchatOptions.addAll(_getFileTemplateOptions(context, baseMessage, group, additionParameter));
        }
        return cometchatOptions;
    }

    @Override
    public List<CometChatMessageComposerAction> getAttachmentOptions(Context context,
                                                                     User user,
                                                                     Group group,
                                                                     HashMap<String, String> idMap,
                                                                     AdditionParameter additionParameter) {
        return _getDefaultAttachmentOptions(context, user, group, additionParameter);
    }

    private List<CometChatMessageComposerAction> _getDefaultAttachmentOptions(Context context,
                                                                              User user,
                                                                              Group group,
                                                                              AdditionParameter additionParameter) {
        List<CometChatMessageComposerAction> actions = new ArrayList<>();
        if (additionParameter != null) {
            if (additionParameter.getCameraAttachmentOptionVisibility() == View.VISIBLE) actions.add(_getCameraOption(context));
            if (additionParameter.getImageAttachmentOptionVisibility() == View.VISIBLE) actions.add(_getImageOption(context));
            if (additionParameter.getVideoAttachmentOptionVisibility() == View.VISIBLE) actions.add(_getVideoOption(context));
            if (additionParameter.getAudioAttachmentOptionVisibility() == View.VISIBLE) actions.add(_getAudioOption(context));
            if (additionParameter.getFileAttachmentOptionVisibility() == View.VISIBLE) actions.add(_getFileOption(context));
        }
        return actions;
    }

    private CometChatMessageComposerAction _getCameraOption(Context context) {
        return new CometChatMessageComposerAction()
            .setId(UIKitConstants.ComposerAction.CAMERA)
            .setTitle(context.getString(R.string.cometchat_camera))
            .setIcon(R.drawable.cometchat_ic_camera);
    }

    private CometChatMessageComposerAction _getImageOption(Context context) {
        return new CometChatMessageComposerAction()
            .setId(UIKitConstants.ComposerAction.IMAGE)
            .setTitle(context.getString(R.string.cometchat_attach_image))
            .setIcon(R.drawable.cometchat_ic_image_library);
    }

    private CometChatMessageComposerAction _getVideoOption(Context context) {
        return new CometChatMessageComposerAction()
            .setId(UIKitConstants.ComposerAction.VIDEO)
            .setTitle(context.getString(R.string.cometchat_attach_video))
            .setIcon(R.drawable.cometchat_ic_video_library);
    }

    private CometChatMessageComposerAction _getAudioOption(Context context) {
        return new CometChatMessageComposerAction()
            .setId(UIKitConstants.ComposerAction.AUDIO)
            .setTitle(context.getString(R.string.cometchat_attach_audio))
            .setIcon(R.drawable.cometchat_ic_audio);
    }

    private CometChatMessageComposerAction _getFileOption(Context context) {
        return new CometChatMessageComposerAction()
            .setId(UIKitConstants.ComposerAction.DOCUMENT)
            .setTitle(context.getString(R.string.cometchat_attach_document))
            .setIcon(R.drawable.cometchat_ic_file_upload);
    }

    @Override
    public List<CometChatMessageComposerAction> getAIOptions(Context context,
                                                             User user,
                                                             Group group,
                                                             HashMap<String, String> idMap,
                                                             AIOptionsStyle style,
                                                             AdditionParameter additionParameter) {
        return new ArrayList<>();
    }

    @Override
    public View getAuxiliaryOption(Context context, User user, Group group, HashMap<String, String> id, AdditionParameter additionParameter) {
        return null;
    }

    @Override
    public List<CometChatMessageOption> getCommonOptions(Context context, BaseMessage baseMessage, Group group, AdditionParameter additionParameter) {
        return _getCommonOptions(context, baseMessage, group, additionParameter);
    }

    private List<CometChatMessageOption> _getCommonOptions(Context context,
                                                           BaseMessage baseMessage,
                                                           Group group,
                                                           AdditionParameter additionParameter) {
        List<CometChatMessageOption> messageOptions = new ArrayList<>();
        if (baseMessage.getDeletedAt() == 0) {
            if (isMyMessage(baseMessage)) {
                if (additionParameter.getMessageInfoOptionVisibility() == View.VISIBLE) messageOptions.add(_getMessageInformation(context));
            }
            if (baseMessage.getParentMessageId() == 0) {
                if (additionParameter.getReplyInThreadOptionVisibility() == View.VISIBLE) messageOptions.add(_getReplyInThreadOption(context));
            }
            if (baseMessage instanceof TextMessage || baseMessage instanceof MediaMessage) {
                if (additionParameter.getShareMessageOptionVisibility() == View.VISIBLE) messageOptions.add(_getShareOption(context));
            }
            if (_isCommon(baseMessage, group)) {
                if (additionParameter.getDeleteMessageOptionVisibility() == View.VISIBLE) messageOptions.add(_getDeleteOption(context));
            }
            if (baseMessage.getReceiverType().equalsIgnoreCase(UIKitConstants.ReceiverType.GROUP) && !baseMessage
                .getSender()
                .getUid()
                .equalsIgnoreCase(CometChatUIKit.getLoggedInUser().getUid())) {
                if (additionParameter.getMessagePrivatelyOptionVisibility() == View.VISIBLE) messageOptions.add(_getMessagePrivatelyOption(context));
            }
        }
        return messageOptions;
    }

    @Override
    public List<String> getDefaultMessageTypes(AdditionParameter additionParameter) {
        return _getDefaultMessageTypes(additionParameter);
    }

    private List<String> _getDefaultMessageTypes(AdditionParameter additionParameter) {
        return new ArrayList<>(Arrays.asList(UIKitConstants.MessageType.TEXT,
                                             UIKitConstants.MessageType.FORM,
                                             UIKitConstants.MessageType.CARD,
                                             UIKitConstants.MessageType.SCHEDULER,
                                             UIKitConstants.MessageType.FILE,
                                             UIKitConstants.MessageType.IMAGE,
                                             UIKitConstants.MessageType.VIDEO,
                                             additionParameter.getGroupActionMessageVisibility() == View.VISIBLE ? CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER : "",
                                             UIKitConstants.MessageType.AUDIO));
    }

    @Override
    public List<String> getDefaultMessageCategories(AdditionParameter additionParameter) {
        return _getDefaultMessageCategories(additionParameter);
    }

    private List<String> _getDefaultMessageCategories(AdditionParameter additionParameter) {
        return new ArrayList<>(Arrays.asList(CometChatConstants.CATEGORY_MESSAGE,
                                             additionParameter.getGroupActionMessageVisibility() == View.VISIBLE ? CometChatConstants.CATEGORY_ACTION : "",
                                             CometChatConstants.CATEGORY_INTERACTIVE));
    }

    @Override
    public SpannableString getLastConversationMessage(Context context, Conversation conversation, AdditionParameter additionParameter) {
        return _getLastConversationMessage(context, conversation, additionParameter);
    }

    private static SpannableString _getLastConversationMessage(Context context, Conversation conversation, AdditionParameter additionParameter) {
        String lastMessageText;
        BaseMessage baseMessage = conversation.getLastMessage();
        if (baseMessage != null) {
            lastMessageText = getLastMessage(context, baseMessage);
            if (baseMessage.getDeletedAt() > 0) {
                lastMessageText = context.getString(R.string.cometchat_this_message_deleted);
            }
        } else {
            lastMessageText = context.getResources().getString(R.string.cometchat_start_conv_hint);
        }
        return SpannableString.valueOf(FormatterUtils.getFormattedText(context,
                                                                       conversation.getLastMessage(),
                                                                       UIKitConstants.FormattingType.CONVERSATIONS,
                                                                       null,
                                                                       lastMessageText,
                                                                       additionParameter != null && additionParameter.getTextFormatters() != null ? additionParameter.getTextFormatters() : new ArrayList<>()));
    }

    private static String getLastMessage(Context context, BaseMessage lastMessage) {

        String message = null;

        switch (lastMessage.getCategory()) {
            case CometChatConstants.CATEGORY_MESSAGE:
                if (lastMessage instanceof TextMessage) {
                    message = Utils.getMessagePrefix(lastMessage,
                                                     context) + (((TextMessage) lastMessage).getText() == null ? context.getString(R.string.cometchat_this_message_deleted) : ((TextMessage) lastMessage).getText());
                } else if (lastMessage instanceof MediaMessage) {
                    if (lastMessage.getDeletedAt() == 0) {
                        if (lastMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_IMAGE))
                            message = Utils.getMessagePrefix(lastMessage, context) + context.getString(R.string.cometchat_message_image);
                        else if (lastMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_VIDEO))
                            message = Utils.getMessagePrefix(lastMessage, context) + context.getString(R.string.cometchat_message_video);
                        else if (lastMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_FILE))
                            message = Utils.getMessagePrefix(lastMessage, context) + context.getString(R.string.cometchat_message_document);
                        else if (lastMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_AUDIO))
                            message = Utils.getMessagePrefix(lastMessage, context) + context.getString(R.string.cometchat_message_audio);
                    } else message = context.getString(R.string.cometchat_this_message_deleted);
                }
                break;
            case CometChatConstants.CATEGORY_CUSTOM:
                if (lastMessage.getDeletedAt() == 0) {
                    if (lastMessage.getMetadata() != null && lastMessage.getMetadata().has("pushNotification")) {
                        try {
                            message = lastMessage.getMetadata().getString("pushNotification");
                        } catch (Exception e) {
                            CometChatLogger.e(TAG, e.toString());
                        }
                    } else {
                        message = Utils.getMessagePrefix(lastMessage, context) + lastMessage.getType();
                    }
                } else message = context.getString(R.string.cometchat_this_message_deleted);

                break;
            case CometChatConstants.CATEGORY_INTERACTIVE:
                if (lastMessage.getDeletedAt() == 0) {
                    if (lastMessage.getType().equals(UIKitConstants.MessageType.FORM))
                        message = Utils.getMessagePrefix(lastMessage, context) + context.getString(R.string.cometchat_message_form);
                    else if (lastMessage.getType().equals(UIKitConstants.MessageType.CARD))
                        message = Utils.getMessagePrefix(lastMessage, context) + context.getString(R.string.cometchat_message_card);
                    else if (lastMessage.getType().equals(UIKitConstants.MessageType.SCHEDULER)) {
                        InteractiveMessage interactiveMessage = (InteractiveMessage) lastMessage;
                        SchedulerMessage schedulerMessage = (SchedulerMessage) Utils.convertToUIKitMessage(interactiveMessage);
                        message = Utils.getMessagePrefix(lastMessage,
                                                         context) + context.getString(R.string.cometchat_message_scheduler) + " " + (schedulerMessage.getTitle() != null && !schedulerMessage
                            .getTitle()
                            .isEmpty() ? schedulerMessage.getTitle() : "Meet with " + schedulerMessage.getSender().getName());
                    }
                } else message = context.getString(R.string.cometchat_this_message_deleted);
                break;
            case CometChatConstants.CATEGORY_ACTION:
                if (((Action) lastMessage).getMessage() != null && !((Action) lastMessage).getMessage().isEmpty())
                    message = ((Action) lastMessage).getMessage();
                else message = getActionMessage(((Action) lastMessage));
                break;
            default:
                message = context.getString(R.string.cometchat_start_conv_hint);
        }
        return message;
    }

    private static String getActionMessage(Action action) {
        String message = "";
        if (action.getType().equalsIgnoreCase(CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER)) {
            switch (action.getAction()) {
                case CometChatConstants.ActionKeys.ACTION_JOINED: {
                    User actionBy = (User) action.getActionBy();
                    message = String.format(Locale.US, CometChatConstants.ActionMessages.ACTION_GROUP_JOINED_MESSAGE, actionBy.getName());
                    break;
                }
                case CometChatConstants.ActionKeys.ACTION_LEFT: {
                    User actionBy = (User) action.getActionBy();
                    message = String.format(Locale.US, CometChatConstants.ActionMessages.ACTION_GROUP_LEFT_MESSAGE, actionBy.getName());
                    break;
                }
                case CometChatConstants.ActionKeys.ACTION_KICKED: {
                    User actionBy = (User) action.getActionBy();
                    User actionOn = (User) action.getActionOn();
                    message = String.format(Locale.US,
                                            CometChatConstants.ActionMessages.ACTION_MEMBER_KICKED_MESSAGE,
                                            actionBy.getName(),
                                            actionOn.getName());
                    break;
                }
                case CometChatConstants.ActionKeys.ACTION_BANNED: {
                    User actionBy = (User) action.getActionBy();
                    User actionOn = (User) action.getActionOn();
                    message = String.format(Locale.US,
                                            CometChatConstants.ActionMessages.ACTION_MEMBER_BANNED_MESSAGE,
                                            actionBy.getName(),
                                            actionOn.getName());
                    break;
                }
                case CometChatConstants.ActionKeys.ACTION_UNBANNED: {
                    User actionBy = (User) action.getActionBy();
                    User actionOn = (User) action.getActionOn();
                    message = String.format(Locale.US,
                                            CometChatConstants.ActionMessages.ACTION_MEMBER_UNBANNED_MESSAGE,
                                            actionBy.getName(),
                                            actionOn.getName());
                    break;
                }
                case CometChatConstants.ActionKeys.ACTION_MEMBER_ADDED: {
                    User actionBy = (User) action.getActionBy();
                    User actionOn = (User) action.getActionOn();
                    message = String.format(Locale.US,
                                            CometChatConstants.ActionMessages.ACTION_MEMBER_ADDED_TO_GROUP,
                                            actionBy.getName(),
                                            actionOn.getName());
                    break;
                }
                case CometChatConstants.ActionKeys.ACTION_SCOPE_CHANGED: {
                    User actionBy = (User) action.getActionBy();
                    User actionOn = (User) action.getActionOn();
                    message = String.format(Locale.US,
                                            CometChatConstants.ActionMessages.ACTION_MEMBER_SCOPE_CHANGED,
                                            actionBy.getName(),
                                            actionOn.getName(),
                                            action.getNewScope());
                    break;
                }
            }
        } else if (action.getType().equalsIgnoreCase(CometChatConstants.ActionKeys.ACTION_TYPE_MESSAGE)) {
            switch (action.getAction()) {
                case CometChatConstants.ActionKeys.ACTION_MESSAGE_EDITED:
                    message = CometChatConstants.ActionMessages.ACTION_MESSAGE_EDITED_MESSAGE;
                    break;
                case CometChatConstants.ActionKeys.ACTION_MESSAGE_DELETED:
                    message = CometChatConstants.ActionMessages.ACTION_MESSAGE_DELETED_MESSAGE;
                    break;
            }
        }
        return message;
    }

    @Override
    public View getAuxiliaryHeaderMenu(Context context, User user, Group group, AdditionParameter additionParameter) {
        return null;
    }

    @Override
    public List<CometChatTextFormatter> getTextFormatters(Context context, AdditionParameter additionParameter) {
        return _getTextFormatters(context);
    }

    private List<CometChatTextFormatter> _getTextFormatters(Context context) {
        CometChatMentionsFormatter cometchatMentionsFormatter = new CometChatMentionsFormatter(context);
        List<CometChatTextFormatter> list = new ArrayList<>();
        list.add(cometchatMentionsFormatter);
        return list;
    }

    @Override
    public String getId() {
        return null;
    }
}
