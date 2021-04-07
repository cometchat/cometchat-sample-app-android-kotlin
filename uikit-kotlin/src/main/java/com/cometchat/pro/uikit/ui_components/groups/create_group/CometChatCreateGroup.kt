package com.cometchat.pro.uikit.ui_components.groups.create_group

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.Group
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.messages.message_list.CometChatMessageListActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import java.security.SecureRandom

/**
 * Purpose - CometChatCreateGroup class is a fragment used to create a group. User just need to enter
 * group name. All other information like guid, groupIcon are set by this class.
 *
 * @see CometChat.createGroup
 */
class CometChatCreateGroup : Fragment() {
    private var etGroupName: TextInputEditText? = null
    private var etGroupDesc: TextInputEditText? = null
    private var etGroupPassword: TextInputEditText? = null
    private var etGroupCnfPassword: TextInputEditText? = null
    private var des1: TextView? = null
    private var des2: TextView? = null
    private var groupNameLayout: TextInputLayout? = null
    private var groupDescLayout: TextInputLayout? = null
    private var groupPasswordLayout: TextInputLayout? = null
    private var groupCnfPasswordLayout: TextInputLayout? = null
    private var createGroupBtn: MaterialButton? = null
    private var groupTypeSpinner: Spinner? = null
    private var groupType: String? = null
    var TAG = "CometChatCreateGroup"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cometchat_create_group, container, false)
        etGroupName = view.findViewById(R.id.group_name)
        etGroupDesc = view.findViewById(R.id.group_desc)
        etGroupPassword = view.findViewById(R.id.group_pwd)
        etGroupCnfPassword = view.findViewById(R.id.group_cnf_pwd)
        etGroupCnfPassword!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!etGroupPassword!!.getText().toString().isEmpty() && s.toString() == etGroupPassword!!.getText().toString()) {
                    groupCnfPasswordLayout!!.endIconDrawable = resources.getDrawable(R.drawable.ic_check_black_24dp)
                    groupCnfPasswordLayout!!.setEndIconTintList(ColorStateList.valueOf(resources.getColor(R.color.green_600)))
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        des1 = view.findViewById(R.id.tvDes1)
        des2 = view.findViewById(R.id.tvDes2)
        groupNameLayout = view.findViewById(R.id.input_group_name)
        groupDescLayout = view.findViewById(R.id.input_group_desc)
        groupPasswordLayout = view.findViewById(R.id.input_group_pwd)
        groupCnfPasswordLayout = view.findViewById(R.id.input_group_cnf_pwd)
        groupTypeSpinner = view.findViewById(R.id.grouptype_spinner)
        groupTypeSpinner!!.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                if (position == 0) {
                    groupType = CometChatConstants.GROUP_TYPE_PUBLIC
                    groupPasswordLayout!!.setVisibility(View.GONE)
                    groupCnfPasswordLayout!!.setVisibility(View.GONE)
                } else if (position == 1) {
                    groupType = CometChatConstants.GROUP_TYPE_PRIVATE
                    groupPasswordLayout!!.setVisibility(View.GONE)
                    groupCnfPasswordLayout!!.setVisibility(View.GONE)
                } else if (position == 2) {
                    groupType = CometChatConstants.GROUP_TYPE_PASSWORD
                    groupPasswordLayout!!.setVisibility(View.VISIBLE)
                    groupCnfPasswordLayout!!.setVisibility(View.VISIBLE)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
        createGroupBtn = view.findViewById(R.id.btn_create_group)
        createGroupBtn!!.setOnClickListener(View.OnClickListener { createGroup() })
        checkDarkMode()
        return view
    }

    private fun checkDarkMode() {
        if (Utils.isDarkMode(context!!)) {
            des1!!.setTextColor(resources.getColor(R.color.textColorWhite))
            des2!!.setTextColor(resources.getColor(R.color.textColorWhite))
            groupNameLayout!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            groupNameLayout!!.boxStrokeColor = resources.getColor(R.color.textColorWhite)
            groupNameLayout!!.defaultHintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            etGroupName!!.setTextColor(resources.getColor(R.color.textColorWhite))
            groupDescLayout!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            groupDescLayout!!.boxStrokeColor = resources.getColor(R.color.textColorWhite)
            groupDescLayout!!.defaultHintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            etGroupDesc!!.setTextColor(resources.getColor(R.color.textColorWhite))
            groupPasswordLayout!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            groupPasswordLayout!!.boxStrokeColor = resources.getColor(R.color.textColorWhite)
            groupPasswordLayout!!.defaultHintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            etGroupPassword!!.setTextColor(resources.getColor(R.color.textColorWhite))
            groupCnfPasswordLayout!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            groupCnfPasswordLayout!!.boxStrokeColor = resources.getColor(R.color.textColorWhite)
            groupCnfPasswordLayout!!.defaultHintTextColor = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            etGroupCnfPassword!!.setTextColor(resources.getColor(R.color.textColorWhite))
        } else {
            des1!!.setTextColor(resources.getColor(R.color.primaryTextColor))
            des2!!.setTextColor(resources.getColor(R.color.primaryTextColor))
            groupNameLayout!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.primaryTextColor))
            groupNameLayout!!.boxStrokeColor = resources.getColor(R.color.primaryTextColor)
            etGroupName!!.setTextColor(resources.getColor(R.color.primaryTextColor))
            groupDescLayout!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.primaryTextColor))
            groupDescLayout!!.boxStrokeColor = resources.getColor(R.color.primaryTextColor)
            etGroupDesc!!.setTextColor(resources.getColor(R.color.primaryTextColor))
            groupPasswordLayout!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.primaryTextColor))
            groupPasswordLayout!!.boxStrokeColor = resources.getColor(R.color.primaryTextColor)
            etGroupPassword!!.setTextColor(resources.getColor(R.color.primaryTextColor))
            groupCnfPasswordLayout!!.hintTextColor = ColorStateList.valueOf(resources.getColor(R.color.primaryTextColor))
            groupCnfPasswordLayout!!.boxStrokeColor = resources.getColor(R.color.primaryTextColor)
            etGroupCnfPassword!!.setTextColor(resources.getColor(R.color.primaryTextColor))
        }
    }

    private fun createGroup() {
        if (!etGroupName!!.text.toString().isEmpty()) {
            if (groupType == CometChatConstants.GROUP_TYPE_PUBLIC || groupType == CometChatConstants.GROUP_TYPE_PRIVATE) {
                val group = Group("group" + generateRandomString(95), etGroupName!!.text.toString(), groupType, "")
                createGroup(group)
            } else if (groupType == CometChatConstants.GROUP_TYPE_PASSWORD) {
                if (etGroupPassword!!.text.toString().isEmpty()) etGroupPassword!!.error = resources.getString(R.string.fill_this_field) else if (etGroupCnfPassword!!.text.toString().isEmpty()) etGroupCnfPassword!!.error = resources.getString(R.string.fill_this_field) else if (etGroupPassword!!.text.toString() == etGroupCnfPassword!!.text.toString()) {
                    val group = Group("group" + generateRandomString(95), etGroupName!!.text.toString(), groupType, etGroupPassword!!.text.toString())
                    createGroup(group)
                } else if (etGroupPassword != null) Snackbar.make(etGroupCnfPassword!!.rootView, resources.getString(R.string.password_not_matched), Snackbar.LENGTH_LONG).show()
            }
        } else {
            etGroupName!!.error = resources.getString(R.string.fill_this_field)
        }
    }

    private fun createGroup(group: Group) {
        CometChat.createGroup(group, object : CallbackListener<Group>() {
            override fun onSuccess(group: Group) {
                val intent = Intent(activity, CometChatMessageListActivity::class.java)
                intent.putExtra(UIKitConstants.IntentStrings.NAME, group.name)
                intent.putExtra(UIKitConstants.IntentStrings.GROUP_OWNER, group.owner)
                intent.putExtra(UIKitConstants.IntentStrings.GUID, group.guid)
                intent.putExtra(UIKitConstants.IntentStrings.AVATAR, group.icon)
                intent.putExtra(UIKitConstants.IntentStrings.GROUP_TYPE, group.groupType)
                intent.putExtra(UIKitConstants.IntentStrings.TYPE, CometChatConstants.RECEIVER_TYPE_GROUP)
                intent.putExtra(UIKitConstants.IntentStrings.MEMBER_COUNT, group.membersCount)
                intent.putExtra(UIKitConstants.IntentStrings.GROUP_DESC, group.description)
                intent.putExtra(UIKitConstants.IntentStrings.GROUP_PASSWORD, group.password)
                if (activity != null) activity!!.finish()
                startActivity(intent)
            }

            override fun onError(e: CometChatException) {
//                Snackbar.make(etGroupName!!.rootView, resources.getString(R.string.create_group_error), Snackbar.LENGTH_LONG).show()
                context?.let { Utils.showDialog(it, e) }
                Log.e(TAG, "onError: " + e.message)
            }
        })
    }

    companion object {
        /**
         * This method is used to create group when called from layout. It uses `Random.nextInt()`
         * to generate random number to use with group id and group icon. Any Random number between 10 to
         * 1000 are choosen.
         *
         */
        fun generateRandomString(length: Int): String {
            require(length >= 1)
            val sb = StringBuilder(length)
            for (i in 0 until length) {
                // 0-62 (exclusive), random returns 0-61
                val random = SecureRandom()
                val CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz"
                val CHAR_UPPER = CHAR_LOWER.toUpperCase()
                val NUMBER = "0123456789"
                val DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER
                val rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length)
                val rndChar = DATA_FOR_RANDOM_STRING[rndCharAt]
                // debug
                System.out.format("%d\t:\t%c%n", rndCharAt, rndChar)
                sb.append(rndChar)
            }
            return sb.toString()
        }
    }
}