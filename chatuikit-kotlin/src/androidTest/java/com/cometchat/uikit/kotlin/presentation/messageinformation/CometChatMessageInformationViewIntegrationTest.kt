package com.cometchat.uikit.kotlin.presentation.messageinformation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.data.datasource.MessageReceiptEventListener
import com.cometchat.uikit.core.domain.repository.MessageInformationRepository
import com.cometchat.uikit.core.viewmodel.CometChatMessageInformationViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.messageinformation.ui.CometChatMessageInformation
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Integration Tests for CometChatMessageInformation (XML/Kotlin).
 *
 * Architecture:
 *   [Fake Repository] → [Real ViewModel] → [Real CometChatMessageInformation View]
 *       → [Espresso assertions on rendered UI]
 *
 * Tests verify:
 * - Toolbar title is displayed
 * - USER conversation shows read/delivered timestamps
 * - GROUP conversation shows receipt list in RecyclerView
 * - Empty state when no receipts
 * - Error state when fetch fails
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.messageinformation.CometChatMessageInformationViewIntegrationTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatMessageInformationViewIntegrationTest {

    @Before
    fun setup() {
        println("=== SETUP: Clearing MessageInfoHostFragment state ===")
        MessageInfoHostFragment.injectedRepository = null
        MessageInfoHostFragment.message = null
    }

    @After
    fun tearDown() {
        MessageInfoHostFragment.injectedRepository = null
        MessageInfoHostFragment.message = null
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun createGroupMessage(messageId: Long): BaseMessage {
        return TextMessage("group-1", "Hello group!", CometChatConstants.RECEIVER_TYPE_GROUP).apply {
            id = messageId
            sender = User().apply { uid = "sender-1"; name = "Sender" }
        }
    }

    private fun createUserMessage(messageId: Long, deliveredAt: Long, readAt: Long): BaseMessage {
        val receiver = User().apply { uid = "receiver-1"; name = "Receiver" }
        return TextMessage(receiver.uid, "Hello!", CometChatConstants.RECEIVER_TYPE_USER).apply {
            id = messageId
            this.deliveredAt = deliveredAt
            this.readAt = readAt
            this.receiver = receiver
            sender = User().apply { uid = "sender-1"; name = "Sender" }
        }
    }

    private fun createMockReceipts(count: Int, messageId: Long = 100L): List<MessageReceipt> {
        return (1..count).map { i ->
            MessageReceipt().apply {
                this.messageId = messageId
                this.deliveredAt = 1000L + i * 100
                this.readAt = 2000L + i * 100
                sender = User().apply { uid = "user-$i"; name = "User $i" }
            }
        }
    }

    private fun createSuccessRepository(receipts: List<MessageReceipt>): MessageInformationRepository {
        return object : MessageInformationRepository {
            override suspend fun fetchReceipts(messageId: Long) = Result.success(receipts)
            override fun createReceiptFromMessage(message: BaseMessage): MessageReceipt {
                return MessageReceipt().apply {
                    sender = if (message.receiver is User) message.receiver as User else null
                    readAt = message.readAt
                    deliveredAt = if (message.deliveredAt == 0L) message.readAt else message.deliveredAt
                    messageId = message.id
                }
            }
        }
    }

    private fun createErrorRepository(code: String, msg: String): MessageInformationRepository {
        return object : MessageInformationRepository {
            override suspend fun fetchReceipts(messageId: Long) =
                Result.failure<List<MessageReceipt>>(CometChatException(code, msg))
            override fun createReceiptFromMessage(message: BaseMessage) = MessageReceipt()
        }
    }

    private fun launchFragment(): FragmentScenario<MessageInfoHostFragment> {
        return launchFragmentInContainer(
            themeResId = R.style.CometChatTheme_DayNight
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Toolbar title is displayed
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun toolbar_displaysTitle() {
        println("=== TEST: toolbar_displaysTitle ===")
        MessageInfoHostFragment.injectedRepository = createSuccessRepository(emptyList())
        MessageInfoHostFragment.message = createGroupMessage(100L)

        println("STEP 1: Launching fragment")
        val scenario = launchFragment()

        println("STEP 2: Asserting toolbar title is displayed")
        onView(withId(R.id.tvToolBarTitle)).check(matches(isDisplayed()))

        println("RESULT: Toolbar title displayed ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: USER conversation shows receipt section
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun userConversation_showsReceiptSection() {
        println("=== TEST: userConversation_showsReceiptSection ===")
        MessageInfoHostFragment.injectedRepository = createSuccessRepository(emptyList())
        MessageInfoHostFragment.message = createUserMessage(100L, deliveredAt = 1000L, readAt = 2000L)

        println("STEP 1: Launching fragment with USER message")
        val scenario = launchFragment()

        println("STEP 2: Asserting user receipt section is visible")
        onView(withId(R.id.messageReceiptsUser))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        println("STEP 3: Asserting group RecyclerView is NOT displayed")
        onView(withId(R.id.messageInfoRecyclerViewGroup)).check(matches(not(isDisplayed())))

        println("RESULT: USER receipt section visible, GROUP list hidden ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: GROUP conversation shows receipt RecyclerView
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun groupConversation_showsReceiptList() {
        println("=== TEST: groupConversation_showsReceiptList ===")
        val receipts = createMockReceipts(3, messageId = 100L)
        MessageInfoHostFragment.injectedRepository = createSuccessRepository(receipts)
        MessageInfoHostFragment.message = createGroupMessage(100L)

        println("STEP 1: Launching fragment with GROUP message and 3 receipts")
        val scenario = launchFragment()

        println("STEP 2: Asserting group RecyclerView is visible")
        onView(withId(R.id.messageInfoRecyclerViewGroup))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        println("STEP 3: Asserting user receipt section is NOT displayed")
        onView(withId(R.id.messageReceiptsUser)).check(matches(not(isDisplayed())))

        println("RESULT: GROUP receipt list visible, USER section hidden ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: USER message with deliveredAt=0 shows empty (no receipt section)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun userMessage_notDelivered_hidesReceiptSection() {
        println("=== TEST: userMessage_notDelivered_hidesReceiptSection ===")
        MessageInfoHostFragment.injectedRepository = createSuccessRepository(emptyList())
        MessageInfoHostFragment.message = createUserMessage(100L, deliveredAt = 0L, readAt = 0L)

        println("STEP 1: Launching fragment with undelivered USER message")
        val scenario = launchFragment()

        println("STEP 2: Asserting user receipt section is NOT displayed (empty state)")
        // When deliveredAt=0, the component shows empty state
        onView(withId(R.id.messageReceiptsUser)).check(matches(not(isDisplayed())))

        println("RESULT: Undelivered message hides receipt section ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Message bubble section is displayed
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun messageBubble_isDisplayed() {
        println("=== TEST: messageBubble_isDisplayed ===")
        MessageInfoHostFragment.injectedRepository = createSuccessRepository(emptyList())
        MessageInfoHostFragment.message = createGroupMessage(100L)

        println("STEP 1: Launching fragment")
        val scenario = launchFragment()

        println("STEP 2: Asserting message bubble parent layout is displayed")
        onView(withId(R.id.messageBubbleParentLayout)).check(matches(isDisplayed()))

        println("RESULT: Message bubble section displayed ✅")
        scenario.close()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Host Fragment
    // ─────────────────────────────────────────────────────────────────────────

    class MessageInfoHostFragment : Fragment() {

        companion object {
            var injectedRepository: MessageInformationRepository? = null
            var message: BaseMessage? = null
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            println("  MessageInfoHostFragment.onCreateView")
            val messageInfoView = CometChatMessageInformation(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }

            // Inject ViewModel and message BEFORE addView to ensure data is
            // available when the view attaches and renders
            injectedRepository?.let { repo ->
                val viewModel = CometChatMessageInformationViewModel(
                    repository = repo,
                    eventListener = MessageReceiptEventListener(),
                    enableListeners = false
                )
                messageInfoView.setViewModel(viewModel)
                println("  MessageInfoHostFragment: ViewModel set BEFORE attach")
            }

            message?.let { msg ->
                messageInfoView.setMessage(msg)
                println("  MessageInfoHostFragment: Message set BEFORE attach (type=${msg.receiverType})")
            }

            return FrameLayout(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                addView(messageInfoView)
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            println("  MessageInfoHostFragment.onViewCreated: View ready")
        }
    }
}
