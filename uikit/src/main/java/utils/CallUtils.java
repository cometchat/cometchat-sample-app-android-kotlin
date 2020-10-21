package utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.R;
import com.google.android.material.snackbar.Snackbar;

import constant.StringContract;
import screen.CometChatCallActivity;
import screen.CometChatStartCallActivity;

/**
 * Purpose - This class contains all the static methods which are useful for hanlde calling in UIKit.
 * Developers can use these method to intiate,join or start call.
 *
 * Created On - 7th October 2020
 *
 * Modified On - 7th Ocotber 2020
 *
 */
public class CallUtils {

    private static final String TAG = "CallUtils";

    /**
     * This method is used to initiate a call for user or a group.
     * @param context is a object of Context
     * @param recieverID is String - It can be either uid or guid
     * @param receiverType is String - It can be either user or group
     *                     [<code>CometChatConstants.RECEIVER_TYPE_USER</code> or
     *                     <code>CometChatConstants.RECEIVER_TYPE_GROUP</code>]
     * @param callType is String -  It can be either audio or video
     *                 [<code>CometChatConstants.CALL_TYPE_AUDIO</code> or
     *                 <code>CometChatConstants.CALL_TYPE_VIDEO</code>]
     *
     * @see CometChat#initiateCall(Call, CometChat.CallbackListener)
     */
    public static void initiateCall(Context context, String recieverID, String receiverType, String callType)
    {
        Call call = new Call(recieverID,receiverType,callType);
        CometChat.initiateCall(call, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
                if (receiverType.equalsIgnoreCase(CometChatConstants.RECEIVER_TYPE_USER))
                    startCallIntent(context,((User)call.getCallReceiver()),call.getType(),true,call.getSessionId());
                else if (receiverType.equalsIgnoreCase(CometChatConstants.RECEIVER_TYPE_GROUP))
                    startGroupCallIntent(context,((Group)call.getCallReceiver()),call.getType(),true,call.getSessionId());
            }

            @Override
            public void onError(CometChatException e) {
                Log.e(TAG, "onError: "+e.getMessage());
                Snackbar.make(((Activity)context).getWindow().getDecorView().getRootView(),context.getResources().getString(R.string.call_initiate_error)+":"+e.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /**
     * This method is used to load a <code>CometChatCallActivity</code> for a user call. Based on
     * parameter passed in intent it will load either incoming or outgoing call screen.
     * @param context is a object of Context
     * @param user is object of User
     * @param type is a String. It is callType which can be either audio or video
     * @param isOutgoing is a boolean which helps to identify whether call is incoming or outgoing
     * @param sessionId is a String. It is unique session of a call.
     */
    public static void startCallIntent(Context context, User user, String type,
                                       boolean isOutgoing, @NonNull String sessionId) {
        Intent videoCallIntent = new Intent(context, CometChatCallActivity.class);
        videoCallIntent.putExtra(StringContract.IntentStrings.NAME, user.getName());
        videoCallIntent.putExtra(StringContract.IntentStrings.UID,user.getUid());
        videoCallIntent.putExtra(StringContract.IntentStrings.SESSION_ID,sessionId);
        videoCallIntent.putExtra(StringContract.IntentStrings.AVATAR, user.getAvatar());
        videoCallIntent.setAction(type);
        videoCallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isOutgoing) {
            videoCallIntent.setType("outgoing");
        }
        else {
            videoCallIntent.setType("incoming");
        }
        context.startActivity(videoCallIntent);
    }

    /**
     * This method is used to load a <code>CometChatCallActivity</code> for a user call. Based on
     * parameter passed in intent it will load either incoming or outgoing call screen.
     * @param context is a object of Context
     * @param group is object of Group
     * @param type is a String. It is callType which can be either audio or video
     * @param isOutgoing is a boolean which helps to identify whether call is incoming or outgoing
     * @param sessionId is a String. It is unique session of a call.
     */
    public static void startGroupCallIntent(Context context, Group group, String type,
                                            boolean isOutgoing, @NonNull String sessionId) {
        Intent videoCallIntent = new Intent(context, CometChatCallActivity.class);
        videoCallIntent.putExtra(StringContract.IntentStrings.NAME, group.getName());
        videoCallIntent.putExtra(StringContract.IntentStrings.UID,group.getGuid());
        videoCallIntent.putExtra(StringContract.IntentStrings.SESSION_ID,sessionId);
        videoCallIntent.putExtra(StringContract.IntentStrings.AVATAR, group.getIcon());
        videoCallIntent.setAction(type);
        videoCallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (isOutgoing) {
            videoCallIntent.setType("outgoing");
        }
        else {
            videoCallIntent.setType("incoming");
        }
        context.startActivity(videoCallIntent);
    }

    /**
     * This method is used to load <code>CometChatStartCallActivity</code> which starts a call in
     * seperate screen
     * @param context is object of Context
     * @param call is object of Call
     */
    public static void startCall(Context context, Call call) {
        Intent intent = new Intent(context, CometChatStartCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(StringContract.IntentStrings.SESSION_ID,call.getSessionId());
        ((Activity)context).finish();
        context.startActivity(intent);
    }

    /**
     * This method is used to join an ongoing call.
     * @param context
     */
    public static void joinOnGoingCall(Context context) {
        Intent intent = new Intent(context,CometChatCallActivity.class);
        intent.putExtra(StringContract.IntentStrings.JOIN_ONGOING,true);
        context.startActivity(intent);
    }

}
