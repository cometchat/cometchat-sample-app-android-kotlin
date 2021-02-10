package com.cometchat.pro.uikit.ui_resources.utils.custom_alertDialog

import android.view.View
import androidx.appcompat.app.AlertDialog

interface OnAlertDialogButtonClickListener {
    fun onButtonClick(alertDialog: AlertDialog?, v: View?, which: Int, popupId: Int)
}