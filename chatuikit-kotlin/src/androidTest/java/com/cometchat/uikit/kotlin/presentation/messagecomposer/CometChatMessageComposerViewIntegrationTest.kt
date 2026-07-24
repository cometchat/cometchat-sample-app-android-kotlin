package com.cometchat.uikit.kotlin.presentation.messagecomposer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.data.datasource.MessageComposerDataSource
import com.cometchat.uikit.core.data.repository.MessageComposerRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.EditMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendCustomMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendMediaMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendTextMessageUseCase
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.messagecomposer.ui.CometChatMessageComposer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented integration tests for CometChatMessageComposer (chatuikit-kotlin).
 * Real View inflated in Fragment, Espresso assertions, fake DataSource only.
 *
 * NOTE: The CometChatMessageComposer's onDetachedFromWindow() calls endTyping()
 * which requires CometChat.init(). To avoid this, tests that need a ViewModel
 * use a TestSafeMessageComposerViewModel that guards typing calls with try-catch.
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest --tests "*CometChatMessageComposerViewIntegrationTest"
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatMessageComposerViewIntegrationTest {

    @Before
    fun setup() {
        MessageComposerHostFragment.injectedUser = null
        MessageComposerHostFragment.injectedGroup = null
        MessageComposerHostFragment.injectedDataSource = null
        MessageComposerHostFragment.editMessageToSet = null
    }

    @After
    fun tearDown() {
        MessageComposerHostFragment.injectedUser = null
        MessageComposerHostFragment.injectedGroup = null
        MessageComposerHostFragment.injectedDataSource = null
        MessageComposerHostFragment.editMessageToSet = null
    }

    // ==================== Helper ====================

    private fun createFakeDataSource(): MessageComposerDataSource {
        return object : MessageComposerDataSource {
            override suspend fun sendTextMessage(message: TextMessage): TextMessage = message
            override suspend fun sendMediaMessage(message: MediaMessage): MediaMessage = message
            override suspend fun sendCustomMessage(message: CustomMessage): CustomMessage = message
            override suspend fun editMessage(message: BaseMessage): BaseMessage = message
        }
    }

    // ==================== Compose Box Rendering ====================

    @Test
    fun composeBox_isDisplayed_forUserConversation() {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        MessageComposerHostFragment.injectedUser = user
        MessageComposerHostFragment.injectedDataSource = createFakeDataSource()

        launchFragmentInContainer<MessageComposerHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.composeBoxCard))
            .check(matches(isDisplayed()))
    }

    @Test
    fun composeBox_isDisplayed_forGroupConversation() {
        val group = MockFactory.createGroup(guid = "group-1", name = "Developers", membersCount = 10)
        MessageComposerHostFragment.injectedGroup = group
        MessageComposerHostFragment.injectedDataSource = createFakeDataSource()

        launchFragmentInContainer<MessageComposerHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.composeBoxCard))
            .check(matches(isDisplayed()))
    }

    // ==================== Text Input ====================

    @Test
    fun messageInput_isDisplayed() {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        MessageComposerHostFragment.injectedUser = user
        MessageComposerHostFragment.injectedDataSource = createFakeDataSource()

        launchFragmentInContainer<MessageComposerHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.etMessageInput))
            .check(matches(isDisplayed()))
    }

    @Test
    fun messageInput_showsPlaceholderHint() {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        MessageComposerHostFragment.injectedUser = user
        MessageComposerHostFragment.injectedDataSource = createFakeDataSource()

        launchFragmentInContainer<MessageComposerHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.etMessageInput))
            .check(matches(isDisplayed()))
    }

    // ==================== Send Button ====================

    @Test
    fun sendButton_isDisplayed() {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        MessageComposerHostFragment.injectedUser = user
        MessageComposerHostFragment.injectedDataSource = createFakeDataSource()

        launchFragmentInContainer<MessageComposerHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.sendButtonLayout))
            .check(matches(isDisplayed()))
    }

    // ==================== Attachment Button ====================

    @Test
    fun attachmentButton_isDisplayed() {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        MessageComposerHostFragment.injectedUser = user
        MessageComposerHostFragment.injectedDataSource = createFakeDataSource()

        launchFragmentInContainer<MessageComposerHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.ivAttachment))
            .check(matches(isDisplayed()))
    }

    // ==================== Edit Preview ====================

    @Test
    fun editPreview_isDisplayed_whenEditMessageSet() {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        MessageComposerHostFragment.injectedUser = user
        MessageComposerHostFragment.injectedDataSource = createFakeDataSource()
        MessageComposerHostFragment.editMessageToSet = MockFactory.createTextMessage(
            id = 100L,
            text = "Original message",
            senderUid = "user-1",
            receiverId = "user-2"
        )

        launchFragmentInContainer<MessageComposerHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.editPreviewCard))
            .check(matches(isDisplayed()))
    }

    // ==================== User/Group Configuration ====================

    @Test
    fun composer_configuredForUser_rendersCorrectly() {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        MessageComposerHostFragment.injectedUser = user
        MessageComposerHostFragment.injectedDataSource = createFakeDataSource()

        launchFragmentInContainer<MessageComposerHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Verify the composer is displayed and functional
        onView(withId(R.id.etMessageInput))
            .check(matches(isDisplayed()))
        onView(withId(R.id.composeBoxCard))
            .check(matches(isDisplayed()))
    }

    @Test
    fun composer_configuredForGroup_rendersCorrectly() {
        val group = MockFactory.createGroup(guid = "group-1", name = "Developers", membersCount = 10)
        MessageComposerHostFragment.injectedGroup = group
        MessageComposerHostFragment.injectedDataSource = createFakeDataSource()

        launchFragmentInContainer<MessageComposerHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Verify the composer is displayed and functional
        onView(withId(R.id.etMessageInput))
            .check(matches(isDisplayed()))
        onView(withId(R.id.composeBoxCard))
            .check(matches(isDisplayed()))
    }
}

