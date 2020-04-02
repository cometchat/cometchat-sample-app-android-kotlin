package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.pro.uikit.R;
import com.cometchat.pro.uikit.databinding.GroupListRowBinding;
import com.cometchat.pro.models.Group;

import java.util.ArrayList;
import java.util.List;

import utils.FontUtils;

/**
 * Purpose - GroupListAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of groups. It helps to organize the list data in recyclerView.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 23rd March 2020
 *
 */


public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.GroupViewHolder> {

    private Context context;

    private List<Group> groupList = new ArrayList<>();

    private FontUtils fontUtils;

    /**
     * It is a constructor which is used to initialize wherever we needed.
     *
     * @param context is a object of Context.
     */
    public GroupListAdapter(Context context) {
        this.context = context;
        fontUtils=FontUtils.getInstance(context);
    }

    /**
     * It is constructor which takes groupsList as parameter and bind it with groupList in adapter.
     *
     * @param context is a object of Context.
     * @param groupList is a list of groups used in this adapter.
     */
    public GroupListAdapter(Context context, List<Group> groupList) {

        this.groupList = groupList;
        this.context = context;
        fontUtils=FontUtils.getInstance(context);
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        GroupListRowBinding groupListRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.group_list_row, parent, false);
        return new GroupViewHolder(groupListRowBinding);
    }

    /**
     * This method is used to bind the GroupViewHolder contents with group at given
     * position. It set group icon, group name in a respective GroupViewHolder content.
     *
     * @param groupViewHolder is a object of GroupViewHolder.
     * @param position is a position of item in recyclerView.
     * @see Group
     */
    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder groupViewHolder, int position) {
        Group group = groupList.get(position);
        groupViewHolder.groupListRowBinding.setGroup(group);
        groupViewHolder.groupListRowBinding.executePendingBindings();
        groupViewHolder.groupListRowBinding.avGroup.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        groupViewHolder.groupListRowBinding.getRoot().setTag(R.string.group, group);
        groupViewHolder.groupListRowBinding.txtUserMessage.setTypeface(fontUtils.getTypeFace(FontUtils.robotoRegular));
        groupViewHolder.groupListRowBinding.txtUserName.setTypeface(fontUtils.getTypeFace(FontUtils.robotoMedium));

    }

    /**
     * This method is used to update groupList in adapter.
     * @param groupList is a list of groups which will be updated in adapter.
     */
    public void updateGroupList(List<Group> groupList) {

        for (int i = 0; i <groupList.size() ; i++) {
            if (!this.groupList.contains(groupList.get(i))) {
                this.groupList.add(groupList.get(i));
            }
        }
        notifyDataSetChanged();
    }

    /**
     * This method is used to update a particular group in groupList of adapter.
     *
     * @param group is an object of Group. It will be updated with previous group in a list.
     */
    public void updateGroup(Group group) {
        if (group != null) {
            if (groupList.contains(group)) {
                int index = groupList.indexOf(group);
                groupList.remove(index);
                groupList.add(group);
                notifyItemChanged(index);
            } else {
                groupList.add(group);
                notifyItemInserted(getItemCount() - 1);
            }
        }
    }

    /**
     * This method is used to remove particular group from groupList in adapter.
     *
     * @param group is a object of Group which will be removed from groupList.
     *
     * @see Group
     */
    public void removeGroup(Group group) {
        if (group != null) {
            if (groupList.contains(group)) {
                int index = groupList.indexOf(group);
                groupList.remove(group);
                notifyItemRemoved(index);
            }
        }
    }

    @Override
    public int getItemCount() {
        return groupList.size();

    }

    /**
     * This method is used to set searchGroupList with a groupList in adapter.
     *
     * @param groups is a list of group which will be set with a groupList in adapter.
     */
    public void searchGroup(List<Group> groups) {
        if (groups != null) {
            groupList = groups;
            notifyDataSetChanged();
        }

    }

    /**
     * This method is used to add particular group in groupList of adapter.
     *
     * @param group is a object of group which will be added in groupList.
     *
     * @see Group
     *
     */
    public void add(Group group) {
        if (group != null) {
            updateGroup(group);
        }
    }

    public void clear() {
        groupList.clear();
        notifyDataSetChanged();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {

        GroupListRowBinding groupListRowBinding;

        GroupViewHolder(@NonNull GroupListRowBinding groupListRowBinding) {
            super(groupListRowBinding.getRoot());
            this.groupListRowBinding = groupListRowBinding;
        }
    }
}
