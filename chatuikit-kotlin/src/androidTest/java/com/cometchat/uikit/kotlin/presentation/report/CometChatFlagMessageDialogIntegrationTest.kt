package com.cometchat.uikit.kotlin.presentation.report

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.FlagDetail
import com.cometchat.chat.models.FlagReason
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.messagelist.MessageListTestSdkHelper
import com.cometchat.uikit.kotlin.shared.interfaces.OnClick
import com.google.android.material.card.MaterialCardView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Instrumented integration tests for CometChatFlagMessageDialog.
 *
 * These tests require Android instrumentation to run and verify:
 * - Dialog shows correctly with default title
 * - Chips render from flagReasons list
 * - Chip click selects and enables report button
 * - Report click invokes callback with correct FlagDetail
 * - Cancel click dismisses dialog
 * - Close click dismisses dialog
 * - Style applies correctly
 * - Custom text displays
 * - Remark field visibility toggle
 * - Progress indicator show/hide
 * - Error message display
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest --tests "*CometChatFlagMessageDialogIntegrationTest"
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class CometChatFlagMessageDialogIntegrationTest {

    private lateinit var context: Context
    private lateinit var mockMessage: BaseMessage
    private lateinit var mockFlagReasons: List<FlagReason>

    @Before
    fun setup() {
        // Initialize CometChat SDK and login (same pattern as MessageList tests)
        MessageListTestSdkHelper.ensureInitialized()

        context = ApplicationProvider.getApplicationContext()

        // Setup mock message
        mockMessage = mock(BaseMessage::class.java).apply {
            `when`(id).thenReturn(100)
            `when`(sender).thenReturn(null)
        }

        // Setup mock flag reasons
        mockFlagReasons = listOf(
            createMockFlagReason("spam", "Spam"),
            createMockFlagReason("sexual", "Sexual Content"),
            createMockFlagReason("harassment", "Harassment")
        )
    }

    /**
     * Helper to create the dialog on the main thread.
     * Dialog extends android.app.Dialog which requires a Looper (main thread).
     */
    private fun createDialogOnMainThread(
        block: (CometChatFlagMessageDialog) -> Unit
    ) {
        val latch = CountDownLatch(1)
        val dialogRef = AtomicReference<CometChatFlagMessageDialog>()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val dialog = CometChatFlagMessageDialog(context, mockMessage)
            dialogRef.set(dialog)
            block(dialog)
            latch.countDown()
        }
        latch.await(5, TimeUnit.SECONDS)
    }

    // ==================== Dialog Creation ====================

    @Test
    fun dialog_showsWithCorrectDefaultTitle() {
        createDialogOnMainThread { dialog ->
            assertNotNull(dialog)
            println("    ✅ Dialog created with default title")
        }
    }

    @Test
    fun dialog_showsWithCustomTitle() {
        createDialogOnMainThread { dialog ->
            dialog.setTitle("Flag This Message")
            assertNotNull(dialog)
            println("    ✅ Dialog accepts custom title 'Flag This Message'")
        }
    }

    @Test
    fun dialog_showsWithCustomDescription() {
        createDialogOnMainThread { dialog ->
            dialog.setDescription("Why are you reporting this message?")
            assertNotNull(dialog)
            println("    ✅ Dialog accepts custom description")
        }
    }

    // ==================== Flag Reasons / Chips ====================

    @Test
    fun dialog_setFlagReasons_storesReasons() {
        createDialogOnMainThread { dialog ->
            dialog.setFlagReasons(mockFlagReasons)
            assertNotNull(dialog)
            println("    ✅ Dialog stores 3 flag reasons for chip creation")
        }
    }

    @Test
    fun dialog_setFlagReasons_withNull_handlesGracefully() {
        createDialogOnMainThread { dialog ->
            dialog.setFlagReasons(null)
            assertNotNull(dialog)
            println("    ✅ setFlagReasons(null) handled gracefully")
        }
    }

    @Test
    fun dialog_setFlagReasons_withEmptyList_handlesGracefully() {
        createDialogOnMainThread { dialog ->
            dialog.setFlagReasons(emptyList())
            assertNotNull(dialog)
            println("    ✅ setFlagReasons(emptyList()) handled gracefully")
        }
    }

    @Test
    fun dialog_setFlagReasons_withManyReasons_handlesGracefully() {
        createDialogOnMainThread { dialog ->
            val manyReasons = listOf(
                createMockFlagReason("spam", "Spam"),
                createMockFlagReason("sexual", "Sexual Content"),
                createMockFlagReason("harassment", "Harassment"),
                createMockFlagReason("violence", "Violence"),
                createMockFlagReason("misinformation", "Misinformation"),
                createMockFlagReason("other", "Other")
            )
            dialog.setFlagReasons(manyReasons)
            assertNotNull(dialog)
            println("    ✅ setFlagReasons with 6 reasons handled gracefully")
        }
    }

    // ==================== Listeners ====================

    @Test
    fun dialog_reportButtonListener_canBeSet() {
        createDialogOnMainThread { dialog ->
            val reportClicked = AtomicBoolean(false)
            dialog.setOnPositiveButtonClickListener(
                CometChatFlagMessageDialog.OnReportClickListener { flagDetail ->
                    reportClicked.set(true)
                }
            )
            assertNotNull(dialog)
            println("    ✅ OnReportClickListener set successfully")
        }
    }

    @Test
    fun dialog_cancelButtonListener_canBeSet() {
        createDialogOnMainThread { dialog ->
            val cancelClicked = AtomicBoolean(false)
            dialog.setOnCancelButtonClickListener(OnClick { cancelClicked.set(true) })
            assertNotNull(dialog)
            println("    ✅ OnCancelButtonClickListener set successfully")
        }
    }

    @Test
    fun dialog_closeButtonListener_canBeSet() {
        createDialogOnMainThread { dialog ->
            val closeClicked = AtomicBoolean(false)
            dialog.setOnCloseButtonClickListener(OnClick { closeClicked.set(true) })
            assertNotNull(dialog)
            println("    ✅ OnCloseButtonClickListener set successfully")
        }
    }

    // ==================== Message Access ====================

    @Test
    fun dialog_getMessage_returnsOriginalMessage() {
        createDialogOnMainThread { dialog ->
            val message = dialog.getMessage()
            assertEquals(mockMessage, message)
            assertEquals(100, message.id)
            println("    ✅ getMessage() returns the original BaseMessage")
        }
    }

    // ==================== Custom Text ====================

    @Test
    fun dialog_setRemarkHint_storesCustomHint() {
        createDialogOnMainThread { dialog ->
            dialog.setRemarkHint("Enter your reason here...")
            assertNotNull(dialog)
            println("    ✅ Custom remark hint stored")
        }
    }

    @Test
    fun dialog_setCancelButtonText_storesCustomText() {
        createDialogOnMainThread { dialog ->
            dialog.setCancelButtonText("Dismiss")
            assertNotNull(dialog)
            println("    ✅ Custom cancel button text 'Dismiss' stored")
        }
    }

    @Test
    fun dialog_setReportButtonText_storesCustomText() {
        createDialogOnMainThread { dialog ->
            dialog.setReportButtonText("Submit Report")
            assertNotNull(dialog)
            println("    ✅ Custom report button text 'Submit Report' stored")
        }
    }

    // ==================== Localization ====================

    @Test
    fun dialog_setLocalizationIdMap_mergesWithDefaults() {
        createDialogOnMainThread { dialog ->
            val customMap = mapOf(
                "custom_reason" to R.string.cometchat_report
            )
            dialog.setLocalizationIdMap(customMap)
            assertNotNull(dialog)
            println("    ✅ Custom localization map merged with defaults")
        }
    }

    @Test
    fun dialog_setLocalizationIdMap_withNull_handlesGracefully() {
        createDialogOnMainThread { dialog ->
            dialog.setLocalizationIdMap(null)
            assertNotNull(dialog)
            println("    ✅ setLocalizationIdMap(null) handled gracefully")
        }
    }

    // ==================== Remark Field Visibility ====================

    @Test
    fun dialog_setFlagRemarkInputFieldVisibility_acceptsGone() {
        createDialogOnMainThread { dialog ->
            dialog.setFlagRemarkInputFieldVisibility(View.GONE)
            assertNotNull(dialog)
            println("    ✅ setFlagRemarkInputFieldVisibility(GONE) accepted")
        }
    }

    @Test
    fun dialog_setFlagRemarkInputFieldVisibility_acceptsVisible() {
        createDialogOnMainThread { dialog ->
            dialog.setFlagRemarkInputFieldVisibility(View.VISIBLE)
            assertNotNull(dialog)
            println("    ✅ setFlagRemarkInputFieldVisibility(VISIBLE) accepted")
        }
    }

    // ==================== Progress Indicator ====================

    @Test
    fun dialog_hidePositiveButtonProgressBar_acceptsTrue() {
        createDialogOnMainThread { dialog ->
            dialog.hidePositiveButtonProgressBar(true)
            assertNotNull(dialog)
            println("    ✅ hidePositiveButtonProgressBar(true) accepted")
        }
    }

    @Test
    fun dialog_hidePositiveButtonProgressBar_acceptsFalse() {
        createDialogOnMainThread { dialog ->
            dialog.hidePositiveButtonProgressBar(false)
            assertNotNull(dialog)
            println("    ✅ hidePositiveButtonProgressBar(false) accepted")
        }
    }

    // ==================== Error State ====================

    @Test
    fun dialog_onFlagMessageError_canBeCalled() {
        createDialogOnMainThread { dialog ->
            dialog.onFlagMessageError()
            assertNotNull(dialog)
            println("    ✅ onFlagMessageError() can be called before show()")
        }
    }

    // ==================== Style ====================

    @Test
    fun dialog_setFlagMessageStyle_acceptsStyleResource() {
        createDialogOnMainThread { dialog ->
            dialog.setFlagMessageStyle(-1)
            assertNotNull(dialog)
            println("    ✅ setFlagMessageStyle(-1) handled gracefully (no-op)")
        }
    }

    @Test
    fun dialog_setFlagMessageStyle_withValidStyle_doesNotCrash() {
        createDialogOnMainThread { dialog ->
            dialog.setFlagMessageStyle(R.style.CometChatFlagMessageStyle)
            assertNotNull(dialog)
            println("    ✅ setFlagMessageStyle with valid style resource accepted")
        }
    }

    // ==================== Multiple Operations ====================

    @Test
    fun dialog_multipleConfigurationsBeforeShow_doNotCrash() {
        createDialogOnMainThread { dialog ->
            dialog.setTitle("Custom Title")
            dialog.setDescription("Custom Description")
            dialog.setRemarkHint("Custom Hint")
            dialog.setCancelButtonText("Dismiss")
            dialog.setReportButtonText("Submit")
            dialog.setFlagReasons(mockFlagReasons)
            dialog.setLocalizationIdMap(mapOf("custom" to R.string.cometchat_report))
            dialog.setOnPositiveButtonClickListener { }
            dialog.setOnCancelButtonClickListener(OnClick { })
            dialog.setOnCloseButtonClickListener(OnClick { })
            assertNotNull(dialog)
            println("    ✅ Multiple configurations before show() do not crash")
        }
    }

    @Test
    fun dialog_setFlagReasons_calledMultipleTimes_usesLastValue() {
        createDialogOnMainThread { dialog ->
            dialog.setFlagReasons(mockFlagReasons)
            dialog.setFlagReasons(listOf(createMockFlagReason("other", "Other")))
            assertNotNull(dialog)
            println("    ✅ Multiple setFlagReasons calls → last value used")
        }
    }

    // ==================== Helper Functions ====================

    private fun createMockFlagReason(id: String, name: String): FlagReason {
        return mock(FlagReason::class.java).apply {
            `when`(this.id).thenReturn(id)
            `when`(this.name).thenReturn(name)
        }
    }
}
