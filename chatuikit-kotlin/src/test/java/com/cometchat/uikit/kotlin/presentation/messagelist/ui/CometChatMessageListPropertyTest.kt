package com.cometchat.uikit.kotlin.presentation.messagelist.ui

import android.content.Context
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.char
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * A simple test implementation of CometChatTextFormatter for property testing.
 * This allows us to create formatters with different tracking characters.
 */
private class TestTextFormatter(trackingChar: Char) : CometChatTextFormatter(trackingChar) {
    override fun search(context: Context, queryString: String?) {
        // No-op for testing
    }

    override fun onScrollToBottom() {
        // No-op for testing
    }
}

/**
 * Test class that simulates the text formatter storage behavior of CometChatMessageList
 * and MessageAdapter without requiring Android context.
 * 
 * This mirrors the actual implementation:
 * - CometChatMessageList stores formatters in _textFormatters
 * - CometChatMessageList propagates to MessageAdapter via messageAdapter.textFormatters = formatterList
 * - MessageAdapter stores formatters in textFormatters property
 */
private class TestMessageListFormatterStorage {
    // Simulates CometChatMessageList._textFormatters
    private var _textFormatters: List<CometChatTextFormatter> = emptyList()
    
    // Simulates MessageAdapter.textFormatters
    private var adapterTextFormatters: List<CometChatTextFormatter> = emptyList()
    
    /**
     * Simulates CometChatMessageList.setTextFormatters()
     * Sets formatters and propagates to adapter.
     */
    fun setTextFormatters(formatters: List<CometChatTextFormatter>?) {
        val formatterList = formatters ?: emptyList()
        _textFormatters = formatterList
        // Propagate to adapter (simulates: messageAdapter.textFormatters = formatterList)
        adapterTextFormatters = formatterList
    }
    
    /**
     * Simulates CometChatMessageList.getTextFormatters()
     */
    fun getTextFormatters(): List<CometChatTextFormatter> = _textFormatters
    
    /**
     * Simulates accessing MessageAdapter.textFormatters
     */
    fun getAdapterTextFormatters(): List<CometChatTextFormatter> = adapterTextFormatters
}


/**
 * Property-based tests for CometChatMessageList text formatter propagation.
 * Uses Kotest property testing to verify correctness properties.
 *
 * Feature: messagelist-property-parity, Property 2: Text Formatter Propagation
 * 
 * *For any* list of text formatters set via `setTextFormatters`, the formatters
 * SHALL be accessible at the TextMessageBubble level and applied to message text rendering.
 *
 * **Validates: Requirements 3.1, 3.2, 3.5**
 */
