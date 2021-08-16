package com.cometchat.pro.uikit.ui_components.chats.delete_chat

import android.graphics.*

class MyButton(
    private val listener: MyButtonClickListener
){
    private var pos:Int = 0
    private var clickRegion: RectF? = null

    fun onClick(x:Float,y:Float) : Boolean{
        if(clickRegion != null && clickRegion!!.contains(x,y)){
            listener.onClick(pos)
            return true
        }
        return false
    }

    fun onDraw(c:Canvas,rectF:RectF,pos:Int){
        clickRegion = rectF
        this.pos = pos
    }
}
