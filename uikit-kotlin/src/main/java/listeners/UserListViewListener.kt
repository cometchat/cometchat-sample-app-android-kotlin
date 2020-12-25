package listeners

import android.view.View
import com.cometchat.pro.models.User

interface UserListViewListener {
    /**
     * Get Single click event on UserList
     *
     * {@inheritDoc}
     * @param user user object of the class
     * @param position position of the clicked user in the list
     * @param view view reference of the item clicked
     * @see User
     */
    fun onClick(user: User?, position: Int, view: View?)

    /**
     *
     * Get Long click event on UserList
     * {@inheritDoc}
     * @param user user object of the class
     * @param position position of the clicked user in the list
     * @param view view reference of the item clicked
     * @see User
     */
    fun onLongClick(user: User?, position: Int, view: View?)
}