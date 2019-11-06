package com.inscripts.cometchatpulse.Extensions

import android.graphics.Typeface
import android.support.v7.widget.Toolbar
import android.widget.TextView


fun Toolbar.setTitleTypeface(typeface: Typeface){

    for (i in 0 until childCount)
    {
        val view=getChildAt(i)
        if (view is TextView && view.text==title){
            view.typeface=typeface
            break
        }
    }
}
