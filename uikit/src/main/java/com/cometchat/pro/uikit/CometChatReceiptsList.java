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

import com.cometchat.pro.models.MessageReceipt;
import com.cometchat.pro.models.User;

import java.util.List;

import listeners.ClickListener;
import listeners.OnItemClickListener;
import listeners.RecyclerTouchListener;
import viewmodel.ReceiptListViewModel;
import viewmodel.UserListViewModel;

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
 *
 */

@BindingMethods(value = {@BindingMethod(type = CometChatReceiptsList.class, attribute = "app:receiptlist", method = "setReceiptsList")})
public class CometChatReceiptsList extends RecyclerView {

    private Context context;

    private ReceiptListViewModel receiptListViewModel;

    private boolean showDelivery;

    private boolean showRead;

    public CometChatReceiptsList(@NonNull Context context) {
        super(context);
        this.context = context;
        setViewModel();
    }

    public CometChatReceiptsList(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        getAttributes(attrs);
        setViewModel();
    }

    public CometChatReceiptsList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        getAttributes(attrs);
        setViewModel();
    }

    private void getAttributes(AttributeSet attributeSet) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attributeSet, R.styleable.CometChatReceiptsList, 0, 0);
        showDelivery = a.getBoolean(R.styleable.CometChatReceiptsList_showDeliveryReceipt,true);
        showRead = a.getBoolean(R.styleable.CometChatReceiptsList_showReadReceipt,true);
    }

    /**
     *  This methods sets the list of messageReceipts provided by the developer
     *
     * @param messageReceiptList list of users
     *
     */
    public void setMessageReceiptList(List<MessageReceipt> messageReceiptList) {
        if (receiptListViewModel != null)
            receiptListViewModel.setReceiptList(messageReceiptList);
    }

    private void setViewModel() {
        if (receiptListViewModel == null) {
            receiptListViewModel = new ReceiptListViewModel(context,this,showDelivery,showRead);
        }
    }

    public void add(int index,MessageReceipt messageReceipt){
        if (receiptListViewModel!=null)
            receiptListViewModel.add(index,messageReceipt);
    }

    /**
     * Method helps in adding the messageReceipt to list
     *
     * @param messageReceipt to be added in the list
     */
    public void add(MessageReceipt messageReceipt){
        if (receiptListViewModel!=null)
            receiptListViewModel.add(messageReceipt);
    }

    /**
     *  This methods updates the particular messageReceipt provided by the developer
     *
     * @param messageReceipt object of the MessageReceipt to be updated
     *
     */
    public void update(MessageReceipt messageReceipt){
        if (receiptListViewModel!=null)
            receiptListViewModel.update(messageReceipt);

    }

    public void update(int index,MessageReceipt messageReceipt){
        if (receiptListViewModel!=null)
            receiptListViewModel.update(index,messageReceipt);
    }

    /**
     * This method is used to clear a receiptList of CometChatreceiptList Component.
     */
    public void clear() {
        receiptListViewModel.clear();
    }

    public void showDelivery(boolean showDelivery) {
        this.showDelivery = showDelivery;
    }

    public void showRead(boolean showRead) {
        this.showRead = showRead;
    }
}
