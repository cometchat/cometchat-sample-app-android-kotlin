package com.cometchat.pro.androiduikit.ComponentFragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cometchat.pro.androiduikit.R
import com.cometchat.pro.uikit.BadgeCount
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import utils.Utils

class BadgeCountFragment : Fragment() {
    private var count = 1
    private var badgeCountLayout: TextInputLayout? = null
    private var badgeCountSizeLayout: TextInputLayout? = null
    private var badgeCountEdt: TextInputEditText? = null
    private var countSize: TextInputEditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_badge_count, container, false)
        val badgeCount: BadgeCount = view.findViewById(R.id.badgeCount)
        badgeCountLayout = view.findViewById(R.id.badgeCount_layout)
        badgeCountSizeLayout = view.findViewById(R.id.badgeCountSize_layout)
        badgeCountEdt = view.findViewById(R.id.badgeCount_edt)
        countSize = view.findViewById(R.id.countSize)
        countSize!!.setText(12.toString())
        badgeCountEdt!!.setText(1.toString())
        badgeCountEdt!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.length > 0 && charSequence.length < 7) {
                    count = charSequence.toString().toInt()
                    badgeCount.setCount(charSequence.toString().toInt())
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        countSize!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.length > 0 && charSequence.toString().toInt() < 32) badgeCount.setCountSize(charSequence.toString().toFloat()) else if (charSequence.length == 0) {
                    badgeCount.setCountSize(12f)
                }
            }

            override fun afterTextChanged(editable: Editable) {}
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

        /**/view.findViewById<View>(R.id.count_red).setOnClickListener {
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
        checkDarkMode()
        return view
    }

    private fun checkDarkMode() {
        if (Utils.isDarkMode(context!!)) {
            badgeCountLayout!!.boxStrokeColor = resources.getColor(R.color.textColorWhite)
            badgeCountLayout!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            badgeCountLayout!!.defaultHintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            badgeCountSizeLayout!!.boxStrokeColor = resources.getColor(R.color.textColorWhite)
            badgeCountSizeLayout!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            badgeCountSizeLayout!!.defaultHintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
        } else {
            badgeCountLayout!!.boxStrokeColor = resources.getColor(R.color.primaryTextColor)
            badgeCountLayout!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.primaryTextColor))
            badgeCountLayout!!.defaultHintTextColor = ColorStateList.valueOf(resources.getColor(R.color.primaryTextColor))
            badgeCountSizeLayout!!.boxStrokeColor = resources.getColor(R.color.primaryTextColor)
            badgeCountSizeLayout!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.primaryTextColor))
            badgeCountSizeLayout!!.defaultHintTextColor = ColorStateList.valueOf(resources.getColor(R.color.primaryTextColor))
        }
    }

    private fun refreshbadgeCount(badgeCount: BadgeCount) {
        badgeCount.setCount(count)
    }
}