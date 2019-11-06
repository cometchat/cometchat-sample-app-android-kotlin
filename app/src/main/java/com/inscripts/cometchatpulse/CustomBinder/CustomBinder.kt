package com.inscripts.cometchatpulse.CustomBinder

import android.annotation.SuppressLint
import android.content.Context
import android.databinding.BindingAdapter
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.ImageView
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.helpers.Logger
import com.inscripts.cometchatpulse.CometChatPro
import com.inscripts.cometchatpulse.CustomView.CircleImageView
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.Appearance
import com.inscripts.cometchatpulse.Utils.DateUtil
import com.inscripts.cometchatpulse.Utils.MediaUtil
import java.sql.Timestamp

@BindingAdapter(value = ["setTimeStamp"])
fun setTimeStamp(timeView: TextView, timeLong: Long) {

    val dt = java.util.Date(timeLong * 1000)
    val time = Timestamp(dt.getTime())

    val str = DateUtil.getMessageTime(time.toString(), "hh:mm a")
    timeView.setText(str)
}


@BindingAdapter(value = ["setDeliveryStatus"])
fun setDeliveryStatus(deliveryStatusView: CircleImageView, deliveryStatus: Int) {

    if (StringContract.AppDetails.theme == Appearance.AppTheme.AZURE_RADIANCE) {
        deliveryStatusView.circleBackgroundColor = StringContract.Color.iconTint
    } else {
        deliveryStatusView.circleBackgroundColor = StringContract.Color.primaryColor
    }

    if (deliveryStatus == 0) {
        deliveryStatusView.setImageDrawable(ContextCompat.getDrawable(CometChatPro.applicationContext(), R.drawable.ic_access_time_24dp))
    } else if (deliveryStatus == 1) {
        deliveryStatusView.setImageDrawable(ContextCompat.getDrawable(CometChatPro.applicationContext(), R.drawable.ic_check_24dp))
    }

}


@BindingAdapter(value = ["setVideoMessage", "setContext"])
fun setVideoMessage(videoVideoView: VideoView, url: String, context: Context) {
    videoVideoView.setVideoPath(url)
    videoVideoView.setMediaController(MediaController(context))
    videoVideoView.keepScreenOn = true
    videoVideoView.start()
}

