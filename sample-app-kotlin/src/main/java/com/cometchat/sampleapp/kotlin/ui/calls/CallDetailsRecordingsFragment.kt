package com.cometchat.sampleapp.kotlin.ui.calls

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import com.cometchat.sampleapp.kotlin.databinding.FragmentCallDetailsTabBinding
import com.google.gson.Gson

/**
 * Fragment displaying call recordings for a specific call.
 * Shows a list of recordings if available, otherwise shows empty state.
 *
 * Validates: Requirements 2.7, 2.8
 */
class CallDetailsRecordingsFragment : Fragment() {
    
    private var _binding: FragmentCallDetailsTabBinding? = null
    private val binding get() = _binding!!
    private lateinit var callLog: CallLog
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            callLog = Gson().fromJson(it.getString("callLog"), CallLog::class.java)
            callLog.initiator = Gson().fromJson(it.getString("initiator"), CallUser::class.java)
            callLog.receiver = Gson().fromJson(it.getString("receiver"), CallUser::class.java)
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCallDetailsTabBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvCallDetails.layoutManager = LinearLayoutManager(context)
        
        if (callLog.isHasRecording) {
            binding.rvCallDetails.visibility = View.VISIBLE
            binding.emptyStateView.visibility = View.GONE
            // TODO: Add recordings adapter when recordings API is available
        } else {
            binding.rvCallDetails.visibility = View.GONE
            binding.emptyStateView.visibility = View.VISIBLE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
