package screen;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.ConversationsRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.helpers.CometChatHelper;
import com.cometchat.pro.models.Action;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.MessageReceipt;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.CometChatConversationList;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.Conversation;
import com.cometchat.pro.models.CustomMessage;
import com.cometchat.pro.models.MediaMessage;
import com.cometchat.pro.models.TextMessage;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

import listeners.OnItemClickListener;
import utils.FontUtils;
import utils.Utils;

/*

* Purpose - CometChatConversationList class is a fragment used to display list of conversations and perform certain action on click of item.
            It also provide search bar to perform search operation on the list of conversations.User can search by username, groupname, last message of conversation.

* Created on - 20th December 2019

* Modified on  - 23rd March 2020

*/

public class CometChatConversationListScreen extends Fragment implements TextWatcher {

    private CometChatConversationList rvConversationList;    //Uses to display list of conversations.

    private ConversationsRequest conversationsRequest;    //Uses to fetch Conversations.

    private static OnItemClickListener events;

    private EditText searchEdit;    //Uses to perform search operations.

    private TextView tvTitle;

    private ShimmerFrameLayout conversationShimmer;

    private RelativeLayout rlSearchBox;

    private LinearLayout noConversationView;

    private static final String TAG = "ConversationList";

    private View view;

    private List<Conversation> conversationList = new ArrayList<>();

    public CometChatConversationListScreen() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         view = inflater.inflate(R.layout.fragment_conversation_screen, container, false);

        rvConversationList = view.findViewById(R.id.rv_conversation_list);

        noConversationView = view.findViewById(R.id.no_conversation_view);

        searchEdit = view.findViewById(R.id.search_bar);

        tvTitle = view.findViewById(R.id.tv_title);

        tvTitle.setTypeface(FontUtils.getInstance(getActivity()).getTypeFace(FontUtils.robotoMedium));

        rlSearchBox = view.findViewById(R.id.rl_search_box);

        conversationShimmer = view.findViewById(R.id.shimmer_layout);

        checkDarkMode();

