package com.cometchat.uikit.kotlin.shared.resources.utils

import android.content.ComponentName
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Parcelable
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.cometchat.uikit.kotlin.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility object for media-related operations including creating intents for
 * camera, file pickers, and handling file URIs.
 */
object MediaUtils {
    
    private const val TAG = "MediaUtils"
    
    /**
     * Stores the path of the captured image from camera.
     * Used to retrieve the file after camera capture completes.
     */
    @JvmStatic
    var pictureImagePath: String? = null
        private set
    
    /**
     * Stores the URI of the captured image (for Android 10+).
     */
    @JvmStatic
    var uri: Uri? = null
        private set
    
    /**
     * Creates an intent to open the device camera for capturing a photo.
     * 
     * @param context The context to use for creating the intent
     * @return Intent configured to launch the camera
     */
    @JvmStatic
    fun openCamera(context: Context): Intent {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "$timeStamp.jpg"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        pictureImagePath = "${storageDir.absolutePath}/$imageFileName"
        val file = File(pictureImagePath!!)
        
        var outputFileUri: Uri?
        
        // Use the app's package name for FileProvider authority
        val authority = "${context.packageName}.provider"
        outputFileUri = FileProvider.getUriForFile(context, authority, file)
        
        if (Build.VERSION.SDK_INT >= 29) {
            val resolver: ContentResolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM")
            }
            outputFileUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri = outputFileUri
        } else if (Build.VERSION.SDK_INT <= 23) {
            outputFileUri = Uri.fromFile(file)
            uri = outputFileUri
        }
        
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
        }
    }
    
    /**
     * Creates an intent to open the image picker/gallery.
     * 
     * @return Intent configured to pick images
     */
    @JvmStatic
    fun openImagePicker(): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }
    }
    
    /**
     * Creates an intent to open the video picker.
     * 
     * @return Intent configured to pick videos
     */
    @JvmStatic
    fun openVideoPicker(): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }
    }
    
    /**
     * Creates an intent to open the audio picker.
     * 
     * @param context The context to use for creating the intent
     * @return Intent configured to pick audio files
     */
    @JvmStatic
    fun openAudioPicker(context: Context): Intent {
        val allIntents = mutableListOf<Intent>()
        val packageManager = context.packageManager
        
        val audioIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
        }
        
        val listGallery = packageManager.queryIntentActivities(audioIntent, 0)
        for (res in listGallery) {
            val intent = Intent(audioIntent).apply {
                component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                setPackage(res.activityInfo.packageName)
            }
            allIntents.add(intent)
        }
        
        if (allIntents.isEmpty()) {
            return audioIntent
        }
        
        var mainIntent = allIntents.last()
        for (intent in allIntents) {
            intent.component?.let { component ->
                if (component.className == "com.android.documentsui.DocumentsActivity") {
                    mainIntent = intent
                }
            }
        }
        allIntents.remove(mainIntent)
        
        return Intent.createChooser(mainIntent, "Select source").apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toTypedArray<Parcelable>())
        }
    }
    
    /**
     * Creates an intent to open the file/document picker.
     * 
     * @return Intent configured to pick files
     */
    @JvmStatic
    fun openFilePicker(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
    }
    
    /**
     * Handles the camera result and returns the captured image path.
     * 
     * @return The path to the captured image file
     */
    @JvmStatic
    fun handleCameraImage(): String? {
        return pictureImagePath
    }
    
    /**
     * Gets the real file path from a URI.
     * 
     * @param context The context to use
     * @param fileUri The URI to resolve
     * @param isThirdParty Whether the URI is from a third-party app
     * @return The File object for the URI
     */
    @JvmStatic
    fun getRealPath(context: Context, fileUri: Uri, isThirdParty: Boolean = false): File? {
        return when {
            isGoogleDrive(fileUri) || isThirdParty -> downloadFile(context, fileUri)
            Build.VERSION.SDK_INT < 28 -> {
                val path = getRealPathFromURI(context, fileUri)
                path?.let { File(it) }
            }
            else -> {
                val path = getFilePathForN(fileUri, context)
                path?.let { File(it) }
            }
        }
    }
    
    /**
     * Downloads a file from a URI to the cache directory.
     */
    private fun downloadFile(context: Context, imageUri: Uri?): File? {
        if (imageUri == null) return null
        
        return try {
            val fileName = getFileName(context, imageUri)
            val file = File(context.cacheDir, fileName)
            
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }
            file
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error downloading file: ${e.message}")
            null
        }
    }
    
    /**
     * Gets the file name from a URI.
     */
    @JvmStatic
    fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "unknown_file"
    }
    
    private fun getFilePathForN(uri: Uri, context: Context): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return null
                
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex == -1) return null
                
                val name = cursor.getString(nameIndex)
                val file = File(context.filesDir, name)
                
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        val buffer = ByteArray(1024 * 1024)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                    }
                }
                file.path
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, e.toString())
            null
        }
    }
    
    private fun getRealPathFromURI(context: Context, uri: Uri): String? {
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return "${Environment.getExternalStorageDirectory()}/${split[1]}"
                    }
                }
                isDownloadsDocument(uri) -> {
                    var id = DocumentsContract.getDocumentId(uri)
                    if (id.startsWith("raw:")) {
                        return id.substring(4)
                    }
                    if (id.startsWith("msf:")) {
                        id = id.substring(4)
                    }
                    // Try content URI
                    val contentUriPrefixes = arrayOf(
                        "content://downloads/public_downloads",
                        "content://downloads/my_downloads"
                    )
                    for (prefix in contentUriPrefixes) {
                        try {
                            val contentUri = android.content.ContentUris.withAppendedId(
                                Uri.parse(prefix),
                                id.toLong()
                            )
                            val path = getDataColumn(context, contentUri, null, null)
                            if (path != null) return path
                        } catch (e: Exception) {
                            // Continue to next prefix
                        }
                    }
                }
                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    val type = split[0]
                    val contentUri = when (type) {
                        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        else -> null
                    }
                    contentUri?.let {
                        val selection = MediaStore.Images.Media._ID + "=?"
                        val selectionArgs = arrayOf(split[1])
                        return getDataColumn(context, it, selection, selectionArgs)
                    }
                }
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            if (isGooglePhotosUri(uri)) return uri.lastPathSegment
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }
    
    private fun getDataColumn(
        context: Context,
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        val column = "_data"
        val projection = arrayOf(column)
        
        return try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(column)
                    cursor.getString(index)
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }
    
    private fun isGoogleDrive(uri: Uri): Boolean {
        return uri.authority?.contains("com.google.android.apps.docs.storage") == true
    }
    
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }
    
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
    
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }
    
    /**
     * Opens a file using the appropriate application.
     *
     * @param context The context to use
     * @param file The file to open
     */
    @JvmStatic
    fun openFile(context: Context, file: File) {
        try {
            val authority = "${context.packageName}.provider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            val mimeType = context.contentResolver.getType(uri) 
                ?: getMimeType(file.name)
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error opening file: ${e.message}")
        }
    }
    
    /**
     * Opens media in an appropriate player application.
     *
     * @param context The context to use
     * @param url The URL of the media
     * @param mimeType The MIME type of the media
     */
    @JvmStatic
    fun openMediaInPlayer(context: Context, url: String?, mimeType: String?) {
        if (url.isNullOrEmpty()) return
        
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(url), mimeType ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error opening media: ${e.message}")
        }
    }
    
    /**
     * Downloads a file from a URL.
     *
     * @param context The context to use
     * @param url The URL of the file
     * @param fileName The name to save the file as
     * @param extension The file extension
     */
    @JvmStatic
    fun downloadFile(context: Context, url: String, fileName: String, extension: String) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
            val request = android.app.DownloadManager.Request(Uri.parse(url))
            request.setTitle(fileName)
            request.setDescription("Downloading...")
            request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "$fileName$extension"
            )
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error downloading file: ${e.message}")
        }
    }
    
    /**
     * Gets the MIME type from a file name.
     */
    private fun getMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: "application/octet-stream"
    }
    
    /**
     * Gets the content type from a URI.
     * 
     * @param context The context to use
     * @param uri The URI to check
     * @return The content type string (image, video, audio, or file)
     */
    @JvmStatic
    fun getContentType(context: Context, uri: Uri?): String {
        if (uri == null) return "file"
        
        val mimeType = context.contentResolver.getType(uri)
        return when {
            mimeType?.startsWith("image/") == true -> "image"
            mimeType?.startsWith("video/") == true -> "video"
            mimeType?.startsWith("audio/") == true -> "audio"
            else -> "file"
        }
    }
    
    /**
     * Downloads a file from a URL in a background thread and shares it after download completes.
     * 
     * This method downloads the file to the Downloads directory and then launches a share intent
     * with the downloaded file using FileProvider.
     *
     * @param context The context to use
     * @param fileUrl The URL of the file to download
     * @param fileName The name to save the file as
     * @param mimeType The MIME type of the file
     * @param onComplete Optional callback invoked on the main thread when download and share completes
     * @param onError Optional callback invoked on the main thread if download fails
     */
    @JvmStatic
    fun downloadFileAndShare(
        context: Context,
        fileUrl: String,
        fileName: String,
        mimeType: String,
        onComplete: (() -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        
        Thread {
            try {
                if (!file.exists()) {
                    val url = java.net.URL(fileUrl)
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.connect()
                    
                    if (connection.responseCode != java.net.HttpURLConnection.HTTP_OK) {
                        throw Exception("HTTP error: ${connection.responseCode}")
                    }
                    
                    connection.inputStream.use { input ->
                        java.io.FileOutputStream(file).use { output ->
                            val buffer = ByteArray(4096)
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                            }
                            output.flush()
                        }
                    }
                }
                
                // Share the file on the main thread
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    try {
                        shareFile(context, file, mimeType)
                        onComplete?.invoke()
                    } catch (e: Exception) {
                        android.util.Log.e(TAG, "Error sharing file: ${e.message}")
                        onError?.invoke(e)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error downloading file: ${e.message}")
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onError?.invoke(e)
                }
            }
        }.start()
    }
    
    /**
     * Shares a file using the system share intent.
     *
     * @param context The context to use
     * @param file The file to share
     * @param mimeType The MIME type of the file
     */
    @JvmStatic
    fun shareFile(context: Context, file: File, mimeType: String) {
        try {
            val authority = "${context.packageName}.provider"
            val fileUri = FileProvider.getUriForFile(context, authority, file)
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, fileUri)
                type = mimeType
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(shareIntent, context.getString(R.string.cometchat_share))
            )
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error sharing file: ${e.message}")
            throw e
        }
    }
}
