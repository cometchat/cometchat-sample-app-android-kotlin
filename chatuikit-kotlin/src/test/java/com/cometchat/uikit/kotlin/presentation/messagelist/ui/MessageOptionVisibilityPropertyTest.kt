package com.cometchat.uikit.kotlin.presentation.messagelist.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Android View visibility constants for testing.
 * These mirror the actual Android View constants.
 */
private object OptionViewVisibility {
    const val VISIBLE = 0
    const val INVISIBLE = 4
    const val GONE = 8
}

/**
 * Message option constants for testing.
 * These mirror the actual UIKitConstants.MessageOption constants.
 */
private object MessageOptionConstants {
    const val REPLY_IN_THREAD = "reply_in_thread"
    const val REPLY = "reply"
    const val REPLY_TO_MESSAGE = "reply_to_message"
    const val COPY = "copy"
    const val EDIT = "edit"
    const val DELETE = "delete"
    const val TRANSLATE = "translate"
    const val SHARE = "share"
    const val MESSAGE_PRIVATELY = "message_privately"
    const val MESSAGE_INFORMATION = "message_information"
    const val REPORT = "report"
    const val MARK_AS_UNREAD = "mark_as_unread"
    const val REACT = "react"
}

/**
 * Simple data class representing a message option for testing.
 */
private data class TestMessageOption(
    val id: String,
    val title: String = id
)

/**
 * Test class that simulates the message option visibility storage and filtering
 * behavior of CometChatMessageList without requiring Android context.
 *
 * This mirrors the actual implementation pattern:
 * - Visibility fields for each message option (default View.VISIBLE)
 * - Setter methods to update visibility
 * - Getter methods to retrieve visibility
 * - getOptionVisibilityMap() to build the visibility map
 * - getFilteredMessageOptions() to filter options based on visibility
 */
private class TestMessageOptionVisibilityStorage {
    // Message Option Visibility fields (mirrors CometChatMessageList)
    private var replyInThreadOptionVisibility: Int = OptionViewVisibility.VISIBLE
    private var replyOptionVisibility: Int = OptionViewVisibility.VISIBLE
    private var copyMessageOptionVisibility: Int = OptionViewVisibility.VISIBLE
    private var editMessageOptionVisibility: Int = OptionViewVisibility.VISIBLE
    private var deleteMessageOptionVisibility: Int = OptionViewVisibility.VISIBLE
    private var messageReactionOptionVisibility: Int = OptionViewVisibility.VISIBLE
    private var messageInfoOptionVisibility: Int = OptionViewVisibility.VISIBLE
    private var translateMessageOptionVisibility: Int = OptionViewVisibility.VISIBLE
    private var shareMessageOptionVisibility: Int = OptionViewVisibility.VISIBLE
    private var messagePrivatelyOptionVisibility: Int = OptionViewVisibility.VISIBLE
    private var markUnreadOptionVisibility: Int = OptionViewVisibility.GONE
    private var flagOptionVisibility: Int = OptionViewVisibility.VISIBLE


    // ========================================
    // Setters (mirrors CometChatMessageList)
    // ========================================

    fun setReplyInThreadOptionVisibility(visibility: Int) {
        this.replyInThreadOptionVisibility = visibility
    }

    fun setReplyOptionVisibility(visibility: Int) {
        this.replyOptionVisibility = visibility
    }

    fun setCopyMessageOptionVisibility(visibility: Int) {
        this.copyMessageOptionVisibility = visibility
    }

    fun setEditMessageOptionVisibility(visibility: Int) {
        this.editMessageOptionVisibility = visibility
    }

    fun setDeleteMessageOptionVisibility(visibility: Int) {
        this.deleteMessageOptionVisibility = visibility
    }

    fun setMessageReactionOptionVisibility(visibility: Int) {
        this.messageReactionOptionVisibility = visibility
    }

    fun setMessageInfoOptionVisibility(visibility: Int) {
        this.messageInfoOptionVisibility = visibility
    }

    fun setTranslateMessageOptionVisibility(visibility: Int) {
        this.translateMessageOptionVisibility = visibility
    }

    fun setShareMessageOptionVisibility(visibility: Int) {
        this.shareMessageOptionVisibility = visibility
    }

