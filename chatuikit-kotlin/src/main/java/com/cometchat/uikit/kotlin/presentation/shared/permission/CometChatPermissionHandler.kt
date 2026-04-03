package com.cometchat.uikit.kotlin.presentation.shared.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResult
import androidx.core.content.ContextCompat
import com.cometchat.uikit.kotlin.presentation.shared.permission.builder.ActivityResultHandlerBuilder
import com.cometchat.uikit.kotlin.presentation.shared.permission.builder.PermissionHandlerBuilder
import com.cometchat.uikit.kotlin.presentation.shared.permission.listener.ActivityResultListener
import com.cometchat.uikit.kotlin.presentation.shared.permission.listener.BasePermissionResultListener
import com.cometchat.uikit.kotlin.presentation.shared.permission.listener.PermissionResultListener
import java.lang.ref.WeakReference

/**
 * Permission types supported by the CometChat permission handler.
 */
enum class PermissionType {
    CAMERA,
    MICROPHONE,
    STORAGE,
    CAMERA_AND_STORAGE,
    MICROPHONE_AND_STORAGE
}

/**
 * CometChatPermissionHandler provides a unified way to handle runtime permissions
 * and activity results for camera, microphone, storage access, and file pickers.
 *
 * This implementation uses a separate transparent Activity (CometChatPermissionActivity)
 * to handle permission requests and activity results. This approach allows requesting
 * permissions or launching intents from any context (View, Service, etc.) without
 * requiring the caller to register ActivityResultLauncher in their onCreate lifecycle.
 *
 * Usage for permissions:
 * ```kotlin
 * CometChatPermissionHandler.withContext(context)
 *     .withPermissions(arrayOf(Manifest.permission.RECORD_AUDIO))
 *     .withListener(object : PermissionResultListener {
 *         override fun permissionResult(granted: List<String>, denied: List<String>) {
 *             if (denied.isEmpty()) {
 *                 // All permissions granted
 *             } else {
 *                 // Some permissions denied
 *             }
 *         }
 *     })
 *     .check()
 * ```
 *
 * Usage for activity results (file pickers, camera, etc.):
 * ```kotlin
 * CometChatPermissionHandler.withContext(context)
 *     .registerListener { result ->
 *         if (result.resultCode == Activity.RESULT_OK) {
 *             val uri = result.data?.data
 *             // Handle selected file
 *         }
 *     }
 *     .withIntent(Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/star" })
 *     .launch()
 * ```
 */
class CometChatPermissionHandler private constructor(context: Context) : PermissionHandlerBuilder, ActivityResultHandlerBuilder {

    private var permissions: Array<String>? = null
    private var permissionResultListener: PermissionResultListener = BasePermissionResultListener()
    private var requestIntent: Intent? = null
    private var activityResultListener: ActivityResultListener? = null

    init {
        initialize(context)
    }

    // PermissionHandlerBuilder implementation
    override fun withPermissions(permissions: Array<String>): PermissionHandlerBuilder {
        this.permissions = permissions
        return this
    }

    override fun withListener(listener: PermissionResultListener): PermissionHandlerBuilder {
        this.permissionResultListener = listener
        return this
    }

    override fun check() {
        val perms = permissions ?: return
        instance?.checkPermissions(permissionResultListener, perms)
    }

    // ActivityResultHandlerBuilder implementation
    override fun withIntent(intent: Intent): ActivityResultHandlerBuilder {
        this.requestIntent = intent
        return this
    }

    override fun registerListener(listener: ActivityResultListener): ActivityResultHandlerBuilder {
        this.activityResultListener = listener
        return this
    }

    override fun launch() {
        val intent = requestIntent ?: return
        val listener = activityResultListener ?: return
        instance?.launchIntent(listener, intent)
    }

