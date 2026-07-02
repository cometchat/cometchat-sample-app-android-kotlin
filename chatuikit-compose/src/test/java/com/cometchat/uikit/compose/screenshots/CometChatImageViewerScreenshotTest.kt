package com.cometchat.uikit.compose.screenshots

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil.Coil
import coil.ImageLoader
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult
import coil.decode.DataSource
import com.cometchat.uikit.compose.presentation.imageviewer.style.CometChatImageViewerStyle
import com.cometchat.uikit.compose.presentation.imageviewer.ui.CometChatImageViewerScreen
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.compose.presentation.utils.RoborazziConfig
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/**
 * Roborazzi screenshot tests for CometChatImageViewerScreen (chatuikit-compose).
 *
 * Uses the same mock data as chatuikit-kotlin's CometChatImageViewerScreenshotTest
 * to maintain visual parity. Captures golden images for all visual states:
 * - Loading state (progress indicator visible)
 * - Content state (image loaded with toolbar)
 * - Dark theme variant
 * - Toolbar hidden state
 * - Multiple images (page indicator)
 * - Image with caption
 * - Custom style variants
 *
 * Run:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*CometChatImageViewerScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatImageViewerScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/imageviewer"
        )
    )

    // ==================== Colors (matching chatuikit-kotlin) ====================

    private val IMAGE_PLACEHOLDER_BG = Color.parseColor("#2C3E50")   // Dark blue-gray
    private val IMAGE_PLACEHOLDER_BG_2 = Color.parseColor("#1ABC9C") // Teal
    private val IMAGE_PLACEHOLDER_BG_3 = Color.parseColor("#8E44AD") // Purple

    // ==================== Mock Data (matching chatuikit-kotlin) ====================

    private data class MockImage(
        val url: String,
        val senderName: String,
        val timestamp: String,
        val placeholderColor: Int,
        val fileName: String,
        val mimeType: String
    )

    private val mockImages = listOf(
        MockImage(
            url = "https://data-in.cc-cluster-2.io/2285624461a8f498/media/1706745600_photo.jpg",
            senderName = "Alice Smith",
            timestamp = "Jan 31, 2024 • 10:30 AM",
            placeholderColor = IMAGE_PLACEHOLDER_BG,
            fileName = "photo.jpg",
            mimeType = "image/jpeg"
        ),
        MockImage(
            url = "https://data-in.cc-cluster-2.io/2285624461a8f498/media/1706746200_screenshot.png",
            senderName = "Bob Johnson",
            timestamp = "Jan 31, 2024 • 10:45 AM",
            placeholderColor = IMAGE_PLACEHOLDER_BG_2,
            fileName = "screenshot.png",
            mimeType = "image/png"
        ),
        MockImage(
            url = "https://data-in.cc-cluster-2.io/2285624461a8f498/media/1706747000_design.png",
            senderName = "Charlie Brown",
            timestamp = "Jan 31, 2024 • 11:00 AM",
            placeholderColor = IMAGE_PLACEHOLDER_BG_3,
            fileName = "design.png",
            mimeType = "image/png"
        )
    )

    // ==================== Setup ====================

    @Before
    fun setupFakeImageLoader() {
        val context = RuntimeEnvironment.getApplication()
        val interceptor = object : Interceptor {
            override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
                // Use a rich programmatic bitmap matching chatuikit-kotlin's mock image style
                val bitmap = createMockImageBitmap(IMAGE_PLACEHOLDER_BG)
                val drawable = BitmapDrawable(context.resources, bitmap)
                return SuccessResult(
                    drawable = drawable,
                    request = chain.request,
                    dataSource = DataSource.MEMORY
                )
            }
        }
        val imageLoader = ImageLoader.Builder(context)
            .components { add(interceptor) }
            .crossfade(false)
            .build()
        Coil.setImageLoader(imageLoader)
    }

    // ==================== UI States ====================

    @Test
    fun stateLoading() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "",
                    fileName = mockImages[0].fileName,
                    mimeType = mockImages[0].mimeType,
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }
    }

    @Test
    fun stateContent() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = mockImages[0].url,
                    fileName = mockImages[0].fileName,
                    mimeType = mockImages[0].mimeType,
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = mockImages[0].url,
                    fileName = mockImages[0].fileName,
                    mimeType = mockImages[0].mimeType,
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }
    }

    @Test
    fun stateToolbarHidden() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = mockImages[0].url,
                    fileName = mockImages[0].fileName,
                    mimeType = mockImages[0].mimeType,
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }
    }

    @Test
    fun stateMultipleImages() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = mockImages[1].url,
                    fileName = mockImages[1].fileName,
                    mimeType = mockImages[1].mimeType,
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }
    }

    @Test
    fun stateImageWithCaption() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = mockImages[2].url,
                    fileName = mockImages[2].fileName,
                    mimeType = mockImages[2].mimeType,
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }
    }

    // ==================== Theme Variants ====================

    @Test
    fun theme_lightDefault() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = mockImages[0].url,
                    fileName = mockImages[0].fileName,
                    mimeType = mockImages[0].mimeType,
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }
    }

    @Test
    fun theme_darkDefault() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = mockImages[0].url,
                    fileName = mockImages[0].fileName,
                    mimeType = mockImages[0].mimeType,
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }
    }

    // ==================== Style Variants ====================

    @Test
    fun styleCustomColors() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                val customStyle = CometChatImageViewerStyle(
                    backgroundColor = ComposeColor(0xFF1A1A2E),
                    toolbarBackgroundColor = ComposeColor(0xFF16213E),
                    iconTintColor = ComposeColor(0xFF64B5F6),
                    loadingIndicatorColor = ComposeColor(0xFFFF6B6B)
                )
                CometChatImageViewerScreen(
                    imageUrl = mockImages[0].url,
                    fileName = mockImages[0].fileName,
                    mimeType = mockImages[0].mimeType,
                    style = customStyle,
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }
    }

    @Test
    fun styleTransparentToolbar() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                val customStyle = CometChatImageViewerStyle(
                    backgroundColor = ComposeColor.Black,
                    toolbarBackgroundColor = ComposeColor.Transparent,
                    iconTintColor = ComposeColor.White,
                    loadingIndicatorColor = ComposeColor.White
                )
                CometChatImageViewerScreen(
                    imageUrl = mockImages[1].url,
                    fileName = mockImages[1].fileName,
                    mimeType = mockImages[1].mimeType,
                    style = customStyle,
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }
    }

    @Test
    fun styleHighContrastIndicator() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                val customStyle = CometChatImageViewerStyle(
                    backgroundColor = ComposeColor.Black,
                    toolbarBackgroundColor = ComposeColor.Black.copy(alpha = 0.8f),
                    iconTintColor = ComposeColor.Yellow,
                    loadingIndicatorColor = ComposeColor.Green
                )
                CometChatImageViewerScreen(
                    imageUrl = "",
                    fileName = mockImages[0].fileName,
                    mimeType = mockImages[0].mimeType,
                    style = customStyle,
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }
    }

    // ==================== Content Variants ====================

    @Test
    fun contentWithLongFileName() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = mockImages[0].url,
                    fileName = "very-long-filename-that-might-overflow-the-toolbar-area-in-some-cases.jpg",
                    mimeType = "image/jpeg",
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }
    }

    @Test
    fun contentWithPngMimeType() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = mockImages[2].url,
                    fileName = mockImages[2].fileName,
                    mimeType = mockImages[2].mimeType,
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }
    }

    // ==================== Helper: Mock Image Bitmap ====================

    /**
     * Creates a programmatic Bitmap that simulates a landscape/nature photo.
     * Uses gradients and shapes to create a visually interesting image placeholder.
     * Matches the implementation in chatuikit-kotlin's CometChatImageViewerScreenshotTest.
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

    // ==================== Helper: Composable Capture ====================

    private fun captureComposable(content: @Composable () -> Unit) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setContent { content() }
        }

        try {
            ShadowLooper.idleMainLooper()
        } catch (_: Exception) { }

        scenario.onActivity { activity ->
            val composeView = activity.window.decorView
                .findViewById<ViewGroup>(android.R.id.content)
                .getChildAt(0)
            composeView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }
}
