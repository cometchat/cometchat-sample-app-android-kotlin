package com.cometchat.sampleapp.compose.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.cometchat.sampleapp.compose.navigation.AppNavGraph
import com.cometchat.sampleapp.compose.ui.theme.SampleAppTheme

/**
 * Main activity for the CometChat Sample App (Jetpack Compose).
 *
 * This is the single activity that hosts all Compose screens.
 * It sets up the app theme and navigation graph.
 *
 * ## Architecture:
 * - Single Activity architecture with Compose Navigation
 * - All screens are composables managed by NavHost
 * - Theme is applied at the root level
 *
 * ## Features:
 * - Edge-to-edge display support
 * - Light and dark theme support
 * - CometChat UIKit theme integration
 *
 * @see AppNavGraph for navigation setup
 * @see SampleAppTheme for theme configuration
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        // Ensure the window handles insets properly for keyboard
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SampleAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavGraph()
                }
            }
        }
    }
}
