package com.cometchat.chatuikit.shared.resources.utils.unread_message_decoration;

import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.models.BaseMessage;

public interface NewMessageIndicatorDecorationAdapter<T extends RecyclerView.ViewHolder> {

    BaseMessage getNewMessageIndicatorId(int position);

    T onCreateNewMessageViewHolder(ViewGroup var1);

    void onBindNewMessageViewHolder(T var1, int position, long messageId);
}
