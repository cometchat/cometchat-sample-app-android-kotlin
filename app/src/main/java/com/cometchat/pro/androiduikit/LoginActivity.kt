package com.cometchat.pro.androiduikit

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.pro.androiduikit.constants.AppConfig
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import utils.Utils

class LoginActivity : AppCompatActivity() {

    private var inputLayout: TextInputLayout? = null
    private var uid : TextInputEditText? = null
    private var progressBar : ProgressBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        uid = findViewById(R.id.etUID)
        progressBar = findViewById(R.id.loginProgress);
        inputLayout = findViewById(R.id.inputUID)
        uid!!.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                if (uid!!.text!!.toString().isEmpty()) {
                    Toast.makeText(this@LoginActivity, "Fill Username field", Toast.LENGTH_LONG).show()
                } else {
                    findViewById<View>(R.id.loginProgress).visibility = View.VISIBLE
                    inputLayout!!.isEndIconVisible = false
                    login(uid!!.text!!.toString())
                }
            }
            true
        }

        inputLayout!!.setEndIconOnClickListener { view ->
            if (uid!!.text!!.toString().isEmpty()) {
                Toast.makeText(this@LoginActivity, "Fill Username field", Toast.LENGTH_LONG).show()
            } else {
                findViewById<View>(R.id.loginProgress).visibility = View.VISIBLE
                inputLayout!!.isEndIconVisible = false
                login(uid!!.text!!.toString())
            }

        }

        if (Utils.isDarkMode(this)) {
            uid!!.setTextColor(resources.getColor(R.color.textColorWhite))
            inputLayout!!.boxStrokeColor = resources.getColor(R.color.textColorWhite)
            inputLayout!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            inputLayout!!.defaultHintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            uid!!.setHintTextColor(resources.getColor(R.color.textColorWhite))
            progressBar!!.setIndeterminateTintList(ColorStateList.valueOf(resources.getColor(R.color.textColorWhite)))
        } else {
            uid!!.setTextColor(resources.getColor(R.color.primaryTextColor))
            inputLayout!!.boxStrokeColor = resources.getColor(R.color.primaryTextColor)
            uid!!.hint = ""
            inputLayout!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.secondaryTextColor))
            progressBar!!.setIndeterminateTintList(ColorStateList.valueOf(resources.getColor(R.color.primaryTextColor)))

        }
    }

    private fun login(uid: String) {


        CometChat.login(uid, AppConfig.AppDetails.API_KEY, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(user: User) {
                startActivity(Intent(this@LoginActivity, SelectActivity::class.java))
                finish()
            }

            override fun onError(e: CometChatException) {
                inputLayout!!.isEndIconVisible = true
                findViewById<View>(R.id.loginProgress).visibility = View.GONE
                Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun createUser(view: View?) {
        startActivity(Intent(this@LoginActivity, CreateUserActivity::class.java))
    }
}
