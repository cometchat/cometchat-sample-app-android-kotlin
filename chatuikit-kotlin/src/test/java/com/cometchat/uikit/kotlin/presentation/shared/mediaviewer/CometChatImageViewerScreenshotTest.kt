package com.cometchat.uikit.kotlin.presentation.shared.mediaviewer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.kotlin.R
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.kotlin.presentation.utils.RoborazziConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/**
 * Roborazzi screenshot tests for CometChatImageViewer (chatuikit-kotlin).
 *
 * Renders simulated image viewer states with mock data to capture golden images.
 * The image viewer is a full-screen component with:
 * - Black background
 * - Image content area (ViewPager)
 * - Top toolbar with back button, title, and share icon
 * - Progress bar for loading state
 *
 * Since actual image loading from URLs doesn't work in Robolectric,
 * we simulate the visual states with placeholder image representations.
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*CometChatImageViewerScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatImageViewerScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/imageviewer"
        )
    )

    // ==================== Colors ====================

    private val BG_COLOR = Color.BLACK
    private val TOOLBAR_BG = Color.argb(180, 0, 0, 0) // Semi-transparent black
    private val ICON_COLOR = Color.WHITE
    private val TEXT_COLOR = Color.WHITE
    private val IMAGE_PLACEHOLDER_BG = Color.parseColor("#2C3E50") // Dark blue-gray
    private val IMAGE_PLACEHOLDER_BG_2 = Color.parseColor("#1ABC9C") // Teal
    private val IMAGE_PLACEHOLDER_BG_3 = Color.parseColor("#8E44AD") // Purple
    private val PROGRESS_COLOR = Color.WHITE

    // ==================== Mock Data ====================

    private data class MockImage(
        val url: String,
        val senderName: String,
        val timestamp: String,
        val placeholderColor: Int,
        val placeholderEmoji: String
    )

    private val mockImages = listOf(
        MockImage(
            url = "https://data-in.cc-cluster-2.io/2285624461a8f498/media/1706745600_photo.jpg",
            senderName = "Alice Smith",
            timestamp = "Jan 31, 2024 • 10:30 AM",
            placeholderColor = IMAGE_PLACEHOLDER_BG,
            placeholderEmoji = "🏔️"
        ),
        MockImage(
            url = "https://data-in.cc-cluster-2.io/2285624461a8f498/media/1706746200_screenshot.png",
            senderName = "Bob Johnson",
            timestamp = "Jan 31, 2024 • 10:45 AM",
            placeholderColor = IMAGE_PLACEHOLDER_BG_2,
            placeholderEmoji = "🌊"
        ),
        MockImage(
            url = "https://data-in.cc-cluster-2.io/2285624461a8f498/media/1706747000_design.png",
            senderName = "Charlie Brown",
            timestamp = "Jan 31, 2024 • 11:00 AM",
            placeholderColor = IMAGE_PLACEHOLDER_BG_3,
            placeholderEmoji = "🎨"
        )
    )

    // ==================== UI States ====================

    @Test
    fun stateLoading() {
        launchAndCapture { activity ->
            buildImageViewerLayout(activity) {
                addImagePlaceholder(activity, mockImages[0], showImage = false)
                addToolbar(activity, mockImages[0].senderName, mockImages[0].timestamp)
                addProgressBar(activity)
            }
        }
    }

    @Test
    fun stateContent() {
        launchAndCapture { activity ->
            buildImageViewerLayout(activity) {
                addImagePlaceholder(activity, mockImages[0], showImage = true)
                addToolbar(activity, mockImages[0].senderName, mockImages[0].timestamp)
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        launchAndCapture { activity ->
            buildImageViewerLayout(activity) {
                addImagePlaceholder(activity, mockImages[0], showImage = true)
                addToolbar(activity, mockImages[0].senderName, mockImages[0].timestamp)
            }
        }
    }

    @Test
    fun stateToolbarHidden() {
        launchAndCapture { activity ->
            buildImageViewerLayout(activity) {
                addImagePlaceholder(activity, mockImages[0], showImage = true)
                // No toolbar — simulates hidden state during pinch-to-zoom or tap-to-toggle
            }
        }
    }

    @Test
    fun stateMultipleImages() {
        launchAndCapture { activity ->
            buildImageViewerLayout(activity) {
                addImagePlaceholder(activity, mockImages[1], showImage = true)
                addToolbar(activity, mockImages[1].senderName, mockImages[1].timestamp)
                addPageIndicator(activity, currentPage = 2, totalPages = 3)
            }
        }
    }

    @Test
    fun stateImageWithCaption() {
        launchAndCapture { activity ->
            buildImageViewerLayout(activity) {
                addImagePlaceholder(activity, mockImages[2], showImage = true)
                addToolbar(activity, mockImages[2].senderName, mockImages[2].timestamp)
                addCaption(activity, "Check out this new design mockup for the chat screen! 🎨")
            }
        }
    }

    // ==================== Helper: Capture ====================

    private fun launchAndCapture(configure: (ComponentActivity) -> View) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            val view = configure(activity)
            activity.setContentView(view)
            ShadowLooper.idleMainLooper()

            val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.EXACTLY)
            view.measure(widthSpec, heightSpec)
            view.layout(0, 0, 1080, 2160)
            ShadowLooper.idleMainLooper()

            view.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== Layout Builder ====================

    private fun buildImageViewerLayout(
        activity: ComponentActivity,
        builder: FrameLayout.() -> Unit
    ): View {
        return FrameLayout(activity).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(BG_COLOR)
            builder()
        }
    }

    // ==================== Component Builders ====================

    private fun FrameLayout.addImagePlaceholder(
        activity: ComponentActivity,
        image: MockImage,
        showImage: Boolean
    ) {
        if (!showImage) return

        // Create a realistic-looking image using a programmatic Bitmap
        val imageView = ImageView(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageBitmap(createMockImageBitmap(image.placeholderColor))
        }
        addView(imageView)
    }

    /**
     * Creates a programmatic Bitmap that simulates a landscape/nature photo.
     * Uses gradients and shapes to create a visually interesting image placeholder.
     */
    private fun createMockImageBitmap(baseColor: Int): Bitmap {
        val width = 1080
        val height = 2160
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Sky gradient (top portion)
        val skyPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height * 0.6f,
                Color.parseColor("#1A237E"), // Deep blue
                Color.parseColor("#4FC3F7"), // Light blue
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height * 0.6f, skyPaint)

        // Mountain/landscape (middle portion)
        val mountainPaint = Paint().apply {
            color = baseColor
            isAntiAlias = true
        }
        val mountainPath = android.graphics.Path().apply {
            moveTo(0f, height * 0.45f)
            lineTo(width * 0.3f, height * 0.3f)
            lineTo(width * 0.5f, height * 0.4f)
            lineTo(width * 0.7f, height * 0.25f)
            lineTo(width.toFloat(), height * 0.4f)
            lineTo(width.toFloat(), height.toFloat())
            lineTo(0f, height.toFloat())
            close()
        }
        canvas.drawPath(mountainPath, mountainPaint)

        // Ground gradient (bottom portion)
        val groundPaint = Paint().apply {
            shader = LinearGradient(
                0f, height * 0.6f, 0f, height.toFloat(),
                Color.parseColor("#2E7D32"), // Green
                Color.parseColor("#1B5E20"), // Dark green
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, height * 0.65f, width.toFloat(), height.toFloat(), groundPaint)

        // Sun circle
        val sunPaint = Paint().apply {
            color = Color.parseColor("#FFF176")
            isAntiAlias = true
        }
        canvas.drawCircle(width * 0.75f, height * 0.15f, 80f, sunPaint)

        return bitmap
    }

    private fun FrameLayout.addToolbar(
        activity: ComponentActivity,
        senderName: String,
        timestamp: String
    ) {
        val toolbar = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(56)
            ).apply { gravity = Gravity.TOP }
            setBackgroundColor(TOOLBAR_BG)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(0), dp(8), dp(0))
        }

        // Back arrow
        val backButton = TextView(activity).apply {
            text = "←"
            textSize = 22f
            setTextColor(ICON_COLOR)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
        }
        toolbar.addView(backButton)

        // Title section
        val titleSection = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(8)
            }
        }

        val nameText = TextView(activity).apply {
            text = senderName
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(TEXT_COLOR)
        }
        titleSection.addView(nameText)

        val timeText = TextView(activity).apply {
            text = timestamp
            textSize = 12f
            setTextColor(Color.argb(180, 255, 255, 255))
        }
        titleSection.addView(timeText)

        toolbar.addView(titleSection)

        // Share button
        val shareButton = TextView(activity).apply {
            text = "⤴"
            textSize = 20f
            setTextColor(ICON_COLOR)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
        }
        toolbar.addView(shareButton)

        addView(toolbar)
    }

    private fun FrameLayout.addProgressBar(activity: ComponentActivity) {
        val progress = ProgressBar(activity).apply {
            layoutParams = FrameLayout.LayoutParams(dp(40), dp(40)).apply {
                gravity = Gravity.CENTER
            }
            isIndeterminate = true
            indeterminateTintList = android.content.res.ColorStateList.valueOf(PROGRESS_COLOR)
        }
        addView(progress)
    }

    private fun FrameLayout.addPageIndicator(
        activity: ComponentActivity,
        currentPage: Int,
        totalPages: Int
    ) {
        val indicator = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(24)
            }
            setPadding(dp(12), dp(6), dp(12), dp(6))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(12).toFloat()
                setColor(Color.argb(150, 0, 0, 0))
            }
        }

        for (i in 1..totalPages) {
            val dot = View(activity).apply {
                layoutParams = LinearLayout.LayoutParams(dp(8), dp(8)).apply {
                    marginStart = dp(4)
                    marginEnd = dp(4)
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(if (i == currentPage) Color.WHITE else Color.argb(100, 255, 255, 255))
                }
            }
            indicator.addView(dot)
        }

        addView(indicator)
    }

    private fun FrameLayout.addCaption(activity: ComponentActivity, caption: String) {
        val captionContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM
            }
            setBackgroundColor(Color.argb(180, 0, 0, 0))
            setPadding(dp(16), dp(12), dp(16), dp(16))
        }

        val captionText = TextView(activity).apply {
            text = caption
            textSize = 14f
            setTextColor(Color.WHITE)
        }
        captionContainer.addView(captionText)

        addView(captionContainer)
    }

    // ==================== Utility Methods ====================

    private fun dp(value: Int): Int = (value * 2.75f).toInt() // xxhdpi scale
}
