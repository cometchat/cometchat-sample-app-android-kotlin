package com.cometchat.uikit.compose.presentation.shared.baseelements

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.ErrorResult
import coil.request.ImageResult
import coil.request.SuccessResult
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.theme.CometChatTheme
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CometChatAvatarInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var originalImageLoader: ImageLoader? = null

    /**
     * Installs a Coil ImageLoader that forces all image loads to fail
     * by returning an ErrorResult. This triggers the onError callback
     * in AsyncImage, causing the avatar to fall back to displaying initials.
     */
    private fun installFailingImageLoader() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        originalImageLoader = Coil.imageLoader(context)
        val interceptor = object : Interceptor {
            override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
                return ErrorResult(
                    drawable = null,
                    request = chain.request,
                    throwable = RuntimeException("Simulated image load failure")
                )
            }
        }
        val imageLoader = ImageLoader.Builder(context)
            .components { add(interceptor) }
            .crossfade(false)
            .build()
        Coil.setImageLoader(imageLoader)
    }

    /**
     * Installs a Coil ImageLoader that returns a successful result with a
     * transparent drawable for all image requests.
     */
    private fun installSuccessImageLoader() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        originalImageLoader = Coil.imageLoader(context)
        val interceptor = object : Interceptor {
            override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
                return SuccessResult(
                    drawable = ColorDrawable(Color.TRANSPARENT),
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

    @Test
    fun displaysInitialsWhenNoAvatarUrlProvided() {
        composeTestRule.setContent {
            CometChatTheme {
                CometChatAvatar(name = "John Doe")
            }
        }
        composeTestRule.onNodeWithText("JD").assertIsDisplayed()
    }

    @Test
    fun displaysImageWhenAvatarUrlIsValid() {
        installSuccessImageLoader()

        composeTestRule.setContent {
            CometChatTheme {
                CometChatAvatar(
                    name = "John Doe",
                    avatarUrl = "https://example.com/avatar.jpg"
                )
            }
        }

        composeTestRule.waitForIdle()

        // When image URL loads successfully, initials should NOT be displayed
        // The image should be shown instead
        composeTestRule.onNodeWithText("JD").assertIsNotDisplayed()
    }

    @Test
    fun displaysInitialsWhenAvatarUrlFails() {
        installFailingImageLoader()

        composeTestRule.setContent {
            CometChatTheme {
                CometChatAvatar(
                    modifier = Modifier.size(48.dp),
                    name = "John Doe",
                    avatarUrl = "https://invalid-url-that-does-not-exist-xyz123.com/avatar.jpg"
                )
            }
        }

        // Wait until the image loading fails and initials become visible
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("JD").fetchSemanticsNodes().isNotEmpty()
        }

        // When image URL fails to load, initials should be displayed as fallback
        composeTestRule.onNodeWithText("JD").assertIsDisplayed()
    }

    @Test
    fun fallsBackToInitialsWhenImageFailsToLoad() {
        // Test with an empty/invalid URL to trigger fallback immediately
        composeTestRule.setContent {
            CometChatTheme {
                CometChatAvatar(
                    name = "Jane Smith",
                    avatarUrl = "" // Empty URL forces initials to show
                )
            }
        }

        composeTestRule.waitForIdle()

        // Fallback to initials when image URL is empty or invalid
        // For multi-word names, initials are first letter of each word: "JS"
        composeTestRule.onNodeWithText("JS").assertIsDisplayed()
    }

    @Test
    fun updatesDisplayedInitialsWhenNameChanges() {
        val nameState = mutableStateOf("John Doe")

        composeTestRule.setContent {
            CometChatTheme {
                CometChatAvatar(name = nameState.value)
            }
        }

        composeTestRule.onNodeWithText("JD").assertIsDisplayed()

        composeTestRule.runOnIdle {
            nameState.value = "Alice"
        }

        composeTestRule.waitForIdle()

        // For single-word names, initials are first two characters: "AL"
        composeTestRule.onNodeWithText("AL").assertIsDisplayed()
        composeTestRule.onNodeWithText("JD").assertIsNotDisplayed()
    }
}
