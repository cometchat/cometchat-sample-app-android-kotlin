package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.Conversation;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.uikit.databinding.CallHistoryRowBinding;
import com.cometchat.pro.uikit.databinding.CallListRowBinding;

import java.util.ArrayList;
import java.util.List;

import utils.FontUtils;
import utils.Utils;

/**
 * Purpose - CallHistoryAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of calls. It helps to organize the list data in recyclerView.
 *
 * Created on - 23rd March 2020
 *
 * Modified on  - 24th March 2020
 *
 */

public class CallHistoryAdapter extends RecyclerView.Adapter<CallHistoryAdapter.CallViewHolder> {

    private Context context;

    private List<BaseMessage> callList = new ArrayList<>();

    private FontUtils fontUtils;

    private String loggedInUser = CometChat.getLoggedInUser().getUid();
    /**
     * It is constructor which takes callList as parameter and bind it with callList in adapter.
     *
     * @param context is a object of Context.
     * @param callList is list of calls used in this adapter.
     */
    public CallHistoryAdapter(Context context, List<BaseMessage> callList) {
        updateList(callList);
        this.context = context;
        fontUtils=FontUtils.getInstance(context);
    }

    /**
     * It is a constructor which is used to initialize wherever we needed.
     *
     * @param context
     */
    public CallHistoryAdapter(Context context) {
        this.context = context;
        fontUtils=FontUtils.getInstance(context);

    }

    @NonNull
    @Override
    public CallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        CallHistoryRowBinding callHistoryRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.call_history_row, parent, false);

        return new CallViewHolder(callHistoryRowBinding);
    }

    /**
     *  This method is used to bind the ConversationViewHolder contents with conversation at given
     *  position. It set avatar, name, lastMessage, unreadMessageCount and messageTime of conversation
     *  in a respective ConversationViewHolder content. It checks whether conversation type is user
     *  or group and set name and avatar as accordingly. It also checks whether last message is text, media
     *  or file and modify txtUserMessage view accordingly.
     *
     * @param callViewHolder is a object of ConversationViewHolder.
     * @param position is a position of item in recyclerView.
     *
     * @see Conversation
     */
    @Override
    public void onBindViewHolder(@NonNull CallViewHolder callViewHolder, int position) {
        BaseMessage baseMessage = callList.get(position);
        Call call = (Call)baseMessage;
        boolean isIncoming=false,isVideo=false,isMissed=false;
        String callMessageText="";

        if(call.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
            if (call.getSender().getUid().equals(loggedInUser)) {
                if(call.getCallStatus().equals(CometChatConstants.CALL_STATUS_UNANSWERED)) {
                    callMessageText = context.getResources().getString(R.string.missed_call);
                    isMissed = true;
                } else if(call.getCallStatus().equals(CometChatConstants.CALL_STATUS_REJECTED)) {
                    callMessageText = context.getResources().getString(R.string.rejected_call);
                } else
                    callMessageText = context.getResources().getString(R.string.outgoing);
                isIncoming = false;
            } else {
                if(call.getCallStatus().equals(CometChatConstants.CALL_STATUS_UNANSWERED)) {
                    callMessageText = context.getResources().getString(R.string.missed_call);
                    isMissed = true;
                } else if(call.getCallStatus().equals(CometChatConstants.CALL_STATUS_REJECTED)) {
                    callMessageText = context.getResources().getString(R.string.rejected_call);
                } else
                    callMessageText = context.getResources().getString(R.string.incoming);
                isIncoming = true;
            }
        }
//        else {
//            if (call.getSender().getUid().equals(loggedInUser))
//            {
//                callMessageText = context.getResources().getString(R.string.outgoing);
//                isIncoming = false;
//            }
//            else
//            {
//                callMessageText = context.getResources().getString(R.string.incoming);
//                isIncoming = true;
//            }
//        }
        if(call.getType().equals(CometChatConstants.CALL_TYPE_VIDEO))
        {
            callMessageText = callMessageText+" "+context.getResources().getString(R.string.video_call);
            isVideo = true;
        }
        else
        {
            callMessageText = callMessageText+" "+context.getResources().getString(R.string.audio_call);
            isVideo = false;
        }
        if (isVideo)
        {
            callViewHolder.callHistoryRowBinding.callInfoTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_videocam_24dp,0,0,0);
        }
        else
        {
            if (isIncoming && isMissed) {
                callViewHolder.callHistoryRowBinding.callInfoTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_missed_incoming_24dp,0,0,0);
            } else if(isIncoming && !isMissed) {
                callViewHolder.callHistoryRowBinding.callInfoTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_incoming_24dp,0,0,0);
            } else if (!isIncoming && isMissed) {
                callViewHolder.callHistoryRowBinding.callInfoTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_missed_outgoing_24dp,0,0,0);
            } else {
                callViewHolder.callHistoryRowBinding.callInfoTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_outgoing_24dp,0,0,0);
            }
        }
        callViewHolder.callHistoryRowBinding.callTimeTv.setText(Utils.getHeaderDate(call.getInitiatedAt()*1000));
        callViewHolder.callHistoryRowBinding.callInfoTv.setText(callMessageText);
        callViewHolder.callHistoryRowBinding.callDateTv.setText(Utils.getDate(call.getSentAt()*1000L));