@BindingAdapter(value = ["setOptionImage"])
fun setOptionImage(ivOption: CircleImageView,drawable: Drawable) {

    try {
        drawable.setColorFilter(StringContract.Color.white, PorterDuff.Mode.SRC_ATOP)
        ivOption.setImageBitmap(MediaUtil.getPlaceholderImage(CometChatPro.applicationContext(),drawable))
        if (StringContract.AppDetails.theme == Appearance.AppTheme.AZURE_RADIANCE) {
            ivOption.circleBackgroundColor = StringContract.Color.primaryDarkColor
        } else {
            ivOption.circleBackgroundColor = StringContract.Color.primaryColor
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }

}

@BindingAdapter(value = ["setGroupIcon"])
fun setGroupIcon(view: CircleImageView, groupIcon: String?) {
    val default: Drawable = ContextCompat.getDrawable(CometChatPro.applicationContext(), R.drawable.ic_group_default)!!
    view.circleBackgroundColor = StringContract.Color.primaryColor
    if (groupIcon != null) {
        val requestOptions = RequestOptions()
        requestOptions.placeholder(default)
        Glide.with(CometChatPro.applicationContext()).applyDefaultRequestOptions(requestOptions)
                .load(groupIcon).into(view)
    } else {
        try {
            if (StringContract.AppDetails.theme == Appearance.AppTheme.AZURE_RADIANCE) {
                default.setColorFilter(StringContract.Color.primaryDarkColor, PorterDuff.Mode.SRC_ATOP)
            }
            view.setImageBitmap(MediaUtil.getPlaceholderImage(CometChatPro.applicationContext(), default))
        } catch (e: Exception) {
            view.setImageBitmap(MediaUtil.getPlaceholderImage(CometChatPro.applicationContext(), default))
        }
    }
}

@BindingAdapter(value = ["setStatus", "setLastActive"])
fun setStatus(tvStatus: TextView, status: String, lastActive: Long?) {

    if (status.equals(CometChatConstants.USER_STATUS_ONLINE, ignoreCase = true)) {
        tvStatus.text = status
    } else if (status.equals(CometChatConstants.USER_STATUS_OFFLINE, ignoreCase = true)) {
        if (lastActive != null) {
            tvStatus.text = DateUtil.getLastSeenDate(lastActive, CometChatPro.applicationContext())
        } else {
            tvStatus.text = status
        }

    }
}

@BindingAdapter(value = ["setStatusIcon"])
fun setStatusIcon(ivStatus: ImageView, status: String) {

    val statusDrawable: Drawable

    if (status.equals(CometChatConstants.USER_STATUS_ONLINE, ignoreCase = true)) {
        statusDrawable = ContextCompat.getDrawable(CometChatPro.applicationContext(), R.drawable.cc_status_available)!!
    } else {
        statusDrawable = ContextCompat.getDrawable(CometChatPro.applicationContext(), R.drawable.cc_status_offline)!!
    }

    ivStatus.setImageDrawable(statusDrawable)
}

@SuppressLint("CheckResult")
@BindingAdapter(value = ["setImage"])
fun setImage(imageView: ImageView, url: String?) {

    try {
        if (url != null) {
            Log.d("corner", "called")
            val requestOptions = RequestOptions()
            requestOptions.placeholder(ContextCompat.getDrawable(CometChatPro.applicationContext(),
                    R.drawable.ic_broken_image_black))
            requestOptions.centerCrop()
            Glide.with(CometChatPro.applicationContext())
                    .applyDefaultRequestOptions(requestOptions)
                    .load(url)
                    .into(imageView)
        }


    } catch (e: Exception) {
        e.printStackTrace()
    }

}

@BindingAdapter(value = ["groupDetailIcon"])
fun setGroupDeatail(view: ImageView, url: String?) {
    val default: Drawable = ContextCompat.getDrawable(CometChatPro.applicationContext(), R.drawable.ic_group_default)!!
    try {
        if (url != null) {

            val requestOptions = RequestOptions()
            requestOptions.placeholder(default)
            Glide.with(CometChatPro.applicationContext()).applyDefaultRequestOptions(requestOptions)
                    .load(url).into(view)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@SuppressLint("CheckResult")
@BindingAdapter(value = ["setUserImage"])
fun setImage(view: CircleImageView, avatar: String?) {

    val default: Drawable = ContextCompat.getDrawable(CometChatPro.applicationContext(), R.drawable.default_avatar)!!
    if (avatar != null) {
        val requestOptions = RequestOptions()
        requestOptions.placeholder(default)
        Glide.with(CometChatPro.applicationContext()).applyDefaultRequestOptions(requestOptions)
                .load(avatar).into(view)
    } else {

        try {
            if (StringContract.AppDetails.theme == Appearance.AppTheme.AZURE_RADIANCE) {
                default.setColorFilter(StringContract.Color.primaryDarkColor, PorterDuff.Mode.SRC_ATOP)
            }
            view.circleBackgroundColor = StringContract.Color.primaryColor
            view.setImageBitmap(MediaUtil.getPlaceholderImage(CometChatPro.applicationContext(), default))
        } catch (e: Exception) {
            if (StringContract.AppDetails.theme == Appearance.AppTheme.AZURE_RADIANCE) {
                default.setColorFilter(StringContract.Color.primaryDarkColor, PorterDuff.Mode.SRC_ATOP)
            }
            view.circleBackgroundColor = StringContract.Color.primaryColor
            view.setImageDrawable(ContextCompat.getDrawable(CometChatPro.applicationContext(), R.drawable.default_avatar))
            e.printStackTrace()
        }

    }
}