package adapter;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.models.MessageReceipt;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.uikit.databinding.ConversationListRowBinding;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.Conversation;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.TextMessage;
import com.cometchat.pro.models.User;

import java.util.ArrayList;
import java.util.List;

import utils.FontUtils;
import utils.Utils;

/**
 * Purpose - ConversationListAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of conversations. It helps to organize the list data in recyclerView.
 * It also help to perform search operation on list of conversation.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 23rd March 2020
 *
 */

public class ConversationListAdapter extends RecyclerView.Adapter<ConversationListAdapter.ConversationViewHolder> implements Filterable {

    private Context context;

    /**
     * ConversationListAdapter maintains two arrayList i.e conversationList and filterConversationList.
     * conversationList is a original list and it will not get modified while filterConversationList
     * will get modified as per search filter. In case if search field is empty then to retrieve
     * original list we set filerConversationList = conversationList.
     * Here filterConversationList will be main list for this adapter.
     */
    private List<Conversation> conversationList = new ArrayList<>();

    private List<Conversation> filterConversationList = new ArrayList<>();

    private FontUtils fontUtils;

    /**
     * It is constructor which takes conversationList as parameter and bind it with conversationList
     * and filterConversationList in adapter.
     *
     * @param context is a object of Context.
     * @param conversationList is list of conversations used in this adapter.
     */
    public ConversationListAdapter(Context context, List<Conversation> conversationList) {
        this.conversationList = conversationList;
        this.filterConversationList = conversationList;
        this.context = context;
        fontUtils=FontUtils.getInstance(context);
    }

    /**
     * It is a constructor which is used to initialize wherever we needed.
     *
     * @param context
     */
    public ConversationListAdapter(Context context) {
        this.context = context;
        fontUtils=FontUtils.getInstance(context);

    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        ConversationListRowBinding conversationListRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.conversation_list_row, parent, false);

