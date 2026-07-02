package com.cometchat.uikit.kotlin.presentation.outgoingcall

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cometchat.chat.core.Call
import com.cometchat.uikit.kotlin.shared.interfaces.Function2

/**
 * A test-only Fragment that hosts CometChatOutgoingCall with an injected mock Call.
 *
 * The Call is injected via the companion object before fragment launch.
 * Custom click handlers and error callbacks are set to capture invocations
 * without triggering real SDK calls.
 *
 * Architecture:
 *   [Injected Mock Call] → [Real CometChatOutgoingCall View] → [Callback capture]
 *
 * Usage:
 *   OutgoingCallHostFragment.injectedCall = mockCall
 *   launchFragmentInContainer<OutgoingCallHostFragment>(themeResId = R.style.CometChatTheme_DayNight)
 */
class OutgoingCallHostFragment : Fragment() {

    companion object {
        /** Injected before fragment launch — cleared after each test */
        var injectedCall: Call? = null

        /** Callback capture state */
        var onEndCallClickResult: Boolean = false
        var onErrorResult: String? = null

        /** Custom view flags */
        var useCustomTitleView: Boolean = false
        var useCustomSubtitleView: Boolean = false
        var useCustomAvatarView: Boolean = false
        var useCustomEndCallView: Boolean = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val outgoingCallView = CometChatOutgoingCall(requireContext())
        return FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            addView(outgoingCallView)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val outgoingCallView = (view as FrameLayout).getChildAt(0) as CometChatOutgoingCall

        // Disable sound to avoid issues in test environment
        outgoingCallView.setDisableSoundForCalls(true)

        // Set custom click handler to capture callback
        outgoingCallView.setOnEndCallClickListener {
            onEndCallClickResult = true
        }

        // Set error callback
        outgoingCallView.setOnError { exception ->
            onErrorResult = exception.code
        }

        // Set the injected call FIRST (custom views need call to be non-null)
        injectedCall?.let { call ->
            outgoingCallView.setCall(call)
        }

        // Apply custom views if flags are set (AFTER setCall so call is non-null)
        if (useCustomTitleView) {
            outgoingCallView.setTitleView(Function2 { ctx, _ ->
                TextView(ctx).apply {
                    text = "Custom Title View"
                    id = View.generateViewId()
                }
            })
        }
        if (useCustomSubtitleView) {
            outgoingCallView.setSubtitleView(Function2 { ctx, _ ->
                TextView(ctx).apply {
                    text = "Custom Subtitle View"
                    id = View.generateViewId()
                }
            })
        }
        if (useCustomAvatarView) {
            outgoingCallView.setAvatarView(Function2 { ctx, _ ->
                TextView(ctx).apply {
                    text = "Custom Avatar View"
                    id = View.generateViewId()
                }
            })
        }
        if (useCustomEndCallView) {
            outgoingCallView.setEndCallView(Function2 { ctx, _ ->
                TextView(ctx).apply {
                    text = "Custom End Call View"
                    id = View.generateViewId()
                }
            })
        }
    }
}
