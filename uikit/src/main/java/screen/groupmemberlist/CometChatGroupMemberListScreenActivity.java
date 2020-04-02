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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_screen);
        guid = getIntent().getStringExtra(StringContract.IntentStrings.GUID);
        Fragment fragment = new CometChatGroupMemberListScreen();
        Bundle bundle = new Bundle();
        bundle.putString(StringContract.IntentStrings.GUID,guid);
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
