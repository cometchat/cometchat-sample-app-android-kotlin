package com.cometchat.chatuikit.aiassistantchathistory;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Conversation;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatAiAssistantChatHistoryBinding;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.interfaces.Function2;
import com.cometchat.chatuikit.shared.interfaces.OnClick;
import com.cometchat.chatuikit.shared.interfaces.OnItemClick;
import com.cometchat.chatuikit.shared.interfaces.OnItemLongClick;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.custom_dialog.CometChatConfirmDialog;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.ClickListener;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.RecyclerTouchListener;
import com.cometchat.chatuikit.shared.resources.utils.sticker_header.StickyHeaderDecoration;
import com.cometchat.chatuikit.shared.views.popupmenu.CometChatPopupMenu;
import com.cometchat.chatuikit.shimmer.CometChatShimmerAdapter;
import com.cometchat.chatuikit.shimmer.CometChatShimmerUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class CometChatAIAssistantChatHistory extends MaterialCardView {
    private static final String TAG = "CometChatAIAssistChatHistory";
    private CometchatAiAssistantChatHistoryBinding binding;
    private CometChatAIAssistantChatHistoryViewModel viewModel;
    private AIAssistantChatHistoryAdapter adapter;
    private LinearLayoutManager layoutManager;
    private LifecycleOwner lifecycleOwner;

    private CometChatPopupMenu cometchatPopUpMenu;
    private Function2<Context, BaseMessage, List<CometChatPopupMenu.MenuItem>> addOptions;
    private Function2<Context, BaseMessage, List<CometChatPopupMenu.MenuItem>> options;
    private Drawable deleteOptionIcon;
    private @ColorInt int deleteOptionIconTint;
    private @ColorInt int deleteOptionTextColor;
    private @StyleRes int deleteOptionTextAppearance;

    private CometChatConfirmDialog deleteAlertDialog;

    private boolean isScrolling;
    private boolean isInProgress;
    private boolean hasMore;
    private boolean isDetachedFromWindow;

    private StickyHeaderDecoration stickyHeaderDecoration;

    private OnClick onCloseButtonClickListener;
    private OnClick onNewChatClickListener;
    private OnItemClick<BaseMessage> onItemClick;
    private OnItemLongClick<BaseMessage> onItemLongClick;

    private int errorStateVisibility = View.VISIBLE;
    private int emptyStateVisibility = View.VISIBLE;

    private @ColorInt int chatHistoryBackgroundColor;

    private @ColorInt int headerBackgroundColor;
    private @ColorInt int headerTextColor;
    private @StyleRes int headerTextAppearance;
    private Drawable headerCloseIcon;
    private @ColorInt int headerCloseIconTint;

    private @ColorInt int newChatTextColor;
    private @DrawableRes int newChatIcon;
    private @ColorInt int newChatIconTint;
    private @StyleRes int newChatTextAppearance;

    private @ColorInt int dateSeparatorTextColor;
    private @StyleRes int dateSeparatorTextAppearance;

    private @ColorInt int itemBackgroundColor;
    private @ColorInt int itemTextColor;
    private @StyleRes int itemTextAppearance;

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

    /**
     * Observer for removing a conversation from the list. Notifies the adapter to
     * remove the item at the specified position.
     */
    Observer<Integer> remove = new Observer<Integer>() {
        @Override
        public void onChanged(Integer integer) {
            adapter.notifyItemRemoved(integer);
        }
    };

    /**
     * Observer for monitoring the conversation deletion state. Displays a progress
     * dialog based on the current deletion state.
     */
    Observer<UIKitConstants.DeleteState> deleteStateObserver = new Observer<UIKitConstants.DeleteState>() {
        @Override
        public void onChanged(UIKitConstants.DeleteState progressState) {
            if (UIKitConstants.DeleteState.SUCCESS_DELETE.equals(progressState)) {
                if (deleteAlertDialog != null) deleteAlertDialog.dismiss();
            } else if (UIKitConstants.DeleteState.FAILURE_DELETE.equals(progressState)) {
                if (deleteAlertDialog != null) deleteAlertDialog.dismiss();
                Toast.makeText(getContext(), getContext().getString(R.string.cometchat_conversation_delete_error), Toast.LENGTH_SHORT).show();
            } else if (UIKitConstants.DeleteState.INITIATED_DELETE.equals(progressState)) {
                deleteAlertDialog.hidePositiveButtonProgressBar(false);
            }
        }
    };

    /**
     * Notifies the adapter of a range of new items that have been inserted into the
     * data set.
     *
     * @param finalRange the number of new items added to the adapter
     */
    public void notifyRangeChanged(int finalRange) {
        adapter.notifyItemRangeInserted(0, finalRange);
    }

    public CometChatAIAssistantChatHistory(Context context) {
        this(context, null);
    }

    public CometChatAIAssistantChatHistory(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometChatAIAssistantChatHistoryStyle);
    }

    public CometChatAIAssistantChatHistory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        binding = CometchatAiAssistantChatHistoryBinding.inflate(LayoutInflater.from(getContext()), this, true);
        Utils.initMaterialCard(this);
        setupRecyclerView();
        initViewModel();
        setupClickListeners();
        applyStyleAttributes(attrs, defStyleAttr);
    }

    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatAIAssistantChatHistory, defStyleAttr, 0);
        @StyleRes int styleResId = directAttributes.getResourceId(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryStyle, 0);
        directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatAIAssistantChatHistory, defStyleAttr, styleResId);
        extractAttributesAndApplyDefaults(directAttributes);
    }

    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        if (typedArray == null) return;
        try {
            setChatHistoryBackgroundColor(typedArray.getColor(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryBackgroundColor, CometChatTheme.getBackgroundColor3(getContext())));

            setChatHistoryHeaderBackgroundColor(typedArray.getColor(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryHeaderBackgroundColor, CometChatTheme.getBackgroundColor3(getContext())));
            setChatHistoryHeaderTextColor(typedArray.getColor(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryHeaderTextColor, CometChatTheme.getTextColorPrimary(getContext())));
            setChatHistoryHeaderTextAppearance(typedArray.getResourceId(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryHeaderTextAppearance, 0));
            setChatHistoryHeaderCloseIcon(typedArray.getDrawable(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryHeaderCloseIcon));
            setChatHistoryHeaderCloseIconTint(typedArray.getColor(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryHeaderCloseIconTint, CometChatTheme.getIconTintSecondary(getContext())));

            setNewChatBackgroundColor(typedArray.getColor(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryNewChatBackgroundColor, CometChatTheme.getBackgroundColor3(getContext())));
            setNewChatTextColor(typedArray.getColor(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryNewChatTextColor, CometChatTheme.getTextColorPrimary(getContext())));
            setNewChatTextAppearance(typedArray.getResourceId(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryNewChatTextAppearance, 0));
            setNewChatIcon(typedArray.getResourceId(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryNewChatIcon, 0));
            setNewChatIconTint(typedArray.getColor(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryNewChatIconTint, CometChatTheme.getIconTintSecondary(getContext())));

            setDateSeparatorBackgroundColor(typedArray.getColor(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDateSeparatorBackgroundColor, CometChatTheme.getBackgroundColor3(getContext())));
            setDateSeparatorTextColor(typedArray.getColor(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDateSeparatorTextColor, CometChatTheme.getTextColorTertiary(getContext())));
            setDateSeparatorTextAppearance(typedArray.getResourceId(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDateSeparatorTextAppearance, 0));

            setItemBackgroundColor(typedArray.getColor(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryItemBackgroundColor, CometChatTheme.getBackgroundColor3(getContext())));
            setItemTextColor(typedArray.getColor(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryItemTextColor, CometChatTheme.getTextColorPrimary(getContext())));
            setItemTextAppearance(typedArray.getResourceId(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryItemTextAppearance, 0));

            setDeleteOptionIcon(typedArray.getDrawable(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDeleteOptionIcon));
            setDeleteOptionIconTint(typedArray.getColor(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDeleteOptionIconTint, CometChatTheme.getErrorColor(getContext())));
            setDeleteOptionTextColor(typedArray.getColor(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDeleteOptionTextColor, CometChatTheme.getTextColorPrimary(getContext())));
            setDeleteOptionTextAppearance(typedArray.getResourceId(R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDeleteOptionTextAppearance, 0));
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Sets the style for this view from the provided style resource.
     *
     * @param style The style resource ID to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatAIAssistantChatHistory);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Returns the text appearance resource ID for the delete option text. This
     * appearance defines the style for the text in the option.
     *
     * @return the resource ID for the error state title text appearance
     */
    public @StyleRes int getDeleteOptionTextAppearance() {
        return deleteOptionTextAppearance;
    }

    /**
     * Sets the text appearance style for the delete option in the conversations
     * popup menu.
     *
     * @param textAppearance the style resource ID to use for the delete option text appearance.
     */
    private void setDeleteOptionTextAppearance(@StyleRes int textAppearance) {
        if (textAppearance != 0) {
            this.deleteOptionTextAppearance = textAppearance;
        }
    }

    /**
     * Gets the text color for the delete option in the popup menu on long press.
     *
     * @return the color used for the delete option text.
     */
    public int getDeleteOptionTextColor() {
        return deleteOptionTextColor;
    }

    /**
     * Sets the text color for the delete option in the popup menu on long press.
     *
     * @param color the color to set for the delete option text.
     */
    private void setDeleteOptionTextColor(@ColorInt int color) {
        if (color != 0) {
            this.deleteOptionTextColor = color;
        }
    }

    /**
     * Gets the tint color for the delete option icon in the popup menu on long
     * press.
     *
     * @return the color used as the tint for the delete option icon.
     */
    public int getDeleteOptionIconTint() {
        return deleteOptionIconTint;
    }

    /**
     * Sets the tint color for the delete option icon in the popup menu on long
     * press.
     *
     * @param color the color to set as the tint for the delete option icon.
     */
    private void setDeleteOptionIconTint(@ColorInt int color) {
        if (color != 0) {
            this.deleteOptionIconTint = color;
        }
    }


    /**
     * Gets the drawable icon used for the delete option in the popup menu on long
     * press.
     *
     * @return the drawable icon for the delete option.
     */
    public Drawable getDeleteOptionIcon() {
        return deleteOptionIcon;
    }

    /**
     * Sets the drawable icon used for the delete option in the popup menu on long
     * press.
     *
     * @param drawable the drawable icon to set for the delete option.
     */
    private void setDeleteOptionIcon(Drawable drawable) {
        if (drawable != null) {
            this.deleteOptionIcon = drawable;
        }
    }

    /**
     * Gets the background color for the date separator in the chat history.
     *
     * @return the color used as the background for the date separator.
     */
    public int getDateSeparatorBackgroundColor() {
        return CometChatTheme.getBackgroundColor3(getContext());
    }

    /**
     * Sets the background color for the date separator in the chat history.
     *
     * @param dateSeparatorBackgroundColor The color to set as the background for the date separator.
     */
    private void setDateSeparatorBackgroundColor(@ColorInt int dateSeparatorBackgroundColor) {
        adapter.setDateSeparatorBackgroundColor(dateSeparatorBackgroundColor);
    }

    /**
     * Gets the background color for the entire chat history view.
     *
     * @return the color used as the background for the chat history.
     */
    public int getChatHistoryBackgroundColor() {
        return chatHistoryBackgroundColor;
    }

    /**
     * Sets the background color for the entire chat history view.
     *
     * @param color The color to set as the background for the chat history.
     */
    private void setChatHistoryBackgroundColor(@ColorInt int color) {
        if (color != 0) {
            this.chatHistoryBackgroundColor = color;
            binding.aiAssistantChatHistoryParent.setBackgroundColor(chatHistoryBackgroundColor);
        }
    }

    /**
     * Gets the background color for the chat history header.
     *
     * @return the color used as the background for the chat history header.
     */
    public int getChatHistoryHeaderBackgroundColor() {
        return headerBackgroundColor;
    }

    /**
     * Sets the background color for the chat history header.
     *
     * @param color The color to set as the background for the chat history header.
     */
    private void setChatHistoryHeaderBackgroundColor(@ColorInt int color) {
        if (color != 0) {
            this.headerBackgroundColor = color;
            binding.chatHistoryHeader.setBackgroundColor(headerBackgroundColor);
        }
    }

    /**
     * Gets the text color for the chat history header title.
     *
     * @return the color used for the chat history header title text.
     */
    public int getChatHistoryHeaderTextColor() {
        return headerTextColor;
    }

    /**
     * Sets the text color for the chat history header title.
     *
     * @param color The color to set for the chat history header title text.
     */
    private void setChatHistoryHeaderTextColor(@ColorInt int color) {
        if (color != 0) {
            this.headerTextColor = color;
            binding.tvChatHistory.setTextColor(headerTextColor);
        }
    }

    /**
     * Gets the text appearance resource ID for the chat history header title.
     *
     * @return the resource ID for the chat history header title text appearance
     */
    public @StyleRes int getChatHistoryHeaderTextAppearance() {
        return headerTextAppearance;
    }

    /**
     * Sets the text appearance style for the chat history header title.
     *
     * @param textAppearance the style resource ID to use for the chat history header title text appearance.
     */
    private void setChatHistoryHeaderTextAppearance(@StyleRes int textAppearance) {
        if (textAppearance != 0) {
            this.headerTextAppearance = textAppearance;
            binding.tvChatHistory.setTextAppearance(headerTextAppearance);
        }
    }

    /**
     * Gets the drawable icon used for the chat history header close button.
     *
     * @return the drawable icon for the chat history header close button.
     */
    public Drawable getChatHistoryHeaderCloseIcon() {
        return headerCloseIcon;
    }

    /**
     * Sets the drawable icon used for the chat history header close button.
     *
     * @param drawable the drawable icon to set for the chat history header close button.
     */
    private void setChatHistoryHeaderCloseIcon(Drawable drawable) {
        if (drawable != null) {
            this.headerCloseIcon = drawable;
            binding.ivClose.setImageDrawable(headerCloseIcon);
        }
    }

    /**
     * Gets the tint color for the chat history header close button icon.
     *
     * @return the color used as the tint for the chat history header close button icon.
     */
    public int getChatHistoryHeaderCloseIconTint() {
        return headerCloseIconTint;
    }

    /**
     * Sets the tint color for the chat history header close button icon.
     *
     * @param color the color to set as the tint for the chat history header close button icon.
     */
    private void setChatHistoryHeaderCloseIconTint(@ColorInt int color) {
        if (color != 0) {
            this.headerCloseIconTint = color;
            binding.ivClose.setImageTintList(ColorStateList.valueOf(headerCloseIconTint));
        }
    }

    /**
     * Sets the background color for the "New Chat" button in the chat history header.
     *
     * @param color The color to set as the background for the "New Chat" button.
     */
    private void setNewChatBackgroundColor(int color) {
        binding.newChatLayout.setBackgroundColor(color);
    }

    /**
     * Gets the text color for the "New Chat" button in the chat history header.
     *
     * @return the color used for the "New Chat" button text.
     */
    public int getNewChatTextColor() {
        return newChatTextColor;
    }

    /**
     * Sets the text color for the "New Chat" button in the chat history header.
     *
     * @param color The color to set for the "New Chat" button text.
     */
    private void setNewChatTextColor(@ColorInt int color) {
        if (color != 0) {
            this.newChatTextColor = color;
            binding.tvNewChat.setTextColor(newChatTextColor);
        }
    }

    /**
     * Gets the text appearance resource ID for the "New Chat" button text.
     *
     * @return the resource ID for the "New Chat" button text appearance
     */
    public @StyleRes int getNewChatTextAppearance() {
        return newChatTextAppearance;
    }

    /**
     * Sets the text appearance style for the "New Chat" button text.
     *
     * @param textAppearance the style resource ID to use for the "New Chat" button text appearance.
     */
    private void setNewChatTextAppearance(@StyleRes int textAppearance) {
        if (textAppearance != 0) {
            this.newChatTextAppearance = textAppearance;
            binding.tvNewChat.setTextAppearance(newChatTextAppearance);
        }
    }

    /**
     * Gets the drawable icon used for the "New Chat" button.
     *
     * @return the drawable icon for the "New Chat" button.
     */
    public @DrawableRes int getNewChatIcon() {
        return newChatIcon;
    }

    /**
     * Sets the icon icon used for the "New Chat" button.
     *
     * @param icon the icon icon to set for the "New Chat" button.
     */
    private void setNewChatIcon(@DrawableRes int icon) {
        if (icon != 0) {
            this.newChatIcon = icon;
            binding.ivNewChat.setImageResource(newChatIcon);
        }
    }

    /**
     * Gets the tint color for the "New Chat" button icon.
     *
     * @return the color used as the tint for the "New Chat" button icon.
     */
    public int getNewChatIconTint() {
        return newChatIconTint;
    }

    /**
     * Sets the tint color for the "New Chat" button icon.
     *
     * @param color the color to set as the tint for the "New Chat" button icon.
     */
    private void setNewChatIconTint(@ColorInt int color) {
        if (color != 0) {
            this.newChatIconTint = color;
            binding.ivNewChat.setColorFilter(newChatIconTint);
        }
    }

    /**
     * Gets the text color for the date separator in the chat history.
     *
     * @return the color used for the date separator text.
     */
    public int getDateSeparatorTextColor() {
        return dateSeparatorTextColor;
    }

    /**
     * Sets the text color for the date separator in the chat history.
     *
     * @param color The color to set for the date separator text.
     */
    private void setDateSeparatorTextColor(@ColorInt int color) {
        if (color != 0) {
            this.dateSeparatorTextColor = color;
            adapter.setDateSeparatorTextColor(dateSeparatorTextColor);
        }
    }

    /**
     * Gets the text appearance resource ID for the date separator text.
     *
     * @return the resource ID for the date separator text appearance
     */
    public @StyleRes int getDateSeparatorTextAppearance() {
        return dateSeparatorTextAppearance;
    }

    /**
     * Sets the text appearance style for the date separator text.
     *
     * @param textAppearance the style resource ID to use for the date separator text appearance.
     */
    private void setDateSeparatorTextAppearance(@StyleRes int textAppearance) {
        if (textAppearance != 0) {
            this.dateSeparatorTextAppearance = textAppearance;
            adapter.setDateSeparatorTextAppearance(dateSeparatorTextAppearance);
        }
    }

    /**
     * Gets the background color for individual chat history items.
     *
     * @return the color used as the background for chat history items.
     */
    public int getItemBackgroundColor() {
        return itemBackgroundColor;
    }

    /**
     * Sets the background color for individual chat history items.
     *
     * @param color The color to set as the background for chat history items.
     */
    private void setItemBackgroundColor(@ColorInt int color) {
        if (color != 0) {
            this.itemBackgroundColor = color;
            adapter.setItemBackgroundColor(itemBackgroundColor);
        }
    }

    /**
     * Gets the text color for individual chat history items.
     *
     * @return the color used for chat history item text.
     */
    public int getItemTextColor() {
        return itemTextColor;
    }

    /**
     * Sets the text color for individual chat history items.
     *
     * @param color The color to set for chat history item text.
     */
    private void setItemTextColor(@ColorInt int color) {
        if (color != 0) {
            this.itemTextColor = color;
            adapter.setItemTextColor(itemTextColor);
        }
    }

    /**
     * Gets the text appearance resource ID for individual chat history items.
     *
     * @return the resource ID for chat history item text appearance
     */
    public @StyleRes int getItemTextAppearance() {
        return itemTextAppearance;
    }

    /**
     * Sets the text appearance style for individual chat history items.
     *
     * @param textAppearance the style resource ID to use for chat history item text appearance.
     */
    private void setItemTextAppearance(@StyleRes int textAppearance) {
        if (textAppearance != 0) {
            this.itemTextAppearance = textAppearance;
            adapter.setItemTextAppearance(itemTextAppearance);
        }
    }

    // Handles the UI state when there are chat history items to display.
    private void handleNonEmptyState() {
        hideShimmer();
        binding.emptyStateView.setVisibility(View.GONE);
        binding.errorStateView.setVisibility(View.GONE);
        setRecyclerViewVisibility(View.VISIBLE);
    }

    // Sets the visibility of the RecyclerView displaying chat history.
    private void setRecyclerViewVisibility(int visibility) {
        binding.rvChatHistory.setVisibility(visibility);
    }

    // Handles the UI state when there are no chat history items to display.
    private void handleEmptyState() {
        hideShimmer();
        setRecyclerViewVisibility(View.GONE);
        setErrorStateVisibility(View.GONE);
        if (emptyStateVisibility == View.VISIBLE) {
            binding.tvEmptyTitle.setText(getResources().getString(R.string.cometchat_no_conversations_history_title));
            binding.tvEmptySubtitle.setText(getResources().getString(R.string.cometchat_no_conversations_history_subtitle));
            binding.emptyStateView.setVisibility(View.VISIBLE);
        } else {
            binding.emptyStateView.setVisibility(View.GONE);
        }
    }

    // Handles the UI state when there is an error loading chat history items.
    private void handleErrorState() {
        hideShimmer();
        setRecyclerViewVisibility(View.GONE);
        setEmptyStateVisibility(View.GONE);
        if (errorStateVisibility == View.VISIBLE) {
            binding.tvErrorTitle.setText(getResources().getString(R.string.cometchat_error_conversations_title));
            binding.tvErrorSubtitle.setText(getResources().getString(R.string.cometchat_error_conversations_subtitle));
            binding.errorStateView.setVisibility(View.VISIBLE);
        } else {
            binding.errorStateView.setVisibility(View.GONE);
        }
    }

    /** Sets the visibility of the error state view.
     *
     * @param visibility The visibility state to set (e.g., View.VISIBLE, View.GONE).
     */
    public void setErrorStateVisibility(int visibility) {
        this.errorStateVisibility = visibility;
        binding.errorStateView.setVisibility(visibility);
    }

    /** Sets the visibility of the empty state view.
     *
     * @param visibility The visibility state to set (e.g., View.VISIBLE, View.GONE).
     */
    public void setEmptyStateVisibility(int visibility) {
        this.emptyStateVisibility = visibility;
        binding.emptyStateView.setVisibility(visibility);
    }

    // Hides the shimmer loading effect.
    private void hideShimmer() {
        if (binding.shimmerEffectFrame.isShimmerRunning()) binding.shimmerEffectFrame.stopShimmer();
        binding.shimmerParentLayout.setVisibility(View.GONE);
    }

    // Handles the UI state when chat history items have been successfully loaded.
    private void handleLoadedState() {
        hideShimmer();
        binding.errorStateView.setVisibility(View.GONE);
        binding.emptyStateView.setVisibility(View.GONE);
        binding.rvChatHistory.setVisibility(View.VISIBLE);
    }

    // Handles the UI state when chat history items are being loaded.
    private void handleLoadingState() {
        CometChatShimmerAdapter adapter = new CometChatShimmerAdapter(30, R.layout.shimmer_chat_history_item);
        binding.shimmerRecyclerviewMessageList.setAdapter(adapter);
        binding.shimmerRecyclerviewMessageList.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        binding.rvChatHistory.setVisibility(View.GONE);
        binding.shimmerParentLayout.setVisibility(View.VISIBLE);
        binding.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(getContext()));
        binding.shimmerEffectFrame.startShimmer();
    }

    // Sets up the RecyclerView for displaying chat history items.
    private void setupRecyclerView() {
        adapter = new AIAssistantChatHistoryAdapter(getContext());
        layoutManager = new LinearLayoutManager(getContext());
        binding.rvChatHistory.setLayoutManager(layoutManager);
        binding.rvChatHistory.setAdapter(adapter);
        stickyHeaderDecoration = new StickyHeaderDecoration(adapter);
        binding.rvChatHistory.addItemDecoration(stickyHeaderDecoration, 0);

        binding.rvChatHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
    }

    // Sets up LiveData observers to monitor changes in chat history data and UI states.
    private void initViewModel() {
        viewModel = new ViewModelProvider.NewInstanceFactory().create(CometChatAIAssistantChatHistoryViewModel.class);
        lifecycleOwner = Utils.getLifecycleOwner(getContext());
        if (lifecycleOwner == null) return;
        attachObservers();
    }

    private void attachObservers() {
        viewModel.getMessagesLiveData().observe(lifecycleOwner, this::onMessagesReceived);
        viewModel.getStateLiveData().observe(lifecycleOwner, stateChangeObserver);
        viewModel.getDeleteStateMutableLiveData().observe(lifecycleOwner, deleteStateObserver);
        viewModel.getRemoveMessage().observe(lifecycleOwner, remove);
        viewModel.getMutableMessagesRangeChanged().observe(lifecycleOwner, this::notifyRangeChanged);
        viewModel.getMutableHasMore().observe(lifecycleOwner, this::hasMore);
        viewModel.getMutableIsInProgress().observe(lifecycleOwner, this::isInProgress);
    }

    private void hasMore(Boolean aBoolean) {
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
        if (hasMore && !isInProgress) {
            if (isScrolling && adapter != null && ((adapter.getItemCount() - 1) - layoutManager.findLastVisibleItemPosition() < 2)) {
                isInProgress = true;
                isScrolling = false;
                fetchPreviousMessages();
            }
        }
    }

    private void fetchPreviousMessages() {
        if (viewModel != null) {
            viewModel.fetchMessages();
        }
    }

    // Sets up click listeners for various UI elements.
    private void setupClickListeners() {
        binding.ivClose.setOnClickListener(v -> {
            if (onCloseButtonClickListener != null) {
                onCloseButtonClickListener.onClick();
            }
        });

        binding.newChatLayout.setOnClickListener(v -> {
            if (onNewChatClickListener != null) {
                onNewChatClickListener.onClick();
            }
        });

        cometchatPopUpMenu = new CometChatPopupMenu(getContext(), 0);
        binding.rvChatHistory.addOnItemTouchListener(new RecyclerTouchListener(getContext(), binding.rvChatHistory, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (onItemClick != null)
                    onItemClick.click(view, position, adapter.getItem(position));

            }

            @Override
            public void onLongClick(View view, int position) {
                if (onItemLongClick != null) {
                    onItemLongClick.longClick(view, position, adapter.getItem(position));
                } else {
                    BaseMessage message = adapter.getItem(position);
                    preparePopupMenu(view, message);
                }
            }
        }));
    }

    // Prepares and displays the popup menu with options for the selected chat history item.
    private void preparePopupMenu(View view, BaseMessage baseMessage) {
        List<CometChatPopupMenu.MenuItem> optionsArrayList = new ArrayList<>();
        if (options != null) {
            optionsArrayList.addAll(options.apply(getContext(), baseMessage));
        } else {
            optionsArrayList.add(new CometChatPopupMenu.MenuItem(UIKitConstants.ConversationOption.DELETE,
                    getContext().getString(R.string.cometchat_delete),
                    deleteOptionIcon,
                    null,
                    deleteOptionIconTint,
                    0,
                    deleteOptionTextColor,
                    deleteOptionTextAppearance,
                    null));

            if (addOptions != null)
                optionsArrayList.addAll(addOptions.apply(getContext(), baseMessage));
        }
        cometchatPopUpMenu.setMenuItems(optionsArrayList);
        cometchatPopUpMenu.setOnMenuItemClickListener((id, name) -> {
            for (CometChatPopupMenu.MenuItem item : optionsArrayList) {
                if (id.equalsIgnoreCase(item.getId())) {
                    if (item.getOnClick() != null) {
                        item.getOnClick().onClick();
                    } else {
                        handleDefaultClickEvents(item, baseMessage);
                    }
                    break;
                }
            }
            cometchatPopUpMenu.dismiss();
        });

        cometchatPopUpMenu.show(view);
    }

    /**
     * Handles default click events for the given menu item associated with a group
     * member.
     *
     * <p>
     * This method performs specific actions based on the ID of the menu item. If
     * the item ID corresponds to changing the scope of the group member, it
     * displays a bottom sheet dialog to allow the user to delete baseMessage.
     * If the item ID indicates a ban or kick action, it shows a confirmation dialog
     * to confirm the action.
     *
     * @param item         The {@link CometChatPopupMenu.MenuItem} that was clicked.
     * @param baseMessage The {@link Conversation} associated with the clicked menu item.
     */
    private void handleDefaultClickEvents(CometChatPopupMenu.MenuItem item, BaseMessage baseMessage) {
        if (item.getId().equalsIgnoreCase(UIKitConstants.ConversationOption.DELETE)) {
            deleteAlertDialog = new CometChatConfirmDialog(getContext(), R.style.CometChatConfirmDialogStyle);
            showDeleteConversationAlertDialog(baseMessage);
        }
    }

    private void showDeleteConversationAlertDialog(BaseMessage baseMessage) {
        deleteAlertDialog.setConfirmDialogIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.cometchat_ic_delete, null));
        deleteAlertDialog.setTitleText(getContext().getString(R.string.cometchat_conversation_delete_message_title));
        deleteAlertDialog.setSubtitleText(getContext().getString(R.string.cometchat_conversation_delete_message_subtitle));
        deleteAlertDialog.setPositiveButtonText(getContext().getString(R.string.cometchat_delete));
        deleteAlertDialog.setNegativeButtonText(getContext().getString(R.string.cometchat_cancel));
        deleteAlertDialog.setOnPositiveButtonClick(v -> {
            viewModel.deleteChatHistoryItem(baseMessage);
        });
        deleteAlertDialog.setOnNegativeButtonClick(v -> deleteAlertDialog.dismiss());
        deleteAlertDialog.setConfirmDialogElevation(0);
        deleteAlertDialog.setCancelable(false);
        deleteAlertDialog.show();
    }

    // Callback when messages are received from ViewModel
    private void onMessagesReceived(List<BaseMessage> messages) {
        if (messages != null && !messages.isEmpty()) {
            adapter.setMessageList(messages);
        }
    }

    /**
     * Set user and fetch their chat messages
     *
     * @param user The user whose messages to fetch
     */
    public void setUser(User user) {
        if (user != null) {
            viewModel.setUser(user);
        }
    }

    /**
     * Set click listener for close button
     *
     * @param listener The click listener
     */
    public void setOnCloseClickListener(OnClick listener) {
        this.onCloseButtonClickListener = listener;
    }

    /**
     * Set click listener for new chat button
     *
     * @param listener The click listener
     */
    public void setOnNewChatClickListener(OnClick listener) {
        this.onNewChatClickListener = listener;
    }

    /**
     * Sets the item click listener for chat history items.
     *
     * @param listener The click listener to set
     */
    public void setOnItemClickListener(OnItemClick<BaseMessage> listener) {
        this.onItemClick = listener;
        adapter.setOnItemClickListener(listener);
    }

    /**
     * Sets the item long click listener for chat history items.
     *
     * @param listener The long click listener to set
     */
    public void setOnItemLongClickListener(OnItemLongClick<BaseMessage> listener) {
        this.onItemLongClick = listener;
        adapter.setOnItemLongClickListener(listener);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isDetachedFromWindow) {
            attachObservers();
            isDetachedFromWindow = false;
        }
        viewModel.addListener();
    }

    /**
     * Called when the view is detached from a window. Removes observers.
     */
    @Override
    protected void onDetachedFromWindow() {
        isDetachedFromWindow = true;
        removeListeners();
        disposeObservers();
        super.onDetachedFromWindow();
    }

    private void removeListeners() {
        if (viewModel != null) {
            viewModel.removeListener();
        }
    }

    public void disposeObservers() {
        try {
            if (lifecycleOwner != null && viewModel != null) {
                viewModel.getMessagesLiveData().removeObservers(lifecycleOwner);
                viewModel.getStateLiveData().removeObservers(lifecycleOwner);
                viewModel.getDeleteStateMutableLiveData().removeObservers(lifecycleOwner);
                viewModel.getRemoveMessage().removeObservers(lifecycleOwner);
                viewModel.getMutableMessagesRangeChanged().removeObservers(lifecycleOwner);
                viewModel.getMutableHasMore().removeObservers(lifecycleOwner);
                viewModel.getMutableIsInProgress().removeObservers(lifecycleOwner);
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, "Error in disposing observers: " + e.getMessage());
        }
    }
}
