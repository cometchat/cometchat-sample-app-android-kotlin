package com.inscripts.cometchatpulse.Extensions

import android.graphics.Typeface
import androidx.appcompat.widget.Toolbar
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