    fun setMessagePrivatelyOptionVisibility(visibility: Int) {
        this.messagePrivatelyOptionVisibility = visibility
    }

    fun setMarkUnreadOptionVisibility(visibility: Int) {
        this.markUnreadOptionVisibility = visibility
    }

    fun setFlagOptionVisibility(visibility: Int) {
        this.flagOptionVisibility = visibility
    }

    // ========================================
    // Getters (mirrors CometChatMessageList)
    // ========================================

    fun getReplyInThreadOptionVisibility(): Int = replyInThreadOptionVisibility
    fun getReplyOptionVisibility(): Int = replyOptionVisibility
    fun getCopyMessageOptionVisibility(): Int = copyMessageOptionVisibility
    fun getEditMessageOptionVisibility(): Int = editMessageOptionVisibility
    fun getDeleteMessageOptionVisibility(): Int = deleteMessageOptionVisibility
    fun getMessageReactionOptionVisibility(): Int = messageReactionOptionVisibility
    fun getMessageInfoOptionVisibility(): Int = messageInfoOptionVisibility
    fun getTranslateMessageOptionVisibility(): Int = translateMessageOptionVisibility
    fun getShareMessageOptionVisibility(): Int = shareMessageOptionVisibility
    fun getMessagePrivatelyOptionVisibility(): Int = messagePrivatelyOptionVisibility
    fun getMarkUnreadOptionVisibility(): Int = markUnreadOptionVisibility
    fun getFlagOptionVisibility(): Int = flagOptionVisibility

    // ========================================
    // Option Visibility Map (mirrors CometChatMessageList.getOptionVisibilityMap)
    // ========================================

    /**
     * Returns a map of option ID to visibility value.
     * This mirrors CometChatMessageList.getOptionVisibilityMap().
     */
    fun getOptionVisibilityMap(): Map<String, Int> {
        return mapOf(
            MessageOptionConstants.REPLY_IN_THREAD to replyInThreadOptionVisibility,
            MessageOptionConstants.REPLY to replyOptionVisibility,
            MessageOptionConstants.REPLY_TO_MESSAGE to replyOptionVisibility,
            MessageOptionConstants.COPY to copyMessageOptionVisibility,
            MessageOptionConstants.EDIT to editMessageOptionVisibility,
            MessageOptionConstants.DELETE to deleteMessageOptionVisibility,
            MessageOptionConstants.TRANSLATE to translateMessageOptionVisibility,
            MessageOptionConstants.SHARE to shareMessageOptionVisibility,
            MessageOptionConstants.MESSAGE_PRIVATELY to messagePrivatelyOptionVisibility,
            MessageOptionConstants.MESSAGE_INFORMATION to messageInfoOptionVisibility,
            MessageOptionConstants.REPORT to flagOptionVisibility,
            MessageOptionConstants.MARK_AS_UNREAD to markUnreadOptionVisibility,
            MessageOptionConstants.REACT to messageReactionOptionVisibility
        )
    }
}


/**
 * Utility object that simulates MessageOptionsUtils.getFilteredMessageOptions().
 * Filters a list of message options based on visibility settings.
 */
private object TestMessageOptionsFilter {

