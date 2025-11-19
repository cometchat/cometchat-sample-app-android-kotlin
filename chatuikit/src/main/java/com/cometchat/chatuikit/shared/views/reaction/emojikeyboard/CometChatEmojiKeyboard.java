package com.cometchat.chatuikit.shared.views.reaction.emojikeyboard;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CometChatEmojiKeyboard extends BottomSheetDialogFragment {
    private static final String TAG = CometChatEmojiKeyboard.class.getSimpleName();

    private FragmentManager fm;

    private EmojiKeyBoardView emojiKeyBoardView;

    public CometChatEmojiKeyboard() {
    }

    public void setStyle(@StyleRes int emojiKeyboardStyle) {
        emojiKeyBoardView.setStyle(emojiKeyboardStyle);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {

        super.onDismiss(dialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Utils.removeParentFromView(emojiKeyBoardView);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                               Utils.convertDpToPx(requireContext(), 400));
        if (emojiKeyBoardView != null) emojiKeyBoardView.setLayoutParams(layoutParams);
        return emojiKeyBoardView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(R.color.cometchat_color_transparent);
            }
        });
        return dialog;
    }

    /**
     * show() method is use to populate the emoji key board Dialog in Screen
     */
    public void show() {
        if (!isAdded() && !fm.isDestroyed()) show(fm, CometChatEmojiKeyboard.class.getSimpleName());
    }

    public void show(Context context) {
        emojiKeyBoardView = new EmojiKeyBoardView(context);
        Activity activity = Utils.getActivity(context);
        if (Utils.isActivityUsable(activity) && activity instanceof AppCompatActivity) {
            setUpFragmentManger(context);
            fm = ((AppCompatActivity) context).getSupportFragmentManager();
            if (!isAdded()) {
                if (!fm.isDestroyed()) {
                    show(fm, CometChatEmojiKeyboard.class.getSimpleName());
                } else {
                    setUpFragmentManger(context);
                    show(fm, CometChatEmojiKeyboard.class.getSimpleName());
                }
            }
        }
    }

    private void setUpFragmentManger(Context context) {
        fm = ((AppCompatActivity) context).getSupportFragmentManager();
    }

    public void setOnClick(EmojiKeyBoardView.OnClick onClick) {
        if (onClick != null) {
            emojiKeyBoardView.setOnClick(onClick);
        }
    }
}
