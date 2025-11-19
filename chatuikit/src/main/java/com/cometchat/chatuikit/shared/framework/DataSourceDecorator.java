package com.cometchat.chatuikit.shared.framework;

import android.content.Context;
import android.text.SpannableString;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.models.AIAssistantMessage;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Conversation;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.ai.AIOptionsStyle;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.formatters.CometChatTextFormatter;
import com.cometchat.chatuikit.shared.models.AdditionParameter;
import com.cometchat.chatuikit.shared.models.CometChatMessageComposerAction;
import com.cometchat.chatuikit.shared.models.CometChatMessageOption;
import com.cometchat.chatuikit.shared.models.CometChatMessageTemplate;
import com.cometchat.chatuikit.shared.models.interactivemessage.CardMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.FormMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.SchedulerMessage;
import com.cometchat.chatuikit.shared.views.messagebubble.CometChatMessageBubble;

import java.util.HashMap;
import java.util.List;

public abstract class DataSourceDecorator implements DataSource {
    private static final String TAG = DataSourceDecorator.class.getSimpleName();
    private final DataSource dataSource;

    public DataSourceDecorator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Returns a list of message options for a text message.
     *
     * @param context     The context of the application.
     * @param baseMessage The base message object.
     * @param group       The group object associated with the message.
     * @return A list of CometChatMessageOption objects representing the available
     * options for the text message.
     */
    @Override
    public List<CometChatMessageOption> getTextMessageOptions(Context context,
                                                              BaseMessage baseMessage,
                                                              Group group,
                                                              @NonNull AdditionParameter additionParameter) {
        return dataSource.getTextMessageOptions(context, baseMessage, group, additionParameter);
    }

    /**
     * Returns a list of message options for an image message.
     *
     * @param context     The context of the application.
     * @param baseMessage The base message object.
     * @param group       The group object associated with the message.
     * @return A list of CometChatMessageOption objects representing the available
     * options for the image message.
     */
    @Override
    public List<CometChatMessageOption> getImageMessageOptions(Context context,
                                                               BaseMessage baseMessage,
                                                               Group group,
                                                               @NonNull AdditionParameter additionParameter) {
        return dataSource.getImageMessageOptions(context, baseMessage, group, additionParameter);
    }

    /**
     * Returns a list of message options for a video message.
     *
     * @param context     The context of the application.
     * @param baseMessage The base message object.
     * @param group       The group object associated with the message.
     * @return A list of CometChatMessageOption objects representing the available
     * options for the video message.
     */
    @Override
    public List<CometChatMessageOption> getVideoMessageOptions(Context context,
                                                               BaseMessage baseMessage,
                                                               Group group,
                                                               @NonNull AdditionParameter additionParameter) {
        return dataSource.getVideoMessageOptions(context, baseMessage, group, additionParameter);
    }

    /**
     * Returns a list of message options for an audio message.
     *
     * @param context     The context of the application.
     * @param baseMessage The base message object.
     * @param group       The group object associated with the message.
     * @return A list of CometChatMessageOption objects representing the available
     * options for the audio message.
     */
    @Override
    public List<CometChatMessageOption> getAudioMessageOptions(Context context,
                                                               BaseMessage baseMessage,
                                                               Group group,
                                                               @NonNull AdditionParameter additionParameter) {
        return dataSource.getAudioMessageOptions(context, baseMessage, group, additionParameter);
    }

    /**
     * Returns a list of message options for a file message.
     *
     * @param context     The context of the application.
     * @param baseMessage The base message object.
     * @param group       The group object associated with the message.
     * @return A list of CometChatMessageOption objects representing the available
     * options for the file message.
     */
    @Override
    public List<CometChatMessageOption> getFileMessageOptions(Context context,
                                                              BaseMessage baseMessage,
                                                              Group group,
                                                              @NonNull AdditionParameter additionParameter) {
        return dataSource.getFileMessageOptions(context, baseMessage, group, additionParameter);
    }

