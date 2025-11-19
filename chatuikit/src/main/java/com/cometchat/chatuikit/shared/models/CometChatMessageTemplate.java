package com.cometchat.chatuikit.shared.models;

import android.content.Context;

import androidx.annotation.NonNull;

import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chatuikit.shared.interfaces.Function3;
import com.cometchat.chatuikit.shared.viewholders.MessagesViewHolderListener;

import java.util.ArrayList;
import java.util.List;

/**
 * The CometChatMessageTemplate class represents a template for creating
 * customized message views in a chat application. <br>
 * It provides a flexible way to define and customize various components of a
 * message bubble view, such as the message bubble, header, content, bottom, and
 * footer views, as well as options associated with the message. <br>
 * By using this template, developers can create rich and dynamic chat message
 * views that align with their application's design and functionality
 * requirements. <br>
 * example :
 *
 * <pre>
 * {
 * 	&#64;code
 * 	CometChatMessageTemplate template = new CometChatMessageTemplate()
 * 			.setCategory(CometChatConstants.CATEGORY_MESSAGE).setType(CometChatConstants.MESSAGE_TYPE_TEXT)
 * 			.setOptions((context, baseMessage, group) -> ChatConfigurator.getUtils().getTextMessageOptions(context,
 * 					baseMessage, group))
 * 			.setBottomView((context, baseMessage, isLeftAlign) -> ChatConfigurator.getUtils().getBottomView(context,
 * 					baseMessage, isLeftAlign))
 * 			.setContentView((context, baseMessage, isLeftAlign) -> {
 * 				if (baseMessage.getDeletedAt() == 0) {
 * 					if (UIKitConstants.MessageBubbleAlignment.LEFT.equals(isLeftAlign)) {
 * 						return ChatConfigurator.getUtils().getTextBubbleContentView(context,
 * 								(TextMessage) baseMessage, null, Gravity.START, isLeftAlign);
 *                    } else {
 * 						return ChatConfigurator.getUtils().getTextBubbleContentView(context,
 * 								(TextMessage) baseMessage, null, Gravity.END, isLeftAlign);
 *                    }
 *                } else
 * 					return ChatConfigurator.getUtils().getDeleteMessageBubble(context, isLeftAlign);
 *            });
 * }
 * </pre>
 */
public class CometChatMessageTemplate {
    private static final String TAG = CometChatMessageTemplate.class.getSimpleName();
    private String category;
    private String type;
    private Function3<Context, BaseMessage, Group, List<CometChatMessageOption>> options;

    private MessagesViewHolderListener replyView;
    private MessagesViewHolderListener contentView;
    private MessagesViewHolderListener bubbleView;
    private MessagesViewHolderListener headerView;
    private MessagesViewHolderListener bottomView;
    private MessagesViewHolderListener statusInfoView;
    private MessagesViewHolderListener footerView;

    /**
     * Sets the reply view for the CometChatMessageTemplate.
     *
     * @param messageTemplateCallBack The listener interface that defines callbacks for the reply view.
     * @return The current CometChatMessageTemplate instance to enable method
     * chaining. This method allows you to specify a custom view to be used
     * as the reply section above the message bubble for this chat message
     * template. The provided listener interface
     * (`MessagesViewHolderListener`) defines callbacks that will be invoked
     * when various interactions occur with the reply view, similar to the
     * bubble and content views. By implementing the
     * `MessagesViewHolderListener` interface and passing an instance to
     * this method, you can customize the behavior and appearance of the
     * reply view for your specific needs.
     */
    public CometChatMessageTemplate setReplyView(MessagesViewHolderListener messageTemplateCallBack) {
        this.replyView = messageTemplateCallBack;
        return this;
    }

