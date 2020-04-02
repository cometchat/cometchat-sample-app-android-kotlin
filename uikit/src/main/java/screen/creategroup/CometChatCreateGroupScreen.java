package screen.creategroup;

import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.uikit.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.security.SecureRandom;
import java.util.Random;

import constant.StringContract;
import screen.messagelist.CometChatMessageListActivity;

/**
 * Purpose - CometChatCreateGroup class is a fragment used to create a group. User just need to enter
 * group name. All other information like guid, groupIcon are set by this class.
 *
 * @see CometChat#createGroup(Group, CometChat.CallbackListener)
 *
 *
 */


public class CometChatCreateGroupScreen extends Fragment {

    private TextInputEditText etGroupName;

    String TAG = "CometChatCreateGroup";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_comet_chat_create_group_screen, container, false);

        etGroupName = view.findViewById(R.id.group_name);

        MaterialButton createGroupBtn = view.findViewById(R.id.btn_create_group);

        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });
        return view;
    }

    /**
     * This method is used to create group when called from layout. It uses <code>Random.nextInt()</code>
     * to generate random number to use with group id and group icon. Any Random number between 10 to
     * 1000 are choosen.
     *
     */

    public static String generateRandomString(int length) {
        if (length < 1) throw new IllegalArgumentException();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // 0-62 (exclusive), random returns 0-61
            SecureRandom random = new SecureRandom();
            String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
            String CHAR_UPPER = CHAR_LOWER.toUpperCase();
            String NUMBER = "0123456789";
            String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);
            // debug
            System.out.format("%d\t:\t%c%n", rndCharAt, rndChar);
            sb.append(rndChar);
        }
        return sb.toString();
    }

    private void createGroup() {
        if (etGroupName.getText()!=null&&!etGroupName.getText().toString().isEmpty()) {
            Group group = new Group("group"+generateRandomString(95), etGroupName.getText().toString(), CometChatConstants.GROUP_TYPE_PUBLIC,"");
            CometChat.createGroup(group, new CometChat.CallbackListener<Group>() {
                @Override
                public void onSuccess(Group group) {
                    Intent intent = new Intent(getActivity(), CometChatMessageListActivity.class);
                    intent.putExtra(StringContract.IntentStrings.NAME,group.getName());
                    intent.putExtra(StringContract.IntentStrings.GROUP_OWNER,group.getOwner());
                    intent.putExtra(StringContract.IntentStrings.GUID,group.getGuid());
                    intent.putExtra(StringContract.IntentStrings.AVATAR,group.getIcon());
                    intent.putExtra(StringContract.IntentStrings.TYPE,CometChatConstants.RECEIVER_TYPE_GROUP);
                    if (getActivity()!=null)
                    getActivity().finish();

                    startActivity(intent);
                }

                @Override
                public void onError(CometChatException e) {
                    Snackbar.make(etGroupName.getRootView(),getResources().getString(R.string.create_group_error),Snackbar.LENGTH_LONG).show();
                    Log.e(TAG, "onError: "+e.getMessage() );
                }
            });
        }
        else {
            etGroupName.setError(getResources().getString(R.string.fill_this_field));
        }
    }

}
