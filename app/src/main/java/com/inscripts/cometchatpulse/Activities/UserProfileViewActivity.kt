package com.inscripts.cometchatpulse.Activities

import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.inscripts.cometchatpulse.Helpers.CCPermissionHelper
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.ViewModel.OnetoOneViewModel
import com.inscripts.cometchatpulse.databinding.ActivityUserProfileViewBinding
import android.support.v4.content.ContextCompat
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import com.inscripts.cometchatpulse.Extensions.setTitleTypeface
import com.inscripts.cometchatpulse.Utils.Appearance


class UserProfileViewActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityUserProfileViewBinding

    private var uid: String? = null

    private lateinit var oneToOneViewModel: OnetoOneViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CommonUtil.setStatusBarColor(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile_view)

        binding.url = intent?.getStringExtra(StringContract.IntentString.USER_AVATAR)

        binding.status = intent?.getStringExtra(StringContract.IntentString.USER_STATUS)

        binding.name = intent?.getStringExtra(StringContract.IntentString.USER_NAME)

        uid = intent?.getStringExtra(StringContract.IntentString.USER_ID)

        oneToOneViewModel = ViewModelProviders.of(this).get(OnetoOneViewModel::class.java)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.title=intent?.getStringExtra(StringContract.IntentString.USER_NAME)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setTitleTypeface(StringContract.Font.title)

        binding.toolbar.navigationIcon?.setColorFilter(StringContract.Color.iconTint,PorterDuff.Mode.SRC_ATOP)

        binding.tv30.setTextColor(StringContract.Color.primaryColor)

        binding.toolbar.setTitle(intent?.getStringExtra(StringContract.IntentString.USER_NAME))

        binding.toolbar.setBackgroundColor(StringContract.Color.primaryColor)

        binding.toolbar.setTitleTextColor(StringContract.Color.white)

        if (StringContract.AppDetails.theme== Appearance.AppTheme.AZURE_RADIANCE) {
            binding.toolbar.setTitleTextColor(StringContract.Color.black)
            binding.ivVideo.drawable.setColorFilter(StringContract.Color.iconTint,PorterDuff.Mode.SRC_ATOP)
            binding.ivVoice.drawable.setColorFilter(StringContract.Color.iconTint,PorterDuff.Mode.SRC_ATOP)

        }
        else{
            binding.ivVideo.drawable.setColorFilter(StringContract.Color.primaryColor,PorterDuff.Mode.SRC_ATOP)
            binding.ivVoice.drawable.setColorFilter(StringContract.Color.primaryColor,PorterDuff.Mode.SRC_ATOP)
            binding.toolbar.setTitleTextColor(StringContract.Color.white)
        }

        binding.videoCall.setOnClickListener(this)
        binding.voiceCall.setOnClickListener(this)


        if (CometChat.getLoggedInUser().uid.equals(intent?.getStringExtra(StringContract.IntentString.USER_ID))) {

            binding.voiceCall.visibility = View.GONE
            binding.videoCall.visibility = View.GONE
            binding.cardViewCallContainer.visibility = View.GONE
            binding.status=getString(R.string.online)

        }
         CommonUtil.setStatusBarColor(this)

    }

    override fun onClick(p0: View?) {

        when (p0?.id) {

            R.id.voice_call -> {
                if (CCPermissionHelper.hasPermissions(this, *arrayOf(CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO))) {

                    uid?.let { oneToOneViewModel.initCall(this, it, CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.CALL_TYPE_AUDIO) }
                } else {
                    CCPermissionHelper.requestPermissions(this, arrayOf(CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO),
                            StringContract.RequestCode.VOICE_CALL)
                }

            }

            R.id.video_call -> {
                if (CCPermissionHelper.hasPermissions(this, *arrayOf(CCPermissionHelper.REQUEST_PERMISSION_CAMERA, CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO))) {

                    uid?.let { oneToOneViewModel.initCall(this, it, CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.CALL_TYPE_VIDEO) }
                } else {
                    CCPermissionHelper.requestPermissions(this, arrayOf(CCPermissionHelper.REQUEST_PERMISSION_CAMERA, CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO),
                            StringContract.RequestCode.VIDEO_CALL)
                }


            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {

            StringContract.RequestCode.VOICE_CALL -> if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {

                uid?.let { oneToOneViewModel.initCall(this, it, CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.CALL_TYPE_AUDIO) }

            } else {
                Toast.makeText(this, getString(R.string.voice_call_warning), Toast.LENGTH_SHORT).show()
            }
            StringContract.RequestCode.VIDEO_CALL -> if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                uid?.let { oneToOneViewModel.initCall(this, it, CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.CALL_TYPE_VIDEO) }
            } else {

                Toast.makeText(this, getString(R.string.video_call_warning), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item?.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
