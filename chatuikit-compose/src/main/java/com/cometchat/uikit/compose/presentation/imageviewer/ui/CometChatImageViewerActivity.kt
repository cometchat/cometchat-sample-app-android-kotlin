package com.cometchat.uikit.compose.presentation.imageviewer.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
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

        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL).orEmpty()
        val fileName = intent.getStringExtra(EXTRA_FILE_NAME).orEmpty()
        val mimeType = intent.getStringExtra(EXTRA_MIME_TYPE).orEmpty()

        setContent {
            CometChatImageViewerScreen(
                imageUrl = imageUrl,
                fileName = fileName,
                mimeType = mimeType,
                onBack = { finish() },
                onShare = { url, name, mime -> shareImage(url, name, mime) }
            )
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
        private const val TAG = "CometChatImageViewerActivity"

        /**
         * Creates an Intent to launch the image viewer.
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
        ): Intent {
            return Intent(context, CometChatImageViewerActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_URL, imageUrl)
                putExtra(EXTRA_FILE_NAME, fileName)
                putExtra(EXTRA_MIME_TYPE, mimeType)
            }
        }
    }
}
