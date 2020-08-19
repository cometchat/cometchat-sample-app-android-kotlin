package screen.groupmemberlist;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.cometchat.pro.uikit.R;

import constant.StringContract;

public class CometChatGroupMemberListScreenActivity extends AppCompatActivity {

    private String guid;

    private boolean showModerators;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_screen);
        guid = getIntent().getStringExtra(StringContract.IntentStrings.GUID);
        showModerators = getIntent().getBooleanExtra(StringContract.IntentStrings.SHOW_MODERATORLIST,showModerators);
        Fragment fragment = new CometChatGroupMemberListScreen();
        Bundle bundle = new Bundle();
        bundle.putString(StringContract.IntentStrings.GUID,guid);
        bundle.putBoolean(StringContract.IntentStrings.SHOW_MODERATORLIST,showModerators);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_fragment,fragment).commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

           if(item.getItemId()==android.R.id.home){
               onBackPressed();
           }

        return super.onOptionsItemSelected(item);
    }


}
