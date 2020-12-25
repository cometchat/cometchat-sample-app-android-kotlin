package utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Vibrator
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.loader.content.CursorLoader
import com.cometchat.pro.uikit.BuildConfig
import utils.Utils.Companion.generateFileName
import utils.Utils.Companion.getDocumentCacheDir
import utils.Utils.Companion.getFileName
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

public class MediaUtils {
    companion object {
        private var activity: Activity? = null

        var pictureImagePath: String? = null

        var uri: Uri? = null

        fun getPickImageChooserIntent(a: Activity): Intent? {
            activity = a
            // Determine Uri of camera image to save.
            val outputFileUri: Uri = getCaptureImageOutputUri()!!
            val allIntents: MutableList<Intent?> = ArrayList()
            val packageManager: PackageManager = activity!!.getPackageManager()

            // collect all camera intents
            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val listCam = packageManager.queryIntentActivities(captureIntent, 0)
            for (res in listCam) {
                val intent = Intent(captureIntent)
                intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                intent.setPackage(res.activityInfo.packageName)
                if (outputFileUri != null) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
                }
                allIntents.add(intent)
            }

            // collect all gallery intents
            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.type = "image/*"
            val listGallery = packageManager.queryIntentActivities(galleryIntent, 0)
            for (res in listGallery) {
                val intent = Intent(galleryIntent)
                intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                intent.setPackage(res.activityInfo.packageName)
                allIntents.add(intent)
            }

            // the main intent is the last in the list (fucking android) so pickup the useless one
            var mainIntent = allIntents[allIntents.size - 1]
            for (intent in allIntents) {
                if (intent!!.component!!.className == "com.android.documentsui.DocumentsActivity") {
                    mainIntent = intent
                    break
                }
            }
            allIntents.remove(mainIntent)

            // Create a chooser from the main intent
            val chooserIntent = Intent.createChooser(mainIntent, "Select source")

            // Add all other intents
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toTypedArray())
            return chooserIntent
        }

        fun getFileIntent(type: Array<String>): Intent? {
            val intent = Intent()
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, type)
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            return intent
        }

        /**
         * This method is used to open file from url.
         * @param url is Url of file.
         */
        fun openFile(url: String?, context: Context) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        fun openCamera(context: Context): Intent? {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val imageFileName = "$timeStamp.jpg"
            val storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES)
            pictureImagePath = storageDir.absolutePath + "/" + imageFileName
            val file: File = File(pictureImagePath)
            var outputFileUri: Uri?
            var app: ApplicationInfo? = null
            var provider: String? = null
            try {
                app = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                val bundle = app.metaData
                provider = bundle.getString(BuildConfig.LIBRARY_PACKAGE_NAME)
                Log.d("openCamera", "openCamera:  $provider")
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            outputFileUri = FileProvider.getUriForFile(context, "$provider.provider", file)
            if (Build.VERSION.SDK_INT >= 29) {
                val resolver = context.contentResolver
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp)
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM")
                outputFileUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri = outputFileUri
            } else if (Build.VERSION.SDK_INT <= 23) {
                outputFileUri = Uri.fromFile(file)
                uri = outputFileUri
            }
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
            return intent
        }

        fun openGallery(a: Activity): Intent? {
            activity = a
            val allIntents: MutableList<Intent?> = ArrayList()
            val packageManager: PackageManager = activity!!.getPackageManager()
            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.type = "image/* video/*"
            val listGallery = packageManager.queryIntentActivities(galleryIntent, 0)
            for (res in listGallery) {
                val intent = Intent(galleryIntent)
                intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                intent.setPackage(res.activityInfo.packageName)
                allIntents.add(intent)
            }
            var mainIntent = allIntents[allIntents.size - 1]
            for (intent in allIntents) {
                if (intent!!.component!!.className == "com.android.documentsui.DocumentsActivity") {
                    mainIntent = intent
                    break
                }
            }
            allIntents.remove(mainIntent)

            // Create a chooser from the main intent
            val chooserIntent = Intent.createChooser(mainIntent, "Select source")

            // Add all other intents
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toTypedArray())
            return chooserIntent
        }

        fun openAudio(a: Activity): Intent? {
            activity = a
            val allIntents: MutableList<Intent?> = ArrayList()
            val packageManager: PackageManager = activity!!.getPackageManager()
            val audioIntent = Intent(Intent.ACTION_GET_CONTENT)
            audioIntent.type = "audio/*"
            val listGallery = packageManager.queryIntentActivities(audioIntent, 0)
            for (res in listGallery) {
                val intent = Intent(audioIntent)
                intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                intent.setPackage(res.activityInfo.packageName)
                allIntents.add(intent)
            }
            var mainIntent = allIntents[allIntents.size - 1]
            for (intent in allIntents) {
                if (intent!!.component!!.className == "com.android.documentsui.DocumentsActivity") {
                    mainIntent = intent
                    break
                }
            }
            allIntents.remove(mainIntent)

            // Create a chooser from the main intent
            val chooserIntent = Intent.createChooser(mainIntent, "Select source")

            // Add all other intents
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toTypedArray())
            return chooserIntent
        }

        private fun getCaptureImageOutputUri(): Uri? {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val imageFileName = "$timeStamp.jpg"
            val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val file = File(storageDir.absolutePath + "/" + imageFileName)
            return Uri.fromFile(file)
        }

        fun handleCameraImage(): String? {
            return pictureImagePath
        }

        fun processImageIntentData(resultCode: Int, data: Intent): File? {
            val bitmap: Bitmap?
            val picUri: Uri
            if (resultCode == Activity.RESULT_OK) {
                if (getPickImageResultUri(data) != null) {
                    picUri = getPickImageResultUri(data)!!
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, picUri)
                        return createFileFromBitmap(bitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    bitmap = data.extras!!["data"] as Bitmap?
                    return createFileFromBitmap(bitmap!!)
                }
            }
            return null
        }

        private fun getPickImageResultUri(data: Intent?): Uri? {
            var isCamera = true
            if (data != null) {
                val action = data.action
                isCamera = action != null && action == MediaStore.ACTION_IMAGE_CAPTURE
            }
            return if (isCamera) getCaptureImageOutputUri() else data!!.data
        }

        private fun createFileFromBitmap(bitmap: Bitmap): File? {
            val f: File = File(activity?.cacheDir, System.currentTimeMillis().toString())
            try {
                f.createNewFile()
                val bos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
                val bitmapdata = bos.toByteArray()
                val fos = FileOutputStream(f)
                fos.write(bitmapdata)
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return f
        }

        fun getRealPath(context: Context?, fileUri: Uri?): File {
            Log.d("", "getRealPath: " + fileUri!!.path)
            val realPath: String
            if (isGoogleDrive(fileUri)) {
                return saveDriveFile(context!!, fileUri!!)
            } else if (Build.VERSION.SDK_INT < 11) {
                realPath = getRealPathFromURI_BelowAPI11(context!!, fileUri)!!
            } else if (Build.VERSION.SDK_INT < 19) {
                realPath = getRealPathFromURI_API11to18(context!!, fileUri)!!
            } else {
                realPath = getRealPathFromURI_API19(context!!, fileUri)!!
            }
            return File(realPath)
        }

        fun saveDriveFile(context: Context?, uri: Uri?): File {
            return try {
                val inputStream = context!!.contentResolver.openInputStream(uri!!)
                val originalSize = inputStream!!.available()
                var bis: BufferedInputStream? = null
                var bos: BufferedOutputStream? = null
                val fileName = Utils.getFileName(context!!, uri)
                val file: File = makeEmptyFileWithTitle(fileName)!!
                bis = BufferedInputStream(inputStream)
                bos = BufferedOutputStream(FileOutputStream(
                        file, false))
                val buf = ByteArray(originalSize)
                bis.read(buf)
                do {
                    bos.write(buf)
                } while (bis.read(buf) != -1)
                bos.flush()
                bos.close()
                bis.close()
                file
            } catch (e: IOException) {
                null!!
            }
        }

        fun makeEmptyFileWithTitle(title: String?): File? {
            val root = Environment.getExternalStorageDirectory().absolutePath
            return File(root, title)
        }

        @SuppressLint("NewApi")
        private fun getRealPathFromURI_API11to18(context: Context, contentUri: Uri): String? {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            var result: String? = null
            val cursorLoader = CursorLoader(context, contentUri, proj, null, null, null)
            val cursor = cursorLoader.loadInBackground()
            if (cursor != null) {
                val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                result = cursor.getString(column_index)
                cursor.close()
            }
            return result
        }

        private fun getRealPathFromURI_BelowAPI11(context: Context, contentUri: Uri): String? {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            var column_index = 0
            var result: String? = ""
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                result = cursor.getString(column_index)
                cursor.close()
                return result
            }
            return result
        }

        /**
         * Get a file path from a Uri. This will get the the path for Storage Access
         * Framework Documents, as well as the _data field for the MediaStore and
         * other file-based ContentProviders.
         *
         * @param context The context.
         * @param uri     The Uri to query.
         * @author paulburke
         */
        private fun getRealPathFromURI_API19(context: Context, uri: Uri): String? {
            val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }
                } else if (isDownloadsDocument(uri)) {
                    var id = DocumentsContract.getDocumentId(uri)
                    if (id != null) {
                        if (id.startsWith("raw:")) {
                            return id.substring(4)
                        }
                        if (id.startsWith("msf:")) {
                            id = id.substring(4)
                        }
                    }
                    val contentUriPrefixesToTry = arrayOf(
                            "content://downloads/public_downloads",
                            "content://downloads/my_downloads"
                    )
                    for (contentUriPrefix in contentUriPrefixesToTry) {
                        val contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), java.lang.Long.valueOf(id!!))
                        try {
                            val path: String = getDataColumn(context, contentUri, null, null)!!
                            if (path != null) {
                                return path
                            }
                        } catch (e: Exception) {
                        }
                    }

                    // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
                    val fileName = getFileName(context, uri)
                    val cacheDir = getDocumentCacheDir(context)
                    val file = generateFileName(fileName, cacheDir)
                    var destinationPath: String? = null
                    if (file != null) {
                        destinationPath = file.absolutePath
                        saveFileFromUri(context, uri, destinationPath)
                    }
                    return destinationPath
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                            split[1]
                    )
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {

                // Return the remote address
                return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context, uri, null, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

        private fun saveFileFromUri(context: Context, uri: Uri, destinationPath: String) {
            var ips: InputStream? = null
            var bos: BufferedOutputStream? = null
            try {
                ips = context.contentResolver.openInputStream(uri)
                bos = BufferedOutputStream(FileOutputStream(destinationPath, false))
                val buf = ByteArray(1024)
                ips!!.read(buf)
                do {
                    bos.write(buf)
                } while (ips.read(buf) != -1)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    ips?.close()
                    bos?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * Get the value of the data column for this Uri. This is useful for
         * MediaStore Uris, and other file-based ContentProviders.
         *
         * @param context       The context.
         * @param uri           The Uri to query.
         * @param selection     (Optional) Filter used in the query.
         * @param selectionArgs (Optional) Selection arguments used in the query.
         * @return The value of the _data column, which is typically a file path.
         */
        fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(
                    column
            )
            try {
                cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs,
                        null)
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is Google Drive
         */
        fun isGoogleDrive(uri: Uri): Boolean {
            return uri.authority!!.contains("com.google.android.apps.docs.storage")
        }


        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is Google Photos.
         */
        fun isGooglePhotosUri(uri: Uri): Boolean {
            return "com.google.android.apps.photos.content" == uri.authority
        }

        fun openFrontCam(): Camera? {
            var camCount = 0
            var camera: Camera? = null
            val cameraInfo = CameraInfo()
            camCount = Camera.getNumberOfCameras()
            for (i in 0 until camCount) {
                Camera.getCameraInfo(i, cameraInfo)
                if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                    try {
                        camera = Camera.open(i)
                        camera.setDisplayOrientation(90)
                    } catch (re: RuntimeException) {
                    }
                }
            }
            return camera
        }

        fun playSendSound(context: Context?, ringId: Int) {
            val mMediaPlayer = MediaPlayer.create(context, ringId)
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mMediaPlayer.start()
            mMediaPlayer.setOnCompletionListener { mediaPlayer ->
                var mediaPlayer = mediaPlayer
                if (mediaPlayer != null) {
                    mediaPlayer.stop()
                    mediaPlayer.release()
                    mediaPlayer = null
                }
            }
        }

        fun vibrate(context: Context) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(100)
        }
    }
}