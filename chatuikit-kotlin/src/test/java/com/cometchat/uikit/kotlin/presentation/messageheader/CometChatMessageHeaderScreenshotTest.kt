package com.cometchat.uikit.kotlin.presentation.messageheader

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.usecase.GetGroupUseCase
import com.cometchat.uikit.core.domain.usecase.GetUserUseCase
import com.cometchat.uikit.core.viewmodel.CometChatMessageHeaderViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.messageheader.style.CometChatMessageHeaderStyle
import com.cometchat.uikit.kotlin.presentation.messageheader.ui.CometChatMessageHeader
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.kotlin.presentation.utils.RoborazziConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/**
 * Roborazzi screenshot tests for CometChatMessageHeader (chatuikit-kotlin).
 * Captures golden images for all visual states.
 *
 * Uses the same approach as real-world usage:
 *   activity.setContentView(CometChatMessageHeader(this))
 *
 * The activity handles layout naturally, ensuring proper rendering of all
 * elements (avatar, status indicator, title, subtitle, menu icon).
 *
 * Run to record:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*CometChatMessageHeaderScreenshotTest"
 *
 * Run to verify:
 *   ./gradlew :chatuikit-kotlin:verifyRoborazziDebug --tests "*CometChatMessageHeaderScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatMessageHeaderScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/messageheader"
        )
    )

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

    private fun createUser(
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

    private fun createGroup(
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
     * Launches an ActivityScenario, inflates CometChatMessageHeader,
     * configures it via the provided block, and captures a screenshot.
     *
     * Lets the Activity handle layout naturally — simplified approach
     * matching real-world usage.
     */
    private fun launchAndCapture(configure: (ComponentActivity) -> CometChatMessageHeader) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            val view = configure(activity)

            // Set content view just like real usage — Activity handles layout naturally
            activity.setContentView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )

            ShadowLooper.idleMainLooper()
        }

        // Let the looper process all pending messages (layout, draw, ViewModel emissions)
        ShadowLooper.idleMainLooper()

        // Capture from the decor view to include all drawn decorations
        scenario.onActivity { activity ->
            fixAvatarCircularRendering(activity.window.decorView)
            activity.window.decorView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    /**
     * Fixes avatar circular rendering for Robolectric screenshot tests.
     * In Robolectric's native graphics mode, CometChatAvatar has its outer radius
     * set to 0 by Utils.initMaterialCard(). This method traverses the view hierarchy
     * and sets radius to Float.MAX_VALUE on CometChatAvatar instances.
     */
    private fun fixAvatarCircularRendering(root: View) {
        if (root is CometChatAvatar) {
            root.radius = Float.MAX_VALUE
        }
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                fixAvatarCircularRendering(root.getChildAt(i))
            }
        }
    }

    // ==================== UI States ====================

    @Test
    fun stateUserOnline() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            val user = createUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
            vm.setUser(user)
            header
        }
    }

    @Test
    fun stateUserLastSeen() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            val user = createUser(uid = "user-1", name = "Bob Smith", status = CometChatConstants.USER_STATUS_OFFLINE)
            whenever(user.lastActiveAt).thenReturn(System.currentTimeMillis() / 1000 - 3600) // 1 hour ago
            vm.setUser(user)
            header
        }
    }

    @Test
    fun stateGroupPublic() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            val group = createGroup(guid = "group-1", name = "Developers", type = CometChatConstants.GROUP_TYPE_PUBLIC, membersCount = 15)
            vm.setGroup(group)
            header
        }
    }

    @Test
    fun stateGroupPrivate() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            val group = createGroup(guid = "group-1", name = "Private Team", type = CometChatConstants.GROUP_TYPE_PRIVATE, membersCount = 8)
            vm.setGroup(group)
            header
        }
    }

    @Test
    fun stateGroupPassword() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            val group = createGroup(guid = "group-1", name = "Protected Room", type = CometChatConstants.GROUP_TYPE_PASSWORD, membersCount = 3)
            vm.setGroup(group)
            header
        }
    }

    // ==================== Visibility Toggles ====================

    @Test
    fun visibilityWithBackButton() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            header.setBackButtonVisibility(View.VISIBLE)
            val user = createUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
            vm.setUser(user)
            header
        }
    }

    @Test
    fun visibilityNoUserStatus() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            header.setUserStatusVisibility(View.GONE)
            val user = createUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
            vm.setUser(user)
            header
        }
    }

    @Test
    fun visibilityNoGroupStatus() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            header.setGroupStatusVisibility(View.GONE)
            val group = createGroup(guid = "group-1", name = "Private Team", type = CometChatConstants.GROUP_TYPE_PRIVATE, membersCount = 8)
            vm.setGroup(group)
            header
        }
    }

    @Test
    fun visibilityWithMenuIcon() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            header.setMenuIconVisibility(View.VISIBLE)
            val user = createUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
            vm.setUser(user)
            header
        }
    }

    // ==================== Dark Theme ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateUserOnlineDark() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            val user = createUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
            vm.setUser(user)
            header
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateGroupPrivateDark() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            val group = createGroup(guid = "group-1", name = "Private Team", type = CometChatConstants.GROUP_TYPE_PRIVATE, membersCount = 8)
            vm.setGroup(group)
            header
        }
    }

    // ==================== Custom Style ====================

    @Test
    fun styleCustomBackground() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            val customStyle = CometChatMessageHeaderStyle(
                backgroundColor = Color.parseColor("#F5F5DC")
            )
            header.setStyle(customStyle)
            val user = createUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
            vm.setUser(user)
            header
        }
    }

    @Test
    fun stylingCustomColors() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            val customStyle = CometChatMessageHeaderStyle(
                backgroundColor = Color.parseColor("#1A1A2E"),
                titleTextColor = Color.WHITE,
                subtitleTextColor = Color.parseColor("#AAAAAA"),
                typingIndicatorTextColor = Color.parseColor("#00FF00"),
                backIconTint = Color.WHITE,
                menuIconTint = Color.WHITE
            )
            header.setStyle(customStyle)
            header.setBackButtonVisibility(View.VISIBLE)
            val user = createUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
            vm.setUser(user)
            header
        }
    }

    @Test
    fun styleAllProperties() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            val customStyle = CometChatMessageHeaderStyle(
                backgroundColor = Color.parseColor("#1A1A2E"),
                strokeColor = Color.parseColor("#333333"),
                strokeWidth = 2,
                cornerRadius = 16,
                titleTextColor = Color.WHITE,
                subtitleTextColor = Color.parseColor("#AAAAAA"),
                typingIndicatorTextColor = Color.parseColor("#00FF00"),
                backIconTint = Color.WHITE,
                menuIconTint = Color.WHITE,
                newChatIconTint = Color.parseColor("#6851D6"),
                chatHistoryIconTint = Color.parseColor("#6851D6"),
                videoCallIconTint = Color.parseColor("#6851D6"),
                voiceCallIconTint = Color.parseColor("#6851D6")
            )
            header.setStyle(customStyle)
            header.setBackButtonVisibility(View.VISIBLE)
            header.setMenuIconVisibility(View.VISIBLE)
            val user = createUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
            vm.setUser(user)
            header
        }
    }

    // ==================== Content Variants ====================

    @Test
    fun contentLongUserName() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            val user = createUser(uid = "user-1", name = "A Very Long User Name That Should Be Truncated With Ellipsis", status = CometChatConstants.USER_STATUS_ONLINE)
            vm.setUser(user)
            header
        }
    }

    @Test
    fun contentLongGroupName() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            val group = createGroup(guid = "group-1", name = "A Very Long Group Name That Should Be Truncated With Ellipsis In The Header", membersCount = 250)
            vm.setGroup(group)
            header
        }
    }

    @Test
    fun contentSingleMember() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            val group = createGroup(guid = "group-1", name = "Solo Group", membersCount = 1)
            vm.setGroup(group)
            header
        }
    }

    @Test
    fun contentHighMemberCount() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            val group = createGroup(guid = "group-1", name = "Large Community", membersCount = 9999)
            vm.setGroup(group)
            header
        }
    }

    // ==================== Blocked User ====================

    @Test
    fun stateUserBlocked() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            val user = createUser(uid = "user-1", name = "Blocked User", isBlockedByMe = true)
            vm.setUser(user)
            header
        }
    }

    // ==================== Menu / Options ====================

    @Test
    fun menuWithOptions() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            val user = createUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
            vm.setUser(user)
            val menuItems = listOf(
                CometChatPopupMenu.MenuItem("search", "Search", null, null, 0, 0, 0, 0) {},
                CometChatPopupMenu.MenuItem("mute", "Mute Notifications", null, null, 0, 0, 0, 0) {},
                CometChatPopupMenu.MenuItem("block", "Block User", null, null, 0, 0, 0, 0) {}
            )
            header.setMenuOptions(menuItems)
            header
        }
    }

    @Test
    fun menuIconVisibleWithOptions() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            val group = createGroup(guid = "group-1", name = "Developers", membersCount = 10)
            vm.setGroup(group)
            val menuItems = listOf(
                CometChatPopupMenu.MenuItem("info", "Group Info", null, null, 0, 0, 0, 0) {},
                CometChatPopupMenu.MenuItem("leave", "Leave Group", null, null, 0, 0, 0, 0) {}
            )
            header.setMenuOptions(menuItems)
            header
        }
    }

    @Test
    fun menuIconHiddenWithoutOptions() {
        launchAndCapture { activity ->
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            val user = createUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
            vm.setUser(user)
            // No options set — menu icon should be hidden
            header
        }
    }

    @Test
    fun menuWithOptionsExpanded() {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            val header = CometChatMessageHeader(activity)
            val vm = createViewModel()
            header.setViewModel(vm)
            val user = createUser(uid = "user-1", name = "Alice Johnson", status = CometChatConstants.USER_STATUS_ONLINE)
            vm.setUser(user)
            val menuItems = listOf(
                CometChatPopupMenu.MenuItem("search", "Search", null, null, 0, 0, 0, 0) {},
                CometChatPopupMenu.MenuItem("mute", "Mute Notifications", null, null, 0, 0, 0, 0) {},
                CometChatPopupMenu.MenuItem("block", "Block User", null, null, 0, 0, 0, 0) {}
            )
            header.setMenuOptions(menuItems)

            // Let Activity handle layout naturally
            activity.setContentView(
                header,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )

            ShadowLooper.idleMainLooper()

            // Click the menu icon to expand the popup
            val menuIcon = header.findViewById<View>(R.id.messageHeaderMenuIcon)
            menuIcon?.performClick()

            ShadowLooper.idleMainLooper()

            fixAvatarCircularRendering(activity.window.decorView)

            // Capture the full decor view (includes popup if visible)
            activity.window.decorView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }
}
