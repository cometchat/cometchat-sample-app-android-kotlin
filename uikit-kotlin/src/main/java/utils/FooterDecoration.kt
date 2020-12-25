package utils

import adapter.UserListAdapter
import android.app.Activity
import android.graphics.Canvas
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.cometchat.pro.models.User
import java.util.*

class FooterDecoration(activity: Activity, parent: RecyclerView?, @LayoutRes resId: Int) : ItemDecoration() {
//    private val adapter: UserListAdapter
    private val mLayout: View
    private val viewResID = 0
    private val activity: Activity
    private val replylist: List<User> = ArrayList()
    private val TAG = FooterDecoration::class.java.simpleName
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        // layout basically just gets drawn on the reserved space on top of the first view
        mLayout.layout(parent.left, 0, parent.right, mLayout.measuredHeight)
        for (i in 0 until parent.childCount) {
            val view = parent.getChildAt(i)
            if (parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount - 1) {
                c.save()
                c.translate(0f, view.bottom.toFloat())
                mLayout.draw(c)
                c.restore()
                //                Avatar avatar = mLayout.findViewById(R.id.uservw);
//                avatar.setAvatar(CometChat.getLoggedInUser());

//                final RecyclerView userList = mLayout.findViewById(viewResID);
//                userList.setAdapter(adapter);
//                for(int u=0;u<5;u++) {
//                    User user = new User();
//                    user.setUid("user"+u);
//                    user.setName("testuser"+u);
//                    adapter.add(user);
//                }
//                userList.setBackgroundColor(Color.RED);
//                ((RelativeLayout)(userList.getParent())).setBackgroundColor(Color.RED);
                break
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount - 1) {
            outRect[0, 0, 0] = mLayout.measuredHeight
        } else {
            outRect.setEmpty()
        }
    }

    init {
        // inflate and measure the layout
        mLayout = LayoutInflater.from(activity).inflate(resId, parent, false)
        mLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        //        this.viewResID = imageViewResID;
//        adapter = adapter
        this.activity = activity
    }
}