package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.pro.uikit.R;
import com.cometchat.pro.uikit.databinding.UserListRowBinding;
import com.cometchat.pro.models.User;

import java.util.ArrayList;
import java.util.List;

import listeners.StickyHeaderAdapter;
import utils.FontUtils;
import utils.Utils;

/**
 * Purpose - UserListAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of users. It helps to organize the users in recyclerView.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 23rd March 2020
 *
 */

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder>
        implements StickyHeaderAdapter<UserListAdapter.InitialHolder> {

    private  Context context;

    private List<User> userArrayList = new ArrayList<>();

    private static final String TAG = "UserListAdapter";

    private FontUtils fontUtils;

    /**
     * It is a contructor which is used to initialize wherever we needed.
     *
     * @param context is a object of Context.
     */
    public UserListAdapter(Context context) {
        this.context=context;
        fontUtils=FontUtils.getInstance(context);
    }

    /**
     * It is constructor which takes userArrayList as parameter and bind it with userArrayList in adapter.
     *
     * @param context is a object of Context.
     * @param userArrayList is a list of users used in this adapter.
     */
    public UserListAdapter(Context context, List<User> userArrayList) {
        this.userArrayList = userArrayList;
        this.context= context;
        fontUtils=FontUtils.getInstance(context);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        UserListRowBinding userListRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.user_list_row, parent, false);

        return new UserViewHolder(userListRowBinding);
    }

    /**
     * This method is used to bind the UserViewHolder contents with user at given
     * position. It set username userAvatar in respective UserViewHolder content.
     *
     * @param userViewHolder is a object of UserViewHolder.
     * @param i is a position of item in recyclerView.
     * @see User
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder userViewHolder, int i) {

        final User user = userArrayList.get(i);
        User user1 = i + 1 < userArrayList.size()? userArrayList.get(i + 1) : null;

        if (user1 != null && user.getName().toLowerCase().substring(0, 1).toCharArray()[0] == user1.getName().substring(0, 1).toLowerCase().toCharArray()[0]) {
            userViewHolder.userListRowBinding.tvSeprator.setVisibility(View.GONE);
        } else {
            userViewHolder.userListRowBinding.tvSeprator.setVisibility(View.VISIBLE);
        }
        userViewHolder.userListRowBinding.txtUserName.setText(user.getName());
        userViewHolder.userListRowBinding.executePendingBindings();
        userViewHolder.userListRowBinding.avUser.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        userViewHolder.userListRowBinding.getRoot().setTag(R.string.user, user);

        userViewHolder.userListRowBinding.txtUserName.setTypeface(fontUtils.getTypeFace(FontUtils.robotoMedium));

        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            userViewHolder.userListRowBinding.avUser.setInitials(user.getName());
        } else {
            userViewHolder.userListRowBinding.avUser.setAvatar(user.getAvatar());
        }
        if(Utils.isDarkMode(context)) {
            userViewHolder.userListRowBinding.txtUserName.setTextColor(context.getResources().getColor(R.color.textColorWhite));
            userViewHolder.userListRowBinding.tvSeprator.setBackgroundColor(context.getResources().getColor(R.color.grey));
        } else {
            userViewHolder.userListRowBinding.txtUserName.setTextColor(context.getResources().getColor(R.color.primaryTextColor));
            userViewHolder.userListRowBinding.tvSeprator.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
        }
    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }


    /**
     * This method is used to update the users of userArrayList in adapter.
     *
     * @param users is a list of updated user.
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
     * This method is used to update particular user in userArrayList of adapter.
     *
     * @param user is a object of User which will updated in userArrayList.
     * @see User
     */
    public void updateUser(User user) {
        if (userArrayList.contains(user)){
            int index=userArrayList.indexOf(user);
            userArrayList.remove(index);
            userArrayList.add(index,user);
            notifyItemChanged(index);
        }else {
            userArrayList.add(user);
            notifyItemInserted(getItemCount()-1);
        }
    }

    /**
     * This method is used to remove particular user from userArrayList of adapter.
     *
     * @param user is a object of user which will be removed from userArrayList.
     * @see User
     */
    public void removeUser(User user) {
        if (userArrayList.contains(user)) {
            int index=userArrayList.indexOf(user);
            this.userArrayList.remove(user);
            notifyItemRemoved(index);
        }

    }

    @Override
    public long getHeaderId(int var1) {
        User user = this.userArrayList.get(var1);
        char name = user.getName() != null && !user.getName().isEmpty() ? user.getName().substring(0, 1).toUpperCase().toCharArray()[0] : '#';
        return (int) name;

    }

    @Override
    public InitialHolder onCreateHeaderViewHolder(ViewGroup var1) {
        return new InitialHolder(LayoutInflater.from(var1.getContext()).inflate(R.layout.cc_initial_header, var1, false));
    }

    @Override
    public void onBindHeaderViewHolder(InitialHolder var1, int var2, long var3) {
        User user = userArrayList.get(var2);
        char name = user.getName() != null && !user.getName().isEmpty() ? user.getName().substring(0, 1).toCharArray()[0] : '#';
        var1.textView.setText(String.valueOf(name));
    }

    /**
     * This method is used to set list of search user with a userArrayList in adapter.
     *
     * @param users is a list of searched users.
     *
     */
    public void searchUser(List<User> users) {
        this.userArrayList = users;
        notifyDataSetChanged();
    }

    /**
     * This method is used to add a user in userArrayList.
     * @param user is a object of user which will be added in userArrayList.
     * @see User
     */
    public void add(User user) {
        updateUser(user);
    }

    /**
     * This method is used to add a user at particular position in userArrayList of adapter.
     *
     * @param index is a postion where user will be addded.
     * @param user is a object of User which will be added.
     * @see User
     */
    public void add(int index, User user) {
        userArrayList.add(index,user);
        notifyItemInserted(index);

    }

    /**
     * This method is used to update a user of particular position in userArrayList.
     *
     * @param index is a position of user.
     * @param user is a object of User which will be updated at given position in userArrayList.
     * @see User
     */
    public void updateUser(int index, User user) {
        if (userArrayList.contains(user)){
            userArrayList.remove(user);
            userArrayList.add(index,user);
            notifyDataSetChanged();
        }
    }

    /**
     * This method is used to remove user from particular position in userArrayList.
     * @param index is position of user which will be removed.
     */
    public void removeUser(int index) {
        if (userArrayList.size()<index) {
            userArrayList.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void clear() {
        userArrayList.clear();
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        UserListRowBinding userListRowBinding;
        UserViewHolder(UserListRowBinding userListRowBinding) {
            super(userListRowBinding.getRoot());
            this.userListRowBinding = userListRowBinding;

        }

    }

    class InitialHolder extends RecyclerView.ViewHolder {

        private TextView textView;

        InitialHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.text_char);
        }
    }
}
