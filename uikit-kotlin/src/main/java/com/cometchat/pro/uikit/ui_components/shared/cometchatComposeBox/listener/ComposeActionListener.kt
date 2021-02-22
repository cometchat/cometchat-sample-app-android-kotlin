package com.cometchat.pro.uikit.ui_components.shared.cometchatComposeBox.listener

import android.text.Editable
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.core.view.inputmethod.InputContentInfoCompat

abstract class ComposeActionListener {
    fun onMoreActionClicked(moreIcon: ImageView?) {}
    open fun onCameraActionClicked() {}
    open fun onGalleryActionClicked() {}
    open fun onAudioActionClicked() {}
    open fun onFileActionClicked() {}
    open fun onLocationActionClicked() {}
    fun onEmojiActionClicked(emojiIcon: ImageView?) {}
    open fun onSendActionClicked(editText: EditText?) {}
    open fun onVoiceNoteComplete(string: String?) {}
    abstract fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int)
    abstract fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int)
    abstract fun afterTextChanged(editable: Editable?)

    open fun onEditTextMediaSelected(inputContentInfo: InputContentInfoCompat?) {}

    fun getCameraActionView(cameraIcon: ImageView) {
        cameraIcon.visibility = View.VISIBLE
    }

    fun getGalleryActionView(galleryIcon: ImageView) {
        galleryIcon.visibility = View.VISIBLE
    }

    fun getFileActionView(fileIcon: ImageView) {
        fileIcon.visibility = View.VISIBLE
    }

    open fun onStickerActionClicked() {}

    open fun onWhiteBoardClicked() {}
    open fun onWriteBoardClicked() {}
    open fun onStartCallClicked() {}
}