//        callViewHolder.callListRowBinding.executePendingBindings();
//        callType = call.getType();
//        callCategory = call.getCategory();
//        callViewHolder.callListRowBinding.callMessage.setText(call.getAction());
//        callViewHolder.callListRowBinding.callMessage.setTypeface(fontUtils.getTypeFace(FontUtils.robotoRegular));
//        callViewHolder.callListRowBinding.callSenderName.setTypeface(fontUtils.getTypeFace(FontUtils.robotoMedium));

//        if (conversation.getConversationType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
//            name = ((User) conversation.getConversationWith()).getName();
//            avatar = ((User) conversation.getConversationWith()).getAvatar();
//        } else {
//            name = ((Group) conversation.getConversationWith()).getName();
//            avatar = ((Group) conversation.getConversationWith()).getIcon();
//        }
//
//        conversationViewHolder.conversationListRowBinding.messageCount.setCount(conversation.getUnreadMessageCount());
//        conversationViewHolder.conversationListRowBinding.txtUserName.setText(name);
//        conversationViewHolder.conversationListRowBinding.avUser.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
//
//        if (avatar != null && !avatar.isEmpty()) {
//            conversationViewHolder.conversationListRowBinding.avUser.setAvatar(avatar);
//        } else {
//            conversationViewHolder.conversationListRowBinding.avUser.setInitials(name);
//        }

        callViewHolder.callHistoryRowBinding.getRoot().setTag(R.string.call, call);

    }

    @Override
    public int getItemCount() {
        return callList.size();
    }

    /**
     * This method is used to update the callList with new calls and avoid
     * duplicates call entries.
     *
     * @param calls is a list of calls which will be updated in adapter.
     */
    public void updateList(List<BaseMessage> calls) {

        callList.addAll(filterList(calls));
        notifyDataSetChanged();
    }

    private List<BaseMessage> filterList(List<BaseMessage> messageList)
    {
        ArrayList<BaseMessage> filteredList = new ArrayList<>();
        for (BaseMessage baseMessage : messageList)
        {
            if (((Call)baseMessage).getCallStatus().equals(CometChatConstants.CALL_STATUS_INITIATED)
                    || (((Call) baseMessage).getCallStatus().equals(CometChatConstants.CALL_STATUS_UNANSWERED))) {
                filteredList.add(baseMessage);
            }
        }
        return filteredList;
    }

    /**
     * This method is used to remove the call from callList
     *
     * @param call is a object of Call.
     *
     * @see Call
     *
     */
    public void remove(Call call) {
        int position = callList.indexOf(call);
        callList.remove(call);
        notifyItemRemoved(position);
    }


    /**
     * This method is used to update call in callList.
     *
     * @param call is an object of Call. It is used to update the previous call
     *                     in list
     * @see Call
     */
    public void update(Call call) {

        if (callList.contains(call)) {
            Call oldCall = (Call)callList.get(callList.indexOf(call));
            callList.remove(oldCall);
            callList.add(0, call);
        } else {
            callList.add(0, call);
        }
        notifyDataSetChanged();

    }

    /**
     * This method is used to add call in list.
     *
     * @param call is an object of Call. It will be added to callList.
     *
     * @see Call
     */
    public void add(Call call) {
        if (callList != null)
            callList.add(call);
    }

    /**
     * This method is used to reset the adapter by clearing filterConversationList.
     */
    public void resetAdapterList() {
        callList.clear();
        notifyDataSetChanged();
    }

    class CallViewHolder extends RecyclerView.ViewHolder {

        CallHistoryRowBinding callHistoryRowBinding;

        CallViewHolder(CallHistoryRowBinding callHistoryRowBinding) {
            super(callHistoryRowBinding.getRoot());
            this.callHistoryRowBinding = callHistoryRowBinding;
        }

    }
}