    /**
     * Sets the content view for the CometChatMessageTemplate.
     *
     * @param messageTemplateCallBack The listener interface that defines callbacks for the content
     *                                view.
     * @return The current CometChatMessageTemplate instance to enable method
     * chaining. This method allows you to specify a custom view to be used
     * as the content area within the message bubble for this chat message
     * template. The provided listener interface
     * (`MessagesViewHolderListener`) defines callbacks that will be invoked
     * when various actions occur on the content view, similar to the bubble
     * view. By implementing the `MessagesViewHolderListener` interface and
     * passing an instance to this method, you can customize the behavior
     * and appearance of the content view within the message bubble for your
     * specific needs.
     */
    public CometChatMessageTemplate setContentView(MessagesViewHolderListener messageTemplateCallBack) {
        this.contentView = messageTemplateCallBack;
        return this;
    }

    /**
     * Sets the bubble view for the CometChatMessageTemplate.
     *
     * @param messageTemplateCallBack The listener interface that defines callbacks for the bubble view.
     * @return The current CometChatMessageTemplate instance to enable method
     * chaining. This method allows you to specify a custom view to be used
     * as the message bubble for this chat message template. The provided
     * listener interface (`MessagesViewHolderListener`) defines callbacks
     * that will be invoked when various actions occur on the bubble view,
     * such as: * Clicking on the message bubble * Long pressing on the
     * message bubble * Displaying information about the sender or recipient
     * * Handling other interactions with the message bubble By implementing
     * the `MessagesViewHolderListener` interface and passing an instance to
     * this method, you can customize the behavior and appearance of the
     * message bubble for your specific needs.
     */
    public CometChatMessageTemplate setBubbleView(MessagesViewHolderListener messageTemplateCallBack) {
        this.bubbleView = messageTemplateCallBack;
        return this;
    }

    /**
     * Sets the header view for the CometChatMessageTemplate.
     *
     * @param messageTemplateCallBack The listener interface that defines callbacks for the header view.
     * @return The current CometChatMessageTemplate instance to enable method
     * chaining. This method allows you to specify a custom view to be used
     * as the header section above the message bubble for this chat message
     * template. The provided listener interface
     * (`MessagesViewHolderListener`) defines callbacks that will be invoked
     * when various interactions occur with the header view, similar to the
     * bubble and content views. By implementing the
     * `MessagesViewHolderListener` interface and passing an instance to
     * this method, you can customize the behavior and appearance of the
     * header view for your specific needs.
     */
    public CometChatMessageTemplate setHeaderView(MessagesViewHolderListener messageTemplateCallBack) {
        this.headerView = messageTemplateCallBack;
        return this;
    }

    /**
     * Sets the bottom view for the CometChatMessageTemplate.
     *
     * @param messageTemplateCallBack The listener interface that defines callbacks for the bottom view.
     * @return The current CometChatMessageTemplate instance to enable method
     * chaining. This method allows you to specify a custom view to be used
     * as the bottom section below the message bubble for this chat message
     * template. The provided listener interface
     * (`MessagesViewHolderListener`) defines callbacks that will be invoked
     * when various interactions occur with the bottom view, similar to the
     * bubble, content, and header views. By implementing the
     * `MessagesViewHolderListener` interface and passing an instance to
     * this method, you can customize the behavior and appearance of the
     * bottom view for your specific needs.
     */
    public CometChatMessageTemplate setBottomView(MessagesViewHolderListener messageTemplateCallBack) {
        this.bottomView = messageTemplateCallBack;
        return this;
    }

    /**
     * Sets the status info view for the CometChatMessageTemplate.
     *
     * @param messageTemplateCallBack The listener interface that defines callbacks for the status info
     *                                view.
     * @return The current CometChatMessageTemplate instance to enable method
     * chaining. This method allows you to specify a custom view to be used
     * as the status info section below the message bubble for this chat
     * message template. The provided listener interface
     * (`MessagesViewHolderListener`) defines callbacks that will be invoked
     * when various interactions occur with the status info view, similar to
     * the bubble, content, and header views. By implementing the
     * `MessagesViewHolderListener` interface and passing an instance to
     * this method, you can customize the behavior and appearance of the
     * status info view for your specific needs.
     */
    public CometChatMessageTemplate setStatusInfoView(MessagesViewHolderListener messageTemplateCallBack) {
        this.statusInfoView = messageTemplateCallBack;
        return this;
    }

