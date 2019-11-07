package com.inscripts.cometchatpulse.Utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable

class MediaUtil {


    companion object {

        fun getPlaceholderImage(context: Context, drawable: Drawable): Bitmap {
            var bitmap = getBitmapFromDrawable( drawable)

            val outputImage = Bitmap.createBitmap(bitmap.getWidth() + 80, bitmap.getHeight() + 80, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(outputImage)
            canvas.drawARGB(0, 0, 0, 0)
            canvas.drawBitmap(bitmap, 40f, 40f, null)
            bitmap = outputImage

            return bitmap
        }

        private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
            val bitmap: Bitmap

            bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            return bitmap
        }
    }
}