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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.compose.presentation.utils.RoborazziConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Compose screenshot tests for CometChatAIAssistantChatHistory using Roborazzi.
 *
 * Uses the simulated-view approach (same as chatuikit-kotlin) to render mock data
 * directly, avoiding SDK dependencies that require CometChat.init().
 *
 * States captured:
 * - Empty state (no conversations history)
 * - Content state (messages with date separators)
 * - Dark mode variants
 * - Custom styling
 * - Loading (shimmer) state
 * - Error state
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*CometChatAIAssistantChatHistoryScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatAIAssistantChatHistoryScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/aiassistantchathistory"
        )
    )

    // ==================== Colors ====================

    private val BG_COLOR = Color.White
    private val BG_COLOR_DARK = Color(0xFF1A1A2E)
    private val HEADER_BG = Color.White
    private val HEADER_BG_DARK = Color(0xFF1A1A2E)
    private val TEXT_PRIMARY = Color(0xFF1A1A1A)
    private val TEXT_PRIMARY_DARK = Color(0xFFEEEEEE)
    private val TEXT_SECONDARY = Color(0xFF666666)
    private val TEXT_SECONDARY_DARK = Color(0xFFAAAAAA)
    private val TEXT_TERTIARY = Color(0xFF999999)
    private val TEXT_TERTIARY_DARK = Color(0xFF777777)
    private val DIVIDER_COLOR = Color(0xFFE8E8E8)
    private val DIVIDER_COLOR_DARK = Color(0xFF333344)
    private val ICON_TINT = Color(0xFF666666)
    private val ICON_TINT_DARK = Color(0xFFAAAAAA)
    private val DATE_BG = Color(0xFFF5F5F5)
    private val DATE_BG_DARK = Color(0xFF2A2A3E)
    private val ACCENT_COLOR = Color(0xFF6851D6)
    private val ERROR_COLOR = Color(0xFFF44336)
    private val SHIMMER_BASE = Color(0xFFEEEEEE)
    private val SHIMMER_BASE_DARK = Color(0xFF2D2D44)

    // ==================== Mock Data ====================

    private data class MockChatHistoryItem(val text: String, val sentAt: Long)

    private val mockMessages = listOf(
        MockChatHistoryItem("How do I implement push notifications in Android?", 1706745600L),
        MockChatHistoryItem("Can you explain the difference between LiveData and StateFlow?", 1706745900L),
        MockChatHistoryItem("What's the best way to handle configuration changes?", 1706746200L),
        MockChatHistoryItem("Help me write a unit test for my ViewModel", 1706659200L),
        MockChatHistoryItem("How to use Hilt for dependency injection?", 1706659500L),
        MockChatHistoryItem("Explain coroutine scopes and structured concurrency", 1706659800L),
        MockChatHistoryItem("What are the best practices for Room database migrations?", 1706572800L),
        MockChatHistoryItem("How to implement pagination with Paging 3 library?", 1706573100L),
        MockChatHistoryItem("Can you help me debug this RecyclerView performance issue?", 1706573400L),
        MockChatHistoryItem("What's the recommended architecture for a multi-module project?", 1706573700L)
    )

    // ==================== UI States ====================

    @Test
    fun stateEmpty() {
        captureComposable {
            ChatHistoryScreen(dark = false) {
                EmptyState(dark = false)
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateEmptyDark() {
        captureComposable {
            ChatHistoryScreen(dark = true) {
                EmptyState(dark = true)
            }
        }
    }

    @Test
    fun stateContent() {
        captureComposable {
            ChatHistoryScreen(dark = false) {
                ContentState(dark = false)
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        captureComposable {
            ChatHistoryScreen(dark = true) {
                ContentState(dark = true)
            }
        }
    }

    @Test
    fun stateLoading() {
        captureComposable {
            ChatHistoryScreen(dark = false) {
                LoadingState(dark = false)
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateLoadingDark() {
        captureComposable {
            ChatHistoryScreen(dark = true) {
                LoadingState(dark = true)
            }
        }
    }

    @Test
    fun stateError() {
        captureComposable {
            ChatHistoryScreen(dark = false) {
                ErrorState(dark = false)
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateErrorDark() {
        captureComposable {
            ChatHistoryScreen(dark = true) {
                ErrorState(dark = true)
            }
        }
    }

    // ==================== Style Variants ====================

    @Test
    fun styleCustomBackground() {
        val customBg = Color(0xFFF5F5DC)
        captureComposable {
            ChatHistoryScreen(dark = false, bgColor = customBg, headerBg = customBg) {
                ContentState(dark = false)
            }
        }
    }

    @Test
    fun stylingCustomColors() {
        captureComposable {
            ChatHistoryScreen(dark = false) {
                ContentStateCustomColors()
            }
        }
    }

    // ==================== Content Variants ====================

    @Test
    fun contentWithDateSeparators() {
        captureComposable {
            ChatHistoryScreen(dark = false) {
                ContentWithMultipleDates()
            }
        }
    }

    @Test
    fun contentSingleMessage() {
        captureComposable {
            ChatHistoryScreen(dark = false) {
                Column {
                    DateSeparator("Today", dark = false)
                    MessageItem("How do I get started with CometChat SDK?", dark = false)
                }
            }
        }
    }

    @Test
    fun contentLongMessages() {
        captureComposable {
            ChatHistoryScreen(dark = false) {
                ContentLongMessages()
            }
        }
    }

    // ==================== Popup Menu ====================

    @Test
    fun popupMenuDelete() {
        captureComposable {
            ChatHistoryScreen(dark = false) {
                Column {
                    DateSeparator("Today", dark = false)
                    MessageItem(mockMessages[0].text, dark = false)
                    MessageItem(mockMessages[1].text, dark = false, highlighted = true)
                    PopupMenu(listOf("Delete"))
                    MessageItem(mockMessages[2].text, dark = false)
                }
            }
        }
    }

    // ==================== Helper: Capture ====================

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

    // ==================== Composable Builders ====================

    @Composable
    private fun ChatHistoryScreen(
        dark: Boolean,
        bgColor: Color? = null,
        headerBg: Color? = null,
        content: @Composable () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor ?: if (dark) BG_COLOR_DARK else BG_COLOR)
        ) {
            Header(dark = dark, headerBg = headerBg)
            Divider(color = if (dark) DIVIDER_COLOR_DARK else DIVIDER_COLOR, thickness = 1.dp)
            NewChatRow(dark = dark)
            content()
        }
    }

    @Composable
    private fun Header(dark: Boolean, headerBg: Color? = null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBg ?: if (dark) HEADER_BG_DARK else HEADER_BG)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = if (dark) ICON_TINT_DARK else ICON_TINT,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Chat History",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY,
                modifier = Modifier.weight(1f)
            )
        }
    }

    @Composable
    private fun NewChatRow(dark: Boolean) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "New Chat",
                tint = if (dark) ICON_TINT_DARK else ICON_TINT,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "New Chat",
                fontSize = 14.sp,
                color = if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY
            )
        }
    }

    @Composable
    private fun DateSeparator(dateText: String, dark: Boolean, textColor: Color? = null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (dark) DATE_BG_DARK else DATE_BG)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = dateText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor ?: if (dark) TEXT_TERTIARY_DARK else TEXT_TERTIARY
            )
        }
    }

    @Composable
    private fun MessageItem(
        text: String,
        dark: Boolean,
        textColor: Color? = null,
        highlighted: Boolean = false
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (highlighted) Modifier.background(
                        if (dark) Color(0xFF2D2D44) else Color(0xFFE8E8F8)
                    ) else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                color = textColor ?: if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @Composable
    private fun ContentState(dark: Boolean) {
        LazyColumn {
            item { DateSeparator("Jan 31, 2024", dark = dark) }
            items(mockMessages.take(3)) { msg -> MessageItem(msg.text, dark = dark) }
            item { DateSeparator("Jan 30, 2024", dark = dark) }
            items(mockMessages.subList(3, 6)) { msg -> MessageItem(msg.text, dark = dark) }
            item { DateSeparator("Jan 29, 2024", dark = dark) }
            items(mockMessages.subList(6, 10)) { msg -> MessageItem(msg.text, dark = dark) }
        }
    }

    @Composable
    private fun ContentStateCustomColors() {
        Column {
            DateSeparator("Jan 31, 2024", dark = false, textColor = ACCENT_COLOR)
            mockMessages.take(3).forEach { msg ->
                MessageItem(msg.text, dark = false, textColor = ACCENT_COLOR)
            }
            DateSeparator("Jan 30, 2024", dark = false, textColor = ACCENT_COLOR)
            mockMessages.subList(3, 5).forEach { msg ->
                MessageItem(msg.text, dark = false, textColor = ACCENT_COLOR)
            }
        }
    }

    @Composable
    private fun ContentWithMultipleDates() {
        Column {
            DateSeparator("Today", dark = false)
            MessageItem("What's the latest version of Kotlin?", dark = false)
            MessageItem("How to use sealed interfaces?", dark = false)
            DateSeparator("Yesterday", dark = false)
            MessageItem("Explain Compose recomposition", dark = false)
            MessageItem("Best practices for state hoisting", dark = false)
            DateSeparator("Jan 28, 2024", dark = false)
            MessageItem("How to implement dark theme properly?", dark = false)
            MessageItem("Material 3 dynamic colors setup", dark = false)
            DateSeparator("Jan 25, 2024", dark = false)
            MessageItem("Jetpack Navigation with deep links", dark = false)
            MessageItem("How to handle back navigation in Compose?", dark = false)
        }
    }

    @Composable
    private fun ContentLongMessages() {
        Column {
            DateSeparator("Today", dark = false)
            MessageItem(
                "Can you explain the complete lifecycle of an Android Activity including all the callbacks and when they are triggered during configuration changes, process death, and normal navigation?",
                dark = false
            )
            MessageItem(
                "What is the recommended way to implement a complex multi-step form with validation, state persistence across configuration changes, and proper error handling in Jetpack Compose?",
                dark = false
            )
            MessageItem(
                "Help me understand the differences between ViewModelScope, lifecycleScope, and GlobalScope in Kotlin coroutines and when to use each one",
                dark = false
            )
            DateSeparator("Yesterday", dark = false)
            MessageItem(
                "How to properly implement offline-first architecture with Room database, WorkManager for sync, and proper conflict resolution strategies?",
                dark = false
            )
            MessageItem(
                "Explain the complete flow of dependency injection with Hilt including custom scopes, assisted injection, and multi-module setup",
                dark = false
            )
        }
    }

    @Composable
    private fun LoadingState(dark: Boolean) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val widths = listOf(0.85f, 0.65f, 0.75f, 0.55f, 0.85f, 0.65f, 0.75f, 0.55f, 0.85f, 0.65f)
            widths.forEach { fraction ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (dark) SHIMMER_BASE_DARK else SHIMMER_BASE)
                )
            }
        }
    }

    @Composable
    private fun EmptyState(dark: Boolean) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (dark) SHIMMER_BASE_DARK else SHIMMER_BASE)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No conversations history",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Start a new conversation with the AI assistant",
                    fontSize = 13.sp,
                    color = if (dark) TEXT_SECONDARY_DARK else TEXT_SECONDARY
                )
            }
        }
    }

    @Composable
    private fun ErrorState(dark: Boolean) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (dark) Color(0xFF442222) else Color(0xFFFFEBEE))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Something went wrong",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Unable to load conversations. Please try again.",
                    fontSize = 13.sp,
                    color = if (dark) TEXT_SECONDARY_DARK else TEXT_SECONDARY
                )
            }
        }
    }

    @Composable
    private fun PopupMenu(options: List<String>) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Column(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(4.dp)
            ) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🗑️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = option, fontSize = 14.sp, color = ERROR_COLOR)
                    }
                }
            }
        }
    }
}
