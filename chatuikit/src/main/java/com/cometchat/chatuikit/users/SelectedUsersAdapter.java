package com.cometchat.chatuikit.users;

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
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatItemSelectedAvatarBinding;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for displaying a list of selected users in a RecyclerView. This adapter
 * handles the binding of group data to the views and supports customizable styles.
 */
public class SelectedUsersAdapter extends RecyclerView.Adapter<SelectedUsersAdapter.SelectedAvatarViewHolder> {
    private final List<User> users = new ArrayList<>();
    private OnRemoveClickListener onRemoveClickListener;
    private @StyleRes int avatarStyle;
    private Drawable removeButtonIcon;
    private @StyleRes int itemTextAppearance;
    private @ColorInt int itemTextColor;
    private @ColorInt int removeButtonIconTint;

    /**
     * Constructs a SelectedUsersAdapter.
     */
    public SelectedUsersAdapter() {
        setHasStableIds(true);
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
     * Sets the item title text appearance and refreshes the view.
     *
     * @param itemTitleTextAppearance the text appearance resource ID to set
     */
    public void setItemTitleTextAppearance(@StyleRes int itemTitleTextAppearance) {
        this.itemTextAppearance = itemTitleTextAppearance;
        notifyDataSetChanged();
    }

    /**
     * Sets the item title text color and refreshes the view.
     *
     * @param itemTitleTextColor the color to set for item titles
     */
    public void setItemTitleTextColor(@ColorInt int itemTitleTextColor) {
        this.itemTextColor = itemTitleTextColor;
        notifyDataSetChanged();
    }

    /**
     * Sets the User item Remove icon drawable.
     *
     * @param itemRemoveIcon the drawable to set for the User item Remove icon
     */
    public void setRemoveButtonIcon(Drawable itemRemoveIcon) {
        this.removeButtonIcon = itemRemoveIcon;
        notifyDataSetChanged();
    }

    /**
     * Sets the tint color for the item remove icon tint.
     *
     * @param itemRemoveIconTint the tint color to set for the item remove icon tint
     */
    public void setRemoveButtonIconTint(@ColorInt int itemRemoveIconTint) {
        this.removeButtonIconTint = itemRemoveIconTint;
        notifyDataSetChanged();
    }

    /**
     * Creates a new ViewHolder for the user item.
     *
     * @param parent   the parent view group to which the new view will be attached
     * @param viewType the view type of the new view
     * @return a new SelectedAvatar instance
     */
    @NonNull
    @Override
    public SelectedAvatarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cometchat_item_selected_avatar, parent, false);
        return new SelectedAvatarViewHolder(view);
    }

    /**
     * Binds the user data to the specified ViewHolder.
     *
     * @param holder   the ViewHolder to bind data to
     * @param position the position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull SelectedAvatarViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    /**
     * Returns the total number of items in the adapter.
     *
     * @return the number of items in the user list
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * Returns the stable ID for the item at the given position.
     * @param position Adapter position to query
     * @return Stable ID of the item at position
     */
    @Override
    public long getItemId(int position) {
        return users.get(position).getUid().hashCode();
    }

    /**
     * Adds a user to the selected users list if not already present.
     * Triggers a RecyclerView insert animation when successful.
     *
     * @param user The {@link User} to add
     */
    public void addUser(User user) {
        if (indexOf(user) == -1) {
            users.add(user);
            notifyItemInserted(users.size() - 1);
        } else {
        }
    }

    /**
     * Removes a user from the selected users list.
     * Triggers a RecyclerView remove animation if the user exists.
     *
     * @param user The {@link User} to remove
     */
    public void removeUser(User user) {
        int index = indexOf(user);
        if (index != -1) {
            users.remove(index);
            notifyItemRemoved(index);
        }
    }

    /**
     * Returns a copy of the current selected users list.
     * A defensive copy is returned to prevent external modification.
     * @return A new {@link List} containing selected users
     */
    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    /**
     * Finds the index of the given user in the selected users list.
     * @param user The {@link User} to search for
     * @return Index of the user if found, otherwise {@code -1}
     */
    private int indexOf(User user) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUid().equals(user.getUid())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sets a listener to receive callbacks when the remove icon is clicked for a selected user.
     * @param listener Callback invoked when a user is removed
     */
    public void setOnRemoveClickListener(OnRemoveClickListener listener) {
        this.onRemoveClickListener = listener;
    }

    /**
     * ViewHolder for each user item in the RecyclerView.
     */
    public class SelectedAvatarViewHolder extends RecyclerView.ViewHolder {
        CometchatItemSelectedAvatarBinding binding;

        /**
         * Creates a new ViewHolder instance.
         *
         * @param itemView The inflated item view
         */
        SelectedAvatarViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = CometchatItemSelectedAvatarBinding.bind(itemView);
        }

        /**
         * Binds the given {@link User} data to the item view.
         *
         * @param user The user whose data should be displayed
         */
        void bind(User user) {
            binding.removeButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            binding.removeButton.setPadding(
                    (int) Utils.dpToPx(binding.getRoot().getContext(), 3),
                    (int) Utils.dpToPx(binding.getRoot().getContext(), 3),
                    (int) Utils.dpToPx(binding.getRoot().getContext(), 3),
                    (int) Utils.dpToPx(binding.getRoot().getContext(), 3)
            );

            binding.selectedAvatar.setAvatar(user.getName(), user.getAvatar());
            binding.tvSelectedItemName.setText(user.getName());
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
                    onRemoveClickListener.onRemove(user);
                }
            });
        }
    }

    /**
     * Listener interface for handling remove actions
     * on selected users.
     */
    public interface OnRemoveClickListener {

        /**
         * Called when the remove icon for a user is clicked.
         * @param user The user to be removed
         */
        void onRemove(User user);
    }
}