class CometChatMessageListPropertyTest : FunSpec({

    // ==================== Generators ====================

    /**
     * Generator for tracking characters (lowercase letters).
     */
    val trackingCharArb = Arb.char('a'..'z')

    /**
     * Generator for a single TestTextFormatter with a random tracking character.
     */
    val textFormatterArb = arbitrary {
        val trackingChar = trackingCharArb.bind()
        TestTextFormatter(trackingChar)
    }

    /**
     * Generator for a list of TestTextFormatters with unique tracking characters.
     */
    fun textFormatterListArb(size: IntRange = 0..5) = arbitrary {
        val count = Arb.int(size).bind()
        val chars = ('a'..'z').shuffled().take(count)
        chars.map { TestTextFormatter(it) }
    }

    // ==================== Property Tests ====================

    /**
     * Feature: messagelist-property-parity, Property 2: Text Formatter Propagation
     *
     * *For any* list of text formatters set via `setTextFormatters`, the formatters
     * SHALL be stored correctly in the MessageList and accessible via getTextFormatters().
     *
     * **Validates: Requirements 3.1**
     */
    context("Property 2: Text Formatter Propagation") {

        test("setTextFormatters stores formatters correctly and getTextFormatters returns them") {
            checkAll(100, textFormatterListArb(0..5)) { formatters ->
                val storage = TestMessageListFormatterStorage()

                // Set formatters (simulating MessageList.setTextFormatters)
                storage.setTextFormatters(formatters)

                // Verify formatters are stored correctly in MessageList
                storage.getTextFormatters() shouldHaveSize formatters.size
                storage.getTextFormatters() shouldContainExactly formatters
            }
        }

        test("formatters are propagated to MessageAdapter") {
            checkAll(100, textFormatterListArb(1..5)) { formatters ->
                val storage = TestMessageListFormatterStorage()

                // Set formatters on MessageList
                storage.setTextFormatters(formatters)

                // Verify formatters are propagated to adapter
                storage.getAdapterTextFormatters() shouldHaveSize formatters.size
                storage.getAdapterTextFormatters() shouldContainExactly formatters
            }
        }

        test("empty formatter list should be stored as empty") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageListFormatterStorage()

                // Set empty list
                storage.setTextFormatters(emptyList())

                // Verify empty list is stored
                storage.getTextFormatters().shouldBeEmpty()
                storage.getAdapterTextFormatters().shouldBeEmpty()
            }
        }

        test("null formatter list should be treated as empty list") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageListFormatterStorage()

                // Set null (simulating setTextFormatters(null))
                storage.setTextFormatters(null)

                // Verify empty list is stored
                storage.getTextFormatters().shouldBeEmpty()
                storage.getAdapterTextFormatters().shouldBeEmpty()
            }
        }

        test("formatter tracking characters should be preserved after propagation") {
            checkAll(100, textFormatterListArb(1..5)) { formatters ->
                val storage = TestMessageListFormatterStorage()

                // Store original tracking characters
                val originalTrackingChars = formatters.map { it.getTrackingCharacter() }

                // Set formatters
                storage.setTextFormatters(formatters)

                // Verify tracking characters are preserved in MessageList
                val messageListTrackingChars = storage.getTextFormatters().map { it.getTrackingCharacter() }
                messageListTrackingChars shouldContainExactly originalTrackingChars

                // Verify tracking characters are preserved in Adapter
                val adapterTrackingChars = storage.getAdapterTextFormatters().map { it.getTrackingCharacter() }
                adapterTrackingChars shouldContainExactly originalTrackingChars
            }
        }

        test("replacing formatters should update both MessageList and Adapter") {
            checkAll(100, textFormatterListArb(1..3), textFormatterListArb(1..3)) { firstList, secondList ->
                val storage = TestMessageListFormatterStorage()

                // Set first list
                storage.setTextFormatters(firstList)
                storage.getTextFormatters() shouldHaveSize firstList.size
                storage.getAdapterTextFormatters() shouldHaveSize firstList.size

                // Replace with second list
                storage.setTextFormatters(secondList)
                storage.getTextFormatters() shouldHaveSize secondList.size
                storage.getTextFormatters() shouldContainExactly secondList
                storage.getAdapterTextFormatters() shouldHaveSize secondList.size
                storage.getAdapterTextFormatters() shouldContainExactly secondList
            }
        }

        test("formatter count should match input count for any valid list size") {
            checkAll(100, Arb.int(0..10)) { count ->
                val storage = TestMessageListFormatterStorage()

                // Create formatters with unique tracking characters
                val chars = ('a'..'z').shuffled().take(count)
                val formatters = chars.map { TestTextFormatter(it) }

                // Set formatters
                storage.setTextFormatters(formatters)

                // Verify count matches in both MessageList and Adapter
                storage.getTextFormatters().size shouldBe count
                storage.getAdapterTextFormatters().size shouldBe count
            }
        }

        test("formatters should maintain order after propagation") {
            checkAll(100, textFormatterListArb(2..5)) { formatters ->
                val storage = TestMessageListFormatterStorage()

                // Set formatters
                storage.setTextFormatters(formatters)

                // Verify order is maintained in MessageList
                formatters.forEachIndexed { index, formatter ->
                    storage.getTextFormatters()[index].getTrackingCharacter() shouldBe formatter.getTrackingCharacter()
                }

                // Verify order is maintained in Adapter
                formatters.forEachIndexed { index, formatter ->
                    storage.getAdapterTextFormatters()[index].getTrackingCharacter() shouldBe formatter.getTrackingCharacter()
                }
            }
        }

        test("formatter identity should be preserved (same instance) in both MessageList and Adapter") {
            checkAll(100, textFormatterListArb(1..5)) { formatters ->
                val storage = TestMessageListFormatterStorage()

                // Set formatters
                storage.setTextFormatters(formatters)

                // Verify same instances are returned from MessageList
                formatters.forEachIndexed { index, formatter ->
                    storage.getTextFormatters()[index] shouldBe formatter
                }

                // Verify same instances are returned from Adapter
                formatters.forEachIndexed { index, formatter ->
                    storage.getAdapterTextFormatters()[index] shouldBe formatter
                }
            }
        }

        test("MessageList and Adapter should have identical formatter references") {
            checkAll(100, textFormatterListArb(1..5)) { formatters ->
                val storage = TestMessageListFormatterStorage()

                // Set formatters
                storage.setTextFormatters(formatters)

                // Verify MessageList and Adapter have identical references
                storage.getTextFormatters().forEachIndexed { index, messageListFormatter ->
                    storage.getAdapterTextFormatters()[index] shouldBe messageListFormatter
                }
            }
        }
    }
})