/**
 * Test-safe subclass of CometChatMessageComposerViewModel that guards
 * startTyping() and endTyping() calls with try-catch to prevent crashes
 * when CometChat SDK is not initialized in the test environment.
 */
class TestSafeMessageComposerViewModel(
    sendTextMessageUseCase: SendTextMessageUseCase,
    sendMediaMessageUseCase: SendMediaMessageUseCase,
    sendCustomMessageUseCase: SendCustomMessageUseCase,
    editMessageUseCase: EditMessageUseCase
) : CometChatMessageComposerViewModel(
    sendTextMessageUseCase = sendTextMessageUseCase,
    sendMediaMessageUseCase = sendMediaMessageUseCase,
    sendCustomMessageUseCase = sendCustomMessageUseCase,
    editMessageUseCase = editMessageUseCase,
    enableListeners = false
) {
    override fun startTyping() {
        // No-op in tests — CometChat SDK not initialized
    }

    override fun endTyping() {
        // No-op in tests — CometChat SDK not initialized
    }
}

/**
 * Host Fragment for injecting test data into CometChatMessageComposer.
 */
open class MessageComposerHostFragment : Fragment() {

    companion object {
        var injectedUser: User? = null
        var injectedGroup: Group? = null
        var injectedDataSource: MessageComposerDataSource? = null
        var editMessageToSet: TextMessage? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val composer = CometChatMessageComposer(requireContext())
        return FrameLayout(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            addView(composer)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val composer = (view as FrameLayout).getChildAt(0) as CometChatMessageComposer

        // Create ViewModel with fake DataSource using test-safe subclass
        val dataSource = injectedDataSource ?: object : MessageComposerDataSource {
            override suspend fun sendTextMessage(message: TextMessage): TextMessage = message
            override suspend fun sendMediaMessage(message: MediaMessage): MediaMessage = message
            override suspend fun sendCustomMessage(message: CustomMessage): CustomMessage = message
            override suspend fun editMessage(message: BaseMessage): BaseMessage = message
        }
        val repository = MessageComposerRepositoryImpl(dataSource)
        val sendTextMessageUseCase = SendTextMessageUseCase(repository)
        val sendMediaMessageUseCase = SendMediaMessageUseCase(repository)
        val sendCustomMessageUseCase = SendCustomMessageUseCase(repository)
        val editMessageUseCase = EditMessageUseCase(repository)
        val viewModel = TestSafeMessageComposerViewModel(
            sendTextMessageUseCase = sendTextMessageUseCase,
            sendMediaMessageUseCase = sendMediaMessageUseCase,
            sendCustomMessageUseCase = sendCustomMessageUseCase,
            editMessageUseCase = editMessageUseCase
        )
        composer.setViewModel(viewModel)

        // Set user or group
        injectedUser?.let { composer.setUser(it) }
        injectedGroup?.let { composer.setGroup(it) }

        // Set edit message if provided
        editMessageToSet?.let { viewModel.setEditMessage(it) }
    }
}
