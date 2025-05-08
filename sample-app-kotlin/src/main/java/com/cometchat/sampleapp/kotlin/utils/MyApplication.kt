package com.cometchat.sampleapp.kotlin.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupWindow
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.chatuikit.CometChatTheme
import com.cometchat.chatuikit.calls.CallingExtension
import com.cometchat.chatuikit.calls.CometChatCallActivity
import com.cometchat.chatuikit.calls.incomingcall.CometChatIncomingCall
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper
import com.cometchat.chatuikit.shared.events.CometChatCallEvents
import com.cometchat.chatuikit.shared.interfaces.OnError
import com.cometchat.chatuikit.shared.resources.soundmanager.CometChatSoundManager
import com.cometchat.chatuikit.shared.resources.soundmanager.Sound
import com.cometchat.chatuikit.shared.resources.utils.Utils
import com.cometchat.sampleapp.kotlin.data.repository.Repository
import com.google.android.material.snackbar.Snackbar

/**
 * This is a custom Application class for managing call notifications and
 * handling CometChat events throughout the app lifecycle. It also registers
 * lifecycle callbacks to keep track of the currently active activity.
 */
class MyApplication : Application() {
    private var currentActivity: Activity? = null
    private var snackBar: Snackbar? = null
    private lateinit var soundManager: CometChatSoundManager
    private var tempCall: Call? = null

    /**
     * Initializes the application by setting up Firebase, adding the CometChat call
     * listener, and registering activity lifecycle callbacks.
     */
    override fun onCreate() {
        super.onCreate()

        soundManager = CometChatSoundManager(this)

        addCallListener()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var isActivityChangingConfigurations = false
            private var activityReferences = 0

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                currentActivity = activity
                if (currentActivity is CometChatCallActivity) {
                    Utils.setStatusBarColor(
                        currentActivity, CometChatTheme.getBackgroundColor3(activity)
                    )
                } else {
                    Utils.setStatusBarColor(
                        currentActivity, CometChatTheme.getBackgroundColor1(activity)
                    )
                }
            }

            override fun onActivityStarted(activity: Activity) {
                currentActivity = activity
                if (++activityReferences == 1 && !isActivityChangingConfigurations) {
                    isAppInForeground = true
                }
            }

            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
                if (snackBar != null && tempCall != null) {
                    showTopSnackBar(tempCall)
                } else
                    dismissTopSnackBar()
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
                isActivityChangingConfigurations = activity.isChangingConfigurations
                if (--activityReferences == 0 && !isActivityChangingConfigurations) {
                    isAppInForeground = false
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
                if (currentActivity === activity) {
                    currentActivity = null
                }
            }
        })
    }

    /**
     * Adds a call listener using CometChat to handle incoming and outgoing call
     * events.
     */
    private fun addCallListener() {
        CometChat.addCallListener(LISTENER_ID, object : CometChat.CallListener() {
            override fun onIncomingCallReceived(call: Call) {
                playSound()
                launchIncomingCallPopup(call)
            }

            override fun onOutgoingCallAccepted(call: Call) {
                dismissTopSnackBar()
            }

            override fun onOutgoingCallRejected(call: Call) {
                dismissTopSnackBar()
            }

            override fun onIncomingCallCancelled(call: Call) {
                dismissTopSnackBar()
            }

            override fun onCallEndedMessageReceived(call: Call) {
                dismissTopSnackBar()
            }
        })

        CometChatCallEvents.addListener(LISTENER_ID, object : CometChatCallEvents() {
            override fun ccCallAccepted(call: Call) {
                dismissTopSnackBar()
            }

            override fun ccCallRejected(call: Call) {
                dismissTopSnackBar()
            }
        })
    }

    /**
     * Displays a custom SnackBar notification at the top of the screen for incoming
     * calls.
     *
     * @param call The call object representing the incoming call.
     */
    @SuppressLint("RestrictedApi")
    fun showTopSnackBar(call: Call?) {
        if (currentActivity == null && call == null) return
        tempCall = call // Get the root view of the current activity
        val rootView: View = currentActivity!!.findViewById(android.R.id.content)


        val cometChatIncomingCall = CometChatIncomingCall(currentActivity)
        cometChatIncomingCall.disableSoundForCalls(true)
        cometChatIncomingCall.call = call!!
        cometChatIncomingCall.onError = OnError { cometchatException: CometChatException? -> dismissTopSnackBar() }


        snackBar = Snackbar.make(rootView, " ", Snackbar.LENGTH_INDEFINITE)
        val layout: Snackbar.SnackbarLayout = snackBar?.view as Snackbar.SnackbarLayout
        val params: FrameLayout.LayoutParams = layout.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        params.topMargin = Utils.convertDpToPx(this, 35)
        layout.setLayoutParams(params)
        layout.setBackgroundColor(
            currentActivity!!.resources.getColor(android.R.color.transparent, null)
        )

        layout.addView(cometChatIncomingCall, 0)

        for (popupWindow in popupWindows) {
            if (popupWindow.isShowing) popupWindow.dismiss()
        }
        popupWindows.clear()
        snackBar!!.show()
    }

    /**
     * Dismisses the current top SnackBar if it's being shown and resets any related
     * call state.
     */
    fun dismissTopSnackBar() {
        if (snackBar != null && snackBar!!.isShown) {
            snackBar!!.dismiss()
            snackBar = null
            tempCall = null
            CallingExtension.setActiveCall(null)
        }
        pauseSound()
    }

    /**
     * Launches an incoming call popup when an incoming call is received.
     *
     * @param call The call object representing the incoming call.
     */
    fun launchIncomingCallPopup(call: Call) {
        if (call.callInitiator is User) {
            val callInitiator = call.callInitiator as User
            if (CometChatUIKit.getLoggedInUser().uid.equals(callInitiator.uid, ignoreCase = true)) return
        }

        if (CometChat.getActiveCall() == null && CallingExtension.getActiveCall() == null && !CallingExtension.isActiveMeeting()) {
            CallingExtension.setActiveCall(call)
            showTopSnackBar(call)
        } else {
            rejectCallWithBusyStatus(call)
        }
    }

    /**
     * Rejects an incoming call with a "busy" status if another call is active.
     *
     * @param call The call object representing the call to be rejected.
     */
    private fun rejectCallWithBusyStatus(call: Call) {
        Thread {
            try {
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
            Repository.rejectCallWithBusyStatus(call, object : CometChat.CallbackListener<Call>() {
                override fun onSuccess(call: Call) {
                    CometChatUIKitHelper.onCallRejected(call)
                }

                override fun onError(e: CometChatException) {
                }
            })
        }.start()
    }

    /**
     * Plays the sound for an incoming call notification.
     */
    private fun playSound() {
        soundManager.play(Sound.incomingCall)
    }

    /**
     * Silently pauses any currently playing sound.
     */
    private fun pauseSound() {
        soundManager.pauseSilently()
    }

    companion object {
        var popupWindows = ArrayList<PopupWindow>()
        private var LISTENER_ID: String = System.currentTimeMillis().toString()
        var currentOpenChatId: String? = null
        var isAppInForeground: Boolean = false
    }
}
