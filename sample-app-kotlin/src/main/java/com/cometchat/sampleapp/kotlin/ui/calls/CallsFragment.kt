package com.cometchat.sampleapp.kotlin.ui.calls

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.cometchat.calls.constants.CometChatCallsConstants
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.calls.CometChatCallActivity
import com.cometchat.sampleapp.kotlin.databinding.FragmentCallLogsBinding
import com.google.gson.Gson

/**
 * Fragment displaying the list of call logs (Calls tab).
 *
 * This fragment uses the CometChatCallLogs component from chatuikit-kotlin
 * to display call history. It handles:
 * - Displaying call logs with caller/callee info, type, status, and timestamp
 * - Navigation to CallDetailsActivity on call log tap
 * - Call initiation on call icon click
 * - Loading and empty states
 *
 * ## Architecture:
 * - Uses ViewBinding for type-safe view access
 * - Uses CallsViewModel for call initiation logic
 * - Delegates UI rendering to CometChatCallLogs component
 *
 * Validates: Requirements 9.1, 9.2, 9.3, 9.4, 10.2
 */
class CallsFragment : Fragment() {

    private var _binding: FragmentCallLogsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CallsViewModel
    
    /** Flag to prevent multiple simultaneous call initiations */
    private var isCallActive = false
    private var enableAutoRefresh = false

    companion object {
        private const val TAG = "CallsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCallLogsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setupCallLogs()
    }

    override fun onResume() {
        super.onResume()
        if (enableAutoRefresh) {
            enableAutoRefresh = false
            isCallActive = false
        }
    }

    override fun onPause() {
        super.onPause()
        enableAutoRefresh = true
    }

    /**
     * Initializes the ViewModel and observes its LiveData.
     */
    private fun initViewModel() {
        viewModel = ViewModelProvider.NewInstanceFactory().create(CallsViewModel::class.java)
        viewModel.onCallStart().observe(viewLifecycleOwner, onCallStart())
        viewModel.onError().observe(viewLifecycleOwner, onError())
    }

    /**
     * Observer for successful call initiation.
     */
    private fun onCallStart(): Observer<Call> {
        return Observer { call: Call? ->
            call?.let {
                CometChatCallActivity.launchOutgoingCallScreen(requireContext(), it, null)
            }
            Log.d(TAG, "Call started: ${call?.sessionId}")
        }
    }

    /**
     * Observer for call initiation errors.
     */
    private fun onError(): Observer<CometChatException> {
        return Observer { e: CometChatException ->
            context?.let { ctx ->
                Toast.makeText(ctx, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Sets up the CometChatCallLogs component.
     *
     * Configures click handlers for:
     * - Call log item clicks -> Navigate to CallDetailsActivity
     * - Call icon clicks -> Initiate call of same type
     *
     * Validates: Requirements 9.1, 9.2, 9.3, 9.4
     */
    private fun setupCallLogs() {
        binding.callLogs.apply {
            // Hide back icon since this is a tab (Requirement 9.1)
            setBackIconVisibility(View.GONE)

            // Set click handler for call log items -> Navigate to CallDetailsActivity (Requirement 9.2)
            setOnItemClick { callLog ->
                navigateToCallDetails(callLog)
            }

            // Set click handler for call icon -> Initiate call (Requirements 9.3, 9.4)
            setOnCallTypeIconClick { callLog ->
                handleCallIconClick(callLog)
            }

            // Handle errors
            setOnError { exception ->
                Log.e(TAG, "Error loading call logs: ${exception.message}")
            }
        }
    }

    /**
     * Navigates to CallDetailsActivity with the call log data.
     *
     * Serializes the call log, initiator, and receiver as JSON extras.
     *
     * @param callLog The selected call log
     *
     * Validates: Requirement 9.2
     */
    private fun navigateToCallDetails(callLog: CallLog) {
        val intent = Intent(context, CallDetailsActivity::class.java)
        intent.putExtra("callLog", Gson().toJson(callLog))
        intent.putExtra("initiator", Gson().toJson(callLog.initiator))
        intent.putExtra("receiver", Gson().toJson(callLog.receiver))
        startActivity(intent)
    }

    /**
     * Handles call icon click to initiate a call of the same type.
     *
     * @param callLog The call log to base the new call on
     *
     * Validates: Requirements 9.3, 9.4
     */
    private fun handleCallIconClick(callLog: CallLog) {
        if (isCallActive) return
        
        isCallActive = true
        
        // Determine the call type from the call log (Requirement 9.3)
        val callType = when (callLog.type) {
            CometChatCallsConstants.CALL_TYPE_VIDEO -> CometChatConstants.CALL_TYPE_VIDEO
            else -> CometChatConstants.CALL_TYPE_AUDIO
        }
        
        val listener = object : CometChat.CallbackListener<Void>() {
            override fun onSuccess(unused: Void?) {
                isCallActive = false
            }

            override fun onError(e: CometChatException) {
                isCallActive = false
                Log.e(TAG, "Failed to initiate call: ${e.message}")
            }
        }
        
        viewModel.startCall(callType, callLog, listener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
