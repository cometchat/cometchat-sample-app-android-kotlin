package com.cometchat.uikit.kotlin.presentation.shared.mediaviewer

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout

import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.resources.utils.MediaUtils
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Full-screen image viewer activity that displays images with pinch-to-zoom,
 * drag-to-dismiss, toolbar overlay, and share action.
 *
 * This is a 1:1 Kotlin port of the Java CometChatImageViewerActivity from the
 * chatuikit module.
 */
class CometChatImageViewerActivity : AppCompatActivity() {

    companion object {
        private const val ARGS_IMAGE_URLS = "ARGS_IMAGE_URLS"
        private const val ARGS_FILE_NAME = "ARGS_FILE_NAME"
        private const val MIME_TYPE_URL = "MIME_TYPE_URL"
        private const val ARGS_START_INDEX = "ARGS_START_INDEX"
        private const val TAG = "CometChatImageViewerActivity"

        /**
         * Creates an Intent to launch the image viewer.
         *
         * @param context The context to create the intent from
         * @param urls List of image URLs to display
         * @param mimeType List of MIME types for each image
         * @param filenames List of filenames for each image
         * @param startIndex Index of the image to show first (multi-attachment grid preview)
         */
        @JvmStatic
        @JvmOverloads
        fun createIntent(
            context: Context,
            urls: List<String>,
            mimeType: List<String>,
            filenames: List<String>,
            startIndex: Int = 0
        ): Intent {
            return Intent(context, CometChatImageViewerActivity::class.java).apply {
                putExtra(ARGS_IMAGE_URLS, java.io.Serializable::class.java.cast(urls))
                putExtra(MIME_TYPE_URL, java.io.Serializable::class.java.cast(mimeType))
                putExtra(ARGS_FILE_NAME, java.io.Serializable::class.java.cast(filenames))
                putExtra(ARGS_START_INDEX, startIndex)
            }
        }
    }

    private var urls: List<String>? = null
    private var mimeTypes: List<String>? = null
    private var filenames: List<String>? = null
    private var initialPos = 0
    private var adapter: ImageAdapter? = null
    private lateinit var viewPager: ViewPager
    private lateinit var toolbar: Toolbar
    private lateinit var topBar: LinearLayout
    private lateinit var downloadBtn: ImageView
    private lateinit var shareBtn: ImageView
    private lateinit var progressBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
        setContentView(R.layout.cometchat_activity_image_viewer)

        viewPager = findViewById(R.id.viewpager)
        toolbar = findViewById(R.id.toolbar)
        topBar = findViewById(R.id.top_bar_container)
        downloadBtn = findViewById(R.id.button_download)
        shareBtn = findViewById(R.id.button_share)
        progressBar = findViewById(R.id.progress_bar)

        @Suppress("UNCHECKED_CAST")
        urls = intent.getSerializableExtra(ARGS_IMAGE_URLS) as? List<String>
        @Suppress("UNCHECKED_CAST")
        mimeTypes = intent.getSerializableExtra(MIME_TYPE_URL) as? List<String>
        @Suppress("UNCHECKED_CAST")
        filenames = intent.getSerializableExtra(ARGS_FILE_NAME) as? List<String>
        initialPos = intent.getIntExtra(ARGS_START_INDEX, 0)
            .coerceIn(0, (urls?.lastIndex ?: 0).coerceAtLeast(0))

