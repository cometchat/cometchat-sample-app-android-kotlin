package com.cometchat.uikit.core.utils

import com.cometchat.chat.core.Call

/**
 * Utility object for tracking active call state in the UIKit.
 * This is used to prevent initiating new calls when one is already in progress.
 * 
 * Note: This mirrors the functionality of CallingExtension in the chatuikit module
 * but is available in chatuikit-core for use by shared ViewModels.
 */
object CallingState {
    
    @Volatile
    private var activeCall: Call? = null
    
    @Volatile
    private var isActiveMeeting: Boolean = false
    
    /**
     * Gets the currently active call tracked by the UIKit.
     * @return The active Call or null if no call is active
     */
    fun getActiveCall(): Call? = activeCall
    
    /**
     * Sets the currently active call.
     * @param call The Call to set as active, or null to clear
     */
    fun setActiveCall(call: Call?) {
        activeCall = call
    }
    
    /**
     * Checks if there is an active meeting in progress.
     * @return true if a meeting is active, false otherwise
     */
    fun isActiveMeeting(): Boolean = isActiveMeeting
    
    /**
     * Sets the active meeting state.
     * @param isActive true if a meeting is active, false otherwise
     */
    fun setIsActiveMeeting(isActive: Boolean) {
        isActiveMeeting = isActive
    }
    
    /**
     * Clears all active call state.
     */
    fun clear() {
        activeCall = null
        isActiveMeeting = false
    }
}
