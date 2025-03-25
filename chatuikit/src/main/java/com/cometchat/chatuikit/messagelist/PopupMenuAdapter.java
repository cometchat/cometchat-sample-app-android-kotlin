package com.cometchat.chatuikit.messagelist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chatuikit.R;

import java.util.List;

class PopupMenuAdapter extends RecyclerView.Adapter<PopupMenuAdapter.StringViewHolder> {
    private static final String TAG = PopupMenuAdapter.class.getSimpleName();
    private final List<CometChatMessagePopupMenu.MenuItem> items;
    private final Context context;
    private final OnMenuItemClickListener onMenuItemClickListener;
    private @ColorInt int textColor;
    private @StyleRes int textAppearance;
    private @ColorInt int startIconColor;
    private @ColorInt int endIconColor;

    public PopupMenuAdapter(Context context, List<CometChatMessagePopupMenu.MenuItem> items, OnMenuItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.onMenuItemClickListener = listener;
    }

    @NonNull
    @Override
    public StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cometchat_popup_menu_row, parent, false);
        return new StringViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StringViewHolder holder, int position) {
        CometChatMessagePopupMenu.MenuItem item = items.get(position);
        holder.bindView(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
    }

    public void setTextAppearance(@StyleRes int textAppearance) {
        this.textAppearance = textAppearance;
    }

    public void setStartIconTint(@ColorInt int startIconColor) {
        this.startIconColor = startIconColor;
    }

    public void setEndIconTint(@ColorInt int endIconColor) {
        this.endIconColor = endIconColor;
    }

    public interface OnMenuItemClickListener {
        void onItemClick(String id, String item);
    }

    class StringViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewItem;
        private final ImageView startIv;
        private final ImageView endIv;

        StringViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewItem = itemView.findViewById(R.id.menu_item);
            startIv = itemView.findViewById(R.id.start_icon);
            endIv = itemView.findViewById(R.id.end_icon);
        }

        public void bindView(CometChatMessagePopupMenu.MenuItem item) {
            textViewItem.setText(item.getName());
            itemView.setOnClickListener(v -> {
                if (onMenuItemClickListener != null)
                    onMenuItemClickListener.onItemClick(item.getId(), item.getName());
            });
            if (item.getStartIcon() != null) {
                startIv.setVisibility(View.VISIBLE);
                startIv.setImageDrawable(item.getStartIcon());
            } else {
                startIv.setVisibility(View.GONE);
            }
            if (item.getEndIcon() != null) {
                endIv.setVisibility(View.VISIBLE);
                endIv.setImageDrawable(item.getEndIcon());
            } else {
                endIv.setVisibility(View.GONE);
            }

            if (item.getTextColor() == 0) {
                textViewItem.setTextColor(textColor);
            } else {
                textViewItem.setTextColor(item.getTextColor());
            }

            if (item.getTextAppearance() == 0) {
                textViewItem.setTextAppearance(textAppearance);
            } else {
                textViewItem.setTextAppearance(item.getTextAppearance());
            }

            if (item.getStartIconTint() == 0) {
                startIv.setColorFilter(startIconColor);
            } else {
                startIv.setColorFilter(item.getStartIconTint());
            }

            if (item.getEndIconTint() == 0) {
                endIv.setColorFilter(endIconColor);
            } else {
                endIv.setColorFilter(item.getEndIconTint());
            }
        }
    }
}
