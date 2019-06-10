package com.inscripts.cometchatpulse.Helpers

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.helpers.Logger
import com.inscripts.cometchatpulse.Utils.FileUtil
import com.inscripts.cometchatpulse.Utils.FileUtil.Companion.getPath
import com.inscripts.cometchatpulse.Utils.MediaUtil
import java.io.File
import java.lang.Exception
import java.net.URISyntaxException

class AttachmentHelper {

    companion object {

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

        fun captureImage():Intent {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            return intent
        }

        fun captureVideo():Intent {
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0)
            intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 15000000L)

            return intent

        }

        fun  handleCameraImage(context: Context?, data: Intent):String {
            var filePath: String=""
            try {
            Logger.error("uri", data.data?.toString())
            val bitmap = data.extras!!.get("data") as Bitmap
            val fileUri = FileUtil.getImageUri(context!!, bitmap)

            Logger.error("", "fileUri: $fileUri")
               filePath= FileUtil.ImagePath(fileUri, context)

            }catch (e:Exception){
                e.printStackTrace()
            }

            return filePath
        }

        fun handleCameraVideo(context: Context?, data: Intent): String? {
            val path = context?.let { FileUtil.getVideoPath(data.data, it) }
            Logger.debug("handleCameraVideo", " Video Path $path")
            return path

        }

        fun handleFile(context: Context?, data: Intent): Array<String?> {

            var filePath: Array<String?> = arrayOfNulls(3)

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val uri = data.data
//                val file = File(uri!!.path)//create path from uri
//                val split = file.path.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()//split the path.
//                for (string in split){
//                    Log.d("handleFile",string)
//                }
//                val path=split[0]
//                Log.d("File",split[1])
//                var messageType:String=""
//                try {
//                Log.d("filetype",context?.contentResolver?.getType(uri))
//                val type: String? =context?.contentResolver?.getType(uri)?.toLowerCase()
//
//                 if (type!=null) {
//                     if (type.contains("image")||type.contains("picture")||type.contains("photo")){
//                         messageType=CometChatConstants.MESSAGE_TYPE_IMAGE
//                     }
//                    else if (type.contains("video")||type.contains("mp4")||type.contains("avi")||
//                             type.contains("flv")){
//                         messageType=CometChatConstants.MESSAGE_TYPE_VIDEO
//                     }
//                   else if (type.contains("aac")||type.contains("m4a")||type.contains("amr")
//                         ||type.contains("opus")||type.contains("mp3")){
//
//                         messageType=CometChatConstants.MESSAGE_TYPE_AUDIO
//                     }
//                     else{
//                         messageType=CometChatConstants.MESSAGE_TYPE_FILE
//                     }
//                 }
//
//                }catch (e:Exception){
//                    e.printStackTrace()
//                }
//
//
//               filePath= arrayOf(path,messageType)
//
//            } else {
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