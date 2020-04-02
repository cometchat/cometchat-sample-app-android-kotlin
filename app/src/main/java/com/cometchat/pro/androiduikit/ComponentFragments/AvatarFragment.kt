package com.cometchat.pro.androiduikit.ComponentFragments


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment

import com.cometchat.pro.androiduikit.R
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.uikit.Avatar
import com.google.android.material.textfield.TextInputEditText

class AvatarFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_avatar, container, false)
        val avatar = view.findViewById<Avatar>(R.id.avataricon)
        avatar.setBorderColor(resources.getColor(R.color.colorPrimaryDark))
        avatar.setAvatar(CometChat.getLoggedInUser().avatar)
        val borderWidth = view.findViewById<TextInputEditText>(R.id.borderWidth)
        borderWidth.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.length > 0)
                    avatar.setBorderWidth(Integer.parseInt(charSequence.toString()))
            }

            override fun afterTextChanged(editable: Editable) {

            }
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

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    fun refreshAvatar(avatar: Avatar) {
        avatar.setAvatar(CometChat.getLoggedInUser())
    }
}// Required empty public constructor
