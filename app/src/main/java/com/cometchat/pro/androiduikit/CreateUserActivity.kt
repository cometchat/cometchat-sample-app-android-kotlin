package com.cometchat.pro.androiduikit

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.pro.androiduikit.constants.AppConfig
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.cometchat.pro.uikit.ui_resources.utils.Utils

class CreateUserActivity : AppCompatActivity() {
    private var inputUid: TextInputLayout? = null
    private var inputName: TextInputLayout? = null
    private var uid: TextInputEditText? = null
    private var name: TextInputEditText? = null
    private var createUserBtn: MaterialButton? = null
    private var progressBar: ProgressBar? = null
    private var title: TextView? = null
    private var des1: TextView? = null
    private var des2: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        inputUid = findViewById(R.id.inputUID)
        inputName = findViewById(R.id.inputName)
        title = findViewById(R.id.tvTitle)
        des1 = findViewById(R.id.tvDes1)
        des2 = findViewById(R.id.tvDes2)
        progressBar = findViewById(R.id.createUser_pb)
        uid = findViewById(R.id.etUID)
        name = findViewById(R.id.etName)
        createUserBtn = findViewById(R.id.create_user_btn)
        createUserBtn!!.setTextColor(resources.getColor(R.color.textColorWhite))
        createUserBtn!!.setOnClickListener(View.OnClickListener {
            if (uid!!.text.toString().isEmpty()) uid!!.error = resources.getString(R.string.fill_this_field) else if (name!!.text.toString().isEmpty()) name!!.error = resources.getString(R.string.fill_this_field) else {
                progressBar!!.setVisibility(View.VISIBLE)
                createUserBtn!!.setClickable(false)
                val user = User()
                user.uid = uid!!.getText().toString()
                user.name = name!!.getText().toString()
                CometChat.createUser(user, AppConfig.AppDetails.AUTH_KEY, object : CallbackListener<User>() {
                    override fun onSuccess(user: User) {
                        login(user)
                    }

                    override fun onError(e: CometChatException) {
                        createUserBtn!!.setClickable(true)
                        Toast.makeText(this@CreateUserActivity, "Failed to create user", Toast.LENGTH_LONG).show()
                    }
                })
            }
        })
        checkDarkMode()
    }

    private fun checkDarkMode() {
        if (Utils.isDarkMode(this)) {
            title!!.setTextColor(resources.getColor(R.color.textColorWhite))
            des2!!.setTextColor(resources.getColor(R.color.textColorWhite))
            uid!!.setTextColor(resources.getColor(R.color.textColorWhite))
            name!!.setTextColor(resources.getColor(R.color.textColorWhite))
            inputUid!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            inputUid!!.defaultHintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            inputUid!!.boxStrokeColor = resources.getColor(R.color.textColorWhite)
            inputName!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            inputName!!.boxStrokeColor = resources.getColor(R.color.textColorWhite)
            inputName!!.defaultHintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            progressBar!!.indeterminateTintList = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
        } else {
            title!!.setTextColor(resources.getColor(R.color.primaryTextColor))
            des2!!.setTextColor(resources.getColor(R.color.primaryTextColor))
            uid!!.setTextColor(resources.getColor(R.color.primaryTextColor))
            inputUid!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.secondaryTextColor))
            inputUid!!.boxStrokeColor = resources.getColor(R.color.primaryTextColor)
            inputName!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.secondaryTextColor))
            inputName!!.boxStrokeColor = resources.getColor(R.color.primaryTextColor)
            name!!.setTextColor(resources.getColor(R.color.primaryTextColor))
            progressBar!!.indeterminateTintList = ColorStateList.valueOf(resources.getColor(R.color.primaryTextColor))
        }
    }

    private fun login(user: User) {
        CometChat.login(user.uid, AppConfig.AppDetails.AUTH_KEY, object : CallbackListener<User?>() {
            override fun onSuccess(user: User?) {
                startActivity(Intent(this@CreateUserActivity, SelectActivity::class.java))
            }

            override fun onError(e: CometChatException) {
                if (uid != null) Snackbar.make(uid!!.rootView, "Unable to login", Snackbar.LENGTH_INDEFINITE).setAction("Try Again") { startActivity(Intent(this@CreateUserActivity, LoginActivity::class.java)) }.show()
            }
        })
    }
}