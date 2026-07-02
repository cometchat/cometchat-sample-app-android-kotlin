package com.cometchat.uikit.compose.screenshots

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.compose.presentation.utils.RoborazziConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Basic Roborazzi screenshot test for CometChatMessageList bubble rendering.
 *
 * Renders each bubble type individually so every bubble is fully visible
 * without scrolling. Covers left (incoming), right (outgoing), and center
 * (action/system) alignments for all message types.
 *
 * Run:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*.CometChatMessageListScreenshotTest"
 *   ./gradlew :chatuikit-compose:verifyRoborazziDebug --tests "*.CometChatMessageListScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatMessageListScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/messagelist"
        )
    )

    // ==================== Section 1: Text Bubbles ====================

    @Test
    fun textBubble_incomingAndOutgoing() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CometChatTheme.colorScheme.backgroundColor3)
                        .padding(vertical = 8.dp)
                ) {
                    DateSeparatorBubble("January 1, 2025")
                    IncomingTextBubble("Alice Smith", "Hey! How are you doing today?", "10:30 AM")
                    OutgoingTextBubble("I'm doing great, thanks! Working on the new feature.", "10:31 AM")
                    IncomingTextBubble("Bob Johnson", "That sounds exciting! Let me know if you need any help with the implementation. I've worked on similar features before.", "10:32 AM")
                    OutgoingTextBubble("Sure, let's discuss tomorrow! 👍🎉", "10:33 AM")
                }
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun textBubble_incomingAndOutgoing_dark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CometChatTheme.colorScheme.backgroundColor3)
                        .padding(vertical = 8.dp)
                ) {
                    DateSeparatorBubble("January 1, 2025")
                    IncomingTextBubble("Alice Smith", "Hey! How are you doing today?", "10:30 AM")
                    OutgoingTextBubble("I'm doing great, thanks! Working on the new feature.", "10:31 AM")
                    IncomingTextBubble("Bob Johnson", "That sounds exciting! Let me know if you need any help.", "10:32 AM")
                    OutgoingTextBubble("Sure, let's discuss tomorrow! 👍🎉", "10:33 AM")
                }
            }
        }
    }

    // ==================== Section 2: Image Bubbles ====================

    @Test
    fun imageBubble_incomingAndOutgoing() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CometChatTheme.colorScheme.backgroundColor3)
                        .padding(vertical = 8.dp)
                ) {
                    DateSeparatorBubble("Today")
                    IncomingMediaBubble("Alice Smith", "🖼️ Image", "10:34 AM", Color(0xFFE3F2FD), 160)
                    OutgoingMediaBubble("🖼️ Image", "10:35 AM", Color(0xFFE8F5E9), 160)
                    IncomingMediaBubble("Bob Johnson", "🖼️ Screenshot.png", "10:36 AM", Color(0xFFF3E5F5), 140)
                    OutgoingMediaBubble("🖼️ Photo_2025.jpg", "10:37 AM", Color(0xFFFFF9C4), 140)
                }
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun imageBubble_incomingAndOutgoing_dark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CometChatTheme.colorScheme.backgroundColor3)
                        .padding(vertical = 8.dp)
                ) {
                    DateSeparatorBubble("Today")
                    IncomingMediaBubble("Alice Smith", "🖼️ Image", "10:34 AM", Color(0xFF1A237E), 160)
                    OutgoingMediaBubble("🖼️ Image", "10:35 AM", Color(0xFF1B5E20), 160)
                }
            }
        }
    }

    // ==================== Section 3: Video Bubbles ====================

    @Test
    fun videoBubble_incomingAndOutgoing() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CometChatTheme.colorScheme.backgroundColor3)
                        .padding(vertical = 8.dp)
                ) {
                    DateSeparatorBubble("Today")
                    IncomingMediaBubble("Charlie Brown", "🎬 Video (2:15)", "11:00 AM", Color(0xFFE0F7FA), 160)
                    OutgoingMediaBubble("🎬 Video (0:45)", "11:01 AM", Color(0xFFFFF9C4), 160)
                    IncomingMediaBubble("Alice Smith", "🎬 Recording.mp4 (5:30)", "11:02 AM", Color(0xFFE8EAF6), 140)
                    OutgoingMediaBubble("🎬 Screen_record.mp4 (1:20)", "11:03 AM", Color(0xFFE0F2F1), 140)
                }
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun videoBubble_incomingAndOutgoing_dark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CometChatTheme.colorScheme.backgroundColor3)
                        .padding(vertical = 8.dp)
                ) {
                    DateSeparatorBubble("Today")
                    IncomingMediaBubble("Charlie Brown", "🎬 Video (2:15)", "11:00 AM", Color(0xFF004D40), 160)
                    OutgoingMediaBubble("🎬 Video (0:45)", "11:01 AM", Color(0xFF1A237E), 160)
                }
            }
        }
    }

    // ==================== Section 4: Audio Bubbles ====================

    @Test
    fun audioBubble_incomingAndOutgoing() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CometChatTheme.colorScheme.backgroundColor3)
                        .padding(vertical = 8.dp)
                ) {
                    DateSeparatorBubble("Today")
                    IncomingAudioBubble("Alice Smith", "0:32", "10:38 AM")
                    OutgoingAudioBubble("1:05", "10:39 AM")
                    IncomingAudioBubble("Bob Johnson", "2:48", "10:40 AM")
                    OutgoingAudioBubble("0:15", "10:41 AM")
                }
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun audioBubble_incomingAndOutgoing_dark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CometChatTheme.colorScheme.backgroundColor3)
                        .padding(vertical = 8.dp)
                ) {
                    DateSeparatorBubble("Today")
                    IncomingAudioBubble("Alice Smith", "0:32", "10:38 AM")
                    OutgoingAudioBubble("1:05", "10:39 AM")
                }
            }
        }
    }

    // ==================== Section 5: File/Document Bubbles ====================

    @Test
    fun fileBubble_incomingAndOutgoing() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CometChatTheme.colorScheme.backgroundColor3)
                        .padding(vertical = 8.dp)
                ) {
                    DateSeparatorBubble("Today")
                    IncomingFileBubble("Bob Johnson", "project_spec.pdf", "2.4 MB", "10:36 AM")
                    OutgoingFileBubble("design_doc.pdf", "1.8 MB", "10:37 AM")
                    IncomingFileBubble("Alice Smith", "meeting_notes.docx", "540 KB", "10:42 AM")
                    OutgoingFileBubble("report_final.xlsx", "3.1 MB", "10:43 AM")
                }
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun fileBubble_incomingAndOutgoing_dark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CometChatTheme.colorScheme.backgroundColor3)
                        .padding(vertical = 8.dp)
                ) {
                    DateSeparatorBubble("Today")
                    IncomingFileBubble("Bob Johnson", "project_spec.pdf", "2.4 MB", "10:36 AM")
                    OutgoingFileBubble("design_doc.pdf", "1.8 MB", "10:37 AM")
                }
            }
        }
    }

    // ==================== Section 6: Custom Bubbles ====================

    @Test
    fun customBubble_incomingAndOutgoing() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CometChatTheme.colorScheme.backgroundColor3)
                        .padding(vertical = 8.dp)
                ) {
                    DateSeparatorBubble("Today")
                    IncomingCustomBubble("Alice Smith", "📊 Poll: Team Lunch", "Where should we eat today?", "11:00 AM")
                    OutgoingCustomBubble("📍 Location Shared", "123 Main Street, City Center", "11:01 AM")
                    IncomingCustomBubble("Bob Johnson", "📅 Meeting Invite", "Sprint Planning - 3:00 PM", "11:05 AM")
                    OutgoingCustomBubble("🎫 Sticker", "👋 Hello!", "11:06 AM")
                }
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun customBubble_incomingAndOutgoing_dark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CometChatTheme.colorScheme.backgroundColor3)
                        .padding(vertical = 8.dp)
                ) {
                    DateSeparatorBubble("Today")
                    IncomingCustomBubble("Alice Smith", "📊 Poll: Team Lunch", "Where should we eat today?", "11:00 AM")
                    OutgoingCustomBubble("📍 Location Shared", "123 Main Street, City Center", "11:01 AM")
                }
            }
        }
    }

    // ==================== Section 7: Action/Center Bubbles ====================

    @Test
    fun actionBubble_centerAligned() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CometChatTheme.colorScheme.backgroundColor3)
                        .padding(vertical = 8.dp)
                ) {
                    DateSeparatorBubble("January 1, 2025")
                    ActionBubble("Alice Smith joined the group")
                    IncomingTextBubble("Alice Smith", "Hi everyone!", "10:00 AM")
                    ActionBubble("Bob Johnson was added by Admin")
                    ActionBubble("📞 Voice call ended • 5:32")
                    ActionBubble("📹 Video call ended • 12:05")
                    ActionBubble("Charlie left the group")
                    DateSeparatorBubble("Today")
                    ActionBubble("Group name changed to \"Project Alpha\"")
                }
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun actionBubble_centerAligned_dark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CometChatTheme.colorScheme.backgroundColor3)
                        .padding(vertical = 8.dp)
                ) {
                    DateSeparatorBubble("January 1, 2025")
                    ActionBubble("Alice Smith joined the group")
                    ActionBubble("📞 Voice call ended • 5:32")
                    ActionBubble("📹 Video call ended • 12:05")
                    ActionBubble("Charlie left the group")
                    DateSeparatorBubble("Today")
                    ActionBubble("Group name changed to \"Project Alpha\"")
                }
            }
        }
    }

    // ==================== Section 8: Deleted Message Bubbles ====================

    @Test
    fun deletedBubble_incomingAndOutgoing() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CometChatTheme.colorScheme.backgroundColor3)
                        .padding(vertical = 8.dp)
                ) {
                    DateSeparatorBubble("Today")
                    IncomingTextBubble("Alice Smith", "Check this out!", "10:30 AM")
                    DeletedMessageBubbleIncoming("Alice Smith")
                    OutgoingTextBubble("What was that?", "10:32 AM")
                    DeletedMessageBubbleOutgoing()
                    IncomingTextBubble("Bob Johnson", "Never mind 😅", "10:33 AM")
                }
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun deletedBubble_incomingAndOutgoing_dark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CometChatTheme.colorScheme.backgroundColor3)
                        .padding(vertical = 8.dp)
                ) {
                    DateSeparatorBubble("Today")
                    DeletedMessageBubbleIncoming("Alice Smith")
                    DeletedMessageBubbleOutgoing()
                }
            }
        }
    }

    // ==================== Bubble Composables ====================

    @Composable
    private fun DateSeparatorBubble(text: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = CometChatTheme.typography.caption1Medium,
                color = CometChatTheme.colorScheme.textColorPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CometChatTheme.colorScheme.backgroundColor2)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }

    @Composable
    private fun ActionBubble(text: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = CometChatTheme.typography.caption1Regular,
                color = CometChatTheme.colorScheme.textColorSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CometChatTheme.colorScheme.backgroundColor2.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }

    @Composable
    private fun IncomingTextBubble(senderName: String, text: String, time: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CometChatTheme.colorScheme.extendedPrimaryColor500),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = senderName.take(1).uppercase(),
                    color = Color.White,
                    style = CometChatTheme.typography.caption1Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.widthIn(max = 260.dp)) {
                Text(
                    text = senderName,
                    style = CometChatTheme.typography.caption2Medium,
                    color = CometChatTheme.colorScheme.textColorSecondary,
                    modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp, 12.dp, 12.dp, 12.dp))
                        .background(CometChatTheme.colorScheme.backgroundColor1)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            text = text,
                            style = CometChatTheme.typography.bodyRegular,
                            color = CometChatTheme.colorScheme.textColorPrimary
                        )
                        Text(
                            text = time,
                            style = CometChatTheme.typography.caption2Regular,
                            color = CometChatTheme.colorScheme.textColorTertiary,
                            modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun OutgoingTextBubble(text: String, time: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clip(RoundedCornerShape(12.dp, 4.dp, 12.dp, 12.dp))
                    .background(CometChatTheme.colorScheme.extendedPrimaryColor500)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = text,
                        style = CometChatTheme.typography.bodyRegular,
                        color = Color.White
                    )
                    Text(
                        text = time,
                        style = CometChatTheme.typography.caption2Regular,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun IncomingMediaBubble(senderName: String, mediaType: String, time: String, color: Color, height: Int = 120) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CometChatTheme.colorScheme.extendedPrimaryColor500),
                contentAlignment = Alignment.Center
            ) {
                Text(senderName.take(1).uppercase(), color = Color.White, style = CometChatTheme.typography.caption1Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.widthIn(max = 260.dp)) {
                Text(
                    text = senderName,
                    style = CometChatTheme.typography.caption2Medium,
                    color = CometChatTheme.colorScheme.textColorSecondary,
                    modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp, 12.dp, 12.dp, 12.dp))
                        .background(CometChatTheme.colorScheme.backgroundColor1)
                        .padding(4.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(height.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(mediaType, style = CometChatTheme.typography.caption1Medium, color = CometChatTheme.colorScheme.textColorSecondary)
                        }
                        Text(
                            text = time,
                            style = CometChatTheme.typography.caption2Regular,
                            color = CometChatTheme.colorScheme.textColorTertiary,
                            modifier = Modifier.align(Alignment.End).padding(top = 4.dp, end = 8.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun OutgoingMediaBubble(mediaType: String, time: String, color: Color, height: Int = 120) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clip(RoundedCornerShape(12.dp, 4.dp, 12.dp, 12.dp))
                    .background(CometChatTheme.colorScheme.extendedPrimaryColor500.copy(alpha = 0.1f))
                    .padding(4.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(mediaType, style = CometChatTheme.typography.caption1Medium, color = CometChatTheme.colorScheme.textColorSecondary)
                    }
                    Text(
                        text = time,
                        style = CometChatTheme.typography.caption2Regular,
                        color = CometChatTheme.colorScheme.textColorTertiary,
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp, end = 8.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun IncomingAudioBubble(senderName: String, duration: String, time: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CometChatTheme.colorScheme.extendedPrimaryColor500),
                contentAlignment = Alignment.Center
            ) {
                Text(senderName.take(1).uppercase(), color = Color.White, style = CometChatTheme.typography.caption1Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.widthIn(max = 260.dp)) {
                Text(
                    text = senderName,
                    style = CometChatTheme.typography.caption2Medium,
                    color = CometChatTheme.colorScheme.textColorSecondary,
                    modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp, 12.dp, 12.dp, 12.dp))
                        .background(CometChatTheme.colorScheme.backgroundColor1)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CometChatTheme.colorScheme.extendedPrimaryColor500.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▶", color = CometChatTheme.colorScheme.extendedPrimaryColor500)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(CometChatTheme.colorScheme.extendedPrimaryColor500.copy(alpha = 0.3f))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(duration, style = CometChatTheme.typography.caption2Regular, color = CometChatTheme.colorScheme.textColorTertiary)
                                Text(time, style = CometChatTheme.typography.caption2Regular, color = CometChatTheme.colorScheme.textColorTertiary)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun OutgoingAudioBubble(duration: String, time: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clip(RoundedCornerShape(12.dp, 4.dp, 12.dp, 12.dp))
                    .background(CometChatTheme.colorScheme.extendedPrimaryColor500)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("▶", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.4f))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(duration, style = CometChatTheme.typography.caption2Regular, color = Color.White.copy(alpha = 0.7f))
                            Text(time, style = CometChatTheme.typography.caption2Regular, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun IncomingFileBubble(senderName: String, fileName: String, fileSize: String, time: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CometChatTheme.colorScheme.extendedPrimaryColor500),
                contentAlignment = Alignment.Center
            ) {
                Text(senderName.take(1).uppercase(), color = Color.White, style = CometChatTheme.typography.caption1Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.widthIn(max = 260.dp)) {
                Text(
                    text = senderName,
                    style = CometChatTheme.typography.caption2Medium,
                    color = CometChatTheme.colorScheme.textColorSecondary,
                    modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp, 12.dp, 12.dp, 12.dp))
                        .background(CometChatTheme.colorScheme.backgroundColor1)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFF3E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📄", style = CometChatTheme.typography.heading4Medium)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(fileName, style = CometChatTheme.typography.caption1Medium, color = CometChatTheme.colorScheme.textColorPrimary)
                            Text(fileSize, style = CometChatTheme.typography.caption2Regular, color = CometChatTheme.colorScheme.textColorTertiary)
                        }
                        Text(time, style = CometChatTheme.typography.caption2Regular, color = CometChatTheme.colorScheme.textColorTertiary)
                    }
                }
            }
        }
    }

    @Composable
    private fun OutgoingFileBubble(fileName: String, fileSize: String, time: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clip(RoundedCornerShape(12.dp, 4.dp, 12.dp, 12.dp))
                    .background(CometChatTheme.colorScheme.extendedPrimaryColor500)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📄", style = CometChatTheme.typography.heading4Medium)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(fileName, style = CometChatTheme.typography.caption1Medium, color = Color.White)
                        Text(fileSize, style = CometChatTheme.typography.caption2Regular, color = Color.White.copy(alpha = 0.7f))
                    }
                    Text(time, style = CometChatTheme.typography.caption2Regular, color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }

    @Composable
    private fun IncomingCustomBubble(senderName: String, title: String, subtitle: String, time: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CometChatTheme.colorScheme.extendedPrimaryColor500),
                contentAlignment = Alignment.Center
            ) {
                Text(senderName.take(1).uppercase(), color = Color.White, style = CometChatTheme.typography.caption1Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.widthIn(max = 260.dp)) {
                Text(
                    text = senderName,
                    style = CometChatTheme.typography.caption2Medium,
                    color = CometChatTheme.colorScheme.textColorSecondary,
                    modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp, 12.dp, 12.dp, 12.dp))
                        .background(CometChatTheme.colorScheme.backgroundColor1)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Column {
                        Text(title, style = CometChatTheme.typography.caption1Bold, color = CometChatTheme.colorScheme.textColorPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(subtitle, style = CometChatTheme.typography.bodyRegular, color = CometChatTheme.colorScheme.textColorSecondary)
                        Text(
                            text = time,
                            style = CometChatTheme.typography.caption2Regular,
                            color = CometChatTheme.colorScheme.textColorTertiary,
                            modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun OutgoingCustomBubble(title: String, subtitle: String, time: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clip(RoundedCornerShape(12.dp, 4.dp, 12.dp, 12.dp))
                    .background(CometChatTheme.colorScheme.extendedPrimaryColor500)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(title, style = CometChatTheme.typography.caption1Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(subtitle, style = CometChatTheme.typography.bodyRegular, color = Color.White.copy(alpha = 0.85f))
                    Text(
                        text = time,
                        style = CometChatTheme.typography.caption2Regular,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun DeletedMessageBubbleIncoming(senderName: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CometChatTheme.colorScheme.extendedPrimaryColor500),
                contentAlignment = Alignment.Center
            ) {
                Text(senderName.take(1).uppercase(), color = Color.White, style = CometChatTheme.typography.caption1Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.widthIn(max = 260.dp)) {
                Text(
                    text = senderName,
                    style = CometChatTheme.typography.caption2Medium,
                    color = CometChatTheme.colorScheme.textColorSecondary,
                    modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp, 12.dp, 12.dp, 12.dp))
                        .background(CometChatTheme.colorScheme.backgroundColor1.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "⊘ This message was deleted",
                        style = CometChatTheme.typography.bodyRegular,
                        fontStyle = FontStyle.Italic,
                        color = CometChatTheme.colorScheme.textColorTertiary
                    )
                }
            }
        }
    }

    @Composable
    private fun DeletedMessageBubbleOutgoing() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clip(RoundedCornerShape(12.dp, 4.dp, 12.dp, 12.dp))
                    .background(CometChatTheme.colorScheme.extendedPrimaryColor500.copy(alpha = 0.3f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "⊘ You deleted this message",
                    style = CometChatTheme.typography.bodyRegular,
                    fontStyle = FontStyle.Italic,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }

    // ==================== Helper: Static Capture ====================

    private fun captureComposable(content: @Composable () -> Unit) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setContent { content() }
        }
        scenario.onActivity { activity ->
            val composeView = activity.window.decorView
                .findViewById<ViewGroup>(android.R.id.content)
                .getChildAt(0)
            composeView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }
}
