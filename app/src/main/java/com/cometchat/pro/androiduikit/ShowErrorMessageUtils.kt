package com.cometchat.pro.androiduikit

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.cometchat.pro.exceptions.CometChatException

public class ShowErrorMessageUtils {
    companion object {
        public fun showDialog(context : Context, e: CometChatException) {

            val builder = AlertDialog.Builder(context)
            val dialogView = LayoutInflater.from(context).inflate(R.layout.error_message_view, null, false)
            builder.setView(dialogView)
            dialogView.findViewById<TextView>(R.id.tv_error_message).text = e.message
            val alertDialog = builder.create()
            alertDialog.window?.setGravity(Gravity.TOP)
            alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
            dialogView.findViewById<ImageView>(R.id.iv_error_close).setOnClickListener(View.OnClickListener {
                alertDialog.dismiss()
            })
            alertDialog.show()
        }
    }
}