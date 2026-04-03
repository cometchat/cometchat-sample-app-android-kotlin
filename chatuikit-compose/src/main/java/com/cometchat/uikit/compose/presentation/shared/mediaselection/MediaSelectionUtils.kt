package com.cometchat.uikit.compose.presentation.shared.mediaselection

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.FileProvider
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
 * Detects the content type from a MIME type string.
 *
 * @param mimeType The MIME type to analyze
 * @return The detected content type (image, video, audio, file, or unknown)
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
 * @return The MIME type or null if not determinable
 */
fun getMimeType(context: Context, uri: Uri): String? {
    return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        context.contentResolver.getType(uri)
    } else {
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension?.lowercase())
    }
}

/**
 * Gets the file name from a URI.
 *
 * @param context The context to use for content resolution
 * @param uri The URI to analyze
 * @return The file name or a default name if not determinable
 */
fun getFileName(context: Context, uri: Uri): String {
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
 *
 * @param context The context to use for content resolution
 * @param uri The URI to analyze
 * @return The file size in bytes or 0 if not determinable
 */
fun getFileSize(context: Context, uri: Uri): Long {
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
 *
 * @param context The context to use for file operations
 * @param uri The URI to copy from
 * @param fileName The name for the destination file
 * @return The created file or null if copy failed
 */
fun copyUriToFile(context: Context, uri: Uri, fileName: String): File? {
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
 * Creates a MediaSelectionResult from a URI.
 *
 * @param context The context to use for content resolution
 * @param uri The URI of the selected media
 * @param copyToCache Whether to copy the content to app cache
 * @return The MediaSelectionResult
 */
fun createMediaSelectionResult(
    context: Context,
    uri: Uri,
    copyToCache: Boolean = true
): MediaSelectionResult {
    val mimeType = getMimeType(context, uri)
    val contentType = detectContentType(mimeType)
    val fileName = getFileName(context, uri)
    val fileSize = getFileSize(context, uri)
    
    val file = if (copyToCache) {
        copyUriToFile(context, uri, fileName)
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
 * Creates a temporary file for camera capture.
 *
 * @param context The context to use for file operations
 * @param prefix The prefix for the file name
 * @param extension The file extension (e.g., ".jpg")
 * @return Pair of the file and its content URI
 */
fun createTempFileForCapture(
    context: Context,
    prefix: String = "capture",
    extension: String = ".jpg"
): Pair<File, Uri>? {
    return try {
        val cacheDir = File(context.cacheDir, "camera_capture")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        
        val file = File.createTempFile(prefix, extension, cacheDir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        
        Pair(file, uri)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * State holder for media selection launchers.
 */
class MediaSelectionState(
    val launchImagePicker: () -> Unit,
    val launchVideoPicker: () -> Unit,
    val launchImageAndVideoPicker: () -> Unit,
    val launchAudioPicker: () -> Unit,
    val launchFilePicker: () -> Unit,
    val launchCamera: () -> Unit,
    val launchVideoCapture: () -> Unit
)

/**
 * Remembers and creates media selection launchers for various media types.
 *
 * @param onImageSelected Callback when an image is selected
 * @param onVideoSelected Callback when a video is selected
 * @param onAudioSelected Callback when an audio file is selected
 * @param onFileSelected Callback when a file is selected
 * @param onCameraCapture Callback when a photo is captured
 * @param onVideoCapture Callback when a video is captured
 * @param onError Callback when an error occurs
 * @return MediaSelectionState with launcher functions
 */
@Composable
fun rememberMediaSelectionState(
    onImageSelected: ((MediaSelectionResult) -> Unit)? = null,
    onVideoSelected: ((MediaSelectionResult) -> Unit)? = null,
    onAudioSelected: ((MediaSelectionResult) -> Unit)? = null,
    onFileSelected: ((MediaSelectionResult) -> Unit)? = null,
    onCameraCapture: ((MediaSelectionResult) -> Unit)? = null,
    onVideoCapture: ((MediaSelectionResult) -> Unit)? = null,
    onError: ((Exception) -> Unit)? = null
): MediaSelectionState {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Camera capture state
    val cameraFileState = remember { mutableMapOf<String, Pair<File, Uri>?>() }
    
    // Pending action after permission is granted
    val pendingCameraAction = remember { mutableMapOf<String, () -> Unit>() }
    
    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingCameraAction["camera"]?.invoke()
        } else {
            onError?.invoke(SecurityException("Camera permission denied"))
        }
        pendingCameraAction.remove("camera")
    }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            try {
                val result = createMediaSelectionResult(context, it)
                onImageSelected?.invoke(result)
            } catch (e: Exception) {
                onError?.invoke(e)
            }
        }
    }
    
    // Video picker launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            try {
                val result = createMediaSelectionResult(context, it)
                onVideoSelected?.invoke(result)
            } catch (e: Exception) {
                onError?.invoke(e)
            }
        }
    }
    
    // Image and video picker launcher
    val imageAndVideoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            try {
                val result = createMediaSelectionResult(context, it)
                when (result.contentType) {
                    MediaContentType.IMAGE -> onImageSelected?.invoke(result)
                    MediaContentType.VIDEO -> onVideoSelected?.invoke(result)
                    else -> onFileSelected?.invoke(result)
                }
            } catch (e: Exception) {
                onError?.invoke(e)
            }
        }
    }
    
    // Audio picker launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val result = createMediaSelectionResult(context, it)
                onAudioSelected?.invoke(result)
            } catch (e: Exception) {
                onError?.invoke(e)
            }
        }
    }
    
    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val result = createMediaSelectionResult(context, it)
                onFileSelected?.invoke(result)
            } catch (e: Exception) {
                onError?.invoke(e)
            }
        }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraFileState["photo"]?.let { (file, uri) ->
                try {
                    val result = MediaSelectionResult(
                        uri = uri,
                        file = file,
                        contentType = MediaContentType.IMAGE,
                        fileName = file.name,
                        mimeType = "image/jpeg",
                        fileSize = file.length()
                    )
                    onCameraCapture?.invoke(result)
                } catch (e: Exception) {
                    onError?.invoke(e)
                }
            }
        }
        cameraFileState.remove("photo")
    }
    
    // Video capture launcher
    val videoCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) {
            cameraFileState["video"]?.let { (file, uri) ->
                try {
                    val result = MediaSelectionResult(
                        uri = uri,
                        file = file,
                        contentType = MediaContentType.VIDEO,
                        fileName = file.name,
                        mimeType = "video/mp4",
                        fileSize = file.length()
                    )
                    onVideoCapture?.invoke(result)
                } catch (e: Exception) {
                    onError?.invoke(e)
                }
            }
        }
        cameraFileState.remove("video")
    }
    
    return remember(
        imagePickerLauncher,
        videoPickerLauncher,
        imageAndVideoPickerLauncher,
        audioPickerLauncher,
        filePickerLauncher,
        cameraLauncher,
        videoCaptureLauncher,
        cameraPermissionLauncher
    ) {
        MediaSelectionState(
            launchImagePicker = {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            launchVideoPicker = {
                videoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            },
            launchImageAndVideoPicker = {
                imageAndVideoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
            },
            launchAudioPicker = {
                audioPickerLauncher.launch("audio/*")
            },
            launchFilePicker = {
                filePickerLauncher.launch("*/*")
            },
            launchCamera = {
                // Check if camera permission is already granted
                val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.CAMERA
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                
                val launchCameraAction: () -> Unit = {
                    createTempFileForCapture(context, "photo", ".jpg")?.let { (file, uri) ->
                        cameraFileState["photo"] = Pair(file, uri)
                        cameraLauncher.launch(uri)
                    }
                    Unit
                }
                
                if (hasPermission) {
                    launchCameraAction()
                } else {
                    // Request permission first
                    pendingCameraAction["camera"] = launchCameraAction
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            },
            launchVideoCapture = {
                // Check if camera permission is already granted
                val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.CAMERA
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                
                val launchVideoCaptureAction: () -> Unit = {
                    createTempFileForCapture(context, "video", ".mp4")?.let { (file, uri) ->
                        cameraFileState["video"] = Pair(file, uri)
                        videoCaptureLauncher.launch(uri)
                    }
                    Unit
                }
                
                if (hasPermission) {
                    launchVideoCaptureAction()
                } else {
                    // Request permission first
                    pendingCameraAction["camera"] = launchVideoCaptureAction
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            }
        )
    }
}
