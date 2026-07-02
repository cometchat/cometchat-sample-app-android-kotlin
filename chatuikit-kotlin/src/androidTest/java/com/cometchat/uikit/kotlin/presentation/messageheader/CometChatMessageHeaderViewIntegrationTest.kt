package com.cometchat.uikit.kotlin.presentation.messageheader

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
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.data.datasource.MessageHeaderDataSource
import com.cometchat.uikit.core.data.repository.MessageHeaderRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.GetGroupUseCase
import com.cometchat.uikit.core.domain.usecase.GetUserUseCase
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageHeaderViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.messageheader.ui.CometChatMessageHeader
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented integration tests for CometChatMessageHeader (chatuikit-kotlin).
 * Real View inflated in Fragment, Espresso assertions, fake DataSource only.
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest --tests "*CometChatMessageHeaderViewIntegrationTest"
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatMessageHeaderViewIntegrationTest {

    @Before
    fun setup() {
        MessageHeaderHostFragment.injectedUser = null
        MessageHeaderHostFragment.injectedGroup = null
    }

    @After
    fun tearDown() {
        MessageHeaderHostFragment.injectedUser = null
        MessageHeaderHostFragment.injectedGroup = null
    }

    @Test
    fun userConversation_displaysUserName() {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
        MessageHeaderHostFragment.injectedUser = user

        launchFragmentInContainer<MessageHeaderHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.tvMessageHeaderName))
            .check(matches(isDisplayed()))
            .check(matches(withText("Alice Johnson")))
    }

    @Test
    fun userOnline_displaysOnlineSubtitle() {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice", status = CometChatConstants.USER_STATUS_ONLINE)
        MessageHeaderHostFragment.injectedUser = user

        launchFragmentInContainer<MessageHeaderHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.tvMessageHeaderSubtitle))
            .check(matches(isDisplayed()))
    }

    @Test
    fun groupConversation_displaysGroupName() {
        val group = MockFactory.createGroup(guid = "group-1", name = "Developers", membersCount = 10)
        MessageHeaderHostFragment.injectedGroup = group

        launchFragmentInContainer<MessageHeaderHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.tvMessageHeaderName))
            .check(matches(isDisplayed()))
            .check(matches(withText("Developers")))
    }

    @Test
    fun groupConversation_displaysMemberCount() {
        val group = MockFactory.createGroup(guid = "group-1", name = "Developers", membersCount = 10)
        MessageHeaderHostFragment.injectedGroup = group

        launchFragmentInContainer<MessageHeaderHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.tvMessageHeaderSubtitle))
            .check(matches(isDisplayed()))
    }

    @Test
    fun backButton_isVisibleWhenSet() {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        MessageHeaderHostFragment.injectedUser = user
        MessageHeaderHostFragment.showBackButton = true

        launchFragmentInContainer<MessageHeaderHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.ivMessageHeaderBack))
            .check(matches(isDisplayed()))

        MessageHeaderHostFragment.showBackButton = false
    }

    @Test
    fun avatar_isDisplayed() {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        MessageHeaderHostFragment.injectedUser = user

        launchFragmentInContainer<MessageHeaderHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.messageHeaderAvatarView))
            .check(matches(isDisplayed()))
    }
}

/**
 * Host Fragment for injecting test data into CometChatMessageHeader.
 */
open class MessageHeaderHostFragment : Fragment() {

    companion object {
        var injectedUser: User? = null
        var injectedGroup: Group? = null
        var showBackButton: Boolean = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val header = CometChatMessageHeader(requireContext())
        return FrameLayout(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            addView(header)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val header = (view as FrameLayout).getChildAt(0) as CometChatMessageHeader

        // Create ViewModel with fake DataSource
        val fakeDataSource = object : MessageHeaderDataSource {
            override suspend fun getUser(uid: String): User = injectedUser ?: MockFactory.createUser()
            override suspend fun getGroup(guid: String): Group = injectedGroup ?: MockFactory.createGroup()
        }
        val repository = MessageHeaderRepositoryImpl(fakeDataSource)
        val getUserUseCase = GetUserUseCase(repository)
        val getGroupUseCase = GetGroupUseCase(repository)
        val viewModel = CometChatMessageHeaderViewModel(
            getUserUseCase = getUserUseCase,
            getGroupUseCase = getGroupUseCase,
            enableListeners = false
        )
        header.setViewModel(viewModel)

        if (showBackButton) {
            header.setBackButtonVisibility(View.VISIBLE)
        }

        injectedUser?.let { header.setUser(it) }
        injectedGroup?.let { header.setGroup(it) }
    }
}
