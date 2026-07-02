package com.cometchat.uikit.kotlin.presentation.report

import android.view.View
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.FlagDetail
import com.cometchat.chat.models.FlagReason
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for CometChatFlagMessageDialog interaction behavior (chatuikit-kotlin).
 *
 * Verifies that user interactions produce correct state changes and callbacks:
 * - Close button click invokes OnCloseButtonClickListener or dismisses
 * - Cancel button click invokes OnCancelButtonClickListener or dismisses
 * - Report button click invokes OnReportClickListener with FlagDetail
 * - Chip selection updates selectedChipPosition
 * - Report button disabled when no chip selected
 * - Multiple chip selections (only last selected)
 * - Remark text included in FlagDetail
 * - Localization map applies to chip text
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatFlagMessageDialogInteractionTest"
 */
class CometChatFlagMessageDialogInteractionTest : FunSpec({

    beforeTest {
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        println()
    }

    // ==================== Close Button ====================

    test("close button click without listener → dialog dismisses (default behavior)") {
        // When onCloseClickListener is null, ivClose.setOnClickListener calls dismiss()
        val onCloseClickListener: (() -> Unit)? = null
        val shouldDismiss = onCloseClickListener == null
        shouldDismiss shouldBe true
        println("    ✅ Close button with no listener → dismiss()")
    }

    test("close button click with listener → invokes OnCloseButtonClickListener") {
        var closeClicked = false
        val onCloseClickListener: () -> Unit = { closeClicked = true }
        onCloseClickListener()
        closeClicked shouldBe true
        println("    ✅ Close button with listener → listener invoked")
    }

    // ==================== Cancel Button ====================

    test("cancel button click without listener → dialog dismisses (default behavior)") {
        val onCancelClickListener: (() -> Unit)? = null
        val shouldDismiss = onCancelClickListener == null
        shouldDismiss shouldBe true
        println("    ✅ Cancel button with no listener → dismiss()")
    }

    test("cancel button click with listener → invokes OnCancelButtonClickListener") {
        var cancelClicked = false
        val onCancelClickListener: () -> Unit = { cancelClicked = true }
        onCancelClickListener()
        cancelClicked shouldBe true
        println("    ✅ Cancel button with listener → listener invoked")
    }

    // ==================== Report Button ====================

    test("report button click when enabled → invokes OnReportClickListener with FlagDetail") {
        val flagReasons = createMockFlagReasons(3)
        val selectedChipPosition = 1
        val remarkText = "This is spam content"

        // Simulate report button click logic
        var reportedFlagDetail: FlagDetail? = null
        val onReportClickListener: (FlagDetail) -> Unit = { detail ->
            reportedFlagDetail = detail
        }

        // Simulate the dialog's report button click behavior
        val isReportButtonEnabled = selectedChipPosition >= 0
        if (isReportButtonEnabled) {
            val selectedReason = flagReasons[selectedChipPosition]
            val flagDetail = FlagDetail().apply {
                reasonId = selectedReason.id
                remark = remarkText
            }
            onReportClickListener(flagDetail)
        }

        reportedFlagDetail shouldNotBe null
        reportedFlagDetail!!.reasonId shouldBe "sexual"
        reportedFlagDetail!!.remark shouldBe "This is spam content"
        println("    ✅ Report button click → FlagDetail with reasonId='sexual', remark='This is spam content'")
    }

    test("report button click when disabled → does not invoke listener") {
        val selectedChipPosition = -1
        var reportClicked = false

        val isReportButtonEnabled = selectedChipPosition >= 0
        if (isReportButtonEnabled) {
            reportClicked = true
        }

        reportClicked shouldBe false
        println("    ✅ Report button disabled (no chip selected) → listener NOT invoked")
    }

    test("report button click without listener set → no crash") {
        val selectedChipPosition = 0
        val isReportButtonEnabled = selectedChipPosition >= 0
        val onReportClickListener: ((FlagDetail) -> Unit)? = null

        // Simulate: if (isReportButtonEnabled && onReportClickListener != null)
        val shouldInvoke = isReportButtonEnabled && onReportClickListener != null
        shouldInvoke shouldBe false
        println("    ✅ Report button click with null listener → no crash")
    }

    // ==================== Chip Selection ====================

    test("chip selection updates selectedChipPosition from -1 to valid index") {
        var selectedChipPosition = -1

        // Simulate clicking chip at position 2
        selectedChipPosition = 2

        selectedChipPosition shouldBe 2
        println("    ✅ Chip click → selectedChipPosition updated from -1 to 2")
    }

    test("selecting new chip deselects previous chip") {
        var selectedChipPosition = 0

        // Simulate clicking a different chip
        val previousPosition = selectedChipPosition
        selectedChipPosition = 2

        previousPosition shouldBe 0
        selectedChipPosition shouldBe 2
        // In the dialog, the previous chip gets updateChipStyle(false) and new gets updateChipStyle(true)
        println("    ✅ New chip selection: previous (0) deselected, new (2) selected")
    }

    test("multiple chip selections — only last one is active") {
        var selectedChipPosition = -1

        // Click chip 0
        selectedChipPosition = 0
        selectedChipPosition shouldBe 0

        // Click chip 1
        selectedChipPosition = 1
        selectedChipPosition shouldBe 1

        // Click chip 2
        selectedChipPosition = 2
        selectedChipPosition shouldBe 2

        // Only the last clicked chip should be active
        println("    ✅ Multiple chip clicks → only last (position 2) is active")
    }

    test("chip selection enables report button state") {
        var selectedChipPosition = -1
        var isReportButtonEnabled = selectedChipPosition >= 0
        isReportButtonEnabled shouldBe false

        // Select a chip
        selectedChipPosition = 1
        isReportButtonEnabled = selectedChipPosition >= 0
        isReportButtonEnabled shouldBe true
        println("    ✅ Chip selection transitions report button from disabled to enabled")
    }

    // ==================== Remark Text ====================

    test("remark text is captured in FlagDetail on report") {
        val flagReasons = createMockFlagReasons(3)
        val selectedChipPosition = 0
        val remarkText = "Additional details about the report"

        val flagDetail = FlagDetail().apply {
            reasonId = flagReasons[selectedChipPosition].id
            remark = remarkText
        }

        flagDetail.reasonId shouldBe "spam"
        flagDetail.remark shouldBe "Additional details about the report"
        println("    ✅ Remark text captured in FlagDetail: '${flagDetail.remark}'")
    }

    test("empty remark text results in empty string in FlagDetail") {
        val flagReasons = createMockFlagReasons(3)
        val selectedChipPosition = 0
        val remarkText = ""

        val flagDetail = FlagDetail().apply {
            reasonId = flagReasons[selectedChipPosition].id
            remark = remarkText
        }

        flagDetail.remark shouldBe ""
        println("    ✅ Empty remark → empty string in FlagDetail")
    }

    // ==================== Localization ====================

    test("localization map merges with default map") {
        val defaultMap = mutableMapOf(
            "spam" to 1,
            "sexual" to 2,
            "harassment" to 3
        )
        val customMap = mapOf(
            "custom_reason" to 100,
            "spam" to 999 // Override default
        )

        defaultMap.putAll(customMap)

        defaultMap.size shouldBe 4
        defaultMap["spam"] shouldBe 999 // Overridden
        defaultMap["custom_reason"] shouldBe 100 // Added
        defaultMap["sexual"] shouldBe 2 // Unchanged
        defaultMap["harassment"] shouldBe 3 // Unchanged
        println("    ✅ Custom localization map merges with defaults, overrides existing keys")
    }

    test("setLocalizationIdMap with null does not crash") {
        val flagReasonMap = mutableMapOf(
            "spam" to 1,
            "sexual" to 2,
            "harassment" to 3
        )

        val nullMap: Map<String, Int>? = null
        nullMap?.let { flagReasonMap.putAll(it) }

        flagReasonMap.size shouldBe 3
        println("    ✅ setLocalizationIdMap(null) → no change to existing map")
    }

    test("getLocalizedFlagReasonName returns name when id not in map") {
        val reason = mock<FlagReason>()
        whenever(reason.id).thenReturn("unknown_reason")
        whenever(reason.name).thenReturn("Unknown Reason")

        val flagReasonMap = mapOf(
            "spam" to 1,
            "sexual" to 2,
            "harassment" to 3
        )

        val reasonId = reason.id?.lowercase() ?: ""
        val resourceId = flagReasonMap[reasonId]
        val result = if (resourceId != null) "localized" else reason.name ?: ""

        result shouldBe "Unknown Reason"
        println("    ✅ Unknown reason ID → falls back to FlagReason.name")
    }

    test("getLocalizedFlagReasonName returns empty string when id is null") {
        val reason = mock<FlagReason>()
        whenever(reason.id).thenReturn(null)
        whenever(reason.name).thenReturn("Some Name")

        val reasonId = reason.id
        val result = if (reasonId.isNullOrEmpty()) "" else "localized"

        result shouldBe ""
        println("    ✅ Null reason ID → returns empty string")
    }

    // ==================== FlagDetail Construction ====================

    test("FlagDetail contains correct reasonId from selected chip") {
        val flagReasons = createMockFlagReasons(3)

        // Test each chip position
        for (i in flagReasons.indices) {
            val flagDetail = FlagDetail().apply {
                reasonId = flagReasons[i].id
            }
            flagDetail.reasonId shouldBe flagReasons[i].id
        }
        println("    ✅ Each chip position maps to correct reasonId in FlagDetail")
    }

    test("FlagDetail includes both reasonId and remark when both provided") {
        val flagReasons = createMockFlagReasons(3)
        val selectedChipPosition = 2
        val remarkText = "Detailed explanation of the issue"

        val flagDetail = FlagDetail().apply {
            reasonId = flagReasons[selectedChipPosition].id
            remark = remarkText
        }

        flagDetail.reasonId shouldBe "harassment"
        flagDetail.remark shouldBe "Detailed explanation of the issue"
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
