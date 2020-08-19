package com.cometchat.pro.uikit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingMethod;
import androidx.databinding.BindingMethods;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.Conversation;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.User;

import java.util.List;

import listeners.ClickListener;
import listeners.OnItemClickListener;
import listeners.RecyclerTouchListener;
import viewmodel.CallViewModel;
import viewmodel.ConversationViewModel;


/**
 * Purpose - CometChatCallList class is a subclass of recyclerview and used as component by
 * developer to display list of calls. Developer just need to fetchMessages whose type is ACTION_CALL
 * at their end and pass it to this component to display list of calls. It helps user to create call
 * list easily and saves their time.
 *
 * @see com.cometchat.pro.core.Call
 *
 * Created on - 23rd March 2020
 *
 * Modified on  - 02nd April 2020
 *
*/

@BindingMethods( value ={@BindingMethod(type = CometChatCallList.class, attribute = "app:calllist", method = "setCallList")})
public class CometChatCallList extends RecyclerView {

    private  Context context;

    private CallViewModel callViewModel;

    public CometChatCallList(@NonNull Context context) {
        super(context);
        this.context=context;
        setViewModel();
    }

    public CometChatCallList(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        setViewModel();
    }

    public CometChatCallList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context=context;
        setViewModel();
    }

    private void setViewModel(){
        if (callViewModel==null)
            callViewModel=new CallViewModel(context,this);

    }

    /**
     *   This method set the fetched list into the CometChatCallList Component.
     *
     * @param callList to set into the view CometChatCallList
     */
    public void setCallList(List<BaseMessage> callList){
        if (callViewModel!=null)
            callViewModel.setCallList(callList);
    }

    /**
     *  This methods updates the call item or add if not present in the list
     *
     *
     * @param call to be added or updated
     *
     */
    public void update(Call call){
        if (callViewModel!=null)
            callViewModel.update(call);
    }

    /**
     *  provide way to remove a particular call from the list
     *
     * @param call to be removed
     */
    public void remove(Call call){
        if (callViewModel!=null)
            callViewModel.remove(call);
    }


    /**
     *  This method helps to get Click events of CometChatCallList
     *
     * @param onItemClickListener object of the OnItemClickListener
     *
     */
    public void setItemClickListener(OnItemClickListener<Call> onItemClickListener){

        this.addOnItemTouchListener(new RecyclerTouchListener(context, this, new ClickListener() {
            @Override
            public void onClick(View var1, int var2) {
                Call call=(Call)var1.getTag(R.string.call);
                var1.findViewById(R.id.user_detail_vw).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener!=null)
                            onItemClickListener.OnItemClick(call,var2);
                        else
                            throw new NullPointerException("OnItemClickListener<Call> is null" );
                    }
                });
            }

            @Override
            public void onLongClick(View var1, int var2) {
                Call call =(Call)var1.getTag(R.string.call);
                 if (onItemClickListener!=null)
                     onItemClickListener.OnItemLongClick(call,var2);
                 else
                     throw new NullPointerException("OnItemClickListener<Call> is null" );

            }
        }));

    }

    /**
     *  This method helps to get Click events of CometChatCallList
     *
     * @param onItemClickListener object of the OnItemClickListener
     *
     */
    public void setItemCallClickListener(OnItemClickListener<Call> onItemClickListener){

        this.addOnItemTouchListener(new RecyclerTouchListener(context, this, new ClickListener() {
            @Override
            public void onClick(View var1, int var2) {
                String uid;
                String type;
                Call call=(Call)var1.getTag(R.string.call);
                if (call.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                    if (((User)call.getCallInitiator()).getUid().equals(CometChat.getLoggedInUser().getUid())) {
                        uid = ((User)call.getCallReceiver()).getUid();
                    }
                    else {
                        uid = ((User)call.getCallInitiator()).getUid();
                    }
                    type = CometChatConstants.RECEIVER_TYPE_USER;
                }
                else
                {
                    uid = ((Group)call.getCallReceiver()).getGuid();
                    type = CometChatConstants.RECEIVER_TYPE_GROUP;
                }
                var1.findViewById(R.id.call_iv).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Call callObj = new Call(uid,type,CometChatConstants.CALL_TYPE_AUDIO);
                        if (onItemClickListener!=null)
                            onItemClickListener.OnItemClick(callObj,var2);
                        else
                            throw new NullPointerException("OnItemClickListener<Call> is null" );
                    }
                });
            }

            @Override
            public void onLongClick(View var1, int var2) {

            }
        }));

    }
    public int size() {
        return callViewModel.size();
    }
}