    /**
     * Sets the footer view for the CometChatMessageTemplate.
     *
     * @param messageTemplateCallBack The listener interface that defines callbacks for the footer view.
     * @return The current CometChatMessageTemplate instance to enable method
     * chaining. This method allows you to specify a custom view to be used
     * as the footer section below the message bubble for this chat message
     * template. The provided listener interface
     * (`MessagesViewHolderListener`) defines callbacks that will be invoked
     * when various interactions occur with the footer view, similar to the
     * bubble, content, header, and bottom views. By implementing the
     * `MessagesViewHolderListener` interface and passing an instance to
     * this method, you can customize the behavior and appearance of the
     * footer view for your specific needs.
     */
    public CometChatMessageTemplate setFooterView(MessagesViewHolderListener messageTemplateCallBack) {
        this.footerView = messageTemplateCallBack;
        return this;
    }

    /**
     * Sets the category of the message.
     *
     * @param category the category of the message
     * @return the current instance of CometChatMessageTemplate
     */
    public CometChatMessageTemplate setCategory(String category) {
        this.category = category;
        return this;
    }

    /**
     * Sets the type of the message.
     *
     * @param type the type of the message
     * @return the current instance of CometChatMessageTemplate
     */
    public CometChatMessageTemplate setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Sets the functional interface to retrieve the options associated with the
     * message.
     *
     * @param options the functional interface to retrieve the options
     * @return the current instance of CometChatMessageTemplate
     */
    @NonNull
    public CometChatMessageTemplate setOptions(Function3<Context, BaseMessage, Group, List<CometChatMessageOption>> options) {
        this.options = options;
        return this;
    }

    /**
     * Retrieves the category of the message.
     *
     * @return the category of the message
     */
    public String getCategory() {
        return category;
    }

    /**
     * Retrieves the type of the message.
     *
     * @return the type of the message
     */
    public String getType() {
        return type;
    }

    public Function3<Context, BaseMessage, Group, List<CometChatMessageOption>> getOptions() {
        return options;
    }

    /**
     * Retrieves the options associated with the message.
     *
     * @param context     the context of the application
     * @param baseMessage the base message for which the options are retrieved
     * @param group       the group associated with the message
     * @return the list of options associated with the message
     */
    public List<CometChatMessageOption> getOptions(Context context, BaseMessage baseMessage, Group group) {
        List<CometChatMessageOption> optionList = new ArrayList<>();
        if (options != null) {
            optionList = options.apply(context, baseMessage, group);
        }
        return optionList;
    }

    public MessagesViewHolderListener getReplyView() {
        return replyView;
    }

    public MessagesViewHolderListener getContentView() {
        return contentView;
    }

    public MessagesViewHolderListener getBubbleView() {
        return bubbleView;
    }

    public MessagesViewHolderListener getHeaderView() {
        return headerView;
    }

    public MessagesViewHolderListener getBottomView() {
        return bottomView;
    }

    public MessagesViewHolderListener getStatusInfoView() {
        return statusInfoView;
    }

    public MessagesViewHolderListener getFooterView() {
        return footerView;
    }

    @NonNull
    public CometChatMessageTemplate clone() {
        CometChatMessageTemplate messageTemplate = new CometChatMessageTemplate();
        messageTemplate.setReplyView(getReplyView());
        messageTemplate.setBubbleView(getBubbleView());
        messageTemplate.setContentView(getContentView());
        messageTemplate.setStatusInfoView(getStatusInfoView());
        messageTemplate.setFooterView(getFooterView());
        messageTemplate.setBottomView(getBottomView());

        messageTemplate.setCategory(getCategory());
        messageTemplate.setType(getType());
        messageTemplate.setOptions(getOptions());
        return messageTemplate;
    }
}
