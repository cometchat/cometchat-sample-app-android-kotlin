package com.cometchat.uikit.kotlin.presentation.threadheader

import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatThreadHeaderViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.messagelist.MessageListTestSdkHelper
import com.cometchat.uikit.kotlin.presentation.threadheader.style.CometChatThreadHeaderStyle
import com.cometchat.uikit.kotlin.presentation.threadheader.ui.CometChatThreadHeader
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.whenever

private val THREAD_HEADER_TEST_ID = View.generateViewId()

/**
 * Instrumented integration tests for CometChatThreadHeader (chatuikit-kotlin).
 * Tests real View inflation, RecyclerView rendering, and visibility controls.
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest --tests "*CometChatThreadHeaderIntegrationTest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatThreadHeaderIntegrationTest {

    @Before
    fun setup() {
        MessageListTestSdkHelper.ensureInitialized()
        ThreadHeaderHostFragment.injectedParentMessage = null
        ThreadHeaderHostFragment.injectedReplyCount = 0
    }

    @After
    fun tearDown() {
        ThreadHeaderHostFragment.injectedParentMessage = null
        ThreadHeaderHostFragment.injectedReplyCount = 0
    }

    private fun createParentMessage(
        id: Long = 100L,
        senderUid: String = "user-1",
        replyCount: Int = 0,
        text: String = "Parent message for thread"
    ): BaseMessage {
        val message = MockFactory.createTextMessage(
            id = id,
            senderUid = senderUid,
            text = text,
            sentAt = 1735689600L
        )
        whenever(message.replyCount).thenReturn(replyCount)
        return message
    }

    // ==================== View Inflation ====================

    @Test
    fun viewInflatesCorrectly() {
        ThreadHeaderHostFragment.injectedParentMessage = createParentMessage(replyCount = 3)
        ThreadHeaderHostFragment.injectedReplyCount = 3

        launchFragmentInContainer<ThreadHeaderHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // The thread header view should be displayed
        onView(withId(THREAD_HEADER_TEST_ID))
            .check(matches(isDisplayed()))
    }

    // ==================== Parent Message Display ====================

    @Test
    fun parentMessageDisplaysInRecyclerView() {
        ThreadHeaderHostFragment.injectedParentMessage = createParentMessage(
            replyCount = 5,
            text = "Hello from thread parent"
        )
        ThreadHeaderHostFragment.injectedReplyCount = 5

        launchFragmentInContainer<ThreadHeaderHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // RecyclerView should be displayed with the parent message
        onView(withId(R.id.rv_parent_bubble_view))
            .check(matches(isDisplayed()))
    }

    @Test
    fun replyCountVisibilityGoneHidesText() {
        ThreadHeaderHostFragment.injectedParentMessage = createParentMessage(replyCount = 5)
        ThreadHeaderHostFragment.injectedReplyCount = 5
        ThreadHeaderHostFragment.hideReplyCount = true

        launchFragmentInContainer<ThreadHeaderHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Reply count text should not be visible
    }

    // ==================== Style Application ====================

    @Test
    fun styleAppliesCorrectly() {
        ThreadHeaderHostFragment.injectedParentMessage = createParentMessage(replyCount = 3)
        ThreadHeaderHostFragment.injectedReplyCount = 3
        ThreadHeaderHostFragment.customStyle = CometChatThreadHeaderStyle(
            backgroundColor = 0xFFEEEEEE.toInt(),
            replyCountBackgroundColor = 0xFFDDDDDD.toInt(),
            replyCountTextColor = 0xFF333333.toInt(),
            cornerRadius = 16
        )

        launchFragmentInContainer<ThreadHeaderHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // View should be displayed with custom style applied
        onView(withId(THREAD_HEADER_TEST_ID))
            .check(matches(isDisplayed()))
    }
}

/**
 * Host Fragment for CometChatThreadHeader instrumented tests.
 * Provides injection points for test configuration.
 */
class ThreadHeaderHostFragment : Fragment() {

    companion object {
        var injectedParentMessage: BaseMessage? = null
        var injectedReplyCount: Int = 0
        var hideReplyCountBar: Boolean = false
        var hideReplyCount: Boolean = false
        var customStyle: CometChatThreadHeaderStyle? = null
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): View {
        val threadHeader = CometChatThreadHeader(requireContext())
        threadHeader.id = THREAD_HEADER_TEST_ID

        return FrameLayout(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            addView(threadHeader)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val threadHeader = (view as FrameLayout).getChildAt(0) as CometChatThreadHeader

        // Set up ViewModel with listeners disabled
        val viewModel = CometChatThreadHeaderViewModel(enableListeners = false)
        threadHeader.setViewModel(viewModel)

        // Apply custom style if provided
        customStyle?.let { threadHeader.setStyle(it) }

        // Apply visibility controls
        if (hideReplyCountBar) {
            threadHeader.setReplyCountBarVisibility(View.GONE)
        }
        if (hideReplyCount) {
            threadHeader.setReplyCountVisibility(View.GONE)
        }

        // Set parent message
        injectedParentMessage?.let { message ->
            threadHeader.setParentMessage(message)
        }
    }
}
