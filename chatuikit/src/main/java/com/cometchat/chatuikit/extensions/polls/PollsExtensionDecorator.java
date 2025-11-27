package com.cometchat.chatuikit.extensions.polls;

import android.app.Dialog;
import android.content.Context;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Conversation;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.extensions.ExtensionConstants;
import com.cometchat.chatuikit.extensions.polls.bubble.CometChatPollBubble;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper;
import com.cometchat.chatuikit.shared.constants.MessageStatus;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.events.CometChatMessageEvents;
import com.cometchat.chatuikit.shared.framework.ChatConfigurator;
import com.cometchat.chatuikit.shared.framework.DataSource;
import com.cometchat.chatuikit.shared.framework.DataSourceDecorator;
import com.cometchat.chatuikit.shared.models.AdditionParameter;
import com.cometchat.chatuikit.shared.models.CometChatMessageComposerAction;
import com.cometchat.chatuikit.shared.models.CometChatMessageTemplate;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.utils.MessageBubbleUtils;
import com.cometchat.chatuikit.shared.viewholders.MessagesViewHolderListener;
import com.cometchat.chatuikit.shared.views.deletebubble.CometChatDeleteBubble;
import com.cometchat.chatuikit.shared.views.messagebubble.CometChatMessageBubble;
import com.cometchat.chatuikit.shared.views.messagepreview.CometChatMessagePreview;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PollsExtensionDecorator extends DataSourceDecorator {
    private static final String TAG = PollsExtensionDecorator.class.getSimpleName();
    private final String LISTENER_ID;
    private BaseMessage quotedMessage;

    public PollsExtensionDecorator(DataSource dataSource) {
        super(dataSource);
        LISTENER_ID = TAG + System.currentTimeMillis();
        addListeners();
    }

    private void addListeners() {
        CometChatMessageEvents.addListener(LISTENER_ID, new CometChatMessageEvents() {
            /**
             * Called when a reply to a message is sent/in progress.
             *
             * @param baseMessage The replied message object.
             * @param status      The status of the reply message.
             */
            @Override
            public void ccReplyToMessage(BaseMessage baseMessage, int status) {
                if (status == MessageStatus.ERROR || status == MessageStatus.SUCCESS) {
                    quotedMessage = null;
                } else {
                    quotedMessage = baseMessage;
                }
            }
        });
    }

    @Override
    public List<CometChatMessageTemplate> getMessageTemplates(@NonNull AdditionParameter additionParameter) {
        List<CometChatMessageTemplate> templates = super.getMessageTemplates(additionParameter);
        templates.add(getPollTemplate(additionParameter));
        return templates;
    }

    @Override
    public List<CometChatMessageComposerAction> getAttachmentOptions(Context context,
                                                                     User user,
                                                                     Group group,
                                                                     HashMap<String, String> idMap,
                                                                     AdditionParameter additionParameter) {
        if (!idMap.containsKey(UIKitConstants.MapId.PARENT_MESSAGE_ID)) {
            List<CometChatMessageComposerAction> messageComposerActions = super.getAttachmentOptions(context, user, group, idMap, additionParameter);
            if (additionParameter != null && additionParameter.getPollAttachmentOptionVisibility() == View.VISIBLE)
                messageComposerActions.add(new CometChatMessageComposerAction()
                                               .setId(ExtensionConstants.ExtensionType.EXTENSION_POLL)
                                               .setTitle(context.getString(R.string.cometchat_poll))
                                               .setIcon(R.drawable.cometchat_ic_polls)
                                               .setTitleColor(CometChatTheme.getTextColorPrimary(context))
                                               .setBackground(CometChatTheme.getBackgroundColor1(context))
                                               .setTitleAppearance(CometChatTheme.getTextAppearanceBodyRegular(context))
                                               .setIconTintColor(CometChatTheme.getIconTintHighlight(context))
                                               .setOnClick(() -> {
                                                   String id, type;
                                                   if (user != null) {
                                                       id = user.getUid();
                                                       type = UIKitConstants.ReceiverType.USER;
                                                   } else {
                                                       id = group.getGuid();
                                                       type = UIKitConstants.ReceiverType.GROUP;
                                                   }
                                                   long quotedMessageId = Utils.getQuotedMessageId(quotedMessage, user, group);
                                                   if (quotedMessageId == -1) {
                                                       quotedMessage = null;
                                                   }
                                                   AlertDialog.Builder alertDialog = new AlertDialog.Builder(context,
                                                                                                             androidx.appcompat.R.style.AlertDialog_AppCompat);
                                                   CometChatCreatePoll chatCreatePoll = new CometChatCreatePoll(context);
                                                   ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                                                              ViewGroup.LayoutParams.MATCH_PARENT);
                                                   chatCreatePoll.setLayoutParams(params);
                                                   chatCreatePoll.setFitsSystemWindows(true);
                                                   alertDialog.setView(chatCreatePoll);
                                                   Dialog dialog = alertDialog.create();
                                                   Utils.setDialogStatusBarColor(dialog, CometChatTheme.getBackgroundColor1(context));
                                                   chatCreatePoll.setBackClickListener(view -> dialog.dismiss());
                                                   chatCreatePoll.setOnSubmitClickListener((question, options) -> {
                                                       try {
                                                           chatCreatePoll.setErrorStateVisibility(View.GONE);
                                                           chatCreatePoll.setProgressVisibility(View.VISIBLE);
                                                           JSONObject jsonObject = new JSONObject();
                                                           jsonObject.put("question", question);
                                                           jsonObject.put("options", options);
                                                           jsonObject.put("receiver", id);
                                                           jsonObject.put("receiverType", type);
                                                           if (quotedMessageId > -1) {
                                                               jsonObject.put("quotedMessageId", quotedMessageId);
                                                           }
                                                           CometChat.callExtension("polls",
                                                                                   "POST",
                                                                                   "/v2/create",
                                                                                   jsonObject,
                                                                                   new CometChat.CallbackListener<JSONObject>() {
                                                                                       @Override
                                                                                       public void onSuccess(JSONObject jsonObject) {
                                                                                           chatCreatePoll.setProgressVisibility(View.GONE);
                                                                                           dialog.dismiss();
                                                                                           if (quotedMessage != null)
                                                                                               CometChatUIKitHelper.onMessageReply(quotedMessage, MessageStatus.SUCCESS);
                                                                                       }

                                                                                       @Override
                                                                                       public void onError(CometChatException e) {
                                                                                           chatCreatePoll.setProgressVisibility(View.GONE);
                                                                                           chatCreatePoll.setErrorStateVisibility(View.VISIBLE);
                                                                                       }
                                                                                   });
                                                       } catch (Exception e) {
                                                           CometChatLogger.e(TAG, e.toString());
                                                       }
                                                   });
                                                   Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                   dialog.show();
                                               }));
            return messageComposerActions;
        } else return super.getAttachmentOptions(context, user, group, idMap, additionParameter);
    }

    @Override
    public List<String> getDefaultMessageTypes(AdditionParameter additionParameter) {
        List<String> types = super.getDefaultMessageTypes(additionParameter);
        if (!types.contains(ExtensionConstants.ExtensionType.EXTENSION_POLL)) {
            types.add(ExtensionConstants.ExtensionType.EXTENSION_POLL);
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
    public SpannableString getLastConversationMessage(Context context, Conversation conversation, AdditionParameter additionParameter) {
        if (conversation != null && conversation.getLastMessage() != null && UIKitConstants.MessageCategory.CUSTOM.equals(conversation
                                                                                                                              .getLastMessage()
                                                                                                                              .getCategory()) && ExtensionConstants.ExtensionType.EXTENSION_POLL.equalsIgnoreCase(
            conversation.getLastMessage().getType()))
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

    public String getLastMessage(@NonNull Context context, BaseMessage lastMessage) {
        return Utils.getMessagePrefix(lastMessage, context) + context.getString(R.string.cometchat_polls);
    }

    public CometChatMessageTemplate getPollTemplate(@NonNull AdditionParameter additionParameter) {
        return new CometChatMessageTemplate()
                .setCategory(UIKitConstants.MessageCategory.CUSTOM)
                .setType(ExtensionConstants.ExtensionType.EXTENSION_POLL)
                .setOptions((context, baseMessage, isLeftAlign) -> ChatConfigurator
                        .getDataSource()
                        .getCommonOptions(context, baseMessage, isLeftAlign, additionParameter))
                .setReplyView(new MessagesViewHolderListener() {
                    @Override
                    public View createView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
                        return CometChatUIKit.getDataSource().getReplyViewContainer(context);
                    }

                    @Override
                    public void bindView(Context context, View createdView, BaseMessage message, UIKitConstants.MessageBubbleAlignment alignment, RecyclerView.ViewHolder holder, List<BaseMessage> messageList, int position) {
                        CometChatUIKit.getDataSource().bindReplyViewContainer(context, createdView, message, alignment, holder, messageList, position, additionParameter);
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) createdView.getLayoutParams();
                        params.width = Utils.convertDpToPx(context, 240);
                        createdView.setLayoutParams(params);

                        CometChatMessagePreview cometChatMessagePreview = createdView.findViewById(R.id.reply_message_preview);
                        cometChatMessagePreview.setMinimumWidth(240);
                        cometChatMessagePreview.setMaxWidth(240);
                    }
                })
                .setContentView(new MessagesViewHolderListener() {
                    @Override
                    public View createView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
                        View view = View.inflate(context, R.layout.cometchat_poll_bubble_layout_container, null);
                        LinearLayout parent = view.findViewById(R.id.cometchat_poll_bubble_layout_container_parent);

                        MessageBubbleUtils.setDeletedMessageBubble(context, view);
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) parent.getLayoutParams();
                        parent.setGravity(alignment.equals(UIKitConstants.MessageBubbleAlignment.RIGHT) ? Gravity.END : Gravity.START);
                        parent.setLayoutParams(params);
                        return view;
                    }

                    @Override
                    public void bindView(Context context,
                                         View createdView,
                                         BaseMessage message,
                                         UIKitConstants.MessageBubbleAlignment alignment,
                                         RecyclerView.ViewHolder holder,
                                         List<BaseMessage> messageList,
                                         int position) {
                        CometChatPollBubble pollBubble = createdView.findViewById(R.id.cometchat_poll_bubble);
                        CometChatDeleteBubble deletedBubble = createdView.findViewById(R.id.cometchat_delete_text_bubble);
                        if (message.getDeletedAt() == 0) {
                            deletedBubble.setVisibility(View.GONE);
                            pollBubble.setVisibility(View.VISIBLE);
                            pollBubble.setMessage((CustomMessage) message);
                            pollBubble.setStyle(CometChatUIKit
                                    .getLoggedInUser()
                                    .getUid()
                                    .equals(message
                                            .getSender()
                                            .getUid()) ? additionParameter.getOutgoingPollBubbleStyle() : additionParameter.getIncomingPollBubbleStyle());
                        } else {
                            pollBubble.setVisibility(View.GONE);
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
        return PollsExtension.class.getSimpleName();
    }
}
