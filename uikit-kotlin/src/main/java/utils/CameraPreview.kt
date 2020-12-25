package utils

import android.content.Context
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.cometchat.pro.helpers.Logger

class CameraPreview(private val c: Context, private val mCamera: Camera) : SurfaceView(c), SurfaceHolder.Callback {
    private val mHolder: SurfaceHolder = holder
    override fun surfaceCreated(holder: SurfaceHolder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder)
            mCamera.setDisplayOrientation(90)
            mCamera.parameters
            mCamera.startPreview()
        } catch (e: Exception) {
            Logger.error(TAG, "Error setting camera preview: " + e.message)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

        // empty. Take care of releasing the Camera preview in your activity.
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.surface == null) {
            // preview surface does not exist
            return
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview()
        } catch (e: Exception) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder)
            mCamera.startPreview()
        } catch (e: Exception) {
            Logger.error(TAG, "Error starting camera preview: " + e.message)
        }
    }

    companion object {
        private const val TAG = "CameraPreview"
    }

    init {

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder.addCallback(this)
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }
}