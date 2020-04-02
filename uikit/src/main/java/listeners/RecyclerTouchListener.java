package listeners;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
    private ClickListener clickListener;
    private GestureDetector gestureDetector;

    public RecyclerTouchListener(Context var1, final RecyclerView var2, final ClickListener var3) {
        this.clickListener = var3;
        this.gestureDetector = new GestureDetector(var1, new GestureDetector.SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent var1) {
                return true;
            }

            public void onLongPress(MotionEvent var1) {
                View var2x = var2.findChildViewUnder(var1.getX(), var1.getY());
                if (var2x != null && var3 != null) {
                    var3.onLongClick(var2x, var2.getChildPosition(var2x));
                }

            }
        });
    }

    public boolean onInterceptTouchEvent(RecyclerView var1, MotionEvent var2) {
        View var3 = var1.findChildViewUnder(var2.getX(), var2.getY());
        if (var3 != null && this.clickListener != null && this.gestureDetector.onTouchEvent(var2)) {
            this.clickListener.onClick(var3, var1.getChildPosition(var3));
        }

        return false;
    }

    public void onTouchEvent(RecyclerView var1, MotionEvent var2) {
    }

    public void onRequestDisallowInterceptTouchEvent(boolean var1) {
    }



}
