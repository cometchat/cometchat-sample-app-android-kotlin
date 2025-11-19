package com.cometchat.chatuikit.shared.framework;

import android.content.Context;
import android.text.SpannableString;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
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

/**
 * The DataSource interface provides methods to retrieve various data related to
 * CometChat messages and UI components.
 */
public interface DataSource {

    /**
     * Returns a list of message options for a text message.
     *
     * @param context     The context of the application.
     * @param baseMessage The base message object.
     * @param group       The group object associated with the message.
     * @return A list of CometChatMessageOption objects representing the available
     * options for the text message.
     */
    List<CometChatMessageOption> getTextMessageOptions(Context context,
                                                       BaseMessage baseMessage,
                                                       Group group,
                                                       @NonNull AdditionParameter additionParameter);

    /**
     * Returns a list of message options for an image message.
     *
     * @param context     The context of the application.
     * @param baseMessage The base message object.
     * @param group       The group object associated with the message.
     * @return A list of CometChatMessageOption objects representing the available
     * options for the image message.
     */
    List<CometChatMessageOption> getImageMessageOptions(Context context,
                                                        BaseMessage baseMessage,
                                                        Group group,
                                                        @NonNull AdditionParameter additionParameter);

    /**
     * Returns a list of message options for a video message.
     *
     * @param context     The context of the application.
     * @param baseMessage The base message object.
     * @param group       The group object associated with the message.
     * @return A list of CometChatMessageOption objects representing the available
     * options for the video message.
     */
    List<CometChatMessageOption> getVideoMessageOptions(Context context,
                                                        BaseMessage baseMessage,
                                                        Group group,
                                                        @NonNull AdditionParameter additionParameter);

    /**
     * Returns a list of message options for an audio message.
     *
     * @param context     The context of the application.
     * @param baseMessage The base message object.
     * @param group       The group object associated with the message.
     * @return A list of CometChatMessageOption objects representing the available
     * options for the audio message.
     */
    List<CometChatMessageOption> getAudioMessageOptions(Context context,
                                                        BaseMessage baseMessage,
                                                        Group group,
                                                        @NonNull AdditionParameter additionParameter);

    /**
     * Returns a list of message options for a file message.
     *
     * @param context     The context of the application.
     * @param baseMessage The base message object.
     * @param group       The group object associated with the message.
     * @return A list of CometChatMessageOption objects representing the available
     * options for the file message.
     */
    List<CometChatMessageOption> getFileMessageOptions(Context context,
                                                       BaseMessage baseMessage,
                                                       Group group,
                                                       @NonNull AdditionParameter additionParameter);

