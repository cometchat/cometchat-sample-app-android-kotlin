package com.cometchat.uikit.compose.presentation.messageheader.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
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
 * Compose UI tests for CometChatMessageHeader custom view slots.
 * Verifies that custom composable slots override default content.
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest --tests "*CometChatMessageHeaderCustomViewTest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatMessageHeaderCustomViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createViewModel(): CometChatMessageHeaderViewModel {
        return CometChatMessageHeaderViewModel(
            getUserUseCase = mock<GetUserUseCase>(),
            getGroupUseCase = mock<GetGroupUseCase>(),
            enableListeners = false
        )
    }

    @Test
    fun customLeadingView_replacesDefaultAvatar() {
        val vm = createViewModel()
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        vm.setUser(user)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user,
                    leadingView = { _, _ ->
                        Text("Custom Leading")
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Custom Leading").assertIsDisplayed()
    }

    @Test
    fun customTitleView_replacesDefaultTitle() {
        val vm = createViewModel()
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        vm.setUser(user)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user,
                    titleView = { u, _ ->
                        Text("Custom: ${u?.name}")
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Custom: Alice").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alice").assertDoesNotExist()
    }

    @Test
    fun customSubtitleView_replacesDefaultSubtitle() {
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
                        Text("Custom Subtitle Content")
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Custom Subtitle Content").assertIsDisplayed()
    }

    @Test
    fun customTrailingView_displaysInTrailingSlot() {
        val vm = createViewModel()
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        vm.setUser(user)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user,
                    trailingView = { _, _ ->
                        Text("Trailing Content")
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Trailing Content").assertIsDisplayed()
    }

    @Test
    fun customAuxiliaryView_replacesDefaultCallButtons() {
        val vm = createViewModel()
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        vm.setUser(user)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user,
                    auxiliaryView = { _, _ ->
                        Text("Custom Auxiliary")
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Custom Auxiliary").assertIsDisplayed()
    }

    @Test
    fun customItemView_replacesEntireHeaderContent() {
        val vm = createViewModel()
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        vm.setUser(user)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user,
                    itemView = { u, _ ->
                        Text("Fully Custom Header for ${u?.name}")
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Fully Custom Header for Alice").assertIsDisplayed()
        // Default content should not exist
        composeTestRule.onNodeWithText("Alice").assertDoesNotExist()
    }

    @Test
    fun customItemView_forGroup_receivesGroupData() {
        val vm = createViewModel()
        val group = MockFactory.createGroup(guid = "group-1", name = "Developers", membersCount = 10)
        vm.setGroup(group)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    group = group,
                    itemView = { _, g ->
                        Text("Group: ${g?.name} (${g?.membersCount})")
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Group: Developers (10)").assertIsDisplayed()
    }
}
