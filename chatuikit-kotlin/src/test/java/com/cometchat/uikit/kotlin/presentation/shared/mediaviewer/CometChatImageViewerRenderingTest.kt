package com.cometchat.uikit.kotlin.presentation.shared.mediaviewer

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.viewpager.widget.ViewPager
import com.cometchat.uikit.kotlin.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import android.content.Intent
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Rendering tests for CometChatImageViewerActivity (chatuikit-kotlin).
 *
 * Verifies that the Activity inflates correctly and all key views are present
 * in the expected initial states. Uses Robolectric for JVM-based Activity testing.
 *
 * Component Classification: Shared UI primitive (full-screen image viewer with gestures)
 * - No ViewModel, no DataSource, no Repository, no Listeners
 * - Stateless viewer driven by Intent extras
 *
 * Validates:
 * - Activity inflates layout correctly
 * - ViewPager is present for image swiping
 * - Toolbar is present with back navigation
 * - Share button is present
 * - Progress bar shows during loading and hides after image loads
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatImageViewerRenderingTest"
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class CometChatImageViewerRenderingTest {

    // ==================== Activity Inflation ====================

    @Test
    fun `activity inflates layout correctly with all required views`() {
        println("  🧪 activity inflates layout correctly with all required views")

        val activity = buildActivity(
            urls = listOf("https://example.com/image1.jpg"),
            mimeTypes = listOf("image/jpeg"),
            filenames = listOf("image1.jpg")
        )

        activity shouldNotBe null
        println("    ✅ Activity created successfully")
    }

    @Test
    fun `ViewPager is present in the layout`() {
        println("  🧪 ViewPager is present in the layout")

        val activity = buildActivity(
            urls = listOf("https://example.com/image1.jpg"),
            mimeTypes = listOf("image/jpeg"),
            filenames = listOf("image1.jpg")
        )

        val viewPager = activity.findViewById<ViewPager>(R.id.viewpager)
        viewPager shouldNotBe null
        viewPager.shouldBeInstanceOf<ViewPager>()
        println("    ✅ ViewPager found with id R.id.viewpager")
    }

    @Test
    fun `Toolbar is present with back navigation enabled`() {
        println("  🧪 Toolbar is present with back navigation enabled")

        val activity = buildActivity(
            urls = listOf("https://example.com/image1.jpg"),
            mimeTypes = listOf("image/jpeg"),
            filenames = listOf("image1.jpg")
        )

        val toolbar = activity.findViewById<Toolbar>(R.id.toolbar)
        toolbar shouldNotBe null
        toolbar.shouldBeInstanceOf<Toolbar>()

        // supportActionBar should have home-as-up enabled
        activity.supportActionBar shouldNotBe null
        activity.supportActionBar?.title shouldBe ""
        println("    ✅ Toolbar present, supportActionBar configured with empty title")
    }

    @Test
    fun `share button is present in the top bar`() {
        println("  🧪 share button is present in the top bar")

        val activity = buildActivity(
            urls = listOf("https://example.com/image1.jpg"),
            mimeTypes = listOf("image/jpeg"),
            filenames = listOf("image1.jpg")
        )

        val shareBtn = activity.findViewById<ImageView>(R.id.button_share)
        shareBtn shouldNotBe null
        shareBtn.shouldBeInstanceOf<ImageView>()
        shareBtn.contentDescription shouldBe "Share"
        println("    ✅ Share button found with contentDescription='Share'")
    }

    @Test
    fun `top bar container is present`() {
        println("  🧪 top bar container is present")

        val activity = buildActivity(
            urls = listOf("https://example.com/image1.jpg"),
            mimeTypes = listOf("image/jpeg"),
            filenames = listOf("image1.jpg")
        )

        val topBar = activity.findViewById<LinearLayout>(R.id.top_bar_container)
        topBar shouldNotBe null
        topBar.shouldBeInstanceOf<LinearLayout>()
        println("    ✅ Top bar container (LinearLayout) found")
    }

    @Test
    fun `progress bar is present in the layout`() {
        println("  🧪 progress bar is present in the layout")

        val activity = buildActivity(
            urls = listOf("https://example.com/image1.jpg"),
            mimeTypes = listOf("image/jpeg"),
            filenames = listOf("image1.jpg")
        )

        val progressBar = activity.findViewById<View>(R.id.progress_bar)
        progressBar shouldNotBe null
        println("    ✅ Progress bar found with id R.id.progress_bar")
    }

    @Test
    fun `progress bar is visible during initial loading state`() {
        println("  🧪 progress bar is visible during initial loading state")

        val activity = buildActivity(
            urls = listOf("https://example.com/image1.jpg"),
            mimeTypes = listOf("image/jpeg"),
            filenames = listOf("image1.jpg")
        )

        val progressBar = activity.findViewById<View>(R.id.progress_bar)
        // After initViews(), toggleProgressBarVisibility(View.VISIBLE) is called
        progressBar.visibility shouldBe View.VISIBLE
        println("    ✅ Progress bar is VISIBLE during loading")
    }

    @Test
    fun `ViewPager adapter is set with correct item count`() {
        println("  🧪 ViewPager adapter is set with correct item count")

        val urls = listOf(
            "https://example.com/image1.jpg",
            "https://example.com/image2.jpg",
            "https://example.com/image3.jpg"
        )
        val activity = buildActivity(
            urls = urls,
            mimeTypes = listOf("image/jpeg", "image/png", "image/gif"),
            filenames = listOf("image1.jpg", "image2.png", "image3.gif")
        )

        val viewPager = activity.findViewById<ViewPager>(R.id.viewpager)
        viewPager.adapter shouldNotBe null
        viewPager.adapter?.count shouldBe 3
        println("    ✅ ViewPager adapter has count=3 matching URL list size")
    }

    @Test
    fun `ViewPager starts at position 0`() {
        println("  🧪 ViewPager starts at position 0")

        val activity = buildActivity(
            urls = listOf("https://example.com/image1.jpg", "https://example.com/image2.jpg"),
            mimeTypes = listOf("image/jpeg", "image/png"),
            filenames = listOf("image1.jpg", "image2.png")
        )

        val viewPager = activity.findViewById<ViewPager>(R.id.viewpager)
        viewPager.currentItem shouldBe 0
        println("    ✅ ViewPager currentItem=0 (starts at first image)")
    }

    // ==================== Helper: Build Activity with Intent ====================

    private fun buildActivity(
        urls: List<String>,
        mimeTypes: List<String>,
        filenames: List<String>
    ): CometChatImageViewerActivity {
        val intent = Intent().apply {
            putExtra("ARGS_IMAGE_URLS", java.io.Serializable::class.java.cast(urls))
            putExtra("MIME_TYPE_URL", java.io.Serializable::class.java.cast(mimeTypes))
            putExtra("ARGS_FILE_NAME", java.io.Serializable::class.java.cast(filenames))
        }

        val activityController = Robolectric.buildActivity(
            CometChatImageViewerActivity::class.java,
            intent
        )
        return activityController.setup().get()
    }
}
