package com.cometchat.chatuikit.messagelist;

import android.app.Activity;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.core.MessagesRequest;
import com.cometchat.chat.core.ReactionsRequest;
import com.cometchat.chat.enums.ModerationStatus;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.Action;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.FlagReason;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.ReactionCount;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.report.CometChatFlagMessageDialog;
import com.cometchat.chatuikit.shared.constants.MessageStatus;
import com.cometchat.chatuikit.shared.interfaces.ToolCallListener;
import com.cometchat.chatuikit.shared.ai.CometChatAIStreamService;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.messageinformation.CometChatMessageInformation;
import com.cometchat.chatuikit.reactionlist.CometChatReactionList;
import com.cometchat.chatuikit.reactionlist.OnReactionListItemClick;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.formatters.CometChatMentionsFormatter;
import com.cometchat.chatuikit.shared.formatters.CometChatTextFormatter;
import com.cometchat.chatuikit.shared.formatters.FormatterUtils;
import com.cometchat.chatuikit.shared.framework.ChatConfigurator;
import com.cometchat.chatuikit.shared.interfaces.DateTimeFormatterCallback;
import com.cometchat.chatuikit.shared.interfaces.EmojiPickerClickListener;
import com.cometchat.chatuikit.shared.interfaces.Function1;
import com.cometchat.chatuikit.shared.interfaces.MessageOptionClickListener;
import com.cometchat.chatuikit.shared.interfaces.OnEmpty;
import com.cometchat.chatuikit.shared.interfaces.OnError;
import com.cometchat.chatuikit.shared.interfaces.OnLoad;
import com.cometchat.chatuikit.shared.interfaces.ReactionClickListener;
import com.cometchat.chatuikit.shared.models.AdditionParameter;
import com.cometchat.chatuikit.shared.models.CometChatMessageOption;
import com.cometchat.chatuikit.shared.models.CometChatMessageTemplate;
import com.cometchat.chatuikit.shared.resources.soundmanager.CometChatSoundManager;
import com.cometchat.chatuikit.shared.resources.soundmanager.Sound;
import com.cometchat.chatuikit.shared.resources.utils.AudioPlayer;
import com.cometchat.chatuikit.shared.resources.utils.MediaUtils;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.custom_dialog.CometChatConfirmDialog;
import com.cometchat.chatuikit.shared.resources.utils.sticker_header.StickyHeaderDecoration;
import com.cometchat.chatuikit.shared.resources.utils.swipetoreply.SwipeActions;
import com.cometchat.chatuikit.shared.resources.utils.swipetoreply.SwipeController;
import com.cometchat.chatuikit.shared.resources.utils.unread_message_decoration.NewMessageIndicatorDecoration;
import com.cometchat.chatuikit.shared.views.aiconversationstarter.CometChatAIConversationStarterView;
import com.cometchat.chatuikit.shared.views.aiconversationsummary.view.CometChatAIConversationSummaryView;
import com.cometchat.chatuikit.shared.views.aismartreplies.CometChatAISmartRepliesView;
import com.cometchat.chatuikit.shared.views.avatar.CometChatAvatar;
import com.cometchat.chatuikit.shared.views.badge.CometChatBadge;
import com.cometchat.chatuikit.shared.views.messagebubble.CometChatMessageBubble;
import com.cometchat.chatuikit.shared.views.optionsheet.OptionSheetMenuItem;
import com.cometchat.chatuikit.shared.views.reaction.emojikeyboard.CometChatEmojiKeyboard;
import com.cometchat.chatuikit.shared.views.reaction.emojikeyboard.EmojiKeyBoardView;
import com.cometchat.chatuikit.shared.views.reaction.interfaces.OnAddMoreReactionsClick;
import com.cometchat.chatuikit.shared.views.reaction.interfaces.OnReactionClick;
import com.cometchat.chatuikit.shared.views.reaction.interfaces.OnReactionLongClick;
import com.cometchat.chatuikit.shimmer.CometChatShimmerAdapter;
import com.cometchat.chatuikit.shimmer.CometChatShimmerFrameLayout;
import com.cometchat.chatuikit.shimmer.CometChatShimmerUtils;
import com.cometchat.chatuikit.threadheader.CometChatThreadHeader;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CometChatMessageList is a custom view that extends {@link MaterialCardView}
 * and implements {@link MessageAdapter.OnMessageLongClick}. This class is
 * responsible for displaying a list of messages in a chat interface with
 * various custom UI elements like headers, footers, message indicators, and
 * error states. It manages the message templates, reactions, and user
 * interactions such as long-click events.
 *
 * <p>
 * It supports custom views for loading, empty, and error states, as well as
 * configurations for sound, style, and other customizable options like bubble
 * styles and action sheets.
 *
 * <p>
 * Key features include:
 *
 * <ul>
 * <li>Displaying chat messages in a RecyclerView with sticky headers.
 * <li>Handling long-click actions on messages via the
 * {@link MessageAdapter.OnMessageLongClick} interface.
 * <li>Supporting custom reactions and mentions in chat messages.
 * <li>Providing custom error handling and UI customization options.
 * </ul>
 */
public class CometChatMessageList extends MaterialCardView implements MessageAdapter.OnMessageLongClick {
    private static final String TAG = CometChatMessageList.class.getSimpleName();
    private boolean isDetachedFromWindow;

    private LifecycleOwner lifecycleOwner;
    private final HashMap<String, Integer> messageViewTypes = new HashMap<>();
    private final HashMap<String, String> messageTypesToRetrieve = new HashMap<>();
    private final HashMap<String, String> messageCategoriesToRetrieve = new HashMap<>();
    private final HashMap<String, CometChatMessageTemplate> messageTemplateHashMap = new HashMap<>();
    private boolean enableConversationStarter = false;
    private boolean enableSmartReplies = false;
    private int replyInThreadOptionVisibility = VISIBLE;
    private int replyOptionVisibility = VISIBLE;
    private int translateMessageOptionVisibility = VISIBLE;
    private int copyMessageOptionVisibility = VISIBLE;
    private int editMessageOptionVisibility = VISIBLE;
    private int shareMessageOptionVisibility = VISIBLE;
    private int messagePrivatelyOptionVisibility = VISIBLE;
    private int deleteMessageOptionVisibility = VISIBLE;
    private int messageInfoOptionVisibility = VISIBLE;
    private int groupActionMessageVisibility = VISIBLE;
    private int messageReactionOptionVisibility = VISIBLE;
    private int flagRemarkInputFieldVisibility = VISIBLE;
    private int avatarVisibility = VISIBLE;
    private int receiptsVisibility = VISIBLE;
    private int moderationViewVisibility = VISIBLE;
    private int markAsUnreadOptionVisibility = GONE;

    private boolean startFromUnreadMessages = false;
    // User and Group
    private User user;
    private Group group;
    // Sound Settings
    private boolean disableSoundForMessages;
    private @RawRes int customSoundForMessages;
    private CometChatSoundManager soundManager;
    // Message and Template Management
    private BaseMessage baseMessage;
    private long gotoMessageId = -1;
    private CometChatMessageBubble messageBubble;
    private CometChatMessageTemplate messageTemplate;
    private List<CometChatMessageOption> customOption = new ArrayList<>();
    private List<CometChatMessageTemplate> messageTemplates = new ArrayList<>();
    // Message List and Adapter
    private RecyclerView rvChatListView;
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private StickyHeaderDecoration stickyHeaderDecoration;
    private NewMessageIndicatorDecoration newMessageIndicatorDecoration;
    private boolean autoFetch = true;
    private boolean isAgentChat = false;
    private boolean hasMore;
    private boolean hasMoreNewMessages;
    private boolean hasMorePreviousMessages;
    private boolean isScrolling;
    private boolean isInProgress;
    private boolean scrollToBottomOnNewMessage;
    private boolean swipeToReplyEnabled = true;
    private ItemTouchHelper itemTouchHelper;
    // UI Components - Headers, Footers, and Indicators
    private LinearLayout headerView, footerView;
    private MaterialCardView newMessageLayout;
    private CometChatBadge badge;
    private ImageView newMessageIndicatorIcon;
    private int newMessageCount = 0;
    // Custom Views (Empty, Error, Loading)
    private View customEmptyView;
    private View customErrorView;
    private View customLoadingView;
    private View customAIAssistantEmptyChatGreetingView;
    private int errorStateVisibility = VISIBLE;
    private CometChatAvatar aiAssistantEmptyChatGreetingImageView;
    private TextView aiAssistantEmptyChatGreetingTextView, aiAssistantEmptyChatGreetingSubtitleTextView;
    private @ColorInt int aiAssistantEmptyChatTitleTextColor;
    private @ColorInt int aiAssistantEmptyChatSubtitleTextColor;
    private @StyleRes int aiAssistantEmptyChatTitleTextAppearance;
    private @StyleRes int aiAssistantEmptyChatSubtitleTextAppearance;
    private TextView errorTextView, errorSubtitleTextView;
    private @ColorInt int errorStateTitleTextColor;
    private @ColorInt int errorStateSubtitleTextColor;
    private @StyleRes int errorStateTitleTextAppearance;
    private @StyleRes int errorStateSubtitleTextAppearance;

    // Shimmer Layouts and Adapters
    private LinearLayout shimmerParentLayout;
    private RecyclerView shimmerRecyclerviewMessageListList;
    private CometChatShimmerFrameLayout shimmerEffectFrame;
    // Custom Panels (Top, Bottom)
    private View internalBottomPanel, internalTopPanel;
    private RelativeLayout messageListLayout;
    // Reactions and Mentions
    private CometChatEmojiKeyboard emojiKeyboard;
    private CometChatMentionsFormatter cometchatMentionsFormatter;
    private boolean disableMentionAll;
    private List<String> quickReactions;
    private @DrawableRes int addReactionIcon;
    // Dialogs and Alerts
    private CometChatMessageInformation cometchatMessageInformation;
    private CometChatConfirmDialog deleteAlertDialog;
    private CometChatFlagMessageDialog flagMessageDialog;
    /**
     * Observer for monitoring the deletion state of a message.
     *
     * <p>
     * This observer listens for changes in the message deletion state and performs
     * actions based on the result. If the message is successfully deleted, the
     * baseMessage is set to null. If the deletion fails, an error is displayed.
     */
    Observer<UIKitConstants.DeleteState> messageDeleteObserver = new Observer<UIKitConstants.DeleteState>() {
        @Override
        public void onChanged(UIKitConstants.DeleteState progressState) {
            if (UIKitConstants.DeleteState.INITIATED_DELETE.equals(progressState)) {
                if (deleteAlertDialog != null) deleteAlertDialog.hidePositiveButtonProgressBar(false);
            } else if (UIKitConstants.DeleteState.SUCCESS_DELETE.equals(progressState)) {
                if (deleteAlertDialog != null) {
                    deleteAlertDialog.dismiss();
                    deleteAlertDialog = null;
                }
                baseMessage = null;
            } else if (UIKitConstants.DeleteState.FAILURE_DELETE.equals(progressState)) {
                if (deleteAlertDialog != null) {
                    deleteAlertDialog.dismiss();
                    deleteAlertDialog = null;
                }
                baseMessage = null;
                Toast.makeText(getContext(), getContext().getString(R.string.cometchat_message_delete_error), Toast.LENGTH_SHORT).show();
            }
        }
    };

    Observer<UIKitConstants.FlagMessageState> flagMessageObserver = new Observer<UIKitConstants.FlagMessageState>() {
        @Override
        public void onChanged(UIKitConstants.FlagMessageState progressState) {
            if (UIKitConstants.FlagMessageState.INITIATED_FLAG.equals(progressState)) {
                if (flagMessageDialog != null) flagMessageDialog.hidePositiveButtonProgressBar(false);
            } else if (UIKitConstants.FlagMessageState.SUCCESS_FLAG.equals(progressState)) {
                Toast.makeText(getContext(), getContext().getString(R.string.cometchat_flag_message_success), Toast.LENGTH_SHORT).show();
                if (flagMessageDialog != null) {
                    flagMessageDialog.dismiss();
                    flagMessageDialog = null;
                }
                baseMessage = null;
            } else if (UIKitConstants.FlagMessageState.FAILURE_FLAG.equals(progressState)) {
                if (flagMessageDialog != null) {
                    flagMessageDialog.hidePositiveButtonProgressBar(true);
                    flagMessageDialog.onFlagMessageError();
                }
                baseMessage = null;
            }
        }
    };
    private DateTimeFormatterCallback dateTimeFormatter;
    // Layout Components
    private LinearLayout customViewLayout, errorViewLayout, aiAssistantEmptyChatGreetingLayout;
    private LinearLayout parent;
    private ImageView paginationLoadingIcon;
    private ImageView newMessagesPaginationIcon;
    // Styles
    private @StyleRes int incomingMessageBubbleStyle;
    private @StyleRes int outgoingMessageBubbleStyle;
    private @StyleRes int dateSeparatorStyle;
    private @StyleRes int deleteDialogStyle;
    private @StyleRes int messageInformationStyle;
    private @StyleRes int messageOptionSheetStyle;
    private @StyleRes int reactionListStyle;
    // Other Properties
    private long parentMessageId = -1;
    private AdditionParameter additionParameter;
    private List<CometChatTextFormatter> textFormatters;
    private BottomSheetDialog bottomSheetDialog;
    // ViewModel
    private MessageListViewModel messageListViewModel;
    // Interfaces
    private OnError onError;
    private OnLoad<BaseMessage> onLoad;
    private OnEmpty onEmpty;
    /**
     * Observer for monitoring changes in UI states. It reacts to different states
     * like LOADING, LOADED, ERROR, EMPTY, and NON_EMPTY.
     */
    Observer<UIKitConstants.States> stateChangeObserver = states -> {
        switch (states) {
            case LOADING:
                handleLoadingState();
                break;
            case LOADED:
                handleLoadedState();
                break;
            case ERROR:
                handleErrorState();
                break;
            case EMPTY:
                handleEmptyState();
                break;
            case NON_EMPTY:
                handleNonEmptyState();
                break;
            default:
                break;
        }
    };
    private CometChatMessagePopupMenu cometchatPopUpMenuMessage;
    private OnReactionListItemClick onReactionListItemClick;
    private ThreadReplyClick onThreadRepliesClick;
    private MessageAdapter.OnMessageLongClick messageLongClick;
    private EmojiPickerClickListener emojiPickerClickListener;
    private MessageOptionClickListener messageOptionClickListener;
    private ReactionClickListener quickReactionClickListener;
    private ReactionsRequest.ReactionsRequestBuilder reactionRequestBuilder;
    /**
     * onReactionClick is an interface that provides methods to handle click on reaction
     */
    private OnReactionClick onReactionClick = new OnReactionClick() {
        @Override
        public void onClick(String emoji, BaseMessage baseMessage) {
            for (ReactionCount reactionCount : baseMessage.getReactions()) {
                if (reactionCount.getReaction().equals(emoji) && reactionCount.getReactedByMe()) {
                    messageListViewModel.removeReaction(baseMessage, emoji);
                    return;
                }
            }
            messageListViewModel.addReaction(baseMessage, emoji);
        }
    };

    SwipeController controller = new SwipeController(getContext(), new SwipeActions() {
        @Override
        public void onSwipePerformed(int position) {
            List<BaseMessage> messages = messageAdapter.getBaseMessageList();
            if (position < 0 || position >= messages.size()) return;
            BaseMessage message = messages.get(position);
            if (message != null) {
                CometChatUIKitHelper.onMessageReply(message, MessageStatus.IN_PROGRESS);
            }
        }
    });

    /**
     * onReactionLongClick is an interface that provides methods to handle long click on reaction
     */
    private OnReactionLongClick onReactionLongClick = this::openReactionListBottomSheet;
    /**
     * onAddMoreReactionsClick is an interface that provides methods to handle click on add more reactions
     */
    private OnAddMoreReactionsClick onAddMoreReactionsClick = baseMessage -> openReactionListBottomSheet(getContext().getString(R.string.cometchat_all), baseMessage);

    // AI Conversation Summary
    private boolean enableConversationSummary = false;
    private CometChatAIConversationSummaryView aiConversationSummaryView;
    private @StyleRes int conversationSummaryStyle = 0;
    private int unreadMessageThreshold = 30;

    private List<String> smartRepliesKeywords;
    private int smartRepliesDelayDuration;
    private CometChatAISmartRepliesView aiSmartRepliesView;
    private CometChatAIConversationStarterView aiConversationStarterView;
    private @StyleRes int smartRepliesStyle = 0;
    private @StyleRes int conversationStarterStyle = 0;

    private List<String> aiAssistantSuggestedMessages;
    private int aiAssistantSuggestedMessagesVisibility = VISIBLE;
    private HashMap<String, ToolCallListener> toolCallListenerHashMap = new HashMap<>();
    private int streamingSpeed;

    // Flex box for ai assistant suggested messages
    private FlexboxLayout aiAssistantSuggestedMessageContainer;

    // AI Assistant styling
    private @ColorInt int aiAssistantSuggestedMessageStrokeColor;
    private @Dimension int aiAssistantSuggestedMessageStrokeWidth;
    private @Dimension float aiAssistantSuggestedMessageCornerRadius;
    private @ColorInt int aiAssistantSuggestedMessageBackgroundColor;
    private @ColorInt int aiAssistantSuggestedMessageTextColor;
    private @StyleRes int aiAssistantSuggestedMessageTextAppearance;
    private Drawable aiAssistantSuggestedMessageEndIcon;
    private @ColorInt int aiAssistantSuggestedMessageEndIconTint;

    // Mark as unread indicator view
    private @ColorInt int newMessageIndicatorSeparatorColor;
    private @ColorInt int newMessageIndicatorTextColor;
    private @ColorInt int newMessageIndicatorTextAppearance;

    private AttributeSet attrs;
    private @StyleRes int defStyleAttr;
    private @StyleRes int style;
    private @StyleRes int flagMessageStyle = -1;
    private Map<String, Integer> flagReasonLocalization;
    private String mentionAllLabelId;
    private String mentionAllLabel;

    /**
     * Constructs a new {@link CometChatMessageList} with the specified context.
     *
     * @param context The context in which the view is running.
     */
    public CometChatMessageList(Context context) {
        this(context, null);
    }

