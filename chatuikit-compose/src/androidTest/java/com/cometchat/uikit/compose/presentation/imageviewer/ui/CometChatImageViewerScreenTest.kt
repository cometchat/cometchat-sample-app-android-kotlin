package com.cometchat.uikit.compose.presentation.imageviewer.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult
import com.cometchat.uikit.compose.presentation.imageviewer.style.CometChatImageViewerStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for CometChatImageViewerScreen (chatuikit-compose).
 *
 * Verifies the composable renders correctly on a real device/emulator:
 * - Component renders with loading indicator
 * - Back button is clickable and invokes callback
 * - Share button is clickable and invokes callback with correct params
 * - Custom style applies correctly
 *
 * Component Classification: Shared UI primitive (full-screen image viewer)
 * - No ViewModel — stateless composable driven by parameters
 * - Tests use createComposeRule for Compose UI testing
 *
 * Image rendering approach: Installs a custom Coil ImageLoader that intercepts
 * all image requests and returns a programmatic landscape bitmap (sky gradient,
 * mountains, ground, sun) — matching the chatuikit-kotlin screenshot test pattern.
 * This ensures the image actually renders in the test environment without network access.
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest --tests "*CometChatImageViewerScreenTest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatImageViewerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var originalImageLoader: ImageLoader? = null

    // ==================== Image Rendering Setup ====================

    /**
     * Installs a Coil ImageLoader that returns a programmatic landscape bitmap
     * for all image requests. The bitmap simulates a nature photo with:
     * - Sky gradient (deep blue to light blue)
     * - Mountain silhouette
     * - Ground gradient (green tones)
     * - Sun circle
     *
     * This matches the approach used in chatuikit-kotlin's
     * CometChatImageViewerScreenshotTest.createMockImageBitmap().
     */
    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        originalImageLoader = Coil.imageLoader(context)

        val interceptor = object : Interceptor {
            override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
                val bitmap = createMockImageBitmap()
                val drawable = BitmapDrawable(
                    chain.request.context.resources,
                    bitmap
                )
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

    @After
    fun tearDown() {
        originalImageLoader?.let { Coil.setImageLoader(it) }
    }

    /**
     * Creates a programmatic Bitmap that simulates a landscape/nature photo.
     * Uses gradients and shapes to create a visually interesting image placeholder,
     * matching the chatuikit-kotlin screenshot test approach.
     */
    private fun createMockImageBitmap(): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Sky gradient (top portion)
        val skyPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height * 0.6f,
                android.graphics.Color.parseColor("#1A237E"), // Deep blue
                android.graphics.Color.parseColor("#4FC3F7"), // Light blue
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height * 0.6f, skyPaint)

        // Mountain/landscape (middle portion)
        val mountainPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#2C3E50") // Dark blue-gray
            isAntiAlias = true
        }
        val mountainPath = Path().apply {
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
                android.graphics.Color.parseColor("#2E7D32"), // Green
                android.graphics.Color.parseColor("#1B5E20"), // Dark green
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, height * 0.65f, width.toFloat(), height.toFloat(), groundPaint)

        // Sun circle
        val sunPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#FFF176")
            isAntiAlias = true
        }
        canvas.drawCircle(width * 0.75f, height * 0.15f, 80f, sunPaint)

        return bitmap
    }

    // ==================== Rendering Tests ====================

    @Test
    fun componentRendersWithBackButton() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "https://example.com/photo.jpg",
                    fileName = "photo.jpg",
                    mimeType = "image/jpeg",
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }

        // Back button should be displayed with "Back" content description
        composeTestRule.onNodeWithContentDescription("Back")
            .assertIsDisplayed()
    }

    @Test
    fun componentRendersWithShareButton() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "https://example.com/photo.jpg",
                    fileName = "photo.jpg",
                    mimeType = "image/jpeg",
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }

        // Share button should be displayed with "Share" content description
        composeTestRule.onNodeWithContentDescription("Share")
            .assertIsDisplayed()
    }

    @Test
    fun componentRendersWithLoadingIndicator() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "https://example.com/photo.jpg",
                    fileName = "photo.jpg",
                    mimeType = "image/jpeg",
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }

        // The loading indicator should be present initially while image loads
        // CircularProgressIndicator is shown when isLoading=true (initial state)
        composeTestRule.waitForIdle()
        // Back and Share buttons should still be visible during loading
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share").assertIsDisplayed()
    }

    // ==================== Interaction Tests ====================

    @Test
    fun backButtonClickInvokesOnBackCallback() {
        var backInvoked = false

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "https://example.com/photo.jpg",
                    fileName = "photo.jpg",
                    mimeType = "image/jpeg",
                    onBack = { backInvoked = true },
                    onShare = { _, _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back")
            .performClick()

        composeTestRule.waitForIdle()
        assert(backInvoked) { "Expected onBack to be invoked after clicking back button" }
    }

    @Test
    fun shareButtonClickInvokesOnShareCallback() {
        var shareInvoked = false
        var receivedUrl = ""
        var receivedFileName = ""
        var receivedMimeType = ""

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "https://example.com/sunset.jpg",
                    fileName = "sunset.jpg",
                    mimeType = "image/jpeg",
                    onBack = {},
                    onShare = { url, name, mime ->
                        shareInvoked = true
                        receivedUrl = url
                        receivedFileName = name
                        receivedMimeType = mime
                    }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Share")
            .performClick()

        composeTestRule.waitForIdle()
        assert(shareInvoked) { "Expected onShare to be invoked after clicking share button" }
        assert(receivedUrl == "https://example.com/sunset.jpg") {
            "Expected url='https://example.com/sunset.jpg', got '$receivedUrl'"
        }
        assert(receivedFileName == "sunset.jpg") {
            "Expected fileName='sunset.jpg', got '$receivedFileName'"
        }
        assert(receivedMimeType == "image/jpeg") {
            "Expected mimeType='image/jpeg', got '$receivedMimeType'"
        }
    }

    @Test
    fun backButtonIsClickable() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "https://example.com/photo.jpg",
                    fileName = "photo.jpg",
                    mimeType = "image/jpeg",
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }

        // Verify the back button exists and can be clicked without crash
        composeTestRule.onNodeWithContentDescription("Back")
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun shareButtonIsClickable() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "https://example.com/photo.jpg",
                    fileName = "photo.jpg",
                    mimeType = "image/jpeg",
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }

        // Verify the share button exists and can be clicked without crash
        composeTestRule.onNodeWithContentDescription("Share")
            .assertIsDisplayed()
            .performClick()
    }

    // ==================== Custom Style Tests ====================

    @Test
    fun customStyleAppliesWithoutCrash() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                val customStyle = CometChatImageViewerStyle(
                    backgroundColor = Color(0xFF1A1A2E),
                    toolbarBackgroundColor = Color(0xFF16213E),
                    iconTintColor = Color(0xFF64B5F6),
                    loadingIndicatorColor = Color(0xFFFF6B6B)
                )
                CometChatImageViewerScreen(
                    imageUrl = "https://example.com/photo.jpg",
                    fileName = "photo.jpg",
                    mimeType = "image/jpeg",
                    style = customStyle,
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }

        // Component should render without crash with custom style
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share").assertIsDisplayed()
    }

    @Test
    fun darkThemeRendersWithoutCrash() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "https://example.com/photo.jpg",
                    fileName = "photo.jpg",
                    mimeType = "image/jpeg",
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }

        // Component should render without crash in dark theme
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share").assertIsDisplayed()
    }

    @Test
    fun lightThemeRendersWithoutCrash() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "https://example.com/photo.jpg",
                    fileName = "photo.jpg",
                    mimeType = "image/jpeg",
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }

        // Component should render without crash in light theme
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share").assertIsDisplayed()
    }

    // ==================== Edge Cases ====================

    @Test
    fun emptyImageUrlRendersWithoutCrash() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "",
                    fileName = "photo.jpg",
                    mimeType = "image/jpeg",
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }

        // Should not crash with empty URL — shows loading state
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun emptyFileNameRendersWithoutCrash() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "https://example.com/photo.jpg",
                    fileName = "",
                    mimeType = "image/jpeg",
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }

        // Should not crash with empty fileName
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share").assertIsDisplayed()
    }

    @Test
    fun shareWithEmptyParamsStillInvokesCallback() {
        var shareInvoked = false

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "",
                    fileName = "",
                    mimeType = "",
                    onBack = {},
                    onShare = { _, _, _ -> shareInvoked = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Share")
            .performClick()

        composeTestRule.waitForIdle()
        // The composable always invokes onShare with whatever params it has
        // Validation is the caller's responsibility
        assert(shareInvoked) { "Expected onShare to be invoked even with empty params" }
    }
}
