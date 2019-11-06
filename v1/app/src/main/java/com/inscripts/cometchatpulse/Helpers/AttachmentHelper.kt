package com.inscripts.cometchatpulse.Helpers

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.FragmentActivity
import android.util.Log
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.helpers.Logger
import com.inscripts.cometchatpulse.Utils.FileUtil
import com.inscripts.cometchatpulse.Utils.FileUtil.Companion.getPath
import com.inscripts.cometchatpulse.Utils.MediaUtil
import java.io.File
import java.lang.Exception
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.*

class AttachmentHelper {

    companion object {

        private var pictureImagePath: String=""

        fun selectMedia(activity: FragmentActivity?, type: String,
                        extraMimeType: Array<String>?):Intent {

            val intent = Intent()
            intent.type = type

            if (extraMimeType != null && Build.VERSION.SDK_INT >= 19) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, extraMimeType)
            }

           intent.action = Intent.ACTION_OPEN_DOCUMENT

            intent.action = Intent.ACTION_GET_CONTENT

            return intent

        }

        fun captureImage():Intent?{
            var intent:Intent?= null
            try {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val imageFileName = "$timeStamp.jpg"
                val storageDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES)
                pictureImagePath = storageDir.absolutePath + "/" + imageFileName
                val file = File(pictureImagePath)
                val outputFileUri: Uri
                outputFileUri = Uri.fromFile(file)
                intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)

            } catch (e: Exception) {
                e.printStackTrace()
            }
            return intent
        }

        fun captureVideo():Intent {
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0)
            intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 15000000L)

            return intent

        }

        fun handleCameraImage():String {
            return pictureImagePath
        }

        fun handleCameraVideo(context: Context?, data: Intent): String? {
            val path = context?.let { FileUtil.getVideoPath(data.data, it) }
            Logger.debug("handleCameraVideo", " Video Path $path")
            return path

        }

        fun handleFile(context: Context?, data: Intent): Array<String?> {

            var filePath: Array<String?> = arrayOfNulls(3)

//
                try {
                    filePath = getPath(context!!, data.data)
                } catch (e: URISyntaxException) {
                    e.printStackTrace()
                }

//            }

            return filePath
        }

    }

}