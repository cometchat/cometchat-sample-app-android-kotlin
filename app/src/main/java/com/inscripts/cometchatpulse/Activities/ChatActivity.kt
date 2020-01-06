package com.inscripts.cometchatpulse.Activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.User
import com.inscripts.cometchatpulse.Fragment.GroupFragment
import com.inscripts.cometchatpulse.Fragment.OneToOneFragment
import com.inscripts.cometchatpulse.Helpers.CustomAlertDialogHelper
import com.inscripts.cometchatpulse.Helpers.OnBackArrowClickListener
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.CommonUtil
import kotlinx.android.synthetic.main.activity_main.*

class ChatActivity : AppCompatActivity(), OnBackArrowClickListener {

    override fun onBackClick() {
        if (twoPane) {

            if (list_container.visibility == View.VISIBLE) {
                list_container.visibility = View.GONE
            } else if (list_container.visibility == View.GONE) {
                list_container.visibility = View.VISIBLE
            }
        }
    }

    private var twoPane: Boolean = false

    var resId: Int = 0


    private lateinit var group: Group

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        handleIntent()

    }

    fun handleIntent()
    {
        if (twoPane) {
            resId = R.id.chat_frame_container_detail
        } else {
            resId = R.id.chat_main_container
        }
        if (intent.getStringExtra(StringContract.IntentString.RECIVER_TYPE).equals(CometChatConstants.CONVERSATION_TYPE_USER)) {
            val oneToOneFragment = OneToOneFragment().apply {
                arguments = Bundle().apply {
                    putString(StringContract.IntentString.USER_ID, intent.getStringExtra(StringContract.IntentString.USER_ID))
                    putString(StringContract.IntentString.USER_NAME, intent.getStringExtra(StringContract.IntentString.USER_NAME))
                    putString(StringContract.IntentString.USER_AVATAR, intent.getStringExtra(StringContract.IntentString.USER_AVATAR))
                    putString(StringContract.IntentString.USER_STATUS, intent.getStringExtra(StringContract.IntentString.USER_STATUS))
                    putLong(StringContract.IntentString.LAST_ACTIVE, intent.getLongExtra(StringContract.IntentString.LAST_ACTIVE, 0))
                }
            }
            supportFragmentManager.beginTransaction()
                    .replace(resId, oneToOneFragment).addToBackStack(null).commit()
        }
        else
        {
            val groupChat = GroupFragment().apply {
                arguments = Bundle().apply {
                    putString(StringContract.IntentString.GROUP_ID, intent.getStringExtra(StringContract.IntentString.GROUP_ID))
                    putString(StringContract.IntentString.GROUP_NAME, intent.getStringExtra(StringContract.IntentString.GROUP_NAME))
                    putString(StringContract.IntentString.GROUP_ICON, intent.getStringExtra(StringContract.IntentString.GROUP_ICON))
                    putString(StringContract.IntentString.GROUP_OWNER, intent.getStringExtra(StringContract.IntentString.GROUP_OWNER))
                    putString(StringContract.IntentString.GROUP_DESCRIPTION, intent.getStringExtra(StringContract.IntentString.GROUP_DESCRIPTION))
                    putString(StringContract.IntentString.USER_SCOPE,intent.getStringExtra(StringContract.IntentString.USER_SCOPE))
                }
            }
            supportFragmentManager.beginTransaction()
                    .replace(resId, groupChat).addToBackStack(null).commit()
        }
    }
}
