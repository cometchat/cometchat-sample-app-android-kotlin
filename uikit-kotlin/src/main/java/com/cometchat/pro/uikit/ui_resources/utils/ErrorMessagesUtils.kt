package com.cometchat.pro.uikit.ui_resources.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.constants.CometChatConstants.Errors.*
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants

class ErrorMessagesUtils {
    companion object {
        fun cometChatErrorMessage(context: Context?, e : String?) {
            when (e) {
                ERROR_INTERNET_UNAVAILABLE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.please_check_your_internet_connection))
                }
                else -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.something_went_wrong_please_try_again))
                }
            }
        }

        fun showCometChatErrorDialog (context: Context?, errorMessage : String?) {
            val builder = context?.let { AlertDialog.Builder(it) }
            val dialogView = LayoutInflater.from(context).inflate(R.layout.cometchat_error_message_view, null, false)
            builder?.setView(dialogView)
            dialogView.findViewById<TextView>(R.id.tv_error_message).text = errorMessage
            dialogView.findViewById<LinearLayout>(R.id.ll_background).background = context?.getDrawable(R.color.red_600)
            dialogView.findViewById<ImageView>(R.id.iv_error_icon).setImageResource(R.drawable.error_icon)
            val alertDialog = builder?.create()
            alertDialog?.window?.setGravity(Gravity.TOP)
            alertDialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
            dialogView.findViewById<ImageView>(R.id.iv_error_close).setOnClickListener(View.OnClickListener {
                alertDialog?.dismiss()
            })
            alertDialog?.show()
        }
    }
}