package com.cometchat.sampleapp.kotlin.push.appflow.fragments

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
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.kotlin.calls.CometChatCallActivity
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.sampleapp.kotlin.push.R
import com.cometchat.sampleapp.kotlin.push.appflow.CallDetailsActivity
import com.cometchat.sampleapp.kotlin.push.appflow.viewmodels.CallsFragmentViewModel
import com.cometchat.sampleapp.kotlin.push.databinding.FragmentCallsBinding
import com.google.gson.Gson

/**
 * Fragment displaying the CometChatCallLogs component.
 * Handles call log clicks to navigate to CallDetailsActivity.
 */
class CallsFragment : Fragment() {

    private var _binding: FragmentCallsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: CallsFragmentViewModel
    private var isCallActive = false
    private var enableAutoRefresh = false
    
    private val TAG = "CallsFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCallsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initClickListeners()
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

    private fun initViewModel() {
        viewModel = ViewModelProvider.NewInstanceFactory().create(CallsFragmentViewModel::class.java)
        viewModel.onCallStart().observe(viewLifecycleOwner, onCallStart())
        viewModel.onError().observe(viewLifecycleOwner, onError())
    }

    private fun onCallStart(): Observer<Call> {
        return Observer { call: Call? ->
            call?.let {
                CometChatCallActivity.launchOutgoingCallScreen(requireContext(), it, null)
            }
            Log.d(TAG, "Call started: ${call?.sessionId}")
        }
    }

    private fun onError(): Observer<CometChatException> {
        return Observer { e: CometChatException ->
            context?.let { ctx ->
                Toast.makeText(ctx, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initClickListeners() {
        // Set up item click listener for call logs
        binding.callLog.setOnItemClick { callLog ->
            val intent = Intent(context, CallDetailsActivity::class.java)
            intent.putExtra("callLog", Gson().toJson(callLog))
            intent.putExtra("initiator", Gson().toJson(callLog.initiator))
            intent.putExtra("receiver", Gson().toJson(callLog.receiver))
            startActivity(intent)
        }

        // Set up call type icon click listener
        binding.callLog.setOnCallTypeIconClick { callLog ->
            if (!isCallActive) {
                isCallActive = true
                val listener = object : CometChat.CallbackListener<Void>() {
                    override fun onSuccess(unused: Void?) {
                        isCallActive = false
                    }

                    override fun onError(e: CometChatException) {
                        isCallActive = false
                        Log.e(TAG, "Failed to initiate call: ${e.message}")
                    }
                }
                if (callLog.type == CometChatCallsConstants.CALL_TYPE_AUDIO) {
                    viewModel.startCall(CometChatConstants.CALL_TYPE_AUDIO, callLog, listener)
                } else if (callLog.type == CometChatCallsConstants.CALL_TYPE_VIDEO) {
                    viewModel.startCall(CometChatConstants.CALL_TYPE_VIDEO, callLog, listener)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val TAG: String = CallsFragment::class.java.simpleName
    }
}
