package com.inscripts.cometchatpulse.Activities

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import androidx.databinding.DataBindingUtil
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.text.Html
import android.text.SpannableString
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.inscripts.cometchatpulse.Adapter.AutoCompleteAdapter
import com.inscripts.cometchatpulse.Pojo.GroupOption
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

            var autoAdapter=AutoCompleteAdapter(this,R.layout.sample_user_item,getSampleUserList())
            binding.sampleList.adapter=autoAdapter

//            binding.editTextUid.onItemClickListener =
//                    AdapterView.OnItemClickListener { parent, view, position, id ->
//                        binding.editTextUid.setText(getSampleUserList()[position].id?.trim())
//                    }

            binding.sampleList.setOnItemClickListener{
                parent, view, position, id ->
                binding.editTextUid.setText(getSampleUserList()[position].id)
                getSampleUserList()[position].id?.trim()?.let { onLoginClick(it) }
            }


            drawableUid.setColorFilter(StringContract.Color.primaryColor,PorterDuff.Mode.SRC_ATOP)
          for (drawable:Drawable? in binding.editTextUid.compoundDrawables) {
              if (drawable != null) {
                  drawable.setColorFilter(PorterDuffColorFilter(ContextCompat.getColor(binding.editTextUid.getContext(), StringContract.Color.primaryColor), PorterDuff.Mode.SRC_ATOP))
              }
          }

        }

        CommonUtil.setStatusBarColor(this)

    }

     fun getSampleUserList():MutableList<GroupOption>{
         val sampleList:MutableList<GroupOption> = arrayListOf()

         sampleList.add(GroupOption("Iron Man",getDrawable(R.drawable.ironman),"superhero1"))
         sampleList.add(GroupOption("Captain America",getDrawable(R.drawable.captainamerica),"superhero2"))
         sampleList.add(GroupOption("SpiderMan",getDrawable(R.drawable.spiderman),"superhero3"))
         sampleList.add(GroupOption("Wolverine",getDrawable(R.drawable.wolverine),"superhero4"))

         return sampleList
     }

    fun onLoginClick(uid:String){

        Log.d("Login", uid)
        binding.progressHorizontal.visibility=View.VISIBLE
        kotlin.run {
            Toast.makeText(this@LoginActivity,getString(R.string.please_wait),Toast.LENGTH_SHORT).show()
            CometChat.login(uid.trim(), StringContract.AppDetails.API_KEY, object : CometChat.CallbackListener<User>() {
                override fun onSuccess(p0: User?) {
                    startActivity()
                    Log.d(TAG,"login: ${StringContract.AppDetails.APP_ID}_user_${p0?.uid}")

                }

                override fun onError(p0: CometChatException?) {
                    Toast.makeText(context, p0?.message, Toast.LENGTH_SHORT).show()
                     binding.progressHorizontal.visibility=View.GONE
                     Log.d(TAG,"onError:Login "+p0?.message)

                }

            })

        }


    }

    fun startActivity() {
       startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}


