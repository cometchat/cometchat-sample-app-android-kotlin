package com.inscripts.cometchatpulse.Activities

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.databinding.ActivityImageViewBinding

class ImageViewActivity : AppCompatActivity() {

    private lateinit var binding:ActivityImageViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CommonUtil.setStatusBarColor(this)

        binding=DataBindingUtil.setContentView(this,R.layout.activity_image_view)

        binding.context=this

        binding.type=intent?.getStringExtra(StringContract.IntentString.FILE_TYPE)

        binding.url=intent?.getStringExtra(StringContract.IntentString.URL)

        CommonUtil.setStatusBarColor(this)
    }
}
