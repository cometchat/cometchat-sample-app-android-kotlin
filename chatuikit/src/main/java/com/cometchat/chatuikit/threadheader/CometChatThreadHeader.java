package com.cometchat.chatuikit.threadheader;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatThreadHeaderBinding;
import com.cometchat.chatuikit.messagelist.MessageAdapter;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.formatters.CometChatMentionsFormatter;
import com.cometchat.chatuikit.shared.formatters.CometChatTextFormatter;
import com.cometchat.chatuikit.shared.framework.ChatConfigurator;
import com.cometchat.chatuikit.shared.models.AdditionParameter;
import com.cometchat.chatuikit.shared.models.CometChatMessageTemplate;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * CometChatThreadHeader is a custom view that extends MaterialCardView and
 * provides a header specifically designed for displaying threaded conversations
 * in CometChat.
 *
 * <p>
 * This view is intended to be used in a chat interface where threaded messages
 * are displayed. It offers various customization options and integrates with
 * CometChat's UI components and message formatters to present a consistent and
 * visually appealing chat experience. The header can display information about
 * the parent message, such as the sender's avatar, message content, and
 * timestamp. It also supports additional features such as mentions, reactions,
 * and message formatting.
 *
 * <p>
 * Key Features:
 *
 * <ul>
 * <li><b>Parent Message Display:</b> Displays the parent message of the thread,
 * showing the sender's avatar, message content, and timestamp to provide
 * context for the conversation.
 * <li><b>Customization Options:</b> Supports various customization settings
 * such as avatar visibility, message alignment, timestamp alignment, and custom
 * message bubble styles to match the app's design theme.
 * <li><b>Mentions and Reactions:</b> Integrates with CometChat's mentions and
 * reactions components to offer interactive and engaging chat experiences,
 * allowing users to interact with messages through reactions and tagged
 * mentions.
 * <li><b>Text Formatting:</b> Uses text formatters to style and format the
 * message content, including handling bold, italic, and other text attributes,
 * providing a rich text experience within the chat interface.
 * <li><b>Threaded Conversation Support:</b> Designed to work seamlessly with
 * CometChat's threading system, displaying and managing threads in a
 * user-friendly and intuitive manner.
 * </ul>
 *
 * <p>
 * Usage:
 *
 * <pre>
 * {
 * 	&#64;code
 * 	CometChatThreadHeader threadHeader = new CometChatThreadHeader(context);
 * 	threadHeader.setParentMessage(parentMessage);
 * }
 * </pre>
 *
 * <p>
 * This class also supports integration with view models and CometChat UI
 * components such as {@link CometChatMentionsFormatter},
 * {@link CometChatTextFormatter}, and {@link CometChatMessageTemplate} to
 * provide flexibility in configuring and managing threaded conversations within
 * the chat application.
 *
 * <p>
 * Extend and customize this class to fit specific UI needs or override default
 * behavior for threaded conversation headers in the CometChat SDK.
 */
public class CometChatThreadHeader extends MaterialCardView {
    private static final String TAG = CometChatThreadHeader.class.getSimpleName();
    /**
     * This class manages the thread header configuration and styling for displaying
     * messages.
     */
    private CometchatThreadHeaderBinding binding;

    /**
     * ViewModel for managing and observing threaded header data.
     */
    private ThreadHeaderViewModel threadHeaderViewModel;

    /**
     * Adapter for handling and displaying messages in the message list.
     */
    private MessageAdapter adapter;

    /**
     * Formatter for handling mentions within messages.
     */
    private CometChatMentionsFormatter cometchatMentionsFormatter;

    /**
     * The parent message that the thread is based on.
     */
    private BaseMessage parentMessage;

    /**
     * A property indicating whether reactions should be hidden in the thread.
     */
    private int reactionVisibility = VISIBLE;

    /**
     * The alignment of the message list.
     */
    private UIKitConstants.MessageListAlignment alignment = UIKitConstants.MessageListAlignment.STANDARD;

