package com.cometchat.uikit.kotlin.presentation.calllogs.utils

import android.content.Context
import android.view.View
import com.cometchat.calls.model.CallLog

/**
 * Interface for customizing call log list item views.
 * Allows creating and binding custom views for different parts of the list item.
 */
interface CallLogsViewHolderListener {
    
    /**
     * Creates a custom view for the call log item.
     * Called once when the ViewHolder is created.
     *
     * @param context The context for creating views
     * @param callLog The call log data (may be null during initial creation)
     * @return The custom view to display, or null to use the default view
     */
    fun createView(context: Context, callLog: CallLog?): View?
    
    /**
     * Binds data to the custom view.
     * Called each time the ViewHolder is bound to new data.
     *
     * @param context The context
     * @param view The custom view created by createView()
     * @param callLog The call log data to bind
     */
    fun bindView(context: Context, view: View, callLog: CallLog)
}
