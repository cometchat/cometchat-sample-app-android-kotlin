package com.cometchat.uikit.compose.presentation.groups

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
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.models.Group
import com.cometchat.uikit.compose.presentation.groups.style.CometChatGroupsStyle
import com.cometchat.uikit.compose.presentation.groups.ui.CometChatGroups
import com.cometchat.uikit.compose.presentation.utils.captureWithPopups
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.repository.GroupsRepository
import com.cometchat.uikit.core.domain.usecase.FetchGroupsUseCase
import com.cometchat.uikit.core.domain.usecase.JoinGroupUseCase
import com.cometchat.uikit.core.viewmodel.CometChatGroupsViewModel
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
 * Roborazzi screenshot tests for CometChatGroups (Compose).
 *
 * Captures golden images for ALL visual states of the CometChatGroups composable
 * using Robolectric for JVM-based rendering and Roborazzi for screenshot capture.
 *
 * Includes:
 * - Static capture tests (captureComposable)
 * - Interaction-driven tests with popup menus and selection modes (captureWithPopups)
 *
 * Each test method produces one golden PNG in src/test/snapshots/groups/.
 *
 * Run:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*.CometChatGroupsScreenshotTest"
 *   ./gradlew :chatuikit-compose:verifyRoborazziDebug --tests "*.CometChatGroupsScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatGroupsScreenshotTest {

    @get:Rule(order = 0)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule(order = 1)
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/groups"
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
     * (e.g., "TA" for The Avengers) instead of a blank transparent image.
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
                CometChatGroups(
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
                CometChatGroups(
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
                CometChatGroups(
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
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroups(5))
                )
            }
        }
    }






    // ==================== Section 3: Selection Mode ====================

    @Test
    fun selectionModeSingle() {
        setContentWithItems(selectionMode = UIKitConstants.SelectionMode.SINGLE)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("The Avengers").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    @Test
    fun selectionModeMultiple() {
        setContentWithItems(selectionMode = UIKitConstants.SelectionMode.MULTIPLE)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("The Avengers").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Justice League").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Design Team").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    // ==================== Section 4: Scroll States ====================

    @Test
    fun scrollToBottom() {
        setContentWithItems(groups = createGroups(20))
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(19)
        composeTestRule.waitForIdle()

        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    @Test
    fun scrollToMiddle() {
        setContentWithItems(groups = createGroups(20))
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
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroups(5)),
                    title = "My Groups"
                )
            }
        }
    }

    @Test
    fun selectionClearedAfterDiscardButtonClick() {
        setContentWithItems(selectionMode = UIKitConstants.SelectionMode.MULTIPLE)
        composeTestRule.waitForIdle()

        // Select items
        composeTestRule.onNodeWithText("The Avengers").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Justice League").performClick()
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
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroups(5)),
                    hideToolbar = true
                )
            }
        }
    }

    @Test
    fun visibilityNoSearchBox() {
        captureComposable {
            CometChatTheme {
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroups(5)),
                    hideSearchBox = true
                )
            }
        }
    }

    @Test
    fun visibilityNoSeparators() {
        captureComposable {
            CometChatTheme {
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroups(5)),
                    hideSeparator = true
                )
            }
        }
    }

    @Test
    fun visibilityNoGroupType() {
        captureComposable {
            CometChatTheme {
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroups(5)),
                    hideGroupType = true
                )
            }
        }
    }

    @Test
    fun visibilityWithBackButton() {
        captureComposable {
            CometChatTheme {
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroups(5)),
                    hideBackIcon = false
                )
            }
        }
    }

    // ==================== Section 7: Custom Views ====================

    @Test
    fun customLoadingView() {
        captureComposable {
            CometChatTheme {
                CometChatGroups(
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
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(emptyList()),
                    emptyView = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No groups found here!", fontSize = 18.sp)
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
                CometChatGroups(
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
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroups(3)),
                    itemView = { group ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE3F2FD))
                                .padding(16.dp)
                        ) {
                            Text("Custom: ${group.name}", fontSize = 16.sp)
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
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroups(3)),
                    leadingView = { group ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF4CAF50))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = group.name?.first()?.toString() ?: "?",
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
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroups(3)),
                    titleView = { group ->
                        Text(
                            text = "★ ${group.name}",
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
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroups(3)),
                    subtitleView = { group ->
                        Text(
                            text = "${group.membersCount} members • ${group.groupType}",
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
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroups(3)),
                    trailingView = { _ ->
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
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroups(3)),
                    style = CometChatGroupsStyle.default().copy(
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
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroups(3)),
                    style = CometChatGroupsStyle.default().copy(
                        backgroundColor = Color(0xFFFFF8E1),
                        titleTextColor = Color(0xFFE65100),
                        toolbarSeparatorColor = Color(0xFFFF6D00)
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
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroups(25))
                )
            }
        }
    }

    @Test
    fun contentMixedGroupTypes() {
        val groups = listOf(
            createMockGroup("g1", "Public Team", CometChatConstants.GROUP_TYPE_PUBLIC, 12),
            createMockGroup("g2", "Private Club", CometChatConstants.GROUP_TYPE_PRIVATE, 8),
            createMockGroup("g3", "Secret Vault", CometChatConstants.GROUP_TYPE_PASSWORD, 5),
            createMockGroup("g4", "Open Forum", CometChatConstants.GROUP_TYPE_PUBLIC, 25),
            createMockGroup("g5", "VIP Lounge", CometChatConstants.GROUP_TYPE_PRIVATE, 15),
            createMockGroup("g6", "Locked Room", CometChatConstants.GROUP_TYPE_PASSWORD, 3)
        )
        captureComposable {
            CometChatTheme {
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(groups)
                )
            }
        }
    }

    @Test
    fun contentHighMemberCount() {
        val groups = listOf(
            createMockGroup("g1", "Mega Community", CometChatConstants.GROUP_TYPE_PUBLIC, 999),
            createMockGroup("g2", "Large Team", CometChatConstants.GROUP_TYPE_PRIVATE, 500),
            createMockGroup("g3", "Growing Group", CometChatConstants.GROUP_TYPE_PASSWORD, 250)
        )
        captureComposable {
            CometChatTheme {
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(groups)
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
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(createGroups(5))
                )
            }
        }
    }


    // ==================== Helper: Static Capture ====================

    /**
     * Launches a fresh Activity, sets Compose content, and captures a screenshot.
     * Each call gets a fresh Activity with a clean ViewModelStore.
     */
    private fun captureComposable(content: @Composable () -> Unit) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setContent { content() }
        }
        scenario.onActivity { activity ->
            val composeView = activity.window.decorView
                .findViewById<android.view.ViewGroup>(android.R.id.content)
                .getChildAt(0)
            composeView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== Helper: Interaction Content ====================

    /**
     * Sets content on the composeTestRule activity with a CometChatGroups composable
     * configured for interaction-based tests (popup, selection, scroll).
     */
    private fun setContentWithItems(
        groups: List<Group> = createGroups(5),
        selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE,
        options: ((Context, Group) -> List<MenuItem>)? = null,
        isDarkTheme: Boolean = false
    ) {
        val viewModel = createViewModel(groups)

        composeTestRule.activity.setContent {
            CometChatTheme(colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()) {
                CometChatGroups(
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
     * Creates a CometChatGroupsViewModel with a fake repository that returns
     * the provided list of groups deterministically.
     */
    private fun createViewModel(groups: List<Group>): CometChatGroupsViewModel {
        var fetched = false
        val repository = object : GroupsRepository {
            override suspend fun fetchGroups(request: GroupsRequest): Result<List<Group>> {
                return if (!fetched) {
                    fetched = true
                    Result.success(groups)
                } else {
                    Result.success(emptyList())
                }
            }
            override suspend fun joinGroup(
                groupId: String,
                groupType: String,
                password: String?
            ): Result<Group> {
                return Result.success(groups.firstOrNull() ?: Group())
            }
            override fun hasMoreGroups() = !fetched
        }
        return CometChatGroupsViewModel(
            fetchGroupsUseCase = FetchGroupsUseCase(repository),
            joinGroupUseCase = JoinGroupUseCase(repository),
            enableListeners = false
        )
    }

    /**
     * Creates a ViewModel that stays in the loading state.
     * Uses a repository that never completes.
     */
    private fun createLoadingViewModel(): CometChatGroupsViewModel {
        val repository = object : GroupsRepository {
            override suspend fun fetchGroups(request: GroupsRequest): Result<List<Group>> {
                // Never returns — ViewModel stays in Loading state
                kotlinx.coroutines.delay(Long.MAX_VALUE)
                return Result.success(emptyList())
            }
            override suspend fun joinGroup(
                groupId: String,
                groupType: String,
                password: String?
            ): Result<Group> {
                return Result.success(Group())
            }
            override fun hasMoreGroups() = true
        }
        return CometChatGroupsViewModel(
            fetchGroupsUseCase = FetchGroupsUseCase(repository),
            joinGroupUseCase = JoinGroupUseCase(repository),
            enableListeners = false
        )
    }

    /**
     * Creates a ViewModel that immediately enters the error state.
     */
    private fun createErrorViewModel(): CometChatGroupsViewModel {
        val repository = object : GroupsRepository {
            override suspend fun fetchGroups(request: GroupsRequest): Result<List<Group>> {
                return Result.failure(
                    com.cometchat.chat.exceptions.CometChatException(
                        "ERR_FETCH_GROUPS",
                        "Failed to fetch groups"
                    )
                )
            }
            override suspend fun joinGroup(
                groupId: String,
                groupType: String,
                password: String?
            ): Result<Group> {
                return Result.success(Group())
            }
            override fun hasMoreGroups() = true
        }
        return CometChatGroupsViewModel(
            fetchGroupsUseCase = FetchGroupsUseCase(repository),
            joinGroupUseCase = JoinGroupUseCase(repository),
            enableListeners = false
        )
    }

    // ==================== Deterministic Mock Data ====================

    /**
     * Creates a list of deterministic groups with fixed names, types, and member counts.
     * Uses real SDK Group objects (not mocks) to ensure all properties are properly
     * initialized and the component renders correctly.
     */
    private fun createGroups(count: Int): List<Group> {
        val names = listOf(
            "The Avengers", "Justice League", "Design Team", "Developers Hub", "S.H.I.E.L.D.",
            "X-Men", "Guardians", "Fantastic Four", "Thunderbolts", "Eternals",
            "Alpha Flight", "New Warriors", "Power Pack", "Young Avengers", "Illuminati",
            "Midnight Sons", "West Coast", "Dark Avengers", "Secret Warriors", "Inhumans",
            "Defenders", "Champions", "Excalibur", "Force Works", "Great Lakes"
        )
        val avatarKeys = listOf(
            "avengers", "justiceleague", "designteam", "developershub", "shield",
            "xmen", "guardians", "fantasticfour", "thunderbolts", "eternals",
            "alphaflight", "newwarriors", "powerpack", "youngavengers", "illuminati",
            "midnightsons", "westcoast", "darkavengers", "secretwarriors", "inhumans",
            "defenders", "champions", "excalibur", "forceworks", "greatlakes"
        )
        val types = listOf(
            CometChatConstants.GROUP_TYPE_PUBLIC,
            CometChatConstants.GROUP_TYPE_PRIVATE,
            CometChatConstants.GROUP_TYPE_PASSWORD
        )
        val memberCounts = listOf(12, 8, 5, 25, 15, 30, 7, 4)

        return (0 until count).map { i ->
            val nameIndex = i % names.size
            Group().apply {
                guid = "group_$i"
                name = names[nameIndex]
                // No icon URL — shows name initials
                groupType = types[i % types.size]
                membersCount = memberCounts[i % memberCounts.size]
            }
        }
    }

    /**
     * Creates a single Group with the given properties.
     * No icon URL — component will show name initials (e.g., "TA", "DT").
     */
    private fun createMockGroup(
        guid: String,
        name: String,
        groupType: String,
        membersCount: Int,
        avatarKey: String? = null
    ): Group {
        return Group().apply {
            this.guid = guid
            this.name = name
            // No icon URL — shows name initials
            this.groupType = groupType
            this.membersCount = membersCount
        }
    }
}
