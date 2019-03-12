package com.inscripts.cometchatpulse.Helpers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import com.cometchat.pro.helpers.Logger


class CCPermissionHelper {


    companion object {



    private val TAG = CCPermissionHelper::class.java.simpleName


    // requested permissions
    val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    val REQUEST_PERMISSION_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO
    val REQUEST_PERMISSION_CAMERA = Manifest.permission.CAMERA
    val REQUEST_PERMISSION_HARDWARE_LOCATION=Manifest.permission.LOCATION_HARDWARE
    val REQUEST_PERMISSION_COARSE_LOCATION=Manifest.permission.ACCESS_COARSE_LOCATION
    val REQUEST_PERMISSION_FINE_LOCATION=Manifest.permission.ACCESS_FINE_LOCATION
    val REQUEST_PERMISSION_READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE

    /**
     * method to check whether the permissions has been granted to the app
     * @param context the context_menu of the requesting component
     * @param permissions list of permissions requested
     * @return boolean whether app has permission or not
     */
    fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null
                && permissions != null) {
            for (permission in permissions) {
                Logger.error(TAG, " hasPermissions() : Permission : " + permission
                        + "checkSelfPermission : " + ActivityCompat.checkSelfPermission(context, permission))
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * method to request permissions
     * @param activity the activity requesting permissions
     * @param permission list of the requested permissions
     * @param requestCode int request code
     */
    fun requestPermissions(activity: Activity, permission: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, permission, requestCode)
    }

    }

}