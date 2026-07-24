package com.cometchat.uikit.kotlin.presentation.shared.mediaviewer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.VideoView

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.resources.utils.MediaUtils

/**
 * In-app (non full-screen) video player activity.
 *
 * Plays videos inside the app using framework [VideoView]s (no extra player dependency), one per
 * [ViewPager] page so a multi-attachment message can be swiped through — mirroring
 * [CometChatImageViewerActivity]. Only the current page plays; the rest are paused on swipe. The
 * player is constrained within the system bar insets via the layout's `fitsSystemWindows`, and
 * provides download (save to device) and share actions alongside a back arrow.
 */
class CometChatVideoViewerActivity : AppCompatActivity() {

    companion object {
        private const val ARGS_VIDEO_URLS = "ARGS_VIDEO_URLS"
        private const val ARGS_FILE_NAMES = "ARGS_FILE_NAMES"
        private const val ARGS_MIME_TYPES = "ARGS_MIME_TYPES"
        private const val ARGS_START_INDEX = "ARGS_START_INDEX"
        private const val TAG = "CometChatVideoViewerActivity"

        /**
         * Creates an Intent to launch the in-app video player for a single video.
         */
        @JvmStatic
        fun createIntent(
            context: Context,
            url: String,
            fileName: String,
            mimeType: String
        ): Intent = createIntent(context, listOf(url), listOf(fileName), listOf(mimeType))

        /**
         * Creates an Intent to launch the in-app video player for a set of videos with swipe
         * navigation (multi-attachment message), opened at [startIndex].
         *
         * @param context The context to create the intent from
         * @param urls Urls of the videos, in order
         * @param fileNames Filenames parallel to [urls] (used in download/share)
         * @param mimeTypes MIME types parallel to [urls]
         * @param startIndex Index of the video to show first
         */
        @JvmStatic
        @JvmOverloads
        fun createIntent(
            context: Context,
            urls: List<String>,
            fileNames: List<String>,
            mimeTypes: List<String>,
            startIndex: Int = 0
        ): Intent {
            return Intent(context, CometChatVideoViewerActivity::class.java).apply {
                putExtra(ARGS_VIDEO_URLS, java.io.Serializable::class.java.cast(urls))
                putExtra(ARGS_FILE_NAMES, java.io.Serializable::class.java.cast(fileNames))
                putExtra(ARGS_MIME_TYPES, java.io.Serializable::class.java.cast(mimeTypes))
                putExtra(ARGS_START_INDEX, startIndex)
            }
        }
    }

    private var urls: List<String> = emptyList()
    private var fileNames: List<String> = emptyList()
    private var mimeTypes: List<String> = emptyList()
    private var initialPos = 0
    private var currentPos = 0

    private lateinit var viewPager: ViewPager
    private lateinit var toolbar: Toolbar
    private lateinit var downloadBtn: ImageView
    private lateinit var shareBtn: ImageView
    private var adapter: VideoPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cometchat_activity_video_viewer)

        viewPager = findViewById(R.id.video_pager)
        toolbar = findViewById(R.id.toolbar)
        downloadBtn = findViewById(R.id.button_download)
        shareBtn = findViewById(R.id.button_share)

        @Suppress("UNCHECKED_CAST")
        urls = (intent.getSerializableExtra(ARGS_VIDEO_URLS) as? List<String>).orEmpty()
        @Suppress("UNCHECKED_CAST")
        fileNames = (intent.getSerializableExtra(ARGS_FILE_NAMES) as? List<String>).orEmpty()
        @Suppress("UNCHECKED_CAST")
        mimeTypes = (intent.getSerializableExtra(ARGS_MIME_TYPES) as? List<String>).orEmpty()
        initialPos = intent.getIntExtra(ARGS_START_INDEX, 0)
            .coerceIn(0, (urls.lastIndex).coerceAtLeast(0))
        currentPos = initialPos

        if (urls.isEmpty()) {
            Log.e(TAG, "No video url to display")
            finish()
            return
        }

        initToolbar()
        downloadBtn.setOnClickListener { downloadMessage() }
        shareBtn.setOnClickListener { shareMessage() }
        initViewPager()
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
        adapter = VideoPagerAdapter(urls)
        viewPager.adapter = adapter
        viewPager.currentItem = initialPos
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                currentPos = position
                adapter?.playOnly(position)
            }
        })
        // Kick off playback for the initially shown page once the pager has laid out.
        viewPager.post { adapter?.playOnly(initialPos) }
    }

    private fun downloadMessage() {
        val url = urls.getOrNull(currentPos)
        val name = fileNames.getOrNull(currentPos)
        if (url.isNullOrEmpty() || name.isNullOrEmpty()) {
            Log.e(TAG, "Cannot download video, url or filename is null")
            return
        }
        // Save directly to the device (Downloads); no share sheet. The filename already carries
        // its extension, so pass an empty extension to avoid duplicating it.
        MediaUtils.downloadFile(context = this, url = url, fileName = name, extension = "")
    }

    private fun shareMessage() {
        val url = urls.getOrNull(currentPos)
        val name = fileNames.getOrNull(currentPos)
        val mime = mimeTypes.getOrNull(currentPos)
        if (url.isNullOrEmpty() || name.isNullOrEmpty() || mime.isNullOrEmpty()) {
            Log.e(TAG, "Cannot share video, url or filename or mimeType is null")
            return
        }
        MediaUtils.downloadFileAndShare(context = this, fileUrl = url, fileName = name, mimeType = mime)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onPause() {
        super.onPause()
        adapter?.pauseAll()
    }

    override fun onResume() {
        super.onResume()
        adapter?.playOnly(currentPos)
    }

    override fun onDestroy() {
        adapter?.releaseAll()
        adapter = null
        super.onDestroy()
    }

    override fun finish() {
        super.finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, R.anim.cometchat_fade_out_fast)
    }

    /** Hosts one [VideoView] per video; plays only the current page. */
    private inner class VideoPagerAdapter(private val urls: List<String>) : PagerAdapter() {

        private val videoViews = HashMap<Int, VideoView>()

        override fun getCount(): Int = urls.size

        override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = View.inflate(container.context, R.layout.cometchat_item_video, null)
            val videoView: VideoView = view.findViewById(R.id.video_view)
            val progressBar: ProgressBar = view.findViewById(R.id.item_progress_bar)

            val controller = MediaController(container.context)
            controller.setAnchorView(videoView)
            videoView.setMediaController(controller)
            videoView.setVideoURI(Uri.parse(urls[position]))
            videoView.setOnPreparedListener {
                progressBar.visibility = View.GONE
                if (position == currentPos) videoView.start()
            }
            videoView.setOnErrorListener { _, _, _ ->
                progressBar.visibility = View.GONE
                Log.e(TAG, "Failed to play video: ${urls[position]}")
                false
            }

            container.addView(view)
            videoViews[position] = videoView
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            videoViews[position]?.stopPlayback()
            videoViews.remove(position)
            container.removeView(obj as View)
        }

        fun playOnly(position: Int) {
            videoViews.forEach { (pos, vv) ->
                if (pos == position) vv.start() else if (vv.isPlaying) vv.pause()
            }
        }

        fun pauseAll() {
            videoViews.values.forEach { if (it.isPlaying) it.pause() }
        }

        fun releaseAll() {
            videoViews.values.forEach { it.stopPlayback() }
            videoViews.clear()
        }
    }
}
