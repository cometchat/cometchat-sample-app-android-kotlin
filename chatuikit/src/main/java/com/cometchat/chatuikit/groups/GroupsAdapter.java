package com.cometchat.chatuikit.groups;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.models.Group;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatListBaseItemsBinding;
import com.cometchat.chatuikit.shared.interfaces.OnItemClick;
import com.cometchat.chatuikit.shared.interfaces.OnItemLongClick;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.viewholders.GroupsViewHolderListener;
import com.cometchat.chatuikit.shared.views.statusindicator.StatusIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Adapter class for displaying a list of groups in a RecyclerView. This adapter
 * handles the binding of group data to the views and supports selection of
 * groups with customizable styles.
 */
public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {
    private static final String TAG = GroupsAdapter.class.getSimpleName();
    private final Context context;
    private boolean hideGroupType = false;
    private boolean isSelectionEnabled;
    private List<Group> groupList;
    private HashMap<Group, Boolean> selectedGroups;
    private GroupsViewHolderListener tailView;
    private GroupsViewHolderListener subtitleView;
    private GroupsViewHolderListener listItemView;
    private GroupsViewHolderListener leadingView;
    private GroupsViewHolderListener titleView;

    private @StyleRes int avatarStyle;
    private @StyleRes int itemTitleTextAppearance;
    private @ColorInt int itemTitleTextColor;
    private @ColorInt int itemBackgroundColor;
    private @StyleRes int statusIndicatorStyle;
    private @ColorInt int itemSelectedBackgroundColor;
    private @StyleRes int subtitleTextAppearance;
    private @ColorInt int subtitleTextColor;
    private @Dimension int checkBoxStrokeWidth;
    private @Dimension int checkBoxCornerRadius;
    private @ColorInt int checkBoxStrokeColor;
    private @ColorInt int checkBoxBackgroundColor;
    private @ColorInt int checkBoxCheckedBackgroundColor;
    private @ColorInt int checkBoxSelectIconTint;
    private Drawable checkBoxSelectIcon;
    private OnItemClick<Group> onItemClick;
    private OnItemLongClick<Group> onItemLongClick;

    /**
     * Constructor for GroupsAdapter.
     *
     * @param context The context in which the adapter is being used.
     */
    public GroupsAdapter(Context context) {
        this.context = context;
        groupList = new ArrayList<>();
        selectGroup(new HashMap<>());
    }

    /**
     * Sets the selected groups.
     *
     * @param hashMap A HashMap containing the selected groups.
     */
    public void selectGroup(HashMap<Group, Boolean> hashMap) {
        if (hashMap != null) {
            this.selectedGroups = hashMap;
        }
        notifyDataSetChanged();
    }

    /**
     * Called when RecyclerView needs a new {@link GroupViewHolder} of the given
     * type to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is
     *                 bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new GroupViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupViewHolder(CometchatListBaseItemsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false).getRoot());
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents
     *                 of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        holder.bindView(groupList.get(position), position);
    }

    /**
     * Returns the view type of the item at the specified position.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The view type of the item at the specified position.
     */
    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return groupList.size();
    }

    /**
     * Sets the list of groups to be displayed by the adapter.
     *
     * @param list The list of groups to be displayed.
     */
    public void setGroupList(List<Group> list) {
        if (list != null) {
            this.groupList = list;
            notifyDataSetChanged();
        }
    }

    /**
     * Returns the avatar style resource ID.
     *
     * @return The avatar style resource ID.
     */
    public @StyleRes int getAvatarStyle() {
        return avatarStyle;
    }

    /**
     * Sets the avatar style resource ID.
     *
     * @param avatarStyle The avatar style resource ID.
     */
    public void setAvatarStyle(@StyleRes int avatarStyle) {
        this.avatarStyle = avatarStyle;
        notifyDataSetChanged();
    }

    /**
     * Returns the item title text appearance resource ID.
     *
     * @return The item title text appearance resource ID.
     */
    public @StyleRes int getItemTitleTextAppearance() {
        return itemTitleTextAppearance;
    }

    /**
     * Sets the item title text appearance resource ID.
     *
     * @param itemTitleTextAppearance The item title text appearance resource ID.
     */
    public void setItemTitleTextAppearance(@StyleRes int itemTitleTextAppearance) {
        this.itemTitleTextAppearance = itemTitleTextAppearance;
        notifyDataSetChanged();
    }

    /**
     * Returns the item title text color.
     *
     * @return The item title text color.
     */
    public @ColorInt int getItemTitleTextColor() {
        return itemTitleTextColor;
    }

    /**
     * Sets the item title text color.
     *
     * @param itemTitleTextColor The item title text color.
     */
    public void setItemTitleTextColor(@ColorInt int itemTitleTextColor) {
        this.itemTitleTextColor = itemTitleTextColor;
        notifyDataSetChanged();
    }

    /**
     * Returns the item background color.
     *
     * @return The item background color.
     */
    public @ColorInt int getItemBackgroundColor() {
        return itemBackgroundColor;
    }

    /**
     * Sets the item background color.
     *
     * @param itemBackgroundColor The item background color.
     */
    public void setItemBackgroundColor(@ColorInt int itemBackgroundColor) {
        this.itemBackgroundColor = itemBackgroundColor;
        notifyDataSetChanged();
    }

    /**
     * Returns the status indicator style resource ID.
     *
     * @return The status indicator style resource ID.
     */
    public @StyleRes int getStatusIndicatorStyle() {
        return statusIndicatorStyle;
    }

    /**
     * Sets the status indicator style resource ID.
     *
     * @param statusIndicatorStyle The status indicator style resource ID.
     */
    public void setStatusIndicatorStyle(@StyleRes int statusIndicatorStyle) {
        this.statusIndicatorStyle = statusIndicatorStyle;
        notifyDataSetChanged();
    }

    /**
     * Returns the item selected background color.
     *
     * @return The item selected background color.
     */
    public @ColorInt int getItemSelectedBackgroundColor() {
        return itemSelectedBackgroundColor;
    }

    /**
     * Sets the item selected background color.
     *
     * @param itemSelectedBackgroundColor The item selected background color.
     */
    public void setItemSelectedBackgroundColor(@ColorInt int itemSelectedBackgroundColor) {
        this.itemSelectedBackgroundColor = itemSelectedBackgroundColor;
        notifyDataSetChanged();
    }

    /**
     * Returns the checkbox stroke width.
     *
     * @return The checkbox stroke width.
     */
    public @Dimension int getCheckBoxStrokeWidth() {
        return checkBoxStrokeWidth;
    }

    /**
     * Sets the checkbox stroke width.
     *
     * @param checkBoxStrokeWidth The checkbox stroke width.
     */
    public void setCheckBoxStrokeWidth(@Dimension int checkBoxStrokeWidth) {
        this.checkBoxStrokeWidth = checkBoxStrokeWidth;
        notifyDataSetChanged();
    }

    /**
     * Returns the checkbox corner radius.
     *
     * @return The checkbox corner radius.
     */
    public @Dimension int getCheckBoxCornerRadius() {
        return checkBoxCornerRadius;
    }

    /**
     * Sets the checkbox corner radius.
     *
     * @param checkBoxCornerRadius The checkbox corner radius.
     */
    public void setCheckBoxCornerRadius(@Dimension int checkBoxCornerRadius) {
        this.checkBoxCornerRadius = checkBoxCornerRadius;
        notifyDataSetChanged();
    }

    /**
     * Returns the checkbox stroke color.
     *
     * @return The checkbox stroke color.
     */
    public @ColorInt int getCheckBoxStrokeColor() {
        return checkBoxStrokeColor;
    }

    /**
     * Sets the checkbox stroke color.
     *
     * @param checkBoxStrokeColor The checkbox stroke color.
     */
    public void setCheckBoxStrokeColor(@ColorInt int checkBoxStrokeColor) {
        this.checkBoxStrokeColor = checkBoxStrokeColor;
        notifyDataSetChanged();
    }

    /**
     * Returns the checkbox background color.
     *
     * @return The checkbox background color.
     */
    public @ColorInt int getCheckBoxBackgroundColor() {
        return checkBoxBackgroundColor;
    }

    /**
     * Sets the checkbox background color.
     *
     * @param checkBoxBackgroundColor The checkbox background color.
     */
    public void setCheckBoxBackgroundColor(@ColorInt int checkBoxBackgroundColor) {
        this.checkBoxBackgroundColor = checkBoxBackgroundColor;
        notifyDataSetChanged();
    }

    /**
     * Returns the checkbox checked background color.
     *
     * @return The checkbox checked background color.
     */
    public @ColorInt int getCheckBoxCheckedBackgroundColor() {
        return checkBoxCheckedBackgroundColor;
    }

    /**
     * Sets the checkbox checked background color.
     *
     * @param checkBoxCheckedBackgroundColor The checkbox checked background color.
     */
    public void setCheckBoxCheckedBackgroundColor(@ColorInt int checkBoxCheckedBackgroundColor) {
        this.checkBoxCheckedBackgroundColor = checkBoxCheckedBackgroundColor;
        notifyDataSetChanged();
    }

    /**
     * Returns whether selection is enabled.
     *
     * @return True if selection is enabled, false otherwise.
     */
    public boolean isSelectionEnabled() {
        return isSelectionEnabled;
    }

    /**
     * Sets whether selection is enabled.
     *
     * @param selectionEnabled True to enable selection, false to disable.
     */
    public void isSelectionEnabled(boolean selectionEnabled) {
        isSelectionEnabled = selectionEnabled;
        notifyDataSetChanged();
    }

    /**
     * Returns the subtitle text color.
     *
     * @return The subtitle text color.
     */
    public @ColorInt int getSubtitleTextColor() {
        return subtitleTextColor;
    }

    /**
     * Sets the subtitle text color.
     *
     * @param subtitleTextColor The subtitle text color.
     */
    public void setSubtitleTextColor(@ColorInt int subtitleTextColor) {
        this.subtitleTextColor = subtitleTextColor;
        notifyDataSetChanged();
    }

    /**
     * Returns the subtitle text appearance resource ID.
     *
     * @return The subtitle text appearance resource ID.
     */
    public @StyleRes int getSubtitleTextAppearance() {
        return subtitleTextAppearance;
    }

    /**
     * Sets the subtitle text appearance resource ID.
     *
     * @param subtitleTextAppearance The subtitle text appearance resource ID.
     */
    public void setSubtitleTextAppearance(@StyleRes int subtitleTextAppearance) {
        this.subtitleTextAppearance = subtitleTextAppearance;
        notifyDataSetChanged();
    }

    /**
     * Returns the checkbox select icon.
     *
     * @return The checkbox select icon.
     */
    public Drawable getCheckBoxSelectIcon() {
        return checkBoxSelectIcon;
    }

    /**
     * Sets the checkbox select icon.
     *
     * @param checkBoxSelectIcon The checkbox select icon.
     */
    public void setCheckBoxSelectIcon(Drawable checkBoxSelectIcon) {
        this.checkBoxSelectIcon = checkBoxSelectIcon;
    }

    /**
     * Returns the checkbox select icon tint color.
     *
     * @return The checkbox select icon tint color.
     */
    public @ColorInt int getCheckBoxSelectIconTint() {
        return checkBoxSelectIconTint;
    }

    /**
     * Sets the checkbox select icon tint color.
     *
     * @param checkBoxSelectIconTint The checkbox select icon tint color.
     */
    public void setCheckBoxSelectIconTint(@ColorInt int checkBoxSelectIconTint) {
        this.checkBoxSelectIconTint = checkBoxSelectIconTint;
    }

    /**
     * Returns the group at the specified position.
     *
     * @param pos The position of the group in the list.
     * @return The group at the specified position.
     */
    public Group getGroup(int pos) {
        return groupList.get(pos);
    }

    /**
     * Sets the listener for the list item view.
     *
     * @param listItemView The listener for the list item view.
     */
    public void setItemView(GroupsViewHolderListener listItemView) {
        if (listItemView != null) {
            this.listItemView = listItemView;
            notifyDataSetChanged();
        }
    }

    /**
     * Sets the listener for the subtitle view.
     *
     * @param subtitleView The listener for the subtitle view.
     */
    public void setSubtitleView(GroupsViewHolderListener subtitleView) {
        if (subtitleView != null) {
            this.subtitleView = subtitleView;
            notifyDataSetChanged();
        }
    }

    /**
     * Sets the listener for the tail view.
     *
     * @param tailView The listener for the tail view.
     */
    public void setTrailingView(GroupsViewHolderListener tailView) {
        if (tailView != null) {
            this.tailView = tailView;
            notifyDataSetChanged();
        }
    }

    public void setTitleView(GroupsViewHolderListener titleView) {
        this.titleView = titleView;
        notifyDataSetChanged();
    }

    public void setLeadingView(GroupsViewHolderListener leadingView) {
        this.leadingView = leadingView;
        notifyDataSetChanged();
    }

    public void hideGroupType(boolean hide) {
        this.hideGroupType = hide;
        notifyDataSetChanged();
    }

    public void setOnItemClick(OnItemClick<Group> onItemClickListener) {
        this.onItemClick = onItemClickListener;
    }

    public void setOnItemLongClick(OnItemLongClick<Group> onItemLongClickListener) {
        this.onItemLongClick = onItemLongClickListener;
    }

    /**
     * ViewHolder class for group items.
     */
    public class GroupViewHolder extends RecyclerView.ViewHolder {
        CometchatListBaseItemsBinding binding;
        private View customListItemView, customSubtitleView, customTailView, customTitleView, customLeadingView;

        /**
         * Constructor for GroupViewHolder.
         *
         * @param itemView The view of the group item.
         */
        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CometchatListBaseItemsBinding.bind(itemView);
            if (listItemView != null) {
                customListItemView = listItemView.createView(context, binding);
                Utils.handleView(binding.parentView, customListItemView, true);
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
                if (tailView != null) {
                    customTailView = tailView.createView(context, binding);
                    Utils.handleView(binding.tailView, customTailView, true);
                }
            }
        }

        /**
         * Binds the group data to the views.
         *
         * @param group    The group data to be bound.
         * @param position The position of the group in the list.
         */
        public void bindView(Group group, int position) {
            if (listItemView != null) {
                listItemView.bindView(context, customListItemView, group, this, groupList, position);
            } else {
                if (leadingView != null)
                    leadingView.bindView(context, customLeadingView, group, this, groupList, position);
                else {
                    binding.avatar.setAvatar(group.getName(), group.getIcon());
                    binding.avatar.setStyle(avatarStyle);
                    handleGroupType(group);
                }

                if (titleView != null) {
                    titleView.bindView(context, customTitleView, group, this, groupList, position);
                } else {
                    binding.tvTitle.setText(group.getName());
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
                    if (!selectedGroups.isEmpty() && selectedGroups.containsKey(group)) {
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
                    subtitleView.bindView(context, customSubtitleView, group, this, groupList, position);
                } else {
                    TextView subtitleText = new TextView(context);
                    String members = group.getMembersCount() + " " + context.getResources().getString(R.string.cometchat_members);
                    subtitleText.setTextAppearance(subtitleTextAppearance);
                    subtitleText.setTextColor(subtitleTextColor);
                    subtitleText.setMaxLines(1);
                    subtitleText.setEllipsize(TextUtils.TruncateAt.END);
                    subtitleText.setText(members);
                    binding.subtitleContainer.removeAllViews();
                    binding.subtitleContainer.addView(subtitleText);
                }

                if (tailView != null) {
                    tailView.bindView(context, customTailView, group, this, groupList, position);
                }
            }

            binding.parentView.setOnClickListener( v -> {
                if (onItemClick != null) {
                    onItemClick.click(v, position, group);
                }
            });

            binding.parentView.setOnLongClickListener(v -> {
                if (onItemLongClick != null) {
                    onItemLongClick.longClick(v, position, group);
                }
                return false;
            });

            itemView.setTag(R.string.cometchat_group, group);
        }

        /**
         * Handles the group type and sets the appropriate status indicator.
         *
         * @param group The group whose type is to be handled.
         */
        private void handleGroupType(Group group) {
            if (!hideGroupType) {
                binding.statusIndicator.setVisibility(View.VISIBLE);
                if (group.getGroupType().equals(CometChatConstants.GROUP_TYPE_PASSWORD)) {
                    binding.statusIndicator.setStatusIndicator(StatusIndicator.PROTECTED_GROUP);
                } else if (group.getGroupType().equals(CometChatConstants.GROUP_TYPE_PRIVATE)) {
                    binding.statusIndicator.setStatusIndicator(StatusIndicator.PRIVATE_GROUP);
                } else {
                    binding.statusIndicator.setStatusIndicator(StatusIndicator.PUBLIC_GROUP);
                }
            } else {
                binding.statusIndicator.setVisibility(View.GONE);
            }
        }
    }
}
