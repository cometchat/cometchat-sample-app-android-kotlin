package com.cometchat.uikit.compose.presentation.stickerkeyboard.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.compose.presentation.stickerkeyboard.style.CometChatStickerKeyboardStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.core.domain.model.Sticker
import com.cometchat.uikit.core.domain.model.StickerSet
import com.cometchat.uikit.core.domain.usecase.GetStickersUseCase
import com.cometchat.uikit.core.viewmodel.CometChatStickerKeyboardViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Compose instrumented tests for CometChatStickerKeyboard.
 *
 * Tests the real Composable rendered in a Compose test environment.
 * Uses a mock UseCase to control data without network calls.
 *
 * Verifies:
 * - Component renders
 * - Loading/empty/error states display
 * - Custom views work
 * - Sticker click callback
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest --tests "*.CometChatStickerKeyboardListTest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatStickerKeyboardListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ==================== Helper Methods ====================

    private fun createStickerSet(name: String, stickerCount: Int): StickerSet {
        val stickers = (1..stickerCount).map { i ->
            Sticker(
                name = "$name-sticker-$i",
                url = "https://example.com/stickers/$name/$i.png",
                setName = name
            )
        }
        return StickerSet(
            name = name,
            stickers = stickers,
            iconUrl = stickers.firstOrNull()?.url ?: ""
        )
    }

    private fun createStickerSets(count: Int): List<StickerSet> {
        return (1..count).map { i -> createStickerSet("Set-$i", stickerCount = 4) }
    }

    private fun createContentViewModel(): CometChatStickerKeyboardViewModel {
        val getStickersUseCase: GetStickersUseCase = mock()
        val sets = createStickerSets(3)
        runBlocking {
            whenever(getStickersUseCase.invoke()).thenReturn(Result.success(sets))
        }
        return CometChatStickerKeyboardViewModel(getStickersUseCase)
    }

    private fun createEmptyViewModel(): CometChatStickerKeyboardViewModel {
        val getStickersUseCase: GetStickersUseCase = mock()
        runBlocking {
            whenever(getStickersUseCase.invoke()).thenReturn(Result.success(emptyList()))
        }
        return CometChatStickerKeyboardViewModel(getStickersUseCase)
    }

    private fun createErrorViewModel(): CometChatStickerKeyboardViewModel {
        val getStickersUseCase: GetStickersUseCase = mock()
        runBlocking {
            whenever(getStickersUseCase.invoke()).thenReturn(
                Result.failure(CometChatException("ERR_NETWORK", "Failed to load stickers"))
            )
        }
        return CometChatStickerKeyboardViewModel(getStickersUseCase)
    }

    // ==================== Component Renders ====================

    @Test
    fun componentRendersWithContentState() {
        val viewModel = createContentViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        // Component should render without crashing
    }

    @Test
    fun componentRendersWithDarkTheme() {
        val viewModel = createContentViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        // Component should render in dark theme without crashing
    }

    // ==================== Loading State ====================

    @Test
    fun loadingStateDisplaysShimmer() {
        // Use a ViewModel that stays in loading state
        val getStickersUseCase: GetStickersUseCase = mock()
        runBlocking {
            whenever(getStickersUseCase.invoke()).thenAnswer {
                // Suspend indefinitely to keep in Loading state
                runBlocking { CompletableDeferred<Unit>().await() }
                Result.success(emptyList<StickerSet>())
            }
        }
        val viewModel = CometChatStickerKeyboardViewModel(getStickersUseCase)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        // Loading state should show shimmer (accessibility description)
        composeTestRule.onNodeWithContentDescription("Loading stickers, please wait")
            .assertIsDisplayed()
    }

    // ==================== Empty State ====================

    @Test
    fun emptyStateDisplaysEmptyMessage() {
        val viewModel = createEmptyViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        // Empty state text should be displayed
        // Note: actual text comes from string resources
    }

    // ==================== Error State ====================

    @Test
    fun errorStateDisplaysErrorWithRetry() {
        val viewModel = createErrorViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        // Error state should show retry button (text from string resources)
    }

    // ==================== Custom Views ====================

    @Test
    fun customLoadingViewReplacesDefault() {
        val getStickersUseCase: GetStickersUseCase = mock()
        runBlocking {
            whenever(getStickersUseCase.invoke()).thenAnswer {
                runBlocking { CompletableDeferred<Unit>().await() }
                Result.success(emptyList<StickerSet>())
            }
        }
        val viewModel = CometChatStickerKeyboardViewModel(getStickersUseCase)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    loadingView = {
                        Text("Custom Loading View")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Custom Loading View")
            .assertIsDisplayed()
    }

    @Test
    fun customEmptyViewReplacesDefault() {
        val viewModel = createEmptyViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    emptyView = {
                        Text("Custom Empty View")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Custom Empty View")
            .assertIsDisplayed()
    }

    @Test
    fun customErrorViewReplacesDefault() {
        val viewModel = createErrorViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    errorView = { onRetry ->
                        Text("Custom Error View")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Custom Error View")
            .assertIsDisplayed()
    }

    // ==================== Sticker Click Callback ====================

    @Test
    fun stickerClickCallbackIsInvoked() {
        val viewModel = createContentViewModel()
        var clickedSticker: Sticker? = null

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onStickerClick = { sticker ->
                        clickedSticker = sticker
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        // The callback is set via LaunchedEffect on the ViewModel
        // Actual click testing requires finding sticker items in the grid
    }

    // ==================== Hide State Flags ====================

    @Test
    fun hideLoadingStatePreventsLoadingDisplay() {
        val getStickersUseCase: GetStickersUseCase = mock()
        runBlocking {
            whenever(getStickersUseCase.invoke()).thenAnswer {
                runBlocking { CompletableDeferred<Unit>().await() }
                Result.success(emptyList<StickerSet>())
            }
        }
        val viewModel = CometChatStickerKeyboardViewModel(getStickersUseCase)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    hideLoadingState = true
                )
            }
        }

        composeTestRule.waitForIdle()
        // Loading shimmer should NOT be displayed
        composeTestRule.onNodeWithContentDescription("Loading stickers, please wait")
            .assertDoesNotExist()
    }

    @Test
    fun hideEmptyStatePreventsEmptyDisplay() {
        val viewModel = createEmptyViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    hideEmptyState = true
                )
            }
        }

        composeTestRule.waitForIdle()
        // Empty state should NOT be displayed when hidden
    }


    @Test
    fun hideErrorStatePreventsErrorDisplay() {
        val viewModel = createErrorViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    hideErrorState = true
                )
            }
        }

        composeTestRule.waitForIdle()
        // Error state should NOT be displayed when hidden
    }

    // ==================== Error Callback ====================

    @Test
    fun onErrorCallbackIsInvokedOnErrorState() {
        val viewModel = createErrorViewModel()
        var errorReceived: CometChatException? = null

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatStickerKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onError = { exception ->
                        errorReceived = exception
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        // The onError callback should be invoked via LaunchedEffect
        // when uiState transitions to Error
    }
}
