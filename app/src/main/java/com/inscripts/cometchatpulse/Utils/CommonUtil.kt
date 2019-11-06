package com.inscripts.cometchatpulse.Utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.location.LocationManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.os.Build
import android.support.annotation.IdRes
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.User
import com.inscripts.cometchatpulse.Activities.CallActivity
import com.inscripts.cometchatpulse.Activities.LocationActivity
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract

class CommonUtil {

    companion object {

         var cm: ConnectivityManager?=null

        fun setCardView(cardView: CardView) {
            cardView.cardElevation = StringContract.Dimensions.cardViewElevation
            cardView.radius = StringContract.Dimensions.cardViewCorner
            val param = cardView.layoutParams as RelativeLayout.LayoutParams
            param.marginEnd = StringContract.Dimensions.marginEnd
            param.marginStart = StringContract.Dimensions.marginStart
            cardView.layoutParams = param

        }


         fun setDrawable(color:Int,radius:Float): Drawable? {

            val drawable= GradientDrawable()
            drawable.shape= GradientDrawable.RECTANGLE
            drawable.cornerRadius= radius
            drawable.setColor(color)

            return drawable
        }



        fun checkPermission (context: Context):Boolean{

            return (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

        fun startCallIntent(receiverType:String,context: Context, user: User, type: String,
                            isOutgoing: Boolean, sessionId: String) {
            val callIntent = Intent(context, CallActivity::class.java)
            callIntent.putExtra(StringContract.IntentString.USER_NAME, user.name)
            callIntent.putExtra(StringContract.IntentString.USER_ID, user.uid)
            callIntent.putExtra(StringContract.IntentString.SESSION_ID, sessionId)
            callIntent.putExtra(StringContract.IntentString.USER_AVATAR, user.avatar)
            callIntent.putExtra(StringContract.IntentString.RECIVER_TYPE, receiverType)
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            callIntent.action = type

            if (isOutgoing) {
                callIntent.type = StringContract.IntentString.OUTGOING
            } else {
                callIntent.type = StringContract.IntentString.INCOMING
            }
            context.startActivity(callIntent)
        }


        fun getAudioManager(context: Context): AudioManager {
            return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }

        fun startCallIntent(receiverType:String,context: Context, group: Group, type: String,
                            isOutgoing: Boolean, sessionId: String) {
            val callIntent = Intent(context, CallActivity::class.java)
            callIntent.putExtra(StringContract.IntentString.GROUP_NAME, group.name)
            callIntent.putExtra(StringContract.IntentString.GROUP_ID, group.guid)
            callIntent.putExtra(StringContract.IntentString.SESSION_ID, sessionId)
            callIntent.putExtra(StringContract.IntentString.GROUP_ICON, group.icon)
            callIntent.putExtra(StringContract.IntentString.RECIVER_TYPE, receiverType)
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            callIntent.action = type

            if (isOutgoing) {
                callIntent.type = StringContract.IntentString.OUTGOING
            } else {
                callIntent.type = StringContract.IntentString.INCOMING
            }
            context.startActivity(callIntent)
        }

        fun isConnected(context: Context): Boolean {

            if (cm == null) {
                cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            }

            val var0 = cm!!.getActiveNetworkInfo()
            return null != var0 && var0!!.isConnectedOrConnecting()
        }


        fun setTitle(title: String, context: Context): SpannableString {
            val ss = SpannableString(title)

            var color:Int

            if (StringContract.AppDetails.theme==Appearance.AppTheme.AZURE_RADIANCE){
                color=StringContract.Color.iconTint
            }
            else{
             color=StringContract.Color.primaryColor
            }
            ss.setSpan(ForegroundColorSpan(color), 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            return ss
        }


        fun dpToPx(context: Context, valueInDp: Float): Float {
            val resources = context.resources
            val metrics = resources.displayMetrics
            return valueInDp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }


        fun findById(parent: View, @IdRes resId: Int): Any {
            return parent.findViewById<View>(resId) as Any
        }

        fun setStatusBarColor(var0: Activity) {
            try {
                if (Build.VERSION.SDK_INT >= 21) {
                    val var2 = var0.window
                    var2.addFlags((-2147483648).toInt())
                    var2.statusBarColor = StringContract.Color.primaryDarkColor
                }
            } catch (var3: Exception) {
                var3.printStackTrace()
            }

        }

    }
}
