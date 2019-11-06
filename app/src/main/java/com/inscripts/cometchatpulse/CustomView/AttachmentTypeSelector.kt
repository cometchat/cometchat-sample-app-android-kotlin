package com.inscripts.cometchatpulse.CustomView

import android.animation.Animator
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.view.*
import android.view.animation.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.StringContract.RequestCode.Companion.ADD_DOCUMENT
import com.inscripts.cometchatpulse.StringContract.RequestCode.Companion.ADD_GALLERY
import com.inscripts.cometchatpulse.StringContract.RequestCode.Companion.ADD_SOUND
import com.inscripts.cometchatpulse.StringContract.RequestCode.Companion.LOCATION
import com.inscripts.cometchatpulse.StringContract.RequestCode.Companion.TAKE_PHOTO
import com.inscripts.cometchatpulse.StringContract.RequestCode.Companion.TAKE_VIDEO
import com.inscripts.cometchatpulse.Utils.AnimUtil
import com.inscripts.cometchatpulse.Utils.Appearance
//import com.inscripts.cometchatpulse.R
//import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.Utils.MediaUtil

class AttachmentTypeSelector(context: Context, private var listener: AttachmentClickedListener?) : PopupWindow() {
//
    private val ANIMATION_DURATION = 300

    private val TAG = AttachmentTypeSelector::class.java.simpleName

    private  var imageButton: CircleImageView

    private  var audioButton: CircleImageView
    private  var documentButton: CircleImageView

    private  var cameraButton: CircleImageView
    private  var locationButton: CircleImageView
    private  var videoButton: CircleImageView