    /**
     * Constructs a new {@link CometChatMessageList} with the given context and
     * attribute set.
     *
     * @param context The context in which the view is running.
     * @param attrs   The attribute set containing custom attributes for the view.
     */
    public CometChatMessageList(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatMessageListStyle);
    }

    /**
     * Constructs a new {@link CometChatMessageList} with the given context,
     * attribute set, and default style attribute.
     *
     * @param context      The context in which the view is running.
     * @param attrs        The attribute set containing custom attributes for the view.
     * @param defStyleAttr The default style attribute to apply to the view.
     */
    public CometChatMessageList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateAndInitializeView(attrs, defStyleAttr);
    }

    /**
     * Opens the reactions bottom sheet dialog for a given message and emoji.
     *
     * @param emoji       The selected emoji for the reaction.
     * @param baseMessage The message for which the reactions are being managed.
     */
    private void openReactionListBottomSheet(String emoji, BaseMessage baseMessage) {
        CometChatReactionList cometchatReactionList = new CometChatReactionList(getContext());
        cometchatReactionList.setSelectedReaction(emoji);
        cometchatReactionList.setBaseMessage(baseMessage);
        cometchatReactionList.setReactionsRequestBuilder(reactionRequestBuilder);
        cometchatReactionList.setOnReactionListItemClick(onReactionListItemClick);
        cometchatReactionList.setStyle(reactionListStyle);
        cometchatReactionList.setOnEmpty(() -> bottomSheetDialog.dismiss());
        Utils.showBottomSheet(getContext(), bottomSheetDialog, true, true, cometchatReactionList);
    }

    /**
     * Inflates the view and initializes the components and attributes.
     *
     * @param attrs        The attribute set used to apply custom styles.
     * @param defStyleAttr The default style attributes to be applied.
     */
    public void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        // Initialize MaterialCard using a utility function
        Utils.initMaterialCard(this);
        bottomSheetDialog = new BottomSheetDialog(getContext());
        cometchatPopUpMenuMessage = new CometChatMessagePopupMenu(getContext(), 0);

        // Initialize additional parameters and screen height
        additionParameter = new AdditionParameter();

        // Initialize ViewModel for message list
        messageListViewModel = new ViewModelProvider.NewInstanceFactory().create(MessageListViewModel.class);

        // Initialize sound manager
        soundManager = new CometChatSoundManager(getContext());

        // Set message long click listener
        messageLongClick = this;

        // Inflate the view and initialize components
        View view = LayoutInflater.from(getContext()).inflate(R.layout.cometchat_messagelist, (ViewGroup) getParent(), false);
        initViewComponent(view);


        // Add the inflated view to the parent layout
        addView(view);

        this.attrs = attrs;
        this.defStyleAttr = defStyleAttr;

        // Apply additional style attributes
        applyStyleAttributes(attrs, defStyleAttr, 0);
    }

    /**
     * Initializes all UI components of the inflated view.
     *
     * @param view The inflated view containing the UI components.
     */
    public void initViewComponent(View view) {
        // Initialize message templates
        messageTemplates = ChatConfigurator.getDataSource().getMessageTemplates(additionParameter);

        // Initialize text formatters and set default mentions
        this.textFormatters = new ArrayList<>();
        setQuickReactions(Arrays.asList(Utils.getDefaultReactionsList()));
        initializeSmartRepliesView();
        initializeConversationStarterView();
        initializeConversationSummaryView();
        processMentionsFormatter();

        // Set up RecyclerView for the chat list
        rvChatListView = view.findViewById(R.id.rv_message_list);
        messageAdapter = new MessageAdapter(getContext(), messageTemplateHashMap, messageLongClick);

        // Configure message adapter with reactions Action Events
        messageAdapter.setOnReactionClick(onReactionClick);
        messageAdapter.setOnReactionLongClick(onReactionLongClick);
        messageAdapter.setOnAddMoreReactionsClick(onAddMoreReactionsClick);

        // Fetch and set message filters
        fetchMessageFilter();

        // Initialize new message layout components and hide initially
        newMessageLayout = view.findViewById(R.id.new_message_layout);

        badge = view.findViewById(R.id.badge);
        newMessageIndicatorIcon = view.findViewById(R.id.image_view);
        newMessageLayout.setVisibility(GONE);

        // Initialize error view layout and text components
        errorViewLayout = view.findViewById(R.id.error_message_list_layout);
        errorTextView = view.findViewById(R.id.tv_error_message_list);
        errorSubtitleTextView = view.findViewById(R.id.tv_error_message_list_subtitle);
        customViewLayout = view.findViewById(R.id.customView_lay);

        // Initialize empty chat greeting layout and text components
        aiAssistantEmptyChatGreetingLayout = view.findViewById(R.id.empty_message_list_layout);
        aiAssistantEmptyChatGreetingImageView = view.findViewById(R.id.iv_empty_message_list);
        aiAssistantEmptyChatGreetingTextView = view.findViewById(R.id.tv_empty_message_list);
        aiAssistantEmptyChatGreetingSubtitleTextView = view.findViewById(R.id.tv_empty_message_list_subtitle);

        // Initialize flex box for ai assistant suggested messages
        aiAssistantSuggestedMessageContainer = view.findViewById(R.id.ai_assistant_suggested_messages_container);

        // Initialize shimmer loading effect components
        shimmerParentLayout = view.findViewById(R.id.shimmer_parent_layout);
        shimmerEffectFrame = view.findViewById(R.id.shimmer_effect_frame);
        shimmerRecyclerviewMessageListList = view.findViewById(R.id.shimmer_recyclerview_message_list);

        // Initialize pagination loading icon
        paginationLoadingIcon = view.findViewById(R.id.paginating_icon);
        newMessagesPaginationIcon = view.findViewById(R.id.new_messages_pagination_icon);
        parent = view.findViewById(R.id.parent);

        // Hide header date initially
        setStickyDateVisibility(VISIBLE);

        // Set report option visibility based on flag reasons
        List<FlagReason> flagReasons = CometChatUIKit.getFlagReasons();
        setFlagOptionVisibility(flagReasons == null || flagReasons.isEmpty() ? GONE : VISIBLE);

        // Set up message list layout and header/footer views
        messageListLayout = view.findViewById(R.id.message_list_layout);
        headerView = view.findViewById(R.id.header_view_layout);
        footerView = view.findViewById(R.id.footer_view_layout);

        // Initialize LinearLayoutManager for the RecyclerView
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvChatListView.setLayoutManager(linearLayoutManager);

        // Observe changes in message list using ViewModel
        observeMessageListChanges();

        // Set scroll listener for RecyclerView
        rvChatListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                handleScrollStateChange(newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                handleScroll();
            }
        });

        newMessageLayout.setOnClickListener(v -> handleNewMessageLayoutClick());

        additionParameter.setOnMessagePreviewClick((messagePreview, position, message) -> {
            if (messageListViewModel.getMessageList().contains(message)) {
                int index = messageListViewModel.getMessageList().indexOf(message);
                if (linearLayoutManager != null) {
                    if (messageAdapter != null && messageAdapter.getItemCount() > 0) {
                        if (isScrolling) rvChatListView.stopScroll();
                        int centerOffset = rvChatListView.getHeight() / 2;
                        linearLayoutManager.scrollToPositionWithOffset(index, centerOffset);
                        messageAdapter.notifyDataSetChanged();
                        setHighlightMessageId(message.getId(), index);
                    }
                }
            } else messageListViewModel.goToMessage(message.getId());
        });
    }

    private void initializeConversationStarterView() {
        aiConversationStarterView = new CometChatAIConversationStarterView(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                               ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(Utils.convertDpToPx(getContext(), 10),
                                getResources().getDimensionPixelSize(R.dimen.cometchat_margin_2),
                                Utils.convertDpToPx(getContext(), 10),
                                Utils.convertDpToPx(getContext(), 10));
        aiConversationStarterView.setLayoutParams(layoutParams);
        aiConversationStarterView.setOnClick((id, reply, position) -> {
            CometChatUIKitHelper.onComposeMessage(id, reply);
            detachedAIConversationStarterView();
        });
    }

    /**
     *  Initializes the AI Smart Replies view and sets its layout parameters and click handler.
     */
    private void initializeConversationSummaryView() {
        aiConversationSummaryView = new CometChatAIConversationSummaryView(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(
                Utils.convertDpToPx(getContext(), 10),
                getResources().getDimensionPixelSize(R.dimen.cometchat_margin_2),
                Utils.convertDpToPx(getContext(), 10),
                Utils.convertDpToPx(getContext(), 10));
        aiConversationSummaryView.setLayoutParams(layoutParams);

        // Set close button click handler
        aiConversationSummaryView.setOnCloseClick(view -> detachedAIConversationSummaryView());
    }

    public void generateConversationSummary() {
        messageListViewModel.fetchConversationSummary();
    }

    public void handleConversationSummaryUIState(UIKitConstants.States states) {
        switch (states) {
            case LOADING:
                aiConversationSummaryView.showLoadingView();
                attachAIConversationSummaryView();
                break;
            case LOADED:
                // Summary is set via separate observer
                break;
            case ERROR:
                aiConversationSummaryView.showErrorView();
                break;
            default:
                break;
        }
    }

    public void attachAIConversationSummaryView() {
        setFooterView(aiConversationSummaryView);
    }

    public void detachedAIConversationSummaryView() {
        footerView.removeView(aiConversationSummaryView);
    }

    /** Sets the summary text for the AI conversation summary view.
     *
     * @param summary The summary text to be displayed.
     */
    private void setConversationSummary(String summary) {
        aiConversationSummaryView.setSummary(summary);
    }

    /** Checks if the AI conversation summary feature is enabled.
     *
     * @return A boolean indicating whether the AI conversation summary feature is enabled.
     */
    public boolean isEnableConversationSummary() {
        return enableConversationSummary;
    }

    /** Enables or disables the AI conversation summary feature.
     *
     * @param enableConversationSummary A boolean indicating whether to enable or disable the feature.
     */
    public void setEnableConversationSummary(boolean enableConversationSummary) {
        this.enableConversationSummary = enableConversationSummary;
        messageListViewModel.setEnableConversationSummary(enableConversationSummary);
    }

    /** Sets the threshold for unread messages to display the unread header.
     *
     * @param unreadTresHold The threshold value for unread messages.
     */
    public void setUnreadMessageThreshold(int unreadTresHold) {
        this.unreadMessageThreshold = unreadTresHold;
        messageListViewModel.setUnreadThreshold(unreadTresHold);
    }

    public int getUnreadMessageThreshold() {
        return unreadMessageThreshold;
    }

    public int getAIConversationSummaryStyle() {
        return conversationSummaryStyle;
    }

    public void setAIConversationSummaryStyle(@StyleRes int styleResId) {
        this.conversationSummaryStyle = styleResId;
        aiConversationSummaryView.setStyle(styleResId);
    }

    /**
     * Observes changes in the message list and other state changes using ViewModel.
     */
    private void observeMessageListChanges() {
        lifecycleOwner = Utils.getLifecycleOwner(getContext());
        if (lifecycleOwner == null) return;

        // Stop audio playback when the app goes to background
        lifecycleOwner.getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            if (event == Lifecycle.Event.ON_STOP) {
                AudioPlayer.getInstance().reset();
            }
        });

        attachObservers();
    }

    public void attachObservers() {
        messageListViewModel.getMutableMessageList().observe(lifecycleOwner, this::setList);
        messageListViewModel.messagesRangeChanged().observe(lifecycleOwner, this::notifyRangeChanged);
        messageListViewModel.updateMessage().observe(lifecycleOwner, this::updateMessage);
        messageListViewModel.getOnMessageDeleted().observe(lifecycleOwner, this::messageDeleted);
        messageListViewModel.addMessage().observe(lifecycleOwner, this::addMessage);
        messageListViewModel.getCometChatException().observe(lifecycleOwner, this::throwError);
        messageListViewModel.removeMessage().observe(lifecycleOwner, this::removeMessage);
        messageListViewModel.getMutableIsInProgress().observe(lifecycleOwner, this::isInProgress);
        messageListViewModel.getMutableHasMore().observe(lifecycleOwner, this::hasMore);
        messageListViewModel.notifyUpdate().observe(lifecycleOwner, this::notifyDataChanged);
        messageListViewModel.getStates().observe(lifecycleOwner, stateChangeObserver);
        messageListViewModel.getMessageDeleteState().observe(lifecycleOwner, messageDeleteObserver);
        messageListViewModel.getMessageFlagState().observe(lifecycleOwner, flagMessageObserver);
        messageListViewModel.closeTopPanel().observe(lifecycleOwner, this::closeInternalTopPanel);
        messageListViewModel.closeBottomPanel().observe(lifecycleOwner, this::closeInternalBottomPanel);
        messageListViewModel.showTopPanel().observe(lifecycleOwner, this::showInternalTopPanel);
        messageListViewModel.showBottomPanel().observe(lifecycleOwner, this::showInternalBottomPanel);
        messageListViewModel.getMutableSmartReplies().observe(lifecycleOwner, this::setSmartReplies);
        messageListViewModel.getMutableConversationStarterReplies().observe(lifecycleOwner, this::setConversationStarters);
        messageListViewModel.getRemoveConversationStarter().observe(lifecycleOwner, this::removeAIView);
        messageListViewModel.getConversationStarterUIState().observe(lifecycleOwner, this::handleConversationStarterUIState);
        messageListViewModel.getSmartRepliesUIState().observe(lifecycleOwner, this::handleAISmartRepliesUIState);
        messageListViewModel.messagesRangeChangedAtEnd().observe(lifecycleOwner, this::notifyRangeChangedAtEnd);
        messageListViewModel.getMutableHasMoreNewMessages().observe(lifecycleOwner, this::hasMoreNewMessages);
        messageListViewModel.getMutableHasMorePreviousMessages().observe(lifecycleOwner, this::hasMorePreviousMessages);
        messageListViewModel.getScrollToMessageId().observe(lifecycleOwner, this::scrollToMessageId);
        messageListViewModel.getMutableConversationSummary().observe(lifecycleOwner, this::setConversationSummary);
        messageListViewModel.getRemoveConversationSummary().observe(lifecycleOwner, this::removeAIView);
        messageListViewModel.getConversationSummaryUIState().observe(lifecycleOwner, this::handleConversationSummaryUIState);
        messageListViewModel.getUnreadMessageAnchorLiveData().observe(lifecycleOwner, this::setUnreadMessageAnchorDecoration);
        messageListViewModel.getUnreadCountLiveData().observe(lifecycleOwner, this::setUnreadMessageCount);
    }

    private void setUnreadMessageCount(Integer count) {
        newMessageCount = count;
        if (messageAdapter != null && ((messageAdapter.getItemCount() - 1) - linearLayoutManager.findLastVisibleItemPosition() < 2)) {
            newMessageLayout.setVisibility(GONE);
        } else {
            newMessageLayout.setVisibility(VISIBLE);
            if (newMessageCount == 0) badge.setVisibility(GONE);
            else {
                badge.setVisibility(VISIBLE);
                badge.setCount(newMessageCount);
            }
        }
    }

    private void setUnreadMessageAnchorDecoration(BaseMessage message) {
        if (newMessageIndicatorDecoration == null) {
            newMessageIndicatorDecoration = new NewMessageIndicatorDecoration(messageAdapter);
            rvChatListView.addItemDecoration(newMessageIndicatorDecoration);
        }
        newMessageIndicatorDecoration.setUnreadMessageId(message.getId());
        rvChatListView.invalidateItemDecorations();
    }

    public void scrollToMessageId(long messageId) {
        if (messageId == 0) return;
        if (messageAdapter != null) {
            int position = messageAdapter.findPositionById(messageId);
            if (position >= 0) {
                if (isScrolling) rvChatListView.stopScroll();
                int centerOffset = rvChatListView.getHeight() / 2;
                linearLayoutManager.scrollToPositionWithOffset(position, centerOffset);
                messageAdapter.notifyDataSetChanged();
                rvChatListView.post(() -> setUnreadMessageCount(newMessageCount));
                if (messageListViewModel.isHighlightScroll()) {
                    setHighlightMessageId(messageId, position);
                }
            }
        }
    }

    /**
     * Sets the highlight on a specific message by its ID and initiates a fade-out
     * animation after a delay.
     *
     * @param messageId The ID of the message to be highlighted.
     * @param position The position of the message in the adapter.
     */
    private void setHighlightMessageId(long messageId, int position) {
        messageAdapter.setHighlightedMessage(messageId, position);
        fadeOutMessageHighlight(position);
    }

    /**
     * Initiates a fade-out animation for the highlighted message.
     * The animation gradually reduces the highlight effect over a duration of 1 second.
     */
    private void fadeOutMessageHighlight(int position) {
        if (messageAdapter != null) {
            ValueAnimator fadeOut = ValueAnimator.ofFloat(1f, 0f);
            fadeOut.setDuration(2000);
            fadeOut.setInterpolator(new DecelerateInterpolator());
            fadeOut.addUpdateListener(animation -> {
                float alpha = (float) animation.getAnimatedValue();
                if (messageAdapter != null)
                    messageAdapter.updateHighlightAlpha(alpha, position);
            });

            fadeOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (messageAdapter != null) {
                        messageAdapter.clearHighlight(position);
                    }
                }
            });

            fadeOut.start();
        }
    }

    private void hasMorePreviousMessages(Boolean aBoolean) {
        this.hasMorePreviousMessages = aBoolean;
    }

    private void hasMoreNewMessages(Boolean aBoolean) {
        this.hasMoreNewMessages = aBoolean;
    }


    private void messageDeleted(BaseMessage message) {
        if (baseMessage != null && message != null && baseMessage.getId() == message.getId()) {
            if (cometchatPopUpMenuMessage != null && message.getDeletedAt() > 0) {
                cometchatPopUpMenuMessage.dismiss();
            }
        } else {
            if (isAgentChat && message != null && messageListViewModel.getParentMessageId() == message.getId()) {
                if (user != null)
                    CometChatUIKitHelper.onOpenChat(user, null);
            }
        }
    }

    public void handleAISmartRepliesUIState(UIKitConstants.States states) {
        switch (states) {
            case LOADING:
                aiSmartRepliesView.showLoadingView();
                attachAISmartRepliesView();
                break;
            case ERROR:
                aiSmartRepliesView.showErrorView();
                break;
            default:
                break;
        }
    }

    public void attachAISmartRepliesView() {
        setFooterView(aiSmartRepliesView);
    }

    public void detachedAISmartRepliesView() {
        footerView.removeView(aiSmartRepliesView);
    }

    public void handleConversationStarterUIState(UIKitConstants.States states) {
        switch (states) {
            case LOADING:
                aiConversationStarterView.showLoadingView();
                setFooterView(aiConversationStarterView);
                break;
            case ERROR:
                aiConversationStarterView.showErrorView();
                break;
            default:
                break;
        }
    }

    public void attachAIConversationStarterView() {
        setFooterView(aiConversationStarterView);
    }

    /**
     * Sets a custom footer view for the message list.
     *
     * @param footerView The View object representing the custom footer view.
     */
    public void setFooterView(View footerView) {
        Utils.handleView(this.footerView, footerView, false);
    }

    /*
     * Detaches the AI conversation starter view from the footer.
     */
    public void detachedAIConversationStarterView() {
        footerView.removeView(aiConversationStarterView);
    }

    /*
     * Removes the AI views (smart replies, conversation starter, and conversation summary)
     * based on the provided boolean flag.
     *
     * @param aBoolean A boolean flag indicating whether to remove the AI views.
     */
    private void removeAIView(Boolean aBoolean) {
        if (aBoolean) {
            detachedAISmartRepliesView();
            detachedAIConversationStarterView();
            detachedAIConversationSummaryView();
        }
    }

    /*
     * Sets the list of smart replies in the AI Smart Replies view.
     *
     * @param strings A list of strings representing the smart replies.
     */
    private void setConversationStarters(List<String> strings) {
        aiConversationStarterView.setReplyList(strings);
    }

    /**
     * Handles the click event on the new message layout.
     * Checks if the lastReadMessageId is already visible in the viewport.
     * If visible, scrolls directly to bottom. Otherwise, fetches messages with unread count.
     */
    private void handleNewMessageLayoutClick() {
        newMessageCount = 0;
        messageListViewModel.resetPendingNewMessageCount();
        if (isScrolling) rvChatListView.stopScroll();

        // Check if the first unread message (or its indicator) is visible or near visible in viewport
        boolean isFirstUnreadMessageVisible = false;

        if (messageAdapter != null && newMessageIndicatorDecoration != null) {
            // Use the unread message ID from the decoration (same as what's displayed)
            long unreadMessageId = newMessageIndicatorDecoration.getUnreadMessageId();
            if (unreadMessageId > 0) {
                int unreadPosition = messageAdapter.findPositionById(unreadMessageId);
                if (unreadPosition >= 0) {
                    int firstVisible = linearLayoutManager.findFirstVisibleItemPosition();
                    int lastVisible = linearLayoutManager.findLastVisibleItemPosition();
                    // Check if unread message indicator is within visible range or nearby
                    isFirstUnreadMessageVisible = (unreadPosition >= firstVisible - 1 && unreadPosition <= lastVisible + 1);
                }
            }
        }

        messageListViewModel.resetMessageRequest();
        messageListViewModel.clear();
        messageListViewModel.clearGoToMessageId();

        if (isFirstUnreadMessageVisible) {
            // First unread message is already visible, scroll to bottom directly
            messageListViewModel.fetchMessages();
        } else {
            // Existing behavior - navigate to lastReadMessageId if needed
            messageListViewModel.fetchMessagesWithUnreadCount();
        }

        newMessageLayout.setVisibility(GONE);
    }

    /**
     * Handles changes in the RecyclerView scroll state.
     *
     * @param newState The new scroll state of the RecyclerView.
     */
    private void handleScrollStateChange(int newState) {
        if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            isScrolling = true;
        } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            isScrolling = false;
        }
    }

    /**
     * Handles the scrolling behavior of the RecyclerView.
     */
    private void handleScroll() {
        if (hasMorePreviousMessages || hasMoreNewMessages && !isInProgress) {
            if (linearLayoutManager.findLastVisibleItemPosition() == (messageAdapter.getItemCount() - 1) || !rvChatListView.canScrollVertically(1)) {
                messageListViewModel.markLastMessageAsRead(messageListViewModel.getLastMessage());
            }

            if (hasMorePreviousMessages && isScrolling && (linearLayoutManager.findFirstVisibleItemPosition() == 0 || !rvChatListView.canScrollVertically(-1))) {
                isInProgress = true;
                isScrolling = false;
                paginationLoadingIcon.setVisibility(VISIBLE);
                messageListViewModel.fetchMessages();
            } else if (hasMoreNewMessages && isScrolling && (linearLayoutManager.findLastVisibleItemPosition() == (messageAdapter.getItemCount() - 1) || !rvChatListView.canScrollVertically(1))) {
                isInProgress = true;
                isScrolling = false;
                newMessagesPaginationIcon.setVisibility(VISIBLE);
                messageListViewModel.fetchNextMessages();
            }
        }

        // Hide the new message layout if the user scrolls near the bottom
        if (messageAdapter != null && ((messageAdapter.getItemCount() - 1) - linearLayoutManager.findLastVisibleItemPosition() < 2)) {
            newMessageLayout.setVisibility(GONE);
        } else {
            newMessageLayout.setVisibility(VISIBLE);
            if (newMessageCount == 0) badge.setVisibility(GONE);
            else {
                badge.setVisibility(VISIBLE);
                badge.setCount(newMessageCount);
            }
        }
    }

    /**
     * Applies the style attributes defined in XML or programmatically to the
     * CometChatMessageList view.
     *
     * @param attrs        The attributes defined in the XML layout.
     * @param defStyleAttr The default style attribute.
     * @param defStyleRes  The default style resource.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatMessageList, defStyleAttr, defStyleRes);
        @StyleRes int styleResId = typedArray.getResourceId(R.styleable.CometChatMessageList_cometchatMessageListStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatMessageList, defStyleRes, styleResId);
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
            setIncomingMessageBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageList_cometchatMessageListIncomingMessageBubbleStyle, 0));
            setOutgoingMessageBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageList_cometchatMessageListOutgoingMessageBubbleStyle, 0));
            setMessageInformationStyle(typedArray.getResourceId(R.styleable.CometChatMessageList_cometchatMessageListMessageInformationStyle, 0));
            setMessageOptionSheetStyle(typedArray.getResourceId(R.styleable.CometChatMessageList_cometchatMessageListMessageOptionSheetStyle, 0));
            setDateSeparatorStyle(typedArray.getResourceId(R.styleable.CometChatMessageList_cometchatMessageListDateSeparatorStyle, 0));
            additionParameter.setActionBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageList_cometchatMessageListActionBubbleStyle, 0));
            additionParameter.setCallActionBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageList_cometchatMessageListCallActionBubbleStyle, 0));
            setDeleteDialogStyle(typedArray.getResourceId(R.styleable.CometChatMessageList_cometchatMessageListDeleteDialogStyle, 0));
            setErrorStateTitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatMessageList_cometchatMessageListErrorStateTitleTextAppearance, 0));
            setErrorStateSubtitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatMessageList_cometchatMessageListErrorStateSubtitleTextAppearance, 0));
            setErrorStateTitleTextColor(typedArray.getColor(R.styleable.CometChatMessageList_cometchatMessageListErrorStateTitleTextColor, CometChatTheme.getTextColorPrimary(getContext())));
            setErrorStateSubtitleTextColor(typedArray.getColor(R.styleable.CometChatMessageList_cometchatMessageListErrorStateSubtitleTextColor, CometChatTheme.getTextColorSecondary(getContext())));
            setAiAssistantEmptyChatTitleTextColor(typedArray.getColor(R.styleable.CometChatMessageList_cometchatMessageListEmptyChatGreetingTitleTextColor, CometChatTheme.getTextColorPrimary(getContext())));
            setAiAssistantEmptyChatTitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatMessageList_cometchatMessageListEmptyChatGreetingTitleTextAppearance, 0));
            setAiAssistantEmptyChatSubtitleTextColor(typedArray.getColor(R.styleable.CometChatMessageList_cometchatMessageListEmptyChatGreetingSubtitleTextColor, CometChatTheme.getNeutralColor500(getContext())));
            setAiAssistantEmptyChatSubtitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatMessageList_cometchatMessageListEmptyChatGreetingSubtitleTextAppearance, 0));
            setAiAssistantSuggestedMessageBackgroundColor(typedArray.getColor(R.styleable.CometChatMessageList_cometchatMessageListAIAssistantSuggestedMessageBackgroundColor,0));
            setAiAssistantSuggestedMessageTextColor(typedArray.getColor(R.styleable.CometChatMessageList_cometchatMessageListAIAssistantSuggestedMessageTextColor, 0));
            setAiAssistantSuggestedMessageTextAppearance(typedArray.getResourceId(R.styleable.CometChatMessageList_cometchatMessageListAIAssistantSuggestedMessageTextAppearance, 0));
            setAiAssistantSuggestedMessageStrokeColor(typedArray.getColor(R.styleable.CometChatMessageList_cometchatMessageListAIAssistantSuggestedMessageStrokeColor,0));
            setAiAssistantSuggestedMessageStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatMessageList_cometchatMessageListAIAssistantSuggestedMessageStrokeWidth, 0));
            setAiAssistantSuggestedMessageCornerRadius(typedArray.getDimension(R.styleable.CometChatMessageList_cometchatMessageListAIAssistantSuggestedMessageCornerRadius, 0));
            setAiAssistantSuggestedMessageEndIcon(typedArray.getDrawable(R.styleable.CometChatMessageList_cometchatMessageListAIAssistantSuggestedMessageEndIcon));
            setAiAssistantSuggestedMessageEndIconTint(typedArray.getColor(R.styleable.CometChatMessageList_cometchatMessageListAIAssistantSuggestedMessageEndIconTint,0));
            setNewMessageIndicatorSeparatorColor(typedArray.getColor(R.styleable.CometChatMessageList_cometchatMessageListNewMessageIndicatorSeparatorColor,0));
            setNewMessageIndicatorTextColor(typedArray.getColor(R.styleable.CometChatMessageList_cometchatMessageListNewMessageIndicatorTextColor,0));
            setNewMessageIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatMessageList_cometchatMessageListNewMessageIndicatorTextAppearance,0));
            setCardBackgroundColor(typedArray.getColor(R.styleable.CometChatMessageList_cometchatMessageListBackgroundColor, CometChatTheme.getBackgroundColor3(getContext())));
            setStrokeColor(ColorStateList.valueOf(typedArray.getColor(R.styleable.CometChatMessageList_cometchatMessageListStrokeColor, 0)));
            setStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatMessageList_cometchatMessageListStrokeWidth, 0));
            setRadius(typedArray.getDimension(R.styleable.CometChatMessageList_cometchatMessageListCornerRadius, 0));
            Drawable backgroundDrawable = typedArray.getDrawable(R.styleable.CometChatMessageList_cometchatMessageListBackgroundDrawable);
            setReactionListStyle(typedArray.getResourceId(R.styleable.CometChatMessageList_cometchatMessageListReactionListStyle, 0));
            setAISmartRepliesStyle(typedArray.getResourceId(R.styleable.CometChatMessageList_cometchatMessageListAISmartRepliesStyle, 0));
            setAIConversationStarterStyle(typedArray.getResourceId(R.styleable.CometChatMessageList_cometchatMessageListAIConversationStarterStyle, 0));
            if (backgroundDrawable != null) setBackgroundDrawable(backgroundDrawable);
            setAIConversationSummaryStyle(typedArray.getResourceId(R.styleable.CometChatMessageList_cometchatMessageListAIConversationSummaryStyle, 0));
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
                additionParameter.setIncomingTextBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatTextBubbleStyle, 0));
                additionParameter.setIncomingImageBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatImageBubbleStyle, 0));
                additionParameter.setIncomingFileBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatFileBubbleStyle, 0));
                additionParameter.setIncomingAudioBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatAudioBubbleStyle, 0));
                additionParameter.setIncomingVideoBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatVideoBubbleStyle, 0));
                additionParameter.setIncomingDeleteBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatDeleteBubbleStyle, 0));
                additionParameter.setIncomingPollBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatPollBubbleStyle, 0));
                additionParameter.setIncomingCollaborativeBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatCollaborativeBubbleStyle, 0));
                additionParameter.setIncomingMeetCallBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMeetCallBubbleStyle, 0));
                additionParameter.setAIAssistantMessageBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatAIAssistantBubbleStyle, 0));
                additionParameter.setIncomingReplyMessagePreviewStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometChatMessagePreviewStyle, 0));
                setIncomingMessageBubbleMentionsStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMessageBubbleMentionsStyle, 0));
            } else {
                additionParameter.setOutgoingTextBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatTextBubbleStyle, 0));
                additionParameter.setOutgoingImageBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatImageBubbleStyle, 0));
                additionParameter.setOutgoingFileBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatFileBubbleStyle, 0));
                additionParameter.setOutgoingAudioBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatAudioBubbleStyle, 0));
                additionParameter.setOutgoingVideoBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatVideoBubbleStyle, 0));
                additionParameter.setOutgoingDeleteBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatDeleteBubbleStyle, 0));
                additionParameter.setOutgoingPollBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatPollBubbleStyle, 0));
                additionParameter.setOutgoingCollaborativeBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatCollaborativeBubbleStyle, 0));
                additionParameter.setOutgoingMeetCallBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMeetCallBubbleStyle, 0));
                additionParameter.setOutgoingReplyMessagePreviewStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometChatMessagePreviewStyle, 0));
                setOutgoingMessageBubbleMentionsStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMessageBubbleMentionsStyle, 0));
                setModerationViewStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatModerationViewStyle, 0));
            }
        } finally {
            typedArray.recycle();
        }
    }

    /* Sets the smart replies in the AI Smart Replies view.
     */
    private void setSmartReplies(List<String> replies) {
        aiSmartRepliesView.setReplyList(replies);
    }

    /*
     *  Initializes the AI Smart Replies view and sets its layout parameters and click handler.
     */
    public void initializeSmartRepliesView() {
        aiSmartRepliesView = new CometChatAISmartRepliesView(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                               ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(Utils.convertDpToPx(getContext(), 10),
                                getResources().getDimensionPixelSize(R.dimen.cometchat_margin_2),
                                Utils.convertDpToPx(getContext(), 10),
                                Utils.convertDpToPx(getContext(), 10));
        aiSmartRepliesView.setLayoutParams(layoutParams);

        aiSmartRepliesView.setOnCLoseIconClick(view -> detachedAISmartRepliesView());
        aiSmartRepliesView.setOnClick((view, id, reply, position) -> {
            CometChatUIKitHelper.onComposeMessage(id, reply);
            detachedAISmartRepliesView();
        });
    }

    /** Gets the current DateTimeFormatterCallback used for formatting date and time in messages.
     *
     * @return The current DateTimeFormatterCallback.
     */
    public DateTimeFormatterCallback getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    /** Sets a custom DateTimeFormatterCallback for formatting date and time in messages.
     *
     * @param dateTimeFormatter The DateTimeFormatterCallback to be set.
     */
    public void setDateTimeFormatter(DateTimeFormatterCallback dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
        messageAdapter.setDateTimeFormatter(dateTimeFormatter);
    }

    /** Checks if AI conversation starters are enabled in the message list.
     *
     * @return true if AI conversation starters are enabled, false otherwise
     */
    public boolean isEnableConversationStarter() {
        return enableConversationStarter;
    }

    /** Enables or disables AI conversation starters in the message list.
     *
     * @param enableConversationStarter true to enable AI conversation starters, false to disable
     */
    public void setEnableConversationStarter(boolean enableConversationStarter) {
        this.enableConversationStarter = enableConversationStarter;
        messageListViewModel.setEnableConversationStarter(enableConversationStarter);
    }

    /** Checks if AI smart replies are enabled in the message list.
     *
     * @return true if AI smart replies are enabled, false otherwise
     */
    public boolean isEnableSmartReplies() {
        return enableSmartReplies;
    }

    /** Enables or disables AI smart replies in the message list.
     *
     * @param enableSmartReplies true to enable AI smart replies, false to disable
     */
    public void setEnableSmartReplies(boolean enableSmartReplies) {
        this.enableSmartReplies = enableSmartReplies;
        messageListViewModel.setEnableSmartReplies(enableSmartReplies);
    }

    /**
     * Sets whether swipe to reply functionality is enabled or disabled.
     * When disabled, users cannot swipe on messages to reply to them.
     *
     * @param swipeToReplyEnabled true to enable swipe to reply, false to disable
     */
    public void setSwipeToReplyEnabled(boolean swipeToReplyEnabled) {
        this.swipeToReplyEnabled = swipeToReplyEnabled;

        if (itemTouchHelper != null && rvChatListView != null) {
            if (swipeToReplyEnabled) {
                itemTouchHelper.attachToRecyclerView(rvChatListView);
            } else {
                itemTouchHelper.attachToRecyclerView(null);
            }
        }
    }

    /**
     * Gets the current state of swipe to reply functionality.
     *
     * @return true if swipe to reply is enabled, false if disabled
     */
    public boolean isSwipeToReplyEnabled() {
        return swipeToReplyEnabled;
    }

    /**
     * Gets the keywords for AI smart replies.
     *
     * @return the list of keywords for AI smart replies
     */
    public List<String> getSmartRepliesKeywords() {
        return smartRepliesKeywords;
    }

    /**
     * Sets the keywords for AI smart replies.
     *
     * @param smartRepliesKeywords the list of keywords for AI smart replies
     */
    public void setAISmartRepliesKeywords(List<String> smartRepliesKeywords) {
        this.smartRepliesKeywords = smartRepliesKeywords;
        messageListViewModel.setSmartReplyKeywords(smartRepliesKeywords);
    }

    /**
     * Gets the delay duration for AI smart replies.
     *
     * @return the delay duration in milliseconds
     */
    public int getAISmartRepliesDelayDuration() {
        return smartRepliesDelayDuration;
    }

    /**
     * Sets the delay duration for AI smart replies.
     *
     * @param smartRepliesDelayDuration the delay duration in milliseconds
     */
    public void setSmartRepliesDelayDuration(int smartRepliesDelayDuration) {
        this.smartRepliesDelayDuration = smartRepliesDelayDuration;
        messageListViewModel.setSmartRepliesDelay(smartRepliesDelayDuration);
    }

    /**
     * Gets the style for AI smart replies view.
     *
     * @return the style resource ID for the AI smart replies view
     */
    public int getAISmartRepliesStyle() {
        return smartRepliesStyle;
    }

    /**
     * Apply style from style resource
     * @param styleResId Style resource ID
     */
    public void setAISmartRepliesStyle(@StyleRes int styleResId) {
        this.smartRepliesStyle = styleResId;
        aiSmartRepliesView.setStyle(styleResId);
    }

    /**
     * Apply style from style resource
     * @param styleResId Style resource ID
     */
    public void setFlagMessageStyle(@StyleRes int styleResId) {
        if (styleResId == -1) return;
        this.flagMessageStyle = styleResId;
    }

    /**
     * Gets the style for AI conversation starter view.
     *
     * @return the style resource ID for the AI conversation starter view
     */
    public int getAIConversationStarterStyle() {
        return conversationStarterStyle;
    }

    /**
     * Sets the style for AI conversation starter view using the specified style resource.
     *
     * @param styleResId the style resource ID for the AI conversation starter view
     */
    public void setAIConversationStarterStyle(@StyleRes int styleResId) {
        this.conversationStarterStyle = styleResId;
        aiConversationStarterView.setStyle(styleResId);
    }

    /**
     * Sets the style for mentions in the outgoing message bubble using the
     * specified style resource.
     *
     * @param resourceId the style resource ID for the outgoing bubble mention text
     */
    private void setOutgoingMessageBubbleMentionsStyle(@StyleRes int resourceId) {
        cometchatMentionsFormatter.setOutgoingBubbleMentionTextStyle(getContext(), resourceId);
    }

    /**
     * Sets the style for mentions in the incoming message bubble using the
     * specified style resource.
     *
     * @param resourceId the style resource ID for the incoming bubble mention text
     */
    private void setIncomingMessageBubbleMentionsStyle(@StyleRes int resourceId) {
        cometchatMentionsFormatter.setIncomingBubbleMentionTextStyle(getContext(), resourceId);
    }

    /**
     * Sets the styles for the CometChatMessageList by applying a given style
     * resource.
     *
     * @param styleResId The style resource ID to be applied.
     */
    public void setStyle(@StyleRes int styleResId) {
        this.style = styleResId;
        if (styleResId != 0) {
            TypedArray finalTypedArray = getContext().getTheme().obtainStyledAttributes(styleResId, R.styleable.CometChatMessageList);
            extractAttributesAndApplyDefaults(finalTypedArray);
        }
    }

    /**
     * Sets the stroke color for the card.
     *
     * @param strokeColor The color state list for the stroke.
     */
    @Override
    public void setStrokeColor(ColorStateList strokeColor) {
        super.setStrokeColor(strokeColor);
    }

    /**
     * Sets the stroke width for the card.
     *
     * @param strokeWidth The width of the stroke in pixels.
     */
    @Override
    public void setStrokeWidth(int strokeWidth) {
        super.setStrokeWidth(strokeWidth);
    }

    /**
     * Sets the corner radius of the card.
     *
     * @param radius The radius in pixels.
     */
    @Override
    public void setRadius(float radius) {
        super.setRadius(radius);
    }

    /**
     * Sets the background color of the card.
     *
     * @param color The color to set as the background.
     */
    @Override
    public void setCardBackgroundColor(int color) {
        super.setCardBackgroundColor(color);
    }

    /**
     * Sets the background drawable for the card.
     *
     * @param drawable The drawable to be used as the background.
     */
    @Override
    public void setBackgroundDrawable(Drawable drawable) {
        super.setBackgroundDrawable(drawable);
    }

    /**
     * Processes the text formatters by updating the addition parameter with the
     * current list of text formatters.
     */
    private void processFormatters() {
        additionParameter.setTextFormatters(textFormatters);
    }

    /**
     * Processes and adds the {@link CometChatMentionsFormatter} to the list of text
     * formatters if it's available in the data source. This method iterates through
     * the formatters provided by the data source and sets the
     * `cometchatMentionsFormatter` if found.
     */
    private void processMentionsFormatter() {
        List<CometChatTextFormatter> formatters = CometChatUIKit.getDataSource().getTextFormatters(getContext(), additionParameter);
        for (CometChatTextFormatter textFormatter : formatters) {
            if (textFormatter instanceof CometChatMentionsFormatter) {
                cometchatMentionsFormatter = (CometChatMentionsFormatter) textFormatter;
                cometchatMentionsFormatter.setMentionAllLabel(mentionAllLabelId, mentionAllLabel);
                break;
            }
        }
        setTextFormatters(formatters);
    }

    /**
     * @return the instance of mention formatter.
     */
    public CometChatMentionsFormatter getMentionsFormatter() {
        return cometchatMentionsFormatter;
    }

    /**
     * Sets the margin for the left message bubble.
     *
     * @param top    The top margin in pixels.
     * @param bottom The bottom margin in pixels.
     * @param left   The left margin in pixels.
     * @param right  The right margin in pixels.
     */
    public void setLeftBubbleMargin(int top, int bottom, int left, int right) {
        messageAdapter.setLeftBubbleMargin(top, bottom, left, right);
    }

    /**
     * Sets the margin for the right message bubble.
     *
     * @param top    The top margin in pixels.
     * @param bottom The bottom margin in pixels.
     * @param left   The left margin in pixels.
     * @param right  The right margin in pixels.
     */
    public void setRightBubbleMargin(int top, int bottom, int left, int right) {
        messageAdapter.setRightBubbleMargin(top, bottom, left, right);
    }

    /**
     * Sets the margin for both the left and right message bubbles.
     *
     * @param top    The top margin in pixels.
     * @param bottom The bottom margin in pixels.
     * @param left   The left margin in pixels.
     * @param right  The right margin in pixels.
     */
    public void setBubbleMargin(int top, int bottom, int left, int right) {
        messageAdapter.setBubbleMargin(top, bottom, left, right);
    }

    /**
     * Toggles the visibility of the header date in the message list.
     *
     * @param visibility If GONE, hides the header date; if VISIBLE, shows the header date.
     */
    public void setStickyDateVisibility(int visibility) {
        if (visibility == VISIBLE) {
            stickyHeaderDecoration = new StickyHeaderDecoration(messageAdapter);
            rvChatListView.addItemDecoration(stickyHeaderDecoration, 0);
        } else {
            rvChatListView.removeItemDecoration(stickyHeaderDecoration);
        }
    }

    /**
     * Displays an internal bottom panel by applying the given view function to the
     * current context.
     *
     * <p>
     * This method removes any existing parent from the current internal bottom
     * panel, if it exists, and detaches it from its parent view. It then creates a
     * new view using the provided function and adds it to the footer view.
     *
     * @param view A function that takes a {@link Context} and returns a {@link View}
     *             to be displayed as the internal bottom panel.
     */
    public void showInternalBottomPanel(@Nullable Function1<Context, View> view) {
        if (view != null) {
            Utils.removeParentFromView(internalBottomPanel);
            if (internalBottomPanel != null) footerView.removeView(internalBottomPanel);
            this.internalBottomPanel = view.apply(getContext());
            footerView.addView(internalBottomPanel);
        }
    }

    /**
     * Displays an internal top panel by applying the given view function to the
     * current context.
     *
     * <p>
     * This method removes any existing parent from the current internal top panel,
     * if it exists, and detaches it from its parent view. It then creates a new
     * view using the provided function and adds it to the header view.
     *
     * @param view A function that takes a {@link Context} and returns a {@link View}
     *             to be displayed as the internal top panel.
     */
    public void showInternalTopPanel(Function1<Context, View> view) {
        if (view != null) {
            Utils.removeParentFromView(internalTopPanel);
            if (internalTopPanel != null) headerView.removeView(internalTopPanel);
            this.internalTopPanel = view.apply(getContext());
            headerView.addView(internalTopPanel);
        }
    }

    /**
     * Closes the internal top panel by removing it from the header view.
     *
     * <p>
     * This method sets the internal top panel reference to null after removing the
     * view from the header.
     *
     * @param avoid This parameter is not used and can be null.
     */
    public void closeInternalTopPanel(Void avoid) {
        headerView.removeView(internalTopPanel);
        internalTopPanel = null;
    }

    /**
     * Closes the internal bottom panel by removing it from the footer view.
     *
     * <p>
     * This method sets the internal bottom panel reference to null after removing
     * the view from the footer.
     *
     * @param avoid This parameter is not used and can be null.
     */
    public void closeInternalBottomPanel(Void avoid) {
        footerView.removeView(internalBottomPanel);
        internalBottomPanel = null;
    }

    /**
     * Sets whether to enable automatic fetching of messages.
     *
     * @param autoFetch If true, automatic fetching is enabled; if false, it is disabled.
     */
    public void setAutoFetch(boolean autoFetch) {
        this.autoFetch = autoFetch;
    }

    /**
     * Notifies the adapter that the data has changed, prompting a refresh of the
     * displayed messages.
     *
     * @param unused not used in this method, included for method signature consistency
     */
    public void notifyDataChanged(Void unused) {
        messageAdapter.notifyDataSetChanged();
    }

    /**
     * Notifies the adapter of a range of new items that have been inserted into the
     * data set.
     *
     * @param finalRange the number of new items added to the adapter
     */
    public void notifyRangeChanged(int finalRange) {
        messageAdapter.notifyItemRangeInserted(0, finalRange);
        paginationLoadingIcon.setVisibility(GONE);
    }

    /**
     * Notifies the adapter of a range of new items that have been inserted at the end
     * of the data set (for pagination when scrolling down).
     *
     * @param finalRange the number of new items added to the adapter at the end
     */
    public void notifyRangeChangedAtEnd(int finalRange) {
        int insertPosition = messageAdapter.getItemCount() - finalRange;
        messageAdapter.notifyItemRangeInserted(insertPosition, finalRange);
        newMessagesPaginationIcon.setVisibility(GONE);

        BaseMessage firstUnreadMessage = messageListViewModel.getFirstUnreadMessage();
        if (firstUnreadMessage != null && firstUnreadMessage.getId() > -1) {
            setUnreadMessageAnchorDecoration(firstUnreadMessage);
            markConversationAsRead();
        }
    }

    /**
     * Notifies the adapter that a specific item has been removed.
     *
     * @param integer the position of the item to be removed
     */
    public void removeMessage(Integer integer) {
        messageAdapter.notifyItemRemoved(integer);
    }

    /**
     * Updates the hasMore flag to indicate whether there are more messages to load.
     *
     * @param aBoolean true if there are more messages, false otherwise
     */
    public void hasMore(Boolean aBoolean) {
        hasMore = aBoolean;
    }

    /**
     * Updates the isInProgress flag to indicate whether a loading operation is in
     * progress.
     *
     * @param aBoolean true if loading is in progress, false otherwise
     */
    public void isInProgress(Boolean aBoolean) {
        isInProgress = aBoolean;
    }

    /**
     * Displays a layout indicating the number of new messages received.
     *
     * @param messageCount the number of new messages to display
     */
    public void showNewMessage(int messageCount) {
        newMessageCount = messageCount;
        if (newMessageCount > 0) {
            newMessageLayout.setVisibility(VISIBLE);
            badge.setVisibility(VISIBLE);
            badge.setCount(newMessageCount);
        } else {
            newMessageLayout.setVisibility(GONE);
        }
    }

    /**
     * Adds a new message to the message list.
     *
     * @param baseMessage The BaseMessage object representing the new message.
     */
    public void addMessage(BaseMessage baseMessage) {
        if (!disableSoundForMessages) soundManager.play(Sound.incomingMessage, customSoundForMessages);

        // Get accumulated count and first unread message from ViewModel (handles case when observer was paused)
        int pendingCount = messageListViewModel.getAndResetPendingNewMessageCount();
        BaseMessage firstPendingUnread = messageListViewModel.getAndResetFirstPendingUnreadMessage();

        // Set unread message indicator decoration only if:
        // 1. There are pending unread messages from when observer was paused (pendingCount > 1 means messages accumulated)
        // 2. Indicator hasn't been set yet
        // This ensures indicator only shows when returning from thread screen, not during normal real-time messaging
        if (firstPendingUnread != null && newMessageIndicatorDecoration == null && pendingCount > 1) {
            setUnreadMessageAnchorDecoration(firstPendingUnread);
        }

        if (!scrollToBottomOnNewMessage) {
            if (rvChatListView.getLayoutManager() != null) {
                if ((messageAdapter.getItemCount() - 1) - ((LinearLayoutManager) rvChatListView.getLayoutManager()).findLastVisibleItemPosition() < 5)
                    scrollToBottom();
                else {
                    if (baseMessage != null && baseMessage.getSender() != null && CometChatUIKit.getLoggedInUser() != null && !CometChatUIKit
                        .getLoggedInUser()
                        .getUid()
                        .equalsIgnoreCase(baseMessage.getSender().getUid())) {
                        newMessageCount += pendingCount;
                        showNewMessage(newMessageCount);
                    } else
                        messageAdapter.notifyItemInserted(messageAdapter.getItemCount() - 1);
                }
            } else {
                if (baseMessage != null && baseMessage.getSender() != null && CometChatUIKit.getLoggedInUser() != null && !CometChatUIKit
                    .getLoggedInUser()
                    .getUid()
                    .equalsIgnoreCase(baseMessage.getSender().getUid())) {
                    newMessageCount += pendingCount;
                    showNewMessage(newMessageCount);
                } else
                    messageAdapter.notifyItemInserted(messageAdapter.getItemCount() - 1);
            }
        } else scrollToBottom();
    }

    /**
     * Sets a custom header view for the message list.
     *
     * @param headerView The View object representing the custom header view.
     */
    public void setHeaderView(View headerView) {
        Utils.handleView(this.headerView, headerView, false);
    }

    /**
     * Sets the layout resource for the empty state view.
     *
     * @param id The resource ID of the layout for the empty state view.
     */
    public void setEmptyStateView(@LayoutRes int id) {
        if (id != 0) {
            try {
                customEmptyView = View.inflate(getContext(), id, null);
            } catch (Exception e) {
                customEmptyView = null;
                CometChatLogger.e(TAG, e.toString());
            }
        }
    }

    /**
     * Sets the layout resource for the empty state view.
     *
     * @param id The resource ID of the layout for the empty state view.
     */
    public void setAIAssistantEmptyChatGreetingView(@LayoutRes int id) {
        if (id != 0) {
            try {
                customAIAssistantEmptyChatGreetingView = View.inflate(getContext(), id, null);
            } catch (Exception e) {
                customAIAssistantEmptyChatGreetingView = null;
                CometChatLogger.e(TAG, e.toString());
            }
        }
    }

    /**
     * Sets the layout resource for the error state view.
     *
     * @param id The resource ID of the layout for the error state view.
     */
    public void setErrorStateView(@LayoutRes int id) {
        if (id != 0) {
            try {
                customErrorView = View.inflate(getContext(), id, null);
            } catch (Exception e) {
                customErrorView = null;
                CometChatLogger.e(TAG, e.toString());
            }
        }
    }

    /**
     * Sets the layout resource for the loading state view.
     *
     * @param id The resource ID of the layout for the loading state view.
     */
    public void setLoadingStateView(@LayoutRes int id) {
        if (id != 0) {
            try {
                customLoadingView = View.inflate(getContext(), id, null);
            } catch (Exception e) {
                customLoadingView = null;
                CometChatLogger.e(TAG, e.toString());
            }
        }
    }

    /**
     * sets the style for the new message indicator badge.
     */
    public void setBadgeStyle(@StyleRes int badgeStyle) {
        badge.setStyle(badgeStyle);
    }

    /**
     * Sets the icon color for the new message indicator.
     *
     * @param color The color to be applied to the new message indicator icon.
     */
    public void setNewMessageIndicatorIconTint(@ColorInt int color) {
        if (color != 0) newMessageIndicatorIcon.setImageTintList(ColorStateList.valueOf(color));
    }

    /**
     * Sets whether to scroll to the bottom of the message list automatically on new
     * messages.
     *
     * @param value {@code true} to enable auto-scrolling to the bottom on new
     *              messages, {@code false} otherwise.
     */
    public void scrollToBottomOnNewMessage(boolean value) {
        scrollToBottomOnNewMessage = value;
    }

    /**
     * Checks if the message list is currently scrolled to the bottom.
     *
     * @return {@code true} if the message list is at the bottom, {@code false}
     * otherwise.
     */
    public boolean atBottom() {
        if (messageListViewModel.getMessageList() != null && !messageListViewModel.getMessageList().isEmpty()) {
            int start = Math.max(0, messageListViewModel.getMessageList().size() - 5);
            boolean containsActionMessage = false;
            for (int i = start; i < messageListViewModel.getMessageList().size(); i++) {
                BaseMessage message = messageListViewModel.getMessageList().get(i);
                if (message instanceof Action || message instanceof Call) {
                    containsActionMessage = true;
                }
            }
            if (containsActionMessage && ((messageAdapter.getItemCount() - 1) - linearLayoutManager.findLastVisibleItemPosition() < 10)) return true;
        }
        return messageAdapter != null && ((messageAdapter.getItemCount() - 1) - linearLayoutManager.findLastVisibleItemPosition() < 5);
    }

    /**
     * Sets the parent message ID for the message list. This is used to display
     * threaded replies.
     *
     * @param parentMessage The ID of the parent message.
     */
    public void setParentMessage(long parentMessage) {
        if (parentMessage > -1) {
            this.parentMessageId = parentMessage;
        }
    }

    /**
     * Sets the MessagesRequestBuilder for fetching messages in the message list.
     *
     * @param builder The MessagesRequestBuilder to set.
     */
    public void setMessagesRequestBuilder(MessagesRequest.MessagesRequestBuilder builder) {
        messageListViewModel.setMessagesRequestBuilder(builder);
    }

    /**
     * Sets the ReactionsRequestBuilder for fetching reactions in the message list.
     *
     * @param builder The ReactionsRequestBuilder to set.
     */
    public void setReactionsRequestBuilder(ReactionsRequest.ReactionsRequestBuilder builder) {
        this.reactionRequestBuilder = builder;
    }

    /**
     * Sets whether to disable sound for messages in the message list.
     *
     * @param disableSoundForMessages true to disable sound, false to enable sound.
     */
    public void disableSoundForMessages(boolean disableSoundForMessages) {
        this.disableSoundForMessages = disableSoundForMessages;
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
        messageAdapter.hideReceipts(visibility != VISIBLE);
    }

    /**
     * Sets the alignment of messages in the message list.
     *
     * @param alignment The MessageListAlignment enum representing the alignment.
     */
    public void setMessageAlignment(UIKitConstants.MessageListAlignment alignment) {
        messageAdapter.setAlignment(alignment);
        cometchatPopUpMenuMessage.setMessageAlignment(alignment);
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
        messageAdapter.showAvatar(visibility == VISIBLE);
    }

    /**
     * Sets the date pattern for displaying message dates in the message list.
     *
     * @param timeFormat The SimpleDateFormat object representing the date pattern.
     */
    public void setTimeFormat(SimpleDateFormat timeFormat) {
        messageAdapter.setTimeFormat(timeFormat);
    }

    /**
     * Sets the date pattern for displaying sticky dates
     * list.
     *
     * @param dateFormat The SimpleDateFormat object representing the date separator pattern.
     */
    public void setDateFormat(SimpleDateFormat dateFormat) {
        messageAdapter.setDateFormat(dateFormat);
    }

    /**
     * Sets the message templates for the message list.
     *
     * @param cometchatMessageTemplates The list of CometChatMessageTemplate objects representing the
     *                                  message templates. Fetches the message filter based on the message
     *                                  templates. This method is called internally to update the message
     *                                  filter. You do not need to call this method directly.
     */
    public void setTemplates(List<CometChatMessageTemplate> cometchatMessageTemplates) {
        if (cometchatMessageTemplates != null) {
            if (!cometchatMessageTemplates.isEmpty()) {
                messageTemplates = cometchatMessageTemplates;
            } else {
                messageTemplates = new ArrayList<>();
            }
            fetchMessageFilter();
        }
    }

    /**
     * Fetches the message filter based on the current message templates.
     * This method populates the message types, categories, and view types
     * based on the provided message templates. It also updates the message
     * adapter and ViewModel with the new message template mappings.
     */
    public void fetchMessageFilter() {
        messageTypesToRetrieve.clear();
        messageCategoriesToRetrieve.clear();
        messageTemplateHashMap.clear();
        messageViewTypes.clear();
        int i = 1;
        for (CometChatMessageTemplate template : messageTemplates) {
            if (template != null && template.getCategory() != null && template.getType() != null) {
                if (groupActionMessageVisibility != VISIBLE && template.getType().equals(CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER)) {
                    continue;
                }

                messageTypesToRetrieve.put(template.getCategory() + "_" + template.getType(), template.getType());
                messageCategoriesToRetrieve.put(template.getCategory(), template.getCategory());
                messageTemplateHashMap.put(template.getCategory() + "_" + template.getType(), template);
                messageViewTypes.put(template.getCategory() + "_" + template.getType(), i);
                i++;
            }
        }
        messageAdapter.setMessageTemplateHashMap(messageTemplateHashMap, messageViewTypes);
        messageListViewModel.setMessageTemplateHashMap(messageTemplateHashMap);
        messageListViewModel.setMessagesTypesAndCategories(new ArrayList<>(messageTypesToRetrieve.values()),
                                                           new ArrayList<>(messageCategoriesToRetrieve.values()));
    }

    /**
     * Retrieves the current mapping of message view types.
     *
     * @return a HashMap containing message view types with their corresponding
     * integer values.
     */
    private HashMap<String, Integer> getMessageViewTypes() {
        return messageViewTypes;
    }

    /**
     * Handles the LOADING state by displaying a loading view or a shimmer adapter.
     */
    private void handleLoadingState() {
        if (customLoadingView != null) {
            customViewLayout.setVisibility(View.VISIBLE);
            customViewLayout.removeAllViews();
            customViewLayout.addView(customLoadingView);
        } else {
            messageListLayout.setVisibility(View.GONE);
            setAIAssistantEmptyStateVisibility();
            CometChatShimmerAdapter adapter = new CometChatShimmerAdapter(2, R.layout.cometchat_shimmer_message_list);
            shimmerRecyclerviewMessageListList.setAdapter(adapter);
            shimmerRecyclerviewMessageListList.setLayoutManager(new LinearLayoutManager(getContext()) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            });
            shimmerParentLayout.setVisibility(View.VISIBLE);
            shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(getContext()));
            shimmerEffectFrame.startShimmer();
        }
    }

    /*
     * Sets the visibility of the AI assistant empty state layout based on whether
     * it is an agent chat.
     */
    private void setAIAssistantEmptyStateVisibility() {
        if (isAgentChat) {
            aiAssistantEmptyChatGreetingLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Handles the LOADED state by hiding the shimmer and displaying the message
     * list layout.
     */
    private void handleLoadedState() {
        hideShimmer();
        hideAllStates();
        messageListLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the shimmer view by setting its visibility to GONE and stopping the
     * shimmer animation.
     */
    private void hideShimmer() {
        shimmerParentLayout.setVisibility(View.GONE);
        shimmerEffectFrame.stopShimmer();
    }

    /**
     * Hides all states by setting their visibility to GONE.
     */
    private void hideAllStates() {
        customViewLayout.setVisibility(View.GONE);
        errorViewLayout.setVisibility(View.GONE);
        aiAssistantEmptyChatGreetingLayout.setVisibility(View.GONE);
    }

    /**
     * Handles the ERROR state by showing an error view if there are no messages. If
     * messages exist, it hides the pagination loading icon.
     */
    private void handleErrorState() {
        if (messageListViewModel.getMessageList() == null || messageListViewModel.getMessageList().isEmpty()) {
            if (errorStateVisibility == VISIBLE) {
                if (customErrorView != null) {
                    customViewLayout.setVisibility(View.VISIBLE);
                    customViewLayout.removeAllViews();
                    customViewLayout.addView(customErrorView);
                } else {
                    hideShimmer();
                    hideAllStates();
                    errorTextView.setText(getResources().getString(R.string.cometchat_error_conversations_title));
                    errorSubtitleTextView.setText(getResources().getString(R.string.cometchat_error_conversations_subtitle));
                    errorViewLayout.setVisibility(View.VISIBLE);
                }
            } else {
                errorViewLayout.setVisibility(View.GONE);
            }
        } else {
            paginationLoadingIcon.setVisibility(GONE);
            newMessagesPaginationIcon.setVisibility(GONE);
        }
    }

    /**
     * Handles the EMPTY state by showing a custom empty view if available,
     * otherwise hides the shimmer and error state.
     */
    private void handleEmptyState() {
        if (onEmpty != null) onEmpty.onEmpty();
        if (isAgentChat) {
            if (customAIAssistantEmptyChatGreetingView != null) {
                aiAssistantEmptyChatGreetingLayout.removeAllViews();
                aiAssistantEmptyChatGreetingLayout.addView(customAIAssistantEmptyChatGreetingView);
                aiAssistantEmptyChatGreetingLayout.setVisibility(VISIBLE);
            } else {
                hideShimmer();
                hideErrorState();
                setUpAIAssistantGreetingView();
            }
            messageListLayout.setVisibility(View.GONE);
        } else {
            if (customEmptyView != null) {
                customViewLayout.setVisibility(View.VISIBLE);
                customViewLayout.removeAllViews();
                customViewLayout.addView(customEmptyView);
            } else {
                hideShimmer();
                hideErrorState();
            }
            messageListLayout.setVisibility(View.GONE);
        }
    }

    /*
     * Sets up the AI assistant greeting view by extracting the greeting title,
     * subtitle, and suggested messages from the user's metadata.
     */
    private void setUpAIAssistantGreetingView() {
        JSONObject metadata = user.getMetadata();
        String greetingTitle = extractStringFromMetadata(metadata, UIKitConstants.AIAssistantJsonConstants.GREETING_MESSAGE);
        String greetingSubtitle = extractStringFromMetadata(metadata, UIKitConstants.AIAssistantJsonConstants.INTRODUCTORY_MESSAGE);
        List<String> suggestedMessages;
        if (aiAssistantSuggestedMessages != null && !aiAssistantSuggestedMessages.isEmpty()) {
            suggestedMessages = aiAssistantSuggestedMessages;
        } else suggestedMessages = extractSuggestedMessages(metadata);
        setupSuggestedMessages(suggestedMessages);
        showAIAgentGreetingView(greetingTitle, greetingSubtitle, suggestedMessages);
    }

    /*
     * Displays the AI assistant greeting view with the provided title, subtitle,
     * and suggested messages.
     *
     * @param greetingTitle     The title of the greeting message.
     * @param greetingSubtitle  The subtitle of the greeting message.
     * @param suggestedMessages A list of suggested messages to be displayed.
     */
    private void showAIAgentGreetingView(String greetingTitle, String greetingSubtitle, List<String> suggestedMessages) {
        aiAssistantEmptyChatGreetingTextView.setText(!greetingTitle.isEmpty() ? greetingTitle : getResources().getString(R.string.cometchat_empty_chat_title));
        aiAssistantEmptyChatGreetingSubtitleTextView.setText(!greetingSubtitle.isEmpty() ? greetingSubtitle : getResources().getString(R.string.cometchat_empty_chat_subtitle));
        aiAssistantEmptyChatGreetingLayout.setVisibility(View.VISIBLE);
        aiAssistantEmptyChatGreetingImageView.setAvatar(user.getName(), user.getAvatar());
        aiAssistantEmptyChatGreetingImageView.setVisibility(VISIBLE);
    }

    /*
     * Extracts a string value from the provided metadata JSON object.
     *
     * @param metadata The JSONObject containing metadata.
     * @param key      The key for which the string value needs to be extracted.
     * @return The extracted string value, or an empty string if not found or an error occurs.
     */
    private String extractStringFromMetadata(JSONObject metadata, String key) {
        if (metadata == null) return "";

        try {
            return metadata.optString(key, "");
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.getMessage());
            return "";
        }
    }

    /*
     * Extracts suggested messages from the provided metadata JSON object.
     *
     * @param metadata The JSONObject containing metadata with suggested messages.
     * @return A list of suggested messages extracted from the metadata.
     */
    private List<String> extractSuggestedMessages(JSONObject metadata) {
        List<String> messages = new ArrayList<>();
        if (metadata == null) return messages;

        try {
            if (metadata.has(UIKitConstants.AIAssistantJsonConstants.SUGGESTED_MESSAGES)) {
                JSONArray suggestedMessagesArray = metadata.getJSONArray(UIKitConstants.AIAssistantJsonConstants.SUGGESTED_MESSAGES);
                for (int i = 0; i < suggestedMessagesArray.length(); i++) {
                    String message = suggestedMessagesArray.getString(i).trim();
                    if (!message.isEmpty()) {
                        messages.add(message);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return messages;
    }

    /*
     * Sets up the suggested messages in the AI assistant greeting view.
     *
     * @param suggestedMessages A list of suggested messages to be displayed.
     */
    private void setupSuggestedMessages(List<String> suggestedMessages) {
        if (!suggestedMessages.isEmpty()) {
            aiAssistantSuggestedMessageContainer.removeAllViews();

            for (int i = 0; i < suggestedMessages.size(); i++) {
                MaterialCardView suggestedMessagesCard = new MaterialCardView(getContext());
                Utils.initMaterialCard(suggestedMessagesCard);
                MarginLayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                int margin = getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_margin_1);
                layoutParams.setMargins(margin, margin, margin, margin);
                suggestedMessagesCard.setLayoutParams(layoutParams);

                LinearLayout suggestedMessagesContainer = new LinearLayout(getContext());
                suggestedMessagesContainer.setOrientation(LinearLayout.HORIZONTAL);
                suggestedMessagesContainer.setGravity(Gravity.CENTER_VERTICAL);
                suggestedMessagesContainer.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                suggestedMessagesContainer.setPadding(
                        getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_padding_4),
                        getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_padding_2),
                        getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_padding_4),
                        getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_padding_2)
                );

                TextView tvSuggestedMessage = getSuggestedMessageTextView(i, suggestedMessages);
                ImageView suggestedMessageEndIcon = getSuggestedMessageEndIcon(i);
                setSuggestedMessagesStyle(suggestedMessagesCard, suggestedMessageEndIcon, tvSuggestedMessage);

                // Add Views
                suggestedMessagesContainer.addView(tvSuggestedMessage);
                suggestedMessagesContainer.addView(suggestedMessageEndIcon);

                suggestedMessagesCard.addView(suggestedMessagesContainer);

                aiAssistantSuggestedMessageContainer.addView(suggestedMessagesCard);
                if (aiAssistantSuggestedMessagesVisibility == VISIBLE) {
                    aiAssistantSuggestedMessageContainer.setVisibility(View.VISIBLE);
                } else {
                    aiAssistantSuggestedMessageContainer.setVisibility(View.GONE);
                }

                // OnClick behavior
                String composeMessage = suggestedMessages.get(i);
                suggestedMessagesCard.setOnClickListener(v -> {
                    CometChatUIKitHelper.onComposeMessage(user.getUid(), composeMessage);
                });
            }
        }
    }

    /*
     * Creates and returns an ImageView for the suggested message end icon.
     *
     * @param i The index of the suggested message in the list.
     * @return An ImageView configured with the suggested message end icon.
     */
    private ImageView getSuggestedMessageEndIcon(int i) {
        ImageView suggestedMessageEndIcon = new ImageView(getContext());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_18dp),
                getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_18dp)
        );
        iconParams.setMarginStart(getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_margin_2));
        suggestedMessageEndIcon.setLayoutParams(iconParams);
        suggestedMessageEndIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        suggestedMessageEndIcon.setImageDrawable(aiAssistantSuggestedMessageEndIcon);
        return suggestedMessageEndIcon;
    }

    /*
     * Creates and returns a TextView for a suggested message.
     *
     * @param i                 The index of the suggested message in the list.
     * @param suggestedMessages The list of suggested messages.
     * @return A TextView configured with the suggested message text and styles.
     */
    private TextView getSuggestedMessageTextView(int i, List<String> suggestedMessages) {
        TextView tvSuggestedMessage = new TextView(getContext());
        tvSuggestedMessage.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        tvSuggestedMessage.setLayoutParams(textParams);
        tvSuggestedMessage.setEllipsize(TextUtils.TruncateAt.END);
        tvSuggestedMessage.setText(suggestedMessages.get(i));
        return tvSuggestedMessage;
    }

    /*
     * Applies styles to the suggested message card, end icon, and text view.
     *
     * @param suggestedMessagesCard   The MaterialCardView representing the suggested message card.
     * @param suggestedMessageEndIcon The ImageView representing the end icon of the suggested message.
     * @param tvSuggestedMessage      The TextView representing the suggested message text.
     */
    private void setSuggestedMessagesStyle(MaterialCardView suggestedMessagesCard, ImageView suggestedMessageEndIcon, TextView tvSuggestedMessage) {
        tvSuggestedMessage.setTextColor(aiAssistantSuggestedMessageTextColor);
        tvSuggestedMessage.setTextAppearance(aiAssistantSuggestedMessageTextAppearance);
        suggestedMessageEndIcon.setImageTintList(ColorStateList.valueOf(aiAssistantSuggestedMessageEndIconTint));
        suggestedMessagesCard.setCardBackgroundColor(aiAssistantSuggestedMessageBackgroundColor);
        suggestedMessagesCard.setStrokeColor(aiAssistantSuggestedMessageStrokeColor);
        suggestedMessagesCard.setStrokeWidth(aiAssistantSuggestedMessageStrokeWidth);
        suggestedMessagesCard.setRadius(aiAssistantSuggestedMessageCornerRadius);
    }

    /**
     * Hides the error state by setting the error view's visibility to GONE.
     */
    private void hideErrorState() {
        errorViewLayout.setVisibility(View.GONE);
    }

    /**
     * Handles the NON_EMPTY state by hiding the shimmer and displaying the message
     * list layout.
     */
    private void handleNonEmptyState() {
        hideShimmer();
        hideAllStates();
        messageListLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Sets the list of messages to be displayed in the chat view.
     *
     * <p>
     * This method sets the adapter for the RecyclerView and updates the message
     * list, message template mappings, and the date format for messages. It also
     * ensures that the chat view scrolls to the bottom after setting the new list.
     *
     * @param messageList The list of {@link BaseMessage} objects to be displayed in the
     *                    chat view. This list will replace the current message list in the
     *                    adapter.
     */
    public void setList(List<BaseMessage> messageList) {
        rvChatListView.setAdapter(messageAdapter);
        messageAdapter.setBaseMessageList(messageList);
        if (onLoad != null) onLoad.onLoad(messageList);
        messageAdapter.setMessageTemplateHashMap(messageTemplateHashMap, messageViewTypes);
        BaseMessage firstUnreadMessage = messageListViewModel.getFirstUnreadMessage();
        if (gotoMessageId == -1 || firstUnreadMessage != null && firstUnreadMessage.getId() > -1) {
            markConversationAsRead();
        }
        scrollToBottom();
    }

    /*
     * Marks the current conversation as read.
     *
     * <p>
     * This method updates the read status of the conversation in the ViewModel,
     * indicating that all messages in the conversation have been read by the user.
     */
    private void markConversationAsRead() {
        messageListViewModel.markConversationRead();
    }

    /**
     * Updates the specified message in the list and notifies the adapter.
     *
     * <p>
     * This method refreshes the UI for the message at the specified index. If the
     * updated message is the last message in the list, it will scroll to the bottom
     * of the chat view if the view is already scrolled to the bottom.
     *
     * @param index The index of the message to be updated in the message list. This
     *              should be a valid index within the current list of messages.
     */
    public void updateMessage(int index) {
        messageAdapter.notifyItemChanged(index, messageListViewModel.getMessageList().get(index));
    }

    /**
     * Scrolls the message list to the bottom. This method is useful to
     * automatically scroll to the latest message in the list. It also marks the
     * last message as read.
     */
    public void scrollToBottom() {
        if (messageAdapter != null && messageAdapter.getItemCount() > 0) {
            linearLayoutManager.scrollToPositionWithOffset(messageAdapter.getItemCount() - 1, -1000000000);
            messageAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Retrieves the current message adapter used by the chat view.
     *
     * @return The {@link MessageAdapter} instance currently set for the chat view.
     */
    public MessageAdapter getAdapter() {
        return messageAdapter;
    }

    /**
     * Sets a new message adapter for the chat view.
     *
     * <p>
     * This method updates the chat view to use the specified adapter for displaying
     * messages. It also sets the adapter to the RecyclerView.
     *
     * @param adapter The {@link MessageAdapter} to be set for the chat view.
     */
    public void setAdapter(MessageAdapter adapter) {
        this.messageAdapter = adapter;
        rvChatListView.setAdapter(adapter);
    }

    /**
     * Gets the ViewModel associated with the message list.
     *
     * @return The {@link MessageListViewModel} instance that manages the message
     * list data.
     */
    public MessageListViewModel getViewModel() {
        return messageListViewModel;
    }

    /**
     * Retrieves the parent view layout of the chat view.
     *
     * @return The {@link LinearLayout} that acts as the parent view for the chat
     * layout.
     */
    public LinearLayout getView() {
        return parent;
    }

    /**
     * Retrieves the layout that displays the new message indicator.
     *
     * @return The {@link MaterialCardView} instance used to show the new message
     * layout.
     */
    public MaterialCardView getNewMessageLayout() {
        return newMessageLayout;
    }

    /**
     * Retrieves the ImageView that displays the new message indicator icon.
     *
     * @return The {@link ImageView} instance used to show the new message indicator
     * icon.
     */
    public ImageView getNewMessageImageView() {
        return newMessageIndicatorIcon;
    }

    /**
     * Checks if there are more messages to load in the chat view.
     *
     * @return {@code true} if there are more messages available; {@code false}
     * otherwise.
     */
    public boolean isHasMore() {
        return hasMore;
    }

    /**
     * Retrieves the sticky header decoration used in the chat view.
     *
     * @return The {@link StickyHeaderDecoration} instance associated with the
     * RecyclerView.
     */
    public StickyHeaderDecoration getStickyHeaderDecoration() {
        return stickyHeaderDecoration;
    }

    /**
     * Sets whether to hide or show the error in the message list.
     *
     * @param visibility gone to hide the error, visible to show the error.
     */
    public void setErrorStateVisibility(int visibility) {
        errorStateVisibility = visibility;
    }

    /** Checks whether to start loading messages from unread messages.
     *
     * @return true if starting from unread messages, false otherwise.
     */
    public boolean isStartFromUnreadMessages() {
        return startFromUnreadMessages;
    }

    /** Sets whether to start loading messages from unread messages.
     *
     * @param startFromUnreadMessages true to start from unread messages, false otherwise.
     */
    public void setStartFromUnreadMessages(boolean startFromUnreadMessages) {
        this.startFromUnreadMessages = startFromUnreadMessages;
        messageListViewModel.setStartFromUnreadMessages(startFromUnreadMessages);
    }

    /** Sets a custom view to be displayed as the unread header in the message list.
     *
     * @param view The custom view to be set as the unread header.
     */
    public void setNewMessageIndicatorView(View view) {
        if (messageAdapter != null) {
            messageAdapter.setNewMessageIndicatorView(view);
        }
    }

    /** get mark as unread option visibility
     *
     * @return An integer representing the visibility of the unread option.
     */
    public int getMarkAsUnreadOptionVisibility() {
        return markAsUnreadOptionVisibility;
    }

    /** Sets the visibility of the mark as unread option.
     *
     * @param visibility An integer representing the visibility status of the unread option.
     */
    public void setMarkAsUnreadOptionVisibility(int visibility) {
        this.markAsUnreadOptionVisibility = visibility;
        additionParameter.setMarkUnreadOptionVisibility(visibility);
    }

    /**
     * get message reaction option visibility
     *
     * @return An integer representing the visibility of the reaction option.
     */
    public int getMessageReactionOptionVisibility() {
        return messageReactionOptionVisibility;
    }

    /**
     * Sets the visibility of the message reaction option.
     *
     * @param visibility An integer representing the visibility status of the reaction option.
     */
    public void setMessageReactionOptionVisibility(int visibility) {
        this.messageReactionOptionVisibility = visibility;
    }

    /** Sets the visibility of the "Report" option.
     *
     * @param visibility An integer representing the visibility status of the report option.
     */
    public void setFlagOptionVisibility(int visibility) {
        additionParameter.setFlagOptionVisibility(visibility);
    }

    /** Retrieves the visibility status of the "Report" option.
     *
     * @return An integer representing the visibility of the report option.
     */
    public int getFlagOptionVisibility() {
        return additionParameter.getFlagOptionVisibility();
    }

    /**
     * Retrieves the visibility status of the "Reply in Thread" option.
     *
     * @return An integer representing the visibility of the reply in thread option.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getReplyInThreadOptionVisibility() {
        return replyInThreadOptionVisibility;
    }

    /**
     * Sets the visibility of the "Reply in Thread" option.
     *
     * @param visibility An integer representing the visibility status of the reply in thread option.
     *                   Accepts values such as {@code View.VISIBLE}, {@code View.INVISIBLE}, or {@code View.GONE}.
     */
    public void setReplyInThreadOptionVisibility(int visibility) {
        this.replyInThreadOptionVisibility = visibility;
        additionParameter.setReplyInThreadOptionVisibility(visibility);
    }

    /**
     * Retrieves the visibility status of the "Reply to Message" option.
     *
     * @return An integer representing the visibility of the reply to message option.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getReplyOptionVisibility() {
        return replyOptionVisibility;
    }

    /**
     * Sets the visibility of the "Reply to Message" option.
     *
     * @param visibility An integer representing the visibility status of the reply to message option.
     */
    public void setReplyOptionVisibility(int visibility) {
        this.replyOptionVisibility = visibility;
        additionParameter.setReplyToMessageOptionVisibility(visibility);
    }

    /**
     * Retrieves the visibility status of the "Translate Message" option.
     *
     * @return An integer representing the visibility of the translate message option.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getTranslateMessageOptionVisibility() {
        return translateMessageOptionVisibility;
    }

    /**
     * Sets the visibility of the "Translate Message" option.
     *
     * @param visibility An integer representing the visibility status of the translate message option.
     */
    public void setTranslateMessageOptionVisibility(int visibility) {
        this.translateMessageOptionVisibility = visibility;
        additionParameter.setTranslateMessageOptionVisibility(visibility);
    }

    /**
     * Retrieves the visibility status of the "Copy Message" option.
     *
     * @return An integer representing the visibility of the copy message option.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getCopyMessageOptionVisibility() {
        return copyMessageOptionVisibility;
    }

    /**
     * Sets the visibility of the "Copy Message" option.
     *
     * @param visibility An integer representing the visibility status of the copy message option.
     */
    public void setCopyMessageOptionVisibility(int visibility) {
        this.copyMessageOptionVisibility = visibility;
        additionParameter.setCopyMessageOptionVisibility(visibility);
    }

    /**
     * Retrieves the visibility status of the "Edit Message" option.
     *
     * @return An integer representing the visibility of the edit message option.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getEditMessageOptionVisibility() {
        return editMessageOptionVisibility;
    }

    /**
     * Sets the visibility of the "Edit Message" option.
     *
     * @param visibility An integer representing the visibility status of the edit message option.
     */
    public void setEditMessageOptionVisibility(int visibility) {
        this.editMessageOptionVisibility = visibility;
        additionParameter.setEditMessageOptionVisibility(visibility);
    }

    /**
     * Retrieves the visibility status of the "Share Message" option.
     *
     * @return An integer representing the visibility of the share message option.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getShareMessageOptionVisibility() {
        return shareMessageOptionVisibility;
    }

    /**
     * Sets the visibility of the "Share Message" option.
     *
     * @param visibility An integer representing the visibility status of the share message option.
     */
    public void setShareMessageOptionVisibility(int visibility) {
        this.shareMessageOptionVisibility = visibility;
        additionParameter.setShareMessageOptionVisibility(visibility);
    }

    /**
     * Retrieves the visibility status of the "Message Privately" option.
     *
     * @return An integer representing the visibility of the message privately option.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getMessagePrivatelyOptionVisibility() {
        return messagePrivatelyOptionVisibility;
    }

    /**
     * Sets the visibility of the "Message Privately" option.
     *
     * @param visibility An integer representing the visibility status of the message privately option.
     */
    public void setMessagePrivatelyOptionVisibility(int visibility) {
        this.messagePrivatelyOptionVisibility = visibility;
        additionParameter.setMessagePrivatelyOptionVisibility(visibility);
    }

    /**
     * Retrieves the visibility status of the "Delete Message" option.
     *
     * @return An integer representing the visibility of the delete message option.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getDeleteMessageOptionVisibility() {
        return deleteMessageOptionVisibility;
    }

    /**
     * Sets the visibility of the "Delete Message" option.
     *
     * @param visibility An integer representing the visibility status of the delete message option.
     */
    public void setDeleteMessageOptionVisibility(int visibility) {
        this.deleteMessageOptionVisibility = visibility;
        additionParameter.setDeleteMessageOptionVisibility(visibility);
    }

    /**
     * Retrieves the visibility status of the "Message Info" option.
     *
     * @return An integer representing the visibility of the message info option.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getMessageInfoOptionVisibility() {
        return messageInfoOptionVisibility;
    }

    /**
     * Sets the visibility of the "Message Info" option.
     *
     * @param visibility An integer representing the visibility status of the message info option.
     */
    public void setMessageInfoOptionVisibility(int visibility) {
        this.messageInfoOptionVisibility = visibility;
        additionParameter.setMessageInfoOptionVisibility(visibility);
    }

    /**
     * Retrieves the visibility status of the "Group Action Message" option.
     *
     * @return An integer representing the visibility of the group action message option.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getGroupActionMessageVisibility() {
        return groupActionMessageVisibility;
    }

    /**
     * Sets the visibility of the "Group Action Message".
     *
     * @param visibility An integer representing the visibility status of the group action message.
     */
    public void setGroupActionMessageVisibility(int visibility) {
        this.groupActionMessageVisibility = visibility;
        additionParameter.setGroupActionMessageVisibility(visibility);
        messageAdapter.hideGroupActionMessage(visibility != View.VISIBLE);
    }

    /**
     * Throws a CometChatException and handles the error by invoking the provided
     * onError callback.
     *
     * @param cometchatException The CometChatException to be thrown and handled.
     */
    public void throwError(CometChatException cometchatException) {
        if (onError != null) onError.onError(cometchatException);
    }

    /**
     * Handles the long click event on a message bubble.
     *
     * @param list                     the list of available message options.
     * @param baseMessage              the message that was long-clicked.
     * @param cometchatMessageTemplate the message template associated with the message.
     * @param cometchatMessageBubble   the message bubble view.
     */
    @Override
    public void onLongClick(List<CometChatMessageOption> list,
                            BaseMessage baseMessage,
                            CometChatMessageTemplate cometchatMessageTemplate,
                            CometChatMessageBubble cometchatMessageBubble) {
        if (baseMessage != null && baseMessage.getId() != 0) {
            this.customOption = list;
            this.baseMessage = baseMessage;
            this.messageBubble = cometchatMessageBubble;
            this.messageTemplate = cometchatMessageTemplate;

            boolean hasOptions = list != null && !list.isEmpty();
            boolean reactionsVisible = messageReactionOptionVisibility == VISIBLE;

            if (hasOptions || reactionsVisible) {
                openMessageOptionBottomSheet(getActionItems());
            }
        }
    }

    /** Sets the message ID to navigate to.
     *
     * @param gotoMessageId The ID of the message to navigate to.
     */
    public void gotoMessage(long gotoMessageId) {
        if (gotoMessageId != 0) {
            this.gotoMessageId = gotoMessageId;
        }
    }

    /**
     * Opens a bottom sheet for message options.
     *
     * @param items the list of OptionSheetMenuItem to display in the bottom sheet.
     */
    private void openMessageOptionBottomSheet(List<OptionSheetMenuItem> items) {
        cometchatPopUpMenuMessage.setStyle(messageOptionSheetStyle);
        cometchatPopUpMenuMessage.setAddReactionIcon(addReactionIcon);
        cometchatPopUpMenuMessage.setReceiptsVisibility(receiptsVisibility);
        ModerationStatus moderationStatus = Utils.getModerationStatus(baseMessage);
        if (UIKitConstants.MessageCategory.INTERACTIVE.equals(baseMessage.getCategory()) || messageReactionOptionVisibility != View.VISIBLE || UIKitConstants.ModerationConstants.DISAPPROVED.equals(moderationStatus)) {
            cometchatPopUpMenuMessage.setQuickReactionsVisibility(GONE);
        }

        cometchatPopUpMenuMessage.setEmojiPickerClickListener(() -> {
            if (emojiPickerClickListener != null) {
                emojiPickerClickListener.onEmojiPickerClick();
            } else {
                showEmojiKeyBoard();
            }
        });
        cometchatPopUpMenuMessage.setReactionClickListener((baseMessage, reaction) -> {
            if (quickReactionClickListener != null) {
                quickReactionClickListener.onReactionClick(baseMessage, reaction);
            } else {
                if (onReactionClick != null) onReactionClick.onClick(reaction, baseMessage);
            }
            cometchatPopUpMenuMessage.dismiss();
        });

        cometchatPopUpMenuMessage.setMessageTemplates(messageTemplates);
        cometchatPopUpMenuMessage.setTextFormatters(textFormatters);
        cometchatPopUpMenuMessage.setQuickReactions(quickReactions);
        List<CometChatMessagePopupMenu.MenuItem> menuItems = new ArrayList<>();
        for (OptionSheetMenuItem optionItem : items) {
            menuItems.add(new CometChatMessagePopupMenu.MenuItem(optionItem.getId(),
                                                                 optionItem.getText(),
                                                                 optionItem.getStartIcon() != 0 ? ResourcesCompat.getDrawable(getResources(),
                                                                                                                              optionItem.getStartIcon(),
                                                                                                                              getContext().getTheme()) : null,
                                                                 optionItem.getEndIcon() != 0 ? ResourcesCompat.getDrawable(getResources(),
                                                                                                                            optionItem.getEndIcon(),
                                                                                                                            getContext().getTheme()) : null,
                                                                 optionItem.getStartIconTint(),
                                                                 optionItem.getEndIconTint(),
                                                                 optionItem.getTextColor(),
                                                                 optionItem.getAppearance(),
                                                                 null));
        }

        cometchatPopUpMenuMessage.setMenuItems(menuItems);
        cometchatPopUpMenuMessage.setOnMenuItemClickListener((id, name) -> {
            if (messageOptionClickListener == null) {
                for (CometChatMessageOption option : customOption) {
                    if (option != null && option.getId() != null && option.getId().equals(id)) {
                        if (option.getClick() != null) {
                            option.getClick().onClick();
                            cometchatPopUpMenuMessage.dismiss();
                            break;
                        } else {
                            handleMessageOptionSheetClicks(id);
                        }
                        cometchatPopUpMenuMessage.dismiss();
                    }
                }
            } else {
                messageOptionClickListener.onMessageOptionClick(new OptionSheetMenuItem(id, name, 0, 0, 0, 0, 0, 0));
            }
        });
        cometchatPopUpMenuMessage.show(messageBubble.getContentView(), this, baseMessage);
    }

    /**
     * Retrieves a list of action items for the options available on a message.
     *
     * @return a list of OptionSheetMenuItem representing the action items.
     */
    @NonNull
    private List<OptionSheetMenuItem> getActionItems() {
        List<OptionSheetMenuItem> itemList = new ArrayList<>();
        for (CometChatMessageOption option : customOption) {
            if (option != null) {
                OptionSheetMenuItem actionItem = new OptionSheetMenuItem(option.getId(),
                                                                         option.getTitle(),
                                                                         option.getIcon(),
                                                                         0,
                                                                         option.getIconTintColor(),
                                                                         0,
                                                                         option.getTitleAppearance(),
                                                                         option.getTitleColor());
                actionItem.setBackground(option.getBackgroundColor());
                itemList.add(actionItem);
            }
        }
        return itemList;
    }

    /**
     * Displays the emoji keyboard for selecting emojis. If the emoji keyboard is
     * not already created, it initializes it.
     */
    private void showEmojiKeyBoard() {
        if (emojiKeyboard == null) {
            emojiKeyboard = new CometChatEmojiKeyboard();
        }

        emojiKeyboard.show(getContext());
        emojiKeyboard.setOnClick(new EmojiKeyBoardView.OnClick() {
            @Override
            public void onClick(String emoji) {
                onReactionClick.onClick(emoji, baseMessage);
                emojiKeyboard.dismiss();
                cometchatPopUpMenuMessage.dismiss();
            }

            @Override
            public void onLongClick(String emoji) {
                // Handle long click on emoji if necessary
            }
        });
    }

    /**
     * Performs the action associated with the specified action item.
     *
     * @param id the String representing the action to perform.
     */
    private void handleMessageOptionSheetClicks(@NonNull String id) {
        switch (id) {
            case UIKitConstants.MessageOption.MESSAGE_INFORMATION:
                showMessageInformation();
                break;
            case UIKitConstants.MessageOption.DELETE:
                showDeleteMessageAlertDialog(baseMessage);
                break;
            case UIKitConstants.MessageOption.REPLY_IN_THREAD:
                if (onThreadRepliesClick != null) {
                    onThreadRepliesClick.onThreadReplyClick(getContext(), baseMessage, messageTemplate);
                }
                break;
            case UIKitConstants.MessageOption.COPY:
                copyMessage(baseMessage);
                break;
            case UIKitConstants.MessageOption.SHARE:
                shareMessage();
                break;
            case UIKitConstants.MessageOption.MESSAGE_PRIVATELY:
                messagePrivately();
                break;
            case UIKitConstants.MessageOption.EDIT:
                messageListViewModel.onMessageEdit(baseMessage);
                break;
            case UIKitConstants.MessageOption.REPLY_TO_MESSAGE:
                messageListViewModel.onMessageReply(baseMessage);
                break;
            case UIKitConstants.MessageOption.REPORT:
                showFlagMessageDialog(baseMessage);
                break;
            case UIKitConstants.MessageOption.MARK_UNREAD:
                markMessageAsUnread(baseMessage);
                break;
        }
    }

    /*
     * Marks the specified message as unread.
     *
     * @param baseMessage the message to be marked as unread.
     */
    private void markMessageAsUnread(BaseMessage baseMessage) {
        messageListViewModel.markMessageAsUnread(baseMessage);
    }

    /**
     * Displays detailed information about the current message in an alert dialog.
     *
     * <p>
     * This method creates an alert dialog using the AppCompat theme and initializes
     * an instance of {@link CometChatMessageInformation}. It sets the layout
     * parameters, assigns the current message, template, and bubble view to the
     * {@code CometChatMessageInformation} instance. It also applies the message
     * bubble style based on the template and sets up a listener to dismiss the
     * dialog when the back button is pressed. The dialog is then displayed to the
     * user.
     */
    public void showMessageInformation() {
        Activity activity = Utils.getActivity(getContext());
        cometchatMessageInformation = new CometChatMessageInformation();
        cometchatMessageInformation.init(getContext(), baseMessage);
        cometchatMessageInformation.setStyle(messageInformationStyle);
        cometchatMessageInformation.setTemplate(messageTemplate);
        cometchatMessageInformation.setBubbleView((context, message) -> {
            CometChatThreadHeader threadHeader = new CometChatThreadHeader(context);
            threadHeader.setParentMessage(baseMessage);
            threadHeader.setTemplates(messageTemplates);
            threadHeader.setTextFormatters(textFormatters);
            threadHeader.setReplyCountBarVisibility(View.GONE);
            threadHeader.setCardBackgroundColor(Color.TRANSPARENT);
            threadHeader.setMaxHeight(Utils.convertDpToPx(context, 300));
            threadHeader.setReactionVisibility(View.GONE);
            return threadHeader;
        });
        cometchatMessageInformation.setBottomSheetListener(() -> cometchatMessageInformation = null);
        try {
            if (Utils.isActivityUsable(activity) && activity instanceof AppCompatActivity) {
                cometchatMessageInformation.show(((AppCompatActivity) activity).getSupportFragmentManager(), "CometChatMessageInformation");
            } else {
                CometChatLogger.e(TAG, "Activity is not usable to show Message Information");
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, "Error showing message information: " + e.getMessage());
        }
    }

    /**
     * Displays an alert dialog to confirm the deletion of a message.
     *
     * @param baseMessage the message to be deleted, passed for the deletion confirmation
     *                    action
     */
    private void showDeleteMessageAlertDialog(BaseMessage baseMessage) {
        deleteAlertDialog = new CometChatConfirmDialog(getContext(), deleteDialogStyle);
        deleteAlertDialog.setConfirmDialogIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.cometchat_ic_delete, null));
        deleteAlertDialog.setTitleText(getContext().getString(R.string.cometchat_delete_message_title));
        deleteAlertDialog.setSubtitleText(getContext().getString(R.string.cometchat_delete_message_subtitle));
        deleteAlertDialog.setPositiveButtonText(getContext().getString(R.string.cometchat_delete));
        deleteAlertDialog.setNegativeButtonText(getContext().getString(R.string.cometchat_cancel));
        deleteAlertDialog.setOnPositiveButtonClick(v -> messageListViewModel.deleteMessage(baseMessage));
        deleteAlertDialog.setOnNegativeButtonClick(v -> deleteAlertDialog.dismiss());
        deleteAlertDialog.setConfirmDialogElevation(0);
        deleteAlertDialog.setCancelable(false);
        deleteAlertDialog.show();
    }

    private void showFlagMessageDialog(BaseMessage baseMessage) {
        flagMessageDialog = new CometChatFlagMessageDialog(getContext(), baseMessage);
        if (flagReasonLocalization != null) flagMessageDialog.setLocalizationIdMap(flagReasonLocalization);
        flagMessageDialog.setFlagReasons(CometChatUIKit.getFlagReasons());
        flagMessageDialog.setFlagRemarkInputFieldVisibility(flagRemarkInputFieldVisibility);
        flagMessageDialog.setOnPositiveButtonClickListener((flagDetail) -> {
            messageListViewModel.flagMessage(flagDetail, baseMessage);
        });
        if (flagMessageStyle != -1) flagMessageDialog.setFlagMessageStyle(flagMessageStyle);
        flagMessageDialog.setOnCancelButtonClickListener(() -> flagMessageDialog.dismiss());
        flagMessageDialog.setOnCloseButtonClickListener(() -> flagMessageDialog.dismiss());
        flagMessageDialog.show();
    }

    /**
     * Sets the localization ID map for flag reasons.
     *
     * @param localizationIdMap A map containing localization IDs for flag reasons.
     */
    public void setFlagReasonLocalization(Map<String, Integer> localizationIdMap) {
        this.flagReasonLocalization = localizationIdMap;
    }

    /**
     * Retrieves the visibility status of the flag remark input field in the flag message dialog.
     *
     * @return An integer representing the visibility of the flag remark input field.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getFlagRemarkInputFieldVisibility() {
        return flagRemarkInputFieldVisibility;
    }

    /**
     * Sets the visibility of the flag remark input field in the flag message dialog.
     *
     * @param visibility An integer representing the visibility status of the flag remark input field.
     *                   Accepts values such as {@code View.VISIBLE}, {@code View.INVISIBLE}, or {@code View.GONE}.
     */
    public void setFlagRemarkInputFieldVisibility(int visibility) {
        this.flagRemarkInputFieldVisibility = visibility;
    }

    /**
     * Copies the text of a given message to the system clipboard.
     * <p>
     * The raw message text (including markdown formatting syntax) is placed on
     * the clipboard so that when pasted into the composer, the markdown can be
     * parsed back into rich text formatting spans.
     * </p>
     *
     * @param baseMessage The message to be copied, expected to be of type
     *                    {@link TextMessage}.
     * @throws ClassCastException If the provided baseMessage is not an instance of
     *                            {@link TextMessage}.
     */
    public void copyMessage(BaseMessage baseMessage) {
        String message = ((TextMessage) baseMessage).getText();
        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Messages", message);
        clipboardManager.setPrimaryClip(clipData);
    }

    /**
     * Shares the current message (either text or image) with other applications.
     *
     * <p>
     * If the message is of type text, it creates a sharing intent with the
     * formatted message text and launches the share dialog.
     *
     * <p>
     * If the message is of type image, it retrieves the image using Glide, saves it
     * to the device, and then creates a sharing intent to share the image.
     *
     * <p>
     * If the message is a media message with an attachment but not an image, it
     * triggers a file download using the `MediaUtils` utility class to handle
     * sharing of the media.
     */
    public void shareMessage() {
        if (baseMessage != null && baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            String message = ((TextMessage) baseMessage).getText();
            String formatterString = String.valueOf(FormatterUtils.getFormattedText(getContext(),
                                                                                    baseMessage,
                                                                                    UIKitConstants.FormattingType.MESSAGE_BUBBLE,
                                                                                    UIKitConstants.MessageBubbleAlignment.RIGHT,
                                                                                    message,
                                                                                    textFormatters));
            intent.setType("text/plain");
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getContext().getString(R.string.cometchat_share));
            intent.putExtra(android.content.Intent.EXTRA_TEXT, formatterString);
            getContext().startActivity(Intent.createChooser(intent, getContext().getString(R.string.cometchat_share)));
        } else if (baseMessage instanceof MediaMessage) {
            // Use downloadFileInNewThread for all media types (image, video, audio, file)
            // This properly handles secure URLs with FAT headers
            MediaMessage message = (MediaMessage) baseMessage;
            if (message.getAttachment() != null) {
                MediaUtils.downloadFileInNewThread(getContext(),
                                                   message.getAttachment().getFileUrl(),
                                                   message.getId() + message.getAttachment().getFileName(),
                                                   message.getAttachment().getFileMimeType(),
                                                   UIKitConstants.files.SHARE);
            }
        }
    }

    /**
     * Sends a private message to the sender of the currently selected message. This
     * method fetches the sender's information using the messageListViewModel.
     */
    private void messagePrivately() {
        messageListViewModel.fetchMessageSender(baseMessage);
    }

    /**
     * Gets the RecyclerView that displays the chat messages.
     *
     * @return The {@link RecyclerView} instance used to display the chat messages.
     */
    public RecyclerView getRecyclerView() {
        return this.rvChatListView;
    }

    /**
     * Retrieves the long click listener for messages.
     *
     * @return the OnMessageLongClick listener.
     */
    public MessageAdapter.OnMessageLongClick getMessageLongClick() {
        return messageLongClick;
    }

    /**
     * Gets the resource ID for the add reaction icon.
     *
     * @return The resource ID of the add reaction icon.
     */
    public int getAddReactionIcon() {
        return addReactionIcon;
    }

    /**
     * Sets the resource ID for the add reaction icon.
     *
     * @param addReactionIcon The resource ID to set for the add reaction icon.
     */
    public void setAddReactionIcon(int addReactionIcon) {
        this.addReactionIcon = addReactionIcon;
    }

    /**
     * Gets the list of quick reactions.
     *
     * @return The list of quick reactions.
     */
    public List<String> getQuickReactions() {
        return quickReactions;
    }

    /**
     * Sets the list of quick reactions.
     *
     * @param quickReactions The list of quick reactions to set.
     */
    public void setQuickReactions(List<String> quickReactions) {
        if (quickReactions != null) this.quickReactions = quickReactions;
    }

    /**
     * Returns the current user object. This method provides access to the user
     * information.
     *
     * @return the {@link User} object
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user for the chat.
     *
     * @param user The User object representing the chat user.
     */
    public void setUser(User user) {
        if (user != null) {
            this.user = user;
            this.group = null;
            this.isAgentChat = Utils.isAgentChat(user);
            messageAdapter.setType(UIKitConstants.ReceiverType.USER);
            messageAdapter.setUser(user);
            messageListViewModel.setUser(user,
                                         new ArrayList<>(messageTypesToRetrieve.values()),
                                         new ArrayList<>(messageCategoriesToRetrieve.values()),
                                         parentMessageId, isAgentChat, gotoMessageId);
            if (isAgentChat) {
                setStickyDateVisibility(View.GONE);
                if (parentMessageId != -1) {
                    if (gotoMessageId > -1) {
                        messageListViewModel.goToMessage(gotoMessageId);
                    } else {
                        messageListViewModel.fetchMessages();
                    }
                } else {
                    handleEmptyState();
                }
            } else {
                if (autoFetch) messageListViewModel.fetchMessagesWithUnreadCount();
            }
            aiConversationStarterView.setUid(user.getUid());
            aiSmartRepliesView.setUid(user.getUid());
            processFormatters();
            initializeItemTouchHelper();
        }
    }

    /*
     * Initializes the ItemTouchHelper for swipe-to-reply functionality if
     * applicable.
     */
    private void initializeItemTouchHelper() {
        if (!isAgentChat) {
            itemTouchHelper = new ItemTouchHelper(controller);
            if (swipeToReplyEnabled) {
                itemTouchHelper.attachToRecyclerView(rvChatListView);
            }
            controller.setAdapter(messageAdapter);
        }
    }

    /**
     * Returns the current group object. This method provides access to the group
     * information.
     *
     * @return the {@link Group} object
     */
    public Group getGroup() {
        return group;
    }

    /**
     * Sets the group for the message list. The group represents the conversation
     * group to which the messages belong.
     *
     * @param group The group to set.
     */
    public void setGroup(Group group) {
        if (group != null) {
            this.group = group;
            this.user = null;
            messageAdapter.setType(UIKitConstants.ReceiverType.GROUP);
            messageAdapter.setGroup(group);
            messageListViewModel.setGroup(group,
                                          new ArrayList<>(messageTypesToRetrieve.values()),
                                          new ArrayList<>(messageCategoriesToRetrieve.values()),
                                          parentMessageId, gotoMessageId);
            if (autoFetch) messageListViewModel.fetchMessagesWithUnreadCount();
            aiConversationStarterView.setUid(group.getGuid());
            aiSmartRepliesView.setUid(group.getGuid());
            processFormatters();
            initializeItemTouchHelper();
        }
    }

    /**
     * Returns the style resource ID for the bubble of incoming messages.
     *
     * @return the resource ID for the incoming message bubble style
     */
    public @StyleRes int getIncomingMessageBubbleStyle() {
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
                                        R.attr.cometchatMessageListIncomingMessageBubbleStyle,
                                        incomingMessageBubbleStyle);
            extractAttributesAndApplyBubbleDefaults(typedArray, true);
            messageAdapter.setIncomingMessageBubbleStyle(incomingMessageBubbleStyle);
            this.incomingMessageBubbleStyle = incomingMessageBubbleStyle;
        }
    }

    /**
     * Returns the style resource ID for the bubble of outgoing messages.
     *
     * @return the resource ID for the outgoing message bubble style
     */
    public @StyleRes int getOutgoingMessageBubbleStyle() {
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
                                        R.attr.cometchatMessageListOutgoingMessageBubbleStyle,
                                        outgoingMessageBubbleStyle);
            extractAttributesAndApplyBubbleDefaults(typedArray, false);
            messageAdapter.setOutgoingMessageBubbleStyle(outgoingMessageBubbleStyle);
            this.outgoingMessageBubbleStyle = outgoingMessageBubbleStyle;
        }
    }

    /**
     * Sets the moderation view style for the message list.
     */
    public void setModerationViewStyle(@StyleRes int moderationViewStyle) {
        messageAdapter.setModerationViewStyle(moderationViewStyle);
    }

    /**
     * Returns the style resource ID for the date separator between messages.
     *
     * @return the resource ID for the date separator style
     */
    public @StyleRes int getDateSeparatorStyle() {
        return dateSeparatorStyle;
    }

    /**
     * Sets the date separator style for the message list.
     *
     * @param dateStyle The DateStyle object defining the style of date separators.
     */
    public void setDateSeparatorStyle(@StyleRes int dateStyle) {
        this.dateSeparatorStyle = dateStyle;
        messageAdapter.setDateSeparatorStyle(dateStyle);
    }

    /**
     * Returns the style resource ID for the delete dialog. This style customizes
     * the appearance of the delete dialog box.
     *
     * @return the resource ID for the delete dialog style
     */
    public @StyleRes int getDeleteDialogStyle() {
        return deleteDialogStyle;
    }

    /**
     * Sets the style for the message delete dialog.
     *
     * @param style The delete object defining the style of the message delete dialog.
     */
    public void setDeleteDialogStyle(@StyleRes int style) {
        this.deleteDialogStyle = style;
    }

    /**
     * Returns the color integer for the error state title text. This color
     * customizes the title text color in the error state.
     *
     * @return the color for the error state title text
     */
    public @ColorInt int getErrorStateTitleTextColor() {
        return errorStateTitleTextColor;
    }

    /**
     * Sets the color for the error state title text.
     *
     * @param errorStateTitleTextColor The color to be set for the error state title text.
     */
    public void setErrorStateTitleTextColor(@ColorInt int errorStateTitleTextColor) {
        this.errorStateTitleTextColor = errorStateTitleTextColor;
        errorTextView.setTextColor(errorStateTitleTextColor);
    }

    /**
     * Returns the color integer for the error state subtitle text. This color
     * customizes the subtitle text color in the error state.
     *
     * @return the color for the error state subtitle text
     */
    public @ColorInt int getErrorStateSubtitleTextColor() {
        return errorStateSubtitleTextColor;
    }

    /**
     * Sets the color for the error state subtitle text.
     *
     * @param errorStateSubtitleTextColor The color to be set for the error state subtitle text.
     */
    public void setErrorStateSubtitleTextColor(@ColorInt int errorStateSubtitleTextColor) {
        this.errorStateSubtitleTextColor = errorStateSubtitleTextColor;
        errorSubtitleTextView.setTextColor(errorStateSubtitleTextColor);
    }

    /**
     * Returns the text appearance resource ID for the error state title. This
     * appearance defines the style for the title text in the error state.
     *
     * @return the resource ID for the error state title text appearance
     */
    public @StyleRes int getErrorStateTitleTextAppearance() {
        return errorStateTitleTextAppearance;
    }

    /**
     * Sets the text appearance for the error state title text of the message list.
     *
     * @param appearance The style resource for the error state text appearance.
     */
    public void setErrorStateTitleTextAppearance(@StyleRes int appearance) {
        this.errorStateTitleTextAppearance = appearance;
        errorTextView.setTextAppearance(appearance);
    }

    /**
     * Returns the text appearance resource ID for the error state subtitle. This
     * appearance defines the style for the subtitle text in the error state.
     *
     * @return the resource ID for the error state subtitle text appearance
     */
    public @StyleRes int getErrorStateSubtitleTextAppearance() {
        return errorStateSubtitleTextAppearance;
    }

    /**
     * Sets the text appearance for the error state subtitle text of the message
     * list.
     *
     * @param appearance The style resource for the error state text appearance.
     */
    public void setErrorStateSubtitleTextAppearance(@StyleRes int appearance) {
        this.errorStateSubtitleTextAppearance = appearance;
        errorSubtitleTextView.setTextAppearance(appearance);
    }

    /**
     * Retrieves the style resource for message information.
     *
     * @return the message information style resource ID.
     */
    public @StyleRes int getMessageInformationStyle() {
        return messageInformationStyle;
    }

    /**
     * Returns the text appearance resource ID for the empty chat subtitle. This
     * appearance defines the style for the subtitle text in the empty chat state.
     *
     * @return the resource ID for the empty chat subtitle text appearance
     */
    public @StyleRes int getAiAssistantEmptyChatSubtitleTextAppearance() {
        return aiAssistantEmptyChatSubtitleTextAppearance;
    }

    /**
     * Sets the text appearance for the empty chat subtitle text of the message list.
     *
     * @param resourceId The style resource for the empty chat subtitle text appearance.
     */
    private void setAiAssistantEmptyChatSubtitleTextAppearance(@StyleRes int resourceId) {
        this.aiAssistantEmptyChatSubtitleTextAppearance = resourceId;
        aiAssistantEmptyChatGreetingSubtitleTextView.setTextAppearance(resourceId);
    }

    /**
     * Returns the color integer for the empty chat subtitle text. This color
     * customizes the subtitle text color in the empty chat state.
     *
     * @return the color for the empty chat subtitle text
     */
    public @ColorInt int getAiAssistantEmptyChatSubtitleTextColor() {
        return aiAssistantEmptyChatSubtitleTextColor;
    }

    /**
     * Sets the color for the empty chat subtitle text.
     *
     * @param color The color to be set for the empty chat subtitle text.
     */
    private void setAiAssistantEmptyChatSubtitleTextColor(@ColorInt int color) {
        this.aiAssistantEmptyChatSubtitleTextColor = color;
        aiAssistantEmptyChatGreetingSubtitleTextView.setTextColor(color);
    }

    /**
     * Returns the text appearance resource ID for the empty chat title. This
     * appearance defines the style for the title text in the empty chat state.
     *
     * @return the resource ID for the empty chat title text appearance
     */
    public @StyleRes int getAiAssistantEmptyChatTitleTextAppearance() {
        return aiAssistantEmptyChatTitleTextAppearance;
    }

    /**
     * Sets the text appearance for the empty chat title text of the message list.
     *
     * @param resourceId The style resource for the empty chat title text appearance.
     */
    private void setAiAssistantEmptyChatTitleTextAppearance(@StyleRes int resourceId) {
        this.aiAssistantEmptyChatTitleTextAppearance = resourceId;
        aiAssistantEmptyChatGreetingTextView.setTextAppearance(resourceId);
    }

    /**
     * Returns the color integer for the empty chat title text. This color
     * customizes the title text color in the empty chat state.
     *
     * @return the color for the empty chat title text
     */
    public @ColorInt int getAiAssistantEmptyChatTitleTextColor() {
        return aiAssistantEmptyChatTitleTextColor;
    }

    /**
     * Sets the color for the empty chat title text.
     *
     * @param color The color to be set for the empty chat title text.
     */
    private void setAiAssistantEmptyChatTitleTextColor(@ColorInt int color) {
        this.aiAssistantEmptyChatTitleTextColor = color;
        aiAssistantEmptyChatGreetingTextView.setTextColor(color);
    }

    /** Returns the corner radius for the AI assistant suggested message bubble.
     *
     * @return the corner radius in pixels
     */
    public float getAiAssistantSuggestedMessageCornerRadius() {
        return aiAssistantSuggestedMessageCornerRadius;
    }

    /** Sets the corner radius for the AI assistant suggested message bubble.
     *
     * @param dimension The corner radius in pixels.
     */
    private void setAiAssistantSuggestedMessageCornerRadius(float dimension) {
        this.aiAssistantSuggestedMessageCornerRadius = dimension;
    }

    /** Returns the stroke width for the AI assistant suggested message bubble.
     *
     * @return the stroke width in pixels
     */
    public int getAiAssistantSuggestedMessageStrokeWidth() {
        return aiAssistantSuggestedMessageStrokeWidth;
    }

    /** Sets the stroke width for the AI assistant suggested message bubble.
     *
     * @param dimensionPixelSize The stroke width in pixels.
     */
    private void setAiAssistantSuggestedMessageStrokeWidth(int dimensionPixelSize) {
        this.aiAssistantSuggestedMessageStrokeWidth = dimensionPixelSize;
    }

    /** Returns the resource ID for the end icon of the AI assistant suggested message.
     *
     * @return the resource ID of the end icon
     */
    public Drawable getAiAssistantSuggestedMessageEndIcon() {
        return aiAssistantSuggestedMessageEndIcon;
    }

    /** Sets the drawable for the end icon of the AI assistant suggested message.
     *
     * @param drawable The drawable to be set for the end icon.
     */
    private void setAiAssistantSuggestedMessageEndIcon(Drawable drawable) {
        this.aiAssistantSuggestedMessageEndIcon = drawable;
    }

    /** Returns the color integer for the end icon tint of the AI assistant suggested message.
     *
     * @return the color for the end icon tint
     */
    public @ColorInt int getAiAssistantSuggestedMessageEndIconTint() {
        return aiAssistantSuggestedMessageEndIconTint;
    }

    /** Sets the color for the end icon tint of the AI assistant suggested message.
     *
     * @param color The color to be set for the end icon tint.
     */
    private void setAiAssistantSuggestedMessageEndIconTint(@ColorInt int color) {
        this.aiAssistantSuggestedMessageEndIconTint = color;
    }

    /** Returns the background color integer for the AI assistant suggested message bubble.
     *
     * @return the background color for the suggested message bubble
     */
    public @ColorInt int getAiAssistantSuggestedMessageBackgroundColor() {
        return aiAssistantSuggestedMessageBackgroundColor;
    }

    /** Sets the background color for the AI assistant suggested message bubble.
     *
     * @param color The color to be set for the suggested message bubble background.
     */
    private void setAiAssistantSuggestedMessageBackgroundColor(@ColorInt int color) {
        this.aiAssistantSuggestedMessageBackgroundColor = color;
    }

    /** Returns the stroke color integer for the AI assistant suggested message bubble.
     *
     * @return the stroke color for the suggested message bubble
     */
    public int getAiAssistantSuggestedMessageStrokeColor() {
        return aiAssistantSuggestedMessageStrokeColor;
    }

    /** Sets the stroke color for the AI assistant suggested message bubble.
     *
     * @param color The color to be set for the suggested message bubble stroke.
     */
    private void setAiAssistantSuggestedMessageStrokeColor(@ColorInt int color) {
        this.aiAssistantSuggestedMessageStrokeColor = color;
    }

    /** Returns the text appearance resource ID for the AI assistant suggested message.
     *
     * @return the resource ID for the suggested message text appearance
     */
    public @StyleRes int getAiAssistantSuggestedMessageTextAppearance() {
        return aiAssistantSuggestedMessageTextAppearance;
    }

    /** Sets the text appearance for the AI assistant suggested message.
     *
     * @param textAppearance The style resource for the suggested message text appearance.
     */
    private void setAiAssistantSuggestedMessageTextAppearance(@StyleRes int textAppearance) {
        this.aiAssistantSuggestedMessageTextAppearance = textAppearance;
    }

    /** Returns the text color integer for the AI assistant suggested message.
     *
     * @return the text color for the suggested message
     */
    public @ColorInt int getAiAssistantSuggestedMessageTextColor() {
        return aiAssistantSuggestedMessageTextColor;
    }

    /** Sets the text color for the AI assistant suggested message.
     *
     * @param color The color to be set for the suggested message text.
     */
    private void setAiAssistantSuggestedMessageTextColor(@ColorInt int color) {
        this.aiAssistantSuggestedMessageTextColor = color;
    }

    /** Returns the separator color integer for the new message indicator.
     *
     * @return the separator color for the new message indicator
     */
    public int getNewMessageIndicatorSeparatorColor() {
        return newMessageIndicatorSeparatorColor;
    }

    /** Sets the separator color for the new message indicator.
     *
     * @param newMessageIndicatorSeparatorColor The color to be set for the separator.
     */
    public void setNewMessageIndicatorSeparatorColor(int newMessageIndicatorSeparatorColor) {
        this.newMessageIndicatorSeparatorColor = newMessageIndicatorSeparatorColor;
        messageAdapter.setNewMessageIndicatorSeparatorColor(newMessageIndicatorSeparatorColor);
    }

    /** Returns the text color integer for the new message indicator.
     *
     * @return the text color for the new message indicator
     */
    public int getNewMessageIndicatorTextColor() {
        return newMessageIndicatorTextColor;
    }

    /** Sets the text color for the new message indicator.
     *
     * @param newMessageIndicatorTextColor The color to be set for the text.
     */
    public void setNewMessageIndicatorTextColor(int newMessageIndicatorTextColor) {
        this.newMessageIndicatorTextColor = newMessageIndicatorTextColor;
        messageAdapter.setNewMessageIndicatorTextColor(newMessageIndicatorTextColor);
    }

    /** Returns the text appearance resource ID for the new message indicator.
     *
     * @return the resource ID for the new message indicator text appearance
     */
    public int getNewMessageIndicatorTextAppearance() {
        return newMessageIndicatorTextAppearance;
    }

    /** Sets the text appearance for the new message indicator.
     *
     * @param newMessageIndicatorTextAppearance The style resource for the new message indicator text appearance.
     */
    public void setNewMessageIndicatorTextAppearance(int newMessageIndicatorTextAppearance) {
        this.newMessageIndicatorTextAppearance = newMessageIndicatorTextAppearance;
        messageAdapter.setNewMessageIndicatorTextAppearance(newMessageIndicatorTextAppearance);
    }

    /**
     * Sets the style resource for message information.
     *
     * @param messageInformationStyle the style resource ID to set.
     */
    public void setMessageInformationStyle(@StyleRes int messageInformationStyle) {
        this.messageInformationStyle = messageInformationStyle;
    }

    /**
     * Retrieves the style resource for the message option sheet.
     *
     * @return the message option sheet style resource ID.
     */
    public @StyleRes int getMessageOptionSheetStyle() {
        return messageOptionSheetStyle;
    }

    /**
     * Sets the style resource for the message option sheet.
     *
     * @param messageOptionSheetStyle the style resource ID to set.
     */
    public void setMessageOptionSheetStyle(@StyleRes int messageOptionSheetStyle) {
        this.messageOptionSheetStyle = messageOptionSheetStyle;
    }

    /**
     * Retrieves the style resource for the reaction list.
     *
     * @return the reaction list style resource ID.
     */
    public @StyleRes int getReactionListStyle() {
        return reactionListStyle;
    }

    /**
     * Sets the style resource for the reaction list.
     *
     * @param reactionListStyle the style resource ID to set.
     */
    public void setReactionListStyle(@StyleRes int reactionListStyle) {
        this.reactionListStyle = reactionListStyle;
    }

    /** Sets the suggested messages for the AI assistant.
     *
     * @param suggestedMessages The list of suggested messages to set.
     */
    public void setAIAssistantSuggestedMessages(List<String> suggestedMessages) {
        this.aiAssistantSuggestedMessages = suggestedMessages;
    }

    /** Sets the visibility of the AI assistant suggested messages.
     *
     * @param aiAssistantSuggestedMessagesVisibility true to show, false to hide.
     */
    public void setAiAssistantSuggestedMessagesVisibility(int aiAssistantSuggestedMessagesVisibility) {
        this.aiAssistantSuggestedMessagesVisibility = aiAssistantSuggestedMessagesVisibility;

    }

    /** Sets the tools available for the AI assistant.
     *
     * @param aiAssistantTools A HashMap containing tool names as keys and their corresponding ToolCallListener as values.
     */
    public void setAiAssistantTools(@NonNull HashMap<String,ToolCallListener> aiAssistantTools) {
        this.toolCallListenerHashMap = aiAssistantTools;
        CometChatAIStreamService.setAiAssistantTools(aiAssistantTools);
    }

    public void refreshStyle() {
        if (style != 0) {
            setStyle(style);
        } else applyStyleAttributes(attrs, defStyleAttr, 0);
    }

    /** Sets the streaming speed for AI responses.
     *
     * @param streamingSpeed The speed at which AI responses are streamed, in milliseconds.
     */
    public void setStreamingSpeed(Integer streamingSpeed) {
        this.streamingSpeed = streamingSpeed;
        CometChatAIStreamService.setStreamDelay(streamingSpeed);
    }

    /**
     * Retrieves the listener for reaction clicks.
     *
     * @return the ReactionClickListener.
     */
    public ReactionClickListener getQuickReactionClickListener() {
        return quickReactionClickListener;
    }

    /**
     * Sets the listener for reaction clicks.
     *
     * @param quickReactionClickListener the ReactionClickListener to set.
     */
    public void setQuickReactionClickListener(ReactionClickListener quickReactionClickListener) {
        this.quickReactionClickListener = quickReactionClickListener;
    }

    /**
     * Retrieves the listener for message option clicks.
     *
     * @return the MessageOptionClickListener.
     */
    public MessageOptionClickListener getMessageOptionClickListener() {
        return messageOptionClickListener;
    }

    /**
     * Sets the listener for message option clicks.
     *
     * @param messageOptionClickListener the MessageOptionClickListener to set.
     */
    public void setMessageOptionClickListener(MessageOptionClickListener messageOptionClickListener) {
        this.messageOptionClickListener = messageOptionClickListener;
    }

    /**
     * Retrieves the callback for reaction click events.
     *
     * @return An instance of {@link OnReactionClick} that handles reaction click events.
     */
    public OnReactionClick getOnReactionClick() {
        return onReactionClick;
    }

    /**
     * Sets the callback for reaction click events.
     * Updates the message adapter with the provided reaction click listener.
     *
     * @param onReactionClick An instance of {@link OnReactionClick} to be invoked when a reaction is clicked.
     */
    public void setOnReactionClick(OnReactionClick onReactionClick) {
        this.onReactionClick = onReactionClick;
        messageAdapter.setOnReactionClick(onReactionClick);
    }

    /**
     * Retrieves the callback for reaction long-click events.
     *
     * @return An instance of {@link OnReactionLongClick} that handles reaction long-click events.
     */
    public OnReactionLongClick getOnReactionLongClick() {
        return onReactionLongClick;
    }

    /**
     * Sets the callback for reaction long-click events.
     * Updates the message adapter with the provided reaction long-click listener.
     *
     * @param onReactionLongClick An instance of {@link OnReactionLongClick} to be invoked when a reaction is long-clicked.
     */
    public void setOnReactionLongClick(OnReactionLongClick onReactionLongClick) {
        this.onReactionLongClick = onReactionLongClick;
        messageAdapter.setOnReactionLongClick(onReactionLongClick);
    }

    /**
     * Retrieves the callback for the "Add More Reactions" click event.
     *
     * @return An instance of {@link OnAddMoreReactionsClick} that handles add-more-reactions click events.
     */
    public OnAddMoreReactionsClick getOnAddMoreReactionsClick() {
        return onAddMoreReactionsClick;
    }

    /**
     * Sets the callback for the "Add More Reactions" click event.
     * Updates the message adapter with the provided add-more-reactions click listener.
     *
     * @param onAddMoreReactionsClick An instance of {@link OnAddMoreReactionsClick} to be invoked when
     *                                the "Add More Reactions" button is clicked.
     */
    public void setOnAddMoreReactionsClick(OnAddMoreReactionsClick onAddMoreReactionsClick) {
        this.onAddMoreReactionsClick = onAddMoreReactionsClick;
        messageAdapter.setOnAddMoreReactionsClick(onAddMoreReactionsClick);
    }

    /**
     * Retrieves the configured addition parameter object.
     *
     * @return the AdditionParameter.
     */
    public AdditionParameter getAdditionParameter() {
        return additionParameter;
    }

    /**
     * Retrieves the listener for emoji picker clicks.
     *
     * @return the EmojiPickerClickListener.
     */
    public EmojiPickerClickListener getEmojiPickerClick() {
        return emojiPickerClickListener;
    }

    /**
     * Sets the listener for emoji picker clicks.
     *
     * @param emojiPickerClickListener the EmojiPickerClickListener to set.
     */
    public void setEmojiPickerClick(EmojiPickerClickListener emojiPickerClickListener) {
        this.emojiPickerClickListener = emojiPickerClickListener;
    }

    /**
     * Checks whether sound is disabled for incoming messages.
     *
     * @return {@code true} if message sound is disabled, {@code false} otherwise.
     */
    public boolean isDisableSoundForMessages() {
        return disableSoundForMessages;
    }

    /**
     * Retrieves the custom sound ID for incoming messages.
     *
     * @return An integer representing the custom sound resource ID.
     */
    public int getCustomSoundForMessages() {
        return customSoundForMessages;
    }

    /**
     * Sets a custom sound resource for incoming messages in the message list.
     *
     * @param sound The resource ID of the custom sound. Pass 0 to use the default
     *              sound.
     */
    public void setCustomSoundForMessages(@RawRes int sound) {
        if (sound != 0) this.customSoundForMessages = sound;
    }

    /**
     * Retrieves the error callback.
     *
     * @return An instance of {@link OnError} that handles error events.
     */
    public OnError getOnError() {
        return onError;
    }

    /**
     * Sets the callback for handling errors in the message list.
     *
     * @param onError The OnError object representing the error callback.
     */
    public void setOnError(OnError onError) {
        this.onError = onError;
    }

    /**
     * Retrieves the callback for handling data loading events.
     *
     * @return An instance of {@link OnLoad} for handling the loading of {@link BaseMessage} objects.
     */
    public OnLoad<BaseMessage> getOnLoad() {
        return onLoad;
    }

    /**
     * Sets the callback for handling data loading events.
     *
     * @param onLoad An instance of {@link OnLoad} that is triggered when messages are being loaded.
     */
    public void setOnLoad(OnLoad<BaseMessage> onLoad) {
        this.onLoad = onLoad;
    }

    /**
     * Retrieves the callback for handling empty state events.
     *
     * @return An instance of {@link OnEmpty} triggered when no messages are available.
     */
    public OnEmpty getOnEmpty() {
        return onEmpty;
    }

    /**
     * Sets the callback for handling empty state events.
     *
     * @param onEmpty An instance of {@link OnEmpty} that is triggered when there are no messages available.
     */
    public void setOnEmpty(OnEmpty onEmpty) {
        this.onEmpty = onEmpty;
    }

    /**
     * Retrieves the list of text formatters applied to messages.
     *
     * @return A list of {@link CometChatTextFormatter} instances used for text formatting.
     */
    public List<CometChatTextFormatter> getTextFormatters() {
        return textFormatters;
    }

    /**
     * Sets the list of custom text formatters.
     *
     * @param cometchatTextFormatters A list of {@link CometChatTextFormatter} to add to the current
     *                                list of formatters. If the provided list is not null, it will add
     *                                these formatters to the existing ones and process them.
     */
    public void setTextFormatters(List<CometChatTextFormatter> cometchatTextFormatters) {
        if (cometchatTextFormatters != null) {
            this.textFormatters = cometchatTextFormatters;
            processFormatters();
        }
    }

    /**
     * Retrieves the parent message ID for thread replies.
     *
     * @return An integer representing the parent message ID.
     */
    public long getParentMessageId() {
        return parentMessageId;
    }

    /**
     * Retrieves the callback for reaction list item clicks.
     *
     * @return An instance of {@link OnReactionListItemClick} triggered when a reaction list item is clicked.
     */
    public OnReactionListItemClick getOnReactionListItemClick() {
        return onReactionListItemClick;
    }

    /**
     * Sets the item click listener for the ReactedUsers.
     *
     * @param onReactionListItemClick The item click listener to set.
     */
    public void setOnReactionListItemClick(OnReactionListItemClick onReactionListItemClick) {
        this.onReactionListItemClick = onReactionListItemClick;
    }

    /**
     * Retrieves the callback for handling thread reply clicks.
     *
     * @return An instance of {@link ThreadReplyClick} triggered when a thread reply is clicked.
     */
    public ThreadReplyClick getOnThreadRepliesClick() {
        return onThreadRepliesClick;
    }

    /**
     * Sets the listener for handling thread reply clicks in the message list.
     *
     * @param onThreadRepliesClick The ThreadReplyClick object representing the listener.
     */
    public void setOnThreadRepliesClick(ThreadReplyClick onThreadRepliesClick) {
        if (onThreadRepliesClick != null) {
            this.onThreadRepliesClick = onThreadRepliesClick;
            messageAdapter.setThreadReplyClick(onThreadRepliesClick);
        }
    }

    /**
     * Retrieves the builder for creating a reaction request.
     *
     * @return An instance of {@link ReactionsRequest.ReactionsRequestBuilder} for configuring reaction requests.
     */
    public ReactionsRequest.ReactionsRequestBuilder getReactionRequestBuilder() {
        return reactionRequestBuilder;
    }

    /**
     * Retrieves the style ID for smart replies.
     *
     * @return An integer representing the smart replies style.
     */
    public int getSmartRepliesStyle() {
        return smartRepliesStyle;
    }

    /**
     * Retrieves the style ID for conversation starters.
     *
     * @return An integer representing the conversation starter style.
     */
    public int getConversationStarterStyle() {
        return conversationStarterStyle;
    }

    /**
     * Gets the visibility of the moderation view.
     * @return the visibility of the moderation view.
     */
    public int getModerationViewVisibility() {
        return moderationViewVisibility;
    }

    /**
     * Sets the visibility of the moderation view.
     * @param visibility the visibility to set for the moderation view.
     */
    public void setModerationViewVisibility(int visibility) {
        this.moderationViewVisibility = visibility;
        messageAdapter.setModerationViewVisibility(visibility != View.VISIBLE);
    }

    /**
     * Sets a custom label for the "mention all" feature for a specific ID.
     *
     * @param id The unique identifier (such as a group or user ID) for which the mention all label should be set.
     * @param mentionAllLabel The custom label to display when mentioning all members.
     *
     * If either parameter is null or empty, or if the mentions formatter is not initialized, this method does nothing.
     */
    public void setMentionAllLabelId(String id, String mentionAllLabel) {
        if (id != null && !id.isEmpty() && mentionAllLabel != null && !mentionAllLabel.isEmpty()) {
            if (cometchatMentionsFormatter != null) {
                cometchatMentionsFormatter.setMentionAllLabel(id, mentionAllLabel);
            }
            this.mentionAllLabelId = id;
            this.mentionAllLabel = mentionAllLabel;
        }
    }

    /**
     * Called when the view is attached to the window. This method adds a listener
     * to the message list ViewModel and processes any formatters needed for the UI.
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isDetachedFromWindow) {
            attachObservers();
            isDetachedFromWindow = false;
        }
        messageListViewModel.addListener();
        processFormatters();
    }

    /**
     * Called when the view is detached from the window. This method dismisses the
     * bottom sheet dialog if it is currently showing and removes the listener from
     * the message list ViewModel.
     */
    @Override
    protected void onDetachedFromWindow() {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
        AudioPlayer.getInstance().stop();
        messageListViewModel.removeListener();
        disposeObservers();
        isDetachedFromWindow = true;
        super.onDetachedFromWindow();
    }

    public void disposeObservers() {
        if (lifecycleOwner != null) {
            messageListViewModel.getMutableMessageList().removeObservers(lifecycleOwner);
            messageListViewModel.messagesRangeChanged().removeObservers(lifecycleOwner);
            messageListViewModel.updateMessage().removeObservers(lifecycleOwner);
            messageListViewModel.getOnMessageDeleted().removeObservers(lifecycleOwner);
            messageListViewModel.addMessage().removeObservers(lifecycleOwner);
            messageListViewModel.getCometChatException().removeObservers(lifecycleOwner);
            messageListViewModel.removeMessage().removeObservers(lifecycleOwner);
            messageListViewModel.getMutableIsInProgress().removeObservers(lifecycleOwner);
            messageListViewModel.getMutableHasMore().removeObservers(lifecycleOwner);
            messageListViewModel.notifyUpdate().removeObservers(lifecycleOwner);
            messageListViewModel.getStates().removeObservers(lifecycleOwner);
            messageListViewModel.getMessageDeleteState().removeObservers(lifecycleOwner);
            messageListViewModel.closeTopPanel().removeObservers(lifecycleOwner);
            messageListViewModel.closeBottomPanel().removeObservers(lifecycleOwner);
            messageListViewModel.showTopPanel().removeObservers(lifecycleOwner);
            messageListViewModel.showBottomPanel().removeObservers(lifecycleOwner);
            messageListViewModel.getMutableSmartReplies().removeObservers(lifecycleOwner);
            messageListViewModel.getMutableConversationStarterReplies().removeObservers(lifecycleOwner);
            messageListViewModel.getRemoveConversationStarter().removeObservers(lifecycleOwner);
            messageListViewModel.getConversationStarterUIState().removeObservers(lifecycleOwner);
            messageListViewModel.getSmartRepliesUIState().removeObservers(lifecycleOwner);
            messageListViewModel.messagesRangeChangedAtEnd().removeObservers(lifecycleOwner);
            messageListViewModel.getMutableHasMoreNewMessages().removeObservers(lifecycleOwner);
            messageListViewModel.getMutableHasMorePreviousMessages().removeObservers(lifecycleOwner);
            messageListViewModel.getScrollToMessageId().removeObservers(lifecycleOwner);
            messageListViewModel.getMutableConversationSummary().removeObservers(lifecycleOwner);
            messageListViewModel.getRemoveConversationSummary().removeObservers(lifecycleOwner);
            messageListViewModel.getConversationSummaryUIState().removeObservers(lifecycleOwner);
        }
    }

    /**
     * Interface for handling thread reply clicks.
     */
    public interface ThreadReplyClick {
        void onThreadReplyClick(Context context, BaseMessage baseMessage, CometChatMessageTemplate cometchatMessageTemplate);
    }
}
