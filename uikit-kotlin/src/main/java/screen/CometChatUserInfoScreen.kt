package screen

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
import com.cometchat.pro.uikit.Avatar
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.databinding.FragmentMoreInfoScreenBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import constant.StringContract
import utils.FontUtils
import utils.PreferenceUtil
import utils.Utils

class CometChatUserInfoScreen constructor() : Fragment() {
    private val notificationIv: Avatar? = null
    private var dialog: AlertDialog.Builder? = null
    var moreInfoScreenBinding: FragmentMoreInfoScreenBinding? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                     savedInstanceState: Bundle?): View? {
        moreInfoScreenBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_more_info_screen, container, false)
        moreInfoScreenBinding!!.setUser(CometChat.getLoggedInUser())
        moreInfoScreenBinding!!.ivUser.setAvatar(CometChat.getLoggedInUser())
        moreInfoScreenBinding!!.tvTitle.setTypeface(FontUtils.getInstance(getActivity()).getTypeFace(FontUtils.robotoMedium))
        Log.e("onCreateView: ", CometChat.getLoggedInUser().toString())
        moreInfoScreenBinding!!.privacyAndSecurity.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(view: View) {
                startActivity(Intent(getContext(), CometChatMorePrivacyScreenActivity::class.java))
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
        val view: View = LayoutInflater.from(getContext()).inflate(R.layout.update_user, null)
        val avatar: Avatar = view.findViewById(R.id.user_avatar)
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
        val authkey = StringContract.AppInfo.AUTH_KEY;
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