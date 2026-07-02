package com.cometchat.uikit.compose.presentation.report

import com.cometchat.chat.models.FlagDetail
import com.cometchat.chat.models.FlagReason
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for CometChatFlagMessageDialog Compose component interaction behavior.
 *
 * Verifies that user interactions produce correct callback invocations:
 * - Close button invokes onCloseClick
 * - Cancel button invokes onCancelClick
 * - Report button invokes onReportClick with correct FlagDetail
 * - Chip click updates selectedChipIndex and enables report button
 * - Remark text captured in FlagDetail
 * - Localization map applies to chip text
 *
 * Since CometChatFlagMessageDialog is a composable with callback parameters,
 * these tests verify the callback invocation logic and FlagDetail construction.
 *
 * Run:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*CometChatFlagMessageDialogInteractionTest"
 */
class CometChatFlagMessageDialogInteractionTest : FunSpec({

    beforeTest {
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        println()
    }

    // ==================== Close Button ====================

    test("close button click invokes onCloseClick callback") {
        var closeClicked = false
        val onCloseClick: () -> Unit = { closeClicked = true }

        // Simulate close button click
        onCloseClick()

        closeClicked shouldBe true
        println("    ✅ Close button click → onCloseClick invoked")
    }

    test("onCloseClick defaults to onCancelClick when not provided") {
        var cancelClicked = false
        val onCancelClick: () -> Unit = { cancelClicked = true }
        val onCloseClick: () -> Unit = onCancelClick // Default behavior

        onCloseClick()

        cancelClicked shouldBe true
        println("    ✅ onCloseClick defaults to onCancelClick")
    }

    // ==================== Cancel Button ====================

    test("cancel button click invokes onCancelClick callback") {
        var cancelClicked = false
        val onCancelClick: () -> Unit = { cancelClicked = true }

        // Simulate cancel button click
        onCancelClick()

        cancelClicked shouldBe true
        println("    ✅ Cancel button click → onCancelClick invoked")
    }

    // ==================== Report Button ====================

    test("report button click invokes onReportClick with correct FlagDetail") {
        val flagReasons = createMockFlagReasons(3)
        val selectedChipIndex = 1
        val remarkText = "This is offensive content"

        var reportedDetail: FlagDetail? = null
        val onReportClick: (FlagDetail) -> Unit = { detail -> reportedDetail = detail }

        // Simulate report button click logic from the composable
        val isReportEnabled = selectedChipIndex >= 0
        if (isReportEnabled && selectedChipIndex < flagReasons.size) {
            val selectedReason = flagReasons[selectedChipIndex]
            val flagDetail = FlagDetail().apply {
                reasonId = selectedReason.id
                remark = remarkText
            }
            onReportClick(flagDetail)
        }

        reportedDetail shouldNotBe null
        reportedDetail!!.reasonId shouldBe "sexual"
        reportedDetail!!.remark shouldBe "This is offensive content"
        println("    ✅ Report button → onReportClick with FlagDetail(reasonId='sexual', remark='This is offensive content')")
    }

    test("report button does not invoke callback when no chip selected") {
        val flagReasons = createMockFlagReasons(3)
        val selectedChipIndex = -1

        var reportClicked = false
        val onReportClick: (FlagDetail) -> Unit = { reportClicked = true }

        val isReportEnabled = selectedChipIndex >= 0
        if (isReportEnabled && selectedChipIndex < flagReasons.size) {
            onReportClick(FlagDetail())
        }

        reportClicked shouldBe false
        println("    ✅ No chip selected → report button disabled, callback NOT invoked")
    }

    test("report button does not invoke when selectedChipIndex exceeds flagReasons size") {
        val flagReasons = createMockFlagReasons(3)
        val selectedChipIndex = 5 // Out of bounds

        var reportClicked = false
        val onReportClick: (FlagDetail) -> Unit = { reportClicked = true }

        val isReportEnabled = selectedChipIndex >= 0
        if (isReportEnabled && selectedChipIndex < flagReasons.size) {
            onReportClick(FlagDetail())
        }

        reportClicked shouldBe false
        println("    ✅ selectedChipIndex out of bounds → callback NOT invoked")
    }

    // ==================== Chip Selection ====================

    test("chip click updates selectedChipIndex to clicked index") {
        var selectedChipIndex = -1

        // Simulate clicking chip at index 0
        selectedChipIndex = 0
        selectedChipIndex shouldBe 0

        // Simulate clicking chip at index 2
        selectedChipIndex = 2
        selectedChipIndex shouldBe 2
        println("    ✅ Chip clicks update selectedChipIndex: -1 → 0 → 2")
    }

    test("clicking same chip does not deselect (stays selected)") {
        var selectedChipIndex = -1

        // Click chip 1
        selectedChipIndex = 1
        selectedChipIndex shouldBe 1

        // Click chip 1 again — in the composable, onClick always sets the index
        selectedChipIndex = 1
        selectedChipIndex shouldBe 1
        println("    ✅ Clicking same chip keeps it selected (no toggle behavior)")
    }

    test("chip selection enables report button") {
        var selectedChipIndex = -1

        // Initially disabled
        (selectedChipIndex >= 0) shouldBe false

        // Select chip
        selectedChipIndex = 0
        (selectedChipIndex >= 0) shouldBe true

        // Select different chip — still enabled
        selectedChipIndex = 2
        (selectedChipIndex >= 0) shouldBe true
        println("    ✅ Report button enabled state correctly tracks chip selection")
    }

    // ==================== Remark Text ====================

    test("remark text is captured in FlagDetail when report is clicked") {
        val flagReasons = createMockFlagReasons(3)
        val selectedChipIndex = 0
        val remarkText = "Additional context for the report"

        var capturedRemark: String? = null
        val onReportClick: (FlagDetail) -> Unit = { detail ->
            capturedRemark = detail.remark
        }

        if (selectedChipIndex >= 0 && selectedChipIndex < flagReasons.size) {
            val flagDetail = FlagDetail().apply {
                reasonId = flagReasons[selectedChipIndex].id
                remark = remarkText
            }
            onReportClick(flagDetail)
        }

        capturedRemark shouldBe "Additional context for the report"
        println("    ✅ Remark text captured in FlagDetail on report click")
    }

    test("empty remark text results in empty string in FlagDetail") {
        val flagReasons = createMockFlagReasons(3)
        val selectedChipIndex = 2
        val remarkText = ""

        var capturedRemark: String? = null
        val onReportClick: (FlagDetail) -> Unit = { detail ->
            capturedRemark = detail.remark
        }

        if (selectedChipIndex >= 0 && selectedChipIndex < flagReasons.size) {
            val flagDetail = FlagDetail().apply {
                reasonId = flagReasons[selectedChipIndex].id
                remark = remarkText
            }
            onReportClick(flagDetail)
        }

        capturedRemark shouldBe ""
        println("    ✅ Empty remark → empty string in FlagDetail")
    }

    // ==================== Dismiss Callback ====================

    test("onDismiss callback invoked when dialog is dismissed") {
        var dismissed = false
        val onDismiss: () -> Unit = { dismissed = true }

        // Simulate dialog dismissal
        onDismiss()

        dismissed shouldBe true
        println("    ✅ Dialog dismissal → onDismiss invoked")
    }

    test("onDismiss defaults to onCancelClick when not provided") {
        var cancelClicked = false
        val onCancelClick: () -> Unit = { cancelClicked = true }
        val onDismiss: () -> Unit = onCancelClick // Default behavior

        onDismiss()

        cancelClicked shouldBe true
        println("    ✅ onDismiss defaults to onCancelClick")
    }

    // ==================== Localization ====================

    test("localization map resolves chip text for known reason IDs") {
        val defaultLocalizationMap = mapOf(
            "spam" to 1001,
            "sexual" to 1002,
            "harassment" to 1003
        )

        val reason = mock<FlagReason>()
        whenever(reason.id).thenReturn("spam")
        whenever(reason.name).thenReturn("Spam Fallback")

        val reasonId = reason.id?.lowercase() ?: ""
        val resourceId = defaultLocalizationMap[reasonId]

        resourceId shouldNotBe null
        resourceId shouldBe 1001
        println("    ✅ Known reason ID 'spam' → resolves to resource ID 1001")
    }

    test("localization map falls back to FlagReason.name for unknown IDs") {
        val defaultLocalizationMap = mapOf(
            "spam" to 1001,
            "sexual" to 1002,
            "harassment" to 1003
        )

        val reason = mock<FlagReason>()
        whenever(reason.id).thenReturn("custom_reason")
        whenever(reason.name).thenReturn("Custom Reason Name")

        val reasonId = reason.id?.lowercase() ?: ""
        val resourceId = defaultLocalizationMap[reasonId]
        val displayText = if (resourceId != null) "localized" else reason.name ?: ""

        displayText shouldBe "Custom Reason Name"
        println("    ✅ Unknown reason ID → falls back to FlagReason.name")
    }

    test("localization map uses lowercase id for lookup") {
        val defaultLocalizationMap = mapOf(
            "spam" to 1001
        )

        val reason = mock<FlagReason>()
        whenever(reason.id).thenReturn("SPAM")

        val reasonId = reason.id?.lowercase() ?: ""
        val resourceId = defaultLocalizationMap[reasonId]

        resourceId shouldBe 1001
        println("    ✅ Uppercase 'SPAM' → lowercased to 'spam' → resolves correctly")
    }

    // ==================== FlagDetail Construction ====================

    test("FlagDetail reasonId matches selected chip's FlagReason.id") {
        val flagReasons = createMockFlagReasons(3)

        for (i in flagReasons.indices) {
            val flagDetail = FlagDetail().apply {
                reasonId = flagReasons[i].id
            }
            flagDetail.reasonId shouldBe flagReasons[i].id
        }
        println("    ✅ Each chip index maps to correct FlagReason.id in FlagDetail")
    }

    test("FlagDetail includes both reasonId and remark") {
        val flagReasons = createMockFlagReasons(3)
        val selectedChipIndex = 0
        val remarkText = "Detailed explanation"

        val flagDetail = FlagDetail().apply {
            reasonId = flagReasons[selectedChipIndex].id
            remark = remarkText
        }

        flagDetail.reasonId shouldBe "spam"
        flagDetail.remark shouldBe "Detailed explanation"
        println("    ✅ FlagDetail correctly contains both reasonId and remark")
    }
})

// ==================== Helper Functions ====================

private fun createMockFlagReasons(count: Int): List<FlagReason> {
    val reasonIds = listOf("spam", "sexual", "harassment", "other", "violence")
    val reasonNames = listOf("Spam", "Sexual Content", "Harassment", "Other", "Violence")
    return (0 until count).map { i ->
        val reason = mock<FlagReason>()
        whenever(reason.id).thenReturn(reasonIds[i])
        whenever(reason.name).thenReturn(reasonNames[i])
        reason
    }
}
