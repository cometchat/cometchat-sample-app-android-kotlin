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
        private const val TAG = "CometChatImageViewerActivity"

        /**
         * Creates an Intent to launch the image viewer.
         *
         * @param context The context to create the intent from
         * @param urls List of image URLs to display
         * @param mimeType List of MIME types for each image
         * @param filenames List of filenames for each image
         */
        @JvmStatic
        fun createIntent(
            context: Context,
            urls: List<String>,
            mimeType: List<String>,
            filenames: List<String>
        ): Intent {
            return Intent(context, CometChatImageViewerActivity::class.java).apply {
                putExtra(ARGS_IMAGE_URLS, java.io.Serializable::class.java.cast(urls))
                putExtra(MIME_TYPE_URL, java.io.Serializable::class.java.cast(mimeType))
                putExtra(ARGS_FILE_NAME, java.io.Serializable::class.java.cast(filenames))
            }
        }
    }

    private var urls: List<String>? = null
    private var mimeTypes: List<String>? = null
    private var filenames: List<String>? = null
    private val initialPos = 0
    private var adapter: ImageAdapter? = null
    private lateinit var viewPager: ViewPager
    private lateinit var toolbar: Toolbar
    private lateinit var topBar: LinearLayout
    private lateinit var shareBtn: ImageView
    private lateinit var progressBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
        setContentView(R.layout.cometchat_activity_image_viewer)

        viewPager = findViewById(R.id.viewpager)
        toolbar = findViewById(R.id.toolbar)
        topBar = findViewById(R.id.top_bar_container)
        shareBtn = findViewById(R.id.button_share)
        progressBar = findViewById(R.id.progress_bar)

        @Suppress("UNCHECKED_CAST")
        urls = intent.getSerializableExtra(ARGS_IMAGE_URLS) as? List<String>
        @Suppress("UNCHECKED_CAST")
        mimeTypes = intent.getSerializableExtra(MIME_TYPE_URL) as? List<String>
        @Suppress("UNCHECKED_CAST")
        filenames = intent.getSerializableExtra(ARGS_FILE_NAME) as? List<String>

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
        adapter = ImageAdapter(this, urls ?: emptyList())
        viewPager.adapter = adapter
        viewPager.currentItem = initialPos
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
        private val urls: List<String>
    ) : PagerAdapter() {

        private val previewMap = HashMap<Int, CometChatImagePreview>()
        private val views = HashMap<Int, ImageView>()
        var currentPos = 0
            private set

        override fun getCount(): Int = urls.size

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = View.inflate(context, R.layout.cometchat_item_image, null)
            val image: ImageView = view.findViewById(R.id.image)
            val frameLayout: FrameLayout = view.findViewById(R.id.container)
            container.addView(view)
            loadImage(image, frameLayout, position)
            views[position] = image
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
