package com.cometchat.pro.androiduikit

import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast

import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

import com.cometchat.pro.androiduikit.constants.AppConfig

class LoginActivity : AppCompatActivity() {

    private var inputLayout: TextInputLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val uid = findViewById<TextInputEditText>(R.id.etUID)
        inputLayout = findViewById(R.id.inputUID)
        uid.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                if (uid.text!!.toString().isEmpty()) {
                    Toast.makeText(this@LoginActivity, "Fill Username field", Toast.LENGTH_LONG).show()
                } else {
                    findViewById<View>(R.id.loginProgress).visibility = View.VISIBLE
                    inputLayout!!.isEndIconVisible = false
                    login(uid.text!!.toString())
                }
            }
            true
        }

        inputLayout!!.setEndIconOnClickListener { view ->
            if (uid.text!!.toString().isEmpty()) {
                Toast.makeText(this@LoginActivity, "Fill Username field", Toast.LENGTH_LONG).show()
            } else {
                findViewById<View>(R.id.loginProgress).visibility = View.VISIBLE
                inputLayout!!.isEndIconVisible = false
                login(uid.text!!.toString())
            }

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

}
