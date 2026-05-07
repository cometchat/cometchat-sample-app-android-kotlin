package com.cometchat.ai.sampleapp.compose.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.cometchat.ai.sampleapp.compose.navigation.AppNavGraph
import com.cometchat.ai.sampleapp.compose.ui.theme.AIAssistantSampleAppTheme

/**
 * Single-activity host for the AI Assistant Sample App (Compose).
 * Applies edge-to-edge + the Compose navigation graph.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AIAssistantSampleAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph()
                }
            }
        }
    }
}
