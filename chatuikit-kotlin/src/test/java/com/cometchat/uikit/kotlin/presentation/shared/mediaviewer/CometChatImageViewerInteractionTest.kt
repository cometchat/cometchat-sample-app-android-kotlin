package com.cometchat.uikit.kotlin.presentation.shared.mediaviewer

import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.kotlin.R
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Interaction tests for CometChatImageViewerActivity (chatuikit-kotlin).
 *
 * Verifies user interactions:
 * - Back button (navigate up) finishes the activity
 * - Share button triggers share intent flow
 * - createIntent companion method builds correct Intent with extras
 *
 * Component Classification: Shared UI primitive (full-screen image viewer)
 * - No ViewModel — interactions are handled directly by the Activity
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatImageViewerInteractionTest"
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class CometChatImageViewerInteractionTest {

    // ==================== Back Button Interaction ====================

    @Test
    fun `onSupportNavigateUp finishes the activity`() {
        println("  🧪 onSupportNavigateUp finishes the activity")

        val activity = buildActivity(
            urls = listOf("https://example.com/image1.jpg"),
            mimeTypes = listOf("image/jpeg"),
            filenames = listOf("image1.jpg")
        )

        activity.onSupportNavigateUp()

        activity.isFinishing shouldBe true
        println("    ✅ Activity is finishing after onSupportNavigateUp()")
    }

    @Test
    fun `back pressed callback finishes the activity`() {
        println("  🧪 back pressed callback finishes the activity")

        val activity = buildActivity(
            urls = listOf("https://example.com/image1.jpg"),
            mimeTypes = listOf("image/jpeg"),
            filenames = listOf("image1.jpg")
        )

        activity.onBackPressedDispatcher.onBackPressed()

        activity.isFinishing shouldBe true
        println("    ✅ Activity is finishing after onBackPressed()")
    }

    // ==================== createIntent Companion Method ====================

    @Test
    fun `createIntent builds Intent with correct image URLs extra`() {
        println("  🧪 createIntent builds Intent with correct image URLs extra")

        val context: Context = RuntimeEnvironment.getApplication()
        val urls = listOf("https://example.com/img1.jpg", "https://example.com/img2.png")
        val mimeTypes = listOf("image/jpeg", "image/png")
        val filenames = listOf("img1.jpg", "img2.png")

        val intent = CometChatImageViewerActivity.createIntent(context, urls, mimeTypes, filenames)

        intent shouldNotBe null
        intent.component?.className shouldBe CometChatImageViewerActivity::class.java.name

        @Suppress("UNCHECKED_CAST")
        val extractedUrls = intent.getSerializableExtra("ARGS_IMAGE_URLS") as? List<String>
        extractedUrls shouldNotBe null
        extractedUrls shouldBe urls
        println("    ✅ Intent contains ARGS_IMAGE_URLS=$urls")
    }

    @Test
    fun `createIntent builds Intent with correct MIME type extra`() {
        println("  🧪 createIntent builds Intent with correct MIME type extra")

        val context: Context = RuntimeEnvironment.getApplication()
        val urls = listOf("https://example.com/img1.jpg")
        val mimeTypes = listOf("image/jpeg")
        val filenames = listOf("img1.jpg")

        val intent = CometChatImageViewerActivity.createIntent(context, urls, mimeTypes, filenames)

        @Suppress("UNCHECKED_CAST")
        val extractedMimeTypes = intent.getSerializableExtra("MIME_TYPE_URL") as? List<String>
        extractedMimeTypes shouldNotBe null
        extractedMimeTypes shouldBe mimeTypes
        println("    ✅ Intent contains MIME_TYPE_URL=$mimeTypes")
    }

    @Test
    fun `createIntent builds Intent with correct filenames extra`() {
        println("  🧪 createIntent builds Intent with correct filenames extra")

        val context: Context = RuntimeEnvironment.getApplication()
        val urls = listOf("https://example.com/img1.jpg")
        val mimeTypes = listOf("image/jpeg")
        val filenames = listOf("photo_2024.jpg")

        val intent = CometChatImageViewerActivity.createIntent(context, urls, mimeTypes, filenames)

        @Suppress("UNCHECKED_CAST")
        val extractedFilenames = intent.getSerializableExtra("ARGS_FILE_NAME") as? List<String>
        extractedFilenames shouldNotBe null
        extractedFilenames shouldBe filenames
        println("    ✅ Intent contains ARGS_FILE_NAME=$filenames")
    }

    @Test
    fun `createIntent with multiple images preserves order`() {
        println("  🧪 createIntent with multiple images preserves order")

        val context: Context = RuntimeEnvironment.getApplication()
        val urls = listOf(
            "https://example.com/first.jpg",
            "https://example.com/second.png",
            "https://example.com/third.gif"
        )
        val mimeTypes = listOf("image/jpeg", "image/png", "image/gif")
        val filenames = listOf("first.jpg", "second.png", "third.gif")

        val intent = CometChatImageViewerActivity.createIntent(context, urls, mimeTypes, filenames)

        @Suppress("UNCHECKED_CAST")
        val extractedUrls = intent.getSerializableExtra("ARGS_IMAGE_URLS") as? List<String>
        extractedUrls?.get(0) shouldBe "https://example.com/first.jpg"
        extractedUrls?.get(1) shouldBe "https://example.com/second.png"
        extractedUrls?.get(2) shouldBe "https://example.com/third.gif"
        println("    ✅ Intent preserves URL order: first, second, third")
    }

    @Test
    fun `createIntent with empty lists produces valid Intent`() {
        println("  🧪 createIntent with empty lists produces valid Intent")

        val context: Context = RuntimeEnvironment.getApplication()
        val intent = CometChatImageViewerActivity.createIntent(
            context,
            urls = emptyList(),
            mimeType = emptyList(),
            filenames = emptyList()
        )

        intent shouldNotBe null
        intent.component?.className shouldBe CometChatImageViewerActivity::class.java.name
        println("    ✅ Intent created successfully with empty lists")
    }

    @Test
    fun `share button click with null data does not crash`() {
        println("  🧪 share button click with null data does not crash")

        // Build activity with empty extras to simulate null data scenario
        val intent = Intent().apply {
            // Intentionally not setting extras
        }
        val activityController = Robolectric.buildActivity(
            CometChatImageViewerActivity::class.java,
            intent
        )
        val activity = activityController.setup().get()

        // Click share button — should not crash even with null data
        val shareBtn = activity.findViewById<android.widget.ImageView>(R.id.button_share)
        shareBtn.performClick()

        // Activity should still be alive (no crash)
        activity.isFinishing shouldBe false
        println("    ✅ Share button click with null data → no crash, activity still alive")
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