        initViews()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                adapter?.clear()
                finish()
            }
        })
    }

    private fun initViews() {
        toggleProgressBarVisibility(View.VISIBLE)
        initToolbar()
        initViewPager()
        downloadBtn.setOnClickListener { downloadMessage() }
        shareBtn.setOnClickListener { shareMessage() }
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            title = ""
        }
    }

    private fun initViewPager() {
        adapter = ImageAdapter(this, urls ?: emptyList(), mimeTypes ?: emptyList())
        viewPager.adapter = adapter
        viewPager.currentItem = initialPos
    }

    private fun downloadMessage() {
        if (urls.isNullOrEmpty() || filenames.isNullOrEmpty()) {
            Log.e(TAG, "Cannot download image, urls or filenames are null")
            return
        }
        val currentPos = adapter?.currentPos ?: 0
        // Save directly to the device (Downloads); no share sheet. The filename already carries
        // its extension, so pass an empty extension to avoid duplicating it.
        MediaUtils.downloadFile(
            context = this,
            url = urls!![currentPos],
            fileName = filenames!![currentPos],
            extension = ""
        )
    }

    private fun shareMessage() {
        if (urls.isNullOrEmpty() || mimeTypes.isNullOrEmpty() || filenames.isNullOrEmpty()) {
            Log.e(TAG, "Cannot share image, urls or mimeTypes or filenames are null")
            return
        }
        val currentPos = adapter?.currentPos ?: 0
        MediaUtils.downloadFileAndShare(
            context = this,
            fileUrl = urls!![currentPos],
            fileName = filenames!![currentPos],
            mimeType = mimeTypes!![currentPos]
        )
    }

    private fun toggleProgressBarVisibility(visibility: Int) {
        progressBar.visibility = visibility
    }

    override fun finish() {
        super.finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, R.anim.cometchat_fade_out_fast)
    }

    override fun onDestroy() {
        adapter = null
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        supportFinishAfterTransition()
        finish()
        return true
    }

    private fun showToolbar() {
        topBar.animate()
            .setInterpolator(AccelerateDecelerateInterpolator())
            .translationY(0f)
    }

    private fun hideToolbar() {
        topBar.animate()
            .setInterpolator(AccelerateDecelerateInterpolator())
            .translationY(-toolbar.height.toFloat())
    }

    inner class ImageAdapter(
        private val context: Context,
        private val urls: List<String>,
        private val mimeTypes: List<String> = emptyList()
    ) : PagerAdapter() {

        private val previewMap = HashMap<Int, CometChatImagePreview>()
        private val views = HashMap<Int, ImageView>()
        var currentPos = 0
            private set

        override fun getCount(): Int = urls.size

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            // Non-image page (kind-mismatched attachment of a server-sent mixed payload) — show
            // the Google Drive-style "No preview available" page with its own Download button.
            // An EMPTY mime (legacy message) still tries the normal image preview.
            val mime = mimeTypes.getOrElse(position) { "" }.lowercase()
            if (mime.isNotEmpty() && !mime.startsWith("image/")) {
                val view = createNoPreviewPage(position)
                container.addView(view)
                if (position == initialPos) {
                    toggleProgressBarVisibility(View.GONE)
                    startPostponedEnterTransition()
                }
                return view
            }

            val view = View.inflate(context, R.layout.cometchat_item_image, null)
            val image: ImageView = view.findViewById(R.id.image)
            val frameLayout: FrameLayout = view.findViewById(R.id.container)
            container.addView(view)
            loadImage(image, frameLayout, position)
            views[position] = image
            return view
        }

        /** Inflates + theme-binds the "No preview available" page for [position]. */
        private fun createNoPreviewPage(position: Int): View {
            val view = View.inflate(context, R.layout.cometchat_item_no_preview, null)
            // Slightly translucent so the viewer's backdrop shows through the card.
            view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.no_preview_card)
                .setCardBackgroundColor(
                    androidx.core.graphics.ColorUtils.setAlphaComponent(
                        CometChatTheme.getBackgroundColor1(context), 217 // ~0.85 alpha
                    )
                )
            view.findViewById<View>(R.id.no_preview_icon_container).background =
                android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(CometChatTheme.getNeutralColor100(context))
                }
            view.findViewById<android.widget.TextView>(R.id.no_preview_title)
                .setTextColor(CometChatTheme.getTextColorPrimary(context))
            view.findViewById<android.widget.TextView>(R.id.no_preview_subtitle)
                .setTextColor(CometChatTheme.getTextColorSecondary(context))
            val white = CometChatTheme.getColorWhite(context)
            view.findViewById<ImageView>(R.id.no_preview_download_icon).imageTintList =
                android.content.res.ColorStateList.valueOf(white)
            view.findViewById<android.widget.TextView>(R.id.no_preview_download_text).setTextColor(white)
            view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.no_preview_download).apply {
                setCardBackgroundColor(CometChatTheme.getPrimaryColor(context))
                setOnClickListener {
                    val url = urls.getOrElse(position) { "" }
                    val name = filenames?.getOrElse(position) { "" }.orEmpty()
                    if (url.isNotEmpty()) {
                        MediaUtils.downloadFile(context = context, url = url, fileName = name, extension = "")
                    }
                }
            }
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as View)
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
            super.setPrimaryItem(container, position, obj)
            this.currentPos = position
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

        private fun loadImage(image: ImageView, container: ViewGroup, position: Int) {
            Glide.with(image.context)
                .load(urls[position])
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        startPostponedEnterTransition()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        toggleProgressBarVisibility(View.GONE)
                        val cometChatImagePreview = CometChatImagePreviewUtils.createImagePreview(image, container)
                        cometChatImagePreview.setOnViewTranslateListener(object : CometChatImagePreview.OnViewTranslateListener {
                            override fun onStart(view: ImageView) {
                                hideToolbar()
                            }

                            override fun onViewTranslate(view: ImageView, amount: Float) {
                                // No-op
                            }

                            override fun onDismiss(view: ImageView) {
                                finishAfterTransition()
                            }

                            override fun onRestore(view: ImageView) {
                                showToolbar()
                            }
                        })
                        previewMap[position] = cometChatImagePreview
                        if (position == initialPos) {
                            startPostponedEnterTransition()
                        }
                        return false
                    }
                })
                .into(image)
        }

        fun clear() {
            for (cometChatImagePreview in previewMap.values) {
                cometChatImagePreview.cleanup()
            }
            previewMap.clear()
        }
    }
}
