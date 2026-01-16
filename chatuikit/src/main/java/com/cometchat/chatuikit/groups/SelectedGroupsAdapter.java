package com.cometchat.chatuikit.groups;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.models.Group;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatItemSelectedAvatarBinding;
import com.cometchat.chatuikit.shared.resources.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for displaying a list of selected groups in a RecyclerView. This adapter
 * handles the binding of group data to the views and supports customizable styles.
 */

public class SelectedGroupsAdapter extends RecyclerView.Adapter<SelectedGroupsAdapter.SelectedAvatarViewHolder> {

    private final List<Group> groups = new ArrayList<>();
    private OnRemoveClickListener onRemoveClickListener;
    private @StyleRes int avatarStyle;
    private Drawable removeButtonIcon;
    private @StyleRes int itemTextAppearance;
    private @ColorInt int itemTextColor;
    private @ColorInt int removeButtonIconTint;

    /**
     * Creates a new {@code SelectedGroupsAdapter}.
     * <p>
     * Stable IDs are enabled to ensure RecyclerView correctly tracks
     * group items during insertions and removals.
     */
    public SelectedGroupsAdapter() {
        setHasStableIds(true);
    }

    /**
     * Sets the avatar style for group avatars.
     *
     * @param avatarStyle Style resource ID for the avatar
     */
    public void setAvatarStyle(@StyleRes int avatarStyle) {
        this.avatarStyle = avatarStyle;
        notifyDataSetChanged();
    }

    /**
     * Sets the text appearance for the group name.
     *
     * @param appearance Text appearance resource ID
     */
    public void setItemTitleTextAppearance(@StyleRes int appearance) {
        this.itemTextAppearance = appearance;
        notifyDataSetChanged();
    }

    /**
     * Sets the text color for the group name.
     *
     * @param color Text color value
     */
    public void setItemTitleTextColor(@ColorInt int color) {
        this.itemTextColor = color;
        notifyDataSetChanged();
    }

    /**
     * Sets the drawable used as the remove icon.
     *
     * @param icon Drawable for remove action
     */
    public void setRemoveButtonIcon(Drawable icon) {
        this.removeButtonIcon = icon;
        notifyDataSetChanged();
    }

    /**
     * Sets the tint color for the remove icon.
     *
     * @param color Tint color value
     */
    public void setRemoveButtonIconTint(@ColorInt int color) {
        this.removeButtonIconTint = color;
        notifyDataSetChanged();
    }

    /**
     * Inflates the layout and creates a new {@link SelectedAvatarViewHolder} ViewHolder.
     *
     * @param parent   Parent ViewGroup
     * @param viewType View type of the new item
     * @return A new {@link SelectedAvatarViewHolder} instance
     */
    @NonNull
    @Override
    public SelectedAvatarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cometchat_item_selected_avatar, parent, false);
        return new SelectedAvatarViewHolder(view);
    }

    /**
     * Binds group data to the ViewHolder.
     *
     * @param holder   ViewHolder instance
     * @param position Adapter position
     */
    @Override
    public void onBindViewHolder(@NonNull SelectedAvatarViewHolder holder, int position) {
        holder.bind(groups.get(position));
    }

    /**
     * Returns the number of selected groups.
     *
     * @return Size of the groups list
     */
    @Override
    public int getItemCount() {
        return groups.size();
    }

    /**
     * Returns a stable and unique ID for the group at the given position.
     *
     * <p>
     * The group's GUID is used as the identity source.
     *
     * @param position Adapter position
     * @return Stable ID representing the group
     */
    @Override
    public long getItemId(int position) {
        return groups.get(position).getGuid().hashCode();
    }

    /**
     * Adds a group to the selected list if it does not already exist.
     * @param group Group to add
     */
    public void addGroup(Group group) {
        if (indexOf(group) == -1) {
            groups.add(group);
            notifyItemInserted(groups.size() - 1);
        }
    }

    /**
     * Removes a group from the selected list.
     * @param group Group to remove
     */
    public void removeGroup(Group group) {
        int index = indexOf(group);
        if (index != -1) {
            groups.remove(index);
            notifyItemRemoved(index);
        }
    }

    /**
     * Returns a defensive copy of the selected groups list.
     * @return New list containing selected groups
     */
    public List<Group> getGroups() {
        return new ArrayList<>(groups);
    }

    /**
     * Finds the index of a group in the selected list.
     * @param group Group to find
     * @return Index if found, otherwise {@code -1}
     */
    private int indexOf(Group group) {
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getGuid().equals(group.getGuid())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sets a listener to receive callbacks when a group is removed.
     * @param listener Remove click listener
     */
    public void setOnRemoveClickListener(OnRemoveClickListener listener) {
        this.onRemoveClickListener = listener;
    }

    /**
     * ViewHolder representing a single selected group item.
     * <p>
     * Responsible for binding group avatar, name, styles,
     * and handling remove icon clicks.
     */
    public class SelectedAvatarViewHolder extends RecyclerView.ViewHolder {
        CometchatItemSelectedAvatarBinding binding;

        /**
         * Creates a new ViewHolder instance.
         * @param itemView Inflated item view
         */
        SelectedAvatarViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = CometchatItemSelectedAvatarBinding.bind(itemView);
        }

        /**
         * Binds group data to the item view.
         * @param group Group whose data should be displayed
         */
        void bind(Group group) {
            binding.selectedAvatar.setAvatar(group.getName(), group.getIcon());
            binding.tvSelectedItemName.setText(group.getName());

            binding.removeButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            binding.removeButton.setPadding(
                    (int) Utils.dpToPx(binding.getRoot().getContext(), 3),
                    (int) Utils.dpToPx(binding.getRoot().getContext(), 3),
                    (int) Utils.dpToPx(binding.getRoot().getContext(), 3),
                    (int) Utils.dpToPx(binding.getRoot().getContext(), 3)
            );

            if (avatarStyle != 0) {
                binding.selectedAvatar.setStyle(avatarStyle);
            }
            if (itemTextAppearance != 0) {
                binding.tvSelectedItemName.setTextAppearance(itemTextAppearance);
            }
            if (itemTextColor != 0) {
                binding.tvSelectedItemName.setTextColor(itemTextColor);
            }
            if (removeButtonIcon != null) {
                binding.removeButton.setImageDrawable(removeButtonIcon);
            }
            if(removeButtonIconTint != 0){
                binding.removeButton.setImageTintList(ColorStateList.valueOf(removeButtonIconTint));
            }
            binding.removeButton.setOnClickListener(v -> {
                if (onRemoveClickListener != null) {
                    onRemoveClickListener.onRemove(group);
                }
            });
        }
    }


    /**
     * Callback interface for handling group removal actions.
     */
    public interface OnRemoveClickListener {
        /**
         * Called when the remove icon for a group is clicked.
         * @param group Group to be removed
         */
        void onRemove(Group group);
    }
}