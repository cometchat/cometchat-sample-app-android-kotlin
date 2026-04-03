package com.cometchat.uikit.compose.presentation.shared.mediarecorder

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Example usage of CometChatMediaRecorder component
 */
@Composable
fun MediaRecorderExample(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CometChat Media Recorder",
            style = CometChatTheme.typography.heading2Bold,
            color = CometChatTheme.colorScheme.textColorPrimary,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        CometChatMediaRecorder(
            modifier = Modifier.fillMaxWidth(),
            onClose = {
                Toast.makeText(context, "Recording cancelled", Toast.LENGTH_SHORT).show()
            },
            onSubmit = { file, ctx ->
                if (file != null) {
                    Toast.makeText(ctx, "Recording submitted: ${file.name}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(ctx, "No recording to submit", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

/**
 * Preview for MediaRecorderExample
 */
@Preview(showBackground = true)
@Composable
fun MediaRecorderExamplePreview() {
    CometChatTheme {
        MediaRecorderExample()
    }
}

/**
 * Customized MediaRecorder with custom colors
 */
@Composable
fun CustomMediaRecorderExample(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    CometChatMediaRecorder(
        modifier = modifier,
        style = MediaRecorderStyle(
            backgroundColor = CometChatTheme.colorScheme.backgroundColor2,
            recordingIconBackgroundColor = CometChatTheme.colorScheme.primary,
            recordingIconTint = CometChatTheme.colorScheme.colorWhite,
            startIconTint = CometChatTheme.colorScheme.errorColor,
            sendIconTint = CometChatTheme.colorScheme.primary,
            cornerRadius = 12.dp,
            strokeWidth = 2.dp
        ),
        onClose = {
            // Handle close action
            Toast.makeText(context, "Recording cancelled", Toast.LENGTH_SHORT).show()
        },
        onSubmit = { file, ctx ->
            // Handle submit action
            file?.let {
                // Process the recorded file
                Toast.makeText(ctx, "Processing audio file: ${it.absolutePath}", Toast.LENGTH_LONG).show()
            }
        }
    )
}

/**
 * Preview for CustomMediaRecorderExample
 */
@Preview(showBackground = true)
@Composable
fun CustomMediaRecorderExamplePreview() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            CustomMediaRecorderExample(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}