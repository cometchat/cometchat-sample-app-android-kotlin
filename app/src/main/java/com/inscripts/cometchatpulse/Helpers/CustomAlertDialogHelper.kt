package com.inscripts.cometchatpulse.Helpers

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import com.cometchat.pro.helpers.Logger
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract

class CustomAlertDialogHelper: View.OnClickListener {

    private val TAG = CustomAlertDialogHelper::class.java.simpleName

    private lateinit var onAlertDialogButtonClick: OnAlertDialogButtonClickListener

    private var view: View? = null

    private lateinit var alertDialogCreater: AlertDialog

    private var popupId: Int = 0
    private var colorPrimary: Int = 0

    //	cc cometChat;
     constructor(context: Context, title: String, view: View, positiveTitle: String, neutralTitle: String,
                                negativeTitle: String, onAlertDialogButton: OnAlertDialogButtonClickListener, popUpId: Int, isCancelable: Boolean) {
        onAlertDialogButtonClick = onAlertDialogButton
        // LayoutInflater inflater = (LayoutInflater)
        // context_menu.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.view = view
        val builder = AlertDialog.Builder(context)

        //todo get color from cc Sdk
        //		cometChat = cc.getInstance(context_menu);
        colorPrimary = StringContract.Color.primaryDarkColor
        //		colorPrimary = (int) cometChat.getCCSetting(new CCSettingMapper(SettingType.UI_SETTINGS, SettingSubType.COLOR_PRIMARY));
        builder.setView(view)
        builder.setCancelable(isCancelable)
        if (title != "") {
            builder.setTitle(title)
        }

        Logger.error(TAG, "ACTION_SEND title : $title")
        Logger.error(TAG, "ACTION_SEND positiveTitle : $positiveTitle")
        Logger.error(TAG, "ACTION_SEND negativeTitle : $negativeTitle")

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

        this.popupId = popUpId

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


    override fun onClick(v: View) {
        onAlertDialogButtonClick.onButtonClick(alertDialogCreater, view, v.id, popupId)
    }

}