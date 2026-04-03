package com.cometchat.uikit.kotlin.presentation.ongoingcall.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.constants.UIKitConstants.CallWorkFlow
import com.cometchat.uikit.core.models.OngoingCallEvent
import com.cometchat.uikit.core.models.OngoingCallUIState
import com.cometchat.uikit.core.utils.CallingState
import com.cometchat.uikit.core.viewmodel.CometChatOngoingCallViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.ongoingcall.style.CometChatOngoingCallStyle
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * CometChatOngoingCall is a custom view that displays an active voice/video call interface.
 *
 * This component:
 * - Shows a loading indicator while connecting to the call
 * - Hosts the CometChat Calls SDK's call UI in a RelativeLayout container
 * - Handles call lifecycle events (end, timeout, user join/leave)
 * - Implements DefaultLifecycleObserver for proper lifecycle management
 *
 * **Validates: Requirements 16.1, 16.2, 16.3, 16.5**
 *
 * Usage in XML:
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.ongoingcall.ui.CometChatOngoingCall
 *     android:id="@+id/ongoing_call"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent" />
 * ```
 *
 * Usage in Kotlin:
 * ```kotlin
 * val ongoingCall = CometChatOngoingCall(context)
 * ongoingCall.setSessionId(sessionId)
 * ongoingCall.setCallType(callType)
 * ongoingCall.setCallSettingsBuilder(builder)
 * ongoingCall.startCall()
 * ```
 */
class CometChatOngoingCall @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr), DefaultLifecycleObserver {

    companion object {
        private const val TAG = "CometChatOngoingCall"
    }

    // ==================== Views ====================
    private lateinit var progressBar: ProgressBar
    private lateinit var callView: RelativeLayout


    // ==================== State ====================
    private var viewModel: CometChatOngoingCallViewModel? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var viewScope: CoroutineScope? = null
    private var activity: Activity? = null

    // ==================== Callbacks ====================
    private var onError: ((CometChatException) -> Unit)? = null

    // ==================== Style ====================
    private var style: CometChatOngoingCallStyle

    // ==================== Observer Jobs ====================
    private var uiStateJob: Job? = null
    private var eventsJob: Job? = null

    init {
        style = CometChatOngoingCallStyle.default(context)
        activity = Utils.getActivity(context)

        if (!isInEditMode) {
            initializeView()
            initViewModel()
            // Create default CallSettingsBuilder (matching Java implementation behavior)
            activity?.let { setCallSettingsBuilder(CometChatCalls.CallSettingsBuilder(it)) }
            // Request the necessary permissions (matching Java implementation)
            requestCallPermissions()
        }
    }
    
    /**
     * Requests RECORD_AUDIO and CAMERA permissions required for calls.
     * Matches the Java implementation behavior.
     */
    private fun requestCallPermissions() {
        activity?.let { act ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val permissions = arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
                )
                act.requestPermissions(permissions, 101)
            }
        }
    }

    // ==================== Initialization ====================

    /**
     * Initializes the view hierarchy by inflating the layout.
     * Layout contains:
     * - ProgressBar for loading state
     * - RelativeLayout call_view for hosting SDK call UI
     *
     * **Validates: Requirements 16.3**
     */
    private fun initializeView() {
        Utils.initMaterialCard(this)

        // Inflate the layout
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.cometchat_ongoing_call_screen, this, true)

        // Find views
        progressBar = findViewById(R.id.progress_bar)
        callView = findViewById(R.id.call_view)

        applyStyle()
    }

    /**
     * Initializes the ViewModel and sets up lifecycle observation.
     */
    private fun initViewModel() {
        lifecycleOwner = Utils.getLifecycleOwner(context)
        if (lifecycleOwner == null) return

        viewScope = lifecycleOwner?.lifecycleScope

        // Create ViewModel
        val viewModelStoreOwner = lifecycleOwner as? ViewModelStoreOwner
        viewModel = if (viewModelStoreOwner != null) {
            ViewModelProvider(viewModelStoreOwner)[CometChatOngoingCallViewModel::class.java]
        } else {
            CometChatOngoingCallViewModel()
        }
    }

    // ==================== Public Configuration Methods ====================

    /**
     * Sets the call session ID.
     *
     * **Validates: Requirement 2.1**
     *
     * @param sessionId The unique identifier for the call session
     */
    fun setSessionId(sessionId: String) {
        viewModel?.setSessionId(sessionId)
    }

    /**
     * Sets the call type.
     *
     * **Validates: Requirement 2.2**
     *
     * @param callType The type of call ("audio" or "video")
     */
    fun setCallType(callType: String) {
        viewModel?.setCallType(callType)
    }

    /**
     * Sets the call workflow.
     *
     * **Validates: Requirement 2.4**
     *
     * @param workFlow DEFAULT for 1:1 calls, MEETING for group calls
     */
    fun setCallWorkFlow(workFlow: CallWorkFlow) {
        viewModel?.setCallWorkFlow(workFlow)
    }

    /**
     * Sets the custom call settings builder.
     *
     * **Validates: Requirement 2.3**
     *
     * @param builder The CometChatCalls.CallSettingsBuilder to use for call configuration
     */
    fun setCallSettingsBuilder(builder: CometChatCalls.CallSettingsBuilder?) {
        viewModel?.setCallSettingsBuilder(builder)
    }

    /**
     * Starts the call session.
     * Must be called after setting sessionId, callType, and callSettingsBuilder.
     *
     * **Validates: Requirements 3.1-3.8**
     */
    fun startCall() {
        viewModel?.startCall(callView)
    }

    /**
     * Sets the error callback.
     *
     * **Validates: Requirement 14.1**
     *
     * @param onError The error callback
     */
    fun setOnError(onError: ((CometChatException) -> Unit)?) {
        this.onError = onError
    }

    /**
     * Gets the error callback.
     *
     * @return The current error callback
     */
    fun getOnError(): ((CometChatException) -> Unit)? = onError

    /**
     * Sets the style for this component.
     *
     * **Validates: Requirement 16.5**
     *
     * @param style The CometChatOngoingCallStyle to apply
     */
    fun setStyle(style: CometChatOngoingCallStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Gets the current style.
     *
     * @return The current CometChatOngoingCallStyle
     */
    fun getStyle(): CometChatOngoingCallStyle = style

    /**
     * Gets the ViewModel associated with this component.
     *
     * @return The CometChatOngoingCallViewModel instance
     */
    fun getViewModel(): CometChatOngoingCallViewModel? = viewModel

    /**
     * Gets the call view container.
     *
     * @return The RelativeLayout container for the call UI
     */
    fun getCallView(): RelativeLayout = callView

    // ==================== Lifecycle Methods ====================

    /**
     * Called when the view is attached to a window.
     * Adds listeners and attaches lifecycle observer.
     *
     * **Validates: Requirements 13.1, 11.3**
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewModel?.addListeners()
        lifecycleOwner?.lifecycle?.addObserver(this)
        attachObservers()
    }

    /**
     * Called when the view is detached from a window.
     * Removes listeners and disposes observers.
     *
     * **Validates: Requirements 13.2, 11.4**
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewModel?.removeListeners()
        lifecycleOwner?.lifecycle?.removeObserver(this)
        disposeObservers()
    }

    /**
     * Called when the lifecycle owner is stopped.
     * Handles PIP exit if applicable.
     *
     * **Validates: Requirement 10.1-10.4**
     */
    override fun onStop(owner: LifecycleOwner) {
        // PIP exit handling is done in the Activity
    }

    /**
     * Called when the lifecycle owner is destroyed.
     * Removes lifecycle observer.
     *
     * **Validates: Requirement 11.3**
     */
    override fun onDestroy(owner: LifecycleOwner) {
        lifecycleOwner?.lifecycle?.removeObserver(this)
    }

    // ==================== Observer Management ====================

    /**
     * Attaches observers to the ViewModel StateFlows.
     *
     * **Validates: Requirements 4.1, 4.2, 5.1, 5.2, 5.3, 14.5, 16.4**
     */
    fun attachObservers() {
        val scope = viewScope ?: return
        val vm = viewModel ?: return

        // Observe UI state
        uiStateJob = scope.launch {
            vm.uiState.collect { state ->
                handleUIState(state)
            }
        }

        // Observe events
        eventsJob = scope.launch {
            vm.events.collect { event ->
                handleEvent(event)
            }
        }
    }

    /**
     * Disposes observers from the ViewModel.
     *
     * **Validates: Requirement 11.3, 11.4**
     */
    fun disposeObservers() {
        uiStateJob?.cancel()
        eventsJob?.cancel()
        uiStateJob = null
        eventsJob = null
    }

    // ==================== Private Methods ====================

    /**
     * Applies the current style to all views.
     */
    private fun applyStyle() {
        // Container styling
        setCardBackgroundColor(style.backgroundColor)
        radius = style.cornerRadius
        strokeWidth = style.strokeWidth
        strokeColor = style.strokeColor

        // Progress indicator styling
        progressBar.indeterminateTintList = ColorStateList.valueOf(style.progressIndicatorColor)
    }


    /**
     * Handles UI state changes from the ViewModel.
     *
     * **Validates: Requirements 4.1, 4.2, 5.1, 5.2, 5.3**
     *
     * @param state The current OngoingCallUIState
     */
    private fun handleUIState(state: OngoingCallUIState) {
        when (state) {
            is OngoingCallUIState.Loading -> {
                // Show progress bar, hide call view (Requirements 4.1, 4.2)
                progressBar.visibility = View.VISIBLE
                callView.visibility = View.GONE
            }
            is OngoingCallUIState.Connected -> {
                // Hide progress bar, show call view (Requirements 5.1, 5.2)
                progressBar.visibility = View.GONE
                callView.visibility = View.VISIBLE

                // Set status bar color (Requirement 5.3)
                setStatusBarColor()
            }
            is OngoingCallUIState.Ended -> {
                // Call ended - activity will handle finishing
            }
            is OngoingCallUIState.Error -> {
                // Error state - invoke callback
                onError?.invoke(state.exception)
            }
        }
    }

    /**
     * Handles events from the ViewModel.
     *
     * **Validates: Requirements 1.7, 2.1, 2.2, 2.3, 2.7, 14.5**
     *
     * @param event The OngoingCallEvent
     */
    private fun handleEvent(event: OngoingCallEvent) {
        when (event) {
            is OngoingCallEvent.CallEnded -> {
                // Call ended event - finish activity directly (matching Java's endCall(Boolean) method)
                if (Utils.isActivityUsable(activity)) {
                    activity?.finish()
                }
            }
            is OngoingCallEvent.SessionTimeout -> {
                // Session timeout - finish activity directly (matching Java behavior)
                if (Utils.isActivityUsable(activity)) {
                    activity?.finish()
                }
            }
            is OngoingCallEvent.UserJoined -> {
                // User joined - handle PIP mode if needed
                if (event.isCurrentUser) {
                    // Activity handles PIP mode entry
                }
            }
            is OngoingCallEvent.UserLeft -> {
                // User left - no action needed in view
            }
            is OngoingCallEvent.Error -> {
                // Error event - invoke callback (Requirement 14.5)
                onError?.invoke(event.exception)
            }
        }
    }

    /**
     * Sets the status bar color to the calling background color.
     *
     * **Validates: Requirement 5.3**
     */
    private fun setStatusBarColor() {
        if (activity != null && !activity!!.isFinishing && !activity!!.isDestroyed) {
            val callingBackgroundColor = ContextCompat.getColor(context, R.color.cometchat_calling_background)
            activity!!.window.statusBarColor = callingBackgroundColor
        }
    }

    /**
     * Handles PIP exit by ending the call appropriately based on workflow.
     * Called by the hosting Activity when PIP mode is exited.
     *
     * **Validates: Requirements 10.1, 10.2, 10.3, 10.4**
     *
     * @param callWorkFlow The current call workflow
     */
    fun handlePiPExit(callWorkFlow: CallWorkFlow) {
        viewModel?.removeListeners()
        if (callWorkFlow != CallWorkFlow.MEETING) {
            viewModel?.endCall()
            CometChat.clearActiveCall()
        } else {
            CometChatCalls.endSession()
        }
        CallingState.setIsActiveMeeting(false)
    }
}
