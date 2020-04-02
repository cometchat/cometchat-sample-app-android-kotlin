package listeners;

import android.view.View;

import androidx.appcompat.app.AlertDialog;

public interface OnAlertDialogButtonClickListener {
	void onButtonClick(AlertDialog alertDialog, View v, int which, int popupId);
}