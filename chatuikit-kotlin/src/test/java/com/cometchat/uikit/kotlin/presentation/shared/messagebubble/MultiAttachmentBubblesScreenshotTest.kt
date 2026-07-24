package com.cometchat.uikit.kotlin.presentation.shared.messagebubble

import android.content.res.Configuration
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.audiosbubble.CometChatAudiosBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.filesbubble.CometChatFilesBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.imagesbubble.CometChatImagesBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.videosbubble.CometChatVideosBubble
import com.cometchat.uikit.kotlin.presentation.utils.RoborazziConfig
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.google.android.material.card.MaterialCardView
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * ENG-36737 — Layer 5 (VRT) golden screenshots for the Views per-type multi-attachment bubbles:
 * [CometChatImagesBubble] (grid rules 1/2/3/4/5+ with "+N" overflow), [CometChatVideosBubble]
 * (play badge), [CometChatFilesBubble] (card stack, collapse at 3), and [CometChatAudiosBubble]
 * (single = no card overlay, multi = stacked cards).
 *
 * Every capture wraps the content bubble in real bubble chrome — incoming = theme
 * `cometchatBackgroundColor1` aligned start, outgoing = theme `cometchatPrimaryColor` aligned
 * end — on an #EEEEEE canvas so bubble bounds are visible against the background.
 *
 * Remote media renders as the Glide placeholder under Robolectric (no network) — deterministic,
 * and exactly what makes grid geometry, overlays, captions, and card chrome comparable.
 *
 * Record:  ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*MultiAttachmentBubblesScreenshotTest"
 * Verify:  ./gradlew :chatuikit-kotlin:compareRoborazziDebug --tests "*MultiAttachmentBubblesScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class MultiAttachmentBubblesScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/multiattachmentbubbles"
        )
    )

    private companion object {
        /** Light canvas — #EEEEEE so light bubbles stay visible against the background. */
        const val CANVAS_LIGHT = 0xFFEEEEEE.toInt()

        /** Dark canvas for -night qualifiers, so dark bubbles stay visible too. */
        const val CANVAS_DARK = 0xFF121212.toInt()

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

    private fun fileAttachments() = listOf(
        attachment("report.pdf", "application/pdf"),
        attachment("notes.docx", "application/msword"),
        attachment("data.xlsx", "application/vnd.ms-excel"),
        attachment("slides.pptx", "application/vnd.ms-powerpoint"),
        attachment("archive.zip", "application/zip")
    )

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

    // ==================== Chrome + capture helpers ====================

    private fun resolveThemeColor(activity: ComponentActivity, attr: Int): Int {
        val value = TypedValue()
        return if (activity.theme.resolveAttribute(attr, value, true)) value.data else Color.WHITE
    }

    /**
     * Wraps a content bubble in message-bubble chrome: rounded card colored like the real bubble
     * (incoming = backgroundColor1 at start, outgoing = primaryColor at end), with the fixed
     * [TIME_LABEL] timestamp row underneath the content — same instant every recording.
     */
    private fun bubbleChrome(activity: ComponentActivity, outgoing: Boolean, content: View): View {
        // Exactly what CometChatMessageBubbleStyle.outgoing()/incoming() use as their default
        // bubble background: primary (cometchat_color_primary) vs neutral300 (#F5F5F5 in light).
        val bubbleColor =
            if (outgoing) CometChatTheme.getPrimaryColor(activity)
            else CometChatTheme.getNeutralColor300(activity)
        val density = activity.resources.displayMetrics.density
        val timeView = TextView(activity).apply {
            text = TIME_LABEL
            textSize = 11f
            gravity = Gravity.END
            setTextColor(
                if (outgoing) 0xE6FFFFFF.toInt()
                else resolveThemeColor(activity, R.attr.cometchatTextColorTertiary)
            )
            setPadding((8 * density).toInt(), (2 * density).toInt(), (8 * density).toInt(), (6 * density).toInt())
        }
        val column = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            addView(
                content,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
            addView(
                timeView,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }
        val card = MaterialCardView(activity).apply {
            radius = 16 * resources.displayMetrics.density
            cardElevation = 0f
            strokeWidth = 0
            setCardBackgroundColor(bubbleColor)
            addView(
                column,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
        return FrameLayout(activity).apply {
            addView(
                card,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    if (outgoing) Gravity.END else Gravity.START
                )
            )
        }
    }

    private fun launchAndCapture(
        outgoing: Boolean = false,
        configure: (ComponentActivity) -> View
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            val bubble = bubbleChrome(activity, outgoing, configure(activity))

            val isNight = (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
            val container = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(if (isNight) CANVAS_DARK else CANVAS_LIGHT)
                val pad = (16 * resources.displayMetrics.density).toInt()
                setPadding(pad, pad, pad, pad)
                addView(
                    bubble,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }
            activity.setContentView(container)
            ShadowLooper.idleMainLooper()

            val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.EXACTLY)
            container.measure(widthSpec, heightSpec)
            container.layout(0, 0, 1080, 2160)
            ShadowLooper.idleMainLooper()

            container.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== Images grid ====================

    @Test
    fun imagesGrid_singleImage_incoming() {
        launchAndCapture { activity ->
            CometChatImagesBubble(activity).apply {
                setMessage(mediaMessage(CometChatConstants.MESSAGE_TYPE_IMAGE, imageAttachments(1)))
            }
        }
    }

    @Test
    fun imagesGrid_twoImages_incoming() {
        launchAndCapture { activity ->
            CometChatImagesBubble(activity).apply {
                setMessage(mediaMessage(CometChatConstants.MESSAGE_TYPE_IMAGE, imageAttachments(2)))
            }
        }
    }

    @Test
    fun imagesGrid_threeImages_incoming() {
        launchAndCapture { activity ->
            CometChatImagesBubble(activity).apply {
                setMessage(mediaMessage(CometChatConstants.MESSAGE_TYPE_IMAGE, imageAttachments(3)))
            }
        }
    }

    @Test
    fun imagesGrid_fourImages_incoming() {
        launchAndCapture { activity ->
            CometChatImagesBubble(activity).apply {
                setMessage(mediaMessage(CometChatConstants.MESSAGE_TYPE_IMAGE, imageAttachments(4)))
            }
        }
    }

    @Test
    fun imagesGrid_sixImages_showsPlusNOverflow_incoming() {
        launchAndCapture { activity ->
            CometChatImagesBubble(activity).apply {
                setMessage(mediaMessage(CometChatConstants.MESSAGE_TYPE_IMAGE, imageAttachments(6)))
            }
        }
    }

    @Test
    fun imagesGrid_withCaption_outgoing() {
        launchAndCapture(outgoing = true) { activity ->
            CometChatImagesBubble(activity).apply {
                setMessage(
                    mediaMessage(
                        CometChatConstants.MESSAGE_TYPE_IMAGE,
                        imageAttachments(2),
                        caption = "Weekend trip photos"
                    )
                )
            }
        }
    }

    // ==================== Videos grid ====================

    @Test
    fun videosGrid_twoVideos_showsPlayBadges_incoming() {
        launchAndCapture { activity ->
            CometChatVideosBubble(activity).apply {
                setMessage(
                    mediaMessage(
                        CometChatConstants.MESSAGE_TYPE_VIDEO,
                        listOf(
                            attachment("clip_1.mp4", "video/mp4"),
                            attachment("clip_2.mp4", "video/mp4")
                        )
                    )
                )
            }
        }
    }

    // ==================== Files card stack ====================

    @Test
    fun filesBubble_singleFile_incoming() {
        launchAndCapture { activity ->
            CometChatFilesBubble(activity).apply {
                setOutgoing(false)
                setMessage(
                    mediaMessage(
                        CometChatConstants.MESSAGE_TYPE_FILE,
                        listOf(attachment("report.pdf", "application/pdf"))
                    )
                )
            }
        }
    }

    @Test
    fun filesBubble_fiveFiles_collapsed_incoming() {
        launchAndCapture { activity ->
            CometChatFilesBubble(activity).apply {
                setOutgoing(false)
                setMessage(mediaMessage(CometChatConstants.MESSAGE_TYPE_FILE, fileAttachments()))
            }
        }
    }

    @Test
    fun filesBubble_twoFiles_outgoing() {
        launchAndCapture(outgoing = true) { activity ->
            CometChatFilesBubble(activity).apply {
                setOutgoing(true)
                setMessage(
                    mediaMessage(
                        CometChatConstants.MESSAGE_TYPE_FILE,
                        listOf(
                            attachment("report.pdf", "application/pdf"),
                            attachment("notes.docx", "application/msword")
                        ),
                        caption = "Q3 documents"
                    )
                )
            }
        }
    }

    // ==================== Audio card stack ====================

    @Test
    fun audiosBubble_singleAudio_noCardOverlay_incoming() {
        launchAndCapture { activity ->
            CometChatAudiosBubble(activity).apply {
                setOutgoing(false)
                setMessage(
                    mediaMessage(
                        CometChatConstants.MESSAGE_TYPE_AUDIO,
                        listOf(attachment("podcast.mp3", "audio/mpeg"))
                    )
                )
            }
        }
    }

    @Test
    fun audiosBubble_threeAudios_stackedCards_incoming() {
        launchAndCapture { activity ->
            CometChatAudiosBubble(activity).apply {
                setOutgoing(false)
                setMessage(
                    mediaMessage(
                        CometChatConstants.MESSAGE_TYPE_AUDIO,
                        listOf(
                            attachment("track_1.mp3", "audio/mpeg"),
                            attachment("track_2.mp3", "audio/mpeg"),
                            attachment("track_3.mp3", "audio/mpeg")
                        )
                    )
                )
            }
        }
    }

    @Test
    fun audiosBubble_twoAudios_outgoing() {
        launchAndCapture(outgoing = true) { activity ->
            CometChatAudiosBubble(activity).apply {
                setOutgoing(true)
                setMessage(
                    mediaMessage(
                        CometChatConstants.MESSAGE_TYPE_AUDIO,
                        listOf(
                            attachment("track_1.mp3", "audio/mpeg"),
                            attachment("track_2.mp3", "audio/mpeg")
                        )
                    )
                )
            }
        }
    }

    // ==================== Dark mode ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun filesBubble_collapsed_darkMode() {
        launchAndCapture { activity ->
            CometChatFilesBubble(activity).apply {
                setOutgoing(false)
                setMessage(
                    mediaMessage(CometChatConstants.MESSAGE_TYPE_FILE, fileAttachments().take(4))
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun imagesGrid_sixImages_darkMode() {
        launchAndCapture { activity ->
            CometChatImagesBubble(activity).apply {
                setMessage(mediaMessage(CometChatConstants.MESSAGE_TYPE_IMAGE, imageAttachments(6)))
            }
        }
    }
}
