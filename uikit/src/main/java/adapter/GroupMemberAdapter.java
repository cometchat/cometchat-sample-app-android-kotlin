package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.models.GroupMember;
import com.cometchat.pro.models.TypingIndicator;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.uikit.databinding.UserListRowBinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import utils.FontUtils;
import utils.Utils;

/**
 * Purpose - GroupMemberAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of group members. It helps to organize the list data in recyclerView.
 * It also help to perform search operation on list of groups members.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 24th January 2020
 *
 */


public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.GroupMemberViewHolder> {

    private Context context;

    private String groupOwnerId;

    private static final String TAG = GroupMemberAdapter.class.getSimpleName();

    private List<GroupMember> groupMemberList = new ArrayList<>();

    private FontUtils fontUtils;

    /**
     * It is a constructor which is used to initialize wherever we needed.
     *
     * @param context is a object of Context.
     */
    public GroupMemberAdapter(Context context) {
        this.context = context;
        fontUtils=FontUtils.getInstance(context);
    }

    /**
     * It is constructor which takes groupMemberList as parameter and bind it with groupMemberList in adapter.
     *
     * @param context is a object of Context.
     * @param groupMemberList is a list of group member used in this adapter.
     */
    public GroupMemberAdapter(Context context, List<GroupMember> groupMemberList, String groupOwnerId) {
        this.groupMemberList = groupMemberList;
        this.groupOwnerId = groupOwnerId;
        this.context = context;
        fontUtils=FontUtils.getInstance(context);

    }

    @NonNull
    @Override
    public GroupMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        UserListRowBinding userListRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.user_list_row, parent, false);

        return new GroupMemberViewHolder(userListRowBinding);
    }

    /**
     *  This method is used to bind the GroupMemberViewHolder contents with groupMember at given
     *  position. It set name icon, scope with respective GroupMemberViewHolder content.
     *
     * @param groupMemberViewHolder is a object of GroupMemberViewHolder.
     * @param i is a position of item in recyclerView.
     * @see GroupMember
     */
    @Override
    public void onBindViewHolder(@NonNull GroupMemberViewHolder groupMemberViewHolder, int i) {

        GroupMember groupMember = groupMemberList.get(i);

        groupMemberViewHolder.userListRowBinding.avUser.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        groupMemberViewHolder.userListRowBinding.getRoot().setTag(R.string.user, groupMember);


        if (groupMember.getUid().equals(CometChat.getLoggedInUser().getUid())) {
            groupMemberViewHolder.userListRowBinding.txtUserName.setText(R.string.you);
        } else
            groupMemberViewHolder.userListRowBinding.txtUserName.setText(groupMember.getName());

        if (groupOwnerId != null && groupMember.getUid().equals(groupOwnerId) &&
                groupMember.getScope().equals(CometChatConstants.SCOPE_ADMIN)) {
            groupMemberViewHolder.userListRowBinding.txtUserScope.setText(R.string.owner);
        } else if (groupMember.getScope().equals(CometChatConstants.SCOPE_ADMIN)) {
            groupMemberViewHolder.userListRowBinding.txtUserScope.setText(R.string.admin);
        } else if (groupMember.getScope().equals(CometChatConstants.SCOPE_MODERATOR)){
            groupMemberViewHolder.userListRowBinding.txtUserScope.setText(R.string.moderator);
        } else {
            groupMemberViewHolder.userListRowBinding.txtUserScope.setText("");
        }

        groupMemberViewHolder.userListRowBinding.txtUserName.setTypeface(fontUtils.getTypeFace(FontUtils.robotoMedium));
        if (groupMember.getAvatar() == null || groupMember.getAvatar().isEmpty())
            groupMemberViewHolder.userListRowBinding.avUser.setInitials(groupMember.getName());
        else
            groupMemberViewHolder.userListRowBinding.avUser.setAvatar(groupMember.getAvatar());

        if (Utils.isDarkMode(context))
        {
            groupMemberViewHolder.userListRowBinding.txtUserName.setTextColor(context.getResources().getColor(R.color.textColorWhite));
            groupMemberViewHolder.userListRowBinding.tvSeprator.setBackgroundColor(context.getResources().getColor(R.color.grey));
            groupMemberViewHolder.userListRowBinding.txtUserScope.setTextColor(context.getResources().getColor(R.color.textColorWhite));
        } else {
            groupMemberViewHolder.userListRowBinding.txtUserName.setTextColor(context.getResources().getColor(R.color.primaryTextColor));
            groupMemberViewHolder.userListRowBinding.tvSeprator.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
            groupMemberViewHolder.userListRowBinding.txtUserScope.setTextColor(context.getResources().getColor(R.color.secondaryTextColor));
        }

        if (i == getItemCount() - 1)
            groupMemberViewHolder.userListRowBinding.tvSeprator.setVisibility(View.GONE);


    }

    @Override
    public int getItemCount() {
        return groupMemberList.size();

    }

    /**
     * This method is used to add group members in groupMemberList of adapter
     *
     * @param groupMembers is a list of group members which will be added in adapter.
     * @see GroupMember
     */
    public void addAll(List<GroupMember> groupMembers) {

        for (GroupMember groupMember : groupMembers) {
            if (!groupMemberList.contains(groupMember)) {
                groupMemberList.add(groupMember);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * This method is used to set search list in a groupMemberList of adapter.
     *
     * @param filterlist is a list of searched group members.
     */
    public void searchGroupMembers(List<GroupMember> filterlist) {
        this.groupMemberList = filterlist;
        notifyDataSetChanged();
    }

    /**
     * This method is used to add group member in a groupMemberList of adapter.
     *
     * @param joinedUser is object of GroupMember which will be added in a groupList.
     * @see GroupMember
     */
    public void addGroupMember(GroupMember joinedUser) {
        groupMemberList.add(joinedUser);
        notifyDataSetChanged();
    }

    /**
     * This method is used to remove group member from groupMemberList of adapter.
     *
     * @param groupMember is a object of GroupMember which will be removed from groupList.
     * @see GroupMember
     */
    public void removeGroupMember(GroupMember groupMember) {
         if (groupMemberList.contains(groupMember)) {
             groupMemberList.remove(groupMember);
             notifyDataSetChanged();
         }
    }

    /**
     * This method is used to update group member from a groupMemberList of a adapter.
     *
     * @param groupMember is a object of GroupMember which will updated with old group member in
     *                    groupMemberList.
     * @see GroupMember
     */
    public void updateMember(GroupMember groupMember) {
        if (groupMemberList.contains(groupMember)) {
            int index = groupMemberList.indexOf(groupMember);
            groupMemberList.remove(groupMember);
            groupMemberList.add(index, groupMember);
            notifyItemChanged(index);
        }
    }

    public void resetAdapter() {
        groupMemberList.clear();
        notifyDataSetChanged();
    }

    class GroupMemberViewHolder extends RecyclerView.ViewHolder {

        UserListRowBinding userListRowBinding;

        GroupMemberViewHolder(UserListRowBinding userListRowBinding) {
            super(userListRowBinding.getRoot());
            this.userListRowBinding = userListRowBinding;

        }
    }

    /**
     * This method is used to update group members in a groupMemberList of a adapter.
     *
     * @param groupMembers is a list of updated group members.
     */
    public void updateGroupMembers(List<GroupMember> groupMembers) {
        for (int i = 0; i < groupMembers.size(); i++) {
            if (groupMemberList.contains(groupMembers.get(i))) {
                int index = groupMemberList.indexOf(groupMembers.get(i));
                groupMemberList.remove(index);
                groupMemberList.add(index, groupMembers.get(i));
            } else {
                groupMemberList.add(groupMembers.get(i));
            }
        }
        notifyDataSetChanged();
    }
}
