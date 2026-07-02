package com.cometchat.sampleapp.compose.push.appflow

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.incomingcall.ui.CometChatIncomingCall
import com.cometchat.sampleapp.compose.push.ComposeApplication
import com.cometchat.sampleapp.compose.push.ui.AppTheme

/**
 * Activity that hosts the App Flow.
 */
class AppFlowActivity : ComponentActivity() {
    
    companion object {
        fun launch(activity: ComponentActivity) {
            val intent = Intent(activity, AppFlowActivity::class.java)
            activity.startActivity(intent)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        setContent {
            AppFlowContent()
        }
    }
}

/**
 * Composable content for AppFlowActivity with incoming call overlay.
 */
@Composable
private fun AppFlowContent() {
    val systemDarkMode = isSystemInDarkTheme()
    var isDarkMode by remember { mutableStateOf(systemDarkMode) }
    
    val application = ComposeApplication.getInstance()
    val incomingCall by application?.incomingCall?.collectAsState() ?: remember { mutableStateOf(null) }
    
    AppTheme(isDarkTheme = isDarkMode) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Incoming call overlay at the top
            incomingCall?.let { call ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                        .align(Alignment.TopCenter)
                ) {
                    CometChatIncomingCall(
                        call = call,
                        modifier = Modifier.fillMaxWidth(),
                        disableSoundForCalls = true,
                        onAcceptClick = null,
                        onRejectClick = null,
                        onError = { exception ->
                            Log.e("AppFlowActivity", "IncomingCall error: ${exception.message}", exception)
                            application?.dismissIncomingCall()
                        }
                    )
                }
            }
        }
    }
}
