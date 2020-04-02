package listeners;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.cometchat.pro.uikit.R;

public class CustomAlertDialogHelper implements View.OnClickListener {
    private static final String TAG = CustomAlertDialogHelper.class.getSimpleName();

    private OnAlertDialogButtonClickListener onAlertDialogButtonClick;

    private View view;

    private AlertDialog alertDialogCreater;

    private int popupId;
    private int colorPrimary;

    //	cc cometChat;
    public CustomAlertDialogHelper(Context context, String title, View view, String positiveTitle, String neutralTitle,
                                   String negativeTitle, OnAlertDialogButtonClickListener onAlertDialogButton, int popUpId, boolean isCancelable) {
        onAlertDialogButtonClick = onAlertDialogButton;
        // LayoutInflater inflater = (LayoutInflater)
        // context_menu.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.view = view;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        //todo get color from cc Sdk
//		cometChat = cc.getInstance(context_menu);
        colorPrimary = context.getResources().getColor(R.color.colorPrimaryDark);
//		colorPrimary = (int) cometChat.getCCSetting(new CCSettingMapper(SettingType.UI_SETTINGS, SettingSubType.COLOR_PRIMARY));
        builder.setView(view);
        builder.setCancelable(isCancelable);
        if (!title.equals("")) {
            builder.setTitle(title);
        }


        if (!positiveTitle.equals("")) {
            builder.setPositiveButton(positiveTitle, null);
        }
        if (!negativeTitle.equals("")) {
            builder.setNegativeButton(negativeTitle, null);
        }
        if (!neutralTitle.equals("")) {
            builder.setNeutralButton(neutralTitle, null);
        }

        alertDialogCreater = builder.create();
        alertDialogCreater.show();

        this.popupId = popUpId;

        Button positiveButton = alertDialogCreater.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setId(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setTextColor(colorPrimary);
        positiveButton.setOnClickListener(this);

        Button negativeButton = alertDialogCreater.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setId(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setTextColor(colorPrimary);
        negativeButton.setOnClickListener(this);

        Button neutralButton = alertDialogCreater.getButton(DialogInterface.BUTTON_NEUTRAL);
        neutralButton.setId(DialogInterface.BUTTON_NEUTRAL);
        neutralButton.setTextColor(colorPrimary);
        neutralButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        onAlertDialogButtonClick.onButtonClick(alertDialogCreater, view, v.getId(), popupId);
    }

}