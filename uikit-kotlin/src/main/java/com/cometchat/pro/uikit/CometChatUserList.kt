package com.cometchat.pro.uikit

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.CometChatUserList
import listeners.ClickListener
import listeners.OnItemClickListener
import listeners.RecyclerTouchListener
import viewmodel.UserListViewModel

/**
 * Purpose - CometChatUserList class is a subclass of recyclerview and used as component by
 * developer to display list of users. Developer just need to fetchUsers at their end
 * and pass it to this component to display list of Users. It helps user to create conversation
 * list easily and saves their time.
 * @see User
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 23rd March 2020
 */
@BindingMethods(value = [BindingMethod(type = CometChatUserList::class, attribute = "app:userlist", method = "setUserList")])
class CometChatUserList : RecyclerView {
    private var c: Context? = null
    private var userListViewModel: UserListViewModel? = null
    private var showHeader = false

    constructor(context: Context) : super(context) {
        this.c = context
        setViewModel()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.c = context
        getAttributes(attrs)
        setViewModel()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        this.c = context
        getAttributes(attrs)
        setViewModel()
    }

    private fun getAttributes(attributeSet: AttributeSet?) {
        val a = getContext().theme.obtainStyledAttributes(attributeSet, R.styleable.CometChatUserList, 0, 0)
        showHeader = a.getBoolean(R.styleable.CometChatUserList_headers, false)
    }

    /**
     * This methods sets the list of users provided by the developer
     *
     * @param userList list of users
     */
    fun setUserList(userList: List<User?>) {
        if (userListViewModel != null) userListViewModel!!.setUsersList(userList)
    }

    private fun setViewModel() {
        if (userListViewModel == null) {
            userListViewModel = UserListViewModel(context, this, showHeader)
        }
    }

    fun add(index: Int, user: User?) {
        if (userListViewModel != null) userListViewModel!!.add(index, user)
    }

    /**
     * Method helps in adding the user to list
     *
     * @param user to be added in the list
     */
    fun add(user: User?) {
        if (userListViewModel != null) userListViewModel!!.add(user)
    }

    /**
     * This methods updates the particular user provided by the developer
     *
     * @param user object of the user to be updated
     */
    fun update(user: User?) {
        if (userListViewModel != null) userListViewModel!!.update(user)
    }

    fun update(index: Int, user: User?) {
        if (userListViewModel != null) userListViewModel!!.update(index, user)
    }

    fun remove(index: Int) {
        if (userListViewModel != null) userListViewModel!!.remove(index)
    }

    /**
     * Removes user from the list based on user provided
     *
     * @param user of the user to be removed
     */
    fun remove(user: User?) {
        if (userListViewModel != null) {
            userListViewModel!!.remove(user)
        }
    }

    /**
     * This method provides click event callback to the developer.
     *
     * @param onItemClickListener object of `OnItemClickListener<User>`` class
    </User>` */
    fun setItemClickListener(onItemClickListener: OnItemClickListener<User?>?) {
        addOnItemTouchListener(RecyclerTouchListener(context, this, object : ClickListener() {
            override fun onClick(var1: View, var2: Int) {
                val user = var1.getTag(R.string.user) as User
                if (onItemClickListener != null) onItemClickListener.OnItemClick(user, var2) else throw NullPointerException(resources.getString(R.string.user_itemclick_error))
            }

            override fun onLongClick(var1: View, var2: Int) {
                val user = var1.getTag(R.string.user) as User
                if (onItemClickListener != null) onItemClickListener.OnItemLongClick(user, var2) else throw NullPointerException(resources.getString(R.string.user_itemclick_error))
            }
        }))
    }

    /**
     * This method is used to set list of searched user in CometChatUserList Component.
     * @param userList is object of List<User>. It is list of searched users.
    </User> */
    fun searchUserList(userList: List<User?>?) {
        userListViewModel!!.searchUserList(userList)
    }

    /**
     * This method is used to clear a userlist of CometChatUserList Component.
     */
    fun clear() {
        userListViewModel!!.clear()
    }
}