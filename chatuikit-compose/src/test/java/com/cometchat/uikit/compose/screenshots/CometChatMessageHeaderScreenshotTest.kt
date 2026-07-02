package com.cometchat.uikit.compose.screenshots

import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil.Coil
import coil.ImageLoader
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.messageheader.ui.CometChatMessageHeader
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.domain.usecase.GetGroupUseCase
import com.cometchat.uikit.core.domain.usecase.GetUserUseCase
import com.cometchat.uikit.core.viewmodel.CometChatMessageHeaderViewModel
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.compose.presentation.utils.RoborazziConfig
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Roborazzi screenshot tests for CometChatMessageHeader (chatuikit-compose).
 * Captures golden images for all visual states using composable rendering.
 *
 * Uses the same approach as real-world usage — Activity handles layout naturally.
 * Each test sets composable content and captures the rendered view.
 *
 * Run to record:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*CometChatMessageHeaderScreenshotTest"
 *
 * Run to verify:
 *   ./gradlew :chatuikit-compose:verifyRoborazziDebug --tests "*CometChatMessageHeaderScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatMessageHeaderScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/messageheader"
        )
    )

    @Before
    fun setupFakeImageLoader() {
        val context = RuntimeEnvironment.getApplication()
        val interceptor = object : Interceptor {
            override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
                return SuccessResult(
                    drawable = ColorDrawable(android.graphics.Color.TRANSPARENT),
                    request = chain.request,
                    dataSource = coil.decode.DataSource.MEMORY
                )
            }
        }
        val imageLoader = ImageLoader.Builder(context)
            .components { add(interceptor) }
            .crossfade(false)
            .build()
        Coil.setImageLoader(imageLoader)
    }

    // ==================== Helpers ====================

    private fun createViewModel(): CometChatMessageHeaderViewModel {
        val getUserUseCase = mock<GetUserUseCase>()
        val getGroupUseCase = mock<GetGroupUseCase>()
        return CometChatMessageHeaderViewModel(
            getUserUseCase = getUserUseCase,
            getGroupUseCase = getGroupUseCase,
            enableListeners = false
        )
    }

    private fun createMockUser(
        uid: String = "user-1",
        name: String = "Test User",
        status: String = CometChatConstants.USER_STATUS_ONLINE,
        isBlockedByMe: Boolean = false
    ): User {
        val user = mock<User>()
        whenever(user.uid).thenReturn(uid)
        whenever(user.name).thenReturn(name)
        whenever(user.status).thenReturn(status)
        whenever(user.isBlockedByMe).thenReturn(isBlockedByMe)
        whenever(user.isHasBlockedMe).thenReturn(false)
        return user
    }

    private fun createMockGroup(
        guid: String = "group-1",
        name: String = "Test Group",
        type: String = CometChatConstants.GROUP_TYPE_PUBLIC,
        membersCount: Int = 5
    ): Group {
        val group = mock<Group>()
        whenever(group.guid).thenReturn(guid)
        whenever(group.name).thenReturn(name)
        whenever(group.groupType).thenReturn(type)
        whenever(group.membersCount).thenReturn(membersCount)
        return group
    }

    /**
     * Launches an ActivityScenario, sets composable content, and captures a screenshot.
     * Lets the Activity handle layout naturally — simplified approach matching real-world usage.
     */
    private fun captureComposable(content: @Composable () -> Unit) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setContent { content() }
        }
        scenario.onActivity { activity ->
            val composeView = activity.window.decorView
                .findViewById<ViewGroup>(android.R.id.content)
                .getChildAt(0)
            composeView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== UI States ====================

    @Test
    fun stateUserOnline() {
        val vm = createViewModel()
        val user = createMockUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
        vm.setUser(user)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user
                )
            }
        }
    }

    @Test
    fun stateUserLastSeen() {
        val vm = createViewModel()
        val user = createMockUser(uid = "user-1", name = "Bob Smith", status = CometChatConstants.USER_STATUS_OFFLINE)
        whenever(user.lastActiveAt).thenReturn(System.currentTimeMillis() / 1000 - 3600) // 1 hour ago
        vm.setUser(user)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user
                )
            }
        }
    }

    @Test
    fun stateGroupPublic() {
        val vm = createViewModel()
        val group = createMockGroup(guid = "group-1", name = "Developers", type = CometChatConstants.GROUP_TYPE_PUBLIC, membersCount = 15)
        vm.setGroup(group)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    group = group
                )
            }
        }
    }

    @Test
    fun stateGroupPrivate() {
        val vm = createViewModel()
        val group = createMockGroup(guid = "group-1", name = "Private Team", type = CometChatConstants.GROUP_TYPE_PRIVATE, membersCount = 8)
        vm.setGroup(group)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    group = group
                )
            }
        }
    }

    @Test
    fun stateGroupPassword() {
        val vm = createViewModel()
        val group = createMockGroup(guid = "group-1", name = "Protected Room", type = CometChatConstants.GROUP_TYPE_PASSWORD, membersCount = 3)
        vm.setGroup(group)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    group = group
                )
            }
        }
    }

    // ==================== Visibility Toggles ====================

    @Test
    fun visibilityWithBackButton() {
        val vm = createViewModel()
        val user = createMockUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
        vm.setUser(user)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user,
                    hideBackButton = false
                )
            }
        }
    }

    @Test
    fun visibilityNoBackButton() {
        val vm = createViewModel()
        val user = createMockUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
        vm.setUser(user)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user,
                    hideBackButton = true
                )
            }
        }
    }

    @Test
    fun visibilityNoUserStatus() {
        val vm = createViewModel()
        val user = createMockUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
        vm.setUser(user)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user,
                    hideUserStatus = true
                )
            }
        }
    }

    @Test
    fun visibilityNoGroupStatus() {
        val vm = createViewModel()
        val group = createMockGroup(guid = "group-1", name = "Private Team", type = CometChatConstants.GROUP_TYPE_PRIVATE, membersCount = 8)
        vm.setGroup(group)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    group = group,
                    hideGroupStatus = true
                )
            }
        }
    }

    // ==================== Dark Theme ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateUserOnlineDark() {
        val vm = createViewModel()
        val user = createMockUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
        vm.setUser(user)

        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateGroupPrivateDark() {
        val vm = createViewModel()
        val group = createMockGroup(guid = "group-1", name = "Private Team", type = CometChatConstants.GROUP_TYPE_PRIVATE, membersCount = 8)
        vm.setGroup(group)

        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    group = group
                )
            }
        }
    }

    // ==================== Content Variants ====================

    @Test
    fun contentLongUserName() {
        val vm = createViewModel()
        val user = createMockUser(uid = "user-1", name = "A Very Long User Name That Should Be Truncated With Ellipsis In The Header", status = CometChatConstants.USER_STATUS_ONLINE)
        vm.setUser(user)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user
                )
            }
        }
    }

    @Test
    fun contentHighMemberCount() {
        val vm = createViewModel()
        val group = createMockGroup(guid = "group-1", name = "Large Community", membersCount = 9999)
        vm.setGroup(group)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    group = group
                )
            }
        }
    }

    @Test
    fun contentSingleMember() {
        val vm = createViewModel()
        val group = createMockGroup(guid = "group-1", name = "Solo Group", membersCount = 1)
        vm.setGroup(group)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    group = group
                )
            }
        }
    }

    // ==================== Blocked User ====================

    @Test
    fun stateUserBlocked() {
        val vm = createViewModel()
        val user = createMockUser(uid = "user-1", name = "Blocked User", isBlockedByMe = true)
        vm.setUser(user)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user
                )
            }
        }
    }

    // ==================== Menu / Options ====================

    @Test
    fun menuWithOptions() {
        val vm = createViewModel()
        val user = createMockUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
        vm.setUser(user)

        val menuItems = listOf(
            com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem(id = "search", name = "Search"),
            com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem(id = "mute", name = "Mute Notifications"),
            com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem(id = "block", name = "Block User")
        )

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user,
                    hideMenuIcon = false,
                    options = menuItems
                )
            }
        }
    }

    @Test
    fun menuIconVisibleWithOptions() {
        val vm = createViewModel()
        val group = createMockGroup(guid = "group-1", name = "Developers", membersCount = 10)
        vm.setGroup(group)

        val menuItems = listOf(
            com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem(id = "info", name = "Group Info"),
            com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem(id = "leave", name = "Leave Group")
        )

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    group = group,
                    hideMenuIcon = false,
                    options = menuItems
                )
            }
        }
    }

    @Test
    fun menuIconHiddenWithoutOptions() {
        val vm = createViewModel()
        val user = createMockUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
        vm.setUser(user)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    messageHeaderViewModel = vm,
                    user = user,
                    hideMenuIcon = true,
                    options = null
                )
            }
        }
    }
}
