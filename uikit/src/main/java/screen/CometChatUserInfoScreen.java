package screen;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.uikit.databinding.FragmentMoreInfoScreenBinding;
import com.cometchat.pro.uikit.Avatar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;

import constant.StringContract;
import utils.FontUtils;
import utils.MediaUtils;
import utils.PreferenceUtil;
import utils.Utils;

public class CometChatUserInfoScreen extends Fragment {

    private Avatar notificationIv;
    private AlertDialog.Builder dialog;
    FragmentMoreInfoScreenBinding moreInfoScreenBinding;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        moreInfoScreenBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_more_info_screen, container, false);
        moreInfoScreenBinding.setUser(CometChat.getLoggedInUser());
        moreInfoScreenBinding.ivUser.setAvatar(CometChat.getLoggedInUser());

        moreInfoScreenBinding.tvTitle.setTypeface(FontUtils.getInstance(getActivity()).getTypeFace(FontUtils.robotoMedium));
        Log.e("onCreateView: ", CometChat.getLoggedInUser().toString());
        moreInfoScreenBinding.privacyAndSecurity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), CometChatMorePrivacyScreenActivity.class));
            }
        });

        if(Utils.isDarkMode(getContext())) {
            moreInfoScreenBinding.tvTitle.setTextColor(getResources().getColor(R.color.textColorWhite));
            moreInfoScreenBinding.tvSeperator.setBackgroundColor(getResources().getColor(R.color.grey));
            moreInfoScreenBinding.tvSeperator1.setBackgroundColor(getResources().getColor(R.color.grey));
        } else {
            moreInfoScreenBinding.tvTitle.setTextColor(getResources().getColor(R.color.primaryTextColor));
            moreInfoScreenBinding.tvSeperator.setBackgroundColor(getResources().getColor(R.color.light_grey));
            moreInfoScreenBinding.tvSeperator1.setBackgroundColor(getResources().getColor(R.color.light_grey));
        }

        moreInfoScreenBinding.userContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserDialog();
            }
        });
        return moreInfoScreenBinding.getRoot();
    }

    private void updateUserDialog() {
        dialog = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.update_user,null);
        Avatar avatar = view.findViewById(R.id.user_avatar);
        avatar.setAvatar(CometChat.getLoggedInUser());
        TextInputEditText avatar_url = view.findViewById(R.id.avatar_url_edt);
        avatar_url.setText(CometChat.getLoggedInUser().getAvatar());
        TextInputEditText username = view.findViewById(R.id.username_edt);
        username.setText(CometChat.getLoggedInUser().getName());
        MaterialButton updateUserBtn = view.findViewById(R.id.updateUserBtn);
        MaterialButton cancelBtn = view.findViewById(R.id.cancelBtn);

        if(CometChat.getLoggedInUser().getAvatar()==null) {
            avatar.setVisibility(View.GONE);
            avatar_url.setVisibility(View.GONE);
        }
        else {
            avatar.setVisibility(View.VISIBLE);
            avatar_url.setVisibility(View.GONE);
        }
        avatar_url.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty())
                {
                    avatar.setVisibility(View.VISIBLE);
                    Glide.with(getContext()).load(s.toString()).into(avatar);
                } else
                    avatar.setVisibility(View.GONE);
            }
        });
        AlertDialog alertDialog = dialog.create();
        alertDialog.setView(view);
        updateUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = new User();
                if (username.getText().toString().isEmpty())
                    username.setError(getString(R.string.fill_this_field));
                else {
                    user.setName(username.getText().toString());
                    user.setUid(CometChat.getLoggedInUser().getUid());
                    user.setAvatar(avatar_url.getText().toString());
                    updateUser(user);
                    alertDialog.dismiss();
                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void updateUser(User user) {
        CometChat.updateUser(user, StringContract.AppInfo.API_KEY, new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (getContext()!=null)
                    Toast.makeText(getContext(),"Updated User Successfull",Toast.LENGTH_LONG).show();
                moreInfoScreenBinding.setUser(user);
            }

            @Override
            public void onError(CometChatException e) {
                if (getContext()!=null)
                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }
}
