package com.inscripts.cometchatpulse.Utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.cometchat.pro.constants.CometChatConstants
import com.inscripts.cometchatpulse.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.*

class FileUtil {

    companion object {

        @Throws(URISyntaxException::class)
        fun getPath(context: Context, uri: Uri?): Array<String?> {
            var uri = uri
            val needToCheckUri = Build.VERSION.SDK_INT >= 19
            var selection: String? = null
            var selectionArgs: Array<String>? = null
            var ar: Array<String?> = arrayOf()
            var type: String? = null
            // Uri is different in versions after KITKAT (Android 4.4), we need to
            // deal with different Uris.
            if (needToCheckUri && DocumentsContract.isDocumentUri(context.applicationContext, uri)) {
                if (isExternalStorageDocument(uri!!)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    ar = arrayOf(Environment.getExternalStorageDirectory().toString() + "/" + split[1], CometChatConstants.MESSAGE_TYPE_FILE)
                    return ar
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    uri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    type = split[0]
                    if ("image".equals(type)) {
                        type = CometChatConstants.MESSAGE_TYPE_IMAGE
                        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video".equals(type)) {
                        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        type = CometChatConstants.MESSAGE_TYPE_VIDEO
                    } else if ("audio".equals(type)) {
                        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        type = CometChatConstants.MESSAGE_TYPE_AUDIO
                    }
                    selection = "_id=?"
                    selectionArgs = arrayOf(split[1])
                }
            }
            if ("content".equals(uri!!.scheme!!, ignoreCase = true)) {
                val projection = arrayOf(MediaStore.Images.Media.DATA)
                var cursor: Cursor? = null
                try {
                    cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                    val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    type = context.contentResolver?.getType(uri)?.toLowerCase()
                    if (type != null) {
                        if (type.contains("jpeg") || type.contains("jpg") || type.contains("png") || type.contains("image")
                                || type.contains("picture") || type.contains("photo")) {
                            type = CometChatConstants.MESSAGE_TYPE_IMAGE
                        } else if (type.contains("video") || type.contains("mp4")
                                || type.contains("avi") ||
                                type.contains("flv")) {
                            type = CometChatConstants.MESSAGE_TYPE_VIDEO
                        } else if (type.contains("aac") || type.contains("m4a") || type.contains("amr")
                                || type.contains("opus") || type.contains("mp3")) {

                            type = CometChatConstants.MESSAGE_TYPE_AUDIO
                        } else {
                            type = CometChatConstants.MESSAGE_TYPE_FILE
                        }
                    }
                    if (cursor.moveToFirst()) {
                        ar = arrayOf(cursor.getString(column_index), type)
                        return ar
                    }
                } catch (e: Exception) {
                }

            } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
                ar = arrayOf(uri.path, CometChatConstants.MESSAGE_TYPE_FILE)
                return ar
            }
            return ar;
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

        fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
            val bytes = ByteArrayOutputStream()
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
            return Uri.parse(path)
        }

        fun ImagePath(uri: Uri, context: Context): String {
            var path = ""
            if (context.contentResolver != null) {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                if (cursor != null) {
                    cursor.moveToFirst()
                    val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    path = cursor.getString(idx)
                    cursor.close()
                }
            }
            return path
        }

        fun getVideoPath(uri: Uri, context: Context): String {
            var path = ""
            if (context.contentResolver != null) {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                if (cursor != null) {
                    cursor.moveToFirst()
                    val idx = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA)
                    path = cursor.getString(idx)
                    cursor.close()
                }
            }
            return path
        }

        fun checkDirExistence(context: Context?,type: String): Boolean {

            val audioDir = File( Environment.getExternalStorageDirectory().toString() + "/" +
                    context?.resources?.getString(R.string.app_name) + "/" + type + "/")

            if (audioDir.isDirectory) {
                return true
            }

            return false
        }

        fun checkFileExistence(path: String): Boolean {

            val filepath = File(path)

            if (filepath.exists()) {

                return true
            }

            return false
        }

        fun getPath(context: Context, folder: String): String {

            val path = Environment.getExternalStorageDirectory().toString() + "/" +
                    context.resources?.getString(R.string.app_name) + "/" + folder + "/"

            return path
        }

        fun getFileName(mediaFile: String?): String? {

            val t = mediaFile?.substring(mediaFile.lastIndexOf("/"))?.split("_".toRegex())?.dropLastWhile({ it.isEmpty() })?.toTypedArray()

            return t?.get(2)
        }

        fun getFileExtension(mediaFile: String?): String? {

            return mediaFile?.substring(mediaFile.lastIndexOf(".") + 1)
        }


        fun getOutputMediaFile(context: Context?): String? {
            val var0 = File(Environment.getExternalStorageDirectory(), context?.resources?.getString(R.string.app_name))
            if (!var0.exists() && !var0.mkdirs()) {
                return null
            } else {
                val var1 = (Environment.getExternalStorageDirectory().toString() + "/" +
                        context?.resources?.getString(R.string.app_name) + "/" +"audio/")
                createDirectory(var1)
                return var1 + SimpleDateFormat("yyyyMMddHHmmss").format(Date()) + ".mp3"
            }
        }

        fun createDirectory(var0: String) {
            if (!File(var0).exists()) {
                File(var0).mkdirs()
            }

        }

        fun makeDirectory(context: Context?,type:String) {

            val audioDir = Environment.getExternalStorageDirectory().toString() + "/" +
                    context?.resources?.getString(R.string.app_name) + "/" + type + "/"

            createDirectory(audioDir)
        }

    }
}