package com.cometchat.uikit.compose.presentation.messageheader.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.domain.usecase.GetGroupUseCase
import com.cometchat.uikit.core.domain.usecase.GetUserUseCase
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageHeaderViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

/**
 * Compose UI tests for CometChatMessageHeader composable.
 * Tests rendering, visibility, and content display.
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest --tests "*CometChatMessageHeaderListTest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatMessageHeaderListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createViewModel(): CometChatMessageHeaderViewModel {
        val getUserUseCase = mock<GetUserUseCase>()
        val getGroupUseCase = mock<GetGroupUseCase>()
        return CometChatMessageHeaderViewModel(
            getUserUseCase = getUserUseCase,
            getGroupUseCase = getGroupUseCase,
            enableListeners = false
        )
    }

    // ==================== User Display ====================

    @Test
    fun userConversation_displaysUserName() {
        val vm = createViewModel()
        val user = MockFactory.createUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
        vm.setUser(user)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user
                )
            }
        }

        composeTestRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
    }

    @Test
    fun userOnline_displaysOnlineStatus() {
        val vm = createViewModel()
        val user = MockFactory.createUser(uid = "user-1", name = "Alice", status = CometChatConstants.USER_STATUS_ONLINE)
        vm.setUser(user)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user
                )
            }
        }

        composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
    }

    // ==================== Group Display ====================

    @Test
    fun groupConversation_displaysGroupName() {
        val vm = createViewModel()
        val group = MockFactory.createGroup(guid = "group-1", name = "Developers", membersCount = 10)
        vm.setGroup(group)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    group = group
                )
            }
        }

        composeTestRule.onNodeWithText("Developers").assertIsDisplayed()
    }

    // ==================== Back Button ====================

    @Test
    fun backButton_isDisplayedWhenNotHidden() {
        val vm = createViewModel()
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        vm.setUser(user)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user,
                    hideBackButton = false
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun backButton_isHiddenWhenFlagSet() {
        val vm = createViewModel()
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        vm.setUser(user)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user,
                    hideBackButton = true
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").assertDoesNotExist()
    }

    // ==================== Custom Views ====================

    @Test
    fun customTitleView_displaysCustomContent() {
        val vm = createViewModel()
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        vm.setUser(user)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user,
                    titleView = { _, _ ->
                        androidx.compose.material3.Text("Custom Title")
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Custom Title").assertIsDisplayed()
        // Default title should not be shown
        composeTestRule.onNodeWithText("Alice").assertDoesNotExist()
    }

    @Test
    fun customSubtitleView_displaysCustomContent() {
        val vm = createViewModel()
        val user = MockFactory.createUser(uid = "user-1", name = "Alice", status = CometChatConstants.USER_STATUS_ONLINE)
        vm.setUser(user)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user,
                    subtitleView = { _, _ ->
                        androidx.compose.material3.Text("Custom Subtitle")
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Custom Subtitle").assertIsDisplayed()
    }

    @Test
    fun customItemView_replacesEntireContent() {
        val vm = createViewModel()
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        vm.setUser(user)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user,
                    itemView = { _, _ ->
                        androidx.compose.material3.Text("Completely Custom Header")
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Completely Custom Header").assertIsDisplayed()
        // Default content should not be shown
        composeTestRule.onNodeWithText("Alice").assertDoesNotExist()
    }
}
