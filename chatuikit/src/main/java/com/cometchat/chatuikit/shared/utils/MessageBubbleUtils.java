package com.cometchat.chatuikit.shared.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.models.Action;
import com.cometchat.chat.models.Attachment;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.formatters.FormatterUtils;
import com.cometchat.chatuikit.shared.models.AdditionParameter;
import com.cometchat.chatuikit.shared.models.CometChatMessageTemplate;
import com.cometchat.chatuikit.shared.models.interactivemessage.CardMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.FormMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.SchedulerMessage;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.views.actionbubble.CometChatActionBubble;
import com.cometchat.chatuikit.shared.views.audiobubble.CometChatAudioBubble;
import com.cometchat.chatuikit.shared.views.avatar.CometChatAvatar;
import com.cometchat.chatuikit.shared.views.cardbubble.CometChatCardBubble;
import com.cometchat.chatuikit.shared.views.date.CometChatDate;
import com.cometchat.chatuikit.shared.views.deletebubble.CometChatDeleteBubble;
import com.cometchat.chatuikit.shared.views.filebubble.CometChatFileBubble;
import com.cometchat.chatuikit.shared.views.formbubble.CometChatFormBubble;
import com.cometchat.chatuikit.shared.views.imagebubble.CometChatImageBubble;
import com.cometchat.chatuikit.shared.views.messagebubble.CometChatMessageBubble;
import com.cometchat.chatuikit.shared.views.messagereceipt.CometChatMessageReceipt;
import com.cometchat.chatuikit.shared.views.messagereceipt.Receipt;
import com.cometchat.chatuikit.shared.views.reaction.CometChatMessageReaction;
import com.cometchat.chatuikit.shared.views.reaction.interfaces.OnAddMoreReactionsClick;
import com.cometchat.chatuikit.shared.views.reaction.interfaces.OnReactionClick;
import com.cometchat.chatuikit.shared.views.reaction.interfaces.OnReactionLongClick;
import com.cometchat.chatuikit.shared.views.schedulerbubble.CometChatSchedulerBubble;
import com.cometchat.chatuikit.shared.views.textbubble.CometChatTextBubble;
import com.cometchat.chatuikit.shared.views.videobubble.CometChatVideoBubble;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class MessageBubbleUtils {
    private static final String TAG = MessageBubbleUtils.class.getSimpleName();

    public static View getHeaderViewContainer(Context context) {
        return View.inflate(context, R.layout.cometchat_top_view, null);
    }

    public static void bindHeaderView(View view,
                                      boolean showName,
                                      String name,
                                      String time,
                                      boolean showTime,
                                      @ColorInt int nameColor,
                                      @StyleRes int nameAppearance,
                                      @StyleRes int dateStyle) {
        if (view != null) {
            TextView nameText = view.findViewById(R.id.cometchat_bubble_header_name_tv);
            CometChatDate cometchatDate = view.findViewById(R.id.cometchat_bubble_header_time);
            nameText.setVisibility(!showName ? View.GONE : View.VISIBLE);
            nameText.setText(name);
            cometchatDate.setCustomDateString(time);
            cometchatDate.setVisibility(showTime ? View.VISIBLE : View.GONE);
            nameText.setMaxLines(1);
            nameText.setEllipsize(TextUtils.TruncateAt.END);
            if (nameColor != 0) nameText.setTextColor(nameColor);
            if (nameAppearance != 0) nameText.setTextAppearance(nameAppearance);
            cometchatDate.setStyle(dateStyle);
        }
    }

    public static View getStatusInfoViewContainer(Context context) {
        return View.inflate(context, R.layout.cometchat_status_info_view, null);
    }

    public static void bindStatusInfoView(View view,
                                          boolean showReceipt,
                                          Receipt receipt,
                                          String time,
                                          boolean showTime,
                                          @StyleRes int receiptStyle,
                                          @StyleRes int dateStyle) {
        if (view != null) {
            CometChatMessageReceipt cometchatMessageReceipt = view.findViewById(R.id.receipt);
            MaterialCardView dateTimeContainer = view.findViewById(R.id.date_time_container);
            CometChatDate cometchatDate = view.findViewById(R.id.date_time);
            Utils.initMaterialCard(dateTimeContainer);
            cometchatMessageReceipt.setMessageReceipt(receipt);
            cometchatDate.setCustomDateString(time);
            cometchatMessageReceipt.setVisibility(showReceipt ? View.VISIBLE : View.GONE);
            cometchatDate.setVisibility(showTime ? View.VISIBLE : View.GONE);
            cometchatMessageReceipt.setStyle(receiptStyle);
            cometchatDate.setStyle(dateStyle);
            @Dimension int strokeWidth = cometchatDate.getDateStrokeWidth();
            @ColorInt int strokeColor = cometchatDate.getStrokeColor();
            @Dimension int cornerRadius = cometchatDate.getDateCornerRadius();
            @ColorInt int backgroundColor = cometchatDate.getDateBackgroundColor();
            cometchatDate.setDateBackgroundColor(Color.TRANSPARENT);
            cometchatDate.setStrokeColor(Color.TRANSPARENT);
            cometchatDate.setDateStrokeWidth(0);
            dateTimeContainer.setStrokeColor(0);
            if (backgroundColor != 0)
                dateTimeContainer.setCardBackgroundColor(Utils.applyColorWithAlphaValue(backgroundColor, 153)); // apply
            // 60%
            // opacity
            // to
            // background
            // color
            dateTimeContainer.setRadius(cornerRadius);
            dateTimeContainer.setStrokeWidth(strokeWidth);
            dateTimeContainer.setStrokeColor(strokeColor);
        }
    }

    @NonNull
    public static View getReactionsViewContainer(Context context, @NonNull UIKitConstants.MessageBubbleAlignment alignment) {
        View view = View.inflate(context, R.layout.cometchat_reaction_layout_container, null);
        LinearLayout reactionLayout = view.findViewById(R.id.cometchat_reaction_layout_parent_container);
        int gravity = alignment.equals(UIKitConstants.MessageBubbleAlignment.RIGHT) ? Gravity.END : Gravity.START;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                                                               LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = gravity;
        reactionLayout.setLayoutParams(layoutParams);
        return view;
    }

    public static void bindReactionsView(View footerView,
                                         BaseMessage baseMessage,
                                         int reactionLimit,
                                         @StyleRes int reactionStyle,
                                         OnReactionClick onReactionClick,
                                         OnReactionLongClick onReactionLongClick,
                                         OnAddMoreReactionsClick onAddMoreReactionsClick
    ) {
        CometChatMessageReaction messageReaction = footerView.findViewById(R.id.cometchat_reaction_view);
        messageReaction.setStyle(reactionStyle);
        messageReaction.setOnReactionClick(onReactionClick);
        messageReaction.setOnReactionLongClick(onReactionLongClick);
        messageReaction.setOnAddMoreReactionsClick(onAddMoreReactionsClick);
        messageReaction.bindReactionsToMessage(baseMessage, reactionLimit);
    }

    public static View getTextContentViewContainer(Context context) {
        View view = View.inflate(context, R.layout.cometchat_text_bubble_container, null);
        setDeletedMessageBubble(context, view);
        return view;
    }

    public static void setDeletedMessageBubble(Context context, View view) {
        CometChatDeleteBubble cometchatDeleteBubble = view.findViewById(R.id.cometchat_delete_text_bubble);
        cometchatDeleteBubble.setText(context.getResources().getString(R.string.cometchat_this_message_deleted));
    }

    public static void bindTextContentView(View view,
                                           TextMessage textMessage,
                                           UIKitConstants.MessageBubbleAlignment alignment,
                                           @StyleRes int textBubbleStyle,
                                           @StyleRes int deleteBubbleStyle,
                                           AdditionParameter additionParameter) {
        if (view != null) {
            CometChatTextBubble cometchatTextBubble = view.findViewById(R.id.text_bubble);
            CometChatDeleteBubble deletedBubble = view.findViewById(R.id.cometchat_delete_text_bubble);
            cometchatTextBubble.setStyle(textBubbleStyle);

            if (textMessage.getDeletedAt() == 0) {
                deletedBubble.setVisibility(View.GONE);
                cometchatTextBubble.setVisibility(View.VISIBLE);
                cometchatTextBubble.setMessage(textMessage, additionParameter.getTextFormatters(), alignment);
            } else {
                cometchatTextBubble.setVisibility(View.GONE);
                deletedBubble.setVisibility(View.VISIBLE);
                deletedBubble.setStyle(deleteBubbleStyle);
            }
        }
    }

    public static View getImageContentViewContainer(Context context) {
        if (context != null) {
            View view = View.inflate(context, R.layout.cometchat_image_bubble_layout_container, null);
            setDeletedMessageBubble(context, view);
            return view;
        }
        return null;
    }

    public static void bindImageContentView(View view,
                                            String thumbnail,
                                            UIKitConstants.MessageBubbleAlignment alignment,
                                            MediaMessage mediaMessage,
                                            @StyleRes int imageBubbleStyle,
                                            @StyleRes int deleteBubbleStyle,
                                            AdditionParameter additionParameter) {
        if (view != null) {
            CometChatImageBubble cometchatImageBubble = view.findViewById(R.id.image_bubble);
            CometChatDeleteBubble deletedBubble = view.findViewById(R.id.cometchat_delete_text_bubble);
            if (mediaMessage.getDeletedAt() == 0) {
                cometchatImageBubble.setStyle(imageBubbleStyle);
                deletedBubble.setVisibility(View.GONE);
                cometchatImageBubble.setVisibility(View.VISIBLE);
                File file = Utils.getFileFromLocalPath(mediaMessage);
                cometchatImageBubble.setMessage(mediaMessage, file);
                if (file == null && (thumbnail != null && !thumbnail.isEmpty()))
                    cometchatImageBubble.setImageThumbnail(thumbnail);
                if (mediaMessage.getCaption() != null) {
                    cometchatImageBubble.setCaption(SpannableString.valueOf(FormatterUtils.getFormattedText(view.getContext(),
                                                                                                            mediaMessage,
                                                                                                            UIKitConstants.FormattingType.MESSAGE_BUBBLE,
                                                                                                            alignment,
                                                                                                            mediaMessage.getCaption(),
                                                                                                            additionParameter != null && additionParameter.getTextFormatters() != null ? additionParameter.getTextFormatters() : new ArrayList<>())));
                }
            } else {
                cometchatImageBubble.setVisibility(View.GONE);
                deletedBubble.setVisibility(View.VISIBLE);
                deletedBubble.setStyle(deleteBubbleStyle);
            }
        }
    }

    public static View getVideoContentViewContainer(Context context) {
        if (context != null) {
            View view = View.inflate(context, R.layout.cometchat_video_bubble_layout_container, null);
            setDeletedMessageBubble(context, view);
            return view;
        }
        return null;
    }

    public static void bindVideoContentView(View view,
                                            String thumbnailUrl,
                                            MediaMessage mediaMessage,
                                            @StyleRes int videoBubbleStyle,
                                            @StyleRes int deleteBubbleStyle,
                                            AdditionParameter additionParameter) {
        if (view != null) {
            CometChatVideoBubble cometchatVideoBubble = view.findViewById(R.id.video_bubble);
            CometChatDeleteBubble deletedBubble = view.findViewById(R.id.cometchat_delete_text_bubble);
            if (mediaMessage.getDeletedAt() != 0) {
                deletedBubble.setVisibility(View.VISIBLE);
                cometchatVideoBubble.setVisibility(View.GONE);
                deletedBubble.setStyle(deleteBubbleStyle);
            } else {
                cometchatVideoBubble.setStyle(videoBubbleStyle);
                deletedBubble.setVisibility(View.GONE);
                cometchatVideoBubble.setVisibility(View.VISIBLE);
                Attachment attachment = mediaMessage.getAttachment();
                File file = Utils.getFileFromLocalPath(mediaMessage);
                cometchatVideoBubble.setVideoUrl(file, attachment != null ? attachment.getFileUrl() : "");
                if (thumbnailUrl != null && !thumbnailUrl.isEmpty())
                    cometchatVideoBubble.setThumbnailUrl(thumbnailUrl, 0);
            }
        }
    }

    public static View getFileContentViewContainer(Context context) {
        if (context != null) {
            View view = View.inflate(context, R.layout.cometchat_file_bubble_layout_container, null);
            setDeletedMessageBubble(context, view);
            return view;
        }
        return null;
    }

    public static void bindFileContentView(Context context,
                                           View view,
                                           MediaMessage mediaMessage,
                                           @StyleRes int fileBubbleStyle,
                                           @StyleRes int deleteBubbleStyle,
                                           AdditionParameter additionParameter) {
        if (view != null) {
            CometChatFileBubble cometchatFileBubble = view.findViewById(R.id.file_bubble);
            CometChatDeleteBubble deletedBubble = view.findViewById(R.id.cometchat_delete_text_bubble);
            if (mediaMessage.getDeletedAt() != 0) {
                cometchatFileBubble.setVisibility(View.GONE);
                deletedBubble.setVisibility(View.VISIBLE);
                deletedBubble.setStyle(deleteBubbleStyle);
            } else {
                cometchatFileBubble.setMessage(mediaMessage);
                cometchatFileBubble.setStyle(fileBubbleStyle);
                cometchatFileBubble.setVisibility(View.VISIBLE);
                deletedBubble.setVisibility(View.GONE);
            }
        }
    }

    public static View getAudioContentViewContainer(Context context) {
        if (context != null) {
            View view = View.inflate(context, R.layout.cometchat_audio_bubble_layout_container, null);
            setDeletedMessageBubble(context, view);
            return view;
        }
        return null;
    }

    public static void bindAudioContentView(Context context,
                                            View view,
                                            MediaMessage mediaMessage,
                                            @StyleRes int audioBubbleStyle,
                                            @StyleRes int deleteBubbleStyle,
                                            AdditionParameter additionParameter) {
        if (view != null) {
            CometChatAudioBubble cometchatAudioBubble = view.findViewById(R.id.audio_bubble);
            CometChatDeleteBubble deletedBubble = view.findViewById(R.id.cometchat_delete_text_bubble);
            if (mediaMessage.getDeletedAt() != 0) {
                cometchatAudioBubble.setVisibility(View.GONE);
                deletedBubble.setVisibility(View.VISIBLE);
                deletedBubble.setStyle(deleteBubbleStyle);
            } else {
                cometchatAudioBubble.setStyle(audioBubbleStyle);
                cometchatAudioBubble.setVisibility(View.VISIBLE);
                deletedBubble.setVisibility(View.GONE);
                cometchatAudioBubble.setMessage(mediaMessage);
            }
        }
    }

    public static View getActionContentViewContainer(Context context) {
        return View.inflate(context, R.layout.cometchat_action_bubble_layout, null);
    }

    public static void bindActionContentView(Context context, View view, String text, Action action, @StyleRes int actionBubbleStyle) {
        if (view != null) {
            CometChatActionBubble cometchatActionBubble = view.findViewById(R.id.cometchat_action_bubble);
            String actionMessage = getActionMessage(context, text, action);
            cometchatActionBubble.setText(actionMessage);
            cometchatActionBubble.setStyle(actionBubbleStyle);
        }
    }

    @Nullable
    public static String getActionMessage(Context context, String text, Action action) {
        String actionMessage = "";
        if (text == null && action != null) {
            String actionType = action.getAction();
            switch (actionType) {
                case CometChatConstants.ActionKeys.ACTION_JOINED:
                    actionMessage = ((User) action.getActionBy()).getName() + " " + context.getString(R.string.cometchat_joined);
                    break;
                case CometChatConstants.ActionKeys.ACTION_MEMBER_ADDED:
                    actionMessage = ((User) action.getActionBy()).getName() + " " + context.getString(R.string.cometchat_added) + " " + ((User) action.getActionOn()).getName();
                    break;
                case CometChatConstants.ActionKeys.ACTION_KICKED:
                    actionMessage = ((User) action.getActionBy()).getName() + " " + context.getString(R.string.cometchat_kicked_by) + " " + ((User) action.getActionOn()).getName();
                    break;
                case CometChatConstants.ActionKeys.ACTION_BANNED:
                    actionMessage = ((User) action.getActionBy()).getName() + " " + context.getString(R.string.cometchat_banned) + " " + ((User) action.getActionOn()).getName();
                    break;
                case CometChatConstants.ActionKeys.ACTION_UNBANNED:
                    actionMessage = ((User) action.getActionBy()).getName() + " " + context.getString(R.string.cometchat_unban) + " " + ((User) action.getActionOn()).getName();
                    break;
                case CometChatConstants.ActionKeys.ACTION_LEFT:
                    actionMessage = ((User) action.getActionBy()).getName() + " " + context.getString(R.string.cometchat_left);
                    break;
                case CometChatConstants.ActionKeys.ACTION_SCOPE_CHANGED:
                    String newScope = action.getNewScope();
                    switch (newScope.toLowerCase()) {
                        case CometChatConstants.SCOPE_MODERATOR:
                            actionMessage = ((User) action.getActionBy()).getName() + " " + context.getString(R.string.cometchat_made) + " " + ((User) action.getActionOn()).getName() + " " + context.getString(
                                R.string.cometchat_moderator);
                            break;
                        case CometChatConstants.SCOPE_ADMIN:
                            actionMessage = ((User) action.getActionBy()).getName() + " " + context.getString(R.string.cometchat_made) + " " + ((User) action.getActionOn()).getName() + " " + context.getString(
                                R.string.cometchat_admin);
                            break;
                        case CometChatConstants.SCOPE_PARTICIPANT:
                            actionMessage = ((User) action.getActionBy()).getName() + " " + context.getString(R.string.cometchat_made) + " " + ((User) action.getActionOn()).getName() + " " + context.getString(
                                R.string.cometchat_participant);
                            break;
                        default:
                            actionMessage = action.getMessage();
                            break;
                    }
                    break;
                default:
                    break;
            }
        } else actionMessage = text;
        return actionMessage;
    }

    public static View getAvatarLeadingViewContainer(Context context, UIKitConstants.MessageBubbleAlignment alignment) {
        if (context != null) {
            View view = View.inflate(context, R.layout.cometchat_avatar_leading_view, null);
            CometChatAvatar avatar = view.findViewById(R.id.avatar);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) avatar.getLayoutParams();
            if (alignment.equals(UIKitConstants.MessageBubbleAlignment.RIGHT)) {
                layoutParams.setMargins(context.getResources().getDimensionPixelSize(R.dimen.cometchat_padding_2), 0, 0, 0);
            } else if (alignment.equals(UIKitConstants.MessageBubbleAlignment.LEFT)) {
                layoutParams.setMargins(0, 0, context.getResources().getDimensionPixelSize(R.dimen.cometchat_padding_2), 0);
            }
            avatar.setLayoutParams(layoutParams);
            return view;
        }
        return null;
    }

    public static void bindAvatarLeadingView(View view, boolean showAvatar, String url, String name, @StyleRes int avatarStyle) {
        if (view != null) {
            CometChatAvatar avatar = view.findViewById(R.id.avatar);
            avatar.setVisibility(showAvatar ? View.VISIBLE : View.GONE);
            avatar.setStyle(avatarStyle);
            avatar.setAvatar(name, url);
        }
    }

    public static View getThreadViewContainer(Context context) {
        if (context != null) {
            return View.inflate(context, R.layout.cometchat_thread_view, null);
        }
        return null;
    }

    public static void bindThreadView(View view,
                                      BaseMessage baseMessage,
                                      String text,
                                      UIKitConstants.MessageBubbleAlignment iconAlignment,
                                      @ColorInt int textColor,
                                      @StyleRes int textAppearance,
                                      @ColorInt int iconTint,
                                      @DrawableRes int icon) {
        if (view != null) {
            TextView textView = view.findViewById(R.id.cometchat_thread_reply_count);
            ImageView imageView;
            imageView = view.findViewById(R.id.cometchat_thread_left_image);
            imageView.setVisibility(View.VISIBLE);
            if (textAppearance != 0) textView.setTextAppearance(textAppearance);
            if (textColor != 0) textView.setTextColor(textColor);
            if (iconTint != 0) imageView.setImageTintList(ColorStateList.valueOf(iconTint));
            if (icon != 0) imageView.setImageResource(icon);
            if (baseMessage != null && text == null && baseMessage.getDeletedAt() == 0 && baseMessage.getReplyCount() > 0) {
                if (baseMessage.getReplyCount() == 1)
                    textView.setText(baseMessage.getReplyCount() + " " + view.getContext().getResources().getString(R.string.cometchat_reply));
                else
                    textView.setText(baseMessage.getReplyCount() + " " + view.getContext().getResources().getString(R.string.cometchat_replies));
            }
            if (text != null && !text.isEmpty()) textView.setText(text);
        }
    }

    public static CometChatMessageBubble getMessageBubble(Context context,
                                                          BaseMessage baseMessage,
                                                          CometChatMessageTemplate template,
                                                          UIKitConstants.MessageBubbleAlignment alignment) {
        CometChatMessageBubble messageBubble = new CometChatMessageBubble(context);
        messageBubble.setMessageAlignment(alignment);

        View bubbleView = template.getContentView().createView(context, messageBubble, UIKitConstants.MessageBubbleAlignment.RIGHT);
        template
            .getContentView()
            .bindView(context, bubbleView, baseMessage, UIKitConstants.MessageBubbleAlignment.RIGHT, null, Collections.singletonList(baseMessage), 0);
        messageBubble.setContentView(bubbleView);
        return messageBubble;
    }

    public static View getFormBubbleContainer(@NonNull Context context, UIKitConstants.MessageBubbleAlignment alignment) {
        View view = View.inflate(context, R.layout.cometchat_form_message_bubble_layout_container, null);
        setNotSupportedMessageBubble(context, view);
        return view;
    }

    public static void setNotSupportedMessageBubble(Context context, View view) {
        CometChatDeleteBubble cometchatDeleteBubble = view.findViewById(R.id.cometchat_delete_text_bubble);
        cometchatDeleteBubble.setText(context.getResources().getString(R.string.cometchat_this_message_type_is_not_supported));
    }

    public static void bindFormBubble(View view,
                                      FormMessage formMessage,
                                      @StyleRes int formBubbleStyle,
                                      @StyleRes int deleteBubbleStyle,
                                      AdditionParameter additionParameter) {
        if (view != null) {
            CometChatFormBubble messageBubble = view.findViewById(R.id.cometchat_form_bubble_container);
            CometChatDeleteBubble deletedBubble = view.findViewById(R.id.cometchat_delete_text_bubble);
            messageBubble.setVisibility(View.GONE);
            deletedBubble.setVisibility(View.VISIBLE);
            deletedBubble.setStyle(deleteBubbleStyle);
            /*
             * if (formMessage.getDeletedAt() == 0) {
             * deletedBubble.setVisibility(View.GONE);
             * messageBubble.setVisibility(View.VISIBLE);
             * messageBubble.setFormMessage(formMessage); } else {
             * messageBubble.setVisibility(View.GONE);
             * deletedBubble.setVisibility(View.VISIBLE); }
             */
        }
    }

    public static View getSchedulerBubbleContainer(Context context, UIKitConstants.MessageBubbleAlignment alignment) {
        View view = View.inflate(context, R.layout.cometchat_meeting_message_bubble_layout_container, null);
        setNotSupportedMessageBubble(context, view);
        return view;
    }

    public static void bindSchedulerBubble(@Nullable View view,
                                           SchedulerMessage schedulerMessage,
                                           @StyleRes int schedulerBubbleStyle,
                                           @StyleRes int deleteBubbleStyle,
                                           AdditionParameter additionParameter) {
        if (view != null) {
            CometChatSchedulerBubble messageBubble = view.findViewById(R.id.cometchat_meeting_bubble_container);
            CometChatDeleteBubble deletedBubble = view.findViewById(R.id.cometchat_delete_text_bubble);
            messageBubble.setVisibility(View.GONE);
            deletedBubble.setVisibility(View.VISIBLE);
            deletedBubble.setStyle(deleteBubbleStyle);
            /*
             * if (schedulerMessage.getDeletedAt() == 0) {
             * messageBubble.setVisibility(View.VISIBLE);
             * deletedBubble.setVisibility(View.GONE);
             * messageBubble.setSchedulerMessage(schedulerMessage); } else {
             * messageBubble.setVisibility(View.GONE);
             * deletedBubble.setVisibility(View.VISIBLE); }
             */
        }
    }

    public static View getCardBubbleContainer(Context context, UIKitConstants.MessageBubbleAlignment alignment) {
        View view = View.inflate(context, R.layout.cometchat_card_message_bubble_layout_container, null);
        setNotSupportedMessageBubble(context, view);
        return view;
    }

    public static void bindCardBubble(View view,
                                      CardMessage cardMessage,
                                      @StyleRes int cardBubbleStyle,
                                      @StyleRes int deleteBubbleStyle,
                                      AdditionParameter additionParameter) {
        if (view != null) {
            CometChatCardBubble messageBubble = view.findViewById(R.id.cc_card_bubble);
            CometChatDeleteBubble deletedBubble = view.findViewById(R.id.cometchat_delete_text_bubble);
            messageBubble.setVisibility(View.GONE);
            deletedBubble.setVisibility(View.VISIBLE);
            deletedBubble.setStyle(deleteBubbleStyle);
            /*
             * if (cardMessage.getDeletedAt() == 0) {
             * deletedBubble.setVisibility(View.GONE);
             * messageBubble.setVisibility(View.VISIBLE);
             * messageBubble.setCardMessage(cardMessage); } else {
             * messageBubble.setVisibility(View.GONE);
             * deletedBubble.setVisibility(View.VISIBLE); }
             */
        }
    }
}
