package com.cometchat.pro.uikit.ui_resources.utils.recycler_touch

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.RecyclerViewSwipeListener.UnderlayButton
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View.OnTouchListener
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.RecyclerViewSwipeListener
import kotlin.jvm.Synchronized
import android.view.GestureDetector
import android.view.View
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.RecyclerViewSwipeListener.UnderlayButtonClickListener
import java.util.*

abstract class RecyclerViewSwipeListener(context: Context?) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
    private var recyclerView: RecyclerView? = null
    private var buttons: MutableList<UnderlayButton> = ArrayList<UnderlayButton>();
    private var gestureDetector: GestureDetector
    private var swipedPos = -1
    private var swipeThreshold = 0.5f
    private val buttonsBuffer: MutableMap<Int, MutableList<UnderlayButton>>
    private var recoverQueue: Queue<Int>
    private val gestureListener: SimpleOnGestureListener = object : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            for (button in buttons) {
                if (button.onClick(e.x, e.y)) break
            }
            return true
        }
    }
    init {
        buttons = ArrayList()
        gestureDetector = GestureDetector(context, gestureListener)
        buttonsBuffer = HashMap()
        recoverQueue = object : LinkedList<Int>() {
            override fun add(o: Int): Boolean {
                return if (contains(o)) false else super.add(o)
            }
        }
    }


    private val onTouchListener = OnTouchListener { view, e ->
        if (swipedPos < 0) return@OnTouchListener false
        val point = Point(
            e.rawX.toInt(), e.rawY.toInt()
        )
        val swipedViewHolder = recyclerView!!.findViewHolderForAdapterPosition(swipedPos)
        val swipedItem = swipedViewHolder!!.itemView
        val rect = Rect()
        swipedItem.getGlobalVisibleRect(rect)
        if (e.action == MotionEvent.ACTION_DOWN || e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_MOVE) {
            if (rect.top < point.y && rect.bottom > point.y)
                    gestureDetector.onTouchEvent(e) else {
                recoverQueue.add(swipedPos)
                swipedPos = -1
                recoverSwipedItem()
            }
        }
        false
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val pos = viewHolder.adapterPosition
        if (swipedPos != pos) recoverQueue.add(swipedPos)
        swipedPos = pos
        if (buttonsBuffer.containsKey(swipedPos)) buttons =
            buttonsBuffer[swipedPos]!! else buttons.clear()
        buttonsBuffer.clear()
        swipeThreshold = 0.5f * buttons.size * BUTTON_WIDTH
        recoverSwipedItem()
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return swipeThreshold
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return 0.1f * defaultValue
    }

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return 5.0f * defaultValue
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val pos = viewHolder.adapterPosition
        var translationX = dX
        val itemView = viewHolder.itemView
        if (pos < 0) {
            swipedPos = pos
            return
        }
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX < 0) {
                var buffer: MutableList<UnderlayButton> = ArrayList()
                if (!buttonsBuffer.containsKey(pos)) {
                    instantiateUnderlayButton(viewHolder, buffer)
                    buttonsBuffer[pos] = buffer
                } else {
                    buffer = buttonsBuffer[pos]!!
                }
                translationX = dX * buffer.size * BUTTON_WIDTH / itemView.width
                drawButtons(c, itemView, buffer, pos, translationX)
            }
        }
        super.onChildDraw(
            c,
            recyclerView,
            viewHolder,
            translationX,
            dY,
            actionState,
            isCurrentlyActive
        )
    }

    @Synchronized
    private fun recoverSwipedItem() {
        while (!recoverQueue.isEmpty()) {
            val pos = recoverQueue.poll()
            if (pos > -1) {
                recyclerView!!.adapter!!.notifyItemChanged(pos)
            }
        }
    }

    private fun drawButtons(
        c: Canvas,
        itemView: View,
        buffer: List<UnderlayButton>,
        pos: Int,
        dX: Float
    ) {
        var right = itemView.right.toFloat()
        val dButtonWidth = -1 * dX / buffer.size
        for (button in buffer) {
            val left = right - dButtonWidth
            button.onDraw(
                c,
                RectF(
                    left,
                    itemView.top.toFloat(),
                    right,
                    itemView.bottom.toFloat()
                ),
                pos
            )
            right = left
        }
    }

    fun attachToRecyclerView(recyclerView: RecyclerView?) {
        this.recyclerView = recyclerView
        this.recyclerView!!.setOnTouchListener(onTouchListener)
        val itemTouchHelper = ItemTouchHelper(this)
        itemTouchHelper.attachToRecyclerView(this.recyclerView)
    }

    abstract fun instantiateUnderlayButton(
        viewHolder: RecyclerView.ViewHolder?,
        underlayButtons: MutableList<UnderlayButton>?
    )

    interface UnderlayButtonClickListener {
        fun onClick(pos: Int)
    }

    class UnderlayButton(
        private val text: String,
        private val imageBitmap: Bitmap?,
        private val color: Int,
        private val clickListener: UnderlayButtonClickListener
    ) {
        private var pos = 0
        private var clickRegion: RectF? = null
        fun onClick(x: Float, y: Float): Boolean {
            if (clickRegion != null && clickRegion!!.contains(x, y)) {
                clickListener.onClick(pos)
                return true
            }
            return false
        }

        fun onDraw(c: Canvas, rect: RectF, pos: Int) {
            val p = Paint()

            // Draw background
            p.color = color
            c.drawRect(rect, p)

            // Draw Text
            p.color = Color.WHITE
            //p.setTextSize(LayoutHelper.getPx(MyApplication.getAppContext(), 12));
            p.textSize = Resources.getSystem().displayMetrics.density * 12
            val r = Rect()
            val cHeight = rect.height()
            val cWidth = rect.width()
            p.textAlign = Paint.Align.LEFT
            p.getTextBounds(text, 0, text.length, r)
            val x = cWidth / 2f - r.width() / 2f - r.left
            val y = cHeight / 2f + r.height() / 2f - r.bottom
            if (text.isNotEmpty()) c.drawText(text, rect.left + x, rect.top + y / 0.72f, p)
            if (imageBitmap != null) {
                c.drawBitmap(imageBitmap, rect.left + x /1.2f , rect.top + y / 1.5f, p)
            }
            clickRegion = rect
            this.pos = pos
        }
    }

    companion object {
        const val BUTTON_WIDTH = 200
    }


}