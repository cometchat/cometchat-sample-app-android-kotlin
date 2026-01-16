package com.cometchat.chatuikit.conversations;

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

import com.cometchat.chat.models.Conversation;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatItemSelectedAvatarBinding;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.utils.ConversationsUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for displaying a list of selected conversations in a RecyclerView. This adapter
 * handles the binding of group data to the views and supports customizable styles.
 */
public class SelectedConversationsAdapter extends
        RecyclerView.Adapter<SelectedConversationsAdapter.SelectedAvatarViewHolder> {

    private final List<Conversation> conversations = new ArrayList<>();
    private OnRemoveClickListener onRemoveClickListener;
    private @StyleRes int avatarStyle;
    private Drawable removeButtonIcon;
    private @StyleRes int itemTextAppearance;
    private @ColorInt int itemTextColor;
    private @ColorInt int removeButtonIconTint;

    /**
     * Constructs the {@code SelectedConversationsAdapter}.
     *
     * <p>
     * Stable IDs are enabled to ensure RecyclerView can correctly
     * track conversation items during insertions and removals.
     */
    public SelectedConversationsAdapter() {
        setHasStableIds(true);
    }

    /**
     * Sets the avatar style applied to conversation avatars.
     *
     * @param avatarStyle Style resource ID for avatars
     */
    public void setAvatarStyle(@StyleRes int avatarStyle) {
        this.avatarStyle = avatarStyle;
        notifyDataSetChanged();
    }

    /**
     * Sets the text appearance for the conversation title.
     *
     * @param appearance Text appearance resource ID
     */
    public void setItemTitleTextAppearance(@StyleRes int appearance) {
        this.itemTextAppearance = appearance;
        notifyDataSetChanged();
    }

    /**
     * Sets the text color for the conversation title.
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
     * Binds conversation data to the ViewHolder.
     *
     * @param holder   ViewHolder instance
     * @param position Adapter position
     */
    @Override
    public void onBindViewHolder(@NonNull SelectedAvatarViewHolder holder, int position) {
        holder.bind(conversations.get(position));
    }

    /**
     * Returns the number of selected conversations.
     *
     * @return Size of the selected conversations list
     */
    @Override
    public int getItemCount() {
        return conversations.size();
    }

    /**
     * Returns a stable and unique ID for the conversation at the given position.
     *
     * <p>
     * The conversation ID is used as the identity source.
     *
     * @param position Adapter position
     * @return Stable ID representing the conversation
     */
    @Override
    public long getItemId(int position) {
        return conversations.get(position).getConversationId().hashCode();
    }

    /**
     * Adds a conversation to the selected list if it does not already exist.
     *
     * @param conversation Conversation to add
     */
    public void addConversation(Conversation conversation) {
        if (indexOf(conversation) == -1) {
            conversations.add(conversation);
            notifyItemInserted(conversations.size() - 1);
        }
    }

    /**
     * Removes a conversation from the selected list.
     *
     * @param conversation Conversation to remove
     */
    public void removeConversation(Conversation conversation) {
        int index = indexOf(conversation);
        if (index != -1) {
            conversations.remove(index);
            notifyItemRemoved(index);
        }
    }

    /**
     * Returns the selected conversations list.
     *
     * @return New list containing selected conversations
     */
    public List<Conversation> getConversations() {
        return new ArrayList<>(conversations);
    }

    /**
     * Finds the index of a conversation in the selected list.
     *
     * @param conversation Conversation to find
     * @return Index if found, otherwise {@code -1}
     */
    private int indexOf(Conversation conversation) {
        for (int i = 0; i < conversations.size(); i++) {
            if (conversations.get(i).getConversationId().equals(conversation.getConversationId())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sets a listener to receive callbacks when a conversation is removed.
     * @param listener Remove click listener
     */
    public void setOnRemoveClickListener(OnRemoveClickListener listener) {
        this.onRemoveClickListener = listener;
    }

    /**
     * ViewHolder representing a single selected conversation item.
     *
     * <p>
     * Responsible for binding conversation avatar, title,
     * styles, and handling remove icon clicks.
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
         * Binds conversation data to the item view.
         *
         * @param conversation Conversation whose data should be displayed
         */
        void bind(Conversation conversation) {
            String name = ConversationsUtils.getConversationTitle(conversation);
            String avatar = ConversationsUtils.getConversationAvatar(conversation);
            binding.selectedAvatar.setAvatar(name, avatar);
            binding.tvSelectedItemName.setText(name);

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
                    onRemoveClickListener.onRemove(conversation);
                }
            });
        }
    }

    /**
     * Callback interface for handling conversation removal actions.
     */
    public interface OnRemoveClickListener {

        /**
         * Called when the remove icon for a conversation is clicked.
         *
         * @param conversation Conversation to be removed
         */
        void onRemove(Conversation conversation);
    }
}