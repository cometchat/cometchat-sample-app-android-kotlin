package com.cometchat.chatuikit.shared.viewholders;

import android.content.Context;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chatuikit.shared.interfaces.ViewHolderCallBack;

import java.util.List;

public abstract class MessagesSearchViewHolderListener<T extends BaseMessage> implements ViewHolderCallBack {

    public abstract View createView(Context context,
                                    View listItem);

    public abstract void bindView(Context context,
                                  View createdView,
                                  T message,
                                  RecyclerView.ViewHolder holder,
                                  List<BaseMessage> messagesList,
                                  int position);
}
