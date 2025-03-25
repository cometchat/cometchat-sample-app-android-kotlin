package com.cometchat.chatuikit.shared.viewholders;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.calls.model.CallLog;
import com.cometchat.chatuikit.databinding.CometchatCallLogsItemsBinding;
import com.cometchat.chatuikit.shared.interfaces.ViewHolderCallBack;

import java.util.List;

public abstract class CallLogsViewHolderListener implements ViewHolderCallBack {
    private static final String TAG = CallLogsViewHolderListener.class.getSimpleName();

    public View createView(Context context, CometchatCallLogsItemsBinding listItem) {
        return null;
    }

    public void bindView(Context context, View createdView, CallLog call, RecyclerView.ViewHolder holder, List<CallLog> callList, int position) {
    }
}
