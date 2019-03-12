package com.inscripts.cometchatpulse.AsyncTask

import android.os.AsyncTask
import android.view.View
import com.inscripts.cometchatpulse.CometChatPro
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.Utils.FileUtil
import com.inscripts.cometchatpulse.databinding.LeftAudioBinding
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class DownloadFile() : AsyncTask<String, Int, String>() {

    private lateinit var mediaUrl: String

    private lateinit var leftAudioBinding: LeftAudioBinding

    private lateinit var type: String

    constructor(type: String, mediaUrl: String, leftAudioBinding: LeftAudioBinding) : this() {
        this.mediaUrl = mediaUrl
        this.leftAudioBinding = leftAudioBinding
        this.type = type
    }

    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: String?): String? {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        var connection: HttpURLConnection? = null
        var file: File? = null
        try {
            var url: URL = URL(mediaUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {

                return ("Server " + connection.responseCode
                        + " " + connection.responseMessage)
            }
            val fileLength = connection.contentLength

            inputStream = connection.inputStream


            file = File(FileUtil.getPath(CometChatPro.applicationContext(), type) +
                    FileUtil.getFileName(mediaUrl))

            outputStream = FileOutputStream(file)

            val data = ByteArray(4096)

            var total: Long = 0

            var count = 0

            do {

                count = inputStream.read(data)

                if (count != 1) {

                    if (isCancelled) {
                        inputStream.close()
                        return null

                    }
                    total += count

                    if (fileLength > 0) {
                        publishProgress((total * 100 / fileLength).toInt())
                        outputStream.write(data, 0, count)
                    }
                } else {
                    break
                }

            } while (count != -1)

        } catch (e: Exception) {
            e.printStackTrace()

        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close()
                }
                if (inputStream != null) {
                    inputStream.close()
                }


            } catch (io: IOException) {

                io.printStackTrace()
            }

            if (connection != null) {
                connection.disconnect()
            }
        }
        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        try {
            leftAudioBinding.progress.visibility = View.VISIBLE
            leftAudioBinding.progress.max=100
            leftAudioBinding.download.setImageResource(R.drawable.ic_close_24dp)
            leftAudioBinding.progress.progress = values[0]!!
            leftAudioBinding.playButton.visibility = View.GONE
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)

        try {
            leftAudioBinding.progress.visibility = View.GONE
            leftAudioBinding.download.visibility = View.GONE
            leftAudioBinding.playButton.visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}