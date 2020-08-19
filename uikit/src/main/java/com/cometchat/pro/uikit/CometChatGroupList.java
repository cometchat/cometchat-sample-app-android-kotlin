package com.cometchat.pro.uikit;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingMethod;
import androidx.databinding.BindingMethods;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.pro.models.Group;

import java.util.List;

import listeners.ClickListener;
import listeners.OnItemClickListener;
import listeners.RecyclerTouchListener;
import viewmodel.GroupListViewModel;

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
 *
 */

@BindingMethods( value ={@BindingMethod(type = CometChatGroupList.class, attribute = "app:grouplist", method = "setGroupList")})
public class CometChatGroupList extends RecyclerView {

    private Context context;

    private GroupListViewModel groupListViewModel;

    public CometChatGroupList(@NonNull Context context) {
        super(context);
        this.context=context;
        setViewModel();

    }

    public CometChatGroupList(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        getAttributes(attrs);
        this.context=context;
        setViewModel();
    }

    public CometChatGroupList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getAttributes(attrs);
        this.context=context;
        setViewModel();
    }


    private void getAttributes(AttributeSet attributeSet){
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attributeSet, R.styleable.CometChatGroupList, 0, 0);

    }

    private void setViewModel(){
        if(groupListViewModel==null){
            groupListViewModel=new GroupListViewModel(context,this);
        }
    }

    /**
     * This method helps updating the group in CometChatGroupList
     *
     * @param group object to be updated in the list
     * @param position is int value which indicates position of group in list.
     */
    public void update(Group group,int position){
        if (groupListViewModel!=null){
            groupListViewModel.update(group,position);
        }
    }
    /**
     *   Removes group from the list based on group object provided
     *
     *  @param group of the group to be removed
     *
     */
    public void remove(Group group){
        if (groupListViewModel!=null)
            groupListViewModel.remove(group);

    }

    /**
     * Add group to the list
     *
     * @param group to be added in the list
     *
     */
    public void add(Group group){
        if (groupListViewModel!=null)
            groupListViewModel.add(group);
    }

    /**
     *  This methods sets/update the list of group provided by the developer
     *
     * @param groupList list of groups
     *
     */
    public void setGroupList(List<Group> groupList){
          if (groupListViewModel!=null)
          groupListViewModel.setGroupList(groupList);

    }

    /**
     * This method is used to get events on click of group item in group list.
     *
     * @param onItemClickListener is a object on OnItemClickListener
     *
     */
    public void setItemClickListener(OnItemClickListener<Group> onItemClickListener){

        this.addOnItemTouchListener(new RecyclerTouchListener(context, this, new ClickListener() {
            @Override
            public void onClick(View var1, int var2) {
                Group group=(Group)var1.getTag(R.string.group);
                if (onItemClickListener!=null)
                    onItemClickListener.OnItemClick(group,var2);
                else
                    throw new NullPointerException(getResources().getString(R.string.group_itemclick_error));
            }

            @Override
            public void onLongClick(View var1, int var2) {
                Group group=(Group)var1.getTag(R.string.group);
                if (onItemClickListener!=null)
                onItemClickListener.OnItemLongClick(group,var2);
                else
                    throw new NullPointerException(getResources().getString(R.string.group_itemclick_error));
            }
        }));

    }

    /**
     * This method is used to update a grouplist with a searched groups list in CometChatGroupList Component.
     * @param groups is object of List<Group>, It is list of searched groups.
     */
    public void searchGroupList(List<Group> groups) {
        groupListViewModel.searchGroupList(groups);
    }

    /**
     * This method is used to clear a list of groups present in CometChatGroupList Component.
     *
     * @see GroupListViewModel#clear()
     */
    public void clear() {
        groupListViewModel.clear();
    }
}
