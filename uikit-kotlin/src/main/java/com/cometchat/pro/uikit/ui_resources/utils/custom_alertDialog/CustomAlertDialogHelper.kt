package com.cometchat.pro.uikit.ui_resources.utils.custom_alertDialog

import android.content.Context
import android.content.DialogInterface
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.cometchat.pro.uikit.R

class CustomAlertDialogHelper(context: Context, title: String, private val view: View, positiveTitle: String, neutralTitle: String,
                              negativeTitle: String, private val onAlertDialogButtonClick: OnAlertDialogButtonClickListener, popUpId: Int, isCancelable: Boolean) : View.OnClickListener {
    private val alertDialogCreater: AlertDialog
    private val popupId: Int
    private val colorPrimary: Int
    override fun onClick(v: View) {
        onAlertDialogButtonClick.onButtonClick(alertDialogCreater, view, v.id, popupId)
    }

    companion object {
        private val TAG = CustomAlertDialogHelper::class.java.simpleName
    }

    //	cc cometChat;
    init {
        // LayoutInflater inflater = (LayoutInflater)
        // context_menu.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        val builder = AlertDialog.Builder(context)

        //todo get color from cc Sdk
//		cometChat = cc.getInstance(context_menu);
        colorPrimary = context.resources.getColor(R.color.colorPrimaryDark)
        //		colorPrimary = (int) cometChat.getCCSetting(new CCSettingMapper(SettingType.UI_SETTINGS, SettingSubType.COLOR_PRIMARY));
        builder.setView(view)
        builder.setCancelable(isCancelable)
        if (title != "") {
            builder.setTitle(title)
        }
        if (positiveTitle != "") {
            builder.setPositiveButton(positiveTitle, null)
        }
        if (negativeTitle != "") {
            builder.setNegativeButton(negativeTitle, null)
        }
        if (neutralTitle != "") {
            builder.setNeutralButton(neutralTitle, null)
        }
        alertDialogCreater = builder.create()
        alertDialogCreater.show()
        popupId = popUpId
        val positiveButton = alertDialogCreater.getButton(DialogInterface.BUTTON_POSITIVE)
        positiveButton.id = DialogInterface.BUTTON_POSITIVE
        positiveButton.setTextColor(colorPrimary)
        positiveButton.setOnClickListener(this)
        val negativeButton = alertDialogCreater.getButton(DialogInterface.BUTTON_NEGATIVE)
        negativeButton.id = DialogInterface.BUTTON_NEGATIVE
        negativeButton.setTextColor(colorPrimary)
        negativeButton.setOnClickListener(this)
        val neutralButton = alertDialogCreater.getButton(DialogInterface.BUTTON_NEUTRAL)
        neutralButton.id = DialogInterface.BUTTON_NEUTRAL
        neutralButton.setTextColor(colorPrimary)
        neutralButton.setOnClickListener(this)
    }
}