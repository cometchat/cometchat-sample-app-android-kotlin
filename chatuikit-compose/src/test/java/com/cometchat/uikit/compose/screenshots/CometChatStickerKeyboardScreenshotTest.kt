package com.cometchat.uikit.compose.screenshots

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil.Coil
import coil.ImageLoader
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult
import coil.decode.DataSource
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.compose.presentation.stickerkeyboard.style.CometChatStickerKeyboardStyle
import com.cometchat.uikit.compose.presentation.stickerkeyboard.ui.CometChatStickerKeyboard
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.domain.model.Sticker
import com.cometchat.uikit.core.domain.model.StickerSet
import com.cometchat.uikit.core.domain.usecase.GetStickersUseCase
import com.cometchat.uikit.core.viewmodel.CometChatStickerKeyboardViewModel
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.compose.presentation.utils.RoborazziConfig
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/**
 * Screenshot tests for CometChatStickerKeyboard (Compose).
 *
 * Uses the same mock data as chatuikit-kotlin's CometChatStickerKeyboardScreenshotTest
 * to maintain visual parity. Mock sticker data follows the CometChat sticker extension
 * customData format:
 * {"sticker_name": "gery", "sticker_url": "https://data-in.cc-cluster-2.io/stickers/gery/gery_1.png"}
 *
 * States captured:
 * - stateLoading / stateLoadingDark: Loading shimmer placeholder
 * - stateContent / stateContentDark: Sticker grid with tab bar
 * - stateContentSecondTab: Second sticker set selected
 * - stateEmpty / stateEmptyDark: No stickers available message
 * - stateError / stateErrorDark: Error with retry button
 * - styleCustomColors: Custom theming applied
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*CometChatStickerKeyboardScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatStickerKeyboardScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/stickerkeyboard"
        )
    )

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var getStickersUseCase: GetStickersUseCase

    // ==================== Mock Data (matching chatuikit-kotlin) ====================

    /**
     * Mock sticker sets matching the chatuikit-kotlin test data exactly.
     * Each set has 8 stickers with URLs following the CometChat sticker extension format.
     */
    private val mockStickerSets = listOf(
        StickerSet(
            name = "gery",
            stickers = (1..8).map { i ->
                Sticker(
                    name = "gery",
                    url = "https://data-in.cc-cluster-2.io/stickers/gery/gery_$i.png",
                    setName = "gery"
                )
            },
            iconUrl = "https://data-in.cc-cluster-2.io/stickers/gery/gery_1.png"
        ),
        StickerSet(
            name = "cool_monkey",
            stickers = (1..8).map { i ->
                Sticker(
                    name = "cool_monkey",
                    url = "https://data-in.cc-cluster-2.io/stickers/cool_monkey/cool_monkey_$i.png",
                    setName = "cool_monkey"
                )
            },
            iconUrl = "https://data-in.cc-cluster-2.io/stickers/cool_monkey/cool_monkey_1.png"
        ),
        StickerSet(
            name = "love",
            stickers = (1..8).map { i ->
                Sticker(
                    name = "love",
                    url = "https://data-in.cc-cluster-2.io/stickers/love/love_$i.png",
                    setName = "love"
                )
            },
            iconUrl = "https://data-in.cc-cluster-2.io/stickers/love/love_1.png"
        ),
        StickerSet(
            name = "funny",
            stickers = (1..8).map { i ->
                Sticker(
                    name = "funny",
                    url = "https://data-in.cc-cluster-2.io/stickers/funny/funny_$i.png",
                    setName = "funny"
                )
            },
            iconUrl = "https://data-in.cc-cluster-2.io/stickers/funny/funny_1.png"
        )
    )

    /**
     * Emoji representations for sticker placeholders matching chatuikit-kotlin.
     * Used to create visible placeholder bitmaps for the Coil interceptor.
     */
    private val stickerEmojis = mapOf(
        "gery" to listOf("😀", "😂", "🤣", "😊", "😇", "🥰", "😍", "🤩"),
        "cool_monkey" to listOf("🐵", "🙈", "🙉", "🙊", "🐒", "🦍", "🦧", "🐵"),
        "love" to listOf("❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍"),
        "funny" to listOf("😜", "🤪", "😝", "🤑", "🤗", "🤭", "🤫", "🤔")
    )

    // ==================== Setup ====================

    @Before
    fun setup() {
        getStickersUseCase = mock()

        // Setup fake image loader that returns visible sticker placeholder bitmaps
        // matching the chatuikit-kotlin approach of rendering emoji-based placeholders.
        //
        // Key: We use DataSource.MEMORY_CACHE which tells Coil to skip the crossfade
        // animation (crossfade is only applied for non-cached results). This ensures
        // SubcomposeAsyncImage renders the image immediately without animation delay.
        val context = RuntimeEnvironment.getApplication()
        val interceptor = object : Interceptor {
            override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
                val url = chain.request.data.toString()
                val emoji = getEmojiForUrl(url)
                val bitmap = createStickerPlaceholderBitmap(emoji)
                val drawable = BitmapDrawable(context.resources, bitmap)
                return SuccessResult(
                    drawable = drawable,
                    request = chain.request,
                    dataSource = DataSource.MEMORY_CACHE
                )
            }
        }
        val mainDispatcher = kotlinx.coroutines.Dispatchers.Main.immediate
        val imageLoader = ImageLoader.Builder(context)
            .components { add(interceptor) }
            .crossfade(false)
            .interceptorDispatcher(mainDispatcher)
            .fetcherDispatcher(mainDispatcher)
            .decoderDispatcher(mainDispatcher)
            .build()
        Coil.setImageLoader(imageLoader)
    }

    // ==================== Helper Methods ====================

    /**
     * Maps a sticker URL to its corresponding emoji placeholder.
     * Parses the URL to determine the sticker set and index.
     */
    private fun getEmojiForUrl(url: String): String {
        for ((setName, emojis) in stickerEmojis) {
            if (url.contains("/$setName/")) {
                // Extract index from URL like "gery_3.png" -> index 2
                val regex = Regex("${setName}_(\\d+)\\.png")
                val match = regex.find(url)
                val index = (match?.groupValues?.get(1)?.toIntOrNull() ?: 1) - 1
                return emojis.getOrElse(index) { emojis.first() }
            }
        }
        return "📦" // Default fallback
    }

    /**
     * Creates a placeholder bitmap with an emoji rendered on a light gray background.
     * Matches the visual style of chatuikit-kotlin's sticker grid cells.
     */
    private fun createStickerPlaceholderBitmap(emoji: String): Bitmap {
        val size = 108 // Match stickerItemSize
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background matching STICKER_PLACEHOLDER_BG from kotlin test
        val bgPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#F0F0F0")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

        // Emoji text centered
        val textPaint = Paint().apply {
            textSize = 48f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.DEFAULT
        }
        val xPos = size / 2f
        val yPos = (size / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(emoji, xPos, yPos, textPaint)

        return bitmap
    }

    private fun createContentViewModel(): CometChatStickerKeyboardViewModel {
        runBlocking {
            whenever(getStickersUseCase.invoke()).thenReturn(Result.success(mockStickerSets))
        }
        return CometChatStickerKeyboardViewModel(getStickersUseCase)
    }

    private fun createEmptyViewModel(): CometChatStickerKeyboardViewModel {
        runBlocking {
            whenever(getStickersUseCase.invoke()).thenReturn(Result.success(emptyList()))
        }
        return CometChatStickerKeyboardViewModel(getStickersUseCase)
    }

    private fun createErrorViewModel(): CometChatStickerKeyboardViewModel {
        runBlocking {
            whenever(getStickersUseCase.invoke()).thenReturn(
                Result.failure(CometChatException("ERR_NETWORK", "Failed to load stickers"))
            )
        }
        return CometChatStickerKeyboardViewModel(getStickersUseCase)
    }

    private fun captureComposable(content: @Composable () -> Unit) {
        composeTestRule.setContent { content() }

        // Wait for all pending compositions, coroutines, and image loading to complete.
        // SubcomposeAsyncImage uses internal coroutines that need multiple idle cycles.
        ShadowLooper.idleMainLooper()
        composeTestRule.waitForIdle()
        ShadowLooper.idleMainLooper()
        composeTestRule.waitForIdle()

        composeTestRule.onRoot().captureRoboImage(roborazziOptions = RoborazziConfig.options())
    }

    // ==================== Section 1: UI States ====================

    @Test
    fun stateLoading() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = null
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateLoadingDark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = null
                )
            }
        }
    }

    @Test
    fun stateContent() {
        val viewModel = createContentViewModel()
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        val viewModel = createContentViewModel()
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun stateContentSecondTab() {
        val viewModel = createContentViewModel()
        // Select the second tab (cool_monkey) matching kotlin test
        viewModel.selectStickerSet(1)
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun stateEmpty() {
        val viewModel = createEmptyViewModel()
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateEmptyDark() {
        val viewModel = createEmptyViewModel()
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun stateError() {
        val viewModel = createErrorViewModel()
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateErrorDark() {
        val viewModel = createErrorViewModel()
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }
    }

    // ==================== Section 2: Style Variants ====================

    @Test
    fun styleCustomColors() {
        val viewModel = createContentViewModel()
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                val customStyle = CometChatStickerKeyboardStyle.default(
                    backgroundColor = Color(0xFF1A1A2E),
                    separatorColor = Color(0xFF333333),
                    tabActiveIndicatorColor = Color(0xFF6851D6)
                )
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    style = customStyle
                )
            }
        }
    }

    // ==================== Section 3: Theme Variants ====================

    @Test
    fun theme_lightDefault() {
        val viewModel = createContentViewModel()
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun theme_darkDefault() {
        val viewModel = createContentViewModel()
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun theme_customColorScheme() {
        val viewModel = createContentViewModel()
        captureComposable {
            val customColors = lightColorScheme(
                primary = Color(0xFF6851D6)
            )
            CometChatTheme(colorScheme = customColors) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }
    }
}
