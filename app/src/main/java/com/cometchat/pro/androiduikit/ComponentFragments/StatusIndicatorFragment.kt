package com.cometchat.pro.androiduikit.ComponentFragments

import android.content.Context
import android.net.Uri
import android.os.Bundle

import androidx.fragment.app.Fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup

import com.cometchat.pro.androiduikit.R
import com.cometchat.pro.uikit.StatusIndicator

class StatusIndicatorFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_status_indicator, container, false)
        val statusIndicator = view.findViewById<StatusIndicator>(R.id.statusIndicator)
        val statusChangeGroup = view.findViewById<RadioGroup>(R.id.statusChange)
        statusIndicator.setUserStatus("online")
        statusChangeGroup.setOnCheckedChangeListener { radioGroup, i ->
            if (i == R.id.online) {
                statusIndicator.setUserStatus("online")
            } else {
                statusIndicator.setUserStatus("offline")
            }
        }
        return view
    }
}
