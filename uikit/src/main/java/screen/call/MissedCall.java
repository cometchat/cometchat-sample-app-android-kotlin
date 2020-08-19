package screen.call;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.MessagesRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.CometChatCallList;
import com.cometchat.pro.uikit.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import constant.StringContract;
import listeners.OnItemClickListener;
import screen.CometChatCallActivity;
import screen.CometChatGroupDetailScreenActivity;
import screen.CometChatUserDetailScreenActivity;
import utils.Utils;

/**
 * MissedCall.class is a Fragment class used to display all the call that has been missed by loggedIn
 * user.
 *
 * Created At : 25th March 2020
 *
 * Modified On : 02nd April 2020
 */
public class MissedCall extends Fragment {

    private CometChatCallList rvCallList;

    private LinearLayout noCallView;

    private MessagesRequest messagesRequest;

    private LinearLayoutManager linearLayoutManager;

    private String loggedInUid = CometChat.getLoggedInUser().getUid();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_missed_call, container, false);
        rvCallList = view.findViewById(R.id.callList_rv);
        linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        rvCallList.setLayoutManager(linearLayoutManager);
        noCallView = view.findViewById(R.id.no_call_vw);
        rvCallList.setItemClickListener(new OnItemClickListener<Call>() {
            @Override
            public void OnItemClick(Call var, int position) {
                if (var.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                    User user;
                    if (((User)var.getCallInitiator()).getUid().equals(CometChat.getLoggedInUser().getUid())) {
                        user =  ((User)var.getCallReceiver());
                    }
                    else
                    {
                        user = (User)var.getCallInitiator();
                    }
                    Intent intent = new Intent(getContext(), CometChatUserDetailScreenActivity.class);
                    intent.putExtra(StringContract.IntentStrings.UID, user.getUid());
                    intent.putExtra(StringContract.IntentStrings.NAME, user.getName());
                    intent.putExtra(StringContract.IntentStrings.AVATAR, user.getAvatar());
                    intent.putExtra(StringContract.IntentStrings.STATUS, user.getStatus());
                    intent.putExtra(StringContract.IntentStrings.IS_BLOCKED_BY_ME, user.isBlockedByMe());
                    intent.putExtra(StringContract.IntentStrings.FROM_CALL_LIST,true);
                    startActivity(intent);
                }
                else {
                    Group group;
                    group = ((Group)var.getCallReceiver());
                    Intent intent = new Intent(getContext(), CometChatGroupDetailScreenActivity.class);
                    intent.putExtra(StringContract.IntentStrings.GUID, group.getGuid());
                    intent.putExtra(StringContract.IntentStrings.NAME, group.getName());
                    intent.putExtra(StringContract.IntentStrings.AVATAR, group.getIcon());
                    intent.putExtra(StringContract.IntentStrings.MEMBER_SCOPE, group.getScope());
                    intent.putExtra(StringContract.IntentStrings.GROUP_OWNER, group.getOwner());
                    intent.putExtra(StringContract.IntentStrings.GROUP_TYPE,group.getGroupType());
                    intent.putExtra(StringContract.IntentStrings.MEMBER_COUNT,group.getMembersCount());
                    intent.putExtra(StringContract.IntentStrings.GROUP_PASSWORD,group.getPassword());
                    intent.putExtra(StringContract.IntentStrings.GROUP_DESC,group.getDescription());
                    startActivity(intent);
                }
            }
        });

        rvCallList.setItemCallClickListener(new OnItemClickListener<Call>() {
            @Override
            public void OnItemClick(Call var, int position) {
                checkOnGoingCall(var);
            }
        });
        rvCallList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (!rvCallList.canScrollVertically(1))
                {
                    getCallList();
                }
            }
        });
        return view;
    }

    private void checkOnGoingCall(Call var) {
        if(CometChat.getActiveCall()!=null && CometChat.getActiveCall().getCallStatus().equals(CometChatConstants.CALL_STATUS_ONGOING) && CometChat.getActiveCall().getSessionId()!=null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle(getContext().getResources().getString(R.string.ongoing_call))
                    .setMessage(getContext().getResources().getString(R.string.ongoing_call_message))
                    .setPositiveButton(getContext().getResources().getString(R.string.join), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Utils.joinOnGoingCall(getContext());
                        }
                    }).setNegativeButton(getContext().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        }
        else {
            initiateCall(var);
        }
    }

    private void initiateCall(Call var) {
        CometChat.initiateCall(var, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
                Log.e("onSuccess: ", call.toString());
                if (call.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                    User user;
                    if (((User) call.getCallInitiator()).getUid().equals(CometChat.getLoggedInUser().getUid())) {
                        user = ((User) call.getCallReceiver());
                    } else {
                        user = (User) call.getCallInitiator();
                    }
                    Utils.startCallIntent(getContext(), user, CometChatConstants.CALL_TYPE_AUDIO, true, call.getSessionId());
                } else
                    Utils.startGroupCallIntent(getContext(), ((Group) call.getCallReceiver()), CometChatConstants.CALL_TYPE_AUDIO, true, call.getSessionId());
            }

            @Override
            public void onError(CometChatException e) {
                if (rvCallList != null)
                    Snackbar.make(rvCallList, e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void getCallList() {
        if (messagesRequest == null)
        {
            messagesRequest = new MessagesRequest.MessagesRequestBuilder().setCategory(CometChatConstants.CATEGORY_CALL).build();
        }

        messagesRequest.fetchPrevious(new CometChat.CallbackListener<List<BaseMessage>>() {
            @Override
            public void onSuccess(List<BaseMessage> baseMessages) {
                Collections.reverse(baseMessages);
                rvCallList.setCallList(filteredMissedCall(baseMessages));
                if (rvCallList.size()!=0)
                {
                    noCallView.setVisibility(View.GONE);
                }
                else
                    noCallView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(CometChatException e) {
                if (rvCallList!=null)
                    Snackbar.make(rvCallList,R.string.call_list_error,Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private List<BaseMessage> filteredMissedCall(List<BaseMessage> baseMessages) {
        List<BaseMessage> filteredList = new ArrayList<>();
        for (BaseMessage baseMessage : baseMessages) {
            Call call = (Call)baseMessage;
            if(!((User)call.getCallInitiator()).getUid().equals(loggedInUid)) {
                if (((Call) baseMessage).getCallStatus().equals(CometChatConstants.CALL_STATUS_UNANSWERED)
                        || ((Call) baseMessage).getCallStatus().equals(CometChatConstants.CALL_STATUS_CANCELLED)) {
                    filteredList.add(baseMessage);
                }
            }
        }
        return filteredList;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible)
        {
            getCallList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
