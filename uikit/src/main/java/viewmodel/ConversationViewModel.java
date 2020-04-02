package viewmodel;

import android.content.Context;
import android.util.Log;

import com.cometchat.pro.core.ConversationsRequest;
import com.cometchat.pro.models.Conversation;
import com.cometchat.pro.models.MessageReceipt;
import com.cometchat.pro.uikit.CometChatConversationList;

import java.util.List;

import adapter.ConversationListAdapter;

public class ConversationViewModel {

    private  Context context;

    private ConversationListAdapter conversationListAdapter;

    private ConversationViewModel(){

    }
    public ConversationViewModel(Context context,CometChatConversationList cometChatConversationList){
        this.context=context;
        setAdapter(cometChatConversationList);
    }

    private ConversationListAdapter getAdapter(){
       if (conversationListAdapter==null){
           conversationListAdapter=new ConversationListAdapter(context);
       }
       return conversationListAdapter;
    }

    public void add(Conversation conversation){
        if (conversationListAdapter!=null)
            conversationListAdapter.add(conversation);
    }

    private void setAdapter(CometChatConversationList cometChatConversationList){
        conversationListAdapter=new ConversationListAdapter(context);
        cometChatConversationList.setAdapter(conversationListAdapter);
    }


    public void setConversationList(List<Conversation> conversationList) {
        if (conversationListAdapter!=null) {
                conversationListAdapter.updateList(conversationList);
        }
        else
        {
            Log.e("ERROR", "setConversationList: ERROR " );
        }
    }


    public void update(Conversation conversation) {
        if (conversationListAdapter!=null)
            conversationListAdapter.update(conversation);
    }

    public void remove(Conversation conversation) {
        if (conversationListAdapter!=null)
            conversationListAdapter.remove(conversation);
    }

    public void searchConversation(String searchString) {
        if (conversationListAdapter!=null)
            conversationListAdapter.getFilter().filter(searchString);
    }

    public void setDeliveredReceipts(MessageReceipt messageReceipt) {
        if (conversationListAdapter!=null)
            conversationListAdapter.setDeliveredReceipts(messageReceipt);
    }

    public void setReadReceipts(MessageReceipt messageReceipt) {
        if (conversationListAdapter!=null)
            conversationListAdapter.setReadReceipts(messageReceipt);
    }

    public void clear() {
        if (conversationListAdapter!=null)
            conversationListAdapter.resetAdapterList();
    }

    public int size() {
        return conversationListAdapter.getItemCount();
    }
}