package listeners

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener

class RecyclerTouchListener(var1: Context?, val var2: RecyclerView, private val clickListener: ClickListener) : OnItemTouchListener {
    private val gestureDetector: GestureDetector
    override fun onInterceptTouchEvent(var1: RecyclerView, var2: MotionEvent): Boolean {
        val var3 = var1.findChildViewUnder(var2.x, var2.y)
        if (var3 != null && clickListener != null && gestureDetector.onTouchEvent(var2)) {
            clickListener.onClick(var3, var1.getChildPosition(var3))
        }
        return false
    }

    override fun onTouchEvent(var1: RecyclerView, var2: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(var1: Boolean) {}

    init {
        gestureDetector = GestureDetector(var1, object : SimpleOnGestureListener() {
            override fun onSingleTapUp(var1: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(var1: MotionEvent) {
                val var2x = var2.findChildViewUnder(var1.x, var1.y)
                if (var2x != null && clickListener != null) {
                    clickListener.onLongClick(var2x, var2.getChildPosition(var2x))
                }
            }
        })
    }
}