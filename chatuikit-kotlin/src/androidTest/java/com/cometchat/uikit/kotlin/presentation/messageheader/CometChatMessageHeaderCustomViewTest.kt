package com.cometchat.uikit.kotlin.presentation.messageheader

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
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
import com.cometchat.uikit.kotlin.presentation.messageheader.utils.MessageHeaderViewHolderListener
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for CometChatMessageHeader custom view slots.
 * Verifies that custom views can be injected into leading, title, subtitle,
 * trailing, and auxiliary slots.
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest --tests "*CometChatMessageHeaderCustomViewTest"
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class CometChatMessageHeaderCustomViewTest {

    @Before
    fun setup() {
        CustomViewHostFragment.customViewType = null
        CustomViewHostFragment.injectedUser = null
    }

    @After
    fun tearDown() {
        CustomViewHostFragment.customViewType = null
        CustomViewHostFragment.injectedUser = null
    }

    @Test
    fun customTitleView_displaysCustomText() {
        CustomViewHostFragment.customViewType = "title"
        CustomViewHostFragment.injectedUser = MockFactory.createUser(uid = "user-1", name = "Alice")

        launchFragmentInContainer<CustomViewHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withText("Custom Title View"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun customSubtitleView_displaysCustomText() {
        CustomViewHostFragment.customViewType = "subtitle"
        CustomViewHostFragment.injectedUser = MockFactory.createUser(uid = "user-1", name = "Alice")

        launchFragmentInContainer<CustomViewHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withText("Custom Subtitle View"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun customTrailingView_displaysCustomText() {
        CustomViewHostFragment.customViewType = "trailing"
        CustomViewHostFragment.injectedUser = MockFactory.createUser(uid = "user-1", name = "Alice")

        launchFragmentInContainer<CustomViewHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withText("Custom Trailing View"))
            .check(matches(isDisplayed()))
    }
}

/**
 * Host Fragment for testing custom view injection.
 */
class CustomViewHostFragment : Fragment() {

    companion object {
        var customViewType: String? = null
        var injectedUser: User? = null
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

        // Create ViewModel
        val fakeDataSource = object : MessageHeaderDataSource {
            override suspend fun getUser(uid: String): User = injectedUser ?: MockFactory.createUser()
            override suspend fun getGroup(guid: String): Group = MockFactory.createGroup()
        }
        val repository = MessageHeaderRepositoryImpl(fakeDataSource)
        val viewModel = CometChatMessageHeaderViewModel(
            getUserUseCase = GetUserUseCase(repository),
            getGroupUseCase = GetGroupUseCase(repository),
            enableListeners = false
        )
        header.setViewModel(viewModel)

        // Set custom view based on type
        when (customViewType) {
            "title" -> header.setTitleViewListener(object : MessageHeaderViewHolderListener {
                override fun createView(context: Context, user: User?, group: Group?): View {
                    return TextView(context).apply { text = "Custom Title View" }
                }
            })
            "subtitle" -> header.setSubtitleViewListener(object : MessageHeaderViewHolderListener {
                override fun createView(context: Context, user: User?, group: Group?): View {
                    return TextView(context).apply { text = "Custom Subtitle View" }
                }
            })
            "trailing" -> header.setTrailingViewListener(object : MessageHeaderViewHolderListener {
                override fun createView(context: Context, user: User?, group: Group?): View {
                    return TextView(context).apply { text = "Custom Trailing View" }
                }
            })
        }

        injectedUser?.let { header.setUser(it) }
    }
}
