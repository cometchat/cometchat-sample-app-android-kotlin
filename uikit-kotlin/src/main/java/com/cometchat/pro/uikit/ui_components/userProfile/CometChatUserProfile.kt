package com.cometchat.pro.uikit.ui_components.userProfile

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.databinding.FragmentCometchatUserProfileBinding
import com.cometchat.pro.uikit.ui_components.shared.cometchatAvatar.CometChatAvatar
import com.cometchat.pro.uikit.ui_components.userProfile.privacy_and_security.CometChatMorePrivacyActivity
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction
import com.cometchat.pro.uikit.ui_settings.UIKitSettings
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

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
        moreInfoScreenBinding?.user = CometChat.getLoggedInUser()
        moreInfoScreenBinding?.ivUser?.setAvatar(CometChat.getLoggedInUser())
        moreInfoScreenBinding?.tvTitle?.typeface = FontUtils.getInstance(activity).getTypeFace(FontUtils.robotoMedium)
        Log.e("onCreateView: ", CometChat.getLoggedInUser().toString())
        moreInfoScreenBinding?.privacyAndSecurity?.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(view: View) {
                startActivity(Intent(context, CometChatMorePrivacyActivity::class.java))
            }
        })
        if (UIKitSettings.color != null) {
            val widgetColor = Color.parseColor(UIKitSettings.color)
            val wrappedDrawable = DrawableCompat.wrap(resources.getDrawable(R.drawable.ic_security_24dp))
            wrappedDrawable.setTint(widgetColor)
            DrawableCompat.setTint(wrappedDrawable, widgetColor)
            moreInfoScreenBinding?.ivSecurity?.setImageDrawable(wrappedDrawable)
        }
        if (Utils.isDarkMode(context!!)) {
            moreInfoScreenBinding?.tvTitle?.setTextColor(resources.getColor(R.color.textColorWhite))
            moreInfoScreenBinding?.tvSeperator?.setBackgroundColor(resources.getColor(R.color.grey))
            moreInfoScreenBinding?.tvSeperator1?.setBackgroundColor(resources.getColor(R.color.grey))
        } else {
            moreInfoScreenBinding?.tvTitle?.setTextColor(resources.getColor(R.color.primaryTextColor))
            moreInfoScreenBinding?.tvSeperator?.setBackgroundColor(resources.getColor(R.color.light_grey))
            moreInfoScreenBinding?.tvSeperator1?.setBackgroundColor(resources.getColor(R.color.light_grey))
        }
        moreInfoScreenBinding?.userContainer?.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                updateUserDialog()
            }
        })
        return moreInfoScreenBinding?.root
    }

    private fun updateUserDialog() {
        dialog = AlertDialog.Builder(context)
        val view: View = LayoutInflater.from(context).inflate(R.layout.cometchat_update_user_dialog, null)
        val avatar: CometChatAvatar = view.findViewById(R.id.user_avatar)
        avatar.setAvatar(CometChat.getLoggedInUser())
        val avatar_url: TextInputEditText = view.findViewById(R.id.avatar_url_edt)
        avatar_url.setText(CometChat.getLoggedInUser().avatar)
        val username: TextInputEditText = view.findViewById(R.id.username_edt)
        username.setText(CometChat.getLoggedInUser().name)
        val updateUserBtn: MaterialButton = view.findViewById(R.id.updateUserBtn)
        val cancelBtn: MaterialButton = view.findViewById(R.id.cancelBtn)
        if (CometChat.getLoggedInUser().avatar == null) {
            avatar.visibility = View.GONE
            avatar_url.visibility = View.GONE
        } else {
            avatar.visibility = View.VISIBLE
            avatar_url.visibility = View.GONE
        }
        avatar_url.addTextChangedListener(object : TextWatcher {
            public override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            public override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            public override fun afterTextChanged(s: Editable) {
                if (s.toString().isNotEmpty()) {
                    avatar.visibility = View.VISIBLE
                    Glide.with((context)!!).load(s.toString()).into(avatar)
                } else avatar.visibility = View.GONE
            }
        })
        val alertDialog: AlertDialog = dialog!!.create()
        alertDialog.setView(view)
        updateUserBtn.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                val user: User = User()
                if (username.text.toString().isEmpty()) username.error = getString(R.string.fill_this_field) else {
                    user.name = username.text.toString()
                    user.uid = CometChat.getLoggedInUser().uid
                    user.avatar = avatar_url.text.toString()
                    updateUser(user)
                    alertDialog.dismiss()
                }
            }
        })
        cancelBtn.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun updateUser(user: User) {
        val authkey = UIKitConstants.AppInfo.AUTH_KEY;
        CometChat.updateUser(user, authkey, object : CallbackListener<User?>() {
            public override fun onSuccess(user: User?) {
                if (context != null) Toast.makeText(context, "Updated User Successfully", Toast.LENGTH_LONG).show()
                moreInfoScreenBinding?.user = user
            }

            public override fun onError(e: CometChatException) {
                if (context != null)
                    ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
//                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        })
    }
}