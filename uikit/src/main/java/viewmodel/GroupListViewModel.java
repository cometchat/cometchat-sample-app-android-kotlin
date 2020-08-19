package viewmodel;

import android.content.Context;

import com.cometchat.pro.core.GroupsRequest;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.CometChatGroupList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import adapter.GroupListAdapter;

public class GroupListViewModel {

    private  Context context;

    private GroupsRequest groupsRequest;

    private GroupListAdapter groupListAdapter;

    private List<User> groupList = new ArrayList<>();

    private HashMap<String, Group> groupHashMap = new HashMap<>();

    private CometChatGroupList groupListView;


    private static final String TAG = "GroupListViewModel";

    private GroupListViewModel (){

    }

    public GroupListViewModel(Context context,CometChatGroupList cometChatGroupList){
        this.groupListView=cometChatGroupList;
        this.context=context;
        setGroupListAdapter(cometChatGroupList);
    }

    private GroupListAdapter getAdapter(){
        if (groupListAdapter==null){
            groupListAdapter=new GroupListAdapter(context);
        }
        return groupListAdapter;
    }

    private void setGroupListAdapter(CometChatGroupList cometChatGroupList){
        groupListAdapter=new GroupListAdapter(context);
        cometChatGroupList.setAdapter(groupListAdapter);
    }

    public void setGroupList(List<Group> groupList){
         if (groupListAdapter!=null) {
              if (groupList!=null&&groupList.size()!=0)
                groupListAdapter.updateGroupList(groupList);
         }
    }


    public void remove(Group group){
         if (groupListAdapter!=null)
        groupListAdapter.removeGroup(group);
    }


    public void update(Group group,int i) {
        if (groupListAdapter!=null)
            groupListAdapter.updateGroup(group,i);
    }

    public void add(Group group) {
        if (groupListAdapter!=null)
            groupListAdapter.add(group);
    }

    public void searchGroupList(List<Group> groups) {
        if (groupListAdapter!=null)
            groupListAdapter.searchGroup(groups);
    }

    public void clear() {
        if (groupListAdapter!=null)
            groupListAdapter.clear();
    }
}
