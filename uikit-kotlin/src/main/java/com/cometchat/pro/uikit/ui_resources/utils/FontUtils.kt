package com.cometchat.pro.uikit.ui_resources.utils

import android.content.Context
import android.graphics.Typeface

public class FontUtils {


    private var context: Context? = null
    companion object {
        private var _instance: FontUtils? = null

        val robotoMedium = "Roboto-Medium.ttf"

        val robotoBlack = "Roboto-Regular.ttf"

        val robotoRegular = "Roboto-Regular.ttf"

        val robotoBold = "Roboto-Bold.ttf"

        val robotoLight = "Roboto-Light.ttf"

        val robotoThin = "Roboto-Thin.ttf"

//         var context: Context? = null

        fun getInstance(context: Context?): FontUtils {
            if (_instance == null) {
                _instance = FontUtils(context!!)
            }
            return _instance!!
        }



    }
    fun getTypeFace(fontName: String?): Typeface? {
        var typeface: Typeface? = null
        if (context != null) typeface = Typeface.createFromAsset(context!!.assets, fontName)
        return typeface
    }
    constructor(context: Context) {
        this.context = context
    }
}