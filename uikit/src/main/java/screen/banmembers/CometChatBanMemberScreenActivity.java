package screen.banmembers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.BannedGroupMembersRequest;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.GroupMember;
import com.cometchat.pro.uikit.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import adapter.GroupMemberAdapter;
import constant.StringContract;
import listeners.ClickListener;
import listeners.RecyclerTouchListener;

public class CometChatBanMemberScreenActivity extends AppCompatActivity {

    private String guid,gName;
    private String loggedInUserScope;
    private MaterialToolbar banToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comet_chat_ban_member_screen);
        banToolbar = findViewById(R.id.banToolbar);
        banToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        handleIntent();
        CometChatBanMemberScreen banFragment = new CometChatBanMemberScreen();
        Bundle bundle = new Bundle();
        bundle.putString(StringContract.IntentStrings.GUID,guid);
        bundle.putString(StringContract.IntentStrings.GROUP_NAME,gName);
        bundle.putString(StringContract.IntentStrings.MEMBER_SCOPE,loggedInUserScope);
        banFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.ban_member_frame,banFragment).commit();
    }

    public void handleIntent() {
        if (getIntent().hasExtra(StringContract.IntentStrings.GUID)) {
            guid = getIntent().getStringExtra(StringContract.IntentStrings.GUID);
        }
        if (getIntent().hasExtra(StringContract.IntentStrings.GROUP_NAME)) {
            gName = getIntent().getStringExtra(StringContract.IntentStrings.GROUP_NAME);
            banToolbar.setTitle(String.format(getResources().getString(R.string.ban_member_of_group),gName));
        }
        if (getIntent().hasExtra(StringContract.IntentStrings.MEMBER_SCOPE)) {
            loggedInUserScope = getIntent().getStringExtra(StringContract.IntentStrings.MEMBER_SCOPE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
