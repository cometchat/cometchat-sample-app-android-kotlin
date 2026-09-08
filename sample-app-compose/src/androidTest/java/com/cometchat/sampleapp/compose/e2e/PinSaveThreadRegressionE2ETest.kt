package com.cometchat.sampleapp.compose.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.TextMessage
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestConfig
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.SHORT_TIMEOUT
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.TIMEOUT
import com.cometchat.sampleapp.compose.e2e.helpers.PinSaveRegressionHelper as H
import com.cometchat.sampleapp.compose.e2e.helpers.PinSaveRegressionHelper.log
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.utils.PinSaveUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.regex.Pattern

/**
 * ENG-38902 regression E2E (Compose / sample-app-compose). One test per QA sub-issue; the Kotlin
 * sample carries the same four scenarios.
 *
 * - test01 ENG-38915  saved indicator survives editing the message
 * - test02 ENG-38916  saved parent shows the indicator inside the thread view
 * - test03 ENG-38917  pinned-messages list: long-press → "Unpin message" → confirm dialog
 * - test04 ENG-38914/38918  at the pin cap the toast names the limit (never "Something went
 *          wrong") and the server keeps refusing on retry
 *
 * Unlike the defensive suites these ASSERT each step the bug report describes.
 *
 * Run (credentials via the suite's runner args — appId/region/authKey/testUid/partnerUid):
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.PinSaveThreadRegressionE2ETest
 */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PinSaveThreadRegressionE2ETest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)
    }

    /** ENG-38915 — the Saved glyph must still be on the bubble after the message is edited. */
    @Test
    fun test01_savedIndicatorSurvivesEdit() {
        H.openChatWith(device)
        val text = H.uniqueText("SaveEdit")
        H.sendAndAwait(device, text)

        H.longPress(device, text)
        H.tapOption(device, "Save message")
        assertTrue("Saved glyph did not appear after saving", H.waitForBubbleIndicator(device, text, "Saved"))
        log("test01: saved ✔")

        H.longPress(device, text)
        H.tapOption(device, "Edit")
        val editText = device.wait(Until.findObject(By.clazz("android.widget.EditText")), TIMEOUT)
        assertNotNull("Composer not in edit mode", editText)
        val edited = "$text edited"
        editText!!.click(); Thread.sleep(300)
        editText.text = edited
        Thread.sleep(1000)
        (device.findObject(By.desc("Send")) ?: device.findObject(By.descContains("Send")))?.click() ?: device.pressEnter()
        assertTrue("Edited text did not render", device.wait(Until.hasObject(By.textContains(edited)), TIMEOUT))
        Thread.sleep(2500)
        log("test01: edited ✔")

        assertTrue(
            "ENG-38915 regression: Saved glyph vanished from the bubble after the edit",
            H.waitForBubbleIndicator(device, edited, "Saved")
        )
    }

    /** ENG-38916 — saving the parent in the list must show the Saved glyph on the parent inside the thread. */
    @Test
    fun test02_savedParentShowsIndicatorInThread() {
        H.openChatWith(device)
        val parent = H.uniqueText("SaveParent")
        H.sendAndAwait(device, parent)

        // Give it a thread: Reply In Thread → send a reply → back to the conversation.
        H.longPress(device, parent)
        H.tapOption(device, "Reply In Thread")
        assertTrue("Thread screen did not open", device.wait(Until.hasObject(By.clazz("android.widget.EditText")), TIMEOUT))
        H.sendAndAwait(device, H.uniqueText("Reply"))
        device.pressBack()
        assertTrue("Did not return to the conversation", device.wait(Until.hasObject(By.textContains(parent)), TIMEOUT))
        Thread.sleep(E2ETestHelper.SETTLE_TIME)

        H.longPress(device, parent)
        H.tapOption(device, "Save message")
        assertTrue("Saved glyph missing in the conversation", H.waitForBubbleIndicator(device, parent, "Saved"))
        log("test02: parent saved in list ✔")

        val replies = H.waitFor(device, By.text(Pattern.compile(".*\\d+\\s*Repl.*")), SHORT_TIMEOUT)
        assertNotNull("'N Replies' row not found under the parent", replies)
        H.tap(device, replies!!)
        assertTrue("Thread did not open", device.wait(Until.hasObject(By.textContains(parent)), TIMEOUT))

        assertTrue(
            "ENG-38916 regression: parent bubble in the thread has no Saved glyph right after saving",
            H.waitForBubbleIndicator(device, parent, "Saved")
        )
    }

    /** ENG-38917 — pinned list: long-press opens the menu; Unpin asks for confirmation. */
    @Test
    fun test03_pinnedListLongPressOffersUnpinWithConfirm() {
        H.openChatWith(device)
        val text = H.uniqueText("PinList")
        H.sendAndAwait(device, text)
        H.longPress(device, text)
        H.tapOption(device, "Pin message")
        assertTrue("Pinned glyph did not appear", H.waitForBubbleIndicator(device, text, "Pinned"))

        val menu = H.waitFor(device, By.desc("Menu"), SHORT_TIMEOUT)
        assertNotNull("Header menu button not found", menu)
        H.tap(device, menu!!)
        H.tapOption(device, "Pinned messages")
        assertTrue("Pinned Messages screen did not open", device.wait(Until.hasObject(By.textContains("Pinned Messages")), TIMEOUT))
        assertTrue("Pinned row for '$text' not listed", device.wait(Until.hasObject(By.textContains(text)), TIMEOUT))
        Thread.sleep(E2ETestHelper.SETTLE_TIME)

        H.longPress(device, text)
        val unpin = H.findOption(device, "Unpin message")
        assertNotNull("ENG-38917 regression: long-press on a pinned row showed no options menu", unpin)
        H.tap(device, unpin!!)

        assertTrue(
            "ENG-38917 regression: no unpin confirmation dialog",
            device.wait(Until.hasObject(By.textContains("unpin")), SHORT_TIMEOUT)
        )
        H.tapOption(device, "Unpin")
        assertTrue("Row still listed after unpin", device.wait(Until.gone(By.textContains(text)), TIMEOUT))
    }

    /** ENG-38914 / ENG-38918 — at the cap the toast is limit-specific and the server keeps refusing. */
    @Test
    fun test04_pinLimitToastNamesTheLimitAndStaysEnforced() {
        val limit = CometChat.getPinMessageLimit()
        log("test04: pinned-messages limit from app settings = $limit")
        assumeTrue("Pin limit not served or too large to exercise ($limit)", limit in 1..12)

        val partner = E2ETestConfig.ONE_TO_ONE_UID
        H.pinnedViaSdk(partner).forEach { H.unpinViaSdk(it.id) }

        val seeded = (1..limit + 1).map { H.sendViaSdk(partner, H.uniqueText("Cap$it")) }
        try {
            seeded.take(limit).forEachIndexed { i, m ->
                val r = H.pinViaSdk(m.id)
                assertTrue("Pin ${i + 1}/$limit failed unexpectedly: ${r.error?.code} ${r.error?.message}", r.ok)
            }
            val overflow = seeded.last()

            H.openChatWith(device, partner)
            val overflowText = (overflow as TextMessage).text
            assertTrue("Overflow message not on screen", device.wait(Until.hasObject(By.textContains(overflowText)), TIMEOUT))
            H.longPress(device, overflowText)
            val pinNode = H.findOption(device, "Pin message")
            assertNotNull("'Pin message' option missing on the overflow message", pinNode)
            val toast = H.captureToast({ H.tap(device, pinNode!!) })
            log("test04: toast = $toast")
            if (toast != null) {
                assertFalse("ENG-38914 regression: generic toast shown at the cap: '$toast'", toast.contains("Something went wrong", ignoreCase = true))
                assertTrue("Toast is not limit-specific: '$toast'", toast.contains("pin", ignoreCase = true) && toast.contains("limit", ignoreCase = true) || toast.contains("only pin", ignoreCase = true))
            }
            Thread.sleep(2000)
            assertFalse("Overflow message shows a Pinned glyph — the cap was not enforced", H.bubbleHasIndicator(device, overflowText, "Pinned"))

            repeat(3) { attempt ->
                val r = H.pinViaSdk(overflow.id)
                assertFalse("ENG-38918: retry ${attempt + 1} pinned past the cap of $limit", r.ok)
                assertEquals(
                    "Unexpected error code on retry ${attempt + 1}: ${r.error?.code} / ${r.error?.message}",
                    UIKitConstants.PinSaveErrorCodes.PINNED_MESSAGES_LIMIT_EXCEEDED, r.error?.code
                )
                val failure = PinSaveUtils.classifyFailure(r.error)
                assertTrue("Not classified as a limit error: $failure", failure is PinSaveUtils.Failure.LimitReached)
                log("test04: retry ${attempt + 1} → ${r.error?.code} params=${r.error?.errorParams} classified=$failure")
            }
        } finally {
            seeded.forEach { H.unpinViaSdk(it.id) }
        }
    }
}