    companion object {
        private var instance: PermissionHandlerInstance? = null

        /**
         * Creates a new permission handler with the given context.
         * @param context The context to use for permission requests
         * @return A new CometChatPermissionHandler instance
         */
        @JvmStatic
        fun withContext(context: Context): CometChatPermissionHandler {
            return CometChatPermissionHandler(context)
        }

        private fun initialize(context: Context) {
            if (instance == null) {
                instance = PermissionHandlerInstance(context)
            } else {
                instance?.setContext(context)
            }
        }

        /**
         * Called when CometChatPermissionActivity is ready.
         */
        internal fun onActivityReady(activity: CometChatPermissionActivity) {
            instance?.onActivityReady(activity)
        }

        /**
         * Called when CometChatPermissionActivity is destroyed.
         */
        internal fun onActivityDestroyed(oldActivity: CometChatPermissionActivity) {
            instance?.onActivityDestroyed(oldActivity)
        }

        /**
         * Called when permission results are received.
         */
        internal fun onPermissionsRequested(grantedPermissions: List<String>, deniedPermissions: List<String>) {
            instance?.updatedPermissionResults(grantedPermissions, deniedPermissions)
        }

        /**
         * Called when activity result is received.
         */
        internal fun onActivityResultCompleted(result: ActivityResult) {
            instance?.updatedActivityResult(result)
        }

        /**
         * Gets the required permissions for a given permission type.
         * Handles different Android versions appropriately.
         *
         * @param type The permission type
         * @return Array of Android permission strings
         */
        @JvmStatic
        fun getPermissionsForType(type: PermissionType): Array<String> {
            return when (type) {
                PermissionType.CAMERA -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        arrayOf(Manifest.permission.CAMERA)
                    } else {
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    }
                }
                PermissionType.MICROPHONE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        arrayOf(Manifest.permission.RECORD_AUDIO)
                    } else {
                        arrayOf(
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    }
                }
                PermissionType.STORAGE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        arrayOf(
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.READ_MEDIA_AUDIO
                        )
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    } else {
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    }
                }
                PermissionType.CAMERA_AND_STORAGE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO
                        )
                    } else {
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    }
                }
                PermissionType.MICROPHONE_AND_STORAGE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        arrayOf(
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_MEDIA_AUDIO
                        )
                    } else {
                        arrayOf(
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    }
                }
            }
        }

        /**
         * Checks if a specific permission is granted.
         *
         * @param context The context to use for checking permission
         * @param permission The permission to check
         * @return true if permission is granted
         */
        @JvmStatic
        fun isPermissionGranted(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        /**
         * Checks if all permissions for a given type are granted.
         *
         * @param context The context to use for checking permissions
         * @param type The permission type to check
         * @return true if all permissions are granted
         */
        @JvmStatic
        fun arePermissionsGranted(context: Context, type: PermissionType): Boolean {
            return getPermissionsForType(type).all { isPermissionGranted(context, it) }
        }
    }
}

/**
 * Internal class that manages the permission request state and activity lifecycle.
 */
internal class PermissionHandlerInstance(context: Context) {

    private var contextRef: WeakReference<Context> = WeakReference(context)
    private var activity: CometChatPermissionActivity? = null
    private var permissionResultListener: PermissionResultListener = BasePermissionResultListener()
    private var activityResultListener: ActivityResultListener? = null

    fun setContext(context: Context) {
        contextRef = WeakReference(context)
    }

    fun checkPermissions(listener: PermissionResultListener, permissions: Array<String>) {
        val context = contextRef.get() ?: return
        
        // Clean up any finished activity
        if (activity?.isFinishing == true) {
            onActivityDestroyed(activity!!)
        }

        this.permissionResultListener = listener

        // Check if all permissions are already granted
        if (isEveryPermissionGranted(permissions, context)) {
            updatedPermissionResults(permissions.toList(), emptyList())
        } else {
            startActivityForPermission(permissions)
        }
    }

    fun launchIntent(listener: ActivityResultListener, intent: Intent) {
        val context = contextRef.get() ?: return
        
        // Clean up any finished activity
        if (activity?.isFinishing == true) {
            onActivityDestroyed(activity!!)
        }

        this.activityResultListener = listener
        startActivityForResult(intent)
    }

    private fun startActivityForPermission(permissions: Array<String>) {
        val context = contextRef.get() ?: return
        
        val intent = Intent(context, CometChatPermissionActivity::class.java).apply {
            putExtra(CometChatPermissionActivity.PERMISSION_STRING, permissions)
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        
        context.startActivity(intent)
        
        if (context is Activity) {
            context.overridePendingTransition(0, 0)
        }
    }

    private fun startActivityForResult(intentToLaunch: Intent) {
        val context = contextRef.get() ?: return
        
        val intent = Intent(context, CometChatPermissionActivity::class.java).apply {
            putExtra(CometChatPermissionActivity.INTENT_STRING, intentToLaunch)
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        
        context.startActivity(intent)
        
        if (context is Activity) {
            context.overridePendingTransition(0, 0)
        }
    }

    fun onActivityReady(activity: CometChatPermissionActivity) {
        this.activity = activity
    }

    fun onActivityDestroyed(oldActivity: CometChatPermissionActivity) {
        if (activity == oldActivity) {
            activity = null
        }
    }

    fun updatedPermissionResults(granted: List<String>, denied: List<String>) {
        activity?.finish()
        
        val currentListener = permissionResultListener
        permissionResultListener = BasePermissionResultListener()
        currentListener.permissionResult(granted, denied)
    }

    fun updatedActivityResult(result: ActivityResult) {
        activity?.finish()
        
        val currentListener = activityResultListener
        activityResultListener = null
        currentListener?.onActivityResult(result)
    }

    private fun isEveryPermissionGranted(permissions: Array<String>, context: Context): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}
