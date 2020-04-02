package screen.adminlist;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.cometchat.pro.uikit.R;

import constant.StringContract;

public class CometChatAdminListScreenActivity extends AppCompatActivity {


    private String guid;

    private String ownerId;

    private String loggedInUserScope;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_screen);
        handleIntent();
    }

    private void handleIntent() {
        if (getIntent().hasExtra(StringContract.IntentStrings.MEMBER_SCOPE)){
            loggedInUserScope=getIntent().getStringExtra(StringContract.IntentStrings.MEMBER_SCOPE);
        }
        if (getIntent().hasExtra(StringContract.IntentStrings.GUID)) {
            guid = getIntent().getStringExtra(StringContract.IntentStrings.GUID);
        }
        if (getIntent().hasExtra(StringContract.IntentStrings.GROUP_OWNER)) {
            ownerId = getIntent().getStringExtra(StringContract.IntentStrings.GROUP_OWNER);
        }
        Fragment fragment = new CometChatAdminListScreen();
        Bundle bundle = new Bundle();
        bundle.putString(StringContract.IntentStrings.GUID,guid);
        bundle.putString(StringContract.IntentStrings.GROUP_OWNER,ownerId);
        bundle.putString(StringContract.IntentStrings.MEMBER_SCOPE,loggedInUserScope);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_fragment,fragment).commit();

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
