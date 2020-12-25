package listeners

import android.view.View
import androidx.appcompat.app.AlertDialog

interface OnAlertDialogButtonClickListener {
    fun onButtonClick(alertDialog: AlertDialog?, v: View?, which: Int, popupId: Int)
}