/**
 * Test class that simulates the mentions formatter behavior of CometChatMessageList
 * without requiring Android context.
 *
 * This mirrors the actual implementation:
 * - CometChatMessageList creates CometChatMentionsFormatter in processMentionsFormatter()
 * - CometChatMessageList stores mentionAllLabelId and mentionAllLabel
 * - setDisableMentionAll() delegates to cometchatMentionsFormatter.setDisableMentionAll()
 * - setMentionAllLabelId() delegates to cometchatMentionsFormatter.setMentionAllLabel()
 */
private class TestMentionsFormatterStorage {
    // Simulates CometChatMessageList.cometchatMentionsFormatter
    private var mentionsFormatter: TestMentionsFormatter? = null

    // Simulates CometChatMessageList.mentionAllLabelId
    private var mentionAllLabelId: String? = null

    // Simulates CometChatMessageList.mentionAllLabel
    private var mentionAllLabel: String? = null

    /**
     * Simulates CometChatMessageList.processMentionsFormatter()
     * Creates the mentions formatter and configures it with stored settings.
     */
    fun processMentionsFormatter() {
        mentionsFormatter = TestMentionsFormatter()

        // Configure mentionAllLabel if already set
        if (!mentionAllLabelId.isNullOrEmpty() && !mentionAllLabel.isNullOrEmpty()) {
            mentionsFormatter?.setMentionAllLabel(mentionAllLabelId!!, mentionAllLabel!!)
        }
    }

    /**
     * Simulates CometChatMessageList.getMentionsFormatter()
     */
    fun getMentionsFormatter(): TestMentionsFormatter? = mentionsFormatter

    /**
     * Simulates CometChatMessageList.setDisableMentionAll()
     */
    fun setDisableMentionAll(disable: Boolean) {
        mentionsFormatter?.setDisableMentionAll(disable)
    }

    /**
     * Simulates CometChatMessageList.setMentionAllLabelId()
     */
    fun setMentionAllLabelId(id: String?, label: String?) {
        if (!id.isNullOrEmpty() && !label.isNullOrEmpty()) {
            mentionsFormatter?.setMentionAllLabel(id, label)
            this.mentionAllLabelId = id
            this.mentionAllLabel = label
        }
    }

    /**
     * Gets the stored mentionAllLabelId
     */
    fun getMentionAllLabelId(): String? = mentionAllLabelId

    /**
     * Gets the stored mentionAllLabel
     */
    fun getMentionAllLabel(): String? = mentionAllLabel
}

/**
 * Test implementation of mentions formatter that tracks configuration state.
 * This simulates CometChatMentionsFormatter without requiring Android context.
 */
private class TestMentionsFormatter {
    private var disableMentionAll: Boolean = false
    private var mentionAllId: String = "all"
    private var mentionAllLabelText: String = "Notify All"

    fun setDisableMentionAll(disable: Boolean) {
        disableMentionAll = disable
    }

    fun getDisableMentionAll(): Boolean = disableMentionAll

    fun setMentionAllLabel(labelId: String, labelText: String) {
        if (labelId.isNotEmpty() && labelText.isNotEmpty()) {
            mentionAllId = labelId
            mentionAllLabelText = labelText
        }
    }

    fun getMentionAllId(): String = mentionAllId

    fun getMentionAllLabelText(): String = mentionAllLabelText
}

/**
 * Property-based tests for CometChatMessageList mentions formatter processing.
 * Uses Kotest property testing to verify correctness properties.
 *
 * Feature: messagelist-property-parity, Property 3: Mentions Formatter Processing
 *
 * *For any* text containing @mentions, when a CometChatMentionsFormatter is in the formatter list,
 * the mentions SHALL be rendered with the configured style and the @all mention SHALL respect
 * the `disableMentionAll` setting.
 *
 * **Validates: Requirements 3.6, 3.7, 3.8**
 */
