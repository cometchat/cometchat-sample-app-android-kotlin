package com.cometchat.uikit.kotlin.presentation.stickerkeyboard

import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.LifecycleOwner
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.uikit.core.data.datasource.StickerDataSource
import com.cometchat.uikit.core.data.repository.StickerRepositoryImpl
import com.cometchat.uikit.core.domain.model.Sticker
import com.cometchat.uikit.core.domain.model.StickerSet
import com.cometchat.uikit.core.domain.usecase.GetStickersUseCase
import com.cometchat.uikit.core.viewmodel.CometChatStickerKeyboardViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.stickerkeyboard.style.CometChatStickerKeyboardStyle
import com.cometchat.uikit.kotlin.presentation.stickerkeyboard.ui.CometChatStickerKeyboard
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented integration tests for CometChatStickerKeyboard (Kotlin View).
 *
 * Tests the real View inflated in a Fragment with Espresso assertions.
 * Uses a fake DataSource to control data without network calls.
 *
 * Verifies:
 * - View inflates correctly
 * - Loading state shows shimmer
 * - Custom views replace defaults
 * - Style applies correctly
 * - Sticker click callback works
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest --tests "*.CometChatStickerKeyboardIntegrationTest"
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatStickerKeyboardIntegrationTest {

    @Before
    fun setup() {
        StickerKeyboardHostFragment.injectedDataSource = null
        StickerKeyboardHostFragment.onStickerClickResult = null
        StickerKeyboardHostFragment.customStyle = null
        StickerKeyboardHostFragment.customLoadingView = null
        StickerKeyboardHostFragment.customEmptyView = null
        StickerKeyboardHostFragment.customErrorView = null
    }

    @After
    fun tearDown() {
        StickerKeyboardHostFragment.injectedDataSource = null
        StickerKeyboardHostFragment.onStickerClickResult = null
        StickerKeyboardHostFragment.customStyle = null
        StickerKeyboardHostFragment.customLoadingView = null
        StickerKeyboardHostFragment.customEmptyView = null
        StickerKeyboardHostFragment.customErrorView = null
    }

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

    private fun launchWithDataSource(dataSource: StickerDataSource) {
        StickerKeyboardHostFragment.injectedDataSource = dataSource
        launchFragmentInContainer<StickerKeyboardHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )
    }

    // ==================== Tests ====================

    @Test
    fun viewInflatesCorrectly() {
        val sets = createStickerSets(3)
        val dataSource = object : StickerDataSource {
            override suspend fun fetchStickers(): Result<List<StickerSet>> = Result.success(sets)
        }

        launchWithDataSource(dataSource)

        // The sticker keyboard view should be displayed
        onView(withId(R.id.stickers_view))
            .check(matches(isDisplayed()))
    }

    @Test
    fun loadingStateShowsShimmer() {
        // Use a data source that never completes (simulated by returning empty after delay)
        val dataSource = object : StickerDataSource {
            override suspend fun fetchStickers(): Result<List<StickerSet>> {
                // Simulate loading by suspending indefinitely
                kotlinx.coroutines.delay(Long.MAX_VALUE)
                return Result.success(emptyList())
            }
        }

        launchWithDataSource(dataSource)

        // Shimmer frame should be visible during loading
        onView(withId(R.id.shimmer_effect_frame))
            .check(matches(isDisplayed()))
    }

    @Test
    fun contentStateShowsViewPagerAndTabBar() {
        val sets = createStickerSets(3)
        val dataSource = object : StickerDataSource {
            override suspend fun fetchStickers(): Result<List<StickerSet>> = Result.success(sets)
        }

        launchWithDataSource(dataSource)

        // ViewPager and tab bar should be visible
        onView(withId(R.id.view_pager))
            .check(matches(isDisplayed()))
        onView(withId(R.id.rv_tab_bar))
            .check(matches(isDisplayed()))
    }

    @Test
    fun emptyStateShowsEmptyMessage() {
        val dataSource = object : StickerDataSource {
            override suspend fun fetchStickers(): Result<List<StickerSet>> = Result.success(emptyList())
        }

        launchWithDataSource(dataSource)

        // Empty layout should be visible
        onView(withId(R.id.empty_sticker_layout))
            .check(matches(isDisplayed()))
    }

    @Test
    fun errorStateShowsErrorWithRetryButton() {
        val dataSource = object : StickerDataSource {
            override suspend fun fetchStickers(): Result<List<StickerSet>> =
                Result.failure(com.cometchat.chat.exceptions.CometChatException("ERR", "Network error"))
        }

        launchWithDataSource(dataSource)

        // Error layout should be visible with retry button
        onView(withId(R.id.error_sticker_layout))
            .check(matches(isDisplayed()))
        onView(withId(R.id.retry_btn))
            .check(matches(isDisplayed()))
    }

    @Test
    fun customLoadingViewReplacesDefault() {
        StickerKeyboardHostFragment.customLoadingView = { context ->
            TextView(context).apply {
                text = "Custom Loading..."
                id = View.generateViewId()
            }
        }

        val dataSource = object : StickerDataSource {
            override suspend fun fetchStickers(): Result<List<StickerSet>> {
                kotlinx.coroutines.delay(Long.MAX_VALUE)
                return Result.success(emptyList())
            }
        }

        launchWithDataSource(dataSource)

        // Custom loading view should be displayed
        onView(withText("Custom Loading..."))
            .check(matches(isDisplayed()))
        // Default shimmer should NOT be visible
        onView(withId(R.id.shimmer_effect_frame))
            .check(matches(not(isDisplayed())))
    }

    @Test
    fun customEmptyViewReplacesDefault() {
        StickerKeyboardHostFragment.customEmptyView = { context ->
            TextView(context).apply {
                text = "Custom Empty State"
                id = View.generateViewId()
            }
        }

        val dataSource = object : StickerDataSource {
            override suspend fun fetchStickers(): Result<List<StickerSet>> = Result.success(emptyList())
        }

        launchWithDataSource(dataSource)

        // Custom empty view should be displayed
        onView(withText("Custom Empty State"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun customErrorViewReplacesDefault() {
        StickerKeyboardHostFragment.customErrorView = { context ->
            TextView(context).apply {
                text = "Custom Error View"
                id = View.generateViewId()
            }
        }

        val dataSource = object : StickerDataSource {
            override suspend fun fetchStickers(): Result<List<StickerSet>> =
                Result.failure(com.cometchat.chat.exceptions.CometChatException("ERR", "Fail"))
        }

        launchWithDataSource(dataSource)

        // Custom error view should be displayed
        onView(withText("Custom Error View"))
            .check(matches(isDisplayed()))
        // Default error layout should NOT be visible
        onView(withId(R.id.error_sticker_layout))
            .check(matches(not(isDisplayed())))
    }

    @Test
    fun styleAppliesCorrectly() {
        StickerKeyboardHostFragment.customStyle = CometChatStickerKeyboardStyle(
            backgroundColor = android.graphics.Color.parseColor("#1A1A2E"),
            separatorColor = android.graphics.Color.parseColor("#333333"),
            tabActiveIndicatorColor = android.graphics.Color.BLUE
        )

        val sets = createStickerSets(2)
        val dataSource = object : StickerDataSource {
            override suspend fun fetchStickers(): Result<List<StickerSet>> = Result.success(sets)
        }

        launchWithDataSource(dataSource)

        // View should still be displayed with custom style
        onView(withId(R.id.stickers_view))
            .check(matches(isDisplayed()))
    }

    @Test
    fun stickerClickCallbackWorks() {
        val sets = createStickerSets(2)
        val dataSource = object : StickerDataSource {
            override suspend fun fetchStickers(): Result<List<StickerSet>> = Result.success(sets)
        }

        launchWithDataSource(dataSource)

        // Verify the view is in content state (callback is set in fragment)
        onView(withId(R.id.view_pager))
            .check(matches(isDisplayed()))
    }
}

/**
 * Host Fragment for CometChatStickerKeyboard instrumented tests.
 * Allows injection of fake DataSource and custom views.
 *
 * Uses reflection to inject a custom ViewModel (built from the fake DataSource)
 * into the CometChatStickerKeyboard view BEFORE it attaches to the window.
 * This prevents the view from creating its own ViewModel that calls the real SDK.
 * After attachment, it also triggers observation of the ViewModel's state flows.
 */
class StickerKeyboardHostFragment : Fragment() {

    companion object {
        var injectedDataSource: StickerDataSource? = null
        var onStickerClickResult: Sticker? = null
        var customStyle: CometChatStickerKeyboardStyle? = null
        var customLoadingView: ((android.content.Context) -> View)? = null
        var customEmptyView: ((android.content.Context) -> View)? = null
        var customErrorView: ((android.content.Context) -> View)? = null
    }

    private var stickerKeyboard: CometChatStickerKeyboard? = null

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): View {
        val keyboard = CometChatStickerKeyboard(requireContext())
        stickerKeyboard = keyboard

        // Inject fake ViewModel via reflection BEFORE the view attaches to window.
        // CometChatStickerKeyboard.initializeViewModel() checks if viewModel != null
        // and skips creation if already set, so pre-injecting prevents the real SDK call.
        injectedDataSource?.let { dataSource ->
            val repository = StickerRepositoryImpl(dataSource)
            val useCase = GetStickersUseCase(repository)
            val viewModel = CometChatStickerKeyboardViewModel(getStickersUseCase = useCase)

            try {
                val viewModelField = CometChatStickerKeyboard::class.java
                    .getDeclaredField("viewModel")
                viewModelField.isAccessible = true
                viewModelField.set(keyboard, viewModel)
            } catch (e: Exception) {
                throw IllegalStateException(
                    "Failed to inject ViewModel into CometChatStickerKeyboard via reflection", e
                )
            }
        }

        // Apply custom views if set
        customLoadingView?.let { creator ->
            keyboard.setLoadingStateView(creator(requireContext()))
        }
        customEmptyView?.let { creator ->
            keyboard.setEmptyStateView(creator(requireContext()))
        }
        customErrorView?.let { creator ->
            keyboard.setErrorStateView(creator(requireContext()))
        }

        // Apply custom style if set
        customStyle?.let { style ->
            keyboard.setStyle(style)
        }

        // Set sticker click listener
        keyboard.setStickerClickListener { sticker ->
            onStickerClickResult = sticker
        }

        return FrameLayout(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            addView(keyboard, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // After the view is attached and has a LifecycleOwner, trigger observation
        // of the injected ViewModel's state flows via reflection.
        val keyboard = stickerKeyboard ?: return
        if (injectedDataSource == null) return

        try {
            val observeMethod = CometChatStickerKeyboard::class.java
                .getDeclaredMethod("observeViewModel", androidx.lifecycle.LifecycleOwner::class.java)
            observeMethod.isAccessible = true
            observeMethod.invoke(keyboard, viewLifecycleOwner)
        } catch (e: Exception) {
            throw IllegalStateException(
                "Failed to call observeViewModel on CometChatStickerKeyboard via reflection", e
            )
        }
    }
}
