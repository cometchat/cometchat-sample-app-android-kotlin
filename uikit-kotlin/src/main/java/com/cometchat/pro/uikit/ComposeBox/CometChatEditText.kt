package com.cometchat.pro.uikit.ComposeBox

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.os.BuildCompat
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat

class CometChatEditText : AppCompatEditText {

    constructor(context: Context):super(context){

    }
    constructor(context: Context, attributeSet: AttributeSet):super(context, attributeSet){}
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr){
    }

    private val TAG = "CometChatEditText"
    var onEditTextMediaListener: CometChatEditText.OnEditTextMediaListener? = null

    override fun onCreateInputConnection(outAttrs: EditorInfo?): InputConnection {
        val ic = super.onCreateInputConnection(outAttrs)
        EditorInfoCompat.setContentMimeTypes(outAttrs!!, arrayOf("image/png", "image/gif"))

        val callback = InputConnectionCompat.OnCommitContentListener { inputContentInfo, flags, opts ->
            // read and display inputContentInfo asynchronously
            if (BuildCompat.isAtLeastNMR1() && flags and
                    InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION != 0) {
                try {
                    inputContentInfo.requestPermission()
                } catch (e: Exception) {
                    return@OnCommitContentListener false // return false if failed
                }
            }
            val cr = context.contentResolver
            val mimeType = cr.getType(inputContentInfo.linkUri!!)
            Log.e(TAG, """
     onCommitContent: ${inputContentInfo.linkUri!!.path}
     ${inputContentInfo.contentUri}
     $mimeType
     """.trimIndent())
            onEditTextMediaListener!!.OnMediaSelected(inputContentInfo)
            // read and display inputContentInfo asynchronously.
            // call inputContentInfo.releasePermission() as needed.
            true // return true if succeeded
        }
        return InputConnectionCompat.createWrapper(ic, outAttrs, callback)
    }

    fun setMediaSelected(onEditTextMediaListener: OnEditTextMediaListener?) {
        this.onEditTextMediaListener = onEditTextMediaListener
    }

    interface OnEditTextMediaListener {
        fun OnMediaSelected(i: InputContentInfoCompat?)
    }
}