    private var currentAnchor: View? = null
    private var rect: Rect? = null
    private var winHeight: Int = 0

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.attachment_type_selector, null, true) as LinearLayout

        this.imageButton = CommonUtil.findById(layout, R.id.gallery_button) as CircleImageView
        this.audioButton = CommonUtil.findById(layout, R.id.audio_button) as CircleImageView
        this.documentButton = CommonUtil.findById(layout, R.id.document_button) as CircleImageView
        this.cameraButton = CommonUtil.findById(layout, R.id.camera_button) as CircleImageView
        this.locationButton = CommonUtil.findById(layout, R.id.close_button) as CircleImageView
        this.videoButton = CommonUtil.findById(layout, R.id.video) as CircleImageView

        var color:Int?
        if (StringContract.AppDetails.theme == Appearance.AppTheme.AZURE_RADIANCE)
        {
            color=StringContract.Color.iconTint
        }
        else{
            color=StringContract.Color.primaryColor
        }

        val imageDrawable = ContextCompat.getDrawable(context,R.drawable.ic_outline_image_24px)
        val cameraDrawable = ContextCompat.getDrawable(context,R.drawable.ic_outline_add_a_photo_24px)
        val audioDrawable = ContextCompat.getDrawable(context,R.drawable.ic_mic_24dp)
        val videoDrawable = ContextCompat.getDrawable(context,R.drawable.ic_outline_videocam_24px)
        val documentDrawable = ContextCompat.getDrawable(context,R.drawable.ic_outline_insert_drive_file_24px)
        val locationDrawable = ContextCompat.getDrawable(context,R.drawable.ic_location_on_black_24dp)

        imageDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        cameraDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        audioDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        videoDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        documentDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        locationDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)

        this.imageButton.setImageBitmap(imageDrawable?.let { MediaUtil.getPlaceholderImage(context, it) })
        this.audioButton.setImageBitmap(audioDrawable?.let { MediaUtil.getPlaceholderImage(context, it) })
        this.documentButton.setImageBitmap(documentDrawable?.let { MediaUtil.getPlaceholderImage(context, it) })
        this.locationButton.setImageBitmap(locationDrawable?.let { MediaUtil.getPlaceholderImage(context, it) })
        this.cameraButton.setImageBitmap(cameraDrawable?.let { MediaUtil.getPlaceholderImage(context, it) })
        this.videoButton.setImageBitmap(videoDrawable?.let { MediaUtil.getPlaceholderImage(context, it) })

        this.imageButton.setOnClickListener(PropagatingClickListener(ADD_GALLERY))
        this.audioButton.setOnClickListener(PropagatingClickListener(ADD_SOUND))
        this.documentButton.setOnClickListener(PropagatingClickListener(ADD_DOCUMENT))
        this.cameraButton.setOnClickListener(PropagatingClickListener(TAKE_PHOTO))
        this.videoButton.setOnClickListener(PropagatingClickListener(TAKE_VIDEO))
        this.locationButton.setOnClickListener(PropagatingClickListener(LOCATION))


        this.imageButton.borderColor=color
        this.audioButton.borderColor=color
        this.documentButton.borderColor=color
        this.cameraButton.borderColor=color
        this.videoButton.borderColor=color
        this.locationButton.borderColor=color




        contentView = layout
        width = LinearLayout.LayoutParams.MATCH_PARENT
        height = LinearLayout.LayoutParams.WRAP_CONTENT
        setBackgroundDrawable(BitmapDrawable())
        animationStyle = 0
        isClippingEnabled = false
        inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
        isFocusable = true
        isTouchable = true
    }



    fun show(activity: Activity, anchor: View) {

        val y: Int
//        if (KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
//            y = 0
//            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
//            //Find the currently focused view, so we can grab the correct window token from it.
//            imm.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
//        } else {
//            rect = Rect()
//            activity.window.decorView.getWindowVisibleDisplayFrame(rect)
//            winHeight = activity.window.decorView.height
//            y = winHeight - rect!!.bottom
//        }

            rect = Rect()
            activity.window.decorView.getWindowVisibleDisplayFrame(rect)
            winHeight = activity.window.decorView.height
            y = winHeight - rect!!.bottom


        this.currentAnchor = anchor

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            isAttachedInDecor=false
        }

        showAtLocation(anchor, Gravity.BOTTOM, 0,y)


        contentView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                contentView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    animateWindowInCircular(anchor, contentView)
                } else {
                    animateWindowInTranslate(contentView)
                }
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animateButtonIn(imageButton, ANIMATION_DURATION / 2)
            animateButtonIn(cameraButton, ANIMATION_DURATION / 2)
            animateButtonIn(audioButton, ANIMATION_DURATION / 3)
            animateButtonIn(videoButton, ANIMATION_DURATION / 3)
            animateButtonIn(documentButton, ANIMATION_DURATION / 4)
            animateButtonIn(locationButton, 0)
        }
    }

    override fun dismiss() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animateWindowOutCircular(currentAnchor, contentView)
        } else {
            animateWindowOutTranslate(contentView)
        }
    }

    fun setListener(listener: AttachmentClickedListener?) {
        this.listener = listener
    }

    private fun animateButtonIn(button: View, delay: Int) {
        val animation = AnimationSet(true)
        val scale = ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.0f)

        animation.addAnimation(scale)
        animation.interpolator = OvershootInterpolator(1f)
        animation.duration = ANIMATION_DURATION.toLong()
        animation.startOffset = delay.toLong()
        button.startAnimation(animation)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun animateWindowInCircular(anchor: View?, contentView: View) {
        val coordinates = AnimUtil.getClickOrigin(anchor, contentView)
        val animator = ViewAnimationUtils.createCircularReveal(contentView,
                coordinates.first,
                coordinates.second,
                0f,
                Math.max(contentView.width, contentView.height).toFloat())
        animator.duration = ANIMATION_DURATION.toLong()
        animator.start()
    }

    private fun animateWindowInTranslate(contentView: View) {
        val animation = TranslateAnimation(0f, 0f, contentView.height.toFloat(), 0f)
        animation.duration = ANIMATION_DURATION.toLong()

        getContentView().startAnimation(animation)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun animateWindowOutCircular(anchor: View?, contentView: View) {
        val coordinates = AnimUtil.getClickOrigin(anchor, contentView)
        val animator = ViewAnimationUtils.createCircularReveal(getContentView(),
                coordinates.first,
                coordinates.second,
                Math.max(getContentView().width, getContentView().height).toFloat(),
                0f)

        animator.duration = ANIMATION_DURATION.toLong()
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                super@AttachmentTypeSelector.dismiss()
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })

        animator.start()
    }

    private fun animateWindowOutTranslate(contentView: View) {
        val animation = TranslateAnimation(0f, 0f, 0f, (contentView.top + contentView.height).toFloat())
        animation.duration = ANIMATION_DURATION.toLong()
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                super@AttachmentTypeSelector.dismiss()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        getContentView().startAnimation(animation)
    }


    private inner class PropagatingClickListener (private val type: Int) : View.OnClickListener {

        override fun onClick(v: View) {
            animateWindowOutTranslate(contentView)

            if (listener != null) listener!!.onClick(type)
        }

    }

    private inner class CloseClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            dismiss()
        }
    }

    interface AttachmentClickedListener {
        fun onClick(type: Int)

    }

}