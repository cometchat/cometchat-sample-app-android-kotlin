package com.cometchat.pro.androiduikit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import java.util.ArrayList
import com.cometchat.pro.androiduikit.constants.AppConfig
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private var loginBtn: MaterialButton? = null

    private var superhero1: MaterialCardView? = null

    private var superhero2: MaterialCardView? = null

    private var superhero3: MaterialCardView? = null

    private var superhero4: MaterialCardView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loginBtn = findViewById(R.id.login)
        superhero1 = findViewById(R.id.superhero1)
        superhero2 = findViewById(R.id.superhero2)
        superhero3 = findViewById(R.id.superhero3)
        superhero4 = findViewById(R.id.superhero4)
        loginBtn!!.setOnClickListener { startActivity(Intent(this@MainActivity, LoginActivity::class.java)) }

        superhero1!!.setOnClickListener { view ->
            findViewById<View>(R.id.superhero1_progressbar).visibility = View.VISIBLE
            login("superhero1")
        }
        superhero2!!.setOnClickListener { view ->
            findViewById<View>(R.id.superhero2_progressbar).visibility = View.VISIBLE
            login("superhero2")
        }
        superhero3!!.setOnClickListener { view ->
            findViewById<View>(R.id.superhero3_progressbar).visibility = View.VISIBLE
            login("superhero3")
        }
        superhero4!!.setOnClickListener { view ->
            findViewById<View>(R.id.superhero4_progressbar).visibility = View.VISIBLE
            login("superhero4")
        }

    }

    private fun login(uid: String) {

        CometChat.login(uid, AppConfig.AppDetails.API_KEY, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(user: User) {
                startActivity(Intent(this@MainActivity, SelectActivity::class.java))
                finish()
            }

            override fun onError(e: CometChatException) {
                val str = uid + "_progressbar"
                val id = resources.getIdentifier(str, "id", packageName)
                findViewById<View>(id).visibility = View.GONE
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
