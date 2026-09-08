package com.cometchat.uikit.kotlin.presentation.threadheader

import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.UIKitSettings
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatThreadHeaderViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.messagelist.MessageListTestSdkHelper
import com.cometchat.uikit.kotlin.presentation.threadheader.ui.CometChatThreadHeader
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.whenever

private val SUB_HEADER_TEST_ID = View.generateViewId()

/**
 * Instrumented tests for the thread-subscription bell on [CometChatThreadHeader] (chatuikit-kotlin).
 *
 * Covers the rendering contract for P3 (ENG-37569):
 * - the bell renders only when the `enableThreadSubscription` feature gate is on, a parent message
 *   is present, and the per-control visibility is not turned off;
 * - `setThreadSubscriptionVisibility(GONE)` hides it even when the gate is on.
 *
 * The gate is set by initializing [CometChatUIKit] with the desired flag (its `init` assigns the
 * settings synchronously). State reads resolve to UNKNOWN for an unseeded thread, so the control
 * renders as un-followed/enabled — which is all these visibility checks depend on.
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest --tests "*CometChatThreadHeaderSubscriptionInstrumentedTest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatThreadHeaderSubscriptionInstrumentedTest {

    @Before
    fun setup() {
        MessageListTestSdkHelper.ensureInitialized()
        SubscriptionHeaderHostFragment.reset()
    }

    /**
     * The default fixture roots the thread in a group. [oneToOneParentMessage] covers the 1-1 case,
     * where the bell is offered too (ENG-38903) — the control follows threading's own scope.
     */
    private fun rootParentMessage(id: Long = 200L): BaseMessage {
        val message = MockFactory.createTextMessage(
            id = id,
            senderUid = "user-1",
            text = "Parent message",
            receiverId = "group-1",
            receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
        )
        whenever(message.replyCount).thenReturn(3)
        return message
    }

    private fun oneToOneParentMessage(id: Long = 200L): BaseMessage {
        val message = MockFactory.createTextMessage(
            id = id,
            senderUid = "user-1",
            text = "Parent message"
        )
        whenever(message.replyCount).thenReturn(3)
        return message
    }

    @Test
    fun bellIsHiddenWhenFeatureGateOff() {
        SubscriptionHeaderHostFragment.enableThreadSubscription = false
        SubscriptionHeaderHostFragment.injectedParentMessage = rootParentMessage()

        launchFragmentInContainer<SubscriptionHeaderHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.iv_thread_subscription))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun bellIsVisibleWhenFeatureGateOnWithParentMessage() {
        SubscriptionHeaderHostFragment.enableThreadSubscription = true
        SubscriptionHeaderHostFragment.injectedParentMessage = rootParentMessage()

        launchFragmentInContainer<SubscriptionHeaderHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.iv_thread_subscription))
            .check(matches(isDisplayed()))
    }


    @Test
    fun setThreadSubscriptionVisibilityGoneHidesBellEvenWhenGateOn() {
        SubscriptionHeaderHostFragment.enableThreadSubscription = true
        SubscriptionHeaderHostFragment.injectedParentMessage = rootParentMessage()
        SubscriptionHeaderHostFragment.threadSubscriptionVisibility = View.GONE

        launchFragmentInContainer<SubscriptionHeaderHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.iv_thread_subscription))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun bellIsHiddenWhenNoParentMessage() {
        SubscriptionHeaderHostFragment.enableThreadSubscription = true
        SubscriptionHeaderHostFragment.injectedParentMessage = null

        launchFragmentInContainer<SubscriptionHeaderHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.iv_thread_subscription))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }
}

/**
 * Host fragment for the thread-subscription bell tests. Sets the feature gate via [CometChatUIKit]
 * before the header is built, then configures the header.
 */
class SubscriptionHeaderHostFragment : Fragment() {

    companion object {
        var injectedParentMessage: BaseMessage? = null
        var enableThreadSubscription: Boolean = false
        var threadSubscriptionVisibility: Int? = null

        fun reset() {
            injectedParentMessage = null
            enableThreadSubscription = false
            threadSubscriptionVisibility = null
        }
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): View {
        // Set the feature gate before the header reads it. init() assigns authenticationSettings
        // synchronously, so isThreadSubscriptionEnabled() reflects this immediately.
        val settings = UIKitSettings.UIKitSettingsBuilder()
            .setAppId("278059f315a564b4")
            .setRegion("in")
            .setAuthKey("5bb2416b7eb003c1f94234c26178a4b053c66b97")
            .setEnableThreadSubscription(enableThreadSubscription)
            .build()
        CometChatUIKit.init(requireContext(), settings, null)

        val threadHeader = CometChatThreadHeader(requireContext())
        threadHeader.id = SUB_HEADER_TEST_ID

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
        threadHeader.setViewModel(CometChatThreadHeaderViewModel(enableListeners = false))
        threadSubscriptionVisibility?.let { threadHeader.setThreadSubscriptionVisibility(it) }
        injectedParentMessage?.let { threadHeader.setParentMessage(it) }
    }
}
