package com.cometchat.chatuikit.shared.resources.utils.unread_message_decoration;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chatuikit.shared.resources.utils.sticker_header.StickyHeaderAdapter;

import java.util.HashMap;
import java.util.Map;

public class NewMessageIndicatorDecoration extends RecyclerView.ItemDecoration {

    private final NewMessageIndicatorDecorationAdapter adapter;
    private long unreadMessageId = -1;
    private final Map<Long, RecyclerView.ViewHolder> mHeaderCache;
    private final Map<Long, RecyclerView.ViewHolder> mStickyHeaderCache;

    public NewMessageIndicatorDecoration(NewMessageIndicatorDecorationAdapter adapter) {
        this.adapter = adapter;
        this.mHeaderCache = new HashMap<>();
        this.mStickyHeaderCache = new HashMap<>();
    }

    public void setUnreadMessageId(long messageId) {
        this.unreadMessageId = messageId;
        mHeaderCache.clear();
        mStickyHeaderCache.clear();
    }

    public long getUnreadMessageId() {
        return unreadMessageId;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = getPosition(parent, view);
        if (position == RecyclerView.NO_POSITION) return;

        if (hasHeader(position)) {
            View header = getHeader(parent, position).itemView;
            outRect.top += header.getMeasuredHeight();
        } else {
            outRect.top += 0;
        }
    }

    private int getPosition(RecyclerView parent, View view) {
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) {
            position = parent.getChildLayoutPosition(view);
        }
        return position;
    }

    private boolean hasHeader(int position) {
        BaseMessage message = adapter.getNewMessageIndicatorId(position);
        return message != null && message.getId() == unreadMessageId;
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int position = getPosition(parent, child);

            if (position == RecyclerView.NO_POSITION) continue;

            if (hasHeader(position)) {
                View header = getHeader(parent, position).itemView;
                
                c.save();
                int left = child.getLeft();
                int top = getHeaderTop(parent, child, header, position, i);
                
                c.translate(left, top);

                if (header.isLayoutRequested()) {
                    header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());
                }
                
                header.draw(c);
                c.restore();
            }
        }
    }
    
    private int getHeaderTop(RecyclerView parent, View child, View header, int adapterPos, int layoutPos) {
        int top = (int) child.getY() - header.getHeight();
        if (adapter instanceof StickyHeaderAdapter) {
            StickyHeaderAdapter stickyAdapter = (StickyHeaderAdapter) adapter;
            if (hasStickyHeader(stickyAdapter, adapterPos)) {
                long stickyHeaderId = stickyAdapter.getHeaderId(adapterPos);
                RecyclerView.ViewHolder stickyHolder;
                
                if (mStickyHeaderCache.containsKey(stickyHeaderId)) {
                    stickyHolder = mStickyHeaderCache.get(stickyHeaderId);
                } else {
                    stickyHolder = stickyAdapter.onCreateHeaderViewHolder(parent);
                    View stickyView = stickyHolder.itemView;
                    stickyAdapter.onBindHeaderViewHolder(stickyHolder, adapterPos, stickyHeaderId);
                    
                    int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getMeasuredWidth(), View.MeasureSpec.EXACTLY);
                    int heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getMeasuredHeight(), View.MeasureSpec.UNSPECIFIED);
                    
                    int childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                            parent.getPaddingLeft() + parent.getPaddingRight(), stickyView.getLayoutParams().width);
                    int childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                            parent.getPaddingTop() + parent.getPaddingBottom(), stickyView.getLayoutParams().height);
                    
                    stickyView.measure(childWidth, childHeight);
                    mStickyHeaderCache.put(stickyHeaderId, stickyHolder);
                }
                
                top -= stickyHolder.itemView.getMeasuredHeight();
            }
        }
        return top;
    }

    private boolean hasStickyHeader(StickyHeaderAdapter adapter, int position) {
        if (position == 0) return true;
        return adapter.getHeaderId(position) != -1L && adapter.getHeaderId(position) != adapter.getHeaderId(position - 1);
    }

    private RecyclerView.ViewHolder getHeader(RecyclerView parent, int position) {
        if (mHeaderCache.containsKey(unreadMessageId)) {
            return mHeaderCache.get(unreadMessageId);
        } else {
            RecyclerView.ViewHolder holder = adapter.onCreateNewMessageViewHolder(parent);
            View header = holder.itemView;
            if (header.getLayoutParams() == null) {
                header.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            adapter.onBindNewMessageViewHolder(holder, position, unreadMessageId);

            int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getMeasuredWidth(), View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getMeasuredHeight(), View.MeasureSpec.UNSPECIFIED);

            int childWidth = ViewGroup.getChildMeasureSpec(widthSpec, parent.getPaddingLeft() + parent.getPaddingRight(), header.getLayoutParams().width);
            int childHeight = ViewGroup.getChildMeasureSpec(heightSpec, parent.getPaddingTop() + parent.getPaddingBottom(), header.getLayoutParams().height);

            header.measure(childWidth, childHeight);
            header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());
            mHeaderCache.put(unreadMessageId, holder);
            return holder;
        }
    }
}
