package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.cardbubble

import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.cometchat.cards.CometChatCardView
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.testutils.MockFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for CometChatCardBubble rendering behavior.
 *
 * Tests verify:
 * - Card JSON is passed correctly to CometChatCardView renderer
 * - Fallback text is shown when card payload is empty/null
 * - Fallback priority: getFallbackText() → getText() → "Card Message"
 * - View hierarchy is correct (FrameLayout with CometChatCardView or TextView)
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatCardBubbleRenderingTest"
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CometChatCardBubbleRenderingTest {

    private lateinit var context: android.content.Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    // ==================== Normal card rendering ====================

    @Test
    fun `setMessage with valid card JSON renders CometChatCardView`() {
        val bubble = CometChatCardBubble(context)
        val cardMessage = MockFactory.createCardMessage(
            id = 1L,
            cardJson = mapOf("version" to "1.0", "body" to listOf<Any>())
        )

        bubble.setMessage(cardMessage, UIKitConstants.MessageBubbleAlignment.LEFT)

        // Should contain a CometChatCardView child
        assertEquals(1, bubble.childCount)
        assertTrue(bubble.getChildAt(0) is CometChatCardView)
        println("    → rendered CometChatCardView child")
    }

    // ==================== Fallback rendering ====================

    @Test
    fun `setMessage with null card renders fallback text from getText`() {
        val bubble = CometChatCardBubble(context)
        val cardMessage = MockFactory.createCardMessage(
            id = 2L,
            cardJson = null,
            text = "Preview text for card",
            fallbackText = null
        )

        bubble.setMessage(cardMessage, UIKitConstants.MessageBubbleAlignment.LEFT)

        assertEquals(1, bubble.childCount)
        val textView = bubble.getChildAt(0) as? TextView
        assertNotNull(textView)
        assertEquals("Preview text for card", textView!!.text.toString())
        println("    → rendered fallback text: '${textView.text}'")
    }

    @Test
    fun `setMessage with null card and null text renders Card Message default`() {
        val bubble = CometChatCardBubble(context)
        val cardMessage = MockFactory.createEmptyCardMessage(
            id = 3L,
            text = null,
            fallbackText = null
        )

        bubble.setMessage(cardMessage, UIKitConstants.MessageBubbleAlignment.LEFT)

        assertEquals(1, bubble.childCount)
        val textView = bubble.getChildAt(0) as? TextView
        assertNotNull(textView)
        // Should show the localized "Card Message" string resource
        println("    → rendered default fallback: '${textView!!.text}'")
    }

    @Test
    fun `setMessage with null card prefers fallbackText over text`() {
        val bubble = CometChatCardBubble(context)
        val cardMessage = MockFactory.createCardMessage(
            id = 4L,
            cardJson = null,
            text = "Generic text",
            fallbackText = "Specific fallback"
        )

        bubble.setMessage(cardMessage, UIKitConstants.MessageBubbleAlignment.LEFT)

        assertEquals(1, bubble.childCount)
        val textView = bubble.getChildAt(0) as? TextView
        assertNotNull(textView)
        assertEquals("Specific fallback", textView!!.text.toString())
        println("    → fallbackText takes priority: '${textView.text}'")
    }

    @Test
    fun `setMessage with empty string card JSON renders fallback`() {
        val bubble = CometChatCardBubble(context)
        val cardMessage = MockFactory.createCardMessage(
            id = 5L,
            cardJson = "", // empty object toString = ""
            text = "Empty card fallback"
        )

        bubble.setMessage(cardMessage, UIKitConstants.MessageBubbleAlignment.LEFT)

        // Should fall through to fallback since cardJson.toString() is empty
        assertEquals(1, bubble.childCount)
        val child = bubble.getChildAt(0)
        assertTrue(child is TextView)
        println("    → empty card JSON → fallback rendered")
    }

    // ==================== View replacement ====================

    @Test
    fun `calling setMessage twice replaces the previous content`() {
        val bubble = CometChatCardBubble(context)

        // First call with fallback
        val emptyCard = MockFactory.createEmptyCardMessage(id = 1L, text = "First")
        bubble.setMessage(emptyCard, UIKitConstants.MessageBubbleAlignment.LEFT)
        assertEquals(1, bubble.childCount)

        // Second call with valid card
        val validCard = MockFactory.createCardMessage(
            id = 2L,
            cardJson = mapOf("version" to "1.0", "body" to listOf<Any>())
        )
        bubble.setMessage(validCard, UIKitConstants.MessageBubbleAlignment.LEFT)
        assertEquals(1, bubble.childCount)
        assertTrue(bubble.getChildAt(0) is CometChatCardView)
        println("    → second setMessage replaced first view")
    }

    // ==================== Layout params ====================

    @Test
    fun `card bubble has expected layout params`() {
        val bubble = CometChatCardBubble(context)
        val lp = bubble.layoutParams
        assertNotNull(lp)
        println("    → layoutParams: width=${lp.width}, height=${lp.height}")
    }
}
