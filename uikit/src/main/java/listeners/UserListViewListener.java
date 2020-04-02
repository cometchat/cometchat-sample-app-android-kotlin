package listeners;

import android.view.View;

import com.cometchat.pro.models.User;

public interface UserListViewListener {

    /**
     * Get Single click event on UserList
     *
     * {@inheritDoc}
     * @param user user object of the class
     * @param position position of the clicked user in the list
     * @param view view reference of the item clicked
     * @see User
     *
     */
    void onClick(User user, int position,View view);


    /**
     *
     *  Get Long click event on UserList
     *{@inheritDoc}
     * @param user user object of the class
     * @param position position of the clicked user in the list
     * @param view view reference of the item clicked
     * @see User
     *
     */
    void onLongClick(User user, int position,View view);

}
