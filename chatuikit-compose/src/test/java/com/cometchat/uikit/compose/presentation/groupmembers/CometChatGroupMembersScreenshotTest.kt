package com.cometchat.uikit.compose.presentation.groupmembers

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.compose.presentation.groupmembers.style.CometChatGroupMembersStyle
import com.cometchat.uikit.compose.presentation.groupmembers.ui.CometChatGroupMembers
import com.cometchat.uikit.compose.presentation.utils.captureWithPopups
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.repository.GroupMembersRepository
import com.cometchat.uikit.core.domain.usecase.BanGroupMemberUseCase
import com.cometchat.uikit.core.domain.usecase.ChangeMemberScopeUseCase
import com.cometchat.uikit.core.domain.usecase.FetchGroupMembersUseCase
import com.cometchat.uikit.core.domain.usecase.KickGroupMemberUseCase
import com.cometchat.uikit.core.viewmodel.CometChatGroupMembersViewModel
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.compose.presentation.utils.RoborazziConfig
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Roborazzi screenshot tests for CometChatGroupMembers (Compose).
 *
 * Captures golden images for ALL visual states of the CometChatGroupMembers composable
 * using Robolectric for JVM-based rendering and Roborazzi for screenshot capture.
 *
 * Includes:
 * - Static capture tests (captureComposable)
 * - Interaction-driven tests with popup menus and selection modes (captureWithPopups)
 *
 * Each test method produces one golden PNG in src/test/snapshots/groupmembers/.
 *
 * Run:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*.CometChatGroupMembersScreenshotTest"
 *   ./gradlew :chatuikit-compose:verifyRoborazziDebug --tests "*.CometChatGroupMembersScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatGroupMembersScreenshotTest {

    @get:Rule(order = 0)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule(order = 1)
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/groupmembers"
        )
    )

    private companion object {
        private const val AVATAR_BASE_URL = "https://data-us.cometchat.io/assets/images/avatars/"
        // Fixed epoch timestamp (Jan 1, 2025 UTC) — deterministic across runs
        private const val FIXED_TIMESTAMP = 1735689600L
    }

    /**
     * Configure a Coil ImageLoader with an interceptor that returns a transparent
     * Configure a Coil ImageLoader with an interceptor that forces image load failure.
     * This ensures the CometChatAvatar composable falls back to showing name initials
     * (e.g., "IM" for Iron Man) instead of a blank transparent image.
     */
    @Before
    fun setupFakeImageLoader() {
        val context = RuntimeEnvironment.getApplication()
        val interceptor = object : Interceptor {
            override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
                // Throw an exception so AsyncImage triggers onError callback,
                // which sets imageLoadFailed = true and shows name initials
                throw Exception("Fake image load failure for screenshot tests")
            }
        }
        val imageLoader = ImageLoader.Builder(context)
            .components { add(interceptor) }
            .crossfade(false)
            .build()
        Coil.setImageLoader(imageLoader)
    }

    // ==================== Section 1: UI States ====================

    @Test
    fun stateLoading() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createLoadingViewModel()
                )
            }
        }
    }

    @Test
    fun stateEmpty() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(emptyList())
                )
            }
        }
    }

    @Test
    fun stateError() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createErrorViewModel()
                )
            }
        }
    }

    @Test
    fun stateContent() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroupMembers(5))
                )
            }
        }
    }

    // ==================== Section 2: Popup Menu ====================




    // ==================== Section 3: Selection Mode ====================

    @Test
    fun selectionModeSingle() {
        setContentWithItems(selectionMode = UIKitConstants.SelectionMode.SINGLE)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Iron Man").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    @Test
    fun selectionModeMultiple() {
        setContentWithItems(selectionMode = UIKitConstants.SelectionMode.MULTIPLE)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Iron Man").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Captain America").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Spiderman").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    // ==================== Section 4: Scroll States ====================

    @Test
    fun scrollToBottom() {
        setContentWithItems(members = createGroupMembers(20))
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(19)
        composeTestRule.waitForIdle()

        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    @Test
    fun scrollToMiddle() {
        setContentWithItems(members = createGroupMembers(20))
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(10)
        composeTestRule.waitForIdle()

        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    // ==================== Section 5: Toolbar ====================

    @Test
    fun toolbarCustomTitle() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroupMembers(5)),
                    title = "Team Members"
                )
            }
        }
    }

    @Test
    fun selectionClearedAfterDiscardButtonClick() {
        setContentWithItems(selectionMode = UIKitConstants.SelectionMode.MULTIPLE)
        composeTestRule.waitForIdle()

        // Select items
        composeTestRule.onNodeWithText("Iron Man").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Captain America").performClick()
        composeTestRule.waitForIdle()

        // Click discard button
        composeTestRule.onNodeWithContentDescription("Discard selection").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    // ==================== Section 6: Visibility ====================

    @Test
    fun visibilityNoToolbar() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroupMembers(5)),
                    hideToolbar = true
                )
            }
        }
    }

    @Test
    fun visibilityNoSearchBox() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroupMembers(5)),
                    hideSearch = true
                )
            }
        }
    }

    @Test
    fun visibilityNoSeparators() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroupMembers(5)),
                    hideSeparator = true
                )
            }
        }
    }

    @Test
    fun visibilityNoUserStatus() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroupMembers(5)),
                    hideUserStatus = true
                )
            }
        }
    }

    @Test
    fun visibilityWithBackButton() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroupMembers(5)),
                    hideBackButton = false
                )
            }
        }
    }

    // ==================== Section 7: Custom Views ====================

    @Test
    fun customLoadingView() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createLoadingViewModel(),
                    loadingView = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Custom Loading...", fontSize = 18.sp)
                        }
                    }
                )
            }
        }
    }

    @Test
    fun customEmptyView() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(emptyList()),
                    emptyView = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No members found here!", fontSize = 18.sp)
                        }
                    }
                )
            }
        }
    }

    @Test
    fun customErrorView() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createErrorViewModel(),
                    errorView = { _ ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Custom Error View", fontSize = 18.sp, color = Color.Red)
                        }
                    }
                )
            }
        }
    }

    @Test
    fun customItemView() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroupMembers(3)),
                    listItemView = { member ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE3F2FD))
                                .padding(16.dp)
                        ) {
                            Text("Custom: ${member.name}", fontSize = 16.sp)
                        }
                    }
                )
            }
        }
    }

    @Test
    fun customLeadingView() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroupMembers(3)),
                    leadingView = { member ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF4CAF50))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = member.name?.first()?.toString() ?: "?",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                )
            }
        }
    }

    @Test
    fun customTitleView() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroupMembers(3)),
                    titleView = { member ->
                        Text(
                            text = "★ ${member.name}",
                            fontSize = 16.sp,
                            color = Color(0xFF1565C0)
                        )
                    }
                )
            }
        }
    }

    @Test
    fun customSubtitleView() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroupMembers(3)),
                    subtitleView = { member ->
                        Text(
                            text = "Scope: ${member.scope} • ${member.status}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                )
            }
        }
    }

    @Test
    fun customTrailingView() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroupMembers(3)),
                    tailView = { _ ->
                        Text(
                            text = "→",
                            fontSize = 20.sp,
                            color = Color.Gray
                        )
                    }
                )
            }
        }
    }

    // ==================== Section 8: Style ====================

    @Test
    fun styleCustomBackground() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroupMembers(3)),
                    style = CometChatGroupMembersStyle.default().copy(
                        backgroundColor = Color(0xFFF3E5F5)
                    )
                )
            }
        }
    }

    @Test
    fun stylingCustomColors() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroupMembers(3)),
                    style = CometChatGroupMembersStyle.default().copy(
                        backgroundColor = Color(0xFFFFF8E1),
                        titleTextColor = Color(0xFFE65100),
                        separatorColor = Color(0xFFFF6D00)
                    )
                )
            }
        }
    }

    // ==================== Section 9: Content ====================

    @Test
    fun contentLargeList() {
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroupMembers(25))
                )
            }
        }
    }

    @Test
    fun contentAllScopes() {
        val members = listOf(
            createMockGroupMember("m1", "Tony Stark", CometChatConstants.SCOPE_ADMIN, CometChatConstants.USER_STATUS_ONLINE),
            createMockGroupMember("m2", "Steve Rogers", CometChatConstants.SCOPE_MODERATOR, CometChatConstants.USER_STATUS_OFFLINE),
            createMockGroupMember("m3", "Peter Parker", CometChatConstants.SCOPE_PARTICIPANT, CometChatConstants.USER_STATUS_ONLINE),
            createMockGroupMember("m4", "Natasha Romanoff", CometChatConstants.SCOPE_ADMIN, CometChatConstants.USER_STATUS_OFFLINE),
            createMockGroupMember("m5", "Thor Odinson", CometChatConstants.SCOPE_MODERATOR, CometChatConstants.USER_STATUS_ONLINE),
            createMockGroupMember("m6", "Bruce Banner", CometChatConstants.SCOPE_PARTICIPANT, CometChatConstants.USER_STATUS_OFFLINE)
        )
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(members)
                )
            }
        }
    }

    @Test
    fun contentModeratorBadgeTruncation() {
        val members = listOf(
            createMockGroupMember("m1", "Iron Man", CometChatConstants.SCOPE_MODERATOR, CometChatConstants.USER_STATUS_ONLINE),
            createMockGroupMember("m2", "Captain America", CometChatConstants.SCOPE_MODERATOR, CometChatConstants.USER_STATUS_OFFLINE),
            createMockGroupMember("m3", "Spiderman", CometChatConstants.SCOPE_MODERATOR, CometChatConstants.USER_STATUS_ONLINE)
        )
        captureComposable {
            CometChatTheme {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(members)
                )
            }
        }
    }

    // ==================== Section 10: Dark Theme ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroupMembers(5))
                )
            }
        }
    }


    // ==================== Helper: Static Capture ====================

    /**
     * Sets content on the composeTestRule activity and captures a screenshot.
     *
     * Uses composeTestRule (not a separate ActivityScenario) to ensure that
     * LaunchedEffect coroutines (setGroup + fetchGroupMembers) complete before
     * the screenshot is captured. CometChatGroupMembers fetches data via
     * LaunchedEffect — not in the ViewModel's init — so the compose test
     * framework's idle mechanism is required to synchronize.
     */
    private fun captureComposable(content: @Composable () -> Unit) {
        composeTestRule.activity.setContent { content() }
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    // ==================== Helper: Interaction Content ====================

    /**
     * Sets content on the composeTestRule activity with a CometChatGroupMembers composable
     * configured for interaction-based tests (popup, selection, scroll).
     */
    private fun setContentWithItems(
        members: List<GroupMember> = createGroupMembers(5),
        selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE,
        options: ((GroupMember) -> List<MenuItem>)? = null,
        isDarkTheme: Boolean = false
    ) {
        val viewModel = createViewModel(members)

        composeTestRule.activity.setContent {
            CometChatTheme(colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()) {
                CometChatGroupMembers(
                    group = createGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    selectionMode = selectionMode,
                    options = options
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    // ==================== ViewModel Factory ====================

    /**
     * Creates a CometChatGroupMembersViewModel with a fake repository that returns
     * the provided list of group members deterministically.
     */
    private fun createViewModel(members: List<GroupMember>): CometChatGroupMembersViewModel {
        var fetched = false
        val repository = object : GroupMembersRepository {
            override suspend fun fetchGroupMembers(
                guid: String,
                limit: Int,
                searchKeyword: String?
            ): Result<List<GroupMember>> {
                return if (!fetched) {
                    fetched = true
                    Result.success(members)
                } else {
                    Result.success(emptyList())
                }
            }
            override suspend fun kickMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun banMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun changeMemberScope(guid: String, uid: String, scope: String) = Result.success(Unit)
            override fun hasMore() = !fetched
            override fun resetRequest() { fetched = false }
        }
        return CometChatGroupMembersViewModel(
            fetchGroupMembersUseCase = FetchGroupMembersUseCase(repository),
            kickGroupMemberUseCase = KickGroupMemberUseCase(repository),
            banGroupMemberUseCase = BanGroupMemberUseCase(repository),
            changeMemberScopeUseCase = ChangeMemberScopeUseCase(repository),
            enableListeners = false
        )
    }

    /**
     * Creates a ViewModel that stays in the loading state.
     * Uses a repository that never completes.
     */
    private fun createLoadingViewModel(): CometChatGroupMembersViewModel {
        val repository = object : GroupMembersRepository {
            override suspend fun fetchGroupMembers(
                guid: String,
                limit: Int,
                searchKeyword: String?
            ): Result<List<GroupMember>> {
                // Never returns — ViewModel stays in Loading state
                kotlinx.coroutines.delay(Long.MAX_VALUE)
                return Result.success(emptyList())
            }
            override suspend fun kickMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun banMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun changeMemberScope(guid: String, uid: String, scope: String) = Result.success(Unit)
            override fun hasMore() = true
            override fun resetRequest() {}
        }
        return CometChatGroupMembersViewModel(
            fetchGroupMembersUseCase = FetchGroupMembersUseCase(repository),
            kickGroupMemberUseCase = KickGroupMemberUseCase(repository),
            banGroupMemberUseCase = BanGroupMemberUseCase(repository),
            changeMemberScopeUseCase = ChangeMemberScopeUseCase(repository),
            enableListeners = false
        )
    }

    /**
     * Creates a ViewModel that immediately enters the error state.
     */
    private fun createErrorViewModel(): CometChatGroupMembersViewModel {
        val repository = object : GroupMembersRepository {
            override suspend fun fetchGroupMembers(
                guid: String,
                limit: Int,
                searchKeyword: String?
            ): Result<List<GroupMember>> {
                return Result.failure(
                    com.cometchat.chat.exceptions.CometChatException(
                        "ERR_FETCH_GROUP_MEMBERS",
                        "Failed to fetch group members"
                    )
                )
            }
            override suspend fun kickMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun banMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun changeMemberScope(guid: String, uid: String, scope: String) = Result.success(Unit)
            override fun hasMore() = true
            override fun resetRequest() {}
        }
        return CometChatGroupMembersViewModel(
            fetchGroupMembersUseCase = FetchGroupMembersUseCase(repository),
            kickGroupMemberUseCase = KickGroupMemberUseCase(repository),
            banGroupMemberUseCase = BanGroupMemberUseCase(repository),
            changeMemberScopeUseCase = ChangeMemberScopeUseCase(repository),
            enableListeners = false
        )
    }

    // ==================== Deterministic Mock Data ====================

    /**
     * Creates a deterministic Group object used as the parent group for all tests.
     */
    private fun createGroup(): Group {
        return Group().apply {
            guid = "group_test"
            name = "Test Group"
            // No icon URL — shows name initials
            groupType = CometChatConstants.GROUP_TYPE_PUBLIC
            membersCount = 10
        }
    }

    /**
     * Creates a list of deterministic group members with fixed names, scopes, and statuses.
     * No avatar URL — component will show name initials (e.g., "IM", "CA").
     */
    private fun createGroupMembers(count: Int): List<GroupMember> {
        val names = listOf(
            "Iron Man", "Captain America", "Spiderman", "Black Widow", "Thor",
            "Hulk", "Hawkeye", "Black Panther", "Doctor Strange", "Ant-Man",
            "Scarlet Witch", "Vision", "Falcon", "Winter Soldier", "War Machine",
            "Star-Lord", "Gamora", "Drax", "Rocket", "Groot",
            "Nebula", "Mantis", "Shang-Chi", "Ms. Marvel", "Moon Knight"
        )
        val scopes = listOf(
            CometChatConstants.SCOPE_ADMIN,
            CometChatConstants.SCOPE_MODERATOR,
            CometChatConstants.SCOPE_PARTICIPANT,
            CometChatConstants.SCOPE_PARTICIPANT
        )
        val statuses = listOf(
            CometChatConstants.USER_STATUS_ONLINE,
            CometChatConstants.USER_STATUS_OFFLINE
        )

        return (0 until count).map { i ->
            val nameIndex = i % names.size
            GroupMember("member_$i", scopes[i % scopes.size]).apply {
                name = names[nameIndex]
                // No avatar URL — shows name initials
                status = statuses[i % 2]
            }
        }
    }

    /**
     * Creates a single GroupMember with the given properties.
     * No avatar URL — component will show name initials.
     */
    private fun createMockGroupMember(
        uid: String,
        name: String,
        scope: String,
        status: String,
        avatarKey: String? = null
    ): GroupMember {
        return GroupMember(uid, scope).apply {
            this.name = name
            // No avatar URL — shows name initials
            this.status = status
        }
    }
}