    /**
     * Filters a list of message options based on visibility settings.
     * This mirrors MessageOptionsUtils.getFilteredMessageOptions().
     *
     * @param options The list of options to filter
     * @param optionVisibilityMap Map of option ID to visibility value
     * @return Filtered list of options based on visibility settings
     */
    fun getFilteredMessageOptions(
        options: List<TestMessageOption>,
        optionVisibilityMap: Map<String, Int>
    ): List<TestMessageOption> {
        return options.filter { option ->
            when (option.id) {
                MessageOptionConstants.REPLY_IN_THREAD ->
                    optionVisibilityMap[MessageOptionConstants.REPLY_IN_THREAD] == OptionViewVisibility.VISIBLE
                MessageOptionConstants.REPLY, MessageOptionConstants.REPLY_TO_MESSAGE ->
                    optionVisibilityMap[MessageOptionConstants.REPLY_TO_MESSAGE] == OptionViewVisibility.VISIBLE
                MessageOptionConstants.COPY ->
                    optionVisibilityMap[MessageOptionConstants.COPY] == OptionViewVisibility.VISIBLE
                MessageOptionConstants.EDIT ->
                    optionVisibilityMap[MessageOptionConstants.EDIT] == OptionViewVisibility.VISIBLE
                MessageOptionConstants.DELETE ->
                    optionVisibilityMap[MessageOptionConstants.DELETE] == OptionViewVisibility.VISIBLE
                MessageOptionConstants.TRANSLATE ->
                    optionVisibilityMap[MessageOptionConstants.TRANSLATE] == OptionViewVisibility.VISIBLE
                MessageOptionConstants.SHARE ->
                    optionVisibilityMap[MessageOptionConstants.SHARE] == OptionViewVisibility.VISIBLE
                MessageOptionConstants.MESSAGE_PRIVATELY ->
                    optionVisibilityMap[MessageOptionConstants.MESSAGE_PRIVATELY] == OptionViewVisibility.VISIBLE
                MessageOptionConstants.MESSAGE_INFORMATION ->
                    optionVisibilityMap[MessageOptionConstants.MESSAGE_INFORMATION] == OptionViewVisibility.VISIBLE
                MessageOptionConstants.REPORT ->
                    optionVisibilityMap[MessageOptionConstants.REPORT] == OptionViewVisibility.VISIBLE
                MessageOptionConstants.MARK_AS_UNREAD ->
                    optionVisibilityMap[MessageOptionConstants.MARK_AS_UNREAD] == OptionViewVisibility.VISIBLE
                MessageOptionConstants.REACT ->
                    optionVisibilityMap[MessageOptionConstants.REACT] == OptionViewVisibility.VISIBLE
                else -> true
            }
        }
    }
}

/**
 * Property-based tests for message option visibility control.
 * Uses Kotest property testing to verify correctness properties.
 *
 * Feature: messagelist-property-parity, Property 1: Message Option Visibility Control
 *
 * *For any* message option visibility property and any valid visibility value (VISIBLE, INVISIBLE, GONE),
 * when the visibility setter is called, the corresponding message option SHALL appear or be hidden
 * in the context menu according to the visibility value.
 *
 * **Validates: Requirements 1.1-1.10**
 */
