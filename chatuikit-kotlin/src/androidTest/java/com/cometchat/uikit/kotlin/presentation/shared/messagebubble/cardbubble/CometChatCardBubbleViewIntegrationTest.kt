package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.cardbubble

import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.cards.CometChatCardView
import com.cometchat.chat.models.CardMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatUIEvent
import com.cometchat.uikit.core.testutils.MockFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented integration tests for CometChatCardBubble (Views).
 *
 * Uses [launchFragmentInContainer] (fragment-testing) to host the view in a real,
 * themed Activity provided by `fragment-testing-manifest` — the module-standard way
 * to obtain an Activity context without declaring one in a test manifest. The
 * `onFragment` callback runs on the UI thread, which the WebView-based
 * CometChatCardView requires.
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.shared.messagebubble.cardbubble.CometChatCardBubbleViewIntegrationTest
 */
@RunWith(AndroidJUnit4::class)
class CometChatCardBubbleViewIntegrationTest {

    // ==================== View Creation ====================

    @Test
    fun cardBubble_inflates_withoutCrash() {
        launchFragmentInContainer<Fragment>().onFragment { fragment ->
            val bubble: FrameLayout = CometChatCardBubble(fragment.requireActivity())
            assertNotNull(bubble)
            assertEquals(0, bubble.childCount)
        }
    }

    @Test
    fun cardBubble_hasLayoutParams_afterConstruction() {
        launchFragmentInContainer<Fragment>().onFragment { fragment ->
            val bubble = CometChatCardBubble(fragment.requireActivity())
            assertNotNull(bubble.layoutParams)
        }
    }

    // ==================== Card Rendering ====================

    @Test
    fun setMessage_withValidCard_rendersCometChatCardView() {
        launchFragmentInContainer<Fragment>().onFragment { fragment ->
            val bubble = CometChatCardBubble(fragment.requireActivity())
            val message = MockFactory.createCardMessage(
                id = 1L,
                cardJson = mapOf("version" to "1.0", "body" to listOf<Any>())
            )

            bubble.setMessage(message, UIKitConstants.MessageBubbleAlignment.LEFT)

            assertEquals(1, bubble.childCount)
            assertTrue(bubble.getChildAt(0) is CometChatCardView)
        }
    }

    // ==================== Fallback Rendering ====================

    @Test
    fun setMessage_withNullCard_rendersTextViewFallback() {
        launchFragmentInContainer<Fragment>().onFragment { fragment ->
            val bubble = CometChatCardBubble(fragment.requireActivity())
            // No fallbackText, so the bubble falls through to text ("Preview text").
            // Fallback priority is fallbackText → text → "Card Message".
            val message = MockFactory.createCardMessage(
                id = 2L,
                cardJson = null,
                text = "Preview text",
                fallbackText = null
            )

            bubble.setMessage(message, UIKitConstants.MessageBubbleAlignment.LEFT)

            assertEquals(1, bubble.childCount)
            assertTrue(bubble.getChildAt(0) is TextView)
            assertEquals("Preview text", (bubble.getChildAt(0) as TextView).text.toString())
        }
    }

    @Test
    fun setMessage_withNullCardAndNullText_rendersDefaultFallback() {
        launchFragmentInContainer<Fragment>().onFragment { fragment ->
            val bubble = CometChatCardBubble(fragment.requireActivity())
            val message = MockFactory.createEmptyCardMessage(
                id = 3L,
                text = null,
                fallbackText = null
            )

            bubble.setMessage(message, UIKitConstants.MessageBubbleAlignment.LEFT)

            assertEquals(1, bubble.childCount)
            assertTrue(bubble.getChildAt(0) is TextView)
            val textView = bubble.getChildAt(0) as TextView
            assertNotNull(textView.text)
            assertTrue(textView.text.isNotEmpty())
        }
    }

    @Test
    fun setMessage_fallbackPriority_fallbackTextOverText() {
        launchFragmentInContainer<Fragment>().onFragment { fragment ->
            val bubble = CometChatCardBubble(fragment.requireActivity())
            val message = MockFactory.createCardMessage(
                id = 4L,
                cardJson = null,
                text = "Generic text",
                fallbackText = "Specific fallback"
            )

            bubble.setMessage(message, UIKitConstants.MessageBubbleAlignment.LEFT)

            val textView = bubble.getChildAt(0) as TextView
            assertEquals("Specific fallback", textView.text.toString())
        }
    }

    // ==================== View Replacement ====================

    @Test
    fun setMessage_calledTwice_replacesContent() {
        launchFragmentInContainer<Fragment>().onFragment { fragment ->
            val bubble = CometChatCardBubble(fragment.requireActivity())

            // First: fallback text
            val emptyCard = MockFactory.createEmptyCardMessage(id = 1L, text = "First")
            bubble.setMessage(emptyCard, UIKitConstants.MessageBubbleAlignment.LEFT)
            assertEquals(1, bubble.childCount)
            assertTrue(bubble.getChildAt(0) is TextView)

            // Second: valid card (WebView-based)
            val validCard = MockFactory.createCardMessage(
                id = 2L,
                cardJson = mapOf("version" to "1.0", "body" to listOf<Any>())
            )
            bubble.setMessage(validCard, UIKitConstants.MessageBubbleAlignment.LEFT)
            assertEquals(1, bubble.childCount)
            assertTrue(bubble.getChildAt(0) is CometChatCardView)
        }
    }

    // ==================== Width Constraint ====================

    @Test
    fun cardView_hasMaxWidth_approximately75PercentOfScreen() {
        launchFragmentInContainer<Fragment>().onFragment { fragment ->
            val activity = fragment.requireActivity()
            val bubble = CometChatCardBubble(activity)
            val message = MockFactory.createCardMessage(
                id = 5L,
                cardJson = mapOf("version" to "1.0", "body" to listOf<Any>())
            )

            bubble.setMessage(message, UIKitConstants.MessageBubbleAlignment.LEFT)

            val cardView = bubble.getChildAt(0) as CometChatCardView
            val screenWidth = activity.resources.displayMetrics.widthPixels
            val expectedMaxWidth = (screenWidth * 0.75).toInt()
            val lp = cardView.layoutParams
            assertEquals(expectedMaxWidth, lp.width)
        }
    }

    // ==================== Action Event Emission ====================

    @Test
    fun cardAction_emitsCardActionClicked_onUIEventBus() {
        runBlocking {
            val message = MockFactory.createCardMessage(id = 10L)
            val action = mapOf("type" to "openUrl", "url" to "https://test.com")

            val event = CometChatUIEvent.CardActionClicked(
                message = message,
                actionEvent = action
            )

            val received = CoroutineScope(Dispatchers.Default).async {
                CometChatEvents.uiEvents.first { it is CometChatUIEvent.CardActionClicked }
            }

            CometChatEvents.emitUIEvent(event)

            val result = received.await() as CometChatUIEvent.CardActionClicked
            assertEquals(10L, (result.message as CardMessage).id)
            assertEquals(action, result.actionEvent)
        }
    }
}
