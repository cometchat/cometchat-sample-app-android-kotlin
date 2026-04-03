package com.cometchat.uikit.kotlin.presentation.shared.mediaselection

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileOutputStream

/**
 * Media content types supported by the media selection utilities.
 */
object MediaContentType {
    const val IMAGE = "image"
    const val VIDEO = "video"
    const val AUDIO = "audio"
    const val FILE = "file"
    const val UNKNOWN = "unknown"
}

/**
 * Result of a media selection operation.
 *
 * @property uri The URI of the selected media
 * @property file The file created from the URI (if copied to app storage)
 * @property contentType The detected content type of the media
 * @property fileName The original file name
 * @property mimeType The MIME type of the media
 * @property fileSize The size of the file in bytes
 */
data class MediaSelectionResult(
    val uri: Uri,
    val file: File?,
    val contentType: String,
    val fileName: String,
    val mimeType: String?,
    val fileSize: Long
)

/**
 * Callback interface for media selection results.
 */
interface MediaSelectionCallback {
    /**
     * Called when media is successfully selected.
     *
     * @param result The media selection result
     */
    fun onMediaSelected(result: MediaSelectionResult)
    
    /**
     * Called when media selection is cancelled.
     */
    fun onSelectionCancelled() {}
    
    /**
     * Called when an error occurs during media selection.
     *
     * @param exception The exception that occurred
     */
    fun onError(exception: Exception) {}
}

/**
 * MediaSelectionHelper provides utilities for selecting media files from the device.
 *
 * Features:
 * - Image picker using Photo Picker API
 * - Video picker using Photo Picker API
 * - Audio picker using GetContent
 * - File picker using GetContent
 * - Camera capture for photos
 * - Video capture
 * - Content type detection
 * - File copying to app cache
 *
 * Usage with Activity:
 * ```kotlin
 * class MyActivity : ComponentActivity() {
 *     private lateinit var mediaHelper: MediaSelectionHelper
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         mediaHelper = MediaSelectionHelper(this)
 *     }
 *
 *     fun selectImage() {
 *         mediaHelper.launchImagePicker(object : MediaSelectionCallback {
 *             override fun onMediaSelected(result: MediaSelectionResult) {
 *                 // Handle selected image
 *             }
 *         })
 *     }
 * }
 * ```
 *
 * Usage with Fragment:
 * ```kotlin
 * class MyFragment : Fragment() {
 *     private lateinit var mediaHelper: MediaSelectionHelper
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         mediaHelper = MediaSelectionHelper(this)
 *     }
 * }
 * ```
 */
class MediaSelectionHelper {
    
    private val context: Context
    private var currentCallback: MediaSelectionCallback? = null
    private var cameraFile: File? = null
    private var cameraUri: Uri? = null
    
    // Activity result launchers
    private val imagePickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private val videoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private val imageAndVideoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private val audioPickerLauncher: ActivityResultLauncher<String>
    private val filePickerLauncher: ActivityResultLauncher<String>
    private val cameraLauncher: ActivityResultLauncher<Uri>
    private val videoCaptureLauncher: ActivityResultLauncher<Uri>
    
    /**
     * Creates a MediaSelectionHelper for an Activity.
     *
     * @param activity The ComponentActivity to use for launching pickers
     */
    constructor(activity: ComponentActivity) {
        this.context = activity
        
        imagePickerLauncher = activity.registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri -> handlePickerResult(uri) }
        
