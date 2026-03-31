package com.cometchat.chatuikit.shared.resources.utils.swipetoreply;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chatuikit.messagelist.MessageAdapter;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.resources.utils.Utils;

public class SwipeController extends ItemTouchHelper.Callback {
    private static final float MAX_SWIPE_DISTANCE = 150f;  // Max swipe distance for visual effect

    private final Context context;
    private final SwipeActions swipeControllerActions;

    private View view;

    private float dx = 0f;

    private boolean swipeBack = false;

    private MessageAdapter messageAdapter;

    public SwipeController(Context context, SwipeActions swipeControllerActions) {
        this.context = context;
        this.swipeControllerActions = swipeControllerActions;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        view = viewHolder.itemView;
        if (viewHolder instanceof MessageAdapter.CenterViewHolder) {
            if (UIKitConstants.MessageCategory.ACTION.equalsIgnoreCase(((MessageAdapter.CenterViewHolder)viewHolder).template.getCategory())
            || (UIKitConstants.MessageCategory.CALL.equalsIgnoreCase(((MessageAdapter.CenterViewHolder)viewHolder).template.getCategory()))) {
                return ItemTouchHelper.Callback.makeMovementFlags(ItemTouchHelper.ACTION_STATE_IDLE, 0);
            }
        }
        if (messageAdapter != null && messageAdapter.getBaseMessageList() != null && !messageAdapter.getBaseMessageList().isEmpty()) {
            int position = viewHolder.getAbsoluteAdapterPosition();
            if (position != RecyclerView.NO_POSITION && position < messageAdapter.getBaseMessageList().size()) {
                BaseMessage baseMessage = messageAdapter.getBaseMessageList().get(position);
                if (baseMessage != null && (baseMessage.getDeletedAt() > 0 || baseMessage.getSentAt() == 0)
                        || baseMessage != null && baseMessage.getId() == 0
                        || (baseMessage instanceof TextMessage && UIKitConstants.ModerationConstants.DISAPPROVED.equals(((TextMessage) baseMessage).getModerationStatus()))) {
                    return ItemTouchHelper.Callback.makeMovementFlags(ItemTouchHelper.ACTION_STATE_IDLE, 0);
                }
            }
        }
        return ItemTouchHelper.Callback.makeMovementFlags(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.RIGHT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (swipeBack) {
            swipeBack = false;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            setTouchListener(recyclerView, viewHolder);
        }

        // Restrict swipe to maximum distance
        float maxSwipeDistanceDp = convertToDp((int) MAX_SWIPE_DISTANCE);
        float restrictedDx = Math.min(dX, maxSwipeDistanceDp);

        if (view.getTranslationX() < convertToDp(130) || restrictedDx < dx) {
            super.onChildDraw(c, recyclerView, viewHolder, restrictedDx, dY, actionState, isCurrentlyActive);
            dx = restrictedDx;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setTouchListener(RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder) {
        recyclerView.setOnTouchListener((v, event) -> {
            swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
            if (swipeBack) {
                if (Math.abs(view.getTranslationX()) >= convertToDp(100)) {
                    int position = viewHolder.getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        swipeControllerActions.onSwipePerformed(position);
                    }
                }
            }
            return false;
        });
    }

    private int convertToDp(int pixels) {
        return Utils.getDP((float) pixels, context);
    }

    public void setAdapter(MessageAdapter messageAdapter) {
        this.messageAdapter = messageAdapter;
    }
}

