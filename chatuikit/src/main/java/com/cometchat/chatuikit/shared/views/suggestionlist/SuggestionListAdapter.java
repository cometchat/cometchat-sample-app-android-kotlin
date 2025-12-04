package com.cometchat.chatuikit.shared.views.suggestionlist;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatSuggestionListItemsBinding;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;

import java.util.List;

/**
 * Adapter for managing a list of suggestion items in a RecyclerView. This
 * adapter is responsible for creating and binding views for each item in the
 * suggestion list and managing the display of item avatars and text styles.
 */
public class SuggestionListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = SuggestionListAdapter.class.getSimpleName();
    private final Context context;
    private List<SuggestionItem> suggestionItemList;
    private SuggestionListViewHolderListener suggestionListViewHolderListener;

    private @StyleRes int suggestionListItemAvatarStyle;
    private @StyleRes int suggestionListItemTextAppearance;
    private @ColorInt int suggestionListItemTextColor;
    private @ColorInt int suggestionListItemInfoTextColor;
    private @StyleRes int suggestionListItemInfoTextAppearance;
    private boolean showAvatar = true;

    /**
     * Constructor to initialize the SuggestionListAdapter with context and item
     * list.
     *
     * @param context            the context in which the adapter is running
     * @param suggestionItemList the list of suggestion items to display
     */
    public SuggestionListAdapter(Context context, List<SuggestionItem> suggestionItemList) {
        this.context = context;
        this.suggestionItemList = suggestionItemList;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CometchatSuggestionListItemsBinding binding = CometchatSuggestionListItemsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TagViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SuggestionItem suggestionItem = suggestionItemList.get(position);
        if (holder instanceof TagViewHolder) {
            ((TagViewHolder) holder).bindView(suggestionItem, position);
        }
    }

    @Override
    public int getItemCount() {
        return suggestionItemList != null ? suggestionItemList.size() : 0;
    }

    /**
     * Updates the suggestion item list and notifies the adapter of the change.
     *
     * @param suggestionItemList the new list of suggestion items
     */
    public void updateList(List<SuggestionItem> suggestionItemList) {
        if (suggestionItemList != null) {
            this.suggestionItemList = suggestionItemList;
            notifyDataSetChanged();
        }
    }

    /**
     * Sets the listener for creating and binding list item views.
     *
     * @param suggestionListViewHolderListener the listener to handle view creation and binding
     */
    public void setListItemView(SuggestionListViewHolderListener suggestionListViewHolderListener) {
        if (this.suggestionListViewHolderListener != null) {
            this.suggestionListViewHolderListener = suggestionListViewHolderListener;
        }
    }

    /**
     * Retrieves the style resource ID for the suggestion list item avatar.
     *
     * @return the style resource ID for the suggestion list item avatar
     */
    public @StyleRes int getSuggestionListItemAvatarStyle() {
        return suggestionListItemAvatarStyle;
    }

    /**
     * Sets the style resource ID for the suggestion list item avatar and notifies
     * the adapter of the change.
     *
     * @param suggestionListItemAvatarStyle the style resource ID to apply to the suggestion list item avatar
     */
    public void setSuggestionListItemAvatarStyle(@StyleRes int suggestionListItemAvatarStyle) {
        this.suggestionListItemAvatarStyle = suggestionListItemAvatarStyle;
        notifyDataSetChanged();
    }

    /**
     * Retrieves the style resource ID for the suggestion list item text appearance.
     *
     * @return the style resource ID for the suggestion list item text appearance
     */
    public @StyleRes int getSuggestionListItemTextAppearance() {
        return suggestionListItemTextAppearance;
    }

    /**
     * Sets the style resource ID for the suggestion list item text appearance and
     * notifies the adapter of the change.
     *
     * @param suggestionListItemTextAppearance the style resource ID to apply to the suggestion list item text
     */
    public void setSuggestionListItemTextAppearance(@StyleRes int suggestionListItemTextAppearance) {
        this.suggestionListItemTextAppearance = suggestionListItemTextAppearance;
        notifyDataSetChanged();
    }

    /**
     * Retrieves the color resource ID for the suggestion list item text.
     *
     * @return the color resource ID for the suggestion list item text
     */
    public @ColorInt int getSuggestionListItemTextColor() {
        return suggestionListItemTextColor;
    }

    /**
     * Sets the color resource ID for the suggestion list item text and notifies the
     * adapter of the change.
     *
     * @param suggestionListItemTextColor the color resource ID to apply to the suggestion list item text
     */
    public void setSuggestionListItemTextColor(@ColorInt int suggestionListItemTextColor) {
        this.suggestionListItemTextColor = suggestionListItemTextColor;
        notifyDataSetChanged();
    }

    /**
     * Sets the color resource ID for the suggestion list item info text.
     * @param suggestionListItemInfoTextColor the color resource ID to apply to the suggestion list item info text
     */
    public void setSuggestionListItemInfoTextColor(int suggestionListItemInfoTextColor) {
        this.suggestionListItemInfoTextColor = suggestionListItemInfoTextColor;
    }

    /**
     * Retrieves the color resource ID for the suggestion list item info text.
     *
     * @return the color resource ID for the suggestion list item info text
     */
    public int getSuggestionListItemInfoTextColor() {
        return suggestionListItemInfoTextColor;
    }

    /**
     * Sets the style resource ID for the suggestion list item info text appearance.
     *
     * @param suggestionListItemInfoTextAppearance the style resource ID to apply to the suggestion list item info text appearance
     */
    public void setSuggestionListItemInfoTextAppearance(int suggestionListItemInfoTextAppearance) {
        this.suggestionListItemInfoTextAppearance = suggestionListItemInfoTextAppearance;
    }

    /**
     * Retrieves the style resource ID for the suggestion list item info text appearance.
     *
     * @return the style resource ID for the suggestion list item info text appearance
     */
    public int getSuggestionListItemInfoTextAppearance() {
        return suggestionListItemInfoTextAppearance;
    }

    /**
     * Checks whether the avatar should be displayed.
     *
     * @return true if the avatar should be shown; false otherwise
     */
    public boolean showAvatar() {
        return showAvatar;
    }

    /**
     * Sets whether to show the avatar in the suggestion list.
     *
     * @param showAvatar true to display the avatar; false to hide it
     */
    public void showAvatar(boolean showAvatar) {
        this.showAvatar = showAvatar;
    }



    /**
     * ViewHolder class for managing individual suggestion item views.
     */
    public class TagViewHolder extends RecyclerView.ViewHolder {
        private final CometchatSuggestionListItemsBinding binding;

        /**
         * Constructor to initialize the TagViewHolder with an item view.
         *
         * @param itemView the view for a single item in the RecyclerView
         */
        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CometchatSuggestionListItemsBinding.bind(itemView);

            if (suggestionListViewHolderListener != null) {
                View listItemView = suggestionListViewHolderListener.createView(context, binding);
                if (listItemView != null) {
                    binding.parentLayout.removeAllViews();
                    binding.parentLayout.addView(listItemView);
                }
            }
        }

        /**
         * Binds data to the view for a suggestion item.
         *
         * @param suggestionItem the suggestion item to bind
         * @param position       the position of the item in the list
         */
        public void bindView(SuggestionItem suggestionItem, int position) {
            if (showAvatar) {
                String iconUrl = suggestionItem.getLeadingIconUrl();
                String nameText = suggestionItem.getName();

                // Check if URL is valid
                if (isValidUrl(iconUrl)) {
                    // If URL is valid, use name as first argument and URL as second
                    binding.suggestionItemAvatar.setAvatar(nameText, iconUrl);
                } else if (!TextUtils.isEmpty(iconUrl)) {
                    // If URL is not valid but not empty/null, use URL as first argument instead of name
                    binding.suggestionItemAvatar.setAvatar(iconUrl, (String) null);
                } else {
                    // If URL is null or empty, keep the original flow (use name and URL)
                    binding.suggestionItemAvatar.setAvatar(nameText, iconUrl);
                }

                binding.suggestionItemAvatar.setStyle(suggestionListItemAvatarStyle);
                binding.suggestionItemAvatar.setVisibility(View.VISIBLE);
            } else {
                binding.suggestionItemAvatar.setVisibility(View.GONE);
            }

            binding.tvSuggestionItemName.setText(suggestionItem.getName());
            binding.tvSuggestionItemName.setTextColor(suggestionListItemTextColor);
            binding.tvSuggestionItemName.setTextAppearance(suggestionListItemTextAppearance);
            binding.tvMetaInfo.setTextColor(suggestionListItemInfoTextColor);
            binding.tvMetaInfo.setTextAppearance(suggestionListItemInfoTextAppearance);

            if (suggestionItem.getData() != null) {
                if (suggestionItem.getData().has(UIKitConstants.JSONKeys.INFO_TEXT)) {
                    binding.tvMetaInfo.setVisibility(View.VISIBLE);
                    binding.tvMetaInfo.setText(suggestionItem.getData().optString(UIKitConstants.JSONKeys.INFO_TEXT));
                } else {
                    binding.tvMetaInfo.setVisibility(View.GONE);
                }
            } else binding.tvMetaInfo.setVisibility(View.GONE);

            itemView.setTag(R.string.cometchat_tag_item, suggestionItem);
        }

        /**
         * Validates if the provided URL string is a valid URL.
         *
         * @param url the URL string to validate
         * @return true if the URL is valid; false otherwise
         */
        private boolean isValidUrl(String url) {
            return !TextUtils.isEmpty(url) && Patterns.WEB_URL.matcher(url).matches();
        }
    }
}
