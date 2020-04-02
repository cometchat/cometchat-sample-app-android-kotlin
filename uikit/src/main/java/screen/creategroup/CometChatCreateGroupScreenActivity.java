package screen.creategroup;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.cometchat.pro.uikit.R;

public class CometChatCreateGroupScreenActivity extends AppCompatActivity {

    private Fragment fragment;

    private String guid;

    private String loggedInUserScope;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_screen);
        Fragment fragment = new CometChatCreateGroupScreen();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_fragment,fragment).commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


}
