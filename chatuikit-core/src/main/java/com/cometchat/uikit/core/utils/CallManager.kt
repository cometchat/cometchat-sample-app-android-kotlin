package com.cometchat.uikit.core.utils

import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat

/**
 * Singleton object for centralized active call state management.
 * 
 * This replaces the static call storage previously handled by CallingExtension
 * and provides a unified way to track active calls across all UIKit components.
 * 
 * The CallManager:
 * - Stores the currently active Call object
 * - Tracks whether an active meeting (group call) is in progress
 * - Listens to CometChat SDK call events to automatically clear state when calls end
 * - Provides session ID matching logic to ensure only the correct call is cleared
 * 
 * @see CallingState for the legacy implementation pattern
 */
object CallManager {
    
    private const val TAG = "CallManager"
    
    @Volatile
    private var activeCall: Call? = null
    
    @Volatile
    private var isActiveMeeting: Boolean = false
    
    private var listenerId: String? = null
    
    /**
     * Sets the currently active call.
     * 
     * @param call The Call to set as active, or null to clear the active call
     */
    fun setActiveCall(call: Call?) {
        activeCall = call
    }
    
    /**
     * Gets the currently active call.
     * 
     * @return The active Call or null if no call is active
     */
    fun getActiveCall(): Call? = activeCall
    
    /**
     * Clears the active call state.
     * Sets the active call to null.
     */
    fun clearActiveCall() {
        activeCall = null
    }
    
    /**
     * Checks if there is an active call.
     * 
     * @return true if there is an active call, false otherwise
     */
    fun hasActiveCall(): Boolean = activeCall != null
    
    /**
     * Sets whether there is an active meeting (group call) in progress.
     * 
     * @param isActive true if a meeting is active, false otherwise
     */
    fun setIsActiveMeeting(isActive: Boolean) {
        isActiveMeeting = isActive
    }
    
    /**
     * Checks if there is an active meeting (group call) in progress.
     * 
     * @return true if a meeting is active, false otherwise
     */
    fun isActiveMeeting(): Boolean = isActiveMeeting
    
    /**
     * Adds CometChat SDK call listeners to handle call state changes.
     * 
     * The listener will automatically clear the active call when:
     * - onOutgoingCallAccepted is received (call transitions to ongoing)
     * - onOutgoingCallRejected is received and session IDs match
     * - onIncomingCallCancelled is received and session IDs match
     * - onCallEndedMessageReceived is received and session IDs match
     * 
     * This method should be called during UIKit initialization.
     */
    internal fun addListeners() {
        if (listenerId != null) {
            // Listeners already added
            return
        }
        
        listenerId = "CallManager_${System.currentTimeMillis()}"
        
        CometChat.addCallListener(listenerId!!, object : CometChat.CallListener() {
            override fun onIncomingCallReceived(call: Call?) {
                // No action needed - incoming calls are handled by IncomingCallViewModel
            }
            
            override fun onOutgoingCallAccepted(call: Call?) {
                // When outgoing call is accepted, clear the active call
                // as it transitions to an ongoing call state
                clearActiveCall()
            }
            
            override fun onOutgoingCallRejected(call: Call?) {
                // Clear active call only if session IDs match
                call?.let { rejectedCall ->
                    clearActiveCallIfSessionMatches(rejectedCall.sessionId)
                }
            }
            
            override fun onIncomingCallCancelled(call: Call?) {
                // Clear active call only if session IDs match
                call?.let { cancelledCall ->
                    clearActiveCallIfSessionMatches(cancelledCall.sessionId)
                }
            }
            
            override fun onCallEndedMessageReceived(call: Call?) {
                // Clear active call only if session IDs match
                call?.let { endedCall ->
                    clearActiveCallIfSessionMatches(endedCall.sessionId)
                }
            }
        })
    }
    
    /**
     * Removes the CometChat SDK call listeners.
     * 
     * This method should be called during UIKit cleanup.
     */
    internal fun removeListeners() {
        listenerId?.let { id ->
            CometChat.removeCallListener(id)
            listenerId = null
        }
    }
    
    /**
     * Clears the active call if the provided session ID matches the active call's session ID.
     * 
     * @param sessionId The session ID to match against the active call
     */
    private fun clearActiveCallIfSessionMatches(sessionId: String?) {
        if (sessionId.isNullOrEmpty()) {
            return
        }
        
        val currentActiveCall = activeCall
        if (currentActiveCall != null && currentActiveCall.sessionId == sessionId) {
            clearActiveCall()
        }
    }
    
    /**
     * Resets all call state.
     * Clears the active call and resets the active meeting flag.
     * 
     * This is useful for cleanup during logout or app termination.
     */
    fun reset() {
        activeCall = null
        isActiveMeeting = false
    }
}
