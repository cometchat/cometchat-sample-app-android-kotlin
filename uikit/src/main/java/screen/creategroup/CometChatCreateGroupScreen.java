package screen.creategroup;

import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.uikit.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.security.SecureRandom;
import java.util.Random;

import constant.StringContract;
import screen.messagelist.CometChatMessageListActivity;
import utils.Utils;

/**
 * Purpose - CometChatCreateGroup class is a fragment used to create a group. User just need to enter
 * group name. All other information like guid, groupIcon are set by this class.
 *
 * @see CometChat#createGroup(Group, CometChat.CallbackListener)
 *
 *
 */


public class CometChatCreateGroupScreen extends Fragment {

    private TextInputEditText etGroupName,etGroupDesc,etGroupPassword,etGroupCnfPassword;

    private TextView des1;

    private TextView des2;

    private TextInputLayout groupNameLayout,groupDescLayout,groupPasswordLayout,groupCnfPasswordLayout;

    private MaterialButton createGroupBtn;

    private Spinner groupTypeSpinner;

    private String groupType;

    String TAG = "CometChatCreateGroup";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_comet_chat_create_group_screen, container, false);

        etGroupName = view.findViewById(R.id.group_name);
        etGroupDesc = view.findViewById(R.id.group_desc);
        etGroupPassword = view.findViewById(R.id.group_pwd);
        etGroupCnfPassword = view.findViewById(R.id.group_cnf_pwd);
        etGroupCnfPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!etGroupPassword.getText().toString().isEmpty() && s.toString().equals(etGroupPassword.getText().toString())) {
                    groupCnfPasswordLayout.setEndIconDrawable(getResources().getDrawable(R.drawable.ic_check_black_24dp));
                    groupCnfPasswordLayout.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.green_600)));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        des1 = view.findViewById(R.id.tvDes1);

        des2 = view.findViewById(R.id.tvDes2);

        groupNameLayout = view.findViewById(R.id.input_group_name);
        groupDescLayout = view.findViewById(R.id.input_group_desc);
        groupPasswordLayout = view.findViewById(R.id.input_group_pwd);
        groupCnfPasswordLayout = view.findViewById(R.id.input_group_cnf_pwd);
        groupTypeSpinner = view.findViewById(R.id.grouptype_spinner);
        groupTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position==0) {
                    groupType = CometChatConstants.GROUP_TYPE_PUBLIC;
                    groupPasswordLayout.setVisibility(View.GONE);
                    groupCnfPasswordLayout.setVisibility(View.GONE);
                } else if (position==1) {
                    groupType = CometChatConstants.GROUP_TYPE_PRIVATE;
                    groupPasswordLayout.setVisibility(View.GONE);
                    groupCnfPasswordLayout.setVisibility(View.GONE);
                } else if (position==2) {
                    groupType = CometChatConstants.GROUP_TYPE_PASSWORD;
                    groupPasswordLayout.setVisibility(View.VISIBLE);
                    groupCnfPasswordLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        createGroupBtn = view.findViewById(R.id.btn_create_group);

        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });
        checkDarkMode();
        return view;
    }

    private void checkDarkMode() {
        if (Utils.isDarkMode(getContext())) {
            des1.setTextColor(getResources().getColor(R.color.textColorWhite));
            des2.setTextColor(getResources().getColor(R.color.textColorWhite));
            groupNameLayout.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            groupNameLayout.setBoxStrokeColor(getResources().getColor(R.color.textColorWhite));
            groupNameLayout.setDefaultHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            etGroupName.setTextColor(getResources().getColor(R.color.textColorWhite));

            groupDescLayout.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            groupDescLayout.setBoxStrokeColor(getResources().getColor(R.color.textColorWhite));
            groupDescLayout.setDefaultHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            etGroupDesc.setTextColor(getResources().getColor(R.color.textColorWhite));

            groupPasswordLayout.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            groupPasswordLayout.setBoxStrokeColor(getResources().getColor(R.color.textColorWhite));
            groupPasswordLayout.setDefaultHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            etGroupPassword.setTextColor(getResources().getColor(R.color.textColorWhite));

            groupCnfPasswordLayout.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            groupCnfPasswordLayout.setBoxStrokeColor(getResources().getColor(R.color.textColorWhite));
            groupCnfPasswordLayout.setDefaultHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            etGroupCnfPassword.setTextColor(getResources().getColor(R.color.textColorWhite));

        } else {
            des1.setTextColor(getResources().getColor(R.color.primaryTextColor));
            des2.setTextColor(getResources().getColor(R.color.primaryTextColor));
            groupNameLayout.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.primaryTextColor)));
            groupNameLayout.setBoxStrokeColor(getResources().getColor(R.color.primaryTextColor));
            etGroupName.setTextColor(getResources().getColor(R.color.primaryTextColor));

            groupDescLayout.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.primaryTextColor)));
            groupDescLayout.setBoxStrokeColor(getResources().getColor(R.color.primaryTextColor));
            etGroupDesc.setTextColor(getResources().getColor(R.color.primaryTextColor));

            groupPasswordLayout.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.primaryTextColor)));
            groupPasswordLayout.setBoxStrokeColor(getResources().getColor(R.color.primaryTextColor));
            etGroupPassword.setTextColor(getResources().getColor(R.color.primaryTextColor));

            groupCnfPasswordLayout.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.primaryTextColor)));
            groupCnfPasswordLayout.setBoxStrokeColor(getResources().getColor(R.color.primaryTextColor));
            etGroupCnfPassword.setTextColor(getResources().getColor(R.color.primaryTextColor));
        }
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
        if (!etGroupName.getText().toString().isEmpty()) {
            if(groupType.equals(CometChatConstants.GROUP_TYPE_PUBLIC) || groupType.equals(CometChatConstants.GROUP_TYPE_PRIVATE)) {
                Group group = new Group("group" + generateRandomString(95), etGroupName.getText().toString(), groupType, "");
                createGroup(group);
            }
            else if (groupType.equals(CometChatConstants.GROUP_TYPE_PASSWORD)) {
                if(etGroupPassword.getText().toString().isEmpty())
                    etGroupPassword.setError(getResources().getString(R.string.fill_this_field));
                else if (etGroupCnfPassword.getText().toString().isEmpty())
                    etGroupCnfPassword.setError(getResources().getString(R.string.fill_this_field));
                else if(etGroupPassword.getText().toString().equals(etGroupCnfPassword.getText().toString())) {
                    Group group = new Group("group" + generateRandomString(95), etGroupName.getText().toString(), groupType, etGroupPassword.getText().toString());
                    createGroup(group);
                }
                else
                    if (etGroupPassword!=null)
                        Snackbar.make(etGroupCnfPassword.getRootView(),getResources().getString(R.string.password_not_matched),Snackbar.LENGTH_LONG).show();
            }
        }
        else {
            etGroupName.setError(getResources().getString(R.string.fill_this_field));
        }
    }

    private void createGroup(Group group) {
        CometChat.createGroup(group, new CometChat.CallbackListener<Group>() {
            @Override
            public void onSuccess(Group group) {
                Intent intent = new Intent(getActivity(), CometChatMessageListActivity.class);
                intent.putExtra(StringContract.IntentStrings.NAME,group.getName());
                intent.putExtra(StringContract.IntentStrings.GROUP_OWNER,group.getOwner());
                intent.putExtra(StringContract.IntentStrings.GUID,group.getGuid());
                intent.putExtra(StringContract.IntentStrings.AVATAR,group.getIcon());
                intent.putExtra(StringContract.IntentStrings.GROUP_TYPE,group.getGroupType());
                intent.putExtra(StringContract.IntentStrings.TYPE,CometChatConstants.RECEIVER_TYPE_GROUP);
                intent.putExtra(StringContract.IntentStrings.MEMBER_COUNT,group.getMembersCount());
                intent.putExtra(StringContract.IntentStrings.GROUP_DESC,group.getDescription());
                intent.putExtra(StringContract.IntentStrings.GROUP_PASSWORD,group.getPassword());
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
}
