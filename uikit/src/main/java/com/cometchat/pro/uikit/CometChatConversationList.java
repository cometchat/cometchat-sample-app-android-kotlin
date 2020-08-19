package com.cometchat.pro.uikit;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingMethod;
import androidx.databinding.BindingMethods;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.helpers.CometChatHelper;
import com.cometchat.pro.models.Action;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.Conversation;
import com.cometchat.pro.models.CustomMessage;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.MediaMessage;
import com.cometchat.pro.models.MessageReceipt;
import com.cometchat.pro.models.TextMessage;
import com.cometchat.pro.models.User;

import java.util.List;

import listeners.ClickListener;
import listeners.OnItemClickListener;
import listeners.RecyclerTouchListener;
import viewmodel.ConversationViewModel;


/**
 * Purpose - CometChatConversationList class is a subclass of recyclerview and used as component by
 * developer to display list of conversation. Developer just need to fetchConversation at their end
 * and pass it to this component to display list of conversation. It helps user to create conversation
 * list easily and saves their time.
 * @see Conversation
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 23rd March 2020
 *
*/

@BindingMethods( value ={@BindingMethod(type = CometChatConversationList.class, attribute = "app:conversationlist", method = "setConversationList")})
public class CometChatConversationList extends RecyclerView {

    private  Context context;

    private ConversationViewModel conversationViewModel;

    public CometChatConversationList(@NonNull Context context) {
        super(context);
        this.context=context;
        setViewModel();
    }

    public CometChatConversationList(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        setViewModel();
    }

    public CometChatConversationList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context=context;
        setViewModel();
    }

    private void setViewModel(){
        if (conversationViewModel==null)
            conversationViewModel=new ConversationViewModel(context,this);
    }

    /**
     *   This method set the fetched list into the CometChatConversationList Component.
     *
     * @param conversationList to set into the view CometChatConversationList
     */
    public void setConversationList(List<Conversation> conversationList){
        if (conversationViewModel!=null)
            conversationViewModel.setConversationList(conversationList);
    }

    /**
     *  This methods updates the conversation item or add if not present in the list
     *
     *
     * @param conversation to be added or updated
     *
     */
    public void update(Conversation conversation){
        if (conversationViewModel!=null)
            conversationViewModel.update(conversation);
    }

    /**
     *  provide way to remove a particular conversation from the list
     *
     * @param conversation to be removed
     */
    public void remove(Conversation conversation){
        if (conversationViewModel!=null)
            conversationViewModel.remove(conversation);
    }


    /**
     *  This method helps to get Click events of CometChatConversationList
     *
     * @param onItemClickListener object of the OnItemClickListener
     *
     */
    public void setItemClickListener(OnItemClickListener<Conversation> onItemClickListener){

        this.addOnItemTouchListener(new RecyclerTouchListener(context, this, new ClickListener() {
            @Override
            public void onClick(View var1, int var2) {
                Conversation conversation=(Conversation)var1.getTag(R.string.conversation);
                if (onItemClickListener!=null)
                    onItemClickListener.OnItemClick(conversation,var2);
                else
                    throw new NullPointerException(getResources().getString(R.string.conversation_itemclick_error));
            }

            @Override
            public void onLongClick(View var1, int var2) {
                Conversation conversation=(Conversation)var1.getTag(R.string.conversation);
                 if (onItemClickListener!=null)
                     onItemClickListener.OnItemLongClick(conversation,var2);
                 else
                     throw new NullPointerException(getResources().getString(R.string.conversation_itemclick_error));

            }
        }));

    }

    /**
     * This method is used to perform search operation in a list of conversations.
     * @param searchString is a String object which will be searched in conversation.
     *
     * @see ConversationViewModel#searchConversation(String)
     */
    public void searchConversation(String searchString) {
        conversationViewModel.searchConversation(searchString);
    }

    /**
     * This method is used to refresh conversation list if any new conversation is initiated or updated.
     * It converts the message recieved from message listener using <code>CometChatHelper.getConversationFromMessage(message)</code>
     *
     * @param message
     * @see CometChatHelper#getConversationFromMessage(BaseMessage)
     * @see Conversation
     */
    public void refreshConversation(BaseMessage message) {
        Conversation newConversation = CometChatHelper.getConversationFromMessage(message);
        update(newConversation);
    }


    /**
     * This method is used to update Reciept of conversation from conversationList.
     * @param messageReceipt is object of MessageReceipt which is recieved in real-time.
     *
     * @see MessageReceipt
     */
    public void setReciept(MessageReceipt messageReceipt) {
        if (conversationViewModel != null && messageReceipt.getReceivertype().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
            if (messageReceipt.getReceiptType().equals(MessageReceipt.RECEIPT_TYPE_DELIVERED))
                conversationViewModel.setDeliveredReceipts(messageReceipt);
            else
                conversationViewModel.setReadReceipts(messageReceipt);
        }
    }

    /**
     * This method is used to clear a list of conversation present in CometChatConversationList Component
     * @see ConversationViewModel#clear()
     */
    public void clearList() {
        if (conversationViewModel!=null)
            conversationViewModel.clear();
    }

    public int size() {
        return conversationViewModel.size();
    }
}
