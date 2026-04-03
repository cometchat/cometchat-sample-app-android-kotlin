package com.cometchat.uikit.kotlin.presentation.emojikeyboard.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.NonNull
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.emojikeyboard.model.EmojiRepository
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * BottomSheetDialogFragment that hosts an [EmojiKeyBoardView] for modal presentation.
 *
 * Direct 1:1 port of `CometChatEmojiKeyboard.java` from the Java chatuikit module.
 */
class CometChatEmojiKeyboard : BottomSheetDialogFragment() {

    companion object {
        private val TAG = CometChatEmojiKeyboard::class.java.simpleName
    }

    private var fm: FragmentManager? = null
    private var emojiKeyBoardView: EmojiKeyBoardView? = null

    fun setStyle(@StyleRes emojiKeyboardStyle: Int) {
        emojiKeyBoardView?.setStyle(emojiKeyboardStyle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Remove emojiKeyBoardView from any existing parent
        emojiKeyBoardView?.let { view ->
            val parent = view.parent
            if (parent is ViewGroup) {
                parent.removeView(view)
            }
        }
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            Utils.convertDpToPx(requireContext(), 400)
        )
        emojiKeyBoardView?.layoutParams = layoutParams
        return emojiKeyBoardView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(R.color.cometchat_color_transparent)
        }
        return dialog
    }

    /**
     * Shows the emoji keyboard dialog using the stored FragmentManager.
     */
    fun show() {
        fm?.let { fragmentManager ->
            if (!isAdded && !fragmentManager.isDestroyed) {
                show(fragmentManager, TAG)
            }
        }
    }

    /**
     * Creates the [EmojiKeyBoardView], triggers emoji loading, and shows the dialog.
     *
     * @param context The context used to create the view and obtain the FragmentManager
     */
    fun show(context: Context) {
        EmojiRepository.loadAndSaveEmojis(context)
        emojiKeyBoardView = EmojiKeyBoardView(context)
        val activity: Activity? = Utils.getActivity(context)
        if (Utils.isActivityUsable(activity) && activity is AppCompatActivity) {
            setupFragmentManager(context)
            fm = activity.supportFragmentManager
            if (!isAdded) {
                fm?.let { fragmentManager ->
                    if (!fragmentManager.isDestroyed) {
                        show(fragmentManager, TAG)
                    } else {
                        setupFragmentManager(context)
                        show(fragmentManager, TAG)
                    }
                }
            }
        }
    }

    private fun setupFragmentManager(context: Context) {
        fm = (context as AppCompatActivity).supportFragmentManager
    }

    /**
     * Sets the emoji click callback, forwarding to the hosted [EmojiKeyBoardView].
     */
    fun setOnClick(onClick: EmojiKeyBoardView.OnClick?) {
        if (onClick != null) {
            emojiKeyBoardView?.setOnClick(onClick)
        }
    }
}
