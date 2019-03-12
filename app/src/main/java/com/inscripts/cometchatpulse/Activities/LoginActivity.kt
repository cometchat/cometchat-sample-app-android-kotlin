package com.inscripts.cometchatpulse.Activities

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.databinding.DataBindingUtil
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.SpannableString
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.google.firebase.messaging.FirebaseMessaging
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.Appearance
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.databinding.ActivityLoginBinding


public class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var context: Context
    private val TAG="LoginActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        CommonUtil.setStatusBarColor(this)
        if (CometChat.getLoggedInUser() != null) {
            startActivity()
        }

        else {
          binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
            binding.login = this

             if (StringContract.AppDetails.theme==Appearance.AppTheme.PERSIAN_BLUE||
                     StringContract.AppDetails.theme==Appearance.AppTheme.MOUNTAIN_MEADOW) {
                 binding.btLogin.setTextColor(StringContract.Color.white)
                 binding.rlBack.setBackgroundColor(StringContract.Color.primaryColor)
                 binding.progressHorizontal.progressTintList= ColorStateList.valueOf(StringContract.Color.primaryColor)
             }
            else{
                 binding.progressHorizontal.progressTintList= ColorStateList.valueOf(StringContract.Color.iconTint)
                 binding.btLogin.setTextColor(StringContract.Color.black)
                 binding.rlBack.setBackgroundColor(StringContract.Color.primaryDarkColor)
             }

            binding.editTextUid.typeface=StringContract.Font.name
            binding.btLogin.typeface=StringContract.Font.status
            val drawableUid=(binding.btLogin.background)

            drawableUid.setColorFilter(StringContract.Color.primaryColor,PorterDuff.Mode.SRC_ATOP)
          for (drawable:Drawable? in binding.editTextUid.compoundDrawables) {
              if (drawable != null) {
                  drawable.setColorFilter(PorterDuffColorFilter(ContextCompat.getColor(binding.editTextUid.getContext(), StringContract.Color.primaryColor), PorterDuff.Mode.SRC_ATOP))
              }
          }

        }

        CommonUtil.setStatusBarColor(this)

    }

    fun onLoginClick(uid:String){

        Log.d("Login", uid)
        binding.progressHorizontal.visibility=View.VISIBLE
        kotlin.run {
            Toast.makeText(this@LoginActivity,"Please Wait",Toast.LENGTH_SHORT).show()
            CometChat.login(uid.trim(), StringContract.AppDetails.API_KEY, object : CometChat.CallbackListener<User>() {
                override fun onSuccess(p0: User?) {
                    startActivity()

                    FirebaseMessaging.getInstance().subscribeToTopic("user_${p0?.uid}")
                }

                override fun onError(p0: CometChatException?) {
                    Toast.makeText(context, p0?.message, Toast.LENGTH_SHORT).show()
                     binding.progressHorizontal.visibility=View.GONE
                    Log.d(TAG,"onError:Login "+p0?.message)


                }

            })

//            CometChat.login("superhero11_3b34bb32bfc7f3a930c829b75d6ffeb579efbd0e",object :CometChat.CallbackListener<User>(){
//                override fun onSuccess(p0: User?) {
//                   Log.d(TAG,"onSuccess: "+p0.toString())
//                    binding.progressHorizontal.visibility=View.GONE
//                    startActivity()
//                }
//
//                override fun onError(p0: CometChatException?) {
//                   Log.d(TAG,"onError: "+p0?.message)
//                    binding.progressHorizontal.visibility=View.GONE
//                }
//
//            })
        }


    }

    fun startActivity() {
       startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}