        searchEdit.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                rvConversationList.searchConversation(textView.getText().toString());
                return true;
            }
            return false;
        });


        // Uses to fetch next list of conversations if rvConversationList (RecyclerView) is scrolled in upward direction.
        rvConversationList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                if (!recyclerView.canScrollVertically(1)) {
                    makeConversationList();
                }

            }
        });

        // Used to trigger event on click of conversation item in rvConversationList (RecyclerView)
        rvConversationList.setItemClickListener(new OnItemClickListener<Conversation>() {
            @Override
            public void OnItemClick(Conversation conversation, int position) {
                if (events!=null)
                    events.OnItemClick(conversation,position);
            }
        });

        return view;
    }

    private void checkDarkMode() {
        if(Utils.isDarkMode(getContext())) {
            tvTitle.setTextColor(getResources().getColor(R.color.textColorWhite));
        } else {
            tvTitle.setTextColor(getResources().getColor(R.color.primaryTextColor));
        }
    }

    /**
     * This method is used to retrieve list of conversations you have done.
     * For more detail please visit our official documentation {@link "https://prodocs.cometchat.com/docs/android-messaging-retrieve-conversations" }
     *
     * @see ConversationsRequest
     */
    private void makeConversationList() {

        if (conversationsRequest == null) {
            conversationsRequest = new ConversationsRequest.ConversationsRequestBuilder().setLimit(50).build();
        }
        conversationsRequest.fetchNext(new CometChat.CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                conversationList.addAll(conversations);
                if (conversationList.size() != 0) {
                    stopHideShimmer();
                    noConversationView.setVisibility(View.GONE);
                    rvConversationList.setConversationList(conversations);
                } else {
                    checkNoConverstaion();
                }
            }

            @Override
            public void onError(CometChatException e) {
                stopHideShimmer();
                if (getActivity()!=null)
                    Toast.makeText(getActivity(),"Unable to load conversations",Toast.LENGTH_LONG).show();
                Log.d(TAG, "onError: "+e.getMessage());
            }
        });
    }

    private void checkNoConverstaion() {
        if (rvConversationList.size()==0) {
            stopHideShimmer();
            noConversationView.setVisibility(View.VISIBLE);
            rvConversationList.setVisibility(View.GONE);
        } else {
            noConversationView.setVisibility(View.GONE);
            rvConversationList.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This method is used to hide shimmer effect if the list is loaded.
     */
    private void stopHideShimmer() {
        conversationShimmer.stopShimmer();
        conversationShimmer.setVisibility(View.GONE);
        tvTitle.setVisibility(View.VISIBLE);
        rlSearchBox.setVisibility(View.VISIBLE);
    }


    /**
     * @param onItemClickListener An object of <code>OnItemClickListener&lt;T&gt;</code> abstract class helps to initialize with events
     *                            to perform onItemClick & onItemLongClick.
     * @see OnItemClickListener
     */
    public static void setItemClickListener(OnItemClickListener<Conversation> onItemClickListener) {
        events = onItemClickListener;
    }

    /**
     * This method has message listener which recieve real time message and based on these messages, conversations are updated.
     *
     * @see CometChat#addMessageListener(String, CometChat.MessageListener)
     */
    private void addConversationListener() {
        CometChat.addMessageListener(TAG, new CometChat.MessageListener() {
            @Override
            public void onTextMessageReceived(TextMessage message) {
                if (rvConversationList!=null) {
                    rvConversationList.refreshConversation(message);
                    checkNoConverstaion();
                }
            }

            @Override
            public void onMediaMessageReceived(MediaMessage message) {
                if (rvConversationList != null) {
                    rvConversationList.refreshConversation(message);
                    checkNoConverstaion();
                }
            }

            @Override
            public void onCustomMessageReceived(CustomMessage message) {
                if (rvConversationList != null) {
                    rvConversationList.refreshConversation(message);
                    checkNoConverstaion();
                }
            }

            @Override
            public void onMessagesDelivered(MessageReceipt messageReceipt) {
                if (rvConversationList!=null)
                   rvConversationList.setReciept(messageReceipt);
            }

            @Override
            public void onMessagesRead(MessageReceipt messageReceipt) {
                if (rvConversationList!=null)
                    rvConversationList.setReciept(messageReceipt);
            }

            @Override
            public void onMessageEdited(BaseMessage message) {
                if (rvConversationList!=null)
                    rvConversationList.refreshConversation(message);
            }

            @Override
            public void onMessageDeleted(BaseMessage message) {
                if (rvConversationList!=null)
                    rvConversationList.refreshConversation(message);
            }
        });
        CometChat.addGroupListener(TAG, new CometChat.GroupListener() {
            @Override
            public void onGroupMemberKicked(Action action, User kickedUser, User kickedBy, Group kickedFrom) {
                Log.e(TAG, "onGroupMemberKicked: "+kickedUser);
                if (kickedUser.getUid().equals(CometChat.getLoggedInUser().getUid())) {
                    if (rvConversationList!=null)
                        updateConversation(action,true);
                }
                else {
                    updateConversation(action,false);
                }
            }

            @Override
            public void onMemberAddedToGroup(Action action, User addedby, User userAdded, Group addedTo) {
                Log.e(TAG, "onMemberAddedToGroup: " );
                updateConversation(action,false);
            }

            @Override
            public void onGroupMemberJoined(Action action, User joinedUser, Group joinedGroup) {
                Log.e(TAG, "onGroupMemberJoined: " );
                updateConversation(action,false);
            }

            @Override
            public void onGroupMemberLeft(Action action, User leftUser, Group leftGroup) {
                Log.e(TAG, "onGroupMemberLeft: " );
                if (leftUser.getUid().equals(CometChat.getLoggedInUser().getUid())) {
                    updateConversation(action,true);
                }
                else {
                   updateConversation(action,false);
                }
            }

            @Override
            public void onGroupMemberScopeChanged(Action action, User updatedBy, User updatedUser, String scopeChangedTo, String scopeChangedFrom, Group group) {
                updateConversation(action,false);
            }
        });
    }

    /**
     * This method is used to update conversation received in real-time.
     * @param baseMessage is object of BaseMessage.class used to get respective Conversation.
     * @param isRemove is boolean used to check whether conversation needs to be removed or not.
     *
     * @see CometChatHelper#getConversationFromMessage(BaseMessage) This method return the conversation
     * of receiver using baseMessage.
     *
     */
    private void updateConversation(BaseMessage baseMessage,boolean isRemove) {
        if (rvConversationList != null) {
            Conversation conversation = CometChatHelper.getConversationFromMessage(baseMessage);
            if (isRemove)
                rvConversationList.remove(conversation);
            else
                rvConversationList.update(conversation);
            checkNoConverstaion();
        }
    }

    /**
     * This method is used to remove the conversationlistener.
     */
    private void removeConversationListener() {
        CometChat.removeMessageListener(TAG);
        CometChat.removeGroupListener(TAG);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        conversationsRequest = null;
        searchEdit.addTextChangedListener(this);
//        rvConversationList.clearList();
        makeConversationList();
        addConversationListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        searchEdit.removeTextChangedListener(this);
        removeConversationListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() == 0) {
//                    // if searchEdit is empty then fetch all conversations.
            conversationsRequest = null;
            rvConversationList.clearList();
            makeConversationList();
        } else {
//                    // Search conversation based on text in searchEdit field.
            rvConversationList.searchConversation(s.toString());
        }
    }
}
