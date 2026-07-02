package com.cometchat.uikit.core.formatter

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-based tests for the core formatting engine.
 *
 * Tests Properties 2–9 from the rich-text-formatting-parity design document.
 * Each property is validated with a minimum of 100 random iterations.
 */
class CoreFormattingEnginePropertyTest : StringSpec({

    // ==================== Shared Generators ====================

    val inlineFormats = listOf(
        RichTextFormat.BOLD,
        RichTextFormat.ITALIC,
        RichTextFormat.UNDERLINE,
        RichTextFormat.STRIKETHROUGH,
        RichTextFormat.INLINE_CODE
    )

    val arbInlineFormat: Arb<RichTextFormat> = Arb.element(inlineFormats)

    val arbText: Arb<String> = Arb.string(minSize = 2, maxSize = 200)

    /** Generates a non-empty subset of inline formats. */
    val arbInlineFormatSet: Arb<Set<RichTextFormat>> = Arb.set(arbInlineFormat, 1..5)

    // ==================== Helpers ====================

    /** Simulates typing a single character at the current cursor position. */
    fun typeChar(controller: RichTextEditorController, ch: Char) {
        val text = controller.state.text
        val pos = controller.state.selectionStart
        val newText = text.substring(0, pos) + ch + text.substring(pos)
        controller.onTextChanged(newText, pos + 1, pos + 1)
    }

    /** Simulates moving cursor to a new position (selection change only). */
    fun moveCursor(controller: RichTextEditorController, pos: Int) {
        controller.onTextChanged(controller.state.text, pos, pos)
    }

    // ==================== Property 3 ====================
    // Feature: rich-text-formatting-parity, Property 3: Text insertion inside formatted span extends the span

    /**
     * Property 3: For any formatted span and for any text inserted at a position
     * within or at the end boundary of that span, the span's end SHALL increase
     * by the insertion length, and the inserted text SHALL be covered by the span's formats.
     *
     * We test via RichTextSpanManager.onTextInserted directly because the controller
     * uses diff-based detection which cannot distinguish insertion position when
     * identical characters are involved. The span manager is the authoritative source
     * for span extension behavior.
     *
     * **Validates: Requirements 1.3, 2.3, 3.3, 4.3, 5.3, 14.1, 14.2, 14.3**
     */
    "Property 3: text insertion inside formatted span extends the span" {
        data class TestCase(val textLen: Int, val spanStart: Int, val spanEnd: Int, val insertPos: Int, val insertLen: Int, val format: RichTextFormat)

        val arbTestCase: Arb<TestCase> = arbitrary {
            val textLen = Arb.int(3, 200).bind()
            val format = arbInlineFormat.bind()
            val spanStart = Arb.int(0, textLen - 2).bind()
            val spanEnd = Arb.int(spanStart + 1, textLen).bind()
            // Insert position: within or at end boundary of span
            val insertPos = Arb.int(spanStart, spanEnd).bind()
            val insertLen = Arb.int(1, 20).bind()
            TestCase(textLen, spanStart, spanEnd, insertPos, insertLen, format)
        }

        checkAll(100, arbTestCase) { (_, spanStart, spanEnd, insertPos, insertLen, format) ->
            val manager = RichTextSpanManager()

            // Apply format to range
            manager.addFormat(spanStart, spanEnd, format)

            val originalSpanEnd = spanEnd

            // Simulate text insertion at insertPos
            manager.onTextInserted(insertPos, insertLen)

            val expectedNewEnd = originalSpanEnd + insertLen

            // Verify: every position in the inserted range has the format
            for (pos in insertPos until insertPos + insertLen) {
                val formatsAtPos = manager.getFormatsAt(pos)
                formatsAtPos.contains(format) shouldBe true
            }

            // Verify: the original span end shifted by insertion length
            if (expectedNewEnd > 0) {
                manager.getFormatsAt(expectedNewEnd - 1).contains(format) shouldBe true
            }

            // Verify: position just after the new end does NOT have the format
            manager.getFormatsAt(expectedNewEnd).contains(format) shouldBe false
        }
    }


    // ==================== Property 4 ====================
    // Feature: rich-text-formatting-parity, Property 4: Removing one format preserves all other formats

    /**
     * Property 4: For any text range with multiple formats applied, removing a single
     * format from that range SHALL preserve all other formats on that range unchanged.
     *
     * **Validates: Requirements 1.4, 2.4, 3.4, 4.4, 5.4, 20.3**
     */
    "Property 4: removing one format preserves all other formats" {
        data class TestCase(val textLen: Int, val spanStart: Int, val spanEnd: Int, val formats: Set<RichTextFormat>, val formatToRemove: RichTextFormat)

        val arbTestCase: Arb<TestCase> = arbitrary {
            val textLen = Arb.int(5, 200).bind()
            val spanStart = Arb.int(0, textLen - 2).bind()
            val spanEnd = Arb.int(spanStart + 1, textLen).bind()
            // Need at least 2 formats so removing one still leaves others
            val formats = Arb.set(arbInlineFormat, 2..5).bind()
            val formatToRemove = Arb.element(formats.toList()).bind()
            TestCase(textLen, spanStart, spanEnd, formats, formatToRemove)
        }

        checkAll(100, arbTestCase) { (textLen, spanStart, spanEnd, formats, formatToRemove) ->
            val manager = RichTextSpanManager()

            // Apply all formats to the range
            for (fmt in formats) {
                manager.addFormat(spanStart, spanEnd, fmt)
            }

            // Verify all formats are present before removal
            for (fmt in formats) {
                manager.getFormatsInRange(spanStart, spanEnd).contains(fmt) shouldBe true
            }

            // Remove one format
            manager.removeFormat(spanStart, spanEnd, formatToRemove)

            // Verify: removed format is gone
            manager.getFormatsInRange(spanStart, spanEnd).contains(formatToRemove) shouldBe false

            // Verify: all other formats are still present
            val remainingFormats = formats - formatToRemove
            for (fmt in remainingFormats) {
                manager.getFormatsInRange(spanStart, spanEnd).contains(fmt) shouldBe true
            }
        }
    }

    // ==================== Property 5 ====================
    // Feature: rich-text-formatting-parity, Property 5: Format compatibility — disabled formats are correct

    /**
     * Property 5: For any set of active formats, FormatCompatibility.getDisabledFormats()
     * SHALL return the correct disabled set per the compatibility rules:
     * - CODE_BLOCK active → {BOLD, ITALIC, UNDERLINE, STRIKETHROUGH, INLINE_CODE, LINK} disabled
     * - BULLET_LIST active → ORDERED_LIST disabled (and vice versa)
     * - LINK active → INLINE_CODE disabled (and vice versa)
     * - {BOLD, ITALIC, UNDERLINE, STRIKETHROUGH} never disable each other
     *
     * **Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.5, 10.6**
     */
    "Property 5: format compatibility — disabled formats are correct" {
        val allFormats = RichTextFormat.entries.toList()
        val arbFormatSet: Arb<Set<RichTextFormat>> = Arb.set(Arb.element(allFormats), 0..allFormats.size)

        checkAll(100, arbFormatSet) { activeFormats ->
            val disabled = FormatCompatibility.getDisabledFormats(activeFormats)

            // Rule 1: CODE_BLOCK active → inline formats disabled
            if (RichTextFormat.CODE_BLOCK in activeFormats) {
                disabled.contains(RichTextFormat.BOLD) shouldBe true
                disabled.contains(RichTextFormat.ITALIC) shouldBe true
                disabled.contains(RichTextFormat.UNDERLINE) shouldBe true
                disabled.contains(RichTextFormat.STRIKETHROUGH) shouldBe true
                disabled.contains(RichTextFormat.INLINE_CODE) shouldBe true
                disabled.contains(RichTextFormat.LINK) shouldBe true
            }

            // Rule 2: BULLET_LIST active → ORDERED_LIST disabled
            if (RichTextFormat.BULLET_LIST in activeFormats) {
                disabled.contains(RichTextFormat.ORDERED_LIST) shouldBe true
            }

            // Rule 3: ORDERED_LIST active → BULLET_LIST disabled
            if (RichTextFormat.ORDERED_LIST in activeFormats) {
                disabled.contains(RichTextFormat.BULLET_LIST) shouldBe true
            }

            // Rule 4: LINK active → INLINE_CODE disabled
            if (RichTextFormat.LINK in activeFormats) {
                disabled.contains(RichTextFormat.INLINE_CODE) shouldBe true
            }

            // Rule 5: INLINE_CODE active → LINK disabled
            if (RichTextFormat.INLINE_CODE in activeFormats) {
                disabled.contains(RichTextFormat.LINK) shouldBe true
            }

            // Rule 6: BOLD, ITALIC, UNDERLINE, STRIKETHROUGH never disable each other
            val basicFormats = setOf(RichTextFormat.BOLD, RichTextFormat.ITALIC, RichTextFormat.UNDERLINE, RichTextFormat.STRIKETHROUGH)
            // If only basic formats are active (no CODE_BLOCK, etc.), none of them should be disabled
            if (activeFormats.all { it in basicFormats }) {
                for (fmt in basicFormats) {
                    disabled.contains(fmt) shouldBe false
                }
            }
        }
    }


    // ==================== Property 6 ====================
    // Feature: rich-text-formatting-parity, Property 6: Auto-deselect removes conflicting formats

    /**
     * Property 6: For any set of active formats, calling RichTextFormat.toggleFormat():
     * - with CODE_BLOCK SHALL produce a result containing only CODE_BLOCK
     * - with BULLET_LIST SHALL remove CODE_BLOCK and ORDERED_LIST
     * - with ORDERED_LIST SHALL remove CODE_BLOCK and BULLET_LIST
     * - with BLOCKQUOTE SHALL remove CODE_BLOCK
     *
     * **Validates: Requirements 11.1, 11.2, 11.3, 11.4**
     */
    "Property 6: auto-deselect removes conflicting formats" {
        val allFormats = RichTextFormat.entries.toList()
        val arbFormatSet: Arb<Set<RichTextFormat>> = Arb.set(Arb.element(allFormats), 0..allFormats.size)

        checkAll(100, arbFormatSet) { activeFormats ->
            // Test CODE_BLOCK toggle ON (when not already active)
            if (RichTextFormat.CODE_BLOCK !in activeFormats) {
                val result = RichTextFormat.toggleFormat(activeFormats, RichTextFormat.CODE_BLOCK)
                // CODE_BLOCK auto-deselects ALL others
                result shouldBe setOf(RichTextFormat.CODE_BLOCK)
            }

            // Test BULLET_LIST toggle ON (when not already active)
            if (RichTextFormat.BULLET_LIST !in activeFormats) {
                val result = RichTextFormat.toggleFormat(activeFormats, RichTextFormat.BULLET_LIST)
                result.contains(RichTextFormat.BULLET_LIST) shouldBe true
                result.contains(RichTextFormat.CODE_BLOCK) shouldBe false
                result.contains(RichTextFormat.ORDERED_LIST) shouldBe false
            }

            // Test ORDERED_LIST toggle ON (when not already active)
            if (RichTextFormat.ORDERED_LIST !in activeFormats) {
                val result = RichTextFormat.toggleFormat(activeFormats, RichTextFormat.ORDERED_LIST)
                result.contains(RichTextFormat.ORDERED_LIST) shouldBe true
                result.contains(RichTextFormat.CODE_BLOCK) shouldBe false
                result.contains(RichTextFormat.BULLET_LIST) shouldBe false
            }

            // Test BLOCKQUOTE toggle ON (when not already active)
            if (RichTextFormat.BLOCKQUOTE !in activeFormats) {
                val result = RichTextFormat.toggleFormat(activeFormats, RichTextFormat.BLOCKQUOTE)
                result.contains(RichTextFormat.BLOCKQUOTE) shouldBe true
                result.contains(RichTextFormat.CODE_BLOCK) shouldBe false
            }
        }
    }

    // ==================== Property 7 ====================
    // Feature: rich-text-formatting-parity, Property 7: Pending formats applied to next typed character and cleared

    /**
     * Property 7: For any non-empty set of pending formats and for any character typed,
     * the newly inserted character SHALL have all pending formats applied, and
     * pendingFormats SHALL be empty after the insertion.
     *
     * **Validates: Requirements 12.2, 12.3**
     */
    "Property 7: pending formats applied to next typed character and cleared" {
        data class TestCase(val pendingFormats: Set<RichTextFormat>, val charToType: Char)

        val arbChar: Arb<Char> = arbitrary {
            val code = Arb.int(0x61, 0x7A).bind() // a-z
            code.toChar()
        }

        val arbTestCase: Arb<TestCase> = arbitrary {
            val formats = arbInlineFormatSet.bind()
            val ch = arbChar.bind()
            TestCase(formats, ch)
        }

        checkAll(100, arbTestCase) { (pendingFormats, charToType) ->
            val controller = RichTextEditorController()

            // Set pending formats by toggling each one at cursor
            for (fmt in pendingFormats) {
                controller.toggleFormat(fmt)
            }

            // Verify pending formats are set
            for (fmt in pendingFormats) {
                controller.state.pendingFormats.contains(fmt) shouldBe true
            }

            // Type a character
            typeChar(controller, charToType)

            // Verify: the typed character has all pending formats
            val formatsAtChar = controller.state.spanManager.getFormatsAt(0)
            for (fmt in pendingFormats) {
                formatsAtChar.contains(fmt) shouldBe true
            }

            // Verify: pendingFormats is cleared
            controller.state.pendingFormats.shouldBeEmpty()
        }
    }


    // ==================== Property 8 ====================
    // Feature: rich-text-formatting-parity, Property 8: Disabled formats excluded from inherited formats and cleared

    /**
     * Property 8: For any formatted span and for any non-empty set of disabled formats,
     * typing a character inside the span SHALL result in the new character having
     * (span formats − disabled formats), and disabledFormats SHALL be empty after insertion.
     *
     * **Validates: Requirements 13.2, 13.3**
     */
    "Property 8: disabled formats excluded from inherited formats and cleared" {
        data class TestCase(val spanFormats: Set<RichTextFormat>, val formatToDisable: RichTextFormat)

        val arbTestCase: Arb<TestCase> = arbitrary {
            // Need at least 1 format to disable from
            val formats = Arb.set(arbInlineFormat, 1..5).bind()
            val toDisable = Arb.element(formats.toList()).bind()
            TestCase(formats, toDisable)
        }

        checkAll(100, arbTestCase) { (spanFormats, formatToDisable) ->
            val controller = RichTextEditorController()

            // Type some text using pending formats so the span is created
            for (fmt in spanFormats) {
                controller.toggleFormat(fmt)
            }
            // Type "abc" — all chars get the pending formats
            typeChar(controller, 'a')
            typeChar(controller, 'b')
            typeChar(controller, 'c')

            // Now cursor is at position 3, inside the formatted span
            // Toggle the format OFF to add it to disabledFormats
            controller.toggleFormat(formatToDisable)
            controller.state.disabledFormats.contains(formatToDisable) shouldBe true

            // Type a new character
            typeChar(controller, 'x')

            // Verify: the new character at position 3 should NOT have the disabled format
            val formatsAtNewChar = controller.state.spanManager.getFormatsAt(3)
            formatsAtNewChar.contains(formatToDisable) shouldBe false

            // Verify: the new character should have all other span formats
            val expectedFormats = spanFormats - formatToDisable
            for (fmt in expectedFormats) {
                formatsAtNewChar.contains(fmt) shouldBe true
            }

            // Verify: disabledFormats is cleared after typing
            controller.state.disabledFormats.shouldBeEmpty()
        }
    }

    // ==================== Property 9 ====================
    // Feature: rich-text-formatting-parity, Property 9: Cursor movement clears pending and disabled formats

    /**
     * Property 9: For any non-empty pendingFormats or disabledFormats, changing the
     * selection to a different position SHALL clear both sets.
     *
     * **Validates: Requirements 12.4**
     */
    "Property 9: cursor movement clears pending and disabled formats" {
        data class TestCase(val pendingFormats: Set<RichTextFormat>, val disabledFormat: RichTextFormat?, val moveToPos: Int)

        val arbTestCase: Arb<TestCase> = arbitrary {
            val pending = arbInlineFormatSet.bind()
            // Optionally also have a disabled format (from a different set)
            val disabled = if (Arb.int(0, 1).bind() == 1) arbInlineFormat.bind() else null
            val moveToPos = Arb.int(0, 5).bind()
            TestCase(pending, disabled, moveToPos)
        }

        checkAll(100, arbTestCase) { (pendingFormats, disabledFormat, moveToPos) ->
            val controller = RichTextEditorController()

            // Type some text first so we have positions to move to
            typeChar(controller, 'h')
            typeChar(controller, 'e')
            typeChar(controller, 'l')
            typeChar(controller, 'l')
            typeChar(controller, 'o')
            typeChar(controller, '!')

            // Set up pending formats
            for (fmt in pendingFormats) {
                controller.state.pendingFormats.add(fmt)
            }

            // Set up disabled format if present
            if (disabledFormat != null) {
                controller.state.disabledFormats.add(disabledFormat)
            }

            // Verify at least one set is non-empty
            val hadPending = controller.state.pendingFormats.isNotEmpty()
            val hadDisabled = controller.state.disabledFormats.isNotEmpty()
            (hadPending || hadDisabled) shouldBe true

            // Move cursor to a different position
            val safePos = moveToPos.coerceIn(0, controller.state.text.length)
            // Ensure we actually move to a different position
            val currentPos = controller.state.selectionStart
            val targetPos = if (safePos == currentPos && safePos > 0) safePos - 1 else if (safePos == currentPos) safePos + 1 else safePos
            val finalPos = targetPos.coerceIn(0, controller.state.text.length)

            if (finalPos != currentPos) {
                moveCursor(controller, finalPos)

                // Verify: both sets are cleared
                controller.state.pendingFormats.shouldBeEmpty()
                controller.state.disabledFormats.shouldBeEmpty()
            }
        }
    }
})
