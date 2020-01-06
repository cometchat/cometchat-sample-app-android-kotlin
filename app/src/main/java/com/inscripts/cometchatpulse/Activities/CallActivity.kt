package com.inscripts.cometchatpulse.Activities

import android.app.Activity
import androidx.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import androidx.databinding.DataBindingUtil
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import com.cometchat.pro.constants.CometChatConstants
import com.inscripts.cometchatpulse.Repository.MessageRepository
import com.inscripts.cometchatpulse.Helpers.CCPermissionHelper
import com.inscripts.cometchatpulse.Helpers.CometChatAudioHelper
import com.inscripts.cometchatpulse.Helpers.OutgoingAudioHelper
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.ViewModel.OnetoOneViewModel
import com.inscripts.cometchatpulse.databinding.ActivityCallBinding
import java.lang.NullPointerException

class CallActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var name: String

    private lateinit var id: String

    lateinit var binding: ActivityCallBinding

    private var imageUrl: String? = null

    private var cometChatAudioHelper: CometChatAudioHelper? = null

    private lateinit var sessionID: String

    private lateinit var onetoOneViewModel: OnetoOneViewModel

    private lateinit var notification: Uri

    private var isOutGoing:Boolean=false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_call)
        CommonUtil.setStatusBarColor(this)
        cometChatAudioHelper = CometChatAudioHelper(this)
        cometChatAudioHelper?.initAudio()
        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        onetoOneViewModel = ViewModelProviders.of(this).get(OnetoOneViewModel::class.java)
        if (intent.hasExtra("NotificationIntent"))
        {
            onetoOneViewModel.acceptCall(intent.getStringExtra(StringContract.IntentString.SESSION_ID), binding.mainCallView, this@CallActivity)
            if (intent.hasExtra(StringContract.IntentString.USER_NAME) && intent.hasExtra(StringContract.IntentString.USER_AVATAR)) {
                binding.name = intent.getStringExtra(StringContract.IntentString.USER_NAME)
                binding.image = intent.getStringExtra(StringContract.IntentString.USER_AVATAR)
            }
            else
            {
                binding.name = intent.getStringExtra(StringContract.IntentString.GROUP_NAME)
                binding.image = intent.getStringExtra(StringContract.IntentString.GROUP_ICON)
            }
            binding.acceptCall.visibility = View.GONE
            binding.hangUp.visibility = View.GONE
            binding.tvCallText.text = "Calling "+binding.name
        }
        else {
            try {

                if (intent?.action?.equals(CometChatConstants.CALL_TYPE_VIDEO)!!) {
                    if (!CCPermissionHelper.hasPermissions(this, *arrayOf(CCPermissionHelper.REQUEST_PERMISSION_CAMERA, CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO))) {
                        CCPermissionHelper.requestPermissions(this, arrayOf(CCPermissionHelper.REQUEST_PERMISSION_CAMERA, CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO),
                                StringContract.RequestCode.VIDEO_CALL)
                    }

                } else if (intent?.action.equals(CometChatConstants.CALL_TYPE_AUDIO)) {
                    if (!CCPermissionHelper.hasPermissions(this, *arrayOf(CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO))) {

                        CCPermissionHelper.requestPermissions(this, arrayOf(CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO),
                                StringContract.RequestCode.VOICE_CALL)
                    }
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }

            if (intent.hasExtra(StringContract.IntentString.RECIVER_TYPE)) {

                if (intent.getStringExtra(StringContract.IntentString.RECIVER_TYPE).equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {

                    name = intent.getStringExtra(StringContract.IntentString.GROUP_NAME)
                    id = intent.getStringExtra(StringContract.IntentString.GROUP_ID)
                    imageUrl = intent?.getStringExtra(StringContract.IntentString.GROUP_ICON)
                } else {
                    name = intent.getStringExtra(StringContract.IntentString.USER_NAME)
                    id = intent.getStringExtra(StringContract.IntentString.USER_ID)
                    imageUrl = intent?.getStringExtra(StringContract.IntentString.USER_AVATAR)
                }
            }

            if (intent?.type.equals(StringContract.IntentString.INCOMING)) {
                binding.tvCallText.text = getString(R.string.incoming_call)
                binding.tvCallText.currentHintTextColor
                cometChatAudioHelper?.startIncomingAudio(notification, true)
                isOutGoing = false
            } else if (intent?.type.equals(StringContract.IntentString.OUTGOING)) {
                cometChatAudioHelper?.startOutgoingAudio(OutgoingAudioHelper.Type.IN_COMMUNICATION)
                val rl = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT)

                rl.addRule(RelativeLayout.CENTER_HORIZONTAL)
                rl.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                rl.bottomMargin = 56

                isOutGoing = true

                binding.hangUp.layoutParams = rl
                binding.acceptCall.visibility = View.GONE
                binding.tvCallText.text = getString(R.string.ringing)
            }

            binding.name = name
            binding.image = imageUrl

            binding.acceptCall.setOnClickListener(this)
            binding.hangUp.setOnClickListener(this)

            sessionID = intent.getStringExtra(StringContract.IntentString.SESSION_ID)
        }

    }

    override fun onStart() {
        super.onStart()
        onetoOneViewModel.addCallListener(this@CallActivity, StringContract.ListenerName.CALL_EVENT_LISTENER, binding.mainCallView)
    }

    override fun onDestroy() {
        super.onDestroy()
        onetoOneViewModel.removeCallListener(StringContract.ListenerName.CALL_EVENT_LISTENER)
        cometChatAudioHelper?.stop(false)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {

            StringContract.RequestCode.VOICE_CALL -> if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, getString(R.string.voice_call_warning), Toast.LENGTH_SHORT).show()
            }
            StringContract.RequestCode.VIDEO_CALL -> if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, getString(R.string.video_call_warning), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onClick(p0: View?) {

        when (p0?.id) {

            R.id.acceptCall -> {
                cometChatAudioHelper?.stop(false)
                onetoOneViewModel.acceptCall(sessionID, binding.mainCallView, this@CallActivity)
            }

            R.id.hangUp -> {
                cometChatAudioHelper?.stop(true)
                 if (isOutGoing){
                     onetoOneViewModel.rejectCall(sessionID, CometChatConstants.CALL_STATUS_CANCELLED, this@CallActivity)
                 }
                else{
                     onetoOneViewModel.rejectCall(sessionID, CometChatConstants.CALL_STATUS_REJECTED, this@CallActivity)
                 }

            }

        }
    }
}
