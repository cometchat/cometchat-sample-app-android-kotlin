package com.cometchat.uikit.compose.presentation.users

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
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.users.style.CometChatUsersStyle
import com.cometchat.uikit.compose.presentation.users.ui.CometChatUsers
import com.cometchat.uikit.compose.presentation.utils.captureWithPopups
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.repository.UsersRepository
import com.cometchat.uikit.core.domain.usecase.FetchUsersUseCase
import com.cometchat.uikit.core.domain.usecase.SearchUsersUseCase
import com.cometchat.uikit.core.viewmodel.CometChatUsersViewModel
import com.github.takahirom.roborazzi.RoborazziRule
import com.cometchat.uikit.compose.presentation.utils.RoborazziConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Roborazzi screenshot tests for CometChatUsers (Compose).
 *
 * Captures golden images for ALL visual states of the CometChatUsers composable
 * using Robolectric for JVM-based rendering and Roborazzi for screenshot capture.
 *
 * Includes:
 * - Static capture tests (captureComposable)
 * - Interaction-driven tests with popup menus and selection modes (captureWithPopups)
 *
 * Each test method produces one golden PNG in src/test/snapshots/users/.
 *
 * Run:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*.CometChatUsersScreenshotTest"
 *   ./gradlew :chatuikit-compose:verifyRoborazziDebug --tests "*.CometChatUsersScreenshotTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatUsersScreenshotTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule(order = 0)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule(order = 1)
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/users"
        )
    )

    private companion object {
        // Fixed epoch timestamp (Jan 1, 2025 UTC) — deterministic across runs
        private const val FIXED_TIMESTAMP = 1735689600L
    }

    @Before
    fun setupDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Configure a Coil ImageLoader with an interceptor that returns a transparent
     * drawable for all image requests. This ensures avatars render deterministically
     * in the Robolectric JVM environment where network requests are unavailable.
     */
    @Before
    fun setupFakeImageLoader() {
        val context = RuntimeEnvironment.getApplication()
        val interceptor = object : Interceptor {
            override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
                // Throw an exception so AsyncImage triggers onError callback,
                // which sets imageLoadFailed = true and shows name initials (e.g., "IM", "CA")
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
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createLoadingViewModel()
                )
            }
        }
    }

    @Test
    fun stateEmpty() {
        captureComposable {
            CometChatTheme {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createViewModel(emptyList())
                )
            }
        }
    }

    @Test
    fun stateError() {
        captureComposable {
            CometChatTheme {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createErrorViewModel()
                )
            }
        }
    }

    @Test
    fun stateContent() {
        captureComposable {
            CometChatTheme {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createViewModel(createUsers(5))
                )
            }
        }
    }

    // ==================== Section 2: Popup Menu ====================
    // NOTE: Long-press popup tests removed from JVM screenshot testing.
    // Robolectric doesn't properly position Compose Popup windows relative to
    // the anchor (popup appears at top instead of near the long-pressed item).
    // These should be tested via instrumented tests on a real device/emulator.

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
        setContentWithItems(users = createUsers(20))
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(19)
        composeTestRule.waitForIdle()

        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    @Test
    fun scrollToMiddle() {
        setContentWithItems(users = createUsers(20))
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
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createViewModel(createUsers(5)),
                    title = "My Contacts"
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
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createViewModel(createUsers(5)),
                    hideToolbar = true
                )
            }
        }
    }

    @Test
    fun visibilityNoSearchBox() {
        captureComposable {
            CometChatTheme {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createViewModel(createUsers(5)),
                    hideSearchBox = true
                )
            }
        }
    }

    @Test
    fun visibilityNoSeparators() {
        captureComposable {
            CometChatTheme {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createViewModel(createUsers(5)),
                    hideSeparator = true
                )
            }
        }
    }

    @Test
    fun visibilityNoUserStatus() {
        captureComposable {
            CometChatTheme {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createViewModel(createUsers(5)),
                    hideStatusIndicator = true
                )
            }
        }
    }

    @Test
    fun visibilityWithBackButton() {
        captureComposable {
            CometChatTheme {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createViewModel(createUsers(5)),
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
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createLoadingViewModel(),
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
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createViewModel(emptyList()),
                    emptyView = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No users found here!", fontSize = 18.sp)
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
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createErrorViewModel(),
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
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createViewModel(createUsers(3)),
                    itemView = { user ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE3F2FD))
                                .padding(16.dp)
                        ) {
                            Text("Custom: ${user.name}", fontSize = 16.sp)
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
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createViewModel(createUsers(3)),
                    leadingView = { user ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF4CAF50))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = user.name?.first()?.toString() ?: "?",
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
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createViewModel(createUsers(3)),
                    titleView = { user ->
                        Text(
                            text = "★ ${user.name}",
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
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createViewModel(createUsers(3)),
                    subtitleView = { user ->
                        Text(
                            text = if (user.status == CometChatConstants.USER_STATUS_ONLINE) "● Active now" else "○ Away",
                            fontSize = 12.sp,
                            color = if (user.status == CometChatConstants.USER_STATUS_ONLINE) Color(0xFF4CAF50) else Color.Gray
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
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createViewModel(createUsers(3)),
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
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createViewModel(createUsers(3)),
                    style = CometChatUsersStyle.default().copy(
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
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createViewModel(createUsers(3)),
                    style = CometChatUsersStyle.default().copy(
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
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createViewModel(createUsers(25))
                )
            }
        }
    }

    @Test
    fun contentOnlineOfflineMix() {
        val users = listOf(
            createMockUser("u1", "Iron Man", CometChatConstants.USER_STATUS_ONLINE),
            createMockUser("u2", "Captain America", CometChatConstants.USER_STATUS_OFFLINE),
            createMockUser("u3", "Spiderman", CometChatConstants.USER_STATUS_ONLINE),
            createMockUser("u4", "Black Widow", CometChatConstants.USER_STATUS_OFFLINE),
            createMockUser("u5", "Thor", CometChatConstants.USER_STATUS_ONLINE),
            createMockUser("u6", "Hulk", CometChatConstants.USER_STATUS_OFFLINE),
            createMockUser("u7", "Hawkeye", CometChatConstants.USER_STATUS_ONLINE),
            createMockUser("u8", "Black Panther", CometChatConstants.USER_STATUS_OFFLINE)
        )
        captureComposable {
            CometChatTheme {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createViewModel(users)
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
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = createViewModel(createUsers(5))
                )
            }
        }
    }

    // ==================== Helper: Static Capture ====================

    /**
     * Sets content on the composeTestRule activity, waits for idle (so ViewModel
     * coroutines and recomposition complete), and captures a screenshot.
     *
     * Uses composeTestRule for proper Compose idle synchronization — this ensures
     * LaunchedEffect coroutines (fetchUsers) complete and the UI recomposes with
     * the fetched data before the screenshot is taken.
     */
    private fun captureComposable(content: @Composable () -> Unit) {
        composeTestRule.activity.setContent { content() }
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    // ==================== Helper: Interaction Content ====================

    /**
     * Sets content on the composeTestRule activity with a CometChatUsers composable
     * configured for interaction-based tests (popup, selection, scroll).
     */
    private fun setContentWithItems(
        users: List<User> = createUsers(5),
        selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE,
        options: ((Context, User) -> List<MenuItem>)? = null,
        isDarkTheme: Boolean = false
    ) {
        val viewModel = createViewModel(users)

        composeTestRule.activity.setContent {
            CometChatTheme(colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    selectionMode = selectionMode,
                    options = options
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    // ==================== ViewModel Factory ====================

    /**
     * Creates a CometChatUsersViewModel with a fake repository that returns
     * the provided list of users deterministically.
     */
    private fun createViewModel(users: List<User>): CometChatUsersViewModel {
        var fetched = false
        val repository = object : UsersRepository {
            override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                return if (!fetched) {
                    fetched = true
                    Result.success(users)
                } else {
                    Result.success(emptyList())
                }
            }
            override fun hasMoreUsers() = !fetched
        }
        return CometChatUsersViewModel(
            fetchUsersUseCase = FetchUsersUseCase(repository),
            searchUsersUseCase = SearchUsersUseCase(repository),
            enableListeners = false
        )
    }

    /**
     * Creates a ViewModel that stays in the loading state.
     * Uses a repository that never completes.
     */
    private fun createLoadingViewModel(): CometChatUsersViewModel {
        val repository = object : UsersRepository {
            override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                // Never returns — ViewModel stays in Loading state
                kotlinx.coroutines.delay(Long.MAX_VALUE)
                return Result.success(emptyList())
            }
            override fun hasMoreUsers() = true
        }
        return CometChatUsersViewModel(
            fetchUsersUseCase = FetchUsersUseCase(repository),
            searchUsersUseCase = SearchUsersUseCase(repository),
            enableListeners = false
        )
    }

    /**
     * Creates a ViewModel that immediately enters the error state.
     */
    private fun createErrorViewModel(): CometChatUsersViewModel {
        val repository = object : UsersRepository {
            override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                return Result.failure(
                    com.cometchat.chat.exceptions.CometChatException(
                        "ERR_FETCH_USERS",
                        "Failed to fetch users"
                    )
                )
            }
            override fun hasMoreUsers() = true
        }
        return CometChatUsersViewModel(
            fetchUsersUseCase = FetchUsersUseCase(repository),
            searchUsersUseCase = SearchUsersUseCase(repository),
            enableListeners = false
        )
    }

    // ==================== Deterministic Mock Data ====================

    /**
     * Creates a list of deterministic users with fixed names and statuses.
     * Avatar URL is intentionally NOT set so the component renders name initials
     * (e.g., "IM" for Iron Man) instead of loading a transparent placeholder image.
     */
    private fun createUsers(count: Int): List<User> {
        val names = listOf(
            "Iron Man", "Captain America", "Spiderman", "Black Widow", "Thor",
            "Hulk", "Hawkeye", "Black Panther", "Doctor Strange", "Ant-Man",
            "Scarlet Witch", "Vision", "Falcon", "Winter Soldier", "War Machine",
            "Star-Lord", "Gamora", "Drax", "Rocket", "Groot",
            "Nebula", "Mantis", "Shang-Chi", "Ms. Marvel", "Moon Knight"
        )
        val statuses = listOf(
            CometChatConstants.USER_STATUS_ONLINE,
            CometChatConstants.USER_STATUS_OFFLINE
        )

        return (0 until count).map { i ->
            val nameIndex = i % names.size
            User().apply {
                uid = "user_$i"
                name = names[nameIndex]
                // No avatar URL set — component will show name initials (e.g., "IM", "CA")
                status = statuses[i % 2]
            }
        }
    }

    /**
     * Creates a single User with the given properties.
     * Avatar URL is intentionally NOT set so initials are displayed.
     */
    private fun createMockUser(
        uid: String,
        name: String,
        status: String,
        avatarKey: String? = null
    ): User {
        return User().apply {
            this.uid = uid
            this.name = name
            // No avatar URL — shows initials
            this.status = status
        }
    }
}