class MessageOptionVisibilityPropertyTest : FunSpec({

    // ==================== Generators ====================

    /**
     * Generator for valid Android View visibility constants.
     */
    val visibilityArb = Arb.element(OptionViewVisibility.VISIBLE, OptionViewVisibility.INVISIBLE, OptionViewVisibility.GONE)

    /**
     * All message options that can be filtered by visibility.
     */
    val allMessageOptions = listOf(
        TestMessageOption(MessageOptionConstants.REPLY_IN_THREAD, "Reply in Thread"),
        TestMessageOption(MessageOptionConstants.REPLY, "Reply"),
        TestMessageOption(MessageOptionConstants.COPY, "Copy"),
        TestMessageOption(MessageOptionConstants.EDIT, "Edit"),
        TestMessageOption(MessageOptionConstants.DELETE, "Delete"),
        TestMessageOption(MessageOptionConstants.TRANSLATE, "Translate"),
        TestMessageOption(MessageOptionConstants.SHARE, "Share"),
        TestMessageOption(MessageOptionConstants.MESSAGE_PRIVATELY, "Message Privately"),
        TestMessageOption(MessageOptionConstants.MESSAGE_INFORMATION, "Message Info"),
        TestMessageOption(MessageOptionConstants.REPORT, "Report"),
        TestMessageOption(MessageOptionConstants.MARK_AS_UNREAD, "Mark as Unread"),
        TestMessageOption(MessageOptionConstants.REACT, "React")
    )


    // ==================== Property Tests ====================

    context("Property 1: Message Option Visibility Control") {

        // ========================================
        // Reply In Thread Option Visibility Tests
        // ========================================

        test("setReplyInThreadOptionVisibility(View.VISIBLE) includes reply in thread option in filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to VISIBLE
                storage.setReplyInThreadOptionVisibility(OptionViewVisibility.VISIBLE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify reply in thread option is included
                filteredOptions.map { it.id } shouldContain MessageOptionConstants.REPLY_IN_THREAD
            }
        }

        test("setReplyInThreadOptionVisibility(View.GONE) excludes reply in thread option from filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to GONE
                storage.setReplyInThreadOptionVisibility(OptionViewVisibility.GONE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify reply in thread option is excluded
                filteredOptions.map { it.id } shouldNotContain MessageOptionConstants.REPLY_IN_THREAD
            }
        }

        test("setReplyInThreadOptionVisibility(View.INVISIBLE) excludes reply in thread option from filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to INVISIBLE
                storage.setReplyInThreadOptionVisibility(OptionViewVisibility.INVISIBLE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify reply in thread option is excluded
                filteredOptions.map { it.id } shouldNotContain MessageOptionConstants.REPLY_IN_THREAD
            }
        }

        // ========================================
        // Reply Option Visibility Tests
        // ========================================

        test("setReplyOptionVisibility(View.VISIBLE) includes reply option in filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to VISIBLE
                storage.setReplyOptionVisibility(OptionViewVisibility.VISIBLE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify reply option is included
                filteredOptions.map { it.id } shouldContain MessageOptionConstants.REPLY
            }
        }

        test("setReplyOptionVisibility(View.GONE) excludes reply option from filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to GONE
                storage.setReplyOptionVisibility(OptionViewVisibility.GONE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify reply option is excluded
                filteredOptions.map { it.id } shouldNotContain MessageOptionConstants.REPLY
            }
        }

        // ========================================
        // Copy Option Visibility Tests
        // ========================================

        test("setCopyMessageOptionVisibility(View.VISIBLE) includes copy option in filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to VISIBLE
                storage.setCopyMessageOptionVisibility(OptionViewVisibility.VISIBLE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify copy option is included
                filteredOptions.map { it.id } shouldContain MessageOptionConstants.COPY
            }
        }

        test("setCopyMessageOptionVisibility(View.GONE) excludes copy option from filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to GONE
                storage.setCopyMessageOptionVisibility(OptionViewVisibility.GONE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify copy option is excluded
                filteredOptions.map { it.id } shouldNotContain MessageOptionConstants.COPY
            }
        }


        // ========================================
        // Edit Option Visibility Tests
        // ========================================

        test("setEditMessageOptionVisibility(View.VISIBLE) includes edit option in filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to VISIBLE
                storage.setEditMessageOptionVisibility(OptionViewVisibility.VISIBLE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify edit option is included
                filteredOptions.map { it.id } shouldContain MessageOptionConstants.EDIT
            }
        }

        test("setEditMessageOptionVisibility(View.GONE) excludes edit option from filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to GONE
                storage.setEditMessageOptionVisibility(OptionViewVisibility.GONE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify edit option is excluded
                filteredOptions.map { it.id } shouldNotContain MessageOptionConstants.EDIT
            }
        }

        // ========================================
        // Delete Option Visibility Tests
        // ========================================

        test("setDeleteMessageOptionVisibility(View.VISIBLE) includes delete option in filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to VISIBLE
                storage.setDeleteMessageOptionVisibility(OptionViewVisibility.VISIBLE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify delete option is included
                filteredOptions.map { it.id } shouldContain MessageOptionConstants.DELETE
            }
        }

        test("setDeleteMessageOptionVisibility(View.GONE) excludes delete option from filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to GONE
                storage.setDeleteMessageOptionVisibility(OptionViewVisibility.GONE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify delete option is excluded
                filteredOptions.map { it.id } shouldNotContain MessageOptionConstants.DELETE
            }
        }

        // ========================================
        // Message Reaction Option Visibility Tests
        // ========================================

        test("setMessageReactionOptionVisibility(View.VISIBLE) includes react option in filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to VISIBLE
                storage.setMessageReactionOptionVisibility(OptionViewVisibility.VISIBLE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify react option is included
                filteredOptions.map { it.id } shouldContain MessageOptionConstants.REACT
            }
        }

        test("setMessageReactionOptionVisibility(View.GONE) excludes react option from filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to GONE
                storage.setMessageReactionOptionVisibility(OptionViewVisibility.GONE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify react option is excluded
                filteredOptions.map { it.id } shouldNotContain MessageOptionConstants.REACT
            }
        }

        // ========================================
        // Message Info Option Visibility Tests
        // ========================================

        test("setMessageInfoOptionVisibility(View.VISIBLE) includes message info option in filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to VISIBLE
                storage.setMessageInfoOptionVisibility(OptionViewVisibility.VISIBLE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify message info option is included
                filteredOptions.map { it.id } shouldContain MessageOptionConstants.MESSAGE_INFORMATION
            }
        }

        test("setMessageInfoOptionVisibility(View.GONE) excludes message info option from filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to GONE
                storage.setMessageInfoOptionVisibility(OptionViewVisibility.GONE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify message info option is excluded
                filteredOptions.map { it.id } shouldNotContain MessageOptionConstants.MESSAGE_INFORMATION
            }
        }


        // ========================================
        // Translate Option Visibility Tests
        // ========================================

        test("setTranslateMessageOptionVisibility(View.VISIBLE) includes translate option in filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to VISIBLE
                storage.setTranslateMessageOptionVisibility(OptionViewVisibility.VISIBLE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify translate option is included
                filteredOptions.map { it.id } shouldContain MessageOptionConstants.TRANSLATE
            }
        }

        test("setTranslateMessageOptionVisibility(View.GONE) excludes translate option from filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to GONE
                storage.setTranslateMessageOptionVisibility(OptionViewVisibility.GONE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify translate option is excluded
                filteredOptions.map { it.id } shouldNotContain MessageOptionConstants.TRANSLATE
            }
        }

        // ========================================
        // Share Option Visibility Tests
        // ========================================

        test("setShareMessageOptionVisibility(View.VISIBLE) includes share option in filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to VISIBLE
                storage.setShareMessageOptionVisibility(OptionViewVisibility.VISIBLE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify share option is included
                filteredOptions.map { it.id } shouldContain MessageOptionConstants.SHARE
            }
        }

        test("setShareMessageOptionVisibility(View.GONE) excludes share option from filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to GONE
                storage.setShareMessageOptionVisibility(OptionViewVisibility.GONE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify share option is excluded
                filteredOptions.map { it.id } shouldNotContain MessageOptionConstants.SHARE
            }
        }

        // ========================================
        // Message Privately Option Visibility Tests
        // ========================================

        test("setMessagePrivatelyOptionVisibility(View.VISIBLE) includes message privately option in filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to VISIBLE
                storage.setMessagePrivatelyOptionVisibility(OptionViewVisibility.VISIBLE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify message privately option is included
                filteredOptions.map { it.id } shouldContain MessageOptionConstants.MESSAGE_PRIVATELY
            }
        }

        test("setMessagePrivatelyOptionVisibility(View.GONE) excludes message privately option from filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to GONE
                storage.setMessagePrivatelyOptionVisibility(OptionViewVisibility.GONE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify message privately option is excluded
                filteredOptions.map { it.id } shouldNotContain MessageOptionConstants.MESSAGE_PRIVATELY
            }
        }


        // ========================================
        // Report/Flag Option Visibility Tests
        // ========================================

        test("setFlagOptionVisibility(View.VISIBLE) includes report option in filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to VISIBLE
                storage.setFlagOptionVisibility(OptionViewVisibility.VISIBLE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify report option is included
                filteredOptions.map { it.id } shouldContain MessageOptionConstants.REPORT
            }
        }

        test("setFlagOptionVisibility(View.GONE) excludes report option from filtered options") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set visibility to GONE
                storage.setFlagOptionVisibility(OptionViewVisibility.GONE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify report option is excluded
                filteredOptions.map { it.id } shouldNotContain MessageOptionConstants.REPORT
            }
        }

        // ========================================
        // Cross-Option Property Tests
        // ========================================

        test("visibility setting for any option affects only that option in filtered results") {
            checkAll(100, visibilityArb) { visibility ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set only reply in thread to the test visibility
                storage.setReplyInThreadOptionVisibility(visibility)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify reply in thread follows visibility
                val containsReplyInThread = filteredOptions.any { it.id == MessageOptionConstants.REPLY_IN_THREAD }
                containsReplyInThread shouldBe (visibility == OptionViewVisibility.VISIBLE)

                // Verify other options with default VISIBLE are still included
                filteredOptions.map { it.id } shouldContain MessageOptionConstants.COPY
                filteredOptions.map { it.id } shouldContain MessageOptionConstants.EDIT
                filteredOptions.map { it.id } shouldContain MessageOptionConstants.DELETE
            }
        }

        test("multiple options can be hidden independently") {
            checkAll(100, visibilityArb, visibilityArb, visibilityArb) { vis1, vis2, vis3 ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set different visibilities for different options
                storage.setReplyInThreadOptionVisibility(vis1)
                storage.setCopyMessageOptionVisibility(vis2)
                storage.setDeleteMessageOptionVisibility(vis3)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify each option follows its own visibility setting
                val ids = filteredOptions.map { it.id }
                (MessageOptionConstants.REPLY_IN_THREAD in ids) shouldBe (vis1 == OptionViewVisibility.VISIBLE)
                (MessageOptionConstants.COPY in ids) shouldBe (vis2 == OptionViewVisibility.VISIBLE)
                (MessageOptionConstants.DELETE in ids) shouldBe (vis3 == OptionViewVisibility.VISIBLE)
            }
        }

        test("all options visible by default except mark as unread") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Filter options without changing any visibility
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify all options except mark as unread are included
                val ids = filteredOptions.map { it.id }
                ids shouldContain MessageOptionConstants.REPLY_IN_THREAD
                ids shouldContain MessageOptionConstants.REPLY
                ids shouldContain MessageOptionConstants.COPY
                ids shouldContain MessageOptionConstants.EDIT
                ids shouldContain MessageOptionConstants.DELETE
                ids shouldContain MessageOptionConstants.TRANSLATE
                ids shouldContain MessageOptionConstants.SHARE
                ids shouldContain MessageOptionConstants.MESSAGE_PRIVATELY
                ids shouldContain MessageOptionConstants.MESSAGE_INFORMATION
                ids shouldContain MessageOptionConstants.REPORT
                ids shouldContain MessageOptionConstants.REACT

                // Mark as unread defaults to GONE
                ids shouldNotContain MessageOptionConstants.MARK_AS_UNREAD
            }
        }

        test("hiding all options results in empty filtered list") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Hide all options
                storage.setReplyInThreadOptionVisibility(OptionViewVisibility.GONE)
                storage.setReplyOptionVisibility(OptionViewVisibility.GONE)
                storage.setCopyMessageOptionVisibility(OptionViewVisibility.GONE)
                storage.setEditMessageOptionVisibility(OptionViewVisibility.GONE)
                storage.setDeleteMessageOptionVisibility(OptionViewVisibility.GONE)
                storage.setMessageReactionOptionVisibility(OptionViewVisibility.GONE)
                storage.setMessageInfoOptionVisibility(OptionViewVisibility.GONE)
                storage.setTranslateMessageOptionVisibility(OptionViewVisibility.GONE)
                storage.setShareMessageOptionVisibility(OptionViewVisibility.GONE)
                storage.setMessagePrivatelyOptionVisibility(OptionViewVisibility.GONE)
                storage.setFlagOptionVisibility(OptionViewVisibility.GONE)
                // Mark as unread is already GONE by default

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify all options are excluded
                filteredOptions.size shouldBe 0
            }
        }


        test("getter returns the value set by setter for all options") {
            checkAll(100, visibilityArb) { visibility ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set all options to the same visibility
                storage.setReplyInThreadOptionVisibility(visibility)
                storage.setReplyOptionVisibility(visibility)
                storage.setCopyMessageOptionVisibility(visibility)
                storage.setEditMessageOptionVisibility(visibility)
                storage.setDeleteMessageOptionVisibility(visibility)
                storage.setMessageReactionOptionVisibility(visibility)
                storage.setMessageInfoOptionVisibility(visibility)
                storage.setTranslateMessageOptionVisibility(visibility)
                storage.setShareMessageOptionVisibility(visibility)
                storage.setMessagePrivatelyOptionVisibility(visibility)
                storage.setMarkUnreadOptionVisibility(visibility)
                storage.setFlagOptionVisibility(visibility)

                // Verify getters return the set values
                storage.getReplyInThreadOptionVisibility() shouldBe visibility
                storage.getReplyOptionVisibility() shouldBe visibility
                storage.getCopyMessageOptionVisibility() shouldBe visibility
                storage.getEditMessageOptionVisibility() shouldBe visibility
                storage.getDeleteMessageOptionVisibility() shouldBe visibility
                storage.getMessageReactionOptionVisibility() shouldBe visibility
                storage.getMessageInfoOptionVisibility() shouldBe visibility
                storage.getTranslateMessageOptionVisibility() shouldBe visibility
                storage.getShareMessageOptionVisibility() shouldBe visibility
                storage.getMessagePrivatelyOptionVisibility() shouldBe visibility
                storage.getMarkUnreadOptionVisibility() shouldBe visibility
                storage.getFlagOptionVisibility() shouldBe visibility
            }
        }

        test("option visibility map contains all option entries") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                val visibilityMap = storage.getOptionVisibilityMap()

                // Verify all expected keys are present
                visibilityMap.keys shouldContain MessageOptionConstants.REPLY_IN_THREAD
                visibilityMap.keys shouldContain MessageOptionConstants.REPLY
                visibilityMap.keys shouldContain MessageOptionConstants.REPLY_TO_MESSAGE
                visibilityMap.keys shouldContain MessageOptionConstants.COPY
                visibilityMap.keys shouldContain MessageOptionConstants.EDIT
                visibilityMap.keys shouldContain MessageOptionConstants.DELETE
                visibilityMap.keys shouldContain MessageOptionConstants.TRANSLATE
                visibilityMap.keys shouldContain MessageOptionConstants.SHARE
                visibilityMap.keys shouldContain MessageOptionConstants.MESSAGE_PRIVATELY
                visibilityMap.keys shouldContain MessageOptionConstants.MESSAGE_INFORMATION
                visibilityMap.keys shouldContain MessageOptionConstants.REPORT
                visibilityMap.keys shouldContain MessageOptionConstants.MARK_AS_UNREAD
                visibilityMap.keys shouldContain MessageOptionConstants.REACT
            }
        }

        test("toggling visibility multiple times maintains correct state") {
            checkAll(100, visibilityArb, visibilityArb) { first, second ->
                val storage = TestMessageOptionVisibilityStorage()

                // Toggle reply in thread visibility
                storage.setReplyInThreadOptionVisibility(first)
                storage.getReplyInThreadOptionVisibility() shouldBe first

                storage.setReplyInThreadOptionVisibility(second)
                storage.getReplyInThreadOptionVisibility() shouldBe second

                // Verify filtering reflects the final state
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )
                val containsReplyInThread = filteredOptions.any { it.id == MessageOptionConstants.REPLY_IN_THREAD }
                containsReplyInThread shouldBe (second == OptionViewVisibility.VISIBLE)
            }
        }

        test("INVISIBLE is treated as not visible for filtering") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestMessageOptionVisibilityStorage()

                // Set all options to INVISIBLE
                storage.setReplyInThreadOptionVisibility(OptionViewVisibility.INVISIBLE)
                storage.setReplyOptionVisibility(OptionViewVisibility.INVISIBLE)
                storage.setCopyMessageOptionVisibility(OptionViewVisibility.INVISIBLE)
                storage.setEditMessageOptionVisibility(OptionViewVisibility.INVISIBLE)
                storage.setDeleteMessageOptionVisibility(OptionViewVisibility.INVISIBLE)
                storage.setMessageReactionOptionVisibility(OptionViewVisibility.INVISIBLE)
                storage.setMessageInfoOptionVisibility(OptionViewVisibility.INVISIBLE)
                storage.setTranslateMessageOptionVisibility(OptionViewVisibility.INVISIBLE)
                storage.setShareMessageOptionVisibility(OptionViewVisibility.INVISIBLE)
                storage.setMessagePrivatelyOptionVisibility(OptionViewVisibility.INVISIBLE)
                storage.setFlagOptionVisibility(OptionViewVisibility.INVISIBLE)

                // Filter options
                val filteredOptions = TestMessageOptionsFilter.getFilteredMessageOptions(
                    allMessageOptions,
                    storage.getOptionVisibilityMap()
                )

                // Verify all options are excluded (INVISIBLE is treated as not visible)
                filteredOptions.size shouldBe 0
            }
        }
    }
})
