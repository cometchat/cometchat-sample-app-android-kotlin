package screen;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CallSettings;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.R;
import com.google.android.material.snackbar.Snackbar;

import constant.StringContract;

public class CometChatStartCallActivity extends AppCompatActivity {

    public static CometChatStartCallActivity activity;

    private RelativeLayout mainView;

    private String sessionID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comet_chat_start_call);
        mainView = findViewById(R.id.call_view);
        sessionID = getIntent().getStringExtra(StringContract.IntentStrings.SESSION_ID);
        CallSettings callSettings = new CallSettings.CallSettingsBuilder(this,mainView)
                .setSessionId(sessionID)
                .build();
        CometChat.startCall(callSettings, new CometChat.OngoingCallListener() {
            @Override
            public void onUserJoined(User user) {
                Log.e("onUserJoined: ",user.getUid() );
            }

            @Override
            public void onUserLeft(User user) {
                Snackbar.make(mainView,"User Left: "+user.getName(),Snackbar.LENGTH_LONG).show();
                Log.e( "onUserLeft: ",user.getUid() );
            }

            @Override
            public void onError(CometChatException e) {
                Log.e( "onError: ",e.getMessage() );
            }

            @Override
            public void onCallEnded(Call call) {
                Log.e("onCallEnded: ",call.toString() );
                finish();
            }
        });
    }
}