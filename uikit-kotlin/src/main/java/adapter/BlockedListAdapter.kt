package adapter

import adapter.BlockedListAdapter.BlockedViewHolder
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.databinding.UserListRowBinding
import utils.FontUtils
import utils.Utils
import java.util.*

/**
 * Purpose - BlockListAdapter is a subclass of RecyclerView Adapter which is used to display the list of blocked
 * users. It helps to organize the list data in recyclerview.
 *
 * Created on - 16th June 2020
 *
 * Modified on  - 16th June 2020
 *
 */
class BlockedListAdapter(context: Context, userArrayList: List<User>) : RecyclerView.Adapter<BlockedViewHolder>() {
    private var context: Context
    private val userArrayList: MutableList<User> = ArrayList()
    private val fontUtils: FontUtils
    companion object {
        private const val TAG = "UserListAdapter"
    }

    /**
     * It is a constructor which is used to initialized the adapter whereever we needed.
     *
     * @param context is object of Context.
     * @param userArrayList is a list of blocked users used in this adapter.
     */
    init {
        updateList(userArrayList!!)
        this.context = context
        fontUtils = FontUtils.getInstance(context)
    }
    override fun onCreateViewHolder(parent: ViewGroup, i: Int): BlockedViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val userListRowBinding: UserListRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.user_list_row, parent, false)
        return BlockedViewHolder(userListRowBinding)
    }

    /**
     * This method is used to bind the BlockedViewHolder contents with user at given position.
     * It sets name and avatar field of a user with respective BlockedViewHolder content.
     *
     * @param blockedViewHolder is a object of BlockedViewHolder
     * @param i is a position of item in recyclerView
     * @see User
     */
    override fun onBindViewHolder(blockedViewHolder: BlockedViewHolder, i: Int) {
        val user = userArrayList[i] //Take User which is at ith position in userArrayList
        val user1 = if (i + 1 < userArrayList.size) userArrayList[i + 1] else null
        if (user1 != null && user.name.substring(0, 1).toCharArray()[0] == user1.name.substring(0, 1).toCharArray()[0]) {
            blockedViewHolder.userListRowBinding.tvSeprator.visibility = View.GONE
        } else {
            blockedViewHolder.userListRowBinding.tvSeprator.visibility = View.VISIBLE
        }
        blockedViewHolder.userListRowBinding.txtUserName.text = user.name
        blockedViewHolder.userListRowBinding.avUser.setBackgroundColor(context.resources.getColor(R.color.colorPrimary))
        blockedViewHolder.userListRowBinding.root.setTag(R.string.user, user)
        blockedViewHolder.userListRowBinding.txtUserScope.visibility = View.GONE
        blockedViewHolder.userListRowBinding.unblockUser.visibility = View.VISIBLE
        blockedViewHolder.userListRowBinding.txtUserName.typeface = fontUtils.getTypeFace(FontUtils.robotoMedium)
        if (user.avatar == null || user.avatar.isEmpty()) {
            blockedViewHolder.userListRowBinding.avUser.setInitials(user.name)
        } else {
            blockedViewHolder.userListRowBinding.avUser.setAvatar(user.avatar)
        }
        if (Utils.isDarkMode(context)) blockedViewHolder.userListRowBinding.txtUserName.setTextColor(context.resources.getColor(R.color.textColorWhite)) else blockedViewHolder.userListRowBinding.txtUserName.setTextColor(context.resources.getColor(R.color.primaryTextColor))
    }

    override fun getItemCount(): Int {
        return userArrayList.size
    }

    /**
     * This method is used to update userList of adapter with new usersList which is passed in parameter.
     * @param users is a list of users which will be updated in adapter.
     */
    fun updateList(users: List<User>) {
        for (i in users.indices) {
            if (userArrayList.contains(users[i])) {
                val index = userArrayList.indexOf(users[i])
                userArrayList.removeAt(index)
                userArrayList.add(index, users[i])
            } else {
                userArrayList.add(users[i])
            }
        }
        notifyDataSetChanged()
    }

    /**
     * This method is used to remove user from userlist.
     *
     * @param user is an object of User. It is a user which will be removed from list.
     *
     * @see User
     */
    fun removeUser(user: User?) {
        if (userArrayList.contains(user)) {
            val index = userArrayList.indexOf(user)
            userArrayList.remove(user)
            notifyItemRemoved(index)
        }
    }

    inner class BlockedViewHolder(var userListRowBinding: UserListRowBinding) : RecyclerView.ViewHolder(userListRowBinding.root)
}