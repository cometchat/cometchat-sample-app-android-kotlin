package utils;

import android.content.Context;
import android.view.View;

import androidx.databinding.BindingAdapter;

import com.cometchat.pro.core.UsersRequest;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.uikit.CometChatGroupList;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.User;

import java.util.HashMap;
import java.util.List;

import adapter.GroupListAdapter;
import adapter.UserListAdapter;
import listeners.ClickListener;
import listeners.UserListViewListener;
import listeners.RecyclerTouchListener;

import com.cometchat.pro.uikit.CometChatUserList;


public class CometChatBind {

    private static UsersRequest usersRequest = null;

    private static UserListAdapter userListAdapter;

    private static GroupListAdapter groupListAdapter;

    private static HashMap<String, User> userHashMap = new HashMap<>();

    private static HashMap<String, Group> groupHashMap = new HashMap<>();

    private static UserListViewListener onClickListener;

    private static Context context;

    @BindingAdapter(value = {"android:data_source"})
    public static void setData(CometChatUserList userListView, List<User> users) {

          if (users!=null) {
              for (User user : users) {
                  userHashMap.put(user.getUid(), user);
              }

          }
    }

    @BindingAdapter(value = {"android:data_source","android:context"} )
    public static void setData(CometChatGroupList groupListView, Context c, List<Group> groups) {
           context=c;
        if (groups!=null) {
            for (Group group : groups) {
                groupHashMap.put(group.getGuid(), group);
            }
        }
    }


    @BindingAdapter(value = {"android:itemClickListener"})
    public static void setClickListener(CometChatUserList userListView, Context context) {
        try {

            onClickListener = (UserListViewListener) context;

            userListView.addOnItemTouchListener(new RecyclerTouchListener(context, userListView, new ClickListener() {
                @Override
                public void onClick(View var1, int var2) {
                    User user = (User) var1.getTag(R.string.user);
                    onClickListener.onClick(user, var2, var1);
                }

                @Override
                public void onLongClick(View var1, int var2) {
                    User user = (User) var1.getTag(R.string.user);
                    onClickListener.onLongClick(user, var2, var1);
                }
            }));

        } catch (ClassCastException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
