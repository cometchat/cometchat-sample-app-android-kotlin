package com.cometchat.uikit.kotlin.presentation.incomingcall

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.cometchat.chat.core.Call

/**
 * A test-only Fragment that hosts CometChatIncomingCall with an injected mock Call.
 *
 * The Call is injected via the companion object before fragment launch.
 * Custom click handlers and error callbacks are set to capture invocations
 * without triggering real SDK calls.
 *
 * Architecture:
 *   [Injected Mock Call] → [Real CometChatIncomingCall View] → [Callback capture]
 *
 * Usage:
 *   IncomingCallHostFragment.injectedCall = mockCall
 *   launchFragmentInContainer<IncomingCallHostFragment>(themeResId = R.style.CometChatTheme_DayNight)
 */
class IncomingCallHostFragment : Fragment() {

    companion object {
        /** Injected before fragment launch — cleared after each test */
        var injectedCall: Call? = null

        /** Callback capture state */
        var onAcceptClickResult: Boolean = false
        var onRejectClickResult: Boolean = false
        var onErrorResult: String? = null

        /** Custom view flags */
        var useCustomItemView: Boolean = false
        var useCustomLeadingView: Boolean = false
        var useCustomTitleView: Boolean = false
        var useCustomSubtitleView: Boolean = false
        var useCustomTrailingView: Boolean = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val incomingCallView = CometChatIncomingCall(requireContext())
        return FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(incomingCallView)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val incomingCallView = (view as FrameLayout).getChildAt(0) as CometChatIncomingCall

        // Disable sound to avoid issues in test environment
        incomingCallView.setDisableSoundForCalls(true)

        // Set custom click handlers to capture callbacks
        incomingCallView.setOnAcceptClickListener {
            onAcceptClickResult = true
        }
        incomingCallView.setOnRejectClickListener {
            onRejectClickResult = true
        }
        incomingCallView.setOnError { exception ->
            onErrorResult = exception.code
        }

        // Apply custom views if flags are set
        if (useCustomItemView) {
            val customView = android.widget.TextView(requireContext()).apply {
                text = "Custom Item View"
                id = View.generateViewId()
            }
            incomingCallView.setItemView(customView)
        }
        if (useCustomLeadingView) {
            val customView = android.widget.TextView(requireContext()).apply {
                text = "Custom Leading View"
                id = View.generateViewId()
            }
            incomingCallView.setLeadingView(customView)
        }
        if (useCustomTitleView) {
            val customView = android.widget.TextView(requireContext()).apply {
                text = "Custom Title View"
                id = View.generateViewId()
            }
            incomingCallView.setTitleView(customView)
        }
        if (useCustomSubtitleView) {
            val customView = android.widget.TextView(requireContext()).apply {
                text = "Custom Subtitle View"
                id = View.generateViewId()
            }
            incomingCallView.setSubtitleView(customView)
        }
        if (useCustomTrailingView) {
            val customView = android.widget.TextView(requireContext()).apply {
                text = "Custom Trailing View"
                id = View.generateViewId()
            }
            incomingCallView.setTrailingView(customView)
        }

        // Set the injected call
        injectedCall?.let { call ->
            incomingCallView.setCall(call)
        }
    }
}
