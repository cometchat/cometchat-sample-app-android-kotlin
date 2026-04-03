package com.cometchat.sampleapp.kotlin.ui.calls

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import com.cometchat.sampleapp.kotlin.databinding.FragmentCallDetailsTabBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch

/**
 * Fragment displaying call history for a specific user.
 * Shows a list of previous calls with the same user.
 *
 * Validates: Requirements 2.7, 2.8
 */
class CallDetailsHistoryFragment : Fragment() {
    
    private val TAG = CallDetailsHistoryFragment::class.java.simpleName
    private var _binding: FragmentCallDetailsTabBinding? = null
    private val binding get() = _binding!!
    private lateinit var callLog: CallLog
    private lateinit var viewModel: CallDetailsHistoryViewModel
    private lateinit var adapter: CallDetailsHistoryAdapter
    private lateinit var layoutManager: LinearLayoutManager
    
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
        initRecyclerView()
        initViewModel()
    }
    
    private fun initRecyclerView() {
        layoutManager = LinearLayoutManager(requireContext())
        binding.rvCallDetails.layoutManager = layoutManager
        adapter = CallDetailsHistoryAdapter(requireContext())
        binding.rvCallDetails.adapter = adapter
        
        binding.rvCallDetails.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    viewModel.fetchCallLogs()
                }
            }
        })
    }
    
    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[CallDetailsHistoryViewModel::class.java]
        viewModel.setCallLog(callLog)
        viewModel.fetchCallLogs()
        
        // Observe UI state using StateFlow
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is CallHistoryUIState.Loading -> {
                            binding.rvCallDetails.visibility = View.GONE
                            binding.emptyStateView.visibility = View.GONE
                        }
                        is CallHistoryUIState.Content -> {
                            binding.rvCallDetails.visibility = View.VISIBLE
                            binding.emptyStateView.visibility = View.GONE
                            adapter.setCallLogs(state.callLogs)
                        }
                        is CallHistoryUIState.Empty -> {
                            binding.rvCallDetails.visibility = View.GONE
                            binding.emptyStateView.visibility = View.VISIBLE
                        }
                        is CallHistoryUIState.Error -> {
                            Log.e(TAG, "Error: ${state.exception}")
                            binding.rvCallDetails.visibility = View.GONE
                            binding.emptyStateView.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