    /**
     * @param context
     * @param messageBubble
     * @param alignment
     * @return
     */
    @Override
    public View getBottomView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
        return dataSource.getBottomView(context, messageBubble, alignment);
    }

    /**
     * @param context
     * @param createdView
     * @param message
     * @param alignment
     * @param holder
     * @param messageList
     * @param position
     * @param additionParameter
     */
    @Override
    public void bindBottomView(Context context,
                               View createdView,
                               BaseMessage message,
                               UIKitConstants.MessageBubbleAlignment alignment,
                               RecyclerView.ViewHolder holder,
                               List<BaseMessage> messageList,
                               int position,
                               @NonNull AdditionParameter additionParameter) {
        dataSource.bindBottomView(context, createdView, message, alignment, holder, messageList, position, additionParameter);
    }

    /**
     * Returns the reply view container.
     *
     * @param context The context of the application.
     * @return The reply view container.
     */
    @Override
    public View getReplyViewContainer(Context context) {
        return dataSource.getReplyViewContainer(context);
    }

    /**
     * Binds the reply view container with the provided message and other parameters.
     *
     * @param context           The context of the application.
     * @param createdView       The created reply view container.
     * @param message           The base message object.
     * @param alignment         The message bubble alignment.
     * @param holder            The RecyclerView ViewHolder.
     * @param messageList       The list of base messages.
     * @param position          The position of the message in the list.
     * @param additionParameter Additional parameters for binding.
     */
    @Override
    public void bindReplyViewContainer(Context context, View createdView, BaseMessage message, UIKitConstants.MessageBubbleAlignment alignment, RecyclerView.ViewHolder holder, List<BaseMessage> messageList, int position, AdditionParameter additionParameter) {
        dataSource.bindReplyViewContainer(context, createdView, message, alignment, holder, messageList, position, additionParameter);
    }

    /**
     * @param context
     * @param messageBubble
     * @param alignment
     * @return
     */
    @Override
    public View getTextBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
        return dataSource.getTextBubbleContentView(context, messageBubble, alignment);
    }

    /**
     * @param context
     * @param createdView
     * @param message
     * @param textBubbleStyle
     * @param alignment
     * @param holder
     * @param messageList
     * @param position
     * @param additionParameter
     */
    @Override
    public void bindTextBubbleContentView(Context context,
                                          View createdView,
                                          TextMessage message,
                                          int textBubbleStyle,
                                          UIKitConstants.MessageBubbleAlignment alignment,
                                          RecyclerView.ViewHolder holder,
                                          List<BaseMessage> messageList,
                                          int position,
                                          @NonNull AdditionParameter additionParameter) {
        dataSource.bindTextBubbleContentView(context,
                                             createdView,
                                             message,
                                             textBubbleStyle,
                                             alignment,
                                             holder,
                                             messageList,
                                             position,
                                             additionParameter);
    }

    /**
     * @param context
     * @param messageBubble
     * @param alignment
     * @return
     */
    @Override
    public View getFormBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
        return dataSource.getFormBubbleContentView(context, messageBubble, alignment);
    }

    /**
     * @param context
     * @param createdView
     * @param message
     * @param formBubbleStyle
     * @param alignment
     * @param holder
     * @param messageList
     * @param position
     * @param additionParameter
     */
    @Override
    public void bindFormBubbleContentView(Context context,
                                          View createdView,
                                          FormMessage message,
                                          int formBubbleStyle,
                                          UIKitConstants.MessageBubbleAlignment alignment,
                                          RecyclerView.ViewHolder holder,
                                          List<BaseMessage> messageList,
                                          int position,
                                          @NonNull AdditionParameter additionParameter) {
        dataSource.bindFormBubbleContentView(context,
                                             createdView,
                                             message,
                                             formBubbleStyle,
                                             alignment,
                                             holder,
                                             messageList,
                                             position,
                                             additionParameter);
    }

    /**
     * @param context
     * @param messageBubble
     * @param alignment
     * @return
     */
    @Override
    public View getSchedulerBubbleContentView(Context context,
                                              CometChatMessageBubble messageBubble,
                                              UIKitConstants.MessageBubbleAlignment alignment) {
        return dataSource.getSchedulerBubbleContentView(context, messageBubble, alignment);
    }

    /**
     * @param context
     * @param createdView
     * @param message
     * @param schedulerBubbleStyle
     * @param alignment
     * @param holder
     * @param messageList
     * @param position
     * @param additionParameter
     */
    @Override
    public void bindSchedulerBubbleContentView(Context context,
                                               View createdView,
                                               SchedulerMessage message,
                                               int schedulerBubbleStyle,
                                               UIKitConstants.MessageBubbleAlignment alignment,
                                               RecyclerView.ViewHolder holder,
                                               List<BaseMessage> messageList,
                                               int position,
                                               @NonNull AdditionParameter additionParameter) {
        dataSource.bindSchedulerBubbleContentView(context,
                                                  createdView,
                                                  message,
                                                  schedulerBubbleStyle,
                                                  alignment,
                                                  holder,
                                                  messageList,
                                                  position,
                                                  additionParameter);
    }

    /**
     * @param context
     * @param messageBubble
     * @param alignment
     * @return
     */
    @Override
    public View getCardBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
        return dataSource.getCardBubbleContentView(context, messageBubble, alignment);
    }

    /**
     * @param context
     * @param createdView
     * @param message
     * @param cardBubbleStyle
     * @param alignment
     * @param holder
     * @param messageList
     * @param position
     * @param additionParameter
     */
    @Override
    public void bindCardBubbleContentView(Context context,
                                          View createdView,
                                          CardMessage message,
                                          int cardBubbleStyle,
                                          UIKitConstants.MessageBubbleAlignment alignment,
                                          RecyclerView.ViewHolder holder,
                                          List<BaseMessage> messageList,
                                          int position,
                                          @NonNull AdditionParameter additionParameter) {
        dataSource.bindCardBubbleContentView(context,
                                             createdView,
                                             message,
                                             cardBubbleStyle,
                                             alignment,
                                             holder,
                                             messageList,
                                             position,
                                             additionParameter);
    }

    /**
     * @param context
     * @param messageBubble
     * @param alignment
     * @return
     */
    @Override
    public View getImageBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
        return dataSource.getImageBubbleContentView(context, messageBubble, alignment);
    }

    /**
     * @param context
     * @param createdView
     * @param imageUrl
     * @param message
     * @param imageBubbleStyle
     * @param alignment
     * @param holder
     * @param messageList
     * @param position
     * @param additionParameter
     */
    @Override
    public void bindImageBubbleContentView(Context context,
                                           View createdView,
                                           String imageUrl,
                                           MediaMessage message,
                                           int imageBubbleStyle,
                                           UIKitConstants.MessageBubbleAlignment alignment,
                                           RecyclerView.ViewHolder holder,
                                           List<BaseMessage> messageList,
                                           int position,
                                           @NonNull AdditionParameter additionParameter) {
        dataSource.bindImageBubbleContentView(context,
                                              createdView,
                                              imageUrl,
                                              message,
                                              imageBubbleStyle,
                                              alignment,
                                              holder,
                                              messageList,
                                              position,
                                              additionParameter);
    }

    /**
     * @param context
     * @param messageBubble
     * @param alignment
     * @return
     */
    @Override
    public View getVideoBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
        return dataSource.getVideoBubbleContentView(context, messageBubble, alignment);
    }

    /**
     * @param context
     * @param createdView
     * @param thumbnailUrl
     * @param message
     * @param videoBubbleStyle
     * @param alignment
     * @param holder
     * @param messageList
     * @param position
     * @param additionParameter
     */
    @Override
    public void bindVideoBubbleContentView(Context context,
                                           View createdView,
                                           String thumbnailUrl,
                                           MediaMessage message,
                                           int videoBubbleStyle,
                                           UIKitConstants.MessageBubbleAlignment alignment,
                                           RecyclerView.ViewHolder holder,
                                           List<BaseMessage> messageList,
                                           int position,
                                           @NonNull AdditionParameter additionParameter) {
        dataSource.bindVideoBubbleContentView(context,
                                              createdView,
                                              thumbnailUrl,
                                              message,
                                              videoBubbleStyle,
                                              alignment,
                                              holder,
                                              messageList,
                                              position,
                                              additionParameter);
    }

    /**
     * @param context
     * @param messageBubble
     * @param alignment
     * @return
     */
    @Override
    public View getFileBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
        return dataSource.getFileBubbleContentView(context, messageBubble, alignment);
    }

    /**
     * @param context
     * @param createdView
     * @param message
     * @param fileBubbleStyle
     * @param alignment
     * @param holder
     * @param messageList
     * @param position
     * @param additionParameter
     */
    @Override
    public void bindFileBubbleContentView(Context context,
                                          View createdView,
                                          MediaMessage message,
                                          int fileBubbleStyle,
                                          UIKitConstants.MessageBubbleAlignment alignment,
                                          RecyclerView.ViewHolder holder,
                                          List<BaseMessage> messageList,
                                          int position,
                                          @NonNull AdditionParameter additionParameter) {
        dataSource.bindFileBubbleContentView(context,
                                             createdView,
                                             message,
                                             fileBubbleStyle,
                                             alignment,
                                             holder,
                                             messageList,
                                             position,
                                             additionParameter);
    }

    /**
     * @param context
     * @param messageBubble
     * @param alignment
     * @return
     */
    @Override
    public View getAudioBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
        return dataSource.getAudioBubbleContentView(context, messageBubble, alignment);
    }

    /**
     * @param context
     * @param createdView
     * @param message
     * @param audioBubbleStyle
     * @param alignment
     * @param holder
     * @param messageList
     * @param position
     * @param additionParameter
     */
    @Override
    public void bindAudioBubbleContentView(Context context,
                                           View createdView,
                                           MediaMessage message,
                                           int audioBubbleStyle,
                                           UIKitConstants.MessageBubbleAlignment alignment,
                                           RecyclerView.ViewHolder holder,
                                           List<BaseMessage> messageList,
                                           int position,
                                           @NonNull AdditionParameter additionParameter) {
        dataSource.bindAudioBubbleContentView(context,
                                              createdView,
                                              message,
                                              audioBubbleStyle,
                                              alignment,
                                              holder,
                                              messageList,
                                              position,
                                              additionParameter);
    }

    @Override
    public View getAIAssistantBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
        return dataSource.getAIAssistantBubbleContentView(context, messageBubble, alignment);
    }

    @Override
    public void bindAIAssistantBubbleContentView(Context context, View createdView, AIAssistantMessage message, UIKitConstants.MessageBubbleAlignment alignment, RecyclerView.ViewHolder holder, List<BaseMessage> messageList, int position, @NonNull AdditionParameter additionParameter) {
        dataSource.bindAIAssistantBubbleContentView(context, createdView, message, alignment, holder, messageList, position, additionParameter);
    }

    /**
     * @param additionParameter
     * @return
     */
    @Override
    public CometChatMessageTemplate getAudioTemplate(@NonNull AdditionParameter additionParameter) {
        return dataSource.getAudioTemplate(additionParameter);
    }

    /**
     * @param additionParameter
     * @return
     */
    @Override
    public CometChatMessageTemplate getVideoTemplate(@NonNull AdditionParameter additionParameter) {
        return dataSource.getVideoTemplate(additionParameter);
    }

    /**
     * @param additionParameter
     * @return
     */
    @Override
    public CometChatMessageTemplate getImageTemplate(@NonNull AdditionParameter additionParameter) {
        return dataSource.getImageTemplate(additionParameter);
    }

    /**
     * @param additionParameter
     * @return
     */
    @Override
    public CometChatMessageTemplate getGroupActionsTemplate(@NonNull AdditionParameter additionParameter) {
        return dataSource.getGroupActionsTemplate(additionParameter);
    }

    /**
     * @param additionParameter
     * @return
     */
    @Override
    public CometChatMessageTemplate getFileTemplate(@NonNull AdditionParameter additionParameter) {
        return dataSource.getFileTemplate(additionParameter);
    }

    /**
     * @param additionParameter
     * @return
     */
    @Override
    public CometChatMessageTemplate getTextTemplate(@NonNull AdditionParameter additionParameter) {
        return dataSource.getTextTemplate(additionParameter);
    }

    /**
     * @param additionParameter
     * @return
     */
    @Override
    public CometChatMessageTemplate getFormTemplate(@NonNull AdditionParameter additionParameter) {
        return dataSource.getFormTemplate(additionParameter);
    }

    /**
     * @param additionParameter
     * @return
     */
    @Override
    public CometChatMessageTemplate getSchedulerTemplate(@NonNull AdditionParameter additionParameter) {
        return dataSource.getSchedulerTemplate(additionParameter);
    }

    /**
     * @param additionParameter
     * @return
     */
    @Override
    public CometChatMessageTemplate getCardTemplate(@NonNull AdditionParameter additionParameter) {
        return dataSource.getCardTemplate(additionParameter);
    }

    /**
     * @param additionParameter
     * @return
     */
    @Override
    public List<CometChatMessageTemplate> getMessageTemplates(@NonNull AdditionParameter additionParameter) {
        return dataSource.getMessageTemplates(additionParameter);
    }

    @Override
    public CometChatMessageTemplate getAIAssistantTemplate(@NonNull AdditionParameter additionParameter) {
        return dataSource.getAIAssistantTemplate(additionParameter);
    }

    /**
     * Returns the message template for the specified category and type.
     *
     * @param category The category of the message.
     * @param type     The type of the message.
     * @return The CometChatMessageTemplate object representing the specified
     * message template.
     */
    @Override
    public CometChatMessageTemplate getMessageTemplate(String category, String type, @NonNull AdditionParameter additionParameter) {
        return dataSource.getMessageTemplate(category, type, additionParameter);
    }

    /**
     * Returns a list of message options for a message.
     *
     * @param context     The context of the application.
     * @param baseMessage The base message object.
     * @param group       The group object associated with the message.
     * @return A list of CometChatMessageOption objects representing the available
     * options for the message.
     */
    @Override
    public List<CometChatMessageOption> getMessageOptions(Context context,
                                                          BaseMessage baseMessage,
                                                          Group group,
                                                          @NonNull AdditionParameter additionParameter) {
        return dataSource.getMessageOptions(context, baseMessage, group, additionParameter);
    }

    /**
     * Returns a list of attachment options for the message composer.
     *
     * @param context The context of the application.
     * @param user    The user object associated with the composer.
     * @param group   The group object associated with the composer.
     * @param idMap   The map of attachment IDs.
     * @return A list of CometChatMessageComposerAction objects representing the
     * available attachment options.
     */
    @Override
    public List<CometChatMessageComposerAction> getAttachmentOptions(Context context,
                                                                     User user,
                                                                     Group group,
                                                                     HashMap<String, String> idMap,
                                                                     @NonNull AdditionParameter additionParameter) {
        return dataSource.getAttachmentOptions(context, user, group, idMap, additionParameter);
    }

    /**
     * Returns a list of AI options for the message composer.
     *
     * @param context        The context of the application.
     * @param user           The user object associated with the composer.
     * @param group          The group object associated with the composer.
     * @param idMap          The map of attachment IDs.
     * @param aiOptionsStyle
     * @return A list of CometChatMessageComposerAction objects representing the
     * available AI options.
     */
    @Override
    public List<CometChatMessageComposerAction> getAIOptions(Context context,
                                                             User user,
                                                             Group group,
                                                             HashMap<String, String> idMap,
                                                             AIOptionsStyle aiOptionsStyle,
                                                             @NonNull AdditionParameter additionParameter) {
        return dataSource.getAIOptions(context, user, group, idMap, aiOptionsStyle, additionParameter);
    }

    /**
     * Returns the auxiliary option view for the message composer.
     *
     * @param context The context of the application.
     * @param user    The user object associated with the composer.
     * @param group   The group object associated with the composer.
     * @param id      The ID of the auxiliary option.
     * @return The auxiliary option view.
     */
    @Override
    public View getAuxiliaryOption(Context context,
                                   User user,
                                   Group group,
                                   HashMap<String, String> id,
                                   @NonNull AdditionParameter additionParameter) {
        return dataSource.getAuxiliaryOption(context, user, group, id, additionParameter);
    }

    /**
     * Returns a list of common options for the message.
     *
     * @param context     The context of the application.
     * @param baseMessage The base message object.
     * @param group       The group object associated with the message.
     * @return A list of CometChatMessageOption objects representing the common
     * options for the message.
     */
    @Override
    public List<CometChatMessageOption> getCommonOptions(Context context,
                                                         BaseMessage baseMessage,
                                                         Group group,
                                                         @NonNull AdditionParameter additionParameter) {
        return dataSource.getCommonOptions(context, baseMessage, group, additionParameter);
    }

    /**
     * Returns a list of default message types.
     *
     * @return A list of default message types.
     */
    @Override
    public List<String> getDefaultMessageTypes(@NonNull AdditionParameter additionParameter) {
        return dataSource.getDefaultMessageTypes(additionParameter);
    }

    /**
     * Returns a list of default message categories.
     *
     * @return A list of default message categories.
     */
    @Override
    public List<String> getDefaultMessageCategories(@NonNull AdditionParameter additionParameter) {
        return dataSource.getDefaultMessageCategories(additionParameter);
    }

    /**
     * @param context
     * @param conversation
     * @param additionParameter
     * @return
     */
    @Override
    public SpannableString getLastConversationMessage(Context context, Conversation conversation, @NonNull AdditionParameter additionParameter) {
        return dataSource.getLastConversationMessage(context, conversation, additionParameter);
    }

    /**
     * Returns the auxiliary header menu view for the user or group.
     *
     * @param context The context of the application.
     * @param user    The user object associated with the header menu.
     * @param group   The group object associated with the header menu.
     * @return The auxiliary header menu view.
     */
    @Override
    public View getAuxiliaryHeaderMenu(Context context, User user, Group group, @NonNull AdditionParameter additionParameter) {
        return dataSource.getAuxiliaryHeaderMenu(context, user, group, additionParameter);
    }

    /**
     * @param context
     * @return
     */
    @Override
    public List<CometChatTextFormatter> getTextFormatters(Context context, @NonNull AdditionParameter additionParameter) {
        return dataSource.getTextFormatters(context, additionParameter);
    }
}
