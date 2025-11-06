package com.cometchat.chatuikit.users;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatListBaseItemsBinding;
import com.cometchat.chatuikit.databinding.CometchatUserListStickyHeaderBinding;
import com.cometchat.chatuikit.shared.interfaces.OnItemClick;
import com.cometchat.chatuikit.shared.interfaces.OnItemLongClick;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.sticker_header.StickyHeaderAdapter;
import com.cometchat.chatuikit.shared.viewholders.UsersViewHolderListener;
import com.cometchat.chatuikit.shared.views.statusindicator.StatusIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Adapter class for managing a list of users in a RecyclerView. Implements
 * StickyHeaderAdapter for sticky header functionality.
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> implements StickyHeaderAdapter<UsersAdapter.StickyViewHolder> {
    private final Context context;

    private boolean hideUserStatus;
    private boolean isSelectionEnabled;

    private @StyleRes int avatarStyle;
    private @StyleRes int itemTitleTextAppearance;
    private @ColorInt int itemTitleTextColor;
    private @ColorInt int itemBackgroundColor;
    private @StyleRes int statusIndicatorStyle;
    private @ColorInt int itemSelectedBackgroundColor;

    private @Dimension int checkBoxStrokeWidth;
    private @Dimension int checkBoxCornerRadius;
    private @ColorInt int checkBoxStrokeColor;
    private @ColorInt int checkBoxBackgroundColor;
    private @ColorInt int checkBoxCheckedBackgroundColor;
    private @ColorInt int checkBoxSelectIconTint;
    private Drawable checkBoxSelectIcon;

    private @ColorInt int stickyTitleColor;
    private @StyleRes int stickyTitleAppearance;
    private @ColorInt int stickyTitleBackgroundColor;

    private List<User> userList;
    private HashMap<User, Boolean> selectedUsers;

    private UsersViewHolderListener tailView;
    private UsersViewHolderListener subtitleView;
    private UsersViewHolderListener listItemView;
    private UsersViewHolderListener titleView;
    private UsersViewHolderListener leadingView;
    private OnItemLongClick<User> onItemLongClick;
    private OnItemClick<User> onItemClick;

    /**
     * Constructs a UsersAdapter with the given context.
     *
     * @param context the context from which the adapter is created
     */
    public UsersAdapter(Context context) {
        this.context = context;
        userList = new ArrayList<>();
        selectedUsers = new HashMap<>();
    }

    /**
     * Sets the selected users map for the adapter.
     *
     * @param selectedUsers a HashMap containing User objects as keys and Boolean values indicating selection status
     */
    public void setSelectedUsersMap(HashMap<User, Boolean> selectedUsers) {
        this.selectedUsers = selectedUsers;
    }

    /**
     * Creates a new ViewHolder for the user item.
     *
     * @param parent   the parent view group to which the new view will be attached
     * @param viewType the view type of the new view
     * @return a new UserViewHolder instance
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(CometchatListBaseItemsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false).getRoot());
    }

    /**
     * Binds the user data to the specified ViewHolder.
     *
     * @param holder   the ViewHolder to bind data to
     * @param position the position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bindView(userList.get(position), position);
    }

    /**
     * Returns the view type of the item at the specified position.
     *
     * @param position the position of the item within the adapter's data set
     * @return an integer representing the view type for the item
     */
    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    /**
     * Returns the total number of items in the adapter.
     *
     * @return the number of items in the user list
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * Sets the user list for the adapter and refreshes the view.
     *
     * @param list the list of users to set
     */
    public void setUserList(List<User> list) {
        if (list != null) {
            this.userList = list;
            notifyDataSetChanged();
        }
    }

    /**
     * Returns the current avatar style.
     *
     * @return the avatar style resource ID
     */
    public @StyleRes int getAvatarStyle() {
        return avatarStyle;
    }

    /**
     * Sets the avatar style and refreshes the view.
     *
     * @param avatarStyle the avatar style resource ID to set
     */
    public void setAvatarStyle(@StyleRes int avatarStyle) {
        this.avatarStyle = avatarStyle;
        notifyDataSetChanged();
    }

    /**
     * Returns the current item title text appearance.
     *
     * @return the item title text appearance resource ID
     */
    public @StyleRes int getItemTitleTextAppearance() {
        return itemTitleTextAppearance;
    }

    /**
     * Sets the item title text appearance and refreshes the view.
     *
     * @param itemTitleTextAppearance the text appearance resource ID to set
     */
    public void setItemTitleTextAppearance(@StyleRes int itemTitleTextAppearance) {
        this.itemTitleTextAppearance = itemTitleTextAppearance;
        notifyDataSetChanged();
    }

    /**
     * Returns the current item title text color.
     *
     * @return the item title text color
     */
    public @ColorInt int getItemTitleTextColor() {
        return itemTitleTextColor;
    }

    /**
     * Sets the item title text color and refreshes the view.
     *
     * @param itemTitleTextColor the color to set for item titles
     */
    public void setItemTitleTextColor(@ColorInt int itemTitleTextColor) {
        this.itemTitleTextColor = itemTitleTextColor;
        notifyDataSetChanged();
    }

    /**
     * Returns the current item background color.
     *
     * @return the item background color
     */
    public @ColorInt int getItemBackgroundColor() {
        return itemBackgroundColor;
    }

    /**
     * Sets the background color for the items and refreshes the view.
     *
     * @param itemBackgroundColor the background color to set for items
     */
    public void setItemBackgroundColor(@ColorInt int itemBackgroundColor) {
        this.itemBackgroundColor = itemBackgroundColor;
        notifyDataSetChanged();
    }

    /**
     * Returns the current status indicator style.
     *
     * @return the status indicator style resource ID
     */
    public @StyleRes int getStatusIndicatorStyle() {
        return statusIndicatorStyle;
    }

    /**
     * Sets the status indicator style and refreshes the view.
     *
     * @param statusIndicatorStyle the status indicator style resource ID to set
     */
    public void setStatusIndicatorStyle(@StyleRes int statusIndicatorStyle) {
        this.statusIndicatorStyle = statusIndicatorStyle;
        notifyDataSetChanged();
    }

    /**
     * Returns the current selected background color for items.
     *
     * @return the selected background color for items
     */
    public @ColorInt int getItemSelectedBackgroundColor() {
        return itemSelectedBackgroundColor;
    }

    /**
     * Sets the selected background color for items and refreshes the view.
     *
     * @param itemSelectedBackgroundColor the background color to set for selected items
     */
    public void setItemSelectedBackgroundColor(@ColorInt int itemSelectedBackgroundColor) {
        this.itemSelectedBackgroundColor = itemSelectedBackgroundColor;
        notifyDataSetChanged();
    }

    /**
     * Returns the stroke width for the checkbox.
     *
     * @return the stroke width for the checkbox
     */
    public @Dimension int getCheckBoxStrokeWidth() {
        return checkBoxStrokeWidth;
    }

    /**
     * Sets the stroke width for the checkbox and refreshes the view.
     *
     * @param checkBoxStrokeWidth the stroke width to set for the checkbox
     */
    public void setCheckBoxStrokeWidth(@Dimension int checkBoxStrokeWidth) {
        this.checkBoxStrokeWidth = checkBoxStrokeWidth;
        notifyDataSetChanged();
    }

    /**
     * Returns the corner radius for the checkbox.
     *
     * @return the corner radius for the checkbox
     */
    public @Dimension int getCheckBoxCornerRadius() {
        return checkBoxCornerRadius;
    }

    /**
     * Sets the corner radius for the checkbox and refreshes the view.
     *
     * @param checkBoxCornerRadius the corner radius to set for the checkbox
     */
    public void setCheckBoxCornerRadius(@Dimension int checkBoxCornerRadius) {
        this.checkBoxCornerRadius = checkBoxCornerRadius;
        notifyDataSetChanged();
    }

    /**
     * Returns the stroke color for the checkbox.
     *
     * @return the stroke color for the checkbox
     */
    public @ColorInt int getCheckBoxStrokeColor() {
        return checkBoxStrokeColor;
    }

    /**
     * Sets the stroke color for the checkbox and refreshes the view.
     *
     * @param checkBoxStrokeColor the stroke color to set for the checkbox
     */
    public void setCheckBoxStrokeColor(@ColorInt int checkBoxStrokeColor) {
        this.checkBoxStrokeColor = checkBoxStrokeColor;
        notifyDataSetChanged();
    }

    /**
     * Returns the background color for the checkbox.
     *
     * @return the background color for the checkbox
     */
    public @ColorInt int getCheckBoxBackgroundColor() {
        return checkBoxBackgroundColor;
    }

    /**
     * Sets the background color for the checkbox and refreshes the view.
     *
     * @param checkBoxBackgroundColor the background color to set for the checkbox
     */
    public void setCheckBoxBackgroundColor(@ColorInt int checkBoxBackgroundColor) {
        this.checkBoxBackgroundColor = checkBoxBackgroundColor;
        notifyDataSetChanged();
    }

    /**
     * Returns the background color for the checked state of the checkbox.
     *
     * @return the checked background color for the checkbox
     */
    public @ColorInt int getCheckBoxCheckedBackgroundColor() {
        return checkBoxCheckedBackgroundColor;
    }

    /**
     * Sets the background color for the checked state of the checkbox and refreshes
     * the view.
     *
     * @param checkBoxCheckedBackgroundColor the background color to set for the checked checkbox
     */
    public void setCheckBoxCheckedBackgroundColor(@ColorInt int checkBoxCheckedBackgroundColor) {
        this.checkBoxCheckedBackgroundColor = checkBoxCheckedBackgroundColor;
        notifyDataSetChanged();
    }

    /**
     * Returns the current color for the sticky title.
     *
     * @return the sticky title color
     */
    public @ColorInt int getStickyTitleColor() {
        return stickyTitleColor;
    }

    /**
     * Sets the color for the sticky title and refreshes the view.
     *
     * @param stickyTitleColor the color to set for the sticky title
     */
    public void setStickyTitleColor(@ColorInt int stickyTitleColor) {
        this.stickyTitleColor = stickyTitleColor;
        notifyDataSetChanged();
    }

    /**
     * Returns the current appearance style for the sticky title.
     *
     * @return the sticky title appearance resource ID
     */
    public @StyleRes int getStickyTitleAppearance() {
        return stickyTitleAppearance;
    }

    /**
     * Sets the appearance style for the sticky title and refreshes the view.
     *
     * @param stickyTitleAppearance the appearance style resource ID to set for the sticky title
     */
    public void setStickyTitleAppearance(@StyleRes int stickyTitleAppearance) {
        this.stickyTitleAppearance = stickyTitleAppearance;
        notifyDataSetChanged();
    }

    /**
     * Returns the current background color for the sticky title.
     *
     * @return the sticky title background color
     */
    public @ColorInt int getStickyTitleBackgroundColor() {
        return stickyTitleBackgroundColor;
    }

    /**
     * Sets the background color for the sticky title and refreshes the view.
     *
     * @param stickyTitleBackgroundColor the background color to set for the sticky title
     */
    public void setStickyTitleBackgroundColor(@ColorInt int stickyTitleBackgroundColor) {
        this.stickyTitleBackgroundColor = stickyTitleBackgroundColor;
        notifyDataSetChanged();
    }

    /**
     * Checks if selection is enabled.
     *
     * @return true if selection is enabled, false otherwise
     */
    public boolean isSelectionEnabled() {
        return isSelectionEnabled;
    }

    /**
     * Enables or disables selection and refreshes the view.
     *
     * @param selectionEnabled true to enable selection, false to disable
     */
    public void isSelectionEnabled(boolean selectionEnabled) {
        isSelectionEnabled = selectionEnabled;
        notifyDataSetChanged();
    }

    /**
     * Returns the checkbox select icon drawable.
     *
     * @return the checkbox select icon drawable
     */
    public Drawable getCheckBoxSelectIcon() {
        return checkBoxSelectIcon;
    }

    /**
     * Sets the checkbox select icon drawable.
     *
     * @param checkBoxSelectIcon the drawable to set for the checkbox select icon
     */
    public void setCheckBoxSelectIcon(Drawable checkBoxSelectIcon) {
        this.checkBoxSelectIcon = checkBoxSelectIcon;
    }

    /**
     * Returns the tint color for the checkbox select icon.
     *
     * @return the tint color for the checkbox select icon
     */
    public @ColorInt int getCheckBoxSelectIconTint() {
        return checkBoxSelectIconTint;
    }

    /**
     * Sets the tint color for the checkbox select icon.
     *
     * @param checkBoxSelectIconTint the tint color to set for the checkbox select icon
     */
    public void setCheckBoxSelectIconTint(@ColorInt int checkBoxSelectIconTint) {
        this.checkBoxSelectIconTint = checkBoxSelectIconTint;
    }

    /**
     * Retrieves the header ID for a given position.
     *
     * @param var1 the position of the item
     * @return the header ID for the item
     */
    @Override
    public long getHeaderId(int var1) {
        User user = this.userList.get(var1);
        char name = user.getName() != null && !user.getName().isEmpty() ? user.getName().substring(0, 1).toUpperCase().toCharArray()[0] : '#';
        return (int) name;
    }

    /**
     * Creates a new header view holder.
     *
     * @param viewGroup the parent view group
     * @return a new StickyViewHolder instance
     */
    @Override
    public StickyViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        return new StickyViewHolder(CometchatUserListStickyHeaderBinding
                                        .inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false)
                                        .getRoot());
    }

    /**
     * Binds the header view holder with the corresponding data.
     *
     * @param view     the StickyViewHolder to bind
     * @param position the position of the item
     * @param var3     additional parameter
     */
    @Override
    public void onBindHeaderViewHolder(StickyViewHolder view, int position, long var3) {
        User user = userList.get(position);
        char name = user.getName() != null && !user.getName().isEmpty() ? user.getName().substring(0, 1).toCharArray()[0] : '#';
        view.binding.tvTitle.setText(String.valueOf(name));
        view.binding.tvTitle.setTextColor(stickyTitleColor);
        view.binding.tvTitle.setTextAppearance(stickyTitleAppearance);
        view.binding.stickyView.setBackgroundColor(stickyTitleBackgroundColor);

        // Update marginStart of the sticky header text programmatically
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.binding.tvTitle.getLayoutParams();
        if (isSelectionEnabled) {
            layoutParams.setMarginStart(context.getResources().getDimensionPixelSize(R.dimen.cometchat_padding_5));
        } else {
            // Checkbox is not visible, align to avatar directly
            layoutParams.setMarginStart(context.getResources().getDimensionPixelSize(R.dimen.cometchat_padding_4));
        }
        view.binding.tvTitle.setLayoutParams(layoutParams);
    }

    /**
     * Retrieves the user at the specified position.
     *
     * @param pos the position of the user
     * @return the User object at the specified position
     */
    public User getUser(int pos) {
        return userList.get(pos);
    }

    /**
     * Sets the listener for the list item view and refreshes the view.
     *
     * @param listItemView the listener for the list item view
     */
    public void setItemView(UsersViewHolderListener listItemView) {
        if (listItemView != null) {
            this.listItemView = listItemView;
            notifyDataSetChanged();
        }
    }

    /**
     * Sets the listener for the subtitle view and refreshes the view.
     *
     * @param subtitleView the listener for the subtitle view
     */
    public void setSubtitleView(UsersViewHolderListener subtitleView) {
        if (subtitleView != null) {
            this.subtitleView = subtitleView;
            notifyDataSetChanged();
        }
    }

    /**
     * Sets the listener for the tail view and refreshes the view.
     *
     * @param tailView the listener for the tail view
     */
    public void setTrailingView(UsersViewHolderListener tailView) {
        if (tailView != null) {
            this.tailView = tailView;
            notifyDataSetChanged();
        }
    }

    public void hideUserStatus(boolean hide) {
        this.hideUserStatus = hide;
        notifyDataSetChanged();
    }

    public void setTitleView(UsersViewHolderListener titleView) {
        this.titleView = titleView;
        notifyDataSetChanged();
    }

    public void setLeadingView(UsersViewHolderListener leadingView) {
        this.leadingView = leadingView;
        notifyDataSetChanged();
    }

    public void setOnItemClick(OnItemClick<User> onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void setOnItemLongClick(OnItemLongClick<User> onItemLongClick) {
        this.onItemLongClick = onItemLongClick;
    }

    /**
     * ViewHolder for the sticky header in the RecyclerView.
     */
    public static class StickyViewHolder extends RecyclerView.ViewHolder {
        private final CometchatUserListStickyHeaderBinding binding;

        StickyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CometchatUserListStickyHeaderBinding.bind(itemView);
        }
    }

    /**
     * ViewHolder for each user item in the RecyclerView.
     */
    public class UserViewHolder extends RecyclerView.ViewHolder {
        CometchatListBaseItemsBinding binding;
        private View customListItemView, customSubtitleView, customTailView, customTitleView, customleadingView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CometchatListBaseItemsBinding.bind(itemView);

            if (listItemView != null) {
                customListItemView = listItemView.createView(context, binding);
                Utils.handleView(binding.parentView, customListItemView, true);
            } else {
                if (leadingView != null) {
                    customleadingView = leadingView.createView(context, binding);
                    Utils.handleView(binding.avatarContainer, customleadingView, true);
                }

                if (titleView != null) {
                    customTitleView = titleView.createView(context, binding);
                    Utils.handleView(binding.titleContainer, customTitleView, true);
                }

                if (subtitleView != null) {
                    customSubtitleView = subtitleView.createView(context, binding);
                    Utils.handleView(binding.subtitleContainer, customSubtitleView, true);
                }
                if (tailView != null) {
                    customTailView = tailView.createView(context, binding);
                    Utils.handleView(binding.tailView, customTailView, true);
                }
            }
        }

        /**
         * Binds the user data to the view components.
         *
         * @param user     the user data to bind
         * @param position the position of the user in the list
         */
        public void bindView(User user, int position) {
            if (listItemView != null) {
                listItemView.bindView(context, customListItemView, user, this, userList, position);
            } else {
                if (leadingView != null) {
                    leadingView.bindView(context, customleadingView, user, this, userList, position);
                } else {
                    binding.avatar.setAvatar(user.getName(), user.getAvatar());
                    binding.avatar.setStyle(avatarStyle);
                    if (Utils.isBlocked(user)) {
                        binding.statusIndicator.setStatusIndicator(StatusIndicator.OFFLINE);
                    } else {
                        binding.statusIndicator.setStatusIndicator(user
                                                                       .getStatus()
                                                                       .equalsIgnoreCase(CometChatConstants.USER_STATUS_ONLINE) && !hideUserStatus ? StatusIndicator.ONLINE : StatusIndicator.OFFLINE);
                    }
                }
                if (titleView != null) {
                    titleView.bindView(context, customTitleView, user, this, userList, position);
                } else {
                    binding.tvTitle.setText(user.getName());
                    binding.tvTitle.setTextAppearance(itemTitleTextAppearance);
                    binding.tvTitle.setTextColor(itemTitleTextColor);
                }

                if (isSelectionEnabled) {
                    Utils.initMaterialCard(binding.checkboxView);
                    binding.ivCheckbox.setImageDrawable(checkBoxSelectIcon);
                    binding.ivCheckbox.setImageTintList(ColorStateList.valueOf(checkBoxSelectIconTint));
                    binding.checkboxView.setStrokeWidth(checkBoxStrokeWidth);
                    binding.checkboxView.setStrokeColor(ColorStateList.valueOf(checkBoxStrokeColor));
                    binding.checkboxView.setRadius(checkBoxCornerRadius);

                    if (!selectedUsers.isEmpty() && selectedUsers.containsKey(user)) {
                        binding.ivCheckbox.setVisibility(View.VISIBLE);
                        binding.checkboxView.setStrokeWidth(0);
                        binding.parentView.setBackgroundColor(itemSelectedBackgroundColor);
                        binding.checkboxView.setCardBackgroundColor(checkBoxCheckedBackgroundColor);
                    } else {
                        binding.ivCheckbox.setVisibility(View.GONE);
                        binding.checkboxView.setStrokeWidth(checkBoxStrokeWidth);
                        binding.parentView.setBackgroundColor(itemBackgroundColor);
                        binding.checkboxView.setCardBackgroundColor(checkBoxBackgroundColor);
                    }
                    binding.checkboxView.setVisibility(View.VISIBLE);
                } else {
                    binding.checkboxView.setStrokeWidth(checkBoxStrokeWidth);
                    binding.parentView.setBackgroundColor(itemBackgroundColor);
                    binding.checkboxView.setVisibility(View.GONE);
                }

                if (subtitleView != null) {
                    subtitleView.bindView(context, customSubtitleView, user, this, userList, position);
                }

                if (tailView != null) {
                    tailView.bindView(context, customTailView, user, this, userList, position);
                }
            }

            binding.parentView.setOnClickListener(v -> {
                if (onItemClick != null) {
                    onItemClick.click(v, position, user);
                }
            });

            binding.parentView.setOnLongClickListener(v -> {
                if (onItemLongClick != null) {
                    onItemLongClick.longClick(v, position, user);
                }
                return false;
            });

            itemView.setTag(R.string.cometchat_user, user);
        }
    }
}
