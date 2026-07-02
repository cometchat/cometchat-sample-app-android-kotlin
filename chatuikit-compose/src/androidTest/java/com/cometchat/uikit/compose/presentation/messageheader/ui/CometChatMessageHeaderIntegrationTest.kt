package com.cometchat.uikit.compose.presentation.messageheader.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.data.datasource.MessageHeaderDataSource
import com.cometchat.uikit.core.data.repository.MessageHeaderRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.GetGroupUseCase
import com.cometchat.uikit.core.domain.usecase.GetUserUseCase
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageHeaderViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose integration tests for CometChatMessageHeader.
 * Tests real ViewModel with fake DataSource → real Repository → real UseCases.
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest --tests "*CometChatMessageHeaderIntegrationTest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatMessageHeaderIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createViewModelWithFakeDataSource(
        fakeUser: User? = null,
        fakeGroup: Group? = null
    ): CometChatMessageHeaderViewModel {
        val dataSource = object : MessageHeaderDataSource {
            override suspend fun getUser(uid: String): User = fakeUser ?: MockFactory.createUser()
            override suspend fun getGroup(guid: String): Group = fakeGroup ?: MockFactory.createGroup()
        }
        val repository = MessageHeaderRepositoryImpl(dataSource)
        return CometChatMessageHeaderViewModel(
            getUserUseCase = GetUserUseCase(repository),
            getGroupUseCase = GetGroupUseCase(repository),
            enableListeners = false
        )
    }

    @Test
    fun fullChain_userConversation_displaysCorrectly() {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
        val vm = createViewModelWithFakeDataSource(fakeUser = user)
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
    fun fullChain_groupConversation_displaysCorrectly() {
        val group = MockFactory.createGroup(guid = "group-1", name = "Developers", membersCount = 15)
        val vm = createViewModelWithFakeDataSource(fakeGroup = group)
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

    @Test
    fun fullChain_switchingUserToGroup_updatesUI() {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        val group = MockFactory.createGroup(guid = "group-1", name = "Developers", membersCount = 10)
        val vm = createViewModelWithFakeDataSource(fakeUser = user, fakeGroup = group)
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

        // Switch to group
        composeTestRule.runOnUiThread {
            vm.setGroup(group)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Developers").assertIsDisplayed()
    }

    @Test
    fun fullChain_refreshUser_updatesUI() {
        val initialUser = MockFactory.createUser(uid = "user-1", name = "Alice Old", status = CometChatConstants.USER_STATUS_OFFLINE)
        val refreshedUser = MockFactory.createUser(uid = "user-1", name = "Alice Updated", status = CometChatConstants.USER_STATUS_ONLINE)
        val vm = createViewModelWithFakeDataSource(fakeUser = refreshedUser)
        vm.setUser(initialUser)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = initialUser
                )
            }
        }

        composeTestRule.onNodeWithText("Alice Old").assertIsDisplayed()

        // Refresh user
        composeTestRule.runOnUiThread {
            vm.refreshUser("user-1")
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Alice Updated").assertIsDisplayed()
    }
}
