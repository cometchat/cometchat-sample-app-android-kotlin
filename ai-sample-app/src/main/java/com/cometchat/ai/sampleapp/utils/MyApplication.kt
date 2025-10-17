package com.cometchat.ai.sampleapp.utils

import android.app.Application

/**
 * This is a custom Application class for managing call notifications and
 * handling CometChat events throughout the app lifecycle. It also registers
 * lifecycle callbacks to keep track of the currently active activity.
 */
class MyApplication : Application() {

    companion object {
        var currentOpenChatId: String? = null
    }
}
