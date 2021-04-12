package com.cometchat.pro.uikit.ui_components.shared.cometchatUsers

import com.cometchat.pro.uikit.ui_components.shared.cometchatUsers.CometChatUsersAdapter.InitialHolder
import com.cometchat.pro.uikit.ui_components.shared.cometchatUsers.CometChatUsersAdapter.UserViewHolder
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.databinding.CometchatUserListItemBinding
import com.cometchat.pro.uikit.ui_resources.utils.sticker_header.StickyHeaderAdapter
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import java.util.*

/**
 * Purpose - UserListAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of users. It helps to organize the users in recyclerView.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 23rd March 2020
 *
 */
class CometChatUsersAdapter(context: Context) : RecyclerView.Adapter<UserViewHolder>(), StickyHeaderAdapter<InitialHolder?> {
    private var context: Context
    private var userArrayList: MutableList<User> = ArrayList()
    private var fontUtils: FontUtils

    /**
     * It is constructor which takes userArrayList as parameter and bind it with userArrayList in adapter.
     *
     * @param context is a object of Context.
     * @param userArrayList is a list of users used in this adapter.
     */
    init {
        this.userArrayList = userArrayList
        this.context = context
        fontUtils = FontUtils.getInstance(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): UserViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val userListRowBinding: CometchatUserListItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cometchat_user_list_item, parent, false)
        return UserViewHolder(userListRowBinding)
    }

    /**
     * This method is used to bind the UserViewHolder contents with user at given
     * position. It set username userAvatar in respective UserViewHolder content.
     *
     * @param userViewHolder is a object of UserViewHolder.
     * @param i is a position of item in recyclerView.
     * @see User
     */
    override fun onBindViewHolder(userViewHolder: UserViewHolder, i: Int) {
        val user = userArrayList[i]
        val user1 = if (i + 1 < userArrayList.size) userArrayList[i + 1] else null
        if (user1 != null && user.name.toLowerCase().substring(0, 1).toCharArray()[0] == user1.name.substring(0, 1).toLowerCase().toCharArray()[0]) {
            userViewHolder.userListRowBinding.tvSeprator.visibility = View.GONE
        } else {
            userViewHolder.userListRowBinding.tvSeprator.visibility = View.VISIBLE
        }
        if (user.status == CometChatConstants.USER_STATUS_ONLINE)
            userViewHolder.userListRowBinding.statusIndicator.visibility = View.VISIBLE

        userViewHolder.userListRowBinding.statusIndicator.setUserStatus(user.status)
        userViewHolder.userListRowBinding.txtUserName.text = user.name
        userViewHolder.userListRowBinding.executePendingBindings()
        userViewHolder.userListRowBinding.avUser.setBackgroundColor(context.resources.getColor(R.color.colorPrimary))
        userViewHolder.userListRowBinding.root.setTag(R.string.user, user)
        userViewHolder.userListRowBinding.txtUserName.typeface = fontUtils.getTypeFace(FontUtils.robotoMedium)
        if (user.avatar == null || user.avatar.isEmpty()) {
            userViewHolder.userListRowBinding.avUser.setInitials(user.name)
        } else {
            userViewHolder.userListRowBinding.avUser.setAvatar(user.avatar)
        }
        if (Utils.isDarkMode(context)) {
            userViewHolder.userListRowBinding.txtUserName.setTextColor(context.resources.getColor(R.color.textColorWhite))
            userViewHolder.userListRowBinding.tvSeprator.setBackgroundColor(context.resources.getColor(R.color.grey))
        } else {
            userViewHolder.userListRowBinding.txtUserName.setTextColor(context.resources.getColor(R.color.primaryTextColor))
            userViewHolder.userListRowBinding.tvSeprator.setBackgroundColor(context.resources.getColor(R.color.light_grey))
        }
    }

    override fun getItemCount(): Int {
        return userArrayList.size
    }

    /**
     * This method is used to update the users of userArrayList in adapter.
     *
     * @param users is a list of updated user.
     */
    fun updateList(users: List<User?>) {
        for (i in users.indices) {
            if (userArrayList.contains(users[i])) {
                val index = userArrayList.indexOf(users[i])
                userArrayList.removeAt(index)
                userArrayList.add(index, users[i]!!)
            } else {
                userArrayList.add(users[i]!!)
            }
        }
        notifyDataSetChanged()
    }

    /**
     * This method is used to update particular user in userArrayList of adapter.
     *
     * @param user is a object of User which will updated in userArrayList.
     * @see User
     */
    fun updateUser(user: User) {
        if (userArrayList.contains(user)) {
            val index = userArrayList.indexOf(user)
            userArrayList.removeAt(index)
            userArrayList.add(index, user)
            notifyItemChanged(index)
        } else {
            userArrayList.add(user)
            notifyItemInserted(itemCount - 1)
        }
    }

    /**
     * This method is used to remove particular user from userArrayList of adapter.
     *
     * @param user is a object of user which will be removed from userArrayList.
     * @see User
     */
    fun removeUser(user: User?) {
        if (userArrayList.contains(user)) {
            val index = userArrayList.indexOf(user)
            userArrayList.remove(user)
            notifyItemRemoved(index)
        }
    }

    override fun getHeaderId(var1: Int): Long {
        val user = userArrayList[var1]
        val name = if (user.name != null && user.name.isNotEmpty()) user.name.substring(0, 1).toUpperCase().toCharArray()[0] else '#'
        return name.toLong()
    }

    override fun onCreateHeaderViewHolder(var1: ViewGroup?): InitialHolder? {
        return InitialHolder(LayoutInflater.from(var1!!.context).inflate(R.layout.cometchat_userlist_header, var1, false))
    }

    override fun onBindHeaderViewHolder(var1: Any, var2: Int, var3: Long) {
        val user = userArrayList[var2]
        val name = if (user.name != null && user.name.isNotEmpty()) user.name.substring(0, 1).toCharArray()[0] else '#'
        var initialHolder = var1 as InitialHolder;
        initialHolder.textView.text = name.toString()
    }

    /**
     * This method is used to set list of search user with a userArrayList in adapter.
     *
     * @param users is a list of searched users.
     */
    fun searchUser(users: List<User?>) {
        userArrayList.clear()
        updateList(users)
        notifyDataSetChanged()
    }

    /**
     * This method is used to add a user in userArrayList.
     * @param user is a object of user which will be added in userArrayList.
     * @see User
     */
    fun add(user: User) {
        updateUser(user)
    }

    /**
     * This method is used to add a user at particular position in userArrayList of adapter.
     *
     * @param index is a postion where user will be addded.
     * @param user is a object of User which will be added.
     * @see User
     */
    fun add(index: Int, user: User) {
        userArrayList.add(index, user)
        notifyItemInserted(index)
    }

    /**
     * This method is used to update a user of particular position in userArrayList.
     *
     * @param index is a position of user.
     * @param user is a object of User which will be updated at given position in userArrayList.
     * @see User
     */
    fun updateUser(index: Int, user: User) {
        if (userArrayList.contains(user)) {
            userArrayList.remove(user)
            userArrayList.add(index, user)
            notifyDataSetChanged()
        }
    }

    /**
     * This method is used to remove user from particular position in userArrayList.
     * @param index is position of user which will be removed.
     */
    fun removeUser(index: Int) {
        if (userArrayList.size < index) {
            userArrayList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun clear() {
        userArrayList.clear()
        notifyDataSetChanged()
    }

    inner class UserViewHolder(var userListRowBinding: CometchatUserListItemBinding) : RecyclerView.ViewHolder(userListRowBinding.root)

    inner class InitialHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView

        init {
            textView = itemView.findViewById(R.id.text_char)
        }
    }

    companion object {
        private const val TAG = "UserListAdapter"
    }
}