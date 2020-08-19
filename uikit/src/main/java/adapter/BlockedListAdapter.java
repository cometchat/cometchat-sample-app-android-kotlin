package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.uikit.databinding.UserListRowBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import listeners.StickyHeaderAdapter;
import utils.FontUtils;
import utils.Utils;

/**
 * Purpose - BlockListAdapter is a subclass of RecyclerView Adapter which is used to display the list of blocked
 * users. It helps to organize the list data in recyclerview.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 24th January 2020
 *
 */
public class BlockedListAdapter extends RecyclerView.Adapter<BlockedListAdapter.BlockedViewHolder> {

    private  Context context;

    private List<User> userArrayList = new ArrayList<>();

    private FontUtils fontUtils;

    private static final String TAG = "UserListAdapter";

    /**
     * It is a constructor which is used to initialized the adapter whereever we needed.
     *
     * @param context is object of Context.
     * @param userArrayList is a list of blocked users used in this adapter.
     */
    public BlockedListAdapter(Context context, List<User> userArrayList) {
        this.userArrayList = userArrayList;
        this.context= context;
        fontUtils=FontUtils.getInstance(context);
    }

    @NonNull
    @Override
    public BlockedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        UserListRowBinding userListRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.user_list_row, parent, false);

        return new BlockedViewHolder(userListRowBinding);
    }

    /**
     * This method is used to bind the BlockedViewHolder contents with user at given position.
     * It sets name and avatar field of a user with respective BlockedViewHolder content.
     *
     * @param blockedViewHolder is a object of BlockedViewHolder
     * @param i is a position of item in recyclerView
     * @see User
     */
    @Override
    public void onBindViewHolder(@NonNull BlockedViewHolder blockedViewHolder, int i) {
        final User user = userArrayList.get(i);    //Take User which is at ith position in userArrayList
        User user1 = i + 1 < userArrayList.size()? userArrayList.get(i + 1) : null;

        if (user1 != null && user.getName().substring(0, 1).toCharArray()[0] == user1.getName().substring(0, 1).toCharArray()[0]) {
            blockedViewHolder.userListRowBinding.tvSeprator.setVisibility(View.GONE);
        } else {
            blockedViewHolder.userListRowBinding.tvSeprator.setVisibility(View.VISIBLE);
        }
        blockedViewHolder.userListRowBinding.txtUserName.setText(user.getName());
        blockedViewHolder.userListRowBinding.avUser.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        blockedViewHolder.userListRowBinding.getRoot().setTag(R.string.user, user);
        blockedViewHolder.userListRowBinding.txtUserScope.setVisibility(View.GONE);
        blockedViewHolder.userListRowBinding.unblockUser.setVisibility(View.VISIBLE);
        blockedViewHolder.userListRowBinding.txtUserName.setTypeface(fontUtils.getTypeFace(FontUtils.robotoMedium));

        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            blockedViewHolder.userListRowBinding.avUser.setInitials(user.getName());
        } else {
            blockedViewHolder.userListRowBinding.avUser.setAvatar(user.getAvatar());
        }

        if(Utils.isDarkMode(context))
            blockedViewHolder.userListRowBinding.txtUserName.setTextColor(context.getResources().getColor(R.color.textColorWhite));
        else
            blockedViewHolder.userListRowBinding.txtUserName.setTextColor(context.getResources().getColor(R.color.primaryTextColor));
    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }


    /**
     * This method is used to update userList of adapter with new usersList which is passed in parameter.
     * @param users is a list of users which will be updated in adapter.
     */
    public void updateList(List<User> users) {
        for (int i = 0; i < users.size(); i++) {
            if (userArrayList.contains(users.get(i))){
                int index=userArrayList.indexOf(users.get(i));
                userArrayList.remove(index);
                userArrayList.add(index,users.get(i));
            }else {
                userArrayList.add(users.get(i));
            }
        }
        notifyDataSetChanged();
    }


    /**
     * This method is used to remove user from userlist.
     *
     * @param user is an object of User. It is a user which will be removed from list.
     *
     * @see User
     */
    public void removeUser(User user) {
        if (userArrayList.contains(user)) {
            int index=userArrayList.indexOf(user);
            this.userArrayList.remove(user);
            notifyItemRemoved(index);
        }

    }

    class BlockedViewHolder extends RecyclerView.ViewHolder {

        UserListRowBinding userListRowBinding;
        BlockedViewHolder(UserListRowBinding userListRowBinding) {
            super(userListRowBinding.getRoot());
            this.userListRowBinding = userListRowBinding;

        }

    }
}
