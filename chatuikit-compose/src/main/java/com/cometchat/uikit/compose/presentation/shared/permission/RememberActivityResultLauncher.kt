package com.cometchat.uikit.compose.presentation.shared.permission

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

@Composable
fun rememberActivityResultLauncher(
    onActivityResult: ((resultCode: Int, data: Intent?) -> Unit)? = null
): ManagedActivityResultLauncher {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onActivityResult?.invoke(result.resultCode, result.data)
    }
    
    return remember(launcher) {
        ManagedActivityResultLauncher(
            launchIntent = { intent -> launcher.launch(intent) }
        )
    }
}

/**
 * Wrapper class that provides a method to launch activities for result.
 *
 * @property launchIntent Function to launch an activity with an intent
 */
@Stable
class ManagedActivityResultLauncher(
    private val launchIntent: (Intent) -> Unit
) {
    /**
     * Launches an activity with the given intent and waits for result.
     *
     * @param intent The intent to launch the activity with
     */
    fun launch(intent: Intent) {
        launchIntent(intent)
    }
}

/**
 * Creates and remembers a launcher for a custom activity result contract.
 * This is a more flexible version that allows using custom contracts.
 *
 * @param contract The activity result contract to use
 * @param onResult Callback invoked when the result is available
 * @return A function that can be called to launch the contract
 *
 * @sample
 * ```
 * val takePictureLauncher = rememberCustomActivityResultLauncher(
 *     contract = ActivityResultContracts.TakePicturePreview(),
 *     onResult = { bitmap ->
 *         bitmap?.let {
 *             // Handle captured image bitmap
 *         }
 *     }
 * )
 *
 * Button(onClick = { takePictureLauncher() }) {
 *     Text("Take Picture")
 * }
 * ```
 */
@Composable
fun <I, O> rememberCustomActivityResultLauncher(
    contract: ActivityResultContract<I, O>,
    onResult: (O) -> Unit
): (I) -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = contract,
        onResult = onResult
    )
    
    return remember(launcher) {
        { input: I -> launcher.launch(input) }
    }
}