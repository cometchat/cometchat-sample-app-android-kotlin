package com.cometchat.uikit.compose.presentation.imageviewer.ui

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.FileProvider
import com.cometchat.uikit.compose.R
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Host Activity that launches the Compose in-app video player.
 *
 * Mirrors [CometChatImageViewerActivity]: the video plays inside the app (not the external OS
 * player), constrained within the system bar insets, with download (save to device) and share
 * actions.
 */
class CometChatVideoViewerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // List extras (multi-attachment swipe) with the single extras as fallback so pre-existing
        // single-video intents keep working.
        val videoUrls = intent.getStringArrayListExtra(EXTRA_VIDEO_URLS)
            ?: arrayListOf(intent.getStringExtra(EXTRA_VIDEO_URL).orEmpty())
        val fileNames = intent.getStringArrayListExtra(EXTRA_FILE_NAMES)
            ?: arrayListOf(intent.getStringExtra(EXTRA_FILE_NAME).orEmpty())
        val mimeTypes = intent.getStringArrayListExtra(EXTRA_MIME_TYPES)
            ?: arrayListOf(intent.getStringExtra(EXTRA_MIME_TYPE).orEmpty())
        val startIndex = intent.getIntExtra(EXTRA_START_INDEX, 0)

        setContent {
            CometChatVideoViewerScreen(
                videoUrls = videoUrls,
                fileNames = fileNames,
                mimeTypes = mimeTypes,
                initialPage = startIndex,
                onBack = { finish() },
                onDownload = { url, name, _ -> downloadVideo(url, name) },
                onShare = { url, name, mime -> shareVideo(url, name, mime) }
            )
        }
    }

    /**
     * Saves the video to the public Downloads directory via [DownloadManager] without opening the
     * share sheet. Shows a system download notification on completion.
     */
    private fun downloadVideo(url: String, fileName: String) {
        if (url.isEmpty()) {
            Log.e(TAG, "Cannot download video, url is empty")
            return
        }
        try {
            val safeName = fileName.ifEmpty { url.substringAfterLast('/').ifEmpty { "download" } }
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(safeName)
                .setDescription("Downloading…")
                .setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeName)
            (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
            Toast.makeText(this, R.string.cometchat_downloading, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download video: ${e.message}")
        }
    }

    /**
     * Downloads the video and opens the Android share sheet.
     */
    private fun shareVideo(url: String, fileName: String, mimeType: String) {
        if (url.isEmpty() || fileName.isEmpty() || mimeType.isEmpty()) {
            Log.e(TAG, "Cannot share video, url or mimeType or filename is empty")
            return
        }

        Thread {
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                val file = File(downloadsDir, fileName)

                if (!file.exists()) {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.connect()
                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        Log.e(TAG, "Failed to download video: HTTP ${connection.responseCode}")
                        return@Thread
                    }
                    connection.inputStream.use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                runOnUiThread {
                    try {
                        val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_STREAM, uri)
                            type = mimeType
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivity(Intent.createChooser(shareIntent, null))
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to share video: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download video for sharing: ${e.message}")
            }
        }.start()
    }

    override fun finish() {
        super.finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, R.anim.cometchat_fade_out_fast)
    }

    companion object {
        private const val EXTRA_VIDEO_URL = "extra_video_url"
        private const val EXTRA_FILE_NAME = "extra_file_name"
        private const val EXTRA_MIME_TYPE = "extra_mime_type"
        private const val EXTRA_VIDEO_URLS = "extra_video_urls"
        private const val EXTRA_FILE_NAMES = "extra_file_names"
        private const val EXTRA_MIME_TYPES = "extra_mime_types"
        private const val EXTRA_START_INDEX = "extra_start_index"
        private const val TAG = "CometChatVideoViewerActivity"

        /**
         * Creates an Intent to launch the in-app video player for a single video.
         *
         * @param context The context to create the intent from
         * @param videoUrl Remote URL of the video to play
         * @param fileName Filename for the downloaded file (used in download/share)
         * @param mimeType MIME type of the video (e.g., video/mp4)
         */
        fun createIntent(
            context: Context,
            videoUrl: String,
            fileName: String,
            mimeType: String
        ): Intent = createIntent(context, listOf(videoUrl), listOf(fileName), listOf(mimeType))

        /**
         * Creates an Intent to launch the in-app video player for a set of videos with swipe
         * navigation (multi-attachment message), opened at [startIndex].
         *
         * @param context The context to create the intent from
         * @param videoUrls Urls of the videos, in order
         * @param fileNames Filenames parallel to [videoUrls] (used in download/share)
         * @param mimeTypes MIME types parallel to [videoUrls]
         * @param startIndex Index of the video to show first
         */
        fun createIntent(
            context: Context,
            videoUrls: List<String>,
            fileNames: List<String>,
            mimeTypes: List<String>,
            startIndex: Int = 0
        ): Intent {
            return Intent(context, CometChatVideoViewerActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_VIDEO_URLS, ArrayList(videoUrls))
                putStringArrayListExtra(EXTRA_FILE_NAMES, ArrayList(fileNames))
                putStringArrayListExtra(EXTRA_MIME_TYPES, ArrayList(mimeTypes))
                putExtra(EXTRA_START_INDEX, startIndex)
            }
        }
    }
}
