package com.cometchat.pro.uikit.ui_components.messages.live_reaction

import android.view.MotionEvent
import android.view.View

abstract class LiveReactionListener(reactionClickListener: ReactionClickListener) : View.OnTouchListener{

    var reactionClickListener: ReactionClickListener = reactionClickListener
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                v?.animate()?.scaleX(1.5f)?.setDuration(300)?.start()
                v?.animate()?.scaleY(1.5f)?.setDuration(300)?.start()
                reactionClickListener.onClick(v)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                v?.animate()?.scaleX(1f)?.setDuration(300)?.start()
                v?.animate()?.scaleY(1f)?.setDuration(300)?.start()
                reactionClickListener.onCancel(v)
                return true
            }
        }
        return false
    }
}