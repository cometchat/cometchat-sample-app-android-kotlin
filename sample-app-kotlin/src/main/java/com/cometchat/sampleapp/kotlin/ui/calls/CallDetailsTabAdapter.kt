package com.cometchat.sampleapp.kotlin.ui.calls

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cometchat.calls.model.CallLog
import com.google.gson.Gson

/**
 * ViewPager2 adapter for call details tabs (History, Participants, Recordings).
 *
 * Validates: Requirements 2.7, 2.8
 */
class CallDetailsTabAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    
    private val fragmentList = mutableListOf<Fragment>()
    private val callLogList = mutableListOf<CallLog>()
    private val tabTitleList = mutableListOf<String>()
    
    fun addFragment(fragment: Fragment, tabTitle: String, callLog: CallLog) {
        fragmentList.add(fragment)
        tabTitleList.add(tabTitle)
        callLogList.add(callLog)
    }
    
    override fun createFragment(position: Int): Fragment {
        val fragment = fragmentList[position]
        val callLog = callLogList[position]
        
        val args = Bundle()
        args.putString("callLog", Gson().toJson(callLog))
        args.putString("initiator", Gson().toJson(callLog.initiator))
        args.putString("receiver", Gson().toJson(callLog.receiver))
        fragment.arguments = args
        
        return fragment
    }
    
    override fun getItemCount(): Int = fragmentList.size
    
    fun getTabTitle(position: Int): String = tabTitleList[position]
}
