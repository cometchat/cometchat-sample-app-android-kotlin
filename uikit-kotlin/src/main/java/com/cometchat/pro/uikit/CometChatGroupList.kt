package com.cometchat.pro.uikit

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.models.Group
import com.cometchat.pro.uikit.CometChatGroupList
import listeners.ClickListener
import listeners.OnItemClickListener
import listeners.RecyclerTouchListener
import viewmodel.GroupListViewModel

/**
 * Purpose - CometChatGroupList class is a subclass of recyclerview and used as component by
 * developer to display list of groups. Developer just need to fetchGroup at their end
 * and pass it to this component to display list of groups. It helps user to create groups
 * list easily and saves their time.
 * @see Group
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 23rd March 2020
 */
@BindingMethods(value = [BindingMethod(type = CometChatGroupList::class, attribute = "app:grouplist", method = "setGroupList")])
class CometChatGroupList : RecyclerView {
    private var c: Context
    private var groupListViewModel: GroupListViewModel? = null

    constructor(context: Context) : super(context) {
        this.c = context
        setViewModel()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        getAttributes(attrs)
        this.c = context
        setViewModel()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        getAttributes(attrs)
        this.c = context
        setViewModel()
    }

    private fun getAttributes(attributeSet: AttributeSet?) {
        val a = getContext().theme.obtainStyledAttributes(attributeSet, R.styleable.CometChatGroupList, 0, 0)
    }

    private fun setViewModel() {
        if (groupListViewModel == null) {
            groupListViewModel = GroupListViewModel(context, this)
        }
    }

    /**
     * This method helps updating the group in CometChatGroupList
     *
     * @param group object to be updated in the list
     */
    fun update(group: Group?) {
        if (groupListViewModel != null) {
            groupListViewModel!!.update(group)
        }
    }

    /**
     * Removes group from the list based on group object provided
     *
     * @param group of the group to be removed
     */
    fun remove(group: Group?) {
        if (groupListViewModel != null) groupListViewModel!!.remove(group)
    }

    /**
     * Add group to the list
     *
     * @param group to be added in the list
     */
    fun add(group: Group?) {
        if (groupListViewModel != null) groupListViewModel!!.add(group)
    }

    /**
     * This methods sets/update the list of group provided by the developer
     *
     * @param groupList list of groups
     */
    fun setGroupList(groupList: List<Group>?) {
        if (groupListViewModel != null) groupListViewModel!!.setGroupList(groupList)
    }

    /**
     * This method is used to get events on click of group item in group list.
     *
     * @param onItemClickListener is a object on OnItemClickListener
     */
    fun setItemClickListener(onItemClickListener: OnItemClickListener<Group?>?) {
        addOnItemTouchListener(RecyclerTouchListener(context, this, object : ClickListener() {
            override fun onClick(var1: View, var2: Int) {
                val group = var1.getTag(R.string.group) as Group
                if (onItemClickListener != null) onItemClickListener.OnItemClick(group, var2) else throw NullPointerException(resources.getString(R.string.group_itemclick_error))
            }

            override fun onLongClick(var1: View, var2: Int) {
                val group = var1.getTag(R.string.group) as Group
                if (onItemClickListener != null) onItemClickListener.OnItemLongClick(group, var2) else throw NullPointerException(resources.getString(R.string.group_itemclick_error))
            }
        }))
    }

    /**
     * This method is used to update a grouplist with a searched groups list in CometChatGroupList Component.
     * @param groups is object of List<Group>, It is list of searched groups.
    </Group> */
    fun searchGroupList(groups: List<Group?>?) {
        groupListViewModel!!.searchGroupList(groups)
    }

    /**
     * This method is used to clear a list of groups present in CometChatGroupList Component.
     *
     * @see GroupListViewModel.clear
     */
    fun clear() {
        groupListViewModel!!.clear()
    }
}