package com.cometchat.pro.uikit.ComposeBox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.cometchat.pro.uikit.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ComposeBoxActionFragment : BottomSheetDialogFragment() {

    private lateinit var galleryMessage: TextView
    private lateinit var cameraMessage: TextView
    private lateinit var fileMessage: TextView
    private lateinit var audioMessage: TextView
    private lateinit var locationMessage: TextView
    private lateinit var stickerMessage: TextView
    private lateinit var whiteBoardMessage: TextView
    private lateinit var writeBoardMessage: TextView

    private var isGalleryVisible = false
    private var isCameraVisible = false
    private var isAudioVisible = false
    private var isFileVisible = false
    private var isLocationVisible = false
    private var isStickerVisible = false
    private var isWhiteBoardVisible = false
    private var isWriteBoardVisible = false


    private var composeBoxActionListener: ComposeBoxActionListener? = null

    private var type: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isGalleryVisible = arguments!!.getBoolean("isGalleryVisible")
        isCameraVisible = arguments!!.getBoolean("isCameraVisible")
        isFileVisible = arguments!!.getBoolean("isFileVisible")
        isAudioVisible = arguments!!.getBoolean("isAudioVisible")
        isLocationVisible = arguments!!.getBoolean("isLocationVisible")
        isStickerVisible = arguments!!.getBoolean("isStickerVisible")
        isWhiteBoardVisible = arguments!!.getBoolean("isWhiteBoardVisible")
        isWriteBoardVisible = arguments!!.getBoolean("isWriteBoardVisible")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        var view: View = inflater.inflate(R.layout.fragment_composebox_actions, container, false)

        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog?
            // androidx should use: com.google.android.material.R.id.design_bottom_sheet
            val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = 0
        }

        galleryMessage = view.findViewById(R.id.gallery_message)
        cameraMessage = view.findViewById(R.id.camera_message)
        fileMessage = view.findViewById(R.id.file_message)
        audioMessage = view.findViewById(R.id.audio_message)
        locationMessage = view.findViewById(R.id.location_message)
        stickerMessage = view.findViewById(R.id.sticker_message)
        whiteBoardMessage = view.findViewById(R.id.whiteboard_message)
        writeBoardMessage = view.findViewById(R.id.writeboard_message)

        if (isGalleryVisible) galleryMessage.visibility = View.VISIBLE else galleryMessage.visibility = View.GONE
        if (isCameraVisible) cameraMessage.visibility = View.VISIBLE else cameraMessage.visibility = View.GONE
        if (isFileVisible) fileMessage.visibility = View.VISIBLE else fileMessage.visibility = View.GONE
        if (isAudioVisible) audioMessage.visibility = View.VISIBLE else audioMessage.visibility = View.GONE
        if (isLocationVisible) locationMessage.visibility = View.VISIBLE else locationMessage.visibility = View.GONE
        if (isStickerVisible) stickerMessage.visibility = View.VISIBLE else stickerMessage.visibility = View.GONE
        if (isWhiteBoardVisible) whiteBoardMessage.visibility = View.VISIBLE else whiteBoardMessage.visibility = View.GONE
        if (isWriteBoardVisible) writeBoardMessage.visibility = View.VISIBLE else writeBoardMessage.visibility = View.GONE

        galleryMessage.setOnClickListener {
            if (composeBoxActionListener != null) composeBoxActionListener!!.onGalleryClick()
            dismiss()
        }
        cameraMessage.setOnClickListener {
            if (composeBoxActionListener != null) composeBoxActionListener!!.onCameraClick()
            dismiss()
        }
        fileMessage.setOnClickListener {
            if (composeBoxActionListener != null) composeBoxActionListener!!.onFileClick()
            dismiss()
        }
        audioMessage.setOnClickListener {
            if (composeBoxActionListener != null) composeBoxActionListener!!.onAudioClick()
            dismiss()
        }
        locationMessage.setOnClickListener {
            if (composeBoxActionListener != null) composeBoxActionListener!!.onLocationClick()
            dismiss()
        }
        stickerMessage.setOnClickListener {
            if (composeBoxActionListener != null) composeBoxActionListener!!.onStickerClick()
            dismiss()
        }
        whiteBoardMessage.setOnClickListener(View.OnClickListener {
            if (composeBoxActionListener != null) composeBoxActionListener!!.onWhiteBoardClick()
            dismiss()
        })
        writeBoardMessage.setOnClickListener(View.OnClickListener {
            if (composeBoxActionListener != null) composeBoxActionListener!!.onWriteBoardClick()
            dismiss()
        })

        return view
    }

    fun setComposeBoxActionListener(composeBoxActionListener: ComposeBoxActionListener?) {
        this.composeBoxActionListener = composeBoxActionListener
    }

    interface ComposeBoxActionListener {
        fun onGalleryClick()
        fun onCameraClick()
        fun onFileClick()
        fun onAudioClick()
        fun onLocationClick()
        fun onStickerClick()
        fun onWhiteBoardClick()
        fun onWriteBoardClick()
    }
}