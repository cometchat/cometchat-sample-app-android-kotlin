package com.cometchat.chatuikit.messagelist;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.extensions.ExtensionConstants;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.interfaces.DateTimeFormatterCallback;
import com.cometchat.chatuikit.shared.models.CometChatMessageOption;
import com.cometchat.chatuikit.shared.models.CometChatMessageTemplate;
import com.cometchat.chatuikit.shared.resources.localise.CometChatLocalize;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.sticker_header.StickyHeaderAdapter;
import com.cometchat.chatuikit.shared.utils.MessageBubbleUtils;
import com.cometchat.chatuikit.shared.utils.MessageReceiptUtils;
import com.cometchat.chatuikit.shared.views.date.CometChatDate;
import com.cometchat.chatuikit.shared.views.date.Pattern;
import com.cometchat.chatuikit.shared.views.messagebubble.CometChatMessageBubble;
import com.cometchat.chatuikit.shared.views.messagereceipt.Receipt;
import com.cometchat.chatuikit.shared.views.reaction.CometChatReaction;
import com.cometchat.chatuikit.shared.views.reaction.interfaces.OnAddMoreReactionsClick;
import com.cometchat.chatuikit.shared.views.reaction.interfaces.OnReactionClick;
import com.cometchat.chatuikit.shared.views.reaction.interfaces.OnReactionLongClick;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements StickyHeaderAdapter<MessageAdapter.DateItemHolder> {
    private static final String TAG = MessageAdapter.class.getSimpleName();
    // Message Type Constants
    private static final String LEFT_MESSAGE = "1";
    private static final String RIGHT_MESSAGE = "2";
    private static final String CENTER_MESSAGE = "3";
    private static final String IGNORE_MESSAGE = "10000";
    // Context and Message List
    private final Context context;
    private final OnMessageLongClick onMessageLongClick;
    // Layout and Margins
    private final int layoutDirection;
    // Reaction Handling
    private final HashMap<Integer, CometChatMessageTemplate> viewTypeTemplateHashMap;
    private OnAddMoreReactionsClick onAddMoreReactionsClick;
    private OnReactionClick onReactionClick;
    private OnReactionLongClick onReactionLongClick;
    private List<BaseMessage> baseMessageList;
    // Interaction and Configuration
    private CometChatMessageList.ThreadReplyClick threadReplyClick;
    private boolean disableReadReceipt;
    private UIKitConstants.MessageListAlignment listAlignment = UIKitConstants.MessageListAlignment.STANDARD;
    private boolean showAvatar = false;
    private boolean showLeftBubbleUserAvatar = false;
    private boolean showLeftBubbleGroupAvatar = true;
    private SimpleDateFormat timeFormat;
    private SimpleDateFormat dateSeparatorFormat;
    private DateTimeFormatterCallback dateTimeFormatter;
    private UIKitConstants.TimeStampAlignment timeStampAlignment = UIKitConstants.TimeStampAlignment.BOTTOM;
    // Message Template and Style Configuration
    private HashMap<String, CometChatMessageTemplate> messageTemplateHashMap;
    private HashMap<String, Integer> messageViewTypeHashMap;
    // User and Group Information
    private String type;
    private User user;
    private Group group;
    private int leftBubbleMarginTop = -1, leftBubbleMarginBottom = -1, leftBubbleMarginStart = -1, leftBubbleMarginEnd = -1; // Margins for left bubble
    private int rightBubbleMarginTop = -1, rightBubbleMarginBottom = -1, rightBubbleMarginStart = -1, rightBubbleMarginEnd = -1; // Margins for right bubble
    private int reactionChipSize = 0;
    private int plusReactionChipSize = 0;
    private boolean disableReactions;
    private int threadVisibility = View.VISIBLE;

    // Date Separator and General Styles
    private @StyleRes int dateSeparatorStyle;

    // Incoming Message Bubble Customization
    private @ColorInt int incomingMessageBubbleBackgroundColor;
    private @Dimension int incomingMessageBubbleCornerRadius;
    private @Dimension int incomingMessageBubbleStrokeWidth;
    private @ColorInt int incomingMessageBubbleStrokeColor;
    private Drawable incomingMessageBubbleBackgroundDrawable;
    private @StyleRes int incomingMessageBubbleDateStyle;
    private @StyleRes int incomingMessageBubbleReceiptStyle;
    private @StyleRes int incomingMessageBubbleAvatarStyle;
    private @StyleRes int incomingMessageBubbleThreadIndicatorTextAppearance;
    private @ColorInt int incomingMessageBubbleThreadIndicatorTextColor;
    private @ColorInt int incomingMessageBubbleThreadIndicatorIconTint;
    private @StyleRes int incomingMessageBubbleSenderNameTextAppearance;
    private @ColorInt int incomingMessageBubbleSenderNameTextColor;
    private @StyleRes int incomingMessageBubbleReactionStyle;

    // Outgoing Message Bubble Customization
    private @ColorInt int outgoingMessageBubbleBackgroundColor;
    private @Dimension int outgoingMessageBubbleCornerRadius;
    private @Dimension int outgoingMessageBubbleStrokeWidth;
    private @ColorInt int outgoingMessageBubbleStrokeColor;
    private Drawable outgoingMessageBubbleBackgroundDrawable;
    private @StyleRes int outgoingMessageBubbleDateStyle;
    private @StyleRes int outgoingMessageBubbleReceiptStyle;
    private @StyleRes int outgoingMessageBubbleAvatarStyle;
    private @StyleRes int outgoingMessageBubbleThreadIndicatorTextAppearance;
    private @ColorInt int outgoingMessageBubbleThreadIndicatorTextColor;
    private @ColorInt int outgoingMessageBubbleThreadIndicatorIconTint;
    private @StyleRes int outgoingMessageBubbleSenderNameTextAppearance;
    private @ColorInt int outgoingMessageBubbleSenderNameTextColor;
    private @StyleRes int outgoingMessageBubbleReactionStyle;

    // Incoming Text Bubble Customization
    private @ColorInt int incomingTextBubbleBackgroundColor;
    private @Dimension int incomingTextBubbleCornerRadius;
    private @Dimension int incomingTextBubbleStrokeWidth;
    private @ColorInt int incomingTextBubbleStrokeColor;
    private Drawable incomingTextBubbleBackgroundDrawable;
    private @StyleRes int incomingTextBubbleDateStyle;
    private @StyleRes int incomingTextBubbleReceiptStyle;
    private @StyleRes int incomingTextBubbleAvatarStyle;
    private @StyleRes int incomingTextBubbleThreadIndicatorTextAppearance;
    private @ColorInt int incomingTextBubbleThreadIndicatorTextColor;
    private @ColorInt int incomingTextBubbleThreadIndicatorIconTint;
    private @StyleRes int incomingTextBubbleSenderNameTextAppearance;
    private @ColorInt int incomingTextBubbleSenderNameTextColor;

    // Outgoing Text Bubble Customization
    private @ColorInt int outgoingDeleteBubbleBackgroundColor;
    private @Dimension int outgoingDeleteBubbleCornerRadius;
    private @Dimension int outgoingDeleteBubbleStrokeWidth;
    private @ColorInt int outgoingDeleteBubbleStrokeColor;
    private Drawable outgoingDeleteBubbleBackgroundDrawable;
    private @StyleRes int outgoingDeleteBubbleDateStyle;
    private @StyleRes int outgoingDeleteBubbleAvatarStyle;
    private @StyleRes int outgoingDeleteBubbleSenderNameTextAppearance;
    private @ColorInt int outgoingDeleteBubbleSenderNameTextColor;

    // Incoming Deleted Bubble Customization
    private @ColorInt int incomingDeleteBubbleBackgroundColor;
    private @Dimension int incomingDeleteBubbleCornerRadius;
    private @Dimension int incomingDeleteBubbleStrokeWidth;
    private @ColorInt int incomingDeleteBubbleStrokeColor;
    private Drawable incomingDeleteBubbleBackgroundDrawable;
    private @StyleRes int incomingDeleteBubbleDateStyle;
    private @StyleRes int incomingDeleteBubbleAvatarStyle;
    private @StyleRes int incomingDeleteBubbleSenderNameTextAppearance;
    private @ColorInt int incomingDeleteBubbleSenderNameTextColor;

    // Outgoing Deleted Bubble Customization
    private @ColorInt int outgoingTextBubbleBackgroundColor;
    private @Dimension int outgoingTextBubbleCornerRadius;
    private @Dimension int outgoingTextBubbleStrokeWidth;
    private @ColorInt int outgoingTextBubbleStrokeColor;
    private Drawable outgoingTextBubbleBackgroundDrawable;
    private @StyleRes int outgoingTextBubbleDateStyle;
    private @StyleRes int outgoingTextBubbleReceiptStyle;
    private @StyleRes int outgoingTextBubbleAvatarStyle;
    private @StyleRes int outgoingTextBubbleThreadIndicatorTextAppearance;
    private @ColorInt int outgoingTextBubbleThreadIndicatorTextColor;
    private @ColorInt int outgoingTextBubbleThreadIndicatorIconTint;
    private @StyleRes int outgoingTextBubbleSenderNameTextAppearance;
    private @ColorInt int outgoingTextBubbleSenderNameTextColor;

    // Incoming Image Bubble Customization
    private @ColorInt int incomingImageBubbleBackgroundColor;
    private @Dimension int incomingImageBubbleCornerRadius;
    private @Dimension int incomingImageBubbleStrokeColor;
    private @ColorInt int incomingImageBubbleStrokeWidth;
    private Drawable incomingImageBubbleBackgroundDrawable;
    private @StyleRes int incomingImageBubbleDateStyle;
    private @StyleRes int incomingImageBubbleReceiptStyle;
    private @StyleRes int incomingImageBubbleAvatarStyle;
    private @StyleRes int incomingImageBubbleThreadIndicatorTextAppearance;
    private @ColorInt int incomingImageBubbleThreadIndicatorTextColor;
    private @ColorInt int incomingImageBubbleThreadIndicatorIconTint;
    private @StyleRes int incomingImageBubbleSenderNameTextAppearance;
    private @ColorInt int incomingImageBubbleSenderNameTextColor;

    // Outgoing Image Bubble Customization
    private @ColorInt int outgoingImageBubbleBackgroundColor;
    private @Dimension int outgoingImageBubbleCornerRadius;
    private @Dimension int outgoingImageBubbleStrokeWidth;
    private @ColorInt int outgoingImageBubbleStrokeColor;
    private Drawable outgoingImageBubbleBackgroundDrawable;
    private @StyleRes int outgoingImageBubbleDateStyle;
    private @StyleRes int outgoingImageBubbleReceiptStyle;
    private @StyleRes int outgoingImageBubbleAvatarStyle;
    private @StyleRes int outgoingImageBubbleThreadIndicatorTextAppearance;
    private @ColorInt int outgoingImageBubbleThreadIndicatorTextColor;
    private @ColorInt int outgoingImageBubbleThreadIndicatorIconTint;
    private @StyleRes int outgoingImageBubbleSenderNameTextAppearance;
    private @ColorInt int outgoingImageBubbleSenderNameTextColor;

    // Incoming File Bubble Customization
    private @ColorInt int incomingFileBubbleBackgroundColor;
    private @Dimension int incomingFileBubbleCornerRadius;
    private @Dimension int incomingFileBubbleStrokeWidth;
    private @ColorInt int incomingFileBubbleStrokeColor;
    private Drawable incomingFileBubbleBackgroundDrawable;
    private @StyleRes int incomingFileBubbleDateStyle;
    private @StyleRes int incomingFileBubbleReceiptStyle;
    private @StyleRes int incomingFileBubbleAvatarStyle;
    private @StyleRes int incomingFileBubbleThreadIndicatorTextAppearance;
    private @ColorInt int incomingFileBubbleThreadIndicatorTextColor;
    private @ColorInt int incomingFileBubbleThreadIndicatorIconTint;
    private @StyleRes int incomingFileBubbleSenderNameTextAppearance;
    private @ColorInt int incomingFileBubbleSenderNameTextColor;

    // Outgoing File Bubble Customization
    private @ColorInt int outgoingFileBubbleBackgroundColor;
    private @Dimension int outgoingFileBubbleCornerRadius;
    private @Dimension int outgoingFileBubbleStrokeWidth;
    private @ColorInt int outgoingFileBubbleStrokeColor;
    private Drawable outgoingFileBubbleBackgroundDrawable;
    private @StyleRes int outgoingFileBubbleDateStyle;
    private @StyleRes int outgoingFileBubbleReceiptStyle;
    private @StyleRes int outgoingFileBubbleAvatarStyle;
    private @StyleRes int outgoingFileBubbleThreadIndicatorTextAppearance;
    private @ColorInt int outgoingFileBubbleThreadIndicatorTextColor;
    private @ColorInt int outgoingFileBubbleThreadIndicatorIconTint;
    private @StyleRes int outgoingFileBubbleSenderNameTextAppearance;
    private @ColorInt int outgoingFileBubbleSenderNameTextColor;

    // Incoming Audio Bubble Customization
    private @ColorInt int incomingAudioBubbleBackgroundColor;
    private @Dimension int incomingAudioBubbleCornerRadius;
    private @Dimension int incomingAudioBubbleStrokeWidth;
    private @ColorInt int incomingAudioBubbleStrokeColor;
    private Drawable incomingAudioBubbleBackgroundDrawable;
    private @StyleRes int incomingAudioBubbleDateStyle;
    private @StyleRes int incomingAudioBubbleReceiptStyle;
    private @StyleRes int incomingAudioBubbleAvatarStyle;
    private @StyleRes int incomingAudioBubbleThreadIndicatorTextAppearance;
    private @ColorInt int incomingAudioBubbleThreadIndicatorTextColor;
    private @ColorInt int incomingAudioBubbleThreadIndicatorIconTint;
    private @StyleRes int incomingAudioBubbleSenderNameTextAppearance;
    private @ColorInt int incomingAudioBubbleSenderNameTextColor;

    // Outgoing Audio Bubble Customization
    private @ColorInt int outgoingAudioBubbleBackgroundColor;
    private @Dimension int outgoingAudioBubbleCornerRadius;
    private @Dimension int outgoingAudioBubbleStrokeWidth;
    private @ColorInt int outgoingAudioBubbleStrokeColor;
    private Drawable outgoingAudioBubbleBackgroundDrawable;
    private @StyleRes int outgoingAudioBubbleDateStyle;
    private @StyleRes int outgoingAudioBubbleReceiptStyle;
    private @StyleRes int outgoingAudioBubbleAvatarStyle;
    private @StyleRes int outgoingAudioBubbleThreadIndicatorTextAppearance;
    private @ColorInt int outgoingAudioBubbleThreadIndicatorTextColor;
    private @ColorInt int outgoingAudioBubbleThreadIndicatorIconTint;
    private @StyleRes int outgoingAudioBubbleSenderNameTextAppearance;
    private @ColorInt int outgoingAudioBubbleSenderNameTextColor;

    // Incoming Video Bubble Customization
    private @ColorInt int incomingVideoBubbleBackgroundColor;
    private @Dimension int incomingVideoBubbleCornerRadius;
    private @Dimension int incomingVideoBubbleStrokeWidth;
    private @ColorInt int incomingVideoBubbleStrokeColor;
    private Drawable incomingVideoBubbleBackgroundDrawable;
    private @StyleRes int incomingVideoBubbleDateStyle;
    private @StyleRes int incomingVideoBubbleReceiptStyle;
    private @StyleRes int incomingVideoBubbleAvatarStyle;
    private @StyleRes int incomingVideoBubbleThreadIndicatorTextAppearance;
    private @ColorInt int incomingVideoBubbleThreadIndicatorTextColor;
    private @ColorInt int incomingVideoBubbleThreadIndicatorIconTint;
    private @StyleRes int incomingVideoBubbleSenderNameTextAppearance;
    private @ColorInt int incomingVideoBubbleSenderNameTextColor;

    // Outgoing Video Bubble Customization
    private @ColorInt int outgoingVideoBubbleBackgroundColor;
    private @Dimension int outgoingVideoBubbleCornerRadius;
    private @Dimension int outgoingVideoBubbleStrokeWidth;
    private @ColorInt int outgoingVideoBubbleStrokeColor;
    private Drawable outgoingVideoBubbleBackgroundDrawable;
    private @StyleRes int outgoingVideoBubbleDateStyle;
    private @StyleRes int outgoingVideoBubbleReceiptStyle;
    private @StyleRes int outgoingVideoBubbleAvatarStyle;
    private @StyleRes int outgoingVideoBubbleThreadIndicatorTextAppearance;
    private @ColorInt int outgoingVideoBubbleThreadIndicatorTextColor;
    private @ColorInt int outgoingVideoBubbleThreadIndicatorIconTint;
    private @StyleRes int outgoingVideoBubbleSenderNameTextAppearance;
    private @ColorInt int outgoingVideoBubbleSenderNameTextColor;

    // Incoming Form Bubble Customization
    private @ColorInt int incomingFormBubbleBackgroundColor;
    private @Dimension int incomingFormBubbleCornerRadius;
    private @Dimension int incomingFormBubbleStrokeWidth;
    private @ColorInt int incomingFormBubbleStrokeColor;
    private Drawable incomingFormBubbleBackgroundDrawable;
    private @StyleRes int incomingFormBubbleDateStyle;
    private @StyleRes int incomingFormBubbleReceiptStyle;
    private @StyleRes int incomingFormBubbleAvatarStyle;
    private @StyleRes int incomingFormBubbleThreadIndicatorTextAppearance;
    private @ColorInt int incomingFormBubbleThreadIndicatorTextColor;
    private @ColorInt int incomingFormBubbleThreadIndicatorIconTint;
    private @StyleRes int incomingFormBubbleSenderNameTextAppearance;
    private @ColorInt int incomingFormBubbleSenderNameTextColor;

    // Outgoing Form Bubble Customization
    private @ColorInt int outgoingFormBubbleBackgroundColor;
    private @Dimension int outgoingFormBubbleCornerRadius;
    private @Dimension int outgoingFormBubbleStrokeWidth;
    private @ColorInt int outgoingFormBubbleStrokeColor;
    private Drawable outgoingFormBubbleBackgroundDrawable;
    private @StyleRes int outgoingFormBubbleDateStyle;
    private @StyleRes int outgoingFormBubbleReceiptStyle;
    private @StyleRes int outgoingFormBubbleAvatarStyle;
    private @StyleRes int outgoingFormBubbleThreadIndicatorTextAppearance;
    private @ColorInt int outgoingFormBubbleThreadIndicatorTextColor;
    private @ColorInt int outgoingFormBubbleThreadIndicatorIconTint;
    private @StyleRes int outgoingFormBubbleSenderNameTextAppearance;
    private @ColorInt int outgoingFormBubbleSenderNameTextColor;

    // Incoming Scheduler Bubble Customization
    private @ColorInt int incomingSchedulerBubbleBackgroundColor;
    private @Dimension int incomingSchedulerBubbleCornerRadius;
    private @Dimension int incomingSchedulerBubbleStrokeWidth;
    private @ColorInt int incomingSchedulerBubbleStrokeColor;
    private Drawable incomingSchedulerBubbleBackgroundDrawable;
    private @StyleRes int incomingSchedulerBubbleDateStyle;
    private @StyleRes int incomingSchedulerBubbleReceiptStyle;
    private @StyleRes int incomingSchedulerBubbleAvatarStyle;
    private @StyleRes int incomingSchedulerBubbleThreadIndicatorTextAppearance;
    private @ColorInt int incomingSchedulerBubbleThreadIndicatorTextColor;
    private @ColorInt int incomingSchedulerBubbleThreadIndicatorIconTint;
    private @StyleRes int incomingSchedulerBubbleSenderNameTextAppearance;
    private @ColorInt int incomingSchedulerBubbleSenderNameTextColor;

    // Outgoing Scheduler Bubble Customization
    private @ColorInt int outgoingSchedulerBubbleBackgroundColor;
    private @Dimension int outgoingSchedulerBubbleCornerRadius;
    private @Dimension int outgoingSchedulerBubbleStrokeWidth;
    private @ColorInt int outgoingSchedulerBubbleStrokeColor;
    private Drawable outgoingSchedulerBubbleBackgroundDrawable;
    private @StyleRes int outgoingSchedulerBubbleDateStyle;
    private @StyleRes int outgoingSchedulerBubbleReceiptStyle;
    private @StyleRes int outgoingSchedulerBubbleAvatarStyle;
    private @StyleRes int outgoingSchedulerBubbleThreadIndicatorTextAppearance;
    private @ColorInt int outgoingSchedulerBubbleThreadIndicatorTextColor;
    private @ColorInt int outgoingSchedulerBubbleThreadIndicatorIconTint;
    private @StyleRes int outgoingSchedulerBubbleSenderNameTextAppearance;
    private @ColorInt int outgoingSchedulerBubbleSenderNameTextColor;

    // Incoming Poll Bubble Customization
    private @ColorInt int incomingPollBubbleBackgroundColor;
    private @Dimension int incomingPollBubbleCornerRadius;
    private @Dimension int incomingPollBubbleStrokeWidth;
    private @ColorInt int incomingPollBubbleStrokeColor;
    private Drawable incomingPollBubbleBackgroundDrawable;
    private @StyleRes int incomingPollBubbleDateStyle;
    private @StyleRes int incomingPollBubbleReceiptStyle;
    private @StyleRes int incomingPollBubbleAvatarStyle;
    private @StyleRes int incomingPollBubbleThreadIndicatorTextAppearance;
    private @ColorInt int incomingPollBubbleThreadIndicatorTextColor;
    private @ColorInt int incomingPollBubbleThreadIndicatorIconTint;
    private @ColorInt int incomingPollBubbleSenderNameTextColor;

    // Outgoing Poll Bubble Customization
    private @ColorInt int outgoingPollBubbleBackgroundColor;
    private @Dimension int outgoingPollBubbleCornerRadius;
    private @Dimension int outgoingPollBubbleStrokeWidth;
    private @ColorInt int outgoingPollBubbleStrokeColor;
    private Drawable outgoingPollBubbleBackgroundDrawable;
    private @StyleRes int outgoingPollBubbleDateStyle;
    private @StyleRes int outgoingPollBubbleReceiptStyle;
    private @StyleRes int outgoingPollBubbleAvatarStyle;
    private @StyleRes int outgoingPollBubbleThreadIndicatorTextAppearance;
    private @ColorInt int outgoingPollBubbleThreadIndicatorTextColor;
    private @ColorInt int outgoingPollBubbleThreadIndicatorIconTint;
    private @ColorInt int outgoingPollBubbleSenderNameTextColor;

    // Incoming Whiteboard Bubble Customization
    private @ColorInt int incomingStickerBubbleBackgroundColor;
    private @Dimension int incomingStickerBubbleCornerRadius;
    private @Dimension int incomingStickerBubbleStrokeWidth;
    private @ColorInt int incomingStickerBubbleStrokeColor;
    private Drawable incomingStickerBubbleBackgroundDrawable;
    private @StyleRes int incomingStickerBubbleDateStyle;
    private @StyleRes int incomingStickerBubbleReceiptStyle;
    private @StyleRes int incomingStickerBubbleAvatarStyle;
    private @StyleRes int incomingStickerBubbleThreadIndicatorTextAppearance;
    private @ColorInt int incomingStickerBubbleThreadIndicatorTextColor;
    private @ColorInt int incomingStickerBubbleThreadIndicatorIconTint;
    private @ColorInt int incomingStickerBubbleSenderNameTextColor;

    // Outgoing Whiteboard Bubble Customization
    private @ColorInt int outgoingStickerBubbleBackgroundColor;
    private @Dimension int outgoingStickerBubbleCornerRadius;
    private @Dimension int outgoingStickerBubbleStrokeWidth;
    private @ColorInt int outgoingStickerBubbleStrokeColor;
    private Drawable outgoingStickerBubbleBackgroundDrawable;
    private @StyleRes int outgoingStickerBubbleDateStyle;
    private @StyleRes int outgoingStickerBubbleReceiptStyle;
    private @StyleRes int outgoingStickerBubbleAvatarStyle;
    private @StyleRes int outgoingStickerBubbleThreadIndicatorTextAppearance;
    private @ColorInt int outgoingStickerBubbleThreadIndicatorTextColor;
    private @ColorInt int outgoingStickerBubbleThreadIndicatorIconTint;
    private @ColorInt int outgoingStickerBubbleSenderNameTextColor;

    // Incoming Whiteboard Bubble Customization
    private @ColorInt int incomingCollaborativeBubbleBackgroundColor;
    private @Dimension int incomingCollaborativeBubbleCornerRadius;
    private @Dimension int incomingCollaborativeBubbleStrokeWidth;
    private @ColorInt int incomingCollaborativeBubbleStrokeColor;
    private Drawable incomingCollaborativeBubbleBackgroundDrawable;
    private @StyleRes int incomingCollaborativeBubbleDateStyle;
    private @StyleRes int incomingCollaborativeBubbleReceiptStyle;
    private @StyleRes int incomingCollaborativeBubbleAvatarStyle;
    private @StyleRes int incomingCollaborativeBubbleThreadIndicatorTextAppearance;
    private @ColorInt int incomingCollaborativeBubbleThreadIndicatorTextColor;
    private @ColorInt int incomingCollaborativeBubbleThreadIndicatorIconTint;
    private @ColorInt int incomingCollaborativeBubbleSenderNameTextColor;

    // Outgoing Whiteboard Bubble Customization
    private @ColorInt int outgoingCollaborativeBubbleBackgroundColor;
    private @Dimension int outgoingCollaborativeBubbleCornerRadius;
    private @Dimension int outgoingCollaborativeBubbleStrokeWidth;
    private @ColorInt int outgoingCollaborativeBubbleStrokeColor;
    private Drawable outgoingCollaborativeBubbleBackgroundDrawable;
    private @StyleRes int outgoingCollaborativeBubbleDateStyle;
    private @StyleRes int outgoingCollaborativeBubbleReceiptStyle;
    private @StyleRes int outgoingCollaborativeBubbleAvatarStyle;
    private @StyleRes int outgoingCollaborativeBubbleThreadIndicatorTextAppearance;
    private @ColorInt int outgoingCollaborativeBubbleThreadIndicatorTextColor;
    private @ColorInt int outgoingCollaborativeBubbleThreadIndicatorIconTint;
    private @ColorInt int outgoingCollaborativeBubbleSenderNameTextColor;

    // Incoming Meet Call Bubble Customization
    private @ColorInt int incomingMeetCallBubbleBackgroundColor;
    private @Dimension int incomingMeetCallBubbleCornerRadius;
    private @Dimension int incomingMeetCallBubbleStrokeWidth;
    private @ColorInt int incomingMeetCallBubbleStrokeColor;
    private Drawable incomingMeetCallBubbleBackgroundDrawable;
    private @StyleRes int incomingMeetCallBubbleAvatarStyle;
    private @StyleRes int incomingMeetCallBubbleThreadIndicatorTextAppearance;
    private @ColorInt int incomingMeetCallBubbleThreadIndicatorTextColor;
    private @ColorInt int incomingMeetCallBubbleThreadIndicatorIconTint;
    private @ColorInt int incomingMeetCallBubbleSenderNameTextColor;

    // Outgoing Meet Call Bubble Customization
    private @ColorInt int outgoingMeetCallBubbleBackgroundColor;
    private @Dimension int outgoingMeetCallBubbleCornerRadius;
    private @Dimension int outgoingMeetCallBubbleStrokeWidth;
    private @ColorInt int outgoingMeetCallBubbleStrokeColor;
    private Drawable outgoingMeetCallBubbleBackgroundDrawable;
    private @StyleRes int outgoingMeetCallBubbleAvatarStyle;
    private @StyleRes int outgoingMeetCallBubbleThreadIndicatorTextAppearance;
    private @ColorInt int outgoingMeetCallBubbleThreadIndicatorTextColor;
    private @ColorInt int outgoingMeetCallBubbleThreadIndicatorIconTint;
    private @ColorInt int outgoingMeetCallBubbleSenderNameTextColor;
    private @StyleRes int outgoingMeetCallBubbleSenderNameTextAppearance;
    private @StyleRes int incomingMeetCallBubbleSenderNameTextAppearance;
    private boolean hideGroupActionMessage;

    /**
     * Constructor for the MessageAdapter class.
     *
     * @param context                the context in which the adapter is being used.
     * @param messageTemplateHashMap a HashMap containing message templates associated with their IDs.
     * @param onMessageLongClick     a callback interface for handling long-click events on messages.
     */
    public MessageAdapter(Context context, HashMap<String, CometChatMessageTemplate> messageTemplateHashMap, OnMessageLongClick onMessageLongClick) {
        this.context = context;
        baseMessageList = new ArrayList<>();
        layoutDirection = context.getResources().getConfiguration().getLayoutDirection();
        this.messageTemplateHashMap = messageTemplateHashMap;
        this.onMessageLongClick = onMessageLongClick;
        this.viewTypeTemplateHashMap = new HashMap<>();
        this.messageViewTypeHashMap = new HashMap<>();
        this.timeFormat = new SimpleDateFormat("h:mm a", CometChatLocalize.getDefault());
        this.dateTimeFormatter = CometChatUIKit.getAuthSettings().getDateTimeFormatterCallback();
        // Get the size of the reaction chips
        reactionChipSize = getTheSizeOfReactionChip("😂", 1);
        plusReactionChipSize = getTheSizeOfReactionChip("+", 9);
    }

    /**
     * Calculates and returns the width of a reaction chip for a given emoji and
     * count. This method creates a new `CometChatReaction` view for the provided
     * emoji and count, wraps it inside a temporary `LinearLayout` to measure its
     * width, and then returns the calculated width.
     *
     * @param emoji The emoji representing the reaction.
     * @param count The count of reactions for the given emoji.
     * @return The width of the reaction chip based on the given emoji and count.
     */
    private int getTheSizeOfReactionChip(String emoji, int count) {
        CometChatReaction cometchatReactionForEmoji = new CometChatReaction(context);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(cometchatReactionForEmoji.buildReactionView(emoji, count, false, null, null));
        return Utils.getViewWidth(linearLayout);
    }

    /**
     * Creates a new ViewHolder for the specified view type.
     *
     * @param parent   The parent ViewGroup into which the new ViewHolder will be added.
     * @param viewType The view type of the new ViewHolder.
     * @return A new ViewHolder for the specified view type.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Convert viewType to String for easier matching
        String viewTypeString = String.valueOf(viewType);

        // Check the type of message based on the view type
        if (viewTypeString.endsWith(RIGHT_MESSAGE)) {
            // Create a ViewHolder for right-aligned messages
            return new RightViewHolder(getRightView(parent), viewTypeTemplateHashMap.get(viewType));
        } else if (viewTypeString.endsWith(CENTER_MESSAGE)) {
            // Create a ViewHolder for center-aligned messages
            return new CenterViewHolder(getCenterView(parent), viewTypeTemplateHashMap.get(viewType));
        } else if (viewTypeString.endsWith(IGNORE_MESSAGE)) {
            // Create a ViewHolder for an empty row
            return new EmptyRowHolder(LinearLayout.inflate(context, R.layout.cometchat_empty_view_holder_row, null));
        } else {
            // Default case: Create a ViewHolder for left-aligned messages
            return new LeftViewHolder(getLeftView(parent), viewTypeTemplateHashMap.get(viewType));
        }
    }

    /**
     * Inflates the layout for right-aligned messages.
     *
     * @param parent The parent ViewGroup for the new view.
     * @return A View for right-aligned messages.
     */
    public View getRightView(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cometchat_right_message_row, parent, false);
        view.setTag(RIGHT_MESSAGE); // Set tag to identify the view type
        return view;
    }

    /**
     * Inflates the layout for center-aligned messages.
     *
     * @param parent The parent ViewGroup for the new view.
     * @return A View for center-aligned messages.
     */
    public View getCenterView(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cometchat_center_message_row, parent, false);
        view.setTag(CENTER_MESSAGE); // Set tag to identify the view type
        return view;
    }

    /**
     * Inflates the layout for left-aligned messages.
     *
     * @param parent The parent ViewGroup for the new view.
     * @return A View for left-aligned messages.
     */
    public View getLeftView(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cometchat_left_message_row, parent, false);
        view.setTag(LEFT_MESSAGE); // Set tag to identify the view type
        return view;
    }

    /**
     * Binds the data to the ViewHolder for the specified position in the
     * RecyclerView.
     *
     * @param holder   The ViewHolder to which data should be bound.
     * @param position The position of the item within the RecyclerView's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Retrieve the BaseMessage object for the current position
        BaseMessage baseMessage = baseMessageList.get(position);

        // Check the type of ViewHolder and bind the message data accordingly
        if (holder instanceof LeftViewHolder) {
            ((LeftViewHolder) holder).bindBubble(baseMessage, position); // Bind data for left-aligned messages
        } else if (holder instanceof RightViewHolder) {
            ((RightViewHolder) holder).bindBubble(baseMessage, position); // Bind data for right-aligned messages
        } else if (holder instanceof CenterViewHolder) {
            ((CenterViewHolder) holder).bindBubble(baseMessage, position); // Bind data for center-aligned messages
        } else {
            // Log an error if the ViewHolder type is unknown
            Log.e("", "onBindViewHolder: Unknown ViewHolder");
        }
    }

    /**
     * Returns the view type for the item at the specified position in the
     * RecyclerView.
     *
     * @param position The position of the item within the RecyclerView's data set.
     * @return The view type for the item at the specified position.
     */
    @Override
    public int getItemViewType(int position) {
        return getItemViewTypes(position);
    }

    /**
     * Determines the view type for the item at the specified position based on the
     * message category and sender.
     *
     * @param position The position of the item in the RecyclerView.
     * @return An integer representing the view type for the specified item.
     */
    public int getItemViewTypes(int position) {
        BaseMessage baseMessage = baseMessageList.get(position);
        String type;

        // Retrieve the template based on category and type
        CometChatMessageTemplate template = messageTemplateHashMap.get(baseMessage.getCategory() + "_" + baseMessage.getType());

        // Safely retrieve the initial view type, defaulting to 0 if null
        Integer viewTypeFromMap = messageViewTypeHashMap.get(baseMessage.getCategory() + "_" + baseMessage.getType());
        int initialViewType = (viewTypeFromMap != null) ? viewTypeFromMap : 0;

        // Determine message alignment/type
        if (baseMessage.getCategory().equals(CometChatConstants.CATEGORY_ACTION) ||
            baseMessage.getCategory().equals(CometChatConstants.CATEGORY_CALL)) {
            if (baseMessage.getCategory().equals(CometChatConstants.CATEGORY_ACTION)) {
                if (hideGroupActionMessage) {
                    type = IGNORE_MESSAGE;
                } else {
                    type = CENTER_MESSAGE;
                }
            } else {
                type = CENTER_MESSAGE;
            }
        } else {
            if (!UIKitConstants.MessageListAlignment.LEFT_ALIGNED.equals(listAlignment)) {
                if (baseMessage.getSender() != null && CometChatUIKit.getLoggedInUser() != null && baseMessage
                    .getSender()
                    .getUid()
                    .equals(CometChatUIKit.getLoggedInUser().getUid())) {
                    type = RIGHT_MESSAGE;
                } else {
                    type = LEFT_MESSAGE;
                }
            } else {
                type = LEFT_MESSAGE;
            }
        }

        // Safely construct the new type string
        String newType = initialViewType + type;
        int viewType;

        try {
            viewType = Integer.parseInt(newType);
        } catch (NumberFormatException e) {
            CometChatLogger.e(TAG, e.toString());
            viewType = 0; // Default fallback in case of error
        }

        // Save the template to the map, using a default if none exists
        viewTypeTemplateHashMap.put(viewType, template != null ? template : new CometChatMessageTemplate());

        return viewType;
    }

    @Override
    public int getItemCount() {
        return baseMessageList.size();
    }

    @Override
    public long getHeaderId(int var1) {
        if (baseMessageList.size() > var1) {
            BaseMessage baseMessage = baseMessageList.get(var1);
            return Long.parseLong(Utils.getDateId(baseMessage.getSentAt() * 1000));
        } else return 0;
    }

    /**
     * Creates a header ViewHolder for date items.
     *
     * @param var1 The ViewGroup for the header item.
     * @return A new DateItemHolder for the date header.
     */
    @Override
    public MessageAdapter.DateItemHolder onCreateHeaderViewHolder(ViewGroup var1) {
        return new DateItemHolder(LayoutInflater.from(var1.getContext()).inflate(R.layout.cometchat_message_date_header, var1, false));
    }

    /**
     * Binds the data for a header item in the RecyclerView, displaying the message
     * date.
     *
     * @param var1 The ViewHolder for the date item.
     * @param var2 The position of the message in the list.
     * @param var3 The ID for the date item (not used in this implementation).
     */
    @Override
    public void onBindHeaderViewHolder(MessageAdapter.DateItemHolder var1, int var2, long var3) {
        // Check if the provided index is valid for the baseMessageList
        if (baseMessageList.size() > var2) {
            BaseMessage baseMessage = baseMessageList.get(var2); // Retrieve the message for the header

            // Set padding for the date text view
            var1.txtMessageDate.setPadding(context.getResources().getDimensionPixelSize(R.dimen.cometchat_padding_2),
                                           context.getResources().getDimensionPixelSize(R.dimen.cometchat_padding_1),
                                           context.getResources().getDimensionPixelSize(R.dimen.cometchat_padding_2),
                                           context.getResources().getDimensionPixelSize(R.dimen.cometchat_padding_1));

            // Check if a custom date separator pattern is available
            // If the sent timestamp is valid, set the date; otherwise, show an updating
            // message
            if (baseMessage.getSentAt() > 0) {
                var1.txtMessageDate.setDateFormat(dateSeparatorFormat);
                var1.txtMessageDate.setDateTimeFormatterCallback(dateTimeFormatter);
                var1.txtMessageDate.setDate(baseMessage.getSentAt(), Pattern.DAY_DATE);
            } else {
                var1.txtMessageDate.setDateText(context.getString(R.string.cometchat_updating));
            }
            // Apply the style to the date text view
            var1.txtMessageDate.setStyle(dateSeparatorStyle);
        }
    }

    /**
     * Retrieves the date separator pattern formatted for a given message. This
     * method returns a formatted string representing the date separator for the
     * provided {@link BaseMessage}. The separator is formatted according to the
     * pattern specified by the dateSeparatorPattern function. If no pattern is
     * defined, null is returned.
     *
     * @param baseMessage The {@link BaseMessage} for which the date separator pattern is
     *                    requested.
     * @return A formatted string representing the date separator for the message,
     * or null if no pattern is defined.
     */
    public String getDateSeparatorPattern(BaseMessage baseMessage) {
        if (dateSeparatorFormat != null) return dateSeparatorFormat.format(baseMessage.getSentAt() * 1000);
        return null;
    }

    public void setMessageTemplateHashMap(HashMap<String, CometChatMessageTemplate> messageTemplateHashMap,
                                          HashMap<String, Integer> viewTypeHashMap) {
        this.messageTemplateHashMap = messageTemplateHashMap;
        this.messageViewTypeHashMap = viewTypeHashMap;
        notifyDataSetChanged();
    }

    /**
     * Sets the margins for both left and right message bubbles.
     *
     * <p>
     * This method applies the same margin values to both the left and right message
     * bubbles. The top, bottom, left, and right margin values are specified in
     * density-independent pixels (dp).
     *
     * @param top    The top margin in dp. This will be applied to both left and right
     *               bubbles.
     * @param bottom The bottom margin in dp. This will be applied to both left and
     *               right bubbles.
     * @param left   The left margin in dp. This will be applied to both left and right
     *               bubbles.
     * @param right  The right margin in dp. This will be applied to both left and
     *               right bubbles.
     */
    public void setBubbleMargin(int top, int bottom, int left, int right) {
        setLeftBubbleMargin(top, bottom, left, right);
        setRightBubbleMargin(top, bottom, left, right);
    }

    /**
     * Sets the left margin for the message bubble.
     *
     * <p>
     * This method allows customization of the message bubble's left margin by
     * specifying the top, bottom, left, and right margin values in
     * density-independent pixels (dp). Only positive margin values will be applied.
     *
     * @param top    The top margin in dp. If it is negative, it will not be updated.
     * @param bottom The bottom margin in dp. If it is negative, it will not be
     *               updated.
     * @param left   The left margin in dp. If it is negative, it will not be updated.
     * @param right  The right margin in dp. If it is negative, it will not be updated.
     */
    public void setLeftBubbleMargin(int top, int bottom, int left, int right) {
        if (top >= 0) this.leftBubbleMarginTop = Utils.convertDpToPx(context, top);
        if (bottom >= 0) this.leftBubbleMarginBottom = Utils.convertDpToPx(context, bottom);
        if (left >= 0) this.leftBubbleMarginStart = Utils.convertDpToPx(context, left);
        if (right >= 0) this.leftBubbleMarginEnd = Utils.convertDpToPx(context, right);
        // Notify the adapter that the data has changed to refresh the view
        notifyDataSetChanged();
    }

    /**
     * Sets the right margin for the message bubble.
     *
     * <p>
     * This method allows customization of the message bubble's right margin by
     * specifying the top, bottom, left, and right margin values in
     * density-independent pixels (dp). Only positive margin values will be applied.
     *
     * @param top    The top margin in dp. If it is negative, it will not be updated.
     * @param bottom The bottom margin in dp. If it is negative, it will not be
     *               updated.
     * @param left   The left margin in dp. If it is negative, it will not be updated.
     * @param right  The right margin in dp. If it is negative, it will not be updated.
     */
    public void setRightBubbleMargin(int top, int bottom, int left, int right) {
        if (top >= 0) this.rightBubbleMarginTop = Utils.convertDpToPx(context, top);
        if (bottom >= 0) this.rightBubbleMarginBottom = Utils.convertDpToPx(context, bottom);
        if (left >= 0) this.rightBubbleMarginStart = Utils.convertDpToPx(context, left);
        if (right >= 0) this.rightBubbleMarginEnd = Utils.convertDpToPx(context, right);
        // Notify the adapter that the data has changed to refresh the view
        notifyDataSetChanged();
    }

    /**
     * Sets whether reactions are disabled.
     *
     * @param reactions true to disable reactions, false to enable them.
     */
    public void disableReactions(boolean reactions) {
        this.disableReactions = reactions;
    }

    /**
     * Determines whether the sender's name should be hidden for a given message in
     * the chat bubble. This method evaluates several conditions to decide whether
     * to hide the sender's name. It checks the alignment of the message bubble, the
     * message list alignment, and whether the message is part of a group
     * conversation.
     *
     * @param baseMessage The message for which the decision is being made.
     * @param alignment   The alignment of the message bubble, which can be either left or
     *                    right.
     * @return {@code true} if the sender's name should be hidden, {@code false}
     * otherwise.
     */
    private boolean isHideName(BaseMessage baseMessage, UIKitConstants.MessageBubbleAlignment alignment) {
        boolean hideName;

        if (listAlignment.equals(UIKitConstants.MessageListAlignment.STANDARD) && (alignment.equals(UIKitConstants.MessageBubbleAlignment.LEFT) || alignment.equals(
            UIKitConstants.MessageBubbleAlignment.RIGHT)) && baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
            hideName = false;
        } else hideName = !listAlignment.equals(UIKitConstants.MessageListAlignment.LEFT_ALIGNED);
        return hideName;
    }

    /**
     * Applies the appropriate style to the message bubble based on the provided
     * parameters.
     *
     * @param message                The message data to be styled in the bubble.
     * @param isIncomingMessage      Indicates whether the message is incoming or outgoing.
     * @param messageBubbleAlignment The alignment of the message bubble (e.g., left, right, center).
     * @param cometchatMessageBubble The CometChat message bubble to which styles will be applied.
     * @param showReadReceipt        Indicates whether the read receipt should be displayed.
     * @param hideName               Indicates whether the sender's name should be hidden.
     * @param headerView             The view to display at the top of the message bubble, if
     *                               applicable.
     * @param statusInfoView         The view to display status information (e.g., delivered, read).
     * @param threadView             The view for displaying thread information, if applicable.
     * @param leadingView            The view for displaying leading information (e.g., sender profile
     *                               picture), if applicable.
     */
    private void applyBubbleStyle(BaseMessage message,
                                  boolean isIncomingMessage,
                                  UIKitConstants.MessageBubbleAlignment messageBubbleAlignment,
                                  CometChatMessageBubble cometchatMessageBubble,
                                  boolean showReadReceipt,
                                  boolean hideName,
                                  View headerView,
                                  View statusInfoView,
                                  View threadView,
                                  View leadingView) {
        String bubbleId = message.getCategory() + "_" + message.getType();
        if (message.getDeletedAt() == 0) {
            switch (bubbleId) {
                case UIKitConstants.MessageTemplateId.TEXT:
                    if (isIncomingMessage) {
                        setMessageBubbleStyle(incomingTextBubbleBackgroundColor,
                                              incomingTextBubbleCornerRadius,
                                              incomingTextBubbleStrokeWidth,
                                              incomingTextBubbleStrokeColor,
                                              incomingTextBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       incomingTextBubbleSenderNameTextColor,
                                       incomingTextBubbleSenderNameTextAppearance,
                                       incomingTextBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           incomingTextBubbleReceiptStyle,
                                           incomingTextBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, incomingTextBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       incomingTextBubbleThreadIndicatorTextColor,
                                       incomingTextBubbleThreadIndicatorTextAppearance,
                                       incomingTextBubbleThreadIndicatorIconTint,
                                       0);
                    } else {
                        setMessageBubbleStyle(outgoingTextBubbleBackgroundColor,
                                              outgoingTextBubbleCornerRadius,
                                              outgoingTextBubbleStrokeWidth,
                                              outgoingTextBubbleStrokeColor,
                                              outgoingTextBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       outgoingTextBubbleSenderNameTextColor,
                                       outgoingTextBubbleSenderNameTextAppearance,
                                       outgoingTextBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           outgoingTextBubbleReceiptStyle,
                                           outgoingTextBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, outgoingTextBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       outgoingTextBubbleThreadIndicatorTextColor,
                                       outgoingTextBubbleThreadIndicatorTextAppearance,
                                       outgoingTextBubbleThreadIndicatorIconTint,
                                       0);
                    }
                    break;
                case UIKitConstants.MessageTemplateId.IMAGE:
                    if (isIncomingMessage) {
                        setMessageBubbleStyle(incomingImageBubbleBackgroundColor,
                                              incomingImageBubbleCornerRadius,
                                              incomingImageBubbleStrokeWidth,
                                              incomingImageBubbleStrokeColor,
                                              incomingImageBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       incomingImageBubbleSenderNameTextColor,
                                       incomingImageBubbleSenderNameTextAppearance,
                                       incomingImageBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           incomingImageBubbleReceiptStyle,
                                           incomingImageBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, incomingImageBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       incomingImageBubbleThreadIndicatorTextColor,
                                       incomingImageBubbleThreadIndicatorTextAppearance,
                                       incomingImageBubbleThreadIndicatorIconTint,
                                       0);
                    } else {
                        setMessageBubbleStyle(outgoingImageBubbleBackgroundColor,
                                              outgoingImageBubbleCornerRadius,
                                              outgoingImageBubbleStrokeWidth,
                                              outgoingImageBubbleStrokeColor,
                                              outgoingImageBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       outgoingImageBubbleSenderNameTextColor,
                                       outgoingImageBubbleSenderNameTextAppearance,
                                       outgoingImageBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           outgoingImageBubbleReceiptStyle,
                                           outgoingImageBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, outgoingImageBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       outgoingImageBubbleThreadIndicatorTextColor,
                                       outgoingImageBubbleThreadIndicatorTextAppearance,
                                       outgoingImageBubbleThreadIndicatorIconTint,
                                       0);
                    }
                    break;
                case UIKitConstants.MessageTemplateId.VIDEO:
                    if (isIncomingMessage) {
                        setMessageBubbleStyle(incomingVideoBubbleBackgroundColor,
                                              incomingVideoBubbleCornerRadius,
                                              incomingVideoBubbleStrokeWidth,
                                              incomingVideoBubbleStrokeColor,
                                              incomingVideoBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       incomingVideoBubbleSenderNameTextColor,
                                       incomingVideoBubbleSenderNameTextAppearance,
                                       incomingVideoBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           incomingVideoBubbleReceiptStyle,
                                           incomingVideoBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, incomingVideoBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       incomingVideoBubbleThreadIndicatorTextColor,
                                       incomingVideoBubbleThreadIndicatorTextAppearance,
                                       incomingVideoBubbleThreadIndicatorIconTint,
                                       0);
                    } else {
                        setMessageBubbleStyle(outgoingVideoBubbleBackgroundColor,
                                              outgoingVideoBubbleCornerRadius,
                                              outgoingVideoBubbleStrokeWidth,
                                              outgoingVideoBubbleStrokeColor,
                                              outgoingVideoBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       outgoingVideoBubbleSenderNameTextColor,
                                       outgoingVideoBubbleSenderNameTextAppearance,
                                       outgoingVideoBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           outgoingVideoBubbleReceiptStyle,
                                           outgoingVideoBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, outgoingVideoBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       outgoingVideoBubbleThreadIndicatorTextColor,
                                       outgoingVideoBubbleThreadIndicatorTextAppearance,
                                       outgoingVideoBubbleThreadIndicatorIconTint,
                                       0);
                    }
                    break;
                case UIKitConstants.MessageTemplateId.AUDIO:
                    if (isIncomingMessage) {
                        setMessageBubbleStyle(incomingAudioBubbleBackgroundColor,
                                              incomingAudioBubbleCornerRadius,
                                              incomingAudioBubbleStrokeWidth,
                                              incomingAudioBubbleStrokeColor,
                                              incomingAudioBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       incomingAudioBubbleSenderNameTextColor,
                                       incomingAudioBubbleSenderNameTextAppearance,
                                       incomingAudioBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           incomingAudioBubbleReceiptStyle,
                                           incomingAudioBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, incomingAudioBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       incomingAudioBubbleThreadIndicatorTextColor,
                                       incomingAudioBubbleThreadIndicatorTextAppearance,
                                       incomingAudioBubbleThreadIndicatorIconTint,
                                       0);
                    } else {
                        setMessageBubbleStyle(outgoingAudioBubbleBackgroundColor,
                                              outgoingAudioBubbleCornerRadius,
                                              outgoingAudioBubbleStrokeWidth,
                                              outgoingAudioBubbleStrokeColor,
                                              outgoingAudioBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       outgoingAudioBubbleSenderNameTextColor,
                                       outgoingAudioBubbleSenderNameTextAppearance,
                                       outgoingAudioBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           outgoingAudioBubbleReceiptStyle,
                                           outgoingAudioBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, outgoingAudioBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       outgoingAudioBubbleThreadIndicatorTextColor,
                                       outgoingAudioBubbleThreadIndicatorTextAppearance,
                                       outgoingAudioBubbleThreadIndicatorIconTint,
                                       0);
                    }
                    break;
                case UIKitConstants.MessageTemplateId.FILE:
                    if (isIncomingMessage) {
                        setMessageBubbleStyle(incomingFileBubbleBackgroundColor,
                                              incomingFileBubbleCornerRadius,
                                              incomingFileBubbleStrokeWidth,
                                              incomingFileBubbleStrokeColor,
                                              incomingFileBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       incomingFileBubbleSenderNameTextColor,
                                       incomingFileBubbleSenderNameTextAppearance,
                                       incomingFileBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           incomingFileBubbleReceiptStyle,
                                           incomingFileBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, incomingFileBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       incomingFileBubbleThreadIndicatorTextColor,
                                       incomingFileBubbleThreadIndicatorTextAppearance,
                                       incomingFileBubbleThreadIndicatorIconTint,
                                       0);
                    } else {
                        setMessageBubbleStyle(outgoingFileBubbleBackgroundColor,
                                              outgoingFileBubbleCornerRadius,
                                              outgoingFileBubbleStrokeWidth,
                                              outgoingFileBubbleStrokeColor,
                                              outgoingFileBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       outgoingFileBubbleSenderNameTextColor,
                                       outgoingFileBubbleSenderNameTextAppearance,
                                       outgoingFileBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           outgoingFileBubbleReceiptStyle,
                                           outgoingFileBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, outgoingFileBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       outgoingFileBubbleThreadIndicatorTextColor,
                                       outgoingFileBubbleThreadIndicatorTextAppearance,
                                       outgoingFileBubbleThreadIndicatorIconTint,
                                       0);
                    }
                    break;
                case UIKitConstants.MessageTemplateId.FORM:
                    if (isIncomingMessage) {
                        setMessageBubbleStyle(incomingFormBubbleBackgroundColor,
                                              incomingFormBubbleCornerRadius,
                                              incomingFormBubbleStrokeWidth,
                                              incomingFormBubbleStrokeColor,
                                              incomingFormBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       incomingFormBubbleSenderNameTextColor,
                                       incomingFormBubbleSenderNameTextAppearance,
                                       incomingFormBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           incomingFormBubbleReceiptStyle,
                                           incomingFormBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, incomingFormBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       incomingFormBubbleThreadIndicatorTextColor,
                                       incomingFormBubbleThreadIndicatorTextAppearance,
                                       incomingFormBubbleThreadIndicatorIconTint,
                                       0);
                    } else {
                        setMessageBubbleStyle(outgoingFormBubbleBackgroundColor,
                                              outgoingFormBubbleCornerRadius,
                                              outgoingFormBubbleStrokeWidth,
                                              outgoingFormBubbleStrokeColor,
                                              outgoingFormBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       outgoingFormBubbleSenderNameTextColor,
                                       outgoingFormBubbleSenderNameTextAppearance,
                                       outgoingFormBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           outgoingFormBubbleReceiptStyle,
                                           outgoingFormBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, outgoingFormBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       outgoingFormBubbleThreadIndicatorTextColor,
                                       outgoingFormBubbleThreadIndicatorTextAppearance,
                                       outgoingFormBubbleThreadIndicatorIconTint,
                                       0);
                    }
                    break;
                case UIKitConstants.MessageTemplateId.SCHEDULER:
                    if (isIncomingMessage) {
                        setMessageBubbleStyle(incomingSchedulerBubbleBackgroundColor,
                                              incomingSchedulerBubbleCornerRadius,
                                              incomingSchedulerBubbleStrokeWidth,
                                              incomingSchedulerBubbleStrokeColor,
                                              incomingSchedulerBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       incomingSchedulerBubbleSenderNameTextColor,
                                       incomingSchedulerBubbleSenderNameTextAppearance,
                                       incomingSchedulerBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           incomingSchedulerBubbleReceiptStyle,
                                           incomingSchedulerBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, incomingSchedulerBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       incomingSchedulerBubbleThreadIndicatorTextColor,
                                       incomingSchedulerBubbleThreadIndicatorTextAppearance,
                                       incomingSchedulerBubbleThreadIndicatorIconTint,
                                       0);
                    } else {
                        setMessageBubbleStyle(outgoingSchedulerBubbleBackgroundColor,
                                              outgoingSchedulerBubbleCornerRadius,
                                              outgoingSchedulerBubbleStrokeWidth,
                                              outgoingSchedulerBubbleStrokeColor,
                                              outgoingSchedulerBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       outgoingSchedulerBubbleSenderNameTextColor,
                                       outgoingSchedulerBubbleSenderNameTextAppearance,
                                       outgoingSchedulerBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           outgoingSchedulerBubbleReceiptStyle,
                                           outgoingSchedulerBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, outgoingSchedulerBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       outgoingSchedulerBubbleThreadIndicatorTextColor,
                                       outgoingSchedulerBubbleThreadIndicatorTextAppearance,
                                       outgoingSchedulerBubbleThreadIndicatorIconTint,
                                       0);
                    }
                    break;
                case UIKitConstants.MessageCategory.CUSTOM + "_" + ExtensionConstants.ExtensionType.EXTENSION_POLL:
                    if (isIncomingMessage) {
                        setMessageBubbleStyle(incomingPollBubbleBackgroundColor,
                                              incomingPollBubbleCornerRadius,
                                              incomingPollBubbleStrokeWidth,
                                              incomingPollBubbleStrokeColor,
                                              incomingPollBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       incomingPollBubbleSenderNameTextColor,
                                       incomingSchedulerBubbleSenderNameTextAppearance,
                                       incomingPollBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           incomingPollBubbleReceiptStyle,
                                           incomingPollBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, incomingPollBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       incomingPollBubbleThreadIndicatorTextColor,
                                       incomingPollBubbleThreadIndicatorTextAppearance,
                                       incomingPollBubbleThreadIndicatorIconTint,
                                       0);
                    } else {
                        setMessageBubbleStyle(outgoingPollBubbleBackgroundColor,
                                              outgoingPollBubbleCornerRadius,
                                              outgoingPollBubbleStrokeWidth,
                                              outgoingPollBubbleStrokeColor,
                                              outgoingPollBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       outgoingPollBubbleSenderNameTextColor,
                                       outgoingSchedulerBubbleSenderNameTextAppearance,
                                       outgoingPollBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           outgoingPollBubbleReceiptStyle,
                                           outgoingPollBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, outgoingPollBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       outgoingPollBubbleThreadIndicatorTextColor,
                                       outgoingPollBubbleThreadIndicatorTextAppearance,
                                       outgoingPollBubbleThreadIndicatorIconTint,
                                       0);
                    }
                    break;
                case UIKitConstants.MessageCategory.CUSTOM + "_" + ExtensionConstants.ExtensionType.STICKER:
                    if (isIncomingMessage) {
                        setMessageBubbleStyle(incomingStickerBubbleBackgroundColor,
                                              incomingStickerBubbleCornerRadius,
                                              incomingStickerBubbleStrokeWidth,
                                              incomingStickerBubbleStrokeColor,
                                              incomingStickerBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       incomingStickerBubbleSenderNameTextColor,
                                       incomingSchedulerBubbleSenderNameTextAppearance,
                                       incomingStickerBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           incomingStickerBubbleReceiptStyle,
                                           incomingStickerBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, incomingStickerBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       incomingStickerBubbleThreadIndicatorTextColor,
                                       incomingStickerBubbleThreadIndicatorTextAppearance,
                                       incomingStickerBubbleThreadIndicatorIconTint,
                                       0);
                    } else {
                        setMessageBubbleStyle(outgoingStickerBubbleBackgroundColor,
                                              outgoingStickerBubbleCornerRadius,
                                              outgoingStickerBubbleStrokeWidth,
                                              outgoingStickerBubbleStrokeColor,
                                              outgoingStickerBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       outgoingStickerBubbleSenderNameTextColor,
                                       outgoingSchedulerBubbleSenderNameTextAppearance,
                                       outgoingStickerBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           outgoingStickerBubbleReceiptStyle,
                                           outgoingStickerBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, outgoingStickerBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       outgoingStickerBubbleThreadIndicatorTextColor,
                                       outgoingStickerBubbleThreadIndicatorTextAppearance,
                                       outgoingStickerBubbleThreadIndicatorIconTint,
                                       0);
                    }
                    break;
                case UIKitConstants.MessageCategory.CUSTOM + "_" + ExtensionConstants.ExtensionType.DOCUMENT:
                    if (isIncomingMessage) {
                        setMessageBubbleStyle(incomingCollaborativeBubbleBackgroundColor,
                                              incomingCollaborativeBubbleCornerRadius,
                                              incomingCollaborativeBubbleStrokeWidth,
                                              incomingCollaborativeBubbleStrokeColor,
                                              incomingCollaborativeBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       incomingCollaborativeBubbleSenderNameTextColor,
                                       incomingSchedulerBubbleSenderNameTextAppearance,
                                       incomingCollaborativeBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           incomingCollaborativeBubbleReceiptStyle,
                                           incomingCollaborativeBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, incomingCollaborativeBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       incomingCollaborativeBubbleThreadIndicatorTextColor,
                                       incomingCollaborativeBubbleThreadIndicatorTextAppearance,
                                       incomingCollaborativeBubbleThreadIndicatorIconTint,
                                       0);
                    } else {
                        setMessageBubbleStyle(outgoingCollaborativeBubbleBackgroundColor,
                                              outgoingCollaborativeBubbleCornerRadius,
                                              outgoingCollaborativeBubbleStrokeWidth,
                                              outgoingCollaborativeBubbleStrokeColor,
                                              outgoingCollaborativeBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       outgoingCollaborativeBubbleSenderNameTextColor,
                                       outgoingSchedulerBubbleSenderNameTextAppearance,
                                       outgoingCollaborativeBubbleDateStyle);
                        bindStatusInfoView(statusInfoView,
                                           showReadReceipt,
                                           MessageReceiptUtils.MessageReceipt(message),
                                           getTimePattern(message),
                                           timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                           outgoingCollaborativeBubbleReceiptStyle,
                                           outgoingCollaborativeBubbleDateStyle);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, outgoingCollaborativeBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       outgoingCollaborativeBubbleThreadIndicatorTextColor,
                                       outgoingCollaborativeBubbleThreadIndicatorTextAppearance,
                                       outgoingCollaborativeBubbleThreadIndicatorIconTint,
                                       0);
                    }
                    break;
                case UIKitConstants.MessageCategory.CUSTOM + "_" + UIKitConstants.MessageType.MEETING:
                    if (isIncomingMessage) {
                        setMessageBubbleStyle(incomingMeetCallBubbleBackgroundColor,
                                              incomingMeetCallBubbleCornerRadius,
                                              incomingMeetCallBubbleStrokeWidth,
                                              incomingMeetCallBubbleStrokeColor,
                                              incomingMeetCallBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       incomingMeetCallBubbleSenderNameTextColor,
                                       incomingMeetCallBubbleSenderNameTextAppearance,
                                       0);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, incomingMeetCallBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       incomingMeetCallBubbleThreadIndicatorTextColor,
                                       incomingMeetCallBubbleThreadIndicatorTextAppearance,
                                       incomingMeetCallBubbleThreadIndicatorIconTint,
                                       0);
                    } else {
                        setMessageBubbleStyle(outgoingMeetCallBubbleBackgroundColor,
                                              outgoingMeetCallBubbleCornerRadius,
                                              outgoingMeetCallBubbleStrokeWidth,
                                              outgoingMeetCallBubbleStrokeColor,
                                              outgoingMeetCallBubbleBackgroundDrawable,
                                              cometchatMessageBubble);
                        bindHeaderView(headerView,
                                       !hideName,
                                       message.getSender().getName(),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                       outgoingMeetCallBubbleSenderNameTextColor,
                                       outgoingMeetCallBubbleSenderNameTextAppearance,
                                       0);
                        bindLeadingView(leadingView, message, messageBubbleAlignment, outgoingMeetCallBubbleAvatarStyle);
                        bindThreadView(threadView,
                                       message,
                                       null,
                                       messageBubbleAlignment,
                                       outgoingMeetCallBubbleThreadIndicatorTextColor,
                                       outgoingMeetCallBubbleThreadIndicatorTextAppearance,
                                       outgoingMeetCallBubbleThreadIndicatorIconTint,
                                       0);
                    }
                default:
                    cometchatMessageBubble.setBackgroundDrawable(isIncomingMessage ? incomingMessageBubbleBackgroundDrawable : outgoingMessageBubbleBackgroundDrawable);
                    cometchatMessageBubble.setBackgroundColor(isIncomingMessage ? incomingMessageBubbleBackgroundColor : outgoingMessageBubbleBackgroundColor);
                    cometchatMessageBubble.setCornerRadius(isIncomingMessage ? incomingMessageBubbleCornerRadius : outgoingMessageBubbleCornerRadius);
                    cometchatMessageBubble.setStrokeWidth(isIncomingMessage ? incomingMessageBubbleStrokeWidth : outgoingMessageBubbleStrokeWidth);
                    cometchatMessageBubble.setStrokeColor(isIncomingMessage ? incomingMessageBubbleStrokeColor : outgoingMessageBubbleStrokeColor);
                    bindHeaderView(headerView,
                                   !hideName,
                                   message.getSender().getName(),
                                   getTimePattern(message),
                                   timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                                   isIncomingMessage ? incomingMessageBubbleSenderNameTextColor : outgoingMessageBubbleSenderNameTextColor,
                                   isIncomingMessage ? incomingMessageBubbleSenderNameTextAppearance : outgoingMessageBubbleSenderNameTextAppearance,
                                   isIncomingMessage ? incomingMessageBubbleDateStyle : outgoingMessageBubbleDateStyle);
                    bindStatusInfoView(statusInfoView,
                                       showReadReceipt,
                                       MessageReceiptUtils.MessageReceipt(message),
                                       getTimePattern(message),
                                       timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                       isIncomingMessage ? incomingMessageBubbleReceiptStyle : outgoingMessageBubbleReceiptStyle,
                                       isIncomingMessage ? incomingMessageBubbleDateStyle : outgoingMessageBubbleDateStyle);
                    bindLeadingView(leadingView,
                                    message,
                                    messageBubbleAlignment,
                                    isIncomingMessage ? incomingMessageBubbleAvatarStyle : outgoingMessageBubbleAvatarStyle);
                    bindThreadView(threadView,
                                   message,
                                   null,
                                   messageBubbleAlignment,
                                   isIncomingMessage ? incomingMessageBubbleThreadIndicatorTextColor : outgoingMessageBubbleThreadIndicatorTextColor,
                                   isIncomingMessage ? incomingMessageBubbleThreadIndicatorTextAppearance : outgoingMessageBubbleThreadIndicatorTextAppearance,
                                   isIncomingMessage ? incomingMessageBubbleThreadIndicatorIconTint : outgoingMessageBubbleThreadIndicatorIconTint,
                                   0);
                    break;
            }
        } else {
            if (isIncomingMessage) {
                setMessageBubbleStyle(incomingDeleteBubbleBackgroundColor,
                                      incomingDeleteBubbleCornerRadius,
                                      incomingDeleteBubbleStrokeWidth,
                                      incomingDeleteBubbleStrokeColor,
                                      incomingDeleteBubbleBackgroundDrawable,
                                      cometchatMessageBubble);
                bindHeaderView(headerView,
                               !hideName,
                               message.getSender().getName(),
                               getTimePattern(message),
                               timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                               incomingDeleteBubbleSenderNameTextColor,
                               incomingDeleteBubbleSenderNameTextAppearance,
                               incomingDeleteBubbleDateStyle);
                bindStatusInfoView(statusInfoView,
                                   showReadReceipt,
                                   MessageReceiptUtils.MessageReceipt(message),
                                   getTimePattern(message),
                                   timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                   0,
                                   incomingDeleteBubbleDateStyle);
                bindLeadingView(leadingView, message, messageBubbleAlignment, incomingDeleteBubbleAvatarStyle);
                bindThreadView(threadView, message, null, messageBubbleAlignment, 0, 0, 0, 0);
            } else {
                setMessageBubbleStyle(outgoingDeleteBubbleBackgroundColor,
                                      outgoingDeleteBubbleCornerRadius,
                                      outgoingDeleteBubbleStrokeWidth,
                                      outgoingDeleteBubbleStrokeColor,
                                      outgoingDeleteBubbleBackgroundDrawable,
                                      cometchatMessageBubble);
                bindHeaderView(headerView,
                               !hideName,
                               message.getSender().getName(),
                               getTimePattern(message),
                               timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.TOP),
                               outgoingDeleteBubbleSenderNameTextColor,
                               outgoingDeleteBubbleSenderNameTextAppearance,
                               outgoingDeleteBubbleDateStyle);
                bindStatusInfoView(statusInfoView,
                                   showReadReceipt,
                                   MessageReceiptUtils.MessageReceipt(message),
                                   getTimePattern(message),
                                   timeStampAlignment.equals(UIKitConstants.TimeStampAlignment.BOTTOM),
                                   0,
                                   outgoingDeleteBubbleDateStyle);
                bindLeadingView(leadingView, message, messageBubbleAlignment, outgoingDeleteBubbleAvatarStyle);
                bindThreadView(threadView, message, null, messageBubbleAlignment, 0, 0, 0, 0);
            }
        }
    }

    /**
     * Sets the style of a message bubble for the CometChatMessageBubble instance.
     *
     * <p>
     * This method allows customization of the message bubble's appearance by
     * setting its background, corner radius, stroke width, and stroke color. If a
     * background drawable is provided, it will take precedence over the background
     * color.
     *
     * @param backgroundColor        The background color of the message bubble, used only if {@code
     *                               backgroundDrawable} is {@code null}.
     * @param cornerRadius           The radius of the corners of the message bubble.
     * @param strokeWidth            The width of the border stroke of the message bubble.
     * @param strokeColor            The color of the border stroke of the message bubble.
     * @param backgroundDrawable     The drawable to be used as the background of the message bubble.
     *                               If {@code null}, {@code backgroundColor} will be used instead.
     * @param cometchatMessageBubble The CometChatMessageBubble instance to which the style will be
     *                               applied.
     */
    private void setMessageBubbleStyle(@ColorInt int backgroundColor,
                                       int cornerRadius,
                                       int strokeWidth,
                                       @ColorInt int strokeColor,
                                       Drawable backgroundDrawable,
                                       CometChatMessageBubble cometchatMessageBubble) {
        if (backgroundDrawable != null) {
            cometchatMessageBubble.setBackgroundDrawable(backgroundDrawable);
        } else {
            cometchatMessageBubble.setBackgroundColor(backgroundColor);
        }
        cometchatMessageBubble.setCornerRadius(cornerRadius);
        cometchatMessageBubble.setStrokeWidth(strokeWidth);
        cometchatMessageBubble.setStrokeColor(strokeColor);
    }

    /**
     * Binds the header view with the provided details.
     *
     * @param view           the view to bind the header to.
     * @param showName       indicates whether to show the name.
     * @param name           the name to display.
     * @param time           the time to display.
     * @param showTime       indicates whether to show the time.
     * @param nameColor      the color of the name text.
     * @param nameAppearance the style resource for the name appearance.
     * @param dateStyle      the style resource for the date appearance.
     */
    private void bindHeaderView(View view,
                                boolean showName,
                                String name,
                                String time,
                                boolean showTime,
                                @ColorInt int nameColor,
                                @StyleRes int nameAppearance,
                                @StyleRes int dateStyle) {
        if (view != null && view.getTag() != null && view.getTag().equals(UIKitConstants.ViewTag.INTERNAL_HEADER_VIEW)) {
            MessageBubbleUtils.bindHeaderView(view, showName, name, time, showTime, nameColor, nameAppearance, dateStyle);
        }
    }

    public String getTimePattern(BaseMessage baseMessage) {
        long time = baseMessage.getSentAt() * 1000;
        if (dateTimeFormatter != null) {
            String timeString = dateTimeFormatter.time(time);
            if (timeString != null) {
                return timeString;
            }
        }
        if (timeFormat != null) return timeFormat.format(time);
        return "";
    }

    /**
     * Binds the status info view with the provided details.
     *
     * @param view            the view to bind the status info to.
     * @param showReadReceipt indicates whether to show the read receipt.
     * @param receipt         the receipt data to display.
     * @param time            the time to display.
     * @param showTime        indicates whether to show the time.
     * @param receiptStyle    the style resource for the receipt appearance.
     * @param dateStyle       the style resource for the date appearance.
     */
    private void bindStatusInfoView(View view,
                                    boolean showReadReceipt,
                                    Receipt receipt,
                                    String time,
                                    boolean showTime,
                                    @StyleRes int receiptStyle,
                                    @StyleRes int dateStyle) {
        if (view != null && view.getTag() != null && view.getTag().equals(UIKitConstants.ViewTag.INTERNAL_STATUS_INFO_VIEW)) {
            MessageBubbleUtils.bindStatusInfoView(view, showReadReceipt, receipt, time, showTime, receiptStyle, dateStyle);
        }
    }

    /**
     * Binds the leading view with the provided message and alignment details.
     *
     * @param view        the view to bind the leading info to.
     * @param baseMessage the base message containing sender info.
     * @param alignment   the alignment of the message bubble.
     * @param avatarStyle the style resource for the avatar appearance.
     */
    private void bindLeadingView(View view, BaseMessage baseMessage, UIKitConstants.MessageBubbleAlignment alignment, @StyleRes int avatarStyle) {
        if (view != null && view.getTag() != null && view.getTag().equals(UIKitConstants.ViewTag.INTERNAL_LEADING_VIEW)) {
            if (alignment.equals(UIKitConstants.MessageBubbleAlignment.LEFT)) {
                if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                    if (listAlignment.equals(UIKitConstants.MessageListAlignment.LEFT_ALIGNED)) {
                        MessageBubbleUtils.bindAvatarLeadingView(view,
                                                                 true,
                                                                 baseMessage.getSender().getAvatar(),
                                                                 baseMessage.getSender().getName(),
                                                                 avatarStyle);
                    } else
                        MessageBubbleUtils.bindAvatarLeadingView(view,
                                                                 showLeftBubbleUserAvatar,
                                                                 baseMessage.getSender().getAvatar(),
                                                                 baseMessage.getSender().getName(),
                                                                 avatarStyle);
                } else if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP))
                    MessageBubbleUtils.bindAvatarLeadingView(view,
                                                             showLeftBubbleGroupAvatar,
                                                             baseMessage.getSender().getAvatar(),
                                                             baseMessage.getSender().getName(),
                                                             avatarStyle);
            } else {
                if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER))
                    MessageBubbleUtils.bindAvatarLeadingView(view,
                                                             showLeftBubbleUserAvatar && layoutDirection == View.LAYOUT_DIRECTION_RTL,
                                                             baseMessage.getSender().getAvatar(),
                                                             baseMessage.getSender().getName(),
                                                             avatarStyle);
                else if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP))
                    MessageBubbleUtils.bindAvatarLeadingView(view,
                                                             showLeftBubbleGroupAvatar && layoutDirection == View.LAYOUT_DIRECTION_RTL,
                                                             baseMessage.getSender().getAvatar(),
                                                             baseMessage.getSender().getName(),
                                                             avatarStyle);
            }
        }
    }

    /**
     * Binds the thread view with the provided message and reply details.
     *
     * @param view                      the view to bind the thread info to.
     * @param baseMessage               the base message containing thread info.
     * @param replyMessage              the reply message text.
     * @param alignment                 the alignment of the thread bubble.
     * @param threadReplyTextColor      the color of the thread reply text.
     * @param threadReplyTextAppearance the style resource for the thread reply text appearance.
     * @param threadReplyIconTint       the tint color for the thread reply icon.
     * @param threadReplyIcon           the drawable resource for the thread reply icon.
     */
    private void bindThreadView(View view,
                                BaseMessage baseMessage,
                                String replyMessage,
                                UIKitConstants.MessageBubbleAlignment alignment,
                                @ColorInt int threadReplyTextColor,
                                @StyleRes int threadReplyTextAppearance,
                                @ColorInt int threadReplyIconTint,
                                @DrawableRes int threadReplyIcon) {
        if (view != null && view.getTag() != null && view.getTag().equals(UIKitConstants.ViewTag.INTERNAL_THREAD_VIEW)) {
            MessageBubbleUtils.bindThreadView(view,
                                              baseMessage,
                                              replyMessage,
                                              alignment,
                                              threadReplyTextColor,
                                              threadReplyTextAppearance,
                                              threadReplyIconTint,
                                              threadReplyIcon);
        }
    }

    /**
     * Toggles the visibility of the read receipt. This method determines whether
     * the read receipt should be hidden or shown, and updates the view accordingly.
     *
     * @param disableReadReceipt {@code true} to hide the read receipt, {@code false} to show it.
     */
    public void hideReceipts(boolean disableReadReceipt) {
        this.disableReadReceipt = disableReadReceipt;
        notifyDataSetChanged();
    }

    public void setOnAddMoreReactionsClick(OnAddMoreReactionsClick onAddMoreReactionsClick) {
        this.onAddMoreReactionsClick = onAddMoreReactionsClick;
    }

    public void setOnReactionClick(OnReactionClick onReactionClick) {
        this.onReactionClick = onReactionClick;
    }

    public void setOnReactionLongClick(OnReactionLongClick onReactionLongClick) {
        this.onReactionLongClick = onReactionLongClick;
    }

    /**
     * Sets the style resource for the incoming message bubble and applies the
     * corresponding attributes.
     *
     * <p>
     * This method takes a style resource for the incoming message bubble and
     * extracts various properties from it, such as background color, corner radius,
     * stroke width, and other visual aspects. It then applies these settings to
     * customize the appearance of incoming message bubbles. Finally, it refreshes
     * the view.
     *
     * @param messageBubbleStyle The style resource ID for the incoming message bubble.
     */
    public void setIncomingMessageBubbleStyle(@StyleRes int messageBubbleStyle) {
        if (messageBubbleStyle != 0) {
            TypedArray typedArray = context.obtainStyledAttributes(messageBubbleStyle, R.styleable.CometChatMessageBubble);
            try {
                setIncomingMessageBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatMessageBubble_cometchatMessageBubbleBackgroundColor,
                                                                            0));
                setIncomingMessageBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatMessageBubble_cometchatMessageBubbleCornerRadius,
                                                                                      0));
                setIncomingMessageBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatMessageBubble_cometchatMessageBubbleStrokeWidth,
                                                                                     0));
                setIncomingMessageBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatMessageBubble_cometchatMessageBubbleStrokeColor, 0));
                setIncomingMessageBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatMessageBubble_cometchatMessageBubbleBackgroundDrawable));
                setIncomingMessageBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMessageBubbleDateStyle, 0));
                setIncomingMessageBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMessageBubbleMessageReceiptStyle,
                                                                              0));
                setIncomingMessageBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMessageBubbleAvatarStyle,
                                                                             0));
                setIncomingMessageBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMessageBubbleThreadIndicatorTextAppearance,
                                                                                               0));
                setIncomingMessageBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatMessageBubble_cometchatMessageBubbleThreadIndicatorTextColor,
                                                                                     0));
                setIncomingMessageBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatMessageBubble_cometchatMessageBubbleThreadIndicatorIconTint,
                                                                                    0));
                setIncomingMessageBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMessageBubbleSenderNameTextAppearance,
                                                                                          0));
                setIncomingMessageBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatMessageBubble_cometchatMessageBubbleSenderNameTextColor,
                                                                                0));
                setIncomingDeleteBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatDeleteBubbleStyle, 0));
                setIncomingTextBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatTextBubbleStyle, 0));
                setIncomingImageBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatImageBubbleStyle, 0));
                setIncomingAudioBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatAudioBubbleStyle, 0));
                setIncomingVideoBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatVideoBubbleStyle, 0));
                setIncomingFileBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatFileBubbleStyle, 0));
                setIncomingFormBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatFormBubbleStyle, 0));
                setIncomingSchedulerBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatSchedulerBubbleStyle, 0));
                setIncomingPollBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatPollBubbleStyle, 0));
                setIncomingStickerBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatStickerBubbleStyle, 0));
                setIncomingCollaborativeBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatCollaborativeBubbleStyle,
                                                                             0));
                setIncomingMessageBubbleReactionStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatReactionStyle, 0));
                setIncomingMeetCallBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMeetCallBubbleStyle, 0));
            } finally {
                typedArray.recycle();
            }
            notifyDataSetChanged();
        }
    }

    /**
     * Sets the background color for the incoming message bubble.
     *
     * @param incomingTextBubbleStyle The background color for the incoming message bubble.
     */
    private void setIncomingMessageBubbleBackgroundColor(@ColorInt int incomingTextBubbleStyle) {
        this.incomingMessageBubbleBackgroundColor = incomingTextBubbleStyle;
    }

    /**
     * Sets the corner radius for the incoming message bubble.
     *
     * @param incomingTextBubbleStyle The corner radius for the incoming message bubble.
     */
    private void setIncomingMessageBubbleCornerRadius(@Dimension int incomingTextBubbleStyle) {
        this.incomingMessageBubbleCornerRadius = incomingTextBubbleStyle;
    }

    /**
     * Sets the stroke width for the incoming message bubble.
     *
     * @param incomingTextBubbleStyle The stroke width for the incoming message bubble.
     */
    private void setIncomingMessageBubbleStrokeWidth(@Dimension int incomingTextBubbleStyle) {
        this.incomingMessageBubbleStrokeWidth = incomingTextBubbleStyle;
    }

    /**
     * Sets the stroke color for the incoming message bubble.
     *
     * @param incomingTextBubbleStyle The stroke color for the incoming message bubble.
     */
    private void setIncomingMessageBubbleStrokeColor(@ColorInt int incomingTextBubbleStyle) {
        this.incomingMessageBubbleStrokeColor = incomingTextBubbleStyle;
    }

    /**
     * Sets the background drawable for the incoming message bubble.
     *
     * @param incomingTextBubbleStyle The background drawable for the incoming message bubble.
     */
    private void setIncomingMessageBubbleBackgroundDrawable(Drawable incomingTextBubbleStyle) {
        this.incomingMessageBubbleBackgroundDrawable = incomingTextBubbleStyle;
    }

    /**
     * Sets the style resource for the incoming message bubble date.
     *
     * @param incomingMessageBubbleDateStyle The style resource to apply.
     */
    public void setIncomingMessageBubbleDateStyle(@StyleRes int incomingMessageBubbleDateStyle) {
        this.incomingMessageBubbleDateStyle = incomingMessageBubbleDateStyle;
    }

    /**
     * Sets the style resource for the incoming message bubble receipt.
     *
     * @param incomingMessageBubbleReceiptStyle The style resource to apply.
     */
    public void setIncomingMessageBubbleReceiptStyle(@StyleRes int incomingMessageBubbleReceiptStyle) {
        this.incomingMessageBubbleReceiptStyle = incomingMessageBubbleReceiptStyle;
    }

    /**
     * Sets the style resource for the incoming message bubble avatar.
     *
     * @param incomingMessageBubbleAvatarStyle The style resource to apply.
     */
    public void setIncomingMessageBubbleAvatarStyle(@StyleRes int incomingMessageBubbleAvatarStyle) {
        this.incomingMessageBubbleAvatarStyle = incomingMessageBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance resource for the incoming message bubble thread
     * indicator.
     *
     * @param incomingMessageBubbleThreadIndicatorTextAppearance The text appearance resource to apply.
     */
    public void setIncomingMessageBubbleThreadIndicatorTextAppearance(@StyleRes int incomingMessageBubbleThreadIndicatorTextAppearance) {
        this.incomingMessageBubbleThreadIndicatorTextAppearance = incomingMessageBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the incoming message bubble thread indicator.
     *
     * @param incomingMessageBubbleThreadIndicatorTextColor The color to apply.
     */
    public void setIncomingMessageBubbleThreadIndicatorTextColor(@ColorInt int incomingMessageBubbleThreadIndicatorTextColor) {
        this.incomingMessageBubbleThreadIndicatorTextColor = incomingMessageBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the tint color for the incoming message bubble thread indicator icon.
     *
     * @param incomingMessageBubbleThreadIndicatorIconTint The color to apply.
     */
    public void setIncomingMessageBubbleThreadIndicatorIconTint(@ColorInt int incomingMessageBubbleThreadIndicatorIconTint) {
        this.incomingMessageBubbleThreadIndicatorIconTint = incomingMessageBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance resource for the incoming message bubble sender
     * name.
     *
     * @param incomingMessageBubbleSenderNameTextAppearance The text appearance resource to apply.
     */
    public void setIncomingMessageBubbleSenderNameTextAppearance(@StyleRes int incomingMessageBubbleSenderNameTextAppearance) {
        this.incomingMessageBubbleSenderNameTextAppearance = incomingMessageBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the incoming message bubble sender name.
     *
     * @param incomingMessageBubbleSenderNameTextColor The color to apply.
     */
    public void setIncomingMessageBubbleSenderNameTextColor(@ColorInt int incomingMessageBubbleSenderNameTextColor) {
        this.incomingMessageBubbleSenderNameTextColor = incomingMessageBubbleSenderNameTextColor;
    }

    /**
     * Sets the style resource for the incoming delete message bubble and applies
     * the corresponding attributes. This method takes a style resource for the
     * incoming delete message bubble and extracts various properties, such as
     * background color, corner radius, stroke width, and other visual aspects. It
     * applies these settings to customize the appearance of incoming delete message
     * bubbles. The method uses a {@link TypedArray} to access the custom attributes
     * defined in the style resource and applies default values from the outgoing
     * message bubble style when necessary. After applying the properties, the
     * {@link TypedArray} is recycled to release resources.
     *
     * @param style The style resource ID for the outgoing delete message bubble.
     */
    public void setIncomingDeleteBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatDeleteBubble);
        try {
            setIncomingDeleteBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatDeleteBubble_cometchatDeleteBubbleBackgroundColor,
                                                                       incomingMessageBubbleBackgroundColor));
            setIncomingDeleteBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatDeleteBubble_cometchatDeleteBubbleCornerRadius,
                                                                                 incomingMessageBubbleCornerRadius));
            setIncomingDeleteBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatDeleteBubble_cometchatDeleteBubbleStrokeWidth,
                                                                                incomingMessageBubbleStrokeWidth));
            setIncomingDeleteBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatDeleteBubble_cometchatDeleteBubbleStrokeColor,
                                                                   incomingMessageBubbleStrokeColor));
            setIncomingDeleteBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatDeleteBubble_cometchatDeleteBubbleBackgroundDrawable));
            setIncomingDeleteBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatDeleteBubble_cometchatDeleteBubbleDateStyle,
                                                                      incomingMessageBubbleDateStyle));
            setIncomingDeleteBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatDeleteBubble_cometchatDeleteBubbleAvatarStyle,
                                                                        incomingMessageBubbleAvatarStyle));
            setIncomingDeleteBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatDeleteBubble_cometchatDeleteBubbleSenderNameTextAppearance,
                                                                                     incomingMessageBubbleSenderNameTextAppearance));
            setIncomingDeleteBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatDeleteBubble_cometchatDeleteBubbleSenderNameTextColor,
                                                                           incomingMessageBubbleSenderNameTextColor));
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Sets the style for incoming text message bubbles and applies the
     * corresponding attributes. This method applies a given style resource to the
     * incoming text message bubbles by extracting various visual attributes such as
     * background color, corner radius, stroke width, and text appearances. It also
     * configures elements like date, receipt indicators, avatar, and thread
     * indicators. The attributes are obtained from the {@link TypedArray}, and the
     * method ensures that the array is properly recycled after use.
     *
     * @param style The style resource ID for the incoming text bubble.
     */
    public void setIncomingTextBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatTextBubble);
        try {
            setIncomingTextBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatTextBubble_cometchatTextBubbleBackgroundColor,
                                                                     incomingMessageBubbleBackgroundColor));
            setIncomingTextBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatTextBubble_cometchatTextBubbleCornerRadius,
                                                                               incomingMessageBubbleCornerRadius));
            setIncomingTextBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatTextBubble_cometchatTextBubbleStrokeWidth,
                                                                              incomingMessageBubbleStrokeWidth));
            setIncomingTextBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatTextBubble_cometchatTextBubbleStrokeColor,
                                                                 incomingMessageBubbleStrokeColor));
            setIncomingTextBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatTextBubble_cometchatTextBubbleBackgroundDrawable));
            setIncomingTextBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatTextBubble_cometchatTextBubbleDateStyle,
                                                                    incomingMessageBubbleDateStyle));
            setIncomingTextBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatTextBubble_cometchatTextBubbleMessageReceiptStyle,
                                                                       incomingMessageBubbleReceiptStyle));
            setIncomingTextBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatTextBubble_cometchatTextBubbleAvatarStyle,
                                                                      incomingMessageBubbleAvatarStyle));
            setIncomingTextBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatTextBubble_cometchatTextBubbleThreadIndicatorTextAppearance,
                                                                                        incomingMessageBubbleThreadIndicatorTextAppearance));
            setIncomingTextBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatTextBubble_cometchatTextBubbleThreadIndicatorTextColor,
                                                                              incomingMessageBubbleThreadIndicatorTextColor));
            setIncomingTextBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatTextBubble_cometchatTextBubbleThreadIndicatorIconTint,
                                                                             incomingMessageBubbleThreadIndicatorIconTint));
            setIncomingTextBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatTextBubble_cometchatTextBubbleSenderNameTextAppearance,
                                                                                   incomingMessageBubbleSenderNameTextAppearance));
            setIncomingTextBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatTextBubble_cometchatTextBubbleSenderNameTextColor,
                                                                         incomingMessageBubbleSenderNameTextColor));
        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the style for incoming image message bubbles and applies the
     * corresponding attributes. This method applies a given style resource to the
     * incoming image message bubbles by extracting various visual attributes such
     * as background color, corner radius, stroke width, and text appearances. It
     * also configures elements like date, receipt indicators, avatar, and thread
     * indicators. The attributes are obtained from the {@link TypedArray}, and the
     * method ensures that the array is properly recycled after use.
     *
     * @param style The style resource ID for the incoming image bubble.
     */
    public void setIncomingImageBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatImageBubble);
        try {
            setIncomingImageBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatImageBubble_cometchatImageBubbleBackgroundColor,
                                                                      incomingMessageBubbleBackgroundColor));
            setIncomingImageBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatImageBubble_cometchatImageBubbleCornerRadius,
                                                                                incomingMessageBubbleCornerRadius));
            setIncomingImageBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatImageBubble_cometchatImageBubbleStrokeWidth,
                                                                               incomingMessageBubbleStrokeWidth));
            setIncomingImageBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatImageBubble_cometchatImageBubbleStrokeColor,
                                                                  incomingMessageBubbleStrokeColor));
            setIncomingImageBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatImageBubble_cometchatImageBubbleBackgroundDrawable));
            setIncomingImageBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatImageBubble_cometchatImageBubbleDateStyle,
                                                                     incomingMessageBubbleDateStyle));
            setIncomingImageBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatImageBubble_cometchatImageBubbleMessageReceiptStyle,
                                                                        incomingMessageBubbleReceiptStyle));
            setIncomingImageBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatImageBubble_cometchatImageBubbleAvatarStyle,
                                                                       incomingMessageBubbleAvatarStyle));
            setIncomingImageBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatImageBubble_cometchatImageBubbleThreadIndicatorTextAppearance,
                                                                                         incomingMessageBubbleThreadIndicatorTextAppearance));
            setIncomingImageBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatImageBubble_cometchatImageBubbleThreadIndicatorTextColor,
                                                                               incomingMessageBubbleThreadIndicatorTextColor));
            setIncomingImageBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatImageBubble_cometchatImageBubbleThreadIndicatorIconTint,
                                                                              incomingMessageBubbleThreadIndicatorIconTint));
            setIncomingImageBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatImageBubble_cometchatImageBubbleSenderNameTextAppearance,
                                                                                    incomingMessageBubbleSenderNameTextAppearance));
            setIncomingImageBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatImageBubble_cometchatImageBubbleSenderNameTextColor,
                                                                          incomingMessageBubbleSenderNameTextColor));

        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the style for incoming audio message bubbles and applies the
     * corresponding attributes. This method applies a style resource to the
     * incoming audio message bubbles by extracting various visual attributes such
     * as background color, corner radius, stroke width, and text appearances. It
     * also configures elements like date, receipt indicators, avatar, and thread
     * indicators. The attributes are obtained from the {@link TypedArray}, and the
     * method ensures that the array is properly recycled after use.
     *
     * @param style The style resource ID for the incoming audio bubble.
     */
    public void setIncomingAudioBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatAudioBubble);
        try {
            setIncomingAudioBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatAudioBubble_cometchatAudioBubbleBackgroundColor,
                                                                      incomingMessageBubbleBackgroundColor));
            setIncomingAudioBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatAudioBubble_cometchatAudioBubbleCornerRadius,
                                                                                incomingMessageBubbleCornerRadius));
            setIncomingAudioBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatAudioBubble_cometchatAudioBubbleStrokeWidth,
                                                                               incomingMessageBubbleStrokeWidth));
            setIncomingAudioBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatAudioBubble_cometchatAudioBubbleStrokeColor,
                                                                  incomingMessageBubbleStrokeColor));
            setIncomingAudioBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatAudioBubble_cometchatAudioBubbleBackgroundDrawable));
            setIncomingAudioBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatAudioBubble_cometchatAudioBubbleDateStyle,
                                                                     incomingMessageBubbleDateStyle));
            setIncomingAudioBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatAudioBubble_cometchatAudioBubbleMessageReceiptStyle,
                                                                        incomingMessageBubbleReceiptStyle));
            setIncomingAudioBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatAudioBubble_cometchatAudioBubbleAvatarStyle,
                                                                       incomingMessageBubbleAvatarStyle));
            setIncomingAudioBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatAudioBubble_cometchatAudioBubbleThreadIndicatorTextAppearance,
                                                                                         incomingMessageBubbleThreadIndicatorTextAppearance));
            setIncomingAudioBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatAudioBubble_cometchatAudioBubbleThreadIndicatorTextColor,
                                                                               incomingMessageBubbleThreadIndicatorTextColor));
            setIncomingAudioBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatAudioBubble_cometchatAudioBubbleThreadIndicatorIconTint,
                                                                              incomingMessageBubbleThreadIndicatorIconTint));
            setIncomingAudioBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatAudioBubble_cometchatAudioBubbleSenderNameTextAppearance,
                                                                                    incomingMessageBubbleSenderNameTextAppearance));
            setIncomingAudioBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatAudioBubble_cometchatAudioBubbleSenderNameTextColor,
                                                                          incomingMessageBubbleSenderNameTextColor));

        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the style for incoming video message bubbles and applies the
     * corresponding attributes. This method applies a style resource to the
     * incoming video message bubbles by extracting various visual attributes such
     * as background color, corner radius, stroke width, and text appearances. It
     * also configures elements like date, receipt indicators, avatar, and thread
     * indicators. The attributes are obtained from the {@link TypedArray}, and the
     * method ensures that the array is properly recycled after use.
     *
     * @param style The style resource ID for the incoming video bubble.
     */
    public void setIncomingVideoBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatVideoBubble);
        try {
            setIncomingVideoBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatVideoBubble_cometchatVideoBubbleBackgroundColor,
                                                                      incomingMessageBubbleBackgroundColor));
            setIncomingVideoBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatVideoBubble_cometchatVideoBubbleCornerRadius,
                                                                                incomingMessageBubbleCornerRadius));
            setIncomingVideoBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatVideoBubble_cometchatVideoBubbleStrokeWidth,
                                                                               incomingMessageBubbleStrokeWidth));
            setIncomingVideoBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatVideoBubble_cometchatVideoBubbleStrokeColor,
                                                                  incomingMessageBubbleStrokeColor));
            setIncomingVideoBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatVideoBubble_cometchatVideoBubbleBackgroundDrawable));
            setIncomingVideoBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatVideoBubble_cometchatVideoBubbleDateStyle,
                                                                     incomingMessageBubbleDateStyle));
            setIncomingVideoBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatVideoBubble_cometchatVideoBubbleMessageReceiptStyle,
                                                                        incomingMessageBubbleReceiptStyle));
            setIncomingVideoBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatVideoBubble_cometchatVideoBubbleAvatarStyle,
                                                                       incomingMessageBubbleAvatarStyle));
            setIncomingVideoBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatVideoBubble_cometchatVideoBubbleThreadIndicatorTextAppearance,
                                                                                         incomingMessageBubbleThreadIndicatorTextAppearance));
            setIncomingVideoBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatVideoBubble_cometchatVideoBubbleThreadIndicatorTextColor,
                                                                               incomingMessageBubbleThreadIndicatorTextColor));
            setIncomingVideoBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatVideoBubble_cometchatVideoBubbleThreadIndicatorIconTint,
                                                                              incomingMessageBubbleThreadIndicatorIconTint));
            setIncomingVideoBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatVideoBubble_cometchatVideoBubbleSenderNameTextAppearance,
                                                                                    incomingMessageBubbleSenderNameTextAppearance));
            setIncomingVideoBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatVideoBubble_cometchatVideoBubbleSenderNameTextColor,
                                                                          incomingMessageBubbleSenderNameTextColor));

        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the style for incoming file message bubbles and applies the
     * corresponding attributes. This method applies a style resource to the
     * incoming file message bubbles by extracting various visual attributes such as
     * background color, corner radius, stroke width, and text appearances. It also
     * configures elements like date, receipt indicators, avatar, and thread
     * indicators. The attributes are obtained from the {@link TypedArray}, and the
     * method ensures that the array is properly recycled after use.
     *
     * @param style The style resource ID for the incoming file bubble.
     */
    public void setIncomingFileBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatFileBubble);
        try {
            setIncomingFileBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatFileBubble_cometchatFileBubbleBackgroundColor,
                                                                     incomingMessageBubbleBackgroundColor));
            setIncomingFileBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatFileBubble_cometchatFileBubbleCornerRadius,
                                                                               incomingMessageBubbleCornerRadius));
            setIncomingFileBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatFileBubble_cometchatFileBubbleStrokeWidth,
                                                                              incomingMessageBubbleStrokeWidth));
            setIncomingFileBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatFileBubble_cometchatFileBubbleStrokeColor,
                                                                 incomingMessageBubbleStrokeColor));
            setIncomingFileBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatFileBubble_cometchatFileBubbleBackgroundDrawable));
            setIncomingFileBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatFileBubble_cometchatFileBubbleDateStyle,
                                                                    incomingMessageBubbleDateStyle));
            setIncomingFileBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatFileBubble_cometchatFileBubbleMessageReceiptStyle,
                                                                       incomingMessageBubbleReceiptStyle));
            setIncomingFileBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatFileBubble_cometchatFileBubbleAvatarStyle,
                                                                      incomingMessageBubbleAvatarStyle));
            setIncomingFileBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatFileBubble_cometchatFileBubbleThreadIndicatorTextAppearance,
                                                                                        incomingMessageBubbleThreadIndicatorTextAppearance));
            setIncomingFileBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatFileBubble_cometchatFileBubbleThreadIndicatorTextColor,
                                                                              incomingMessageBubbleThreadIndicatorTextColor));
            setIncomingFileBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatFileBubble_cometchatFileBubbleThreadIndicatorIconTint,
                                                                             incomingMessageBubbleThreadIndicatorIconTint));
            setIncomingFileBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatFileBubble_cometchatFileBubbleSenderNameTextAppearance,
                                                                                   incomingMessageBubbleSenderNameTextAppearance));
            setIncomingFileBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatFileBubble_cometchatFileBubbleSenderNameTextColor,
                                                                         incomingMessageBubbleSenderNameTextColor));

        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the style for incoming form bubbles.
     *
     * @param formBubbleStyle The resource ID of the style to apply to incoming form bubbles.
     */
    public void setIncomingFormBubbleStyle(@StyleRes int formBubbleStyle) {
        TypedArray typedArray = context.obtainStyledAttributes(formBubbleStyle, R.styleable.CometChatFormBubble);
        try {
            setIncomingFormBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatFormBubble_cometchatFormBubbleBackgroundColor,
                                                                     incomingMessageBubbleBackgroundColor));
            setIncomingFormBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatFormBubble_cometchatFormBubbleCornerRadius,
                                                                               incomingMessageBubbleCornerRadius));
            setIncomingFormBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatFormBubble_cometchatFormBubbleStrokeWidth,
                                                                              incomingMessageBubbleStrokeWidth));
            setIncomingFormBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatFormBubble_cometchatFormBubbleStrokeColor,
                                                                 incomingMessageBubbleStrokeColor));
            setIncomingFormBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatFormBubble_cometchatFormBubbleBackgroundDrawable));
            setIncomingFormBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatFormBubble_cometchatFormBubbleDateStyle,
                                                                    incomingMessageBubbleDateStyle));
            setIncomingFormBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatFormBubble_cometchatFormBubbleMessageReceiptStyle,
                                                                       incomingMessageBubbleReceiptStyle));
            setIncomingFormBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatFormBubble_cometchatFormBubbleAvatarStyle,
                                                                      incomingMessageBubbleAvatarStyle));
            setIncomingFormBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatFormBubble_cometchatFormBubbleThreadIndicatorTextAppearance,
                                                                                        incomingMessageBubbleThreadIndicatorTextAppearance));
            setIncomingFormBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatFormBubble_cometchatFormBubbleThreadIndicatorTextColor,
                                                                              incomingMessageBubbleThreadIndicatorTextColor));
            setIncomingFormBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatFormBubble_cometchatFormBubbleThreadIndicatorIconTint,
                                                                             incomingMessageBubbleThreadIndicatorIconTint));
            setIncomingFormBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatFormBubble_cometchatFormBubbleSenderNameTextAppearance,
                                                                                   incomingMessageBubbleSenderNameTextAppearance));
            setIncomingFormBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatFormBubble_cometchatFormBubbleSenderNameTextColor,
                                                                         incomingMessageBubbleSenderNameTextColor));
        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the style for incoming scheduler bubbles.
     *
     * @param schedulerBubbleStyle The resource ID of the style to apply to incoming scheduler
     *                             bubbles.
     */
    public void setIncomingSchedulerBubbleStyle(@StyleRes int schedulerBubbleStyle) {
        TypedArray typedArray = context.obtainStyledAttributes(schedulerBubbleStyle, R.styleable.CometChatSchedulerBubble);
        try {
            setIncomingSchedulerBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleBackgroundColor,
                                                                          incomingMessageBubbleBackgroundColor));
            setIncomingSchedulerBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleCornerRadius,
                                                                                    incomingMessageBubbleCornerRadius));
            setIncomingSchedulerBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleStrokeWidth,
                                                                                   incomingMessageBubbleStrokeWidth));
            setIncomingSchedulerBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleStrokeColor,
                                                                      incomingMessageBubbleStrokeColor));
            setIncomingSchedulerBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleBackgroundDrawable));
            setIncomingSchedulerBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleDateStyle,
                                                                         incomingMessageBubbleDateStyle));
            setIncomingSchedulerBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleMessageReceiptStyle,
                                                                            incomingMessageBubbleReceiptStyle));
            setIncomingSchedulerBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleAvatarStyle,
                                                                           incomingMessageBubbleAvatarStyle));
            setIncomingSchedulerBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleThreadIndicatorTextAppearance,
                                                                                             incomingMessageBubbleThreadIndicatorTextAppearance));
            setIncomingSchedulerBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleThreadIndicatorTextColor,
                                                                                   incomingMessageBubbleThreadIndicatorTextColor));
            setIncomingSchedulerBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleThreadIndicatorIconTint,
                                                                                  incomingMessageBubbleThreadIndicatorIconTint));
            setIncomingSchedulerBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleSenderNameTextAppearance,
                                                                                        incomingMessageBubbleSenderNameTextAppearance));
            setIncomingSchedulerBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleSenderNameTextColor,
                                                                              incomingMessageBubbleSenderNameTextColor));
        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the style for incoming poll message bubbles and applies the
     * corresponding attributes. This method applies a style resource to the
     * incoming poll message bubbles by extracting various visual attributes such as
     * background color, corner radius, stroke width, and text appearances. It also
     * configures elements like date, receipt indicators, avatar, and thread
     * indicators. The attributes are obtained from the {@link TypedArray}, and the
     * method ensures that the array is properly recycled after use.
     *
     * @param style The style resource ID for the incoming poll bubble.
     */
    public void setIncomingPollBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatPollBubble);
        try {
            setIncomingPollBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleBackgroundColor,
                                                                     incomingMessageBubbleBackgroundColor));
            setIncomingPollBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatPollBubble_cometchatPollBubbleCornerRadius,
                                                                               incomingMessageBubbleCornerRadius));
            setIncomingPollBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatPollBubble_cometchatPollBubbleStrokeWidth,
                                                                              incomingMessageBubbleStrokeWidth));
            setIncomingPollBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleStrokeColor,
                                                                 incomingMessageBubbleStrokeColor));
            setIncomingPollBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatPollBubble_cometchatPollBubbleBackgroundDrawable));
            setIncomingPollBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatPollBubble_cometchatPollBubbleDateStyle,
                                                                    incomingMessageBubbleDateStyle));
            setIncomingPollBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatPollBubble_cometchatPollBubbleMessageReceiptStyle,
                                                                       incomingMessageBubbleReceiptStyle));
            setIncomingPollBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatPollBubble_cometchatPollBubbleAvatarStyle,
                                                                      incomingMessageBubbleAvatarStyle));
            setIncomingPollBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatPollBubble_cometchatPollBubbleThreadIndicatorTextAppearance,
                                                                                        incomingMessageBubbleThreadIndicatorTextAppearance));
            setIncomingPollBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleThreadIndicatorTextColor,
                                                                              incomingMessageBubbleThreadIndicatorTextColor));
            setIncomingPollBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleThreadIndicatorIconTint,
                                                                             incomingMessageBubbleThreadIndicatorIconTint));
            setIncomingPollBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatPollBubble_cometchatPollBubbleSenderNameTextAppearance,
                                                                                   incomingMessageBubbleSenderNameTextAppearance));
            setIncomingPollBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleSenderNameTextColor,
                                                                         incomingMessageBubbleSenderNameTextColor));
        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the style for incoming sticker message bubbles and applies the
     * corresponding attributes. This method applies a style resource to the
     * incoming sticker message bubbles by extracting various visual attributes such
     * as background color, corner radius, stroke width, and text appearances. It
     * also configures elements like date, receipt indicators, avatar, and thread
     * indicators. The attributes are obtained from the {@link TypedArray}, and the
     * method ensures that the array is properly recycled after use. The default
     * background color is set to transparent.
     *
     * @param style The style resource ID for the incoming sticker bubble.
     */
    public void setIncomingStickerBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatStickerBubble);
        try {
            setIncomingStickerBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatStickerBubble_cometchatStickerBubbleBackgroundColor,
                                                                        Color.TRANSPARENT));
            setIncomingStickerBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatStickerBubble_cometchatStickerBubbleCornerRadius,
                                                                                  incomingMessageBubbleCornerRadius));
            setIncomingStickerBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatStickerBubble_cometchatStickerBubbleStrokeWidth,
                                                                                 incomingMessageBubbleStrokeWidth));
            setIncomingStickerBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatStickerBubble_cometchatStickerBubbleStrokeColor,
                                                                    incomingMessageBubbleStrokeColor));
            setIncomingStickerBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatStickerBubble_cometchatStickerBubbleBackgroundDrawable));
            setIncomingStickerBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatStickerBubble_cometchatStickerBubbleDateStyle,
                                                                       incomingMessageBubbleDateStyle));
            setIncomingStickerBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatStickerBubble_cometchatStickerBubbleMessageReceiptStyle,
                                                                          incomingMessageBubbleReceiptStyle));
            setIncomingStickerBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatStickerBubble_cometchatStickerBubbleAvatarStyle,
                                                                         incomingMessageBubbleAvatarStyle));
            setIncomingStickerBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatStickerBubble_cometchatStickerBubbleThreadIndicatorTextAppearance,
                                                                                           incomingMessageBubbleThreadIndicatorTextAppearance));
            setIncomingStickerBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatStickerBubble_cometchatStickerBubbleThreadIndicatorTextColor,
                                                                                 incomingMessageBubbleThreadIndicatorTextColor));
            setIncomingStickerBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatStickerBubble_cometchatStickerBubbleThreadIndicatorIconTint,
                                                                                incomingMessageBubbleThreadIndicatorIconTint));
            setIncomingStickerBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatStickerBubble_cometchatStickerBubbleSenderNameTextAppearance,
                                                                                      incomingMessageBubbleSenderNameTextAppearance));
            setIncomingStickerBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatStickerBubble_cometchatStickerBubbleSenderNameTextColor,
                                                                            incomingMessageBubbleSenderNameTextColor));
        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the style for incoming collaborative message bubbles and applies the
     * corresponding attributes. This method applies a style resource to the
     * incoming collaborative message bubbles by extracting various visual
     * attributes such as background color, corner radius, stroke width, and text
     * appearances. It also configures elements like date, receipt indicators,
     * avatar, and thread indicators. The attributes are obtained from the
     * {@link TypedArray}, and the method ensures that the array is properly
     * recycled after use.
     *
     * @param style The style resource ID for the incoming collaborative bubble.
     */
    public void setIncomingCollaborativeBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatCollaborativeBubble);
        try {
            setIncomingCollaborativeBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleBackgroundColor,
                                                                              incomingMessageBubbleBackgroundColor));
            setIncomingCollaborativeBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleCornerRadius,
                                                                                        incomingMessageBubbleCornerRadius));
            setIncomingCollaborativeBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleStrokeWidth,
                                                                                       incomingMessageBubbleStrokeWidth));
            setIncomingCollaborativeBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleStrokeColor,
                                                                          incomingMessageBubbleStrokeColor));
            setIncomingCollaborativeBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleBackgroundDrawable));
            setIncomingCollaborativeBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleDateStyle,
                                                                             incomingMessageBubbleDateStyle));
            setIncomingCollaborativeBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleMessageReceiptStyle,
                                                                                incomingMessageBubbleReceiptStyle));
            setIncomingCollaborativeBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleAvatarStyle,
                                                                               incomingMessageBubbleAvatarStyle));
            setIncomingCollaborativeBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleThreadIndicatorTextAppearance,
                                                                                                 incomingMessageBubbleThreadIndicatorTextAppearance));
            setIncomingCollaborativeBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleThreadIndicatorTextColor,
                                                                                       incomingMessageBubbleThreadIndicatorTextColor));
            setIncomingCollaborativeBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleThreadIndicatorIconTint,
                                                                                      incomingMessageBubbleThreadIndicatorIconTint));
            setIncomingCollaborativeBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleSenderNameTextAppearance,
                                                                                            incomingMessageBubbleSenderNameTextAppearance));
            setIncomingCollaborativeBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleSenderNameTextColor,
                                                                                  incomingMessageBubbleSenderNameTextColor));
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Sets the style resource for incoming message bubble reactions. This method
     * allows customization of the visual appearance of reactions associated with
     * incoming message bubbles by providing a style resource ID. The style can
     * define attributes such as colors, padding, and other UI elements specific to
     * incoming message reactions.
     *
     * @param style The style resource ID for incoming message bubble reactions.
     */
    public void setIncomingMessageBubbleReactionStyle(@StyleRes int style) {
        this.incomingMessageBubbleReactionStyle = style;
    }

    /**
     * Sets the style for incoming meet call bubbles.
     *
     * @param style The resource ID of the style to apply to incoming meet call
     *              bubbles.
     */
    public void setIncomingMeetCallBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatMeetCallBubble);
        try {
            setIncomingMeetCallBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleBackgroundColor,
                                                                         incomingMessageBubbleBackgroundColor));
            setIncomingMeetCallBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleCornerRadius,
                                                                                   incomingMessageBubbleCornerRadius));
            setIncomingMeetCallBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleStrokeWidth,
                                                                                  incomingMessageBubbleStrokeWidth));
            setIncomingMeetCallBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleStrokeColor,
                                                                     incomingMessageBubbleStrokeColor));
            setIncomingMeetCallBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleBackgroundDrawable));
            setIncomingMeetCallBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleAvatarStyle,
                                                                          incomingMessageBubbleAvatarStyle));
            setIncomingMeetCallBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleThreadIndicatorTextAppearance,
                                                                                            incomingMessageBubbleThreadIndicatorTextAppearance));
            setIncomingMeetCallBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleThreadIndicatorTextColor,
                                                                                  incomingMessageBubbleThreadIndicatorTextColor));
            setIncomingMeetCallBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleThreadIndicatorIconTint,
                                                                                 incomingMessageBubbleThreadIndicatorIconTint));
            setIncomingMeetCallBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleSenderNameTextAppearance,
                                                                                       incomingMessageBubbleSenderNameTextAppearance));
            setIncomingMeetCallBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleSenderNameTextColor,
                                                                             incomingMessageBubbleSenderNameTextColor));
        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the background color for the incoming delete bubble.
     *
     * @param incomingDeleteBubbleBackgroundColor the background color for the incoming delete bubble
     */
    public void setIncomingDeleteBubbleBackgroundColor(@ColorInt int incomingDeleteBubbleBackgroundColor) {
        this.incomingDeleteBubbleBackgroundColor = incomingDeleteBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the incoming delete bubble.
     *
     * @param incomingDeleteBubbleCornerRadius the corner radius for the incoming delete bubble
     */
    public void setIncomingDeleteBubbleCornerRadius(@ColorInt int incomingDeleteBubbleCornerRadius) {
        this.incomingDeleteBubbleCornerRadius = incomingDeleteBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the incoming delete bubble.
     *
     * @param incomingDeleteBubbleStrokeWidth the stroke width for the incoming delete bubble
     */
    public void setIncomingDeleteBubbleStrokeWidth(@Dimension int incomingDeleteBubbleStrokeWidth) {
        this.incomingDeleteBubbleStrokeWidth = incomingDeleteBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the incoming delete bubble.
     *
     * @param incomingDeleteBubbleStrokeColor the stroke color for the incoming delete bubble
     */
    public void setIncomingDeleteBubbleStrokeColor(@ColorInt int incomingDeleteBubbleStrokeColor) {
        this.incomingDeleteBubbleStrokeColor = incomingDeleteBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the incoming delete bubble.
     *
     * @param incomingDeleteBubbleBackgroundDrawable the background drawable for the incoming delete bubble
     */
    public void setIncomingDeleteBubbleBackgroundDrawable(Drawable incomingDeleteBubbleBackgroundDrawable) {
        this.incomingDeleteBubbleBackgroundDrawable = incomingDeleteBubbleBackgroundDrawable;
    }

    /**
     * Sets the date style for the incoming delete bubble.
     *
     * @param incomingDeleteBubbleDateStyle the date style resource ID for the incoming delete bubble
     */
    public void setIncomingDeleteBubbleDateStyle(@StyleRes int incomingDeleteBubbleDateStyle) {
        this.incomingDeleteBubbleDateStyle = incomingDeleteBubbleDateStyle;
    }

    /**
     * Sets the avatar style for the incoming delete bubble.
     *
     * @param incomingDeleteBubbleAvatarStyle the avatar style resource ID for the incoming delete bubble
     */
    public void setIncomingDeleteBubbleAvatarStyle(@StyleRes int incomingDeleteBubbleAvatarStyle) {
        this.incomingDeleteBubbleAvatarStyle = incomingDeleteBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance for the incoming delete bubble sender name.
     *
     * @param incomingDeleteBubbleSenderNameTextAppearance the text appearance resource ID for the incoming delete bubble
     *                                                     sender name
     */
    public void setIncomingDeleteBubbleSenderNameTextAppearance(@StyleRes int incomingDeleteBubbleSenderNameTextAppearance) {
        this.incomingDeleteBubbleSenderNameTextAppearance = incomingDeleteBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the incoming delete bubble sender name.
     *
     * @param incomingDeleteBubbleSenderNameTextColor the text color for the incoming delete bubble sender name
     */
    public void setIncomingDeleteBubbleSenderNameTextColor(@ColorInt int incomingDeleteBubbleSenderNameTextColor) {
        this.incomingDeleteBubbleSenderNameTextColor = incomingDeleteBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the incoming text bubble.
     *
     * @param incomingTextBubbleBackgroundColor The background color for the incoming text bubble.
     */
    private void setIncomingTextBubbleBackgroundColor(@ColorInt int incomingTextBubbleBackgroundColor) {
        this.incomingTextBubbleBackgroundColor = incomingTextBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the incoming text bubble.
     *
     * @param incomingTextBubbleCornerRadius The corner radius for the incoming text bubble.
     */
    private void setIncomingTextBubbleCornerRadius(@Dimension int incomingTextBubbleCornerRadius) {
        this.incomingTextBubbleCornerRadius = incomingTextBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the incoming text bubble.
     *
     * @param incomingTextBubbleStrokeWidth The stroke width for the incoming text bubble.
     */
    private void setIncomingTextBubbleStrokeWidth(@Dimension int incomingTextBubbleStrokeWidth) {
        this.incomingTextBubbleStrokeWidth = incomingTextBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the incoming text bubble.
     *
     * @param incomingTextBubbleStrokeColor The stroke color for the incoming text bubble.
     */
    private void setIncomingTextBubbleStrokeColor(@ColorInt int incomingTextBubbleStrokeColor) {
        this.incomingTextBubbleStrokeColor = incomingTextBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the incoming text bubble.
     *
     * @param incomingTextBubbleBackgroundDrawable The background drawable for the incoming text bubble.
     */
    private void setIncomingTextBubbleBackgroundDrawable(Drawable incomingTextBubbleBackgroundDrawable) {
        this.incomingTextBubbleBackgroundDrawable = incomingTextBubbleBackgroundDrawable;
    }

    /**
     * Sets the style resource for the incoming text bubble date.
     *
     * @param incomingTextBubbleDateStyle The style resource to apply.
     */
    public void setIncomingTextBubbleDateStyle(@StyleRes int incomingTextBubbleDateStyle) {
        this.incomingTextBubbleDateStyle = incomingTextBubbleDateStyle;
    }

    /**
     * Sets the style resource for the incoming text bubble receipt.
     *
     * @param incomingTextBubbleReceiptStyle The style resource to apply.
     */
    public void setIncomingTextBubbleReceiptStyle(@StyleRes int incomingTextBubbleReceiptStyle) {
        this.incomingTextBubbleReceiptStyle = incomingTextBubbleReceiptStyle;
    }

    /**
     * Sets the style resource for the incoming text bubble avatar.
     *
     * @param incomingTextBubbleAvatarStyle The style resource to apply.
     */
    public void setIncomingTextBubbleAvatarStyle(@StyleRes int incomingTextBubbleAvatarStyle) {
        this.incomingTextBubbleAvatarStyle = incomingTextBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance resource for the incoming text bubble thread
     * indicator.
     *
     * @param incomingTextBubbleThreadIndicatorTextAppearance The text appearance resource to apply.
     */
    public void setIncomingTextBubbleThreadIndicatorTextAppearance(@StyleRes int incomingTextBubbleThreadIndicatorTextAppearance) {
        this.incomingTextBubbleThreadIndicatorTextAppearance = incomingTextBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the incoming text bubble thread indicator.
     *
     * @param incomingTextBubbleThreadIndicatorTextColor The color to apply.
     */
    public void setIncomingTextBubbleThreadIndicatorTextColor(@ColorInt int incomingTextBubbleThreadIndicatorTextColor) {
        this.incomingTextBubbleThreadIndicatorTextColor = incomingTextBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the tint color for the incoming text bubble thread indicator icon.
     *
     * @param incomingTextBubbleThreadIndicatorIconTint The color to apply.
     */
    public void setIncomingTextBubbleThreadIndicatorIconTint(@ColorInt int incomingTextBubbleThreadIndicatorIconTint) {
        this.incomingTextBubbleThreadIndicatorIconTint = incomingTextBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance resource for the incoming text bubble sender name.
     *
     * @param incomingTextBubbleSenderNameTextAppearance The text appearance resource to apply.
     */
    public void setIncomingTextBubbleSenderNameTextAppearance(@StyleRes int incomingTextBubbleSenderNameTextAppearance) {
        this.incomingTextBubbleSenderNameTextAppearance = incomingTextBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the incoming text bubble sender name.
     *
     * @param incomingTextBubbleSenderNameTextColor The color to apply.
     */
    public void setIncomingTextBubbleSenderNameTextColor(@ColorInt int incomingTextBubbleSenderNameTextColor) {
        this.incomingTextBubbleSenderNameTextColor = incomingTextBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the incoming image bubble.
     *
     * @param incomingImageBubbleBackgroundColor The background color for the incoming image bubble.
     */
    private void setIncomingImageBubbleBackgroundColor(@ColorInt int incomingImageBubbleBackgroundColor) {
        this.incomingImageBubbleBackgroundColor = incomingImageBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the incoming image bubble.
     *
     * @param incomingImageBubbleCornerRadius The corner radius for the incoming image bubble.
     */
    private void setIncomingImageBubbleCornerRadius(@Dimension int incomingImageBubbleCornerRadius) {
        this.incomingImageBubbleCornerRadius = incomingImageBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the incoming image bubble.
     *
     * @param incomingImageBubbleStrokeWidth The stroke width for the incoming image bubble.
     */
    private void setIncomingImageBubbleStrokeWidth(@Dimension int incomingImageBubbleStrokeWidth) {
        this.incomingImageBubbleStrokeWidth = incomingImageBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the incoming image bubble.
     *
     * @param incomingImageBubbleStrokeColor The stroke color for the incoming image bubble.
     */
    private void setIncomingImageBubbleStrokeColor(@ColorInt int incomingImageBubbleStrokeColor) {
        this.incomingImageBubbleStrokeColor = incomingImageBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the incoming image bubble.
     *
     * @param incomingImageBubbleBackgroundDrawable The background drawable for the incoming image bubble.
     */
    private void setIncomingImageBubbleBackgroundDrawable(Drawable incomingImageBubbleBackgroundDrawable) {
        this.incomingImageBubbleBackgroundDrawable = incomingImageBubbleBackgroundDrawable;
    }

    /**
     * Sets the style resource for the incoming image bubble date.
     *
     * @param incomingImageBubbleDateStyle The style resource to apply.
     */
    public void setIncomingImageBubbleDateStyle(@StyleRes int incomingImageBubbleDateStyle) {
        this.incomingImageBubbleDateStyle = incomingImageBubbleDateStyle;
    }

    /**
     * Sets the style resource for the incoming image bubble receipt.
     *
     * @param incomingImageBubbleReceiptStyle The style resource to apply.
     */
    public void setIncomingImageBubbleReceiptStyle(@StyleRes int incomingImageBubbleReceiptStyle) {
        this.incomingImageBubbleReceiptStyle = incomingImageBubbleReceiptStyle;
    }

    /**
     * Sets the style resource for the incoming image bubble avatar.
     *
     * @param incomingImageBubbleAvatarStyle The style resource to apply.
     */
    public void setIncomingImageBubbleAvatarStyle(@StyleRes int incomingImageBubbleAvatarStyle) {
        this.incomingImageBubbleAvatarStyle = incomingImageBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance resource for the incoming image bubble thread
     * indicator.
     *
     * @param incomingImageBubbleThreadIndicatorTextAppearance The text appearance resource to apply.
     */
    public void setIncomingImageBubbleThreadIndicatorTextAppearance(@StyleRes int incomingImageBubbleThreadIndicatorTextAppearance) {
        this.incomingImageBubbleThreadIndicatorTextAppearance = incomingImageBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the incoming image bubble thread indicator.
     *
     * @param incomingImageBubbleThreadIndicatorTextColor The color to apply.
     */
    public void setIncomingImageBubbleThreadIndicatorTextColor(@ColorInt int incomingImageBubbleThreadIndicatorTextColor) {
        this.incomingImageBubbleThreadIndicatorTextColor = incomingImageBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the tint color for the incoming image bubble thread indicator icon.
     *
     * @param incomingImageBubbleThreadIndicatorIconTint The color to apply.
     */
    public void setIncomingImageBubbleThreadIndicatorIconTint(@ColorInt int incomingImageBubbleThreadIndicatorIconTint) {
        this.incomingImageBubbleThreadIndicatorIconTint = incomingImageBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance resource for the incoming image bubble sender name.
     *
     * @param incomingImageBubbleSenderNameTextAppearance The text appearance resource to apply.
     */
    public void setIncomingImageBubbleSenderNameTextAppearance(@StyleRes int incomingImageBubbleSenderNameTextAppearance) {
        this.incomingImageBubbleSenderNameTextAppearance = incomingImageBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the incoming image bubble sender name.
     *
     * @param incomingImageBubbleSenderNameTextColor The color to apply.
     */
    public void setIncomingImageBubbleSenderNameTextColor(@ColorInt int incomingImageBubbleSenderNameTextColor) {
        this.incomingImageBubbleSenderNameTextColor = incomingImageBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the incoming audio bubble.
     *
     * @param incomingAudioBubbleBackgroundColor The background color for the incoming audio bubble.
     */
    private void setIncomingAudioBubbleBackgroundColor(@ColorInt int incomingAudioBubbleBackgroundColor) {
        this.incomingAudioBubbleBackgroundColor = incomingAudioBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the incoming audio bubble.
     *
     * @param incomingAudioBubbleCornerRadius The corner radius for the incoming audio bubble.
     */
    private void setIncomingAudioBubbleCornerRadius(@Dimension int incomingAudioBubbleCornerRadius) {
        this.incomingAudioBubbleCornerRadius = incomingAudioBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the incoming audio bubble.
     *
     * @param incomingAudioBubbleStrokeWidth The stroke width for the incoming audio bubble.
     */
    private void setIncomingAudioBubbleStrokeWidth(@Dimension int incomingAudioBubbleStrokeWidth) {
        this.incomingAudioBubbleStrokeWidth = incomingAudioBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the incoming audio bubble.
     *
     * @param incomingAudioBubbleStrokeColor The stroke color for the incoming audio bubble.
     */
    private void setIncomingAudioBubbleStrokeColor(@ColorInt int incomingAudioBubbleStrokeColor) {
        this.incomingAudioBubbleStrokeColor = incomingAudioBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the incoming audio bubble.
     *
     * @param incomingAudioBubbleBackgroundDrawable The background drawable for the incoming audio bubble.
     */
    private void setIncomingAudioBubbleBackgroundDrawable(Drawable incomingAudioBubbleBackgroundDrawable) {
        this.incomingAudioBubbleBackgroundDrawable = incomingAudioBubbleBackgroundDrawable;
    }

    /**
     * Sets the style resource for the incoming audio bubble date.
     *
     * @param incomingAudioBubbleDateStyle The style resource to apply.
     */
    public void setIncomingAudioBubbleDateStyle(@StyleRes int incomingAudioBubbleDateStyle) {
        this.incomingAudioBubbleDateStyle = incomingAudioBubbleDateStyle;
    }

    /**
     * Sets the style resource for the incoming audio bubble receipt.
     *
     * @param incomingAudioBubbleReceiptStyle The style resource to apply.
     */
    public void setIncomingAudioBubbleReceiptStyle(@StyleRes int incomingAudioBubbleReceiptStyle) {
        this.incomingAudioBubbleReceiptStyle = incomingAudioBubbleReceiptStyle;
    }

    /**
     * Sets the style resource for the incoming audio bubble avatar.
     *
     * @param incomingAudioBubbleAvatarStyle The style resource to apply.
     */
    public void setIncomingAudioBubbleAvatarStyle(@StyleRes int incomingAudioBubbleAvatarStyle) {
        this.incomingAudioBubbleAvatarStyle = incomingAudioBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance for the thread indicator in the incoming audio
     * bubble.
     *
     * @param incomingAudioBubbleThreadIndicatorTextAppearance the style resource ID for the text appearance
     */
    public void setIncomingAudioBubbleThreadIndicatorTextAppearance(@StyleRes int incomingAudioBubbleThreadIndicatorTextAppearance) {
        this.incomingAudioBubbleThreadIndicatorTextAppearance = incomingAudioBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the thread indicator in the incoming audio bubble.
     *
     * @param incomingAudioBubbleThreadIndicatorTextColor the color to set for the thread indicator text
     */
    public void setIncomingAudioBubbleThreadIndicatorTextColor(@ColorInt int incomingAudioBubbleThreadIndicatorTextColor) {
        this.incomingAudioBubbleThreadIndicatorTextColor = incomingAudioBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the icon tint for the thread indicator in the incoming audio bubble.
     *
     * @param incomingAudioBubbleThreadIndicatorIconTint the color to set for the thread indicator icon tint
     */
    public void setIncomingAudioBubbleThreadIndicatorIconTint(@ColorInt int incomingAudioBubbleThreadIndicatorIconTint) {
        this.incomingAudioBubbleThreadIndicatorIconTint = incomingAudioBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance for the sender name in the incoming audio bubble.
     *
     * @param incomingAudioBubbleSenderNameTextAppearance the style resource ID for the sender name text appearance
     */
    public void setIncomingAudioBubbleSenderNameTextAppearance(@StyleRes int incomingAudioBubbleSenderNameTextAppearance) {
        this.incomingAudioBubbleSenderNameTextAppearance = incomingAudioBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the sender name in the incoming audio bubble.
     *
     * @param incomingAudioBubbleSenderNameTextColor the color to set for the sender name
     */
    public void setIncomingAudioBubbleSenderNameTextColor(@ColorInt int incomingAudioBubbleSenderNameTextColor) {
        this.incomingAudioBubbleSenderNameTextColor = incomingAudioBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the incoming video bubble.
     *
     * @param incomingVideoBubbleBackgroundColor The background color for the incoming video bubble.
     */
    private void setIncomingVideoBubbleBackgroundColor(@ColorInt int incomingVideoBubbleBackgroundColor) {
        this.incomingVideoBubbleBackgroundColor = incomingVideoBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the incoming video bubble.
     *
     * @param incomingVideoBubbleCornerRadius The corner radius for the incoming video bubble.
     */
    private void setIncomingVideoBubbleCornerRadius(@Dimension int incomingVideoBubbleCornerRadius) {
        this.incomingVideoBubbleCornerRadius = incomingVideoBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the incoming video bubble.
     *
     * @param incomingVideoBubbleStrokeWidth The stroke width for the incoming video bubble.
     */
    private void setIncomingVideoBubbleStrokeWidth(@Dimension int incomingVideoBubbleStrokeWidth) {
        this.incomingVideoBubbleStrokeWidth = incomingVideoBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the incoming video bubble.
     *
     * @param incomingVideoBubbleStrokeColor The stroke color for the incoming video bubble.
     */
    private void setIncomingVideoBubbleStrokeColor(@ColorInt int incomingVideoBubbleStrokeColor) {
        this.incomingVideoBubbleStrokeColor = incomingVideoBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the incoming video bubble.
     *
     * @param incomingVideoBubbleBackgroundDrawable The background drawable for the incoming video bubble.
     */
    private void setIncomingVideoBubbleBackgroundDrawable(Drawable incomingVideoBubbleBackgroundDrawable) {
        this.incomingVideoBubbleBackgroundDrawable = incomingVideoBubbleBackgroundDrawable;
    }

    /**
     * Sets the style resource for the incoming video bubble date.
     *
     * @param incomingVideoBubbleDateStyle The style resource to apply.
     */
    public void setIncomingVideoBubbleDateStyle(@StyleRes int incomingVideoBubbleDateStyle) {
        this.incomingVideoBubbleDateStyle = incomingVideoBubbleDateStyle;
    }

    /**
     * Sets the style resource for the incoming video bubble receipt.
     *
     * @param incomingVideoBubbleReceiptStyle The style resource to apply.
     */
    public void setIncomingVideoBubbleReceiptStyle(@StyleRes int incomingVideoBubbleReceiptStyle) {
        this.incomingVideoBubbleReceiptStyle = incomingVideoBubbleReceiptStyle;
    }

    /**
     * Sets the style resource for the incoming video bubble avatar.
     *
     * @param incomingVideoBubbleAvatarStyle The style resource to apply.
     */
    public void setIncomingVideoBubbleAvatarStyle(@StyleRes int incomingVideoBubbleAvatarStyle) {
        this.incomingVideoBubbleAvatarStyle = incomingVideoBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance for the thread indicator in the incoming video
     * bubble.
     *
     * @param incomingVideoBubbleThreadIndicatorTextAppearance the style resource ID for the text appearance
     */
    public void setIncomingVideoBubbleThreadIndicatorTextAppearance(@StyleRes int incomingVideoBubbleThreadIndicatorTextAppearance) {
        this.incomingVideoBubbleThreadIndicatorTextAppearance = incomingVideoBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the thread indicator in the incoming video bubble.
     *
     * @param incomingVideoBubbleThreadIndicatorTextColor the color to set for the thread indicator text
     */
    public void setIncomingVideoBubbleThreadIndicatorTextColor(@ColorInt int incomingVideoBubbleThreadIndicatorTextColor) {
        this.incomingVideoBubbleThreadIndicatorTextColor = incomingVideoBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the icon tint for the thread indicator in the incoming video bubble.
     *
     * @param incomingVideoBubbleThreadIndicatorIconTint the color to set for the thread indicator icon tint
     */
    public void setIncomingVideoBubbleThreadIndicatorIconTint(@ColorInt int incomingVideoBubbleThreadIndicatorIconTint) {
        this.incomingVideoBubbleThreadIndicatorIconTint = incomingVideoBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance for the sender name in the incoming video bubble.
     *
     * @param incomingVideoBubbleSenderNameTextAppearance the style resource ID for the sender name text appearance
     */
    public void setIncomingVideoBubbleSenderNameTextAppearance(@StyleRes int incomingVideoBubbleSenderNameTextAppearance) {
        this.incomingVideoBubbleSenderNameTextAppearance = incomingVideoBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the sender name in the incoming video bubble.
     *
     * @param incomingVideoBubbleSenderNameTextColor the color to set for the sender name
     */
    public void setIncomingVideoBubbleSenderNameTextColor(@ColorInt int incomingVideoBubbleSenderNameTextColor) {
        this.incomingVideoBubbleSenderNameTextColor = incomingVideoBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the incoming file bubble.
     *
     * @param incomingFileBubbleBackgroundColor The background color for the incoming file bubble.
     */
    private void setIncomingFileBubbleBackgroundColor(@ColorInt int incomingFileBubbleBackgroundColor) {
        this.incomingFileBubbleBackgroundColor = incomingFileBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the incoming file bubble.
     *
     * @param incomingFileBubbleCornerRadius The corner radius for the incoming file bubble.
     */
    private void setIncomingFileBubbleCornerRadius(@Dimension int incomingFileBubbleCornerRadius) {
        this.incomingFileBubbleCornerRadius = incomingFileBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the incoming file bubble.
     *
     * @param incomingFileBubbleStrokeWidth The stroke width for the incoming file bubble.
     */
    private void setIncomingFileBubbleStrokeWidth(@Dimension int incomingFileBubbleStrokeWidth) {
        this.incomingFileBubbleStrokeWidth = incomingFileBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the incoming file bubble.
     *
     * @param incomingFileBubbleStrokeColor The stroke color for the incoming file bubble.
     */
    private void setIncomingFileBubbleStrokeColor(@ColorInt int incomingFileBubbleStrokeColor) {
        this.incomingFileBubbleStrokeColor = incomingFileBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the incoming file bubble.
     *
     * @param incomingFileBubbleBackgroundDrawable The background drawable for the incoming file bubble.
     */
    private void setIncomingFileBubbleBackgroundDrawable(Drawable incomingFileBubbleBackgroundDrawable) {
        this.incomingFileBubbleBackgroundDrawable = incomingFileBubbleBackgroundDrawable;
    }

    /**
     * Sets the style resource for the incoming file bubble date.
     *
     * @param incomingFileBubbleDateStyle The style resource to apply.
     */
    public void setIncomingFileBubbleDateStyle(@StyleRes int incomingFileBubbleDateStyle) {
        this.incomingFileBubbleDateStyle = incomingFileBubbleDateStyle;
    }

    /**
     * Sets the style resource for the incoming file bubble receipt.
     *
     * @param incomingFileBubbleReceiptStyle The style resource to apply.
     */
    public void setIncomingFileBubbleReceiptStyle(@StyleRes int incomingFileBubbleReceiptStyle) {
        this.incomingFileBubbleReceiptStyle = incomingFileBubbleReceiptStyle;
    }

    /**
     * Sets the style resource for the incoming file bubble avatar.
     *
     * @param incomingFileBubbleAvatarStyle The style resource to apply.
     */
    public void setIncomingFileBubbleAvatarStyle(@StyleRes int incomingFileBubbleAvatarStyle) {
        this.incomingFileBubbleAvatarStyle = incomingFileBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance resource for the incoming file bubble thread
     * indicator.
     *
     * @param incomingFileBubbleThreadIndicatorTextAppearance The text appearance resource to apply.
     */
    public void setIncomingFileBubbleThreadIndicatorTextAppearance(@StyleRes int incomingFileBubbleThreadIndicatorTextAppearance) {
        this.incomingFileBubbleThreadIndicatorTextAppearance = incomingFileBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the incoming file bubble thread indicator.
     *
     * @param incomingFileBubbleThreadIndicatorTextColor The color to apply.
     */
    public void setIncomingFileBubbleThreadIndicatorTextColor(@ColorInt int incomingFileBubbleThreadIndicatorTextColor) {
        this.incomingFileBubbleThreadIndicatorTextColor = incomingFileBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the tint color for the incoming file bubble thread indicator icon.
     *
     * @param incomingFileBubbleThreadIndicatorIconTint The color to apply.
     */
    public void setIncomingFileBubbleThreadIndicatorIconTint(@ColorInt int incomingFileBubbleThreadIndicatorIconTint) {
        this.incomingFileBubbleThreadIndicatorIconTint = incomingFileBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance resource for the incoming file bubble sender name.
     *
     * @param incomingFileBubbleSenderNameTextAppearance The text appearance resource to apply.
     */
    public void setIncomingFileBubbleSenderNameTextAppearance(@StyleRes int incomingFileBubbleSenderNameTextAppearance) {
        this.incomingFileBubbleSenderNameTextAppearance = incomingFileBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the incoming file bubble sender name.
     *
     * @param incomingFileBubbleSenderNameTextColor The color to apply.
     */
    public void setIncomingFileBubbleSenderNameTextColor(@ColorInt int incomingFileBubbleSenderNameTextColor) {
        this.incomingFileBubbleSenderNameTextColor = incomingFileBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the incoming form bubble.
     *
     * @param incomingFormBubbleBackgroundColor The color to set as the background.
     */
    private void setIncomingFormBubbleBackgroundColor(@ColorInt int incomingFormBubbleBackgroundColor) {
        this.incomingFormBubbleBackgroundColor = incomingFormBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the incoming form bubble.
     *
     * @param incomingFormBubbleCornerRadius The corner radius in pixels.
     */
    private void setIncomingFormBubbleCornerRadius(@Dimension int incomingFormBubbleCornerRadius) {
        this.incomingFormBubbleCornerRadius = incomingFormBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the incoming form bubble.
     *
     * @param incomingFormBubbleStrokeWidth The stroke width in pixels.
     */
    private void setIncomingFormBubbleStrokeWidth(@Dimension int incomingFormBubbleStrokeWidth) {
        this.incomingFormBubbleStrokeWidth = incomingFormBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the incoming form bubble.
     *
     * @param incomingFormBubbleStrokeColor The color to set as the stroke.
     */
    private void setIncomingFormBubbleStrokeColor(@ColorInt int incomingFormBubbleStrokeColor) {
        this.incomingFormBubbleStrokeColor = incomingFormBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the incoming form bubble.
     *
     * @param incomingFormBubbleBackgroundDrawable The drawable to set as the background.
     */
    private void setIncomingFormBubbleBackgroundDrawable(Drawable incomingFormBubbleBackgroundDrawable) {
        this.incomingFormBubbleBackgroundDrawable = incomingFormBubbleBackgroundDrawable;
    }

    /**
     * Sets the style resource for the incoming form bubble date.
     *
     * @param incomingFormBubbleDateStyle The style resource to apply.
     */
    public void setIncomingFormBubbleDateStyle(@StyleRes int incomingFormBubbleDateStyle) {
        this.incomingFormBubbleDateStyle = incomingFormBubbleDateStyle;
    }

    /**
     * Sets the style resource for the incoming form bubble receipt.
     *
     * @param incomingFormBubbleReceiptStyle The style resource to apply.
     */
    public void setIncomingFormBubbleReceiptStyle(@StyleRes int incomingFormBubbleReceiptStyle) {
        this.incomingFormBubbleReceiptStyle = incomingFormBubbleReceiptStyle;
    }

    /**
     * Sets the style resource for the incoming form bubble avatar.
     *
     * @param incomingFormBubbleAvatarStyle The style resource to apply.
     */
    public void setIncomingFormBubbleAvatarStyle(@StyleRes int incomingFormBubbleAvatarStyle) {
        this.incomingFormBubbleAvatarStyle = incomingFormBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance for the thread indicator in the incoming form
     * bubble.
     *
     * @param incomingFormBubbleThreadIndicatorTextAppearance the style resource ID for the text appearance
     */
    public void setIncomingFormBubbleThreadIndicatorTextAppearance(@StyleRes int incomingFormBubbleThreadIndicatorTextAppearance) {
        this.incomingFormBubbleThreadIndicatorTextAppearance = incomingFormBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the incoming form bubble thread indicator.
     *
     * @param incomingFormBubbleThreadIndicatorTextColor the color to set for the incoming form bubble thread indicator
     *                                                   text
     */
    public void setIncomingFormBubbleThreadIndicatorTextColor(@ColorInt int incomingFormBubbleThreadIndicatorTextColor) {
        this.incomingFormBubbleThreadIndicatorTextColor = incomingFormBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the tint color for the icon of the incoming form bubble thread
     * indicator.
     *
     * @param incomingFormBubbleThreadIndicatorIconTint the color to set for the incoming form bubble thread indicator
     *                                                  icon
     */
    public void setIncomingFormBubbleThreadIndicatorIconTint(@ColorInt int incomingFormBubbleThreadIndicatorIconTint) {
        this.incomingFormBubbleThreadIndicatorIconTint = incomingFormBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance style for the sender's name in the incoming form
     * bubble.
     *
     * @param incomingFormBubbleSenderNameTextAppearance the style resource ID for the incoming form bubble sender name
     *                                                   text appearance
     */
    public void setIncomingFormBubbleSenderNameTextAppearance(@StyleRes int incomingFormBubbleSenderNameTextAppearance) {
        this.incomingFormBubbleSenderNameTextAppearance = incomingFormBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the sender's name in the incoming form bubble.
     *
     * @param incomingFormBubbleSenderNameTextColor the color to set for the incoming form bubble sender name text
     */
    public void setIncomingFormBubbleSenderNameTextColor(@ColorInt int incomingFormBubbleSenderNameTextColor) {
        this.incomingFormBubbleSenderNameTextColor = incomingFormBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the incoming scheduler bubble.
     *
     * @param incomingSchedulerBubbleBackgroundColor The color to set as the background.
     */
    public void setIncomingSchedulerBubbleBackgroundColor(@ColorInt int incomingSchedulerBubbleBackgroundColor) {
        this.incomingSchedulerBubbleBackgroundColor = incomingSchedulerBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the incoming scheduler bubble.
     *
     * @param incomingSchedulerBubbleCornerRadius The corner radius in pixels.
     */
    public void setIncomingSchedulerBubbleCornerRadius(@Dimension int incomingSchedulerBubbleCornerRadius) {
        this.incomingSchedulerBubbleCornerRadius = incomingSchedulerBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the incoming scheduler bubble.
     *
     * @param incomingSchedulerBubbleStrokeWidth The stroke width in pixels.
     */
    public void setIncomingSchedulerBubbleStrokeWidth(@Dimension int incomingSchedulerBubbleStrokeWidth) {
        this.incomingSchedulerBubbleStrokeWidth = incomingSchedulerBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the incoming scheduler bubble.
     *
     * @param incomingSchedulerBubbleStrokeColor The color to set as the stroke.
     */
    public void setIncomingSchedulerBubbleStrokeColor(@ColorInt int incomingSchedulerBubbleStrokeColor) {
        this.incomingSchedulerBubbleStrokeColor = incomingSchedulerBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the incoming scheduler bubble.
     *
     * @param incomingSchedulerBubbleBackgroundDrawable The drawable to set as the background.
     */
    public void setIncomingSchedulerBubbleBackgroundDrawable(Drawable incomingSchedulerBubbleBackgroundDrawable) {
        this.incomingSchedulerBubbleBackgroundDrawable = incomingSchedulerBubbleBackgroundDrawable;
    }

    /**
     * Sets the style resource for the incoming scheduler bubble date.
     *
     * @param incomingSchedulerBubbleDateStyle The style resource to apply.
     */
    public void setIncomingSchedulerBubbleDateStyle(@StyleRes int incomingSchedulerBubbleDateStyle) {
        this.incomingSchedulerBubbleDateStyle = incomingSchedulerBubbleDateStyle;
    }

    /**
     * Sets the style resource for the incoming scheduler bubble receipt.
     *
     * @param incomingSchedulerBubbleReceiptStyle The style resource to apply.
     */
    public void setIncomingSchedulerBubbleReceiptStyle(@StyleRes int incomingSchedulerBubbleReceiptStyle) {
        this.incomingSchedulerBubbleReceiptStyle = incomingSchedulerBubbleReceiptStyle;
    }

    /**
     * Sets the style resource for the incoming scheduler bubble avatar.
     *
     * @param incomingSchedulerBubbleAvatarStyle The style resource to apply.
     */
    public void setIncomingSchedulerBubbleAvatarStyle(@StyleRes int incomingSchedulerBubbleAvatarStyle) {
        this.incomingSchedulerBubbleAvatarStyle = incomingSchedulerBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance style for the thread indicator in the incoming
     * scheduler bubble.
     *
     * @param incomingSchedulerBubbleThreadIndicatorTextAppearance the style resource ID for the incoming scheduler bubble thread
     *                                                             indicator text appearance
     */
    public void setIncomingSchedulerBubbleThreadIndicatorTextAppearance(@StyleRes int incomingSchedulerBubbleThreadIndicatorTextAppearance) {
        this.incomingSchedulerBubbleThreadIndicatorTextAppearance = incomingSchedulerBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the incoming scheduler bubble thread indicator.
     *
     * @param incomingSchedulerBubbleThreadIndicatorTextColor the color to set for the incoming scheduler bubble thread
     *                                                        indicator text
     */
    public void setIncomingSchedulerBubbleThreadIndicatorTextColor(@ColorInt int incomingSchedulerBubbleThreadIndicatorTextColor) {
        this.incomingSchedulerBubbleThreadIndicatorTextColor = incomingSchedulerBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the tint color for the icon of the incoming scheduler bubble thread
     * indicator.
     *
     * @param incomingSchedulerBubbleThreadIndicatorIconTint the color to set for the incoming scheduler bubble thread
     *                                                       indicator icon
     */
    public void setIncomingSchedulerBubbleThreadIndicatorIconTint(@ColorInt int incomingSchedulerBubbleThreadIndicatorIconTint) {
        this.incomingSchedulerBubbleThreadIndicatorIconTint = incomingSchedulerBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance style for the sender's name in the incoming
     * scheduler bubble.
     *
     * @param incomingSchedulerBubbleSenderNameTextAppearance the style resource ID for the incoming scheduler bubble sender
     *                                                        name text appearance
     */
    public void setIncomingSchedulerBubbleSenderNameTextAppearance(@StyleRes int incomingSchedulerBubbleSenderNameTextAppearance) {
        this.incomingSchedulerBubbleSenderNameTextAppearance = incomingSchedulerBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the sender's name in the incoming scheduler bubble.
     *
     * @param incomingSchedulerBubbleSenderNameTextColor the color to set for the incoming scheduler bubble sender name
     *                                                   text
     */
    public void setIncomingSchedulerBubbleSenderNameTextColor(@ColorInt int incomingSchedulerBubbleSenderNameTextColor) {
        this.incomingSchedulerBubbleSenderNameTextColor = incomingSchedulerBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the incoming poll bubble.
     *
     * @param incomingPollBubbleBackgroundColor The color to set as the background.
     */
    private void setIncomingPollBubbleBackgroundColor(@ColorInt int incomingPollBubbleBackgroundColor) {
        this.incomingPollBubbleBackgroundColor = incomingPollBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the incoming poll bubble.
     *
     * @param incomingPollBubbleCornerRadius The corner radius in pixels.
     */
    private void setIncomingPollBubbleCornerRadius(@Dimension int incomingPollBubbleCornerRadius) {
        this.incomingPollBubbleCornerRadius = incomingPollBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the incoming poll bubble.
     *
     * @param incomingPollBubbleStrokeWidth The stroke width in pixels.
     */
    private void setIncomingPollBubbleStrokeWidth(@Dimension int incomingPollBubbleStrokeWidth) {
        this.incomingPollBubbleStrokeWidth = incomingPollBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the incoming poll bubble.
     *
     * @param incomingPollBubbleStrokeColor The color to set as the stroke.
     */
    private void setIncomingPollBubbleStrokeColor(@ColorInt int incomingPollBubbleStrokeColor) {
        this.incomingPollBubbleStrokeColor = incomingPollBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the incoming poll bubble.
     *
     * @param incomingPollBubbleBackgroundDrawable The drawable to set as the background.
     */
    private void setIncomingPollBubbleBackgroundDrawable(Drawable incomingPollBubbleBackgroundDrawable) {
        this.incomingPollBubbleBackgroundDrawable = incomingPollBubbleBackgroundDrawable;
    }

    /**
     * Sets the style resource for the incoming poll bubble date.
     *
     * @param incomingPollBubbleDateStyle The style resource to apply.
     */
    public void setIncomingPollBubbleDateStyle(@StyleRes int incomingPollBubbleDateStyle) {
        this.incomingPollBubbleDateStyle = incomingPollBubbleDateStyle;
    }

    /**
     * Sets the style resource for the incoming poll bubble receipt.
     *
     * @param incomingPollBubbleReceiptStyle The style resource to apply.
     */
    public void setIncomingPollBubbleReceiptStyle(@StyleRes int incomingPollBubbleReceiptStyle) {
        this.incomingPollBubbleReceiptStyle = incomingPollBubbleReceiptStyle;
    }

    /**
     * Sets the style resource for the incoming poll bubble avatar.
     *
     * @param incomingPollBubbleAvatarStyle The style resource to apply.
     */
    public void setIncomingPollBubbleAvatarStyle(@StyleRes int incomingPollBubbleAvatarStyle) {
        this.incomingPollBubbleAvatarStyle = incomingPollBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance style for the thread indicator in the incoming poll
     * bubble.
     *
     * @param incomingPollBubbleThreadIndicatorTextAppearance the style resource ID for the incoming poll bubble thread
     *                                                        indicator text appearance
     */
    public void setIncomingPollBubbleThreadIndicatorTextAppearance(@StyleRes int incomingPollBubbleThreadIndicatorTextAppearance) {
        this.incomingPollBubbleThreadIndicatorTextAppearance = incomingPollBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the incoming poll bubble thread indicator.
     *
     * @param incomingPollBubbleThreadIndicatorTextColor the color to set for the incoming poll bubble thread indicator
     *                                                   text
     */
    public void setIncomingPollBubbleThreadIndicatorTextColor(@ColorInt int incomingPollBubbleThreadIndicatorTextColor) {
        this.incomingPollBubbleThreadIndicatorTextColor = incomingPollBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the icon tint for the incoming poll bubble thread indicator.
     *
     * @param incomingPollBubbleThreadIndicatorIconTint the icon tint for the incoming poll bubble thread indicator
     */
    public void setIncomingPollBubbleThreadIndicatorIconTint(@ColorInt int incomingPollBubbleThreadIndicatorIconTint) {
        this.incomingPollBubbleThreadIndicatorIconTint = incomingPollBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance for the incoming poll bubble sender name.
     *
     * @param incomingPollBubbleSenderNameTextAppearance the text appearance resource ID for the incoming poll bubble
     *                                                   sender name
     */
    public void setIncomingPollBubbleSenderNameTextAppearance(@StyleRes int incomingPollBubbleSenderNameTextAppearance) {
    }

    /**
     * Sets the text color for the incoming poll bubble sender name.
     *
     * @param incomingPollBubbleSenderNameTextColor the text color for the incoming poll bubble sender name
     */
    public void setIncomingPollBubbleSenderNameTextColor(@ColorInt int incomingPollBubbleSenderNameTextColor) {
        this.incomingPollBubbleSenderNameTextColor = incomingPollBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the incoming sticker bubble.
     *
     * @param incomingStickerBubbleBackgroundColor the background color for the incoming sticker bubble
     */
    public void setIncomingStickerBubbleBackgroundColor(@ColorInt int incomingStickerBubbleBackgroundColor) {
        this.incomingStickerBubbleBackgroundColor = incomingStickerBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the incoming sticker bubble.
     *
     * @param incomingStickerBubbleCornerRadius the corner radius for the incoming sticker bubble
     */
    public void setIncomingStickerBubbleCornerRadius(@Dimension int incomingStickerBubbleCornerRadius) {
        this.incomingStickerBubbleCornerRadius = incomingStickerBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the incoming sticker bubble.
     *
     * @param incomingStickerBubbleStrokeWidth the stroke width for the incoming sticker bubble
     */
    public void setIncomingStickerBubbleStrokeWidth(@Dimension int incomingStickerBubbleStrokeWidth) {
        this.incomingStickerBubbleStrokeWidth = incomingStickerBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the incoming sticker bubble.
     *
     * @param incomingStickerBubbleStrokeColor the stroke color for the incoming sticker bubble
     */
    public void setIncomingStickerBubbleStrokeColor(@ColorInt int incomingStickerBubbleStrokeColor) {
        this.incomingStickerBubbleStrokeColor = incomingStickerBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the incoming sticker bubble.
     *
     * @param incomingStickerBubbleBackgroundDrawable the background drawable for the incoming sticker bubble
     */
    public void setIncomingStickerBubbleBackgroundDrawable(Drawable incomingStickerBubbleBackgroundDrawable) {
        this.incomingStickerBubbleBackgroundDrawable = incomingStickerBubbleBackgroundDrawable;
    }

    /**
     * Sets the date style for the incoming sticker bubble.
     *
     * @param incomingStickerBubbleDateStyle the date style resource ID for the incoming sticker bubble
     */
    public void setIncomingStickerBubbleDateStyle(@StyleRes int incomingStickerBubbleDateStyle) {
        this.incomingStickerBubbleDateStyle = incomingStickerBubbleDateStyle;
    }

    /**
     * Sets the receipt style for the incoming sticker bubble.
     *
     * @param incomingStickerBubbleReceiptStyle the receipt style resource ID for the incoming sticker bubble
     */
    public void setIncomingStickerBubbleReceiptStyle(@StyleRes int incomingStickerBubbleReceiptStyle) {
        this.incomingStickerBubbleReceiptStyle = incomingStickerBubbleReceiptStyle;
    }

    /**
     * Sets the avatar style for the incoming sticker bubble.
     *
     * @param incomingStickerBubbleAvatarStyle the avatar style resource ID for the incoming sticker bubble
     */
    public void setIncomingStickerBubbleAvatarStyle(@StyleRes int incomingStickerBubbleAvatarStyle) {
        this.incomingStickerBubbleAvatarStyle = incomingStickerBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance for the incoming sticker bubble thread indicator.
     *
     * @param incomingStickerBubbleThreadIndicatorTextAppearance the text appearance resource ID for the incoming sticker bubble
     *                                                           thread indicator
     */
    public void setIncomingStickerBubbleThreadIndicatorTextAppearance(@StyleRes int incomingStickerBubbleThreadIndicatorTextAppearance) {
        this.incomingStickerBubbleThreadIndicatorTextAppearance = incomingStickerBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the incoming sticker bubble thread indicator.
     *
     * @param incomingStickerBubbleThreadIndicatorTextColor the text color for the incoming sticker bubble thread indicator
     */
    public void setIncomingStickerBubbleThreadIndicatorTextColor(@ColorInt int incomingStickerBubbleThreadIndicatorTextColor) {
        this.incomingStickerBubbleThreadIndicatorTextColor = incomingStickerBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the icon tint for the incoming sticker bubble thread indicator.
     *
     * @param incomingStickerBubbleThreadIndicatorIconTint the icon tint color for the incoming sticker bubble thread
     *                                                     indicator
     */
    public void setIncomingStickerBubbleThreadIndicatorIconTint(@ColorInt int incomingStickerBubbleThreadIndicatorIconTint) {
        this.incomingStickerBubbleThreadIndicatorIconTint = incomingStickerBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance for the incoming sticker bubble sender name.
     *
     * @param incomingStickerBubbleSenderNameTextAppearance the text appearance resource ID for the incoming sticker bubble
     *                                                      sender name
     */
    public void setIncomingStickerBubbleSenderNameTextAppearance(@StyleRes int incomingStickerBubbleSenderNameTextAppearance) {
    }

    /**
     * Sets the text color for the incoming sticker bubble sender name.
     *
     * @param incomingStickerBubbleSenderNameTextColor the text color for the incoming sticker bubble sender name
     */
    public void setIncomingStickerBubbleSenderNameTextColor(@ColorInt int incomingStickerBubbleSenderNameTextColor) {
        this.incomingStickerBubbleSenderNameTextColor = incomingStickerBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the incoming collaborative bubble.
     *
     * @param incomingCollaborativeBubbleBackgroundColor the background color for the incoming collaborative bubble
     */
    public void setIncomingCollaborativeBubbleBackgroundColor(@ColorInt int incomingCollaborativeBubbleBackgroundColor) {
        this.incomingCollaborativeBubbleBackgroundColor = incomingCollaborativeBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the incoming collaborative bubble.
     *
     * @param incomingCollaborativeBubbleCornerRadius the corner radius for the incoming collaborative bubble
     */
    public void setIncomingCollaborativeBubbleCornerRadius(@Dimension int incomingCollaborativeBubbleCornerRadius) {
        this.incomingCollaborativeBubbleCornerRadius = incomingCollaborativeBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the incoming collaborative bubble.
     *
     * @param incomingCollaborativeBubbleStrokeWidth the stroke width for the incoming collaborative bubble
     */
    public void setIncomingCollaborativeBubbleStrokeWidth(@Dimension int incomingCollaborativeBubbleStrokeWidth) {
        this.incomingCollaborativeBubbleStrokeWidth = incomingCollaborativeBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the incoming collaborative bubble.
     *
     * @param incomingCollaborativeBubbleStrokeColor the stroke color for the incoming collaborative bubble
     */
    public void setIncomingCollaborativeBubbleStrokeColor(@ColorInt int incomingCollaborativeBubbleStrokeColor) {
        this.incomingCollaborativeBubbleStrokeColor = incomingCollaborativeBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the incoming collaborative bubble.
     *
     * @param incomingCollaborativeBubbleBackgroundDrawable the background drawable for the incoming collaborative bubble
     */
    public void setIncomingCollaborativeBubbleBackgroundDrawable(Drawable incomingCollaborativeBubbleBackgroundDrawable) {
        this.incomingCollaborativeBubbleBackgroundDrawable = incomingCollaborativeBubbleBackgroundDrawable;
    }

    /**
     * Sets the date style for the incoming collaborative bubble.
     *
     * @param incomingCollaborativeBubbleDateStyle the date style resource ID for the incoming collaborative bubble
     */
    public void setIncomingCollaborativeBubbleDateStyle(@StyleRes int incomingCollaborativeBubbleDateStyle) {
        this.incomingCollaborativeBubbleDateStyle = incomingCollaborativeBubbleDateStyle;
    }

    /**
     * Sets the receipt style for the incoming collaborative bubble.
     *
     * @param incomingCollaborativeBubbleReceiptStyle the receipt style resource ID for the incoming collaborative
     *                                                bubble
     */
    public void setIncomingCollaborativeBubbleReceiptStyle(@StyleRes int incomingCollaborativeBubbleReceiptStyle) {
        this.incomingCollaborativeBubbleReceiptStyle = incomingCollaborativeBubbleReceiptStyle;
    }

    /**
     * Sets the avatar style for the incoming collaborative bubble.
     *
     * @param incomingCollaborativeBubbleAvatarStyle the avatar style resource ID for the incoming collaborative bubble
     */
    public void setIncomingCollaborativeBubbleAvatarStyle(@StyleRes int incomingCollaborativeBubbleAvatarStyle) {
        this.incomingCollaborativeBubbleAvatarStyle = incomingCollaborativeBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance for the incoming collaborative bubble thread
     * indicator.
     *
     * @param incomingCollaborativeBubbleThreadIndicatorTextAppearance the text appearance resource ID for the incoming collaborative
     *                                                                 bubble thread indicator
     */
    public void setIncomingCollaborativeBubbleThreadIndicatorTextAppearance(@StyleRes int incomingCollaborativeBubbleThreadIndicatorTextAppearance) {
        this.incomingCollaborativeBubbleThreadIndicatorTextAppearance = incomingCollaborativeBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the incoming collaborative bubble thread indicator.
     *
     * @param incomingCollaborativeBubbleThreadIndicatorTextColor the text color for the incoming collaborative bubble thread
     *                                                            indicator
     */
    public void setIncomingCollaborativeBubbleThreadIndicatorTextColor(@ColorInt int incomingCollaborativeBubbleThreadIndicatorTextColor) {
        this.incomingCollaborativeBubbleThreadIndicatorTextColor = incomingCollaborativeBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the icon tint for the incoming collaborative bubble thread indicator.
     *
     * @param incomingCollaborativeBubbleThreadIndicatorIconTint the icon tint color for the incoming collaborative bubble thread
     *                                                           indicator
     */
    public void setIncomingCollaborativeBubbleThreadIndicatorIconTint(@ColorInt int incomingCollaborativeBubbleThreadIndicatorIconTint) {
        this.incomingCollaborativeBubbleThreadIndicatorIconTint = incomingCollaborativeBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance for the incoming collaborative bubble sender name.
     *
     * @param incomingCollaborativeBubbleSenderNameTextAppearance the text appearance resource ID for the incoming collaborative
     *                                                            bubble sender name
     */
    public void setIncomingCollaborativeBubbleSenderNameTextAppearance(@StyleRes int incomingCollaborativeBubbleSenderNameTextAppearance) {
    }

    /**
     * Sets the text color for the incoming collaborative bubble sender name.
     *
     * @param incomingCollaborativeBubbleSenderNameTextColor the text color for the incoming collaborative bubble sender name
     */
    public void setIncomingCollaborativeBubbleSenderNameTextColor(@ColorInt int incomingCollaborativeBubbleSenderNameTextColor) {
        this.incomingCollaborativeBubbleSenderNameTextColor = incomingCollaborativeBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the incoming meet call bubble.
     *
     * @param incomingMeetCallBubbleBackgroundColor the background color for the incoming meet call bubble
     */
    public void setIncomingMeetCallBubbleBackgroundColor(@ColorInt int incomingMeetCallBubbleBackgroundColor) {
        this.incomingMeetCallBubbleBackgroundColor = incomingMeetCallBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the incoming meet call bubble.
     *
     * @param incomingMeetCallBubbleCornerRadius the corner radius for the incoming meet call bubble
     */
    public void setIncomingMeetCallBubbleCornerRadius(@Dimension int incomingMeetCallBubbleCornerRadius) {
        this.incomingMeetCallBubbleCornerRadius = incomingMeetCallBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the incoming meet call bubble.
     *
     * @param incomingMeetCallBubbleStrokeWidth the stroke width for the incoming meet call bubble
     */
    public void setIncomingMeetCallBubbleStrokeWidth(@Dimension int incomingMeetCallBubbleStrokeWidth) {
        this.incomingMeetCallBubbleStrokeWidth = incomingMeetCallBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the incoming meet call bubble.
     *
     * @param incomingMeetCallBubbleStrokeColor the stroke color for the incoming meet call bubble
     */
    public void setIncomingMeetCallBubbleStrokeColor(@ColorInt int incomingMeetCallBubbleStrokeColor) {
        this.incomingMeetCallBubbleStrokeColor = incomingMeetCallBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the incoming meet call bubble.
     *
     * @param incomingMeetCallBubbleBackgroundDrawable the background drawable for the incoming meet call bubble
     */
    public void setIncomingMeetCallBubbleBackgroundDrawable(Drawable incomingMeetCallBubbleBackgroundDrawable) {
        this.incomingMeetCallBubbleBackgroundDrawable = incomingMeetCallBubbleBackgroundDrawable;
    }

    /**
     * Sets the avatar style for the incoming meet call bubble.
     *
     * @param incomingMeetCallBubbleAvatarStyle the avatar style resource ID for the incoming meet call bubble
     */
    public void setIncomingMeetCallBubbleAvatarStyle(@StyleRes int incomingMeetCallBubbleAvatarStyle) {
        this.incomingMeetCallBubbleAvatarStyle = incomingMeetCallBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance for the incoming meet call bubble thread indicator.
     *
     * @param incomingMeetCallBubbleThreadIndicatorTextAppearance the text appearance resource ID for the incoming meet call bubble
     *                                                            thread indicator
     */
    public void setIncomingMeetCallBubbleThreadIndicatorTextAppearance(@StyleRes int incomingMeetCallBubbleThreadIndicatorTextAppearance) {
        this.incomingMeetCallBubbleThreadIndicatorTextAppearance = incomingMeetCallBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the incoming meet call bubble thread indicator.
     *
     * @param incomingMeetCallBubbleThreadIndicatorTextColor the text color for the incoming meet call bubble thread indicator
     */
    public void setIncomingMeetCallBubbleThreadIndicatorTextColor(@ColorInt int incomingMeetCallBubbleThreadIndicatorTextColor) {
        this.incomingMeetCallBubbleThreadIndicatorTextColor = incomingMeetCallBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the icon tint for the incoming meet call bubble thread indicator.
     *
     * @param incomingMeetCallBubbleThreadIndicatorIconTint the icon tint color for the incoming meet call bubble thread
     *                                                      indicator
     */
    public void setIncomingMeetCallBubbleThreadIndicatorIconTint(@ColorInt int incomingMeetCallBubbleThreadIndicatorIconTint) {
        this.incomingMeetCallBubbleThreadIndicatorIconTint = incomingMeetCallBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance for the incoming meet call bubble sender name.
     *
     * @param incomingMeetCallBubbleSenderNameTextAppearance the text appearance resource ID for the incoming meet call bubble
     *                                                       sender name
     */
    public void setIncomingMeetCallBubbleSenderNameTextAppearance(@StyleRes int incomingMeetCallBubbleSenderNameTextAppearance) {
        this.incomingMeetCallBubbleSenderNameTextAppearance = incomingMeetCallBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the incoming meet call bubble sender name.
     *
     * @param incomingMeetCallBubbleSenderNameTextColor the text color for the incoming meet call bubble sender name
     */
    public void setIncomingMeetCallBubbleSenderNameTextColor(@ColorInt int incomingMeetCallBubbleSenderNameTextColor) {
        this.incomingMeetCallBubbleSenderNameTextColor = incomingMeetCallBubbleSenderNameTextColor;
    }

    /**
     * Sets the style resource for the outgoing message bubble and applies the
     * corresponding attributes. This method configures the visual style for
     * outgoing message bubbles by extracting various properties from a provided
     * style resource. These properties include background color, corner radius,
     * stroke width, and additional styling for elements such as date, avatar,
     * thread indicators, and reaction styles. The method also sets specific bubble
     * styles for different message types like text, image, video, audio, and files.
     * A {@link TypedArray} is used to obtain the attributes from the style
     * resource, which are then applied to the outgoing message bubble. The method
     * ensures that the {@link TypedArray} is recycled after use to avoid resource
     * leaks.
     *
     * @param messageBubbleStyle The style resource ID for the outgoing message bubble.
     */
    public void setOutgoingMessageBubbleStyle(@StyleRes int messageBubbleStyle) {
        if (messageBubbleStyle != 0) {
            TypedArray typedArray = context.obtainStyledAttributes(messageBubbleStyle, R.styleable.CometChatMessageBubble);
            try {
                setOutgoingMessageBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatMessageBubble_cometchatMessageBubbleBackgroundColor,
                                                                            com.cometchat.chatuikit.CometChatTheme.getPrimaryColor(context)));
                setOutgoingMessageBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatMessageBubble_cometchatMessageBubbleCornerRadius,
                                                                                      0));
                setOutgoingMessageBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatMessageBubble_cometchatMessageBubbleStrokeWidth,
                                                                                     0));
                setOutgoingMessageBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatMessageBubble_cometchatMessageBubbleStrokeColor, 0));
                setOutgoingMessageBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatMessageBubble_cometchatMessageBubbleBackgroundDrawable));
                setOutgoingMessageBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMessageBubbleDateStyle, 0));
                setOutgoingMessageBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMessageBubbleMessageReceiptStyle,
                                                                              0));
                setOutgoingMessageBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMessageBubbleAvatarStyle,
                                                                             0));
                setOutgoingMessageBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMessageBubbleThreadIndicatorTextAppearance,
                                                                                               0));
                setOutgoingMessageBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatMessageBubble_cometchatMessageBubbleThreadIndicatorTextColor,
                                                                                     0));
                setOutgoingMessageBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatMessageBubble_cometchatMessageBubbleThreadIndicatorIconTint,
                                                                                    0));
                setOutgoingMessageBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMessageBubbleSenderNameTextAppearance,
                                                                                          0));
                setOutgoingMessageBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatMessageBubble_cometchatMessageBubbleSenderNameTextColor,
                                                                                0));
                setOutgoingDeleteBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatDeleteBubbleStyle, 0));
                setOutgoingTextBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatTextBubbleStyle, 0));
                setOutgoingImageBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatImageBubbleStyle, 0));
                setOutgoingAudioBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatAudioBubbleStyle, 0));
                setOutgoingVideoBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatVideoBubbleStyle, 0));
                setOutgoingFileBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatFileBubbleStyle, 0));
                setOutgoingFormBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatFormBubbleStyle, 0));
                setOutgoingSchedulerBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatSchedulerBubbleStyle, 0));
                setOutgoingPollBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatPollBubbleStyle, 0));
                setOutgoingStickerBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatStickerBubbleStyle, 0));
                setOutgoingCollaborativeBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatCollaborativeBubbleStyle,
                                                                             0));
                setOutgoingMessageBubbleReactionStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatReactionStyle, 0));
                setOutgoingMeetCallBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatMeetCallBubbleStyle, 0));
            } finally {
                typedArray.recycle();
            }
            notifyDataSetChanged();
        }
    }

    /**
     * Sets the background color for the outgoing message bubble.
     *
     * @param outgoingTextBubbleStyle The background color for the outgoing message bubble.
     */
    private void setOutgoingMessageBubbleBackgroundColor(@ColorInt int outgoingTextBubbleStyle) {
        this.outgoingMessageBubbleBackgroundColor = outgoingTextBubbleStyle;
    }

    /**
     * Sets the corner radius for the outgoing message bubble.
     *
     * @param outgoingTextBubbleStyle The corner radius for the outgoing message bubble.
     */
    private void setOutgoingMessageBubbleCornerRadius(@Dimension int outgoingTextBubbleStyle) {
        this.outgoingMessageBubbleCornerRadius = outgoingTextBubbleStyle;
    }

    /**
     * Sets the stroke width for the outgoing message bubble.
     *
     * @param outgoingTextBubbleStyle The stroke width for the outgoing message bubble.
     */
    private void setOutgoingMessageBubbleStrokeWidth(@Dimension int outgoingTextBubbleStyle) {
        this.outgoingMessageBubbleStrokeWidth = outgoingTextBubbleStyle;
    }

    /**
     * Sets the stroke color for the outgoing message bubble.
     *
     * @param outgoingTextBubbleStyle The stroke color for the outgoing message bubble.
     */
    private void setOutgoingMessageBubbleStrokeColor(@ColorInt int outgoingTextBubbleStyle) {
        this.outgoingMessageBubbleStrokeColor = outgoingTextBubbleStyle;
    }

    /**
     * Sets the background drawable for the outgoing message bubble.
     *
     * @param outgoingTextBubbleStyle The background drawable for the outgoing message bubble.
     */
    private void setOutgoingMessageBubbleBackgroundDrawable(Drawable outgoingTextBubbleStyle) {
        this.outgoingMessageBubbleBackgroundDrawable = outgoingTextBubbleStyle;
    }

    /**
     * Sets the style resource for the outgoing message bubble date.
     *
     * @param outgoingMessageBubbleDateStyle The style resource to apply.
     */
    public void setOutgoingMessageBubbleDateStyle(@StyleRes int outgoingMessageBubbleDateStyle) {
        this.outgoingMessageBubbleDateStyle = outgoingMessageBubbleDateStyle;
    }

    /**
     * Sets the style resource for the outgoing message bubble receipt.
     *
     * @param outgoingMessageBubbleReceiptStyle The style resource to apply.
     */
    public void setOutgoingMessageBubbleReceiptStyle(@StyleRes int outgoingMessageBubbleReceiptStyle) {
        this.outgoingMessageBubbleReceiptStyle = outgoingMessageBubbleReceiptStyle;
    }

    /**
     * Sets the style resource for the outgoing message bubble avatar.
     *
     * @param outgoingMessageBubbleAvatarStyle The style resource to apply.
     */
    public void setOutgoingMessageBubbleAvatarStyle(@StyleRes int outgoingMessageBubbleAvatarStyle) {
        this.outgoingMessageBubbleAvatarStyle = outgoingMessageBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance resource for the outgoing message bubble thread
     * indicator.
     *
     * @param outgoingMessageBubbleThreadIndicatorTextAppearance The text appearance resource to apply.
     */
    public void setOutgoingMessageBubbleThreadIndicatorTextAppearance(@StyleRes int outgoingMessageBubbleThreadIndicatorTextAppearance) {
        this.outgoingMessageBubbleThreadIndicatorTextAppearance = outgoingMessageBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the outgoing message bubble thread indicator.
     *
     * @param outgoingMessageBubbleThreadIndicatorTextColor The color to apply.
     */
    public void setOutgoingMessageBubbleThreadIndicatorTextColor(@ColorInt int outgoingMessageBubbleThreadIndicatorTextColor) {
        this.outgoingMessageBubbleThreadIndicatorTextColor = outgoingMessageBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the tint color for the outgoing message bubble thread indicator icon.
     *
     * @param outgoingMessageBubbleThreadIndicatorIconTint The color to apply.
     */
    public void setOutgoingMessageBubbleThreadIndicatorIconTint(@ColorInt int outgoingMessageBubbleThreadIndicatorIconTint) {
        this.outgoingMessageBubbleThreadIndicatorIconTint = outgoingMessageBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance resource for the outgoing message bubble sender
     * name.
     *
     * @param outgoingMessageBubbleSenderNameTextAppearance The text appearance resource to apply.
     */
    public void setOutgoingMessageBubbleSenderNameTextAppearance(@StyleRes int outgoingMessageBubbleSenderNameTextAppearance) {
        this.outgoingMessageBubbleSenderNameTextAppearance = outgoingMessageBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the outgoing message bubble sender name.
     *
     * @param outgoingMessageBubbleSenderNameTextColor The color to apply.
     */
    public void setOutgoingMessageBubbleSenderNameTextColor(@ColorInt int outgoingMessageBubbleSenderNameTextColor) {
        this.outgoingMessageBubbleSenderNameTextColor = outgoingMessageBubbleSenderNameTextColor;
    }

    /**
     * Sets the style resource for the outgoing delete message bubble and applies
     * the corresponding attributes. This method takes a style resource for the
     * outgoing delete message bubble and extracts various properties, such as
     * background color, corner radius, stroke width, and other visual aspects. It
     * applies these settings to customize the appearance of outgoing delete message
     * bubbles. The method uses a {@link TypedArray} to access the custom attributes
     * defined in the style resource and applies default values from the outgoing
     * message bubble style when necessary. After applying the properties, the
     * {@link TypedArray} is recycled to release resources.
     *
     * @param style The style resource ID for the outgoing delete message bubble.
     */
    public void setOutgoingDeleteBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatDeleteBubble);
        try {
            setOutgoingDeleteBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatDeleteBubble_cometchatDeleteBubbleBackgroundColor,
                                                                       outgoingMessageBubbleBackgroundColor));
            setOutgoingDeleteBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatDeleteBubble_cometchatDeleteBubbleCornerRadius,
                                                                                 outgoingMessageBubbleCornerRadius));
            setOutgoingDeleteBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatDeleteBubble_cometchatDeleteBubbleStrokeWidth,
                                                                                outgoingMessageBubbleStrokeWidth));
            setOutgoingDeleteBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatDeleteBubble_cometchatDeleteBubbleStrokeColor,
                                                                   outgoingMessageBubbleStrokeColor));
            setOutgoingDeleteBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatDeleteBubble_cometchatDeleteBubbleBackgroundDrawable));
            setOutgoingDeleteBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatDeleteBubble_cometchatDeleteBubbleDateStyle,
                                                                      outgoingMessageBubbleDateStyle));
            setOutgoingDeleteBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatDeleteBubble_cometchatDeleteBubbleAvatarStyle,
                                                                        outgoingMessageBubbleAvatarStyle));
            setOutgoingDeleteBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatDeleteBubble_cometchatDeleteBubbleSenderNameTextAppearance,
                                                                                     outgoingMessageBubbleSenderNameTextAppearance));
            setOutgoingDeleteBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatDeleteBubble_cometchatDeleteBubbleSenderNameTextColor,
                                                                           outgoingMessageBubbleSenderNameTextColor));
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Sets the style for outgoing text message bubbles and applies the
     * corresponding attributes. This method applies a given style resource to the
     * outgoing text message bubbles by extracting various visual attributes such as
     * background color, corner radius, stroke width, and text appearances. It also
     * configures elements like date, receipt indicators, avatar, and thread
     * indicators. The attributes are obtained from the {@link TypedArray}, and the
     * method ensures that the array is properly recycled after use.
     *
     * @param style The style resource ID for the outgoing text bubble.
     */
    public void setOutgoingTextBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatTextBubble);
        try {
            setOutgoingTextBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatTextBubble_cometchatTextBubbleBackgroundColor,
                                                                     outgoingMessageBubbleBackgroundColor));
            setOutgoingTextBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatTextBubble_cometchatTextBubbleCornerRadius,
                                                                               outgoingMessageBubbleCornerRadius));
            setOutgoingTextBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatTextBubble_cometchatTextBubbleStrokeWidth,
                                                                              outgoingMessageBubbleStrokeWidth));
            setOutgoingTextBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatTextBubble_cometchatTextBubbleStrokeColor,
                                                                 outgoingMessageBubbleStrokeColor));
            setOutgoingTextBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatTextBubble_cometchatTextBubbleBackgroundDrawable));
            setOutgoingTextBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatTextBubble_cometchatTextBubbleDateStyle,
                                                                    outgoingMessageBubbleDateStyle));
            setOutgoingTextBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatTextBubble_cometchatTextBubbleMessageReceiptStyle,
                                                                       outgoingMessageBubbleReceiptStyle));
            setOutgoingTextBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatTextBubble_cometchatTextBubbleAvatarStyle,
                                                                      outgoingMessageBubbleAvatarStyle));
            setOutgoingTextBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatTextBubble_cometchatTextBubbleThreadIndicatorTextAppearance,
                                                                                        incomingMessageBubbleThreadIndicatorTextAppearance));
            setOutgoingTextBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatTextBubble_cometchatTextBubbleThreadIndicatorTextColor,
                                                                              incomingMessageBubbleThreadIndicatorTextColor));
            setOutgoingTextBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatTextBubble_cometchatTextBubbleThreadIndicatorIconTint,
                                                                             incomingMessageBubbleThreadIndicatorTextColor));
            setOutgoingTextBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatTextBubble_cometchatTextBubbleSenderNameTextAppearance,
                                                                                   incomingMessageBubbleSenderNameTextAppearance));
            setOutgoingTextBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatTextBubble_cometchatTextBubbleSenderNameTextColor,
                                                                         incomingMessageBubbleSenderNameTextColor));
        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the style for outgoing image message bubbles and applies the
     * corresponding attributes. This method applies a given style resource to the
     * outgoing image message bubbles by extracting various visual attributes such
     * as background color, corner radius, stroke width, and text appearances. It
     * also configures elements like date, receipt indicators, avatar, and thread
     * indicators. The attributes are obtained from the {@link TypedArray}, and the
     * method ensures that the array is properly recycled after use.
     *
     * @param style The style resource ID for the outgoing image bubble.
     */
    public void setOutgoingImageBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatImageBubble);
        try {
            setOutgoingImageBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatImageBubble_cometchatImageBubbleBackgroundColor,
                                                                      outgoingMessageBubbleBackgroundColor));
            setOutgoingImageBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatImageBubble_cometchatImageBubbleCornerRadius,
                                                                                outgoingMessageBubbleCornerRadius));
            setOutgoingImageBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatImageBubble_cometchatImageBubbleStrokeWidth,
                                                                               outgoingMessageBubbleStrokeWidth));
            setOutgoingImageBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatImageBubble_cometchatImageBubbleStrokeColor,
                                                                  outgoingMessageBubbleStrokeColor));
            setOutgoingImageBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatImageBubble_cometchatImageBubbleBackgroundDrawable));
            setOutgoingImageBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatImageBubble_cometchatImageBubbleDateStyle,
                                                                     outgoingMessageBubbleDateStyle));
            setOutgoingImageBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatImageBubble_cometchatImageBubbleMessageReceiptStyle,
                                                                        outgoingMessageBubbleReceiptStyle));
            setOutgoingImageBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatImageBubble_cometchatImageBubbleAvatarStyle,
                                                                       outgoingMessageBubbleAvatarStyle));
            setOutgoingImageBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatImageBubble_cometchatImageBubbleThreadIndicatorTextAppearance,
                                                                                         outgoingMessageBubbleThreadIndicatorTextAppearance));
            setOutgoingImageBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatImageBubble_cometchatImageBubbleThreadIndicatorTextColor,
                                                                               outgoingMessageBubbleThreadIndicatorTextColor));
            setOutgoingImageBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatImageBubble_cometchatImageBubbleThreadIndicatorIconTint,
                                                                              outgoingMessageBubbleThreadIndicatorIconTint));
            setOutgoingImageBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatImageBubble_cometchatImageBubbleSenderNameTextAppearance,
                                                                                    outgoingMessageBubbleSenderNameTextAppearance));
            setOutgoingImageBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatImageBubble_cometchatImageBubbleSenderNameTextColor,
                                                                          outgoingMessageBubbleSenderNameTextColor));
        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the style for outgoing audio message bubbles and applies the
     * corresponding attributes. This method applies a style resource to the
     * outgoing audio message bubbles by extracting various visual attributes such
     * as background color, corner radius, stroke width, and text appearances. It
     * also configures elements like date, receipt indicators, avatar, and thread
     * indicators. The attributes are obtained from the {@link TypedArray}, and the
     * method ensures that the array is properly recycled after use.
     *
     * @param style The style resource ID for the outgoing audio bubble.
     */
    public void setOutgoingAudioBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatAudioBubble);
        try {
            setOutgoingAudioBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatAudioBubble_cometchatAudioBubbleBackgroundColor,
                                                                      outgoingMessageBubbleBackgroundColor));
            setOutgoingAudioBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatAudioBubble_cometchatAudioBubbleCornerRadius,
                                                                                outgoingMessageBubbleCornerRadius));
            setOutgoingAudioBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatAudioBubble_cometchatAudioBubbleStrokeWidth,
                                                                               outgoingMessageBubbleStrokeWidth));
            setOutgoingAudioBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatAudioBubble_cometchatAudioBubbleStrokeColor,
                                                                  outgoingMessageBubbleStrokeColor));
            setOutgoingAudioBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatAudioBubble_cometchatAudioBubbleBackgroundDrawable));
            setOutgoingAudioBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatAudioBubble_cometchatAudioBubbleDateStyle,
                                                                     outgoingMessageBubbleDateStyle));
            setOutgoingAudioBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatAudioBubble_cometchatAudioBubbleMessageReceiptStyle,
                                                                        outgoingMessageBubbleReceiptStyle));
            setOutgoingAudioBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatAudioBubble_cometchatAudioBubbleAvatarStyle,
                                                                       outgoingMessageBubbleAvatarStyle));
            setOutgoingAudioBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatAudioBubble_cometchatAudioBubbleThreadIndicatorTextAppearance,
                                                                                         outgoingMessageBubbleThreadIndicatorTextAppearance));
            setOutgoingAudioBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatAudioBubble_cometchatAudioBubbleThreadIndicatorTextColor,
                                                                               outgoingMessageBubbleThreadIndicatorTextColor));
            setOutgoingAudioBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatAudioBubble_cometchatAudioBubbleThreadIndicatorIconTint,
                                                                              outgoingMessageBubbleThreadIndicatorIconTint));
            setOutgoingAudioBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatAudioBubble_cometchatAudioBubbleSenderNameTextAppearance,
                                                                                    outgoingMessageBubbleSenderNameTextAppearance));
            setOutgoingAudioBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatAudioBubble_cometchatAudioBubbleSenderNameTextColor,
                                                                          outgoingMessageBubbleSenderNameTextColor));
        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the style for outgoing video message bubbles and applies the
     * corresponding attributes. This method applies a style resource to the
     * outgoing video message bubbles by extracting various visual attributes such
     * as background color, corner radius, stroke width, and text appearances. It
     * also configures elements like date, receipt indicators, avatar, and thread
     * indicators. The attributes are obtained from the {@link TypedArray}, and the
     * method ensures that the array is properly recycled after use.
     *
     * @param style The style resource ID for the outgoing video bubble.
     */
    public void setOutgoingVideoBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatVideoBubble);
        try {
            setOutgoingVideoBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatVideoBubble_cometchatVideoBubbleBackgroundColor,
                                                                      outgoingMessageBubbleBackgroundColor));
            setOutgoingVideoBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatVideoBubble_cometchatVideoBubbleCornerRadius,
                                                                                outgoingMessageBubbleCornerRadius));
            setOutgoingVideoBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatVideoBubble_cometchatVideoBubbleStrokeWidth,
                                                                               outgoingMessageBubbleStrokeWidth));
            setOutgoingVideoBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatVideoBubble_cometchatVideoBubbleStrokeColor,
                                                                  outgoingMessageBubbleStrokeColor));
            setOutgoingVideoBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatVideoBubble_cometchatVideoBubbleBackgroundDrawable));
            setOutgoingVideoBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatVideoBubble_cometchatVideoBubbleDateStyle,
                                                                     outgoingMessageBubbleDateStyle));
            setOutgoingVideoBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatVideoBubble_cometchatVideoBubbleMessageReceiptStyle,
                                                                        outgoingMessageBubbleReceiptStyle));
            setOutgoingVideoBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatVideoBubble_cometchatVideoBubbleAvatarStyle,
                                                                       outgoingMessageBubbleAvatarStyle));
            setOutgoingVideoBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatVideoBubble_cometchatVideoBubbleThreadIndicatorTextAppearance,
                                                                                         outgoingMessageBubbleThreadIndicatorTextAppearance));
            setOutgoingVideoBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatVideoBubble_cometchatVideoBubbleThreadIndicatorTextColor,
                                                                               outgoingMessageBubbleThreadIndicatorTextColor));
            setOutgoingVideoBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatVideoBubble_cometchatVideoBubbleThreadIndicatorIconTint,
                                                                              outgoingMessageBubbleThreadIndicatorIconTint));
            setOutgoingVideoBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatVideoBubble_cometchatVideoBubbleSenderNameTextAppearance,
                                                                                    outgoingMessageBubbleSenderNameTextAppearance));
            setOutgoingVideoBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatVideoBubble_cometchatVideoBubbleSenderNameTextColor,
                                                                          outgoingMessageBubbleSenderNameTextColor));

        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the style for outgoing file message bubbles and applies the
     * corresponding attributes. This method applies a style resource to the
     * outgoing file message bubbles by extracting various visual attributes such as
     * background color, corner radius, stroke width, and text appearances. It also
     * configures elements like date, receipt indicators, avatar, and thread
     * indicators. The attributes are obtained from the {@link TypedArray}, and the
     * method ensures that the array is properly recycled after use.
     *
     * @param style The style resource ID for the outgoing file bubble.
     */
    public void setOutgoingFileBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatFileBubble);
        try {
            setOutgoingFileBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatFileBubble_cometchatFileBubbleBackgroundColor,
                                                                     outgoingMessageBubbleBackgroundColor));
            setOutgoingFileBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatFileBubble_cometchatFileBubbleCornerRadius,
                                                                               outgoingMessageBubbleCornerRadius));
            setOutgoingFileBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatFileBubble_cometchatFileBubbleStrokeWidth,
                                                                              outgoingMessageBubbleStrokeWidth));
            setOutgoingFileBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatFileBubble_cometchatFileBubbleStrokeColor,
                                                                 outgoingMessageBubbleStrokeColor));
            setOutgoingFileBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatFileBubble_cometchatFileBubbleBackgroundDrawable));
            setOutgoingFileBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatFileBubble_cometchatFileBubbleDateStyle,
                                                                    outgoingMessageBubbleDateStyle));
            setOutgoingFileBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatFileBubble_cometchatFileBubbleMessageReceiptStyle,
                                                                       outgoingMessageBubbleReceiptStyle));
            setOutgoingFileBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatFileBubble_cometchatFileBubbleAvatarStyle,
                                                                      outgoingMessageBubbleAvatarStyle));
            setOutgoingFileBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatFileBubble_cometchatFileBubbleThreadIndicatorTextAppearance,
                                                                                        outgoingMessageBubbleThreadIndicatorTextAppearance));
            setOutgoingFileBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatFileBubble_cometchatFileBubbleThreadIndicatorTextColor,
                                                                              outgoingMessageBubbleThreadIndicatorTextColor));
            setOutgoingFileBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatFileBubble_cometchatFileBubbleThreadIndicatorIconTint,
                                                                             outgoingMessageBubbleThreadIndicatorIconTint));
            setOutgoingFileBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatFileBubble_cometchatFileBubbleSenderNameTextAppearance,
                                                                                   outgoingMessageBubbleSenderNameTextAppearance));
            setOutgoingFileBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatFileBubble_cometchatFileBubbleSenderNameTextColor,
                                                                         outgoingMessageBubbleSenderNameTextColor));
        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the style for outgoing form bubbles.
     *
     * @param formBubbleStyle The resource ID of the style to apply to outgoing form bubbles.
     */
    public void setOutgoingFormBubbleStyle(@StyleRes int formBubbleStyle) {
        TypedArray typedArray = context.obtainStyledAttributes(formBubbleStyle, R.styleable.CometChatFormBubble);
        try {
            setOutgoingFormBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatFormBubble_cometchatFormBubbleBackgroundColor,
                                                                     outgoingMessageBubbleBackgroundColor));
            setOutgoingFormBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatFormBubble_cometchatFormBubbleCornerRadius,
                                                                               outgoingMessageBubbleCornerRadius));
            setOutgoingFormBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatFormBubble_cometchatFormBubbleStrokeWidth,
                                                                              outgoingMessageBubbleStrokeWidth));
            setOutgoingFormBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatFormBubble_cometchatFormBubbleStrokeColor,
                                                                 outgoingMessageBubbleStrokeColor));
            setOutgoingFormBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatFormBubble_cometchatFormBubbleBackgroundDrawable));
            setOutgoingFormBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatFormBubble_cometchatFormBubbleDateStyle,
                                                                    outgoingMessageBubbleDateStyle));
            setOutgoingFormBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatFormBubble_cometchatFormBubbleMessageReceiptStyle,
                                                                       outgoingMessageBubbleReceiptStyle));
            setOutgoingFormBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatFormBubble_cometchatFormBubbleAvatarStyle,
                                                                      outgoingMessageBubbleAvatarStyle));
            setOutgoingFormBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatFormBubble_cometchatFormBubbleThreadIndicatorTextAppearance,
                                                                                        outgoingMessageBubbleThreadIndicatorTextAppearance));
            setOutgoingFormBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatFormBubble_cometchatFormBubbleThreadIndicatorTextColor,
                                                                              outgoingMessageBubbleThreadIndicatorTextColor));
            setOutgoingFormBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatFormBubble_cometchatFormBubbleThreadIndicatorIconTint,
                                                                             outgoingMessageBubbleThreadIndicatorIconTint));
            setOutgoingFormBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatFormBubble_cometchatFormBubbleSenderNameTextAppearance,
                                                                                   outgoingMessageBubbleSenderNameTextAppearance));
            setOutgoingFormBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatFormBubble_cometchatFormBubbleSenderNameTextColor,
                                                                         outgoingMessageBubbleSenderNameTextColor));
        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the style for outgoing scheduler bubbles.
     *
     * @param schedulerBubbleStyle The resource ID of the style to apply to outgoing scheduler
     *                             bubbles.
     */
    public void setOutgoingSchedulerBubbleStyle(@StyleRes int schedulerBubbleStyle) {
        TypedArray typedArray = context.obtainStyledAttributes(schedulerBubbleStyle, R.styleable.CometChatSchedulerBubble);
        try {
            setOutgoingSchedulerBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleBackgroundColor,
                                                                          outgoingMessageBubbleBackgroundColor));
            setOutgoingSchedulerBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleCornerRadius,
                                                                                    outgoingMessageBubbleCornerRadius));
            setOutgoingSchedulerBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleStrokeWidth,
                                                                                   outgoingMessageBubbleStrokeWidth));
            setOutgoingSchedulerBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleStrokeColor,
                                                                      outgoingMessageBubbleStrokeColor));
            setOutgoingSchedulerBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleBackgroundDrawable));
            setOutgoingSchedulerBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleDateStyle,
                                                                         outgoingMessageBubbleDateStyle));
            setOutgoingSchedulerBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleMessageReceiptStyle,
                                                                            outgoingMessageBubbleReceiptStyle));
            setOutgoingSchedulerBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleAvatarStyle,
                                                                           outgoingMessageBubbleAvatarStyle));
            setOutgoingSchedulerBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleThreadIndicatorTextAppearance,
                                                                                             outgoingMessageBubbleThreadIndicatorTextAppearance));
            setOutgoingSchedulerBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleThreadIndicatorTextColor,
                                                                                   outgoingMessageBubbleThreadIndicatorTextColor));
            setOutgoingSchedulerBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleThreadIndicatorIconTint,
                                                                                  outgoingMessageBubbleThreadIndicatorIconTint));
            setOutgoingSchedulerBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleSenderNameTextAppearance,
                                                                                        outgoingMessageBubbleSenderNameTextAppearance));
            setOutgoingSchedulerBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleSenderNameTextColor,
                                                                              outgoingMessageBubbleSenderNameTextColor));
        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the style for outgoing poll message bubbles and applies the
     * corresponding attributes. This method applies a style resource to the
     * outgoing poll message bubbles by extracting various visual attributes such as
     * background color, corner radius, stroke width, and text appearances. It also
     * configures elements like date, receipt indicators, avatar, and thread
     * indicators. The attributes are obtained from the {@link TypedArray}, and the
     * method ensures that the array is properly recycled after use.
     *
     * @param style The style resource ID for the outgoing poll bubble.
     */
    public void setOutgoingPollBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatPollBubble);
        try {
            setOutgoingPollBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleBackgroundColor,
                                                                     outgoingMessageBubbleBackgroundColor));
            setOutgoingPollBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatPollBubble_cometchatPollBubbleCornerRadius,
                                                                               outgoingMessageBubbleCornerRadius));
            setOutgoingPollBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatPollBubble_cometchatPollBubbleStrokeWidth,
                                                                              outgoingMessageBubbleStrokeWidth));
            setOutgoingPollBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleStrokeColor,
                                                                 outgoingMessageBubbleStrokeColor));
            setOutgoingPollBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatPollBubble_cometchatPollBubbleBackgroundDrawable));
            setOutgoingPollBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatPollBubble_cometchatPollBubbleDateStyle,
                                                                    outgoingMessageBubbleDateStyle));
            setOutgoingPollBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatPollBubble_cometchatPollBubbleMessageReceiptStyle,
                                                                       outgoingMessageBubbleReceiptStyle));
            setOutgoingPollBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatPollBubble_cometchatPollBubbleAvatarStyle,
                                                                      outgoingMessageBubbleAvatarStyle));
            setOutgoingPollBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatPollBubble_cometchatPollBubbleThreadIndicatorTextAppearance,
                                                                                        outgoingMessageBubbleThreadIndicatorTextAppearance));
            setOutgoingPollBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleThreadIndicatorTextColor,
                                                                              outgoingMessageBubbleThreadIndicatorTextColor));
            setOutgoingPollBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleThreadIndicatorIconTint,
                                                                             outgoingMessageBubbleThreadIndicatorIconTint));
            setOutgoingPollBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatPollBubble_cometchatPollBubbleSenderNameTextAppearance,
                                                                                   outgoingMessageBubbleSenderNameTextAppearance));
            setOutgoingPollBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleSenderNameTextColor,
                                                                         outgoingMessageBubbleSenderNameTextColor));
        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the style for outgoing sticker message bubbles and applies the
     * corresponding attributes. This method applies a style resource to the
     * outgoing sticker message bubbles by extracting various visual attributes such
     * as background color, corner radius, stroke width, and text appearances. It
     * also configures elements like date, receipt indicators, avatar, and thread
     * indicators. The attributes are obtained from the {@link TypedArray}, and the
     * method ensures that the array is properly recycled after use. The default
     * background color is set to transparent.
     *
     * @param style The style resource ID for the outgoing sticker bubble.
     */
    public void setOutgoingStickerBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatStickerBubble);
        try {
            setOutgoingStickerBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatStickerBubble_cometchatStickerBubbleBackgroundColor,
                                                                        Color.TRANSPARENT));
            setOutgoingStickerBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatStickerBubble_cometchatStickerBubbleCornerRadius,
                                                                                  outgoingMessageBubbleCornerRadius));
            setOutgoingStickerBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatStickerBubble_cometchatStickerBubbleStrokeWidth,
                                                                                 outgoingMessageBubbleStrokeWidth));
            setOutgoingStickerBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatStickerBubble_cometchatStickerBubbleStrokeColor,
                                                                    outgoingMessageBubbleStrokeColor));
            setOutgoingStickerBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatStickerBubble_cometchatStickerBubbleBackgroundDrawable));
            setOutgoingStickerBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatStickerBubble_cometchatStickerBubbleDateStyle,
                                                                       outgoingMessageBubbleDateStyle));
            setOutgoingStickerBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatStickerBubble_cometchatStickerBubbleMessageReceiptStyle,
                                                                          outgoingMessageBubbleReceiptStyle));
            setOutgoingStickerBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatStickerBubble_cometchatStickerBubbleAvatarStyle,
                                                                         outgoingMessageBubbleAvatarStyle));
            setOutgoingStickerBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatStickerBubble_cometchatStickerBubbleThreadIndicatorTextAppearance,
                                                                                           outgoingMessageBubbleThreadIndicatorTextAppearance));
            setOutgoingStickerBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatStickerBubble_cometchatStickerBubbleThreadIndicatorTextColor,
                                                                                 outgoingMessageBubbleThreadIndicatorTextColor));
            setOutgoingStickerBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatStickerBubble_cometchatStickerBubbleThreadIndicatorIconTint,
                                                                                outgoingMessageBubbleThreadIndicatorIconTint));
            setOutgoingStickerBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatStickerBubble_cometchatStickerBubbleSenderNameTextAppearance,
                                                                                      outgoingMessageBubbleSenderNameTextAppearance));
            setOutgoingStickerBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatStickerBubble_cometchatStickerBubbleSenderNameTextColor,
                                                                            outgoingMessageBubbleSenderNameTextColor));
        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the style for outgoing collaborative message bubbles and applies the
     * corresponding attributes. This method applies a style resource to the
     * outgoing collaborative message bubbles by extracting various visual
     * attributes such as background color, corner radius, stroke width, and text
     * appearances. It also configures elements like date, receipt indicators,
     * avatar, and thread indicators. The attributes are obtained from the
     * {@link TypedArray}, and the method ensures that the array is properly
     * recycled after use.
     *
     * @param style The style resource ID for the outgoing collaborative bubble.
     */
    public void setOutgoingCollaborativeBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatCollaborativeBubble);
        try {
            setOutgoingCollaborativeBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleBackgroundColor,
                                                                              outgoingMessageBubbleBackgroundColor));
            setOutgoingCollaborativeBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleCornerRadius,
                                                                                        outgoingMessageBubbleCornerRadius));
            setOutgoingCollaborativeBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleStrokeWidth,
                                                                                       outgoingMessageBubbleStrokeWidth));
            setOutgoingCollaborativeBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleStrokeColor,
                                                                          outgoingMessageBubbleStrokeColor));
            setOutgoingCollaborativeBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleBackgroundDrawable));
            setOutgoingCollaborativeBubbleDateStyle(typedArray.getResourceId(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleDateStyle,
                                                                             outgoingMessageBubbleDateStyle));
            setOutgoingCollaborativeBubbleReceiptStyle(typedArray.getResourceId(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleMessageReceiptStyle,
                                                                                outgoingMessageBubbleReceiptStyle));
            setOutgoingCollaborativeBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleAvatarStyle,
                                                                               outgoingMessageBubbleAvatarStyle));
            setOutgoingCollaborativeBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleThreadIndicatorTextAppearance,
                                                                                                 outgoingMessageBubbleThreadIndicatorTextAppearance));
            setOutgoingCollaborativeBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleThreadIndicatorTextColor,
                                                                                       outgoingMessageBubbleThreadIndicatorTextColor));
            setOutgoingCollaborativeBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleThreadIndicatorIconTint,
                                                                                      outgoingMessageBubbleThreadIndicatorIconTint));
            setOutgoingCollaborativeBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleSenderNameTextAppearance,
                                                                                            outgoingMessageBubbleSenderNameTextAppearance));
            setOutgoingCollaborativeBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleSenderNameTextColor,
                                                                                  outgoingMessageBubbleSenderNameTextColor));
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Sets the style resource for outgoing message bubble reactions. This method
     * allows customization of the visual appearance of reactions associated with
     * outgoing message bubbles by providing a style resource ID. The style can
     * define attributes such as colors, padding, and other UI elements specific to
     * outgoing message reactions.
     *
     * @param style The style resource ID for outgoing message bubble reactions.
     */
    public void setOutgoingMessageBubbleReactionStyle(@StyleRes int style) {
        this.outgoingMessageBubbleReactionStyle = style;
    }

    /**
     * Sets the style for outgoing meet call bubbles.
     *
     * @param style The resource ID of the style to apply to outgoing meet call
     *              bubbles.
     */
    public void setOutgoingMeetCallBubbleStyle(@StyleRes int style) {
        TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatMeetCallBubble);
        try {
            setOutgoingMeetCallBubbleBackgroundColor(typedArray.getColor(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleBackgroundColor,
                                                                         outgoingMessageBubbleBackgroundColor));
            setOutgoingMeetCallBubbleCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleCornerRadius,
                                                                                   outgoingMessageBubbleCornerRadius));
            setOutgoingMeetCallBubbleStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleStrokeWidth,
                                                                                  outgoingMessageBubbleStrokeWidth));
            setOutgoingMeetCallBubbleStrokeColor(typedArray.getColor(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleStrokeColor,
                                                                     outgoingMessageBubbleStrokeColor));
            setOutgoingMeetCallBubbleBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleBackgroundDrawable));
            setOutgoingMeetCallBubbleAvatarStyle(typedArray.getResourceId(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleAvatarStyle,
                                                                          outgoingMessageBubbleAvatarStyle));
            setOutgoingMeetCallBubbleThreadIndicatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleThreadIndicatorTextAppearance,
                                                                                            outgoingMessageBubbleThreadIndicatorTextAppearance));
            setOutgoingMeetCallBubbleThreadIndicatorTextColor(typedArray.getColor(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleThreadIndicatorTextColor,
                                                                                  outgoingMessageBubbleThreadIndicatorTextColor));
            setOutgoingMeetCallBubbleThreadIndicatorIconTint(typedArray.getColor(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleThreadIndicatorIconTint,
                                                                                 outgoingMessageBubbleThreadIndicatorIconTint));
            setOutgoingMeetCallBubbleSenderNameTextAppearance(typedArray.getResourceId(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleSenderNameTextAppearance,
                                                                                       outgoingMessageBubbleSenderNameTextAppearance));
            setOutgoingMeetCallBubbleSenderNameTextColor(typedArray.getColor(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleSenderNameTextColor,
                                                                             outgoingMessageBubbleSenderNameTextColor));
        } finally {
            typedArray.recycle();
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the background color for the outgoing delete bubble.
     *
     * @param outgoingDeleteBubbleBackgroundColor the background color for the outgoing delete bubble
     */
    public void setOutgoingDeleteBubbleBackgroundColor(@ColorInt int outgoingDeleteBubbleBackgroundColor) {
        this.outgoingDeleteBubbleBackgroundColor = outgoingDeleteBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the outgoing delete bubble.
     *
     * @param outgoingDeleteBubbleCornerRadius the corner radius for the outgoing delete bubble
     */
    public void setOutgoingDeleteBubbleCornerRadius(@Dimension int outgoingDeleteBubbleCornerRadius) {
        this.outgoingDeleteBubbleCornerRadius = outgoingDeleteBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the outgoing delete bubble.
     *
     * @param outgoingDeleteBubbleStrokeWidth the stroke width for the outgoing delete bubble
     */
    public void setOutgoingDeleteBubbleStrokeWidth(@Dimension int outgoingDeleteBubbleStrokeWidth) {
        this.outgoingDeleteBubbleStrokeWidth = outgoingDeleteBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the outgoing delete bubble.
     *
     * @param outgoingDeleteBubbleStrokeColor the stroke color for the outgoing delete bubble
     */
    public void setOutgoingDeleteBubbleStrokeColor(@ColorInt int outgoingDeleteBubbleStrokeColor) {
        this.outgoingDeleteBubbleStrokeColor = outgoingDeleteBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the outgoing delete bubble.
     *
     * @param outgoingDeleteBubbleBackgroundDrawable the background drawable for the outgoing delete bubble
     */
    public void setOutgoingDeleteBubbleBackgroundDrawable(Drawable outgoingDeleteBubbleBackgroundDrawable) {
        this.outgoingDeleteBubbleBackgroundDrawable = outgoingDeleteBubbleBackgroundDrawable;
    }

    /**
     * Sets the date style for the outgoing delete bubble.
     *
     * @param outgoingDeleteBubbleDateStyle the date style resource ID for the outgoing delete bubble
     */
    public void setOutgoingDeleteBubbleDateStyle(@StyleRes int outgoingDeleteBubbleDateStyle) {
        this.outgoingDeleteBubbleDateStyle = outgoingDeleteBubbleDateStyle;
    }

    /**
     * Sets the avatar style for the outgoing delete bubble.
     *
     * @param outgoingDeleteBubbleAvatarStyle the avatar style resource ID for the outgoing delete bubble
     */
    public void setOutgoingDeleteBubbleAvatarStyle(@StyleRes int outgoingDeleteBubbleAvatarStyle) {
        this.outgoingDeleteBubbleAvatarStyle = outgoingDeleteBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance for the outgoing delete bubble sender name.
     *
     * @param outgoingDeleteBubbleSenderNameTextAppearance the text appearance resource ID for the outgoing delete bubble
     *                                                     sender name
     */
    public void setOutgoingDeleteBubbleSenderNameTextAppearance(@StyleRes int outgoingDeleteBubbleSenderNameTextAppearance) {
        this.outgoingDeleteBubbleSenderNameTextAppearance = outgoingDeleteBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the outgoing delete bubble sender name.
     *
     * @param outgoingDeleteBubbleSenderNameTextColor the text color for the outgoing delete bubble sender name
     */
    public void setOutgoingDeleteBubbleSenderNameTextColor(@ColorInt int outgoingDeleteBubbleSenderNameTextColor) {
        this.outgoingDeleteBubbleSenderNameTextColor = outgoingDeleteBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the outgoing text bubble.
     *
     * @param outgoingTextBubbleBackgroundColor The background color for the outgoing text bubble.
     */
    private void setOutgoingTextBubbleBackgroundColor(@ColorInt int outgoingTextBubbleBackgroundColor) {
        this.outgoingTextBubbleBackgroundColor = outgoingTextBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the outgoing text bubble.
     *
     * @param outgoingTextBubbleCornerRadius The corner radius for the outgoing text bubble.
     */
    private void setOutgoingTextBubbleCornerRadius(@Dimension int outgoingTextBubbleCornerRadius) {
        this.outgoingTextBubbleCornerRadius = outgoingTextBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the outgoing text bubble.
     *
     * @param outgoingTextBubbleStrokeWidth The stroke width for the outgoing text bubble.
     */
    private void setOutgoingTextBubbleStrokeWidth(@Dimension int outgoingTextBubbleStrokeWidth) {
        this.outgoingTextBubbleStrokeWidth = outgoingTextBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the outgoing text bubble.
     *
     * @param outgoingTextBubbleStrokeColor The stroke color for the outgoing text bubble.
     */
    private void setOutgoingTextBubbleStrokeColor(@ColorInt int outgoingTextBubbleStrokeColor) {
        this.outgoingTextBubbleStrokeColor = outgoingTextBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the outgoing text bubble.
     *
     * @param outgoingTextBubbleBackgroundDrawable The background drawable for the outgoing text bubble.
     */
    private void setOutgoingTextBubbleBackgroundDrawable(Drawable outgoingTextBubbleBackgroundDrawable) {
        this.outgoingTextBubbleBackgroundDrawable = outgoingTextBubbleBackgroundDrawable;
    }

    /**
     * Sets the style resource for the outgoing text bubble date.
     *
     * @param outgoingTextBubbleDateStyle The style resource to apply.
     */
    public void setOutgoingTextBubbleDateStyle(@StyleRes int outgoingTextBubbleDateStyle) {
        this.outgoingTextBubbleDateStyle = outgoingTextBubbleDateStyle;
    }

    /**
     * Sets the style resource for the outgoing text bubble receipt.
     *
     * @param outgoingTextBubbleReceiptStyle The style resource to apply.
     */
    public void setOutgoingTextBubbleReceiptStyle(@StyleRes int outgoingTextBubbleReceiptStyle) {
        this.outgoingTextBubbleReceiptStyle = outgoingTextBubbleReceiptStyle;
    }

    /**
     * Sets the style resource for the outgoing text bubble avatar.
     *
     * @param outgoingTextBubbleAvatarStyle The style resource to apply.
     */
    public void setOutgoingTextBubbleAvatarStyle(@StyleRes int outgoingTextBubbleAvatarStyle) {
        this.outgoingTextBubbleAvatarStyle = outgoingTextBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance resource for the outgoing text bubble thread
     * indicator.
     *
     * @param outgoingTextBubbleThreadIndicatorTextAppearance The text appearance resource to apply.
     */
    public void setOutgoingTextBubbleThreadIndicatorTextAppearance(@StyleRes int outgoingTextBubbleThreadIndicatorTextAppearance) {
        this.outgoingTextBubbleThreadIndicatorTextAppearance = outgoingTextBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the outgoing text bubble thread indicator.
     *
     * @param outgoingTextBubbleThreadIndicatorTextColor The color to apply.
     */
    public void setOutgoingTextBubbleThreadIndicatorTextColor(@ColorInt int outgoingTextBubbleThreadIndicatorTextColor) {
        this.outgoingTextBubbleThreadIndicatorTextColor = outgoingTextBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the tint color for the outgoing text bubble thread indicator icon.
     *
     * @param outgoingTextBubbleThreadIndicatorIconTint The color to apply.
     */
    public void setOutgoingTextBubbleThreadIndicatorIconTint(@ColorInt int outgoingTextBubbleThreadIndicatorIconTint) {
        this.outgoingTextBubbleThreadIndicatorIconTint = outgoingTextBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance resource for the outgoing text bubble sender name.
     *
     * @param outgoingTextBubbleSenderNameTextAppearance The text appearance resource to apply.
     */
    public void setOutgoingTextBubbleSenderNameTextAppearance(@StyleRes int outgoingTextBubbleSenderNameTextAppearance) {
        this.outgoingTextBubbleSenderNameTextAppearance = outgoingTextBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the outgoing text bubble sender name.
     *
     * @param outgoingTextBubbleSenderNameTextColor The color to apply.
     */
    public void setOutgoingTextBubbleSenderNameTextColor(@ColorInt int outgoingTextBubbleSenderNameTextColor) {
        this.outgoingTextBubbleSenderNameTextColor = outgoingTextBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the outgoing image bubble.
     *
     * @param outgoingImageBubbleBackgroundColor The background color for the outgoing image bubble.
     */
    private void setOutgoingImageBubbleBackgroundColor(@ColorInt int outgoingImageBubbleBackgroundColor) {
        this.outgoingImageBubbleBackgroundColor = outgoingImageBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the outgoing image bubble.
     *
     * @param outgoingImageBubbleCornerRadius The corner radius for the outgoing image bubble.
     */
    private void setOutgoingImageBubbleCornerRadius(@Dimension int outgoingImageBubbleCornerRadius) {
        this.outgoingImageBubbleCornerRadius = outgoingImageBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the outgoing image bubble.
     *
     * @param outgoingImageBubbleStrokeWidth The stroke width for the outgoing image bubble.
     */
    private void setOutgoingImageBubbleStrokeWidth(@Dimension int outgoingImageBubbleStrokeWidth) {
        this.outgoingImageBubbleStrokeWidth = outgoingImageBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the outgoing image bubble.
     *
     * @param outgoingImageBubbleStrokeColor The stroke color for the outgoing image bubble.
     */
    private void setOutgoingImageBubbleStrokeColor(@ColorInt int outgoingImageBubbleStrokeColor) {
        this.outgoingImageBubbleStrokeColor = outgoingImageBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the outgoing image bubble.
     *
     * @param outgoingImageBubbleBackgroundDrawable The background drawable for the outgoing image bubble.
     */
    private void setOutgoingImageBubbleBackgroundDrawable(Drawable outgoingImageBubbleBackgroundDrawable) {
        this.outgoingImageBubbleBackgroundDrawable = outgoingImageBubbleBackgroundDrawable;
    }

    /**
     * Sets the style resource for the outgoing image bubble date.
     *
     * @param outgoingImageBubbleDateStyle The style resource to apply.
     */
    public void setOutgoingImageBubbleDateStyle(@StyleRes int outgoingImageBubbleDateStyle) {
        this.outgoingImageBubbleDateStyle = outgoingImageBubbleDateStyle;
    }

    /**
     * Sets the style resource for the outgoing image bubble receipt.
     *
     * @param outgoingImageBubbleReceiptStyle The style resource to apply.
     */
    public void setOutgoingImageBubbleReceiptStyle(@StyleRes int outgoingImageBubbleReceiptStyle) {
        this.outgoingImageBubbleReceiptStyle = outgoingImageBubbleReceiptStyle;
    }

    /**
     * Sets the style resource for the outgoing image bubble avatar.
     *
     * @param outgoingImageBubbleAvatarStyle The style resource to apply.
     */
    public void setOutgoingImageBubbleAvatarStyle(@StyleRes int outgoingImageBubbleAvatarStyle) {
        this.outgoingImageBubbleAvatarStyle = outgoingImageBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance resource for the outgoing image bubble thread
     * indicator.
     *
     * @param outgoingImageBubbleThreadIndicatorTextAppearance The text appearance resource to apply.
     */
    public void setOutgoingImageBubbleThreadIndicatorTextAppearance(@StyleRes int outgoingImageBubbleThreadIndicatorTextAppearance) {
        this.outgoingImageBubbleThreadIndicatorTextAppearance = outgoingImageBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the outgoing image bubble thread indicator.
     *
     * @param outgoingImageBubbleThreadIndicatorTextColor The color to apply.
     */
    public void setOutgoingImageBubbleThreadIndicatorTextColor(@ColorInt int outgoingImageBubbleThreadIndicatorTextColor) {
        this.outgoingImageBubbleThreadIndicatorTextColor = outgoingImageBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the tint color for the outgoing image bubble thread indicator icon.
     *
     * @param outgoingImageBubbleThreadIndicatorIconTint The color to apply.
     */
    public void setOutgoingImageBubbleThreadIndicatorIconTint(@ColorInt int outgoingImageBubbleThreadIndicatorIconTint) {
        this.outgoingImageBubbleThreadIndicatorIconTint = outgoingImageBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance resource for the outgoing image bubble sender name.
     *
     * @param outgoingImageBubbleSenderNameTextAppearance The text appearance resource to apply.
     */
    public void setOutgoingImageBubbleSenderNameTextAppearance(@StyleRes int outgoingImageBubbleSenderNameTextAppearance) {
        this.outgoingImageBubbleSenderNameTextAppearance = outgoingImageBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the outgoing image bubble sender name.
     *
     * @param outgoingImageBubbleSenderNameTextColor The color to apply.
     */
    public void setOutgoingImageBubbleSenderNameTextColor(@ColorInt int outgoingImageBubbleSenderNameTextColor) {
        this.outgoingImageBubbleSenderNameTextColor = outgoingImageBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the outgoing audio bubble.
     *
     * @param outgoingAudioBubbleBackgroundColor The background color for the outgoing audio bubble.
     */
    private void setOutgoingAudioBubbleBackgroundColor(@ColorInt int outgoingAudioBubbleBackgroundColor) {
        this.outgoingAudioBubbleBackgroundColor = outgoingAudioBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the outgoing audio bubble.
     *
     * @param outgoingAudioBubbleCornerRadius The corner radius for the outgoing audio bubble.
     */
    private void setOutgoingAudioBubbleCornerRadius(@Dimension int outgoingAudioBubbleCornerRadius) {
        this.outgoingAudioBubbleCornerRadius = outgoingAudioBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the outgoing audio bubble.
     *
     * @param outgoingAudioBubbleStrokeWidth The stroke width for the outgoing audio bubble.
     */
    private void setOutgoingAudioBubbleStrokeWidth(@Dimension int outgoingAudioBubbleStrokeWidth) {
        this.outgoingAudioBubbleStrokeWidth = outgoingAudioBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the outgoing audio bubble.
     *
     * @param outgoingAudioBubbleStrokeColor The stroke color for the outgoing audio bubble.
     */
    private void setOutgoingAudioBubbleStrokeColor(@ColorInt int outgoingAudioBubbleStrokeColor) {
        this.outgoingAudioBubbleStrokeColor = outgoingAudioBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the outgoing audio bubble.
     *
     * @param outgoingAudioBubbleBackgroundDrawable The background drawable for the outgoing audio bubble.
     */
    private void setOutgoingAudioBubbleBackgroundDrawable(Drawable outgoingAudioBubbleBackgroundDrawable) {
        this.outgoingAudioBubbleBackgroundDrawable = outgoingAudioBubbleBackgroundDrawable;
    }

    /**
     * Sets the style resource for the outgoing audio bubble date.
     *
     * @param outgoingAudioBubbleDateStyle The style resource to apply.
     */
    public void setOutgoingAudioBubbleDateStyle(@StyleRes int outgoingAudioBubbleDateStyle) {
        this.outgoingAudioBubbleDateStyle = outgoingAudioBubbleDateStyle;
    }

    /**
     * Sets the style resource for the outgoing audio bubble receipt.
     *
     * @param outgoingAudioBubbleReceiptStyle The style resource to apply.
     */
    public void setOutgoingAudioBubbleReceiptStyle(@StyleRes int outgoingAudioBubbleReceiptStyle) {
        this.outgoingAudioBubbleReceiptStyle = outgoingAudioBubbleReceiptStyle;
    }

    /**
     * Sets the style resource for the outgoing audio bubble avatar.
     *
     * @param outgoingAudioBubbleAvatarStyle The style resource to apply.
     */
    public void setOutgoingAudioBubbleAvatarStyle(@StyleRes int outgoingAudioBubbleAvatarStyle) {
        this.outgoingAudioBubbleAvatarStyle = outgoingAudioBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance for the thread indicator in the outgoing audio
     * bubble.
     *
     * @param outgoingAudioBubbleThreadIndicatorTextAppearance the style resource ID for the text appearance
     */
    public void setOutgoingAudioBubbleThreadIndicatorTextAppearance(@StyleRes int outgoingAudioBubbleThreadIndicatorTextAppearance) {
        this.outgoingAudioBubbleThreadIndicatorTextAppearance = outgoingAudioBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the thread indicator in the outgoing audio bubble.
     *
     * @param outgoingAudioBubbleThreadIndicatorTextColor the color to set for the thread indicator text
     */
    public void setOutgoingAudioBubbleThreadIndicatorTextColor(@ColorInt int outgoingAudioBubbleThreadIndicatorTextColor) {
        this.outgoingAudioBubbleThreadIndicatorTextColor = outgoingAudioBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the icon tint for the thread indicator in the outgoing audio bubble.
     *
     * @param outgoingAudioBubbleThreadIndicatorIconTint the color to set for the thread indicator icon tint
     */
    public void setOutgoingAudioBubbleThreadIndicatorIconTint(@ColorInt int outgoingAudioBubbleThreadIndicatorIconTint) {
        this.outgoingAudioBubbleThreadIndicatorIconTint = outgoingAudioBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance for the sender name in the outgoing audio bubble.
     *
     * @param outgoingAudioBubbleSenderNameTextAppearance the style resource ID for the sender name text appearance
     */
    public void setOutgoingAudioBubbleSenderNameTextAppearance(@StyleRes int outgoingAudioBubbleSenderNameTextAppearance) {
        this.outgoingAudioBubbleSenderNameTextAppearance = outgoingAudioBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the sender name in the outgoing audio bubble.
     *
     * @param outgoingAudioBubbleSenderNameTextColor the color to set for the sender name
     */
    public void setOutgoingAudioBubbleSenderNameTextColor(@ColorInt int outgoingAudioBubbleSenderNameTextColor) {
        this.outgoingAudioBubbleSenderNameTextColor = outgoingAudioBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the outgoing video bubble.
     *
     * @param outgoingVideoBubbleBackgroundColor The color to set as the background.
     */
    private void setOutgoingVideoBubbleBackgroundColor(@ColorInt int outgoingVideoBubbleBackgroundColor) {
        this.outgoingVideoBubbleBackgroundColor = outgoingVideoBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the outgoing video bubble.
     *
     * @param outgoingVideoBubbleCornerRadius The corner radius in pixels.
     */
    private void setOutgoingVideoBubbleCornerRadius(@Dimension int outgoingVideoBubbleCornerRadius) {
        this.outgoingVideoBubbleCornerRadius = outgoingVideoBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the outgoing video bubble.
     *
     * @param outgoingVideoBubbleStrokeWidth The stroke width in pixels.
     */
    private void setOutgoingVideoBubbleStrokeWidth(@Dimension int outgoingVideoBubbleStrokeWidth) {
        this.outgoingVideoBubbleStrokeWidth = outgoingVideoBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the outgoing video bubble.
     *
     * @param outgoingVideoBubbleStrokeColor The color to set as the stroke.
     */
    private void setOutgoingVideoBubbleStrokeColor(@ColorInt int outgoingVideoBubbleStrokeColor) {
        this.outgoingVideoBubbleStrokeColor = outgoingVideoBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the outgoing video bubble.
     *
     * @param outgoingVideoBubbleBackgroundDrawable The drawable to set as the background.
     */
    private void setOutgoingVideoBubbleBackgroundDrawable(Drawable outgoingVideoBubbleBackgroundDrawable) {
        this.outgoingVideoBubbleBackgroundDrawable = outgoingVideoBubbleBackgroundDrawable;
    }

    /**
     * Sets the style resource for the outgoing video bubble date.
     *
     * @param outgoingVideoBubbleDateStyle The style resource to apply.
     */
    public void setOutgoingVideoBubbleDateStyle(@StyleRes int outgoingVideoBubbleDateStyle) {
        this.outgoingVideoBubbleDateStyle = outgoingVideoBubbleDateStyle;
    }

    /**
     * Sets the style resource for the outgoing video bubble receipt.
     *
     * @param outgoingVideoBubbleReceiptStyle The style resource to apply.
     */
    public void setOutgoingVideoBubbleReceiptStyle(@StyleRes int outgoingVideoBubbleReceiptStyle) {
        this.outgoingVideoBubbleReceiptStyle = outgoingVideoBubbleReceiptStyle;
    }

    /**
     * Sets the style resource for the outgoing video bubble avatar.
     *
     * @param outgoingVideoBubbleAvatarStyle The style resource to apply.
     */
    public void setOutgoingVideoBubbleAvatarStyle(@StyleRes int outgoingVideoBubbleAvatarStyle) {
        this.outgoingVideoBubbleAvatarStyle = outgoingVideoBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance for the thread indicator in the outgoing video
     * bubble.
     *
     * @param outgoingVideoBubbleThreadIndicatorTextAppearance the style resource ID for the text appearance
     */
    public void setOutgoingVideoBubbleThreadIndicatorTextAppearance(@StyleRes int outgoingVideoBubbleThreadIndicatorTextAppearance) {
        this.outgoingVideoBubbleThreadIndicatorTextAppearance = outgoingVideoBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the thread indicator in the outgoing video bubble.
     *
     * @param outgoingVideoBubbleThreadIndicatorTextColor the color to set for the thread indicator text
     */
    public void setOutgoingVideoBubbleThreadIndicatorTextColor(@ColorInt int outgoingVideoBubbleThreadIndicatorTextColor) {
        this.outgoingVideoBubbleThreadIndicatorTextColor = outgoingVideoBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the icon tint for the thread indicator in the outgoing video bubble.
     *
     * @param outgoingVideoBubbleThreadIndicatorIconTint the color to set for the thread indicator icon tint
     */
    public void setOutgoingVideoBubbleThreadIndicatorIconTint(@ColorInt int outgoingVideoBubbleThreadIndicatorIconTint) {
        this.outgoingVideoBubbleThreadIndicatorIconTint = outgoingVideoBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance for the sender name in the outgoing video bubble.
     *
     * @param outgoingVideoBubbleSenderNameTextAppearance the style resource ID for the sender name text appearance
     */
    public void setOutgoingVideoBubbleSenderNameTextAppearance(@StyleRes int outgoingVideoBubbleSenderNameTextAppearance) {
        this.outgoingVideoBubbleSenderNameTextAppearance = outgoingVideoBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the sender name in the outgoing video bubble.
     *
     * @param outgoingVideoBubbleSenderNameTextColor the color to set for the sender name
     */
    public void setOutgoingVideoBubbleSenderNameTextColor(@ColorInt int outgoingVideoBubbleSenderNameTextColor) {
        this.outgoingVideoBubbleSenderNameTextColor = outgoingVideoBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the outgoing file bubble.
     *
     * @param outgoingFileBubbleBackgroundColor The background color for the outgoing file bubble.
     */
    private void setOutgoingFileBubbleBackgroundColor(@ColorInt int outgoingFileBubbleBackgroundColor) {
        this.outgoingFileBubbleBackgroundColor = outgoingFileBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the outgoing file bubble.
     *
     * @param outgoingFileBubbleCornerRadius The corner radius for the outgoing file bubble.
     */
    private void setOutgoingFileBubbleCornerRadius(@Dimension int outgoingFileBubbleCornerRadius) {
        this.outgoingFileBubbleCornerRadius = outgoingFileBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the outgoing file bubble.
     *
     * @param outgoingFileBubbleStrokeWidth The stroke width for the outgoing file bubble.
     */
    private void setOutgoingFileBubbleStrokeWidth(@Dimension int outgoingFileBubbleStrokeWidth) {
        this.outgoingFileBubbleStrokeWidth = outgoingFileBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the outgoing file bubble.
     *
     * @param outgoingFileBubbleStrokeColor The stroke color for the outgoing file bubble.
     */
    private void setOutgoingFileBubbleStrokeColor(@ColorInt int outgoingFileBubbleStrokeColor) {
        this.outgoingFileBubbleStrokeColor = outgoingFileBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the outgoing file bubble.
     *
     * @param outgoingFileBubbleBackgroundDrawable The background drawable for the outgoing file bubble.
     */
    private void setOutgoingFileBubbleBackgroundDrawable(Drawable outgoingFileBubbleBackgroundDrawable) {
        this.outgoingFileBubbleBackgroundDrawable = outgoingFileBubbleBackgroundDrawable;
    }

    /**
     * Sets the style resource for the outgoing file bubble date.
     *
     * @param outgoingFileBubbleDateStyle The style resource to apply.
     */
    public void setOutgoingFileBubbleDateStyle(@StyleRes int outgoingFileBubbleDateStyle) {
        this.outgoingFileBubbleDateStyle = outgoingFileBubbleDateStyle;
    }

    /**
     * Sets the style resource for the outgoing file bubble receipt.
     *
     * @param outgoingFileReceiptStyle The style resource to apply.
     */
    public void setOutgoingFileBubbleReceiptStyle(@StyleRes int outgoingFileReceiptStyle) {
        this.outgoingFileBubbleReceiptStyle = outgoingFileReceiptStyle;
    }

    /**
     * Sets the style resource for the outgoing file bubble avatar.
     *
     * @param outgoingFileBubbleAvatarStyle The style resource to apply.
     */
    public void setOutgoingFileBubbleAvatarStyle(@StyleRes int outgoingFileBubbleAvatarStyle) {
        this.outgoingFileBubbleAvatarStyle = outgoingFileBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance resource for the outgoing file bubble thread
     * indicator.
     *
     * @param outgoingFileBubbleThreadIndicatorTextAppearance The text appearance resource to apply.
     */
    public void setOutgoingFileBubbleThreadIndicatorTextAppearance(@StyleRes int outgoingFileBubbleThreadIndicatorTextAppearance) {
        this.outgoingFileBubbleThreadIndicatorTextAppearance = outgoingFileBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the outgoing file bubble thread indicator.
     *
     * @param outgoingFileBubbleThreadIndicatorTextColor The color to apply.
     */
    public void setOutgoingFileBubbleThreadIndicatorTextColor(@ColorInt int outgoingFileBubbleThreadIndicatorTextColor) {
        this.outgoingFileBubbleThreadIndicatorTextColor = outgoingFileBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the tint color for the outgoing file bubble thread indicator icon.
     *
     * @param outgoingFileBubbleThreadIndicatorIconTint The color to apply.
     */
    public void setOutgoingFileBubbleThreadIndicatorIconTint(@ColorInt int outgoingFileBubbleThreadIndicatorIconTint) {
        this.outgoingFileBubbleThreadIndicatorIconTint = outgoingFileBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance resource for the outgoing file bubble sender name.
     *
     * @param outgoingFileBubbleSenderNameTextAppearance The text appearance resource to apply.
     */
    public void setOutgoingFileBubbleSenderNameTextAppearance(@StyleRes int outgoingFileBubbleSenderNameTextAppearance) {
        this.outgoingFileBubbleSenderNameTextAppearance = outgoingFileBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the sender name in the outgoing file bubble.
     *
     * @param outgoingFileBubbleSenderNameTextColor the color to set for the sender name
     */
    public void setOutgoingFileBubbleSenderNameTextColor(@ColorInt int outgoingFileBubbleSenderNameTextColor) {
        this.outgoingFileBubbleSenderNameTextColor = outgoingFileBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the outgoing form bubble.
     *
     * @param outgoingFormBubbleBackgroundColor The color to set as the background.
     */
    private void setOutgoingFormBubbleBackgroundColor(@ColorInt int outgoingFormBubbleBackgroundColor) {
        this.outgoingFormBubbleBackgroundColor = outgoingFormBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the outgoing form bubble.
     *
     * @param outgoingFormBubbleCornerRadius The corner radius in pixels.
     */
    private void setOutgoingFormBubbleCornerRadius(@Dimension int outgoingFormBubbleCornerRadius) {
        this.outgoingFormBubbleCornerRadius = outgoingFormBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the outgoing form bubble.
     *
     * @param outgoingFormBubbleStrokeWidth The stroke width in pixels.
     */
    private void setOutgoingFormBubbleStrokeWidth(@Dimension int outgoingFormBubbleStrokeWidth) {
        this.outgoingFormBubbleStrokeWidth = outgoingFormBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the outgoing form bubble.
     *
     * @param outgoingFormBubbleStrokeColor The color to set as the stroke.
     */
    private void setOutgoingFormBubbleStrokeColor(@ColorInt int outgoingFormBubbleStrokeColor) {
        this.outgoingFormBubbleStrokeColor = outgoingFormBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the outgoing form bubble.
     *
     * @param outgoingFormBubbleBackgroundDrawable The drawable to set as the background.
     */
    private void setOutgoingFormBubbleBackgroundDrawable(Drawable outgoingFormBubbleBackgroundDrawable) {
        this.outgoingFormBubbleBackgroundDrawable = outgoingFormBubbleBackgroundDrawable;
    }

    /**
     * Sets the style resource for the outgoing form bubble date.
     *
     * @param outgoingFormBubbleDateStyle The style resource to apply.
     */
    public void setOutgoingFormBubbleDateStyle(@StyleRes int outgoingFormBubbleDateStyle) {
        this.outgoingFormBubbleDateStyle = outgoingFormBubbleDateStyle;
    }

    /**
     * Sets the style resource for the outgoing form bubble receipt.
     *
     * @param outgoingFormBubbleReceiptStyle The style resource to apply.
     */
    public void setOutgoingFormBubbleReceiptStyle(@StyleRes int outgoingFormBubbleReceiptStyle) {
        this.outgoingFormBubbleReceiptStyle = outgoingFormBubbleReceiptStyle;
    }

    /**
     * Sets the style resource for the outgoing form bubble avatar.
     *
     * @param outgoingFormBubbleAvatarStyle The style resource to apply.
     */
    public void setOutgoingFormBubbleAvatarStyle(@StyleRes int outgoingFormBubbleAvatarStyle) {
        this.outgoingFormBubbleAvatarStyle = outgoingFormBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance style for the thread indicator in the outgoing form
     * bubble.
     *
     * @param outgoingFormBubbleThreadIndicatorTextAppearance the style resource ID for the outgoing form bubble thread
     *                                                        indicator text appearance
     */
    public void setOutgoingFormBubbleThreadIndicatorTextAppearance(@StyleRes int outgoingFormBubbleThreadIndicatorTextAppearance) {
        this.outgoingFormBubbleThreadIndicatorTextAppearance = outgoingFormBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the outgoing form bubble thread indicator.
     *
     * @param outgoingFormBubbleThreadIndicatorTextColor the color to set for the outgoing form bubble thread indicator
     *                                                   text
     */
    public void setOutgoingFormBubbleThreadIndicatorTextColor(@ColorInt int outgoingFormBubbleThreadIndicatorTextColor) {
        this.outgoingFormBubbleThreadIndicatorTextColor = outgoingFormBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the tint color for the icon of the outgoing form bubble thread
     * indicator.
     *
     * @param outgoingFormBubbleThreadIndicatorIconTint the color to set for the outgoing form bubble thread indicator
     *                                                  icon
     */
    public void setOutgoingFormBubbleThreadIndicatorIconTint(@ColorInt int outgoingFormBubbleThreadIndicatorIconTint) {
        this.outgoingFormBubbleThreadIndicatorIconTint = outgoingFormBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance style for the sender's name in the outgoing form
     * bubble.
     *
     * @param outgoingFormBubbleSenderNameTextAppearance the style resource ID for the outgoing form bubble sender name
     *                                                   text appearance
     */
    public void setOutgoingFormBubbleSenderNameTextAppearance(@StyleRes int outgoingFormBubbleSenderNameTextAppearance) {
        this.outgoingFormBubbleSenderNameTextAppearance = outgoingFormBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the sender's name in the outgoing form bubble.
     *
     * @param outgoingFormBubbleSenderNameTextColor the color to set for the outgoing form bubble sender name text
     */
    public void setOutgoingFormBubbleSenderNameTextColor(@ColorInt int outgoingFormBubbleSenderNameTextColor) {
        this.outgoingFormBubbleSenderNameTextColor = outgoingFormBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the outgoing scheduler bubble.
     *
     * @param outgoingSchedulerBubbleBackgroundColor The color to set as the background.
     */
    public void setOutgoingSchedulerBubbleBackgroundColor(@ColorInt int outgoingSchedulerBubbleBackgroundColor) {
        this.outgoingSchedulerBubbleBackgroundColor = outgoingSchedulerBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the outgoing scheduler bubble.
     *
     * @param outgoingSchedulerBubbleCornerRadius The corner radius in pixels.
     */
    public void setOutgoingSchedulerBubbleCornerRadius(@Dimension int outgoingSchedulerBubbleCornerRadius) {
        this.outgoingSchedulerBubbleCornerRadius = outgoingSchedulerBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the outgoing scheduler bubble.
     *
     * @param outgoingSchedulerBubbleStrokeWidth The stroke width in pixels.
     */
    public void setOutgoingSchedulerBubbleStrokeWidth(@Dimension int outgoingSchedulerBubbleStrokeWidth) {
        this.outgoingSchedulerBubbleStrokeWidth = outgoingSchedulerBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the outgoing scheduler bubble.
     *
     * @param outgoingSchedulerBubbleStrokeColor The color to set as the stroke.
     */
    public void setOutgoingSchedulerBubbleStrokeColor(@ColorInt int outgoingSchedulerBubbleStrokeColor) {
        this.outgoingSchedulerBubbleStrokeColor = outgoingSchedulerBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the outgoing scheduler bubble.
     *
     * @param outgoingSchedulerBubbleBackgroundDrawable The drawable to set as the background.
     */
    public void setOutgoingSchedulerBubbleBackgroundDrawable(Drawable outgoingSchedulerBubbleBackgroundDrawable) {
        this.outgoingSchedulerBubbleBackgroundDrawable = outgoingSchedulerBubbleBackgroundDrawable;
    }

    /**
     * Sets the style resource for the outgoing scheduler bubble date.
     *
     * @param outgoingSchedulerBubbleDateStyle The style resource to apply.
     */
    public void setOutgoingSchedulerBubbleDateStyle(@StyleRes int outgoingSchedulerBubbleDateStyle) {
        this.outgoingSchedulerBubbleDateStyle = outgoingSchedulerBubbleDateStyle;
    }

    /**
     * Sets the style resource for the outgoing scheduler bubble receipt.
     *
     * @param outgoingSchedulerBubbleReceiptStyle The style resource to apply.
     */
    public void setOutgoingSchedulerBubbleReceiptStyle(@StyleRes int outgoingSchedulerBubbleReceiptStyle) {
        this.outgoingSchedulerBubbleReceiptStyle = outgoingSchedulerBubbleReceiptStyle;
    }

    /**
     * Sets the style resource for the outgoing scheduler bubble avatar.
     *
     * @param outgoingSchedulerBubbleAvatarStyle The style resource to apply.
     */
    public void setOutgoingSchedulerBubbleAvatarStyle(@StyleRes int outgoingSchedulerBubbleAvatarStyle) {
        this.outgoingSchedulerBubbleAvatarStyle = outgoingSchedulerBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance style for the thread indicator in the outgoing
     * scheduler bubble.
     *
     * @param outgoingSchedulerBubbleThreadIndicatorTextAppearance the style resource ID for the outgoing scheduler bubble thread
     *                                                             indicator text appearance
     */
    public void setOutgoingSchedulerBubbleThreadIndicatorTextAppearance(@StyleRes int outgoingSchedulerBubbleThreadIndicatorTextAppearance) {
        this.outgoingSchedulerBubbleThreadIndicatorTextAppearance = outgoingSchedulerBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the outgoing scheduler bubble thread indicator.
     *
     * @param outgoingSchedulerBubbleThreadIndicatorTextColor the color to set for the outgoing scheduler bubble thread
     *                                                        indicator text
     */
    public void setOutgoingSchedulerBubbleThreadIndicatorTextColor(@ColorInt int outgoingSchedulerBubbleThreadIndicatorTextColor) {
        this.outgoingSchedulerBubbleThreadIndicatorTextColor = outgoingSchedulerBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the tint color for the icon of the outgoing scheduler bubble thread
     * indicator.
     *
     * @param outgoingSchedulerBubbleThreadIndicatorIconTint the color to set for the outgoing scheduler bubble thread
     *                                                       indicator icon
     */
    public void setOutgoingSchedulerBubbleThreadIndicatorIconTint(@ColorInt int outgoingSchedulerBubbleThreadIndicatorIconTint) {
        this.outgoingSchedulerBubbleThreadIndicatorIconTint = outgoingSchedulerBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance style for the sender's name in the outgoing
     * scheduler bubble.
     *
     * @param outgoingSchedulerBubbleSenderNameTextAppearance the style resource ID for the outgoing scheduler bubble sender
     *                                                        name text appearance
     */
    public void setOutgoingSchedulerBubbleSenderNameTextAppearance(@StyleRes int outgoingSchedulerBubbleSenderNameTextAppearance) {
        this.outgoingSchedulerBubbleSenderNameTextAppearance = outgoingSchedulerBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the sender's name in the outgoing scheduler bubble.
     *
     * @param outgoingSchedulerBubbleSenderNameTextColor the color to set for the outgoing scheduler bubble sender name
     *                                                   text
     */
    public void setOutgoingSchedulerBubbleSenderNameTextColor(@ColorInt int outgoingSchedulerBubbleSenderNameTextColor) {
        this.outgoingSchedulerBubbleSenderNameTextColor = outgoingSchedulerBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the outgoing poll bubble.
     *
     * @param outgoingPollBubbleBackgroundColor The color to set as the background.
     */
    private void setOutgoingPollBubbleBackgroundColor(@ColorInt int outgoingPollBubbleBackgroundColor) {
        this.outgoingPollBubbleBackgroundColor = outgoingPollBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the outgoing poll bubble.
     *
     * @param outgoingPollBubbleCornerRadius The corner radius in pixels.
     */
    private void setOutgoingPollBubbleCornerRadius(@Dimension int outgoingPollBubbleCornerRadius) {
        this.outgoingPollBubbleCornerRadius = outgoingPollBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the outgoing poll bubble.
     *
     * @param outgoingPollBubbleStrokeWidth The stroke width in pixels.
     */
    private void setOutgoingPollBubbleStrokeWidth(@Dimension int outgoingPollBubbleStrokeWidth) {
        this.outgoingPollBubbleStrokeWidth = outgoingPollBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the outgoing poll bubble.
     *
     * @param outgoingPollBubbleStrokeColor The color to set as the stroke.
     */
    private void setOutgoingPollBubbleStrokeColor(@ColorInt int outgoingPollBubbleStrokeColor) {
        this.outgoingPollBubbleStrokeColor = outgoingPollBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the outgoing poll bubble.
     *
     * @param outgoingPollBubbleBackgroundDrawable The drawable to set as the background.
     */
    private void setOutgoingPollBubbleBackgroundDrawable(Drawable outgoingPollBubbleBackgroundDrawable) {
        this.outgoingPollBubbleBackgroundDrawable = outgoingPollBubbleBackgroundDrawable;
    }

    /**
     * Sets the style resource for the outgoing poll bubble date.
     *
     * @param outgoingPollBubbleDateStyle The style resource to apply.
     */
    public void setOutgoingPollBubbleDateStyle(@StyleRes int outgoingPollBubbleDateStyle) {
        this.outgoingPollBubbleDateStyle = outgoingPollBubbleDateStyle;
    }

    /**
     * Sets the style resource for the outgoing poll bubble receipt.
     *
     * @param outgoingPollBubbleReceiptStyle The style resource to apply.
     */
    public void setOutgoingPollBubbleReceiptStyle(@StyleRes int outgoingPollBubbleReceiptStyle) {
        this.outgoingPollBubbleReceiptStyle = outgoingPollBubbleReceiptStyle;
    }

    /**
     * Sets the style resource for the outgoing poll bubble avatar.
     *
     * @param outgoingPollBubbleAvatarStyle The style resource to apply.
     */
    public void setOutgoingPollBubbleAvatarStyle(@StyleRes int outgoingPollBubbleAvatarStyle) {
        this.outgoingPollBubbleAvatarStyle = outgoingPollBubbleAvatarStyle;
    }

    // Collaborative bubble

    /**
     * Sets the text appearance for the outgoing poll bubble thread indicator.
     *
     * @param outgoingPollBubbleThreadIndicatorTextAppearance the text appearance resource ID for the outgoing poll bubble
     *                                                        thread indicator
     */
    public void setOutgoingPollBubbleThreadIndicatorTextAppearance(@StyleRes int outgoingPollBubbleThreadIndicatorTextAppearance) {
        this.outgoingPollBubbleThreadIndicatorTextAppearance = outgoingPollBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the outgoing poll bubble thread indicator.
     *
     * @param outgoingPollBubbleThreadIndicatorTextColor the text color for the outgoing poll bubble thread indicator
     */
    public void setOutgoingPollBubbleThreadIndicatorTextColor(@ColorInt int outgoingPollBubbleThreadIndicatorTextColor) {
        this.outgoingPollBubbleThreadIndicatorTextColor = outgoingPollBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the icon tint for the outgoing poll bubble thread indicator.
     *
     * @param outgoingPollBubbleThreadIndicatorIconTint the icon tint for the outgoing poll bubble thread indicator
     */
    public void setOutgoingPollBubbleThreadIndicatorIconTint(@ColorInt int outgoingPollBubbleThreadIndicatorIconTint) {
        this.outgoingPollBubbleThreadIndicatorIconTint = outgoingPollBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance for the outgoing poll bubble sender name.
     *
     * @param outgoingPollBubbleSenderNameTextAppearance the text appearance resource ID for the outgoing poll bubble
     *                                                   sender name
     */
    public void setOutgoingPollBubbleSenderNameTextAppearance(@StyleRes int outgoingPollBubbleSenderNameTextAppearance) {
    }

    /**
     * Sets the text color for the outgoing poll bubble sender name.
     *
     * @param outgoingPollBubbleSenderNameTextColor the text color for the outgoing poll bubble sender name
     */
    public void setOutgoingPollBubbleSenderNameTextColor(@ColorInt int outgoingPollBubbleSenderNameTextColor) {
        this.outgoingPollBubbleSenderNameTextColor = outgoingPollBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the outgoing sticker bubble.
     *
     * @param outgoingStickerBubbleBackgroundColor the background color for the outgoing sticker bubble
     */
    public void setOutgoingStickerBubbleBackgroundColor(@ColorInt int outgoingStickerBubbleBackgroundColor) {
        this.outgoingStickerBubbleBackgroundColor = outgoingStickerBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the outgoing sticker bubble.
     *
     * @param outgoingStickerBubbleCornerRadius the corner radius for the outgoing sticker bubble
     */
    public void setOutgoingStickerBubbleCornerRadius(@Dimension int outgoingStickerBubbleCornerRadius) {
        this.outgoingStickerBubbleCornerRadius = outgoingStickerBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the outgoing sticker bubble.
     *
     * @param outgoingStickerBubbleStrokeWidth the stroke width for the outgoing sticker bubble
     */
    public void setOutgoingStickerBubbleStrokeWidth(@Dimension int outgoingStickerBubbleStrokeWidth) {
        this.outgoingStickerBubbleStrokeWidth = outgoingStickerBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the outgoing sticker bubble.
     *
     * @param outgoingStickerBubbleStrokeColor the stroke color for the outgoing sticker bubble
     */
    public void setOutgoingStickerBubbleStrokeColor(@ColorInt int outgoingStickerBubbleStrokeColor) {
        this.outgoingStickerBubbleStrokeColor = outgoingStickerBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the outgoing sticker bubble.
     *
     * @param outgoingStickerBubbleBackgroundDrawable the background drawable for the outgoing sticker bubble
     */
    public void setOutgoingStickerBubbleBackgroundDrawable(Drawable outgoingStickerBubbleBackgroundDrawable) {
        this.outgoingStickerBubbleBackgroundDrawable = outgoingStickerBubbleBackgroundDrawable;
    }

    /**
     * Sets the date style for the outgoing sticker bubble.
     *
     * @param outgoingStickerBubbleDateStyle the date style resource ID for the outgoing sticker bubble
     */
    public void setOutgoingStickerBubbleDateStyle(@StyleRes int outgoingStickerBubbleDateStyle) {
        this.outgoingStickerBubbleDateStyle = outgoingStickerBubbleDateStyle;
    }

    /**
     * Sets the receipt style for the outgoing sticker bubble.
     *
     * @param outgoingStickerBubbleReceiptStyle the receipt style resource ID for the outgoing sticker bubble
     */
    public void setOutgoingStickerBubbleReceiptStyle(@StyleRes int outgoingStickerBubbleReceiptStyle) {
        this.outgoingStickerBubbleReceiptStyle = outgoingStickerBubbleReceiptStyle;
    }

    /**
     * Sets the avatar style for the outgoing sticker bubble.
     *
     * @param outgoingStickerBubbleAvatarStyle the avatar style resource ID for the outgoing sticker bubble
     */
    public void setOutgoingStickerBubbleAvatarStyle(@StyleRes int outgoingStickerBubbleAvatarStyle) {
        this.outgoingStickerBubbleAvatarStyle = outgoingStickerBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance for the outgoing sticker bubble thread indicator.
     *
     * @param outgoingStickerBubbleThreadIndicatorTextAppearance the text appearance resource ID for the outgoing sticker bubble
     *                                                           thread indicator
     */
    public void setOutgoingStickerBubbleThreadIndicatorTextAppearance(@StyleRes int outgoingStickerBubbleThreadIndicatorTextAppearance) {
        this.outgoingStickerBubbleThreadIndicatorTextAppearance = outgoingStickerBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the outgoing sticker bubble thread indicator.
     *
     * @param outgoingStickerBubbleThreadIndicatorTextColor the text color for the outgoing sticker bubble thread indicator
     */
    public void setOutgoingStickerBubbleThreadIndicatorTextColor(@ColorInt int outgoingStickerBubbleThreadIndicatorTextColor) {
        this.outgoingStickerBubbleThreadIndicatorTextColor = outgoingStickerBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the icon tint for the outgoing sticker bubble thread indicator.
     *
     * @param outgoingStickerBubbleThreadIndicatorIconTint the icon tint color for the outgoing sticker bubble thread
     *                                                     indicator
     */
    public void setOutgoingStickerBubbleThreadIndicatorIconTint(@ColorInt int outgoingStickerBubbleThreadIndicatorIconTint) {
        this.outgoingStickerBubbleThreadIndicatorIconTint = outgoingStickerBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance for the outgoing sticker bubble sender name.
     *
     * @param outgoingStickerBubbleSenderNameTextAppearance the text appearance resource ID for the outgoing sticker bubble
     *                                                      sender name
     */
    public void setOutgoingStickerBubbleSenderNameTextAppearance(@StyleRes int outgoingStickerBubbleSenderNameTextAppearance) {
    }

    /**
     * Sets the text color for the outgoing sticker bubble sender name.
     *
     * @param outgoingStickerBubbleSenderNameTextColor the text color for the outgoing sticker bubble sender name
     */
    public void setOutgoingStickerBubbleSenderNameTextColor(@ColorInt int outgoingStickerBubbleSenderNameTextColor) {
        this.outgoingStickerBubbleSenderNameTextColor = outgoingStickerBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the outgoing collaborative bubble.
     *
     * @param outgoingCollaborativeBubbleBackgroundColor the background color for the outgoing collaborative bubble
     */
    public void setOutgoingCollaborativeBubbleBackgroundColor(@ColorInt int outgoingCollaborativeBubbleBackgroundColor) {
        this.outgoingCollaborativeBubbleBackgroundColor = outgoingCollaborativeBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the outgoing collaborative bubble.
     *
     * @param outgoingCollaborativeBubbleCornerRadius the corner radius for the outgoing collaborative bubble
     */
    public void setOutgoingCollaborativeBubbleCornerRadius(@Dimension int outgoingCollaborativeBubbleCornerRadius) {
        this.outgoingCollaborativeBubbleCornerRadius = outgoingCollaborativeBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the outgoing collaborative bubble.
     *
     * @param outgoingCollaborativeBubbleStrokeWidth the stroke width for the outgoing collaborative bubble
     */
    public void setOutgoingCollaborativeBubbleStrokeWidth(@Dimension int outgoingCollaborativeBubbleStrokeWidth) {
        this.outgoingCollaborativeBubbleStrokeWidth = outgoingCollaborativeBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the outgoing collaborative bubble.
     *
     * @param outgoingCollaborativeBubbleStrokeColor the stroke color for the outgoing collaborative bubble
     */
    public void setOutgoingCollaborativeBubbleStrokeColor(@ColorInt int outgoingCollaborativeBubbleStrokeColor) {
        this.outgoingCollaborativeBubbleStrokeColor = outgoingCollaborativeBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the outgoing collaborative bubble.
     *
     * @param outgoingCollaborativeBubbleBackgroundDrawable the background drawable for the outgoing collaborative bubble
     */
    public void setOutgoingCollaborativeBubbleBackgroundDrawable(Drawable outgoingCollaborativeBubbleBackgroundDrawable) {
        this.outgoingCollaborativeBubbleBackgroundDrawable = outgoingCollaborativeBubbleBackgroundDrawable;
    }

    /**
     * Sets the date style for the outgoing collaborative bubble.
     *
     * @param outgoingCollaborativeBubbleDateStyle the date style resource ID for the outgoing collaborative bubble
     */
    public void setOutgoingCollaborativeBubbleDateStyle(@StyleRes int outgoingCollaborativeBubbleDateStyle) {
        this.outgoingCollaborativeBubbleDateStyle = outgoingCollaborativeBubbleDateStyle;
    }

    /**
     * Sets the receipt style for the outgoing collaborative bubble.
     *
     * @param outgoingCollaborativeBubbleReceiptStyle the receipt style resource ID for the outgoing collaborative
     *                                                bubble
     */
    public void setOutgoingCollaborativeBubbleReceiptStyle(@StyleRes int outgoingCollaborativeBubbleReceiptStyle) {
        this.outgoingCollaborativeBubbleReceiptStyle = outgoingCollaborativeBubbleReceiptStyle;
    }

    /**
     * Sets the avatar style for the outgoing collaborative bubble.
     *
     * @param outgoingCollaborativeBubbleAvatarStyle the avatar style resource ID for the outgoing collaborative bubble
     */
    public void setOutgoingCollaborativeBubbleAvatarStyle(@StyleRes int outgoingCollaborativeBubbleAvatarStyle) {
        this.outgoingCollaborativeBubbleAvatarStyle = outgoingCollaborativeBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance for the outgoing collaborative bubble thread
     * indicator.
     *
     * @param outgoingCollaborativeBubbleThreadIndicatorTextAppearance the text appearance resource ID for the outgoing collaborative
     *                                                                 bubble thread indicator
     */
    public void setOutgoingCollaborativeBubbleThreadIndicatorTextAppearance(@StyleRes int outgoingCollaborativeBubbleThreadIndicatorTextAppearance) {
        this.outgoingCollaborativeBubbleThreadIndicatorTextAppearance = outgoingCollaborativeBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the outgoing collaborative bubble thread indicator.
     *
     * @param outgoingCollaborativeBubbleThreadIndicatorTextColor the text color for the outgoing collaborative bubble thread
     *                                                            indicator
     */
    public void setOutgoingCollaborativeBubbleThreadIndicatorTextColor(@ColorInt int outgoingCollaborativeBubbleThreadIndicatorTextColor) {
        this.outgoingCollaborativeBubbleThreadIndicatorTextColor = outgoingCollaborativeBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the icon tint for the outgoing collaborative bubble thread indicator.
     *
     * @param outgoingCollaborativeBubbleThreadIndicatorIconTint the icon tint for the outgoing collaborative bubble thread
     *                                                           indicator
     */
    public void setOutgoingCollaborativeBubbleThreadIndicatorIconTint(@ColorInt int outgoingCollaborativeBubbleThreadIndicatorIconTint) {
        this.outgoingCollaborativeBubbleThreadIndicatorIconTint = outgoingCollaborativeBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance for the outgoing collaborative bubble sender name.
     *
     * @param outgoingCollaborativeBubbleSenderNameTextAppearance the text appearance resource ID for the outgoing collaborative
     *                                                            bubble sender name
     */
    public void setOutgoingCollaborativeBubbleSenderNameTextAppearance(@StyleRes int outgoingCollaborativeBubbleSenderNameTextAppearance) {
    }

    /**
     * Sets the text color for the outgoing collaborative bubble sender name.
     *
     * @param outgoingCollaborativeBubbleSenderNameTextColor the text color for the outgoing collaborative bubble sender name
     */
    public void setOutgoingCollaborativeBubbleSenderNameTextColor(@ColorInt int outgoingCollaborativeBubbleSenderNameTextColor) {
        this.outgoingCollaborativeBubbleSenderNameTextColor = outgoingCollaborativeBubbleSenderNameTextColor;
    }

    /**
     * Sets the background color for the outgoing meet call bubble.
     *
     * @param outgoingMeetCallBubbleBackgroundColor the background color for the outgoing meet call bubble
     */
    public void setOutgoingMeetCallBubbleBackgroundColor(@ColorInt int outgoingMeetCallBubbleBackgroundColor) {
        this.outgoingMeetCallBubbleBackgroundColor = outgoingMeetCallBubbleBackgroundColor;
    }

    /**
     * Sets the corner radius for the outgoing meet call bubble.
     *
     * @param outgoingMeetCallBubbleCornerRadius the corner radius for the outgoing meet call bubble
     */
    public void setOutgoingMeetCallBubbleCornerRadius(@Dimension int outgoingMeetCallBubbleCornerRadius) {
        this.outgoingMeetCallBubbleCornerRadius = outgoingMeetCallBubbleCornerRadius;
    }

    /**
     * Sets the stroke width for the outgoing meet call bubble.
     *
     * @param outgoingMeetCallBubbleStrokeWidth the stroke width for the outgoing meet call bubble
     */
    public void setOutgoingMeetCallBubbleStrokeWidth(@Dimension int outgoingMeetCallBubbleStrokeWidth) {
        this.outgoingMeetCallBubbleStrokeWidth = outgoingMeetCallBubbleStrokeWidth;
    }

    /**
     * Sets the stroke color for the outgoing meet call bubble.
     *
     * @param outgoingMeetCallBubbleStrokeColor the stroke color for the outgoing meet call bubble
     */
    public void setOutgoingMeetCallBubbleStrokeColor(@ColorInt int outgoingMeetCallBubbleStrokeColor) {
        this.outgoingMeetCallBubbleStrokeColor = outgoingMeetCallBubbleStrokeColor;
    }

    /**
     * Sets the background drawable for the outgoing meet call bubble.
     *
     * @param outgoingMeetCallBubbleBackgroundDrawable the background drawable for the outgoing meet call bubble
     */
    public void setOutgoingMeetCallBubbleBackgroundDrawable(Drawable outgoingMeetCallBubbleBackgroundDrawable) {
        this.outgoingMeetCallBubbleBackgroundDrawable = outgoingMeetCallBubbleBackgroundDrawable;
    }

    /**
     * Sets the avatar style for the outgoing meet call bubble.
     *
     * @param outgoingMeetCallBubbleAvatarStyle the avatar style resource ID for the outgoing meet call bubble
     */
    public void setOutgoingMeetCallBubbleAvatarStyle(@StyleRes int outgoingMeetCallBubbleAvatarStyle) {
        this.outgoingMeetCallBubbleAvatarStyle = outgoingMeetCallBubbleAvatarStyle;
    }

    /**
     * Sets the text appearance for the outgoing meet call bubble thread indicator.
     *
     * @param outgoingMeetCallBubbleThreadIndicatorTextAppearance the text appearance resource ID for the outgoing meet call bubble
     *                                                            thread indicator
     */
    public void setOutgoingMeetCallBubbleThreadIndicatorTextAppearance(@StyleRes int outgoingMeetCallBubbleThreadIndicatorTextAppearance) {
        this.outgoingMeetCallBubbleThreadIndicatorTextAppearance = outgoingMeetCallBubbleThreadIndicatorTextAppearance;
    }

    /**
     * Sets the text color for the outgoing meet call bubble thread indicator.
     *
     * @param outgoingMeetCallBubbleThreadIndicatorTextColor the text color for the outgoing meet call bubble thread indicator
     */
    public void setOutgoingMeetCallBubbleThreadIndicatorTextColor(@ColorInt int outgoingMeetCallBubbleThreadIndicatorTextColor) {
        this.outgoingMeetCallBubbleThreadIndicatorTextColor = outgoingMeetCallBubbleThreadIndicatorTextColor;
    }

    /**
     * Sets the icon tint for the outgoing meet call bubble thread indicator.
     *
     * @param outgoingMeetCallBubbleThreadIndicatorIconTint the icon tint color for the outgoing meet call bubble thread
     *                                                      indicator
     */
    public void setOutgoingMeetCallBubbleThreadIndicatorIconTint(@ColorInt int outgoingMeetCallBubbleThreadIndicatorIconTint) {
        this.outgoingMeetCallBubbleThreadIndicatorIconTint = outgoingMeetCallBubbleThreadIndicatorIconTint;
    }

    /**
     * Sets the text appearance for the outgoing meet call bubble sender name.
     *
     * @param outgoingMeetCallBubbleSenderNameTextAppearance the text appearance resource ID for the outgoing meet call bubble
     *                                                       sender name
     */
    public void setOutgoingMeetCallBubbleSenderNameTextAppearance(@StyleRes int outgoingMeetCallBubbleSenderNameTextAppearance) {
        this.outgoingMeetCallBubbleSenderNameTextAppearance = outgoingMeetCallBubbleSenderNameTextAppearance;
    }

    /**
     * Sets the text color for the outgoing meet call bubble sender name.
     *
     * @param outgoingMeetCallBubbleSenderNameTextColor the text color for the outgoing meet call bubble sender name
     */
    public void setOutgoingMeetCallBubbleSenderNameTextColor(@ColorInt int outgoingMeetCallBubbleSenderNameTextColor) {
        this.outgoingMeetCallBubbleSenderNameTextColor = outgoingMeetCallBubbleSenderNameTextColor;
    }

    /**
     * Sets the alignment of the message list. This method allows the alignment of
     * the message list to be customized. It can be set to a specific alignment
     * value defined in the {@link UIKitConstants.MessageListAlignment} enumeration.
     * When the alignment is changed, the message list will be refreshed to reflect
     * the new alignment.
     *
     * @param listAlignment The desired alignment for the message list. If null, the method
     *                      will not change the alignment.
     */
    public void setAlignment(UIKitConstants.MessageListAlignment listAlignment) {
        if (listAlignment != null) {
            this.listAlignment = listAlignment;
            notifyDataSetChanged();
        }
    }

    public void setThreadVisibility(int visibility) {
        this.threadVisibility = visibility;
        notifyDataSetChanged();
    }

    /**
     * Toggles the visibility of user avatars in the message list. This method
     * allows the user avatars to be shown or hidden in the message list. Setting
     * this to true will display avatars for both individual users and groups, while
     * setting it to false will hide them.
     *
     * @param showAvatar A boolean value indicating whether to show user avatars (true) or
     *                   hide them (false).
     */
    public void showAvatar(boolean showAvatar) {
        this.showAvatar = showAvatar;
        this.showLeftBubbleUserAvatar = showAvatar;
        this.showLeftBubbleGroupAvatar = showAvatar;
        notifyDataSetChanged();
    }

    /**
     * Returns the list of base messages.
     *
     * @return the list of base messages
     */
    public List<BaseMessage> getBaseMessageList() {
        return baseMessageList;
    }

    /**
     * Sets the list of base messages to be displayed. This method updates the
     * current message list and refreshes the view to display the new messages.
     *
     * @param baseMessageList The list of {@link BaseMessage} objects to display.
     */
    public void setBaseMessageList(List<BaseMessage> baseMessageList) {
        if (baseMessageList != null) {
            this.baseMessageList = baseMessageList;
            notifyDataSetChanged();
        }
    }

    /**
     * Returns the context associated with this instance.
     *
     * @return the context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Returns the thread reply click listener.
     *
     * @return the thread reply click listener
     */
    public CometChatMessageList.ThreadReplyClick getThreadReplyClick() {
        return threadReplyClick;
    }

    /**
     * Sets the thread reply click listener. This listener handles actions when a
     * thread reply is clicked in the message list.
     *
     * @param threadReplyClick The {@link CometChatMessageList.ThreadReplyClick} listener.
     */
    public void setThreadReplyClick(CometChatMessageList.ThreadReplyClick threadReplyClick) {
        if (threadReplyClick != null) {
            this.threadReplyClick = threadReplyClick;
            notifyDataSetChanged();
        }
    }

    /**
     * Returns whether read receipts are disabled.
     *
     * @return true if read receipts are disabled; false otherwise
     */
    public boolean isDisableReadReceipt() {
        return disableReadReceipt;
    }

    /**
     * Returns the alignment of the message list.
     *
     * @return the message list alignment
     */
    public UIKitConstants.MessageListAlignment getListAlignment() {
        return listAlignment;
    }

    /**
     * Returns whether to show the avatar.
     *
     * @return true if the avatar should be shown; false otherwise
     */
    public boolean isShowAvatar() {
        return showAvatar;
    }

    /**
     * Returns whether to show the user avatar for left bubbles.
     *
     * @return true if the left bubble user avatar should be shown; false otherwise
     */
    public boolean isShowLeftBubbleUserAvatar() {
        return showLeftBubbleUserAvatar;
    }

    /**
     * Returns whether to show the group avatar for left bubbles.
     *
     * @return true if the left bubble group avatar should be shown; false otherwise
     */
    public boolean isShowLeftBubbleGroupAvatar() {
        return showLeftBubbleGroupAvatar;
    }

    /**
     * Returns the date pattern function for formatting dates.
     *
     * @return the date pattern function
     */
    public SimpleDateFormat getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(SimpleDateFormat timeFormat) {
        if (timeFormat != null) {
            this.timeFormat = timeFormat;
            notifyDataSetChanged();
        }
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        if (dateFormat != null) {
            this.dateSeparatorFormat = dateFormat;
            notifyDataSetChanged();
        }
    }

    /**
     * Returns the date separator pattern function for formatting date separators.
     *
     * @return the date separator pattern function
     */
    public SimpleDateFormat getDateSeparatorFormat() {
        return dateSeparatorFormat;
    }

    /**
     * Returns the alignment of the timestamp.
     *
     * @return the timestamp alignment
     */
    public UIKitConstants.TimeStampAlignment getTimeStampAlignment() {
        return timeStampAlignment;
    }

    /**
     * Sets the alignment of the timestamps in the message list.
     *
     * <p>
     * This method allows the user to specify how timestamps should be aligned
     * within the message bubbles. Depending on the alignment setting, timestamps
     * may be displayed to the left, right, or centered in relation to the message
     * bubbles. When the alignment is updated, the message list will refresh to
     * apply the new alignment setting.
     *
     * @param timeStampAlignment The {@link UIKitConstants.TimeStampAlignment} value representing
     *                           the desired timestamp alignment. If null, the method will not
     *                           change the alignment.
     */
    public void setTimeStampAlignment(UIKitConstants.TimeStampAlignment timeStampAlignment) {
        if (timeStampAlignment != null) {
            this.timeStampAlignment = timeStampAlignment;
            notifyDataSetChanged();
        }
    }

    /**
     * Returns the long click listener for messages.
     *
     * @return the long click listener for messages
     */
    public OnMessageLongClick getOnMessageLongClick() {
        return onMessageLongClick;
    }

    /**
     * Returns the hash map of message templates.
     *
     * @return the hash map of message templates
     */
    public HashMap<String, CometChatMessageTemplate> getMessageTemplateHashMap() {
        return messageTemplateHashMap;
    }

    /**
     * Returns the style resource ID for the date separator.
     *
     * @return the date separator style resource ID
     */
    public @StyleRes int getDateSeparatorStyle() {
        return dateSeparatorStyle;
    }

    /**
     * Sets the style resource ID for the date separator. This method updates the
     * date separator's appearance and refreshes the view to reflect the new style.
     *
     * @param dateStyle The resource ID for the date separator style.
     */
    public void setDateSeparatorStyle(@StyleRes int dateStyle) {
        if (dateStyle != 0) {
            this.dateSeparatorStyle = dateStyle;
            notifyDataSetChanged();
        }
    }

    /**
     * Returns the type of the message or object.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the current object or entity. This method updates the type
     * and refreshes the view to reflect the new type.
     *
     * @param type The type as a {@link String}.
     */
    public void setType(String type) {
        this.type = type;
        notifyDataSetChanged();
    }

    /**
     * Returns the user associated with this instance.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user associated with this instance. This method allows the user to
     * be set, which may affect how messages are displayed or processed in the user
     * interface. The user information can be utilized in various functionalities
     * such as message display, sender identification, etc.
     *
     * @param user The {@link User} object representing the user to be set. If null,
     *             this will set the user to null.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the group associated with this instance.
     *
     * @return the group
     */
    public Group getGroup() {
        return group;
    }

    /**
     * Sets the group associated with this instance. This method allows the group to
     * be set, which may impact how messages are displayed or processed in the user
     * interface. The group information can be utilized for features like group
     * messaging and identification of the group sender.
     *
     * @param group The {@link Group} object representing the group to be set. If
     *              null, this will set the group to null.
     */
    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     * Returns the layout direction.
     *
     * @return the layout direction
     */
    public int getLayoutDirection() {
        return layoutDirection;
    }

    /**
     * Returns the top margin for the left bubble.
     *
     * @return the top margin for the left bubble
     */
    public int getLeftBubbleMarginTop() {
        return leftBubbleMarginTop;
    }

    /**
     * Returns the bottom margin for the left bubble.
     *
     * @return the bottom margin for the left bubble
     */
    public int getLeftBubbleMarginBottom() {
        return leftBubbleMarginBottom;
    }

    /**
     * Returns the start margin for the left bubble.
     *
     * @return the start margin for the left bubble
     */
    public int getLeftBubbleMarginStart() {
        return leftBubbleMarginStart;
    }

    /**
     * Returns the end margin for the left bubble.
     *
     * @return the end margin for the left bubble
     */
    public int getLeftBubbleMarginEnd() {
        return leftBubbleMarginEnd;
    }

    /**
     * Returns the top margin for the right bubble.
     *
     * @return the top margin for the right bubble
     */
    public int getRightBubbleMarginTop() {
        return rightBubbleMarginTop;
    }

    /**
     * Returns the bottom margin for the right bubble.
     *
     * @return the bottom margin for the right bubble
     */
    public int getRightBubbleMarginBottom() {
        return rightBubbleMarginBottom;
    }

    /**
     * Returns the start margin for the right bubble.
     *
     * @return the start margin for the right bubble
     */
    public int getRightBubbleMarginStart() {
        return rightBubbleMarginStart;
    }

    /**
     * Returns the end margin for the right bubble.
     *
     * @return the end margin for the right bubble
     */
    public int getRightBubbleMarginEnd() {
        return rightBubbleMarginEnd;
    }

    /**
     * Adjusts the width of the content view based on the number of reactions
     * present in the message. This method calculates the total width required to
     * display all the reactions for the message, and then adjusts the width of the
     * content view to accommodate all the reactions.
     *
     * @param footerView              The footer view for the message.
     * @param contentView             The content view for the message.
     * @param cometchatMessageBubble  The CometChatMessageBubble instance for the message.
     * @param baseMessage             The BaseMessage instance for the message.
     * @param reactionStyle           The style resource for the reactions.
     * @param onReactionClick         The listener for reaction clicks.
     * @param onReactionLongClick     The listener for reaction long clicks.
     * @param onAddMoreReactionsClick The listener for add more reactions clicks.
     */
    private void adjustFooterAndContentView(View footerView,
                                            View contentView,
                                            CometChatMessageBubble cometchatMessageBubble,
                                            BaseMessage baseMessage,
                                            @StyleRes int reactionStyle,
                                            OnReactionClick onReactionClick,
                                            OnReactionLongClick onReactionLongClick,
                                            OnAddMoreReactionsClick onAddMoreReactionsClick
    ) {
        if (footerView != null) {
            int reactionLimit = 4;
            MessageBubbleUtils.bindReactionsView(footerView,
                                                 baseMessage,
                                                 reactionLimit,
                                                 reactionStyle,
                                                 onReactionClick,
                                                 onReactionLongClick,
                                                 onAddMoreReactionsClick);
            if (contentView != null) {
                contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                int contentViewWidth = contentView.getMeasuredWidth();
                ViewGroup.LayoutParams contentViewLayoutParams = contentView.getLayoutParams();
                if (!baseMessage.getReactions().isEmpty() && baseMessage.getReactions().size() >= 2) {
                    int totalChipSize;
                    if (baseMessage.getReactions().size() <= reactionLimit) {
                        totalChipSize = reactionChipSize * Math.min(baseMessage.getReactions().size(), reactionLimit);
                    } else {
                        totalChipSize = (reactionChipSize * reactionLimit) + plusReactionChipSize;
                    }
                    if (contentViewWidth < totalChipSize) {
                        contentViewLayoutParams.width = totalChipSize;
                    } else {
                        contentView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                                                                  LinearLayout.LayoutParams.WRAP_CONTENT));
                    }
                } else {
                    contentView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                                                              LinearLayout.LayoutParams.WRAP_CONTENT));
                }
            }
        } else {
            cometchatMessageBubble.setFooterView(null);
        }
    }

    public void hideGroupActionMessage(boolean hideGroupActionMessage) {
        this.hideGroupActionMessage = hideGroupActionMessage;
        notifyDataSetChanged();
    }

    public void setDateTimeFormatter(DateTimeFormatterCallback dateTimeFormatter) {
        if (dateTimeFormatter != null) {
            this.dateTimeFormatter = dateTimeFormatter;
            notifyDataSetChanged();
        }
    }

    /**
     * Interface definition for a callback to be invoked when a message is
     * long-clicked.
     *
     * <p>
     * This interface provides a method that is triggered when a long click occurs
     * on a message bubble. Implementing this interface allows handling of long
     * click events, enabling features such as showing context menus or additional
     * options related to the message.
     */
    public interface OnMessageLongClick {
        void onLongClick(List<CometChatMessageOption> list,
                         BaseMessage message,
                         CometChatMessageTemplate cometchatMessageTemplate,
                         CometChatMessageBubble cometchatMessageBubble);
    }

    /**
     * ViewHolder for empty rows in the RecyclerView.
     */
    public class EmptyRowHolder extends RecyclerView.ViewHolder {
        public EmptyRowHolder(View inflate) {
            super(inflate);
        }
    }

    /**
     * ViewHolder for displaying left-aligned message bubbles in a chat application.
     */
    public class LeftViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout parent; // Parent layout for the message bubble
        public CometChatMessageBubble cometchatMessageBubble; // The message bubble view
        public CometChatMessageTemplate template; // The template used for customization
        public UIKitConstants.MessageBubbleAlignment alignment; // Alignment of the message bubble
        public View contentView, headerView, footerView, threadView, bottomView, statusInfoView, leadingView, bubbleView; // Various views for different sections of the bubble

        /**
         * Constructs a LeftViewHolder with the specified item view and message
         * template.
         *
         * @param itemView The inflated view for the left message bubble.
         * @param template The template defining how the message bubble should look.
         */
        public LeftViewHolder(@NonNull View itemView, CometChatMessageTemplate template) {
            super(itemView);
            parent = itemView.findViewById(R.id.message_adapter_message_bubble_parent); // Initialize parent layout
            cometchatMessageBubble = itemView.findViewById(R.id.message_bubble); // Initialize message bubble
            this.template = template;
            alignment = UIKitConstants.MessageBubbleAlignment.LEFT; // Default alignment
            // Adjust alignment based on layout direction (RTL)
            if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                alignment = UIKitConstants.MessageBubbleAlignment.RIGHT;
            }
            cometchatMessageBubble.setMessageAlignment(alignment); // Set message alignment

            // Set margins for the parent layout based on provided values
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) cometchatMessageBubble.getLayoutParams();
            if (leftBubbleMarginStart != -1) layoutParams.leftMargin = leftBubbleMarginStart;
            if (leftBubbleMarginEnd != -1) layoutParams.rightMargin = leftBubbleMarginEnd;
            if (leftBubbleMarginTop != -1) layoutParams.topMargin = leftBubbleMarginTop;
            if (leftBubbleMarginBottom != -1) layoutParams.bottomMargin = leftBubbleMarginBottom;
            cometchatMessageBubble.setLayoutParams(layoutParams); // Apply layout parameters

            // Check if template and views are available, then initialize the views
            // accordingly
            if (template != null) {
                if (template.getBubbleView() != null) {
                    bubbleView = template.getBubbleView().createView(context, cometchatMessageBubble, alignment);
                }
                // Add bubble view if available
                if (bubbleView != null) {
                    parent.removeAllViews();
                    parent.addView(bubbleView);
                } else {
                    // Initialize content, header, bottom, status info, footer, thread, and leading
                    // views
                    if (template.getContentView() != null) {
                        contentView = template.getContentView().createView(context, cometchatMessageBubble, alignment);
                    }
                    if (template.getHeaderView() != null) {
                        headerView = template.getHeaderView().createView(context, cometchatMessageBubble, alignment);
                    } else {
                        headerView = MessageBubbleUtils.getHeaderViewContainer(context);
                        headerView.setTag(UIKitConstants.ViewTag.INTERNAL_HEADER_VIEW);
                    }
                    if (template.getBottomView() != null) {
                        bottomView = template.getBottomView().createView(context, cometchatMessageBubble, alignment);
                    }
                    if (template.getStatusInfoView() != null) {
                        statusInfoView = template.getStatusInfoView().createView(context, cometchatMessageBubble, alignment);
                    } else {
                        statusInfoView = MessageBubbleUtils.getStatusInfoViewContainer(context);
                        statusInfoView.setTag(UIKitConstants.ViewTag.INTERNAL_STATUS_INFO_VIEW);
                    }
                    if (template.getFooterView() != null) {
                        footerView = template.getFooterView().createView(context, cometchatMessageBubble, alignment);
                    } else {
                        if (!disableReactions) {
                            footerView = MessageBubbleUtils.getReactionsViewContainer(context, alignment);
                        }
                    }
                    if (threadVisibility == View.VISIBLE) {
                        threadView = MessageBubbleUtils.getThreadViewContainer(context);
                        threadView.setTag(UIKitConstants.ViewTag.INTERNAL_THREAD_VIEW);
                    }
                    leadingView = MessageBubbleUtils.getAvatarLeadingViewContainer(context, alignment);
                    leadingView.setTag(UIKitConstants.ViewTag.INTERNAL_LEADING_VIEW);

                    // Set all initialized views to the message bubble
                    cometchatMessageBubble.setContentView(contentView);
                    cometchatMessageBubble.setBottomView(bottomView);
                    cometchatMessageBubble.setStatusInfoView(statusInfoView);
                    cometchatMessageBubble.setThreadView(threadView);
                    cometchatMessageBubble.setLeadingView(leadingView);
                    cometchatMessageBubble.setHeaderView(headerView);
                    cometchatMessageBubble.setFooterView(footerView);
                }
            }
        }

        /**
         * Binds the provided message data to the view holder.
         *
         * @param baseMessage The message data to bind to the view.
         * @param position    The position of the message in the list.
         */
        public void bindBubble(BaseMessage baseMessage, int position) {
            boolean hideName = isHideName(baseMessage, alignment);
            boolean showReadReceipt = (layoutDirection != View.LAYOUT_DIRECTION_RTL) && !disableReadReceipt && !alignment.equals(UIKitConstants.MessageBubbleAlignment.LEFT) && baseMessage.getDeletedAt() == 0;
            String time = getTimePattern(baseMessage); // Format message time

            if (template != null) {
                if (template.getBubbleView() != null) {
                    // Bind custom bubble view if available
                    template.getBubbleView().bindView(context, bubbleView, baseMessage, alignment, this, baseMessageList, position);
                } else {
                    boolean isIncoming = !baseMessage
                        .getSender()
                        .getUid()
                        .equals(CometChatUIKit.getLoggedInUser().getUid()); // Check if the message is incoming
                    applyBubbleStyle(baseMessage,
                                     isIncoming,
                                     alignment,
                                     cometchatMessageBubble,
                                     showReadReceipt,
                                     hideName,
                                     headerView,
                                     statusInfoView,
                                     threadView,
                                     leadingView);

                    // Bind content view if available
                    if (template.getContentView() != null) {
                        cometchatMessageBubble.setContentViewVisibility(View.VISIBLE);
                        template.getContentView().bindView(context, contentView, baseMessage, alignment, this, baseMessageList, position);
                    } else {
                        cometchatMessageBubble.setContentViewVisibility(View.GONE);
                    }

                    // Bind header view if available
                    if (template.getHeaderView() != null) {
                        template.getHeaderView().bindView(context, headerView, baseMessage, alignment, this, baseMessageList, position);
                    } else {
                        if (headerView == null) {
                            cometchatMessageBubble.setHeaderView(null);
                        }
                    }

                    // Bind bottom view if available
                    if (template.getBottomView() != null) {
                        cometchatMessageBubble.setBottomViewVisibility(View.VISIBLE);
                        template.getBottomView().bindView(context, bottomView, baseMessage, alignment, this, baseMessageList, position);
                    } else {
                        cometchatMessageBubble.setBottomViewVisibility(View.GONE);
                    }

                    // Bind status info view if available
                    if (template.getStatusInfoView() != null) {
                        template.getStatusInfoView().bindView(context, statusInfoView, baseMessage, alignment, this, baseMessageList, position);
                    } else {
                        if (statusInfoView == null) {
                            cometchatMessageBubble.setStatusInfoView(null);
                        }
                    }

                    // Bind footer view if available
                    if (template.getFooterView() != null) {
                        template.getFooterView().bindView(context, footerView, baseMessage, alignment, this, baseMessageList, position);
                    } else {
                        adjustFooterAndContentView(footerView,
                                                   contentView,
                                                   cometchatMessageBubble,
                                                   baseMessage,
                                                   isIncoming ? incomingMessageBubbleReactionStyle : outgoingMessageBubbleReactionStyle,
                                                   onReactionClick,
                                                   onReactionLongClick,
                                                   onAddMoreReactionsClick);
                    }

                    // Handle thread view visibility for replies
                    if (baseMessage.getReplyCount() > 0 && baseMessage.getDeletedAt() == 0) {
                        cometchatMessageBubble.setThreadViewVisibility(View.VISIBLE);
                        if (threadView != null) {
                            threadView.setOnClickListener(view1 -> {
                                if (threadReplyClick != null) {
                                    threadReplyClick.onThreadReplyClick(context, baseMessage, template);
                                }
                            });
                        }
                    } else {
                        cometchatMessageBubble.setThreadViewVisibility(View.GONE);
                        cometchatMessageBubble.setThreadView(null);
                    }
                }
                // Set long click listener for message options
                parent.setOnLongClickListener(view -> {
                    List<CometChatMessageOption> options = template.getOptions(context, baseMessage, group);
                    if (baseMessage.getDeletedAt() == 0) {
                        onMessageLongClick.onLongClick(options,
                                                       baseMessage,
                                                       template,
                                                       cometchatMessageBubble);
                        return true;
                    } else return false;
                });
            }
        }
    }

    /**
     * ViewHolder for displaying right-aligned message bubbles in a chat
     * application.
     */
    public class RightViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout parent; // Parent layout for the message bubble
        public CometChatMessageBubble cometchatMessageBubble; // The message bubble view
        public CometChatMessageTemplate template; // The template used for customization
        public UIKitConstants.MessageBubbleAlignment alignment; // Alignment of the message bubble
        public View contentView, headerView, footerView, threadView, bottomView, statusInfoView, leadingView, bubbleView; // Various views for different sections of the bubble

        /**
         * Constructs a RightViewHolder with the specified item view and message
         * template.
         *
         * @param itemView The inflated view for the right message bubble.
         * @param template The template defining how the message bubble should look.
         */
        public RightViewHolder(@NonNull View itemView, CometChatMessageTemplate template) {
            super(itemView);
            parent = itemView.findViewById(R.id.message_adapter_message_bubble_parent); // Initialize parent layout
            cometchatMessageBubble = itemView.findViewById(R.id.message_bubble); // Initialize message bubble
            this.template = template;
            alignment = UIKitConstants.MessageBubbleAlignment.RIGHT; // Default alignment
            // Adjust alignment based on layout direction (RTL)
            if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT;
            }
            cometchatMessageBubble.setMessageAlignment(alignment); // Set message alignment

            // Set margins for the parent layout based on provided values
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) cometchatMessageBubble.getLayoutParams();
            if (rightBubbleMarginStart != -1) layoutParams.leftMargin = rightBubbleMarginStart;
            if (rightBubbleMarginEnd != -1) layoutParams.rightMargin = rightBubbleMarginEnd;
            if (rightBubbleMarginTop != -1) layoutParams.topMargin = rightBubbleMarginTop;
            if (rightBubbleMarginBottom != -1) layoutParams.bottomMargin = rightBubbleMarginBottom;
            cometchatMessageBubble.setLayoutParams(layoutParams); // Apply layout parameters

            // Check if template and views are available, then initialize the views
            // accordingly
            if (template != null) {
                if (template.getBubbleView() != null) {
                    bubbleView = template.getBubbleView().createView(context, cometchatMessageBubble, alignment);
                }
                // Add bubble view if available
                if (bubbleView != null) {
                    parent.removeAllViews();
                    parent.addView(bubbleView);
                } else {
                    // Initialize content, header, bottom, status info, footer, thread, and leading
                    // views
                    if (template.getContentView() != null) {
                        contentView = template.getContentView().createView(context, cometchatMessageBubble, alignment);
                    }
                    if (template.getHeaderView() != null) {
                        headerView = template.getHeaderView().createView(context, cometchatMessageBubble, alignment);
                    } else {
                        headerView = MessageBubbleUtils.getHeaderViewContainer(context);
                        headerView.setTag(UIKitConstants.ViewTag.INTERNAL_HEADER_VIEW);
                    }
                    if (template.getBottomView() != null) {
                        bottomView = template.getBottomView().createView(context, cometchatMessageBubble, alignment);
                    }
                    if (template.getStatusInfoView() != null) {
                        statusInfoView = template.getStatusInfoView().createView(context, cometchatMessageBubble, alignment);
                    } else {
                        statusInfoView = MessageBubbleUtils.getStatusInfoViewContainer(context);
                        statusInfoView.setTag(UIKitConstants.ViewTag.INTERNAL_STATUS_INFO_VIEW);
                    }
                    if (template.getFooterView() != null) {
                        footerView = template.getFooterView().createView(context, cometchatMessageBubble, alignment);
                    } else {
                        if (!disableReactions) {
                            footerView = MessageBubbleUtils.getReactionsViewContainer(context, alignment);
                        }
                    }
                    if (threadVisibility == View.VISIBLE) {
                        threadView = MessageBubbleUtils.getThreadViewContainer(context);
                        threadView.setTag(UIKitConstants.ViewTag.INTERNAL_THREAD_VIEW);
                    }
                    leadingView = MessageBubbleUtils.getAvatarLeadingViewContainer(context, alignment);
                    leadingView.setTag(UIKitConstants.ViewTag.INTERNAL_LEADING_VIEW);

                    // Set all initialized views to the message bubble
                    cometchatMessageBubble.setContentView(contentView);
                    cometchatMessageBubble.setBottomView(bottomView);
                    cometchatMessageBubble.setStatusInfoView(statusInfoView);
                    cometchatMessageBubble.setThreadView(threadView);
                    cometchatMessageBubble.setLeadingView(leadingView);
                    cometchatMessageBubble.setHeaderView(headerView);
                    cometchatMessageBubble.setFooterView(footerView);
                }
            }
        }

        /**
         * Binds the provided message data to the view holder.
         *
         * @param baseMessage The message data to bind to the view.
         * @param position    The position of the message in the list.
         */
        public void bindBubble(BaseMessage baseMessage, int position) {
            boolean hideName = true;
            boolean showReadReceipt = (layoutDirection != View.LAYOUT_DIRECTION_RTL) && !disableReadReceipt && !alignment.equals(UIKitConstants.MessageBubbleAlignment.LEFT) && baseMessage.getDeletedAt() == 0;
            String time = getTimePattern(baseMessage); // Format message time

            if (template != null) {
                if (template.getBubbleView() != null) {
                    // Bind custom bubble view if available
                    template.getBubbleView().bindView(context, bubbleView, baseMessage, alignment, this, baseMessageList, position);
                } else {
                    boolean isIncoming = !baseMessage
                        .getSender()
                        .getUid()
                        .equals(CometChatUIKit.getLoggedInUser().getUid()); // Check if the message is incoming
                    applyBubbleStyle(baseMessage,
                                     isIncoming,
                                     alignment,
                                     cometchatMessageBubble,
                                     showReadReceipt,
                                     hideName,
                                     headerView,
                                     statusInfoView,
                                     threadView,
                                     leadingView);

                    // Bind content view if available
                    if (template.getContentView() != null) {
                        cometchatMessageBubble.setContentViewVisibility(View.VISIBLE);
                        template.getContentView().bindView(context, contentView, baseMessage, alignment, this, baseMessageList, position);
                    } else {
                        cometchatMessageBubble.setContentViewVisibility(View.GONE);
                    }

                    // Bind header view if available
                    if (template.getHeaderView() != null) {
                        template.getHeaderView().bindView(context, headerView, baseMessage, alignment, this, baseMessageList, position);
                    } else {
                        if (headerView == null) {
                            cometchatMessageBubble.setHeaderView(null);
                        }
                    }

                    // Bind bottom view if available
                    if (template.getBottomView() != null) {
                        cometchatMessageBubble.setBottomViewVisibility(View.VISIBLE);
                        template.getBottomView().bindView(context, bottomView, baseMessage, alignment, this, baseMessageList, position);
                    } else {
                        cometchatMessageBubble.setBottomViewVisibility(View.GONE);
                    }

                    // Bind status info view if available
                    if (template.getStatusInfoView() != null) {
                        template.getStatusInfoView().bindView(context, statusInfoView, baseMessage, alignment, this, baseMessageList, position);
                    } else {
                        if (statusInfoView == null) {
                            cometchatMessageBubble.setStatusInfoView(null);
                        }
                    }

                    // Bind footer view if available
                    if (template.getFooterView() != null) {
                        template.getFooterView().bindView(context, footerView, baseMessage, alignment, this, baseMessageList, position);
                    } else {
                        adjustFooterAndContentView(footerView,
                                                   contentView,
                                                   cometchatMessageBubble,
                                                   baseMessage,
                                                   isIncoming ? incomingMessageBubbleReactionStyle : outgoingMessageBubbleReactionStyle,
                                                   onReactionClick,
                                                   onReactionLongClick,
                                                   onAddMoreReactionsClick);
                    }

                    // Handle thread view visibility for replies
                    if (baseMessage.getReplyCount() > 0 && baseMessage.getDeletedAt() == 0) {
                        cometchatMessageBubble.setThreadViewVisibility(View.VISIBLE);
                        if (threadView != null) {
                            threadView.setOnClickListener(view1 -> {
                                if (threadReplyClick != null) {
                                    threadReplyClick.onThreadReplyClick(context, baseMessage, template);
                                }
                            });
                        }
                    } else {
                        cometchatMessageBubble.setThreadViewVisibility(View.GONE);
                        cometchatMessageBubble.setThreadView(null);
                    }
                }
                // Set long click listener for message options
                parent.setOnLongClickListener(view -> {
                    List<CometChatMessageOption> options = template.getOptions(context, baseMessage, group);
                    if (baseMessage.getDeletedAt() == 0) {
                        onMessageLongClick.onLongClick(options,
                                                       baseMessage,
                                                       template,
                                                       cometchatMessageBubble);
                        return true;
                    } else return false;
                });
            }
        }
    }

    /**
     * ViewHolder for displaying center-aligned message bubbles in a chat
     * application.
     */
    public class CenterViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout parent; // Parent layout for the message bubble
        public CometChatMessageBubble cometchatMessageBubble; // The message bubble view
        public CometChatMessageTemplate template; // The template used for customization
        public UIKitConstants.MessageBubbleAlignment alignment; // Alignment of the message bubble
        public View contentView, headerView, footerView, threadView, bottomView, statusInfoView, leadingView, bubbleView; // Various views for different sections of the bubble

        /**
         * Constructs a CenterViewHolder with the specified item view and message
         * template.
         *
         * @param itemView The inflated view for the center message bubble.
         * @param template The template defining how the message bubble should look.
         */
        public CenterViewHolder(@NonNull View itemView, CometChatMessageTemplate template) {
            super(itemView);
            parent = itemView.findViewById(R.id.message_adapter_message_bubble_parent); // Initialize parent layout
            cometchatMessageBubble = itemView.findViewById(R.id.message_bubble); // Initialize message bubble
            alignment = UIKitConstants.MessageBubbleAlignment.CENTER; // Set alignment to center
            cometchatMessageBubble.setMessageAlignment(alignment); // Apply alignment to message bubble
            this.template = template;

            // Check if the template is available and initialize views accordingly
            if (template != null) {
                if (template.getBubbleView() != null) {
                    bubbleView = template.getBubbleView().createView(context, cometchatMessageBubble, alignment);
                }
                // Add bubble view if available
                if (bubbleView != null) {
                    parent.removeAllViews();
                    parent.addView(bubbleView);
                } else {
                    // Initialize various views based on the template
                    if (template.getContentView() != null) {
                        contentView = template.getContentView().createView(context, cometchatMessageBubble, alignment);
                    }
                    if (template.getHeaderView() != null) {
                        headerView = template.getHeaderView().createView(context, cometchatMessageBubble, alignment);
                    }
                    if (template.getBottomView() != null) {
                        bottomView = template.getBottomView().createView(context, cometchatMessageBubble, alignment);
                    }
                    if (template.getStatusInfoView() != null) {
                        statusInfoView = template.getStatusInfoView().createView(context, cometchatMessageBubble, alignment);
                    }
                    if (template.getFooterView() != null) {
                        footerView = template.getFooterView().createView(context, cometchatMessageBubble, alignment);
                    }
                    // Set all initialized views to the message bubble
                    cometchatMessageBubble.setContentView(contentView);
                    cometchatMessageBubble.setBottomView(bottomView);
                    cometchatMessageBubble.setStatusInfoView(statusInfoView);
                    cometchatMessageBubble.setThreadView(null); // No thread view for center messages
                    cometchatMessageBubble.setLeadingView(null); // No leading view for center messages
                    cometchatMessageBubble.setHeaderView(headerView);
                    cometchatMessageBubble.setFooterView(footerView);
                }
            }
        }

        /**
         * Binds the provided message data to the view holder.
         *
         * @param baseMessage The message data to bind to the view.
         * @param position    The position of the message in the list.
         */
        public void bindBubble(BaseMessage baseMessage, int position) {
            String time = getTimePattern(baseMessage); // Format message time

            if (template != null) {
                if (template.getBubbleView() != null) {
                    // Bind custom bubble view if available
                    template.getBubbleView().bindView(context, bubbleView, baseMessage, alignment, this, baseMessageList, position);
                } else {
                    // Bind content view if available
                    if (template.getContentView() != null) {
                        template.getContentView().bindView(context, contentView, baseMessage, alignment, this, baseMessageList, position);
                    }
                    // Bind bottom view if available
                    if (template.getBottomView() != null) {
                        template.getBottomView().bindView(context, bottomView, baseMessage, alignment, this, baseMessageList, position);
                    }
                    // Bind status info view if available
                    if (template.getStatusInfoView() != null) {
                        template.getStatusInfoView().bindView(context, statusInfoView, baseMessage, alignment, this, baseMessageList, position);
                    }
                    // Bind header view if available
                    if (template.getHeaderView() != null) {
                        template.getHeaderView().bindView(context, headerView, baseMessage, alignment, this, baseMessageList, position);
                    }
                    // Bind footer view if available
                    if (template.getFooterView() != null) {
                        template.getFooterView().bindView(context, footerView, baseMessage, alignment, this, baseMessageList, position);
                    }
                }
            }
        }
    }

    /**
     * ViewHolder for displaying date items in a chat application.
     */
    public class DateItemHolder extends RecyclerView.ViewHolder {
        public CometChatDate txtMessageDate; // Text view for displaying the date

        /**
         * Constructs a DateItemHolder with the specified item view.
         *
         * @param itemView The inflated view for the date item.
         */
        DateItemHolder(@NonNull View itemView) {
            super(itemView);
            txtMessageDate = itemView.findViewById(R.id.txt_message_date); // Initialize date text view
        }
    }
}
