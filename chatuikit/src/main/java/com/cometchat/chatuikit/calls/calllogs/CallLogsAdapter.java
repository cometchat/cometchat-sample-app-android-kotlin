package com.cometchat.chatuikit.calls.calllogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.calls.constants.CometChatCallsConstants;
import com.cometchat.calls.model.CallLog;
import com.cometchat.calls.model.CallUser;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.calls.utils.CallUtils;
import com.cometchat.chatuikit.databinding.CometchatCallLogsItemsBinding;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.interfaces.DateTimeFormatterCallback;
import com.cometchat.chatuikit.shared.interfaces.Function2;
import com.cometchat.chatuikit.shared.interfaces.OnItemClick;
import com.cometchat.chatuikit.shared.interfaces.OnItemLongClick;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.viewholders.CallLogsViewHolderListener;
import com.cometchat.chatuikit.shared.views.date.CometChatDate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The CallLogsAdapter class is an adapter for the RecyclerView that manages and
 * displays a list of call logs within a chat application. It extends the
 * RecyclerView.Adapter class and provides the necessary methods to bind call
 * log data to the RecyclerView's view holders. The adapter handles the
 * creation, binding, and recycling of view holders, allowing for efficient
 * display of call log entries. It also supports custom views and click
 * listeners for each call log item, enhancing user interaction.
 *
 * <p>
 * Created on: 22 October 2024 Modified on: 22 October 2024
 */
public class CallLogsAdapter extends RecyclerView.Adapter<CallLogsAdapter.CallLogsViewHolder> {
    private static final String TAG = CallLogsAdapter.class.getSimpleName();
    private final Context context;

    private List<CallLog> callLogs;

    private HashMap<CallLog, Boolean> selectedCalls;

    private Function2<Context, CallLog, View> subtitle, tail, customView;

    private OnItemClick<CallLog> itemClickListener;
    private CometChatCallLogs.OnCallIconClick callIconClickListener;
    private OnItemLongClick<CallLog> itemLongClickListener;


    // Call Logs Item Text Appearance and Colors
    private @StyleRes int itemTitleTextAppearance;
    private @ColorInt int itemTitleTextColor;
    private @StyleRes int itemSubtitleTextAppearance;
    private @ColorInt int itemSubtitleTextColor;

    // Incoming Call Icons and Tints
    private @Nullable Drawable itemIncomingCallIcon;
    private @ColorInt int itemIncomingCallIconTint;

    // Outgoing Call Icons and Tints
    private @Nullable Drawable itemOutgoingCallIcon;
    private @ColorInt int itemOutgoingCallIconTint;

    // Missed Call Icons and Tints
    private @ColorInt int itemMissedCallTitleColor;
    private @Nullable Drawable itemMissedCallIcon;
    private @ColorInt int itemMissedCallIconTint;

    // Audio and Video Call Icons and Tints
    private @Nullable Drawable itemAudioCallIcon;
    private @ColorInt int itemAudioCallIconTint;
    private @Nullable Drawable itemVideoCallIcon;
    private @ColorInt int itemVideoCallIconTint;
    private CallLogsViewHolderListener subtitleView, titleView, leadingView, trailingView, listItemView;

    // Avatar Style and Date Style
    private @StyleRes int avatarStyle;
    private @StyleRes int dateStyle;
    private SimpleDateFormat simpleDateFormat;
    private DateTimeFormatterCallback dateTimeFormatter;

    /**
     * Creates a new instance of the CallLogsAdapter.
     *
     * @param context The context in which the adapter is operating. This is typically
     *                an Activity or Fragment context. Initializes the base message list
     *                as an empty ArrayList and selects the base message using an empty
     *                HashMap.
     */
    public CallLogsAdapter(Context context) {
        this.context = context;
        callLogs = new ArrayList<>();
        selectBaseMessage(new HashMap<>());
    }

