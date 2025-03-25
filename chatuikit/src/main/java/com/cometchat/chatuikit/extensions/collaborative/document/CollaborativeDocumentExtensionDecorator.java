package com.cometchat.chatuikit.extensions.collaborative.document;

import android.content.Context;
import android.text.SpannableString;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Conversation;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.extensions.ExtensionConstants;
import com.cometchat.chatuikit.extensions.Extensions;
import com.cometchat.chatuikit.extensions.collaborative.CollaborativeBoardBubbleConfiguration;
import com.cometchat.chatuikit.extensions.collaborative.CollaborativeUtils;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.framework.ChatConfigurator;
import com.cometchat.chatuikit.shared.framework.DataSource;
import com.cometchat.chatuikit.shared.framework.DataSourceDecorator;
import com.cometchat.chatuikit.shared.models.AdditionParameter;
import com.cometchat.chatuikit.shared.models.CometChatMessageComposerAction;
import com.cometchat.chatuikit.shared.models.CometChatMessageTemplate;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.viewholders.MessagesViewHolderListener;
import com.cometchat.chatuikit.shared.views.messagebubble.CometChatMessageBubble;
import com.cometchat.chatuikit.shared.views.reaction.ExtensionResponseListener;

import java.util.HashMap;
import java.util.List;

public class CollaborativeDocumentExtensionDecorator extends DataSourceDecorator {
    private static final String TAG = CollaborativeDocumentExtensionDecorator.class.getSimpleName();
    private final String collaborativeDocumentExtensionTypeConstant = ExtensionConstants.ExtensionType.DOCUMENT;
    private CollaborativeBoardBubbleConfiguration configuration;

    public CollaborativeDocumentExtensionDecorator(DataSource dataSource) {
        super(dataSource);
    }

    public CollaborativeDocumentExtensionDecorator(DataSource dataSource, CollaborativeBoardBubbleConfiguration configuration) {
        super(dataSource);
        this.configuration = configuration;
    }

    @Override
    public List<CometChatMessageTemplate> getMessageTemplates(AdditionParameter additionParameter) {
        List<CometChatMessageTemplate> templates = super.getMessageTemplates(additionParameter);
        templates.add(getWhiteBoardTemplate(additionParameter));
        return templates;
    }

    @Override
    public List<CometChatMessageComposerAction> getAttachmentOptions(Context context,
                                                                     @Nullable User user,
                                                                     Group group,
                                                                     HashMap<String, String> idMap, AdditionParameter additionParameter) {
        if (!idMap.containsKey(UIKitConstants.MapId.PARENT_MESSAGE_ID)) {
            List<CometChatMessageComposerAction> messageComposerActions = super.getAttachmentOptions(context, user, group, idMap, additionParameter);
            if (additionParameter != null && additionParameter.getCollaborativeDocumentOptionVisibility() == View.VISIBLE)
                messageComposerActions.add(new CometChatMessageComposerAction()
                                               .setId(ExtensionConstants.ExtensionType.DOCUMENT)
                                               .setTitle(context.getString(R.string.cometchat_collaborative_doc))
                                               .setIcon(R.drawable.cometchat_ic_collaborative_document)
                                               .setTitleColor(CometChatTheme.getTextColorPrimary(context))
                                               .setTitleAppearance(CometChatTheme.getTextAppearanceBodyRegular(context))
                                               .setIconTintColor(CometChatTheme.getIconTintHighlight(context))
                                               .setBackground(CometChatTheme.getBackgroundColor1(context))
                                               .setOnClick(() -> {
                                                   String id, type;
                                                   id = user != null ? user.getUid() : group.getGuid();
                                                   type = user != null ? UIKitConstants.ReceiverType.USER : UIKitConstants.ReceiverType.GROUP;
                                                   Extensions.callWriteBoardExtension(id, type, new ExtensionResponseListener() {
                                                       @Override
                                                       public void OnResponseSuccess(Object var) {
                                                       }

                                                       @Override
                                                       public void OnResponseFailed(CometChatException e) {
                                                           showError(context);
                                                       }
                                                   });
                                               }));
            return messageComposerActions;
        } else return super.getAttachmentOptions(context, user, group, idMap, additionParameter);
    }

    @Override
    public List<String> getDefaultMessageTypes(AdditionParameter additionParameter) {
        List<String> types = super.getDefaultMessageTypes(additionParameter);
        if (!types.contains(collaborativeDocumentExtensionTypeConstant)) {
            types.add(collaborativeDocumentExtensionTypeConstant);
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
        if (conversation != null && conversation.getLastMessage() != null && (UIKitConstants.MessageCategory.CUSTOM.equals(conversation
                                                                                                                               .getLastMessage()
                                                                                                                               .getCategory()) && ExtensionConstants.ExtensionType.DOCUMENT.equalsIgnoreCase(
            conversation.getLastMessage().getType())))
            return SpannableString.valueOf(getLastConversationMessage_(context, conversation, additionParameter));
        else return super.getLastConversationMessage(context, conversation, additionParameter);
    }

    @NonNull
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
        return Utils.getMessagePrefix(lastMessage, context) + context.getString(R.string.cometchat_document);
    }

    private void showError(Context context) {
        String errorMessage = context.getString(R.string.cometchat_something_went_wrong);
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
    }

    public CometChatMessageTemplate getWhiteBoardTemplate(AdditionParameter additionParameter) {
        return new CometChatMessageTemplate()
            .setCategory(UIKitConstants.MessageCategory.CUSTOM)
            .setType(collaborativeDocumentExtensionTypeConstant)
            .setOptions((context, baseMessage, isLeftAlign) -> ChatConfigurator
                .getDataSource()
                .getCommonOptions(context, baseMessage, isLeftAlign, additionParameter))
            .setContentView(new MessagesViewHolderListener() {
                @NonNull
                @Override
                public View createView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
                    return CollaborativeUtils.getCollaborativeBubbleView(context,
                                                                         configuration,
                                                                         context.getResources().getString(R.string.cometchat_collaborative_doc),
                                                                         context
                                                                             .getResources()
                                                                             .getString(R.string.cometchat_open_document_to_edit_content_together),
                                                                         context.getResources().getString(R.string.cometchat_open_document));
                }

                @Override
                public void bindView(Context context,
                                     View createdView,
                                     @NonNull BaseMessage message,
                                     UIKitConstants.MessageBubbleAlignment alignment,
                                     RecyclerView.ViewHolder holder,
                                     List<BaseMessage> messageList,
                                     int position) {
                    CollaborativeUtils.bindWriteBordCollaborativeBubble(context,
                                                                        createdView,
                                                                        CometChatUIKit
                                                                            .getLoggedInUser()
                                                                            .getUid()
                                                                            .equals(message
                                                                                        .getSender()
                                                                                        .getUid()) ? additionParameter.getOutgoingCollaborativeBubbleStyle() : additionParameter.getIncomingCollaborativeBubbleStyle(),
                                                                        message,
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
    public String getId() {
        return CollaborativeDocumentExtensionDecorator.class.getSimpleName();
    }
}
