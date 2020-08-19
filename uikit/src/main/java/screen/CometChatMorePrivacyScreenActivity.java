package screen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cometchat.pro.core.BlockedUsersRequest;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import screen.blockuserlist.CometChatBlockUserListScreenActivity;
import utils.FontUtils;
import utils.Utils;

public class CometChatMorePrivacyScreenActivity extends AppCompatActivity {

    private TextView tvBlockUserCount;

    private BlockedUsersRequest blockedUsersRequest;

    private TextView blockUserTv;

    private View divider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comet_chat_more_privacy_screen);

        blockUserTv = findViewById(R.id.blocked_user_tv);
        tvBlockUserCount = findViewById(R.id.tv_blocked_user_count);
        MaterialToolbar toolbar = findViewById(R.id.privacy_toolbar);
        divider = findViewById(R.id.divider);
        setSupportActionBar(toolbar);

         if (getSupportActionBar()!=null)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

         if (Utils.changeToolbarFont(toolbar)!=null){
             Utils.changeToolbarFont(toolbar).setTypeface(FontUtils.getInstance(this).getTypeFace(FontUtils.robotoMedium));
         }
         if(Utils.isDarkMode(this)) {
             divider.setBackgroundColor(getResources().getColor(R.color.grey));
             blockUserTv.setTextColor(getResources().getColor(R.color.textColorWhite));
         } else {
             divider.setBackgroundColor(getResources().getColor(R.color.light_grey));
             blockUserTv.setTextColor(getResources().getColor(R.color.primaryTextColor));
         }
         getBlockCount();
    }

    public void blockUserList(View view) {
        startActivity(new Intent(this, CometChatBlockUserListScreenActivity.class));
    }

    public void getBlockCount() {

         blockedUsersRequest = new BlockedUsersRequest.BlockedUsersRequestBuilder().setDirection(BlockedUsersRequest.DIRECTION_BLOCKED_BY_ME).setLimit(100).build();
         blockedUsersRequest.fetchNext(new CometChat.CallbackListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {

                if (users.size() == 0) {
                    tvBlockUserCount.setText("");
                } else if (users.size() < 2) {
                    tvBlockUserCount.setText(users.size() +" "+getResources().getString(R.string.user));
                } else {
                    tvBlockUserCount.setText(users.size() + " "+getResources().getString(R.string.users));
                }

            }

            @Override
            public void onError(CometChatException e) {
                Snackbar.make(tvBlockUserCount,getResources().getString(R.string.blocked_list_error),Snackbar.LENGTH_SHORT).show();
                Toast.makeText(CometChatMorePrivacyScreenActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        blockedUsersRequest=null;
        getBlockCount();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

         if (item.getItemId()==android.R.id.home)
             onBackPressed();

        return super.onOptionsItemSelected(item);
    }
}
