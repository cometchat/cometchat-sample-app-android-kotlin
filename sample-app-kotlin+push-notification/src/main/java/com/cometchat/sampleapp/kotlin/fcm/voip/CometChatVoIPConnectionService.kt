package com.cometchat.sampleapp.kotlin.fcm.voip

class CometChatVoIPConnectionService : android.telecom.ConnectionService() {
    private var timeoutHandler: android.os.Handler? = null
    private var timeoutRunnable: Runnable? = null

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: android.telecom.PhoneAccountHandle, request: android.telecom.ConnectionRequest
    ): android.telecom.Connection {
        CometChatVoIPUtils.isCallOngoing = true
        val extras = request.extras
        val callerName = extras.getString("callerName", "Unknown Caller")
        val callerAvatar = extras.getString("callerAvatar", "")
        val connection = CometChatVoIPConnection(this)
        connection.extras = extras
        connection.setCallerDisplayName(callerName, android.telecom.TelecomManager.PRESENTATION_ALLOWED)
        connection.setAddress(request.address, android.telecom.TelecomManager.PRESENTATION_ALLOWED)
        connection.setRinging() // Initialize handler and timeout runnable
        timeoutHandler = android.os.Handler()
        timeoutRunnable = Runnable {
            if (connection.state == android.telecom.Connection.STATE_RINGING) {
                connection.setDisconnected(android.telecom.DisconnectCause(android.telecom.DisconnectCause.REJECTED))
                connection.destroy()
            }
        }
        timeoutHandler!!.postDelayed(timeoutRunnable!!, CALL_TIMEOUT.toLong())

        return connection
    }

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: android.telecom.PhoneAccountHandle, request: android.telecom.ConnectionRequest
    ): android.telecom.Connection {
        return CometChatVoIPConnection(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (timeoutHandler != null) {
            timeoutHandler!!.removeCallbacks(timeoutRunnable!!)
        }
        CometChatVoIPUtils.isCallOngoing = false
    }

    companion object {
        private const val CALL_TIMEOUT = 40000 // 40 seconds
    }
}