package viewmodel;

import android.content.Context;

import com.cometchat.pro.uikit.CometChatUserList;
import com.cometchat.pro.models.User;

import java.util.List;

import adapter.UserListAdapter;
import listeners.StickyHeaderDecoration;

public class UserListViewModel {

    private static final String TAG = "UserListViewModel";

    private  Context context;

    private UserListAdapter userListAdapter;

    private CometChatUserList userListView;



    public UserListViewModel(Context context,CometChatUserList cometChatUserList,boolean showHeader){
        this.userListView=cometChatUserList;
        this.context=context;
        setUserListAdapter(cometChatUserList,showHeader);
    }

    private UserListViewModel(){

    }

    private UserListAdapter getAdapter() {
        if (userListAdapter==null){
            userListAdapter=new UserListAdapter(context);
        }
        return userListAdapter;
    }

    public void add(User user){
        if (userListAdapter!=null)
            userListAdapter.add(user);

    }
    public void add(int index,User user){
        if (userListAdapter!=null)
            userListAdapter.add(index,user);

    }

    public void update(User user){
        if (userListAdapter!=null)
            userListAdapter.updateUser(user);

    }

    public void remove(User user){
        if (userListAdapter!=null)
            userListAdapter.removeUser(user);

    }
    public void remove(int index){
        if (userListAdapter!=null)
            userListAdapter.removeUser(index);
    }

    public void clear()
    {
        if (userListAdapter!=null)
            userListAdapter.clear();
    }
    private void setUserListAdapter(CometChatUserList cometChatUserList,boolean showHeader){
        userListAdapter=new UserListAdapter(context);
        if(showHeader) {
            StickyHeaderDecoration stickyHeaderDecoration = new StickyHeaderDecoration(userListAdapter);
            cometChatUserList.addItemDecoration(stickyHeaderDecoration, 0);
        }
        cometChatUserList.setAdapter(userListAdapter);
    }

    public void setUsersList(List<User> usersList){
          getAdapter().updateList(usersList);
    }

    public void update(int index, User user) {
        if (userListAdapter!=null)
            userListAdapter.updateUser(index,user);
    }

    public void searchUserList(List<User> userList) {
        if (userListAdapter!=null)
            userListAdapter.searchUser(userList);
    }
}

