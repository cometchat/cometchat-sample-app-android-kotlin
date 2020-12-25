package com.cometchat.pro.androiduikit.ComponentFragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cometchat.pro.androiduikit.R
import com.cometchat.pro.core.CometChat
import android.text.TextWatcher
import android.text.Editable
import android.widget.RadioGroup
import android.content.res.ColorStateList
import android.view.View
import androidx.fragment.app.Fragment
import com.cometchat.pro.androiduikit.ColorPickerDialog
import com.cometchat.pro.uikit.Avatar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import utils.Utils

class AvatarFragment : Fragment(), ColorPickerDialog.OnColorChangedListener {
    private var c: Context? = null
    private var borderWidthLayout: TextInputLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_avatar, container, false)
        val avatar: Avatar = view.findViewById(R.id.avataricon)
        avatar.setBorderColor(resources.getColor(R.color.colorPrimaryDark))
        avatar.setAvatar(CometChat.getLoggedInUser().avatar)
        val borderWidth: TextInputEditText = view.findViewById(R.id.borderWidth)
        borderWidthLayout = view.findViewById(R.id.borderWidth_layout)
        borderWidth.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.length > 0) avatar.setBorderWidth(charSequence.toString().toInt())
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        val shapegroup = view.findViewById<RadioGroup>(R.id.shapeGroup)
        shapegroup.setOnCheckedChangeListener { radioGroup, i ->
            if (i == R.id.circle) {
                avatar.setShape("circle")
                refreshAvatar(avatar)
            } else {
                avatar.setShape("rectangle")
                refreshAvatar(avatar)
            }
        }
        view.findViewById<View>(R.id.red).setOnClickListener {
            avatar.setBorderColor(resources.getColor(R.color.red))
            refreshAvatar(avatar)
        }
        view.findViewById<View>(R.id.yellow).setOnClickListener {
            avatar.setBorderColor(resources.getColor(R.color.yellow))
            refreshAvatar(avatar)
        }
        view.findViewById<View>(R.id.purple).setOnClickListener {
            avatar.setBorderColor(resources.getColor(R.color.purple))
            refreshAvatar(avatar)
        }
        view.findViewById<View>(R.id.green).setOnClickListener {
            avatar.setBorderColor(resources.getColor(R.color.green))
            refreshAvatar(avatar)
        }
        view.findViewById<View>(R.id.blue).setOnClickListener {
            avatar.setBorderColor(resources.getColor(R.color.blue))
            refreshAvatar(avatar)
        }
        view.findViewById<View>(R.id.violet).setOnClickListener {
            avatar.setBorderColor(resources.getColor(R.color.violet))
            refreshAvatar(avatar)
        }
        checkDarkMode()
        return view
    }

    private fun checkDarkMode() {
        if (Utils.isDarkMode(context!!)) {
            borderWidthLayout!!.defaultHintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            borderWidthLayout!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            borderWidthLayout!!.boxStrokeColor = resources.getColor(R.color.textColorWhite)
        } else {
            borderWidthLayout!!.defaultHintTextColor = ColorStateList.valueOf(resources.getColor(R.color.primaryTextColor))
            borderWidthLayout!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.primaryTextColor))
            borderWidthLayout!!.boxStrokeColor = resources.getColor(R.color.primaryTextColor)
        }
    }

    override fun colorChanged(key: String?, color: Int) {}
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.c = context
    }

    fun refreshAvatar(avatar: Avatar) {
        avatar.setAvatar(CometChat.getLoggedInUser())
    }
}