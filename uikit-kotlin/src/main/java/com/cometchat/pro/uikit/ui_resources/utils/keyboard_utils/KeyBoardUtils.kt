package com.cometchat.pro.uikit.ui_resources.utils.keyboard_utils

import android.app.Activity
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener

public class KeyBoardUtils {
    companion object {
        var mAppHeight = 0

        var currentOrientation = -1

        fun setKeyboardVisibilityListener(activity: Activity, contentView: View, keyboardVisibilityListener: KeyboardVisibilityListener) {
            contentView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                private var mPreviousHeight = 0
                override fun onGlobalLayout() {
                    val newHeight = contentView.height
                    if (newHeight == mPreviousHeight) return
                    mPreviousHeight = newHeight
                    if (activity.resources.configuration.orientation != currentOrientation) {
                        currentOrientation = activity.resources.configuration.orientation
                        mAppHeight = 0
                    }
                    if (newHeight >= mAppHeight) {
                        mAppHeight = newHeight
                    }
                    if (newHeight != 0) {
                        if (mAppHeight > newHeight) {
                            keyboardVisibilityListener.onKeyboardVisibilityChanged(true)
                        } else {
                            keyboardVisibilityListener.onKeyboardVisibilityChanged(false)
                        }
                    }
                }
            })
        }
    }
}