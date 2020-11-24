package screen;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cometchat.pro.uikit.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import constant.StringContract;
import utils.Extensions;

public class CometChatReactionInfoScreenActivity extends AppCompatActivity {

    private LinearLayout reactionInfoLayout;

    private JSONObject jsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reaction_info_screen);
        reactionInfoLayout = findViewById(R.id.reaction_info_layout);
        if (getIntent().hasExtra(StringContract.IntentStrings.REACTION_INFO)) {
            try {
                jsonObject = new JSONObject(getIntent().getStringExtra(StringContract.IntentStrings.REACTION_INFO));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        HashMap<String, List<String>> reactionInfo = Extensions.getReactionsInfo(jsonObject);
        for (String str : reactionInfo.keySet()) {
            View view = LayoutInflater.from(this).inflate(R.layout.reaction_info_row,null);
            TextView react = view.findViewById(R.id.react_tv);
            TextView users = view.findViewById(R.id.users_tv);
            react.setText(str);
            List<String> usernames = reactionInfo.get(str);
            for (String uname : usernames) {
                if (users.getText().toString().trim().isEmpty())
                    users.setText(uname);
                else
                    users.setText(users.getText().toString()+","+uname);
            }
            reactionInfoLayout.addView(view);
        }
    }
}
