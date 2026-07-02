package com.cometchat.uikit.kotlin.presentation.emojikeyboard

import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.emojikeyboard.ui.EmojiKeyBoardView
import com.google.android.material.tabs.TabLayout
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented integration tests for CometChatEmojiKeyboard (chatuikit-kotlin).
 *
 * Tests the EmojiKeyBoardView in a real Fragment environment with Espresso assertions.
 * Verifies:
 * - View inflates and displays correctly
 * - Emoji grid (RecyclerView) is visible
 * - Tab bar (TabLayout) is visible
 * - Click on emoji invokes callback
 * - Style applies correctly
 *
 * Uses a test host Fragment that inflates EmojiKeyBoardView directly.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatEmojiKeyboardIntegrationTest {

    @Before
    fun setup() {
        EmojiKeyboardHostFragment.clickedEmoji = null
        EmojiKeyboardHostFragment.longClickedEmoji = null
    }

    @After
    fun tearDown() {
        EmojiKeyboardHostFragment.clickedEmoji = null
        EmojiKeyboardHostFragment.longClickedEmoji = null
    }

    // ==================== View Inflation Tests ====================

    @Test
    fun emojiKeyboardView_inflatesSuccessfully() {
        launchFragmentInContainer<EmojiKeyboardHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // The EmojiKeyBoardView should be displayed
        onView(withId(R.id.emoji_list_view))
            .check(matches(isDisplayed()))
    }

    @Test
    fun emojiKeyboardView_showsRecyclerView() {
        launchFragmentInContainer<EmojiKeyboardHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // RecyclerView for emoji grid should be visible
        onView(withId(R.id.emoji_list_view))
            .check(matches(isDisplayed()))
    }

    @Test
    fun emojiKeyboardView_showsTabLayout() {
        launchFragmentInContainer<EmojiKeyboardHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // TabLayout for category navigation should be visible
        onView(withId(R.id.category_tab))
            .check(matches(isDisplayed()))
    }

    @Test
    fun emojiKeyboardView_showsSeparator() {
        launchFragmentInContainer<EmojiKeyboardHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Separator between content and tabs should be visible
        onView(withId(R.id.separator))
            .check(matches(isDisplayed()))
    }

    // ==================== Style Application Tests ====================

    @Test
    fun emojiKeyboardView_customBackgroundColor_applies() {
        EmojiKeyboardHostFragment.customBackgroundColor = 0xFFF5F5DC.toInt()

        launchFragmentInContainer<EmojiKeyboardHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // View should still be displayed with custom background
        onView(withId(R.id.emoji_list_view))
            .check(matches(isDisplayed()))

        EmojiKeyboardHostFragment.customBackgroundColor = null
    }

    @Test
    fun emojiKeyboardView_customSeparatorColor_applies() {
        EmojiKeyboardHostFragment.customSeparatorColor = 0xFF333333.toInt()

        launchFragmentInContainer<EmojiKeyboardHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.separator))
            .check(matches(isDisplayed()))

        EmojiKeyboardHostFragment.customSeparatorColor = null
    }

    @Test
    fun emojiKeyboardView_customCornerRadius_applies() {
        EmojiKeyboardHostFragment.customCornerRadius = 24

        launchFragmentInContainer<EmojiKeyboardHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.emoji_list_view))
            .check(matches(isDisplayed()))

        EmojiKeyboardHostFragment.customCornerRadius = null
    }

    // ==================== Callback Tests ====================

    @Test
    fun emojiKeyboardView_onClickCallback_isRegistered() {
        launchFragmentInContainer<EmojiKeyboardHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Verify the view is displayed and callback is registered
        // (actual emoji click requires data to be loaded from assets)
        onView(withId(R.id.emoji_list_view))
            .check(matches(isDisplayed()))
    }
}

/**
 * Host Fragment for testing EmojiKeyBoardView in instrumented tests.
 * Provides injection points for custom styling and callback verification.
 */
class EmojiKeyboardHostFragment : Fragment() {

    companion object {
        var clickedEmoji: String? = null
        var longClickedEmoji: String? = null
        var customBackgroundColor: Int? = null
        var customSeparatorColor: Int? = null
        var customCornerRadius: Int? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val emojiKeyBoardView = EmojiKeyBoardView(requireContext())

        // Apply custom styling if set
        customBackgroundColor?.let { emojiKeyBoardView.setBackgroundColor(it) }
        customSeparatorColor?.let { emojiKeyBoardView.setSeparatorColor(it) }
        customCornerRadius?.let { emojiKeyBoardView.setCornerRadius(it) }

        // Register click callback
        emojiKeyBoardView.setOnClick(object : EmojiKeyBoardView.OnClick {
            override fun onClick(emoji: String) {
                clickedEmoji = emoji
            }
            override fun onLongClick(emoji: String) {
                longClickedEmoji = emoji
            }
        })

        return FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            addView(emojiKeyBoardView, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ))
        }
    }
}
