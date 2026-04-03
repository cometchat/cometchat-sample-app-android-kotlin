package com.cometchat.uikit.kotlin.presentation.shared.permission

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

/**
 * Transparent activity that handles permission requests and activity results.
 * 
 * This activity is launched to request permissions or launch intents for results
 * because ActivityResultLauncher must be registered during onCreate lifecycle.
 * By using a separate activity, we can request permissions or launch intents
 * from any context (View, Service, etc.) without requiring the caller to register
 * launchers in their onCreate.
 * 
 * The activity is transparent and non-touchable, so it appears invisible to the user.
 * Only the system permission dialog or picker is shown.
 */
class CometChatPermissionActivity : AppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var permissions: Array<String>? = null
    private var intentToLaunch: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make the activity transparent and non-touchable
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        
        // Get permissions or intent from intent extras
        permissions = intent.getStringArrayExtra(PERMISSION_STRING)
        @Suppress("DEPRECATION")
        intentToLaunch = intent.getParcelableExtra(INTENT_STRING)
        
        // Register the permission launcher
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionResults ->
            val grantedPermissions = mutableListOf<String>()
            val deniedPermissions = mutableListOf<String>()
            
            for ((permission, isGranted) in permissionResults) {
                if (permissions?.contains(permission) == true) {
                    if (isGranted) {
                        grantedPermissions.add(permission)
                    } else {
                        deniedPermissions.add(permission)
                    }
                }
            }
            
            // Notify the handler
            CometChatPermissionHandler.onPermissionsRequested(grantedPermissions, deniedPermissions)
            finish()
        }
        
        // Register the activity result launcher for intents
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // Notify the handler
            CometChatPermissionHandler.onActivityResultCompleted(result)
            finish()
        }
        
        // Notify handler that activity is ready
        CometChatPermissionHandler.onActivityReady(this)
        
        // Launch permission request or intent based on what was provided
        val perms = permissions
        val intent = intentToLaunch
        when {
            perms != null -> {
                permissionLauncher.launch(perms)
            }
            intent != null -> {
                activityResultLauncher.launch(intent)
            }
            else -> {
                finish()
            }
        }
        
        // No transition animation
        overridePendingTransition(0, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        CometChatPermissionHandler.onActivityDestroyed(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        CometChatPermissionHandler.onActivityReady(this)
    }

    companion object {
        const val PERMISSION_STRING = "permissionString"
        const val INTENT_STRING = "intentString"
    }
}
