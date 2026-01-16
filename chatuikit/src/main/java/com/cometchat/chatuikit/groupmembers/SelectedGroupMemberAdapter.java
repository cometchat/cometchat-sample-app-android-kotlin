package com.cometchat.chatuikit.groupmembers;

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
import com.cometchat.chat.models.GroupMember;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatItemSelectedAvatarBinding;
import com.cometchat.chatuikit.shared.resources.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for displaying a list of selected group members in a RecyclerView. This adapter
 * handles the binding of group data to the views and supports customizable styles.
 */
public class SelectedGroupMemberAdapter extends RecyclerView.Adapter<SelectedGroupMemberAdapter.SelectedAvatarViewHolder> {

    private final List<GroupMember> groupMembers = new ArrayList<>();
    private OnRemoveClickListener onRemoveClickListener;
    private @StyleRes int avatarStyle;
    private Drawable removeButtonIcon;
    private @StyleRes int itemTextAppearance;
    private @ColorInt int itemTextColor;
    private @ColorInt int removeButtonIconTint;

    /**
     * Constructs the {@code SelectedGroupMemberAdapter}.
     *
     * <p>
     * Stable IDs are enabled to ensure RecyclerView can correctly
     * track group member items during insertions and removals.
     */
    public SelectedGroupMemberAdapter() {
        setHasStableIds(true);
    }

    /**
     * Sets the avatar style applied to group member avatars.
     *
     * @param avatarStyle Style resource ID for avatars
     */
    public void setAvatarStyle(@StyleRes int avatarStyle) {
        this.avatarStyle = avatarStyle;
        notifyDataSetChanged();
    }

    /**
     * Sets the text appearance for the group member name.
     *
     * @param appearance Text appearance resource ID
     */
    public void setItemTitleTextAppearance(@StyleRes int appearance) {
        this.itemTextAppearance = appearance;
        notifyDataSetChanged();
    }

    /**
     * Sets the text color for the group member name.
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
     * Binds group member data to the ViewHolder.
     *
     * @param holder   ViewHolder instance
     * @param position Adapter position
     */
    @Override
    public void onBindViewHolder(@NonNull SelectedAvatarViewHolder holder, int position) {
        holder.bind(groupMembers.get(position));
    }

    /**
     * Returns the number of selected group members.
     *
     * @return Size of the selected group members list
     */
    @Override
    public int getItemCount() {
        return groupMembers.size();
    }

    /**
     * Returns a stable and unique ID for the group member at the given position.
     *
     * <p>
     * The group member UID is used as the identity source.
     *
     * @param position Adapter position
     * @return Stable ID representing the group member
     */
    @Override
    public long getItemId(int position) {
        return groupMembers.get(position).getUid().hashCode();
    }

    /**
     * Adds a group member to the selected list if not already present.
     *
     * @param groupMember Group member to add
     */
    public void addGroupMember(GroupMember groupMember) {
        if (indexOf(groupMember) == -1) {
            groupMembers.add(groupMember);
            notifyItemInserted(groupMembers.size() - 1);
        }
    }

    /**
     * Removes a group member from the selected list.
     *
     * @param groupMember Group member to remove
     */
    public void removeGroupMember(GroupMember groupMember) {
        int index = indexOf(groupMember);
        if (index != -1) {
            groupMembers.remove(index);
            notifyItemRemoved(index);
        }
    }

    /**
     * Returns a defensive copy of the selected group members list.
     *
     * @return New list containing selected group members
     */
    public List<GroupMember> getGroupMembers() {
        return new ArrayList<>(groupMembers);
    }

    /**
     * Finds the index of a group member in the selected list.
     *
     * @param groupMember Group member to find
     * @return Index if found, otherwise {@code -1}
     */
    private int indexOf(GroupMember groupMember) {
        for (int i = 0; i < groupMembers.size(); i++) {
            if (groupMembers.get(i).getUid().equals(groupMember.getUid())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sets a listener to receive callbacks when a group member is removed.
     *
     * @param listener Remove click listener
     */
    public void setOnRemoveClickListener(OnRemoveClickListener listener) {
        this.onRemoveClickListener = listener;
    }

    /**
     * ViewHolder representing a single selected group member item.
     *
     * <p>
     * Responsible for binding avatar, name, styles,
     * and handling remove icon click events.
     */
    public class SelectedAvatarViewHolder extends RecyclerView.ViewHolder {
        CometchatItemSelectedAvatarBinding binding;

        /**
         * Creates a new ViewHolder instance.
         *
         * @param itemView Inflated item view
         */
        SelectedAvatarViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = CometchatItemSelectedAvatarBinding.bind(itemView);
        }

        /**
         * Binds group member data to the item view.
         *
         * @param groupMember Group member whose data should be displayed
         */
        void bind(GroupMember groupMember) {
            binding.selectedAvatar.setAvatar(groupMember.getName(), groupMember.getAvatar());
            binding.tvSelectedItemName.setText(groupMember.getName());

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
                    onRemoveClickListener.onRemove(groupMember);
                }
            });
        }
    }

    /**
     * Callback interface for handling group member removal actions.
     */
    public interface OnRemoveClickListener {

        /**
         * Called when the remove icon for a group member is clicked.
         *
         * @param groupMember Group member to be removed
         */
        void onRemove(GroupMember groupMember);
    }
}