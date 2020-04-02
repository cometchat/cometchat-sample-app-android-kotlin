package utils;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.Avatar;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.uikit.SmartReplyList;

import java.util.ArrayList;
import java.util.List;

import adapter.SmartReplyListAdapter;
import adapter.UserListAdapter;

public class FooterDecoration extends RecyclerView.ItemDecoration {

    private UserListAdapter adapter;
    private View mLayout;
    private int viewResID;
    private Activity activity;
    private List<User> replylist = new ArrayList<>();
    private final String TAG = FooterDecoration.class.getSimpleName();

    public FooterDecoration(final Activity activity, RecyclerView parent, @LayoutRes int resId) {
        // inflate and measure the layout
        mLayout = LayoutInflater.from(activity).inflate(resId, parent, false);
        mLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//        this.viewResID = imageViewResID;
        this.adapter = adapter;
        this.activity = activity;
    }


    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        // layout basically just gets drawn on the reserved space on top of the first view
        mLayout.layout(parent.getLeft(), 0, parent.getRight(), mLayout.getMeasuredHeight());
        for (int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
                c.save();
                c.translate(0, view.getBottom());
                mLayout.draw(c);
                c.restore();
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

                break;
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
            outRect.set(0, 0, 0, mLayout.getMeasuredHeight());
        } else {
            outRect.setEmpty();
        }
    }
}
