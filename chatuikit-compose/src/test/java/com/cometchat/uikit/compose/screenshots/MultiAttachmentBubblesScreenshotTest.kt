package com.cometchat.uikit.compose.screenshots

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.models.AttachmentUploadStatus
import com.cometchat.uikit.core.models.AttachmentUploadTile
import com.cometchat.uikit.compose.presentation.messagecomposer.ui.CometChatAttachmentTray
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatAudiosBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatFilesBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatImagesBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatVideosBubble
import com.cometchat.uikit.compose.presentation.utils.RoborazziConfig
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * ENG-36737 — Layer 5 (VRT) golden screenshots for the Compose per-type multi-attachment bubbles
 * ([CometChatImagesBubble] grid rules 1/2/3/4/5+ with "+N", [CometChatVideosBubble] play badge,
 * [CometChatFilesBubble] card stack + collapse, [CometChatAudiosBubble] single-no-card vs stacked
 * cards) and the composer attachment tray tile states (DONE / UPLOADING ring / FAILED retry badge /
 * REJECTED error badge across media, document, and audio tiles).
 *
 * Remote media renders as Coil's empty/placeholder state under Robolectric (no network) —
 * deterministic, which is what makes grid geometry, overlays, and card chrome comparable.
 *
 * Run:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*.MultiAttachmentBubblesScreenshotTest"
 *   ./gradlew :chatuikit-compose:compareRoborazziDebug --tests "*.MultiAttachmentBubblesScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class MultiAttachmentBubblesScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/multiattachmentbubbles"
        )
    )

    private companion object {
        /** Every message carries this exact instant: 15 Oct 2024, 4:56 PM GMT. */
        const val FIXED_SENT_AT = 1_729_011_360L

        /** Deterministic label for [FIXED_SENT_AT] — pinned to GMT so it never shifts with the host TZ. */
        val TIME_LABEL: String = SimpleDateFormat("h:mm a", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("GMT") }
            .format(Date(FIXED_SENT_AT * 1000))
    }

    // ==================== Data helpers ====================

    private fun attachment(name: String, mime: String, sizeBytes: Int = 3_200_000) = Attachment().apply {
        fileUrl = "https://cdn.example.com/$name"
        fileName = name
        fileMimeType = mime
        fileExtension = name.substringAfterLast('.', "")
        fileSize = sizeBytes
    }

    private fun imageAttachments(count: Int) =
        (1..count).map { attachment("photo_$it.jpg", "image/jpeg") }

    private fun mediaMessage(
        type: String,
        attachments: List<Attachment>,
        caption: String? = null
    ): MediaMessage {
        val sender = User().apply {
            uid = "sender-1"
            name = "Alice"
        }
        return MediaMessage("receiver-1", type, CometChatConstants.RECEIVER_TYPE_USER).apply {
            this.id = 1L
            this.sender = sender
            this.sentAt = FIXED_SENT_AT
            this.category = CometChatConstants.CATEGORY_MESSAGE
            this.attachments = attachments
            caption?.let { this.caption = it }
        }
    }

    private fun tile(
        fileId: String,
        name: String,
        mime: String,
        status: AttachmentUploadStatus,
        percent: Int = 0,
        category: String? = null
    ) = AttachmentUploadTile(
        fileId = fileId,
        name = name,
        size = 3_200_000L,
        mimeType = mime,
        category = category ?: com.cometchat.uikit.core.models.defaultAttachmentCategory(mime),
        percent = percent,
        status = status
    )

    // ==================== Capture helper ====================

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

    /** Light canvas — #EEEEEE so light bubbles stay visible; dark mode gets its own dark canvas. */
    private val canvasLight = Color(0xFFEEEEEE)
    private val canvasDark = Color(0xFF121212)

    private fun captureBubble(dark: Boolean = false, content: @Composable () -> Unit) {
        captureComposable {
            CometChatTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (dark) canvasDark else canvasLight)
                        .padding(16.dp)
                ) {
                    content()
                }
            }
        }
    }

    /**
     * Real message-bubble chrome around a content bubble: incoming = `backgroundColor1` rounded
     * card aligned start, outgoing = `extendedPrimaryColor500` aligned end — matching the message
     * list's bubble treatment so the golden shows the bubble exactly as users see it.
     */
    @Composable
    private fun BubbleChrome(outgoing: Boolean, content: @Composable () -> Unit) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (outgoing) Arrangement.End else Arrangement.Start
        ) {
            Column(
                modifier = Modifier
                    .clip(
                        if (outgoing) RoundedCornerShape(12.dp, 4.dp, 12.dp, 12.dp)
                        else RoundedCornerShape(4.dp, 12.dp, 12.dp, 12.dp)
                    )
                    // Exactly what CometChatMessageBubbleStyle.outgoing()/incoming() use as
                    // their default bubble background: primary vs backgroundColor4 (#F5F5F5 light).
                    .background(
                        if (outgoing) CometChatTheme.colorScheme.primary
                        else CometChatTheme.colorScheme.backgroundColor4
                    )
            ) {
                content()
                Text(
                    text = TIME_LABEL,
                    style = CometChatTheme.typography.caption2Regular,
                    color = if (outgoing) Color.White.copy(alpha = 0.9f)
                    else CometChatTheme.colorScheme.textColorTertiary,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 6.dp)
                )
            }
        }
    }

    // ==================== Images grid ====================

    @Test
    fun imagesGrid_singleImage() {
        captureBubble {
            BubbleChrome(outgoing = false) {
                CometChatImagesBubble(
                    message = mediaMessage(CometChatConstants.MESSAGE_TYPE_IMAGE, imageAttachments(1)),
                    alignment = UIKitConstants.MessageBubbleAlignment.LEFT
                )
            }
        }
    }

    @Test
    fun imagesGrid_twoImages() {
        captureBubble {
            BubbleChrome(outgoing = false) {
                CometChatImagesBubble(
                    message = mediaMessage(CometChatConstants.MESSAGE_TYPE_IMAGE, imageAttachments(2)),
                    alignment = UIKitConstants.MessageBubbleAlignment.LEFT
                )
            }
        }
    }

    @Test
    fun imagesGrid_threeImages() {
        captureBubble {
            BubbleChrome(outgoing = false) {
                CometChatImagesBubble(
                    message = mediaMessage(CometChatConstants.MESSAGE_TYPE_IMAGE, imageAttachments(3)),
                    alignment = UIKitConstants.MessageBubbleAlignment.LEFT
                )
            }
        }
    }

    @Test
    fun imagesGrid_fourImages() {
        captureBubble {
            BubbleChrome(outgoing = false) {
                CometChatImagesBubble(
                    message = mediaMessage(CometChatConstants.MESSAGE_TYPE_IMAGE, imageAttachments(4)),
                    alignment = UIKitConstants.MessageBubbleAlignment.LEFT
                )
            }
        }
    }

    @Test
    fun imagesGrid_sixImages_showsPlusNOverflow() {
        captureBubble {
            BubbleChrome(outgoing = false) {
                CometChatImagesBubble(
                    message = mediaMessage(CometChatConstants.MESSAGE_TYPE_IMAGE, imageAttachments(6)),
                    alignment = UIKitConstants.MessageBubbleAlignment.LEFT
                )
            }
        }
    }

    @Test
    fun imagesGrid_withCaption_outgoing() {
        captureBubble {
            BubbleChrome(outgoing = true) {
                CometChatImagesBubble(
                    message = mediaMessage(
                        CometChatConstants.MESSAGE_TYPE_IMAGE,
                        imageAttachments(2),
                        caption = "Weekend trip photos"
                    ),
                    alignment = UIKitConstants.MessageBubbleAlignment.RIGHT
                )
            }
        }
    }

    // ==================== Videos grid ====================

    @Test
    fun videosGrid_twoVideos_showsPlayBadges() {
        captureBubble {
            BubbleChrome(outgoing = false) {
                CometChatVideosBubble(
                    message = mediaMessage(
                        CometChatConstants.MESSAGE_TYPE_VIDEO,
                        listOf(
                            attachment("clip_1.mp4", "video/mp4"),
                            attachment("clip_2.mp4", "video/mp4")
                        )
                    ),
                    alignment = UIKitConstants.MessageBubbleAlignment.LEFT
                )
            }
        }
    }

    // ==================== Files card stack ====================

    @Test
    fun filesBubble_singleFile_incoming() {
        captureBubble {
            BubbleChrome(outgoing = false) {
                CometChatFilesBubble(
                    message = mediaMessage(
                        CometChatConstants.MESSAGE_TYPE_FILE,
                        listOf(attachment("report.pdf", "application/pdf"))
                    ),
                    alignment = UIKitConstants.MessageBubbleAlignment.LEFT
                )
            }
        }
    }

    @Test
    fun filesBubble_fiveFiles_collapsedToThreeWithMoreToggle() {
        captureBubble {
            BubbleChrome(outgoing = false) {
                CometChatFilesBubble(
                    message = mediaMessage(
                        CometChatConstants.MESSAGE_TYPE_FILE,
                        listOf(
                            attachment("report.pdf", "application/pdf"),
                            attachment("notes.docx", "application/msword"),
                            attachment("data.xlsx", "application/vnd.ms-excel"),
                            attachment("slides.pptx", "application/vnd.ms-powerpoint"),
                            attachment("archive.zip", "application/zip")
                        )
                    ),
                    alignment = UIKitConstants.MessageBubbleAlignment.LEFT
                )
            }
        }
    }

    @Test
    fun filesBubble_outgoing_translucentCards() {
        captureBubble {
            BubbleChrome(outgoing = true) {
                CometChatFilesBubble(
                    message = mediaMessage(
                        CometChatConstants.MESSAGE_TYPE_FILE,
                        listOf(
                            attachment("report.pdf", "application/pdf"),
                            attachment("notes.docx", "application/msword")
                        ),
                        caption = "Q3 documents"
                    ),
                    alignment = UIKitConstants.MessageBubbleAlignment.RIGHT
                )
            }
        }
    }

    // ==================== Audio card stack ====================

    @Test
    fun audiosBubble_singleAudio_noCardOverlay() {
        captureBubble {
            BubbleChrome(outgoing = false) {
                CometChatAudiosBubble(
                    message = mediaMessage(
                        CometChatConstants.MESSAGE_TYPE_AUDIO,
                        listOf(attachment("podcast.mp3", "audio/mpeg"))
                    ),
                    alignment = UIKitConstants.MessageBubbleAlignment.LEFT
                )
            }
        }
    }

    @Test
    fun audiosBubble_threeAudios_stackedCards() {
        captureBubble {
            BubbleChrome(outgoing = false) {
                CometChatAudiosBubble(
                    message = mediaMessage(
                        CometChatConstants.MESSAGE_TYPE_AUDIO,
                        listOf(
                            attachment("track_1.mp3", "audio/mpeg"),
                            attachment("track_2.mp3", "audio/mpeg"),
                            attachment("track_3.mp3", "audio/mpeg")
                        )
                    ),
                    alignment = UIKitConstants.MessageBubbleAlignment.LEFT
                )
            }
        }
    }

    @Test
    fun audiosBubble_outgoing() {
        captureBubble {
            BubbleChrome(outgoing = true) {
                CometChatAudiosBubble(
                    message = mediaMessage(
                        CometChatConstants.MESSAGE_TYPE_AUDIO,
                        listOf(
                            attachment("track_1.mp3", "audio/mpeg"),
                            attachment("track_2.mp3", "audio/mpeg")
                        )
                    ),
                    alignment = UIKitConstants.MessageBubbleAlignment.RIGHT
                )
            }
        }
    }

    // ==================== Composer attachment tray ====================

    @Test
    fun attachmentTray_uploadStates() {
        captureBubble {
            CometChatAttachmentTray(
                tiles = listOf(
                    tile("f1", "photo.jpg", "image/jpeg", AttachmentUploadStatus.DONE),
                    tile("f2", "clip.mp4", "video/mp4", AttachmentUploadStatus.UPLOADING, percent = 45),
                    tile("f3", "report.pdf", "application/pdf", AttachmentUploadStatus.REJECTED),
                    tile("f4", "archive.zip", "application/zip", AttachmentUploadStatus.FAILED),
                    tile("f5", "song.mp3", "audio/mpeg", AttachmentUploadStatus.DONE)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    @Test
    fun attachmentTray_documentCards() {
        captureBubble {
            CometChatAttachmentTray(
                tiles = listOf(
                    tile("f1", "report.pdf", "application/pdf", AttachmentUploadStatus.DONE),
                    tile("f2", "notes.docx", "application/msword", AttachmentUploadStatus.UPLOADING, percent = 60),
                    tile("f3", "data.xlsx", "application/vnd.ms-excel", AttachmentUploadStatus.FAILED)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // ==================== Dark mode ====================

    @Test
    fun filesBubble_collapsed_darkMode() {
        captureBubble(dark = true) {
            BubbleChrome(outgoing = false) {
                CometChatFilesBubble(
                    message = mediaMessage(
                        CometChatConstants.MESSAGE_TYPE_FILE,
                        listOf(
                            attachment("report.pdf", "application/pdf"),
                            attachment("notes.docx", "application/msword"),
                            attachment("data.xlsx", "application/vnd.ms-excel"),
                            attachment("slides.pptx", "application/vnd.ms-powerpoint")
                        )
                    ),
                    alignment = UIKitConstants.MessageBubbleAlignment.LEFT
                )
            }
        }
    }

    @Test
    fun imagesGrid_sixImages_darkMode() {
        captureBubble(dark = true) {
            BubbleChrome(outgoing = false) {
                CometChatImagesBubble(
                    message = mediaMessage(CometChatConstants.MESSAGE_TYPE_IMAGE, imageAttachments(6)),
                    alignment = UIKitConstants.MessageBubbleAlignment.LEFT
                )
            }
        }
    }
}
