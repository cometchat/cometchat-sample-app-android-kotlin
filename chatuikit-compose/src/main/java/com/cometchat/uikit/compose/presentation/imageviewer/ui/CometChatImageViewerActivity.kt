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
 * Host Activity that launches the Compose image viewer screen.
 *
 * Replaces the existing View-based CometChatImageViewerActivity with a
 * ComponentActivity using setContent for Jetpack Compose.
 *
 * **Validates: Requirements 1.1, 6.1, 6.2, 6.3, 7.1, 7.2**
 */
class CometChatImageViewerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // List extras (multi-attachment grid preview) with the single extras as fallback so
        // pre-existing single-image intents keep working.
        val imageUrls = intent.getStringArrayListExtra(EXTRA_IMAGE_URLS)
            ?: arrayListOf(intent.getStringExtra(EXTRA_IMAGE_URL).orEmpty())
        val fileNames = intent.getStringArrayListExtra(EXTRA_FILE_NAMES)
            ?: arrayListOf(intent.getStringExtra(EXTRA_FILE_NAME).orEmpty())
        val mimeTypes = intent.getStringArrayListExtra(EXTRA_MIME_TYPES)
            ?: arrayListOf(intent.getStringExtra(EXTRA_MIME_TYPE).orEmpty())
        val startIndex = intent.getIntExtra(EXTRA_START_INDEX, 0)

        setContent {
            CometChatImageViewerScreen(
                imageUrls = imageUrls,
                fileNames = fileNames,
                mimeTypes = mimeTypes,
                initialPage = startIndex,
                onBack = { finish() },
                onDownload = { url, name, _ -> downloadImage(url, name) },
                onShare = { url, name, mime -> shareImage(url, name, mime) }
            )
        }
    }

    /**
     * Saves the image to the public Downloads directory via [DownloadManager] without opening the
     * share sheet. Shows a system download notification on completion.
     */
    private fun downloadImage(url: String, fileName: String) {
        if (url.isEmpty()) {
            Log.e(TAG, "Cannot download image, url is empty")
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
            Log.e(TAG, "Failed to download image: ${e.message}")
        }
    }

    /**
     * Downloads the image and opens the Android share sheet.
     * Validates that url, fileName, and mimeType are all non-empty before proceeding.
     */
    private fun shareImage(url: String, fileName: String, mimeType: String) {
        if (!ImageViewerUtils.isShareValid(url, fileName, mimeType)) {
            Log.e(TAG, "Cannot share image, url or mimeType or filename is empty")
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
                        Log.e(TAG, "Failed to download image: HTTP ${connection.responseCode}")
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
                        val uri = FileProvider.getUriForFile(
                            this,
                            "$packageName.provider",
                            file
                        )
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_STREAM, uri)
                            type = mimeType
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivity(Intent.createChooser(shareIntent, null))
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to share image: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download image for sharing: ${e.message}")
            }
        }.start()
    }

    override fun finish() {
        super.finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, R.anim.cometchat_fade_out_fast)
    }

    companion object {
        private const val EXTRA_IMAGE_URL = "extra_image_url"
        private const val EXTRA_FILE_NAME = "extra_file_name"
        private const val EXTRA_MIME_TYPE = "extra_mime_type"
        private const val EXTRA_IMAGE_URLS = "extra_image_urls"
        private const val EXTRA_FILE_NAMES = "extra_file_names"
        private const val EXTRA_MIME_TYPES = "extra_mime_types"
        private const val EXTRA_START_INDEX = "extra_start_index"
        private const val TAG = "CometChatImageViewerActivity"

        /**
         * Creates an Intent to launch the image viewer for a single image.
         *
         * @param context The context to create the intent from
         * @param imageUrl Remote URL of the image to display
         * @param fileName Filename for the downloaded file (used in share)
         * @param mimeType MIME type of the image (e.g., image/jpeg)
         */
        fun createIntent(
            context: Context,
            imageUrl: String,
            fileName: String,
            mimeType: String
        ): Intent = createIntent(context, listOf(imageUrl), listOf(fileName), listOf(mimeType))

        /**
         * Creates an Intent to launch the image viewer for a set of images with swipe navigation
         * (multi-attachment grid preview), opened at [startIndex].
         *
         * @param context The context to create the intent from
         * @param imageUrls Urls of the images, in grid order
         * @param fileNames Filenames parallel to [imageUrls] (used in share)
         * @param mimeTypes MIME types parallel to [imageUrls]
         * @param startIndex Index of the image to show first
         */
        fun createIntent(
            context: Context,
            imageUrls: List<String>,
            fileNames: List<String>,
            mimeTypes: List<String>,
            startIndex: Int = 0
        ): Intent {
            return Intent(context, CometChatImageViewerActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_IMAGE_URLS, ArrayList(imageUrls))
                putStringArrayListExtra(EXTRA_FILE_NAMES, ArrayList(fileNames))
                putStringArrayListExtra(EXTRA_MIME_TYPES, ArrayList(mimeTypes))
                putExtra(EXTRA_START_INDEX, startIndex)
            }
        }
    }
}
