package com.cometchat.pro.androiduikit.ComponentFragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast

import androidx.fragment.app.Fragment

import com.cometchat.pro.androiduikit.R
import com.cometchat.pro.uikit.BadgeCount
import com.cometchat.pro.uikit.StatusIndicator
import com.google.android.material.textfield.TextInputEditText

class BadgeCountFragment : Fragment() {

    private var count = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_badge_count, container, false)
        val badgeCount = view.findViewById<BadgeCount>(R.id.badgeCount)
        val badgecountedt = view.findViewById<TextInputEditText>(R.id.badgeCount_edt)
        val countSize = view.findViewById<TextInputEditText>(R.id.countSize)
        countSize.setText(12.toString())
        badgecountedt.setText(1.toString())
        badgecountedt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.length > 0 && charSequence.length < 7) {
                    count = Integer.parseInt(charSequence.toString())
                    badgeCount.setCount(Integer.parseInt(charSequence.toString()))
                }

            }

            override fun afterTextChanged(editable: Editable) {

            }
        })
        countSize.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.length > 0 && Integer.parseInt(charSequence.toString()) < 32)
                    badgeCount.setCountSize(java.lang.Float.parseFloat(charSequence.toString()))
                else if (charSequence.length == 0) {
                    badgeCount.setCountSize(12f)
                }
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })
        view.findViewById<View>(R.id.bd_red).setOnClickListener {
            badgeCount.setCountBackground(resources.getColor(R.color.red))
            refreshbadgeCount(badgeCount)
        }
        view.findViewById<View>(R.id.bd_yellow).setOnClickListener {
            badgeCount.setCountBackground(resources.getColor(R.color.yellow))
            refreshbadgeCount(badgeCount)
        }
        view.findViewById<View>(R.id.bd_purple).setOnClickListener {
            badgeCount.setCountBackground(resources.getColor(R.color.purple))
            refreshbadgeCount(badgeCount)
        }
        view.findViewById<View>(R.id.bd_green).setOnClickListener {
            badgeCount.setCountBackground(resources.getColor(R.color.green))
            refreshbadgeCount(badgeCount)
        }
        view.findViewById<View>(R.id.bd_blue).setOnClickListener {
            badgeCount.setCountBackground(resources.getColor(R.color.blue))
            refreshbadgeCount(badgeCount)
        }
        view.findViewById<View>(R.id.bd_violet).setOnClickListener {
            badgeCount.setCountBackground(resources.getColor(R.color.violet))
            refreshbadgeCount(badgeCount)
        }

        /**/
        view.findViewById<View>(R.id.count_red).setOnClickListener {
            badgeCount.setCountColor(resources.getColor(R.color.red))
            refreshbadgeCount(badgeCount)
        }
        view.findViewById<View>(R.id.count_yellow).setOnClickListener {
            badgeCount.setCountColor(resources.getColor(R.color.yellow))
            refreshbadgeCount(badgeCount)
        }
        view.findViewById<View>(R.id.count_purple).setOnClickListener {
            badgeCount.setCountColor(resources.getColor(R.color.purple))
            refreshbadgeCount(badgeCount)
        }
        view.findViewById<View>(R.id.count_green).setOnClickListener {
            badgeCount.setCountColor(resources.getColor(R.color.green))
            refreshbadgeCount(badgeCount)
        }
        view.findViewById<View>(R.id.count_blue).setOnClickListener {
            badgeCount.setCountColor(resources.getColor(R.color.blue))
            refreshbadgeCount(badgeCount)
        }
        view.findViewById<View>(R.id.count_violet).setOnClickListener {
            badgeCount.setCountColor(resources.getColor(R.color.violet))
            refreshbadgeCount(badgeCount)
        }
        return view
    }

    private fun refreshbadgeCount(badgeCount: BadgeCount) {
        badgeCount.setCount(count)
    }
}