    View getBottomView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment);

    void bindBottomView(Context context,
                        View createdView,
                        BaseMessage message,
                        UIKitConstants.MessageBubbleAlignment alignment,
                        RecyclerView.ViewHolder holder,
                        List<BaseMessage> messageList,
                        int position,
                        @NonNull AdditionParameter additionParameter);

    View getTextBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment);

    void bindTextBubbleContentView(Context context,
                                   View createdView,
                                   TextMessage message,
                                   @StyleRes int textBubbleStyle,
                                   UIKitConstants.MessageBubbleAlignment alignment,
                                   RecyclerView.ViewHolder holder,
                                   List<BaseMessage> messageList,
                                   int position,
                                   @NonNull AdditionParameter additionParameter);

    View getFormBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment);

    void bindFormBubbleContentView(Context context,
                                   View createdView,
                                   FormMessage message,
                                   @StyleRes int formBubbleStyle,
                                   UIKitConstants.MessageBubbleAlignment alignment,
                                   RecyclerView.ViewHolder holder,
                                   List<BaseMessage> messageList,
                                   int position,
                                   @NonNull AdditionParameter additionParameter);

    View getSchedulerBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment);

    void bindSchedulerBubbleContentView(Context context,
                                        View createdView,
                                        SchedulerMessage message,
                                        @StyleRes int schedulerBubbleStyle,
                                        UIKitConstants.MessageBubbleAlignment alignment,
                                        RecyclerView.ViewHolder holder,
                                        List<BaseMessage> messageList,
                                        int position,
                                        @NonNull AdditionParameter additionParameter);

    View getCardBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment);

    void bindCardBubbleContentView(Context context,
                                   View createdView,
                                   CardMessage message,
                                   @StyleRes int cardBubbleStyle,
                                   UIKitConstants.MessageBubbleAlignment alignment,
                                   RecyclerView.ViewHolder holder,
                                   List<BaseMessage> messageList,
                                   int position,
                                   @NonNull AdditionParameter additionParameter);

    View getImageBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment);

    void bindImageBubbleContentView(Context context,
                                    View createdView,
                                    String imageUrl,
                                    MediaMessage message,
                                    @StyleRes int imageBubbleStyle,
                                    UIKitConstants.MessageBubbleAlignment alignment,
                                    RecyclerView.ViewHolder holder,
                                    List<BaseMessage> messageList,
                                    int position,
                                    @NonNull AdditionParameter additionParameter);

    View getVideoBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment);

    void bindVideoBubbleContentView(Context context,
                                    View createdView,
                                    String thumbnailUrl,
                                    MediaMessage message,
                                    @StyleRes int videoBubbleStyle,
                                    UIKitConstants.MessageBubbleAlignment alignment,
                                    RecyclerView.ViewHolder holder,
                                    List<BaseMessage> messageList,
                                    int position,
                                    @NonNull AdditionParameter additionParameter);

    View getFileBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment);

    void bindFileBubbleContentView(Context context,
                                   View createdView,
                                   MediaMessage message,
                                   @StyleRes int fileBubbleStyle,
                                   UIKitConstants.MessageBubbleAlignment alignment,
                                   RecyclerView.ViewHolder holder,
                                   List<BaseMessage> messageList,
                                   int position,
                                   @NonNull AdditionParameter additionParameter);

    View getAudioBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment);

    void bindAudioBubbleContentView(Context context,
                                    View createdView,
                                    MediaMessage message,
                                    @StyleRes int audioBubbleStyle,
                                    UIKitConstants.MessageBubbleAlignment alignment,
                                    RecyclerView.ViewHolder holder,
                                    List<BaseMessage> messageList,
                                    int position,
                                    @NonNull AdditionParameter additionParameter);

    View getAIAssistantBubbleContentView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment);

    void bindAIAssistantBubbleContentView(Context context,
                                          View createdView,
                                          AIAssistantMessage message,
                                          UIKitConstants.MessageBubbleAlignment alignment,
                                          RecyclerView.ViewHolder holder,
                                          List<BaseMessage> messageList,
                                          int position,
                                          @NonNull AdditionParameter additionParameter);

    CometChatMessageTemplate getAudioTemplate(@NonNull AdditionParameter additionParameter);

    CometChatMessageTemplate getVideoTemplate(@NonNull AdditionParameter additionParameter);

    CometChatMessageTemplate getImageTemplate(@NonNull AdditionParameter additionParameter);

    CometChatMessageTemplate getGroupActionsTemplate(@NonNull AdditionParameter additionParameter);

    CometChatMessageTemplate getFileTemplate(@NonNull AdditionParameter additionParameter);

    CometChatMessageTemplate getTextTemplate(@NonNull AdditionParameter additionParameter);

    CometChatMessageTemplate getFormTemplate(@NonNull AdditionParameter additionParameter);

    CometChatMessageTemplate getSchedulerTemplate(@NonNull AdditionParameter additionParameter);

    CometChatMessageTemplate getCardTemplate(@NonNull AdditionParameter additionParameter);

    List<CometChatMessageTemplate> getMessageTemplates(@NonNull AdditionParameter additionParameter);

    CometChatMessageTemplate getAIAssistantTemplate(@NonNull AdditionParameter additionParameter);

    /**
     * Returns the message template for the specified category and type.
     *
     * @param category The category of the message.
     * @param type     The type of the message.
     * @return The CometChatMessageTemplate object representing the specified
     * message template.
     */
    CometChatMessageTemplate getMessageTemplate(String category, String type, @NonNull AdditionParameter additionParameter);

    /**
     * Returns a list of message options for a message.
     *
     * @param context     The context of the application.
     * @param baseMessage The base message object.
     * @param group       The group object associated with the message.
     * @return A list of CometChatMessageOption objects representing the available
     * options for the message.
     */
    List<CometChatMessageOption> getMessageOptions(Context context,
                                                   BaseMessage baseMessage,
                                                   Group group,
                                                   @NonNull AdditionParameter additionParameter);

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
    List<CometChatMessageComposerAction> getAttachmentOptions(Context context,
                                                              User user,
                                                              Group group,
                                                              HashMap<String, String> idMap,
                                                              @NonNull AdditionParameter additionParameter);

    /**
     * Returns a list of AI options for the message composer.
     *
     * @param context The context of the application.
     * @param user    The user object associated with the composer.
     * @param group   The group object associated with the composer.
     * @param idMap   The map of attachment IDs.
     * @return A list of CometChatMessageComposerAction objects representing the
     * available AI options.
     */
    List<CometChatMessageComposerAction> getAIOptions(Context context,
                                                      User user,
                                                      Group group,
                                                      HashMap<String, String> idMap,
                                                      AIOptionsStyle aiOptionsStyle,
                                                      @NonNull AdditionParameter additionParameter);

    /**
     * Returns the auxiliary option view for the message composer.
     *
     * @param context The context of the application.
     * @param user    The user object associated with the composer.
     * @param group   The group object associated with the composer.
     * @param id      The ID of the auxiliary option.
     * @return The auxiliary option view.
     */
    View getAuxiliaryOption(Context context, User user, Group group, HashMap<String, String> id, @NonNull AdditionParameter additionParameter);

    /**
     * Returns a list of common options for the message.
     *
     * @param context     The context of the application.
     * @param baseMessage The base message object.
     * @param group       The group object associated with the message.
     * @return A list of CometChatMessageOption objects representing the common
     * options for the message.
     */
    List<CometChatMessageOption> getCommonOptions(Context context,
                                                  BaseMessage baseMessage,
                                                  Group group,
                                                  @NonNull AdditionParameter additionParameter);

    /**
     * Returns a list of default message types.
     *
     * @return A list of default message types.
     */
    List<String> getDefaultMessageTypes(@NonNull AdditionParameter additionParameter);

    /**
     * Returns a list of default message categories.
     *
     * @return A list of default message categories.
     */
    List<String> getDefaultMessageCategories(@NonNull AdditionParameter additionParameter);

    SpannableString getLastConversationMessage(Context context, Conversation conversation, @NonNull AdditionParameter additionParameter);

    /**
     * Returns the auxiliary header menu view for the user or group.
     *
     * @param context The context of the application.
     * @param user    The user object associated with the header menu.
     * @param group   The group object associated with the header menu.
     * @return The auxiliary header menu view.
     */
    View getAuxiliaryHeaderMenu(Context context, User user, Group group, @NonNull AdditionParameter additionParameter);

    List<CometChatTextFormatter> getTextFormatters(Context context, @NonNull AdditionParameter additionParameter);

    /**
     * Returns the reply view container.
     *
     * @param context The context of the application.
     * @return The reply view container.
     */
    View getReplyViewContainer(Context context);


    /**
     * Returns the ID of the data source.
     *
     * @return The ID of the data source.
     */
    @Nullable
    String getId();

    /**
     * Binds the reply view container with the provided message and other parameters.
     *
     * @param context        The context of the application.
     * @param createdView    The created reply view container.
     * @param message        The base message object.
     * @param alignment      The message bubble alignment.
     * @param holder         The RecyclerView ViewHolder.
     * @param messageList    The list of base messages.
     * @param position       The position of the message in the list.
     * @param additionParameter Additional parameters for binding.
     */
    void bindReplyViewContainer(Context context, View createdView, BaseMessage message, UIKitConstants.MessageBubbleAlignment alignment, RecyclerView.ViewHolder holder, List<BaseMessage> messageList, int position, AdditionParameter additionParameter);
}