    /**
     * Selects the base message based on the provided HashMap.
     *
     * @param hashMap A HashMap mapping CallLog items to their selected states. If the
     *                HashMap is not null, it updates the selectedCalls and notifies the
     *                adapter.
     */
    public void selectBaseMessage(HashMap<CallLog, Boolean> hashMap) {
        if (hashMap != null) {
            this.selectedCalls = hashMap;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public CallLogsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CometchatCallLogsItemsBinding binding = CometchatCallLogsItemsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CallLogsViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull CallLogsViewHolder holder, @SuppressLint("RecyclerView") int position) {
        CallLog callLog = callLogs.get(position);
        holder.bindView(callLog, position);
    }

    @Override
    public int getItemCount() {
        return callLogs.size();
    }

    /**
     * Gets the custom view for the given CallLog item.
     *
     * @param callLog The CallLog item for which the custom view is to be retrieved.
     * @return The View representing the custom view, or null if no custom view
     * function is set.
     */
    private View getCustomView(CallLog callLog) {
        if (customView != null) return customView.apply(context, callLog);
        return null;
    }

    /**
     * Sets up the avatar for the given CallLog item in the provided ViewHolder.
     *
     * @param holder  The ViewHolder that holds the views for the CallLog item.
     * @param callLog The CallLog item containing user details such as name and avatar.
     */
    private void setupAvatar(CallLogsViewHolder holder, CallLog callLog) {
        String name = CallUtils.getCallLogUserName(callLog);
        String avatar = CallUtils.getCallLogUserAvatar(callLog);
        holder.binding.avatar.setAvatar(name, avatar);
        holder.binding.avatar.setStyle(avatarStyle);
    }

    /**
     * Sets up the title view for the given CallLog item in the provided ViewHolder.
     *
     * @param holder  The ViewHolder that holds the views for the CallLog item.
     * @param callLog The CallLog item containing the user's name.
     */
    private void setupTitle(CallLogsViewHolder holder, CallLog callLog) {
        String name = CallUtils.getCallLogUserName(callLog);
        holder.binding.tvTitle.setText(name);
        holder.binding.tvTitle.setTextAppearance(itemTitleTextAppearance);
        holder.binding.tvTitle.setTextColor(itemTitleTextColor);
    }

    public void setSubtitleView(CallLogsViewHolderListener subtitleView) {
        this.subtitleView = subtitleView;
        notifyDataSetChanged();
    }

    public void setTitleView(CallLogsViewHolderListener titleView) {
        this.titleView = titleView;
        notifyDataSetChanged();
    }

    public void setLeadingView(CallLogsViewHolderListener leadingView) {
        this.leadingView = leadingView;
        notifyDataSetChanged();
    }

    public void setTrailingView(CallLogsViewHolderListener trailingView) {
        this.trailingView = trailingView;
        notifyDataSetChanged();
    }

    public void setItemView(CallLogsViewHolderListener itemView) {
        this.listItemView = itemView;
        notifyDataSetChanged();
    }

    public void setCallIconClickListener(CometChatCallLogs.OnCallIconClick callIconClickListener) {
        this.callIconClickListener = callIconClickListener;
    }

    public void setItemClickListener(OnItemClick<CallLog> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setItemLongClickListener(OnItemLongClick<CallLog> itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    /**
     * Sets the base message list for the adapter.
     *
     * @param list The list of CallLog items to be set as the base message list. If
     *             the list is not null, it updates the baseMessageList and notifies
     *             the adapter.
     */
    public void setCallLogs(List<CallLog> list) {
        if (list != null) {
            this.callLogs = list;
            notifyDataSetChanged();
        }
    }

    /**
     * Gets the context associated with the adapter.
     *
     * @return The context used by the adapter.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Gets the currently selected calls.
     *
     * @return A HashMap containing the CallLog items and their selected states.
     */
    public HashMap<CallLog, Boolean> getSelectedCalls() {
        return selectedCalls;
    }

    /**
     * Returns the item title text appearance.
     *
     * @return The resource ID of the text appearance style for the item title.
     */
    public @StyleRes int getItemTitleTextAppearance() {
        return itemTitleTextAppearance;
    }

    /**
     * Sets the item title text appearance.
     *
     * @param itemTitleTextAppearance The resource ID of the text appearance style for the item title.
     */
    public void setItemTitleTextAppearance(@StyleRes int itemTitleTextAppearance) {
        this.itemTitleTextAppearance = itemTitleTextAppearance;
        notifyDataSetChanged();
    }

    /**
     * Returns the item title text color.
     *
     * @return The color of the item title text.
     */
    public @ColorInt int getItemTitleTextColor() {
        return itemTitleTextColor;
    }

    /**
     * Sets the item title text color.
     *
     * @param itemTitleTextColor The color of the item title text.
     */
    public void setItemTitleTextColor(@ColorInt int itemTitleTextColor) {
        this.itemTitleTextColor = itemTitleTextColor;
        notifyDataSetChanged();
    }

    /**
     * Returns the item subtitle text appearance.
     *
     * @return The resource ID of the text appearance style for the item subtitle.
     */
    public @StyleRes int getItemSubtitleTextAppearance() {
        return itemSubtitleTextAppearance;
    }

    /**
     * Sets the item subtitle text appearance.
     *
     * @param itemSubtitleTextAppearance The resource ID of the text appearance style for the item
     *                                   subtitle.
     */
    public void setItemSubtitleTextAppearance(@StyleRes int itemSubtitleTextAppearance) {
        this.itemSubtitleTextAppearance = itemSubtitleTextAppearance;
        notifyDataSetChanged();
    }

    /**
     * Returns the item subtitle text color.
     *
     * @return The color of the item subtitle text.
     */
    public @ColorInt int getItemSubtitleTextColor() {
        return itemSubtitleTextColor;
    }

    /**
     * Sets the item subtitle text color.
     *
     * @param itemSubtitleTextColor The color of the item subtitle text.
     */
    public void setItemSubtitleTextColor(@ColorInt int itemSubtitleTextColor) {
        this.itemSubtitleTextColor = itemSubtitleTextColor;
        notifyDataSetChanged();
    }

    /**
     * Returns the incoming call icon.
     *
     * @return The drawable for the incoming call icon.
     */
    public @Nullable Drawable getItemIncomingCallIcon() {
        return itemIncomingCallIcon;
    }

    /**
     * Sets the incoming call icon.
     *
     * @param itemIncomingCallIcon The drawable for the incoming call icon.
     */
    public void setItemIncomingCallIcon(@Nullable Drawable itemIncomingCallIcon) {
        this.itemIncomingCallIcon = itemIncomingCallIcon;
        notifyDataSetChanged();
    }

    /**
     * Returns the tint for the incoming call icon.
     *
     * @return The tint color of the incoming call icon.
     */
    public @ColorInt int getItemIncomingCallIconTint() {
        return itemIncomingCallIconTint;
    }

    /**
     * Sets the tint for the incoming call icon.
     *
     * @param itemIncomingCallIconTint The tint color for the incoming call icon.
     */
    public void setItemIncomingCallIconTint(@ColorInt int itemIncomingCallIconTint) {
        this.itemIncomingCallIconTint = itemIncomingCallIconTint;
        notifyDataSetChanged();
    }

    /**
     * Returns the outgoing call icon.
     *
     * @return The drawable for the outgoing call icon.
     */
    public @Nullable Drawable getItemOutgoingCallIcon() {
        return itemOutgoingCallIcon;
    }

    /**
     * Sets the outgoing call icon.
     *
     * @param itemOutgoingCallIcon The drawable for the outgoing call icon.
     */
    public void setItemOutgoingCallIcon(@Nullable Drawable itemOutgoingCallIcon) {
        this.itemOutgoingCallIcon = itemOutgoingCallIcon;
        notifyDataSetChanged();
    }

    public @ColorInt int getItemMissedCallTitleColor() {
        return itemMissedCallTitleColor;
    }

    public void setItemMissedCallTitleColor(@ColorInt int itemMissedCallTitleColor) {
        this.itemMissedCallTitleColor = itemMissedCallTitleColor;
        notifyDataSetChanged();
    }

    /**
     * Returns the tint for the outgoing call icon.
     *
     * @return The tint color of the outgoing call icon.
     */
    public @ColorInt int getItemOutgoingCallIconTint() {
        return itemOutgoingCallIconTint;
    }

    /**
     * Sets the tint for the outgoing call icon.
     *
     * @param itemOutgoingCallIconTint The tint color for the outgoing call icon.
     */
    public void setItemOutgoingCallIconTint(@ColorInt int itemOutgoingCallIconTint) {
        this.itemOutgoingCallIconTint = itemOutgoingCallIconTint;
        notifyDataSetChanged();
    }

    /**
     * Returns the missed call icon.
     *
     * @return The drawable for the missed call icon.
     */
    public @Nullable Drawable getItemMissedCallIcon() {
        return itemMissedCallIcon;
    }

    /**
     * Sets the missed call icon.
     *
     * @param itemMissedCallIcon The drawable for the missed call icon.
     */
    public void setItemMissedCallIcon(@Nullable Drawable itemMissedCallIcon) {
        this.itemMissedCallIcon = itemMissedCallIcon;
        notifyDataSetChanged();
    }

    /**
     * Returns the tint for the missed call icon.
     *
     * @return The tint color of the missed call icon.
     */
    public @ColorInt int getItemMissedCallIconTint() {
        return itemMissedCallIconTint;
    }

    /**
     * Sets the tint for the missed call icon.
     *
     * @param itemMissedCallIconTint The tint color for the missed call icon.
     */
    public void setItemMissedCallIconTint(@ColorInt int itemMissedCallIconTint) {
        this.itemMissedCallIconTint = itemMissedCallIconTint;
        notifyDataSetChanged();
    }

    /**
     * Returns the audio call icon.
     *
     * @return The drawable for the audio call icon.
     */
    public @Nullable Drawable getItemAudioCallIcon() {
        return itemAudioCallIcon;
    }

    /**
     * Sets the audio call icon.
     *
     * @param itemAudioCallIcon The drawable for the audio call icon.
     */
    public void setItemAudioCallIcon(@Nullable Drawable itemAudioCallIcon) {
        this.itemAudioCallIcon = itemAudioCallIcon;
        notifyDataSetChanged();
    }

    /**
     * Returns the tint for the audio call icon.
     *
     * @return The tint color of the audio call icon.
     */
    public @ColorInt int getItemAudioCallIconTint() {
        return itemAudioCallIconTint;
    }

    /**
     * Sets the tint for the audio call icon.
     *
     * @param itemAudioCallIconTint The tint color for the audio call icon.
     */
    public void setItemAudioCallIconTint(@ColorInt int itemAudioCallIconTint) {
        this.itemAudioCallIconTint = itemAudioCallIconTint;
        notifyDataSetChanged();
    }

    /**
     * Returns the video call icon.
     *
     * @return The drawable for the video call icon.
     */
    public @Nullable Drawable getItemVideoCallIcon() {
        return itemVideoCallIcon;
    }

    /**
     * Sets the video call icon.
     *
     * @param itemVideoCallIcon The drawable for the video call icon.
     */
    public void setItemVideoCallIcon(@Nullable Drawable itemVideoCallIcon) {
        this.itemVideoCallIcon = itemVideoCallIcon;
        notifyDataSetChanged();
    }

    /**
     * Returns the tint for the video call icon.
     *
     * @return The tint color of the video call icon.
     */
    public @ColorInt int getItemVideoCallIconTint() {
        return itemVideoCallIconTint;
    }

    /**
     * Sets the tint for the video call icon.
     *
     * @param itemVideoCallIconTint The tint color for the video call icon.
     */
    public void setItemVideoCallIconTint(@ColorInt int itemVideoCallIconTint) {
        this.itemVideoCallIconTint = itemVideoCallIconTint;
        notifyDataSetChanged();
    }

    /**
     * Returns the avatar style.
     *
     * @return The resource ID of the avatar style.
     */
    public @StyleRes int getAvatarStyle() {
        return avatarStyle;
    }

    /**
     * Sets the avatar style.
     *
     * @param avatarStyle The resource ID of the avatar style.
     */
    public void setAvatarStyle(@StyleRes int avatarStyle) {
        this.avatarStyle = avatarStyle;
        notifyDataSetChanged();
    }

    /**
     * Returns the date style.
     *
     * @return The resource ID of the date style.
     */
    public @StyleRes int getDateStyle() {
        return dateStyle;
    }

    /**
     * Sets the date style.
     *
     * @param dateStyle The resource ID of the date style.
     */
    public void setDateStyle(@StyleRes int dateStyle) {
        this.dateStyle = dateStyle;
        notifyDataSetChanged();
    }

    public void setDateFormat(SimpleDateFormat simpleDateFormat) {
        this.simpleDateFormat = simpleDateFormat;
        notifyDataSetChanged();
    }

    public void setDateTimeFormatter(DateTimeFormatterCallback dateTimeFormatter) {
        if (dateTimeFormatter != null) {
            this.dateTimeFormatter = dateTimeFormatter;
            notifyDataSetChanged();
        }
    }

    /**
     * ViewHolder for the CallLogsAdapter, holding the view references for a single
     * CallLog item. This class extends RecyclerView.ViewHolder and is used to
     * efficiently display each item in the list.
     */
    public class CallLogsViewHolder extends RecyclerView.ViewHolder {
        private final CometchatCallLogsItemsBinding binding;
        private View customItemView, customTitleView, customSubtitleView, customLeadingView, customTrailingView;

        /**
         * Constructor for the CallLogsViewHolder.
         *
         * @param item The view for a single item in the RecyclerView. It binds the view
         *             to the corresponding layout defined in the binding class.
         */
        public CallLogsViewHolder(@NonNull View item) {
            super(item);
            binding = CometchatCallLogsItemsBinding.bind(item);
            if (listItemView != null) {
                customItemView = listItemView.createView(context, binding);
                Utils.handleView(binding.parentView, customItemView, true);
            } else {
                if (leadingView != null) {
                    customLeadingView = leadingView.createView(context, binding);
                    Utils.handleView(binding.avatarContainer, customLeadingView, true);
                }

                if (titleView != null) {
                    customTitleView = titleView.createView(context, binding);
                    Utils.handleView(binding.titleContainer, customTitleView, true);
                }
                if (subtitleView != null) {
                    customSubtitleView = subtitleView.createView(context, binding);
                    Utils.handleView(binding.subtitleContainer, customSubtitleView, true);
                }
                if (trailingView != null) {
                    customTrailingView = trailingView.createView(context, binding);
                    Utils.handleView(binding.tailView, customTrailingView, true);
                }
            }

        }

        public void bindView(CallLog callLog, int position) {

            View customView = getCustomView(callLog);

            if (listItemView == null) {
                if (leadingView != null) {
                    leadingView.bindView(context, customLeadingView, callLog, this, callLogs, position);
                } else {
                    setupAvatar(this, callLog);
                }

                if (titleView == null) {
                    setupTitle(this, callLog);
                } else {
                    titleView.bindView(context, customTitleView, callLog, this, callLogs, position);
                }

                if (subtitleView == null) {
                    setupSubtitleView(this, callLog);
                } else {
                    subtitleView.bindView(context, customSubtitleView, callLog, this, callLogs, position);
                }

                if (trailingView == null) {
                    setupTailView(this, callLog, position);
                } else {
                    trailingView.bindView(context, customTrailingView, callLog, this, callLogs, position);
                }
            } else {
                listItemView.bindView(context, customItemView, callLog, this, callLogs, position);
                binding.parentView.addView(customView);
            }

            setupClickListeners(this, position, callLog);
        }

        /**
         * Sets up the subtitle view for the given CallLog item in the provided
         * ViewHolder.
         *
         * <p>
         * This method removes any existing views in the subtitle view and adds a new
         * ImageView and a date view based on the details of the CallLog item.
         *
         * @param holder  The ViewHolder that holds the views for the CallLog item.
         * @param callLog The CallLog item containing the call details such as type,
         *                initiator, and status.
         */
        private void setupSubtitleView(CallLogsViewHolder holder, CallLog callLog) {
            holder.binding.subtitleView.removeAllViews();
            ImageView imageView = new ImageView(context);
            CometChatDate cometchatDate = createCometChatDate(callLog.getInitiatedAt());

            if (callLog.getInitiator() instanceof CallUser) {
                CallUser initiator = (CallUser) callLog.getInitiator();
                boolean isLoggedInUser = CallUtils.isLoggedInUser(initiator);
                boolean isMissedOrUnanswered = callLog.getStatus().equals(CometChatCallsConstants.CALL_STATUS_UNANSWERED) || callLog
                    .getStatus()
                    .equals(CometChatCallsConstants.CALL_STATUS_MISSED);

                if (callLog.getType().equals(CometChatCallsConstants.CALL_TYPE_AUDIO) || callLog
                    .getType()
                    .equals(CometChatCallsConstants.CALL_TYPE_VIDEO) || callLog.getType().equals(CometChatCallsConstants.CALL_TYPE_AUDIO_VIDEO)) {

                    if (isLoggedInUser) {
                        setupCallIcon(holder, imageView, cometchatDate, itemOutgoingCallIcon, itemOutgoingCallIconTint);
                    } else if (isMissedOrUnanswered) {
                        setupMissedCall(holder, imageView, cometchatDate);
                    } else {
                        setupCallIcon(holder, imageView, cometchatDate, itemIncomingCallIcon, itemIncomingCallIconTint);
                    }
                }
            }
        }

        /**
         * Sets up the tail view for the given CallLog item.
         *
         * @param holder   The ViewHolder that holds the views for the CallLog item.
         * @param callLog  The CallLog item associated with the ViewHolder.
         * @param position The position of the CallLog item in the list.
         */
        private void setupTailView(CallLogsViewHolder holder, CallLog callLog, int position) {
            holder.binding.tailView.removeAllViews();
            ImageView imageView = new ImageView(context);

            if (callLog.getType().equals(CometChatCallsConstants.CALL_TYPE_AUDIO)) {
                imageView.setBackground(itemAudioCallIcon);
                imageView.setBackgroundTintList(ColorStateList.valueOf(itemAudioCallIconTint));
            } else {
                imageView.setBackground(itemVideoCallIcon);
                imageView.setBackgroundTintList(ColorStateList.valueOf(itemVideoCallIconTint));
            }

            imageView.setOnClickListener(v -> {
                if (callIconClickListener != null) {
                    callIconClickListener.onCallIconClick(imageView, holder, position, callLog);
                }
            });

            holder.binding.tailView.addView(imageView);
        }

        /**
         * Sets up click listeners for the parent layout of the CallLog item.
         *
         * @param holder   The ViewHolder that holds the views for the CallLog item.
         * @param position The position of the CallLog item in the list.
         * @param callLog  The CallLog item associated with the ViewHolder.
         */
        private void setupClickListeners(CallLogsViewHolder holder, int position, CallLog callLog) {
            holder.binding.parentView.setOnClickListener(view -> {
                if (itemClickListener != null) {
                    itemClickListener.click(holder.itemView, position, callLog);
                }
            });

            holder.binding.parentView.setOnLongClickListener(view -> {
                if (itemLongClickListener != null) {
                    itemLongClickListener.longClick(holder.itemView, position, callLog);
                }
                return true;
            });
        }

        /**
         * Creates a CometChatDate view using the provided timestamp.
         *
         * @param timestamp The timestamp of the call log to convert to a date view.
         * @return A CometChatDate view displaying the formatted date.
         */
        private CometChatDate createCometChatDate(long timestamp) {
            CometChatDate cometchatDate = new CometChatDate(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                                   ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(context.getResources().getDimensionPixelSize(R.dimen.cometchat_margin_1), 0, 0, 0);
            cometchatDate.setLayoutParams(layoutParams);
            cometchatDate.setDateText(Utils.callLogsTimeStamp(timestamp,
                                                              simpleDateFormat,
                                                              dateTimeFormatter == null ? CometChatUIKit
                                                                  .getAuthSettings()
                                                                  .getDateTimeFormatterCallback() : dateTimeFormatter));
            cometchatDate.setStyle(dateStyle);
            return cometchatDate;
        }

        /**
         * Sets up the call icon in the subtitle view for the given CallLog item.
         *
         * @param holder        The ViewHolder that holds the views for the CallLog item.
         * @param imageView     The ImageView to display the call icon.
         * @param cometchatDate The CometChatDate view displaying the date.
         * @param icon          The drawable resource for the call icon.
         * @param iconTint      The tint color for the call icon.
         */
        private void setupCallIcon(CallLogsViewHolder holder,
                                   ImageView imageView,
                                   CometChatDate cometchatDate,
                                   Drawable icon,
                                   @ColorInt int iconTint) {
            holder.binding.subtitleView.removeAllViews();
            imageView.setBackground(icon);
            imageView.setBackgroundTintList(ColorStateList.valueOf(iconTint));
            holder.binding.subtitleView.addView(imageView);
            holder.binding.subtitleView.addView(cometchatDate);
        }

        /**
         * Sets up the view for a missed call in the subtitle view for the given CallLog
         * item.
         *
         * @param holder        The ViewHolder that holds the views for the CallLog item.
         * @param imageView     The ImageView to display the missed call icon.
         * @param cometchatDate The CometChatDate view displaying the date.
         */
        private void setupMissedCall(CallLogsViewHolder holder, @NonNull ImageView imageView, CometChatDate cometchatDate) {
            holder.binding.subtitleView.removeAllViews();
            imageView.setBackground(itemMissedCallIcon);
            imageView.setBackgroundTintList(ColorStateList.valueOf(itemMissedCallIconTint));
            holder.binding.tvTitle.setTextColor(itemMissedCallTitleColor);
            holder.binding.subtitleView.addView(imageView);
            holder.binding.subtitleView.addView(cometchatDate);
        }


        public CometchatCallLogsItemsBinding getBinding() {
            return binding;
        }
    }
}
