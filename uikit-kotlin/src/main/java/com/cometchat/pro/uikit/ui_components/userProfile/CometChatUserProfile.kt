package com.cometchat.pro.uikit.ui_components.userProfile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.ui_components.shared.cometchatAvatar.CometChatAvatar
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.databinding.FragmentCometchatUserProfileBinding
import com.cometchat.pro.uikit.ui_components.userProfile.privacy_and_security.CometChatMorePrivacyActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.cometchat.pro.uikit.ui_resources.constants.UIKitContracts
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils

class CometChatUserProfile constructor() : Fragment() {
    private val notificationIv: CometChatAvatar? = null
    private var dialog: AlertDialog.Builder? = null
    var moreInfoScreenBinding: FragmentCometchatUserProfileBinding? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                     savedInstanceState: Bundle?): View? {
        moreInfoScreenBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_cometchat_user_profile, container, false)
        moreInfoScreenBinding!!.setUser(CometChat.getLoggedInUser())
        moreInfoScreenBinding!!.ivUser.setAvatar(CometChat.getLoggedInUser())
        moreInfoScreenBinding!!.tvTitle.setTypeface(FontUtils.getInstance(getActivity()).getTypeFace(FontUtils.robotoMedium))
        Log.e("onCreateView: ", CometChat.getLoggedInUser().toString())
        moreInfoScreenBinding!!.privacyAndSecurity.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(view: View) {
                startActivity(Intent(getContext(), CometChatMorePrivacyActivity::class.java))
            }
        })
        if (Utils.isDarkMode(getContext()!!)) {
            moreInfoScreenBinding!!.tvTitle.setTextColor(getResources().getColor(R.color.textColorWhite))
            moreInfoScreenBinding!!.tvSeperator.setBackgroundColor(getResources().getColor(R.color.grey))
            moreInfoScreenBinding!!.tvSeperator1.setBackgroundColor(getResources().getColor(R.color.grey))
        } else {
            moreInfoScreenBinding!!.tvTitle.setTextColor(getResources().getColor(R.color.primaryTextColor))
            moreInfoScreenBinding!!.tvSeperator.setBackgroundColor(getResources().getColor(R.color.light_grey))
            moreInfoScreenBinding!!.tvSeperator1.setBackgroundColor(getResources().getColor(R.color.light_grey))
        }
        moreInfoScreenBinding!!.userContainer.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                updateUserDialog()
            }
        })
        return moreInfoScreenBinding!!.getRoot()
    }

    private fun updateUserDialog() {
        dialog = AlertDialog.Builder(getContext())
        val view: View = LayoutInflater.from(getContext()).inflate(R.layout.cometchat_update_user_dialog, null)
        val avatar: CometChatAvatar = view.findViewById(R.id.user_avatar)
        avatar.setAvatar(CometChat.getLoggedInUser())
        val avatar_url: TextInputEditText = view.findViewById(R.id.avatar_url_edt)
        avatar_url.setText(CometChat.getLoggedInUser().getAvatar())
        val username: TextInputEditText = view.findViewById(R.id.username_edt)
        username.setText(CometChat.getLoggedInUser().getName())
        val updateUserBtn: MaterialButton = view.findViewById(R.id.updateUserBtn)
        val cancelBtn: MaterialButton = view.findViewById(R.id.cancelBtn)
        if (CometChat.getLoggedInUser().getAvatar() == null) {
            avatar.setVisibility(View.GONE)
            avatar_url.setVisibility(View.GONE)
        } else {
            avatar.setVisibility(View.VISIBLE)
            avatar_url.setVisibility(View.GONE)
        }
        avatar_url.addTextChangedListener(object : TextWatcher {
            public override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            public override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            public override fun afterTextChanged(s: Editable) {
                if (!s.toString().isEmpty()) {
                    avatar.setVisibility(View.VISIBLE)
                    Glide.with((getContext())!!).load(s.toString()).into(avatar)
                } else avatar.setVisibility(View.GONE)
            }
        })
        val alertDialog: AlertDialog = dialog!!.create()
        alertDialog.setView(view)
        updateUserBtn.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                val user: User = User()
                if (username.getText().toString().isEmpty()) username.setError(getString(R.string.fill_this_field)) else {
                    user.setName(username.getText().toString())
                    user.setUid(CometChat.getLoggedInUser().getUid())
                    user.setAvatar(avatar_url.getText().toString())
                    updateUser(user)
                    alertDialog.dismiss()
                }
            }
        })
        cancelBtn.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                alertDialog.dismiss()
            }
        })
        alertDialog.show()
    }

    private fun updateUser(user: User) {
        val authkey = UIKitContracts.AppInfo.AUTH_KEY;
        CometChat.updateUser(user, authkey, object : CallbackListener<User?>() {
            public override fun onSuccess(user: User?) {
                if (getContext() != null) Toast.makeText(getContext(), "Updated User Successfull", Toast.LENGTH_LONG).show()
                moreInfoScreenBinding!!.setUser(user)
            }

            public override fun onError(e: CometChatException) {
                if (getContext() != null) Toast.makeText(getContext(), e.message, Toast.LENGTH_LONG).show()
            }
        })
    }
}