        videoPickerLauncher = activity.registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri -> handlePickerResult(uri) }
        
        imageAndVideoPickerLauncher = activity.registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri -> handlePickerResult(uri) }
        
        audioPickerLauncher = activity.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri -> handlePickerResult(uri) }
        
        filePickerLauncher = activity.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri -> handlePickerResult(uri) }
        
        cameraLauncher = activity.registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success -> handleCameraResult(success, MediaContentType.IMAGE) }
        
        videoCaptureLauncher = activity.registerForActivityResult(
            ActivityResultContracts.CaptureVideo()
        ) { success -> handleCameraResult(success, MediaContentType.VIDEO) }
    }
    
    /**
     * Creates a MediaSelectionHelper for a Fragment.
     *
     * @param fragment The Fragment to use for launching pickers
     */
    constructor(fragment: Fragment) {
        this.context = fragment.requireContext()
        
        imagePickerLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri -> handlePickerResult(uri) }
        
        videoPickerLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri -> handlePickerResult(uri) }
        
        imageAndVideoPickerLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri -> handlePickerResult(uri) }
        
        audioPickerLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri -> handlePickerResult(uri) }
        
        filePickerLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri -> handlePickerResult(uri) }
        
        cameraLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success -> handleCameraResult(success, MediaContentType.IMAGE) }
        
        videoCaptureLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.CaptureVideo()
        ) { success -> handleCameraResult(success, MediaContentType.VIDEO) }
    }
    
    /**
     * Launches the image picker.
     *
     * @param callback Callback for the selection result
     */
    fun launchImagePicker(callback: MediaSelectionCallback) {
        currentCallback = callback
        imagePickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
    
    /**
     * Launches the video picker.
     *
     * @param callback Callback for the selection result
     */
    fun launchVideoPicker(callback: MediaSelectionCallback) {
        currentCallback = callback
        videoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        )
    }
    
    /**
     * Launches the image and video picker.
     *
     * @param callback Callback for the selection result
     */
    fun launchImageAndVideoPicker(callback: MediaSelectionCallback) {
        currentCallback = callback
        imageAndVideoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
        )
    }
    
    /**
     * Launches the audio picker.
     *
     * @param callback Callback for the selection result
     */
    fun launchAudioPicker(callback: MediaSelectionCallback) {
        currentCallback = callback
        audioPickerLauncher.launch("audio/*")
    }
    
    /**
     * Launches the file picker.
     *
     * @param callback Callback for the selection result
     * @param mimeType The MIME type filter (default: all files)
     */
    fun launchFilePicker(callback: MediaSelectionCallback, mimeType: String = "*/*") {
        currentCallback = callback
        filePickerLauncher.launch(mimeType)
    }
    
    /**
     * Launches the camera for photo capture.
     *
     * @param callback Callback for the capture result
     */
    fun launchCamera(callback: MediaSelectionCallback) {
        currentCallback = callback
        createTempFileForCapture("photo", ".jpg")?.let { (file, uri) ->
            cameraFile = file
            cameraUri = uri
            cameraLauncher.launch(uri)
        } ?: run {
            callback.onError(Exception("Failed to create temp file for camera"))
        }
    }
    
    /**
     * Launches the camera for video capture.
     *
     * @param callback Callback for the capture result
     */
    fun launchVideoCapture(callback: MediaSelectionCallback) {
        currentCallback = callback
        createTempFileForCapture("video", ".mp4")?.let { (file, uri) ->
            cameraFile = file
            cameraUri = uri
            videoCaptureLauncher.launch(uri)
        } ?: run {
            callback.onError(Exception("Failed to create temp file for video capture"))
        }
    }
    
    /**
     * Handles the result from picker launchers.
     */
    private fun handlePickerResult(uri: Uri?) {
        if (uri == null) {
            currentCallback?.onSelectionCancelled()
            return
        }
        
        try {
            val result = createMediaSelectionResult(uri)
            currentCallback?.onMediaSelected(result)
        } catch (e: Exception) {
            currentCallback?.onError(e)
        }
    }
    
    /**
     * Handles the result from camera launchers.
     */
    private fun handleCameraResult(success: Boolean, contentType: String) {
        if (!success) {
            currentCallback?.onSelectionCancelled()
            cleanupCameraFiles()
            return
        }
        
        val file = cameraFile
        val uri = cameraUri
        
        if (file == null || uri == null) {
            currentCallback?.onError(Exception("Camera file not found"))
            cleanupCameraFiles()
            return
        }
        
        try {
            val mimeType = when (contentType) {
                MediaContentType.IMAGE -> "image/jpeg"
                MediaContentType.VIDEO -> "video/mp4"
                else -> null
            }
            
            val result = MediaSelectionResult(
                uri = uri,
                file = file,
                contentType = contentType,
                fileName = file.name,
                mimeType = mimeType,
                fileSize = file.length()
            )
            currentCallback?.onMediaSelected(result)
        } catch (e: Exception) {
            currentCallback?.onError(e)
        }
        
        cleanupCameraFiles()
    }
    
    /**
     * Cleans up temporary camera files.
     */
    private fun cleanupCameraFiles() {
        cameraFile = null
        cameraUri = null
    }
    
    /**
     * Creates a MediaSelectionResult from a URI.
     */
    private fun createMediaSelectionResult(uri: Uri, copyToCache: Boolean = true): MediaSelectionResult {
        val mimeType = getMimeType(uri)
        val contentType = detectContentType(mimeType)
        val fileName = getFileName(uri)
        val fileSize = getFileSize(uri)
        
        val file = if (copyToCache) {
            copyUriToFile(uri, fileName)
        } else {
            null
        }
        
        return MediaSelectionResult(
            uri = uri,
            file = file,
            contentType = contentType,
            fileName = fileName,
            mimeType = mimeType,
            fileSize = fileSize
        )
    }
    
    /**
     * Detects the content type from a MIME type string.
     */
    private fun detectContentType(mimeType: String?): String {
        return when {
            mimeType == null -> MediaContentType.UNKNOWN
            mimeType.startsWith("image/") -> MediaContentType.IMAGE
            mimeType.startsWith("video/") -> MediaContentType.VIDEO
            mimeType.startsWith("audio/") -> MediaContentType.AUDIO
            else -> MediaContentType.FILE
        }
    }
    
    /**
     * Gets the MIME type from a URI.
     */
    private fun getMimeType(uri: Uri): String? {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.getType(uri)
        } else {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension?.lowercase())
        }
    }
    
    /**
     * Gets the file name from a URI.
     */
    private fun getFileName(uri: Uri): String {
        var fileName = "unknown"
        
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            }
        } else {
            fileName = uri.lastPathSegment ?: "unknown"
        }
        
        return fileName
    }
    
    /**
     * Gets the file size from a URI.
     */
    private fun getFileSize(uri: Uri): Long {
        var size = 0L
        
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex >= 0) {
                        size = cursor.getLong(sizeIndex)
                    }
                }
            }
        } else {
            uri.path?.let { path ->
                size = File(path).length()
            }
        }
        
        return size
    }
    
    /**
     * Copies a URI content to a file in the app's cache directory.
     */
    private fun copyUriToFile(uri: Uri, fileName: String): File? {
        return try {
            val cacheDir = File(context.cacheDir, "media_selection")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            
            val destinationFile = File(cacheDir, fileName)
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            destinationFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Creates a temporary file for camera capture.
     */
    private fun createTempFileForCapture(prefix: String, extension: String): Pair<File, Uri>? {
        return try {
            val cacheDir = File(context.cacheDir, "camera_capture")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            
            val file = File.createTempFile(prefix, extension, cacheDir)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            Pair(file, uri)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    companion object {
        /**
         * Detects the content type from a MIME type string.
         *
         * @param mimeType The MIME type to analyze
         * @return The detected content type
         */
        fun detectContentType(mimeType: String?): String {
            return when {
                mimeType == null -> MediaContentType.UNKNOWN
                mimeType.startsWith("image/") -> MediaContentType.IMAGE
                mimeType.startsWith("video/") -> MediaContentType.VIDEO
                mimeType.startsWith("audio/") -> MediaContentType.AUDIO
                else -> MediaContentType.FILE
            }
        }
        
        /**
         * Gets the MIME type from a URI.
         *
         * @param context The context to use for content resolution
         * @param uri The URI to analyze
         * @return The MIME type or null
         */
        fun getMimeType(context: Context, uri: Uri): String? {
            return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                context.contentResolver.getType(uri)
            } else {
                val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension?.lowercase())
            }
        }
    }
}
