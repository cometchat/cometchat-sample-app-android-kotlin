package com.cometchat.chatuikit.shared.views.aiconversationsummary.view;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatAiConversationSummaryRowBinding;

import java.util.ArrayList;
import java.util.List;

public class AIConversationSummaryAdapter extends RecyclerView.Adapter<AIConversationSummaryAdapter.ViewHolder> {
    private static final String TAG = AIConversationSummaryAdapter.class.getSimpleName();
    private List<String> list;
    private Drawable itemBackgroundDrawable;
    private @ColorInt int itemBackgroundColor;
    private @ColorInt int itemStrokeColor;
    private @Dimension int itemStrokeWidth;
    private @Dimension int itemCornerRadius;
    private @ColorInt int itemTextColor;
    private @StyleRes int itemTextAppearance;

    public AIConversationSummaryAdapter() {
        list = new ArrayList<>();
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to
     * represent an item.
     *
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent
     * the items of the given type. You can either create a new View manually or
     * inflate it from an XML layout file.
     *
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(ViewHolder, int, List)}. Since it will be re-used to
     * display different items in the data set, it is a good idea to cache
     * references to sub views of the View to avoid unnecessary
     * {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is
     *                 bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(ViewHolder, int)
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CometchatAiConversationSummaryRowBinding binding = CometchatAiConversationSummaryRowBinding.inflate(LayoutInflater.from(parent.getContext()),
                                                                                                            parent,
                                                                                                            false
        );
        return new ViewHolder(binding.getRoot());
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This
     * method should update the contents of the {@link ViewHolder#itemView} to
     * reflect the item at the given position.
     *
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item
     * itself is invalidated or the new position cannot be determined. For this
     * reason, you should only use the <code>position</code> parameter while
     * acquiring the related data item inside this method and should not keep a copy
     * of it. If you need the position of an item later on (e.g. in a click
     * listener), use {@link ViewHolder#getBindingAdapterPosition()} which will have
     * the updated adapter position.
     *
     * <p>
     * Override {@link #onBindViewHolder(ViewHolder, int, List)} instead if Adapter
     * can handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents
     *                 of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindView(list.get(position));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setList(List<String> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void setItemBackgroundColor(@ColorInt int itemBackgroundColor) {
        this.itemBackgroundColor = itemBackgroundColor;
    }

    public void setItemStrokeColor(@ColorInt int itemStrokeColor) {
        this.itemStrokeColor = itemStrokeColor;
    }

    public void setItemStrokeWidth(@Dimension int itemStrokeWidth) {
        this.itemStrokeWidth = itemStrokeWidth;
    }

    public void setItemCornerRadius(@Dimension int itemCornerRadius) {
        this.itemCornerRadius = itemCornerRadius;
    }

    public void setItemTextColor(@ColorInt int itemTextColor) {
        this.itemTextColor = itemTextColor;
    }

    public void setItemTextAppearance(@StyleRes int itemTextAppearance) {
        this.itemTextAppearance = itemTextAppearance;
    }

    public void setItemBackgroundDrawable(Drawable itemBackgroundDrawable) {
        this.itemBackgroundDrawable = itemBackgroundDrawable;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final CometchatAiConversationSummaryRowBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CometchatAiConversationSummaryRowBinding.bind(itemView);
        }

        public void bindView(String reply) {
            binding.tvMessage.setText(reply);
            binding.messageCard.setCardBackgroundColor(itemBackgroundColor);
            binding.messageCard.setStrokeColor(itemStrokeColor);
            binding.messageCard.setStrokeWidth(itemStrokeWidth);
            binding.messageCard.setRadius(itemCornerRadius);
            binding.tvMessage.setTextColor(itemTextColor);
            binding.tvMessage.setTextAppearance(itemTextAppearance);
            if (itemBackgroundDrawable != null) binding.messageCard.setBackgroundDrawable(itemBackgroundDrawable);
            itemView.setTag(R.string.cometchat_reply_lowercase, reply);
        }
    }
}
