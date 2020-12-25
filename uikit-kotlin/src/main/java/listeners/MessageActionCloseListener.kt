package listeners

import android.content.DialogInterface

public interface MessageActionCloseListener {
    fun handleDialogClose(dialog: DialogInterface?)
}
