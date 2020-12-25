package listeners

import android.view.View

public abstract class ReactionClickListener {
    open fun onClick(var1: View?) {}
    open fun onCancel(var1: View?) {}
}