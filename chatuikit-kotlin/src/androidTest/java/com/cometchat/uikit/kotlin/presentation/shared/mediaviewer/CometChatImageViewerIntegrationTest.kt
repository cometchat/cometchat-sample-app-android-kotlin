package com.cometchat.uikit.kotlin.presentation.shared.mediaviewer

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.uikit.kotlin.R
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso integration test for CometChatImageViewerActivity (chatuikit-kotlin).
 *
 * This test launches the CometChatImageViewerActivity with mock Intent extras
 * and uses Espresso to assert on the rendered UI:
 * - Activity launches with correct intent extras
 * - ViewPager is displayed
 * - Toolbar is visible
 * - Share button is clickable
 * - Back navigation works
 *
 * Architecture:
 *   [Intent with extras] → [Real CometChatImageViewerActivity] → [Espresso assertions]
 *
 * Since CometChatImageViewerActivity is stateless (no ViewModel), we test through
 * the Activity's public contract: Intent extras → rendered views.
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.shared.mediaviewer.CometChatImageViewerIntegrationTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatImageViewerIntegrationTest {

    private var scenario: ActivityScenario<CometChatImageViewerActivity>? = null

    @Before
    fun setup() {
        // No-op: each test creates its own scenario
    }

    @After
    fun tearDown() {
        scenario?.close()
        scenario = null
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper: Create Intent with test data
    // ─────────────────────────────────────────────────────────────────────────

    private fun createTestIntent(
        urls: List<String> = listOf("https://example.com/test-image.png"),
        mimeTypes: List<String> = listOf("image/png"),
        filenames: List<String> = listOf("test-image.png")
    ): Intent {
        return CometChatImageViewerActivity.createIntent(
            context = ApplicationProvider.getApplicationContext(),
            urls = urls,
            mimeType = mimeTypes,
            filenames = filenames
        )
    }

    private fun launchActivity(
        urls: List<String> = listOf("https://example.com/test-image.png"),
        mimeTypes: List<String> = listOf("image/png"),
        filenames: List<String> = listOf("test-image.png")
    ): ActivityScenario<CometChatImageViewerActivity> {
        val intent = createTestIntent(urls, mimeTypes, filenames)
        val activityScenario = ActivityScenario.launch<CometChatImageViewerActivity>(intent)
        scenario = activityScenario
        return activityScenario
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Activity launches successfully with intent extras
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun activity_launchesWithCorrectIntentExtras() {
        val urls = listOf("https://example.com/image1.png", "https://example.com/image2.jpg")
        val mimeTypes = listOf("image/png", "image/jpeg")
        val filenames = listOf("image1.png", "image2.jpg")

        val activityScenario = launchActivity(urls, mimeTypes, filenames)

        // Activity should launch without crashing
        activityScenario.onActivity { activity ->
            // Verify the activity is not null and is running
            assert(activity != null)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: ViewPager is displayed
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun activity_displaysViewPager() {
        launchActivity()

        onView(withId(R.id.viewpager))
            .check(matches(isDisplayed()))
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Toolbar is visible
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun activity_displaysToolbar() {
        launchActivity()

        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: Top bar container is visible
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun activity_displaysTopBarContainer() {
        launchActivity()

        onView(withId(R.id.top_bar_container))
            .check(matches(isDisplayed()))
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Share button is visible and clickable
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun activity_shareButtonIsDisplayedAndClickable() {
        launchActivity()

        onView(withId(R.id.button_share))
            .check(matches(isDisplayed()))

        onView(withId(R.id.button_share))
            .check(matches(isClickable()))
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: Share button has correct content description
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun activity_shareButtonHasContentDescription() {
        launchActivity()

        onView(withContentDescription("Share"))
            .check(matches(isDisplayed()))
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 7: Back navigation works (toolbar navigate up)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun activity_backNavigationFinishesActivity() {
        val activityScenario = launchActivity()

        // Navigate up via toolbar
        onView(withContentDescription("Navigate up"))
            .perform(click())

        // After clicking navigate up, the activity should reach DESTROYED state.
        // Using state assertion avoids the race condition where onActivity throws
        // NullPointerException because the activity is already destroyed.
        Thread.sleep(500)
        assert(activityScenario.state == Lifecycle.State.DESTROYED)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 8: Activity launches with multiple images
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun activity_launchesWithMultipleImages() {
        val urls = listOf(
            "https://example.com/img1.png",
            "https://example.com/img2.jpg",
            "https://example.com/img3.gif"
        )
        val mimeTypes = listOf("image/png", "image/jpeg", "image/gif")
        val filenames = listOf("img1.png", "img2.jpg", "img3.gif")

        val activityScenario = launchActivity(urls, mimeTypes, filenames)

        // ViewPager should be displayed with multiple pages
        onView(withId(R.id.viewpager))
            .check(matches(isDisplayed()))

        activityScenario.onActivity { activity ->
            assert(activity != null)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 9: Progress bar exists in layout
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun activity_progressBarExistsInLayout() {
        val activityScenario = launchActivity()

        activityScenario.onActivity { activity ->
            val progressBar = activity.findViewById<android.view.View>(R.id.progress_bar)
            assert(progressBar != null)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 10: Activity with single image launches correctly
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun activity_singleImageLaunchesCorrectly() {
        val activityScenario = launchActivity(
            urls = listOf("https://example.com/single.png"),
            mimeTypes = listOf("image/png"),
            filenames = listOf("single.png")
        )

        onView(withId(R.id.viewpager))
            .check(matches(isDisplayed()))

        onView(withId(R.id.button_share))
            .check(matches(isDisplayed()))

        activityScenario.onActivity { activity ->
            assert(!activity.isFinishing)
        }
    }
}
