package viewmodel;

import android.content.Context;

import com.cometchat.pro.core.Call;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.Conversation;
import com.cometchat.pro.uikit.CometChatCallList;

import java.util.List;

import adapter.CallListAdapter;
import adapter.ConversationListAdapter;

public class CallViewModel {

    private  Context context;

    private CallListAdapter callListAdapter;

    private CometChatCallList callListView;

    private CallViewModel(){

    }
    public CallViewModel(Context context, CometChatCallList cometChatCallList){
        this.callListView=cometChatCallList;
        this.context=context;
        setAdapter();
    }

    private CallListAdapter getAdapter(){
       if (callListAdapter==null){
           callListAdapter=new CallListAdapter(context);
       }
       return callListAdapter;
    }

    public void add(Call call){
        if (callListAdapter!=null)
            callListAdapter.add(call);
    }

    private void setAdapter(){
        callListAdapter=new CallListAdapter(context);
        callListView.setAdapter(callListAdapter);
    }


    public void setCallList(List<BaseMessage> callList) {
        if (callListAdapter!=null)
            callListAdapter.updateList(callList);
    }


    public void update(Call call) {
        if (callListAdapter!=null)
            callListAdapter.update(call);
    }

    public void remove(Call call) {
        if (callListAdapter!=null)
            callListAdapter.remove(call);
    }

    public int size() {
        return callListAdapter.getItemCount();
    }
}