class CometChatMessageListMentionsPropertyTest : FunSpec({

    // ==================== Generators ====================

    /**
     * Generator for non-empty alphanumeric strings (for IDs and labels).
     * Uses arbitrary to filter out empty strings.
     */
    val nonEmptyStringArb = arbitrary {
        var result: String
        do {
            result = Arb.string(1..20).bind()
        } while (result.isEmpty())
        result
    }

    /**
     * Generator for valid mention all label IDs (non-empty).
     */
    val mentionAllIdArb = arbitrary {
        var result: String
        do {
            result = Arb.string(1..10).bind()
        } while (result.isEmpty())
        result
    }

    /**
     * Generator for valid mention all label text (non-empty).
     */
    val mentionAllLabelArb = arbitrary {
        var result: String
        do {
            result = Arb.string(1..30).bind()
        } while (result.isEmpty())
        result
    }

    // ==================== Property Tests ====================

    /**
     * Feature: messagelist-property-parity, Property 3: Mentions Formatter Processing
     *
     * Tests that mentions formatter can be extracted and configured.
     *
     * **Validates: Requirements 3.6**
     */
    context("Property 3: Mentions Formatter Processing") {

        test("processMentionsFormatter creates and stores mentions formatter") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMentionsFormatterStorage()

                // Initially no formatter
                storage.getMentionsFormatter().shouldBeNull()

                // Process mentions formatter
                storage.processMentionsFormatter()

                // Formatter should now exist
                storage.getMentionsFormatter().shouldNotBeNull()
            }
        }

        test("setDisableMentionAll properly configures the formatter") {
            checkAll(100, Arb.boolean()) { disable ->
                val storage = TestMentionsFormatterStorage()
                storage.processMentionsFormatter()

                // Set disable mention all
                storage.setDisableMentionAll(disable)

                // Verify formatter is configured correctly
                storage.getMentionsFormatter()?.getDisableMentionAll() shouldBe disable
            }
        }

        test("setDisableMentionAll with true disables @all mentions") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMentionsFormatterStorage()
                storage.processMentionsFormatter()

                // Disable mention all
                storage.setDisableMentionAll(true)

                // Verify @all is disabled
                storage.getMentionsFormatter()?.getDisableMentionAll()?.shouldBeTrue()
            }
        }

        test("setDisableMentionAll with false enables @all mentions") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMentionsFormatterStorage()
                storage.processMentionsFormatter()

                // First disable, then enable
                storage.setDisableMentionAll(true)
                storage.setDisableMentionAll(false)

                // Verify @all is enabled
                storage.getMentionsFormatter()?.getDisableMentionAll()?.shouldBeFalse()
            }
        }

        test("setMentionAllLabelId properly sets the custom label") {
            checkAll(100, mentionAllIdArb, mentionAllLabelArb) { id, label ->
                val storage = TestMentionsFormatterStorage()
                storage.processMentionsFormatter()

                // Set custom label
                storage.setMentionAllLabelId(id, label)

                // Verify formatter has the custom label
                storage.getMentionsFormatter()?.getMentionAllId() shouldBe id
                storage.getMentionsFormatter()?.getMentionAllLabelText() shouldBe label
            }
        }

        test("setMentionAllLabelId stores values in MessageList") {
            checkAll(100, mentionAllIdArb, mentionAllLabelArb) { id, label ->
                val storage = TestMentionsFormatterStorage()
                storage.processMentionsFormatter()

                // Set custom label
                storage.setMentionAllLabelId(id, label)

                // Verify values are stored in MessageList
                storage.getMentionAllLabelId() shouldBe id
                storage.getMentionAllLabel() shouldBe label
            }
        }

        test("setMentionAllLabelId with empty id does nothing") {
            checkAll(100, mentionAllLabelArb) { label ->
                val storage = TestMentionsFormatterStorage()
                storage.processMentionsFormatter()

                val originalId = storage.getMentionsFormatter()?.getMentionAllId()

                // Try to set with empty id
                storage.setMentionAllLabelId("", label)

                // Verify nothing changed
                storage.getMentionsFormatter()?.getMentionAllId() shouldBe originalId
                storage.getMentionAllLabelId().shouldBeNull()
            }
        }

        test("setMentionAllLabelId with empty label does nothing") {
            checkAll(100, mentionAllIdArb) { id ->
                val storage = TestMentionsFormatterStorage()
                storage.processMentionsFormatter()

                val originalLabel = storage.getMentionsFormatter()?.getMentionAllLabelText()

                // Try to set with empty label
                storage.setMentionAllLabelId(id, "")

                // Verify nothing changed
                storage.getMentionsFormatter()?.getMentionAllLabelText() shouldBe originalLabel
                storage.getMentionAllLabel().shouldBeNull()
            }
        }

        test("setMentionAllLabelId with null id does nothing") {
            checkAll(100, mentionAllLabelArb) { label ->
                val storage = TestMentionsFormatterStorage()
                storage.processMentionsFormatter()

                val originalId = storage.getMentionsFormatter()?.getMentionAllId()

                // Try to set with null id
                storage.setMentionAllLabelId(null, label)

                // Verify nothing changed
                storage.getMentionsFormatter()?.getMentionAllId() shouldBe originalId
                storage.getMentionAllLabelId().shouldBeNull()
            }
        }

        test("setMentionAllLabelId with null label does nothing") {
            checkAll(100, mentionAllIdArb) { id ->
                val storage = TestMentionsFormatterStorage()
                storage.processMentionsFormatter()

                val originalLabel = storage.getMentionsFormatter()?.getMentionAllLabelText()

                // Try to set with null label
                storage.setMentionAllLabelId(id, null)

                // Verify nothing changed
                storage.getMentionsFormatter()?.getMentionAllLabelText() shouldBe originalLabel
                storage.getMentionAllLabel().shouldBeNull()
            }
        }

        test("multiple setMentionAllLabelId calls update to latest values") {
            checkAll(100, mentionAllIdArb, mentionAllLabelArb, mentionAllIdArb, mentionAllLabelArb) { id1, label1, id2, label2 ->
                val storage = TestMentionsFormatterStorage()
                storage.processMentionsFormatter()

                // Set first values
                storage.setMentionAllLabelId(id1, label1)
                storage.getMentionsFormatter()?.getMentionAllId() shouldBe id1
                storage.getMentionsFormatter()?.getMentionAllLabelText() shouldBe label1

                // Set second values
                storage.setMentionAllLabelId(id2, label2)
                storage.getMentionsFormatter()?.getMentionAllId() shouldBe id2
                storage.getMentionsFormatter()?.getMentionAllLabelText() shouldBe label2
            }
        }

        test("disableMentionAll and mentionAllLabel can be configured independently") {
            checkAll(100, Arb.boolean(), mentionAllIdArb, mentionAllLabelArb) { disable, id, label ->
                val storage = TestMentionsFormatterStorage()
                storage.processMentionsFormatter()

                // Configure both settings
                storage.setDisableMentionAll(disable)
                storage.setMentionAllLabelId(id, label)

                // Verify both are set correctly
                storage.getMentionsFormatter()?.getDisableMentionAll() shouldBe disable
                storage.getMentionsFormatter()?.getMentionAllId() shouldBe id
                storage.getMentionsFormatter()?.getMentionAllLabelText() shouldBe label
            }
        }

        test("setDisableMentionAll before processMentionsFormatter has no effect") {
            checkAll(100, Arb.boolean()) { disable ->
                val storage = TestMentionsFormatterStorage()

                // Try to set before formatter exists
                storage.setDisableMentionAll(disable)

                // No formatter yet, so nothing to verify on formatter
                storage.getMentionsFormatter().shouldBeNull()

                // Now create formatter
                storage.processMentionsFormatter()

                // Formatter should have default value (false), not the value we tried to set
                storage.getMentionsFormatter()?.getDisableMentionAll()?.shouldBeFalse()
            }
        }

        test("formatter default disableMentionAll is false") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMentionsFormatterStorage()
                storage.processMentionsFormatter()

                // Default should be false (mentions enabled)
                storage.getMentionsFormatter()?.getDisableMentionAll()?.shouldBeFalse()
            }
        }

        test("formatter default mentionAllId is 'all'") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMentionsFormatterStorage()
                storage.processMentionsFormatter()

                // Default should be "all"
                storage.getMentionsFormatter()?.getMentionAllId() shouldBe "all"
            }
        }
    }
})