    /**
     * A property indicating whether avatars should be shown in the message list.
     */
    private int avatarVisibility = VISIBLE;

    /**
     * A SimpleDateFormat to format the date pattern for messages.
     */
    private SimpleDateFormat timePattern;

    /**
     * Additional parameters for customizing the message list.
     */
    private AdditionParameter additionParameter;

    /**
     * A list of text formatters to apply to the messages.
     */
    private List<CometChatTextFormatter> textFormatters;

    /**
     * A list of message templates for customizing message views.
     */
    private List<CometChatMessageTemplate> messageTemplates;

    /**
     * Style resource ID for the incoming message bubble.
     */
    private @StyleRes int incomingMessageBubbleStyle;

    /**
     * Style resource ID for the outgoing message bubble.
     */
    private @StyleRes int outgoingMessageBubbleStyle;

    private @Dimension int maxHeightLimit;
    private int receiptsVisibility = VISIBLE;
    private int replyCountVisibility = VISIBLE;
    private int replyCountBarVisibility = VISIBLE;

    /**
     * Constructs a new CometChatThreadHeader with the specified context.
     *
     * @param context The Context in which the view is running, used to access resources
     *                and other application-specific data.
     */
    public CometChatThreadHeader(Context context) {
        this(context, null);
    }

    /**
     * Constructs a new CometChatThreadHeader with the specified context and
     * attribute set.
     *
     * @param context The Context in which the view is running, used to access resources
     *                and other application-specific data.
     * @param attrs   A collection of attributes, as found associated with a tag in the
     *                XML that is inflating the view.
     */
    public CometChatThreadHeader(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatThreadHeaderStyle);
    }

    /**
     * Constructs a new CometChatThreadHeader with the specified context, attribute
     * set, and default style attribute.
     *
     * @param context      The Context in which the view is running, used to access resources
     *                     and other application-specific data.
     * @param attrs        A collection of attributes, as found associated with a tag in the
     *                     XML that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a reference to a
     *                     style resource that supplies default values for the view. Can be 0
     *                     to not look for defaults.
     */
    public CometChatThreadHeader(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateAndInitializeView(attrs, defStyleAttr);
    }

    /**
     * Initializes the CometChatThreadHeader view with the specified attributes and
     * default style.
     *
     * <p>
     * This method is responsible for setting up the initial state of the view,
     * configuring components, and applying styles based on the provided attributes
     * and default style attribute.
     *
     * @param attrs        The attribute set containing values from XML used to configure the
     *                     view.
     * @param defStyleAttr The default style attribute that defines styling defaults for the
     *                     view.
     */
    private void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        // Initialize material card properties
        Utils.initMaterialCard(this);

        // Initialize additional parameters
        additionParameter = new AdditionParameter();

        // Initialize text formatters and mentions formatter
        this.textFormatters = new ArrayList<>();
        processMentionsFormatter();
        setTextFormatters(null);

        // Load message templates based on the additional parameters
        messageTemplates = ChatConfigurator.getDataSource().getMessageTemplates(additionParameter);

        // Set up the message adapter for displaying messages in the thread
        adapter = new MessageAdapter(getContext(), new HashMap<>(), (list, message, cometchatMessageTemplate, cometchatMessageBubble) -> {
            // on a long click of parent bubble do nothing
        });
        adapter.setThreadVisibility(GONE);

        // Create the message map used by the adapter
        createMessageMap();

        // Inflate the layout and set up the RecyclerView
        binding = CometchatThreadHeaderBinding.inflate(LayoutInflater.from(getContext()), this, true);
        binding.rvParentBubbleView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvParentBubbleView.setAdapter(adapter);

        // Initialize and set up the ViewModel
        threadHeaderViewModel = new ViewModelProvider.NewInstanceFactory().create(ThreadHeaderViewModel.class);
        threadHeaderViewModel.getParentMessageListLiveData().observe((LifecycleOwner) getContext(), this::updateMessage);
        threadHeaderViewModel.getReplyCount().observe((LifecycleOwner) getContext(), this::updateReplyCount);

        // Apply additional style attributes to the view
        applyStyleAttributes(attrs, defStyleAttr, 0);
    }

    /**
     * Applies the style attributes defined in XML or programmatically to the
     * cometchatThreadedHeader view.
     *
     * @param attrs        The attributes defined in the XML layout.
     * @param defStyleAttr The default style attribute.
     * @param defStyleRes  The default style resource.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatThreadHeader, defStyleAttr, defStyleRes);
        @StyleRes int styleResId = typedArray.getResourceId(R.styleable.CometChatThreadHeader_cometchatThreadHeaderStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatThreadHeader, defStyleRes, styleResId);
        extractAttributesAndApplyDefaults(typedArray);
    }

    /**
     * Extracts the attributes from the TypedArray and applies default styles to the
     * view.
     *
     * @param typedArray The TypedArray containing the attribute values.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        try {
            setIncomingMessageBubbleStyle(typedArray.getResourceId(R.styleable.CometChatThreadHeader_cometchatThreadHeaderIncomingMessageBubbleStyle,
                                                                   0));
            setOutgoingMessageBubbleStyle(typedArray.getResourceId(R.styleable.CometChatThreadHeader_cometchatThreadHeaderOutgoingMessageBubbleStyle,
                                                                   0));
            setCardBackgroundColor(typedArray.getColor(R.styleable.CometChatThreadHeader_cometchatThreadHeaderBackgroundColor, 0));
            setStrokeColor(ColorStateList.valueOf(typedArray.getColor(R.styleable.CometChatThreadHeader_cometchatThreadHeaderStrokeColor, 0)));
            setStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatThreadHeader_cometchatThreadHeaderStrokeWidth, 0));
            setRadius(typedArray.getDimension(R.styleable.CometChatThreadHeader_cometchatThreadHeaderCornerRadius, 0));
            Drawable backgroundDrawable = typedArray.getDrawable(R.styleable.CometChatThreadHeader_cometchatThreadHeaderBackgroundDrawable);
            if (backgroundDrawable != null) setBackgroundDrawable(backgroundDrawable);
            setReplyCountBackgroundColor(typedArray.getColor(R.styleable.CometChatThreadHeader_cometchatThreadHeaderReplyCountBackgroundColor,
                                                             CometChatTheme.getExtendedPrimaryColor100(getContext())));
            setReplyCountTextAppearance(typedArray.getResourceId(R.styleable.CometChatThreadHeader_cometchatThreadHeaderReplyCountTextAppearance, 0));
            setReplyCountTextColor(typedArray.getColor(R.styleable.CometChatThreadHeader_cometchatThreadHeaderReplyCountTextColor, 0));
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Extracts and applies default styles for message bubbles based on whether it's
     * incoming or outgoing.
     *
     * @param typedArray The TypedArray containing the attributes for the message bubble.
     * @param isIncoming True if applying styles for incoming message bubbles, false for
     *                   outgoing.
     */
    private void extractAttributesAndApplyBubbleDefaults(TypedArray typedArray, boolean isIncoming) {
        try {
            if (isIncoming) {
                additionParameter.setIncomingTextBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatTextBubbleStyle,
                                                                                      0));
                additionParameter.setIncomingImageBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatImageBubbleStyle,
                                                                                       0));
                additionParameter.setIncomingFileBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatFileBubbleStyle,
                                                                                      0));
                additionParameter.setIncomingAudioBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatAudioBubbleStyle,
                                                                                       0));
                additionParameter.setIncomingVideoBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatVideoBubbleStyle,
                                                                                       0));
                additionParameter.setIncomingDeleteBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatDeleteBubbleStyle,
                                                                                        0));
                additionParameter.setIncomingPollBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatPollBubbleStyle,
                                                                                      0));
                additionParameter.setIncomingCollaborativeBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatCollaborativeBubbleStyle,
                                                                                               0));
                additionParameter.setIncomingMeetCallBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMeetCallBubbleStyle,
                                                                                          0));
                setIncomingMessageBubbleMentionsStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMessageBubbleMentionsStyle,
                                                                               0));
            } else {
                additionParameter.setOutgoingTextBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatTextBubbleStyle,
                                                                                      0));
                additionParameter.setOutgoingImageBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatImageBubbleStyle,
                                                                                       0));
                additionParameter.setOutgoingFileBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatFileBubbleStyle,
                                                                                      0));
                additionParameter.setOutgoingAudioBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatAudioBubbleStyle,
                                                                                       0));
                additionParameter.setOutgoingVideoBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatVideoBubbleStyle,
                                                                                       0));
                additionParameter.setOutgoingDeleteBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatDeleteBubbleStyle,
                                                                                        0));
                additionParameter.setOutgoingPollBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatPollBubbleStyle,
                                                                                      0));
                additionParameter.setOutgoingCollaborativeBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatCollaborativeBubbleStyle,
                                                                                               0));
                additionParameter.setOutgoingMeetCallBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMeetCallBubbleStyle,
                                                                                          0));
                setOutgoingMessageBubbleMentionsStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMessageBubbleMentionsStyle,
                                                                               0));
            }
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Sets the styles for the CometChatThreadHeader by applying a given style
     * resource.
     *
     * @param styleResId The style resource ID to be applied.
     */
    public void setStyle(@StyleRes int styleResId) {
        if (styleResId != 0) {
            TypedArray finalTypedArray = getContext().getTheme().obtainStyledAttributes(styleResId, R.styleable.CometChatThreadHeader);
            extractAttributesAndApplyDefaults(finalTypedArray);
        }
    }

    /**
     * Sets the text style for mentions in outgoing message bubbles.
     *
     * @param resourceId The resource ID of the style to be applied to mentions in outgoing
     *                   bubbles.
     */
    private void setOutgoingMessageBubbleMentionsStyle(@StyleRes int resourceId) {
        cometchatMentionsFormatter.setOutgoingBubbleMentionTextStyle(getContext(), resourceId);
    }

    /**
     * Sets the text style for mentions in incoming message bubbles.
     *
     * @param resourceId The resource ID of the style to be applied to mentions in incoming
     *                   bubbles.
     */
    private void setIncomingMessageBubbleMentionsStyle(@StyleRes int resourceId) {
        cometchatMentionsFormatter.setIncomingBubbleMentionTextStyle(getContext(), resourceId);
    }

    /**
     * Sets the background color for the reply count view.
     *
     * @param color The color to be applied as the background of the reply count view.
     */
    public void setReplyCountBackgroundColor(@ColorInt int color) {
        binding.tvReplies.setBackgroundColor(color);
    }

    /**
     * Sets the text color for the reply count view.
     *
     * @param color The color to be applied to the text of the reply count view.
     */
    public void setReplyCountTextColor(@ColorInt int color) {
        binding.tvReplies.setTextColor(color);
    }

    /**
     * Sets the text appearance for the reply count view.
     *
     * @param style The style resource ID to apply to the reply count text.
     */
    public void setReplyCountTextAppearance(@StyleRes int style) {
        binding.tvReplies.setTextAppearance(style);
    }

    /**
     * Measures the dimensions of the Threaded Header, enforcing the maximum height
     * limit if set.
     *
     * @param widthMeasureSpec  the width measurement specification
     * @param heightMeasureSpec the height measurement specification
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (maxHeightLimit != 0 && heightSize > maxHeightLimit) {
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeightLimit, heightMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Called when the view is attached to a window.
     *
     * <p>
     * This method is invoked when the view is being attached to a window. It calls
     * the superclass implementation and then adds a listener to the
     * {@link ThreadHeaderViewModel}. This listener is responsible for receiving
     * updates and changes related to the parent message and its replies while the
     * view is active.
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        threadHeaderViewModel.addListener();
    }

    /**
     * Updates the reply count displayed in the thread header.
     *
     * <p>
     * If the reply count is less than 2, it displays "1 reply"; otherwise, it
     * displays the number of replies with "replies". This method is called to
     * update the view whenever the reply count changes.
     *
     * @param integer The number of replies to display in the thread header.
     */
    private void updateReplyCount(Integer integer) {
        binding.tvReplies.setText(
                integer == 0 ? integer + " " + getContext().getResources().getString(R.string.cometchat_replies) : integer + " " + getContext().getResources().getString(integer < 2
                        ? R.string.cometchat_reply
                        : R.string.cometchat_replies)
        );
    }

    /**
     * Processes and adds the {@link CometChatMentionsFormatter} to the list of text
     * formatters if it's available in the data source. This method iterates through
     * the formatters provided by the data source and sets the
     * `cometchatMentionsFormatter` if found. It ensures that the mentions formatter
     * is included in the text formatters for consistent styling of mentions within
     * messages.
     */
    private void processMentionsFormatter() {
        for (CometChatTextFormatter textFormatter : CometChatUIKit.getDataSource().getTextFormatters(getContext(), additionParameter)) {
            if (textFormatter instanceof CometChatMentionsFormatter) {
                cometchatMentionsFormatter = (CometChatMentionsFormatter) textFormatter;
                break;
            }
        }
        this.textFormatters.add(cometchatMentionsFormatter);
    }

    /**
     * Adds a new message template to the Thread Header.
     *
     * @param template The {@link CometChatMessageTemplate} to be added. If the template
     *                 is not null, it will be added to the list of message templates,
     *                 and the templates will be set to update the view accordingly.
     */
    public void addTemplate(CometChatMessageTemplate template) {
        if (template != null) {
            messageTemplates.add(template);
            createMessageMap();
        }
    }

    /**
     * Creates a map of message templates and assigns them to the adapter.
     *
     * <p>
     * This method iterates through the list of message templates and builds two
     * hash maps: one for the view types and another for mapping each template based
     * on its category and type. These maps are then passed to the adapter for
     * efficient handling and rendering of the different message templates.
     */
    public void createMessageMap() {
        HashMap<String, Integer> messageViewTypes = new HashMap<>();
        HashMap<String, CometChatMessageTemplate> messageTemplateHashMap = new HashMap<>();
        int i = 1;
        for (CometChatMessageTemplate template : messageTemplates) {
            if (template != null && template.getCategory() != null && template.getType() != null) {
                messageTemplateHashMap.put(template.getCategory() + "_" + template.getType(), template);
                messageViewTypes.put(template.getCategory() + "_" + template.getType(), i);
                i++;
            }
        }
        adapter.setMessageTemplateHashMap(messageTemplateHashMap, messageViewTypes);
    }

    /**
     * Sets the list of message templates used in the Thread Header.
     *
     * <p>
     * This method replaces the current list of message templates with the provided
     * list. If the list is not null and not empty, the templates will be updated;
     * otherwise, it will reset to an empty list. After setting the templates, it
     * creates a map to manage these templates.
     *
     * @param cometchatMessageTemplates A list of {@link CometChatMessageTemplate} instances to set. If
     *                                  the list is null or empty, an empty list will be used.
     */
    public void setTemplates(List<CometChatMessageTemplate> cometchatMessageTemplates) {
        if (cometchatMessageTemplates != null) {
            if (!cometchatMessageTemplates.isEmpty()) {
                messageTemplates = cometchatMessageTemplates;
            } else {
                messageTemplates = new ArrayList<>();
            }
            createMessageMap();
        }
    }

    public void setLeftBubbleMargin(int top, int bottom, int left, int right) {
        adapter.setLeftBubbleMargin(top, bottom, left, right);
    }

    public void setRightBubbleMargin(int top, int bottom, int left, int right) {
        adapter.setRightBubbleMargin(top, bottom, left, right);
    }

    /**
     * Updates the messages displayed in the thread.
     *
     * <p>
     * This method is called when the list of messages in the thread is updated. It
     * passes the new list of messages to the adapter to refresh the view.
     *
     * @param message A list of {@link BaseMessage} objects representing the messages in
     *                the thread.
     */
    private void updateMessage(List<BaseMessage> message) {
        adapter.setBaseMessageList(message);
    }

    /**
     * Called when the view is detached from a window.
     *
     * <p>
     * This method is invoked when the view is being detached from a window. It
     * calls the superclass implementation and then removes the listener from the
     * {@link ThreadHeaderViewModel}. This is done to prevent memory leaks and
     * ensure that the view model no longer sends updates to a view that is no
     * longer displayed.
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        threadHeaderViewModel.removeListener();
    }

    /**
     * Returns the RecyclerView used for displaying the parent message bubble.
     *
     * @return The {@link RecyclerView} instance for the parent message bubble view.
     */
    public RecyclerView getRecyclerView() {
        return binding.rvParentBubbleView;
    }

    /**
     * Returns the binding object associated with the Thread Header.
     *
     * @return The {@link CometchatThreadHeaderBinding} instance for this view.
     */
    public CometchatThreadHeaderBinding getBinding() {
        return binding;
    }

    /**
     * Returns the view model used for managing the state of the thread header.
     *
     * @return The {@link ThreadHeaderViewModel} instance associated with the
     * thread header.
     */
    public ThreadHeaderViewModel getThreadedHeaderViewModel() {
        return threadHeaderViewModel;
    }

    /**
     * Returns the message adapter used in the Thread Header.
     *
     * @return The {@link MessageAdapter} instance for managing messages in the
     * thread.
     */
    public MessageAdapter getAdapter() {
        return adapter;
    }

    /**
     * Returns the mentions formatter used for styling mentions in messages.
     *
     * @return The {@link CometChatMentionsFormatter} instance.
     */
    public CometChatMentionsFormatter getCometchatMentionsFormatter() {
        return cometchatMentionsFormatter;
    }

    /**
     * Returns the parent message of the thread.
     *
     * @return The {@link BaseMessage} instance representing the parent message.
     */
    public BaseMessage getParentMessage() {
        return parentMessage;
    }

    /**
     * Sets the parent message for the thread and updates the view model
     * accordingly.
     *
     * <p>
     * This method is used to set the parent message of the thread and notify the
     * threaded header's view model of this change. The parent message provides
     * context for the threaded conversation and is essential for displaying the
     * correct message details. Additionally, this method triggers the processing of
     * formatters to apply any necessary formatting or styling to the message.
     *
     * @param message The parent message for the thread. Must not be null.
     */
    public void setParentMessage(@NonNull BaseMessage message) {
        this.parentMessage = message;
        threadHeaderViewModel.setParentMessage(message);
        processFormatters();
    }

    /**
     * Processes the text formatters by updating the {@link AdditionParameter} with
     * the current list of text formatters.
     *
     * <p>
     * This method ensures that the {@link AdditionParameter} is always up-to-date
     * with the current list of text formatters, allowing the thread view to apply
     * formatting consistently.
     */
    private void processFormatters() {
        additionParameter.setTextFormatters(textFormatters);
    }

    /**
     * Returns the alignment of the message list in the Thread Header.
     *
     * @return The {@link UIKitConstants.MessageListAlignment} enum representing the
     * message alignment.
     */
    public UIKitConstants.MessageListAlignment getAlignment() {
        return alignment;
    }

    /**
     * Sets the alignment of messages in the Thread Header.
     *
     * @param alignment The {@link UIKitConstants.MessageListAlignment} enum representing
     *                  the alignment of the messages (e.g., left, right, or standard
     *                  alignment).
     */
    public void setAlignment(UIKitConstants.MessageListAlignment alignment) {
        this.alignment = alignment;
        adapter.setAlignment(alignment);
    }

    /**
     * Returns the date pattern function used for formatting message dates.
     *
     * @return The {@link SimpleDateFormat} object representing the date pattern function.
     */
    public SimpleDateFormat getTimePattern() {
        return timePattern;
    }

    /**
     * Sets the date pattern for displaying message dates in the Thread Header.
     *
     * @param timePattern The {@link SimpleDateFormat} object representing the function that
     *                    formats the date for each message. This allows customization of
     *                    the date display format.
     */
    public void setTimeFormat(SimpleDateFormat timePattern) {
        this.timePattern = timePattern;
        adapter.setTimeFormat(timePattern);
    }

    /**
     * Returns the addition parameter used for storing additional settings and
     * configurations.
     *
     * @return The {@link AdditionParameter} instance.
     */
    public AdditionParameter getAdditionParameter() {
        return additionParameter;
    }

    /**
     * Returns the list of text formatters used in the Thread Header.
     *
     * @return A list of {@link CometChatTextFormatter} instances.
     */
    public List<CometChatTextFormatter> getTextFormatters() {
        return textFormatters;
    }

    /**
     * Sets the list of custom text formatters.
     *
     * <p>
     * This method allows the addition of custom text formatters to the current list
     * of formatters used in the Thread Header. If the provided list is not null,
     * these formatters will be added to the existing ones and processed
     * accordingly.
     *
     * @param cometchatTextFormatters A list of {@link CometChatTextFormatter} to add to the current
     *                                list of formatters.
     */
    public void setTextFormatters(List<CometChatTextFormatter> cometchatTextFormatters) {
        if (cometchatTextFormatters != null) {
            this.textFormatters.addAll(cometchatTextFormatters);
            processFormatters();
        }
    }

    /**
     * Returns the list of message templates used in the Thread Header.
     *
     * @return A list of {@link CometChatMessageTemplate} instances.
     */
    public List<CometChatMessageTemplate> getMessageTemplates() {
        return messageTemplates;
    }

    /**
     * Returns the style resource ID for incoming message bubbles.
     *
     * @return The style resource ID for incoming message bubbles.
     */
    public int getIncomingMessageBubbleStyle() {
        return incomingMessageBubbleStyle;
    }

    /**
     * Sets the incoming message bubble style and applies the extracted styles.
     *
     * @param incomingMessageBubbleStyle The style resource ID for the incoming message bubble.
     */
    public void setIncomingMessageBubbleStyle(@StyleRes int incomingMessageBubbleStyle) {
        if (incomingMessageBubbleStyle != 0) {
            TypedArray typedArray = getContext()
                .getTheme()
                .obtainStyledAttributes(null,
                                        R.styleable.CometChatMessageBubble,
                                        R.attr.cometchatThreadHeaderIncomingMessageBubbleStyle,
                                        incomingMessageBubbleStyle);
            extractAttributesAndApplyBubbleDefaults(typedArray, true);
            adapter.setIncomingMessageBubbleStyle(incomingMessageBubbleStyle);
            this.incomingMessageBubbleStyle = incomingMessageBubbleStyle;
        }
    }

    /**
     * Returns the style resource ID for outgoing message bubbles.
     *
     * @return The style resource ID for outgoing message bubbles.
     */
    public int getOutgoingMessageBubbleStyle() {
        return outgoingMessageBubbleStyle;
    }

    /**
     * Sets the outgoing message bubble style and applies the extracted styles.
     *
     * @param outgoingMessageBubbleStyle The style resource ID for the outgoing message bubble.
     */
    public void setOutgoingMessageBubbleStyle(@StyleRes int outgoingMessageBubbleStyle) {
        if (outgoingMessageBubbleStyle != 0) {
            TypedArray typedArray = getContext()
                .getTheme()
                .obtainStyledAttributes(null,
                                        R.styleable.CometChatMessageBubble,
                                        R.attr.cometchatThreadHeaderOutgoingMessageBubbleStyle,
                                        outgoingMessageBubbleStyle);
            extractAttributesAndApplyBubbleDefaults(typedArray, false);
            adapter.setOutgoingMessageBubbleStyle(outgoingMessageBubbleStyle);
            this.outgoingMessageBubbleStyle = outgoingMessageBubbleStyle;
        }
    }

    /**
     * Retrieves the visibility status of reactions.
     *
     * @return An integer representing the visibility of reactions.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getReactionVisibility() {
        return reactionVisibility;
    }

    /**
     * Sets whether reactions should be hidden in the Thread Header.
     *
     * <p>
     * This method controls whether reactions to messages are shown or hidden. It
     * updates the view model and the adapter to reflect the change.
     *
     * @param visibility GONE to hide reactions, VISIBLE to show them.
     */
    public void setReactionVisibility(int visibility) {
        this.reactionVisibility = visibility;
        threadHeaderViewModel.setHideReaction(visibility == GONE);
        adapter.disableReactions(visibility == GONE);
    }

    /**
     * Retrieves the visibility status of the avatar.
     *
     * @return An integer representing the visibility of the avatar.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getAvatarVisibility() {
        return avatarVisibility;
    }

    /**
     * Shows or hides the avatar in the Thread Header.
     *
     * @param visibility GONE to hide the avatar, VISIBLE to show it. Controls whether the
     *                   sender's avatar appears alongside messages in the thread.
     */
    public void setAvatarVisibility(int visibility) {
        this.avatarVisibility = visibility;
        adapter.showAvatar(visibility == VISIBLE);
    }

    /**
     * Retrieves the maximum height limit.
     *
     * @return An integer representing the maximum height limit.
     */
    public int getMaxHeight() {
        return maxHeightLimit;
    }

    /**
     * Sets the maximum height limit for the suggestion list.
     *
     * @param maxHeightLimit the maximum height limit in pixels
     */
    public void setMaxHeight(@Dimension int maxHeightLimit) {
        if (maxHeightLimit > 0) {
            this.maxHeightLimit = maxHeightLimit;
        }
    }

    /**
     * Retrieves the visibility status of read receipts.
     *
     * @return An integer representing the visibility of read receipts.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getReceiptsVisibility() {
        return receiptsVisibility;
    }

    /**
     * Hide or show the read receipt in the Message list view.
     *
     * @param visibility GONE to hide the read receipt, VISIBLE to show it.
     */
    public void setReceiptsVisibility(int visibility) {
        this.receiptsVisibility = visibility;
        adapter.hideReceipts(visibility != VISIBLE);
    }

    /**
     * Retrieves the visibility status of the reply count.
     *
     * @return An integer representing the visibility of the reply count.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getReplyCountVisibility() {
        return replyCountVisibility;
    }

    /**
     * Hide or show the reply count in the Thread Header.
     *
     * @param visibility GONE to hide the reply count, VISIBLE to show it.
     */
    public void setReplyCountVisibility(int visibility) {
        this.replyCountVisibility = visibility;
        binding.tvReplies.setVisibility(visibility);
    }

    /**
     * Retrieves the visibility status of the reply count bar.
     *
     * @return An integer representing the visibility of the reply count bar.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getReplyCountBarVisibility() {
        return replyCountBarVisibility;
    }

    /**
     * Hide or show the reply count bar in the Thread Header.
     *
     * @param visibility GONE to hide the reply count bar, VISIBLE to show it.
     */
    public void setReplyCountBarVisibility(int visibility) {
        this.replyCountBarVisibility = visibility;
        binding.repliesLayout.setVisibility(visibility);
    }

}