        return new ConversationViewHolder(conversationListRowBinding);
    }

    /**
     *  This method is used to bind the ConversationViewHolder contents with conversation at given
     *  position. It set avatar, name, lastMessage, unreadMessageCount and messageTime of conversation
     *  in a respective ConversationViewHolder content. It checks whether conversation type is user
     *  or group and set name and avatar as accordingly. It also checks whether last message is text, media
     *  or file and modify txtUserMessage view accordingly.
     *
     * @param conversationViewHolder is a object of ConversationViewHolder.
     * @param position is a position of item in recyclerView.
     *
     * @see Conversation
     */
    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder conversationViewHolder, int position) {
        Conversation conversation = filterConversationList.get(position);

        String avatar;
        String name;
        String lastMessageText = null;
        BaseMessage baseMessage = conversation.getLastMessage();
        conversationViewHolder.conversationListRowBinding.setConversation(conversation);
        conversationViewHolder.conversationListRowBinding.executePendingBindings();

        String type = null;
        String category = null;
        if (baseMessage != null) {
            type = baseMessage.getType();
            category = baseMessage.getCategory();
            setStatusIcon(conversationViewHolder.conversationListRowBinding.messageTime,baseMessage);
            conversationViewHolder.conversationListRowBinding.messageTime.setVisibility(View.VISIBLE);
            conversationViewHolder.conversationListRowBinding.messageTime.setText(Utils.getLastMessageDate(baseMessage.getSentAt()));
            lastMessageText=Utils.getLastMessage(baseMessage);
        } else {
            lastMessageText = context.getResources().getString(R.string.tap_to_start_conversation);
            conversationViewHolder.conversationListRowBinding.txtUserMessage.setMarqueeRepeatLimit(100);
            conversationViewHolder.conversationListRowBinding.txtUserMessage.setHorizontallyScrolling(true);
            conversationViewHolder.conversationListRowBinding.txtUserMessage.setSingleLine(true);
            conversationViewHolder.conversationListRowBinding.messageTime.setVisibility(View.GONE);
        }
        conversationViewHolder.conversationListRowBinding.txtUserMessage.setText(lastMessageText);
        conversationViewHolder.conversationListRowBinding.txtUserMessage.setTypeface(fontUtils.getTypeFace(FontUtils.robotoRegular));
        conversationViewHolder.conversationListRowBinding.txtUserName.setTypeface(fontUtils.getTypeFace(FontUtils.robotoMedium));
        conversationViewHolder.conversationListRowBinding.messageTime.setTypeface(fontUtils.getTypeFace(FontUtils.robotoRegular));

        if (conversation.getConversationType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
            name = ((User) conversation.getConversationWith()).getName();
            avatar = ((User) conversation.getConversationWith()).getAvatar();
        } else {
            name = ((Group) conversation.getConversationWith()).getName();
            avatar = ((Group) conversation.getConversationWith()).getIcon();
        }

        conversationViewHolder.conversationListRowBinding.messageCount.setCount(conversation.getUnreadMessageCount());
        conversationViewHolder.conversationListRowBinding.txtUserName.setText(name);
        conversationViewHolder.conversationListRowBinding.avUser.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));

        if (avatar != null && !avatar.isEmpty()) {
            conversationViewHolder.conversationListRowBinding.avUser.setAvatar(avatar);
        } else {
            conversationViewHolder.conversationListRowBinding.avUser.setInitials(name);
        }

        conversationViewHolder.conversationListRowBinding.getRoot().setTag(R.string.conversation, conversation);

    }

    private void setStatusIcon(TextView txtTime, BaseMessage baseMessage) {
        if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)&&
                baseMessage.getSender().getUid().equals(CometChat.getLoggedInUser().getUid())) {

            if (baseMessage.getReadAt() != 0) {
                txtTime.setText(Utils.getLastMessageDate(baseMessage.getSentAt()));
                txtTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_double_tick, 0, 0, 0);
                txtTime.setCompoundDrawablePadding(10);
            } else if (baseMessage.getDeliveredAt() != 0) {
                txtTime.setText(Utils.getHeaderDate(baseMessage.getSentAt()*1000));
                txtTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_done_all_black_24dp, 0, 0, 0);
                txtTime.setCompoundDrawablePadding(10);
            } else {
                txtTime.setText(Utils.getHeaderDate(baseMessage.getSentAt()*1000));
                txtTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_black_24dp, 0, 0, 0);
                txtTime.setCompoundDrawablePadding(10);
            }
        } else {
            txtTime.setText(Utils.getHeaderDate(baseMessage.getSentAt()));
            txtTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    @Override
    public int getItemCount() {
        return filterConversationList.size();
    }

    /**
     * This method is used to update the filterConversationList with new conversations and avoid
     * duplicates conversations.
     *
     * @param conversations is a list of conversation which will be updated in adapter.
     */
    public void updateList(List<Conversation> conversations) {

        for (int i = 0; i <conversations.size() ; i++) {
           if (filterConversationList.contains(conversations.get(i))){
               int index=filterConversationList.indexOf(conversations.get(i));
               filterConversationList.remove(conversations.get(i));
               filterConversationList.add(index,conversations.get(i));
            }else {
               filterConversationList.add(conversations.get(i));
           }
        }
        notifyDataSetChanged();
    }
    public void setReadReceipts(MessageReceipt readReceipts){
        for (int i =0; i<filterConversationList.size()-1; i++) {
            Conversation conversation = filterConversationList.get(i);
            if (conversation.getConversationType().equals(CometChatConstants.RECEIVER_TYPE_USER)&&
                    readReceipts.getSender().getUid().equals(((User)conversation.getConversationWith()).getUid())) {
                BaseMessage baseMessage = filterConversationList.get(i).getLastMessage();
                if (baseMessage != null && baseMessage.getReadAt() == 0) {
                    baseMessage.setReadAt(readReceipts.getReadAt());
                    int index = filterConversationList.indexOf(filterConversationList.get(i));
                    filterConversationList.remove(index);
                    conversation.setLastMessage(baseMessage);
                    filterConversationList.add(index, conversation);

                }
            }
        }
        notifyDataSetChanged();
    }

    public void setDeliveredReceipts(MessageReceipt deliveryReceipts){
        for (int i =0; i<filterConversationList.size()-1; i++) {
            Conversation conversation = filterConversationList.get(i);
            if (conversation.getConversationType().equals(CometChatConstants.RECEIVER_TYPE_USER)&&
                    deliveryReceipts.getSender().getUid().equals(((User)conversation.getConversationWith()).getUid())) {

                BaseMessage baseMessage = filterConversationList.get(i).getLastMessage();
                if (baseMessage != null && baseMessage.getDeliveredAt() == 0) {
                    baseMessage.setReadAt(deliveryReceipts.getDeliveredAt());
                    int index = filterConversationList.indexOf(filterConversationList.get(i));

                    filterConversationList.remove(index);
                    conversation.setLastMessage(baseMessage);
                    filterConversationList.add(index, conversation);

                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * This method is used to remove the conversation from filterConversationList
     *
     * @param conversation is a object of conversation.
     *
     * @see Conversation
     *
     */
    public void remove(Conversation conversation) {
        int position = filterConversationList.indexOf(conversation);
        filterConversationList.remove(conversation);
        notifyItemRemoved(position);
    }


    /**
     * This method is used to update conversation in filterConversationList.
     *
     * @param conversation is an object of Conversation. It is used to update the previous conversation
     *                     in list
     * @see Conversation
     */
    public void update(Conversation conversation) {

        if (filterConversationList.contains(conversation)) {
            Conversation oldConversation = filterConversationList.get(filterConversationList.indexOf(conversation));
            filterConversationList.remove(oldConversation);
            conversation.setUnreadMessageCount(oldConversation.getUnreadMessageCount() + 1);
            filterConversationList.add(0, conversation);
        } else {
            filterConversationList.add(0, conversation);
        }
        notifyDataSetChanged();

    }

    /**
     * This method is used to add conversation in list.
     *
     * @param conversation is an object of Conversation. It will be added to filterConversationList.
     *
     * @see Conversation
     */
    public void add(Conversation conversation) {
        if (filterConversationList != null)
            filterConversationList.add(conversation);
    }

    /**
     * This method is used to reset the adapter by clearing filterConversationList.
     */
    public void resetAdapterList() {
        filterConversationList.clear();
        notifyDataSetChanged();
    }

    /**
     * It is used to perform search operation in filterConversationList. It will check
     * whether searchKeyword is similar to username or group name and modify filterConversationList
     * accordingly. In case if searchKeyword is empty it will set filterConversationList = conversationList

     * @return
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchKeyword = charSequence.toString();

                if (searchKeyword.isEmpty()) {
                    filterConversationList = conversationList;
                } else {
                    List<Conversation> tempFilter = new ArrayList<>();
                    for (Conversation conversation : filterConversationList) {

                        if (conversation.getConversationType().equals(CometChatConstants.CONVERSATION_TYPE_USER) &&
                                ((User) conversation.getConversationWith()).getName().toLowerCase().contains(searchKeyword)) {

                            tempFilter.add(conversation);
                        } else if (conversation.getConversationType().equals(CometChatConstants.CONVERSATION_TYPE_GROUP) &&
                                ((Group) conversation.getConversationWith()).getName().toLowerCase().contains(searchKeyword)) {
                            tempFilter.add(conversation);
                        } else if (conversation.getLastMessage()!=null && conversation.getLastMessage().getType().equals(CometChatConstants.MESSAGE_TYPE_TEXT)
                                && ((TextMessage)conversation.getLastMessage()).getText().contains(searchKeyword)) {
                            tempFilter.add(conversation);
                        }
                    }
                    filterConversationList=tempFilter;

                }
                FilterResults filterResults=new FilterResults();
                filterResults.values=filterConversationList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
             filterConversationList= (List<Conversation>) filterResults.values;
              notifyDataSetChanged();
            }
        };
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {

        ConversationListRowBinding conversationListRowBinding;

        ConversationViewHolder(ConversationListRowBinding conversationListRowBinding) {
            super(conversationListRowBinding.getRoot());
            this.conversationListRowBinding = conversationListRowBinding;
        }

    }
}
